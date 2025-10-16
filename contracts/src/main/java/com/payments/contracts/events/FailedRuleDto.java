package com.payments.contracts.events;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Failed Rule DTO
 *
 * <p>Event DTO for failed validation rules: - Rule identification - Failure reason - Failure
 * timestamp - Field that failed validation
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Failed rule DTO")
public class FailedRuleDto {

  @Schema(description = "Rule ID", example = "BUSINESS_RULE_001")
  private String ruleId;

  @Schema(description = "Rule name", example = "Amount Limit Check")
  private String ruleName;

  @Schema(description = "Rule type", example = "BUSINESS")
  private String ruleType;

  @Schema(description = "Failure reason", example = "Payment amount exceeds maximum limit")
  private String failureReason;

  @Schema(description = "Field that failed validation", example = "amount")
  private String field;

  @Schema(description = "Failure timestamp")
  private Instant failedAt;
}
