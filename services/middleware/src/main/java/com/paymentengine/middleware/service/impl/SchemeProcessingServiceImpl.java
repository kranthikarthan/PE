package com.paymentengine.middleware.service.impl;

import com.paymentengine.middleware.service.*;
import com.paymentengine.middleware.dto.corebanking.*;
import com.paymentengine.middleware.entity.CoreBankingConfiguration;
import com.paymentengine.middleware.repository.CoreBankingConfigurationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * Implementation of scheme processing service
 */
@Service
public class SchemeProcessingServiceImpl implements SchemeProcessingService {
    
    private static final Logger logger = LoggerFactory.getLogger(SchemeProcessingServiceImpl.class);
    private static final DateTimeFormatter ISO_DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
    
    private final ClearingSystemRoutingService clearingSystemRoutingService;
    private final Pain001ToPacs008TransformationService transformationService;
    private final SchemeMessageService schemeMessageService;
    private final Iso20022FormatService iso20022FormatService;
    private final PaymentRoutingService paymentRoutingService;
    private final CoreBankingConfigurationRepository coreBankingConfigRepository;
    
    @Autowired
    public SchemeProcessingServiceImpl(
            ClearingSystemRoutingService clearingSystemRoutingService,
            Pain001ToPacs008TransformationService transformationService,
            SchemeMessageService schemeMessageService,
            Iso20022FormatService iso20022FormatService,
            PaymentRoutingService paymentRoutingService,
            CoreBankingConfigurationRepository coreBankingConfigRepository) {
        this.clearingSystemRoutingService = clearingSystemRoutingService;
        this.transformationService = transformationService;
        this.schemeMessageService = schemeMessageService;
        this.iso20022FormatService = iso20022FormatService;
        this.paymentRoutingService = paymentRoutingService;
        this.coreBankingConfigRepository = coreBankingConfigRepository;
    }
    
    @Override
    public CompletableFuture<SchemeProcessingResult> processPain001ThroughScheme(
            Map<String, Object> pain001Message,
            String tenantId,
            String paymentType,
            String localInstrumentCode,
            String responseMode) {
        
        logger.info("Processing PAIN.001 through scheme for tenant: {}, paymentType: {}, localInstrument: {}", 
                tenantId, paymentType, localInstrumentCode);
        
        return CompletableFuture.supplyAsync(() -> {
            long startTime = System.currentTimeMillis();
            String messageId = null;
            String correlationId = null;
            String clearingSystemCode = null;
            String transactionId = null;
            
            try {
                // 1. Extract message information
                Pain001ToPacs008TransformationService.PaymentInfo paymentInfo = 
                        transformationService.extractPaymentInfo(pain001Message);
                messageId = paymentInfo.getMessageId();
                correlationId = "CORR-" + System.currentTimeMillis();
                
                // 2. Determine clearing system
                ClearingSystemRoutingService.ClearingSystemRoute route = 
                        clearingSystemRoutingService.routeMessage(tenantId, paymentType, localInstrumentCode, "pacs008");
                clearingSystemCode = route.getClearingSystemCode();
                
                logger.info("Routing PAIN.001 to clearing system: {} for messageId: {}", clearingSystemCode, messageId);
                
                // 3. Transform PAIN.001 to PACS.008
                Map<String, Object> pacs008Message = transformationService.transformPain001ToPacs008(
                        pain001Message, tenantId, paymentType, localInstrumentCode);
                
                // 4. Send PACS.008 to clearing system
                CompletableFuture<Map<String, Object>> clearingSystemResponse = sendMessageToClearingSystem(
                        pacs008Message, clearingSystemCode, "pacs008", route.getSchemeConfigurationId());
                
                // 5. Process clearing system response
                Map<String, Object> response = clearingSystemResponse.get();
                Map<String, Object> processedResponse = processClearingSystemResponse(response, "pacs002");
                
                // 6. Generate PACS.002 acknowledgment
                Map<String, Object> pacs002Response = generatePacs002Response(
                        messageId, transactionId, "ACSC", "G000"); // Accepted Settlement Completed
                
                // 7. Generate PAIN.002 response to client
                Map<String, Object> pain002Response = generatePain002Response(
                        messageId, transactionId, "ACSC", "G000", responseMode);
                
                long processingTime = System.currentTimeMillis() - startTime;
                
                logger.info("Successfully processed PAIN.001 through scheme: {} in {}ms", messageId, processingTime);
                
                return new SchemeProcessingResult(
                        messageId, correlationId, "SUCCESS", clearingSystemCode, transactionId,
                        pacs008Message, pacs002Response, pain002Response, null, processingTime
                );
                
            } catch (Exception e) {
                long processingTime = System.currentTimeMillis() - startTime;
                logger.error("Error processing PAIN.001 through scheme: {}", e.getMessage());
                
                // Generate error responses
                Map<String, Object> pacs002Error = generatePacs002Response(
                        messageId, transactionId, "RJCT", "NARR"); // Rejected
                Map<String, Object> pain002Error = generatePain002Response(
                        messageId, transactionId, "RJCT", "NARR", responseMode);
                
                return new SchemeProcessingResult(
                        messageId, correlationId, "ERROR", clearingSystemCode, transactionId,
                        null, pacs002Error, pain002Error, e.getMessage(), processingTime
                );
            }
        });
    }
    
    @Override
    public Map<String, Object> processPacs008FromScheme(Map<String, Object> pacs008Message, String tenantId) {
        logger.info("Processing PACS.008 from scheme for tenant: {}", tenantId);
        
        try {
            // Extract PACS.008 information
            Map<String, Object> fiToFI = (Map<String, Object>) pacs008Message.get("FIToFICustomerCreditTransfer");
            Map<String, Object> grpHdr = (Map<String, Object>) fiToFI.get("GrpHdr");
            Map<String, Object> cdtTrfTxInf = (Map<String, Object>) fiToFI.get("CdtTrfTxInf");
            
            String messageId = (String) grpHdr.get("MsgId");
            String endToEndId = (String) ((Map<String, Object>) cdtTrfTxInf.get("PmtId")).get("EndToEndId");
            
            // Process the incoming payment (this would typically create a transaction in core banking)
            String transactionId = "TXN-" + System.currentTimeMillis();
            
            logger.info("Processed PACS.008 from scheme: {} -> transaction: {}", messageId, transactionId);
            
            // Generate PACS.002 acknowledgment
            return generatePacs002Response(messageId, transactionId, "ACSC", "G000");
            
        } catch (Exception e) {
            logger.error("Error processing PACS.008 from scheme: {}", e.getMessage());
            throw new RuntimeException("Failed to process PACS.008 from scheme", e);
        }
    }
    
    @Override
    public Map<String, Object> generatePacs002Response(String originalMessageId, String transactionId, String status, String reasonCode) {
        logger.debug("Generating PACS.002 response for messageId: {}, status: {}", originalMessageId, status);
        
        try {
            String pacs002MessageId = "PACS002-" + System.currentTimeMillis();
            String currentDateTime = LocalDateTime.now().format(ISO_DATETIME_FORMATTER);
            
            Map<String, Object> pacs002 = new HashMap<>();
            Map<String, Object> fiToFIPaymentStatusReport = new HashMap<>();
            
            // Group Header
            Map<String, Object> grpHdr = new HashMap<>();
            grpHdr.put("MsgId", pacs002MessageId);
            grpHdr.put("CreDtTm", currentDateTime);
            grpHdr.put("NbOfTxs", "1");
            
            // Instructing Agent (Our Bank)
            Map<String, Object> instgAgt = new HashMap<>();
            Map<String, Object> instgAgtFinInstnId = new HashMap<>();
            instgAgtFinInstnId.put("BICFI", "PAYMENTUS33XXX");
            instgAgtFinInstnId.put("Nm", "Payment Engine Bank");
            instgAgt.put("FinInstnId", instgAgtFinInstnId);
            grpHdr.put("InstgAgt", instgAgt);
            
            // Instructed Agent (Clearing System)
            Map<String, Object> instdAgt = new HashMap<>();
            Map<String, Object> instdAgtFinInstnId = new HashMap<>();
            instdAgtFinInstnId.put("BICFI", "SCHEMEFI33XXX");
            instdAgtFinInstnId.put("Nm", "Payment Scheme");
            instdAgt.put("FinInstnId", instdAgtFinInstnId);
            grpHdr.put("InstdAgt", instdAgt);
            
            fiToFIPaymentStatusReport.put("GrpHdr", grpHdr);
            
            // Original Group Information and Status
            Map<String, Object> orgnlGrpInfAndSts = new HashMap<>();
            orgnlGrpInfAndSts.put("OrgnlMsgId", originalMessageId);
            orgnlGrpInfAndSts.put("OrgnlMsgNmId", "pacs.008.001.03");
            orgnlGrpInfAndSts.put("OrgnlCreDtTm", currentDateTime);
            orgnlGrpInfAndSts.put("GrpSts", status);
            fiToFIPaymentStatusReport.put("OrgnlGrpInfAndSts", orgnlGrpInfAndSts);
            
            // Transaction Information and Status
            Map<String, Object> txInfAndSts = new HashMap<>();
            txInfAndSts.put("StsId", "STS-" + System.currentTimeMillis());
            txInfAndSts.put("OrgnlEndToEndId", "E2E-" + System.currentTimeMillis());
            txInfAndSts.put("TxSts", status);
            txInfAndSts.put("AccptncDtTm", currentDateTime);
            
            // Status Reason Information
            Map<String, Object> stsRsnInf = new HashMap<>();
            Map<String, Object> rsn = new HashMap<>();
            rsn.put("Cd", reasonCode);
            stsRsnInf.put("Rsn", rsn);
            txInfAndSts.put("StsRsnInf", stsRsnInf);
            
            fiToFIPaymentStatusReport.put("TxInfAndSts", txInfAndSts);
            pacs002.put("FIToFIPaymentStatusReport", fiToFIPaymentStatusReport);
            
            // Add metadata
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("originalMessageId", originalMessageId);
            metadata.put("transactionId", transactionId);
            metadata.put("status", status);
            metadata.put("reasonCode", reasonCode);
            metadata.put("generationTimestamp", currentDateTime);
            pacs002.put("_metadata", metadata);
            
            logger.debug("Generated PACS.002 response: {} for original message: {}", pacs002MessageId, originalMessageId);
            
            return pacs002;
            
        } catch (Exception e) {
            logger.error("Error generating PACS.002 response: {}", e.getMessage());
            throw new RuntimeException("Failed to generate PACS.002 response", e);
        }
    }
    
    @Override
    public Map<String, Object> generatePain002Response(String originalMessageId, String transactionId, String status, String reasonCode, String responseMode) {
        logger.debug("Generating PAIN.002 response for messageId: {}, status: {}", originalMessageId, status);
        
        try {
            String pain002MessageId = "PAIN002-" + System.currentTimeMillis();
            String currentDateTime = LocalDateTime.now().format(ISO_DATETIME_FORMATTER);
            
            Map<String, Object> pain002 = new HashMap<>();
            Map<String, Object> cstmrPmtStsRpt = new HashMap<>();
            
            // Group Header
            Map<String, Object> grpHdr = new HashMap<>();
            grpHdr.put("MsgId", pain002MessageId);
            grpHdr.put("CreDtTm", currentDateTime);
            grpHdr.put("NbOfTxs", "1");
            
            // Initiating Party
            Map<String, Object> initgPty = new HashMap<>();
            initgPty.put("Nm", "Payment Engine Bank");
            grpHdr.put("InitgPty", initgPty);
            
            cstmrPmtStsRpt.put("GrpHdr", grpHdr);
            
            // Original Group Information and Status
            Map<String, Object> orgnlGrpInfAndSts = new HashMap<>();
            orgnlGrpInfAndSts.put("OrgnlMsgId", originalMessageId);
            orgnlGrpInfAndSts.put("OrgnlMsgNmId", "pain.001.001.03");
            orgnlGrpInfAndSts.put("OrgnlCreDtTm", currentDateTime);
            orgnlGrpInfAndSts.put("GrpSts", status);
            cstmrPmtStsRpt.put("OrgnlGrpInfAndSts", orgnlGrpInfAndSts);
            
            // Payment Information Status
            Map<String, Object> pmtInfSts = new HashMap<>();
            pmtInfSts.put("PmtInfId", "PMT-" + System.currentTimeMillis());
            pmtInfSts.put("PmtInfSts", status);
            
            // Transaction Information and Status
            Map<String, Object> txInfAndSts = new HashMap<>();
            txInfAndSts.put("StsId", "STS-" + System.currentTimeMillis());
            txInfAndSts.put("OrgnlInstrId", "INSTR-" + System.currentTimeMillis());
            txInfAndSts.put("OrgnlEndToEndId", "E2E-" + System.currentTimeMillis());
            txInfAndSts.put("TxSts", status);
            txInfAndSts.put("AccptncDtTm", currentDateTime);
            
            // Status Reason Information
            Map<String, Object> stsRsnInf = new HashMap<>();
            Map<String, Object> rsn = new HashMap<>();
            rsn.put("Cd", reasonCode);
            stsRsnInf.put("Rsn", rsn);
            txInfAndSts.put("StsRsnInf", stsRsnInf);
            
            pmtInfSts.put("TxInfAndSts", txInfAndSts);
            cstmrPmtStsRpt.put("PmtInfSts", pmtInfSts);
            
            pain002.put("CstmrPmtStsRpt", cstmrPmtStsRpt);
            
            // Add metadata
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("originalMessageId", originalMessageId);
            metadata.put("transactionId", transactionId);
            metadata.put("status", status);
            metadata.put("reasonCode", reasonCode);
            metadata.put("responseMode", responseMode);
            metadata.put("generationTimestamp", currentDateTime);
            pain002.put("_metadata", metadata);
            
            logger.debug("Generated PAIN.002 response: {} for original message: {}", pain002MessageId, originalMessageId);
            
            return pain002;
            
        } catch (Exception e) {
            logger.error("Error generating PAIN.002 response: {}", e.getMessage());
            throw new RuntimeException("Failed to generate PAIN.002 response", e);
        }
    }
    
    @Override
    public CompletableFuture<Map<String, Object>> sendMessageToClearingSystem(
            Map<String, Object> message,
            String clearingSystemCode,
            String messageType,
            String schemeConfigurationId) {
        
        logger.info("Sending {} message to clearing system: {}", messageType, clearingSystemCode);
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Get clearing system configuration
                ClearingSystemRoutingService.ClearingSystemConfig config = 
                        clearingSystemRoutingService.getClearingSystemConfig(clearingSystemCode);
                
                // Create scheme message request
                com.paymentengine.middleware.dto.SchemeMessageRequest request = 
                        new com.paymentengine.middleware.dto.SchemeMessageRequest(
                                messageType,
                                "MSG-" + System.currentTimeMillis(),
                                "CORR-" + System.currentTimeMillis(),
                                com.paymentengine.middleware.dto.SchemeConfigRequest.MessageFormat.JSON,
                                com.paymentengine.middleware.dto.SchemeConfigRequest.InteractionMode.SYNCHRONOUS,
                                message,
                                Map.of(
                                        "clearingSystemCode", clearingSystemCode,
                                        "schemeConfigurationId", schemeConfigurationId,
                                        "endpointUrl", config.getEndpointUrl()
                                )
                        );
                
                // Send message using scheme message service
                com.paymentengine.middleware.dto.SchemeMessageResponse response = 
                        schemeMessageService.sendMessage(schemeConfigurationId, request);
                
                logger.info("Sent {} message to clearing system: {} - Status: {}", 
                        messageType, clearingSystemCode, response.getStatus());
                
                return Map.of(
                        "status", response.getStatus(),
                        "responseCode", response.getResponseCode(),
                        "responseMessage", response.getResponseMessage(),
                        "payload", response.getPayload(),
                        "processingTimeMs", response.getProcessingTimeMs(),
                        "timestamp", response.getTimestamp()
                );
                
            } catch (Exception e) {
                logger.error("Error sending message to clearing system: {}", e.getMessage());
                throw new RuntimeException("Failed to send message to clearing system", e);
            }
        });
    }
    
    @Override
    public Map<String, Object> processClearingSystemResponse(Map<String, Object> response, String messageType) {
        logger.debug("Processing clearing system response for messageType: {}", messageType);
        
        try {
            String status = (String) response.get("status");
            String responseCode = (String) response.get("responseCode");
            String responseMessage = (String) response.get("responseMessage");
            
            // Process based on message type
            if ("pacs002".equals(messageType)) {
                // Process PACS.002 response from clearing system
                return Map.of(
                        "messageType", "pacs002",
                        "status", status,
                        "responseCode", responseCode,
                        "responseMessage", responseMessage,
                        "processedAt", LocalDateTime.now().format(ISO_DATETIME_FORMATTER)
                );
            } else {
                // Generic processing
                return Map.of(
                        "messageType", messageType,
                        "status", status,
                        "responseCode", responseCode,
                        "responseMessage", responseMessage,
                        "processedAt", LocalDateTime.now().format(ISO_DATETIME_FORMATTER)
                );
            }
            
        } catch (Exception e) {
            logger.error("Error processing clearing system response: {}", e.getMessage());
            throw new RuntimeException("Failed to process clearing system response", e);
        }
    }
    
    /**
     * Process payment with core banking integration
     */
    public CompletableFuture<SchemeProcessingResult> processPaymentWithCoreBanking(
            Map<String, Object> paymentRequest,
            String tenantId,
            String paymentType,
            String localInstrumentCode) {
        
        logger.info("Processing payment with core banking integration for tenant: {}, paymentType: {}", 
                   tenantId, paymentType);
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Extract payment details from request
                String fromAccountNumber = (String) paymentRequest.get("fromAccountNumber");
                String toAccountNumber = (String) paymentRequest.get("toAccountNumber");
                BigDecimal amount = new BigDecimal(paymentRequest.get("amount").toString());
                String currency = (String) paymentRequest.get("currency");
                String transactionReference = (String) paymentRequest.get("transactionReference");
                
                // Create payment routing request
                PaymentRoutingRequest routingRequest = new PaymentRoutingRequest();
                routingRequest.setFromAccountNumber(fromAccountNumber);
                routingRequest.setToAccountNumber(toAccountNumber);
                routingRequest.setPaymentType(paymentType);
                routingRequest.setLocalInstrumentationCode(localInstrumentCode);
                routingRequest.setAmount(amount);
                routingRequest.setCurrency(currency);
                routingRequest.setTenantId(tenantId);
                routingRequest.setTransactionReference(transactionReference);
                
                // Determine payment routing
                PaymentRouting routing = paymentRoutingService.determinePaymentRouting(routingRequest);
                
                // Process based on routing type
                TransactionResult result;
                switch (routing.getRoutingType()) {
                    case SAME_BANK:
                        result = processSameBankPayment(paymentRequest, tenantId);
                        break;
                    case OTHER_BANK:
                        result = processOtherBankPayment(paymentRequest, tenantId, routing);
                        break;
                    case INCOMING_CLEARING:
                        result = processIncomingClearingPayment(paymentRequest, tenantId);
                        break;
                    default:
                        throw new RuntimeException("Unsupported routing type: " + routing.getRoutingType());
                }
                
                // Create scheme processing result
                SchemeProcessingResult schemeResult = new SchemeProcessingResult();
                schemeResult.setSuccess(result.isSuccess());
                schemeResult.setTransactionReference(result.getTransactionReference());
                schemeResult.setStatus(result.getStatus().name());
                schemeResult.setMessage(result.getStatusMessage());
                schemeResult.setRoutingType(routing.getRoutingType().name());
                schemeResult.setClearingSystemCode(routing.getClearingSystemCode());
                schemeResult.setProcessedAt(LocalDateTime.now());
                
                if (result.isFailed()) {
                    schemeResult.setErrorCode(result.getErrorCode());
                    schemeResult.setErrorMessage(result.getErrorMessage());
                }
                
                logger.info("Payment processed successfully with core banking: {}", result.getTransactionReference());
                return schemeResult;
                
            } catch (Exception e) {
                logger.error("Error processing payment with core banking: {}", e.getMessage(), e);
                
                SchemeProcessingResult errorResult = new SchemeProcessingResult();
                errorResult.setSuccess(false);
                errorResult.setStatus("FAILED");
                errorResult.setMessage("Payment processing failed");
                errorResult.setErrorMessage(e.getMessage());
                errorResult.setProcessedAt(LocalDateTime.now());
                
                return errorResult;
            }
        });
    }
    
    /**
     * Process same bank payment
     */
    private TransactionResult processSameBankPayment(Map<String, Object> paymentRequest, String tenantId) {
        // Create transfer request
        TransferTransactionRequest transferRequest = new TransferTransactionRequest();
        transferRequest.setTransactionReference((String) paymentRequest.get("transactionReference"));
        transferRequest.setFromAccountNumber((String) paymentRequest.get("fromAccountNumber"));
        transferRequest.setToAccountNumber((String) paymentRequest.get("toAccountNumber"));
        transferRequest.setAmount(new BigDecimal(paymentRequest.get("amount").toString()));
        transferRequest.setCurrency((String) paymentRequest.get("currency"));
        transferRequest.setPaymentType((String) paymentRequest.get("paymentType"));
        transferRequest.setDescription((String) paymentRequest.get("description"));
        transferRequest.setTenantId(tenantId);
        
        return paymentRoutingService.processSameBankPayment(transferRequest);
    }
    
    /**
     * Process other bank payment via clearing system
     */
    private TransactionResult processOtherBankPayment(Map<String, Object> paymentRequest, String tenantId, PaymentRouting routing) {
        // Create transfer request
        TransferTransactionRequest transferRequest = new TransferTransactionRequest();
        transferRequest.setTransactionReference((String) paymentRequest.get("transactionReference"));
        transferRequest.setFromAccountNumber((String) paymentRequest.get("fromAccountNumber"));
        transferRequest.setToAccountNumber((String) paymentRequest.get("toAccountNumber"));
        transferRequest.setAmount(new BigDecimal(paymentRequest.get("amount").toString()));
        transferRequest.setCurrency((String) paymentRequest.get("currency"));
        transferRequest.setPaymentType((String) paymentRequest.get("paymentType"));
        transferRequest.setDescription((String) paymentRequest.get("description"));
        transferRequest.setTenantId(tenantId);
        
        return paymentRoutingService.processOtherBankPayment(transferRequest);
    }
    
    /**
     * Process incoming clearing payment
     */
    private TransactionResult processIncomingClearingPayment(Map<String, Object> paymentRequest, String tenantId) {
        // Create ISO 20022 payment request
        Iso20022PaymentRequest isoRequest = new Iso20022PaymentRequest();
        isoRequest.setTransactionReference((String) paymentRequest.get("transactionReference"));
        isoRequest.setFromAccountNumber((String) paymentRequest.get("fromAccountNumber"));
        isoRequest.setToAccountNumber((String) paymentRequest.get("toAccountNumber"));
        isoRequest.setAmount(new BigDecimal(paymentRequest.get("amount").toString()));
        isoRequest.setCurrency((String) paymentRequest.get("currency"));
        isoRequest.setPaymentType((String) paymentRequest.get("paymentType"));
        isoRequest.setMessageType((String) paymentRequest.get("messageType"));
        isoRequest.setIso20022Message((String) paymentRequest.get("iso20022Message"));
        isoRequest.setTenantId(tenantId);
        
        return paymentRoutingService.processIncomingClearingPayment(isoRequest);
    }
}