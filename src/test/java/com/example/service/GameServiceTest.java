package com.example.service;

import com.example.model.Game;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GameServiceTest {

    private GameService gameService;
    private PlayerService playerService;

    @BeforeEach
    void setUp() {
        playerService = new PlayerService();
        gameService = new GameService(playerService);
    }

    @Test
    void createGame_shouldReturnGameWithId() {
        var game = gameService.createGame("Test Game");
        assertNotNull(game.getId());
        assertEquals("Test Game", game.getName());
        assertEquals(Game.GameStatus.WAITING, game.getStatus());
    }

    @Test
    void joinGame_shouldAddPlayerToGame() {
        var player = playerService.createPlayer("Alice", "alice@test.com");
        var game = gameService.createGame("Test");

        var updated = gameService.joinGame(game.getId(), player.getId());
        assertEquals(1, updated.getPlayers().size());
    }

    @Test
    void joinGame_withTwoPlayers_shouldActivateGame() {
        var alice = playerService.createPlayer("Alice", "alice@test.com");
        var bob = playerService.createPlayer("Bob", "bob@test.com");
        var game = gameService.createGame("Test");

        gameService.joinGame(game.getId(), alice.getId());
        var updated = gameService.joinGame(game.getId(), bob.getId());

        assertEquals(Game.GameStatus.ACTIVE, updated.getStatus());
        assertNotNull(updated.getCurrentPlayer());
    }

    @Test
    void makeMove_shouldUpdateBoard() {
        var alice = playerService.createPlayer("Alice", "alice@test.com");
        var bob = playerService.createPlayer("Bob", "bob@test.com");
        var game = gameService.createGame("Test");
        gameService.joinGame(game.getId(), alice.getId());
        gameService.joinGame(game.getId(), bob.getId());

        var updated = gameService.makeMove(game.getId(), alice.getId(), 0, 0);
        assertEquals("X", updated.getBoard().get(0));
    }

    @Test
    void makeMove_wrongTurn_shouldThrow() {
        var alice = playerService.createPlayer("Alice", "alice@test.com");
        var bob = playerService.createPlayer("Bob", "bob@test.com");
        var game = gameService.createGame("Test");
        gameService.joinGame(game.getId(), alice.getId());
        gameService.joinGame(game.getId(), bob.getId());

        assertThrows(GameService.InvalidMoveException.class,
                () -> gameService.makeMove(game.getId(), bob.getId(), 0, 0));
    }

    @Test
    void getGameStats_shouldReturnCorrectCounts() {
        gameService.createGame("Game 1");
        gameService.createGame("Game 2");

        var stats = gameService.getGameStats();
        assertEquals(2, stats.totalGames());
        assertEquals(2, stats.waitingGames());
    }
}
