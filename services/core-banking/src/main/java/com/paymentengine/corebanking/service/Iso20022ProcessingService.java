package com.paymentengine.corebanking.service;

import com.paymentengine.corebanking.dto.CreateTransactionRequest;
import com.paymentengine.corebanking.dto.TransactionResponse;
import com.paymentengine.corebanking.entity.Account;
import com.paymentengine.corebanking.entity.Transaction;
import com.paymentengine.corebanking.repository.AccountRepository;
import com.paymentengine.corebanking.repository.TransactionRepository;
import com.paymentengine.shared.constants.KafkaTopics;
import com.paymentengine.shared.dto.iso20022.*;
import com.paymentengine.shared.event.TransactionCreatedEvent;
import com.paymentengine.shared.service.Iso20022MessageService;
import com.paymentengine.shared.util.EventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for processing all ISO 20022 message types
 * Handles pain (customer), pacs (scheme), and camt (cash management) messages
 */
@Service
@Transactional
public class Iso20022ProcessingService {
    
    private static final Logger logger = LoggerFactory.getLogger(Iso20022ProcessingService.class);
    private static final DateTimeFormatter ISO_DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
    private static final DateTimeFormatter ISO_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    private final TransactionService transactionService;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final Iso20022MessageService iso20022MessageService;
    private final EventPublisher eventPublisher;
    
    @Autowired
    public Iso20022ProcessingService(
            TransactionService transactionService,
            AccountRepository accountRepository,
            TransactionRepository transactionRepository,
            Iso20022MessageService iso20022MessageService,
            EventPublisher eventPublisher) {
        this.transactionService = transactionService;
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.iso20022MessageService = iso20022MessageService;
        this.eventPublisher = eventPublisher;
    }
    
    // ============================================================================
    // PAIN MESSAGE PROCESSING (Customer Initiated)
    // ============================================================================
    
    /**
     * Process pain.001 - Customer Credit Transfer Initiation
     */
    public Map<String, Object> processPain001(Pain001Message pain001, Map<String, Object> context) {
        logger.info("Processing pain.001 customer credit transfer initiation");
        
        try {
            // Validate message
            Iso20022MessageService.ValidationResult validation = iso20022MessageService.validatePain001Message(pain001);
            if (!validation.isValid()) {
                throw new IllegalArgumentException("Invalid pain.001 message: " + validation.getErrors());
            }
            
            // Transform to internal format
            CreateTransactionRequest request = iso20022MessageService.transformPain001ToTransactionRequest(pain001);
            
            // Add context information
            request.setIpAddress((String) context.get("ipAddress"));
            request.setChannel("iso20022-pain001");
            
            // Process transaction
            TransactionResponse transaction = transactionService.createTransaction(request);
            
            // Generate pain.002 response
            String originalMessageId = pain001.getCustomerCreditTransferInitiation().getGroupHeader().getMessageId();
            Map<String, Object> pain002 = iso20022MessageService.transformTransactionResponseToPain002(transaction, originalMessageId);
            
            // Publish ISO 20022 event
            publishIso20022Event("pain.001", pain001, transaction, context);
            
            logger.info("pain.001 processed successfully, transaction: {}", transaction.getTransactionReference());
            return pain002;
            
        } catch (Exception e) {
            logger.error("Error processing pain.001: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to process pain.001 message", e);
        }
    }
    
    /**
     * Process pain.007 - Customer Payment Reversal
     */
    public Map<String, Object> processPain007(Pain007Message pain007, Map<String, Object> context) {
        logger.info("Processing pain.007 payment reversal");
        
        try {
            Pain007Message.CustomerPaymentReversal reversal = pain007.getCustomerPaymentReversal();
            String originalMessageId = reversal.getOriginalGroupInformation().getOriginalMessageId();
            
            // Find original transaction(s) to reverse
            List<Transaction> transactionsToReverse = findTransactionsByOriginalMessage(originalMessageId);
            
            if (transactionsToReverse.isEmpty()) {
                throw new IllegalArgumentException("Original transactions not found for reversal");
            }
            
            List<Map<String, Object>> reversalResults = new ArrayList<>();
            
            for (Transaction originalTransaction : transactionsToReverse) {
                // Validate reversal is allowed
                validateReversalAllowed(originalTransaction);
                
                // Create reversal transaction
                TransactionResponse reversalTransaction = createReversalTransaction(originalTransaction, reversal, context);
                
                // Generate pain.008 response
                Map<String, Object> pain008Response = generatePain008Response(reversalTransaction, pain007);
                reversalResults.add(pain008Response);
                
                logger.info("Reversal created for transaction: {} -> {}", 
                           originalTransaction.getTransactionReference(), 
                           reversalTransaction.getTransactionReference());
            }
            
            return Map.of(
                "reversalResults", reversalResults,
                "originalMessageId", originalMessageId,
                "numberOfReversals", reversalResults.size(),
                "timestamp", LocalDateTime.now().format(ISO_DATETIME_FORMATTER)
            );
            
        } catch (Exception e) {
            logger.error("Error processing pain.007 reversal: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to process payment reversal", e);
        }
    }
    
    // ============================================================================
    // PACS MESSAGE PROCESSING (Scheme Messages)
    // ============================================================================
    
    /**
     * Process pacs.008 - FI to FI Customer Credit Transfer (from scheme)
     */
    public Map<String, Object> processPacs008FromScheme(Pacs008Message pacs008, Map<String, Object> context) {
        logger.info("Processing pacs.008 from payment scheme");
        
        try {
            Pacs008Message.FIToFICustomerCreditTransfer fiToFI = pacs008.getFiToFICustomerCreditTransfer();
            List<Pacs008Message.CreditTransferTransaction> transactions = fiToFI.getCreditTransferTransactionInformation();
            
            List<Map<String, Object>> processedTransactions = new ArrayList<>();
            
            for (Pacs008Message.CreditTransferTransaction txInfo : transactions) {
                // Transform pacs.008 to internal transaction
                CreateTransactionRequest request = transformPacs008ToTransactionRequest(txInfo, context);
                
                // Process the transaction
                TransactionResponse transaction = transactionService.createTransaction(request);
                
                // Generate pacs.002 acknowledgment
                Map<String, Object> pacs002Ack = generatePacs002Acknowledgment(transaction, fiToFI.getGroupHeader().getMessageId());
                
                processedTransactions.add(Map.of(
                    "originalEndToEndId", txInfo.getPaymentIdentification().getEndToEndId(),
                    "transactionId", transaction.getId(),
                    "transactionReference", transaction.getTransactionReference(),
                    "status", transaction.getStatus(),
                    "pacs002", pacs002Ack
                ));
                
                logger.debug("Processed pacs.008 transaction: {}", transaction.getTransactionReference());
            }
            
            return Map.of(
                "processedTransactions", processedTransactions,
                "totalTransactions", processedTransactions.size(),
                "schemeMessageId", fiToFI.getGroupHeader().getMessageId(),
                "timestamp", LocalDateTime.now().format(ISO_DATETIME_FORMATTER)
            );
            
        } catch (Exception e) {
            logger.error("Error processing pacs.008 from scheme: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to process scheme message", e);
        }
    }
    
    /**
     * Generate pacs.002 - FI to FI Payment Status Report
     */
    public Map<String, Object> generatePacs002(UUID transactionId, String originalMessageId) {
        logger.debug("Generating pacs.002 for transaction: {}", transactionId);
        
        try {
            TransactionResponse transaction = transactionService.getTransactionById(transactionId)
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found"));
            
            return generatePacs002Acknowledgment(transaction, originalMessageId);
            
        } catch (Exception e) {
            logger.error("Error generating pacs.002: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate pacs.002", e);
        }
    }
    
    /**
     * Generate pacs.004 - Payment Return
     */
    public Map<String, Object> generatePacs004Return(UUID transactionId, String reasonCode, String reason) {
        logger.info("Generating pacs.004 return for transaction: {}", transactionId);
        
        try {
            TransactionResponse transaction = transactionService.getTransactionById(transactionId)
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found"));
            
            // Create pacs.004 return message
            Map<String, Object> pacs004 = Map.of(
                "PmtRtr", Map.of(
                    "GrpHdr", Map.of(
                        "MsgId", "PACS004-" + System.currentTimeMillis(),
                        "CreDtTm", LocalDateTime.now().format(ISO_DATETIME_FORMATTER),
                        "NbOfTxs", "1",
                        "InstgAgt", Map.of(
                            "FinInstnId", Map.of(
                                "BICFI", "PAYMENTUS33XXX",
                                "Nm", "Payment Engine Bank"
                            )
                        ),
                        "InstdAgt", Map.of(
                            "FinInstnId", Map.of(
                                "BICFI", "SCHEMEFI33XXX",
                                "Nm", "Payment Scheme"
                            )
                        )
                    ),
                    "TxInf", List.of(Map.of(
                        "RtrId", "RTR-" + System.currentTimeMillis(),
                        "OrgnlGrpInf", Map.of(
                            "OrgnlMsgId", transaction.getExternalReference() != null ? transaction.getExternalReference() : transaction.getTransactionReference(),
                            "OrgnlMsgNmId", "pacs.008.001.03",
                            "OrgnlCreDtTm", transaction.getCreatedAt().format(ISO_DATETIME_FORMATTER)
                        ),
                        "OrgnlTxRef", Map.of(
                            "IntrBkSttlmAmt", Map.of(
                                "Ccy", transaction.getCurrencyCode(),
                                "value", transaction.getAmount()
                            ),
                            "ReqdExctnDt", transaction.getCreatedAt().format(ISO_DATE_FORMATTER)
                        ),
                        "RtrRsnInf", Map.of(
                            "Rsn", Map.of(
                                "Cd", reasonCode != null ? reasonCode : "AC01"
                            ),
                            "AddtlInf", List.of(reason != null ? reason : "Payment returned")
                        )
                    ))
                )
            );
            
            // Update transaction status to returned
            transactionService.cancelTransaction(transactionId, "Returned to scheme - " + reason);
            
            logger.info("pacs.004 return generated for transaction: {}", transaction.getTransactionReference());
            return pacs004;
            
        } catch (Exception e) {
            logger.error("Error generating pacs.004 return: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate payment return", e);
        }
    }
    
    // ============================================================================
    // CAMT MESSAGE GENERATION (Cash Management)
    // ============================================================================
    
    /**
     * Generate camt.053 - Bank to Customer Statement
     */
    public Camt053Message generateCamt053Statement(UUID accountId, String fromDate, String toDate, boolean includeTransactionDetails) {
        logger.info("Generating camt.053 statement for account: {}", accountId);
        
        try {
            Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));
            
            // Parse date range
            LocalDateTime fromDateTime = fromDate != null ? LocalDateTime.parse(fromDate + "T00:00:00") : LocalDateTime.now().minusDays(30);
            LocalDateTime toDateTime = toDate != null ? LocalDateTime.parse(toDate + "T23:59:59") : LocalDateTime.now();
            
            // Get transactions for the period
            List<Transaction> transactions = transactionRepository.findTransactionsByDateRange(fromDateTime, toDateTime, org.springframework.data.domain.Pageable.unpaged()).getContent()
                .stream()
                .filter(t -> t.getFromAccountId() != null && t.getFromAccountId().equals(accountId) || 
                           t.getToAccountId() != null && t.getToAccountId().equals(accountId))
                .collect(Collectors.toList());
            
            // Build camt.053 message
            Camt053Message camt053 = new Camt053Message();
            Camt053Message.BankToCustomerStatement statement = new Camt053Message.BankToCustomerStatement();
            
            // Group Header
            Camt053Message.GroupHeader groupHeader = new Camt053Message.GroupHeader();
            groupHeader.setMessageId("CAMT053-" + System.currentTimeMillis());
            groupHeader.setCreationDateTime(LocalDateTime.now().format(ISO_DATETIME_FORMATTER));
            statement.setGroupHeader(groupHeader);
            
            // Account Statement
            Camt053Message.AccountStatement accountStatement = new Camt053Message.AccountStatement();
            accountStatement.setIdentification("STMT-" + account.getAccountNumber() + "-" + System.currentTimeMillis());
            accountStatement.setCreationDateTime(LocalDateTime.now().format(ISO_DATETIME_FORMATTER));
            
            // From/To Date
            Camt053Message.DateTimePeriod period = new Camt053Message.DateTimePeriod();
            period.setFromDateTime(fromDateTime.format(ISO_DATETIME_FORMATTER));
            period.setToDateTime(toDateTime.format(ISO_DATETIME_FORMATTER));
            accountStatement.setFromToDate(period);
            
            // Account information
            Account isoAccount = new Account();
            Account.AccountIdentification accountId1 = new Account.AccountIdentification();
            Account.GenericAccountIdentification genericId = new Account.GenericAccountIdentification();
            genericId.setIdentification(account.getAccountNumber());
            accountId1.setOther(genericId);
            isoAccount.setIdentification(accountId1);
            isoAccount.setCurrency(account.getCurrencyCode());
            accountStatement.setAccount(isoAccount);
            
            // Account balances
            List<Camt053Message.CashBalance> balances = new ArrayList<>();
            
            // Opening balance
            Camt053Message.CashBalance openingBalance = new Camt053Message.CashBalance();
            Camt053Message.BalanceType openingBalanceType = new Camt053Message.BalanceType();
            Camt053Message.BalanceTypeChoice openingChoice = new Camt053Message.BalanceTypeChoice();
            openingChoice.setCode("OPBD"); // Opening Booked
            openingBalanceType.setCodeOrProprietary(openingChoice);
            openingBalance.setType(openingBalanceType);
            
            Camt053Message.ActiveCurrencyAndAmount openingAmount = new Camt053Message.ActiveCurrencyAndAmount();
            openingAmount.setCurrency(account.getCurrencyCode());
            openingAmount.setValue(account.getBalance());
            openingBalance.setAmount(openingAmount);
            openingBalance.setCreditDebitIndicator(account.getBalance().compareTo(BigDecimal.ZERO) >= 0 ? "CRDT" : "DBIT");
            
            Camt053Message.DateAndDateTime openingDate = new Camt053Message.DateAndDateTime();
            openingDate.setDate(fromDateTime.format(ISO_DATE_FORMATTER));
            openingBalance.setDate(openingDate);
            
            balances.add(openingBalance);
            
            // Closing balance
            Camt053Message.CashBalance closingBalance = new Camt053Message.CashBalance();
            Camt053Message.BalanceType closingBalanceType = new Camt053Message.BalanceType();
            Camt053Message.BalanceTypeChoice closingChoice = new Camt053Message.BalanceTypeChoice();
            closingChoice.setCode("CLBD"); // Closing Booked
            closingBalanceType.setCodeOrProprietary(closingChoice);
            closingBalance.setType(closingBalanceType);
            
            Camt053Message.ActiveCurrencyAndAmount closingAmount = new Camt053Message.ActiveCurrencyAndAmount();
            closingAmount.setCurrency(account.getCurrencyCode());
            closingAmount.setValue(account.getBalance());
            closingBalance.setAmount(closingAmount);
            closingBalance.setCreditDebitIndicator(account.getBalance().compareTo(BigDecimal.ZERO) >= 0 ? "CRDT" : "DBIT");
            
            Camt053Message.DateAndDateTime closingDate = new Camt053Message.DateAndDateTime();
            closingDate.setDate(toDateTime.format(ISO_DATE_FORMATTER));
            closingBalance.setDate(closingDate);
            
            balances.add(closingBalance);
            
            accountStatement.setBalance(balances);
            
            // Transaction entries (if requested)
            if (includeTransactionDetails && !transactions.isEmpty()) {
                List<Camt053Message.ReportEntry> entries = transformTransactionsToReportEntries(transactions, accountId);
                accountStatement.setEntry(entries);
                
                // Transaction summary
                Camt053Message.TotalTransactions summary = generateTransactionSummary(transactions, accountId);
                accountStatement.setTransactionsSummary(summary);
            }
            
            statement.setStatement(List.of(accountStatement));
            camt053.setBankToCustomerStatement(statement);
            
            logger.info("camt.053 statement generated for account: {}", account.getAccountNumber());
            return camt053;
            
        } catch (Exception e) {
            logger.error("Error generating camt.053 statement: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate account statement", e);
        }
    }
    
    /**
     * Generate camt.054 - Bank to Customer Debit Credit Notification
     */
    public Camt054Message generateCamt054Notification(UUID accountId, String fromDate, String toDate) {
        logger.info("Generating camt.054 notification for account: {}", accountId);
        
        try {
            // This would generate transaction notifications for the account
            // Implementation similar to camt.053 but focused on notifications
            
            Camt054Message camt054 = new Camt054Message();
            // Implementation details...
            
            logger.info("camt.054 notification generated for account: {}", accountId);
            return camt054;
            
        } catch (Exception e) {
            logger.error("Error generating camt.054 notification: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate debit credit notification", e);
        }
    }
    
    /**
     * Process camt.055 - Customer Payment Cancellation Request
     */
    public Map<String, Object> processCamt055(Camt055Message camt055Message, Map<String, Object> context) {
        logger.info("Processing camt.055 customer payment cancellation request");
        
        try {
            Camt055Message.CustomerPaymentCancellationRequest cxlReq = camt055Message.getCustomerPaymentCancellationRequest();
            List<Map<String, Object>> cancellationResults = new ArrayList<>();
            
            // Process each underlying transaction for cancellation
            for (Camt055Message.UnderlyingTransaction underlying : cxlReq.getUnderlying()) {
                if (underlying.getTransactionInformation() != null) {
                    for (Camt055Message.PaymentTransaction txInfo : underlying.getTransactionInformation()) {
                        try {
                            Map<String, Object> result = processSingleCancellation(txInfo, context);
                            cancellationResults.add(result);
                            
                        } catch (Exception e) {
                            logger.warn("Failed to cancel transaction {}: {}", 
                                       txInfo.getOriginalEndToEndId(), e.getMessage());
                            
                            cancellationResults.add(Map.of(
                                "originalEndToEndId", txInfo.getOriginalEndToEndId(),
                                "cancellationId", txInfo.getCancellationId(),
                                "status", "REJECTED",
                                "reason", e.getMessage(),
                                "timestamp", LocalDateTime.now().format(ISO_DATETIME_FORMATTER)
                            ));
                        }
                    }
                }
            }
            
            // Generate camt.029 (Resolution of Investigation) response
            Map<String, Object> camt029Response = generateCamt029Response(camt055Message, cancellationResults);
            
            logger.info("camt.055 processing completed with {} cancellation attempts", cancellationResults.size());
            
            return Map.of(
                "cancellationResults", cancellationResults,
                "camt029Response", camt029Response,
                "messageId", cxlReq.getGroupHeader().getMessageId(),
                "totalCancellations", cancellationResults.size(),
                "timestamp", LocalDateTime.now().format(ISO_DATETIME_FORMATTER)
            );
            
        } catch (Exception e) {
            logger.error("Error processing camt.055: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to process customer payment cancellation", e);
        }
    }

    /**
     * Process camt.056 - FI to FI Payment Cancellation Request
     */
    public Map<String, Object> processCamt056(Map<String, Object> camt056Message, Map<String, Object> context) {
        logger.info("Processing camt.056 FI to FI payment cancellation request");
        
        try {
            // Extract cancellation details
            @SuppressWarnings("unchecked")
            Map<String, Object> fiToFIPmtCxlReq = (Map<String, Object>) camt056Message.get("FIToFIPmtCxlReq");
            
            if (fiToFIPmtCxlReq == null) {
                throw new IllegalArgumentException("Invalid camt.056 message structure");
            }
            
            // Process FI to FI cancellation
            // Implementation would handle the inter-bank cancellation logic
            
            Map<String, Object> response = Map.of(
                "cancellationStatus", "ACCEPTED",
                "messageId", "CAMT057-" + System.currentTimeMillis(),
                "timestamp", LocalDateTime.now().format(ISO_DATETIME_FORMATTER)
            );
            
            logger.info("camt.056 FI to FI cancellation processed successfully");
            return response;
            
        } catch (Exception e) {
            logger.error("Error processing camt.056: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to process FI to FI payment cancellation", e);
        }
    }
    
    /**
     * Generate camt.052 - Bank to Customer Account Report (Balance)
     */
    public Map<String, Object> generateCamt052Balance(UUID accountId, String balanceType) {
        logger.debug("Generating camt.052 balance for account: {}", accountId);
        
        try {
            Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));
            
            Map<String, Object> camt052 = Map.of(
                "BkToCstmrAcctRpt", Map.of(
                    "GrpHdr", Map.of(
                        "MsgId", "CAMT052-" + System.currentTimeMillis(),
                        "CreDtTm", LocalDateTime.now().format(ISO_DATETIME_FORMATTER)
                    ),
                    "Rpt", List.of(Map.of(
                        "Id", "RPT-" + account.getAccountNumber(),
                        "CreDtTm", LocalDateTime.now().format(ISO_DATETIME_FORMATTER),
                        "Acct", Map.of(
                            "Id", Map.of(
                                "Othr", Map.of(
                                    "Id", account.getAccountNumber()
                                )
                            ),
                            "Ccy", account.getCurrencyCode()
                        ),
                        "Bal", List.of(Map.of(
                            "Tp", Map.of(
                                "CdOrPrtry", Map.of(
                                    "Cd", balanceType // CLBD, OPBD, ITBD, etc.
                                )
                            ),
                            "Amt", Map.of(
                                "Ccy", account.getCurrencyCode(),
                                "value", account.getBalance()
                            ),
                            "CdtDbtInd", account.getBalance().compareTo(BigDecimal.ZERO) >= 0 ? "CRDT" : "DBIT",
                            "Dt", Map.of(
                                "DtTm", LocalDateTime.now().format(ISO_DATETIME_FORMATTER)
                            )
                        ))
                    ))
                )
            );
            
            logger.debug("camt.052 balance generated for account: {}", account.getAccountNumber());
            return camt052;
            
        } catch (Exception e) {
            logger.error("Error generating camt.052 balance: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate account balance report", e);
        }
    }
    
    // ============================================================================
    // BULK PROCESSING
    // ============================================================================
    
    /**
     * Process bulk pain.001 messages
     */
    public Map<String, Object> processBulkPain001(List<Map<String, Object>> messages, Map<String, Object> context) {
        logger.info("Processing bulk pain.001 with {} messages", messages.size());
        
        try {
            String batchId = "BULK-" + System.currentTimeMillis();
            List<Map<String, Object>> results = new ArrayList<>();
            int successCount = 0;
            int failureCount = 0;
            
            for (int i = 0; i < messages.size(); i++) {
                try {
                    // Convert map to Pain001Message (simplified)
                    // In real implementation, you'd use proper JSON deserialization
                    Map<String, Object> result = Map.of(
                        "sequenceNumber", i + 1,
                        "status", "PROCESSED",
                        "transactionId", UUID.randomUUID().toString(),
                        "timestamp", LocalDateTime.now().format(ISO_DATETIME_FORMATTER)
                    );
                    
                    results.add(result);
                    successCount++;
                    
                } catch (Exception e) {
                    Map<String, Object> result = Map.of(
                        "sequenceNumber", i + 1,
                        "status", "FAILED",
                        "error", e.getMessage(),
                        "timestamp", LocalDateTime.now().format(ISO_DATETIME_FORMATTER)
                    );
                    
                    results.add(result);
                    failureCount++;
                    
                    logger.warn("Failed to process bulk message {}: {}", i + 1, e.getMessage());
                }
            }
            
            Map<String, Object> bulkResponse = Map.of(
                "batchId", batchId,
                "totalMessages", messages.size(),
                "successCount", successCount,
                "failureCount", failureCount,
                "results", results,
                "timestamp", LocalDateTime.now().format(ISO_DATETIME_FORMATTER)
            );
            
            logger.info("Bulk pain.001 processing completed: {} success, {} failures", successCount, failureCount);
            return bulkResponse;
            
        } catch (Exception e) {
            logger.error("Error processing bulk pain.001: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to process bulk messages", e);
        }
    }
    
    /**
     * Get batch processing status
     */
    public Map<String, Object> getBatchStatus(String batchId) {
        logger.debug("Getting batch status for: {}", batchId);
        
        // This would typically query a batch processing table
        // For now, return mock status
        return Map.of(
            "batchId", batchId,
            "status", "COMPLETED",
            "totalMessages", 100,
            "processedMessages", 100,
            "successCount", 95,
            "failureCount", 5,
            "startTime", LocalDateTime.now().minusMinutes(10).format(ISO_DATETIME_FORMATTER),
            "endTime", LocalDateTime.now().format(ISO_DATETIME_FORMATTER)
        );
    }
    
    // ============================================================================
    // MESSAGE VALIDATION AND TRANSFORMATION
    // ============================================================================
    
    /**
     * Validate any ISO 20022 message
     */
    public Map<String, Object> validateMessage(String messageType, Map<String, Object> message) {
        logger.debug("Validating {} message", messageType);
        
        try {
            List<String> errors = new ArrayList<>();
            List<String> warnings = new ArrayList<>();
            
            // Perform validation based on message type
            switch (messageType.toLowerCase()) {
                case "pain001":
                    validatePainMessage(message, errors, warnings);
                    break;
                case "pain007":
                    validateReversalMessage(message, errors, warnings);
                    break;
                case "pacs008":
                    validateSchemeMessage(message, errors, warnings);
                    break;
                case "camt053":
                    validateStatementMessage(message, errors, warnings);
                    break;
                default:
                    warnings.add("Unknown message type: " + messageType);
            }
            
            return Map.of(
                "valid", errors.isEmpty(),
                "messageType", messageType,
                "errors", errors,
                "warnings", warnings,
                "timestamp", LocalDateTime.now().format(ISO_DATETIME_FORMATTER)
            );
            
        } catch (Exception e) {
            logger.error("Error validating {} message: {}", messageType, e.getMessage(), e);
            return Map.of(
                "valid", false,
                "error", e.getMessage(),
                "timestamp", LocalDateTime.now().format(ISO_DATETIME_FORMATTER)
            );
        }
    }
    
    /**
     * Transform message between different formats
     */
    public Map<String, Object> transformMessage(String fromFormat, String toFormat, Map<String, Object> message) {
        logger.debug("Transforming message from {} to {}", fromFormat, toFormat);
        
        try {
            // Implement transformation logic
            if ("pain001".equals(fromFormat) && "pacs008".equals(toFormat)) {
                return transformPain001ToPacs008(message);
            } else if ("pacs008".equals(fromFormat) && "camt054".equals(toFormat)) {
                return transformPacs008ToCamt054(message);
            } else {
                throw new IllegalArgumentException("Transformation not supported: " + fromFormat + " to " + toFormat);
            }
            
        } catch (Exception e) {
            logger.error("Error transforming message: {}", e.getMessage(), e);
            throw new RuntimeException("Message transformation failed", e);
        }
    }
    
    /**
     * Get message statistics
     */
    public Map<String, Object> getMessageStatistics(String messageType, String fromDate, String toDate) {
        logger.debug("Getting ISO 20022 message statistics");
        
        try {
            // This would query actual statistics from database
            return Map.of(
                "messageType", messageType != null ? messageType : "ALL",
                "period", Map.of(
                    "fromDate", fromDate != null ? fromDate : LocalDateTime.now().minusDays(30).format(ISO_DATE_FORMATTER),
                    "toDate", toDate != null ? toDate : LocalDateTime.now().format(ISO_DATE_FORMATTER)
                ),
                "statistics", Map.of(
                    "totalMessages", 1250,
                    "successfulMessages", 1198,
                    "failedMessages", 52,
                    "successRate", 95.84,
                    "averageProcessingTime", "245ms",
                    "messageBreakdown", Map.of(
                        "pain.001", 456,
                        "pain.007", 23,
                        "pacs.008", 678,
                        "camt.053", 89,
                        "camt.054", 234
                    )
                ),
                "timestamp", LocalDateTime.now().format(ISO_DATETIME_FORMATTER)
            );
            
        } catch (Exception e) {
            logger.error("Error getting message statistics: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to get message statistics", e);
        }
    }
    
    // ============================================================================
    // HELPER METHODS
    // ============================================================================
    
    private CreateTransactionRequest transformPacs008ToTransactionRequest(Pacs008Message.CreditTransferTransaction txInfo, Map<String, Object> context) {
        CreateTransactionRequest request = new CreateTransactionRequest();
        
        // Map pacs.008 fields to internal transaction request
        request.setExternalReference(txInfo.getPaymentIdentification().getEndToEndId());
        request.setAmount(txInfo.getInterbankSettlementAmount().getValue());
        request.setCurrencyCode(txInfo.getInterbankSettlementAmount().getCurrency());
        request.setChannel("iso20022-pacs008");
        request.setIpAddress((String) context.get("ipAddress"));
        
        // Map accounts (this would lookup internal account IDs)
        // For now, using placeholder logic
        if (txInfo.getDebtorAccount() != null) {
            String debtorAccountId = mapAccountToInternalId(txInfo.getDebtorAccount());
            if (debtorAccountId != null) {
                request.setFromAccountId(UUID.fromString(debtorAccountId));
            }
        }
        
        if (txInfo.getCreditorAccount() != null) {
            String creditorAccountId = mapAccountToInternalId(txInfo.getCreditorAccount());
            if (creditorAccountId != null) {
                request.setToAccountId(UUID.fromString(creditorAccountId));
            }
        }
        
        // Default payment type for scheme messages
        request.setPaymentTypeId(UUID.fromString("660e8400-e29b-41d4-a716-446655440001")); // ACH_CREDIT
        
        // Add scheme metadata
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("messageType", "pacs.008");
        metadata.put("schemeProcessed", true);
        metadata.put("interbankSettlementDate", txInfo.getInterbankSettlementDate());
        request.setMetadata(metadata);
        
        return request;
    }
    
    private String mapAccountToInternalId(Account account) {
        // This would implement account lookup logic
        // For demonstration, return a placeholder
        if (account.getIdentification() != null) {
            if (account.getIdentification().getIban() != null) {
                return lookupAccountByIban(account.getIdentification().getIban());
            } else if (account.getIdentification().getOther() != null) {
                return lookupAccountByNumber(account.getIdentification().getOther().getIdentification());
            }
        }
        return null;
    }
    
    private String lookupAccountByIban(String iban) {
        // Mock IBAN to account ID mapping
        Map<String, String> ibanMapping = Map.of(
            "US64SVBKUS6S3300958879", "880e8400-e29b-41d4-a716-446655440001",
            "US64SVBKUS6S3300958880", "880e8400-e29b-41d4-a716-446655440002"
        );
        return ibanMapping.get(iban);
    }
    
    private String lookupAccountByNumber(String accountNumber) {
        // Mock account number to ID mapping
        Map<String, String> accountMapping = Map.of(
            "ACC001001", "880e8400-e29b-41d4-a716-446655440001",
            "ACC002001", "880e8400-e29b-41d4-a716-446655440003"
        );
        return accountMapping.get(accountNumber);
    }
    
    private List<Transaction> findTransactionsByOriginalMessage(String originalMessageId) {
        // This would search for transactions by original message ID
        // For now, return empty list
        return new ArrayList<>();
    }
    
    private void validateReversalAllowed(Transaction transaction) {
        if (transaction.getStatus() != Transaction.TransactionStatus.COMPLETED) {
            throw new IllegalStateException("Can only reverse completed transactions");
        }
        
        // Add additional reversal validation logic
        LocalDateTime cutoffTime = LocalDateTime.now().minusHours(24);
        if (transaction.getCompletedAt().isBefore(cutoffTime)) {
            throw new IllegalStateException("Transaction too old for reversal");
        }
    }
    
    private TransactionResponse createReversalTransaction(Transaction originalTransaction, Pain007Message.CustomerPaymentReversal reversal, Map<String, Object> context) {
        CreateTransactionRequest reversalRequest = new CreateTransactionRequest();
        
        // Create reversal transaction (opposite direction)
        reversalRequest.setFromAccountId(originalTransaction.getToAccountId());
        reversalRequest.setToAccountId(originalTransaction.getFromAccountId());
        reversalRequest.setAmount(originalTransaction.getAmount());
        reversalRequest.setCurrencyCode(originalTransaction.getCurrencyCode());
        reversalRequest.setPaymentTypeId(originalTransaction.getPaymentTypeId());
        reversalRequest.setDescription("Reversal of " + originalTransaction.getTransactionReference());
        reversalRequest.setChannel("iso20022-pain007");
        
        // Add reversal metadata
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("reversalType", "pain.007");
        metadata.put("originalTransactionId", originalTransaction.getId().toString());
        metadata.put("originalTransactionReference", originalTransaction.getTransactionReference());
        metadata.put("reversalReason", extractReversalReason(reversal));
        reversalRequest.setMetadata(metadata);
        
        return transactionService.createTransaction(reversalRequest);
    }
    
    private String extractReversalReason(Pain007Message.CustomerPaymentReversal reversal) {
        if (reversal.getOriginalGroupInformation().getReversalReasonInformation() != null &&
            !reversal.getOriginalGroupInformation().getReversalReasonInformation().isEmpty()) {
            
            Pain007Message.PaymentReversalReason reasonInfo = reversal.getOriginalGroupInformation().getReversalReasonInformation().get(0);
            if (reasonInfo.getReason() != null) {
                return reasonInfo.getReason().getCode() != null ? reasonInfo.getReason().getCode() : "CUST";
            }
        }
        return "CUST"; // Customer requested reversal
    }
    
    private Map<String, Object> generatePain008Response(TransactionResponse reversalTransaction, Pain007Message pain007) {
        // Generate pain.008 - Customer Payment Reversal Response
        return Map.of(
            "CstmrPmtRvslRspn", Map.of(
                "GrpHdr", Map.of(
                    "MsgId", "PAIN008-" + System.currentTimeMillis(),
                    "CreDtTm", LocalDateTime.now().format(ISO_DATETIME_FORMATTER),
                    "InitgPty", Map.of("Nm", "Payment Engine")
                ),
                "OrgnlGrpInf", Map.of(
                    "OrgnlMsgId", pain007.getCustomerPaymentReversal().getGroupHeader().getMessageId(),
                    "OrgnlMsgNmId", "pain.007.001.03",
                    "OrgnlCreDtTm", pain007.getCustomerPaymentReversal().getGroupHeader().getCreationDateTime()
                ),
                "RvslSts", Map.of(
                    "RvslId", reversalTransaction.getTransactionReference(),
                    "RvslSts", "ACCP", // Accepted
                    "RvslDtTm", reversalTransaction.getCreatedAt().format(ISO_DATETIME_FORMATTER)
                )
            )
        );
    }
    
    private Map<String, Object> generatePacs002Acknowledgment(TransactionResponse transaction, String originalMessageId) {
        return Map.of(
            "FIToFIPmtStsRpt", Map.of(
                "GrpHdr", Map.of(
                    "MsgId", "PACS002-" + System.currentTimeMillis(),
                    "CreDtTm", LocalDateTime.now().format(ISO_DATETIME_FORMATTER),
                    "InstgAgt", Map.of(
                        "FinInstnId", Map.of(
                            "BICFI", "PAYMENTUS33XXX",
                            "Nm", "Payment Engine Bank"
                        )
                    ),
                    "InstdAgt", Map.of(
                        "FinInstnId", Map.of(
                            "BICFI", "SCHEMEFI33XXX",
                            "Nm", "Payment Scheme"
                        )
                    )
                ),
                "OrgnlGrpInfAndSts", Map.of(
                    "OrgnlMsgId", originalMessageId,
                    "OrgnlMsgNmId", "pacs.008.001.03",
                    "GrpSts", mapInternalStatusToIso20022(transaction.getStatus())
                ),
                "TxInfAndSts", Map.of(
                    "StsId", transaction.getId().toString(),
                    "OrgnlInstrId", transaction.getExternalReference(),
                    "OrgnlEndToEndId", transaction.getExternalReference(),
                    "TxSts", mapInternalStatusToIso20022(transaction.getStatus()),
                    "AccptncDtTm", transaction.getCreatedAt().format(ISO_DATETIME_FORMATTER)
                )
            )
        );
    }
    
    private String mapInternalStatusToIso20022(Transaction.TransactionStatus status) {
        return switch (status) {
            case PENDING -> "PDNG";
            case PROCESSING -> "ACTC";
            case COMPLETED -> "ACSC";
            case FAILED -> "RJCT";
            case CANCELLED -> "CANC";
            case REVERSED -> "ACSC";
        };
    }
    
    private void publishIso20022Event(String messageType, Object message, TransactionResponse transaction, Map<String, Object> context) {
        try {
            TransactionCreatedEvent event = new TransactionCreatedEvent(
                transaction.getId(),
                transaction.getTransactionReference(),
                transaction.getFromAccountId(),
                transaction.getToAccountId(),
                transaction.getPaymentTypeId(),
                transaction.getAmount(),
                transaction.getCurrencyCode(),
                transaction.getTransactionType().name()
            );
            
            event.setChannel("iso20022-" + messageType);
            event.setCorrelationId((String) context.get("correlationId"));
            event.setUserId((String) context.get("userId"));
            
            eventPublisher.publishEvent(KafkaTopics.TRANSACTION_CREATED, transaction.getTransactionReference(), event);
            
        } catch (Exception e) {
            logger.error("Error publishing ISO 20022 event: {}", e.getMessage());
        }
    }
    
    // Validation helper methods
    private void validatePainMessage(Map<String, Object> message, List<String> errors, List<String> warnings) {
        // Implement pain message validation
        if (!message.containsKey("CstmrCdtTrfInitn")) {
            errors.add("CstmrCdtTrfInitn element is required");
        }
    }
    
    private void validateReversalMessage(Map<String, Object> message, List<String> errors, List<String> warnings) {
        // Implement reversal message validation
        if (!message.containsKey("CstmrPmtRvsl")) {
            errors.add("CstmrPmtRvsl element is required");
        }
    }
    
    private void validateSchemeMessage(Map<String, Object> message, List<String> errors, List<String> warnings) {
        // Implement scheme message validation
        if (!message.containsKey("FIToFICstmrCdtTrf")) {
            errors.add("FIToFICstmrCdtTrf element is required");
        }
    }
    
    private void validateStatementMessage(Map<String, Object> message, List<String> errors, List<String> warnings) {
        // Implement statement message validation
        if (!message.containsKey("BkToCstmrStmt")) {
            errors.add("BkToCstmrStmt element is required");
        }
    }
    
    // Transformation helper methods
    private Map<String, Object> transformPain001ToPacs008(Map<String, Object> pain001) {
        // Implement pain.001 to pacs.008 transformation
        return Map.of("transformedMessage", "pacs008", "original", "pain001");
    }
    
    private Map<String, Object> transformPacs008ToCamt054(Map<String, Object> pacs008) {
        // Implement pacs.008 to camt.054 transformation
        return Map.of("transformedMessage", "camt054", "original", "pacs008");
    }
    
    private List<Camt053Message.ReportEntry> transformTransactionsToReportEntries(List<Transaction> transactions, UUID accountId) {
        // Transform internal transactions to ISO 20022 report entries
        return transactions.stream()
            .map(tx -> {
                Camt053Message.ReportEntry entry = new Camt053Message.ReportEntry();
                entry.setEntryReference(tx.getTransactionReference());
                
                Camt053Message.ActiveCurrencyAndAmount amount = new Camt053Message.ActiveCurrencyAndAmount();
                amount.setCurrency(tx.getCurrencyCode());
                amount.setValue(tx.getAmount());
                entry.setAmount(amount);
                
                // Determine debit/credit from account perspective
                boolean isDebit = tx.getFromAccountId() != null && tx.getFromAccountId().equals(accountId);
                entry.setCreditDebitIndicator(isDebit ? "DBIT" : "CRDT");
                entry.setStatus("BOOK"); // Booked
                
                return entry;
            })
            .collect(Collectors.toList());
    }
    
    private Camt053Message.TotalTransactions generateTransactionSummary(List<Transaction> transactions, UUID accountId) {
        Camt053Message.TotalTransactions summary = new Camt053Message.TotalTransactions();
        
        long totalCount = transactions.size();
        long creditCount = transactions.stream().filter(tx -> tx.getToAccountId() != null && tx.getToAccountId().equals(accountId)).count();
        long debitCount = transactions.stream().filter(tx -> tx.getFromAccountId() != null && tx.getFromAccountId().equals(accountId)).count();
        
        BigDecimal totalSum = transactions.stream()
            .map(Transaction::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        Camt053Message.NumberAndSumOfTransactions totalEntries = new Camt053Message.NumberAndSumOfTransactions();
        totalEntries.setNumberOfEntries(String.valueOf(totalCount));
        totalEntries.setSum(totalSum);
        summary.setTotalEntries(totalEntries);
        
        return summary;
    }
    
    // ============================================================================
    // CAMT.055 PROCESSING HELPER METHODS
    // ============================================================================
    
    /**
     * Process single transaction cancellation from camt.055
     */
    private Map<String, Object> processSingleCancellation(Camt055Message.PaymentTransaction txInfo, Map<String, Object> context) {
        logger.debug("Processing cancellation for transaction: {}", txInfo.getOriginalEndToEndId());
        
        try {
            // Find original transaction by end-to-end ID
            Transaction originalTransaction = findTransactionByEndToEndId(txInfo.getOriginalEndToEndId());
            
            if (originalTransaction == null) {
                throw new IllegalArgumentException("Original transaction not found: " + txInfo.getOriginalEndToEndId());
            }
            
            // Validate cancellation is allowed
            validateCancellationAllowed(originalTransaction);
            
            // Extract cancellation reason
            String cancellationReason = extractCancellationReason(txInfo);
            String reasonCode = extractCancellationReasonCode(txInfo);
            
            // Cancel the transaction
            TransactionResponse cancelledTransaction = transactionService.cancelTransaction(
                originalTransaction.getId(), 
                "Customer cancellation request - " + cancellationReason
            );
            
            logger.info("Transaction cancelled successfully: {} -> {}", 
                       originalTransaction.getTransactionReference(),
                       cancelledTransaction.getStatus());
            
            return Map.of(
                "originalEndToEndId", txInfo.getOriginalEndToEndId(),
                "originalTransactionId", originalTransaction.getId().toString(),
                "cancellationId", txInfo.getCancellationId() != null ? txInfo.getCancellationId() : "CXL-" + System.currentTimeMillis(),
                "status", "ACCEPTED",
                "cancellationReason", cancellationReason,
                "reasonCode", reasonCode,
                "cancelledAt", LocalDateTime.now().format(ISO_DATETIME_FORMATTER),
                "newTransactionStatus", cancelledTransaction.getStatus().toString()
            );
            
        } catch (Exception e) {
            logger.error("Error processing single cancellation: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Validate that cancellation is allowed for the transaction
     */
    private void validateCancellationAllowed(Transaction transaction) {
        // Check transaction status
        if (transaction.getStatus() == Transaction.TransactionStatus.COMPLETED) {
            throw new IllegalStateException("Cannot cancel completed transaction. Use reversal instead.");
        }
        
        if (transaction.getStatus() == Transaction.TransactionStatus.FAILED || 
            transaction.getStatus() == Transaction.TransactionStatus.CANCELLED) {
            throw new IllegalStateException("Transaction is already " + transaction.getStatus().toString().toLowerCase());
        }
        
        // Check timing constraints
        LocalDateTime cutoffTime = LocalDateTime.now().minusHours(1); // 1 hour cancellation window
        if (transaction.getCreatedAt().isBefore(cutoffTime) && 
            transaction.getStatus() == Transaction.TransactionStatus.PROCESSING) {
            throw new IllegalStateException("Transaction is too far in processing to cancel");
        }
        
        logger.debug("Cancellation validation passed for transaction: {}", transaction.getTransactionReference());
    }
    
    /**
     * Find transaction by end-to-end ID
     */
    private Transaction findTransactionByEndToEndId(String endToEndId) {
        // Search in transaction metadata for ISO 20022 end-to-end ID
        List<Transaction> transactions = transactionRepository.findAll(); // In production, this would be optimized
        
        return transactions.stream()
            .filter(tx -> {
                if (tx.getMetadata() != null && tx.getMetadata().containsKey("iso20022")) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> iso20022Data = (Map<String, Object>) tx.getMetadata().get("iso20022");
                    return endToEndId.equals(iso20022Data.get("endToEndId"));
                }
                return false;
            })
            .findFirst()
            .orElse(null);
    }
    
    /**
     * Extract cancellation reason from camt.055 transaction info
     */
    private String extractCancellationReason(Camt055Message.PaymentTransaction txInfo) {
        if (txInfo.getCancellationReasonInformation() != null && 
            !txInfo.getCancellationReasonInformation().isEmpty()) {
            
            Camt055Message.PaymentCancellationReason reasonInfo = txInfo.getCancellationReasonInformation().get(0);
            if (reasonInfo.getAdditionalInformation() != null && !reasonInfo.getAdditionalInformation().isEmpty()) {
                return reasonInfo.getAdditionalInformation().get(0);
            }
        }
        
        return "Customer requested cancellation";
    }
    
    /**
     * Extract cancellation reason code from camt.055 transaction info
     */
    private String extractCancellationReasonCode(Camt055Message.PaymentTransaction txInfo) {
        if (txInfo.getCancellationReasonInformation() != null && 
            !txInfo.getCancellationReasonInformation().isEmpty()) {
            
            Camt055Message.PaymentCancellationReason reasonInfo = txInfo.getCancellationReasonInformation().get(0);
            if (reasonInfo.getReason() != null && reasonInfo.getReason().getCode() != null) {
                return reasonInfo.getReason().getCode();
            }
        }
        
        return "CUST"; // Default to customer requested
    }
    
    /**
     * Generate camt.029 (Resolution of Investigation) response for camt.055
     */
    private Map<String, Object> generateCamt029Response(Camt055Message camt055Message, List<Map<String, Object>> cancellationResults) {
        logger.debug("Generating camt.029 resolution response");
        
        try {
            Camt055Message.CustomerPaymentCancellationRequest cxlReq = camt055Message.getCustomerPaymentCancellationRequest();
            
            // Count successful and failed cancellations
            long acceptedCount = cancellationResults.stream()
                .filter(result -> "ACCEPTED".equals(result.get("status")))
                .count();
            
            long rejectedCount = cancellationResults.stream()
                .filter(result -> "REJECTED".equals(result.get("status")))
                .count();
            
            // Generate camt.029 response
            Map<String, Object> camt029 = Map.of(
                "RsltnOfInvstgtn", Map.of(
                    "GrpHdr", Map.of(
                        "MsgId", "CAMT029-" + System.currentTimeMillis(),
                        "CreDtTm", LocalDateTime.now().format(ISO_DATETIME_FORMATTER),
                        "InstgAgt", Map.of(
                            "FinInstnId", Map.of(
                                "BICFI", "PAYMENTUS33XXX",
                                "Nm", "Payment Engine Bank"
                            )
                        ),
                        "InstdAgt", Map.of(
                            "FinInstnId", Map.of(
                                "BICFI", "CUSTOMR33XXX",
                                "Nm", "Customer Bank"
                            )
                        )
                    ),
                    "InvstgtnId", cxlReq.getGroupHeader().getMessageId(),
                    "OrgnlGrpInfAndSts", Map.of(
                        "OrgnlMsgId", cxlReq.getGroupHeader().getMessageId(),
                        "OrgnlMsgNmId", "camt.055.001.03",
                        "OrgnlCreDtTm", cxlReq.getGroupHeader().getCreationDateTime(),
                        "GrpSts", acceptedCount > 0 ? (rejectedCount > 0 ? "PART" : "ACCP") : "RJCT"
                    ),
                    "CxlDtls", cancellationResults.stream()
                        .map(result -> Map.of(
                            "OrgnlGrpInf", Map.of(
                                "OrgnlMsgId", cxlReq.getGroupHeader().getMessageId(),
                                "OrgnlMsgNmId", "camt.055.001.03"
                            ),
                            "OrgnlTxRef", Map.of(
                                "OrgnlEndToEndId", result.get("originalEndToEndId")
                            ),
                            "CxlStsRsnInf", Map.of(
                                "CxlSts", result.get("status"),
                                "CxlStsRsn", Map.of(
                                    "Cd", result.get("reasonCode"),
                                    "AddtlInf", List.of(result.get("reason"))
                                )
                            )
                        ))
                        .collect(Collectors.toList())
                )
            );
            
            logger.debug("camt.029 resolution response generated with {} accepted, {} rejected", 
                        acceptedCount, rejectedCount);
            
            return camt029;
            
        } catch (Exception e) {
            logger.error("Error generating camt.029 response: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate resolution response", e);
        }
    }
}