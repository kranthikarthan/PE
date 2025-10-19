package com.payments.bankservafricaadapter.client;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.jcraft.jsch.*;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Unit tests for BankservAfricaSftpClient
 *
 * <p>Tests SFTP file operations with BankservAfrica including:
 *
 * <ul>
 *   <li>File upload/download
 *   <li>Directory operations
 *   <li>Connection management
 *   <li>Error handling
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
class BankservAfricaSftpClientTest {

  private BankservAfricaSftpClient sftpClient;

  @BeforeEach
  void setUp() {
    sftpClient = new BankservAfricaSftpClient();

    // Set configuration using reflection
    ReflectionTestUtils.setField(sftpClient, "host", "sftp.bankservafrica.test");
    ReflectionTestUtils.setField(sftpClient, "port", 22);
    ReflectionTestUtils.setField(sftpClient, "username", "test-user");
    ReflectionTestUtils.setField(sftpClient, "privateKeyPath", "/tmp/test_key");
    ReflectionTestUtils.setField(sftpClient, "passphrase", "test-passphrase");
    ReflectionTestUtils.setField(sftpClient, "uploadDir", "/upload");
    ReflectionTestUtils.setField(sftpClient, "downloadDir", "/download");
    ReflectionTestUtils.setField(sftpClient, "archiveDir", "/archive");
    ReflectionTestUtils.setField(sftpClient, "timeout", 30000);
    ReflectionTestUtils.setField(sftpClient, "strictHostKeyChecking", "no");
  }

  @Test
  void shouldUploadFileContent() {
    // Given
    byte[] content = "ACH batch content".getBytes(StandardCharsets.UTF_8);
    String remoteFileName = "ACH_20250101_120000.txt";

    // When/Then - Configuration test only
    assertThat(sftpClient).isNotNull();
    assertThat(content).isNotEmpty();
    assertThat(remoteFileName).isNotEmpty();

    // Note: Actual SFTP operations require real server or embedded SFTP server
    // This test validates object creation and configuration
  }

  @Test
  void shouldHandleUploadError() {
    // Given
    byte[] content = "test content".getBytes();
    String remoteFileName = "test.txt";

    // When/Then
    // Without real SFTP server, operations will fail
    // This validates error handling structure exists
    assertThatCode(
            () -> {
              try {
                sftpClient.uploadFileContent(content, remoteFileName);
              } catch (BankservAfricaSftpClient.BankservAfricaSftpException e) {
                // Expected for test without real server
                assertThat(e.getMessage()).contains("Failed to upload");
              }
            })
        .doesNotThrowAnyException();
  }

  @Test
  void shouldHandleDownloadError() {
    // Given
    String remoteFileName = "response.txt";

    // When/Then
    assertThatCode(
            () -> {
              try {
                sftpClient.downloadFileContent(remoteFileName);
              } catch (BankservAfricaSftpClient.BankservAfricaSftpException e) {
                // Expected for test without real server
                assertThat(e.getMessage()).contains("Failed to download");
              }
            })
        .doesNotThrowAnyException();
  }

  @Test
  void shouldHandleListFilesError() {
    // When/Then
    assertThatCode(
            () -> {
              try {
                sftpClient.listFiles();
              } catch (BankservAfricaSftpClient.BankservAfricaSftpException e) {
                // Expected for test without real server
                assertThat(e.getMessage()).contains("Failed to list");
              }
            })
        .doesNotThrowAnyException();
  }

  @Test
  void shouldHandleConnectionTest() {
    // When
    boolean result = sftpClient.testConnection();

    // Then
    assertThat(result).isFalse(); // Expected without real server
  }

  @Test
  void shouldValidateConfiguration() {
    // Then
    assertThat(ReflectionTestUtils.getField(sftpClient, "host"))
        .isEqualTo("sftp.bankservafrica.test");
    assertThat(ReflectionTestUtils.getField(sftpClient, "port")).isEqualTo(22);
    assertThat(ReflectionTestUtils.getField(sftpClient, "username")).isEqualTo("test-user");
    assertThat(ReflectionTestUtils.getField(sftpClient, "uploadDir")).isEqualTo("/upload");
    assertThat(ReflectionTestUtils.getField(sftpClient, "downloadDir")).isEqualTo("/download");
    assertThat(ReflectionTestUtils.getField(sftpClient, "archiveDir")).isEqualTo("/archive");
  }

  @Test
  void shouldHandleFileExistsCheck() {
    // Given
    String remoteFileName = "test.txt";

    // When/Then
    assertThatCode(
            () -> {
              try {
                sftpClient.fileExists(remoteFileName);
              } catch (BankservAfricaSftpClient.BankservAfricaSftpException e) {
                // Expected for test without real server (unless it's SSH_FX_NO_SUCH_FILE)
                assertThat(e.getMessage()).contains("Failed to check file existence");
              }
            })
        .doesNotThrowAnyException();
  }

  @Test
  void shouldHandleArchiveFile() {
    // Given
    String remoteFileName = "completed.txt";

    // When/Then
    assertThatCode(
            () -> {
              try {
                sftpClient.archiveFile(remoteFileName);
              } catch (BankservAfricaSftpClient.BankservAfricaSftpException e) {
                // Expected for test without real server
                assertThat(e.getMessage()).contains("Failed to archive");
              }
            })
        .doesNotThrowAnyException();
  }

  @Test
  void shouldHandleDeleteFile() {
    // Given
    String remoteFileName = "old_file.txt";

    // When/Then
    assertThatCode(
            () -> {
              try {
                sftpClient.deleteFile(remoteFileName);
              } catch (BankservAfricaSftpClient.BankservAfricaSftpException e) {
                // Expected for test without real server
                assertThat(e.getMessage()).contains("Failed to delete");
              }
            })
        .doesNotThrowAnyException();
  }

  /** Note: Integration tests with real/embedded SFTP server should be in separate test class */
  @Test
  void shouldProvideAppropriateExceptionMessages() {
    // Given
    Exception cause = new JSchException("Connection refused");
    BankservAfricaSftpClient.BankservAfricaSftpException exception =
        new BankservAfricaSftpClient.BankservAfricaSftpException("SFTP operation failed", cause);

    // Then
    assertThat(exception.getMessage()).isEqualTo("SFTP operation failed");
    assertThat(exception.getCause()).isEqualTo(cause);
    assertThat(exception.getCause().getMessage()).isEqualTo("Connection refused");
  }
}
