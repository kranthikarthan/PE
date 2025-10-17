package com.payments.samosadapter.client;

import feign.Logger;
import feign.Request;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * SAMOS Clearing Network Client Configuration
 *
 * <p>Feign client configuration for SAMOS clearing network: - Request interceptors - Logging
 * configuration - Timeout configuration - Error handling
 */
@Slf4j
@Configuration
public class SamosClearingNetworkClientConfig {

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
        template.header("User-Agent", "samos-adapter-service/1.0.0");
        template.header("X-Service-Name", "samos-adapter");
        template.header("X-Service-Version", "1.0.0");
        log.debug("Applied SAMOS request interceptor to template: {}", template.url());
      }
    };
  }

  /** Request options for timeout configuration */
  @Bean
  public Request.Options requestOptions() {
    return new Request.Options(
        15, // Connect timeout (longer for clearing network)
        TimeUnit.SECONDS,
        60, // Read timeout (longer for clearing network processing)
        TimeUnit.SECONDS,
        true // Follow redirects
        );
  }
}
