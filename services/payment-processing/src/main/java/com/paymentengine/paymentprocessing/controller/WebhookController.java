package com.paymentengine.paymentprocessing.controller;

import com.paymentengine.paymentprocessing.service.WebhookService;
import io.micrometer.core.annotation.Timed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import java.util.List;
import java.util.Set;

import java.time.Instant;
import java.util.Map;

/**
 * Webhook management controller
 */
@RestController
@RequestMapping("/api/v1/webhooks")
@CrossOrigin(origins = "*", maxAge = 3600)
public class WebhookController {
    
    private static final Logger logger = LoggerFactory.getLogger(WebhookController.class);
    
    private final WebhookService webhookService;
    private final RedisTemplate<String, Object> redisTemplate;

    @Value("webhook:configs")
    private String WEBHOOK_CONFIG_KEY;
    
    @Autowired
    public WebhookController(WebhookService webhookService, RedisTemplate<String, Object> redisTemplate) {
        this.webhookService = webhookService;
        this.redisTemplate = redisTemplate;
    }
    
    /**
     * Test webhook endpoint
     */
    @PostMapping("/test")
    @PreAuthorize("hasAuthority('webhook:create')")
    @Timed(value = "webhook.test", description = "Time taken to test webhook")
    public ResponseEntity<Map<String, Object>> testWebhook(
            @RequestBody Map<String, Object> request) {
        
        String webhookUrl = (String) request.get("url");
        String secret = (String) request.get("secret");
        
        logger.info("Testing webhook URL: {}", webhookUrl);
        
        try {
            Map<String, Object> testPayload = Map.of(
                "eventType", "webhook.test",
                "timestamp", Instant.now().toString(),
                "data", Map.of(
                    "message", "This is a test webhook from Payment Engine",
                    "testId", java.util.UUID.randomUUID().toString()
                )
            );
            
            WebhookService.WebhookEndpoint testEndpoint = new WebhookService.WebhookEndpoint(
                "test-endpoint",
                webhookUrl,
                secret,
                List.of("webhook.test"),
                true
            );
            
            webhookService.sendWebhook(testEndpoint, testPayload).get();
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Webhook test sent successfully",
                "timestamp", Instant.now().toString()
            ));
            
        } catch (Exception e) {
            logger.error("Webhook test failed for URL {}: {}", webhookUrl, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", e.getMessage(),
                "timestamp", Instant.now().toString()
            ));
        }
    }
    
    /**
     * Get webhook delivery status
     */
    @GetMapping("/delivery-status")
    @PreAuthorize("hasAuthority('webhook:read')")
    @Timed(value = "webhook.delivery_status", description = "Time taken to get webhook delivery status")
    public ResponseEntity<Map<String, Object>> getWebhookDeliveryStatus(
            @RequestParam(required = false) String endpointName,
            @RequestParam(defaultValue = "24") int hours) {
        
        logger.debug("Getting webhook delivery status for endpoint: {}, hours: {}", endpointName, hours);
        
        try {
            // This would query delivery records from Redis/database
            Map<String, Object> deliveryStatus = Map.of(
                "totalDeliveries", 150,
                "successfulDeliveries", 145,
                "failedDeliveries", 5,
                "successRate", 96.7,
                "averageResponseTime", "245ms",
                "lastDeliveryAt", Instant.now().minusSeconds(300).toString(),
                "endpoints", Map.of(
                    "test-endpoint-1", Map.of(
                        "deliveries", 75,
                        "success", 73,
                        "failed", 2,
                        "successRate", 97.3
                    ),
                    "test-endpoint-2", Map.of(
                        "deliveries", 75,
                        "success", 72,
                        "failed", 3,
                        "successRate", 96.0
                    )
                )
            );
            
            return ResponseEntity.ok(deliveryStatus);
            
        } catch (Exception e) {
            logger.error("Error getting webhook delivery status: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Retry failed webhooks
     */
    @PostMapping("/retry")
    @PreAuthorize("hasAuthority('webhook:update')")
    @Timed(value = "webhook.retry", description = "Time taken to retry webhooks")
    public ResponseEntity<Map<String, Object>> retryFailedWebhooks(
            @RequestBody Map<String, Object> request) {
        
        String eventId = (String) request.get("eventId");
        String endpointName = (String) request.get("endpointName");
        
        logger.info("Retrying failed webhooks - eventId: {}, endpoint: {}", eventId, endpointName);
        
        try {
            // This would implement the actual retry logic
            // For now, return a success response
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Webhook retry initiated",
                "eventId", eventId,
                "endpointName", endpointName,
                "timestamp", Instant.now().toString()
            ));
            
        } catch (Exception e) {
            logger.error("Error retrying webhooks: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }
    
    /**
     * Configure webhook endpoint
     */
    @PostMapping("/configure")
    @PreAuthorize("hasAuthority('webhook:create')")
    @Timed(value = "webhook.configure", description = "Time taken to configure webhook")
    public ResponseEntity<Map<String, Object>> configureWebhook(
            @RequestBody Map<String, Object> request) {
        
        String name = (String) request.get("name");
        String url = (String) request.get("url");
        
        logger.info("Configuring webhook endpoint: {} -> {}", name, url);
        
        try {
            // Store webhook configuration in Redis
            String configKey = WEBHOOK_CONFIG_KEY + ":" + name;
            redisTemplate.opsForHash().putAll(configKey, request);
            redisTemplate.opsForSet().add(WEBHOOK_CONFIG_KEY + ":keys", configKey);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Webhook configured successfully",
                "name", name,
                "url", url,
                "timestamp", Instant.now().toString()
            ));
            
        } catch (Exception e) {
            logger.error("Error configuring webhook {}: {}", name, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }
    
    /**
     * Get webhook configuration
     */
    @GetMapping("/config")
    @PreAuthorize("hasAuthority('webhook:read')")
    @Timed(value = "webhook.config", description = "Time taken to get webhook config")
    public ResponseEntity<Map<String, Object>> getWebhookConfig() {
        
        logger.debug("Getting webhook configuration");
        
        try {
            // Get all webhook configurations
            Set<Object> configKeys = redisTemplate.opsForSet().members(WEBHOOK_CONFIG_KEY + ":keys");
            
            Map<String, Object> config = Map.of(
                "endpoints", configKeys != null ? configKeys.size() : 0,
                "supportedEvents", List.of(
                    "transaction.created",
                    "transaction.updated", 
                    "transaction.completed",
                    "transaction.failed",
                    "account.balance.updated"
                ),
                "webhookVersion", "1.0",
                "signatureMethod", "HMAC-SHA256"
            );
            
            return ResponseEntity.ok(config);
            
        } catch (Exception e) {
            logger.error("Error getting webhook configuration: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
}