package com.justice.playswap.auth;

import com.justice.playswap.auth.entity.TokenEncryptionConverter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TokenEncryptionTest {

    private TokenEncryptionConverter converter;

    @BeforeEach
    void setUp() {
        converter = new TokenEncryptionConverter("CustomSecurePlaySwapEncryptionKey2026!");
    }

    @Test
    void testFailFastOnBlankOrNullKey() {
        assertThrows(IllegalStateException.class, () -> new TokenEncryptionConverter(null));
        assertThrows(IllegalStateException.class, () -> new TokenEncryptionConverter(""));
        assertThrows(IllegalStateException.class, () -> new TokenEncryptionConverter("   "));
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

    @Test
    void testRandomIvProducesDifferentCiphertext() {
        String token = "sample-token-for-iv-randomness-test";
        String encrypted1 = converter.convertToDatabaseColumn(token);
        String encrypted2 = converter.convertToDatabaseColumn(token);

        assertNotNull(encrypted1);
        assertNotNull(encrypted2);
        assertNotEquals(encrypted1, encrypted2);
        assertEquals(token, converter.convertToEntityAttribute(encrypted1));
        assertEquals(token, converter.convertToEntityAttribute(encrypted2));
    }

    @Test
    void testLegacyEcbDecryptionFallback() throws Exception {
        String originalToken = "legacy-ecb-token-value-123456789";
        byte[] keyBytes = "CustomSecurePlaySwapEncryptionKey2026!".getBytes(java.nio.charset.StandardCharsets.UTF_8);
        java.security.MessageDigest sha = java.security.MessageDigest.getInstance("SHA-256");
        javax.crypto.spec.SecretKeySpec spec = new javax.crypto.spec.SecretKeySpec(sha.digest(keyBytes), "AES");
        javax.crypto.Cipher cipher = javax.crypto.Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(javax.crypto.Cipher.ENCRYPT_MODE, spec);
        String ecbCiphertext = java.util.Base64.getEncoder().encodeToString(cipher.doFinal(originalToken.getBytes(java.nio.charset.StandardCharsets.UTF_8)));

        String decrypted = converter.convertToEntityAttribute(ecbCiphertext);
        assertEquals(originalToken, decrypted);
    }
}
