package com.payments.routing.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

/**
 * Routing Rule Domain Model
 * 
 * Represents a routing rule for payment routing decisions:
 * - Rule conditions and criteria
 * - Routing decisions and priorities
 * - Rule metadata and lifecycle
 */
@Entity
@Table(name = "routing_rules")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoutingRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "rule_name", nullable = false, length = 100)
    private String ruleName;

    @Column(name = "rule_description", length = 500)
    private String ruleDescription;

    @Column(name = "tenant_id", nullable = false, length = 50)
    private String tenantId;

    @Column(name = "business_unit_id", length = 50)
    private String businessUnitId;

    @Enumerated(EnumType.STRING)
    @Column(name = "rule_type", nullable = false)
    private RoutingRuleType ruleType;

    @Enumerated(EnumType.STRING)
    @Column(name = "rule_status", nullable = false)
    private RoutingRuleStatus ruleStatus;

    @Column(name = "priority", nullable = false)
    private Integer priority;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Column(name = "effective_from")
    private Instant effectiveFrom;

    @Column(name = "effective_to")
    private Instant effectiveTo;

    @OneToMany(mappedBy = "routingRule", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<RoutingCondition> conditions;

    @OneToMany(mappedBy = "routingRule", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<RoutingAction> actions;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    @Version
    private Long version;

    /**
     * Check if rule is currently effective
     * 
     * @return True if rule is effective
     */
    public boolean isEffective() {
        Instant now = Instant.now();
        return isActive && 
               (effectiveFrom == null || !now.isBefore(effectiveFrom)) &&
               (effectiveTo == null || !now.isAfter(effectiveTo));
    }

    /**
     * Check if rule applies to tenant and business unit
     * 
     * @param tenantId Tenant ID
     * @param businessUnitId Business unit ID
     * @return True if rule applies
     */
    public boolean appliesTo(String tenantId, String businessUnitId) {
        if (!this.tenantId.equals(tenantId)) {
            return false;
        }
        
        if (this.businessUnitId != null && !this.businessUnitId.equals(businessUnitId)) {
            return false;
        }
        
        return true;
    }

    /**
     * Get rule priority for sorting
     * 
     * @return Rule priority
     */
    public Integer getPriority() {
        return priority != null ? priority : Integer.MAX_VALUE;
    }
}
