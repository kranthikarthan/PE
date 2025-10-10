package com.paymentengine.corebanking.integration;

import com.paymentengine.corebanking.dto.CreateTransactionRequest;
import com.paymentengine.corebanking.dto.TransactionResponse;
import com.paymentengine.corebanking.entity.Account;
import com.paymentengine.corebanking.entity.PaymentType;
import com.paymentengine.corebanking.entity.Transaction;
import com.paymentengine.corebanking.exception.AccountException;
import com.paymentengine.corebanking.exception.ValidationException;
import com.paymentengine.corebanking.repository.AccountRepository;
import com.paymentengine.corebanking.repository.PaymentTypeRepository;
import com.paymentengine.corebanking.repository.TransactionRepository;
import com.paymentengine.corebanking.service.TransactionService;
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
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for Transaction Service
 */
@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
@Transactional
class TransactionIntegrationTest {

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
    private TransactionService transactionService;

    @Autowired
    private TransactionRepository transactionRepository;

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
    @DisplayName("Should create and process transaction successfully")
    void shouldCreateAndProcessTransactionSuccessfully() {
        // Given
        CreateTransactionRequest request = new CreateTransactionRequest();
        request.setFromAccountId(fromAccount.getId());
        request.setToAccountId(toAccount.getId());
        request.setPaymentTypeId(paymentType.getId());
        request.setAmount(new BigDecimal("1000.00"));
        request.setCurrencyCode("USD");
        request.setDescription("Test transfer");

        // When
        TransactionResponse response = transactionService.createTransaction(request);

        // Then
        assertNotNull(response);
        assertNotNull(response.getId());
        assertNotNull(response.getTransactionReference());
        assertEquals("1000.00", response.getAmount().toString());
        assertEquals("USD", response.getCurrencyCode());
        assertEquals(Transaction.TransactionStatus.COMPLETED, response.getStatus());

        // Verify account balances updated
        Account updatedFromAccount = accountRepository.findById(fromAccount.getId()).orElseThrow();
        Account updatedToAccount = accountRepository.findById(toAccount.getId()).orElseThrow();
        
        assertEquals(new BigDecimal("9000.00"), updatedFromAccount.getBalance());
        assertEquals(new BigDecimal("6000.00"), updatedToAccount.getBalance());
    }

    @Test
    @DisplayName("Should reject transaction with insufficient funds")
    void shouldRejectTransactionWithInsufficientFunds() {
        // Given
        CreateTransactionRequest request = new CreateTransactionRequest();
        request.setFromAccountId(fromAccount.getId());
        request.setToAccountId(toAccount.getId());
        request.setPaymentTypeId(paymentType.getId());
        request.setAmount(new BigDecimal("15000.00")); // More than available balance
        request.setCurrencyCode("USD");

        // When & Then
        AccountException exception = assertThrows(AccountException.class, () -> {
            transactionService.createTransaction(request);
        });

        assertEquals("INSUFFICIENT_FUNDS", exception.getErrorCode());
        assertTrue(exception.getMessage().contains("Insufficient funds"));
    }

    @Test
    @DisplayName("Should reject transaction with invalid amount")
    void shouldRejectTransactionWithInvalidAmount() {
        // Given
        CreateTransactionRequest request = new CreateTransactionRequest();
        request.setFromAccountId(fromAccount.getId());
        request.setToAccountId(toAccount.getId());
        request.setPaymentTypeId(paymentType.getId());
        request.setAmount(new BigDecimal("-100.00")); // Negative amount
        request.setCurrencyCode("USD");

        // When & Then
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            transactionService.createTransaction(request);
        });

        assertEquals("VALIDATION_ERROR", exception.getErrorCode());
        assertTrue(exception.getValidationErrors().contains("Amount must be positive"));
    }

    @Test
    @DisplayName("Should reject transaction with missing payment type")
    void shouldRejectTransactionWithMissingPaymentType() {
        // Given
        CreateTransactionRequest request = new CreateTransactionRequest();
        request.setFromAccountId(fromAccount.getId());
        request.setToAccountId(toAccount.getId());
        request.setPaymentTypeId(null); // Missing payment type
        request.setAmount(new BigDecimal("1000.00"));
        request.setCurrencyCode("USD");

        // When & Then
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            transactionService.createTransaction(request);
        });

        assertEquals("VALIDATION_ERROR", exception.getErrorCode());
        assertTrue(exception.getValidationErrors().contains("Payment type ID is required"));
    }

    @Test
    @DisplayName("Should handle concurrent transactions without deadlock")
    void shouldHandleConcurrentTransactionsWithoutDeadlock() throws InterruptedException {
        // Given
        CreateTransactionRequest request1 = new CreateTransactionRequest();
        request1.setFromAccountId(fromAccount.getId());
        request1.setToAccountId(toAccount.getId());
        request1.setPaymentTypeId(paymentType.getId());
        request1.setAmount(new BigDecimal("1000.00"));
        request1.setCurrencyCode("USD");

        CreateTransactionRequest request2 = new CreateTransactionRequest();
        request2.setFromAccountId(toAccount.getId());
        request2.setToAccountId(fromAccount.getId());
        request2.setPaymentTypeId(paymentType.getId());
        request2.setAmount(new BigDecimal("500.00"));
        request2.setCurrencyCode("USD");

        // When - Execute transactions concurrently
        Thread thread1 = new Thread(() -> {
            try {
                transactionService.createTransaction(request1);
            } catch (Exception e) {
                fail("Transaction 1 failed: " + e.getMessage());
            }
        });

        Thread thread2 = new Thread(() -> {
            try {
                transactionService.createTransaction(request2);
            } catch (Exception e) {
                fail("Transaction 2 failed: " + e.getMessage());
            }
        });

        thread1.start();
        thread2.start();
        thread1.join();
        thread2.join();

        // Then - Verify no deadlock occurred and balances are correct
        Account updatedFromAccount = accountRepository.findById(fromAccount.getId()).orElseThrow();
        Account updatedToAccount = accountRepository.findById(toAccount.getId()).orElseThrow();
        
        // Net effect: fromAccount -1000 +500 = -500, toAccount +1000 -500 = +500
        assertEquals(new BigDecimal("9500.00"), updatedFromAccount.getBalance());
        assertEquals(new BigDecimal("5500.00"), updatedToAccount.getBalance());
    }

    @Test
    @DisplayName("Should generate unique transaction references")
    void shouldGenerateUniqueTransactionReferences() {
        // Given
        CreateTransactionRequest request1 = new CreateTransactionRequest();
        request1.setFromAccountId(fromAccount.getId());
        request1.setToAccountId(toAccount.getId());
        request1.setPaymentTypeId(paymentType.getId());
        request1.setAmount(new BigDecimal("100.00"));
        request1.setCurrencyCode("USD");

        CreateTransactionRequest request2 = new CreateTransactionRequest();
        request2.setFromAccountId(fromAccount.getId());
        request2.setToAccountId(toAccount.getId());
        request2.setPaymentTypeId(paymentType.getId());
        request2.setAmount(new BigDecimal("200.00"));
        request2.setCurrencyCode("USD");

        // When
        TransactionResponse response1 = transactionService.createTransaction(request1);
        TransactionResponse response2 = transactionService.createTransaction(request2);

        // Then
        assertNotEquals(response1.getTransactionReference(), response2.getTransactionReference());
        assertTrue(response1.getTransactionReference().startsWith("TXN-"));
        assertTrue(response2.getTransactionReference().startsWith("TXN-"));
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