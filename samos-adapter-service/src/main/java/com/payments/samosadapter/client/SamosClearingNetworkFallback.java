package com.payments.samosadapter.client;

import com.payments.samosadapter.dto.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * SAMOS Clearing Network Fallback
 *
 * <p>Fallback implementation for SAMOS clearing network client: - Circuit breaker fallback - Error handling - Default responses - Logging
 */
@Slf4j
@Component
public class SamosClearingNetworkFallback implements SamosClearingNetworkClient {

  @Override
  public SamosPaymentSubmissionResponse submitPayment(
      SamosPaymentSubmissionRequest request, String authorization, String correlationId, String tenantId) {

    log.warn(
        "SAMOS clearing network fallback triggered for payment submission: {} with correlation: {}",
        request.getPaymentId(),
        correlationId);

    return SamosPaymentSubmissionResponse.builder()
        .paymentId(request.getPaymentId())
        .transactionId("FALLBACK-" + System.currentTimeMillis())
        .status("FAILED")
        .statusReason("SAMOS clearing network unavailable")
        .submittedAt(Instant.now())
        .responseCode("FALLBACK")
        .responseMessage("SAMOS clearing network unavailable - payment submission failed")
        .correlationId(correlationId)
        .responseTimestamp(System.currentTimeMillis())
        .requestId(request.getRequestId())
        .build();
  }

  @Override
  public SamosPaymentStatusResponse checkPaymentStatus(
      SamosPaymentStatusRequest request, String authorization, String correlationId, String tenantId) {

    log.warn(
        "SAMOS clearing network fallback triggered for payment status: {} with correlation: {}",
        request.getPaymentId(),
        correlationId);

    return SamosPaymentStatusResponse.builder()
        .paymentId(request.getPaymentId())
        .transactionId("FALLBACK-" + System.currentTimeMillis())
        .status("UNKNOWN")
        .statusReason("SAMOS clearing network unavailable")
        .lastUpdatedAt(Instant.now())
        .responseCode("FALLBACK")
        .responseMessage("SAMOS clearing network unavailable - status unknown")
        .correlationId(correlationId)
        .responseTimestamp(System.currentTimeMillis())
        .requestId(request.getRequestId())
        .build();
  }

  @Override
  public SamosSettlementResponse processSettlement(
      SamosSettlementRequest request, String authorization, String correlationId, String tenantId) {

    log.warn(
        "SAMOS clearing network fallback triggered for settlement: {} with correlation: {}",
        request.getSettlementId(),
        correlationId);

    return SamosSettlementResponse.builder()
        .settlementId(request.getSettlementId())
        .status("FAILED")
        .statusReason("SAMOS clearing network unavailable")
        .processedAt(Instant.now())
        .responseCode("FALLBACK")
        .responseMessage("SAMOS clearing network unavailable - settlement failed")
        .correlationId(correlationId)
        .responseTimestamp(System.currentTimeMillis())
        .requestId(request.getRequestId())
        .build();
  }

  @Override
  public SamosIso20022ValidationResponse validateIso20022Message(
      SamosIso20022ValidationRequest request, String authorization, String correlationId, String tenantId) {

    log.warn(
        "SAMOS clearing network fallback triggered for ISO 20022 validation: {} with correlation: {}",
        request.getMessageId(),
        correlationId);

    return SamosIso20022ValidationResponse.builder()
        .messageId(request.getMessageId())
        .isValid(false)
        .validationStatus("FALLBACK")
        .validationErrors(List.of("SAMOS clearing network unavailable"))
        .validationWarnings(List.of("Using fallback response"))
        .responseCode("FALLBACK")
        .responseMessage("SAMOS clearing network unavailable - validation failed")
        .correlationId(correlationId)
        .responseTimestamp(System.currentTimeMillis())
        .requestId(request.getRequestId())
        .validatedAt(Instant.now())
        .build();
  }
}
