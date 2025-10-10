package com.paymentengine.paymentprocessing.service.impl;

import com.paymentengine.paymentprocessing.entity.Status;

import com.paymentengine.paymentprocessing.entity.QueuedMessage;
import com.paymentengine.paymentprocessing.repository.QueuedMessageRepository;
import com.paymentengine.paymentprocessing.service.MessageQueueService;
import com.paymentengine.paymentprocessing.service.ResiliencyConfigurationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * Implementation of MessageQueueService for handling queued messages
 * during service outages and recovery scenarios
 */
@Service
@Transactional
public class MessageQueueServiceImpl implements MessageQueueService {

    private static final Logger logger = LoggerFactory.getLogger(MessageQueueServiceImpl.class);

    @Autowired
    private QueuedMessageRepository queuedMessageRepository;

    @Autowired
    private ResiliencyConfigurationService resiliencyConfigurationService;

    @Autowired
    private RestTemplate restTemplate;

    private final ExecutorService executorService = Executors.newFixedThreadPool(10);

    @Override
    public String enqueueMessage(String messageType, String tenantId, String serviceName, 
                                String endpointUrl, String httpMethod, Map<String, Object> payload,
                                Map<String, String> headers, Map<String, Object> metadata) {
        logger.info("Enqueuing message for service: {} tenant: {} type: {}", serviceName, tenantId, messageType);

        try {
            QueuedMessage queuedMessage = new QueuedMessage();
            queuedMessage.setMessageId(UUID.randomUUID().toString());
            queuedMessage.setMessageType(messageType);
            queuedMessage.setTenantId(tenantId);
            queuedMessage.setServiceName(serviceName);
            queuedMessage.setEndpointUrl(endpointUrl);
            queuedMessage.setHttpMethod(httpMethod != null ? httpMethod : "POST");
            queuedMessage.setPayload(payload);
            queuedMessage.setHeaders(headers);
            queuedMessage.setMetadata(metadata);
            queuedMessage.setStatus(QueuedMessage.Status.PENDING);
            queuedMessage.setPriority(1);
            queuedMessage.setRetryCount(0);
            queuedMessage.setMaxRetries(3);
            queuedMessage.setNextRetryAt(calculateNextRetryTime(0));
            queuedMessage.setExpiresAt(LocalDateTime.now().plusHours(24)); // Default 24 hour expiry
            queuedMessage.setCreatedAt(LocalDateTime.now());
            queuedMessage.setUpdatedAt(LocalDateTime.now());
            queuedMessage.setCreatedBy("system");
            queuedMessage.setUpdatedBy("system");

            // Add correlation ID if not present
            if (metadata != null && !metadata.containsKey("correlationId")) {
                metadata.put("correlationId", queuedMessage.getMessageId());
                queuedMessage.setCorrelationId(queuedMessage.getMessageId());
            }

            QueuedMessage savedMessage = queuedMessageRepository.save(queuedMessage);
            logger.info("Successfully enqueued message: {} for service: {}", savedMessage.getMessageId(), serviceName);

            return savedMessage.getMessageId();
        } catch (Exception e) {
            logger.error("Failed to enqueue message for service: {} tenant: {} type: {}", serviceName, tenantId, messageType, e);
            throw new RuntimeException("Failed to enqueue message", e);
        }
    }

    @Override
    public CompletableFuture<Map<String, Object>> processMessage(String messageId) {
        logger.info("Processing queued message: {}", messageId);

        return CompletableFuture.supplyAsync(() -> {
            try {
                Optional<QueuedMessage> messageOpt = queuedMessageRepository.findByMessageId(messageId);
                if (messageOpt.isEmpty()) {
                    throw new RuntimeException("Message not found: " + messageId);
                }

                QueuedMessage message = messageOpt.get();
                
                // Check if message has expired
                if (message.getExpiresAt() != null && LocalDateTime.now().isAfter(message.getExpiresAt())) {
                    message.setStatus(QueuedMessage.Status.EXPIRED);
                    message.setErrorMessage("Message expired");
                    message.setUpdatedAt(LocalDateTime.now());
                    queuedMessageRepository.save(message);
                    throw new RuntimeException("Message expired: " + messageId);
                }

                // Update status to processing
                message.setStatus(QueuedMessage.Status.PROCESSING);
                message.setProcessingStartedAt(LocalDateTime.now());
                message.setUpdatedAt(LocalDateTime.now());
                queuedMessageRepository.save(message);

                // Execute the actual call with resiliency patterns
                Map<String, Object> result = executeQueuedCall(message);

                // Update message with success result
                message.setStatus(QueuedMessage.Status.PROCESSED);
                message.setProcessingCompletedAt(LocalDateTime.now());
                message.setProcessingTimeMs(ChronoUnit.MILLIS.between(message.getProcessingStartedAt(), message.getProcessingCompletedAt()));
                message.setResult(result);
                message.setUpdatedAt(LocalDateTime.now());
                queuedMessageRepository.save(message);

                logger.info("Successfully processed queued message: {} in {}ms", messageId, message.getProcessingTimeMs());
                return result;

            } catch (Exception e) {
                logger.error("Failed to process queued message: {}", messageId, e);
                handleProcessingFailure(messageId, e);
                throw new RuntimeException("Failed to process queued message: " + messageId, e);
            }
        }, executorService);
    }

    @Override
    public List<QueuedMessage> getQueuedMessages(String tenantId, String serviceName, 
                                                QueuedMessage.Status status, int limit) {
        logger.debug("Retrieving queued messages for tenant: {} service: {} status: {} limit: {}", 
                    tenantId, serviceName, status, limit);

        Pageable pageable = PageRequest.of(0, limit, Sort.by("createdAt").descending());
        
        if (tenantId != null && serviceName != null && status != null) {
            return queuedMessageRepository.findByTenantIdAndServiceNameAndStatus(tenantId, serviceName, status, pageable);
        } else if (tenantId != null && serviceName != null) {
            return queuedMessageRepository.findByTenantIdAndServiceName(tenantId, serviceName, pageable);
        } else if (tenantId != null && status != null) {
            return queuedMessageRepository.findByTenantIdAndStatus(tenantId, status, pageable);
        } else if (tenantId != null) {
            return queuedMessageRepository.findByTenantId(tenantId, pageable);
        } else if (status != null) {
            return queuedMessageRepository.findByStatus(status, pageable);
        } else {
            return queuedMessageRepository.findAll(pageable).getContent();
        }
    }

    @Override
    public List<QueuedMessage> getMessagesForRetry() {
        logger.debug("Retrieving messages ready for retry");

        LocalDateTime now = LocalDateTime.now();
        return queuedMessageRepository.findByStatusAndNextRetryAtBeforeAndRetryCountLessThan(
                QueuedMessage.Status.FAILED, now, 3);
    }

    @Override
    public CompletableFuture<Void> retryFailedMessages() {
        logger.info("Starting retry of failed messages");

        return CompletableFuture.runAsync(() -> {
            try {
                List<QueuedMessage> failedMessages = getMessagesForRetry();
                logger.info("Found {} messages ready for retry", failedMessages.size());

                List<CompletableFuture<Void>> retryFutures = failedMessages.stream()
                        .map(this::retryMessage)
                        .collect(Collectors.toList());

                CompletableFuture.allOf(retryFutures.toArray(new CompletableFuture[0])).join();
                logger.info("Completed retry of failed messages");

            } catch (Exception e) {
                logger.error("Error during retry of failed messages", e);
            }
        }, executorService);
    }

    @Override
    public void cancelMessage(String messageId, String reason) {
        logger.info("Cancelling message: {} reason: {}", messageId, reason);

        try {
            Optional<QueuedMessage> messageOpt = queuedMessageRepository.findByMessageId(messageId);
            if (messageOpt.isPresent()) {
                QueuedMessage message = messageOpt.get();
                message.setStatus(QueuedMessage.Status.CANCELLED);
                message.setErrorMessage("Cancelled: " + reason);
                message.setUpdatedAt(LocalDateTime.now());
                queuedMessageRepository.save(message);
                logger.info("Successfully cancelled message: {}", messageId);
            } else {
                logger.warn("Message not found for cancellation: {}", messageId);
            }
        } catch (Exception e) {
            logger.error("Failed to cancel message: {}", messageId, e);
            throw new RuntimeException("Failed to cancel message: " + messageId, e);
        }
    }

    @Override
    public void cleanupExpiredMessages() {
        logger.info("Starting cleanup of expired messages");

        try {
            LocalDateTime now = LocalDateTime.now();
            List<QueuedMessage> expiredMessages = queuedMessageRepository.findByExpiresAtBefore(now);
            
            for (QueuedMessage message : expiredMessages) {
                message.setStatus(QueuedMessage.Status.EXPIRED);
                message.setErrorMessage("Message expired");
                message.setUpdatedAt(LocalDateTime.now());
            }
            
            queuedMessageRepository.saveAll(expiredMessages);
            logger.info("Cleaned up {} expired messages", expiredMessages.size());

        } catch (Exception e) {
            logger.error("Error during cleanup of expired messages", e);
        }
    }

    @Override
    public Map<String, Object> getQueueStatistics(String tenantId) {
        logger.debug("Getting queue statistics for tenant: {}", tenantId);

        try {
            Map<String, Object> stats = new HashMap<>();
            
            if (tenantId != null) {
                stats.put("totalMessages", queuedMessageRepository.countByTenantId(tenantId));
                stats.put("pendingMessages", queuedMessageRepository.countByTenantIdAndStatus(tenantId, QueuedMessage.Status.PENDING));
                stats.put("processingMessages", queuedMessageRepository.countByTenantIdAndStatus(tenantId, QueuedMessage.Status.PROCESSING));
                stats.put("processedMessages", queuedMessageRepository.countByTenantIdAndStatus(tenantId, QueuedMessage.Status.PROCESSED));
                stats.put("failedMessages", queuedMessageRepository.countByTenantIdAndStatus(tenantId, QueuedMessage.Status.FAILED));
                stats.put("expiredMessages", queuedMessageRepository.countByTenantIdAndStatus(tenantId, QueuedMessage.Status.EXPIRED));
                stats.put("cancelledMessages", queuedMessageRepository.countByTenantIdAndStatus(tenantId, QueuedMessage.Status.CANCELLED));
            } else {
                stats.put("totalMessages", queuedMessageRepository.count());
                stats.put("pendingMessages", queuedMessageRepository.countByStatus(QueuedMessage.Status.PENDING));
                stats.put("processingMessages", queuedMessageRepository.countByStatus(QueuedMessage.Status.PROCESSING));
                stats.put("processedMessages", queuedMessageRepository.countByStatus(QueuedMessage.Status.PROCESSED));
                stats.put("failedMessages", queuedMessageRepository.countByStatus(QueuedMessage.Status.FAILED));
                stats.put("expiredMessages", queuedMessageRepository.countByStatus(QueuedMessage.Status.EXPIRED));
                stats.put("cancelledMessages", queuedMessageRepository.countByStatus(QueuedMessage.Status.CANCELLED));
            }

            stats.put("timestamp", LocalDateTime.now().toString());
            return stats;

        } catch (Exception e) {
            logger.error("Error getting queue statistics for tenant: {}", tenantId, e);
            throw new RuntimeException("Failed to get queue statistics", e);
        }
    }

    // Private helper methods

    private Map<String, Object> executeQueuedCall(QueuedMessage message) {
        logger.debug("Executing queued call for message: {} to endpoint: {}", 
                    message.getMessageId(), message.getEndpointUrl());

        try {
            // Prepare HTTP headers
            HttpHeaders headers = new HttpHeaders();
            if (message.getHeaders() != null) {
                message.getHeaders().forEach(headers::set);
            }
            headers.set("Content-Type", "application/json");

            // Prepare HTTP entity
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(message.getPayload(), headers);

            // Execute the call with resiliency patterns
            return resiliencyConfigurationService.executeResilientCall(
                    message.getServiceName(),
                    message.getTenantId(),
                    () -> {
                        HttpMethod httpMethod = HttpMethod.valueOf(message.getHttpMethod());
                        ResponseEntity<Map> response = restTemplate.exchange(
                                message.getEndpointUrl(), httpMethod, entity, Map.class);
                        
                        if (response.getStatusCode().is2xxSuccessful()) {
                            return response.getBody() != null ? response.getBody() : new HashMap<>();
                        } else {
                            throw new RuntimeException("HTTP call failed with status: " + response.getStatusCode());
                        }
                    },
                    (exception) -> {
                        logger.warn("Queued call failed for message: {} using fallback", message.getMessageId());
                        Map<String, Object> fallbackResult = new HashMap<>();
                        fallbackResult.put("status", "FAILED");
                        fallbackResult.put("error", "Service unavailable: " + exception.getMessage());
                        fallbackResult.put("fallbackUsed", true);
                        fallbackResult.put("timestamp", LocalDateTime.now().toString());
                        return fallbackResult;
                    }
            );

        } catch (Exception e) {
            logger.error("Error executing queued call for message: {}", message.getMessageId(), e);
            throw new RuntimeException("Failed to execute queued call", e);
        }
    }

    private CompletableFuture<Void> retryMessage(QueuedMessage message) {
        return CompletableFuture.runAsync(() -> {
            try {
                logger.info("Retrying message: {} (attempt {})", message.getMessageId(), message.getRetryCount() + 1);

                // Update retry count and next retry time
                message.setRetryCount(message.getRetryCount() + 1);
                message.setNextRetryAt(calculateNextRetryTime(message.getRetryCount()));
                message.setStatus(QueuedMessage.Status.RETRY);
                message.setUpdatedAt(LocalDateTime.now());
                queuedMessageRepository.save(message);

                // Process the message
                processMessage(message.getMessageId()).join();

            } catch (Exception e) {
                logger.error("Retry failed for message: {}", message.getMessageId(), e);
                handleProcessingFailure(message.getMessageId(), e);
            }
        }, executorService);
    }

    private void handleProcessingFailure(String messageId, Exception exception) {
        try {
            Optional<QueuedMessage> messageOpt = queuedMessageRepository.findByMessageId(messageId);
            if (messageOpt.isPresent()) {
                QueuedMessage message = messageOpt.get();
                
                if (message.getRetryCount() < message.getMaxRetries()) {
                    // Schedule for retry
                    message.setStatus(QueuedMessage.Status.FAILED);
                    message.setNextRetryAt(calculateNextRetryTime(message.getRetryCount()));
                    message.setErrorMessage(exception.getMessage());
                    message.setErrorDetails(Map.of(
                            "exceptionType", exception.getClass().getSimpleName(),
                            "stackTrace", Arrays.toString(exception.getStackTrace())
                    ));
                } else {
                    // Max retries exceeded
                    message.setStatus(QueuedMessage.Status.FAILED);
                    message.setErrorMessage("Max retries exceeded: " + exception.getMessage());
                    message.setErrorDetails(Map.of(
                            "exceptionType", exception.getClass().getSimpleName(),
                            "stackTrace", Arrays.toString(exception.getStackTrace()),
                            "maxRetriesExceeded", true
                    ));
                }
                
                message.setUpdatedAt(LocalDateTime.now());
                queuedMessageRepository.save(message);
            }
        } catch (Exception e) {
            logger.error("Error handling processing failure for message: {}", messageId, e);
        }
    }

    private LocalDateTime calculateNextRetryTime(int retryCount) {
        // Exponential backoff: 1min, 2min, 4min, 8min, 16min, 32min
        long delayMinutes = (long) Math.pow(2, retryCount);
        return LocalDateTime.now().plusMinutes(delayMinutes);
    }
}