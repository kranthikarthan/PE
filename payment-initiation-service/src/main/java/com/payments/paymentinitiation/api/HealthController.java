package com.payments.paymentinitiation.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

/**
 * Health Check Controller
 *
 * <p>Provides health check endpoints for monitoring and load balancer health checks
 */
@Slf4j
@RestController
@RequestMapping("/api/v1")
@Tag(name = "Health", description = "Health check endpoints")
public class HealthController {

  @GetMapping(
      value = "/health",
      produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(
      summary = "Health Check",
      description = "Returns the health status of the Payment Initiation Service")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Service is healthy",
            content = @io.swagger.v3.oas.annotations.media.Content(
                mediaType = "application/json",
                schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = HealthResponse.class))),
        @ApiResponse(
            responseCode = "503",
            description = "Service is unhealthy")
      })
  public ResponseEntity<HealthResponse> health() {
    log.debug("Health check requested");
    
    try {
      // Basic health check - service is running
      HealthResponse response = HealthResponse.builder()
          .status("UP")
          .service("Payment Initiation Service")
          .version("0.1.0-SNAPSHOT")
          .timestamp(Instant.now())
          .build();
      
      return ResponseEntity.ok(response);
      
    } catch (Exception e) {
      log.error("Health check failed", e);
      HealthResponse response = HealthResponse.builder()
          .status("DOWN")
          .service("Payment Initiation Service")
          .version("0.1.0-SNAPSHOT")
          .timestamp(Instant.now())
          .error(e.getMessage())
          .build();
      
      return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }
  }

  @GetMapping(
      value = "/pain001/test",
      produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(
      summary = "Test pain.001 Endpoint",
      description = "Test endpoint for pain.001 message processing")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Test successful",
            content = @io.swagger.v3.oas.annotations.media.Content(
                mediaType = "application/json",
                schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = TestResponse.class)))
      })
  public ResponseEntity<TestResponse> testPain001() {
    log.info("pain.001 test endpoint called");
    
    TestResponse response = TestResponse.builder()
        .message("pain.001 test endpoint is working")
        .timestamp(Instant.now())
        .service("Payment Initiation Service")
        .version("0.1.0-SNAPSHOT")
        .build();
    
    return ResponseEntity.ok(response);
  }

  /**
   * Health response DTO
   */
  @lombok.Data
  @lombok.Builder
  @lombok.NoArgsConstructor
  @lombok.AllArgsConstructor
  @io.swagger.v3.oas.annotations.media.Schema(description = "Health check response")
  public static class HealthResponse {
    @io.swagger.v3.oas.annotations.media.Schema(description = "Service status")
    private String status;
    
    @io.swagger.v3.oas.annotations.media.Schema(description = "Service name")
    private String service;
    
    @io.swagger.v3.oas.annotations.media.Schema(description = "Service version")
    private String version;
    
    @io.swagger.v3.oas.annotations.media.Schema(description = "Response timestamp")
    private Instant timestamp;
    
    @io.swagger.v3.oas.annotations.media.Schema(description = "Error message if unhealthy")
    private String error;
  }

  /**
   * Test response DTO
   */
  @lombok.Data
  @lombok.Builder
  @lombok.NoArgsConstructor
  @lombok.AllArgsConstructor
  @io.swagger.v3.oas.annotations.media.Schema(description = "Test response")
  public static class TestResponse {
    @io.swagger.v3.oas.annotations.media.Schema(description = "Test message")
    private String message;
    
    @io.swagger.v3.oas.annotations.media.Schema(description = "Response timestamp")
    private Instant timestamp;
    
    @io.swagger.v3.oas.annotations.media.Schema(description = "Service name")
    private String service;
    
    @io.swagger.v3.oas.annotations.media.Schema(description = "Service version")
    private String version;
  }
}