package com.payments.samosadapter.service;

import com.payments.domain.shared.ClearingAdapterId;
import com.payments.domain.shared.ClearingMessageId;
import com.payments.domain.shared.ClearingRouteId;
import com.payments.domain.shared.TenantContext;
import com.payments.samosadapter.domain.ClearingMessageLog;
import com.payments.samosadapter.domain.ClearingRoute;
import com.payments.samosadapter.domain.SamosAdapter;
import com.payments.samosadapter.dto.SamosAdapterValidationResponse;
import com.payments.samosadapter.dto.SamosComplianceRequest;
import com.payments.samosadapter.dto.SamosComplianceResponse;
import com.payments.samosadapter.dto.SamosFraudDetectionRequest;
import com.payments.samosadapter.dto.SamosFraudDetectionResponse;
import com.payments.samosadapter.dto.SamosRiskAssessmentRequest;
import com.payments.samosadapter.dto.SamosRiskAssessmentResponse;
import com.payments.samosadapter.exception.SamosAdapterAlreadyExistsException;
import com.payments.samosadapter.exception.SamosAdapterNotFoundException;
import com.payments.samosadapter.exception.SamosAdapterOperationException;
import com.payments.samosadapter.repository.SamosAdapterRepository;
import com.payments.telemetry.TracingService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Timer;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * SAMOS Adapter Service
 *
 * <p>Business logic for managing SAMOS adapter configurations and operations. Handles adapter
 * lifecycle, configuration updates, and status management.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SamosAdapterService {

  private final SamosAdapterRepository samosAdapterRepository;
  private final TracingService tracingService;
  private final SamosCacheService samosCacheService;
  private final SamosBusinessRulesService samosBusinessRulesService;
  private final SamosOAuth2TokenService samosOAuth2TokenService;
  private final SamosServiceDiscoveryService samosServiceDiscoveryService;
  private final SamosComplianceService samosComplianceService;
  private final SamosFraudDetectionService samosFraudDetectionService;
  private final SamosRiskAssessmentService samosRiskAssessmentService;
  private final Counter samosAdapterCreatedCounter;
  private final Counter samosAdapterActivatedCounter;
  private final Counter samosAdapterDeactivatedCounter;
  private final Counter samosAdapterConfigurationUpdatedCounter;
  private final Timer samosAdapterCreationTimer;
  private final Timer samosAdapterActivationTimer;
  private final Timer samosAdapterDeactivationTimer;
  private final Timer samosAdapterConfigurationUpdateTimer;

  /** Create a new SAMOS adapter */
  @CircuitBreaker(name = "samos-adapter", fallbackMethod = "createAdapterFallback")
  @Retry(name = "samos-adapter")
  @TimeLimiter(name = "samos-adapter")
  @CacheEvict(value = "samos-adapter-count", allEntries = true)
  public CompletableFuture<SamosAdapter> createAdapter(
      ClearingAdapterId adapterId,
      TenantContext tenantContext,
      String adapterName,
      String endpoint,
      String createdBy) {

    return CompletableFuture.supplyAsync(
        () -> {
          return tracingService.executeInSpan(
              "samos.adapter.create",
              Map.of(
                  "adapter.name", adapterName,
                  "tenant.id", tenantContext.getTenantId(),
                  "business.unit.id", tenantContext.getBusinessUnitId()),
              () -> {
                try {
                  return samosAdapterCreationTimer.recordCallable(
                      () -> {
                        log.info(
                            "Creating SAMOS adapter: {} for tenant: {}",
                            adapterName,
                            tenantContext.getTenantId());

                        // Check if adapter name already exists for tenant
                        if (samosAdapterRepository.existsByTenantIdAndAdapterName(
                            tenantContext.getTenantId(), adapterName)) {
                          throw new SamosAdapterAlreadyExistsException(
                              adapterName, tenantContext.getTenantId());
                        }

                        SamosAdapter adapter =
                            SamosAdapter.create(
                                adapterId, tenantContext, adapterName, endpoint, createdBy);

                        SamosAdapter savedAdapter = samosAdapterRepository.save(adapter);

                        // Apply default configuration to resilience patterns
                        applyConfigurationToResiliencePatterns(savedAdapter);

                        // Publish domain events
                        savedAdapter
                            .getDomainEvents()
                            .forEach(
                                event -> {
                                  log.info(
                                      "Publishing domain event: {} for adapter: {}",
                                      event.getEventType(),
                                      savedAdapter.getId());
                                  // TODO: Publish to event bus (Kafka/Azure Service Bus)
                                });
                        savedAdapter.clearDomainEvents();

                        // Record metrics
                        samosAdapterCreatedCounter.increment();

                        log.info(
                            "Created SAMOS adapter: {} with ID: {} - timeout: {}s, retries: {}, encryption: {}, api: {}",
                            adapterName,
                            savedAdapter.getId(),
                            savedAdapter.getTimeoutSeconds(),
                            savedAdapter.getRetryAttempts(),
                            savedAdapter.getEncryptionEnabled(),
                            savedAdapter.getApiVersion());
                        return savedAdapter;
                      });
                } catch (Exception e) {
                  log.error("Error creating SAMOS adapter: {} - {}", adapterName, e.getMessage());
                  throw new SamosAdapterOperationException("create", e.getMessage());
                }
              });
        });
  }

  /** Fallback method for createAdapter */
  public CompletableFuture<SamosAdapter> createAdapterFallback(
      ClearingAdapterId adapterId,
      TenantContext tenantContext,
      String adapterName,
      String endpoint,
      String createdBy,
      Exception ex) {
    log.error("Failed to create SAMOS adapter: {} - {}", adapterName, ex.getMessage());
    return CompletableFuture.failedFuture(
        new SamosAdapterOperationException("create", ex.getMessage()));
  }

  /** Fallback method for getAdapter */
  public Optional<SamosAdapter> getAdapterFallback(ClearingAdapterId adapterId, Exception ex) {
    log.error("Failed to get SAMOS adapter: {} - {}", adapterId, ex.getMessage());
    return Optional.empty();
  }

  /** Get SAMOS adapter by ID */
  @CircuitBreaker(name = "samos-adapter", fallbackMethod = "getAdapterFallback")
  @Retry(name = "samos-adapter")
  @Transactional(readOnly = true)
  public Optional<SamosAdapter> getAdapter(ClearingAdapterId adapterId) {
    return samosAdapterRepository.findById(adapterId);
  }

  /** Get SAMOS adapter by tenant ID and adapter name */
  @Transactional(readOnly = true)
  public Optional<SamosAdapter> getAdapterByTenantAndName(UUID tenantId, String adapterName) {
    return samosAdapterRepository.findByTenantIdAndAdapterName(
        tenantId != null ? tenantId.toString() : null, adapterName);
  }

  /** Get all active adapters for tenant */
  @Transactional(readOnly = true)
  public List<SamosAdapter> getActiveAdapters(UUID tenantId) {
    return samosAdapterRepository.findActiveByTenantId(
        tenantId != null ? tenantId.toString() : null);
  }

  /** Get all adapters for tenant and business unit */
  @Transactional(readOnly = true)
  public List<SamosAdapter> getAdaptersByTenantAndBusinessUnit(UUID tenantId, UUID businessUnitId) {
    return samosAdapterRepository.findByTenantIdAndBusinessUnitId(
        tenantId != null ? tenantId.toString() : null,
        businessUnitId != null ? businessUnitId.toString() : null);
  }

  /** Update adapter configuration */
  @CircuitBreaker(name = "samos-adapter", fallbackMethod = "updateAdapterConfigurationFallback")
  @Retry(name = "samos-adapter")
  @TimeLimiter(name = "samos-adapter")
  public CompletableFuture<SamosAdapter> updateAdapterConfiguration(
      ClearingAdapterId adapterId,
      String endpoint,
      String apiVersion,
      Integer timeoutSeconds,
      Integer retryAttempts,
      Boolean encryptionEnabled,
      String certificatePath,
      String certificatePassword,
      String updatedBy) {

    return CompletableFuture.supplyAsync(
        () -> {
          return tracingService.executeInSpan(
              "samos.adapter.update",
              Map.of(
                  "adapter.id", adapterId.toString(),
                  "endpoint", endpoint != null ? endpoint : "null",
                  "api.version", apiVersion != null ? apiVersion : "null",
                  "timeout.seconds", timeoutSeconds != null ? timeoutSeconds.toString() : "null",
                  "retry.attempts", retryAttempts != null ? retryAttempts.toString() : "null",
                  "encryption.enabled",
                      encryptionEnabled != null ? encryptionEnabled.toString() : "null"),
              () -> {
                log.info("Updating SAMOS adapter configuration: {}", adapterId);

                SamosAdapter adapter =
                    samosAdapterRepository
                        .findById(adapterId)
                        .orElseThrow(() -> new SamosAdapterNotFoundException(adapterId));

                adapter.updateConfiguration(
                    endpoint,
                    apiVersion,
                    timeoutSeconds,
                    retryAttempts,
                    encryptionEnabled,
                    certificatePath,
                    certificatePassword,
                    updatedBy);

                SamosAdapter updatedAdapter = samosAdapterRepository.save(adapter);

                // Apply configuration changes to resilience patterns
                applyConfigurationToResiliencePatterns(updatedAdapter);

                // Publish domain events
                updatedAdapter
                    .getDomainEvents()
                    .forEach(
                        event -> {
                          log.info(
                              "Publishing domain event: {} for adapter: {}",
                              event.getEventType(),
                              updatedAdapter.getId());
                          // TODO: Publish to event bus (Kafka/Azure Service Bus)
                        });
                updatedAdapter.clearDomainEvents();

                log.info(
                    "Updated SAMOS adapter configuration: {} with timeout: {}s, retries: {}, encryption: {}, api: {}",
                    adapterId,
                    updatedAdapter.getTimeoutSeconds(),
                    updatedAdapter.getRetryAttempts(),
                    updatedAdapter.getEncryptionEnabled(),
                    updatedAdapter.getApiVersion());
                return updatedAdapter;
              });
        });
  }

  /**
   * Add a route to the SAMOS adapter
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
  public SamosAdapter addRoute(
      ClearingAdapterId adapterId,
      String routeName,
      String source,
      String destination,
      Integer priority,
      String addedBy) {

    return tracingService.executeInSpan(
        "samos.adapter.addRoute",
        Map.of(
            "adapter.id", adapterId.toString(),
            "route.name", routeName,
            "source", source,
            "destination", destination,
            "priority", priority != null ? priority.toString() : "null",
            "added.by", addedBy),
        () -> {
          log.info(
              "Adding route to SAMOS adapter: {} - route: {} from {} to {}",
              adapterId,
              routeName,
              source,
              destination);

          SamosAdapter adapter =
              samosAdapterRepository
                  .findById(adapterId)
                  .orElseThrow(() -> new SamosAdapterNotFoundException(adapterId));

          adapter.addRoute(
              ClearingRouteId.generate(), routeName, source, destination, priority, addedBy);

          SamosAdapter updatedAdapter = samosAdapterRepository.save(adapter);

          // Publish domain events
          updatedAdapter
              .getDomainEvents()
              .forEach(
                  event -> {
                    log.info(
                        "Publishing domain event: {} for adapter: {}",
                        event.getEventType(),
                        updatedAdapter.getId());
                    // TODO: Publish to event bus (Kafka/Azure Service Bus)
                  });
          updatedAdapter.clearDomainEvents();

          log.info(
              "Successfully added route to SAMOS adapter: {} - route: {}", adapterId, routeName);
          return updatedAdapter;
        });
  }

  /**
   * Get all routes for the SAMOS adapter
   *
   * @param adapterId The adapter ID
   * @return List of routes
   */
  public List<ClearingRoute> getRoutes(ClearingAdapterId adapterId) {
    log.info("Getting routes for SAMOS adapter: {}", adapterId);

    SamosAdapter adapter =
        samosAdapterRepository
            .findById(adapterId)
            .orElseThrow(() -> new SamosAdapterNotFoundException(adapterId));

    List<ClearingRoute> routes = adapter.getRoutes();
    log.info("Found {} routes for SAMOS adapter: {}", routes.size(), adapterId);

    return routes;
  }

  /**
   * Log a message for the SAMOS adapter
   *
   * @param adapterId The adapter ID
   * @param direction The message direction (INBOUND/OUTBOUND)
   * @param messageType The message type (e.g., pacs.008, pain.001)
   * @param payloadHash The payload hash
   * @param statusCode The status code
   * @return The updated adapter
   */
  @Transactional
  public SamosAdapter logMessage(
      ClearingAdapterId adapterId,
      String direction,
      String messageType,
      String payloadHash,
      Integer statusCode) {

    return tracingService.executeInSpan(
        "samos.adapter.logMessage",
        Map.of(
            "adapter.id",
            adapterId.toString(),
            "direction",
            direction,
            "message.type",
            messageType,
            "payload.hash",
            payloadHash != null ? payloadHash : "null",
            "status.code",
            statusCode != null ? statusCode.toString() : "null"),
        () -> {
          log.info(
              "Logging message for SAMOS adapter: {} - direction: {}, type: {}, status: {}",
              adapterId,
              direction,
              messageType,
              statusCode);

          SamosAdapter adapter =
              samosAdapterRepository
                  .findById(adapterId)
                  .orElseThrow(() -> new SamosAdapterNotFoundException(adapterId));

          adapter.logMessage(
              ClearingMessageId.generate(), direction, messageType, payloadHash, statusCode);

          SamosAdapter updatedAdapter = samosAdapterRepository.save(adapter);

          // Publish domain events
          updatedAdapter
              .getDomainEvents()
              .forEach(
                  event -> {
                    log.info(
                        "Publishing domain event: {} for adapter: {}",
                        event.getEventType(),
                        updatedAdapter.getId());
                    // TODO: Publish to event bus (Kafka/Azure Service Bus)
                  });
          updatedAdapter.clearDomainEvents();

          log.info(
              "Successfully logged message for SAMOS adapter: {} - direction: {}, type: {}",
              adapterId,
              direction,
              messageType);
          return updatedAdapter;
        });
  }

  /**
   * Get all message logs for the SAMOS adapter
   *
   * @param adapterId The adapter ID
   * @return List of message logs
   */
  public List<ClearingMessageLog> getMessageLogs(ClearingAdapterId adapterId) {
    log.info("Getting message logs for SAMOS adapter: {}", adapterId);

    SamosAdapter adapter =
        samosAdapterRepository
            .findById(adapterId)
            .orElseThrow(() -> new SamosAdapterNotFoundException(adapterId));

    List<ClearingMessageLog> messageLogs = adapter.getMessageLogs();
    log.info("Found {} message logs for SAMOS adapter: {}", messageLogs.size(), adapterId);

    return messageLogs;
  }

  /**
   * Apply adapter configuration to resilience patterns
   *
   * @param adapter The adapter with updated configuration
   */
  private void applyConfigurationToResiliencePatterns(SamosAdapter adapter) {
    log.info(
        "Applying configuration to resilience patterns for adapter: {} - timeout: {}s, retries: {}, encryption: {}",
        adapter.getId(),
        adapter.getTimeoutSeconds(),
        adapter.getRetryAttempts(),
        adapter.getEncryptionEnabled());

    // Log configuration changes for monitoring
    if (adapter.getEncryptionEnabled()) {
      log.info(
          "Encryption enabled for SAMOS adapter: {} - API version: {}",
          adapter.getId(),
          adapter.getApiVersion());
    } else {
      log.warn(
          "Encryption disabled for SAMOS adapter: {} - API version: {}",
          adapter.getId(),
          adapter.getApiVersion());
    }

    // Record configuration metrics
    samosAdapterConfigurationUpdatedCounter.increment();
  }

  /** Fallback method for updateAdapterConfiguration */
  public CompletableFuture<SamosAdapter> updateAdapterConfigurationFallback(
      ClearingAdapterId adapterId,
      String endpoint,
      String apiVersion,
      Integer timeoutSeconds,
      Integer retryAttempts,
      Boolean encryptionEnabled,
      String certificatePath,
      String certificatePassword,
      String updatedBy,
      Exception ex) {
    log.error("Failed to update SAMOS adapter configuration: {} - {}", adapterId, ex.getMessage());
    return CompletableFuture.failedFuture(
        new SamosAdapterOperationException("update configuration", ex.getMessage()));
  }

  /** Activate adapter */
  @CircuitBreaker(name = "samos-adapter", fallbackMethod = "activateAdapterFallback")
  @Retry(name = "samos-adapter")
  @TimeLimiter(name = "samos-adapter")
  public CompletableFuture<SamosAdapter> activateAdapter(
      ClearingAdapterId adapterId, String activatedBy) {
    return CompletableFuture.supplyAsync(
        () -> {
          return tracingService.executeInSpan(
              "samos.adapter.activate",
              Map.of("adapter.id", adapterId.toString(), "activated.by", activatedBy),
              () -> {
                try {
                  return samosAdapterActivationTimer.recordCallable(
                      () -> {
                        log.info("Activating SAMOS adapter: {}", adapterId);

                        SamosAdapter adapter =
                            samosAdapterRepository
                                .findById(adapterId)
                                .orElseThrow(() -> new SamosAdapterNotFoundException(adapterId));

                        adapter.activate(activatedBy);

                        SamosAdapter activatedAdapter = samosAdapterRepository.save(adapter);

                        // Publish domain events
                        activatedAdapter
                            .getDomainEvents()
                            .forEach(
                                event -> {
                                  log.info(
                                      "Publishing domain event: {} for adapter: {}",
                                      event.getEventType(),
                                      activatedAdapter.getId());
                                  // TODO: Publish to event bus (Kafka/Azure Service Bus)
                                });
                        activatedAdapter.clearDomainEvents();

                        // Record metrics
                        samosAdapterActivatedCounter.increment();

                        log.info("Activated SAMOS adapter: {}", adapterId);
                        return activatedAdapter;
                      });
                } catch (Exception e) {
                  log.error("Error activating SAMOS adapter: {} - {}", adapterId, e.getMessage());
                  throw new SamosAdapterOperationException("activate", e.getMessage());
                }
              });
        });
  }

  /** Fallback method for activateAdapter */
  public CompletableFuture<SamosAdapter> activateAdapterFallback(
      ClearingAdapterId adapterId, String activatedBy, Exception ex) {
    log.error("Failed to activate SAMOS adapter: {} - {}", adapterId, ex.getMessage());
    return CompletableFuture.failedFuture(
        new SamosAdapterOperationException("activate", ex.getMessage()));
  }

  /** Deactivate adapter */
  @CircuitBreaker(name = "samos-adapter", fallbackMethod = "deactivateAdapterFallback")
  @Retry(name = "samos-adapter")
  @TimeLimiter(name = "samos-adapter")
  public CompletableFuture<SamosAdapter> deactivateAdapter(
      ClearingAdapterId adapterId, String reason, String deactivatedBy) {
    return CompletableFuture.supplyAsync(
        () -> {
          return tracingService.executeInSpan(
              "samos.adapter.deactivate",
              Map.of(
                  "adapter.id", adapterId.toString(),
                  "reason", reason,
                  "deactivated.by", deactivatedBy),
              () -> {
                log.info("Deactivating SAMOS adapter: {} - Reason: {}", adapterId, reason);

                SamosAdapter adapter =
                    samosAdapterRepository
                        .findById(adapterId)
                        .orElseThrow(() -> new SamosAdapterNotFoundException(adapterId));

                adapter.deactivate(reason, deactivatedBy);

                SamosAdapter deactivatedAdapter = samosAdapterRepository.save(adapter);

                // Publish domain events
                deactivatedAdapter
                    .getDomainEvents()
                    .forEach(
                        event -> {
                          log.info(
                              "Publishing domain event: {} for adapter: {}",
                              event.getEventType(),
                              deactivatedAdapter.getId());
                          // TODO: Publish to event bus (Kafka/Azure Service Bus)
                        });
                deactivatedAdapter.clearDomainEvents();

                log.info("Deactivated SAMOS adapter: {}", adapterId);
                return deactivatedAdapter;
              });
        });
  }

  /** Fallback method for deactivateAdapter */
  public CompletableFuture<SamosAdapter> deactivateAdapterFallback(
      ClearingAdapterId adapterId, String reason, String deactivatedBy, Exception ex) {
    log.error("Failed to deactivate SAMOS adapter: {} - {}", adapterId, ex.getMessage());
    return CompletableFuture.failedFuture(
        new SamosAdapterOperationException("deactivate", ex.getMessage()));
  }

  /** Check if adapter is active */
  @Transactional(readOnly = true)
  public boolean isAdapterActive(ClearingAdapterId adapterId) {
    return samosAdapterRepository.findById(adapterId).map(SamosAdapter::isActive).orElse(false);
  }

  /** Get adapter count for tenant */
  @Transactional(readOnly = true)
  public long getActiveAdapterCount(UUID tenantId) {
    return samosAdapterRepository.countActiveByTenantId(
        tenantId != null ? tenantId.toString() : null);
  }

  /** Validate adapter configuration */
  @Transactional(readOnly = true)
  public boolean validateAdapterConfiguration(ClearingAdapterId adapterId) {
    return samosAdapterRepository
        .findById(adapterId)
        .map(
            adapter -> {
              // Basic validation checks
              return adapter.getEndpoint() != null
                  && !adapter.getEndpoint().isBlank()
                  && adapter.getTimeoutSeconds() > 0
                  && adapter.getRetryAttempts() >= 0;
            })
        .orElse(false);
  }

  /** Get total adapter count */
  @Cacheable(value = "samos-adapter-count", key = "'total'")
  public long getAdapterCount() {
    return tracingService.executeInSpan(
        "samos.adapter.getAdapterCount",
        Map.of("operation", "getAdapterCount"),
        () -> {
          log.debug("Getting total adapter count");
          return samosAdapterRepository.count();
        });
  }

  /** Get active adapter count */
  @Cacheable(value = "samos-adapter-count", key = "'active'")
  public long getActiveAdapterCount() {
    return tracingService.executeInSpan(
        "samos.adapter.getActiveAdapterCount",
        Map.of("operation", "getActiveAdapterCount"),
        () -> {
          log.debug("Getting active adapter count");
          return samosAdapterRepository.countByStatus(
              com.payments.domain.clearing.AdapterOperationalStatus.ACTIVE);
        });
  }

  /**
   * Validate SAMOS adapter using business rules
   *
   * @param adapterId The adapter ID
   * @param tenantContext Tenant context
   * @return Validation response
   */
  public SamosAdapterValidationResponse validateAdapter(
      ClearingAdapterId adapterId, TenantContext tenantContext) {

    return tracingService.executeInSpan(
        "samos.adapter.validate",
        Map.of(
            "adapter.id", adapterId.toString(),
            "tenant.id", tenantContext.getTenantId(),
            "business.unit.id", tenantContext.getBusinessUnitId()),
        () -> {
          log.info(
              "Validating SAMOS adapter: {} for tenant: {}",
              adapterId,
              tenantContext.getTenantId());

          SamosAdapter adapter =
              samosAdapterRepository
                  .findById(adapterId)
                  .orElseThrow(() -> new SamosAdapterNotFoundException(adapterId.toString()));

          return samosBusinessRulesService.validateAdapterConfiguration(adapter, tenantContext);
        });
  }

  /**
   * Get OAuth2 authorization header for SAMOS clearing network
   *
   * @return Authorization header value
   */
  public String getAuthorizationHeader() {
    return tracingService.executeInSpan(
        "samos.adapter.getAuthorizationHeader",
        Map.of("operation", "getAuthorizationHeader"),
        () -> {
          log.debug("Getting SAMOS OAuth2 authorization header");
          return samosOAuth2TokenService.getAuthorizationHeader();
        });
  }

  /**
   * Check if OAuth2 token is valid for SAMOS clearing network
   *
   * @return True if token is valid
   */
  public boolean isTokenValid() {
    return tracingService.executeInSpan(
        "samos.adapter.isTokenValid",
        Map.of("operation", "isTokenValid"),
        () -> {
          log.debug("Checking SAMOS OAuth2 token validity");
          return samosOAuth2TokenService.isTokenValid();
        });
  }

  /**
   * Refresh OAuth2 token for SAMOS clearing network
   *
   * @return New authorization header
   */
  public String refreshToken() {
    return tracingService.executeInSpan(
        "samos.adapter.refreshToken",
        Map.of("operation", "refreshToken"),
        () -> {
          log.info("Refreshing SAMOS OAuth2 token");
          return samosOAuth2TokenService.refreshAccessToken();
        });
  }

  /**
   * Discover available SAMOS clearing network services
   *
   * @return List of service instances
   */
  public List<org.springframework.cloud.client.ServiceInstance> discoverClearingServices() {
    return tracingService.executeInSpan(
        "samos.adapter.discoverServices",
        Map.of("operation", "discoverClearingServices"),
        () -> {
          log.debug("Discovering SAMOS clearing network services");
          return samosServiceDiscoveryService.discoverSamosServices();
        });
  }

  /**
   * Get the best available SAMOS service endpoint
   *
   * @return Service endpoint URL or null
   */
  public String getClearingServiceEndpoint() {
    return tracingService.executeInSpan(
        "samos.adapter.getServiceEndpoint",
        Map.of("operation", "getClearingServiceEndpoint"),
        () -> {
          log.debug("Getting SAMOS clearing network service endpoint");
          return samosServiceDiscoveryService.getSamosServiceEndpoint();
        });
  }

  /**
   * Check if service discovery is healthy
   *
   * @return True if service discovery is working
   */
  public boolean isServiceDiscoveryHealthy() {
    return tracingService.executeInSpan(
        "samos.adapter.isServiceDiscoveryHealthy",
        Map.of("operation", "isServiceDiscoveryHealthy"),
        () -> {
          log.debug("Checking SAMOS service discovery health");
          return samosServiceDiscoveryService.isServiceDiscoveryHealthy();
        });
  }

  /**
   * Execute compliance rules for SAMOS clearing network
   *
   * @param adapterId The adapter ID
   * @param request Compliance request
   * @param tenantContext Tenant context
   * @return Compliance response
   */
  public SamosComplianceResponse executeComplianceRules(
      ClearingAdapterId adapterId, SamosComplianceRequest request, TenantContext tenantContext) {

    return tracingService.executeInSpan(
        "samos.adapter.executeComplianceRules",
        Map.of(
            "adapter.id", adapterId.toString(),
            "payment.id", request.getPaymentId(),
            "tenant.id", tenantContext.getTenantId(),
            "business.unit.id", tenantContext.getBusinessUnitId()),
        () -> {
          log.info(
              "Executing SAMOS compliance rules for adapter: {} and payment: {}",
              adapterId,
              request.getPaymentId());

          SamosAdapter adapter =
              samosAdapterRepository
                  .findById(adapterId)
                  .orElseThrow(() -> new SamosAdapterNotFoundException(adapterId.toString()));

          return samosComplianceService.executeComplianceRules(adapter, request, tenantContext);
        });
  }

  /**
   * Execute fraud detection rules for SAMOS clearing network
   *
   * @param adapterId The adapter ID
   * @param request Fraud detection request
   * @param tenantContext Tenant context
   * @return Fraud detection response
   */
  public SamosFraudDetectionResponse executeFraudDetectionRules(
      ClearingAdapterId adapterId,
      SamosFraudDetectionRequest request,
      TenantContext tenantContext) {

    return tracingService.executeInSpan(
        "samos.adapter.executeFraudDetectionRules",
        Map.of(
            "adapter.id", adapterId.toString(),
            "payment.id", request.getPaymentId(),
            "tenant.id", tenantContext.getTenantId(),
            "business.unit.id", tenantContext.getBusinessUnitId()),
        () -> {
          log.info(
              "Executing SAMOS fraud detection rules for adapter: {} and payment: {}",
              adapterId,
              request.getPaymentId());

          SamosAdapter adapter =
              samosAdapterRepository
                  .findById(adapterId)
                  .orElseThrow(() -> new SamosAdapterNotFoundException(adapterId.toString()));

          return samosFraudDetectionService.executeFraudDetectionRules(
              adapter, request, tenantContext);
        });
  }

  /**
   * Execute risk assessment rules for SAMOS clearing network
   *
   * @param adapterId The adapter ID
   * @param request Risk assessment request
   * @param tenantContext Tenant context
   * @return Risk assessment response
   */
  public SamosRiskAssessmentResponse executeRiskAssessmentRules(
      ClearingAdapterId adapterId,
      SamosRiskAssessmentRequest request,
      TenantContext tenantContext) {

    return tracingService.executeInSpan(
        "samos.adapter.executeRiskAssessmentRules",
        Map.of(
            "adapter.id", adapterId.toString(),
            "payment.id", request.getPaymentId(),
            "tenant.id", tenantContext.getTenantId(),
            "business.unit.id", tenantContext.getBusinessUnitId()),
        () -> {
          log.info(
              "Executing SAMOS risk assessment rules for adapter: {} and payment: {}",
              adapterId,
              request.getPaymentId());

          SamosAdapter adapter =
              samosAdapterRepository
                  .findById(adapterId)
                  .orElseThrow(() -> new SamosAdapterNotFoundException(adapterId.toString()));

          return samosRiskAssessmentService.executeRiskAssessmentRules(
              adapter, request, tenantContext);
        });
  }
}
