package com.payments.bankservafricaadapter.service;

import com.payments.bankservafricaadapter.domain.BankservAfricaAdapter;
import com.payments.bankservafricaadapter.domain.ClearingRoute;
import com.payments.bankservafricaadapter.domain.ClearingMessageLog;
import com.payments.bankservafricaadapter.exception.BankservAfricaAdapterNotFoundException;
import com.payments.bankservafricaadapter.exception.BankservAfricaAdapterAlreadyExistsException;
import com.payments.bankservafricaadapter.exception.BankservAfricaAdapterOperationException;
import com.payments.telemetry.TracingService;
import com.payments.bankservafricaadapter.repository.BankservAfricaAdapterRepository;
import com.payments.domain.shared.ClearingAdapterId;
import com.payments.domain.shared.ClearingMessageId;
import com.payments.domain.shared.ClearingRouteId;
import com.payments.domain.shared.TenantContext;
import com.payments.domain.clearing.ClearingNetwork;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Service for managing BankservAfrica adapter configurations
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BankservAfricaAdapterService {
    
    private final BankservAfricaAdapterRepository adapterRepository;
    private final TracingService tracingService;
    
    /**
     * Create a new BankservAfrica adapter
     */
    @CircuitBreaker(name = "bankservafrica-adapter", fallbackMethod = "createAdapterFallback")
    @Retry(name = "bankservafrica-adapter")
    @TimeLimiter(name = "bankservafrica-adapter")
    @Transactional
    public CompletableFuture<BankservAfricaAdapter> createAdapter(
            ClearingAdapterId adapterId,
            TenantContext tenantContext,
            String adapterName,
            ClearingNetwork network,
            String endpoint,
            String createdBy) {
        
        return CompletableFuture.supplyAsync(() -> {
            return tracingService.executeInSpan("bankservafrica.adapter.create", Map.of(
                "adapter.name", adapterName,
                "tenant.id", tenantContext.getTenantId(),
                "business.unit.id", tenantContext.getBusinessUnitId(),
                "network", network.toString()
            ), () -> {
                log.info("Creating BankservAfrica adapter: {} for tenant: {}", adapterName, tenantContext.getTenantId());
            
            // Check if adapter already exists
            Optional<BankservAfricaAdapter> existingAdapter = adapterRepository
                    .findByTenantIdAndAdapterName(tenantContext.getTenantId(), adapterName);
            
            if (existingAdapter.isPresent()) {
                throw new IllegalArgumentException("Adapter with name " + adapterName + " already exists for tenant " + tenantContext.getTenantId());
            }
            
            BankservAfricaAdapter adapter = BankservAfricaAdapter.create(
                    adapterId, tenantContext, adapterName, network, endpoint, createdBy);
            
            BankservAfricaAdapter savedAdapter = adapterRepository.save(adapter);
            
            // Publish domain events
            savedAdapter.getDomainEvents().forEach(event -> {
                log.info("Publishing domain event: {} for adapter: {}", event.getEventType(), savedAdapter.getId());
                // TODO: Publish to event bus (Kafka/Azure Service Bus)
            });
            savedAdapter.clearDomainEvents();
            
            log.info("Successfully created BankservAfrica adapter: {} with ID: {}", adapterName, adapterId);
            
            return savedAdapter;
            });
        });
    }
    
    /**
     * Fallback method for createAdapter
     */
    public CompletableFuture<BankservAfricaAdapter> createAdapterFallback(
            ClearingAdapterId adapterId,
            TenantContext tenantContext,
            String adapterName,
            ClearingNetwork network,
            String endpoint,
            String createdBy,
            Exception ex) {
        log.error("Failed to create BankservAfrica adapter: {} - {}", adapterName, ex.getMessage());
        return CompletableFuture.failedFuture(new RuntimeException("Failed to create BankservAfrica adapter: " + ex.getMessage()));
    }
    
    /**
     * Update adapter configuration
     */
    @CircuitBreaker(name = "bankservafrica-adapter", fallbackMethod = "updateAdapterConfigurationFallback")
    @Retry(name = "bankservafrica-adapter")
    @TimeLimiter(name = "bankservafrica-adapter")
    @Transactional
    public CompletableFuture<BankservAfricaAdapter> updateAdapterConfiguration(
            ClearingAdapterId adapterId,
            String endpoint,
            String apiVersion,
            Integer timeoutSeconds,
            Integer retryAttempts,
            Boolean encryptionEnabled,
            Integer batchSize,
            String processingWindowStart,
            String processingWindowEnd,
            String updatedBy) {
        
        return CompletableFuture.supplyAsync(() -> {
            return tracingService.executeInSpan("bankservafrica.adapter.update", Map.of(
                "adapter.id", adapterId.toString(),
                "endpoint", endpoint != null ? endpoint : "null",
                "api.version", apiVersion != null ? apiVersion : "null",
                "timeout.seconds", timeoutSeconds != null ? timeoutSeconds.toString() : "null",
                "retry.attempts", retryAttempts != null ? retryAttempts.toString() : "null",
                "encryption.enabled", encryptionEnabled != null ? encryptionEnabled.toString() : "null",
                "batch.size", batchSize != null ? batchSize.toString() : "null"
            ), () -> {
                log.info("Updating BankservAfrica adapter configuration: {}", adapterId);
        
        BankservAfricaAdapter adapter = adapterRepository.findById(adapterId)
                .orElseThrow(() -> new IllegalArgumentException("Adapter not found: " + adapterId));
        
        adapter.updateConfiguration(
                endpoint, apiVersion, timeoutSeconds, retryAttempts, 
                encryptionEnabled, batchSize, processingWindowStart, 
                processingWindowEnd, updatedBy);
        
        BankservAfricaAdapter updatedAdapter = adapterRepository.save(adapter);
        
        // Apply configuration changes to resilience patterns
        applyConfigurationToResiliencePatterns(updatedAdapter);
        
        // Publish domain events
        updatedAdapter.getDomainEvents().forEach(event -> {
            log.info("Publishing domain event: {} for adapter: {}", event.getEventType(), updatedAdapter.getId());
            // TODO: Publish to event bus (Kafka/Azure Service Bus)
        });
        updatedAdapter.clearDomainEvents();
        
        log.info("Successfully updated BankservAfrica adapter configuration: {} with timeout: {}s, retries: {}, encryption: {}, api: {}", 
                 adapterId, updatedAdapter.getTimeoutSeconds(), updatedAdapter.getRetryAttempts(), 
                 updatedAdapter.getEncryptionEnabled(), updatedAdapter.getApiVersion());
        
        return updatedAdapter;
            });
        });
    }
    
    /**
     * Add a route to the BankservAfrica adapter
     * 
     * @param adapterId The adapter ID
     * @param routeName The route name
     * @param source The source endpoint
     * @param destination The destination endpoint
     * @param priority The route priority
     * @param addedBy The user adding the route
     * @return The updated adapter
     */
    @Transactional
    public BankservAfricaAdapter addRoute(
            ClearingAdapterId adapterId,
            String routeName,
            String source,
            String destination,
            Integer priority,
            String addedBy) {
        
        return tracingService.executeInSpan("bankservafrica.adapter.addRoute", Map.of(
            "adapter.id", adapterId.toString(),
            "route.name", routeName,
            "source", source,
            "destination", destination,
            "priority", priority != null ? priority.toString() : "null",
            "added.by", addedBy
        ), () -> {
            log.info("Adding route to BankservAfrica adapter: {} - route: {} from {} to {}", 
                     adapterId, routeName, source, destination);
        
        BankservAfricaAdapter adapter = adapterRepository.findById(adapterId)
                .orElseThrow(() -> new BankservAfricaAdapterNotFoundException(adapterId));
        
        adapter.addRoute(
                ClearingRouteId.generate(),
                routeName,
                source,
                destination,
                priority,
                addedBy);
        
        BankservAfricaAdapter updatedAdapter = adapterRepository.save(adapter);
        
        // Publish domain events
        updatedAdapter.getDomainEvents().forEach(event -> {
            log.info("Publishing domain event: {} for adapter: {}", event.getEventType(), updatedAdapter.getId());
            // TODO: Publish to event bus (Kafka/Azure Service Bus)
        });
        updatedAdapter.clearDomainEvents();
        
        log.info("Successfully added route to BankservAfrica adapter: {} - route: {}", adapterId, routeName);
        return updatedAdapter;
        });
    }
    
    /**
     * Get all routes for the BankservAfrica adapter
     * 
     * @param adapterId The adapter ID
     * @return List of routes
     */
    public List<ClearingRoute> getRoutes(ClearingAdapterId adapterId) {
        log.info("Getting routes for BankservAfrica adapter: {}", adapterId);
        
        BankservAfricaAdapter adapter = adapterRepository.findById(adapterId)
                .orElseThrow(() -> new BankservAfricaAdapterNotFoundException(adapterId));
        
        List<ClearingRoute> routes = adapter.getRoutes();
        log.info("Found {} routes for BankservAfrica adapter: {}", routes.size(), adapterId);
        
        return routes;
    }
    
    /**
     * Log a message for the BankservAfrica adapter
     * 
     * @param adapterId The adapter ID
     * @param direction The message direction (INBOUND/OUTBOUND)
     * @param messageType The message type (e.g., EFT, ISO8583, ACH)
     * @param payloadHash The payload hash
     * @param statusCode The status code
     * @return The updated adapter
     */
    @Transactional
    public BankservAfricaAdapter logMessage(
            ClearingAdapterId adapterId,
            String direction,
            String messageType,
            String payloadHash,
            Integer statusCode) {
        
        return tracingService.executeInSpan("bankservafrica.adapter.logMessage", Map.of(
            "adapter.id", adapterId.toString(),
            "direction", direction,
            "message.type", messageType,
            "payload.hash", payloadHash != null ? payloadHash : "null",
            "status.code", statusCode != null ? statusCode.toString() : "null"
        ), () -> {
            log.info("Logging message for BankservAfrica adapter: {} - direction: {}, type: {}, status: {}", 
                     adapterId, direction, messageType, statusCode);
        
        BankservAfricaAdapter adapter = adapterRepository.findById(adapterId)
                .orElseThrow(() -> new BankservAfricaAdapterNotFoundException(adapterId));
        
        adapter.logMessage(
                ClearingMessageId.generate(),
                direction,
                messageType,
                payloadHash,
                statusCode);
        
        BankservAfricaAdapter updatedAdapter = adapterRepository.save(adapter);
        
        // Publish domain events
        updatedAdapter.getDomainEvents().forEach(event -> {
            log.info("Publishing domain event: {} for adapter: {}", event.getEventType(), updatedAdapter.getId());
            // TODO: Publish to event bus (Kafka/Azure Service Bus)
        });
        updatedAdapter.clearDomainEvents();
        
        log.info("Successfully logged message for BankservAfrica adapter: {} - direction: {}, type: {}", 
                 adapterId, direction, messageType);
        return updatedAdapter;
        });
    }
    
    /**
     * Get all message logs for the BankservAfrica adapter
     * 
     * @param adapterId The adapter ID
     * @return List of message logs
     */
    public List<ClearingMessageLog> getMessageLogs(ClearingAdapterId adapterId) {
        log.info("Getting message logs for BankservAfrica adapter: {}", adapterId);
        
        BankservAfricaAdapter adapter = adapterRepository.findById(adapterId)
                .orElseThrow(() -> new BankservAfricaAdapterNotFoundException(adapterId));
        
        List<ClearingMessageLog> messageLogs = adapter.getMessageLogs();
        log.info("Found {} message logs for BankservAfrica adapter: {}", messageLogs.size(), adapterId);
        
        return messageLogs;
    }

    /**
     * Apply adapter configuration to resilience patterns
     * 
     * @param adapter The adapter with updated configuration
     */
    private void applyConfigurationToResiliencePatterns(BankservAfricaAdapter adapter) {
        log.info("Applying configuration to resilience patterns for adapter: {} - timeout: {}s, retries: {}, encryption: {}", 
                 adapter.getId(), adapter.getTimeoutSeconds(), adapter.getRetryAttempts(), adapter.getEncryptionEnabled());
        
        // Log configuration changes for monitoring
        if (adapter.getEncryptionEnabled()) {
            log.info("Encryption enabled for BankservAfrica adapter: {} - API version: {}", adapter.getId(), adapter.getApiVersion());
        } else {
            log.warn("Encryption disabled for BankservAfrica adapter: {} - API version: {}", adapter.getId(), adapter.getApiVersion());
        }
        
        // Log batch processing configuration
        log.info("Batch processing configuration for adapter: {} - batch size: {}, window: {} to {}", 
                 adapter.getId(), adapter.getBatchSize(), adapter.getProcessingWindowStart(), adapter.getProcessingWindowEnd());
    }
    
    /**
     * Activate adapter
     */
    @Transactional
    public BankservAfricaAdapter activateAdapter(ClearingAdapterId adapterId, String activatedBy) {
        log.info("Activating BankservAfrica adapter: {}", adapterId);
        
        BankservAfricaAdapter adapter = adapterRepository.findById(adapterId)
                .orElseThrow(() -> new IllegalArgumentException("Adapter not found: " + adapterId));
        
        adapter.activate(activatedBy);
        
        BankservAfricaAdapter activatedAdapter = adapterRepository.save(adapter);
        
        // Publish domain events
        activatedAdapter.getDomainEvents().forEach(event -> {
            log.info("Publishing domain event: {} for adapter: {}", event.getEventType(), activatedAdapter.getId());
            // TODO: Publish to event bus (Kafka/Azure Service Bus)
        });
        activatedAdapter.clearDomainEvents();
        
        log.info("Successfully activated BankservAfrica adapter: {}", adapterId);
        
        return activatedAdapter;
    }
    
    /**
     * Deactivate adapter
     */
    @Transactional
    public BankservAfricaAdapter deactivateAdapter(ClearingAdapterId adapterId, String reason, String deactivatedBy) {
        log.info("Deactivating BankservAfrica adapter: {} with reason: {}", adapterId, reason);
        
        BankservAfricaAdapter adapter = adapterRepository.findById(adapterId)
                .orElseThrow(() -> new IllegalArgumentException("Adapter not found: " + adapterId));
        
        adapter.deactivate(reason, deactivatedBy);
        
        BankservAfricaAdapter deactivatedAdapter = adapterRepository.save(adapter);
        
        // Publish domain events
        deactivatedAdapter.getDomainEvents().forEach(event -> {
            log.info("Publishing domain event: {} for adapter: {}", event.getEventType(), deactivatedAdapter.getId());
            // TODO: Publish to event bus (Kafka/Azure Service Bus)
        });
        deactivatedAdapter.clearDomainEvents();
        
        log.info("Successfully deactivated BankservAfrica adapter: {}", adapterId);
        
        return deactivatedAdapter;
    }
    
    /**
     * Find adapter by ID
     */
    public Optional<BankservAfricaAdapter> findById(ClearingAdapterId adapterId) {
        return adapterRepository.findById(adapterId);
    }
    
    /**
     * Find adapter by tenant ID and adapter name
     */
    public Optional<BankservAfricaAdapter> findByTenantIdAndAdapterName(String tenantId, String adapterName) {
        return adapterRepository.findByTenantIdAndAdapterName(tenantId, adapterName);
    }
    
    /**
     * Find all adapters by tenant ID
     */
    public List<BankservAfricaAdapter> findByTenantId(String tenantId) {
        return adapterRepository.findByTenantId(tenantId);
    }
    
    /**
     * Find all adapters by tenant ID and business unit
     */
    public List<BankservAfricaAdapter> findByTenantIdAndBusinessUnitId(String tenantId, String businessUnitId) {
        return adapterRepository.findByTenantIdAndBusinessUnitId(tenantId, businessUnitId);
    }
    
    /**
     * Find active adapters by tenant ID
     */
    public List<BankservAfricaAdapter> findActiveByTenantId(String tenantId) {
        return adapterRepository.findActiveByTenantId(tenantId);
    }
    
    /**
     * Find adapters by network
     */
    public List<BankservAfricaAdapter> findByNetwork(String network) {
        return adapterRepository.findByNetwork(network);
    }
    
    /**
     * Find adapters by status
     */
    public List<BankservAfricaAdapter> findByStatus(String status) {
        return adapterRepository.findByStatus(status);
    }

    /** Get total adapter count */
    public long getAdapterCount() {
        return tracingService.executeInSpan(
            "bankservafrica.adapter.getAdapterCount",
            Map.of("operation", "getAdapterCount"),
            () -> {
                log.debug("Getting total adapter count");
                return adapterRepository.count();
            });
    }

    /** Get active adapter count */
    public long getActiveAdapterCount() {
        return tracingService.executeInSpan(
            "bankservafrica.adapter.getActiveAdapterCount",
            Map.of("operation", "getActiveAdapterCount"),
            () -> {
                log.debug("Getting active adapter count");
                return adapterRepository.countByStatus(com.payments.domain.clearing.AdapterOperationalStatus.ACTIVE);
            });
    }
}
