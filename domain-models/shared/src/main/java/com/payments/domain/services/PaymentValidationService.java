package com.payments.domain.services;

import com.payments.domain.entities.Payment;
import com.payments.domain.shared.*;
import com.payments.domain.valueobjects.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/** Domain service for payment validation Contains business rules for payment validation */
public class PaymentValidationService {

  /** Validate a payment according to business rules */
  public ValidationResult validatePayment(Payment payment) {
    List<String> errors = new ArrayList<>();

    // Amount validation
    if (payment.getAmount() == null) {
      errors.add("Payment amount is required");
    } else if (payment.getAmount().getAmount().compareTo(BigDecimal.ZERO) <= 0) {
      errors.add("Payment amount must be greater than zero");
    } else if (payment.getAmount().getAmount().compareTo(new BigDecimal("1000000")) > 0) {
      errors.add("Payment amount exceeds maximum limit of 1,000,000");
    }

    // Account validation
    if (payment.getBeneficiaryAccount() == null
        || payment.getBeneficiaryAccount().trim().isEmpty()) {
      errors.add("Beneficiary account is required");
    } else if (!isValidAccountNumber(payment.getBeneficiaryAccount())) {
      errors.add("Invalid beneficiary account number format");
    }

    if (payment.getBeneficiaryBankCode() == null
        || payment.getBeneficiaryBankCode().trim().isEmpty()) {
      errors.add("Beneficiary bank code is required");
    } else if (!isValidBankCode(payment.getBeneficiaryBankCode())) {
      errors.add("Invalid beneficiary bank code format");
    }

    // Tenant validation
    if (payment.getTenantId() == null) {
      errors.add("Tenant ID is required");
    }

    // Description validation
    if (payment.getDescription() != null && payment.getDescription().length() > 200) {
      errors.add("Description cannot exceed 200 characters");
    }

    // Beneficiary name validation
    if (payment.getBeneficiaryName() != null && payment.getBeneficiaryName().length() > 100) {
      errors.add("Beneficiary name cannot exceed 100 characters");
    }

    return ValidationResult.builder().isValid(errors.isEmpty()).errors(errors).build();
  }

  /** Validate account number format */
  private boolean isValidAccountNumber(String accountNumber) {
    if (accountNumber == null) return false;

    // Basic validation - should be numeric and between 8-20 characters
    return accountNumber.matches("\\d{8,20}");
  }

  /** Validate bank code format */
  private boolean isValidBankCode(String bankCode) {
    if (bankCode == null) return false;

    // Basic validation - should be 6 digits
    return bankCode.matches("\\d{6}");
  }

  /** Check if payment can be processed based on business rules */
  public boolean canProcessPayment(Payment payment) {
    ValidationResult validation = validatePayment(payment);
    return validation.isValid() && !payment.isTerminal();
  }

  /** Get validation rules for a specific payment type */
  public List<String> getValidationRules(PaymentType paymentType) {
    List<String> rules = new ArrayList<>();

    switch (paymentType) {
      case RTGS:
        rules.add("RTGS payments require minimum amount of 1,000,000");
        rules.add("RTGS payments must be processed during business hours");
        break;
      case EFT:
        rules.add("EFT payments have daily limits");
        rules.add("EFT payments are processed in batches");
        break;
      case CARD_PAYMENT:
        rules.add("Card payments require card validation");
        rules.add("Card payments have transaction limits");
        break;
      default:
        rules.add("Standard payment validation rules apply");
    }

    return rules;
  }
}
