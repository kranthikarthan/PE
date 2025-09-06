package com.paymentengine.corebanking.controller;

import com.paymentengine.corebanking.service.Iso20022ProcessingService;
import com.paymentengine.shared.dto.iso20022.*;
import io.micrometer.core.annotation.Timed;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Comprehensive ISO 20022 Message Controller
 * Handles all ISO 20022 message types: pain, pacs, camt
 */
@RestController
@RequestMapping("/api/v1/iso20022")
@CrossOrigin(origins = "*", maxAge = 3600)
public class Iso20022MessageController {
    
    private static final Logger logger = LoggerFactory.getLogger(Iso20022MessageController.class);
    
    private final Iso20022ProcessingService iso20022ProcessingService;
    
    @Autowired
    public Iso20022MessageController(Iso20022ProcessingService iso20022ProcessingService) {
        this.iso20022ProcessingService = iso20022ProcessingService;
    }
    
    // ============================================================================
    // PAIN MESSAGES (Customer Initiated)
    // ============================================================================
    
    /**
     * Process pain.001 - Customer Credit Transfer Initiation
     */
    @PostMapping("/pain001")
    @PreAuthorize("hasAuthority('payment:create')")
    @Timed(value = "iso20022.pain001", description = "Time taken to process pain.001 message")
    public ResponseEntity<Map<String, Object>> processPain001(
            @Valid @RequestBody Pain001Message pain001Message,
            HttpServletRequest httpRequest) {
        
        logger.info("Processing pain.001 message: {}", 
                   pain001Message.getCustomerCreditTransferInitiation().getGroupHeader().getMessageId());
        
        try {
            Map<String, Object> response = iso20022ProcessingService.processPain001(pain001Message, getRequestContext(httpRequest));
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (Exception e) {
            logger.error("Error processing pain.001: {}", e.getMessage(), e);
            return createErrorResponse("PAIN001_PROCESSING_ERROR", e.getMessage());
        }
    }
    
    /**
     * Process pain.007 - Customer Payment Reversal
     */
    @PostMapping("/pain007")
    @PreAuthorize("hasAuthority('payment:reverse')")
    @Timed(value = "iso20022.pain007", description = "Time taken to process pain.007 reversal")
    public ResponseEntity<Map<String, Object>> processPain007(
            @Valid @RequestBody Pain007Message pain007Message,
            HttpServletRequest httpRequest) {
        
        logger.info("Processing pain.007 reversal: {}", 
                   pain007Message.getCustomerPaymentReversal().getGroupHeader().getMessageId());
        
        try {
            Map<String, Object> response = iso20022ProcessingService.processPain007(pain007Message, getRequestContext(httpRequest));
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error processing pain.007: {}", e.getMessage(), e);
            return createErrorResponse("PAIN007_PROCESSING_ERROR", e.getMessage());
        }
    }
    
    /**
     * Get pain.002 - Customer Payment Status Report
     */
    @GetMapping("/pain002/{transactionId}")
    @PreAuthorize("hasAuthority('transaction:read')")
    @Timed(value = "iso20022.pain002", description = "Time taken to get pain.002 status")
    public ResponseEntity<Map<String, Object>> getPain002Status(
            @PathVariable UUID transactionId,
            @RequestParam(required = false) String originalMessageId) {
        
        logger.debug("Getting pain.002 status for transaction: {}", transactionId);
        
        try {
            Map<String, Object> response = iso20022ProcessingService.generatePain002(transactionId, originalMessageId);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error generating pain.002: {}", e.getMessage(), e);
            return createErrorResponse("PAIN002_GENERATION_ERROR", e.getMessage());
        }
    }
    
    // ============================================================================
    // PACS MESSAGES (Scheme/Network Processing)
    // ============================================================================
    
    /**
     * Process pacs.008 - FI to FI Customer Credit Transfer
     */
    @PostMapping("/pacs008")
    @PreAuthorize("hasAuthority('scheme:process')")
    @Timed(value = "iso20022.pacs008", description = "Time taken to process pacs.008 message")
    public ResponseEntity<Map<String, Object>> processPacs008(
            @Valid @RequestBody Pacs008Message pacs008Message,
            HttpServletRequest httpRequest) {
        
        logger.info("Processing pacs.008 from scheme: {}", 
                   pacs008Message.getFiToFICustomerCreditTransfer().getGroupHeader().getMessageId());
        
        try {
            Map<String, Object> response = iso20022ProcessingService.processPacs008(pacs008Message, getRequestContext(httpRequest));
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error processing pacs.008: {}", e.getMessage(), e);
            return createErrorResponse("PACS008_PROCESSING_ERROR", e.getMessage());
        }
    }
    
    /**
     * Generate pacs.002 - FI to FI Payment Status Report
     */
    @GetMapping("/pacs002/{transactionId}")
    @PreAuthorize("hasAuthority('scheme:read')")
    @Timed(value = "iso20022.pacs002", description = "Time taken to generate pacs.002 status")
    public ResponseEntity<Map<String, Object>> generatePacs002(
            @PathVariable UUID transactionId,
            @RequestParam(required = false) String originalMessageId) {
        
        logger.debug("Generating pacs.002 for transaction: {}", transactionId);
        
        try {
            Map<String, Object> response = iso20022ProcessingService.generatePacs002(transactionId, originalMessageId);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error generating pacs.002: {}", e.getMessage(), e);
            return createErrorResponse("PACS002_GENERATION_ERROR", e.getMessage());
        }
    }
    
    // ============================================================================
    // CAMT MESSAGES (Cash Management)
    // ============================================================================
    
    /**
     * Generate camt.053 - Bank to Customer Statement
     */
    @GetMapping("/camt053/account/{accountId}")
    @PreAuthorize("hasAuthority('account:read')")
    @Timed(value = "iso20022.camt053", description = "Time taken to generate camt.053 statement")
    public ResponseEntity<Camt053Message> generateCamt053(
            @PathVariable UUID accountId,
            @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String toDate,
            @RequestParam(defaultValue = "false") boolean includeTransactionDetails) {
        
        logger.info("Generating camt.053 statement for account: {}", accountId);
        
        try {
            Camt053Message response = iso20022ProcessingService.generateCamt053(accountId, fromDate, toDate, includeTransactionDetails);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error generating camt.053: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Generate camt.054 - Bank to Customer Debit Credit Notification
     */
    @GetMapping("/camt054/account/{accountId}")
    @PreAuthorize("hasAuthority('account:read')")
    @Timed(value = "iso20022.camt054", description = "Time taken to generate camt.054 notification")
    public ResponseEntity<Camt054Message> generateCamt054(
            @PathVariable UUID accountId,
            @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String toDate) {
        
        logger.info("Generating camt.054 notification for account: {}", accountId);
        
        try {
            Camt054Message response = iso20022ProcessingService.generateCamt054(accountId, fromDate, toDate);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error generating camt.054: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Process camt.055 - Customer Payment Cancellation Request
     */
    @PostMapping("/camt055")
    @PreAuthorize("hasAuthority('payment:cancel')")
    @Timed(value = "iso20022.camt055", description = "Time taken to process camt.055 customer cancellation")
    public ResponseEntity<Map<String, Object>> processCamt055(
            @Valid @RequestBody Camt055Message camt055Message,
            HttpServletRequest httpRequest) {
        
        logger.info("Processing camt.055 customer payment cancellation request: {}", 
                   camt055Message.getCustomerPaymentCancellationRequest().getGroupHeader().getMessageId());
        
        try {
            Map<String, Object> response = iso20022ProcessingService.processCamt055(camt055Message, getRequestContext(httpRequest));
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid camt.055 cancellation request: {}", e.getMessage());
            return createErrorResponse("INVALID_CANCELLATION_REQUEST", e.getMessage());
        } catch (Exception e) {
            logger.error("Error processing camt.055: {}", e.getMessage(), e);
            return createErrorResponse("CAMT055_PROCESSING_ERROR", e.getMessage());
        }
    }

    /**
     * Process camt.056 - FI to FI Payment Cancellation Request
     */
    @PostMapping("/camt056")
    @PreAuthorize("hasAuthority('payment:cancel')")
    @Timed(value = "iso20022.camt056", description = "Time taken to process camt.056 cancellation")
    public ResponseEntity<Map<String, Object>> processCamt056(
            @Valid @RequestBody Map<String, Object> camt056Message,
            HttpServletRequest httpRequest) {
        
        logger.info("Processing camt.056 FI to FI cancellation request");
        
        try {
            Map<String, Object> response = iso20022ProcessingService.processCamt056(camt056Message, getRequestContext(httpRequest));
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error processing camt.056: {}", e.getMessage(), e);
            return createErrorResponse("CAMT056_PROCESSING_ERROR", e.getMessage());
        }
    }
    
    // ============================================================================
    // MESSAGE VALIDATION AND UTILITIES
    // ============================================================================
    
    /**
     * Validate any ISO 20022 message
     */
    @PostMapping("/validate/{messageType}")
    @PreAuthorize("hasAuthority('message:validate')")
    @Timed(value = "iso20022.validate", description = "Time taken to validate ISO 20022 message")
    public ResponseEntity<Map<String, Object>> validateMessage(
            @PathVariable String messageType,
            @RequestBody Map<String, Object> message) {
        
        logger.debug("Validating {} message", messageType);
        
        try {
            Map<String, Object> validationResult = iso20022ProcessingService.validateMessage(messageType, message);
            return ResponseEntity.ok(validationResult);
            
        } catch (Exception e) {
            logger.error("Error validating {} message: {}", messageType, e.getMessage(), e);
            return createErrorResponse("MESSAGE_VALIDATION_ERROR", e.getMessage());
        }
    }
    
    /**
     * Get supported ISO 20022 message types
     */
    @GetMapping("/supported-messages")
    @PreAuthorize("hasAuthority('message:read')")
    @Timed(value = "iso20022.supported_messages", description = "Time taken to get supported messages")
    public ResponseEntity<Map<String, Object>> getSupportedMessages() {
        
        Map<String, Object> supportedMessages = Map.of(
            "customerInitiated", Map.of(
                "pain.001.001.03", Map.of(
                    "name", "Customer Credit Transfer Initiation",
                    "description", "Initiate credit transfers",
                    "direction", "inbound",
                    "supported", true
                ),
                "pain.002.001.03", Map.of(
                    "name", "Customer Payment Status Report",
                    "description", "Report payment status to customer",
                    "direction", "outbound", 
                    "supported", true
                ),
                "pain.007.001.03", Map.of(
                    "name", "Customer Payment Reversal",
                    "description", "Reverse customer payments",
                    "direction", "inbound",
                    "supported", true
                ),
                "pain.008.001.03", Map.of(
                    "name", "Customer Payment Reversal Response",
                    "description", "Response to payment reversal",
                    "direction", "outbound",
                    "supported", true
                )
            ),
            "schemeProcessing", Map.of(
                "pacs.008.001.03", Map.of(
                    "name", "FI to FI Customer Credit Transfer",
                    "description", "Process payments from schemes",
                    "direction", "inbound",
                    "supported", true
                ),
                "pacs.002.001.03", Map.of(
                    "name", "FI to FI Payment Status Report",
                    "description", "Send status to schemes",
                    "direction", "outbound",
                    "supported", true
                ),
                "pacs.004.001.03", Map.of(
                    "name", "Payment Return",
                    "description", "Return payments to schemes",
                    "direction", "outbound",
                    "supported", true
                )
            ),
            "cashManagement", Map.of(
                "camt.053.001.03", Map.of(
                    "name", "Bank to Customer Statement",
                    "description", "Account statements",
                    "direction", "outbound",
                    "supported", true
                ),
                "camt.054.001.03", Map.of(
                    "name", "Bank to Customer Debit Credit Notification",
                    "description", "Transaction notifications",
                    "direction", "outbound",
                    "supported", true
                ),
                "camt.055.001.03", Map.of(
                    "name", "Customer Payment Cancellation Request",
                    "description", "Customer requests payment cancellation",
                    "direction", "inbound",
                    "supported", true
                ),
                "camt.056.001.03", Map.of(
                    "name", "FI to FI Payment Cancellation Request",
                    "description", "Cancel payments between FIs",
                    "direction", "inbound",
                    "supported", true
                )
            ),
            "messageFormats", Map.of(
                "supported", List.of("JSON", "XML"),
                "default", "JSON",
                "contentTypes", List.of("application/json", "application/xml")
            ),
            "businessDayCodes", Map.of(
                "FWNG", "Following Business Day",
                "MODF", "Modified Following Business Day",
                "PREC", "Preceding Business Day",
                "UMOD", "Unmodified Following Business Day"
            ),
            "returnReasonCodes", Map.of(
                "AC01", "Incorrect Account Number",
                "AC04", "Closed Account Number", 
                "AC06", "Blocked Account",
                "AG01", "Transaction Forbidden",
                "AG02", "Invalid Bank Operation Code",
                "AM04", "Insufficient Funds",
                "AM05", "Duplication",
                "BE01", "Inconsistent With End Customer",
                "DT01", "Invalid Date",
                "RF01", "Not Unique Transaction Reference",
                "RR01", "Missing Debtor Address",
                "RR02", "Missing Debtor Name",
                "RR03", "Missing Creditor Name",
                "RR04", "Regulatory Reason"
            )
        );
        
        return ResponseEntity.ok(supportedMessages);
    }
    
    /**
     * Transform message between formats
     */
    @PostMapping("/transform/{fromFormat}/{toFormat}")
    @PreAuthorize("hasAuthority('message:transform')")
    @Timed(value = "iso20022.transform", description = "Time taken to transform message format")
    public ResponseEntity<Map<String, Object>> transformMessage(
            @PathVariable String fromFormat,
            @PathVariable String toFormat,
            @RequestBody Map<String, Object> message) {
        
        logger.info("Transforming message from {} to {}", fromFormat, toFormat);
        
        try {
            Map<String, Object> transformedMessage = iso20022ProcessingService.transformMessage(fromFormat, toFormat, message);
            return ResponseEntity.ok(transformedMessage);
            
        } catch (Exception e) {
            logger.error("Error transforming message: {}", e.getMessage(), e);
            return createErrorResponse("MESSAGE_TRANSFORMATION_ERROR", e.getMessage());
        }
    }
    
    // ============================================================================
    // PACS MESSAGES (Scheme Processing)
    // ============================================================================
    
    /**
     * Process pacs.008 - FI to FI Customer Credit Transfer (from scheme)
     */
    @PostMapping("/pacs008")
    @PreAuthorize("hasAuthority('scheme:process')")
    @Timed(value = "iso20022.pacs008", description = "Time taken to process pacs.008 from scheme")
    public ResponseEntity<Map<String, Object>> processPacs008(
            @Valid @RequestBody Pacs008Message pacs008Message,
            HttpServletRequest httpRequest) {
        
        logger.info("Processing pacs.008 from scheme: {}", 
                   pacs008Message.getFiToFICustomerCreditTransfer().getGroupHeader().getMessageId());
        
        try {
            Map<String, Object> response = iso20022ProcessingService.processPacs008FromScheme(pacs008Message, getRequestContext(httpRequest));
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error processing pacs.008 from scheme: {}", e.getMessage(), e);
            return createErrorResponse("PACS008_SCHEME_PROCESSING_ERROR", e.getMessage());
        }
    }
    
    /**
     * Generate pacs.004 - Payment Return (to scheme)
     */
    @PostMapping("/pacs004/{transactionId}")
    @PreAuthorize("hasAuthority('scheme:return')")
    @Timed(value = "iso20022.pacs004", description = "Time taken to generate pacs.004 return")
    public ResponseEntity<Map<String, Object>> generatePacs004(
            @PathVariable UUID transactionId,
            @RequestBody Map<String, String> returnRequest) {
        
        String returnReason = returnRequest.get("reason");
        String reasonCode = returnRequest.get("reasonCode");
        
        logger.info("Generating pacs.004 return for transaction: {} with reason: {}", transactionId, returnReason);
        
        try {
            Map<String, Object> response = iso20022ProcessingService.generatePacs004Return(transactionId, reasonCode, returnReason);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error generating pacs.004 return: {}", e.getMessage(), e);
            return createErrorResponse("PACS004_GENERATION_ERROR", e.getMessage());
        }
    }
    
    // ============================================================================
    // CAMT MESSAGES (Cash Management)
    // ============================================================================
    
    /**
     * Generate camt.053 - Bank to Customer Statement
     */
    @GetMapping("/camt053/account/{accountId}")
    @PreAuthorize("hasAuthority('account:statement')")
    @Timed(value = "iso20022.camt053", description = "Time taken to generate camt.053 statement")
    public ResponseEntity<Camt053Message> generateCamt053(
            @PathVariable UUID accountId,
            @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String toDate,
            @RequestParam(defaultValue = "false") boolean includeTransactionDetails) {
        
        logger.info("Generating camt.053 statement for account: {} from {} to {}", accountId, fromDate, toDate);
        
        try {
            Camt053Message response = iso20022ProcessingService.generateCamt053Statement(accountId, fromDate, toDate, includeTransactionDetails);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error generating camt.053: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Generate camt.054 - Bank to Customer Debit Credit Notification
     */
    @GetMapping("/camt054/account/{accountId}")
    @PreAuthorize("hasAuthority('account:notification')")
    @Timed(value = "iso20022.camt054", description = "Time taken to generate camt.054 notification")
    public ResponseEntity<Camt054Message> generateCamt054(
            @PathVariable UUID accountId,
            @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String toDate) {
        
        logger.info("Generating camt.054 notification for account: {}", accountId);
        
        try {
            Camt054Message response = iso20022ProcessingService.generateCamt054Notification(accountId, fromDate, toDate);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error generating camt.054: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Get account balance in camt format
     */
    @GetMapping("/camt052/account/{accountId}")
    @PreAuthorize("hasAuthority('account:balance')")
    @Timed(value = "iso20022.camt052", description = "Time taken to get camt.052 balance")
    public ResponseEntity<Map<String, Object>> getCamt052Balance(
            @PathVariable UUID accountId,
            @RequestParam(defaultValue = "CLBD") String balanceType) {
        
        logger.debug("Getting camt.052 balance for account: {}", accountId);
        
        try {
            Map<String, Object> response = iso20022ProcessingService.generateCamt052Balance(accountId, balanceType);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error getting camt.052 balance: {}", e.getMessage(), e);
            return createErrorResponse("CAMT052_BALANCE_ERROR", e.getMessage());
        }
    }
    
    // ============================================================================
    // BULK AND BATCH PROCESSING
    // ============================================================================
    
    /**
     * Process bulk pain.001 messages
     */
    @PostMapping("/bulk/pain001")
    @PreAuthorize("hasAuthority('payment:bulk')")
    @Timed(value = "iso20022.bulk_pain001", description = "Time taken to process bulk pain.001 messages")
    public ResponseEntity<Map<String, Object>> processBulkPain001(
            @Valid @RequestBody Map<String, Object> bulkRequest,
            HttpServletRequest httpRequest) {
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> messages = (List<Map<String, Object>>) bulkRequest.get("messages");
        
        logger.info("Processing bulk pain.001 with {} messages", messages != null ? messages.size() : 0);
        
        try {
            Map<String, Object> response = iso20022ProcessingService.processBulkPain001(messages, getRequestContext(httpRequest));
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error processing bulk pain.001: {}", e.getMessage(), e);
            return createErrorResponse("BULK_PAIN001_PROCESSING_ERROR", e.getMessage());
        }
    }
    
    /**
     * Get batch processing status
     */
    @GetMapping("/batch/{batchId}/status")
    @PreAuthorize("hasAuthority('batch:read')")
    @Timed(value = "iso20022.batch_status", description = "Time taken to get batch status")
    public ResponseEntity<Map<String, Object>> getBatchStatus(@PathVariable String batchId) {
        
        logger.debug("Getting batch status for: {}", batchId);
        
        try {
            Map<String, Object> response = iso20022ProcessingService.getBatchStatus(batchId);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error getting batch status: {}", e.getMessage(), e);
            return createErrorResponse("BATCH_STATUS_ERROR", e.getMessage());
        }
    }
    
    // ============================================================================
    // REPORTING AND ANALYTICS
    // ============================================================================
    
    /**
     * Get ISO 20022 message statistics
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasAuthority('reporting:read')")
    @Timed(value = "iso20022.statistics", description = "Time taken to get ISO 20022 statistics")
    public ResponseEntity<Map<String, Object>> getMessageStatistics(
            @RequestParam(required = false) String messageType,
            @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String toDate) {
        
        logger.debug("Getting ISO 20022 statistics for message type: {}", messageType);
        
        try {
            Map<String, Object> response = iso20022ProcessingService.getMessageStatistics(messageType, fromDate, toDate);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error getting message statistics: {}", e.getMessage(), e);
            return createErrorResponse("STATISTICS_ERROR", e.getMessage());
        }
    }
    
    /**
     * Health check for ISO 20022 service
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> health = Map.of(
            "status", "UP",
            "service", "iso20022-message-service",
            "supportedMessages", List.of("pain.001", "pain.002", "pain.007", "pain.008", "pacs.008", "pacs.002", "pacs.004", "camt.053", "camt.054", "camt.056"),
            "messageFormat", "JSON",
            "version", "1.0.0",
            "timestamp", LocalDateTime.now().toString()
        );
        
        return ResponseEntity.ok(health);
    }
    
    // ============================================================================
    // HELPER METHODS
    // ============================================================================
    
    private Map<String, Object> getRequestContext(HttpServletRequest request) {
        return Map.of(
            "ipAddress", getClientIpAddress(request),
            "userAgent", request.getHeader("User-Agent") != null ? request.getHeader("User-Agent") : "",
            "userId", request.getHeader("X-User-ID") != null ? request.getHeader("X-User-ID") : "",
            "correlationId", request.getHeader("X-Correlation-ID") != null ? request.getHeader("X-Correlation-ID") : "",
            "timestamp", LocalDateTime.now().toString(),
            "channel", "iso20022-api"
        );
    }
    
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
    
    private ResponseEntity<Map<String, Object>> createErrorResponse(String errorCode, String errorMessage) {
        Map<String, Object> error = Map.of(
            "error", Map.of(
                "code", errorCode,
                "message", errorMessage,
                "timestamp", LocalDateTime.now().toString(),
                "service", "iso20022-message-service"
            )
        );
        
        return ResponseEntity.badRequest().body(error);
    }
}