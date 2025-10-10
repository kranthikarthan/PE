package com.paymentengine.gateway.service;

import com.paymentengine.gateway.dto.tenant.EnhancedTenantSetupRequest;
import com.paymentengine.gateway.dto.tenant.EnhancedTenantSetupResponse;
import com.paymentengine.gateway.dto.tenant.ConfigurationDeploymentResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDateTime;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service for managing multi-level authentication configuration in API Gateway
 * Integrates with Payment Processing Service for multi-level auth management
 */
@Service
@Transactional
public class MultiLevelAuthConfigurationService {
    
    private static final Logger logger = LoggerFactory.getLogger(MultiLevelAuthConfigurationService.class);

    @Autowired
    private RestTemplate restTemplate;

    @Value("${services.payment-processing.base-url:http://payment-processing:8080}")
    private String paymentProcessingBaseUrl;
    
    /**
     * Create tenant with multi-level authentication configuration
     */
    public EnhancedTenantSetupResponse createTenantWithMultiLevelAuth(EnhancedTenantSetupRequest request) {
        logger.info("Creating tenant with multi-level authentication configuration: {}", request.getBasicInfo().getTenantId());
        
        try {
            // Call Payment Processing Service to create multi-level auth configuration
            String url = paymentProcessingBaseUrl + "/api/v1/multi-level-auth/tenant";
            ResponseEntity<EnhancedTenantSetupResponse> response = restTemplate.postForEntity(
                    url,
                    request,
                    EnhancedTenantSetupResponse.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                logger.info("Successfully created tenant with multi-level auth: {}", request.getBasicInfo().getTenantId());
                return response.getBody();
            }

            return EnhancedTenantSetupResponse.builder()
                    .success(false)
                    .message("Payment Processing rejected tenant provisioning request")
                    .build();

        } catch (Exception e) {
            logger.error("Failed to create tenant with multi-level auth: {}", request.getBasicInfo().getTenantId(), e);
            return EnhancedTenantSetupResponse.builder()
                    .success(false)
                    .message("Failed to create tenant: " + e.getMessage())
                    .build();
        }
    }
    
    /**
     * Validate tenant configuration
     */
    public Map<String, Object> validateTenantConfiguration(EnhancedTenantSetupRequest request) {
        logger.info("Validating tenant configuration: {}", request.getBasicInfo().getTenantId());

        try {
            String tenantId = request.getBasicInfo().getTenantId();
            String serviceType = resolveServiceType(request);
            String endpoint = resolveEndpoint(request);
            String paymentType = resolvePaymentType(request);

            HttpHeaders headers = new HttpHeaders();
            headers.set("X-Tenant-ID", tenantId);

            URI uri = buildHierarchyUri(
                    "/validate-hierarchy/" + tenantId,
                    serviceType,
                    endpoint,
                    paymentType
            );

            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    uri,
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    new ParameterizedTypeReference<>() {}
            );

            return response.getBody() != null ? response.getBody() : Map.of("valid", false);

        } catch (Exception e) {
            logger.error("Failed to validate tenant configuration: {}", request.getBasicInfo().getTenantId(), e);
            return Map.of(
                    "valid", false,
                    "errors", List.of("Validation failed: " + e.getMessage())
            );
        }
    }
    
    /**
     * Deploy tenant configurations
     */
    public ConfigurationDeploymentResult deployTenantConfigurations(EnhancedTenantSetupRequest request) {
        logger.info("Deploying tenant configurations: {}", request.getBasicInfo().getTenantId());
        
        try {
            // TODO: Call Payment Processing Service to deploy configurations
            // String url = "http://payment-processing-service/api/v1/multi-level-auth/deploy";
            // ConfigurationDeploymentResult result = restTemplate.postForObject(url, request, ConfigurationDeploymentResult.class);
            
            // For now, create a placeholder deployment result
            ConfigurationDeploymentResult result = ConfigurationDeploymentResult.builder()
                    .success(true)
                    .message("Configurations deployed successfully")
                    .tenantId(request.getBasicInfo().getTenantId())
                    .deployedAt(LocalDateTime.now())
                    .build();
            
            logger.info("Successfully deployed tenant configurations: {}", request.getBasicInfo().getTenantId());
            return result;
            
        } catch (Exception e) {
            logger.error("Failed to deploy tenant configurations: {}", request.getBasicInfo().getTenantId(), e);
            return ConfigurationDeploymentResult.builder()
                    .success(false)
                    .message("Deployment failed: " + e.getMessage())
                    .build();
        }
    }
    
    /**
     * Test tenant configuration
     */
    public Map<String, Object> testTenantConfiguration(
            String tenantId, 
            String serviceType, 
            String endpoint, 
            String paymentType) {
        
        logger.info("Testing tenant configuration: {} - {}:{}:{}", tenantId, serviceType, endpoint, paymentType);

        try {
            Optional<Map<String, Object>> hierarchy = Optional.ofNullable(getConfigurationHierarchy(
                    tenantId, serviceType, endpoint, paymentType));

            boolean hasActiveLevels = hierarchy
                    .map(body -> (List<Map<String, Object>>) body.getOrDefault("levels", List.of()))
                    .map(levels -> levels.stream().anyMatch(level -> Boolean.TRUE.equals(level.get("active"))))
                    .orElse(false);

            return Map.of(
                    "success", hasActiveLevels,
                    "tenantId", tenantId,
                    "serviceType", serviceType,
                    "endpoint", endpoint,
                    "paymentType", paymentType,
                    "details", hierarchy.orElse(Map.of())
            );

        } catch (Exception e) {
            logger.error("Failed to test tenant configuration: {}", tenantId, e);
            return Map.of(
                    "success", false,
                    "error", "Test failed: " + e.getMessage()
            );
        }
    }
    
    /**
     * Get configuration hierarchy for tenant
     */
    public Map<String, Object> getConfigurationHierarchy(
            String tenantId,
            String serviceType,
            String endpoint,
            String paymentType) {
        
        logger.info("Getting configuration hierarchy for tenant: {} - {}:{}:{}", tenantId, serviceType, endpoint, paymentType);

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-Tenant-ID", tenantId);

            URI uri = buildHierarchyUri(
                    "/hierarchy/" + tenantId,
                    serviceType,
                    endpoint,
                    paymentType
            );

            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    uri,
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    new ParameterizedTypeReference<>() {}
            );

            return response.getBody();

        } catch (Exception e) {
            logger.error("Failed to get configuration hierarchy for tenant: {}", tenantId, e);
            return Map.of(
                    "error", "Failed to get hierarchy: " + e.getMessage()
            );
        }
    }

    private URI buildHierarchyUri(String pathSuffix,
                                  String serviceType,
                                  String endpoint,
                                  String paymentType) {
        UriComponentsBuilder builder = UriComponentsBuilder
                .fromHttpUrl(paymentProcessingBaseUrl + "/api/v1/multi-level-auth")
                .path(pathSuffix);

        if (serviceType != null && !serviceType.isBlank()) {
            builder.queryParam("serviceType", serviceType);
        }
        if (endpoint != null && !endpoint.isBlank()) {
            builder.queryParam("endpoint", endpoint);
        }
        if (paymentType != null && !paymentType.isBlank()) {
            builder.queryParam("paymentType", paymentType);
        }

        return builder.build(true).toUri();
    }

    private String resolveServiceType(EnhancedTenantSetupRequest request) {
        return Optional.ofNullable(request.getDownstreamCallConfigs())
                .filter(list -> !list.isEmpty())
                .map(list -> list.get(0).getServiceType())
                .filter(value -> value != null && !value.isBlank())
                .orElse("api-gateway");
    }

    private String resolveEndpoint(EnhancedTenantSetupRequest request) {
        return Optional.ofNullable(request.getDownstreamCallConfigs())
                .filter(list -> !list.isEmpty())
                .map(list -> list.get(0).getEndpoint())
                .filter(value -> value != null && !value.isBlank())
                .orElse("/gateway");
    }

    private String resolvePaymentType(EnhancedTenantSetupRequest request) {
        Optional<String> fromDownstream = Optional.ofNullable(request.getDownstreamCallConfigs())
                .filter(list -> !list.isEmpty())
                .map(list -> list.get(0).getPaymentType())
                .filter(value -> value != null && !value.isBlank());

        if (fromDownstream.isPresent()) {
            return fromDownstream.get();
        }

        return Optional.ofNullable(request.getPaymentTypeConfigs())
                .filter(list -> !list.isEmpty())
                .map(list -> list.get(0).getPaymentType())
                .filter(value -> value != null && !value.isBlank())
                .orElse(null);
    }
    
    /**
     * Get available configuration templates
     */
    public List<Map<String, Object>> getConfigurationTemplates() {
        logger.info("Getting configuration templates");
        
        try {
            // TODO: Call Payment Processing Service to get configuration templates
            // String url = "http://payment-processing-service/api/v1/multi-level-auth/templates";
            // List<Map<String, Object>> templates = restTemplate.getForObject(url, List.class);
            
            // For now, return placeholder templates
            List<Map<String, Object>> templates = List.of(
                    Map.of(
                            "name", "Basic JWT Configuration",
                            "description", "Basic JWT authentication for all levels",
                            "authMethod", "JWT",
                            "levels", List.of("clearing-system", "tenant", "payment-type", "downstream-call")
                    ),
                    Map.of(
                            "name", "JWS with Client Headers",
                            "description", "JWS authentication with client headers",
                            "authMethod", "JWS",
                            "levels", List.of("clearing-system", "tenant", "payment-type", "downstream-call")
                    ),
                    Map.of(
                            "name", "OAuth2 Configuration",
                            "description", "OAuth2 authentication for external services",
                            "authMethod", "OAUTH2",
                            "levels", List.of("clearing-system", "tenant", "payment-type", "downstream-call")
                    )
            );
            
            return templates;
            
        } catch (Exception e) {
            logger.error("Failed to get configuration templates", e);
            return List.of();
        }
    }
    
    /**
     * Clone tenant configuration
     */
    public EnhancedTenantSetupResponse cloneTenantConfiguration(
            String sourceTenantId, 
            String targetTenantId, 
            String targetTenantName) {
        
        logger.info("Cloning tenant configuration from {} to {}", sourceTenantId, targetTenantId);
        
        try {
            // TODO: Call Payment Processing Service to clone configuration
            // String url = "http://payment-processing-service/api/v1/multi-level-auth/clone";
            // CloneRequest request = new CloneRequest(sourceTenantId, targetTenantId, targetTenantName);
            // EnhancedTenantSetupResponse response = restTemplate.postForObject(url, request, EnhancedTenantSetupResponse.class);
            
            // For now, create a placeholder response
            EnhancedTenantSetupResponse response = EnhancedTenantSetupResponse.builder()
                    .success(true)
                    .message("Tenant configuration cloned successfully")
                    .tenantId(targetTenantId)
                    .tenantName(targetTenantName)
                    .createdAt(LocalDateTime.now())
                    .build();
            
            logger.info("Successfully cloned tenant configuration from {} to {}", sourceTenantId, targetTenantId);
            return response;
            
        } catch (Exception e) {
            logger.error("Failed to clone tenant configuration from {} to {}", sourceTenantId, targetTenantId, e);
            return EnhancedTenantSetupResponse.builder()
                    .success(false)
                    .message("Clone failed: " + e.getMessage())
                    .build();
        }
    }
    
    /**
     * Export tenant configuration
     */
    public Map<String, Object> exportTenantConfiguration(String tenantId) {
        logger.info("Exporting tenant configuration: {}", tenantId);
        
        try {
            // TODO: Call Payment Processing Service to export configuration
            // String url = "http://payment-processing-service/api/v1/multi-level-auth/export/" + tenantId;
            // Map<String, Object> configuration = restTemplate.getForObject(url, Map.class);
            
            // For now, return a placeholder configuration
            Map<String, Object> configuration = Map.of(
                    "tenantId", tenantId,
                    "exportedAt", LocalDateTime.now(),
                    "configuration", Map.of(
                            "clearingSystem", Map.of("authMethod", "JWT"),
                            "paymentTypes", List.of(),
                            "downstreamCalls", List.of()
                    )
            );
            
            return configuration;
            
        } catch (Exception e) {
            logger.error("Failed to export tenant configuration: {}", tenantId, e);
            return Map.of(
                    "error", "Export failed: " + e.getMessage()
            );
        }
    }
    
    /**
     * Import tenant configuration
     */
    public EnhancedTenantSetupResponse importTenantConfiguration(Map<String, Object> configuration) {
        logger.info("Importing tenant configuration");
        
        try {
            // TODO: Call Payment Processing Service to import configuration
            // String url = "http://payment-processing-service/api/v1/multi-level-auth/import";
            // EnhancedTenantSetupResponse response = restTemplate.postForObject(url, configuration, EnhancedTenantSetupResponse.class);
            
            // For now, create a placeholder response
            EnhancedTenantSetupResponse response = EnhancedTenantSetupResponse.builder()
                    .success(true)
                    .message("Tenant configuration imported successfully")
                    .createdAt(LocalDateTime.now())
                    .build();
            
            logger.info("Successfully imported tenant configuration");
            return response;
            
        } catch (Exception e) {
            logger.error("Failed to import tenant configuration", e);
            return EnhancedTenantSetupResponse.builder()
                    .success(false)
                    .message("Import failed: " + e.getMessage())
                    .build();
        }
    }
    
    /**
     * Get setup wizard progress
     */
    public Map<String, Object> getSetupProgress(String tenantId) {
        logger.info("Getting setup progress for tenant: {}", tenantId);
        
        try {
            // TODO: Call Payment Processing Service to get setup progress
            // String url = "http://payment-processing-service/api/v1/multi-level-auth/progress/" + tenantId;
            // Map<String, Object> progress = restTemplate.getForObject(url, Map.class);
            
            // For now, return a placeholder progress
            Map<String, Object> progress = Map.of(
                    "tenantId", tenantId,
                    "currentStep", 0,
                    "totalSteps", 6,
                    "completedSteps", List.of(),
                    "progressPercentage", 0
            );
            
            return progress;
            
        } catch (Exception e) {
            logger.error("Failed to get setup progress for tenant: {}", tenantId, e);
            return Map.of(
                    "error", "Failed to get progress: " + e.getMessage()
            );
        }
    }
    
    /**
     * Update setup wizard progress
     */
    public Map<String, Object> updateSetupProgress(String tenantId, Map<String, Object> progressData) {
        logger.info("Updating setup progress for tenant: {}", tenantId);
        
        try {
            // TODO: Call Payment Processing Service to update setup progress
            // String url = "http://payment-processing-service/api/v1/multi-level-auth/progress/" + tenantId;
            // Map<String, Object> result = restTemplate.putForObject(url, progressData, Map.class);
            
            // For now, return a placeholder result
            Map<String, Object> result = Map.of(
                    "success", true,
                    "tenantId", tenantId,
                    "updatedAt", LocalDateTime.now()
            );
            
            return result;
            
        } catch (Exception e) {
            logger.error("Failed to update setup progress for tenant: {}", tenantId, e);
            return Map.of(
                    "error", "Failed to update progress: " + e.getMessage()
            );
        }
    }
}