package com.payments.samosadapter.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Request DTO for creating a SAMOS settlement account */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SamosSettlementAccountCreateRequest {

  @NotBlank(message = "Tenant ID is required")
  private String tenantId;

  @NotBlank(message = "Account number is required")
  @Pattern(regexp = "\\d{9}", message = "Account number must be 9 digits")
  private String accountNumber;

  @NotBlank(message = "Bank code is required")
  @Size(max = 6, message = "Bank code must be max 6 characters")
  private String bankCode;

  @NotBlank(message = "Bank name is required")
  @Size(max = 255, message = "Bank name must be max 255 characters")
  private String bankName;

  @NotNull(message = "Daily debit limit is required")
  @DecimalMin(value = "0.01", message = "Daily debit limit must be positive")
  @Digits(integer = 16, fraction = 2, message = "Daily debit limit must have max 2 decimal places")
  private BigDecimal dailyDebitLimit;

  @NotNull(message = "Collateral requirement is required")
  @DecimalMin(value = "0.00", message = "Collateral requirement cannot be negative")
  @Digits(
      integer = 16,
      fraction = 2,
      message = "Collateral requirement must have max 2 decimal places")
  private BigDecimal collateralRequirement;

  @NotBlank(message = "Created by is required")
  private String createdBy;

  @Email(message = "SARB contact email must be valid")
  private String sarbContactEmail;

  @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "SARB contact phone must be 10-15 digits")
  private String sarbContactPhone;
}
