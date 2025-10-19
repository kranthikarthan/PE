package com.payments.domain.valueobjects;

/** Value object for clearing network */
public enum ClearingNetwork {
  SAMOS("SAMOS RTGS"),
  BANKSERV_AFRICA("BankservAfrica"),
  RTC("RTC"),
  PAYSHAP("PayShap"),
  SWIFT("SWIFT");

  private final String description;

  ClearingNetwork(String description) {
    this.description = description;
  }

  public String getDescription() {
    return description;
  }
}
