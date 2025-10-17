package com.payments.swiftadapter.domain;

import com.payments.domain.clearing.AdapterOperationalStatus;
import com.payments.domain.clearing.ClearingNetwork;
import com.payments.domain.shared.ClearingAdapterId;
import com.payments.domain.shared.ClearingMessageId;
import com.payments.domain.shared.ClearingRouteId;
import com.payments.domain.shared.DomainEvent;
import com.payments.domain.shared.TenantContext;
import com.payments.swiftadapter.exception.InvalidSwiftAdapterException;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * SWIFT Adapter Aggregate Root
 *
 * <p>Handles international payments with SWIFT network integration. Features: - International
 * payments via SWIFT network - MT103/pacs.008 messaging support - Sanctions screening and
 * compliance - Foreign exchange (FX) conversion - Cross-border payment processing
 */
@Entity
@Table(name = "swift_adapters")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SwiftAdapter {

  @EmbeddedId
  private ClearingAdapterId id;

  @Embedded
  @AttributeOverrides({
    @AttributeOverride(name = "tenantId", column = @Column(name = "tenant_id")),
    @AttributeOverride(name = "tenantName", column = @Column(name = "tenant_name")),
    @AttributeOverride(name = "businessUnitId", column = @Column(name = "business_unit_id")),
    @AttributeOverride(name = "businessUnitName", column = @Column(name = "business_unit_name"))
  })
  private TenantContext tenantContext;

  @Column(name = "adapter_name", nullable = false)
  private String adapterName;

  @Enumerated(EnumType.STRING)
  @Column(name = "network", nullable = false)
  private ClearingNetwork network;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false)
  private AdapterOperationalStatus status;

  @Column(name = "endpoint", nullable = false)
  private String endpoint;

  @Column(name = "api_version")
  private String apiVersion;

  @Column(name = "timeout_seconds")
  private Integer timeoutSeconds;

  @Column(name = "retry_attempts")
  private Integer retryAttempts;

  @Column(name = "encryption_enabled")
  private Boolean encryptionEnabled;

  @Column(name = "batch_size")
  private Integer batchSize;

  @Column(name = "sanctions_screening_enabled")
  private Boolean sanctionsScreeningEnabled;

  @Column(name = "sanctions_endpoint")
  private String sanctionsEndpoint;

  @Column(name = "sanctions_timeout")
  private Integer sanctionsTimeout;

  @Column(name = "sanctions_retry_attempts")
  private Integer sanctionsRetryAttempts;

  @Column(name = "fx_conversion_enabled")
  private Boolean fxConversionEnabled;

  @Column(name = "fx_endpoint")
  private String fxEndpoint;

  @Column(name = "fx_timeout")
  private Integer fxTimeout;

  @Column(name = "fx_retry_attempts")
  private Integer fxRetryAttempts;

  @Column(name = "created_at")
  private Instant createdAt;

  @Column(name = "updated_at")
  private Instant updatedAt;

  @Column(name = "created_by")
  private String createdBy;

  @Column(name = "updated_by")
  private String updatedBy;

  @Version
  @Column(name = "version")
  private Integer version;

  @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
  @JoinColumn(name = "clearing_adapter_id")
  @Builder.Default
  private List<ClearingRoute> routes = new ArrayList<>();

  @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
  @JoinColumn(name = "clearing_adapter_id")
  @Builder.Default
  private List<ClearingMessageLog> messageLogs = new ArrayList<>();

  @Transient @Builder.Default private List<DomainEvent> domainEvents = new ArrayList<>();

  /** Create a new SWIFT adapter */
  public static SwiftAdapter create(
      ClearingAdapterId id,
      TenantContext tenantContext,
      String adapterName,
      String endpoint,
      String createdBy) {
    if (id == null) {
      throw new InvalidSwiftAdapterException("Adapter ID cannot be null");
    }
    if (tenantContext == null) {
      throw new InvalidSwiftAdapterException("Tenant context cannot be null");
    }
    if (adapterName == null || adapterName.trim().isEmpty()) {
      throw new InvalidSwiftAdapterException("Adapter name cannot be null or empty");
    }
    if (endpoint == null || endpoint.trim().isEmpty()) {
      throw new InvalidSwiftAdapterException("Endpoint cannot be null or empty");
    }

    SwiftAdapter adapter =
        SwiftAdapter.builder()
            .id(id)
            .tenantContext(tenantContext)
            .adapterName(adapterName)
            .network(ClearingNetwork.SWIFT)
            .status(AdapterOperationalStatus.INACTIVE)
            .endpoint(endpoint)
            .apiVersion("1.0")
            .timeoutSeconds(10)
            .retryAttempts(3)
            .encryptionEnabled(true)
            .batchSize(100)
            .sanctionsScreeningEnabled(true)
            .sanctionsTimeout(5)
            .sanctionsRetryAttempts(2)
            .fxConversionEnabled(true)
            .fxTimeout(3)
            .fxRetryAttempts(2)
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .createdBy(createdBy)
            .version(1)
            .build();

    adapter.registerEvent(
        new SwiftAdapterCreatedEvent(
            id.toString(), tenantContext.getTenantId(), adapterName, Instant.now()));

    return adapter;
  }

  /** Update adapter configuration */
  public void updateConfiguration(
      String endpoint,
      String apiVersion,
      Integer timeoutSeconds,
      Integer retryAttempts,
      Boolean encryptionEnabled,
      Integer batchSize,
      Boolean sanctionsScreeningEnabled,
      String sanctionsEndpoint,
      Integer sanctionsTimeout,
      Integer sanctionsRetryAttempts,
      Boolean fxConversionEnabled,
      String fxEndpoint,
      Integer fxTimeout,
      Integer fxRetryAttempts,
      String updatedBy) {
    if (endpoint != null && !endpoint.trim().isEmpty()) {
      this.endpoint = endpoint;
    }
    if (apiVersion != null && !apiVersion.trim().isEmpty()) {
      this.apiVersion = apiVersion;
    }
    if (timeoutSeconds != null && timeoutSeconds > 0) {
      this.timeoutSeconds = timeoutSeconds;
    }
    if (retryAttempts != null && retryAttempts >= 0) {
      this.retryAttempts = retryAttempts;
    }
    if (encryptionEnabled != null) {
      this.encryptionEnabled = encryptionEnabled;
    }
    if (batchSize != null && batchSize > 0) {
      this.batchSize = batchSize;
    }
    if (sanctionsScreeningEnabled != null) {
      this.sanctionsScreeningEnabled = sanctionsScreeningEnabled;
    }
    if (sanctionsEndpoint != null && !sanctionsEndpoint.trim().isEmpty()) {
      this.sanctionsEndpoint = sanctionsEndpoint;
    }
    if (sanctionsTimeout != null && sanctionsTimeout > 0) {
      this.sanctionsTimeout = sanctionsTimeout;
    }
    if (sanctionsRetryAttempts != null && sanctionsRetryAttempts >= 0) {
      this.sanctionsRetryAttempts = sanctionsRetryAttempts;
    }
    if (fxConversionEnabled != null) {
      this.fxConversionEnabled = fxConversionEnabled;
    }
    if (fxEndpoint != null && !fxEndpoint.trim().isEmpty()) {
      this.fxEndpoint = fxEndpoint;
    }
    if (fxTimeout != null && fxTimeout > 0) {
      this.fxTimeout = fxTimeout;
    }
    if (fxRetryAttempts != null && fxRetryAttempts >= 0) {
      this.fxRetryAttempts = fxRetryAttempts;
    }

    this.updatedAt = Instant.now();
    this.updatedBy = updatedBy;
    this.version++;

    registerEvent(
        new SwiftAdapterConfigurationUpdatedEvent(
            this.id.toString(), this.tenantContext.getTenantId(), Instant.now()));
  }

  /** Activate the adapter */
  public void activate(String activatedBy) {
    if (this.status == AdapterOperationalStatus.ACTIVE) {
      throw new IllegalStateException("Adapter is already active");
    }

    this.status = AdapterOperationalStatus.ACTIVE;
    this.updatedAt = Instant.now();
    this.updatedBy = activatedBy;
    this.version++;

    registerEvent(
        new SwiftAdapterActivatedEvent(this.id.toString(), this.tenantContext.getTenantId(), Instant.now()));
  }

  /** Deactivate the adapter */
  public void deactivate(String reason, String deactivatedBy) {
    if (this.status == AdapterOperationalStatus.INACTIVE) {
      throw new IllegalStateException("Adapter is already inactive");
    }

    this.status = AdapterOperationalStatus.INACTIVE;
    this.updatedAt = Instant.now();
    this.updatedBy = deactivatedBy;
    this.version++;

    registerEvent(
        new SwiftAdapterDeactivatedEvent(
            this.id.toString(), this.tenantContext.getTenantId(), reason, Instant.now()));
  }

  /** Check if adapter is active */
  public boolean isActive() {
    return this.status == AdapterOperationalStatus.ACTIVE;
  }

  /** Validate adapter configuration */
  public boolean isConfigurationValid() {
    return this.endpoint != null
        && !this.endpoint.trim().isEmpty()
        && this.apiVersion != null
        && !this.apiVersion.trim().isEmpty()
        && this.timeoutSeconds != null
        && this.timeoutSeconds > 0
        && this.retryAttempts != null
        && this.retryAttempts >= 0;
  }

  /** Add route */
  public void addRoute(
      ClearingRouteId routeId,
      String routeName,
      String source,
      String destination,
      Integer priority,
      String addedBy) {
    ClearingRoute route =
        ClearingRoute.create(routeId, this.id, routeName, source, destination, priority, addedBy);
    this.routes.add(route);
    this.updatedAt = Instant.now();
    this.updatedBy = addedBy;

    registerEvent(new SwiftRouteAddedEvent(this.id, routeId, routeName, Instant.now()));
  }

  /** Log message */
  public void logMessage(
      ClearingMessageId messageId,
      String direction,
      String messageType,
      String payloadHash,
      Integer statusCode) {
    ClearingMessageLog log =
        ClearingMessageLog.create(
            messageId, this.id, direction, messageType, payloadHash, statusCode);
    this.messageLogs.add(log);
    this.updatedAt = Instant.now();

    registerEvent(new SwiftMessageLoggedEvent(this.id, direction, messageType, statusCode, Instant.now()));
  }

  /** Get routes */
  public List<ClearingRoute> getRoutes() {
    return new ArrayList<>(routes);
  }

  /** Get message logs */
  public List<ClearingMessageLog> getMessageLogs() {
    return new ArrayList<>(messageLogs);
  }

  /** Register a domain event */
  private void registerEvent(DomainEvent event) {
    this.domainEvents.add(event);
  }

  /** Get domain events */
  public List<DomainEvent> getDomainEvents() {
    return new ArrayList<>(this.domainEvents);
  }

  /** Clear domain events */
  public void clearDomainEvents() {
    this.domainEvents.clear();
  }

  /** Get processing window start time */
  public String getProcessingWindowStart() {
    return "09:00"; // Default processing window start
  }

  /** Get processing window end time */
  public String getProcessingWindowEnd() {
    return "17:00"; // Default processing window end
  }
}
