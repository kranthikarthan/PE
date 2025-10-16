package com.payments.paymentinitiation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import com.payments.contracts.payment.PaymentInitiationRequest;
import com.payments.contracts.payment.PaymentInitiationResponse;
import com.payments.contracts.payment.PaymentStatus;
import com.payments.domain.payment.Payment;
import com.payments.domain.shared.PaymentId;
import com.payments.domain.payment.PaymentType;
import com.payments.domain.payment.Priority;
import com.payments.domain.shared.AccountNumber;
import com.payments.domain.shared.Money;
import com.payments.domain.shared.TenantContext;
import com.payments.paymentinitiation.port.PaymentRepositoryPort;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for PaymentInitiationService
 *
 * <p>Tests business logic in isolation with mocked dependencies
 */
@ExtendWith(MockitoExtension.class)
class PaymentInitiationServiceTest {

  @Mock private PaymentRepositoryPort paymentRepository;

  @Mock private IdempotencyService idempotencyService;

  @Mock private PaymentDomainService paymentDomainService;

  @Mock private PaymentEventPublisher eventPublisher;

  private PaymentInitiationService paymentInitiationService;

  @BeforeEach
  void setUp() {
    paymentInitiationService =
        new PaymentInitiationService(
            paymentRepository, idempotencyService, paymentDomainService, eventPublisher);
  }

  @Test
  void initiatePayment_ShouldReturnSuccessResponse_WhenValidRequest() {
    // Given
    PaymentInitiationRequest request = createValidPaymentRequest();
    String correlationId = UUID.randomUUID().toString();
    String tenantId = "TENANT-001";
    String businessUnitId = "BU-001";

    when(idempotencyService.isDuplicate(anyString(), anyString())).thenReturn(false);
    when(paymentRepository.save(any(Payment.class))).thenReturn(createMockPayment());
    doNothing()
        .when(idempotencyService)
        .recordIdempotency(anyString(), anyString(), any(PaymentId.class));
    doNothing()
        .when(paymentDomainService)
        .validatePaymentBusinessRules(any(Payment.class), any(TenantContext.class));
    doNothing().when(eventPublisher).publishPaymentInitiatedEvent(any(Payment.class), anyString());

    // When
    PaymentInitiationResponse response =
        paymentInitiationService.initiatePayment(request, correlationId, tenantId, businessUnitId);

    // Then
    assertThat(response).isNotNull();
    assertThat(response.getPaymentId()).isNotNull();
    assertThat(response.getStatus()).isEqualTo(PaymentStatus.INITIATED);
    assertThat(response.getTenantContext()).isNotNull();
    assertThat(response.getInitiatedAt()).isNotNull();
    assertThat(response.getErrorMessage()).isNull();

    verify(idempotencyService).isDuplicate(request.getIdempotencyKey(), tenantId);
    verify(paymentDomainService)
        .validatePaymentBusinessRules(any(Payment.class), any(TenantContext.class));
    verify(paymentRepository).save(any(Payment.class));
    verify(idempotencyService).recordIdempotency(anyString(), anyString(), any(PaymentId.class));
    verify(eventPublisher).publishPaymentInitiatedEvent(any(Payment.class), eq(correlationId));
  }

  @Test
  void initiatePayment_ShouldThrowException_WhenDuplicateIdempotencyKey() {
    // Given
    PaymentInitiationRequest request = createValidPaymentRequest();
    String correlationId = UUID.randomUUID().toString();
    String tenantId = "TENANT-001";
    String businessUnitId = "BU-001";

    when(idempotencyService.isDuplicate(anyString(), anyString())).thenReturn(true);

    // When & Then
    assertThatThrownBy(
            () ->
                paymentInitiationService.initiatePayment(
                    request, correlationId, tenantId, businessUnitId))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Duplicate payment request");

    verify(idempotencyService).isDuplicate(request.getIdempotencyKey(), tenantId);
    verify(paymentRepository, never()).save(any(Payment.class));
    verify(eventPublisher, never()).publishPaymentInitiatedEvent(any(Payment.class), anyString());
  }

  @Test
  void getPaymentStatus_ShouldReturnPaymentStatus_WhenPaymentExists() {
    // Given
    String paymentId = "PAY-001";
    String correlationId = UUID.randomUUID().toString();
    String tenantId = "TENANT-001";
    String businessUnitId = "BU-001";

    Payment mockPayment = createMockPayment();
    when(paymentRepository.findById(any(PaymentId.class))).thenReturn(Optional.of(mockPayment));

    // When
    PaymentInitiationResponse response =
        paymentInitiationService.getPaymentStatus(
            paymentId, correlationId, tenantId, businessUnitId);

    // Then
    assertThat(response).isNotNull();
    assertThat(response.getPaymentId()).isNotNull();
    assertThat(response.getStatus()).isEqualTo(PaymentStatus.INITIATED);
    assertThat(response.getTenantContext()).isNotNull();
    assertThat(response.getInitiatedAt()).isNotNull();

    verify(paymentRepository).findById(any(PaymentId.class));
  }

  @Test
  void getPaymentStatus_ShouldThrowException_WhenPaymentNotFound() {
    // Given
    String paymentId = "PAY-001";
    String correlationId = UUID.randomUUID().toString();
    String tenantId = "TENANT-001";
    String businessUnitId = "BU-001";

    when(paymentRepository.findById(any(PaymentId.class))).thenReturn(Optional.empty());

    // When & Then
    assertThatThrownBy(
            () ->
                paymentInitiationService.getPaymentStatus(
                    paymentId, correlationId, tenantId, businessUnitId))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Payment not found: PAY-001");

    verify(paymentRepository).findById(any(PaymentId.class));
  }

  @Test
  void validatePayment_ShouldReturnValidatedResponse_WhenValidationSucceeds() {
    // Given
    String paymentId = "PAY-001";
    String correlationId = UUID.randomUUID().toString();
    String tenantId = "TENANT-001";
    String businessUnitId = "BU-001";

    Payment mockPayment = createMockPayment();
    when(paymentRepository.findByIdAndTenantId(any(PaymentId.class), anyString()))
        .thenReturn(Optional.of(mockPayment));
    doNothing()
        .when(paymentDomainService)
        .validatePaymentBusinessRules(any(Payment.class), any(TenantContext.class));
    doNothing()
        .when(paymentDomainService)
        .processPaymentStatusChange(any(Payment.class), any(), anyString());

    // When
    PaymentInitiationResponse response =
        paymentInitiationService.validatePayment(
            paymentId, correlationId, tenantId, businessUnitId);

    // Then
    assertThat(response).isNotNull();
    assertThat(response.getPaymentId()).isNotNull();
    assertThat(response.getStatus()).isEqualTo(PaymentStatus.VALIDATED);

    verify(paymentRepository).findByIdAndTenantId(any(PaymentId.class), eq(tenantId));
    verify(paymentDomainService)
        .validatePaymentBusinessRules(any(Payment.class), any(TenantContext.class));
    verify(paymentDomainService).processPaymentStatusChange(any(Payment.class), any(), anyString());
  }

  @Test
  void failPayment_ShouldReturnFailedResponse_WhenPaymentExists() {
    // Given
    String paymentId = "PAY-001";
    String reason = "Insufficient funds";
    String correlationId = UUID.randomUUID().toString();
    String tenantId = "TENANT-001";
    String businessUnitId = "BU-001";

    Payment mockPayment = createMockPayment();
    when(paymentRepository.findByIdAndTenantId(any(PaymentId.class), anyString()))
        .thenReturn(Optional.of(mockPayment));
    doNothing()
        .when(paymentDomainService)
        .processPaymentStatusChange(any(Payment.class), any(), anyString());

    // When
    PaymentInitiationResponse response =
        paymentInitiationService.failPayment(
            paymentId, reason, correlationId, tenantId, businessUnitId);

    // Then
    assertThat(response).isNotNull();
    assertThat(response.getPaymentId()).isNotNull();
    assertThat(response.getStatus()).isEqualTo(PaymentStatus.FAILED);
    assertThat(response.getErrorMessage()).isEqualTo(reason);

    verify(paymentRepository).findByIdAndTenantId(any(PaymentId.class), eq(tenantId));
    verify(paymentDomainService).processPaymentStatusChange(any(Payment.class), any(), anyString());
  }

  @Test
  void completePayment_ShouldReturnCompletedResponse_WhenPaymentExists() {
    // Given
    String paymentId = "PAY-001";
    String correlationId = UUID.randomUUID().toString();
    String tenantId = "TENANT-001";
    String businessUnitId = "BU-001";

    Payment mockPayment = createMockPayment();
    when(paymentRepository.findByIdAndTenantId(any(PaymentId.class), anyString()))
        .thenReturn(Optional.of(mockPayment));
    doNothing()
        .when(paymentDomainService)
        .processPaymentStatusChange(any(Payment.class), any(), anyString());

    // When
    PaymentInitiationResponse response =
        paymentInitiationService.completePayment(
            paymentId, correlationId, tenantId, businessUnitId);

    // Then
    assertThat(response).isNotNull();
    assertThat(response.getPaymentId()).isNotNull();
    assertThat(response.getStatus()).isEqualTo(PaymentStatus.COMPLETED);

    verify(paymentRepository).findByIdAndTenantId(any(PaymentId.class), eq(tenantId));
    verify(paymentDomainService).processPaymentStatusChange(any(Payment.class), any(), anyString());
  }

  private PaymentInitiationRequest createValidPaymentRequest() {
    return PaymentInitiationRequest.builder()
        .paymentId(new PaymentId("PAY-001"))
        .idempotencyKey("IDEMPOTENCY-001")
        .sourceAccount("12345678901")
        .destinationAccount("98765432109")
        .amount(new Money(BigDecimal.valueOf(1000.00), "ZAR"))
        .reference("Test payment")
        .paymentType(PaymentType.EFT)
        .priority(Priority.NORMAL)
        .tenantContext(
            TenantContext.builder().tenantId("TENANT-001").businessUnitId("BU-001").build())
        .initiatedBy("user@example.com")
        .build();
  }

  private Payment createMockPayment() {
    return Payment.builder()
        .id(new PaymentId("PAY-001"))
        .idempotencyKey("IDEMPOTENCY-001")
        .sourceAccount(new AccountNumber("12345678901"))
        .destinationAccount(new AccountNumber("98765432109"))
        .amount(new Money(BigDecimal.valueOf(1000.00), "ZAR"))
        .reference("Test payment")
        .paymentType(PaymentType.EFT)
        .priority(Priority.NORMAL)
        .tenantContext(
            TenantContext.builder().tenantId("TENANT-001").businessUnitId("BU-001").build())
        .status(com.payments.domain.payment.PaymentStatus.INITIATED)
        .initiatedBy("user@example.com")
        .initiatedAt(Instant.now())
        .build();
  }
}
