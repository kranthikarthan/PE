package com.paymentengine.middleware.service;

import com.paymentengine.middleware.entity.PayloadSchemaMapping;
import com.paymentengine.middleware.repository.PayloadSchemaMappingRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.cache.annotation.Cacheable;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Payload Transformation Service
 * 
 * This service provides dynamic payload transformation between internal data models
 * and external core banking system payloads based on configurable schema mappings.
 * It supports field mapping, data transformation, validation, and conditional logic.
 */
@Service
public class PayloadTransformationService {
    
    private static final Logger logger = LoggerFactory.getLogger(PayloadTransformationService.class);
    
    @Autowired
    private PayloadSchemaMappingRepository payloadSchemaMappingRepository;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    /**
     * Transform request payload to external format
     */
    @Cacheable(value = "payload-transformations", key = "#endpointConfigId + '_REQUEST_' + #mappingName")
    public Optional<Map<String, Object>> transformRequestPayload(UUID endpointConfigId, 
                                                               String mappingName, 
                                                               Map<String, Object> sourcePayload) {
        logger.debug("Transforming request payload for endpoint: {} and mapping: {}", endpointConfigId, mappingName);
        
        try {
            // Get payload schema mapping
            Optional<PayloadSchemaMapping> mapping = payloadSchemaMappingRepository
                .findActiveByEndpointConfigIdAndMappingName(endpointConfigId, mappingName);
            
            if (mapping.isEmpty()) {
                logger.warn("No payload schema mapping found for endpoint: {} and mapping: {}", endpointConfigId, mappingName);
                return Optional.empty();
            }
            
            PayloadSchemaMapping schemaMapping = mapping.get();
            
            // Check if mapping supports request direction
            if (schemaMapping.getDirection() != PayloadSchemaMapping.Direction.REQUEST && 
                schemaMapping.getDirection() != PayloadSchemaMapping.Direction.BIDIRECTIONAL) {
                logger.warn("Payload schema mapping does not support request direction for endpoint: {} and mapping: {}", 
                           endpointConfigId, mappingName);
                return Optional.empty();
            }
            
            // Transform payload
            Map<String, Object> transformedPayload = performTransformation(
                sourcePayload, 
                schemaMapping, 
                PayloadSchemaMapping.Direction.REQUEST
            );
            
            logger.debug("Successfully transformed request payload for endpoint: {} and mapping: {}", endpointConfigId, mappingName);
            return Optional.of(transformedPayload);
            
        } catch (Exception e) {
            logger.error("Error transforming request payload for endpoint: {} and mapping: {}: {}", 
                        endpointConfigId, mappingName, e.getMessage());
            return Optional.empty();
        }
    }
    
    /**
     * Transform response payload from external format
     */
    @Cacheable(value = "payload-transformations", key = "#endpointConfigId + '_RESPONSE_' + #mappingName")
    public Optional<Map<String, Object>> transformResponsePayload(UUID endpointConfigId, 
                                                                String mappingName, 
                                                                Map<String, Object> sourcePayload) {
        logger.debug("Transforming response payload for endpoint: {} and mapping: {}", endpointConfigId, mappingName);
        
        try {
            // Get payload schema mapping
            Optional<PayloadSchemaMapping> mapping = payloadSchemaMappingRepository
                .findActiveByEndpointConfigIdAndMappingName(endpointConfigId, mappingName);
            
            if (mapping.isEmpty()) {
                logger.warn("No payload schema mapping found for endpoint: {} and mapping: {}", endpointConfigId, mappingName);
                return Optional.empty();
            }
            
            PayloadSchemaMapping schemaMapping = mapping.get();
            
            // Check if mapping supports response direction
            if (schemaMapping.getDirection() != PayloadSchemaMapping.Direction.RESPONSE && 
                schemaMapping.getDirection() != PayloadSchemaMapping.Direction.BIDIRECTIONAL) {
                logger.warn("Payload schema mapping does not support response direction for endpoint: {} and mapping: {}", 
                           endpointConfigId, mappingName);
                return Optional.empty();
            }
            
            // Transform payload
            Map<String, Object> transformedPayload = performTransformation(
                sourcePayload, 
                schemaMapping, 
                PayloadSchemaMapping.Direction.RESPONSE
            );
            
            logger.debug("Successfully transformed response payload for endpoint: {} and mapping: {}", endpointConfigId, mappingName);
            return Optional.of(transformedPayload);
            
        } catch (Exception e) {
            logger.error("Error transforming response payload for endpoint: {} and mapping: {}: {}", 
                        endpointConfigId, mappingName, e.getMessage());
            return Optional.empty();
        }
    }
    
    /**
     * Get all available mappings for an endpoint
     */
    public List<PayloadSchemaMapping> getAvailableMappings(UUID endpointConfigId) {
        return payloadSchemaMappingRepository.findActiveByEndpointConfigId(endpointConfigId);
    }
    
    /**
     * Get mappings by direction for an endpoint
     */
    public List<PayloadSchemaMapping> getMappingsByDirection(UUID endpointConfigId, PayloadSchemaMapping.Direction direction) {
        return payloadSchemaMappingRepository.findActiveByEndpointConfigIdAndDirection(endpointConfigId, direction);
    }
    
    /**
     * Validate payload against schema mapping
     */
    public ValidationResult validatePayload(UUID endpointConfigId, 
                                          String mappingName, 
                                          Map<String, Object> payload, 
                                          PayloadSchemaMapping.Direction direction) {
        logger.debug("Validating payload for endpoint: {} and mapping: {}", endpointConfigId, mappingName);
        
        try {
            // Get payload schema mapping
            Optional<PayloadSchemaMapping> mapping = payloadSchemaMappingRepository
                .findActiveByEndpointConfigIdAndMappingName(endpointConfigId, mappingName);
            
            if (mapping.isEmpty()) {
                return new ValidationResult(false, "No payload schema mapping found");
            }
            
            PayloadSchemaMapping schemaMapping = mapping.get();
            
            // Check direction support
            if (schemaMapping.getDirection() != direction && 
                schemaMapping.getDirection() != PayloadSchemaMapping.Direction.BIDIRECTIONAL) {
                return new ValidationResult(false, "Mapping does not support " + direction + " direction");
            }
            
            // Perform validation
            return performValidation(payload, schemaMapping, direction);
            
        } catch (Exception e) {
            logger.error("Error validating payload for endpoint: {} and mapping: {}: {}", 
                        endpointConfigId, mappingName, e.getMessage());
            return new ValidationResult(false, "Validation error: " + e.getMessage());
        }
    }
    
    /**
     * Perform the actual transformation
     */
    private Map<String, Object> performTransformation(Map<String, Object> sourcePayload, 
                                                     PayloadSchemaMapping schemaMapping, 
                                                     PayloadSchemaMapping.Direction direction) {
        Map<String, Object> transformedPayload = new HashMap<>();
        
        // Get field mappings
        Map<String, Object> fieldMappings = schemaMapping.getFieldMappings();
        if (fieldMappings == null) {
            fieldMappings = new HashMap<>();
        }
        
        // Apply field mappings
        for (Map.Entry<String, Object> entry : fieldMappings.entrySet()) {
            String targetField = entry.getKey();
            Object mappingConfig = entry.getValue();
            
            Object transformedValue = applyFieldMapping(sourcePayload, mappingConfig, schemaMapping);
            if (transformedValue != null) {
                setNestedValue(transformedPayload, targetField, transformedValue);
            }
        }
        
        // Apply default values
        Map<String, Object> defaultValues = schemaMapping.getDefaultValues();
        if (defaultValues != null) {
            for (Map.Entry<String, Object> entry : defaultValues.entrySet()) {
                String fieldPath = entry.getKey();
                Object defaultValue = entry.getValue();
                
                if (getNestedValue(transformedPayload, fieldPath) == null) {
                    setNestedValue(transformedPayload, fieldPath, defaultValue);
                }
            }
        }
        
        // Apply conditional mappings
        Map<String, Object> conditionalMappings = schemaMapping.getConditionalMappings();
        if (conditionalMappings != null) {
            applyConditionalMappings(transformedPayload, sourcePayload, conditionalMappings, schemaMapping);
        }
        
        // Apply transformations
        Map<String, Object> transformationRules = schemaMapping.getTransformationRules();
        if (transformationRules != null) {
            applyTransformationRules(transformedPayload, transformationRules);
        }
        
        return transformedPayload;
    }
    
    /**
     * Apply field mapping
     */
    private Object applyFieldMapping(Map<String, Object> sourcePayload, 
                                   Object mappingConfig, 
                                   PayloadSchemaMapping schemaMapping) {
        if (mappingConfig instanceof String) {
            // Simple field mapping
            String sourceField = (String) mappingConfig;
            return getNestedValue(sourcePayload, sourceField);
        } else if (mappingConfig instanceof Map) {
            // Complex mapping configuration
            Map<String, Object> config = (Map<String, Object>) mappingConfig;
            String sourceField = (String) config.get("source");
            String transformation = (String) config.get("transformation");
            Object defaultValue = config.get("default");
            
            Object value = getNestedValue(sourcePayload, sourceField);
            
            if (value == null) {
                return defaultValue;
            }
            
            // Apply transformation if specified
            if (transformation != null) {
                value = applyTransformation(value, transformation);
            }
            
            return value;
        }
        
        return null;
    }
    
    /**
     * Apply conditional mappings
     */
    private void applyConditionalMappings(Map<String, Object> targetPayload, 
                                        Map<String, Object> sourcePayload, 
                                        Map<String, Object> conditionalMappings, 
                                        PayloadSchemaMapping schemaMapping) {
        for (Map.Entry<String, Object> entry : conditionalMappings.entrySet()) {
            String condition = entry.getKey();
            Object mappingConfig = entry.getValue();
            
            if (evaluateCondition(sourcePayload, condition)) {
                if (mappingConfig instanceof Map) {
                    Map<String, Object> config = (Map<String, Object>) mappingConfig;
                    String targetField = (String) config.get("target");
                    Object sourceField = config.get("source");
                    
                    if (targetField != null && sourceField != null) {
                        Object value = applyFieldMapping(sourcePayload, sourceField, schemaMapping);
                        setNestedValue(targetPayload, targetField, value);
                    }
                }
            }
        }
    }
    
    /**
     * Apply transformation rules
     */
    private void applyTransformationRules(Map<String, Object> payload, Map<String, Object> transformationRules) {
        for (Map.Entry<String, Object> entry : transformationRules.entrySet()) {
            String fieldPath = entry.getKey();
            Object transformation = entry.getValue();
            
            Object value = getNestedValue(payload, fieldPath);
            if (value != null && transformation instanceof String) {
                Object transformedValue = applyTransformation(value, (String) transformation);
                setNestedValue(payload, fieldPath, transformedValue);
            }
        }
    }
    
    /**
     * Apply individual transformation
     */
    private Object applyTransformation(Object value, String transformation) {
        switch (transformation.toLowerCase()) {
            case "uppercase":
                return value.toString().toUpperCase();
            case "lowercase":
                return value.toString().toLowerCase();
            case "trim":
                return value.toString().trim();
            case "date_format":
                // Implement date formatting logic
                return value;
            case "number_format":
                // Implement number formatting logic
                return value;
            case "currency_format":
                // Implement currency formatting logic
                return value;
            default:
                return value;
        }
    }
    
    /**
     * Evaluate condition
     */
    private boolean evaluateCondition(Map<String, Object> payload, String condition) {
        // Simple condition evaluation - can be enhanced with more complex logic
        if (condition.contains("==")) {
            String[] parts = condition.split("==");
            if (parts.length == 2) {
                String fieldPath = parts[0].trim();
                String expectedValue = parts[1].trim().replaceAll("'", "").replaceAll("\"", "");
                Object actualValue = getNestedValue(payload, fieldPath);
                return expectedValue.equals(actualValue != null ? actualValue.toString() : null);
            }
        }
        return false;
    }
    
    /**
     * Perform validation
     */
    private ValidationResult performValidation(Map<String, Object> payload, 
                                             PayloadSchemaMapping schemaMapping, 
                                             PayloadSchemaMapping.Direction direction) {
        Map<String, Object> validationRules = schemaMapping.getValidationRules();
        if (validationRules == null) {
            return new ValidationResult(true, "No validation rules defined");
        }
        
        List<String> errors = new ArrayList<>();
        
        for (Map.Entry<String, Object> entry : validationRules.entrySet()) {
            String fieldPath = entry.getKey();
            Object rules = entry.getValue();
            
            Object value = getNestedValue(payload, fieldPath);
            
            if (rules instanceof Map) {
                Map<String, Object> ruleMap = (Map<String, Object>) rules;
                
                // Required validation
                Boolean required = (Boolean) ruleMap.get("required");
                if (required != null && required && (value == null || value.toString().trim().isEmpty())) {
                    errors.add("Field " + fieldPath + " is required");
                }
                
                // Type validation
                String type = (String) ruleMap.get("type");
                if (type != null && value != null) {
                    if (!validateType(value, type)) {
                        errors.add("Field " + fieldPath + " must be of type " + type);
                    }
                }
                
                // Length validation
                Integer maxLength = (Integer) ruleMap.get("maxLength");
                if (maxLength != null && value != null && value.toString().length() > maxLength) {
                    errors.add("Field " + fieldPath + " exceeds maximum length of " + maxLength);
                }
                
                Integer minLength = (Integer) ruleMap.get("minLength");
                if (minLength != null && value != null && value.toString().length() < minLength) {
                    errors.add("Field " + fieldPath + " must be at least " + minLength + " characters long");
                }
                
                // Pattern validation
                String pattern = (String) ruleMap.get("pattern");
                if (pattern != null && value != null && !value.toString().matches(pattern)) {
                    errors.add("Field " + fieldPath + " does not match required pattern");
                }
            }
        }
        
        return new ValidationResult(errors.isEmpty(), errors.isEmpty() ? "Validation passed" : String.join(", ", errors));
    }
    
    /**
     * Validate type
     */
    private boolean validateType(Object value, String type) {
        switch (type.toLowerCase()) {
            case "string":
                return value instanceof String;
            case "number":
                return value instanceof Number;
            case "integer":
                return value instanceof Integer;
            case "boolean":
                return value instanceof Boolean;
            case "array":
                return value instanceof List || value instanceof ArrayNode;
            case "object":
                return value instanceof Map || value instanceof ObjectNode;
            default:
                return true;
        }
    }
    
    /**
     * Get nested value from map using dot notation
     */
    private Object getNestedValue(Map<String, Object> map, String path) {
        String[] keys = path.split("\\.");
        Object current = map;
        
        for (String key : keys) {
            if (current instanceof Map) {
                current = ((Map<String, Object>) current).get(key);
            } else {
                return null;
            }
        }
        
        return current;
    }
    
    /**
     * Set nested value in map using dot notation
     */
    private void setNestedValue(Map<String, Object> map, String path, Object value) {
        String[] keys = path.split("\\.");
        Map<String, Object> current = map;
        
        for (int i = 0; i < keys.length - 1; i++) {
            String key = keys[i];
            if (!current.containsKey(key) || !(current.get(key) instanceof Map)) {
                current.put(key, new HashMap<String, Object>());
            }
            current = (Map<String, Object>) current.get(key);
        }
        
        current.put(keys[keys.length - 1], value);
    }
    
    /**
     * Validation Result DTO
     */
    public static class ValidationResult {
        private boolean valid;
        private String message;
        
        public ValidationResult(boolean valid, String message) {
            this.valid = valid;
            this.message = message;
        }
        
        public boolean isValid() {
            return valid;
        }
        
        public String getMessage() {
            return message;
        }
    }
}