package com.paymentengine.corebanking.entity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * JPA converter for Map<String, Object> to JSON string conversion
 * Used for storing metadata as JSONB in PostgreSQL
 */
@Converter
public class MapToJsonConverter implements AttributeConverter<Map<String, Object>, String> {
    
    private static final Logger logger = LoggerFactory.getLogger(MapToJsonConverter.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    @Override
    public String convertToDatabaseColumn(Map<String, Object> attribute) {
        if (attribute == null) {
            return null;
        }
        
        try {
            return objectMapper.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            logger.error("Error converting map to JSON", e);
            return "{}";
        }
    }
    
    @Override
    public Map<String, Object> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.trim().isEmpty()) {
            return new HashMap<>();
        }
        
        try {
            return objectMapper.readValue(dbData, new TypeReference<Map<String, Object>>() {});
        } catch (JsonProcessingException e) {
            logger.error("Error converting JSON to map", e);
            return new HashMap<>();
        }
    }
}