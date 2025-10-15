package com.payments.paymentinitiation.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.payments.contracts.payment.PaymentInitiationRequest;
import com.payments.contracts.payment.PaymentInitiationResponse;
import com.payments.contracts.payment.PaymentStatus;
import com.payments.domain.payment.PaymentId;
import com.payments.domain.shared.Money;
import com.payments.domain.shared.TenantContext;
import com.payments.paymentinitiation.service.PaymentInitiationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for PaymentInitiationController
 * 
 * Tests REST endpoints with mocked service layer
 */
@WebMvcTest(PaymentInitiationController.class)
class PaymentInitiationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PaymentInitiationService paymentInitiationService;

    @Test
    void initiatePayment_ShouldReturn201_WhenValidRequest() throws Exception {
        // Given
        PaymentInitiationRequest request = createValidPaymentRequest();
        PaymentInitiationResponse response = createValidPaymentResponse();
        
        when(paymentInitiationService.initiatePayment(any(PaymentInitiationRequest.class), anyString(), anyString(), anyString()))
                .thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/v1/payments/initiate")
                .header("X-Correlation-ID", UUID.randomUUID().toString())
                .header("X-Tenant-ID", "TENANT-001")
                .header("X-Business-Unit-ID", "BU-001")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.paymentId").exists())
                .andExpect(jsonPath("$.status").value("INITIATED"))
                .andExpect(jsonPath("$.tenantContext").exists())
                .andExpect(jsonPath("$.initiatedAt").exists());
    }

    @Test
    void initiatePayment_ShouldReturn400_WhenInvalidRequest() throws Exception {
        // Given
        PaymentInitiationRequest request = createInvalidPaymentRequest();
        
        when(paymentInitiationService.initiatePayment(any(PaymentInitiationRequest.class), anyString(), anyString(), anyString()))
                .thenThrow(new IllegalArgumentException("Invalid payment request"));

        // When & Then
        mockMvc.perform(post("/api/v1/payments/initiate")
                .header("X-Correlation-ID", UUID.randomUUID().toString())
                .header("X-Tenant-ID", "TENANT-001")
                .header("X-Business-Unit-ID", "BU-001")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorMessage").value("Invalid payment request"));
    }

    @Test
    void getPaymentStatus_ShouldReturn200_WhenPaymentExists() throws Exception {
        // Given
        String paymentId = "PAY-001";
        PaymentInitiationResponse response = createValidPaymentResponse();
        
        when(paymentInitiationService.getPaymentStatus(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(response);

        // When & Then
        mockMvc.perform(get("/api/v1/payments/{paymentId}/status", paymentId)
                .header("X-Correlation-ID", UUID.randomUUID().toString())
                .header("X-Tenant-ID", "TENANT-001")
                .header("X-Business-Unit-ID", "BU-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentId").exists())
                .andExpect(jsonPath("$.status").value("INITIATED"));
    }

    @Test
    void getPaymentStatus_ShouldReturn404_WhenPaymentNotFound() throws Exception {
        // Given
        String paymentId = "PAY-001";
        
        when(paymentInitiationService.getPaymentStatus(anyString(), anyString(), anyString(), anyString()))
                .thenThrow(new IllegalArgumentException("Payment not found: PAY-001"));

        // When & Then
        mockMvc.perform(get("/api/v1/payments/{paymentId}/status", paymentId)
                .header("X-Correlation-ID", UUID.randomUUID().toString())
                .header("X-Tenant-ID", "TENANT-001")
                .header("X-Business-Unit-ID", "BU-001"))
                .andExpect(status().isNotFound());
    }

    @Test
    void validatePayment_ShouldReturn200_WhenValidationSucceeds() throws Exception {
        // Given
        String paymentId = "PAY-001";
        PaymentInitiationResponse response = createValidatedPaymentResponse();
        
        when(paymentInitiationService.validatePayment(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/v1/payments/{paymentId}/validate", paymentId)
                .header("X-Correlation-ID", UUID.randomUUID().toString())
                .header("X-Tenant-ID", "TENANT-001")
                .header("X-Business-Unit-ID", "BU-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentId").exists())
                .andExpect(jsonPath("$.status").value("VALIDATED"));
    }

    @Test
    void failPayment_ShouldReturn200_WhenPaymentFails() throws Exception {
        // Given
        String paymentId = "PAY-001";
        String reason = "Insufficient funds";
        PaymentInitiationResponse response = createFailedPaymentResponse();
        
        when(paymentInitiationService.failPayment(anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/v1/payments/{paymentId}/fail", paymentId)
                .header("X-Correlation-ID", UUID.randomUUID().toString())
                .header("X-Tenant-ID", "TENANT-001")
                .header("X-Business-Unit-ID", "BU-001")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"reason\":\"" + reason + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentId").exists())
                .andExpect(jsonPath("$.status").value("FAILED"))
                .andExpect(jsonPath("$.errorMessage").value(reason));
    }

    @Test
    void completePayment_ShouldReturn200_WhenPaymentCompletes() throws Exception {
        // Given
        String paymentId = "PAY-001";
        PaymentInitiationResponse response = createCompletedPaymentResponse();
        
        when(paymentInitiationService.completePayment(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/v1/payments/{paymentId}/complete", paymentId)
                .header("X-Correlation-ID", UUID.randomUUID().toString())
                .header("X-Tenant-ID", "TENANT-001")
                .header("X-Business-Unit-ID", "BU-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentId").exists())
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    void getPaymentHistory_ShouldReturn200_WhenHistoryExists() throws Exception {
        // Given
        PaymentInitiationResponse response = createValidPaymentResponse();
        
        when(paymentInitiationService.getPaymentHistory(anyString(), anyString(), anyString()))
                .thenReturn(java.util.List.of(response));

        // When & Then
        mockMvc.perform(get("/api/v1/payments/history")
                .header("X-Correlation-ID", UUID.randomUUID().toString())
                .header("X-Tenant-ID", "TENANT-001")
                .header("X-Business-Unit-ID", "BU-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.payments").isArray())
                .andExpect(jsonPath("$.totalCount").value(1));
    }

    @Test
    void initiatePayment_ShouldReturn400_WhenMissingHeaders() throws Exception {
        // Given
        PaymentInitiationRequest request = createValidPaymentRequest();

        // When & Then
        mockMvc.perform(post("/api/v1/payments/initiate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    private PaymentInitiationRequest createValidPaymentRequest() {
        return PaymentInitiationRequest.builder()
                .paymentId(new PaymentId("PAY-001"))
                .idempotencyKey("IDEMPOTENCY-001")
                .sourceAccount("12345678901")
                .destinationAccount("98765432109")
                .amount(new Money(BigDecimal.valueOf(1000.00), "ZAR"))
                .reference("Test payment")
                .paymentType(com.payments.contracts.payment.PaymentType.EFT)
                .priority(com.payments.contracts.payment.Priority.NORMAL)
                .tenantContext(TenantContext.builder()
                        .tenantId("TENANT-001")
                        .businessUnitId("BU-001")
                        .build())
                .initiatedBy("user@example.com")
                .build();
    }

    private PaymentInitiationRequest createInvalidPaymentRequest() {
        return PaymentInitiationRequest.builder()
                .paymentId(new PaymentId("PAY-001"))
                .idempotencyKey("") // Invalid: empty idempotency key
                .sourceAccount("12345678901")
                .destinationAccount("98765432109")
                .amount(new Money(BigDecimal.valueOf(1000.00), "ZAR"))
                .reference("Test payment")
                .paymentType(com.payments.contracts.payment.PaymentType.EFT)
                .priority(com.payments.contracts.payment.Priority.NORMAL)
                .tenantContext(TenantContext.builder()
                        .tenantId("TENANT-001")
                        .businessUnitId("BU-001")
                        .build())
                .initiatedBy("user@example.com")
                .build();
    }

    private PaymentInitiationResponse createValidPaymentResponse() {
        return PaymentInitiationResponse.builder()
                .paymentId(new PaymentId("PAY-001"))
                .status(PaymentStatus.INITIATED)
                .tenantContext(TenantContext.builder()
                        .tenantId("TENANT-001")
                        .businessUnitId("BU-001")
                        .build())
                .initiatedAt(Instant.now())
                .build();
    }

    private PaymentInitiationResponse createValidatedPaymentResponse() {
        return PaymentInitiationResponse.builder()
                .paymentId(new PaymentId("PAY-001"))
                .status(PaymentStatus.VALIDATED)
                .tenantContext(TenantContext.builder()
                        .tenantId("TENANT-001")
                        .businessUnitId("BU-001")
                        .build())
                .initiatedAt(Instant.now())
                .build();
    }

    private PaymentInitiationResponse createFailedPaymentResponse() {
        return PaymentInitiationResponse.builder()
                .paymentId(new PaymentId("PAY-001"))
                .status(PaymentStatus.FAILED)
                .tenantContext(TenantContext.builder()
                        .tenantId("TENANT-001")
                        .businessUnitId("BU-001")
                        .build())
                .initiatedAt(Instant.now())
                .errorMessage("Insufficient funds")
                .build();
    }

    private PaymentInitiationResponse createCompletedPaymentResponse() {
        return PaymentInitiationResponse.builder()
                .paymentId(new PaymentId("PAY-001"))
                .status(PaymentStatus.COMPLETED)
                .tenantContext(TenantContext.builder()
                        .tenantId("TENANT-001")
                        .businessUnitId("BU-001")
                        .build())
                .initiatedAt(Instant.now())
                .build();
    }
}
