package com.paymentengine.paymentprocessing.audit;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Service for comprehensive audit logging
 */
@Service
public class AuditLoggingService {

    private static final String AUDIT_TOPIC = "audit-logs";
    
    @Value("${app.audit.enabled:true}")
    private boolean auditEnabled;
    
    @Value("${app.audit.kafka.enabled:true}")
    private boolean kafkaAuditEnabled;
    
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public AuditLoggingService(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * Log authentication events
     */
    public void logAuthenticationEvent(String userId, String action, boolean success, 
                                     String ipAddress, String userAgent, Map<String, Object> metadata) {
        AuditEvent event = AuditEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType("AUTHENTICATION")
                .action(action)
                .userId(userId)
                .success(success)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .timestamp(Instant.now())
                .metadata(metadata)
                .build();
        
        logEvent(event);
    }

    /**
     * Log authorization events
     */
    public void logAuthorizationEvent(String userId, String resource, String action, 
                                    boolean success, String reason, Map<String, Object> metadata) {
        AuditEvent event = AuditEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType("AUTHORIZATION")
                .action(action)
                .userId(userId)
                .resource(resource)
                .success(success)
                .reason(reason)
                .timestamp(Instant.now())
                .metadata(metadata)
                .build();
        
        logEvent(event);
    }

    /**
     * Log message processing events
     */
    public void logMessageProcessingEvent(String messageId, String messageType, String tenantId,
                                        String action, boolean success, long processingTimeMs,
                                        Map<String, Object> metadata) {
        logMessageProcessingEvent(messageId, messageType, tenantId, action, success, processingTimeMs, metadata, null);
    }

    /**
     * Log message processing events with UETR
     */
    public void logMessageProcessingEvent(String messageId, String messageType, String tenantId,
                                        String action, boolean success, long processingTimeMs,
                                        Map<String, Object> metadata, String uetr) {
        AuditEvent event = AuditEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType("MESSAGE_PROCESSING")
                .action(action)
                .messageId(messageId)
                .messageType(messageType)
                .tenantId(tenantId)
                .success(success)
                .processingTimeMs(processingTimeMs)
                .uetr(uetr)
                .timestamp(Instant.now())
                .metadata(metadata)
                .build();
        
        logEvent(event);
    }

    /**
     * Log clearing system interaction events
     */
    public void logClearingSystemEvent(String clearingSystemId, String messageType, String action,
                                     boolean success, long responseTimeMs, int statusCode,
                                     Map<String, Object> metadata) {
        AuditEvent event = AuditEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType("CLEARING_SYSTEM")
                .action(action)
                .clearingSystemId(clearingSystemId)
                .messageType(messageType)
                .success(success)
                .responseTimeMs(responseTimeMs)
                .statusCode(statusCode)
                .timestamp(Instant.now())
                .metadata(metadata)
                .build();
        
        logEvent(event);
    }

    /**
     * Log configuration changes
     */
    public void logConfigurationChange(String userId, String resourceType, String resourceId,
                                     String action, Map<String, Object> oldValues,
                                     Map<String, Object> newValues, Map<String, Object> metadata) {
        AuditEvent event = AuditEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType("CONFIGURATION_CHANGE")
                .action(action)
                .userId(userId)
                .resourceType(resourceType)
                .resourceId(resourceId)
                .oldValues(oldValues)
                .newValues(newValues)
                .timestamp(Instant.now())
                .metadata(metadata)
                .build();
        
        logEvent(event);
    }

    /**
     * Log webhook delivery events
     */
    public void logWebhookDeliveryEvent(String webhookUrl, String messageId, String action,
                                      boolean success, long deliveryTimeMs, int statusCode,
                                      Map<String, Object> metadata) {
        AuditEvent event = AuditEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType("WEBHOOK_DELIVERY")
                .action(action)
                .webhookUrl(webhookUrl)
                .messageId(messageId)
                .success(success)
                .deliveryTimeMs(deliveryTimeMs)
                .statusCode(statusCode)
                .timestamp(Instant.now())
                .metadata(metadata)
                .build();
        
        logEvent(event);
    }

    /**
     * Log Kafka message events
     */
    public void logKafkaMessageEvent(String topic, String messageId, String action,
                                   boolean success, Map<String, Object> metadata) {
        AuditEvent event = AuditEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType("KAFKA_MESSAGE")
                .action(action)
                .topic(topic)
                .messageId(messageId)
                .success(success)
                .timestamp(Instant.now())
                .metadata(metadata)
                .build();
        
        logEvent(event);
    }

    /**
     * Log security events
     */
    public void logSecurityEvent(String eventType, String userId, String action, boolean success,
                               String ipAddress, String reason, Map<String, Object> metadata) {
        AuditEvent event = AuditEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType("SECURITY")
                .action(action)
                .userId(userId)
                .success(success)
                .ipAddress(ipAddress)
                .reason(reason)
                .timestamp(Instant.now())
                .metadata(metadata)
                .build();
        
        logEvent(event);
    }

    /**
     * Log system events
     */
    public void logSystemEvent(String eventType, String action, boolean success,
                             String component, String reason, Map<String, Object> metadata) {
        AuditEvent event = AuditEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType("SYSTEM")
                .action(action)
                .component(component)
                .success(success)
                .reason(reason)
                .timestamp(Instant.now())
                .metadata(metadata)
                .build();
        
        logEvent(event);
    }

    /**
     * Log UETR tracking events
     */
    public void logUetrTrackingEvent(String uetr, String messageType, String tenantId,
                                   String action, boolean success, String status,
                                   String processingSystem, Map<String, Object> metadata) {
        AuditEvent event = AuditEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType("UETR_TRACKING")
                .action(action)
                .uetr(uetr)
                .messageType(messageType)
                .tenantId(tenantId)
                .success(success)
                .component(processingSystem)
                .reason(status)
                .timestamp(Instant.now())
                .metadata(metadata)
                .build();
        
        logEvent(event);
    }

    /**
     * Log the audit event
     */
    private void logEvent(AuditEvent event) {
        if (!auditEnabled) {
            return;
        }

        try {
            // Log to console (for development)
            System.out.println("AUDIT: " + event.toString());
            
            // Send to Kafka if enabled
            if (kafkaAuditEnabled) {
                CompletableFuture.runAsync(() -> {
                    try {
                        kafkaTemplate.send(AUDIT_TOPIC, event.getEventId(), event);
                    } catch (Exception e) {
                        System.err.println("Failed to send audit event to Kafka: " + e.getMessage());
                    }
                });
            }
            
        } catch (Exception e) {
            System.err.println("Failed to log audit event: " + e.getMessage());
        }
    }

    /**
     * Audit event builder
     */
    public static class AuditEvent {
        private String eventId;
        private String eventType;
        private String action;
        private String userId;
        private String messageId;
        private String messageType;
        private String tenantId;
        private String uetr;
        private String clearingSystemId;
        private String resource;
        private String resourceType;
        private String resourceId;
        private String webhookUrl;
        private String topic;
        private String component;
        private String ipAddress;
        private String userAgent;
        private String reason;
        private boolean success;
        private long processingTimeMs;
        private long responseTimeMs;
        private long deliveryTimeMs;
        private int statusCode;
        private Map<String, Object> oldValues;
        private Map<String, Object> newValues;
        private Map<String, Object> metadata;
        private Instant timestamp;

        // Builder pattern
        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private AuditEvent event = new AuditEvent();

            public Builder eventId(String eventId) {
                event.eventId = eventId;
                return this;
            }

            public Builder eventType(String eventType) {
                event.eventType = eventType;
                return this;
            }

            public Builder action(String action) {
                event.action = action;
                return this;
            }

            public Builder userId(String userId) {
                event.userId = userId;
                return this;
            }

            public Builder messageId(String messageId) {
                event.messageId = messageId;
                return this;
            }

            public Builder messageType(String messageType) {
                event.messageType = messageType;
                return this;
            }

            public Builder tenantId(String tenantId) {
                event.tenantId = tenantId;
                return this;
            }

            public Builder uetr(String uetr) {
                event.uetr = uetr;
                return this;
            }

            public Builder clearingSystemId(String clearingSystemId) {
                event.clearingSystemId = clearingSystemId;
                return this;
            }

            public Builder resource(String resource) {
                event.resource = resource;
                return this;
            }

            public Builder resourceType(String resourceType) {
                event.resourceType = resourceType;
                return this;
            }

            public Builder resourceId(String resourceId) {
                event.resourceId = resourceId;
                return this;
            }

            public Builder webhookUrl(String webhookUrl) {
                event.webhookUrl = webhookUrl;
                return this;
            }

            public Builder topic(String topic) {
                event.topic = topic;
                return this;
            }

            public Builder component(String component) {
                event.component = component;
                return this;
            }

            public Builder ipAddress(String ipAddress) {
                event.ipAddress = ipAddress;
                return this;
            }

            public Builder userAgent(String userAgent) {
                event.userAgent = userAgent;
                return this;
            }

            public Builder reason(String reason) {
                event.reason = reason;
                return this;
            }

            public Builder success(boolean success) {
                event.success = success;
                return this;
            }

            public Builder processingTimeMs(long processingTimeMs) {
                event.processingTimeMs = processingTimeMs;
                return this;
            }

            public Builder responseTimeMs(long responseTimeMs) {
                event.responseTimeMs = responseTimeMs;
                return this;
            }

            public Builder deliveryTimeMs(long deliveryTimeMs) {
                event.deliveryTimeMs = deliveryTimeMs;
                return this;
            }

            public Builder statusCode(int statusCode) {
                event.statusCode = statusCode;
                return this;
            }

            public Builder oldValues(Map<String, Object> oldValues) {
                event.oldValues = oldValues;
                return this;
            }

            public Builder newValues(Map<String, Object> newValues) {
                event.newValues = newValues;
                return this;
            }

            public Builder metadata(Map<String, Object> metadata) {
                event.metadata = metadata;
                return this;
            }

            public Builder timestamp(Instant timestamp) {
                event.timestamp = timestamp;
                return this;
            }

            public AuditEvent build() {
                return event;
            }
        }

        // Getters
        public String getEventId() { return eventId; }
        public String getEventType() { return eventType; }
        public String getAction() { return action; }
        public String getUserId() { return userId; }
        public String getMessageId() { return messageId; }
        public String getMessageType() { return messageType; }
        public String getTenantId() { return tenantId; }
        public String getUetr() { return uetr; }
        public String getClearingSystemId() { return clearingSystemId; }
        public String getResource() { return resource; }
        public String getResourceType() { return resourceType; }
        public String getResourceId() { return resourceId; }
        public String getWebhookUrl() { return webhookUrl; }
        public String getTopic() { return topic; }
        public String getComponent() { return component; }
        public String getIpAddress() { return ipAddress; }
        public String getUserAgent() { return userAgent; }
        public String getReason() { return reason; }
        public boolean isSuccess() { return success; }
        public long getProcessingTimeMs() { return processingTimeMs; }
        public long getResponseTimeMs() { return responseTimeMs; }
        public long getDeliveryTimeMs() { return deliveryTimeMs; }
        public int getStatusCode() { return statusCode; }
        public Map<String, Object> getOldValues() { return oldValues; }
        public Map<String, Object> getNewValues() { return newValues; }
        public Map<String, Object> getMetadata() { return metadata; }
        public Instant getTimestamp() { return timestamp; }

        @Override
        public String toString() {
            return String.format("AuditEvent{eventId='%s', eventType='%s', action='%s', userId='%s', success=%s, timestamp=%s}",
                    eventId, eventType, action, userId, success, timestamp);
        }
    }
}