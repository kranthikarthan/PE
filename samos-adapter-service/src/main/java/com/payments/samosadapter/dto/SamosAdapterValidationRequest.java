package com.payments.samosadapter.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * SAMOS Adapter Validation Request
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SamosAdapterValidationRequest {
    private String adapterId;
    private String requestId;
    private String tenantId;
    private String businessUnitId;
    private String validationType;
}
