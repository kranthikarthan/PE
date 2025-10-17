package com.payments.samosadapter.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** SAMOS ISO 20022 Validation Request */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SamosIso20022ValidationRequest {
  private String messageId;
  private String requestId;
  private String messageType;
  private String messageContent;
  private String tenantId;
  private String businessUnitId;
}
