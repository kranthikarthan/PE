package com.payments.paymentinitiation.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuditEventAsyncPublisher {

  private final KafkaTemplate<String, String> kafkaTemplate;
  private final ObjectMapper objectMapper = new ObjectMapper();

  @Value("${app.kafka.audit-topic:payment-audit-logs}")
  private String auditTopic;

  @Async
  @CircuitBreaker(name = "auditPublisher", fallbackMethod = "fallback")
  public CompletableFuture<Void> publish(Object payload, String tenantKey) {
    try {
      String json = objectMapper.writeValueAsString(payload);
      return kafkaTemplate.send(auditTopic, tenantKey, json).thenApply(r -> null);
    } catch (Exception e) {
      throw new RuntimeException("Failed to serialize audit payload", e);
    }
  }

  private CompletableFuture<Void> fallback(Object payload, String tenantKey, Throwable t) {
    log.warn("Audit publish failed; proceeding without blocking. reason={}", t.getMessage());
    return CompletableFuture.completedFuture(null);
  }
}
