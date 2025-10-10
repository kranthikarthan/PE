package com.paymentengine.paymentprocessing.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Idempotency Key Entity
 * Tracks processed requests to prevent duplicate processing
 */
@Entity
@Table(name = "idempotency_keys", indexes = {
    @Index(name = "idx_idempotency_key", columnList = "idempotencyKey", unique = true),
    @Index(name = "idx_idempotency_tenant", columnList = "tenantId"),
    @Index(name = "idx_idempotency_expires", columnList = "expiresAt"),
    @Index(name = "idx_idempotency_hash", columnList = "requestHash")
})
public class IdempotencyKey {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 255)
    private String idempotencyKey;

    @Column(nullable = false, length = 100)
    private String tenantId;

    @Column(nullable = false, length = 500)
    private String endpoint;

    @Column(nullable = false, length = 64)
    private String requestHash;

    @Column(length = 10)
    private String httpMethod;

    @Column(columnDefinition = "jsonb")
    private Map<String, Object> requestBody;

    @Column(columnDefinition = "jsonb")
    private Map<String, Object> requestHeaders;

    private Integer responseStatus;

    @Column(columnDefinition = "jsonb")
    private Map<String, Object> responseBody;

    @Column(columnDefinition = "jsonb")
    private Map<String, Object> responseHeaders;

    @Column(nullable = false)
    private LocalDateTime processedAt = LocalDateTime.now();

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    // Constructors
    public IdempotencyKey() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getIdempotencyKey() { return idempotencyKey; }
    public void setIdempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; }

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }

    public String getEndpoint() { return endpoint; }
    public void setEndpoint(String endpoint) { this.endpoint = endpoint; }

    public String getRequestHash() { return requestHash; }
    public void setRequestHash(String requestHash) { this.requestHash = requestHash; }

    public String getHttpMethod() { return httpMethod; }
    public void setHttpMethod(String httpMethod) { this.httpMethod = httpMethod; }

    public Map<String, Object> getRequestBody() { return requestBody; }
    public void setRequestBody(Map<String, Object> requestBody) { this.requestBody = requestBody; }

    public Map<String, Object> getRequestHeaders() { return requestHeaders; }
    public void setRequestHeaders(Map<String, Object> requestHeaders) { this.requestHeaders = requestHeaders; }

    public Integer getResponseStatus() { return responseStatus; }
    public void setResponseStatus(Integer responseStatus) { this.responseStatus = responseStatus; }

    public Map<String, Object> getResponseBody() { return responseBody; }
    public void setResponseBody(Map<String, Object> responseBody) { this.responseBody = responseBody; }

    public Map<String, Object> getResponseHeaders() { return responseHeaders; }
    public void setResponseHeaders(Map<String, Object> responseHeaders) { this.responseHeaders = responseHeaders; }

    public LocalDateTime getProcessedAt() { return processedAt; }
    public void setProcessedAt(LocalDateTime processedAt) { this.processedAt = processedAt; }

    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
