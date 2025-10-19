package com.payments.swiftadapter.client;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;
import javax.net.ssl.SSLContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.io.HttpClientConnectionManager;
import org.apache.hc.client5.http.ssl.HttpsSupport;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.core5.ssl.SSLContextBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * SWIFT Network Client Configuration
 *
 * <p>Configures secure mTLS connection to SWIFT Alliance Gateway for international payment
 * messaging.
 *
 * <p>Features:
 *
 * <ul>
 *   <li>mTLS (Mutual TLS) authentication
 *   <li>Certificate-based authentication
 *   <li>Connection pooling
 *   <li>Timeout configuration
 *   <li>SSL/TLS 1.3 support
 * </ul>
 *
 * <p>Configuration:
 *
 * <pre>
 * swift.network.base-url=https://swift-gateway.example.com/api/v1
 * swift.network.keystore-path=/etc/swift/certs/client.p12
 * swift.network.keystore-password=${SWIFT_KEYSTORE_PASSWORD}
 * swift.network.truststore-path=/etc/swift/certs/truststore.jks
 * swift.network.truststore-password=${SWIFT_TRUSTSTORE_PASSWORD}
 * swift.network.connect-timeout=10000
 * swift.network.read-timeout=30000
 * swift.network.max-connections=50
 * </pre>
 */
@Configuration
@Slf4j
public class SwiftNetworkClientConfig {

  @Value("${swift.network.base-url}")
  private String baseUrl;

  @Value("${swift.network.keystore-path}")
  private String keystorePath;

  @Value("${swift.network.keystore-password}")
  private String keystorePassword;

  @Value("${swift.network.truststore-path}")
  private String truststorePath;

  @Value("${swift.network.truststore-password}")
  private String truststorePassword;

  @Value("${swift.network.connect-timeout:10000}")
  private int connectTimeout;

  @Value("${swift.network.read-timeout:30000}")
  private int readTimeout;

  @Value("${swift.network.max-connections:50}")
  private int maxConnections;

  /**
   * Create RestTemplate with mTLS configuration
   *
   * @return Configured RestTemplate
   */
  @Bean(name = "swiftNetworkRestTemplate")
  public RestTemplate swiftNetworkRestTemplate()
      throws KeyStoreException,
          IOException,
          CertificateException,
          NoSuchAlgorithmException,
          UnrecoverableKeyException,
          KeyManagementException {

    log.info("Initializing SWIFT Network Client with mTLS");

    // Load keystore (client certificate)
    KeyStore keyStore = KeyStore.getInstance("PKCS12");
    try (FileInputStream keystoreInputStream = new FileInputStream(keystorePath)) {
      keyStore.load(keystoreInputStream, keystorePassword.toCharArray());
    }

    // Load truststore (trusted CAs)
    KeyStore trustStore = KeyStore.getInstance("JKS");
    try (FileInputStream truststoreInputStream = new FileInputStream(truststorePath)) {
      trustStore.load(truststoreInputStream, truststorePassword.toCharArray());
    }

    // Create SSL context with mTLS
    SSLContext sslContext =
        SSLContextBuilder.create()
            .loadKeyMaterial(keyStore, keystorePassword.toCharArray())
            .loadTrustMaterial(trustStore, null)
            .build();

    // Create SSL socket factory
    SSLConnectionSocketFactory sslSocketFactory =
        new SSLConnectionSocketFactory(
            sslContext,
            new String[] {"TLSv1.3", "TLSv1.2"},
            null,
            HttpsSupport.getDefaultHostnameVerifier());

    // Create connection manager with SSL
    HttpClientConnectionManager connectionManager =
        PoolingHttpClientConnectionManagerBuilder.create()
            .setSSLSocketFactory(sslSocketFactory)
            .setMaxConnTotal(maxConnections)
            .setMaxConnPerRoute(maxConnections / 2)
            .build();

    // Create HTTP client
    CloseableHttpClient httpClient =
        HttpClients.custom().setConnectionManager(connectionManager).build();

    // Create request factory
    HttpComponentsClientHttpRequestFactory requestFactory =
        new HttpComponentsClientHttpRequestFactory(httpClient);
    requestFactory.setConnectTimeout(connectTimeout);
    requestFactory.setConnectionRequestTimeout(connectTimeout);

    log.info(
        "SWIFT Network Client initialized: {} (connect: {}ms, read: {}ms)",
        baseUrl,
        connectTimeout,
        readTimeout);

    return new RestTemplate(requestFactory);
  }

  /** Get base URL for SWIFT network */
  public String getBaseUrl() {
    return baseUrl;
  }
}
