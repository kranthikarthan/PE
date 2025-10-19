package com.payments.batch.sftp;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties for SFTP connections.
 *
 * <p>This configuration supports multiple SFTP servers with different authentication
 * methods including password-based and key-based authentication. It provides
 * connection pooling, timeout settings, and retry mechanisms for reliable file transfers.
 *
 * <p><b>Supported Authentication Methods:</b>
 * <ul>
 *   <li>Password authentication
 *   <li>SSH key authentication (RSA, DSA, ECDSA)
 *   <li>Certificate-based authentication
 * </ul>
 *
 * <p><b>Configuration Properties:</b>
 * <ul>
 *   <li><b>host</b>: SFTP server hostname or IP address
 *   <li><b>port</b>: SFTP server port (default: 22)
 *   <li><b>username</b>: SFTP username
 *   <li><b>password</b>: SFTP password (if using password auth)
 *   <li><b>privateKey</b>: SSH private key content (if using key auth)
 *   <li><b>knownHosts</b>: Known hosts file path
 *   <li><b>timeout</b>: Connection timeout in milliseconds
 *   <li><b>retryAttempts</b>: Number of retry attempts for failed operations
 * </ul>
 *
 * @since PE-403
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "batch.sftp")
public class SftpConfiguration {
  
  /** SFTP server hostname or IP address */
  private String host;
  
  /** SFTP server port (default: 22) */
  private int port = 22;
  
  /** SFTP username */
  private String username;
  
  /** SFTP password (for password authentication) */
  private String password;
  
  /** SSH private key content (for key authentication) */
  private String privateKey;
  
  /** SSH private key passphrase */
  private String privateKeyPassphrase;
  
  /** Known hosts file path */
  private String knownHosts;
  
  /** Connection timeout in milliseconds (default: 30000) */
  private int timeout = 30000;
  
  /** Socket timeout in milliseconds (default: 30000) */
  private int socketTimeout = 30000;
  
  /** Number of retry attempts for failed operations (default: 3) */
  private int retryAttempts = 3;
  
  /** Retry delay in milliseconds (default: 1000) */
  private long retryDelay = 1000;
  
  /** Maximum number of concurrent connections (default: 5) */
  private int maxConnections = 5;
  
  /** Connection pool timeout in milliseconds (default: 60000) */
  private int poolTimeout = 60000;
  
  /** Whether to use compression (default: false) */
  private boolean compressionEnabled = false;
  
  /** Compression level (1-9, default: 6) */
  private int compressionLevel = 6;
  
  /** Whether to verify host key (default: true) */
  private boolean strictHostKeyChecking = true;
  
  /** Preferred authentication methods */
  private String[] preferredAuthMethods = {"publickey", "password"};
  
  /** Maximum file size for uploads in bytes (default: 100MB) */
  private long maxFileSize = 100 * 1024 * 1024;
  
  /** Temporary directory for file operations */
  private String tempDirectory = System.getProperty("java.io.tmpdir");
  
  /** Whether to create directories if they don't exist */
  private boolean createDirectories = true;
  
  /** File permissions for uploaded files (octal, default: 644) */
  private String filePermissions = "644";
  
  /** Directory permissions for created directories (octal, default: 755) */
  private String directoryPermissions = "755";
  
  /**
   * Gets the authentication method based on available credentials.
   *
   * @return the authentication method to use
   */
  public SftpAuthMethod getAuthMethod() {
    if (privateKey != null && !privateKey.trim().isEmpty()) {
      return SftpAuthMethod.KEY;
    } else if (password != null && !password.trim().isEmpty()) {
      return SftpAuthMethod.PASSWORD;
    } else {
      return SftpAuthMethod.NONE;
    }
  }
  
  /**
   * Validates the SFTP configuration.
   *
   * @return true if configuration is valid, false otherwise
   */
  public boolean isValid() {
    return host != null && !host.trim().isEmpty() &&
           username != null && !username.trim().isEmpty() &&
           port > 0 && port <= 65535 &&
           timeout > 0 &&
           retryAttempts >= 0 &&
           maxConnections > 0;
  }
  
  /**
   * Gets the connection URL for logging purposes.
   *
   * @return the connection URL (without credentials)
   */
  public String getConnectionUrl() {
    return String.format("sftp://%s@%s:%d", username, host, port);
  }
}
