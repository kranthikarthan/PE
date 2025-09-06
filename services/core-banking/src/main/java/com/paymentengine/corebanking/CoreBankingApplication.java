package com.paymentengine.corebanking;

import com.paymentengine.shared.tenant.TenantInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Core Banking Service Application
 * 
 * Provides transaction processing, account management, and payment processing
 * capabilities for the Payment Engine system.
 * 
 * Now includes multi-tenant support with tenant context management.
 */
@SpringBootApplication(scanBasePackages = {
    "com.paymentengine.corebanking",
    "com.paymentengine.shared"
})
@EnableKafka
@EnableCaching
@EnableAsync
@EnableTransactionManagement
public class CoreBankingApplication implements WebMvcConfigurer {

    @Autowired
    private TenantInterceptor tenantInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(tenantInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/v1/health", "/api/v1/metrics");
    }

    public static void main(String[] args) {
        SpringApplication.run(CoreBankingApplication.class, args);
    }
}