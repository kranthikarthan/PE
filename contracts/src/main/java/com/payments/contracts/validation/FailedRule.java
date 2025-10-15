package com.payments.contracts.validation;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Failed validation rule DTO Aligns with domain FailedRule value object */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Failed validation rule details")
public class FailedRule {

  @NotBlank(message = "Rule ID is required")
  @Schema(description = "Unique rule identifier", required = true)
  private String ruleId;

  @NotBlank(message = "Rule name is required")
  @Schema(description = "Human-readable rule name", required = true)
  private String ruleName;

  @NotNull(message = "Rule type is required")
  @Schema(description = "Type of validation rule", required = true)
  private RuleType ruleType;

  @NotBlank(message = "Failure reason is required")
  @Schema(description = "Reason for rule failure", required = true)
  private String failureReason;

  @Schema(description = "Additional context about the failure")
  private String context;
}
