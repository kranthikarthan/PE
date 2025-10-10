package com.paymentengine.discovery.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "service_registries")
public class ServiceRegistry {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "service_name", nullable = false, length = 100, unique = true)
    private String serviceName;

    @Column(name = "service_type", nullable = false, length = 50)
    private String serviceType; // MICROSERVICE, GATEWAY, DATABASE, MESSAGE_QUEUE, etc.

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "version", length = 20)
    private String version;

    @Column(name = "health_check_interval", nullable = false)
    private Integer healthCheckInterval; // in seconds

    @Column(name = "heartbeat_timeout", nullable = false)
    private Integer heartbeatTimeout; // in seconds

    @Column(name = "max_instances")
    private Integer maxInstances;

    @Column(name = "min_instances")
    private Integer minInstances;

    @Column(name = "load_balancing_strategy", length = 50)
    private String loadBalancingStrategy; // ROUND_ROBIN, LEAST_CONNECTIONS, RANDOM, etc.

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Constructors
    public ServiceRegistry() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.healthCheckInterval = 30;
        this.heartbeatTimeout = 90;
        this.minInstances = 1;
        this.maxInstances = 10;
        this.loadBalancingStrategy = "ROUND_ROBIN";
    }

    public ServiceRegistry(String serviceName, String serviceType) {
        this();
        this.serviceName = serviceName;
        this.serviceType = serviceType;
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Integer getHealthCheckInterval() {
        return healthCheckInterval;
    }

    public void setHealthCheckInterval(Integer healthCheckInterval) {
        this.healthCheckInterval = healthCheckInterval;
    }

    public Integer getHeartbeatTimeout() {
        return heartbeatTimeout;
    }

    public void setHeartbeatTimeout(Integer heartbeatTimeout) {
        this.heartbeatTimeout = heartbeatTimeout;
    }

    public Integer getMaxInstances() {
        return maxInstances;
    }

    public void setMaxInstances(Integer maxInstances) {
        this.maxInstances = maxInstances;
    }

    public Integer getMinInstances() {
        return minInstances;
    }

    public void setMinInstances(Integer minInstances) {
        this.minInstances = minInstances;
    }

    public String getLoadBalancingStrategy() {
        return loadBalancingStrategy;
    }

    public void setLoadBalancingStrategy(String loadBalancingStrategy) {
        this.loadBalancingStrategy = loadBalancingStrategy;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}