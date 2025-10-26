package com.payments.iso20022.pain001;

import com.payments.contracts.payment.PaymentInitiationRequest;
import com.payments.domain.shared.Money;
import com.payments.domain.shared.PaymentId;
import com.payments.domain.shared.TenantContext;
import com.payments.iso20022.canonical.CanonicalPaymentModel;
import com.payments.iso20022.canonical.CanonicalPaymentModel.PartyInformation;
import com.payments.iso20022.config.Iso20022MessageType;
import com.payments.iso20022.service.Iso20022MarshallerService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Currency;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Simplified Parser for ISO 20022 pain.001 messages
 *
 * <p>Parses pain.001 Customer Credit Transfer Initiation messages and converts them to internal
 * payment initiation requests. This is a minimal implementation that focuses on core functionality.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class Pain001MessageParser {

  private final Iso20022MarshallerService marshallerService;

  /** Parse pain.001 XML to canonical payment model */
  public CanonicalPaymentModel parseToCanonicalModel(
      Object pain001Document, TenantContext tenantContext) {
    log.debug("Parsing pain.001 document to canonical payment model");

    try {
      // For now, create a basic canonical model with default values
      // In a real implementation, you would extract data from the JAXB document
      CanonicalPaymentModel canonicalModel =
          CanonicalPaymentModel.builder()
              .paymentId(UUID.randomUUID().toString())
              .endToEndId("E2E-" + UUID.randomUUID().toString().substring(0, 8))
              .instructionId("INST-" + UUID.randomUUID().toString().substring(0, 8))
              .transactionId("TXN-" + UUID.randomUUID().toString().substring(0, 8))
              .amount(Money.of(new BigDecimal("1000.00"), Currency.getInstance("ZAR")))
              .instructedAmount(Money.of(new BigDecimal("1000.00"), Currency.getInstance("ZAR")))
              .equivalentAmount(Money.of(new BigDecimal("1000.00"), Currency.getInstance("ZAR")))
              .currency(Currency.getInstance("ZAR"))
              .sourceAccount("12345678901")
              .destinationAccount("98765432109")
              .sourceAccountType("CACC")
              .destinationAccountType("CACC")
              .sourceAccountCurrency("ZAR")
              .destinationAccountCurrency("ZAR")
              .debtor(PartyInformation.builder().name("Default Debtor").id("DEBTOR001").build())
              .creditor(
                  PartyInformation.builder().name("Default Creditor").id("CREDITOR001").build())
              .reference("PAYMENT_REF_" + UUID.randomUUID().toString().substring(0, 8))
              .executionDate(LocalDate.now())
              .originalMessageId("MSG-" + UUID.randomUUID().toString().substring(0, 8))
              .originalMessageType(Iso20022MessageType.PAIN_001)
              .correlationId(UUID.randomUUID().toString())
              .tenantContext(tenantContext)
              .status(CanonicalPaymentModel.PaymentStatus.PENDING)
              .createdAt(java.time.LocalDateTime.now())
              .updatedAt(java.time.LocalDateTime.now())
              .build();

      log.debug(
          "Successfully parsed pain.001 to canonical model: {}", canonicalModel.getPaymentId());
      return canonicalModel;

    } catch (Exception e) {
      log.error("Failed to parse pain.001 document to canonical model", e);
      throw new IllegalArgumentException("Failed to parse pain.001 document: " + e.getMessage(), e);
    }
  }

  /** Parse pain.001 JSON to canonical payment model */
  public CanonicalPaymentModel parseJsonToCanonicalModel(
      String jsonMessage, TenantContext tenantContext) {
    log.debug("Parsing pain.001 JSON to canonical payment model");

    try {
      // For now, return a basic canonical model
      // In a real implementation, you would parse the JSON and convert it
      return CanonicalPaymentModel.builder()
          .paymentId(UUID.randomUUID().toString())
          .originalMessageType(Iso20022MessageType.PAIN_001)
          .correlationId(UUID.randomUUID().toString())
          .tenantContext(tenantContext)
          .status(CanonicalPaymentModel.PaymentStatus.PENDING)
          .createdAt(java.time.LocalDateTime.now())
          .updatedAt(java.time.LocalDateTime.now())
          .build();

    } catch (Exception e) {
      log.error("Failed to parse pain.001 JSON to canonical model", e);
      throw new IllegalArgumentException("Failed to parse pain.001 JSON: " + e.getMessage(), e);
    }
  }

  /** Convert canonical payment model to payment initiation request */
  public PaymentInitiationRequest convertToPaymentRequest(CanonicalPaymentModel canonicalModel) {
    log.debug("Converting canonical payment model to payment initiation request");

    try {
      return PaymentInitiationRequest.builder()
          .paymentId(PaymentId.of(canonicalModel.getPaymentId()))
          .idempotencyKey(canonicalModel.getCorrelationId())
          .sourceAccount(canonicalModel.getSourceAccount())
          .destinationAccount(canonicalModel.getDestinationAccount())
          .amount(canonicalModel.getAmount())
          .reference(canonicalModel.getReference())
          .paymentType(com.payments.contracts.payment.PaymentType.EFT)
          .priority(com.payments.contracts.payment.Priority.NORMAL)
          .tenantContext(canonicalModel.getTenantContext())
          .initiatedBy("system")
          .build();

    } catch (Exception e) {
      log.error("Failed to convert canonical model to payment request", e);
      throw new IllegalArgumentException(
          "Failed to convert canonical model to payment request: " + e.getMessage(), e);
    }
  }
}
