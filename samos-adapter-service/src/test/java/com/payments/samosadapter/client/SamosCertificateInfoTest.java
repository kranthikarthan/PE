package com.payments.samosadapter.client;

import static org.assertj.core.api.Assertions.*;

import java.util.Calendar;
import java.util.Date;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Unit tests for SamosCertificateInfo */
@DisplayName("SAMOS Certificate Info Tests")
class SamosCertificateInfoTest {

  @Test
  @DisplayName("Should calculate days until expiry correctly")
  void shouldCalculateDaysUntilExpiryCorrectly() {
    // Given - Certificate expires in 60 days
    Calendar cal = Calendar.getInstance();
    cal.add(Calendar.DAY_OF_MONTH, 60);
    Date expiryDate = cal.getTime();

    SamosCertificateInfo certInfo =
        SamosCertificateInfo.builder()
            .alias("test-cert")
            .notBefore(new Date())
            .notAfter(expiryDate)
            .build();

    // When
    long daysUntilExpiry = certInfo.getDaysUntilExpiry();

    // Then - Should be approximately 60 days (allowing for time precision)
    assertThat(daysUntilExpiry).isBetween(59L, 60L);
  }

  @Test
  @DisplayName("Should detect expired certificate")
  void shouldDetectExpiredCertificate() {
    // Given - Certificate expired yesterday
    Calendar cal = Calendar.getInstance();
    cal.add(Calendar.DAY_OF_MONTH, -1);
    Date expiryDate = cal.getTime();

    SamosCertificateInfo certInfo =
        SamosCertificateInfo.builder()
            .alias("test-cert")
            .notBefore(new Date(System.currentTimeMillis() - 365L * 24 * 60 * 60 * 1000))
            .notAfter(expiryDate)
            .build();

    // When/Then
    assertThat(certInfo.isExpired()).isTrue();
    assertThat(certInfo.isValid()).isFalse();
    assertThat(certInfo.getStatus())
        .isEqualTo(SamosCertificateInfo.CertificateStatus.EXPIRED);
  }

  @Test
  @DisplayName("Should detect certificate expiring soon")
  void shouldDetectCertificateExpiringSoon() {
    // Given - Certificate expires in 20 days
    Calendar cal = Calendar.getInstance();
    cal.add(Calendar.DAY_OF_MONTH, 20);
    Date expiryDate = cal.getTime();

    SamosCertificateInfo certInfo =
        SamosCertificateInfo.builder()
            .alias("test-cert")
            .notBefore(new Date(System.currentTimeMillis() - 365L * 24 * 60 * 60 * 1000))
            .notAfter(expiryDate)
            .build();

    // When/Then
    assertThat(certInfo.isExpiringSoon(30)).isTrue();
    assertThat(certInfo.isExpiringSoon(15)).isFalse();
    assertThat(certInfo.isValid()).isTrue();
    assertThat(certInfo.getStatus())
        .isEqualTo(SamosCertificateInfo.CertificateStatus.EXPIRING_SOON);
  }

  @Test
  @DisplayName("Should detect valid certificate")
  void shouldDetectValidCertificate() {
    // Given - Certificate valid for 180 days
    Calendar cal = Calendar.getInstance();
    cal.add(Calendar.DAY_OF_MONTH, 180);
    Date expiryDate = cal.getTime();

    SamosCertificateInfo certInfo =
        SamosCertificateInfo.builder()
            .alias("test-cert")
            .notBefore(new Date(System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000))
            .notAfter(expiryDate)
            .build();

    // When/Then
    assertThat(certInfo.isValid()).isTrue();
    assertThat(certInfo.isExpired()).isFalse();
    assertThat(certInfo.isExpiringSoon(90)).isFalse();
    assertThat(certInfo.getStatus())
        .isEqualTo(SamosCertificateInfo.CertificateStatus.VALID);
  }

  @Test
  @DisplayName("Should detect certificate expiring within 90 days")
  void shouldDetectCertificateExpiringWithin90Days() {
    // Given - Certificate expires in 60 days
    Calendar cal = Calendar.getInstance();
    cal.add(Calendar.DAY_OF_MONTH, 60);
    Date expiryDate = cal.getTime();

    SamosCertificateInfo certInfo =
        SamosCertificateInfo.builder()
            .alias("test-cert")
            .notBefore(new Date(System.currentTimeMillis() - 365L * 24 * 60 * 60 * 1000))
            .notAfter(expiryDate)
            .build();

    // When/Then
    assertThat(certInfo.isExpiringSoon(90)).isTrue();
    assertThat(certInfo.isValid()).isTrue();
    assertThat(certInfo.getStatus())
        .isEqualTo(SamosCertificateInfo.CertificateStatus.EXPIRING);
  }

  @Test
  @DisplayName("Should detect certificate not yet valid")
  void shouldDetectCertificateNotYetValid() {
    // Given - Certificate valid starting tomorrow
    Calendar startCal = Calendar.getInstance();
    startCal.add(Calendar.DAY_OF_MONTH, 1);
    Date startDate = startCal.getTime();

    Calendar endCal = Calendar.getInstance();
    endCal.add(Calendar.DAY_OF_MONTH, 365);
    Date endDate = endCal.getTime();

    SamosCertificateInfo certInfo =
        SamosCertificateInfo.builder()
            .alias("test-cert")
            .notBefore(startDate)
            .notAfter(endDate)
            .build();

    // When/Then
    assertThat(certInfo.isValid()).isFalse();
    assertThat(certInfo.isExpired()).isFalse();
    assertThat(certInfo.getStatus())
        .isEqualTo(SamosCertificateInfo.CertificateStatus.NOT_YET_VALID);
  }

  @Test
  @DisplayName("Should create not found certificate info")
  void shouldCreateNotFoundCertificateInfo() {
    // When
    SamosCertificateInfo certInfo = SamosCertificateInfo.notFound("missing-cert");

    // Then
    assertThat(certInfo.getAlias()).isEqualTo("missing-cert");
    assertThat(certInfo.getStatus())
        .isEqualTo(SamosCertificateInfo.CertificateStatus.NOT_FOUND);
    assertThat(certInfo.getErrorMessage()).contains("not found");
  }

  @Test
  @DisplayName("Should create error certificate info")
  void shouldCreateErrorCertificateInfo() {
    // When
    SamosCertificateInfo certInfo =
        SamosCertificateInfo.error("error-cert", "Failed to load keystore");

    // Then
    assertThat(certInfo.getAlias()).isEqualTo("error-cert");
    assertThat(certInfo.getStatus()).isEqualTo(SamosCertificateInfo.CertificateStatus.ERROR);
    assertThat(certInfo.getErrorMessage()).isEqualTo("Failed to load keystore");
  }

  @Test
  @DisplayName("Should handle null dates gracefully")
  void shouldHandleNullDatesGracefully() {
    // Given - Certificate with null dates
    SamosCertificateInfo certInfo =
        SamosCertificateInfo.builder().alias("test-cert").build();

    // When/Then - Should not throw exceptions
    assertThat(certInfo.getDaysUntilExpiry()).isEqualTo(-1);
    assertThat(certInfo.isExpired()).isFalse();
    assertThat(certInfo.isValid()).isFalse();
  }

  @Test
  @DisplayName("Should calculate negative days for expired certificate")
  void shouldCalculateNegativeDaysForExpiredCertificate() {
    // Given - Certificate expired 10 days ago
    Calendar cal = Calendar.getInstance();
    cal.add(Calendar.DAY_OF_MONTH, -10);
    Date expiryDate = cal.getTime();

    SamosCertificateInfo certInfo =
        SamosCertificateInfo.builder()
            .alias("test-cert")
            .notBefore(new Date(System.currentTimeMillis() - 365L * 24 * 60 * 60 * 1000))
            .notAfter(expiryDate)
            .build();

    // When
    long daysUntilExpiry = certInfo.getDaysUntilExpiry();

    // Then - Should be negative
    assertThat(daysUntilExpiry).isBetween(-11L, -9L);
    assertThat(certInfo.isExpired()).isTrue();
  }

  @Test
  @DisplayName("Should detect exact expiry threshold")
  void shouldDetectExactExpiryThreshold() {
    // Given - Certificate expires in exactly 30 days
    Calendar cal = Calendar.getInstance();
    cal.add(Calendar.DAY_OF_MONTH, 30);
    Date expiryDate = cal.getTime();

    SamosCertificateInfo certInfo =
        SamosCertificateInfo.builder()
            .alias("test-cert")
            .notBefore(new Date(System.currentTimeMillis() - 365L * 24 * 60 * 60 * 1000))
            .notAfter(expiryDate)
            .build();

    // When/Then
    assertThat(certInfo.isExpiringSoon(30)).isTrue();
    assertThat(certInfo.getStatus())
        .isEqualTo(SamosCertificateInfo.CertificateStatus.EXPIRING_SOON);
  }

  @Test
  @DisplayName("Should build complete certificate info")
  void shouldBuildCompleteCertificateInfo() {
    // Given
    Date startDate = new Date(System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000);
    Date endDate = new Date(System.currentTimeMillis() + 335L * 24 * 60 * 60 * 1000);

    // When
    SamosCertificateInfo certInfo =
        SamosCertificateInfo.builder()
            .alias("samos-client-cert")
            .subject("CN=PaymentEngine,O=MyBank,C=ZA")
            .issuer("CN=SARB CA,O=South African Reserve Bank,C=ZA")
            .serialNumber("1234567890")
            .notBefore(startDate)
            .notAfter(endDate)
            .signatureAlgorithm("SHA256withRSA")
            .version(3)
            .build();

    // Then
    assertThat(certInfo.getAlias()).isEqualTo("samos-client-cert");
    assertThat(certInfo.getSubject()).contains("PaymentEngine");
    assertThat(certInfo.getIssuer()).contains("SARB CA");
    assertThat(certInfo.getSerialNumber()).isEqualTo("1234567890");
    assertThat(certInfo.getSignatureAlgorithm()).isEqualTo("SHA256withRSA");
    assertThat(certInfo.getVersion()).isEqualTo(3);
    assertThat(certInfo.isValid()).isTrue();
  }
}

