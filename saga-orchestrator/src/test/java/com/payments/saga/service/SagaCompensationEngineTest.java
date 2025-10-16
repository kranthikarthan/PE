package com.payments.saga.service;

import com.payments.domain.shared.TenantContext;
import com.payments.saga.domain.*;
import com.payments.saga.service.SagaCompensationEngine;
import com.payments.saga.service.SagaStepService;
import com.payments.saga.service.SagaEventService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for SagaCompensationEngine
 */
@ExtendWith(MockitoExtension.class)
class SagaCompensationEngineTest {

    @Mock
    private RestTemplate restTemplate;
    
    @Mock
    private SagaStepService sagaStepService;
    
    @Mock
    private SagaEventService sagaEventService;

    private SagaCompensationEngine sagaCompensationEngine;
    private TenantContext tenantContext;

    @BeforeEach
    void setUp() {
        sagaCompensationEngine = new SagaCompensationEngine(restTemplate, sagaStepService, sagaEventService);
        tenantContext = TenantContext.of("tenant-1", "Test Tenant", "bu-1", "Test Business Unit");
    }

    @Test
    void testStartCompensation_Success() {
        // Given
        Saga saga = createTestSaga();
        List<SagaStep> completedSteps = createCompletedSteps(saga.getId());
        saga.setSteps(completedSteps);
        
        when(sagaStepService.getCompletedStepsBySagaId(any(SagaId.class)))
                .thenReturn(completedSteps);
        when(sagaStepService.saveStep(any(SagaStep.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(restTemplate.postForObject(anyString(), any(), eq(Map.class)))
                .thenReturn(Map.of("compensationCompleted", true));

        // When
        sagaCompensationEngine.startCompensation(saga);

        // Then
        verify(sagaStepService).getCompletedStepsBySagaId(saga.getId());
        verify(sagaStepService, times(completedSteps.size())).saveStep(any(SagaStep.class));
        verify(restTemplate, times(completedSteps.size())).postForObject(anyString(), any(), eq(Map.class));
    }

    @Test
    void testStartCompensation_NoCompletedSteps() {
        // Given
        Saga saga = createTestSaga();
        when(sagaStepService.getCompletedStepsBySagaId(any(SagaId.class))).thenReturn(List.of());

        // When
        sagaCompensationEngine.startCompensation(saga);

        // Then
        verify(sagaStepService).getCompletedStepsBySagaId(saga.getId());
        verify(sagaStepService, never()).saveStep(any(SagaStep.class));
        verify(restTemplate, never()).postForObject(anyString(), any(), eq(Map.class));
    }

    @Test
    void testCompensateStep_Success() {
        // Given
        SagaStep step = createCompletedStep();
        step.setCompensationEndpoint("/api/v1/compensate");
        
        when(sagaStepService.saveStep(any(SagaStep.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(restTemplate.postForObject(anyString(), any(), eq(Map.class)))
                .thenReturn(Map.of("compensationCompleted", true));

        // When
        sagaCompensationEngine.compensateStep(step);

        // Then
        assertEquals(SagaStepStatus.COMPENSATED, step.getStatus());
        assertNotNull(step.getCompensatedAt());
        
        verify(sagaStepService).saveStep(step);
        verify(sagaEventService).publishStepCompensationStartedEvent(step);
        verify(sagaEventService).publishStepCompensationCompletedEvent(eq(step), any(Map.class));
        verify(restTemplate).postForObject(contains("compensate"), any(), eq(Map.class));
    }

    @Test
    void testCompensateStep_NoCompensationEndpoint() {
        // Given
        SagaStep step = createCompletedStep();
        step.setCompensationEndpoint(null);

        // When
        sagaCompensationEngine.compensateStep(step);

        // Then
        assertEquals(SagaStepStatus.COMPENSATED, step.getStatus());
        verify(sagaStepService).saveStep(step);
        verify(restTemplate, never()).postForObject(anyString(), any(), eq(Map.class));
    }

    @Test
    void testCompensateStep_CompensationFailure() {
        // Given
        SagaStep step = createCompletedStep();
        step.setCompensationEndpoint("/api/v1/compensate");
        
        when(sagaStepService.saveStep(any(SagaStep.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(restTemplate.postForObject(anyString(), any(), eq(Map.class)))
                .thenThrow(new RuntimeException("Compensation service unavailable"));

        // When
        sagaCompensationEngine.compensateStep(step);

        // Then
        assertEquals(SagaStepStatus.FAILED, step.getStatus());
        assertNotNull(step.getErrorMessage());
        assertTrue(step.getErrorMessage().contains("Compensation failed"));
        
        verify(sagaStepService).saveStep(step);
        verify(sagaEventService).publishStepCompensationStartedEvent(step);
        verify(sagaEventService).publishStepCompensationFailedEvent(eq(step), anyString());
    }

    @Test
    void testCompensateStep_NotCompletedStatus() {
        // Given
        SagaStep step = createTestStep();
        step.markAsRunning(); // Set to RUNNING status

        // When
        sagaCompensationEngine.compensateStep(step);

        // Then
        verify(sagaStepService, never()).saveStep(any(SagaStep.class));
        verify(restTemplate, never()).postForObject(anyString(), any(), eq(Map.class));
    }

    @Test
    void testCompensateStep_EmptyCompensationEndpoint() {
        // Given
        SagaStep step = createCompletedStep();
        step.setCompensationEndpoint("");

        // When
        sagaCompensationEngine.compensateStep(step);

        // Then
        assertEquals(SagaStepStatus.COMPENSATED, step.getStatus());
        verify(sagaStepService).saveStep(step);
        verify(restTemplate, never()).postForObject(anyString(), any(), eq(Map.class));
    }

    private Saga createTestSaga() {
        return Saga.builder()
                .id(SagaId.generate())
                .sagaName("TestSaga")
                .status(SagaStatus.RUNNING)
                .tenantContext(tenantContext)
                .correlationId("corr-123")
                .paymentId("pay-456")
                .currentStepIndex(0)
                .build();
    }

    private List<SagaStep> createCompletedSteps(SagaId sagaId) {
        SagaStep step1 = createTestStep();
        step1.setSagaId(sagaId);
        step1.setSequence(1);
        step1.markAsCompleted(Map.of("result1", "success"));
        
        SagaStep step2 = createTestStep();
        step2.setSagaId(sagaId);
        step2.setSequence(2);
        step2.markAsCompleted(Map.of("result2", "success"));
        
        return List.of(step1, step2);
    }

    private SagaStep createTestStep() {
        return SagaStep.builder()
                .id(SagaStepId.generate())
                .stepName("TestStep")
                .stepType(SagaStepType.VALIDATION)
                .status(SagaStepStatus.PENDING)
                .sequence(1)
                .serviceName("test-service")
                .endpoint("/test")
                .compensationEndpoint("/api/v1/compensate")
                .maxRetries(3)
                .tenantContext(tenantContext)
                .correlationId("corr-123")
                .inputData(Map.of("test", "data"))
                .build();
    }

    private SagaStep createCompletedStep() {
        SagaStep step = createTestStep();
        step.markAsCompleted(Map.of("result", "success"));
        return step;
    }
}






