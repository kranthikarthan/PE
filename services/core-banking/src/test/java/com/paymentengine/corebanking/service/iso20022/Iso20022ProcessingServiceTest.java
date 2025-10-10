package com.paymentengine.corebanking.service.iso20022;

import com.paymentengine.corebanking.dto.CreateTransactionRequest;
import com.paymentengine.corebanking.dto.TransactionResponse;
import com.paymentengine.corebanking.entity.Account;
import com.paymentengine.corebanking.entity.Transaction;
import com.paymentengine.corebanking.repository.AccountRepository;
import com.paymentengine.corebanking.repository.TransactionRepository;
import com.paymentengine.corebanking.service.Iso20022ProcessingService;
import com.paymentengine.corebanking.service.TransactionService;
import com.paymentengine.shared.dto.iso20022.Pain001Message;
import com.paymentengine.shared.dto.iso20022.Camt055Message;
import com.paymentengine.shared.service.Iso20022MessageService;
import com.paymentengine.shared.util.EventPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ISO 20022 Processing Service
 */
@ExtendWith(MockitoExtension.class)
class Iso20022ProcessingServiceTest {

    @Mock
    private TransactionService transactionService;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private Iso20022MessageService iso20022MessageService;

    @Mock
    private EventPublisher eventPublisher;

    @InjectMocks
    private Iso20022ProcessingService iso20022ProcessingService;

    private Pain001Message samplePain001;
    private Camt055Message sampleCamt055;
    private TransactionResponse sampleTransactionResponse;
    private Transaction sampleTransaction;
    private Account sampleAccount;

    @BeforeEach
    void setUp() {
        setupTestData();
    }

    @Test
    @DisplayName("Should process pain.001 message successfully")
    void shouldProcessPain001Successfully() {
        // Given
        Map<String, Object> context = Map.of("ipAddress", "127.0.0.1", "userId", "test-user");
        
        Iso20022MessageService.ValidationResult validationResult = new Iso20022MessageService.ValidationResult();
        when(iso20022MessageService.validatePain001Message(samplePain001)).thenReturn(validationResult);
        
        CreateTransactionRequest transactionRequest = new CreateTransactionRequest();
        transactionRequest.setAmount(new BigDecimal("1000.00"));
        when(iso20022MessageService.transformPain001ToTransactionRequest(samplePain001)).thenReturn(transactionRequest);
        
        when(transactionService.createTransaction(any(CreateTransactionRequest.class))).thenReturn(sampleTransactionResponse);
        
        Map<String, Object> pain002Response = Map.of("CstmrPmtStsRpt", Map.of("GrpSts", "ACSC"));
        when(iso20022MessageService.transformTransactionResponseToPain002(any(TransactionResponse.class), anyString()))
            .thenReturn(pain002Response);

        // When
        Map<String, Object> result = iso20022ProcessingService.processPain001(samplePain001, context);

        // Then
        assertNotNull(result);
        assertTrue(result.containsKey("CstmrPmtStsRpt"));
        verify(transactionService).createTransaction(any(CreateTransactionRequest.class));
        verify(eventPublisher).publishEvent(anyString(), anyString(), any());
    }

    @Test
    @DisplayName("Should reject invalid pain.001 message")
    void shouldRejectInvalidPain001() {
        // Given
        Map<String, Object> context = Map.of("ipAddress", "127.0.0.1");
        
        Iso20022MessageService.ValidationResult validationResult = new Iso20022MessageService.ValidationResult();
        validationResult.addError("Missing required field: GroupHeader.MessageId");
        when(iso20022MessageService.validatePain001Message(samplePain001)).thenReturn(validationResult);

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            iso20022ProcessingService.processPain001(samplePain001, context);
        });
        
        assertTrue(exception.getMessage().contains("Invalid pain.001 message"));
        verify(transactionService, never()).createTransaction(any());
    }

    @Test
    @DisplayName("Should process camt.055 cancellation successfully")
    void shouldProcessCamt055Successfully() {
        // Given
        Map<String, Object> context = Map.of("ipAddress", "127.0.0.1", "userId", "test-user");
        
        // Mock finding the original transaction
        when(transactionRepository.findAll()).thenReturn(List.of(sampleTransaction));
        
        // Mock successful cancellation
        TransactionResponse cancelledTransaction = new TransactionResponse();
        cancelledTransaction.setId(sampleTransaction.getId());
        cancelledTransaction.setTransactionReference(sampleTransaction.getTransactionReference());
        cancelledTransaction.setStatus(Transaction.TransactionStatus.CANCELLED);
        
        when(transactionService.cancelTransaction(any(UUID.class), anyString())).thenReturn(cancelledTransaction);

        // When
        Map<String, Object> result = iso20022ProcessingService.processCamt055(sampleCamt055, context);

        // Then
        assertNotNull(result);
        assertTrue(result.containsKey("cancellationResults"));
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> cancellationResults = (List<Map<String, Object>>) result.get("cancellationResults");
        assertFalse(cancellationResults.isEmpty());
        assertEquals("ACCEPTED", cancellationResults.get(0).get("status"));
        
        verify(transactionService).cancelTransaction(any(UUID.class), anyString());
    }

    @Test
    @DisplayName("Should reject cancellation of completed transaction")
    void shouldRejectCancellationOfCompletedTransaction() {
        // Given
        Map<String, Object> context = Map.of("ipAddress", "127.0.0.1");
        
        // Set transaction as completed
        sampleTransaction.setStatus(Transaction.TransactionStatus.COMPLETED);
        when(transactionRepository.findAll()).thenReturn(List.of(sampleTransaction));

        // When
        Map<String, Object> result = iso20022ProcessingService.processCamt055(sampleCamt055, context);

        // Then
        assertNotNull(result);
        assertTrue(result.containsKey("cancellationResults"));
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> cancellationResults = (List<Map<String, Object>>) result.get("cancellationResults");
        assertFalse(cancellationResults.isEmpty());
        assertEquals("REJECTED", cancellationResults.get(0).get("status"));
        
        verify(transactionService, never()).cancelTransaction(any(), any());
    }

    @Test
    @DisplayName("Should handle transaction not found for cancellation")
    void shouldHandleTransactionNotFoundForCancellation() {
        // Given
        Map<String, Object> context = Map.of("ipAddress", "127.0.0.1");
        
        // Mock empty transaction list (transaction not found)
        when(transactionRepository.findAll()).thenReturn(List.of());

        // When
        Map<String, Object> result = iso20022ProcessingService.processCamt055(sampleCamt055, context);

        // Then
        assertNotNull(result);
        assertTrue(result.containsKey("cancellationResults"));
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> cancellationResults = (List<Map<String, Object>>) result.get("cancellationResults");
        assertFalse(cancellationResults.isEmpty());
        assertEquals("REJECTED", cancellationResults.get(0).get("status"));
        assertTrue(((String) cancellationResults.get(0).get("reason")).contains("not found"));
    }

    @Test
    @DisplayName("Should validate message timing constraints")
    void shouldValidateMessageTimingConstraints() {
        // Given
        Map<String, Object> context = Map.of("ipAddress", "127.0.0.1");
        
        // Set transaction as old and processing
        sampleTransaction.setStatus(Transaction.TransactionStatus.PROCESSING);
        sampleTransaction.setCreatedAt(LocalDateTime.now().minusHours(2)); // 2 hours ago
        when(transactionRepository.findAll()).thenReturn(List.of(sampleTransaction));

        // When
        Map<String, Object> result = iso20022ProcessingService.processCamt055(sampleCamt055, context);

        // Then
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> cancellationResults = (List<Map<String, Object>>) result.get("cancellationResults");
        assertEquals("REJECTED", cancellationResults.get(0).get("status"));
        assertTrue(((String) cancellationResults.get(0).get("reason")).contains("too far in processing"));
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
        assertEquals("pain001", stats.get("messageType"));
    }

    @Test
    @DisplayName("Should validate different message types")
    void shouldValidateDifferentMessageTypes() {
        // Given
        Map<String, Object> testMessage = Map.of("test", "message");

        // When
        Map<String, Object> pain001Validation = iso20022ProcessingService.validateMessage("pain001", testMessage);
        Map<String, Object> camt055Validation = iso20022ProcessingService.validateMessage("camt055", testMessage);

        // Then
        assertNotNull(pain001Validation);
        assertNotNull(camt055Validation);
        assertTrue(pain001Validation.containsKey("valid"));
        assertTrue(camt055Validation.containsKey("valid"));
    }

    @Test
    @DisplayName("Should handle bulk processing")
    void shouldHandleBulkProcessing() {
        // Given
        List<Map<String, Object>> bulkMessages = List.of(
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

    private void setupTestData() {
        // Setup sample account
        sampleAccount = new Account();
        sampleAccount.setId(UUID.fromString("880e8400-e29b-41d4-a716-446655440001"));
        sampleAccount.setAccountNumber("ACC001001");
        sampleAccount.setBalance(new BigDecimal("15000.00"));
        sampleAccount.setAvailableBalance(new BigDecimal("15000.00"));
        sampleAccount.setCurrencyCode("USD");
        sampleAccount.setStatus(Account.AccountStatus.ACTIVE);

        // Setup sample transaction
        sampleTransaction = new Transaction();
        sampleTransaction.setId(UUID.fromString("bb0e8400-e29b-41d4-a716-446655440001"));
        sampleTransaction.setTransactionReference("TXN-TEST-001");
        sampleTransaction.setFromAccountId(UUID.fromString("880e8400-e29b-41d4-a716-446655440001"));
        sampleTransaction.setToAccountId(UUID.fromString("880e8400-e29b-41d4-a716-446655440002"));
        sampleTransaction.setAmount(new BigDecimal("1000.00"));
        sampleTransaction.setCurrencyCode("USD");
        sampleTransaction.setStatus(Transaction.TransactionStatus.PENDING);
        sampleTransaction.setTransactionType(Transaction.TransactionType.TRANSFER);
        sampleTransaction.setCreatedAt(LocalDateTime.now());
        
        // Add ISO 20022 metadata
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("iso20022", Map.of(
            "messageId", "MSG-TEST-001",
            "endToEndId", "E2E-TEST-001",
            "instructionId", "INSTR-TEST-001"
        ));
        sampleTransaction.setMetadata(metadata);

        // Setup sample transaction response
        sampleTransactionResponse = new TransactionResponse();
        sampleTransactionResponse.setId(sampleTransaction.getId());
        sampleTransactionResponse.setTransactionReference(sampleTransaction.getTransactionReference());
        sampleTransactionResponse.setAmount(sampleTransaction.getAmount());
        sampleTransactionResponse.setCurrencyCode(sampleTransaction.getCurrencyCode());
        sampleTransactionResponse.setStatus(sampleTransaction.getStatus());
        sampleTransactionResponse.setTransactionType(sampleTransaction.getTransactionType());
        sampleTransactionResponse.setCreatedAt(sampleTransaction.getCreatedAt());
        sampleTransactionResponse.setUpdatedAt(sampleTransaction.getUpdatedAt());
    }
}