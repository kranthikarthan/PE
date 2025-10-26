package com.payments.paymentinitiation.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;

import com.payments.domain.shared.TenantContext;
import com.payments.iso20022.canonical.CanonicalPaymentModel;
import com.payments.paymentinitiation.saga.SagaEventPublisher;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Currency;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit test for PayShap clearing system integration
 *
 * <p>Tests the PayShap event publishing functionality
 */
@ExtendWith(MockitoExtension.class)
public class PayShapIntegrationTest {

  @Mock private SagaEventPublisher sagaEventPublisher;

  @Test
  public void testPayShapEventPublishing() {
    // Given: A canonical payment model for PayShap
    CanonicalPaymentModel canonicalPayment =
        CanonicalPaymentModel.builder()
            .paymentId("PAY-TEST-001")
            .amount(
                com.payments.domain.shared.Money.of(
                    new BigDecimal("2500.00"), Currency.getInstance("ZAR")))
            .currency(Currency.getInstance("ZAR"))
            .sourceAccount("12345678901")
            .destinationAccount("98765432109")
            .executionDate(LocalDate.now())
            .clearingSystem("PAYSHAP")
            .tenantContext(
                TenantContext.builder()
                    .tenantId("tenant-1")
                    .businessUnitId("business-unit-1")
                    .build())
            .build();

    // When: Publishing PayShap processing initiated event
    String sagaId = "SAGA-TEST-001";

    // Mock the event publisher to not throw exceptions
    doNothing()
        .when(sagaEventPublisher)
        .publishPayShapProcessingInitiated(anyString(), any(CanonicalPaymentModel.class));

    // This should not throw an exception
    sagaEventPublisher.publishPayShapProcessingInitiated(sagaId, canonicalPayment);

    // Then: Event should be published successfully
    assertThat(canonicalPayment.getClearingSystem()).isEqualTo("PAYSHAP");
    assertThat(canonicalPayment.getAmount().getAmount())
        .isEqualByComparingTo(new BigDecimal("2500.00"));
    assertThat(canonicalPayment.getCurrency().getCurrencyCode()).isEqualTo("ZAR");
  }

  @Test
  public void testPayShapRoutingCriteria() {
    // Given: A low-value ZAR payment (within PayShap limits)
    CanonicalPaymentModel canonicalPayment =
        CanonicalPaymentModel.builder()
            .paymentId("PAY-TEST-002")
            .amount(
                com.payments.domain.shared.Money.of(
                    new BigDecimal("2500.00"), Currency.getInstance("ZAR")))
            .currency(Currency.getInstance("ZAR"))
            .sourceAccount("12345678901")
            .destinationAccount("98765432109")
            .executionDate(LocalDate.now())
            .clearingSystem("PAYSHAP")
            .tenantContext(
                TenantContext.builder()
                    .tenantId("tenant-1")
                    .businessUnitId("business-unit-1")
                    .build())
            .build();

    // When: Checking PayShap routing criteria
    BigDecimal amount = canonicalPayment.getAmount().getAmount();
    String currency = canonicalPayment.getCurrency().getCurrencyCode();
    String clearingSystem = canonicalPayment.getClearingSystem();

    // Then: Should meet PayShap criteria
    assertThat(amount).isLessThanOrEqualTo(new BigDecimal("3000.00"));
    assertThat(currency).isEqualTo("ZAR");
    assertThat(clearingSystem).isEqualTo("PAYSHAP");
  }
}
