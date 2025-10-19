package com.payments.payshapadapter.client;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * PayShap Proxy Registration Request
 *
 * <p>Request to register a new proxy identifier in the PayShap Proxy Registry, linking it to a bank
 * account.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PayShapProxyRegistrationRequest {

  /** Proxy identifier (mobile number, email, ID, etc.) */
  @NotBlank(message = "Proxy value is required")
  private String proxyValue;

  /** Type of proxy identifier */
  @NotNull(message = "Proxy type is required")
  private PayShapProxyType proxyType;

  /** Bank account number to link */
  @NotBlank(message = "Account number is required")
  private String accountNumber;

  /** Bank branch code */
  @NotBlank(message = "Branch code is required")
  private String branchCode;

  /** Bank universal branch code (6-digit) */
  @NotBlank(message = "Universal branch code is required")
  private String universalBranchCode;

  /** Account holder name */
  @NotBlank(message = "Account holder name is required")
  private String accountHolderName;

  /** Account holder ID number */
  @NotBlank(message = "Account holder ID is required")
  private String accountHolderIdNumber;

  /** Account type (SAVINGS, CURRENT, TRANSMISSION) */
  @NotBlank(message = "Account type is required")
  private String accountType;

  /** Institution code */
  @NotBlank(message = "Institution code is required")
  private String institutionCode;

  /** Transaction reference (for audit trail) */
  private String transactionReference;

  /** Customer consent confirmation */
  @NotNull(message = "Customer consent is required")
  private Boolean customerConsentConfirmed;
}
