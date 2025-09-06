package com.paymentengine.corebanking.controller;

import com.paymentengine.corebanking.dto.CreateTransactionRequest;
import com.paymentengine.corebanking.dto.TransactionResponse;
import com.paymentengine.corebanking.entity.Transaction;
import com.paymentengine.corebanking.service.TransactionService;
import io.micrometer.core.annotation.Timed;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * REST controller for transaction operations
 */
@RestController
@RequestMapping("/api/v1/transactions")
@CrossOrigin(origins = "*", maxAge = 3600)
public class TransactionController {
    
    private static final Logger logger = LoggerFactory.getLogger(TransactionController.class);
    
    private final TransactionService transactionService;
    
    @Autowired
    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }
    
    /**
     * Create a new transaction
     */
    @PostMapping
    @PreAuthorize("hasAuthority('payment:create')")
    @Timed(value = "transaction.create", description = "Time taken to create a transaction")
    public ResponseEntity<TransactionResponse> createTransaction(
            @Valid @RequestBody CreateTransactionRequest request,
            HttpServletRequest httpRequest) {
        
        logger.info("Creating transaction request: {}", request);
        
        try {
            // Add request context
            request.setIpAddress(getClientIpAddress(httpRequest));
            request.setChannel("api");
            
            TransactionResponse response = transactionService.createTransaction(request);
            
            logger.info("Transaction created successfully: {}", response.getTransactionReference());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid transaction request: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("Error creating transaction: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Get transaction by ID
     */
    @GetMapping("/{transactionId}")
    @PreAuthorize("hasAuthority('transaction:read')")
    @Timed(value = "transaction.get", description = "Time taken to get a transaction")
    public ResponseEntity<TransactionResponse> getTransaction(@PathVariable UUID transactionId) {
        
        logger.debug("Getting transaction by ID: {}", transactionId);
        
        return transactionService.getTransactionById(transactionId)
            .map(transaction -> ResponseEntity.ok(transaction))
            .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Get transaction by reference
     */
    @GetMapping("/reference/{reference}")
    @PreAuthorize("hasAuthority('transaction:read')")
    @Timed(value = "transaction.get.reference", description = "Time taken to get a transaction by reference")
    public ResponseEntity<TransactionResponse> getTransactionByReference(@PathVariable String reference) {
        
        logger.debug("Getting transaction by reference: {}", reference);
        
        return transactionService.getTransactionByReference(reference)
            .map(transaction -> ResponseEntity.ok(transaction))
            .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Get transaction status
     */
    @GetMapping("/{transactionId}/status")
    @PreAuthorize("hasAuthority('transaction:read')")
    @Timed(value = "transaction.status", description = "Time taken to get transaction status")
    public ResponseEntity<Map<String, Object>> getTransactionStatus(@PathVariable UUID transactionId) {
        
        logger.debug("Getting transaction status for ID: {}", transactionId);
        
        return transactionService.getTransactionById(transactionId)
            .map(transaction -> {
                Map<String, Object> status = Map.of(
                    "transactionId", transaction.getId(),
                    "transactionReference", transaction.getTransactionReference(),
                    "status", transaction.getStatus(),
                    "amount", transaction.getAmount(),
                    "currencyCode", transaction.getCurrencyCode(),
                    "lastUpdated", transaction.getUpdatedAt()
                );
                return ResponseEntity.ok(status);
            })
            .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Search transactions
     */
    @GetMapping("/search")
    @PreAuthorize("hasAuthority('transaction:read')")
    @Timed(value = "transaction.search", description = "Time taken to search transactions")
    public ResponseEntity<Page<TransactionResponse>> searchTransactions(
            @RequestParam(required = false) String transactionReference,
            @RequestParam(required = false) UUID accountId,
            @RequestParam(required = false) Transaction.TransactionStatus status,
            @RequestParam(required = false) UUID paymentTypeId,
            @RequestParam(required = false) BigDecimal minAmount,
            @RequestParam(required = false) BigDecimal maxAmount,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {
        
        logger.debug("Searching transactions with criteria - reference: {}, accountId: {}, status: {}", 
                    transactionReference, accountId, status);
        
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<TransactionResponse> transactions = transactionService.searchTransactions(
            transactionReference, accountId, status, paymentTypeId,
            minAmount, maxAmount, startDate, endDate, pageable
        );
        
        return ResponseEntity.ok(transactions);
    }
    
    /**
     * Get transactions for an account
     */
    @GetMapping("/account/{accountId}")
    @PreAuthorize("hasAuthority('account:read')")
    @Timed(value = "transaction.account", description = "Time taken to get transactions for account")
    public ResponseEntity<Page<TransactionResponse>> getTransactionsByAccount(
            @PathVariable UUID accountId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        logger.debug("Getting transactions for account: {}", accountId);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<TransactionResponse> transactions = transactionService.getTransactionsByAccountId(accountId, pageable);
        
        return ResponseEntity.ok(transactions);
    }
    
    /**
     * Cancel a transaction
     */
    @PostMapping("/{transactionId}/cancel")
    @PreAuthorize("hasAuthority('transaction:update')")
    @Timed(value = "transaction.cancel", description = "Time taken to cancel a transaction")
    public ResponseEntity<TransactionResponse> cancelTransaction(
            @PathVariable UUID transactionId,
            @RequestBody Map<String, String> request) {
        
        logger.info("Cancelling transaction: {}", transactionId);
        
        try {
            String reason = request.getOrDefault("reason", "Cancelled by user");
            TransactionResponse response = transactionService.cancelTransaction(transactionId, reason);
            
            logger.info("Transaction {} cancelled successfully", transactionId);
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            logger.warn("Cannot cancel transaction {}: {}", transactionId, e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("Error cancelling transaction {}: {}", transactionId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Get transaction statistics
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasAuthority('transaction:read')")
    @Timed(value = "transaction.statistics", description = "Time taken to get transaction statistics")
    public ResponseEntity<Map<String, Object>> getTransactionStatistics(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        logger.debug("Getting transaction statistics from {} to {}", startDate, endDate);
        
        Map<String, Object> statistics = transactionService.getTransactionStatistics(startDate, endDate);
        return ResponseEntity.ok(statistics);
    }
    
    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        Map<String, String> status = Map.of(
            "status", "UP",
            "service", "core-banking",
            "timestamp", LocalDateTime.now().toString()
        );
        return ResponseEntity.ok(status);
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