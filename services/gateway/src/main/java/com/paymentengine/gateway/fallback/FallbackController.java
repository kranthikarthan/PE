package com.paymentengine.gateway.fallback;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

/**
 * Fallback controller for circuit breaker failures
 */
@RestController
@RequestMapping("/fallback")
public class FallbackController {

    @GetMapping("/iso20022")
    public ResponseEntity<Map<String, Object>> iso20022Fallback() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of(
                        "error", "ISO 20022 service temporarily unavailable",
                        "message", "The ISO 20022 message processing service is currently experiencing issues. Please try again later.",
                        "status", "SERVICE_UNAVAILABLE",
                        "timestamp", Instant.now().toString(),
                        "retryAfter", 30,
                        "fallback", true
                ));
    }

    @GetMapping("/scheme")
    public ResponseEntity<Map<String, Object>> schemeFallback() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of(
                        "error", "Scheme configuration service temporarily unavailable",
                        "message", "The scheme configuration service is currently experiencing issues. Please try again later.",
                        "status", "SERVICE_UNAVAILABLE",
                        "timestamp", Instant.now().toString(),
                        "retryAfter", 30,
                        "fallback", true
                ));
    }

    @GetMapping("/clearing-system")
    public ResponseEntity<Map<String, Object>> clearingSystemFallback() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of(
                        "error", "Clearing system service temporarily unavailable",
                        "message", "The clearing system service is currently experiencing issues. Please try again later.",
                        "status", "SERVICE_UNAVAILABLE",
                        "timestamp", Instant.now().toString(),
                        "retryAfter", 30,
                        "fallback", true
                ));
    }
}