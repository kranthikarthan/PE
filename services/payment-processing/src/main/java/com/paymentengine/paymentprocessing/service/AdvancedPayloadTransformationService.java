package com.paymentengine.paymentprocessing.service;

import com.paymentengine.paymentprocessing.entity.AdvancedPayloadMapping;
import com.paymentengine.paymentprocessing.repository.AdvancedPayloadMappingRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.cache.annotation.Cacheable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Advanced Payload Transformation Service
 * 
 * Provides flexible payload transformation with support for static values, derived values,
 * conditional logic, and auto-generated IDs, configurable per tenant, payment type,
 * local instrumentation code, and clearing system.
 */
@Service
public class AdvancedPayloadTransformationService {
    
    private static final Logger logger = LoggerFactory.getLogger(AdvancedPayloadTransformationService.class);
    
    @Autowired
    private AdvancedPayloadMappingRepository advancedPayloadMappingRepository;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    /**
     * Transform payload with advanced mapping capabilities
     */
    @Cacheable(value = "advanced-payload-transformations", key = "#tenantId + '_' + #paymentType + '_' + #localInstrumentationCode + '_' + #clearingSystemCode + '_' + #direction")
    public Optional<Map<String, Object>> transformPayload(
            String tenantId,
            String paymentType,
            String localInstrumentationCode,
            String clearingSystemCode,
            AdvancedPayloadMapping.Direction direction,
            Map<String, Object> sourcePayload) {
        
        logger.debug("Transforming payload for tenant: {}, paymentType: {}, localInstrument: {}, clearingSystem: {}, direction: {}", 
                    tenantId, paymentType, localInstrumentationCode, clearingSystemCode, direction);
        
        try {
            // Find applicable mappings
            List<AdvancedPayloadMapping> mappings = findApplicableMappings(
                tenantId, paymentType, localInstrumentationCode, clearingSystemCode, direction);
            
            if (mappings.isEmpty()) {
                logger.warn("No applicable mappings found for tenant: {}, paymentType: {}, localInstrument: {}, clearingSystem: {}, direction: {}", 
                           tenantId, paymentType, localInstrumentationCode, clearingSystemCode, direction);
                return Optional.empty();
            }
            
            // Apply mappings in priority order
            Map<String, Object> transformedPayload = new HashMap<>();
            
            for (AdvancedPayloadMapping mapping : mappings) {
                transformedPayload = applyAdvancedMapping(sourcePayload, transformedPayload, mapping);
            }
            
            logger.debug("Successfully transformed payload for tenant: {}, paymentType: {}, localInstrument: {}, clearingSystem: {}, direction: {}", 
                        tenantId, paymentType, localInstrumentationCode, clearingSystemCode, direction);
            
            return Optional.of(transformedPayload);
            
        } catch (Exception e) {
            logger.error("Error transforming payload for tenant: {}, paymentType: {}, localInstrument: {}, clearingSystem: {}, direction: {}: {}", 
                        tenantId, paymentType, localInstrumentationCode, clearingSystemCode, direction, e.getMessage());
            return Optional.empty();
        }
    }
    
    /**
     * Find applicable mappings based on criteria
     */
    private List<AdvancedPayloadMapping> findApplicableMappings(
            String tenantId,
            String paymentType,
            String localInstrumentationCode,
            String clearingSystemCode,
            AdvancedPayloadMapping.Direction direction) {
        
        List<AdvancedPayloadMapping> allMappings = advancedPayloadMappingRepository.findActiveByTenantId(tenantId);
        
        return allMappings.stream()
            .filter(mapping -> mapping.matchesCriteria(tenantId, paymentType, localInstrumentationCode, clearingSystemCode))
            .filter(mapping -> mapping.isApplicableForDirection(direction))
            .sorted(Comparator.comparing(AdvancedPayloadMapping::getPriority))
            .collect(Collectors.toList());
    }
    
    /**
     * Apply advanced mapping to payload
     */
    private Map<String, Object> applyAdvancedMapping(
            Map<String, Object> sourcePayload,
            Map<String, Object> targetPayload,
            AdvancedPayloadMapping mapping) {
        
        Map<String, Object> result = new HashMap<>(targetPayload);
        
        // Apply field mappings
        if (mapping.getFieldMappings() != null) {
            result = applyFieldMappings(sourcePayload, result, mapping.getFieldMappings());
        }
        
        // Apply value assignments
        if (mapping.getValueAssignments() != null) {
            result = applyValueAssignments(result, mapping.getValueAssignments());
        }
        
        // Apply derived value rules
        if (mapping.getDerivedValueRules() != null) {
            result = applyDerivedValueRules(sourcePayload, result, mapping.getDerivedValueRules());
        }
        
        // Apply auto generation rules
        if (mapping.getAutoGenerationRules() != null) {
            result = applyAutoGenerationRules(result, mapping.getAutoGenerationRules());
        }
        
        // Apply conditional mappings
        if (mapping.getConditionalMappings() != null) {
            result = applyConditionalMappings(sourcePayload, result, mapping.getConditionalMappings());
        }
        
        // Apply transformation rules
        if (mapping.getTransformationRules() != null) {
            result = applyTransformationRules(result, mapping.getTransformationRules());
        }
        
        // Apply default values
        if (mapping.getDefaultValues() != null) {
            result = applyDefaultValues(result, mapping.getDefaultValues());
        }
        
        return result;
    }
    
    /**
     * Apply field mappings
     */
    private Map<String, Object> applyFieldMappings(
            Map<String, Object> sourcePayload,
            Map<String, Object> targetPayload,
            Map<String, Object> fieldMappings) {
        
        Map<String, Object> result = new HashMap<>(targetPayload);
        
        for (Map.Entry<String, Object> entry : fieldMappings.entrySet()) {
            String targetField = entry.getKey();
            Object mappingConfig = entry.getValue();
            
            Object value = resolveFieldValue(sourcePayload, mappingConfig);
            if (value != null) {
                setNestedValue(result, targetField, value);
            }
        }
        
        return result;
    }
    
    /**
     * Apply value assignments (static values)
     */
    private Map<String, Object> applyValueAssignments(
            Map<String, Object> targetPayload,
            Map<String, Object> valueAssignments) {
        
        Map<String, Object> result = new HashMap<>(targetPayload);
        
        for (Map.Entry<String, Object> entry : valueAssignments.entrySet()) {
            String fieldPath = entry.getKey();
            Object value = entry.getValue();
            
            setNestedValue(result, fieldPath, value);
        }
        
        return result;
    }
    
    /**
     * Apply derived value rules
     */
    private Map<String, Object> applyDerivedValueRules(
            Map<String, Object> sourcePayload,
            Map<String, Object> targetPayload,
            Map<String, Object> derivedValueRules) {
        
        Map<String, Object> result = new HashMap<>(targetPayload);
        
        for (Map.Entry<String, Object> entry : derivedValueRules.entrySet()) {
            String fieldPath = entry.getKey();
            Object ruleConfig = entry.getValue();
            
            Object derivedValue = calculateDerivedValue(sourcePayload, targetPayload, ruleConfig);
            if (derivedValue != null) {
                setNestedValue(result, fieldPath, derivedValue);
            }
        }
        
        return result;
    }
    
    /**
     * Apply auto generation rules
     */
    private Map<String, Object> applyAutoGenerationRules(
            Map<String, Object> targetPayload,
            Map<String, Object> autoGenerationRules) {
        
        Map<String, Object> result = new HashMap<>(targetPayload);
        
        for (Map.Entry<String, Object> entry : autoGenerationRules.entrySet()) {
            String fieldPath = entry.getKey();
            Object ruleConfig = entry.getValue();
            
            Object generatedValue = generateValue(ruleConfig);
            if (generatedValue != null) {
                setNestedValue(result, fieldPath, generatedValue);
            }
        }
        
        return result;
    }
    
    /**
     * Apply conditional mappings
     */
    private Map<String, Object> applyConditionalMappings(
            Map<String, Object> sourcePayload,
            Map<String, Object> targetPayload,
            Map<String, Object> conditionalMappings) {
        
        Map<String, Object> result = new HashMap<>(targetPayload);
        
        for (Map.Entry<String, Object> entry : conditionalMappings.entrySet()) {
            String condition = entry.getKey();
            Object mappingConfig = entry.getValue();
            
            if (evaluateCondition(sourcePayload, targetPayload, condition)) {
                if (mappingConfig instanceof Map) {
                    Map<String, Object> config = (Map<String, Object>) mappingConfig;
                    String targetField = (String) config.get("target");
                    Object sourceField = config.get("source");
                    
                    if (targetField != null && sourceField != null) {
                        Object value = resolveFieldValue(sourcePayload, sourceField);
                        setNestedValue(result, targetField, value);
                    }
                }
            }
        }
        
        return result;
    }
    
    /**
     * Apply transformation rules
     */
    private Map<String, Object> applyTransformationRules(
            Map<String, Object> targetPayload,
            Map<String, Object> transformationRules) {
        
        Map<String, Object> result = new HashMap<>(targetPayload);
        
        for (Map.Entry<String, Object> entry : transformationRules.entrySet()) {
            String fieldPath = entry.getKey();
            Object transformation = entry.getValue();
            
            Object value = getNestedValue(result, fieldPath);
            if (value != null && transformation instanceof String) {
                Object transformedValue = applyTransformation(value, (String) transformation);
                setNestedValue(result, fieldPath, transformedValue);
            }
        }
        
        return result;
    }
    
    /**
     * Apply default values
     */
    private Map<String, Object> applyDefaultValues(
            Map<String, Object> targetPayload,
            Map<String, Object> defaultValues) {
        
        Map<String, Object> result = new HashMap<>(targetPayload);
        
        for (Map.Entry<String, Object> entry : defaultValues.entrySet()) {
            String fieldPath = entry.getKey();
            Object defaultValue = entry.getValue();
            
            if (getNestedValue(result, fieldPath) == null) {
                setNestedValue(result, fieldPath, defaultValue);
            }
        }
        
        return result;
    }
    
    /**
     * Resolve field value from source payload
     */
    private Object resolveFieldValue(Map<String, Object> sourcePayload, Object mappingConfig) {
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
     * Calculate derived value based on rules
     */
    private Object calculateDerivedValue(
            Map<String, Object> sourcePayload,
            Map<String, Object> targetPayload,
            Object ruleConfig) {
        
        if (ruleConfig instanceof Map) {
            Map<String, Object> config = (Map<String, Object>) ruleConfig;
            String expression = (String) config.get("expression");
            String type = (String) config.get("type");
            
            if (expression != null) {
                return evaluateExpression(sourcePayload, targetPayload, expression, type);
            }
        }
        
        return null;
    }
    
    /**
     * Generate value based on auto generation rules
     */
    private Object generateValue(Object ruleConfig) {
        if (ruleConfig instanceof Map) {
            Map<String, Object> config = (Map<String, Object>) ruleConfig;
            String type = (String) config.get("type");
            String format = (String) config.get("format");
            String prefix = (String) config.get("prefix");
            String suffix = (String) config.get("suffix");
            Integer length = (Integer) config.get("length");
            
            switch (type != null ? type.toUpperCase() : "UUID") {
                case "UUID":
                    return UUID.randomUUID().toString();
                case "TIMESTAMP":
                    return LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                case "RANDOM_STRING":
                    return generateRandomString(length != null ? length : 10);
                case "SEQUENTIAL":
                    return generateSequentialId(prefix, suffix, length);
                case "CUSTOM":
                    return generateCustomValue(config);
                default:
                    return UUID.randomUUID().toString();
            }
        }
        
        return UUID.randomUUID().toString();
    }
    
    /**
     * Evaluate expression for derived values
     */
    private Object evaluateExpression(
            Map<String, Object> sourcePayload,
            Map<String, Object> targetPayload,
            String expression,
            String type) {
        
        try {
            // Replace field references with actual values
            String processedExpression = processExpression(expression, sourcePayload, targetPayload);
            
            // Evaluate the expression based on type
            switch (type != null ? type.toUpperCase() : "STRING") {
                case "STRING":
                    return processedExpression;
                case "NUMBER":
                    return evaluateNumericExpression(processedExpression);
                case "BOOLEAN":
                    return evaluateBooleanExpression(processedExpression);
                case "DATE":
                    return evaluateDateExpression(processedExpression);
                default:
                    return processedExpression;
            }
        } catch (Exception e) {
            logger.error("Error evaluating expression: {}", expression, e);
            return null;
        }
    }
    
    /**
     * Process expression by replacing field references
     */
    private String processExpression(
            String expression,
            Map<String, Object> sourcePayload,
            Map<String, Object> targetPayload) {
        
        String result = expression;
        
        // Replace source field references: ${source.field}
        Pattern sourcePattern = Pattern.compile("\\$\\{source\\.([^}]+)\\}");
        Matcher sourceMatcher = sourcePattern.matcher(result);
        while (sourceMatcher.find()) {
            String fieldPath = sourceMatcher.group(1);
            Object value = getNestedValue(sourcePayload, fieldPath);
            String wholeMatch = sourceMatcher.group(0);
            result = result.replace(wholeMatch, value != null ? value.toString() : "");
        }
        
        // Replace target field references: ${target.field}
        Pattern targetPattern = Pattern.compile("\\$\\{target\\.([^}]+)\\}");
        Matcher targetMatcher = targetPattern.matcher(result);
        while (targetMatcher.find()) {
            String fieldPath = targetMatcher.group(1);
            Object value = getNestedValue(targetPayload, fieldPath);
            result = result.replace(targetMatcher.group(0), value != null ? value.toString() : "");
        }
        
        // Replace function calls: ${function()}
        Pattern functionPattern = Pattern.compile("\\$\\{([^}]+)\\(\\)\\}");
        Matcher functionMatcher = functionPattern.matcher(result);
        while (functionMatcher.find()) {
            String function = functionMatcher.group(1);
            String functionResult = executeFunction(function);
            result = result.replace(functionMatcher.group(0), functionResult);
        }
        
        return result;
    }
    
    /**
     * Evaluate condition
     */
    private boolean evaluateCondition(
            Map<String, Object> sourcePayload,
            Map<String, Object> targetPayload,
            String condition) {
        
        try {
            // Process the condition expression
            String processedCondition = processExpression(condition, sourcePayload, targetPayload);
            
            // Evaluate boolean conditions
            if (processedCondition.contains("==")) {
                String[] parts = processedCondition.split("==");
                if (parts.length == 2) {
                    String left = parts[0].trim().replaceAll("'", "").replaceAll("\"", "");
                    String right = parts[1].trim().replaceAll("'", "").replaceAll("\"", "");
                    return left.equals(right);
                }
            } else if (processedCondition.contains("!=")) {
                String[] parts = processedCondition.split("!=");
                if (parts.length == 2) {
                    String left = parts[0].trim().replaceAll("'", "").replaceAll("\"", "");
                    String right = parts[1].trim().replaceAll("'", "").replaceAll("\"", "");
                    return !left.equals(right);
                }
            } else if (processedCondition.contains(">")) {
                String[] parts = processedCondition.split(">");
                if (parts.length == 2) {
                    try {
                        double left = Double.parseDouble(parts[0].trim());
                        double right = Double.parseDouble(parts[1].trim());
                        return left > right;
                    } catch (NumberFormatException e) {
                        return false;
                    }
                }
            } else if (processedCondition.contains("<")) {
                String[] parts = processedCondition.split("<");
                if (parts.length == 2) {
                    try {
                        double left = Double.parseDouble(parts[0].trim());
                        double right = Double.parseDouble(parts[1].trim());
                        return left < right;
                    } catch (NumberFormatException e) {
                        return false;
                    }
                }
            }
            
            // Default to false for unrecognized conditions
            return false;
            
        } catch (Exception e) {
            logger.error("Error evaluating condition: {}", condition, e);
            return false;
        }
    }
    
    /**
     * Apply transformation to value
     */
    private Object applyTransformation(Object value, String transformation) {
        if (value == null) {
            return null;
        }
        
        switch (transformation.toLowerCase()) {
            case "uppercase":
                return value.toString().toUpperCase();
            case "lowercase":
                return value.toString().toLowerCase();
            case "trim":
                return value.toString().trim();
            case "date_format":
                return formatDate(value);
            case "number_format":
                return formatNumber(value);
            case "currency_format":
                return formatCurrency(value);
            case "mask":
                return maskValue(value);
            case "encrypt":
                return encryptValue(value);
            default:
                return value;
        }
    }
    
    /**
     * Execute function
     */
    private String executeFunction(String function) {
        switch (function.toLowerCase()) {
            case "uuid":
                return UUID.randomUUID().toString();
            case "timestamp":
                return LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            case "now":
                return LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            case "date":
                return LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
            case "time":
                return LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_TIME);
            default:
                return "";
        }
    }
    
    /**
     * Generate random string
     */
    private String generateRandomString(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        
        return sb.toString();
    }
    
    /**
     * Generate sequential ID
     */
    private String generateSequentialId(String prefix, String suffix, Integer length) {
        // This would typically use a sequence generator or counter
        long timestamp = System.currentTimeMillis();
        String id = String.valueOf(timestamp);
        
        if (length != null && id.length() > length) {
            id = id.substring(id.length() - length);
        }
        
        return (prefix != null ? prefix : "") + id + (suffix != null ? suffix : "");
    }
    
    /**
     * Generate custom value
     */
    private Object generateCustomValue(Map<String, Object> config) {
        // Custom value generation logic
        return config.get("value");
    }
    
    /**
     * Evaluate numeric expression
     */
    private Object evaluateNumericExpression(String expression) {
        try {
            // Simple numeric evaluation (can be enhanced with a proper expression evaluator)
            return new BigDecimal(expression);
        } catch (Exception e) {
            return 0;
        }
    }
    
    /**
     * Evaluate boolean expression
     */
    private Object evaluateBooleanExpression(String expression) {
        return Boolean.parseBoolean(expression);
    }
    
    /**
     * Evaluate date expression
     */
    private Object evaluateDateExpression(String expression) {
        try {
            return LocalDateTime.parse(expression);
        } catch (Exception e) {
            return LocalDateTime.now();
        }
    }
    
    /**
     * Format date
     */
    private Object formatDate(Object value) {
        // Date formatting logic
        return value;
    }
    
    /**
     * Format number
     */
    private Object formatNumber(Object value) {
        // Number formatting logic
        return value;
    }
    
    /**
     * Format currency
     */
    private Object formatCurrency(Object value) {
        // Currency formatting logic
        return value;
    }
    
    /**
     * Mask value
     */
    private Object maskValue(Object value) {
        // Value masking logic
        return value;
    }
    
    /**
     * Encrypt value
     */
    private Object encryptValue(Object value) {
        // Value encryption logic
        return value;
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
}