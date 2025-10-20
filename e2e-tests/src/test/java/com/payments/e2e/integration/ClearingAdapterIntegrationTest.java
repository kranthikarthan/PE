package com.payments.e2e.integration;

import com.payments.e2e.services.ClearingService;
import com.payments.e2e.services.NotificationService;
import com.payments.e2e.data.TestDataBuilder;
import com.payments.e2e.data.TestDataBuilder.PaymentRequest;
import com.payments.e2e.services.ClearingService.ClearingResponse;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Clearing Adapter Integration Tests
 * 
 * Tests the integration between clearing adapters and external clearing systems
 * using WireMock stubs for comprehensive E2E testing.
 */
@SpringBootTest
@ActiveProfiles("e2e")
@Tag("integration")
@Tag("clearing")
@DisplayName("Clearing Adapter Integration Tests")
public class ClearingAdapterIntegrationTest {

    @Autowired
    private ClearingService clearingService;
    
    @Autowired
    private NotificationService notificationService;
    
    @Autowired
    private TestDataBuilder testDataBuilder;

    @BeforeEach
    void setUp() {
        // Setup clearing system mocks
        testDataBuilder.setupClearingSystemMocks();
        
        // Clear previous notifications
        notificationService.clearNotifications();
    }

    @AfterEach
    void tearDown() {
        // Cleanup after each test
        notificationService.clearNotifications();
    }

    @Test
    @DisplayName("Test SAMOS Clearing Integration")
    void testSAMOSClearingIntegration() {
        // Given
        PaymentRequest request = createTestPaymentRequest();
        request.setClearingSystem("SAMOS");
        
        // When
        ClearingResponse response = clearingService.submitPaymentToClearing(request, "SAMOS");
        
        // Then
        assertNotNull(response);
        assertEquals(request.getPaymentId(), response.getPaymentId());
        assertEquals("SAMOS", response.getClearingSystem());
        assertEquals("SUBMITTED", response.getStatus());
        assertEquals("ACSC", response.getStatusCode());
        assertTrue(response.getClearingReference().startsWith("SAMOS-"));
        assertEquals(0.00, response.getFees());
        assertEquals("ZAR", response.getCurrency());
        
        // Verify notification was sent
        var notifications = notificationService.getNotificationsForPayment(request.getPaymentId());
        assertFalse(notifications.isEmpty());
    }

    @Test
    @DisplayName("Test RTC Clearing Integration")
    void testRTCClearingIntegration() {
        // Given
        PaymentRequest request = createTestPaymentRequest();
        request.setClearingSystem("RTC");
        
        // When
        ClearingResponse response = clearingService.submitPaymentToClearing(request, "RTC");
        
        // Then
        assertNotNull(response);
        assertEquals(request.getPaymentId(), response.getPaymentId());
        assertEquals("RTC", response.getClearingSystem());
        assertEquals("SUCCESS", response.getStatus());
        assertEquals("ACSC", response.getStatusCode());
        assertTrue(response.getClearingReference().startsWith("RTC-"));
        assertEquals(2.50, response.getFees());
        assertEquals("ZAR", response.getCurrency());
        
        // Verify notification was sent
        var notifications = notificationService.getNotificationsForPayment(request.getPaymentId());
        assertFalse(notifications.isEmpty());
    }

    @Test
    @DisplayName("Test PayShap Clearing Integration")
    void testPayShapClearingIntegration() {
        // Given
        PaymentRequest request = createTestPaymentRequest();
        request.setClearingSystem("PayShap");
        
        // When
        ClearingResponse response = clearingService.submitPaymentToClearing(request, "PayShap");
        
        // Then
        assertNotNull(response);
        assertEquals(request.getPaymentId(), response.getPaymentId());
        assertEquals("PayShap", response.getClearingSystem());
        assertEquals("SUCCESS", response.getStatus());
        assertEquals("ACSC", response.getStatusCode());
        assertTrue(response.getClearingReference().startsWith("PayShap-"));
        assertEquals(2.50, response.getFees());
        assertEquals("ZAR", response.getCurrency());
        
        // Verify notification was sent
        var notifications = notificationService.getNotificationsForPayment(request.getPaymentId());
        assertFalse(notifications.isEmpty());
    }

    @Test
    @DisplayName("Test SWIFT Clearing Integration")
    void testSWIFTClearingIntegration() {
        // Given
        PaymentRequest request = createTestPaymentRequest();
        request.setClearingSystem("SWIFT");
        request.setCurrency("USD"); // International payment
        
        // When
        ClearingResponse response = clearingService.submitPaymentToClearing(request, "SWIFT");
        
        // Then
        assertNotNull(response);
        assertEquals(request.getPaymentId(), response.getPaymentId());
        assertEquals("SWIFT", response.getClearingSystem());
        assertEquals("SUBMITTED", response.getStatus());
        assertEquals("ACSC", response.getStatusCode());
        assertTrue(response.getClearingReference().startsWith("SWIFT-"));
        assertEquals(15.00, response.getFees());
        assertEquals("USD", response.getCurrency());
        
        // Verify notification was sent
        var notifications = notificationService.getNotificationsForPayment(request.getPaymentId());
        assertFalse(notifications.isEmpty());
    }

    @Test
    @DisplayName("Test BankservAfrica Clearing Integration")
    void testBankservAfricaClearingIntegration() {
        // Given
        PaymentRequest request = createTestPaymentRequest();
        request.setClearingSystem("BankservAfrica");
        
        // When
        ClearingResponse response = clearingService.submitPaymentToClearing(request, "BankservAfrica");
        
        // Then
        assertNotNull(response);
        assertEquals(request.getPaymentId(), response.getPaymentId());
        assertEquals("BankservAfrica", response.getClearingSystem());
        assertEquals("SUBMITTED", response.getStatus());
        assertEquals("ACSC", response.getStatusCode());
        assertTrue(response.getClearingReference().startsWith("BankservAfrica-"));
        assertEquals(5.00, response.getFees());
        assertEquals("ZAR", response.getCurrency());
        
        // Verify notification was sent
        var notifications = notificationService.getNotificationsForPayment(request.getPaymentId());
        assertFalse(notifications.isEmpty());
    }

    @Test
    @DisplayName("Test Multiple Clearing Systems")
    void testMultipleClearingSystems() {
        // Given
        PaymentRequest request = createTestPaymentRequest();
        request.setMultipleClearingRoutes(true);
        
        // When - Test different clearing systems
        ClearingResponse samosResponse = clearingService.submitPaymentToClearing(request, "SAMOS");
        ClearingResponse rtcResponse = clearingService.submitPaymentToClearing(request, "RTC");
        ClearingResponse payshapResponse = clearingService.submitPaymentToClearing(request, "PayShap");
        
        // Then
        assertNotNull(samosResponse);
        assertNotNull(rtcResponse);
        assertNotNull(payshapResponse);
        
        assertEquals("SAMOS", samosResponse.getClearingSystem());
        assertEquals("RTC", rtcResponse.getClearingSystem());
        assertEquals("PayShap", payshapResponse.getClearingSystem());
        
        // Verify all notifications were sent
        var notifications = notificationService.getNotificationsForPayment(request.getPaymentId());
        assertFalse(notifications.isEmpty());
    }

    @Test
    @DisplayName("Test Clearing System Availability")
    void testClearingSystemAvailability() {
        // Given
        boolean allAvailable = clearingService.areAllAdaptersAvailable();
        
        // Then
        assertTrue(allAvailable, "All clearing adapters should be available");
        
        // Test individual adapters
        var availableSystems = clearingService.getAvailableClearingSystems();
        assertTrue(availableSystems.contains("SAMOS"));
        assertTrue(availableSystems.contains("RTC"));
        assertTrue(availableSystems.contains("PayShap"));
        assertTrue(availableSystems.contains("SWIFT"));
        assertTrue(availableSystems.contains("BankservAfrica"));
    }

    @Test
    @DisplayName("Test Clearing Event Notifications")
    void testClearingEventNotifications() {
        // Given
        String paymentId = "TEST-PAYMENT-001";
        String clearingSystem = "SAMOS";
        
        // When
        notificationService.sendClearingEvent(paymentId, clearingSystem, "SUBMITTED");
        notificationService.sendClearingEvent(paymentId, clearingSystem, "PROCESSED");
        notificationService.sendClearingEvent(paymentId, clearingSystem, "SETTLED");
        
        // Then
        var notifications = notificationService.getNotificationsForPayment(paymentId);
        assertEquals(3, notifications.size());
        
        assertTrue(notifications.stream().anyMatch(n -> n.getEventType().equals("CLEARING_SUBMITTED")));
        assertTrue(notifications.stream().anyMatch(n -> n.getEventType().equals("CLEARING_PROCESSED")));
        assertTrue(notifications.stream().anyMatch(n -> n.getEventType().equals("CLEARING_SETTLED")));
    }

    @Test
    @DisplayName("Test System Alert Notifications")
    void testSystemAlertNotifications() {
        // Given
        String alertType = "CLEARING_SYSTEM_DOWN";
        String message = "SAMOS clearing system is temporarily unavailable";
        
        // When
        notificationService.sendSystemAlert(alertType, message);
        
        // Then
        var notifications = notificationService.getNotificationsForPayment("SYSTEM");
        assertFalse(notifications.isEmpty());
        
        var alert = notifications.get(0);
        assertEquals("SYSTEM_ALERT_CLEARING_SYSTEM_DOWN", alert.getEventType());
        assertEquals(message, alert.getMessage());
    }

    private PaymentRequest createTestPaymentRequest() {
        PaymentRequest request = new PaymentRequest();
        request.setPaymentId("TEST-PAYMENT-" + System.currentTimeMillis());
        request.setIdempotencyKey("IDEMPOTENCY-" + System.currentTimeMillis());
        request.setFromAccount("12345678901");
        request.setToAccount("98765432109");
        request.setAmount(1000.00);
        request.setCurrency("ZAR");
        request.setReference("TEST-REF-" + System.currentTimeMillis());
        request.setTenantId("TENANT-TEST-001");
        request.setStatus("PROCESSED");
        return request;
    }
}
