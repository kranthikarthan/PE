package com.payments.validation.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * Event Topics Configuration
 *
 * <p>Defines all Kafka topics for validation service: - Inbound topics (events to process) -
 * Outbound topics (validation results) - Topic naming conventions - Partition and replication
 * settings
 */
@Configuration
public class EventTopicsConfig {

  // Inbound Topics (Events to Validate)
  @Value("${validation.topics.inbound.payment-initiated:payment-initiated}")
  public String PAYMENT_INITIATED_TOPIC;

  @Value("${validation.topics.inbound.payment-updated:payment-updated}")
  public String PAYMENT_UPDATED_TOPIC;

  @Value("${validation.topics.inbound.account-changed:account-changed}")
  public String ACCOUNT_CHANGED_TOPIC;

  // Outbound Topics (Validation Results)
  @Value("${validation.topics.outbound.payment-validated:payment-validated}")
  public String PAYMENT_VALIDATED_TOPIC;

  @Value("${validation.topics.outbound.validation-failed:validation-failed}")
  public String VALIDATION_FAILED_TOPIC;

  @Value("${validation.topics.outbound.fraud-detected:fraud-detected}")
  public String FRAUD_DETECTED_TOPIC;

  @Value("${validation.topics.outbound.risk-assessment:risk-assessment}")
  public String RISK_ASSESSMENT_TOPIC;

  // Topic Configuration
  @Value("${validation.topics.partitions:3}")
  public int TOPIC_PARTITIONS;

  @Value("${validation.topics.replication-factor:1}")
  public short REPLICATION_FACTOR;

  @Value("${validation.topics.retention-ms:604800000}") // 7 days
  public long RETENTION_MS;

  /** Get topic configuration for creating topics */
  public Map<String, Object> getTopicConfig() {
    Map<String, Object> config = new HashMap<>();
    config.put("num.partitions", TOPIC_PARTITIONS);
    config.put("default.replication.factor", REPLICATION_FACTOR);
    config.put("retention.ms", RETENTION_MS);
    config.put("cleanup.policy", "delete");
    config.put("compression.type", "snappy");
    return config;
  }

  /** Get all inbound topics */
  public List<String> getInboundTopics() {
    return List.of(PAYMENT_INITIATED_TOPIC, PAYMENT_UPDATED_TOPIC, ACCOUNT_CHANGED_TOPIC);
  }

  /** Get all outbound topics */
  public List<String> getOutboundTopics() {
    return List.of(
        PAYMENT_VALIDATED_TOPIC,
        VALIDATION_FAILED_TOPIC,
        FRAUD_DETECTED_TOPIC,
        RISK_ASSESSMENT_TOPIC);
  }

  /** Get all topics */
  public List<String> getAllTopics() {
    List<String> allTopics = new ArrayList<>();
    allTopics.addAll(getInboundTopics());
    allTopics.addAll(getOutboundTopics());
    return allTopics;
  }
}
