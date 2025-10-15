package com.payments.domain.transaction;

import static org.junit.jupiter.api.Assertions.*;

import com.payments.domain.shared.*;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class TransactionTest {

  @Test
  void create_process_clear_complete_flow() {
    TransactionId txId = TransactionId.generate();
    TenantContext tenant = TenantContext.of("t1", "Tenant One", "bu1", "BU One");
    PaymentId paymentId = PaymentId.generate();
    AccountNumber debit = AccountNumber.of("12345678901");
    AccountNumber credit = AccountNumber.of("10987654321");
    Money amount = Money.zar(new BigDecimal("250.00"));

    Transaction tx =
        Transaction.create(txId, tenant, paymentId, debit, credit, amount, TransactionType.CREDIT);

    assertEquals(TransactionStatus.CREATED, tx.getStatus());

    tx.startProcessing();
    assertEquals(TransactionStatus.PROCESSING, tx.getStatus());

    tx.markCleared("SAMOS", "CLR-1");
    tx.complete();
    assertTrue(tx.isCompleted());
  }
}
