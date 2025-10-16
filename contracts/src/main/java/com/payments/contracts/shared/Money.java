package com.payments.contracts.shared;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Currency;

/**
 * Money Value Object
 *
 * <p>Represents monetary amounts with currency: - Amount value - Currency code - Validation
 * rules - Immutable value object
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Money value object")
public class Money {

  @NotNull(message = "Amount is required")
  @DecimalMin(value = "0.0", inclusive = false, message = "Amount must be positive")
  @Schema(description = "Monetary amount", required = true, example = "1000.00")
  private BigDecimal amount;

  @NotNull(message = "Currency is required")
  @Schema(description = "Currency code", required = true, example = "ZAR")
  private Currency currency;

  @Schema(description = "Formatted amount with currency", example = "ZAR 1,000.00")
  private String formattedAmount;

  /**
   * Check if the amount is positive
   *
   * @return true if amount is positive
   */
  public boolean isPositive() {
    return amount != null && amount.compareTo(BigDecimal.ZERO) > 0;
  }

  /**
   * Check if the amount is zero
   *
   * @return true if amount is zero
   */
  public boolean isZero() {
    return amount != null && amount.compareTo(BigDecimal.ZERO) == 0;
  }

  /**
   * Check if the amount is negative
   *
   * @return true if amount is negative
   */
  public boolean isNegative() {
    return amount != null && amount.compareTo(BigDecimal.ZERO) < 0;
  }
}
