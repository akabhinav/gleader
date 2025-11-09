package com.gaming.leaderboard.controller;

import com.gaming.leaderboard.dto.LeaderboardDTO.*;
import com.gaming.leaderboard.model.Score.GameType;
import com.gaming.leaderboard.service.HybridLeaderboardService;
import com.gaming.leaderboard.service.LeaderboardService.TimePeriod;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST Controller for Leaderboard operations
 * Provides various leaderboard views and rankings
 * Uses hybrid service that intelligently switches between Redis and Database
 */
@RestController
@RequestMapping("/api/v1/leaderboard")
@RequiredArgsConstructor
@Slf4j
public class LeaderboardController {

    private final HybridLeaderboardService leaderboardService;

    @GetMapping("/{gameType}")
    public ResponseEntity<LeaderboardResponse> getLeaderboard(
        @PathVariable GameType gameType,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "50") int size
    ) {
        log.info("GET /api/v1/leaderboard/{} - page: {}, size: {}", gameType, page, size);

        // Validate page size
        if (size > 1000) {
            size = 1000;
        }

        LeaderboardResponse response = leaderboardService.getLeaderboard(gameType, page, size);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{gameType}/period/{timePeriod}")
    public ResponseEntity<LeaderboardResponse> getLeaderboardByTimePeriod(
        @PathVariable GameType gameType,
        @PathVariable TimePeriod timePeriod,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "50") int size
    ) {
        log.info("GET /api/v1/leaderboard/{}/period/{} - page: {}, size: {}",
            gameType, timePeriod, page, size);

        if (size > 1000) {
            size = 1000;
        }

        LeaderboardResponse response = leaderboardService.getLeaderboardByTimePeriod(
            gameType,
            timePeriod,
            page,
            size
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{gameType}/rank/player/{playerId}")
    public ResponseEntity<PlayerRankResponse> getPlayerRank(
        @PathVariable GameType gameType,
        @PathVariable Long playerId
    ) {
        log.info("GET /api/v1/leaderboard/{}/rank/player/{}", gameType, playerId);
        PlayerRankResponse response = leaderboardService.getPlayerRank(playerId, gameType);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{gameType}/around/player/{playerId}")
    public ResponseEntity<LeaderboardResponse> getLeaderboardAroundPlayer(
        @PathVariable GameType gameType,
        @PathVariable Long playerId,
        @RequestParam(defaultValue = "5") int contextSize
    ) {
        log.info("GET /api/v1/leaderboard/{}/around/player/{} - context: {}",
            gameType, playerId, contextSize);

        LeaderboardResponse response = leaderboardService.getLeaderboardAroundPlayer(
            playerId,
            gameType,
            contextSize
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus() {
        log.info("GET /api/v1/leaderboard/status - Checking leaderboard service status");
        return ResponseEntity.ok(Map.of(
            "service", "Leaderboard",
            "mode", leaderboardService.getStatus(),
            "redisAvailable", leaderboardService.isRedisAvailable()
        ));
    }
}
