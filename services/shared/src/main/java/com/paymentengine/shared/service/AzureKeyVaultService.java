package com.paymentengine.shared.service;

import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.models.KeyVaultSecret;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Service for managing secrets with Azure Key Vault
 */
@Service
@ConditionalOnBean(SecretClient.class)
public class AzureKeyVaultService {
    
    private static final Logger logger = LoggerFactory.getLogger(AzureKeyVaultService.class);
    
    private final SecretClient secretClient;
    
    @Autowired
    public AzureKeyVaultService(SecretClient secretClient) {
        this.secretClient = secretClient;
    }
    
    /**
     * Get secret value from Key Vault with caching
     */
    @Cacheable(value = "keyvault-secrets", key = "#secretName")
    public String getSecret(String secretName) {
        try {
            logger.debug("Retrieving secret: {}", secretName);
            
            KeyVaultSecret secret = secretClient.getSecret(secretName);
            
            if (secret == null || secret.getValue() == null) {
                logger.warn("Secret not found or empty: {}", secretName);
                return null;
            }
            
            logger.debug("Secret retrieved successfully: {}", secretName);
            return secret.getValue();
            
        } catch (Exception e) {
            logger.error("Error retrieving secret {}: {}", secretName, e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve secret: " + secretName, e);
        }
    }
    
    /**
     * Get multiple secrets at once
     */
    public Map<String, String> getSecrets(String... secretNames) {
        Map<String, String> secrets = new HashMap<>();
        
        for (String secretName : secretNames) {
            try {
                String value = getSecret(secretName);
                if (value != null) {
                    secrets.put(secretName, value);
                }
            } catch (Exception e) {
                logger.error("Failed to retrieve secret {}: {}", secretName, e.getMessage());
            }
        }
        
        return secrets;
    }
    
    /**
     * Set secret in Key Vault
     */
    public void setSecret(String secretName, String secretValue) {
        try {
            logger.debug("Setting secret: {}", secretName);
            
            secretClient.setSecret(secretName, secretValue);
            
            // Evict from cache to force refresh
            evictSecretFromCache(secretName);
            
            logger.info("Secret set successfully: {}", secretName);
            
        } catch (Exception e) {
            logger.error("Error setting secret {}: {}", secretName, e.getMessage(), e);
            throw new RuntimeException("Failed to set secret: " + secretName, e);
        }
    }
    
    /**
     * Check if secret exists
     */
    public boolean secretExists(String secretName) {
        try {
            KeyVaultSecret secret = secretClient.getSecret(secretName);
            return secret != null && secret.getValue() != null;
        } catch (Exception e) {
            logger.debug("Secret does not exist or error checking: {}", secretName);
            return false;
        }
    }
    
    /**
     * Get database connection string from Key Vault
     */
    @Cacheable(value = "database-connection", key = "'database-url'")
    public String getDatabaseConnectionString() {
        String host = getSecret("database-host");
        String port = getSecret("database-port");
        String database = getSecret("database-name");
        String username = getSecret("database-username");
        String password = getSecret("database-password");
        
        if (host == null || database == null || username == null || password == null) {
            throw new RuntimeException("Database connection secrets not found in Key Vault");
        }
        
        return String.format("jdbc:postgresql://%s:%s/%s?user=%s&password=%s&sslmode=require",
            host, port != null ? port : "5432", database, username, password);
    }
    
    /**
     * Get JWT signing key from Key Vault
     */
    @Cacheable(value = "jwt-key", key = "'jwt-secret'")
    public String getJwtSecret() {
        String jwtSecret = getSecret("jwt-secret");
        
        if (jwtSecret == null) {
            logger.warn("JWT secret not found in Key Vault, using default");
            return "default-jwt-secret-change-in-production";
        }
        
        return jwtSecret;
    }
    
    /**
     * Get encryption key from Key Vault
     */
    @Cacheable(value = "encryption-key", key = "'encryption-key'")
    public String getEncryptionKey() {
        String encryptionKey = getSecret("encryption-key");
        
        if (encryptionKey == null) {
            throw new RuntimeException("Encryption key not found in Key Vault");
        }
        
        return encryptionKey;
    }
    
    private void evictSecretFromCache(String secretName) {
        // This would evict the specific secret from cache
        // Implementation depends on the cache manager being used
        logger.debug("Evicting secret from cache: {}", secretName);
    }
}