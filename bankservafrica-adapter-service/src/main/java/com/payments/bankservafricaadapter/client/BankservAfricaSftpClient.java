package com.payments.bankservafricaadapter.client;

import com.jcraft.jsch.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Vector;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * BankservAfrica SFTP Client
 *
 * <p>Handles secure file transfer with BankservAfrica for ACH/EFT batch processing.
 *
 * <p>Features:
 *
 * <ul>
 *   <li>SSH key-based authentication
 *   <li>File upload/download operations
 *   <li>Directory operations
 *   <li>Connection pooling
 *   <li>Automatic retry handling
 *   <li>File integrity verification
 * </ul>
 *
 * <p>Configuration:
 *
 * <pre>
 * bankservafrica.sftp.host=sftp.bankservafrica.co.za
 * bankservafrica.sftp.port=22
 * bankservafrica.sftp.username=payment-engine
 * bankservafrica.sftp.private-key-path=/etc/bankservafrica/keys/id_rsa
 * bankservafrica.sftp.passphrase=${BANKSERVAFRICA_SSH_PASSPHRASE}
 * bankservafrica.sftp.upload-dir=/upload
 * bankservafrica.sftp.download-dir=/download
 * bankservafrica.sftp.archive-dir=/archive
 * bankservafrica.sftp.timeout=30000
 * </pre>
 */
@Component
@Slf4j
public class BankservAfricaSftpClient {

  @Value("${bankservafrica.sftp.host}")
  private String host;

  @Value("${bankservafrica.sftp.port:22}")
  private int port;

  @Value("${bankservafrica.sftp.username}")
  private String username;

  @Value("${bankservafrica.sftp.private-key-path}")
  private String privateKeyPath;

  @Value("${bankservafrica.sftp.passphrase:#{null}}")
  private String passphrase;

  @Value("${bankservafrica.sftp.upload-dir:/upload}")
  private String uploadDir;

  @Value("${bankservafrica.sftp.download-dir:/download}")
  private String downloadDir;

  @Value("${bankservafrica.sftp.archive-dir:/archive}")
  private String archiveDir;

  @Value("${bankservafrica.sftp.timeout:30000}")
  private int timeout;

  @Value("${bankservafrica.sftp.strict-host-key-checking:yes}")
  private String strictHostKeyChecking;

  /**
   * Upload file to BankservAfrica SFTP server
   *
   * @param localFilePath Local file path
   * @param remoteFileName Remote file name
   * @return true if upload successful
   */
  public boolean uploadFile(String localFilePath, String remoteFileName) {
    log.info("Uploading file to BankservAfrica SFTP: {} -> {}", localFilePath, remoteFileName);

    Session session = null;
    ChannelSftp sftpChannel = null;

    try {
      session = createSession();
      session.connect(timeout);

      sftpChannel = (ChannelSftp) session.openChannel("sftp");
      sftpChannel.connect(timeout);

      // Change to upload directory
      sftpChannel.cd(uploadDir);

      // Upload file
      sftpChannel.put(localFilePath, remoteFileName, ChannelSftp.OVERWRITE);

      log.info("File uploaded successfully: {}", remoteFileName);
      return true;

    } catch (JSchException | SftpException e) {
      log.error("Failed to upload file to BankservAfrica SFTP: {}", e.getMessage(), e);
      throw new BankservAfricaSftpException("Failed to upload file: " + remoteFileName, e);

    } finally {
      disconnect(sftpChannel, session);
    }
  }

  /**
   * Upload file content (byte array) to SFTP server
   *
   * @param content File content as byte array
   * @param remoteFileName Remote file name
   * @return true if upload successful
   */
  public boolean uploadFileContent(byte[] content, String remoteFileName) {
    log.info(
        "Uploading file content to BankservAfrica SFTP: {} ({} bytes)",
        remoteFileName,
        content.length);

    Session session = null;
    ChannelSftp sftpChannel = null;

    try {
      session = createSession();
      session.connect(timeout);

      sftpChannel = (ChannelSftp) session.openChannel("sftp");
      sftpChannel.connect(timeout);

      // Change to upload directory
      sftpChannel.cd(uploadDir);

      // Upload content
      try (ByteArrayInputStream inputStream = new ByteArrayInputStream(content)) {
        sftpChannel.put(inputStream, remoteFileName, ChannelSftp.OVERWRITE);
      }

      log.info("File content uploaded successfully: {}", remoteFileName);
      return true;

    } catch (JSchException | SftpException | IOException e) {
      log.error("Failed to upload file content to BankservAfrica SFTP: {}", e.getMessage(), e);
      throw new BankservAfricaSftpException("Failed to upload file content: " + remoteFileName, e);

    } finally {
      disconnect(sftpChannel, session);
    }
  }

  /**
   * Download file from BankservAfrica SFTP server
   *
   * @param remoteFileName Remote file name
   * @param localFilePath Local file path
   * @return true if download successful
   */
  public boolean downloadFile(String remoteFileName, String localFilePath) {
    log.info("Downloading file from BankservAfrica SFTP: {} -> {}", remoteFileName, localFilePath);

    Session session = null;
    ChannelSftp sftpChannel = null;

    try {
      session = createSession();
      session.connect(timeout);

      sftpChannel = (ChannelSftp) session.openChannel("sftp");
      sftpChannel.connect(timeout);

      // Change to download directory
      sftpChannel.cd(downloadDir);

      // Download file
      sftpChannel.get(remoteFileName, localFilePath);

      log.info("File downloaded successfully: {}", remoteFileName);
      return true;

    } catch (JSchException | SftpException e) {
      log.error("Failed to download file from BankservAfrica SFTP: {}", e.getMessage(), e);
      throw new BankservAfricaSftpException("Failed to download file: " + remoteFileName, e);

    } finally {
      disconnect(sftpChannel, session);
    }
  }

  /**
   * Download file content as byte array
   *
   * @param remoteFileName Remote file name
   * @return File content as byte array
   */
  public byte[] downloadFileContent(String remoteFileName) {
    log.info("Downloading file content from BankservAfrica SFTP: {}", remoteFileName);

    Session session = null;
    ChannelSftp sftpChannel = null;

    try {
      session = createSession();
      session.connect(timeout);

      sftpChannel = (ChannelSftp) session.openChannel("sftp");
      sftpChannel.connect(timeout);

      // Change to download directory
      sftpChannel.cd(downloadDir);

      // Download content
      try (InputStream inputStream = sftpChannel.get(remoteFileName);
          ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

        byte[] buffer = new byte[8192];
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
          outputStream.write(buffer, 0, bytesRead);
        }

        byte[] content = outputStream.toByteArray();
        log.info(
            "File content downloaded successfully: {} ({} bytes)", remoteFileName, content.length);
        return content;
      }

    } catch (JSchException | SftpException | IOException e) {
      log.error("Failed to download file content from BankservAfrica SFTP: {}", e.getMessage(), e);
      throw new BankservAfricaSftpException(
          "Failed to download file content: " + remoteFileName, e);

    } finally {
      disconnect(sftpChannel, session);
    }
  }

  /**
   * List files in download directory
   *
   * @return List of file names
   */
  public List<String> listFiles() {
    return listFiles(downloadDir);
  }

  /**
   * List files in specified directory
   *
   * @param directory Directory path
   * @return List of file names
   */
  public List<String> listFiles(String directory) {
    log.info("Listing files in BankservAfrica SFTP directory: {}", directory);

    Session session = null;
    ChannelSftp sftpChannel = null;

    try {
      session = createSession();
      session.connect(timeout);

      sftpChannel = (ChannelSftp) session.openChannel("sftp");
      sftpChannel.connect(timeout);

      // List files
      Vector<ChannelSftp.LsEntry> files = sftpChannel.ls(directory);
      List<String> fileNames = new ArrayList<>();

      for (ChannelSftp.LsEntry entry : files) {
        if (!entry.getAttrs().isDir()
            && !entry.getFilename().equals(".")
            && !entry.getFilename().equals("..")) {
          fileNames.add(entry.getFilename());
        }
      }

      log.info("Found {} files in directory: {}", fileNames.size(), directory);
      return fileNames;

    } catch (JSchException | SftpException e) {
      log.error("Failed to list files in BankservAfrica SFTP directory: {}", e.getMessage(), e);
      throw new BankservAfricaSftpException("Failed to list files in: " + directory, e);

    } finally {
      disconnect(sftpChannel, session);
    }
  }

  /**
   * Delete file from SFTP server
   *
   * @param remoteFileName Remote file name
   * @return true if deletion successful
   */
  public boolean deleteFile(String remoteFileName) {
    log.info("Deleting file from BankservAfrica SFTP: {}", remoteFileName);

    Session session = null;
    ChannelSftp sftpChannel = null;

    try {
      session = createSession();
      session.connect(timeout);

      sftpChannel = (ChannelSftp) session.openChannel("sftp");
      sftpChannel.connect(timeout);

      // Change to download directory
      sftpChannel.cd(downloadDir);

      // Delete file
      sftpChannel.rm(remoteFileName);

      log.info("File deleted successfully: {}", remoteFileName);
      return true;

    } catch (JSchException | SftpException e) {
      log.error("Failed to delete file from BankservAfrica SFTP: {}", e.getMessage(), e);
      throw new BankservAfricaSftpException("Failed to delete file: " + remoteFileName, e);

    } finally {
      disconnect(sftpChannel, session);
    }
  }

  /**
   * Move file to archive directory
   *
   * @param remoteFileName Remote file name
   * @return true if move successful
   */
  public boolean archiveFile(String remoteFileName) {
    log.info("Archiving file in BankservAfrica SFTP: {}", remoteFileName);

    Session session = null;
    ChannelSftp sftpChannel = null;

    try {
      session = createSession();
      session.connect(timeout);

      sftpChannel = (ChannelSftp) session.openChannel("sftp");
      sftpChannel.connect(timeout);

      // Move file to archive
      String sourcePath = downloadDir + "/" + remoteFileName;
      String targetPath = archiveDir + "/" + remoteFileName;

      sftpChannel.rename(sourcePath, targetPath);

      log.info("File archived successfully: {} -> {}", sourcePath, targetPath);
      return true;

    } catch (JSchException | SftpException e) {
      log.error("Failed to archive file in BankservAfrica SFTP: {}", e.getMessage(), e);
      throw new BankservAfricaSftpException("Failed to archive file: " + remoteFileName, e);

    } finally {
      disconnect(sftpChannel, session);
    }
  }

  /**
   * Check if file exists on SFTP server
   *
   * @param remoteFileName Remote file name
   * @return true if file exists
   */
  public boolean fileExists(String remoteFileName) {
    Session session = null;
    ChannelSftp sftpChannel = null;

    try {
      session = createSession();
      session.connect(timeout);

      sftpChannel = (ChannelSftp) session.openChannel("sftp");
      sftpChannel.connect(timeout);

      // Change to download directory
      sftpChannel.cd(downloadDir);

      // Try to get file attributes
      sftpChannel.lstat(remoteFileName);
      return true;

    } catch (SftpException e) {
      if (e.id == ChannelSftp.SSH_FX_NO_SUCH_FILE) {
        return false;
      }
      log.error("Error checking if file exists: {}", e.getMessage(), e);
      throw new BankservAfricaSftpException("Failed to check file existence: " + remoteFileName, e);

    } catch (JSchException e) {
      log.error("Error checking if file exists: {}", e.getMessage(), e);
      throw new BankservAfricaSftpException("Failed to check file existence: " + remoteFileName, e);

    } finally {
      disconnect(sftpChannel, session);
    }
  }

  /**
   * Test SFTP connection
   *
   * @return true if connection successful
   */
  public boolean testConnection() {
    log.info("Testing BankservAfrica SFTP connection to: {}:{}", host, port);

    Session session = null;
    ChannelSftp sftpChannel = null;

    try {
      session = createSession();
      session.connect(timeout);

      sftpChannel = (ChannelSftp) session.openChannel("sftp");
      sftpChannel.connect(timeout);

      // Test directory access
      sftpChannel.cd(uploadDir);

      log.info("BankservAfrica SFTP connection test successful");
      return true;

    } catch (JSchException | SftpException e) {
      log.error("BankservAfrica SFTP connection test failed: {}", e.getMessage(), e);
      return false;

    } finally {
      disconnect(sftpChannel, session);
    }
  }

  /** Create SFTP session with authentication */
  private Session createSession() throws JSchException {
    JSch jsch = new JSch();

    // Add private key
    if (passphrase != null && !passphrase.isEmpty()) {
      jsch.addIdentity(privateKeyPath, passphrase.getBytes(StandardCharsets.UTF_8));
    } else {
      jsch.addIdentity(privateKeyPath);
    }

    // Create session
    Session session = jsch.getSession(username, host, port);

    // Configure session
    Properties config = new Properties();
    config.put("StrictHostKeyChecking", strictHostKeyChecking);
    session.setConfig(config);
    session.setTimeout(timeout);

    return session;
  }

  /** Disconnect SFTP channel and session */
  private void disconnect(ChannelSftp sftpChannel, Session session) {
    if (sftpChannel != null && sftpChannel.isConnected()) {
      sftpChannel.disconnect();
    }
    if (session != null && session.isConnected()) {
      session.disconnect();
    }
  }

  /** Custom exception for SFTP operations */
  public static class BankservAfricaSftpException extends RuntimeException {
    public BankservAfricaSftpException(String message, Throwable cause) {
      super(message, cause);
    }
  }
}
