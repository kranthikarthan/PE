package com.payments.e2e.services;

import com.payments.e2e.data.TestDataBuilder.PaymentRequest;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

/**
 * Clearing Service for E2E Tests
 * 
 * Simulates clearing system interactions for comprehensive E2E testing
 * including SAMOS, RTC, PayShap, SWIFT, and BankservAfrica.
 */
@Service
public class ClearingService {

    private static final Logger logger = LoggerFactory.getLogger(ClearingService.class);
    
    private Map<String, Boolean> adapterAvailability = new HashMap<>();
    private List<String> availableClearingSystems = new ArrayList<>();

    public ClearingService() {
        initializeClearingSystems();
    }

    private void initializeClearingSystems() {
        logger.info("Initializing clearing systems for E2E tests");
        
        // Initialize adapter availability
        adapterAvailability.put("SAMOS", true);
        adapterAvailability.put("RTC", true);
        adapterAvailability.put("PayShap", true);
        adapterAvailability.put("SWIFT", true);
        adapterAvailability.put("BankservAfrica", true);
        
        // Initialize available clearing systems
        availableClearingSystems.addAll(adapterAvailability.keySet());
    }

    public boolean areAllAdaptersAvailable() {
        logger.info("Checking adapter availability");
        
        boolean allAvailable = adapterAvailability.values().stream()
            .allMatch(available -> available);
            
        logger.info("All adapters available: {}", allAvailable);
        return allAvailable;
    }

    public ClearingResponse submitPaymentToClearing(PaymentRequest request, String clearingSystem) {
        logger.info("Submitting payment {} to {} clearing system", 
            request.getPaymentId(), clearingSystem);
        
        // Simulate clearing system processing
        ClearingResponse response = new ClearingResponse();
        response.setPaymentId(request.getPaymentId());
        response.setClearingSystem(clearingSystem);
        response.setProcessingTime(System.currentTimeMillis());
        
        // Simulate different outcomes based on clearing system
        switch (clearingSystem.toUpperCase()) {
            case "SAMOS":
                return processSAMOSPayment(request, response);
            case "RTC":
                return processRTCPayment(request, response);
            case "PAYSHAP":
                return processPayShapPayment(request, response);
            case "SWIFT":
                return processSWIFTPayment(request, response);
            case "BANKSERVAFRICA":
                return processBankservAfricaPayment(request, response);
            default:
                return processGenericPayment(request, response);
        }
    }

    private ClearingResponse processSAMOSPayment(PaymentRequest request, ClearingResponse response) {
        logger.info("Processing SAMOS payment: {}", request.getPaymentId());
        
        // Simulate SAMOS RTGS processing
        response.setStatus("SUBMITTED");
        response.setStatusCode("ACSC");
        response.setStatusDescription("Payment submitted to SAMOS RTGS");
        response.setSettlementTime(System.currentTimeMillis() + 30000); // 30 seconds
        
        // Simulate SAMOS-specific fields
        response.setClearingReference("SAMOS-" + System.currentTimeMillis());
        response.setFees(0.00);
        response.setCurrency("ZAR");
        
        logger.info("SAMOS payment processed: {} - {}", 
            response.getStatus(), response.getStatusDescription());
        
        return response;
    }

    private ClearingResponse processRTCPayment(PaymentRequest request, ClearingResponse response) {
        logger.info("Processing RTC payment: {}", request.getPaymentId());
        
        // Simulate RTC real-time clearing
        response.setStatus("SUCCESS");
        response.setStatusCode("ACSC");
        response.setStatusDescription("RTC payment successfully processed");
        response.setSettlementTime(System.currentTimeMillis() + 5000); // 5 seconds
        
        // Simulate RTC-specific fields
        response.setClearingReference("RTC-" + System.currentTimeMillis());
        response.setFees(2.50);
        response.setCurrency("ZAR");
        
        logger.info("RTC payment processed: {} - {}", 
            response.getStatus(), response.getStatusDescription());
        
        return response;
    }

    private ClearingResponse processPayShapPayment(PaymentRequest request, ClearingResponse response) {
        logger.info("Processing PayShap payment: {}", request.getPaymentId());
        
        // Simulate PayShap P2P processing
        response.setStatus("SUCCESS");
        response.setStatusCode("ACSC");
        response.setStatusDescription("PayShap P2P payment successfully processed");
        response.setSettlementTime(System.currentTimeMillis() + 10000); // 10 seconds
        
        // Simulate PayShap-specific fields
        response.setClearingReference("PayShap-" + System.currentTimeMillis());
        response.setFees(2.50);
        response.setCurrency("ZAR");
        
        logger.info("PayShap payment processed: {} - {}", 
            response.getStatus(), response.getStatusDescription());
        
        return response;
    }

    private ClearingResponse processSWIFTPayment(PaymentRequest request, ClearingResponse response) {
        logger.info("Processing SWIFT payment: {}", request.getPaymentId());
        
        // Simulate SWIFT messaging
        response.setStatus("SUBMITTED");
        response.setStatusCode("ACSC");
        response.setStatusDescription("SWIFT message submitted for international processing");
        response.setSettlementTime(System.currentTimeMillis() + 300000); // 5 minutes
        
        // Simulate SWIFT-specific fields
        response.setClearingReference("SWIFT-" + System.currentTimeMillis());
        response.setFees(15.00);
        response.setCurrency(request.getCurrency());
        
        logger.info("SWIFT payment processed: {} - {}", 
            response.getStatus(), response.getStatusDescription());
        
        return response;
    }

    private ClearingResponse processBankservAfricaPayment(PaymentRequest request, ClearingResponse response) {
        logger.info("Processing BankservAfrica payment: {}", request.getPaymentId());
        
        // Simulate BankservAfrica ACH/EFT processing
        response.setStatus("SUBMITTED");
        response.setStatusCode("ACSC");
        response.setStatusDescription("Payment submitted to BankservAfrica ACH");
        response.setSettlementTime(System.currentTimeMillis() + 3600000); // 1 hour
        
        // Simulate BankservAfrica-specific fields
        response.setClearingReference("BankservAfrica-" + System.currentTimeMillis());
        response.setFees(5.00);
        response.setCurrency("ZAR");
        
        logger.info("BankservAfrica payment processed: {} - {}", 
            response.getStatus(), response.getStatusDescription());
        
        return response;
    }

    private ClearingResponse processGenericPayment(PaymentRequest request, ClearingResponse response) {
        logger.info("Processing generic payment: {}", request.getPaymentId());
        
        response.setStatus("SUBMITTED");
        response.setStatusCode("ACSC");
        response.setStatusDescription("Payment submitted to clearing system");
        response.setSettlementTime(System.currentTimeMillis() + 60000); // 1 minute
        
        response.setClearingReference("CLEARING-" + System.currentTimeMillis());
        response.setFees(0.00);
        response.setCurrency(request.getCurrency());
        
        return response;
    }

    public List<String> getAvailableClearingSystems() {
        return new ArrayList<>(availableClearingSystems);
    }

    public void setAdapterAvailability(String adapter, boolean available) {
        adapterAvailability.put(adapter, available);
        logger.info("Adapter {} availability set to: {}", adapter, available);
    }

    // Inner class for clearing response
    public static class ClearingResponse {
        private String paymentId;
        private String clearingSystem;
        private String status;
        private String statusCode;
        private String statusDescription;
        private long processingTime;
        private long settlementTime;
        private String clearingReference;
        private double fees;
        private String currency;

        // Getters and setters
        public String getPaymentId() { return paymentId; }
        public void setPaymentId(String paymentId) { this.paymentId = paymentId; }
        
        public String getClearingSystem() { return clearingSystem; }
        public void setClearingSystem(String clearingSystem) { this.clearingSystem = clearingSystem; }
        
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        
        public String getStatusCode() { return statusCode; }
        public void setStatusCode(String statusCode) { this.statusCode = statusCode; }
        
        public String getStatusDescription() { return statusDescription; }
        public void setStatusDescription(String statusDescription) { this.statusDescription = statusDescription; }
        
        public long getProcessingTime() { return processingTime; }
        public void setProcessingTime(long processingTime) { this.processingTime = processingTime; }
        
        public long getSettlementTime() { return settlementTime; }
        public void setSettlementTime(long settlementTime) { this.settlementTime = settlementTime; }
        
        public String getClearingReference() { return clearingReference; }
        public void setClearingReference(String clearingReference) { this.clearingReference = clearingReference; }
        
        public double getFees() { return fees; }
        public void setFees(double fees) { this.fees = fees; }
        
        public String getCurrency() { return currency; }
        public void setCurrency(String currency) { this.currency = currency; }
    }
}
