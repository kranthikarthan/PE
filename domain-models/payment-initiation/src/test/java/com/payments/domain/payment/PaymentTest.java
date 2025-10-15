package com.payments.domain.payment;

import static org.junit.jupiter.api.Assertions.*;

import com.payments.domain.shared.*;
import com.payments.domain.validation.ValidationResult;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class PaymentTest {

  @Test
  void initiate_and_happy_path_flow_completes() {
    PaymentId paymentId = PaymentId.generate();
    TenantContext tenant = TenantContext.of("t1", "Tenant One", "bu1", "BU One");
    Money amount = Money.zar(new BigDecimal("100.00"));
    AccountNumber src = AccountNumber.of("12345678901");
    AccountNumber dst = AccountNumber.of("10987654321");
    PaymentReference ref = PaymentReference.of("REF-1");

    Payment payment =
        Payment.initiate(
            paymentId,
            tenant,
            amount,
            src,
            dst,
            ref,
            PaymentType.EFT,
            Priority.NORMAL,
            "tester",
            "idem-pt-1");

    assertEquals(PaymentStatus.INITIATED, payment.getStatus());

    ValidationResult validation =
        ValidationResult.create(
            com.payments.domain.validation.ValidationId.generate(), tenant, paymentId);
    validation.completeValidation(true, "unit-test");
    payment.validate(validation);
    assertEquals(PaymentStatus.VALIDATED, payment.getStatus());

    payment.submitToClearing(ClearingSystemReference.of("CLS-1"));
    payment.markCleared(ClearingConfirmation.of("CONF-1"));
    payment.complete();

    assertEquals(PaymentStatus.COMPLETED, payment.getStatus());
  }
}
