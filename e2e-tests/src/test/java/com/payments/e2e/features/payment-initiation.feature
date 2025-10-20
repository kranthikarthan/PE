# EFT Payment Processing E2E Tests

@payment-initiation @eft-payment
Feature: EFT Payment Processing
  As a payment system
  I want to process EFT payments end-to-end
  So that customers can transfer funds between accounts

  Background:
    Given the payment system is running
    And test tenant "TENANT-TEST-001" is configured
    And test account "ACC-TEST-001" has balance "1000.00"
    And test account "ACC-TEST-002" has balance "500.00"
    And external systems are mocked

  @happy-path @eft-success
  Scenario: Successful EFT Payment Processing
    Given a valid EFT payment request is created
      | Field | Value |
      | From Account | ACC-TEST-001 |
      | To Account | ACC-TEST-002 |
      | Amount | 100.00 |
      | Currency | ZAR |
      | Reference | EFT-TEST-001 |
    When the payment is submitted
    Then the payment should be initiated successfully
    And the payment should be validated successfully
    And the payment should be processed successfully
    And the payment should be routed to SAMOS clearing
    And the payment should be submitted to clearing system
    And the payment should be settled successfully
    And the source account balance should be "900.00"
    And the destination account balance should be "600.00"
    And a payment notification should be sent
    And an audit trail should be created

  @failure-scenario @insufficient-balance
  Scenario: EFT Payment with Insufficient Balance
    Given a valid EFT payment request is created
      | Field | Value |
      | From Account | ACC-TEST-001 |
      | To Account | ACC-TEST-002 |
      | Amount | 1500.00 |
      | Currency | ZAR |
      | Reference | EFT-TEST-002 |
    When the payment is submitted
    Then the payment should be initiated successfully
    And the payment should be validated successfully
    And the account adapter should return insufficient balance error
    And the payment should be rejected
    And a rejection notification should be sent
    And the account balances should remain unchanged
    And an audit trail should be created

  @failure-scenario @fraud-detection
  Scenario: EFT Payment Rejected by Fraud Detection
    Given a valid EFT payment request is created
      | Field | Value |
      | From Account | ACC-TEST-001 |
      | To Account | ACC-TEST-002 |
      | Amount | 100.00 |
      | Currency | ZAR |
      | Reference | EFT-TEST-003 |
    And the fraud detection service returns high risk score
    When the payment is submitted
    Then the payment should be initiated successfully
    And the payment should be validated successfully
    And the fraud detection should reject the payment
    And the payment should be rejected
    And a fraud rejection notification should be sent
    And the account balances should remain unchanged
    And an audit trail should be created

  @failure-scenario @clearing-timeout
  Scenario: EFT Payment with Clearing System Timeout
    Given a valid EFT payment request is created
      | Field | Value |
      | From Account | ACC-TEST-001 |
      | To Account | ACC-TEST-002 |
      | Amount | 100.00 |
      | Currency | ZAR |
      | Reference | EFT-TEST-004 |
    And the clearing system is configured to timeout
    When the payment is submitted
    Then the payment should be initiated successfully
    And the payment should be validated successfully
    And the payment should be processed successfully
    And the payment should be routed to SAMOS clearing
    And the clearing system should timeout
    And the payment should be compensated
    And the account balances should be restored
    And a timeout notification should be sent
    And an audit trail should be created

  @multi-tenant @tenant-isolation
  Scenario: EFT Payment with Tenant Isolation
    Given test tenant "TENANT-TEST-002" is configured
    And test account "ACC-TEST-003" belongs to "TENANT-TEST-002"
    And test account "ACC-TEST-004" belongs to "TENANT-TEST-002"
    And a valid EFT payment request is created for tenant "TENANT-TEST-002"
      | Field | Value |
      | From Account | ACC-TEST-003 |
      | To Account | ACC-TEST-004 |
      | Amount | 50.00 |
      | Currency | ZAR |
      | Reference | EFT-TEST-005 |
    When the payment is submitted
    Then the payment should be processed successfully
    And the payment should only be visible to "TENANT-TEST-002"
    And tenant "TENANT-TEST-001" should not see this payment
    And tenant data should be properly isolated

  @performance @high-volume
  Scenario: High Volume EFT Payment Processing
    Given 100 EFT payment requests are created
    And each payment has amount between "10.00" and "100.00"
    When all payments are submitted concurrently
    Then all payments should be processed within "30" seconds
    And the success rate should be "100" percent
    And the average processing time should be less than "3" seconds
    And no payment should be lost or duplicated
