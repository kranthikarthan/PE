package com.payments.routing.engine;

import com.payments.routing.domain.RoutingCondition;
import com.payments.routing.domain.RoutingOperator;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Condition Evaluator
 *
 * <p>Evaluates routing conditions against payment attributes: - Numeric comparisons (amount,
 * priority) - String comparisons (currency, payment type) - Date/time comparisons - Regex pattern
 * matching - List membership (IN, NOT_IN)
 *
 * <p>Performance: DSA-optimized data structures, efficient comparisons Resilience: Graceful error
 * handling, validation
 */
@Slf4j
@Component
public class ConditionEvaluator {

  /**
   * Evaluate routing condition against request
   *
   * @param condition Routing condition
   * @param request Routing request
   * @return True if condition matches
   */
  public boolean evaluate(RoutingCondition condition, RoutingRequest request) {
    Object fieldValue = getFieldValue(condition.getFieldName(), request);
    String expectedValue = condition.getFieldValue();
    RoutingOperator operator = condition.getOperator();

    if (fieldValue == null) {
      return operator == RoutingOperator.IS_NULL;
    } else if (operator == RoutingOperator.IS_NOT_NULL) {
      return true;
    }

    try {
      switch (operator) {
        case EQUALS:
          return compareEquals(fieldValue, expectedValue);
        case NOT_EQUALS:
          return !compareEquals(fieldValue, expectedValue);
        case GREATER_THAN:
          return compareNumeric(fieldValue, expectedValue) > 0;
        case LESS_THAN:
          return compareNumeric(fieldValue, expectedValue) < 0;
        case GREATER_THAN_OR_EQUALS:
          return compareNumeric(fieldValue, expectedValue) >= 0;
        case LESS_THAN_OR_EQUALS:
          return compareNumeric(fieldValue, expectedValue) <= 0;
        case CONTAINS:
          return fieldValue.toString().contains(expectedValue);
        case IN:
          return compareIn(fieldValue, expectedValue);
        case NOT_IN:
          return !compareIn(fieldValue, expectedValue);
        case REGEX:
          return compareRegex(fieldValue, expectedValue);
        default:
          log.warn("Unsupported routing operator: {}", operator);
          return false;
      }
    } catch (Exception e) {
      log.error(
          "Error evaluating condition {}: {} with field value {}: {}",
          condition.getFieldName(),
          condition.getOperator(),
          fieldValue,
          e.getMessage());
      return false;
    }
  }

  /**
   * Get field value from routing request
   *
   * @param fieldName Field name
   * @param request Routing request
   * @return Field value
   */
  private Object getFieldValue(String fieldName, RoutingRequest request) {
    switch (fieldName) {
      case "paymentId":
        return request.getPaymentId();
      case "amount":
        return request.getAmount();
      case "currency":
        return request.getCurrency();
      case "paymentType":
        return request.getPaymentType();
      case "tenantId":
        return request.getTenantId();
      case "businessUnitId":
        return request.getBusinessUnitId();
      case "sourceAccount":
        return request.getSourceAccount();
      case "destinationAccount":
        return request.getDestinationAccount();
      case "priority":
        return request.getPriority();
      case "createdAt":
        return request.getCreatedAt();
      default:
        if (request.getMetadata() != null && request.getMetadata().containsKey(fieldName)) {
          return request.getMetadata().get(fieldName);
        }
        log.warn("Field '{}' not found in RoutingRequest or metadata.", fieldName);
        return null;
    }
  }

  /**
   * Compare values for equality
   *
   * @param fieldValue Field value
   * @param expectedValue Expected value
   * @return True if equal
   */
  private boolean compareEquals(Object fieldValue, String expectedValue) {
    return fieldValue.toString().equalsIgnoreCase(expectedValue);
  }

  /**
   * Compare numeric values
   *
   * @param fieldValue Field value
   * @param expectedValue Expected value
   * @return Comparison result (-1, 0, 1)
   */
  private int compareNumeric(Object fieldValue, String expectedValue) {
    BigDecimal fieldNum;
    BigDecimal expectedNum;
    try {
      fieldNum = new BigDecimal(fieldValue.toString());
      expectedNum = new BigDecimal(expectedValue);
    } catch (NumberFormatException e) {
      log.error(
          "Cannot compare non-numeric values as numbers: fieldValue='{}', expectedValue='{}'",
          fieldValue,
          expectedValue);
      throw e;
    }
    return fieldNum.compareTo(expectedNum);
  }

  /**
   * Compare values for list membership
   *
   * @param fieldValue Field value
   * @param expectedValue Expected value (comma-separated list)
   * @return True if field value is in the list
   */
  private boolean compareIn(Object fieldValue, String expectedValue) {
    List<String> expectedValues =
        Arrays.stream(expectedValue.split(",")).map(String::trim).collect(Collectors.toList());
    return expectedValues.stream().anyMatch(val -> val.equalsIgnoreCase(fieldValue.toString()));
  }

  /**
   * Compare values using regex pattern
   *
   * @param fieldValue Field value
   * @param expectedValue Regex pattern
   * @return True if pattern matches
   */
  private boolean compareRegex(Object fieldValue, String expectedValue) {
    try {
      return Pattern.compile(expectedValue).matcher(fieldValue.toString()).matches();
    } catch (PatternSyntaxException e) {
      log.error("Invalid regex pattern: '{}'", expectedValue, e);
      throw e;
    }
  }
}
