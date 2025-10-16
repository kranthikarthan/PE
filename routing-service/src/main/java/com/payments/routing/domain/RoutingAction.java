package com.payments.routing.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Routing Action Domain Model
 *
 * <p>Represents an action to be taken when routing rule matches: - Action type and parameters -
 * Clearing system selection - Routing decisions
 */
@Entity
@Table(name = "routing_actions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoutingAction {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "routing_rule_id", nullable = false)
  private RoutingRule routingRule;

  @Column(name = "action_order", nullable = false)
  private Integer actionOrder;

  @Enumerated(EnumType.STRING)
  @Column(name = "action_type", nullable = false)
  private RoutingActionType actionType;

  @Column(name = "clearing_system", length = 50)
  private String clearingSystem;

  @Column(name = "routing_priority")
  private Integer routingPriority;

  @Column(name = "action_parameters", length = 1000)
  private String actionParameters;

  @Column(name = "is_primary")
  private Boolean isPrimary;

  @Column(name = "description", length = 200)
  private String description;

  /**
   * Check if action is primary
   *
   * @return True if action is primary
   */
  public boolean isPrimary() {
    return isPrimary != null && isPrimary;
  }

  /**
   * Get routing priority for sorting
   *
   * @return Routing priority
   */
  public Integer getRoutingPriority() {
    return routingPriority != null ? routingPriority : Integer.MAX_VALUE;
  }
}
