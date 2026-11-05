package com.justice.playswap.auth;

import com.justice.playswap.auth.controller.AuthController;
import com.justice.playswap.auth.service.JwtRevocationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpCookie;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.util.ReflectionTestUtils;
import reactor.core.publisher.Mono;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private JwtRevocationService jwtRevocationService;

    @Mock
    private Jwt jwt;

    @InjectMocks
    private AuthController authController;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authController, "frontendUrl", "http://localhost:3000");
    }

    @Test
    void logout_ClearsCookieAndRevokesJwt() {
        when(jwtRevocationService.revoke(jwt)).thenReturn(Mono.empty());

        MockServerHttpRequest request = MockServerHttpRequest.post("/api/auth/logout")
                .cookie(new HttpCookie("playswap_token", "existing-jwt-token"))
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        Mono<ResponseEntity<Map<String, String>>> responseMono = authController.logout(exchange, jwt);
        ResponseEntity<Map<String, String>> response = responseMono.block();

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertEquals("Logged out successfully", response.getBody().get("message"));

        // Verify cookie is cleared with maxAge 0
        ResponseCookie clearedCookie = exchange.getResponse().getCookies().getFirst("playswap_token");
        assertNotNull(clearedCookie);
        assertEquals("", clearedCookie.getValue());
        assertEquals(0, clearedCookie.getMaxAge().getSeconds());

        verify(jwtRevocationService).revoke(jwt);
    }

    @Test
    void testLogout_Unauthenticated() {
        doReturn(Mono.empty()).when(jwtRevocationService).revoke(isNull());
        MockServerHttpRequest request = MockServerHttpRequest.post("/api/auth/logout").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        Mono<ResponseEntity<Map<String, String>>> responseMono = authController.logout(exchange, null);
        ResponseEntity<Map<String, String>> response = responseMono.block();

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        ResponseCookie clearedCookie = exchange.getResponse().getCookies().getFirst("playswap_token");
        assertNotNull(clearedCookie);
        assertEquals(0, clearedCookie.getMaxAge().getSeconds());
        verify(jwtRevocationService).revoke(null);
    }
}
