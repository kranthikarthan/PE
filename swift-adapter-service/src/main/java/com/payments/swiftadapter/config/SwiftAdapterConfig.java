package com.payments.swiftadapter.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Configuration class for SWIFT Adapter
 */
@Configuration
@ConfigurationProperties(prefix = "swift.adapter")
@Data
@Import({
    // Add any additional configuration classes here
})
public class SwiftAdapterConfig {
    
    /** SWIFT endpoint configuration */
    private String endpoint;
    private String apiVersion;
    private Integer timeoutSeconds;
    private Integer retryAttempts;
    private Boolean encryptionEnabled;
    private String processingWindowStart;
    private String processingWindowEnd;
}
