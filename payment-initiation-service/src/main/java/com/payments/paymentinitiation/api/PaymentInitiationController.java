package com.payments.paymentinitiation.api;

import com.payments.contracts.payment.PaymentInitiationRequest;
import com.payments.contracts.payment.PaymentInitiationResponse;
import com.payments.paymentinitiation.service.PaymentInitiationService;
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
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for Payment Initiation
 * 
 * Provides endpoints for:
 * - Initiating payments
 * - Retrieving payment status
 * - Health checks
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Tag(name = "Payment Initiation", description = "Payment initiation and status management")
public class PaymentInitiationController {

    private final PaymentInitiationService paymentInitiationService;

    @PostMapping(
        value = "/initiate",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Operation(
        summary = "Initiate Payment",
        description = "Initiate a new payment with validation and idempotency"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Payment initiated successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = PaymentInitiationResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid request or validation failed",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "409",
            description = "Duplicate payment (idempotency key already used)",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        )
    })
    public ResponseEntity<PaymentInitiationResponse> initiatePayment(
        @Parameter(description = "Payment initiation request", required = true)
        @Valid @RequestBody PaymentInitiationRequest request,
        
        @Parameter(description = "Correlation ID for tracing", required = true)
        @RequestHeader("X-Correlation-ID") String correlationId,
        
        @Parameter(description = "Tenant ID", required = true)
        @RequestHeader("X-Tenant-ID") String tenantId,
        
        @Parameter(description = "Business Unit ID", required = true)
        @RequestHeader("X-Business-Unit-ID") String businessUnitId
    ) {
        log.info("Initiating payment for tenant: {}, business unit: {}, correlation: {}", 
                tenantId, businessUnitId, correlationId);
        
        try {
            PaymentInitiationResponse response = paymentInitiationService.initiatePayment(
                request, correlationId, tenantId, businessUnitId
            );
            
            log.info("Payment initiated successfully: {}", response.getPaymentId());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (IllegalArgumentException e) {
            log.warn("Invalid payment request: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(PaymentInitiationResponse.builder()
                    .errorMessage(e.getMessage())
                    .build());
                    
        } catch (Exception e) {
            log.error("Failed to initiate payment", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(PaymentInitiationResponse.builder()
                    .errorMessage("Payment initiation failed")
                    .build());
        }
    }

    @GetMapping(
        value = "/{paymentId}/status",
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Operation(
        summary = "Get Payment Status",
        description = "Retrieve the current status of a payment"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Payment status retrieved successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = PaymentInitiationResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Payment not found",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        )
    })
    public ResponseEntity<PaymentInitiationResponse> getPaymentStatus(
        @Parameter(description = "Payment ID", required = true)
        @PathVariable String paymentId,
        
        @Parameter(description = "Correlation ID for tracing", required = true)
        @RequestHeader("X-Correlation-ID") String correlationId,
        
        @Parameter(description = "Tenant ID", required = true)
        @RequestHeader("X-Tenant-ID") String tenantId,
        
        @Parameter(description = "Business Unit ID", required = true)
        @RequestHeader("X-Business-Unit-ID") String businessUnitId
    ) {
        log.info("Retrieving payment status for: {}, tenant: {}, correlation: {}", 
                paymentId, tenantId, correlationId);
        
        try {
            PaymentInitiationResponse response = paymentInitiationService.getPaymentStatus(
                paymentId, correlationId, tenantId, businessUnitId
            );
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            log.warn("Payment not found: {}", e.getMessage());
            return ResponseEntity.notFound().build();
            
        } catch (Exception e) {
            log.error("Failed to retrieve payment status", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping(
        value = "/{paymentId}/validate",
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Operation(
        summary = "Validate Payment",
        description = "Validate a payment against business rules"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Payment validated successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = PaymentInitiationResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Validation failed",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Payment not found",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        )
    })
    public ResponseEntity<PaymentInitiationResponse> validatePayment(
        @Parameter(description = "Payment ID", required = true)
        @PathVariable String paymentId,
        
        @Parameter(description = "Correlation ID for tracing", required = true)
        @RequestHeader("X-Correlation-ID") String correlationId,
        
        @Parameter(description = "Tenant ID", required = true)
        @RequestHeader("X-Tenant-ID") String tenantId,
        
        @Parameter(description = "Business Unit ID", required = true)
        @RequestHeader("X-Business-Unit-ID") String businessUnitId
    ) {
        log.info("Validating payment: {}, tenant: {}, correlation: {}", 
                paymentId, tenantId, correlationId);
        
        try {
            PaymentInitiationResponse response = paymentInitiationService.validatePayment(
                paymentId, correlationId, tenantId, businessUnitId
            );
            
            log.info("Payment validated successfully: {}", paymentId);
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            log.warn("Payment validation failed: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(PaymentInitiationResponse.builder()
                    .errorMessage(e.getMessage())
                    .build());
                    
        } catch (Exception e) {
            log.error("Failed to validate payment", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(PaymentInitiationResponse.builder()
                    .errorMessage("Payment validation failed")
                    .build());
        }
    }

    @PostMapping(
        value = "/{paymentId}/fail",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Operation(
        summary = "Fail Payment",
        description = "Mark a payment as failed with reason"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Payment failed successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = PaymentInitiationResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Payment not found",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        )
    })
    public ResponseEntity<PaymentInitiationResponse> failPayment(
        @Parameter(description = "Payment ID", required = true)
        @PathVariable String paymentId,
        
        @Parameter(description = "Failure reason", required = true)
        @RequestBody FailureRequest failureRequest,
        
        @Parameter(description = "Correlation ID for tracing", required = true)
        @RequestHeader("X-Correlation-ID") String correlationId,
        
        @Parameter(description = "Tenant ID", required = true)
        @RequestHeader("X-Tenant-ID") String tenantId,
        
        @Parameter(description = "Business Unit ID", required = true)
        @RequestHeader("X-Business-Unit-ID") String businessUnitId
    ) {
        log.info("Failing payment: {} with reason: {}, tenant: {}, correlation: {}", 
                paymentId, failureRequest.getReason(), tenantId, correlationId);
        
        try {
            PaymentInitiationResponse response = paymentInitiationService.failPayment(
                paymentId, failureRequest.getReason(), correlationId, tenantId, businessUnitId
            );
            
            log.info("Payment failed successfully: {}", paymentId);
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            log.warn("Payment not found: {}", e.getMessage());
            return ResponseEntity.notFound().build();
            
        } catch (Exception e) {
            log.error("Failed to fail payment", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(PaymentInitiationResponse.builder()
                    .errorMessage("Payment failure processing failed")
                    .build());
        }
    }

    @PostMapping(
        value = "/{paymentId}/complete",
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Operation(
        summary = "Complete Payment",
        description = "Mark a payment as completed"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Payment completed successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = PaymentInitiationResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Payment not found",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        )
    })
    public ResponseEntity<PaymentInitiationResponse> completePayment(
        @Parameter(description = "Payment ID", required = true)
        @PathVariable String paymentId,
        
        @Parameter(description = "Correlation ID for tracing", required = true)
        @RequestHeader("X-Correlation-ID") String correlationId,
        
        @Parameter(description = "Tenant ID", required = true)
        @RequestHeader("X-Tenant-ID") String tenantId,
        
        @Parameter(description = "Business Unit ID", required = true)
        @RequestHeader("X-Business-Unit-ID") String businessUnitId
    ) {
        log.info("Completing payment: {}, tenant: {}, correlation: {}", 
                paymentId, tenantId, correlationId);
        
        try {
            PaymentInitiationResponse response = paymentInitiationService.completePayment(
                paymentId, correlationId, tenantId, businessUnitId
            );
            
            log.info("Payment completed successfully: {}", paymentId);
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            log.warn("Payment not found: {}", e.getMessage());
            return ResponseEntity.notFound().build();
            
        } catch (Exception e) {
            log.error("Failed to complete payment", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(PaymentInitiationResponse.builder()
                    .errorMessage("Payment completion failed")
                    .build());
        }
    }

    @GetMapping(
        value = "/history",
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Operation(
        summary = "Get Payment History",
        description = "Retrieve payment history for tenant and business unit"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Payment history retrieved successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = PaymentHistoryResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        )
    })
    public ResponseEntity<PaymentHistoryResponse> getPaymentHistory(
        @Parameter(description = "Correlation ID for tracing", required = true)
        @RequestHeader("X-Correlation-ID") String correlationId,
        
        @Parameter(description = "Tenant ID", required = true)
        @RequestHeader("X-Tenant-ID") String tenantId,
        
        @Parameter(description = "Business Unit ID", required = true)
        @RequestHeader("X-Business-Unit-ID") String businessUnitId
    ) {
        log.info("Retrieving payment history for tenant: {}, business unit: {}, correlation: {}", 
                tenantId, businessUnitId, correlationId);
        
        try {
            var payments = paymentInitiationService.getPaymentHistory(
                tenantId, businessUnitId, correlationId
            );
            
            PaymentHistoryResponse response = PaymentHistoryResponse.builder()
                    .payments(payments)
                    .totalCount(payments.size())
                    .build();
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Failed to retrieve payment history", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(PaymentHistoryResponse.builder()
                    .errorMessage("Payment history retrieval failed")
                    .build());
        }
    }

    /**
     * Failure request DTO
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    @Schema(description = "Payment failure request")
    public static class FailureRequest {
        @Schema(description = "Failure reason", required = true)
        private String reason;
    }

    /**
     * Payment history response DTO
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    @Schema(description = "Payment history response")
    public static class PaymentHistoryResponse {
        @Schema(description = "List of payments")
        private java.util.List<PaymentInitiationResponse> payments;
        
        @Schema(description = "Total count of payments")
        private Integer totalCount;
        
        @Schema(description = "Error message if any")
        private String errorMessage;
    }

    /**
     * Error response DTO
     */
    @Schema(description = "Error response")
    public static class ErrorResponse {
        @Schema(description = "Error message")
        public String message;
        
        @Schema(description = "Error code")
        public String code;
        
        @Schema(description = "Timestamp")
        public String timestamp;
    }
}
