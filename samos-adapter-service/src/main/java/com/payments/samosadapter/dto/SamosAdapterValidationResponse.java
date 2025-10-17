package com.payments.samosadapter.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

/**
 * SAMOS Adapter Validation Response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SamosAdapterValidationResponse {
    private String adapterId;
    private Boolean isValid;
    private String validationStatus;
    private List<String> appliedRules;
    private List<String> validationErrors;
    private List<String> validationWarnings;
    private Instant validatedAt;
    private String tenantId;
    private String businessUnitId;
}
