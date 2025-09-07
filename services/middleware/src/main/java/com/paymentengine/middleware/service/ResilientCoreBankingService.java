package com.paymentengine.middleware.service;

import com.paymentengine.middleware.dto.corebanking.*;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.timelimiter.TimeLimiter;
import io.github.resilience4j.bulkhead.Bulkhead;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

/**
 * Resilient wrapper for core banking service calls
 * Uses existing Resilience4j patterns from ResilienceConfiguration
 */
@Service
public class ResilientCoreBankingService {
    
    @Autowired
    private CircuitBreaker coreBankingDebitCircuitBreaker;
    
    @Autowired
    private CircuitBreaker coreBankingCreditCircuitBreaker;
    
    @Autowired
    private Retry coreBankingRetry;
    
    @Autowired
    private TimeLimiter coreBankingTimeLimiter;
    
    @Autowired
    private Bulkhead coreBankingBulkhead;
    
    @Autowired
    @Qualifier("restCoreBankingAdapter")
    private CoreBankingAdapter coreBankingAdapter;
    
    /**
     * Process debit transaction with resiliency patterns
     */
    public TransactionResult processDebit(DebitTransactionRequest request) {
        Supplier<TransactionResult> supplier = () -> {
            return coreBankingAdapter.processDebit(request);
        };
        
        // Apply resiliency patterns in order: Bulkhead -> TimeLimiter -> Retry -> CircuitBreaker
        Supplier<TransactionResult> decoratedSupplier = Bulkhead
                .decorateSupplier(coreBankingBulkhead, supplier);
        
        decoratedSupplier = TimeLimiter
                .decorateSupplier(coreBankingTimeLimiter, decoratedSupplier);
        
        decoratedSupplier = Retry
                .decorateSupplier(coreBankingRetry, decoratedSupplier);
        
        decoratedSupplier = CircuitBreaker
                .decorateSupplier(coreBankingDebitCircuitBreaker, decoratedSupplier);
        
        return decoratedSupplier.get();
    }
    
    /**
     * Process credit transaction with resiliency patterns
     */
    public TransactionResult processCredit(CreditTransactionRequest request) {
        Supplier<TransactionResult> supplier = () -> {
            return coreBankingAdapter.processCredit(request);
        };
        
        // Apply resiliency patterns in order: Bulkhead -> TimeLimiter -> Retry -> CircuitBreaker
        Supplier<TransactionResult> decoratedSupplier = Bulkhead
                .decorateSupplier(coreBankingBulkhead, supplier);
        
        decoratedSupplier = TimeLimiter
                .decorateSupplier(coreBankingTimeLimiter, decoratedSupplier);
        
        decoratedSupplier = Retry
                .decorateSupplier(coreBankingRetry, decoratedSupplier);
        
        decoratedSupplier = CircuitBreaker
                .decorateSupplier(coreBankingCreditCircuitBreaker, decoratedSupplier);
        
        return decoratedSupplier.get();
    }
    
    /**
     * Process transfer transaction with resiliency patterns
     */
    public TransactionResult processTransfer(TransferTransactionRequest request) {
        Supplier<TransactionResult> supplier = () -> {
            return coreBankingAdapter.processTransfer(request);
        };
        
        // Apply resiliency patterns in order: Bulkhead -> TimeLimiter -> Retry -> CircuitBreaker
        Supplier<TransactionResult> decoratedSupplier = Bulkhead
                .decorateSupplier(coreBankingBulkhead, supplier);
        
        decoratedSupplier = TimeLimiter
                .decorateSupplier(coreBankingTimeLimiter, decoratedSupplier);
        
        decoratedSupplier = Retry
                .decorateSupplier(coreBankingRetry, decoratedSupplier);
        
        // Use debit circuit breaker for transfers (they involve both debit and credit)
        decoratedSupplier = CircuitBreaker
                .decorateSupplier(coreBankingDebitCircuitBreaker, decoratedSupplier);
        
        return decoratedSupplier.get();
    }
    
    /**
     * Process debit transaction asynchronously with resiliency patterns
     */
    public CompletableFuture<TransactionResult> processDebitAsync(DebitTransactionRequest request) {
        Supplier<CompletableFuture<TransactionResult>> supplier = () -> {
            return CompletableFuture.supplyAsync(() -> {
                return coreBankingAdapter.processDebit(request);
            });
        };
        
        // Apply resiliency patterns
        Supplier<CompletableFuture<TransactionResult>> decoratedSupplier = Bulkhead
                .decorateSupplier(coreBankingBulkhead, supplier);
        
        decoratedSupplier = TimeLimiter
                .decorateSupplier(coreBankingTimeLimiter, decoratedSupplier);
        
        decoratedSupplier = Retry
                .decorateSupplier(coreBankingRetry, decoratedSupplier);
        
        decoratedSupplier = CircuitBreaker
                .decorateSupplier(coreBankingDebitCircuitBreaker, decoratedSupplier);
        
        return decoratedSupplier.get();
    }
    
    /**
     * Process credit transaction asynchronously with resiliency patterns
     */
    public CompletableFuture<TransactionResult> processCreditAsync(CreditTransactionRequest request) {
        Supplier<CompletableFuture<TransactionResult>> supplier = () -> {
            return CompletableFuture.supplyAsync(() -> {
                return coreBankingAdapter.processCredit(request);
            });
        };
        
        // Apply resiliency patterns
        Supplier<CompletableFuture<TransactionResult>> decoratedSupplier = Bulkhead
                .decorateSupplier(coreBankingBulkhead, supplier);
        
        decoratedSupplier = TimeLimiter
                .decorateSupplier(coreBankingTimeLimiter, decoratedSupplier);
        
        decoratedSupplier = Retry
                .decorateSupplier(coreBankingRetry, decoratedSupplier);
        
        decoratedSupplier = CircuitBreaker
                .decorateSupplier(coreBankingCreditCircuitBreaker, decoratedSupplier);
        
        return decoratedSupplier.get();
    }
    
    /**
     * Get core banking service health status
     */
    public Map<String, Object> getCoreBankingHealth() {
        Map<String, Object> health = Map.of(
            "debitCircuitBreakerState", coreBankingDebitCircuitBreaker.getState(),
            "creditCircuitBreakerState", coreBankingCreditCircuitBreaker.getState(),
            "bulkheadAvailableCalls", coreBankingBulkhead.getMetrics().getAvailableConcurrentCalls(),
            "retryMetrics", coreBankingRetry.getMetrics(),
            "timeLimiterMetrics", coreBankingTimeLimiter.getMetrics()
        );
        
        return health;
    }
}