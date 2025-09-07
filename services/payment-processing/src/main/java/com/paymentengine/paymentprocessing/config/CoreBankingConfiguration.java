package com.paymentengine.paymentprocessing.config;

import com.paymentengine.paymentprocessing.service.CoreBankingAdapter;
import com.paymentengine.paymentprocessing.service.impl.RestCoreBankingAdapter;
import com.paymentengine.paymentprocessing.service.impl.GrpcCoreBankingAdapter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.client.RestTemplate;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableAsync;

import java.time.Duration;

/**
 * Core Banking Configuration
 * 
 * Configures core banking adapters and related beans for the payment engine.
 * Supports both REST API and gRPC adapters for external core banking integration.
 */
@Configuration
@EnableRetry
@EnableAsync
public class CoreBankingConfiguration {
    
    @Value("${core-banking.rest.base-url:http://localhost:8081}")
    private String restBaseUrl;
    
    @Value("${core-banking.rest.timeout:30000}")
    private int restTimeout;
    
    @Value("${core-banking.rest.retry-attempts:3}")
    private int restRetryAttempts;
    
    @Value("${core-banking.grpc.host:localhost}")
    private String grpcHost;
    
    @Value("${core-banking.grpc.port:9090}")
    private int grpcPort;
    
    @Value("${core-banking.grpc.timeout:30000}")
    private int grpcTimeout;
    
    @Value("${core-banking.grpc.retry-attempts:3}")
    private int grpcRetryAttempts;
    
    /**
     * RestTemplate bean for REST API calls
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
    
    /**
     * REST Core Banking Adapter
     */
    @Bean("restCoreBankingAdapter")
    public CoreBankingAdapter restCoreBankingAdapter(RestTemplate restTemplate) {
        RestCoreBankingAdapter adapter = new RestCoreBankingAdapter();
        // Set properties via reflection or constructor injection
        return adapter;
    }
    
    /**
     * gRPC Core Banking Adapter
     */
    @Bean("grpcCoreBankingAdapter")
    public CoreBankingAdapter grpcCoreBankingAdapter() {
        GrpcCoreBankingAdapter adapter = new GrpcCoreBankingAdapter();
        // Set properties via reflection or constructor injection
        return adapter;
    }
    
    /**
     * Primary Core Banking Adapter (defaults to REST)
     */
    @Bean
    @Primary
    public CoreBankingAdapter primaryCoreBankingAdapter(@Qualifier("restCoreBankingAdapter") CoreBankingAdapter restAdapter) {
        return restAdapter;
    }
}