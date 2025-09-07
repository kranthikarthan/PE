package com.paymentengine.shared.service;

import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * Service for generating Unique End-to-End Transaction References (UETR)
 * 
 * UETR is a 36-character alphanumeric code that uniquely identifies
 * a payment transaction from initiation to completion across all
 * systems and participants in the payment chain.
 * 
 * Format: XXXXXXXX-XXXX-XXXX-XXXX-XXXXXXXXXXXX (36 characters)
 * - 8 characters: Timestamp-based prefix
 * - 4 characters: System identifier
 * - 4 characters: Message type identifier
 * - 4 characters: Sequence number
 * - 16 characters: UUID suffix
 */
@Service
public class UetrGenerationService {

    private static final Logger logger = LoggerFactory.getLogger(UetrGenerationService.class);
    
    private static final String SYSTEM_ID = "PE01"; // Payment Engine System ID
    private static final SecureRandom secureRandom = new SecureRandom();
    
    // Message type identifiers
    private static final String MSG_TYPE_PAIN001 = "P001";
    private static final String MSG_TYPE_PACS008 = "P008";
    private static final String MSG_TYPE_PACS002 = "P002";
    private static final String MSG_TYPE_PAIN002 = "P002";
    private static final String MSG_TYPE_CAMT054 = "C054";
    private static final String MSG_TYPE_CAMT055 = "C055";
    private static final String MSG_TYPE_CAMT056 = "C056";
    private static final String MSG_TYPE_CAMT029 = "C029";
    
    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");
    
    /**
     * Generate a UETR for a new payment transaction
     * 
     * @param messageType The ISO 20022 message type
     * @param tenantId The tenant identifier
     * @return Generated UETR
     */
    public String generateUetr(String messageType, String tenantId) {
        try {
            // Generate timestamp-based prefix (8 characters)
            String timestampPrefix = generateTimestampPrefix();
            
            // Get message type identifier (4 characters)
            String msgTypeId = getMessageTypeIdentifier(messageType);
            
            // Generate sequence number (4 characters)
            String sequenceNumber = generateSequenceNumber();
            
            // Generate UUID suffix (16 characters)
            String uuidSuffix = generateUuidSuffix();
            
            // Construct UETR
            String uetr = String.format("%s-%s-%s-%s-%s", 
                timestampPrefix, SYSTEM_ID, msgTypeId, sequenceNumber, uuidSuffix);
            
            logger.info("Generated UETR: {} for messageType: {}, tenantId: {}", 
                uetr, messageType, tenantId);
            
            return uetr;
            
        } catch (Exception e) {
            logger.error("Error generating UETR for messageType: {}, tenantId: {}", 
                messageType, tenantId, e);
            throw new RuntimeException("Failed to generate UETR", e);
        }
    }
    
    /**
     * Generate a UETR for a response message based on the original UETR
     * 
     * @param originalUetr The original UETR from the initiating message
     * @param responseMessageType The response message type
     * @return Generated UETR for the response
     */
    public String generateResponseUetr(String originalUetr, String responseMessageType) {
        try {
            // For response messages, we maintain the same timestamp and system ID
            // but change the message type and generate new sequence/UUID
            String[] parts = originalUetr.split("-");
            if (parts.length != 5) {
                throw new IllegalArgumentException("Invalid UETR format: " + originalUetr);
            }
            
            String timestampPrefix = parts[0];
            String systemId = parts[1];
            String msgTypeId = getMessageTypeIdentifier(responseMessageType);
            String sequenceNumber = generateSequenceNumber();
            String uuidSuffix = generateUuidSuffix();
            
            String responseUetr = String.format("%s-%s-%s-%s-%s", 
                timestampPrefix, systemId, msgTypeId, sequenceNumber, uuidSuffix);
            
            logger.info("Generated response UETR: {} for original UETR: {}, responseType: {}", 
                responseUetr, originalUetr, responseMessageType);
            
            return responseUetr;
            
        } catch (Exception e) {
            logger.error("Error generating response UETR for original: {}, responseType: {}", 
                originalUetr, responseMessageType, e);
            throw new RuntimeException("Failed to generate response UETR", e);
        }
    }
    
    /**
     * Validate UETR format
     * 
     * @param uetr The UETR to validate
     * @return true if valid, false otherwise
     */
    public boolean isValidUetr(String uetr) {
        if (uetr == null || uetr.length() != 36) {
            return false;
        }
        
        // Check format: XXXXXXXX-XXXX-XXXX-XXXX-XXXXXXXXXXXX
        String pattern = "^[A-Z0-9]{8}-[A-Z0-9]{4}-[A-Z0-9]{4}-[A-Z0-9]{4}-[A-Z0-9]{12}$";
        return uetr.matches(pattern);
    }
    
    /**
     * Extract timestamp from UETR
     * 
     * @param uetr The UETR
     * @return Timestamp prefix or null if invalid
     */
    public String extractTimestamp(String uetr) {
        if (!isValidUetr(uetr)) {
            return null;
        }
        return uetr.substring(0, 8);
    }
    
    /**
     * Extract system ID from UETR
     * 
     * @param uetr The UETR
     * @return System ID or null if invalid
     */
    public String extractSystemId(String uetr) {
        if (!isValidUetr(uetr)) {
            return null;
        }
        return uetr.substring(9, 13);
    }
    
    /**
     * Extract message type from UETR
     * 
     * @param uetr The UETR
     * @return Message type identifier or null if invalid
     */
    public String extractMessageType(String uetr) {
        if (!isValidUetr(uetr)) {
            return null;
        }
        return uetr.substring(14, 18);
    }
    
    /**
     * Check if two UETRs are related (same timestamp and system)
     * 
     * @param uetr1 First UETR
     * @param uetr2 Second UETR
     * @return true if related, false otherwise
     */
    public boolean areRelatedUetrs(String uetr1, String uetr2) {
        if (!isValidUetr(uetr1) || !isValidUetr(uetr2)) {
            return false;
        }
        
        String timestamp1 = extractTimestamp(uetr1);
        String systemId1 = extractSystemId(uetr1);
        String timestamp2 = extractTimestamp(uetr2);
        String systemId2 = extractSystemId(uetr2);
        
        return timestamp1 != null && systemId1 != null && 
               timestamp1.equals(timestamp2) && systemId1.equals(systemId2);
    }
    
    /**
     * Generate timestamp-based prefix (8 characters)
     * Format: YYYYMMDD
     */
    private String generateTimestampPrefix() {
        return Instant.now().atZone(java.time.ZoneOffset.UTC)
                .format(TIMESTAMP_FORMAT);
    }
    
    /**
     * Get message type identifier (4 characters)
     */
    private String getMessageTypeIdentifier(String messageType) {
        if (messageType == null) {
            return "UNKN";
        }
        
        return switch (messageType.toUpperCase()) {
            case "PAIN001" -> MSG_TYPE_PAIN001;
            case "PACS008" -> MSG_TYPE_PACS008;
            case "PACS002" -> MSG_TYPE_P002;
            case "PAIN002" -> MSG_TYPE_P002;
            case "CAMT054" -> MSG_TYPE_C054;
            case "CAMT055" -> MSG_TYPE_C055;
            case "CAMT056" -> MSG_TYPE_C056;
            case "CAMT029" -> MSG_TYPE_C029;
            default -> "UNKN";
        };
    }
    
    /**
     * Generate sequence number (4 characters)
     * Format: 4-digit hexadecimal number
     */
    private String generateSequenceNumber() {
        int sequence = secureRandom.nextInt(65536); // 0 to 65535
        return String.format("%04X", sequence);
    }
    
    /**
     * Generate UUID suffix (16 characters)
     * Format: 16-character alphanumeric string
     */
    private String generateUuidSuffix() {
        UUID uuid = UUID.randomUUID();
        return uuid.toString().replace("-", "").toUpperCase().substring(0, 16);
    }
}