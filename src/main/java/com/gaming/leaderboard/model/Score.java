package com.gaming.leaderboard.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Score entity representing a player's game score
 */
@Entity
@Table(name = "scores", indexes = {
    @Index(name = "idx_player_game", columnList = "player_id,game_type"),
    @Index(name = "idx_game_score", columnList = "game_type,score_value"),
    @Index(name = "idx_created_at", columnList = "created_at")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Score {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;

    @Column(name = "score_value", nullable = false)
    private Long scoreValue;

    @Enumerated(EnumType.STRING)
    @Column(name = "game_type", nullable = false, length = 50)
    private GameType gameType;

    @Column(name = "session_duration_seconds")
    private Integer sessionDurationSeconds;

    @Column(name = "level_reached")
    private Integer levelReached;

    @Column(length = 500)
    private String metadata;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public enum GameType {
        RACING,
        PUZZLE,
        SHOOTER,
        STRATEGY,
        ADVENTURE,
        SPORTS,
        GENERAL
    }
}
