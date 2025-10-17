package com.payments.payshapadapter.domain;

import com.payments.domain.clearing.AdapterOperationalStatus;
import com.payments.domain.clearing.ClearingNetwork;
import com.payments.domain.shared.ClearingAdapterId;
import com.payments.domain.shared.ClearingMessageId;
import com.payments.domain.shared.ClearingRouteId;
import com.payments.domain.shared.DomainEvent;
import com.payments.domain.shared.TenantContext;
import com.payments.payshapadapter.exception.InvalidPayShapAdapterException;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * PayShap Adapter Aggregate Root
 *
 * <p>Handles instant P2P payments with proxy registry integration. Features: - Instant P2P payments
 * up to R3,000 - Proxy registry integration for mobile/email lookup - Real-time processing and
 * settlement
 */
@Entity
@Table(name = "payshap_adapters")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PayShapAdapter {

  @EmbeddedId private ClearingAdapterId id;

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

  @Column(name = "amount_limit")
  private BigDecimal amountLimit;

  @Column(name = "currency_code")
  private String currencyCode;

  @Column(name = "processing_window_start")
  private String processingWindowStart;

  @Column(name = "processing_window_end")
  private String processingWindowEnd;

  @Column(name = "proxy_registry_endpoint")
  private String proxyRegistryEndpoint;

  @Column(name = "proxy_registry_timeout")
  private Integer proxyRegistryTimeout;

  @Column(name = "proxy_registry_retry_attempts")
  private Integer proxyRegistryRetryAttempts;

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

  /** Create a new PayShap adapter */
  public static PayShapAdapter create(
      ClearingAdapterId id,
      TenantContext tenantContext,
      String adapterName,
      String endpoint,
      String createdBy) {
    if (id == null) {
      throw new InvalidPayShapAdapterException("Adapter ID cannot be null");
    }
    if (tenantContext == null) {
      throw new InvalidPayShapAdapterException("Tenant context cannot be null");
    }
    if (adapterName == null || adapterName.trim().isEmpty()) {
      throw new InvalidPayShapAdapterException("Adapter name cannot be null or empty");
    }
    if (endpoint == null || endpoint.trim().isEmpty()) {
      throw new InvalidPayShapAdapterException("Endpoint cannot be null or empty");
    }

    PayShapAdapter adapter =
        PayShapAdapter.builder()
            .id(id)
            .tenantContext(tenantContext)
            .adapterName(adapterName)
            .network(ClearingNetwork.PAYSHAP)
            .status(AdapterOperationalStatus.INACTIVE)
            .endpoint(endpoint)
            .apiVersion("1.0")
            .timeoutSeconds(5)
            .retryAttempts(3)
            .encryptionEnabled(true)
            .batchSize(50)
            .amountLimit(new BigDecimal("3000.00"))
            .currencyCode("ZAR")
            .processingWindowStart("00:00")
            .processingWindowEnd("23:59")
            .proxyRegistryTimeout(3)
            .proxyRegistryRetryAttempts(2)
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .createdBy(createdBy)
            .version(1)
            .build();

    adapter.registerEvent(
        new PayShapAdapterCreatedEvent(
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
      String processingWindowStart,
      String processingWindowEnd,
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
    if (processingWindowStart != null && !processingWindowStart.trim().isEmpty()) {
      this.processingWindowStart = processingWindowStart;
    }
    if (processingWindowEnd != null && !processingWindowEnd.trim().isEmpty()) {
      this.processingWindowEnd = processingWindowEnd;
    }

    this.updatedAt = Instant.now();
    this.updatedBy = updatedBy;
    this.version++;

    registerEvent(
        new PayShapAdapterConfigurationUpdatedEvent(
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
        new PayShapAdapterActivatedEvent(
            this.id.toString(), this.tenantContext.getTenantId(), Instant.now()));
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
        new PayShapAdapterDeactivatedEvent(
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

    registerEvent(new PayShapRouteAddedEvent(this.id, routeId, routeName, Instant.now()));
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

    registerEvent(
        new PayShapMessageLoggedEvent(this.id, direction, messageType, statusCode, Instant.now()));
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
}
