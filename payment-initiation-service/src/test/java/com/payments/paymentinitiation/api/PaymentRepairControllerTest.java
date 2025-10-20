package com.payments.paymentinitiation.api;

import com.payments.contracts.payment.PaymentInitiationResponse;
import com.payments.paymentinitiation.service.PaymentRepairService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Payment Repair Controller Test
 * 
 * Comprehensive test suite for the Payment Repair REST API.
 * Tests all endpoints with proper authentication and authorization.
 */
@WebMvcTest(PaymentRepairController.class)
class PaymentRepairControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PaymentRepairService paymentRepairService;

    @Test
    @WithMockUser(roles = "OPS_OPERATOR")
    void testRetryPayment_ShouldReturnSuccess() throws Exception {
        // Given
        PaymentRepairController.RetryRequest request = PaymentRepairController.RetryRequest.builder()
            .reason("System error resolved")
            .forceRetry(false)
            .build();

        PaymentInitiationResponse mockResponse = PaymentInitiationResponse.builder()
            .paymentId("payment-123")
            .status("RETRY_INITIATED")
            .message("Payment retry initiated successfully")
            .build();
        
        when(paymentRepairService.retryPayment(anyString(), any(), anyString(), anyString(), anyString(), anyString()))
            .thenReturn(mockResponse);

        // When & Then
        mockMvc.perform(post("/api/repair/v1/payments/payment-123/retry")
                .header("X-User-ID", "operator")
                .header("X-Correlation-ID", "corr-123")
                .header("X-Tenant-ID", "tenant-123")
                .header("X-Business-Unit-ID", "bu-123")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.paymentId").value("payment-123"))
            .andExpect(jsonPath("$.status").value("RETRY_INITIATED"))
            .andExpect(jsonPath("$.message").value("Payment retry initiated successfully"));
    }

    @Test
    @WithMockUser(roles = "OPS_OPERATOR")
    void testCancelPayment_ShouldReturnSuccess() throws Exception {
        // Given
        PaymentRepairController.CancelRequest request = PaymentRepairController.CancelRequest.builder()
            .reason("Customer requested cancellation")
            .forceCancel(false)
            .build();

        PaymentInitiationResponse mockResponse = PaymentInitiationResponse.builder()
            .paymentId("payment-123")
            .status("CANCELLED")
            .message("Payment cancelled successfully")
            .build();
        
        when(paymentRepairService.cancelPayment(anyString(), any(), anyString(), anyString(), anyString(), anyString()))
            .thenReturn(mockResponse);

        // When & Then
        mockMvc.perform(post("/api/repair/v1/payments/payment-123/cancel")
                .header("X-User-ID", "operator")
                .header("X-Correlation-ID", "corr-123")
                .header("X-Tenant-ID", "tenant-123")
                .header("X-Business-Unit-ID", "bu-123")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.paymentId").value("payment-123"))
            .andExpect(jsonPath("$.status").value("CANCELLED"))
            .andExpect(jsonPath("$.message").value("Payment cancelled successfully"));
    }

    @Test
    @WithMockUser(roles = "OPS_VIEWER")
    void testGetPaymentRepairHistory_ShouldReturnHistory() throws Exception {
        // Given
        List<PaymentRepairController.RepairHistoryItem> mockHistory = List.of(
            PaymentRepairController.RepairHistoryItem.builder()
                .action("RETRY")
                .timestamp(Instant.now().toString())
                .performedBy("operator")
                .reason("System error resolved")
                .result("SUCCESS")
                .build()
        );
        
        PaymentRepairController.RepairHistoryResponse mockResponse = PaymentRepairController.RepairHistoryResponse.builder()
            .paymentId("payment-123")
            .repairHistory(mockHistory)
            .totalCount(1)
            .build();
        
        when(paymentRepairService.getPaymentRepairHistory(anyString(), anyString(), anyString(), anyString()))
            .thenReturn(mockHistory);

        // When & Then
        mockMvc.perform(get("/api/repair/v1/payments/payment-123/repair-history")
                .header("X-Correlation-ID", "corr-123")
                .header("X-Tenant-ID", "tenant-123")
                .header("X-Business-Unit-ID", "bu-123")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.paymentId").value("payment-123"))
            .andExpect(jsonPath("$.repairHistory").isArray())
            .andExpect(jsonPath("$.repairHistory[0].action").value("RETRY"))
            .andExpect(jsonPath("$.repairHistory[0].performedBy").value("operator"))
            .andExpect(jsonPath("$.totalCount").value(1));
    }

    @Test
    @WithMockUser(roles = "OPS_VIEWER")
    void testGetFailedPayments_ShouldReturnFailedPayments() throws Exception {
        // Given
        List<PaymentInitiationResponse> mockFailedPayments = List.of(
            PaymentInitiationResponse.builder()
                .paymentId("payment-123")
                .status("FAILED")
                .amount(100.00)
                .currency("USD")
                .build(),
            PaymentInitiationResponse.builder()
                .paymentId("payment-456")
                .status("TIMEOUT")
                .amount(200.00)
                .currency("USD")
                .build()
        );
        
        when(paymentRepairService.getFailedPayments(any(), any(), anyString(), anyString(), anyString()))
            .thenReturn(mockFailedPayments);

        // When & Then
        mockMvc.perform(get("/api/repair/v1/payments/failed")
                .param("page", "0")
                .param("size", "20")
                .header("X-Correlation-ID", "corr-123")
                .header("X-Tenant-ID", "tenant-123")
                .header("X-Business-Unit-ID", "bu-123")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.failedPayments").isArray())
            .andExpect(jsonPath("$.failedPayments[0].paymentId").value("payment-123"))
            .andExpect(jsonPath("$.failedPayments[0].status").value("FAILED"))
            .andExpect(jsonPath("$.failedPayments[1].paymentId").value("payment-456"))
            .andExpect(jsonPath("$.failedPayments[1].status").value("TIMEOUT"))
            .andExpect(jsonPath("$.totalCount").value(2));
    }

    @Test
    @WithMockUser(roles = "OPS_ADMIN")
    void testBulkRetryPayments_ShouldReturnSuccess() throws Exception {
        // Given
        PaymentRepairController.BulkRetryRequest request = PaymentRepairController.BulkRetryRequest.builder()
            .paymentIds(List.of("payment-123", "payment-456"))
            .reason("Bulk retry after system maintenance")
            .forceRetry(false)
            .build();

        PaymentRepairController.BulkRetryResponse mockResponse = PaymentRepairController.BulkRetryResponse.builder()
            .totalProcessed(2)
            .successCount(2)
            .failureCount(0)
            .failedPaymentIds(List.of())
            .build();
        
        when(paymentRepairService.bulkRetryPayments(any(), anyString(), anyString(), anyString(), anyString()))
            .thenReturn(mockResponse);

        // When & Then
        mockMvc.perform(post("/api/repair/v1/payments/bulk-retry")
                .header("X-User-ID", "admin")
                .header("X-Correlation-ID", "corr-123")
                .header("X-Tenant-ID", "tenant-123")
                .header("X-Business-Unit-ID", "bu-123")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalProcessed").value(2))
            .andExpect(jsonPath("$.successCount").value(2))
            .andExpect(jsonPath("$.failureCount").value(0))
            .andExpect(jsonPath("$.failedPaymentIds").isEmpty());
    }

    @Test
    @WithMockUser(roles = "OPS_OPERATOR")
    void testRetryPayment_WithInvalidRequest_ShouldReturnBadRequest() throws Exception {
        // Given
        PaymentRepairController.RetryRequest request = PaymentRepairController.RetryRequest.builder()
            .reason("") // Empty reason should fail validation
            .forceRetry(false)
            .build();

        // When & Then
        mockMvc.perform(post("/api/repair/v1/payments/payment-123/retry")
                .header("X-User-ID", "operator")
                .header("X-Correlation-ID", "corr-123")
                .header("X-Tenant-ID", "tenant-123")
                .header("X-Business-Unit-ID", "bu-123")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .with(csrf()))
            .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "OPS_OPERATOR")
    void testRetryPayment_WithPaymentNotFound_ShouldReturnNotFound() throws Exception {
        // Given
        PaymentRepairController.RetryRequest request = PaymentRepairController.RetryRequest.builder()
            .reason("System error resolved")
            .forceRetry(false)
            .build();

        when(paymentRepairService.retryPayment(anyString(), any(), anyString(), anyString(), anyString(), anyString()))
            .thenThrow(new IllegalArgumentException("Payment not found: payment-123"));

        // When & Then
        mockMvc.perform(post("/api/repair/v1/payments/payment-123/retry")
                .header("X-User-ID", "operator")
                .header("X-Correlation-ID", "corr-123")
                .header("X-Tenant-ID", "tenant-123")
                .header("X-Business-Unit-ID", "bu-123")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .with(csrf()))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errorMessage").value("Payment not found: payment-123"));
    }

    @Test
    void testRetryPayment_WithoutAuthentication_ShouldReturnUnauthorized() throws Exception {
        PaymentRepairController.RetryRequest request = PaymentRepairController.RetryRequest.builder()
            .reason("System error resolved")
            .forceRetry(false)
            .build();

        mockMvc.perform(post("/api/repair/v1/payments/payment-123/retry")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .with(csrf()))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "OPS_VIEWER")
    void testRetryPayment_WithInsufficientRole_ShouldReturnForbidden() throws Exception {
        PaymentRepairController.RetryRequest request = PaymentRepairController.RetryRequest.builder()
            .reason("System error resolved")
            .forceRetry(false)
            .build();

        mockMvc.perform(post("/api/repair/v1/payments/payment-123/retry")
                .header("X-User-ID", "viewer")
                .header("X-Correlation-ID", "corr-123")
                .header("X-Tenant-ID", "tenant-123")
                .header("X-Business-Unit-ID", "bu-123")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .with(csrf()))
            .andExpect(status().isForbidden());
    }
}
