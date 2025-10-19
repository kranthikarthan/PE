package com.payments.samosadapter.client;

import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * SAMOS Certificate Information
 *
 * <p>Contains information about the mTLS client certificate for monitoring and alerting purposes.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SamosCertificateInfo {

  private String alias;
  private String subject;
  private String issuer;
  private String serialNumber;
  private Date notBefore;
  private Date notAfter;
  private String signatureAlgorithm;
  private Integer version;
  private CertificateStatus status;
  private String errorMessage;

  /**
   * Calculate days until expiry
   *
   * @return Days until certificate expires (negative if expired)
   */
  public long getDaysUntilExpiry() {
    if (notAfter == null) {
      return -1;
    }
    long diff = notAfter.getTime() - System.currentTimeMillis();
    return diff / (1000 * 60 * 60 * 24);
  }

  /**
   * Check if certificate is expired
   *
   * @return true if expired
   */
  public boolean isExpired() {
    return notAfter != null && notAfter.before(new Date());
  }

  /**
   * Check if certificate is expiring soon (within threshold)
   *
   * @param days Threshold in days
   * @return true if expiring within threshold
   */
  public boolean isExpiringSoon(int days) {
    return getDaysUntilExpiry() <= days && getDaysUntilExpiry() > 0;
  }

  /**
   * Check if certificate is valid
   *
   * @return true if valid (not expired, not before start date)
   */
  public boolean isValid() {
    Date now = new Date();
    return notBefore != null && notAfter != null && now.after(notBefore) && now.before(notAfter);
  }

  /**
   * Get certificate status
   *
   * @return Certificate status
   */
  public CertificateStatus getStatus() {
    if (status != null) {
      return status;
    }

    if (isExpired()) {
      return CertificateStatus.EXPIRED;
    } else if (isExpiringSoon(30)) {
      return CertificateStatus.EXPIRING_SOON;
    } else if (isExpiringSoon(90)) {
      return CertificateStatus.EXPIRING;
    } else if (isValid()) {
      return CertificateStatus.VALID;
    } else {
      return CertificateStatus.NOT_YET_VALID;
    }
  }

  /**
   * Create certificate info for not found certificate
   *
   * @param alias Certificate alias
   * @return Certificate info with NOT_FOUND status
   */
  public static SamosCertificateInfo notFound(String alias) {
    return SamosCertificateInfo.builder()
        .alias(alias)
        .status(CertificateStatus.NOT_FOUND)
        .errorMessage("Certificate not found with alias: " + alias)
        .build();
  }

  /**
   * Create certificate info for error condition
   *
   * @param alias Certificate alias
   * @param errorMessage Error message
   * @return Certificate info with ERROR status
   */
  public static SamosCertificateInfo error(String alias, String errorMessage) {
    return SamosCertificateInfo.builder()
        .alias(alias)
        .status(CertificateStatus.ERROR)
        .errorMessage(errorMessage)
        .build();
  }

  /** Certificate Status Enum */
  public enum CertificateStatus {
    VALID,
    EXPIRING, // Within 90 days
    EXPIRING_SOON, // Within 30 days
    EXPIRED,
    NOT_YET_VALID,
    NOT_FOUND,
    ERROR
  }
}
