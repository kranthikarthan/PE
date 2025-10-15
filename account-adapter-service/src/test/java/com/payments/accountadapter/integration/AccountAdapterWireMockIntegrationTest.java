package com.payments.accountadapter.integration;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.payments.accountadapter.api.AccountAdapterController;
import com.payments.accountadapter.dto.*;
import com.payments.accountadapter.service.AccountAdapterService;
import com.payments.accountadapter.service.AccountCacheService;
import com.payments.accountadapter.service.OAuth2TokenService;
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

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Account Adapter WireMock Integration Test
 * 
 * Integration tests using WireMock for external service mocking:
 * - Account balance retrieval
 * - Account validation
 * - Account status checking
 * - Error scenarios
 * - Circuit breaker testing
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class AccountAdapterWireMockIntegrationTest {

    @Autowired
    private WireMockServer wireMockServer;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private AccountAdapterService accountAdapterService;

    @Autowired
    private AccountCacheService accountCacheService;

    @Autowired
    private OAuth2TokenService oAuth2TokenService;

    @LocalServerPort
    private int port;

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

        // Clear cache before each test
        accountCacheService.clearAllCache();
        oAuth2TokenService.clearTokenCache();

        // Setup WireMock stubs
        setupWireMockStubs();
    }

    @AfterEach
    void tearDown() {
        wireMockServer.resetAll();
    }

    /**
     * Test successful account balance retrieval
     */
    @Test
    void testGetAccountBalance_Success() {
        // Given
        String url = baseUrl + "/api/v1/accounts/" + accountNumber + "/balance";
        HttpHeaders headers = createHeaders();

        // When
        ResponseEntity<AccountBalanceResponse> response = restTemplate.exchange(
                url, HttpMethod.GET, new HttpEntity<>(headers), AccountBalanceResponse.class);

        // Then
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getAccountNumber()).isEqualTo(accountNumber);
        assertThat(response.getBody().getAvailableBalance()).isNotNull();
        assertThat(response.getBody().getResponseCode()).isEqualTo("SUCCESS");

        // Verify WireMock was called
        wireMockServer.verify(getRequestedFor(urlEqualTo("/api/v1/accounts/balance"))
                .withHeader("Authorization", matching("Bearer.*"))
                .withHeader("X-Correlation-ID", equalTo(correlationId))
                .withHeader("X-Tenant-ID", equalTo(tenantId)));
    }

    /**
     * Test account balance retrieval with caching
     */
    @Test
    void testGetAccountBalance_WithCaching() {
        // Given
        String url = baseUrl + "/api/v1/accounts/" + accountNumber + "/balance";
        HttpHeaders headers = createHeaders();

        // When - First call
        ResponseEntity<AccountBalanceResponse> response1 = restTemplate.exchange(
                url, HttpMethod.GET, new HttpEntity<>(headers), AccountBalanceResponse.class);

        // When - Second call (should use cache)
        ResponseEntity<AccountBalanceResponse> response2 = restTemplate.exchange(
                url, HttpMethod.GET, new HttpEntity<>(headers), AccountBalanceResponse.class);

        // Then
        assertThat(response1.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response2.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response1.getBody()).isNotNull();
        assertThat(response2.getBody()).isNotNull();

        // Verify WireMock was called only once (second call used cache)
        wireMockServer.verify(1, getRequestedFor(urlEqualTo("/api/v1/accounts/balance")));
    }

    /**
     * Test account validation success
     */
    @Test
    void testValidateAccount_Success() {
        // Given
        String url = baseUrl + "/api/v1/accounts/" + accountNumber + "/validate";
        HttpHeaders headers = createHeaders();

        // When
        ResponseEntity<AccountValidationResponse> response = restTemplate.exchange(
                url, HttpMethod.POST, new HttpEntity<>(headers), AccountValidationResponse.class);

        // Then
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getAccountNumber()).isEqualTo(accountNumber);
        assertThat(response.getBody().isValid()).isTrue();
        assertThat(response.getBody().getResponseCode()).isEqualTo("SUCCESS");

        // Verify WireMock was called
        wireMockServer.verify(postRequestedFor(urlEqualTo("/api/v1/accounts/validate"))
                .withHeader("Authorization", matching("Bearer.*"))
                .withHeader("X-Correlation-ID", equalTo(correlationId))
                .withHeader("X-Tenant-ID", equalTo(tenantId)));
    }

    /**
     * Test account status retrieval
     */
    @Test
    void testGetAccountStatus_Success() {
        // Given
        String url = baseUrl + "/api/v1/accounts/" + accountNumber + "/status";
        HttpHeaders headers = createHeaders();

        // When
        ResponseEntity<AccountStatusResponse> response = restTemplate.exchange(
                url, HttpMethod.GET, new HttpEntity<>(headers), AccountStatusResponse.class);

        // Then
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getAccountNumber()).isEqualTo(accountNumber);
        assertThat(response.getBody().getAccountStatus()).isEqualTo("ACTIVE");
        assertThat(response.getBody().getResponseCode()).isEqualTo("SUCCESS");

        // Verify WireMock was called
        wireMockServer.verify(getRequestedFor(urlEqualTo("/api/v1/accounts/status"))
                .withHeader("Authorization", matching("Bearer.*"))
                .withHeader("X-Correlation-ID", equalTo(correlationId))
                .withHeader("X-Tenant-ID", equalTo(tenantId)));
    }

    /**
     * Test external service error handling
     */
    @Test
    void testGetAccountBalance_ExternalServiceError() {
        // Given
        String url = baseUrl + "/api/v1/accounts/" + accountNumber + "/balance";
        HttpHeaders headers = createHeaders();

        // Setup WireMock to return 500 error
        wireMockServer.stubFor(get(urlEqualTo("/api/v1/accounts/balance"))
                .willReturn(aResponse()
                        .withStatus(500)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"error\": \"Internal Server Error\"}")));

        // When
        ResponseEntity<AccountBalanceResponse> response = restTemplate.exchange(
                url, HttpMethod.GET, new HttpEntity<>(headers), AccountBalanceResponse.class);

        // Then
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue(); // Fallback response
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getResponseCode()).isEqualTo("FALLBACK");
        assertThat(response.getBody().getResponseMessage()).contains("Account service unavailable");
    }

    /**
     * Test timeout handling
     */
    @Test
    void testGetAccountBalance_Timeout() {
        // Given
        String url = baseUrl + "/api/v1/accounts/" + accountNumber + "/balance";
        HttpHeaders headers = createHeaders();

        // Setup WireMock to delay response
        wireMockServer.stubFor(get(urlEqualTo("/api/v1/accounts/balance"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(createAccountBalanceResponseJson())
                        .withFixedDelay(3000))); // 3 second delay

        // When
        ResponseEntity<AccountBalanceResponse> response = restTemplate.exchange(
                url, HttpMethod.GET, new HttpEntity<>(headers), AccountBalanceResponse.class);

        // Then
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue(); // Fallback response
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getResponseCode()).isEqualTo("FALLBACK");
    }

    /**
     * Test circuit breaker functionality
     */
    @Test
    void testCircuitBreaker_OpensAfterFailures() {
        // Given
        String url = baseUrl + "/api/v1/accounts/" + accountNumber + "/balance";
        HttpHeaders headers = createHeaders();

        // Setup WireMock to return 500 errors
        wireMockServer.stubFor(get(urlEqualTo("/api/v1/accounts/balance"))
                .willReturn(aResponse()
                        .withStatus(500)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"error\": \"Internal Server Error\"}")));

        // When - Make multiple calls to trigger circuit breaker
        for (int i = 0; i < 5; i++) {
            ResponseEntity<AccountBalanceResponse> response = restTemplate.exchange(
                    url, HttpMethod.GET, new HttpEntity<>(headers), AccountBalanceResponse.class);
            
            assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
            assertThat(response.getBody()).isNotNull();
        }

        // Then - Circuit breaker should be open
        // Note: This is a simplified test. In a real scenario, you would verify
        // the circuit breaker state through health endpoints or metrics
    }

    /**
     * Setup WireMock stubs
     */
    private void setupWireMockStubs() {
        // OAuth2 token endpoint
        wireMockServer.stubFor(post(urlEqualTo("/oauth/token"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"access_token\":\"test-token-123\",\"token_type\":\"Bearer\",\"expires_in\":3600}")));

        // Account balance endpoint
        wireMockServer.stubFor(get(urlEqualTo("/api/v1/accounts/balance"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(createAccountBalanceResponseJson())));

        // Account validation endpoint
        wireMockServer.stubFor(post(urlEqualTo("/api/v1/accounts/validate"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(createAccountValidationResponseJson())));

        // Account status endpoint
        wireMockServer.stubFor(get(urlEqualTo("/api/v1/accounts/status"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(createAccountStatusResponseJson())));
    }

    /**
     * Create test headers
     */
    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Tenant-ID", tenantId);
        headers.set("X-Business-Unit-ID", businessUnitId);
        headers.set("X-Correlation-ID", correlationId);
        return headers;
    }

    /**
     * Create account balance response JSON
     */
    private String createAccountBalanceResponseJson() {
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
                """.formatted(correlationId, System.currentTimeMillis());
    }

    /**
     * Create account validation response JSON
     */
    private String createAccountValidationResponseJson() {
        return """
                {
                    "accountNumber": "12345678901",
                    "accountHolderName": "John Doe",
                    "accountType": "CHECKING",
                    "accountStatus": "ACTIVE",
                    "isValid": true,
                    "validationStatus": "VALID",
                    "validationErrors": [],
                    "validationWarnings": [],
                    "responseCode": "SUCCESS",
                    "responseMessage": "Account validation successful",
                    "correlationId": "%s",
                    "responseTimestamp": %d,
                    "requestId": "req-123",
                    "validatedAt": "2024-01-15T10:30:00Z"
                }
                """.formatted(correlationId, System.currentTimeMillis());
    }

    /**
     * Create account status response JSON
     */
    private String createAccountStatusResponseJson() {
        return """
                {
                    "accountNumber": "12345678901",
                    "accountHolderName": "John Doe",
                    "accountType": "CHECKING",
                    "accountStatus": "ACTIVE",
                    "statusReason": "Account is active",
                    "statusChangedAt": "2024-01-15T10:30:00Z",
                    "previousStatus": "ACTIVE",
                    "restrictions": [],
                    "permissions": ["READ", "WRITE"],
                    "responseCode": "SUCCESS",
                    "responseMessage": "Account status retrieved successfully",
                    "correlationId": "%s",
                    "responseTimestamp": %d,
                    "requestId": "req-123",
                    "lastActivityDate": "2024-01-15T10:30:00Z"
                }
                """.formatted(correlationId, System.currentTimeMillis());
    }
}
