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

/** Unit tests for ComplianceRuleEngine */
@ExtendWith(MockitoExtension.class)
class ComplianceRuleEngineTest {

  private ComplianceRuleEngine complianceRuleEngine;

  @BeforeEach
  void setUp() {
    complianceRuleEngine = new ComplianceRuleEngine();
  }

  @Test
  void executeRules_WithValidPayment_ShouldPass() {
    PaymentInitiatedEvent event = createValidPaymentEvent();
    ValidationContext context = createValidationContext();

    RuleExecutionFacade.RuleExecutionResult result =
        complianceRuleEngine.executeRules(context, event);

    assertThat(result).isNotNull();
    assertThat(result.getRuleType().toString()).isEqualTo("COMPLIANCE");
    assertThat(result.isSuccess()).isTrue();
    assertThat(result.getAppliedRules())
        .containsExactly(
            "COMPLIANCE_RULE_001",
            "COMPLIANCE_RULE_002",
            "COMPLIANCE_RULE_003",
            "COMPLIANCE_RULE_004");
    assertThat(result.getFailedRules()).isEmpty();
    assertThat(result.getRiskScore()).isEqualTo(0);
    assertThat(result.getExecutionTime()).isGreaterThanOrEqualTo(0);
  }

  @Test
  void executeRules_WithMissingReference_ShouldPassWithNoFailures() {
    PaymentInitiatedEvent event = createPaymentEventWithoutReference();
    ValidationContext context = createValidationContext();

    RuleExecutionFacade.RuleExecutionResult result =
        complianceRuleEngine.executeRules(context, event);

    assertThat(result).isNotNull();
    assertThat(result.getRuleType().toString()).isEqualTo("COMPLIANCE");
    assertThat(result.isSuccess()).isTrue();
    assertThat(result.getAppliedRules()).contains("COMPLIANCE_RULE_001");
    assertThat(result.getFailedRules()).isEmpty();
    assertThat(result.getRiskScore()).isEqualTo(0);
  }

  @Test
  void executeRules_WithEmptyReference_ShouldPassWithNoFailures() {
    PaymentInitiatedEvent event = createPaymentEventWithEmptyReference();
    ValidationContext context = createValidationContext();

    RuleExecutionFacade.RuleExecutionResult result =
        complianceRuleEngine.executeRules(context, event);

    assertThat(result).isNotNull();
    assertThat(result.getRuleType().toString()).isEqualTo("COMPLIANCE");
    assertThat(result.isSuccess()).isTrue();
    assertThat(result.getAppliedRules()).contains("COMPLIANCE_RULE_001");
    assertThat(result.getFailedRules()).isEmpty();
    assertThat(result.getRiskScore()).isEqualTo(0);
  }

  @Test
  void executeRules_WithValidReference_ShouldPass() {
    // Given
    PaymentInitiatedEvent event = createValidPaymentEvent();
    ValidationContext context = createValidationContext();

    // When
    RuleExecutionFacade.RuleExecutionResult result =
        complianceRuleEngine.executeRules(context, event);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getRuleType().toString()).isEqualTo(RuleType.COMPLIANCE);
    assertThat(result.isSuccess()).isTrue();
    assertThat(result.getAppliedRules()).contains("COMPLIANCE_RULE_001");
    assertThat(result.getFailedRules()).isEmpty();
    assertThat(result.getRiskScore()).isEqualTo(0);
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
    event.setReference("Test Payment Reference");
    event.setPaymentType(com.payments.contracts.payment.PaymentType.EFT);
    event.setPriority(com.payments.contracts.payment.Priority.NORMAL);
    event.setInitiatedBy("user@example.com");
    event.setInitiatedAt(Instant.now());
    return event;
  }

  private PaymentInitiatedEvent createPaymentEventWithoutReference() {
    PaymentInitiatedEvent event = createValidPaymentEvent();
    event.setReference(null);
    return event;
  }

  private PaymentInitiatedEvent createPaymentEventWithEmptyReference() {
    PaymentInitiatedEvent event = createValidPaymentEvent();
    event.setReference("");
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
