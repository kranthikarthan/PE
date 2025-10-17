package com.payments.payshapadapter.repository;

import com.payments.domain.clearing.AdapterOperationalStatus;
import com.payments.domain.shared.ClearingAdapterId;
import com.payments.payshapadapter.domain.PayShapAdapter;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/** Repository for PayShap Adapter entities */
@Repository
public interface PayShapAdapterRepository extends JpaRepository<PayShapAdapter, String> {

  /** Find adapter by ClearingAdapterId */
  Optional<PayShapAdapter> findById(ClearingAdapterId id);

  /** Find adapters by tenant and business unit */
  @Query(
      "SELECT a FROM PayShapAdapter a WHERE a.tenantContext.tenantId = :tenantId AND a.tenantContext.businessUnitId = :businessUnitId")
  List<PayShapAdapter> findByTenantIdAndBusinessUnitId(
      @Param("tenantId") String tenantId, @Param("businessUnitId") String businessUnitId);

  /** Find active adapters by tenant */
  @Query(
      "SELECT a FROM PayShapAdapter a WHERE a.tenantContext.tenantId = :tenantId AND a.status = :status")
  List<PayShapAdapter> findByTenantIdAndStatus(
      @Param("tenantId") String tenantId, @Param("status") AdapterOperationalStatus status);

  /** Find adapters by status */
  List<PayShapAdapter> findByStatus(AdapterOperationalStatus status);

  /** Find adapters by network */
  List<PayShapAdapter> findByNetwork(String network);

  /** Check if adapter exists by tenant and name */
  @Query(
      "SELECT COUNT(a) > 0 FROM PayShapAdapter a WHERE a.tenantContext.tenantId = :tenantId AND a.adapterName = :adapterName")
  boolean existsByTenantIdAndAdapterName(
      @Param("tenantId") String tenantId, @Param("adapterName") String adapterName);
}
