package com.payments.routing.engine;

import com.payments.routing.domain.RoutingAction;
import com.payments.routing.domain.RoutingActionType;
import com.payments.routing.domain.RoutingRule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Action Executor
 * 
 * Executes routing actions based on matched rules:
 * - Route to clearing system
 * - Set priority
 * - Add metadata
 * - Reject payment
 * - Hold payment
 * - Send notifications
 * 
 * Performance: Efficient action processing
 * Resilience: Graceful error handling
 */
@Slf4j
@Component
public class ActionExecutor {

    @Value("${routing.fallback-clearing-system:DEFAULT_CLEARING}")
    private String fallbackClearingSystem;

    /**
     * Execute routing actions for matched rule
     * 
     * @param rule Routing rule
     * @param request Routing request
     * @return Routing decision
     */
    public RoutingDecision execute(RoutingRule rule, RoutingRequest request) {
        RoutingDecision.RoutingDecisionBuilder decisionBuilder = RoutingDecision.builder()
                .paymentId(request.getPaymentId())
                .ruleId(rule.getId().toString())
                .ruleName(rule.getRuleName())
                .decisionReason("Matched rule: " + rule.getRuleName());

        // Group actions by type for efficient processing
        Map<RoutingActionType, List<RoutingAction>> actionsByType = rule.getActions().stream()
                .collect(Collectors.groupingBy(RoutingAction::getActionType));

        // 1. Handle ROUTE_TO_CLEARING_SYSTEM action
        actionsByType.getOrDefault(RoutingActionType.ROUTE_TO_CLEARING_SYSTEM, List.of()).stream()
                .filter(RoutingAction::getIsPrimary)
                .findFirst()
                .ifPresent(action -> {
                    decisionBuilder.clearingSystem(action.getClearingSystem());
                    log.debug("Action: Route to clearing system '{}' for rule '{}'", 
                            action.getClearingSystem(), rule.getRuleName());
                });

        // If no primary clearing system action, use fallback
        if (decisionBuilder.build().getClearingSystem() == null) {
            decisionBuilder.clearingSystem(fallbackClearingSystem);
            decisionBuilder.fallback(true);
            log.warn("No primary ROUTE_TO_CLEARING_SYSTEM action found for rule '{}'. Using fallback: {}", 
                    rule.getRuleName(), fallbackClearingSystem);
        }

        // 2. Handle SET_PRIORITY action
        actionsByType.getOrDefault(RoutingActionType.SET_PRIORITY, List.of()).stream()
                .findFirst()
                .ifPresent(action -> {
                    decisionBuilder.priority(action.getActionParameters());
                    log.debug("Action: Set priority to '{}' for rule '{}'", 
                            action.getActionParameters(), rule.getRuleName());
                });

        // 3. Handle ADD_METADATA action
        actionsByType.getOrDefault(RoutingActionType.ADD_METADATA, List.of())
                .forEach(action -> {
                    if (action.getActionParameters() != null && action.getActionParameters().contains("=")) {
                        String[] parts = action.getActionParameters().split("=", 2);
                        if (parts.length == 2) {
                            // Add metadata using builder pattern
                    Map<String, String> metadata = new HashMap<>();
                    metadata.put(parts[0].trim(), parts[1].trim());
                    decisionBuilder.metadata(metadata);
                            log.debug("Action: Add metadata '{}:{}' for rule '{}'", 
                                    parts[0].trim(), parts[1].trim(), rule.getRuleName());
                        }
                    }
                });

        // 4. Handle REJECT_PAYMENT action
        if (actionsByType.containsKey(RoutingActionType.REJECT_PAYMENT)) {
            decisionBuilder.rejected(true);
            actionsByType.get(RoutingActionType.REJECT_PAYMENT).stream().findFirst()
                    .ifPresent(action -> decisionBuilder.decisionReason(
                            "Payment rejected by rule: " + rule.getRuleName() + ". " + action.getActionParameters()));
            log.debug("Action: Reject payment for rule '{}'", rule.getRuleName());
        }

        // 5. Handle HOLD_PAYMENT action
        if (actionsByType.containsKey(RoutingActionType.HOLD_PAYMENT)) {
            decisionBuilder.held(true);
            actionsByType.get(RoutingActionType.HOLD_PAYMENT).stream().findFirst()
                    .ifPresent(action -> decisionBuilder.decisionReason(
                            "Payment held by rule: " + rule.getRuleName() + ". " + action.getActionParameters()));
            log.debug("Action: Hold payment for rule '{}'", rule.getRuleName());
        }

        // 6. Handle NOTIFY action
        actionsByType.getOrDefault(RoutingActionType.NOTIFY, List.of())
                .forEach(action -> {
                    // Add notification using builder pattern
                    Set<String> notifications = new HashSet<>();
                    notifications.add(action.getActionParameters());
                    decisionBuilder.notifications(notifications);
                    log.debug("Action: Notify '{}' for rule '{}'", action.getActionParameters(), rule.getRuleName());
                });

        return decisionBuilder.build();
    }
}