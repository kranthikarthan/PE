package com.payments.saga.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.payments.domain.shared.TenantContext;
import com.payments.saga.domain.*;
import com.payments.saga.event.SagaEventPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.util.concurrent.ListenableFuture;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for SagaEventPublisher
 */
@ExtendWith(MockitoExtension.class)
class SagaEventPublisherTest {

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;
    
    @Mock
    private ObjectMapper objectMapper;

    private SagaEventPublisher sagaEventPublisher;
    private TenantContext tenantContext;

    @BeforeEach
    void setUp() {
        sagaEventPublisher = new SagaEventPublisher(kafkaTemplate, objectMapper);
        tenantContext = TenantContext.of("tenant-1", "Test Tenant", "bu-1", "Test Business Unit");
    }

    @Test
    void testPublishSagaStarted() throws Exception {
        // Given
        SagaId sagaId = SagaId.generate();
        SagaStartedEvent event = new SagaStartedEvent(
            sagaId, tenantContext, "corr-123", "TestSaga", 
            Map.of("paymentId", "pay-456", "amount", 1000.0)
        );
        
        String expectedMessage = "{\"eventId\":\"event-123\",\"sagaId\":\"" + sagaId.getValue() + "\"}";
        CompletableFuture<SendResult<String, String>> future = CompletableFuture.completedFuture(mock(SendResult.class));
        
        when(objectMapper.writeValueAsString(any(Map.class))).thenReturn(expectedMessage);
        when(kafkaTemplate.send(eq("saga.started"), eq(sagaId.getValue()), eq(expectedMessage)))
                .thenReturn(future);

        // When
        sagaEventPublisher.publishSagaStarted(event);

        // Then
        verify(objectMapper).writeValueAsString(any(Map.class));
        verify(kafkaTemplate).send(eq("saga.started"), eq(sagaId.getValue()), eq(expectedMessage));
    }

    @Test
    void testPublishSagaStepStarted() throws Exception {
        // Given
        SagaId sagaId = SagaId.generate();
        SagaStepStartedEvent event = new SagaStepStartedEvent(
            sagaId, tenantContext, "corr-123", "ValidatePayment", 
            SagaStepType.VALIDATION, 1, "validation-service", "/api/v1/validate"
        );
        
        String expectedMessage = "{\"eventId\":\"event-123\",\"sagaId\":\"" + sagaId.getValue() + "\"}";
        CompletableFuture<SendResult<String, String>> future = CompletableFuture.completedFuture(mock(SendResult.class));
        
        when(objectMapper.writeValueAsString(any(Map.class))).thenReturn(expectedMessage);
        when(kafkaTemplate.send(eq("saga.step.started"), eq(sagaId.getValue()), eq(expectedMessage)))
                .thenReturn(future);

        // When
        sagaEventPublisher.publishSagaStepStarted(event);

        // Then
        verify(objectMapper).writeValueAsString(any(Map.class));
        verify(kafkaTemplate).send(eq("saga.step.started"), eq(sagaId.getValue()), eq(expectedMessage));
    }

    @Test
    void testPublishSagaStepCompleted() throws Exception {
        // Given
        SagaId sagaId = SagaId.generate();
        SagaStepCompletedEvent event = new SagaStepCompletedEvent(
            sagaId, tenantContext, "corr-123", "ValidatePayment", 
            SagaStepType.VALIDATION, 1, "validation-service", 
            Map.of("validationResult", "success")
        );
        
        String expectedMessage = "{\"eventId\":\"event-123\",\"sagaId\":\"" + sagaId.getValue() + "\"}";
        CompletableFuture<SendResult<String, String>> future = CompletableFuture.completedFuture(mock(SendResult.class));
        
        when(objectMapper.writeValueAsString(any(Map.class))).thenReturn(expectedMessage);
        when(kafkaTemplate.send(eq("saga.step.completed"), eq(sagaId.getValue()), eq(expectedMessage)))
                .thenReturn(future);

        // When
        sagaEventPublisher.publishSagaStepCompleted(event);

        // Then
        verify(objectMapper).writeValueAsString(any(Map.class));
        verify(kafkaTemplate).send(eq("saga.step.completed"), eq(sagaId.getValue()), eq(expectedMessage));
    }

    @Test
    void testPublishSagaStepFailed() throws Exception {
        // Given
        SagaId sagaId = SagaId.generate();
        SagaStepFailedEvent event = new SagaStepFailedEvent(
            sagaId, tenantContext, "corr-123", "ValidatePayment", 
            SagaStepType.VALIDATION, 1, "validation-service", 
            "Validation failed", Map.of("error", "invalid_data")
        );
        
        String expectedMessage = "{\"eventId\":\"event-123\",\"sagaId\":\"" + sagaId.getValue() + "\"}";
        CompletableFuture<SendResult<String, String>> future = CompletableFuture.completedFuture(mock(SendResult.class));
        
        when(objectMapper.writeValueAsString(any(Map.class))).thenReturn(expectedMessage);
        when(kafkaTemplate.send(eq("saga.step.failed"), eq(sagaId.getValue()), eq(expectedMessage)))
                .thenReturn(future);

        // When
        sagaEventPublisher.publishSagaStepFailed(event);

        // Then
        verify(objectMapper).writeValueAsString(any(Map.class));
        verify(kafkaTemplate).send(eq("saga.step.failed"), eq(sagaId.getValue()), eq(expectedMessage));
    }

    @Test
    void testPublishSagaCompleted() throws Exception {
        // Given
        SagaId sagaId = SagaId.generate();
        SagaCompletedEvent event = new SagaCompletedEvent(
            sagaId, tenantContext, "corr-123", "TestSaga", 
            Map.of("totalSteps", 5, "duration", 1200)
        );
        
        String expectedMessage = "{\"eventId\":\"event-123\",\"sagaId\":\"" + sagaId.getValue() + "\"}";
        CompletableFuture<SendResult<String, String>> future = CompletableFuture.completedFuture(mock(SendResult.class));
        
        when(objectMapper.writeValueAsString(any(Map.class))).thenReturn(expectedMessage);
        when(kafkaTemplate.send(eq("saga.completed"), eq(sagaId.getValue()), eq(expectedMessage)))
                .thenReturn(future);

        // When
        sagaEventPublisher.publishSagaCompleted(event);

        // Then
        verify(objectMapper).writeValueAsString(any(Map.class));
        verify(kafkaTemplate).send(eq("saga.completed"), eq(sagaId.getValue()), eq(expectedMessage));
    }

    @Test
    void testPublishSagaCompensationStarted() throws Exception {
        // Given
        SagaId sagaId = SagaId.generate();
        SagaCompensationStartedEvent event = new SagaCompensationStartedEvent(
            sagaId, tenantContext, "corr-123", "TestSaga", 
            "Step failed", Map.of("failedStep", "ValidatePayment")
        );
        
        String expectedMessage = "{\"eventId\":\"event-123\",\"sagaId\":\"" + sagaId.getValue() + "\"}";
        CompletableFuture<SendResult<String, String>> future = CompletableFuture.completedFuture(mock(SendResult.class));
        
        when(objectMapper.writeValueAsString(any(Map.class))).thenReturn(expectedMessage);
        when(kafkaTemplate.send(eq("saga.compensation.started"), eq(sagaId.getValue()), eq(expectedMessage)))
                .thenReturn(future);

        // When
        sagaEventPublisher.publishSagaCompensationStarted(event);

        // Then
        verify(objectMapper).writeValueAsString(any(Map.class));
        verify(kafkaTemplate).send(eq("saga.compensation.started"), eq(sagaId.getValue()), eq(expectedMessage));
    }

    @Test
    void testPublishSagaCompensated() throws Exception {
        // Given
        SagaId sagaId = SagaId.generate();
        SagaCompensatedEvent event = new SagaCompensatedEvent(
            sagaId, tenantContext, "corr-123", "TestSaga", 
            Map.of("compensatedSteps", 3, "compensationDuration", 800)
        );
        
        String expectedMessage = "{\"eventId\":\"event-123\",\"sagaId\":\"" + sagaId.getValue() + "\"}";
        CompletableFuture<SendResult<String, String>> future = CompletableFuture.completedFuture(mock(SendResult.class));
        
        when(objectMapper.writeValueAsString(any(Map.class))).thenReturn(expectedMessage);
        when(kafkaTemplate.send(eq("saga.compensated"), eq(sagaId.getValue()), eq(expectedMessage)))
                .thenReturn(future);

        // When
        sagaEventPublisher.publishSagaCompensated(event);

        // Then
        verify(objectMapper).writeValueAsString(any(Map.class));
        verify(kafkaTemplate).send(eq("saga.compensated"), eq(sagaId.getValue()), eq(expectedMessage));
    }

    @Test
    void testPublishEvent_SerializationError() throws Exception {
        // Given
        SagaId sagaId = SagaId.generate();
        SagaStartedEvent event = new SagaStartedEvent(
            sagaId, tenantContext, "corr-123", "TestSaga", 
            Map.of("paymentId", "pay-456")
        );
        
        when(objectMapper.writeValueAsString(any(Map.class)))
                .thenThrow(new RuntimeException("Serialization failed"));

        // When
        sagaEventPublisher.publishSagaStarted(event);

        // Then
        verify(objectMapper).writeValueAsString(any(Map.class));
        verify(kafkaTemplate, never()).send(anyString(), anyString(), anyString());
    }

    @Test
    void testPublishEvent_KafkaError() throws Exception {
        // Given
        SagaId sagaId = SagaId.generate();
        SagaStartedEvent event = new SagaStartedEvent(
            sagaId, tenantContext, "corr-123", "TestSaga", 
            Map.of("paymentId", "pay-456")
        );
        
        String expectedMessage = "{\"eventId\":\"event-123\",\"sagaId\":\"" + sagaId.getValue() + "\"}";
        CompletableFuture<SendResult<String, String>> future = new CompletableFuture<>();
        future.completeExceptionally(new RuntimeException("Kafka error"));
        
        when(objectMapper.writeValueAsString(any(Map.class))).thenReturn(expectedMessage);
        when(kafkaTemplate.send(eq("saga.started"), eq(sagaId.getValue()), eq(expectedMessage)))
                .thenReturn(future);

        // When
        sagaEventPublisher.publishSagaStarted(event);

        // Then
        verify(objectMapper).writeValueAsString(any(Map.class));
        verify(kafkaTemplate).send(eq("saga.started"), eq(sagaId.getValue()), eq(expectedMessage));
    }
}






