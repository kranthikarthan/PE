package com.payments.reconciliation.service;

import com.payments.domain.reconciliation.ReconciliationException;
import com.payments.domain.reconciliation.ReconciliationRun;
import com.payments.reconciliation.repository.ReconciliationExceptionRepository;
import com.payments.reconciliation.repository.ReconciliationRunRepository;
import com.payments.reconciliation.dto.ReconciliationExceptionRequest;
import com.payments.reconciliation.dto.ReconciliationExceptionResponse;
import com.payments.reconciliation.dto.ReconciliationExceptionResolutionRequest;
import com.payments.reconciliation.exception.ReconciliationExceptionServiceException;
import com.payments.domain.shared.TenantContext;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for managing reconciliation exceptions.
 *
 * <p>This service provides comprehensive exception management capabilities including
 * exception creation, assignment, resolution, and tracking. It handles the complete
 * exception lifecycle from creation to resolution.
 *
 * @since PE-412
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ReconciliationExceptionService {
  
  private final ReconciliationExceptionRepository exceptionRepository;
  private final ReconciliationRunRepository runRepository;
  
  /**
   * Creates a new reconciliation exception.
   *
   * @param request the exception request
   * @return the created exception response
   * @throws ReconciliationExceptionServiceException if creation fails
   */
  @Transactional
  @CircuitBreaker(name = "reconciliation-exception", fallbackMethod = "createExceptionFallback")
  @Retry(name = "reconciliation-exception")
  @TimeLimiter(name = "reconciliation-exception")
  public ReconciliationExceptionResponse createException(ReconciliationExceptionRequest request) {
    try {
      log.info("Creating reconciliation exception for run: {}", request.getRunId());
      
      // Validate reconciliation run exists
      Optional<ReconciliationRun> runOpt = runRepository.findByIdAndTenantId(request.getRunId(), TenantContext.getCurrentTenantId());
      if (runOpt.isEmpty()) {
        throw new ReconciliationExceptionServiceException("Reconciliation run not found: " + request.getRunId());
      }
      
      ReconciliationException exception = ReconciliationException.builder()
          .exceptionId(UUID.randomUUID().toString())
          .runId(request.getRunId())
          .exceptionType(request.getExceptionType())
          .internalTransactionId(request.getInternalTransactionId())
          .clearingTransactionId(request.getClearingTransactionId())
          .amountDifference(request.getAmountDifference())
          .status(ReconciliationException.ExceptionStatus.OPEN)
          .description(request.getDescription())
          .details(request.getDetails())
          .priority(request.getPriority())
          .assignedTo(request.getAssignedTo())
          .createdBy(TenantContext.getCurrentUserId())
          .build();
      
      exception.setTenantAndBusinessUnit();
      
      ReconciliationException savedException = exceptionRepository.save(exception);
      
      log.info("Successfully created reconciliation exception: {}", savedException.getExceptionId());
      
      return ReconciliationExceptionResponse.builder()
          .exceptionId(savedException.getExceptionId())
          .runId(savedException.getRunId())
          .exceptionType(savedException.getExceptionType())
          .internalTransactionId(savedException.getInternalTransactionId())
          .clearingTransactionId(savedException.getClearingTransactionId())
          .amountDifference(savedException.getAmountDifference())
          .status(savedException.getStatus())
          .description(savedException.getDescription())
          .details(savedException.getDetails())
          .priority(savedException.getPriority())
          .assignedTo(savedException.getAssignedTo())
          .resolution(savedException.getResolution())
          .resolvedAt(savedException.getResolvedAt())
          .resolvedBy(savedException.getResolvedBy())
          .createdAt(savedException.getCreatedAt())
          .createdBy(savedException.getCreatedBy())
          .updatedAt(savedException.getUpdatedAt())
          .updatedBy(savedException.getUpdatedBy())
          .build();
      
    } catch (Exception e) {
      log.error("Failed to create reconciliation exception: {}", e.getMessage(), e);
      throw new ReconciliationExceptionServiceException("Failed to create reconciliation exception", e);
    }
  }
  
  /**
   * Assigns an exception to a user.
   *
   * @param exceptionId the exception ID
   * @param assignedTo the user to assign to
   * @return the updated exception response
   * @throws ReconciliationExceptionServiceException if assignment fails
   */
  @Transactional
  public ReconciliationExceptionResponse assignException(String exceptionId, String assignedTo) {
    try {
      log.info("Assigning reconciliation exception: {} to user: {}", exceptionId, assignedTo);
      
      Optional<ReconciliationException> exceptionOpt = exceptionRepository.findByExceptionIdAndTenantId(exceptionId, TenantContext.getCurrentTenantId());
      if (exceptionOpt.isEmpty()) {
        throw new ReconciliationExceptionServiceException("Reconciliation exception not found: " + exceptionId);
      }
      
      ReconciliationException exception = exceptionOpt.get();
      exception.assignTo(assignedTo);
      exception.setUpdatedBy(TenantContext.getCurrentUserId());
      
      ReconciliationException savedException = exceptionRepository.save(exception);
      
      log.info("Successfully assigned reconciliation exception: {} to user: {}", exceptionId, assignedTo);
      
      return mapToExceptionResponse(savedException);
      
    } catch (Exception e) {
      log.error("Failed to assign reconciliation exception: {}", e.getMessage(), e);
      throw new ReconciliationExceptionServiceException("Failed to assign reconciliation exception", e);
    }
  }
  
  /**
   * Resolves an exception.
   *
   * @param exceptionId the exception ID
   * @param request the resolution request
   * @return the updated exception response
   * @throws ReconciliationExceptionServiceException if resolution fails
   */
  @Transactional
  public ReconciliationExceptionResponse resolveException(String exceptionId, ReconciliationExceptionResolutionRequest request) {
    try {
      log.info("Resolving reconciliation exception: {}", exceptionId);
      
      Optional<ReconciliationException> exceptionOpt = exceptionRepository.findByExceptionIdAndTenantId(exceptionId, TenantContext.getCurrentTenantId());
      if (exceptionOpt.isEmpty()) {
        throw new ReconciliationExceptionServiceException("Reconciliation exception not found: " + exceptionId);
      }
      
      ReconciliationException exception = exceptionOpt.get();
      exception.resolve(request.getResolution(), request.getResolutionNotes(), TenantContext.getCurrentUserId());
      exception.setUpdatedBy(TenantContext.getCurrentUserId());
      
      ReconciliationException savedException = exceptionRepository.save(exception);
      
      log.info("Successfully resolved reconciliation exception: {}", exceptionId);
      
      return mapToExceptionResponse(savedException);
      
    } catch (Exception e) {
      log.error("Failed to resolve reconciliation exception: {}", e.getMessage(), e);
      throw new ReconciliationExceptionServiceException("Failed to resolve reconciliation exception", e);
    }
  }
  
  /**
   * Gets all exceptions for a reconciliation run.
   *
   * @param runId the reconciliation run ID
   * @return list of exception responses
   * @throws ReconciliationExceptionServiceException if retrieval fails
   */
  public List<ReconciliationExceptionResponse> getExceptionsByRunId(Long runId) {
    try {
      log.info("Retrieving reconciliation exceptions for run: {}", runId);
      
      List<ReconciliationException> exceptions = exceptionRepository.findByRunIdAndTenantId(runId, TenantContext.getCurrentTenantId());
      
      return exceptions.stream()
          .map(this::mapToExceptionResponse)
          .collect(Collectors.toList());
      
    } catch (Exception e) {
      log.error("Failed to retrieve reconciliation exceptions: {}", e.getMessage(), e);
      throw new ReconciliationExceptionServiceException("Failed to retrieve reconciliation exceptions", e);
    }
  }
  
  /**
   * Gets all exceptions for the current tenant.
   *
   * @return list of exception responses
   * @throws ReconciliationExceptionServiceException if retrieval fails
   */
  public List<ReconciliationExceptionResponse> getAllExceptions() {
    try {
      log.info("Retrieving all reconciliation exceptions for tenant: {}", TenantContext.getCurrentTenantId());
      
      List<ReconciliationException> exceptions = exceptionRepository.findByTenantId(TenantContext.getCurrentTenantId());
      
      return exceptions.stream()
          .map(this::mapToExceptionResponse)
          .collect(Collectors.toList());
      
    } catch (Exception e) {
      log.error("Failed to retrieve reconciliation exceptions: {}", e.getMessage(), e);
      throw new ReconciliationExceptionServiceException("Failed to retrieve reconciliation exceptions", e);
    }
  }
  
  /**
   * Gets exceptions by status.
   *
   * @param status the exception status
   * @return list of exception responses
   * @throws ReconciliationExceptionServiceException if retrieval fails
   */
  public List<ReconciliationExceptionResponse> getExceptionsByStatus(ReconciliationException.ExceptionStatus status) {
    try {
      log.info("Retrieving reconciliation exceptions by status: {}", status);
      
      List<ReconciliationException> exceptions = exceptionRepository.findByStatusAndTenantId(status, TenantContext.getCurrentTenantId());
      
      return exceptions.stream()
          .map(this::mapToExceptionResponse)
          .collect(Collectors.toList());
      
    } catch (Exception e) {
      log.error("Failed to retrieve reconciliation exceptions by status: {}", e.getMessage(), e);
      throw new ReconciliationExceptionServiceException("Failed to retrieve reconciliation exceptions by status", e);
    }
  }
  
  /**
   * Gets exceptions assigned to a user.
   *
   * @param assignedTo the assigned user
   * @return list of exception responses
   * @throws ReconciliationExceptionServiceException if retrieval fails
   */
  public List<ReconciliationExceptionResponse> getExceptionsByAssignedTo(String assignedTo) {
    try {
      log.info("Retrieving reconciliation exceptions assigned to: {}", assignedTo);
      
      List<ReconciliationException> exceptions = exceptionRepository.findByAssignedToAndTenantId(assignedTo, TenantContext.getCurrentTenantId());
      
      return exceptions.stream()
          .map(this::mapToExceptionResponse)
          .collect(Collectors.toList());
      
    } catch (Exception e) {
      log.error("Failed to retrieve reconciliation exceptions by assigned user: {}", e.getMessage(), e);
      throw new ReconciliationExceptionServiceException("Failed to retrieve reconciliation exceptions by assigned user", e);
    }
  }
  
  /**
   * Gets exception statistics for a reconciliation run.
   *
   * @param runId the reconciliation run ID
   * @return exception statistics
   * @throws ReconciliationExceptionServiceException if retrieval fails
   */
  public ReconciliationExceptionStatistics getExceptionStatistics(Long runId) {
    try {
      log.info("Retrieving exception statistics for run: {}", runId);
      
      long totalExceptions = exceptionRepository.countByRunIdAndTenantId(runId, TenantContext.getCurrentTenantId());
      long openExceptions = exceptionRepository.countByRunIdAndStatusAndTenantId(runId, ReconciliationException.ExceptionStatus.OPEN, TenantContext.getCurrentTenantId());
      long resolvedExceptions = exceptionRepository.countByRunIdAndStatusAndTenantId(runId, ReconciliationException.ExceptionStatus.RESOLVED, TenantContext.getCurrentTenantId());
      long closedExceptions = exceptionRepository.countByRunIdAndStatusAndTenantId(runId, ReconciliationException.ExceptionStatus.CLOSED, TenantContext.getCurrentTenantId());
      
      return ReconciliationExceptionStatistics.builder()
          .runId(runId)
          .totalExceptions(totalExceptions)
          .openExceptions(openExceptions)
          .resolvedExceptions(resolvedExceptions)
          .closedExceptions(closedExceptions)
          .resolutionRate(totalExceptions > 0 ? (double) (resolvedExceptions + closedExceptions) / totalExceptions : 0.0)
          .build();
      
    } catch (Exception e) {
      log.error("Failed to retrieve exception statistics: {}", e.getMessage(), e);
      throw new ReconciliationExceptionServiceException("Failed to retrieve exception statistics", e);
    }
  }
  
  /**
   * Maps exception entity to response DTO.
   *
   * @param exception the exception entity
   * @return the exception response
   */
  private ReconciliationExceptionResponse mapToExceptionResponse(ReconciliationException exception) {
    return ReconciliationExceptionResponse.builder()
        .exceptionId(exception.getExceptionId())
        .runId(exception.getRunId())
        .exceptionType(exception.getExceptionType())
        .internalTransactionId(exception.getInternalTransactionId())
        .clearingTransactionId(exception.getClearingTransactionId())
        .amountDifference(exception.getAmountDifference())
        .status(exception.getStatus())
        .description(exception.getDescription())
        .details(exception.getDetails())
        .priority(exception.getPriority())
        .assignedTo(exception.getAssignedTo())
        .resolution(exception.getResolution())
        .resolvedAt(exception.getResolvedAt())
        .resolvedBy(exception.getResolvedBy())
        .createdAt(exception.getCreatedAt())
        .createdBy(exception.getCreatedBy())
        .updatedAt(exception.getUpdatedAt())
        .updatedBy(exception.getUpdatedBy())
        .build();
  }
  
  // Fallback method for circuit breaker
  public ReconciliationExceptionResponse createExceptionFallback(ReconciliationExceptionRequest request, Exception ex) {
    log.error("Fallback: Failed to create reconciliation exception", ex);
    throw new ReconciliationExceptionServiceException("Service temporarily unavailable", ex);
  }
  
  /**
   * DTO for exception statistics.
   */
  @lombok.Data
  @lombok.Builder
  @lombok.NoArgsConstructor
  @lombok.AllArgsConstructor
  public static class ReconciliationExceptionStatistics {
    private Long runId;
    private long totalExceptions;
    private long openExceptions;
    private long resolvedExceptions;
    private long closedExceptions;
    private double resolutionRate;
  }
}
