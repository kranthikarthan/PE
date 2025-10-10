package com.paymentengine.gateway.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Map;

/**
 * Fallback controller for circuit breaker responses
 */
@RestController
public class FallbackController {
    
    @RequestMapping("/fallback/general")
    public Mono<ResponseEntity<Map<String, Object>>> generalFallback() {
        Map<String, Object> response = Map.of(
            "error", Map.of(
                "code", "SERVICE_UNAVAILABLE",
                "message", "Service is temporarily unavailable. Please try again later.",
                "timestamp", Instant.now().toString(),
                "fallback", true
            )
        );
        
        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response));
    }
    
    @RequestMapping("/fallback/transaction")
    public Mono<ResponseEntity<Map<String, Object>>> transactionFallback() {
        Map<String, Object> response = Map.of(
            "error", Map.of(
                "code", "TRANSACTION_SERVICE_UNAVAILABLE",
                "message", "Transaction service is temporarily unavailable. Your request has been queued for processing.",
                "timestamp", Instant.now().toString(),
                "fallback", true,
                "retryAfter", "300" // 5 minutes
            )
        );
        
        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response));
    }
    
    @RequestMapping("/fallback/account")
    public Mono<ResponseEntity<Map<String, Object>>> accountFallback() {
        Map<String, Object> response = Map.of(
            "error", Map.of(
                "code", "ACCOUNT_SERVICE_UNAVAILABLE",
                "message", "Account service is temporarily unavailable. Please try again later.",
                "timestamp", Instant.now().toString(),
                "fallback", true,
                "retryAfter", "60" // 1 minute
            )
        );
        
        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response));
    }
    
    @RequestMapping("/fallback/webhook")
    public Mono<ResponseEntity<Map<String, Object>>> webhookFallback() {
        Map<String, Object> response = Map.of(
            "error", Map.of(
                "code", "WEBHOOK_SERVICE_UNAVAILABLE",
                "message", "Webhook processing service is temporarily unavailable. Webhook will be retried automatically.",
                "timestamp", Instant.now().toString(),
                "fallback", true,
                "retryAfter", "120" // 2 minutes
            )
        );
        
        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response));
    }
    
    @RequestMapping("/fallback/auth")
    public Mono<ResponseEntity<Map<String, Object>>> authFallback() {
        Map<String, Object> response = Map.of(
            "error", Map.of(
                "code", "AUTH_SERVICE_UNAVAILABLE",
                "message", "Authentication service is temporarily unavailable. Please try again later.",
                "timestamp", Instant.now().toString(),
                "fallback", true,
                "retryAfter", "30" // 30 seconds
            )
        );
        
        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response));
    }
}