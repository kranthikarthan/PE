package com.payments.paymentinitiation.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.payments.contracts.payment.PaymentInitiationResponse;
import com.payments.contracts.payment.PaymentStatus;
import com.payments.contracts.payment.PaymentType;
import com.payments.contracts.payment.Priority;
import com.payments.domain.payment.Payment;
import com.payments.domain.shared.AccountNumber;
import com.payments.domain.shared.Money;
import com.payments.domain.shared.PaymentId;
import com.payments.domain.shared.TenantContext;
import com.payments.paymentinitiation.entity.PaymentEntity;
import com.payments.paymentinitiation.entity.PaymentStatusHistoryEntity;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for PaymentMapper
 *
 * <p>Tests mapping between domain objects and entities
 */
class PaymentMapperTest {

  private PaymentMapper paymentMapper;

  @BeforeEach
  void setUp() {
    paymentMapper = new PaymentMapper();
  }

  @Test
  void toEntity_ShouldMapDomainToEntity_WhenValidDomain() {
    // Given
    Payment domainPayment = createDomainPayment();

    // When
    PaymentEntity entity = paymentMapper.toEntity(domainPayment);

    // Then
    assertThat(entity).isNotNull();
    assertThat(entity.getPaymentId()).isEqualTo(domainPayment.getId());
    assertThat(entity.getIdempotencyKey()).isEqualTo(domainPayment.getIdempotencyKey());
    assertThat(entity.getSourceAccount()).isEqualTo(domainPayment.getSourceAccount());
    assertThat(entity.getDestinationAccount())
        .isEqualTo(domainPayment.getDestinationAccount());
    assertThat(entity.getAmount()).isEqualTo(domainPayment.getAmount());
    assertThat(entity.getReference()).isEqualTo(domainPayment.getReference().getValue());
    assertThat(entity.getPaymentType()).isEqualTo(domainPayment.getPaymentType());
    assertThat(entity.getPriority()).isEqualTo(domainPayment.getPriority());
    assertThat(entity.getTenantContext()).isEqualTo(domainPayment.getTenantContext());
    assertThat(entity.getStatus()).isEqualTo(domainPayment.getStatus());
    assertThat(entity.getInitiatedBy()).isEqualTo(domainPayment.getInitiatedBy());
    assertThat(entity.getInitiatedAt()).isEqualTo(domainPayment.getInitiatedAt());
  }

  @Test
  void toDomain_ShouldMapEntityToDomain_WhenValidEntity() {
    // Given
    PaymentEntity entity = createPaymentEntity();

    // When
    Payment domainPayment = paymentMapper.toDomain(entity);

    // Then
    assertThat(domainPayment).isNotNull();
    assertThat(domainPayment.getId()).isEqualTo(entity.getPaymentId());
    assertThat(domainPayment.getIdempotencyKey()).isEqualTo(entity.getIdempotencyKey());
    assertThat(domainPayment.getSourceAccount()).isEqualTo(entity.getSourceAccount());
    assertThat(domainPayment.getDestinationAccount()).isEqualTo(entity.getDestinationAccount());
    assertThat(domainPayment.getAmount()).isEqualTo(entity.getAmount());
    assertThat(domainPayment.getReference().getValue()).isEqualTo(entity.getReference());
    assertThat(domainPayment.getPaymentType()).isEqualTo(entity.getPaymentType());
    assertThat(domainPayment.getPriority()).isEqualTo(entity.getPriority());
    assertThat(domainPayment.getTenantContext()).isEqualTo(entity.getTenantContext());
    assertThat(domainPayment.getStatus()).isEqualTo(entity.getStatus());
    assertThat(domainPayment.getInitiatedBy()).isEqualTo(entity.getInitiatedBy());
    assertThat(domainPayment.getInitiatedAt()).isEqualTo(entity.getInitiatedAt());
  }

  @Test
  void toResponse_ShouldMapDomainToResponse_WhenValidDomain() {
    // Given
    Payment domainPayment = createDomainPayment();

    // When
    PaymentInitiationResponse response = paymentMapper.toResponse(domainPayment);

    // Then
    assertThat(response).isNotNull();
    assertThat(response.getPaymentId()).isEqualTo(domainPayment.getId());
    assertThat(response.getStatus())
        .isEqualTo(PaymentStatus.valueOf(domainPayment.getStatus().name()));
    assertThat(response.getTenantContext()).isEqualTo(domainPayment.getTenantContext());
    assertThat(response.getInitiatedAt()).isEqualTo(domainPayment.getInitiatedAt());
    assertThat(response.getErrorMessage()).isNull();
  }

  @Test
  void toResponse_ShouldMapDomainToResponseWithError_WhenDomainHasError() {
    // Given
    Payment domainPayment = createDomainPayment();
    domainPayment.setFailureReason("Test error message");

    // When
    PaymentInitiationResponse response = paymentMapper.toResponse(domainPayment);

    // Then
    assertThat(response).isNotNull();
    assertThat(response.getPaymentId()).isEqualTo(domainPayment.getId());
    assertThat(response.getStatus())
        .isEqualTo(PaymentStatus.valueOf(domainPayment.getStatus().name()));
    assertThat(response.getTenantContext()).isEqualTo(domainPayment.getTenantContext());
    assertThat(response.getInitiatedAt()).isEqualTo(domainPayment.getInitiatedAt());
    assertThat(response.getErrorMessage()).isEqualTo("Test error message");
  }

  @Test
  void toStatusHistoryEntities_ShouldMapStatusChanges_WhenValidDomain() {
    // Given
    Payment domainPayment = createDomainPayment();
    // Drive status changes via updateStatus to populate history
    domainPayment.updateStatus(
        com.payments.domain.payment.PaymentStatus.VALIDATED, "Validation successful");
    domainPayment.updateStatus(
        com.payments.domain.payment.PaymentStatus.COMPLETED, "Payment completed");

    // When
    List<PaymentStatusHistoryEntity> entities =
        paymentMapper.toStatusHistoryEntities(domainPayment);

    // Then
    assertThat(entities).hasSize(2);
    assertThat(entities.get(0).getToStatus().name()).isEqualTo("VALIDATED");
    assertThat(entities.get(0).getReason()).isEqualTo("Validation successful");
    assertThat(entities.get(1).getToStatus().name()).isEqualTo("COMPLETED");
    assertThat(entities.get(1).getReason()).isEqualTo("Payment completed");
  }

  @Test
  void toStatusHistoryEntities_ShouldReturnEmpty_WhenNoStatusChanges() {
    // Given
    Payment domainPayment = createDomainPayment();

    // When
    List<PaymentStatusHistoryEntity> entities =
        paymentMapper.toStatusHistoryEntities(domainPayment);

    // Then
    assertThat(entities).isEmpty();
  }

  @Test
  void mapPaymentType_ShouldMapCorrectly_WhenValidTypes() {
    // Test all payment type mappings
    assertThat(paymentMapper.mapPaymentType(com.payments.domain.payment.PaymentType.EFT))
        .isEqualTo(PaymentType.EFT);
    assertThat(paymentMapper.mapPaymentType(com.payments.domain.payment.PaymentType.RTC))
        .isEqualTo(PaymentType.IMMEDIATE_PAYMENT);
  }

  @Test
  void mapPriority_ShouldMapCorrectly_WhenValidPriorities() {
    // Test all priority mappings
    assertThat(paymentMapper.mapPriority(com.payments.domain.payment.Priority.NORMAL))
        .isEqualTo(Priority.NORMAL);
    assertThat(paymentMapper.mapPriority(com.payments.domain.payment.Priority.HIGH))
        .isEqualTo(Priority.HIGH);
  }

  @Test
  void mapStatus_ShouldMapCorrectly_WhenValidStatuses() {
    // Test all status mappings
    assertThat(paymentMapper.mapStatus(com.payments.domain.payment.PaymentStatus.INITIATED))
        .isEqualTo(PaymentStatus.INITIATED);
    assertThat(paymentMapper.mapStatus(com.payments.domain.payment.PaymentStatus.VALIDATED))
        .isEqualTo(PaymentStatus.VALIDATED);
    assertThat(paymentMapper.mapStatus(com.payments.domain.payment.PaymentStatus.COMPLETED))
        .isEqualTo(PaymentStatus.COMPLETED);
    assertThat(paymentMapper.mapStatus(com.payments.domain.payment.PaymentStatus.FAILED))
        .isEqualTo(PaymentStatus.FAILED);
  }

  private Payment createDomainPayment() {
    return Payment.builder()
        .id(new PaymentId("TEST-PAYMENT-001"))
        .idempotencyKey("TEST-IDEMPOTENCY-001")
        .sourceAccount(AccountNumber.of("12345678901"))
        .destinationAccount(AccountNumber.of("98765432109"))
        .amount(Money.zar(BigDecimal.valueOf(1000.00)))
        .reference(com.payments.domain.payment.PaymentReference.of("Test payment"))
        .paymentType(com.payments.domain.payment.PaymentType.EFT)
        .priority(com.payments.domain.payment.Priority.NORMAL)
        .tenantContext(
            TenantContext.builder()
                .tenantId("TEST-TENANT-001")
                .businessUnitId("TEST-BU-001")
                .build())
        .status(com.payments.domain.payment.PaymentStatus.INITIATED)
        .initiatedBy("test@example.com")
        .initiatedAt(Instant.now())
        .build();
  }

  private PaymentEntity createPaymentEntity() {
    return PaymentEntity.builder()
        .paymentId(new PaymentId("TEST-PAYMENT-001"))
        .idempotencyKey("TEST-IDEMPOTENCY-001")
        .sourceAccount(AccountNumber.of("12345678901"))
        .destinationAccount(AccountNumber.of("98765432109"))
        .amount(Money.zar(BigDecimal.valueOf(1000.00)))
        .reference("Test payment")
        .paymentType(com.payments.domain.payment.PaymentType.EFT)
        .priority(com.payments.domain.payment.Priority.NORMAL)
        .tenantContext(
            TenantContext.builder().tenantId("TEST-TENANT-001").businessUnitId("TEST-BU-001").build())
        .status(com.payments.domain.payment.PaymentStatus.INITIATED)
        .initiatedBy("test@example.com")
        .initiatedAt(Instant.now())
        .build();
  }
}
