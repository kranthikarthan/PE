package com.paymentengine.middleware.service;

import com.paymentengine.middleware.dto.LoginRequest;
import com.paymentengine.middleware.dto.LoginResponse;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.timelimiter.TimeLimiter;
import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.ratelimiter.RateLimiter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.function.Supplier;

@Service
public class ResilientAuthService {
    
    @Autowired
    private CircuitBreaker authServiceCircuitBreaker;
    
    @Autowired
    private Retry authServiceRetry;
    
    @Autowired
    private TimeLimiter authServiceTimeLimiter;
    
    @Autowired
    private Bulkhead authServiceBulkhead;
    
    @Autowired
    private RateLimiter authServiceRateLimiter;
    
    @Autowired
    private RestTemplate restTemplate;
    
    private static final String AUTH_SERVICE_URL = "http://auth-service:8080/api/v1/auth";
    
    public LoginResponse authenticate(LoginRequest request) {
        Supplier<LoginResponse> supplier = () -> {
            return restTemplate.postForObject(
                    AUTH_SERVICE_URL + "/login", 
                    request, 
                    LoginResponse.class
            );
        };
        
        Supplier<LoginResponse> decoratedSupplier = CircuitBreaker
                .decorateSupplier(authServiceCircuitBreaker, supplier);
        
        decoratedSupplier = Retry.decorateSupplier(authServiceRetry, decoratedSupplier);
        decoratedSupplier = TimeLimiter.decorateSupplier(authServiceTimeLimiter, decoratedSupplier);
        decoratedSupplier = Bulkhead.decorateSupplier(authServiceBulkhead, decoratedSupplier);
        decoratedSupplier = RateLimiter.decorateSupplier(authServiceRateLimiter, decoratedSupplier);
        
        return decoratedSupplier.get();
    }
    
    public boolean validateToken(String token) {
        Supplier<Boolean> supplier = () -> {
            return restTemplate.getForObject(
                    AUTH_SERVICE_URL + "/validate?token=" + token, 
                    Boolean.class
            );
        };
        
        Supplier<Boolean> decoratedSupplier = CircuitBreaker
                .decorateSupplier(authServiceCircuitBreaker, supplier);
        
        decoratedSupplier = Retry.decorateSupplier(authServiceRetry, decoratedSupplier);
        decoratedSupplier = TimeLimiter.decorateSupplier(authServiceTimeLimiter, decoratedSupplier);
        decoratedSupplier = Bulkhead.decorateSupplier(authServiceBulkhead, decoratedSupplier);
        decoratedSupplier = RateLimiter.decorateSupplier(authServiceRateLimiter, decoratedSupplier);
        
        return decoratedSupplier.get();
    }
    
    public LoginResponse refreshToken(String refreshToken) {
        Supplier<LoginResponse> supplier = () -> {
            return restTemplate.postForObject(
                    AUTH_SERVICE_URL + "/refresh", 
                    refreshToken, 
                    LoginResponse.class
            );
        };
        
        Supplier<LoginResponse> decoratedSupplier = CircuitBreaker
                .decorateSupplier(authServiceCircuitBreaker, supplier);
        
        decoratedSupplier = Retry.decorateSupplier(authServiceRetry, decoratedSupplier);
        decoratedSupplier = TimeLimiter.decorateSupplier(authServiceTimeLimiter, decoratedSupplier);
        decoratedSupplier = Bulkhead.decorateSupplier(authServiceBulkhead, decoratedSupplier);
        decoratedSupplier = RateLimiter.decorateSupplier(authServiceRateLimiter, decoratedSupplier);
        
        return decoratedSupplier.get();
    }
    
    public void logout(String token) {
        Supplier<Void> supplier = () -> {
            restTemplate.postForObject(
                    AUTH_SERVICE_URL + "/logout", 
                    token, 
                    Void.class
            );
            return null;
        };
        
        Supplier<Void> decoratedSupplier = CircuitBreaker
                .decorateSupplier(authServiceCircuitBreaker, supplier);
        
        decoratedSupplier = Retry.decorateSupplier(authServiceRetry, decoratedSupplier);
        decoratedSupplier = TimeLimiter.decorateSupplier(authServiceTimeLimiter, decoratedSupplier);
        decoratedSupplier = Bulkhead.decorateSupplier(authServiceBulkhead, decoratedSupplier);
        decoratedSupplier = RateLimiter.decorateSupplier(authServiceRateLimiter, decoratedSupplier);
        
        decoratedSupplier.get();
    }
}