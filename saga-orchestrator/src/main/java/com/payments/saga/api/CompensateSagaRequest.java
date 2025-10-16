package com.payments.saga.api;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Request DTO for compensating a saga */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to compensate a saga")
public class CompensateSagaRequest {

  @Schema(
      description = "Compensation reason",
      example = "Payment validation failed",
      required = true)
  private String reason;
}
