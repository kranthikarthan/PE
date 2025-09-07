package com.paymentengine.middleware.service.impl;

import com.paymentengine.middleware.entity.TenantConfiguration;
import com.paymentengine.middleware.repository.TenantConfigurationRepository;
import com.paymentengine.middleware.service.TenantCloningService;
import com.paymentengine.middleware.dto.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Implementation of TenantCloningService
 * Provides comprehensive tenant cloning, versioning, and migration capabilities
 */
@Service
@Transactional
public class TenantCloningServiceImpl implements TenantCloningService {

    private static final Logger logger = LoggerFactory.getLogger(TenantCloningServiceImpl.class);

    @Autowired
    private TenantConfigurationRepository tenantConfigurationRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private final YAMLFactory yamlFactory = new YAMLFactory();
    private final XmlMapper xmlMapper = new XmlMapper();

    @Override
    public TenantCloneResponse cloneTenant(TenantCloneRequest request) {
        logger.info("Starting tenant cloning: {} -> {}", request.getSourceTenantId(), request.getTargetTenantId());
        
        try {
            // Validate source tenant exists
            TenantConfiguration sourceConfig = getSourceConfiguration(request.getSourceTenantId(), request.getSourceVersion());
            if (sourceConfig == null) {
                return TenantCloneResponse.error("Source tenant configuration not found");
            }

            // Generate target version if not provided
            String targetVersion = request.getTargetVersion();
            if (targetVersion == null) {
                targetVersion = generateVersion(request.getTargetTenantId());
            }

            // Check if target version already exists
            if (tenantConfigurationRepository.findByTenantIdAndVersion(request.getTargetTenantId(), targetVersion).isPresent()) {
                return TenantCloneResponse.error("Target version already exists: " + targetVersion);
            }

            // Create new tenant configuration
            TenantConfiguration newConfig = createClonedConfiguration(sourceConfig, request, targetVersion);

            // Save the new configuration
            TenantConfiguration savedConfig = tenantConfigurationRepository.save(newConfig);

            // Create response
            TenantCloneResponse response = TenantCloneResponse.success(
                "Tenant cloned successfully", 
                savedConfig.getId(), 
                savedConfig.getTenantId(), 
                savedConfig.getVersion()
            );

            response.setEnvironment(savedConfig.getEnvironment());
            response.setSourceTenantId(savedConfig.getSourceTenantId());
            response.setSourceVersion(savedConfig.getSourceVersion());
            response.setClonedAt(savedConfig.getClonedAt());
            response.setClonedBy(savedConfig.getClonedBy());

            // Create summary
            Map<String, Object> summary = new HashMap<>();
            summary.put("configurationDataCount", savedConfig.getConfigurationData() != null ? savedConfig.getConfigurationData().size() : 0);
            summary.put("metadataCount", savedConfig.getMetadata() != null ? savedConfig.getMetadata().size() : 0);
            summary.put("overridesApplied", request.getConfigurationOverrides() != null ? request.getConfigurationOverrides().size() : 0);
            response.setSummary(summary);

            logger.info("Tenant cloning completed successfully: {} -> {} (version: {})", 
                request.getSourceTenantId(), request.getTargetTenantId(), targetVersion);

            return response;

        } catch (Exception e) {
            logger.error("Error cloning tenant: {}", e.getMessage(), e);
            return TenantCloneResponse.error("Failed to clone tenant: " + e.getMessage());
        }
    }

    @Override
    public TenantCloneResponse cloneTenantToEnvironment(TenantCloneRequest request) {
        logger.info("Cloning tenant to different environment: {} -> {} ({})", 
            request.getSourceTenantId(), request.getTargetTenantId(), request.getTargetEnvironment());

        // Set environment-specific overrides
        Map<String, String> envOverrides = getEnvironmentOverrides(request.getTargetEnvironment());
        if (request.getConfigurationOverrides() == null) {
            request.setConfigurationOverrides(envOverrides);
        } else {
            request.getConfigurationOverrides().putAll(envOverrides);
        }

        return cloneTenant(request);
    }

    @Override
    public TenantCloneResponse cloneTenantVersion(TenantCloneRequest request) {
        logger.info("Cloning specific tenant version: {} (version: {}) -> {}", 
            request.getSourceTenantId(), request.getSourceVersion(), request.getTargetTenantId());

        if (request.getSourceVersion() == null) {
            return TenantCloneResponse.error("Source version is required for version-specific cloning");
        }

        return cloneTenant(request);
    }

    @Override
    public TenantExportResponse exportTenant(TenantExportRequest request) {
        logger.info("Exporting tenant configuration: {} (version: {})", request.getTenantId(), request.getVersion());

        try {
            // Get tenant configuration(s) to export
            List<TenantConfiguration> configsToExport = getConfigurationsToExport(request);
            if (configsToExport.isEmpty()) {
                return TenantExportResponse.error("No configurations found to export");
            }

            // Create export data
            Map<String, Object> exportData = createExportData(configsToExport, request);

            // Generate export file
            String exportId = generateExportId();
            String filePath = generateExportFile(exportData, request, exportId);

            // Create response
            TenantExportResponse response = TenantExportResponse.success(
                "Tenant exported successfully", 
                exportId, 
                request.getTenantId()
            );

            response.setVersion(request.getVersion());
            response.setExportFormat(request.getExportFormat());
            response.setFilePath(filePath);
            response.setDownloadUrl("/api/tenant-management/exports/" + exportId + "/download");
            response.setFileSize(Files.size(Paths.get(filePath)));
            response.setExportedAt(LocalDateTime.now());
            response.setExportedBy(request.getExportedBy());

            // Create export summary
            Map<String, Object> summary = new HashMap<>();
            summary.put("configurationsExported", configsToExport.size());
            summary.put("versionsIncluded", configsToExport.stream().map(TenantConfiguration::getVersion).collect(Collectors.toList()));
            summary.put("totalConfigurationData", configsToExport.stream().mapToInt(c -> c.getConfigurationData() != null ? c.getConfigurationData().size() : 0).sum());
            summary.put("totalMetadata", configsToExport.stream().mapToInt(c -> c.getMetadata() != null ? c.getMetadata().size() : 0).sum());
            response.setExportSummary(summary);

            logger.info("Tenant export completed successfully: {} (export ID: {})", request.getTenantId(), exportId);

            return response;

        } catch (Exception e) {
            logger.error("Error exporting tenant: {}", e.getMessage(), e);
            return TenantExportResponse.error("Failed to export tenant: " + e.getMessage());
        }
    }

    @Override
    public TenantImportResponse importTenant(TenantImportRequest request) {
        logger.info("Importing tenant configuration: {} -> {}", request.getImportData(), request.getTargetTenantId());

        try {
            // Parse import data
            Map<String, Object> importData = parseImportData(request.getImportData(), request.getImportFormat());

            // Validate import data
            if (request.getValidateBeforeImport()) {
                Map<String, Object> validationResults = validateImportData(importData, request);
                if (!(Boolean) validationResults.get("valid")) {
                    return TenantImportResponse.error("Import validation failed", validationResults);
                }
            }

            // Create tenant configurations from import data
            List<TenantConfiguration> importedConfigs = createConfigurationsFromImport(importData, request);

            // Save configurations
            List<TenantConfiguration> savedConfigs = tenantConfigurationRepository.saveAll(importedConfigs);

            // Create response
            TenantImportResponse response = TenantImportResponse.success(
                "Tenant imported successfully",
                savedConfigs.get(0).getId(),
                request.getTargetTenantId(),
                savedConfigs.get(0).getVersion()
            );

            response.setImportedAt(LocalDateTime.now());
            response.setImportedBy(request.getImportedBy());

            // Create import summary
            Map<String, Object> summary = new HashMap<>();
            summary.put("configurationsImported", savedConfigs.size());
            summary.put("versionsImported", savedConfigs.stream().map(TenantConfiguration::getVersion).collect(Collectors.toList()));
            response.setImportSummary(summary);

            logger.info("Tenant import completed successfully: {} -> {} ({} configurations)", 
                request.getImportData(), request.getTargetTenantId(), savedConfigs.size());

            return response;

        } catch (Exception e) {
            logger.error("Error importing tenant: {}", e.getMessage(), e);
            return TenantImportResponse.error("Failed to import tenant: " + e.getMessage());
        }
    }

    @Override
    public List<String> getAvailableTenants() {
        return tenantConfigurationRepository.findDistinctTenantIds();
    }

    @Override
    public List<String> getTenantVersions(String tenantId) {
        return tenantConfigurationRepository.findVersionsByTenantId(tenantId);
    }

    @Override
    public List<TenantConfiguration> getTenantHistory(String tenantId) {
        return tenantConfigurationRepository.findByTenantIdOrderByCreatedAtDesc(tenantId);
    }

    @Override
    public Map<String, Object> compareTenantConfigurations(String tenantId1, String version1, String tenantId2, String version2) {
        logger.info("Comparing tenant configurations: {}:{} vs {}:{}", tenantId1, version1, tenantId2, version2);

        try {
            TenantConfiguration config1 = tenantConfigurationRepository.findByTenantIdAndVersion(tenantId1, version1)
                .orElseThrow(() -> new RuntimeException("Configuration not found: " + tenantId1 + ":" + version1));
            
            TenantConfiguration config2 = tenantConfigurationRepository.findByTenantIdAndVersion(tenantId2, version2)
                .orElseThrow(() -> new RuntimeException("Configuration not found: " + tenantId2 + ":" + version2));

            Map<String, Object> comparison = new HashMap<>();
            
            // Compare basic properties
            comparison.put("tenantId1", config1.getTenantId());
            comparison.put("version1", config1.getVersion());
            comparison.put("tenantId2", config2.getTenantId());
            comparison.put("version2", config2.getVersion());
            comparison.put("environment1", config1.getEnvironment());
            comparison.put("environment2", config2.getEnvironment());

            // Compare configuration data
            Map<String, Object> configDataComparison = compareMaps(config1.getConfigurationData(), config2.getConfigurationData());
            comparison.put("configurationDataComparison", configDataComparison);

            // Compare metadata
            Map<String, Object> metadataComparison = compareMaps(config1.getMetadata(), config2.getMetadata());
            comparison.put("metadataComparison", metadataComparison);

            // Calculate similarity score
            double similarityScore = calculateSimilarityScore(configDataComparison, metadataComparison);
            comparison.put("similarityScore", similarityScore);

            return comparison;

        } catch (Exception e) {
            logger.error("Error comparing tenant configurations: {}", e.getMessage(), e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return error;
        }
    }

    @Override
    public Map<String, Object> validateTenantConfiguration(String tenantId, String version) {
        logger.info("Validating tenant configuration: {}:{}", tenantId, version);

        try {
            TenantConfiguration config = tenantConfigurationRepository.findByTenantIdAndVersion(tenantId, version)
                .orElseThrow(() -> new RuntimeException("Configuration not found: " + tenantId + ":" + version));

            Map<String, Object> validation = new HashMap<>();
            List<String> errors = new ArrayList<>();
            List<String> warnings = new ArrayList<>();

            // Validate basic properties
            if (config.getTenantId() == null || config.getTenantId().trim().isEmpty()) {
                errors.add("Tenant ID is required");
            }

            if (config.getVersion() == null || config.getVersion().trim().isEmpty()) {
                errors.add("Version is required");
            }

            if (config.getEnvironment() == null) {
                errors.add("Environment is required");
            }

            // Validate configuration data
            if (config.getConfigurationData() == null || config.getConfigurationData().isEmpty()) {
                warnings.add("No configuration data found");
            }

            // Validate metadata
            if (config.getMetadata() == null || config.getMetadata().isEmpty()) {
                warnings.add("No metadata found");
            }

            // Check for required configuration keys
            validateRequiredConfigurationKeys(config.getConfigurationData(), errors, warnings);

            validation.put("valid", errors.isEmpty());
            validation.put("errors", errors);
            validation.put("warnings", warnings);
            validation.put("configurationId", config.getId());
            validation.put("tenantId", config.getTenantId());
            validation.put("version", config.getVersion());

            return validation;

        } catch (Exception e) {
            logger.error("Error validating tenant configuration: {}", e.getMessage(), e);
            Map<String, Object> error = new HashMap<>();
            error.put("valid", false);
            error.put("error", e.getMessage());
            return error;
        }
    }

    @Override
    public Map<String, Object> getCloningStatistics() {
        logger.info("Getting cloning statistics");

        Map<String, Object> stats = new HashMap<>();

        // Total configurations
        long totalConfigs = tenantConfigurationRepository.count();
        stats.put("totalConfigurations", totalConfigs);

        // Configurations by environment
        Map<String, Long> configsByEnvironment = new HashMap<>();
        for (TenantConfiguration.Environment env : TenantConfiguration.Environment.values()) {
            long count = tenantConfigurationRepository.countByEnvironment(env);
            configsByEnvironment.put(env.name(), count);
        }
        stats.put("configurationsByEnvironment", configsByEnvironment);

        // Active configurations
        Map<String, Long> activeConfigsByEnvironment = new HashMap<>();
        for (TenantConfiguration.Environment env : TenantConfiguration.Environment.values()) {
            long count = tenantConfigurationRepository.countByEnvironmentAndIsActiveTrue(env);
            activeConfigsByEnvironment.put(env.name(), count);
        }
        stats.put("activeConfigurationsByEnvironment", activeConfigsByEnvironment);

        // Cloned configurations
        List<String> templateTenants = tenantConfigurationRepository.findTemplateTenantIds();
        stats.put("templateTenants", templateTenants);
        stats.put("templateTenantCount", templateTenants.size());

        // Recent activity
        LocalDateTime oneWeekAgo = LocalDateTime.now().minusWeeks(1);
        List<TenantConfiguration> recentConfigs = tenantConfigurationRepository.findByCreatedAtBetween(oneWeekAgo, LocalDateTime.now());
        stats.put("recentConfigurations", recentConfigs.size());

        return stats;
    }

    @Override
    public TenantCloneResponse rollbackTenant(String tenantId, String targetVersion) {
        logger.info("Rolling back tenant: {} to version: {}", tenantId, targetVersion);

        try {
            // Get target configuration
            TenantConfiguration targetConfig = tenantConfigurationRepository.findByTenantIdAndVersion(tenantId, targetVersion)
                .orElseThrow(() -> new RuntimeException("Target version not found: " + targetVersion));

            // Deactivate current active configuration
            Optional<TenantConfiguration> currentActive = tenantConfigurationRepository.findByTenantIdAndIsActiveTrue(tenantId);
            if (currentActive.isPresent()) {
                TenantConfiguration active = currentActive.get();
                active.setIsActive(false);
                tenantConfigurationRepository.save(active);
            }

            // Activate target configuration
            targetConfig.setIsActive(true);
            targetConfig.setUpdatedAt(LocalDateTime.now());
            TenantConfiguration savedConfig = tenantConfigurationRepository.save(targetConfig);

            TenantCloneResponse response = TenantCloneResponse.success(
                "Tenant rolled back successfully",
                savedConfig.getId(),
                savedConfig.getTenantId(),
                savedConfig.getVersion()
            );

            response.setEnvironment(savedConfig.getEnvironment());
            response.setClonedAt(LocalDateTime.now());

            return response;

        } catch (Exception e) {
            logger.error("Error rolling back tenant: {}", e.getMessage(), e);
            return TenantCloneResponse.error("Failed to rollback tenant: " + e.getMessage());
        }
    }

    @Override
    public TenantCloneResponse createTemplate(String tenantId, String version, String templateName) {
        logger.info("Creating template from tenant: {} (version: {}) with name: {}", tenantId, version, templateName);

        try {
            TenantConfiguration sourceConfig = tenantConfigurationRepository.findByTenantIdAndVersion(tenantId, version)
                .orElseThrow(() -> new RuntimeException("Source configuration not found"));

            // Create template configuration
            TenantConfiguration templateConfig = new TenantConfiguration();
            templateConfig.setTenantId("TEMPLATE_" + templateName.toUpperCase());
            templateConfig.setVersion("1.0.0");
            templateConfig.setName(templateName);
            templateConfig.setDescription("Template created from " + tenantId + " version " + version);
            templateConfig.setEnvironment(sourceConfig.getEnvironment());
            templateConfig.setIsActive(false);
            templateConfig.setIsDefault(false);
            templateConfig.setSourceTenantId(tenantId);
            templateConfig.setSourceVersion(version);
            templateConfig.setClonedBy("SYSTEM");
            templateConfig.setClonedAt(LocalDateTime.now());
            templateConfig.setConfigurationData(new HashMap<>(sourceConfig.getConfigurationData()));
            templateConfig.setMetadata(new HashMap<>(sourceConfig.getMetadata()));
            templateConfig.setCreatedBy("SYSTEM");
            templateConfig.setUpdatedBy("SYSTEM");

            // Add template metadata
            templateConfig.getMetadata().put("template.name", templateName);
            templateConfig.getMetadata().put("template.created.from", tenantId + ":" + version);
            templateConfig.getMetadata().put("template.created.at", LocalDateTime.now().toString());

            TenantConfiguration savedTemplate = tenantConfigurationRepository.save(templateConfig);

            return TenantCloneResponse.success(
                "Template created successfully",
                savedTemplate.getId(),
                savedTemplate.getTenantId(),
                savedTemplate.getVersion()
            );

        } catch (Exception e) {
            logger.error("Error creating template: {}", e.getMessage(), e);
            return TenantCloneResponse.error("Failed to create template: " + e.getMessage());
        }
    }

    @Override
    public TenantCloneResponse applyTemplate(String tenantId, String templateName, Map<String, String> overrides) {
        logger.info("Applying template: {} to tenant: {}", templateName, tenantId);

        try {
            // Find template
            TenantConfiguration template = tenantConfigurationRepository.findByTenantIdAndIsActiveTrue("TEMPLATE_" + templateName.toUpperCase())
                .orElseThrow(() -> new RuntimeException("Template not found: " + templateName));

            // Create clone request
            TenantCloneRequest request = new TenantCloneRequest();
            request.setSourceTenantId(template.getTenantId());
            request.setSourceVersion(template.getVersion());
            request.setTargetTenantId(tenantId);
            request.setTargetEnvironment(template.getEnvironment());
            request.setConfigurationOverrides(overrides);
            request.setClonedBy("SYSTEM");
            request.setChangeLog("Applied template: " + templateName);

            return cloneTenant(request);

        } catch (Exception e) {
            logger.error("Error applying template: {}", e.getMessage(), e);
            return TenantCloneResponse.error("Failed to apply template: " + e.getMessage());
        }
    }

    // Helper methods

    private TenantConfiguration getSourceConfiguration(String tenantId, String version) {
        if (version != null) {
            return tenantConfigurationRepository.findByTenantIdAndVersion(tenantId, version).orElse(null);
        } else {
            return tenantConfigurationRepository.findByTenantIdAndIsActiveTrue(tenantId).orElse(null);
        }
    }

    private String generateVersion(String tenantId) {
        Optional<String> latestVersion = tenantConfigurationRepository.findLatestVersionByTenantId(tenantId);
        if (latestVersion.isPresent()) {
            String[] parts = latestVersion.get().split("\\.");
            int major = Integer.parseInt(parts[0]);
            int minor = Integer.parseInt(parts[1]);
            int patch = Integer.parseInt(parts[2]);
            return String.format("%d.%d.%d", major, minor, patch + 1);
        } else {
            return "1.0.0";
        }
    }

    private TenantConfiguration createClonedConfiguration(TenantConfiguration source, TenantCloneRequest request, String targetVersion) {
        TenantConfiguration newConfig = new TenantConfiguration();
        newConfig.setTenantId(request.getTargetTenantId());
        newConfig.setVersion(targetVersion);
        newConfig.setName(request.getName() != null ? request.getName() : source.getName());
        newConfig.setDescription(request.getDescription() != null ? request.getDescription() : source.getDescription());
        newConfig.setEnvironment(request.getTargetEnvironment());
        newConfig.setIsActive(request.getActivateAfterClone());
        newConfig.setIsDefault(false);
        newConfig.setSourceTenantId(source.getTenantId());
        newConfig.setSourceVersion(source.getVersion());
        newConfig.setClonedBy(request.getClonedBy());
        newConfig.setClonedAt(LocalDateTime.now());
        newConfig.setCreatedBy(request.getClonedBy());
        newConfig.setUpdatedBy(request.getClonedBy());

        // Copy configuration data
        if (request.getCopyConfigurationData() && source.getConfigurationData() != null) {
            Map<String, String> configData = new HashMap<>(source.getConfigurationData());
            if (request.getConfigurationOverrides() != null) {
                configData.putAll(request.getConfigurationOverrides());
            }
            newConfig.setConfigurationData(configData);
        }

        // Copy metadata
        if (request.getCopyMetadata() && source.getMetadata() != null) {
            Map<String, String> metadata = new HashMap<>(source.getMetadata());
            if (request.getMetadataOverrides() != null) {
                metadata.putAll(request.getMetadataOverrides());
            }
            if (request.getAdditionalMetadata() != null) {
                metadata.putAll(request.getAdditionalMetadata());
            }
            newConfig.setMetadata(metadata);
        }

        // Set change log
        if (request.getChangeLog() != null) {
            newConfig.setChangeLog(request.getChangeLog());
        } else {
            newConfig.setChangeLog("Cloned from " + source.getTenantId() + " version " + source.getVersion());
        }

        return newConfig;
    }

    private Map<String, String> getEnvironmentOverrides(TenantConfiguration.Environment environment) {
        Map<String, String> overrides = new HashMap<>();
        
        switch (environment) {
            case DEVELOPMENT:
                overrides.put("logging.level", "DEBUG");
                overrides.put("debug.enabled", "true");
                overrides.put("cache.enabled", "false");
                break;
            case INTEGRATION:
                overrides.put("logging.level", "INFO");
                overrides.put("debug.enabled", "false");
                overrides.put("cache.enabled", "true");
                break;
            case USER_ACCEPTANCE:
                overrides.put("logging.level", "WARN");
                overrides.put("debug.enabled", "false");
                overrides.put("cache.enabled", "true");
                break;
            case PRODUCTION:
                overrides.put("logging.level", "ERROR");
                overrides.put("debug.enabled", "false");
                overrides.put("cache.enabled", "true");
                overrides.put("monitoring.enabled", "true");
                break;
        }
        
        return overrides;
    }

    private List<TenantConfiguration> getConfigurationsToExport(TenantExportRequest request) {
        if (request.getIncludeVersions() != null && !request.getIncludeVersions().isEmpty()) {
            return request.getIncludeVersions().stream()
                .map(version -> tenantConfigurationRepository.findByTenantIdAndVersion(request.getTenantId(), version))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
        } else if (request.getVersion() != null) {
            return tenantConfigurationRepository.findByTenantIdAndVersion(request.getTenantId(), request.getVersion())
                .map(List::of)
                .orElse(Collections.emptyList());
        } else {
            return tenantConfigurationRepository.findByTenantIdOrderByCreatedAtDesc(request.getTenantId());
        }
    }

    private Map<String, Object> createExportData(List<TenantConfiguration> configs, TenantExportRequest request) {
        Map<String, Object> exportData = new HashMap<>();
        
        exportData.put("exportMetadata", Map.of(
            "exportedAt", LocalDateTime.now().toString(),
            "exportedBy", request.getExportedBy(),
            "exportReason", request.getExportReason(),
            "tenantId", request.getTenantId(),
            "configurationsCount", configs.size()
        ));

        List<Map<String, Object>> configurations = configs.stream()
            .map(this::convertConfigurationToMap)
            .collect(Collectors.toList());
        
        exportData.put("configurations", configurations);
        
        return exportData;
    }

    private Map<String, Object> convertConfigurationToMap(TenantConfiguration config) {
        Map<String, Object> configMap = new HashMap<>();
        configMap.put("id", config.getId().toString());
        configMap.put("tenantId", config.getTenantId());
        configMap.put("version", config.getVersion());
        configMap.put("name", config.getName());
        configMap.put("description", config.getDescription());
        configMap.put("isActive", config.getIsActive());
        configMap.put("isDefault", config.getIsDefault());
        configMap.put("environment", config.getEnvironment().name());
        configMap.put("sourceTenantId", config.getSourceTenantId());
        configMap.put("sourceVersion", config.getSourceVersion());
        configMap.put("clonedBy", config.getClonedBy());
        configMap.put("clonedAt", config.getClonedAt() != null ? config.getClonedAt().toString() : null);
        configMap.put("configurationData", config.getConfigurationData());
        configMap.put("metadata", config.getMetadata());
        configMap.put("changeLog", config.getChangeLog());
        configMap.put("createdBy", config.getCreatedBy());
        configMap.put("updatedBy", config.getUpdatedBy());
        configMap.put("createdAt", config.getCreatedAt().toString());
        configMap.put("updatedAt", config.getUpdatedAt().toString());
        
        return configMap;
    }

    private String generateExportId() {
        return "export_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8);
    }

    private String generateExportFile(Map<String, Object> exportData, TenantExportRequest request, String exportId) throws IOException {
        String fileName = String.format("tenant_export_%s_%s.%s", 
            request.getTenantId(), 
            exportId, 
            request.getExportFormat().toLowerCase());
        
        Path exportDir = Paths.get("/tmp/exports");
        Files.createDirectories(exportDir);
        
        String filePath = exportDir.resolve(fileName).toString();
        
        // Write file based on format
        switch (request.getExportFormat().toUpperCase()) {
            case "JSON":
                objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File(filePath), exportData);
                break;
            case "YAML":
                ObjectMapper yamlMapper = new ObjectMapper(yamlFactory);
                yamlMapper.writerWithDefaultPrettyPrinter().writeValue(new File(filePath), exportData);
                break;
            case "XML":
                xmlMapper.writerWithDefaultPrettyPrinter().writeValue(new File(filePath), exportData);
                break;
            default:
                throw new IllegalArgumentException("Unsupported export format: " + request.getExportFormat());
        }
        
        // Compress if requested
        if (request.getCompress()) {
            String compressedPath = filePath + ".zip";
            try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(Paths.get(compressedPath)))) {
                ZipEntry entry = new ZipEntry(fileName);
                zos.putNextEntry(entry);
                Files.copy(Paths.get(filePath), zos);
                zos.closeEntry();
            }
            Files.delete(Paths.get(filePath));
            return compressedPath;
        }
        
        return filePath;
    }

    private Map<String, Object> parseImportData(String importData, String format) throws IOException {
        switch (format.toUpperCase()) {
            case "JSON":
                return objectMapper.readValue(importData, Map.class);
            case "YAML":
                ObjectMapper yamlMapper = new ObjectMapper(yamlFactory);
                return yamlMapper.readValue(importData, Map.class);
            case "XML":
                return xmlMapper.readValue(importData, Map.class);
            default:
                throw new IllegalArgumentException("Unsupported import format: " + format);
        }
    }

    private Map<String, Object> validateImportData(Map<String, Object> importData, TenantImportRequest request) {
        Map<String, Object> validation = new HashMap<>();
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        // Check if configurations exist
        if (!importData.containsKey("configurations")) {
            errors.add("No configurations found in import data");
        }

        // Check if target tenant already exists
        if (!request.getOverwriteExisting()) {
            Optional<TenantConfiguration> existing = tenantConfigurationRepository.findByTenantIdAndIsActiveTrue(request.getTargetTenantId());
            if (existing.isPresent()) {
                errors.add("Target tenant already exists and overwrite is disabled");
            }
        }

        validation.put("valid", errors.isEmpty());
        validation.put("errors", errors);
        validation.put("warnings", warnings);

        return validation;
    }

    private List<TenantConfiguration> createConfigurationsFromImport(Map<String, Object> importData, TenantImportRequest request) {
        List<Map<String, Object>> configMaps = (List<Map<String, Object>>) importData.get("configurations");
        
        return configMaps.stream()
            .map(configMap -> createConfigurationFromMap(configMap, request))
            .collect(Collectors.toList());
    }

    private TenantConfiguration createConfigurationFromMap(Map<String, Object> configMap, TenantImportRequest request) {
        TenantConfiguration config = new TenantConfiguration();
        
        config.setTenantId(request.getTargetTenantId());
        config.setVersion(request.getTargetVersion() != null ? request.getTargetVersion() : (String) configMap.get("version"));
        config.setName(request.getName() != null ? request.getName() : (String) configMap.get("name"));
        config.setDescription(request.getDescription() != null ? request.getDescription() : (String) configMap.get("description"));
        config.setEnvironment(request.getTargetEnvironment() != null ? request.getTargetEnvironment() : 
            TenantConfiguration.Environment.valueOf((String) configMap.get("environment")));
        config.setIsActive(request.getActivateAfterImport());
        config.setIsDefault(false);
        config.setSourceTenantId((String) configMap.get("sourceTenantId"));
        config.setSourceVersion((String) configMap.get("sourceVersion"));
        config.setClonedBy(request.getImportedBy());
        config.setClonedAt(LocalDateTime.now());
        config.setCreatedBy(request.getImportedBy());
        config.setUpdatedBy(request.getImportedBy());
        
        // Set configuration data
        Map<String, String> configData = (Map<String, String>) configMap.get("configurationData");
        if (configData != null && request.getConfigurationOverrides() != null) {
            configData.putAll(request.getConfigurationOverrides());
        }
        config.setConfigurationData(configData);
        
        // Set metadata
        Map<String, String> metadata = (Map<String, String>) configMap.get("metadata");
        if (metadata != null) {
            if (request.getMetadataOverrides() != null) {
                metadata.putAll(request.getMetadataOverrides());
            }
            if (request.getAdditionalMetadata() != null) {
                metadata.putAll(request.getAdditionalMetadata());
            }
        }
        config.setMetadata(metadata);
        
        // Set change log
        if (request.getChangeLog() != null) {
            config.setChangeLog(request.getChangeLog());
        } else {
            config.setChangeLog("Imported from external source");
        }
        
        return config;
    }

    private Map<String, Object> compareMaps(Map<String, String> map1, Map<String, String> map2) {
        Map<String, Object> comparison = new HashMap<>();
        
        Set<String> allKeys = new HashSet<>();
        if (map1 != null) allKeys.addAll(map1.keySet());
        if (map2 != null) allKeys.addAll(map2.keySet());
        
        List<String> onlyInFirst = new ArrayList<>();
        List<String> onlyInSecond = new ArrayList<>();
        List<String> different = new ArrayList<>();
        List<String> same = new ArrayList<>();
        
        for (String key : allKeys) {
            String value1 = map1 != null ? map1.get(key) : null;
            String value2 = map2 != null ? map2.get(key) : null;
            
            if (value1 == null) {
                onlyInSecond.add(key);
            } else if (value2 == null) {
                onlyInFirst.add(key);
            } else if (!value1.equals(value2)) {
                different.add(key);
            } else {
                same.add(key);
            }
        }
        
        comparison.put("onlyInFirst", onlyInFirst);
        comparison.put("onlyInSecond", onlyInSecond);
        comparison.put("different", different);
        comparison.put("same", same);
        comparison.put("totalKeys", allKeys.size());
        
        return comparison;
    }

    private double calculateSimilarityScore(Map<String, Object> configComparison, Map<String, Object> metadataComparison) {
        int configSame = ((List<String>) configComparison.get("same")).size();
        int configTotal = (Integer) configComparison.get("totalKeys");
        int metadataSame = ((List<String>) metadataComparison.get("same")).size();
        int metadataTotal = (Integer) metadataComparison.get("totalKeys");
        
        double configScore = configTotal > 0 ? (double) configSame / configTotal : 1.0;
        double metadataScore = metadataTotal > 0 ? (double) metadataSame / metadataTotal : 1.0;
        
        return (configScore + metadataScore) / 2.0;
    }

    private void validateRequiredConfigurationKeys(Map<String, String> configData, List<String> errors, List<String> warnings) {
        if (configData == null) return;
        
        // Add validation for required keys
        String[] requiredKeys = {"tenant.id", "environment", "database.url"};
        for (String key : requiredKeys) {
            if (!configData.containsKey(key)) {
                warnings.add("Missing recommended configuration key: " + key);
            }
        }
    }
}