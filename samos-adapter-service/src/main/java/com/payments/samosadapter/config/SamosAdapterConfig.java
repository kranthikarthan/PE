package com.payments.samosadapter.config;

import java.util.List;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * SAMOS Adapter Configuration
 *
 * <p>Configuration properties for SAMOS adapter service including endpoint settings, ISO 20022
 * message types, and resilience patterns.
 */
@Configuration
@ConfigurationProperties(prefix = "samos.adapter")
@Data
public class SamosAdapterConfig {

  /** SAMOS endpoint configuration */
  private String endpoint;

  private String apiVersion;
  private Integer timeoutSeconds;
  private Integer retryAttempts;
  private Boolean encryptionEnabled;
  private String certificatePath;
  private String certificatePassword;

  /** ISO 20022 message configuration */
  private Iso20022Config iso20022 = new Iso20022Config();

  @Data
  public static class Iso20022Config {
    private String namespace = "urn:iso:std:iso:20022:tech:xsd";
    private String version = "2013-05-01";
    private List<String> messageTypes;
  }
}
