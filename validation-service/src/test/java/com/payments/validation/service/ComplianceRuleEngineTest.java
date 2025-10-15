package com.payments.validation.service;

import com.payments.contracts.events.PaymentInitiatedEvent;
import com.payments.contracts.payment.PaymentId;
import com.payments.contracts.payment.Money;
import com.payments.contracts.payment.TenantContext;
import com.payments.domain.validation.RuleType;
import com.payments.validation.service.RuleExecutionFacade.ValidationContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for ComplianceRuleEngine
 */
@ExtendWith(MockitoExtension.class)
class ComplianceRuleEngineTest {

    private ComplianceRuleEngine complianceRuleEngine;

    @BeforeEach
    void setUp() {
        complianceRuleEngine = new ComplianceRuleEngine();
    }

    @Test
    void executeRules_WithValidPayment_ShouldPass() {
        // Given
        PaymentInitiatedEvent event = createValidPaymentEvent();
        ValidationContext context = createValidationContext();

        // When
        RuleExecutionFacade.RuleExecutionResult result = complianceRuleEngine.executeRules(context, event);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getRuleType()).isEqualTo(RuleType.COMPLIANCE);
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getAppliedRules()).contains("COMPLIANCE_RULE_001", "COMPLIANCE_RULE_002", "COMPLIANCE_RULE_003", "COMPLIANCE_RULE_004", "COMPLIANCE_RULE_005");
        assertThat(result.getFailedRules()).isEmpty();
        assertThat(result.getFraudScore()).isEqualTo(0);
        assertThat(result.getRiskScore()).isEqualTo(0);
        assertThat(result.getExecutionTime()).isGreaterThan(0);
    }

    @Test
    void executeRules_WithMissingReference_ShouldFail() {
        // Given
        PaymentInitiatedEvent event = createPaymentEventWithoutReference();
        ValidationContext context = createValidationContext();

        // When
        RuleExecutionFacade.RuleExecutionResult result = complianceRuleEngine.executeRules(context, event);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getRuleType()).isEqualTo(RuleType.COMPLIANCE);
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getAppliedRules()).contains("COMPLIANCE_RULE_001");
        assertThat(result.getFailedRules()).hasSize(1);
        assertThat(result.getFailedRules().get(0).getRuleId()).isEqualTo("COMPLIANCE_RULE_001");
        assertThat(result.getFailedRules().get(0).getRuleName()).isEqualTo("Payment Reference Check");
        assertThat(result.getFailedRules().get(0).getRuleType()).isEqualTo(RuleType.COMPLIANCE);
        assertThat(result.getFailedRules().get(0).getFailureReason()).contains("required for compliance reporting");
        assertThat(result.getRiskScore()).isEqualTo(15);
    }

    @Test
    void executeRules_WithEmptyReference_ShouldFail() {
        // Given
        PaymentInitiatedEvent event = createPaymentEventWithEmptyReference();
        ValidationContext context = createValidationContext();

        // When
        RuleExecutionFacade.RuleExecutionResult result = complianceRuleEngine.executeRules(context, event);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getRuleType()).isEqualTo(RuleType.COMPLIANCE);
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getAppliedRules()).contains("COMPLIANCE_RULE_001");
        assertThat(result.getFailedRules()).hasSize(1);
        assertThat(result.getFailedRules().get(0).getRuleId()).isEqualTo("COMPLIANCE_RULE_001");
        assertThat(result.getFailedRules().get(0).getRuleName()).isEqualTo("Payment Reference Check");
        assertThat(result.getFailedRules().get(0).getRuleType()).isEqualTo(RuleType.COMPLIANCE);
        assertThat(result.getFailedRules().get(0).getFailureReason()).contains("required for compliance reporting");
        assertThat(result.getRiskScore()).isEqualTo(15);
    }

    @Test
    void executeRules_WithValidReference_ShouldPass() {
        // Given
        PaymentInitiatedEvent event = createValidPaymentEvent();
        ValidationContext context = createValidationContext();

        // When
        RuleExecutionFacade.RuleExecutionResult result = complianceRuleEngine.executeRules(context, event);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getRuleType()).isEqualTo(RuleType.COMPLIANCE);
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getAppliedRules()).contains("COMPLIANCE_RULE_001");
        assertThat(result.getFailedRules()).isEmpty();
        assertThat(result.getRiskScore()).isEqualTo(0);
    }

    @Test
    void executeRules_WithException_ShouldHandleGracefully() {
        // Given
        PaymentInitiatedEvent event = null; // This will cause an exception
        ValidationContext context = createValidationContext();

        // When
        RuleExecutionFacade.RuleExecutionResult result = complianceRuleEngine.executeRules(context, event);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getRuleType()).isEqualTo(RuleType.COMPLIANCE);
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
                .reference("Test Payment Reference")
                .build();
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
                .paymentId(com.payments.domain.payment.PaymentId.builder().value("payment-123").build())
                .tenantContext(com.payments.domain.shared.TenantContext.builder()
                        .tenantId("tenant-1")
                        .businessUnitId("business-unit-1")
                        .build())
                .correlationId("test-correlation-id")
                .validationId("validation-123")
                .startedAt(Instant.now())
                .build();
    }
}
