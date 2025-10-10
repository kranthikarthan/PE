package com.paymentengine.paymentprocessing.repository;

import com.paymentengine.paymentprocessing.entity.PaymentTypeAuthConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentTypeAuthConfigurationRepository extends JpaRepository<PaymentTypeAuthConfiguration, UUID> {
    
    List<PaymentTypeAuthConfiguration> findByTenantId(String tenantId);
    
    List<PaymentTypeAuthConfiguration> findByPaymentType(String paymentType);
    
    List<PaymentTypeAuthConfiguration> findByTenantIdAndPaymentType(String tenantId, String paymentType);
    
    Optional<PaymentTypeAuthConfiguration> findByTenantIdAndPaymentTypeAndIsActive(
        String tenantId, String paymentType, Boolean isActive);
    
    List<PaymentTypeAuthConfiguration> findByTenantIdAndIsActive(String tenantId, Boolean isActive);
    
    List<PaymentTypeAuthConfiguration> findByPaymentTypeAndIsActive(String paymentType, Boolean isActive);
    
    List<PaymentTypeAuthConfiguration> findByIsActive(Boolean isActive);
    
    Optional<PaymentTypeAuthConfiguration> findByTenantIdAndPaymentTypeAndAuthMethodAndIsActive(
        String tenantId, String paymentType, PaymentTypeAuthConfiguration.AuthMethod authMethod, Boolean isActive);
    
    List<PaymentTypeAuthConfiguration> findByClearingSystem(String clearingSystem);
    
    List<PaymentTypeAuthConfiguration> findByCurrency(String currency);
    
    List<PaymentTypeAuthConfiguration> findByIsHighValue(Boolean isHighValue);
}