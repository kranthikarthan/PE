package com.payments.bankservafricaadapter.client;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Unit tests for BankservAfricaBatchFileService
 *
 * <p>Tests batch file orchestration including:
 *
 * <ul>
 *   <li>Upload with encryption
 *   <li>Download with decryption
 *   <li>File listing
 *   <li>Configuration testing
 *   <li>Error handling
 *   <li>Resilience patterns
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
class BankservAfricaBatchFileServiceTest {

  @Mock private BankservAfricaSftpClient sftpClient;

  @Mock private BankservAfricaPgpService pgpService;

  private BankservAfricaBatchFileService batchFileService;

  @BeforeEach
  void setUp() {
    batchFileService = new BankservAfricaBatchFileService(sftpClient, pgpService);

    // Set test configuration
    ReflectionTestUtils.setField(batchFileService, "tempDir", "/tmp/test");
    ReflectionTestUtils.setField(batchFileService, "archiveDir", "/tmp/archive");
    ReflectionTestUtils.setField(batchFileService, "cleanupTempFiles", false); // Don't cleanup in
    // tests
  }

  @Test
  void shouldUploadBatchFileBytes() {
    // Given
    String tenantId = "BANK001";
    byte[] batchContent = "ACH batch data".getBytes(StandardCharsets.UTF_8);
    byte[] encryptedContent = "encrypted data".getBytes();

    when(pgpService.encrypt(batchContent)).thenReturn(encryptedContent);
    when(sftpClient.uploadFileContent(any(byte[].class), anyString())).thenReturn(true);

    // When
    String fileName = batchFileService.uploadBatchFileBytes(batchContent, tenantId);

    // Then
    assertThat(fileName).isNotNull();
    assertThat(fileName).startsWith("ACH_");
    assertThat(fileName).endsWith(".txt.pgp");
    assertThat(fileName).contains(tenantId);

    verify(pgpService).encrypt(batchContent);
    verify(sftpClient).uploadFileContent(encryptedContent, fileName);
  }

  @Test
  void shouldHandleUploadError() {
    // Given
    String tenantId = "BANK001";
    byte[] batchContent = "ACH batch data".getBytes();

    when(pgpService.encrypt(any(byte[].class)))
        .thenThrow(
            new BankservAfricaPgpService.BankservAfricaPgpException("Encryption failed", null));

    // When/Then
    assertThatThrownBy(() -> batchFileService.uploadBatchFileBytes(batchContent, tenantId))
        .isInstanceOf(BankservAfricaBatchFileService.BankservAfricaBatchFileException.class)
        .hasMessageContaining("Failed to upload batch file");
  }

  @Test
  void shouldDownloadBatchFileBytes() {
    // Given
    String remoteFileName = "EFT_20250101_120000.txt.pgp";
    byte[] encryptedContent = "encrypted response".getBytes();
    byte[] decryptedContent = "EFT response data".getBytes();

    when(sftpClient.downloadFileContent(remoteFileName)).thenReturn(encryptedContent);
    when(pgpService.decrypt(encryptedContent)).thenReturn(decryptedContent);
    when(sftpClient.archiveFile(remoteFileName)).thenReturn(true);

    // When
    byte[] result = batchFileService.downloadBatchFileBytes(remoteFileName);

    // Then
    assertThat(result).isEqualTo(decryptedContent);

    verify(sftpClient).downloadFileContent(remoteFileName);
    verify(pgpService).decrypt(encryptedContent);
    verify(sftpClient).archiveFile(remoteFileName);
  }

  @Test
  void shouldHandleDownloadError() {
    // Given
    String remoteFileName = "response.txt.pgp";

    when(sftpClient.downloadFileContent(remoteFileName))
        .thenThrow(
            new BankservAfricaSftpClient.BankservAfricaSftpException("Download failed", null));

    // When/Then
    assertThatThrownBy(() -> batchFileService.downloadBatchFileBytes(remoteFileName))
        .isInstanceOf(BankservAfricaBatchFileService.BankservAfricaBatchFileException.class)
        .hasMessageContaining("Failed to download batch file");
  }

  @Test
  void shouldListAvailableFiles() {
    // Given
    List<String> expectedFiles =
        Arrays.asList("EFT_20250101_120000.txt.pgp", "EFT_20250101_130000.txt.pgp");

    when(sftpClient.listFiles()).thenReturn(expectedFiles);

    // When
    List<String> result = batchFileService.listAvailableFiles();

    // Then
    assertThat(result).hasSize(2);
    assertThat(result).containsExactlyElementsOf(expectedFiles);

    verify(sftpClient).listFiles();
  }

  @Test
  void shouldHandleListFilesError() {
    // Given
    when(sftpClient.listFiles())
        .thenThrow(new BankservAfricaSftpClient.BankservAfricaSftpException("List failed", null));

    // When/Then
    assertThatThrownBy(() -> batchFileService.listAvailableFiles())
        .isInstanceOf(BankservAfricaBatchFileService.BankservAfricaBatchFileException.class)
        .hasMessageContaining("Failed to list batch files");
  }

  @Test
  void shouldCheckFileExists() {
    // Given
    String remoteFileName = "response.txt.pgp";
    when(sftpClient.fileExists(remoteFileName)).thenReturn(true);

    // When
    boolean exists = batchFileService.fileExists(remoteFileName);

    // Then
    assertThat(exists).isTrue();
    verify(sftpClient).fileExists(remoteFileName);
  }

  @Test
  void shouldTestConfiguration() {
    // Given
    when(sftpClient.testConnection()).thenReturn(true);
    when(pgpService.testConfiguration()).thenReturn(true);

    // When
    boolean result = batchFileService.testConfiguration();

    // Then
    assertThat(result).isTrue();
    verify(sftpClient).testConnection();
    verify(pgpService).testConfiguration();
  }

  @Test
  void shouldFailConfigurationTestIfSftpFails() {
    // Given
    when(sftpClient.testConnection()).thenReturn(false);

    // When
    boolean result = batchFileService.testConfiguration();

    // Then
    assertThat(result).isFalse();
    verify(sftpClient).testConnection();
    verify(pgpService, never()).testConfiguration();
  }

  @Test
  void shouldFailConfigurationTestIfPgpFails() {
    // Given
    when(sftpClient.testConnection()).thenReturn(true);
    when(pgpService.testConfiguration()).thenReturn(false);

    // When
    boolean result = batchFileService.testConfiguration();

    // Then
    assertThat(result).isFalse();
    verify(sftpClient).testConnection();
    verify(pgpService).testConfiguration();
  }

  @Test
  void shouldHandleConfigurationTestError() {
    // Given
    when(sftpClient.testConnection()).thenThrow(new RuntimeException("Connection error"));

    // When
    boolean result = batchFileService.testConfiguration();

    // Then
    assertThat(result).isFalse();
  }

  @Test
  void shouldGenerateCorrectFileNameFormat() {
    // Given
    String tenantId = "TENANT123";
    byte[] batchContent = "test".getBytes();
    byte[] encryptedContent = "encrypted".getBytes();

    when(pgpService.encrypt(any(byte[].class))).thenReturn(encryptedContent);
    when(sftpClient.uploadFileContent(any(byte[].class), anyString())).thenReturn(true);

    // When
    String fileName = batchFileService.uploadBatchFileBytes(batchContent, tenantId);

    // Then
    assertThat(fileName).matches("ACH_\\d{8}_\\d{6}_TENANT123\\.txt\\.pgp");
  }

  @Test
  void shouldHandleDecryptionError() {
    // Given
    String remoteFileName = "response.txt.pgp";
    byte[] encryptedContent = "encrypted".getBytes();

    when(sftpClient.downloadFileContent(remoteFileName)).thenReturn(encryptedContent);
    when(pgpService.decrypt(encryptedContent))
        .thenThrow(
            new BankservAfricaPgpService.BankservAfricaPgpException("Decryption failed", null));

    // When/Then
    assertThatThrownBy(() -> batchFileService.downloadBatchFileBytes(remoteFileName))
        .isInstanceOf(BankservAfricaBatchFileService.BankservAfricaBatchFileException.class)
        .hasMessageContaining("Failed to download batch file");
  }

  @Test
  void shouldProvideAppropriateExceptionMessages() {
    // Given
    Exception cause = new IOException("File system error");
    BankservAfricaBatchFileService.BankservAfricaBatchFileException exception =
        new BankservAfricaBatchFileService.BankservAfricaBatchFileException(
            "Batch operation failed", cause);

    // Then
    assertThat(exception.getMessage()).isEqualTo("Batch operation failed");
    assertThat(exception.getCause()).isEqualTo(cause);
    assertThat(exception.getCause().getMessage()).isEqualTo("File system error");
  }

  @Test
  void shouldHandleEmptyFileList() {
    // Given
    when(sftpClient.listFiles()).thenReturn(List.of());

    // When
    List<String> result = batchFileService.listAvailableFiles();

    // Then
    assertThat(result).isEmpty();
    verify(sftpClient).listFiles();
  }

  @Test
  void shouldArchiveFileAfterDownload() {
    // Given
    String remoteFileName = "EFT_20250101_120000.txt.pgp";
    byte[] encryptedContent = "encrypted".getBytes();
    byte[] decryptedContent = "decrypted".getBytes();

    when(sftpClient.downloadFileContent(remoteFileName)).thenReturn(encryptedContent);
    when(pgpService.decrypt(encryptedContent)).thenReturn(decryptedContent);
    when(sftpClient.archiveFile(remoteFileName)).thenReturn(true);

    // When
    batchFileService.downloadBatchFileBytes(remoteFileName);

    // Then
    verify(sftpClient).archiveFile(remoteFileName);
  }
}
