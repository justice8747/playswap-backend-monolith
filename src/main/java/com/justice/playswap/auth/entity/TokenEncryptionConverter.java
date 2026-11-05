package com.justice.playswap.auth.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

@Slf4j
@Converter
@Component
public class TokenEncryptionConverter implements AttributeConverter<String, String> {

    private static final String GCM_TRANSFORMATION = "AES/GCM/NoPadding";
    private static final String LEGACY_ECB_TRANSFORMATION = "AES/ECB/PKCS5Padding";
    private static final int GCM_IV_LENGTH_BYTE = 12; // 96 bits standard NIST recommendation
    private static final int GCM_TAG_LENGTH_BIT = 128; // 128-bit authentication tag
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final SecretKeySpec secretKeySpec;

    public TokenEncryptionConverter(@Value("${app.token-encryption-key}") String rawKey) {
        if (rawKey == null || rawKey.isBlank()) {
            throw new IllegalStateException("FATAL: 'app.token-encryption-key' must be configured in environment or application properties.");
        }
        try {
            byte[] keyBytes = rawKey.getBytes(StandardCharsets.UTF_8);
            MessageDigest sha = MessageDigest.getInstance("SHA-256");
            keyBytes = sha.digest(keyBytes);
            this.secretKeySpec = new SecretKeySpec(keyBytes, "AES");
        } catch (Exception e) {
            throw new IllegalStateException("Error initializing AES encryption key", e);
        }
    }

    @Override
    public String convertToDatabaseColumn(String attribute) {
        if (attribute == null) {
            return null;
        }
        try {
            byte[] iv = new byte[GCM_IV_LENGTH_BYTE];
            SECURE_RANDOM.nextBytes(iv);

            Cipher cipher = Cipher.getInstance(GCM_TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, new GCMParameterSpec(GCM_TAG_LENGTH_BIT, iv));

            byte[] cipherText = cipher.doFinal(attribute.getBytes(StandardCharsets.UTF_8));

            ByteBuffer byteBuffer = ByteBuffer.allocate(iv.length + cipherText.length);
            byteBuffer.put(iv);
            byteBuffer.put(cipherText);

            return Base64.getEncoder().encodeToString(byteBuffer.array());
        } catch (Exception e) {
            log.error("Failed to encrypt OAuth token for storage", e);
            throw new RuntimeException("Encryption error", e);
        }
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return dbData;
        }
        try {
            byte[] decoded = Base64.getDecoder().decode(dbData);

            // Attempt AES/GCM decryption first
            if (decoded.length >= GCM_IV_LENGTH_BYTE + 16) {
                try {
                    Cipher cipher = Cipher.getInstance(GCM_TRANSFORMATION);
                    GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH_BIT, decoded, 0, GCM_IV_LENGTH_BYTE);
                    cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, spec);
                    byte[] plainText = cipher.doFinal(decoded, GCM_IV_LENGTH_BYTE, decoded.length - GCM_IV_LENGTH_BYTE);
                    return new String(plainText, StandardCharsets.UTF_8);
                } catch (Exception gcmEx) {
                    log.debug("AES/GCM decryption failed; attempting legacy AES/ECB fallback: {}", gcmEx.getMessage());
                }
            }

            // Fallback for legacy AES/ECB encrypted tokens
            try {
                Cipher cipher = Cipher.getInstance(LEGACY_ECB_TRANSFORMATION);
                cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
                return new String(cipher.doFinal(decoded), StandardCharsets.UTF_8);
            } catch (Exception ecbEx) {
                // If not ECB either, treat as legacy plaintext
                log.warn("Stored OAuth token could not be decrypted via AES (GCM/ECB); treating as legacy unencrypted token");
                return dbData;
            }
        } catch (IllegalArgumentException notBase64) {
            // Not Base64-encoded: treat as plaintext
            return dbData;
        } catch (Exception e) {
            log.warn("Stored OAuth token could not be decrypted; treating as legacy unencrypted token: {}", e.getMessage());
            return dbData;
        }
    }
}
