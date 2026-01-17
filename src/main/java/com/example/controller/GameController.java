package com.example.controller;

import com.example.model.Game;
import com.example.service.GameService;
import com.example.service.GameService.GameNotFoundException;
import com.example.service.GameService.GameStatsResponse;
import com.example.service.GameService.InvalidGameStateException;
import com.example.service.GameService.InvalidMoveException;
import com.example.service.GameService.PlayerNotFoundException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/games")
public class GameController {

    private final GameService gameService;

    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    @PostMapping
    public ResponseEntity<Game> createGame(@RequestBody CreateGameRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(gameService.createGame(request.name()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Game> getGame(@PathVariable String id) {
        return gameService.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<Game>> listGames() {
        return ResponseEntity.ok(gameService.findAll());
    }

    @GetMapping("/waiting")
    public ResponseEntity<List<Game>> getWaitingGames() {
        return ResponseEntity.ok(gameService.findWaitingGames());
    }

    @GetMapping("/stats")
    public ResponseEntity<GameStatsResponse> getGameStats() {
        return ResponseEntity.ok(gameService.getGameStats());
    }

    @GetMapping("/{id}/status")
    public ResponseEntity<Game> getGameStatus(@PathVariable String id) {
        return gameService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/join")
    public ResponseEntity<?> joinGame(@PathVariable String id, @Valid @RequestBody JoinGameRequest request) {
        try {
            return ResponseEntity.ok(gameService.joinGame(id, request.playerId()));
        } catch (GameNotFoundException | PlayerNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (InvalidGameStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{id}/moves")
    public ResponseEntity<?> makeMove(@PathVariable String id, @Valid @RequestBody MakeMoveRequest request) {
        try {
            return ResponseEntity.ok(gameService.makeMove(id, request.playerId(), request.row(), request.col()));
        } catch (GameNotFoundException | PlayerNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (InvalidGameStateException | InvalidMoveException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGame(@PathVariable String id) {
        return gameService.deleteGame(id) ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    record CreateGameRequest(String name) {
    }

    record JoinGameRequest(@NotBlank String playerId) {
    }

    record MakeMoveRequest(@NotBlank String playerId, @Min(0) @Max(2) int row, @Min(0) @Max(2) int col) {
    }
}
