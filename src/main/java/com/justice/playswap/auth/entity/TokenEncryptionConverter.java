package com.justice.playswap.auth.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

@Slf4j
@Converter
@Component
public class TokenEncryptionConverter implements AttributeConverter<String, String> {

    private static String secretKey = "PlaySwapSecureTokenEncryptionDefaultKey2026!";

    @Value("${app.token-encryption-key:PlaySwapSecureTokenEncryptionDefaultKey2026!}")
    public void setSecretKey(String key) {
        if (key != null && !key.isBlank()) {
            secretKey = key;
        }
    }

    private static SecretKeySpec getKey() {
        try {
            byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
            MessageDigest sha = MessageDigest.getInstance("SHA-256");
            keyBytes = sha.digest(keyBytes);
            return new SecretKeySpec(keyBytes, "AES");
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
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, getKey());
            return Base64.getEncoder().encodeToString(cipher.doFinal(attribute.getBytes(StandardCharsets.UTF_8)));
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
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, getKey());
            return new String(cipher.doFinal(decoded), StandardCharsets.UTF_8);
        } catch (Exception e) {
            // Graceful fallback if token was stored in plain text prior to encryption
            log.warn("Stored OAuth token could not be decrypted via AES; treating as legacy unencrypted token: {}", e.getMessage());
            return dbData;
        }
    }
}
