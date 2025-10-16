package com.payments.paymentinitiation.contract;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.payments.contracts.payment.PaymentInitiationRequest;
import com.payments.contracts.payment.PaymentType;
import com.payments.contracts.payment.Priority;
import com.payments.domain.shared.PaymentId;
import com.payments.domain.shared.Money;
import com.payments.domain.shared.TenantContext;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Contract tests for Payment Initiation API
 *
 * <p>Tests API contracts and OpenAPI documentation compliance
 */
@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
class PaymentInitiationContractTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @Test
  void paymentInitiationRequest_ShouldMatchContract() throws Exception {
    // Given
    PaymentInitiationRequest request =
        PaymentInitiationRequest.builder()
            .paymentId(new PaymentId("PAY-001"))
            .idempotencyKey("IDEMPOTENCY-001")
            .sourceAccount("12345678901")
            .destinationAccount("98765432109")
            .amount(new Money(BigDecimal.valueOf(1000.00), "ZAR"))
            .reference("Contract test payment")
            .paymentType(PaymentType.EFT)
            .priority(Priority.NORMAL)
            .tenantContext(
                TenantContext.builder().tenantId("TENANT-001").businessUnitId("BU-001").build())
            .initiatedBy("contract-test@example.com")
            .build();

    // When & Then
    mockMvc
        .perform(
            post("/api/v1/payments/initiate")
                .header("X-Correlation-ID", UUID.randomUUID().toString())
                .header("X-Tenant-ID", "TENANT-001")
                .header("X-Business-Unit-ID", "BU-001")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.paymentId").exists())
        .andExpect(jsonPath("$.status").exists())
        .andExpect(jsonPath("$.tenantContext").exists())
        .andExpect(jsonPath("$.initiatedAt").exists())
        .andExpect(jsonPath("$.errorMessage").doesNotExist());
  }

  @Test
  void paymentInitiationResponse_ShouldMatchContract() throws Exception {
    // Given
    PaymentInitiationRequest request = createValidPaymentRequest();

    // When & Then
    mockMvc
        .perform(
            post("/api/v1/payments/initiate")
                .header("X-Correlation-ID", UUID.randomUUID().toString())
                .header("X-Tenant-ID", "TENANT-001")
                .header("X-Business-Unit-ID", "BU-001")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.paymentId").isString())
        .andExpect(jsonPath("$.status").value("INITIATED"))
        .andExpect(jsonPath("$.tenantContext.tenantId").value("TENANT-001"))
        .andExpect(jsonPath("$.tenantContext.businessUnitId").value("BU-001"))
        .andExpect(jsonPath("$.initiatedAt").isString())
        .andExpect(jsonPath("$.errorMessage").doesNotExist());
  }

  @Test
  void paymentStatusResponse_ShouldMatchContract() throws Exception {
    // Given
    String paymentId = "PAY-001";

    // When & Then
    mockMvc
        .perform(
            get("/api/v1/payments/{paymentId}/status", paymentId)
                .header("X-Correlation-ID", UUID.randomUUID().toString())
                .header("X-Tenant-ID", "TENANT-001")
                .header("X-Business-Unit-ID", "BU-001"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.paymentId").exists())
        .andExpect(jsonPath("$.status").exists())
        .andExpect(jsonPath("$.tenantContext").exists())
        .andExpect(jsonPath("$.initiatedAt").exists());
  }

  @Test
  void paymentValidationResponse_ShouldMatchContract() throws Exception {
    // Given
    String paymentId = "PAY-001";

    // When & Then
    mockMvc
        .perform(
            post("/api/v1/payments/{paymentId}/validate", paymentId)
                .header("X-Correlation-ID", UUID.randomUUID().toString())
                .header("X-Tenant-ID", "TENANT-001")
                .header("X-Business-Unit-ID", "BU-001"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.paymentId").exists())
        .andExpect(jsonPath("$.status").exists())
        .andExpect(jsonPath("$.tenantContext").exists())
        .andExpect(jsonPath("$.initiatedAt").exists());
  }

  @Test
  void paymentFailureResponse_ShouldMatchContract() throws Exception {
    // Given
    String paymentId = "PAY-001";
    String reason = "Insufficient funds";

    // When & Then
    mockMvc
        .perform(
            post("/api/v1/payments/{paymentId}/fail", paymentId)
                .header("X-Correlation-ID", UUID.randomUUID().toString())
                .header("X-Tenant-ID", "TENANT-001")
                .header("X-Business-Unit-ID", "BU-001")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"reason\":\"" + reason + "\"}"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.paymentId").exists())
        .andExpect(jsonPath("$.status").exists())
        .andExpect(jsonPath("$.tenantContext").exists())
        .andExpect(jsonPath("$.initiatedAt").exists());
  }

  @Test
  void paymentCompletionResponse_ShouldMatchContract() throws Exception {
    // Given
    String paymentId = "PAY-001";

    // When & Then
    mockMvc
        .perform(
            post("/api/v1/payments/{paymentId}/complete", paymentId)
                .header("X-Correlation-ID", UUID.randomUUID().toString())
                .header("X-Tenant-ID", "TENANT-001")
                .header("X-Business-Unit-ID", "BU-001"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.paymentId").exists())
        .andExpect(jsonPath("$.status").exists())
        .andExpect(jsonPath("$.tenantContext").exists())
        .andExpect(jsonPath("$.initiatedAt").exists());
  }

  @Test
  void paymentHistoryResponse_ShouldMatchContract() throws Exception {
    // When & Then
    mockMvc
        .perform(
            get("/api/v1/payments/history")
                .header("X-Correlation-ID", UUID.randomUUID().toString())
                .header("X-Tenant-ID", "TENANT-001")
                .header("X-Business-Unit-ID", "BU-001"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.payments").isArray())
        .andExpect(jsonPath("$.totalCount").isNumber())
        .andExpect(jsonPath("$.errorMessage").doesNotExist());
  }

  @Test
  void errorResponse_ShouldMatchContract() throws Exception {
    // Given: Invalid request (missing required headers)
    PaymentInitiationRequest request = createValidPaymentRequest();

    // When & Then
    mockMvc
        .perform(
            post("/api/v1/payments/initiate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON));
  }

  @Test
  void openApiDocumentation_ShouldBeAccessible() throws Exception {
    // When & Then
    mockMvc
        .perform(get("/v3/api-docs"))
        .andExpect(status().isOk())
        .andExpect(content().contentType("application/json"))
        .andExpect(jsonPath("$.openapi").exists())
        .andExpect(jsonPath("$.info").exists())
        .andExpect(jsonPath("$.paths").exists());
  }

  @Test
  void swaggerUi_ShouldBeAccessible() throws Exception {
    // When & Then
    mockMvc
        .perform(get("/swagger-ui/index.html"))
        .andExpect(status().isOk())
        .andExpect(content().contentType("text/html"));
  }

  @Test
  void healthCheck_ShouldBeAccessible() throws Exception {
    // When & Then
    mockMvc
        .perform(get("/actuator/health"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.status").exists());
  }

  private PaymentInitiationRequest createValidPaymentRequest() {
    return PaymentInitiationRequest.builder()
        .paymentId(new PaymentId("PAY-001"))
        .idempotencyKey("IDEMPOTENCY-001")
        .sourceAccount("12345678901")
        .destinationAccount("98765432109")
        .amount(new Money(BigDecimal.valueOf(1000.00), "ZAR"))
        .reference("Contract test payment")
        .paymentType(PaymentType.EFT)
        .priority(Priority.NORMAL)
        .tenantContext(
            TenantContext.builder().tenantId("TENANT-001").businessUnitId("BU-001").build())
        .initiatedBy("contract-test@example.com")
        .build();
  }
}
