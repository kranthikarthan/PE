package com.payments.paymentinitiation.api;

import com.payments.contracts.payment.PaymentInitiationResponse;
import com.payments.paymentinitiation.service.PaymentRepairService;
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
import java.util.Map;

/**
 * Payment Repair Controller
 * 
 * Provides repair and management APIs for payments in the payments engine.
 * Enables operations teams to retry, cancel, and manage failed payments.
 * 
 * Base URL: /api/repair/v1/payments
 */
@Slf4j
@RestController
@RequestMapping("/api/repair/v1/payments")
@RequiredArgsConstructor
@Tag(name = "Payment Repair", description = "Payment repair and management operations")
public class PaymentRepairController {

    private final PaymentRepairService paymentRepairService;

    /**
     * Retry a failed payment
     * 
     * POST /api/repair/v1/payments/{paymentId}/retry
     */
    @PostMapping(value = "/{paymentId}/retry", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('OPS_OPERATOR', 'OPS_ADMIN', 'PLATFORM_ADMIN')")
    @Operation(
        summary = "Retry Failed Payment",
        description = "Retry a failed payment with optional repair parameters")
    @ApiResponses(
        value = {
            @ApiResponse(
                responseCode = "200",
                description = "Payment retry initiated successfully",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = PaymentInitiationResponse.class))),
            @ApiResponse(
                responseCode = "400",
                description = "Invalid retry request",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(
                responseCode = "404",
                description = "Payment not found",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(
                responseCode = "409",
                description = "Payment cannot be retried",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class)))
        })
    public ResponseEntity<PaymentInitiationResponse> retryPayment(
            @Parameter(description = "Payment ID", required = true) @PathVariable("paymentId") String paymentId,
            @Parameter(description = "Retry request", required = true) @RequestBody @Valid RetryRequest retryRequest,
            @Parameter(description = "User ID", required = true) @RequestHeader("X-User-ID") String userId,
            @Parameter(description = "Correlation ID for tracing", required = true) @RequestHeader("X-Correlation-ID") String correlationId,
            @Parameter(description = "Tenant ID", required = true) @RequestHeader("X-Tenant-ID") String tenantId,
            @Parameter(description = "Business Unit ID", required = true) @RequestHeader("X-Business-Unit-ID") String businessUnitId) {
        
        log.info("Retrying payment: {} by user: {}, tenant: {}, correlation: {}", 
                paymentId, userId, tenantId, correlationId);
        
        try {
            PaymentInitiationResponse response = paymentRepairService.retryPayment(
                paymentId, retryRequest, userId, correlationId, tenantId, businessUnitId);
            
            log.info("Payment retry initiated successfully: {}", paymentId);
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            log.warn("Invalid retry request for payment: {} - {}", paymentId, e.getMessage());
            return ResponseEntity.badRequest()
                .body(PaymentInitiationResponse.builder().errorMessage(e.getMessage()).build());
                
        } catch (Exception e) {
            log.error("Failed to retry payment: {}", paymentId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(PaymentInitiationResponse.builder()
                    .errorMessage("Payment retry failed")
                    .build());
        }
    }

    /**
     * Cancel a payment
     * 
     * POST /api/repair/v1/payments/{paymentId}/cancel
     */
    @PostMapping(value = "/{paymentId}/cancel", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('OPS_OPERATOR', 'OPS_ADMIN', 'PLATFORM_ADMIN')")
    @Operation(
        summary = "Cancel Payment",
        description = "Cancel a payment that is in progress or pending")
    @ApiResponses(
        value = {
            @ApiResponse(
                responseCode = "200",
                description = "Payment cancelled successfully",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = PaymentInitiationResponse.class))),
            @ApiResponse(
                responseCode = "400",
                description = "Invalid cancel request",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(
                responseCode = "404",
                description = "Payment not found",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(
                responseCode = "409",
                description = "Payment cannot be cancelled",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class)))
        })
    public ResponseEntity<PaymentInitiationResponse> cancelPayment(
            @Parameter(description = "Payment ID", required = true) @PathVariable("paymentId") String paymentId,
            @Parameter(description = "Cancel request", required = true) @RequestBody @Valid CancelRequest cancelRequest,
            @Parameter(description = "User ID", required = true) @RequestHeader("X-User-ID") String userId,
            @Parameter(description = "Correlation ID for tracing", required = true) @RequestHeader("X-Correlation-ID") String correlationId,
            @Parameter(description = "Tenant ID", required = true) @RequestHeader("X-Tenant-ID") String tenantId,
            @Parameter(description = "Business Unit ID", required = true) @RequestHeader("X-Business-Unit-ID") String businessUnitId) {
        
        log.info("Cancelling payment: {} by user: {}, tenant: {}, correlation: {}", 
                paymentId, userId, tenantId, correlationId);
        
        try {
            PaymentInitiationResponse response = paymentRepairService.cancelPayment(
                paymentId, cancelRequest, userId, correlationId, tenantId, businessUnitId);
            
            log.info("Payment cancelled successfully: {}", paymentId);
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            log.warn("Invalid cancel request for payment: {} - {}", paymentId, e.getMessage());
            return ResponseEntity.badRequest()
                .body(PaymentInitiationResponse.builder().errorMessage(e.getMessage()).build());
                
        } catch (Exception e) {
            log.error("Failed to cancel payment: {}", paymentId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(PaymentInitiationResponse.builder()
                    .errorMessage("Payment cancellation failed")
                    .build());
        }
    }

    /**
     * Get payment repair history
     * 
     * GET /api/repair/v1/payments/{paymentId}/repair-history
     */
    @GetMapping(value = "/{paymentId}/repair-history", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('OPS_VIEWER', 'OPS_OPERATOR', 'OPS_ADMIN', 'PLATFORM_ADMIN')")
    @Operation(
        summary = "Get Payment Repair History",
        description = "Retrieve the repair history for a specific payment")
    @ApiResponses(
        value = {
            @ApiResponse(
                responseCode = "200",
                description = "Repair history retrieved successfully",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = RepairHistoryResponse.class))),
            @ApiResponse(
                responseCode = "404",
                description = "Payment not found",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class)))
        })
    public ResponseEntity<RepairHistoryResponse> getPaymentRepairHistory(
            @Parameter(description = "Payment ID", required = true) @PathVariable("paymentId") String paymentId,
            @Parameter(description = "Correlation ID for tracing", required = true) @RequestHeader("X-Correlation-ID") String correlationId,
            @Parameter(description = "Tenant ID", required = true) @RequestHeader("X-Tenant-ID") String tenantId,
            @Parameter(description = "Business Unit ID", required = true) @RequestHeader("X-Business-Unit-ID") String businessUnitId) {
        
        log.info("Retrieving repair history for payment: {}, tenant: {}, correlation: {}", 
                paymentId, tenantId, correlationId);
        
        try {
            List<RepairHistoryItem> repairHistory = paymentRepairService.getPaymentRepairHistory(
                paymentId, correlationId, tenantId, businessUnitId);
            
            RepairHistoryResponse response = RepairHistoryResponse.builder()
                .paymentId(paymentId)
                .repairHistory(repairHistory)
                .totalCount(repairHistory.size())
                .build();
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            log.warn("Payment not found: {}", e.getMessage());
            return ResponseEntity.notFound().build();
            
        } catch (Exception e) {
            log.error("Failed to retrieve repair history for payment: {}", paymentId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(RepairHistoryResponse.builder()
                    .errorMessage("Repair history retrieval failed")
                    .build());
        }
    }

    /**
     * Get failed payments for repair
     * 
     * GET /api/repair/v1/payments/failed
     */
    @GetMapping(value = "/failed", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('OPS_VIEWER', 'OPS_OPERATOR', 'OPS_ADMIN', 'PLATFORM_ADMIN')")
    @Operation(
        summary = "Get Failed Payments",
        description = "Retrieve list of failed payments that can be repaired")
    @ApiResponses(
        value = {
            @ApiResponse(
                responseCode = "200",
                description = "Failed payments retrieved successfully",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = FailedPaymentsResponse.class)))
        })
    public ResponseEntity<FailedPaymentsResponse> getFailedPayments(
            @Parameter(description = "Page number", required = false) @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", required = false) @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Correlation ID for tracing", required = true) @RequestHeader("X-Correlation-ID") String correlationId,
            @Parameter(description = "Tenant ID", required = true) @RequestHeader("X-Tenant-ID") String tenantId,
            @Parameter(description = "Business Unit ID", required = true) @RequestHeader("X-Business-Unit-ID") String businessUnitId) {
        
        log.info("Retrieving failed payments for tenant: {}, business unit: {}, correlation: {}", 
                tenantId, businessUnitId, correlationId);
        
        try {
            List<PaymentInitiationResponse> failedPayments = paymentRepairService.getFailedPayments(
                page, size, correlationId, tenantId, businessUnitId);
            
            FailedPaymentsResponse response = FailedPaymentsResponse.builder()
                .failedPayments(failedPayments)
                .totalCount(failedPayments.size())
                .page(page)
                .size(size)
                .build();
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Failed to retrieve failed payments", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(FailedPaymentsResponse.builder()
                    .errorMessage("Failed payments retrieval failed")
                    .build());
        }
    }

    /**
     * Bulk retry failed payments
     * 
     * POST /api/repair/v1/payments/bulk-retry
     */
    @PostMapping(value = "/bulk-retry", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('OPS_ADMIN', 'PLATFORM_ADMIN')")
    @Operation(
        summary = "Bulk Retry Failed Payments",
        description = "Retry multiple failed payments in bulk")
    @ApiResponses(
        value = {
            @ApiResponse(
                responseCode = "200",
                description = "Bulk retry initiated successfully",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = BulkRetryResponse.class)))
        })
    public ResponseEntity<BulkRetryResponse> bulkRetryPayments(
            @Parameter(description = "Bulk retry request", required = true) @RequestBody @Valid BulkRetryRequest bulkRetryRequest,
            @Parameter(description = "User ID", required = true) @RequestHeader("X-User-ID") String userId,
            @Parameter(description = "Correlation ID for tracing", required = true) @RequestHeader("X-Correlation-ID") String correlationId,
            @Parameter(description = "Tenant ID", required = true) @RequestHeader("X-Tenant-ID") String tenantId,
            @Parameter(description = "Business Unit ID", required = true) @RequestHeader("X-Business-Unit-ID") String businessUnitId) {
        
        log.info("Bulk retrying {} payments by user: {}, tenant: {}, correlation: {}", 
                bulkRetryRequest.getPaymentIds().size(), userId, tenantId, correlationId);
        
        try {
            BulkRetryResponse response = paymentRepairService.bulkRetryPayments(
                bulkRetryRequest, userId, correlationId, tenantId, businessUnitId);
            
            log.info("Bulk retry initiated for {} payments", response.getTotalProcessed());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Failed to bulk retry payments", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(BulkRetryResponse.builder()
                    .errorMessage("Bulk retry failed")
                    .build());
        }
    }

    // DTOs for the API

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    @Schema(description = "Payment retry request")
    public static class RetryRequest {
        @Schema(description = "Retry reason", required = true)
        private String reason;
        
        @Schema(description = "Force retry even if payment is not in failed state")
        private Boolean forceRetry = false;
        
        @Schema(description = "Additional repair parameters")
        private Map<String, Object> repairParameters;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    @Schema(description = "Payment cancel request")
    public static class CancelRequest {
        @Schema(description = "Cancel reason", required = true)
        private String reason;
        
        @Schema(description = "Force cancel even if payment is not in cancellable state")
        private Boolean forceCancel = false;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    @Schema(description = "Repair history item")
    public static class RepairHistoryItem {
        @Schema(description = "Repair action")
        private String action;
        
        @Schema(description = "Repair timestamp")
        private String timestamp;
        
        @Schema(description = "User who performed the repair")
        private String performedBy;
        
        @Schema(description = "Repair reason")
        private String reason;
        
        @Schema(description = "Repair result")
        private String result;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    @Schema(description = "Repair history response")
    public static class RepairHistoryResponse {
        @Schema(description = "Payment ID")
        private String paymentId;
        
        @Schema(description = "List of repair history items")
        private List<RepairHistoryItem> repairHistory;
        
        @Schema(description = "Total count of repair actions")
        private Integer totalCount;
        
        @Schema(description = "Error message if any")
        private String errorMessage;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    @Schema(description = "Failed payments response")
    public static class FailedPaymentsResponse {
        @Schema(description = "List of failed payments")
        private List<PaymentInitiationResponse> failedPayments;
        
        @Schema(description = "Total count of failed payments")
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
    @Schema(description = "Bulk retry request")
    public static class BulkRetryRequest {
        @Schema(description = "List of payment IDs to retry", required = true)
        private List<String> paymentIds;
        
        @Schema(description = "Bulk retry reason", required = true)
        private String reason;
        
        @Schema(description = "Force retry even if payments are not in failed state")
        private Boolean forceRetry = false;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    @Schema(description = "Bulk retry response")
    public static class BulkRetryResponse {
        @Schema(description = "Total payments processed")
        private Integer totalProcessed;
        
        @Schema(description = "Successfully retried payments")
        private Integer successCount;
        
        @Schema(description = "Failed retry attempts")
        private Integer failureCount;
        
        @Schema(description = "List of failed payment IDs")
        private List<String> failedPaymentIds;
        
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
