package com.paymentengine.paymentprocessing.eventsourcing.domain;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

public abstract class DomainEvent {
    
    private UUID eventId;
    private String eventType;
    private String aggregateId;
    private int version;
    private LocalDateTime timestamp;
    private String userId;
    private String correlationId;
    private String causationId;
    private Map<String, Object> metadata;
    private String eventData;
    private String eventSchema;
    private String eventVersion;
    private boolean isEncrypted;
    private String checksum;
    
    public DomainEvent() {
        this.eventId = UUID.randomUUID();
        this.timestamp = LocalDateTime.now();
        this.version = 1;
        this.isEncrypted = false;
    }
    
    public DomainEvent(String aggregateId, String eventType) {
        this();
        this.aggregateId = aggregateId;
        this.eventType = eventType;
    }
    
    public DomainEvent(String aggregateId, String eventType, String eventData) {
        this(aggregateId, eventType);
        this.eventData = eventData;
    }
    
    // Getters and Setters
    public UUID getEventId() {
        return eventId;
    }
    
    public void setEventId(UUID eventId) {
        this.eventId = eventId;
    }
    
    public String getEventType() {
        return eventType;
    }
    
    public void setEventType(String eventType) {
        this.eventType = eventType;
    }
    
    public String getAggregateId() {
        return aggregateId;
    }
    
    public void setAggregateId(String aggregateId) {
        this.aggregateId = aggregateId;
    }
    
    public int getVersion() {
        return version;
    }
    
    public void setVersion(int version) {
        this.version = version;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public String getCorrelationId() {
        return correlationId;
    }
    
    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }
    
    public String getCausationId() {
        return causationId;
    }
    
    public void setCausationId(String causationId) {
        this.causationId = causationId;
    }
    
    public Map<String, Object> getMetadata() {
        return metadata;
    }
    
    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }
    
    public String getEventData() {
        return eventData;
    }
    
    public void setEventData(String eventData) {
        this.eventData = eventData;
    }
    
    public String getEventSchema() {
        return eventSchema;
    }
    
    public void setEventSchema(String eventSchema) {
        this.eventSchema = eventSchema;
    }
    
    public String getEventVersion() {
        return eventVersion;
    }
    
    public void setEventVersion(String eventVersion) {
        this.eventVersion = eventVersion;
    }
    
    public boolean isEncrypted() {
        return isEncrypted;
    }
    
    public void setEncrypted(boolean encrypted) {
        isEncrypted = encrypted;
    }
    
    public String getChecksum() {
        return checksum;
    }
    
    public void setChecksum(String checksum) {
        this.checksum = checksum;
    }
    
    // Abstract methods
    public abstract String getAggregateType();
    
    public abstract void validate();
    
    // Utility methods
    public void addMetadata(String key, Object value) {
        if (this.metadata == null) {
            this.metadata = new java.util.HashMap<>();
        }
        this.metadata.put(key, value);
    }
    
    public Object getMetadata(String key) {
        return this.metadata != null ? this.metadata.get(key) : null;
    }
    
    public void removeMetadata(String key) {
        if (this.metadata != null) {
            this.metadata.remove(key);
        }
    }
    
    public boolean hasMetadata(String key) {
        return this.metadata != null && this.metadata.containsKey(key);
    }
    
    public void incrementVersion() {
        this.version++;
    }
    
    // setCorrelationId and setCausationId already defined above
    
    @Override
    public String toString() {
        return "DomainEvent{" +
                "eventId=" + eventId +
                ", eventType='" + eventType + '\'' +
                ", aggregateId='" + aggregateId + '\'' +
                ", version=" + version +
                ", timestamp=" + timestamp +
                ", userId='" + userId + '\'' +
                ", correlationId='" + correlationId + '\'' +
                ", causationId='" + causationId + '\'' +
                ", isEncrypted=" + isEncrypted +
                '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        DomainEvent that = (DomainEvent) o;
        
        return eventId != null ? eventId.equals(that.eventId) : that.eventId == null;
    }
    
    @Override
    public int hashCode() {
        return eventId != null ? eventId.hashCode() : 0;
    }
}