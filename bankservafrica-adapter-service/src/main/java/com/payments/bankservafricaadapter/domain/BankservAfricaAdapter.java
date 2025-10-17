package com.payments.bankservafricaadapter.domain;

import com.payments.domain.shared.TenantContext;
import com.payments.domain.shared.ClearingAdapterId;
import com.payments.domain.shared.ClearingMessageId;
import com.payments.domain.shared.ClearingRouteId;
import com.payments.domain.clearing.ClearingNetwork;
import com.payments.domain.clearing.AdapterOperationalStatus;
import com.payments.domain.shared.DomainEvent;
import com.payments.bankservafricaadapter.exception.InvalidBankservAfricaAdapterException;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * BankservAfrica Adapter Aggregate Root
 *
 * <p>Integrates with BankservAfrica clearing network for:
 * - EFT batch processing
 * - ISO 8583 message handling
 * - ACH integration
 * - Multi-tenant support
 */
@Entity
@Table(name = "bankservafrica_adapters")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BankservAfricaAdapter {

    @EmbeddedId
    private ClearingAdapterId id;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "tenantId", column = @Column(name = "tenant_id")),
        @AttributeOverride(name = "businessUnitId", column = @Column(name = "business_unit_id"))
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

    @Column(name = "api_version", nullable = false)
    private String apiVersion;

    @Column(name = "timeout_seconds", nullable = false)
    private Integer timeoutSeconds;

    @Column(name = "retry_attempts", nullable = false)
    private Integer retryAttempts;

    @Column(name = "encryption_enabled", nullable = false)
    private Boolean encryptionEnabled;

    @Column(name = "batch_size", nullable = false)
    private Integer batchSize;

    @Column(name = "processing_window_start")
    private String processingWindowStart;

    @Column(name = "processing_window_end")
    private String processingWindowEnd;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "created_by", nullable = false)
    private String createdBy;

    @Column(name = "updated_by", nullable = false)
    private String updatedBy;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "bankservafrica_adapter_id")
    private List<BankservAfricaEftMessage> eftMessages = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "bankservafrica_adapter_id")
    private List<BankservAfricaIso8583Message> iso8583Messages = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "bankservafrica_adapter_id")
    private List<BankservAfricaAchTransaction> achTransactions = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "bankservafrica_adapter_id")
    private List<BankservAfricaTransactionLog> transactionLogs = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "bankservafrica_adapter_id")
    private List<BankservAfricaSettlementRecord> settlementRecords = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "clearing_adapter_id")
    private List<ClearingRoute> routes = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "clearing_adapter_id")
    private List<ClearingMessageLog> messageLogs = new ArrayList<>();

    @Transient
    private List<DomainEvent> domainEvents = new ArrayList<>();

    /**
     * Create a new BankservAfrica adapter
     */
    public static BankservAfricaAdapter create(
            ClearingAdapterId id,
            TenantContext tenantContext,
            String adapterName,
            ClearingNetwork network,
            String endpoint,
            String createdBy) {
        
        if (adapterName == null || adapterName.isBlank()) {
            throw new InvalidBankservAfricaAdapterException("Adapter name cannot be null or blank");
        }
        if (endpoint == null || endpoint.isBlank()) {
            throw new InvalidBankservAfricaAdapterException("Endpoint cannot be null or blank");
        }

        BankservAfricaAdapter adapter = new BankservAfricaAdapter();
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
        adapter.batchSize = 1000;
        adapter.processingWindowStart = "06:00";
        adapter.processingWindowEnd = "18:00";
        adapter.createdAt = Instant.now();
        adapter.updatedAt = Instant.now();
        adapter.createdBy = createdBy;
        adapter.updatedBy = createdBy;

        adapter.registerEvent(new BankservAfricaAdapterCreatedEvent(
                adapter.id, adapter.adapterName, adapter.network, adapter.createdAt));

        return adapter;
    }

    /**
     * Update adapter configuration
     */
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

        registerEvent(new BankservAfricaAdapterConfigurationUpdatedEvent(
                this.id, endpoint, apiVersion, Instant.now()));
    }

    /**
     * Activate the adapter
     */
    public void activate(String activatedBy) {
        if (this.status == AdapterOperationalStatus.ACTIVE) {
            throw new InvalidBankservAfricaAdapterException("Adapter already active");
        }
        this.status = AdapterOperationalStatus.ACTIVE;
        this.updatedAt = Instant.now();
        this.updatedBy = activatedBy;

        registerEvent(new BankservAfricaAdapterActivatedEvent(this.id, activatedBy, Instant.now()));
    }

    /**
     * Deactivate the adapter
     */
    public void deactivate(String reason, String deactivatedBy) {
        if (this.status == AdapterOperationalStatus.INACTIVE) {
            throw new InvalidBankservAfricaAdapterException("Adapter already inactive");
        }
        this.status = AdapterOperationalStatus.INACTIVE;
        this.updatedAt = Instant.now();
        this.updatedBy = deactivatedBy;

        registerEvent(new BankservAfricaAdapterDeactivatedEvent(this.id, reason, deactivatedBy, Instant.now()));
    }

    /**
     * Add EFT message
     */
    public void addEftMessage(BankservAfricaEftMessage eftMessage) {
        this.eftMessages.add(eftMessage);
        this.updatedAt = Instant.now();
        
        registerEvent(new BankservAfricaEftMessageAddedEvent(
                this.id, eftMessage.getId().toString(), eftMessage.getBatchId(), Instant.now()));
    }

    /**
     * Add ISO 8583 message
     */
    public void addIso8583Message(BankservAfricaIso8583Message iso8583Message) {
        this.iso8583Messages.add(iso8583Message);
        this.updatedAt = Instant.now();
        
        registerEvent(new BankservAfricaIso8583MessageAddedEvent(
                this.id, iso8583Message.getId().toString(), iso8583Message.getTransactionId(), Instant.now()));
    }

    /**
     * Add ACH transaction
     */
    public void addAchTransaction(BankservAfricaAchTransaction achTransaction) {
        this.achTransactions.add(achTransaction);
        this.updatedAt = Instant.now();
        
        registerEvent(new BankservAfricaAchTransactionAddedEvent(
                this.id, achTransaction.getId().toString(), achTransaction.getAchBatchId(), Instant.now()));
    }

    /**
     * Add transaction log
     */
    public void addTransactionLog(BankservAfricaTransactionLog transactionLog) {
        this.transactionLogs.add(transactionLog);
        this.updatedAt = Instant.now();
        
        registerEvent(new BankservAfricaTransactionLoggedEvent(
                this.id, transactionLog.getTransactionId(), transactionLog.getStatus(), Instant.now()));
    }

    /**
     * Add settlement record
     */
    public void addSettlementRecord(BankservAfricaSettlementRecord settlementRecord) {
        this.settlementRecords.add(settlementRecord);
        this.updatedAt = Instant.now();
        
        registerEvent(new BankservAfricaSettlementRecordAddedEvent(
                this.id, settlementRecord.getId().toString(), settlementRecord.getSettlementDate(), Instant.now()));
    }

    /**
     * Check if adapter is active
     */
    public boolean isActive() {
        return this.status == AdapterOperationalStatus.ACTIVE;
    }

    /**
     * Get EFT messages
     */
    public List<BankservAfricaEftMessage> getEftMessages() {
        return Collections.unmodifiableList(eftMessages);
    }

    /**
     * Get ISO 8583 messages
     */
    public List<BankservAfricaIso8583Message> getIso8583Messages() {
        return Collections.unmodifiableList(iso8583Messages);
    }

    /**
     * Get ACH transactions
     */
    public List<BankservAfricaAchTransaction> getAchTransactions() {
        return Collections.unmodifiableList(achTransactions);
    }

    /**
     * Get transaction logs
     */
    public List<BankservAfricaTransactionLog> getTransactionLogs() {
        return Collections.unmodifiableList(transactionLogs);
    }

    /**
     * Get settlement records
     */
    public List<BankservAfricaSettlementRecord> getSettlementRecords() {
        return Collections.unmodifiableList(settlementRecords);
    }

    /**
     * Get domain events
     */
    public List<DomainEvent> getDomainEvents() {
        return Collections.unmodifiableList(domainEvents);
    }

    /**
     * Clear domain events
     */
    public void clearDomainEvents() {
        this.domainEvents.clear();
    }

    /**
     * Add route
     */
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

        registerEvent(new BankservAfricaRouteAddedEvent(this.id, routeId, routeName, Instant.now()));
    }

    /**
     * Log message
     */
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

        registerEvent(new BankservAfricaMessageLoggedEvent(this.id, direction, messageType, statusCode, Instant.now()));
    }

    /**
     * Get routes
     */
    public List<ClearingRoute> getRoutes() {
        return Collections.unmodifiableList(routes);
    }

    /**
     * Get message logs
     */
    public List<ClearingMessageLog> getMessageLogs() {
        return Collections.unmodifiableList(messageLogs);
    }

    /**
     * Register domain event
     */
    private void registerEvent(DomainEvent event) {
        this.domainEvents.add(event);
    }

    // Getters for JPA
    public ClearingAdapterId getId() { return id; }
    public TenantContext getTenantContext() { return tenantContext; }
    public String getAdapterName() { return adapterName; }
    public ClearingNetwork getNetwork() { return network; }
    public AdapterOperationalStatus getStatus() { return status; }
    public String getEndpoint() { return endpoint; }
    public String getApiVersion() { return apiVersion; }
    public Integer getTimeoutSeconds() { return timeoutSeconds; }
    public Integer getRetryAttempts() { return retryAttempts; }
    public Boolean getEncryptionEnabled() { return encryptionEnabled; }
    public Integer getBatchSize() { return batchSize; }
    public String getProcessingWindowStart() { return processingWindowStart; }
    public String getProcessingWindowEnd() { return processingWindowEnd; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public String getCreatedBy() { return createdBy; }
    public String getUpdatedBy() { return updatedBy; }
}
