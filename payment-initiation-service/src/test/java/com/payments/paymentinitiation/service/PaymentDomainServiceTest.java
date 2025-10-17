package com.payments.paymentinitiation.service;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import com.payments.domain.payment.Payment;
import com.payments.domain.payment.PaymentStatus;
import com.payments.domain.payment.PaymentType;
import com.payments.domain.payment.Priority;
import com.payments.domain.shared.AccountNumber;
import com.payments.domain.shared.Money;
import com.payments.domain.shared.PaymentId;
import com.payments.domain.shared.TenantContext;
import com.payments.paymentinitiation.port.PaymentRepositoryPort;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.stream.IntStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

/**
 * Unit tests for PaymentDomainService
 *
 * <p>Tests complex business logic and cross-aggregate operations
 */
@ExtendWith(MockitoExtension.class)
class PaymentDomainServiceTest {

  @Mock private PaymentRepositoryPort paymentRepository;

  @Mock private PaymentEventPublisher eventPublisher;

  private PaymentDomainService paymentDomainService;

  @BeforeEach
  void setUp() {
    paymentDomainService = new PaymentDomainService(paymentRepository, eventPublisher);
  }

  @Test
  void validatePaymentBusinessRules_ShouldPass_WhenAllRulesValid() {
    // Given
    Payment payment = createValidPayment();
    TenantContext tenantContext = createValidTenantContext();

    when(paymentRepository.findByTenantIdAndDateRange(
            anyString(), any(Instant.class), any(Instant.class), any(Pageable.class)))
        .thenReturn(createEmptyPaymentPage());

    // When & Then
    assertThatCode(() -> paymentDomainService.validatePaymentBusinessRules(payment, tenantContext))
        .doesNotThrowAnyException();

    verify(paymentRepository, times(2))
        .findByTenantIdAndDateRange(
            anyString(), any(Instant.class), any(Instant.class), any(Pageable.class));
  }

  @Test
  void validatePaymentBusinessRules_ShouldThrowException_WhenDailyLimitExceeded() {
    // Given
    Payment payment = createValidPayment();
    TenantContext tenantContext = createValidTenantContext();

    // Mock existing payments that exceed daily limit
    List<Payment> existingPayments =
        List.of(
            createPaymentWithAmount(BigDecimal.valueOf(800000.00)),
            createPaymentWithAmount(BigDecimal.valueOf(300000.00)));
    Page<Payment> paymentPage = new PageImpl<>(existingPayments);

    when(paymentRepository.findByTenantIdAndDateRange(
            anyString(), any(Instant.class), any(Instant.class), any(Pageable.class)))
        .thenReturn(paymentPage);

    // When & Then
    assertThatThrownBy(
            () -> paymentDomainService.validatePaymentBusinessRules(payment, tenantContext))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Daily payment limit exceeded");

  }

  @Test
  void validatePaymentBusinessRules_ShouldThrowException_WhenVelocityLimitExceeded() {
    // Given
    Payment payment = createValidPayment();
    TenantContext tenantContext = createValidTenantContext();

    // Mock many recent payments to exceed velocity limit
    List<Payment> recentPayments =
        IntStream.range(0, 110)
            .mapToObj(index -> createPaymentWithAmount(BigDecimal.valueOf(100.00 + index)))
            .toList();
    Page<Payment> paymentPage = new PageImpl<>(recentPayments);

    when(paymentRepository.findByTenantIdAndDateRange(
            anyString(), any(Instant.class), any(Instant.class), any(Pageable.class)))
        .thenReturn(paymentPage);

    // When & Then
    assertThatThrownBy(
            () -> paymentDomainService.validatePaymentBusinessRules(payment, tenantContext))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Payment velocity limit exceeded");

  }

  @Test
  void processPaymentStatusChange_ShouldUpdateStatus_WhenValidTransition() {
    // Given
    Payment payment = createValidPayment();
    PaymentStatus newStatus = PaymentStatus.VALIDATED;
    String reason = "Validation successful";

    when(paymentRepository.save(any(Payment.class))).thenReturn(payment);
    doNothing()
        .when(eventPublisher)
        .publishPaymentStatusChangedEvent(
            any(Payment.class), any(PaymentStatus.class), anyString());

    // When
    paymentDomainService.processPaymentStatusChange(payment, newStatus, reason);

    // Then
    verify(paymentRepository).save(payment);
    verify(eventPublisher).publishPaymentStatusChangedEvent(payment, newStatus, reason);
  }

  @Test
  void processPaymentStatusChange_ShouldThrowException_WhenInvalidTransition() {
    // Given
    Payment payment = createValidPayment();
    payment.setStatus(PaymentStatus.COMPLETED); // Terminal state
    PaymentStatus newStatus = PaymentStatus.VALIDATED;
    String reason = "Invalid transition";

    // When & Then
    assertThatThrownBy(
            () -> paymentDomainService.processPaymentStatusChange(payment, newStatus, reason))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Invalid status transition");
  }

  private Payment createValidPayment() {
    return Payment.builder()
        .id(new PaymentId("PAY-001"))
        .idempotencyKey("IDEMPOTENCY-001")
        .sourceAccount(AccountNumber.of("12345678901"))
        .destinationAccount(AccountNumber.of("98765432109"))
        .amount(Money.zar(BigDecimal.valueOf(1000.00)))
        .reference(com.payments.domain.payment.PaymentReference.of("Test payment"))
        .paymentType(PaymentType.EFT)
        .priority(Priority.NORMAL)
        .tenantContext(createValidTenantContext())
        .status(com.payments.domain.payment.PaymentStatus.INITIATED)
        .initiatedBy("user@example.com")
        .initiatedAt(Instant.now())
        .build();
  }

  private Payment createPaymentWithAmount(BigDecimal amount) {
    return Payment.builder()
        .id(new PaymentId("PAY-EXISTING"))
        .idempotencyKey("IDEMPOTENCY-EXISTING")
        .sourceAccount(AccountNumber.of("12345678901"))
        .destinationAccount(AccountNumber.of("98765432109"))
        .amount(Money.zar(amount))
        .reference(com.payments.domain.payment.PaymentReference.of("Existing payment"))
        .paymentType(PaymentType.EFT)
        .priority(Priority.NORMAL)
        .tenantContext(createValidTenantContext())
        .status(com.payments.domain.payment.PaymentStatus.INITIATED)
        .initiatedBy("user@example.com")
        .initiatedAt(Instant.now())
        .build();
  }

  private TenantContext createValidTenantContext() {
    return TenantContext.builder().tenantId("TENANT-001").businessUnitId("BU-001").build();
  }

  private Page<Payment> createEmptyPaymentPage() {
    return new PageImpl<>(List.of());
  }
}
