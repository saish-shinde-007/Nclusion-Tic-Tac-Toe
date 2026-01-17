package com.example.service;

import com.example.model.Game;
import com.example.model.Game.GameStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/** Service for managing game logic and state. */
@Service
public class GameService {

    // In-memory storage for games
    private final Map<String, Game> games = new ConcurrentHashMap<>();
    private final PlayerService playerService;

    public GameService(PlayerService playerService) {
        this.playerService = playerService;
    }

    /** Create a new game. */
    public Game createGame(String name) {
        var game = new Game(name);
        games.put(game.getId(), game);
        return game;
    }

    public Optional<Game> findById(String id) {
        return Optional.ofNullable(games.get(id));
    }

    public List<Game> findAll() {
        return new ArrayList<>(games.values());
    }

    public List<Game> findWaitingGames() {
        return games.values().stream()
                .filter(g -> g.getStatus() == GameStatus.WAITING)
                .collect(Collectors.toList());
    }

    public Game joinGame(String gameId, String playerId) {
        var game = findById(gameId).orElseThrow(() -> new GameNotFoundException("Game not found"));
        var player = playerService.findById(playerId)
                .orElseThrow(() -> new PlayerNotFoundException("Player not found"));

        if (game.getPlayers().size() >= 2)
            throw new InvalidGameStateException("Game is full");
        if (game.getPlayers().contains(player))
            throw new InvalidGameStateException("Already in game");

        game.addPlayer(player);
        return game;
    }

    public Game makeMove(String gameId, String playerId, int row, int col) {
        var game = findById(gameId).orElseThrow(() -> new GameNotFoundException("Game not found"));
        var player = playerService.findById(playerId)
                .orElseThrow(() -> new PlayerNotFoundException("Player not found"));

        if (game.getStatus() != GameStatus.ACTIVE)
            throw new InvalidGameStateException("Game not active");
        if (!game.getCurrentPlayer().getId().equals(playerId))
            throw new InvalidMoveException("Not your turn");

        var position = row * 3 + col;
        if (position < 0 || position > 8)
            throw new InvalidMoveException("Invalid position");
        if (game.getBoard().get(position) != null)
            throw new InvalidMoveException("Cell occupied");

        game.makeMove(player, position);
        return game;
    }

    public GameStatsResponse getGameStats() {
        var total = games.size();
        var waiting = games.values().stream().filter(g -> g.getStatus() == GameStatus.WAITING).count();
        var active = games.values().stream().filter(g -> g.getStatus() == GameStatus.ACTIVE).count();
        var completed = games.values().stream().filter(g -> g.getStatus() == GameStatus.COMPLETED).count();
        var draw = games.values().stream().filter(g -> g.getStatus() == GameStatus.DRAW).count();
        return new GameStatsResponse(total, waiting, active, completed, draw);
    }

    public boolean deleteGame(String id) {
        return games.remove(id) != null;
    }

    public static class GameNotFoundException extends RuntimeException {
        public GameNotFoundException(String m) {
            super(m);
        }
    }

    public static class PlayerNotFoundException extends RuntimeException {
        public PlayerNotFoundException(String m) {
            super(m);
        }
    }

    public static class InvalidGameStateException extends RuntimeException {
        public InvalidGameStateException(String m) {
            super(m);
        }
    }

    public static class InvalidMoveException extends RuntimeException {
        public InvalidMoveException(String m) {
            super(m);
        }
    }

    public record GameStatsResponse(long totalGames, long waitingGames, long activeGames, long completedGames,
            long drawGames) {
    }
}
