package com.payments.swiftadapter.fixtures;

import com.payments.domain.clearing.AdapterOperationalStatus;
import com.payments.domain.clearing.ClearingNetwork;
import com.payments.domain.shared.ClearingAdapterId;
import com.payments.domain.shared.TenantContext;
import com.payments.swiftadapter.domain.SwiftAdapter;
import java.time.Instant;

/**
 * Test data builder for SwiftAdapter
 * 
 * Provides a fluent API for creating test instances of SwiftAdapter with sensible defaults.
 * This eliminates hardcoded test data and improves maintainability.
 * 
 * Example usage:
 * <pre>
 * SwiftAdapter adapter = SwiftAdapterTestDataBuilder.aSwiftAdapter()
 *     .withAdapterName("Custom Name")
 *     .withEndpoint("https://custom.endpoint.com")
 *     .build();
 * </pre>
 */
public class SwiftAdapterTestDataBuilder {

  private ClearingAdapterId id;
  private String adapterName;
  private AdapterOperationalStatus status;
  private ClearingNetwork network;
  private String endpoint;
  private String apiVersion;
  private Integer timeoutSeconds;
  private Integer retryAttempts;
  private Boolean encryptionEnabled;
  private TenantContext tenantContext;
  private String createdBy;
  private String updatedBy;
  private Instant createdAt;
  private Instant updatedAt;

  private SwiftAdapterTestDataBuilder() {
    this.id = ClearingAdapterId.of("swift-adapter-test-001");
    this.adapterName = "Test SWIFT Adapter";
    this.status = AdapterOperationalStatus.ACTIVE;
    this.network = ClearingNetwork.SWIFT;
    this.endpoint = "https://test.swift.com/api/v1";
    this.apiVersion = "1.0";
    this.timeoutSeconds = 30;
    this.retryAttempts = 3;
    this.encryptionEnabled = true;
    this.tenantContext = TenantContextTestDataBuilder.aTenantContext().build();
    this.createdBy = "test-user";
    this.updatedBy = "test-user";
    this.createdAt = Instant.now();
    this.updatedAt = Instant.now();
  }

  public static SwiftAdapterTestDataBuilder aSwiftAdapter() {
    return new SwiftAdapterTestDataBuilder();
  }

  public SwiftAdapterTestDataBuilder withId(ClearingAdapterId id) {
    this.id = id;
    return this;
  }

  public SwiftAdapterTestDataBuilder withAdapterName(String adapterName) {
    this.adapterName = adapterName;
    return this;
  }

  public SwiftAdapterTestDataBuilder withStatus(AdapterOperationalStatus status) {
    this.status = status;
    return this;
  }

  public SwiftAdapterTestDataBuilder withNetwork(ClearingNetwork network) {
    this.network = network;
    return this;
  }

  public SwiftAdapterTestDataBuilder withEndpoint(String endpoint) {
    this.endpoint = endpoint;
    return this;
  }

  public SwiftAdapterTestDataBuilder withApiVersion(String apiVersion) {
    this.apiVersion = apiVersion;
    return this;
  }

  public SwiftAdapterTestDataBuilder withTimeoutSeconds(Integer timeoutSeconds) {
    this.timeoutSeconds = timeoutSeconds;
    return this;
  }

  public SwiftAdapterTestDataBuilder withRetryAttempts(Integer retryAttempts) {
    this.retryAttempts = retryAttempts;
    return this;
  }

  public SwiftAdapterTestDataBuilder withEncryptionEnabled(Boolean encryptionEnabled) {
    this.encryptionEnabled = encryptionEnabled;
    return this;
  }

  public SwiftAdapterTestDataBuilder withTenantContext(TenantContext tenantContext) {
    this.tenantContext = tenantContext;
    return this;
  }

  public SwiftAdapterTestDataBuilder withCreatedBy(String createdBy) {
    this.createdBy = createdBy;
    return this;
  }

  public SwiftAdapterTestDataBuilder withUpdatedBy(String updatedBy) {
    this.updatedBy = updatedBy;
    return this;
  }

  public SwiftAdapterTestDataBuilder inactive() {
    this.status = AdapterOperationalStatus.INACTIVE;
    return this;
  }

  public SwiftAdapterTestDataBuilder active() {
    this.status = AdapterOperationalStatus.ACTIVE;
    return this;
  }

  public SwiftAdapter build() {
    return SwiftAdapter.builder()
        .id(id)
        .adapterName(adapterName)
        .status(status)
        .network(network)
        .endpoint(endpoint)
        .apiVersion(apiVersion)
        .timeoutSeconds(timeoutSeconds)
        .retryAttempts(retryAttempts)
        .encryptionEnabled(encryptionEnabled)
        .tenantContext(tenantContext)
        .createdBy(createdBy)
        .updatedBy(updatedBy)
        .createdAt(createdAt)
        .updatedAt(updatedAt)
        .build();
  }
}
