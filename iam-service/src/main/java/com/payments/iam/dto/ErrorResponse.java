package com.payments.iam.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * ErrorResponse - Standard error response DTO.
 *
 * <p>Returned by IamExceptionHandler for all exceptions
 * Contains: timestamp, HTTP status, error type, message, path
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {

  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
  private LocalDateTime timestamp;

  private int status;

  private String error;

  private String message;

  private String details;

  private String path;
}
