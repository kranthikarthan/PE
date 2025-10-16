package com.payments.contracts.events;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

/**
 * Validation Response DTO
 * 
 * Event DTO for validation response:
 * - Validation outcome and metadata
 * - Risk and fraud assessment
 * - Applied and failed rules
 * - Validation timestamp
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Validation response DTO")
public class ValidationResponseDto {
    
    @Schema(description = "Validation ID", example = "val_123456789")
    private String validationId;
    
    @Schema(description = "Validation status", example = "PASSED")
    private String status;
    
    @Schema(description = "Fraud score (0-100)", example = "25")
    private Integer fraudScore;
    
    @Schema(description = "Risk score (0-100)", example = "30")
    private Integer riskScore;
    
    @Schema(description = "Risk level", example = "LOW")
    private String riskLevel;
    
    @Schema(description = "List of applied rules")
    private List<String> appliedRules;
    
    @Schema(description = "List of failed rules")
    private List<FailedRuleDto> failedRules;
    
    @Schema(description = "Validation reason")
    private String reason;
    
    @Schema(description = "Validation timestamp")
    private Instant validatedAt;
    
    @Schema(description = "Validator service")
    private String validatorService;
}
