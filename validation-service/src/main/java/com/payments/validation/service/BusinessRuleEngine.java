package com.payments.validation.service;

import com.payments.contracts.events.PaymentInitiatedEvent;
import com.payments.domain.validation.FailedRule;
import com.payments.domain.validation.RuleType;
import com.payments.validation.service.RuleExecutionFacade.ValidationContext;
import com.payments.validation.service.RuleExecutionFacade.RuleExecutionResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Business Rule Engine
 * 
 * Executes business validation rules:
 * - Amount limits and thresholds
 * - Account validation rules
 * - Business logic constraints
 * - Operational rules
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BusinessRuleEngine {

    /**
     * Execute business rules
     * 
     * @param context Validation context
     * @param event Payment initiated event
     * @return Rule execution result
     */
    public RuleExecutionResult executeRules(ValidationContext context, PaymentInitiatedEvent event) {
        log.debug("Executing business rules for payment: {}", event.getPaymentId().getValue());
        
        long startTime = System.currentTimeMillis();
        List<String> appliedRules = new ArrayList<>();
        List<FailedRule> failedRules = new ArrayList<>();
        
        try {
            // Rule 1: Amount limit check
            executeAmountLimitRule(event, appliedRules, failedRules);
            
            // Rule 2: Account validation
            executeAccountValidationRule(event, appliedRules, failedRules);
            
            // Rule 3: Business hours check
            executeBusinessHoursRule(event, appliedRules, failedRules);
            
            // Rule 4: Currency validation
            executeCurrencyValidationRule(event, appliedRules, failedRules);
            
            // Rule 5: Payment type validation
            executePaymentTypeValidationRule(event, appliedRules, failedRules);
            
            long executionTime = System.currentTimeMillis() - startTime;
            
            return RuleExecutionResult.builder()
                    .ruleType(RuleType.BUSINESS)
                    .success(failedRules.isEmpty())
                    .appliedRules(appliedRules)
                    .failedRules(failedRules)
                    .fraudScore(0)
                    .riskScore(calculateRiskScore(failedRules))
                    .executionTime(executionTime)
                    .build();
            
        } catch (Exception e) {
            log.error("Error executing business rules for payment: {}", 
                    event.getPaymentId().getValue(), e);
            
            long executionTime = System.currentTimeMillis() - startTime;
            return RuleExecutionResult.builder()
                    .ruleType(RuleType.BUSINESS)
                    .success(false)
                    .appliedRules(appliedRules)
                    .failedRules(failedRules)
                    .fraudScore(0)
                    .riskScore(100)
                    .executionTime(executionTime)
                    .errorMessage(e.getMessage())
                    .build();
        }
    }

    /**
     * Execute amount limit rule
     */
    private void executeAmountLimitRule(PaymentInitiatedEvent event, List<String> appliedRules, List<FailedRule> failedRules) {
        appliedRules.add("BUSINESS_RULE_001");
        
        double amount = event.getAmount().getAmount().doubleValue();
        double maxAmount = 100000.0; // TODO: Get from configuration
        
        if (amount > maxAmount) {
            failedRules.add(FailedRule.builder()
                    .ruleId("BUSINESS_RULE_001")
                    .ruleName("Amount Limit Check")
                    .ruleType(RuleType.BUSINESS)
                    .failureReason(String.format("Payment amount %.2f exceeds maximum limit %.2f", amount, maxAmount))
                    .failedAt(Instant.now())
                    .build());
        }
    }

    /**
     * Execute account validation rule
     */
    private void executeAccountValidationRule(PaymentInitiatedEvent event, List<String> appliedRules, List<FailedRule> failedRules) {
        appliedRules.add("BUSINESS_RULE_002");
        
        // Check if source and destination accounts are different
        if (event.getSourceAccount().equals(event.getDestinationAccount())) {
            failedRules.add(FailedRule.builder()
                    .ruleId("BUSINESS_RULE_002")
                    .ruleName("Account Validation")
                    .ruleType(RuleType.BUSINESS)
                    .failureReason("Source and destination accounts cannot be the same")
                    .failedAt(Instant.now())
                    .build());
        }
    }

    /**
     * Execute business hours rule
     */
    private void executeBusinessHoursRule(PaymentInitiatedEvent event, List<String> appliedRules, List<FailedRule> failedRules) {
        appliedRules.add("BUSINESS_RULE_003");
        
        // TODO: Implement actual business hours check
        // For now, just log that the rule was applied
        log.debug("Business hours rule applied for payment: {}", event.getPaymentId().getValue());
    }

    /**
     * Execute currency validation rule
     */
    private void executeCurrencyValidationRule(PaymentInitiatedEvent event, List<String> appliedRules, List<FailedRule> failedRules) {
        appliedRules.add("BUSINESS_RULE_004");
        
        String currency = event.getAmount().getCurrency();
        if (currency == null || currency.trim().isEmpty()) {
            failedRules.add(FailedRule.builder()
                    .ruleId("BUSINESS_RULE_004")
                    .ruleName("Currency Validation")
                    .ruleType(RuleType.BUSINESS)
                    .failureReason("Payment currency is required")
                    .failedAt(Instant.now())
                    .build());
        }
    }

    /**
     * Execute payment type validation rule
     */
    private void executePaymentTypeValidationRule(PaymentInitiatedEvent event, List<String> appliedRules, List<FailedRule> failedRules) {
        appliedRules.add("BUSINESS_RULE_005");
        
        // TODO: Implement payment type validation
        // For now, just log that the rule was applied
        log.debug("Payment type validation rule applied for payment: {}", event.getPaymentId().getValue());
    }

    /**
     * Calculate risk score based on failed rules
     */
    private int calculateRiskScore(List<FailedRule> failedRules) {
        return failedRules.size() * 10; // Each failed business rule adds 10 points
    }
}
