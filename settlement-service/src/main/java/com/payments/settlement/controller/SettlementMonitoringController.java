package com.payments.settlement.controller;

import com.payments.settlement.dto.SettlementMonitoringRequest;
import com.payments.settlement.dto.SettlementMonitoringResponse;
import com.payments.settlement.dto.SettlementAlertRequest;
import com.payments.settlement.dto.SettlementAlertResponse;
import com.payments.settlement.dto.SettlementMetricsRequest;
import com.payments.settlement.dto.SettlementMetricsResponse;
import com.payments.settlement.service.SettlementMonitoringService;
import com.payments.settlement.exception.SettlementMonitoringException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for settlement monitoring operations.
 *
 * <p>This controller provides REST endpoints for settlement
 * monitoring operations including monitoring, alerts, and
 * metrics management. It includes comprehensive API documentation
 * and error handling.
 *
 * @since PE-411
 */
@RestController
@RequestMapping("/api/v1/settlement/monitoring")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Settlement Monitoring", description = "Settlement monitoring and alerting operations")
public class SettlementMonitoringController {
  
  private final SettlementMonitoringService monitoringService;
  
  /**
   * Creates a new settlement monitoring entry.
   *
   * @param request the monitoring request
   * @return the created monitoring response
   */
  @PostMapping("/monitoring")
  @Operation(summary = "Create settlement monitoring entry", description = "Creates a new settlement monitoring entry")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Monitoring entry created successfully"),
      @ApiResponse(responseCode = "400", description = "Invalid request data"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public ResponseEntity<SettlementMonitoringResponse> createMonitoring(
      @Valid @RequestBody SettlementMonitoringRequest request) {
    try {
      log.info("Creating settlement monitoring entry: {}", request.getMonitoringName());
      
      SettlementMonitoringResponse response = monitoringService.createMonitoring(request);
      
      log.info("Successfully created settlement monitoring entry: {}", response.getMonitoringId());
      
      return ResponseEntity.status(HttpStatus.CREATED).body(response);
      
    } catch (SettlementMonitoringException e) {
      log.error("Failed to create settlement monitoring entry: {}", e.getMessage(), e);
      throw e;
    } catch (Exception e) {
      log.error("Unexpected error creating settlement monitoring entry: {}", e.getMessage(), e);
      throw new SettlementMonitoringException("Failed to create settlement monitoring entry", e);
    }
  }
  
  /**
   * Creates a new settlement alert.
   *
   * @param request the alert request
   * @return the created alert response
   */
  @PostMapping("/alerts")
  @Operation(summary = "Create settlement alert", description = "Creates a new settlement alert")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Alert created successfully"),
      @ApiResponse(responseCode = "400", description = "Invalid request data"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public ResponseEntity<SettlementAlertResponse> createAlert(
      @Valid @RequestBody SettlementAlertRequest request) {
    try {
      log.info("Creating settlement alert: {}", request.getAlertName());
      
      SettlementAlertResponse response = monitoringService.createAlert(request);
      
      log.info("Successfully created settlement alert: {}", response.getAlertId());
      
      return ResponseEntity.status(HttpStatus.CREATED).body(response);
      
    } catch (SettlementMonitoringException e) {
      log.error("Failed to create settlement alert: {}", e.getMessage(), e);
      throw e;
    } catch (Exception e) {
      log.error("Unexpected error creating settlement alert: {}", e.getMessage(), e);
      throw new SettlementMonitoringException("Failed to create settlement alert", e);
    }
  }
  
  /**
   * Creates a new settlement metrics entry.
   *
   * @param request the metrics request
   * @return the created metrics response
   */
  @PostMapping("/metrics")
  @Operation(summary = "Create settlement metrics entry", description = "Creates a new settlement metrics entry")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Metrics entry created successfully"),
      @ApiResponse(responseCode = "400", description = "Invalid request data"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public ResponseEntity<SettlementMetricsResponse> createMetrics(
      @Valid @RequestBody SettlementMetricsRequest request) {
    try {
      log.info("Creating settlement metrics entry: {}", request.getMetricsName());
      
      SettlementMetricsResponse response = monitoringService.createMetrics(request);
      
      log.info("Successfully created settlement metrics entry: {}", response.getMetricsId());
      
      return ResponseEntity.status(HttpStatus.CREATED).body(response);
      
    } catch (SettlementMonitoringException e) {
      log.error("Failed to create settlement metrics entry: {}", e.getMessage(), e);
      throw e;
    } catch (Exception e) {
      log.error("Unexpected error creating settlement metrics entry: {}", e.getMessage(), e);
      throw new SettlementMonitoringException("Failed to create settlement metrics entry", e);
    }
  }
  
  /**
   * Gets all monitoring entries for the current tenant.
   *
   * @return list of monitoring responses
   */
  @GetMapping("/monitoring")
  @Operation(summary = "Get all monitoring entries", description = "Retrieves all settlement monitoring entries for the current tenant")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Monitoring entries retrieved successfully"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public ResponseEntity<List<SettlementMonitoringResponse>> getAllMonitoring() {
    try {
      log.info("Retrieving all settlement monitoring entries");
      
      List<SettlementMonitoringResponse> responses = monitoringService.getAllMonitoring();
      
      log.info("Successfully retrieved {} settlement monitoring entries", responses.size());
      
      return ResponseEntity.ok(responses);
      
    } catch (SettlementMonitoringException e) {
      log.error("Failed to retrieve settlement monitoring entries: {}", e.getMessage(), e);
      throw e;
    } catch (Exception e) {
      log.error("Unexpected error retrieving settlement monitoring entries: {}", e.getMessage(), e);
      throw new SettlementMonitoringException("Failed to retrieve settlement monitoring entries", e);
    }
  }
  
  /**
   * Gets all alerts for the current tenant.
   *
   * @return list of alert responses
   */
  @GetMapping("/alerts")
  @Operation(summary = "Get all alerts", description = "Retrieves all settlement alerts for the current tenant")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Alerts retrieved successfully"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public ResponseEntity<List<SettlementAlertResponse>> getAllAlerts() {
    try {
      log.info("Retrieving all settlement alerts");
      
      List<SettlementAlertResponse> responses = monitoringService.getAllAlerts();
      
      log.info("Successfully retrieved {} settlement alerts", responses.size());
      
      return ResponseEntity.ok(responses);
      
    } catch (SettlementMonitoringException e) {
      log.error("Failed to retrieve settlement alerts: {}", e.getMessage(), e);
      throw e;
    } catch (Exception e) {
      log.error("Unexpected error retrieving settlement alerts: {}", e.getMessage(), e);
      throw new SettlementMonitoringException("Failed to retrieve settlement alerts", e);
    }
  }
  
  /**
   * Gets all metrics for the current tenant.
   *
   * @return list of metrics responses
   */
  @GetMapping("/metrics")
  @Operation(summary = "Get all metrics", description = "Retrieves all settlement metrics for the current tenant")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Metrics retrieved successfully"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public ResponseEntity<List<SettlementMetricsResponse>> getAllMetrics() {
    try {
      log.info("Retrieving all settlement metrics");
      
      List<SettlementMetricsResponse> responses = monitoringService.getAllMetrics();
      
      log.info("Successfully retrieved {} settlement metrics", responses.size());
      
      return ResponseEntity.ok(responses);
      
    } catch (SettlementMonitoringException e) {
      log.error("Failed to retrieve settlement metrics: {}", e.getMessage(), e);
      throw e;
    } catch (Exception e) {
      log.error("Unexpected error retrieving settlement metrics: {}", e.getMessage(), e);
      throw new SettlementMonitoringException("Failed to retrieve settlement metrics", e);
    }
  }
  
  /**
   * Acknowledges an alert.
   *
   * @param alertId the alert ID
   * @param acknowledgedBy the user acknowledging the alert
   * @return the updated alert response
   */
  @PutMapping("/alerts/{alertId}/acknowledge")
  @Operation(summary = "Acknowledge alert", description = "Acknowledges a settlement alert")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Alert acknowledged successfully"),
      @ApiResponse(responseCode = "404", description = "Alert not found"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public ResponseEntity<SettlementAlertResponse> acknowledgeAlert(
      @Parameter(description = "Alert ID") @PathVariable String alertId,
      @Parameter(description = "User acknowledging the alert") @RequestParam String acknowledgedBy) {
    try {
      log.info("Acknowledging settlement alert: {} by user: {}", alertId, acknowledgedBy);
      
      SettlementAlertResponse response = monitoringService.acknowledgeAlert(alertId, acknowledgedBy);
      
      log.info("Successfully acknowledged settlement alert: {}", alertId);
      
      return ResponseEntity.ok(response);
      
    } catch (SettlementMonitoringException e) {
      log.error("Failed to acknowledge settlement alert: {}", e.getMessage(), e);
      throw e;
    } catch (Exception e) {
      log.error("Unexpected error acknowledging settlement alert: {}", e.getMessage(), e);
      throw new SettlementMonitoringException("Failed to acknowledge settlement alert", e);
    }
  }
  
  /**
   * Resolves an alert.
   *
   * @param alertId the alert ID
   * @param resolvedBy the user resolving the alert
   * @param resolutionNotes the resolution notes
   * @return the updated alert response
   */
  @PutMapping("/alerts/{alertId}/resolve")
  @Operation(summary = "Resolve alert", description = "Resolves a settlement alert")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Alert resolved successfully"),
      @ApiResponse(responseCode = "404", description = "Alert not found"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public ResponseEntity<SettlementAlertResponse> resolveAlert(
      @Parameter(description = "Alert ID") @PathVariable String alertId,
      @Parameter(description = "User resolving the alert") @RequestParam String resolvedBy,
      @Parameter(description = "Resolution notes") @RequestParam String resolutionNotes) {
    try {
      log.info("Resolving settlement alert: {} by user: {}", alertId, resolvedBy);
      
      SettlementAlertResponse response = monitoringService.resolveAlert(alertId, resolvedBy, resolutionNotes);
      
      log.info("Successfully resolved settlement alert: {}", alertId);
      
      return ResponseEntity.ok(response);
      
    } catch (SettlementMonitoringException e) {
      log.error("Failed to resolve settlement alert: {}", e.getMessage(), e);
      throw e;
    } catch (Exception e) {
      log.error("Unexpected error resolving settlement alert: {}", e.getMessage(), e);
      throw new SettlementMonitoringException("Failed to resolve settlement alert", e);
    }
  }
}
