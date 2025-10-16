package com.payments.transactionprocessing.dto;

import com.payments.domain.transaction.TransactionEvent;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionEventResponse {
  private String eventId;
  private String transactionId;
  private String tenantId;
  private String businessUnitId;
  private String eventType;
  private String eventData;
  private Instant occurredAt;
  private Long eventSequence;
  private String correlationId;
  private String causationId;

  public static TransactionEventResponse fromDomain(TransactionEvent event) {
    return TransactionEventResponse.builder()
        .eventId(event.getId().getValue())
        .transactionId(event.getTransactionId().getValue())
        .tenantId(event.getTenantContext().getTenantId())
        .businessUnitId(event.getTenantContext().getBusinessUnitId())
        .eventType(event.getEventType())
        .eventData(event.getEventData())
        .occurredAt(event.getOccurredAt())
        .eventSequence(event.getEventSequence())
        .correlationId(event.getCorrelationId())
        .causationId(event.getCausationId())
        .build();
  }
}
