package com.payments.saga.service;

import com.payments.saga.domain.Saga;
import com.payments.saga.domain.SagaId;
import com.payments.saga.domain.SagaStep;
import com.payments.saga.domain.SagaStepId;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Service for looking up sagas by various criteria */
@Service
@RequiredArgsConstructor
@Slf4j
public class SagaLookupService {

  private final SagaService sagaService;
  private final SagaStepService sagaStepService;

  /** Find saga by payment ID */
  @Transactional(readOnly = true)
  public Optional<Saga> findSagaByPaymentId(String paymentId) {
    log.debug("Looking up saga by payment ID: {}", paymentId);

    List<Saga> sagas = sagaService.getSagasByPaymentId(paymentId);

    if (sagas.isEmpty()) {
      log.debug("No saga found for payment ID: {}", paymentId);
      return Optional.empty();
    }

    // Return the most recent saga (assuming multiple sagas for same payment is rare)
    Saga latestSaga = sagas.get(0);
    log.debug("Found saga {} for payment ID: {}", latestSaga.getId().getValue(), paymentId);

    return Optional.of(latestSaga);
  }

  /** Find saga by correlation ID */
  @Transactional(readOnly = true)
  public Optional<Saga> findSagaByCorrelationId(String correlationId) {
    log.debug("Looking up saga by correlation ID: {}", correlationId);

    List<Saga> sagas = sagaService.getSagasByCorrelationId(correlationId);

    if (sagas.isEmpty()) {
      log.debug("No saga found for correlation ID: {}", correlationId);
      return Optional.empty();
    }

    // Return the most recent saga
    Saga latestSaga = sagas.get(0);
    log.debug("Found saga {} for correlation ID: {}", latestSaga.getId().getValue(), correlationId);

    return Optional.of(latestSaga);
  }

  /** Find saga by saga ID */
  @Transactional(readOnly = true)
  public Optional<Saga> findSagaById(String sagaId) {
    log.debug("Looking up saga by ID: {}", sagaId);

    return sagaService.getSaga(SagaId.of(sagaId));
  }

  /** Find the current step for a saga */
  @Transactional(readOnly = true)
  public Optional<SagaStep> findCurrentStep(SagaId sagaId) {
    log.debug("Looking up current step for saga: {}", sagaId.getValue());

    Optional<Saga> saga = sagaService.getSaga(sagaId);
    if (saga.isEmpty()) {
      log.debug("Saga not found: {}", sagaId.getValue());
      return Optional.empty();
    }

    return saga.get().getCurrentStep();
  }

  /** Find step by step ID */
  @Transactional(readOnly = true)
  public Optional<SagaStep> findStepById(String stepId) {
    log.debug("Looking up step by ID: {}", stepId);

    return sagaStepService.getStep(SagaStepId.of(stepId));
  }

  /** Find all steps for a saga */
  @Transactional(readOnly = true)
  public List<SagaStep> findStepsBySagaId(SagaId sagaId) {
    log.debug("Looking up steps for saga: {}", sagaId.getValue());

    return sagaStepService.getStepsBySagaId(sagaId);
  }

  /** Find completed steps for a saga (for compensation) */
  @Transactional(readOnly = true)
  public List<SagaStep> findCompletedSteps(SagaId sagaId) {
    log.debug("Looking up completed steps for saga: {}", sagaId.getValue());

    return sagaStepService.getCompletedStepsBySagaId(sagaId);
  }
}
