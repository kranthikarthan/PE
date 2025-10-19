package com.payments.batch.sftp;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for SftpConfiguration.
 *
 * @since PE-403
 */
@DisplayName("SFTP Configuration Tests")
class SftpConfigurationTest {
  
  private SftpConfiguration config;
  
  @BeforeEach
  void setUp() {
    config = new SftpConfiguration();
  }
  
  @Test
  @DisplayName("Should have default values")
  void shouldHaveDefaultValues() {
    assertEquals(22, config.getPort());
    assertEquals(30000, config.getTimeout());
    assertEquals(30000, config.getSocketTimeout());
    assertEquals(3, config.getRetryAttempts());
    assertEquals(1000, config.getRetryDelay());
    assertEquals(5, config.getMaxConnections());
    assertEquals(60000, config.getPoolTimeout());
    assertFalse(config.isCompressionEnabled());
    assertEquals(6, config.getCompressionLevel());
    assertTrue(config.isStrictHostKeyChecking());
    assertEquals(100 * 1024 * 1024, config.getMaxFileSize());
    assertTrue(config.isCreateDirectories());
    assertEquals("644", config.getFilePermissions());
    assertEquals("755", config.getDirectoryPermissions());
  }
  
  @Test
  @DisplayName("Should detect password authentication method")
  void shouldDetectPasswordAuthenticationMethod() {
    // Given
    config.setPassword("testpassword");
    
    // When
    SftpAuthMethod authMethod = config.getAuthMethod();
    
    // Then
    assertEquals(SftpAuthMethod.PASSWORD, authMethod);
  }
  
  @Test
  @DisplayName("Should detect key authentication method")
  void shouldDetectKeyAuthenticationMethod() {
    // Given
    config.setPrivateKey("-----BEGIN PRIVATE KEY-----");
    
    // When
    SftpAuthMethod authMethod = config.getAuthMethod();
    
    // Then
    assertEquals(SftpAuthMethod.KEY, authMethod);
  }
  
  @Test
  @DisplayName("Should prefer key authentication over password")
  void shouldPreferKeyAuthenticationOverPassword() {
    // Given
    config.setPassword("testpassword");
    config.setPrivateKey("-----BEGIN PRIVATE KEY-----");
    
    // When
    SftpAuthMethod authMethod = config.getAuthMethod();
    
    // Then
    assertEquals(SftpAuthMethod.KEY, authMethod);
  }
  
  @Test
  @DisplayName("Should detect no authentication method")
  void shouldDetectNoAuthenticationMethod() {
    // When
    SftpAuthMethod authMethod = config.getAuthMethod();
    
    // Then
    assertEquals(SftpAuthMethod.NONE, authMethod);
  }
  
  @Test
  @DisplayName("Should validate configuration with required fields")
  void shouldValidateConfigurationWithRequiredFields() {
    // Given
    config.setHost("sftp.example.com");
    config.setUsername("testuser");
    config.setPort(22);
    config.setTimeout(30000);
    config.setRetryAttempts(3);
    config.setMaxConnections(5);
    
    // When
    boolean isValid = config.isValid();
    
    // Then
    assertTrue(isValid);
  }
  
  @Test
  @DisplayName("Should invalidate configuration with missing host")
  void shouldInvalidateConfigurationWithMissingHost() {
    // Given
    config.setUsername("testuser");
    config.setPort(22);
    config.setTimeout(30000);
    config.setRetryAttempts(3);
    config.setMaxConnections(5);
    
    // When
    boolean isValid = config.isValid();
    
    // Then
    assertFalse(isValid);
  }
  
  @Test
  @DisplayName("Should invalidate configuration with missing username")
  void shouldInvalidateConfigurationWithMissingUsername() {
    // Given
    config.setHost("sftp.example.com");
    config.setPort(22);
    config.setTimeout(30000);
    config.setRetryAttempts(3);
    config.setMaxConnections(5);
    
    // When
    boolean isValid = config.isValid();
    
    // Then
    assertFalse(isValid);
  }
  
  @Test
  @DisplayName("Should invalidate configuration with invalid port")
  void shouldInvalidateConfigurationWithInvalidPort() {
    // Given
    config.setHost("sftp.example.com");
    config.setUsername("testuser");
    config.setPort(0);
    config.setTimeout(30000);
    config.setRetryAttempts(3);
    config.setMaxConnections(5);
    
    // When
    boolean isValid = config.isValid();
    
    // Then
    assertFalse(isValid);
  }
  
  @Test
  @DisplayName("Should invalidate configuration with invalid timeout")
  void shouldInvalidateConfigurationWithInvalidTimeout() {
    // Given
    config.setHost("sftp.example.com");
    config.setUsername("testuser");
    config.setPort(22);
    config.setTimeout(0);
    config.setRetryAttempts(3);
    config.setMaxConnections(5);
    
    // When
    boolean isValid = config.isValid();
    
    // Then
    assertFalse(isValid);
  }
  
  @Test
  @DisplayName("Should invalidate configuration with negative retry attempts")
  void shouldInvalidateConfigurationWithNegativeRetryAttempts() {
    // Given
    config.setHost("sftp.example.com");
    config.setUsername("testuser");
    config.setPort(22);
    config.setTimeout(30000);
    config.setRetryAttempts(-1);
    config.setMaxConnections(5);
    
    // When
    boolean isValid = config.isValid();
    
    // Then
    assertFalse(isValid);
  }
  
  @Test
  @DisplayName("Should invalidate configuration with zero max connections")
  void shouldInvalidateConfigurationWithZeroMaxConnections() {
    // Given
    config.setHost("sftp.example.com");
    config.setUsername("testuser");
    config.setPort(22);
    config.setTimeout(30000);
    config.setRetryAttempts(3);
    config.setMaxConnections(0);
    
    // When
    boolean isValid = config.isValid();
    
    // Then
    assertFalse(isValid);
  }
  
  @Test
  @DisplayName("Should generate connection URL")
  void shouldGenerateConnectionUrl() {
    // Given
    config.setHost("sftp.example.com");
    config.setUsername("testuser");
    config.setPort(22);
    
    // When
    String connectionUrl = config.getConnectionUrl();
    
    // Then
    assertEquals("sftp://testuser@sftp.example.com:22", connectionUrl);
  }
  
  @Test
  @DisplayName("Should generate connection URL with custom port")
  void shouldGenerateConnectionUrlWithCustomPort() {
    // Given
    config.setHost("sftp.example.com");
    config.setUsername("testuser");
    config.setPort(2222);
    
    // When
    String connectionUrl = config.getConnectionUrl();
    
    // Then
    assertEquals("sftp://testuser@sftp.example.com:2222", connectionUrl);
  }
}
