package com.payments.e2e.stepdefinitions;

import com.payments.e2e.config.E2ETestConfiguration;
import com.payments.e2e.data.TestDataBuilder;
import com.payments.e2e.models.PaymentRequest;
import com.payments.e2e.models.PaymentResponse;
import com.payments.e2e.services.PaymentService;
import com.payments.e2e.services.AccountService;
import com.payments.e2e.services.NotificationService;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import io.restassured.response.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.awaitility.Awaitility;
import org.awaitility.Duration;

import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

/**
 * Payment Step Definitions for E2E Tests
 * 
 * Implements step definitions for payment-related scenarios including:
 * - Payment initiation
 * - Payment validation
 * - Payment processing
 * - Payment clearing
 * - Payment settlement
 */
@ContextConfiguration(classes = {E2ETestConfiguration.class})
public class PaymentStepDefinitions {

    @Autowired
    private PaymentService paymentService;
    
    @Autowired
    private AccountService accountService;
    
    @Autowired
    private NotificationService notificationService;
    
    @Autowired
    private TestDataBuilder testDataBuilder;

    private PaymentRequest currentPaymentRequest;
    private PaymentResponse currentPaymentResponse;
    private String currentTenantId;
    private String currentAccountId;

    // Background Steps
    @Given("the payment system is running")
    public void the_payment_system_is_running() {
        // Verify all services are healthy
        assertThat(paymentService.isHealthy()).isTrue();
        assertThat(accountService.isHealthy()).isTrue();
        assertThat(notificationService.isHealthy()).isTrue();
    }

    @Given("test tenant {string} is configured")
    public void test_tenant_is_configured(String tenantId) {
        this.currentTenantId = tenantId;
        testDataBuilder.setupTenant(tenantId);
    }

    @Given("test account {string} has balance {string}")
    public void test_account_has_balance(String accountId, String balance) {
        this.currentAccountId = accountId;
        BigDecimal accountBalance = new BigDecimal(balance);
        testDataBuilder.setupAccount(accountId, accountBalance);
    }

    @Given("external systems are mocked")
    public void external_systems_are_mocked() {
        testDataBuilder.setupExternalSystemMocks();
    }

    // Payment Initiation Steps
    @Given("a valid EFT payment request is created")
    public void a_valid_eft_payment_request_is_created(io.cucumber.datatable.DataTable dataTable) {
        currentPaymentRequest = testDataBuilder.createPaymentRequest(dataTable);
        currentPaymentRequest.setPaymentType("EFT");
        currentPaymentRequest.setTenantId(currentTenantId);
    }

    @Given("a valid payment request is created")
    public void a_valid_payment_request_is_created(io.cucumber.datatable.DataTable dataTable) {
        currentPaymentRequest = testDataBuilder.createPaymentRequest(dataTable);
        currentPaymentRequest.setTenantId(currentTenantId);
    }

    @When("the payment is submitted")
    public void the_payment_is_submitted() {
        currentPaymentResponse = paymentService.submitPayment(currentPaymentRequest);
    }

    @When("the payment is submitted for validation")
    public void the_payment_is_submitted_for_validation() {
        currentPaymentResponse = paymentService.submitPaymentForValidation(currentPaymentRequest);
    }

    @When("the payment is submitted for processing")
    public void the_payment_is_submitted_for_processing() {
        currentPaymentResponse = paymentService.submitPaymentForProcessing(currentPaymentRequest);
    }

    @When("the payment is submitted to SAMOS")
    public void the_payment_is_submitted_to_samos() {
        currentPaymentResponse = paymentService.submitPaymentToClearing(currentPaymentRequest, "SAMOS");
    }

    @When("the payment is submitted to RTC")
    public void the_payment_is_submitted_to_rtc() {
        currentPaymentResponse = paymentService.submitPaymentToClearing(currentPaymentRequest, "RTC");
    }

    @When("the payment is submitted to PayShap")
    public void the_payment_is_submitted_to_payshap() {
        currentPaymentResponse = paymentService.submitPaymentToClearing(currentPaymentRequest, "PayShap");
    }

    @When("the payment is submitted to SWIFT")
    public void the_payment_is_submitted_to_swift() {
        currentPaymentResponse = paymentService.submitPaymentToClearing(currentPaymentRequest, "SWIFT");
    }

    @When("the payment is submitted for settlement")
    public void the_payment_is_submitted_for_settlement() {
        currentPaymentResponse = paymentService.submitPaymentForSettlement(currentPaymentRequest);
    }

    // Payment Status Assertions
    @Then("the payment should be initiated successfully")
    public void the_payment_should_be_initiated_successfully() {
        assertThat(currentPaymentResponse.getStatus()).isEqualTo("INITIATED");
        assertThat(currentPaymentResponse.getPaymentId()).isNotNull();
    }

    @Then("the payment should be validated successfully")
    public void the_payment_should_be_validated_successfully() {
        await().atMost(30, TimeUnit.SECONDS)
            .until(() -> paymentService.getPaymentStatus(currentPaymentResponse.getPaymentId()).equals("VALIDATED"));
        
        String status = paymentService.getPaymentStatus(currentPaymentResponse.getPaymentId());
        assertThat(status).isEqualTo("VALIDATED");
    }

    @Then("the payment should be processed successfully")
    public void the_payment_should_be_processed_successfully() {
        await().atMost(30, TimeUnit.SECONDS)
            .until(() -> paymentService.getPaymentStatus(currentPaymentResponse.getPaymentId()).equals("PROCESSED"));
        
        String status = paymentService.getPaymentStatus(currentPaymentResponse.getPaymentId());
        assertThat(status).isEqualTo("PROCESSED");
    }

    @Then("the payment should be routed to SAMOS clearing")
    public void the_payment_should_be_routed_to_samos_clearing() {
        await().atMost(30, TimeUnit.SECONDS)
            .until(() -> paymentService.getPaymentStatus(currentPaymentResponse.getPaymentId()).equals("ROUTED"));
        
        String status = paymentService.getPaymentStatus(currentPaymentResponse.getPaymentId());
        assertThat(status).isEqualTo("ROUTED");
    }

    @Then("the payment should be submitted to clearing system")
    public void the_payment_should_be_submitted_to_clearing_system() {
        await().atMost(30, TimeUnit.SECONDS)
            .until(() -> paymentService.getPaymentStatus(currentPaymentResponse.getPaymentId()).equals("SUBMITTED"));
        
        String status = paymentService.getPaymentStatus(currentPaymentResponse.getPaymentId());
        assertThat(status).isEqualTo("SUBMITTED");
    }

    @Then("the payment should be settled successfully")
    public void the_payment_should_be_settled_successfully() {
        await().atMost(30, TimeUnit.SECONDS)
            .until(() -> paymentService.getPaymentStatus(currentPaymentResponse.getPaymentId()).equals("SETTLED"));
        
        String status = paymentService.getPaymentStatus(currentPaymentResponse.getPaymentId());
        assertThat(status).isEqualTo("SETTLED");
    }

    // Account Balance Assertions
    @Then("the source account balance should be {string}")
    public void the_source_account_balance_should_be(String expectedBalance) {
        BigDecimal balance = accountService.getAccountBalance(currentPaymentRequest.getFromAccount());
        assertThat(balance).isEqualTo(new BigDecimal(expectedBalance));
    }

    @Then("the destination account balance should be {string}")
    public void the_destination_account_balance_should_be(String expectedBalance) {
        BigDecimal balance = accountService.getAccountBalance(currentPaymentRequest.getToAccount());
        assertThat(balance).isEqualTo(new BigDecimal(expectedBalance));
    }

    @Then("the account balances should remain unchanged")
    public void the_account_balances_should_remain_unchanged() {
        BigDecimal sourceBalance = accountService.getAccountBalance(currentPaymentRequest.getFromAccount());
        BigDecimal destinationBalance = accountService.getAccountBalance(currentPaymentRequest.getToAccount());
        
        // Verify balances are the same as initial setup
        assertThat(sourceBalance).isEqualTo(new BigDecimal("1000.00"));
        assertThat(destinationBalance).isEqualTo(new BigDecimal("500.00"));
    }

    // Notification Assertions
    @Then("a payment notification should be sent")
    public void a_payment_notification_should_be_sent() {
        await().atMost(30, TimeUnit.SECONDS)
            .until(() -> notificationService.hasNotification(currentPaymentResponse.getPaymentId()));
        
        assertThat(notificationService.hasNotification(currentPaymentResponse.getPaymentId())).isTrue();
    }

    @Then("a rejection notification should be sent")
    public void a_rejection_notification_should_be_sent() {
        await().atMost(30, TimeUnit.SECONDS)
            .until(() -> notificationService.hasRejectionNotification(currentPaymentResponse.getPaymentId()));
        
        assertThat(notificationService.hasRejectionNotification(currentPaymentResponse.getPaymentId())).isTrue();
    }

    @Then("a fraud rejection notification should be sent")
    public void a_fraud_rejection_notification_should_be_sent() {
        await().atMost(30, TimeUnit.SECONDS)
            .until(() -> notificationService.hasFraudRejectionNotification(currentPaymentResponse.getPaymentId()));
        
        assertThat(notificationService.hasFraudRejectionNotification(currentPaymentResponse.getPaymentId())).isTrue();
    }

    @Then("a timeout notification should be sent")
    public void a_timeout_notification_should_be_sent() {
        await().atMost(30, TimeUnit.SECONDS)
            .until(() -> notificationService.hasTimeoutNotification(currentPaymentResponse.getPaymentId()));
        
        assertThat(notificationService.hasTimeoutNotification(currentPaymentResponse.getPaymentId())).isTrue();
    }

    // Audit Trail Assertions
    @Then("an audit trail should be created")
    public void an_audit_trail_should_be_created() {
        await().atMost(30, TimeUnit.SECONDS)
            .until(() -> paymentService.hasAuditTrail(currentPaymentResponse.getPaymentId()));
        
        assertThat(paymentService.hasAuditTrail(currentPaymentResponse.getPaymentId())).isTrue();
    }

    // Failure Scenarios
    @Then("the account adapter should return insufficient balance error")
    public void the_account_adapter_should_return_insufficient_balance_error() {
        await().atMost(30, TimeUnit.SECONDS)
            .until(() -> paymentService.getPaymentStatus(currentPaymentResponse.getPaymentId()).equals("FAILED"));
        
        String status = paymentService.getPaymentStatus(currentPaymentResponse.getPaymentId());
        assertThat(status).isEqualTo("FAILED");
    }

    @Then("the payment should be rejected")
    public void the_payment_should_be_rejected() {
        String status = paymentService.getPaymentStatus(currentPaymentResponse.getPaymentId());
        assertThat(status).isEqualTo("REJECTED");
    }

    @Then("the fraud detection should reject the payment")
    public void the_fraud_detection_should_reject_the_payment() {
        await().atMost(30, TimeUnit.SECONDS)
            .until(() -> paymentService.getPaymentStatus(currentPaymentResponse.getPaymentId()).equals("REJECTED"));
        
        String status = paymentService.getPaymentStatus(currentPaymentResponse.getPaymentId());
        assertThat(status).isEqualTo("REJECTED");
    }

    @Then("the clearing system should timeout")
    public void the_clearing_system_should_timeout() {
        await().atMost(30, TimeUnit.SECONDS)
            .until(() -> paymentService.getPaymentStatus(currentPaymentResponse.getPaymentId()).equals("CLEARING_TIMEOUT"));
        
        String status = paymentService.getPaymentStatus(currentPaymentResponse.getPaymentId());
        assertThat(status).isEqualTo("CLEARING_TIMEOUT");
    }

    @Then("the payment should be compensated")
    public void the_payment_should_be_compensated() {
        await().atMost(30, TimeUnit.SECONDS)
            .until(() -> paymentService.getPaymentStatus(currentPaymentResponse.getPaymentId()).equals("COMPENSATED"));
        
        String status = paymentService.getPaymentStatus(currentPaymentResponse.getPaymentId());
        assertThat(status).isEqualTo("COMPENSATED");
    }

    // Multi-tenant Steps
    @Given("a valid EFT payment request is created for tenant {string}")
    public void a_valid_eft_payment_request_is_created_for_tenant(String tenantId, io.cucumber.datatable.DataTable dataTable) {
        currentPaymentRequest = testDataBuilder.createPaymentRequest(dataTable);
        currentPaymentRequest.setPaymentType("EFT");
        currentPaymentRequest.setTenantId(tenantId);
    }

    @Then("the payment should only be visible to {string}")
    public void the_payment_should_only_be_visible_to(String tenantId) {
        assertThat(paymentService.isPaymentVisibleToTenant(currentPaymentResponse.getPaymentId(), tenantId)).isTrue();
    }

    @Then("tenant {string} should not see this payment")
    public void tenant_should_not_see_this_payment(String tenantId) {
        assertThat(paymentService.isPaymentVisibleToTenant(currentPaymentResponse.getPaymentId(), tenantId)).isFalse();
    }

    @Then("tenant data should be properly isolated")
    public void tenant_data_should_be_properly_isolated() {
        assertThat(paymentService.isTenantDataIsolated(currentPaymentResponse.getPaymentId())).isTrue();
    }

    // Performance Steps
    @Given("{int} EFT payment requests are created")
    public void eft_payment_requests_are_created(Integer count) {
        // Implementation for creating multiple payment requests
        testDataBuilder.createMultiplePaymentRequests(count);
    }

    @When("all payments are submitted concurrently")
    public void all_payments_are_submitted_concurrently() {
        // Implementation for concurrent payment submission
        paymentService.submitMultiplePaymentsConcurrently();
    }

    @Then("all payments should be processed within {string} seconds")
    public void all_payments_should_be_processed_within_seconds(String maxSeconds) {
        int maxSecondsInt = Integer.parseInt(maxSeconds);
        await().atMost(maxSecondsInt, TimeUnit.SECONDS)
            .until(() -> paymentService.allPaymentsProcessed());
        
        assertThat(paymentService.allPaymentsProcessed()).isTrue();
    }

    @Then("the success rate should be {string} percent")
    public void the_success_rate_should_be_percent(String expectedRate) {
        double successRate = paymentService.getSuccessRate();
        assertThat(successRate).isGreaterThanOrEqualTo(Double.parseDouble(expectedRate));
    }

    @Then("the average processing time should be less than {string} seconds")
    public void the_average_processing_time_should_be_less_than_seconds(String maxSeconds) {
        double avgProcessingTime = paymentService.getAverageProcessingTime();
        assertThat(avgProcessingTime).isLessThan(Double.parseDouble(maxSeconds));
    }

    @Then("no payment should be lost or duplicated")
    public void no_payment_should_be_lost_or_duplicated() {
        assertThat(paymentService.noPaymentsLost()).isTrue();
        assertThat(paymentService.noPaymentsDuplicated()).isTrue();
    }
}
