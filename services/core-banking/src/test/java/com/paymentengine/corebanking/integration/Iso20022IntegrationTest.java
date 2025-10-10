package com.paymentengine.corebanking.integration.iso20022;

import com.paymentengine.corebanking.entity.Account;
import com.paymentengine.corebanking.entity.PaymentType;
import com.paymentengine.corebanking.entity.Transaction;
import com.paymentengine.corebanking.repository.AccountRepository;
import com.paymentengine.corebanking.repository.PaymentTypeRepository;
import com.paymentengine.corebanking.service.Iso20022ProcessingService;
import com.paymentengine.shared.dto.iso20022.Pain001Message;
import com.paymentengine.shared.dto.iso20022.Camt055Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for ISO 20022 Processing Service
 */
@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
@Transactional
class Iso20022IntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("payment_engine_test")
            .withUsername("test_user")
            .withPassword("test_pass");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private Iso20022ProcessingService iso20022ProcessingService;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private PaymentTypeRepository paymentTypeRepository;

    private Account fromAccount;
    private Account toAccount;
    private PaymentType paymentType;

    @BeforeEach
    void setUp() {
        // Create test accounts
        fromAccount = createTestAccount("ACC001", new BigDecimal("10000.00"));
        toAccount = createTestAccount("ACC002", new BigDecimal("5000.00"));
        
        // Create test payment type
        paymentType = createTestPaymentType();
    }

    @Test
    @DisplayName("Should process pain.001 message successfully")
    void shouldProcessPain001MessageSuccessfully() {
        // Given
        Pain001Message pain001 = createTestPain001Message();
        Map<String, Object> context = Map.of(
            "ipAddress", "127.0.0.1",
            "userId", "test-user",
            "correlationId", "test-correlation-id"
        );

        // When
        Map<String, Object> result = iso20022ProcessingService.processPain001(pain001, context);

        // Then
        assertNotNull(result);
        assertTrue(result.containsKey("responseMode"));
        assertTrue(result.containsKey("transactionId"));
        assertTrue(result.containsKey("status"));
    }

    @Test
    @DisplayName("Should process camt.055 cancellation successfully")
    void shouldProcessCamt055CancellationSuccessfully() {
        // Given
        // First create a transaction to cancel
        Transaction transaction = createTestTransaction();
        
        Camt055Message camt055 = createTestCamt055Message(transaction);
        Map<String, Object> context = Map.of(
            "ipAddress", "127.0.0.1",
            "userId", "test-user"
        );

        // When
        Map<String, Object> result = iso20022ProcessingService.processCamt055(camt055, context);

        // Then
        assertNotNull(result);
        assertTrue(result.containsKey("cancellationResults"));
        assertTrue(result.containsKey("camt029Response"));
        assertTrue(result.containsKey("totalCancellations"));
    }

    @Test
    @DisplayName("Should validate message successfully")
    void shouldValidateMessageSuccessfully() {
        // Given
        Map<String, Object> testMessage = Map.of(
            "CstmrCdtTrfInitn", Map.of(
                "GrpHdr", Map.of(
                    "MsgId", "MSG-TEST-001",
                    "CreDtTm", LocalDateTime.now().toString(),
                    "NbOfTxs", "1"
                )
            )
        );

        // When
        Map<String, Object> result = iso20022ProcessingService.validateMessage("pain001", testMessage);

        // Then
        assertNotNull(result);
        assertTrue(result.containsKey("valid"));
        assertTrue(result.containsKey("messageType"));
        assertEquals("pain001", result.get("messageType"));
    }

    @Test
    @DisplayName("Should generate message statistics")
    void shouldGenerateMessageStatistics() {
        // When
        Map<String, Object> stats = iso20022ProcessingService.getMessageStatistics("pain001", "2024-01-01", "2024-01-31");

        // Then
        assertNotNull(stats);
        assertTrue(stats.containsKey("messageType"));
        assertTrue(stats.containsKey("statistics"));
        assertTrue(stats.containsKey("period"));
        assertEquals("pain001", stats.get("messageType"));
    }

    @Test
    @DisplayName("Should handle bulk processing")
    void shouldHandleBulkProcessing() {
        // Given
        java.util.List<Map<String, Object>> bulkMessages = java.util.List.of(
            Map.of("message", "1"),
            Map.of("message", "2"),
            Map.of("message", "3")
        );
        Map<String, Object> context = Map.of("ipAddress", "127.0.0.1");

        // When
        Map<String, Object> result = iso20022ProcessingService.processBulkPain001(bulkMessages, context);

        // Then
        assertNotNull(result);
        assertTrue(result.containsKey("batchId"));
        assertTrue(result.containsKey("totalMessages"));
        assertTrue(result.containsKey("results"));
        assertEquals(3, result.get("totalMessages"));
    }

    private Pain001Message createTestPain001Message() {
        // Create a basic pain.001 message structure
        Pain001Message pain001 = new Pain001Message();
        Pain001Message.CustomerCreditTransferInitiation ccti = new Pain001Message.CustomerCreditTransferInitiation();
        
        // Group Header
        Pain001Message.GroupHeader groupHeader = new Pain001Message.GroupHeader();
        groupHeader.setMessageId("MSG-TEST-001");
        groupHeader.setCreationDateTime(LocalDateTime.now());
        groupHeader.setNumberOfTransactions("1");
        groupHeader.setControlSum(new BigDecimal("1000.00"));
        groupHeader.setInitiatingParty(new Pain001Message.Party("Test Bank", null, null));
        ccti.setGroupHeader(groupHeader);
        
        // Payment Information
        Pain001Message.PaymentInformation paymentInfo = new Pain001Message.PaymentInformation();
        paymentInfo.setPaymentInformationId("PMT-TEST-001");
        paymentInfo.setPaymentMethod("TRF");
        paymentInfo.setRequestedExecutionDate(LocalDateTime.now().toLocalDate());
        paymentInfo.setDebtor(new Pain001Message.Party("Test Debtor", null, null));
        paymentInfo.setDebtorAccount(createTestAccountInfo(fromAccount.getAccountNumber()));
        paymentInfo.setCreditor(new Pain001Message.Party("Test Creditor", null, null));
        paymentInfo.setCreditorAccount(createTestAccountInfo(toAccount.getAccountNumber()));
        
        // Credit Transfer Transaction
        Pain001Message.CreditTransferTransaction ctt = new Pain001Message.CreditTransferTransaction();
        ctt.setPaymentId(new Pain001Message.PaymentIdentification("INSTR-TEST-001", "E2E-TEST-001"));
        ctt.setAmount(new Pain001Message.Amount(new BigDecimal("1000.00"), "USD"));
        ctt.setRemittanceInformation("Test payment");
        paymentInfo.setCreditTransferTransactionInformation(java.util.List.of(ctt));
        
        ccti.setPaymentInformation(java.util.List.of(paymentInfo));
        pain001.setCustomerCreditTransferInitiation(ccti);
        
        return pain001;
    }

    private Camt055Message createTestCamt055Message(Transaction transaction) {
        Camt055Message camt055 = new Camt055Message();
        Camt055Message.CustomerPaymentCancellationRequest cpcReq = new Camt055Message.CustomerPaymentCancellationRequest();
        
        // Group Header
        Camt055Message.GroupHeader groupHeader = new Camt055Message.GroupHeader();
        groupHeader.setMessageId("CAMT055-TEST-001");
        groupHeader.setCreationDateTime(LocalDateTime.now());
        groupHeader.setInitiatingParty(new Camt055Message.Party("Test Bank", null, null));
        cpcReq.setGroupHeader(groupHeader);
        
        // Underlying Transaction
        Camt055Message.UnderlyingTransaction underlying = new Camt055Message.UnderlyingTransaction();
        Camt055Message.PaymentTransaction txInfo = new Camt055Message.PaymentTransaction();
        txInfo.setOriginalEndToEndId("E2E-TEST-001");
        txInfo.setCancellationId("CXL-TEST-001");
        txInfo.setCancellationReasonInformation(java.util.List.of(
            new Camt055Message.PaymentCancellationReason(
                new Camt055Message.Reason("CUST"),
                java.util.List.of("Customer requested cancellation")
            )
        ));
        underlying.setTransactionInformation(java.util.List.of(txInfo));
        cpcReq.setUnderlying(java.util.List.of(underlying));
        
        camt055.setCustomerPaymentCancellationRequest(cpcReq);
        return camt055;
    }

    private Pain001Message.AccountInfo createTestAccountInfo(String accountNumber) {
        Pain001Message.AccountInfo accountInfo = new Pain001Message.AccountInfo();
        Pain001Message.AccountIdentification accountId = new Pain001Message.AccountIdentification();
        Pain001Message.GenericAccountIdentification genericId = new Pain001Message.GenericAccountIdentification();
        genericId.setIdentification(accountNumber);
        accountId.setOther(genericId);
        accountInfo.setIdentification(accountId);
        return accountInfo;
    }

    private Transaction createTestTransaction() {
        Transaction transaction = new Transaction();
        transaction.setTransactionReference("TXN-TEST-001");
        transaction.setFromAccountId(fromAccount.getId());
        transaction.setToAccountId(toAccount.getId());
        transaction.setPaymentTypeId(paymentType.getId());
        transaction.setAmount(new BigDecimal("1000.00"));
        transaction.setCurrencyCode("USD");
        transaction.setStatus(Transaction.TransactionStatus.PENDING);
        transaction.setTransactionType(Transaction.TransactionType.TRANSFER);
        transaction.setCreatedAt(LocalDateTime.now());
        
        // Add ISO 20022 metadata
        Map<String, Object> metadata = new java.util.HashMap<>();
        metadata.put("iso20022", Map.of(
            "messageId", "MSG-TEST-001",
            "endToEndId", "E2E-TEST-001",
            "instructionId", "INSTR-TEST-001"
        ));
        transaction.setMetadata(metadata);
        
        return transaction;
    }

    private Account createTestAccount(String accountNumber, BigDecimal balance) {
        Account account = new Account();
        account.setAccountNumber(accountNumber);
        account.setCustomerId(UUID.randomUUID());
        account.setAccountTypeId(UUID.randomUUID());
        account.setCurrencyCode("USD");
        account.setBalance(balance);
        account.setAvailableBalance(balance);
        account.setStatus(Account.AccountStatus.ACTIVE);
        return accountRepository.save(account);
    }

    private PaymentType createTestPaymentType() {
        PaymentType paymentType = new PaymentType();
        paymentType.setName("Test Payment Type");
        paymentType.setCode("TEST");
        paymentType.setDescription("Test payment type for integration tests");
        paymentType.setIsActive(true);
        paymentType.setIsSynchronous(true);
        paymentType.setMinAmount(new BigDecimal("0.01"));
        paymentType.setMaxAmount(new BigDecimal("100000.00"));
        paymentType.setFeeType("FIXED");
        paymentType.setFeeAmount(new BigDecimal("0.00"));
        return paymentTypeRepository.save(paymentType);
    }
}