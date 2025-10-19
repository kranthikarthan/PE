package com.payments.batch.sftp;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents information about a remote SFTP file or directory.
 *
 * <p>This class encapsulates file metadata including name, path, size, type,
 * and modification time for remote SFTP files and directories.
 *
 * @since PE-403
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SftpFileInfo {
  
  /** File or directory name */
  private String name;
  
  /** Full remote path */
  private String path;
  
  /** File size in bytes */
  private long size;
  
  /** Whether this is a directory */
  private boolean isDirectory;
  
  /** Last modification time in milliseconds since epoch */
  private long lastModified;
  
  /**
   * Gets the file size in a human-readable format.
   *
   * @return formatted file size (e.g., "1.5 MB", "250 KB")
   */
  public String getFormattedSize() {
    if (isDirectory) {
      return "DIR";
    }
    
    if (size < 1024) {
      return size + " B";
    } else if (size < 1024 * 1024) {
      return String.format("%.1f KB", size / 1024.0);
    } else if (size < 1024 * 1024 * 1024) {
      return String.format("%.1f MB", size / (1024.0 * 1024.0));
    } else {
      return String.format("%.1f GB", size / (1024.0 * 1024.0 * 1024.0));
    }
  }
  
  /**
   * Gets the last modification time as a LocalDateTime.
   *
   * @return last modification time
   */
  public LocalDateTime getLastModifiedDateTime() {
    return LocalDateTime.ofInstant(
        Instant.ofEpochMilli(lastModified), 
        ZoneId.systemDefault()
    );
  }
  
  /**
   * Gets the last modification time in a formatted string.
   *
   * @return formatted modification time (e.g., "2024-01-15 14:30:25")
   */
  public String getFormattedLastModified() {
    return getLastModifiedDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
  }
  
  /**
   * Gets the file extension (for files only).
   *
   * @return file extension without the dot, or empty string if no extension
   */
  public String getExtension() {
    if (isDirectory || name == null) {
      return "";
    }
    
    int lastDotIndex = name.lastIndexOf('.');
    if (lastDotIndex > 0 && lastDotIndex < name.length() - 1) {
      return name.substring(lastDotIndex + 1).toLowerCase();
    }
    
    return "";
  }
  
  /**
   * Checks if this is a file (not a directory).
   *
   * @return true if this is a file, false if it's a directory
   */
  public boolean isFile() {
    return !isDirectory;
  }
  
  /**
   * Checks if this file has a specific extension.
   *
   * @param extension the extension to check (without the dot)
   * @return true if the file has the specified extension
   */
  public boolean hasExtension(String extension) {
    if (extension == null || extension.trim().isEmpty()) {
      return getExtension().isEmpty();
    }
    
    return getExtension().equals(extension.toLowerCase());
  }
  
  /**
   * Checks if this file is older than the specified number of days.
   *
   * @param days the number of days
   * @return true if the file is older than the specified days
   */
  public boolean isOlderThanDays(int days) {
    long daysInMillis = days * 24L * 60L * 60L * 1000L;
    return (System.currentTimeMillis() - lastModified) > daysInMillis;
  }
  
  /**
   * Checks if this file is newer than the specified number of days.
   *
   * @param days the number of days
   * @return true if the file is newer than the specified days
   */
  public boolean isNewerThanDays(int days) {
    long daysInMillis = days * 24L * 60L * 60L * 1000L;
    return (System.currentTimeMillis() - lastModified) <= daysInMillis;
  }
  
  /**
   * Gets a summary of the file information.
   *
   * @return file information summary
   */
  public String getSummary() {
    String type = isDirectory ? "DIR" : "FILE";
    return String.format("%s %s %s %s", 
        type, 
        getFormattedSize(), 
        getFormattedLastModified(), 
        name
    );
  }
  
  @Override
  public String toString() {
    return getSummary();
  }
}
