package com.justice.playswap.auth;

import com.justice.playswap.auth.controller.MeController;
import com.justice.playswap.auth.entity.User;
import com.justice.playswap.auth.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MeControllerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private Jwt jwt;

    @InjectMocks
    private MeController meController;

    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
    }

    @Test
    void testGetSpotifyProfile_Connected() {
        when(jwt.getClaimAsString("preferred_username")).thenReturn(null);
        when(jwt.getSubject()).thenReturn(userId.toString());

        User user = User.builder()
                .id(userId)
                .spotifyId("spotify-user-123")
                .spotifyEmail("spotify@example.com")
                .email("user@example.com")
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        Mono<ResponseEntity<Map<String, Object>>> responseMono = meController.getSpotifyProfile(jwt);
        ResponseEntity<Map<String, Object>> response = responseMono.block();

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals(true, body.get("connected"));
        assertEquals("spotify-user-123", body.get("spotifyId"));
        assertEquals("spotify@example.com", body.get("email"));
    }

    @Test
    void testGetSpotifyProfile_NotConnected() {
        when(jwt.getClaimAsString("preferred_username")).thenReturn(userId.toString());

        User user = User.builder()
                .id(userId)
                .spotifyId(null)
                .email("user@example.com")
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        Mono<ResponseEntity<Map<String, Object>>> responseMono = meController.getSpotifyProfile(jwt);
        ResponseEntity<Map<String, Object>> response = responseMono.block();

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals(false, body.get("connected"));
    }

    @Test
    void testGetYoutubeProfile_Connected() {
        when(jwt.getClaimAsString("preferred_username")).thenReturn(null);
        when(jwt.getSubject()).thenReturn(userId.toString());

        User user = User.builder()
                .id(userId)
                .youtubeId("yt-channel-999")
                .youtubeEmail("yt@example.com")
                .email("user@example.com")
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        Mono<ResponseEntity<Map<String, Object>>> responseMono = meController.getYoutubeProfile(jwt);
        ResponseEntity<Map<String, Object>> response = responseMono.block();

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals(true, body.get("connected"));
        assertEquals("yt-channel-999", body.get("youtubeId"));
        assertEquals("yt@example.com", body.get("email"));
    }
}
