package com.paymentengine.paymentprocessing.service;

import com.paymentengine.paymentprocessing.dto.KafkaMessageRequest;
import com.paymentengine.paymentprocessing.dto.KafkaMessageResponse;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.timelimiter.TimeLimiter;
import io.github.resilience4j.bulkhead.Bulkhead;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.function.Supplier;

@Service
public class ResilientKafkaService {
    
    @Autowired
    private CircuitBreaker kafkaCircuitBreaker;
    
    @Autowired
    private Retry kafkaRetry;
    
    @Autowired
    private TimeLimiter kafkaTimeLimiter;
    
    @Autowired
    private Bulkhead kafkaBulkhead;
    
    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;
    
    public KafkaMessageResponse sendMessage(KafkaMessageRequest request) {
        Supplier<KafkaMessageResponse> supplier = () -> {
            KafkaMessageResponse resp = new KafkaMessageResponse();
            resp.setMessageId(request.getMessageId());
            resp.setSuccess(true);
            return resp;
        };
        
        Supplier<KafkaMessageResponse> decoratedSupplier = CircuitBreaker
                .decorateSupplier(kafkaCircuitBreaker, supplier);
        
        decoratedSupplier = Retry.decorateSupplier(kafkaRetry, decoratedSupplier);
        // no time limiter wrapping required for stub
        decoratedSupplier = Bulkhead.decorateSupplier(kafkaBulkhead, decoratedSupplier);
        
        return decoratedSupplier.get();
    }
    
    public void sendMessageAsync(KafkaMessageRequest request) {
        Supplier<Void> supplier = () -> null;
        
        Supplier<Void> decoratedSupplier = CircuitBreaker
                .decorateSupplier(kafkaCircuitBreaker, supplier);
        
        decoratedSupplier = Retry.decorateSupplier(kafkaRetry, decoratedSupplier);
        // no time limiter wrapping required for stub
        decoratedSupplier = Bulkhead.decorateSupplier(kafkaBulkhead, decoratedSupplier);
        
        decoratedSupplier.get();
    }
}