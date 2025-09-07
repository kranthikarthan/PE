package com.paymentengine.paymentprocessing.service.impl;

import com.paymentengine.paymentprocessing.dto.corebanking.*;
import com.paymentengine.paymentprocessing.service.CoreBankingAdapter;
import com.paymentengine.paymentprocessing.service.AdvancedPayloadTransformationService;
import com.paymentengine.paymentprocessing.service.ResilientCoreBankingService;
import com.paymentengine.paymentprocessing.entity.AdvancedPayloadMapping;
import com.paymentengine.paymentprocessing.client.ExternalCoreBankingClient;
import com.paymentengine.paymentprocessing.exception.CoreBankingException;
import com.paymentengine.paymentprocessing.exception.AccountException;
import com.paymentengine.paymentprocessing.exception.ValidationException;
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
    
    @Autowired
    private AdvancedPayloadTransformationService advancedPayloadTransformationService;
    
    @Autowired
    private ResilientCoreBankingService resilientCoreBankingService;
    
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
        logger.info("Processing debit transaction: {} using advanced mapping and resiliency patterns", request.getTransactionReference());
        
        try {
            // Use resiliency service for the core banking call
            return resiliencyConfigurationService.executeResilientCall(
                    "core-banking-debit", 
                    request.getTenantId(),
                    () -> performDebitTransaction(request),
                    (exception) -> handleDebitFallback(request, exception)
            );
            
        } catch (Exception e) {
            logger.error("Error processing debit transaction {}: {}", 
                        request.getTransactionReference(), e.getMessage());
            return createFailedTransactionResult(request.getTransactionReference(), e.getMessage());
        }
    }
    
    /**
     * Perform the actual debit transaction
     */
    private TransactionResult performDebitTransaction(DebitTransactionRequest request) {
        // Convert request to map for advanced mapping
        Map<String, Object> requestMap = convertRequestToMap(request);
        
        // Try to use advanced mapping for core banking debit request transformation
        Optional<Map<String, Object>> transformedRequest = advancedPayloadTransformationService.transformPayload(
                request.getTenantId(), request.getPaymentType(), request.getLocalInstrumentCode(), null,
                AdvancedPayloadMapping.Direction.CORE_BANKING_DEBIT_REQUEST, requestMap);
        
        DebitTransactionRequest finalRequest = request;
        if (transformedRequest.isPresent()) {
            logger.debug("Using advanced mapping for core banking debit request transformation");
            finalRequest = convertMapToDebitRequest(transformedRequest.get(), request);
        } else {
            logger.debug("No advanced mapping found for core banking debit request, using original request");
        }
        
        HttpHeaders headers = createHeaders(request.getTenantId());
        HttpEntity<DebitTransactionRequest> entity = new HttpEntity<>(finalRequest, headers);
        
        ResponseEntity<TransactionResult> response = restTemplate.exchange(
            baseUrl + "/api/v1/transactions/debit",
            HttpMethod.POST,
            entity,
            TransactionResult.class
        );
        
        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            TransactionResult result = response.getBody();
            
            // Try to transform response using advanced mapping
            Map<String, Object> responseMap = convertResultToMap(result);
            Optional<Map<String, Object>> transformedResponse = advancedPayloadTransformationService.transformPayload(
                    request.getTenantId(), request.getPaymentType(), request.getLocalInstrumentCode(), null,
                    AdvancedPayloadMapping.Direction.CORE_BANKING_DEBIT_RESPONSE, responseMap);
            
            if (transformedResponse.isPresent()) {
                logger.debug("Using advanced mapping for core banking debit response transformation");
                result = convertMapToTransactionResult(transformedResponse.get(), result);
            }
            
            return result;
        } else {
            throw new RuntimeException("Failed to process debit: " + response.getStatusCode());
        }
    }
    
    /**
     * Handle debit transaction fallback when the call fails
     */
    private TransactionResult handleDebitFallback(DebitTransactionRequest request, Exception exception) {
        logger.warn("Using fallback response for debit transaction {} due to: {}", 
                   request.getTransactionReference(), exception.getMessage());
        
        // Create a fallback transaction result
        TransactionResult fallbackResult = new TransactionResult();
        fallbackResult.setTransactionReference(request.getTransactionReference());
        fallbackResult.setStatus(TransactionResult.Status.PENDING);
        fallbackResult.setStatusMessage("Transaction queued for manual processing due to core banking unavailability");
        fallbackResult.setErrorMessage("Core banking system unavailable: " + exception.getMessage());
        fallbackResult.setProcessedAt(LocalDateTime.now());
        
        // Add fallback metadata
        Map<String, Object> additionalData = new HashMap<>();
        additionalData.put("fallbackUsed", true);
        additionalData.put("fallbackReason", "Core banking system unavailable");
        additionalData.put("originalError", exception.getMessage());
        additionalData.put("requiresManualProcessing", true);
        additionalData.put("fallbackTimestamp", LocalDateTime.now().toString());
        fallbackResult.setAdditionalData(additionalData);
        
        return fallbackResult;
    }
    
    @Override
    public TransactionResult processCredit(CreditTransactionRequest request) {
        logger.info("Processing credit transaction: {} using advanced mapping and resiliency patterns", request.getTransactionReference());
        
        try {
            return resiliencyConfigurationService.executeResilientCall(
                    "core-banking-credit",
                    request.getTenantId(),
                    () -> performCreditTransaction(request),
                    (exception) -> handleCreditFallback(request, exception)
            );
        } catch (Exception e) {
            logger.error("Error processing credit transaction {}: {}", 
                        request.getTransactionReference(), e.getMessage());
            return createFailedTransactionResult(request.getTransactionReference(), e.getMessage());
        }
    }
    
    /**
     * Perform the actual credit transaction
     */
    private TransactionResult performCreditTransaction(CreditTransactionRequest request) {
        // Convert request to map for advanced mapping
        Map<String, Object> requestMap = convertCreditRequestToMap(request);
        
        // Try to use advanced mapping for core banking credit request transformation
        Optional<Map<String, Object>> transformedRequest = advancedPayloadTransformationService.transformPayload(
                request.getTenantId(), request.getPaymentType(), request.getLocalInstrumentCode(), null,
                AdvancedPayloadMapping.Direction.CORE_BANKING_CREDIT_REQUEST, requestMap);
        
        CreditTransactionRequest finalRequest = request;
        if (transformedRequest.isPresent()) {
            logger.debug("Using advanced mapping for core banking credit request transformation");
            finalRequest = convertMapToCreditRequest(transformedRequest.get(), request);
        } else {
            logger.debug("No advanced mapping found for core banking credit request, using original request");
        }
        
        HttpHeaders headers = createHeaders(request.getTenantId());
        HttpEntity<CreditTransactionRequest> entity = new HttpEntity<>(finalRequest, headers);
        
        ResponseEntity<TransactionResult> response = restTemplate.exchange(
            baseUrl + "/api/v1/transactions/credit",
            HttpMethod.POST,
            entity,
            TransactionResult.class
        );
        
        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            TransactionResult result = response.getBody();
            
            // Try to transform response using advanced mapping
            Map<String, Object> responseMap = convertResultToMap(result);
            Optional<Map<String, Object>> transformedResponse = advancedPayloadTransformationService.transformPayload(
                    request.getTenantId(), request.getPaymentType(), request.getLocalInstrumentCode(), null,
                    AdvancedPayloadMapping.Direction.CORE_BANKING_CREDIT_RESPONSE, responseMap);
            
            if (transformedResponse.isPresent()) {
                logger.debug("Using advanced mapping for core banking credit response transformation");
                result = convertMapToTransactionResult(transformedResponse.get(), result);
            }
            
            return result;
        } else {
            throw new RuntimeException("Failed to process credit: " + response.getStatusCode());
        }
    }
    
    /**
     * Handle credit transaction fallback when the call fails
     */
    private TransactionResult handleCreditFallback(CreditTransactionRequest request, Exception exception) {
        logger.warn("Using fallback response for credit transaction {} due to: {}", 
                   request.getTransactionReference(), exception.getMessage());
        
        // Create a fallback transaction result
        TransactionResult fallbackResult = new TransactionResult();
        fallbackResult.setTransactionReference(request.getTransactionReference());
        fallbackResult.setStatus(TransactionResult.Status.PENDING);
        fallbackResult.setStatusMessage("Transaction queued for manual processing due to core banking unavailability");
        fallbackResult.setErrorMessage("Core banking system unavailable: " + exception.getMessage());
        fallbackResult.setProcessedAt(LocalDateTime.now());
        
        // Add fallback metadata
        Map<String, Object> additionalData = new HashMap<>();
        additionalData.put("fallbackUsed", true);
        additionalData.put("fallbackReason", "Core banking system unavailable");
        additionalData.put("originalError", exception.getMessage());
        additionalData.put("requiresManualProcessing", true);
        additionalData.put("fallbackTimestamp", LocalDateTime.now().toString());
        fallbackResult.setAdditionalData(additionalData);
        
        return fallbackResult;
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
    // ADVANCED MAPPING HELPER METHODS
    // ============================================================================
    
    /**
     * Convert DebitTransactionRequest to Map for advanced mapping
     */
    private Map<String, Object> convertRequestToMap(DebitTransactionRequest request) {
        Map<String, Object> map = new HashMap<>();
        map.put("transactionReference", request.getTransactionReference());
        map.put("tenantId", request.getTenantId());
        map.put("accountNumber", request.getAccountNumber());
        map.put("amount", request.getAmount());
        map.put("currency", request.getCurrency());
        map.put("paymentType", request.getPaymentType());
        map.put("localInstrumentCode", request.getLocalInstrumentCode());
        map.put("description", request.getDescription());
        map.put("reference", request.getReference());
        map.put("valueDate", request.getValueDate());
        map.put("requestedExecutionDate", request.getRequestedExecutionDate());
        map.put("chargeBearer", request.getChargeBearer());
        map.put("remittanceInfo", request.getRemittanceInfo());
        map.put("additionalData", request.getAdditionalData());
        return map;
    }
    
    /**
     * Convert CreditTransactionRequest to Map for advanced mapping
     */
    private Map<String, Object> convertCreditRequestToMap(CreditTransactionRequest request) {
        Map<String, Object> map = new HashMap<>();
        map.put("transactionReference", request.getTransactionReference());
        map.put("tenantId", request.getTenantId());
        map.put("accountNumber", request.getAccountNumber());
        map.put("amount", request.getAmount());
        map.put("currency", request.getCurrency());
        map.put("paymentType", request.getPaymentType());
        map.put("localInstrumentCode", request.getLocalInstrumentCode());
        map.put("description", request.getDescription());
        map.put("reference", request.getReference());
        map.put("valueDate", request.getValueDate());
        map.put("requestedExecutionDate", request.getRequestedExecutionDate());
        map.put("chargeBearer", request.getChargeBearer());
        map.put("remittanceInfo", request.getRemittanceInfo());
        map.put("additionalData", request.getAdditionalData());
        return map;
    }
    
    /**
     * Convert Map back to DebitTransactionRequest
     */
    private DebitTransactionRequest convertMapToDebitRequest(Map<String, Object> map, DebitTransactionRequest original) {
        DebitTransactionRequest request = new DebitTransactionRequest();
        request.setTransactionReference((String) map.getOrDefault("transactionReference", original.getTransactionReference()));
        request.setTenantId((String) map.getOrDefault("tenantId", original.getTenantId()));
        request.setAccountNumber((String) map.getOrDefault("accountNumber", original.getAccountNumber()));
        request.setAmount((BigDecimal) map.getOrDefault("amount", original.getAmount()));
        request.setCurrency((String) map.getOrDefault("currency", original.getCurrency()));
        request.setPaymentType((String) map.getOrDefault("paymentType", original.getPaymentType()));
        request.setLocalInstrumentCode((String) map.getOrDefault("localInstrumentCode", original.getLocalInstrumentCode()));
        request.setDescription((String) map.getOrDefault("description", original.getDescription()));
        request.setReference((String) map.getOrDefault("reference", original.getReference()));
        request.setValueDate((LocalDateTime) map.getOrDefault("valueDate", original.getValueDate()));
        request.setRequestedExecutionDate((LocalDateTime) map.getOrDefault("requestedExecutionDate", original.getRequestedExecutionDate()));
        request.setChargeBearer((String) map.getOrDefault("chargeBearer", original.getChargeBearer()));
        request.setRemittanceInfo((String) map.getOrDefault("remittanceInfo", original.getRemittanceInfo()));
        request.setAdditionalData((Map<String, Object>) map.getOrDefault("additionalData", original.getAdditionalData()));
        return request;
    }
    
    /**
     * Convert Map back to CreditTransactionRequest
     */
    private CreditTransactionRequest convertMapToCreditRequest(Map<String, Object> map, CreditTransactionRequest original) {
        CreditTransactionRequest request = new CreditTransactionRequest();
        request.setTransactionReference((String) map.getOrDefault("transactionReference", original.getTransactionReference()));
        request.setTenantId((String) map.getOrDefault("tenantId", original.getTenantId()));
        request.setAccountNumber((String) map.getOrDefault("accountNumber", original.getAccountNumber()));
        request.setAmount((BigDecimal) map.getOrDefault("amount", original.getAmount()));
        request.setCurrency((String) map.getOrDefault("currency", original.getCurrency()));
        request.setPaymentType((String) map.getOrDefault("paymentType", original.getPaymentType()));
        request.setLocalInstrumentCode((String) map.getOrDefault("localInstrumentCode", original.getLocalInstrumentCode()));
        request.setDescription((String) map.getOrDefault("description", original.getDescription()));
        request.setReference((String) map.getOrDefault("reference", original.getReference()));
        request.setValueDate((LocalDateTime) map.getOrDefault("valueDate", original.getValueDate()));
        request.setRequestedExecutionDate((LocalDateTime) map.getOrDefault("requestedExecutionDate", original.getRequestedExecutionDate()));
        request.setChargeBearer((String) map.getOrDefault("chargeBearer", original.getChargeBearer()));
        request.setRemittanceInfo((String) map.getOrDefault("remittanceInfo", original.getRemittanceInfo()));
        request.setAdditionalData((Map<String, Object>) map.getOrDefault("additionalData", original.getAdditionalData()));
        return request;
    }
    
    /**
     * Convert TransactionResult to Map for advanced mapping
     */
    private Map<String, Object> convertResultToMap(TransactionResult result) {
        Map<String, Object> map = new HashMap<>();
        map.put("transactionReference", result.getTransactionReference());
        map.put("status", result.getStatus());
        map.put("statusMessage", result.getStatusMessage());
        map.put("errorMessage", result.getErrorMessage());
        map.put("processedAt", result.getProcessedAt());
        map.put("transactionId", result.getTransactionId());
        map.put("balanceAfter", result.getBalanceAfter());
        map.put("additionalData", result.getAdditionalData());
        return map;
    }
    
    /**
     * Convert Map back to TransactionResult
     */
    private TransactionResult convertMapToTransactionResult(Map<String, Object> map, TransactionResult original) {
        TransactionResult result = new TransactionResult();
        result.setTransactionReference((String) map.getOrDefault("transactionReference", original.getTransactionReference()));
        result.setStatus((TransactionResult.Status) map.getOrDefault("status", original.getStatus()));
        result.setStatusMessage((String) map.getOrDefault("statusMessage", original.getStatusMessage()));
        result.setErrorMessage((String) map.getOrDefault("errorMessage", original.getErrorMessage()));
        result.setProcessedAt((LocalDateTime) map.getOrDefault("processedAt", original.getProcessedAt()));
        result.setTransactionId((String) map.getOrDefault("transactionId", original.getTransactionId()));
        result.setBalanceAfter((BigDecimal) map.getOrDefault("balanceAfter", original.getBalanceAfter()));
        result.setAdditionalData((Map<String, Object>) map.getOrDefault("additionalData", original.getAdditionalData()));
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