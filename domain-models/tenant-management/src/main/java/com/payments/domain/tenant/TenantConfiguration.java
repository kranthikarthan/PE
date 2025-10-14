package com.payments.domain.tenant;

import com.payments.domain.shared.*;
import lombok.*;
import jakarta.persistence.*;
import java.time.Instant;

/**
 * Tenant Configuration (Entity within Tenant Aggregate)
 */
@Entity
@Table(name = "tenant_configurations")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Data
public class TenantConfiguration {
    
    @EmbeddedId
    private ConfigurationId id;
    
    @Embedded
    private TenantId tenantId;
    
    private String configKey;
    
    private String configValue;
    
    @Enumerated(EnumType.STRING)
    private ConfigurationType configType;
    
    private String description;
    
    private Boolean isEncrypted;
    
    private Instant createdAt;
    
    private Instant updatedAt;
    
    private String createdBy;
    
    private String updatedBy;
    
    public static TenantConfiguration create(
        ConfigurationId id,
        TenantId tenantId,
        String configKey,
        String configValue,
        ConfigurationType configType,
        String createdBy
    ) {
        TenantConfiguration config = new TenantConfiguration();
        config.id = id;
        config.tenantId = tenantId;
        config.configKey = configKey;
        config.configValue = configValue;
        config.configType = configType;
        config.isEncrypted = false;
        config.createdAt = Instant.now();
        config.updatedAt = Instant.now();
        config.createdBy = createdBy;
        config.updatedBy = createdBy;
        
        return config;
    }
    
    public void updateValue(String value, String updatedBy) {
        this.configValue = value;
        this.updatedAt = Instant.now();
        this.updatedBy = updatedBy;
    }
}
