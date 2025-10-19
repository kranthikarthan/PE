package com.payments.payshapadapter.client;

import com.payments.payshapadapter.service.PayShapOAuth2TokenService;
import feign.RequestInterceptor;
import feign.codec.ErrorDecoder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * PayShap Proxy Registry Feign Client Configuration
 *
 * <p>Configures Feign client with:
 *
 * <ul>
 *   <li>OAuth 2.0 authentication (Bearer token)
 *   <li>Request/response logging
 *   <li>Custom error decoder
 *   <li>Timeout configuration
 * </ul>
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class PayShapProxyRegistryConfig {

  private final PayShapOAuth2TokenService tokenService;

  /**
   * OAuth 2.0 Request Interceptor
   *
   * <p>Adds Bearer token to Authorization header for all requests
   */
  @Bean
  public RequestInterceptor oauth2RequestInterceptor() {
    return requestTemplate -> {
      try {
        String accessToken = tokenService.getAccessToken();
        requestTemplate.header("Authorization", "Bearer " + accessToken);
        log.debug("Added OAuth2 Bearer token to PayShap Proxy Registry request");
      } catch (Exception e) {
        log.error("Failed to obtain OAuth2 token for PayShap Proxy Registry: {}", e.getMessage());
        throw new PayShapProxyRegistryException("Failed to obtain OAuth2 token", e);
      }
    };
  }

  /**
   * Custom Error Decoder
   *
   * <p>Handles PayShap-specific error responses
   */
  @Bean
  public ErrorDecoder errorDecoder() {
    return (methodKey, response) -> {
      log.error(
          "PayShap Proxy Registry error: {} {} - Status: {}",
          response.request().httpMethod(),
          response.request().url(),
          response.status());

      switch (response.status()) {
        case 401:
          return new PayShapProxyRegistryException("Unauthorized: Invalid OAuth2 token", null);
        case 403:
          return new PayShapProxyRegistryException("Forbidden: Insufficient permissions", null);
        case 404:
          return new PayShapProxyRegistryException("Proxy not found in registry", null);
        case 409:
          return new PayShapProxyRegistryException("Proxy already registered", null);
        case 422:
          return new PayShapProxyRegistryException("Invalid proxy data", null);
        case 429:
          return new PayShapProxyRegistryException("Rate limit exceeded", null);
        case 500:
        case 502:
        case 503:
          return new PayShapProxyRegistryException(
              "PayShap Proxy Registry service unavailable", null);
        default:
          return new PayShapProxyRegistryException(
              "PayShap Proxy Registry error: " + response.status(), null);
      }
    };
  }

  /** Custom exception for Proxy Registry operations */
  public static class PayShapProxyRegistryException extends RuntimeException {
    public PayShapProxyRegistryException(String message, Throwable cause) {
      super(message, cause);
    }
  }
}
