package com.payments.payshapadapter.client;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * PayShap Proxy Lookup Response
 *
 * <p>Response from PayShap Proxy Registry containing bank account details associated with a proxy
 * identifier.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PayShapProxyLookupResponse {

  /** Whether the proxy was found */
  private boolean found;

  /** Proxy identifier value */
  private String proxyValue;

  /** Proxy type */
  private PayShapProxyType proxyType;

  /** Bank account number */
  private String accountNumber;

  /** Bank branch code */
  private String branchCode;

  /** Bank universal branch code (6-digit) */
  private String universalBranchCode;

  /** Bank name */
  private String bankName;

  /** Account holder name */
  private String accountHolderName;

  /** Account holder ID number (masked) */
  private String accountHolderIdNumber;

  /** Account type (SAVINGS, CURRENT, TRANSMISSION) */
  private String accountType;

  /** Institution code that owns the proxy */
  private String owningInstitutionCode;

  /** Proxy registration date */
  private Instant registrationDate;

  /** Proxy status (ACTIVE, SUSPENDED, PENDING) */
  private String proxyStatus;

  /** Response timestamp */
  private Instant responseTimestamp;

  /** Transaction reference (for audit) */
  private String transactionReference;

  /** Error code (if found = false) */
  private String errorCode;

  /** Error message (if found = false) */
  private String errorMessage;

  /**
   * Check if proxy is active and valid
   *
   * @return true if proxy can be used for payments
   */
  public boolean isActive() {
    return found && "ACTIVE".equalsIgnoreCase(proxyStatus);
  }
}
