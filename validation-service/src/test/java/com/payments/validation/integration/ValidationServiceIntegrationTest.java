package com.payments.validation.integration;

import com.payments.contracts.events.PaymentInitiatedEvent;
import com.payments.contracts.events.PaymentValidatedEvent;
import com.payments.contracts.events.ValidationFailedEvent;
import com.payments.contracts.payment.PaymentId;
import com.payments.contracts.payment.Money;
import com.payments.contracts.payment.TenantContext;
import com.payments.validation.service.ValidationOrchestrator;
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

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for Validation Service
 * 
 * Tests the complete validation flow:
 * - Event consumption
 * - Rule execution
 * - Result persistence
 * - Event publishing
 */
@SpringBootTest
@Testcontainers
@EmbeddedKafka(partitions = 1, topics = {"payment-initiated", "payment-validated", "validation-failed"})
@ActiveProfiles("test")
class ValidationServiceIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("validation_test")
            .withUsername("test")
            .withPassword("test");

    @Autowired
    private ValidationOrchestrator validationOrchestrator;

    private CountDownLatch validationResultLatch;
    private CountDownLatch validationFailedLatch;
    private PaymentValidatedEvent receivedValidatedEvent;
    private ValidationFailedEvent receivedFailedEvent;

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
        validationOrchestrator.validatePayment(event, "test-correlation-id", "tenant-1", "business-unit-1");

        // Then
        boolean resultReceived = validationResultLatch.await(10, TimeUnit.SECONDS);
        assertThat(resultReceived).isTrue();
        assertThat(receivedValidatedEvent).isNotNull();
        assertThat(receivedValidatedEvent.getPaymentId().getValue()).isEqualTo("payment-123");
        assertThat(receivedValidatedEvent.getTenantId()).isEqualTo("tenant-1");
        assertThat(receivedValidatedEvent.getBusinessUnitId()).isEqualTo("business-unit-1");
        assertThat(receivedValidatedEvent.getCorrelationId()).isEqualTo("test-correlation-id");
    }

    @Test
    void validatePayment_WithInvalidPayment_ShouldPublishFailedEvent() throws InterruptedException {
        // Given
        PaymentInitiatedEvent event = createInvalidPaymentEvent();

        // When
        validationOrchestrator.validatePayment(event, "test-correlation-id", "tenant-1", "business-unit-1");

        // Then
        boolean resultReceived = validationFailedLatch.await(10, TimeUnit.SECONDS);
        assertThat(resultReceived).isTrue();
        assertThat(receivedFailedEvent).isNotNull();
        assertThat(receivedFailedEvent.getPaymentId().getValue()).isEqualTo("payment-456");
        assertThat(receivedFailedEvent.getTenantId()).isEqualTo("tenant-1");
        assertThat(receivedFailedEvent.getBusinessUnitId()).isEqualTo("business-unit-1");
        assertThat(receivedFailedEvent.getCorrelationId()).isEqualTo("test-correlation-id");
        assertThat(receivedFailedEvent.getFailedRules()).isNotEmpty();
    }

    @Test
    void validatePayment_WithHighRiskPayment_ShouldPublishFailedEvent() throws InterruptedException {
        // Given
        PaymentInitiatedEvent event = createHighRiskPaymentEvent();

        // When
        validationOrchestrator.validatePayment(event, "test-correlation-id", "tenant-1", "business-unit-1");

        // Then
        boolean resultReceived = validationFailedLatch.await(10, TimeUnit.SECONDS);
        assertThat(resultReceived).isTrue();
        assertThat(receivedFailedEvent).isNotNull();
        assertThat(receivedFailedEvent.getPaymentId().getValue()).isEqualTo("payment-789");
        assertThat(receivedFailedEvent.getTenantId()).isEqualTo("tenant-1");
        assertThat(receivedFailedEvent.getBusinessUnitId()).isEqualTo("business-unit-1");
        assertThat(receivedFailedEvent.getCorrelationId()).isEqualTo("test-correlation-id");
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
        return PaymentInitiatedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType("PaymentInitiated")
                .timestamp(Instant.now())
                .correlationId("test-correlation-id")
                .source("payment-initiation-service")
                .version("1.0.0")
                .tenantId("tenant-1")
                .businessUnitId("business-unit-1")
                .paymentId(PaymentId.builder().value("payment-123").build())
                .tenantContext(TenantContext.builder()
                        .tenantId("tenant-1")
                        .businessUnitId("business-unit-1")
                        .build())
                .amount(Money.builder()
                        .amount(new BigDecimal("1000.00"))
                        .currency("ZAR")
                        .build())
                .sourceAccount("1234567890")
                .destinationAccount("0987654321")
                .reference("Test Payment")
                .build();
    }

    private PaymentInitiatedEvent createInvalidPaymentEvent() {
        return PaymentInitiatedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType("PaymentInitiated")
                .timestamp(Instant.now())
                .correlationId("test-correlation-id")
                .source("payment-initiation-service")
                .version("1.0.0")
                .tenantId("tenant-1")
                .businessUnitId("business-unit-1")
                .paymentId(PaymentId.builder().value("payment-456").build())
                .tenantContext(TenantContext.builder()
                        .tenantId("tenant-1")
                        .businessUnitId("business-unit-1")
                        .build())
                .amount(Money.builder()
                        .amount(new BigDecimal("150000.00"))
                        .currency("ZAR")
                        .build())
                .sourceAccount("1234567890")
                .destinationAccount("1234567890") // Same as source - should fail
                .reference("") // Empty reference - should fail
                .build();
    }

    private PaymentInitiatedEvent createHighRiskPaymentEvent() {
        return PaymentInitiatedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType("PaymentInitiated")
                .timestamp(Instant.now())
                .correlationId("test-correlation-id")
                .source("payment-initiation-service")
                .version("1.0.0")
                .tenantId("tenant-1")
                .businessUnitId("business-unit-1")
                .paymentId(PaymentId.builder().value("payment-789").build())
                .tenantContext(TenantContext.builder()
                        .tenantId("tenant-1")
                        .businessUnitId("business-unit-1")
                        .build())
                .amount(Money.builder()
                        .amount(new BigDecimal("250000.00"))
                        .currency("USD")
                        .build())
                .sourceAccount("9991234567") // Suspicious account
                .destinationAccount("RISK1234567") // High-risk counterparty
                .reference("High Risk Payment")
                .build();
    }
}
