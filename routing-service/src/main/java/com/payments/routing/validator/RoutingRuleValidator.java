package com.payments.routing.validator;

import com.payments.routing.domain.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Routing Rule Validator
 *
 * <p>Validates routing rules for correctness: - Rule structure validation - Condition validation -
 * Action validation - Business rule validation
 */
@Slf4j
@Component
public class RoutingRuleValidator {

  /**
   * Validate a routing rule
   *
   * @param rule Routing rule
   * @return Validation result
   */
  public ValidationResult validate(RoutingRule rule) {
    List<String> errors = new ArrayList<>();
    List<String> warnings = new ArrayList<>();

    // Validate rule basic properties
    validateRuleProperties(rule, errors, warnings);

    // Validate conditions
    if (rule.getConditions() != null) {
      validateConditions(rule.getConditions(), errors, warnings);
    }

    // Validate actions
    if (rule.getActions() != null) {
      validateActions(rule.getActions(), errors, warnings);
    }

    // Validate business rules
    validateBusinessRules(rule, errors, warnings);

    return ValidationResult.builder()
        .isValid(errors.isEmpty())
        .errors(errors)
        .warnings(warnings)
        .build();
  }

  /**
   * Validate rule properties
   *
   * @param rule Routing rule
   * @param errors Error list
   * @param warnings Warning list
   */
  private void validateRuleProperties(
      RoutingRule rule, List<String> errors, List<String> warnings) {
    // Validate rule name
    if (rule.getRuleName() == null || rule.getRuleName().trim().isEmpty()) {
      errors.add("Rule name is required");
    } else if (rule.getRuleName().length() > 100) {
      errors.add("Rule name must be 100 characters or less");
    }

    // Validate tenant ID
    if (rule.getTenantId() == null || rule.getTenantId().trim().isEmpty()) {
      errors.add("Tenant ID is required");
    }

    // Validate rule type
    if (rule.getRuleType() == null) {
      errors.add("Rule type is required");
    }

    // Validate rule status
    if (rule.getRuleStatus() == null) {
      errors.add("Rule status is required");
    }

    // Validate priority
    if (rule.getPriority() == null) {
      errors.add("Priority is required");
    } else if (rule.getPriority() < 1) {
      errors.add("Priority must be greater than 0");
    }

    // Validate effective dates
    if (rule.getEffectiveFrom() != null && rule.getEffectiveTo() != null) {
      if (rule.getEffectiveFrom().isAfter(rule.getEffectiveTo())) {
        errors.add("Effective from date must be before effective to date");
      }
    }

    // Validate business unit ID
    if (rule.getBusinessUnitId() != null && rule.getBusinessUnitId().trim().isEmpty()) {
      warnings.add("Business unit ID is empty, rule will apply to all business units");
    }
  }

  /**
   * Validate rule conditions
   *
   * @param conditions Rule conditions
   * @param errors Error list
   * @param warnings Warning list
   */
  private void validateConditions(
      List<RoutingCondition> conditions, List<String> errors, List<String> warnings) {
    if (conditions.isEmpty()) {
      warnings.add("Rule has no conditions, it will always match");
      return;
    }

    for (int i = 0; i < conditions.size(); i++) {
      RoutingCondition condition = conditions.get(i);
      validateCondition(condition, i, errors, warnings);
    }

    // Validate condition order
    validateConditionOrder(conditions, errors, warnings);
  }

  /**
   * Validate a single condition
   *
   * @param condition Routing condition
   * @param index Condition index
   * @param errors Error list
   * @param warnings Warning list
   */
  private void validateCondition(
      RoutingCondition condition, int index, List<String> errors, List<String> warnings) {
    // Validate field name
    if (condition.getFieldName() == null || condition.getFieldName().trim().isEmpty()) {
      errors.add("Condition " + index + ": Field name is required");
    }

    // Validate operator
    if (condition.getOperator() == null) {
      errors.add("Condition " + index + ": Operator is required");
    }

    // Validate field value
    if (condition.getFieldValue() == null || condition.getFieldValue().trim().isEmpty()) {
      errors.add("Condition " + index + ": Field value is required");
    }

    // Validate field value type
    if (condition.getFieldValueType() != null) {
      validateFieldValueType(condition, index, errors, warnings);
    }

    // Validate logical operator
    if (condition.getLogicalOperator() == null) {
      warnings.add("Condition " + index + ": Logical operator not specified, defaulting to AND");
    }
  }

  /**
   * Validate field value type
   *
   * @param condition Routing condition
   * @param index Condition index
   * @param errors Error list
   * @param warnings Warning list
   */
  private void validateFieldValueType(
      RoutingCondition condition, int index, List<String> errors, List<String> warnings) {
    String valueType = condition.getFieldValueType();
    String fieldValue = condition.getFieldValue();

    try {
      switch (valueType.toUpperCase()) {
        case "DECIMAL", "NUMERIC" -> {
          Double.parseDouble(fieldValue);
        }
        case "INTEGER" -> {
          Integer.parseInt(fieldValue);
        }
        case "BOOLEAN" -> {
          if (!"true".equalsIgnoreCase(fieldValue) && !"false".equalsIgnoreCase(fieldValue)) {
            errors.add("Condition " + index + ": Invalid boolean value: " + fieldValue);
          }
        }
        case "DATE", "DATETIME" -> {
          Instant.parse(fieldValue);
        }
        case "STRING" -> {
          // String is always valid
        }
        default -> {
          warnings.add("Condition " + index + ": Unknown field value type: " + valueType);
        }
      }
    } catch (Exception e) {
      errors.add("Condition " + index + ": Invalid " + valueType + " value: " + fieldValue);
    }
  }

  /**
   * Validate condition order
   *
   * @param conditions Rule conditions
   * @param errors Error list
   * @param warnings Warning list
   */
  private void validateConditionOrder(
      List<RoutingCondition> conditions, List<String> errors, List<String> warnings) {
    for (int i = 0; i < conditions.size(); i++) {
      RoutingCondition condition = conditions.get(i);
      if (condition.getConditionOrder() == null) {
        errors.add("Condition " + i + ": Condition order is required");
      } else if (condition.getConditionOrder() != i + 1) {
        warnings.add(
            "Condition "
                + i
                + ": Condition order should be "
                + (i + 1)
                + " but is "
                + condition.getConditionOrder());
      }
    }
  }

  /**
   * Validate rule actions
   *
   * @param actions Rule actions
   * @param errors Error list
   * @param warnings Warning list
   */
  private void validateActions(
      List<RoutingAction> actions, List<String> errors, List<String> warnings) {
    if (actions.isEmpty()) {
      errors.add("Rule must have at least one action");
      return;
    }

    for (int i = 0; i < actions.size(); i++) {
      RoutingAction action = actions.get(i);
      validateAction(action, i, errors, warnings);
    }

    // Validate action order
    validateActionOrder(actions, errors, warnings);

    // Validate primary action
    validatePrimaryAction(actions, errors, warnings);
  }

  /**
   * Validate a single action
   *
   * @param action Routing action
   * @param index Action index
   * @param errors Error list
   * @param warnings Warning list
   */
  private void validateAction(
      RoutingAction action, int index, List<String> errors, List<String> warnings) {
    // Validate action type
    if (action.getActionType() == null) {
      errors.add("Action " + index + ": Action type is required");
    }

    // Validate clearing system for route actions
    if (action.getActionType() == RoutingActionType.ROUTE_TO_CLEARING_SYSTEM) {
      if (action.getClearingSystem() == null || action.getClearingSystem().trim().isEmpty()) {
        errors.add("Action " + index + ": Clearing system is required for route actions");
      }
    }

    // Validate routing priority
    if (action.getRoutingPriority() != null && action.getRoutingPriority() < 1) {
      errors.add("Action " + index + ": Routing priority must be greater than 0");
    }

    // Validate action parameters
    if (action.getActionParameters() != null && action.getActionParameters().length() > 1000) {
      errors.add("Action " + index + ": Action parameters must be 1000 characters or less");
    }
  }

  /**
   * Validate action order
   *
   * @param actions Rule actions
   * @param errors Error list
   * @param warnings Warning list
   */
  private void validateActionOrder(
      List<RoutingAction> actions, List<String> errors, List<String> warnings) {
    for (int i = 0; i < actions.size(); i++) {
      RoutingAction action = actions.get(i);
      if (action.getActionOrder() == null) {
        errors.add("Action " + i + ": Action order is required");
      } else if (action.getActionOrder() != i + 1) {
        warnings.add(
            "Action "
                + i
                + ": Action order should be "
                + (i + 1)
                + " but is "
                + action.getActionOrder());
      }
    }
  }

  /**
   * Validate primary action
   *
   * @param actions Rule actions
   * @param errors Error list
   * @param warnings Warning list
   */
  private void validatePrimaryAction(
      List<RoutingAction> actions, List<String> errors, List<String> warnings) {
    long primaryCount = actions.stream().mapToLong(action -> action.isPrimary() ? 1 : 0).sum();

    if (primaryCount == 0) {
      warnings.add("No primary action specified, first action will be used");
    } else if (primaryCount > 1) {
      errors.add("Multiple primary actions specified, only one is allowed");
    }
  }

  /**
   * Validate business rules
   *
   * @param rule Routing rule
   * @param errors Error list
   * @param warnings Warning list
   */
  private void validateBusinessRules(RoutingRule rule, List<String> errors, List<String> warnings) {
    // Validate rule is not expired
    if (rule.getEffectiveTo() != null && rule.getEffectiveTo().isBefore(Instant.now())) {
      warnings.add("Rule is already expired");
    }

    // Validate rule is not in the future
    if (rule.getEffectiveFrom() != null && rule.getEffectiveFrom().isAfter(Instant.now())) {
      warnings.add("Rule is not yet effective");
    }

    // Validate rule has at least one condition or action
    if ((rule.getConditions() == null || rule.getConditions().isEmpty())
        && (rule.getActions() == null || rule.getActions().isEmpty())) {
      errors.add("Rule must have at least one condition or action");
    }
  }

  /** Validation Result */
  @lombok.Builder
  @lombok.Data
  @lombok.AllArgsConstructor
  @lombok.NoArgsConstructor
  public static class ValidationResult {
    private boolean isValid;
    private List<String> errors;
    private List<String> warnings;
  }
}
