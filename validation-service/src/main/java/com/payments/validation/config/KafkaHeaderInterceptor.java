package com.payments.validation.config;

import com.payments.validation.service.CorrelationService;
import com.payments.validation.service.TenantContextService;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerInterceptor;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.stereotype.Component;

/**
 * Kafka Header Interceptor
 *
 * <p>Automatically adds correlation and tenant headers to Kafka messages: - Correlation ID
 * propagation - Tenant context propagation - Request tracing - Multi-tenant support
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaHeaderInterceptor implements ProducerInterceptor<String, Object> {

  private final CorrelationService correlationService;
  private final TenantContextService tenantContextService;

  @Override
  public ProducerRecord<String, Object> onSend(ProducerRecord<String, Object> record) {
    log.debug("Intercepting Kafka message for topic: {}", record.topic());

    // Add correlation ID if available
    String correlationId = correlationService.getCurrentCorrelationId();
    if (correlationId != null) {
      record.headers().add("X-Correlation-ID", correlationId.getBytes());
      log.debug("Added correlation ID header: {}", correlationId);
    }

    // Add tenant context if available
    String tenantId = tenantContextService.getCurrentTenantId();
    if (tenantId != null) {
      record.headers().add("X-Tenant-ID", tenantId.getBytes());
      log.debug("Added tenant ID header: {}", tenantId);
    }

    String businessUnitId = tenantContextService.getCurrentBusinessUnitId();
    if (businessUnitId != null) {
      record.headers().add("X-Business-Unit-ID", businessUnitId.getBytes());
      log.debug("Added business unit ID header: {}", businessUnitId);
    }

    // Add service metadata
    record.headers().add("X-Source", "validation-service".getBytes());
    record.headers().add("X-Service-Version", "1.0.0".getBytes());
    record
        .headers()
        .add("X-Processing-Time", String.valueOf(System.currentTimeMillis()).getBytes());

    return record;
  }

  @Override
  public void onAcknowledgement(RecordMetadata metadata, Exception exception) {
    if (exception != null) {
      log.error(
          "Failed to send message to topic: {}, partition: {}, offset: {}",
          metadata.topic(),
          metadata.partition(),
          metadata.offset(),
          exception);
    } else {
      log.debug(
          "Successfully sent message to topic: {}, partition: {}, offset: {}",
          metadata.topic(),
          metadata.partition(),
          metadata.offset());
    }
  }

  @Override
  public void close() {
    log.debug("Closing Kafka header interceptor");
  }

  @Override
  public void configure(Map<String, ?> configs) {
    log.debug("Configuring Kafka header interceptor with configs: {}", configs);
  }
}
