package com.payments.payshapadapter.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

/**
 * PayShap Proxy Registry Client
 *
 * <p>OpenFeign client for PayShap Proxy Registry API integration.
 *
 * <p>Provides proxy lookup, registration, and deregistration for instant P2P payments.
 *
 * <p>Configuration:
 *
 * <pre>
 * payshap.proxy-registry.base-url=https://api.payshap.co.za/proxy-registry/v1
 * payshap.proxy-registry.connect-timeout=5000
 * payshap.proxy-registry.read-timeout=10000
 * </pre>
 */
@FeignClient(
    name = "payshap-proxy-registry",
    url = "${payshap.proxy-registry.base-url}",
    configuration = PayShapProxyRegistryConfig.class)
public interface PayShapProxyRegistryClient {

  /**
   * Lookup proxy in PayShap registry
   *
   * @param request Proxy lookup request
   * @return Proxy lookup response with bank account details
   */
  @PostMapping("/proxies/lookup")
  PayShapProxyLookupResponse lookupProxy(@RequestBody PayShapProxyLookupRequest request);

  /**
   * Register proxy in PayShap registry
   *
   * @param request Proxy registration request
   * @return Proxy registration response
   */
  @PostMapping("/proxies/register")
  PayShapProxyRegistrationResponse registerProxy(
      @RequestBody PayShapProxyRegistrationRequest request);

  /**
   * Deregister proxy from PayShap registry
   *
   * @param proxyValue Proxy identifier value
   * @param proxyType Proxy type
   * @param institutionCode Institution code
   * @return Deregistration response
   */
  @DeleteMapping("/proxies/{proxyValue}")
  PayShapProxyDeregistrationResponse deregisterProxy(
      @PathVariable("proxyValue") String proxyValue,
      @RequestParam("proxyType") String proxyType,
      @RequestParam("institutionCode") String institutionCode);

  /**
   * Validate proxy format
   *
   * @param proxyValue Proxy identifier value
   * @param proxyType Proxy type
   * @return Validation response
   */
  @GetMapping("/proxies/validate")
  PayShapProxyValidationResponse validateProxy(
      @RequestParam("proxyValue") String proxyValue, @RequestParam("proxyType") String proxyType);

  /**
   * Get proxy status
   *
   * @param proxyValue Proxy identifier value
   * @param proxyType Proxy type
   * @return Proxy lookup response
   */
  @GetMapping("/proxies/{proxyValue}/status")
  PayShapProxyLookupResponse getProxyStatus(
      @PathVariable("proxyValue") String proxyValue, @RequestParam("proxyType") String proxyType);
}
