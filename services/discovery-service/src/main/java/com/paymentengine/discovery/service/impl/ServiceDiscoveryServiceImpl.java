package com.paymentengine.discovery.service.impl;

import com.paymentengine.discovery.entity.ServiceInstance;
import com.paymentengine.discovery.entity.ServiceRegistry;
import com.paymentengine.discovery.repository.ServiceInstanceRepository;
import com.paymentengine.discovery.repository.ServiceRegistryRepository;
import com.paymentengine.discovery.service.ServiceDiscoveryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Transactional
public class ServiceDiscoveryServiceImpl implements ServiceDiscoveryService {
    
    private static final Logger logger = LoggerFactory.getLogger(ServiceDiscoveryServiceImpl.class);
    
    @Autowired
    private ServiceInstanceRepository serviceInstanceRepository;
    
    @Autowired
    private ServiceRegistryRepository serviceRegistryRepository;
    
    private final AtomicInteger roundRobinCounter = new AtomicInteger(0);
    private final Random random = new Random();
    
    @Override
    public ServiceInstance registerService(ServiceInstance serviceInstance) {
        logger.info("Registering service instance: {} - {}", serviceInstance.getServiceName(), serviceInstance.getInstanceId());
        
        // Check if service type is registered
        Optional<ServiceRegistry> serviceRegistry = serviceRegistryRepository.findByServiceName(serviceInstance.getServiceName());
        if (serviceRegistry.isEmpty()) {
            throw new IllegalArgumentException("Service type not registered: " + serviceInstance.getServiceName());
        }
        
        // Check if instance already exists
        Optional<ServiceInstance> existingInstance = serviceInstanceRepository.findByServiceNameAndInstanceId(
            serviceInstance.getServiceName(), serviceInstance.getInstanceId());
        
        if (existingInstance.isPresent()) {
            // Update existing instance
            ServiceInstance existing = existingInstance.get();
            existing.setHost(serviceInstance.getHost());
            existing.setPort(serviceInstance.getPort());
            existing.setSecurePort(serviceInstance.getSecurePort());
            existing.setHealthCheckUrl(serviceInstance.getHealthCheckUrl());
            existing.setStatus("UP");
            existing.setMetadata(serviceInstance.getMetadata());
            existing.setZone(serviceInstance.getZone());
            existing.setRegion(serviceInstance.getRegion());
            existing.setLastHeartbeat(LocalDateTime.now());
            existing.setUpdatedAt(LocalDateTime.now());
            
            return serviceInstanceRepository.save(existing);
        } else {
            // Register new instance
            serviceInstance.setStatus("UP");
            serviceInstance.setLastHeartbeat(LocalDateTime.now());
            return serviceInstanceRepository.save(serviceInstance);
        }
    }
    
    @Override
    public ServiceInstance updateServiceInstance(ServiceInstance serviceInstance) {
        logger.info("Updating service instance: {} - {}", serviceInstance.getServiceName(), serviceInstance.getInstanceId());
        
        Optional<ServiceInstance> existing = serviceInstanceRepository.findByServiceNameAndInstanceId(
            serviceInstance.getServiceName(), serviceInstance.getInstanceId());
        
        if (existing.isPresent()) {
            ServiceInstance existingInstance = existing.get();
            existingInstance.setHost(serviceInstance.getHost());
            existingInstance.setPort(serviceInstance.getPort());
            existingInstance.setSecurePort(serviceInstance.getSecurePort());
            existingInstance.setHealthCheckUrl(serviceInstance.getHealthCheckUrl());
            existingInstance.setMetadata(serviceInstance.getMetadata());
            existingInstance.setZone(serviceInstance.getZone());
            existingInstance.setRegion(serviceInstance.getRegion());
            existingInstance.setUpdatedAt(LocalDateTime.now());
            
            return serviceInstanceRepository.save(existingInstance);
        } else {
            throw new IllegalArgumentException("Service instance not found: " + serviceInstance.getServiceName() + " - " + serviceInstance.getInstanceId());
        }
    }
    
    @Override
    public void deregisterService(String serviceName, String instanceId) {
        logger.info("Deregistering service instance: {} - {}", serviceName, instanceId);
        serviceInstanceRepository.deleteByServiceNameAndInstanceId(serviceName, instanceId);
    }
    
    @Override
    public void deregisterAllInstances(String serviceName) {
        logger.info("Deregistering all instances for service: {}", serviceName);
        List<ServiceInstance> instances = serviceInstanceRepository.findByServiceName(serviceName);
        serviceInstanceRepository.deleteAll(instances);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ServiceInstance> discoverServices(String serviceName) {
        return serviceInstanceRepository.findByServiceName(serviceName);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ServiceInstance> discoverHealthyServices(String serviceName) {
        return serviceInstanceRepository.findHealthyInstancesByService(serviceName);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<ServiceInstance> discoverService(String serviceName, String instanceId) {
        return serviceInstanceRepository.findByServiceNameAndInstanceId(serviceName, instanceId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ServiceInstance> discoverServicesByZone(String serviceName, String zone) {
        return serviceInstanceRepository.findHealthyInstancesByServiceAndZone(serviceName, zone);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ServiceInstance> discoverServicesByRegion(String serviceName, String region) {
        return serviceInstanceRepository.findHealthyInstancesByServiceAndRegion(serviceName, region);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<ServiceInstance> getNextInstance(String serviceName, String loadBalancingStrategy) {
        List<ServiceInstance> instances = discoverHealthyServices(serviceName);
        
        if (instances.isEmpty()) {
            return Optional.empty();
        }
        
        return switch (loadBalancingStrategy.toUpperCase()) {
            case "ROUND_ROBIN" -> getRoundRobinInstance(instances);
            case "RANDOM" -> getRandomInstance(instances);
            case "LEAST_CONNECTIONS" -> getLeastConnectionsInstance(instances);
            default -> getRoundRobinInstance(instances);
        };
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<ServiceInstance> getNextInstanceByZone(String serviceName, String zone, String loadBalancingStrategy) {
        List<ServiceInstance> instances = discoverServicesByZone(serviceName, zone);
        
        if (instances.isEmpty()) {
            return Optional.empty();
        }
        
        return switch (loadBalancingStrategy.toUpperCase()) {
            case "ROUND_ROBIN" -> getRoundRobinInstance(instances);
            case "RANDOM" -> getRandomInstance(instances);
            case "LEAST_CONNECTIONS" -> getLeastConnectionsInstance(instances);
            default -> getRoundRobinInstance(instances);
        };
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<ServiceInstance> getNextInstanceByRegion(String serviceName, String region, String loadBalancingStrategy) {
        List<ServiceInstance> instances = discoverServicesByRegion(serviceName, region);
        
        if (instances.isEmpty()) {
            return Optional.empty();
        }
        
        return switch (loadBalancingStrategy.toUpperCase()) {
            case "ROUND_ROBIN" -> getRoundRobinInstance(instances);
            case "RANDOM" -> getRandomInstance(instances);
            case "LEAST_CONNECTIONS" -> getLeastConnectionsInstance(instances);
            default -> getRoundRobinInstance(instances);
        };
    }
    
    @Override
    public void updateHeartbeat(String serviceName, String instanceId) {
        Optional<ServiceInstance> instance = serviceInstanceRepository.findByServiceNameAndInstanceId(serviceName, instanceId);
        if (instance.isPresent()) {
            ServiceInstance serviceInstance = instance.get();
            serviceInstance.setLastHeartbeat(LocalDateTime.now());
            serviceInstance.setUpdatedAt(LocalDateTime.now());
            serviceInstanceRepository.save(serviceInstance);
        }
    }
    
    @Override
    public void markInstanceDown(String serviceName, String instanceId) {
        Optional<ServiceInstance> instance = serviceInstanceRepository.findByServiceNameAndInstanceId(serviceName, instanceId);
        if (instance.isPresent()) {
            ServiceInstance serviceInstance = instance.get();
            serviceInstance.setStatus("DOWN");
            serviceInstance.setUpdatedAt(LocalDateTime.now());
            serviceInstanceRepository.save(serviceInstance);
        }
    }
    
    @Override
    public void markInstanceUp(String serviceName, String instanceId) {
        Optional<ServiceInstance> instance = serviceInstanceRepository.findByServiceNameAndInstanceId(serviceName, instanceId);
        if (instance.isPresent()) {
            ServiceInstance serviceInstance = instance.get();
            serviceInstance.setStatus("UP");
            serviceInstance.setLastHeartbeat(LocalDateTime.now());
            serviceInstance.setUpdatedAt(LocalDateTime.now());
            serviceInstanceRepository.save(serviceInstance);
        }
    }
    
    @Override
    public void cleanupStaleInstances() {
        LocalDateTime cutoffTime = LocalDateTime.now().minusMinutes(5); // 5 minutes timeout
        List<ServiceInstance> staleInstances = serviceInstanceRepository.findStaleInstances(cutoffTime);
        
        for (ServiceInstance instance : staleInstances) {
            logger.warn("Cleaning up stale instance: {} - {}", instance.getServiceName(), instance.getInstanceId());
            instance.setStatus("DOWN");
            instance.setUpdatedAt(LocalDateTime.now());
            serviceInstanceRepository.save(instance);
        }
    }
    
    @Override
    public ServiceRegistry registerServiceType(ServiceRegistry serviceRegistry) {
        logger.info("Registering service type: {}", serviceRegistry.getServiceName());
        
        Optional<ServiceRegistry> existing = serviceRegistryRepository.findByServiceName(serviceRegistry.getServiceName());
        if (existing.isPresent()) {
            throw new IllegalArgumentException("Service type already registered: " + serviceRegistry.getServiceName());
        }
        
        return serviceRegistryRepository.save(serviceRegistry);
    }
    
    @Override
    public ServiceRegistry updateServiceRegistry(ServiceRegistry serviceRegistry) {
        logger.info("Updating service registry: {}", serviceRegistry.getServiceName());
        return serviceRegistryRepository.save(serviceRegistry);
    }
    
    @Override
    public void deregisterServiceType(String serviceName) {
        logger.info("Deregistering service type: {}", serviceName);
        serviceRegistryRepository.deleteByServiceName(serviceName);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ServiceRegistry> getAllServiceTypes() {
        return serviceRegistryRepository.findByIsActiveTrue();
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ServiceRegistry> getServiceTypesByType(String serviceType) {
        return serviceRegistryRepository.findActiveByServiceType(serviceType);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<ServiceRegistry> getServiceRegistry(String serviceName) {
        return serviceRegistryRepository.findByServiceName(serviceName);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<String> getAllActiveServiceNames() {
        return serviceInstanceRepository.findActiveServiceNames();
    }
    
    @Override
    @Transactional(readOnly = true)
    public long getHealthyInstanceCount(String serviceName) {
        return serviceInstanceRepository.countHealthyInstancesByService(serviceName);
    }
    
    @Override
    @Transactional(readOnly = true)
    public long getTotalInstanceCount(String serviceName) {
        return serviceInstanceRepository.findByServiceName(serviceName).size();
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ServiceInstance> getStaleInstances() {
        LocalDateTime cutoffTime = LocalDateTime.now().minusMinutes(5);
        return serviceInstanceRepository.findStaleInstances(cutoffTime);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ServiceInstance> getInstancesForServiceMesh(String serviceName) {
        return serviceInstanceRepository.findHealthyInstancesByService(serviceName);
    }
    
    @Override
    public void updateServiceMeshMetadata(String serviceName, String instanceId, String metadata) {
        Optional<ServiceInstance> instance = serviceInstanceRepository.findByServiceNameAndInstanceId(serviceName, instanceId);
        if (instance.isPresent()) {
            ServiceInstance serviceInstance = instance.get();
            serviceInstance.setMetadata(metadata);
            serviceInstance.setUpdatedAt(LocalDateTime.now());
            serviceInstanceRepository.save(serviceInstance);
        }
    }
    
    // Private helper methods for load balancing
    private Optional<ServiceInstance> getRoundRobinInstance(List<ServiceInstance> instances) {
        if (instances.isEmpty()) {
            return Optional.empty();
        }
        int index = roundRobinCounter.getAndIncrement() % instances.size();
        return Optional.of(instances.get(index));
    }
    
    private Optional<ServiceInstance> getRandomInstance(List<ServiceInstance> instances) {
        if (instances.isEmpty()) {
            return Optional.empty();
        }
        int index = random.nextInt(instances.size());
        return Optional.of(instances.get(index));
    }
    
    private Optional<ServiceInstance> getLeastConnectionsInstance(List<ServiceInstance> instances) {
        if (instances.isEmpty()) {
            return Optional.empty();
        }
        // For now, return the first instance. In a real implementation, you would track connection counts
        return Optional.of(instances.get(0));
    }
}