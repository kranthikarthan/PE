package com.payments.domain.clearing;

import com.payments.domain.shared.*;
import com.payments.domain.shared.ClearingMessageId;
import com.payments.domain.shared.ClearingRouteId;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.*;

/**
 * Clearing Adapter Aggregate Root
 *
 * <p>Integrates with external clearing/settlement networks (e.g., SAMOS, BankservAfrica, RTC,
 * PayShap, SWIFT).
 */
@Entity
@Table(name = "clearing_adapters")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ClearingAdapter {

  @EmbeddedId private ClearingAdapterId id;

  @Embedded
  @AttributeOverrides({
    @AttributeOverride(name = "tenantId", column = @Column(name = "tenant_id")),
    @AttributeOverride(name = "businessUnitId", column = @Column(name = "business_unit_id"))
  })
  private TenantContext tenantContext;

  private String adapterName;

  @Enumerated(EnumType.STRING)
  private ClearingNetwork network;

  @Enumerated(EnumType.STRING)
  private AdapterOperationalStatus status;

  private String endpoint;

  private String apiVersion;

  private Integer timeoutSeconds;

  private Integer retryAttempts;

  private Boolean encryptionEnabled;

  private Instant createdAt;

  private Instant updatedAt;

  private String createdBy;

  private String updatedBy;

  @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
  @JoinColumn(name = "clearing_adapter_id")
  private List<ClearingRoute> routes = new ArrayList<>();

  @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
  @JoinColumn(name = "clearing_adapter_id")
  private List<ClearingMessageLog> messageLogs = new ArrayList<>();

  @Transient private List<DomainEvent> domainEvents = new ArrayList<>();

  public static ClearingAdapter create(
      ClearingAdapterId id,
      TenantContext tenantContext,
      String adapterName,
      ClearingNetwork network,
      String endpoint,
      String createdBy) {
    if (adapterName == null || adapterName.isBlank()) {
      throw new InvalidClearingAdapterException("Adapter name cannot be null or blank");
    }
    if (endpoint == null || endpoint.isBlank()) {
      throw new InvalidClearingAdapterException("Endpoint cannot be null or blank");
    }

    ClearingAdapter adapter = new ClearingAdapter();
    adapter.id = id;
    adapter.tenantContext = tenantContext;
    adapter.adapterName = adapterName;
    adapter.network = network;
    adapter.status = AdapterOperationalStatus.ACTIVE;
    adapter.endpoint = endpoint;
    adapter.apiVersion = "v1";
    adapter.timeoutSeconds = 30;
    adapter.retryAttempts = 3;
    adapter.encryptionEnabled = true;
    adapter.createdAt = Instant.now();
    adapter.updatedAt = Instant.now();
    adapter.createdBy = createdBy;
    adapter.updatedBy = createdBy;

    adapter.registerEvent(
        new ClearingAdapterCreatedEvent(
            adapter.id, adapter.adapterName, adapter.network, adapter.createdAt));

    return adapter;
  }

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

    registerEvent(new ClearingRouteAddedEvent(this.id, routeId, routeName));
  }

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

    registerEvent(new ClearingMessageLoggedEvent(this.id, direction, messageType, statusCode));
  }

  public void updateConfiguration(
      String endpoint,
      String apiVersion,
      Integer timeoutSeconds,
      Integer retryAttempts,
      Boolean encryptionEnabled,
      String updatedBy) {
    this.endpoint = endpoint;
    this.apiVersion = apiVersion;
    this.timeoutSeconds = timeoutSeconds;
    this.retryAttempts = retryAttempts;
    this.encryptionEnabled = encryptionEnabled;
    this.updatedAt = Instant.now();
    this.updatedBy = updatedBy;

    registerEvent(new ClearingAdapterConfigurationUpdatedEvent(this.id, endpoint, apiVersion));
  }

  public void activate(String activatedBy) {
    if (this.status == AdapterOperationalStatus.ACTIVE) {
      throw new InvalidClearingAdapterException("Adapter already active");
    }
    this.status = AdapterOperationalStatus.ACTIVE;
    this.updatedAt = Instant.now();

    registerEvent(new ClearingAdapterActivatedEvent(this.id, activatedBy));
  }

  public void deactivate(String reason, String deactivatedBy) {
    if (this.status == AdapterOperationalStatus.INACTIVE) {
      throw new InvalidClearingAdapterException("Adapter already inactive");
    }
    this.status = AdapterOperationalStatus.INACTIVE;
    this.updatedAt = Instant.now();

    registerEvent(new ClearingAdapterDeactivatedEvent(this.id, reason, deactivatedBy));
  }

  public boolean isActive() {
    return this.status == AdapterOperationalStatus.ACTIVE;
  }

  public List<ClearingRoute> getRoutes() {
    return Collections.unmodifiableList(routes);
  }

  public List<ClearingMessageLog> getMessageLogs() {
    return Collections.unmodifiableList(messageLogs);
  }

  public List<DomainEvent> getDomainEvents() {
    return Collections.unmodifiableList(domainEvents);
  }

  public void clearDomainEvents() {
    this.domainEvents.clear();
  }

  private void registerEvent(DomainEvent event) {
    this.domainEvents.add(event);
  }
}

@Entity
@Table(name = "clearing_routes")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Data
class ClearingRoute {

  @EmbeddedId private ClearingRouteId id;

  @Embedded private ClearingAdapterId clearingAdapterId;

  private String routeName;
  private String source;
  private String destination;
  private Integer priority;

  @Enumerated(EnumType.STRING)
  private RouteStatus status;

  private Instant createdAt;
  private Instant updatedAt;
  private String createdBy;
  private String updatedBy;

  public static ClearingRoute create(
      ClearingRouteId id,
      ClearingAdapterId clearingAdapterId,
      String routeName,
      String source,
      String destination,
      Integer priority,
      String createdBy) {
    ClearingRoute route = new ClearingRoute();
    route.id = id;
    route.clearingAdapterId = clearingAdapterId;
    route.routeName = routeName;
    route.source = source;
    route.destination = destination;
    route.priority = priority;
    route.status = RouteStatus.ACTIVE;
    route.createdAt = Instant.now();
    route.updatedAt = Instant.now();
    route.createdBy = createdBy;
    route.updatedBy = createdBy;
    return route;
  }
}

@Entity
@Table(name = "clearing_message_logs")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Data
class ClearingMessageLog {

  @EmbeddedId private ClearingMessageId id;

  @Embedded private ClearingAdapterId clearingAdapterId;

  private String direction; // INBOUND / OUTBOUND
  private String messageType; // e.g. pacs.008, pain.001
  private String payloadHash;
  private Integer statusCode;
  private Instant createdAt;

  public static ClearingMessageLog create(
      ClearingMessageId id,
      ClearingAdapterId clearingAdapterId,
      String direction,
      String messageType,
      String payloadHash,
      Integer statusCode) {
    ClearingMessageLog log = new ClearingMessageLog();
    log.id = id;
    log.clearingAdapterId = clearingAdapterId;
    log.direction = direction;
    log.messageType = messageType;
    log.payloadHash = payloadHash;
    log.statusCode = statusCode;
    log.createdAt = Instant.now();
    return log;
  }
}
