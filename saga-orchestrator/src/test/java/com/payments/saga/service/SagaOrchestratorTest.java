package com.payments.saga.service;

import com.payments.domain.shared.TenantContext;
import com.payments.saga.domain.*;
import com.payments.saga.service.SagaOrchestrator;
import com.payments.saga.service.SagaService;
import com.payments.saga.service.SagaStepService;
import com.payments.saga.service.SagaEventService;
import com.payments.saga.service.SagaTemplateService;
import com.payments.saga.service.SagaExecutionEngine;
import com.payments.saga.service.SagaCompensationEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for SagaOrchestrator
 */
@ExtendWith(MockitoExtension.class)
class SagaOrchestratorTest {

    @Mock
    private SagaService sagaService;
    
    @Mock
    private SagaStepService sagaStepService;
    
    @Mock
    private SagaEventService sagaEventService;
    
    @Mock
    private SagaTemplateService sagaTemplateService;
    
    @Mock
    private SagaExecutionEngine sagaExecutionEngine;
    
    @Mock
    private SagaCompensationEngine sagaCompensationEngine;

    private SagaOrchestrator sagaOrchestrator;
    private TenantContext tenantContext;
    private SagaTemplate testTemplate;

    @BeforeEach
    void setUp() {
        sagaOrchestrator = new SagaOrchestrator(
            sagaService, sagaStepService, sagaEventService, 
            sagaTemplateService, sagaExecutionEngine, sagaCompensationEngine
        );
        
        tenantContext = TenantContext.of("tenant-1", "Test Tenant", "bu-1", "Test Business Unit");
        
        // Create test template
        testTemplate = SagaTemplate.createPaymentProcessingTemplate();
    }

    @Test
    void testStartSaga_Success() {
        // Given
        String templateName = "PaymentProcessingSaga";
        String correlationId = "corr-123";
        String paymentId = "pay-456";
        Map<String, Object> sagaData = Map.of("amount", 1000.0, "currency", "USD");
        
        when(sagaTemplateService.getTemplate(templateName)).thenReturn(testTemplate);
        when(sagaService.saveSaga(any(Saga.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Saga result = sagaOrchestrator.startSaga(templateName, tenantContext, correlationId, paymentId, sagaData);

        // Then
        assertNotNull(result);
        assertEquals(templateName, result.getSagaName());
        assertEquals(tenantContext, result.getTenantContext());
        assertEquals(correlationId, result.getCorrelationId());
        assertEquals(paymentId, result.getPaymentId());
        assertEquals(SagaStatus.PENDING, result.getStatus());
        assertTrue(result.getTotalSteps() > 0);
        
        verify(sagaTemplateService).getTemplate(templateName);
        verify(sagaService).saveSaga(any(Saga.class));
        verify(sagaEventService).publishEvent(any(SagaStartedEvent.class));
        verify(sagaExecutionEngine).executeSaga(result.getId());
    }

    @Test
    void testStartSaga_TemplateNotFound() {
        // Given
        String templateName = "NonExistentTemplate";
        when(sagaTemplateService.getTemplate(templateName)).thenReturn(null);

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> 
            sagaOrchestrator.startSaga(templateName, tenantContext, "corr-123", "pay-456", Map.of())
        );
    }

    @Test
    void testExecuteNextStep_Success() {
        // Given
        SagaId sagaId = SagaId.generate();
        Saga saga = createTestSaga(sagaId);
        SagaStep currentStep = createTestStep(sagaId, 0);
        saga.addStep(currentStep);
        
        when(sagaService.getSaga(sagaId)).thenReturn(Optional.of(saga));
        when(sagaStepService.saveStep(any(SagaStep.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        sagaOrchestrator.executeNextStep(sagaId);

        // Then
        verify(sagaExecutionEngine).executeStep(currentStep);
    }

    @Test
    void testExecuteNextStep_SagaNotFound() {
        // Given
        SagaId sagaId = SagaId.generate();
        when(sagaService.getSaga(sagaId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> 
            sagaOrchestrator.executeNextStep(sagaId)
        );
    }

    @Test
    void testHandleStepCompletion_Success() {
        // Given
        SagaId sagaId = SagaId.generate();
        SagaStepId stepId = SagaStepId.generate();
        SagaStep step = createTestStep(sagaId, 0);
        Map<String, Object> outputData = Map.of("result", "success");
        
        when(sagaStepService.getStep(stepId)).thenReturn(Optional.of(step));
        when(sagaService.getSaga(sagaId)).thenReturn(Optional.of(createTestSaga(sagaId)));
        when(sagaStepService.saveStep(any(SagaStep.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(sagaService.saveSaga(any(Saga.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        sagaOrchestrator.handleStepCompletion(stepId, outputData);

        // Then
        verify(sagaStepService).saveStep(any(SagaStep.class));
        verify(sagaEventService).publishEvent(any(SagaStepCompletedEvent.class));
        verify(sagaService).saveSaga(any(Saga.class));
    }

    @Test
    void testHandleStepFailure_Success() {
        // Given
        SagaId sagaId = SagaId.generate();
        SagaStepId stepId = SagaStepId.generate();
        SagaStep step = createTestStep(sagaId, 0);
        String errorMessage = "Validation failed";
        Map<String, Object> errorData = Map.of("error", "validation_error");
        
        when(sagaStepService.getStep(stepId)).thenReturn(Optional.of(step));
        when(sagaStepService.saveStep(any(SagaStep.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        sagaOrchestrator.handleStepFailure(stepId, errorMessage, errorData);

        // Then
        verify(sagaStepService).saveStep(any(SagaStep.class));
        verify(sagaEventService).publishEvent(any(SagaStepFailedEvent.class));
    }

    @Test
    void testStartCompensation_Success() {
        // Given
        SagaId sagaId = SagaId.generate();
        Saga saga = createTestSaga(sagaId);
        String reason = "Step failed";
        
        when(sagaService.getSaga(sagaId)).thenReturn(Optional.of(saga));
        when(sagaService.saveSaga(any(Saga.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        sagaOrchestrator.startCompensation(sagaId, reason);

        // Then
        verify(sagaService).saveSaga(any(Saga.class));
        verify(sagaEventService).publishEvent(any(SagaCompensationStartedEvent.class));
        verify(sagaCompensationEngine).startCompensation(saga);
    }

    @Test
    void testCompleteSaga_Success() {
        // Given
        Saga saga = createTestSaga(SagaId.generate());
        saga.start(); // Set to RUNNING status
        
        when(sagaService.saveSaga(any(Saga.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        sagaOrchestrator.completeSaga(saga);

        // Then
        assertEquals(SagaStatus.COMPLETED, saga.getStatus());
        assertNotNull(saga.getCompletedAt());
        verify(sagaService).saveSaga(saga);
        verify(sagaEventService).publishEvent(any(SagaCompletedEvent.class));
    }

    @Test
    void testCompleteCompensation_Success() {
        // Given
        SagaId sagaId = SagaId.generate();
        Saga saga = createTestSaga(sagaId);
        saga.startCompensation(); // Set to COMPENSATING status
        
        when(sagaService.getSaga(sagaId)).thenReturn(Optional.of(saga));
        when(sagaService.saveSaga(any(Saga.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        sagaOrchestrator.completeCompensation(sagaId);

        // Then
        assertEquals(SagaStatus.COMPENSATED, saga.getStatus());
        assertNotNull(saga.getCompensatedAt());
        verify(sagaService).saveSaga(saga);
        verify(sagaEventService).publishEvent(any(SagaCompensatedEvent.class));
    }

    @Test
    void testGetSagaStatus_Success() {
        // Given
        SagaId sagaId = SagaId.generate();
        Saga saga = createTestSaga(sagaId);
        
        when(sagaService.getSaga(sagaId)).thenReturn(Optional.of(saga));

        // When
        Optional<Saga> result = sagaOrchestrator.getSagaStatus(sagaId);

        // Then
        assertTrue(result.isPresent());
        assertEquals(saga, result.get());
    }

    @Test
    void testGetSagaStatus_NotFound() {
        // Given
        SagaId sagaId = SagaId.generate();
        when(sagaService.getSaga(sagaId)).thenReturn(Optional.empty());

        // When
        Optional<Saga> result = sagaOrchestrator.getSagaStatus(sagaId);

        // Then
        assertTrue(result.isEmpty());
    }

    private Saga createTestSaga(SagaId sagaId) {
        return Saga.builder()
                .id(sagaId)
                .sagaName("TestSaga")
                .status(SagaStatus.PENDING)
                .tenantContext(tenantContext)
                .correlationId("corr-123")
                .paymentId("pay-456")
                .currentStepIndex(0)
                .build();
    }

    private SagaStep createTestStep(SagaId sagaId, int sequence) {
        return SagaStep.builder()
                .id(SagaStepId.generate())
                .sagaId(sagaId)
                .stepName("TestStep")
                .stepType(SagaStepType.VALIDATION)
                .status(SagaStepStatus.PENDING)
                .sequence(sequence)
                .serviceName("test-service")
                .endpoint("/test")
                .maxRetries(3)
                .tenantContext(tenantContext)
                .correlationId("corr-123")
                .build();
    }
}






