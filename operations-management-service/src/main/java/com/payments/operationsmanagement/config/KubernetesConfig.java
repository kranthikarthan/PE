package com.payments.operationsmanagement.config;

import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.util.Config;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

/**
 * Kubernetes Configuration
 * 
 * Configures Kubernetes client for operations management functionality.
 * Provides access to Kubernetes API for pod management and service discovery.
 */
@Configuration
@Slf4j
public class KubernetesConfig {

    @Value("${app.kubernetes.namespace:payments}")
    private String namespace;

    @Value("${app.kubernetes.config-path:}")
    private String configPath;

    /**
     * Kubernetes API client
     */
    @Bean
    public ApiClient kubernetesApiClient() {
        try {
            ApiClient client;
            
            if (configPath != null && !configPath.isEmpty()) {
                // Use custom config path
                client = Config.fromFile(configPath);
            } else {
                // Use default config (in-cluster or kubeconfig)
                client = Config.defaultClient();
            }
            
            // Set timeout values
            client.setConnectTimeout(10000);
            client.setReadTimeout(30000);
            client.setWriteTimeout(30000);
            
            log.info("Kubernetes API client configured for namespace: {}", namespace);
            return client;
            
        } catch (IOException e) {
            log.error("Failed to configure Kubernetes API client", e);
            throw new RuntimeException("Failed to configure Kubernetes API client", e);
        }
    }

    /**
     * Core V1 API
     */
    @Bean
    public CoreV1Api coreV1Api(ApiClient apiClient) {
        return new CoreV1Api(apiClient);
    }

    /**
     * Rest template for HTTP calls
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
