package com.paymentengine.shared.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ComponentScan;

/**
 * Auto-configuration for shared services
 */
@Configuration
@ComponentScan(basePackages = {
    "com.paymentengine.shared.config",
    "com.paymentengine.shared.tenant",
    "com.paymentengine.shared.service",
    "com.paymentengine.shared.util"
})
public class SharedAutoConfiguration {
    
    /**
     * ObjectMapper bean for JSON serialization/deserialization
     */
    @Bean
    @ConditionalOnMissingBean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}