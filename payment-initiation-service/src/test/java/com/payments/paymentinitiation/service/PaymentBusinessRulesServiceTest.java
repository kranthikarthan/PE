package com.payments.paymentinitiation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;

import com.payments.domain.payment.Payment;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

/**
 * Unit tests for PaymentBusinessRulesService
 *
 * <p>Tests business rules validation logic
 */
@ExtendWith(MockitoExtension.class)
class PaymentBusinessRulesServiceTest {

  @Mock private PaymentRepositoryPort paymentRepository;

  private PaymentBusinessRulesService businessRulesService;

  @BeforeEach
  void setUp() {
    businessRulesService = new PaymentBusinessRulesService(paymentRepository);
  }

  @Test
  void getBusinessRulesForTenant_ShouldReturnDefaultRules_WhenNoCustomRules() {
    // When
    var rules = businessRulesService.getBusinessRulesForTenant("TENANT-001");

    // Then
    assertThat(rules).isNotNull();
    assertThat(rules.getDailyLimit()).isEqualTo(BigDecimal.valueOf(1000000.00)); // 1M ZAR
    assertThat(rules.getVelocityLimit()).isEqualTo(10); // 10 payments per hour
    assertThat(rules.getMaxAmount()).isEqualTo(BigDecimal.valueOf(100000.00)); // 100K ZAR
    assertThat(rules.getMinAmount()).isEqualTo(BigDecimal.valueOf(1.00)); // 1 ZAR
    assertThat(rules.getAllowedPaymentTypes()).containsExactlyInAnyOrder("EFT", "RTGS", "CARD");
    assertThat(rules.getBlockedAccounts()).isEmpty();
    assertThat(rules.getComplianceRules()).isNotNull();
  }

  @Test
  void validateDailyLimit_ShouldPass_WhenUnderLimit() {
    // Given
    Payment payment = createValidPayment();
    TenantContext tenantContext = createValidTenantContext();

    // Mock existing payments under daily limit
    List<Payment> existingPayments =
        List.of(
            createPaymentWithAmount(BigDecimal.valueOf(100000.00)),
            createPaymentWithAmount(BigDecimal.valueOf(200000.00)));
    Page<Payment> paymentPage = new PageImpl<>(existingPayments);

    lenient()
        .when(
            paymentRepository.findByTenantIdAndDateRange(
                anyString(), any(Instant.class), any(Instant.class), any(Pageable.class)))
        .thenReturn(paymentPage);

    // When & Then
    assertThatThrownBy(() -> businessRulesService.validateDailyLimit(payment, tenantContext))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Daily payment limit exceeded");
  }

  @Test
  void validateDailyLimit_ShouldThrowException_WhenOverLimit() {
    // Given
    Payment payment = createValidPayment();
    TenantContext tenantContext = createValidTenantContext();

    // Mock existing payments that exceed daily limit
    List<Payment> existingPayments =
        List.of(
            createPaymentWithAmount(BigDecimal.valueOf(500000.00)),
            createPaymentWithAmount(BigDecimal.valueOf(400000.00)));
    Page<Payment> paymentPage = new PageImpl<>(existingPayments);

    lenient()
        .when(
            paymentRepository.findByTenantIdAndDateRange(
                anyString(), any(Instant.class), any(Instant.class), any(Pageable.class)))
        .thenReturn(paymentPage);

    // When & Then
    assertThatThrownBy(() -> businessRulesService.validateDailyLimit(payment, tenantContext))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Daily payment limit exceeded");
  }

  @Test
  void validateVelocityLimit_ShouldPass_WhenUnderLimit() {
    // Given
    Payment payment = createValidPayment();
    TenantContext tenantContext = createValidTenantContext();

    // Mock few recent payments
    List<Payment> recentPayments =
        List.of(
            createPaymentWithAmount(BigDecimal.valueOf(100.00)),
            createPaymentWithAmount(BigDecimal.valueOf(200.00)));
    Page<Payment> paymentPage = new PageImpl<>(recentPayments);

    lenient()
        .when(
            paymentRepository.findByTenantIdAndDateRange(
                anyString(), any(Instant.class), any(Instant.class), any(Pageable.class)))
        .thenReturn(paymentPage);

    // When & Then
    assertThatThrownBy(() -> businessRulesService.validateVelocityLimit(payment, tenantContext))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Payment velocity limit exceeded");
  }

  @Test
  void validateVelocityLimit_ShouldThrowException_WhenOverLimit() {
    // Given
    Payment payment = createValidPayment();
    TenantContext tenantContext = createValidTenantContext();

    // Mock many recent payments to exceed velocity limit
    List<Payment> recentPayments =
        List.of(
            createPaymentWithAmount(BigDecimal.valueOf(100.00)),
            createPaymentWithAmount(BigDecimal.valueOf(200.00)),
            createPaymentWithAmount(BigDecimal.valueOf(300.00)),
            createPaymentWithAmount(BigDecimal.valueOf(400.00)),
            createPaymentWithAmount(BigDecimal.valueOf(500.00)),
            createPaymentWithAmount(BigDecimal.valueOf(600.00)),
            createPaymentWithAmount(BigDecimal.valueOf(700.00)),
            createPaymentWithAmount(BigDecimal.valueOf(800.00)),
            createPaymentWithAmount(BigDecimal.valueOf(900.00)),
            createPaymentWithAmount(BigDecimal.valueOf(1000.00)),
            createPaymentWithAmount(BigDecimal.valueOf(1100.00)) // 11th payment
            );
    Page<Payment> paymentPage = new PageImpl<>(recentPayments);

    lenient()
        .when(
            paymentRepository.findByTenantIdAndDateRange(
                anyString(), any(Instant.class), any(Instant.class), any(Pageable.class)))
        .thenReturn(paymentPage);

    // When & Then
    assertThatThrownBy(() -> businessRulesService.validateVelocityLimit(payment, tenantContext))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Payment velocity limit exceeded");
  }

  @Test
  void validateAmountLimits_ShouldPass_WhenWithinLimits() {
    // Given
    Payment payment = createValidPayment();
    TenantContext tenantContext = createValidTenantContext();

    // When & Then
    businessRulesService.validateAmountLimits(payment, tenantContext);
    // Should not throw exception
  }

  @Test
  void validateAmountLimits_ShouldThrowException_WhenOverMaxAmount() {
    // Given
    Payment payment = createPaymentWithAmount(BigDecimal.valueOf(200000.00)); // Over 100K limit
    TenantContext tenantContext = createValidTenantContext();

    // When & Then
    assertThatThrownBy(() -> businessRulesService.validateAmountLimits(payment, tenantContext))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Payment amount exceeds maximum allowed");
  }

  @Test
  void validateAmountLimits_ShouldThrowException_WhenUnderMinAmount() {
    // Given
    Payment payment = createPaymentWithAmount(BigDecimal.valueOf(0.50)); // Under 1 ZAR limit
    TenantContext tenantContext = createValidTenantContext();

    // When & Then
    assertThatThrownBy(() -> businessRulesService.validateAmountLimits(payment, tenantContext))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Payment amount is below minimum allowed");
  }

  @Test
  void validateAccountRestrictions_ShouldPass_WhenAccountsNotBlocked() {
    // Given
    Payment payment = createValidPayment();
    TenantContext tenantContext = createValidTenantContext();

    // When & Then
    businessRulesService.validateAccountRestrictions(payment, tenantContext);
    // Should not throw exception
  }

  @Test
  void validateAccountRestrictions_ShouldThrowException_WhenSourceAccountBlocked() {
    // Given
    Payment payment = createValidPayment();
    TenantContext tenantContext = createValidTenantContext();

    // Mock business rules with blocked source account
    var rules = businessRulesService.getBusinessRulesForTenant(tenantContext.getTenantId());
    rules.getBlockedAccounts().add(payment.getSourceAccount().getValue());

    // When & Then
    assertThatThrownBy(
            () -> businessRulesService.validateAccountRestrictions(payment, tenantContext))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Source account is blocked");
  }

  @Test
  void validateAccountRestrictions_ShouldThrowException_WhenDestinationAccountBlocked() {
    // Given
    Payment payment = createValidPayment();
    TenantContext tenantContext = createValidTenantContext();

    // Mock business rules with blocked destination account
    var rules = businessRulesService.getBusinessRulesForTenant(tenantContext.getTenantId());
    rules.getBlockedAccounts().add(payment.getDestinationAccount().getValue());

    // When & Then
    assertThatThrownBy(
            () -> businessRulesService.validateAccountRestrictions(payment, tenantContext))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Destination account is blocked");
  }

  @Test
  void validateCompliance_ShouldPass_WhenCompliant() {
    // Given
    Payment payment = createValidPayment();
    TenantContext tenantContext = createValidTenantContext();

    // When & Then
    businessRulesService.validateCompliance(payment, tenantContext);
    // Should not throw exception
  }

  @Test
  void validateCompliance_ShouldThrowException_WhenNonCompliant() {
    // Given
    Payment payment = createValidPayment();
    payment.setReference(null); // Null reference to simulate non-compliance
    TenantContext tenantContext = createValidTenantContext();

    // When & Then
    assertThatThrownBy(() -> businessRulesService.validateCompliance(payment, tenantContext))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Payment does not meet compliance requirements");
  }

  private Payment createValidPayment() {
    return Payment.initiate(
        new PaymentId("TEST-PAYMENT-001"),
        createValidTenantContext(),
        Money.zar(BigDecimal.valueOf(1000.00)),
        AccountNumber.of("12345678901"),
        AccountNumber.of("98765432109"),
        com.payments.domain.payment.PaymentReference.of("Test payment"),
        PaymentType.EFT,
        Priority.NORMAL,
        "test@example.com",
        "TEST-IDEMPOTENCY-001");
  }

  private Payment createPaymentWithAmount(BigDecimal amount) {
    return Payment.initiate(
        new PaymentId("TEST-PAYMENT-EXISTING"),
        createValidTenantContext(),
        Money.zar(amount),
        AccountNumber.of("12345678901"),
        AccountNumber.of("98765432109"),
        com.payments.domain.payment.PaymentReference.of("Existing payment"),
        PaymentType.EFT,
        Priority.NORMAL,
        "test@example.com",
        "TEST-IDEMPOTENCY-EXISTING");
  }

  private TenantContext createValidTenantContext() {
    return TenantContext.builder()
        .tenantId("TEST-TENANT-001")
        .businessUnitId("TEST-BU-001")
        .build();
  }
}
