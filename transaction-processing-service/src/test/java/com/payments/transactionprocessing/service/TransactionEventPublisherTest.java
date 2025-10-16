package com.payments.transactionprocessing.service;

import com.payments.domain.transaction.*;
import com.payments.domain.shared.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionEventPublisherTest {

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;
    
    @Mock
    private TransactionEventService transactionEventService;

    private TransactionEventPublisher eventPublisher;
    private TenantContext tenantContext;
    private TransactionId transactionId;
    private PaymentId paymentId;
    private AccountNumber debitAccount;
    private AccountNumber creditAccount;
    private Money amount;

    @BeforeEach
    void setUp() {
        eventPublisher = new TransactionEventPublisher(kafkaTemplate, transactionEventService);
        
        tenantContext = TenantContext.of("tenant1", "Test Tenant", "bu1", "Test Business Unit");
        transactionId = TransactionId.of("txn-123");
        paymentId = PaymentId.of("pay-456");
        debitAccount = AccountNumber.of("1234567890");
        creditAccount = AccountNumber.of("0987654321");
        amount = Money.of(new BigDecimal("100.00"), Currency.getInstance("GBP"));
    }

    @Test
    void publishTransactionEvents_ShouldPublishAllEvents() {
        // Given
        Transaction transaction = createTransactionWithEvents();
        when(kafkaTemplate.send(any(String.class), any(String.class), any(Map.class)))
            .thenReturn(null);

        // When
        eventPublisher.publishTransactionEvents(transaction);

        // Then
        verify(kafkaTemplate, times(2)).send(any(String.class), any(String.class), any(Map.class));
    }

    @Test
    void publishTransactionCreated_ShouldPublishToCorrectTopic() {
        // Given
        TransactionCreatedEvent event = createTransactionCreatedEvent();
        when(kafkaTemplate.send(any(String.class), any(String.class), any(Map.class)))
            .thenReturn(null);

        // When
        eventPublisher.publishTransactionCreated(event);

        // Then
        ArgumentCaptor<String> topicCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Map> payloadCaptor = ArgumentCaptor.forClass(Map.class);
        
        verify(kafkaTemplate).send(topicCaptor.capture(), keyCaptor.capture(), payloadCaptor.capture());
        
        assertEquals("transaction.created", topicCaptor.getValue());
        assertEquals(transactionId.getValue(), keyCaptor.getValue());
        assertNotNull(payloadCaptor.getValue());
    }

    @Test
    void publishTransactionProcessing_ShouldPublishToCorrectTopic() {
        // Given
        TransactionProcessingEvent event = createTransactionProcessingEvent();
        when(kafkaTemplate.send(any(String.class), any(String.class), any(Map.class)))
            .thenReturn(null);

        // When
        eventPublisher.publishTransactionProcessing(event);

        // Then
        ArgumentCaptor<String> topicCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Map> payloadCaptor = ArgumentCaptor.forClass(Map.class);
        
        verify(kafkaTemplate).send(topicCaptor.capture(), keyCaptor.capture(), payloadCaptor.capture());
        
        assertEquals("transaction.processing", topicCaptor.getValue());
        assertEquals(transactionId.getValue(), keyCaptor.getValue());
        assertNotNull(payloadCaptor.getValue());
    }

    @Test
    void publishTransactionClearing_ShouldPublishToCorrectTopic() {
        // Given
        TransactionClearingEvent event = createTransactionClearingEvent();
        when(kafkaTemplate.send(any(String.class), any(String.class), any(Map.class)))
            .thenReturn(null);

        // When
        eventPublisher.publishTransactionClearing(event);

        // Then
        ArgumentCaptor<String> topicCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Map> payloadCaptor = ArgumentCaptor.forClass(Map.class);
        
        verify(kafkaTemplate).send(topicCaptor.capture(), keyCaptor.capture(), payloadCaptor.capture());
        
        assertEquals("transaction.clearing", topicCaptor.getValue());
        assertEquals(transactionId.getValue(), keyCaptor.getValue());
        assertNotNull(payloadCaptor.getValue());
    }

    @Test
    void publishTransactionCompleted_ShouldPublishToCorrectTopic() {
        // Given
        TransactionCompletedEvent event = createTransactionCompletedEvent();
        when(kafkaTemplate.send(any(String.class), any(String.class), any(Map.class)))
            .thenReturn(null);

        // When
        eventPublisher.publishTransactionCompleted(event);

        // Then
        ArgumentCaptor<String> topicCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Map> payloadCaptor = ArgumentCaptor.forClass(Map.class);
        
        verify(kafkaTemplate).send(topicCaptor.capture(), keyCaptor.capture(), payloadCaptor.capture());
        
        assertEquals("transaction.completed", topicCaptor.getValue());
        assertEquals(transactionId.getValue(), keyCaptor.getValue());
        assertNotNull(payloadCaptor.getValue());
    }

    @Test
    void publishTransactionFailed_ShouldPublishToCorrectTopic() {
        // Given
        TransactionFailedEvent event = createTransactionFailedEvent();
        when(kafkaTemplate.send(any(String.class), any(String.class), any(Map.class)))
            .thenReturn(null);

        // When
        eventPublisher.publishTransactionFailed(event);

        // Then
        ArgumentCaptor<String> topicCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Map> payloadCaptor = ArgumentCaptor.forClass(Map.class);
        
        verify(kafkaTemplate).send(topicCaptor.capture(), keyCaptor.capture(), payloadCaptor.capture());
        
        assertEquals("transaction.failed", topicCaptor.getValue());
        assertEquals(transactionId.getValue(), keyCaptor.getValue());
        assertNotNull(payloadCaptor.getValue());
    }

    @Test
    void publishEvent_ShouldHandleKafkaException() {
        // Given
        TransactionCreatedEvent event = createTransactionCreatedEvent();
        when(kafkaTemplate.send(any(String.class), any(String.class), any(Map.class)))
            .thenThrow(new RuntimeException("Kafka error"));

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            eventPublisher.publishEvent(event);
        });
    }

    private Transaction createTransactionWithEvents() {
        Transaction transaction = Transaction.create(
            transactionId, tenantContext, paymentId, 
            debitAccount, creditAccount, amount, TransactionType.PAYMENT
        );
        
        // Add some events to the transaction
        transaction.startProcessing();
        transaction.markAsCleared("CHAPS", "CHAPS-REF-123");
        
        return transaction;
    }

    private TransactionCreatedEvent createTransactionCreatedEvent() {
        return new TransactionCreatedEvent(
            TransactionEventId.of("event-1"),
            transactionId,
            tenantContext,
            "corr-123",
            Instant.now(),
            1L,
            paymentId,
            debitAccount,
            creditAccount,
            amount,
            TransactionType.PAYMENT,
            Map.of("source", "test")
        );
    }

    private TransactionProcessingEvent createTransactionProcessingEvent() {
        return new TransactionProcessingEvent(
            TransactionEventId.of("event-2"),
            transactionId,
            tenantContext,
            "corr-123",
            Instant.now(),
            2L,
            Instant.now(),
            Map.of("source", "test")
        );
    }

    private TransactionClearingEvent createTransactionClearingEvent() {
        return new TransactionClearingEvent(
            TransactionEventId.of("event-3"),
            transactionId,
            tenantContext,
            "corr-123",
            Instant.now(),
            3L,
            "CHAPS",
            "CHAPS-REF-123",
            Instant.now(),
            Map.of("source", "test")
        );
    }

    private TransactionCompletedEvent createTransactionCompletedEvent() {
        return new TransactionCompletedEvent(
            TransactionEventId.of("event-4"),
            transactionId,
            tenantContext,
            "corr-123",
            Instant.now(),
            4L,
            Instant.now(),
            Map.of("source", "test")
        );
    }

    private TransactionFailedEvent createTransactionFailedEvent() {
        return new TransactionFailedEvent(
            TransactionEventId.of("event-5"),
            transactionId,
            tenantContext,
            "corr-123",
            Instant.now(),
            5L,
            "Insufficient funds",
            Instant.now(),
            Map.of("source", "test")
        );
    }
}






