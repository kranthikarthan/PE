package com.payments.saga.event;

import com.payments.saga.domain.SagaCompensatedEvent;
import com.payments.saga.domain.SagaCompensationStartedEvent;
import com.payments.saga.domain.SagaCompletedEvent;
import com.payments.saga.domain.SagaFailedEvent;
import com.payments.saga.domain.SagaStartedEvent;
import com.payments.saga.domain.SagaStepCompletedEvent;
import com.payments.saga.domain.SagaStepFailedEvent;
import com.payments.saga.domain.SagaStepStartedEvent;

/** Interface for publishing saga events */
public interface SagaEventPublisher {
  void publishSagaStarted(SagaStartedEvent event);

  void publishSagaCompleted(SagaCompletedEvent event);

  void publishSagaFailed(SagaFailedEvent event);

  void publishSagaCompensationStarted(SagaCompensationStartedEvent event);

  void publishSagaCompensated(SagaCompensatedEvent event);

  void publishSagaStepStarted(SagaStepStartedEvent event);

  void publishSagaStepCompleted(SagaStepCompletedEvent event);

  void publishSagaStepFailed(SagaStepFailedEvent event);

  void publishToDeadLetterQueue(String topic, String key, String message, Exception error);
}
