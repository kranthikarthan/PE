package com.paymentengine.paymentprocessing.config;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

/**
 * Enterprise-grade Kafka Producer Configuration
 * - Idempotent producer enabled
 * - Exactly-once semantics
 * - Compression enabled
 * - Metrics integration
 */
@Configuration
public class KafkaProducerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.producer.acks:all}")
    private String acks;

    @Value("${spring.kafka.producer.retries:2147483647}")
    private Integer retries;

    @Value("${spring.kafka.producer.enable-idempotence:true}")
    private Boolean enableIdempotence;

    @Value("${spring.kafka.producer.compression-type:snappy}")
    private String compressionType;

    @Value("${spring.kafka.producer.batch-size:16384}")
    private Integer batchSize;

    @Value("${spring.kafka.producer.linger-ms:10}")
    private Integer lingerMs;

    @Value("${spring.kafka.producer.buffer-memory:33554432}")
    private Long bufferMemory;

    @Value("${spring.kafka.producer.max-in-flight-requests-per-connection:5}")
    private Integer maxInFlightRequests;

    @Bean
    public ProducerFactory<String, Object> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        
        // Basic Configuration
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        
        // Idempotence & Reliability
        configProps.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, enableIdempotence);
        configProps.put(ProducerConfig.ACKS_CONFIG, acks);
        configProps.put(ProducerConfig.RETRIES_CONFIG, retries);
        configProps.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, maxInFlightRequests);
        
        // Performance Tuning
        configProps.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, compressionType);
        configProps.put(ProducerConfig.BATCH_SIZE_CONFIG, batchSize);
        configProps.put(ProducerConfig.LINGER_MS_CONFIG, lingerMs);
        configProps.put(ProducerConfig.BUFFER_MEMORY_CONFIG, bufferMemory);
        
        // Timeouts
        configProps.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, 30000);
        configProps.put(ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG, 120000);
        
        // Metrics
        configProps.put(ProducerConfig.METRICS_RECORDING_LEVEL_CONFIG, "INFO");
        
        // Serialization
        configProps.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, false);
        configProps.put(JsonSerializer.TYPE_MAPPINGS, 
            "paymentMessage:com.paymentengine.paymentprocessing.dto.PaymentMessage");
        
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate(
            ProducerFactory<String, Object> producerFactory) {
        
        KafkaTemplate<String, Object> template = new KafkaTemplate<>(producerFactory);
        
        // Set default topic (can be overridden)
        template.setDefaultTopic("payment.default.v1");
        
        return template;
    }
}
