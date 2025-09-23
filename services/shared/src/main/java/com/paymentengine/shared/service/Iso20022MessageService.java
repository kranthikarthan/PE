package com.paymentengine.shared.service;

import com.paymentengine.shared.dto.iso20022.*;
import com.paymentengine.shared.dto.CreateTransactionRequest;
import com.paymentengine.shared.dto.TransactionResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Service for transforming between ISO 20022 pain.001 messages and internal transaction format
 */
@Service
public class Iso20022MessageService {
    
    private static final Logger logger = LoggerFactory.getLogger(Iso20022MessageService.class);
    private static final DateTimeFormatter ISO_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
    private static final DateTimeFormatter ISO_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    /**
     * Transform ISO 20022 pain.001 message to internal CreateTransactionRequest
     */
    public CreateTransactionRequest transformPain001ToTransactionRequest(Pain001Message pain001) {
        logger.debug("Transforming pain.001 message to transaction request");
        
        try {
            Pain001Message.CustomerCreditTransferInitiation cctInitiation = pain001.getCustomerCreditTransferInitiation();
            Pain001Message.PaymentInformation pmtInf = cctInitiation.getPaymentInformation();
            CreditTransferTransactionInformation cdtTrfTxInf = pmtInf.getCreditTransferTransactionInformation();
            
            CreateTransactionRequest request = new CreateTransactionRequest();
            
            // Set basic transaction information
            request.setExternalReference(cdtTrfTxInf.getPaymentIdentification().getEndToEndId());
            request.setAmount(cdtTrfTxInf.getAmount().getInstructedAmount().getValue());
            request.setCurrencyCode(cdtTrfTxInf.getAmount().getInstructedAmount().getCurrency());
            
            // Set description from remittance information
            if (cdtTrfTxInf.getRemittanceInformation() != null && 
                cdtTrfTxInf.getRemittanceInformation().getUnstructured() != null &&
                cdtTrfTxInf.getRemittanceInformation().getUnstructured().length > 0) {
                request.setDescription(cdtTrfTxInf.getRemittanceInformation().getUnstructured()[0]);
            }
            
            // Map account identifications to internal account IDs
            String debtorAccountId = mapAccountIdentificationToId(pmtInf.getDebtorAccount());
            String creditorAccountId = mapAccountIdentificationToId(cdtTrfTxInf.getCreditorAccount());
            
            request.setFromAccountId(debtorAccountId != null ? UUID.fromString(debtorAccountId) : null);
            request.setToAccountId(creditorAccountId != null ? UUID.fromString(creditorAccountId) : null);
            
            // Map payment type based on local instrument and service level
            UUID paymentTypeId = mapPaymentTypeFromIso20022(pmtInf.getPaymentTypeInformation(), cdtTrfTxInf.getPaymentTypeInformation());
            request.setPaymentTypeId(paymentTypeId);
            
            // Add ISO 20022 specific metadata
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("iso20022", Map.of(
                "messageId", cctInitiation.getGroupHeader().getMessageId(),
                "paymentInformationId", pmtInf.getPaymentInformationId(),
                "endToEndId", cdtTrfTxInf.getPaymentIdentification().getEndToEndId(),
                "instructionId", cdtTrfTxInf.getPaymentIdentification().getInstrId(),
                "paymentMethod", pmtInf.getPaymentMethod(),
                "chargeBearer", cdtTrfTxInf.getChargeBearer(),
                "requestedExecutionDate", pmtInf.getRequestedExecutionDate()
            ));
            
            // Add party information
            metadata.put("debtor", Map.of(
                "name", pmtInf.getDebtor().getName(),
                "country", pmtInf.getDebtor().getCountryOfResidence() != null ? pmtInf.getDebtor().getCountryOfResidence() : ""
            ));
            
            metadata.put("creditor", Map.of(
                "name", cdtTrfTxInf.getCreditor().getName(),
                "country", cdtTrfTxInf.getCreditor().getCountryOfResidence() != null ? cdtTrfTxInf.getCreditor().getCountryOfResidence() : ""
            ));
            
            // Add regulatory reporting if present
            if (cdtTrfTxInf.getRegulatoryReporting() != null) {
                metadata.put("regulatoryReporting", Map.of(
                    "indicator", cdtTrfTxInf.getRegulatoryReporting().getDebitCreditReportingIndicator() != null ? 
                        cdtTrfTxInf.getRegulatoryReporting().getDebitCreditReportingIndicator() : ""
                ));
            }
            
            request.setMetadata(metadata);
            request.setChannel("iso20022");
            
            logger.debug("Successfully transformed pain.001 message to transaction request");
            return request;
            
        } catch (Exception e) {
            logger.error("Error transforming pain.001 message: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to transform ISO 20022 message", e);
        }
    }
    
    /**
     * Transform internal TransactionResponse to ISO 20022 pain.002 (status report)
     */
    public Map<String, Object> transformTransactionResponseToPain002(TransactionResponse transaction, String originalMessageId) {
        logger.debug("Transforming transaction response to pain.002 format");
        
        try {
            Map<String, Object> pain002 = new HashMap<>();
            
            // Customer Payment Status Report
            Map<String, Object> cstmrPmtStsRpt = new HashMap<>();
            
            // Group Header
            Map<String, Object> grpHdr = new HashMap<>();
            grpHdr.put("MsgId", "PAIN002-" + UUID.randomUUID().toString());
            grpHdr.put("CreDtTm", LocalDateTime.now().format(ISO_DATE_TIME_FORMATTER));
            grpHdr.put("InitgPty", Map.of("Nm", "Payment Engine"));
            
            // Original Group Information and Status
            Map<String, Object> orgnlGrpInfAndSts = new HashMap<>();
            orgnlGrpInfAndSts.put("OrgnlMsgId", originalMessageId);
            orgnlGrpInfAndSts.put("OrgnlMsgNmId", "pain.001.001.03");
            orgnlGrpInfAndSts.put("OrgnlCreDtTm", transaction.getCreatedAt().format(ISO_DATE_TIME_FORMATTER));
            orgnlGrpInfAndSts.put("GrpSts", mapTransactionStatusToIso20022(transaction.getStatus()));
            
            // Payment Information Status
            Map<String, Object> pmtInfSts = new HashMap<>();
            pmtInfSts.put("PmtInfId", transaction.getTransactionReference());
            pmtInfSts.put("PmtInfSts", mapTransactionStatusToIso20022(transaction.getStatus()));
            
            // Transaction Information and Status
            Map<String, Object> txInfAndSts = new HashMap<>();
            txInfAndSts.put("StsId", transaction.getId().toString());
            txInfAndSts.put("OrgnlInstrId", transaction.getExternalReference());
            txInfAndSts.put("OrgnlEndToEndId", transaction.getExternalReference());
            txInfAndSts.put("TxSts", mapTransactionStatusToIso20022(transaction.getStatus()));
            
            if (transaction.getCompletedAt() != null) {
                txInfAndSts.put("AccptncDtTm", transaction.getCompletedAt().format(ISO_DATE_TIME_FORMATTER));
            }
            
            // Add amount information
            Map<String, Object> orgnlTxRef = new HashMap<>();
            Map<String, Object> amt = new HashMap<>();
            Map<String, Object> instdAmt = new HashMap<>();
            instdAmt.put("Ccy", transaction.getCurrencyCode());
            instdAmt.put("value", transaction.getAmount());
            amt.put("InstdAmt", instdAmt);
            orgnlTxRef.put("Amt", amt);
            
            txInfAndSts.put("OrgnlTxRef", orgnlTxRef);
            
            pmtInfSts.put("TxInfAndSts", txInfAndSts);
            
            cstmrPmtStsRpt.put("GrpHdr", grpHdr);
            cstmrPmtStsRpt.put("OrgnlGrpInfAndSts", orgnlGrpInfAndSts);
            cstmrPmtStsRpt.put("PmtInfSts", pmtInfSts);
            
            pain002.put("CstmrPmtStsRpt", cstmrPmtStsRpt);
            
            logger.debug("Successfully transformed transaction response to pain.002 format");
            return pain002;
            
        } catch (Exception e) {
            logger.error("Error transforming transaction response to pain.002: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to transform to ISO 20022 status report", e);
        }
    }
    
    /**
     * Validate ISO 20022 pain.001 message
     */
    public ValidationResult validatePain001Message(Pain001Message pain001) {
        logger.debug("Validating pain.001 message");
        
        ValidationResult result = new ValidationResult();
        
        try {
            Pain001Message.CustomerCreditTransferInitiation cctInitiation = pain001.getCustomerCreditTransferInitiation();
            
            if (cctInitiation == null) {
                result.addError("CustomerCreditTransferInitiation is required");
                return result;
            }
            
            // Validate Group Header
            validateGroupHeader(cctInitiation.getGroupHeader(), result);
            
            // Validate Payment Information
            validatePaymentInformation(cctInitiation.getPaymentInformation(), result);
            
            logger.debug("pain.001 message validation completed with {} errors", result.getErrors().size());
            
        } catch (Exception e) {
            logger.error("Error validating pain.001 message: {}", e.getMessage(), e);
            result.addError("Validation failed: " + e.getMessage());
        }
        
        return result;
    }
    
    private void validateGroupHeader(Pain001Message.GroupHeader grpHdr, ValidationResult result) {
        if (grpHdr == null) {
            result.addError("GroupHeader is required");
            return;
        }
        
        if (grpHdr.getMessageId() == null || grpHdr.getMessageId().isEmpty()) {
            result.addError("GroupHeader.MessageId is required");
        }
        
        if (grpHdr.getCreationDateTime() == null || grpHdr.getCreationDateTime().isEmpty()) {
            result.addError("GroupHeader.CreationDateTime is required");
        }
        
        if (grpHdr.getNumberOfTransactions() == null || grpHdr.getNumberOfTransactions().isEmpty()) {
            result.addError("GroupHeader.NumberOfTransactions is required");
        }
        
        if (grpHdr.getInitiatingParty() == null) {
            result.addError("GroupHeader.InitiatingParty is required");
        }
    }
    
    private void validatePaymentInformation(Pain001Message.PaymentInformation pmtInf, ValidationResult result) {
        if (pmtInf == null) {
            result.addError("PaymentInformation is required");
            return;
        }
        
        if (pmtInf.getPaymentInformationId() == null || pmtInf.getPaymentInformationId().isEmpty()) {
            result.addError("PaymentInformation.PaymentInformationId is required");
        }
        
        if (pmtInf.getPaymentMethod() == null || pmtInf.getPaymentMethod().isEmpty()) {
            result.addError("PaymentInformation.PaymentMethod is required");
        }
        
        if (pmtInf.getRequestedExecutionDate() == null || pmtInf.getRequestedExecutionDate().isEmpty()) {
            result.addError("PaymentInformation.RequestedExecutionDate is required");
        }
        
        if (pmtInf.getDebtor() == null) {
            result.addError("PaymentInformation.Debtor is required");
        }
        
        if (pmtInf.getDebtorAccount() == null) {
            result.addError("PaymentInformation.DebtorAccount is required");
        }
        
        if (pmtInf.getCreditTransferTransactionInformation() == null) {
            result.addError("PaymentInformation.CreditTransferTransactionInformation is required");
        }
    }
    
    /**
     * Map account identification to internal account ID
     */
    private String mapAccountIdentificationToId(Account account) {
        if (account == null || account.getIdentification() == null) {
            return null;
        }
        
        Account.AccountIdentification id = account.getIdentification();
        
        // Try IBAN first
        if (id.getIban() != null && !id.getIban().isEmpty()) {
            return lookupAccountByIban(id.getIban());
        }
        
        // Try other identification
        if (id.getOther() != null && id.getOther().getIdentification() != null) {
            return lookupAccountByNumber(id.getOther().getIdentification());
        }
        
        return null;
    }
    
    /**
     * Map payment type from ISO 20022 to internal payment type ID
     */
    private UUID mapPaymentTypeFromIso20022(Pain001Message.PaymentTypeInformation pmtTpInf, 
                                          PaymentTypeInformation cdtTrfTxInfPmtTpInf) {
        
        // Default payment type mapping based on local instrument
        String localInstrumentCode = null;
        
        if (pmtTpInf != null && pmtTpInf.getLocalInstrument() != null) {
            localInstrumentCode = pmtTpInf.getLocalInstrument().getCode();
        } else if (cdtTrfTxInfPmtTpInf != null && cdtTrfTxInfPmtTpInf.getLocalInstrument() != null) {
            localInstrumentCode = cdtTrfTxInfPmtTpInf.getLocalInstrument().getCode();
        }
        
        // Map ISO 20022 local instrument codes to internal payment types
        return switch (localInstrumentCode != null ? localInstrumentCode : "TRF") {
            case "RTGS", "RTP" -> UUID.fromString("660e8400-e29b-41d4-a716-446655440005"); // RTP
            case "ACH", "CORE" -> UUID.fromString("660e8400-e29b-41d4-a716-446655440001"); // ACH_CREDIT
            case "WIRE", "SWIFT" -> UUID.fromString("660e8400-e29b-41d4-a716-446655440003"); // WIRE_DOMESTIC
            case "SEPA" -> UUID.fromString("660e8400-e29b-41d4-a716-446655440001"); // ACH_CREDIT (SEPA equivalent)
            case "INST" -> UUID.fromString("660e8400-e29b-41d4-a716-446655440005"); // RTP (Instant)
            default -> UUID.fromString("660e8400-e29b-41d4-a716-446655440001"); // Default to ACH_CREDIT
        };
    }
    
    /**
     * Map internal transaction status to ISO 20022 status
     */
    private String mapTransactionStatusToIso20022(com.paymentengine.corebanking.entity.Transaction.TransactionStatus status) {
        return switch (status) {
            case PENDING -> "PDNG"; // Pending
            case PROCESSING -> "ACTC"; // Accepted Technical Validation
            case COMPLETED -> "ACSC"; // Accepted Settlement Completed
            case FAILED -> "RJCT"; // Rejected
            case CANCELLED -> "CANC"; // Cancelled
            case REVERSED -> "ACSC"; // Accepted Settlement Completed (for reversal)
        };
    }
    
    /**
     * Lookup account by IBAN (placeholder - would integrate with account service)
     */
    private String lookupAccountByIban(String iban) {
        // This would typically call the account service to lookup by IBAN
        // For now, return a placeholder mapping
        logger.debug("Looking up account by IBAN: {}", iban);
        
        // Mock mapping for demonstration
        Map<String, String> ibanToAccountId = Map.of(
            "US64SVBKUS6S3300958879", "880e8400-e29b-41d4-a716-446655440001",
            "US64SVBKUS6S3300958880", "880e8400-e29b-41d4-a716-446655440002",
            "US64SVBKUS6S3300958881", "880e8400-e29b-41d4-a716-446655440003"
        );
        
        return ibanToAccountId.get(iban);
    }
    
    /**
     * Lookup account by account number (placeholder - would integrate with account service)
     */
    private String lookupAccountByNumber(String accountNumber) {
        // This would typically call the account service to lookup by account number
        logger.debug("Looking up account by number: {}", accountNumber);
        
        // Mock mapping for demonstration
        Map<String, String> accountNumberToId = Map.of(
            "ACC001001", "880e8400-e29b-41d4-a716-446655440001",
            "ACC001002", "880e8400-e29b-41d4-a716-446655440002",
            "ACC002001", "880e8400-e29b-41d4-a716-446655440003"
        );
        
        return accountNumberToId.get(accountNumber);
    }
    
    /**
     * Generate ISO 20022 compliant message ID
     */
    public String generateMessageId(String prefix) {
        return prefix + "-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
    
    /**
     * Generate ISO 20022 compliant end-to-end ID
     */
    public String generateEndToEndId() {
        return "E2E-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
    
    /**
     * Validation result class
     */
    public static class ValidationResult {
        private final java.util.List<String> errors = new java.util.ArrayList<>();
        private final java.util.List<String> warnings = new java.util.ArrayList<>();
        
        public void addError(String error) {
            errors.add(error);
        }
        
        public void addWarning(String warning) {
            warnings.add(warning);
        }
        
        public boolean isValid() {
            return errors.isEmpty();
        }
        
        public java.util.List<String> getErrors() {
            return errors;
        }
        
        public java.util.List<String> getWarnings() {
            return warnings;
        }
        
        @Override
        public String toString() {
            return "ValidationResult{" +
                    "valid=" + isValid() +
                    ", errors=" + errors.size() +
                    ", warnings=" + warnings.size() +
                    '}';
        }
    }
}