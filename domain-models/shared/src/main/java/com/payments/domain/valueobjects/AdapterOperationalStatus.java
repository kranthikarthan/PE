package com.payments.domain.valueobjects;

/** Value object for adapter operational status */
public enum AdapterOperationalStatus {
  ACTIVE("Active"),
  INACTIVE("Inactive"),
  MAINTENANCE("Maintenance"),
  SUSPENDED("Suspended");

  private final String description;

  AdapterOperationalStatus(String description) {
    this.description = description;
  }

  public String getDescription() {
    return description;
  }
}
