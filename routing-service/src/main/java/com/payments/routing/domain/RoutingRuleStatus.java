package com.payments.routing.domain;

/**
 * Routing Rule Status Enum
 *
 * <p>Defines the status of routing rules: - ACTIVE: Rule is active and can be used - INACTIVE: Rule
 * is inactive and cannot be used - DRAFT: Rule is in draft state - ARCHIVED: Rule is archived
 */
public enum RoutingRuleStatus {
  ACTIVE,
  INACTIVE,
  DRAFT,
  ARCHIVED
}
