package com.payments.bankservafricaadapter.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * BankservAfrica Batch File Service
 *
 * <p>Orchestrates secure file transfer with BankservAfrica using SFTP + PGP encryption for ACH/EFT
 * batch processing.
 *
 * <p>Features:
 *
 * <ul>
 *   <li>Automated batch file upload with PGP encryption
 *   <li>Automated batch file download with PGP decryption
 *   <li>File naming conventions (ACH/EFT)
 *   <li>Temporary file management
 *   <li>Automatic cleanup and archival
 *   <li>Resilience patterns (circuit breaker, retry)
 * </ul>
 *
 * <p>File Naming Conventions:
 *
 * <pre>
 * Upload: ACH_YYYYMMDD_HHMMSS_TENANT.pgp
 * Download: EFT_YYYYMMDD_HHMMSS_*.pgp
 * </pre>
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class BankservAfricaBatchFileService {

  private static final DateTimeFormatter FILE_TIMESTAMP_FORMAT =
      DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

  private final BankservAfricaSftpClient sftpClient;
  private final BankservAfricaPgpService pgpService;

  @Value("${bankservafrica.batch.temp-dir:/tmp/bankservafrica}")
  private String tempDir;

  @Value("${bankservafrica.batch.archive-dir:/var/bankservafrica/archive}")
  private String archiveDir;

  @Value("${bankservafrica.batch.cleanup-temp-files:true}")
  private boolean cleanupTempFiles;

  /**
   * Upload ACH batch file to BankservAfrica
   *
   * @param batchContent Batch file content (plain text)
   * @param tenantId Tenant ID for file naming
   * @return Remote file name
   */
  @CircuitBreaker(name = "bankservafrica", fallbackMethod = "uploadBatchFileFallback")
  @Retry(name = "bankservafrica")
  public String uploadBatchFile(String batchContent, String tenantId) {
    log.info("Uploading ACH batch file to BankservAfrica (tenant: {})", tenantId);

    Path tempPlainFile = null;
    Path tempEncryptedFile = null;

    try {
      // Ensure temp directory exists
      ensureDirectoryExists(tempDir);

      // Generate file name
      String timestamp = LocalDateTime.now().format(FILE_TIMESTAMP_FORMAT);
      String fileName = String.format("ACH_%s_%s.txt", timestamp, tenantId);
      String encryptedFileName = fileName + ".pgp";

      // Write plain text to temp file
      tempPlainFile = Paths.get(tempDir, fileName);
      Files.write(tempPlainFile, batchContent.getBytes());
      log.debug("Plain text batch file written: {}", tempPlainFile);

      // Encrypt file
      tempEncryptedFile = Paths.get(tempDir, encryptedFileName);
      pgpService.encryptFile(tempPlainFile.toString(), tempEncryptedFile.toString());
      log.debug("Batch file encrypted: {}", tempEncryptedFile);

      // Upload encrypted file
      sftpClient.uploadFile(tempEncryptedFile.toString(), encryptedFileName);
      log.info("ACH batch file uploaded successfully: {}", encryptedFileName);

      // Archive plain text file
      archiveFile(tempPlainFile, "ACH_UPLOAD_" + encryptedFileName);

      return encryptedFileName;

    } catch (IOException e) {
      log.error("Failed to upload ACH batch file: {}", e.getMessage(), e);
      throw new BankservAfricaBatchFileException("Failed to upload batch file", e);

    } finally {
      // Cleanup temp files
      if (cleanupTempFiles) {
        cleanupFile(tempPlainFile);
        cleanupFile(tempEncryptedFile);
      }
    }
  }

  /**
   * Upload batch file using byte array
   *
   * @param batchContent Batch file content as bytes
   * @param tenantId Tenant ID
   * @return Remote file name
   */
  @CircuitBreaker(name = "bankservafrica", fallbackMethod = "uploadBatchFileBytesFallback")
  @Retry(name = "bankservafrica")
  public String uploadBatchFileBytes(byte[] batchContent, String tenantId) {
    log.info(
        "Uploading ACH batch file to BankservAfrica ({} bytes, tenant: {})",
        batchContent.length,
        tenantId);

    try {
      // Generate file name
      String timestamp = LocalDateTime.now().format(FILE_TIMESTAMP_FORMAT);
      String encryptedFileName = String.format("ACH_%s_%s.txt.pgp", timestamp, tenantId);

      // Encrypt content
      byte[] encryptedContent = pgpService.encrypt(batchContent);
      log.debug("Batch content encrypted ({} bytes)", encryptedContent.length);

      // Upload encrypted content
      sftpClient.uploadFileContent(encryptedContent, encryptedFileName);
      log.info("ACH batch file uploaded successfully: {}", encryptedFileName);

      return encryptedFileName;

    } catch (Exception e) {
      log.error("Failed to upload ACH batch file: {}", e.getMessage(), e);
      throw new BankservAfricaBatchFileException("Failed to upload batch file", e);
    }
  }

  /**
   * Download and decrypt response file from BankservAfrica
   *
   * @param remoteFileName Remote file name
   * @return Decrypted file content
   */
  @CircuitBreaker(name = "bankservafrica", fallbackMethod = "downloadBatchFileFallback")
  @Retry(name = "bankservafrica")
  public String downloadBatchFile(String remoteFileName) {
    log.info("Downloading batch file from BankservAfrica: {}", remoteFileName);

    Path tempEncryptedFile = null;
    Path tempDecryptedFile = null;

    try {
      // Ensure temp directory exists
      ensureDirectoryExists(tempDir);

      // Download encrypted file
      tempEncryptedFile = Paths.get(tempDir, remoteFileName);
      sftpClient.downloadFile(remoteFileName, tempEncryptedFile.toString());
      log.debug("Encrypted file downloaded: {}", tempEncryptedFile);

      // Decrypt file
      String decryptedFileName = remoteFileName.replace(".pgp", "_decrypted.txt");
      tempDecryptedFile = Paths.get(tempDir, decryptedFileName);
      pgpService.decryptFile(tempEncryptedFile.toString(), tempDecryptedFile.toString());
      log.debug("File decrypted: {}", tempDecryptedFile);

      // Read decrypted content
      String content = Files.readString(tempDecryptedFile);
      log.info(
          "Batch file downloaded and decrypted successfully: {} ({} chars)",
          remoteFileName,
          content.length());

      // Archive encrypted file (before archiving on SFTP)
      archiveFile(tempEncryptedFile, "EFT_DOWNLOAD_" + remoteFileName);

      // Archive file on SFTP server
      sftpClient.archiveFile(remoteFileName);

      return content;

    } catch (IOException e) {
      log.error("Failed to download batch file: {}", e.getMessage(), e);
      throw new BankservAfricaBatchFileException("Failed to download batch file", e);

    } finally {
      // Cleanup temp files
      if (cleanupTempFiles) {
        cleanupFile(tempEncryptedFile);
        cleanupFile(tempDecryptedFile);
      }
    }
  }

  /**
   * Download batch file as byte array
   *
   * @param remoteFileName Remote file name
   * @return Decrypted file content as bytes
   */
  @CircuitBreaker(name = "bankservafrica", fallbackMethod = "downloadBatchFileBytesFallback")
  @Retry(name = "bankservafrica")
  public byte[] downloadBatchFileBytes(String remoteFileName) {
    log.info("Downloading batch file from BankservAfrica: {}", remoteFileName);

    try {
      // Download encrypted content
      byte[] encryptedContent = sftpClient.downloadFileContent(remoteFileName);
      log.debug("Encrypted content downloaded ({} bytes)", encryptedContent.length);

      // Decrypt content
      byte[] decryptedContent = pgpService.decrypt(encryptedContent);
      log.info(
          "Batch file downloaded and decrypted successfully: {} ({} bytes)",
          remoteFileName,
          decryptedContent.length);

      // Archive file on SFTP server
      sftpClient.archiveFile(remoteFileName);

      return decryptedContent;

    } catch (Exception e) {
      log.error("Failed to download batch file: {}", e.getMessage(), e);
      throw new BankservAfricaBatchFileException("Failed to download batch file", e);
    }
  }

  /**
   * List available response files
   *
   * @return List of file names
   */
  @CircuitBreaker(name = "bankservafrica")
  @Retry(name = "bankservafrica")
  public List<String> listAvailableFiles() {
    log.info("Listing available batch files from BankservAfrica");

    try {
      List<String> files = sftpClient.listFiles();
      log.info("Found {} available batch files", files.size());
      return files;

    } catch (Exception e) {
      log.error("Failed to list batch files: {}", e.getMessage(), e);
      throw new BankservAfricaBatchFileException("Failed to list batch files", e);
    }
  }

  /**
   * Check if file exists on SFTP server
   *
   * @param remoteFileName Remote file name
   * @return true if file exists
   */
  public boolean fileExists(String remoteFileName) {
    return sftpClient.fileExists(remoteFileName);
  }

  /**
   * Test SFTP and PGP configuration
   *
   * @return true if configuration is valid
   */
  public boolean testConfiguration() {
    log.info("Testing BankservAfrica batch file configuration");

    try {
      // Test SFTP connection
      boolean sftpOk = sftpClient.testConnection();
      if (!sftpOk) {
        log.error("SFTP connection test failed");
        return false;
      }

      // Test PGP configuration
      boolean pgpOk = pgpService.testConfiguration();
      if (!pgpOk) {
        log.error("PGP configuration test failed");
        return false;
      }

      log.info("BankservAfrica batch file configuration test successful");
      return true;

    } catch (Exception e) {
      log.error("Configuration test failed: {}", e.getMessage(), e);
      return false;
    }
  }

  /** Ensure directory exists */
  private void ensureDirectoryExists(String directory) throws IOException {
    Path path = Paths.get(directory);
    if (!Files.exists(path)) {
      Files.createDirectories(path);
      log.debug("Created directory: {}", directory);
    }
  }

  /** Archive file to archive directory */
  private void archiveFile(Path sourceFile, String archiveName) {
    try {
      if (sourceFile != null && Files.exists(sourceFile)) {
        ensureDirectoryExists(archiveDir);
        Path archivePath = Paths.get(archiveDir, archiveName);
        Files.copy(sourceFile, archivePath);
        log.debug("File archived: {}", archivePath);
      }
    } catch (IOException e) {
      log.warn("Failed to archive file: {}", e.getMessage());
      // Don't throw exception, archival is best-effort
    }
  }

  /** Cleanup temporary file */
  private void cleanupFile(Path file) {
    try {
      if (file != null && Files.exists(file)) {
        Files.delete(file);
        log.debug("Temp file cleaned up: {}", file);
      }
    } catch (IOException e) {
      log.warn("Failed to cleanup temp file: {}", e.getMessage());
      // Don't throw exception, cleanup is best-effort
    }
  }

  // Fallback methods for resilience

  private String uploadBatchFileFallback(String batchContent, String tenantId, Throwable t) {
    log.error("Circuit breaker: Upload failed for tenant {}: {}", tenantId, t.getMessage());
    throw new BankservAfricaBatchFileException("Upload circuit breaker activated", t);
  }

  private String uploadBatchFileBytesFallback(byte[] batchContent, String tenantId, Throwable t) {
    log.error("Circuit breaker: Upload failed for tenant {}: {}", tenantId, t.getMessage());
    throw new BankservAfricaBatchFileException("Upload circuit breaker activated", t);
  }

  private String downloadBatchFileFallback(String remoteFileName, Throwable t) {
    log.error("Circuit breaker: Download failed for {}: {}", remoteFileName, t.getMessage());
    throw new BankservAfricaBatchFileException("Download circuit breaker activated", t);
  }

  private byte[] downloadBatchFileBytesFallback(String remoteFileName, Throwable t) {
    log.error("Circuit breaker: Download failed for {}: {}", remoteFileName, t.getMessage());
    throw new BankservAfricaBatchFileException("Download circuit breaker activated", t);
  }

  /** Custom exception for batch file operations */
  public static class BankservAfricaBatchFileException extends RuntimeException {
    public BankservAfricaBatchFileException(String message, Throwable cause) {
      super(message, cause);
    }
  }
}
