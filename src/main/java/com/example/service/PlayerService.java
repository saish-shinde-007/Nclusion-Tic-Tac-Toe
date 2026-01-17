package com.example.service;

import com.example.model.Player;
import com.example.model.PlayerStats;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class PlayerService {

    // In-memory storage (can be replaced with repository for persistence)
    private final Map<String, Player> players = new ConcurrentHashMap<>();

    /** Create a new player with the given name and email. */
    public Player createPlayer(String name, String email) {
        if (findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("Player with this email already exists");
        }
        var player = new Player(name, email);
        players.put(player.getId(), player);
        return player;
    }

    /** Get player by ID. */
    public Optional<Player> findById(String id) {
        return Optional.ofNullable(players.get(id));
    }

    public Optional<Player> findByEmail(String email) {
        return players.values().stream()
                .filter(player -> player.getEmail().equals(email))
                .findFirst();
    }

    public List<Player> findAll() {
        return new ArrayList<>(players.values());
    }

    public Player updatePlayer(String id, String name, String email) {
        var player = findById(id).orElseThrow(() -> new IllegalArgumentException("Player not found"));

        if (!player.getEmail().equals(email)) {
            findByEmail(email).ifPresent(existing -> {
                if (!existing.getId().equals(id)) {
                    throw new IllegalArgumentException("Email already in use by another player");
                }
            });
        }

        player.setName(name);
        player.setEmail(email);
        return player;
    }

    public boolean deletePlayer(String id) {
        return players.remove(id) != null;
    }

    public List<Player> searchByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return findAll();
        }
        var searchTerm = name.toLowerCase().trim();
        return players.values().stream()
                .filter(player -> player.getName().toLowerCase().contains(searchTerm))
                .collect(Collectors.toList());
    }

    public PlayerStats getPlayerStats(String id) {
        var player = findById(id).orElseThrow(() -> new IllegalArgumentException("Player not found"));
        return player.getStats();
    }

    public List<Player> getLeaderboard(int limit) {
        return players.values().stream()
                .filter(player -> player.getStats().getGamesPlayed() > 0)
                .sorted((p1, p2) -> Double.compare(p2.getStats().getWinRate(), p1.getStats().getWinRate()))
                .limit(limit)
                .collect(Collectors.toList());
    }

    public List<Player> getMostActivePlayers(int limit) {
        return players.values().stream()
                .sorted((p1, p2) -> Integer.compare(p2.getStats().getGamesPlayed(), p1.getStats().getGamesPlayed()))
                .limit(limit)
                .collect(Collectors.toList());
    }

    public List<Player> getMostEfficientPlayers(int limit) {
        return players.values().stream()
                .filter(player -> player.getStats().getGamesWon() > 0)
                .sorted((p1, p2) -> Double.compare(p2.getStats().getEfficiency(), p1.getStats().getEfficiency()))
                .limit(limit)
                .collect(Collectors.toList());
    }

    public void updatePlayerStats(String playerId, boolean won, boolean drawn, int movesMade) {
        var player = findById(playerId).orElseThrow(() -> new IllegalArgumentException("Player not found"));
        var stats = player.getStats();
        stats.incrementGamesPlayed();

        if (won) {
            stats.incrementGamesWon();
        } else if (drawn) {
            stats.incrementGamesDrawn();
        } else {
            stats.incrementGamesLost();
        }
        stats.addMoves(movesMade);
    }

    public long getTotalPlayerCount() {
        return players.size();
    }

    public List<Player> getPlayersCreatedBetween(Date startDate, Date endDate) {
        return players.values().stream()
                .filter(player -> {
                    var createdAt = Timestamp.valueOf(player.getCreatedAt());
                    return createdAt.after(startDate) && createdAt.before(endDate);
                })
                .collect(Collectors.toList());
    }
}
