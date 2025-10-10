package com.paymentengine.shared.event;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.UUID;

/**
 * Base class for all payment-related events in the system.
 * Uses event sourcing pattern for audit trail and system state reconstruction.
 */
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "eventType"
)
@JsonSubTypes({
    @JsonSubTypes.Type(value = TransactionCreatedEvent.class, name = "TRANSACTION_CREATED"),
    @JsonSubTypes.Type(value = TransactionUpdatedEvent.class, name = "TRANSACTION_UPDATED"),
    @JsonSubTypes.Type(value = TransactionCompletedEvent.class, name = "TRANSACTION_COMPLETED"),
    @JsonSubTypes.Type(value = TransactionFailedEvent.class, name = "TRANSACTION_FAILED"),
    @JsonSubTypes.Type(value = AccountBalanceUpdatedEvent.class, name = "ACCOUNT_BALANCE_UPDATED"),
    @JsonSubTypes.Type(value = AccountCreatedEvent.class, name = "ACCOUNT_CREATED"),
    @JsonSubTypes.Type(value = CustomerCreatedEvent.class, name = "CUSTOMER_CREATED"),
    @JsonSubTypes.Type(value = PaymentNotificationEvent.class, name = "PAYMENT_NOTIFICATION"),
    @JsonSubTypes.Type(value = AuditEvent.class, name = "AUDIT_EVENT"),
    @JsonSubTypes.Type(value = ErrorEvent.class, name = "ERROR_EVENT")
})
public abstract class PaymentEvent {
    
    @NotNull
    private UUID eventId;
    
    @NotNull
    private Instant timestamp;
    
    @NotNull
    private String eventType;
    
    @NotNull
    private String source;
    
    private String correlationId;
    
    private String userId;
    
    private Integer eventVersion;

    protected PaymentEvent() {
        this.eventId = UUID.randomUUID();
        this.timestamp = Instant.now();
        this.eventVersion = 1;
    }

    protected PaymentEvent(String eventType, String source) {
        this();
        this.eventType = eventType;
        this.source = source;
    }

    protected PaymentEvent(String eventType, String source, String correlationId, String userId) {
        this(eventType, source);
        this.correlationId = correlationId;
        this.userId = userId;
    }

    // Getters and Setters
    public UUID getEventId() {
        return eventId;
    }

    public void setEventId(UUID eventId) {
        this.eventId = eventId;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Integer getEventVersion() {
        return eventVersion;
    }

    public void setEventVersion(Integer eventVersion) {
        this.eventVersion = eventVersion;
    }

    @Override
    public String toString() {
        return "PaymentEvent{" +
                "eventId=" + eventId +
                ", timestamp=" + timestamp +
                ", eventType='" + eventType + '\'' +
                ", source='" + source + '\'' +
                ", correlationId='" + correlationId + '\'' +
                ", userId='" + userId + '\'' +
                ", eventVersion=" + eventVersion +
                '}';
    }
}