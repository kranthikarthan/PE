package com.paymentengine.middleware.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * Webhook service for managing and sending webhook notifications
 */
@Service
public class WebhookService {
    
    private static final Logger logger = LoggerFactory.getLogger(WebhookService.class);
    private static final String WEBHOOK_CONFIG_KEY = "webhook_endpoints";
    
    private final WebClient webClient;
    private final RedisTemplate<String, Object> redisTemplate;
    private final NotificationService notificationService;
    
    @Autowired
    public WebhookService(WebClient.Builder webClientBuilder, 
                         RedisTemplate<String, Object> redisTemplate,
                         NotificationService notificationService) {
        this.webClient = webClientBuilder
            .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(1024 * 1024))
            .build();
        this.redisTemplate = redisTemplate;
        this.notificationService = notificationService;
    }
    
    /**
     * Send webhooks for a specific event type
     */
    @Async
    public CompletableFuture<Void> sendWebhooksForEvent(String eventType, Map<String, Object> payload) {
        return CompletableFuture.runAsync(() -> {
            try {
                logger.debug("Sending webhooks for event type: {}", eventType);
                
                List<WebhookEndpoint> endpoints = getWebhookEndpoints(eventType);
                
                if (endpoints.isEmpty()) {
                    logger.debug("No webhook endpoints configured for event type: {}", eventType);
                    return;
                }
                
                // Send webhooks in parallel
                endpoints.parallelStream().forEach(endpoint -> {
                    try {
                        sendWebhook(endpoint, payload).join();
                    } catch (Exception e) {
                        logger.error("Failed to send webhook to {}: {}", endpoint.getUrl(), e.getMessage());
                        handleWebhookFailure(endpoint, payload, e);
                    }
                });
                
                logger.info("Webhooks sent for event type: {} to {} endpoints", eventType, endpoints.size());
                
            } catch (Exception e) {
                logger.error("Error sending webhooks for event type {}: {}", eventType, e.getMessage(), e);
            }
        });
    }
    
    /**
     * Send individual webhook
     */
    @Async
    public CompletableFuture<Void> sendWebhook(WebhookEndpoint endpoint, Map<String, Object> payload) {
        return CompletableFuture.runAsync(() -> {
            try {
                logger.debug("Sending webhook to: {}", endpoint.getUrl());
                
                // Generate signature
                String signature = generateSignature(payload, endpoint.getSecret());
                
                // Add webhook metadata
                Map<String, Object> webhookPayload = Map.of(
                    "webhook", Map.of(
                        "id", java.util.UUID.randomUUID().toString(),
                        "timestamp", Instant.now().toString(),
                        "version", "1.0",
                        "endpoint", endpoint.getName()
                    ),
                    "event", payload
                );
                
                Mono<String> response = webClient.post()
                    .uri(endpoint.getUrl())
                    .header("Content-Type", "application/json")
                    .header("User-Agent", "PaymentEngine-Webhook/1.0")
                    .header("X-Webhook-Signature", signature)
                    .header("X-Webhook-Timestamp", String.valueOf(Instant.now().getEpochSecond()))
                    .header("X-Webhook-Event", (String) payload.get("eventType"))
                    .bodyValue(webhookPayload)
                    .retrieve()
                    .onStatus(
                        status -> status.is4xxClientError() || status.is5xxServerError(),
                        clientResponse -> Mono.error(new RuntimeException("Webhook failed with status: " + clientResponse.statusCode()))
                    )
                    .bodyToMono(String.class);
                
                String result = response.block();
                
                logger.info("Webhook sent successfully to: {}, response: {}", endpoint.getUrl(), 
                           result != null && result.length() > 100 ? result.substring(0, 100) + "..." : result);
                
                // Record successful delivery
                recordWebhookDelivery(endpoint, payload, true, null);
                
            } catch (Exception e) {
                logger.error("Failed to send webhook to {}: {}", endpoint.getUrl(), e.getMessage());
                recordWebhookDelivery(endpoint, payload, false, e.getMessage());
                throw new RuntimeException("Webhook delivery failed", e);
            }
        });
    }
    
    /**
     * Get webhook endpoints for event type
     */
    private List<WebhookEndpoint> getWebhookEndpoints(String eventType) {
        try {
            // In a real implementation, this would query a database
            // For now, return mock endpoints from Redis configuration
            
            @SuppressWarnings("unchecked")
            Map<String, Object> config = (Map<String, Object>) redisTemplate.opsForHash()
                .get(WEBHOOK_CONFIG_KEY, eventType);
            
            if (config == null) {
                return getDefaultWebhookEndpoints(eventType);
            }
            
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> endpointConfigs = (List<Map<String, Object>>) config.get("endpoints");
            
            return endpointConfigs.stream()
                .map(this::mapToWebhookEndpoint)
                .filter(WebhookEndpoint::isActive)
                .toList();
            
        } catch (Exception e) {
            logger.error("Error getting webhook endpoints for event type {}: {}", eventType, e.getMessage());
            return List.of();
        }
    }
    
    /**
     * Get default webhook endpoints for testing
     */
    private List<WebhookEndpoint> getDefaultWebhookEndpoints(String eventType) {
        // Mock webhook endpoints for demonstration
        return List.of(
            new WebhookEndpoint(
                "test-webhook-1",
                "https://webhook.site/test-endpoint-1",
                "test-secret-key-1",
                List.of("transaction.created", "transaction.completed", "transaction.failed"),
                true
            ),
            new WebhookEndpoint(
                "test-webhook-2",
                "https://webhook.site/test-endpoint-2",
                "test-secret-key-2",
                List.of("account.balance.updated"),
                true
            )
        );
    }
    
    private WebhookEndpoint mapToWebhookEndpoint(Map<String, Object> config) {
        return new WebhookEndpoint(
            (String) config.get("name"),
            (String) config.get("url"),
            (String) config.get("secret"),
            (List<String>) config.get("events"),
            Boolean.TRUE.equals(config.get("active"))
        );
    }
    
    private String generateSignature(Map<String, Object> payload, String secret) {
        try {
            // Use the notification service method for consistency
            return notificationService.generateWebhookSignature(payload, secret);
        } catch (Exception e) {
            logger.error("Error generating webhook signature: {}", e.getMessage());
            return "";
        }
    }
    
    private void recordWebhookDelivery(WebhookEndpoint endpoint, Map<String, Object> payload, 
                                     boolean success, String errorMessage) {
        try {
            String deliveryKey = "webhook_delivery:" + endpoint.getName() + ":" + Instant.now().getEpochSecond();
            
            Map<String, Object> deliveryRecord = Map.of(
                "endpoint", endpoint.getName(),
                "url", endpoint.getUrl(),
                "eventType", payload.get("eventType"),
                "eventId", ((Map<String, Object>) payload.get("event")).get("eventId"),
                "timestamp", Instant.now().toString(),
                "success", success,
                "errorMessage", errorMessage != null ? errorMessage : "",
                "retryCount", 0
            );
            
            redisTemplate.opsForHash().putAll(deliveryKey, deliveryRecord);
            redisTemplate.expire(deliveryKey, java.time.Duration.ofDays(7)); // Keep for 7 days
            
        } catch (Exception e) {
            logger.error("Error recording webhook delivery: {}", e.getMessage());
        }
    }
    
    private void handleWebhookFailure(WebhookEndpoint endpoint, Map<String, Object> payload, Exception error) {
        try {
            // Implement retry logic
            logger.warn("Webhook delivery failed for endpoint {}, scheduling retry", endpoint.getName());
            
            // Store failed webhook for retry processing
            String retryKey = "webhook_retry:" + java.util.UUID.randomUUID().toString();
            
            Map<String, Object> retryData = Map.of(
                "endpoint", endpoint,
                "payload", payload,
                "error", error.getMessage(),
                "timestamp", Instant.now().toString(),
                "retryCount", 0,
                "nextRetryAt", Instant.now().plusSeconds(60).toString() // Retry in 1 minute
            );
            
            redisTemplate.opsForValue().set(retryKey, retryData, java.time.Duration.ofHours(24));
            
        } catch (Exception e) {
            logger.error("Error handling webhook failure: {}", e.getMessage());
        }
    }
    
    /**
     * Webhook endpoint configuration class
     */
    public static class WebhookEndpoint {
        private String name;
        private String url;
        private String secret;
        private List<String> eventTypes;
        private boolean active;
        
        public WebhookEndpoint(String name, String url, String secret, List<String> eventTypes, boolean active) {
            this.name = name;
            this.url = url;
            this.secret = secret;
            this.eventTypes = eventTypes;
            this.active = active;
        }
        
        // Getters
        public String getName() { return name; }
        public String getUrl() { return url; }
        public String getSecret() { return secret; }
        public List<String> getEventTypes() { return eventTypes; }
        public boolean isActive() { return active; }
    }
}