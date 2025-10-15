package com.payments.routing.service;

import com.payments.routing.domain.RoutingRuleStatus;
import com.payments.routing.engine.RoutingDecision;
import com.payments.routing.engine.RoutingDecisionEngine;
import com.payments.routing.engine.RoutingRequest;
import com.payments.routing.repository.RoutingRuleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Routing Service
 * 
 * Core business logic for payment routing:
 * - Route payments to appropriate clearing systems
 * - Cache routing decisions for performance
 * - Provide routing statistics
 * - Handle fallback scenarios
 * 
 * Performance: Redis caching, DSA-optimized data structures
 * Resilience: Istio for internal calls (not Resilience4j)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RoutingService {

    private final RoutingRuleRepository ruleRepository;
    private final RoutingDecisionEngine decisionEngine;
    private final RoutingCacheService cacheService;

    // Statistics tracking using DSA-optimized data structures
    private final ConcurrentHashMap<String, AtomicLong> statistics = new ConcurrentHashMap<>();
    private final AtomicLong totalDecisions = new AtomicLong(0);
    private final AtomicLong cacheHits = new AtomicLong(0);
    private final AtomicLong cacheMisses = new AtomicLong(0);

    /**
     * Get routing decision for payment
     * 
     * @param request Routing request
     * @return Routing decision
     */
    @Transactional(readOnly = true)
    public RoutingDecision getRoutingDecision(RoutingRequest request) {
        log.debug("Getting routing decision for payment: {}", request.getPaymentId());

        // Check cache first
        String cacheKey = buildCacheKey(request);
        RoutingDecision cachedDecision = cacheService.get(cacheKey);
        if (cachedDecision != null) {
            cacheHits.incrementAndGet();
            log.debug("Cache hit for payment: {}", request.getPaymentId());
            return cachedDecision;
        }

        // Cache miss - evaluate rules
        cacheMisses.incrementAndGet();
        RoutingDecision decision = decisionEngine.evaluate(request);
        
        // Cache the decision
        cacheService.put(cacheKey, decision);
        
        // Update statistics
        totalDecisions.incrementAndGet();
        statistics.computeIfAbsent(decision.getClearingSystem(), k -> new AtomicLong(0)).incrementAndGet();
        
        log.info("Routing decision made for payment: {} - clearing system: {}, priority: {}", 
                request.getPaymentId(), decision.getClearingSystem(), decision.getPriority());

        return decision;
    }

    /**
     * Get routing decision with fallback clearing system
     * 
     * @param request Routing request
     * @param fallbackClearingSystem Fallback clearing system
     * @return Routing decision
     */
    @Transactional(readOnly = true)
    public RoutingDecision getRoutingDecisionWithFallback(RoutingRequest request, String fallbackClearingSystem) {
        log.debug("Getting routing decision with fallback for payment: {}", request.getPaymentId());

        try {
            // Try normal routing first
            RoutingDecision decision = getRoutingDecision(request);
            
            // If no clearing system found, use fallback
            if (decision.getClearingSystem() == null || decision.getClearingSystem().isEmpty()) {
                log.warn("No clearing system found for payment: {}, using fallback: {}", 
                        request.getPaymentId(), fallbackClearingSystem);
                
                decision = RoutingDecision.builder()
                        .paymentId(request.getPaymentId())
                        .clearingSystem(fallbackClearingSystem)
                        .priority("NORMAL")
                        .decisionReason("Fallback clearing system used")
                        .fallback(true)
                        .build();
            }
            
            return decision;
            
        } catch (Exception e) {
            log.error("Error getting routing decision for payment: {}", request.getPaymentId(), e);
            
            // Return fallback decision
            return RoutingDecision.builder()
                    .paymentId(request.getPaymentId())
                    .clearingSystem(fallbackClearingSystem)
                    .priority("NORMAL")
                    .decisionReason("Error occurred, using fallback clearing system")
                    .fallback(true)
                    .build();
        }
    }

    /**
     * Get routing statistics
     * 
     * @return Routing statistics
     */
    @Transactional(readOnly = true)
    public RoutingStatistics getRoutingStatistics() {
        log.debug("Getting routing statistics");

        long totalRules = ruleRepository.count();
        long activeRules = ruleRepository.countByRuleStatus(RoutingRuleStatus.ACTIVE);
        long cacheSize = cacheService.size();

        return RoutingStatistics.builder()
                .totalRules(totalRules)
                .activeRules(activeRules)
                .cacheSize(cacheSize)
                .totalDecisions(totalDecisions.get())
                .cacheHits(cacheHits.get())
                .cacheMisses(cacheMisses.get())
                .timestamp(Instant.now().toString())
                .build();
    }

    /**
     * Clear routing cache for specific payment
     * 
     * @param paymentId Payment ID
     */
    @CacheEvict(value = "routingDecisions", key = "#paymentId")
    public void clearRoutingCache(String paymentId) {
        log.info("Clearing routing cache for payment: {}", paymentId);
        cacheService.evictByPaymentId(paymentId);
    }

    /**
     * Clear all routing cache
     */
    @CacheEvict(value = "routingDecisions", allEntries = true)
    public void clearAllRoutingCache() {
        log.info("Clearing all routing cache");
        cacheService.clear();
    }

    /**
     * Build cache key for routing request
     * 
     * @param request Routing request
     * @return Cache key
     */
    private String buildCacheKey(RoutingRequest request) {
        return String.format("routing:%s:%s:%s:%s:%s:%s", 
                request.getTenantId(),
                request.getBusinessUnitId(),
                request.getAmount(),
                request.getCurrency(),
                request.getPaymentType(),
                request.getPriority());
    }

    /**
     * Routing Statistics DTO
     */
    @lombok.Data
    @lombok.Builder
    public static class RoutingStatistics {
        private Long totalRules;
        private Long activeRules;
        private Long cacheSize;
        private Long totalDecisions;
        private Long cacheHits;
        private Long cacheMisses;
        private String timestamp;
        
        // Computed properties
        public Double getCacheHitRate() {
            if (totalDecisions == null || totalDecisions == 0) {
                return 0.0;
            }
            return cacheHits != null ? (double) cacheHits / totalDecisions : 0.0;
        }
        
        public Double getAverageDecisionTime() {
            // This would be computed from actual timing data
            return 0.0; // Placeholder
        }
    }
}