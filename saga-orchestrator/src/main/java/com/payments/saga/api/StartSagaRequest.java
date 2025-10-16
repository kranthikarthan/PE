package com.payments.saga.api;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Request DTO for starting a saga
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to start a new saga")
public class StartSagaRequest {
    
    @Schema(description = "Saga template name", example = "PaymentProcessingSaga", required = true)
    private String templateName;
    
    @Schema(description = "Correlation ID for tracking", example = "corr-123", required = true)
    private String correlationId;
    
    @Schema(description = "Payment ID", example = "pay-456", required = true)
    private String paymentId;
    
    @Schema(description = "Additional saga data")
    private Map<String, Object> sagaData;
}






