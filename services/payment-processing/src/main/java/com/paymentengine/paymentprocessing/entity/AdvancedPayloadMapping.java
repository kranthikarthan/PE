package com.paymentengine.paymentprocessing.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Advanced Payload Mapping Entity
 * 
 * Provides flexible payload mapping with support for static values, derived values,
 * conditional logic, and auto-generated IDs, configurable per tenant, payment type,
 * local instrumentation code, and clearing system.
 */
@Entity
@Table(name = "advanced_payload_mappings", schema = "payment_engine")
public class AdvancedPayloadMapping {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "mapping_name", nullable = false, length = 100)
    @NotBlank
    @Size(max = 100)
    private String mappingName;
    
    @Column(name = "tenant_id", nullable = false, length = 50)
    @NotBlank
    @Size(max = 50)
    private String tenantId;
    
    @Column(name = "payment_type", length = 50)
    @Size(max = 50)
    private String paymentType;
    
    @Column(name = "local_instrumentation_code", length = 50)
    @Size(max = 50)
    private String localInstrumentationCode;
    
    @Column(name = "clearing_system_code", length = 50)
    @Size(max = 50)
    private String clearingSystemCode;
    
    @Column(name = "mapping_type", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    @NotNull
    private MappingType mappingType;
    
    @Column(name = "direction", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    @NotNull
    private Direction direction;
    
    @Column(name = "source_schema", columnDefinition = "jsonb")
    private Map<String, Object> sourceSchema;
    
    @Column(name = "target_schema", columnDefinition = "jsonb")
    private Map<String, Object> targetSchema;
    
    @Column(name = "field_mappings", columnDefinition = "jsonb")
    private Map<String, Object> fieldMappings;
    
    @Column(name = "value_assignments", columnDefinition = "jsonb")
    private Map<String, Object> valueAssignments;
    
    @Column(name = "conditional_mappings", columnDefinition = "jsonb")
    private Map<String, Object> conditionalMappings;
    
    @Column(name = "derived_value_rules", columnDefinition = "jsonb")
    private Map<String, Object> derivedValueRules;
    
    @Column(name = "auto_generation_rules", columnDefinition = "jsonb")
    private Map<String, Object> autoGenerationRules;
    
    @Column(name = "transformation_rules", columnDefinition = "jsonb")
    private Map<String, Object> transformationRules;
    
    @Column(name = "validation_rules", columnDefinition = "jsonb")
    private Map<String, Object> validationRules;
    
    @Column(name = "default_values", columnDefinition = "jsonb")
    private Map<String, Object> defaultValues;
    
    @Column(name = "array_handling_config", columnDefinition = "jsonb")
    private Map<String, Object> arrayHandlingConfig;
    
    @Column(name = "nested_object_config", columnDefinition = "jsonb")
    private Map<String, Object> nestedObjectConfig;
    
    @Column(name = "error_handling_config", columnDefinition = "jsonb")
    private Map<String, Object> errorHandlingConfig;
    
    @Column(name = "performance_config", columnDefinition = "jsonb")
    private Map<String, Object> performanceConfig;
    
    @Column(name = "version")
    private String version = "1.0";
    
    @Column(name = "priority")
    private Integer priority = 1;
    
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    @Column(name = "description", length = 1000)
    @Size(max = 1000)
    private String description;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @Column(name = "created_by", length = 100)
    @Size(max = 100)
    private String createdBy;
    
    @Column(name = "updated_by", length = 100)
    @Size(max = 100)
    private String updatedBy;
    
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
    public AdvancedPayloadMapping() {}
    
    public AdvancedPayloadMapping(String mappingName, String tenantId, MappingType mappingType, Direction direction) {
        this.mappingName = mappingName;
        this.tenantId = tenantId;
        this.mappingType = mappingType;
        this.direction = direction;
    }
    
    // Business methods
    public boolean matchesCriteria(String tenantId, String paymentType, String localInstrumentationCode, String clearingSystemCode) {
        if (!this.tenantId.equals(tenantId)) {
            return false;
        }
        
        if (this.paymentType != null && !this.paymentType.equals(paymentType)) {
            return false;
        }
        
        if (this.localInstrumentationCode != null && !this.localInstrumentationCode.equals(localInstrumentationCode)) {
            return false;
        }
        
        if (this.clearingSystemCode != null && !this.clearingSystemCode.equals(clearingSystemCode)) {
            return false;
        }
        
        return true;
    }
    
    public boolean isApplicableForDirection(Direction direction) {
        return this.direction == direction || this.direction == Direction.BIDIRECTIONAL;
    }
    
    public boolean hasValueAssignment(String fieldPath) {
        return valueAssignments != null && valueAssignments.containsKey(fieldPath);
    }
    
    public boolean hasDerivedValueRule(String fieldPath) {
        return derivedValueRules != null && derivedValueRules.containsKey(fieldPath);
    }
    
    public boolean hasAutoGenerationRule(String fieldPath) {
        return autoGenerationRules != null && autoGenerationRules.containsKey(fieldPath);
    }
    
    public boolean hasConditionalMapping(String fieldPath) {
        return conditionalMappings != null && conditionalMappings.containsKey(fieldPath);
    }
    
    // Getters and Setters
    public UUID getId() {
        return id;
    }
    
    public void setId(UUID id) {
        this.id = id;
    }
    
    public String getMappingName() {
        return mappingName;
    }
    
    public void setMappingName(String mappingName) {
        this.mappingName = mappingName;
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
    
    public MappingType getMappingType() {
        return mappingType;
    }
    
    public void setMappingType(MappingType mappingType) {
        this.mappingType = mappingType;
    }
    
    public Direction getDirection() {
        return direction;
    }
    
    public void setDirection(Direction direction) {
        this.direction = direction;
    }
    
    public Map<String, Object> getSourceSchema() {
        return sourceSchema;
    }
    
    public void setSourceSchema(Map<String, Object> sourceSchema) {
        this.sourceSchema = sourceSchema;
    }
    
    public Map<String, Object> getTargetSchema() {
        return targetSchema;
    }
    
    public void setTargetSchema(Map<String, Object> targetSchema) {
        this.targetSchema = targetSchema;
    }
    
    public Map<String, Object> getFieldMappings() {
        return fieldMappings;
    }
    
    public void setFieldMappings(Map<String, Object> fieldMappings) {
        this.fieldMappings = fieldMappings;
    }
    
    public Map<String, Object> getValueAssignments() {
        return valueAssignments;
    }
    
    public void setValueAssignments(Map<String, Object> valueAssignments) {
        this.valueAssignments = valueAssignments;
    }
    
    public Map<String, Object> getConditionalMappings() {
        return conditionalMappings;
    }
    
    public void setConditionalMappings(Map<String, Object> conditionalMappings) {
        this.conditionalMappings = conditionalMappings;
    }
    
    public Map<String, Object> getDerivedValueRules() {
        return derivedValueRules;
    }
    
    public void setDerivedValueRules(Map<String, Object> derivedValueRules) {
        this.derivedValueRules = derivedValueRules;
    }
    
    public Map<String, Object> getAutoGenerationRules() {
        return autoGenerationRules;
    }
    
    public void setAutoGenerationRules(Map<String, Object> autoGenerationRules) {
        this.autoGenerationRules = autoGenerationRules;
    }
    
    public Map<String, Object> getTransformationRules() {
        return transformationRules;
    }
    
    public void setTransformationRules(Map<String, Object> transformationRules) {
        this.transformationRules = transformationRules;
    }
    
    public Map<String, Object> getValidationRules() {
        return validationRules;
    }
    
    public void setValidationRules(Map<String, Object> validationRules) {
        this.validationRules = validationRules;
    }
    
    public Map<String, Object> getDefaultValues() {
        return defaultValues;
    }
    
    public void setDefaultValues(Map<String, Object> defaultValues) {
        this.defaultValues = defaultValues;
    }
    
    public Map<String, Object> getArrayHandlingConfig() {
        return arrayHandlingConfig;
    }
    
    public void setArrayHandlingConfig(Map<String, Object> arrayHandlingConfig) {
        this.arrayHandlingConfig = arrayHandlingConfig;
    }
    
    public Map<String, Object> getNestedObjectConfig() {
        return nestedObjectConfig;
    }
    
    public void setNestedObjectConfig(Map<String, Object> nestedObjectConfig) {
        this.nestedObjectConfig = nestedObjectConfig;
    }
    
    public Map<String, Object> getErrorHandlingConfig() {
        return errorHandlingConfig;
    }
    
    public void setErrorHandlingConfig(Map<String, Object> errorHandlingConfig) {
        this.errorHandlingConfig = errorHandlingConfig;
    }
    
    public Map<String, Object> getPerformanceConfig() {
        return performanceConfig;
    }
    
    public void setPerformanceConfig(Map<String, Object> performanceConfig) {
        this.performanceConfig = performanceConfig;
    }
    
    public String getVersion() {
        return version;
    }
    
    public void setVersion(String version) {
        this.version = version;
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
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
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
    
    @Override
    public String toString() {
        return "AdvancedPayloadMapping{" +
                "id=" + id +
                ", mappingName='" + mappingName + '\'' +
                ", tenantId='" + tenantId + '\'' +
                ", paymentType='" + paymentType + '\'' +
                ", localInstrumentationCode='" + localInstrumentationCode + '\'' +
                ", clearingSystemCode='" + clearingSystemCode + '\'' +
                ", mappingType=" + mappingType +
                ", direction=" + direction +
                ", version='" + version + '\'' +
                ", isActive=" + isActive +
                '}';
    }
    
    /**
     * Mapping Type Enumeration
     */
    public enum MappingType {
        FIELD_MAPPING,
        OBJECT_MAPPING,
        ARRAY_MAPPING,
        NESTED_MAPPING,
        CONDITIONAL_MAPPING,
        TRANSFORMATION_MAPPING,
        VALUE_ASSIGNMENT_MAPPING,
        DERIVED_VALUE_MAPPING,
        AUTO_GENERATION_MAPPING,
        CUSTOM_MAPPING
    }
    
    /**
     * Direction Enumeration
     */
    public enum Direction {
        REQUEST,
        RESPONSE,
        BIDIRECTIONAL,
        FRAUD_API_REQUEST,
        FRAUD_API_RESPONSE,
        CORE_BANKING_DEBIT_REQUEST,
        CORE_BANKING_DEBIT_RESPONSE,
        CORE_BANKING_CREDIT_REQUEST,
        CORE_BANKING_CREDIT_RESPONSE,
        SCHEME_REQUEST,
        SCHEME_RESPONSE
    }
}