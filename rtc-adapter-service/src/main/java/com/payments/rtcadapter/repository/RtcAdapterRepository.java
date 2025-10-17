package com.payments.rtcadapter.repository;

import com.payments.domain.clearing.AdapterOperationalStatus;
import com.payments.domain.shared.ClearingAdapterId;
import com.payments.rtcadapter.domain.RtcAdapter;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/** Repository for RTC Adapter entities */
@Repository
public interface RtcAdapterRepository extends JpaRepository<RtcAdapter, ClearingAdapterId> {

  /** Find adapters by tenant and business unit */
  @Query(
      "SELECT ra FROM RtcAdapter ra WHERE ra.tenantContext.tenantId = :tenantId AND ra.tenantContext.businessUnitId = :businessUnitId")
  List<RtcAdapter> findByTenantAndBusinessUnit(
      @Param("tenantId") String tenantId, @Param("businessUnitId") String businessUnitId);

  /** Find adapters by status */
  @Query("SELECT ra FROM RtcAdapter ra WHERE ra.status = :status")
  List<RtcAdapter> findByStatus(@Param("status") AdapterOperationalStatus status);

  /** Find active adapters by tenant */
  @Query(
      "SELECT ra FROM RtcAdapter ra WHERE ra.tenantContext.tenantId = :tenantId AND ra.status = 'ACTIVE'")
  List<RtcAdapter> findActiveByTenant(@Param("tenantId") String tenantId);

  /** Find adapter by name and tenant */
  @Query(
      "SELECT ra FROM RtcAdapter ra WHERE ra.adapterName = :adapterName AND ra.tenantContext.tenantId = :tenantId")
  Optional<RtcAdapter> findByAdapterNameAndTenant(
      @Param("adapterName") String adapterName, @Param("tenantId") String tenantId);

  /** Count adapters by status */
  long countByStatus(AdapterOperationalStatus status);

  /** Count adapters by tenant */
  @Query("SELECT COUNT(ra) FROM RtcAdapter ra WHERE ra.tenantContext.tenantId = :tenantId")
  long countByTenant(@Param("tenantId") String tenantId);
}
