package com.payments.accountadapter.client;

import feign.Logger;
import feign.Request;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Account Service Client Configuration
 *
 * <p>Feign client configuration for account service: - Request interceptors - Logging configuration
 * - Timeout configuration - Error handling
 */
@Slf4j
@Configuration
public class AccountServiceClientConfig {

  /** Feign logging level */
  @Bean
  public Logger.Level feignLoggerLevel() {
    return Logger.Level.BASIC;
  }

  /** Request interceptor for adding common headers */
  @Bean
  public RequestInterceptor requestInterceptor() {
    return new RequestInterceptor() {
      @Override
      public void apply(RequestTemplate template) {
        template.header("Content-Type", "application/json");
        template.header("Accept", "application/json");
        template.header("User-Agent", "account-adapter-service/1.0.0");
        log.debug("Applied request interceptor to template: {}", template.url());
      }
    };
  }

  /** Request options for timeout configuration */
  @Bean
  public Request.Options requestOptions() {
    return new Request.Options(
        10,
        TimeUnit.SECONDS, // Connect timeout
        30,
        TimeUnit.SECONDS, // Read timeout
        true // Follow redirects
        );
  }
}
