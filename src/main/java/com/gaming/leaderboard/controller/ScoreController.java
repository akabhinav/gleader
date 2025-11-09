package com.gaming.leaderboard.controller;

import com.gaming.leaderboard.dto.ScoreDTO.*;
import com.gaming.leaderboard.model.Score.GameType;
import com.gaming.leaderboard.service.ScoreService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Score management
 */
@RestController
@RequestMapping("/api/v1/scores")
@RequiredArgsConstructor
@Slf4j
public class ScoreController {

    private final ScoreService scoreService;

    @PostMapping
    public ResponseEntity<ScoreResponse> submitScore(
        @Valid @RequestBody SubmitScoreRequest request
    ) {
        log.info("POST /api/v1/scores - Submitting new score for player: {}", request.playerId());
        ScoreResponse response = scoreService.submitScore(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ScoreResponse> getScore(@PathVariable Long id) {
        log.info("GET /api/v1/scores/{} - Fetching score", id);
        ScoreResponse response = scoreService.getScore(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/player/{playerId}")
    public ResponseEntity<List<ScoreResponse>> getPlayerScores(@PathVariable Long playerId) {
        log.info("GET /api/v1/scores/player/{} - Fetching player scores", playerId);
        List<ScoreResponse> response = scoreService.getPlayerScores(playerId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/player/{playerId}/best")
    public ResponseEntity<Long> getPlayerBestScore(
        @PathVariable Long playerId,
        @RequestParam GameType gameType
    ) {
        log.info("GET /api/v1/scores/player/{}/best?gameType={}", playerId, gameType);
        Long bestScore = scoreService.getPlayerBestScore(playerId, gameType);
        return ResponseEntity.ok(bestScore);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteScore(@PathVariable Long id) {
        log.info("DELETE /api/v1/scores/{} - Deleting score", id);
        scoreService.deleteScore(id);
        return ResponseEntity.noContent().build();
    }
}
