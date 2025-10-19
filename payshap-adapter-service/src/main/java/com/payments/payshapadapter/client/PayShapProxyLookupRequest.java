package com.payments.payshapadapter.client;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * PayShap Proxy Lookup Request
 *
 * <p>Request to lookup bank account details from a proxy identifier (mobile number, email, etc.) in
 * the PayShap Proxy Registry.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PayShapProxyLookupRequest {

  /** Proxy identifier (mobile number, email, ID, etc.) */
  @NotBlank(message = "Proxy value is required")
  private String proxyValue;

  /** Type of proxy identifier */
  @NotNull(message = "Proxy type is required")
  private PayShapProxyType proxyType;

  /** Requesting institution code (optional for validation) */
  private String requestingInstitutionCode;

  /** Transaction reference (for audit trail) */
  private String transactionReference;
}
