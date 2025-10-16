package com.payments.routing.domain;

/**
 * Routing Operator Enum
 *
 * <p>Defines the operators for routing conditions: - EQUALS: Field equals value - NOT_EQUALS: Field
 * does not equal value - GREATER_THAN: Field is greater than value - GREATER_THAN_OR_EQUALS: Field
 * is greater than or equal to value - LESS_THAN: Field is less than value - LESS_THAN_OR_EQUALS:
 * Field is less than or equal to value - CONTAINS: Field contains value - NOT_CONTAINS: Field does
 * not contain value - IN: Field is in list of values - NOT_IN: Field is not in list of values -
 * REGEX: Field matches regex pattern - NOT_REGEX: Field does not match regex pattern
 */
public enum RoutingOperator {
  EQUALS,
  NOT_EQUALS,
  GREATER_THAN,
  GREATER_THAN_OR_EQUALS,
  LESS_THAN,
  LESS_THAN_OR_EQUALS,
  CONTAINS,
  NOT_CONTAINS,
  IN,
  NOT_IN,
  REGEX,
  NOT_REGEX,
  IS_NULL,
  IS_NOT_NULL
}
