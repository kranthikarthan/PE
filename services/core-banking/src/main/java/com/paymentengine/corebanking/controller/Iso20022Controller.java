package com.paymentengine.corebanking.controller;

import com.paymentengine.corebanking.dto.CreateTransactionRequest;
import com.paymentengine.corebanking.dto.TransactionResponse;
import com.paymentengine.corebanking.service.TransactionService;
import com.paymentengine.shared.dto.iso20022.Pain001Message;
import com.paymentengine.shared.service.Iso20022MessageService;
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
 * ISO 20022 compliant payment initiation controller
 * Handles pain.001 (Customer Credit Transfer Initiation) messages
 */
@RestController
@RequestMapping("/api/v1/iso20022")
@CrossOrigin(origins = "*", maxAge = 3600)
public class Iso20022Controller {
    
    private static final Logger logger = LoggerFactory.getLogger(Iso20022Controller.class);
    
    private final TransactionService transactionService;
    private final Iso20022MessageService iso20022MessageService;
    
    @Autowired
    public Iso20022Controller(TransactionService transactionService, 
                            Iso20022MessageService iso20022MessageService) {
        this.transactionService = transactionService;
        this.iso20022MessageService = iso20022MessageService;
    }
    
    /**
     * Process pain.001 (Customer Credit Transfer Initiation) message
     */
    @PostMapping("/pain001")
    @PreAuthorize("hasAuthority('payment:create')")
    @Timed(value = "iso20022.pain001", description = "Time taken to process pain.001 message")
    public ResponseEntity<Map<String, Object>> processPain001(
            @Valid @RequestBody Pain001Message pain001Message,
            HttpServletRequest httpRequest) {
        
        logger.info("Processing ISO 20022 pain.001 message: {}", 
                   pain001Message.getCustomerCreditTransferInitiation().getGroupHeader().getMessageId());
        
        try {
            // Validate ISO 20022 message structure
            Iso20022MessageService.ValidationResult validation = iso20022MessageService.validatePain001Message(pain001Message);
            
            if (!validation.isValid()) {
                logger.warn("pain.001 message validation failed: {}", validation.getErrors());
                return ResponseEntity.badRequest().body(Map.of(
                    "error", Map.of(
                        "code", "INVALID_ISO20022_MESSAGE",
                        "message", "pain.001 message validation failed",
                        "details", Map.of(
                            "errors", validation.getErrors(),
                            "warnings", validation.getWarnings()
                        ),
                        "timestamp", LocalDateTime.now().toString()
                    )
                ));
            }
            
            // Transform ISO 20022 message to internal format
            CreateTransactionRequest transactionRequest = iso20022MessageService.transformPain001ToTransactionRequest(pain001Message);
            
            // Add request context
            transactionRequest.setIpAddress(getClientIpAddress(httpRequest));
            transactionRequest.setChannel("iso20022");
            
            // Process the transaction
            TransactionResponse transactionResponse = transactionService.createTransaction(transactionRequest);
            
            // Transform response to pain.002 format (Payment Status Report)
            String originalMessageId = pain001Message.getCustomerCreditTransferInitiation().getGroupHeader().getMessageId();
            Map<String, Object> pain002Response = iso20022MessageService.transformTransactionResponseToPain002(
                transactionResponse, originalMessageId);
            
            logger.info("pain.001 message processed successfully. Transaction: {}", 
                       transactionResponse.getTransactionReference());
            
            return ResponseEntity.status(HttpStatus.CREATED).body(pain002Response);
            
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid pain.001 request: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "error", Map.of(
                    "code", "INVALID_PAYMENT_REQUEST",
                    "message", e.getMessage(),
                    "timestamp", LocalDateTime.now().toString()
                )
            ));
        } catch (Exception e) {
            logger.error("Error processing pain.001 message: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "error", Map.of(
                    "code", "PROCESSING_ERROR",
                    "message", "Failed to process payment initiation",
                    "timestamp", LocalDateTime.now().toString()
                )
            ));
        }
    }
    
    /**
     * Get payment status in pain.002 format
     */
    @GetMapping("/pain002/{transactionId}")
    @PreAuthorize("hasAuthority('transaction:read')")
    @Timed(value = "iso20022.pain002", description = "Time taken to get pain.002 status")
    public ResponseEntity<Map<String, Object>> getPain002Status(
            @PathVariable UUID transactionId,
            @RequestParam(required = false) String originalMessageId) {
        
        logger.debug("Getting pain.002 status for transaction: {}", transactionId);
        
        try {
            TransactionResponse transaction = transactionService.getTransactionById(transactionId)
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found"));
            
            // Generate pain.002 response
            String msgId = originalMessageId != null ? originalMessageId : "UNKNOWN";
            Map<String, Object> pain002Response = iso20022MessageService.transformTransactionResponseToPain002(
                transaction, msgId);
            
            return ResponseEntity.ok(pain002Response);
            
        } catch (IllegalArgumentException e) {
            logger.warn("Transaction not found: {}", transactionId);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error getting pain.002 status for {}: {}", transactionId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Validate pain.001 message without processing
     */
    @PostMapping("/pain001/validate")
    @PreAuthorize("hasAuthority('payment:create')")
    @Timed(value = "iso20022.validate", description = "Time taken to validate pain.001 message")
    public ResponseEntity<Map<String, Object>> validatePain001(
            @Valid @RequestBody Pain001Message pain001Message) {
        
        logger.debug("Validating pain.001 message: {}", 
                    pain001Message.getCustomerCreditTransferInitiation().getGroupHeader().getMessageId());
        
        try {
            Iso20022MessageService.ValidationResult validation = iso20022MessageService.validatePain001Message(pain001Message);
            
            Map<String, Object> response = Map.of(
                "valid", validation.isValid(),
                "messageId", pain001Message.getCustomerCreditTransferInitiation().getGroupHeader().getMessageId(),
                "errors", validation.getErrors(),
                "warnings", validation.getWarnings(),
                "timestamp", LocalDateTime.now().toString()
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error validating pain.001 message: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "valid", false,
                "error", e.getMessage(),
                "timestamp", LocalDateTime.now().toString()
            ));
        }
    }
    
    /**
     * Get supported ISO 20022 message types and versions
     */
    @GetMapping("/supported-messages")
    @PreAuthorize("hasAuthority('payment:read')")
    @Timed(value = "iso20022.supported_messages", description = "Time taken to get supported messages")
    public ResponseEntity<Map<String, Object>> getSupportedMessages() {
        
        Map<String, Object> supportedMessages = Map.of(
            "supportedMessages", Map.of(
                "pain.001.001.03", Map.of(
                    "name", "Customer Credit Transfer Initiation",
                    "description", "Message for initiating credit transfers",
                    "supported", true,
                    "version", "1.3"
                ),
                "pain.002.001.03", Map.of(
                    "name", "Customer Payment Status Report", 
                    "description", "Message for reporting payment status",
                    "supported", true,
                    "version", "1.3"
                )
            ),
            "paymentMethods", Map.of(
                "TRF", "Credit Transfer",
                "DD", "Direct Debit",
                "CHK", "Check",
                "TRA", "Transfer Advice"
            ),
            "localInstruments", Map.of(
                "RTGS", "Real Time Gross Settlement",
                "ACH", "Automated Clearing House",
                "WIRE", "Wire Transfer",
                "RTP", "Real Time Payment",
                "SEPA", "Single Euro Payments Area",
                "INST", "Instant Payment"
            ),
            "serviceLevels", Map.of(
                "SEPA", "Single Euro Payments Area",
                "URGP", "Urgent Payment",
                "NURG", "Non-Urgent Payment"
            ),
            "chargeBearers", Map.of(
                "DEBT", "Debtor",
                "CRED", "Creditor", 
                "SHAR", "Shared",
                "SLEV", "Service Level"
            )
        );
        
        return ResponseEntity.ok(supportedMessages);
    }
    
    /**
     * Health check for ISO 20022 service
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
            "status", "UP",
            "service", "iso20022-service",
            "messageFormat", "pain.001.001.03",
            "timestamp", LocalDateTime.now().toString()
        ));
    }
    
    // Helper methods
    
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
}