package com.paymentengine.middleware.service.impl;

import com.paymentengine.middleware.service.WebhookDeliveryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Implementation of webhook delivery service for async responses
 */
@Service
public class WebhookDeliveryServiceImpl implements WebhookDeliveryService {
    
    private static final Logger logger = LoggerFactory.getLogger(WebhookDeliveryServiceImpl.class);
    
    private final RestTemplate restTemplate;
    private final ScheduledExecutorService scheduler;
    private final Map<String, WebhookDeliveryStatus> deliveryStatuses;
    private final Map<String, WebhookDeliveryResult> deliveryResults;
    
    @Autowired
    public WebhookDeliveryServiceImpl() {
        this.restTemplate = new RestTemplate();
        this.scheduler = Executors.newScheduledThreadPool(10);
        this.deliveryStatuses = new ConcurrentHashMap<>();
        this.deliveryResults = new ConcurrentHashMap<>();
    }
    
    @Override
    public CompletableFuture<WebhookDeliveryResult> deliverWebhook(
            String webhookUrl,
            Map<String, Object> payload,
            Map<String, String> headers,
            String tenantId,
            String messageType,
            String correlationId) {
        
        return deliverWebhookWithRetry(webhookUrl, payload, headers, tenantId, messageType, correlationId, 3, 1000);
    }
    
    @Override
    public CompletableFuture<WebhookDeliveryResult> deliverWebhookWithRetry(
            String webhookUrl,
            Map<String, Object> payload,
            Map<String, String> headers,
            String tenantId,
            String messageType,
            String correlationId,
            int maxRetries,
            long retryDelayMs) {
        
        logger.info("Delivering webhook for correlationId: {}, webhookUrl: {}, messageType: {}", 
                correlationId, webhookUrl, messageType);
        
        // Set initial status
        deliveryStatuses.put(correlationId, WebhookDeliveryStatus.PENDING);
        
        return CompletableFuture.supplyAsync(() -> {
            return deliverWebhookInternal(webhookUrl, payload, headers, tenantId, messageType, 
                    correlationId, maxRetries, retryDelayMs, 0);
        });
    }
    
    private WebhookDeliveryResult deliverWebhookInternal(
            String webhookUrl,
            Map<String, Object> payload,
            Map<String, String> headers,
            String tenantId,
            String messageType,
            String correlationId,
            int maxRetries,
            long retryDelayMs,
            int currentAttempt) {
        
        long startTime = System.currentTimeMillis();
        
        try {
            // Validate webhook URL
            if (!validateWebhookUrl(webhookUrl)) {
                String errorMessage = "Invalid webhook URL: " + webhookUrl;
                logger.error("Webhook delivery failed for correlationId: {} - {}", correlationId, errorMessage);
                
                WebhookDeliveryResult result = new WebhookDeliveryResult(
                        correlationId, webhookUrl, 400, null, null,
                        System.currentTimeMillis() - startTime, false, errorMessage,
                        currentAttempt, Instant.now().toEpochMilli()
                );
                
                deliveryStatuses.put(correlationId, WebhookDeliveryStatus.FAILED);
                deliveryResults.put(correlationId, result);
                return result;
            }
            
            // Set status to delivering
            deliveryStatuses.put(correlationId, WebhookDeliveryStatus.DELIVERING);
            
            // Prepare HTTP request
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.setContentType(MediaType.APPLICATION_JSON);
            
            // Add custom headers
            if (headers != null) {
                headers.forEach(httpHeaders::set);
            }
            
            // Add standard headers
            httpHeaders.set("X-Correlation-ID", correlationId);
            httpHeaders.set("X-Tenant-ID", tenantId);
            httpHeaders.set("X-Message-Type", messageType);
            httpHeaders.set("X-Timestamp", String.valueOf(Instant.now().toEpochMilli()));
            
            // Create request entity
            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(payload, httpHeaders);
            
            // Make HTTP request
            ResponseEntity<String> response = restTemplate.exchange(
                    new URI(webhookUrl),
                    HttpMethod.POST,
                    requestEntity,
                    String.class
            );
            
            long deliveryTime = System.currentTimeMillis() - startTime;
            
            // Check if successful
            boolean success = response.getStatusCode().is2xxSuccessful();
            
            WebhookDeliveryResult result = new WebhookDeliveryResult(
                    correlationId, webhookUrl, response.getStatusCode().value(),
                    response.getBody(), response.getHeaders().toSingleValueMap(),
                    deliveryTime, success, success ? null : "HTTP " + response.getStatusCode().value(),
                    currentAttempt, Instant.now().toEpochMilli()
            );
            
            if (success) {
                logger.info("Webhook delivered successfully for correlationId: {}, status: {}, time: {}ms", 
                        correlationId, response.getStatusCode(), deliveryTime);
                deliveryStatuses.put(correlationId, WebhookDeliveryStatus.DELIVERED);
            } else {
                logger.warn("Webhook delivery failed for correlationId: {}, status: {}, time: {}ms", 
                        correlationId, response.getStatusCode(), deliveryTime);
                
                // Retry if we haven't exceeded max retries
                if (currentAttempt < maxRetries) {
                    deliveryStatuses.put(correlationId, WebhookDeliveryStatus.RETRYING);
                    return retryWebhookDelivery(webhookUrl, payload, headers, tenantId, messageType,
                            correlationId, maxRetries, retryDelayMs, currentAttempt + 1, retryDelayMs);
                } else {
                    deliveryStatuses.put(correlationId, WebhookDeliveryStatus.FAILED);
                }
            }
            
            deliveryResults.put(correlationId, result);
            return result;
            
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            long deliveryTime = System.currentTimeMillis() - startTime;
            String errorMessage = "HTTP error: " + e.getStatusCode() + " - " + e.getResponseBodyAsString();
            
            logger.error("Webhook delivery failed for correlationId: {} - {}", correlationId, errorMessage);
            
            WebhookDeliveryResult result = new WebhookDeliveryResult(
                    correlationId, webhookUrl, e.getStatusCode().value(),
                    e.getResponseBodyAsString(), null, deliveryTime, false, errorMessage,
                    currentAttempt, Instant.now().toEpochMilli()
            );
            
            // Retry if we haven't exceeded max retries
            if (currentAttempt < maxRetries) {
                deliveryStatuses.put(correlationId, WebhookDeliveryStatus.RETRYING);
                return retryWebhookDelivery(webhookUrl, payload, headers, tenantId, messageType,
                        correlationId, maxRetries, retryDelayMs, currentAttempt + 1, retryDelayMs);
            } else {
                deliveryStatuses.put(correlationId, WebhookDeliveryStatus.FAILED);
            }
            
            deliveryResults.put(correlationId, result);
            return result;
            
        } catch (ResourceAccessException e) {
            long deliveryTime = System.currentTimeMillis() - startTime;
            String errorMessage = "Connection error: " + e.getMessage();
            
            logger.error("Webhook delivery failed for correlationId: {} - {}", correlationId, errorMessage);
            
            WebhookDeliveryResult result = new WebhookDeliveryResult(
                    correlationId, webhookUrl, 0, null, null, deliveryTime, false, errorMessage,
                    currentAttempt, Instant.now().toEpochMilli()
            );
            
            // Retry if we haven't exceeded max retries
            if (currentAttempt < maxRetries) {
                deliveryStatuses.put(correlationId, WebhookDeliveryStatus.RETRYING);
                return retryWebhookDelivery(webhookUrl, payload, headers, tenantId, messageType,
                        correlationId, maxRetries, retryDelayMs, currentAttempt + 1, retryDelayMs);
            } else {
                deliveryStatuses.put(correlationId, WebhookDeliveryStatus.FAILED);
            }
            
            deliveryResults.put(correlationId, result);
            return result;
            
        } catch (URISyntaxException e) {
            long deliveryTime = System.currentTimeMillis() - startTime;
            String errorMessage = "Invalid URI: " + e.getMessage();
            
            logger.error("Webhook delivery failed for correlationId: {} - {}", correlationId, errorMessage);
            
            WebhookDeliveryResult result = new WebhookDeliveryResult(
                    correlationId, webhookUrl, 400, null, null, deliveryTime, false, errorMessage,
                    currentAttempt, Instant.now().toEpochMilli()
            );
            
            deliveryStatuses.put(correlationId, WebhookDeliveryStatus.FAILED);
            deliveryResults.put(correlationId, result);
            return result;
            
        } catch (Exception e) {
            long deliveryTime = System.currentTimeMillis() - startTime;
            String errorMessage = "Unexpected error: " + e.getMessage();
            
            logger.error("Webhook delivery failed for correlationId: {} - {}", correlationId, errorMessage, e);
            
            WebhookDeliveryResult result = new WebhookDeliveryResult(
                    correlationId, webhookUrl, 500, null, null, deliveryTime, false, errorMessage,
                    currentAttempt, Instant.now().toEpochMilli()
            );
            
            deliveryStatuses.put(correlationId, WebhookDeliveryStatus.FAILED);
            deliveryResults.put(correlationId, result);
            return result;
        }
    }
    
    private WebhookDeliveryResult retryWebhookDelivery(
            String webhookUrl,
            Map<String, Object> payload,
            Map<String, String> headers,
            String tenantId,
            String messageType,
            String correlationId,
            int maxRetries,
            long retryDelayMs,
            int currentAttempt,
            long delayMs) {
        
        logger.info("Retrying webhook delivery for correlationId: {}, attempt: {}/{}", 
                correlationId, currentAttempt, maxRetries);
        
        try {
            Thread.sleep(delayMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.warn("Webhook retry interrupted for correlationId: {}", correlationId);
        }
        
        return deliverWebhookInternal(webhookUrl, payload, headers, tenantId, messageType,
                correlationId, maxRetries, retryDelayMs, currentAttempt);
    }
    
    @Override
    public boolean validateWebhookUrl(String webhookUrl) {
        if (webhookUrl == null || webhookUrl.trim().isEmpty()) {
            return false;
        }
        
        try {
            URI uri = new URI(webhookUrl);
            String scheme = uri.getScheme();
            String host = uri.getHost();
            
            // Validate scheme
            if (scheme == null || (!scheme.equals("http") && !scheme.equals("https"))) {
                return false;
            }
            
            // Validate host
            if (host == null || host.trim().isEmpty()) {
                return false;
            }
            
            // Additional validation for security
            if (host.equals("localhost") || host.equals("127.0.0.1") || host.startsWith("192.168.") || host.startsWith("10.")) {
                logger.warn("Webhook URL points to private network: {}", webhookUrl);
                // Allow but log warning
            }
            
            return true;
            
        } catch (URISyntaxException e) {
            logger.error("Invalid webhook URL syntax: {}", webhookUrl);
            return false;
        }
    }
    
    @Override
    public WebhookDeliveryStatus getDeliveryStatus(String correlationId) {
        return deliveryStatuses.getOrDefault(correlationId, WebhookDeliveryStatus.PENDING);
    }
    
    @Override
    public Map<String, Object> getDeliveryHistory(String tenantId, String messageType, int limit) {
        // This would typically query a database in a real implementation
        // For now, return a mock response
        return Map.of(
                "tenantId", tenantId,
                "messageType", messageType,
                "limit", limit,
                "deliveries", deliveryResults.values().stream()
                        .filter(result -> result.getCorrelationId().contains(tenantId))
                        .limit(limit)
                        .map(this::convertToMap)
                        .toList(),
                "totalCount", deliveryResults.size(),
                "timestamp", Instant.now().toString()
        );
    }
    
    private Map<String, Object> convertToMap(WebhookDeliveryResult result) {
        return Map.of(
                "correlationId", result.getCorrelationId(),
                "webhookUrl", result.getWebhookUrl(),
                "statusCode", result.getStatusCode(),
                "success", result.isSuccess(),
                "deliveryTimeMs", result.getDeliveryTimeMs(),
                "retryAttempt", result.getRetryAttempt(),
                "timestamp", result.getTimestamp(),
                "errorMessage", result.getErrorMessage() != null ? result.getErrorMessage() : ""
        );
    }
}