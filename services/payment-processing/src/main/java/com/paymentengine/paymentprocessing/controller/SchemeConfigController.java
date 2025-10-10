package com.paymentengine.paymentprocessing.controller;

import com.paymentengine.paymentprocessing.dto.SchemeConfigRequest;
import com.paymentengine.paymentprocessing.dto.SchemeConfigResponse;
import com.paymentengine.paymentprocessing.dto.SchemeMessageRequest;
import com.paymentengine.paymentprocessing.dto.SchemeMessageResponse;
import com.paymentengine.paymentprocessing.dto.SchemeTestRequest;
import com.paymentengine.paymentprocessing.dto.SchemeTestResponse;
import com.paymentengine.paymentprocessing.service.SchemeConfigService;
import com.paymentengine.paymentprocessing.service.SchemeMessageService;
import io.micrometer.core.annotation.Timed;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Controller for scheme interaction configuration management
 */
@RestController
@RequestMapping("/api/v1/scheme")
@CrossOrigin(origins = "*", maxAge = 3600)
public class SchemeConfigController {
    
    private static final Logger logger = LoggerFactory.getLogger(SchemeConfigController.class);
    
    private final SchemeConfigService schemeConfigService;
    private final SchemeMessageService schemeMessageService;
    
    @Autowired
    public SchemeConfigController(
            SchemeConfigService schemeConfigService,
            SchemeMessageService schemeMessageService) {
        this.schemeConfigService = schemeConfigService;
        this.schemeMessageService = schemeMessageService;
    }
    
    // ============================================================================
    // CONFIGURATION MANAGEMENT
    // ============================================================================
    
    /**
     * Get all scheme configurations with pagination and filtering
     */
    @GetMapping("/configurations")
    @PreAuthorize("hasAuthority('scheme:read')")
    @Timed(value = "scheme.configurations.list", description = "Time taken to list scheme configurations")
    public ResponseEntity<Page<SchemeConfigResponse>> getConfigurations(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(required = false) String interactionMode,
            @RequestParam(required = false) String messageFormat,
            @RequestParam(required = false) String responseMode,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "updatedAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {
        
        logger.debug("Getting scheme configurations - page: {}, size: {}, sort: {} {}", 
                page, size, sortBy, sortDirection);
        
        try {
            Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
            Pageable pageable = PageRequest.of(page, size, sort);
            
            Page<SchemeConfigResponse> configurations = schemeConfigService.getConfigurations(
                    name, isActive, interactionMode, messageFormat, responseMode, pageable);
            
            return ResponseEntity.ok(configurations);
            
        } catch (Exception e) {
            logger.error("Error getting scheme configurations: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Get a specific scheme configuration by ID
     */
    @GetMapping("/configurations/{configId}")
    @PreAuthorize("hasAuthority('scheme:read')")
    @Timed(value = "scheme.configurations.get", description = "Time taken to get scheme configuration")
    public ResponseEntity<SchemeConfigResponse> getConfiguration(@PathVariable String configId) {
        
        logger.debug("Getting scheme configuration: {}", configId);
        
        try {
            SchemeConfigResponse configuration = schemeConfigService.getConfiguration(configId);
            return ResponseEntity.ok(configuration);
            
        } catch (Exception e) {
            logger.error("Error getting scheme configuration {}: {}", configId, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Create a new scheme configuration
     */
    @PostMapping("/configurations")
    @PreAuthorize("hasAuthority('scheme:create')")
    @Timed(value = "scheme.configurations.create", description = "Time taken to create scheme configuration")
    public ResponseEntity<SchemeConfigResponse> createConfiguration(
            @Valid @RequestBody SchemeConfigRequest request) {
        
        logger.info("Creating scheme configuration: {}", request.getName());
        
        try {
            SchemeConfigResponse configuration = schemeConfigService.createConfiguration(request);
            logger.info("Scheme configuration created successfully: {}", configuration.getId());
            return ResponseEntity.ok(configuration);
            
        } catch (Exception e) {
            logger.error("Error creating scheme configuration: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Update an existing scheme configuration
     */
    @PutMapping("/configurations/{configId}")
    @PreAuthorize("hasAuthority('scheme:update')")
    @Timed(value = "scheme.configurations.update", description = "Time taken to update scheme configuration")
    public ResponseEntity<SchemeConfigResponse> updateConfiguration(
            @PathVariable String configId,
            @Valid @RequestBody SchemeConfigRequest request) {
        
        logger.info("Updating scheme configuration: {}", configId);
        
        try {
            SchemeConfigResponse configuration = schemeConfigService.updateConfiguration(configId, request);
            logger.info("Scheme configuration updated successfully: {}", configId);
            return ResponseEntity.ok(configuration);
            
        } catch (Exception e) {
            logger.error("Error updating scheme configuration {}: {}", configId, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Delete a scheme configuration
     */
    @DeleteMapping("/configurations/{configId}")
    @PreAuthorize("hasAuthority('scheme:delete')")
    @Timed(value = "scheme.configurations.delete", description = "Time taken to delete scheme configuration")
    public ResponseEntity<Map<String, String>> deleteConfiguration(@PathVariable String configId) {
        
        logger.info("Deleting scheme configuration: {}", configId);
        
        try {
            schemeConfigService.deleteConfiguration(configId);
            logger.info("Scheme configuration deleted successfully: {}", configId);
            return ResponseEntity.ok(Map.of("message", "Configuration deleted successfully"));
            
        } catch (Exception e) {
            logger.error("Error deleting scheme configuration {}: {}", configId, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Clone an existing configuration
     */
    @PostMapping("/configurations/{configId}/clone")
    @PreAuthorize("hasAuthority('scheme:create')")
    @Timed(value = "scheme.configurations.clone", description = "Time taken to clone scheme configuration")
    public ResponseEntity<SchemeConfigResponse> cloneConfiguration(
            @PathVariable String configId,
            @RequestBody Map<String, String> request) {
        
        String newName = request.get("name");
        logger.info("Cloning scheme configuration {} with new name: {}", configId, newName);
        
        try {
            SchemeConfigResponse configuration = schemeConfigService.cloneConfiguration(configId, newName);
            logger.info("Scheme configuration cloned successfully: {} -> {}", configId, configuration.getId());
            return ResponseEntity.ok(configuration);
            
        } catch (Exception e) {
            logger.error("Error cloning scheme configuration {}: {}", configId, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Toggle configuration status (activate/deactivate)
     */
    @PatchMapping("/configurations/{configId}/status")
    @PreAuthorize("hasAuthority('scheme:update')")
    @Timed(value = "scheme.configurations.toggle_status", description = "Time taken to toggle configuration status")
    public ResponseEntity<SchemeConfigResponse> toggleConfigurationStatus(
            @PathVariable String configId,
            @RequestBody Map<String, Boolean> request) {
        
        Boolean isActive = request.get("isActive");
        logger.info("Toggling scheme configuration status: {} -> {}", configId, isActive);
        
        try {
            SchemeConfigResponse configuration = schemeConfigService.toggleConfigurationStatus(configId, isActive);
            logger.info("Scheme configuration status toggled successfully: {}", configId);
            return ResponseEntity.ok(configuration);
            
        } catch (Exception e) {
            logger.error("Error toggling scheme configuration status {}: {}", configId, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    // ============================================================================
    // CONFIGURATION TESTING
    // ============================================================================
    
    /**
     * Test a scheme configuration
     */
    @PostMapping("/configurations/test")
    @PreAuthorize("hasAuthority('scheme:test')")
    @Timed(value = "scheme.configurations.test", description = "Time taken to test scheme configuration")
    public ResponseEntity<SchemeTestResponse> testConfiguration(
            @Valid @RequestBody SchemeTestRequest request) {
        
        logger.info("Testing scheme configuration: {}", request.getConfigId());
        
        try {
            SchemeTestResponse testResponse = schemeConfigService.testConfiguration(request);
            logger.info("Scheme configuration test completed: {} - Success: {}", 
                    request.getConfigId(), testResponse.isSuccess());
            return ResponseEntity.ok(testResponse);
            
        } catch (Exception e) {
            logger.error("Error testing scheme configuration {}: {}", request.getConfigId(), e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Validate a configuration without testing
     */
    @PostMapping("/configurations/{configId}/validate")
    @PreAuthorize("hasAuthority('scheme:read')")
    @Timed(value = "scheme.configurations.validate", description = "Time taken to validate scheme configuration")
    public ResponseEntity<Map<String, Object>> validateConfiguration(@PathVariable String configId) {
        
        logger.debug("Validating scheme configuration: {}", configId);
        
        try {
            Map<String, Object> validationResult = schemeConfigService.validateConfiguration(configId);
            return ResponseEntity.ok(validationResult);
            
        } catch (Exception e) {
            logger.error("Error validating scheme configuration {}: {}", configId, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "valid", false,
                    "errors", new String[]{e.getMessage()}
            ));
        }
    }
    
    /**
     * Get test history for a configuration
     */
    @GetMapping("/configurations/{configId}/test-history")
    @PreAuthorize("hasAuthority('scheme:read')")
    @Timed(value = "scheme.configurations.test_history", description = "Time taken to get test history")
    public ResponseEntity<Page<SchemeTestResponse>> getTestHistory(
            @PathVariable String configId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        logger.debug("Getting test history for configuration: {}", configId);
        
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "timestamp"));
            Page<SchemeTestResponse> testHistory = schemeConfigService.getTestHistory(configId, pageable);
            return ResponseEntity.ok(testHistory);
            
        } catch (Exception e) {
            logger.error("Error getting test history for configuration {}: {}", configId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    // ============================================================================
    // SCHEME MESSAGE PROCESSING
    // ============================================================================
    
    /**
     * Send a scheme message using a specific configuration
     */
    @PostMapping("/configurations/{configId}/send")
    @PreAuthorize("hasAuthority('scheme:send')")
    @Timed(value = "scheme.messages.send", description = "Time taken to send scheme message")
    public ResponseEntity<SchemeMessageResponse> sendSchemeMessage(
            @PathVariable String configId,
            @Valid @RequestBody SchemeMessageRequest request) {
        
        logger.info("Sending scheme message via configuration: {} - MessageType: {}", 
                configId, request.getMessageType());
        
        try {
            SchemeMessageResponse response = schemeMessageService.sendMessage(configId, request);
            logger.info("Scheme message sent successfully: {} - Response: {}", 
                    request.getMessageId(), response.getStatus());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error sending scheme message via configuration {}: {}", configId, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Send an asynchronous scheme message
     */
    @PostMapping("/configurations/{configId}/send-async")
    @PreAuthorize("hasAuthority('scheme:send')")
    @Timed(value = "scheme.messages.send_async", description = "Time taken to send async scheme message")
    public ResponseEntity<Map<String, Object>> sendAsynchronousMessage(
            @PathVariable String configId,
            @Valid @RequestBody SchemeMessageRequest request) {
        
        logger.info("Sending async scheme message via configuration: {} - MessageType: {}", 
                configId, request.getMessageType());
        
        try {
            CompletableFuture<SchemeMessageResponse> future = schemeMessageService.sendAsyncMessage(configId, request);
            
            // Return immediate response with correlation info
            Map<String, Object> immediateResponse = Map.of(
                    "messageId", request.getMessageId(),
                    "correlationId", request.getCorrelationId(),
                    "status", "ACCEPTED",
                    "timestamp", Instant.now().toString(),
                    "futureId", future.toString() // In real implementation, use proper tracking ID
            );
            
            logger.info("Async scheme message accepted: {} - CorrelationId: {}", 
                    request.getMessageId(), request.getCorrelationId());
            return ResponseEntity.ok(immediateResponse);
            
        } catch (Exception e) {
            logger.error("Error sending async scheme message via configuration {}: {}", configId, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Poll for asynchronous message response
     */
    @GetMapping("/configurations/{configId}/poll/{correlationId}")
    @PreAuthorize("hasAuthority('scheme:read')")
    @Timed(value = "scheme.messages.poll", description = "Time taken to poll for message response")
    public ResponseEntity<SchemeMessageResponse> pollMessageResponse(
            @PathVariable String configId,
            @PathVariable String correlationId,
            @RequestParam(defaultValue = "30000") long timeoutMs) {
        
        logger.debug("Polling for message response: {} - CorrelationId: {}", configId, correlationId);
        
        try {
            SchemeMessageResponse response = schemeMessageService.pollResponse(configId, correlationId, timeoutMs);
            if (response != null) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.noContent().build();
            }
            
        } catch (Exception e) {
            logger.error("Error polling for message response {}: {}", correlationId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    // ============================================================================
    // STATISTICS AND MONITORING
    // ============================================================================
    
    /**
     * Get scheme interaction statistics
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasAuthority('scheme:read')")
    @Timed(value = "scheme.statistics", description = "Time taken to get scheme statistics")
    public ResponseEntity<Map<String, Object>> getStatistics(
            @RequestParam(required = false) String configId,
            @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String toDate) {
        
        logger.debug("Getting scheme statistics - ConfigId: {}, From: {}, To: {}", configId, fromDate, toDate);
        
        try {
            Map<String, Object> statistics = schemeConfigService.getStatistics(configId, fromDate, toDate);
            return ResponseEntity.ok(statistics);
            
        } catch (Exception e) {
            logger.error("Error getting scheme statistics: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Get configuration health status
     */
    @GetMapping("/configurations/{configId}/health")
    @PreAuthorize("hasAuthority('scheme:read')")
    @Timed(value = "scheme.configurations.health", description = "Time taken to get configuration health")
    public ResponseEntity<Map<String, Object>> getConfigurationHealth(@PathVariable String configId) {
        
        logger.debug("Getting health status for configuration: {}", configId);
        
        try {
            Map<String, Object> health = schemeConfigService.getConfigurationHealth(configId);
            return ResponseEntity.ok(health);
            
        } catch (Exception e) {
            logger.error("Error getting health status for configuration {}: {}", configId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Get all configuration health statuses
     */
    @GetMapping("/configurations/health")
    @PreAuthorize("hasAuthority('scheme:read')")
    @Timed(value = "scheme.configurations.health_all", description = "Time taken to get all configuration health")
    public ResponseEntity<Map<String, Object>> getAllConfigurationHealth() {
        
        logger.debug("Getting health status for all configurations");
        
        try {
            Map<String, Object> healthStatuses = schemeConfigService.getAllConfigurationHealth();
            return ResponseEntity.ok(healthStatuses);
            
        } catch (Exception e) {
            logger.error("Error getting health status for all configurations: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    // ============================================================================
    // TEMPLATE MANAGEMENT
    // ============================================================================
    
    /**
     * Get available message templates
     */
    @GetMapping("/templates")
    @PreAuthorize("hasAuthority('scheme:read')")
    @Timed(value = "scheme.templates", description = "Time taken to get message templates")
    public ResponseEntity<Map<String, Object>> getMessageTemplates() {
        
        logger.debug("Getting message templates");
        
        try {
            Map<String, Object> templates = schemeConfigService.getMessageTemplates();
            return ResponseEntity.ok(templates);
            
        } catch (Exception e) {
            logger.error("Error getting message templates: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Create a message from template
     */
    @PostMapping("/templates/{templateId}/create")
    @PreAuthorize("hasAuthority('scheme:create')")
    @Timed(value = "scheme.templates.create", description = "Time taken to create message from template")
    public ResponseEntity<SchemeMessageRequest> createMessageFromTemplate(
            @PathVariable String templateId,
            @RequestBody Map<String, Object> variables) {
        
        logger.info("Creating message from template: {}", templateId);
        
        try {
            SchemeMessageRequest message = schemeConfigService.createMessageFromTemplate(templateId, variables);
            return ResponseEntity.ok(message);
            
        } catch (Exception e) {
            logger.error("Error creating message from template {}: {}", templateId, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    // ============================================================================
    // HEALTH CHECK
    // ============================================================================
    
    /**
     * Health check for scheme service
     */
    @GetMapping("/health")
    @Timed(value = "scheme.health", description = "Time taken for scheme health check")
    public ResponseEntity<Map<String, Object>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "scheme-service",
                "timestamp", Instant.now().toString(),
                "version", "1.0.0"
        ));
    }
}