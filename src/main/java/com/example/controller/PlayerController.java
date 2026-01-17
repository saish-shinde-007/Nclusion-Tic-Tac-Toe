package com.example.controller;

import com.example.model.Player;
import com.example.model.PlayerStats;
import com.example.service.PlayerService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/players")
public class PlayerController {

    private final PlayerService playerService;

    @Autowired
    public PlayerController(PlayerService playerService) {
        this.playerService = playerService;
    }

    // Create a new player
    @PostMapping
    public ResponseEntity<Player> createPlayer(@Valid @RequestBody CreatePlayerRequest request) {
        try {
            var player = playerService.createPlayer(request.getName(), request.getEmail());
            return ResponseEntity.status(HttpStatus.CREATED).body(player);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Get player by ID
    @GetMapping("/{id}")
    public ResponseEntity<Player> getPlayer(@PathVariable String id) {
        return playerService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Get all players
    @GetMapping
    public ResponseEntity<List<Player>> getAllPlayers(
            @RequestParam(required = false) String name) {
        var players = playerService.searchByName(name);
        return ResponseEntity.ok(players);
    }

    // Update player
    @PutMapping("/{id}")
    public ResponseEntity<Player> updatePlayer(
            @PathVariable String id,
            @Valid @RequestBody UpdatePlayerRequest request) {
        try {
            var player = playerService.updatePlayer(id, request.getName(), request.getEmail());
            return ResponseEntity.ok(player);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Delete player
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePlayer(@PathVariable String id) {
        boolean deleted = playerService.deletePlayer(id);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    // Get player statistics
    @GetMapping("/{id}/stats")
    public ResponseEntity<PlayerStats> getPlayerStats(@PathVariable String id) {
        try {
            var stats = playerService.getPlayerStats(id);
            return ResponseEntity.ok(stats);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Get leaderboard
    @GetMapping("/leaderboard")
    public ResponseEntity<List<Player>> getLeaderboard(
            @RequestParam(defaultValue = "10") int limit) {
        var leaderboard = playerService.getLeaderboard(limit);
        return ResponseEntity.ok(leaderboard);
    }

    // Get most active players
    @GetMapping("/most-active")
    public ResponseEntity<List<Player>> getMostActivePlayers(
            @RequestParam(defaultValue = "10") int limit) {
        var players = playerService.getMostActivePlayers(limit);
        return ResponseEntity.ok(players);
    }

    // Get most efficient players
    @GetMapping("/most-efficient")
    public ResponseEntity<List<Player>> getMostEfficientPlayers(
            @RequestParam(defaultValue = "10") int limit) {
        var players = playerService.getMostEfficientPlayers(limit);
        return ResponseEntity.ok(players);
    }

    // Get player count
    @GetMapping("/count")
    public ResponseEntity<Map<String, Long>> getPlayerCount() {
        var count = playerService.getTotalPlayerCount();
        return ResponseEntity.ok(Map.of("count", count));
    }

    // Request/Response DTOs
    @Getter
    @Setter
    public static class CreatePlayerRequest {
        @NotBlank(message = "Player name is required")
        @Size(min = 1, max = 100, message = "Player name must be between 1 and 100 characters")
        private String name;

        @NotBlank(message = "Player email is required")
        @Email(regexp = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$", message = "Please provide a valid email address")
        private String email;
    }

    @Getter
    @Setter
    public static class UpdatePlayerRequest {
        @NotBlank(message = "Player name is required")
        @Size(min = 1, max = 100, message = "Player name must be between 1 and 100 characters")
        private String name;

        @NotBlank(message = "Player email is required")
        @Email(regexp = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$", message = "Please provide a valid email address")
        private String email;
    }
}
