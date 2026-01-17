package com.example.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "games")
@Getter
@Setter
public class Game {

    @Id
    private String id;

    private String name;

    @Enumerated(EnumType.STRING)
    private GameStatus status = GameStatus.WAITING;

    @ElementCollection
    @CollectionTable(name = "game_board", joinColumns = @JoinColumn(name = "game_id"))
    private List<String> board = new ArrayList<>();

    @ManyToMany
    @JoinTable(name = "game_players", joinColumns = @JoinColumn(name = "game_id"), inverseJoinColumns = @JoinColumn(name = "player_id"))
    private List<Player> players = new ArrayList<>();

    @ManyToOne
    private Player currentPlayer;

    @ManyToOne
    private Player winner;

    private int moveCount = 0;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    public enum GameStatus {
        WAITING, ACTIVE, COMPLETED, DRAW
    }

    public Game() {
        this.id = UUID.randomUUID().toString();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        for (int i = 0; i < 9; i++)
            board.add(null);
    }

    public Game(String name) {
        this();
        this.name = name;
    }

    public boolean addPlayer(Player player) {
        if (players.size() >= 2 || players.contains(player))
            return false;
        players.add(player);
        if (players.size() == 2) {
            status = GameStatus.ACTIVE;
            currentPlayer = players.get(0);
        }
        return true;
    }

    public boolean makeMove(Player player, int position) {
        if (status != GameStatus.ACTIVE || !player.equals(currentPlayer))
            return false;
        if (position < 0 || position >= 9 || board.get(position) != null)
            return false;

        var symbol = players.indexOf(player) == 0 ? "X" : "O";
        board.set(position, symbol);
        moveCount++;
        player.getStats().addMoves(1);

        if (checkWin(symbol)) {
            status = GameStatus.COMPLETED;
            winner = player;
            player.getStats().incrementGamesWon();
            player.getStats().incrementGamesPlayed();
            players.stream().filter(p -> !p.equals(player)).findFirst()
                    .ifPresent(p -> {
                        p.getStats().incrementGamesLost();
                        p.getStats().incrementGamesPlayed();
                    });
        } else if (board.stream().allMatch(c -> c != null)) {
            status = GameStatus.DRAW;
            players.forEach(p -> {
                p.getStats().incrementGamesDrawn();
                p.getStats().incrementGamesPlayed();
            });
        } else {
            currentPlayer = players.get((players.indexOf(currentPlayer) + 1) % 2);
        }
        updatedAt = LocalDateTime.now();
        return true;
    }

    private boolean checkWin(String s) {
        // Rows
        for (int i = 0; i < 9; i += 3)
            if (s.equals(board.get(i)) && s.equals(board.get(i + 1)) && s.equals(board.get(i + 2)))
                return true;
        // Columns
        for (int i = 0; i < 3; i++)
            if (s.equals(board.get(i)) && s.equals(board.get(i + 3)) && s.equals(board.get(i + 6)))
                return true;
        // Diagonals
        if (s.equals(board.get(0)) && s.equals(board.get(4)) && s.equals(board.get(8)))
            return true;
        if (s.equals(board.get(2)) && s.equals(board.get(4)) && s.equals(board.get(6)))
            return true;
        return false;
    }
}
