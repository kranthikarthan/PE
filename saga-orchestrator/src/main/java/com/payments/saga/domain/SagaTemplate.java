package com.payments.saga.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Template for defining saga workflows
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SagaTemplate {
    private String templateName;
    private String description;
    private List<SagaStepDefinition> stepDefinitions;
    private Map<String, Object> defaultSagaData;
    private int version;

    public static SagaTemplate createPaymentProcessingTemplate() {
        List<SagaStepDefinition> steps = new ArrayList<>();
        
        // Step 1: Payment Validation
        steps.add(SagaStepDefinition.create(
            "Validate Payment",
            SagaStepType.VALIDATION,
            "validation-service",
            "/api/v1/validate",
            "/api/v1/validate/compensate"
        ));
        
        // Step 2: Payment Routing
        steps.add(SagaStepDefinition.create(
            "Route Payment",
            SagaStepType.ROUTING,
            "routing-service",
            "/api/v1/route",
            "/api/v1/route/compensate"
        ));
        
        // Step 3: Account Adapter
        steps.add(SagaStepDefinition.create(
            "Account Operations",
            SagaStepType.ACCOUNT_ADAPTER,
            "account-adapter-service",
            "/api/v1/account/operations",
            "/api/v1/account/operations/compensate"
        ));
        
        // Step 4: Transaction Processing
        steps.add(SagaStepDefinition.create(
            "Process Transaction",
            SagaStepType.TRANSACTION_PROCESSING,
            "transaction-processing-service",
            "/api/v1/transactions",
            "/api/v1/transactions/compensate"
        ));
        
        // Step 5: Notification
        steps.add(SagaStepDefinition.create(
            "Send Notification",
            SagaStepType.NOTIFICATION,
            "notification-service",
            "/api/v1/notify",
            "/api/v1/notify/compensate"
        ));

        return SagaTemplate.builder()
                .templateName("PaymentProcessingSaga")
                .description("Standard payment processing workflow")
                .stepDefinitions(steps)
                .version(1)
                .build();
    }

    public static SagaTemplate createFastPaymentTemplate() {
        List<SagaStepDefinition> steps = new ArrayList<>();
        
        // Step 1: Quick Validation
        steps.add(SagaStepDefinition.create(
            "Quick Validation",
            SagaStepType.VALIDATION,
            "validation-service",
            "/api/v1/validate/quick",
            "/api/v1/validate/quick/compensate"
        ));
        
        // Step 2: Direct Routing
        steps.add(SagaStepDefinition.create(
            "Direct Route",
            SagaStepType.ROUTING,
            "routing-service",
            "/api/v1/route/direct",
            "/api/v1/route/direct/compensate"
        ));
        
        // Step 3: Fast Transaction Processing
        steps.add(SagaStepDefinition.create(
            "Fast Transaction",
            SagaStepType.TRANSACTION_PROCESSING,
            "transaction-processing-service",
            "/api/v1/transactions/fast",
            "/api/v1/transactions/fast/compensate"
        ));

        return SagaTemplate.builder()
                .templateName("FastPaymentSaga")
                .description("Fast payment processing workflow")
                .stepDefinitions(steps)
                .version(1)
                .build();
    }

    public static SagaTemplate createHighValuePaymentTemplate() {
        List<SagaStepDefinition> steps = new ArrayList<>();
        
        // Step 1: Enhanced Validation
        steps.add(SagaStepDefinition.create(
            "Enhanced Validation",
            SagaStepType.VALIDATION,
            "validation-service",
            "/api/v1/validate/enhanced",
            "/api/v1/validate/enhanced/compensate"
        ));
        
        // Step 2: Manual Review (Optional)
        steps.add(SagaStepDefinition.builder()
            .stepName("Manual Review")
            .stepType(SagaStepType.VALIDATION)
            .serviceName("manual-review-service")
            .endpoint("/api/v1/review")
            .compensationEndpoint("/api/v1/review/compensate")
            .maxRetries(1)
            .timeoutSeconds(300)
            .isOptional(true)
            .isCompensationStep(false)
            .build());
        
        // Step 3: Routing with Approval
        steps.add(SagaStepDefinition.create(
            "Route with Approval",
            SagaStepType.ROUTING,
            "routing-service",
            "/api/v1/route/approved",
            "/api/v1/route/approved/compensate"
        ));
        
        // Step 4: Account Operations
        steps.add(SagaStepDefinition.create(
            "Account Operations",
            SagaStepType.ACCOUNT_ADAPTER,
            "account-adapter-service",
            "/api/v1/account/operations",
            "/api/v1/account/operations/compensate"
        ));
        
        // Step 5: Transaction Processing
        steps.add(SagaStepDefinition.create(
            "Process Transaction",
            SagaStepType.TRANSACTION_PROCESSING,
            "transaction-processing-service",
            "/api/v1/transactions",
            "/api/v1/transactions/compensate"
        ));
        
        // Step 6: Compliance Notification
        steps.add(SagaStepDefinition.create(
            "Compliance Notification",
            SagaStepType.NOTIFICATION,
            "notification-service",
            "/api/v1/notify/compliance",
            "/api/v1/notify/compliance/compensate"
        ));

        return SagaTemplate.builder()
                .templateName("HighValuePaymentSaga")
                .description("High-value payment processing workflow with enhanced validation")
                .stepDefinitions(steps)
                .version(1)
                .build();
    }

    public List<SagaStepDefinition> getBusinessSteps() {
        return stepDefinitions.stream()
                .filter(SagaStepDefinition::isBusinessStep)
                .toList();
    }

    public List<SagaStepDefinition> getCompensationSteps() {
        return stepDefinitions.stream()
                .filter(SagaStepDefinition::isCompensationStep)
                .toList();
    }

    public SagaStepDefinition getStepByName(String stepName) {
        return stepDefinitions.stream()
                .filter(step -> step.getStepName().equals(stepName))
                .findFirst()
                .orElse(null);
    }

    public int getTotalSteps() {
        return stepDefinitions.size();
    }

    public int getBusinessStepsCount() {
        return getBusinessSteps().size();
    }
}






