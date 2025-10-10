package com.paymentengine.shared.event;

import com.fasterxml.jackson.annotation.JsonTypeName;
import jakarta.validation.constraints.NotNull;

import java.util.Map;
import java.util.UUID;

@JsonTypeName("AUDIT_EVENT")
public class AuditEvent extends PaymentEvent {
    
    @NotNull
    private String tableName;
    
    @NotNull
    private UUID recordId;
    
    @NotNull
    private String operation; // INSERT, UPDATE, DELETE
    
    private Map<String, Object> oldValues;
    
    private Map<String, Object> newValues;
    
    private String ipAddress;
    
    private String userAgent;
    
    private String sessionId;
    
    private String changedBy;

    public AuditEvent() {
        super("AUDIT_EVENT", "audit-service");
    }

    public AuditEvent(String tableName, UUID recordId, String operation) {
        this();
        this.tableName = tableName;
        this.recordId = recordId;
        this.operation = operation;
    }

    // Getters and Setters
    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public UUID getRecordId() {
        return recordId;
    }

    public void setRecordId(UUID recordId) {
        this.recordId = recordId;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public Map<String, Object> getOldValues() {
        return oldValues;
    }

    public void setOldValues(Map<String, Object> oldValues) {
        this.oldValues = oldValues;
    }

    public Map<String, Object> getNewValues() {
        return newValues;
    }

    public void setNewValues(Map<String, Object> newValues) {
        this.newValues = newValues;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getChangedBy() {
        return changedBy;
    }

    public void setChangedBy(String changedBy) {
        this.changedBy = changedBy;
    }
}