package com.payments.swiftadapter.repository;

import com.payments.domain.clearing.AdapterOperationalStatus;
import com.payments.domain.shared.ClearingAdapterId;
import com.payments.swiftadapter.domain.SwiftAdapter;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/** Repository for SWIFT Adapter entities */
@Repository
public interface SwiftAdapterRepository extends JpaRepository<SwiftAdapter, ClearingAdapterId> {

  /** Find adapter by ID */
  Optional<SwiftAdapter> findById(ClearingAdapterId id);

  /** Find adapter by tenant context */
  @Query(
      "SELECT sa FROM SwiftAdapter sa WHERE sa.tenantContext.tenantId = :tenantId AND sa.tenantContext.businessUnitId = :businessUnitId")
  List<SwiftAdapter> findByTenantContext(
      @Param("tenantId") String tenantId, @Param("businessUnitId") String businessUnitId);

  /** Find active adapters by tenant context */
  @Query(
      "SELECT sa FROM SwiftAdapter sa WHERE sa.tenantContext.tenantId = :tenantId AND sa.tenantContext.businessUnitId = :businessUnitId AND sa.status = :status")
  List<SwiftAdapter> findActiveByTenantContext(
      @Param("tenantId") String tenantId,
      @Param("businessUnitId") String businessUnitId,
      @Param("status") AdapterOperationalStatus status);

  /** Find adapter by tenant context and name */
  @Query(
      "SELECT sa FROM SwiftAdapter sa WHERE sa.tenantContext.tenantId = :tenantId AND sa.tenantContext.businessUnitId = :businessUnitId AND sa.name = :name")
  Optional<SwiftAdapter> findByTenantContextAndName(
      @Param("tenantId") String tenantId,
      @Param("businessUnitId") String businessUnitId,
      @Param("name") String name);

  /** Find adapters by status */
  List<SwiftAdapter> findByStatus(AdapterOperationalStatus status);

  /** Find adapters by network */
  @Query("SELECT sa FROM SwiftAdapter sa WHERE sa.network = :network")
  List<SwiftAdapter> findByNetwork(@Param("network") String network);

  /** Find adapters by tenant context and status */
  @Query(
      "SELECT sa FROM SwiftAdapter sa WHERE sa.tenantContext.tenantId = :tenantId AND sa.tenantContext.businessUnitId = :businessUnitId AND sa.status = :status")
  List<SwiftAdapter> findByTenantContextAndStatus(
      @Param("tenantId") String tenantId,
      @Param("businessUnitId") String businessUnitId,
      @Param("status") AdapterOperationalStatus status);

  /** Check if adapter exists by tenant context and name */
  @Query(
      "SELECT COUNT(sa) > 0 FROM SwiftAdapter sa WHERE sa.tenantContext.tenantId = :tenantId AND sa.tenantContext.businessUnitId = :businessUnitId AND sa.name = :name")
  boolean existsByTenantContextAndName(
      @Param("tenantId") String tenantId,
      @Param("businessUnitId") String businessUnitId,
      @Param("name") String name);

  /** Find adapters by tenant ID and business unit ID */
  @Query(
      "SELECT sa FROM SwiftAdapter sa WHERE sa.tenantContext.tenantId = :tenantId AND sa.tenantContext.businessUnitId = :businessUnitId")
  List<SwiftAdapter> findByTenantIdAndBusinessUnitId(
      @Param("tenantId") String tenantId, @Param("businessUnitId") String businessUnitId);

  /** Find adapters by tenant ID and status */
  @Query(
      "SELECT sa FROM SwiftAdapter sa WHERE sa.tenantContext.tenantId = :tenantId AND sa.status = :status")
  List<SwiftAdapter> findByTenantIdAndStatus(
      @Param("tenantId") String tenantId, @Param("status") AdapterOperationalStatus status);

  /** Count adapters by status */
  @Query("SELECT COUNT(sa) FROM SwiftAdapter sa WHERE sa.status = :status")
  long countByStatus(@Param("status") AdapterOperationalStatus status);
}
