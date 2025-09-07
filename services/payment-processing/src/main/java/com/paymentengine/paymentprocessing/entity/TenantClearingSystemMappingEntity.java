package com.paymentengine.paymentprocessing.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Entity for storing tenant-specific clearing system mappings
 */
@Entity
@Table(name = "tenant_clearing_system_mappings", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"tenant_id", "payment_type", "local_instrument_code"}))
public class TenantClearingSystemMappingEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    @Column(name = "tenant_id", nullable = false, length = 50)
    private String tenantId;
    
    @Column(name = "payment_type", nullable = false, length = 50)
    private String paymentType;
    
    @Column(name = "local_instrument_code", length = 50)
    private String localInstrumentCode;
    
    @Column(name = "clearing_system_code", nullable = false, length = 20)
    private String clearingSystemCode;
    
    @Column(name = "priority", nullable = false)
    private Integer priority = 1;
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
    
    @Column(name = "description", length = 500)
    private String description;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    // Constructors
    public TenantClearingSystemMappingEntity() {}
    
    public TenantClearingSystemMappingEntity(String tenantId, String paymentType, String localInstrumentCode,
                                           String clearingSystemCode, Integer priority, Boolean isActive, String description) {
        this.tenantId = tenantId;
        this.paymentType = paymentType;
        this.localInstrumentCode = localInstrumentCode;
        this.clearingSystemCode = clearingSystemCode;
        this.priority = priority;
        this.isActive = isActive;
        this.description = description;
    }
    
    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    
    public String getPaymentType() { return paymentType; }
    public void setPaymentType(String paymentType) { this.paymentType = paymentType; }
    
    public String getLocalInstrumentCode() { return localInstrumentCode; }
    public void setLocalInstrumentCode(String localInstrumentCode) { this.localInstrumentCode = localInstrumentCode; }
    
    public String getClearingSystemCode() { return clearingSystemCode; }
    public void setClearingSystemCode(String clearingSystemCode) { this.clearingSystemCode = clearingSystemCode; }
    
    public Integer getPriority() { return priority; }
    public void setPriority(Integer priority) { this.priority = priority; }
    
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}