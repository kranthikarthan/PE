package com.payments.saga.service;

import com.payments.saga.domain.Saga;
import com.payments.saga.domain.SagaStep;
import com.payments.saga.domain.SagaStepStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

/**
 * Engine for handling saga compensation
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SagaCompensationEngine {

    private final RestTemplate restTemplate;
    private final SagaStepService sagaStepService;
    private final SagaEventService sagaEventService;

    /**
     * Start compensation for a saga
     */
    public void startCompensation(Saga saga) {
        log.info("Starting compensation for saga {}", saga.getId().getValue());

        // Get completed steps in reverse order (LIFO)
        List<SagaStep> completedSteps = saga.getCompletedSteps();
        completedSteps.sort((s1, s2) -> Integer.compare(s2.getSequence(), s1.getSequence()));

        log.info("Found {} completed steps to compensate", completedSteps.size());

        // Compensate each step
        for (SagaStep step : completedSteps) {
            if (step.hasCompensation()) {
                compensateStep(step);
            } else {
                log.warn("Step {} has no compensation endpoint, marking as compensated", step.getId().getValue());
                step.markAsCompensated();
                sagaStepService.saveStep(step);
            }
        }

        log.info("Compensation completed for saga {}", saga.getId().getValue());
    }

    /**
     * Compensate a specific step
     */
    public void compensateStep(SagaStep step) {
        log.info("Compensating step {} ({})", step.getStepName(), step.getStepType());

        if (step.getStatus() != SagaStepStatus.COMPLETED) {
            log.warn("Step {} is not completed, current status: {}", step.getId().getValue(), step.getStatus());
            return;
        }

        if (step.getCompensationEndpoint() == null || step.getCompensationEndpoint().trim().isEmpty()) {
            log.warn("Step {} has no compensation endpoint", step.getId().getValue());
            step.markAsCompensated();
            sagaStepService.saveStep(step);
            return;
        }

        try {
            // Mark step as compensating
            step.markAsCompensating();
            sagaStepService.saveStep(step);

            // Publish compensation started event
            sagaEventService.publishStepCompensationStartedEvent(step);

            // Execute compensation
            Map<String, Object> compensationResult = executeCompensation(step);

            // Mark step as compensated
            step.markAsCompensated();
            sagaStepService.saveStep(step);

            // Publish compensation completed event
            sagaEventService.publishStepCompensationCompletedEvent(step, compensationResult);

            log.info("Step {} compensated successfully", step.getId().getValue());

        } catch (Exception e) {
            log.error("Compensation failed for step {}: {}", step.getId().getValue(), e.getMessage(), e);
            
            // Mark step as failed compensation
            step.markAsFailed("Compensation failed: " + e.getMessage(), Map.of("compensationError", e.getMessage()));
            sagaStepService.saveStep(step);

            // Publish compensation failed event
            sagaEventService.publishStepCompensationFailedEvent(step, e.getMessage());
        }
    }

    /**
     * Execute compensation for a step
     */
    private Map<String, Object> executeCompensation(SagaStep step) {
        String url = buildCompensationUrl(step.getServiceName(), step.getCompensationEndpoint());
        
        try {
            // Prepare compensation request
            Map<String, Object> compensationRequest = buildCompensationRequest(step);
            
            // Call compensation endpoint
            Map<String, Object> response = restTemplate.postForObject(url, compensationRequest, Map.class);
            
            log.debug("Compensation for step {} completed with response: {}", step.getStepName(), response);
            return response != null ? response : Map.of("compensation", "completed");
            
        } catch (Exception e) {
            log.error("Compensation execution failed for step {}: {}", step.getStepName(), e.getMessage(), e);
            throw new RuntimeException("Compensation execution failed", e);
        }
    }

    /**
     * Build compensation request
     */
    private Map<String, Object> buildCompensationRequest(SagaStep step) {
        return Map.of(
            "stepId", step.getId().getValue(),
            "sagaId", step.getSagaId().getValue(),
            "stepName", step.getStepName(),
            "stepType", step.getStepType().name(),
            "originalInput", step.getInputData(),
            "originalOutput", step.getOutputData(),
            "correlationId", step.getCorrelationId(),
            "tenantId", step.getTenantContext().getTenantId(),
            "businessUnitId", step.getTenantContext().getBusinessUnitId()
        );
    }

    /**
     * Build compensation URL
     */
    private String buildCompensationUrl(String serviceName, String compensationEndpoint) {
        // In a real implementation, this would use service discovery
        String baseUrl = switch (serviceName) {
            case "validation-service" -> "http://localhost:8081";
            case "routing-service" -> "http://localhost:8082";
            case "account-adapter-service" -> "http://localhost:8083";
            case "transaction-processing-service" -> "http://localhost:8084";
            case "notification-service" -> "http://localhost:8086";
            default -> "http://localhost:8080";
        };
        
        return baseUrl + compensationEndpoint;
    }

    /**
     * Check if a step has compensation
     */
    private boolean hasCompensation(SagaStep step) {
        return step.getCompensationEndpoint() != null && !step.getCompensationEndpoint().trim().isEmpty();
    }
}






