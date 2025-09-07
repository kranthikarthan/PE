package com.paymentengine.middleware.service;

import com.paymentengine.middleware.entity.FraudRiskAssessment;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.timelimiter.TimeLimiter;
import io.github.resilience4j.bulkhead.Bulkhead;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

/**
 * Resilient wrapper for fraud API service calls
 * Uses existing Resilience4j patterns from ResilienceConfiguration
 */
@Service
public class ResilientFraudApiService {
    
    @Autowired
    private CircuitBreaker fraudApiCircuitBreaker;
    
    @Autowired
    private Retry fraudApiRetry;
    
    @Autowired
    private TimeLimiter fraudApiTimeLimiter;
    
    @Autowired
    private Bulkhead fraudApiBulkhead;
    
    @Autowired
    private ExternalFraudApiService externalFraudApiService;
    
    /**
     * Call fraud API with resiliency patterns
     */
    public Map<String, Object> callFraudApi(Map<String, Object> bankFraudApiConfig, 
                                           Map<String, Object> paymentData, 
                                           FraudRiskAssessment assessment) {
        
        Supplier<Map<String, Object>> supplier = () -> {
            return externalFraudApiService.buildBankFraudApiRequest(bankFraudApiConfig, paymentData, assessment);
        };
        
        // Apply resiliency patterns in order: Bulkhead -> TimeLimiter -> Retry -> CircuitBreaker
        Supplier<Map<String, Object>> decoratedSupplier = Bulkhead
                .decorateSupplier(fraudApiBulkhead, supplier);
        
        decoratedSupplier = TimeLimiter
                .decorateSupplier(fraudApiTimeLimiter, decoratedSupplier);
        
        decoratedSupplier = Retry
                .decorateSupplier(fraudApiRetry, decoratedSupplier);
        
        decoratedSupplier = CircuitBreaker
                .decorateSupplier(fraudApiCircuitBreaker, decoratedSupplier);
        
        return decoratedSupplier.get();
    }
    
    /**
     * Call fraud API asynchronously with resiliency patterns
     */
    public CompletableFuture<Map<String, Object>> callFraudApiAsync(Map<String, Object> bankFraudApiConfig, 
                                                                   Map<String, Object> paymentData, 
                                                                   FraudRiskAssessment assessment) {
        
        Supplier<CompletableFuture<Map<String, Object>>> supplier = () -> {
            return CompletableFuture.supplyAsync(() -> {
                return externalFraudApiService.buildBankFraudApiRequest(bankFraudApiConfig, paymentData, assessment);
            });
        };
        
        // Apply resiliency patterns
        Supplier<CompletableFuture<Map<String, Object>>> decoratedSupplier = Bulkhead
                .decorateSupplier(fraudApiBulkhead, supplier);
        
        decoratedSupplier = TimeLimiter
                .decorateSupplier(fraudApiTimeLimiter, decoratedSupplier);
        
        decoratedSupplier = Retry
                .decorateSupplier(fraudApiRetry, decoratedSupplier);
        
        decoratedSupplier = CircuitBreaker
                .decorateSupplier(fraudApiCircuitBreaker, decoratedSupplier);
        
        return decoratedSupplier.get();
    }
    
    /**
     * Get fraud API health status
     */
    public Map<String, Object> getFraudApiHealth() {
        Map<String, Object> health = Map.of(
            "circuitBreakerState", fraudApiCircuitBreaker.getState(),
            "bulkheadAvailableCalls", fraudApiBulkhead.getMetrics().getAvailableConcurrentCalls(),
            "retryMetrics", fraudApiRetry.getMetrics(),
            "timeLimiterMetrics", fraudApiTimeLimiter.getMetrics()
        );
        
        return health;
    }
}