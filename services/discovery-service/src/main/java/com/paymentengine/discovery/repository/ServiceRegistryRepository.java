package com.paymentengine.discovery.repository;

import com.paymentengine.discovery.entity.ServiceRegistry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ServiceRegistryRepository extends JpaRepository<ServiceRegistry, UUID> {
    
    Optional<ServiceRegistry> findByServiceName(String serviceName);
    
    List<ServiceRegistry> findByServiceType(String serviceType);
    
    List<ServiceRegistry> findByIsActiveTrue();
    
    List<ServiceRegistry> findByIsActiveFalse();
    
    @Query("SELECT sr FROM ServiceRegistry sr WHERE sr.serviceName LIKE %:pattern%")
    List<ServiceRegistry> findByServiceNameContaining(@Param("pattern") String pattern);
    
    @Query("SELECT sr FROM ServiceRegistry sr WHERE sr.serviceType = :serviceType AND sr.isActive = true")
    List<ServiceRegistry> findActiveByServiceType(@Param("serviceType") String serviceType);
    
    @Query("SELECT DISTINCT sr.serviceType FROM ServiceRegistry sr WHERE sr.isActive = true")
    List<String> findDistinctActiveServiceTypes();
    
    @Query("SELECT sr FROM ServiceRegistry sr WHERE sr.version = :version AND sr.isActive = true")
    List<ServiceRegistry> findActiveByVersion(@Param("version") String version);
    
    @Query("SELECT sr FROM ServiceRegistry sr WHERE sr.loadBalancingStrategy = :strategy AND sr.isActive = true")
    List<ServiceRegistry> findActiveByLoadBalancingStrategy(@Param("strategy") String strategy);
    
    boolean existsByServiceName(String serviceName);
    
    void deleteByServiceName(String serviceName);
}