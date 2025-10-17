package com.payments.validation.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.payments.contracts.events.PaymentInitiatedEvent;
import com.payments.contracts.events.PaymentValidatedEvent;
import com.payments.contracts.events.ValidationFailedEvent;
import com.payments.domain.shared.Money;
import com.payments.domain.shared.PaymentId;
import com.payments.domain.shared.TenantContext;
import com.payments.validation.service.ValidationOrchestrator;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Integration tests for Validation Service
 *
 * <p>Tests the complete validation flow: - Event consumption - Rule execution - Result persistence
 * - Event publishing
 */
@SpringBootTest
@Testcontainers
@EmbeddedKafka(
    partitions = 1,
    topics = {"payment-initiated", "payment-validated", "validation-failed"})
@ActiveProfiles("test")
class ValidationServiceIntegrationTest {

  @Container
  static PostgreSQLContainer<?> postgres =
      new PostgreSQLContainer<>("postgres:15")
          .withDatabaseName("validation_test")
          .withUsername("test")
          .withPassword("test");

  @Autowired private ValidationOrchestrator validationOrchestrator;

  private CountDownLatch validationResultLatch;
  private CountDownLatch validationFailedLatch;
  private PaymentValidatedEvent receivedValidatedEvent;
  private ValidationFailedEvent receivedFailedEvent;

  private static final UUID TEST_CORRELATION_ID =
      UUID.nameUUIDFromBytes("validation-test-correlation".getBytes());

  @BeforeEach
  void setUp() {
    validationResultLatch = new CountDownLatch(1);
    validationFailedLatch = new CountDownLatch(1);
    receivedValidatedEvent = null;
    receivedFailedEvent = null;
  }

  @Test
  void validatePayment_WithValidPayment_ShouldPublishValidatedEvent() throws InterruptedException {
    // Given
    PaymentInitiatedEvent event = createValidPaymentEvent();

    // When
    validationOrchestrator.validatePayment(
        event, TEST_CORRELATION_ID.toString(), "tenant-1", "business-unit-1");

    // Then
    boolean resultReceived = validationResultLatch.await(10, TimeUnit.SECONDS);
    assertThat(resultReceived).isTrue();
    assertThat(receivedValidatedEvent).isNotNull();
    assertThat(receivedValidatedEvent.getPaymentId().getValue()).isEqualTo("payment-123");
    assertThat(receivedValidatedEvent.getTenantId()).isEqualTo("tenant-1");
    assertThat(receivedValidatedEvent.getBusinessUnitId()).isEqualTo("business-unit-1");
    assertThat(receivedValidatedEvent.getCorrelationId()).isEqualTo(TEST_CORRELATION_ID);
  }

  @Test
  void validatePayment_WithInvalidPayment_ShouldPublishFailedEvent() throws InterruptedException {
    // Given
    PaymentInitiatedEvent event = createInvalidPaymentEvent();

    // When
    validationOrchestrator.validatePayment(
        event, TEST_CORRELATION_ID.toString(), "tenant-1", "business-unit-1");

    // Then
    boolean resultReceived = validationFailedLatch.await(10, TimeUnit.SECONDS);
    assertThat(resultReceived).isTrue();
    assertThat(receivedFailedEvent).isNotNull();
    assertThat(receivedFailedEvent.getPaymentId().getValue()).isEqualTo("payment-456");
    assertThat(receivedFailedEvent.getTenantId()).isEqualTo("tenant-1");
    assertThat(receivedFailedEvent.getBusinessUnitId()).isEqualTo("business-unit-1");
    assertThat(receivedFailedEvent.getCorrelationId()).isEqualTo(TEST_CORRELATION_ID);
    assertThat(receivedFailedEvent.getFailedRules()).isNotEmpty();
  }

  @Test
  void validatePayment_WithHighRiskPayment_ShouldPublishFailedEvent() throws InterruptedException {
    // Given
    PaymentInitiatedEvent event = createHighRiskPaymentEvent();

    // When
    validationOrchestrator.validatePayment(
        event, TEST_CORRELATION_ID.toString(), "tenant-1", "business-unit-1");

    // Then
    boolean resultReceived = validationFailedLatch.await(10, TimeUnit.SECONDS);
    assertThat(resultReceived).isTrue();
    assertThat(receivedFailedEvent).isNotNull();
    assertThat(receivedFailedEvent.getPaymentId().getValue()).isEqualTo("payment-789");
    assertThat(receivedFailedEvent.getTenantId()).isEqualTo("tenant-1");
    assertThat(receivedFailedEvent.getBusinessUnitId()).isEqualTo("business-unit-1");
    assertThat(receivedFailedEvent.getCorrelationId()).isEqualTo(TEST_CORRELATION_ID);
    assertThat(receivedFailedEvent.getFailedRules()).isNotEmpty();
  }

  @KafkaListener(topics = "payment-validated")
  public void handlePaymentValidated(PaymentValidatedEvent event) {
    receivedValidatedEvent = event;
    validationResultLatch.countDown();
  }

  @KafkaListener(topics = "validation-failed")
  public void handleValidationFailed(ValidationFailedEvent event) {
    receivedFailedEvent = event;
    validationFailedLatch.countDown();
  }

  private PaymentInitiatedEvent createValidPaymentEvent() {
    PaymentInitiatedEvent event = new PaymentInitiatedEvent();
    event.setEventId(UUID.randomUUID());
    event.setEventType("PaymentInitiated");
    event.setTimestamp(Instant.now());
    event.setCorrelationId(TEST_CORRELATION_ID);
    event.setSource("payment-initiation-service");
    event.setVersion("1.0.0");
    event.setTenantId("tenant-1");
    event.setBusinessUnitId("business-unit-1");
    event.setPaymentId(PaymentId.of("payment-123"));
    event.setTenantContext(
        TenantContext.builder().tenantId("tenant-1").businessUnitId("business-unit-1").build());
    event.setAmount(Money.of(new BigDecimal("1000.00"), Currency.getInstance("ZAR")));
    event.setSourceAccount("1234567890");
    event.setDestinationAccount("0987654321");
    event.setReference("Test Payment");
    event.setPaymentType(com.payments.contracts.payment.PaymentType.EFT);
    event.setPriority(com.payments.contracts.payment.Priority.NORMAL);
    event.setInitiatedBy("user@example.com");
    event.setInitiatedAt(Instant.now());
    return event;
  }

  private PaymentInitiatedEvent createInvalidPaymentEvent() {
    PaymentInitiatedEvent event = createValidPaymentEvent();
    event.setPaymentId(PaymentId.of("payment-456"));
    event.setAmount(Money.of(new BigDecimal("150000.00"), Currency.getInstance("ZAR")));
    event.setDestinationAccount("1234567890");
    event.setReference("");
    return event;
  }

  private PaymentInitiatedEvent createHighRiskPaymentEvent() {
    PaymentInitiatedEvent event = createValidPaymentEvent();
    event.setPaymentId(PaymentId.of("payment-789"));
    event.setAmount(Money.of(new BigDecimal("250000.00"), Currency.getInstance("USD")));
    event.setSourceAccount("9991234567");
    event.setDestinationAccount("RISK1234567");
    event.setReference("High Risk Payment");
    return event;
  }
}
