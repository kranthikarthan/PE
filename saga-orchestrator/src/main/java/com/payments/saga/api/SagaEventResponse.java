package com.payments.saga.api;

import com.payments.saga.domain.SagaEvent;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;

/**
 * Response DTO for saga event information
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Saga event information response")
public class SagaEventResponse {
    
    @Schema(description = "Event ID", example = "event-123")
    private String eventId;
    
    @Schema(description = "Saga ID", example = "saga-123")
    private String sagaId;
    
    @Schema(description = "Event type", example = "SagaStarted")
    private String eventType;
    
    @Schema(description = "Event data")
    private Map<String, Object> eventData;
    
    @Schema(description = "Tenant ID", example = "tenant-1")
    private String tenantId;
    
    @Schema(description = "Business Unit ID", example = "bu-1")
    private String businessUnitId;
    
    @Schema(description = "Correlation ID", example = "corr-123")
    private String correlationId;
    
    @Schema(description = "Occurred at")
    private Instant occurredAt;

    public static SagaEventResponse fromDomain(SagaEvent event) {
        return SagaEventResponse.builder()
                .eventId(event.getEventId())
                .sagaId(event.getSagaId().getValue())
                .eventType(event.getEventType())
                .eventData(event.getEventData())
                .tenantId(event.getTenantContext().getTenantId())
                .businessUnitId(event.getTenantContext().getBusinessUnitId())
                .correlationId(event.getCorrelationId())
                .occurredAt(event.getOccurredAt())
                .build();
    }
}






