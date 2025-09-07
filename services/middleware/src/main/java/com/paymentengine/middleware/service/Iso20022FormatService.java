package com.paymentengine.middleware.service;

import com.paymentengine.middleware.dto.SchemeConfigRequest;

import java.util.Map;

/**
 * Service interface for ISO 20022 message format conversion and validation
 */
public interface Iso20022FormatService {
    
    /**
     * Convert message from one format to another
     */
    Object convertMessage(Object message, SchemeConfigRequest.MessageFormat fromFormat, 
                         SchemeConfigRequest.MessageFormat toFormat, String messageType);
    
    /**
     * Serialize message to JSON
     */
    String serializeToJson(Object message, String messageType);
    
    /**
     * Serialize message to XML
     */
    String serializeToXml(Object message, String messageType);
    
    /**
     * Deserialize message from JSON
     */
    Object deserializeFromJson(String jsonMessage, String messageType);
    
    /**
     * Deserialize message from XML
     */
    Object deserializeFromXml(String xmlMessage, String messageType);
    
    /**
     * Validate message format
     */
    Map<String, Object> validateMessageFormat(Object message, SchemeConfigRequest.MessageFormat format, String messageType);
    
    /**
     * Get message schema for validation
     */
    String getMessageSchema(String messageType, SchemeConfigRequest.MessageFormat format);
    
    /**
     * Transform message between different ISO 20022 versions
     */
    Object transformMessageVersion(Object message, String fromVersion, String toVersion, String messageType);
    
    /**
     * Get supported message types
     */
    Map<String, Object> getSupportedMessageTypes();
    
    /**
     * Get message template
     */
    Object getMessageTemplate(String messageType, SchemeConfigRequest.MessageFormat format);
}