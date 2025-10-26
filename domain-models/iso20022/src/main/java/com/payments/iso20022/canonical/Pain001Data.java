package com.payments.iso20022.canonical;

import com.payments.domain.shared.Money;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Currency;
import java.util.List;
import lombok.Builder;
import lombok.Data;

/** Data class for pain.001 message information */
@Data
@Builder
public class Pain001Data {

  private String messageId;
  private String paymentId;
  private String endToEndId;
  private String instructionId;
  private String transactionId;

  private Money amount;
  private Currency currency;

  private String sourceAccount;
  private String destinationAccount;

  private CanonicalPaymentModel.PartyInformation debtor;
  private CanonicalPaymentModel.PartyInformation creditor;
  private CanonicalPaymentModel.PartyInformation ultimateDebtor;
  private CanonicalPaymentModel.PartyInformation ultimateCreditor;

  private CanonicalPaymentModel.AgentInformation debtorAgent;
  private CanonicalPaymentModel.AgentInformation creditorAgent;
  private CanonicalPaymentModel.AgentInformation intermediaryAgent;

  private LocalDate executionDate;
  private LocalDate requestedExecutionDate;
  private LocalDateTime creationDateTime;

  private String paymentMethod;
  private String paymentType;
  private String serviceLevel;
  private String localInstrument;
  private String categoryPurpose;
  private String chargeBearer;

  private String reference;
  private String purpose;
  private String remittanceInformation;
  private List<String> unstructuredRemittanceInformation;
  private CanonicalPaymentModel.StructuredRemittanceInformation structuredRemittanceInformation;

  private List<CanonicalPaymentModel.ChargesInformation> charges;
  private Money totalCharges;
  private String chargeBearerCode;

  private String regulatoryReporting;
  private String regulatoryCode;
  private String regulatoryText;

  private String businessUnitId;
  private String processingChannel;
  private String clearingSystem;

  private String originalMessageId;
  private String originalMessageType;
  private String correlationId;
  private String trackingId;
  private String batchId;

  private boolean iso20022Compliant;
  private List<String> validationErrors;
  private List<String> complianceWarnings;
  private String validationStatus;
}
