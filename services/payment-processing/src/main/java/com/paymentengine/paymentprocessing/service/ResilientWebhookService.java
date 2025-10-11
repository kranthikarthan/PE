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
            return restTemplate.postForObject(
                    request.getUrl(), 
                    request.getPayload(), 
                    WebhookResponse.class
            );
        };
        
        Supplier<WebhookResponse> decoratedSupplier = CircuitBreaker
                .decorateSupplier(webhookCircuitBreaker, supplier);
        
        decoratedSupplier = Retry.decorateSupplier(webhookRetry, decoratedSupplier);
        {
            Supplier<WebhookResponse> finalDecorated = decoratedSupplier;
            decoratedSupplier = () -> TimeLimiter
                    .decorateFutureSupplier(webhookTimeLimiter, () -> java.util.concurrent.CompletableFuture.supplyAsync(finalDecorated::get))
                    .get();
        }
        decoratedSupplier = Bulkhead.decorateSupplier(webhookBulkhead, decoratedSupplier);
        
        return decoratedSupplier.get();
    }
    
    public void deliverWebhookAsync(WebhookRequest request) {
        Supplier<Void> supplier = () -> {
            restTemplate.postForObject(
                    request.getUrl(), 
                    request.getPayload(), 
                    Void.class
            );
            return null;
        };
        
        Supplier<Void> decoratedSupplier = CircuitBreaker
                .decorateSupplier(webhookCircuitBreaker, supplier);
        
        decoratedSupplier = Retry.decorateSupplier(webhookRetry, decoratedSupplier);
        {
            Supplier<Void> finalDecorated = decoratedSupplier;
            decoratedSupplier = () -> TimeLimiter
                    .decorateFutureSupplier(webhookTimeLimiter, () -> java.util.concurrent.CompletableFuture.supplyAsync(finalDecorated::get))
                    .get();
        }
        decoratedSupplier = Bulkhead.decorateSupplier(webhookBulkhead, decoratedSupplier);
        
        decoratedSupplier.get();
    }
}