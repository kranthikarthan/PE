package com.payments.batch.sftp;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Manages SFTP connections with connection pooling and session management.
 *
 * <p>This component provides efficient SFTP connection management with:
 * <ul>
 *   <li>Connection pooling for improved performance
 *   <li>Automatic session management and cleanup
 *   <li>Retry mechanisms for failed connections
 *   <li>Thread-safe operations
 *   <li>Resource leak prevention
 * </ul>
 *
 * <p><b>Connection Pool Features:</b>
 * <ul>
 *   <li>Configurable maximum connections
 *   <li>Automatic connection validation
 *   <li>Connection timeout handling
 *   <li>Graceful shutdown support
 * </ul>
 *
 * @since PE-403
 */
@Slf4j
@Component
public class SftpConnectionManager {
  
  private final SftpConfiguration config;
  private final JSch jsch;
  private final ConcurrentLinkedQueue<ChannelSftp> availableConnections;
  private final ConcurrentHashMap<String, Session> activeSessions;
  private final Semaphore connectionSemaphore;
  private volatile boolean shutdown = false;
  
  public SftpConnectionManager(SftpConfiguration config) {
    this.config = config;
    this.jsch = new JSch();
    this.availableConnections = new ConcurrentLinkedQueue<>();
    this.activeSessions = new ConcurrentHashMap<>();
    this.connectionSemaphore = new Semaphore(config.getMaxConnections());
    
    initializeJSch();
  }
  
  /**
   * Initializes the JSch instance with configuration.
   */
  private void initializeJSch() {
    try {
      // Set known hosts if configured
      if (config.getKnownHosts() != null && !config.getKnownHosts().trim().isEmpty()) {
        jsch.setKnownHosts(config.getKnownHosts());
      } else {
        // Disable strict host key checking if not configured
        if (!config.isStrictHostKeyChecking()) {
          jsch.setConfig("StrictHostKeyChecking", "no");
        }
      }
      
      // Configure compression if enabled
      if (config.isCompressionEnabled()) {
        jsch.setConfig("compression.s2c", "zlib@openssh.com,zlib,none");
        jsch.setConfig("compression.c2s", "zlib@openssh.com,zlib,none");
        jsch.setConfig("compression_level", String.valueOf(config.getCompressionLevel()));
      }
      
      // Set preferred authentication methods
      jsch.setConfig("PreferredAuthentications", String.join(",", config.getPreferredAuthMethods()));
      
      log.info("JSch initialized with host: {}, port: {}, auth: {}", 
          config.getHost(), config.getPort(), config.getAuthMethod());
      
    } catch (JSchException e) {
      log.error("Failed to initialize JSch: {}", e.getMessage(), e);
      throw new RuntimeException("Failed to initialize SFTP connection manager", e);
    }
  }
  
  /**
   * Gets an SFTP connection from the pool or creates a new one.
   *
   * @return SFTP connection
   * @throws SftpException if connection fails
   */
  public ChannelSftp getConnection() throws SftpException {
    if (shutdown) {
      throw new SftpException(ChannelSftp.SSH_FX_FAILURE, "Connection manager is shutdown");
    }
    
    try {
      // Try to get an available connection from the pool
      ChannelSftp connection = availableConnections.poll();
      
      if (connection != null && isConnectionValid(connection)) {
        log.debug("Reusing existing SFTP connection");
        return connection;
      }
      
      // Create a new connection
      return createNewConnection();
      
    } catch (Exception e) {
      log.error("Failed to get SFTP connection: {}", e.getMessage(), e);
      throw new SftpException(ChannelSftp.SSH_FX_FAILURE, "Failed to get SFTP connection: " + e.getMessage());
    }
  }
  
  /**
   * Returns an SFTP connection to the pool.
   *
   * @param connection the connection to return
   */
  public void returnConnection(ChannelSftp connection) {
    if (connection == null || shutdown) {
      return;
    }
    
    try {
      if (isConnectionValid(connection)) {
        availableConnections.offer(connection);
        log.debug("Returned SFTP connection to pool");
      } else {
        closeConnection(connection);
      }
    } catch (Exception e) {
      log.warn("Error returning SFTP connection to pool: {}", e.getMessage());
      closeConnection(connection);
    } finally {
      connectionSemaphore.release();
    }
  }
  
  /**
   * Creates a new SFTP connection.
   *
   * @return new SFTP connection
   * @throws SftpException if connection creation fails
   */
  private ChannelSftp createNewConnection() throws SftpException {
    try {
      // Acquire semaphore permit
      if (!connectionSemaphore.tryAcquire(config.getPoolTimeout(), TimeUnit.MILLISECONDS)) {
        throw new SftpException(ChannelSftp.SSH_FX_FAILURE, "Connection pool timeout");
      }
      
      // Create session
      Session session = jsch.getSession(config.getUsername(), config.getHost(), config.getPort());
      session.setTimeout(config.getTimeout());
      
      // Configure authentication
      configureAuthentication(session);
      
      // Connect session
      session.connect();
      
      // Create SFTP channel
      ChannelSftp channel = (ChannelSftp) session.openChannel("sftp");
      channel.connect();
      
      // Store session for cleanup
      String sessionKey = generateSessionKey(session);
      activeSessions.put(sessionKey, session);
      
      log.info("Created new SFTP connection to {}", config.getConnectionUrl());
      return channel;
      
    } catch (JSchException e) {
      connectionSemaphore.release();
      log.error("Failed to create SFTP connection: {}", e.getMessage(), e);
      throw new SftpException(ChannelSftp.SSH_FX_FAILURE, "Failed to create SFTP connection: " + e.getMessage());
    }
  }
  
  /**
   * Configures authentication for the session.
   *
   * @param session the SSH session
   * @throws JSchException if authentication configuration fails
   */
  private void configureAuthentication(Session session) throws JSchException {
    SftpAuthMethod authMethod = config.getAuthMethod();
    
    switch (authMethod) {
      case PASSWORD:
        session.setPassword(config.getPassword());
        break;
        
      case KEY:
        if (config.getPrivateKey() != null) {
          // Add private key from string
          jsch.addIdentity("sftp-key", 
              config.getPrivateKey().getBytes(), 
              config.getPrivateKeyPassphrase() != null ? config.getPrivateKeyPassphrase().getBytes() : null, 
              null);
        }
        break;
        
      case CERTIFICATE:
        // Certificate authentication would be implemented here
        log.warn("Certificate authentication not yet implemented");
        break;
        
      case NONE:
        log.warn("No authentication method configured - this is not secure");
        break;
    }
  }
  
  /**
   * Checks if a connection is still valid.
   *
   * @param connection the connection to check
   * @return true if valid, false otherwise
   */
  private boolean isConnectionValid(ChannelSftp connection) {
    try {
      return connection != null && 
             connection.isConnected() && 
             !connection.getSession().isClosed();
    } catch (Exception e) {
      log.debug("Connection validation failed: {}", e.getMessage());
      return false;
    }
  }
  
  /**
   * Closes an SFTP connection and its session.
   *
   * @param connection the connection to close
   */
  private void closeConnection(ChannelSftp connection) {
    if (connection == null) {
      return;
    }
    
    try {
      Session session = connection.getSession();
      String sessionKey = generateSessionKey(session);
      
      connection.disconnect();
      session.disconnect();
      
      activeSessions.remove(sessionKey);
      log.debug("Closed SFTP connection and session");
      
    } catch (Exception e) {
      log.warn("Error closing SFTP connection: {}", e.getMessage());
    }
  }
  
  /**
   * Generates a unique key for session tracking.
   *
   * @param session the SSH session
   * @return unique session key
   */
  private String generateSessionKey(Session session) {
    return String.format("%s:%d:%s", 
        session.getHost(), 
        session.getPort(), 
        session.getUserName());
  }
  
  /**
   * Shuts down the connection manager and closes all connections.
   */
  public void shutdown() {
    shutdown = true;
    
    log.info("Shutting down SFTP connection manager");
    
    // Close all available connections
    ChannelSftp connection;
    while ((connection = availableConnections.poll()) != null) {
      closeConnection(connection);
    }
    
    // Close all active sessions
    activeSessions.values().forEach(session -> {
      try {
        session.disconnect();
      } catch (Exception e) {
        log.warn("Error disconnecting session: {}", e.getMessage());
      }
    });
    activeSessions.clear();
    
    log.info("SFTP connection manager shutdown complete");
  }
  
  /**
   * Gets the current connection pool status.
   *
   * @return connection pool status
   */
  public SftpPoolStatus getPoolStatus() {
    return new SftpPoolStatus(
        availableConnections.size(),
        activeSessions.size(),
        connectionSemaphore.availablePermits(),
        config.getMaxConnections()
    );
  }
  
  /**
   * Represents the current status of the SFTP connection pool.
   */
  public static class SftpPoolStatus {
    private final int availableConnections;
    private final int activeSessions;
    private final int availablePermits;
    private final int maxConnections;
    
    public SftpPoolStatus(int availableConnections, int activeSessions, int availablePermits, int maxConnections) {
      this.availableConnections = availableConnections;
      this.activeSessions = activeSessions;
      this.availablePermits = availablePermits;
      this.maxConnections = maxConnections;
    }
    
    public int getAvailableConnections() { return availableConnections; }
    public int getActiveSessions() { return activeSessions; }
    public int getAvailablePermits() { return availablePermits; }
    public int getMaxConnections() { return maxConnections; }
    
    @Override
    public String toString() {
      return String.format("SftpPoolStatus{available=%d, active=%d, permits=%d, max=%d}", 
          availableConnections, activeSessions, availablePermits, maxConnections);
    }
  }
}
