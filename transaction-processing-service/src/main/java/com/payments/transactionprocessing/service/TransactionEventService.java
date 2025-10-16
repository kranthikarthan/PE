package com.payments.transactionprocessing.service;

import com.payments.domain.shared.TenantContext;
import com.payments.domain.transaction.*;
import com.payments.transactionprocessing.entity.TransactionEventEntity;
import com.payments.transactionprocessing.repository.TransactionEventRepository;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionEventService {

  private final TransactionEventRepository transactionEventRepository;
  private final TransactionEventPublisher eventPublisher;

  @Transactional
  public void saveTransactionEvents(Transaction transaction) {
    log.debug("Saving events for transaction {}", transaction.getId().getValue());

    for (TransactionEvent event : transaction.getEvents()) {
      TransactionEventEntity entity = TransactionEventEntity.fromDomain(event);
      transactionEventRepository.save(entity);
    }

    // Publish events to Kafka
    eventPublisher.publishTransactionEvents(transaction);

    log.debug("Events saved and published for transaction {}", transaction.getId().getValue());
  }

  public List<TransactionEvent> getTransactionEvents(TransactionId transactionId) {
    log.debug("Getting events for transaction {}", transactionId.getValue());

    return transactionEventRepository.findByTransactionId(transactionId).stream()
        .map(TransactionEventEntity::toDomain)
        .collect(Collectors.toList());
  }

  public List<TransactionEvent> getTenantEvents(TenantContext tenantContext) {
    log.debug("Getting events for tenant {}", tenantContext.getTenantId());

    return transactionEventRepository.findByTenantContext(tenantContext).stream()
        .map(TransactionEventEntity::toDomain)
        .collect(Collectors.toList());
  }

  public List<TransactionEvent> getTenantEventsByType(
      TenantContext tenantContext, String eventType) {
    log.debug("Getting {} events for tenant {}", eventType, tenantContext.getTenantId());

    return transactionEventRepository
        .findByTenantContextAndEventType(tenantContext, eventType)
        .stream()
        .map(TransactionEventEntity::toDomain)
        .collect(Collectors.toList());
  }

  public List<TransactionEvent> getTenantEventsByTimeRange(
      TenantContext tenantContext, Instant startTime, Instant endTime) {
    log.debug(
        "Getting events for tenant {} between {} and {}",
        tenantContext.getTenantId(),
        startTime,
        endTime);

    return transactionEventRepository
        .findByTenantContextAndTimeRange(tenantContext, startTime, endTime)
        .stream()
        .map(TransactionEventEntity::toDomain)
        .collect(Collectors.toList());
  }

  public List<TransactionEvent> getEventsByCorrelationId(String correlationId) {
    log.debug("Getting events for correlation ID {}", correlationId);

    return transactionEventRepository.findByCorrelationId(correlationId).stream()
        .map(TransactionEventEntity::toDomain)
        .collect(Collectors.toList());
  }

  public List<TransactionEvent> getTenantEventsByTypeAndTimeRange(
      TenantContext tenantContext, String eventType, Instant startTime, Instant endTime) {
    log.debug(
        "Getting {} events for tenant {} between {} and {}",
        eventType,
        tenantContext.getTenantId(),
        startTime,
        endTime);

    return transactionEventRepository
        .findByTenantContextAndEventTypeAndTimeRange(tenantContext, eventType, startTime, endTime)
        .stream()
        .map(TransactionEventEntity::toDomain)
        .collect(Collectors.toList());
  }

  public Long getNextEventSequence(TransactionId transactionId) {
    log.debug("Getting next event sequence for transaction {}", transactionId.getValue());

    Long maxSequence =
        transactionEventRepository.findMaxEventSequenceByTransactionId(transactionId);
    return maxSequence != null ? maxSequence + 1 : 1L;
  }
}
