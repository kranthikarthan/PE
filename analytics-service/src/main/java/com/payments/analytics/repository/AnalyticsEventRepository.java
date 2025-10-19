package com.payments.analytics.repository;

import com.payments.analytics.domain.AnalyticsEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Repository for Analytics Event entities.
 *
 * <p>This repository provides data access methods for analytics events
 * with support for filtering, pagination, and aggregation.
 *
 * @since PE-415
 */
@Repository
public interface AnalyticsEventRepository extends JpaRepository<AnalyticsEvent, UUID> {

    /**
     * Finds analytics events by filters.
     *
     * @param tenantId the tenant ID
     * @param businessUnitId optional business unit ID
     * @param eventType optional event type filter
     * @param entityType optional entity type filter
     * @param startDate optional start date filter
     * @param endDate optional end date filter
     * @param limit maximum number of results
     * @param offset offset for pagination
     * @return list of analytics events
     */
    @Query("SELECT e FROM AnalyticsEvent e WHERE " +
           "e.tenantId = :tenantId AND " +
           "(:businessUnitId IS NULL OR e.businessUnitId = :businessUnitId) AND " +
           "(:eventType IS NULL OR e.eventType = :eventType) AND " +
           "(:entityType IS NULL OR e.entityType = :entityType) AND " +
           "(:startDate IS NULL OR e.timestamp >= :startDate) AND " +
           "(:endDate IS NULL OR e.timestamp <= :endDate) " +
           "ORDER BY e.timestamp DESC")
    List<AnalyticsEvent> findByFilters(
            @Param("tenantId") UUID tenantId,
            @Param("businessUnitId") UUID businessUnitId,
            @Param("eventType") AnalyticsEvent.EventType eventType,
            @Param("entityType") AnalyticsEvent.EntityType entityType,
            @Param("startDate") OffsetDateTime startDate,
            @Param("endDate") OffsetDateTime endDate,
            @Param("limit") Integer limit,
            @Param("offset") Integer offset
    );

    /**
     * Finds analytics events by entity ID and type.
     *
     * @param entityId the entity ID
     * @param entityType the entity type
     * @param tenantId the tenant ID
     * @return list of analytics events
     */
    List<AnalyticsEvent> findByEntityIdAndEntityTypeAndTenantIdOrderByTimestampDesc(
            String entityId, 
            AnalyticsEvent.EntityType entityType, 
            UUID tenantId
    );

    /**
     * Finds analytics events by event type and time range.
     *
     * @param eventType the event type
     * @param startDate the start date
     * @param endDate the end date
     * @param tenantId the tenant ID
     * @return list of analytics events
     */
    List<AnalyticsEvent> findByEventTypeAndTimestampBetweenAndTenantIdOrderByTimestampDesc(
            AnalyticsEvent.EventType eventType,
            OffsetDateTime startDate,
            OffsetDateTime endDate,
            UUID tenantId
    );

    /**
     * Counts analytics events by filters.
     *
     * @param tenantId the tenant ID
     * @param businessUnitId optional business unit ID
     * @param eventType optional event type filter
     * @param entityType optional entity type filter
     * @param startDate optional start date filter
     * @param endDate optional end date filter
     * @return count of analytics events
     */
    @Query("SELECT COUNT(e) FROM AnalyticsEvent e WHERE " +
           "e.tenantId = :tenantId AND " +
           "(:businessUnitId IS NULL OR e.businessUnitId = :businessUnitId) AND " +
           "(:eventType IS NULL OR e.eventType = :eventType) AND " +
           "(:entityType IS NULL OR e.entityType = :entityType) AND " +
           "(:startDate IS NULL OR e.timestamp >= :startDate) AND " +
           "(:endDate IS NULL OR e.timestamp <= :endDate)")
    long countByFilters(
            @Param("tenantId") UUID tenantId,
            @Param("businessUnitId") UUID businessUnitId,
            @Param("eventType") AnalyticsEvent.EventType eventType,
            @Param("entityType") AnalyticsEvent.EntityType entityType,
            @Param("startDate") OffsetDateTime startDate,
            @Param("endDate") OffsetDateTime endDate
    );

    /**
     * Finds analytics events by correlation ID.
     *
     * @param correlationId the correlation ID
     * @param tenantId the tenant ID
     * @return list of analytics events
     */
    List<AnalyticsEvent> findByCorrelationIdAndTenantIdOrderByTimestampDesc(
            String correlationId, 
            UUID tenantId
    );

    /**
     * Finds analytics events by user ID and time range.
     *
     * @param userId the user ID
     * @param startDate the start date
     * @param endDate the end date
     * @param tenantId the tenant ID
     * @return list of analytics events
     */
    List<AnalyticsEvent> findByUserIdAndTimestampBetweenAndTenantIdOrderByTimestampDesc(
            String userId,
            OffsetDateTime startDate,
            OffsetDateTime endDate,
            UUID tenantId
    );

    /**
     * Finds analytics events by session ID.
     *
     * @param sessionId the session ID
     * @param tenantId the tenant ID
     * @return list of analytics events
     */
    List<AnalyticsEvent> findBySessionIdAndTenantIdOrderByTimestampDesc(
            String sessionId, 
            UUID tenantId
    );

    /**
     * Finds analytics events by status and time range.
     *
     * @param status the event status
     * @param startDate the start date
     * @param endDate the end date
     * @param tenantId the tenant ID
     * @return list of analytics events
     */
    List<AnalyticsEvent> findByStatusAndTimestampBetweenAndTenantIdOrderByTimestampDesc(
            AnalyticsEvent.EventStatus status,
            OffsetDateTime startDate,
            OffsetDateTime endDate,
            UUID tenantId
    );

    /**
     * Finds analytics events by amount range.
     *
     * @param minAmount the minimum amount
     * @param maxAmount the maximum amount
     * @param tenantId the tenant ID
     * @return list of analytics events
     */
    @Query("SELECT e FROM AnalyticsEvent e WHERE " +
           "e.amount >= :minAmount AND " +
           "e.amount <= :maxAmount AND " +
           "e.tenantId = :tenantId " +
           "ORDER BY e.timestamp DESC")
    List<AnalyticsEvent> findByAmountRangeAndTenantId(
            @Param("minAmount") java.math.BigDecimal minAmount,
            @Param("maxAmount") java.math.BigDecimal maxAmount,
            @Param("tenantId") UUID tenantId
    );

    /**
     * Finds analytics events by currency.
     *
     * @param currency the currency
     * @param tenantId the tenant ID
     * @return list of analytics events
     */
    List<AnalyticsEvent> findByCurrencyAndTenantIdOrderByTimestampDesc(
            String currency, 
            UUID tenantId
    );

    /**
     * Deletes analytics events older than specified date.
     *
     * @param cutoffDate the cutoff date
     * @param tenantId the tenant ID
     * @return number of deleted events
     */
    @Query("DELETE FROM AnalyticsEvent e WHERE e.timestamp < :cutoffDate AND e.tenantId = :tenantId")
    int deleteByTimestampBeforeAndTenantId(
            @Param("cutoffDate") OffsetDateTime cutoffDate,
            @Param("tenantId") UUID tenantId
    );
}
