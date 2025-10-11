package com.paymentengine.paymentprocessing.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Custom error handler for Kafka message processing
 */
@Component
public class KafkaErrorHandler {
    private static final Logger logger = LoggerFactory.getLogger(KafkaErrorHandler.class);
}