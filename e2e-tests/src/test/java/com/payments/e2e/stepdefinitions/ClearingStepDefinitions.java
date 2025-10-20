package com.payments.e2e.stepdefinitions;

import com.payments.e2e.config.E2ETestConfiguration;
import com.payments.e2e.data.TestDataBuilder;
import com.payments.e2e.models.PaymentRequest;
import com.payments.e2e.models.ClearingResponse;
import com.payments.e2e.services.ClearingService;
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
 * Clearing Step Definitions for E2E Tests
 * 
 * Implements step definitions for payment clearing scenarios including:
 * - SAMOS clearing
 * - RTC clearing
 * - PayShap clearing
 * - SWIFT clearing
 * - Clearing system failures
 * - Clearing system timeouts
 */
@ContextConfiguration(classes = {E2ETestConfiguration.class})
public class ClearingStepDefinitions {

    @Autowired
    private ClearingService clearingService;
    
    @Autowired
    private NotificationService notificationService;
    
    @Autowired
    private TestDataBuilder testDataBuilder;

    private PaymentRequest currentPaymentRequest;
    private ClearingResponse currentClearingResponse;
    private String currentTenantId;

    // Background Steps
    @Given("all clearing adapters are available")
    public void all_clearing_adapters_are_available() {
        assertThat(clearingService.areAllAdaptersAvailable()).isTrue();
    }

    @Given("clearing systems are mocked")
    public void clearing_systems_are_mocked() {
        testDataBuilder.setupClearingSystemMocks();
    }

    // Payment Request Steps
    @Given("a processed payment request is created")
    public void a_processed_payment_request_is_created(io.cucumber.datatable.DataTable dataTable) {
        currentPaymentRequest = testDataBuilder.createPaymentRequest(dataTable);
        currentPaymentRequest.setTenantId(currentTenantId);
        currentPaymentRequest.setStatus("PROCESSED");
    }

    @Given("the payment is routed to SAMOS clearing")
    public void the_payment_is_routed_to_samos_clearing() {
        testDataBuilder.routePaymentToClearing(currentPaymentRequest, "SAMOS");
    }

    @Given("the payment is routed to RTC clearing")
    public void the_payment_is_routed_to_rtc_clearing() {
        testDataBuilder.routePaymentToClearing(currentPaymentRequest, "RTC");
    }

    @Given("the payment is routed to PayShap clearing")
    public void the_payment_is_routed_to_payshap_clearing() {
        testDataBuilder.routePaymentToClearing(currentPaymentRequest, "PayShap");
    }

    @Given("the payment is routed to SWIFT clearing")
    public void the_payment_is_routed_to_swift_clearing() {
        testDataBuilder.routePaymentToClearing(currentPaymentRequest, "SWIFT");
    }

    @Given("the payment can be routed to multiple clearing systems")
    public void the_payment_can_be_routed_to_multiple_clearing_systems() {
        testDataBuilder.configureMultipleClearingRoutes(currentPaymentRequest);
    }

    @Given("all payments are routed to SAMOS clearing")
    public void all_payments_are_routed_to_samos_clearing() {
        testDataBuilder.routeAllPaymentsToClearing("SAMOS");
    }

    @Given("payments are routed to different clearing systems")
    public void payments_are_routed_to_different_clearing_systems() {
        testDataBuilder.routePaymentsToDifferentClearingSystems();
    }

    // Clearing System Configuration Steps
    @Given("the SAMOS system is configured to timeout")
    public void the_samos_system_is_configured_to_timeout() {
        testDataBuilder.configureClearingSystemTimeout("SAMOS");
    }

    @Given("the SAMOS system is configured to reject the payment")
    public void the_samos_system_is_configured_to_reject_the_payment() {
        testDataBuilder.configureClearingSystemRejection("SAMOS");
    }

    @Given("the SAMOS system completes the payment")
    public void the_samos_system_completes_the_payment() {
        testDataBuilder.configureClearingSystemCompletion("SAMOS");
    }

    @Given("all payments are ready for settlement")
    public void all_payments_are_ready_for_settlement() {
        testDataBuilder.configureAllPaymentsReadyForSettlement();
    }

    // Clearing Submission Steps
    @When("the payment is submitted to SAMOS")
    public void the_payment_is_submitted_to_samos() {
        currentClearingResponse = clearingService.submitPaymentToClearing(currentPaymentRequest, "SAMOS");
    }

    @When("the payment is submitted to RTC")
    public void the_payment_is_submitted_to_rtc() {
        currentClearingResponse = clearingService.submitPaymentToClearing(currentPaymentRequest, "RTC");
    }

    @When("the payment is submitted to PayShap")
    public void the_payment_is_submitted_to_payshap() {
        currentClearingResponse = clearingService.submitPaymentToClearing(currentPaymentRequest, "PayShap");
    }

    @When("the payment is submitted to SWIFT")
    public void the_payment_is_submitted_to_swift() {
        currentClearingResponse = clearingService.submitPaymentToClearing(currentPaymentRequest, "SWIFT");
    }

    @When("the payment is submitted for clearing")
    public void the_payment_is_submitted_for_clearing() {
        currentClearingResponse = clearingService.submitPaymentForClearing(currentPaymentRequest);
    }

    @When("all payments are submitted for batch clearing")
    public void all_payments_are_submitted_for_batch_clearing() {
        clearingService.submitAllPaymentsForBatchClearing();
    }

    @When("all payments are submitted for clearing")
    public void all_payments_are_submitted_for_clearing() {
        clearingService.submitAllPaymentsForClearing();
    }

    // SAMOS Clearing Assertions
    @Then("the SAMOS adapter should format the message correctly")
    public void the_samos_adapter_should_format_the_message_correctly() {
        await().atMost(30, TimeUnit.SECONDS)
            .until(() -> clearingService.isMessageFormattedCorrectly(currentClearingResponse.getClearingId(), "SAMOS"));
        
        assertThat(clearingService.isMessageFormattedCorrectly(currentClearingResponse.getClearingId(), "SAMOS")).isTrue();
    }

    @Then("the message should be submitted to SAMOS")
    public void the_message_should_be_submitted_to_samos() {
        await().atMost(30, TimeUnit.SECONDS)
            .until(() -> clearingService.isMessageSubmittedToClearing(currentClearingResponse.getClearingId(), "SAMOS"));
        
        assertThat(clearingService.isMessageSubmittedToClearing(currentClearingResponse.getClearingId(), "SAMOS")).isTrue();
    }

    @Then("the SAMOS system should acknowledge the payment")
    public void the_samos_system_should_acknowledge_the_payment() {
        await().atMost(30, TimeUnit.SECONDS)
            .until(() -> clearingService.isPaymentAcknowledgedByClearing(currentClearingResponse.getClearingId(), "SAMOS"));
        
        assertThat(clearingService.isPaymentAcknowledgedByClearing(currentClearingResponse.getClearingId(), "SAMOS")).isTrue();
    }

    @Then("the payment should be marked as submitted")
    public void the_payment_should_be_marked_as_submitted() {
        await().atMost(30, TimeUnit.SECONDS)
            .until(() -> clearingService.getClearingStatus(currentClearingResponse.getClearingId()).equals("SUBMITTED"));
        
        String status = clearingService.getClearingStatus(currentClearingResponse.getClearingId());
        assertThat(status).isEqualTo("SUBMITTED");
    }

    @Then("a clearing submission event should be published")
    public void a_clearing_submission_event_should_be_published() {
        await().atMost(30, TimeUnit.SECONDS)
            .until(() -> clearingService.hasClearingSubmissionEvent(currentClearingResponse.getClearingId()));
        
        assertThat(clearingService.hasClearingSubmissionEvent(currentClearingResponse.getClearingId())).isTrue();
    }

    // RTC Clearing Assertions
    @Then("the RTC adapter should format the message correctly")
    public void the_rtc_adapter_should_format_the_message_correctly() {
        await().atMost(30, TimeUnit.SECONDS)
            .until(() -> clearingService.isMessageFormattedCorrectly(currentClearingResponse.getClearingId(), "RTC"));
        
        assertThat(clearingService.isMessageFormattedCorrectly(currentClearingResponse.getClearingId(), "RTC")).isTrue();
    }

    @Then("the message should be submitted to RTC")
    public void the_message_should_be_submitted_to_rtc() {
        await().atMost(30, TimeUnit.SECONDS)
            .until(() -> clearingService.isMessageSubmittedToClearing(currentClearingResponse.getClearingId(), "RTC"));
        
        assertThat(clearingService.isMessageSubmittedToClearing(currentClearingResponse.getClearingId(), "RTC")).isTrue();
    }

    @Then("the RTC system should acknowledge the payment")
    public void the_rtc_system_should_acknowledge_the_payment() {
        await().atMost(30, TimeUnit.SECONDS)
            .until(() -> clearingService.isPaymentAcknowledgedByClearing(currentClearingResponse.getClearingId(), "RTC"));
        
        assertThat(clearingService.isPaymentAcknowledgedByClearing(currentClearingResponse.getClearingId(), "RTC")).isTrue();
    }

    // PayShap Clearing Assertions
    @Then("the PayShap adapter should format the message correctly")
    public void the_payshap_adapter_should_format_the_message_correctly() {
        await().atMost(30, TimeUnit.SECONDS)
            .until(() -> clearingService.isMessageFormattedCorrectly(currentClearingResponse.getClearingId(), "PayShap"));
        
        assertThat(clearingService.isMessageFormattedCorrectly(currentClearingResponse.getClearingId(), "PayShap")).isTrue();
    }

    @Then("the message should be submitted to PayShap")
    public void the_message_should_be_submitted_to_payshap() {
        await().atMost(30, TimeUnit.SECONDS)
            .until(() -> clearingService.isMessageSubmittedToClearing(currentClearingResponse.getClearingId(), "PayShap"));
        
        assertThat(clearingService.isMessageSubmittedToClearing(currentClearingResponse.getClearingId(), "PayShap")).isTrue();
    }

    @Then("the PayShap system should acknowledge the payment")
    public void the_payshap_system_should_acknowledge_the_payment() {
        await().atMost(30, TimeUnit.SECONDS)
            .until(() -> clearingService.isPaymentAcknowledgedByClearing(currentClearingResponse.getClearingId(), "PayShap"));
        
        assertThat(clearingService.isPaymentAcknowledgedByClearing(currentClearingResponse.getClearingId(), "PayShap")).isTrue();
    }

    // SWIFT Clearing Assertions
    @Then("the SWIFT adapter should format the MT103 message correctly")
    public void the_swift_adapter_should_format_the_mt103_message_correctly() {
        await().atMost(30, TimeUnit.SECONDS)
            .until(() -> clearingService.isMessageFormattedCorrectly(currentClearingResponse.getClearingId(), "SWIFT"));
        
        assertThat(clearingService.isMessageFormattedCorrectly(currentClearingResponse.getClearingId(), "SWIFT")).isTrue();
    }

    @Then("the message should be submitted to SWIFT")
    public void the_message_should_be_submitted_to_swift() {
        await().atMost(30, TimeUnit.SECONDS)
            .until(() -> clearingService.isMessageSubmittedToClearing(currentClearingResponse.getClearingId(), "SWIFT"));
        
        assertThat(clearingService.isMessageSubmittedToClearing(currentClearingResponse.getClearingId(), "SWIFT")).isTrue();
    }

    @Then("the SWIFT system should acknowledge the payment")
    public void the_swift_system_should_acknowledge_the_payment() {
        await().atMost(30, TimeUnit.SECONDS)
            .until(() -> clearingService.isPaymentAcknowledgedByClearing(currentClearingResponse.getClearingId(), "SWIFT"));
        
        assertThat(clearingService.isPaymentAcknowledgedByClearing(currentClearingResponse.getClearingId(), "SWIFT")).isTrue();
    }

    // Clearing Failure Assertions
    @Then("the SAMOS adapter should timeout")
    public void the_samos_adapter_should_timeout() {
        await().atMost(30, TimeUnit.SECONDS)
            .until(() -> clearingService.getClearingStatus(currentClearingResponse.getClearingId()).equals("TIMEOUT"));
        
        String status = clearingService.getClearingStatus(currentClearingResponse.getClearingId());
        assertThat(status).isEqualTo("TIMEOUT");
    }

    @Then("the payment should be marked as failed")
    public void the_payment_should_be_marked_as_failed() {
        await().atMost(30, TimeUnit.SECONDS)
            .until(() -> clearingService.getClearingStatus(currentClearingResponse.getClearingId()).equals("FAILED"));
        
        String status = clearingService.getClearingStatus(currentClearingResponse.getClearingId());
        assertThat(status).isEqualTo("FAILED");
    }

    @Then("a clearing timeout event should be published")
    public void a_clearing_timeout_event_should_be_published() {
        await().atMost(30, TimeUnit.SECONDS)
            .until(() -> clearingService.hasClearingTimeoutEvent(currentClearingResponse.getClearingId()));
        
        assertThat(clearingService.hasClearingTimeoutEvent(currentClearingResponse.getClearingId())).isTrue();
    }

    @Then("the payment should be queued for retry")
    public void the_payment_should_be_queued_for_retry() {
        await().atMost(30, TimeUnit.SECONDS)
            .until(() -> clearingService.isPaymentQueuedForRetry(currentClearingResponse.getClearingId()));
        
        assertThat(clearingService.isPaymentQueuedForRetry(currentClearingResponse.getClearingId())).isTrue();
    }

    @Then("the SAMOS adapter should receive a rejection")
    public void the_samos_adapter_should_receive_a_rejection() {
        await().atMost(30, TimeUnit.SECONDS)
            .until(() -> clearingService.getClearingStatus(currentClearingResponse.getClearingId()).equals("REJECTED"));
        
        String status = clearingService.getClearingStatus(currentClearingResponse.getClearingId());
        assertThat(status).isEqualTo("REJECTED");
    }

    @Then("the payment should be marked as rejected")
    public void the_payment_should_be_marked_as_rejected() {
        await().atMost(30, TimeUnit.SECONDS)
            .until(() -> clearingService.getClearingStatus(currentClearingResponse.getClearingId()).equals("REJECTED"));
        
        String status = clearingService.getClearingStatus(currentClearingResponse.getClearingId());
        assertThat(status).isEqualTo("REJECTED");
    }

    @Then("a clearing rejection event should be published")
    public void a_clearing_rejection_event_should_be_published() {
        await().atMost(30, TimeUnit.SECONDS)
            .until(() -> clearingService.hasClearingRejectionEvent(currentClearingResponse.getClearingId()));
        
        assertThat(clearingService.hasClearingRejectionEvent(currentClearingResponse.getClearingId())).isTrue();
    }

    @Then("the payment should be queued for manual review")
    public void the_payment_should_be_queued_for_manual_review() {
        await().atMost(30, TimeUnit.SECONDS)
            .until(() -> clearingService.isPaymentQueuedForManualReview(currentClearingResponse.getClearingId()));
        
        assertThat(clearingService.isPaymentQueuedForManualReview(currentClearingResponse.getClearingId())).isTrue();
    }

    // Clearing Completion Assertions
    @Then("the SAMOS adapter should receive a completion notification")
    public void the_samos_adapter_should_receive_a_completion_notification() {
        await().atMost(30, TimeUnit.SECONDS)
            .until(() -> clearingService.isPaymentCompletedByClearing(currentClearingResponse.getClearingId(), "SAMOS"));
        
        assertThat(clearingService.isPaymentCompletedByClearing(currentClearingResponse.getClearingId(), "SAMOS")).isTrue();
    }

    @Then("the payment should be marked as completed")
    public void the_payment_should_be_marked_as_completed() {
        await().atMost(30, TimeUnit.SECONDS)
            .until(() -> clearingService.getClearingStatus(currentClearingResponse.getClearingId()).equals("COMPLETED"));
        
        String status = clearingService.getClearingStatus(currentClearingResponse.getClearingId());
        assertThat(status).isEqualTo("COMPLETED");
    }

    @Then("a clearing completion event should be published")
    public void a_clearing_completion_event_should_be_published() {
        await().atMost(30, TimeUnit.SECONDS)
            .until(() -> clearingService.hasClearingCompletionEvent(currentClearingResponse.getClearingId()));
        
        assertThat(clearingService.hasClearingCompletionEvent(currentClearingResponse.getClearingId())).isTrue();
    }

    @Then("the payment should be ready for settlement")
    public void the_payment_should_be_ready_for_settlement() {
        await().atMost(30, TimeUnit.SECONDS)
            .until(() -> clearingService.isPaymentReadyForSettlement(currentClearingResponse.getClearingId()));
        
        assertThat(clearingService.isPaymentReadyForSettlement(currentClearingResponse.getClearingId())).isTrue();
    }

    // Routing Assertions
    @Then("the routing service should select the optimal clearing system")
    public void the_routing_service_should_select_the_optimal_clearing_system() {
        await().atMost(30, TimeUnit.SECONDS)
            .until(() -> clearingService.isOptimalClearingSystemSelected(currentClearingResponse.getClearingId()));
        
        assertThat(clearingService.isOptimalClearingSystemSelected(currentClearingResponse.getClearingId())).isTrue();
    }

    @Then("the payment should be submitted to the selected clearing system")
    public void the_payment_should_be_submitted_to_the_selected_clearing_system() {
        await().atMost(30, TimeUnit.SECONDS)
            .until(() -> clearingService.isPaymentSubmittedToSelectedClearing(currentClearingResponse.getClearingId()));
        
        assertThat(clearingService.isPaymentSubmittedToSelectedClearing(currentClearingResponse.getClearingId())).isTrue();
    }

    // Batch Clearing Assertions
    @Then("all payments should be submitted to SAMOS")
    public void all_payments_should_be_submitted_to_samos() {
        await().atMost(30, TimeUnit.SECONDS)
            .until(() -> clearingService.allPaymentsSubmittedToClearing("SAMOS"));
        
        assertThat(clearingService.allPaymentsSubmittedToClearing("SAMOS")).isTrue();
    }

    @Then("all payments should be acknowledged by SAMOS")
    public void all_payments_should_be_acknowledged_by_samos() {
        await().atMost(30, TimeUnit.SECONDS)
            .until(() -> clearingService.allPaymentsAcknowledgedByClearing("SAMOS"));
        
        assertThat(clearingService.allPaymentsAcknowledgedByClearing("SAMOS")).isTrue();
    }

    @Then("all payments should be marked as submitted")
    public void all_payments_should_be_marked_as_submitted() {
        await().atMost(30, TimeUnit.SECONDS)
            .until(() -> clearingService.allPaymentsMarkedAsSubmitted());
        
        assertThat(clearingService.allPaymentsMarkedAsSubmitted()).isTrue();
    }

    @Then("a batch clearing submission event should be published")
    public void a_batch_clearing_submission_event_should_be_published() {
        await().atMost(30, TimeUnit.SECONDS)
            .until(() -> clearingService.hasBatchClearingSubmissionEvent());
        
        assertThat(clearingService.hasBatchClearingSubmissionEvent()).isTrue();
    }

    // Performance Assertions
    @Then("all payments should be cleared within {string} seconds")
    public void all_payments_should_be_cleared_within_seconds(String maxSeconds) {
        int maxSecondsInt = Integer.parseInt(maxSeconds);
        await().atMost(maxSecondsInt, TimeUnit.SECONDS)
            .until(() -> clearingService.allPaymentsCleared());
        
        assertThat(clearingService.allPaymentsCleared()).isTrue();
    }

    @Then("the clearing success rate should be {string} percent")
    public void the_clearing_success_rate_should_be_percent(String expectedRate) {
        double successRate = clearingService.getClearingSuccessRate();
        assertThat(successRate).isGreaterThanOrEqualTo(Double.parseDouble(expectedRate));
    }

    @Then("the average clearing time should be less than {string} seconds")
    public void the_average_clearing_time_should_be_less_than_seconds(String maxSeconds) {
        double avgClearingTime = clearingService.getAverageClearingTime();
        assertThat(avgClearingTime).isLessThan(Double.parseDouble(maxSeconds));
    }

    @Then("no payment should be lost during clearing")
    public void no_payment_should_be_lost_during_clearing() {
        assertThat(clearingService.noPaymentsLostDuringClearing()).isTrue();
    }
}
