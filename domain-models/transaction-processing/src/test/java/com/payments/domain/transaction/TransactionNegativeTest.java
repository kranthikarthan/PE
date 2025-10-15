package com.payments.domain.transaction;

import static org.junit.jupiter.api.Assertions.*;

import com.payments.domain.shared.*;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class TransactionNegativeTest {

  private Transaction newCreatedTx() {
    return Transaction.create(
        TransactionId.generate(),
        TenantContext.of("t1", "Tenant One", "bu1", "BU One"),
        PaymentId.generate(),
        AccountNumber.of("12345678901"),
        AccountNumber.of("10987654321"),
        Money.zar(new BigDecimal("50.00")),
        TransactionType.CREDIT);
  }

  @Test
  void create_fails_when_amount_non_positive() {
    assertThrows(
        InvalidTransactionException.class,
        () ->
            Transaction.create(
                TransactionId.generate(),
                TenantContext.of("t1", "Tenant One", "bu1", "BU One"),
                PaymentId.generate(),
                AccountNumber.of("12345678901"),
                AccountNumber.of("10987654321"),
                Money.zar(new BigDecimal("0.00")),
                TransactionType.CREDIT));
  }

  @Test
  void create_fails_when_debit_equals_credit() {
    AccountNumber same = AccountNumber.of("12345678901");
    assertThrows(
        InvalidTransactionException.class,
        () ->
            Transaction.create(
                TransactionId.generate(),
                TenantContext.of("t1", "Tenant One", "bu1", "BU One"),
                PaymentId.generate(),
                same,
                same,
                Money.zar(new BigDecimal("1.00")),
                TransactionType.CREDIT));
  }

  @Test
  void startProcessing_fails_when_not_created() {
    Transaction tx = newCreatedTx();
    tx.startProcessing();
    assertThrows(InvalidStateTransitionException.class, tx::startProcessing);
  }

  @Test
  void markCleared_fails_when_not_processing() {
    Transaction tx = newCreatedTx();
    assertThrows(InvalidStateTransitionException.class, () -> tx.markCleared("SAMOS", "X"));
  }

  @Test
  void complete_fails_when_not_clearing() {
    Transaction tx = newCreatedTx();
    tx.startProcessing();
    assertThrows(InvalidStateTransitionException.class, tx::complete);
  }

  @Test
  void fail_fails_when_already_completed() {
    Transaction tx = newCreatedTx();
    tx.startProcessing();
    tx.markCleared("SAMOS", "CLR-1");
    tx.complete();
    assertThrows(InvalidStateTransitionException.class, () -> tx.fail("late"));
  }
}
