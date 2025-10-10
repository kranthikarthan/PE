package com.paymentengine.discovery.controller;

import com.paymentengine.discovery.entity.ServiceInstance;
import com.paymentengine.discovery.entity.ServiceRegistry;
import com.paymentengine.discovery.service.ServiceDiscoveryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/discovery")
@CrossOrigin(origins = "*")
public class ServiceDiscoveryController {
    
    @Autowired
    private ServiceDiscoveryService serviceDiscoveryService;
    
    // Service Instance Management
    @PostMapping("/instances")
    public ResponseEntity<ServiceInstance> registerService(@RequestBody ServiceInstance serviceInstance) {
        try {
            ServiceInstance registered = serviceDiscoveryService.registerService(serviceInstance);
            return ResponseEntity.ok(registered);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PutMapping("/instances/{serviceName}/{instanceId}")
    public ResponseEntity<ServiceInstance> updateServiceInstance(
            @PathVariable String serviceName,
            @PathVariable String instanceId,
            @RequestBody ServiceInstance serviceInstance) {
        try {
            serviceInstance.setServiceName(serviceName);
            serviceInstance.setInstanceId(instanceId);
            ServiceInstance updated = serviceDiscoveryService.updateServiceInstance(serviceInstance);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @DeleteMapping("/instances/{serviceName}/{instanceId}")
    public ResponseEntity<Void> deregisterService(
            @PathVariable String serviceName,
            @PathVariable String instanceId) {
        try {
            serviceDiscoveryService.deregisterService(serviceName, instanceId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @DeleteMapping("/instances/{serviceName}")
    public ResponseEntity<Void> deregisterAllInstances(@PathVariable String serviceName) {
        try {
            serviceDiscoveryService.deregisterAllInstances(serviceName);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    // Service Discovery
    @GetMapping("/instances/{serviceName}")
    public ResponseEntity<List<ServiceInstance>> discoverServices(@PathVariable String serviceName) {
        List<ServiceInstance> instances = serviceDiscoveryService.discoverServices(serviceName);
        return ResponseEntity.ok(instances);
    }
    
    @GetMapping("/instances/{serviceName}/healthy")
    public ResponseEntity<List<ServiceInstance>> discoverHealthyServices(@PathVariable String serviceName) {
        List<ServiceInstance> instances = serviceDiscoveryService.discoverHealthyServices(serviceName);
        return ResponseEntity.ok(instances);
    }
    
    @GetMapping("/instances/{serviceName}/{instanceId}")
    public ResponseEntity<ServiceInstance> discoverService(
            @PathVariable String serviceName,
            @PathVariable String instanceId) {
        Optional<ServiceInstance> instance = serviceDiscoveryService.discoverService(serviceName, instanceId);
        return instance.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/instances/{serviceName}/zone/{zone}")
    public ResponseEntity<List<ServiceInstance>> discoverServicesByZone(
            @PathVariable String serviceName,
            @PathVariable String zone) {
        List<ServiceInstance> instances = serviceDiscoveryService.discoverServicesByZone(serviceName, zone);
        return ResponseEntity.ok(instances);
    }
    
    @GetMapping("/instances/{serviceName}/region/{region}")
    public ResponseEntity<List<ServiceInstance>> discoverServicesByRegion(
            @PathVariable String serviceName,
            @PathVariable String region) {
        List<ServiceInstance> instances = serviceDiscoveryService.discoverServicesByRegion(serviceName, region);
        return ResponseEntity.ok(instances);
    }
    
    // Load Balancing
    @GetMapping("/instances/{serviceName}/next")
    public ResponseEntity<ServiceInstance> getNextInstance(
            @PathVariable String serviceName,
            @RequestParam(defaultValue = "ROUND_ROBIN") String strategy) {
        Optional<ServiceInstance> instance = serviceDiscoveryService.getNextInstance(serviceName, strategy);
        return instance.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/instances/{serviceName}/zone/{zone}/next")
    public ResponseEntity<ServiceInstance> getNextInstanceByZone(
            @PathVariable String serviceName,
            @PathVariable String zone,
            @RequestParam(defaultValue = "ROUND_ROBIN") String strategy) {
        Optional<ServiceInstance> instance = serviceDiscoveryService.getNextInstanceByZone(serviceName, zone, strategy);
        return instance.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/instances/{serviceName}/region/{region}/next")
    public ResponseEntity<ServiceInstance> getNextInstanceByRegion(
            @PathVariable String serviceName,
            @PathVariable String region,
            @RequestParam(defaultValue = "ROUND_ROBIN") String strategy) {
        Optional<ServiceInstance> instance = serviceDiscoveryService.getNextInstanceByRegion(serviceName, region, strategy);
        return instance.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }
    
    // Health Management
    @PostMapping("/instances/{serviceName}/{instanceId}/heartbeat")
    public ResponseEntity<Void> updateHeartbeat(
            @PathVariable String serviceName,
            @PathVariable String instanceId) {
        serviceDiscoveryService.updateHeartbeat(serviceName, instanceId);
        return ResponseEntity.ok().build();
    }
    
    @PostMapping("/instances/{serviceName}/{instanceId}/down")
    public ResponseEntity<Void> markInstanceDown(
            @PathVariable String serviceName,
            @PathVariable String instanceId) {
        serviceDiscoveryService.markInstanceDown(serviceName, instanceId);
        return ResponseEntity.ok().build();
    }
    
    @PostMapping("/instances/{serviceName}/{instanceId}/up")
    public ResponseEntity<Void> markInstanceUp(
            @PathVariable String serviceName,
            @PathVariable String instanceId) {
        serviceDiscoveryService.markInstanceUp(serviceName, instanceId);
        return ResponseEntity.ok().build();
    }
    
    @PostMapping("/instances/cleanup")
    public ResponseEntity<Void> cleanupStaleInstances() {
        serviceDiscoveryService.cleanupStaleInstances();
        return ResponseEntity.ok().build();
    }
    
    // Service Registry Management
    @PostMapping("/registry")
    public ResponseEntity<ServiceRegistry> registerServiceType(@RequestBody ServiceRegistry serviceRegistry) {
        try {
            ServiceRegistry registered = serviceDiscoveryService.registerServiceType(serviceRegistry);
            return ResponseEntity.ok(registered);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PutMapping("/registry/{serviceName}")
    public ResponseEntity<ServiceRegistry> updateServiceRegistry(
            @PathVariable String serviceName,
            @RequestBody ServiceRegistry serviceRegistry) {
        try {
            serviceRegistry.setServiceName(serviceName);
            ServiceRegistry updated = serviceDiscoveryService.updateServiceRegistry(serviceRegistry);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @DeleteMapping("/registry/{serviceName}")
    public ResponseEntity<Void> deregisterServiceType(@PathVariable String serviceName) {
        try {
            serviceDiscoveryService.deregisterServiceType(serviceName);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/registry")
    public ResponseEntity<List<ServiceRegistry>> getAllServiceTypes() {
        List<ServiceRegistry> registries = serviceDiscoveryService.getAllServiceTypes();
        return ResponseEntity.ok(registries);
    }
    
    @GetMapping("/registry/{serviceName}")
    public ResponseEntity<ServiceRegistry> getServiceRegistry(@PathVariable String serviceName) {
        Optional<ServiceRegistry> registry = serviceDiscoveryService.getServiceRegistry(serviceName);
        return registry.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/registry/type/{serviceType}")
    public ResponseEntity<List<ServiceRegistry>> getServiceTypesByType(@PathVariable String serviceType) {
        List<ServiceRegistry> registries = serviceDiscoveryService.getServiceTypesByType(serviceType);
        return ResponseEntity.ok(registries);
    }
    
    // Monitoring and Statistics
    @GetMapping("/services")
    public ResponseEntity<List<String>> getAllActiveServiceNames() {
        List<String> serviceNames = serviceDiscoveryService.getAllActiveServiceNames();
        return ResponseEntity.ok(serviceNames);
    }
    
    @GetMapping("/instances/{serviceName}/count/healthy")
    public ResponseEntity<Long> getHealthyInstanceCount(@PathVariable String serviceName) {
        long count = serviceDiscoveryService.getHealthyInstanceCount(serviceName);
        return ResponseEntity.ok(count);
    }
    
    @GetMapping("/instances/{serviceName}/count/total")
    public ResponseEntity<Long> getTotalInstanceCount(@PathVariable String serviceName) {
        long count = serviceDiscoveryService.getTotalInstanceCount(serviceName);
        return ResponseEntity.ok(count);
    }
    
    @GetMapping("/instances/stale")
    public ResponseEntity<List<ServiceInstance>> getStaleInstances() {
        List<ServiceInstance> instances = serviceDiscoveryService.getStaleInstances();
        return ResponseEntity.ok(instances);
    }
    
    // Service Mesh Integration
    @GetMapping("/instances/{serviceName}/mesh")
    public ResponseEntity<List<ServiceInstance>> getInstancesForServiceMesh(@PathVariable String serviceName) {
        List<ServiceInstance> instances = serviceDiscoveryService.getInstancesForServiceMesh(serviceName);
        return ResponseEntity.ok(instances);
    }
    
    @PutMapping("/instances/{serviceName}/{instanceId}/mesh/metadata")
    public ResponseEntity<Void> updateServiceMeshMetadata(
            @PathVariable String serviceName,
            @PathVariable String instanceId,
            @RequestBody String metadata) {
        serviceDiscoveryService.updateServiceMeshMetadata(serviceName, instanceId, metadata);
        return ResponseEntity.ok().build();
    }
}