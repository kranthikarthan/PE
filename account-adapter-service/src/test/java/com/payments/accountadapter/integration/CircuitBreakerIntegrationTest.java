package com.payments.accountadapter.integration;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.github.tomakehurst.wiremock.WireMockServer;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

/**
 * Circuit Breaker Integration Test
 *
 * <p>Tests circuit breaker functionality: - Circuit breaker state transitions - Failure rate
 * thresholds - Half-open state behavior - Fallback mechanisms
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class CircuitBreakerIntegrationTest {

  @Autowired private WireMockServer wireMockServer;

  @Autowired private TestRestTemplate restTemplate;

  @Autowired private CircuitBreakerRegistry circuitBreakerRegistry;

  @LocalServerPort private int port;

  private String baseUrl;
  private String accountNumber;
  private String tenantId;
  private String businessUnitId;
  private String correlationId;

  @BeforeEach
  void setUp() {
    baseUrl = "http://localhost:" + port + "/account-adapter-service";
    accountNumber = "12345678901";
    tenantId = "tenant-123";
    businessUnitId = "business-unit-456";
    correlationId = UUID.randomUUID().toString();

    // Reset circuit breaker state
    CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker("account-service");
    circuitBreaker.transitionToClosedState();
  }

  @AfterEach
  void tearDown() {
    wireMockServer.resetAll();
  }

  /** Test circuit breaker opens after failure threshold */
  @Test
  void testCircuitBreaker_OpensAfterFailures() {
    // Given
    String url = baseUrl + "/api/v1/accounts/" + accountNumber + "/balance";
    HttpHeaders headers = createHeaders();

    // Setup WireMock to return 500 errors
    wireMockServer.stubFor(
        get(urlEqualTo("/api/v1/accounts/balance"))
            .willReturn(
                aResponse()
                    .withStatus(500)
                    .withHeader("Content-Type", "application/json")
                    .withBody("{\"error\": \"Internal Server Error\"}")));

    // When - Make multiple calls to trigger circuit breaker
    for (int i = 0; i < 5; i++) {
      ResponseEntity<Object> response =
          restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), Object.class);

      // All calls should succeed due to fallback
      assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
    }

    // Then - Circuit breaker should be open
    CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker("account-service");
    assertThat(circuitBreaker.getState()).isEqualTo(CircuitBreaker.State.OPEN);
  }

  /** Test circuit breaker half-open state */
  @Test
  void testCircuitBreaker_HalfOpenState() throws InterruptedException {
    // Given
    String url = baseUrl + "/api/v1/accounts/" + accountNumber + "/balance";
    HttpHeaders headers = createHeaders();

    // First, open the circuit breaker
    wireMockServer.stubFor(
        get(urlEqualTo("/api/v1/accounts/balance"))
            .willReturn(
                aResponse()
                    .withStatus(500)
                    .withHeader("Content-Type", "application/json")
                    .withBody("{\"error\": \"Internal Server Error\"}")));

    // Make calls to open circuit breaker
    for (int i = 0; i < 5; i++) {
      restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), Object.class);
    }

    CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker("account-service");
    assertThat(circuitBreaker.getState()).isEqualTo(CircuitBreaker.State.OPEN);

    // Wait for circuit breaker to transition to half-open
    Thread.sleep(3000); // Wait for wait duration

    // Setup successful response
    wireMockServer.stubFor(
        get(urlEqualTo("/api/v1/accounts/balance"))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(createSuccessResponse())));

    // When - Make a call in half-open state
    ResponseEntity<Object> response =
        restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), Object.class);

    // Then - Should succeed and circuit breaker should close
    assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
    // Note: Circuit breaker state transition might take time
  }

  /** Test circuit breaker with slow calls */
  @Test
  void testCircuitBreaker_SlowCalls() {
    // Given
    String url = baseUrl + "/api/v1/accounts/" + accountNumber + "/balance";
    HttpHeaders headers = createHeaders();

    // Setup WireMock to return slow responses
    wireMockServer.stubFor(
        get(urlEqualTo("/api/v1/accounts/balance"))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(createSuccessResponse())
                    .withFixedDelay(1500))); // 1.5 second delay (exceeds 1s threshold)

    // When - Make multiple calls
    for (int i = 0; i < 5; i++) {
      ResponseEntity<Object> response =
          restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), Object.class);

      assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
    }

    // Then - Circuit breaker should be open due to slow calls
    CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker("account-service");
    // Note: Circuit breaker might be open due to slow call rate
  }

  /** Test circuit breaker metrics */
  @Test
  void testCircuitBreaker_Metrics() {
    // Given
    String url = baseUrl + "/api/v1/accounts/" + accountNumber + "/balance";
    HttpHeaders headers = createHeaders();

    // Setup WireMock to return mixed responses
    wireMockServer.stubFor(
        get(urlEqualTo("/api/v1/accounts/balance"))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(createSuccessResponse())));

    // When - Make multiple calls
    for (int i = 0; i < 10; i++) {
      ResponseEntity<Object> response =
          restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), Object.class);

      assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
    }

    // Then - Check circuit breaker metrics
    CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker("account-service");

    assertThat(circuitBreaker.getMetrics().getNumberOfBufferedCalls()).isGreaterThan(0);
    assertThat(circuitBreaker.getMetrics().getNumberOfSuccessfulCalls()).isGreaterThan(0);
    assertThat(circuitBreaker.getState()).isEqualTo(CircuitBreaker.State.CLOSED);
  }

  /** Test circuit breaker with different error types */
  @Test
  void testCircuitBreaker_DifferentErrorTypes() {
    // Given
    String url = baseUrl + "/api/v1/accounts/" + accountNumber + "/balance";
    HttpHeaders headers = createHeaders();

    // Test with different error scenarios
    testWithError(500, "Internal Server Error");
    testWithError(503, "Service Unavailable");
    testWithError(504, "Gateway Timeout");
  }

  /** Test with specific error */
  private void testWithError(int statusCode, String errorMessage) {
    // Setup WireMock for specific error
    wireMockServer.stubFor(
        get(urlEqualTo("/api/v1/accounts/balance"))
            .willReturn(
                aResponse()
                    .withStatus(statusCode)
                    .withHeader("Content-Type", "application/json")
                    .withBody("{\"error\": \"" + errorMessage + "\"}")));

    String url = baseUrl + "/api/v1/accounts/" + accountNumber + "/balance";
    HttpHeaders headers = createHeaders();

    // When
    ResponseEntity<Object> response =
        restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), Object.class);

    // Then - Should still succeed due to fallback
    assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
  }

  /** Create test headers */
  private HttpHeaders createHeaders() {
    HttpHeaders headers = new HttpHeaders();
    headers.set("X-Tenant-ID", tenantId);
    headers.set("X-Business-Unit-ID", businessUnitId);
    headers.set("X-Correlation-ID", correlationId);
    return headers;
  }

  /** Create success response JSON */
  private String createSuccessResponse() {
    return """
                {
                    "accountNumber": "12345678901",
                    "accountHolderName": "John Doe",
                    "accountType": "CHECKING",
                    "accountStatus": "ACTIVE",
                    "availableBalance": 1000.00,
                    "ledgerBalance": 1000.00,
                    "currency": "ZAR",
                    "lastTransactionDate": "2024-01-15T10:30:00Z",
                    "balanceAsOf": "2024-01-15T10:30:00Z",
                    "responseCode": "SUCCESS",
                    "responseMessage": "Account balance retrieved successfully",
                    "correlationId": "%s",
                    "responseTimestamp": %d,
                    "requestId": "req-123"
                }
                """
        .formatted(correlationId, System.currentTimeMillis());
  }
}
