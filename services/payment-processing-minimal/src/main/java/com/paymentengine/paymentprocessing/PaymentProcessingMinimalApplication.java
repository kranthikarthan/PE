package com.paymentengine.paymentprocessing;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

/**
 * Minimal Payment Processing Service Application
 * 
 * Focuses on ISO 20022 message processing without complex dependencies
 */
@SpringBootApplication(exclude = {
    DataSourceAutoConfiguration.class,
    HibernateJpaAutoConfiguration.class
})
@ComponentScan(basePackages = {
    "com.paymentengine.paymentprocessing"
})
public class PaymentProcessingMinimalApplication {

    public static void main(String[] args) {
        SpringApplication.run(PaymentProcessingMinimalApplication.class, args);
    }
}