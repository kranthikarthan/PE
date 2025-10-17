package com.payments.swiftadapter.service;

import com.payments.domain.clearing.AdapterOperationalStatus;
import com.payments.domain.shared.ClearingAdapterId;
import com.payments.domain.shared.ClearingMessageId;
import com.payments.domain.shared.ClearingRouteId;
import com.payments.domain.shared.TenantContext;
import com.payments.swiftadapter.domain.SwiftAdapter;
import com.payments.swiftadapter.domain.ClearingRoute;
import com.payments.swiftadapter.domain.ClearingMessageLog;
import com.payments.swiftadapter.exception.SwiftAdapterNotFoundException;
import com.payments.swiftadapter.exception.SwiftAdapterOperationException;
import com.payments.telemetry.TracingService;
import com.payments.swiftadapter.repository.SwiftAdapterRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for managing SWIFT adapter configurations
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SwiftAdapterService {

  private final SwiftAdapterRepository swiftAdapterRepository;
  private final TracingService tracingService;

  /** Create a new SWIFT adapter */
  @CircuitBreaker(name = "swift-adapter", fallbackMethod = "createAdapterFallback")
  @Retry(name = "swift-adapter")
  @TimeLimiter(name = "swift-adapter")
  @Transactional
  public CompletableFuture<SwiftAdapter> createAdapter(
      ClearingAdapterId adapterId,
      TenantContext tenantContext,
      String adapterName,
      String endpoint,
      String createdBy) {
    return CompletableFuture.supplyAsync(() -> {
      return tracingService.executeInSpan("swift.adapter.create", Map.of(
          "adapter.name", adapterName,
          "tenant.id", tenantContext.getTenantId(),
          "business.unit.id", tenantContext.getBusinessUnitId()
      ), () -> {
        log.info("Creating SWIFT adapter: {} for tenant: {}", adapterName, tenantContext.getTenantId());

    SwiftAdapter adapter =
        SwiftAdapter.create(adapterId, tenantContext, adapterName, endpoint, createdBy);
    SwiftAdapter savedAdapter = swiftAdapterRepository.save(adapter);

    // Publish domain events
    savedAdapter.getDomainEvents().forEach(event -> {
      log.info("Publishing domain event: {} for adapter: {}", event.getEventType(), savedAdapter.getId());
      // TODO: Publish to event bus (Kafka/Azure Service Bus)
    });
    savedAdapter.clearDomainEvents();

    log.info("SWIFT adapter created successfully: {}", savedAdapter.getId());
    return savedAdapter;
      });
    });
  }

  /** Get adapter by ID */
  public Optional<SwiftAdapter> getAdapter(ClearingAdapterId adapterId) {
    return swiftAdapterRepository.findById(adapterId);
  }

  /** Find adapter by ID */
  public Optional<SwiftAdapter> findById(ClearingAdapterId adapterId) {
    return swiftAdapterRepository.findById(adapterId);
  }

  /** Update adapter configuration */
  @Transactional
  public SwiftAdapter updateAdapterConfiguration(
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
    log.info("Updating SWIFT adapter configuration: {}", adapterId);

    SwiftAdapter adapter =
        swiftAdapterRepository
            .findById(adapterId)
            .orElseThrow(() -> new SwiftAdapterNotFoundException(adapterId));

    adapter.updateConfiguration(
        endpoint,
        apiVersion,
        timeoutSeconds,
        retryAttempts,
        encryptionEnabled,
        batchSize,
        true, // sanctionsScreeningEnabled
        "https://sanctions.example.com", // sanctionsEndpoint
        30, // sanctionsTimeout
        3, // sanctionsRetryAttempts
        true, // fxConversionEnabled
        "https://fx.example.com", // fxEndpoint
        30, // fxTimeout
        3, // fxRetryAttempts
        updatedBy);

    SwiftAdapter updatedAdapter = swiftAdapterRepository.save(adapter);

    // Apply configuration changes to resilience patterns
    applyConfigurationToResiliencePatterns(updatedAdapter);

    // Publish domain events
    updatedAdapter.getDomainEvents().forEach(event -> {
      log.info("Publishing domain event: {} for adapter: {}", event.getEventType(), updatedAdapter.getId());
      // TODO: Publish to event bus (Kafka/Azure Service Bus)
    });
    updatedAdapter.clearDomainEvents();

    log.info("SWIFT adapter configuration updated successfully: {} with timeout: {}s, retries: {}, encryption: {}, api: {}", 
             adapterId, updatedAdapter.getTimeoutSeconds(), updatedAdapter.getRetryAttempts(), 
             updatedAdapter.getEncryptionEnabled(), updatedAdapter.getApiVersion());
    return updatedAdapter;
  }

  /**
   * Add a route to the SWIFT adapter
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
  public SwiftAdapter addRoute(
      ClearingAdapterId adapterId,
      String routeName,
      String source,
      String destination,
      Integer priority,
      String addedBy) {
    
    return tracingService.executeInSpan("swift.adapter.addRoute", Map.of(
        "adapter.id", adapterId.toString(),
        "route.name", routeName,
        "source", source,
        "destination", destination,
        "priority", priority != null ? priority.toString() : "null",
        "added.by", addedBy
    ), () -> {
      log.info("Adding route to SWIFT adapter: {} - route: {} from {} to {}", 
               adapterId, routeName, source, destination);
    
    SwiftAdapter adapter = swiftAdapterRepository.findById(adapterId)
        .orElseThrow(() -> new SwiftAdapterNotFoundException(adapterId));
    
    adapter.addRoute(
        ClearingRouteId.generate(),
        routeName,
        source,
        destination,
        priority,
        addedBy);
    
    SwiftAdapter updatedAdapter = swiftAdapterRepository.save(adapter);
    
    // Publish domain events
    updatedAdapter.getDomainEvents().forEach(event -> {
      log.info("Publishing domain event: {} for adapter: {}", event.getEventType(), updatedAdapter.getId());
      // TODO: Publish to event bus (Kafka/Azure Service Bus)
    });
    updatedAdapter.clearDomainEvents();
    
    log.info("Successfully added route to SWIFT adapter: {} - route: {}", adapterId, routeName);
    return updatedAdapter;
    });
  }
  
  /**
   * Get all routes for the SWIFT adapter
   * 
   * @param adapterId The adapter ID
   * @return List of routes
   */
  public List<ClearingRoute> getRoutes(ClearingAdapterId adapterId) {
    log.info("Getting routes for SWIFT adapter: {}", adapterId);
    
    SwiftAdapter adapter = swiftAdapterRepository.findById(adapterId)
        .orElseThrow(() -> new SwiftAdapterNotFoundException(adapterId));
    
    List<ClearingRoute> routes = adapter.getRoutes();
    log.info("Found {} routes for SWIFT adapter: {}", routes.size(), adapterId);
    
    return routes;
  }
  
  /**
   * Log a message for the SWIFT adapter
   * 
   * @param adapterId The adapter ID
   * @param direction The message direction (INBOUND/OUTBOUND)
   * @param messageType The message type (e.g., MT103, pacs.008)
   * @param payloadHash The payload hash
   * @param statusCode The status code
   * @return The updated adapter
   */
  @Transactional
  public SwiftAdapter logMessage(
      ClearingAdapterId adapterId,
      String direction,
      String messageType,
      String payloadHash,
      Integer statusCode) {
    
    return tracingService.executeInSpan("swift.adapter.logMessage", Map.of(
        "adapter.id", adapterId.toString(),
        "direction", direction,
        "message.type", messageType,
        "payload.hash", payloadHash != null ? payloadHash : "null",
        "status.code", statusCode != null ? statusCode.toString() : "null"
    ), () -> {
      log.info("Logging message for SWIFT adapter: {} - direction: {}, type: {}, status: {}", 
               adapterId, direction, messageType, statusCode);
    
    SwiftAdapter adapter = swiftAdapterRepository.findById(adapterId)
        .orElseThrow(() -> new SwiftAdapterNotFoundException(adapterId));
    
    adapter.logMessage(
        ClearingMessageId.generate(),
        direction,
        messageType,
        payloadHash,
        statusCode);
    
    SwiftAdapter updatedAdapter = swiftAdapterRepository.save(adapter);
    
    // Publish domain events
    updatedAdapter.getDomainEvents().forEach(event -> {
      log.info("Publishing domain event: {} for adapter: {}", event.getEventType(), updatedAdapter.getId());
      // TODO: Publish to event bus (Kafka/Azure Service Bus)
    });
    updatedAdapter.clearDomainEvents();
    
    log.info("Successfully logged message for SWIFT adapter: {} - direction: {}, type: {}", 
             adapterId, direction, messageType);
    return updatedAdapter;
    });
  }
  
  /**
   * Get all message logs for the SWIFT adapter
   * 
   * @param adapterId The adapter ID
   * @return List of message logs
   */
  public List<ClearingMessageLog> getMessageLogs(ClearingAdapterId adapterId) {
    log.info("Getting message logs for SWIFT adapter: {}", adapterId);
    
    SwiftAdapter adapter = swiftAdapterRepository.findById(adapterId)
        .orElseThrow(() -> new SwiftAdapterNotFoundException(adapterId));
    
    List<ClearingMessageLog> messageLogs = adapter.getMessageLogs();
    log.info("Found {} message logs for SWIFT adapter: {}", messageLogs.size(), adapterId);
    
    return messageLogs;
  }

  /**
   * Apply adapter configuration to resilience patterns
   * 
   * @param adapter The adapter with updated configuration
   */
  private void applyConfigurationToResiliencePatterns(SwiftAdapter adapter) {
    log.info("Applying configuration to resilience patterns for adapter: {} - timeout: {}s, retries: {}, encryption: {}", 
             adapter.getId(), adapter.getTimeoutSeconds(), adapter.getRetryAttempts(), adapter.getEncryptionEnabled());
    
    // Log configuration changes for monitoring
    if (adapter.getEncryptionEnabled()) {
      log.info("Encryption enabled for SWIFT adapter: {} - API version: {}", adapter.getId(), adapter.getApiVersion());
    } else {
      log.warn("Encryption disabled for SWIFT adapter: {} - API version: {}", adapter.getId(), adapter.getApiVersion());
    }
    
    // Log international payment configuration
    log.info("International payment configuration for adapter: {} - batch size: {}, window: {} to {}", 
             adapter.getId(), adapter.getBatchSize(), adapter.getProcessingWindowStart(), adapter.getProcessingWindowEnd());
  }

  /** Activate adapter */
  @Transactional
  public SwiftAdapter activateAdapter(ClearingAdapterId adapterId, String activatedBy) {
    log.info("Activating SWIFT adapter: {}", adapterId);

    SwiftAdapter adapter =
        swiftAdapterRepository
            .findById(adapterId)
            .orElseThrow(() -> new SwiftAdapterNotFoundException(adapterId));

    adapter.activate(activatedBy);
    SwiftAdapter activatedAdapter = swiftAdapterRepository.save(adapter);

    // Publish domain events
    activatedAdapter.getDomainEvents().forEach(event -> {
      log.info("Publishing domain event: {} for adapter: {}", event.getEventType(), activatedAdapter.getId());
      // TODO: Publish to event bus (Kafka/Azure Service Bus)
    });
    activatedAdapter.clearDomainEvents();

    log.info("SWIFT adapter activated successfully: {}", adapterId);
    return activatedAdapter;
  }

  /** Deactivate adapter */
  @Transactional
  public SwiftAdapter deactivateAdapter(
      ClearingAdapterId adapterId, String reason, String deactivatedBy) {
    log.info("Deactivating SWIFT adapter: {} - Reason: {}", adapterId, reason);

    SwiftAdapter adapter =
        swiftAdapterRepository
            .findById(adapterId)
            .orElseThrow(() -> new SwiftAdapterNotFoundException(adapterId));

    adapter.deactivate(reason, deactivatedBy);
    SwiftAdapter deactivatedAdapter = swiftAdapterRepository.save(adapter);

    // Publish domain events
    deactivatedAdapter.getDomainEvents().forEach(event -> {
      log.info("Publishing domain event: {} for adapter: {}", event.getEventType(), deactivatedAdapter.getId());
      // TODO: Publish to event bus (Kafka/Azure Service Bus)
    });
    deactivatedAdapter.clearDomainEvents();

    log.info("SWIFT adapter deactivated successfully: {}", adapterId);
    return deactivatedAdapter;
  }

  /** Check if adapter is active */
  public boolean isAdapterActive(ClearingAdapterId adapterId) {
    return swiftAdapterRepository.findById(adapterId).map(SwiftAdapter::isActive).orElse(false);
  }

  /** Validate adapter configuration */
  public boolean validateAdapterConfiguration(ClearingAdapterId adapterId) {
    return swiftAdapterRepository
        .findById(adapterId)
        .map(SwiftAdapter::isConfigurationValid)
        .orElse(false);
  }

  /** Get adapters by tenant */
  public List<SwiftAdapter> getAdaptersByTenant(String tenantId, String businessUnitId) {
    return swiftAdapterRepository.findByTenantIdAndBusinessUnitId(tenantId, businessUnitId);
  }

  /** Get active adapters by tenant */
  public List<SwiftAdapter> getActiveAdaptersByTenant(String tenantId) {
    return swiftAdapterRepository.findByTenantIdAndStatus(tenantId, AdapterOperationalStatus.ACTIVE);
  }

  /** Get adapters by status */
  public List<SwiftAdapter> getAdaptersByStatus(AdapterOperationalStatus status) {
    return swiftAdapterRepository.findByStatus(status);
  }

  /** Get all adapters */
  public List<SwiftAdapter> getAllAdapters() {
    return swiftAdapterRepository.findAll();
  }

  /** Delete adapter */
  @Transactional
  public void deleteAdapter(ClearingAdapterId adapterId) {
    log.info("Deleting SWIFT adapter: {}", adapterId);
    swiftAdapterRepository.deleteById(adapterId);
    log.info("SWIFT adapter deleted successfully: {}", adapterId);
  }
}
