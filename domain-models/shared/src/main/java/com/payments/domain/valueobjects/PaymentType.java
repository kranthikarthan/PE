package com.payments.domain.valueobjects;

/** Value object for payment type */
public enum PaymentType {
  CREDIT_TRANSFER("Credit Transfer"),
  DEBIT_TRANSFER("Debit Transfer"),
  RTGS("Real Time Gross Settlement"),
  RTC("Real Time Clearing"),
  EFT("Electronic Funds Transfer"),
  CARD_PAYMENT("Card Payment"),
  WALLET_TRANSFER("Wallet Transfer");

  private final String description;

  PaymentType(String description) {
    this.description = description;
  }

  public String getDescription() {
    return description;
  }
}
