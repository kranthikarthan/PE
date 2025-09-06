package com.paymentengine.corebanking.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Payment Type entity representing different types of payments supported by the system
 */
@Entity
@Table(name = "payment_types", schema = "payment_engine")
public class PaymentType {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "code", unique = true, nullable = false, length = 50)
    @NotNull
    @Size(max = 50)
    private String code;
    
    @Column(name = "name", nullable = false, length = 100)
    @NotNull
    @Size(max = 100)
    private String name;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "is_synchronous")
    private Boolean isSynchronous = true;
    
    @Column(name = "max_amount", precision = 15, scale = 2)
    private BigDecimal maxAmount;
    
    @Column(name = "min_amount", precision = 15, scale = 2)
    @DecimalMin("0.01")
    private BigDecimal minAmount = new BigDecimal("0.01");
    
    @Column(name = "processing_fee", precision = 15, scale = 2)
    @DecimalMin("0.00")
    private BigDecimal processingFee = BigDecimal.ZERO;
    
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    @Column(name = "configuration", columnDefinition = "jsonb")
    @Convert(converter = MapToJsonConverter.class)
    private Map<String, Object> configuration = new HashMap<>();
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // Constructors
    public PaymentType() {}
    
    public PaymentType(String code, String name, Boolean isSynchronous) {
        this.code = code;
        this.name = name;
        this.isSynchronous = isSynchronous;
    }
    
    // Business methods
    public boolean isAmountValid(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }
        
        if (minAmount != null && amount.compareTo(minAmount) < 0) {
            return false;
        }
        
        if (maxAmount != null && amount.compareTo(maxAmount) > 0) {
            return false;
        }
        
        return true;
    }
    
    public BigDecimal calculateFee(BigDecimal amount) {
        if (processingFee == null) {
            return BigDecimal.ZERO;
        }
        
        // Check if fee is percentage-based (stored in configuration)
        if (configuration != null && configuration.containsKey("fee_percentage")) {
            Double feePercentage = (Double) configuration.get("fee_percentage");
            return amount.multiply(BigDecimal.valueOf(feePercentage / 100));
        }
        
        return processingFee;
    }
    
    public Integer getProcessingTimeMinutes() {
        if (configuration == null) {
            return isSynchronous ? 0 : 1440; // Default: immediate or 24 hours
        }
        
        if (configuration.containsKey("processing_time_minutes")) {
            return (Integer) configuration.get("processing_time_minutes");
        }
        
        if (configuration.containsKey("processing_time_hours")) {
            return (Integer) configuration.get("processing_time_hours") * 60;
        }
        
        if (configuration.containsKey("processing_time_seconds")) {
            return (Integer) configuration.get("processing_time_seconds") / 60;
        }
        
        return isSynchronous ? 0 : 1440;
    }
    
    public boolean requiresComplianceCheck() {
        if (configuration == null) {
            return false;
        }
        
        return Boolean.TRUE.equals(configuration.get("requires_compliance_check"));
    }
    
    public String getCutoffTime() {
        if (configuration == null) {
            return null;
        }
        
        return (String) configuration.get("cutoff_time");
    }
    
    // Getters and Setters
    public UUID getId() {
        return id;
    }
    
    public void setId(UUID id) {
        this.id = id;
    }
    
    public String getCode() {
        return code;
    }
    
    public void setCode(String code) {
        this.code = code;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public Boolean getIsSynchronous() {
        return isSynchronous;
    }
    
    public void setIsSynchronous(Boolean isSynchronous) {
        this.isSynchronous = isSynchronous;
    }
    
    public BigDecimal getMaxAmount() {
        return maxAmount;
    }
    
    public void setMaxAmount(BigDecimal maxAmount) {
        this.maxAmount = maxAmount;
    }
    
    public BigDecimal getMinAmount() {
        return minAmount;
    }
    
    public void setMinAmount(BigDecimal minAmount) {
        this.minAmount = minAmount;
    }
    
    public BigDecimal getProcessingFee() {
        return processingFee;
    }
    
    public void setProcessingFee(BigDecimal processingFee) {
        this.processingFee = processingFee;
    }
    
    public Boolean getIsActive() {
        return isActive;
    }
    
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
    
    public Map<String, Object> getConfiguration() {
        return configuration;
    }
    
    public void setConfiguration(Map<String, Object> configuration) {
        this.configuration = configuration;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PaymentType that = (PaymentType) o;
        return Objects.equals(id, that.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return "PaymentType{" +
                "id=" + id +
                ", code='" + code + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", isSynchronous=" + isSynchronous +
                ", maxAmount=" + maxAmount +
                ", minAmount=" + minAmount +
                ", processingFee=" + processingFee +
                ", isActive=" + isActive +
                '}';
    }
}