package com.paymentengine.discovery.repository;

import com.paymentengine.discovery.entity.ServiceInstance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ServiceInstanceRepository extends JpaRepository<ServiceInstance, UUID> {
    
    List<ServiceInstance> findByServiceName(String serviceName);
    
    List<ServiceInstance> findByServiceNameAndStatus(String serviceName, String status);
    
    Optional<ServiceInstance> findByServiceNameAndInstanceId(String serviceName, String instanceId);
    
    List<ServiceInstance> findByStatus(String status);
    
    List<ServiceInstance> findByZone(String zone);
    
    List<ServiceInstance> findByRegion(String region);
    
    @Query("SELECT si FROM ServiceInstance si WHERE si.lastHeartbeat < :cutoffTime")
    List<ServiceInstance> findStaleInstances(@Param("cutoffTime") LocalDateTime cutoffTime);
    
    @Query("SELECT si FROM ServiceInstance si WHERE si.serviceName = :serviceName AND si.status = 'UP' ORDER BY si.lastHeartbeat DESC")
    List<ServiceInstance> findHealthyInstancesByService(@Param("serviceName") String serviceName);
    
    @Query("SELECT COUNT(si) FROM ServiceInstance si WHERE si.serviceName = :serviceName AND si.status = 'UP'")
    long countHealthyInstancesByService(@Param("serviceName") String serviceName);
    
    @Query("SELECT DISTINCT si.serviceName FROM ServiceInstance si WHERE si.status = 'UP'")
    List<String> findActiveServiceNames();
    
    @Query("SELECT si FROM ServiceInstance si WHERE si.serviceName = :serviceName AND si.zone = :zone AND si.status = 'UP'")
    List<ServiceInstance> findHealthyInstancesByServiceAndZone(@Param("serviceName") String serviceName, @Param("zone") String zone);
    
    @Query("SELECT si FROM ServiceInstance si WHERE si.serviceName = :serviceName AND si.region = :region AND si.status = 'UP'")
    List<ServiceInstance> findHealthyInstancesByServiceAndRegion(@Param("serviceName") String serviceName, @Param("region") String region);
    
    void deleteByServiceNameAndInstanceId(String serviceName, String instanceId);
    
    void deleteByLastHeartbeatBefore(LocalDateTime cutoffTime);
}