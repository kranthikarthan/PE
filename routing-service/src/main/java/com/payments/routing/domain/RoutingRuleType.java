package com.payments.routing.domain;

/**
 * Routing Rule Type Enum
 *
 * <p>Defines the types of routing rules: - PAYMENT_TYPE: Based on payment type - AMOUNT_RANGE:
 * Based on payment amount - CURRENCY: Based on currency - TENANT: Based on tenant - BUSINESS_UNIT:
 * Based on business unit - CUSTOM: Custom rule
 */
public enum RoutingRuleType {
  PAYMENT_TYPE,
  AMOUNT_RANGE,
  CURRENCY,
  TENANT,
  BUSINESS_UNIT,
  CUSTOM
}
