package com.gaming.leaderboard.repository;

import com.gaming.leaderboard.model.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for Player entities
 */
@Repository
public interface PlayerRepository extends JpaRepository<Player, Long> {

    Optional<Player> findByUsername(String username);

    Optional<Player> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    @Query("SELECT COUNT(DISTINCT p) FROM Player p WHERE p.status = 'ACTIVE'")
    Long countActivePlayers();

    @Query("""
        SELECT p FROM Player p
        LEFT JOIN FETCH p.scores
        WHERE p.id = :playerId
        """)
    Optional<Player> findByIdWithScores(@Param("playerId") Long playerId);
}
