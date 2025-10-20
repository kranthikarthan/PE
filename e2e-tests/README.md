# E2E Testing Framework

## Overview

This module provides comprehensive End-to-End (E2E) testing for the Payments Engine. It implements a BDD approach using Cucumber to test complete payment flows across all 22 microservices.

## Features

- **BDD Testing**: Cucumber-based feature files with Gherkin syntax
- **Multi-Service Testing**: Tests complete payment flows across all services
- **Async Testing**: Awaitility for event propagation and async operations
- **External System Mocking**: WireMock for core banking, fraud API, and clearing systems
- **Test Data Management**: Dedicated test tenants and accounts
- **Performance Testing**: High-volume payment processing tests
- **Multi-Tenant Testing**: Tenant isolation and data separation validation

## Test Coverage

### Payment Types Tested
- **EFT** (Electronic Funds Transfer) - Domestic retail payments
- **RTC** (Real-Time Clearing) - Instant payments
- **PayShap** - P2P instant payments
- **SWIFT** - International payments
- **Batch** - Bulk payment processing

### Test Scenarios
- **Happy Path**: Successful payment processing
- **Failure Scenarios**: Insufficient balance, fraud rejection, timeouts, compensation
- **Multi-Tenant**: Tenant isolation and data separation
- **Performance**: High-volume concurrent processing
- **Integration**: Cross-service communication and event propagation

## Test Structure

```
e2e-tests/
├── src/test/java/com/payments/e2e/
│   ├── features/                          # Cucumber feature files
│   │   ├── payment-initiation.feature     # EFT payment processing
│   │   ├── payment-validation.feature     # Business rules validation
│   │   ├── payment-processing.feature     # Transaction processing
│   │   ├── payment-clearing.feature       # Clearing system integration
│   │   └── payment-settlement.feature     # Settlement processing
│   ├── step-definitions/                  # Step definition classes
│   │   ├── PaymentStepDefinitions.java    # Payment flow steps
│   │   ├── ValidationStepDefinitions.java # Validation steps
│   │   └── ClearingStepDefinitions.java   # Clearing steps
│   ├── services/                          # Service interaction classes
│   │   ├── PaymentService.java            # Payment API interactions
│   │   ├── ValidationService.java         # Validation API interactions
│   │   ├── ClearingService.java           # Clearing API interactions
│   │   ├── AccountService.java            # Account API interactions
│   │   └── NotificationService.java       # Notification API interactions
│   ├── models/                            # Test data models
│   │   ├── PaymentRequest.java            # Payment request model
│   │   ├── PaymentResponse.java           # Payment response model
│   │   ├── ValidationResponse.java        # Validation response model
│   │   └── ClearingResponse.java          # Clearing response model
│   ├── data/                              # Test data management
│   │   └── TestDataBuilder.java           # Test data creation utilities
│   ├── config/                            # Test configuration
│   │   └── E2ETestConfiguration.java     # Test infrastructure setup
│   └── E2ETestRunner.java                 # Test execution runner
├── src/test/resources/
│   ├── application-e2e.yml                # E2E test configuration
│   ├── wiremock/                          # WireMock stubs
│   │   ├── core-banking/                  # Core banking mocks
│   │   ├── fraud-api/                     # Fraud API mocks
│   │   └── clearing-systems/              # Clearing system mocks
│   └── test-data/                         # Test data files
│       ├── test-tenants.json              # Test tenant data
│       ├── test-accounts.json             # Test account data
│       └── payment-templates.json         # Payment template data
└── pom.xml                                # Maven configuration
```

## Running Tests

### Prerequisites
- Java 21+
- Maven 3.8+
- Docker (for TestContainers)
- All Payment Engine services running

### Execution Commands

```bash
# Run all E2E tests
mvn test -Dtest=E2ETestRunner

# Run specific feature
mvn test -Dtest=E2ETestRunner -Dcucumber.options="--tags @payment-initiation"

# Run with specific profile
mvn test -Dspring.profiles.active=e2e

# Generate Allure report
mvn allure:report
```

### Test Execution Flow

1. **Setup Phase**
   - Start TestContainers (PostgreSQL, Kafka, Redis)
   - Start WireMock servers for external systems
   - Initialize test data (tenants, accounts)
   - Verify all services are healthy

2. **Test Execution**
   - Execute Cucumber scenarios
   - Use Awaitility for async assertions
   - Mock external system responses
   - Validate event propagation

3. **Cleanup Phase**
   - Stop TestContainers
   - Stop WireMock servers
   - Clean up test data
   - Generate reports

## Test Data Management

### Test Tenants
- **TENANT-TEST-001**: Primary test tenant
- **TENANT-TEST-002**: Secondary test tenant for isolation testing

### Test Accounts
- **ACC-TEST-001**: Source account (balance: 1000.00)
- **ACC-TEST-002**: Destination account (balance: 500.00)
- **ACC-TEST-003**: Multi-tenant source account
- **ACC-TEST-004**: Multi-tenant destination account

### Test Data Isolation
- Each test uses unique payment IDs
- Test data is cleaned up after each test
- No shared state between tests
- Independent test execution

## External System Mocking

### Core Banking System
- Account balance queries
- Account validation
- Balance updates
- Account locking/unlocking

### Fraud Detection API
- Risk score calculation
- Fraud detection rules
- Risk assessment responses

### Clearing Systems
- SAMOS clearing
- RTC clearing
- PayShap clearing
- SWIFT clearing
- Clearing acknowledgments
- Clearing rejections

## Performance Testing

### Load Scenarios
- **Sustained Load**: 1000 TPS for 10 minutes
- **Peak Load**: 2000 TPS for 5 minutes
- **Spike Load**: 5000 TPS for 1 minute
- **Endurance Load**: 1000 TPS for 24 hours

### Performance Targets
- **Throughput**: 1000+ TPS sustained
- **Latency**: p95 < 3 seconds, p99 < 5 seconds
- **Error Rate**: < 1% under normal load
- **Availability**: 99.95% uptime

## Reporting

### Allure Reports
- Test execution results
- Step-by-step execution details
- Screenshots and attachments
- Test trends and analytics

### Test Metrics
- Test execution time
- Success/failure rates
- Performance metrics
- Coverage reports

## Configuration

### Application Properties
```yaml
# Test Configuration
test:
  data:
    tenant-id: TENANT-TEST-001
    test-account-id: ACC-TEST-001
    test-user-id: USER-TEST-001
  
  timeouts:
    payment-processing: 30s
    event-propagation: 10s
    api-response: 5s
  
  urls:
    payment-initiation: http://localhost:8081
    validation: http://localhost:8082
    account-adapter: http://localhost:8083
    routing: http://localhost:8084
    transaction-processing: http://localhost:8085
    saga-orchestrator: http://localhost:8086
```

### Test Tags
- `@payment-initiation`: EFT payment processing tests
- `@payment-validation`: Business rules validation tests
- `@payment-processing`: Transaction processing tests
- `@payment-clearing`: Clearing system integration tests
- `@payment-settlement`: Settlement processing tests
- `@happy-path`: Successful payment scenarios
- `@failure-scenario`: Error and failure scenarios
- `@multi-tenant`: Tenant isolation tests
- `@performance`: High-volume processing tests

## Troubleshooting

### Common Issues

1. **TestContainers Not Starting**
   - Ensure Docker is running
   - Check Docker daemon status
   - Verify TestContainers configuration

2. **WireMock Not Responding**
   - Check WireMock server status
   - Verify stub configurations
   - Check port availability

3. **Service Health Checks Failing**
   - Ensure all Payment Engine services are running
   - Check service URLs in configuration
   - Verify network connectivity

4. **Async Assertions Timing Out**
   - Increase timeout values in configuration
   - Check event propagation
   - Verify Kafka connectivity

### Debug Mode
```bash
# Enable debug logging
mvn test -Dlogging.level.com.payments=DEBUG

# Run single scenario
mvn test -Dtest=E2ETestRunner -Dcucumber.options="--tags @eft-success"
```

## Best Practices

1. **Test Isolation**
   - Use unique payment IDs for each test
   - Clean up test data after each test
   - Avoid shared state between tests

2. **Async Testing**
   - Use Awaitility for async assertions
   - Set appropriate timeout values
   - Poll for status changes

3. **External System Mocking**
   - Mock all external dependencies
   - Use realistic response data
   - Test different response scenarios

4. **Performance Testing**
   - Start with low load and increase gradually
   - Monitor system resources during tests
   - Validate performance targets

5. **Error Handling**
   - Test both success and failure scenarios
   - Validate error messages and codes
   - Test compensation flows

## Contributing

When adding new E2E tests:

1. Create feature files in `src/test/java/com/payments/e2e/features/`
2. Implement step definitions in `src/test/java/com/payments/e2e/stepdefinitions/`
3. Add service interactions in `src/test/java/com/payments/e2e/services/`
4. Update test data builders as needed
5. Add appropriate test tags
6. Update documentation

## Support

For issues with E2E tests:
- Check test logs for detailed error messages
- Verify service health and connectivity
- Review test configuration
- Consult the troubleshooting section
