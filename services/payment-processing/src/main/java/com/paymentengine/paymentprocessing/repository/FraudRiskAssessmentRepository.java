package com.paymentengine.paymentprocessing.repository;

import com.paymentengine.paymentprocessing.entity.FraudRiskAssessment;
import com.paymentengine.paymentprocessing.entity.FraudRiskConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Fraud Risk Assessment entities
 */
@Repository
public interface FraudRiskAssessmentRepository extends JpaRepository<FraudRiskAssessment, UUID> {
    
    /**
     * Find fraud risk assessments by assessment ID
     */
    Optional<FraudRiskAssessment> findByAssessmentId(String assessmentId);
    
    /**
     * Find fraud risk assessments by transaction reference
     */
    List<FraudRiskAssessment> findByTransactionReference(String transactionReference);
    
    /**
     * Find fraud risk assessments by tenant ID
     */
    List<FraudRiskAssessment> findByTenantId(String tenantId);
    
    /**
     * Find fraud risk assessments by tenant ID and status
     */
    List<FraudRiskAssessment> findByTenantIdAndStatus(String tenantId, FraudRiskAssessment.AssessmentStatus status);
    
    /**
     * Find fraud risk assessments by tenant ID and decision
     */
    List<FraudRiskAssessment> findByTenantIdAndDecision(String tenantId, FraudRiskAssessment.Decision decision);
    
    /**
     * Find fraud risk assessments by tenant ID and risk level
     */
    List<FraudRiskAssessment> findByTenantIdAndRiskLevel(String tenantId, FraudRiskAssessment.RiskLevel riskLevel);
    
    /**
     * Find fraud risk assessments by tenant ID and payment source
     */
    List<FraudRiskAssessment> findByTenantIdAndPaymentSource(String tenantId, FraudRiskConfiguration.PaymentSource paymentSource);
    
    /**
     * Find fraud risk assessments by tenant ID and risk assessment type
     */
    List<FraudRiskAssessment> findByTenantIdAndRiskAssessmentType(String tenantId, FraudRiskConfiguration.RiskAssessmentType riskAssessmentType);
    
    /**
     * Find fraud risk assessments by tenant ID and payment type
     */
    List<FraudRiskAssessment> findByTenantIdAndPaymentType(String tenantId, String paymentType);
    
    /**
     * Find fraud risk assessments by tenant ID and local instrumentation code
     */
    List<FraudRiskAssessment> findByTenantIdAndLocalInstrumentationCode(String tenantId, String localInstrumentationCode);
    
    /**
     * Find fraud risk assessments by tenant ID and clearing system code
     */
    List<FraudRiskAssessment> findByTenantIdAndClearingSystemCode(String tenantId, String clearingSystemCode);
    
    /**
     * Find fraud risk assessments by status
     */
    List<FraudRiskAssessment> findByStatus(FraudRiskAssessment.AssessmentStatus status);
    
    /**
     * Find fraud risk assessments by decision
     */
    List<FraudRiskAssessment> findByDecision(FraudRiskAssessment.Decision decision);
    
    /**
     * Find fraud risk assessments by risk level
     */
    List<FraudRiskAssessment> findByRiskLevel(FraudRiskAssessment.RiskLevel riskLevel);
    
    /**
     * Find fraud risk assessments by payment source
     */
    List<FraudRiskAssessment> findByPaymentSource(FraudRiskConfiguration.PaymentSource paymentSource);
    
    /**
     * Find fraud risk assessments by risk assessment type
     */
    List<FraudRiskAssessment> findByRiskAssessmentType(FraudRiskConfiguration.RiskAssessmentType riskAssessmentType);
    
    /**
     * Find fraud risk assessments by external API used
     */
    List<FraudRiskAssessment> findByExternalApiUsed(String externalApiUsed);
    
    /**
     * Find fraud risk assessments by configuration ID
     */
    List<FraudRiskAssessment> findByConfigurationId(UUID configurationId);
    
    /**
     * Find fraud risk assessments by assessed date range
     */
    @Query("SELECT fra FROM FraudRiskAssessment fra WHERE fra.assessedAt BETWEEN :startDate AND :endDate ORDER BY fra.assessedAt DESC")
    List<FraudRiskAssessment> findByAssessedAtBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    /**
     * Find fraud risk assessments by tenant ID and assessed date range
     */
    @Query("SELECT fra FROM FraudRiskAssessment fra WHERE fra.tenantId = :tenantId AND fra.assessedAt BETWEEN :startDate AND :endDate ORDER BY fra.assessedAt DESC")
    List<FraudRiskAssessment> findByTenantIdAndAssessedAtBetween(
        @Param("tenantId") String tenantId, 
        @Param("startDate") LocalDateTime startDate, 
        @Param("endDate") LocalDateTime endDate);
    
    /**
     * Find fraud risk assessments by risk score range
     */
    @Query("SELECT fra FROM FraudRiskAssessment fra WHERE fra.riskScore BETWEEN :minScore AND :maxScore ORDER BY fra.riskScore DESC")
    List<FraudRiskAssessment> findByRiskScoreBetween(@Param("minScore") java.math.BigDecimal minScore, @Param("maxScore") java.math.BigDecimal maxScore);
    
    /**
     * Find fraud risk assessments by tenant ID and risk score range
     */
    @Query("SELECT fra FROM FraudRiskAssessment fra WHERE fra.tenantId = :tenantId AND fra.riskScore BETWEEN :minScore AND :maxScore ORDER BY fra.riskScore DESC")
    List<FraudRiskAssessment> findByTenantIdAndRiskScoreBetween(
        @Param("tenantId") String tenantId, 
        @Param("minScore") java.math.BigDecimal minScore, 
        @Param("maxScore") java.math.BigDecimal maxScore);
    
    /**
     * Find fraud risk assessments by processing time range
     */
    @Query("SELECT fra FROM FraudRiskAssessment fra WHERE fra.processingTimeMs BETWEEN :minTime AND :maxTime ORDER BY fra.processingTimeMs DESC")
    List<FraudRiskAssessment> findByProcessingTimeMsBetween(@Param("minTime") Long minTime, @Param("maxTime") Long maxTime);
    
    /**
     * Find fraud risk assessments by external API response time range
     */
    @Query("SELECT fra FROM FraudRiskAssessment fra WHERE fra.externalApiResponseTimeMs BETWEEN :minTime AND :maxTime ORDER BY fra.externalApiResponseTimeMs DESC")
    List<FraudRiskAssessment> findByExternalApiResponseTimeMsBetween(@Param("minTime") Long minTime, @Param("maxTime") Long maxTime);
    
    /**
     * Find fraud risk assessments by retry count
     */
    List<FraudRiskAssessment> findByRetryCount(Integer retryCount);
    
    /**
     * Find fraud risk assessments by retry count greater than
     */
    @Query("SELECT fra FROM FraudRiskAssessment fra WHERE fra.retryCount > :retryCount ORDER BY fra.retryCount DESC")
    List<FraudRiskAssessment> findByRetryCountGreaterThan(@Param("retryCount") Integer retryCount);
    
    /**
     * Find fraud risk assessments by tenant ID and retry count greater than
     */
    @Query("SELECT fra FROM FraudRiskAssessment fra WHERE fra.tenantId = :tenantId AND fra.retryCount > :retryCount ORDER BY fra.retryCount DESC")
    List<FraudRiskAssessment> findByTenantIdAndRetryCountGreaterThan(@Param("tenantId") String tenantId, @Param("retryCount") Integer retryCount);
    
    /**
     * Find expired fraud risk assessments
     */
    @Query("SELECT fra FROM FraudRiskAssessment fra WHERE fra.expiresAt IS NOT NULL AND fra.expiresAt < :currentTime ORDER BY fra.expiresAt ASC")
    List<FraudRiskAssessment> findExpiredAssessments(@Param("currentTime") LocalDateTime currentTime);
    
    /**
     * Find fraud risk assessments that need retry
     */
    @Query("SELECT fra FROM FraudRiskAssessment fra WHERE fra.status IN ('FAILED', 'ERROR', 'TIMEOUT') AND fra.retryCount < :maxRetries ORDER BY fra.updatedAt ASC")
    List<FraudRiskAssessment> findAssessmentsNeedingRetry(@Param("maxRetries") Integer maxRetries);
    
    /**
     * Find fraud risk assessments by tenant ID that need retry
     */
    @Query("SELECT fra FROM FraudRiskAssessment fra WHERE fra.tenantId = :tenantId AND fra.status IN ('FAILED', 'ERROR', 'TIMEOUT') AND fra.retryCount < :maxRetries ORDER BY fra.updatedAt ASC")
    List<FraudRiskAssessment> findByTenantIdAndAssessmentsNeedingRetry(@Param("tenantId") String tenantId, @Param("maxRetries") Integer maxRetries);
    
    /**
     * Find pending fraud risk assessments
     */
    @Query("SELECT fra FROM FraudRiskAssessment fra WHERE fra.status IN ('PENDING', 'IN_PROGRESS') ORDER BY fra.assessedAt ASC")
    List<FraudRiskAssessment> findPendingAssessments();
    
    /**
     * Find pending fraud risk assessments by tenant ID
     */
    @Query("SELECT fra FROM FraudRiskAssessment fra WHERE fra.tenantId = :tenantId AND fra.status IN ('PENDING', 'IN_PROGRESS') ORDER BY fra.assessedAt ASC")
    List<FraudRiskAssessment> findPendingAssessmentsByTenantId(@Param("tenantId") String tenantId);
    
    /**
     * Find completed fraud risk assessments
     */
    @Query("SELECT fra FROM FraudRiskAssessment fra WHERE fra.status = 'COMPLETED' ORDER BY fra.assessedAt DESC")
    List<FraudRiskAssessment> findCompletedAssessments();
    
    /**
     * Find completed fraud risk assessments by tenant ID
     */
    @Query("SELECT fra FROM FraudRiskAssessment fra WHERE fra.tenantId = :tenantId AND fra.status = 'COMPLETED' ORDER BY fra.assessedAt DESC")
    List<FraudRiskAssessment> findCompletedAssessmentsByTenantId(@Param("tenantId") String tenantId);
    
    /**
     * Find failed fraud risk assessments
     */
    @Query("SELECT fra FROM FraudRiskAssessment fra WHERE fra.status IN ('FAILED', 'ERROR', 'TIMEOUT') ORDER BY fra.updatedAt DESC")
    List<FraudRiskAssessment> findFailedAssessments();
    
    /**
     * Find failed fraud risk assessments by tenant ID
     */
    @Query("SELECT fra FROM FraudRiskAssessment fra WHERE fra.tenantId = :tenantId AND fra.status IN ('FAILED', 'ERROR', 'TIMEOUT') ORDER BY fra.updatedAt DESC")
    List<FraudRiskAssessment> findFailedAssessmentsByTenantId(@Param("tenantId") String tenantId);
    
    /**
     * Find approved fraud risk assessments
     */
    @Query("SELECT fra FROM FraudRiskAssessment fra WHERE fra.decision = 'APPROVE' ORDER BY fra.assessedAt DESC")
    List<FraudRiskAssessment> findApprovedAssessments();
    
    /**
     * Find approved fraud risk assessments by tenant ID
     */
    @Query("SELECT fra FROM FraudRiskAssessment fra WHERE fra.tenantId = :tenantId AND fra.decision = 'APPROVE' ORDER BY fra.assessedAt DESC")
    List<FraudRiskAssessment> findApprovedAssessmentsByTenantId(@Param("tenantId") String tenantId);
    
    /**
     * Find rejected fraud risk assessments
     */
    @Query("SELECT fra FROM FraudRiskAssessment fra WHERE fra.decision = 'REJECT' ORDER BY fra.assessedAt DESC")
    List<FraudRiskAssessment> findRejectedAssessments();
    
    /**
     * Find rejected fraud risk assessments by tenant ID
     */
    @Query("SELECT fra FROM FraudRiskAssessment fra WHERE fra.tenantId = :tenantId AND fra.decision = 'REJECT' ORDER BY fra.assessedAt DESC")
    List<FraudRiskAssessment> findRejectedAssessmentsByTenantId(@Param("tenantId") String tenantId);
    
    /**
     * Find fraud risk assessments requiring manual review
     */
    @Query("SELECT fra FROM FraudRiskAssessment fra WHERE fra.decision = 'MANUAL_REVIEW' ORDER BY fra.assessedAt ASC")
    List<FraudRiskAssessment> findManualReviewAssessments();
    
    /**
     * Find fraud risk assessments requiring manual review by tenant ID
     */
    @Query("SELECT fra FROM FraudRiskAssessment fra WHERE fra.tenantId = :tenantId AND fra.decision = 'MANUAL_REVIEW' ORDER BY fra.assessedAt ASC")
    List<FraudRiskAssessment> findManualReviewAssessmentsByTenantId(@Param("tenantId") String tenantId);
    
    /**
     * Find high risk fraud risk assessments
     */
    @Query("SELECT fra FROM FraudRiskAssessment fra WHERE fra.riskLevel = 'HIGH' ORDER BY fra.riskScore DESC")
    List<FraudRiskAssessment> findHighRiskAssessments();
    
    /**
     * Find high risk fraud risk assessments by tenant ID
     */
    @Query("SELECT fra FROM FraudRiskAssessment fra WHERE fra.tenantId = :tenantId AND fra.riskLevel = 'HIGH' ORDER BY fra.riskScore DESC")
    List<FraudRiskAssessment> findHighRiskAssessmentsByTenantId(@Param("tenantId") String tenantId);
    
    /**
     * Find critical risk fraud risk assessments
     */
    @Query("SELECT fra FROM FraudRiskAssessment fra WHERE fra.riskLevel = 'CRITICAL' ORDER BY fra.riskScore DESC")
    List<FraudRiskAssessment> findCriticalRiskAssessments();
    
    /**
     * Find critical risk fraud risk assessments by tenant ID
     */
    @Query("SELECT fra FROM FraudRiskAssessment fra WHERE fra.tenantId = :tenantId AND fra.riskLevel = 'CRITICAL' ORDER BY fra.riskScore DESC")
    List<FraudRiskAssessment> findCriticalRiskAssessmentsByTenantId(@Param("tenantId") String tenantId);
    
    /**
     * Count fraud risk assessments by tenant ID
     */
    long countByTenantId(String tenantId);
    
    /**
     * Count fraud risk assessments by tenant ID and status
     */
    long countByTenantIdAndStatus(String tenantId, FraudRiskAssessment.AssessmentStatus status);
    
    /**
     * Count fraud risk assessments by tenant ID and decision
     */
    long countByTenantIdAndDecision(String tenantId, FraudRiskAssessment.Decision decision);
    
    /**
     * Count fraud risk assessments by tenant ID and risk level
     */
    long countByTenantIdAndRiskLevel(String tenantId, FraudRiskAssessment.RiskLevel riskLevel);
    
    /**
     * Count fraud risk assessments by tenant ID and payment source
     */
    long countByTenantIdAndPaymentSource(String tenantId, FraudRiskConfiguration.PaymentSource paymentSource);
    
    /**
     * Count fraud risk assessments by status
     */
    long countByStatus(FraudRiskAssessment.AssessmentStatus status);
    
    /**
     * Count fraud risk assessments by decision
     */
    long countByDecision(FraudRiskAssessment.Decision decision);
    
    /**
     * Count fraud risk assessments by risk level
     */
    long countByRiskLevel(FraudRiskAssessment.RiskLevel riskLevel);
    
    /**
     * Count fraud risk assessments by payment source
     */
    long countByPaymentSource(FraudRiskConfiguration.PaymentSource paymentSource);
    
    /**
     * Count fraud risk assessments by risk assessment type
     */
    long countByRiskAssessmentType(FraudRiskConfiguration.RiskAssessmentType riskAssessmentType);
    
    /**
     * Count fraud risk assessments by external API used
     */
    long countByExternalApiUsed(String externalApiUsed);
    
    /**
     * Count fraud risk assessments by configuration ID
     */
    long countByConfigurationId(UUID configurationId);
    
    /**
     * Count fraud risk assessments by tenant ID and assessed date range
     */
    @Query("SELECT COUNT(fra) FROM FraudRiskAssessment fra WHERE fra.tenantId = :tenantId AND fra.assessedAt BETWEEN :startDate AND :endDate")
    long countByTenantIdAndAssessedAtBetween(
        @Param("tenantId") String tenantId, 
        @Param("startDate") LocalDateTime startDate, 
        @Param("endDate") LocalDateTime endDate);
    
    /**
     * Count fraud risk assessments by tenant ID and risk score range
     */
    @Query("SELECT COUNT(fra) FROM FraudRiskAssessment fra WHERE fra.tenantId = :tenantId AND fra.riskScore BETWEEN :minScore AND :maxScore")
    long countByTenantIdAndRiskScoreBetween(
        @Param("tenantId") String tenantId, 
        @Param("minScore") java.math.BigDecimal minScore, 
        @Param("maxScore") java.math.BigDecimal maxScore);
    
    /**
     * Count pending fraud risk assessments
     */
    @Query("SELECT COUNT(fra) FROM FraudRiskAssessment fra WHERE fra.status IN ('PENDING', 'IN_PROGRESS')")
    long countPendingAssessments();
    
    /**
     * Count pending fraud risk assessments by tenant ID
     */
    @Query("SELECT COUNT(fra) FROM FraudRiskAssessment fra WHERE fra.tenantId = :tenantId AND fra.status IN ('PENDING', 'IN_PROGRESS')")
    long countPendingAssessmentsByTenantId(@Param("tenantId") String tenantId);
    
    /**
     * Count completed fraud risk assessments
     */
    @Query("SELECT COUNT(fra) FROM FraudRiskAssessment fra WHERE fra.status = 'COMPLETED'")
    long countCompletedAssessments();
    
    /**
     * Count completed fraud risk assessments by tenant ID
     */
    @Query("SELECT COUNT(fra) FROM FraudRiskAssessment fra WHERE fra.tenantId = :tenantId AND fra.status = 'COMPLETED'")
    long countCompletedAssessmentsByTenantId(@Param("tenantId") String tenantId);
    
    /**
     * Count failed fraud risk assessments
     */
    @Query("SELECT COUNT(fra) FROM FraudRiskAssessment fra WHERE fra.status IN ('FAILED', 'ERROR', 'TIMEOUT')")
    long countFailedAssessments();
    
    /**
     * Count failed fraud risk assessments by tenant ID
     */
    @Query("SELECT COUNT(fra) FROM FraudRiskAssessment fra WHERE fra.tenantId = :tenantId AND fra.status IN ('FAILED', 'ERROR', 'TIMEOUT')")
    long countFailedAssessmentsByTenantId(@Param("tenantId") String tenantId);
    
    /**
     * Count approved fraud risk assessments
     */
    @Query("SELECT COUNT(fra) FROM FraudRiskAssessment fra WHERE fra.decision = 'APPROVE'")
    long countApprovedAssessments();
    
    /**
     * Count approved fraud risk assessments by tenant ID
     */
    @Query("SELECT COUNT(fra) FROM FraudRiskAssessment fra WHERE fra.tenantId = :tenantId AND fra.decision = 'APPROVE'")
    long countApprovedAssessmentsByTenantId(@Param("tenantId") String tenantId);
    
    /**
     * Count rejected fraud risk assessments
     */
    @Query("SELECT COUNT(fra) FROM FraudRiskAssessment fra WHERE fra.decision = 'REJECT'")
    long countRejectedAssessments();
    
    /**
     * Count rejected fraud risk assessments by tenant ID
     */
    @Query("SELECT COUNT(fra) FROM FraudRiskAssessment fra WHERE fra.tenantId = :tenantId AND fra.decision = 'REJECT'")
    long countRejectedAssessmentsByTenantId(@Param("tenantId") String tenantId);
    
    /**
     * Count fraud risk assessments requiring manual review
     */
    @Query("SELECT COUNT(fra) FROM FraudRiskAssessment fra WHERE fra.decision = 'MANUAL_REVIEW'")
    long countManualReviewAssessments();
    
    /**
     * Count fraud risk assessments requiring manual review by tenant ID
     */
    @Query("SELECT COUNT(fra) FROM FraudRiskAssessment fra WHERE fra.tenantId = :tenantId AND fra.decision = 'MANUAL_REVIEW'")
    long countManualReviewAssessmentsByTenantId(@Param("tenantId") String tenantId);
    
    /**
     * Count high risk fraud risk assessments
     */
    @Query("SELECT COUNT(fra) FROM FraudRiskAssessment fra WHERE fra.riskLevel = 'HIGH'")
    long countHighRiskAssessments();
    
    /**
     * Count high risk fraud risk assessments by tenant ID
     */
    @Query("SELECT COUNT(fra) FROM FraudRiskAssessment fra WHERE fra.tenantId = :tenantId AND fra.riskLevel = 'HIGH'")
    long countHighRiskAssessmentsByTenantId(@Param("tenantId") String tenantId);
    
    /**
     * Count critical risk fraud risk assessments
     */
    @Query("SELECT COUNT(fra) FROM FraudRiskAssessment fra WHERE fra.riskLevel = 'CRITICAL'")
    long countCriticalRiskAssessments();
    
    /**
     * Count critical risk fraud risk assessments by tenant ID
     */
    @Query("SELECT COUNT(fra) FROM FraudRiskAssessment fra WHERE fra.tenantId = :tenantId AND fra.riskLevel = 'CRITICAL'")
    long countCriticalRiskAssessmentsByTenantId(@Param("tenantId") String tenantId);
    
    /**
     * Find fraud risk assessments by tenant ID and external API used
     */
    List<FraudRiskAssessment> findByTenantIdAndExternalApiUsed(String tenantId, String externalApiUsed);
    
    /**
     * Find fraud risk assessments by tenant ID and configuration ID
     */
    List<FraudRiskAssessment> findByTenantIdAndConfigurationId(String tenantId, UUID configurationId);
    
    /**
     * Find fraud risk assessments by tenant ID, payment type, and local instrumentation code
     */
    List<FraudRiskAssessment> findByTenantIdAndPaymentTypeAndLocalInstrumentationCode(
        String tenantId, String paymentType, String localInstrumentationCode);
    
    /**
     * Find fraud risk assessments by tenant ID, payment type, local instrumentation code, and clearing system code
     */
    List<FraudRiskAssessment> findByTenantIdAndPaymentTypeAndLocalInstrumentationCodeAndClearingSystemCode(
        String tenantId, String paymentType, String localInstrumentationCode, String clearingSystemCode);
}