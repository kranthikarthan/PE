package com.payments.domain.shared;

import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.Value;

/** AccountNumber - Value Object */
@Embeddable
@Value
@NoArgsConstructor(force = true, access = AccessLevel.PROTECTED)
public class AccountNumber {
  String value;

  private AccountNumber(String value) {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException("AccountNumber cannot be null or blank");
    }
    // Validate format (South African account numbers are 11 digits)
    if (!value.matches("\\d{11}")) {
      throw new IllegalArgumentException("Invalid account number format. Must be 11 digits");
    }
    this.value = value;
  }

  public static AccountNumber of(String value) {
    return new AccountNumber(value);
  }
}
