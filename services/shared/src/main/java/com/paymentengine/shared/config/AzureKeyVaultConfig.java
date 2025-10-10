package com.paymentengine.shared.config;

import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.SecretClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Azure Key Vault configuration for secure secrets management
 */
@Configuration
@ConditionalOnProperty(name = "azure.keyvault.enabled", havingValue = "true", matchIfMissing = false)
public class AzureKeyVaultConfig {
    
    private static final Logger logger = LoggerFactory.getLogger(AzureKeyVaultConfig.class);
    
    @Value("${azure.keyvault.uri:}")
    private String keyVaultUri;
    
    @Bean
    public SecretClient secretClient() {
        logger.info("Initializing Azure Key Vault client for URI: {}", keyVaultUri);
        
        try {
            return new SecretClientBuilder()
                .vaultUrl(keyVaultUri)
                .credential(new DefaultAzureCredentialBuilder().build())
                .buildClient();
        } catch (Exception e) {
            logger.error("Failed to initialize Azure Key Vault client: {}", e.getMessage(), e);
            throw new RuntimeException("Azure Key Vault initialization failed", e);
        }
    }
}