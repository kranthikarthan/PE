package com.paymentengine.paymentprocessing.service;

import com.paymentengine.paymentprocessing.dto.WebhookRequest;
import com.paymentengine.paymentprocessing.dto.WebhookResponse;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.timelimiter.TimeLimiter;
import io.github.resilience4j.bulkhead.Bulkhead;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.function.Supplier;

@Service
public class ResilientWebhookService {
    
    @Autowired
    private CircuitBreaker webhookCircuitBreaker;
    
    @Autowired
    private Retry webhookRetry;
    
    @Autowired
    private TimeLimiter webhookTimeLimiter;
    
    @Autowired
    private Bulkhead webhookBulkhead;
    
    @Autowired
    private RestTemplate restTemplate;
    
    public WebhookResponse deliverWebhook(WebhookRequest request) {
        Supplier<WebhookResponse> supplier = () -> {
            WebhookResponse response = new WebhookResponse();
            response.setWebhookId(request.getWebhookId());
            response.setSuccess(true);
            return response;
        };
        
        Supplier<WebhookResponse> decoratedSupplier = CircuitBreaker
                .decorateSupplier(webhookCircuitBreaker, supplier);
        
        decoratedSupplier = Retry.decorateSupplier(webhookRetry, decoratedSupplier);
        decoratedSupplier = Bulkhead.decorateSupplier(webhookBulkhead, decoratedSupplier);
        
        return decoratedSupplier.get();
    }
    
    public void deliverWebhookAsync(WebhookRequest request) {
        Supplier<Void> supplier = () -> null;
        
        Supplier<Void> decoratedSupplier = CircuitBreaker
                .decorateSupplier(webhookCircuitBreaker, supplier);
        
        decoratedSupplier = Retry.decorateSupplier(webhookRetry, decoratedSupplier);
        decoratedSupplier = Bulkhead.decorateSupplier(webhookBulkhead, decoratedSupplier);
        
        decoratedSupplier.get();
    }
}