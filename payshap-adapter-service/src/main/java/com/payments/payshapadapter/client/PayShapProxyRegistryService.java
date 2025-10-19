package com.payments.payshapadapter.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * PayShap Proxy Registry Service
 *
 * <p>Service layer for PayShap Proxy Registry operations with resilience patterns.
 *
 * <p>Features:
 *
 * <ul>
 *   <li>Proxy lookup with circuit breaker
 *   <li>Proxy registration with retry
 *   <li>Proxy deregistration
 *   <li>Proxy validation
 *   <li>Automatic transaction reference generation
 *   <li>Comprehensive error handling
 * </ul>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PayShapProxyRegistryService {

  private final PayShapProxyRegistryClient registryClient;

  /**
   * Lookup proxy in PayShap registry
   *
   * @param proxyValue Proxy identifier value
   * @param proxyType Proxy type
   * @param institutionCode Requesting institution code
   * @return Proxy lookup response
   */
  @CircuitBreaker(name = "payshap-proxy-registry", fallbackMethod = "lookupProxyFallback")
  @Retry(name = "payshap-proxy-registry")
  @TimeLimiter(name = "payshap-proxy-registry")
  public PayShapProxyLookupResponse lookupProxy(
      String proxyValue, PayShapProxyType proxyType, String institutionCode) {
    log.info("Looking up PayShap proxy: {} ({})", proxyValue, proxyType);

    PayShapProxyLookupRequest request =
        PayShapProxyLookupRequest.builder()
            .proxyValue(proxyValue)
            .proxyType(proxyType)
            .requestingInstitutionCode(institutionCode)
            .transactionReference(generateTransactionReference())
            .build();

    PayShapProxyLookupResponse response = registryClient.lookupProxy(request);

    if (response.isFound()) {
      log.info(
          "PayShap proxy found: {} -> {} ({})",
          proxyValue,
          response.getAccountNumber(),
          response.getBankName());
    } else {
      log.warn("PayShap proxy not found: {} ({})", proxyValue, proxyType);
    }

    return response;
  }

  /**
   * Register proxy in PayShap registry
   *
   * @param request Proxy registration request
   * @return Proxy registration response
   */
  @CircuitBreaker(name = "payshap-proxy-registry", fallbackMethod = "registerProxyFallback")
  @Retry(name = "payshap-proxy-registry")
  @TimeLimiter(name = "payshap-proxy-registry")
  public PayShapProxyRegistrationResponse registerProxy(PayShapProxyRegistrationRequest request) {
    log.info(
        "Registering PayShap proxy: {} ({}) for account {}",
        request.getProxyValue(),
        request.getProxyType(),
        request.getAccountNumber());

    // Set transaction reference if not provided
    if (request.getTransactionReference() == null) {
      request.setTransactionReference(generateTransactionReference());
    }

    PayShapProxyRegistrationResponse response = registryClient.registerProxy(request);

    if (response.isSuccess()) {
      log.info(
          "PayShap proxy registered successfully: {} -> {} (Status: {})",
          request.getProxyValue(),
          response.getProxyId(),
          response.getProxyStatus());
    } else {
      log.error(
          "PayShap proxy registration failed: {} - {}",
          response.getErrorCode(),
          response.getErrorMessage());
    }

    return response;
  }

  /**
   * Deregister proxy from PayShap registry
   *
   * @param proxyValue Proxy identifier value
   * @param proxyType Proxy type
   * @param institutionCode Institution code
   * @return Deregistration response
   */
  @CircuitBreaker(name = "payshap-proxy-registry", fallbackMethod = "deregisterProxyFallback")
  @Retry(name = "payshap-proxy-registry")
  public PayShapProxyDeregistrationResponse deregisterProxy(
      String proxyValue, PayShapProxyType proxyType, String institutionCode) {
    log.info("Deregistering PayShap proxy: {} ({})", proxyValue, proxyType);

    PayShapProxyDeregistrationResponse response =
        registryClient.deregisterProxy(proxyValue, proxyType.getPayShapCode(), institutionCode);

    if (response.isSuccess()) {
      log.info("PayShap proxy deregistered successfully: {}", proxyValue);
    } else {
      log.error(
          "PayShap proxy deregistration failed: {} - {}",
          response.getErrorCode(),
          response.getErrorMessage());
    }

    return response;
  }

  /**
   * Validate proxy format
   *
   * @param proxyValue Proxy identifier value
   * @param proxyType Proxy type
   * @return Validation response
   */
  @CircuitBreaker(name = "payshap-proxy-registry")
  public PayShapProxyValidationResponse validateProxy(
      String proxyValue, PayShapProxyType proxyType) {
    log.debug("Validating PayShap proxy format: {} ({})", proxyValue, proxyType);

    PayShapProxyValidationResponse response =
        registryClient.validateProxy(proxyValue, proxyType.getPayShapCode());

    if (response.isValid()) {
      log.debug("PayShap proxy format is valid: {}", proxyValue);
    } else {
      log.warn("PayShap proxy format is invalid: {} - {}", proxyValue, response.getMessage());
    }

    return response;
  }

  /**
   * Get proxy status
   *
   * @param proxyValue Proxy identifier value
   * @param proxyType Proxy type
   * @return Proxy lookup response
   */
  @CircuitBreaker(name = "payshap-proxy-registry")
  public PayShapProxyLookupResponse getProxyStatus(String proxyValue, PayShapProxyType proxyType) {
    log.debug("Getting PayShap proxy status: {} ({})", proxyValue, proxyType);

    return registryClient.getProxyStatus(proxyValue, proxyType.getPayShapCode());
  }

  /** Generate unique transaction reference */
  private String generateTransactionReference() {
    return "PSHP-" + UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase();
  }

  // Fallback methods for resilience

  private PayShapProxyLookupResponse lookupProxyFallback(
      String proxyValue, PayShapProxyType proxyType, String institutionCode, Throwable t) {
    log.error("Circuit breaker: Proxy lookup failed for {}: {}", proxyValue, t.getMessage());

    return PayShapProxyLookupResponse.builder()
        .found(false)
        .proxyValue(proxyValue)
        .proxyType(proxyType)
        .errorCode("SERVICE_UNAVAILABLE")
        .errorMessage("PayShap Proxy Registry service unavailable: " + t.getMessage())
        .responseTimestamp(Instant.now())
        .build();
  }

  private PayShapProxyRegistrationResponse registerProxyFallback(
      PayShapProxyRegistrationRequest request, Throwable t) {
    log.error(
        "Circuit breaker: Proxy registration failed for {}: {}",
        request.getProxyValue(),
        t.getMessage());

    return PayShapProxyRegistrationResponse.builder()
        .success(false)
        .proxyValue(request.getProxyValue())
        .proxyType(request.getProxyType())
        .errorCode("SERVICE_UNAVAILABLE")
        .errorMessage("PayShap Proxy Registry service unavailable: " + t.getMessage())
        .build();
  }

  private PayShapProxyDeregistrationResponse deregisterProxyFallback(
      String proxyValue, PayShapProxyType proxyType, String institutionCode, Throwable t) {
    log.error(
        "Circuit breaker: Proxy deregistration failed for {}: {}", proxyValue, t.getMessage());

    return PayShapProxyDeregistrationResponse.builder()
        .success(false)
        .proxyValue(proxyValue)
        .proxyType(proxyType)
        .errorCode("SERVICE_UNAVAILABLE")
        .errorMessage("PayShap Proxy Registry service unavailable: " + t.getMessage())
        .deregistrationTimestamp(Instant.now())
        .build();
  }
}
