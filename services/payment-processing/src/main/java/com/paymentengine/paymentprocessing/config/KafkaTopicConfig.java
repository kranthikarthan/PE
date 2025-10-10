package com.paymentengine.paymentprocessing.config;

import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaAdmin;

import java.util.HashMap;
import java.util.Map;

/**
 * Kafka Topic Configuration with Versioning Strategy
 * - Topic naming convention: <domain>.<event-type>.v<version>
 * - Separate topics for DLQ
 * - Optimized partition and replication settings
 */
@Configuration
public class KafkaTopicConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.topic.partitions:3}")
    private int partitions;

    @Value("${spring.kafka.topic.replication-factor:1}")
    private short replicationFactor;

    @Value("${spring.kafka.topic.min-insync-replicas:1}")
    private String minInSyncReplicas;

    @Bean
    public KafkaAdmin kafkaAdmin() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        return new KafkaAdmin(configs);
    }

    // ============================================================================
    // PAYMENT TOPICS - Version 1
    // ============================================================================

    @Bean
    public NewTopic paymentInboundTopicV1() {
        return TopicBuilder.name("payment.inbound.v1")
                .partitions(partitions)
                .replicas(replicationFactor)
                .config("retention.ms", "604800000")  // 7 days
                .config("compression.type", "snappy")
                .config("min.insync.replicas", minInSyncReplicas)
                .build();
    }

    @Bean
    public NewTopic paymentOutboundTopicV1() {
        return TopicBuilder.name("payment.outbound.v1")
                .partitions(partitions)
                .replicas(replicationFactor)
                .config("retention.ms", "604800000")  // 7 days
                .config("compression.type", "snappy")
                .config("min.insync.replicas", minInSyncReplicas)
                .build();
    }

    @Bean
    public NewTopic paymentAckTopicV1() {
        return TopicBuilder.name("payment.ack.v1")
                .partitions(partitions)
                .replicas(replicationFactor)
                .config("retention.ms", "259200000")  // 3 days
                .config("compression.type", "snappy")
                .config("min.insync.replicas", minInSyncReplicas)
                .build();
    }

    // ============================================================================
    // DEAD LETTER QUEUE TOPICS
    // ============================================================================

    @Bean
    public NewTopic paymentInboundDlqTopic() {
        return TopicBuilder.name("payment.inbound.dlq.v1")
                .partitions(partitions)
                .replicas(replicationFactor)
                .config("retention.ms", "2592000000")  // 30 days
                .config("compression.type", "snappy")
                .config("min.insync.replicas", minInSyncReplicas)
                .build();
    }

    @Bean
    public NewTopic paymentOutboundDlqTopic() {
        return TopicBuilder.name("payment.outbound.dlq.v1")
                .partitions(partitions)
                .replicas(replicationFactor)
                .config("retention.ms", "2592000000")  // 30 days
                .config("compression.type", "snappy")
                .config("min.insync.replicas", minInSyncReplicas)
                .build();
    }

    // ============================================================================
    // ISO20022 MESSAGE TOPICS - Version 1
    // ============================================================================

    @Bean
    public NewTopic iso20022Pain001Topic() {
        return TopicBuilder.name("iso20022.pain001.v1")
                .partitions(partitions)
                .replicas(replicationFactor)
                .config("retention.ms", "604800000")  // 7 days
                .config("compression.type", "snappy")
                .config("min.insync.replicas", minInSyncReplicas)
                .build();
    }

    @Bean
    public NewTopic iso20022Pacs008Topic() {
        return TopicBuilder.name("iso20022.pacs008.v1")
                .partitions(partitions)
                .replicas(replicationFactor)
                .config("retention.ms", "604800000")  // 7 days
                .config("compression.type", "snappy")
                .config("min.insync.replicas", minInSyncReplicas)
                .build();
    }

    @Bean
    public NewTopic iso20022Pacs002Topic() {
        return TopicBuilder.name("iso20022.pacs002.v1")
                .partitions(partitions)
                .replicas(replicationFactor)
                .config("retention.ms", "604800000")  // 7 days
                .config("compression.type", "snappy")
                .config("min.insync.replicas", minInSyncReplicas)
                .build();
    }

    @Bean
    public NewTopic iso20022DlqTopic() {
        return TopicBuilder.name("iso20022.dlq.v1")
                .partitions(partitions)
                .replicas(replicationFactor)
                .config("retention.ms", "2592000000")  // 30 days
                .config("compression.type", "snappy")
                .config("min.insync.replicas", minInSyncReplicas)
                .build();
    }

    // ============================================================================
    // AUDIT & EVENT TOPICS
    // ============================================================================

    @Bean
    public NewTopic auditEventTopic() {
        return TopicBuilder.name("audit.event.v1")
                .partitions(partitions)
                .replicas(replicationFactor)
                .config("retention.ms", "2592000000")  // 30 days
                .config("compression.type", "snappy")
                .config("min.insync.replicas", minInSyncReplicas)
                .build();
    }
}
