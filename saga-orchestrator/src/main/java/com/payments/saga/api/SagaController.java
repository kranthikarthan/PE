package com.payments.saga.api;

import com.payments.domain.shared.TenantContext;
import com.payments.saga.domain.Saga;
import com.payments.saga.domain.SagaId;
import com.payments.saga.domain.SagaStep;
import com.payments.saga.service.SagaOrchestrator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * REST controller for saga orchestration
 */
@RestController
@RequestMapping("/api/v1/sagas")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Saga Orchestrator", description = "Saga orchestration API")
public class SagaController {

    private final SagaOrchestrator sagaOrchestrator;

    /**
     * Start a new saga
     */
    @PostMapping("/start")
    @Operation(summary = "Start a new saga", description = "Start a new saga with the specified template")
    public ResponseEntity<SagaResponse> startSaga(
            @RequestBody StartSagaRequest request,
            @RequestHeader("X-Tenant-Id") String tenantId,
            @RequestHeader("X-Business-Unit-Id") String businessUnitId) {
        
        log.info("Starting saga with template {} for payment {}", request.getTemplateName(), request.getPaymentId());
        
        TenantContext tenantContext = TenantContext.of(tenantId, "Tenant", businessUnitId, "Business Unit");
        
        Saga saga = sagaOrchestrator.startSaga(
            request.getTemplateName(),
            tenantContext,
            request.getCorrelationId(),
            request.getPaymentId(),
            request.getSagaData()
        );
        
        return ResponseEntity.ok(SagaResponse.fromDomain(saga));
    }

    /**
     * Get saga status
     */
    @GetMapping("/{sagaId}")
    @Operation(summary = "Get saga status", description = "Get the current status of a saga")
    public ResponseEntity<SagaResponse> getSagaStatus(
            @PathVariable String sagaId,
            @RequestHeader("X-Tenant-Id") String tenantId,
            @RequestHeader("X-Business-Unit-Id") String businessUnitId) {
        
        log.debug("Getting saga status for {}", sagaId);
        
        Optional<Saga> saga = sagaOrchestrator.getSagaStatus(SagaId.of(sagaId));
        
        if (saga.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(SagaResponse.fromDomain(saga.get()));
    }

    /**
     * Get saga steps
     */
    @GetMapping("/{sagaId}/steps")
    @Operation(summary = "Get saga steps", description = "Get all steps for a saga")
    public ResponseEntity<List<SagaStepResponse>> getSagaSteps(
            @PathVariable String sagaId,
            @RequestHeader("X-Tenant-Id") String tenantId,
            @RequestHeader("X-Business-Unit-Id") String businessUnitId) {
        
        log.debug("Getting saga steps for {}", sagaId);
        
        List<SagaStep> steps = sagaOrchestrator.getSagaSteps(SagaId.of(sagaId));
        
        return ResponseEntity.ok(steps.stream()
                .map(SagaStepResponse::fromDomain)
                .toList());
    }

    /**
     * Get saga events
     */
    @GetMapping("/{sagaId}/events")
    @Operation(summary = "Get saga events", description = "Get all events for a saga")
    public ResponseEntity<List<SagaEventResponse>> getSagaEvents(
            @PathVariable String sagaId,
            @RequestHeader("X-Tenant-Id") String tenantId,
            @RequestHeader("X-Business-Unit-Id") String businessUnitId) {
        
        log.debug("Getting saga events for {}", sagaId);
        
        List<com.payments.saga.domain.SagaEvent> events = sagaOrchestrator.getSagaEvents(SagaId.of(sagaId));
        
        return ResponseEntity.ok(events.stream()
                .map(SagaEventResponse::fromDomain)
                .toList());
    }

    /**
     * Start compensation for a saga
     */
    @PostMapping("/{sagaId}/compensate")
    @Operation(summary = "Start saga compensation", description = "Start compensation for a failed saga")
    public ResponseEntity<String> startCompensation(
            @PathVariable String sagaId,
            @RequestBody CompensateSagaRequest request,
            @RequestHeader("X-Tenant-Id") String tenantId,
            @RequestHeader("X-Business-Unit-Id") String businessUnitId) {
        
        log.info("Starting compensation for saga {}: {}", sagaId, request.getReason());
        
        sagaOrchestrator.startCompensation(SagaId.of(sagaId), request.getReason());
        
        return ResponseEntity.ok("Compensation started for saga " + sagaId);
    }

    /**
     * Get sagas by correlation ID
     */
    @GetMapping("/correlation/{correlationId}")
    @Operation(summary = "Get sagas by correlation ID", description = "Get all sagas for a correlation ID")
    public ResponseEntity<List<SagaResponse>> getSagasByCorrelationId(
            @PathVariable String correlationId,
            @RequestHeader("X-Tenant-Id") String tenantId,
            @RequestHeader("X-Business-Unit-Id") String businessUnitId) {
        
        log.debug("Getting sagas by correlation ID {}", correlationId);
        
        // This would need to be implemented in the orchestrator
        return ResponseEntity.ok(List.of());
    }

    /**
     * Get sagas by payment ID
     */
    @GetMapping("/payment/{paymentId}")
    @Operation(summary = "Get sagas by payment ID", description = "Get all sagas for a payment ID")
    public ResponseEntity<List<SagaResponse>> getSagasByPaymentId(
            @PathVariable String paymentId,
            @RequestHeader("X-Tenant-Id") String tenantId,
            @RequestHeader("X-Business-Unit-Id") String businessUnitId) {
        
        log.debug("Getting sagas by payment ID {}", paymentId);
        
        // This would need to be implemented in the orchestrator
        return ResponseEntity.ok(List.of());
    }
}






