package com.payments.paymentinitiation.repository;

import com.payments.paymentinitiation.entity.PaymentRepairLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

/**
 * Payment Repair Log Repository
 * 
 * Repository for payment repair log entities.
 * Provides queries for repair history and audit trails.
 */
@Repository
public interface PaymentRepairLogRepository extends JpaRepository<PaymentRepairLogEntity, Long> {

    /**
     * Find repair logs by payment ID
     */
    List<PaymentRepairLogEntity> findByPaymentIdOrderByTimestampDesc(String paymentId);

    /**
     * Find repair logs by payment ID and action
     */
    List<PaymentRepairLogEntity> findByPaymentIdAndActionOrderByTimestampDesc(
        String paymentId, PaymentRepairLogEntity.RepairAction action);

    /**
     * Find repair logs by performed by user
     */
    List<PaymentRepairLogEntity> findByPerformedByOrderByTimestampDesc(String performedBy);

    /**
     * Find repair logs by action type
     */
    List<PaymentRepairLogEntity> findByActionOrderByTimestampDesc(PaymentRepairLogEntity.RepairAction action);

    /**
     * Find repair logs by time range
     */
    @Query("SELECT r FROM PaymentRepairLogEntity r WHERE r.timestamp BETWEEN :startTime AND :endTime ORDER BY r.timestamp DESC")
    List<PaymentRepairLogEntity> findByTimeRange(
        @Param("startTime") Instant startTime, 
        @Param("endTime") Instant endTime);

    /**
     * Find repair logs by payment ID and time range
     */
    @Query("SELECT r FROM PaymentRepairLogEntity r WHERE r.paymentId = :paymentId AND r.timestamp BETWEEN :startTime AND :endTime ORDER BY r.timestamp DESC")
    List<PaymentRepairLogEntity> findByPaymentIdAndTimeRange(
        @Param("paymentId") String paymentId,
        @Param("startTime") Instant startTime, 
        @Param("endTime") Instant endTime);

    /**
     * Count repair actions by payment ID
     */
    long countByPaymentId(String paymentId);

    /**
     * Count repair actions by action type
     */
    long countByAction(PaymentRepairLogEntity.RepairAction action);

    /**
     * Count repair actions by performed by user
     */
    long countByPerformedBy(String performedBy);

    /**
     * Find recent repair logs
     */
    @Query("SELECT r FROM PaymentRepairLogEntity r ORDER BY r.timestamp DESC")
    List<PaymentRepairLogEntity> findRecentRepairLogs();

    /**
     * Find repair logs by result
     */
    List<PaymentRepairLogEntity> findByResultContainingIgnoreCase(String result);

    /**
     * Find repair logs by reason
     */
    List<PaymentRepairLogEntity> findByReasonContainingIgnoreCase(String reason);
}
