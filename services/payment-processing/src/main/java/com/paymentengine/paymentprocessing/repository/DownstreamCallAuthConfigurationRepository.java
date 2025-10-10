package com.paymentengine.paymentprocessing.repository;

import com.paymentengine.paymentprocessing.entity.DownstreamCallAuthConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DownstreamCallAuthConfigurationRepository extends JpaRepository<DownstreamCallAuthConfiguration, UUID> {
    
    List<DownstreamCallAuthConfiguration> findByTenantId(String tenantId);
    
    List<DownstreamCallAuthConfiguration> findByServiceType(String serviceType);
    
    List<DownstreamCallAuthConfiguration> findByEndpoint(String endpoint);
    
    List<DownstreamCallAuthConfiguration> findByTenantIdAndServiceType(String tenantId, String serviceType);
    
    List<DownstreamCallAuthConfiguration> findByTenantIdAndEndpoint(String tenantId, String endpoint);
    
    List<DownstreamCallAuthConfiguration> findByServiceTypeAndEndpoint(String serviceType, String endpoint);
    
    Optional<DownstreamCallAuthConfiguration> findByTenantIdAndServiceTypeAndEndpointAndIsActive(
        String tenantId, String serviceType, String endpoint, Boolean isActive);
    
    List<DownstreamCallAuthConfiguration> findByTenantIdAndIsActive(String tenantId, Boolean isActive);
    
    List<DownstreamCallAuthConfiguration> findByServiceTypeAndIsActive(String serviceType, Boolean isActive);
    
    List<DownstreamCallAuthConfiguration> findByIsActive(Boolean isActive);
    
    Optional<DownstreamCallAuthConfiguration> findByTenantIdAndServiceTypeAndEndpointAndAuthMethodAndIsActive(
        String tenantId, String serviceType, String endpoint, DownstreamCallAuthConfiguration.AuthMethod authMethod, Boolean isActive);
    
    List<DownstreamCallAuthConfiguration> findByPaymentType(String paymentType);
    
    List<DownstreamCallAuthConfiguration> findByTargetHost(String targetHost);
    
    List<DownstreamCallAuthConfiguration> findByTargetHostAndTargetPort(String targetHost, Integer targetPort);
}