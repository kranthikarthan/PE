package com.payments.domain.account;

import com.payments.domain.shared.*;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.*;

/**
 * Account Adapter Aggregate Root
 *
 * <p>Manages account routing, backend system integration, and account information caching.
 */
@Entity
@Table(name = "account_adapters")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AccountAdapter {

  @EmbeddedId private AccountAdapterId id;

  @Embedded
  @AttributeOverrides({
    @AttributeOverride(name = "tenantId", column = @Column(name = "tenant_id")),
    @AttributeOverride(name = "businessUnitId", column = @Column(name = "business_unit_id"))
  })
  private TenantContext tenantContext;

  private String adapterName;

  @Enumerated(EnumType.STRING)
  private AdapterType adapterType;

  @Enumerated(EnumType.STRING)
  private AdapterStatus status;

  private String baseUrl;

  private String apiVersion;

  private String authenticationType;

  private String credentials;

  private Integer timeoutSeconds;

  private Integer retryAttempts;

  private Boolean isActive;

  private Instant createdAt;

  private Instant updatedAt;

  private String createdBy;

  private String updatedBy;

  @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
  @JoinColumn(name = "adapter_id")
  private List<AccountRoutingRule> routingRules = new ArrayList<>();

  @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
  @JoinColumn(name = "adapter_id")
  private List<BackendSystem> backendSystems = new ArrayList<>();

  @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
  @JoinColumn(name = "adapter_id")
  private List<AccountCache> accountCaches = new ArrayList<>();

  @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
  @JoinColumn(name = "adapter_id")
  private List<ApiCallLog> apiCallLogs = new ArrayList<>();

  @Transient private List<DomainEvent> domainEvents = new ArrayList<>();

  // ─────────────────────────────────────────────────────────
  // FACTORY METHOD
  // ─────────────────────────────────────────────────────────

  public static AccountAdapter create(
      AccountAdapterId id,
      TenantContext tenantContext,
      String adapterName,
      AdapterType adapterType,
      String baseUrl,
      String createdBy) {
    // Business validation
    if (adapterName == null || adapterName.isBlank()) {
      throw new InvalidAccountAdapterException("Adapter name cannot be null or blank");
    }

    if (baseUrl == null || baseUrl.isBlank()) {
      throw new InvalidAccountAdapterException("Base URL cannot be null or blank");
    }

    AccountAdapter adapter = new AccountAdapter();
    adapter.id = id;
    adapter.tenantContext = tenantContext;
    adapter.adapterName = adapterName;
    adapter.adapterType = adapterType;
    adapter.status = AdapterStatus.ACTIVE;
    adapter.baseUrl = baseUrl;
    adapter.apiVersion = "v1";
    adapter.authenticationType = "API_KEY";
    adapter.timeoutSeconds = 30;
    adapter.retryAttempts = 3;
    adapter.isActive = true;
    adapter.createdAt = Instant.now();
    adapter.updatedAt = Instant.now();
    adapter.createdBy = createdBy;
    adapter.updatedBy = createdBy;

    // Domain event
    adapter.registerEvent(
        new AccountAdapterCreatedEvent(
            adapter.id, adapter.adapterName, adapter.adapterType, adapter.createdAt));

    return adapter;
  }

  // ─────────────────────────────────────────────────────────
  // BUSINESS METHODS
  // ─────────────────────────────────────────────────────────

  /** Add a routing rule to the adapter */
  public void addRoutingRule(
      RoutingRuleId ruleId,
      String ruleName,
      String condition,
      String targetBackend,
      Integer priority,
      String addedBy) {
    AccountRoutingRule rule =
        AccountRoutingRule.create(
            ruleId, this.id, ruleName, condition, targetBackend, priority, addedBy);

    this.routingRules.add(rule);
    this.updatedAt = Instant.now();
    this.updatedBy = addedBy;

    registerEvent(new RoutingRuleAddedEvent(this.id, ruleId, ruleName, condition));
  }

  /** Add a backend system to the adapter */
  public void addBackendSystem(
      BackendSystemId systemId,
      String systemName,
      String systemType,
      String endpoint,
      String addedBy) {
    BackendSystem system =
        BackendSystem.create(systemId, this.id, systemName, systemType, endpoint, addedBy);

    this.backendSystems.add(system);
    this.updatedAt = Instant.now();
    this.updatedBy = addedBy;

    registerEvent(new BackendSystemAddedEvent(this.id, systemId, systemName, systemType));
  }

  /** Cache account information */
  public void cacheAccountInfo(
      AccountNumber accountNumber,
      String accountHolderName,
      String accountType,
      String bankCode,
      String cachedBy) {
    AccountCache cache =
        AccountCache.create(
            CacheId.generate(),
            this.id,
            accountNumber,
            accountHolderName,
            accountType,
            bankCode,
            cachedBy);

    this.accountCaches.add(cache);
    this.updatedAt = Instant.now();
    this.updatedBy = cachedBy;

    registerEvent(new AccountCachedEvent(this.id, accountNumber, accountHolderName));
  }

  /** Log API call */
  public void logApiCall(
      String endpoint, String method, Integer statusCode, Long responseTimeMs, String requestId) {
    ApiCallLog log =
        ApiCallLog.create(
            LogId.generate(), this.id, endpoint, method, statusCode, responseTimeMs, requestId);

    this.apiCallLogs.add(log);
    this.updatedAt = Instant.now();

    registerEvent(new ApiCallLoggedEvent(this.id, endpoint, method, statusCode));
  }

  /** Update adapter configuration */
  public void updateConfiguration(
      String baseUrl,
      String apiVersion,
      String authenticationType,
      Integer timeoutSeconds,
      Integer retryAttempts,
      String updatedBy) {
    this.baseUrl = baseUrl;
    this.apiVersion = apiVersion;
    this.authenticationType = authenticationType;
    this.timeoutSeconds = timeoutSeconds;
    this.retryAttempts = retryAttempts;
    this.updatedAt = Instant.now();
    this.updatedBy = updatedBy;

    registerEvent(new AdapterConfigurationUpdatedEvent(this.id, baseUrl, apiVersion));
  }

  /** Activate the adapter */
  public void activate(String activatedBy) {
    if (this.status == AdapterStatus.ACTIVE) {
      throw new InvalidAccountAdapterException("Adapter is already active");
    }

    this.status = AdapterStatus.ACTIVE;
    this.isActive = true;
    this.updatedAt = Instant.now();
    this.updatedBy = activatedBy;

    registerEvent(new AdapterActivatedEvent(this.id, activatedBy));
  }

  /** Deactivate the adapter */
  public void deactivate(String reason, String deactivatedBy) {
    if (this.status == AdapterStatus.INACTIVE) {
      throw new InvalidAccountAdapterException("Adapter is already inactive");
    }

    this.status = AdapterStatus.INACTIVE;
    this.isActive = false;
    this.updatedAt = Instant.now();
    this.updatedBy = deactivatedBy;

    registerEvent(new AdapterDeactivatedEvent(this.id, reason, deactivatedBy));
  }

  // ─────────────────────────────────────────────────────────
  // QUERY METHODS
  // ─────────────────────────────────────────────────────────

  public boolean isActive() {
    return this.status == AdapterStatus.ACTIVE && this.isActive;
  }

  public boolean hasRoutingRule(RoutingRuleId ruleId) {
    return routingRules.stream().anyMatch(rule -> rule.getId().equals(ruleId));
  }

  public boolean hasBackendSystem(BackendSystemId systemId) {
    return backendSystems.stream().anyMatch(system -> system.getId().equals(systemId));
  }

  public AccountCache getCachedAccount(AccountNumber accountNumber) {
    return accountCaches.stream()
        .filter(cache -> cache.getAccountNumber().equals(accountNumber))
        .findFirst()
        .orElse(null);
  }

  public List<ApiCallLog> getRecentApiCalls(int limit) {
    return apiCallLogs.stream()
        .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
        .limit(limit)
        .collect(java.util.stream.Collectors.toList());
  }

  public AccountAdapterId getId() {
    return id;
  }

  public String getAdapterName() {
    return adapterName;
  }

  public AdapterType getAdapterType() {
    return adapterType;
  }

  public AdapterStatus getStatus() {
    return status;
  }

  public String getBaseUrl() {
    return baseUrl;
  }

  public List<AccountRoutingRule> getRoutingRules() {
    return Collections.unmodifiableList(routingRules);
  }

  public List<BackendSystem> getBackendSystems() {
    return Collections.unmodifiableList(backendSystems);
  }

  public List<DomainEvent> getDomainEvents() {
    return Collections.unmodifiableList(domainEvents);
  }

  public void clearDomainEvents() {
    this.domainEvents.clear();
  }

  // ─────────────────────────────────────────────────────────
  // PRIVATE HELPERS
  // ─────────────────────────────────────────────────────────

  private void registerEvent(DomainEvent event) {
    this.domainEvents.add(event);
  }
}

/** Account Routing Rule (Entity within AccountAdapter Aggregate) */
@Entity
@Table(name = "account_routing_rules")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Data
class AccountRoutingRule {

  @EmbeddedId private RoutingRuleId id;

  @Embedded private AccountAdapterId adapterId;

  private String ruleName;

  private String condition;

  private String targetBackend;

  private Integer priority;

  @Enumerated(EnumType.STRING)
  private RuleStatus status;

  private Instant createdAt;

  private Instant updatedAt;

  private String createdBy;

  private String updatedBy;

  public static AccountRoutingRule create(
      RoutingRuleId id,
      AccountAdapterId adapterId,
      String ruleName,
      String condition,
      String targetBackend,
      Integer priority,
      String createdBy) {
    AccountRoutingRule rule = new AccountRoutingRule();
    rule.id = id;
    rule.adapterId = adapterId;
    rule.ruleName = ruleName;
    rule.condition = condition;
    rule.targetBackend = targetBackend;
    rule.priority = priority;
    rule.status = RuleStatus.ACTIVE;
    rule.createdAt = Instant.now();
    rule.updatedAt = Instant.now();
    rule.createdBy = createdBy;
    rule.updatedBy = createdBy;

    return rule;
  }
}

/** Backend System (Entity within AccountAdapter Aggregate) */
@Entity
@Table(name = "backend_systems")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Data
class BackendSystem {

  @EmbeddedId private BackendSystemId id;

  @Embedded private AccountAdapterId adapterId;

  private String systemName;

  private String systemType;

  private String endpoint;

  @Enumerated(EnumType.STRING)
  private SystemStatus status;

  private Instant createdAt;

  private Instant updatedAt;

  private String createdBy;

  private String updatedBy;

  public static BackendSystem create(
      BackendSystemId id,
      AccountAdapterId adapterId,
      String systemName,
      String systemType,
      String endpoint,
      String createdBy) {
    BackendSystem system = new BackendSystem();
    system.id = id;
    system.adapterId = adapterId;
    system.systemName = systemName;
    system.systemType = systemType;
    system.endpoint = endpoint;
    system.status = SystemStatus.ACTIVE;
    system.createdAt = Instant.now();
    system.updatedAt = Instant.now();
    system.createdBy = createdBy;
    system.updatedBy = createdBy;

    return system;
  }
}

/** Account Cache (Entity within AccountAdapter Aggregate) */
@Entity
@Table(name = "account_caches")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Data
class AccountCache {

  @EmbeddedId private CacheId id;

  @Embedded private AccountAdapterId adapterId;

  @Embedded private AccountNumber accountNumber;

  private String accountHolderName;

  private String accountType;

  private String bankCode;

  private Instant cachedAt;

  private Instant expiresAt;

  private String cachedBy;

  public static AccountCache create(
      CacheId id,
      AccountAdapterId adapterId,
      AccountNumber accountNumber,
      String accountHolderName,
      String accountType,
      String bankCode,
      String cachedBy) {
    AccountCache cache = new AccountCache();
    cache.id = id;
    cache.adapterId = adapterId;
    cache.accountNumber = accountNumber;
    cache.accountHolderName = accountHolderName;
    cache.accountType = accountType;
    cache.bankCode = bankCode;
    cache.cachedAt = Instant.now();
    cache.expiresAt = Instant.now().plusSeconds(3600); // 1 hour
    cache.cachedBy = cachedBy;

    return cache;
  }
}

/** API Call Log (Entity within AccountAdapter Aggregate) */
@Entity
@Table(name = "api_call_logs")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Data
class ApiCallLog {

  @EmbeddedId private LogId id;

  @Embedded private AccountAdapterId adapterId;

  private String endpoint;

  private String method;

  private Integer statusCode;

  private Long responseTimeMs;

  private String requestId;

  private Instant createdAt;

  public static ApiCallLog create(
      LogId id,
      AccountAdapterId adapterId,
      String endpoint,
      String method,
      Integer statusCode,
      Long responseTimeMs,
      String requestId) {
    ApiCallLog log = new ApiCallLog();
    log.id = id;
    log.adapterId = adapterId;
    log.endpoint = endpoint;
    log.method = method;
    log.statusCode = statusCode;
    log.responseTimeMs = responseTimeMs;
    log.requestId = requestId;
    log.createdAt = Instant.now();

    return log;
  }
}
