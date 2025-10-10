package com.paymentengine.gateway.service;

import com.paymentengine.gateway.dto.multilevel.ResolvedAuthConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service for managing configuration hierarchy in API Gateway
 * Integrates with Payment Processing Service for configuration precedence management
 */
@Service
@Transactional
public class ConfigurationHierarchyService {

    private static final Logger logger = LoggerFactory.getLogger(ConfigurationHierarchyService.class);

    private final RestTemplate restTemplate;
    private final String paymentProcessingBaseUrl;

    public ConfigurationHierarchyService(RestTemplate restTemplate,
                                         @Value("${services.payment-processing.base-url:http://payment-processing:8080}")
                                         String paymentProcessingBaseUrl) {
        this.restTemplate = restTemplate;
        this.paymentProcessingBaseUrl = paymentProcessingBaseUrl;
    }

    /**
     * Get configuration hierarchy for a tenant
     */
    public Optional<Map<String, Object>> getConfigurationHierarchy(String tenantId) {
        logger.info("Getting configuration hierarchy for tenant: {}", tenantId);

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-Tenant-ID", tenantId);

            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    URI.create(paymentProcessingBaseUrl + "/api/v1/multi-level-auth/hierarchy/" + tenantId),
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    new ParameterizedTypeReference<Map<String, Object>>() {}
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                logger.info("Successfully retrieved configuration hierarchy for tenant: {}", tenantId);
                return Optional.of(response.getBody());
            }

            return Optional.empty();

        } catch (Exception e) {
            logger.error("Failed to get configuration hierarchy for tenant: {}", tenantId, e);
            return Optional.empty();
        }
    }
    
    /**
     * Resolve configuration precedence for a specific context
     */
    public Optional<ResolvedAuthConfiguration> resolveConfigurationPrecedence(
            String tenantId,
            String serviceType,
            String endpoint,
            String paymentType) {
        
        logger.info("Resolving configuration precedence for tenant: {}, serviceType: {}, endpoint: {}, paymentType: {}", 
                tenantId, serviceType, endpoint, paymentType);
        
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-Tenant-ID", tenantId);

            String url = paymentProcessingBaseUrl + "/api/v1/multi-level-auth/resolve/" + tenantId
                    + "?serviceType=" + urlEncode(serviceType)
                    + "&endpoint=" + urlEncode(endpoint);
            if (paymentType != null) {
                url = url + "&paymentType=" + urlEncode(paymentType);
            }

            ResponseEntity<ResolvedAuthConfiguration> response = restTemplate.exchange(
                    URI.create(url),
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    ResolvedAuthConfiguration.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                logger.info("Successfully resolved configuration precedence for tenant: {}", tenantId);
                return Optional.of(response.getBody());
            }

            return Optional.empty();

        } catch (Exception e) {
            logger.error("Failed to resolve configuration precedence for tenant: {}", tenantId, e);
            return Optional.empty();
        }
    }
    
    /**
     * Validate configuration hierarchy
     */
    public boolean validateConfigurationHierarchy(String tenantId) {
        logger.info("Validating configuration hierarchy for tenant: {}", tenantId);

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-Tenant-ID", tenantId);

            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    URI.create(paymentProcessingBaseUrl + "/api/v1/multi-level-auth/validate-hierarchy/" + tenantId
                            + "?serviceType=" + urlEncode("api-gateway")
                            + "&endpoint=" + urlEncode("gateway")),
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    new ParameterizedTypeReference<Map<String, Object>>() {}
            );

            boolean valid = response.getStatusCode().is2xxSuccessful()
                    && response.getBody() != null
                    && Boolean.TRUE.equals(response.getBody().get("valid"));

            logger.info("Validated configuration hierarchy for tenant: {} -> {}", tenantId, valid);
            return valid;

        } catch (Exception e) {
            logger.error("Failed to validate configuration hierarchy for tenant: {}", tenantId, e);
            return false;
        }
    }
    
    /**
     * Get configuration precedence rules
     */
    public List<String> getConfigurationPrecedenceRules() {
        logger.info("Getting configuration precedence rules");

        List<String> rules = List.of(
                "1. Downstream Call Level (Highest Priority) - Most specific configuration for individual service calls",
                "2. Payment Type Level - Configuration for specific payment types (SEPA, SWIFT, etc.)",
                "3. Tenant Level - Tenant-specific configuration",
                "4. Clearing System Level (Lowest Priority) - Global configuration for the entire clearing system"
        );
        
        logger.info("Successfully retrieved {} configuration precedence rules", rules.size());
        return rules;
    }

    private String urlEncode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}