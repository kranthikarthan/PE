package com.paymentengine.paymentprocessing.controller;

import com.paymentengine.paymentprocessing.entity.ClearingSystemAuthConfiguration;
import com.paymentengine.paymentprocessing.entity.DownstreamCallAuthConfiguration;
import com.paymentengine.paymentprocessing.entity.PaymentTypeAuthConfiguration;
import com.paymentengine.paymentprocessing.entity.TenantAuthConfiguration;
import com.paymentengine.paymentprocessing.service.MultiLevelAuthConfigurationService;
import io.micrometer.core.annotation.Timed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/multi-level-auth")
@CrossOrigin(origins = "*")
public class MultiLevelAuthConfigurationController {

    private static final Logger logger = LoggerFactory.getLogger(MultiLevelAuthConfigurationController.class);

    private final MultiLevelAuthConfigurationService multiLevelAuthConfigurationService;

    public MultiLevelAuthConfigurationController(MultiLevelAuthConfigurationService multiLevelAuthConfigurationService) {
        this.multiLevelAuthConfigurationService = multiLevelAuthConfigurationService;
    }

    @GetMapping("/hierarchy/{tenantId}")
    @PreAuthorize("hasAuthority('multi-level-auth:read')")
    @Timed(value = "multi.level.auth.hierarchy", description = "Time taken to retrieve auth hierarchy")
    public ResponseEntity<Map<String, Object>> getHierarchy(@PathVariable String tenantId,
                                                            @RequestParam(required = false) String serviceType,
                                                            @RequestParam(required = false) String endpoint,
                                                            @RequestParam(required = false) String paymentType) {
        logger.debug("Retrieving multi-level auth hierarchy for tenant: {}", tenantId);

        List<Map<String, Object>> levels = new ArrayList<>();
        multiLevelAuthConfigurationService.getClearingSystemConfigurationForCurrentEnvironment()
                .ifPresentOrElse(config -> levels.add(buildClearingSystemLevel(config)),
                        () -> levels.add(buildInactiveLevel("clearing-system")));

        multiLevelAuthConfigurationService.getTenantConfiguration(tenantId)
                .ifPresentOrElse(config -> levels.add(buildTenantLevel(config)),
                        () -> levels.add(buildInactiveLevel("tenant")));

        if (paymentType != null) {
            multiLevelAuthConfigurationService.getPaymentTypeConfiguration(tenantId, paymentType)
                    .ifPresentOrElse(config -> levels.add(buildPaymentTypeLevel(paymentType, config)),
                            () -> levels.add(buildInactiveLevel("payment-type")));
        }

        if (serviceType != null && endpoint != null) {
            multiLevelAuthConfigurationService.getDownstreamCallConfiguration(tenantId, serviceType, endpoint)
                    .ifPresentOrElse(config -> levels.add(buildDownstreamLevel(serviceType, endpoint, config)),
                            () -> levels.add(buildInactiveLevel("downstream-call")));
        }

        Map<String, Object> response = new HashMap<>();
        response.put("tenantId", tenantId);
        response.put("serviceType", serviceType);
        response.put("endpoint", endpoint);
        response.put("paymentType", paymentType);
        response.put("levels", levels);
        response.put("timestamp", Instant.now().toString());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/resolve/{tenantId}")
    @PreAuthorize("hasAuthority('multi-level-auth:resolve')")
    @Timed(value = "multi.level.auth.resolve", description = "Time taken to resolve auth configuration")
    public ResponseEntity<MultiLevelAuthConfigurationService.ResolvedAuthConfiguration> resolve(@PathVariable String tenantId,
                                                                                                @RequestParam String serviceType,
                                                                                                @RequestParam String endpoint,
                                                                                                @RequestParam(required = false) String paymentType) {
        logger.debug("Resolving auth configuration for tenant: {}, service: {}, endpoint: {}, paymentType: {}",
                tenantId, serviceType, endpoint, paymentType);

        MultiLevelAuthConfigurationService.ResolvedAuthConfiguration configuration =
                multiLevelAuthConfigurationService.getResolvedConfiguration(tenantId, serviceType, endpoint, paymentType);
        return ResponseEntity.ok(configuration);
    }

    @GetMapping("/validate-hierarchy/{tenantId}")
    @PreAuthorize("hasAuthority('multi-level-auth:validate')")
    @Timed(value = "multi.level.auth.validate", description = "Time taken to validate auth hierarchy")
    public ResponseEntity<Map<String, Object>> validate(@PathVariable String tenantId,
                                                        @RequestParam String serviceType,
                                                        @RequestParam String endpoint,
                                                        @RequestParam(required = false) String paymentType) {
        MultiLevelAuthConfigurationService.ResolvedAuthConfiguration configuration =
                multiLevelAuthConfigurationService.getResolvedConfiguration(tenantId, serviceType, endpoint, paymentType);

        Map<String, Object> response = new HashMap<>();
        response.put("tenantId", tenantId);
        response.put("serviceType", serviceType);
        response.put("endpoint", endpoint);
        response.put("paymentType", paymentType);
        response.put("valid", configuration.getAuthMethod() != null);
        response.put("authMethod", configuration.getAuthMethod());
        response.put("apiKeyConfigured", configuration.getApiKey() != null);
        response.put("timestamp", Instant.now().toString());

        return ResponseEntity.ok(response);
    }

    private Map<String, Object> buildClearingSystemLevel(ClearingSystemAuthConfiguration config) {
        Map<String, Object> level = new HashMap<>();
        level.put("level", "clearing-system");
        level.put("active", Boolean.TRUE.equals(config.getIsActive()));
        level.put("environment", config.getEnvironment());
        level.put("authMethod", config.getAuthMethod());
        level.put("includeClientHeaders", config.getIncludeClientHeaders());
        level.put("clientId", config.getClientId());
        level.put("updatedAt", config.getUpdatedAt());
        return level;
    }

    private Map<String, Object> buildTenantLevel(TenantAuthConfiguration config) {
        Map<String, Object> level = new HashMap<>();
        level.put("level", "tenant");
        level.put("active", Boolean.TRUE.equals(config.getIsActive()));
        level.put("authMethod", config.getAuthMethod());
        level.put("apiKeyHeaderName", config.getApiKeyHeaderName());
        level.put("includeClientHeaders", config.getIncludeClientHeaders());
        level.put("updatedAt", config.getUpdatedAt());
        return level;
    }

    private Map<String, Object> buildPaymentTypeLevel(String paymentType, PaymentTypeAuthConfiguration config) {
        Map<String, Object> level = new HashMap<>();
        level.put("level", "payment-type");
        level.put("paymentType", paymentType);
        level.put("active", Boolean.TRUE.equals(config.getIsActive()));
        level.put("authMethod", config.getAuthMethod());
        level.put("includeClientHeaders", config.getIncludeClientHeaders());
        level.put("updatedAt", config.getUpdatedAt());
        return level;
    }

    private Map<String, Object> buildDownstreamLevel(String serviceType,
                                                     String endpoint,
                                                     DownstreamCallAuthConfiguration config) {
        Map<String, Object> level = new HashMap<>();
        level.put("level", "downstream-call");
        level.put("serviceType", serviceType);
        level.put("endpoint", endpoint);
        level.put("active", Boolean.TRUE.equals(config.getIsActive()));
        level.put("authMethod", config.getAuthMethod());
        level.put("timeoutSeconds", config.getTimeoutSeconds());
        level.put("retryAttempts", config.getRetryAttempts());
        level.put("updatedAt", config.getUpdatedAt());
        return level;
    }

    private Map<String, Object> buildInactiveLevel(String levelName) {
        Map<String, Object> level = new HashMap<>();
        level.put("level", levelName);
        level.put("active", false);
        return level;
    }
}
