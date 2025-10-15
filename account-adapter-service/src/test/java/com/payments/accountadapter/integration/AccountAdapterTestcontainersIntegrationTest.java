package com.payments.accountadapter.integration;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.payments.accountadapter.dto.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Account Adapter Testcontainers Integration Test
 * 
 * Integration tests using Testcontainers for Redis:
 * - Redis integration testing
 * - Cache functionality testing
 * - End-to-end testing
 * - Real infrastructure testing
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Testcontainers
class AccountAdapterTestcontainersIntegrationTest {

    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine")
            .withExposedPorts(6379);

    @Autowired
    private WireMockServer wireMockServer;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @LocalServerPort
    private int port;

    private String baseUrl;
    private String accountNumber;
    private String tenantId;
    private String businessUnitId;
    private String correlationId;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.redis.host", redis::getHost);
        registry.add("spring.redis.port", () -> redis.getMappedPort(6379));
    }

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port + "/account-adapter-service";
        accountNumber = "12345678901";
        tenantId = "tenant-123";
        businessUnitId = "business-unit-456";
        correlationId = UUID.randomUUID().toString();

        // Clear Redis cache
        redisTemplate.getConnectionFactory().getConnection().flushAll();

        // Setup WireMock stubs
        setupWireMockStubs();
    }

    @AfterEach
    void tearDown() {
        wireMockServer.resetAll();
        redisTemplate.getConnectionFactory().getConnection().flushAll();
    }

    /**
     * Test Redis cache integration
     */
    @Test
    void testRedisCacheIntegration() {
        // Given
        String url = baseUrl + "/api/v1/accounts/" + accountNumber + "/balance";
        HttpHeaders headers = createHeaders();

        // When - First call (should cache response)
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

        // Verify cache is working
        String cacheKey = "account:balance:" + tenantId + ":" + accountNumber;
        Object cached = redisTemplate.opsForValue().get(cacheKey);
        assertThat(cached).isNotNull();

        // Verify WireMock was called only once (second call used cache)
        wireMockServer.verify(1, getRequestedFor(urlEqualTo("/api/v1/accounts/balance")));
    }

    /**
     * Test OAuth2 token caching in Redis
     */
    @Test
    void testOAuth2TokenCaching() {
        // Given
        String url = baseUrl + "/api/v1/accounts/" + accountNumber + "/balance";
        HttpHeaders headers = createHeaders();

        // When - Make multiple calls
        for (int i = 0; i < 3; i++) {
            ResponseEntity<AccountBalanceResponse> response = restTemplate.exchange(
                    url, HttpMethod.GET, new HttpEntity<>(headers), AccountBalanceResponse.class);
            
            assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        }

        // Then - Verify OAuth2 token is cached
        String tokenCacheKey = "oauth2:token:account-service";
        Object cachedToken = redisTemplate.opsForValue().get(tokenCacheKey);
        assertThat(cachedToken).isNotNull();

        // Verify OAuth2 token endpoint was called only once
        wireMockServer.verify(1, postRequestedFor(urlEqualTo("/oauth/token")));
    }

    /**
     * Test cache expiration
     */
    @Test
    void testCacheExpiration() throws InterruptedException {
        // Given
        String url = baseUrl + "/api/v1/accounts/" + accountNumber + "/balance";
        HttpHeaders headers = createHeaders();

        // When - First call
        ResponseEntity<AccountBalanceResponse> response1 = restTemplate.exchange(
                url, HttpMethod.GET, new HttpEntity<>(headers), AccountBalanceResponse.class);

        // Wait for cache to expire (test TTL is 10 seconds)
        Thread.sleep(11000);

        // When - Second call after cache expiration
        ResponseEntity<AccountBalanceResponse> response2 = restTemplate.exchange(
                url, HttpMethod.GET, new HttpEntity<>(headers), AccountBalanceResponse.class);

        // Then
        assertThat(response1.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response2.getStatusCode().is2xxSuccessful()).isTrue();

        // Verify WireMock was called twice (cache expired)
        wireMockServer.verify(2, getRequestedFor(urlEqualTo("/api/v1/accounts/balance")));
    }

    /**
     * Test cache clearing functionality
     */
    @Test
    void testCacheClearing() {
        // Given
        String url = baseUrl + "/api/v1/accounts/" + accountNumber + "/balance";
        HttpHeaders headers = createHeaders();

        // When - First call (should cache response)
        ResponseEntity<AccountBalanceResponse> response1 = restTemplate.exchange(
                url, HttpMethod.GET, new HttpEntity<>(headers), AccountBalanceResponse.class);

        // Clear cache via API
        String clearCacheUrl = baseUrl + "/api/v1/cache/accounts/" + accountNumber;
        ResponseEntity<Object> clearResponse = restTemplate.exchange(
                clearCacheUrl, HttpMethod.DELETE, new HttpEntity<>(headers), Object.class);

        // When - Second call after cache clearing
        ResponseEntity<AccountBalanceResponse> response2 = restTemplate.exchange(
                url, HttpMethod.GET, new HttpEntity<>(headers), AccountBalanceResponse.class);

        // Then
        assertThat(response1.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(clearResponse.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response2.getStatusCode().is2xxSuccessful()).isTrue();

        // Verify WireMock was called twice (cache was cleared)
        wireMockServer.verify(2, getRequestedFor(urlEqualTo("/api/v1/accounts/balance")));
    }

    /**
     * Test comprehensive account information
     */
    @Test
    void testComprehensiveAccountInfo() {
        // Given
        String url = baseUrl + "/api/v1/orchestration/accounts/" + accountNumber + "/comprehensive";
        HttpHeaders headers = createHeaders();

        // When
        ResponseEntity<Object> response = restTemplate.exchange(
                url, HttpMethod.GET, new HttpEntity<>(headers), Object.class);

        // Then
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isNotNull();

        // Verify all required endpoints were called
        wireMockServer.verify(postRequestedFor(urlEqualTo("/api/v1/accounts/validate")));
        wireMockServer.verify(getRequestedFor(urlEqualTo("/api/v1/accounts/status")));
        wireMockServer.verify(getRequestedFor(urlEqualTo("/api/v1/accounts/balance")));
    }

    /**
     * Test batch account validation
     */
    @Test
    void testBatchAccountValidation() {
        // Given
        String url = baseUrl + "/api/v1/orchestration/accounts/batch-validation";
        HttpHeaders headers = createHeaders();
        headers.set("Content-Type", "application/json");

        String requestBody = "[\"12345678901\", \"12345678902\", \"12345678903\"]";

        // When
        ResponseEntity<Object> response = restTemplate.exchange(
                url, HttpMethod.POST, new HttpEntity<>(requestBody, headers), Object.class);

        // Then
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isNotNull();

        // Verify validation endpoint was called for each account
        wireMockServer.verify(3, postRequestedFor(urlEqualTo("/api/v1/accounts/validate")));
    }

    /**
     * Test health monitoring
     */
    @Test
    void testHealthMonitoring() {
        // Given
        String url = baseUrl + "/api/v1/health";

        // When
        ResponseEntity<Object> response = restTemplate.exchange(
                url, HttpMethod.GET, new HttpEntity<>(new HttpHeaders()), Object.class);

        // Then
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isNotNull();
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
