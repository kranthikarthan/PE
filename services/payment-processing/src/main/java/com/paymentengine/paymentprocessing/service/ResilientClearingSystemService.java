package com.paymentengine.paymentprocessing.service;

import com.paymentengine.paymentprocessing.dto.ClearingSystemRequest;
import com.paymentengine.paymentprocessing.dto.ClearingSystemResponse;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.timelimiter.TimeLimiter;
import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.ratelimiter.RateLimiter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.Proxy;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

@Service
public class ResilientClearingSystemService {
    
    @Autowired
    private CircuitBreaker clearingSystemCircuitBreaker;
    
    @Autowired
    private Retry clearingSystemRetry;
    
    @Autowired
    private TimeLimiter clearingSystemTimeLimiter;
    
    @Autowired
    private Bulkhead clearingSystemBulkhead;
    
    @Autowired
    private RateLimiter clearingSystemRateLimiter;
    
    @Autowired
    private ClearingSystemRoutingService clearingSystemRoutingService;
    
    public List<ClearingSystemResponse> getAllClearingSystems() {
        Supplier<List<ClearingSystemResponse>> supplier = () -> clearingSystemRoutingService.getAllClearingSystems();

        return executeWithResilience(supplier);
    }

    public ClearingSystemResponse getClearingSystemById(UUID id) {
        Supplier<ClearingSystemResponse> supplier = () -> clearingSystemRoutingService.getClearingSystemById(id);

        return executeWithResilience(supplier);
    }

    public ClearingSystemResponse createClearingSystem(ClearingSystemRequest request) {
        Supplier<ClearingSystemResponse> supplier = () -> clearingSystemRoutingService.createClearingSystem(request);

        return executeWithResilience(supplier);
    }

    public ClearingSystemResponse updateClearingSystem(UUID id, ClearingSystemRequest request) {
        Supplier<ClearingSystemResponse> supplier = () -> clearingSystemRoutingService.updateClearingSystem(id, request);

        return executeWithResilience(supplier);
    }

    public void deleteClearingSystem(UUID id) {
        executeWithResilience(() -> {
            clearingSystemRoutingService.deleteClearingSystem(id);
            return null;
        });
    }

    public CompletableFuture<ClearingSystemResponse> getClearingSystemByIdAsync(UUID id) {
        Supplier<CompletableFuture<ClearingSystemResponse>> supplier = () ->
                CompletableFuture.supplyAsync(() -> clearingSystemRoutingService.getClearingSystemById(id));

        return executeWithResilience(supplier);
    }

    public CompletableFuture<List<ClearingSystemResponse>> getAllClearingSystemsAsync() {
        Supplier<CompletableFuture<List<ClearingSystemResponse>>> supplier = () ->
                CompletableFuture.supplyAsync(clearingSystemRoutingService::getAllClearingSystems);

        return executeWithResilience(supplier);
    }

    private <T> T executeWithResilience(Supplier<T> supplier) {
        Supplier<T> decoratedSupplier = supplier;

        if (clearingSystemCircuitBreaker != null && !isProxy(clearingSystemCircuitBreaker)) {
            decoratedSupplier = CircuitBreaker.decorateSupplier(clearingSystemCircuitBreaker, decoratedSupplier);
        }

        if (clearingSystemRetry != null && !isProxy(clearingSystemRetry)) {
            decoratedSupplier = Retry.decorateSupplier(clearingSystemRetry, decoratedSupplier);
        }

        if (clearingSystemTimeLimiter != null && !isProxy(clearingSystemTimeLimiter)) {
            decoratedSupplier = TimeLimiter.decorateSupplier(clearingSystemTimeLimiter, decoratedSupplier);
        }

        if (clearingSystemBulkhead != null && !isProxy(clearingSystemBulkhead)) {
            decoratedSupplier = Bulkhead.decorateSupplier(clearingSystemBulkhead, decoratedSupplier);
        }

        if (clearingSystemRateLimiter != null && !isProxy(clearingSystemRateLimiter)) {
            decoratedSupplier = RateLimiter.decorateSupplier(clearingSystemRateLimiter, decoratedSupplier);
        }

        return decoratedSupplier.get();
    }

    private boolean isProxy(Object candidate) {
        return candidate != null && Proxy.isProxyClass(candidate.getClass());
    }
}