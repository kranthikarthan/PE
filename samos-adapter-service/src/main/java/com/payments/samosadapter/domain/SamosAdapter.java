package com.payments.samosadapter.domain;

import com.payments.domain.clearing.AdapterOperationalStatus;
import com.payments.domain.clearing.ClearingNetwork;
import com.payments.domain.shared.ClearingAdapterId;
import com.payments.domain.shared.ClearingMessageId;
import com.payments.domain.shared.ClearingRouteId;
import com.payments.domain.shared.DomainEvent;
import com.payments.domain.shared.TenantContext;
import com.payments.samosadapter.exception.InvalidSamosAdapterException;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.*;

/**
 * SAMOS Adapter Entity
 *
 * <p>Represents a SAMOS clearing adapter configuration for high-value RTGS payments to the South
 * African Reserve Bank (SARB).
 */
@Entity
@Table(name = "samos_adapters")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@EqualsAndHashCode(of = "id")
@ToString
public class SamosAdapter {

  @EmbeddedId private ClearingAdapterId id;

  @Embedded
  @AttributeOverrides({
    @AttributeOverride(name = "tenantId", column = @Column(name = "tenant_id")),
    @AttributeOverride(name = "businessUnitId", column = @Column(name = "business_unit_id"))
  })
  private TenantContext tenantContext;

  @Column(name = "adapter_name", nullable = false)
  private String adapterName;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private ClearingNetwork network = ClearingNetwork.SAMOS;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private AdapterOperationalStatus status = AdapterOperationalStatus.ACTIVE;

  @Column(nullable = false)
  private String endpoint;

  @Column(name = "api_version", nullable = false)
  private String apiVersion = "v1";

  @Column(name = "timeout_seconds", nullable = false)
  private Integer timeoutSeconds = 30;

  @Column(name = "retry_attempts", nullable = false)
  private Integer retryAttempts = 3;

  @Column(name = "encryption_enabled", nullable = false)
  private Boolean encryptionEnabled = true;

  @Column(name = "certificate_path")
  private String certificatePath;

  @Column(name = "certificate_password")
  private String certificatePassword;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  @Column(name = "created_by", nullable = false)
  private String createdBy;

  @Column(name = "updated_by", nullable = false)
  private String updatedBy;

  @OneToMany(mappedBy = "samosAdapter", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<SamosPaymentMessage> paymentMessages = new ArrayList<>();

  @OneToMany(mappedBy = "samosAdapter", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<SamosTransactionLog> transactionLogs = new ArrayList<>();

  @OneToMany(mappedBy = "samosAdapter", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<SamosSettlementRecord> settlementRecords = new ArrayList<>();

  @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
  @JoinColumn(name = "clearing_adapter_id")
  private List<ClearingRoute> routes = new ArrayList<>();

  @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
  @JoinColumn(name = "clearing_adapter_id")
  private List<ClearingMessageLog> messageLogs = new ArrayList<>();

  @Transient private List<DomainEvent> domainEvents = new ArrayList<>();

  public static SamosAdapter create(
      ClearingAdapterId id,
      TenantContext tenantContext,
      String adapterName,
      String endpoint,
      String createdBy) {

    if (adapterName == null || adapterName.isBlank()) {
      throw new InvalidSamosAdapterException("Adapter name cannot be null or blank");
    }
    if (endpoint == null || endpoint.isBlank()) {
      throw new InvalidSamosAdapterException("Endpoint cannot be null or blank");
    }

    SamosAdapter adapter = new SamosAdapter();
    adapter.id = id;
    adapter.tenantContext = tenantContext;
    adapter.adapterName = adapterName;
    adapter.endpoint = endpoint;
    adapter.createdAt = Instant.now();
    adapter.updatedAt = Instant.now();
    adapter.createdBy = createdBy;
    adapter.updatedBy = createdBy;

    adapter.registerEvent(
        new SamosAdapterCreatedEvent(
            adapter.id, adapter.adapterName, adapter.network, adapter.createdAt));

    return adapter;
  }

  public void updateConfiguration(
      String endpoint,
      String apiVersion,
      Integer timeoutSeconds,
      Integer retryAttempts,
      Boolean encryptionEnabled,
      String certificatePath,
      String certificatePassword,
      String updatedBy) {

    this.endpoint = endpoint;
    this.apiVersion = apiVersion;
    this.timeoutSeconds = timeoutSeconds;
    this.retryAttempts = retryAttempts;
    this.encryptionEnabled = encryptionEnabled;
    this.certificatePath = certificatePath;
    this.certificatePassword = certificatePassword;
    this.updatedAt = Instant.now();
    this.updatedBy = updatedBy;

    registerEvent(
        new SamosAdapterConfigurationUpdatedEvent(this.id, endpoint, apiVersion, Instant.now()));
  }

  public void activate(String activatedBy) {
    if (this.status == AdapterOperationalStatus.ACTIVE) {
      throw new InvalidSamosAdapterException("Adapter already active");
    }
    this.status = AdapterOperationalStatus.ACTIVE;
    this.updatedAt = Instant.now();
    this.updatedBy = activatedBy;

    registerEvent(new SamosAdapterActivatedEvent(this.id, activatedBy, Instant.now()));
  }

  public void deactivate(String reason, String deactivatedBy) {
    if (this.status == AdapterOperationalStatus.INACTIVE) {
      throw new InvalidSamosAdapterException("Adapter already inactive");
    }
    this.status = AdapterOperationalStatus.INACTIVE;
    this.updatedAt = Instant.now();
    this.updatedBy = deactivatedBy;

    registerEvent(new SamosAdapterDeactivatedEvent(this.id, reason, deactivatedBy, Instant.now()));
  }

  public boolean isActive() {
    return this.status == AdapterOperationalStatus.ACTIVE;
  }

  public List<SamosPaymentMessage> getPaymentMessages() {
    return Collections.unmodifiableList(paymentMessages);
  }

  public List<SamosTransactionLog> getTransactionLogs() {
    return Collections.unmodifiableList(transactionLogs);
  }

  public List<SamosSettlementRecord> getSettlementRecords() {
    return Collections.unmodifiableList(settlementRecords);
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

    registerEvent(new SamosRouteAddedEvent(this.id, routeId, routeName, Instant.now()));
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

    registerEvent(
        new SamosMessageLoggedEvent(this.id, direction, messageType, statusCode, Instant.now()));
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

  // Getters for JPA
  public ClearingAdapterId getId() {
    return id;
  }

  public TenantContext getTenantContext() {
    return tenantContext;
  }

  public String getAdapterName() {
    return adapterName;
  }

  public ClearingNetwork getNetwork() {
    return network;
  }

  public AdapterOperationalStatus getStatus() {
    return status;
  }

  public String getEndpoint() {
    return endpoint;
  }

  public String getApiVersion() {
    return apiVersion;
  }

  public Integer getTimeoutSeconds() {
    return timeoutSeconds;
  }

  public Integer getRetryAttempts() {
    return retryAttempts;
  }

  public Boolean getEncryptionEnabled() {
    return encryptionEnabled;
  }

  public String getCertificatePath() {
    return certificatePath;
  }

  public String getCertificatePassword() {
    return certificatePassword;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }

  public String getCreatedBy() {
    return createdBy;
  }

  public String getUpdatedBy() {
    return updatedBy;
  }
}
