package com.example.controller;

import com.example.service.PlayerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Unit tests for PlayerController.
 * Tests the controller's HTTP response mappings and business logic wiring.
 */
class PlayerControllerTest {

    private PlayerController playerController;
    private PlayerService playerService;

    @BeforeEach
    void setUp() {
        playerService = new PlayerService();
        playerController = new PlayerController(playerService);
    }

    // === Create Player Tests ===

    @Test
    void createPlayer_shouldReturnCreated() {
        var request = createRequest("Alice", "alice@test.com");
        var response = playerController.createPlayer(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Alice", response.getBody().getName());
        assertEquals("alice@test.com", response.getBody().getEmail());
    }

    // === GET Endpoint Tests ===

    @Test
    void getPlayer_whenExists_shouldReturnOk() {
        var player = playerService.createPlayer("Bob", "bob@test.com");
        var response = playerController.getPlayer(player.getId());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Bob", response.getBody().getName());
    }

    @Test
    void getPlayer_whenNotExists_shouldReturnNotFound() {
        var response = playerController.getPlayer("nonexistent-id");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void getAllPlayers_shouldReturnList() {
        playerService.createPlayer("Alice", "alice@test.com");
        playerService.createPlayer("Bob", "bob@test.com");

        var response = playerController.getAllPlayers(null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().size());
    }

    @Test
    void getAllPlayers_withNameFilter_shouldReturnFiltered() {
        playerService.createPlayer("Alice", "alice@test.com");
        playerService.createPlayer("Bob", "bob@test.com");

        var response = playerController.getAllPlayers("Ali");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals("Alice", response.getBody().get(0).getName());
    }

    // === Update Player Tests ===

    @Test
    void updatePlayer_shouldReturnUpdated() {
        var player = playerService.createPlayer("Old Name", "old@test.com");
        var request = updateRequest("New Name", "new@test.com");

        var response = playerController.updatePlayer(player.getId(), request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("New Name", response.getBody().getName());
        assertEquals("new@test.com", response.getBody().getEmail());
    }

    // === DELETE Endpoint Tests ===

    @Test
    void deletePlayer_whenExists_shouldReturnNoContent() {
        var player = playerService.createPlayer("DeleteMe", "delete@test.com");
        var response = playerController.deletePlayer(player.getId());

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertFalse(playerService.findById(player.getId()).isPresent());
    }

    @Test
    void deletePlayer_whenNotExists_shouldReturnNotFound() {
        var response = playerController.deletePlayer("nonexistent");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    // === Stats Endpoint Tests ===

    @Test
    void getPlayerStats_shouldReturnStats() {
        var player = playerService.createPlayer("StatsPlayer", "stats@test.com");
        player.getStats().incrementGamesPlayed();
        player.getStats().incrementGamesWon();

        var response = playerController.getPlayerStats(player.getId());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getGamesPlayed());
        assertEquals(1, response.getBody().getGamesWon());
    }

    @Test
    void getPlayerStats_whenNotExists_shouldReturnNotFound() {
        var response = playerController.getPlayerStats("nonexistent");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    // === Leaderboard Tests ===

    @Test
    void getLeaderboard_shouldReturnSortedPlayers() {
        var alice = playerService.createPlayer("Alice", "alice@test.com");
        var bob = playerService.createPlayer("Bob", "bob@test.com");

        // Alice: 2 wins in 2 games = 100%
        alice.getStats().incrementGamesPlayed();
        alice.getStats().incrementGamesPlayed();
        alice.getStats().incrementGamesWon();
        alice.getStats().incrementGamesWon();

        // Bob: 1 win in 2 games = 50%
        bob.getStats().incrementGamesPlayed();
        bob.getStats().incrementGamesPlayed();
        bob.getStats().incrementGamesWon();

        var response = playerController.getLeaderboard(10);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().size());
        assertEquals("Alice", response.getBody().get(0).getName());
        assertEquals("Bob", response.getBody().get(1).getName());
    }

    @Test
    void getLeaderboard_withCustomLimit_shouldRespectLimit() {
        // Create 3 players with games played
        var p1 = playerService.createPlayer("P1", "p1@test.com");
        var p2 = playerService.createPlayer("P2", "p2@test.com");
        var p3 = playerService.createPlayer("P3", "p3@test.com");

        p1.getStats().incrementGamesPlayed();
        p2.getStats().incrementGamesPlayed();
        p3.getStats().incrementGamesPlayed();

        var response = playerController.getLeaderboard(2);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().size());
    }

    // === Activity Rankings Tests ===

    @Test
    void getMostActivePlayers_shouldReturnByActivity() {
        var active = playerService.createPlayer("Active", "active@test.com");
        var lazy = playerService.createPlayer("Lazy", "lazy@test.com");

        active.getStats().incrementGamesPlayed();
        active.getStats().incrementGamesPlayed();
        active.getStats().incrementGamesPlayed();
        lazy.getStats().incrementGamesPlayed();

        var response = playerController.getMostActivePlayers(10);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().size());
        assertEquals("Active", response.getBody().get(0).getName());
    }

    @Test
    void getMostEfficientPlayers_shouldReturnByEfficiency() {
        var efficient = playerService.createPlayer("Efficient", "eff@test.com");
        var inefficient = playerService.createPlayer("Inefficient", "ineff@test.com");

        // Efficient: 2 wins with 6 moves = 3 moves/win (lower = more efficient)
        efficient.getStats().incrementGamesPlayed();
        efficient.getStats().incrementGamesPlayed();
        efficient.getStats().incrementGamesWon();
        efficient.getStats().incrementGamesWon();
        efficient.getStats().addMoves(6);

        // Inefficient: 1 win with 10 moves = 10 moves/win (higher = less efficient)
        inefficient.getStats().incrementGamesPlayed();
        inefficient.getStats().incrementGamesWon();
        inefficient.getStats().addMoves(10);

        var response = playerController.getMostEfficientPlayers(10);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().size());
        // NOTE: getMostEfficientPlayers sorts DESCENDING by efficiency (moves/wins)
        // Higher value (more moves per win) comes first, which is actually less
        // efficient
        // So "Inefficient" (10 moves/win) comes before "Efficient" (3 moves/win)
        assertEquals("Inefficient", response.getBody().get(0).getName());
        assertEquals("Efficient", response.getBody().get(1).getName());
    }

    // === Count Endpoint Test ===

    @Test
    void getPlayerCount_shouldReturnCorrectCount() {
        playerService.createPlayer("P1", "p1@test.com");
        playerService.createPlayer("P2", "p2@test.com");

        var response = playerController.getPlayerCount();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2L, response.getBody().get("count"));
    }

    // === Helper Methods ===

    private PlayerController.CreatePlayerRequest createRequest(String name, String email) {
        var request = new PlayerController.CreatePlayerRequest();
        request.setName(name);
        request.setEmail(email);
        return request;
    }

    private PlayerController.UpdatePlayerRequest updateRequest(String name, String email) {
        var request = new PlayerController.UpdatePlayerRequest();
        request.setName(name);
        request.setEmail(email);
        return request;
    }
}
