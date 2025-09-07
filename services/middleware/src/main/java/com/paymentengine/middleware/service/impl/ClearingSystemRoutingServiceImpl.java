package com.paymentengine.middleware.service.impl;

import com.paymentengine.middleware.service.ClearingSystemRoutingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Implementation of clearing system routing service
 */
@Service
public class ClearingSystemRoutingServiceImpl implements ClearingSystemRoutingService {
    
    private static final Logger logger = LoggerFactory.getLogger(ClearingSystemRoutingServiceImpl.class);
    
    // In-memory storage for demo - in production, use database
    private final Map<String, ClearingSystemConfig> clearingSystems = new HashMap<>();
    private final Map<String, Map<String, String>> tenantClearingSystemMappings = new HashMap<>();
    private final Map<String, String> paymentTypeToClearingSystemMappings = new HashMap<>();
    private final Map<String, String> localInstrumentToClearingSystemMappings = new HashMap<>();
    
    public ClearingSystemRoutingServiceImpl() {
        initializeClearingSystems();
        initializeMappings();
    }
    
    @Override
    public ClearingSystemRoute determineClearingSystem(String tenantId, String paymentType, String localInstrumentCode) {
        logger.debug("Determining clearing system for tenant: {}, paymentType: {}, localInstrument: {}", 
                tenantId, paymentType, localInstrumentCode);
        
        try {
            // 1. Check tenant-specific clearing system mapping
            String clearingSystemCode = getTenantSpecificClearingSystem(tenantId, paymentType, localInstrumentCode);
            
            // 2. If no tenant-specific mapping, use payment type mapping
            if (clearingSystemCode == null) {
                clearingSystemCode = paymentTypeToClearingSystemMappings.get(paymentType);
            }
            
            // 3. If no payment type mapping, use local instrument mapping
            if (clearingSystemCode == null) {
                clearingSystemCode = localInstrumentToClearingSystemMappings.get(localInstrumentCode);
            }
            
            // 4. Default to primary clearing system if no mapping found
            if (clearingSystemCode == null) {
                clearingSystemCode = "FEDWIRE"; // Default clearing system
                logger.warn("No clearing system mapping found, using default: {}", clearingSystemCode);
            }
            
            // Get clearing system configuration
            ClearingSystemConfig config = clearingSystems.get(clearingSystemCode);
            if (config == null) {
                throw new IllegalArgumentException("Clearing system not found: " + clearingSystemCode);
            }
            
            // Create route
            ClearingSystemRoute route = new ClearingSystemRoute(
                    config.getCode(),
                    config.getName(),
                    getSchemeConfigurationId(clearingSystemCode, "pacs008"),
                    config.getEndpointUrl(),
                    "API_KEY", // Default authentication type
                    config.getAuthenticationConfig(),
                    config.isActive(),
                    "1" // Default priority
            );
            
            logger.info("Determined clearing system: {} for tenant: {}, paymentType: {}", 
                    clearingSystemCode, tenantId, paymentType);
            
            return route;
            
        } catch (Exception e) {
            logger.error("Error determining clearing system: {}", e.getMessage());
            throw new RuntimeException("Failed to determine clearing system", e);
        }
    }
    
    @Override
    public ClearingSystemConfig getClearingSystemConfig(String clearingSystemCode) {
        logger.debug("Getting clearing system config: {}", clearingSystemCode);
        
        ClearingSystemConfig config = clearingSystems.get(clearingSystemCode);
        if (config == null) {
            throw new IllegalArgumentException("Clearing system not found: " + clearingSystemCode);
        }
        
        return config;
    }
    
    @Override
    public Map<String, ClearingSystemConfig> getAvailableClearingSystems(String tenantId) {
        logger.debug("Getting available clearing systems for tenant: {}", tenantId);
        
        Map<String, ClearingSystemConfig> availableSystems = new HashMap<>();
        
        // Get tenant-specific mappings
        Map<String, String> tenantMappings = tenantClearingSystemMappings.get(tenantId);
        if (tenantMappings != null) {
            for (String clearingSystemCode : tenantMappings.values()) {
                ClearingSystemConfig config = clearingSystems.get(clearingSystemCode);
                if (config != null && config.isActive()) {
                    availableSystems.put(clearingSystemCode, config);
                }
            }
        }
        
        // If no tenant-specific systems, return all active systems
        if (availableSystems.isEmpty()) {
            clearingSystems.values().stream()
                    .filter(ClearingSystemConfig::isActive)
                    .forEach(config -> availableSystems.put(config.getCode(), config));
        }
        
        return availableSystems;
    }
    
    @Override
    public boolean validateClearingSystemAccess(String tenantId, String clearingSystemCode) {
        logger.debug("Validating clearing system access for tenant: {}, system: {}", tenantId, clearingSystemCode);
        
        // Check if clearing system exists and is active
        ClearingSystemConfig config = clearingSystems.get(clearingSystemCode);
        if (config == null || !config.isActive()) {
            return false;
        }
        
        // Check tenant-specific access
        Map<String, String> tenantMappings = tenantClearingSystemMappings.get(tenantId);
        if (tenantMappings != null) {
            return tenantMappings.containsValue(clearingSystemCode);
        }
        
        // Default: allow access to all active clearing systems
        return true;
    }
    
    @Override
    public String getSchemeConfigurationId(String clearingSystemCode, String messageType) {
        logger.debug("Getting scheme configuration ID for clearing system: {}, messageType: {}", 
                clearingSystemCode, messageType);
        
        // Generate scheme configuration ID based on clearing system and message type
        return String.format("scheme-%s-%s", clearingSystemCode.toLowerCase(), messageType.toLowerCase());
    }
    
    @Override
    public ClearingSystemRoute routeMessage(String tenantId, String paymentType, String localInstrumentCode, String messageType) {
        logger.info("Routing message for tenant: {}, paymentType: {}, localInstrument: {}, messageType: {}", 
                tenantId, paymentType, localInstrumentCode, messageType);
        
        // Determine clearing system
        ClearingSystemRoute route = determineClearingSystem(tenantId, paymentType, localInstrumentCode);
        
        // Update scheme configuration ID based on message type
        route.setSchemeConfigurationId(getSchemeConfigurationId(route.getClearingSystemCode(), messageType));
        
        return route;
    }
    
    private String getTenantSpecificClearingSystem(String tenantId, String paymentType, String localInstrumentCode) {
        Map<String, String> tenantMappings = tenantClearingSystemMappings.get(tenantId);
        if (tenantMappings == null) {
            return null;
        }
        
        // Check for payment type specific mapping
        String key = paymentType + ":" + localInstrumentCode;
        String clearingSystem = tenantMappings.get(key);
        
        if (clearingSystem == null) {
            // Check for payment type only
            clearingSystem = tenantMappings.get(paymentType);
        }
        
        if (clearingSystem == null) {
            // Check for local instrument only
            clearingSystem = tenantMappings.get(localInstrumentCode);
        }
        
        return clearingSystem;
    }
    
    private void initializeClearingSystems() {
        logger.info("Initializing clearing systems");
        
        // Fedwire (US)
        clearingSystems.put("FEDWIRE", new ClearingSystemConfig(
                "FEDWIRE",
                "Federal Reserve Wire Network",
                "US domestic wire transfer system",
                "US",
                "USD",
                true,
                Map.of("pacs008", "FI to FI Customer Credit Transfer", "pacs002", "FI to FI Payment Status Report"),
                Map.of("WIRE_DOMESTIC", "Domestic Wire Transfer", "WIRE_INTERNATIONAL", "International Wire Transfer"),
                Map.of("WIRE", "Wire Transfer", "FEDWIRE", "Fedwire Transfer"),
                "SYNCHRONOUS",
                30,
                "https://api.fedwire.com/v1/payments",
                Map.of("apiKey", "fedwire-api-key", "certificate", "fedwire-cert.pem")
        ));
        
        // CHAPS (UK)
        clearingSystems.put("CHAPS", new ClearingSystemConfig(
                "CHAPS",
                "Clearing House Automated Payment System",
                "UK same-day high-value payment system",
                "GB",
                "GBP",
                true,
                Map.of("pacs008", "FI to FI Customer Credit Transfer", "pacs002", "FI to FI Payment Status Report"),
                Map.of("WIRE_DOMESTIC", "Domestic Wire Transfer", "WIRE_INTERNATIONAL", "International Wire Transfer"),
                Map.of("WIRE", "Wire Transfer", "CHAPS", "CHAPS Transfer"),
                "SYNCHRONOUS",
                30,
                "https://api.chaps.co.uk/v1/payments",
                Map.of("apiKey", "chaps-api-key", "certificate", "chaps-cert.pem")
        ));
        
        // SEPA (Europe)
        clearingSystems.put("SEPA", new ClearingSystemConfig(
                "SEPA",
                "Single Euro Payments Area",
                "European payment system for euro transactions",
                "EU",
                "EUR",
                true,
                Map.of("pacs008", "FI to FI Customer Credit Transfer", "pacs002", "FI to FI Payment Status Report"),
                Map.of("SEPA_CREDIT", "SEPA Credit Transfer", "SEPA_INSTANT", "SEPA Instant Credit Transfer"),
                Map.of("SEPA", "SEPA Transfer", "INST", "Instant Transfer"),
                "ASYNCHRONOUS",
                60,
                "https://api.sepa.eu/v1/payments",
                Map.of("apiKey", "sepa-api-key", "certificate", "sepa-cert.pem")
        ));
        
        // ACH (US)
        clearingSystems.put("ACH", new ClearingSystemConfig(
                "ACH",
                "Automated Clearing House",
                "US batch payment system",
                "US",
                "USD",
                true,
                Map.of("pacs008", "FI to FI Customer Credit Transfer", "pacs002", "FI to FI Payment Status Report"),
                Map.of("ACH_CREDIT", "ACH Credit Transfer", "ACH_DEBIT", "ACH Debit Transfer"),
                Map.of("ACH", "ACH Transfer", "CCD", "Corporate Credit or Debit"),
                "BATCH",
                3600,
                "https://api.ach.com/v1/payments",
                Map.of("apiKey", "ach-api-key", "certificate", "ach-cert.pem")
        ));
        
        // RTP (US)
        clearingSystems.put("RTP", new ClearingSystemConfig(
                "RTP",
                "Real-Time Payments",
                "US real-time payment system",
                "US",
                "USD",
                true,
                Map.of("pacs008", "FI to FI Customer Credit Transfer", "pacs002", "FI to FI Payment Status Report"),
                Map.of("RTP", "Real-Time Payment"),
                Map.of("RTP", "Real-Time Payment", "INST", "Instant Payment"),
                "SYNCHRONOUS",
                10,
                "https://api.rtp.com/v1/payments",
                Map.of("apiKey", "rtp-api-key", "certificate", "rtp-cert.pem")
        ));
        
        logger.info("Initialized {} clearing systems", clearingSystems.size());
    }
    
    private void initializeMappings() {
        logger.info("Initializing clearing system mappings");
        
        // Payment type to clearing system mappings
        paymentTypeToClearingSystemMappings.put("WIRE_DOMESTIC", "FEDWIRE");
        paymentTypeToClearingSystemMappings.put("WIRE_INTERNATIONAL", "FEDWIRE");
        paymentTypeToClearingSystemMappings.put("ACH_CREDIT", "ACH");
        paymentTypeToClearingSystemMappings.put("ACH_DEBIT", "ACH");
        paymentTypeToClearingSystemMappings.put("RTP", "RTP");
        paymentTypeToClearingSystemMappings.put("SEPA_CREDIT", "SEPA");
        paymentTypeToClearingSystemMappings.put("SEPA_INSTANT", "SEPA");
        
        // Local instrument to clearing system mappings
        localInstrumentToClearingSystemMappings.put("WIRE", "FEDWIRE");
        localInstrumentToClearingSystemMappings.put("FEDWIRE", "FEDWIRE");
        localInstrumentToClearingSystemMappings.put("CHAPS", "CHAPS");
        localInstrumentToClearingSystemMappings.put("ACH", "ACH");
        localInstrumentToClearingSystemMappings.put("CCD", "ACH");
        localInstrumentToClearingSystemMappings.put("RTP", "RTP");
        localInstrumentToClearingSystemMappings.put("INST", "RTP");
        localInstrumentToClearingSystemMappings.put("SEPA", "SEPA");
        
        // Tenant-specific mappings
        tenantClearingSystemMappings.put("default", Map.of(
                "WIRE_DOMESTIC", "FEDWIRE",
                "ACH_CREDIT", "ACH",
                "RTP", "RTP"
        ));
        
        tenantClearingSystemMappings.put("demo-bank", Map.of(
                "WIRE_DOMESTIC", "FEDWIRE",
                "WIRE_INTERNATIONAL", "FEDWIRE",
                "ACH_CREDIT", "ACH",
                "ACH_DEBIT", "ACH",
                "RTP", "RTP",
                "SEPA_CREDIT", "SEPA"
        ));
        
        tenantClearingSystemMappings.put("fintech-corp", Map.of(
                "RTP", "RTP",
                "ACH_CREDIT", "ACH",
                "WIRE_DOMESTIC", "FEDWIRE"
        ));
        
        logger.info("Initialized clearing system mappings");
    }
}