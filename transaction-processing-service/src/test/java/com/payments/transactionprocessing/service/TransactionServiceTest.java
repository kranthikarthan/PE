package com.payments.transactionprocessing.service;

import com.payments.domain.transaction.*;
import com.payments.domain.shared.*;
import com.payments.transactionprocessing.repository.TransactionRepository;
import com.payments.transactionprocessing.repository.LedgerEntryRepository;
import com.payments.transactionprocessing.repository.TransactionEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;
    
    @Mock
    private LedgerService ledgerService;
    
    @Mock
    private TransactionEventService eventService;
    
    @Mock
    private BalanceValidationService balanceValidationService;
    
    @Mock
    private LedgerEntryRepository ledgerEntryRepository;
    
    @Mock
    private TransactionEventRepository transactionEventRepository;

    private TransactionService transactionService;
    private TenantContext tenantContext;
    private TransactionId transactionId;
    private PaymentId paymentId;
    private AccountNumber debitAccount;
    private AccountNumber creditAccount;
    private Money amount;

    @BeforeEach
    void setUp() {
        transactionService = new TransactionService(
            transactionRepository, 
            ledgerService, 
            eventService, 
            balanceValidationService
        );
        
        tenantContext = TenantContext.of("tenant1", "Test Tenant", "bu1", "Test Business Unit");
        transactionId = TransactionId.of("txn-123");
        paymentId = PaymentId.of("pay-456");
        debitAccount = AccountNumber.of("1234567890");
        creditAccount = AccountNumber.of("0987654321");
        amount = Money.of(new BigDecimal("100.00"), Currency.getInstance("GBP"));
    }

    @Test
    void createTransaction_ShouldCreateTransactionSuccessfully() {
        // Given
        when(balanceValidationService.validateDoubleEntryInvariants(any(Transaction.class)))
            .thenReturn(true);
        when(balanceValidationService.validateSufficientBalance(any(AccountNumber.class), 
            any(TenantContext.class), any(Money.class)))
            .thenReturn(true);
        when(balanceValidationService.validateAccountBalances(any(Transaction.class)))
            .thenReturn(true);
        
        TransactionEntity mockEntity = new TransactionEntity();
        mockEntity.setId(transactionId);
        mockEntity.setStatus(TransactionStatus.CREATED);
        when(transactionRepository.save(any(TransactionEntity.class)))
            .thenReturn(mockEntity);

        // When
        Transaction result = transactionService.createTransaction(
            transactionId, tenantContext, paymentId, debitAccount, creditAccount, amount, TransactionType.PAYMENT
        );

        // Then
        assertNotNull(result);
        assertEquals(transactionId, result.getId());
        assertEquals(TransactionStatus.CREATED, result.getStatus());
        
        verify(balanceValidationService).validateDoubleEntryInvariants(any(Transaction.class));
        verify(balanceValidationService).validateSufficientBalance(debitAccount, tenantContext, amount);
        verify(ledgerService).createLedgerEntries(any(Transaction.class));
        verify(balanceValidationService).validateAccountBalances(any(Transaction.class));
        verify(eventService).saveTransactionEvents(any(Transaction.class));
    }

    @Test
    void createTransaction_ShouldThrowException_WhenDoubleEntryValidationFails() {
        // Given
        when(balanceValidationService.validateDoubleEntryInvariants(any(Transaction.class)))
            .thenReturn(false);

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            transactionService.createTransaction(
                transactionId, tenantContext, paymentId, debitAccount, creditAccount, amount, TransactionType.PAYMENT
            );
        });
        
        assertEquals("Transaction violates double-entry bookkeeping rules", exception.getMessage());
        verify(balanceValidationService).validateDoubleEntryInvariants(any(Transaction.class));
        verify(transactionRepository, never()).save(any(TransactionEntity.class));
    }

    @Test
    void createTransaction_ShouldThrowException_WhenInsufficientBalance() {
        // Given
        when(balanceValidationService.validateDoubleEntryInvariants(any(Transaction.class)))
            .thenReturn(true);
        when(balanceValidationService.validateSufficientBalance(any(AccountNumber.class), 
            any(TenantContext.class), any(Money.class)))
            .thenReturn(false);

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            transactionService.createTransaction(
                transactionId, tenantContext, paymentId, debitAccount, creditAccount, amount, TransactionType.PAYMENT
            );
        });
        
        assertEquals("Insufficient balance in debit account 1234567890", exception.getMessage());
        verify(balanceValidationService).validateSufficientBalance(debitAccount, tenantContext, amount);
        verify(transactionRepository, never()).save(any(TransactionEntity.class));
    }

    @Test
    void startProcessing_ShouldUpdateTransactionStatus() {
        // Given
        TransactionEntity mockEntity = new TransactionEntity();
        mockEntity.setId(transactionId);
        mockEntity.setStatus(TransactionStatus.CREATED);
        when(transactionRepository.findByTenantContextAndId(tenantContext, transactionId))
            .thenReturn(mockEntity);
        when(transactionRepository.save(any(TransactionEntity.class)))
            .thenReturn(mockEntity);

        // When
        Transaction result = transactionService.startProcessing(transactionId, tenantContext);

        // Then
        assertNotNull(result);
        assertEquals(TransactionStatus.PROCESSING, result.getStatus());
        
        verify(transactionRepository).findByTenantContextAndId(tenantContext, transactionId);
        verify(transactionRepository).save(any(TransactionEntity.class));
        verify(eventService).saveTransactionEvents(any(Transaction.class));
    }

    @Test
    void markAsCleared_ShouldUpdateTransactionStatus() {
        // Given
        String clearingSystem = "CHAPS";
        String clearingReference = "CHAPS-REF-123";
        
        TransactionEntity mockEntity = new TransactionEntity();
        mockEntity.setId(transactionId);
        mockEntity.setStatus(TransactionStatus.PROCESSING);
        when(transactionRepository.findByTenantContextAndId(tenantContext, transactionId))
            .thenReturn(mockEntity);
        when(transactionRepository.save(any(TransactionEntity.class)))
            .thenReturn(mockEntity);

        // When
        Transaction result = transactionService.markAsCleared(transactionId, tenantContext, clearingSystem, clearingReference);

        // Then
        assertNotNull(result);
        assertEquals(TransactionStatus.CLEARED, result.getStatus());
        
        verify(transactionRepository).findByTenantContextAndId(tenantContext, transactionId);
        verify(transactionRepository).save(any(TransactionEntity.class));
        verify(eventService).saveTransactionEvents(any(Transaction.class));
    }

    @Test
    void completeTransaction_ShouldUpdateTransactionStatus() {
        // Given
        TransactionEntity mockEntity = new TransactionEntity();
        mockEntity.setId(transactionId);
        mockEntity.setStatus(TransactionStatus.CLEARED);
        when(transactionRepository.findByTenantContextAndId(tenantContext, transactionId))
            .thenReturn(mockEntity);
        when(transactionRepository.save(any(TransactionEntity.class)))
            .thenReturn(mockEntity);

        // When
        Transaction result = transactionService.completeTransaction(transactionId, tenantContext);

        // Then
        assertNotNull(result);
        assertEquals(TransactionStatus.COMPLETED, result.getStatus());
        
        verify(transactionRepository).findByTenantContextAndId(tenantContext, transactionId);
        verify(transactionRepository).save(any(TransactionEntity.class));
        verify(eventService).saveTransactionEvents(any(Transaction.class));
    }

    @Test
    void failTransaction_ShouldUpdateTransactionStatus() {
        // Given
        String failureReason = "Insufficient funds";
        
        TransactionEntity mockEntity = new TransactionEntity();
        mockEntity.setId(transactionId);
        mockEntity.setStatus(TransactionStatus.PROCESSING);
        when(transactionRepository.findByTenantContextAndId(tenantContext, transactionId))
            .thenReturn(mockEntity);
        when(transactionRepository.save(any(TransactionEntity.class)))
            .thenReturn(mockEntity);

        // When
        Transaction result = transactionService.failTransaction(transactionId, tenantContext, failureReason);

        // Then
        assertNotNull(result);
        assertEquals(TransactionStatus.FAILED, result.getStatus());
        
        verify(transactionRepository).findByTenantContextAndId(tenantContext, transactionId);
        verify(transactionRepository).save(any(TransactionEntity.class));
        verify(eventService).saveTransactionEvents(any(Transaction.class));
    }

    @Test
    void getTransaction_ShouldReturnTransaction() {
        // Given
        TransactionEntity mockEntity = new TransactionEntity();
        mockEntity.setId(transactionId);
        mockEntity.setStatus(TransactionStatus.CREATED);
        when(transactionRepository.findByTenantContextAndId(tenantContext, transactionId))
            .thenReturn(mockEntity);

        // When
        Transaction result = transactionService.getTransaction(transactionId, tenantContext);

        // Then
        assertNotNull(result);
        assertEquals(transactionId, result.getId());
        assertEquals(TransactionStatus.CREATED, result.getStatus());
        
        verify(transactionRepository).findByTenantContextAndId(tenantContext, transactionId);
    }

    @Test
    void getTransaction_ShouldThrowException_WhenTransactionNotFound() {
        // Given
        when(transactionRepository.findByTenantContextAndId(tenantContext, transactionId))
            .thenReturn(null);

        // When & Then
        assertThrows(com.payments.transactionprocessing.exception.TransactionNotFoundException.class, () -> {
            transactionService.getTransaction(transactionId, tenantContext);
        });
        
        verify(transactionRepository).findByTenantContextAndId(tenantContext, transactionId);
    }

    @Test
    void getTenantTransactions_ShouldReturnTransactionList() {
        // Given
        TransactionEntity mockEntity1 = new TransactionEntity();
        mockEntity1.setId(TransactionId.of("txn-1"));
        mockEntity1.setStatus(TransactionStatus.CREATED);
        
        TransactionEntity mockEntity2 = new TransactionEntity();
        mockEntity2.setId(TransactionId.of("txn-2"));
        mockEntity2.setStatus(TransactionStatus.PROCESSING);
        
        when(transactionRepository.findByTenantContext(tenantContext))
            .thenReturn(List.of(mockEntity1, mockEntity2));

        // When
        List<Transaction> result = transactionService.getTenantTransactions(tenantContext);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        
        verify(transactionRepository).findByTenantContext(tenantContext);
    }

    @Test
    void getTenantTransactionsByStatus_ShouldReturnFilteredTransactions() {
        // Given
        TransactionEntity mockEntity = new TransactionEntity();
        mockEntity.setId(transactionId);
        mockEntity.setStatus(TransactionStatus.CREATED);
        
        when(transactionRepository.findByTenantContextAndStatus(tenantContext, TransactionStatus.CREATED))
            .thenReturn(List.of(mockEntity));

        // When
        List<Transaction> result = transactionService.getTenantTransactionsByStatus(tenantContext, TransactionStatus.CREATED);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(TransactionStatus.CREATED, result.get(0).getStatus());
        
        verify(transactionRepository).findByTenantContextAndStatus(tenantContext, TransactionStatus.CREATED);
    }
}






