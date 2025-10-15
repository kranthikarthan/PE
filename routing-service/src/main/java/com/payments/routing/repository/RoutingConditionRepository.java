package com.payments.routing.repository;

import com.payments.routing.domain.RoutingCondition;
import com.payments.routing.domain.RoutingRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Routing Condition Repository
 * 
 * Repository for routing condition operations:
 * - Find conditions by rule
 * - Find conditions by field name
 * - Custom queries for condition evaluation
 */
@Repository
public interface RoutingConditionRepository extends JpaRepository<RoutingCondition, Long> {

    /**
     * Find conditions by routing rule
     * 
     * @param routingRule Routing rule
     * @return List of conditions ordered by condition order
     */
    List<RoutingCondition> findByRoutingRuleOrderByConditionOrderAsc(RoutingRule routingRule);

    /**
     * Find conditions by rule ID
     * 
     * @param ruleId Rule ID
     * @return List of conditions ordered by condition order
     */
    @Query("SELECT c FROM RoutingCondition c WHERE c.routingRule.id = :ruleId ORDER BY c.conditionOrder ASC")
    List<RoutingCondition> findByRuleIdOrderByConditionOrderAsc(@Param("ruleId") Long ruleId);

    /**
     * Find conditions by field name
     * 
     * @param fieldName Field name
     * @return List of conditions
     */
    List<RoutingCondition> findByFieldNameOrderByConditionOrderAsc(String fieldName);

    /**
     * Find conditions by field name and rule
     * 
     * @param fieldName Field name
     * @param routingRule Routing rule
     * @return List of conditions
     */
    List<RoutingCondition> findByFieldNameAndRoutingRuleOrderByConditionOrderAsc(
            String fieldName, RoutingRule routingRule);

    /**
     * Delete conditions by rule ID
     * 
     * @param ruleId Rule ID
     */
    @Query("DELETE FROM RoutingCondition c WHERE c.routingRule.id = :ruleId")
    void deleteByRuleId(@Param("ruleId") Long ruleId);

    /**
     * Count conditions by rule
     * 
     * @param ruleId Rule ID
     * @return Count of conditions
     */
    @Query("SELECT COUNT(c) FROM RoutingCondition c WHERE c.routingRule.id = :ruleId")
    long countByRuleId(@Param("ruleId") Long ruleId);
}
