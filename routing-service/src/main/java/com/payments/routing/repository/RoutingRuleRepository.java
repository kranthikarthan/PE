package com.payments.routing.repository;

import com.payments.routing.domain.RoutingRule;
import com.payments.routing.domain.RoutingRuleStatus;
import com.payments.routing.domain.RoutingRuleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Routing Rule Repository
 * 
 * Repository for routing rule operations:
 * - Find rules by tenant and business unit
 * - Find active rules
 * - Find rules by type and status
 * - Custom queries for rule evaluation
 */
@Repository
public interface RoutingRuleRepository extends JpaRepository<RoutingRule, Long> {

    /**
     * Find active rules for tenant and business unit
     * 
     * @param tenantId Tenant ID
     * @param businessUnitId Business unit ID
     * @param now Current timestamp
     * @return List of active routing rules
     */
    @Query("SELECT r FROM RoutingRule r WHERE r.tenantId = :tenantId " +
           "AND (r.businessUnitId IS NULL OR r.businessUnitId = :businessUnitId) " +
           "AND r.isActive = true " +
           "AND r.ruleStatus = 'ACTIVE' " +
           "AND (r.effectiveFrom IS NULL OR r.effectiveFrom <= :now) " +
           "AND (r.effectiveTo IS NULL OR r.effectiveTo > :now) " +
           "ORDER BY r.priority ASC")
    List<RoutingRule> findActiveRulesForTenantAndBusinessUnit(
            @Param("tenantId") String tenantId,
            @Param("businessUnitId") String businessUnitId,
            @Param("now") Instant now);

    /**
     * Find rules by tenant
     * 
     * @param tenantId Tenant ID
     * @return List of routing rules
     */
    List<RoutingRule> findByTenantIdOrderByPriorityAsc(String tenantId);

    /**
     * Find rules by tenant and business unit
     * 
     * @param tenantId Tenant ID
     * @param businessUnitId Business unit ID
     * @return List of routing rules
     */
    List<RoutingRule> findByTenantIdAndBusinessUnitIdOrderByPriorityAsc(
            String tenantId, String businessUnitId);

    /**
     * Find rules by type and status
     * 
     * @param ruleType Rule type
     * @param ruleStatus Rule status
     * @return List of routing rules
     */
    List<RoutingRule> findByRuleTypeAndRuleStatusOrderByPriorityAsc(
            RoutingRuleType ruleType, RoutingRuleStatus ruleStatus);

    /**
     * Find rules by tenant and type
     * 
     * @param tenantId Tenant ID
     * @param ruleType Rule type
     * @return List of routing rules
     */
    List<RoutingRule> findByTenantIdAndRuleTypeOrderByPriorityAsc(
            String tenantId, RoutingRuleType ruleType);

    /**
     * Find rule by name and tenant
     * 
     * @param ruleName Rule name
     * @param tenantId Tenant ID
     * @return Optional routing rule
     */
    Optional<RoutingRule> findByRuleNameAndTenantId(String ruleName, String tenantId);

    /**
     * Find rules by status
     * 
     * @param ruleStatus Rule status
     * @return List of routing rules
     */
    List<RoutingRule> findByRuleStatusOrderByPriorityAsc(RoutingRuleStatus ruleStatus);

    /**
     * Find rules expiring before given date
     * 
     * @param beforeDate Date before which rules expire
     * @return List of expiring rules
     */
    @Query("SELECT r FROM RoutingRule r WHERE r.effectiveTo IS NOT NULL AND r.effectiveTo <= :beforeDate")
    List<RoutingRule> findRulesExpiringBefore(@Param("beforeDate") Instant beforeDate);

    /**
     * Find rules becoming effective after given date
     * 
     * @param afterDate Date after which rules become effective
     * @return List of future rules
     */
    @Query("SELECT r FROM RoutingRule r WHERE r.effectiveFrom IS NOT NULL AND r.effectiveFrom > :afterDate")
    List<RoutingRule> findRulesBecomingEffectiveAfter(@Param("afterDate") Instant afterDate);

    /**
     * Count active rules for tenant
     * 
     * @param tenantId Tenant ID
     * @param now Current timestamp
     * @return Count of active rules
     */
    @Query("SELECT COUNT(r) FROM RoutingRule r WHERE r.tenantId = :tenantId " +
           "AND r.isActive = true " +
           "AND r.ruleStatus = 'ACTIVE' " +
           "AND (r.effectiveFrom IS NULL OR r.effectiveFrom <= :now) " +
           "AND (r.effectiveTo IS NULL OR r.effectiveTo > :now)")
    long countActiveRulesForTenant(@Param("tenantId") String tenantId, @Param("now") Instant now);

    /**
     * Find rules with conditions
     * 
     * @param tenantId Tenant ID
     * @param businessUnitId Business unit ID
     * @param now Current timestamp
     * @return List of rules with conditions
     */
    @Query("SELECT DISTINCT r FROM RoutingRule r " +
           "LEFT JOIN FETCH r.conditions c " +
           "LEFT JOIN FETCH r.actions a " +
           "WHERE r.tenantId = :tenantId " +
           "AND (r.businessUnitId IS NULL OR r.businessUnitId = :businessUnitId) " +
           "AND r.isActive = true " +
           "AND r.ruleStatus = 'ACTIVE' " +
           "AND (r.effectiveFrom IS NULL OR r.effectiveFrom <= :now) " +
           "AND (r.effectiveTo IS NULL OR r.effectiveTo > :now) " +
           "ORDER BY r.priority ASC")
    List<RoutingRule> findActiveRulesWithConditionsAndActions(
            @Param("tenantId") String tenantId,
            @Param("businessUnitId") String businessUnitId,
            @Param("now") Instant now);

    /**
     * Count rules by status
     * 
     * @param status Rule status
     * @return Count of rules with the given status
     */
    long countByRuleStatus(RoutingRuleStatus status);
}
