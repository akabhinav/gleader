package com.gaming.leaderboard.service;

import com.gaming.leaderboard.dto.LeaderboardDTO.*;
import com.gaming.leaderboard.exception.ResourceNotFoundException;
import com.gaming.leaderboard.model.LeaderboardEntry;
import com.gaming.leaderboard.model.Score;
import com.gaming.leaderboard.model.Score.GameType;
import com.gaming.leaderboard.repository.ScoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Core leaderboard service with ranking algorithms
 * Supports multiple leaderboard types and time-based rankings
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class LeaderboardService {

    private final ScoreRepository scoreRepository;

    /**
     * Get global leaderboard for a specific game type
     * Uses caching for improved performance
     */
    @Cacheable(value = "leaderboard", key = "#gameType + '_' + #page + '_' + #size")
    public LeaderboardResponse getLeaderboard(GameType gameType, int page, int size) {
        log.info("Fetching leaderboard for {} - page: {}, size: {}", gameType, page, size);

        PageRequest pageRequest = PageRequest.of(page, size);
        List<Score> topScores = scoreRepository.findTopScoresByGameType(gameType, pageRequest);

        List<LeaderboardEntryDTO> entries = buildLeaderboardEntries(topScores, page, size);
        Long totalEntries = scoreRepository.countDistinctPlayersByGameType(gameType);

        return new LeaderboardResponse(
            entries,
            gameType,
            totalEntries.intValue(),
            page,
            size,
            LocalDateTime.now()
        );
    }

    /**
     * Get time-based leaderboard (e.g., daily, weekly, monthly)
     */
    @Cacheable(value = "leaderboard", key = "#gameType + '_' + #timePeriod + '_' + #page + '_' + #size")
    public LeaderboardResponse getLeaderboardByTimePeriod(
        GameType gameType,
        TimePeriod timePeriod,
        int page,
        int size
    ) {
        log.info("Fetching {} leaderboard for {}", timePeriod, gameType);

        LocalDateTime since = calculateTimePeriodStart(timePeriod);
        PageRequest pageRequest = PageRequest.of(page, size);

        List<Score> topScores = scoreRepository.findTopScoresByGameTypeSince(
            gameType,
            since,
            pageRequest
        );

        List<LeaderboardEntryDTO> entries = buildLeaderboardEntries(topScores, page, size);

        return new LeaderboardResponse(
            entries,
            gameType,
            entries.size(),
            page,
            size,
            LocalDateTime.now()
        );
    }

    /**
     * Get player's rank in the leaderboard
     */
    @Cacheable(value = "playerRank", key = "#playerId + '_' + #gameType")
    public PlayerRankResponse getPlayerRank(Long playerId, GameType gameType) {
        log.info("Calculating rank for player {} in {}", playerId, gameType);

        Score playerBestScore = scoreRepository.findTopScoreByPlayerAndGameType(playerId, gameType)
            .orElseThrow(() -> new ResourceNotFoundException(
                "No scores found for player " + playerId + " in " + gameType
            ));

        Long scoresAbove = scoreRepository.countScoresAbove(
            gameType,
            playerBestScore.getScoreValue()
        );

        int rank = scoresAbove.intValue() + 1;
        Long totalPlayers = scoreRepository.countDistinctPlayersByGameType(gameType);

        double percentile = totalPlayers > 0
            ? ((double) (totalPlayers - rank + 1) / totalPlayers) * 100.0
            : 0.0;

        return new PlayerRankResponse(
            playerId,
            playerBestScore.getPlayer().getUsername(),
            playerBestScore.getScoreValue(),
            rank,
            totalPlayers.intValue(),
            Math.round(percentile * 100.0) / 100.0
        );
    }

    /**
     * Get players around a specific player (context leaderboard)
     */
    public LeaderboardResponse getLeaderboardAroundPlayer(
        Long playerId,
        GameType gameType,
        int contextSize
    ) {
        log.info("Fetching leaderboard around player {} in {}", playerId, gameType);

        PlayerRankResponse playerRank = getPlayerRank(playerId, gameType);

        // Calculate page to fetch player's context
        int rank = playerRank.rank();
        int startRank = Math.max(1, rank - contextSize);
        int page = (startRank - 1) / (contextSize * 2);

        return getLeaderboard(gameType, page, contextSize * 2);
    }

    /**
     * Build leaderboard entries with calculated ranks
     */
    private List<LeaderboardEntryDTO> buildLeaderboardEntries(
        List<Score> scores,
        int page,
        int pageSize
    ) {
        List<LeaderboardEntryDTO> entries = new ArrayList<>();
        AtomicInteger currentRank = new AtomicInteger(page * pageSize + 1);

        for (Score score : scores) {
            LeaderboardEntry entry = LeaderboardEntry.from(
                score.getPlayer(),
                score,
                currentRank.getAndIncrement()
            );
            entries.add(LeaderboardEntryDTO.from(entry));
        }

        return entries;
    }

    /**
     * Calculate start time for time period
     */
    private LocalDateTime calculateTimePeriodStart(TimePeriod period) {
        LocalDateTime now = LocalDateTime.now();
        return switch (period) {
            case DAILY -> now.minusDays(1);
            case WEEKLY -> now.minusWeeks(1);
            case MONTHLY -> now.minusMonths(1);
            case YEARLY -> now.minusYears(1);
            case ALL_TIME -> LocalDateTime.MIN;
        };
    }

    /**
     * Time period enum for time-based leaderboards
     * Using Java 21 enhanced switch expressions
     */
    public enum TimePeriod {
        DAILY,
        WEEKLY,
        MONTHLY,
        YEARLY,
        ALL_TIME
    }
}
