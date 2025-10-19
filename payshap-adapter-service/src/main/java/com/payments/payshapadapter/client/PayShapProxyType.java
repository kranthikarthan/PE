package com.payments.payshapadapter.client;

/**
 * PayShap Proxy Types
 *
 * <p>Supported proxy identifiers in PayShap system for instant P2P payments.
 *
 * <ul>
 *   <li>MOBILE_NUMBER - South African mobile numbers (e.g., +27812345678)
 *   <li>EMAIL_ADDRESS - Email addresses
 *   <li>ID_NUMBER - South African ID numbers
 *   <li>PASSPORT_NUMBER - Passport numbers for non-residents
 *   <li>COMPANY_REGISTRATION - Company registration numbers
 * </ul>
 */
public enum PayShapProxyType {
  MOBILE_NUMBER("MOBILE"),
  EMAIL_ADDRESS("EMAIL"),
  ID_NUMBER("ID_NUMBER"),
  PASSPORT_NUMBER("PASSPORT"),
  COMPANY_REGISTRATION("COMPANY_REG");

  private final String payShapCode;

  PayShapProxyType(String payShapCode) {
    this.payShapCode = payShapCode;
  }

  public String getPayShapCode() {
    return payShapCode;
  }

  /**
   * Get enum from PayShap code
   *
   * @param code PayShap code
   * @return ProxyType
   */
  public static PayShapProxyType fromCode(String code) {
    for (PayShapProxyType type : values()) {
      if (type.payShapCode.equals(code)) {
        return type;
      }
    }
    throw new IllegalArgumentException("Unknown PayShap proxy type: " + code);
  }
}
