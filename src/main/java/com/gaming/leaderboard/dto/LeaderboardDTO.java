package com.gaming.leaderboard.dto;

import com.gaming.leaderboard.model.LeaderboardEntry;
import com.gaming.leaderboard.model.Score.GameType;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Leaderboard DTOs using Java 21 Records
 */
public class LeaderboardDTO {

    public record LeaderboardResponse(
        List<LeaderboardEntryDTO> entries,
        GameType gameType,
        Integer totalEntries,
        Integer page,
        Integer pageSize,
        LocalDateTime generatedAt
    ) {}

    public record LeaderboardEntryDTO(
        Long playerId,
        String username,
        String displayName,
        String avatarUrl,
        Long score,
        Integer rank,
        Integer levelReached,
        Integer sessionDuration,
        LocalDateTime achievedAt
    ) {
        public static LeaderboardEntryDTO from(LeaderboardEntry entry) {
            return new LeaderboardEntryDTO(
                entry.playerId(),
                entry.username(),
                entry.displayName(),
                entry.avatarUrl(),
                entry.score(),
                entry.rank(),
                entry.levelReached(),
                entry.sessionDuration(),
                entry.achievedAt()
            );
        }
    }

    public record PlayerRankResponse(
        Long playerId,
        String username,
        Long score,
        Integer rank,
        Integer totalPlayers,
        Double percentile
    ) {}
}
