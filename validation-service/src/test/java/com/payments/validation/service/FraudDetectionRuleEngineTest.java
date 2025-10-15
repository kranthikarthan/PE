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
 * Unit tests for FraudDetectionRuleEngine
 */
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
        RuleExecutionFacade.RuleExecutionResult result = fraudDetectionRuleEngine.executeRules(context, event);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getRuleType()).isEqualTo(RuleType.FRAUD);
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getAppliedRules()).contains("FRAUD_RULE_001", "FRAUD_RULE_002", "FRAUD_RULE_003", "FRAUD_RULE_004", "FRAUD_RULE_005");
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
        RuleExecutionFacade.RuleExecutionResult result = fraudDetectionRuleEngine.executeRules(context, event);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getRuleType()).isEqualTo(RuleType.FRAUD);
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
        RuleExecutionFacade.RuleExecutionResult result = fraudDetectionRuleEngine.executeRules(context, event);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getRuleType()).isEqualTo(RuleType.FRAUD);
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
        RuleExecutionFacade.RuleExecutionResult result = fraudDetectionRuleEngine.executeRules(context, event);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getRuleType()).isEqualTo(RuleType.FRAUD);
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getAppliedRules()).contains("FRAUD_RULE_003");
        assertThat(result.getFailedRules()).hasSize(1);
        assertThat(result.getFailedRules().get(0).getRuleId()).isEqualTo("FRAUD_RULE_003");
        assertThat(result.getFailedRules().get(0).getRuleName()).isEqualTo("Account Pattern Analysis");
        assertThat(result.getFailedRules().get(0).getRuleType()).isEqualTo(RuleType.FRAUD);
        assertThat(result.getFailedRules().get(0).getFailureReason()).contains("Suspicious account pattern");
        assertThat(result.getFraudScore()).isEqualTo(20);
        assertThat(result.getRiskScore()).isEqualTo(20);
    }

    @Test
    void executeRules_WithException_ShouldHandleGracefully() {
        // Given
        PaymentInitiatedEvent event = null; // This will cause an exception
        ValidationContext context = createValidationContext();

        // When
        RuleExecutionFacade.RuleExecutionResult result = fraudDetectionRuleEngine.executeRules(context, event);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getRuleType()).isEqualTo(RuleType.FRAUD);
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getErrorMessage()).isNotNull();
        assertThat(result.getFraudScore()).isEqualTo(100);
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
                .reference("Test Payment")
                .build();
    }

    private PaymentInitiatedEvent createHighAmountPaymentEvent() {
        PaymentInitiatedEvent event = createValidPaymentEvent();
        event.setAmount(Money.builder()
                .amount(new BigDecimal("60000.00"))
                .currency("ZAR")
                .build());
        return event;
    }

    private PaymentInitiatedEvent createVeryHighAmountPaymentEvent() {
        PaymentInitiatedEvent event = createValidPaymentEvent();
        event.setAmount(Money.builder()
                .amount(new BigDecimal("80000.00"))
                .currency("ZAR")
                .build());
        return event;
    }

    private PaymentInitiatedEvent createSuspiciousAccountPaymentEvent() {
        PaymentInitiatedEvent event = createValidPaymentEvent();
        event.setSourceAccount("9991234567"); // Contains "999" which triggers suspicious pattern
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
