package com.payments.paymentinitiation.config;

import org.springframework.context.annotation.Configuration;

/**
 * Repository Configuration
 *
 * <p>Configures repository adapters and ports following the Ports and Adapters pattern
 */
@Configuration
public class RepositoryConfig {

  // Adapters are annotated with @Component and picked up via component scanning.
  // Explicit @Bean definitions removed to avoid duplicate beans of the same port type.
}
