package com.payments.samosadapter.controller;

import com.payments.samosadapter.client.SamosCertificateInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * SAMOS Certificate Monitoring Controller
 *
 * <p>Provides endpoints for monitoring mTLS certificate status and expiry.
 */
@RestController
@RequestMapping("/api/v1/samos/certificates")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "SAMOS Certificate Monitoring", description = "mTLS certificate monitoring APIs")
@ConditionalOnProperty(prefix = "samos.mtls", name = "enabled", havingValue = "true")
public class SamosCertificateMonitoringController {

  private final SamosCertificateInfo certificateInfo;

  @Operation(
      summary = "Get certificate information",
      description = "Get detailed information about the SAMOS mTLS client certificate")
  @GetMapping("/info")
  public ResponseEntity<SamosCertificateInfo> getCertificateInfo() {
    log.debug("GET /api/v1/samos/certificates/info");
    return ResponseEntity.ok(certificateInfo);
  }

  @Operation(
      summary = "Check certificate health",
      description = "Health check endpoint for certificate status")
  @GetMapping("/health")
  public ResponseEntity<CertificateHealthResponse> checkCertificateHealth() {
    log.debug("GET /api/v1/samos/certificates/health");

    boolean healthy = certificateInfo.isValid();
    long daysUntilExpiry = certificateInfo.getDaysUntilExpiry();
    SamosCertificateInfo.CertificateStatus status = certificateInfo.getStatus();

    CertificateHealthResponse response =
        CertificateHealthResponse.builder()
            .healthy(healthy)
            .status(status.name())
            .daysUntilExpiry(daysUntilExpiry)
            .message(getHealthMessage(status, daysUntilExpiry))
            .build();

    if (healthy) {
      return ResponseEntity.ok(response);
    } else {
      return ResponseEntity.status(503).body(response); // Service Unavailable
    }
  }

  private String getHealthMessage(
      SamosCertificateInfo.CertificateStatus status, long daysUntilExpiry) {
    switch (status) {
      case VALID:
        return "Certificate is valid. Expires in " + daysUntilExpiry + " days.";
      case EXPIRING:
        return "Certificate expires in " + daysUntilExpiry + " days. Plan renewal.";
      case EXPIRING_SOON:
        return "ALERT: Certificate expires in " + daysUntilExpiry + " days! Renew immediately!";
      case EXPIRED:
        return "CRITICAL: Certificate has EXPIRED! SAMOS connection unavailable.";
      case NOT_YET_VALID:
        return "Certificate is not yet valid. Check system time.";
      case NOT_FOUND:
        return "Certificate not found. Check configuration.";
      case ERROR:
        return "Error loading certificate: " + certificateInfo.getErrorMessage();
      default:
        return "Unknown certificate status.";
    }
  }

  /** Certificate Health Response DTO */
  @lombok.Data
  @lombok.Builder
  @lombok.NoArgsConstructor
  @lombok.AllArgsConstructor
  public static class CertificateHealthResponse {
    private Boolean healthy;
    private String status;
    private Long daysUntilExpiry;
    private String message;
  }
}
