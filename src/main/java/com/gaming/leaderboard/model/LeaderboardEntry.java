package com.gaming.leaderboard.model;

import com.gaming.leaderboard.model.Score.GameType;

import java.time.LocalDateTime;

/**
 * LeaderboardEntry record - Java 21 Record for immutable leaderboard data
 * Used for transferring leaderboard data without exposing entities
 */
public record LeaderboardEntry(
    Long playerId,
    String username,
    String displayName,
    String avatarUrl,
    Long score,
    Integer rank,
    GameType gameType,
    LocalDateTime achievedAt,
    Integer levelReached,
    Integer sessionDuration
) {

    /**
     * Compact constructor with validation
     */
    public LeaderboardEntry {
        if (rank != null && rank < 1) {
            throw new IllegalArgumentException("Rank must be positive");
        }
        if (score != null && score < 0) {
            throw new IllegalArgumentException("Score cannot be negative");
        }
    }

    /**
     * Factory method to create from Player and Score
     */
    public static LeaderboardEntry from(Player player, Score score, Integer rank) {
        return new LeaderboardEntry(
            player.getId(),
            player.getUsername(),
            player.getDisplayName(),
            player.getAvatarUrl(),
            score.getScoreValue(),
            rank,
            score.getGameType(),
            score.getCreatedAt(),
            score.getLevelReached(),
            score.getSessionDurationSeconds()
        );
    }
}
