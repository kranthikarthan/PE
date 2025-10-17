package com.payments.samosadapter.client;

import com.payments.samosadapter.dto.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

/**
 * SAMOS Clearing Network Feign Client
 *
 * <p>Feign client for SAMOS clearing network integration: - Payment submission - Payment status
 * checking - Settlement processing - ISO 20022 message handling
 */
@FeignClient(
    name = "samos-clearing-network",
    url = "${samos.clearing.network.url}",
    configuration = SamosClearingNetworkClientConfig.class)
public interface SamosClearingNetworkClient {

  /**
   * Submit payment to SAMOS clearing network
   *
   * @param request Payment submission request
   * @param authorization OAuth2 authorization header
   * @param correlationId Correlation ID header
   * @param tenantId Tenant ID header
   * @return Payment submission response
   */
  @PostMapping("/api/v1/samos/payments/submit")
  SamosPaymentSubmissionResponse submitPayment(
      @RequestBody SamosPaymentSubmissionRequest request,
      @RequestHeader("Authorization") String authorization,
      @RequestHeader("X-Correlation-ID") String correlationId,
      @RequestHeader("X-Tenant-ID") String tenantId);

  /**
   * Check payment status in SAMOS clearing network
   *
   * @param request Payment status request
   * @param authorization OAuth2 authorization header
   * @param correlationId Correlation ID header
   * @param tenantId Tenant ID header
   * @return Payment status response
   */
  @PostMapping("/api/v1/samos/payments/status")
  SamosPaymentStatusResponse checkPaymentStatus(
      @RequestBody SamosPaymentStatusRequest request,
      @RequestHeader("Authorization") String authorization,
      @RequestHeader("X-Correlation-ID") String correlationId,
      @RequestHeader("X-Tenant-ID") String tenantId);

  /**
   * Process settlement in SAMOS clearing network
   *
   * @param request Settlement processing request
   * @param authorization OAuth2 authorization header
   * @param correlationId Correlation ID header
   * @param tenantId Tenant ID header
   * @return Settlement processing response
   */
  @PostMapping("/api/v1/samos/settlement/process")
  SamosSettlementResponse processSettlement(
      @RequestBody SamosSettlementRequest request,
      @RequestHeader("Authorization") String authorization,
      @RequestHeader("X-Correlation-ID") String correlationId,
      @RequestHeader("X-Tenant-ID") String tenantId);

  /**
   * Validate ISO 20022 message
   *
   * @param request ISO 20022 validation request
   * @param authorization OAuth2 authorization header
   * @param correlationId Correlation ID header
   * @param tenantId Tenant ID header
   * @return ISO 20022 validation response
   */
  @PostMapping("/api/v1/samos/iso20022/validate")
  SamosIso20022ValidationResponse validateIso20022Message(
      @RequestBody SamosIso20022ValidationRequest request,
      @RequestHeader("Authorization") String authorization,
      @RequestHeader("X-Correlation-ID") String correlationId,
      @RequestHeader("X-Tenant-ID") String tenantId);
}
