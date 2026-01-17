package com.example.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GameTest {

    private Game game;
    private Player player1;
    private Player player2;

    @BeforeEach
    void setUp() {
        game = new Game("Test");
        player1 = new Player("Alice", "alice@test.com");
        player2 = new Player("Bob", "bob@test.com");
        game.addPlayer(player1);
        game.addPlayer(player2);
    }

    @Test
    void testGameStartsActive() {
        assertEquals(Game.GameStatus.ACTIVE, game.getStatus());
        assertEquals(player1, game.getCurrentPlayer());
    }

    @Test
    void testMakeMove() {
        assertTrue(game.makeMove(player1, 0));
        assertEquals("X", game.getBoard().get(0));
        assertEquals(player2, game.getCurrentPlayer());
    }

    @Test
    void testWinDetection() {
        game.makeMove(player1, 0);
        game.makeMove(player2, 3);
        game.makeMove(player1, 1);
        game.makeMove(player2, 4);
        game.makeMove(player1, 2);

        assertEquals(Game.GameStatus.COMPLETED, game.getStatus());
        assertEquals(player1, game.getWinner());
        assertEquals(1, player1.getStats().getGamesWon());
        assertEquals(1, player2.getStats().getGamesLost());
    }

    @Test
    void testDraw() {
        game.makeMove(player1, 0);
        game.makeMove(player2, 1);
        game.makeMove(player1, 2);
        game.makeMove(player2, 5);
        game.makeMove(player1, 3);
        game.makeMove(player2, 6);
        game.makeMove(player1, 4);
        game.makeMove(player2, 8);
        game.makeMove(player1, 7);

        assertEquals(Game.GameStatus.DRAW, game.getStatus());
        assertEquals(1, player1.getStats().getGamesDrawn());
    }

    @Test
    void testInvalidMove() {
        game.makeMove(player1, 0);
        assertFalse(game.makeMove(player1, 1));
        assertFalse(game.makeMove(player2, 0));
    }
}
