package com.payments.payshapadapter.client;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * PayShap Proxy Deregistration Response
 *
 * <p>Response from PayShap Proxy Registry after attempting to deregister a proxy identifier.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PayShapProxyDeregistrationResponse {

  /** Whether deregistration was successful */
  private boolean success;

  /** Deregistered proxy identifier */
  private String proxyValue;

  /** Proxy type */
  private PayShapProxyType proxyType;

  /** Deregistration timestamp */
  private Instant deregistrationTimestamp;

  /** Transaction reference */
  private String transactionReference;

  /** Error code (if success = false) */
  private String errorCode;

  /** Error message (if success = false) */
  private String errorMessage;
}
