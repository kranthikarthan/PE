package com.payments.paymentinitiation.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.payments.contracts.payment.PaymentInitiationRequest;
import com.payments.contracts.payment.PaymentInitiationResponse;
import com.payments.domain.shared.Money;
import com.payments.domain.shared.PaymentId;
import com.payments.domain.shared.TenantContext;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Integration tests for Payment Initiation Service
 *
 * <p>Tests complete payment flow with real database using Testcontainers
 */
@SpringBootTest
@org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("test")
@Transactional
class PaymentInitiationIntegrationTest {

  @Container
  static PostgreSQLContainer<?> postgres =
      new PostgreSQLContainer<>("postgres:15-alpine")
          .withDatabaseName("payments_test")
          .withUsername("test")
          .withPassword("test");

  @DynamicPropertySource
  static void configureProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", postgres::getJdbcUrl);
    registry.add("spring.datasource.username", postgres::getUsername);
    registry.add("spring.datasource.password", postgres::getPassword);
  }

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @org.springframework.boot.test.mock.mockito.MockBean
  private com.payments.paymentinitiation.service.PaymentDomainService paymentDomainService;

  private String correlationId;
  private String tenantId;
  private String businessUnitId;

  @BeforeEach
  void setUp() {
    correlationId = UUID.randomUUID().toString();
    tenantId = "TENANT-001";
    businessUnitId = "BU-001";
  }

  @Test
  void completePaymentFlow_ShouldWorkEndToEnd() throws Exception {
    // Step 1: Initiate Payment
    PaymentInitiationRequest request = createValidPaymentRequest();

    String paymentResponse =
        mockMvc
            .perform(
                post("/api/v1/payments/initiate")
                    .header("X-Correlation-ID", correlationId)
                    .header("X-Tenant-ID", tenantId)
                    .header("X-Business-Unit-ID", businessUnitId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.paymentId").exists())
            .andExpect(jsonPath("$.status").value("INITIATED"))
            .andReturn()
            .getResponse()
            .getContentAsString();

    PaymentInitiationResponse response =
        objectMapper.readValue(paymentResponse, PaymentInitiationResponse.class);
    String paymentId = response.getPaymentId().getValue();

    // Step 2: Get Payment Status
    mockMvc
        .perform(
            get("/api/v1/payments/{paymentId}/status", paymentId)
                .header("X-Correlation-ID", correlationId)
                .header("X-Tenant-ID", tenantId)
                .header("X-Business-Unit-ID", businessUnitId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.paymentId").value(paymentId))
        .andExpect(jsonPath("$.status").value("INITIATED"));

    // Step 3: Validate Payment
    mockMvc
        .perform(
            post("/api/v1/payments/{paymentId}/validate", paymentId)
                .header("X-Correlation-ID", correlationId)
                .header("X-Tenant-ID", tenantId)
                .header("X-Business-Unit-ID", businessUnitId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.paymentId").value(paymentId))
        .andExpect(jsonPath("$.status").value("VALIDATED"));

    // Step 4: Complete Payment
    mockMvc
        .perform(
            post("/api/v1/payments/{paymentId}/complete", paymentId)
                .header("X-Correlation-ID", correlationId)
                .header("X-Tenant-ID", tenantId)
                .header("X-Business-Unit-ID", businessUnitId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.paymentId").value(paymentId))
        .andExpect(jsonPath("$.status").value("COMPLETED"));

    // Step 5: Verify Payment History
    mockMvc
        .perform(
            get("/api/v1/payments/history")
                .header("X-Correlation-ID", correlationId)
                .header("X-Tenant-ID", tenantId)
                .header("X-Business-Unit-ID", businessUnitId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.payments").isArray())
        .andExpect(jsonPath("$.totalCount").value(1))
        .andExpect(jsonPath("$.payments[0].paymentId").value(paymentId))
        .andExpect(jsonPath("$.payments[0].status").value("COMPLETED"));
  }

  @Test
  void paymentFailureFlow_ShouldWorkEndToEnd() throws Exception {
    // Step 1: Initiate Payment
    PaymentInitiationRequest request = createValidPaymentRequest();

    String paymentResponse =
        mockMvc
            .perform(
                post("/api/v1/payments/initiate")
                    .header("X-Correlation-ID", correlationId)
                    .header("X-Tenant-ID", tenantId)
                    .header("X-Business-Unit-ID", businessUnitId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.status").value("INITIATED"))
            .andReturn()
            .getResponse()
            .getContentAsString();

    PaymentInitiationResponse response =
        objectMapper.readValue(paymentResponse, PaymentInitiationResponse.class);
    String paymentId = response.getPaymentId().getValue();

    // Step 2: Fail Payment
    String failureReason = "Insufficient funds";
    mockMvc
        .perform(
            post("/api/v1/payments/{paymentId}/fail", paymentId)
                .header("X-Correlation-ID", correlationId)
                .header("X-Tenant-ID", tenantId)
                .header("X-Business-Unit-ID", businessUnitId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"reason\":\"" + failureReason + "\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.paymentId").value(paymentId))
        .andExpect(jsonPath("$.status").value("FAILED"))
        .andExpect(jsonPath("$.errorMessage").value(failureReason));

    // Step 3: Verify Payment History
    mockMvc
        .perform(
            get("/api/v1/payments/history")
                .header("X-Correlation-ID", correlationId)
                .header("X-Tenant-ID", tenantId)
                .header("X-Business-Unit-ID", businessUnitId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.payments").isArray())
        .andExpect(jsonPath("$.totalCount").value(1))
        .andExpect(jsonPath("$.payments[0].paymentId").value(paymentId))
        .andExpect(jsonPath("$.payments[0].status").value("FAILED"));
  }

  @Test
  void idempotencyTest_ShouldPreventDuplicatePayments() throws Exception {
    // Given
    PaymentInitiationRequest request = createValidPaymentRequest();

    // When: First request
    mockMvc
        .perform(
            post("/api/v1/payments/initiate")
                .header("X-Correlation-ID", correlationId)
                .header("X-Tenant-ID", tenantId)
                .header("X-Business-Unit-ID", businessUnitId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.status").value("INITIATED"));

    // When: Duplicate request with same idempotency key
    mockMvc
        .perform(
            post("/api/v1/payments/initiate")
                .header("X-Correlation-ID", correlationId)
                .header("X-Tenant-ID", tenantId)
                .header("X-Business-Unit-ID", businessUnitId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errorMessage").value("Duplicate payment request"));
  }

  @Test
  void validationTest_ShouldRejectInvalidRequests() throws Exception {
    // Given: Invalid payment request (empty idempotency key)
    PaymentInitiationRequest invalidRequest = createInvalidPaymentRequest();

    // When & Then
    mockMvc
        .perform(
            post("/api/v1/payments/initiate")
                .header("X-Correlation-ID", correlationId)
                .header("X-Tenant-ID", tenantId)
                .header("X-Business-Unit-ID", businessUnitId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void multiTenantTest_ShouldIsolateData() throws Exception {
    // Given: Two different tenants
    String tenant1 = "TENANT-001";
    String tenant2 = "TENANT-002";

    PaymentInitiationRequest request1 = createValidPaymentRequest();
    PaymentInitiationRequest request2 = createValidPaymentRequest();
    request2.setIdempotencyKey("IDEMPOTENCY-002");

    // When: Create payment for tenant 1
    mockMvc
        .perform(
            post("/api/v1/payments/initiate")
                .header("X-Correlation-ID", correlationId)
                .header("X-Tenant-ID", tenant1)
                .header("X-Business-Unit-ID", businessUnitId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request1)))
        .andExpect(status().isCreated());

    // When: Create payment for tenant 2
    mockMvc
        .perform(
            post("/api/v1/payments/initiate")
                .header("X-Correlation-ID", correlationId)
                .header("X-Tenant-ID", tenant2)
                .header("X-Business-Unit-ID", businessUnitId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request2)))
        .andExpect(status().isCreated());

    // Then: Each tenant should only see their own payments
    mockMvc
        .perform(
            get("/api/v1/payments/history")
                .header("X-Correlation-ID", correlationId)
                .header("X-Tenant-ID", tenant1)
                .header("X-Business-Unit-ID", businessUnitId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalCount").value(1));

    mockMvc
        .perform(
            get("/api/v1/payments/history")
                .header("X-Correlation-ID", correlationId)
                .header("X-Tenant-ID", tenant2)
                .header("X-Business-Unit-ID", businessUnitId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalCount").value(1));
  }

  private PaymentInitiationRequest createValidPaymentRequest() {
    return PaymentInitiationRequest.builder()
        .paymentId(new PaymentId("PAY-" + UUID.randomUUID().toString().substring(0, 8)))
        .idempotencyKey("IDEMPOTENCY-" + UUID.randomUUID().toString().substring(0, 8))
        .sourceAccount("12345678901")
        .destinationAccount("98765432109")
        .amount(Money.zar(BigDecimal.valueOf(1000.00)))
        .reference("Integration test payment")
        .paymentType(com.payments.contracts.payment.PaymentType.EFT)
        .priority(com.payments.contracts.payment.Priority.NORMAL)
        .tenantContext(
            TenantContext.builder().tenantId(tenantId).businessUnitId(businessUnitId).build())
        .initiatedBy("integration-test@example.com")
        .build();
  }

  private PaymentInitiationRequest createInvalidPaymentRequest() {
    return PaymentInitiationRequest.builder()
        .paymentId(new PaymentId("PAY-INVALID"))
        .idempotencyKey("") // Invalid: empty idempotency key
        .sourceAccount("12345678901")
        .destinationAccount("98765432109")
        .amount(Money.zar(BigDecimal.valueOf(1000.00)))
        .reference("Invalid payment")
        .paymentType(com.payments.contracts.payment.PaymentType.EFT)
        .priority(com.payments.contracts.payment.Priority.NORMAL)
        .tenantContext(
            TenantContext.builder().tenantId(tenantId).businessUnitId(businessUnitId).build())
        .initiatedBy("integration-test@example.com")
        .build();
  }
}
