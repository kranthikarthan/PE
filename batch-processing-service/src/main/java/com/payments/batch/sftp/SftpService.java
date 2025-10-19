package com.payments.batch.sftp;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.SftpException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Service for SFTP file operations with comprehensive error handling and retry logic.
 *
 * <p>This service provides high-level SFTP operations including file upload, download,
 * listing, archiving, and cleanup. It handles connection management, retry logic,
 * and provides detailed logging for monitoring and troubleshooting.
 *
 * <p><b>Supported Operations:</b>
 * <ul>
 *   <li>File upload (local to remote)
 *   <li>File download (remote to local)
 *   <li>File listing and directory operations
 *   <li>File archiving and cleanup
 *   <li>Directory creation and management
 * </ul>
 *
 * <p><b>Features:</b>
 * <ul>
 *   <li>Automatic retry with exponential backoff
 *   <li>Connection pooling for performance
 *   <li>Comprehensive error handling
 *   <li>File integrity validation
 *   <li>Automatic directory creation
 * </ul>
 *
 * @since PE-403
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SftpService {
  
  private final SftpConnectionManager connectionManager;
  private final SftpConfiguration config;
  
  /**
   * Uploads a file to the SFTP server.
   *
   * @param localFilePath the local file path
   * @param remoteFilePath the remote file path
   * @return upload result with success status and details
   */
  public SftpOperationResult uploadFile(String localFilePath, String remoteFilePath) {
    return executeWithRetry(() -> {
      log.info("Uploading file: {} -> {}", localFilePath, remoteFilePath);
      
      File localFile = new File(localFilePath);
      if (!localFile.exists()) {
        throw new SftpException(ChannelSftp.SSH_FX_NO_SUCH_FILE, "Local file does not exist: " + localFilePath);
      }
      
      if (localFile.length() > config.getMaxFileSize()) {
        throw new SftpException(ChannelSftp.SSH_FX_FAILURE, 
            "File size exceeds maximum allowed size: " + config.getMaxFileSize());
      }
      
      ChannelSftp connection = null;
      try {
        connection = connectionManager.getConnection();
        
        // Ensure remote directory exists
        ensureRemoteDirectoryExists(connection, remoteFilePath);
        
        // Upload file
        try (FileInputStream inputStream = new FileInputStream(localFile)) {
          connection.put(inputStream, remoteFilePath);
        }
        
        // Verify upload
        long remoteFileSize = connection.stat(remoteFilePath).getSize();
        if (remoteFileSize != localFile.length()) {
          throw new SftpException(ChannelSftp.SSH_FX_FAILURE, 
              "File size mismatch after upload. Expected: " + localFile.length() + ", Got: " + remoteFileSize);
        }
        
        log.info("Successfully uploaded file: {} ({} bytes)", remoteFilePath, remoteFileSize);
        return new SftpOperationResult(true, "File uploaded successfully", remoteFileSize);
        
      } finally {
        if (connection != null) {
          connectionManager.returnConnection(connection);
        }
      }
    });
  }
  
  /**
   * Downloads a file from the SFTP server.
   *
   * @param remoteFilePath the remote file path
   * @param localFilePath the local file path
   * @return download result with success status and details
   */
  public SftpOperationResult downloadFile(String remoteFilePath, String localFilePath) {
    return executeWithRetry(() -> {
      log.info("Downloading file: {} -> {}", remoteFilePath, localFilePath);
      
      ChannelSftp connection = null;
      try {
        connection = connectionManager.getConnection();
        
        // Check if remote file exists
        if (!fileExists(connection, remoteFilePath)) {
          throw new SftpException(ChannelSftp.SSH_FX_NO_SUCH_FILE, "Remote file does not exist: " + remoteFilePath);
        }
        
        // Ensure local directory exists
        ensureLocalDirectoryExists(localFilePath);
        
        // Download file
        try (FileOutputStream outputStream = new FileOutputStream(localFilePath)) {
          connection.get(remoteFilePath, outputStream);
        }
        
        // Verify download
        File localFile = new File(localFilePath);
        if (!localFile.exists() || localFile.length() == 0) {
          throw new SftpException(ChannelSftp.SSH_FX_FAILURE, "Downloaded file is empty or does not exist");
        }
        
        log.info("Successfully downloaded file: {} ({} bytes)", localFilePath, localFile.length());
        return new SftpOperationResult(true, "File downloaded successfully", localFile.length());
        
      } finally {
        if (connection != null) {
          connectionManager.returnConnection(connection);
        }
      }
    });
  }
  
  /**
   * Lists files in a remote directory.
   *
   * @param remoteDirectory the remote directory path
   * @return list of remote files
   */
  public List<SftpFileInfo> listFiles(String remoteDirectory) {
    return executeWithRetry(() -> {
      log.debug("Listing files in directory: {}", remoteDirectory);
      
      ChannelSftp connection = null;
      try {
        connection = connectionManager.getConnection();
        
        // Check if directory exists
        if (!directoryExists(connection, remoteDirectory)) {
          throw new SftpException(ChannelSftp.SSH_FX_NO_SUCH_FILE, "Remote directory does not exist: " + remoteDirectory);
        }
        
        // List files
        Vector<?> fileList = connection.ls(remoteDirectory);
        List<SftpFileInfo> files = new ArrayList<>();
        
        for (Object obj : fileList) {
          if (obj instanceof ChannelSftp.LsEntry) {
            ChannelSftp.LsEntry entry = (ChannelSftp.LsEntry) obj;
            if (!entry.getFilename().equals(".") && !entry.getFilename().equals("..")) {
              files.add(new SftpFileInfo(
                  entry.getFilename(),
                  remoteDirectory + "/" + entry.getFilename(),
                  entry.getAttrs().getSize(),
                  entry.getAttrs().isDir(),
                  entry.getAttrs().getMTime() * 1000L // Convert to milliseconds
              ));
            }
          }
        }
        
        log.debug("Found {} files in directory: {}", files.size(), remoteDirectory);
        return files;
        
      } finally {
        if (connection != null) {
          connectionManager.returnConnection(connection);
        }
      }
    });
  }
  
  /**
   * Archives a file by moving it to an archive directory.
   *
   * @param remoteFilePath the remote file path to archive
   * @param archiveDirectory the archive directory
   * @return archive result
   */
  public SftpOperationResult archiveFile(String remoteFilePath, String archiveDirectory) {
    return executeWithRetry(() -> {
      log.info("Archiving file: {} -> {}", remoteFilePath, archiveDirectory);
      
      ChannelSftp connection = null;
      try {
        connection = connectionManager.getConnection();
        
        // Check if source file exists
        if (!fileExists(connection, remoteFilePath)) {
          throw new SftpException(ChannelSftp.SSH_FX_NO_SUCH_FILE, "Source file does not exist: " + remoteFilePath);
        }
        
        // Ensure archive directory exists
        ensureRemoteDirectoryExists(connection, archiveDirectory);
        
        // Generate archive filename with timestamp
        String fileName = Paths.get(remoteFilePath).getFileName().toString();
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String archiveFileName = timestamp + "_" + fileName;
        String archiveFilePath = archiveDirectory + "/" + archiveFileName;
        
        // Move file to archive
        connection.rename(remoteFilePath, archiveFilePath);
        
        log.info("Successfully archived file: {} -> {}", remoteFilePath, archiveFilePath);
        return new SftpOperationResult(true, "File archived successfully", 0);
        
      } finally {
        if (connection != null) {
          connectionManager.returnConnection(connection);
        }
      }
    });
  }
  
  /**
   * Deletes a file from the SFTP server.
   *
   * @param remoteFilePath the remote file path to delete
   * @return delete result
   */
  public SftpOperationResult deleteFile(String remoteFilePath) {
    return executeWithRetry(() -> {
      log.info("Deleting file: {}", remoteFilePath);
      
      ChannelSftp connection = null;
      try {
        connection = connectionManager.getConnection();
        
        // Check if file exists
        if (!fileExists(connection, remoteFilePath)) {
          log.warn("File does not exist for deletion: {}", remoteFilePath);
          return new SftpOperationResult(true, "File does not exist", 0);
        }
        
        // Delete file
        connection.rm(remoteFilePath);
        
        log.info("Successfully deleted file: {}", remoteFilePath);
        return new SftpOperationResult(true, "File deleted successfully", 0);
        
      } finally {
        if (connection != null) {
          connectionManager.returnConnection(connection);
        }
      }
    });
  }
  
  /**
   * Executes an operation with retry logic.
   *
   * @param operation the operation to execute
   * @return operation result
   */
  private <T> T executeWithRetry(SftpOperation<T> operation) {
    Exception lastException = null;
    
    for (int attempt = 1; attempt <= config.getRetryAttempts(); attempt++) {
      try {
        return operation.execute();
      } catch (Exception e) {
        lastException = e;
        log.warn("SFTP operation failed (attempt {}/{}): {}", attempt, config.getRetryAttempts(), e.getMessage());
        
        if (attempt < config.getRetryAttempts()) {
          try {
            Thread.sleep(config.getRetryDelay() * attempt); // Exponential backoff
          } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Operation interrupted", ie);
          }
        }
      }
    }
    
    log.error("SFTP operation failed after {} attempts", config.getRetryAttempts());
    throw new RuntimeException("SFTP operation failed after " + config.getRetryAttempts() + " attempts", lastException);
  }
  
  /**
   * Ensures a remote directory exists, creating it if necessary.
   *
   * @param connection the SFTP connection
   * @param filePath the file path to check
   * @throws SftpException if directory creation fails
   */
  private void ensureRemoteDirectoryExists(ChannelSftp connection, String filePath) throws SftpException {
    if (!config.isCreateDirectories()) {
      return;
    }
    
    String directory = Paths.get(filePath).getParent().toString().replace("\\", "/");
    if (directory != null && !directory.isEmpty() && !directoryExists(connection, directory)) {
      log.debug("Creating remote directory: {}", directory);
      connection.mkdir(directory);
    }
  }
  
  /**
   * Ensures a local directory exists, creating it if necessary.
   *
   * @param filePath the file path to check
   * @throws IOException if directory creation fails
   */
  private void ensureLocalDirectoryExists(String filePath) throws IOException {
    Path directory = Paths.get(filePath).getParent();
    if (directory != null && !Files.exists(directory)) {
      log.debug("Creating local directory: {}", directory);
      Files.createDirectories(directory);
    }
  }
  
  /**
   * Checks if a remote file exists.
   *
   * @param connection the SFTP connection
   * @param filePath the file path to check
   * @return true if file exists, false otherwise
   */
  private boolean fileExists(ChannelSftp connection, String filePath) {
    try {
      connection.stat(filePath);
      return true;
    } catch (SftpException e) {
      return false;
    }
  }
  
  /**
   * Checks if a remote directory exists.
   *
   * @param connection the SFTP connection
   * @param directoryPath the directory path to check
   * @return true if directory exists, false otherwise
   */
  private boolean directoryExists(ChannelSftp connection, String directoryPath) {
    try {
      return connection.stat(directoryPath).isDir();
    } catch (SftpException e) {
      return false;
    }
  }
  
  /**
   * Functional interface for SFTP operations.
   */
  @FunctionalInterface
  private interface SftpOperation<T> {
    T execute() throws Exception;
  }
}
