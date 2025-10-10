package com.paymentengine.paymentprocessing.repository;

import com.paymentengine.paymentprocessing.entity.FraudRiskConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Fraud Risk Configuration entities
 */
@Repository
public interface FraudRiskConfigurationRepository extends JpaRepository<FraudRiskConfiguration, UUID> {
    
    /**
     * Find fraud risk configurations by tenant ID
     */
    List<FraudRiskConfiguration> findByTenantId(String tenantId);
    
    /**
     * Find active fraud risk configurations by tenant ID
     */
    @Query("SELECT frc FROM FraudRiskConfiguration frc WHERE frc.tenantId = :tenantId AND frc.isEnabled = true ORDER BY frc.priority ASC")
    List<FraudRiskConfiguration> findActiveByTenantId(@Param("tenantId") String tenantId);
    
    /**
     * Find fraud risk configurations by tenant ID and payment source
     */
    List<FraudRiskConfiguration> findByTenantIdAndPaymentSource(String tenantId, FraudRiskConfiguration.PaymentSource paymentSource);
    
    /**
     * Find active fraud risk configurations by tenant ID and payment source
     */
    @Query("SELECT frc FROM FraudRiskConfiguration frc WHERE frc.tenantId = :tenantId AND frc.paymentSource = :paymentSource AND frc.isEnabled = true ORDER BY frc.priority ASC")
    List<FraudRiskConfiguration> findActiveByTenantIdAndPaymentSource(
        @Param("tenantId") String tenantId, 
        @Param("paymentSource") FraudRiskConfiguration.PaymentSource paymentSource);
    
    /**
     * Find fraud risk configurations by tenant ID and risk assessment type
     */
    List<FraudRiskConfiguration> findByTenantIdAndRiskAssessmentType(
        String tenantId, 
        FraudRiskConfiguration.RiskAssessmentType riskAssessmentType);
    
    /**
     * Find active fraud risk configurations by tenant ID and risk assessment type
     */
    @Query("SELECT frc FROM FraudRiskConfiguration frc WHERE frc.tenantId = :tenantId AND frc.riskAssessmentType = :riskAssessmentType AND frc.isEnabled = true ORDER BY frc.priority ASC")
    List<FraudRiskConfiguration> findActiveByTenantIdAndRiskAssessmentType(
        @Param("tenantId") String tenantId, 
        @Param("riskAssessmentType") FraudRiskConfiguration.RiskAssessmentType riskAssessmentType);
    
    /**
     * Find fraud risk configurations by tenant ID and payment type
     */
    List<FraudRiskConfiguration> findByTenantIdAndPaymentType(String tenantId, String paymentType);
    
    /**
     * Find active fraud risk configurations by tenant ID and payment type
     */
    @Query("SELECT frc FROM FraudRiskConfiguration frc WHERE frc.tenantId = :tenantId AND frc.paymentType = :paymentType AND frc.isEnabled = true ORDER BY frc.priority ASC")
    List<FraudRiskConfiguration> findActiveByTenantIdAndPaymentType(
        @Param("tenantId") String tenantId, 
        @Param("paymentType") String paymentType);
    
    /**
     * Find fraud risk configurations by tenant ID and local instrumentation code
     */
    List<FraudRiskConfiguration> findByTenantIdAndLocalInstrumentationCode(String tenantId, String localInstrumentationCode);
    
    /**
     * Find active fraud risk configurations by tenant ID and local instrumentation code
     */
    @Query("SELECT frc FROM FraudRiskConfiguration frc WHERE frc.tenantId = :tenantId AND frc.localInstrumentationCode = :localInstrumentationCode AND frc.isEnabled = true ORDER BY frc.priority ASC")
    List<FraudRiskConfiguration> findActiveByTenantIdAndLocalInstrumentationCode(
        @Param("tenantId") String tenantId, 
        @Param("localInstrumentationCode") String localInstrumentationCode);
    
    /**
     * Find fraud risk configurations by tenant ID and clearing system code
     */
    List<FraudRiskConfiguration> findByTenantIdAndClearingSystemCode(String tenantId, String clearingSystemCode);
    
    /**
     * Find active fraud risk configurations by tenant ID and clearing system code
     */
    @Query("SELECT frc FROM FraudRiskConfiguration frc WHERE frc.tenantId = :tenantId AND frc.clearingSystemCode = :clearingSystemCode AND frc.isEnabled = true ORDER BY frc.priority ASC")
    List<FraudRiskConfiguration> findActiveByTenantIdAndClearingSystemCode(
        @Param("tenantId") String tenantId, 
        @Param("clearingSystemCode") String clearingSystemCode);
    
    /**
     * Find fraud risk configurations by payment source
     */
    List<FraudRiskConfiguration> findByPaymentSource(FraudRiskConfiguration.PaymentSource paymentSource);
    
    /**
     * Find active fraud risk configurations by payment source
     */
    @Query("SELECT frc FROM FraudRiskConfiguration frc WHERE frc.paymentSource = :paymentSource AND frc.isEnabled = true ORDER BY frc.priority ASC")
    List<FraudRiskConfiguration> findActiveByPaymentSource(@Param("paymentSource") FraudRiskConfiguration.PaymentSource paymentSource);
    
    /**
     * Find fraud risk configurations by risk assessment type
     */
    List<FraudRiskConfiguration> findByRiskAssessmentType(FraudRiskConfiguration.RiskAssessmentType riskAssessmentType);
    
    /**
     * Find active fraud risk configurations by risk assessment type
     */
    @Query("SELECT frc FROM FraudRiskConfiguration frc WHERE frc.riskAssessmentType = :riskAssessmentType AND frc.isEnabled = true ORDER BY frc.priority ASC")
    List<FraudRiskConfiguration> findActiveByRiskAssessmentType(@Param("riskAssessmentType") FraudRiskConfiguration.RiskAssessmentType riskAssessmentType);
    
    /**
     * Find fraud risk configurations by tenant ID, payment type, and local instrumentation code
     */
    List<FraudRiskConfiguration> findByTenantIdAndPaymentTypeAndLocalInstrumentationCode(
        String tenantId, String paymentType, String localInstrumentationCode);
    
    /**
     * Find active fraud risk configurations by tenant ID, payment type, and local instrumentation code
     */
    @Query("SELECT frc FROM FraudRiskConfiguration frc WHERE frc.tenantId = :tenantId AND frc.paymentType = :paymentType AND frc.localInstrumentationCode = :localInstrumentationCode AND frc.isEnabled = true ORDER BY frc.priority ASC")
    List<FraudRiskConfiguration> findActiveByTenantIdAndPaymentTypeAndLocalInstrumentationCode(
        @Param("tenantId") String tenantId, 
        @Param("paymentType") String paymentType, 
        @Param("localInstrumentationCode") String localInstrumentationCode);
    
    /**
     * Find fraud risk configurations by tenant ID, payment type, local instrumentation code, and clearing system code
     */
    List<FraudRiskConfiguration> findByTenantIdAndPaymentTypeAndLocalInstrumentationCodeAndClearingSystemCode(
        String tenantId, String paymentType, String localInstrumentationCode, String clearingSystemCode);
    
    /**
     * Find active fraud risk configurations by tenant ID, payment type, local instrumentation code, and clearing system code
     */
    @Query("SELECT frc FROM FraudRiskConfiguration frc WHERE frc.tenantId = :tenantId AND frc.paymentType = :paymentType AND frc.localInstrumentationCode = :localInstrumentationCode AND frc.clearingSystemCode = :clearingSystemCode AND frc.isEnabled = true ORDER BY frc.priority ASC")
    List<FraudRiskConfiguration> findActiveByTenantIdAndPaymentTypeAndLocalInstrumentationCodeAndClearingSystemCode(
        @Param("tenantId") String tenantId, 
        @Param("paymentType") String paymentType, 
        @Param("localInstrumentationCode") String localInstrumentationCode, 
        @Param("clearingSystemCode") String clearingSystemCode);
    
    /**
     * Find fraud risk configurations by configuration name
     */
    List<FraudRiskConfiguration> findByConfigurationName(String configurationName);
    
    /**
     * Find active fraud risk configurations by configuration name
     */
    @Query("SELECT frc FROM FraudRiskConfiguration frc WHERE frc.configurationName = :configurationName AND frc.isEnabled = true")
    List<FraudRiskConfiguration> findActiveByConfigurationName(@Param("configurationName") String configurationName);
    
    /**
     * Find fraud risk configuration by tenant ID and configuration name
     */
    Optional<FraudRiskConfiguration> findByTenantIdAndConfigurationName(String tenantId, String configurationName);
    
    /**
     * Find active fraud risk configuration by tenant ID and configuration name
     */
    @Query("SELECT frc FROM FraudRiskConfiguration frc WHERE frc.tenantId = :tenantId AND frc.configurationName = :configurationName AND frc.isEnabled = true")
    Optional<FraudRiskConfiguration> findActiveByTenantIdAndConfigurationName(
        @Param("tenantId") String tenantId, 
        @Param("configurationName") String configurationName);
    
    /**
     * Find fraud risk configurations by version
     */
    List<FraudRiskConfiguration> findByVersion(String version);
    
    /**
     * Find active fraud risk configurations by version
     */
    @Query("SELECT frc FROM FraudRiskConfiguration frc WHERE frc.version = :version AND frc.isEnabled = true ORDER BY frc.priority ASC")
    List<FraudRiskConfiguration> findActiveByVersion(@Param("version") String version);
    
    /**
     * Find fraud risk configurations by priority range
     */
    @Query("SELECT frc FROM FraudRiskConfiguration frc WHERE frc.priority BETWEEN :minPriority AND :maxPriority ORDER BY frc.priority ASC")
    List<FraudRiskConfiguration> findByPriorityRange(@Param("minPriority") Integer minPriority, @Param("maxPriority") Integer maxPriority);
    
    /**
     * Find active fraud risk configurations by priority range
     */
    @Query("SELECT frc FROM FraudRiskConfiguration frc WHERE frc.priority BETWEEN :minPriority AND :maxPriority AND frc.isEnabled = true ORDER BY frc.priority ASC")
    List<FraudRiskConfiguration> findActiveByPriorityRange(@Param("minPriority") Integer minPriority, @Param("maxPriority") Integer maxPriority);
    
    /**
     * Find all active fraud risk configurations
     */
    @Query("SELECT frc FROM FraudRiskConfiguration frc WHERE frc.isEnabled = true ORDER BY frc.priority ASC, frc.createdAt ASC")
    List<FraudRiskConfiguration> findAllActive();
    
    /**
     * Find fraud risk configurations with external API configuration
     */
    @Query("SELECT frc FROM FraudRiskConfiguration frc WHERE frc.externalApiConfig IS NOT NULL AND frc.isEnabled = true")
    List<FraudRiskConfiguration> findWithExternalApiConfig();
    
    /**
     * Find fraud risk configurations with risk rules
     */
    @Query("SELECT frc FROM FraudRiskConfiguration frc WHERE frc.riskRules IS NOT NULL AND frc.isEnabled = true")
    List<FraudRiskConfiguration> findWithRiskRules();
    
    /**
     * Find fraud risk configurations with decision criteria
     */
    @Query("SELECT frc FROM FraudRiskConfiguration frc WHERE frc.decisionCriteria IS NOT NULL AND frc.isEnabled = true")
    List<FraudRiskConfiguration> findWithDecisionCriteria();
    
    /**
     * Find fraud risk configurations with thresholds
     */
    @Query("SELECT frc FROM FraudRiskConfiguration frc WHERE frc.thresholds IS NOT NULL AND frc.isEnabled = true")
    List<FraudRiskConfiguration> findWithThresholds();
    
    /**
     * Find fraud risk configurations by tenant ID with external API configuration
     */
    @Query("SELECT frc FROM FraudRiskConfiguration frc WHERE frc.tenantId = :tenantId AND frc.externalApiConfig IS NOT NULL AND frc.isEnabled = true ORDER BY frc.priority ASC")
    List<FraudRiskConfiguration> findByTenantIdWithExternalApiConfig(@Param("tenantId") String tenantId);
    
    /**
     * Find fraud risk configurations by tenant ID with risk rules
     */
    @Query("SELECT frc FROM FraudRiskConfiguration frc WHERE frc.tenantId = :tenantId AND frc.riskRules IS NOT NULL AND frc.isEnabled = true ORDER BY frc.priority ASC")
    List<FraudRiskConfiguration> findByTenantIdWithRiskRules(@Param("tenantId") String tenantId);
    
    /**
     * Find fraud risk configurations by tenant ID with decision criteria
     */
    @Query("SELECT frc FROM FraudRiskConfiguration frc WHERE frc.tenantId = :tenantId AND frc.decisionCriteria IS NOT NULL AND frc.isEnabled = true ORDER BY frc.priority ASC")
    List<FraudRiskConfiguration> findByTenantIdWithDecisionCriteria(@Param("tenantId") String tenantId);
    
    /**
     * Find fraud risk configurations by tenant ID with thresholds
     */
    @Query("SELECT frc FROM FraudRiskConfiguration frc WHERE frc.tenantId = :tenantId AND frc.thresholds IS NOT NULL AND frc.isEnabled = true ORDER BY frc.priority ASC")
    List<FraudRiskConfiguration> findByTenantIdWithThresholds(@Param("tenantId") String tenantId);
    
    /**
     * Check if fraud risk configuration exists for tenant and configuration name
     */
    boolean existsByTenantIdAndConfigurationName(String tenantId, String configurationName);
    
    /**
     * Check if active fraud risk configuration exists for tenant and configuration name
     */
    @Query("SELECT COUNT(frc) > 0 FROM FraudRiskConfiguration frc WHERE frc.tenantId = :tenantId AND frc.configurationName = :configurationName AND frc.isEnabled = true")
    boolean existsActiveByTenantIdAndConfigurationName(@Param("tenantId") String tenantId, @Param("configurationName") String configurationName);
    
    /**
     * Count fraud risk configurations by tenant ID
     */
    long countByTenantId(String tenantId);
    
    /**
     * Count active fraud risk configurations by tenant ID
     */
    @Query("SELECT COUNT(frc) FROM FraudRiskConfiguration frc WHERE frc.tenantId = :tenantId AND frc.isEnabled = true")
    long countActiveByTenantId(@Param("tenantId") String tenantId);
    
    /**
     * Count fraud risk configurations by payment source
     */
    long countByPaymentSource(FraudRiskConfiguration.PaymentSource paymentSource);
    
    /**
     * Count active fraud risk configurations by payment source
     */
    @Query("SELECT COUNT(frc) FROM FraudRiskConfiguration frc WHERE frc.paymentSource = :paymentSource AND frc.isEnabled = true")
    long countActiveByPaymentSource(@Param("paymentSource") FraudRiskConfiguration.PaymentSource paymentSource);
    
    /**
     * Count fraud risk configurations by risk assessment type
     */
    long countByRiskAssessmentType(FraudRiskConfiguration.RiskAssessmentType riskAssessmentType);
    
    /**
     * Count active fraud risk configurations by risk assessment type
     */
    @Query("SELECT COUNT(frc) FROM FraudRiskConfiguration frc WHERE frc.riskAssessmentType = :riskAssessmentType AND frc.isEnabled = true")
    long countActiveByRiskAssessmentType(@Param("riskAssessmentType") FraudRiskConfiguration.RiskAssessmentType riskAssessmentType);
    
    /**
     * Find fraud risk configurations created by specific user
     */
    List<FraudRiskConfiguration> findByCreatedBy(String createdBy);
    
    /**
     * Find fraud risk configurations updated by specific user
     */
    List<FraudRiskConfiguration> findByUpdatedBy(String updatedBy);
    
    /**
     * Find fraud risk configurations by description containing text
     */
    @Query("SELECT frc FROM FraudRiskConfiguration frc WHERE LOWER(frc.description) LIKE LOWER(CONCAT('%', :description, '%')) AND frc.isEnabled = true")
    List<FraudRiskConfiguration> findByDescriptionContaining(@Param("description") String description);
    
    /**
     * Find latest version of fraud risk configurations by tenant ID and configuration name
     */
    @Query("SELECT frc FROM FraudRiskConfiguration frc WHERE frc.tenantId = :tenantId AND frc.configurationName = :configurationName AND frc.isEnabled = true ORDER BY frc.version DESC")
    List<FraudRiskConfiguration> findLatestVersionByTenantIdAndConfigurationName(
        @Param("tenantId") String tenantId,
        @Param("configurationName") String configurationName);
    
    /**
     * Find fraud risk configurations with real-time risk assessment
     */
    @Query("SELECT frc FROM FraudRiskConfiguration frc WHERE frc.riskAssessmentType = 'REAL_TIME' AND frc.isEnabled = true")
    List<FraudRiskConfiguration> findRealTimeRiskAssessments();
    
    /**
     * Find fraud risk configurations with batch risk assessment
     */
    @Query("SELECT frc FROM FraudRiskConfiguration frc WHERE frc.riskAssessmentType = 'BATCH' AND frc.isEnabled = true")
    List<FraudRiskConfiguration> findBatchRiskAssessments();
    
    /**
     * Find fraud risk configurations for bank clients
     */
    @Query("SELECT frc FROM FraudRiskConfiguration frc WHERE frc.paymentSource = 'BANK_CLIENT' AND frc.isEnabled = true")
    List<FraudRiskConfiguration> findBankClientConfigurations();
    
    /**
     * Find fraud risk configurations for clearing systems
     */
    @Query("SELECT frc FROM FraudRiskConfiguration frc WHERE frc.paymentSource = 'CLEARING_SYSTEM' AND frc.isEnabled = true")
    List<FraudRiskConfiguration> findClearingSystemConfigurations();
    
    /**
     * Find fraud risk configurations for both sources
     */
    @Query("SELECT frc FROM FraudRiskConfiguration frc WHERE frc.paymentSource = 'BOTH' AND frc.isEnabled = true")
    List<FraudRiskConfiguration> findBothSourceConfigurations();
}