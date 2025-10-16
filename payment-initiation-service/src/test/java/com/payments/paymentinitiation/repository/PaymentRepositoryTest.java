package com.payments.paymentinitiation.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.payments.domain.payment.Payment;
import com.payments.domain.shared.PaymentId;
import com.payments.domain.payment.PaymentStatus;
import com.payments.domain.payment.PaymentType;
import com.payments.domain.payment.Priority;
import com.payments.domain.shared.AccountNumber;
import com.payments.domain.shared.Money;
import com.payments.domain.shared.TenantContext;
import com.payments.paymentinitiation.port.PaymentRepositoryPort;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Repository tests for Payment
 *
 * <p>Tests JPA repository operations with real database
 */
@DataJpaTest
@ActiveProfiles("test")
class PaymentRepositoryTest {

  @Autowired private TestEntityManager entityManager;

  @Autowired private PaymentRepositoryPort paymentRepository;

  private Payment testPayment;

  @BeforeEach
  void setUp() {
    testPayment = createTestPayment();
    entityManager.persistAndFlush(testPayment);
  }

  @Test
  void findById_ShouldReturnPayment_WhenPaymentExists() {
    // When
    Optional<Payment> result = paymentRepository.findById(testPayment.getId());

    // Then
    assertThat(result).isPresent();
    assertThat(result.get().getId()).isEqualTo(testPayment.getId());
    assertThat(result.get().getAmount()).isEqualTo(testPayment.getAmount());
    assertThat(result.get().getStatus()).isEqualTo(testPayment.getStatus());
  }

  @Test
  void findById_ShouldReturnEmpty_WhenPaymentDoesNotExist() {
    // Given
    PaymentId nonExistentId = new PaymentId("NON-EXISTENT");

    // When
    Optional<Payment> result = paymentRepository.findById(nonExistentId);

    // Then
    assertThat(result).isEmpty();
  }

  @Test
  void findByIdAndTenantId_ShouldReturnPayment_WhenPaymentExistsForTenant() {
    // When
    Optional<Payment> result =
        paymentRepository.findByIdAndTenantId(
            testPayment.getId(), testPayment.getTenantContext().getTenantId());

    // Then
    assertThat(result).isPresent();
    assertThat(result.get().getId()).isEqualTo(testPayment.getId());
    assertThat(result.get().getTenantContext().getTenantId())
        .isEqualTo(testPayment.getTenantContext().getTenantId());
  }

  @Test
  void findByIdAndTenantId_ShouldReturnEmpty_WhenPaymentDoesNotExistForTenant() {
    // Given
    String differentTenantId = "DIFFERENT-TENANT";

    // When
    Optional<Payment> result =
        paymentRepository.findByIdAndTenantId(testPayment.getId(), differentTenantId);

    // Then
    assertThat(result).isEmpty();
  }

  @Test
  void findByTenantIdAndDateRange_ShouldReturnPayments_WhenPaymentsExistInRange() {
    // Given
    Instant startDate = Instant.now().minusSeconds(3600); // 1 hour ago
    Instant endDate = Instant.now().plusSeconds(3600); // 1 hour from now
    PageRequest pageRequest = PageRequest.of(0, 10);

    // When
    Page<Payment> result =
        paymentRepository.findByTenantIdAndDateRange(
            testPayment.getTenantContext().getTenantId(), startDate, endDate, pageRequest);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getContent()).hasSize(1);
    assertThat(result.getContent().get(0).getId()).isEqualTo(testPayment.getId());
  }

  @Test
  void findByTenantIdAndDateRange_ShouldReturnEmpty_WhenNoPaymentsInRange() {
    // Given
    Instant startDate = Instant.now().plusSeconds(3600); // 1 hour from now
    Instant endDate = Instant.now().plusSeconds(7200); // 2 hours from now
    PageRequest pageRequest = PageRequest.of(0, 10);

    // When
    Page<Payment> result =
        paymentRepository.findByTenantIdAndDateRange(
            testPayment.getTenantContext().getTenantId(), startDate, endDate, pageRequest);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getContent()).isEmpty();
  }

  @Test
  void save_ShouldPersistPayment_WhenValidPayment() {
    // Given
    Payment newPayment = createTestPayment();
    newPayment.setId(new PaymentId("NEW-PAYMENT"));

    // When
    Payment savedPayment = paymentRepository.save(newPayment);
    entityManager.flush();

    // Then
    assertThat(savedPayment).isNotNull();
    assertThat(savedPayment.getId()).isEqualTo(newPayment.getId());

    // Verify it's persisted
    Optional<Payment> retrieved = paymentRepository.findById(newPayment.getId());
    assertThat(retrieved).isPresent();
    assertThat(retrieved.get().getId()).isEqualTo(newPayment.getId());
  }

  @Test
  void save_ShouldUpdatePayment_WhenPaymentExists() {
    // Given
    testPayment.setStatus(PaymentStatus.VALIDATED);
    testPayment.setReference("Updated reference");

    // When
    Payment updatedPayment = paymentRepository.save(testPayment);
    entityManager.flush();

    // Then
    assertThat(updatedPayment.getStatus()).isEqualTo(PaymentStatus.VALIDATED);
    assertThat(updatedPayment.getReference()).isEqualTo("Updated reference");

    // Verify it's updated in database
    Optional<Payment> retrieved = paymentRepository.findById(testPayment.getId());
    assertThat(retrieved).isPresent();
    assertThat(retrieved.get().getStatus()).isEqualTo(PaymentStatus.VALIDATED);
    assertThat(retrieved.get().getReference()).isEqualTo("Updated reference");
  }

  @Test
  void findByTenantIdAndBusinessUnitId_ShouldReturnPayments_WhenPaymentsExist() {
    // Given
    PageRequest pageRequest = PageRequest.of(0, 10);

    // When
    Page<Payment> result =
        paymentRepository.findByTenantIdAndBusinessUnitId(
            testPayment.getTenantContext().getTenantId(),
            testPayment.getTenantContext().getBusinessUnitId(),
            pageRequest);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getContent()).hasSize(1);
    assertThat(result.getContent().get(0).getId()).isEqualTo(testPayment.getId());
  }

  @Test
  void findByTenantIdAndBusinessUnitId_ShouldReturnEmpty_WhenNoPaymentsExist() {
    // Given
    String differentTenantId = "DIFFERENT-TENANT";
    String differentBusinessUnitId = "DIFFERENT-BU";
    PageRequest pageRequest = PageRequest.of(0, 10);

    // When
    Page<Payment> result =
        paymentRepository.findByTenantIdAndBusinessUnitId(
            differentTenantId, differentBusinessUnitId, pageRequest);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getContent()).isEmpty();
  }

  private Payment createTestPayment() {
    return Payment.builder()
        .id(new PaymentId("TEST-PAYMENT-001"))
        .idempotencyKey("TEST-IDEMPOTENCY-001")
        .sourceAccount(new AccountNumber("12345678901"))
        .destinationAccount(new AccountNumber("98765432109"))
        .amount(new Money(BigDecimal.valueOf(1000.00), "ZAR"))
        .reference("Test payment")
        .paymentType(PaymentType.EFT)
        .priority(Priority.NORMAL)
        .tenantContext(
            TenantContext.builder()
                .tenantId("TEST-TENANT-001")
                .businessUnitId("TEST-BU-001")
                .build())
        .status(PaymentStatus.INITIATED)
        .initiatedBy("test@example.com")
        .initiatedAt(Instant.now())
        .build();
  }
}
