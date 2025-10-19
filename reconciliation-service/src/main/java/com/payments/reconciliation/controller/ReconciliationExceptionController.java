package com.payments.reconciliation.controller;

import com.payments.reconciliation.service.ReconciliationExceptionService;
import com.payments.reconciliation.dto.ReconciliationExceptionRequest;
import com.payments.reconciliation.dto.ReconciliationExceptionResponse;
import com.payments.reconciliation.dto.ReconciliationExceptionResolutionRequest;
import com.payments.reconciliation.exception.ReconciliationExceptionServiceException;
import com.payments.domain.reconciliation.ReconciliationException;
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
 * REST controller for reconciliation exception operations.
 *
 * <p>This controller provides REST endpoints for reconciliation
 * exception management including creation, assignment, resolution,
 * and tracking. It includes comprehensive API documentation
 * and error handling.
 *
 * @since PE-412
 */
@RestController
@RequestMapping("/api/v1/reconciliation/exceptions")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Reconciliation Exceptions", description = "Reconciliation exception management operations")
public class ReconciliationExceptionController {
  
  private final ReconciliationExceptionService exceptionService;
  
  /**
   * Creates a new reconciliation exception.
   *
   * @param request the exception request
   * @return the created exception response
   */
  @PostMapping
  @Operation(summary = "Create reconciliation exception", description = "Creates a new reconciliation exception")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Exception created successfully"),
      @ApiResponse(responseCode = "400", description = "Invalid request data"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public ResponseEntity<ReconciliationExceptionResponse> createException(
      @Valid @RequestBody ReconciliationExceptionRequest request) {
    try {
      log.info("Creating reconciliation exception for run: {}", request.getRunId());
      
      ReconciliationExceptionResponse response = exceptionService.createException(request);
      
      log.info("Successfully created reconciliation exception: {}", response.getExceptionId());
      
      return ResponseEntity.status(HttpStatus.CREATED).body(response);
      
    } catch (ReconciliationExceptionServiceException e) {
      log.error("Failed to create reconciliation exception: {}", e.getMessage(), e);
      throw e;
    } catch (Exception e) {
      log.error("Unexpected error creating reconciliation exception: {}", e.getMessage(), e);
      throw new ReconciliationExceptionServiceException("Failed to create reconciliation exception", e);
    }
  }
  
  /**
   * Assigns an exception to a user.
   *
   * @param exceptionId the exception ID
   * @param assignedTo the user to assign to
   * @return the updated exception response
   */
  @PutMapping("/{exceptionId}/assign")
  @Operation(summary = "Assign exception", description = "Assigns a reconciliation exception to a user")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Exception assigned successfully"),
      @ApiResponse(responseCode = "404", description = "Exception not found"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public ResponseEntity<ReconciliationExceptionResponse> assignException(
      @Parameter(description = "Exception ID") @PathVariable String exceptionId,
      @Parameter(description = "User to assign to") @RequestParam String assignedTo) {
    try {
      log.info("Assigning reconciliation exception: {} to user: {}", exceptionId, assignedTo);
      
      ReconciliationExceptionResponse response = exceptionService.assignException(exceptionId, assignedTo);
      
      log.info("Successfully assigned reconciliation exception: {}", exceptionId);
      
      return ResponseEntity.ok(response);
      
    } catch (ReconciliationExceptionServiceException e) {
      log.error("Failed to assign reconciliation exception: {}", e.getMessage(), e);
      throw e;
    } catch (Exception e) {
      log.error("Unexpected error assigning reconciliation exception: {}", e.getMessage(), e);
      throw new ReconciliationExceptionServiceException("Failed to assign reconciliation exception", e);
    }
  }
  
  /**
   * Resolves an exception.
   *
   * @param exceptionId the exception ID
   * @param request the resolution request
   * @return the updated exception response
   */
  @PutMapping("/{exceptionId}/resolve")
  @Operation(summary = "Resolve exception", description = "Resolves a reconciliation exception")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Exception resolved successfully"),
      @ApiResponse(responseCode = "404", description = "Exception not found"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public ResponseEntity<ReconciliationExceptionResponse> resolveException(
      @Parameter(description = "Exception ID") @PathVariable String exceptionId,
      @Valid @RequestBody ReconciliationExceptionResolutionRequest request) {
    try {
      log.info("Resolving reconciliation exception: {}", exceptionId);
      
      ReconciliationExceptionResponse response = exceptionService.resolveException(exceptionId, request);
      
      log.info("Successfully resolved reconciliation exception: {}", exceptionId);
      
      return ResponseEntity.ok(response);
      
    } catch (ReconciliationExceptionServiceException e) {
      log.error("Failed to resolve reconciliation exception: {}", e.getMessage(), e);
      throw e;
    } catch (Exception e) {
      log.error("Unexpected error resolving reconciliation exception: {}", e.getMessage(), e);
      throw new ReconciliationExceptionServiceException("Failed to resolve reconciliation exception", e);
    }
  }
  
  /**
   * Gets all exceptions for a reconciliation run.
   *
   * @param runId the reconciliation run ID
   * @return list of exception responses
   */
  @GetMapping("/runs/{runId}")
  @Operation(summary = "Get exceptions by run", description = "Retrieves all exceptions for a reconciliation run")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Exceptions retrieved successfully"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public ResponseEntity<List<ReconciliationExceptionResponse>> getExceptionsByRunId(
      @Parameter(description = "Reconciliation run ID") @PathVariable Long runId) {
    try {
      log.info("Retrieving reconciliation exceptions for run: {}", runId);
      
      List<ReconciliationExceptionResponse> responses = exceptionService.getExceptionsByRunId(runId);
      
      log.info("Successfully retrieved {} reconciliation exceptions for run: {}", responses.size(), runId);
      
      return ResponseEntity.ok(responses);
      
    } catch (ReconciliationExceptionServiceException e) {
      log.error("Failed to retrieve reconciliation exceptions by run: {}", e.getMessage(), e);
      throw e;
    } catch (Exception e) {
      log.error("Unexpected error retrieving reconciliation exceptions by run: {}", e.getMessage(), e);
      throw new ReconciliationExceptionServiceException("Failed to retrieve reconciliation exceptions by run", e);
    }
  }
  
  /**
   * Gets all exceptions for the current tenant.
   *
   * @return list of exception responses
   */
  @GetMapping
  @Operation(summary = "Get all exceptions", description = "Retrieves all reconciliation exceptions for the current tenant")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Exceptions retrieved successfully"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public ResponseEntity<List<ReconciliationExceptionResponse>> getAllExceptions() {
    try {
      log.info("Retrieving all reconciliation exceptions");
      
      List<ReconciliationExceptionResponse> responses = exceptionService.getAllExceptions();
      
      log.info("Successfully retrieved {} reconciliation exceptions", responses.size());
      
      return ResponseEntity.ok(responses);
      
    } catch (ReconciliationExceptionServiceException e) {
      log.error("Failed to retrieve reconciliation exceptions: {}", e.getMessage(), e);
      throw e;
    } catch (Exception e) {
      log.error("Unexpected error retrieving reconciliation exceptions: {}", e.getMessage(), e);
      throw new ReconciliationExceptionServiceException("Failed to retrieve reconciliation exceptions", e);
    }
  }
  
  /**
   * Gets exceptions by status.
   *
   * @param status the exception status
   * @return list of exception responses
   */
  @GetMapping("/status/{status}")
  @Operation(summary = "Get exceptions by status", description = "Retrieves reconciliation exceptions by status")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Exceptions retrieved successfully"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public ResponseEntity<List<ReconciliationExceptionResponse>> getExceptionsByStatus(
      @Parameter(description = "Exception status") @PathVariable ReconciliationException.ExceptionStatus status) {
    try {
      log.info("Retrieving reconciliation exceptions by status: {}", status);
      
      List<ReconciliationExceptionResponse> responses = exceptionService.getExceptionsByStatus(status);
      
      log.info("Successfully retrieved {} reconciliation exceptions by status: {}", responses.size(), status);
      
      return ResponseEntity.ok(responses);
      
    } catch (ReconciliationExceptionServiceException e) {
      log.error("Failed to retrieve reconciliation exceptions by status: {}", e.getMessage(), e);
      throw e;
    } catch (Exception e) {
      log.error("Unexpected error retrieving reconciliation exceptions by status: {}", e.getMessage(), e);
      throw new ReconciliationExceptionServiceException("Failed to retrieve reconciliation exceptions by status", e);
    }
  }
  
  /**
   * Gets exceptions assigned to a user.
   *
   * @param assignedTo the assigned user
   * @return list of exception responses
   */
  @GetMapping("/assigned/{assignedTo}")
  @Operation(summary = "Get exceptions by assigned user", description = "Retrieves reconciliation exceptions assigned to a user")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Exceptions retrieved successfully"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public ResponseEntity<List<ReconciliationExceptionResponse>> getExceptionsByAssignedTo(
      @Parameter(description = "Assigned user") @PathVariable String assignedTo) {
    try {
      log.info("Retrieving reconciliation exceptions assigned to: {}", assignedTo);
      
      List<ReconciliationExceptionResponse> responses = exceptionService.getExceptionsByAssignedTo(assignedTo);
      
      log.info("Successfully retrieved {} reconciliation exceptions assigned to: {}", responses.size(), assignedTo);
      
      return ResponseEntity.ok(responses);
      
    } catch (ReconciliationExceptionServiceException e) {
      log.error("Failed to retrieve reconciliation exceptions by assigned user: {}", e.getMessage(), e);
      throw e;
    } catch (Exception e) {
      log.error("Unexpected error retrieving reconciliation exceptions by assigned user: {}", e.getMessage(), e);
      throw new ReconciliationExceptionServiceException("Failed to retrieve reconciliation exceptions by assigned user", e);
    }
  }
  
  /**
   * Gets exception statistics for a reconciliation run.
   *
   * @param runId the reconciliation run ID
   * @return exception statistics
   */
  @GetMapping("/runs/{runId}/statistics")
  @Operation(summary = "Get exception statistics", description = "Retrieves exception statistics for a reconciliation run")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public ResponseEntity<ReconciliationExceptionService.ReconciliationExceptionStatistics> getExceptionStatistics(
      @Parameter(description = "Reconciliation run ID") @PathVariable Long runId) {
    try {
      log.info("Retrieving exception statistics for run: {}", runId);
      
      ReconciliationExceptionService.ReconciliationExceptionStatistics statistics = exceptionService.getExceptionStatistics(runId);
      
      log.info("Successfully retrieved exception statistics for run: {}", runId);
      
      return ResponseEntity.ok(statistics);
      
    } catch (ReconciliationExceptionServiceException e) {
      log.error("Failed to retrieve exception statistics: {}", e.getMessage(), e);
      throw e;
    } catch (Exception e) {
      log.error("Unexpected error retrieving exception statistics: {}", e.getMessage(), e);
      throw new ReconciliationExceptionServiceException("Failed to retrieve exception statistics", e);
    }
  }
}
