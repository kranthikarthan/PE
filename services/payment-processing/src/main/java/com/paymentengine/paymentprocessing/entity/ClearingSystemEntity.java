package com.paymentengine.paymentprocessing.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Entity for storing clearing system configurations
 */
@Entity
@Table(name = "clearing_systems")
public class ClearingSystemEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    @Column(name = "code", nullable = false, unique = true, length = 20)
    private String code;
    
    @Column(name = "name", nullable = false, length = 100)
    private String name;
    
    @Column(name = "description", length = 500)
    private String description;
    
    @Column(name = "country_code", length = 3)
    private String countryCode;
    
    @Column(name = "currency", length = 3)
    private String currency;
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
    
    @Column(name = "processing_mode", length = 20)
    private String processingMode; // SYNCHRONOUS, ASYNCHRONOUS, BATCH
    
    @Column(name = "timeout_seconds")
    private Integer timeoutSeconds;
    
    @Column(name = "endpoint_url", length = 500)
    private String endpointUrl;
    
    @Column(name = "authentication_type", length = 20)
    private String authenticationType; // NONE, API_KEY, JWT, OAUTH2, MTLS
    
    @ElementCollection
    @CollectionTable(name = "clearing_system_authentication_config", 
                    joinColumns = @JoinColumn(name = "clearing_system_id"))
    @MapKeyColumn(name = "config_key")
    @Column(name = "config_value")
    private Map<String, String> authenticationConfig;
    
    @ElementCollection
    @CollectionTable(name = "clearing_system_supported_messages", 
                    joinColumns = @JoinColumn(name = "clearing_system_id"))
    @MapKeyColumn(name = "message_type")
    @Column(name = "message_description")
    private Map<String, String> supportedMessageTypes;
    
    @ElementCollection
    @CollectionTable(name = "clearing_system_supported_payment_types", 
                    joinColumns = @JoinColumn(name = "clearing_system_id"))
    @MapKeyColumn(name = "payment_type")
    @Column(name = "payment_description")
    private Map<String, String> supportedPaymentTypes;
    
    @ElementCollection
    @CollectionTable(name = "clearing_system_supported_instruments", 
                    joinColumns = @JoinColumn(name = "clearing_system_id"))
    @MapKeyColumn(name = "instrument_code")
    @Column(name = "instrument_description")
    private Map<String, String> supportedLocalInstruments;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    // Constructors
    public ClearingSystemEntity() {}
    
    public ClearingSystemEntity(String code, String name, String description, String countryCode,
                              String currency, Boolean isActive, String processingMode,
                              Integer timeoutSeconds, String endpointUrl, String authenticationType,
                              Map<String, String> authenticationConfig, Map<String, String> supportedMessageTypes,
                              Map<String, String> supportedPaymentTypes, Map<String, String> supportedLocalInstruments) {
        this.code = code;
        this.name = name;
        this.description = description;
        this.countryCode = countryCode;
        this.currency = currency;
        this.isActive = isActive;
        this.processingMode = processingMode;
        this.timeoutSeconds = timeoutSeconds;
        this.endpointUrl = endpointUrl;
        this.authenticationType = authenticationType;
        this.authenticationConfig = authenticationConfig;
        this.supportedMessageTypes = supportedMessageTypes;
        this.supportedPaymentTypes = supportedPaymentTypes;
        this.supportedLocalInstruments = supportedLocalInstruments;
    }
    
    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getCountryCode() { return countryCode; }
    public void setCountryCode(String countryCode) { this.countryCode = countryCode; }
    
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    
    public String getProcessingMode() { return processingMode; }
    public void setProcessingMode(String processingMode) { this.processingMode = processingMode; }
    
    public Integer getTimeoutSeconds() { return timeoutSeconds; }
    public void setTimeoutSeconds(Integer timeoutSeconds) { this.timeoutSeconds = timeoutSeconds; }
    
    public String getEndpointUrl() { return endpointUrl; }
    public void setEndpointUrl(String endpointUrl) { this.endpointUrl = endpointUrl; }
    
    public String getAuthenticationType() { return authenticationType; }
    public void setAuthenticationType(String authenticationType) { this.authenticationType = authenticationType; }
    
    public Map<String, String> getAuthenticationConfig() { return authenticationConfig; }
    public void setAuthenticationConfig(Map<String, String> authenticationConfig) { this.authenticationConfig = authenticationConfig; }
    
    public Map<String, String> getSupportedMessageTypes() { return supportedMessageTypes; }
    public void setSupportedMessageTypes(Map<String, String> supportedMessageTypes) { this.supportedMessageTypes = supportedMessageTypes; }
    
    public Map<String, String> getSupportedPaymentTypes() { return supportedPaymentTypes; }
    public void setSupportedPaymentTypes(Map<String, String> supportedPaymentTypes) { this.supportedPaymentTypes = supportedPaymentTypes; }
    
    public Map<String, String> getSupportedLocalInstruments() { return supportedLocalInstruments; }
    public void setSupportedLocalInstruments(Map<String, String> supportedLocalInstruments) { this.supportedLocalInstruments = supportedLocalInstruments; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}