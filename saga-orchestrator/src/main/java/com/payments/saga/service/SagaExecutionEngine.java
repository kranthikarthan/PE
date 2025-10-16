package com.payments.saga.service;

import com.payments.saga.domain.SagaId;
import com.payments.saga.domain.SagaStep;
import com.payments.saga.domain.SagaStepStatus;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/** Engine for executing saga steps */
@Service
@RequiredArgsConstructor
@Slf4j
public class SagaExecutionEngine {

  private final RestTemplate restTemplate;
  private final SagaStepService sagaStepService;
  private final SagaEventService sagaEventService;

  /** Execute a saga by processing all its steps */
  public void executeSaga(SagaId sagaId) {
    log.info("Starting execution of saga {}", sagaId.getValue());

    // This will be called asynchronously to process the saga steps
    // The actual step execution is handled by executeStep
  }

  /** Execute a specific saga step */
  public void executeStep(SagaStep step) {
    log.info(
        "Executing step {} ({}) for saga {}",
        step.getStepName(),
        step.getStepType(),
        step.getSagaId().getValue());

    if (step.getStatus() != SagaStepStatus.PENDING) {
      log.warn(
          "Step {} is not pending, current status: {}", step.getId().getValue(), step.getStatus());
      return;
    }

    try {
      // Mark step as running
      step.markAsRunning();
      sagaStepService.saveStep(step);

      // Publish step started event
      sagaEventService.publishStepStartedEvent(step);

      // Execute the step based on its type
      Map<String, Object> result = executeStepByType(step);

      // Mark step as completed
      step.markAsCompleted(result);
      sagaStepService.saveStep(step);

      // Publish step completed event
      sagaEventService.publishStepCompletedEvent(step, result);

      log.info("Step {} completed successfully", step.getId().getValue());

    } catch (Exception e) {
      log.error("Step {} failed: {}", step.getId().getValue(), e.getMessage(), e);
      throw new RuntimeException("Step execution failed", e);
    }
  }

  /** Execute step based on its type */
  private Map<String, Object> executeStepByType(SagaStep step) {
    return switch (step.getStepType()) {
      case VALIDATION -> executeValidationStep(step);
      case ROUTING -> executeRoutingStep(step);
      case ACCOUNT_ADAPTER -> executeAccountAdapterStep(step);
      case TRANSACTION_PROCESSING -> executeTransactionProcessingStep(step);
      case NOTIFICATION -> executeNotificationStep(step);
      case COMPENSATION -> executeCompensationStep(step);
    };
  }

  /** Execute validation step */
  private Map<String, Object> executeValidationStep(SagaStep step) {
    log.debug("Executing validation step: {}", step.getStepName());

    String url = buildServiceUrl(step.getServiceName(), step.getEndpoint());

    try {
      // Call validation service
      Map<String, Object> request = step.getInputData();
      Map<String, Object> response = restTemplate.postForObject(url, request, Map.class);

      log.debug("Validation step {} completed with response: {}", step.getStepName(), response);
      return response != null ? response : Map.of("status", "validated");

    } catch (Exception e) {
      log.error("Validation step {} failed: {}", step.getStepName(), e.getMessage(), e);
      throw new RuntimeException("Validation failed", e);
    }
  }

  /** Execute routing step */
  private Map<String, Object> executeRoutingStep(SagaStep step) {
    log.debug("Executing routing step: {}", step.getStepName());

    String url = buildServiceUrl(step.getServiceName(), step.getEndpoint());

    try {
      // Call routing service
      Map<String, Object> request = step.getInputData();
      Map<String, Object> response = restTemplate.postForObject(url, request, Map.class);

      log.debug("Routing step {} completed with response: {}", step.getStepName(), response);
      return response != null ? response : Map.of("routing", "completed");

    } catch (Exception e) {
      log.error("Routing step {} failed: {}", step.getStepName(), e.getMessage(), e);
      throw new RuntimeException("Routing failed", e);
    }
  }

  /** Execute account adapter step */
  private Map<String, Object> executeAccountAdapterStep(SagaStep step) {
    log.debug("Executing account adapter step: {}", step.getStepName());

    String url = buildServiceUrl(step.getServiceName(), step.getEndpoint());

    try {
      // Call account adapter service
      Map<String, Object> request = step.getInputData();
      Map<String, Object> response = restTemplate.postForObject(url, request, Map.class);

      log.debug(
          "Account adapter step {} completed with response: {}", step.getStepName(), response);
      return response != null ? response : Map.of("account", "processed");

    } catch (Exception e) {
      log.error("Account adapter step {} failed: {}", step.getStepName(), e.getMessage(), e);
      throw new RuntimeException("Account adapter failed", e);
    }
  }

  /** Execute transaction processing step */
  private Map<String, Object> executeTransactionProcessingStep(SagaStep step) {
    log.debug("Executing transaction processing step: {}", step.getStepName());

    String url = buildServiceUrl(step.getServiceName(), step.getEndpoint());

    try {
      // Call transaction processing service
      Map<String, Object> request = step.getInputData();
      Map<String, Object> response = restTemplate.postForObject(url, request, Map.class);

      log.debug(
          "Transaction processing step {} completed with response: {}",
          step.getStepName(),
          response);
      return response != null ? response : Map.of("transaction", "processed");

    } catch (Exception e) {
      log.error("Transaction processing step {} failed: {}", step.getStepName(), e.getMessage(), e);
      throw new RuntimeException("Transaction processing failed", e);
    }
  }

  /** Execute notification step */
  private Map<String, Object> executeNotificationStep(SagaStep step) {
    log.debug("Executing notification step: {}", step.getStepName());

    String url = buildServiceUrl(step.getServiceName(), step.getEndpoint());

    try {
      // Call notification service
      Map<String, Object> request = step.getInputData();
      Map<String, Object> response = restTemplate.postForObject(url, request, Map.class);

      log.debug("Notification step {} completed with response: {}", step.getStepName(), response);
      return response != null ? response : Map.of("notification", "sent");

    } catch (Exception e) {
      log.error("Notification step {} failed: {}", step.getStepName(), e.getMessage(), e);
      throw new RuntimeException("Notification failed", e);
    }
  }

  /** Execute compensation step */
  private Map<String, Object> executeCompensationStep(SagaStep step) {
    log.debug("Executing compensation step: {}", step.getStepName());

    String url = buildServiceUrl(step.getServiceName(), step.getCompensationEndpoint());

    try {
      // Call compensation endpoint
      Map<String, Object> request = step.getInputData();
      Map<String, Object> response = restTemplate.postForObject(url, request, Map.class);

      log.debug("Compensation step {} completed with response: {}", step.getStepName(), response);
      return response != null ? response : Map.of("compensation", "completed");

    } catch (Exception e) {
      log.error("Compensation step {} failed: {}", step.getStepName(), e.getMessage(), e);
      throw new RuntimeException("Compensation failed", e);
    }
  }

  /** Build service URL */
  private String buildServiceUrl(String serviceName, String endpoint) {
    // In a real implementation, this would use service discovery
    String baseUrl =
        switch (serviceName) {
          case "validation-service" -> "http://localhost:8081";
          case "routing-service" -> "http://localhost:8082";
          case "account-adapter-service" -> "http://localhost:8083";
          case "transaction-processing-service" -> "http://localhost:8084";
          case "notification-service" -> "http://localhost:8086";
          default -> "http://localhost:8080";
        };

    return baseUrl + endpoint;
  }
}
