package com.paymentengine.paymentprocessing.controller;

import com.paymentengine.shared.dto.iso20022.Pain001Message;
import com.paymentengine.shared.dto.CreateTransactionRequest;
import com.paymentengine.shared.dto.TransactionResponse;
import com.paymentengine.shared.service.Iso20022MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST Controller for ISO 20022 message processing
 * Provides endpoints for PAIN.001 to PAIN.002 message flow
 */
@RestController
@RequestMapping("/api/v1/iso20022")
@CrossOrigin(origins = "*")
public class Iso20022Controller {

    @Autowired
    private Iso20022MessageService iso20022MessageService;

    /**
     * Process PAIN.001 message and return transaction request
     */
    @PostMapping("/pain001/process")
    public ResponseEntity<CreateTransactionRequest> processPain001(@RequestBody Pain001Message pain001Message) {
        try {
            CreateTransactionRequest transactionRequest = iso20022MessageService.transformPain001ToTransactionRequest(pain001Message);
            return ResponseEntity.ok(transactionRequest);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Generate PAIN.002 response from transaction response
     */
    @PostMapping("/pain002/generate")
    public ResponseEntity<Map<String, Object>> generatePain002(
            @RequestBody TransactionResponse transactionResponse,
            @RequestParam String originalMessageId) {
        try {
            Map<String, Object> pain002Response = iso20022MessageService.transformTransactionResponseToPain002(transactionResponse, originalMessageId);
            return ResponseEntity.ok(pain002Response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Complete PAIN.001 to PAIN.002 flow
     */
    @PostMapping("/flow/complete")
    public ResponseEntity<Map<String, Object>> completeFlow(@RequestBody Pain001Message pain001Message) {
        try {
            // Step 1: Transform PAIN.001 to transaction request
            CreateTransactionRequest transactionRequest = iso20022MessageService.transformPain001ToTransactionRequest(pain001Message);
            
            // Step 2: Simulate transaction processing (normally done by core banking)
            TransactionResponse transactionResponse = new TransactionResponse();
            transactionResponse.setTransactionId("TXN-" + System.currentTimeMillis());
            transactionResponse.setExternalReference(transactionRequest.getExternalReference());
            transactionResponse.setStatus("COMPLETED");
            transactionResponse.setAmount(transactionRequest.getAmount());
            transactionResponse.setCurrencyCode(transactionRequest.getCurrencyCode());
            transactionResponse.setDescription(transactionRequest.getDescription());
            transactionResponse.setCreatedAt(java.time.LocalDateTime.now());
            transactionResponse.setCompletedAt(java.time.LocalDateTime.now());
            
            // Step 3: Generate PAIN.002 response
            Map<String, Object> pain002Response = iso20022MessageService.transformTransactionResponseToPain002(transactionResponse, "MSG-12345");
            
            return ResponseEntity.ok(pain002Response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
            "status", "UP",
            "service", "ISO 20022 Message Processing",
            "version", "1.0.0"
        ));
    }
}