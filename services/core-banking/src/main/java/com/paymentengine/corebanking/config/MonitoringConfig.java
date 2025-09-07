package com.paymentengine.corebanking.config;

import io.micrometer.core.aop.TimedAspect;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.config.MeterFilter;
import io.micrometer.prometheus.PrometheusConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Monitoring Configuration for High TPS
 * 
 * Comprehensive monitoring setup for tracking performance metrics,
 * business metrics, and system health for 2000+ TPS operations.
 */
@Configuration
public class MonitoringConfig {
    
    /**
     * Prometheus Meter Registry for metrics collection
     */
    @Bean
    public PrometheusMeterRegistry prometheusMeterRegistry() {
        return new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);
    }
    
    /**
     * Timed Aspect for automatic method timing
     */
    @Bean
    public TimedAspect timedAspect(MeterRegistry meterRegistry) {
        return new TimedAspect(meterRegistry);
    }
    
    /**
     * Meter Filter for customizing metrics
     */
    @Bean
    public MeterFilter meterFilter() {
        return MeterFilter.acceptNameStartsWith("payment_engine");
    }
}