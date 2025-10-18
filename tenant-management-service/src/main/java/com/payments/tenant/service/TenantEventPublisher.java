package com.payments.tenant.service;

import com.payments.tenant.entity.TenantEntity;
import io.micrometer.core.annotation.Timed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

/**
 * Tenant Event Publisher - Publishes tenant lifecycle events to Kafka topics.
 *
 * <p>Events:
 * - TenantCreatedEvent: New tenant created (PENDING_APPROVAL)
 * - TenantActivatedEvent: Tenant approved and activated
 * - TenantSuspendedEvent: Tenant suspended (temporary deactivation)
 * - TenantDeactivatedEvent: Tenant deactivated (soft delete)
 *
 * <p>Topics:
 * - `tenant.created` - Consumed by audit, notification services
 * - `tenant.activated` - Triggers downstream provisioning
 * - `tenant.suspended` - Alerts compliance/operations
 * - `tenant.deactivated` - Cleanup tasks for related entities
 *
 * <p>Pattern: Enterprise Integration Pattern - Event Message
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TenantEventPublisher {

  private static final String TOPIC_TENANT_CREATED = "tenant.created";
  private static final String TOPIC_TENANT_ACTIVATED = "tenant.activated";
  private static final String TOPIC_TENANT_SUSPENDED = "tenant.suspended";
  private static final String TOPIC_TENANT_DEACTIVATED = "tenant.deactivated";

  private final KafkaTemplate<String, TenantEventMessage> kafkaTemplate;

  /**
   * Publish TenantCreatedEvent.
   *
   * <p>Fired when a new tenant is created (status = PENDING_APPROVAL).
   * Other services (audit, notification) listen for this event.
   *
   * @param tenant Created tenant
   * @param createdBy User who created
   */
  @Timed(value = "tenant.event.created", description = "Publish tenant created event")
  public void publishTenantCreatedEvent(TenantEntity tenant, String createdBy) {
    try {
      TenantEventMessage event =
          TenantEventMessage.builder()
              .tenantId(tenant.getTenantId())
              .tenantName(tenant.getTenantName())
              .tenantType(tenant.getTenantType().toString())
              .status(tenant.getStatus().toString())
              .eventType("TENANT_CREATED")
              .triggeredBy(createdBy)
              .timestamp(System.currentTimeMillis())
              .build();

      sendEvent(TOPIC_TENANT_CREATED, tenant.getTenantId(), event);
      log.info("Published TenantCreatedEvent for tenant: {}", tenant.getTenantId());
    } catch (Exception e) {
      log.error("Failed to publish TenantCreatedEvent for tenant: {}", tenant.getTenantId(), e);
      // Don't throw - tenant is already created, just log event publishing failure
    }
  }

  /**
   * Publish TenantActivatedEvent.
   *
   * <p>Fired when tenant is approved (status = ACTIVE).
   * Triggers downstream provisioning tasks.
   *
   * @param tenant Activated tenant
   * @param activatedBy User who activated
   */
  @Timed(value = "tenant.event.activated", description = "Publish tenant activated event")
  public void publishTenantActivatedEvent(TenantEntity tenant, String activatedBy) {
    try {
      TenantEventMessage event =
          TenantEventMessage.builder()
              .tenantId(tenant.getTenantId())
              .tenantName(tenant.getTenantName())
              .tenantType(tenant.getTenantType().toString())
              .status(tenant.getStatus().toString())
              .eventType("TENANT_ACTIVATED")
              .triggeredBy(activatedBy)
              .timestamp(System.currentTimeMillis())
              .build();

      sendEvent(TOPIC_TENANT_ACTIVATED, tenant.getTenantId(), event);
      log.info("Published TenantActivatedEvent for tenant: {}", tenant.getTenantId());
    } catch (Exception e) {
      log.error("Failed to publish TenantActivatedEvent for tenant: {}", tenant.getTenantId(), e);
    }
  }

  /**
   * Publish TenantSuspendedEvent.
   *
   * <p>Fired when tenant is suspended (temporary deactivation).
   * Alerts compliance and operations.
   *
   * @param tenant Suspended tenant
   * @param suspendedBy User who suspended
   */
  @Timed(value = "tenant.event.suspended", description = "Publish tenant suspended event")
  public void publishTenantSuspendedEvent(TenantEntity tenant, String suspendedBy) {
    try {
      TenantEventMessage event =
          TenantEventMessage.builder()
              .tenantId(tenant.getTenantId())
              .tenantName(tenant.getTenantName())
              .tenantType(tenant.getTenantType().toString())
              .status(tenant.getStatus().toString())
              .eventType("TENANT_SUSPENDED")
              .triggeredBy(suspendedBy)
              .timestamp(System.currentTimeMillis())
              .build();

      sendEvent(TOPIC_TENANT_SUSPENDED, tenant.getTenantId(), event);
      log.warn("Published TenantSuspendedEvent for tenant: {}", tenant.getTenantId());
    } catch (Exception e) {
      log.error("Failed to publish TenantSuspendedEvent for tenant: {}", tenant.getTenantId(), e);
    }
  }

  /**
   * Publish TenantDeactivatedEvent.
   *
   * <p>Fired when tenant is deactivated (soft delete).
   * Triggers cleanup tasks for related entities.
   *
   * @param tenant Deactivated tenant
   * @param deactivatedBy User who deactivated
   */
  @Timed(value = "tenant.event.deactivated", description = "Publish tenant deactivated event")
  public void publishTenantDeactivatedEvent(TenantEntity tenant, String deactivatedBy) {
    try {
      TenantEventMessage event =
          TenantEventMessage.builder()
              .tenantId(tenant.getTenantId())
              .tenantName(tenant.getTenantName())
              .tenantType(tenant.getTenantType().toString())
              .status(tenant.getStatus().toString())
              .eventType("TENANT_DEACTIVATED")
              .triggeredBy(deactivatedBy)
              .timestamp(System.currentTimeMillis())
              .build();

      sendEvent(TOPIC_TENANT_DEACTIVATED, tenant.getTenantId(), event);
      log.warn("Published TenantDeactivatedEvent for tenant: {}", tenant.getTenantId());
    } catch (Exception e) {
      log.error("Failed to publish TenantDeactivatedEvent for tenant: {}", tenant.getTenantId(), e);
    }
  }

  /**
   * Send event message to Kafka topic.
   *
   * <p>Adds correlation ID and tenant ID headers for traceability.
   *
   * @param topic Topic name
   * @param key Message key (tenantId)
   * @param event Event payload
   */
  private void sendEvent(String topic, String key, TenantEventMessage event) {
    Message<TenantEventMessage> message =
        MessageBuilder.withPayload(event)
            .setHeader(KafkaHeaders.TOPIC, topic)
            .setHeader("kafka_messageKey", key)
            .setHeader("X-Correlation-ID", event.getCorrelationId())
            .setHeader("X-Tenant-ID", event.getTenantId())
            .build();

    kafkaTemplate.send(message);
  }

  /**
   * Tenant Event Message DTO.
   *
   * <p>Follows Enterprise Integration Pattern: Event Message
   */
  @lombok.Data
  @lombok.Builder
  @lombok.NoArgsConstructor
  @lombok.AllArgsConstructor
  public static class TenantEventMessage {
    private String tenantId;
    private String tenantName;
    private String tenantType;
    private String status;
    private String eventType;
    private String triggeredBy;
    private long timestamp;

    @lombok.Builder.Default private String correlationId = java.util.UUID.randomUUID().toString();
  }
}
