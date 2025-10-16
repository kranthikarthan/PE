package com.payments.routing.domain;

/**
 * Routing Action Type Enum
 *
 * <p>Defines the types of routing actions: - ROUTE_TO_CLEARING_SYSTEM: Route to specific clearing
 * system - SET_PRIORITY: Set routing priority - ADD_METADATA: Add metadata to payment -
 * REJECT_PAYMENT: Reject payment - HOLD_PAYMENT: Hold payment for manual review - NOTIFY: Send
 * notification
 */
public enum RoutingActionType {
  ROUTE_TO_CLEARING_SYSTEM,
  SET_PRIORITY,
  ADD_METADATA,
  REJECT_PAYMENT,
  HOLD_PAYMENT,
  NOTIFY
}
