package com.payments.e2e.stepdefinitions;

import com.payments.e2e.config.E2ETestConfiguration;
import com.payments.e2e.data.TestDataBuilder;
import com.payments.e2e.models.PaymentRequest;
import com.payments.e2e.models.ValidationResponse;
import com.payments.e2e.services.ValidationService;
import com.payments.e2e.services.NotificationService;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.awaitility.Awaitility;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

/**
 * Validation Step Definitions for E2E Tests
 * 
 * Implements step definitions for payment validation scenarios including:
 * - Business rules validation
 * - Compliance rules validation
 * - Fraud detection validation
 * - Risk assessment validation
 */
@ContextConfiguration(classes = {E2ETestConfiguration.class})
public class ValidationStepDefinitions {

    @Autowired
    private ValidationService validationService;
    
    @Autowired
    private NotificationService notificationService;
    
    @Autowired
    private TestDataBuilder testDataBuilder;

    private PaymentRequest currentPaymentRequest;
    private ValidationResponse currentValidationResponse;
    private String currentTenantId;

    // Background Steps
    @Given("business rules are loaded")
    public void business_rules_are_loaded() {
        validationService.loadBusinessRules();
        assertThat(validationService.areBusinessRulesLoaded()).isTrue();
    }

    @Given("compliance rules are configured")
    public void compliance_rules_are_configured() {
        validationService.configureComplianceRules();
        assertThat(validationService.areComplianceRulesConfigured()).isTrue();
    }

    // Validation Request Steps
    @Given("a valid payment request is created")
    public void a_valid_payment_request_is_created(io.cucumber.datatable.DataTable dataTable) {
        currentPaymentRequest = testDataBuilder.createPaymentRequest(dataTable);
        currentPaymentRequest.setTenantId(currentTenantId);
    }

    @Given("the daily limit for the account is {string}")
    public void the_daily_limit_for_the_account_is(String dailyLimit) {
        testDataBuilder.setAccountDailyLimit(currentPaymentRequest.getFromAccount(), dailyLimit);
    }

    @Given("the destination account is on sanctions list")
    public void the_destination_account_is_on_sanctions_list() {
        testDataBuilder.addAccountToSanctionsList(currentPaymentRequest.getToAccount());
    }

    @Given("the fraud detection service returns high risk score")
    public void the_fraud_detection_service_returns_high_risk_score() {
        testDataBuilder.configureFraudDetectionHighRisk(currentPaymentRequest);
    }

    @Given("the risk assessment returns high risk")
    public void the_risk_assessment_returns_high_risk() {
        testDataBuilder.configureRiskAssessmentHighRisk(currentPaymentRequest);
    }

    @Given("the payment is within daily limits")
    public void the_payment_is_within_daily_limits() {
        testDataBuilder.configurePaymentWithinDailyLimits(currentPaymentRequest);
    }

    @Given("the accounts are valid")
    public void the_accounts_are_valid() {
        testDataBuilder.configureValidAccounts(currentPaymentRequest);
    }

    @Given("the payment passes compliance checks")
    public void the_payment_passes_compliance_checks() {
        testDataBuilder.configurePaymentPassesCompliance(currentPaymentRequest);
    }

    @Given("the fraud detection returns low risk")
    public void the_fraud_detection_returns_low_risk() {
        testDataBuilder.configureFraudDetectionLowRisk(currentPaymentRequest);
    }

    @Given("the risk assessment returns low risk")
    public void the_risk_assessment_returns_low_risk() {
        testDataBuilder.configureRiskAssessmentLowRisk(currentPaymentRequest);
    }

    @Given("each payment has different validation requirements")
    public void each_payment_has_different_validation_requirements() {
        testDataBuilder.configureMultiplePaymentsWithDifferentValidationRequirements();
    }

    // Validation Submission Steps
    @When("the payment is submitted for validation")
    public void the_payment_is_submitted_for_validation() {
        currentValidationResponse = validationService.validatePayment(currentPaymentRequest);
    }

    @When("all payments are submitted for validation")
    public void all_payments_are_submitted_for_validation() {
        validationService.validateMultiplePayments();
    }

    // Validation Success Assertions
    @Then("the business rules should pass")
    public void the_business_rules_should_pass() {
        await().atMost(30, TimeUnit.SECONDS)
            .until(() -> validationService.getBusinessRulesResult(currentValidationResponse.getValidationId()).equals("PASS"));
        
        String result = validationService.getBusinessRulesResult(currentValidationResponse.getValidationId());
        assertThat(result).isEqualTo("PASS");
    }

    @Then("the compliance rules should pass")
    public void the_compliance_rules_should_pass() {
        await().atMost(30, TimeUnit.SECONDS)
            .until(() -> validationService.getComplianceRulesResult(currentValidationResponse.getValidationId()).equals("PASS"));
        
        String result = validationService.getComplianceRulesResult(currentValidationResponse.getValidationId());
        assertThat(result).isEqualTo("PASS");
    }

    @Then("the fraud detection should pass")
    public void the_fraud_detection_should_pass() {
        await().atMost(30, TimeUnit.SECONDS)
            .until(() -> validationService.getFraudDetectionResult(currentValidationResponse.getValidationId()).equals("PASS"));
        
        String result = validationService.getFraudDetectionResult(currentValidationResponse.getValidationId());
        assertThat(result).isEqualTo("PASS");
    }

    @Then("the risk assessment should be low")
    public void the_risk_assessment_should_be_low() {
        await().atMost(30, TimeUnit.SECONDS)
            .until(() -> validationService.getRiskAssessmentResult(currentValidationResponse.getValidationId()).equals("LOW"));
        
        String result = validationService.getRiskAssessmentResult(currentValidationResponse.getValidationId());
        assertThat(result).isEqualTo("LOW");
    }

    @Then("the payment should be approved for processing")
    public void the_payment_should_be_approved_for_processing() {
        await().atMost(30, TimeUnit.SECONDS)
            .until(() -> validationService.getValidationStatus(currentValidationResponse.getValidationId()).equals("APPROVED"));
        
        String status = validationService.getValidationStatus(currentValidationResponse.getValidationId());
        assertThat(status).isEqualTo("APPROVED");
    }

    @Then("all validation rules should pass")
    public void all_validation_rules_should_pass() {
        await().atMost(30, TimeUnit.SECONDS)
            .until(() -> validationService.allValidationRulesPassed(currentValidationResponse.getValidationId()));
        
        assertThat(validationService.allValidationRulesPassed(currentValidationResponse.getValidationId())).isTrue();
    }

    // Validation Failure Assertions
    @Then("the business rules should fail")
    public void the_business_rules_should_fail() {
        await().atMost(30, TimeUnit.SECONDS)
            .until(() -> validationService.getBusinessRulesResult(currentValidationResponse.getValidationId()).equals("FAIL"));
        
        String result = validationService.getBusinessRulesResult(currentValidationResponse.getValidationId());
        assertThat(result).isEqualTo("FAIL");
    }

    @Then("the account validation should fail")
    public void the_account_validation_should_fail() {
        await().atMost(30, TimeUnit.SECONDS)
            .until(() -> validationService.getAccountValidationResult(currentValidationResponse.getValidationId()).equals("FAIL"));
        
        String result = validationService.getAccountValidationResult(currentValidationResponse.getValidationId());
        assertThat(result).isEqualTo("FAIL");
    }

    @Then("the compliance rules should fail")
    public void the_compliance_rules_should_fail() {
        await().atMost(30, TimeUnit.SECONDS)
            .until(() -> validationService.getComplianceRulesResult(currentValidationResponse.getValidationId()).equals("FAIL"));
        
        String result = validationService.getComplianceRulesResult(currentValidationResponse.getValidationId());
        assertThat(result).isEqualTo("FAIL");
    }

    @Then("the fraud detection should fail")
    public void the_fraud_detection_should_fail() {
        await().atMost(30, TimeUnit.SECONDS)
            .until(() -> validationService.getFraudDetectionResult(currentValidationResponse.getValidationId()).equals("FAIL"));
        
        String result = validationService.getFraudDetectionResult(currentValidationResponse.getValidationId());
        assertThat(result).isEqualTo("FAIL");
    }

    @Then("the risk assessment should fail")
    public void the_risk_assessment_should_fail() {
        await().atMost(30, TimeUnit.SECONDS)
            .until(() -> validationService.getRiskAssessmentResult(currentValidationResponse.getValidationId()).equals("FAIL"));
        
        String result = validationService.getRiskAssessmentResult(currentValidationResponse.getValidationId());
        assertThat(result).isEqualTo("FAIL");
    }

    // Validation Result Assertions
    @Then("the validation should return {string}")
    public void the_validation_should_return(String expectedResult) {
        await().atMost(30, TimeUnit.SECONDS)
            .until(() -> validationService.getValidationResult(currentValidationResponse.getValidationId()).equals(expectedResult));
        
        String result = validationService.getValidationResult(currentValidationResponse.getValidationId());
        assertThat(result).isEqualTo(expectedResult);
    }

    @Then("the payment should be rejected")
    public void the_payment_should_be_rejected() {
        await().atMost(30, TimeUnit.SECONDS)
            .until(() -> validationService.getValidationStatus(currentValidationResponse.getValidationId()).equals("REJECTED"));
        
        String status = validationService.getValidationStatus(currentValidationResponse.getValidationId());
        assertThat(status).isEqualTo("REJECTED");
    }

    // Notification Assertions
    @Then("a validation failure notification should be sent")
    public void a_validation_failure_notification_should_be_sent() {
        await().atMost(30, TimeUnit.SECONDS)
            .until(() -> notificationService.hasValidationFailureNotification(currentValidationResponse.getValidationId()));
        
        assertThat(notificationService.hasValidationFailureNotification(currentValidationResponse.getValidationId())).isTrue();
    }

    @Then("a compliance violation notification should be sent")
    public void a_compliance_violation_notification_should_be_sent() {
        await().atMost(30, TimeUnit.SECONDS)
            .until(() -> notificationService.hasComplianceViolationNotification(currentValidationResponse.getValidationId()));
        
        assertThat(notificationService.hasComplianceViolationNotification(currentValidationResponse.getValidationId())).isTrue();
    }

    @Then("a fraud detection notification should be sent")
    public void a_fraud_detection_notification_should_be_sent() {
        await().atMost(30, TimeUnit.SECONDS)
            .until(() -> notificationService.hasFraudDetectionNotification(currentValidationResponse.getValidationId()));
        
        assertThat(notificationService.hasFraudDetectionNotification(currentValidationResponse.getValidationId())).isTrue();
    }

    @Then("a risk assessment notification should be sent")
    public void a_risk_assessment_notification_should_be_sent() {
        await().atMost(30, TimeUnit.SECONDS)
            .until(() -> notificationService.hasRiskAssessmentNotification(currentValidationResponse.getValidationId()));
        
        assertThat(notificationService.hasRiskAssessmentNotification(currentValidationResponse.getValidationId())).isTrue();
    }

    @Then("a validation success notification should be sent")
    public void a_validation_success_notification_should_be_sent() {
        await().atMost(30, TimeUnit.SECONDS)
            .until(() -> notificationService.hasValidationSuccessNotification(currentValidationResponse.getValidationId()));
        
        assertThat(notificationService.hasValidationSuccessNotification(currentValidationResponse.getValidationId())).isTrue();
    }

    // Performance Assertions
    @Then("all payments should be validated within {string} seconds")
    public void all_payments_should_be_validated_within_seconds(String maxSeconds) {
        int maxSecondsInt = Integer.parseInt(maxSeconds);
        await().atMost(maxSecondsInt, TimeUnit.SECONDS)
            .until(() -> validationService.allPaymentsValidated());
        
        assertThat(validationService.allPaymentsValidated()).isTrue();
    }

    @Then("the validation success rate should be {string} percent")
    public void the_validation_success_rate_should_be_percent(String expectedRate) {
        double successRate = validationService.getValidationSuccessRate();
        assertThat(successRate).isGreaterThanOrEqualTo(Double.parseDouble(expectedRate));
    }

    @Then("the average validation time should be less than {string} milliseconds")
    public void the_average_validation_time_should_be_less_than_milliseconds(String maxMilliseconds) {
        double avgValidationTime = validationService.getAverageValidationTime();
        assertThat(avgValidationTime).isLessThan(Double.parseDouble(maxMilliseconds));
    }

    @Then("no payment should be lost during validation")
    public void no_payment_should_be_lost_during_validation() {
        assertThat(validationService.noPaymentsLostDuringValidation()).isTrue();
    }
}
