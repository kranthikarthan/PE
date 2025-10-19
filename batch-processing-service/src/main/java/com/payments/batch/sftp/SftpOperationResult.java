package com.payments.batch.sftp;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents the result of an SFTP operation.
 *
 * <p>This class encapsulates the result of SFTP operations including success status,
 * error messages, file sizes, and timing information for monitoring and debugging.
 *
 * @since PE-403
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SftpOperationResult {
  
  /** Whether the operation was successful */
  private boolean success;
  
  /** Operation result message */
  private String message;
  
  /** File size in bytes (for upload/download operations) */
  private long fileSize;
  
  /** Operation start time */
  private LocalDateTime startTime;
  
  /** Operation end time */
  private LocalDateTime endTime;
  
  /** Error code (if operation failed) */
  private int errorCode;
  
  /** Error details (if operation failed) */
  private String errorDetails;
  
  /** Remote file path (for file operations) */
  private String remoteFilePath;
  
  /** Local file path (for file operations) */
  private String localFilePath;
  
  /**
   * Creates a successful operation result.
   *
   * @param message the success message
   * @param fileSize the file size in bytes
   * @return successful operation result
   */
  public static SftpOperationResult success(String message, long fileSize) {
    return SftpOperationResult.builder()
        .success(true)
        .message(message)
        .fileSize(fileSize)
        .endTime(LocalDateTime.now())
        .build();
  }
  
  /**
   * Creates a successful operation result.
   *
   * @param message the success message
   * @return successful operation result
   */
  public static SftpOperationResult success(String message) {
    return success(message, 0);
  }
  
  /**
   * Creates a failed operation result.
   *
   * @param message the error message
   * @param errorCode the error code
   * @param errorDetails additional error details
   * @return failed operation result
   */
  public static SftpOperationResult failure(String message, int errorCode, String errorDetails) {
    return SftpOperationResult.builder()
        .success(false)
        .message(message)
        .errorCode(errorCode)
        .errorDetails(errorDetails)
        .endTime(LocalDateTime.now())
        .build();
  }
  
  /**
   * Creates a failed operation result.
   *
   * @param message the error message
   * @param errorCode the error code
   * @return failed operation result
   */
  public static SftpOperationResult failure(String message, int errorCode) {
    return failure(message, errorCode, null);
  }
  
  /**
   * Gets the operation duration in milliseconds.
   *
   * @return duration in milliseconds, or -1 if timing information is not available
   */
  public long getDurationMillis() {
    if (startTime == null || endTime == null) {
      return -1;
    }
    return java.time.Duration.between(startTime, endTime).toMillis();
  }
  
  /**
   * Gets a formatted duration string.
   *
   * @return formatted duration (e.g., "1.5s", "250ms")
   */
  public String getFormattedDuration() {
    long duration = getDurationMillis();
    if (duration < 0) {
      return "N/A";
    }
    
    if (duration < 1000) {
      return duration + "ms";
    } else {
      return String.format("%.1fs", duration / 1000.0);
    }
  }
  
  /**
   * Checks if the operation was successful.
   *
   * @return true if successful, false otherwise
   */
  public boolean isSuccess() {
    return success;
  }
  
  /**
   * Checks if the operation failed.
   *
   * @return true if failed, false otherwise
   */
  public boolean isFailure() {
    return !success;
  }
  
  /**
   * Gets a summary of the operation result.
   *
   * @return operation summary
   */
  public String getSummary() {
    if (success) {
      return String.format("SUCCESS: %s (Size: %d bytes, Duration: %s)", 
          message, fileSize, getFormattedDuration());
    } else {
      return String.format("FAILURE: %s (Error: %d, Details: %s)", 
          message, errorCode, errorDetails != null ? errorDetails : "N/A");
    }
  }
  
  @Override
  public String toString() {
    return getSummary();
  }
}
