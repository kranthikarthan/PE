package com.payments.accountadapter.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.payments.accountadapter.client.AccountServiceClient;
import com.payments.accountadapter.client.AccountServiceFallback;
import com.payments.accountadapter.dto.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Account Adapter Service Unit Test
 *
 * <p>Unit tests for AccountAdapterService: - Service method testing - Fallback mechanism testing -
 * Error handling testing - Mocking external dependencies
 */
@ExtendWith(MockitoExtension.class)
class AccountAdapterServiceTest {

  @Mock private AccountServiceClient accountServiceClient;

  @Mock private AccountServiceFallback accountServiceFallback;

  @Mock private OAuth2TokenService oAuth2TokenService;

  @Mock private AccountCacheService accountCacheService;

  private AccountAdapterService accountAdapterService;

  private String accountNumber;
  private String tenantId;
  private String businessUnitId;
  private String correlationId;

  @BeforeEach
  void setUp() {
    accountAdapterService =
        new AccountAdapterService(
            accountServiceClient, accountServiceFallback, oAuth2TokenService, accountCacheService);

    accountNumber = "12345678901";
    tenantId = "tenant-123";
    businessUnitId = "business-unit-456";
    correlationId = UUID.randomUUID().toString();
  }

  /** Test successful account balance retrieval */
  @Test
  void testGetAccountBalance_Success() {
    // Given
    when(accountCacheService.getCachedAccountBalance(accountNumber, tenantId)).thenReturn(null);
    when(oAuth2TokenService.getAccessToken()).thenReturn("test-token");

    AccountBalanceResponse expectedResponse = createAccountBalanceResponse();
    when(accountServiceClient.getAccountBalance(any(), anyString(), anyString(), anyString()))
        .thenReturn(expectedResponse);

    // When
    CompletableFuture<AccountBalanceResponse> result =
        accountAdapterService.getAccountBalance(
            accountNumber, tenantId, businessUnitId, correlationId);

    // Then
    assertThat(result).isNotNull();
    AccountBalanceResponse response = result.join();
    assertThat(response).isNotNull();
    assertThat(response.getAccountNumber()).isEqualTo(accountNumber);
    assertThat(response.getResponseCode()).isEqualTo("SUCCESS");

    verify(accountCacheService).getCachedAccountBalance(accountNumber, tenantId);
    verify(oAuth2TokenService).getAccessToken();
    verify(accountServiceClient).getAccountBalance(any(), anyString(), anyString(), anyString());
    verify(accountCacheService).cacheAccountBalance(accountNumber, tenantId, expectedResponse);
  }

  /** Test account balance retrieval with cache hit */
  @Test
  void testGetAccountBalance_WithCacheHit() {
    // Given
    AccountBalanceResponse cachedResponse = createAccountBalanceResponse();
    when(accountCacheService.getCachedAccountBalance(accountNumber, tenantId))
        .thenReturn(cachedResponse);

    // When
    CompletableFuture<AccountBalanceResponse> result =
        accountAdapterService.getAccountBalance(
            accountNumber, tenantId, businessUnitId, correlationId);

    // Then
    assertThat(result).isNotNull();
    AccountBalanceResponse response = result.join();
    assertThat(response).isEqualTo(cachedResponse);

    verify(accountCacheService).getCachedAccountBalance(accountNumber, tenantId);
    verifyNoInteractions(oAuth2TokenService);
    verifyNoInteractions(accountServiceClient);
    verifyNoMoreInteractions(accountCacheService);
  }

  /** Test account balance retrieval with fallback */
  @Test
  void testGetAccountBalance_WithFallback() {
    // Given
    when(accountCacheService.getCachedAccountBalance(accountNumber, tenantId)).thenReturn(null);
    when(oAuth2TokenService.getAccessToken()).thenReturn("test-token");
    when(accountServiceClient.getAccountBalance(any(), anyString(), anyString(), anyString()))
        .thenThrow(new RuntimeException("Service unavailable"));

    AccountBalanceResponse fallbackResponse = createFallbackAccountBalanceResponse();
    when(accountServiceFallback.getAccountBalance(any(), anyString(), anyString(), anyString()))
        .thenReturn(fallbackResponse);

    // When
    CompletableFuture<AccountBalanceResponse> result =
        accountAdapterService.getAccountBalance(
            accountNumber, tenantId, businessUnitId, correlationId);

    // Then
    assertThat(result).isNotNull();
    AccountBalanceResponse response = result.join();
    assertThat(response).isNotNull();
    assertThat(response.getResponseCode()).isEqualTo("FALLBACK");

    verify(accountCacheService).getCachedAccountBalance(accountNumber, tenantId);
    verify(oAuth2TokenService).getAccessToken();
    verify(accountServiceClient).getAccountBalance(any(), anyString(), anyString(), anyString());
    verify(accountServiceFallback).getAccountBalance(any(), anyString(), anyString(), anyString());
  }

  /** Test successful account validation */
  @Test
  void testValidateAccount_Success() {
    // Given
    when(accountCacheService.getCachedAccountValidation(accountNumber, tenantId)).thenReturn(null);
    when(oAuth2TokenService.getAccessToken()).thenReturn("test-token");

    AccountValidationResponse expectedResponse = createAccountValidationResponse();
    when(accountServiceClient.validateAccount(any(), anyString(), anyString(), anyString()))
        .thenReturn(expectedResponse);

    // When
    CompletableFuture<AccountValidationResponse> result =
        accountAdapterService.validateAccount(
            accountNumber, tenantId, businessUnitId, correlationId);

    // Then
    assertThat(result).isNotNull();
    AccountValidationResponse response = result.join();
    assertThat(response).isNotNull();
    assertThat(response.getAccountNumber()).isEqualTo(accountNumber);
    assertThat(response.isValid()).isTrue();

    verify(accountCacheService).getCachedAccountValidation(accountNumber, tenantId);
    verify(oAuth2TokenService).getAccessToken();
    verify(accountServiceClient).validateAccount(any(), anyString(), anyString(), anyString());
    verify(accountCacheService).cacheAccountValidation(accountNumber, tenantId, expectedResponse);
  }

  /** Test account validation with cache hit */
  @Test
  void testValidateAccount_WithCacheHit() {
    // Given
    AccountValidationResponse cachedResponse = createAccountValidationResponse();
    when(accountCacheService.getCachedAccountValidation(accountNumber, tenantId))
        .thenReturn(cachedResponse);

    // When
    CompletableFuture<AccountValidationResponse> result =
        accountAdapterService.validateAccount(
            accountNumber, tenantId, businessUnitId, correlationId);

    // Then
    assertThat(result).isNotNull();
    AccountValidationResponse response = result.join();
    assertThat(response).isEqualTo(cachedResponse);

    verify(accountCacheService).getCachedAccountValidation(accountNumber, tenantId);
    verifyNoInteractions(oAuth2TokenService);
    verifyNoInteractions(accountServiceClient);
    verifyNoMoreInteractions(accountCacheService);
  }

  /** Test successful account status retrieval */
  @Test
  void testGetAccountStatus_Success() {
    // Given
    when(accountCacheService.getCachedAccountStatus(accountNumber, tenantId)).thenReturn(null);
    when(oAuth2TokenService.getAccessToken()).thenReturn("test-token");

    AccountStatusResponse expectedResponse = createAccountStatusResponse();
    when(accountServiceClient.getAccountStatus(any(), anyString(), anyString(), anyString()))
        .thenReturn(expectedResponse);

    // When
    CompletableFuture<AccountStatusResponse> result =
        accountAdapterService.getAccountStatus(
            accountNumber, tenantId, businessUnitId, correlationId);

    // Then
    assertThat(result).isNotNull();
    AccountStatusResponse response = result.join();
    assertThat(response).isNotNull();
    assertThat(response.getAccountNumber()).isEqualTo(accountNumber);
    assertThat(response.getAccountStatus()).isEqualTo("ACTIVE");

    verify(accountCacheService).getCachedAccountStatus(accountNumber, tenantId);
    verify(oAuth2TokenService).getAccessToken();
    verify(accountServiceClient).getAccountStatus(any(), anyString(), anyString(), anyString());
    verify(accountCacheService).cacheAccountStatus(accountNumber, tenantId, expectedResponse);
  }

  /** Test account status retrieval with cache hit */
  @Test
  void testGetAccountStatus_WithCacheHit() {
    // Given
    AccountStatusResponse cachedResponse = createAccountStatusResponse();
    when(accountCacheService.getCachedAccountStatus(accountNumber, tenantId))
        .thenReturn(cachedResponse);

    // When
    CompletableFuture<AccountStatusResponse> result =
        accountAdapterService.getAccountStatus(
            accountNumber, tenantId, businessUnitId, correlationId);

    // Then
    assertThat(result).isNotNull();
    AccountStatusResponse response = result.join();
    assertThat(response).isEqualTo(cachedResponse);

    verify(accountCacheService).getCachedAccountStatus(accountNumber, tenantId);
    verifyNoInteractions(oAuth2TokenService);
    verifyNoInteractions(accountServiceClient);
    verifyNoMoreInteractions(accountCacheService);
  }

  /** Create account balance response */
  private AccountBalanceResponse createAccountBalanceResponse() {
    return AccountBalanceResponse.builder()
        .accountNumber(accountNumber)
        .accountHolderName("John Doe")
        .accountType("CHECKING")
        .accountStatus("ACTIVE")
        .availableBalance(BigDecimal.valueOf(1000.00))
        .ledgerBalance(BigDecimal.valueOf(1000.00))
        .currency("ZAR")
        .lastTransactionDate(Instant.now())
        .balanceAsOf(Instant.now())
        .responseCode("SUCCESS")
        .responseMessage("Account balance retrieved successfully")
        .correlationId(correlationId)
        .responseTimestamp(System.currentTimeMillis())
        .requestId("req-123")
        .build();
  }

  /** Create fallback account balance response */
  private AccountBalanceResponse createFallbackAccountBalanceResponse() {
    return AccountBalanceResponse.builder()
        .accountNumber(accountNumber)
        .accountHolderName("Unknown")
        .accountType("Unknown")
        .accountStatus("UNKNOWN")
        .availableBalance(BigDecimal.ZERO)
        .ledgerBalance(BigDecimal.ZERO)
        .currency("ZAR")
        .lastTransactionDate(Instant.now())
        .balanceAsOf(Instant.now())
        .responseCode("FALLBACK")
        .responseMessage("Account service unavailable - using fallback")
        .correlationId(correlationId)
        .responseTimestamp(System.currentTimeMillis())
        .requestId("req-123")
        .build();
  }

  /** Create account validation response */
  private AccountValidationResponse createAccountValidationResponse() {
    return AccountValidationResponse.builder()
        .accountNumber(accountNumber)
        .accountHolderName("John Doe")
        .accountType("CHECKING")
        .accountStatus("ACTIVE")
        .isValid(true)
        .validationStatus("VALID")
        .validationErrors(List.of())
        .validationWarnings(List.of())
        .responseCode("SUCCESS")
        .responseMessage("Account validation successful")
        .correlationId(correlationId)
        .responseTimestamp(System.currentTimeMillis())
        .requestId("req-123")
        .validatedAt(Instant.now())
        .build();
  }

  /** Create account status response */
  private AccountStatusResponse createAccountStatusResponse() {
    return AccountStatusResponse.builder()
        .accountNumber(accountNumber)
        .accountHolderName("John Doe")
        .accountType("CHECKING")
        .accountStatus("ACTIVE")
        .statusReason("Account is active")
        .statusChangedAt(Instant.now())
        .previousStatus("ACTIVE")
        .restrictions(List.of())
        .permissions(List.of("READ", "WRITE"))
        .responseCode("SUCCESS")
        .responseMessage("Account status retrieved successfully")
        .correlationId(correlationId)
        .responseTimestamp(System.currentTimeMillis())
        .requestId("req-123")
        .lastActivityDate(Instant.now())
        .build();
  }
}
