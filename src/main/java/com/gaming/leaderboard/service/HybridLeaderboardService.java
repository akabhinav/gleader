package com.gaming.leaderboard.service;

import com.gaming.leaderboard.dto.LeaderboardDTO.*;
import com.gaming.leaderboard.model.Score.GameType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Hybrid Leaderboard Service
 *
 * Intelligently switches between Redis (fast) and Database (fallback)
 * - When Redis is enabled: Uses Redis for O(log n) performance
 * - When Redis is disabled: Falls back to database
 * - When Redis fails: Gracefully degrades to database
 *
 * This allows the system to work locally without Redis,
 * but gain massive performance improvements when Redis is available.
 */
@Service
@Slf4j
@Transactional(readOnly = true)
public class HybridLeaderboardService {

    @Value("${redis.enabled:false}")
    private boolean redisEnabled;

    private final LeaderboardService databaseService;
    private RedisLeaderboardService redisService;

    public HybridLeaderboardService(LeaderboardService databaseService) {
        this.databaseService = databaseService;
    }

    @Autowired(required = false)
    public void setRedisService(RedisLeaderboardService redisService) {
        this.redisService = redisService;
        log.info("Redis leaderboard service enabled");
    }

    /**
     * Get leaderboard - uses Redis if available, falls back to database
     */
    public LeaderboardResponse getLeaderboard(GameType gameType, int page, int size) {
        if (redisEnabled && redisService != null) {
            try {
                log.debug("Using Redis for leaderboard query: {}", gameType);
                return redisService.getTopPlayers(gameType, page, size);
            } catch (Exception e) {
                log.warn("Redis failed, falling back to database: {}", e.getMessage());
                return databaseService.getLeaderboard(gameType, page, size);
            }
        }

        log.debug("Using database for leaderboard query: {}", gameType);
        return databaseService.getLeaderboard(gameType, page, size);
    }

    /**
     * Get leaderboard by time period
     */
    public LeaderboardResponse getLeaderboardByTimePeriod(
        GameType gameType,
        LeaderboardService.TimePeriod timePeriod,
        int page,
        int size
    ) {
        // Time-based leaderboards always use database (more accurate with timestamps)
        log.debug("Using database for time-based leaderboard: {} - {}", gameType, timePeriod);
        return databaseService.getLeaderboardByTimePeriod(gameType, timePeriod, page, size);
    }

    /**
     * Get player rank - uses Redis if available for O(log n) performance
     */
    public PlayerRankResponse getPlayerRank(Long playerId, GameType gameType) {
        if (redisEnabled && redisService != null) {
            try {
                log.debug("Using Redis for rank query: player={}, game={}", playerId, gameType);
                PlayerRankResponse response = redisService.getPlayerRank(playerId, gameType);
                if (response != null) {
                    return response;
                }
                log.debug("Player not found in Redis, checking database");
            } catch (Exception e) {
                log.warn("Redis failed for rank query, falling back to database: {}", e.getMessage());
            }
        }

        log.debug("Using database for rank query: player={}, game={}", playerId, gameType);
        return databaseService.getPlayerRank(playerId, gameType);
    }

    /**
     * Get leaderboard around player
     */
    public LeaderboardResponse getLeaderboardAroundPlayer(
        Long playerId,
        GameType gameType,
        int contextSize
    ) {
        if (redisEnabled && redisService != null) {
            try {
                log.debug("Using Redis for context leaderboard: player={}", playerId);
                return redisService.getLeaderboardAroundPlayer(playerId, gameType, contextSize);
            } catch (Exception e) {
                log.warn("Redis failed for context leaderboard, falling back to database: {}", e.getMessage());
            }
        }

        log.debug("Using database for context leaderboard: player={}", playerId);
        return databaseService.getLeaderboardAroundPlayer(playerId, gameType, contextSize);
    }

    /**
     * Check if Redis is enabled and available
     */
    public boolean isRedisAvailable() {
        if (!redisEnabled || redisService == null) {
            return false;
        }

        try {
            // Simple health check
            redisService.getTotalPlayers(GameType.GENERAL);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Get status information
     */
    public String getStatus() {
        if (redisEnabled && redisService != null) {
            boolean available = isRedisAvailable();
            return available ? "Redis (active)" : "Database (Redis unavailable)";
        }
        return "Database only";
    }
}
