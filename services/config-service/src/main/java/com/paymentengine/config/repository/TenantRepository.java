package com.paymentengine.config.repository;

import com.paymentengine.config.entity.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TenantRepository extends JpaRepository<Tenant, UUID> {
    
    Optional<Tenant> findByCode(String code);
    
    List<Tenant> findByStatus(Tenant.TenantStatus status);
    
    List<Tenant> findByCreatedAtAfter(LocalDateTime date);
    
    @Query("SELECT t FROM Tenant t WHERE t.contactEmail = :email")
    Optional<Tenant> findByContactEmail(@Param("email") String email);
    
    @Query("SELECT COUNT(t) FROM Tenant t WHERE t.status = :status")
    Long countByStatus(@Param("status") Tenant.TenantStatus status);
    
    @Query("SELECT t FROM Tenant t WHERE t.name LIKE %:name%")
    List<Tenant> findByNameContaining(@Param("name") String name);
    
    @Query("SELECT t FROM Tenant t WHERE t.code LIKE %:code%")
    List<Tenant> findByCodeContaining(@Param("code") String code);
    
    boolean existsByCode(String code);
    
    boolean existsByContactEmail(String email);
}