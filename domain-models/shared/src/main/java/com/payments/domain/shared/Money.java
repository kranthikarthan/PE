package com.payments.domain.shared;

import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;
import lombok.Value;

/**
 * Money - Value Object (Immutable)
 *
 * <p>Encapsulates amount and currency with business rules
 */
@Embeddable
@Value // Lombok: Immutable, equals/hashCode based on fields
@NoArgsConstructor(force = true, access = AccessLevel.PROTECTED)
public class Money {

  BigDecimal amount;

  Currency currency;

  // Private constructor - use factory methods
  private Money(BigDecimal amount, Currency currency) {
    if (amount == null) {
      throw new IllegalArgumentException("Amount cannot be null");
    }
    if (currency == null) {
      throw new IllegalArgumentException("Currency cannot be null");
    }

    // Store with consistent scale (2 decimal places)
    this.amount = amount.setScale(2, RoundingMode.HALF_UP);
    this.currency = currency;
  }

  // Factory methods
  public static Money of(BigDecimal amount, Currency currency) {
    return new Money(amount, currency);
  }

  public static Money zar(BigDecimal amount) {
    return new Money(amount, Currency.getInstance("ZAR"));
  }

  public static Money zero(Currency currency) {
    return new Money(BigDecimal.ZERO, currency);
  }

  // Business methods
  public Money add(Money other) {
    assertSameCurrency(other);
    return new Money(this.amount.add(other.amount), this.currency);
  }

  public Money subtract(Money other) {
    assertSameCurrency(other);
    return new Money(this.amount.subtract(other.amount), this.currency);
  }

  public Money multiply(BigDecimal multiplier) {
    return new Money(this.amount.multiply(multiplier), this.currency);
  }

  public boolean isGreaterThan(Money other) {
    assertSameCurrency(other);
    return this.amount.compareTo(other.amount) > 0;
  }

  public boolean isLessThan(Money other) {
    assertSameCurrency(other);
    return this.amount.compareTo(other.amount) < 0;
  }

  public boolean isNegativeOrZero() {
    return this.amount.compareTo(BigDecimal.ZERO) <= 0;
  }

  private void assertSameCurrency(Money other) {
    if (!this.currency.equals(other.currency)) {
      throw new IllegalArgumentException(
          "Cannot operate on different currencies: " + this.currency + " and " + other.currency);
    }
  }
}
