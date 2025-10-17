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

/** Unit tests for BusinessRuleEngine */
@ExtendWith(MockitoExtension.class)
class BusinessRuleEngineTest {

  private BusinessRuleEngine businessRuleEngine;

  @BeforeEach
  void setUp() {
    businessRuleEngine = new BusinessRuleEngine();
  }

  @Test
  void executeRules_WithValidPayment_ShouldPass() {
    // Given
    PaymentInitiatedEvent event = createValidPaymentEvent();
    ValidationContext context = createValidationContext();

    // When
    RuleExecutionFacade.RuleExecutionResult result =
        businessRuleEngine.executeRules(context, event);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getRuleType().toString()).isEqualTo(RuleType.BUSINESS);
    assertThat(result.isSuccess()).isTrue();
    assertThat(result.getAppliedRules())
        .contains(
            "BUSINESS_RULE_001",
            "BUSINESS_RULE_002",
            "BUSINESS_RULE_003",
            "BUSINESS_RULE_004",
            "BUSINESS_RULE_005");
    assertThat(result.getFailedRules()).isEmpty();
    assertThat(result.getFraudScore()).isEqualTo(0);
    assertThat(result.getRiskScore()).isEqualTo(0);
    assertThat(result.getExecutionTime()).isGreaterThanOrEqualTo(0);
  }

  @Test
  void executeRules_WithHighAmount_ShouldFail() {
    // Given
    PaymentInitiatedEvent event = createHighAmountPaymentEvent();
    ValidationContext context = createValidationContext();

    // When
    RuleExecutionFacade.RuleExecutionResult result =
        businessRuleEngine.executeRules(context, event);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getRuleType().toString()).isEqualTo(RuleType.BUSINESS);
    assertThat(result.isSuccess()).isFalse();
    assertThat(result.getAppliedRules()).contains("BUSINESS_RULE_001");
    assertThat(result.getFailedRules()).hasSize(1);
    assertThat(result.getFailedRules().get(0).getRuleId()).isEqualTo("BUSINESS_RULE_001");
    assertThat(result.getFailedRules().get(0).getRuleName()).isEqualTo("Amount Limit Check");
    assertThat(result.getFailedRules().get(0).getRuleType()).isEqualTo(RuleType.BUSINESS);
    assertThat(result.getFailedRules().get(0).getFailureReason()).contains("exceeds maximum limit");
    assertThat(result.getRiskScore()).isEqualTo(10);
  }

  @Test
  void executeRules_WithSameSourceAndDestination_ShouldFail() {
    // Given
    PaymentInitiatedEvent event = createSameAccountPaymentEvent();
    ValidationContext context = createValidationContext();

    // When
    RuleExecutionFacade.RuleExecutionResult result =
        businessRuleEngine.executeRules(context, event);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getRuleType().toString()).isEqualTo(RuleType.BUSINESS);
    assertThat(result.isSuccess()).isFalse();
    assertThat(result.getAppliedRules()).contains("BUSINESS_RULE_002");
    assertThat(result.getFailedRules()).hasSize(1);
    assertThat(result.getFailedRules().get(0).getRuleId()).isEqualTo("BUSINESS_RULE_002");
    assertThat(result.getFailedRules().get(0).getRuleName()).isEqualTo("Account Validation");
    assertThat(result.getFailedRules().get(0).getRuleType()).isEqualTo(RuleType.BUSINESS);
    assertThat(result.getFailedRules().get(0).getFailureReason()).contains("cannot be the same");
    assertThat(result.getRiskScore()).isEqualTo(10);
  }

  @Test
  void executeRules_WithEmptyCurrency_ShouldFail() {
    // Given
    PaymentInitiatedEvent event = createMissingCurrencyPaymentEvent();
    ValidationContext context = createValidationContext();

    // When
    RuleExecutionFacade.RuleExecutionResult result =
        businessRuleEngine.executeRules(context, event);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getRuleType().toString()).isEqualTo(RuleType.BUSINESS);
    assertThat(result.isSuccess()).isFalse();
    assertThat(result.getErrorMessage()).isNotNull();
    assertThat(result.getRiskScore()).isEqualTo(100);
  }

  @Test
  void executeRules_WithMultipleFailures_ShouldFail() {
    // Given
    PaymentInitiatedEvent event = createMultipleFailurePaymentEvent();
    ValidationContext context = createValidationContext();

    // When
    RuleExecutionFacade.RuleExecutionResult result =
        businessRuleEngine.executeRules(context, event);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getRuleType().toString()).isEqualTo(RuleType.BUSINESS);
    assertThat(result.isSuccess()).isFalse();
    assertThat(result.getAppliedRules()).contains("BUSINESS_RULE_001", "BUSINESS_RULE_002");
    assertThat(result.getFailedRules()).hasSize(2); // Amount limit + account validation
    assertThat(result.getRiskScore()).isEqualTo(20); // 2 failures * 10 points each
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
    event.setAmount(Money.of(new BigDecimal("150000.00"), Currency.getInstance("ZAR")));
    return event;
  }

  private PaymentInitiatedEvent createSameAccountPaymentEvent() {
    PaymentInitiatedEvent event = createValidPaymentEvent();
    event.setDestinationAccount("1234567890");
    return event;
  }

  private PaymentInitiatedEvent createMissingCurrencyPaymentEvent() {
    PaymentInitiatedEvent event = createValidPaymentEvent();
    event.setAmount(null);
    return event;
  }

  private PaymentInitiatedEvent createMultipleFailurePaymentEvent() {
    PaymentInitiatedEvent event = createHighAmountPaymentEvent();
    event.setDestinationAccount("1234567890");
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
        .correlationId("test-correlation-id")
        .validationId("validation-123")
        .startedAt(Instant.now())
        .build();
  }
}
