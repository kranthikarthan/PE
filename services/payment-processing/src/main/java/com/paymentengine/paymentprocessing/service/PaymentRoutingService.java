package com.paymentengine.paymentprocessing.service;

import com.paymentengine.paymentprocessing.dto.corebanking.*;
import com.paymentengine.paymentprocessing.entity.CoreBankingConfiguration;
import com.paymentengine.paymentprocessing.entity.ClearingSystemConfiguration;
import com.paymentengine.paymentprocessing.repository.CoreBankingConfigurationRepository;
import com.paymentengine.paymentprocessing.repository.ClearingSystemConfigurationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Payment Routing Service
 * 
 * This service handles payment routing decisions based on:
 * - Same bank to same bank payments
 * - Same bank to other bank payments via clearing systems
 * - Other bank to same bank payments via clearing systems
 * - Tenant, payment type, and local instrumentation code configuration
 */
@Service
public class PaymentRoutingService {
    
    private static final Logger logger = LoggerFactory.getLogger(PaymentRoutingService.class);
    
    @Autowired
    private CoreBankingConfigurationRepository coreBankingConfigRepository;
    
    @Autowired
    private ClearingSystemConfigurationRepository clearingSystemConfigRepository;
    
    @Autowired
    @Qualifier("restCoreBankingAdapter")
    private CoreBankingAdapter restCoreBankingAdapter;
    
    @Autowired
    @Qualifier("grpcCoreBankingAdapter")
    private CoreBankingAdapter grpcCoreBankingAdapter;
    
    /**
     * Determine payment routing for a transaction
     */
    public PaymentRouting determinePaymentRouting(PaymentRoutingRequest request) {
        logger.info("Determining payment routing for transaction: {} from {} to {}", 
                   request.getTransactionReference(), 
                   request.getFromAccountNumber(), 
                   request.getToAccountNumber());
        
        try {
            // Get core banking configuration for tenant
            CoreBankingConfiguration coreBankingConfig = getCoreBankingConfiguration(request.getTenantId());
            if (coreBankingConfig == null) {
                throw new RuntimeException("Core banking configuration not found for tenant: " + request.getTenantId());
            }
            
            // Get the appropriate core banking adapter
            CoreBankingAdapter adapter = getCoreBankingAdapter(coreBankingConfig.getAdapterType());
            
            // Determine routing using the adapter
            PaymentRouting routing = adapter.determinePaymentRouting(request);
            
            // Enhance routing with configuration details
            enhanceRoutingWithConfiguration(routing, request, coreBankingConfig);
            
            logger.info("Payment routing determined: {} for transaction: {}", 
                       routing.getRoutingType(), request.getTransactionReference());
            
            return routing;
            
        } catch (Exception e) {
            logger.error("Error determining payment routing for transaction {}: {}", 
                        request.getTransactionReference(), e.getMessage());
            throw new RuntimeException("Failed to determine payment routing: " + e.getMessage(), e);
        }
    }
    
    /**
     * Process same bank to same bank payment
     */
    public TransactionResult processSameBankPayment(TransferTransactionRequest request) {
        logger.info("Processing same bank payment: {}", request.getTransactionReference());
        
        try {
            // Get core banking configuration
            CoreBankingConfiguration config = getCoreBankingConfiguration(request.getTenantId());
            CoreBankingAdapter adapter = getCoreBankingAdapter(config.getAdapterType());
            
            // Validate both accounts belong to same bank
            boolean isSameBank = adapter.isSameBankPayment(
                request.getFromAccountNumber(), 
                request.getToAccountNumber(), 
                request.getTenantId()
            );
            
            if (!isSameBank) {
                throw new RuntimeException("Accounts do not belong to the same bank");
            }
            
            // Process transfer directly through core banking
            TransactionResult result = adapter.processTransfer(request);
            
            logger.info("Same bank payment processed successfully: {}", request.getTransactionReference());
            return result;
            
        } catch (Exception e) {
            logger.error("Error processing same bank payment {}: {}", 
                        request.getTransactionReference(), e.getMessage());
            throw new RuntimeException("Failed to process same bank payment: " + e.getMessage(), e);
        }
    }
    
    /**
     * Process same bank to other bank payment via clearing system
     */
    public TransactionResult processOtherBankPayment(TransferTransactionRequest request) {
        logger.info("Processing other bank payment: {}", request.getTransactionReference());
        
        try {
            // Get core banking configuration
            CoreBankingConfiguration config = getCoreBankingConfiguration(request.getTenantId());
            CoreBankingAdapter adapter = getCoreBankingAdapter(config.getAdapterType());
            
            // Determine clearing system
            String clearingSystemCode = adapter.getClearingSystemForPayment(
                request.getToAccountNumber(), 
                request.getPaymentType(), 
                request.getTenantId()
            );
            
            // Get clearing system configuration
            ClearingSystemConfiguration clearingConfig = getClearingSystemConfiguration(
                clearingSystemCode, request.getTenantId());
            
            if (clearingConfig == null) {
                throw new RuntimeException("Clearing system configuration not found: " + clearingSystemCode);
            }
            
            // Process debit from source account
            DebitTransactionRequest debitRequest = new DebitTransactionRequest();
            debitRequest.setTransactionReference(request.getTransactionReference() + "-DEBIT");
            debitRequest.setAccountNumber(request.getFromAccountNumber());
            debitRequest.setAmount(request.getAmount());
            debitRequest.setCurrency(request.getCurrency());
            debitRequest.setDescription("Outbound payment to " + request.getToAccountNumber());
            debitRequest.setTenantId(request.getTenantId());
            
            TransactionResult debitResult = adapter.processDebit(debitRequest);
            
            if (!debitResult.isSuccess()) {
                throw new RuntimeException("Failed to debit source account: " + debitResult.getErrorMessage());
            }
            
            // Generate ISO 20022 message for clearing system
            Iso20022PaymentRequest isoRequest = createIso20022PaymentRequest(request, clearingConfig);
            Iso20022PaymentResult isoResult = adapter.processIso20022Payment(isoRequest);
            
            if (!"SUCCESS".equals(isoResult.getStatus())) {
                // Rollback debit if ISO 20022 processing fails
                rollbackDebit(debitRequest, adapter);
                throw new RuntimeException("Failed to process ISO 20022 payment: " + isoResult.getMessage());
            }
            
            // Create successful result
            TransactionResult result = new TransactionResult();
            result.setTransactionReference(request.getTransactionReference());
            result.setStatus(TransactionResult.Status.SUCCESS);
            result.setStatusMessage("Other bank payment processed successfully via clearing system: " + clearingSystemCode);
            result.setAmount(request.getAmount());
            result.setCurrency(request.getCurrency());
            result.setFromAccountNumber(request.getFromAccountNumber());
            result.setToAccountNumber(request.getToAccountNumber());
            result.setCoreBankingReference(isoResult.getTransactionReference());
            
            logger.info("Other bank payment processed successfully: {}", request.getTransactionReference());
            return result;
            
        } catch (Exception e) {
            logger.error("Error processing other bank payment {}: {}", 
                        request.getTransactionReference(), e.getMessage());
            throw new RuntimeException("Failed to process other bank payment: " + e.getMessage(), e);
        }
    }
    
    /**
     * Process incoming payment from other bank via clearing system
     */
    public TransactionResult processIncomingClearingPayment(Iso20022PaymentRequest request) {
        logger.info("Processing incoming clearing payment: {}", request.getTransactionReference());
        
        try {
            // Get core banking configuration
            CoreBankingConfiguration config = getCoreBankingConfiguration(request.getTenantId());
            CoreBankingAdapter adapter = getCoreBankingAdapter(config.getAdapterType());
            
            // Validate ISO 20022 message
            boolean isValid = adapter.validateIso20022Message(
                request.getIso20022Message(), 
                request.getMessageType(), 
                request.getTenantId()
            );
            
            if (!isValid) {
                throw new RuntimeException("Invalid ISO 20022 message");
            }
            
            // Process credit to destination account
            CreditTransactionRequest creditRequest = new CreditTransactionRequest();
            creditRequest.setTransactionReference(request.getTransactionReference() + "-CREDIT");
            creditRequest.setAccountNumber(request.getToAccountNumber());
            creditRequest.setAmount(request.getAmount());
            creditRequest.setCurrency(request.getCurrency());
            creditRequest.setDescription("Incoming payment from " + request.getFromAccountNumber());
            creditRequest.setTenantId(request.getTenantId());
            
            TransactionResult creditResult = adapter.processCredit(creditRequest);
            
            if (!creditResult.isSuccess()) {
                throw new RuntimeException("Failed to credit destination account: " + creditResult.getErrorMessage());
            }
            
            // Generate ISO 20022 response
            Iso20022ResponseRequest responseRequest = createIso20022ResponseRequest(request, creditResult);
            Iso20022PaymentResponse response = adapter.generateIso20022Response(responseRequest);
            
            // Create successful result
            TransactionResult result = new TransactionResult();
            result.setTransactionReference(request.getTransactionReference());
            result.setStatus(TransactionResult.Status.SUCCESS);
            result.setStatusMessage("Incoming clearing payment processed successfully");
            result.setAmount(request.getAmount());
            result.setCurrency(request.getCurrency());
            result.setFromAccountNumber(request.getFromAccountNumber());
            result.setToAccountNumber(request.getToAccountNumber());
            result.setCoreBankingReference(creditResult.getCoreBankingReference());
            
            logger.info("Incoming clearing payment processed successfully: {}", request.getTransactionReference());
            return result;
            
        } catch (Exception e) {
            logger.error("Error processing incoming clearing payment {}: {}", 
                        request.getTransactionReference(), e.getMessage());
            throw new RuntimeException("Failed to process incoming clearing payment: " + e.getMessage(), e);
        }
    }
    
    /**
     * Get local instrumentation code for payment type and tenant
     */
    public String getLocalInstrumentationCode(String paymentType, String tenantId) {
        try {
            CoreBankingConfiguration config = getCoreBankingConfiguration(tenantId);
            CoreBankingAdapter adapter = getCoreBankingAdapter(config.getAdapterType());
            
            return adapter.getLocalInstrumentationCode(paymentType, tenantId);
            
        } catch (Exception e) {
            logger.error("Error getting local instrumentation code for payment type {}: {}", 
                        paymentType, e.getMessage());
            throw new RuntimeException("Failed to get local instrumentation code: " + e.getMessage(), e);
        }
    }
    
    /**
     * Check if payment is same bank or other bank
     */
    public boolean isSameBankPayment(String fromAccountNumber, String toAccountNumber, String tenantId) {
        try {
            CoreBankingConfiguration config = getCoreBankingConfiguration(tenantId);
            CoreBankingAdapter adapter = getCoreBankingAdapter(config.getAdapterType());
            
            return adapter.isSameBankPayment(fromAccountNumber, toAccountNumber, tenantId);
            
        } catch (Exception e) {
            logger.error("Error checking same bank payment: {}", e.getMessage());
            return false;
        }
    }
    
    // ============================================================================
    // HELPER METHODS
    // ============================================================================
    
    private CoreBankingConfiguration getCoreBankingConfiguration(String tenantId) {
        Optional<CoreBankingConfiguration> config = coreBankingConfigRepository.findByTenantId(tenantId);
        if (config.isEmpty()) {
            throw new RuntimeException("Core banking configuration not found for tenant: " + tenantId);
        }
        return config.get();
    }
    
    private ClearingSystemConfiguration getClearingSystemConfiguration(String clearingSystemCode, String tenantId) {
        Optional<ClearingSystemConfiguration> config = clearingSystemConfigRepository
            .findByClearingSystemCodeAndTenantId(clearingSystemCode, tenantId);
        return config.orElse(null);
    }
    
    private CoreBankingAdapter getCoreBankingAdapter(String adapterType) {
        switch (adapterType.toUpperCase()) {
            case "REST":
                return restCoreBankingAdapter;
            case "GRPC":
                return grpcCoreBankingAdapter;
            default:
                throw new RuntimeException("Unsupported core banking adapter type: " + adapterType);
        }
    }
    
    private void enhanceRoutingWithConfiguration(PaymentRouting routing, PaymentRoutingRequest request, 
                                               CoreBankingConfiguration config) {
        // Set processing mode from configuration
        routing.setProcessingMode(config.getProcessingMode());
        
        // Set message format from configuration
        routing.setMessageFormat(config.getMessageFormat());
        
        // Set authentication method
        routing.setAuthenticationMethod(config.getAuthenticationMethod());
        
        // Set priority
        routing.setPriority(config.getPriority());
        
        // Add description
        routing.setDescription(routing.getDescription() + " (via " + config.getAdapterType() + " adapter)");
    }
    
    private Iso20022PaymentRequest createIso20022PaymentRequest(TransferTransactionRequest request, 
                                                              ClearingSystemConfiguration clearingConfig) {
        Iso20022PaymentRequest isoRequest = new Iso20022PaymentRequest();
        isoRequest.setTransactionReference(request.getTransactionReference());
        isoRequest.setFromAccountNumber(request.getFromAccountNumber());
        isoRequest.setToAccountNumber(request.getToAccountNumber());
        isoRequest.setAmount(request.getAmount());
        isoRequest.setCurrency(request.getCurrency());
        isoRequest.setPaymentType(request.getPaymentType());
        isoRequest.setMessageType("pain.001");
        isoRequest.setClearingSystemCode(clearingConfig.getClearingSystemCode());
        isoRequest.setTenantId(request.getTenantId());
        
        // Generate ISO 20022 message (this would be done by a proper ISO 20022 service)
        String isoMessage = generateIso20022Message(isoRequest);
        isoRequest.setIso20022Message(isoMessage);
        
        return isoRequest;
    }
    
    private Iso20022ResponseRequest createIso20022ResponseRequest(Iso20022PaymentRequest request, 
                                                               TransactionResult result) {
        Iso20022ResponseRequest responseRequest = new Iso20022ResponseRequest();
        responseRequest.setTransactionReference(request.getTransactionReference());
        responseRequest.setOriginalMessageId(request.getTransactionReference());
        responseRequest.setStatus(result.getStatus().name());
        responseRequest.setMessageType("pain.002");
        responseRequest.setTenantId(request.getTenantId());
        
        // Generate ISO 20022 response message
        String responseMessage = generateIso20022ResponseMessage(responseRequest);
        responseRequest.setIso20022Message(responseMessage);
        
        return responseRequest;
    }
    
    private String generateIso20022Message(Iso20022PaymentRequest request) {
        // This would be implemented by a proper ISO 20022 message generator
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
               "<Document xmlns=\"urn:iso:std:iso:20022:tech:xsd:pain.001.001.03\">" +
               "<CstmrCdtTrfInitn>" +
               "<GrpHdr>" +
               "<MsgId>" + request.getTransactionReference() + "</MsgId>" +
               "<CreDtTm>" + java.time.Instant.now().toString() + "</CreDtTm>" +
               "<NbOfTxs>1</NbOfTxs>" +
               "</GrpHdr>" +
               "<CdtTrfTxInf>" +
               "<PmtId><TxId>" + request.getTransactionReference() + "</TxId></PmtId>" +
               "<Amt><InstdAmt Ccy=\"" + request.getCurrency() + "\">" + request.getAmount() + "</InstdAmt></Amt>" +
               "<Dbtr><Nm>Debtor</Nm></Dbtr>" +
               "<DbtrAcct><Id><IBAN>" + request.getFromAccountNumber() + "</IBAN></Id></DbtrAcct>" +
               "<Cdtr><Nm>Creditor</Nm></Cdtr>" +
               "<CdtrAcct><Id><IBAN>" + request.getToAccountNumber() + "</IBAN></Id></CdtrAcct>" +
               "</CdtTrfTxInf>" +
               "</CstmrCdtTrfInitn>" +
               "</Document>";
    }
    
    private String generateIso20022ResponseMessage(Iso20022ResponseRequest request) {
        // This would be implemented by a proper ISO 20022 response message generator
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
               "<Document xmlns=\"urn:iso:std:iso:20022:tech:xsd:pain.002.001.03\">" +
               "<CstmrPmtStsRpt>" +
               "<GrpHdr>" +
               "<MsgId>" + request.getTransactionReference() + "</MsgId>" +
               "<CreDtTm>" + java.time.Instant.now().toString() + "</CreDtTm>" +
               "<NbOfTxs>1</NbOfTxs>" +
               "</GrpHdr>" +
               "<OrgnlGrpInfAndSts>" +
               "<OrgnlMsgId>" + request.getOriginalMessageId() + "</OrgnlMsgId>" +
               "<OrgnlMsgNmId>pain.001.001.03</OrgnlMsgNmId>" +
               "<GrpSts>" + request.getStatus() + "</GrpSts>" +
               "</OrgnlGrpInfAndSts>" +
               "</CstmrPmtStsRpt>" +
               "</Document>";
    }
    
    private void rollbackDebit(DebitTransactionRequest debitRequest, CoreBankingAdapter adapter) {
        try {
            // Create credit request to rollback the debit
            CreditTransactionRequest rollbackRequest = new CreditTransactionRequest();
            rollbackRequest.setTransactionReference(debitRequest.getTransactionReference() + "-ROLLBACK");
            rollbackRequest.setAccountNumber(debitRequest.getAccountNumber());
            rollbackRequest.setAmount(debitRequest.getAmount());
            rollbackRequest.setCurrency(debitRequest.getCurrency());
            rollbackRequest.setDescription("Rollback for failed payment");
            rollbackRequest.setTenantId(debitRequest.getTenantId());
            
            adapter.processCredit(rollbackRequest);
            logger.info("Successfully rolled back debit for transaction: {}", debitRequest.getTransactionReference());
            
        } catch (Exception e) {
            logger.error("Failed to rollback debit for transaction {}: {}", 
                        debitRequest.getTransactionReference(), e.getMessage());
        }
    }
}