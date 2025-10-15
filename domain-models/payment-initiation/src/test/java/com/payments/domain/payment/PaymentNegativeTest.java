package com.payments.domain.payment;

import static org.junit.jupiter.api.Assertions.*;

import com.payments.domain.shared.*;
import com.payments.domain.validation.ValidationId;
import com.payments.domain.validation.ValidationResult;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class PaymentNegativeTest {

  private Payment newInitiatedPayment() {
    PaymentId paymentId = PaymentId.generate();
    TenantContext tenant = TenantContext.of("t1", "Tenant One", "bu1", "BU One");
    Money amount = Money.zar(new BigDecimal("10.00"));
    AccountNumber src = AccountNumber.of("12345678901");
    AccountNumber dst = AccountNumber.of("10987654321");
    PaymentReference ref = PaymentReference.of("REF-NEG");
    return Payment.initiate(
        paymentId,
        tenant,
        amount,
        src,
        dst,
        ref,
        PaymentType.EFT,
        Priority.NORMAL,
        "tester",
        "idem-neg-1");
  }

  @Test
  void initiate_fails_when_amount_non_positive() {
    TenantContext tenant = TenantContext.of("t1", "Tenant One", "bu1", "BU One");
    AccountNumber src = AccountNumber.of("12345678901");
    AccountNumber dst = AccountNumber.of("10987654321");
    PaymentReference ref = PaymentReference.of("REF-NEG");

    assertThrows(
        InvalidPaymentException.class,
        () ->
            Payment.initiate(
                PaymentId.generate(),
                tenant,
                Money.zar(new BigDecimal("0.00")),
                src,
                dst,
                ref,
                PaymentType.EFT,
                Priority.NORMAL,
                "tester",
                "idem-neg-2"));
  }

  @Test
  void initiate_fails_when_source_equals_destination() {
    TenantContext tenant = TenantContext.of("t1", "Tenant One", "bu1", "BU One");
    AccountNumber acc = AccountNumber.of("12345678901");
    PaymentReference ref = PaymentReference.of("REF-NEG");

    assertThrows(
        InvalidPaymentException.class,
        () ->
            Payment.initiate(
                PaymentId.generate(),
                tenant,
                Money.zar(new BigDecimal("1.00")),
                acc,
                acc,
                ref,
                PaymentType.EFT,
                Priority.NORMAL,
                "tester",
                "idem-neg-3"));
  }

  @Test
  void validate_fails_when_not_initiated() {
    Payment payment = newInitiatedPayment();
    // move to VALIDATED first, then try to validate again
    ValidationResult vr =
        ValidationResult.create(
            ValidationId.generate(), payment.getTenantContext(), payment.getId());
    vr.completeValidation(true, "ut");
    payment.validate(vr);

    assertThrows(InvalidStateTransitionException.class, () -> payment.validate(vr));
  }

  @Test
  void submitToClearing_fails_when_not_validated() {
    Payment payment = newInitiatedPayment();
    assertThrows(
        InvalidStateTransitionException.class,
        () -> payment.submitToClearing(ClearingSystemReference.of("CLS-NEG")));
  }

  @Test
  void complete_fails_when_not_cleared() {
    Payment payment = newInitiatedPayment();
    // validate
    ValidationResult vr =
        ValidationResult.create(
            ValidationId.generate(), payment.getTenantContext(), payment.getId());
    vr.completeValidation(true, "ut");
    payment.validate(vr);
    // still not cleared
    assertThrows(InvalidStateTransitionException.class, payment::complete);
  }

  @Test
  void fail_fails_when_already_completed() {
    Payment payment = newInitiatedPayment();
    ValidationResult vr =
        ValidationResult.create(
            ValidationId.generate(), payment.getTenantContext(), payment.getId());
    vr.completeValidation(true, "ut");
    payment.validate(vr);
    payment.submitToClearing(ClearingSystemReference.of("CLS-1"));
    payment.markCleared(ClearingConfirmation.of("CONF-1"));
    payment.complete();

    assertThrows(InvalidStateTransitionException.class, () -> payment.fail("late"));
  }
}
