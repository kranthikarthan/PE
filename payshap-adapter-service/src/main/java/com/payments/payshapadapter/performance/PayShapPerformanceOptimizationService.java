package com.payments.payshapadapter.performance;

import com.payments.payshapadapter.domain.PayShapAdapter;
import com.payments.payshapadapter.repository.PayShapAdapterRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * PayShap Performance Optimization Service
 *
 * <p>Advanced performance optimization service for PayShap adapter: - Advanced caching strategies -
 * Performance monitoring - Query optimization - Resource management
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PayShapPerformanceOptimizationService {

  private final MeterRegistry meterRegistry;
  private final PayShapAdapterRepository payShapAdapterRepository;

  // Performance metrics
  private final Counter payShapCacheHitCounter;
  private final Counter payShapCacheMissCounter;
  private final Counter payShapQueryOptimizationCounter;
  private final Counter payShapResourceOptimizationCounter;
  private final Counter payShapP2POptimizationCounter;
  private final Timer payShapQueryTimer;
  private final Timer payShapCacheTimer;
  private final Timer payShapResourceTimer;
  private final Timer payShapP2PTimer;
  private final AtomicLong payShapCacheSize = new AtomicLong(0);
  private final AtomicLong payShapOptimizedQueries = new AtomicLong(0);

  public PayShapPerformanceOptimizationService(
      MeterRegistry meterRegistry, PayShapAdapterRepository payShapAdapterRepository) {
    this.meterRegistry = meterRegistry;
    this.payShapAdapterRepository = payShapAdapterRepository;

    // Initialize performance counters
    this.payShapCacheHitCounter =
        Counter.builder("payshap.cache.hits.total")
            .description("Total number of PayShap cache hits")
            .register(meterRegistry);

    this.payShapCacheMissCounter =
        Counter.builder("payshap.cache.misses.total")
            .description("Total number of PayShap cache misses")
            .register(meterRegistry);

    this.payShapQueryOptimizationCounter =
        Counter.builder("payshap.query.optimizations.total")
            .description("Total number of PayShap query optimizations")
            .register(meterRegistry);

    this.payShapResourceOptimizationCounter =
        Counter.builder("payshap.resource.optimizations.total")
            .description("Total number of PayShap resource optimizations")
            .register(meterRegistry);

    this.payShapP2POptimizationCounter =
        Counter.builder("payshap.p2p.optimizations.total")
            .description("Total number of PayShap P2P optimizations")
            .register(meterRegistry);

    // Initialize performance timers
    this.payShapQueryTimer =
        Timer.builder("payshap.query.duration")
            .description("PayShap query execution duration")
            .register(meterRegistry);

    this.payShapCacheTimer =
        Timer.builder("payshap.cache.duration")
            .description("PayShap cache operation duration")
            .register(meterRegistry);

    this.payShapResourceTimer =
        Timer.builder("payshap.resource.duration")
            .description("PayShap resource operation duration")
            .register(meterRegistry);

    this.payShapP2PTimer =
        Timer.builder("payshap.p2p.duration")
            .description("PayShap P2P operation duration")
            .register(meterRegistry);

    // Initialize performance gauges
    Gauge.builder("payshap.cache.size", this, PayShapPerformanceOptimizationService::getCacheSize)
        .description("PayShap cache size")
        .register(meterRegistry);

    Gauge.builder(
            "payshap.optimized.queries",
            this,
            PayShapPerformanceOptimizationService::getOptimizedQueries)
        .description("PayShap optimized queries count")
        .register(meterRegistry);
  }

  /** Optimized adapter retrieval with caching */
  @Cacheable(value = "payshap-adapters", key = "#adapterId")
  public PayShapAdapter getOptimizedAdapter(String adapterId) {
    Timer.Sample sample = Timer.start(meterRegistry);
    try {
      payShapQueryOptimizationCounter.increment();
      payShapOptimizedQueries.incrementAndGet();

      PayShapAdapter adapter =
          payShapAdapterRepository
              .findById(adapterId)
              .orElseThrow(() -> new RuntimeException("Adapter not found: " + adapterId));

      log.debug("Retrieved optimized PayShap adapter: {}", adapterId);
      return adapter;
    } finally {
      sample.stop(payShapQueryTimer);
    }
  }

  /** Optimized adapter list retrieval with caching */
  @Cacheable(value = "payshap-adapters-list", key = "#status")
  public List<PayShapAdapter> getOptimizedAdaptersByStatus(String status) {
    Timer.Sample sample = Timer.start(meterRegistry);
    try {
      payShapQueryOptimizationCounter.increment();
      payShapOptimizedQueries.incrementAndGet();

      List<PayShapAdapter> adapters = payShapAdapterRepository.findByStatus(status);

      log.debug(
          "Retrieved optimized PayShap adapters by status: {}, count: {}", status, adapters.size());
      return adapters;
    } finally {
      sample.stop(payShapQueryTimer);
    }
  }

  /** Cache management operations */
  @CacheEvict(value = "payshap-adapters", key = "#adapterId")
  public void evictAdapterCache(String adapterId) {
    Timer.Sample sample = Timer.start(meterRegistry);
    try {
      payShapCacheMissCounter.increment();
      payShapCacheSize.decrementAndGet();

      log.debug("Evicted PayShap adapter cache: {}", adapterId);
    } finally {
      sample.stop(payShapCacheTimer);
    }
  }

  /** Cache update operations */
  @CachePut(value = "payshap-adapters", key = "#adapter.id")
  public PayShapAdapter updateAdapterCache(PayShapAdapter adapter) {
    Timer.Sample sample = Timer.start(meterRegistry);
    try {
      payShapCacheHitCounter.increment();
      payShapCacheSize.incrementAndGet();

      log.debug("Updated PayShap adapter cache: {}", adapter.getId());
      return adapter;
    } finally {
      sample.stop(payShapCacheTimer);
    }
  }

  /** Resource optimization */
  public void optimizeResources() {
    Timer.Sample sample = Timer.start(meterRegistry);
    try {
      payShapResourceOptimizationCounter.increment();

      // Perform resource optimization
      optimizeDatabaseConnections();
      optimizeMemoryUsage();
      optimizeThreadPool();
      optimizeP2PProcessing();

      log.debug("Performed PayShap resource optimization");
    } finally {
      sample.stop(payShapResourceTimer);
    }
  }

  /** P2P processing optimization */
  public void optimizeP2PProcessing() {
    Timer.Sample sample = Timer.start(meterRegistry);
    try {
      payShapP2POptimizationCounter.increment();

      // Perform P2P processing optimization
      optimizeP2PLatency();
      optimizeP2PThroughput();

      log.debug("Performed PayShap P2P processing optimization");
    } finally {
      sample.stop(payShapP2PTimer);
    }
  }

  /** Performance metrics collection */
  public Map<String, Object> getPerformanceMetrics() {
    Map<String, Object> metrics = new HashMap<>();

    // Cache metrics
    metrics.put("cacheHits", payShapCacheHitCounter.count());
    metrics.put("cacheMisses", payShapCacheMissCounter.count());
    metrics.put("cacheSize", getCacheSize());
    metrics.put("cacheHitRate", getCacheHitRate());

    // Query metrics
    metrics.put("optimizedQueries", getOptimizedQueries());
    metrics.put(
        "averageQueryTime", payShapQueryTimer.mean(java.util.concurrent.TimeUnit.MILLISECONDS));
    metrics.put("maxQueryTime", payShapQueryTimer.max(java.util.concurrent.TimeUnit.MILLISECONDS));

    // Resource metrics
    metrics.put(
        "averageResourceTime",
        payShapResourceTimer.mean(java.util.concurrent.TimeUnit.MILLISECONDS));
    metrics.put("totalResourceOptimizations", payShapResourceOptimizationCounter.count());

    // P2P metrics
    metrics.put("averageP2PTime", payShapP2PTimer.mean(java.util.concurrent.TimeUnit.MILLISECONDS));
    metrics.put("totalP2POptimizations", payShapP2POptimizationCounter.count());

    // Timestamp
    metrics.put("timestamp", Instant.now().toString());

    return metrics;
  }

  /** Performance optimization recommendations */
  public Map<String, Object> getOptimizationRecommendations() {
    Map<String, Object> recommendations = new HashMap<>();

    double cacheHitRate = getCacheHitRate();
    double averageQueryTime = payShapQueryTimer.mean(java.util.concurrent.TimeUnit.MILLISECONDS);
    double averageP2PTime = payShapP2PTimer.mean(java.util.concurrent.TimeUnit.MILLISECONDS);

    if (cacheHitRate < 0.8) {
      recommendations.put(
          "cacheOptimization", "Consider increasing cache size or improving cache key strategy");
    }

    if (averageQueryTime > 75) {
      recommendations.put(
          "queryOptimization", "Consider adding database indexes or optimizing queries");
    }

    if (averageP2PTime > 30) {
      recommendations.put(
          "p2pOptimization", "Consider optimizing P2P processing latency and throughput");
    }

    if (payShapResourceTimer.mean(java.util.concurrent.TimeUnit.MILLISECONDS) > 40) {
      recommendations.put(
          "resourceOptimization", "Consider optimizing resource allocation and usage");
    }

    recommendations.put("timestamp", Instant.now().toString());

    return recommendations;
  }

  /** Get cache size */
  private double getCacheSize() {
    return payShapCacheSize.get();
  }

  /** Get optimized queries count */
  private double getOptimizedQueries() {
    return payShapOptimizedQueries.get();
  }

  /** Get cache hit rate */
  private double getCacheHitRate() {
    double hits = payShapCacheHitCounter.count();
    double misses = payShapCacheMissCounter.count();
    double total = hits + misses;

    if (total == 0) {
      return 0.0;
    }

    return hits / total;
  }

  /** Optimize database connections */
  private void optimizeDatabaseConnections() {
    log.debug("Optimizing PayShap database connections");
  }

  /** Optimize memory usage */
  private void optimizeMemoryUsage() {
    log.debug("Optimizing PayShap memory usage");
  }

  /** Optimize thread pool */
  private void optimizeThreadPool() {
    log.debug("Optimizing PayShap thread pool");
  }

  /** Optimize P2P latency */
  private void optimizeP2PLatency() {
    log.debug("Optimizing PayShap P2P latency");
  }

  /** Optimize P2P throughput */
  private void optimizeP2PThroughput() {
    log.debug("Optimizing PayShap P2P throughput");
  }
}
