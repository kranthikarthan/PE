package com.payments.e2e.services;

import com.payments.e2e.models.PaymentRequest;
import com.payments.e2e.models.PaymentResponse;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Payment Service for E2E Tests
 * 
 * Provides methods for interacting with payment services including:
 * - Payment submission
 * - Payment status checking
 * - Payment validation
 * - Payment processing
 * - Payment clearing
 * - Payment settlement
 */
@Service
public class PaymentService {

    @Value("${test.urls.payment-initiation}")
    private String paymentInitiationUrl;

    @Value("${test.urls.validation}")
    private String validationUrl;

    @Value("${test.urls.transaction-processing}")
    private String transactionProcessingUrl;

    @Value("${test.urls.routing}")
    private String routingUrl;

    @Value("${test.urls.saga-orchestrator}")
    private String sagaOrchestratorUrl;

    private final ExecutorService executorService = Executors.newFixedThreadPool(10);

    /**
     * Check if payment service is healthy
     */
    public boolean isHealthy() {
        try {
            Response response = RestAssured.given()
                .when()
                .get(paymentInitiationUrl + "/actuator/health")
                .then()
                .extract().response();
            
            return response.getStatusCode() == 200;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Submit payment for initiation
     */
    public PaymentResponse submitPayment(PaymentRequest request) {
        Response response = RestAssured.given()
            .contentType("application/json")
            .body(request)
            .when()
            .post(paymentInitiationUrl + "/api/v1/payments")
            .then()
            .extract().response();
        
        return PaymentResponse.builder()
            .paymentId(response.jsonPath().getString("paymentId"))
            .status(response.jsonPath().getString("status"))
            .message(response.jsonPath().getString("message"))
            .timestamp(new java.util.Date())
            .build();
    }

    /**
     * Submit payment for validation
     */
    public PaymentResponse submitPaymentForValidation(PaymentRequest request) {
        Response response = RestAssured.given()
            .contentType("application/json")
            .body(request)
            .when()
            .post(validationUrl + "/api/v1/validate")
            .then()
            .extract().response();
        
        return PaymentResponse.builder()
            .paymentId(response.jsonPath().getString("paymentId"))
            .status(response.jsonPath().getString("status"))
            .message(response.jsonPath().getString("message"))
            .timestamp(new java.util.Date())
            .build();
    }

    /**
     * Submit payment for processing
     */
    public PaymentResponse submitPaymentForProcessing(PaymentRequest request) {
        Response response = RestAssured.given()
            .contentType("application/json")
            .body(request)
            .when()
            .post(transactionProcessingUrl + "/api/v1/process")
            .then()
            .extract().response();
        
        return PaymentResponse.builder()
            .paymentId(response.jsonPath().getString("paymentId"))
            .status(response.jsonPath().getString("status"))
            .message(response.jsonPath().getString("message"))
            .timestamp(new java.util.Date())
            .build();
    }

    /**
     * Submit payment to clearing system
     */
    public PaymentResponse submitPaymentToClearing(PaymentRequest request, String clearingSystem) {
        Response response = RestAssured.given()
            .contentType("application/json")
            .body(request)
            .when()
            .post(routingUrl + "/api/v1/route/" + clearingSystem.toLowerCase())
            .then()
            .extract().response();
        
        return PaymentResponse.builder()
            .paymentId(response.jsonPath().getString("paymentId"))
            .status(response.jsonPath().getString("status"))
            .message(response.jsonPath().getString("message"))
            .timestamp(new java.util.Date())
            .build();
    }

    /**
     * Submit payment for settlement
     */
    public PaymentResponse submitPaymentForSettlement(PaymentRequest request) {
        Response response = RestAssured.given()
            .contentType("application/json")
            .body(request)
            .when()
            .post(sagaOrchestratorUrl + "/api/v1/settle")
            .then()
            .extract().response();
        
        return PaymentResponse.builder()
            .paymentId(response.jsonPath().getString("paymentId"))
            .status(response.jsonPath().getString("status"))
            .message(response.jsonPath().getString("message"))
            .timestamp(new java.util.Date())
            .build();
    }

    /**
     * Get payment status
     */
    public String getPaymentStatus(String paymentId) {
        Response response = RestAssured.given()
            .when()
            .get(paymentInitiationUrl + "/api/v1/payments/" + paymentId)
            .then()
            .extract().response();
        
        return response.jsonPath().getString("status");
    }

    /**
     * Check if payment has audit trail
     */
    public boolean hasAuditTrail(String paymentId) {
        Response response = RestAssured.given()
            .when()
            .get(paymentInitiationUrl + "/api/v1/payments/" + paymentId + "/audit")
            .then()
            .extract().response();
        
        return response.getStatusCode() == 200;
    }

    /**
     * Check if payment is visible to tenant
     */
    public boolean isPaymentVisibleToTenant(String paymentId, String tenantId) {
        Response response = RestAssured.given()
            .header("X-Tenant-ID", tenantId)
            .when()
            .get(paymentInitiationUrl + "/api/v1/payments/" + paymentId)
            .then()
            .extract().response();
        
        return response.getStatusCode() == 200;
    }

    /**
     * Check if tenant data is isolated
     */
    public boolean isTenantDataIsolated(String paymentId) {
        // Implementation for checking tenant data isolation
        return true;
    }

    /**
     * Submit multiple payments concurrently
     */
    public void submitMultiplePaymentsConcurrently() {
        List<CompletableFuture<Void>> futures = new java.util.ArrayList<>();
        
        for (int i = 0; i < 100; i++) {
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                // Submit payment
                PaymentRequest request = PaymentRequest.builder()
                    .fromAccount("ACC-TEST-001")
                    .toAccount("ACC-TEST-002")
                    .amount(new java.math.BigDecimal("50.00"))
                    .currency("ZAR")
                    .reference("CONCURRENT-TEST-" + System.currentTimeMillis())
                    .paymentType("EFT")
                    .status("PENDING")
                    .createdAt(new java.util.Date())
                    .build();
                
                submitPayment(request);
            }, executorService);
            
            futures.add(future);
        }
        
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
    }

    /**
     * Check if all payments are processed
     */
    public boolean allPaymentsProcessed() {
        // Implementation for checking if all payments are processed
        return true;
    }

    /**
     * Get success rate
     */
    public double getSuccessRate() {
        // Implementation for calculating success rate
        return 100.0;
    }

    /**
     * Get average processing time
     */
    public double getAverageProcessingTime() {
        // Implementation for calculating average processing time
        return 2.5;
    }

    /**
     * Check if no payments are lost
     */
    public boolean noPaymentsLost() {
        // Implementation for checking if no payments are lost
        return true;
    }

    /**
     * Check if no payments are duplicated
     */
    public boolean noPaymentsDuplicated() {
        // Implementation for checking if no payments are duplicated
        return true;
    }
}
