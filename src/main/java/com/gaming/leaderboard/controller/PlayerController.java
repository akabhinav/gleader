package com.gaming.leaderboard.controller;

import com.gaming.leaderboard.dto.PlayerDTO.*;
import com.gaming.leaderboard.service.PlayerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Player management
 */
@RestController
@RequestMapping("/api/v1/players")
@RequiredArgsConstructor
@Slf4j
public class PlayerController {

    private final PlayerService playerService;

    @PostMapping
    public ResponseEntity<PlayerResponse> createPlayer(
        @Valid @RequestBody CreatePlayerRequest request
    ) {
        log.info("POST /api/v1/players - Creating new player: {}", request.username());
        PlayerResponse response = playerService.createPlayer(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PlayerResponse> getPlayer(@PathVariable Long id) {
        log.info("GET /api/v1/players/{} - Fetching player", id);
        PlayerResponse response = playerService.getPlayer(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/username/{username}")
    public ResponseEntity<PlayerResponse> getPlayerByUsername(@PathVariable String username) {
        log.info("GET /api/v1/players/username/{} - Fetching player", username);
        PlayerResponse response = playerService.getPlayerByUsername(username);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<PlayerResponse>> getAllPlayers() {
        log.info("GET /api/v1/players - Fetching all players");
        List<PlayerResponse> response = playerService.getAllPlayers();
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PlayerResponse> updatePlayer(
        @PathVariable Long id,
        @Valid @RequestBody UpdatePlayerRequest request
    ) {
        log.info("PUT /api/v1/players/{} - Updating player", id);
        PlayerResponse response = playerService.updatePlayer(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePlayer(@PathVariable Long id) {
        log.info("DELETE /api/v1/players/{} - Deleting player", id);
        playerService.deletePlayer(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/count/active")
    public ResponseEntity<Long> getActivePlayersCount() {
        log.info("GET /api/v1/players/count/active - Fetching active players count");
        Long count = playerService.getActivePlayersCount();
        return ResponseEntity.ok(count);
    }
}
