package com.payments.payshapadapter.client;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for PayShapProxyRegistryService
 *
 * <p>Tests proxy registry operations including:
 *
 * <ul>
 *   <li>Proxy lookup
 *   <li>Proxy registration
 *   <li>Proxy deregistration
 *   <li>Proxy validation
 *   <li>Error handling
 *   <li>Resilience patterns
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
class PayShapProxyRegistryServiceTest {

  @Mock private PayShapProxyRegistryClient registryClient;

  @InjectMocks private PayShapProxyRegistryService registryService;

  private static final String PROXY_VALUE = "+27812345678";
  private static final PayShapProxyType PROXY_TYPE = PayShapProxyType.MOBILE_NUMBER;
  private static final String INSTITUTION_CODE = "BANK001";
  private static final String ACCOUNT_NUMBER = "1234567890";

  @BeforeEach
  void setUp() {
    // Setup is done by Mockito annotations
  }

  @Test
  void shouldLookupProxySuccessfully() {
    // Given
    PayShapProxyLookupResponse mockResponse =
        PayShapProxyLookupResponse.builder()
            .found(true)
            .proxyValue(PROXY_VALUE)
            .proxyType(PROXY_TYPE)
            .accountNumber(ACCOUNT_NUMBER)
            .branchCode("632005")
            .universalBranchCode("632005")
            .bankName("Test Bank")
            .accountHolderName("John Doe")
            .accountType("SAVINGS")
            .owningInstitutionCode(INSTITUTION_CODE)
            .proxyStatus("ACTIVE")
            .registrationDate(Instant.now())
            .responseTimestamp(Instant.now())
            .build();

    when(registryClient.lookupProxy(any(PayShapProxyLookupRequest.class))).thenReturn(mockResponse);

    // When
    PayShapProxyLookupResponse response =
        registryService.lookupProxy(PROXY_VALUE, PROXY_TYPE, INSTITUTION_CODE);

    // Then
    assertThat(response).isNotNull();
    assertThat(response.isFound()).isTrue();
    assertThat(response.isActive()).isTrue();
    assertThat(response.getProxyValue()).isEqualTo(PROXY_VALUE);
    assertThat(response.getAccountNumber()).isEqualTo(ACCOUNT_NUMBER);
    assertThat(response.getBankName()).isEqualTo("Test Bank");

    verify(registryClient).lookupProxy(any(PayShapProxyLookupRequest.class));
  }

  @Test
  void shouldHandleProxyNotFound() {
    // Given
    PayShapProxyLookupResponse mockResponse =
        PayShapProxyLookupResponse.builder()
            .found(false)
            .proxyValue(PROXY_VALUE)
            .proxyType(PROXY_TYPE)
            .errorCode("NOT_FOUND")
            .errorMessage("Proxy not registered")
            .responseTimestamp(Instant.now())
            .build();

    when(registryClient.lookupProxy(any(PayShapProxyLookupRequest.class))).thenReturn(mockResponse);

    // When
    PayShapProxyLookupResponse response =
        registryService.lookupProxy(PROXY_VALUE, PROXY_TYPE, INSTITUTION_CODE);

    // Then
    assertThat(response).isNotNull();
    assertThat(response.isFound()).isFalse();
    assertThat(response.isActive()).isFalse();
    assertThat(response.getErrorCode()).isEqualTo("NOT_FOUND");

    verify(registryClient).lookupProxy(any(PayShapProxyLookupRequest.class));
  }

  @Test
  void shouldRegisterProxySuccessfully() {
    // Given
    PayShapProxyRegistrationRequest request =
        PayShapProxyRegistrationRequest.builder()
            .proxyValue(PROXY_VALUE)
            .proxyType(PROXY_TYPE)
            .accountNumber(ACCOUNT_NUMBER)
            .branchCode("632005")
            .universalBranchCode("632005")
            .accountHolderName("John Doe")
            .accountHolderIdNumber("8001015009087")
            .accountType("SAVINGS")
            .institutionCode(INSTITUTION_CODE)
            .customerConsentConfirmed(true)
            .build();

    PayShapProxyRegistrationResponse mockResponse =
        PayShapProxyRegistrationResponse.builder()
            .success(true)
            .proxyValue(PROXY_VALUE)
            .proxyType(PROXY_TYPE)
            .proxyId("PSH123456")
            .proxyStatus("ACTIVE")
            .registrationTimestamp(Instant.now())
            .verificationRequired(false)
            .build();

    when(registryClient.registerProxy(any(PayShapProxyRegistrationRequest.class)))
        .thenReturn(mockResponse);

    // When
    PayShapProxyRegistrationResponse response = registryService.registerProxy(request);

    // Then
    assertThat(response).isNotNull();
    assertThat(response.isSuccess()).isTrue();
    assertThat(response.getProxyId()).isEqualTo("PSH123456");
    assertThat(response.getProxyStatus()).isEqualTo("ACTIVE");
    assertThat(response.isVerificationRequired()).isFalse();

    verify(registryClient).registerProxy(any(PayShapProxyRegistrationRequest.class));
  }

  @Test
  void shouldHandleRegistrationFailure() {
    // Given
    PayShapProxyRegistrationRequest request =
        PayShapProxyRegistrationRequest.builder()
            .proxyValue(PROXY_VALUE)
            .proxyType(PROXY_TYPE)
            .accountNumber(ACCOUNT_NUMBER)
            .branchCode("632005")
            .universalBranchCode("632005")
            .accountHolderName("John Doe")
            .accountHolderIdNumber("8001015009087")
            .accountType("SAVINGS")
            .institutionCode(INSTITUTION_CODE)
            .customerConsentConfirmed(true)
            .build();

    PayShapProxyRegistrationResponse mockResponse =
        PayShapProxyRegistrationResponse.builder()
            .success(false)
            .proxyValue(PROXY_VALUE)
            .proxyType(PROXY_TYPE)
            .errorCode("ALREADY_REGISTERED")
            .errorMessage("Proxy already registered to another account")
            .build();

    when(registryClient.registerProxy(any(PayShapProxyRegistrationRequest.class)))
        .thenReturn(mockResponse);

    // When
    PayShapProxyRegistrationResponse response = registryService.registerProxy(request);

    // Then
    assertThat(response).isNotNull();
    assertThat(response.isSuccess()).isFalse();
    assertThat(response.getErrorCode()).isEqualTo("ALREADY_REGISTERED");

    verify(registryClient).registerProxy(any(PayShapProxyRegistrationRequest.class));
  }

  @Test
  void shouldDeregisterProxySuccessfully() {
    // Given
    PayShapProxyDeregistrationResponse mockResponse =
        PayShapProxyDeregistrationResponse.builder()
            .success(true)
            .proxyValue(PROXY_VALUE)
            .proxyType(PROXY_TYPE)
            .deregistrationTimestamp(Instant.now())
            .transactionReference("TXN123")
            .build();

    when(registryClient.deregisterProxy(anyString(), anyString(), anyString()))
        .thenReturn(mockResponse);

    // When
    PayShapProxyDeregistrationResponse response =
        registryService.deregisterProxy(PROXY_VALUE, PROXY_TYPE, INSTITUTION_CODE);

    // Then
    assertThat(response).isNotNull();
    assertThat(response.isSuccess()).isTrue();
    assertThat(response.getProxyValue()).isEqualTo(PROXY_VALUE);

    verify(registryClient)
        .deregisterProxy(PROXY_VALUE, PROXY_TYPE.getPayShapCode(), INSTITUTION_CODE);
  }

  @Test
  void shouldValidateProxyFormat() {
    // Given
    PayShapProxyValidationResponse mockResponse =
        PayShapProxyValidationResponse.builder()
            .valid(true)
            .proxyValue(PROXY_VALUE)
            .proxyType(PROXY_TYPE)
            .message("Valid mobile number format")
            .build();

    when(registryClient.validateProxy(anyString(), anyString())).thenReturn(mockResponse);

    // When
    PayShapProxyValidationResponse response =
        registryService.validateProxy(PROXY_VALUE, PROXY_TYPE);

    // Then
    assertThat(response).isNotNull();
    assertThat(response.isValid()).isTrue();
    assertThat(response.getMessage()).contains("Valid");

    verify(registryClient).validateProxy(PROXY_VALUE, PROXY_TYPE.getPayShapCode());
  }

  @Test
  void shouldHandleInvalidProxyFormat() {
    // Given
    String invalidProxy = "invalid";
    PayShapProxyValidationResponse mockResponse =
        PayShapProxyValidationResponse.builder()
            .valid(false)
            .proxyValue(invalidProxy)
            .proxyType(PROXY_TYPE)
            .errorCode("INVALID_FORMAT")
            .errorMessage("Invalid mobile number format")
            .build();

    when(registryClient.validateProxy(anyString(), anyString())).thenReturn(mockResponse);

    // When
    PayShapProxyValidationResponse response =
        registryService.validateProxy(invalidProxy, PROXY_TYPE);

    // Then
    assertThat(response).isNotNull();
    assertThat(response.isValid()).isFalse();
    assertThat(response.getErrorCode()).isEqualTo("INVALID_FORMAT");

    verify(registryClient).validateProxy(invalidProxy, PROXY_TYPE.getPayShapCode());
  }

  @Test
  void shouldGetProxyStatus() {
    // Given
    PayShapProxyLookupResponse mockResponse =
        PayShapProxyLookupResponse.builder()
            .found(true)
            .proxyValue(PROXY_VALUE)
            .proxyType(PROXY_TYPE)
            .proxyStatus("ACTIVE")
            .registrationDate(Instant.now())
            .build();

    when(registryClient.getProxyStatus(anyString(), anyString())).thenReturn(mockResponse);

    // When
    PayShapProxyLookupResponse response = registryService.getProxyStatus(PROXY_VALUE, PROXY_TYPE);

    // Then
    assertThat(response).isNotNull();
    assertThat(response.isFound()).isTrue();
    assertThat(response.getProxyStatus()).isEqualTo("ACTIVE");

    verify(registryClient).getProxyStatus(PROXY_VALUE, PROXY_TYPE.getPayShapCode());
  }

  @Test
  void shouldSetTransactionReferenceIfNotProvided() {
    // Given
    PayShapProxyRegistrationRequest request =
        PayShapProxyRegistrationRequest.builder()
            .proxyValue(PROXY_VALUE)
            .proxyType(PROXY_TYPE)
            .accountNumber(ACCOUNT_NUMBER)
            .branchCode("632005")
            .universalBranchCode("632005")
            .accountHolderName("John Doe")
            .accountHolderIdNumber("8001015009087")
            .accountType("SAVINGS")
            .institutionCode(INSTITUTION_CODE)
            .customerConsentConfirmed(true)
            // No transaction reference set
            .build();

    PayShapProxyRegistrationResponse mockResponse =
        PayShapProxyRegistrationResponse.builder().success(true).build();

    when(registryClient.registerProxy(any(PayShapProxyRegistrationRequest.class)))
        .thenReturn(mockResponse);

    // When
    registryService.registerProxy(request);

    // Then
    assertThat(request.getTransactionReference()).isNotNull();
    assertThat(request.getTransactionReference()).startsWith("PSHP-");

    verify(registryClient).registerProxy(request);
  }
}
