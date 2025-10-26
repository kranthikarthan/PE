package com.payments.iso20022.canonical;

import com.payments.domain.shared.Money;
import com.payments.domain.shared.TenantContext;
import com.payments.iso20022.config.Iso20022MessageType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Currency;
import java.util.List;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * Canonical Payment Model for internal processing
 *
 * <p>This is the internal representation of a payment that can be processed regardless of the
 * original message format (pain.001, pacs.008, etc.). It serves as the bridge between incoming ISO
 * 20022 messages and the payment processing engine.
 */
@Data
@Builder
@Slf4j
public class CanonicalPaymentModel {

  // Core Payment Information
  private String paymentId;
  private String endToEndId;
  private String instructionId;
  private String transactionId;

  // Amount and Currency
  private Money amount;
  private Money instructedAmount;
  private Money equivalentAmount;
  private Currency currency;

  // Account Information
  private String sourceAccount;
  private String destinationAccount;
  private String sourceAccountType;
  private String destinationAccountType;
  private String sourceAccountCurrency;
  private String destinationAccountCurrency;

  // Party Information
  private PartyInformation debtor;
  private PartyInformation creditor;
  private PartyInformation ultimateDebtor;
  private PartyInformation ultimateCreditor;

  // Agent Information
  private AgentInformation debtorAgent;
  private AgentInformation creditorAgent;
  private AgentInformation intermediaryAgent;

  // Dates and Timing
  private LocalDate executionDate;
  private LocalDate requestedExecutionDate;
  private LocalDateTime creationDateTime;
  private LocalDateTime acceptanceDateTime;
  private LocalDateTime processingDateTime;

  // Payment Details
  private String paymentMethod;
  private String paymentType;
  private String serviceLevel;
  private String localInstrument;
  private String categoryPurpose;
  private String chargeBearer;

  // Reference Information
  private String reference;
  private String purpose;
  private String remittanceInformation;
  private List<String> unstructuredRemittanceInformation;
  private StructuredRemittanceInformation structuredRemittanceInformation;

  // Status Information
  private PaymentStatus status;
  private String statusReason;
  private String statusReasonCode;
  private String additionalStatusInformation;

  // Charges Information
  private List<ChargesInformation> charges;
  private Money totalCharges;
  private String chargeBearerCode;

  // Regulatory Information
  private String regulatoryReporting;
  private String regulatoryCode;
  private String regulatoryText;

  // Tenant and Context
  private TenantContext tenantContext;
  private String businessUnitId;
  private String processingChannel;
  private String clearingSystem;

  // Correlation and Tracking
  private String originalMessageId;
  private Iso20022MessageType originalMessageType;
  private String correlationId;
  private String trackingId;
  private String batchId;

  // Validation and Compliance
  private boolean iso20022Compliant;
  private List<String> validationErrors;
  private List<String> complianceWarnings;
  private String validationStatus;

  // Audit Information
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
  private String createdBy;
  private String updatedBy;
  private String version;

  /** Payment Status Enumeration */
  public enum PaymentStatus {
    PENDING("PENDING", "Payment is pending processing"),
    ACCEPTED("ACCEPTED", "Payment has been accepted"),
    PROCESSING("PROCESSING", "Payment is being processed"),
    COMPLETED("COMPLETED", "Payment has been completed"),
    REJECTED("REJECTED", "Payment has been rejected"),
    CANCELLED("CANCELLED", "Payment has been cancelled"),
    FAILED("FAILED", "Payment processing failed"),
    RETURNED("RETURNED", "Payment has been returned"),
    PARTIALLY_COMPLETED("PARTIALLY_COMPLETED", "Payment partially completed");

    private final String code;
    private final String description;

    PaymentStatus(String code, String description) {
      this.code = code;
      this.description = description;
    }

    public String getCode() {
      return code;
    }

    public String getDescription() {
      return description;
    }
  }

  /** Party Information */
  @Data
  @Builder
  public static class PartyInformation {
    private String name;
    private String id;
    private String idType;
    private String idScheme;
    private String issuer;
    private AddressInformation address;
    private ContactInformation contact;
    private String countryOfResidence;
    private String legalEntityIdentifier;
    private String businessIdentifierCode;
  }

  /** Agent Information */
  @Data
  @Builder
  public static class AgentInformation {
    private String bic;
    private String name;
    private String clearingSystemId;
    private String memberId;
    private AddressInformation address;
    private String country;
    private String branchId;
    private String branchName;
  }

  /** Address Information */
  @Data
  @Builder
  public static class AddressInformation {
    private String addressType;
    private String department;
    private String subDepartment;
    private String streetName;
    private String buildingNumber;
    private String postCode;
    private String townName;
    private String countrySubDivision;
    private String country;
    private List<String> addressLines;
  }

  /** Contact Information */
  @Data
  @Builder
  public static class ContactInformation {
    private String namePrefix;
    private String name;
    private String phoneNumber;
    private String mobileNumber;
    private String faxNumber;
    private String emailAddress;
    private String other;
  }

  /** Structured Remittance Information */
  @Data
  @Builder
  public static class StructuredRemittanceInformation {
    private List<ReferredDocumentInformation> referredDocuments;
    private RemittanceAmount remittanceAmount;
    private CreditorReferenceInformation creditorReference;
    private PartyInformation invoicee;
    private PartyInformation invoicer;
    private TaxInformation taxInformation;
    private GarnishmentInformation garnishmentInformation;
    private List<String> additionalRemittanceInformation;
  }

  /** Referred Document Information */
  @Data
  @Builder
  public static class ReferredDocumentInformation {
    private String type;
    private String number;
    private LocalDate relatedDate;
    private String issuer;
  }

  /** Remittance Amount */
  @Data
  @Builder
  public static class RemittanceAmount {
    private Money duePayableAmount;
    private Money discountAppliedAmount;
    private Money creditNoteAmount;
    private Money taxAmount;
    private List<DocumentAdjustment> adjustmentAmounts;
    private Money remittedAmount;
  }

  /** Document Adjustment */
  @Data
  @Builder
  public static class DocumentAdjustment {
    private Money amount;
    private String creditDebitIndicator;
    private String reason;
    private String additionalInformation;
  }

  /** Creditor Reference Information */
  @Data
  @Builder
  public static class CreditorReferenceInformation {
    private String type;
    private String reference;
    private String issuer;
  }

  /** Tax Information */
  @Data
  @Builder
  public static class TaxInformation {
    private TaxParty creditor;
    private TaxParty debtor;
    private String administrationZone;
    private String referenceNumber;
    private String method;
    private Money totalTaxableBaseAmount;
    private Money totalTaxAmount;
    private LocalDate date;
    private Integer sequenceNumber;
    private List<TaxRecord> taxRecords;
  }

  /** Tax Party */
  @Data
  @Builder
  public static class TaxParty {
    private String taxId;
    private String registrationId;
    private String taxType;
    private TaxAuthorisation authorisation;
  }

  /** Tax Authorisation */
  @Data
  @Builder
  public static class TaxAuthorisation {
    private String title;
    private String name;
  }

  /** Tax Record */
  @Data
  @Builder
  public static class TaxRecord {
    private String type;
    private String category;
    private String categoryDetails;
    private String debtorStatus;
    private String certificateId;
    private String formsCode;
    private TaxPeriod period;
    private TaxAmount amount;
    private String additionalInformation;
  }

  /** Tax Period */
  @Data
  @Builder
  public static class TaxPeriod {
    private LocalDate year;
    private String type;
    private DatePeriodDetails fromToDate;
  }

  /** Date Period Details */
  @Data
  @Builder
  public static class DatePeriodDetails {
    private LocalDate fromDate;
    private LocalDate toDate;
  }

  /** Tax Amount */
  @Data
  @Builder
  public static class TaxAmount {
    private BigDecimal rate;
    private Money taxableBaseAmount;
    private Money totalAmount;
    private List<TaxRecordDetails> details;
  }

  /** Tax Record Details */
  @Data
  @Builder
  public static class TaxRecordDetails {
    private TaxPeriod period;
    private Money amount;
  }

  /** Garnishment Information */
  @Data
  @Builder
  public static class GarnishmentInformation {
    private String type;
    private PartyInformation garnishee;
    private PartyInformation garnishmentAdministrator;
    private String referenceNumber;
    private LocalDate date;
    private Money remittedAmount;
    private Boolean familyMedicalInsuranceIndicator;
    private Boolean employeeTerminationIndicator;
  }

  /** Charges Information */
  @Data
  @Builder
  public static class ChargesInformation {
    private Money amount;
    private AgentInformation agent;
    private String type;
    private String bearer;
  }

  /** Create a new canonical payment model from pain.001 data */
  public static CanonicalPaymentModel fromPain001(
      Pain001Data pain001Data, TenantContext tenantContext) {
    return CanonicalPaymentModel.builder()
        .paymentId(pain001Data.getPaymentId())
        .endToEndId(pain001Data.getEndToEndId())
        .instructionId(pain001Data.getInstructionId())
        .amount(pain001Data.getAmount())
        .instructedAmount(pain001Data.getAmount())
        .currency(pain001Data.getAmount().getCurrency())
        .sourceAccount(pain001Data.getSourceAccount())
        .destinationAccount(pain001Data.getDestinationAccount())
        .debtor(pain001Data.getDebtor())
        .creditor(pain001Data.getCreditor())
        .debtorAgent(pain001Data.getDebtorAgent())
        .creditorAgent(pain001Data.getCreditorAgent())
        .executionDate(pain001Data.getExecutionDate())
        .requestedExecutionDate(pain001Data.getExecutionDate())
        .creationDateTime(pain001Data.getCreationDateTime())
        .paymentMethod(pain001Data.getPaymentMethod())
        .paymentType(pain001Data.getPaymentType())
        .serviceLevel(pain001Data.getServiceLevel())
        .localInstrument(pain001Data.getLocalInstrument())
        .categoryPurpose(pain001Data.getCategoryPurpose())
        .reference(pain001Data.getReference())
        .remittanceInformation(pain001Data.getRemittanceInformation())
        .unstructuredRemittanceInformation(pain001Data.getUnstructuredRemittanceInformation())
        .status(PaymentStatus.PENDING)
        .iso20022Compliant(true)
        .originalMessageType(Iso20022MessageType.PAIN_001)
        .originalMessageId(pain001Data.getMessageId())
        .tenantContext(tenantContext)
        .createdAt(LocalDateTime.now())
        .version("1.0")
        .build();
  }

  /** Convert to pain.002 data for status reporting */
  public Pain002Data toPain002Data() {
    return Pain002Data.builder()
        .messageId(generateMessageId())
        .originalMessageId(this.originalMessageId)
        .originalMessageType(this.originalMessageType.name())
        .paymentId(this.paymentId)
        .endToEndId(this.endToEndId)
        .instructionId(this.instructionId)
        .status(this.status.getCode())
        .statusReason(this.statusReason)
        .statusReasonCode(this.statusReasonCode)
        .amount(this.amount)
        .currency(this.currency)
        .sourceAccount(this.sourceAccount)
        .destinationAccount(this.destinationAccount)
        .executionDate(this.executionDate)
        .processingDateTime(this.processingDateTime)
        .acceptanceDateTime(this.acceptanceDateTime)
        .charges(this.charges)
        .totalCharges(this.totalCharges)
        .build();
  }

  /** Generate a unique message ID */
  private String generateMessageId() {
    return "PAYMENT-STATUS-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
  }

  // Additional getter methods for database service compatibility
  public String getMessageId() {
    return this.originalMessageId;
  }

  public Integer getNumberOfTransactions() {
    return 1; // Default to 1 transaction per payment
  }

  public BigDecimal getControlSum() {
    return this.amount != null ? this.amount.getAmount() : BigDecimal.ZERO;
  }

  public String getInitiatingPartyName() {
    return this.debtor != null ? this.debtor.getName() : null;
  }

  public String getInitiatingPartyId() {
    return this.debtor != null ? this.debtor.getId() : null;
  }

  public String getChargesAgentBic() {
    if (this.charges != null && !this.charges.isEmpty()) {
      ChargesInformation firstCharge = this.charges.get(0);
      return firstCharge.getAgent() != null ? firstCharge.getAgent().getBic() : null;
    }
    return null;
  }
}
