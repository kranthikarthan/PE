package com.payments.rtcadapter.service;

import com.payments.domain.clearing.AdapterOperationalStatus;
import com.payments.domain.shared.ClearingAdapterId;
import com.payments.domain.shared.ClearingMessageId;
import com.payments.domain.shared.ClearingRouteId;
import com.payments.domain.shared.TenantContext;
import com.payments.rtcadapter.domain.RtcAdapter;
import com.payments.rtcadapter.domain.ClearingRoute;
import com.payments.rtcadapter.domain.ClearingMessageLog;
import com.payments.rtcadapter.exception.RtcAdapterNotFoundException;
import com.payments.rtcadapter.exception.RtcAdapterOperationException;
import com.payments.telemetry.TracingService;
import com.payments.rtcadapter.repository.RtcAdapterRepository;
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

/** Service for managing RTC Adapter configurations */
@Service
@RequiredArgsConstructor
@Slf4j
public class RtcAdapterService {

  private final RtcAdapterRepository rtcAdapterRepository;
  private final TracingService tracingService;

  /** Create a new RTC adapter */
  @CircuitBreaker(name = "rtc-adapter", fallbackMethod = "createAdapterFallback")
  @Retry(name = "rtc-adapter")
  @TimeLimiter(name = "rtc-adapter")
  @Transactional
  public CompletableFuture<RtcAdapter> createAdapter(
      ClearingAdapterId adapterId,
      TenantContext tenantContext,
      String adapterName,
      String endpoint,
      String createdBy) {
    return CompletableFuture.supplyAsync(() -> {
      return tracingService.executeInSpan("rtc.adapter.create", Map.of(
          "adapter.name", adapterName,
          "tenant.id", tenantContext.getTenantId(),
          "business.unit.id", tenantContext.getBusinessUnitId()
      ), () -> {
        log.info("Creating RTC adapter: {} for tenant: {}", adapterName, tenantContext.getTenantId());

    RtcAdapter adapter =
        RtcAdapter.create(adapterId, tenantContext, adapterName, endpoint, createdBy);
    RtcAdapter savedAdapter = rtcAdapterRepository.save(adapter);

    // Publish domain events
    savedAdapter.getDomainEvents().forEach(event -> {
      log.info("Publishing domain event: {} for adapter: {}", event.getEventType(), savedAdapter.getId());
      // TODO: Publish to event bus (Kafka/Azure Service Bus)
    });
    savedAdapter.clearDomainEvents();

    log.info("RTC adapter created successfully: {}", savedAdapter.getId());
    return savedAdapter;
      });
    });
  }

  /** Get adapter by ID */
  public Optional<RtcAdapter> getAdapter(ClearingAdapterId adapterId) {
    return rtcAdapterRepository.findById(adapterId);
  }

  /** Get adapter by ID (alternative method name for consistency) */
  public Optional<RtcAdapter> findById(ClearingAdapterId adapterId) {
    return rtcAdapterRepository.findById(adapterId);
  }

  /** Update adapter configuration */
  @Transactional
  public RtcAdapter updateAdapterConfiguration(
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
    log.info("Updating RTC adapter configuration: {}", adapterId);

    RtcAdapter adapter =
        rtcAdapterRepository
            .findById(adapterId)
            .orElseThrow(() -> new RtcAdapterNotFoundException(adapterId));

    adapter.updateConfiguration(
        endpoint,
        apiVersion,
        timeoutSeconds,
        retryAttempts,
        encryptionEnabled,
        batchSize,
        processingWindowStart,
        processingWindowEnd,
        updatedBy);

    RtcAdapter updatedAdapter = rtcAdapterRepository.save(adapter);
    
    // Apply configuration changes to resilience patterns
    applyConfigurationToResiliencePatterns(updatedAdapter);
    
    // Publish domain events
    updatedAdapter.getDomainEvents().forEach(event -> {
      log.info("Publishing domain event: {} for adapter: {}", event.getEventType(), updatedAdapter.getId());
      // TODO: Publish to event bus (Kafka/Azure Service Bus)
    });
    updatedAdapter.clearDomainEvents();
    
    log.info("RTC adapter configuration updated successfully: {} with timeout: {}s, retries: {}, encryption: {}, api: {}", 
             adapterId, updatedAdapter.getTimeoutSeconds(), updatedAdapter.getRetryAttempts(), 
             updatedAdapter.getEncryptionEnabled(), updatedAdapter.getApiVersion());
    return updatedAdapter;
  }

  /**
   * Add a route to the RTC adapter
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
  public RtcAdapter addRoute(
      ClearingAdapterId adapterId,
      String routeName,
      String source,
      String destination,
      Integer priority,
      String addedBy) {
    
    return tracingService.executeInSpan("rtc.adapter.addRoute", Map.of(
        "adapter.id", adapterId.toString(),
        "route.name", routeName,
        "source", source,
        "destination", destination,
        "priority", priority != null ? priority.toString() : "null",
        "added.by", addedBy
    ), () -> {
      log.info("Adding route to RTC adapter: {} - route: {} from {} to {}", 
               adapterId, routeName, source, destination);
    
    RtcAdapter adapter = rtcAdapterRepository.findById(adapterId)
        .orElseThrow(() -> new RtcAdapterNotFoundException(adapterId));
    
    adapter.addRoute(
        ClearingRouteId.generate(),
        routeName,
        source,
        destination,
        priority,
        addedBy);
    
    RtcAdapter updatedAdapter = rtcAdapterRepository.save(adapter);
    
    // Publish domain events
    updatedAdapter.getDomainEvents().forEach(event -> {
      log.info("Publishing domain event: {} for adapter: {}", event.getEventType(), updatedAdapter.getId());
      // TODO: Publish to event bus (Kafka/Azure Service Bus)
    });
    updatedAdapter.clearDomainEvents();
    
    log.info("Successfully added route to RTC adapter: {} - route: {}", adapterId, routeName);
    return updatedAdapter;
    });
  }
  
  /**
   * Get all routes for the RTC adapter
   * 
   * @param adapterId The adapter ID
   * @return List of routes
   */
  public List<ClearingRoute> getRoutes(ClearingAdapterId adapterId) {
    log.info("Getting routes for RTC adapter: {}", adapterId);
    
    RtcAdapter adapter = rtcAdapterRepository.findById(adapterId)
        .orElseThrow(() -> new RtcAdapterNotFoundException(adapterId));
    
    List<ClearingRoute> routes = adapter.getRoutes();
    log.info("Found {} routes for RTC adapter: {}", routes.size(), adapterId);
    
    return routes;
  }
  
  /**
   * Log a message for the RTC adapter
   * 
   * @param adapterId The adapter ID
   * @param direction The message direction (INBOUND/OUTBOUND)
   * @param messageType The message type (e.g., pacs.008, pacs.002)
   * @param payloadHash The payload hash
   * @param statusCode The status code
   * @return The updated adapter
   */
  @Transactional
  public RtcAdapter logMessage(
      ClearingAdapterId adapterId,
      String direction,
      String messageType,
      String payloadHash,
      Integer statusCode) {
    
    return tracingService.executeInSpan("rtc.adapter.logMessage", Map.of(
        "adapter.id", adapterId.toString(),
        "direction", direction,
        "message.type", messageType,
        "payload.hash", payloadHash != null ? payloadHash : "null",
        "status.code", statusCode != null ? statusCode.toString() : "null"
    ), () -> {
      log.info("Logging message for RTC adapter: {} - direction: {}, type: {}, status: {}", 
               adapterId, direction, messageType, statusCode);
    
    RtcAdapter adapter = rtcAdapterRepository.findById(adapterId)
        .orElseThrow(() -> new RtcAdapterNotFoundException(adapterId));
    
    adapter.logMessage(
        ClearingMessageId.generate(),
        direction,
        messageType,
        payloadHash,
        statusCode);
    
    RtcAdapter updatedAdapter = rtcAdapterRepository.save(adapter);
    
    // Publish domain events
    updatedAdapter.getDomainEvents().forEach(event -> {
      log.info("Publishing domain event: {} for adapter: {}", event.getEventType(), updatedAdapter.getId());
      // TODO: Publish to event bus (Kafka/Azure Service Bus)
    });
    updatedAdapter.clearDomainEvents();
    
    log.info("Successfully logged message for RTC adapter: {} - direction: {}, type: {}", 
             adapterId, direction, messageType);
    return updatedAdapter;
    });
  }
  
  /**
   * Get all message logs for the RTC adapter
   * 
   * @param adapterId The adapter ID
   * @return List of message logs
   */
  public List<ClearingMessageLog> getMessageLogs(ClearingAdapterId adapterId) {
    log.info("Getting message logs for RTC adapter: {}", adapterId);
    
    RtcAdapter adapter = rtcAdapterRepository.findById(adapterId)
        .orElseThrow(() -> new RtcAdapterNotFoundException(adapterId));
    
    List<ClearingMessageLog> messageLogs = adapter.getMessageLogs();
    log.info("Found {} message logs for RTC adapter: {}", messageLogs.size(), adapterId);
    
    return messageLogs;
  }

  /**
   * Apply adapter configuration to resilience patterns
   * 
   * @param adapter The adapter with updated configuration
   */
  private void applyConfigurationToResiliencePatterns(RtcAdapter adapter) {
    log.info("Applying configuration to resilience patterns for adapter: {} - timeout: {}s, retries: {}, encryption: {}", 
             adapter.getId(), adapter.getTimeoutSeconds(), adapter.getRetryAttempts(), adapter.getEncryptionEnabled());
    
    // Log configuration changes for monitoring
    if (adapter.getEncryptionEnabled()) {
      log.info("Encryption enabled for RTC adapter: {} - API version: {}", adapter.getId(), adapter.getApiVersion());
    } else {
      log.warn("Encryption disabled for RTC adapter: {} - API version: {}", adapter.getId(), adapter.getApiVersion());
    }
    
    // Log real-time processing configuration
    log.info("Real-time processing configuration for adapter: {} - batch size: {}, window: {} to {}", 
             adapter.getId(), adapter.getBatchSize(), adapter.getProcessingWindowStart(), adapter.getProcessingWindowEnd());
  }

  /** Activate adapter */
  @Transactional
  public RtcAdapter activateAdapter(ClearingAdapterId adapterId, String activatedBy) {
    log.info("Activating RTC adapter: {}", adapterId);

    RtcAdapter adapter =
        rtcAdapterRepository
            .findById(adapterId)
            .orElseThrow(() -> new RtcAdapterNotFoundException(adapterId));

    adapter.activate(activatedBy);
    RtcAdapter activatedAdapter = rtcAdapterRepository.save(adapter);

    // Publish domain events
    activatedAdapter.getDomainEvents().forEach(event -> {
      log.info("Publishing domain event: {} for adapter: {}", event.getEventType(), activatedAdapter.getId());
      // TODO: Publish to event bus (Kafka/Azure Service Bus)
    });
    activatedAdapter.clearDomainEvents();

    log.info("RTC adapter activated successfully: {}", adapterId);
    return activatedAdapter;
  }

  /** Deactivate adapter */
  @Transactional
  public RtcAdapter deactivateAdapter(
      ClearingAdapterId adapterId, String reason, String deactivatedBy) {
    log.info("Deactivating RTC adapter: {} - Reason: {}", adapterId, reason);

    RtcAdapter adapter =
        rtcAdapterRepository
            .findById(adapterId)
            .orElseThrow(() -> new RtcAdapterNotFoundException(adapterId));

    adapter.deactivate(reason, deactivatedBy);
    RtcAdapter deactivatedAdapter = rtcAdapterRepository.save(adapter);

    // Publish domain events
    deactivatedAdapter.getDomainEvents().forEach(event -> {
      log.info("Publishing domain event: {} for adapter: {}", event.getEventType(), deactivatedAdapter.getId());
      // TODO: Publish to event bus (Kafka/Azure Service Bus)
    });
    deactivatedAdapter.clearDomainEvents();

    log.info("RTC adapter deactivated successfully: {}", adapterId);
    return deactivatedAdapter;
  }

  /** Get all adapters for tenant and business unit */
  public List<RtcAdapter> getAdaptersByTenant(String tenantId, String businessUnitId) {
    return rtcAdapterRepository.findByTenantAndBusinessUnit(tenantId, businessUnitId);
  }

  /** Get all active adapters for tenant */
  public List<RtcAdapter> getActiveAdaptersByTenant(String tenantId) {
    return rtcAdapterRepository.findActiveByTenant(tenantId);
  }

  /** Get adapters by status */
  public List<RtcAdapter> getAdaptersByStatus(AdapterOperationalStatus status) {
    return rtcAdapterRepository.findByStatus(status);
  }

  /** Check if adapter is active */
  public boolean isAdapterActive(ClearingAdapterId adapterId) {
    return rtcAdapterRepository.findById(adapterId).map(RtcAdapter::isActive).orElse(false);
  }

  /** Validate adapter configuration */
  public boolean validateAdapterConfiguration(ClearingAdapterId adapterId) {
    return rtcAdapterRepository
        .findById(adapterId)
        .map(
            adapter -> {
              // Basic validation - can be extended
              return adapter.getEndpoint() != null
                  && !adapter.getEndpoint().trim().isEmpty()
                  && adapter.getApiVersion() != null
                  && !adapter.getApiVersion().trim().isEmpty()
                  && adapter.getTimeoutSeconds() != null
                  && adapter.getTimeoutSeconds() > 0
                  && adapter.getRetryAttempts() != null
                  && adapter.getRetryAttempts() >= 0;
            })
        .orElse(false);
  }

  /** Get adapter statistics */
  public long getAdapterCount() {
    return rtcAdapterRepository.count();
  }

  /** Get adapter count by status */
  public long getAdapterCountByStatus(AdapterOperationalStatus status) {
    return rtcAdapterRepository.countByStatus(status);
  }

  /** Get adapter count by tenant */
  public long getAdapterCountByTenant(String tenantId) {
    return rtcAdapterRepository.countByTenant(tenantId);
  }
}
