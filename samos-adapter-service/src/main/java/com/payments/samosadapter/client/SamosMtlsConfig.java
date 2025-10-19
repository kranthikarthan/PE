package com.payments.samosadapter.client;

import feign.Client;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Date;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * SAMOS mTLS Configuration
 *
 * <p>Configures Mutual TLS (mTLS) for secure communication with SARB's SAMOS clearing network.
 *
 * <p>Requirements:
 *
 * <ul>
 *   <li>Client certificate issued by SARB (X.509)
 *   <li>SARB CA certificate for server validation
 *   <li>Secure keystore (PKCS12 or JKS format)
 *   <li>Certificate expiry monitoring
 *   <li>TLS 1.2+ protocol enforcement
 * </ul>
 *
 * <p>Configuration properties:
 *
 * <pre>
 * samos.mtls.enabled=true
 * samos.mtls.keystore.path=file:/path/to/client-keystore.p12
 * samos.mtls.keystore.password=changeit
 * samos.mtls.keystore.type=PKCS12
 * samos.mtls.key.alias=samos-client
 * samos.mtls.key.password=changeit
 * samos.mtls.truststore.path=file:/path/to/truststore.jks
 * samos.mtls.truststore.password=changeit
 * samos.mtls.truststore.type=JKS
 * samos.mtls.protocol=TLSv1.3
 * </pre>
 */
@Configuration
@ConditionalOnProperty(prefix = "samos.mtls", name = "enabled", havingValue = "true")
@Slf4j
public class SamosMtlsConfig {

  @Value("${samos.mtls.keystore.path}")
  private String keystorePath;

  @Value("${samos.mtls.keystore.password}")
  private String keystorePassword;

  @Value("${samos.mtls.keystore.type:PKCS12}")
  private String keystoreType;

  @Value("${samos.mtls.key.alias}")
  private String keyAlias;

  @Value("${samos.mtls.key.password}")
  private String keyPassword;

  @Value("${samos.mtls.truststore.path}")
  private String truststorePath;

  @Value("${samos.mtls.truststore.password}")
  private String truststorePassword;

  @Value("${samos.mtls.truststore.type:JKS}")
  private String truststoreType;

  @Value("${samos.mtls.protocol:TLSv1.3}")
  private String tlsProtocol;

  @Value("${samos.mtls.cert.expiry.warning.days:30}")
  private int expiryWarningDays;

  /**
   * Create Feign Client with mTLS support
   *
   * @return Feign Client configured with mTLS
   */
  @Bean
  public Client feignClient() {
    try {
      log.info("Initializing SAMOS mTLS Feign client...");

      // Load client keystore (contains our certificate + private key)
      KeyStore keyStore = loadKeyStore(keystorePath, keystorePassword, keystoreType);

      // Load truststore (contains SARB CA certificates)
      KeyStore trustStore = loadKeyStore(truststorePath, truststorePassword, truststoreType);

      // Check certificate expiry
      checkCertificateExpiry(keyStore);

      // Initialize Key Manager (for client authentication)
      KeyManagerFactory keyManagerFactory =
          KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
      keyManagerFactory.init(keyStore, keyPassword.toCharArray());

      // Initialize Trust Manager (for server validation)
      TrustManagerFactory trustManagerFactory =
          TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
      trustManagerFactory.init(trustStore);

      // Create SSL Context with mTLS
      SSLContext sslContext = SSLContext.getInstance(tlsProtocol);
      sslContext.init(
          keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), null);

      log.info(
          "SAMOS mTLS configured successfully. Protocol: {}, Key Alias: {}", tlsProtocol, keyAlias);

      // Create Feign Client with custom SSL Context
      return new Client.Default(sslContext.getSocketFactory(), null);

    } catch (Exception e) {
      log.error("Failed to configure SAMOS mTLS: {}", e.getMessage(), e);
      throw new SamosMtlsConfigurationException("Failed to configure mTLS", e);
    }
  }

  /**
   * Load KeyStore from file
   *
   * @param path Keystore file path
   * @param password Keystore password
   * @param type Keystore type (PKCS12, JKS, etc.)
   * @return Loaded KeyStore
   */
  private KeyStore loadKeyStore(String path, String password, String type)
      throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException {

    log.debug("Loading keystore from: {} (type: {})", path, type);

    KeyStore keyStore = KeyStore.getInstance(type);

    // Handle different path formats
    String filePath = path.startsWith("file:") ? path.substring(5) : path;

    try (FileInputStream fis = new FileInputStream(filePath)) {
      keyStore.load(fis, password.toCharArray());
    }

    log.debug("Keystore loaded successfully. Aliases: {}", keyStore.aliases().asIterator());

    return keyStore;
  }

  /**
   * Check certificate expiry and log warnings
   *
   * @param keyStore KeyStore containing certificates
   */
  private void checkCertificateExpiry(KeyStore keyStore) {
    try {
      X509Certificate cert = (X509Certificate) keyStore.getCertificate(keyAlias);

      if (cert == null) {
        log.warn("Certificate not found with alias: {}", keyAlias);
        return;
      }

      Date expiryDate = cert.getNotAfter();
      Date now = new Date();

      long daysUntilExpiry = (expiryDate.getTime() - now.getTime()) / (1000 * 60 * 60 * 24);

      log.info(
          "SAMOS Client Certificate - Subject: {}, Issuer: {}, Expires: {} ({} days)",
          cert.getSubjectX500Principal().getName(),
          cert.getIssuerX500Principal().getName(),
          expiryDate,
          daysUntilExpiry);

      if (daysUntilExpiry < 0) {
        log.error("SAMOS client certificate has EXPIRED! Expiry date: {}", expiryDate);
        throw new SamosMtlsConfigurationException(
            "SAMOS client certificate expired on: " + expiryDate);
      } else if (daysUntilExpiry <= expiryWarningDays) {
        log.warn(
            "SAMOS client certificate expires in {} days! Expiry date: {}. RENEW IMMEDIATELY!",
            daysUntilExpiry,
            expiryDate);
      } else if (daysUntilExpiry <= 90) {
        log.warn(
            "SAMOS client certificate expires in {} days. Expiry date: {}. Plan renewal.",
            daysUntilExpiry,
            expiryDate);
      }

    } catch (KeyStoreException e) {
      log.error("Failed to check certificate expiry: {}", e.getMessage(), e);
    }
  }

  /**
   * Get certificate information for monitoring
   *
   * @return Certificate information
   */
  @Bean
  public SamosCertificateInfo certificateInfo() {
    try {
      KeyStore keyStore = loadKeyStore(keystorePath, keystorePassword, keystoreType);
      X509Certificate cert = (X509Certificate) keyStore.getCertificate(keyAlias);

      if (cert == null) {
        log.warn("Certificate not found for monitoring: {}", keyAlias);
        return SamosCertificateInfo.notFound(keyAlias);
      }

      return SamosCertificateInfo.builder()
          .alias(keyAlias)
          .subject(cert.getSubjectX500Principal().getName())
          .issuer(cert.getIssuerX500Principal().getName())
          .serialNumber(cert.getSerialNumber().toString())
          .notBefore(cert.getNotBefore())
          .notAfter(cert.getNotAfter())
          .signatureAlgorithm(cert.getSigAlgName())
          .version(cert.getVersion())
          .build();

    } catch (Exception e) {
      log.error("Failed to load certificate info: {}", e.getMessage(), e);
      return SamosCertificateInfo.error(keyAlias, e.getMessage());
    }
  }

  /** Custom exception for mTLS configuration errors */
  public static class SamosMtlsConfigurationException extends RuntimeException {
    public SamosMtlsConfigurationException(String message) {
      super(message);
    }

    public SamosMtlsConfigurationException(String message, Throwable cause) {
      super(message, cause);
    }
  }
}
