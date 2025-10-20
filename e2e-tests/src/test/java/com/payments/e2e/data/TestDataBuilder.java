package com.payments.e2e.data;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javafaker.Faker;
import com.payments.e2e.models.PaymentRequest;
import com.payments.e2e.models.TestAccount;
import com.payments.e2e.models.TestTenant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;

/**
 * Test Data Builder for E2E Tests
 * 
 * Provides utilities for creating test data including:
 * - Payment requests
 * - Test accounts
 * - Test tenants
 * - External system mocks
 */
@Component
public class TestDataBuilder {

    @Autowired
    private ObjectMapper objectMapper;
    
    private final Faker faker = new Faker();
    private final Map<String, TestTenant> tenants = new HashMap<>();
    private final Map<String, TestAccount> accounts = new HashMap<>();
    private final Map<String, String> accountDailyLimits = new HashMap<>();
    private final Set<String> sanctionsList = new HashSet<>();
    private final Map<String, Object> fraudDetectionConfig = new HashMap<>();
    private final Map<String, Object> riskAssessmentConfig = new HashMap<>();

    /**
     * Setup test tenant
     */
    public void setupTenant(String tenantId) {
        TestTenant tenant = TestTenant.builder()
            .tenantId(tenantId)
            .tenantName("Test Tenant " + tenantId)
            .status("ACTIVE")
            .createdAt(new Date())
            .build();
        
        tenants.put(tenantId, tenant);
    }

    /**
     * Setup test account with balance
     */
    public void setupAccount(String accountId, BigDecimal balance) {
        TestAccount account = TestAccount.builder()
            .accountId(accountId)
            .accountNumber(faker.finance().iban())
            .balance(balance)
            .currency("ZAR")
            .status("ACTIVE")
            .createdAt(new Date())
            .build();
        
        accounts.put(accountId, account);
    }

    /**
     * Setup external system mocks
     */
    public void setupExternalSystemMocks() {
        // Setup WireMock configurations for external systems
        setupCoreBankingMocks();
        setupFraudApiMocks();
        setupClearingSystemMocks();
    }

    /**
     * Setup clearing system mocks
     */
    public void setupClearingSystemMocks() {
        // Setup WireMock configurations for clearing systems
        setupSAMOSMocks();
        setupRTCMocks();
        setupPayShapMocks();
        setupSWIFTMocks();
    }

    /**
     * Create payment request from data table
     */
    public PaymentRequest createPaymentRequest(io.cucumber.datatable.DataTable dataTable) {
        Map<String, String> data = dataTable.asMap(String.class, String.class);
        
        return PaymentRequest.builder()
            .fromAccount(data.get("From Account"))
            .toAccount(data.get("To Account"))
            .amount(new BigDecimal(data.get("Amount")))
            .currency(data.get("Currency"))
            .reference(data.get("Reference"))
            .paymentType("EFT")
            .status("PENDING")
            .createdAt(new Date())
            .build();
    }

    /**
     * Create multiple payment requests
     */
    public void createMultiplePaymentRequests(int count) {
        for (int i = 0; i < count; i++) {
            PaymentRequest request = PaymentRequest.builder()
                .fromAccount("ACC-TEST-001")
                .toAccount("ACC-TEST-002")
                .amount(new BigDecimal(faker.number().numberBetween(10, 100)))
                .currency("ZAR")
                .reference("EFT-TEST-" + i)
                .paymentType("EFT")
                .status("PENDING")
                .createdAt(new Date())
                .build();
            
            // Store in test context
        }
    }

    /**
     * Set account daily limit
     */
    public void setAccountDailyLimit(String accountId, String dailyLimit) {
        accountDailyLimits.put(accountId, dailyLimit);
    }

    /**
     * Add account to sanctions list
     */
    public void addAccountToSanctionsList(String accountId) {
        sanctionsList.add(accountId);
    }

    /**
     * Configure fraud detection for high risk
     */
    public void configureFraudDetectionHighRisk(PaymentRequest request) {
        fraudDetectionConfig.put(request.getReference(), "HIGH_RISK");
    }

    /**
     * Configure fraud detection for low risk
     */
    public void configureFraudDetectionLowRisk(PaymentRequest request) {
        fraudDetectionConfig.put(request.getReference(), "LOW_RISK");
    }

    /**
     * Configure risk assessment for high risk
     */
    public void configureRiskAssessmentHighRisk(PaymentRequest request) {
        riskAssessmentConfig.put(request.getReference(), "HIGH_RISK");
    }

    /**
     * Configure risk assessment for low risk
     */
    public void configureRiskAssessmentLowRisk(PaymentRequest request) {
        riskAssessmentConfig.put(request.getReference(), "LOW_RISK");
    }

    /**
     * Configure payment within daily limits
     */
    public void configurePaymentWithinDailyLimits(PaymentRequest request) {
        // Implementation for configuring payment within daily limits
    }

    /**
     * Configure valid accounts
     */
    public void configureValidAccounts(PaymentRequest request) {
        // Implementation for configuring valid accounts
    }

    /**
     * Configure payment passes compliance
     */
    public void configurePaymentPassesCompliance(PaymentRequest request) {
        // Implementation for configuring payment passes compliance
    }

    /**
     * Configure multiple payments with different validation requirements
     */
    public void configureMultiplePaymentsWithDifferentValidationRequirements() {
        // Implementation for configuring multiple payments with different validation requirements
    }

    /**
     * Route payment to clearing system
     */
    public void routePaymentToClearing(PaymentRequest request, String clearingSystem) {
        request.setClearingSystem(clearingSystem);
    }

    /**
     * Configure multiple clearing routes
     */
    public void configureMultipleClearingRoutes(PaymentRequest request) {
        // Implementation for configuring multiple clearing routes
    }

    /**
     * Route all payments to clearing system
     */
    public void routeAllPaymentsToClearing(String clearingSystem) {
        // Implementation for routing all payments to clearing system
    }

    /**
     * Route payments to different clearing systems
     */
    public void routePaymentsToDifferentClearingSystems() {
        // Implementation for routing payments to different clearing systems
    }

    /**
     * Configure clearing system timeout
     */
    public void configureClearingSystemTimeout(String clearingSystem) {
        // Implementation for configuring clearing system timeout
    }

    /**
     * Configure clearing system rejection
     */
    public void configureClearingSystemRejection(String clearingSystem) {
        // Implementation for configuring clearing system rejection
    }

    /**
     * Configure clearing system completion
     */
    public void configureClearingSystemCompletion(String clearingSystem) {
        // Implementation for configuring clearing system completion
    }

    /**
     * Configure all payments ready for settlement
     */
    public void configureAllPaymentsReadyForSettlement() {
        // Implementation for configuring all payments ready for settlement
    }

    // Private helper methods for mock setup
    private void setupCoreBankingMocks() {
        // Setup WireMock stubs for core banking system
    }

    private void setupFraudApiMocks() {
        // Setup WireMock stubs for fraud API
    }

    private void setupSAMOSMocks() {
        // Setup WireMock stubs for SAMOS clearing system
    }

    private void setupRTCMocks() {
        // Setup WireMock stubs for RTC clearing system
    }

    private void setupPayShapMocks() {
        // Setup WireMock stubs for PayShap clearing system
    }

    private void setupSWIFTMocks() {
        // Setup WireMock stubs for SWIFT clearing system
    }

    // Getters for test data
    public TestTenant getTenant(String tenantId) {
        return tenants.get(tenantId);
    }

    public TestAccount getAccount(String accountId) {
        return accounts.get(accountId);
    }

    public String getAccountDailyLimit(String accountId) {
        return accountDailyLimits.get(accountId);
    }

    public boolean isAccountOnSanctionsList(String accountId) {
        return sanctionsList.contains(accountId);
    }

    public String getFraudDetectionRisk(String reference) {
        return (String) fraudDetectionConfig.get(reference);
    }

    public String getRiskAssessmentRisk(String reference) {
        return (String) riskAssessmentConfig.get(reference);
    }
}
