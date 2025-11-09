package com.gaming.leaderboard.service;

import com.gaming.leaderboard.dto.LeaderboardDTO.*;
import com.gaming.leaderboard.model.Player;
import com.gaming.leaderboard.model.Score;
import com.gaming.leaderboard.model.Score.GameType;
import com.gaming.leaderboard.repository.ScoreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * Unit tests for LeaderboardService
 */
@ExtendWith(MockitoExtension.class)
class LeaderboardServiceTest {

    @Mock
    private ScoreRepository scoreRepository;

    @InjectMocks
    private LeaderboardService leaderboardService;

    private Player player1, player2, player3;
    private Score score1, score2, score3;

    @BeforeEach
    void setUp() {
        player1 = Player.builder()
            .id(1L)
            .username("player1")
            .displayName("Player One")
            .build();

        player2 = Player.builder()
            .id(2L)
            .username("player2")
            .displayName("Player Two")
            .build();

        player3 = Player.builder()
            .id(3L)
            .username("player3")
            .displayName("Player Three")
            .build();

        score1 = Score.builder()
            .id(1L)
            .player(player1)
            .scoreValue(1000L)
            .gameType(GameType.RACING)
            .build();

        score2 = Score.builder()
            .id(2L)
            .player(player2)
            .scoreValue(800L)
            .gameType(GameType.RACING)
            .build();

        score3 = Score.builder()
            .id(3L)
            .player(player3)
            .scoreValue(600L)
            .gameType(GameType.RACING)
            .build();
    }

    @Test
    void getLeaderboard_ReturnsCorrectRankings() {
        List<Score> topScores = Arrays.asList(score1, score2, score3);

        when(scoreRepository.findTopScoresByGameType(
            eq(GameType.RACING),
            any(PageRequest.class)
        )).thenReturn(topScores);

        when(scoreRepository.countDistinctPlayersByGameType(GameType.RACING))
            .thenReturn(3L);

        LeaderboardResponse response = leaderboardService.getLeaderboard(GameType.RACING, 0, 10);

        assertNotNull(response);
        assertEquals(3, response.entries().size());
        assertEquals(GameType.RACING, response.gameType());

        // Verify rankings
        assertEquals(1, response.entries().get(0).rank());
        assertEquals(1000L, response.entries().get(0).score());
        assertEquals("player1", response.entries().get(0).username());

        assertEquals(2, response.entries().get(1).rank());
        assertEquals(800L, response.entries().get(1).score());

        assertEquals(3, response.entries().get(2).rank());
        assertEquals(600L, response.entries().get(2).score());
    }

    @Test
    void getPlayerRank_CalculatesCorrectly() {
        when(scoreRepository.findTopScoreByPlayerAndGameType(1L, GameType.RACING))
            .thenReturn(Optional.of(score1));

        when(scoreRepository.countScoresAbove(GameType.RACING, 1000L))
            .thenReturn(0L); // No scores above 1000

        when(scoreRepository.countDistinctPlayersByGameType(GameType.RACING))
            .thenReturn(3L);

        PlayerRankResponse response = leaderboardService.getPlayerRank(1L, GameType.RACING);

        assertNotNull(response);
        assertEquals(1L, response.playerId());
        assertEquals(1, response.rank());
        assertEquals(1000L, response.score());
        assertEquals(3, response.totalPlayers());
        assertEquals(100.0, response.percentile());
    }

    @Test
    void getPlayerRank_MiddleRank() {
        when(scoreRepository.findTopScoreByPlayerAndGameType(2L, GameType.RACING))
            .thenReturn(Optional.of(score2));

        when(scoreRepository.countScoresAbove(GameType.RACING, 800L))
            .thenReturn(1L); // One score above 800

        when(scoreRepository.countDistinctPlayersByGameType(GameType.RACING))
            .thenReturn(3L);

        PlayerRankResponse response = leaderboardService.getPlayerRank(2L, GameType.RACING);

        assertEquals(2, response.rank());
        assertEquals(66.67, response.percentile()); // (3-2+1)/3 * 100
    }
}
