package com.payments.samosadapter.repository;

import com.payments.samosadapter.domain.SamosPaymentMessage;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * SAMOS Payment Message Repository
 *
 * <p>Data access layer for SAMOS payment messages with tenant isolation.
 */
@Repository
public interface SamosPaymentMessageRepository extends JpaRepository<SamosPaymentMessage, String> {

  /** Find payment message by message ID */
  @Query("SELECT spm FROM SamosPaymentMessage spm WHERE spm.messageId = :messageId")
  Optional<SamosPaymentMessage> findByMessageId(@Param("messageId") String messageId);

  /** Find payment messages by payment ID */
  @Query(
      "SELECT spm FROM SamosPaymentMessage spm WHERE spm.tenantContext.tenantId = :tenantId AND spm.paymentId = :paymentId")
  List<SamosPaymentMessage> findByTenantIdAndPaymentId(
      @Param("tenantId") UUID tenantId, @Param("paymentId") String paymentId);

  /** Find payment messages by status */
  @Query(
      "SELECT spm FROM SamosPaymentMessage spm WHERE spm.tenantContext.tenantId = :tenantId AND spm.status = :status")
  List<SamosPaymentMessage> findByTenantIdAndStatus(
      @Param("tenantId") UUID tenantId, @Param("status") SamosPaymentMessage.MessageStatus status);

  /** Find payment messages by message type */
  @Query(
      "SELECT spm FROM SamosPaymentMessage spm WHERE spm.tenantContext.tenantId = :tenantId AND spm.messageType = :messageType")
  List<SamosPaymentMessage> findByTenantIdAndMessageType(
      @Param("tenantId") UUID tenantId,
      @Param("messageType") SamosPaymentMessage.MessageType messageType);

  /** Find payment messages by direction */
  @Query(
      "SELECT spm FROM SamosPaymentMessage spm WHERE spm.tenantContext.tenantId = :tenantId AND spm.direction = :direction")
  List<SamosPaymentMessage> findByTenantIdAndDirection(
      @Param("tenantId") UUID tenantId,
      @Param("direction") SamosPaymentMessage.Direction direction);

  /** Find payment messages by date range */
  @Query(
      "SELECT spm FROM SamosPaymentMessage spm WHERE spm.tenantContext.tenantId = :tenantId AND spm.createdAt BETWEEN :startDate AND :endDate")
  List<SamosPaymentMessage> findByTenantIdAndCreatedAtBetween(
      @Param("tenantId") UUID tenantId,
      @Param("startDate") Instant startDate,
      @Param("endDate") Instant endDate);

  /** Find payment messages with pagination */
  @Query("SELECT spm FROM SamosPaymentMessage spm WHERE spm.tenantContext.tenantId = :tenantId")
  Page<SamosPaymentMessage> findByTenantId(@Param("tenantId") UUID tenantId, Pageable pageable);

  /** Find pending messages for processing */
  @Query(
      "SELECT spm FROM SamosPaymentMessage spm WHERE spm.tenantContext.tenantId = :tenantId AND spm.status = 'PENDING' AND spm.direction = 'OUTBOUND'")
  List<SamosPaymentMessage> findPendingOutboundMessages(@Param("tenantId") UUID tenantId);

  /** Count messages by status */
  @Query(
      "SELECT COUNT(spm) FROM SamosPaymentMessage spm WHERE spm.tenantContext.tenantId = :tenantId AND spm.status = :status")
  long countByTenantIdAndStatus(
      @Param("tenantId") UUID tenantId, @Param("status") SamosPaymentMessage.MessageStatus status);

  /** Find failed messages for retry */
  @Query(
      "SELECT spm FROM SamosPaymentMessage spm WHERE spm.tenantContext.tenantId = :tenantId AND spm.status = 'FAILED' AND spm.direction = 'OUTBOUND'")
  List<SamosPaymentMessage> findFailedOutboundMessages(@Param("tenantId") UUID tenantId);
}
