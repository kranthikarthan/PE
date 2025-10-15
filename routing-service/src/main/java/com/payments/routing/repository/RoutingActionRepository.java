package com.payments.routing.repository;

import com.payments.routing.domain.RoutingAction;
import com.payments.routing.domain.RoutingActionType;
import com.payments.routing.domain.RoutingRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Routing Action Repository
 * 
 * Repository for routing action operations:
 * - Find actions by rule
 * - Find actions by type
 * - Custom queries for action evaluation
 */
@Repository
public interface RoutingActionRepository extends JpaRepository<RoutingAction, Long> {

    /**
     * Find actions by routing rule
     * 
     * @param routingRule Routing rule
     * @return List of actions ordered by action order
     */
    List<RoutingAction> findByRoutingRuleOrderByActionOrderAsc(RoutingRule routingRule);

    /**
     * Find actions by rule ID
     * 
     * @param ruleId Rule ID
     * @return List of actions ordered by action order
     */
    @Query("SELECT a FROM RoutingAction a WHERE a.routingRule.id = :ruleId ORDER BY a.actionOrder ASC")
    List<RoutingAction> findByRuleIdOrderByActionOrderAsc(@Param("ruleId") Long ruleId);

    /**
     * Find actions by type
     * 
     * @param actionType Action type
     * @return List of actions
     */
    List<RoutingAction> findByActionTypeOrderByActionOrderAsc(RoutingActionType actionType);

    /**
     * Find primary actions by rule
     * 
     * @param ruleId Rule ID
     * @return List of primary actions
     */
    @Query("SELECT a FROM RoutingAction a WHERE a.routingRule.id = :ruleId AND a.isPrimary = true ORDER BY a.actionOrder ASC")
    List<RoutingAction> findPrimaryActionsByRuleId(@Param("ruleId") Long ruleId);

    /**
     * Find actions by clearing system
     * 
     * @param clearingSystem Clearing system
     * @return List of actions
     */
    List<RoutingAction> findByClearingSystemOrderByActionOrderAsc(String clearingSystem);

    /**
     * Find actions by rule and type
     * 
     * @param ruleId Rule ID
     * @param actionType Action type
     * @return List of actions
     */
    @Query("SELECT a FROM RoutingAction a WHERE a.routingRule.id = :ruleId AND a.actionType = :actionType ORDER BY a.actionOrder ASC")
    List<RoutingAction> findByRuleIdAndActionType(@Param("ruleId") Long ruleId, @Param("actionType") RoutingActionType actionType);

    /**
     * Delete actions by rule ID
     * 
     * @param ruleId Rule ID
     */
    @Query("DELETE FROM RoutingAction a WHERE a.routingRule.id = :ruleId")
    void deleteByRuleId(@Param("ruleId") Long ruleId);

    /**
     * Count actions by rule
     * 
     * @param ruleId Rule ID
     * @return Count of actions
     */
    @Query("SELECT COUNT(a) FROM RoutingAction a WHERE a.routingRule.id = :ruleId")
    long countByRuleId(@Param("ruleId") Long ruleId);

    /**
     * Find actions by rule ordered by priority
     * 
     * @param ruleId Rule ID
     * @return List of actions ordered by routing priority
     */
    @Query("SELECT a FROM RoutingAction a WHERE a.routingRule.id = :ruleId ORDER BY a.routingPriority ASC, a.actionOrder ASC")
    List<RoutingAction> findByRuleIdOrderByRoutingPriorityAsc(@Param("ruleId") Long ruleId);
}
