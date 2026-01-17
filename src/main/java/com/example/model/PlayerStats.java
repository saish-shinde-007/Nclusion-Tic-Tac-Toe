package com.example.model;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

@Embeddable
@Getter
@Setter
public class PlayerStats {

    private int gamesPlayed = 0;
    private int gamesWon = 0;
    private int gamesLost = 0;
    private int gamesDrawn = 0;
    private int totalMoves = 0;

    public double getWinRate() {
        return gamesPlayed == 0 ? 0.0 : (double) gamesWon / gamesPlayed;
    }

    public double getEfficiency() {
        return gamesWon == 0 ? Double.MAX_VALUE : (double) totalMoves / gamesWon;
    }

    public void incrementGamesPlayed() {
        this.gamesPlayed++;
    }

    public void incrementGamesWon() {
        this.gamesWon++;
    }

    public void incrementGamesLost() {
        this.gamesLost++;
    }

    public void incrementGamesDrawn() {
        this.gamesDrawn++;
    }

    public void addMoves(int moves) {
        this.totalMoves += moves;
    }
}
