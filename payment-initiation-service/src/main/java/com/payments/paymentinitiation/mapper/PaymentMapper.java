package com.payments.paymentinitiation.mapper;

import com.payments.contracts.payment.PaymentInitiationResponse;
import com.payments.contracts.payment.PaymentStatus;
import com.payments.contracts.payment.PaymentType;
import com.payments.contracts.payment.Priority;
import com.payments.domain.entities.Payment;
import com.payments.domain.valueobjects.PaymentReference;
import com.payments.paymentinitiation.entity.PaymentEntity;
import com.payments.paymentinitiation.entity.PaymentStatusHistoryEntity;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

/**
 * Payment Mapper
 *
 * <p>Maps between Payment domain aggregate and PaymentEntity JPA entity following the Domain-Driven
 * Design pattern
 */
@Component
public class PaymentMapper {

  /**
   * Convert Payment domain aggregate to PaymentEntity
   *
   * @param payment Payment domain aggregate
   * @return PaymentEntity JPA entity
   */
  public PaymentEntity toEntity(Payment payment) {
    return PaymentEntity.builder()
        .paymentId(payment.getId())
        .idempotencyKey(payment.getIdempotencyKey())
        .sourceAccount(payment.getSourceAccount())
        .destinationAccount(payment.getDestinationAccount())
        .amount(payment.getAmount())
        .reference(payment.getReference().getValue())
        .paymentType(payment.getPaymentType())
        .priority(payment.getPriority())
        .tenantContext(payment.getTenantContext())
        .status(payment.getStatus())
        .initiatedBy(payment.getInitiatedBy())
        .initiatedAt(payment.getInitiatedAt())
        .validatedAt(payment.getValidatedAt())
        .submittedToClearingAt(payment.getSubmittedToClearingAt())
        .clearedAt(payment.getClearedAt())
        .completedAt(payment.getCompletedAt())
        .failedAt(payment.getFailedAt())
        .failureReason(payment.getFailureReason())
        .build();
  }

  /**
   * Convert PaymentEntity to Payment domain aggregate
   *
   * @param entity PaymentEntity JPA entity
   * @return Payment domain aggregate
   */
  public Payment toDomain(PaymentEntity entity) {
    return Payment.builder()
        .paymentId(entity.getPaymentId())
        .idempotencyKey(entity.getIdempotencyKey())
        .sourceAccount(entity.getSourceAccount())
        .destinationAccount(entity.getDestinationAccount())
        .amount(entity.getAmount())
        .reference(PaymentReference.of(entity.getReference()))
        .paymentType(entity.getPaymentType())
        .priority(entity.getPriority())
        .tenantContext(entity.getTenantContext())
        .status(entity.getStatus())
        .initiatedBy(entity.getInitiatedBy())
        .initiatedAt(entity.getInitiatedAt())
        .validatedAt(entity.getValidatedAt())
        .submittedToClearingAt(entity.getSubmittedToClearingAt())
        .clearedAt(entity.getClearedAt())
        .completedAt(entity.getCompletedAt())
        .failedAt(entity.getFailedAt())
        .failureReason(entity.getFailureReason())
        .build();
  }

  /** Map domain to contract response */
  public PaymentInitiationResponse toResponse(Payment payment) {
    return PaymentInitiationResponse.builder()
        .paymentId(payment.getId())
        .status(mapStatus(payment.getStatus()))
        .tenantContext(payment.getTenantContext())
        .initiatedAt(payment.getInitiatedAt())
        .errorMessage(payment.getFailureReason())
        .build();
  }

  /** Convert Payment domain status history to PaymentStatusHistoryEntity list */
  public List<PaymentStatusHistoryEntity> toStatusHistoryEntities(Payment payment) {
    if (payment.getStatusHistory() == null || payment.getStatusHistory().isEmpty()) {
      return List.of();
    }

    return payment.getStatusHistory().stream()
        .map(
            statusChange ->
                PaymentStatusHistoryEntity.builder()
                    .fromStatus(statusChange.getFromStatus())
                    .toStatus(statusChange.getToStatus())
                    .reason(statusChange.getReason())
                    .changedBy(statusChange.getChangedBy())
                    .changedAt(statusChange.getChangedAt())
                    .build())
        .collect(Collectors.toList());
  }

  /** Enum mappers: domain -> contract */
  public PaymentType mapPaymentType(com.payments.domain.valueobjects.PaymentType type) {
    if (type == null) return null;
    
    if (type == com.payments.domain.valueobjects.PaymentType.EFT) {
      return PaymentType.EFT;
    } else if (type == com.payments.domain.valueobjects.PaymentType.RTGS) {
      return PaymentType.IMMEDIATE_PAYMENT;
    } else if (type == com.payments.domain.valueobjects.PaymentType.CREDIT_TRANSFER || 
               type == com.payments.domain.valueobjects.PaymentType.DEBIT_TRANSFER) {
      return PaymentType.EFT;
    } else if (type == com.payments.domain.valueobjects.PaymentType.CARD_PAYMENT || 
               type == com.payments.domain.valueobjects.PaymentType.WALLET_TRANSFER) {
      return PaymentType.EFT;
    }
    
    return PaymentType.EFT; // default fallback
  }

  public Priority mapPriority(com.payments.domain.valueobjects.Priority priority) {
    if (priority == null) return null;
    return switch (priority) {
      case LOW -> Priority.NORMAL; // Map LOW to NORMAL in contract
      case NORMAL -> Priority.NORMAL;
      case HIGH -> Priority.HIGH;
      case URGENT -> Priority.HIGH; // Map URGENT to HIGH in contract
    };
  }

  public PaymentStatus mapStatus(com.payments.domain.valueobjects.PaymentStatus status) {
    return status == null ? null : PaymentStatus.valueOf(status.name());
  }
}
