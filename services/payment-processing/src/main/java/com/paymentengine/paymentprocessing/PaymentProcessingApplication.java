package com.paymentengine.paymentprocessing;

import com.paymentengine.paymentprocessing.config.FeatureToggleProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

/**
 * Payment Processing Service Application
 * 
 * Provides core payment processing capabilities:
 * - Payment transaction processing
 * - ISO20022 message handling
 * - Scheme interactions and routing
 * - Certificate management
 * - Payment validation and authorization
 * - Transaction monitoring and reporting
 */
@SpringBootApplication(
    scanBasePackages = {
        "com.paymentengine.paymentprocessing",
        "com.paymentengine.shared"
    },
    exclude = {
        RedisAutoConfiguration.class,
        KafkaAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class
    }
)
@ComponentScan(basePackages = {
    "com.paymentengine.paymentprocessing.controller",
    "com.paymentengine.shared"
})
@EnableFeignClients
@EnableKafka
@EnableAsync
@EnableScheduling
@EnableConfigurationProperties(FeatureToggleProperties.class)
public class PaymentProcessingApplication {

    public static void main(String[] args) {
        SpringApplication.run(PaymentProcessingApplication.class, args);
    }
}
