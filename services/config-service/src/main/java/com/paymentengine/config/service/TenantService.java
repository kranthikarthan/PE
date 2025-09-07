package com.paymentengine.config.service;

import com.paymentengine.config.entity.Tenant;
import com.paymentengine.config.entity.ConfigurationHistory;
import com.paymentengine.config.repository.TenantRepository;
import com.paymentengine.config.repository.ConfigurationHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class TenantService {
    
    @Autowired
    private TenantRepository tenantRepository;
    
    @Autowired
    private ConfigurationHistoryRepository configurationHistoryRepository;
    
    @Cacheable(value = "tenants", key = "#id")
    public Optional<Tenant> findById(UUID id) {
        return tenantRepository.findById(id);
    }
    
    @Cacheable(value = "tenants", key = "#code")
    public Optional<Tenant> findByCode(String code) {
        return tenantRepository.findByCode(code);
    }
    
    @Cacheable(value = "tenants", key = "'all'")
    public List<Tenant> findAll() {
        return tenantRepository.findAll();
    }
    
    @Cacheable(value = "tenants", key = "#status")
    public List<Tenant> findByStatus(Tenant.TenantStatus status) {
        return tenantRepository.findByStatus(status);
    }
    
    @Cacheable(value = "tenants", key = "#email")
    public Optional<Tenant> findByContactEmail(String email) {
        return tenantRepository.findByContactEmail(email);
    }
    
    public Tenant createTenant(String name, String code, String contactEmail, String contactPhone, String address) {
        if (tenantRepository.existsByCode(code)) {
            throw new RuntimeException("Tenant with code " + code + " already exists");
        }
        
        if (contactEmail != null && tenantRepository.existsByContactEmail(contactEmail)) {
            throw new RuntimeException("Tenant with email " + contactEmail + " already exists");
        }
        
        Tenant tenant = new Tenant();
        tenant.setName(name);
        tenant.setCode(code);
        tenant.setContactEmail(contactEmail);
        tenant.setContactPhone(contactPhone);
        tenant.setAddress(address);
        tenant.setStatus(Tenant.TenantStatus.ACTIVE);
        tenant.setCreatedAt(LocalDateTime.now());
        
        Tenant savedTenant = tenantRepository.save(tenant);
        
        // Record in history
        ConfigurationHistory history = new ConfigurationHistory(
                ConfigurationHistory.ConfigType.TENANT,
                savedTenant.getId(),
                "tenant_created",
                null,
                savedTenant.toString(),
                "Tenant created",
                "system"
        );
        configurationHistoryRepository.save(history);
        
        return savedTenant;
    }
    
    @CacheEvict(value = "tenants", allEntries = true)
    public Tenant updateTenant(UUID id, String name, String contactEmail, String contactPhone, String address) {
        Tenant tenant = tenantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tenant not found"));
        
        String oldValue = tenant.toString();
        
        tenant.setName(name);
        tenant.setContactEmail(contactEmail);
        tenant.setContactPhone(contactPhone);
        tenant.setAddress(address);
        tenant.setUpdatedAt(LocalDateTime.now());
        
        Tenant savedTenant = tenantRepository.save(tenant);
        
        // Record in history
        ConfigurationHistory history = new ConfigurationHistory(
                ConfigurationHistory.ConfigType.TENANT,
                savedTenant.getId(),
                "tenant_updated",
                oldValue,
                savedTenant.toString(),
                "Tenant updated",
                "system"
        );
        configurationHistoryRepository.save(history);
        
        return savedTenant;
    }
    
    @CacheEvict(value = "tenants", allEntries = true)
    public void activateTenant(UUID id) {
        Tenant tenant = tenantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tenant not found"));
        
        String oldValue = tenant.getStatus().toString();
        tenant.setStatus(Tenant.TenantStatus.ACTIVE);
        tenant.setUpdatedAt(LocalDateTime.now());
        
        tenantRepository.save(tenant);
        
        // Record in history
        ConfigurationHistory history = new ConfigurationHistory(
                ConfigurationHistory.ConfigType.TENANT,
                tenant.getId(),
                "status",
                oldValue,
                tenant.getStatus().toString(),
                "Tenant activated",
                "system"
        );
        configurationHistoryRepository.save(history);
    }
    
    @CacheEvict(value = "tenants", allEntries = true)
    public void deactivateTenant(UUID id) {
        Tenant tenant = tenantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tenant not found"));
        
        String oldValue = tenant.getStatus().toString();
        tenant.setStatus(Tenant.TenantStatus.INACTIVE);
        tenant.setUpdatedAt(LocalDateTime.now());
        
        tenantRepository.save(tenant);
        
        // Record in history
        ConfigurationHistory history = new ConfigurationHistory(
                ConfigurationHistory.ConfigType.TENANT,
                tenant.getId(),
                "status",
                oldValue,
                tenant.getStatus().toString(),
                "Tenant deactivated",
                "system"
        );
        configurationHistoryRepository.save(history);
    }
    
    @CacheEvict(value = "tenants", allEntries = true)
    public void suspendTenant(UUID id) {
        Tenant tenant = tenantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tenant not found"));
        
        String oldValue = tenant.getStatus().toString();
        tenant.setStatus(Tenant.TenantStatus.SUSPENDED);
        tenant.setUpdatedAt(LocalDateTime.now());
        
        tenantRepository.save(tenant);
        
        // Record in history
        ConfigurationHistory history = new ConfigurationHistory(
                ConfigurationHistory.ConfigType.TENANT,
                tenant.getId(),
                "status",
                oldValue,
                tenant.getStatus().toString(),
                "Tenant suspended",
                "system"
        );
        configurationHistoryRepository.save(history);
    }
    
    @CacheEvict(value = "tenants", allEntries = true)
    public void deleteTenant(UUID id) {
        Tenant tenant = tenantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tenant not found"));
        
        tenantRepository.delete(tenant);
        
        // Record in history
        ConfigurationHistory history = new ConfigurationHistory(
                ConfigurationHistory.ConfigType.TENANT,
                tenant.getId(),
                "tenant_deleted",
                tenant.toString(),
                null,
                "Tenant deleted",
                "system"
        );
        configurationHistoryRepository.save(history);
    }
    
    public Long countByStatus(Tenant.TenantStatus status) {
        return tenantRepository.countByStatus(status);
    }
    
    public List<Tenant> findByNameContaining(String name) {
        return tenantRepository.findByNameContaining(name);
    }
    
    public List<Tenant> findByCodeContaining(String code) {
        return tenantRepository.findByCodeContaining(code);
    }
    
    public List<Tenant> findByCreatedAtAfter(LocalDateTime date) {
        return tenantRepository.findByCreatedAtAfter(date);
    }
    
    public boolean existsByCode(String code) {
        return tenantRepository.existsByCode(code);
    }
    
    public boolean existsByContactEmail(String email) {
        return tenantRepository.existsByContactEmail(email);
    }
}