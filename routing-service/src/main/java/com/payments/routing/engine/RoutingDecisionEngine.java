package com.payments.routing.engine;

import com.payments.routing.domain.LogicalOperator;
import com.payments.routing.domain.RoutingAction;
import com.payments.routing.domain.RoutingCondition;
import com.payments.routing.domain.RoutingRule;
import com.payments.routing.domain.RoutingRuleStatus;
import com.payments.routing.repository.RoutingRuleRepository;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Routing Decision Engine
 *
 * <p>Core logic for evaluating routing rules: - Evaluate rules in priority order - Handle rule
 * conditions and actions - Provide fallback decisions - Performance optimization with concurrent
 * evaluation
 *
 * <p>Performance: Concurrent rule evaluation, DSA-optimized data structures Resilience: Timeout
 * handling, graceful degradation
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RoutingDecisionEngine {

  private final RoutingRuleRepository ruleRepository;
  private final ConditionEvaluator conditionEvaluator;
  private final ActionExecutor actionExecutor;

  @Value("${routing.rule-evaluation-timeout-ms:2000}")
  private long ruleEvaluationTimeoutMs;

  private final ExecutorService ruleEvaluationExecutor = Executors.newFixedThreadPool(10);

  /**
   * Evaluate routing rules for payment request
   *
   * @param request Routing request
   * @return Routing decision
   */
  public RoutingDecision evaluate(RoutingRequest request) {
    log.debug("Starting routing rule evaluation for payment: {}", request.getPaymentId());
    Instant now = Instant.now();

    // 1. Fetch active rules for tenant and business unit
    List<RoutingRule> activeRules =
        ruleRepository.findActiveRulesForTenantAndBusinessUnit(
            request.getTenantId(), request.getBusinessUnitId(), now);

    if (activeRules.isEmpty()) {
      log.warn(
          "No active routing rules found for tenant: {} and business unit: {}",
          request.getTenantId(),
          request.getBusinessUnitId());
      return RoutingDecision.fallback("No active rules found");
    }

    // 2. Evaluate rules concurrently
    Optional<RoutingRule> winningRule =
        activeRules.stream()
            .map(
                rule ->
                    CompletableFuture.supplyAsync(
                            () -> {
                              try {
                                return evaluateSingleRule(rule, request);
                              } catch (Exception e) {
                                log.error(
                                    "Error evaluating rule {}: {}",
                                    rule.getRuleName(),
                                    e.getMessage());
                                return null;
                              }
                            },
                            ruleEvaluationExecutor)
                        .orTimeout(ruleEvaluationTimeoutMs, TimeUnit.MILLISECONDS)
                        .exceptionally(
                            ex -> {
                              log.warn(
                                  "Rule evaluation for {} timed out or failed: {}",
                                  rule.getRuleName(),
                                  ex.getMessage());
                              return null;
                            }))
            .collect(Collectors.toList())
            .stream()
            .map(CompletableFuture::join)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .min(Comparator.comparingInt(RoutingRule::getPriority));

    if (winningRule.isPresent()) {
      RoutingRule rule = winningRule.get();
      log.info(
          "Winning rule found: {} (Priority: {}) for payment: {}",
          rule.getRuleName(),
          rule.getPriority(),
          request.getPaymentId());
      return actionExecutor.execute(rule, request);
    } else {
      log.info(
          "No matching routing rule found for payment: {}. Returning fallback decision.",
          request.getPaymentId());
      return RoutingDecision.fallback("No matching rule found");
    }
  }

  /**
   * Evaluate single routing rule
   *
   * @param rule Routing rule
   * @param request Routing request
   * @return Optional containing rule if it matches
   */
  private Optional<RoutingRule> evaluateSingleRule(RoutingRule rule, RoutingRequest request) {
    if (rule.getRuleStatus() != RoutingRuleStatus.ACTIVE) {
      return Optional.empty();
    }

    // Check effective dates
    Instant now = Instant.now();
    if ((rule.getEffectiveFrom() != null && now.isBefore(rule.getEffectiveFrom()))
        || (rule.getEffectiveTo() != null && now.isAfter(rule.getEffectiveTo()))) {
      return Optional.empty();
    }

    // Evaluate conditions
    boolean allConditionsMatch = true;
    if (rule.getConditions() != null && !rule.getConditions().isEmpty()) {
      for (RoutingCondition condition :
          rule.getConditions().stream()
              .sorted(Comparator.comparingInt(RoutingCondition::getConditionOrder))
              .collect(Collectors.toList())) {

        boolean conditionMatch = conditionEvaluator.evaluate(condition, request);

        if (condition.getIsNegated()) {
          conditionMatch = !conditionMatch;
        }

        if (condition.getLogicalOperator() == LogicalOperator.OR) {
          if (conditionMatch) {
            allConditionsMatch = true;
            break;
          }
        } else { // AND or no logical operator
          if (!conditionMatch) {
            allConditionsMatch = false;
            break;
          }
        }
      }
    }

    if (allConditionsMatch) {
      boolean hasPrimaryAction =
          rule.getActions() != null
              && rule.getActions().stream().anyMatch(RoutingAction::getIsPrimary);
      if (hasPrimaryAction) {
        log.debug("Rule '{}' matched for payment: {}", rule.getRuleName(), request.getPaymentId());
        return Optional.of(rule);
      } else {
        log.warn(
            "Rule '{}' matched conditions but has no primary action. Skipping.",
            rule.getRuleName());
      }
    }

    return Optional.empty();
  }
}
