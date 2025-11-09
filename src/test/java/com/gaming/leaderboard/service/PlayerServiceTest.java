package com.gaming.leaderboard.service;

import com.gaming.leaderboard.dto.PlayerDTO.*;
import com.gaming.leaderboard.exception.DuplicateResourceException;
import com.gaming.leaderboard.exception.ResourceNotFoundException;
import com.gaming.leaderboard.model.Player;
import com.gaming.leaderboard.repository.PlayerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for PlayerService
 */
@ExtendWith(MockitoExtension.class)
class PlayerServiceTest {

    @Mock
    private PlayerRepository playerRepository;

    @InjectMocks
    private PlayerService playerService;

    private Player testPlayer;
    private CreatePlayerRequest createRequest;

    @BeforeEach
    void setUp() {
        testPlayer = Player.builder()
            .id(1L)
            .username("testuser")
            .email("test@example.com")
            .displayName("Test User")
            .status(Player.PlayerStatus.ACTIVE)
            .build();

        createRequest = new CreatePlayerRequest(
            "testuser",
            "test@example.com",
            "Test User",
            "http://avatar.url"
        );
    }

    @Test
    void createPlayer_Success() {
        when(playerRepository.existsByUsername(anyString())).thenReturn(false);
        when(playerRepository.existsByEmail(anyString())).thenReturn(false);
        when(playerRepository.save(any(Player.class))).thenReturn(testPlayer);

        PlayerResponse response = playerService.createPlayer(createRequest);

        assertNotNull(response);
        assertEquals("testuser", response.username());
        verify(playerRepository).save(any(Player.class));
    }

    @Test
    void createPlayer_DuplicateUsername_ThrowsException() {
        when(playerRepository.existsByUsername("testuser")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () ->
            playerService.createPlayer(createRequest)
        );

        verify(playerRepository, never()).save(any(Player.class));
    }

    @Test
    void getPlayer_Found() {
        when(playerRepository.findByIdWithScores(1L)).thenReturn(Optional.of(testPlayer));

        PlayerResponse response = playerService.getPlayer(1L);

        assertNotNull(response);
        assertEquals(1L, response.id());
        assertEquals("testuser", response.username());
    }

    @Test
    void getPlayer_NotFound_ThrowsException() {
        when(playerRepository.findByIdWithScores(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
            playerService.getPlayer(999L)
        );
    }

    @Test
    void updatePlayer_Success() {
        UpdatePlayerRequest updateRequest = new UpdatePlayerRequest(
            "Updated Name",
            "http://newavatar.url",
            Player.PlayerStatus.INACTIVE
        );

        when(playerRepository.findById(1L)).thenReturn(Optional.of(testPlayer));
        when(playerRepository.save(any(Player.class))).thenReturn(testPlayer);

        PlayerResponse response = playerService.updatePlayer(1L, updateRequest);

        assertNotNull(response);
        verify(playerRepository).save(any(Player.class));
    }

    @Test
    void deletePlayer_Success() {
        when(playerRepository.existsById(1L)).thenReturn(true);

        playerService.deletePlayer(1L);

        verify(playerRepository).deleteById(1L);
    }
}
