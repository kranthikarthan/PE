package com.payments.saga.api;

import com.payments.domain.shared.TenantContext;
import com.payments.saga.domain.Saga;
import com.payments.saga.domain.SagaId;
import com.payments.saga.service.SagaOrchestrator;
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

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Saga Management Controller
 * 
 * Provides management APIs for saga orchestration operations.
 * Enables operations teams to monitor, manage, and control sagas.
 * 
 * Base URL: /api/management/v1/sagas
 */
@Slf4j
@RestController
@RequestMapping("/api/management/v1/sagas")
@RequiredArgsConstructor
@Tag(name = "Saga Management", description = "Saga management and operations API")
public class SagaManagementController {

    private final SagaOrchestrator sagaOrchestrator;

    /**
     * Get all sagas with filtering and pagination
     * 
     * GET /api/management/v1/sagas
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('OPS_VIEWER', 'OPS_OPERATOR', 'OPS_ADMIN', 'PLATFORM_ADMIN')")
    @Operation(
        summary = "Get All Sagas",
        description = "Retrieve all sagas with filtering and pagination")
    @ApiResponses(
        value = {
            @ApiResponse(
                responseCode = "200",
                description = "Sagas retrieved successfully",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = SagaListResponse.class)))
        })
    public ResponseEntity<SagaListResponse> getAllSagas(
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Status filter") @RequestParam(required = false) String status,
            @Parameter(description = "Template filter") @RequestParam(required = false) String template,
            @Parameter(description = "Start date filter") @RequestParam(required = false) String startDate,
            @Parameter(description = "End date filter") @RequestParam(required = false) String endDate,
            @Parameter(description = "Tenant ID", required = true) @RequestHeader("X-Tenant-Id") String tenantId,
            @Parameter(description = "Business Unit ID", required = true) @RequestHeader("X-Business-Unit-Id") String businessUnitId) {
        
        log.info("Getting all sagas for tenant: {}, business unit: {}, page: {}, size: {}", 
                tenantId, businessUnitId, page, size);
        
        try {
            // This would be implemented in the orchestrator service
            List<SagaResponse> sagas = List.of(); // Mock implementation
            long totalCount = 0; // Mock implementation
            
            SagaListResponse response = SagaListResponse.builder()
                .sagas(sagas)
                .totalCount((int) totalCount)
                .page(page)
                .size(size)
                .build();
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Failed to retrieve sagas", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(SagaListResponse.builder()
                    .errorMessage("Failed to retrieve sagas")
                    .build());
        }
    }

    /**
     * Get saga details with full information
     * 
     * GET /api/management/v1/sagas/{sagaId}/details
     */
    @GetMapping(value = "/{sagaId}/details", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('OPS_VIEWER', 'OPS_OPERATOR', 'OPS_ADMIN', 'PLATFORM_ADMIN')")
    @Operation(
        summary = "Get Saga Details",
        description = "Retrieve detailed information about a specific saga")
    @ApiResponses(
        value = {
            @ApiResponse(
                responseCode = "200",
                description = "Saga details retrieved successfully",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = SagaDetailsResponse.class))),
            @ApiResponse(
                responseCode = "404",
                description = "Saga not found",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class)))
        })
    public ResponseEntity<SagaDetailsResponse> getSagaDetails(
            @Parameter(description = "Saga ID", required = true) @PathVariable("sagaId") String sagaId,
            @Parameter(description = "Tenant ID", required = true) @RequestHeader("X-Tenant-Id") String tenantId,
            @Parameter(description = "Business Unit ID", required = true) @RequestHeader("X-Business-Unit-Id") String businessUnitId) {
        
        log.info("Getting saga details for: {}, tenant: {}, business unit: {}", 
                sagaId, tenantId, businessUnitId);
        
        try {
            // This would be implemented in the orchestrator service
            SagaDetailsResponse response = SagaDetailsResponse.builder()
                .sagaId(sagaId)
                .status("RUNNING")
                .templateName("PaymentProcessingSaga")
                .paymentId("payment-123")
                .correlationId("corr-123")
                .startedAt(Instant.now().toString())
                .lastUpdatedAt(Instant.now().toString())
                .steps(List.of())
                .events(List.of())
                .build();
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Failed to retrieve saga details for: {}", sagaId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(SagaDetailsResponse.builder()
                    .errorMessage("Failed to retrieve saga details")
                    .build());
        }
    }

    /**
     * Resume a paused saga
     * 
     * POST /api/management/v1/sagas/{sagaId}/resume
     */
    @PostMapping(value = "/{sagaId}/resume", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('OPS_OPERATOR', 'OPS_ADMIN', 'PLATFORM_ADMIN')")
    @Operation(
        summary = "Resume Saga",
        description = "Resume a paused or failed saga")
    @ApiResponses(
        value = {
            @ApiResponse(
                responseCode = "200",
                description = "Saga resumed successfully",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = SagaResponse.class))),
            @ApiResponse(
                responseCode = "400",
                description = "Saga cannot be resumed",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(
                responseCode = "404",
                description = "Saga not found",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class)))
        })
    public ResponseEntity<SagaResponse> resumeSaga(
            @Parameter(description = "Saga ID", required = true) @PathVariable("sagaId") String sagaId,
            @Parameter(description = "Resume request", required = true) @RequestBody @Valid ResumeSagaRequest request,
            @Parameter(description = "User ID", required = true) @RequestHeader("X-User-ID") String userId,
            @Parameter(description = "Tenant ID", required = true) @RequestHeader("X-Tenant-Id") String tenantId,
            @Parameter(description = "Business Unit ID", required = true) @RequestHeader("X-Business-Unit-Id") String businessUnitId) {
        
        log.info("Resuming saga: {} by user: {}, tenant: {}, business unit: {}", 
                sagaId, userId, tenantId, businessUnitId);
        
        try {
            // This would be implemented in the orchestrator service
            SagaResponse response = SagaResponse.builder()
                .sagaId(sagaId)
                .status("RUNNING")
                .message("Saga resumed successfully")
                .build();
            
            log.info("Saga resumed successfully: {}", sagaId);
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            log.warn("Cannot resume saga: {} - {}", sagaId, e.getMessage());
            return ResponseEntity.badRequest()
                .body(SagaResponse.builder()
                    .errorMessage(e.getMessage())
                    .build());
                
        } catch (Exception e) {
            log.error("Failed to resume saga: {}", sagaId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(SagaResponse.builder()
                    .errorMessage("Saga resume failed")
                    .build());
        }
    }

    /**
     * Force complete a saga
     * 
     * POST /api/management/v1/sagas/{sagaId}/force-complete
     */
    @PostMapping(value = "/{sagaId}/force-complete", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('OPS_ADMIN', 'PLATFORM_ADMIN')")
    @Operation(
        summary = "Force Complete Saga",
        description = "Force complete a saga that is stuck or needs manual intervention")
    @ApiResponses(
        value = {
            @ApiResponse(
                responseCode = "200",
                description = "Saga force completed successfully",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = SagaResponse.class))),
            @ApiResponse(
                responseCode = "400",
                description = "Saga cannot be force completed",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(
                responseCode = "404",
                description = "Saga not found",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class)))
        })
    public ResponseEntity<SagaResponse> forceCompleteSaga(
            @Parameter(description = "Saga ID", required = true) @PathVariable("sagaId") String sagaId,
            @Parameter(description = "Force complete request", required = true) @RequestBody @Valid ForceCompleteSagaRequest request,
            @Parameter(description = "User ID", required = true) @RequestHeader("X-User-ID") String userId,
            @Parameter(description = "Tenant ID", required = true) @RequestHeader("X-Tenant-Id") String tenantId,
            @Parameter(description = "Business Unit ID", required = true) @RequestHeader("X-Business-Unit-Id") String businessUnitId) {
        
        log.info("Force completing saga: {} by user: {}, tenant: {}, business unit: {}", 
                sagaId, userId, tenantId, businessUnitId);
        
        try {
            // This would be implemented in the orchestrator service
            SagaResponse response = SagaResponse.builder()
                .sagaId(sagaId)
                .status("COMPLETED")
                .message("Saga force completed successfully")
                .build();
            
            log.info("Saga force completed successfully: {}", sagaId);
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            log.warn("Cannot force complete saga: {} - {}", sagaId, e.getMessage());
            return ResponseEntity.badRequest()
                .body(SagaResponse.builder()
                    .errorMessage(e.getMessage())
                    .build());
                
        } catch (Exception e) {
            log.error("Failed to force complete saga: {}", sagaId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(SagaResponse.builder()
                    .errorMessage("Saga force complete failed")
                    .build());
        }
    }

    /**
     * Get saga statistics
     * 
     * GET /api/management/v1/sagas/statistics
     */
    @GetMapping(value = "/statistics", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('OPS_VIEWER', 'OPS_OPERATOR', 'OPS_ADMIN', 'PLATFORM_ADMIN')")
    @Operation(
        summary = "Get Saga Statistics",
        description = "Retrieve statistics about sagas")
    @ApiResponses(
        value = {
            @ApiResponse(
                responseCode = "200",
                description = "Saga statistics retrieved successfully",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = SagaStatisticsResponse.class)))
        })
    public ResponseEntity<SagaStatisticsResponse> getSagaStatistics(
            @Parameter(description = "Tenant ID", required = true) @RequestHeader("X-Tenant-Id") String tenantId,
            @Parameter(description = "Business Unit ID", required = true) @RequestHeader("X-Business-Unit-Id") String businessUnitId) {
        
        log.info("Getting saga statistics for tenant: {}, business unit: {}", tenantId, businessUnitId);
        
        try {
            // This would be implemented in the orchestrator service
            SagaStatisticsResponse response = SagaStatisticsResponse.builder()
                .totalSagas(100)
                .runningSagas(25)
                .completedSagas(70)
                .failedSagas(5)
                .averageExecutionTime(120.5)
                .successRate(95.0)
                .build();
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Failed to retrieve saga statistics", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(SagaStatisticsResponse.builder()
                    .errorMessage("Failed to retrieve saga statistics")
                    .build());
        }
    }

    /**
     * Get failed sagas for repair
     * 
     * GET /api/management/v1/sagas/failed
     */
    @GetMapping(value = "/failed", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('OPS_VIEWER', 'OPS_OPERATOR', 'OPS_ADMIN', 'PLATFORM_ADMIN')")
    @Operation(
        summary = "Get Failed Sagas",
        description = "Retrieve list of failed sagas that need attention")
    @ApiResponses(
        value = {
            @ApiResponse(
                responseCode = "200",
                description = "Failed sagas retrieved successfully",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = FailedSagasResponse.class)))
        })
    public ResponseEntity<FailedSagasResponse> getFailedSagas(
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Tenant ID", required = true) @RequestHeader("X-Tenant-Id") String tenantId,
            @Parameter(description = "Business Unit ID", required = true) @RequestHeader("X-Business-Unit-Id") String businessUnitId) {
        
        log.info("Getting failed sagas for tenant: {}, business unit: {}, page: {}, size: {}", 
                tenantId, businessUnitId, page, size);
        
        try {
            // This would be implemented in the orchestrator service
            List<SagaResponse> failedSagas = List.of(); // Mock implementation
            
            FailedSagasResponse response = FailedSagasResponse.builder()
                .failedSagas(failedSagas)
                .totalCount(failedSagas.size())
                .page(page)
                .size(size)
                .build();
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Failed to retrieve failed sagas", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(FailedSagasResponse.builder()
                    .errorMessage("Failed to retrieve failed sagas")
                    .build());
        }
    }

    // DTOs for the API

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    @Schema(description = "Saga list response")
    public static class SagaListResponse {
        @Schema(description = "List of sagas")
        private List<SagaResponse> sagas;
        
        @Schema(description = "Total count of sagas")
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
    @Schema(description = "Saga details response")
    public static class SagaDetailsResponse {
        @Schema(description = "Saga ID")
        private String sagaId;
        
        @Schema(description = "Saga status")
        private String status;
        
        @Schema(description = "Template name")
        private String templateName;
        
        @Schema(description = "Payment ID")
        private String paymentId;
        
        @Schema(description = "Correlation ID")
        private String correlationId;
        
        @Schema(description = "Started at timestamp")
        private String startedAt;
        
        @Schema(description = "Last updated at timestamp")
        private String lastUpdatedAt;
        
        @Schema(description = "List of saga steps")
        private List<SagaStepResponse> steps;
        
        @Schema(description = "List of saga events")
        private List<SagaEventResponse> events;
        
        @Schema(description = "Error message if any")
        private String errorMessage;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    @Schema(description = "Resume saga request")
    public static class ResumeSagaRequest {
        @Schema(description = "Resume reason", required = true)
        private String reason;
        
        @Schema(description = "Force resume even if saga is not in resumable state")
        private Boolean forceResume = false;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    @Schema(description = "Force complete saga request")
    public static class ForceCompleteSagaRequest {
        @Schema(description = "Force complete reason", required = true)
        private String reason;
        
        @Schema(description = "Skip compensation steps")
        private Boolean skipCompensation = false;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    @Schema(description = "Saga statistics response")
    public static class SagaStatisticsResponse {
        @Schema(description = "Total number of sagas")
        private Integer totalSagas;
        
        @Schema(description = "Number of running sagas")
        private Integer runningSagas;
        
        @Schema(description = "Number of completed sagas")
        private Integer completedSagas;
        
        @Schema(description = "Number of failed sagas")
        private Integer failedSagas;
        
        @Schema(description = "Average execution time in seconds")
        private Double averageExecutionTime;
        
        @Schema(description = "Success rate percentage")
        private Double successRate;
        
        @Schema(description = "Error message if any")
        private String errorMessage;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    @Schema(description = "Failed sagas response")
    public static class FailedSagasResponse {
        @Schema(description = "List of failed sagas")
        private List<SagaResponse> failedSagas;
        
        @Schema(description = "Total count of failed sagas")
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
