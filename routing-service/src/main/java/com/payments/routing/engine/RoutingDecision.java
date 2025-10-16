package com.payments.routing.engine;

import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import lombok.Builder;
import lombok.Data;

/**
 * Routing Decision DTO
 *
 * <p>Output of routing decision engine: - Chosen clearing system - Priority level - Decision reason
 * - Additional metadata - Notifications
 *
 * <p>Performance: Immutable DTO with builder pattern Resilience: Fallback decision support
 */
@Data
@Builder
public class RoutingDecision {

  private String paymentId;
  private String ruleId;
  private String ruleName;
  private String clearingSystem;
  private String priority;
  private String decisionReason;

  @Builder.Default private boolean rejected = false;

  @Builder.Default private boolean held = false;

  @Builder.Default private boolean fallback = false;

  @Builder.Default private Map<String, String> metadata = new HashMap<>();

  @Builder.Default private Set<String> notifications = new HashSet<>();

  @Builder.Default private Instant decisionTimestamp = Instant.now();

  /**
   * Add metadata entry
   *
   * @param key Metadata key
   * @param value Metadata value
   */
  public void addMetadata(String key, String value) {
    if (this.metadata == null) {
      this.metadata = new HashMap<>();
    }
    this.metadata.put(key, value);
  }

  /**
   * Add notification
   *
   * @param notification Notification message
   */
  public void addNotification(String notification) {
    if (this.notifications == null) {
      this.notifications = new HashSet<>();
    }
    this.notifications.add(notification);
  }

  /**
   * Create fallback decision
   *
   * @param reason Fallback reason
   * @return Fallback routing decision
   */
  public static RoutingDecision fallback(String reason) {
    return RoutingDecision.builder()
        .clearingSystem("DEFAULT_FALLBACK_SYSTEM")
        .priority("NORMAL")
        .decisionReason("Fallback decision: " + reason)
        .fallback(true)
        .build();
  }
}
