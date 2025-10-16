package com.payments.routing.domain;

/**
 * Logical Operator Enum
 *
 * <p>Defines the logical operators for combining conditions: - AND: All conditions must be true -
 * OR: At least one condition must be true - NOT: Condition must be false
 */
public enum LogicalOperator {
  AND,
  OR,
  NOT
}
