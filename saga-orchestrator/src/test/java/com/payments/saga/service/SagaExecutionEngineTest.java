package com.payments.saga.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.payments.domain.shared.TenantContext;
import com.payments.saga.domain.*;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

/** Unit tests for SagaExecutionEngine */
@ExtendWith(MockitoExtension.class)
class SagaExecutionEngineTest {

  @Mock private RestTemplate restTemplate;

  @Mock private SagaStepService sagaStepService;

  @Mock private SagaEventService sagaEventService;

  private SagaExecutionEngine sagaExecutionEngine;
  private TenantContext tenantContext;

  @BeforeEach
  void setUp() {
    sagaExecutionEngine = new SagaExecutionEngine(restTemplate, sagaStepService, sagaEventService);
    tenantContext = TenantContext.of("tenant-1", "Test Tenant", "bu-1", "Test Business Unit");
  }

  @Test
  void testExecuteStep_ValidationStep_Success() {
    // Given
    SagaStep step = createTestStep(SagaStepType.VALIDATION, "/api/v1/validate");
    Map<String, Object> mockResponse = Map.of("status", "validated", "result", "success");

    when(restTemplate.postForObject(anyString(), any(), eq(Map.class))).thenReturn(mockResponse);
    when(sagaStepService.saveStep(any(SagaStep.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    // When
    sagaExecutionEngine.executeStep(step);

    // Then
    assertEquals(SagaStepStatus.COMPLETED, step.getStatus());
    assertNotNull(step.getCompletedAt());
    assertEquals(mockResponse, step.getOutputData());

    verify(restTemplate).postForObject(contains("validation-service"), any(), eq(Map.class));
    verify(sagaStepService).saveStep(step);
    verify(sagaEventService).publishStepStartedEvent(step);
    verify(sagaEventService).publishStepCompletedEvent(eq(step), eq(mockResponse));
  }

  @Test
  void testExecuteStep_RoutingStep_Success() {
    // Given
    SagaStep step = createTestStep(SagaStepType.ROUTING, "/api/v1/route");
    Map<String, Object> mockResponse = Map.of("clearingSystem", "ACH", "routingDecision", "direct");

    when(restTemplate.postForObject(anyString(), any(), eq(Map.class))).thenReturn(mockResponse);
    when(sagaStepService.saveStep(any(SagaStep.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    // When
    sagaExecutionEngine.executeStep(step);

    // Then
    assertEquals(SagaStepStatus.COMPLETED, step.getStatus());
    assertEquals(mockResponse, step.getOutputData());

    verify(restTemplate).postForObject(contains("routing-service"), any(), eq(Map.class));
  }

  @Test
  void testExecuteStep_AccountAdapterStep_Success() {
    // Given
    SagaStep step = createTestStep(SagaStepType.ACCOUNT_ADAPTER, "/api/v1/account/operations");
    Map<String, Object> mockResponse = Map.of("accountProcessed", true, "balance", 1000.0);

    when(restTemplate.postForObject(anyString(), any(), eq(Map.class))).thenReturn(mockResponse);
    when(sagaStepService.saveStep(any(SagaStep.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    // When
    sagaExecutionEngine.executeStep(step);

    // Then
    assertEquals(SagaStepStatus.COMPLETED, step.getStatus());
    assertEquals(mockResponse, step.getOutputData());

    verify(restTemplate).postForObject(contains("account-adapter-service"), any(), eq(Map.class));
  }

  @Test
  void testExecuteStep_TransactionProcessingStep_Success() {
    // Given
    SagaStep step = createTestStep(SagaStepType.TRANSACTION_PROCESSING, "/api/v1/transactions");
    Map<String, Object> mockResponse = Map.of("transactionId", "txn-123", "status", "created");

    when(restTemplate.postForObject(anyString(), any(), eq(Map.class))).thenReturn(mockResponse);
    when(sagaStepService.saveStep(any(SagaStep.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    // When
    sagaExecutionEngine.executeStep(step);

    // Then
    assertEquals(SagaStepStatus.COMPLETED, step.getStatus());
    assertEquals(mockResponse, step.getOutputData());

    verify(restTemplate)
        .postForObject(contains("transaction-processing-service"), any(), eq(Map.class));
  }

  @Test
  void testExecuteStep_NotificationStep_Success() {
    // Given
    SagaStep step = createTestStep(SagaStepType.NOTIFICATION, "/api/v1/notify");
    Map<String, Object> mockResponse = Map.of("notificationSent", true, "messageId", "msg-123");

    when(restTemplate.postForObject(anyString(), any(), eq(Map.class))).thenReturn(mockResponse);
    when(sagaStepService.saveStep(any(SagaStep.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    // When
    sagaExecutionEngine.executeStep(step);

    // Then
    assertEquals(SagaStepStatus.COMPLETED, step.getStatus());
    assertEquals(mockResponse, step.getOutputData());

    verify(restTemplate).postForObject(contains("notification-service"), any(), eq(Map.class));
  }

  @Test
  void testExecuteStep_CompensationStep_Success() {
    // Given
    SagaStep step = createTestStep(SagaStepType.COMPENSATION, "/api/v1/compensate");
    step.setCompensationEndpoint("/api/v1/compensate");
    Map<String, Object> mockResponse = Map.of("compensationCompleted", true);

    when(restTemplate.postForObject(anyString(), any(), eq(Map.class))).thenReturn(mockResponse);
    when(sagaStepService.saveStep(any(SagaStep.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    // When
    sagaExecutionEngine.executeStep(step);

    // Then
    assertEquals(SagaStepStatus.COMPLETED, step.getStatus());
    assertEquals(mockResponse, step.getOutputData());

    verify(restTemplate).postForObject(contains("compensate"), any(), eq(Map.class));
  }

  @Test
  void testExecuteStep_ServiceFailure() {
    // Given
    SagaStep step = createTestStep(SagaStepType.VALIDATION, "/api/v1/validate");

    when(restTemplate.postForObject(anyString(), any(), eq(Map.class)))
        .thenThrow(new RuntimeException("Service unavailable"));

    // When & Then
    assertThrows(RuntimeException.class, () -> sagaExecutionEngine.executeStep(step));

    verify(restTemplate).postForObject(anyString(), any(), eq(Map.class));
  }

  @Test
  void testExecuteStep_NotPendingStatus() {
    // Given
    SagaStep step = createTestStep(SagaStepType.VALIDATION, "/api/v1/validate");
    step.markAsRunning(); // Set to RUNNING status

    // When
    sagaExecutionEngine.executeStep(step);

    // Then
    verify(restTemplate, never()).postForObject(anyString(), any(), eq(Map.class));
  }

  private SagaStep createTestStep(SagaStepType stepType, String endpoint) {
    return SagaStep.builder()
        .id(SagaStepId.generate())
        .sagaId(SagaId.generate())
        .stepName("TestStep")
        .stepType(stepType)
        .status(SagaStepStatus.PENDING)
        .sequence(1)
        .serviceName("test-service")
        .endpoint(endpoint)
        .maxRetries(3)
        .tenantContext(tenantContext)
        .correlationId("corr-123")
        .inputData(Map.of("test", "data"))
        .build();
  }
}
