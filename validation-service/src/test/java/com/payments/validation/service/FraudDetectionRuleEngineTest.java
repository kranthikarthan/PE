package com.payments.validation.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.payments.contracts.events.PaymentInitiatedEvent;
import com.payments.domain.shared.Money;
import com.payments.domain.shared.PaymentId;
import com.payments.domain.shared.TenantContext;
import com.payments.domain.validation.RuleType;
import com.payments.validation.service.RuleExecutionFacade.ValidationContext;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

/** Unit tests for FraudDetectionRuleEngine */
@ExtendWith(MockitoExtension.class)
class FraudDetectionRuleEngineTest {

  private FraudDetectionRuleEngine fraudDetectionRuleEngine;

  @BeforeEach
  void setUp() {
    fraudDetectionRuleEngine = new FraudDetectionRuleEngine();
  }

  @Test
  void executeRules_WithValidPayment_ShouldPass() {
    // Given
    PaymentInitiatedEvent event = createValidPaymentEvent();
    ValidationContext context = createValidationContext();

    // When
    RuleExecutionFacade.RuleExecutionResult result =
        fraudDetectionRuleEngine.executeRules(context, event);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getRuleType().toString()).isEqualTo(RuleType.FRAUD);
    assertThat(result.isSuccess()).isTrue();
    assertThat(result.getAppliedRules())
        .contains(
            "FRAUD_RULE_001",
            "FRAUD_RULE_002",
            "FRAUD_RULE_003",
            "FRAUD_RULE_004",
            "FRAUD_RULE_005");
    assertThat(result.getFailedRules()).isEmpty();
    assertThat(result.getFraudScore()).isEqualTo(0);
    assertThat(result.getRiskScore()).isEqualTo(0);
    assertThat(result.getExecutionTime()).isGreaterThan(0);
  }

  @Test
  void executeRules_WithHighAmount_ShouldDetectFraud() {
    // Given
    PaymentInitiatedEvent event = createHighAmountPaymentEvent();
    ValidationContext context = createValidationContext();

    // When
    RuleExecutionFacade.RuleExecutionResult result =
        fraudDetectionRuleEngine.executeRules(context, event);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getRuleType().toString()).isEqualTo(RuleType.FRAUD);
    assertThat(result.isSuccess()).isFalse();
    assertThat(result.getAppliedRules()).contains("FRAUD_RULE_001");
    assertThat(result.getFailedRules()).hasSize(1);
    assertThat(result.getFailedRules().get(0).getRuleId()).isEqualTo("FRAUD_RULE_001");
    assertThat(result.getFailedRules().get(0).getRuleName()).isEqualTo("Velocity Check");
    assertThat(result.getFailedRules().get(0).getRuleType()).isEqualTo(RuleType.FRAUD);
    assertThat(result.getFailedRules().get(0).getFailureReason()).contains("velocity check failed");
    assertThat(result.getFraudScore()).isEqualTo(25);
    assertThat(result.getRiskScore()).isEqualTo(20);
  }

  @Test
  void executeRules_WithVeryHighAmount_ShouldDetectMultipleFraud() {
    // Given
    PaymentInitiatedEvent event = createVeryHighAmountPaymentEvent();
    ValidationContext context = createValidationContext();

    // When
    RuleExecutionFacade.RuleExecutionResult result =
        fraudDetectionRuleEngine.executeRules(context, event);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getRuleType().toString()).isEqualTo(RuleType.FRAUD);
    assertThat(result.isSuccess()).isFalse();
    assertThat(result.getAppliedRules()).hasSize(5);
    assertThat(result.getFailedRules()).hasSize(2); // Velocity check + amount anomaly
    assertThat(result.getFraudScore()).isEqualTo(55); // 25 + 30
    assertThat(result.getRiskScore()).isEqualTo(40); // 2 failures * 20 points each
  }

  @Test
  void executeRules_WithSuspiciousAccount_ShouldDetectFraud() {
    // Given
    PaymentInitiatedEvent event = createSuspiciousAccountPaymentEvent();
    ValidationContext context = createValidationContext();

    // When
    RuleExecutionFacade.RuleExecutionResult result =
        fraudDetectionRuleEngine.executeRules(context, event);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getRuleType().toString()).isEqualTo(RuleType.FRAUD);
    assertThat(result.isSuccess()).isFalse();
    assertThat(result.getAppliedRules()).contains("FRAUD_RULE_003");
    assertThat(result.getFailedRules()).hasSize(1);
    assertThat(result.getFailedRules().get(0).getRuleId()).isEqualTo("FRAUD_RULE_003");
    assertThat(result.getFailedRules().get(0).getRuleName()).isEqualTo("Account Pattern Analysis");
    assertThat(result.getFailedRules().get(0).getRuleType()).isEqualTo(RuleType.FRAUD);
    assertThat(result.getFailedRules().get(0).getFailureReason())
        .contains("Suspicious account pattern");
    assertThat(result.getFraudScore()).isEqualTo(20);
    assertThat(result.getRiskScore()).isEqualTo(20);
  }

  @Test

  private PaymentInitiatedEvent createValidPaymentEvent() {
    PaymentInitiatedEvent event = new PaymentInitiatedEvent();
    event.setEventId(UUID.randomUUID());
    event.setEventType("PaymentInitiated");
    event.setTimestamp(Instant.now());
    event.setCorrelationId(UUID.randomUUID());
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

  private PaymentInitiatedEvent createHighAmountPaymentEvent() {
    PaymentInitiatedEvent event = createValidPaymentEvent();
    event.setAmount(Money.of(new BigDecimal("60000.00"), Currency.getInstance("ZAR")));
    return event;
  }

  private PaymentInitiatedEvent createVeryHighAmountPaymentEvent() {
    PaymentInitiatedEvent event = createValidPaymentEvent();
    event.setAmount(Money.of(new BigDecimal("80000.00"), Currency.getInstance("ZAR")));
    return event;
  }

  private PaymentInitiatedEvent createSuspiciousAccountPaymentEvent() {
    PaymentInitiatedEvent event = createValidPaymentEvent();
    event.setSourceAccount("9991234567");
    return event;
  }

  private ValidationContext createValidationContext() {
    return ValidationContext.builder()
        .paymentId(PaymentId.of("payment-123"))
        .tenantContext(
            com.payments.domain.shared.TenantContext.builder()
                .tenantId("tenant-1")
                .businessUnitId("business-unit-1")
                .build())
        .correlationId(UUID.randomUUID().toString())
        .validationId("validation-123")
        .startedAt(Instant.now())
        .build();
  }
}
