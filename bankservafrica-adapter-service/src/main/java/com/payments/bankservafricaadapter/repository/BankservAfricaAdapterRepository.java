package com.payments.bankservafricaadapter.repository;

import com.payments.bankservafricaadapter.domain.BankservAfricaAdapter;
import com.payments.domain.shared.ClearingAdapterId;
import com.payments.domain.shared.TenantContext;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for BankservAfrica Adapter entities
 */
@Repository
public interface BankservAfricaAdapterRepository extends JpaRepository<BankservAfricaAdapter, ClearingAdapterId> {
    
    /**
     * Find adapter by tenant context and adapter name
     */
    @Query("SELECT a FROM BankservAfricaAdapter a WHERE a.tenantContext.tenantId = :tenantId AND a.adapterName = :adapterName")
    Optional<BankservAfricaAdapter> findByTenantIdAndAdapterName(
            @Param("tenantId") String tenantId, 
            @Param("adapterName") String adapterName);
    
    /**
     * Find all adapters by tenant context
     */
    @Query("SELECT a FROM BankservAfricaAdapter a WHERE a.tenantContext.tenantId = :tenantId")
    List<BankservAfricaAdapter> findByTenantId(@Param("tenantId") String tenantId);
    
    /**
     * Find all adapters by tenant context and business unit
     */
    @Query("SELECT a FROM BankservAfricaAdapter a WHERE a.tenantContext.tenantId = :tenantId AND a.tenantContext.businessUnitId = :businessUnitId")
    List<BankservAfricaAdapter> findByTenantIdAndBusinessUnitId(
            @Param("tenantId") String tenantId, 
            @Param("businessUnitId") String businessUnitId);
    
    /**
     * Find active adapters by tenant context
     */
    @Query("SELECT a FROM BankservAfricaAdapter a WHERE a.tenantContext.tenantId = :tenantId AND a.status = 'ACTIVE'")
    List<BankservAfricaAdapter> findActiveByTenantId(@Param("tenantId") String tenantId);
    
    /**
     * Find adapters by network
     */
    @Query("SELECT a FROM BankservAfricaAdapter a WHERE a.network = :network")
    List<BankservAfricaAdapter> findByNetwork(@Param("network") String network);
    
    /**
     * Find adapters by status
     */
    @Query("SELECT a FROM BankservAfricaAdapter a WHERE a.status = :status")
    List<BankservAfricaAdapter> findByStatus(@Param("status") String status);
}
