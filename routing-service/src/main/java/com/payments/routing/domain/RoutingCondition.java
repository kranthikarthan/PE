package com.payments.routing.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Routing Condition Domain Model
 *
 * <p>Represents a condition within a routing rule: - Condition criteria and operators - Field
 * references and values - Logical operators
 */
@Entity
@Table(name = "routing_conditions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoutingCondition {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "routing_rule_id", nullable = false)
  private RoutingRule routingRule;

  @Column(name = "condition_order", nullable = false)
  private Integer conditionOrder;

  @Column(name = "field_name", nullable = false, length = 100)
  private String fieldName;

  @Enumerated(EnumType.STRING)
  @Column(name = "operator", nullable = false)
  private RoutingOperator operator;

  @Column(name = "field_value", length = 500)
  private String fieldValue;

  @Column(name = "field_value_type", length = 50)
  private String fieldValueType;

  @Enumerated(EnumType.STRING)
  @Column(name = "logical_operator")
  private LogicalOperator logicalOperator;

  @Column(name = "is_negated")
  private Boolean isNegated;

  @Column(name = "description", length = 200)
  private String description;

  /**
   * Check if condition is negated
   *
   * @return True if condition is negated
   */
  public boolean isNegated() {
    return isNegated != null && isNegated;
  }

  /**
   * Get logical operator for combining conditions
   *
   * @return Logical operator
   */
  public LogicalOperator getLogicalOperator() {
    return logicalOperator != null ? logicalOperator : LogicalOperator.AND;
  }
}
