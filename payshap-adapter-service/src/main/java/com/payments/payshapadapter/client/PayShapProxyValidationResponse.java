package com.payments.payshapadapter.client;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * PayShap Proxy Validation Response
 *
 * <p>Response from PayShap Proxy Registry validating a proxy identifier format.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PayShapProxyValidationResponse {

  /** Whether proxy format is valid */
  private boolean valid;

  /** Proxy identifier value */
  private String proxyValue;

  /** Proxy type */
  private PayShapProxyType proxyType;

  /** Validation message */
  private String message;

  /** Error code (if valid = false) */
  private String errorCode;

  /** Error message (if valid = false) */
  private String errorMessage;
}
