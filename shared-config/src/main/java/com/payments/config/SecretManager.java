package com.payments.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * Service for managing secrets and encryption
 */
@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "payments.security.encryption.enabled", havingValue = "true", matchIfMissing = true)
public class SecretManager {

    @Value("${payments.security.encryption.algorithm:AES}")
    private String algorithm;

    @Value("${payments.security.encryption.key}")
    private String encryptionKey;

    @Value("${payments.security.encryption.salt}")
    private String salt;

    @Value("${payments.security.encryption.key-length:256}")
    private int keyLength;

    @Value("${payments.security.encryption.iterations:10000}")
    private int iterations;

    private final Map<String, String> secretCache = new HashMap<>();

    /**
     * Encrypt a plain text value
     */
    public String encrypt(String plainText) {
        try {
            SecretKeySpec secretKey = new SecretKeySpec(
                Base64.getDecoder().decode(encryptionKey), algorithm);
            
            Cipher cipher = Cipher.getInstance(algorithm);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            
            byte[] encryptedBytes = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            log.error("Failed to encrypt value: {}", e.getMessage(), e);
            throw new RuntimeException("Encryption failed", e);
        }
    }

    /**
     * Decrypt an encrypted value
     */
    public String decrypt(String encryptedText) {
        try {
            SecretKeySpec secretKey = new SecretKeySpec(
                Base64.getDecoder().decode(encryptionKey), algorithm);
            
            Cipher cipher = Cipher.getInstance(algorithm);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            
            byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedText));
            return new String(decryptedBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("Failed to decrypt value: {}", e.getMessage(), e);
            throw new RuntimeException("Decryption failed", e);
        }
    }

    /**
     * Get a secret value (with caching)
     */
    public String getSecret(String key) {
        return secretCache.computeIfAbsent(key, k -> {
            String value = System.getenv(key);
            if (value == null) {
                value = System.getProperty(key);
            }
            if (value == null) {
                log.warn("Secret not found for key: {}", key);
                return null;
            }
            return value;
        });
    }

    /**
     * Get a secret value with default
     */
    public String getSecret(String key, String defaultValue) {
        String value = getSecret(key);
        return value != null ? value : defaultValue;
    }

    /**
     * Get an encrypted secret value
     */
    public String getEncryptedSecret(String key) {
        String encryptedValue = getSecret(key);
        if (encryptedValue == null) {
            return null;
        }
        try {
            return decrypt(encryptedValue);
        } catch (Exception e) {
            log.error("Failed to decrypt secret for key: {}", key, e);
            return encryptedValue; // Return as-is if decryption fails
        }
    }

    /**
     * Get an encrypted secret value with default
     */
    public String getEncryptedSecret(String key, String defaultValue) {
        String value = getEncryptedSecret(key);
        return value != null ? value : defaultValue;
    }

    /**
     * Store a secret value (encrypted)
     */
    public void storeSecret(String key, String value) {
        String encryptedValue = encrypt(value);
        System.setProperty(key, encryptedValue);
        secretCache.put(key, encryptedValue);
        log.debug("Stored encrypted secret for key: {}", key);
    }

    /**
     * Store a secret value (plain text)
     */
    public void storePlainSecret(String key, String value) {
        System.setProperty(key, value);
        secretCache.put(key, value);
        log.debug("Stored plain secret for key: {}", key);
    }

    /**
     * Remove a secret value
     */
    public void removeSecret(String key) {
        System.clearProperty(key);
        secretCache.remove(key);
        log.debug("Removed secret for key: {}", key);
    }

    /**
     * Check if a secret exists
     */
    public boolean hasSecret(String key) {
        return getSecret(key) != null;
    }

    /**
     * Generate a new encryption key
     */
    public String generateEncryptionKey() {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance(algorithm);
            keyGenerator.init(keyLength);
            SecretKey secretKey = keyGenerator.generateKey();
            return Base64.getEncoder().encodeToString(secretKey.getEncoded());
        } catch (Exception e) {
            log.error("Failed to generate encryption key: {}", e.getMessage(), e);
            throw new RuntimeException("Key generation failed", e);
        }
    }

    /**
     * Generate a random salt
     */
    public String generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] saltBytes = new byte[16];
        random.nextBytes(saltBytes);
        return Base64.getEncoder().encodeToString(saltBytes);
    }

    /**
     * Hash a value with salt
     */
    public String hash(String value, String salt) {
        try {
            javax.crypto.spec.PBEKeySpec spec = new javax.crypto.spec.PBEKeySpec(
                value.toCharArray(), 
                salt.getBytes(StandardCharsets.UTF_8), 
                iterations, 
                keyLength
            );
            javax.crypto.SecretKeyFactory factory = javax.crypto.SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            byte[] hash = factory.generateSecret(spec).getEncoded();
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            log.error("Failed to hash value: {}", e.getMessage(), e);
            throw new RuntimeException("Hashing failed", e);
        }
    }

    /**
     * Verify a hashed value
     */
    public boolean verifyHash(String value, String salt, String hash) {
        String computedHash = hash(value, salt);
        return computedHash.equals(hash);
    }

    /**
     * Clear the secret cache
     */
    public void clearCache() {
        secretCache.clear();
        log.debug("Secret cache cleared");
    }

    /**
     * Get cache statistics
     */
    public Map<String, Object> getCacheStats() {
        return Map.of(
            "cacheSize", secretCache.size(),
            "algorithm", algorithm,
            "keyLength", keyLength,
            "iterations", iterations
        );
    }
}






