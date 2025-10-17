package com.payments.samosadapter.repository;

import com.payments.domain.shared.ClearingAdapterId;
import com.payments.samosadapter.domain.SamosAdapter;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * SAMOS Adapter Repository
 *
 * <p>Data access layer for SAMOS adapter configurations with tenant isolation.
 */
@Repository
public interface SamosAdapterRepository extends JpaRepository<SamosAdapter, ClearingAdapterId> {

  /** Find SAMOS adapter by tenant ID and adapter name */
  @Query(
      "SELECT sa FROM SamosAdapter sa WHERE sa.tenantContext.tenantId = :tenantId AND sa.adapterName = :adapterName")
  Optional<SamosAdapter> findByTenantIdAndAdapterName(
      @Param("tenantId") String tenantId, @Param("adapterName") String adapterName);

  /** Find all active SAMOS adapters for a tenant */
  @Query(
      "SELECT sa FROM SamosAdapter sa WHERE sa.tenantContext.tenantId = :tenantId AND sa.status = 'ACTIVE'")
  List<SamosAdapter> findActiveByTenantId(@Param("tenantId") String tenantId);

  /** Find SAMOS adapter by tenant ID and business unit ID */
  @Query(
      "SELECT sa FROM SamosAdapter sa WHERE sa.tenantContext.tenantId = :tenantId AND sa.tenantContext.businessUnitId = :businessUnitId")
  List<SamosAdapter> findByTenantIdAndBusinessUnitId(
      @Param("tenantId") String tenantId, @Param("businessUnitId") String businessUnitId);

  /** Check if adapter name exists for tenant */
  @Query(
      "SELECT COUNT(sa) > 0 FROM SamosAdapter sa WHERE sa.tenantContext.tenantId = :tenantId AND sa.adapterName = :adapterName")
  boolean existsByTenantIdAndAdapterName(
      @Param("tenantId") String tenantId, @Param("adapterName") String adapterName);

  /** Find SAMOS adapters by status */
  @Query("SELECT sa FROM SamosAdapter sa WHERE sa.status = :status")
  List<SamosAdapter> findByStatus(
      @Param("status") com.payments.domain.clearing.AdapterOperationalStatus status);

  /** Count active adapters for tenant */
  @Query(
      "SELECT COUNT(sa) FROM SamosAdapter sa WHERE sa.tenantContext.tenantId = :tenantId AND sa.status = 'ACTIVE'")
  long countActiveByTenantId(@Param("tenantId") String tenantId);

  /** Count adapters by status */
  @Query("SELECT COUNT(sa) FROM SamosAdapter sa WHERE sa.status = :status")
  long countByStatus(@Param("status") com.payments.domain.clearing.AdapterOperationalStatus status);
}
