package com.payments.tenant.exception;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

/**
 * Error Response DTO - Structured error format returned to clients.
 *
 * <p>Fields:
 * - timestamp: When error occurred (ISO 8601 format)
 * - status: HTTP status code (400, 404, 409, 500)
 * - error: Short error name (e.g., "Validation Failed")
 * - message: Detailed error message
 * - details: Field-level details (for validation errors)
 * - path: Request path that caused the error
 *
 * <p>Example responses:
 * ```json
 * {
 *   "timestamp": "2025-10-18T17:21:43.123Z",
 *   "status": 404,
 *   "error": "Tenant Not Found",
 *   "message": "Tenant not found: BANK-XYZ",
 *   "path": "/tenants/BANK-XYZ"
 * }
 *
 * {
 *   "timestamp": "2025-10-18T17:21:43.123Z",
 *   "status": 400,
 *   "error": "Validation Failed",
 *   "message": "Invalid input parameters",
 *   "details": "tenantName: tenantName must be between 3 and 200 characters; contactEmail: contactEmail must be a valid email address",
 *   "path": "/tenants"
 * }
 * ```
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ErrorResponse {

  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ")
  private OffsetDateTime timestamp;

  private int status;
  private String error;
  private String message;
  private String details;
  private String path;
}
