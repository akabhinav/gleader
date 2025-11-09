package com.gaming.leaderboard.service;

import com.gaming.leaderboard.dto.PlayerDTO.*;
import com.gaming.leaderboard.exception.DuplicateResourceException;
import com.gaming.leaderboard.exception.ResourceNotFoundException;
import com.gaming.leaderboard.model.Player;
import com.gaming.leaderboard.repository.PlayerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing players
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class PlayerService {

    private final PlayerRepository playerRepository;

    @Transactional
    public PlayerResponse createPlayer(CreatePlayerRequest request) {
        log.info("Creating player with username: {}", request.username());

        // Check for duplicates
        if (playerRepository.existsByUsername(request.username())) {
            throw new DuplicateResourceException("Username already exists: " + request.username());
        }
        if (playerRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("Email already exists: " + request.email());
        }

        Player player = Player.builder()
            .username(request.username())
            .email(request.email())
            .displayName(request.displayName() != null ? request.displayName() : request.username())
            .avatarUrl(request.avatarUrl())
            .status(Player.PlayerStatus.ACTIVE)
            .build();

        Player savedPlayer = playerRepository.save(player);
        log.info("Player created successfully with ID: {}", savedPlayer.getId());

        return PlayerResponse.from(savedPlayer);
    }

    @Cacheable(value = "players", key = "#id")
    public PlayerResponse getPlayer(Long id) {
        log.debug("Fetching player with ID: {}", id);
        Player player = playerRepository.findByIdWithScores(id)
            .orElseThrow(() -> new ResourceNotFoundException("Player not found with ID: " + id));
        return PlayerResponse.from(player);
    }

    public PlayerResponse getPlayerByUsername(String username) {
        log.debug("Fetching player with username: {}", username);
        Player player = playerRepository.findByUsername(username)
            .orElseThrow(() -> new ResourceNotFoundException("Player not found with username: " + username));
        return PlayerResponse.from(player);
    }

    @Transactional
    @CacheEvict(value = "players", key = "#id")
    public PlayerResponse updatePlayer(Long id, UpdatePlayerRequest request) {
        log.info("Updating player with ID: {}", id);

        Player player = playerRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Player not found with ID: " + id));

        if (request.displayName() != null) {
            player.setDisplayName(request.displayName());
        }
        if (request.avatarUrl() != null) {
            player.setAvatarUrl(request.avatarUrl());
        }
        if (request.status() != null) {
            player.setStatus(request.status());
        }

        Player updatedPlayer = playerRepository.save(player);
        log.info("Player updated successfully: {}", id);

        return PlayerResponse.from(updatedPlayer);
    }

    @Transactional
    @CacheEvict(value = "players", key = "#id")
    public void deletePlayer(Long id) {
        log.info("Deleting player with ID: {}", id);
        if (!playerRepository.existsById(id)) {
            throw new ResourceNotFoundException("Player not found with ID: " + id);
        }
        playerRepository.deleteById(id);
        log.info("Player deleted successfully: {}", id);
    }

    public List<PlayerResponse> getAllPlayers() {
        log.debug("Fetching all players");
        return playerRepository.findAll().stream()
            .map(PlayerResponse::from)
            .collect(Collectors.toList());
    }

    public Long getActivePlayersCount() {
        return playerRepository.countActivePlayers();
    }
}
