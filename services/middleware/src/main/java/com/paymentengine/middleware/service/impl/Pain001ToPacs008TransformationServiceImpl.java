package com.paymentengine.middleware.service.impl;

import com.paymentengine.middleware.service.Pain001ToPacs008TransformationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Implementation of PAIN.001 to PACS.008 transformation service
 */
@Service
public class Pain001ToPacs008TransformationServiceImpl implements Pain001ToPacs008TransformationService {
    
    private static final Logger logger = LoggerFactory.getLogger(Pain001ToPacs008TransformationServiceImpl.class);
    private static final DateTimeFormatter ISO_DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
    
    @Override
    public Map<String, Object> transformPain001ToPacs008(Map<String, Object> pain001Message, 
                                                       String tenantId, 
                                                       String paymentType, 
                                                       String localInstrumentCode) {
        
        logger.info("Transforming PAIN.001 to PACS.008 for tenant: {}, paymentType: {}, localInstrument: {}", 
                tenantId, paymentType, localInstrumentCode);
        
        try {
            // Validate PAIN.001 message
            Map<String, Object> validation = validatePain001Message(pain001Message);
            if (!(Boolean) validation.get("valid")) {
                throw new IllegalArgumentException("Invalid PAIN.001 message: " + validation.get("errors"));
            }
            
            // Extract payment information
            PaymentInfo paymentInfo = extractPaymentInfo(pain001Message);
            paymentInfo.setPaymentType(paymentType);
            paymentInfo.setLocalInstrumentCode(localInstrumentCode);
            
            // Determine clearing system code (this would be passed from the routing service)
            String clearingSystemCode = determineClearingSystemCode(paymentType, localInstrumentCode);
            
            // Create PACS.008 message
            Map<String, Object> pacs008Message = createPacs008Message(paymentInfo, clearingSystemCode, tenantId);
            
            logger.info("Successfully transformed PAIN.001 to PACS.008 for messageId: {}", paymentInfo.getMessageId());
            
            return pacs008Message;
            
        } catch (Exception e) {
            logger.error("Error transforming PAIN.001 to PACS.008: {}", e.getMessage());
            throw new RuntimeException("Failed to transform PAIN.001 to PACS.008", e);
        }
    }
    
    @Override
    public Map<String, Object> validatePain001Message(Map<String, Object> pain001Message) {
        logger.debug("Validating PAIN.001 message");
        
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        
        try {
            // Check if message has required root element
            if (!pain001Message.containsKey("CstmrCdtTrfInitn")) {
                errors.add("PAIN.001 message must contain CstmrCdtTrfInitn element");
                return createValidationResult(false, errors, warnings);
            }
            
            Map<String, Object> cstmrCdtTrfInitn = (Map<String, Object>) pain001Message.get("CstmrCdtTrfInitn");
            
            // Validate Group Header
            if (!cstmrCdtTrfInitn.containsKey("GrpHdr")) {
                errors.add("CstmrCdtTrfInitn must contain GrpHdr element");
            } else {
                Map<String, Object> grpHdr = (Map<String, Object>) cstmrCdtTrfInitn.get("GrpHdr");
                validateGroupHeader(grpHdr, errors, warnings);
            }
            
            // Validate Payment Information
            if (!cstmrCdtTrfInitn.containsKey("PmtInf")) {
                errors.add("CstmrCdtTrfInitn must contain PmtInf element");
            } else {
                Map<String, Object> pmtInf = (Map<String, Object>) cstmrCdtTrfInitn.get("PmtInf");
                validatePaymentInformation(pmtInf, errors, warnings);
            }
            
            return createValidationResult(errors.isEmpty(), errors, warnings);
            
        } catch (Exception e) {
            logger.error("Error validating PAIN.001 message: {}", e.getMessage());
            errors.add("Validation error: " + e.getMessage());
            return createValidationResult(false, errors, warnings);
        }
    }
    
    @Override
    public PaymentInfo extractPaymentInfo(Map<String, Object> pain001Message) {
        logger.debug("Extracting payment information from PAIN.001 message");
        
        try {
            Map<String, Object> cstmrCdtTrfInitn = (Map<String, Object>) pain001Message.get("CstmrCdtTrfInitn");
            Map<String, Object> grpHdr = (Map<String, Object>) cstmrCdtTrfInitn.get("GrpHdr");
            Map<String, Object> pmtInf = (Map<String, Object>) cstmrCdtTrfInitn.get("PmtInf");
            
            // Extract from Group Header
            String messageId = (String) grpHdr.get("MsgId");
            String creationDateTime = (String) grpHdr.get("CreDtTm");
            String numberOfTransactions = (String) grpHdr.get("NbOfTxs");
            
            Map<String, Object> initgPty = (Map<String, Object>) grpHdr.get("InitgPty");
            String initiatingPartyName = (String) initgPty.get("Nm");
            String initiatingPartyBic = null;
            
            if (initgPty.containsKey("Id")) {
                Map<String, Object> id = (Map<String, Object>) initgPty.get("Id");
                if (id.containsKey("OrgId")) {
                    Map<String, Object> orgId = (Map<String, Object>) id.get("OrgId");
                    initiatingPartyBic = (String) orgId.get("AnyBIC");
                }
            }
            
            // Extract from Payment Information
            String paymentInformationId = (String) pmtInf.get("PmtInfId");
            String paymentMethod = (String) pmtInf.get("PmtMtd");
            String requestedExecutionDate = (String) pmtInf.get("ReqdExctnDt");
            
            // Extract Debtor Information
            Map<String, Object> dbtr = (Map<String, Object>) pmtInf.get("Dbtr");
            String debtorName = (String) dbtr.get("Nm");
            
            Map<String, Object> dbtrAcct = (Map<String, Object>) pmtInf.get("DbtrAcct");
            String debtorAccountId = null;
            if (dbtrAcct.containsKey("Id")) {
                Map<String, Object> id = (Map<String, Object>) dbtrAcct.get("Id");
                if (id.containsKey("Othr")) {
                    Map<String, Object> othr = (Map<String, Object>) id.get("Othr");
                    debtorAccountId = (String) othr.get("Id");
                } else if (id.containsKey("IBAN")) {
                    debtorAccountId = (String) id.get("IBAN");
                }
            }
            String debtorCurrency = (String) dbtrAcct.get("Ccy");
            
            // Extract Debtor Agent
            String debtorBic = null;
            if (pmtInf.containsKey("DbtrAgt")) {
                Map<String, Object> dbtrAgt = (Map<String, Object>) pmtInf.get("DbtrAgt");
                Map<String, Object> finInstnId = (Map<String, Object>) dbtrAgt.get("FinInstnId");
                debtorBic = (String) finInstnId.get("BICFI");
            }
            
            // Extract Credit Transfer Transaction Information
            Map<String, Object> cdtTrfTxInf = (Map<String, Object>) pmtInf.get("CdtTrfTxInf");
            
            // Payment Identification
            Map<String, Object> pmtId = (Map<String, Object>) cdtTrfTxInf.get("PmtId");
            String instructionId = (String) pmtId.get("InstrId");
            String endToEndId = (String) pmtId.get("EndToEndId");
            
            // Amount
            Map<String, Object> amt = (Map<String, Object>) cdtTrfTxInf.get("Amt");
            Map<String, Object> instdAmt = (Map<String, Object>) amt.get("InstdAmt");
            String amount = instdAmt.get("value").toString();
            String currency = (String) instdAmt.get("Ccy");
            
            // Creditor Agent
            String creditorBic = null;
            if (cdtTrfTxInf.containsKey("CdtrAgt")) {
                Map<String, Object> cdtrAgt = (Map<String, Object>) cdtTrfTxInf.get("CdtrAgt");
                Map<String, Object> finInstnId = (Map<String, Object>) cdtrAgt.get("FinInstnId");
                creditorBic = (String) finInstnId.get("BICFI");
            }
            
            // Creditor
            Map<String, Object> cdtr = (Map<String, Object>) cdtTrfTxInf.get("Cdtr");
            String creditorName = (String) cdtr.get("Nm");
            
            // Creditor Account
            Map<String, Object> cdtrAcct = (Map<String, Object>) cdtTrfTxInf.get("CdtrAcct");
            String creditorAccountId = null;
            if (cdtrAcct.containsKey("Id")) {
                Map<String, Object> id = (Map<String, Object>) cdtrAcct.get("Id");
                if (id.containsKey("Othr")) {
                    Map<String, Object> othr = (Map<String, Object>) id.get("Othr");
                    creditorAccountId = (String) othr.get("Id");
                } else if (id.containsKey("IBAN")) {
                    creditorAccountId = (String) id.get("IBAN");
                }
            }
            
            // Remittance Information
            String remittanceInfo = null;
            if (cdtTrfTxInf.containsKey("RmtInf")) {
                Map<String, Object> rmtInf = (Map<String, Object>) cdtTrfTxInf.get("RmtInf");
                if (rmtInf.containsKey("Ustrd")) {
                    List<String> ustrd = (List<String>) rmtInf.get("Ustrd");
                    if (!ustrd.isEmpty()) {
                        remittanceInfo = ustrd.get(0);
                    }
                }
            }
            
            // Payment Type Information
            String serviceLevel = null;
            String localInstrumentCode = null;
            String chargeBearer = null;
            String purposeCode = null;
            String categoryPurpose = null;
            
            if (pmtInf.containsKey("PmtTpInf")) {
                Map<String, Object> pmtTpInf = (Map<String, Object>) pmtInf.get("PmtTpInf");
                
                if (pmtTpInf.containsKey("SvcLvl")) {
                    Map<String, Object> svcLvl = (Map<String, Object>) pmtTpInf.get("SvcLvl");
                    serviceLevel = (String) svcLvl.get("Cd");
                }
                
                if (pmtTpInf.containsKey("LclInstrm")) {
                    Map<String, Object> lclInstrm = (Map<String, Object>) pmtTpInf.get("LclInstrm");
                    localInstrumentCode = (String) lclInstrm.get("Cd");
                }
                
                if (pmtTpInf.containsKey("CtgyPurp")) {
                    Map<String, Object> ctgyPurp = (Map<String, Object>) pmtTpInf.get("CtgyPurp");
                    categoryPurpose = (String) ctgyPurp.get("Cd");
                }
            }
            
            if (cdtTrfTxInf.containsKey("ChrgBr")) {
                chargeBearer = (String) cdtTrfTxInf.get("ChrgBr");
            }
            
            if (cdtTrfTxInf.containsKey("Purp")) {
                Map<String, Object> purp = (Map<String, Object>) cdtTrfTxInf.get("Purp");
                purposeCode = (String) purp.get("Cd");
            }
            
            return new PaymentInfo(
                    messageId, endToEndId, instructionId,
                    debtorName, debtorAccountId, debtorBic,
                    creditorName, creditorAccountId, creditorBic,
                    amount, currency, requestedExecutionDate,
                    remittanceInfo, null, localInstrumentCode,
                    serviceLevel, chargeBearer, purposeCode,
                    categoryPurpose, initiatingPartyName, initiatingPartyBic
            );
            
        } catch (Exception e) {
            logger.error("Error extracting payment information: {}", e.getMessage());
            throw new RuntimeException("Failed to extract payment information", e);
        }
    }
    
    @Override
    public Map<String, Object> createPacs008Message(PaymentInfo paymentInfo, String clearingSystemCode, String tenantId) {
        logger.debug("Creating PACS.008 message for clearing system: {}", clearingSystemCode);
        
        try {
            String pacs008MessageId = "PACS008-" + System.currentTimeMillis();
            String currentDateTime = LocalDateTime.now().format(ISO_DATETIME_FORMATTER);
            
            // Create PACS.008 message structure
            Map<String, Object> pacs008 = new HashMap<>();
            Map<String, Object> fiToFICustomerCreditTransfer = new HashMap<>();
            
            // Group Header
            Map<String, Object> grpHdr = new HashMap<>();
            grpHdr.put("MsgId", pacs008MessageId);
            grpHdr.put("CreDtTm", currentDateTime);
            grpHdr.put("NbOfTxs", "1");
            grpHdr.put("CtrlSum", paymentInfo.getAmount());
            
            // Instructing Agent (Our Bank)
            Map<String, Object> instgAgt = new HashMap<>();
            Map<String, Object> instgAgtFinInstnId = new HashMap<>();
            instgAgtFinInstnId.put("BICFI", "PAYMENTUS33XXX"); // Our bank BIC
            instgAgtFinInstnId.put("Nm", "Payment Engine Bank");
            instgAgt.put("FinInstnId", instgAgtFinInstnId);
            grpHdr.put("InstgAgt", instgAgt);
            
            // Instructed Agent (Clearing System)
            Map<String, Object> instdAgt = new HashMap<>();
            Map<String, Object> instdAgtFinInstnId = new HashMap<>();
            instdAgtFinInstnId.put("BICFI", getClearingSystemBic(clearingSystemCode));
            instdAgtFinInstnId.put("Nm", getClearingSystemName(clearingSystemCode));
            instdAgt.put("FinInstnId", instdAgtFinInstnId);
            grpHdr.put("InstdAgt", instdAgt);
            
            fiToFICustomerCreditTransfer.put("GrpHdr", grpHdr);
            
            // Credit Transfer Transaction Information
            Map<String, Object> cdtTrfTxInf = new HashMap<>();
            
            // Payment Identification
            Map<String, Object> pmtId = new HashMap<>();
            pmtId.put("InstrId", paymentInfo.getInstructionId());
            pmtId.put("EndToEndId", paymentInfo.getEndToEndId());
            pmtId.put("TxId", "TX-" + System.currentTimeMillis());
            cdtTrfTxInf.put("PmtId", pmtId);
            
            // Interbank Settlement Amount
            Map<String, Object> intrBkSttlmAmt = new HashMap<>();
            intrBkSttlmAmt.put("Ccy", paymentInfo.getCurrency());
            intrBkSttlmAmt.put("value", paymentInfo.getAmount());
            cdtTrfTxInf.put("IntrBkSttlmAmt", intrBkSttlmAmt);
            
            // Interbank Settlement Date
            cdtTrfTxInf.put("IntrBkSttlmDt", paymentInfo.getExecutionDate());
            
            // Charge Bearer
            if (paymentInfo.getChargeBearer() != null) {
                cdtTrfTxInf.put("ChrgBr", paymentInfo.getChargeBearer());
            }
            
            // Instructing Agent
            if (paymentInfo.getDebtorBic() != null) {
                Map<String, Object> instgAgtTx = new HashMap<>();
                Map<String, Object> instgAgtTxFinInstnId = new HashMap<>();
                instgAgtTxFinInstnId.put("BICFI", paymentInfo.getDebtorBic());
                instgAgtTx.put("FinInstnId", instgAgtTxFinInstnId);
                cdtTrfTxInf.put("InstgAgt", instgAgtTx);
            }
            
            // Instructed Agent
            if (paymentInfo.getCreditorBic() != null) {
                Map<String, Object> instdAgtTx = new HashMap<>();
                Map<String, Object> instdAgtTxFinInstnId = new HashMap<>();
                instdAgtTxFinInstnId.put("BICFI", paymentInfo.getCreditorBic());
                instdAgtTx.put("FinInstnId", instdAgtTxFinInstnId);
                cdtTrfTxInf.put("InstdAgt", instdAgtTx);
            }
            
            // Debtor
            Map<String, Object> dbtr = new HashMap<>();
            dbtr.put("Nm", paymentInfo.getDebtorName());
            cdtTrfTxInf.put("Dbtr", dbtr);
            
            // Debtor Account
            Map<String, Object> dbtrAcct = new HashMap<>();
            Map<String, Object> dbtrAcctId = new HashMap<>();
            if (paymentInfo.getDebtorAccountId().startsWith("GB") || paymentInfo.getDebtorAccountId().startsWith("DE")) {
                dbtrAcctId.put("IBAN", paymentInfo.getDebtorAccountId());
            } else {
                Map<String, Object> othr = new HashMap<>();
                othr.put("Id", paymentInfo.getDebtorAccountId());
                dbtrAcctId.put("Othr", othr);
            }
            dbtrAcct.put("Id", dbtrAcctId);
            dbtrAcct.put("Ccy", paymentInfo.getCurrency());
            cdtTrfTxInf.put("DbtrAcct", dbtrAcct);
            
            // Debtor Agent
            if (paymentInfo.getDebtorBic() != null) {
                Map<String, Object> dbtrAgt = new HashMap<>();
                Map<String, Object> dbtrAgtFinInstnId = new HashMap<>();
                dbtrAgtFinInstnId.put("BICFI", paymentInfo.getDebtorBic());
                dbtrAgt.put("FinInstnId", dbtrAgtFinInstnId);
                cdtTrfTxInf.put("DbtrAgt", dbtrAgt);
            }
            
            // Creditor
            Map<String, Object> cdtr = new HashMap<>();
            cdtr.put("Nm", paymentInfo.getCreditorName());
            cdtTrfTxInf.put("Cdtr", cdtr);
            
            // Creditor Account
            Map<String, Object> cdtrAcct = new HashMap<>();
            Map<String, Object> cdtrAcctId = new HashMap<>();
            if (paymentInfo.getCreditorAccountId().startsWith("GB") || paymentInfo.getCreditorAccountId().startsWith("DE")) {
                cdtrAcctId.put("IBAN", paymentInfo.getCreditorAccountId());
            } else {
                Map<String, Object> othr = new HashMap<>();
                othr.put("Id", paymentInfo.getCreditorAccountId());
                cdtrAcctId.put("Othr", othr);
            }
            cdtrAcct.put("Id", cdtrAcctId);
            cdtTrfTxInf.put("CdtrAcct", cdtrAcct);
            
            // Creditor Agent
            if (paymentInfo.getCreditorBic() != null) {
                Map<String, Object> cdtrAgt = new HashMap<>();
                Map<String, Object> cdtrAgtFinInstnId = new HashMap<>();
                cdtrAgtFinInstnId.put("BICFI", paymentInfo.getCreditorBic());
                cdtrAgt.put("FinInstnId", cdtrAgtFinInstnId);
                cdtTrfTxInf.put("CdtrAgt", cdtrAgt);
            }
            
            // Remittance Information
            if (paymentInfo.getRemittanceInfo() != null) {
                Map<String, Object> rmtInf = new HashMap<>();
                rmtInf.put("Ustrd", Arrays.asList(paymentInfo.getRemittanceInfo()));
                cdtTrfTxInf.put("RmtInf", rmtInf);
            }
            
            // Purpose
            if (paymentInfo.getPurposeCode() != null) {
                Map<String, Object> purp = new HashMap<>();
                purp.put("Cd", paymentInfo.getPurposeCode());
                cdtTrfTxInf.put("Purp", purp);
            }
            
            fiToFICustomerCreditTransfer.put("CdtTrfTxInf", cdtTrfTxInf);
            pacs008.put("FIToFICustomerCreditTransfer", fiToFICustomerCreditTransfer);
            
            // Add metadata
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("originalMessageId", paymentInfo.getMessageId());
            metadata.put("clearingSystemCode", clearingSystemCode);
            metadata.put("tenantId", tenantId);
            metadata.put("paymentType", paymentInfo.getPaymentType());
            metadata.put("localInstrumentCode", paymentInfo.getLocalInstrumentCode());
            metadata.put("transformationTimestamp", currentDateTime);
            pacs008.put("_metadata", metadata);
            
            logger.info("Created PACS.008 message: {} for clearing system: {}", pacs008MessageId, clearingSystemCode);
            
            return pacs008;
            
        } catch (Exception e) {
            logger.error("Error creating PACS.008 message: {}", e.getMessage());
            throw new RuntimeException("Failed to create PACS.008 message", e);
        }
    }
    
    private void validateGroupHeader(Map<String, Object> grpHdr, List<String> errors, List<String> warnings) {
        if (!grpHdr.containsKey("MsgId") || grpHdr.get("MsgId") == null) {
            errors.add("GrpHdr.MsgId is required");
        }
        
        if (!grpHdr.containsKey("CreDtTm") || grpHdr.get("CreDtTm") == null) {
            errors.add("GrpHdr.CreDtTm is required");
        }
        
        if (!grpHdr.containsKey("NbOfTxs") || grpHdr.get("NbOfTxs") == null) {
            errors.add("GrpHdr.NbOfTxs is required");
        }
        
        if (!grpHdr.containsKey("InitgPty") || grpHdr.get("InitgPty") == null) {
            errors.add("GrpHdr.InitgPty is required");
        }
    }
    
    private void validatePaymentInformation(Map<String, Object> pmtInf, List<String> errors, List<String> warnings) {
        if (!pmtInf.containsKey("PmtInfId") || pmtInf.get("PmtInfId") == null) {
            errors.add("PmtInf.PmtInfId is required");
        }
        
        if (!pmtInf.containsKey("PmtMtd") || pmtInf.get("PmtMtd") == null) {
            errors.add("PmtInf.PmtMtd is required");
        }
        
        if (!pmtInf.containsKey("ReqdExctnDt") || pmtInf.get("ReqdExctnDt") == null) {
            errors.add("PmtInf.ReqdExctnDt is required");
        }
        
        if (!pmtInf.containsKey("Dbtr") || pmtInf.get("Dbtr") == null) {
            errors.add("PmtInf.Dbtr is required");
        }
        
        if (!pmtInf.containsKey("DbtrAcct") || pmtInf.get("DbtrAcct") == null) {
            errors.add("PmtInf.DbtrAcct is required");
        }
        
        if (!pmtInf.containsKey("CdtTrfTxInf") || pmtInf.get("CdtTrfTxInf") == null) {
            errors.add("PmtInf.CdtTrfTxInf is required");
        }
    }
    
    private Map<String, Object> createValidationResult(boolean valid, List<String> errors, List<String> warnings) {
        return Map.of(
                "valid", valid,
                "errors", errors,
                "warnings", warnings,
                "timestamp", LocalDateTime.now().format(ISO_DATETIME_FORMATTER)
        );
    }
    
    private String determineClearingSystemCode(String paymentType, String localInstrumentCode) {
        // This would typically be determined by the clearing system routing service
        // For now, use simple mapping
        if ("RTP".equals(paymentType) || "INST".equals(localInstrumentCode)) {
            return "RTP";
        } else if ("ACH_CREDIT".equals(paymentType) || "ACH_DEBIT".equals(paymentType)) {
            return "ACH";
        } else if ("WIRE_DOMESTIC".equals(paymentType) || "WIRE_INTERNATIONAL".equals(paymentType)) {
            return "FEDWIRE";
        } else if ("SEPA_CREDIT".equals(paymentType) || "SEPA_INSTANT".equals(paymentType)) {
            return "SEPA";
        }
        
        return "FEDWIRE"; // Default
    }
    
    private String getClearingSystemBic(String clearingSystemCode) {
        switch (clearingSystemCode) {
            case "FEDWIRE": return "FEDWIREUS33XXX";
            case "CHAPS": return "CHAPSGB22XXX";
            case "SEPA": return "SEPAEU22XXX";
            case "ACH": return "ACHUS33XXX";
            case "RTP": return "RTPUS33XXX";
            default: return "UNKNOWNUS33XXX";
        }
    }
    
    private String getClearingSystemName(String clearingSystemCode) {
        switch (clearingSystemCode) {
            case "FEDWIRE": return "Federal Reserve Wire Network";
            case "CHAPS": return "Clearing House Automated Payment System";
            case "SEPA": return "Single Euro Payments Area";
            case "ACH": return "Automated Clearing House";
            case "RTP": return "Real-Time Payments";
            default: return "Unknown Clearing System";
        }
    }
}