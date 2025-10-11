package com.paymentengine.paymentprocessing.repository;

import com.paymentengine.paymentprocessing.entity.QueuedMessage;
// Removed incorrect import of Status; using nested enum below
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for QueuedMessage entity
 */
@Repository
public interface QueuedMessageRepository extends JpaRepository<QueuedMessage, String> {

    /**
     * Find queued message by message ID
     */
    Optional<QueuedMessage> findByMessageId(String messageId);

    /**
     * Find queued messages by tenant ID
     */
    Page<QueuedMessage> findByTenantId(String tenantId, Pageable pageable);

    /**
     * Find queued messages by service name
     */
    Page<QueuedMessage> findByServiceName(String serviceName, Pageable pageable);

    /**
     * Find queued messages by status
     */
    Page<QueuedMessage> findByStatus(QueuedMessage.MessageStatus status, Pageable pageable);

    /**
     * Find queued messages by tenant ID and service name
     */
    Page<QueuedMessage> findByTenantIdAndServiceName(String tenantId, String serviceName, Pageable pageable);

    /**
     * Find queued messages by tenant ID and status
     */
    Page<QueuedMessage> findByTenantIdAndStatus(String tenantId, QueuedMessage.MessageStatus status, Pageable pageable);

    /**
     * Find queued messages by service name and status
     */
    Page<QueuedMessage> findByServiceNameAndStatus(String serviceName, QueuedMessage.MessageStatus status, Pageable pageable);

    /**
     * Find queued messages by tenant ID, service name and status
     */
    Page<QueuedMessage> findByTenantIdAndServiceNameAndStatus(String tenantId, String serviceName, 
                                                              QueuedMessage.MessageStatus status, Pageable pageable);

    /**
     * Find messages ready for retry
     */
    List<QueuedMessage> findByStatusAndNextRetryAtBeforeAndRetryCountLessThan(QueuedMessage.MessageStatus status, 
                                                                             LocalDateTime nextRetryAt, 
                                                                             int maxRetries);

    /**
     * Find expired messages
     */
    List<QueuedMessage> findByExpiresAtBefore(LocalDateTime expiresAt);

    /**
     * Find messages by correlation ID
     */
    List<QueuedMessage> findByCorrelationId(String correlationId);

    /**
     * Find messages by parent message ID
     */
    List<QueuedMessage> findByParentMessageId(String parentMessageId);

    /**
     * Count messages by tenant ID
     */
    long countByTenantId(String tenantId);

    /**
     * Count messages by service name
     */
    long countByServiceName(String serviceName);

    /**
     * Count messages by status
     */
    long countByStatus(QueuedMessage.MessageStatus status);

    /**
     * Count messages by tenant ID and status
     */
    long countByTenantIdAndStatus(String tenantId, QueuedMessage.MessageStatus status);

    /**
     * Count messages by service name and status
     */
    long countByServiceNameAndStatus(String serviceName, QueuedMessage.MessageStatus status);

    /**
     * Count messages by tenant ID and service name
     */
    long countByTenantIdAndServiceName(String tenantId, String serviceName);

    /**
     * Count messages by tenant ID, service name and status
     */
    long countByTenantIdAndServiceNameAndStatus(String tenantId, String serviceName, QueuedMessage.MessageStatus status);

    /**
     * Find messages created within a time range
     */
    @Query("SELECT qm FROM QueuedMessage qm WHERE qm.createdAt BETWEEN :startTime AND :endTime")
    List<QueuedMessage> findByCreatedAtBetween(@Param("startTime") LocalDateTime startTime, 
                                               @Param("endTime") LocalDateTime endTime);

    /**
     * Find messages by message type
     */
    Page<QueuedMessage> findByMessageType(String messageType, Pageable pageable);

    /**
     * Find messages by tenant ID and message type
     */
    Page<QueuedMessage> findByTenantIdAndMessageType(String tenantId, String messageType, Pageable pageable);

    /**
     * Find messages by service name and message type
     */
    Page<QueuedMessage> findByServiceNameAndMessageType(String serviceName, String messageType, Pageable pageable);

    /**
     * Find messages by tenant ID, service name and message type
     */
    Page<QueuedMessage> findByTenantIdAndServiceNameAndMessageType(String tenantId, String serviceName, 
                                                                  String messageType, Pageable pageable);

    /**
     * Count messages by message type
     */
    long countByMessageType(String messageType);

    /**
     * Count messages by tenant ID and message type
     */
    long countByTenantIdAndMessageType(String tenantId, String messageType);

    /**
     * Count messages by service name and message type
     */
    long countByServiceNameAndMessageType(String serviceName, String messageType);

    /**
     * Count messages by tenant ID, service name and message type
     */
    long countByTenantIdAndServiceNameAndMessageType(String tenantId, String serviceName, String messageType);

    /**
     * Find messages with high priority
     */
    @Query("SELECT qm FROM QueuedMessage qm WHERE qm.priority >= :minPriority ORDER BY qm.priority DESC, qm.createdAt ASC")
    List<QueuedMessage> findHighPriorityMessages(@Param("minPriority") int minPriority);

    /**
     * Find messages by processing time range
     */
    @Query("SELECT qm FROM QueuedMessage qm WHERE qm.processingTimeMs BETWEEN :minTime AND :maxTime")
    List<QueuedMessage> findByProcessingTimeBetween(@Param("minTime") Long minTime, @Param("maxTime") Long maxTime);

    /**
     * Find messages that have been processing for too long
     */
    @Query("SELECT qm FROM QueuedMessage qm WHERE qm.status = 'PROCESSING' AND qm.processingStartedAt < :cutoffTime")
    List<QueuedMessage> findStuckProcessingMessages(@Param("cutoffTime") LocalDateTime cutoffTime);

    /**
     * Get queue statistics by tenant
     */
    @Query("SELECT qm.status, COUNT(qm) FROM QueuedMessage qm WHERE qm.tenantId = :tenantId GROUP BY qm.status")
    List<Object[]> getQueueStatisticsByTenant(@Param("tenantId") String tenantId);

    /**
     * Get queue statistics by service
     */
    @Query("SELECT qm.status, COUNT(qm) FROM QueuedMessage qm WHERE qm.serviceName = :serviceName GROUP BY qm.status")
    List<Object[]> getQueueStatisticsByService(@Param("serviceName") String serviceName);

    /**
     * Get queue statistics by message type
     */
    @Query("SELECT qm.status, COUNT(qm) FROM QueuedMessage qm WHERE qm.messageType = :messageType GROUP BY qm.status")
    List<Object[]> getQueueStatisticsByMessageType(@Param("messageType") String messageType);

    /**
     * Delete old processed messages
     */
    @Query("DELETE FROM QueuedMessage qm WHERE qm.status = 'PROCESSED' AND qm.processingCompletedAt < :cutoffTime")
    int deleteOldProcessedMessages(@Param("cutoffTime") LocalDateTime cutoffTime);

    /**
     * Delete old expired messages
     */
    @Query("DELETE FROM QueuedMessage qm WHERE qm.status = 'EXPIRED' AND qm.updatedAt < :cutoffTime")
    int deleteOldExpiredMessages(@Param("cutoffTime") LocalDateTime cutoffTime);

    /**
     * Delete old cancelled messages
     */
    @Query("DELETE FROM QueuedMessage qm WHERE qm.status = 'CANCELLED' AND qm.updatedAt < :cutoffTime")
    int deleteOldCancelledMessages(@Param("cutoffTime") LocalDateTime cutoffTime);
}