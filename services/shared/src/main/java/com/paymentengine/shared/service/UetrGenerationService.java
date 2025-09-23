package com.paymentengine.shared.service;

import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Map;
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
     * Get or generate UETR for a payment transaction
     * If externalUetr is provided and valid, use it; otherwise generate a new one
     * 
     * @param messageType The ISO 20022 message type
     * @param tenantId The tenant identifier
     * @param externalUetr Optional external UETR from client or clearing system
     * @return UETR (external if valid, or newly generated)
     */
    public String getOrGenerateUetr(String messageType, String tenantId, String externalUetr) {
        // If external UETR is provided and valid, use it
        if (externalUetr != null && !externalUetr.trim().isEmpty() && isValidUetr(externalUetr)) {
            logger.info("Using external UETR: {} for messageType: {}, tenantId: {}", 
                       externalUetr, messageType, tenantId);
            return externalUetr;
        }
        
        // Generate new UETR if external UETR is not provided or invalid
        if (externalUetr != null && !externalUetr.trim().isEmpty()) {
            logger.warn("External UETR provided but invalid: {} for messageType: {}, tenantId: {}. Generating new UETR.", 
                       externalUetr, messageType, tenantId);
        } else {
            logger.info("No external UETR provided for messageType: {}, tenantId: {}. Generating new UETR.", 
                       messageType, tenantId);
        }
        
        return generateUetr(messageType, tenantId);
    }

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
     * Extract UETR from ISO 20022 message
     * 
     * @param message The ISO 20022 message
     * @param messageType The message type (PAIN001, PACS008, etc.)
     * @return UETR if found, null otherwise
     */
    public String extractUetrFromMessage(Map<String, Object> message, String messageType) {
        try {
            if (message == null) {
                return null;
            }
            
            // Extract UETR based on message type
            switch (messageType.toUpperCase()) {
                case "PAIN001":
                    return extractUetrFromPain001(message);
                case "PACS008":
                    return extractUetrFromPacs008(message);
                case "PACS002":
                    return extractUetrFromPacs002(message);
                case "PAIN002":
                    return extractUetrFromPain002(message);
                case "CAMT054":
                    return extractUetrFromCamt054(message);
                case "CAMT055":
                    return extractUetrFromCamt055(message);
                default:
                    logger.warn("UETR extraction not implemented for message type: {}", messageType);
                    return null;
            }
        } catch (Exception e) {
            logger.error("Error extracting UETR from message type: {}", messageType, e);
            return null;
        }
    }

    /**
     * Extract UETR from PAIN.001 message
     */
    private String extractUetrFromPain001(Map<String, Object> message) {
        try {
            Map<String, Object> cstmrCdtTrfInitn = (Map<String, Object>) message.get("CstmrCdtTrfInitn");
            if (cstmrCdtTrfInitn == null) return null;
            
            List<Map<String, Object>> pmtInfList = (List<Map<String, Object>>) cstmrCdtTrfInitn.get("PmtInf");
            if (pmtInfList == null || pmtInfList.isEmpty()) return null;
            
            Map<String, Object> pmtInf = pmtInfList.get(0);
            List<Map<String, Object>> cdtTrfTxInfList = (List<Map<String, Object>>) pmtInf.get("CdtTrfTxInf");
            if (cdtTrfTxInfList == null || cdtTrfTxInfList.isEmpty()) return null;
            
            Map<String, Object> cdtTrfTxInf = cdtTrfTxInfList.get(0);
            Map<String, Object> pmtId = (Map<String, Object>) cdtTrfTxInf.get("PmtId");
            if (pmtId == null) return null;
            
            return (String) pmtId.get("UETR");
        } catch (Exception e) {
            logger.error("Error extracting UETR from PAIN.001 message", e);
            return null;
        }
    }

    /**
     * Extract UETR from PACS.008 message
     */
    private String extractUetrFromPacs008(Map<String, Object> message) {
        try {
            Map<String, Object> fiToFICustomerCreditTransfer = (Map<String, Object>) message.get("FIToFICstmrCdtTrf");
            if (fiToFICustomerCreditTransfer == null) return null;
            
            List<Map<String, Object>> cdtTrfTxInfList = (List<Map<String, Object>>) fiToFICustomerCreditTransfer.get("CdtTrfTxInf");
            if (cdtTrfTxInfList == null || cdtTrfTxInfList.isEmpty()) return null;
            
            Map<String, Object> cdtTrfTxInf = cdtTrfTxInfList.get(0);
            Map<String, Object> pmtId = (Map<String, Object>) cdtTrfTxInf.get("PmtId");
            if (pmtId == null) return null;
            
            return (String) pmtId.get("UETR");
        } catch (Exception e) {
            logger.error("Error extracting UETR from PACS.008 message", e);
            return null;
        }
    }

    /**
     * Extract UETR from PACS.002 message
     */
    private String extractUetrFromPacs002(Map<String, Object> message) {
        try {
            Map<String, Object> document = (Map<String, Object>) message.get("Document");
            if (document == null) return null;
            
            Map<String, Object> fiToFIPmtStsRpt = (Map<String, Object>) document.get("FIToFIPmtStsRpt");
            if (fiToFIPmtStsRpt == null) return null;
            
            List<Map<String, Object>> txInfAndStsList = (List<Map<String, Object>>) fiToFIPmtStsRpt.get("TxInfAndSts");
            if (txInfAndStsList == null || txInfAndStsList.isEmpty()) return null;
            
            Map<String, Object> txInfAndSts = txInfAndStsList.get(0);
            Map<String, Object> orgnlTxId = (Map<String, Object>) txInfAndSts.get("OrgnlTxId");
            if (orgnlTxId == null) return null;
            
            return (String) orgnlTxId.get("OrgnlUETR");
        } catch (Exception e) {
            logger.error("Error extracting UETR from PACS.002 message", e);
            return null;
        }
    }

    /**
     * Extract UETR from PAIN.002 message
     */
    private String extractUetrFromPain002(Map<String, Object> message) {
        try {
            Map<String, Object> cstmrPmtStsRpt = (Map<String, Object>) message.get("CstmrPmtStsRpt");
            if (cstmrPmtStsRpt == null) return null;
            
            List<Map<String, Object>> orgnlPmtInfAndStsList = (List<Map<String, Object>>) cstmrPmtStsRpt.get("OrgnlPmtInfAndSts");
            if (orgnlPmtInfAndStsList == null || orgnlPmtInfAndStsList.isEmpty()) return null;
            
            Map<String, Object> orgnlPmtInfAndSts = orgnlPmtInfAndStsList.get(0);
            List<Map<String, Object>> txInfAndStsList = (List<Map<String, Object>>) orgnlPmtInfAndSts.get("TxInfAndSts");
            if (txInfAndStsList == null || txInfAndStsList.isEmpty()) return null;
            
            Map<String, Object> txInfAndSts = txInfAndStsList.get(0);
            Map<String, Object> orgnlTxId = (Map<String, Object>) txInfAndSts.get("OrgnlTxId");
            if (orgnlTxId == null) return null;
            
            return (String) orgnlTxId.get("OrgnlUETR");
        } catch (Exception e) {
            logger.error("Error extracting UETR from PAIN.002 message", e);
            return null;
        }
    }

    /**
     * Extract UETR from CAMT.054 message
     */
    private String extractUetrFromCamt054(Map<String, Object> message) {
        try {
            Map<String, Object> document = (Map<String, Object>) message.get("Document");
            if (document == null) return null;
            
            Map<String, Object> bkTpCstmrDbtCdtNtfctn = (Map<String, Object>) document.get("BkTpCstmrDbtCdtNtfctn");
            if (bkTpCstmrDbtCdtNtfctn == null) return null;
            
            List<Map<String, Object>> ntfctnList = (List<Map<String, Object>>) bkTpCstmrDbtCdtNtfctn.get("Ntfctn");
            if (ntfctnList == null || ntfctnList.isEmpty()) return null;
            
            Map<String, Object> ntfctn = ntfctnList.get(0);
            List<Map<String, Object>> ntryList = (List<Map<String, Object>>) ntfctn.get("Ntry");
            if (ntryList == null || ntryList.isEmpty()) return null;
            
            Map<String, Object> ntry = ntryList.get(0);
            List<Map<String, Object>> ntryDtlsList = (List<Map<String, Object>>) ntry.get("NtryDtls");
            if (ntryDtlsList == null || ntryDtlsList.isEmpty()) return null;
            
            Map<String, Object> ntryDtls = ntryDtlsList.get(0);
            List<Map<String, Object>> txDtlsList = (List<Map<String, Object>>) ntryDtls.get("TxDtls");
            if (txDtlsList == null || txDtlsList.isEmpty()) return null;
            
            Map<String, Object> txDtls = txDtlsList.get(0);
            Map<String, Object> refs = (Map<String, Object>) txDtls.get("Refs");
            if (refs == null) return null;
            
            return (String) refs.get("UETR");
        } catch (Exception e) {
            logger.error("Error extracting UETR from CAMT.054 message", e);
            return null;
        }
    }

    /**
     * Extract UETR from CAMT.055 message
     */
    private String extractUetrFromCamt055(Map<String, Object> message) {
        try {
            Map<String, Object> document = (Map<String, Object>) message.get("Document");
            if (document == null) return null;
            
            Map<String, Object> cstmrPmtCxlReq = (Map<String, Object>) document.get("CstmrPmtCxlReq");
            if (cstmrPmtCxlReq == null) return null;
            
            List<Map<String, Object>> pmtInfList = (List<Map<String, Object>>) cstmrPmtCxlReq.get("PmtInf");
            if (pmtInfList == null || pmtInfList.isEmpty()) return null;
            
            Map<String, Object> pmtInf = pmtInfList.get(0);
            List<Map<String, Object>> cxlTxInfList = (List<Map<String, Object>>) pmtInf.get("CxlTxInf");
            if (cxlTxInfList == null || cxlTxInfList.isEmpty()) return null;
            
            Map<String, Object> cxlTxInf = cxlTxInfList.get(0);
            Map<String, Object> orgnlTxId = (Map<String, Object>) cxlTxInf.get("OrgnlTxId");
            if (orgnlTxId == null) return null;
            
            return (String) orgnlTxId.get("OrgnlUETR");
        } catch (Exception e) {
            logger.error("Error extracting UETR from CAMT.055 message", e);
            return null;
        }
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