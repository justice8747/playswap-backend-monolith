package com.justice.playswap.auth;

import com.justice.playswap.auth.entity.User;
import com.justice.playswap.auth.service.JwtService;
import com.nimbusds.jwt.SignedJWT;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;
    private ReactiveJwtDecoder jwtDecoder;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService("CustomTestJwtSecretKeyPlaySwap2026MustBeAtLeast32Bytes!");
        jwtDecoder = NimbusReactiveJwtDecoder.withSecretKey(jwtService.getHmacKey()).build();
    }

    @Test
    void testMintAndDecodeToken() throws Exception {
        UUID userId = UUID.randomUUID();
        User user = User.builder()
                .id(userId)
                .email("test@playswap.com")
                .spotifyId("spotify-123")
                .build();

        String token = jwtService.mintToken(userId, user).block();
        assertNotNull(token);

        SignedJWT signedJWT = SignedJWT.parse(token);
        assertEquals(userId.toString(), signedJWT.getJWTClaimsSet().getSubject());
        assertEquals(userId.toString(), signedJWT.getJWTClaimsSet().getStringClaim("preferred_username"));
        assertEquals("test@playswap.com", signedJWT.getJWTClaimsSet().getStringClaim("email"));
        assertEquals("playswap-backend", signedJWT.getJWTClaimsSet().getIssuer());

        // Verify that ReactiveJwtDecoder decodes and verifies the signature successfully
        StepVerifier.create(jwtDecoder.decode(token))
                .assertNext(decodedJwt -> {
                    assertEquals(userId.toString(), decodedJwt.getSubject());
                    assertEquals(userId.toString(), decodedJwt.getClaimAsString("preferred_username"));
                    assertEquals("test@playswap.com", decodedJwt.getClaimAsString("email"));
                })
                .verifyComplete();
    }
}
