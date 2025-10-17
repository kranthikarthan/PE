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

/** Unit tests for RiskAssessmentRuleEngine */
@ExtendWith(MockitoExtension.class)
class RiskAssessmentRuleEngineTest {

  private RiskAssessmentRuleEngine riskAssessmentRuleEngine;

  @BeforeEach
  void setUp() {
    riskAssessmentRuleEngine = new RiskAssessmentRuleEngine();
  }

  @Test
  void executeRules_WithValidPayment_ShouldPass() {
    // Given
    PaymentInitiatedEvent event = createValidPaymentEvent();
    ValidationContext context = createValidationContext();

    // When
    RuleExecutionFacade.RuleExecutionResult result =
        riskAssessmentRuleEngine.executeRules(context, event);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getRuleType()).isEqualTo(RuleType.RISK);
    assertThat(result.isSuccess()).isTrue();
    assertThat(result.getAppliedRules())
        .contains(
            "RISK_RULE_001", "RISK_RULE_002", "RISK_RULE_003", "RISK_RULE_004", "RISK_RULE_005");
    assertThat(result.getFailedRules()).isEmpty();
    assertThat(result.getFraudScore()).isEqualTo(0);
    assertThat(result.getRiskScore()).isEqualTo(0);
    assertThat(result.getExecutionTime()).isGreaterThan(0);
  }

  @Test
  void executeRules_WithHighCreditRisk_ShouldDetectRisk() {
    // Given
    PaymentInitiatedEvent event = createHighCreditRiskPaymentEvent();
    ValidationContext context = createValidationContext();

    // When
    RuleExecutionFacade.RuleExecutionResult result =
        riskAssessmentRuleEngine.executeRules(context, event);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getRuleType()).isEqualTo(RuleType.RISK);
    assertThat(result.isSuccess()).isFalse();
    assertThat(result.getAppliedRules()).contains("RISK_RULE_001");
    assertThat(result.getFailedRules()).hasSize(1);
    assertThat(result.getFailedRules().get(0).getRuleId()).isEqualTo("RISK_RULE_001");
    assertThat(result.getFailedRules().get(0).getRuleName()).isEqualTo("Credit Risk Assessment");
    assertThat(result.getFailedRules().get(0).getRuleType()).isEqualTo(RuleType.RISK);
    assertThat(result.getFailedRules().get(0).getFailureReason())
        .contains("High credit risk transaction");
    assertThat(result.getRiskScore()).isEqualTo(30);
  }

  @Test
  void executeRules_WithForeignCurrency_ShouldDetectRisk() {
    // Given
    PaymentInitiatedEvent event = createForeignCurrencyPaymentEvent();
    ValidationContext context = createValidationContext();

    // When
    RuleExecutionFacade.RuleExecutionResult result =
        riskAssessmentRuleEngine.executeRules(context, event);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getRuleType()).isEqualTo(RuleType.RISK);
    assertThat(result.isSuccess()).isFalse();
    assertThat(result.getAppliedRules()).contains("RISK_RULE_002");
    assertThat(result.getFailedRules()).hasSize(1);
    assertThat(result.getFailedRules().get(0).getRuleId()).isEqualTo("RISK_RULE_002");
    assertThat(result.getFailedRules().get(0).getRuleName()).isEqualTo("Market Risk Analysis");
    assertThat(result.getFailedRules().get(0).getRuleType()).isEqualTo(RuleType.RISK);
    assertThat(result.getFailedRules().get(0).getFailureReason())
        .contains("Foreign currency transaction");
    assertThat(result.getRiskScore()).isEqualTo(25);
  }

  @Test
  void executeRules_WithHighOperationalRisk_ShouldDetectRisk() {
    // Given
    PaymentInitiatedEvent event = createHighOperationalRiskPaymentEvent();
    ValidationContext context = createValidationContext();

    // When
    RuleExecutionFacade.RuleExecutionResult result =
        riskAssessmentRuleEngine.executeRules(context, event);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getRuleType()).isEqualTo(RuleType.RISK);
    assertThat(result.isSuccess()).isFalse();
    assertThat(result.getAppliedRules()).contains("RISK_RULE_003");
    assertThat(result.getFailedRules()).hasSize(1);
    assertThat(result.getFailedRules().get(0).getRuleId()).isEqualTo("RISK_RULE_003");
    assertThat(result.getFailedRules().get(0).getRuleName())
        .isEqualTo("Operational Risk Evaluation");
    assertThat(result.getFailedRules().get(0).getRuleType()).isEqualTo(RuleType.RISK);
    assertThat(result.getFailedRules().get(0).getFailureReason())
        .contains("High-value transaction");
    assertThat(result.getRiskScore()).isEqualTo(35);
  }

  @Test
  void executeRules_WithHighLiquidityRisk_ShouldDetectRisk() {
    // Given
    PaymentInitiatedEvent event = createHighLiquidityRiskPaymentEvent();
    ValidationContext context = createValidationContext();

    // When
    RuleExecutionFacade.RuleExecutionResult result =
        riskAssessmentRuleEngine.executeRules(context, event);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getRuleType()).isEqualTo(RuleType.RISK);
    assertThat(result.isSuccess()).isFalse();
    assertThat(result.getAppliedRules()).contains("RISK_RULE_004");
    assertThat(result.getFailedRules()).hasSize(1);
    assertThat(result.getFailedRules().get(0).getRuleId()).isEqualTo("RISK_RULE_004");
    assertThat(result.getFailedRules().get(0).getRuleName()).isEqualTo("Liquidity Risk Assessment");
    assertThat(result.getFailedRules().get(0).getRuleType()).isEqualTo(RuleType.RISK);
    assertThat(result.getFailedRules().get(0).getFailureReason()).contains("Large transaction");
    assertThat(result.getRiskScore()).isEqualTo(20);
  }

  @Test
  void executeRules_WithHighCounterpartyRisk_ShouldDetectRisk() {
    // Given
    PaymentInitiatedEvent event = createHighCounterpartyRiskPaymentEvent();
    ValidationContext context = createValidationContext();

    // When
    RuleExecutionFacade.RuleExecutionResult result =
        riskAssessmentRuleEngine.executeRules(context, event);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getRuleType()).isEqualTo(RuleType.RISK);
    assertThat(result.isSuccess()).isFalse();
    assertThat(result.getAppliedRules()).contains("RISK_RULE_005");
    assertThat(result.getFailedRules()).hasSize(1);
    assertThat(result.getFailedRules().get(0).getRuleId()).isEqualTo("RISK_RULE_005");
    assertThat(result.getFailedRules().get(0).getRuleName())
        .isEqualTo("Counterparty Risk Analysis");
    assertThat(result.getFailedRules().get(0).getRuleType()).isEqualTo(RuleType.RISK);
    assertThat(result.getFailedRules().get(0).getFailureReason())
        .contains("High-risk counterparty");
    assertThat(result.getRiskScore()).isEqualTo(40);
  }

  @Test
  void executeRules_WithMultipleRisks_ShouldDetectMultipleRisks() {
    // Given
    PaymentInitiatedEvent event = createMultipleRiskPaymentEvent();
    ValidationContext context = createValidationContext();

    // When
    RuleExecutionFacade.RuleExecutionResult result =
        riskAssessmentRuleEngine.executeRules(context, event);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getRuleType()).isEqualTo(RuleType.RISK);
    assertThat(result.isSuccess()).isFalse();
    assertThat(result.getAppliedRules()).hasSize(5);
    assertThat(result.getFailedRules()).hasSize(2); // Credit risk + operational risk
    assertThat(result.getRiskScore()).isEqualTo(65); // 30 + 35
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

  private PaymentInitiatedEvent createHighCreditRiskPaymentEvent() {
    PaymentInitiatedEvent event = createValidPaymentEvent();
    event.setAmount(Money.of(new BigDecimal("250000.00"), Currency.getInstance("ZAR")));
    return event;
  }

  private PaymentInitiatedEvent createForeignCurrencyPaymentEvent() {
    PaymentInitiatedEvent event = createValidPaymentEvent();
    event.setAmount(Money.of(new BigDecimal("1000.00"), Currency.getInstance("USD")));
    return event;
  }

  private PaymentInitiatedEvent createHighOperationalRiskPaymentEvent() {
    PaymentInitiatedEvent event = createValidPaymentEvent();
    event.setAmount(Money.of(new BigDecimal("1200000.00"), Currency.getInstance("ZAR")));
    return event;
  }

  private PaymentInitiatedEvent createHighLiquidityRiskPaymentEvent() {
    PaymentInitiatedEvent event = createValidPaymentEvent();
    event.setAmount(Money.of(new BigDecimal("600000.00"), Currency.getInstance("ZAR")));
    return event;
  }

  private PaymentInitiatedEvent createHighCounterpartyRiskPaymentEvent() {
    PaymentInitiatedEvent event = createValidPaymentEvent();
    event.setDestinationAccount("RISK1234567");
    return event;
  }

  private PaymentInitiatedEvent createMultipleRiskPaymentEvent() {
    PaymentInitiatedEvent event = createHighOperationalRiskPaymentEvent();
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
