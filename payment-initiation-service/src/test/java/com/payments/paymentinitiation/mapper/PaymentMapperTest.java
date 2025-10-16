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
    assertThat(entity.getPaymentId()).isEqualTo(domainPayment.getId().getValue());
    assertThat(entity.getIdempotencyKey()).isEqualTo(domainPayment.getIdempotencyKey());
    assertThat(entity.getSourceAccount()).isEqualTo(domainPayment.getSourceAccount().getValue());
    assertThat(entity.getDestinationAccount())
        .isEqualTo(domainPayment.getDestinationAccount().getValue());
    assertThat(entity.getAmount()).isEqualTo(domainPayment.getAmount().getAmount());
    assertThat(entity.getCurrency()).isEqualTo(domainPayment.getAmount().getCurrency());
    assertThat(entity.getReference()).isEqualTo(domainPayment.getReference());
    assertThat(entity.getPaymentType()).isEqualTo(domainPayment.getPaymentType().name());
    assertThat(entity.getPriority()).isEqualTo(domainPayment.getPriority().name());
    assertThat(entity.getTenantId()).isEqualTo(domainPayment.getTenantContext().getTenantId());
    assertThat(entity.getBusinessUnitId())
        .isEqualTo(domainPayment.getTenantContext().getBusinessUnitId());
    assertThat(entity.getStatus()).isEqualTo(domainPayment.getStatus().name());
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
    assertThat(domainPayment.getId().getValue()).isEqualTo(entity.getPaymentId());
    assertThat(domainPayment.getIdempotencyKey()).isEqualTo(entity.getIdempotencyKey());
    assertThat(domainPayment.getSourceAccount().getValue()).isEqualTo(entity.getSourceAccount());
    assertThat(domainPayment.getDestinationAccount().getValue())
        .isEqualTo(entity.getDestinationAccount());
    assertThat(domainPayment.getAmount().getAmount()).isEqualTo(entity.getAmount());
    assertThat(domainPayment.getAmount().getCurrency()).isEqualTo(entity.getCurrency());
    assertThat(domainPayment.getReference()).isEqualTo(entity.getReference());
    assertThat(domainPayment.getPaymentType())
        .isEqualTo(com.payments.domain.payment.PaymentType.valueOf(entity.getPaymentType()));
    assertThat(domainPayment.getPriority())
        .isEqualTo(com.payments.domain.payment.Priority.valueOf(entity.getPriority()));
    assertThat(domainPayment.getTenantContext().getTenantId()).isEqualTo(entity.getTenantId());
    assertThat(domainPayment.getTenantContext().getBusinessUnitId())
        .isEqualTo(entity.getBusinessUnitId());
    assertThat(domainPayment.getStatus())
        .isEqualTo(com.payments.domain.payment.PaymentStatus.valueOf(entity.getStatus()));
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
    domainPayment.setErrorMessage("Test error message");

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
    // Note: using fully qualified names for domain enums to avoid clashes with contract enums
    domainPayment.addStatusChange(
        com.payments.domain.payment.PaymentStatus.VALIDATED,
        "Validation successful",
        Instant.now());
    domainPayment.addStatusChange(
        com.payments.domain.payment.PaymentStatus.COMPLETED, "Payment completed", Instant.now());

    // When
    List<PaymentStatusHistoryEntity> entities =
        paymentMapper.toStatusHistoryEntities(domainPayment);

    // Then
    assertThat(entities).hasSize(2);
    assertThat(entities.get(0).getStatus()).isEqualTo("VALIDATED");
    assertThat(entities.get(0).getReason()).isEqualTo("Validation successful");
    assertThat(entities.get(1).getStatus()).isEqualTo("COMPLETED");
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
    assertThat(paymentMapper.mapPaymentType(com.payments.domain.payment.PaymentType.RTGS))
        .isEqualTo(PaymentType.RTGS);
    assertThat(paymentMapper.mapPaymentType(com.payments.domain.payment.PaymentType.CARD))
        .isEqualTo(PaymentType.CARD);
  }

  @Test
  void mapPriority_ShouldMapCorrectly_WhenValidPriorities() {
    // Test all priority mappings
    assertThat(paymentMapper.mapPriority(com.payments.domain.payment.Priority.LOW))
        .isEqualTo(Priority.LOW);
    assertThat(paymentMapper.mapPriority(com.payments.domain.payment.Priority.NORMAL))
        .isEqualTo(Priority.NORMAL);
    assertThat(paymentMapper.mapPriority(com.payments.domain.payment.Priority.HIGH))
        .isEqualTo(Priority.HIGH);
    assertThat(paymentMapper.mapPriority(com.payments.domain.payment.Priority.URGENT))
        .isEqualTo(Priority.URGENT);
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
        .sourceAccount(new AccountNumber("12345678901"))
        .destinationAccount(new AccountNumber("98765432109"))
        .amount(new Money(BigDecimal.valueOf(1000.00), "ZAR"))
        .reference("Test payment")
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
    PaymentEntity entity = new PaymentEntity();
    entity.setPaymentId("TEST-PAYMENT-001");
    entity.setIdempotencyKey("TEST-IDEMPOTENCY-001");
    entity.setSourceAccount("12345678901");
    entity.setDestinationAccount("98765432109");
    entity.setAmount(BigDecimal.valueOf(1000.00));
    entity.setCurrency("ZAR");
    entity.setReference("Test payment");
    entity.setPaymentType("EFT");
    entity.setPriority("NORMAL");
    entity.setTenantId("TEST-TENANT-001");
    entity.setBusinessUnitId("TEST-BU-001");
    entity.setStatus("INITIATED");
    entity.setInitiatedBy("test@example.com");
    entity.setInitiatedAt(Instant.now());
    return entity;
  }
}
