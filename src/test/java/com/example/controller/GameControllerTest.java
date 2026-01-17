package com.example.controller;

import com.example.model.Game;
import com.example.service.GameService;
import com.example.service.PlayerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for GameController.
 * Tests the controller's HTTP response mappings and business logic wiring.
 */
class GameControllerTest {

    private GameController gameController;
    private GameService gameService;
    private PlayerService playerService;

    @BeforeEach
    void setUp() {
        playerService = new PlayerService();
        gameService = new GameService(playerService);
        gameController = new GameController(gameService);
    }

    // === GET Endpoint Tests ===

    @Test
    void getGame_whenExists_shouldReturnOk() {
        var game = gameService.createGame("GetTest");
        var response = gameController.getGame(game.getId());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("GetTest", response.getBody().getName());
    }

    @Test
    void getGame_whenNotExists_shouldReturnNotFound() {
        var response = gameController.getGame("nonexistent-id");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void listGames_shouldReturnAllGames() {
        gameService.createGame("Game1");
        gameService.createGame("Game2");

        var response = gameController.listGames();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().size());
    }

    @Test
    void getWaitingGames_shouldReturnOnlyWaiting() {
        gameService.createGame("Waiting1");
        gameService.createGame("Waiting2");

        var response = gameController.getWaitingGames();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().size());
        assertTrue(response.getBody().stream()
                .allMatch(g -> g.getStatus() == Game.GameStatus.WAITING));
    }

    @Test
    void getGameStats_shouldReturnCorrectStats() {
        gameService.createGame("G1");
        gameService.createGame("G2");

        var response = gameController.getGameStats();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().totalGames());
        assertEquals(2, response.getBody().waitingGames());
    }

    @Test
    void getGameStatus_whenExists_shouldReturnOk() {
        var game = gameService.createGame("StatusGame");
        var response = gameController.getGameStatus(game.getId());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(Game.GameStatus.WAITING, response.getBody().getStatus());
    }

    @Test
    void getGameStatus_whenNotExists_shouldReturnNotFound() {
        var response = gameController.getGameStatus("nonexistent");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    // === DELETE Endpoint Tests ===

    @Test
    void deleteGame_whenExists_shouldReturnNoContent() {
        var game = gameService.createGame("DeleteMe");
        var response = gameController.deleteGame(game.getId());

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    @Test
    void deleteGame_whenNotExists_shouldReturnNotFound() {
        var response = gameController.deleteGame("nonexistent");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    // === Service Integration Tests (testing via service, controller wiring
    // verified above) ===

    @Test
    void joinGame_success_createsActiveGame() {
        var p1 = playerService.createPlayer("JoinP1", "jp1@test.com");
        var p2 = playerService.createPlayer("JoinP2", "jp2@test.com");
        var game = gameService.createGame("JoinGame");

        gameService.joinGame(game.getId(), p1.getId());
        var updated = gameService.joinGame(game.getId(), p2.getId());

        assertEquals(Game.GameStatus.ACTIVE, updated.getStatus());
        assertEquals(2, updated.getPlayers().size());
    }

    @Test
    void makeMove_success_updatesBoard() {
        var p1 = playerService.createPlayer("MoveP1", "mp1@test.com");
        var p2 = playerService.createPlayer("MoveP2", "mp2@test.com");
        var game = gameService.createGame("MoveGame");
        gameService.joinGame(game.getId(), p1.getId());
        gameService.joinGame(game.getId(), p2.getId());

        var currentPlayerId = game.getCurrentPlayer().getId();
        var updated = gameService.makeMove(game.getId(), currentPlayerId, 1, 1);

        assertEquals("X", updated.getBoard().get(4)); // Center cell
    }

    @Test
    void makeMove_wrongTurn_throws() {
        var p1 = playerService.createPlayer("TurnP1", "tp1@test.com");
        var p2 = playerService.createPlayer("TurnP2", "tp2@test.com");
        var game = gameService.createGame("TurnGame");
        gameService.joinGame(game.getId(), p1.getId());
        gameService.joinGame(game.getId(), p2.getId());

        var notCurrentPlayerId = game.getCurrentPlayer().getId().equals(p1.getId())
                ? p2.getId()
                : p1.getId();

        var exception = org.junit.jupiter.api.Assertions.assertThrows(
                GameService.InvalidMoveException.class,
                () -> gameService.makeMove(game.getId(), notCurrentPlayerId, 0, 0));
        assertEquals("Not your turn", exception.getMessage());
    }

    @Test
    void makeMove_cellOccupied_throws() {
        var p1 = playerService.createPlayer("OccP1", "occ1@test.com");
        var p2 = playerService.createPlayer("OccP2", "occ2@test.com");
        var game = gameService.createGame("OccGame");
        gameService.joinGame(game.getId(), p1.getId());
        gameService.joinGame(game.getId(), p2.getId());

        // Make first move
        var currentPlayerId = game.getCurrentPlayer().getId();
        gameService.makeMove(game.getId(), currentPlayerId, 0, 0);

        // Try same cell with other player
        var nextPlayerId = game.getCurrentPlayer().getId();
        var exception = org.junit.jupiter.api.Assertions.assertThrows(
                GameService.InvalidMoveException.class,
                () -> gameService.makeMove(game.getId(), nextPlayerId, 0, 0));
        assertEquals("Cell occupied", exception.getMessage());
    }

    @Test
    void joinGame_gameNotFound_throws() {
        var player = playerService.createPlayer("LonePlayer", "lone@test.com");

        var exception = org.junit.jupiter.api.Assertions.assertThrows(
                GameService.GameNotFoundException.class,
                () -> gameService.joinGame("nonexistent", player.getId()));
        assertEquals("Game not found", exception.getMessage());
    }

    @Test
    void joinGame_gameFull_throws() {
        var p1 = playerService.createPlayer("FullP1", "fp1@test.com");
        var p2 = playerService.createPlayer("FullP2", "fp2@test.com");
        var p3 = playerService.createPlayer("FullP3", "fp3@test.com");
        var game = gameService.createGame("FullGame");

        gameService.joinGame(game.getId(), p1.getId());
        gameService.joinGame(game.getId(), p2.getId());

        var exception = org.junit.jupiter.api.Assertions.assertThrows(
                GameService.InvalidGameStateException.class,
                () -> gameService.joinGame(game.getId(), p3.getId()));
        assertEquals("Game is full", exception.getMessage());
    }
}
