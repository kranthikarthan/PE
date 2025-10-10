package com.paymentengine.paymentprocessing.repository;

import com.paymentengine.paymentprocessing.entity.ClearingSystemAuthConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ClearingSystemAuthConfigurationRepository extends JpaRepository<ClearingSystemAuthConfiguration, UUID> {
    
    List<ClearingSystemAuthConfiguration> findByEnvironment(String environment);
    
    Optional<ClearingSystemAuthConfiguration> findByEnvironmentAndIsActive(String environment, Boolean isActive);
    
    List<ClearingSystemAuthConfiguration> findByIsActive(Boolean isActive);
    
    Optional<ClearingSystemAuthConfiguration> findByEnvironmentAndAuthMethodAndIsActive(
        String environment, ClearingSystemAuthConfiguration.AuthMethod authMethod, Boolean isActive);
}