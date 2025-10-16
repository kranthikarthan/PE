package com.payments.accountadapter.dto;

import java.time.Instant;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Account Status Response DTO
 *
 * <p>Response containing account status information: - Account status - Account details - Status
 * history - Response metadata
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountStatusResponse {

  private String accountNumber;
  private String accountHolderName;
  private String accountType;
  private String accountStatus;
  private String statusReason;
  private Instant statusChangedAt;
  private String previousStatus;
  private List<String> restrictions;
  private List<String> permissions;
  private String responseCode;
  private String responseMessage;
  private String correlationId;
  private Long responseTimestamp;
  private String requestId;
  private Instant lastActivityDate;
}
