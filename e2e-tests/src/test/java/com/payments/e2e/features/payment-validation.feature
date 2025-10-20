# Payment Validation E2E Tests

@payment-validation @business-rules
Feature: Payment Validation
  As a payment system
  I want to validate payments against business rules
  So that only legitimate payments are processed

  Background:
    Given the payment system is running
    And test tenant "TENANT-TEST-001" is configured
    And business rules are loaded
    And compliance rules are configured

  @validation-success @business-rules-pass
  Scenario: Payment Passes All Validation Rules
    Given a valid payment request is created
      | Field | Value |
      | From Account | ACC-TEST-001 |
      | To Account | ACC-TEST-002 |
      | Amount | 100.00 |
      | Currency | ZAR |
      | Reference | VAL-TEST-001 |
    When the payment is submitted for validation
    Then the business rules should pass
    And the compliance rules should pass
    And the fraud detection should pass
    And the risk assessment should be low
    And the payment should be approved for processing

  @validation-failure @amount-limit-exceeded
  Scenario: Payment Exceeds Daily Amount Limit
    Given a valid payment request is created
      | Field | Value |
      | From Account | ACC-TEST-001 |
      | To Account | ACC-TEST-002 |
      | Amount | 50000.00 |
      | Currency | ZAR |
      | Reference | VAL-TEST-002 |
    And the daily limit for the account is "25000.00"
    When the payment is submitted for validation
    Then the business rules should fail
    And the validation should return "DAILY_LIMIT_EXCEEDED"
    And the payment should be rejected
    And a validation failure notification should be sent

  @validation-failure @invalid-account
  Scenario: Payment to Invalid Account
    Given a valid payment request is created
      | Field | Value |
      | From Account | ACC-TEST-001 |
      | To Account | ACC-INVALID-001 |
      | Amount | 100.00 |
      | Currency | ZAR |
      | Reference | VAL-TEST-003 |
    When the payment is submitted for validation
    Then the account validation should fail
    And the validation should return "INVALID_DESTINATION_ACCOUNT"
    And the payment should be rejected
    And a validation failure notification should be sent

  @validation-failure @compliance-violation
  Scenario: Payment Violates Compliance Rules
    Given a valid payment request is created
      | Field | Value |
      | From Account | ACC-TEST-001 |
      | To Account | ACC-TEST-002 |
      | Amount | 100.00 |
      | Currency | ZAR |
      | Reference | VAL-TEST-004 |
    And the destination account is on sanctions list
    When the payment is submitted for validation
    Then the compliance rules should fail
    And the validation should return "COMPLIANCE_VIOLATION"
    And the payment should be rejected
    And a compliance violation notification should be sent

  @validation-failure @fraud-detection
  Scenario: Payment Detected as Fraudulent
    Given a valid payment request is created
      | Field | Value |
      | From Account | ACC-TEST-001 |
      | To Account | ACC-TEST-002 |
      | Amount | 100.00 |
      | Currency | ZAR |
      | Reference | VAL-TEST-005 |
    And the fraud detection service returns high risk score
    When the payment is submitted for validation
    Then the fraud detection should fail
    And the validation should return "FRAUD_DETECTED"
    And the payment should be rejected
    And a fraud detection notification should be sent

  @validation-failure @risk-assessment
  Scenario: Payment Fails Risk Assessment
    Given a valid payment request is created
      | Field | Value |
      | From Account | ACC-TEST-001 |
      | To Account | ACC-TEST-002 |
      | Amount | 100.00 |
      | Currency | ZAR |
      | Reference | VAL-TEST-006 |
    And the risk assessment returns high risk
    When the payment is submitted for validation
    Then the risk assessment should fail
    And the validation should return "HIGH_RISK_PAYMENT"
    And the payment should be rejected
    And a risk assessment notification should be sent

  @validation-success @complex-rules
  Scenario: Payment Passes Complex Validation Rules
    Given a valid payment request is created
      | Field | Value |
      | From Account | ACC-TEST-001 |
      | To Account | ACC-TEST-002 |
      | Amount | 1000.00 |
      | Currency | ZAR |
      | Reference | VAL-TEST-007 |
    And the payment is within daily limits
    And the accounts are valid
    And the payment passes compliance checks
    And the fraud detection returns low risk
    And the risk assessment returns low risk
    When the payment is submitted for validation
    Then all validation rules should pass
    And the payment should be approved for processing
    And a validation success notification should be sent

  @performance @validation-throughput
  Scenario: High Volume Payment Validation
    Given 1000 payment requests are created
    And each payment has different validation requirements
    When all payments are submitted for validation
    Then all payments should be validated within "60" seconds
    And the validation success rate should be "95" percent
    And the average validation time should be less than "200" milliseconds
    And no payment should be lost during validation
