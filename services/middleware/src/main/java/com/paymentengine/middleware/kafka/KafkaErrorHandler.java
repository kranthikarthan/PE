package com.paymentengine.middleware.kafka;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.listener.ErrorHandler;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Custom error handler for Kafka message processing
 */
@Component
public class KafkaErrorHandler implements ErrorHandler {

    private static final Logger logger = LoggerFactory.getLogger(KafkaErrorHandler.class);

    @Override
    public void handle(Exception thrownException, ConsumerRecord<?, ?> data, Consumer<?, ?> consumer) {
        logger.error("Error processing Kafka message: {}", data, thrownException);
        
        // Log the error details
        logger.error("Topic: {}, Partition: {}, Offset: {}, Key: {}", 
                data.topic(), data.partition(), data.offset(), data.key());
        
        // In a real implementation, you might want to:
        // 1. Send the message to a dead letter queue
        // 2. Notify monitoring systems
        // 3. Retry with exponential backoff
        // 4. Store the error for later analysis
        
        // For now, we'll just log and continue
        logger.warn("Continuing with next message after error");
    }

    @Override
    public void handle(Exception thrownException, Message<?> message, MessageListenerContainer container) {
        MessageHeaders headers = message.getHeaders();
        String topic = headers.get(KafkaHeaders.RECEIVED_TOPIC, String.class);
        Integer partition = headers.get(KafkaHeaders.RECEIVED_PARTITION, Integer.class);
        Long offset = headers.get(KafkaHeaders.OFFSET, Long.class);
        Object key = headers.get(KafkaHeaders.RECEIVED_MESSAGE_KEY);
        
        logger.error("Error processing Kafka message from topic: {}, partition: {}, offset: {}, key: {}", 
                topic, partition, offset, key, thrownException);
        
        // Log the error details
        logger.error("Message headers: {}", headers);
        logger.error("Message payload: {}", message.getPayload());
        
        // In a real implementation, you might want to:
        // 1. Send the message to a dead letter queue
        // 2. Notify monitoring systems
        // 3. Retry with exponential backoff
        // 4. Store the error for later analysis
        
        // For now, we'll just log and continue
        logger.warn("Continuing with next message after error");
    }
}