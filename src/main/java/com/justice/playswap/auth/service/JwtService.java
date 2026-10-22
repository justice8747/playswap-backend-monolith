package com.justice.playswap.auth.service;

import com.justice.playswap.auth.entity.User;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Slf4j
@Service
public class JwtService {

    private final SecretKey hmacKey;
    private static final long TOKEN_VALIDITY_SECONDS = 7 * 24 * 3600; // 7 days

    public JwtService(@Value("${app.jwt-secret:${app.token-encryption-key:${AES_SECRET_KEY:PlaySwapSecureJwtSigningDefaultKey2026MustBeAtLeast32BytesLong!}}}") String secret) {
        this.hmacKey = deriveHmacKey(secret);
    }

    public static SecretKey deriveHmacKey(String secret) {
        try {
            // Hash the secret to guarantee a 256-bit (32-byte) key for HS256
            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            byte[] keyBytes = sha256.digest(secret.getBytes(StandardCharsets.UTF_8));
            return new SecretKeySpec(keyBytes, "HmacSHA256");
        } catch (Exception e) {
            throw new IllegalStateException("Failed to derive HMAC-SHA256 key for JWT", e);
        }
    }

    public SecretKey getHmacKey() {
        return this.hmacKey;
    }

    public Mono<String> mintToken(UUID userId, User user) {
        return Mono.fromCallable(() -> {
            Instant now = Instant.now();
            Instant expiry = now.plusSeconds(TOKEN_VALIDITY_SECONDS);

            String email = user.getEmail() != null ? user.getEmail() : "";
            if (email.isBlank() && user.getSpotifyEmail() != null) {
                email = user.getSpotifyEmail();
            }
            if (email.isBlank() && user.getYoutubeEmail() != null) {
                email = user.getYoutubeEmail();
            }

            JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                    .subject(userId.toString())
                    .claim("preferred_username", userId.toString())
                    .claim("email", email)
                    .issuer("playswap-backend")
                    .issueTime(Date.from(now))
                    .expirationTime(Date.from(expiry))
                    .build();

            SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claimsSet);
            JWSSigner signer = new MACSigner(this.hmacKey);
            signedJWT.sign(signer);

            String token = signedJWT.serialize();
            log.info("JWT successfully minted in-memory for user {}", userId);
            return token;
        });
    }
}
