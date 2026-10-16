package com.justice.playswap.auth;

import com.justice.playswap.auth.entity.TokenEncryptionConverter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TokenEncryptionTest {

    private TokenEncryptionConverter converter;

    @BeforeEach
    void setUp() {
        converter = new TokenEncryptionConverter();
        converter.setSecretKey("CustomSecurePlaySwapEncryptionKey2026!");
    }

    @Test
    void testEncryptAndDecrypt() {
        String originalToken = "ya29.a0AfH6SMByz_SampleOAuthAccessToken_VerySecret123456789";

        String encrypted = converter.convertToDatabaseColumn(originalToken);
        assertNotNull(encrypted);
        assertNotEquals(originalToken, encrypted);

        String decrypted = converter.convertToEntityAttribute(encrypted);
        assertEquals(originalToken, decrypted);
    }

    @Test
    void testLegacyPlainTextFallback() {
        // Legacy plaintext tokens that were stored before encryption should be returned as-is
        String legacyToken = "legacy_plaintext_token_not_encrypted";
        String result = converter.convertToEntityAttribute(legacyToken);
        assertEquals(legacyToken, result);
    }

    @Test
    void testNullHandling() {
        assertNull(converter.convertToDatabaseColumn(null));
        assertNull(converter.convertToEntityAttribute(null));
    }
}
