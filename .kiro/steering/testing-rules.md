---
inclusion: always
---
# CURSOR AI RULES FOR PHASE 6: TESTING & QUALITY ASSURANCE

## FUNDAMENTAL PRINCIPLES
- Follow comprehensive testing strategies for enterprise-grade financial systems
- Prioritize quality, security, performance, and compliance
- Implement shift-left testing with automated quality gates
- Focus on production readiness and operational excellence
- Ensure regulatory compliance and audit readiness

## TESTING ARCHITECTURE STANDARDS

### Test Pyramid Implementation
- **Unit Tests (80%)**: Fast, isolated, comprehensive business logic coverage
- **Integration Tests (15%)**: Service boundaries, database, external APIs
- **E2E Tests (5%)**: Complete user journeys, critical business flows
- **Total Coverage**: >80% code coverage, >95% critical path coverage
- **Execution Time**: <30 minutes for full test suite

### Testing Framework Standards
- **JUnit 5** for unit and integration tests
- **Cucumber** for BDD and E2E scenarios
- **RestAssured** for API testing
- **Awaitility** for async testing and event propagation
- **WireMock** for external system mocking
- **TestContainers** for infrastructure testing
- **Gatling** for load and performance testing

## E2E TESTING EXCELLENCE

### Test Coverage Requirements
- **Payment Types**: All 5 types (EFT, RTC, PayShap, SWIFT, Batch)
- **Scenarios**: 50+ E2E scenarios covering happy path and failures
- **Multi-tenancy**: Tenant isolation and data separation validation
- **Async Flows**: Event propagation with proper timeouts (max 30s)
- **Failure Scenarios**: Insufficient balance, fraud rejection, timeouts, compensation

### Test Data Management
- **Dedicated Test Tenants**: TENANT-TEST-001, TENANT-TEST-002
- **Test Accounts**: Known balances, reset before each test
- **Data Cleanup**: Automatic cleanup after test execution
- **Isolation**: Independent tests with no shared state
- **Unique Identifiers**: Avoid conflicts with unique payment IDs

### BDD Implementation
```gherkin
Feature: EFT Payment Processing
  As a customer
  I want to initiate an EFT payment
  So that I can transfer money to another account

Background:
  Given the payment system is up and running
  And tenant "TENANT-001" is onboarded
  And user "john.doe@example.com" is authenticated
  And account "ACC-12345" has balance R10,000

Scenario: Successful EFT payment
  Given I am on the payment initiation page
  When I enter the following payment details:
    | Field                | Value          |
    | Payment Type         | EFT            |
    | Debtor Account       | ACC-12345      |
    | Creditor Account     | ACC-67890      |
    | Amount               | R500.00        |
    | Reference            | Invoice 001    |
  And I submit the payment
  Then I should see payment status "PROCESSING"
  And the payment should be validated
  And the account balance should be reserved
  And the limit should be checked
  And fraud scoring should return "LOW_RISK"
  And the payment should be routed to BankservAfrica
  And the clearing adapter should submit the payment
  And the payment status should be "SUBMITTED"
  And I should receive a notification "Payment submitted successfully"
  And the audit log should contain the payment record
```

## LOAD TESTING EXCELLENCE

### Performance SLOs
- **Throughput**: 1,000+ TPS sustained, 2,000+ TPS peak
- **Latency**: p50 < 1s, p95 < 3s, p99 < 5s
- **Error Rate**: <1% under normal load
- **Availability**: 99.95% uptime
- **Resource Usage**: CPU <70%, Memory <80%, DB connections <80%

### Load Test Scenarios
1. **Sustained Load**: 500 TPS for 10 minutes
2. **Peak Load**: 1,000 TPS for 5 minutes
3. **Spike Test**: 0 → 2,000 TPS instant spike
4. **Endurance Test**: 500 TPS for 8 hours
5. **Stress Test**: Ramp up until failure (target >1,500 TPS)

### Gatling Implementation
```scala
class PaymentLoadTest extends Simulation {
  val httpProtocol = http
    .baseUrl("https://api-staging.payments.example.com")
    .header("Authorization", "Bearer ${authToken}")
    .header("X-Tenant-ID", "TENANT-001")
    .acceptHeader("application/json")
  
  val eftPayment = scenario("EFT Payment")
    .feed(csv("payment-data.csv").circular)
    .exec(
      http("Initiate EFT Payment")
        .post("/api/v1/payments")
        .body(ElFileBody("eft-payment-template.json")).asJson
        .check(status.is(201))
        .check(jsonPath("$.paymentId").saveAs("paymentId"))
    )
    .pause(1)
    .exec(
      http("Check Payment Status")
        .get("/api/v1/payments/${paymentId}")
        .check(status.is(200))
        .check(jsonPath("$.status").in("PROCESSING", "VALIDATED", "SUBMITTED"))
    )
  
  setUp(
    eftPayment.inject(
      nothingFor(5.seconds),
      rampUsersPerSec(10).to(500).during(2.minutes),
      constantUsersPerSec(500).during(10.minutes),
      rampUsersPerSec(500).to(1000).during(1.minute),
      constantUsersPerSec(1000).during(5.minutes)
    )
  ).protocols(httpProtocol)
    .assertions(
      global.responseTime.percentile3.lt(3000),
      global.responseTime.percentile4.lt(5000),
      global.successfulRequests.percent.gt(99)
    )
}
```

## SECURITY TESTING EXCELLENCE

### SAST (Static Application Security Testing)
- **SonarQube**: Security Rating A, 0 critical/high vulnerabilities
- **Code Coverage**: >80% with security focus
- **Quality Gate**: Must pass before deployment
- **Security Rules**: Custom rules for financial applications
- **Dependency Scanning**: OWASP Dependency-Check integration

### DAST (Dynamic Application Security Testing)
- **OWASP ZAP**: Full active scan with 0 critical/high vulnerabilities
- **OWASP Top 10**: Complete testing of all 10 categories
- **Authentication Testing**: JWT validation, token expiration, scope checking
- **Authorization Testing**: RBAC, tenant isolation, privilege escalation
- **Injection Testing**: SQL injection, XSS, XXE, command injection

### Container Security
- **Trivy Scanning**: 0 critical vulnerabilities in container images
- **Base Images**: Minimal Alpine or Distroless images
- **Vulnerability Types**: OS vulnerabilities, library vulnerabilities, misconfigurations
- **Scan Frequency**: Every build, every deployment
- **Remediation**: Immediate fix for critical vulnerabilities

### Secrets Management
- **Gitleaks Scanning**: 0 secrets exposed in codebase
- **Scan Scope**: Code, config files, environment variables, Docker files
- **Secret Types**: Passwords, API keys, tokens, certificates
- **Rotation**: Immediate rotation if secrets found
- **History**: Clean Git history of exposed secrets

## COMPLIANCE TESTING EXCELLENCE

### Regulatory Compliance
- **SARB Compliance**: South African Reserve Bank requirements
- **POPIA Compliance**: Protection of Personal Information Act
- **PCI-DSS Compliance**: Payment Card Industry standards
- **Audit Trails**: Complete, tamper-proof, searchable audit logs
- **Data Protection**: Privacy controls, data anonymization, retention policies

### Multi-tenant Data Isolation
- **Tenant Isolation**: Complete data separation between tenants
- **Data Leakage Prevention**: No cross-tenant data access
- **RLS Validation**: Row Level Security implementation
- **Access Control**: Tenant-specific access controls
- **Audit Logging**: Tenant-specific audit trails

### Privacy Controls
- **Data Anonymization**: PII data anonymization capabilities
- **Data Retention**: Automated data retention policies
- **Consent Management**: User consent tracking and management
- **Data Subject Rights**: GDPR-style data subject rights
- **Privacy Impact Assessment**: Regular privacy impact assessments

## PRODUCTION READINESS EXCELLENCE

### Deployment Validation
- **Blue-Green Deployment**: Zero-downtime deployment testing
- **Canary Deployment**: Gradual rollout with monitoring
- **Rollback Testing**: Quick rollback capability validation
- **GitOps Integration**: ArgoCD deployment automation
- **Infrastructure as Code**: Kubernetes deployment validation

### Monitoring and Alerting
- **Prometheus Integration**: Comprehensive metrics collection
- **Grafana Dashboards**: Real-time monitoring dashboards
- **Jaeger Tracing**: Distributed tracing for performance analysis
- **Alerting Rules**: Critical alerts with escalation procedures
- **Health Checks**: Application and infrastructure health monitoring

### Disaster Recovery
- **Backup Testing**: Regular backup and restore testing
- **Failover Testing**: High availability failover scenarios
- **Data Recovery**: Point-in-time recovery capabilities
- **Business Continuity**: Disaster recovery procedures
- **Recovery Time Objectives**: RTO and RPO validation

## TESTING AUTOMATION STANDARDS

### CI/CD Integration
- **Quality Gates**: Automated quality gate enforcement
- **Test Execution**: Automated test execution on every commit
- **Reporting**: Automated test reports and notifications
- **Artifact Management**: Test artifacts and reports storage
- **Pipeline Integration**: Seamless CI/CD pipeline integration

### Test Data Management
- **Test Data Builders**: Fluent test data creation
- **Data Factories**: Automated test data generation
- **Data Cleanup**: Automatic test data cleanup
- **Data Isolation**: Test data isolation between tests
- **Data Privacy**: Test data privacy and anonymization

### Test Environment Management
- **Environment Provisioning**: Automated test environment setup
- **Environment Isolation**: Isolated test environments
- **Environment Cleanup**: Automatic environment cleanup
- **Environment Monitoring**: Test environment health monitoring
- **Environment Scaling**: Dynamic test environment scaling

## QUALITY ASSURANCE STANDARDS

### Code Quality
- **SonarQube Integration**: Continuous code quality monitoring
- **Code Coverage**: >80% coverage with quality focus
- **Technical Debt**: <5% technical debt ratio
- **Code Smells**: Zero critical code smells
- **Duplication**: <3% code duplication

### Test Quality
- **Test Reliability**: 0 flaky tests, deterministic execution
- **Test Maintainability**: Well-structured, maintainable tests
- **Test Documentation**: Clear test documentation and comments
- **Test Performance**: Fast test execution (<30 minutes total)
- **Test Coverage**: Comprehensive coverage of critical paths

### Documentation Standards
- **Test Documentation**: Comprehensive test documentation
- **API Documentation**: Complete API testing documentation
- **Performance Reports**: Detailed performance analysis reports
- **Security Reports**: Comprehensive security assessment reports
- **Compliance Reports**: Regulatory compliance validation reports

## ERROR HANDLING AND RESILIENCE

### Test Error Handling
- **Graceful Failures**: Tests fail gracefully with clear error messages
- **Error Reporting**: Comprehensive error reporting and logging
- **Retry Logic**: Intelligent retry logic for transient failures
- **Timeout Handling**: Proper timeout handling for async operations
- **Resource Cleanup**: Automatic resource cleanup on test failures

### Test Resilience
- **Network Resilience**: Tests handle network issues gracefully
- **Service Resilience**: Tests handle service failures
- **Data Resilience**: Tests handle data inconsistencies
- **Infrastructure Resilience**: Tests handle infrastructure issues
- **Recovery Testing**: System recovery testing and validation

## PERFORMANCE OPTIMIZATION

### Test Performance
- **Parallel Execution**: Parallel test execution for faster feedback
- **Test Optimization**: Optimized test execution time
- **Resource Usage**: Efficient resource usage during testing
- **Memory Management**: Proper memory management in tests
- **CPU Optimization**: Efficient CPU usage during test execution

### System Performance
- **Performance Baseline**: Establish performance baselines
- **Performance Monitoring**: Continuous performance monitoring
- **Bottleneck Analysis**: Identify and resolve performance bottlenecks
- **Capacity Planning**: Capacity planning and scaling recommendations
- **Performance Tuning**: System performance tuning and optimization

## INTEGRATION WITH EXISTING SYSTEMS

### Spring Boot Integration
- **Spring Boot Test**: Comprehensive Spring Boot testing
- **Test Slices**: Focused testing with @WebMvcTest, @DataJpaTest
- **Test Profiles**: Environment-specific test configurations
- **Test Properties**: Test-specific property configurations
- **Test Annotations**: Spring Boot test annotations and utilities

### Microservices Integration
- **Service Communication**: Inter-service communication testing
- **Event Testing**: Event-driven architecture testing
- **Saga Testing**: Distributed transaction testing
- **Circuit Breaker Testing**: Resilience pattern testing
- **Service Mesh Testing**: Istio service mesh testing

### Infrastructure Integration
- **Kubernetes Testing**: Kubernetes deployment and scaling testing
- **Database Testing**: Database integration and performance testing
- **Message Queue Testing**: Kafka message processing testing
- **Cache Testing**: Redis caching and performance testing
- **Monitoring Integration**: Prometheus, Grafana, Jaeger integration

## SUCCESS METRICS AND KPIs

### Quality Metrics
- **Test Coverage**: >80% code coverage, >95% critical path coverage
- **Test Reliability**: 0 flaky tests, 100% deterministic execution
- **Test Performance**: <30 minutes total execution time
- **Test Automation**: 90%+ automated test coverage
- **Quality Gates**: 100% quality gate pass rate

### Performance Metrics
- **Throughput**: 1,000+ TPS sustained, 2,000+ TPS peak
- **Latency**: p95 < 3s, p99 < 5s
- **Error Rate**: <1% under normal load
- **Availability**: 99.95% uptime
- **Resource Efficiency**: CPU <70%, Memory <80%

### Security Metrics
- **Vulnerability Count**: 0 critical/high vulnerabilities
- **Security Rating**: SonarQube Security Rating A
- **Compliance Score**: 100% regulatory compliance
- **Secrets Exposure**: 0 secrets exposed
- **Security Coverage**: 100% OWASP Top 10 coverage

### Compliance Metrics
- **Regulatory Compliance**: 100% SARB, POPIA, PCI-DSS compliance
- **Audit Readiness**: 100% audit trail completeness
- **Data Protection**: 100% privacy controls implementation
- **Multi-tenancy**: 100% tenant isolation validation
- **Documentation**: 100% compliance documentation completeness

Remember: Focus on creating a comprehensive, automated, and reliable testing framework that ensures the Payments Engine is production-ready, secure, compliant, and performant. The testing framework should provide confidence in system quality and enable rapid, safe deployments.
