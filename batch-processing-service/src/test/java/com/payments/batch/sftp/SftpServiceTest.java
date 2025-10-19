package com.payments.batch.sftp;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.SftpException;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Unit tests for SftpService.
 *
 * @since PE-403
 */
@DisplayName("SFTP Service Tests")
class SftpServiceTest {
  
  @Mock
  private SftpConnectionManager connectionManager;
  
  @Mock
  private ChannelSftp channelSftp;
  
  @Mock
  private SftpConfiguration config;
  
  private SftpService sftpService;
  
  @TempDir
  Path tempDir;
  
  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    sftpService = new SftpService(connectionManager, config);
    
    // Configure mock behavior
    when(config.getRetryAttempts()).thenReturn(3);
    when(config.getRetryDelay()).thenReturn(100L);
    when(config.getMaxFileSize()).thenReturn(1024 * 1024L); // 1MB
    when(config.isCreateDirectories()).thenReturn(true);
  }
  
  @Test
  @DisplayName("Should upload file successfully")
  void shouldUploadFileSuccessfully() throws Exception {
    // Given
    String localFilePath = createTestFile("test.txt", "Hello World");
    String remoteFilePath = "/remote/test.txt";
    
    when(connectionManager.getConnection()).thenReturn(channelSftp);
    when(channelSftp.isConnected()).thenReturn(true);
    when(channelSftp.stat(remoteFilePath)).thenReturn(createMockAttrs(11, false));
    
    // When
    SftpOperationResult result = sftpService.uploadFile(localFilePath, remoteFilePath);
    
    // Then
    assertTrue(result.isSuccess());
    assertEquals("File uploaded successfully", result.getMessage());
    assertEquals(11, result.getFileSize());
    verify(channelSftp).put(any(FileInputStream.class), eq(remoteFilePath));
    verify(connectionManager).returnConnection(channelSftp);
  }
  
  @Test
  @DisplayName("Should download file successfully")
  void shouldDownloadFileSuccessfully() throws Exception {
    // Given
    String remoteFilePath = "/remote/test.txt";
    String localFilePath = tempDir.resolve("downloaded.txt").toString();
    
    when(connectionManager.getConnection()).thenReturn(channelSftp);
    when(channelSftp.isConnected()).thenReturn(true);
    when(channelSftp.stat(remoteFilePath)).thenReturn(createMockAttrs(11, false));
    
    // When
    SftpOperationResult result = sftpService.downloadFile(remoteFilePath, localFilePath);
    
    // Then
    assertTrue(result.isSuccess());
    assertEquals("File downloaded successfully", result.getMessage());
    verify(channelSftp).get(eq(remoteFilePath), any(FileOutputStream.class));
    verify(connectionManager).returnConnection(channelSftp);
  }
  
  @Test
  @DisplayName("Should list files successfully")
  void shouldListFilesSuccessfully() throws Exception {
    // Given
    String remoteDirectory = "/remote";
    Vector<ChannelSftp.LsEntry> fileList = createMockFileList();
    
    when(connectionManager.getConnection()).thenReturn(channelSftp);
    when(channelSftp.isConnected()).thenReturn(true);
    when(channelSftp.stat(remoteDirectory)).thenReturn(createMockAttrs(0, true));
    when(channelSftp.ls(remoteDirectory)).thenReturn(fileList);
    
    // When
    List<SftpFileInfo> files = sftpService.listFiles(remoteDirectory);
    
    // Then
    assertEquals(2, files.size());
    assertEquals("file1.txt", files.get(0).getName());
    assertEquals("file2.txt", files.get(1).getName());
    verify(connectionManager).returnConnection(channelSftp);
  }
  
  @Test
  @DisplayName("Should archive file successfully")
  void shouldArchiveFileSuccessfully() throws Exception {
    // Given
    String remoteFilePath = "/remote/test.txt";
    String archiveDirectory = "/archive";
    
    when(connectionManager.getConnection()).thenReturn(channelSftp);
    when(channelSftp.isConnected()).thenReturn(true);
    when(channelSftp.stat(remoteFilePath)).thenReturn(createMockAttrs(11, false));
    when(channelSftp.stat(archiveDirectory)).thenReturn(createMockAttrs(0, true));
    
    // When
    SftpOperationResult result = sftpService.archiveFile(remoteFilePath, archiveDirectory);
    
    // Then
    assertTrue(result.isSuccess());
    assertEquals("File archived successfully", result.getMessage());
    verify(channelSftp).rename(eq(remoteFilePath), anyString());
    verify(connectionManager).returnConnection(channelSftp);
  }
  
  @Test
  @DisplayName("Should delete file successfully")
  void shouldDeleteFileSuccessfully() throws Exception {
    // Given
    String remoteFilePath = "/remote/test.txt";
    
    when(connectionManager.getConnection()).thenReturn(channelSftp);
    when(channelSftp.isConnected()).thenReturn(true);
    when(channelSftp.stat(remoteFilePath)).thenReturn(createMockAttrs(11, false));
    
    // When
    SftpOperationResult result = sftpService.deleteFile(remoteFilePath);
    
    // Then
    assertTrue(result.isSuccess());
    assertEquals("File deleted successfully", result.getMessage());
    verify(channelSftp).rm(remoteFilePath);
    verify(connectionManager).returnConnection(channelSftp);
  }
  
  @Test
  @DisplayName("Should handle file not found error")
  void shouldHandleFileNotFoundError() throws Exception {
    // Given
    String localFilePath = "/nonexistent/file.txt";
    String remoteFilePath = "/remote/test.txt";
    
    when(connectionManager.getConnection()).thenReturn(channelSftp);
    when(channelSftp.isConnected()).thenReturn(true);
    
    // When
    SftpOperationResult result = sftpService.uploadFile(localFilePath, remoteFilePath);
    
    // Then
    assertTrue(result.isFailure());
    assertTrue(result.getMessage().contains("Local file does not exist"));
    verify(connectionManager).returnConnection(channelSftp);
  }
  
  @Test
  @DisplayName("Should handle file size limit exceeded")
  void shouldHandleFileSizeLimitExceeded() throws Exception {
    // Given
    String localFilePath = createTestFile("large.txt", "x".repeat(1024 * 1024 + 1));
    String remoteFilePath = "/remote/large.txt";
    
    when(connectionManager.getConnection()).thenReturn(channelSftp);
    when(channelSftp.isConnected()).thenReturn(true);
    
    // When
    SftpOperationResult result = sftpService.uploadFile(localFilePath, remoteFilePath);
    
    // Then
    assertTrue(result.isFailure());
    assertTrue(result.getMessage().contains("File size exceeds maximum allowed size"));
    verify(connectionManager).returnConnection(channelSftp);
  }
  
  @Test
  @DisplayName("Should handle SFTP connection errors")
  void shouldHandleSftpConnectionErrors() throws Exception {
    // Given
    String remoteFilePath = "/remote/test.txt";
    String localFilePath = tempDir.resolve("downloaded.txt").toString();
    
    when(connectionManager.getConnection()).thenThrow(new SftpException(ChannelSftp.SSH_FX_FAILURE, "Connection failed"));
    
    // When & Then
    assertThrows(RuntimeException.class, () -> {
      sftpService.downloadFile(remoteFilePath, localFilePath);
    });
  }
  
  @Test
  @DisplayName("Should retry failed operations")
  void shouldRetryFailedOperations() throws Exception {
    // Given
    String localFilePath = createTestFile("test.txt", "Hello World");
    String remoteFilePath = "/remote/test.txt";
    
    when(connectionManager.getConnection())
        .thenThrow(new SftpException(ChannelSftp.SSH_FX_FAILURE, "Connection failed"))
        .thenThrow(new SftpException(ChannelSftp.SSH_FX_FAILURE, "Connection failed"))
        .thenReturn(channelSftp);
    when(channelSftp.isConnected()).thenReturn(true);
    when(channelSftp.stat(remoteFilePath)).thenReturn(createMockAttrs(11, false));
    
    // When
    SftpOperationResult result = sftpService.uploadFile(localFilePath, remoteFilePath);
    
    // Then
    assertTrue(result.isSuccess());
    verify(connectionManager, times(3)).getConnection();
  }
  
  /**
   * Creates a test file with the given content.
   *
   * @param fileName the file name
   * @param content the file content
   * @return the file path
   * @throws Exception if file creation fails
   */
  private String createTestFile(String fileName, String content) throws Exception {
    Path filePath = tempDir.resolve(fileName);
    Files.write(filePath, content.getBytes());
    return filePath.toString();
  }
  
  /**
   * Creates a mock SftpATTRS object.
   *
   * @param size the file size
   * @param isDir whether it's a directory
   * @return mock SftpATTRS
   */
  private SftpATTRS createMockAttrs(long size, boolean isDir) {
    SftpATTRS attrs = mock(SftpATTRS.class);
    when(attrs.getSize()).thenReturn(size);
    when(attrs.isDir()).thenReturn(isDir);
    when(attrs.getMTime()).thenReturn(System.currentTimeMillis() / 1000);
    return attrs;
  }
  
  /**
   * Creates a mock file list for testing.
   *
   * @return mock file list
   */
  private Vector<ChannelSftp.LsEntry> createMockFileList() {
    Vector<ChannelSftp.LsEntry> fileList = new Vector<>();
    
    // Add file1.txt
    ChannelSftp.LsEntry entry1 = mock(ChannelSftp.LsEntry.class);
    when(entry1.getFilename()).thenReturn("file1.txt");
    when(entry1.getAttrs()).thenReturn(createMockAttrs(100, false));
    fileList.add(entry1);
    
    // Add file2.txt
    ChannelSftp.LsEntry entry2 = mock(ChannelSftp.LsEntry.class);
    when(entry2.getFilename()).thenReturn("file2.txt");
    when(entry2.getAttrs()).thenReturn(createMockAttrs(200, false));
    fileList.add(entry2);
    
    return fileList;
  }
}
