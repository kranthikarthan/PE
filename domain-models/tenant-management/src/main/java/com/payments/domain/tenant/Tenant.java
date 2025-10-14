package com.payments.domain.tenant;

import com.payments.domain.shared.*;
import lombok.*;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Tenant Aggregate Root
 * 
 * Represents a tenant in the multi-tenant system.
 * Manages tenant hierarchy, configuration, and access control.
 */
@Entity
@Table(name = "tenants")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Tenant {
    
    @EmbeddedId
    private TenantId id;
    
    private String tenantName;
    
    @Enumerated(EnumType.STRING)
    private TenantType tenantType;
    
    @Enumerated(EnumType.STRING)
    private TenantStatus status;
    
    private String registrationNumber;
    
    private String taxNumber;
    
    private String contactEmail;
    
    private String contactPhone;
    
    private String addressLine1;
    
    private String addressLine2;
    
    private String city;
    
    private String province;
    
    private String postalCode;
    
    private String country;
    
    private String timezone;
    
    private String currency;
    
    private Instant createdAt;
    
    private Instant updatedAt;
    
    private String createdBy;
    
    private String updatedBy;
    
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "tenant_id")
    private List<BusinessUnit> businessUnits = new ArrayList<>();
    
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "tenant_id")
    private List<TenantConfiguration> configurations = new ArrayList<>();
    
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "tenant_id")
    private List<TenantUser> users = new ArrayList<>();
    
    @Transient
    private List<DomainEvent> domainEvents = new ArrayList<>();
    
    // ─────────────────────────────────────────────────────────
    // FACTORY METHOD
    // ─────────────────────────────────────────────────────────
    
    public static Tenant create(
        TenantId id,
        String tenantName,
        TenantType tenantType,
        String contactEmail,
        String createdBy
    ) {
        // Business validation
        if (tenantName == null || tenantName.isBlank()) {
            throw new InvalidTenantException("Tenant name cannot be null or blank");
        }
        
        if (contactEmail == null || contactEmail.isBlank()) {
            throw new InvalidTenantException("Contact email cannot be null or blank");
        }
        
        Tenant tenant = new Tenant();
        tenant.id = id;
        tenant.tenantName = tenantName;
        tenant.tenantType = tenantType;
        tenant.contactEmail = contactEmail;
        tenant.status = TenantStatus.ACTIVE;
        tenant.country = "ZAR";
        tenant.timezone = "Africa/Johannesburg";
        tenant.currency = "ZAR";
        tenant.createdAt = Instant.now();
        tenant.updatedAt = Instant.now();
        tenant.createdBy = createdBy;
        tenant.updatedBy = createdBy;
        
        // Create default business unit
        BusinessUnit defaultBU = BusinessUnit.create(
            BusinessUnitId.generate(),
            id,
            "Default Business Unit",
            BusinessUnitType.TREASURY,
            1,
            createdBy
        );
        tenant.businessUnits.add(defaultBU);
        
        // Set default configurations
        tenant.setDefaultConfigurations(createdBy);
        
        // Domain event
        tenant.registerEvent(new TenantCreatedEvent(
            tenant.id,
            tenant.tenantName,
            tenant.tenantType,
            tenant.createdAt
        ));
        
        return tenant;
    }
    
    // ─────────────────────────────────────────────────────────
    // BUSINESS METHODS
    // ─────────────────────────────────────────────────────────
    
    /**
     * Add a business unit to the tenant
     */
    public void addBusinessUnit(
        BusinessUnitId businessUnitId,
        String businessUnitName,
        BusinessUnitType businessUnitType,
        Integer hierarchyLevel,
        String createdBy
    ) {
        // Business validation
        if (businessUnitName == null || businessUnitName.isBlank()) {
            throw new InvalidTenantException("Business unit name cannot be null or blank");
        }
        
        BusinessUnit businessUnit = BusinessUnit.create(
            businessUnitId,
            this.id,
            businessUnitName,
            businessUnitType,
            hierarchyLevel,
            createdBy
        );
        
        this.businessUnits.add(businessUnit);
        this.updatedAt = Instant.now();
        this.updatedBy = createdBy;
        
        registerEvent(new BusinessUnitAddedEvent(
            this.id,
            businessUnitId,
            businessUnitName,
            businessUnitType
        ));
    }
    
    /**
     * Update tenant configuration
     */
    public void updateConfiguration(
        String configKey,
        String configValue,
        ConfigurationType configType,
        String updatedBy
    ) {
        // Find existing configuration or create new one
        TenantConfiguration existingConfig = configurations.stream()
            .filter(config -> config.getConfigKey().equals(configKey))
            .findFirst()
            .orElse(null);
        
        if (existingConfig != null) {
            existingConfig.updateValue(configValue, updatedBy);
        } else {
            TenantConfiguration newConfig = TenantConfiguration.create(
                ConfigurationId.generate(),
                this.id,
                configKey,
                configValue,
                configType,
                updatedBy
            );
            this.configurations.add(newConfig);
        }
        
        this.updatedAt = Instant.now();
        this.updatedBy = updatedBy;
        
        registerEvent(new ConfigurationUpdatedEvent(
            this.id,
            configKey,
            configValue,
            configType
        ));
    }
    
    /**
     * Add user to tenant
     */
    public void addUser(
        UserId userId,
        String username,
        String email,
        String role,
        String addedBy
    ) {
        TenantUser tenantUser = TenantUser.create(
            TenantUserId.generate(),
            this.id,
            userId,
            username,
            email,
            role,
            addedBy
        );
        
        this.users.add(tenantUser);
        this.updatedAt = Instant.now();
        this.updatedBy = addedBy;
        
        registerEvent(new UserAddedEvent(
            this.id,
            userId,
            username,
            role
        ));
    }
    
    /**
     * Suspend the tenant
     */
    public void suspend(String reason, String suspendedBy) {
        if (this.status == TenantStatus.SUSPENDED) {
            throw new InvalidTenantException("Tenant is already suspended");
        }
        
        this.status = TenantStatus.SUSPENDED;
        this.updatedAt = Instant.now();
        this.updatedBy = suspendedBy;
        
        registerEvent(new TenantSuspendedEvent(
            this.id,
            reason,
            suspendedBy
        ));
    }
    
    /**
     * Activate the tenant
     */
    public void activate(String activatedBy) {
        if (this.status == TenantStatus.ACTIVE) {
            throw new InvalidTenantException("Tenant is already active");
        }
        
        this.status = TenantStatus.ACTIVE;
        this.updatedAt = Instant.now();
        this.updatedBy = activatedBy;
        
        registerEvent(new TenantActivatedEvent(
            this.id,
            activatedBy
        ));
    }
    
    // ─────────────────────────────────────────────────────────
    // QUERY METHODS
    // ─────────────────────────────────────────────────────────
    
    public boolean isActive() {
        return this.status == TenantStatus.ACTIVE;
    }
    
    public boolean isSuspended() {
        return this.status == TenantStatus.SUSPENDED;
    }
    
    public boolean hasBusinessUnit(BusinessUnitId businessUnitId) {
        return businessUnits.stream()
            .anyMatch(bu -> bu.getId().equals(businessUnitId));
    }
    
    public boolean hasUser(UserId userId) {
        return users.stream()
            .anyMatch(user -> user.getUserId().equals(userId));
    }
    
    public String getConfigurationValue(String configKey) {
        return configurations.stream()
            .filter(config -> config.getConfigKey().equals(configKey))
            .map(TenantConfiguration::getConfigValue)
            .findFirst()
            .orElse(null);
    }
    
    public TenantId getId() {
        return id;
    }
    
    public String getTenantName() {
        return tenantName;
    }
    
    public TenantType getTenantType() {
        return tenantType;
    }
    
    public TenantStatus getStatus() {
        return status;
    }
    
    public String getContactEmail() {
        return contactEmail;
    }
    
    public List<BusinessUnit> getBusinessUnits() {
        return Collections.unmodifiableList(businessUnits);
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
    
    private void setDefaultConfigurations(String createdBy) {
        // Set default tenant configurations
        updateConfiguration("max_daily_transactions", "1000000", ConfigurationType.NUMBER, createdBy);
        updateConfiguration("max_daily_amount", "1000000000.00", ConfigurationType.NUMBER, createdBy);
        updateConfiguration("timezone", "Africa/Johannesburg", ConfigurationType.STRING, createdBy);
        updateConfiguration("currency", "ZAR", ConfigurationType.STRING, createdBy);
        updateConfiguration("fraud_detection_enabled", "true", ConfigurationType.BOOLEAN, createdBy);
        updateConfiguration("audit_retention_days", "2555", ConfigurationType.NUMBER, createdBy);
    }
    
    private void registerEvent(DomainEvent event) {
        this.domainEvents.add(event);
    }
}

/**
 * Business Unit (Entity within Tenant Aggregate)
 */
@Entity
@Table(name = "business_units")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Data
class BusinessUnit {
    
    @EmbeddedId
    private BusinessUnitId id;
    
    @Embedded
    private TenantId tenantId;
    
    private String businessUnitName;
    
    @Enumerated(EnumType.STRING)
    private BusinessUnitType businessUnitType;
    
    @Enumerated(EnumType.STRING)
    private BusinessUnitStatus status;
    
    private String parentBusinessUnitId;
    
    private Integer hierarchyLevel;
    
    private Instant createdAt;
    
    private Instant updatedAt;
    
    private String createdBy;
    
    private String updatedBy;
    
    public static BusinessUnit create(
        BusinessUnitId id,
        TenantId tenantId,
        String businessUnitName,
        BusinessUnitType businessUnitType,
        Integer hierarchyLevel,
        String createdBy
    ) {
        BusinessUnit businessUnit = new BusinessUnit();
        businessUnit.id = id;
        businessUnit.tenantId = tenantId;
        businessUnit.businessUnitName = businessUnitName;
        businessUnit.businessUnitType = businessUnitType;
        businessUnit.status = BusinessUnitStatus.ACTIVE;
        businessUnit.hierarchyLevel = hierarchyLevel;
        businessUnit.createdAt = Instant.now();
        businessUnit.updatedAt = Instant.now();
        businessUnit.createdBy = createdBy;
        businessUnit.updatedBy = createdBy;
        
        return businessUnit;
    }
    
    public void updateValue(String value, String updatedBy) {
        // Implementation for updating business unit
        this.updatedAt = Instant.now();
        this.updatedBy = updatedBy;
    }
}
