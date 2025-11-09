package com.gaming.leaderboard.dto;

import com.gaming.leaderboard.model.Player;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Data Transfer Objects using Java 21 Records
 */
public class PlayerDTO {

    public record CreatePlayerRequest(
        @NotBlank(message = "Username is required")
        @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
        String username,

        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        String email,

        @Size(max = 100, message = "Display name cannot exceed 100 characters")
        String displayName,

        String avatarUrl
    ) {}

    public record UpdatePlayerRequest(
        String displayName,
        String avatarUrl,
        Player.PlayerStatus status
    ) {}

    public record PlayerResponse(
        Long id,
        String username,
        String email,
        String displayName,
        String avatarUrl,
        Player.PlayerStatus status,
        Long totalScores,
        Long highestScore
    ) {
        public static PlayerResponse from(Player player) {
            return new PlayerResponse(
                player.getId(),
                player.getUsername(),
                player.getEmail(),
                player.getDisplayName(),
                player.getAvatarUrl(),
                player.getStatus(),
                (long) player.getScores().size(),
                player.getScores().stream()
                    .mapToLong(score -> score.getScoreValue())
                    .max()
                    .orElse(0L)
            );
        }
    }
}
