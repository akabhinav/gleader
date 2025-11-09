package com.gaming.leaderboard.service;

import com.gaming.leaderboard.dto.ScoreDTO.*;
import com.gaming.leaderboard.exception.ResourceNotFoundException;
import com.gaming.leaderboard.model.Player;
import com.gaming.leaderboard.model.Score;
import com.gaming.leaderboard.model.Score.GameType;
import com.gaming.leaderboard.repository.PlayerRepository;
import com.gaming.leaderboard.repository.ScoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing scores
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ScoreService {

    private final ScoreRepository scoreRepository;
    private final PlayerRepository playerRepository;

    @Transactional
    @CacheEvict(value = {"leaderboard", "playerRank"}, allEntries = true)
    public ScoreResponse submitScore(SubmitScoreRequest request) {
        log.info("Submitting score for player {}: {} points in {}",
            request.playerId(), request.scoreValue(), request.gameType());

        Player player = playerRepository.findById(request.playerId())
            .orElseThrow(() -> new ResourceNotFoundException("Player not found with ID: " + request.playerId()));

        if (player.getStatus() != Player.PlayerStatus.ACTIVE) {
            throw new IllegalStateException("Cannot submit score for inactive player");
        }

        Score score = Score.builder()
            .player(player)
            .scoreValue(request.scoreValue())
            .gameType(request.gameType())
            .sessionDurationSeconds(request.sessionDurationSeconds())
            .levelReached(request.levelReached())
            .metadata(request.metadata())
            .build();

        Score savedScore = scoreRepository.save(score);
        log.info("Score submitted successfully with ID: {}", savedScore.getId());

        return ScoreResponse.from(savedScore);
    }

    public ScoreResponse getScore(Long id) {
        log.debug("Fetching score with ID: {}", id);
        Score score = scoreRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Score not found with ID: " + id));
        return ScoreResponse.from(score);
    }

    public List<ScoreResponse> getPlayerScores(Long playerId) {
        log.debug("Fetching scores for player: {}", playerId);
        return scoreRepository.findByPlayerIdOrderByScoreValueDesc(playerId).stream()
            .map(ScoreResponse::from)
            .collect(Collectors.toList());
    }

    public Long getPlayerBestScore(Long playerId, GameType gameType) {
        return scoreRepository.findMaxScoreByPlayerAndGameType(playerId, gameType)
            .orElse(0L);
    }

    @Transactional
    @CacheEvict(value = {"leaderboard", "playerRank"}, allEntries = true)
    public void deleteScore(Long id) {
        log.info("Deleting score with ID: {}", id);
        if (!scoreRepository.existsById(id)) {
            throw new ResourceNotFoundException("Score not found with ID: " + id);
        }
        scoreRepository.deleteById(id);
        log.info("Score deleted successfully: {}", id);
    }
}
