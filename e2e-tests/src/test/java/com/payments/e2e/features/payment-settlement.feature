# Payment Settlement E2E Tests

@payment-settlement @settlement-processing
Feature: Payment Settlement
  As a payment system
  I want to settle payments between financial institutions
  So that funds are properly transferred and reconciled

  Background:
    Given the payment system is running
    And test tenant "TENANT-TEST-001" is configured
    And all settlement services are available
    And settlement systems are mocked

  @settlement-success @net-settlement
  Scenario: Successful Net Settlement
    Given a completed payment request is created
      | Field | Value |
      | From Account | ACC-TEST-001 |
      | To Account | ACC-TEST-002 |
      | Amount | 100.00 |
      | Currency | ZAR |
      | Reference | SETTLE-TEST-001 |
    And the payment is ready for settlement
    When the payment is submitted for settlement
    Then the settlement service should calculate net positions
    And the net settlement should be calculated
    And the settlement should be submitted to the central bank
    And the payment should be marked as settled
    And a settlement completion event should be published

  @settlement-success @gross-settlement
  Scenario: Successful Gross Settlement
    Given a completed payment request is created
      | Field | Value |
      | From Account | ACC-TEST-001 |
      | To Account | ACC-TEST-002 |
      | Amount | 100.00 |
      | Currency | ZAR |
      | Reference | SETTLE-TEST-002 |
    And the payment is ready for settlement
    And gross settlement is required
    When the payment is submitted for settlement
    Then the settlement service should process gross settlement
    And the settlement should be submitted to the central bank
    And the payment should be marked as settled
    And a settlement completion event should be published

  @settlement-success @multi-currency-settlement
  Scenario: Multi-Currency Settlement
    Given a completed payment request is created
      | Field | Value |
      | From Account | ACC-TEST-001 |
      | To Account | ACC-TEST-002 |
      | Amount | 100.00 |
      | Currency | USD |
      | Reference | SETTLE-TEST-003 |
    And the payment is ready for settlement
    And currency conversion is required
    When the payment is submitted for settlement
    Then the settlement service should handle currency conversion
    And the settlement should be submitted with converted amount
    And the payment should be marked as settled
    And a settlement completion event should be published

  @settlement-failure @settlement-timeout
  Scenario: Settlement System Timeout
    Given a completed payment request is created
      | Field | Value |
      | From Account | ACC-TEST-001 |
      | To Account | ACC-TEST-002 |
      | Amount | 100.00 |
      | Currency | ZAR |
      | Reference | SETTLE-TEST-004 |
    And the payment is ready for settlement
    And the settlement system is configured to timeout
    When the payment is submitted for settlement
    Then the settlement service should timeout
    And the payment should be marked as settlement failed
    And a settlement timeout event should be published
    And the payment should be queued for retry

  @settlement-failure @settlement-rejection
  Scenario: Settlement System Rejection
    Given a completed payment request is created
      | Field | Value |
      | From Account | ACC-TEST-001 |
      | To Account | ACC-TEST-002 |
      | Amount | 100.00 |
      | Currency | ZAR |
      | Reference | SETTLE-TEST-005 |
    And the payment is ready for settlement
    And the settlement system is configured to reject the settlement
    When the payment is submitted for settlement
    Then the settlement service should receive a rejection
    And the payment should be marked as settlement rejected
    And a settlement rejection event should be published
    And the payment should be queued for manual review

  @settlement-success @batch-settlement
  Scenario: Batch Payment Settlement
    Given 10 completed payment requests are created
    And each payment has amount "50.00"
    And all payments are ready for settlement
    When all payments are submitted for batch settlement
    Then all payments should be included in the settlement batch
    And the batch settlement should be submitted to the central bank
    And all payments should be marked as settled
    And a batch settlement completion event should be published

  @settlement-success @reconciliation
  Scenario: Payment Reconciliation
    Given a settled payment request is created
      | Field | Value |
      | From Account | ACC-TEST-001 |
      | To Account | ACC-TEST-002 |
      | Amount | 100.00 |
      | Currency | ZAR |
      | Reference | SETTLE-TEST-006 |
    And the payment is marked as settled
    When the reconciliation process runs
    Then the payment should be reconciled
    And the reconciliation status should be updated
    And a reconciliation completion event should be published

  @settlement-success @settlement-monitoring
  Scenario: Settlement Monitoring and Alerting
    Given a settled payment request is created
      | Field | Value |
      | From Account | ACC-TEST-001 |
      | To Account | ACC-TEST-002 |
      | Amount | 100.00 |
      | Currency | ZAR |
      | Reference | SETTLE-TEST-007 |
    And the payment is marked as settled
    When the settlement monitoring process runs
    Then the settlement should be monitored
    And the settlement metrics should be updated
    And a settlement monitoring event should be published

  @settlement-success @settlement-reporting
  Scenario: Settlement Reporting
    Given multiple settled payment requests are created
    And all payments are marked as settled
    When the settlement reporting process runs
    Then settlement reports should be generated
    And the reports should include all settled payments
    And a settlement reporting event should be published

  @performance @settlement-throughput
  Scenario: High Volume Payment Settlement
    Given 1000 completed payment requests are created
    And each payment has amount between "10.00" and "100.00"
    And all payments are ready for settlement
    When all payments are submitted for settlement
    Then all payments should be settled within "600" seconds
    And the settlement success rate should be "99" percent
    And the average settlement time should be less than "5" seconds
    And no payment should be lost during settlement
