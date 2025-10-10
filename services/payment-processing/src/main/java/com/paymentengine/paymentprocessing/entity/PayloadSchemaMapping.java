package com.paymentengine.paymentprocessing.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Payload Schema Mapping Entity
 * 
 * Stores mapping configurations between internal data models and external
 * core banking system payloads, including field mappings, transformations,
 * and validation rules.
 */
@Entity
@Table(name = "payload_schema_mappings", schema = "payment_engine")
public class PayloadSchemaMapping {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "endpoint_config_id", nullable = false)
    @NotNull
    private UUID endpointConfigId;
    
    @Column(name = "mapping_name", nullable = false, length = 100)
    @NotBlank
    @Size(max = 100)
    private String mappingName;
    
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
    
    @Column(name = "transformation_rules", columnDefinition = "jsonb")
    private Map<String, Object> transformationRules;
    
    @Column(name = "validation_rules", columnDefinition = "jsonb")
    private Map<String, Object> validationRules;
    
    @Column(name = "default_values", columnDefinition = "jsonb")
    private Map<String, Object> defaultValues;
    
    @Column(name = "conditional_mappings", columnDefinition = "jsonb")
    private Map<String, Object> conditionalMappings;
    
    @Column(name = "array_handling_config", columnDefinition = "jsonb")
    private Map<String, Object> arrayHandlingConfig;
    
    @Column(name = "nested_object_config", columnDefinition = "jsonb")
    private Map<String, Object> nestedObjectConfig;
    
    @Column(name = "error_handling_config", columnDefinition = "jsonb")
    private Map<String, Object> errorHandlingConfig;
    
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
    public PayloadSchemaMapping() {}
    
    public PayloadSchemaMapping(UUID endpointConfigId, String mappingName, 
                              MappingType mappingType, Direction direction) {
        this.endpointConfigId = endpointConfigId;
        this.mappingName = mappingName;
        this.mappingType = mappingType;
        this.direction = direction;
    }
    
    // Getters and Setters
    public UUID getId() {
        return id;
    }
    
    public void setId(UUID id) {
        this.id = id;
    }
    
    public UUID getEndpointConfigId() {
        return endpointConfigId;
    }
    
    public void setEndpointConfigId(UUID endpointConfigId) {
        this.endpointConfigId = endpointConfigId;
    }
    
    public String getMappingName() {
        return mappingName;
    }
    
    public void setMappingName(String mappingName) {
        this.mappingName = mappingName;
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
    
    public Map<String, Object> getConditionalMappings() {
        return conditionalMappings;
    }
    
    public void setConditionalMappings(Map<String, Object> conditionalMappings) {
        this.conditionalMappings = conditionalMappings;
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
        return "PayloadSchemaMapping{" +
                "id=" + id +
                ", mappingName='" + mappingName + '\'' +
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
        CUSTOM_MAPPING
    }
    
    /**
     * Direction Enumeration
     */
    public enum Direction {
        REQUEST,
        RESPONSE,
        BIDIRECTIONAL
    }
}