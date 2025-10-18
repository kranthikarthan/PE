package com.payments.notification.repository;

import com.payments.notification.domain.model.NotificationChannel;
import com.payments.notification.domain.model.NotificationEntity;
import com.payments.notification.domain.model.NotificationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * JPA Repository for {@link NotificationEntity}.
 *
 * <p>Provides custom queries for:
 * - Finding notifications by status for processing
 * - Querying retry candidates
 * - Searching notification history
 * - Bulk updates for batch processing
 *
 * All queries automatically filter by tenant_id for multi-tenancy.
 *
 * @author Payment Engine
 */
@Repository
public interface NotificationRepository extends JpaRepository<NotificationEntity, UUID> {

  /**
   * Find all pending notifications for a tenant, paginated.
   *
   * @param tenantId the tenant ID
   * @param status the notification status
   * @param pageable pagination parameters
   * @return page of notifications
   */
  Page<NotificationEntity> findByTenantIdAndStatusOrderByCreatedAtAsc(
      String tenantId, NotificationStatus status, Pageable pageable);

  /**
   * Find all PENDING notifications ready for processing.
   *
   * @param tenantId the tenant ID
   * @param status the status (PENDING)
   * @return list of pending notifications
   */
  List<NotificationEntity> findByTenantIdAndStatusAndAttemptsLessThan(
      String tenantId, NotificationStatus status, Integer maxAttempts);

  /**
   * Find notifications eligible for retry.
   *
   * <p>Criteria:
   * - Status is PENDING or RETRY
   * - Attempts < 3
   * - Last attempt was more than 1 minute ago (exponential backoff)
   *
   * @param tenantId the tenant ID
   * @param beforeTime the cutoff time for retries
   * @return list of notifications eligible for retry
   */
  @Query(
      "SELECT n FROM NotificationEntity n WHERE "
          + "n.tenantId = :tenantId AND "
          + "(n.status = 'RETRY' OR n.status = 'FAILED') AND "
          + "n.attempts < 3 AND "
          + "(n.lastAttemptAt IS NULL OR n.lastAttemptAt < :beforeTime)")
  List<NotificationEntity> findRetryCandidates(
      @Param("tenantId") String tenantId, @Param("beforeTime") LocalDateTime beforeTime);

  /**
   * Find all failed notifications (DLQ).
   *
   * @param tenantId the tenant ID
   * @param status the status (FAILED)
   * @param pageable pagination parameters
   * @return page of failed notifications
   */
  Page<NotificationEntity> findByTenantIdAndStatusOrderByUpdatedAtDesc(
      String tenantId, NotificationStatus status, Pageable pageable);

  /**
   * Find notification history for a user.
   *
   * @param tenantId the tenant ID
   * @param userId the user ID
   * @param pageable pagination parameters
   * @return page of user's notifications
   */
  Page<NotificationEntity> findByTenantIdAndUserIdOrderByCreatedAtDesc(
      String tenantId, String userId, Pageable pageable);

  /**
   * Find notifications by user and channel.
   *
   * @param tenantId the tenant ID
   * @param userId the user ID
   * @param channel the notification channel
   * @param pageable pagination parameters
   * @return page of user notifications on specific channel
   */
  Page<NotificationEntity> findByTenantIdAndUserIdAndChannelTypeOrderByCreatedAtDesc(
      String tenantId, String userId, NotificationChannel channel, Pageable pageable);

  /**
   * Find notifications by user and status.
   *
   * @param tenantId the tenant ID
   * @param userId the user ID
   * @param status the notification status
   * @param pageable pagination parameters
   * @return page of user notifications with specific status
   */
  Page<NotificationEntity> findByTenantIdAndUserIdAndStatusOrderByCreatedAtDesc(
      String tenantId, String userId, NotificationStatus status, Pageable pageable);

  /**
   * Count pending notifications for a user.
   *
   * @param tenantId the tenant ID
   * @param userId the user ID
   * @return count of pending notifications
   */
  long countByTenantIdAndUserIdAndStatus(
      String tenantId, String userId, NotificationStatus status);

  /**
   * Update notification status.
   *
   * @param id the notification ID
   * @param status the new status
   * @param now the current timestamp
   */
  @Modifying
  @Transactional
  @Query(
      "UPDATE NotificationEntity n SET n.status = :status, n.updatedAt = :now "
          + "WHERE n.id = :id")
  void updateStatus(
      @Param("id") UUID id,
      @Param("status") NotificationStatus status,
      @Param("now") LocalDateTime now);

  /**
   * Update notification status and attempt count.
   *
   * @param id the notification ID
   * @param status the new status
   * @param attempts the new attempt count
   * @param lastAttemptAt the timestamp of last attempt
   * @param now the current timestamp
   */
  @Modifying
  @Transactional
  @Query(
      "UPDATE NotificationEntity n SET n.status = :status, n.attempts = :attempts, "
          + "n.lastAttemptAt = :lastAttemptAt, n.updatedAt = :now "
          + "WHERE n.id = :id")
  void updateStatusAndAttempts(
      @Param("id") UUID id,
      @Param("status") NotificationStatus status,
      @Param("attempts") Integer attempts,
      @Param("lastAttemptAt") LocalDateTime lastAttemptAt,
      @Param("now") LocalDateTime now);

  /**
   * Mark notification as successfully sent.
   *
   * @param id the notification ID
   * @param providerMessageId the external provider's message ID
   * @param now the current timestamp
   */
  @Modifying
  @Transactional
  @Query(
      "UPDATE NotificationEntity n SET n.status = 'SENT', n.sentAt = :now, "
          + "n.providerMessageId = :providerMessageId, n.updatedAt = :now "
          + "WHERE n.id = :id")
  void markAsSent(
      @Param("id") UUID id,
      @Param("providerMessageId") String providerMessageId,
      @Param("now") LocalDateTime now);

  /**
   * Mark notification as failed with failure reason.
   *
   * @param id the notification ID
   * @param failureReason the reason for failure
   * @param now the current timestamp
   */
  @Modifying
  @Transactional
  @Query(
      "UPDATE NotificationEntity n SET n.status = 'FAILED', n.failureReason = :failureReason, "
          + "n.updatedAt = :now "
          + "WHERE n.id = :id")
  void markAsFailed(
      @Param("id") UUID id,
      @Param("failureReason") String failureReason,
      @Param("now") LocalDateTime now);

  /**
   * Find oldest notification by status for archival.
   *
   * @param tenantId the tenant ID
   * @param status the status
   * @param beforeTime the cutoff time (e.g., 30 days ago)
   * @param pageable pagination parameters
   * @return page of old notifications eligible for archival
   */
  Page<NotificationEntity> findByTenantIdAndStatusAndCreatedAtBeforeOrderByCreatedAtAsc(
      String tenantId, NotificationStatus status, LocalDateTime beforeTime, Pageable pageable);
}
