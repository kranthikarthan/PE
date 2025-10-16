package com.payments.validation.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.payments.contracts.events.PaymentInitiatedEvent;
import com.payments.contracts.payment.Money;
import com.payments.contracts.payment.PaymentId;
import com.payments.contracts.payment.TenantContext;
import com.payments.domain.validation.RuleType;
import com.payments.validation.service.RuleExecutionFacade.ValidationContext;
import java.math.BigDecimal;
import java.time.Instant;
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
    assertThat(result.getRuleType()).isEqualTo(RuleType.BUSINESS);
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
    assertThat(result.getExecutionTime()).isGreaterThan(0);
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
    assertThat(result.getRuleType()).isEqualTo(RuleType.BUSINESS);
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
    assertThat(result.getRuleType()).isEqualTo(RuleType.BUSINESS);
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
    PaymentInitiatedEvent event = createEmptyCurrencyPaymentEvent();
    ValidationContext context = createValidationContext();

    // When
    RuleExecutionFacade.RuleExecutionResult result =
        businessRuleEngine.executeRules(context, event);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getRuleType()).isEqualTo(RuleType.BUSINESS);
    assertThat(result.isSuccess()).isFalse();
    assertThat(result.getAppliedRules()).contains("BUSINESS_RULE_004");
    assertThat(result.getFailedRules()).hasSize(1);
    assertThat(result.getFailedRules().get(0).getRuleId()).isEqualTo("BUSINESS_RULE_004");
    assertThat(result.getFailedRules().get(0).getRuleName()).isEqualTo("Currency Validation");
    assertThat(result.getFailedRules().get(0).getRuleType()).isEqualTo(RuleType.BUSINESS);
    assertThat(result.getFailedRules().get(0).getFailureReason()).contains("currency is required");
    assertThat(result.getRiskScore()).isEqualTo(10);
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
    assertThat(result.getRuleType()).isEqualTo(RuleType.BUSINESS);
    assertThat(result.isSuccess()).isFalse();
    assertThat(result.getAppliedRules()).hasSize(5);
    assertThat(result.getFailedRules()).hasSize(2); // Amount limit + currency validation
    assertThat(result.getRiskScore()).isEqualTo(20); // 2 failures * 10 points each
  }

  @Test
  void executeRules_WithException_ShouldHandleGracefully() {
    // Given
    PaymentInitiatedEvent event = null; // This will cause an exception
    ValidationContext context = createValidationContext();

    // When
    RuleExecutionFacade.RuleExecutionResult result =
        businessRuleEngine.executeRules(context, event);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getRuleType()).isEqualTo(RuleType.BUSINESS);
    assertThat(result.isSuccess()).isFalse();
    assertThat(result.getErrorMessage()).isNotNull();
    assertThat(result.getRiskScore()).isEqualTo(100);
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
        .tenantContext(
            TenantContext.builder().tenantId("tenant-1").businessUnitId("business-unit-1").build())
        .amount(Money.builder().amount(new BigDecimal("1000.00")).currency("ZAR").build())
        .sourceAccount("1234567890")
        .destinationAccount("0987654321")
        .reference("Test Payment")
        .build();
  }

  private PaymentInitiatedEvent createHighAmountPaymentEvent() {
    PaymentInitiatedEvent event = createValidPaymentEvent();
    event.setAmount(Money.builder().amount(new BigDecimal("150000.00")).currency("ZAR").build());
    return event;
  }

  private PaymentInitiatedEvent createSameAccountPaymentEvent() {
    PaymentInitiatedEvent event = createValidPaymentEvent();
    event.setDestinationAccount("1234567890"); // Same as source
    return event;
  }

  private PaymentInitiatedEvent createEmptyCurrencyPaymentEvent() {
    PaymentInitiatedEvent event = createValidPaymentEvent();
    event.setAmount(Money.builder().amount(new BigDecimal("1000.00")).currency("").build());
    return event;
  }

  private PaymentInitiatedEvent createMultipleFailurePaymentEvent() {
    PaymentInitiatedEvent event = createValidPaymentEvent();
    event.setAmount(Money.builder().amount(new BigDecimal("150000.00")).currency("").build());
    return event;
  }

  private ValidationContext createValidationContext() {
    return ValidationContext.builder()
        .paymentId(com.payments.domain.payment.PaymentId.builder().value("payment-123").build())
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
