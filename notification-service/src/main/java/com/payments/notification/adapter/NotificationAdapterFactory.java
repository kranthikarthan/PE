package com.payments.notification.adapter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Factory for creating appropriate notification adapters.
 *
 * <p>Supports two architectures:
 * 1. **Internal** (Default): Direct channel adapters (Email, SMS, Push)
 * 2. **IBM MQ** (Optional): Fire-and-forget via IBM MQ
 *
 * <p>Feature toggle: `notification.adapter.type=internal|ibm-mq`
 *
 * @author Payment Engine
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class NotificationAdapterFactory {

  @Value("${notification.adapter.type:internal}")
  private String adapterType;

  @Value("${notification.adapter.ibm-mq.enabled:false}")
  private boolean ibmMqEnabled;

  private final EmailAdapter emailAdapter;
  private final SMSAdapter smsAdapter;
  private final PushNotificationAdapter pushAdapter;
  // IBM MQ adapter will be optional/lazy-loaded

  /**
   * Get appropriate notification adapter based on configuration.
   *
   * @return NotificationStrategy (internal or IBM MQ)
   */
  public NotificationStrategy getNotificationStrategy() {
    // Determine which strategy to use
    if ("ibm-mq".equalsIgnoreCase(adapterType) && ibmMqEnabled) {
      log.info("Using IBM MQ notification strategy (fire-and-forget)");
      return new IBMMQNotificationStrategy();
    } else {
      log.info("Using internal notification strategy (direct channels)");
      return new InternalNotificationStrategy(emailAdapter, smsAdapter, pushAdapter);
    }
  }

  /**
   * Get current adapter type.
   *
   * @return "internal" or "ibm-mq"
   */
  public String getCurrentAdapterType() {
    return adapterType;
  }

  /**
   * Check if IBM MQ is available and enabled.
   *
   * @return true if IBM MQ is configured
   */
  public boolean isIbmMqAvailable() {
    return "ibm-mq".equalsIgnoreCase(adapterType) && ibmMqEnabled;
  }
}
