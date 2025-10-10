package com.paymentengine.paymentprocessing.service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Centralised service for executing downstream calls with shared resiliency policies.
 */
public interface ResiliencyConfigurationService {

    /**
     * Execute the supplied call applying the configured resiliency policies for the service.
     *
     * @param serviceName logical service name used to resolve resiliency configuration
     * @param tenantId    tenant context for metrics
     * @param primaryCall supplier executed under resiliency protection
     * @param fallback    fallback handler invoked when resiliency policies give up
     * @param <T>         result type
     * @return result from the primary call or fallback
     */
    <T> T executeResilientCall(String serviceName,
                               String tenantId,
                               Supplier<T> primaryCall,
                               Function<Throwable, T> fallback);

    /**
     * Execute an asynchronous call applying the configured resiliency policies.
     */
    <T> CompletableFuture<T> executeResilientCallAsync(String serviceName,
                                                       String tenantId,
                                                       Supplier<CompletableFuture<T>> primaryCall,
                                                       Function<Throwable, T> fallback);

    /**
     * Retrieve the latest recorded health for a service.
     */
    Optional<ServiceHealthStatus> getServiceHealth(String serviceName);

    /**
     * Retrieve the health state for all tracked services.
     */
    List<ServiceHealthStatus> getAllServiceHealth();

    /**
     * Health status model exposed to self-healing components.
     */
    class ServiceHealthStatus {
        public enum Status { HEALTHY, DEGRADED, UNAVAILABLE }

        private final String serviceName;
        private Status status;
        private String lastTenantId;
        private int consecutiveFailures;
        private Instant lastSuccessAt;
        private Instant lastFailureAt;
        private String lastErrorMessage;
        private Map<String, Object> metrics;

        public ServiceHealthStatus(String serviceName) {
            this.serviceName = serviceName;
            this.status = Status.HEALTHY;
        }

        public String getServiceName() {
            return serviceName;
        }

        public Status getStatus() {
            return status;
        }

        public void setStatus(Status status) {
            this.status = status;
        }

        public String getLastTenantId() {
            return lastTenantId;
        }

        public void setLastTenantId(String lastTenantId) {
            this.lastTenantId = lastTenantId;
        }

        public int getConsecutiveFailures() {
            return consecutiveFailures;
        }

        public void setConsecutiveFailures(int consecutiveFailures) {
            this.consecutiveFailures = consecutiveFailures;
        }

        public Instant getLastSuccessAt() {
            return lastSuccessAt;
        }

        public void setLastSuccessAt(Instant lastSuccessAt) {
            this.lastSuccessAt = lastSuccessAt;
        }

        public Instant getLastFailureAt() {
            return lastFailureAt;
        }

        public void setLastFailureAt(Instant lastFailureAt) {
            this.lastFailureAt = lastFailureAt;
        }

        public String getLastErrorMessage() {
            return lastErrorMessage;
        }

        public void setLastErrorMessage(String lastErrorMessage) {
            this.lastErrorMessage = lastErrorMessage;
        }

        public Map<String, Object> getMetrics() {
            return metrics;
        }

        public void setMetrics(Map<String, Object> metrics) {
            this.metrics = metrics;
        }
    }
}
