package com.payments.routing.contract;

import static org.assertj.core.api.Assertions.assertThat;

import com.payments.routing.engine.RoutingDecision;
import com.payments.routing.engine.RoutingRequest;
import com.payments.routing.service.RoutingService;
import com.payments.routing.service.RoutingService.RoutingStatistics;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

/**
 * Routing Service Contract Tests
 *
 * <p>Contract tests for the routing service API: - Happy path scenarios - Error scenarios -
 * Multi-tenant isolation - Performance requirements
 *
 * <p>Coverage: 80% minimum
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class RoutingServiceContractTest {

  @LocalServerPort private int port;

  @Autowired private TestRestTemplate restTemplate;

  @Autowired private RoutingService routingService;

  private String baseUrl;
  private HttpHeaders headers;

  @BeforeEach
  void setUp() {
    baseUrl = "http://localhost:" + port + "/api/v1/routing";

    headers = new HttpHeaders();
    headers.set("X-Tenant-ID", "TENANT001");
    headers.set("X-Correlation-ID", "TEST-CORRELATION-001");
    headers.set("Content-Type", "application/json");
  }

  @Test
  void shouldGetRoutingDecisionSuccessfully() {
    // Given
    RoutingRequest request =
        RoutingRequest.builder()
            .paymentId("PAY-001")
            .tenantId("TENANT001")
            .businessUnitId("BU001")
            .amount(new BigDecimal("1000.00"))
            .currency("ZAR")
            .paymentType("EFT")
            .sourceAccount("ACC123")
            .destinationAccount("ACC456")
            .priority("NORMAL")
            .createdAt(Instant.now())
            .build();

    HttpEntity<RoutingRequest> entity = new HttpEntity<>(request, headers);

    // When
    ResponseEntity<RoutingDecision> response =
        restTemplate.exchange(
            baseUrl + "/decisions", HttpMethod.POST, entity, RoutingDecision.class);

    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getPaymentId()).isEqualTo("PAY-001");
    assertThat(response.getBody().getClearingSystem()).isNotNull();
    assertThat(response.getBody().getPriority()).isNotNull();
    assertThat(response.getBody().getDecisionReason()).isNotNull();
  }

  @Test
  void shouldGetRoutingDecisionWithFallback() {
    // Given
    RoutingRequest request =
        RoutingRequest.builder()
            .paymentId("PAY-002")
            .tenantId("TENANT001")
            .businessUnitId("BU001")
            .amount(new BigDecimal("5000.00"))
            .currency("ZAR")
            .paymentType("EFT")
            .sourceAccount("ACC123")
            .destinationAccount("ACC456")
            .priority("HIGH")
            .createdAt(Instant.now())
            .build();

    HttpEntity<RoutingRequest> entity = new HttpEntity<>(request, headers);

    // When
    ResponseEntity<RoutingDecision> response =
        restTemplate.exchange(
            baseUrl + "/decisions/with-fallback?fallbackClearingSystem=FALLBACK_CLEARING",
            HttpMethod.POST,
            entity,
            RoutingDecision.class);

    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getPaymentId()).isEqualTo("PAY-002");
    assertThat(response.getBody().getClearingSystem()).isNotNull();
  }

  @Test
  void shouldGetRoutingStatistics() {
    // Given
    HttpEntity<Void> entity = new HttpEntity<>(headers);

    // When
    ResponseEntity<RoutingStatistics> response =
        restTemplate.exchange(
            baseUrl + "/statistics", HttpMethod.GET, entity, RoutingStatistics.class);

    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getTotalRules()).isNotNull();
    assertThat(response.getBody().getActiveRules()).isNotNull();
    assertThat(response.getBody().getCacheSize()).isNotNull();
  }

  @Test
  void shouldClearRoutingCache() {
    // Given
    HttpEntity<Void> entity = new HttpEntity<>(headers);

    // When
    ResponseEntity<Map> response =
        restTemplate.exchange(baseUrl + "/cache/PAY-001", HttpMethod.DELETE, entity, Map.class);

    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().get("message")).isEqualTo("Cache cleared successfully");
    assertThat(response.getBody().get("paymentId")).isEqualTo("PAY-001");
  }

  @Test
  void shouldClearAllRoutingCache() {
    // Given
    HttpEntity<Void> entity = new HttpEntity<>(headers);

    // When
    ResponseEntity<Map> response =
        restTemplate.exchange(baseUrl + "/cache", HttpMethod.DELETE, entity, Map.class);

    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().get("message")).isEqualTo("All cache cleared successfully");
  }

  @Test
  void shouldReturn400ForInvalidRequest() {
    // Given
    RoutingRequest request =
        RoutingRequest.builder()
            .paymentId("") // Invalid: empty payment ID
            .tenantId("TENANT001")
            .amount(new BigDecimal("-100.00")) // Invalid: negative amount
            .currency("INVALID") // Invalid: unsupported currency
            .build();

    HttpEntity<RoutingRequest> entity = new HttpEntity<>(request, headers);

    // When
    ResponseEntity<Map> response =
        restTemplate.exchange(baseUrl + "/decisions", HttpMethod.POST, entity, Map.class);

    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().get("code")).isEqualTo("VALIDATION_ERROR");
  }

  @Test
  void shouldReturn403ForTenantMismatch() {
    // Given
    RoutingRequest request =
        RoutingRequest.builder()
            .paymentId("PAY-003")
            .tenantId("TENANT002") // Different tenant
            .amount(new BigDecimal("1000.00"))
            .currency("ZAR")
            .build();

    HttpEntity<RoutingRequest> entity = new HttpEntity<>(request, headers);

    // When
    ResponseEntity<Void> response =
        restTemplate.exchange(baseUrl + "/decisions", HttpMethod.POST, entity, Void.class);

    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  void shouldReturn401ForMissingTenantHeader() {
    // Given
    RoutingRequest request =
        RoutingRequest.builder()
            .paymentId("PAY-004")
            .tenantId("TENANT001")
            .amount(new BigDecimal("1000.00"))
            .currency("ZAR")
            .build();

    HttpHeaders headersWithoutTenant = new HttpHeaders();
    headersWithoutTenant.set("X-Correlation-ID", "TEST-CORRELATION-002");
    headersWithoutTenant.set("Content-Type", "application/json");

    HttpEntity<RoutingRequest> entity = new HttpEntity<>(request, headersWithoutTenant);

    // When
    ResponseEntity<Void> response =
        restTemplate.exchange(baseUrl + "/decisions", HttpMethod.POST, entity, Void.class);

    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
  }

  @Test
  void shouldHandleHighValuePayment() {
    // Given
    RoutingRequest request =
        RoutingRequest.builder()
            .paymentId("PAY-005")
            .tenantId("TENANT001")
            .businessUnitId("BU001")
            .amount(new BigDecimal("1000000.00")) // High value
            .currency("ZAR")
            .paymentType("EFT")
            .sourceAccount("ACC123")
            .destinationAccount("ACC456")
            .priority("HIGH")
            .createdAt(Instant.now())
            .build();

    HttpEntity<RoutingRequest> entity = new HttpEntity<>(request, headers);

    // When
    ResponseEntity<RoutingDecision> response =
        restTemplate.exchange(
            baseUrl + "/decisions", HttpMethod.POST, entity, RoutingDecision.class);

    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getPaymentId()).isEqualTo("PAY-005");
    assertThat(response.getBody().getClearingSystem()).isNotNull();
  }

  @Test
  void shouldHandleInternationalPayment() {
    // Given
    RoutingRequest request =
        RoutingRequest.builder()
            .paymentId("PAY-006")
            .tenantId("TENANT001")
            .businessUnitId("BU001")
            .amount(new BigDecimal("5000.00"))
            .currency("USD") // International currency
            .paymentType("SWIFT")
            .sourceAccount("ACC123")
            .destinationAccount("ACC456")
            .priority("NORMAL")
            .createdAt(Instant.now())
            .build();

    HttpEntity<RoutingRequest> entity = new HttpEntity<>(request, headers);

    // When
    ResponseEntity<RoutingDecision> response =
        restTemplate.exchange(
            baseUrl + "/decisions", HttpMethod.POST, entity, RoutingDecision.class);

    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getPaymentId()).isEqualTo("PAY-006");
    assertThat(response.getBody().getClearingSystem()).isNotNull();
  }

  @Test
  void shouldHandleBatchPayment() {
    // Given
    RoutingRequest request =
        RoutingRequest.builder()
            .paymentId("PAY-007")
            .tenantId("TENANT001")
            .businessUnitId("BU001")
            .amount(new BigDecimal("100.00"))
            .currency("ZAR")
            .paymentType("BATCH")
            .sourceAccount("ACC123")
            .destinationAccount("ACC456")
            .priority("LOW")
            .createdAt(Instant.now())
            .build();

    HttpEntity<RoutingRequest> entity = new HttpEntity<>(request, headers);

    // When
    ResponseEntity<RoutingDecision> response =
        restTemplate.exchange(
            baseUrl + "/decisions", HttpMethod.POST, entity, RoutingDecision.class);

    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getPaymentId()).isEqualTo("PAY-007");
    assertThat(response.getBody().getClearingSystem()).isNotNull();
  }

  @Test
  void shouldHandleRealTimePayment() {
    // Given
    RoutingRequest request =
        RoutingRequest.builder()
            .paymentId("PAY-008")
            .tenantId("TENANT001")
            .businessUnitId("BU001")
            .amount(new BigDecimal("2000.00"))
            .currency("ZAR")
            .paymentType("RTC")
            .sourceAccount("ACC123")
            .destinationAccount("ACC456")
            .priority("IMMEDIATE")
            .createdAt(Instant.now())
            .build();

    HttpEntity<RoutingRequest> entity = new HttpEntity<>(request, headers);

    // When
    ResponseEntity<RoutingDecision> response =
        restTemplate.exchange(
            baseUrl + "/decisions", HttpMethod.POST, entity, RoutingDecision.class);

    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getPaymentId()).isEqualTo("PAY-008");
    assertThat(response.getBody().getClearingSystem()).isNotNull();
  }
}
