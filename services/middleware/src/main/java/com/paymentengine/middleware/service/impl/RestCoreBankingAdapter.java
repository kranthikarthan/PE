package com.paymentengine.middleware.service.impl;

import com.paymentengine.middleware.dto.corebanking.*;
import com.paymentengine.middleware.service.CoreBankingAdapter;
import com.paymentengine.middleware.client.ExternalCoreBankingClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * REST API Core Banking Adapter Implementation
 * 
 * This adapter integrates with external core banking systems via REST API
 * using ISO 20022 standards for payment processing.
 */
@Service("restCoreBankingAdapter")
public class RestCoreBankingAdapter implements CoreBankingAdapter {
    
    private static final Logger logger = LoggerFactory.getLogger(RestCoreBankingAdapter.class);
    
    @Autowired
    private ExternalCoreBankingClient externalCoreBankingClient;
    
    @Autowired
    private RestTemplate restTemplate;
    
    @Value("${core-banking.rest.base-url}")
    private String baseUrl;
    
    @Value("${core-banking.rest.timeout:30000}")
    private int timeout;
    
    @Value("${core-banking.rest.retry-attempts:3}")
    private int retryAttempts;
    
    @Override
    public String getAdapterType() {
        return "REST";
    }
    
    @Override
    public boolean isHealthy() {
        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(
                baseUrl + "/health", Map.class);
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            logger.error("Health check failed for REST Core Banking Adapter: {}", e.getMessage());
            return false;
        }
    }
    
    // ============================================================================
    // ACCOUNT MANAGEMENT
    // ============================================================================
    
    @Override
    @Retryable(value = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 1000))
    public AccountInfo getAccountInfo(String accountNumber, String tenantId) {
        logger.debug("Getting account info for account: {} in tenant: {}", accountNumber, tenantId);
        
        try {
            HttpHeaders headers = createHeaders(tenantId);
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            
            ResponseEntity<AccountInfo> response = restTemplate.exchange(
                baseUrl + "/api/v1/accounts/" + accountNumber,
                HttpMethod.GET,
                entity,
                AccountInfo.class
            );
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody();
            } else {
                throw new RuntimeException("Failed to get account info: " + response.getStatusCode());
            }
            
        } catch (Exception e) {
            logger.error("Error getting account info for account {}: {}", accountNumber, e.getMessage());
            throw new RuntimeException("Failed to get account info: " + e.getMessage(), e);
        }
    }
    
    @Override
    public boolean validateAccount(String accountNumber, String tenantId) {
        try {
            AccountInfo accountInfo = getAccountInfo(accountNumber, tenantId);
            return accountInfo != null && "ACTIVE".equals(accountInfo.getStatus());
        } catch (Exception e) {
            logger.error("Error validating account {}: {}", accountNumber, e.getMessage());
            return false;
        }
    }
    
    @Override
    public AccountBalance getAccountBalance(String accountNumber, String tenantId) {
        logger.debug("Getting account balance for account: {} in tenant: {}", accountNumber, tenantId);
        
        try {
            HttpHeaders headers = createHeaders(tenantId);
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            
            ResponseEntity<AccountBalance> response = restTemplate.exchange(
                baseUrl + "/api/v1/accounts/" + accountNumber + "/balance",
                HttpMethod.GET,
                entity,
                AccountBalance.class
            );
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody();
            } else {
                throw new RuntimeException("Failed to get account balance: " + response.getStatusCode());
            }
            
        } catch (Exception e) {
            logger.error("Error getting account balance for account {}: {}", accountNumber, e.getMessage());
            throw new RuntimeException("Failed to get account balance: " + e.getMessage(), e);
        }
    }
    
    @Override
    public boolean hasSufficientFunds(String accountNumber, BigDecimal amount, String currency, String tenantId) {
        try {
            AccountBalance balance = getAccountBalance(accountNumber, tenantId);
            return balance.getAvailableBalance().compareTo(amount) >= 0;
        } catch (Exception e) {
            logger.error("Error checking sufficient funds for account {}: {}", accountNumber, e.getMessage());
            return false;
        }
    }
    
    @Override
    public AccountHolder getAccountHolder(String accountNumber, String tenantId) {
        logger.debug("Getting account holder for account: {} in tenant: {}", accountNumber, tenantId);
        
        try {
            HttpHeaders headers = createHeaders(tenantId);
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            
            ResponseEntity<AccountHolder> response = restTemplate.exchange(
                baseUrl + "/api/v1/accounts/" + accountNumber + "/holder",
                HttpMethod.GET,
                entity,
                AccountHolder.class
            );
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody();
            } else {
                throw new RuntimeException("Failed to get account holder: " + response.getStatusCode());
            }
            
        } catch (Exception e) {
            logger.error("Error getting account holder for account {}: {}", accountNumber, e.getMessage());
            throw new RuntimeException("Failed to get account holder: " + e.getMessage(), e);
        }
    }
    
    // ============================================================================
    // TRANSACTION PROCESSING
    // ============================================================================
    
    @Override
    public TransactionResult processDebit(DebitTransactionRequest request) {
        logger.info("Processing debit transaction: {}", request.getTransactionReference());
        
        try {
            HttpHeaders headers = createHeaders(request.getTenantId());
            HttpEntity<DebitTransactionRequest> entity = new HttpEntity<>(request, headers);
            
            ResponseEntity<TransactionResult> response = restTemplate.exchange(
                baseUrl + "/api/v1/transactions/debit",
                HttpMethod.POST,
                entity,
                TransactionResult.class
            );
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody();
            } else {
                throw new RuntimeException("Failed to process debit: " + response.getStatusCode());
            }
            
        } catch (Exception e) {
            logger.error("Error processing debit transaction {}: {}", 
                        request.getTransactionReference(), e.getMessage());
            return createFailedTransactionResult(request.getTransactionReference(), e.getMessage());
        }
    }
    
    @Override
    public TransactionResult processCredit(CreditTransactionRequest request) {
        logger.info("Processing credit transaction: {}", request.getTransactionReference());
        
        try {
            HttpHeaders headers = createHeaders(request.getTenantId());
            HttpEntity<CreditTransactionRequest> entity = new HttpEntity<>(request, headers);
            
            ResponseEntity<TransactionResult> response = restTemplate.exchange(
                baseUrl + "/api/v1/transactions/credit",
                HttpMethod.POST,
                entity,
                TransactionResult.class
            );
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody();
            } else {
                throw new RuntimeException("Failed to process credit: " + response.getStatusCode());
            }
            
        } catch (Exception e) {
            logger.error("Error processing credit transaction {}: {}", 
                        request.getTransactionReference(), e.getMessage());
            return createFailedTransactionResult(request.getTransactionReference(), e.getMessage());
        }
    }
    
    @Override
    public TransactionResult processTransfer(TransferTransactionRequest request) {
        logger.info("Processing transfer transaction: {}", request.getTransactionReference());
        
        try {
            HttpHeaders headers = createHeaders(request.getTenantId());
            HttpEntity<TransferTransactionRequest> entity = new HttpEntity<>(request, headers);
            
            ResponseEntity<TransactionResult> response = restTemplate.exchange(
                baseUrl + "/api/v1/transactions/transfer",
                HttpMethod.POST,
                entity,
                TransactionResult.class
            );
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody();
            } else {
                throw new RuntimeException("Failed to process transfer: " + response.getStatusCode());
            }
            
        } catch (Exception e) {
            logger.error("Error processing transfer transaction {}: {}", 
                        request.getTransactionReference(), e.getMessage());
            return createFailedTransactionResult(request.getTransactionReference(), e.getMessage());
        }
    }
    
    // ============================================================================
    // PAYMENT ROUTING
    // ============================================================================
    
    @Override
    public PaymentRouting determinePaymentRouting(PaymentRoutingRequest request) {
        logger.debug("Determining payment routing for: {} to {}", 
                    request.getFromAccountNumber(), request.getToAccountNumber());
        
        try {
            // Check if both accounts belong to the same bank
            boolean isSameBank = isSameBankPayment(
                request.getFromAccountNumber(), 
                request.getToAccountNumber(), 
                request.getTenantId()
            );
            
            if (isSameBank) {
                return createSameBankRouting(request);
            } else {
                return createOtherBankRouting(request);
            }
            
        } catch (Exception e) {
            logger.error("Error determining payment routing: {}", e.getMessage());
            throw new RuntimeException("Failed to determine payment routing: " + e.getMessage(), e);
        }
    }
    
    @Override
    public boolean isSameBankPayment(String fromAccountNumber, String toAccountNumber, String tenantId) {
        try {
            AccountInfo fromAccount = getAccountInfo(fromAccountNumber, tenantId);
            AccountInfo toAccount = getAccountInfo(toAccountNumber, tenantId);
            
            return fromAccount.getBankCode().equals(toAccount.getBankCode());
        } catch (Exception e) {
            logger.error("Error checking same bank payment: {}", e.getMessage());
            return false;
        }
    }
    
    @Override
    public String getClearingSystemForPayment(String toAccountNumber, String paymentType, String tenantId) {
        try {
            HttpHeaders headers = createHeaders(tenantId);
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            
            ResponseEntity<Map> response = restTemplate.exchange(
                baseUrl + "/api/v1/routing/clearing-system?accountNumber=" + toAccountNumber + 
                "&paymentType=" + paymentType,
                HttpMethod.GET,
                entity,
                Map.class
            );
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> data = response.getBody();
                return (String) data.get("clearingSystemCode");
            } else {
                throw new RuntimeException("Failed to get clearing system: " + response.getStatusCode());
            }
            
        } catch (Exception e) {
            logger.error("Error getting clearing system for payment: {}", e.getMessage());
            throw new RuntimeException("Failed to get clearing system: " + e.getMessage(), e);
        }
    }
    
    @Override
    public String getLocalInstrumentationCode(String paymentType, String tenantId) {
        try {
            HttpHeaders headers = createHeaders(tenantId);
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            
            ResponseEntity<Map> response = restTemplate.exchange(
                baseUrl + "/api/v1/routing/local-instrument?paymentType=" + paymentType,
                HttpMethod.GET,
                entity,
                Map.class
            );
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> data = response.getBody();
                return (String) data.get("localInstrumentationCode");
            } else {
                throw new RuntimeException("Failed to get local instrumentation code: " + response.getStatusCode());
            }
            
        } catch (Exception e) {
            logger.error("Error getting local instrumentation code: {}", e.getMessage());
            throw new RuntimeException("Failed to get local instrumentation code: " + e.getMessage(), e);
        }
    }
    
    // ============================================================================
    // ISO 20022 INTEGRATION
    // ============================================================================
    
    @Override
    public Iso20022PaymentResult processIso20022Payment(Iso20022PaymentRequest request) {
        logger.info("Processing ISO 20022 payment: {}", request.getTransactionReference());
        
        try {
            HttpHeaders headers = createHeaders(request.getTenantId());
            headers.set("Content-Type", "application/xml");
            HttpEntity<Iso20022PaymentRequest> entity = new HttpEntity<>(request, headers);
            
            ResponseEntity<Iso20022PaymentResult> response = restTemplate.exchange(
                baseUrl + "/api/v1/iso20022/payment",
                HttpMethod.POST,
                entity,
                Iso20022PaymentResult.class
            );
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody();
            } else {
                throw new RuntimeException("Failed to process ISO 20022 payment: " + response.getStatusCode());
            }
            
        } catch (Exception e) {
            logger.error("Error processing ISO 20022 payment {}: {}", 
                        request.getTransactionReference(), e.getMessage());
            throw new RuntimeException("Failed to process ISO 20022 payment: " + e.getMessage(), e);
        }
    }
    
    @Override
    public Iso20022PaymentResponse generateIso20022Response(Iso20022ResponseRequest request) {
        logger.info("Generating ISO 20022 response: {}", request.getTransactionReference());
        
        try {
            HttpHeaders headers = createHeaders(request.getTenantId());
            headers.set("Content-Type", "application/xml");
            HttpEntity<Iso20022ResponseRequest> entity = new HttpEntity<>(request, headers);
            
            ResponseEntity<Iso20022PaymentResponse> response = restTemplate.exchange(
                baseUrl + "/api/v1/iso20022/response",
                HttpMethod.POST,
                entity,
                Iso20022PaymentResponse.class
            );
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody();
            } else {
                throw new RuntimeException("Failed to generate ISO 20022 response: " + response.getStatusCode());
            }
            
        } catch (Exception e) {
            logger.error("Error generating ISO 20022 response {}: {}", 
                        request.getTransactionReference(), e.getMessage());
            throw new RuntimeException("Failed to generate ISO 20022 response: " + e.getMessage(), e);
        }
    }
    
    @Override
    public boolean validateIso20022Message(String message, String messageType, String tenantId) {
        try {
            HttpHeaders headers = createHeaders(tenantId);
            headers.set("Content-Type", "application/xml");
            
            Map<String, String> request = Map.of(
                "message", message,
                "messageType", messageType
            );
            
            HttpEntity<Map<String, String>> entity = new HttpEntity<>(request, headers);
            
            ResponseEntity<Map> response = restTemplate.exchange(
                baseUrl + "/api/v1/iso20022/validate",
                HttpMethod.POST,
                entity,
                Map.class
            );
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> data = response.getBody();
                return Boolean.TRUE.equals(data.get("valid"));
            } else {
                return false;
            }
            
        } catch (Exception e) {
            logger.error("Error validating ISO 20022 message: {}", e.getMessage());
            return false;
        }
    }
    
    // ============================================================================
    // HELPER METHODS
    // ============================================================================
    
    private HttpHeaders createHeaders(String tenantId) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Tenant-ID", tenantId);
        headers.set("X-Request-ID", UUID.randomUUID().toString());
        headers.set("Content-Type", "application/json");
        return headers;
    }
    
    private PaymentRouting createSameBankRouting(PaymentRoutingRequest request) {
        PaymentRouting routing = new PaymentRouting(
            PaymentRouting.RoutingType.SAME_BANK,
            null,
            request.getLocalInstrumentationCode(),
            request.getPaymentType()
        );
        routing.setProcessingMode("SYNC");
        routing.setMessageFormat("JSON");
        routing.setDescription("Same bank payment - direct processing");
        return routing;
    }
    
    private PaymentRouting createOtherBankRouting(PaymentRoutingRequest request) {
        String clearingSystemCode = getClearingSystemForPayment(
            request.getToAccountNumber(), 
            request.getPaymentType(), 
            request.getTenantId()
        );
        
        PaymentRouting routing = new PaymentRouting(
            PaymentRouting.RoutingType.OTHER_BANK,
            clearingSystemCode,
            request.getLocalInstrumentationCode(),
            request.getPaymentType()
        );
        routing.setProcessingMode("ASYNC");
        routing.setMessageFormat("XML");
        routing.setDescription("Other bank payment via clearing system: " + clearingSystemCode);
        return routing;
    }
    
    private TransactionResult createFailedTransactionResult(String transactionReference, String errorMessage) {
        TransactionResult result = new TransactionResult();
        result.setTransactionReference(transactionReference);
        result.setStatus(TransactionResult.Status.FAILED);
        result.setStatusMessage("Transaction failed");
        result.setErrorMessage(errorMessage);
        result.setProcessedAt(LocalDateTime.now());
        return result;
    }
    
    // ============================================================================
    // NOT IMPLEMENTED METHODS (for REST adapter)
    // ============================================================================
    
    @Override
    public TransactionResult holdFunds(HoldFundsRequest request) {
        throw new UnsupportedOperationException("Hold funds not supported in REST adapter");
    }
    
    @Override
    public TransactionResult releaseFunds(ReleaseFundsRequest request) {
        throw new UnsupportedOperationException("Release funds not supported in REST adapter");
    }
    
    @Override
    public TransactionStatus getTransactionStatus(String transactionReference, String tenantId) {
        throw new UnsupportedOperationException("Transaction status not supported in REST adapter");
    }
    
    @Override
    public BatchTransactionResult processBatchTransactions(BatchTransactionRequest request) {
        throw new UnsupportedOperationException("Batch processing not supported in REST adapter");
    }
    
    @Override
    public BatchStatus getBatchStatus(String batchId, String tenantId) {
        throw new UnsupportedOperationException("Batch status not supported in REST adapter");
    }
    
    @Override
    public List<TransactionRecord> getTransactionsForReconciliation(ReconciliationRequest request) {
        throw new UnsupportedOperationException("Reconciliation not supported in REST adapter");
    }
    
    @Override
    public ReconciliationResult processReconciliation(ReconciliationRequest request) {
        throw new UnsupportedOperationException("Reconciliation not supported in REST adapter");
    }
    
    @Override
    public List<TransactionRecord> getTransactionHistory(TransactionHistoryRequest request) {
        throw new UnsupportedOperationException("Transaction history not supported in REST adapter");
    }
    
    @Override
    public AccountStatement getAccountStatement(AccountStatementRequest request) {
        throw new UnsupportedOperationException("Account statement not supported in REST adapter");
    }
    
    @Override
    public PaymentStatistics getPaymentStatistics(PaymentStatisticsRequest request) {
        throw new UnsupportedOperationException("Payment statistics not supported in REST adapter");
    }
}