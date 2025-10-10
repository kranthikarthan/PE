package com.paymentengine.paymentprocessing.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Map;

/**
 * Request DTO for sending scheme messages
 */
public class SchemeMessageRequest {
    
    @NotBlank(message = "Message type is required")
    private String messageType;
    
    @NotBlank(message = "Message ID is required")
    private String messageId;
    
    @NotBlank(message = "Correlation ID is required")
    private String correlationId;
    
    @NotNull(message = "Format is required")
    private SchemeConfigRequest.MessageFormat format;
    
    @NotNull(message = "Interaction mode is required")
    private SchemeConfigRequest.InteractionMode interactionMode;
    
    @NotNull(message = "Payload is required")
    private Object payload;
    
    private Map<String, Object> metadata;
    
    // Constructors
    public SchemeMessageRequest() {}
    
    public SchemeMessageRequest(String messageType, String messageId, String correlationId,
                               SchemeConfigRequest.MessageFormat format,
                               SchemeConfigRequest.InteractionMode interactionMode,
                               Object payload, Map<String, Object> metadata) {
        this.messageType = messageType;
        this.messageId = messageId;
        this.correlationId = correlationId;
        this.format = format;
        this.interactionMode = interactionMode;
        this.payload = payload;
        this.metadata = metadata;
    }
    
    // Getters and Setters
    public String getMessageType() {
        return messageType;
    }
    
    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }
    
    public String getMessageId() {
        return messageId;
    }
    
    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }
    
    public String getCorrelationId() {
        return correlationId;
    }
    
    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }
    
    public SchemeConfigRequest.MessageFormat getFormat() {
        return format;
    }
    
    public void setFormat(SchemeConfigRequest.MessageFormat format) {
        this.format = format;
    }
    
    public SchemeConfigRequest.InteractionMode getInteractionMode() {
        return interactionMode;
    }
    
    public void setInteractionMode(SchemeConfigRequest.InteractionMode interactionMode) {
        this.interactionMode = interactionMode;
    }
    
    public Object getPayload() {
        return payload;
    }
    
    public void setPayload(Object payload) {
        this.payload = payload;
    }
    
    public Map<String, Object> getMetadata() {
        return metadata;
    }
    
    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }
}