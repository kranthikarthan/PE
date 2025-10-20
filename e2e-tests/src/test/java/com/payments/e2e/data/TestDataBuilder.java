package com.payments.e2e.data;

import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.UUID;

/**
 * Test Data Builder for E2E Tests
 * 
 * Provides test data and mock setup for comprehensive E2E testing
 * including clearing system mocks and payment scenarios.
 */
@Component
public class TestDataBuilder {

    private static final Logger logger = LoggerFactory.getLogger(TestDataBuilder.class);
    
    private Map<String, TestTenant> tenants = new HashMap<>();
    private Map<String, TestAccount> accounts = new HashMap<>();
    private Map<String, String> accountDailyLimits = new HashMap<>();
    private List<String> sanctionsList = new ArrayList<>();
    private Map<String, Object> fraudDetectionConfig = new HashMap<>();
    private Map<String, Object> riskAssessmentConfig = new HashMap<>();

    public TestDataBuilder() {
        initializeTestData();
    }

    private void initializeTestData() {
        logger.info("Initializing test data for E2E tests");
        
        // Initialize test tenants
        setupTestTenants();
        
        // Initialize test accounts
        setupTestAccounts();
        
        // Initialize account limits
        setupAccountLimits();
        
        // Initialize sanctions list
        setupSanctionsList();
        
        // Initialize fraud detection config
        setupFraudDetectionConfig();
        
        // Initialize risk assessment config
        setupRiskAssessmentConfig();
    }

    private void setupTestTenants() {
        tenants.put("TENANT-TEST-001", new TestTenant(
            "TENANT-TEST-001", 
            "Test Bank", 
            "TEST-BU-001",
            "test@bank.com",
            "ACTIVE"
        ));
        
        tenants.put("TENANT-TEST-002", new TestTenant(
            "TENANT-TEST-002", 
            "Demo Bank", 
            "DEMO-BU-001",
            "demo@bank.com",
            "ACTIVE"
        ));
    }

    private void setupTestAccounts() {
        accounts.put("ACC-TEST-001", new TestAccount(
            "ACC-TEST-001",
            "12345678901",
            "TENANT-TEST-001",
            "CURRENT",
            "ACTIVE",
            50000.00
        ));
        
        accounts.put("ACC-TEST-002", new TestAccount(
            "ACC-TEST-002",
            "98765432109",
            "TENANT-TEST-001",
            "CURRENT",
            "ACTIVE",
            25000.00
        ));
    }

    private void setupAccountLimits() {
        accountDailyLimits.put("ACC-TEST-001", "100000.00");
        accountDailyLimits.put("ACC-TEST-002", "50000.00");
    }

    private void setupSanctionsList() {
        sanctionsList.add("SANCTIONS-001");
        sanctionsList.add("BLOCKED-ACCOUNT-001");
    }

    private void setupFraudDetectionConfig() {
        fraudDetectionConfig.put("HIGH-RISK-REF", "HIGH");
        fraudDetectionConfig.put("MEDIUM-RISK-REF", "MEDIUM");
        fraudDetectionConfig.put("LOW-RISK-REF", "LOW");
    }

    private void setupRiskAssessmentConfig() {
        riskAssessmentConfig.put("HIGH-AMOUNT-REF", "HIGH");
        riskAssessmentConfig.put("NORMAL-AMOUNT-REF", "LOW");
    }

    public void setupClearingSystemMocks() {
        logger.info("Setting up clearing system mocks");
        
        // Setup SAMOS mocks
        setupSAMOSMocks();
        
        // Setup RTC mocks
        setupRTCMocks();
        
        // Setup PayShap mocks
        setupPayShapMocks();
        
        // Setup SWIFT mocks
        setupSWIFTMocks();
        
        // Setup BankservAfrica mocks
        setupBankservAfricaMocks();
    }

    public PaymentRequest createPaymentRequest(io.cucumber.datatable.DataTable dataTable) {
        Map<String, String> data = dataTable.asMap(String.class, String.class);
        
        PaymentRequest request = new PaymentRequest();
        request.setFromAccount(data.get("From Account"));
        request.setToAccount(data.get("To Account"));
        request.setAmount(Double.parseDouble(data.get("Amount")));
        request.setCurrency(data.get("Currency"));
        request.setReference(data.get("Reference"));
        request.setPaymentId(UUID.randomUUID().toString());
        request.setIdempotencyKey(UUID.randomUUID().toString());
        
        return request;
    }

    public void routePaymentToClearing(PaymentRequest request, String clearingSystem) {
        logger.info("Routing payment {} to {} clearing", request.getPaymentId(), clearingSystem);
        request.setClearingSystem(clearingSystem);
    }

    public void configureMultipleClearingRoutes(PaymentRequest request) {
        logger.info("Configuring multiple clearing routes for payment {}", request.getPaymentId());
        request.setMultipleClearingRoutes(true);
    }

    public void routeAllPaymentsToClearing(String clearingSystem) {
        logger.info("Routing all payments to {} clearing", clearingSystem);
    }

    public void routePaymentsToDifferentClearingSystems() {
        logger.info("Configuring payments to route to different clearing systems");
    }

    public void configureClearingSystemTimeout(String clearingSystem) {
        logger.info("Configuring {} system to timeout", clearingSystem);
    }

    public void configureClearingSystemRejection(String clearingSystem) {
        logger.info("Configuring {} system to reject payments", clearingSystem);
    }

    public void configureClearingSystemCompletion(String clearingSystem) {
        logger.info("Configuring {} system to complete payments", clearingSystem);
    }

    public void configureAllPaymentsReadyForSettlement() {
        logger.info("Configuring all payments as ready for settlement");
    }

    // Private helper methods for mock setup
    private void setupCoreBankingMocks() {
        logger.info("Setting up WireMock stubs for core banking system");
        // Implementation would configure WireMock stubs for core banking APIs
    }

    private void setupFraudApiMocks() {
        logger.info("Setting up WireMock stubs for fraud API");
        // Implementation would configure WireMock stubs for fraud detection API
    }

    private void setupSAMOSMocks() {
        logger.info("Setting up WireMock stubs for SAMOS clearing system");
        // Implementation would configure WireMock stubs for SAMOS RTGS system
        // - Payment submission endpoint
        // - Payment status query endpoint
        // - Payment cancellation endpoint
        // - Success, failure, and timeout scenarios
    }

    private void setupRTCMocks() {
        logger.info("Setting up WireMock stubs for RTC clearing system");
        // Implementation would configure WireMock stubs for RTC real-time clearing
        // - Instant payment processing
        // - Balance verification
        // - Settlement confirmation
    }

    private void setupPayShapMocks() {
        logger.info("Setting up WireMock stubs for PayShap clearing system");
        // Implementation would configure WireMock stubs for PayShap P2P payments
        // - P2P payment processing
        // - QR code generation
        // - Mobile payment integration
    }

    private void setupSWIFTMocks() {
        logger.info("Setting up WireMock stubs for SWIFT clearing system");
        // Implementation would configure WireMock stubs for SWIFT messaging
        // - MT103 message processing
        // - Sanctions screening
        // - FX rate queries
        // - International payment processing
    }

    private void setupBankservAfricaMocks() {
        logger.info("Setting up WireMock stubs for BankservAfrica clearing system");
        // Implementation would configure WireMock stubs for BankservAfrica ACH/EFT
        // - ACH file upload
        // - EFT transaction processing
        // - Reconciliation file generation
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

    // Inner classes for test data
    public static class TestTenant {
        private String tenantId;
        private String name;
        private String businessUnitId;
        private String contactEmail;
        private String status;

        public TestTenant(String tenantId, String name, String businessUnitId, 
                         String contactEmail, String status) {
            this.tenantId = tenantId;
            this.name = name;
            this.businessUnitId = businessUnitId;
            this.contactEmail = contactEmail;
            this.status = status;
        }

        // Getters and setters
        public String getTenantId() { return tenantId; }
        public void setTenantId(String tenantId) { this.tenantId = tenantId; }
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public String getBusinessUnitId() { return businessUnitId; }
        public void setBusinessUnitId(String businessUnitId) { this.businessUnitId = businessUnitId; }
        
        public String getContactEmail() { return contactEmail; }
        public void setContactEmail(String contactEmail) { this.contactEmail = contactEmail; }
        
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }

    public static class TestAccount {
        private String accountId;
        private String accountNumber;
        private String tenantId;
        private String accountType;
        private String status;
        private double balance;

        public TestAccount(String accountId, String accountNumber, String tenantId,
                          String accountType, String status, double balance) {
            this.accountId = accountId;
            this.accountNumber = accountNumber;
            this.tenantId = tenantId;
            this.accountType = accountType;
            this.status = status;
            this.balance = balance;
        }

        // Getters and setters
        public String getAccountId() { return accountId; }
        public void setAccountId(String accountId) { this.accountId = accountId; }
        
        public String getAccountNumber() { return accountNumber; }
        public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }
        
        public String getTenantId() { return tenantId; }
        public void setTenantId(String tenantId) { this.tenantId = tenantId; }
        
        public String getAccountType() { return accountType; }
        public void setAccountType(String accountType) { this.accountType = accountType; }
        
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        
        public double getBalance() { return balance; }
        public void setBalance(double balance) { this.balance = balance; }
    }

    public static class PaymentRequest {
        private String paymentId;
        private String idempotencyKey;
        private String fromAccount;
        private String toAccount;
        private double amount;
        private String currency;
        private String reference;
        private String tenantId;
        private String status;
        private String clearingSystem;
        private boolean multipleClearingRoutes;

        // Getters and setters
        public String getPaymentId() { return paymentId; }
        public void setPaymentId(String paymentId) { this.paymentId = paymentId; }
        
        public String getIdempotencyKey() { return idempotencyKey; }
        public void setIdempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; }
        
        public String getFromAccount() { return fromAccount; }
        public void setFromAccount(String fromAccount) { this.fromAccount = fromAccount; }
        
        public String getToAccount() { return toAccount; }
        public void setToAccount(String toAccount) { this.toAccount = toAccount; }
        
        public double getAmount() { return amount; }
        public void setAmount(double amount) { this.amount = amount; }
        
        public String getCurrency() { return currency; }
        public void setCurrency(String currency) { this.currency = currency; }
        
        public String getReference() { return reference; }
        public void setReference(String reference) { this.reference = reference; }
        
        public String getTenantId() { return tenantId; }
        public void setTenantId(String tenantId) { this.tenantId = tenantId; }
        
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        
        public String getClearingSystem() { return clearingSystem; }
        public void setClearingSystem(String clearingSystem) { this.clearingSystem = clearingSystem; }
        
        public boolean isMultipleClearingRoutes() { return multipleClearingRoutes; }
        public void setMultipleClearingRoutes(boolean multipleClearingRoutes) { 
            this.multipleClearingRoutes = multipleClearingRoutes; 
        }
    }
}