package com.payments.saga.integration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.payments.domain.shared.TenantContext;
import com.payments.saga.domain.*;
import com.payments.saga.event.SagaEventPublisher;
import com.payments.saga.service.SagaEventService;
import com.payments.saga.service.SagaService;
import com.payments.saga.service.SagaStepService;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.RedisContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

/** Integration tests for Saga Events using Testcontainers */
@SpringBootTest
@Testcontainers
class SagaEventIntegrationTest {

  @Container
  static PostgreSQLContainer<?> postgres =
      new PostgreSQLContainer<>("postgres:15-alpine")
          .withDatabaseName("saga_test")
          .withUsername("test")
          .withPassword("test");

  @Container static RedisContainer redis = new RedisContainer("redis:7-alpine");

  @Container
  static KafkaContainer kafka =
      new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.4.0"));

  @DynamicPropertySource
  static void configureProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", postgres::getJdbcUrl);
    registry.add("spring.datasource.username", postgres::getUsername);
    registry.add("spring.datasource.password", postgres::getPassword);
    registry.add("spring.redis.host", redis::getHost);
    registry.add("spring.redis.port", redis::getFirstMappedPort);
    registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
  }

  @Autowired private SagaEventService sagaEventService;

  @Autowired private SagaEventPublisher sagaEventPublisher;

  @Autowired private SagaService sagaService;

  @Autowired private SagaStepService sagaStepService;

  @MockBean private KafkaTemplate<String, String> kafkaTemplate;

  private TenantContext tenantContext;

  @BeforeEach
  void setUp() {
    tenantContext = TenantContext.of("tenant-1", "Test Tenant", "bu-1", "Test Business Unit");
  }

  @Test
  void testSagaStartedEvent_Persistence() {
    // Given
    SagaId sagaId = SagaId.generate();
    SagaStartedEvent event =
        new SagaStartedEvent(
            sagaId,
            tenantContext,
            "corr-123",
            "TestSaga",
            Map.of("paymentId", "pay-456", "amount", 1000.0));

    // When
    sagaEventService.publishEvent(event);

    // Then
    // Verify event is persisted in database
    List<SagaEvent> events = sagaEventService.getEventsBySagaId(sagaId);
    assertFalse(events.isEmpty());

    SagaEvent savedEvent = events.get(0);
    assertEquals(SagaEventType.SAGA_STARTED, savedEvent.getEventType());
    assertEquals(sagaId, savedEvent.getSagaId());
    assertEquals(tenantContext, savedEvent.getTenantContext());
    assertEquals("corr-123", savedEvent.getCorrelationId());
  }

  @Test
  void testSagaStepStartedEvent_Persistence() {
    // Given
    SagaId sagaId = SagaId.generate();
    SagaStepStartedEvent event =
        new SagaStepStartedEvent(
            sagaId,
            tenantContext,
            "corr-123",
            "ValidatePayment",
            SagaStepType.VALIDATION,
            1,
            "validation-service",
            "/api/v1/validate");

    // When
    sagaEventService.publishEvent(event);

    // Then
    List<SagaEvent> events = sagaEventService.getEventsBySagaId(sagaId);
    assertFalse(events.isEmpty());

    SagaEvent savedEvent = events.get(0);
    assertEquals(SagaEventType.SAGA_STEP_STARTED, savedEvent.getEventType());
    assertEquals(sagaId, savedEvent.getSagaId());
  }

  @Test
  void testSagaStepCompletedEvent_Persistence() {
    // Given
    SagaId sagaId = SagaId.generate();
    SagaStepCompletedEvent event =
        new SagaStepCompletedEvent(
            sagaId,
            tenantContext,
            "corr-123",
            "ValidatePayment",
            SagaStepType.VALIDATION,
            1,
            "validation-service",
            Map.of("validationResult", "success"));

    // When
    sagaEventService.publishEvent(event);

    // Then
    List<SagaEvent> events = sagaEventService.getEventsBySagaId(sagaId);
    assertFalse(events.isEmpty());

    SagaEvent savedEvent = events.get(0);
    assertEquals(SagaEventType.SAGA_STEP_COMPLETED, savedEvent.getEventType());
    assertEquals(sagaId, savedEvent.getSagaId());
  }

  @Test
  void testSagaStepFailedEvent_Persistence() {
    // Given
    SagaId sagaId = SagaId.generate();
    SagaStepFailedEvent event =
        new SagaStepFailedEvent(
            sagaId,
            tenantContext,
            "corr-123",
            "ValidatePayment",
            SagaStepType.VALIDATION,
            1,
            "validation-service",
            "Validation failed",
            Map.of("error", "invalid_data"));

    // When
    sagaEventService.publishEvent(event);

    // Then
    List<SagaEvent> events = sagaEventService.getEventsBySagaId(sagaId);
    assertFalse(events.isEmpty());

    SagaEvent savedEvent = events.get(0);
    assertEquals(SagaEventType.SAGA_STEP_FAILED, savedEvent.getEventType());
    assertEquals(sagaId, savedEvent.getSagaId());
  }

  @Test
  void testSagaCompletedEvent_Persistence() {
    // Given
    SagaId sagaId = SagaId.generate();
    SagaCompletedEvent event =
        new SagaCompletedEvent(
            sagaId,
            tenantContext,
            "corr-123",
            "TestSaga",
            Map.of("totalSteps", 5, "duration", 1200));

    // When
    sagaEventService.publishEvent(event);

    // Then
    List<SagaEvent> events = sagaEventService.getEventsBySagaId(sagaId);
    assertFalse(events.isEmpty());

    SagaEvent savedEvent = events.get(0);
    assertEquals(SagaEventType.SAGA_COMPLETED, savedEvent.getEventType());
    assertEquals(sagaId, savedEvent.getSagaId());
  }

  @Test
  void testSagaCompensationStartedEvent_Persistence() {
    // Given
    SagaId sagaId = SagaId.generate();
    SagaCompensationStartedEvent event =
        new SagaCompensationStartedEvent(
            sagaId,
            tenantContext,
            "corr-123",
            "TestSaga",
            "Step failed",
            Map.of("failedStep", "ValidatePayment"));

    // When
    sagaEventService.publishEvent(event);

    // Then
    List<SagaEvent> events = sagaEventService.getEventsBySagaId(sagaId);
    assertFalse(events.isEmpty());

    SagaEvent savedEvent = events.get(0);
    assertEquals(SagaEventType.SAGA_COMPENSATION_STARTED, savedEvent.getEventType());
    assertEquals(sagaId, savedEvent.getSagaId());
  }

  @Test
  void testSagaCompensatedEvent_Persistence() {
    // Given
    SagaId sagaId = SagaId.generate();
    SagaCompensatedEvent event =
        new SagaCompensatedEvent(
            sagaId,
            tenantContext,
            "corr-123",
            "TestSaga",
            Map.of("compensatedSteps", 3, "compensationDuration", 800));

    // When
    sagaEventService.publishEvent(event);

    // Then
    List<SagaEvent> events = sagaEventService.getEventsBySagaId(sagaId);
    assertFalse(events.isEmpty());

    SagaEvent savedEvent = events.get(0);
    assertEquals(SagaEventType.SAGA_COMPENSATED, savedEvent.getEventType());
    assertEquals(sagaId, savedEvent.getSagaId());
  }

  @Test
  void testSagaEventPublisher_KafkaIntegration() {
    // Given
    SagaId sagaId = SagaId.generate();
    SagaStartedEvent event =
        new SagaStartedEvent(
            sagaId,
            tenantContext,
            "corr-123",
            "TestSaga",
            Map.of("paymentId", "pay-456", "amount", 1000.0));

    // When
    sagaEventPublisher.publishSagaStarted(event);

    // Then
    verify(kafkaTemplate).send(eq("saga.started"), eq(sagaId.getValue()), anyString());
  }

  @Test
  void testSagaEventPublisher_StepStartedEvent() {
    // Given
    SagaId sagaId = SagaId.generate();
    SagaStepStartedEvent event =
        new SagaStepStartedEvent(
            sagaId,
            tenantContext,
            "corr-123",
            "ValidatePayment",
            SagaStepType.VALIDATION,
            1,
            "validation-service",
            "/api/v1/validate");

    // When
    sagaEventPublisher.publishSagaStepStarted(event);

    // Then
    verify(kafkaTemplate).send(eq("saga.step.started"), eq(sagaId.getValue()), anyString());
  }

  @Test
  void testSagaEventPublisher_StepCompletedEvent() {
    // Given
    SagaId sagaId = SagaId.generate();
    SagaStepCompletedEvent event =
        new SagaStepCompletedEvent(
            sagaId,
            tenantContext,
            "corr-123",
            "ValidatePayment",
            SagaStepType.VALIDATION,
            1,
            "validation-service",
            Map.of("validationResult", "success"));

    // When
    sagaEventPublisher.publishSagaStepCompleted(event);

    // Then
    verify(kafkaTemplate).send(eq("saga.step.completed"), eq(sagaId.getValue()), anyString());
  }

  @Test
  void testSagaEventPublisher_StepFailedEvent() {
    // Given
    SagaId sagaId = SagaId.generate();
    SagaStepFailedEvent event =
        new SagaStepFailedEvent(
            sagaId,
            tenantContext,
            "corr-123",
            "ValidatePayment",
            SagaStepType.VALIDATION,
            1,
            "validation-service",
            "Validation failed",
            Map.of("error", "invalid_data"));

    // When
    sagaEventPublisher.publishSagaStepFailed(event);

    // Then
    verify(kafkaTemplate).send(eq("saga.step.failed"), eq(sagaId.getValue()), anyString());
  }

  @Test
  void testSagaEventPublisher_CompletedEvent() {
    // Given
    SagaId sagaId = SagaId.generate();
    SagaCompletedEvent event =
        new SagaCompletedEvent(
            sagaId,
            tenantContext,
            "corr-123",
            "TestSaga",
            Map.of("totalSteps", 5, "duration", 1200));

    // When
    sagaEventPublisher.publishSagaCompleted(event);

    // Then
    verify(kafkaTemplate).send(eq("saga.completed"), eq(sagaId.getValue()), anyString());
  }

  @Test
  void testSagaEventPublisher_CompensationStartedEvent() {
    // Given
    SagaId sagaId = SagaId.generate();
    SagaCompensationStartedEvent event =
        new SagaCompensationStartedEvent(
            sagaId,
            tenantContext,
            "corr-123",
            "TestSaga",
            "Step failed",
            Map.of("failedStep", "ValidatePayment"));

    // When
    sagaEventPublisher.publishSagaCompensationStarted(event);

    // Then
    verify(kafkaTemplate).send(eq("saga.compensation.started"), eq(sagaId.getValue()), anyString());
  }

  @Test
  void testSagaEventPublisher_CompensatedEvent() {
    // Given
    SagaId sagaId = SagaId.generate();
    SagaCompensatedEvent event =
        new SagaCompensatedEvent(
            sagaId,
            tenantContext,
            "corr-123",
            "TestSaga",
            Map.of("compensatedSteps", 3, "compensationDuration", 800));

    // When
    sagaEventPublisher.publishSagaCompensated(event);

    // Then
    verify(kafkaTemplate).send(eq("saga.compensated"), eq(sagaId.getValue()), anyString());
  }

  @Test
  void testSagaEventRetrieval_BySagaId() {
    // Given
    SagaId sagaId = SagaId.generate();
    SagaStartedEvent event1 =
        new SagaStartedEvent(
            sagaId, tenantContext, "corr-123", "TestSaga", Map.of("paymentId", "pay-456"));
    SagaStepStartedEvent event2 =
        new SagaStepStartedEvent(
            sagaId,
            tenantContext,
            "corr-123",
            "ValidatePayment",
            SagaStepType.VALIDATION,
            1,
            "validation-service",
            "/api/v1/validate");

    sagaEventService.publishEvent(event1);
    sagaEventService.publishEvent(event2);

    // When
    List<SagaEvent> events = sagaEventService.getEventsBySagaId(sagaId);

    // Then
    assertEquals(2, events.size());
    assertTrue(events.stream().anyMatch(e -> e.getEventType() == SagaEventType.SAGA_STARTED));
    assertTrue(events.stream().anyMatch(e -> e.getEventType() == SagaEventType.SAGA_STEP_STARTED));
  }

  @Test
  void testSagaEventRetrieval_ByEventType() {
    // Given
    SagaId sagaId = SagaId.generate();
    SagaStartedEvent event =
        new SagaStartedEvent(
            sagaId, tenantContext, "corr-123", "TestSaga", Map.of("paymentId", "pay-456"));

    sagaEventService.publishEvent(event);

    // When
    List<SagaEvent> events = sagaEventService.getEventsByType(SagaEventType.SAGA_STARTED);

    // Then
    assertFalse(events.isEmpty());
    assertTrue(events.stream().allMatch(e -> e.getEventType() == SagaEventType.SAGA_STARTED));
  }

  @Test
  void testSagaEventRetrieval_ByTenant() {
    // Given
    SagaId sagaId = SagaId.generate();
    SagaStartedEvent event =
        new SagaStartedEvent(
            sagaId, tenantContext, "corr-123", "TestSaga", Map.of("paymentId", "pay-456"));

    sagaEventService.publishEvent(event);

    // When
    List<SagaEvent> events = sagaEventService.getEventsByTenant(tenantContext);

    // Then
    assertFalse(events.isEmpty());
    assertTrue(events.stream().allMatch(e -> e.getTenantContext().equals(tenantContext)));
  }
}
