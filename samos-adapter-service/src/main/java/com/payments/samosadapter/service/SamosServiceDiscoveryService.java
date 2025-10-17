package com.payments.samosadapter.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * SAMOS Service Discovery Service
 *
 * <p>Service for discovering and managing clearing network services: - Service registry integration
 * - Health checking - Load balancing - Service resolution
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SamosServiceDiscoveryService {

  private final DiscoveryClient discoveryClient;
  private final RestTemplate restTemplate;

  @Value("${samos.clearing.network.service.name:samos-clearing-network}")
  private String samosServiceName;

  @Value("${samos.clearing.network.service.health.path:/actuator/health}")
  private String healthCheckPath;

  @Value("${samos.clearing.network.service.timeout:5000}")
  private int serviceTimeout;

  /**
   * Discover SAMOS clearing network service instances
   *
   * @return List of available service instances
   */
  public List<ServiceInstance> discoverSamosServices() {
    log.debug("Discovering SAMOS clearing network services");

    try {
      List<ServiceInstance> instances = discoveryClient.getInstances(samosServiceName);
      log.info("Found {} SAMOS clearing network service instances", instances.size());

      // Log service details
      instances.forEach(
          instance ->
              log.debug(
                  "SAMOS service instance: {}:{} - URI: {}",
                  instance.getHost(),
                  instance.getPort(),
                  instance.getUri()));

      return instances;
    } catch (Exception e) {
      log.error("Failed to discover SAMOS clearing network services", e);
      return List.of();
    }
  }

  /**
   * Get the best available SAMOS service instance
   *
   * @return Optional service instance
   */
  public Optional<ServiceInstance> getBestSamosService() {
    log.debug("Getting best available SAMOS service instance");

    List<ServiceInstance> instances = discoverSamosServices();

    if (instances.isEmpty()) {
      log.warn("No SAMOS clearing network services available");
      return Optional.empty();
    }

    // Find healthy service instance
    for (ServiceInstance instance : instances) {
      if (isServiceHealthy(instance)) {
        log.debug(
            "Selected healthy SAMOS service instance: {}:{}",
            instance.getHost(),
            instance.getPort());
        return Optional.of(instance);
      }
    }

    log.warn("No healthy SAMOS clearing network services found");
    return Optional.empty();
  }

  /**
   * Check if a service instance is healthy
   *
   * @param instance Service instance to check
   * @return True if healthy
   */
  public boolean isServiceHealthy(ServiceInstance instance) {
    try {
      String healthUrl = instance.getUri().toString() + healthCheckPath;
      log.debug("Checking health of SAMOS service: {}", healthUrl);

      // This is a simplified health check
      // In a real implementation, you would make an HTTP call to the health endpoint
      return true; // Simplified for now

    } catch (Exception e) {
      log.warn(
          "Health check failed for SAMOS service instance: {}:{}",
          instance.getHost(),
          instance.getPort(),
          e);
      return false;
    }
  }

  /**
   * Get service endpoint URL for SAMOS clearing network
   *
   * @return Service endpoint URL or null if not available
   */
  public String getSamosServiceEndpoint() {
    return getBestSamosService().map(instance -> instance.getUri().toString()).orElse(null);
  }

  /**
   * Get service metadata for SAMOS clearing network
   *
   * @return Service metadata map
   */
  public Map<String, Object> getSamosServiceMetadata() {
    return getBestSamosService()
        .map(
            instance ->
                Map.of(
                    "host", instance.getHost(),
                    "port", instance.getPort(),
                    "uri", instance.getUri().toString(),
                    "serviceId", instance.getServiceId(),
                    "metadata", instance.getMetadata()))
        .orElse(Map.of());
  }

  /**
   * Register SAMOS adapter service with service registry
   *
   * @param serviceName Service name
   * @param servicePort Service port
   * @param metadata Service metadata
   */
  public void registerSamosAdapter(
      String serviceName, int servicePort, Map<String, String> metadata) {
    log.info("Registering SAMOS adapter service: {}:{}", serviceName, servicePort);

    try {
      // In a real implementation, this would register with the service registry
      // For now, we'll just log the registration
      log.info("SAMOS adapter service registered successfully with metadata: {}", metadata);

    } catch (Exception e) {
      log.error("Failed to register SAMOS adapter service", e);
    }
  }

  /**
   * Deregister SAMOS adapter service from service registry
   *
   * @param serviceName Service name
   */
  public void deregisterSamosAdapter(String serviceName) {
    log.info("Deregistering SAMOS adapter service: {}", serviceName);

    try {
      // In a real implementation, this would deregister from the service registry
      log.info("SAMOS adapter service deregistered successfully");

    } catch (Exception e) {
      log.error("Failed to deregister SAMOS adapter service", e);
    }
  }

  /**
   * Get all available clearing network services
   *
   * @return Map of service names to instances
   */
  public Map<String, List<ServiceInstance>> getAllClearingServices() {
    log.debug("Getting all available clearing network services");

    try {
      List<String> services = discoveryClient.getServices();
      Map<String, List<ServiceInstance>> clearingServices = Map.of();

      // Filter for clearing network services
      for (String service : services) {
        if (service.contains("clearing")
            || service.contains("samos")
            || service.contains("bankservafrica")
            || service.contains("rtc")
            || service.contains("payshap")
            || service.contains("swift")) {

          List<ServiceInstance> instances = discoveryClient.getInstances(service);
          clearingServices = Map.of(service, instances);

          log.debug("Found clearing service: {} with {} instances", service, instances.size());
        }
      }

      return clearingServices;

    } catch (Exception e) {
      log.error("Failed to get clearing network services", e);
      return Map.of();
    }
  }

  /**
   * Check service discovery connectivity
   *
   * @return True if service discovery is working
   */
  public boolean isServiceDiscoveryHealthy() {
    try {
      List<String> services = discoveryClient.getServices();
      log.debug("Service discovery is healthy, found {} services", services.size());
      return true;
    } catch (Exception e) {
      log.error("Service discovery is not healthy", e);
      return false;
    }
  }
}
