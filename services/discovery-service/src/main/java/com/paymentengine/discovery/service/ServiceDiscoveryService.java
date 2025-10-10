package com.paymentengine.discovery.service;

import com.paymentengine.discovery.entity.ServiceInstance;
import com.paymentengine.discovery.entity.ServiceRegistry;

import java.util.List;
import java.util.Optional;

public interface ServiceDiscoveryService {
    
    // Service Registration
    ServiceInstance registerService(ServiceInstance serviceInstance);
    
    ServiceInstance updateServiceInstance(ServiceInstance serviceInstance);
    
    void deregisterService(String serviceName, String instanceId);
    
    void deregisterAllInstances(String serviceName);
    
    // Service Discovery
    List<ServiceInstance> discoverServices(String serviceName);
    
    List<ServiceInstance> discoverHealthyServices(String serviceName);
    
    Optional<ServiceInstance> discoverService(String serviceName, String instanceId);
    
    List<ServiceInstance> discoverServicesByZone(String serviceName, String zone);
    
    List<ServiceInstance> discoverServicesByRegion(String serviceName, String region);
    
    // Load Balancing
    Optional<ServiceInstance> getNextInstance(String serviceName, String loadBalancingStrategy);
    
    Optional<ServiceInstance> getNextInstanceByZone(String serviceName, String zone, String loadBalancingStrategy);
    
    Optional<ServiceInstance> getNextInstanceByRegion(String serviceName, String region, String loadBalancingStrategy);
    
    // Health Management
    void updateHeartbeat(String serviceName, String instanceId);
    
    void markInstanceDown(String serviceName, String instanceId);
    
    void markInstanceUp(String serviceName, String instanceId);
    
    void cleanupStaleInstances();
    
    // Service Registry Management
    ServiceRegistry registerServiceType(ServiceRegistry serviceRegistry);
    
    ServiceRegistry updateServiceRegistry(ServiceRegistry serviceRegistry);
    
    void deregisterServiceType(String serviceName);
    
    List<ServiceRegistry> getAllServiceTypes();
    
    List<ServiceRegistry> getServiceTypesByType(String serviceType);
    
    Optional<ServiceRegistry> getServiceRegistry(String serviceName);
    
    // Monitoring and Statistics
    List<String> getAllActiveServiceNames();
    
    long getHealthyInstanceCount(String serviceName);
    
    long getTotalInstanceCount(String serviceName);
    
    List<ServiceInstance> getStaleInstances();
    
    // Service Mesh Integration
    List<ServiceInstance> getInstancesForServiceMesh(String serviceName);
    
    void updateServiceMeshMetadata(String serviceName, String instanceId, String metadata);
}