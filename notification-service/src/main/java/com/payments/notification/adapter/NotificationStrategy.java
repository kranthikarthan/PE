package com.payments.notification.adapter;

import com.payments.notification.domain.model.NotificationEntity;

/**
 * Strategy interface for different notification delivery mechanisms.
 *
 * <p>Implementations:
 * - InternalNotificationStrategy: Direct delivery via Email, SMS, Push adapters
 * - IBMMQNotificationStrategy: Fire-and-forget via IBM MQ
 *
 * @author Payment Engine
 */
public interface NotificationStrategy {

  /**
   * Process and deliver a notification using the strategy's mechanism.
   *
   * @param notification the notification to deliver
   * @throws Exception if delivery fails
   */
  void processNotification(NotificationEntity notification) throws Exception;

  /**
   * Get strategy name for logging/monitoring.
   *
   * @return strategy name
   */
  String getStrategyName();

  /**
   * Check if strategy is available/healthy.
   *
   * @return true if ready for use
   */
  boolean isHealthy();
}
