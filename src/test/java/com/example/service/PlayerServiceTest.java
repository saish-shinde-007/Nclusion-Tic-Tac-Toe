package com.example.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PlayerServiceTest {

    private PlayerService playerService;

    @BeforeEach
    void setUp() {
        playerService = new PlayerService();
    }

    @Test
    void createPlayer_shouldReturnPlayerWithId() {
        var player = playerService.createPlayer("Alice", "alice@test.com");
        assertNotNull(player.getId());
        assertEquals("Alice", player.getName());
        assertEquals("alice@test.com", player.getEmail());
    }

    @Test
    void findById_shouldReturnPlayer() {
        var created = playerService.createPlayer("Bob", "bob@test.com");
        var found = playerService.findById(created.getId());
        assertTrue(found.isPresent());
        assertEquals("Bob", found.get().getName());
    }

    @Test
    void findAll_shouldReturnAllPlayers() {
        playerService.createPlayer("Alice", "alice@test.com");
        playerService.createPlayer("Bob", "bob@test.com");
        assertEquals(2, playerService.findAll().size());
    }

    @Test
    void deletePlayer_shouldRemovePlayer() {
        var player = playerService.createPlayer("Test", "test@test.com");
        assertTrue(playerService.deletePlayer(player.getId()));
        assertFalse(playerService.findById(player.getId()).isPresent());
    }

    @Test
    void getLeaderboard_shouldSortByWinRate() {
        var alice = playerService.createPlayer("Alice", "alice@test.com");
        var bob = playerService.createPlayer("Bob", "bob@test.com");
        // Alice: 2 wins / 2 games = 100% win rate
        alice.getStats().incrementGamesPlayed();
        alice.getStats().incrementGamesPlayed();
        alice.getStats().incrementGamesWon();
        alice.getStats().incrementGamesWon();
        // Bob: 1 win / 2 games = 50% win rate
        bob.getStats().incrementGamesPlayed();
        bob.getStats().incrementGamesPlayed();
        bob.getStats().incrementGamesWon();

        var leaderboard = playerService.getLeaderboard(10);
        assertEquals("Alice", leaderboard.get(0).getName());
        assertEquals("Bob", leaderboard.get(1).getName());
    }
}
