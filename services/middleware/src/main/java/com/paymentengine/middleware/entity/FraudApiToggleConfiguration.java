package com.paymentengine.middleware.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entity for managing fraud API enable/disable configurations at different levels
 */
@Entity
@Table(name = "fraud_api_toggle_configurations", schema = "payment_engine")
@EntityListeners(AuditingEntityListener.class)
public class FraudApiToggleConfiguration {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @NotBlank
    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    @Column(name = "payment_type")
    private String paymentType;

    @Column(name = "local_instrumentation_code")
    private String localInstrumentationCode;

    @Column(name = "clearing_system_code")
    private String clearingSystemCode;

    @NotNull
    @Column(name = "is_enabled", nullable = false)
    private Boolean isEnabled = true;

    @Column(name = "enabled_reason")
    private String enabledReason;

    @Column(name = "disabled_reason")
    private String disabledReason;

    @Column(name = "effective_from")
    private LocalDateTime effectiveFrom;

    @Column(name = "effective_until")
    private LocalDateTime effectiveUntil;

    @Column(name = "priority", nullable = false)
    private Integer priority = 100;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "updated_by")
    private String updatedBy;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Constructors
    public FraudApiToggleConfiguration() {}

    public FraudApiToggleConfiguration(String tenantId, Boolean isEnabled) {
        this.tenantId = tenantId;
        this.isEnabled = isEnabled;
    }

    public FraudApiToggleConfiguration(String tenantId, String paymentType, Boolean isEnabled) {
        this.tenantId = tenantId;
        this.paymentType = paymentType;
        this.isEnabled = isEnabled;
    }

    public FraudApiToggleConfiguration(String tenantId, String paymentType, String localInstrumentationCode, Boolean isEnabled) {
        this.tenantId = tenantId;
        this.paymentType = paymentType;
        this.localInstrumentationCode = localInstrumentationCode;
        this.isEnabled = isEnabled;
    }

    public FraudApiToggleConfiguration(String tenantId, String paymentType, String localInstrumentationCode, String clearingSystemCode, Boolean isEnabled) {
        this.tenantId = tenantId;
        this.paymentType = paymentType;
        this.localInstrumentationCode = localInstrumentationCode;
        this.clearingSystemCode = clearingSystemCode;
        this.isEnabled = isEnabled;
    }

    // Helper methods
    public boolean isEffective() {
        LocalDateTime now = LocalDateTime.now();
        if (effectiveFrom != null && now.isBefore(effectiveFrom)) {
            return false;
        }
        if (effectiveUntil != null && now.isAfter(effectiveUntil)) {
            return false;
        }
        return isActive;
    }

    public String getConfigurationLevel() {
        if (clearingSystemCode != null) {
            return "CLEARING_SYSTEM";
        } else if (localInstrumentationCode != null) {
            return "LOCAL_INSTRUMENT";
        } else if (paymentType != null) {
            return "PAYMENT_TYPE";
        } else {
            return "TENANT";
        }
    }

    public String getConfigurationKey() {
        StringBuilder key = new StringBuilder(tenantId);
        if (paymentType != null) {
            key.append(":").append(paymentType);
        }
        if (localInstrumentationCode != null) {
            key.append(":").append(localInstrumentationCode);
        }
        if (clearingSystemCode != null) {
            key.append(":").append(clearingSystemCode);
        }
        return key.toString();
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(String paymentType) {
        this.paymentType = paymentType;
    }

    public String getLocalInstrumentationCode() {
        return localInstrumentationCode;
    }

    public void setLocalInstrumentationCode(String localInstrumentationCode) {
        this.localInstrumentationCode = localInstrumentationCode;
    }

    public String getClearingSystemCode() {
        return clearingSystemCode;
    }

    public void setClearingSystemCode(String clearingSystemCode) {
        this.clearingSystemCode = clearingSystemCode;
    }

    public Boolean getIsEnabled() {
        return isEnabled;
    }

    public void setIsEnabled(Boolean isEnabled) {
        this.isEnabled = isEnabled;
    }

    public String getEnabledReason() {
        return enabledReason;
    }

    public void setEnabledReason(String enabledReason) {
        this.enabledReason = enabledReason;
    }

    public String getDisabledReason() {
        return disabledReason;
    }

    public void setDisabledReason(String disabledReason) {
        this.disabledReason = disabledReason;
    }

    public LocalDateTime getEffectiveFrom() {
        return effectiveFrom;
    }

    public void setEffectiveFrom(LocalDateTime effectiveFrom) {
        this.effectiveFrom = effectiveFrom;
    }

    public LocalDateTime getEffectiveUntil() {
        return effectiveUntil;
    }

    public void setEffectiveUntil(LocalDateTime effectiveUntil) {
        this.effectiveUntil = effectiveUntil;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
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
        FraudApiToggleConfiguration that = (FraudApiToggleConfiguration) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "FraudApiToggleConfiguration{" +
                "id='" + id + '\'' +
                ", tenantId='" + tenantId + '\'' +
                ", paymentType='" + paymentType + '\'' +
                ", localInstrumentationCode='" + localInstrumentationCode + '\'' +
                ", clearingSystemCode='" + clearingSystemCode + '\'' +
                ", isEnabled=" + isEnabled +
                ", priority=" + priority +
                ", isActive=" + isActive +
                '}';
    }
}