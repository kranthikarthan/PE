package com.payments.paymentinitiation.repository;

import com.payments.paymentinitiation.entity.Pain001MessageEntity;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/** Repository for pain.001 message entities */
@Repository
public interface Pain001MessageRepository extends JpaRepository<Pain001MessageEntity, UUID> {

  /** Find pain.001 message by message ID */
  Optional<Pain001MessageEntity> findByMessageId(String messageId);

  /** Find pain.001 messages by tenant and business unit */
  List<Pain001MessageEntity> findByTenantIdAndBusinessUnitId(
      String tenantId, String businessUnitId);

  /** Find pain.001 messages by validation status */
  List<Pain001MessageEntity> findByValidationStatus(
      Pain001MessageEntity.ValidationStatus validationStatus);

  /** Find pain.001 messages by tenant, business unit, and validation status */
  List<Pain001MessageEntity> findByTenantIdAndBusinessUnitIdAndValidationStatus(
      String tenantId,
      String businessUnitId,
      Pain001MessageEntity.ValidationStatus validationStatus);

  /** Find pain.001 messages created within date range */
  @Query(
      "SELECT p FROM Pain001MessageEntity p WHERE p.tenantId = :tenantId AND p.businessUnitId = :businessUnitId "
          + "AND p.createdAt BETWEEN :startDate AND :endDate ORDER BY p.createdAt DESC")
  List<Pain001MessageEntity> findByTenantAndBusinessUnitAndDateRange(
      @Param("tenantId") String tenantId,
      @Param("businessUnitId") String businessUnitId,
      @Param("startDate") LocalDateTime startDate,
      @Param("endDate") LocalDateTime endDate);

  /** Count pain.001 messages by tenant and business unit */
  long countByTenantIdAndBusinessUnitId(String tenantId, String businessUnitId);

  /** Count pain.001 messages by validation status */
  long countByValidationStatus(Pain001MessageEntity.ValidationStatus validationStatus);

  /** Find pain.001 messages with pending validation */
  @Query(
      "SELECT p FROM Pain001MessageEntity p WHERE p.validationStatus = 'PENDING' "
          + "AND p.createdAt < :cutoffTime ORDER BY p.createdAt ASC")
  List<Pain001MessageEntity> findPendingValidationMessages(
      @Param("cutoffTime") LocalDateTime cutoffTime);

  /** Find pain.001 messages by correlation ID */
  @Query(
      "SELECT p FROM Pain001MessageEntity p WHERE p.id IN "
          + "(SELECT c.pain001Message.id FROM Pain001Pain002CorrelationEntity c WHERE c.correlationId = :correlationId)")
  List<Pain001MessageEntity> findByCorrelationId(@Param("correlationId") String correlationId);
}
