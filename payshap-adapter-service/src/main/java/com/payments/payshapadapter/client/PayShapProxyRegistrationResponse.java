package com.payments.payshapadapter.client;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * PayShap Proxy Registration Response
 *
 * <p>Response from PayShap Proxy Registry after attempting to register a proxy identifier.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PayShapProxyRegistrationResponse {

  /** Whether registration was successful */
  private boolean success;

  /** Registered proxy identifier */
  private String proxyValue;

  /** Proxy type */
  private PayShapProxyType proxyType;

  /** Proxy unique identifier assigned by PayShap */
  private String proxyId;

  /** Registration timestamp */
  private Instant registrationTimestamp;

  /** Proxy status (ACTIVE, PENDING_VERIFICATION) */
  private String proxyStatus;

  /** Transaction reference */
  private String transactionReference;

  /** Verification required (e.g., OTP for mobile) */
  private boolean verificationRequired;

  /** Verification method (SMS, EMAIL) */
  private String verificationMethod;

  /** Error code (if success = false) */
  private String errorCode;

  /** Error message (if success = false) */
  private String errorMessage;
}
