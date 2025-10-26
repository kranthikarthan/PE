package com.payments.paymentinitiation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

/**
 * Payment Initiation Service Application - Simple Test Version
 *
 * <p>Provides REST API for testing with: - Health checks - pain.001 test endpoints
 */
@SpringBootApplication(
    exclude = {
        DataSourceAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class,
        JpaRepositoriesAutoConfiguration.class,
        RedisRepositoriesAutoConfiguration.class
    }
)
@ComponentScan(
    basePackages = "com.payments.paymentinitiation.api",
    includeFilters = @ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        classes = {
            com.payments.paymentinitiation.api.HealthController.class,
            com.payments.paymentinitiation.api.Pain001TestController.class
        }
    )
)
public class PaymentInitiationServiceApplication {

  public static void main(String[] args) {
    SpringApplication.run(PaymentInitiationServiceApplication.class, args);
  }
}
