package com.gaming.leaderboard.repository;

import com.gaming.leaderboard.model.Score;
import com.gaming.leaderboard.model.Score.GameType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for Score entities with optimized queries for leaderboard
 */
@Repository
public interface ScoreRepository extends JpaRepository<Score, Long> {

    List<Score> findByPlayerIdOrderByScoreValueDesc(Long playerId);

    List<Score> findByGameTypeOrderByScoreValueDesc(GameType gameType, Pageable pageable);

    @Query("""
        SELECT s FROM Score s
        JOIN FETCH s.player p
        WHERE s.gameType = :gameType
        ORDER BY s.scoreValue DESC, s.createdAt ASC
        """)
    List<Score> findTopScoresByGameType(
        @Param("gameType") GameType gameType,
        Pageable pageable
    );

    @Query("""
        SELECT s FROM Score s
        JOIN FETCH s.player p
        WHERE s.gameType = :gameType
        AND s.createdAt >= :since
        ORDER BY s.scoreValue DESC, s.createdAt ASC
        """)
    List<Score> findTopScoresByGameTypeSince(
        @Param("gameType") GameType gameType,
        @Param("since") LocalDateTime since,
        Pageable pageable
    );

    @Query("""
        SELECT MAX(s.scoreValue) FROM Score s
        WHERE s.player.id = :playerId
        AND s.gameType = :gameType
        """)
    Optional<Long> findMaxScoreByPlayerAndGameType(
        @Param("playerId") Long playerId,
        @Param("gameType") GameType gameType
    );

    @Query("""
        SELECT COUNT(DISTINCT s.scoreValue) FROM Score s
        WHERE s.gameType = :gameType
        AND s.scoreValue > :score
        """)
    Long countScoresAbove(
        @Param("gameType") GameType gameType,
        @Param("score") Long score
    );

    @Query("""
        SELECT COUNT(DISTINCT p.id) FROM Score s
        JOIN s.player p
        WHERE s.gameType = :gameType
        """)
    Long countDistinctPlayersByGameType(@Param("gameType") GameType gameType);

    @Query("""
        SELECT s FROM Score s
        JOIN FETCH s.player p
        WHERE s.player.id = :playerId
        AND s.gameType = :gameType
        ORDER BY s.scoreValue DESC
        LIMIT 1
        """)
    Optional<Score> findTopScoreByPlayerAndGameType(
        @Param("playerId") Long playerId,
        @Param("gameType") GameType gameType
    );
}
