package com.paymentengine.shared.event;

import com.fasterxml.jackson.annotation.JsonTypeName;
import jakarta.validation.constraints.NotNull;

import java.util.Map;

@JsonTypeName("ERROR_EVENT")
public class ErrorEvent extends PaymentEvent {
    
    @NotNull
    private String component;
    
    @NotNull
    private String errorType;
    
    @NotNull
    private String errorMessage;
    
    private String stackTrace;
    
    private String resolutionAction;
    
    private Map<String, Object> metadata;
    
    private String severity; // CRITICAL, HIGH, MEDIUM, LOW
    
    private boolean isResolved;
    
    private String resolvedBy;

    public ErrorEvent() {
        super("ERROR_EVENT", "error-handler");
    }

    public ErrorEvent(String component, String errorType, String errorMessage) {
        this();
        this.component = component;
        this.errorType = errorType;
        this.errorMessage = errorMessage;
        this.severity = "MEDIUM";
        this.isResolved = false;
    }

    // Getters and Setters
    public String getComponent() {
        return component;
    }

    public void setComponent(String component) {
        this.component = component;
    }

    public String getErrorType() {
        return errorType;
    }

    public void setErrorType(String errorType) {
        this.errorType = errorType;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getStackTrace() {
        return stackTrace;
    }

    public void setStackTrace(String stackTrace) {
        this.stackTrace = stackTrace;
    }

    public String getResolutionAction() {
        return resolutionAction;
    }

    public void setResolutionAction(String resolutionAction) {
        this.resolutionAction = resolutionAction;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public boolean isResolved() {
        return isResolved;
    }

    public void setResolved(boolean resolved) {
        isResolved = resolved;
    }

    public String getResolvedBy() {
        return resolvedBy;
    }

    public void setResolvedBy(String resolvedBy) {
        this.resolvedBy = resolvedBy;
    }
}