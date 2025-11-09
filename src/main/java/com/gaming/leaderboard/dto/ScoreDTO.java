package com.gaming.leaderboard.dto;

import com.gaming.leaderboard.model.Score;
import com.gaming.leaderboard.model.Score.GameType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

/**
 * Score DTOs using Java 21 Records
 */
public class ScoreDTO {

    public record SubmitScoreRequest(
        @NotNull(message = "Player ID is required")
        Long playerId,

        @NotNull(message = "Score value is required")
        @Min(value = 0, message = "Score must be non-negative")
        Long scoreValue,

        @NotNull(message = "Game type is required")
        GameType gameType,

        @Min(value = 0, message = "Session duration must be non-negative")
        Integer sessionDurationSeconds,

        @Min(value = 0, message = "Level must be non-negative")
        Integer levelReached,

        String metadata
    ) {}

    public record ScoreResponse(
        Long id,
        Long playerId,
        String playerUsername,
        Long scoreValue,
        GameType gameType,
        Integer sessionDurationSeconds,
        Integer levelReached,
        String metadata,
        LocalDateTime createdAt
    ) {
        public static ScoreResponse from(Score score) {
            return new ScoreResponse(
                score.getId(),
                score.getPlayer().getId(),
                score.getPlayer().getUsername(),
                score.getScoreValue(),
                score.getGameType(),
                score.getSessionDurationSeconds(),
                score.getLevelReached(),
                score.getMetadata(),
                score.getCreatedAt()
            );
        }
    }
}
