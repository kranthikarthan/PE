package com.payments.rtcadapter.domain;

import com.payments.domain.clearing.AdapterOperationalStatus;
import com.payments.domain.clearing.ClearingNetwork;
import com.payments.domain.shared.ClearingAdapterId;
import com.payments.domain.shared.ClearingMessageId;
import com.payments.domain.shared.ClearingRouteId;
import com.payments.domain.shared.DomainEvent;
import com.payments.domain.shared.TenantContext;
import com.payments.rtcadapter.exception.InvalidRtcAdapterException;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.*;

/**
 * RTC Adapter Aggregate Root
 *
 * <p>Integrates with BankservAfrica's RTC (Real-Time Clearing) for instant low-value payments: -
 * Real-time settlement (seconds) - Amount limit: R5,000 per transaction - 24/7/365 availability -
 * ISO 20022 messaging (pacs.008/pacs.002) - REST API protocol
 */
@Entity
@Table(name = "rtc_adapters")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RtcAdapter {

  @EmbeddedId private ClearingAdapterId id;

  @Embedded private TenantContext tenantContext;

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

  @Column(name = "processing_window_start")
  private String processingWindowStart;

  @Column(name = "processing_window_end")
  private String processingWindowEnd;

  @Column(name = "amount_limit")
  private Double amountLimit;

  @Column(name = "currency_code")
  private String currencyCode;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  @Column(name = "created_by", nullable = false)
  private String createdBy;

  @Column(name = "updated_by")
  private String updatedBy;

  @OneToMany(mappedBy = "rtcAdapter", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  @Builder.Default
  private List<RtcPaymentMessage> paymentMessages = new ArrayList<>();

  @OneToMany(mappedBy = "rtcAdapter", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  @Builder.Default
  private List<RtcTransactionLog> transactionLogs = new ArrayList<>();

  @OneToMany(mappedBy = "rtcAdapter", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  @Builder.Default
  private List<RtcSettlementRecord> settlementRecords = new ArrayList<>();

  @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
  @JoinColumn(name = "clearing_adapter_id")
  @Builder.Default
  private List<ClearingRoute> routes = new ArrayList<>();

  @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
  @JoinColumn(name = "clearing_adapter_id")
  @Builder.Default
  private List<ClearingMessageLog> messageLogs = new ArrayList<>();

  @Transient @Builder.Default private List<DomainEvent> domainEvents = new ArrayList<>();

  /** Create a new RTC adapter */
  public static RtcAdapter create(
      ClearingAdapterId id,
      TenantContext tenantContext,
      String adapterName,
      String endpoint,
      String createdBy) {
    if (id == null) {
      throw new InvalidRtcAdapterException("Adapter ID cannot be null");
    }
    if (tenantContext == null) {
      throw new InvalidRtcAdapterException("Tenant context cannot be null");
    }
    if (adapterName == null || adapterName.trim().isEmpty()) {
      throw new InvalidRtcAdapterException("Adapter name cannot be null or empty");
    }
    if (endpoint == null || endpoint.trim().isEmpty()) {
      throw new InvalidRtcAdapterException("Endpoint cannot be null or empty");
    }

    RtcAdapter adapter =
        RtcAdapter.builder()
            .id(id)
            .tenantContext(tenantContext)
            .adapterName(adapterName)
            .network(ClearingNetwork.RTC)
            .status(AdapterOperationalStatus.ACTIVE)
            .endpoint(endpoint)
            .apiVersion("1.0")
            .timeoutSeconds(10)
            .retryAttempts(3)
            .encryptionEnabled(true)
            .batchSize(100)
            .amountLimit(5000.00)
            .currencyCode("ZAR")
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .createdBy(createdBy)
            .build();

    adapter.registerEvent(
        new RtcAdapterCreatedEvent(
            adapter.id, adapter.adapterName, adapter.network, Instant.now()));

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
    if (this.status == AdapterOperationalStatus.MAINTENANCE) {
      throw new InvalidRtcAdapterException("Cannot update adapter in maintenance mode");
    }

    this.endpoint = endpoint;
    this.apiVersion = apiVersion;
    this.timeoutSeconds = timeoutSeconds;
    this.retryAttempts = retryAttempts;
    this.encryptionEnabled = encryptionEnabled;
    this.batchSize = batchSize;
    this.processingWindowStart = processingWindowStart;
    this.processingWindowEnd = processingWindowEnd;
    this.updatedAt = Instant.now();
    this.updatedBy = updatedBy;

    registerEvent(
        new RtcAdapterConfigurationUpdatedEvent(this.id, endpoint, apiVersion, Instant.now()));
  }

  /** Activate the adapter */
  public void activate(String activatedBy) {
    if (this.status == AdapterOperationalStatus.ACTIVE) {
      throw new InvalidRtcAdapterException("Adapter already active");
    }
    this.status = AdapterOperationalStatus.ACTIVE;
    this.updatedAt = Instant.now();
    this.updatedBy = activatedBy;

    registerEvent(new RtcAdapterActivatedEvent(this.id, activatedBy, Instant.now()));
  }

  /** Deactivate the adapter */
  public void deactivate(String reason, String deactivatedBy) {
    if (this.status == AdapterOperationalStatus.INACTIVE) {
      throw new InvalidRtcAdapterException("Adapter already inactive");
    }
    this.status = AdapterOperationalStatus.INACTIVE;
    this.updatedAt = Instant.now();
    this.updatedBy = deactivatedBy;

    registerEvent(new RtcAdapterDeactivatedEvent(this.id, reason, deactivatedBy, Instant.now()));
  }

  /** Add payment message */
  public void addPaymentMessage(RtcPaymentMessage paymentMessage) {
    this.paymentMessages.add(paymentMessage);
    this.updatedAt = Instant.now();

    registerEvent(
        new RtcPaymentMessageAddedEvent(
            this.id,
            paymentMessage.getId().toString(),
            paymentMessage.getTransactionId(),
            Instant.now()));
  }

  /** Add transaction log */
  public void addTransactionLog(RtcTransactionLog transactionLog) {
    this.transactionLogs.add(transactionLog);
    this.updatedAt = Instant.now();

    registerEvent(
        new RtcTransactionLoggedEvent(
            this.id, transactionLog.getTransactionId(), transactionLog.getStatus(), Instant.now()));
  }

  /** Add settlement record */
  public void addSettlementRecord(RtcSettlementRecord settlementRecord) {
    this.settlementRecords.add(settlementRecord);
    this.updatedAt = Instant.now();

    registerEvent(
        new RtcSettlementRecordAddedEvent(
            this.id,
            settlementRecord.getId().toString(),
            settlementRecord.getSettlementDate(),
            Instant.now()));
  }

  /** Check if adapter is active */
  public boolean isActive() {
    return this.status == AdapterOperationalStatus.ACTIVE;
  }

  /** Check if adapter is in maintenance */
  public boolean isInMaintenance() {
    return this.status == AdapterOperationalStatus.MAINTENANCE;
  }

  /** Validate amount against limit */
  public boolean isAmountWithinLimit(Double amount) {
    return amount != null && amount > 0 && amount <= this.amountLimit;
  }

  /** Get domain events */
  public List<DomainEvent> getDomainEvents() {
    return Collections.unmodifiableList(domainEvents);
  }

  /** Clear domain events */
  public void clearDomainEvents() {
    this.domainEvents.clear();
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

    registerEvent(new RtcRouteAddedEvent(this.id, routeId, routeName, Instant.now()));
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

    registerEvent(new RtcMessageLoggedEvent(this.id, direction, messageType, statusCode, Instant.now()));
  }

  /** Get routes */
  public List<ClearingRoute> getRoutes() {
    return Collections.unmodifiableList(routes);
  }

  /** Get message logs */
  public List<ClearingMessageLog> getMessageLogs() {
    return Collections.unmodifiableList(messageLogs);
  }

  /** Register domain event */
  private void registerEvent(DomainEvent event) {
    this.domainEvents.add(event);
  }

  // JPA getters for embedded fields
  public String getTenantId() {
    return tenantContext != null ? tenantContext.getTenantId() : null;
  }

  public String getBusinessUnitId() {
    return tenantContext != null ? tenantContext.getBusinessUnitId() : null;
  }
}
