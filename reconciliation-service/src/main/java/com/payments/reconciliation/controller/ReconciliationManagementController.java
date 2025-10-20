package com.payments.reconciliation.controller;

import com.payments.reconciliation.service.ReconciliationManagementService;
import com.payments.reconciliation.service.ReconciliationManagementService.ReconciliationRunResult;
import com.payments.reconciliation.service.ReconciliationManagementService.ReconciliationExceptionResult;
import com.payments.reconciliation.service.ReconciliationManagementService.ReconciliationStatistics;
import com.payments.reconciliation.service.ReconciliationManagementService.ReconciliationReport;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Reconciliation Management Controller
 * 
 * Provides comprehensive reconciliation management and monitoring capabilities.
 * Enables operations teams to manage, monitor, and control reconciliation processes.
 * 
 * Base URL: /api/management/v1/reconciliation
 */
@Slf4j
@RestController
@RequestMapping("/api/management/v1/reconciliation")
@RequiredArgsConstructor
@Tag(name = "Reconciliation Management", description = "Reconciliation management and operations API")
public class ReconciliationManagementController {

    private final ReconciliationManagementService reconciliationManagementService;

    /**
     * Get all reconciliation runs with filtering and pagination
     * 
     * GET /api/management/v1/reconciliation/runs
     */
    @GetMapping(value = "/runs", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('OPS_VIEWER', 'OPS_OPERATOR', 'OPS_ADMIN', 'PLATFORM_ADMIN')")
    @Operation(
        summary = "Get All Reconciliation Runs",
        description = "Retrieve all reconciliation runs with filtering and pagination")
    @ApiResponses(
        value = {
            @ApiResponse(
                responseCode = "200",
                description = "Reconciliation runs retrieved successfully",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ReconciliationRunsResponse.class)))
        })
    public ResponseEntity<ReconciliationRunsResponse> getAllReconciliationRuns(
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Status filter") @RequestParam(required = false) String status,
            @Parameter(description = "Clearing system filter") @RequestParam(required = false) String clearingSystem,
            @Parameter(description = "Start date filter") @RequestParam(required = false) String startDate,
            @Parameter(description = "End date filter") @RequestParam(required = false) String endDate,
            @Parameter(description = "Tenant ID", required = true) @RequestHeader("X-Tenant-Id") String tenantId,
            @Parameter(description = "Business Unit ID", required = true) @RequestHeader("X-Business-Unit-Id") String businessUnitId) {
        
        log.info("Getting all reconciliation runs for tenant: {}, business unit: {}, page: {}, size: {}", 
                tenantId, businessUnitId, page, size);
        
        try {
            ReconciliationRunResult result = reconciliationManagementService.getAllReconciliationRuns(
                tenantId, businessUnitId, page, size, status, clearingSystem, startDate, endDate);
            
            ReconciliationRunsResponse response = ReconciliationRunsResponse.builder()
                .runs(result.getRuns())
                .totalCount(result.getTotalCount())
                .page(result.getPage())
                .size(result.getSize())
                .build();
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Failed to retrieve reconciliation runs", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ReconciliationRunsResponse.builder()
                    .errorMessage("Failed to retrieve reconciliation runs")
                    .build());
        }
    }

    /**
     * Get reconciliation run details
     * 
     * GET /api/management/v1/reconciliation/runs/{runId}/details
     */
    @GetMapping(value = "/runs/{runId}/details", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('OPS_VIEWER', 'OPS_OPERATOR', 'OPS_ADMIN', 'PLATFORM_ADMIN')")
    @Operation(
        summary = "Get Reconciliation Run Details",
        description = "Retrieve detailed information about a specific reconciliation run")
    @ApiResponses(
        value = {
            @ApiResponse(
                responseCode = "200",
                description = "Reconciliation run details retrieved successfully",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ReconciliationRunDetailsResponse.class))),
            @ApiResponse(
                responseCode = "404",
                description = "Reconciliation run not found",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class)))
        })
    public ResponseEntity<ReconciliationRunDetailsResponse> getReconciliationRunDetails(
            @Parameter(description = "Run ID", required = true) @PathVariable("runId") String runId,
            @Parameter(description = "Tenant ID", required = true) @RequestHeader("X-Tenant-Id") String tenantId,
            @Parameter(description = "Business Unit ID", required = true) @RequestHeader("X-Business-Unit-Id") String businessUnitId) {
        
        log.info("Getting reconciliation run details for: {}, tenant: {}, business unit: {}", 
                runId, tenantId, businessUnitId);
        
        try {
            ReconciliationRunDetails details = reconciliationManagementService.getReconciliationRunDetails(
                runId, tenantId, businessUnitId);
            
            ReconciliationRunDetailsResponse response = ReconciliationRunDetailsResponse.builder()
                .runId(details.getRunId())
                .status(details.getStatus())
                .clearingSystem(details.getClearingSystem())
                .startedAt(details.getStartedAt())
                .completedAt(details.getCompletedAt())
                .totalRecords(details.getTotalRecords())
                .matchedRecords(details.getMatchedRecords())
                .unmatchedRecords(details.getUnmatchedRecords())
                .exceptions(details.getExceptions())
                .build();
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            log.warn("Reconciliation run not found: {}", runId);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Failed to retrieve reconciliation run details for: {}", runId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ReconciliationRunDetailsResponse.builder()
                    .errorMessage("Failed to retrieve reconciliation run details")
                    .build());
        }
    }

    /**
     * Start a new reconciliation run
     * 
     * POST /api/management/v1/reconciliation/runs/start
     */
    @PostMapping(value = "/runs/start", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('OPS_OPERATOR', 'OPS_ADMIN', 'PLATFORM_ADMIN')")
    @Operation(
        summary = "Start Reconciliation Run",
        description = "Start a new reconciliation run for a specific clearing system")
    @ApiResponses(
        value = {
            @ApiResponse(
                responseCode = "200",
                description = "Reconciliation run started successfully",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ReconciliationRunResponse.class))),
            @ApiResponse(
                responseCode = "400",
                description = "Invalid request parameters",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class)))
        })
    public ResponseEntity<ReconciliationRunResponse> startReconciliationRun(
            @Parameter(description = "Start reconciliation request", required = true) @RequestBody @Valid StartReconciliationRequest request,
            @Parameter(description = "User ID", required = true) @RequestHeader("X-User-ID") String userId,
            @Parameter(description = "Tenant ID", required = true) @RequestHeader("X-Tenant-Id") String tenantId,
            @Parameter(description = "Business Unit ID", required = true) @RequestHeader("X-Business-Unit-Id") String businessUnitId) {
        
        log.info("Starting reconciliation run for clearing system: {} by user: {}, tenant: {}, business unit: {}", 
                request.getClearingSystem(), userId, tenantId, businessUnitId);
        
        try {
            ReconciliationRunResponse response = reconciliationManagementService.startReconciliationRun(
                request.getClearingSystem(), request.getDescription(), userId, tenantId, businessUnitId);
            
            log.info("Reconciliation run started successfully: {}", response.getRunId());
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            log.warn("Cannot start reconciliation run: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(ReconciliationRunResponse.builder()
                    .errorMessage(e.getMessage())
                    .build());
                
        } catch (Exception e) {
            log.error("Failed to start reconciliation run", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ReconciliationRunResponse.builder()
                    .errorMessage("Failed to start reconciliation run")
                    .build());
        }
    }

    /**
     * Stop a running reconciliation
     * 
     * POST /api/management/v1/reconciliation/runs/{runId}/stop
     */
    @PostMapping(value = "/runs/{runId}/stop", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('OPS_OPERATOR', 'OPS_ADMIN', 'PLATFORM_ADMIN')")
    @Operation(
        summary = "Stop Reconciliation Run",
        description = "Stop a running reconciliation process")
    @ApiResponses(
        value = {
            @ApiResponse(
                responseCode = "200",
                description = "Reconciliation run stopped successfully",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ReconciliationRunResponse.class))),
            @ApiResponse(
                responseCode = "400",
                description = "Reconciliation run cannot be stopped",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(
                responseCode = "404",
                description = "Reconciliation run not found",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class)))
        })
    public ResponseEntity<ReconciliationRunResponse> stopReconciliationRun(
            @Parameter(description = "Run ID", required = true) @PathVariable("runId") String runId,
            @Parameter(description = "Stop reconciliation request", required = true) @RequestBody @Valid StopReconciliationRequest request,
            @Parameter(description = "User ID", required = true) @RequestHeader("X-User-ID") String userId,
            @Parameter(description = "Tenant ID", required = true) @RequestHeader("X-Tenant-Id") String tenantId,
            @Parameter(description = "Business Unit ID", required = true) @RequestHeader("X-Business-Unit-Id") String businessUnitId) {
        
        log.info("Stopping reconciliation run: {} by user: {}, tenant: {}, business unit: {}", 
                runId, userId, tenantId, businessUnitId);
        
        try {
            ReconciliationRunResponse response = reconciliationManagementService.stopReconciliationRun(
                runId, request.getReason(), userId, tenantId, businessUnitId);
            
            log.info("Reconciliation run stopped successfully: {}", runId);
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            log.warn("Cannot stop reconciliation run: {} - {}", runId, e.getMessage());
            return ResponseEntity.badRequest()
                .body(ReconciliationRunResponse.builder()
                    .errorMessage(e.getMessage())
                    .build());
                
        } catch (Exception e) {
            log.error("Failed to stop reconciliation run: {}", runId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ReconciliationRunResponse.builder()
                    .errorMessage("Failed to stop reconciliation run")
                    .build());
        }
    }

    /**
     * Get reconciliation exceptions
     * 
     * GET /api/management/v1/reconciliation/exceptions
     */
    @GetMapping(value = "/exceptions", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('OPS_VIEWER', 'OPS_OPERATOR', 'OPS_ADMIN', 'PLATFORM_ADMIN')")
    @Operation(
        summary = "Get Reconciliation Exceptions",
        description = "Retrieve reconciliation exceptions with filtering and pagination")
    @ApiResponses(
        value = {
            @ApiResponse(
                responseCode = "200",
                description = "Reconciliation exceptions retrieved successfully",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ReconciliationExceptionsResponse.class)))
        })
    public ResponseEntity<ReconciliationExceptionsResponse> getReconciliationExceptions(
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Status filter") @RequestParam(required = false) String status,
            @Parameter(description = "Severity filter") @RequestParam(required = false) String severity,
            @Parameter(description = "Clearing system filter") @RequestParam(required = false) String clearingSystem,
            @Parameter(description = "Tenant ID", required = true) @RequestHeader("X-Tenant-Id") String tenantId,
            @Parameter(description = "Business Unit ID", required = true) @RequestHeader("X-Business-Unit-Id") String businessUnitId) {
        
        log.info("Getting reconciliation exceptions for tenant: {}, business unit: {}, page: {}, size: {}", 
                tenantId, businessUnitId, page, size);
        
        try {
            ReconciliationExceptionResult result = reconciliationManagementService.getReconciliationExceptions(
                tenantId, businessUnitId, page, size, status, severity, clearingSystem);
            
            ReconciliationExceptionsResponse response = ReconciliationExceptionsResponse.builder()
                .exceptions(result.getExceptions())
                .totalCount(result.getTotalCount())
                .page(result.getPage())
                .size(result.getSize())
                .build();
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Failed to retrieve reconciliation exceptions", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ReconciliationExceptionsResponse.builder()
                    .errorMessage("Failed to retrieve reconciliation exceptions")
                    .build());
        }
    }

    /**
     * Resolve reconciliation exception
     * 
     * POST /api/management/v1/reconciliation/exceptions/{exceptionId}/resolve
     */
    @PostMapping(value = "/exceptions/{exceptionId}/resolve", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('OPS_OPERATOR', 'OPS_ADMIN', 'PLATFORM_ADMIN')")
    @Operation(
        summary = "Resolve Reconciliation Exception",
        description = "Resolve a reconciliation exception with resolution details")
    @ApiResponses(
        value = {
            @ApiResponse(
                responseCode = "200",
                description = "Reconciliation exception resolved successfully",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ReconciliationExceptionResponse.class))),
            @ApiResponse(
                responseCode = "400",
                description = "Invalid resolution request",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(
                responseCode = "404",
                description = "Reconciliation exception not found",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class)))
        })
    public ResponseEntity<ReconciliationExceptionResponse> resolveReconciliationException(
            @Parameter(description = "Exception ID", required = true) @PathVariable("exceptionId") String exceptionId,
            @Parameter(description = "Resolve exception request", required = true) @RequestBody @Valid ResolveExceptionRequest request,
            @Parameter(description = "User ID", required = true) @RequestHeader("X-User-ID") String userId,
            @Parameter(description = "Tenant ID", required = true) @RequestHeader("X-Tenant-Id") String tenantId,
            @Parameter(description = "Business Unit ID", required = true) @RequestHeader("X-Business-Unit-Id") String businessUnitId) {
        
        log.info("Resolving reconciliation exception: {} by user: {}, tenant: {}, business unit: {}", 
                exceptionId, userId, tenantId, businessUnitId);
        
        try {
            ReconciliationExceptionResponse response = reconciliationManagementService.resolveReconciliationException(
                exceptionId, request.getResolution(), request.getNotes(), userId, tenantId, businessUnitId);
            
            log.info("Reconciliation exception resolved successfully: {}", exceptionId);
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            log.warn("Cannot resolve reconciliation exception: {} - {}", exceptionId, e.getMessage());
            return ResponseEntity.badRequest()
                .body(ReconciliationExceptionResponse.builder()
                    .errorMessage(e.getMessage())
                    .build());
                
        } catch (Exception e) {
            log.error("Failed to resolve reconciliation exception: {}", exceptionId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ReconciliationExceptionResponse.builder()
                    .errorMessage("Failed to resolve reconciliation exception")
                    .build());
        }
    }

    /**
     * Get reconciliation statistics
     * 
     * GET /api/management/v1/reconciliation/statistics
     */
    @GetMapping(value = "/statistics", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('OPS_VIEWER', 'OPS_OPERATOR', 'OPS_ADMIN', 'PLATFORM_ADMIN')")
    @Operation(
        summary = "Get Reconciliation Statistics",
        description = "Retrieve statistics about reconciliation processes")
    @ApiResponses(
        value = {
            @ApiResponse(
                responseCode = "200",
                description = "Reconciliation statistics retrieved successfully",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ReconciliationStatisticsResponse.class)))
        })
    public ResponseEntity<ReconciliationStatisticsResponse> getReconciliationStatistics(
            @Parameter(description = "Start date") @RequestParam(required = false) String startDate,
            @Parameter(description = "End date") @RequestParam(required = false) String endDate,
            @Parameter(description = "Clearing system") @RequestParam(required = false) String clearingSystem,
            @Parameter(description = "Tenant ID", required = true) @RequestHeader("X-Tenant-Id") String tenantId,
            @Parameter(description = "Business Unit ID", required = true) @RequestHeader("X-Business-Unit-Id") String businessUnitId) {
        
        log.info("Getting reconciliation statistics for tenant: {}, business unit: {}", tenantId, businessUnitId);
        
        try {
            ReconciliationStatistics stats = reconciliationManagementService.getReconciliationStatistics(
                tenantId, businessUnitId, startDate, endDate, clearingSystem);
            
            ReconciliationStatisticsResponse response = ReconciliationStatisticsResponse.builder()
                .totalRuns(stats.getTotalRuns())
                .successfulRuns(stats.getSuccessfulRuns())
                .failedRuns(stats.getFailedRuns())
                .runningRuns(stats.getRunningRuns())
                .totalExceptions(stats.getTotalExceptions())
                .resolvedExceptions(stats.getResolvedExceptions())
                .unresolvedExceptions(stats.getUnresolvedExceptions())
                .averageProcessingTime(stats.getAverageProcessingTime())
                .successRate(stats.getSuccessRate())
                .build();
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Failed to retrieve reconciliation statistics", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ReconciliationStatisticsResponse.builder()
                    .errorMessage("Failed to retrieve reconciliation statistics")
                    .build());
        }
    }

    /**
     * Generate reconciliation report
     * 
     * GET /api/management/v1/reconciliation/reports/generate
     */
    @GetMapping(value = "/reports/generate", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('OPS_OPERATOR', 'OPS_ADMIN', 'PLATFORM_ADMIN')")
    @Operation(
        summary = "Generate Reconciliation Report",
        description = "Generate a comprehensive reconciliation report")
    @ApiResponses(
        value = {
            @ApiResponse(
                responseCode = "200",
                description = "Reconciliation report generated successfully",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ReconciliationReportResponse.class)))
        })
    public ResponseEntity<ReconciliationReportResponse> generateReconciliationReport(
            @Parameter(description = "Start date") @RequestParam(required = false) String startDate,
            @Parameter(description = "End date") @RequestParam(required = false) String endDate,
            @Parameter(description = "Clearing system") @RequestParam(required = false) String clearingSystem,
            @Parameter(description = "Report format") @RequestParam(defaultValue = "JSON") String format,
            @Parameter(description = "User ID", required = true) @RequestHeader("X-User-ID") String userId,
            @Parameter(description = "Tenant ID", required = true) @RequestHeader("X-Tenant-Id") String tenantId,
            @Parameter(description = "Business Unit ID", required = true) @RequestHeader("X-Business-Unit-Id") String businessUnitId) {
        
        log.info("Generating reconciliation report for tenant: {}, business unit: {}, format: {}", 
                tenantId, businessUnitId, format);
        
        try {
            ReconciliationReport report = reconciliationManagementService.generateReconciliationReport(
                tenantId, businessUnitId, startDate, endDate, clearingSystem, format, userId);
            
            ReconciliationReportResponse response = ReconciliationReportResponse.builder()
                .reportId(report.getReportId())
                .format(report.getFormat())
                .generatedAt(report.getGeneratedAt())
                .summary(report.getSummary())
                .details(report.getDetails())
                .build();
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Failed to generate reconciliation report", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ReconciliationReportResponse.builder()
                    .errorMessage("Failed to generate reconciliation report")
                    .build());
        }
    }

    // DTOs for the API

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    @Schema(description = "Reconciliation runs response")
    public static class ReconciliationRunsResponse {
        @Schema(description = "List of reconciliation runs")
        private List<ReconciliationRunSummary> runs;
        
        @Schema(description = "Total count of runs")
        private Integer totalCount;
        
        @Schema(description = "Page number")
        private Integer page;
        
        @Schema(description = "Page size")
        private Integer size;
        
        @Schema(description = "Error message if any")
        private String errorMessage;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    @Schema(description = "Reconciliation run summary")
    public static class ReconciliationRunSummary {
        @Schema(description = "Run ID")
        private String runId;
        
        @Schema(description = "Status")
        private String status;
        
        @Schema(description = "Clearing system")
        private String clearingSystem;
        
        @Schema(description = "Started at")
        private String startedAt;
        
        @Schema(description = "Completed at")
        private String completedAt;
        
        @Schema(description = "Total records")
        private Integer totalRecords;
        
        @Schema(description = "Matched records")
        private Integer matchedRecords;
        
        @Schema(description = "Unmatched records")
        private Integer unmatchedRecords;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    @Schema(description = "Reconciliation run details response")
    public static class ReconciliationRunDetailsResponse {
        @Schema(description = "Run ID")
        private String runId;
        
        @Schema(description = "Status")
        private String status;
        
        @Schema(description = "Clearing system")
        private String clearingSystem;
        
        @Schema(description = "Started at")
        private String startedAt;
        
        @Schema(description = "Completed at")
        private String completedAt;
        
        @Schema(description = "Total records")
        private Integer totalRecords;
        
        @Schema(description = "Matched records")
        private Integer matchedRecords;
        
        @Schema(description = "Unmatched records")
        private Integer unmatchedRecords;
        
        @Schema(description = "Exceptions")
        private List<ReconciliationExceptionSummary> exceptions;
        
        @Schema(description = "Error message if any")
        private String errorMessage;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    @Schema(description = "Reconciliation run response")
    public static class ReconciliationRunResponse {
        @Schema(description = "Run ID")
        private String runId;
        
        @Schema(description = "Status")
        private String status;
        
        @Schema(description = "Message")
        private String message;
        
        @Schema(description = "Error message if any")
        private String errorMessage;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    @Schema(description = "Start reconciliation request")
    public static class StartReconciliationRequest {
        @Schema(description = "Clearing system", required = true)
        private String clearingSystem;
        
        @Schema(description = "Description")
        private String description;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    @Schema(description = "Stop reconciliation request")
    public static class StopReconciliationRequest {
        @Schema(description = "Stop reason", required = true)
        private String reason;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    @Schema(description = "Reconciliation exceptions response")
    public static class ReconciliationExceptionsResponse {
        @Schema(description = "List of exceptions")
        private List<ReconciliationExceptionSummary> exceptions;
        
        @Schema(description = "Total count of exceptions")
        private Integer totalCount;
        
        @Schema(description = "Page number")
        private Integer page;
        
        @Schema(description = "Page size")
        private Integer size;
        
        @Schema(description = "Error message if any")
        private String errorMessage;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    @Schema(description = "Reconciliation exception summary")
    public static class ReconciliationExceptionSummary {
        @Schema(description = "Exception ID")
        private String exceptionId;
        
        @Schema(description = "Type")
        private String type;
        
        @Schema(description = "Severity")
        private String severity;
        
        @Schema(description = "Status")
        private String status;
        
        @Schema(description = "Description")
        private String description;
        
        @Schema(description = "Created at")
        private String createdAt;
        
        @Schema(description = "Resolved at")
        private String resolvedAt;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    @Schema(description = "Reconciliation exception response")
    public static class ReconciliationExceptionResponse {
        @Schema(description = "Exception ID")
        private String exceptionId;
        
        @Schema(description = "Status")
        private String status;
        
        @Schema(description = "Message")
        private String message;
        
        @Schema(description = "Error message if any")
        private String errorMessage;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    @Schema(description = "Resolve exception request")
    public static class ResolveExceptionRequest {
        @Schema(description = "Resolution", required = true)
        private String resolution;
        
        @Schema(description = "Notes")
        private String notes;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    @Schema(description = "Reconciliation statistics response")
    public static class ReconciliationStatisticsResponse {
        @Schema(description = "Total runs")
        private Integer totalRuns;
        
        @Schema(description = "Successful runs")
        private Integer successfulRuns;
        
        @Schema(description = "Failed runs")
        private Integer failedRuns;
        
        @Schema(description = "Running runs")
        private Integer runningRuns;
        
        @Schema(description = "Total exceptions")
        private Integer totalExceptions;
        
        @Schema(description = "Resolved exceptions")
        private Integer resolvedExceptions;
        
        @Schema(description = "Unresolved exceptions")
        private Integer unresolvedExceptions;
        
        @Schema(description = "Average processing time in seconds")
        private Double averageProcessingTime;
        
        @Schema(description = "Success rate percentage")
        private Double successRate;
        
        @Schema(description = "Error message if any")
        private String errorMessage;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    @Schema(description = "Reconciliation report response")
    public static class ReconciliationReportResponse {
        @Schema(description = "Report ID")
        private String reportId;
        
        @Schema(description = "Format")
        private String format;
        
        @Schema(description = "Generated at")
        private String generatedAt;
        
        @Schema(description = "Summary")
        private java.util.Map<String, Object> summary;
        
        @Schema(description = "Details")
        private java.util.Map<String, Object> details;
        
        @Schema(description = "Error message if any")
        private String errorMessage;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    @Schema(description = "Error response")
    public static class ErrorResponse {
        @Schema(description = "Error message")
        private String message;
        
        @Schema(description = "Error code")
        private String code;
        
        @Schema(description = "Timestamp")
        private String timestamp;
    }
}
