package com.payments.notification.adapter;

import com.payments.notification.domain.model.NotificationEntity;
import com.payments.notification.domain.model.NotificationTemplateEntity;

/**
 * ChannelAdapter defines the contract for all notification channel implementations.
 *
 * <p>Strategy Pattern: Each channel (Email, SMS, Push) implements this interface independently.
 *
 * <p>Implementations:
 * - EmailAdapter: AWS SES
 * - SMSAdapter: Twilio
 * - PushNotificationAdapter: Firebase FCM
 * - Future: Slack, Teams, WhatsApp, etc.
 *
 * @author Payment Engine
 */
public interface ChannelAdapter {

  /**
   * Send notification via this channel.
   *
   * @param notification the notification entity
   * @param template channel-specific template
   * @param renderedContent rendered template content
   * @throws Exception if send fails
   */
  void send(
      NotificationEntity notification,
      NotificationTemplateEntity template,
      String renderedContent)
      throws Exception;

  /**
   * Get adapter status.
   *
   * @return status description
   */
  String getStatus();
}
