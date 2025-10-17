package com.payments.bankservafricaadapter.controller;

import com.payments.bankservafricaadapter.domain.BankservAfricaAdapter;
import com.payments.bankservafricaadapter.service.BankservAfricaAdapterService;
import com.payments.domain.shared.ClearingAdapterId;
import com.payments.domain.shared.TenantContext;
import com.payments.domain.clearing.ClearingNetwork;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * REST Controller for BankservAfrica Adapter management
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/bankservafrica-adapters")
@RequiredArgsConstructor
@Tag(name = "BankservAfrica Adapter", description = "BankservAfrica clearing adapter management")
public class BankservAfricaAdapterController {
    
    private final BankservAfricaAdapterService adapterService;
    
    /**
     * Create a new BankservAfrica adapter
     */
    @PostMapping
    @Operation(summary = "Create BankservAfrica adapter", description = "Create a new BankservAfrica clearing adapter")
    public ResponseEntity<BankservAfricaAdapter> createBankservAfricaAdapter(
            @Valid @RequestBody CreateBankservAfricaAdapterRequest request) {
        
        log.info("Creating BankservAfrica adapter: {}", request.getAdapterName());
        
        ClearingAdapterId adapterId = ClearingAdapterId.generate();
        TenantContext tenantContext = TenantContext.of(
                request.getTenantId(), 
                request.getTenantName(),
                request.getBusinessUnitId(), 
                request.getBusinessUnitName());
        
        BankservAfricaAdapter adapter = adapterService.createAdapter(
                adapterId,
                tenantContext,
                request.getAdapterName(),
                request.getNetwork(),
                request.getEndpoint(),
                request.getCreatedBy()).join();
        
        return ResponseEntity.status(HttpStatus.CREATED).body(adapter);
    }
    
    /**
     * Update BankservAfrica adapter configuration
     */
    @PutMapping("/{adapterId}/configuration")
    @Operation(summary = "Update adapter configuration", description = "Update BankservAfrica adapter configuration")
    public ResponseEntity<BankservAfricaAdapter> updateBankservAfricaAdapterConfiguration(
            @PathVariable @Parameter(description = "Adapter ID") String adapterId,
            @Valid @RequestBody UpdateBankservAfricaAdapterConfigurationRequest request) {
        
        log.info("Updating BankservAfrica adapter configuration: {}", adapterId);
        
        BankservAfricaAdapter adapter = adapterService.updateAdapterConfiguration(
                ClearingAdapterId.of(adapterId),
                request.getEndpoint(),
                request.getApiVersion(),
                request.getTimeoutSeconds(),
                request.getRetryAttempts(),
                request.getEncryptionEnabled(),
                request.getBatchSize(),
                request.getProcessingWindowStart(),
                request.getProcessingWindowEnd(),
                request.getUpdatedBy()).join();
        
        return ResponseEntity.ok(adapter);
    }
    
    /**
     * Activate BankservAfrica adapter
     */
    @PostMapping("/{adapterId}/activate")
    @Operation(summary = "Activate adapter", description = "Activate BankservAfrica adapter")
    public ResponseEntity<BankservAfricaAdapter> activateBankservAfricaAdapter(
            @PathVariable @Parameter(description = "Adapter ID") String adapterId,
            @Valid @RequestBody ActivateBankservAfricaAdapterRequest request) {
        
        log.info("Activating BankservAfrica adapter: {}", adapterId);
        
        BankservAfricaAdapter adapter = adapterService.activateAdapter(
                ClearingAdapterId.of(adapterId),
                request.getActivatedBy());
        
        return ResponseEntity.ok(adapter);
    }
    
    /**
     * Deactivate BankservAfrica adapter
     */
    @PostMapping("/{adapterId}/deactivate")
    @Operation(summary = "Deactivate adapter", description = "Deactivate BankservAfrica adapter")
    public ResponseEntity<BankservAfricaAdapter> deactivateBankservAfricaAdapter(
            @PathVariable @Parameter(description = "Adapter ID") String adapterId,
            @Valid @RequestBody DeactivateBankservAfricaAdapterRequest request) {
        
        log.info("Deactivating BankservAfrica adapter: {}", adapterId);
        
        BankservAfricaAdapter adapter = adapterService.deactivateAdapter(
                ClearingAdapterId.of(adapterId),
                request.getReason(),
                request.getDeactivatedBy());
        
        return ResponseEntity.ok(adapter);
    }
    
    /**
     * Get BankservAfrica adapter by ID
     */
    @GetMapping("/{adapterId}")
    @Operation(summary = "Get adapter by ID", description = "Get BankservAfrica adapter by ID")
    public ResponseEntity<BankservAfricaAdapter> getBankservAfricaAdapter(
            @PathVariable @Parameter(description = "Adapter ID") String adapterId) {
        
        log.info("Getting BankservAfrica adapter: {}", adapterId);
        
        Optional<BankservAfricaAdapter> adapter = adapterService.findById(
                ClearingAdapterId.of(adapterId));
        
        return adapter.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Get BankservAfrica adapters by tenant ID
     */
    @GetMapping("/tenant/{tenantId}")
    @Operation(summary = "Get adapters by tenant", description = "Get BankservAfrica adapters by tenant ID")
    public ResponseEntity<List<BankservAfricaAdapter>> getBankservAfricaAdaptersByTenant(
            @PathVariable @Parameter(description = "Tenant ID") String tenantId) {
        
        log.info("Getting BankservAfrica adapters for tenant: {}", tenantId);
        
        List<BankservAfricaAdapter> adapters = adapterService.findByTenantId(tenantId);
        
        return ResponseEntity.ok(adapters);
    }
    
    /**
     * Get BankservAfrica adapters by tenant ID and business unit
     */
    @GetMapping("/tenant/{tenantId}/business-unit/{businessUnitId}")
    @Operation(summary = "Get adapters by tenant and business unit", description = "Get BankservAfrica adapters by tenant ID and business unit")
    public ResponseEntity<List<BankservAfricaAdapter>> getBankservAfricaAdaptersByTenantAndBusinessUnit(
            @PathVariable @Parameter(description = "Tenant ID") String tenantId,
            @PathVariable @Parameter(description = "Business Unit ID") String businessUnitId) {
        
        log.info("Getting BankservAfrica adapters for tenant: {} and business unit: {}", tenantId, businessUnitId);
        
        List<BankservAfricaAdapter> adapters = adapterService.findByTenantIdAndBusinessUnitId(tenantId, businessUnitId);
        
        return ResponseEntity.ok(adapters);
    }
    
    /**
     * Get active BankservAfrica adapters by tenant ID
     */
    @GetMapping("/tenant/{tenantId}/active")
    @Operation(summary = "Get active adapters by tenant", description = "Get active BankservAfrica adapters by tenant ID")
    public ResponseEntity<List<BankservAfricaAdapter>> getActiveBankservAfricaAdaptersByTenant(
            @PathVariable @Parameter(description = "Tenant ID") String tenantId) {
        
        log.info("Getting active BankservAfrica adapters for tenant: {}", tenantId);
        
        List<BankservAfricaAdapter> adapters = adapterService.findActiveByTenantId(tenantId);
        
        return ResponseEntity.ok(adapters);
    }
    
    /**
     * Get BankservAfrica adapters by network
     */
    @GetMapping("/network/{network}")
    @Operation(summary = "Get adapters by network", description = "Get BankservAfrica adapters by network")
    public ResponseEntity<List<BankservAfricaAdapter>> getBankservAfricaAdaptersByNetwork(
            @PathVariable @Parameter(description = "Network") String network) {
        
        log.info("Getting BankservAfrica adapters for network: {}", network);
        
        List<BankservAfricaAdapter> adapters = adapterService.findByNetwork(network);
        
        return ResponseEntity.ok(adapters);
    }
    
    /**
     * Get BankservAfrica adapters by status
     */
    @GetMapping("/status/{status}")
    @Operation(summary = "Get adapters by status", description = "Get BankservAfrica adapters by status")
    public ResponseEntity<List<BankservAfricaAdapter>> getBankservAfricaAdaptersByStatus(
            @PathVariable @Parameter(description = "Status") String status) {
        
        log.info("Getting BankservAfrica adapters for status: {}", status);
        
        List<BankservAfricaAdapter> adapters = adapterService.findByStatus(status);
        
        return ResponseEntity.ok(adapters);
    }
    
    // Request DTOs
    public static class CreateBankservAfricaAdapterRequest {
        private String tenantId;
        private String tenantName;
        private String businessUnitId;
        private String businessUnitName;
        private String adapterName;
        private ClearingNetwork network;
        private String endpoint;
        private String createdBy;
        
        // Getters and setters
        public String getTenantId() { return tenantId; }
        public void setTenantId(String tenantId) { this.tenantId = tenantId; }
        public String getTenantName() { return tenantName; }
        public void setTenantName(String tenantName) { this.tenantName = tenantName; }
        public String getBusinessUnitId() { return businessUnitId; }
        public void setBusinessUnitId(String businessUnitId) { this.businessUnitId = businessUnitId; }
        public String getBusinessUnitName() { return businessUnitName; }
        public void setBusinessUnitName(String businessUnitName) { this.businessUnitName = businessUnitName; }
        public String getAdapterName() { return adapterName; }
        public void setAdapterName(String adapterName) { this.adapterName = adapterName; }
        public ClearingNetwork getNetwork() { return network; }
        public void setNetwork(ClearingNetwork network) { this.network = network; }
        public String getEndpoint() { return endpoint; }
        public void setEndpoint(String endpoint) { this.endpoint = endpoint; }
        public String getCreatedBy() { return createdBy; }
        public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    }
    
    public static class UpdateBankservAfricaAdapterConfigurationRequest {
        private String endpoint;
        private String apiVersion;
        private Integer timeoutSeconds;
        private Integer retryAttempts;
        private Boolean encryptionEnabled;
        private Integer batchSize;
        private String processingWindowStart;
        private String processingWindowEnd;
        private String updatedBy;
        
        // Getters and setters
        public String getEndpoint() { return endpoint; }
        public void setEndpoint(String endpoint) { this.endpoint = endpoint; }
        public String getApiVersion() { return apiVersion; }
        public void setApiVersion(String apiVersion) { this.apiVersion = apiVersion; }
        public Integer getTimeoutSeconds() { return timeoutSeconds; }
        public void setTimeoutSeconds(Integer timeoutSeconds) { this.timeoutSeconds = timeoutSeconds; }
        public Integer getRetryAttempts() { return retryAttempts; }
        public void setRetryAttempts(Integer retryAttempts) { this.retryAttempts = retryAttempts; }
        public Boolean getEncryptionEnabled() { return encryptionEnabled; }
        public void setEncryptionEnabled(Boolean encryptionEnabled) { this.encryptionEnabled = encryptionEnabled; }
        public Integer getBatchSize() { return batchSize; }
        public void setBatchSize(Integer batchSize) { this.batchSize = batchSize; }
        public String getProcessingWindowStart() { return processingWindowStart; }
        public void setProcessingWindowStart(String processingWindowStart) { this.processingWindowStart = processingWindowStart; }
        public String getProcessingWindowEnd() { return processingWindowEnd; }
        public void setProcessingWindowEnd(String processingWindowEnd) { this.processingWindowEnd = processingWindowEnd; }
        public String getUpdatedBy() { return updatedBy; }
        public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }
    }
    
    public static class ActivateBankservAfricaAdapterRequest {
        private String activatedBy;
        
        // Getters and setters
        public String getActivatedBy() { return activatedBy; }
        public void setActivatedBy(String activatedBy) { this.activatedBy = activatedBy; }
    }
    
    public static class DeactivateBankservAfricaAdapterRequest {
        private String reason;
        private String deactivatedBy;
        
        // Getters and setters
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
        public String getDeactivatedBy() { return deactivatedBy; }
        public void setDeactivatedBy(String deactivatedBy) { this.deactivatedBy = deactivatedBy; }
    }
}
