package com.paymentengine.shared.util;

import com.paymentengine.shared.event.PaymentEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

/**
 * Utility class for publishing payment events to Kafka topics.
 * Provides centralized event publishing with proper error handling and logging.
 */
@Component
public class EventPublisher {
    
    private static final Logger logger = LoggerFactory.getLogger(EventPublisher.class);
    
    private final KafkaTemplate<String, PaymentEvent> kafkaTemplate;
    
    public EventPublisher(KafkaTemplate<String, PaymentEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }
    
    /**
     * Publish a payment event to the specified topic
     * 
     * @param topic The Kafka topic to publish to
     * @param key The message key (typically transaction ID or account ID)
     * @param event The payment event to publish
     * @return CompletableFuture for async handling
     */
    public CompletableFuture<SendResult<String, PaymentEvent>> publishEvent(String topic, String key, PaymentEvent event) {
        logger.debug("Publishing event {} to topic {} with key {}", 
                    event.getEventType(), topic, key);
        
        CompletableFuture<SendResult<String, PaymentEvent>> future = kafkaTemplate.send(topic, key, event);
        
        future.whenComplete((result, ex) -> {
            if (ex != null) {
                logger.error("Failed to publish event {} to topic {} with key {}: {}", 
                           event.getEventType(), topic, key, ex.getMessage(), ex);
            } else {
                logger.info("Successfully published event {} to topic {} with key {} at offset {}", 
                          event.getEventType(), topic, key, result.getRecordMetadata().offset());
            }
        });
        
        return future;
    }
    
    /**
     * Publish a payment event to the specified topic without a key
     * 
     * @param topic The Kafka topic to publish to
     * @param event The payment event to publish
     * @return CompletableFuture for async handling
     */
    public CompletableFuture<SendResult<String, PaymentEvent>> publishEvent(String topic, PaymentEvent event) {
        return publishEvent(topic, null, event);
    }
    
    /**
     * Publish a payment event synchronously (blocks until complete)
     * 
     * @param topic The Kafka topic to publish to
     * @param key The message key
     * @param event The payment event to publish
     * @throws RuntimeException if publishing fails
     */
    public void publishEventSync(String topic, String key, PaymentEvent event) {
        try {
            SendResult<String, PaymentEvent> result = publishEvent(topic, key, event).get();
            logger.info("Synchronously published event {} to topic {} at offset {}", 
                       event.getEventType(), topic, result.getRecordMetadata().offset());
        } catch (Exception e) {
            logger.error("Failed to synchronously publish event {} to topic {}: {}", 
                        event.getEventType(), topic, e.getMessage(), e);
            throw new RuntimeException("Failed to publish event", e);
        }
    }
    
    /**
     * Publish a payment event synchronously without a key
     * 
     * @param topic The Kafka topic to publish to
     * @param event The payment event to publish
     * @throws RuntimeException if publishing fails
     */
    public void publishEventSync(String topic, PaymentEvent event) {
        publishEventSync(topic, null, event);
    }
}