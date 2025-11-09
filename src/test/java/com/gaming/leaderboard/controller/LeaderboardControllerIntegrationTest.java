package com.gaming.leaderboard.controller;

import com.gaming.leaderboard.model.Player;
import com.gaming.leaderboard.model.Score;
import com.gaming.leaderboard.model.Score.GameType;
import com.gaming.leaderboard.repository.PlayerRepository;
import com.gaming.leaderboard.repository.ScoreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for LeaderboardController
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
class LeaderboardControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private ScoreRepository scoreRepository;

    private Player player1, player2, player3;

    @BeforeEach
    void setUp() {
        scoreRepository.deleteAll();
        playerRepository.deleteAll();

        // Create test players
        player1 = playerRepository.save(Player.builder()
            .username("champion")
            .email("champion@example.com")
            .displayName("Champion Player")
            .status(Player.PlayerStatus.ACTIVE)
            .build());

        player2 = playerRepository.save(Player.builder()
            .username("runner")
            .email("runner@example.com")
            .displayName("Runner Up")
            .status(Player.PlayerStatus.ACTIVE)
            .build());

        player3 = playerRepository.save(Player.builder()
            .username("beginner")
            .email("beginner@example.com")
            .displayName("Beginner")
            .status(Player.PlayerStatus.ACTIVE)
            .build());

        // Create test scores
        scoreRepository.save(Score.builder()
            .player(player1)
            .scoreValue(10000L)
            .gameType(GameType.RACING)
            .levelReached(50)
            .build());

        scoreRepository.save(Score.builder()
            .player(player2)
            .scoreValue(7500L)
            .gameType(GameType.RACING)
            .levelReached(40)
            .build());

        scoreRepository.save(Score.builder()
            .player(player3)
            .scoreValue(5000L)
            .gameType(GameType.RACING)
            .levelReached(30)
            .build());
    }

    @Test
    void getLeaderboard_ReturnsCorrectData() throws Exception {
        mockMvc.perform(get("/api/v1/leaderboard/RACING")
                .param("page", "0")
                .param("size", "10"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.entries", hasSize(3)))
            .andExpect(jsonPath("$.gameType").value("RACING"))
            .andExpect(jsonPath("$.totalEntries").value(3))
            .andExpect(jsonPath("$.entries[0].rank").value(1))
            .andExpect(jsonPath("$.entries[0].score").value(10000))
            .andExpect(jsonPath("$.entries[0].username").value("champion"))
            .andExpect(jsonPath("$.entries[1].rank").value(2))
            .andExpect(jsonPath("$.entries[1].score").value(7500))
            .andExpect(jsonPath("$.entries[2].rank").value(3))
            .andExpect(jsonPath("$.entries[2].score").value(5000));
    }

    @Test
    void getPlayerRank_ReturnsCorrectRank() throws Exception {
        mockMvc.perform(get("/api/v1/leaderboard/RACING/rank/player/" + player1.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.playerId").value(player1.getId()))
            .andExpect(jsonPath("$.rank").value(1))
            .andExpect(jsonPath("$.score").value(10000))
            .andExpect(jsonPath("$.totalPlayers").value(3))
            .andExpect(jsonPath("$.percentile").value(100.0));
    }

    @Test
    void getLeaderboardAroundPlayer_ReturnsContext() throws Exception {
        mockMvc.perform(get("/api/v1/leaderboard/RACING/around/player/" + player2.getId())
                .param("contextSize", "2"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.entries", not(empty())))
            .andExpect(jsonPath("$.gameType").value("RACING"));
    }
}
