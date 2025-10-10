package com.paymentengine.paymentprocessing.repository;

import com.paymentengine.paymentprocessing.entity.MessageSecurityConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for MessageSecurityConfiguration entities
 */
@Repository
public interface MessageSecurityConfigurationRepository extends JpaRepository<MessageSecurityConfiguration, UUID> {
    
    /**
     * Find configurations by tenant ID
     */
    List<MessageSecurityConfiguration> findByTenantIdAndIsActiveTrue(String tenantId);
    
    /**
     * Find configurations by tenant ID and configuration level
     */
    List<MessageSecurityConfiguration> findByTenantIdAndConfigurationLevelAndIsActiveTrue(
            String tenantId, MessageSecurityConfiguration.ConfigurationLevel configurationLevel);
    
    /**
     * Find configurations by tenant ID and payment type
     */
    List<MessageSecurityConfiguration> findByTenantIdAndPaymentTypeAndIsActiveTrue(
            String tenantId, String paymentType);
    
    /**
     * Find configurations by tenant ID, service type, and endpoint
     */
    List<MessageSecurityConfiguration> findByTenantIdAndServiceTypeAndEndpointAndIsActiveTrue(
            String tenantId, String serviceType, String endpoint);
    
    /**
     * Find configurations by tenant ID and direction
     */
    List<MessageSecurityConfiguration> findByTenantIdAndDirectionAndIsActiveTrue(
            String tenantId, MessageSecurityConfiguration.MessageDirection direction);
    
    /**
     * Find configurations by tenant ID, payment type, and direction
     */
    List<MessageSecurityConfiguration> findByTenantIdAndPaymentTypeAndDirectionAndIsActiveTrue(
            String tenantId, String paymentType, MessageSecurityConfiguration.MessageDirection direction);
    
    /**
     * Find configurations by tenant ID, service type, endpoint, and direction
     */
    List<MessageSecurityConfiguration> findByTenantIdAndServiceTypeAndEndpointAndDirectionAndIsActiveTrue(
            String tenantId, String serviceType, String endpoint, MessageSecurityConfiguration.MessageDirection direction);
    
    /**
     * Find configurations by encryption algorithm
     */
    List<MessageSecurityConfiguration> findByEncryptionAlgorithmAndIsActiveTrue(
            MessageSecurityConfiguration.EncryptionAlgorithm encryptionAlgorithm);
    
    /**
     * Find configurations by signature algorithm
     */
    List<MessageSecurityConfiguration> findBySignatureAlgorithmAndIsActiveTrue(
            MessageSecurityConfiguration.SignatureAlgorithm signatureAlgorithm);
    
    /**
     * Find configurations by key provider
     */
    List<MessageSecurityConfiguration> findByEncryptionProviderOrSignatureProviderAndIsActiveTrue(
            MessageSecurityConfiguration.KeyProvider encryptionProvider,
            MessageSecurityConfiguration.KeyProvider signatureProvider);
    
    /**
     * Find configurations by message format
     */
    List<MessageSecurityConfiguration> findByMessageFormatAndIsActiveTrue(
            MessageSecurityConfiguration.MessageFormat messageFormat);
    
    /**
     * Find the most specific configuration for a given context
     * Priority: ENDPOINT > PAYMENT_TYPE > TENANT > CLEARING_SYSTEM
     */
    @Query("SELECT msc FROM MessageSecurityConfiguration msc WHERE " +
           "msc.tenantId = :tenantId AND " +
           "msc.isActive = true AND " +
           "(:serviceType IS NULL OR msc.serviceType = :serviceType) AND " +
           "(:endpoint IS NULL OR msc.endpoint = :endpoint) AND " +
           "(:paymentType IS NULL OR msc.paymentType = :paymentType) AND " +
           "(:direction IS NULL OR msc.direction = :direction OR msc.direction = 'BOTH') " +
           "ORDER BY " +
           "CASE msc.configurationLevel " +
           "WHEN 'ENDPOINT' THEN 1 " +
           "WHEN 'PAYMENT_TYPE' THEN 2 " +
           "WHEN 'TENANT' THEN 3 " +
           "WHEN 'CLEARING_SYSTEM' THEN 4 " +
           "END")
    List<MessageSecurityConfiguration> findMostSpecificConfiguration(
            @Param("tenantId") String tenantId,
            @Param("serviceType") String serviceType,
            @Param("endpoint") String endpoint,
            @Param("paymentType") String paymentType,
            @Param("direction") MessageSecurityConfiguration.MessageDirection direction);
    
    /**
     * Find configurations with encryption enabled
     */
    @Query("SELECT msc FROM MessageSecurityConfiguration msc WHERE " +
           "msc.encryptionEnabled = true AND " +
           "msc.isActive = true")
    List<MessageSecurityConfiguration> findConfigurationsWithEncryptionEnabled();
    
    /**
     * Find configurations with signature enabled
     */
    @Query("SELECT msc FROM MessageSecurityConfiguration msc WHERE " +
           "msc.signatureEnabled = true AND " +
           "msc.isActive = true")
    List<MessageSecurityConfiguration> findConfigurationsWithSignatureEnabled();
    
    /**
     * Find configurations by tenant ID with encryption enabled
     */
    @Query("SELECT msc FROM MessageSecurityConfiguration msc WHERE " +
           "msc.tenantId = :tenantId AND " +
           "msc.encryptionEnabled = true AND " +
           "msc.isActive = true")
    List<MessageSecurityConfiguration> findByTenantIdWithEncryptionEnabled(@Param("tenantId") String tenantId);
    
    /**
     * Find configurations by tenant ID with signature enabled
     */
    @Query("SELECT msc FROM MessageSecurityConfiguration msc WHERE " +
           "msc.tenantId = :tenantId AND " +
           "msc.signatureEnabled = true AND " +
           "msc.isActive = true")
    List<MessageSecurityConfiguration> findByTenantIdWithSignatureEnabled(@Param("tenantId") String tenantId);
    
    /**
     * Find configurations by key ID
     */
    List<MessageSecurityConfiguration> findByEncryptionKeyIdOrSignatureKeyIdAndIsActiveTrue(
            String encryptionKeyId, String signatureKeyId);
    
    /**
     * Find configurations by key provider
     */
    List<MessageSecurityConfiguration> findByEncryptionProviderAndIsActiveTrue(
            MessageSecurityConfiguration.KeyProvider encryptionProvider);
    
    /**
     * Find configurations by signature provider
     */
    List<MessageSecurityConfiguration> findBySignatureProviderAndIsActiveTrue(
            MessageSecurityConfiguration.KeyProvider signatureProvider);
    
    /**
     * Count configurations by tenant ID
     */
    long countByTenantIdAndIsActiveTrue(String tenantId);
    
    /**
     * Count configurations by configuration level
     */
    long countByConfigurationLevelAndIsActiveTrue(MessageSecurityConfiguration.ConfigurationLevel configurationLevel);
    
    /**
     * Count configurations with encryption enabled
     */
    long countByEncryptionEnabledTrueAndIsActiveTrue();
    
    /**
     * Count configurations with signature enabled
     */
    long countBySignatureEnabledTrueAndIsActiveTrue();
    
    /**
     * Find configurations by tenant ID and configuration level with pagination
     */
    @Query("SELECT msc FROM MessageSecurityConfiguration msc WHERE " +
           "msc.tenantId = :tenantId AND " +
           "msc.configurationLevel = :configurationLevel AND " +
           "msc.isActive = true " +
           "ORDER BY msc.createdAt DESC")
    List<MessageSecurityConfiguration> findByTenantIdAndConfigurationLevelWithPagination(
            @Param("tenantId") String tenantId,
            @Param("configurationLevel") MessageSecurityConfiguration.ConfigurationLevel configurationLevel);
    
    /**
     * Find configurations by tenant ID and payment type with pagination
     */
    @Query("SELECT msc FROM MessageSecurityConfiguration msc WHERE " +
           "msc.tenantId = :tenantId AND " +
           "msc.paymentType = :paymentType AND " +
           "msc.isActive = true " +
           "ORDER BY msc.createdAt DESC")
    List<MessageSecurityConfiguration> findByTenantIdAndPaymentTypeWithPagination(
            @Param("tenantId") String tenantId,
            @Param("paymentType") String paymentType);
    
    /**
     * Find configurations by tenant ID and service type with pagination
     */
    @Query("SELECT msc FROM MessageSecurityConfiguration msc WHERE " +
           "msc.tenantId = :tenantId AND " +
           "msc.serviceType = :serviceType AND " +
           "msc.isActive = true " +
           "ORDER BY msc.createdAt DESC")
    List<MessageSecurityConfiguration> findByTenantIdAndServiceTypeWithPagination(
            @Param("tenantId") String tenantId,
            @Param("serviceType") String serviceType);
}