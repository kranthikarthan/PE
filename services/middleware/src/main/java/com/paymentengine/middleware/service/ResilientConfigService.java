package com.paymentengine.middleware.service;

import com.paymentengine.middleware.dto.TenantConfigurationRequest;
import com.paymentengine.middleware.dto.TenantConfigurationResponse;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.timelimiter.TimeLimiter;
import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.ratelimiter.RateLimiter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

@Service
public class ResilientConfigService {
    
    @Autowired
    private CircuitBreaker configServiceCircuitBreaker;
    
    @Autowired
    private Retry configServiceRetry;
    
    @Autowired
    private TimeLimiter configServiceTimeLimiter;
    
    @Autowired
    private Bulkhead configServiceBulkhead;
    
    @Autowired
    private RateLimiter configServiceRateLimiter;
    
    @Autowired
    private RestTemplate restTemplate;
    
    private static final String CONFIG_SERVICE_URL = "http://config-service:8080/api/v1/config";
    
    public List<TenantConfigurationResponse> getTenantConfigurations(UUID tenantId) {
        Supplier<List<TenantConfigurationResponse>> supplier = () -> {
            return restTemplate.getForObject(
                    CONFIG_SERVICE_URL + "/tenants/" + tenantId + "/configurations", 
                    List.class
            );
        };
        
        Supplier<List<TenantConfigurationResponse>> decoratedSupplier = CircuitBreaker
                .decorateSupplier(configServiceCircuitBreaker, supplier);
        
        decoratedSupplier = Retry.decorateSupplier(configServiceRetry, decoratedSupplier);
        decoratedSupplier = TimeLimiter.decorateSupplier(configServiceTimeLimiter, decoratedSupplier);
        decoratedSupplier = Bulkhead.decorateSupplier(configServiceBulkhead, decoratedSupplier);
        decoratedSupplier = RateLimiter.decorateSupplier(configServiceRateLimiter, decoratedSupplier);
        
        return decoratedSupplier.get();
    }
    
    public TenantConfigurationResponse getTenantConfiguration(UUID tenantId, String configKey) {
        Supplier<TenantConfigurationResponse> supplier = () -> {
            return restTemplate.getForObject(
                    CONFIG_SERVICE_URL + "/tenants/" + tenantId + "/configurations/" + configKey, 
                    TenantConfigurationResponse.class
            );
        };
        
        Supplier<TenantConfigurationResponse> decoratedSupplier = CircuitBreaker
                .decorateSupplier(configServiceCircuitBreaker, supplier);
        
        decoratedSupplier = Retry.decorateSupplier(configServiceRetry, decoratedSupplier);
        decoratedSupplier = TimeLimiter.decorateSupplier(configServiceTimeLimiter, decoratedSupplier);
        decoratedSupplier = Bulkhead.decorateSupplier(configServiceBulkhead, decoratedSupplier);
        decoratedSupplier = RateLimiter.decorateSupplier(configServiceRateLimiter, decoratedSupplier);
        
        return decoratedSupplier.get();
    }
    
    public TenantConfigurationResponse setTenantConfiguration(UUID tenantId, TenantConfigurationRequest request) {
        Supplier<TenantConfigurationResponse> supplier = () -> {
            return restTemplate.postForObject(
                    CONFIG_SERVICE_URL + "/tenants/" + tenantId + "/configurations", 
                    request, 
                    TenantConfigurationResponse.class
            );
        };
        
        Supplier<TenantConfigurationResponse> decoratedSupplier = CircuitBreaker
                .decorateSupplier(configServiceCircuitBreaker, supplier);
        
        decoratedSupplier = Retry.decorateSupplier(configServiceRetry, decoratedSupplier);
        decoratedSupplier = TimeLimiter.decorateSupplier(configServiceTimeLimiter, decoratedSupplier);
        decoratedSupplier = Bulkhead.decorateSupplier(configServiceBulkhead, decoratedSupplier);
        decoratedSupplier = RateLimiter.decorateSupplier(configServiceRateLimiter, decoratedSupplier);
        
        return decoratedSupplier.get();
    }
    
    public void deleteTenantConfiguration(UUID tenantId, String configKey) {
        Supplier<Void> supplier = () -> {
            restTemplate.delete(
                    CONFIG_SERVICE_URL + "/tenants/" + tenantId + "/configurations/" + configKey
            );
            return null;
        };
        
        Supplier<Void> decoratedSupplier = CircuitBreaker
                .decorateSupplier(configServiceCircuitBreaker, supplier);
        
        decoratedSupplier = Retry.decorateSupplier(configServiceRetry, decoratedSupplier);
        decoratedSupplier = TimeLimiter.decorateSupplier(configServiceTimeLimiter, decoratedSupplier);
        decoratedSupplier = Bulkhead.decorateSupplier(configServiceBulkhead, decoratedSupplier);
        decoratedSupplier = RateLimiter.decorateSupplier(configServiceRateLimiter, decoratedSupplier);
        
        decoratedSupplier.get();
    }
    
    public String getStringValue(UUID tenantId, String configKey, String defaultValue) {
        try {
            TenantConfigurationResponse config = getTenantConfiguration(tenantId, configKey);
            return config != null ? config.getConfigValue() : defaultValue;
        } catch (Exception e) {
            return defaultValue;
        }
    }
    
    public Integer getIntegerValue(UUID tenantId, String configKey, Integer defaultValue) {
        try {
            TenantConfigurationResponse config = getTenantConfiguration(tenantId, configKey);
            if (config != null && config.getConfigValue() != null) {
                return Integer.parseInt(config.getConfigValue());
            }
            return defaultValue;
        } catch (Exception e) {
            return defaultValue;
        }
    }
    
    public Boolean getBooleanValue(UUID tenantId, String configKey, Boolean defaultValue) {
        try {
            TenantConfigurationResponse config = getTenantConfiguration(tenantId, configKey);
            if (config != null && config.getConfigValue() != null) {
                return Boolean.parseBoolean(config.getConfigValue());
            }
            return defaultValue;
        } catch (Exception e) {
            return defaultValue;
        }
    }
}