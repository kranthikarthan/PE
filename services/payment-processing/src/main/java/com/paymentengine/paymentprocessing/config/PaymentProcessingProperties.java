package com.paymentengine.paymentprocessing.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Map;

/**
 * Centralized Configuration Properties for Payment Processing Service
 * Using @ConfigurationProperties for type-safe configuration
 */
@Configuration
@ConfigurationProperties(prefix = "payment-processing")
@Validated
public class PaymentProcessingProperties {

    private Kafka kafka = new Kafka();
    private Retry retry = new Retry();
    private CircuitBreaker circuitBreaker = new CircuitBreaker();
    private FeatureFlags featureFlags = new FeatureFlags();
    private Security security = new Security();
    private Iso20022 iso20022 = new Iso20022();

    // Getters and Setters
    public Kafka getKafka() { return kafka; }
    public void setKafka(Kafka kafka) { this.kafka = kafka; }
    
    public Retry getRetry() { return retry; }
    public void setRetry(Retry retry) { this.retry = retry; }
    
    public CircuitBreaker getCircuitBreaker() { return circuitBreaker; }
    public void setCircuitBreaker(CircuitBreaker circuitBreaker) { this.circuitBreaker = circuitBreaker; }
    
    public FeatureFlags getFeatureFlags() { return featureFlags; }
    public void setFeatureFlags(FeatureFlags featureFlags) { this.featureFlags = featureFlags; }
    
    public Security getSecurity() { return security; }
    public void setSecurity(Security security) { this.security = security; }
    
    public Iso20022 getIso20022() { return iso20022; }
    public void setIso20022(Iso20022 iso20022) { this.iso20022 = iso20022; }

    // Nested Configuration Classes
    public static class Kafka {
        private String dlqTopicSuffix = ".dlq.v1";
        private boolean enableDlq = true;
        private int maxRetries = 3;
        private long retryBackoffMs = 1000;

        public String getDlqTopicSuffix() { return dlqTopicSuffix; }
        public void setDlqTopicSuffix(String dlqTopicSuffix) { this.dlqTopicSuffix = dlqTopicSuffix; }
        
        public boolean isEnableDlq() { return enableDlq; }
        public void setEnableDlq(boolean enableDlq) { this.enableDlq = enableDlq; }
        
        public int getMaxRetries() { return maxRetries; }
        public void setMaxRetries(int maxRetries) { this.maxRetries = maxRetries; }
        
        public long getRetryBackoffMs() { return retryBackoffMs; }
        public void setRetryBackoffMs(long retryBackoffMs) { this.retryBackoffMs = retryBackoffMs; }
    }

    public static class Retry {
        @Min(1)
        private int maxAttempts = 3;
        @Min(100)
        private long initialIntervalMs = 1000;
        @Min(1)
        private double multiplier = 2.0;
        @Min(1000)
        private long maxIntervalMs = 10000;

        public int getMaxAttempts() { return maxAttempts; }
        public void setMaxAttempts(int maxAttempts) { this.maxAttempts = maxAttempts; }
        
        public long getInitialIntervalMs() { return initialIntervalMs; }
        public void setInitialIntervalMs(long initialIntervalMs) { this.initialIntervalMs = initialIntervalMs; }
        
        public double getMultiplier() { return multiplier; }
        public void setMultiplier(double multiplier) { this.multiplier = multiplier; }
        
        public long getMaxIntervalMs() { return maxIntervalMs; }
        public void setMaxIntervalMs(long maxIntervalMs) { this.maxIntervalMs = maxIntervalMs; }
    }

    public static class CircuitBreaker {
        @Min(1)
        private int slidingWindowSize = 10;
        @Min(1)
        private int minimumNumberOfCalls = 5;
        @Min(1)
        private int failureRateThreshold = 50;
        @Min(1000)
        private long waitDurationInOpenStateMs = 30000;

        public int getSlidingWindowSize() { return slidingWindowSize; }
        public void setSlidingWindowSize(int slidingWindowSize) { this.slidingWindowSize = slidingWindowSize; }
        
        public int getMinimumNumberOfCalls() { return minimumNumberOfCalls; }
        public void setMinimumNumberOfCalls(int minimumNumberOfCalls) { this.minimumNumberOfCalls = minimumNumberOfCalls; }
        
        public int getFailureRateThreshold() { return failureRateThreshold; }
        public void setFailureRateThreshold(int failureRateThreshold) { this.failureRateThreshold = failureRateThreshold; }
        
        public long getWaitDurationInOpenStateMs() { return waitDurationInOpenStateMs; }
        public void setWaitDurationInOpenStateMs(long waitDurationInOpenStateMs) { this.waitDurationInOpenStateMs = waitDurationInOpenStateMs; }
    }

    public static class FeatureFlags {
        private boolean enableFraudCheck = true;
        private boolean enableDuplicateDetection = true;
        private boolean enableAsyncProcessing = true;
        private boolean enableOutboxPattern = false;
        private boolean enableCaching = true;
        private boolean enableMetrics = true;
        private boolean enableTracing = true;
        private Map<String, Boolean> customFlags = new HashMap<>();

        public boolean isEnableFraudCheck() { return enableFraudCheck; }
        public void setEnableFraudCheck(boolean enableFraudCheck) { this.enableFraudCheck = enableFraudCheck; }
        
        public boolean isEnableDuplicateDetection() { return enableDuplicateDetection; }
        public void setEnableDuplicateDetection(boolean enableDuplicateDetection) { this.enableDuplicateDetection = enableDuplicateDetection; }
        
        public boolean isEnableAsyncProcessing() { return enableAsyncProcessing; }
        public void setEnableAsyncProcessing(boolean enableAsyncProcessing) { this.enableAsyncProcessing = enableAsyncProcessing; }
        
        public boolean isEnableOutboxPattern() { return enableOutboxPattern; }
        public void setEnableOutboxPattern(boolean enableOutboxPattern) { this.enableOutboxPattern = enableOutboxPattern; }
        
        public boolean isEnableCaching() { return enableCaching; }
        public void setEnableCaching(boolean enableCaching) { this.enableCaching = enableCaching; }
        
        public boolean isEnableMetrics() { return enableMetrics; }
        public void setEnableMetrics(boolean enableMetrics) { this.enableMetrics = enableMetrics; }
        
        public boolean isEnableTracing() { return enableTracing; }
        public void setEnableTracing(boolean enableTracing) { this.enableTracing = enableTracing; }
        
        public Map<String, Boolean> getCustomFlags() { return customFlags; }
        public void setCustomFlags(Map<String, Boolean> customFlags) { this.customFlags = customFlags; }
    }

    public static class Security {
        @NotBlank
        private String jwtSecretKey = "${JWT_SECRET_KEY:changeme}";
        @Min(1)
        private long jwtExpirationMs = 3600000;  // 1 hour
        private boolean enableJwtAuth = true;
        private boolean enableRateLimiting = true;
        @Min(1)
        private int maxRequestsPerMinute = 100;

        public String getJwtSecretKey() { return jwtSecretKey; }
        public void setJwtSecretKey(String jwtSecretKey) { this.jwtSecretKey = jwtSecretKey; }
        
        public long getJwtExpirationMs() { return jwtExpirationMs; }
        public void setJwtExpirationMs(long jwtExpirationMs) { this.jwtExpirationMs = jwtExpirationMs; }
        
        public boolean isEnableJwtAuth() { return enableJwtAuth; }
        public void setEnableJwtAuth(boolean enableJwtAuth) { this.enableJwtAuth = enableJwtAuth; }
        
        public boolean isEnableRateLimiting() { return enableRateLimiting; }
        public void setEnableRateLimiting(boolean enableRateLimiting) { this.enableRateLimiting = enableRateLimiting; }
        
        public int getMaxRequestsPerMinute() { return maxRequestsPerMinute; }
        public void setMaxRequestsPerMinute(int maxRequestsPerMinute) { this.maxRequestsPerMinute = maxRequestsPerMinute; }
    }

    public static class Iso20022 {
        private boolean enableValidation = true;
        private boolean enableTransformation = true;
        private boolean enablePersistence = true;
        @Min(1)
        private int maxMessageSizeBytes = 10485760;  // 10MB
        private String defaultCharset = "UTF-8";

        public boolean isEnableValidation() { return enableValidation; }
        public void setEnableValidation(boolean enableValidation) { this.enableValidation = enableValidation; }
        
        public boolean isEnableTransformation() { return enableTransformation; }
        public void setEnableTransformation(boolean enableTransformation) { this.enableTransformation = enableTransformation; }
        
        public boolean isEnablePersistence() { return enablePersistence; }
        public void setEnablePersistence(boolean enablePersistence) { this.enablePersistence = enablePersistence; }
        
        public int getMaxMessageSizeBytes() { return maxMessageSizeBytes; }
        public void setMaxMessageSizeBytes(int maxMessageSizeBytes) { this.maxMessageSizeBytes = maxMessageSizeBytes; }
        
        public String getDefaultCharset() { return defaultCharset; }
        public void setDefaultCharset(String defaultCharset) { this.defaultCharset = defaultCharset; }
    }
}
