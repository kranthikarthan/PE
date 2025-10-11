# Testing Architecture - Design Document

## Overview

This document provides the **comprehensive testing architecture** for the Payments Engine. A robust testing strategy is critical for ensuring **quality, reliability, and correctness** of a financial system handling **billions of rands** in transactions. This design implements a **multi-layered testing approach** covering unit, integration, end-to-end, performance, security, and chaos testing.

---

## Testing Principles

### 1. Test Pyramid Strategy

```
Traditional Test Pyramid:

                 /\
                /  \  E2E Tests (5%)
               /    \  - Slow, expensive
              /------\  - Full system tests
             /        \ Integration Tests (15%)
            /          \ - Moderate speed
           /            \ - Service boundaries
          /--------------\
         /                \ Unit Tests (80%)
        /                  \ - Fast, cheap
       /____________________\ - Business logic

Our Test Distribution:
- Unit Tests: 80% (10,000+ tests)
- Integration Tests: 15% (1,500+ tests)
- End-to-End Tests: 5% (500+ tests)

Rationale:
✅ Fast feedback (unit tests run in seconds)
✅ Comprehensive coverage (all code paths)
✅ Catch bugs early (unit tests)
✅ Confidence in integration (integration tests)
✅ Validate user journeys (E2E tests)
```

### 2. Shift-Left Testing

```
Traditional Testing:
Development → Build → Deploy → Test (QA team)
                                  ↑
                            Tests run late
                            Bugs expensive to fix ❌

Shift-Left Testing:
Development (with tests) → Build (with tests) → Deploy → Monitor
    ↑                          ↑
Tests run during development   Tests in CI/CD
Bugs caught early              Quick feedback
Cheap to fix ✅                Automated ✅

Benefits:
✅ Faster feedback (minutes vs hours)
✅ Cheaper to fix (development vs production)
✅ Higher confidence (tests run automatically)
✅ Better quality (developers own tests)
```

### 3. Test Automation

```
Automation Levels:

Level 1: Manual Testing (0% automated)
- Human testers execute test cases
- Slow, expensive, error-prone ❌

Level 2: Automated Testing (50% automated)
- Some tests automated, some manual
- Faster, cheaper, more reliable

Level 3: Continuous Testing (90%+ automated) ✅ OUR GOAL
- All tests automated
- Run on every commit
- Fast feedback (< 10 minutes)
- High confidence

Our Automation Targets:
- Unit Tests: 100% automated ✅
- Integration Tests: 100% automated ✅
- E2E Tests: 90% automated ✅
- Performance Tests: 80% automated ✅
- Security Tests: 70% automated ✅
```

### 4. Quality Gates

```
Quality Gates (Must Pass to Deploy):

Gate 1: Code Quality
├─ Code coverage > 80% (JaCoCo)
├─ SonarQube quality gate PASS
├─ No critical code smells
└─ Technical debt < 5%

Gate 2: Test Success
├─ Unit tests: 100% pass
├─ Integration tests: 100% pass
├─ E2E tests: > 95% pass
└─ Performance tests: < 10% regression

Gate 3: Security
├─ No critical vulnerabilities (Snyk)
├─ No secrets in code (GitGuardian)
├─ Container scan PASS (Trivy)
└─ SAST findings < 10 high

Gate 4: Performance
├─ API response time < 200ms (p95)
├─ Throughput > 90% baseline
├─ Memory usage < 85% limit
└─ CPU usage < 80% limit

If ANY gate fails → Deployment BLOCKED ❌
```

---

## Testing Architecture Layers

### Layer 1: Unit Testing

```
What: Test individual units (methods, classes) in isolation

Scope: Single class/method
Speed: Milliseconds
Coverage: Business logic, validations, calculations

Example: Payment Service Unit Tests

@SpringBootTest
class PaymentServiceTest {
    
    @Mock
    private PaymentRepository paymentRepository;
    
    @Mock
    private ValidationService validationService;
    
    @Mock
    private KafkaTemplate kafkaTemplate;
    
    @InjectMocks
    private PaymentService paymentService;
    
    @Test
    void shouldInitiatePayment_WhenValidRequest() {
        // Given
        PaymentRequest request = PaymentRequest.builder()
            .fromAccount("1234567890")
            .toAccount("0987654321")
            .amount(new Money(10000, Currency.ZAR))
            .build();
        
        ValidationResult validationResult = ValidationResult.success();
        when(validationService.validate(any())).thenReturn(validationResult);
        
        Payment savedPayment = Payment.builder()
            .id(PaymentId.generate())
            .status(PaymentStatus.PENDING)
            .build();
        when(paymentRepository.save(any())).thenReturn(savedPayment);
        
        // When
        Payment result = paymentService.initiatePayment(request);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(PaymentStatus.PENDING);
        verify(validationService).validate(any());
        verify(paymentRepository).save(any());
        verify(kafkaTemplate).send(eq("payment.initiated"), any());
    }
    
    @Test
    void shouldRejectPayment_WhenValidationFails() {
        // Given
        PaymentRequest request = PaymentRequest.builder()
            .fromAccount("INVALID")
            .amount(new Money(-100, Currency.ZAR))  // Negative amount
            .build();
        
        ValidationResult validationResult = ValidationResult.failure("Invalid amount");
        when(validationService.validate(any())).thenReturn(validationResult);
        
        // When & Then
        assertThrows(ValidationException.class, 
            () -> paymentService.initiatePayment(request));
        
        verify(validationService).validate(any());
        verify(paymentRepository, never()).save(any());
        verify(kafkaTemplate, never()).send(anyString(), any());
    }
    
    @Test
    void shouldEnforceTransactionLimit() {
        // Given
        PaymentRequest request = PaymentRequest.builder()
            .fromAccount("1234567890")
            .amount(new Money(1000000, Currency.ZAR))  // 1M ZAR
            .build();
        
        Customer customer = Customer.builder()
            .dailyLimit(new Money(500000, Currency.ZAR))
            .build();
        
        // When & Then
        assertThrows(LimitExceededException.class, 
            () -> paymentService.validateLimit(customer, request));
    }
}

Test Coverage Targets:
- Business logic: 100%
- Validation logic: 100%
- Domain models: 90%
- Controllers: 80%
- Overall: > 80%

Tools:
- Framework: JUnit 5, Mockito
- Coverage: JaCoCo
- Assertions: AssertJ
- Test data: Faker, Instancio
```

### Layer 2: Integration Testing

```
What: Test integration between services and external systems

Scope: Multiple components, databases, message queues
Speed: Seconds to minutes
Coverage: API contracts, database interactions, event publishing

Example: Payment Integration Tests

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@Testcontainers
class PaymentIntegrationTest {
    
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:14")
        .withDatabaseName("payment_test")
        .withUsername("test")
        .withPassword("test");
    
    @Container
    static KafkaContainer kafka = new KafkaContainer(
        DockerImageName.parse("confluentinc/cp-kafka:7.4.0"));
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    @Autowired
    private PaymentRepository paymentRepository;
    
    @Autowired
    private KafkaConsumer<String, PaymentEvent> kafkaConsumer;
    
    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
    }
    
    @Test
    void shouldInitiatePaymentEndToEnd() {
        // Given
        PaymentRequest request = PaymentRequest.builder()
            .fromAccount("1234567890")
            .toAccount("0987654321")
            .amount(10000.00)
            .currency("ZAR")
            .build();
        
        // When
        ResponseEntity<PaymentResponse> response = restTemplate.postForEntity(
            "/api/v1/payments",
            request,
            PaymentResponse.class
        );
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo("PENDING");
        
        // Verify database persistence
        Optional<Payment> savedPayment = paymentRepository.findById(
            response.getBody().getPaymentId());
        assertThat(savedPayment).isPresent();
        
        // Verify Kafka event published
        ConsumerRecords<String, PaymentEvent> records = 
            kafkaConsumer.poll(Duration.ofSeconds(10));
        assertThat(records).hasSize(1);
        
        PaymentEvent event = records.iterator().next().value();
        assertThat(event.getEventType()).isEqualTo("payment.initiated");
        assertThat(event.getPaymentId()).isEqualTo(response.getBody().getPaymentId());
    }
    
    @Test
    void shouldHandleDatabaseFailure() {
        // Given
        postgres.stop();  // Simulate database failure
        
        PaymentRequest request = PaymentRequest.builder()
            .fromAccount("1234567890")
            .amount(10000.00)
            .build();
        
        // When
        ResponseEntity<ErrorResponse> response = restTemplate.postForEntity(
            "/api/v1/payments",
            request,
            ErrorResponse.class
        );
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        assertThat(response.getBody().getErrorCode()).isEqualTo("DATABASE_UNAVAILABLE");
    }
}

Test Coverage:
- API contracts: 100%
- Database operations: 100%
- Event publishing: 100%
- External API calls: 80%
- Error handling: 90%

Tools:
- Framework: Spring Boot Test
- Containers: Testcontainers (PostgreSQL, Kafka, Redis)
- API testing: RestAssured, MockMvc
- Database: H2 (lightweight) or Testcontainers (production-like)
```

### Layer 3: Contract Testing

```
What: Ensure API contracts are maintained between services

Scope: Service boundaries, API compatibility
Speed: Seconds
Coverage: Request/response schemas, breaking changes

Example: Payment-Validation Contract Test (Pact)

// Consumer side (Payment Service)
@ExtendWith(PactConsumerTestExt.class)
@PactTestFor(providerName = "validation-service")
class PaymentValidationContractTest {
    
    @Pact(consumer = "payment-service")
    public RequestResponsePact createPact(PactDslWithProvider builder) {
        return builder
            .given("validation service is available")
            .uponReceiving("a valid payment validation request")
                .path("/api/v1/validate")
                .method("POST")
                .headers("Content-Type", "application/json")
                .body(new PactDslJsonBody()
                    .stringType("fromAccount", "1234567890")
                    .stringType("toAccount", "0987654321")
                    .numberType("amount", 10000.00)
                    .stringType("currency", "ZAR"))
            .willRespondWith()
                .status(200)
                .headers("Content-Type", "application/json")
                .body(new PactDslJsonBody()
                    .stringType("validationId")
                    .booleanType("valid", true)
                    .array("checks")
                        .object()
                            .stringType("checkType", "ACCOUNT_EXISTS")
                            .booleanType("passed", true)
                        .closeObject()
                    .closeArray())
            .toPact();
    }
    
    @Test
    @PactTestFor(pactMethod = "createPact")
    void testValidationServiceContract(MockServer mockServer) {
        // Given
        ValidationClient client = new ValidationClient(mockServer.getUrl());
        
        PaymentValidationRequest request = PaymentValidationRequest.builder()
            .fromAccount("1234567890")
            .toAccount("0987654321")
            .amount(10000.00)
            .currency("ZAR")
            .build();
        
        // When
        ValidationResult result = client.validate(request);
        
        // Then
        assertThat(result.isValid()).isTrue();
        assertThat(result.getChecks()).hasSize(1);
    }
}

// Provider side (Validation Service)
@Provider("validation-service")
@PactBroker(url = "https://pact-broker.paymentsengine.com")
class ValidationServiceContractTest {
    
    @TestTemplate
    @ExtendWith(PactVerificationInvocationContextProvider.class)
    void pactVerificationTestTemplate(PactVerificationContext context) {
        context.verifyInteraction();
    }
    
    @BeforeEach
    void setUp(PactVerificationContext context) {
        context.setTarget(new HttpTestTarget("localhost", 8080));
    }
    
    @State("validation service is available")
    void validationServiceAvailable() {
        // Set up test data
        // Ensure service is ready
    }
}

Benefits:
✅ Prevent breaking changes (detect at build time)
✅ Consumer-driven contracts (consumer defines needs)
✅ Independent deployment (verify compatibility)
✅ Clear API documentation (contracts as spec)

Tools:
- Framework: Pact, Spring Cloud Contract
- Broker: Pact Broker (hosted or self-hosted)
- Verification: Automated in CI/CD
```

### Layer 4: End-to-End Testing

```
What: Test complete user journeys across all services

Scope: Full system, all microservices
Speed: Minutes
Coverage: Critical user flows, business scenarios

Example: Payment Journey E2E Test

@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PaymentJourneyE2ETest {
    
    @Autowired
    private WebTestClient webClient;
    
    private String authToken;
    private String customerId;
    
    @BeforeAll
    void setUp() {
        // Authenticate user
        authToken = authenticateUser("john.doe@example.com", "password123");
        customerId = "CUST-12345";
    }
    
    @Test
    @Order(1)
    void completePaymentJourney() {
        // Step 1: Get customer accounts
        List<Account> accounts = getAccounts(customerId);
        assertThat(accounts).hasSizeGreaterThan(0);
        
        Account fromAccount = accounts.get(0);
        Money initialBalance = fromAccount.getBalance();
        
        // Step 2: Initiate payment
        PaymentRequest request = PaymentRequest.builder()
            .fromAccount(fromAccount.getAccountNumber())
            .toAccount("0987654321")
            .amount(new Money(10000, Currency.ZAR))
            .reference("Test payment")
            .build();
        
        PaymentResponse paymentResponse = webClient
            .post()
            .uri("/api/v1/payments")
            .header("Authorization", "Bearer " + authToken)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .exchange()
            .expectStatus().isCreated()
            .expectBody(PaymentResponse.class)
            .returnResult()
            .getResponseBody();
        
        assertThat(paymentResponse.getPaymentId()).isNotNull();
        assertThat(paymentResponse.getStatus()).isEqualTo("PENDING");
        
        String paymentId = paymentResponse.getPaymentId();
        
        // Step 3: Wait for payment to be validated
        await().atMost(Duration.ofSeconds(30))
            .pollInterval(Duration.ofSeconds(2))
            .until(() -> getPaymentStatus(paymentId).equals("VALIDATED"));
        
        // Step 4: Wait for payment to be processed
        await().atMost(Duration.ofSeconds(60))
            .pollInterval(Duration.ofSeconds(5))
            .until(() -> getPaymentStatus(paymentId).equals("COMPLETED"));
        
        // Step 5: Verify account balance updated
        Account updatedAccount = getAccount(fromAccount.getAccountNumber());
        Money expectedBalance = initialBalance.subtract(new Money(10000, Currency.ZAR));
        assertThat(updatedAccount.getBalance()).isEqualTo(expectedBalance);
        
        // Step 6: Verify transaction history
        List<Transaction> transactions = getTransactions(fromAccount.getAccountNumber());
        assertThat(transactions)
            .filteredOn(t -> t.getPaymentId().equals(paymentId))
            .hasSize(1);
        
        // Step 7: Verify notification sent
        List<Notification> notifications = getNotifications(customerId);
        assertThat(notifications)
            .filteredOn(n -> n.getPaymentId().equals(paymentId))
            .hasSize(1);
        
        // Step 8: Verify audit log created
        List<AuditLog> auditLogs = getAuditLogs(paymentId);
        assertThat(auditLogs).hasSizeGreaterThan(0);
    }
    
    @Test
    @Order(2)
    void shouldRejectPaymentWhenInsufficientBalance() {
        // Given: Account with 1000 ZAR balance
        // When: Attempt payment of 5000 ZAR
        // Then: Payment rejected with INSUFFICIENT_FUNDS error
    }
    
    @Test
    @Order(3)
    void shouldEnforceDailyLimit() {
        // Given: Customer with 100K daily limit
        // When: Attempt 3rd payment today (total > 100K)
        // Then: Payment rejected with DAILY_LIMIT_EXCEEDED error
    }
}

E2E Test Scenarios (500+ tests):
- Happy path: Successful payment end-to-end
- Error scenarios: Insufficient funds, invalid account, limit exceeded
- Multi-service flows: Payment → Validation → Account → Clearing
- Compensation flows: Payment failure triggers reversal
- Notification flows: Payment success triggers notifications
- Audit flows: All actions logged correctly

Tools:
- Framework: Spring Boot Test, RestAssured
- Async waiting: Awaitility
- Test orchestration: JUnit 5
- API client: WebTestClient, RestTemplate
- Data setup: Testcontainers, Flyway migrations
```

### Layer 5: Performance Testing

```
What: Test system performance, scalability, and reliability

Scope: Load, stress, spike, endurance testing
Speed: Minutes to hours
Coverage: Throughput, latency, resource usage

Load Testing Script (Gatling):

class PaymentLoadTest extends Simulation {
    
    // Scenario 1: Normal Load (Baseline)
    val normalLoad = scenario("Normal Load")
        .exec(
            http("Initiate Payment")
                .post("/api/v1/payments")
                .header("Authorization", "Bearer ${authToken}")
                .body(StringBody("""
                    {
                        "fromAccount": "1234567890",
                        "toAccount": "0987654321",
                        "amount": 10000.00,
                        "currency": "ZAR"
                    }
                """))
                .check(status.is(201))
                .check(jsonPath("$.paymentId").saveAs("paymentId"))
        )
        .exec(
            http("Check Payment Status")
                .get("/api/v1/payments/${paymentId}")
                .header("Authorization", "Bearer ${authToken}")
                .check(status.is(200))
                .check(jsonPath("$.status").in("PENDING", "VALIDATED", "COMPLETED"))
        )
    
    // Scenario 2: Peak Load (3x normal)
    val peakLoad = scenario("Peak Load")
        .exec(normalLoad)
    
    // Scenario 3: Spike Load (10x normal, sudden)
    val spikeLoad = scenario("Spike Load")
        .exec(normalLoad)
    
    // Execute scenarios
    setUp(
        // Normal load: 1000 users, 10-second ramp-up, 10-minute duration
        normalLoad.inject(
            rampUsersPerSec(0) to (1000) during (10 seconds),
            constantUsersPerSec(1000) during (10 minutes)
        ),
        
        // Peak load: 3000 users, 30-second ramp-up, 5-minute duration
        peakLoad.inject(
            nothingFor(15 minutes),
            rampUsersPerSec(1000) to (3000) during (30 seconds),
            constantUsersPerSec(3000) during (5 minutes)
        ),
        
        // Spike load: 10000 users, immediate, 2-minute duration
        spikeLoad.inject(
            nothingFor(25 minutes),
            atOnceUsers(10000),
            constantUsersPerSec(10000) during (2 minutes)
        )
    ).protocols(
        http.baseUrl("https://api.paymentsengine.com")
            .acceptHeader("application/json")
            .contentTypeHeader("application/json")
    ).assertions(
        // Performance assertions
        global().responseTime().p95.lt(200),  // p95 < 200ms
        global().successfulRequests().percent.gt(99),  // > 99% success
        global().requestsPerSec.gt(10000)  // > 10K RPS
    )
}

Performance Test Types:

1. Load Testing (Normal Load)
   - Simulate: 1000 concurrent users
   - Duration: 10 minutes
   - Goal: Establish baseline performance
   - Metrics: throughput, latency, resource usage

2. Stress Testing (Beyond Capacity)
   - Simulate: 5000 concurrent users (5x normal)
   - Duration: 10 minutes
   - Goal: Find breaking point
   - Metrics: when does system degrade?

3. Spike Testing (Sudden Surge)
   - Simulate: 0 → 10000 users instantly
   - Duration: 2 minutes
   - Goal: Test auto-scaling, circuit breakers
   - Metrics: recovery time, error rate

4. Endurance Testing (Sustained Load)
   - Simulate: 2000 concurrent users
   - Duration: 24 hours
   - Goal: Detect memory leaks, resource exhaustion
   - Metrics: memory growth, CPU over time

5. Scalability Testing (Horizontal Scaling)
   - Simulate: Increase load, measure scaling
   - Goal: Verify HPA (Horizontal Pod Autoscaler) works
   - Metrics: pod count vs load, performance vs scale

Performance Targets:
- Throughput: > 50,000 req/sec (with reactive services)
- Latency p50: < 50ms
- Latency p95: < 200ms
- Latency p99: < 500ms
- Error rate: < 0.1%
- Availability: > 99.99%

Tools:
- Load testing: Gatling, JMeter, K6
- APM: Azure Application Insights
- Metrics: Prometheus, Grafana
- Profiling: JProfiler, YourKit
```

### Layer 6: Security Testing

```
What: Test security controls, vulnerabilities, and attack resistance

Scope: OWASP Top 10, authentication, authorization, encryption
Speed: Minutes to hours
Coverage: Security vulnerabilities, access controls

Security Test Types:

1. Static Application Security Testing (SAST)
   Tool: SonarQube, Checkmarx
   Scope: Source code analysis
   Runs: On every commit (CI/CD)
   Checks:
   - SQL injection vulnerabilities
   - Cross-site scripting (XSS)
   - Hardcoded secrets
   - Insecure cryptography
   - Path traversal vulnerabilities
   - Authentication/authorization flaws

2. Dynamic Application Security Testing (DAST)
   Tool: OWASP ZAP, Burp Suite
   Scope: Running application
   Runs: Weekly (automated)
   Checks:
   - Injection attacks (SQL, command, LDAP)
   - Broken authentication
   - Sensitive data exposure
   - XML external entities (XXE)
   - Broken access control
   - Security misconfiguration
   - Cross-site scripting (XSS)
   - Insecure deserialization
   - Using components with known vulnerabilities
   - Insufficient logging & monitoring

3. Dependency Scanning
   Tool: Snyk, Dependabot, OWASP Dependency-Check
   Scope: Third-party libraries
   Runs: Daily (automated)
   Checks:
   - Known CVEs in dependencies
   - Outdated dependencies
   - License compliance
   - Transitive dependencies

4. Container Security Scanning
   Tool: Trivy, Clair, Azure Defender
   Scope: Docker images
   Runs: On every build (CI/CD)
   Checks:
   - OS vulnerabilities
   - Application vulnerabilities
   - Misconfigurations
   - Secrets in images

5. Infrastructure Security Testing
   Tool: Checkov, Terrascan, tfsec
   Scope: Terraform code
   Runs: On every commit (CI/CD)
   Checks:
   - Azure misconfigurations
   - Insecure network rules
   - Missing encryption
   - Public access enabled
   - Weak authentication

Example: API Security Test (OWASP ZAP)

# ZAP Automation Script
zap-cli quick-scan \
    --self-contained \
    --start-options '-config api.disablekey=true' \
    --spider \
    --ajax-spider \
    --active-scan \
    --scanners all \
    --report-format html \
    --report-file zap-report.html \
    https://api.paymentsengine.com

# Security Test Cases
1. SQL Injection
   - Test input: ' OR '1'='1
   - Expected: Input rejected, no SQL error

2. Cross-Site Scripting (XSS)
   - Test input: <script>alert('XSS')</script>
   - Expected: Input sanitized/escaped

3. Authentication Bypass
   - Test: Access API without token
   - Expected: 401 Unauthorized

4. Authorization Bypass
   - Test: Access other tenant's data
   - Expected: 403 Forbidden

5. Brute Force Protection
   - Test: 10 failed login attempts
   - Expected: Account locked after 5 attempts

6. Rate Limiting
   - Test: 2000 requests in 1 minute
   - Expected: 429 Too Many Requests after 1000

Security Test Coverage:
- OWASP Top 10: 100%
- Authentication: 100%
- Authorization: 100%
- Input validation: 100%
- Encryption: 100%

Tools:
- SAST: SonarQube, Checkmarx
- DAST: OWASP ZAP, Burp Suite
- Dependency: Snyk, Dependabot
- Container: Trivy, Azure Defender
- Infrastructure: Checkov, tfsec
```

### Layer 7: Chaos Engineering

```
What: Test system resilience by injecting failures

Scope: Service failures, network issues, resource exhaustion
Speed: Minutes to hours
Coverage: Failure scenarios, recovery, fault tolerance

Chaos Testing Framework (Chaos Mesh):

# Chaos Experiment 1: Service Failure

apiVersion: chaos-mesh.org/v1alpha1
kind: PodChaos
metadata:
  name: payment-service-failure
  namespace: payments
spec:
  action: pod-kill
  mode: one
  selector:
    namespaces:
      - payments
    labelSelectors:
      app: payment-service
  scheduler:
    cron: '@hourly'  # Run every hour
  duration: '30s'

# Expected:
# - Kubernetes restarts pod (< 30 seconds)
# - Circuit breaker activates
# - Other services continue normally
# - No failed payments

---

# Chaos Experiment 2: Network Latency

apiVersion: chaos-mesh.org/v1alpha1
kind: NetworkChaos
metadata:
  name: account-adapter-latency
  namespace: payments
spec:
  action: delay
  mode: all
  selector:
    namespaces:
      - payments
    labelSelectors:
      app: account-adapter
  delay:
    latency: '500ms'
    correlation: '100'
    jitter: '0ms'
  duration: '5m'

# Expected:
# - Payment service activates timeout (after 1 second)
# - Circuit breaker opens (after 5 consecutive timeouts)
# - Fallback response returned to user
# - No cascading failures

---

# Chaos Experiment 3: Database Failure

apiVersion: chaos-mesh.org/v1alpha1
kind: PodChaos
metadata:
  name: postgres-failure
  namespace: payments
spec:
  action: pod-failure
  mode: one
  selector:
    namespaces:
      - payments
    labelSelectors:
      app: postgresql
      database: payment-db
  duration: '2m'

# Expected:
# - PostgreSQL replica promoted to primary (< 30 seconds)
# - Payment service reconnects automatically
# - No data loss (write-ahead log)
# - Minimal error rate (< 1% during failover)

---

# Chaos Experiment 4: Resource Exhaustion

apiVersion: chaos-mesh.org/v1alpha1
kind: StressChaos
metadata:
  name: memory-pressure
  namespace: payments
spec:
  mode: one
  selector:
    namespaces:
      - payments
    labelSelectors:
      app: validation-service
  stressors:
    memory:
      workers: 4
      size: '90%'  # Consume 90% of available memory
  duration: '5m'

# Expected:
# - Kubernetes detects high memory usage
# - Pod restarted if OOM (Out of Memory)
# - Other pods continue normally
# - Auto-scaling triggered if needed

Chaos Testing Scenarios:

1. Service Failures
   - Random pod kills
   - Service crashes
   - Expected: Self-healing, no user impact

2. Network Issues
   - Latency injection (500ms delay)
   - Packet loss (10% packets dropped)
   - Network partition (split-brain)
   - Expected: Circuit breakers, timeouts, retries work

3. Resource Exhaustion
   - CPU saturation (100% CPU)
   - Memory pressure (90% memory used)
   - Disk full (no space left)
   - Expected: Resource limits, auto-scaling, graceful degradation

4. Database Failures
   - Primary database down
   - Replica lag (10 seconds behind)
   - Connection pool exhaustion
   - Expected: Failover, connection retry, degraded mode

5. External Service Failures
   - Core banking API down
   - Fraud scoring API timeout
   - Clearing system unavailable
   - Expected: Circuit breakers, fallback responses, compensation

Chaos Testing Schedule:
- Production: Weekly (off-peak hours)
- Staging: Daily
- Dev: Continuous

Tools:
- Framework: Chaos Mesh, Litmus
- Orchestration: Kubernetes
- Monitoring: Grafana, Prometheus
- Alerting: PagerDuty
```

---

## Test Data Management

### Test Data Strategy

```
Test Data Types:

1. Synthetic Data (Generated)
   - Tool: Faker, Instancio
   - Use: Unit tests, load tests
   - Volume: Unlimited
   - PII: No real PII
   - Example:
     Customer {
       name: "John Doe" (generated)
       email: "john.doe.12345@example.com" (generated)
       phone: "+27-81-123-4567" (generated)
       id_number: "8001015009087" (valid format, fake)
     }

2. Anonymized Production Data
   - Process: Copy production → anonymize PII → load to staging
   - Use: Integration tests, E2E tests (staging)
   - Volume: 10-20% of production
   - PII: All PII masked/anonymized
   - Example:
     Original: John Smith, john.smith@gmail.com, +27-81-234-5678
     Anonymized: User-12345, user.12345@example.com, +27-81-***-****

3. Edge Cases (Manual)
   - Use: Boundary testing, error scenarios
   - Volume: 100-200 test cases
   - Examples:
     - Account number: "0000000000" (all zeros)
     - Amount: 0.01 (minimum), 999999999.99 (maximum)
     - Currency: "ZAR", "USD", "EUR", "XXX" (invalid)
     - Special characters: "O'Brien", "Müller"

4. Production-Like Data (Staging)
   - Process: Daily refresh from production (anonymized)
   - Use: Staging environment, UAT
   - Volume: 20-30% of production
   - Refresh: Daily (automated)

Data Anonymization Script:

-- Anonymize production data for staging
UPDATE customers SET
    name = 'Customer-' || customer_id,
    email = 'customer.' || customer_id || '@example.com',
    phone = '+27-81-' || LPAD(FLOOR(RANDOM() * 10000000)::TEXT, 7, '0'),
    id_number = anonymize_id_number(id_number),  -- Keep format, change digits
    address = 'Address ' || customer_id || ', Cape Town, 8001'
WHERE environment = 'staging';

-- Anonymize account numbers (keep format)
UPDATE accounts SET
    account_number = 'ACC' || LPAD(account_id::TEXT, 10, '0')
WHERE environment = 'staging';

-- Anonymize transaction details
UPDATE transactions SET
    reference = 'TEST-' || transaction_id,
    beneficiary_name = 'Beneficiary-' || transaction_id
WHERE environment = 'staging';
```

### Test Data Isolation

```
Test Data Isolation Strategy:

Development Environment:
- Each developer has isolated test data
- Test data reset daily (automated)
- Shared test accounts for common scenarios

Staging Environment:
- Tenant-specific test data (per bank)
- Refreshed daily from production (anonymized)
- Isolated per test suite

Production Environment:
- NO test data (production data only)
- Test transactions clearly marked (TEST-prefix)
- Automatically reversed after testing

Test Tenant:
tenant_id: "TEST-001"
name: "Test Bank"
status: "test_mode"
data_retention: "7 days"  # Auto-delete after 7 days

All test payments:
reference: "TEST-{test-id}"
metadata: { is_test: true }
auto_reverse: true  # Reversed after 1 hour
```

---

## Testing in CI/CD Pipeline

### Automated Test Execution

```
CI/CD Pipeline Test Stages:

┌─────────────────────────────────────────────────────────────┐
│  Commit → Git Push                                          │
└───────────┬─────────────────────────────────────────────────┘
            │
            ▼
┌─────────────────────────────────────────────────────────────┐
│  STAGE 1: Fast Tests (< 5 minutes)                          │
├─────────────────────────────────────────────────────────────┤
│  ✅ Linting (Checkstyle, ESLint)                            │
│  ✅ Unit tests (JUnit, 10,000+ tests)                       │
│  ✅ Code coverage (JaCoCo, > 80%)                           │
│  ✅ SAST (SonarQube)                                        │
│                                                             │
│  If FAIL → Stop pipeline, notify developer                 │
│  If PASS → Continue to Stage 2                             │
└─────────────────────────────────────────────────────────────┘
            │
            ▼
┌─────────────────────────────────────────────────────────────┐
│  STAGE 2: Integration Tests (5-10 minutes)                  │
├─────────────────────────────────────────────────────────────┤
│  ✅ Integration tests (1,500+ tests, Testcontainers)        │
│  ✅ API contract tests (Pact)                               │
│  ✅ Database integration tests                              │
│  ✅ Kafka integration tests                                 │
│                                                             │
│  If FAIL → Stop pipeline, notify developer                 │
│  If PASS → Continue to Stage 3                             │
└─────────────────────────────────────────────────────────────┘
            │
            ▼
┌─────────────────────────────────────────────────────────────┐
│  STAGE 3: Security Tests (5 minutes)                        │
├─────────────────────────────────────────────────────────────┤
│  ✅ Dependency scan (Snyk)                                  │
│  ✅ Secret detection (GitGuardian)                          │
│  ✅ Container scan (Trivy)                                  │
│  ✅ Infrastructure scan (Checkov)                           │
│                                                             │
│  If CRITICAL vulnerability → Stop pipeline                  │
│  If PASS → Continue to Stage 4                             │
└─────────────────────────────────────────────────────────────┘
            │
            ▼
┌─────────────────────────────────────────────────────────────┐
│  STAGE 4: Build & Deploy to Dev (5 minutes)                │
├─────────────────────────────────────────────────────────────┤
│  ✅ Build Docker image                                      │
│  ✅ Push to ACR                                             │
│  ✅ Update GitOps repo                                      │
│  ✅ ArgoCD syncs to dev                                     │
│  ✅ Smoke tests (basic API calls)                           │
│                                                             │
│  If FAIL → Stop pipeline                                    │
│  If PASS → Continue to Stage 5                             │
└─────────────────────────────────────────────────────────────┘
            │
            ▼
┌─────────────────────────────────────────────────────────────┐
│  STAGE 5: E2E Tests in Dev (10-15 minutes)                 │
├─────────────────────────────────────────────────────────────┤
│  ✅ E2E test suite (500+ tests)                             │
│  ✅ Payment journey tests                                   │
│  ✅ Multi-service flow tests                                │
│  ✅ Error scenario tests                                    │
│                                                             │
│  If < 95% pass → Stop pipeline                             │
│  If PASS → Ready for staging                               │
└─────────────────────────────────────────────────────────────┘
            │
            ▼
┌─────────────────────────────────────────────────────────────┐
│  STAGE 6: Deploy to Staging (15 minutes)                   │
├─────────────────────────────────────────────────────────────┤
│  ✅ Manual approval gate                                    │
│  ✅ Deploy to staging (ArgoCD)                              │
│  ✅ Smoke tests                                             │
│  ✅ Performance tests (load test, 10 min)                   │
│  ✅ Security tests (DAST)                                   │
│                                                             │
│  If FAIL → Stop, investigate                                │
│  If PASS → Ready for production                            │
└─────────────────────────────────────────────────────────────┘
            │
            ▼
┌─────────────────────────────────────────────────────────────┐
│  STAGE 7: Deploy to Production (2-4 hours)                 │
├─────────────────────────────────────────────────────────────┤
│  ✅ Manual approval gate (required)                         │
│  ✅ Deploy canary (10% traffic)                             │
│  ✅ Monitor metrics (30 min)                                │
│  ✅ Gradually increase (25% → 50% → 100%)                   │
│  ✅ Automated rollback if issues                            │
│                                                             │
│  If FAIL → Automatic rollback                               │
│  If PASS → Deployment complete ✅                           │
└─────────────────────────────────────────────────────────────┘

Total Pipeline Time:
- Fast path (to dev): 20-30 minutes
- Full path (to production): 3-5 hours

Test Execution:
- Unit: Every commit (~10 min)
- Integration: Every commit (~10 min)
- E2E: Every commit to main (~15 min)
- Performance: Nightly + before production deploy
- Security: Daily (DAST) + every commit (SAST)
- Chaos: Weekly (production), daily (staging)
```

---

## Test Environments

### Environment Test Configuration

```
┌─────────────────────────────────────────────────────────────┐
│  DEVELOPMENT ENVIRONMENT                                    │
├─────────────────────────────────────────────────────────────┤
│  Purpose: Developer testing, feature validation            │
│  Data: Synthetic data (Faker)                              │
│  Tests Run:                                                 │
│  ├─ Unit tests (continuous)                                │
│  ├─ Integration tests (continuous)                         │
│  ├─ Smoke tests (after deployment)                         │
│  └─ Developer manual testing                               │
│                                                             │
│  Infrastructure:                                            │
│  ├─ AKS: 3 nodes                                           │
│  ├─ Services: 1 replica each                               │
│  ├─ Databases: Shared PostgreSQL                           │
│  └─ Testcontainers for isolated tests                      │
│                                                             │
│  Test Execution:                                            │
│  ├─ Frequency: Every commit                                │
│  ├─ Duration: 10-15 minutes                                │
│  └─ Automated: Yes                                          │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│  STAGING ENVIRONMENT                                        │
├─────────────────────────────────────────────────────────────┤
│  Purpose: Pre-production testing, QA, UAT                  │
│  Data: Anonymized production data (refreshed daily)        │
│  Tests Run:                                                 │
│  ├─ Integration tests (daily)                              │
│  ├─ E2E tests (daily)                                       │
│  ├─ Performance tests (nightly)                            │
│  ├─ Security tests (weekly)                                │
│  ├─ Chaos tests (daily)                                    │
│  └─ UAT (manual, before production deploy)                 │
│                                                             │
│  Infrastructure:                                            │
│  ├─ AKS: 10 nodes (80% of production)                     │
│  ├─ Services: 8 replicas each                              │
│  ├─ Databases: Production-like (smaller size)              │
│  ├─ Monitoring: Full stack (Prometheus, Grafana, Jaeger)  │
│  └─ Mirrors production configuration                       │
│                                                             │
│  Test Execution:                                            │
│  ├─ Frequency: Daily                                        │
│  ├─ Duration: 1-2 hours                                     │
│  └─ Automated: Yes (+ manual UAT)                          │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│  PRODUCTION ENVIRONMENT                                     │
├─────────────────────────────────────────────────────────────┤
│  Purpose: Live customer traffic                            │
│  Data: Real production data                                │
│  Tests Run:                                                 │
│  ├─ Smoke tests (after deployment)                         │
│  ├─ Synthetic transaction tests (hourly)                   │
│  ├─ Chaos tests (weekly, off-peak)                         │
│  └─ Monitoring & alerting (continuous)                     │
│                                                             │
│  Infrastructure:                                            │
│  ├─ AKS: 10 cells, 40 nodes each                          │
│  ├─ Services: 10 replicas each (auto-scaled)              │
│  ├─ Databases: Full production scale                       │
│  └─ Complete monitoring stack                              │
│                                                             │
│  Test Execution:                                            │
│  ├─ Smoke: After every deployment                          │
│  ├─ Synthetic: Hourly (verify system working)             │
│  └─ Chaos: Weekly (off-peak hours)                         │
└─────────────────────────────────────────────────────────────┘
```

---

## Test Automation Framework

### Test Framework Structure

```
payments-engine-tests/
├── unit/
│   ├── payment-service/
│   │   ├── PaymentServiceTest.java
│   │   ├── PaymentValidationTest.java
│   │   └── PaymentRepositoryTest.java
│   │
│   ├── validation-service/
│   └── ... (15 more services)
│
├── integration/
│   ├── payment-flow/
│   │   ├── PaymentInitiationIntegrationTest.java
│   │   ├── PaymentProcessingIntegrationTest.java
│   │   └── PaymentCompensationIntegrationTest.java
│   │
│   ├── database/
│   │   ├── PaymentRepositoryIntegrationTest.java
│   │   └── TransactionRepositoryIntegrationTest.java
│   │
│   ├── kafka/
│   │   ├── PaymentEventPublishingTest.java
│   │   └── PaymentEventConsumptionTest.java
│   │
│   └── external-services/
│       ├── CoreBankingIntegrationTest.java
│       └── FraudApiIntegrationTest.java
│
├── contract/
│   ├── consumer/
│   │   └── PaymentValidationContractTest.java
│   │
│   └── provider/
│       └── ValidationServiceContractTest.java
│
├── e2e/
│   ├── journeys/
│   │   ├── CompletePaymentJourneyTest.java
│   │   ├── FailedPaymentJourneyTest.java
│   │   └── BulkPaymentJourneyTest.java
│   │
│   ├── scenarios/
│   │   ├── InsufficientBalanceScenarioTest.java
│   │   ├── DailyLimitExceededScenarioTest.java
│   │   └── FraudDetectionScenarioTest.java
│   │
│   └── multi-tenant/
│       ├── TenantIsolationTest.java
│       └── CrossTenantSecurityTest.java
│
├── performance/
│   ├── load/
│   │   ├── PaymentLoadTest.scala (Gatling)
│   │   └── AccountLoadTest.scala
│   │
│   ├── stress/
│   │   └── SystemStressTest.scala
│   │
│   └── endurance/
│       └── LongRunningTest.scala
│
├── security/
│   ├── owasp/
│   │   ├── SqlInjectionTest.java
│   │   ├── XssTest.java
│   │   └── CsrfTest.java
│   │
│   ├── authentication/
│   │   ├── JwtValidationTest.java
│   │   └─ MfaTest.java
│   │
│   └── authorization/
│       ├── RbacTest.java
│       └── TenantIsolationTest.java
│
├── chaos/
│   ├── experiments/
│   │   ├── service-failure.yaml
│   │   ├── network-latency.yaml
│   │   ├── database-failure.yaml
│   │   └── resource-exhaustion.yaml
│   │
│   └── validation/
│       └── chaos-validation-tests.sh
│
├── smoke/
│   └── smoke-tests.sh (API health checks)
│
└── test-utils/
    ├── TestDataBuilder.java
    ├── TestContainersConfig.java
    ├── MockExternalServices.java
    └── TestHelpers.java

Total Tests:
- Unit: ~10,000 tests
- Integration: ~1,500 tests
- Contract: ~200 tests
- E2E: ~500 tests
- Performance: ~50 scenarios
- Security: ~300 tests
- Chaos: ~20 experiments

Total: ~12,500+ automated tests
```

---

## Test Metrics & Reporting

### Test Metrics Dashboard

```
Grafana Testing Dashboard:

┌─────────────────────────────────────────────────────────────┐
│  Test Execution Metrics (Last 24 Hours)                     │
├─────────────────────────────────────────────────────────────┤
│                                                              │
│  Unit Tests:                                                 │
│  ├─ Total: 10,245 tests                                     │
│  ├─ Passed: 10,243 (99.98%) ✅                              │
│  ├─ Failed: 2 (0.02%) ⚠️                                    │
│  ├─ Skipped: 0                                              │
│  ├─ Duration: 3m 45s                                        │
│  └─ Coverage: 84.5% ✅ (target: > 80%)                      │
│                                                              │
│  Integration Tests:                                          │
│  ├─ Total: 1,542 tests                                      │
│  ├─ Passed: 1,540 (99.87%) ✅                               │
│  ├─ Failed: 2 (0.13%) ⚠️                                    │
│  ├─ Duration: 8m 12s                                        │
│  └─ Success Rate: 99.87%                                    │
│                                                              │
│  E2E Tests:                                                  │
│  ├─ Total: 487 tests                                        │
│  ├─ Passed: 485 (99.59%) ✅                                 │
│  ├─ Failed: 2 (0.41%) ⚠️                                    │
│  ├─ Duration: 12m 34s                                       │
│  └─ Success Rate: 99.59%                                    │
│                                                              │
│  Performance Tests (Nightly):                                │
│  ├─ Throughput: 52,145 req/sec ✅ (target: > 50K)          │
│  ├─ Latency p95: 87ms ✅ (target: < 200ms)                 │
│  ├─ Error Rate: 0.08% ✅ (target: < 1%)                    │
│  └─ Duration: 45 minutes                                     │
│                                                              │
│  Security Tests (Weekly):                                    │
│  ├─ Vulnerabilities Found: 3 (all LOW) ✅                   │
│  ├─ OWASP Top 10: All PASS ✅                               │
│  ├─ Penetration Test: PASS ✅                               │
│  └─ Last Run: 2 days ago                                    │
└─────────────────────────────────────────────────────────────┘

Test Failure Analysis:
┌──────────────────────────────────────────────────────┐
│  Failed Tests (Last 7 Days)                          │
├──────────────────────────────────────────────────────┤
│  1. PaymentServiceTest.testHighValuePayment          │
│     ├─ Failure: Timeout exception                    │
│     ├─ Frequency: 3/100 runs (flaky test)           │
│     └─ Action: Fix timeout configuration             │
│                                                       │
│  2. AccountAdapterTest.testExternalApiFailure        │
│     ├─ Failure: Unexpected exception                │
│     ├─ Frequency: 2/100 runs                         │
│     └─ Action: Improve error handling                │
└──────────────────────────────────────────────────────┘
```

### Test Reporting

```
Test Report (Published after every build):

┌─────────────────────────────────────────────────────────────┐
│  Build #1234 - Payment Service                              │
│  Branch: main                                               │
│  Commit: abc123 - "Add payment validation"                 │
│  Author: John Doe                                           │
│  Date: 2025-10-11 10:00:00                                 │
├─────────────────────────────────────────────────────────────┤
│                                                              │
│  Test Results: ✅ PASS                                      │
│                                                              │
│  Unit Tests:                                                 │
│  ├─ Total: 245 tests                                        │
│  ├─ Passed: 245 (100%) ✅                                   │
│  ├─ Duration: 12 seconds                                    │
│  └─ Coverage: 87.3% ✅                                      │
│                                                              │
│  Integration Tests:                                          │
│  ├─ Total: 42 tests                                         │
│  ├─ Passed: 42 (100%) ✅                                    │
│  ├─ Duration: 3m 24s                                        │
│  └─ Success Rate: 100%                                      │
│                                                              │
│  Code Quality (SonarQube):                                  │
│  ├─ Quality Gate: PASS ✅                                   │
│  ├─ Bugs: 0                                                 │
│  ├─ Vulnerabilities: 0                                      │
│  ├─ Code Smells: 5 (all minor)                             │
│  └─ Technical Debt: 2h (0.5%)                              │
│                                                              │
│  Security Scan:                                             │
│  ├─ Dependencies: 0 critical, 2 medium                     │
│  ├─ Container: PASS (no critical CVEs)                     │
│  └─ Secrets: PASS (no secrets detected)                    │
│                                                              │
│  Next Steps:                                                │
│  ✅ Ready to deploy to dev                                 │
│  ✅ Automated deployment triggered                          │
└─────────────────────────────────────────────────────────────┘

Reports Published To:
- Azure DevOps (test results, trends)
- SonarQube (code quality)
- Snyk (security vulnerabilities)
- Slack (test failure notifications)
- Email (weekly test summary)
```

---

## Performance Testing Strategy

### Load Testing Scenarios

```
Load Test Scenarios:

Scenario 1: Normal Load (Baseline)
├─ Users: 1,000 concurrent
├─ Duration: 10 minutes
├─ RPS: 10,000 requests/second
├─ Goal: Establish baseline metrics
└─ Success criteria:
    ├─ p95 latency < 200ms
    ├─ Error rate < 0.1%
    └─ CPU < 70%, Memory < 75%

Scenario 2: Peak Load (3x Normal)
├─ Users: 3,000 concurrent
├─ Duration: 10 minutes
├─ RPS: 30,000 requests/second
├─ Goal: Test system under peak conditions
└─ Success criteria:
    ├─ p95 latency < 300ms
    ├─ Error rate < 1%
    └─ System stable (no crashes)

Scenario 3: Stress Test (5x Normal)
├─ Users: 5,000 concurrent
├─ Duration: 10 minutes
├─ RPS: 50,000 requests/second
├─ Goal: Find breaking point
└─ Success criteria:
    ├─ Graceful degradation (no crashes)
    ├─ Circuit breakers activate
    └─ Auto-scaling triggers

Scenario 4: Spike Test (Sudden Surge)
├─ Users: 0 → 10,000 instantly
├─ Duration: 2 minutes
├─ RPS: 0 → 100,000 instantly
├─ Goal: Test auto-scaling responsiveness
└─ Success criteria:
    ├─ HPA scales pods within 30 seconds
    ├─ Error rate < 5% during spike
    └─ System recovers within 5 minutes

Scenario 5: Endurance Test (24 Hours)
├─ Users: 2,000 concurrent
├─ Duration: 24 hours
├─ RPS: 20,000 requests/second
├─ Goal: Detect memory leaks, resource exhaustion
└─ Success criteria:
    ├─ No memory leaks (stable memory usage)
    ├─ No resource exhaustion
    └─ Consistent performance (no degradation)

Performance Test Execution:
- Frequency: Nightly (scenarios 1-3)
- Frequency: Weekly (scenario 4-5)
- Frequency: Before production deploy (all scenarios)
- Automated: Yes
- Tool: Gatling, JMeter, K6
```

### Performance Benchmarks

```
Performance Targets (Per Service):

Payment Service:
├─ Throughput: > 50,000 req/sec (reactive)
├─ Latency p50: < 50ms
├─ Latency p95: < 200ms
├─ Latency p99: < 500ms
├─ Error rate: < 0.1%
└─ Availability: > 99.99%

Validation Service:
├─ Throughput: > 40,000 req/sec
├─ Latency p95: < 150ms
└─ Error rate: < 0.1%

Account Adapter Service:
├─ Throughput: > 60,000 req/sec (reactive)
├─ Latency p95: < 300ms (includes external API)
├─ Circuit breaker: Activates if > 50% failures
└─ Error rate: < 1%

Saga Orchestrator:
├─ Throughput: > 5,000 req/sec
├─ Latency p95: < 1000ms (distributed transaction)
├─ Compensation: < 5 seconds
└─ Error rate: < 0.5%

System-Wide:
├─ End-to-end latency: < 2 seconds (p95)
├─ Concurrent users: 10,000+
├─ Data throughput: 1 GB/sec
└─ Database connections: < 80% of pool
```

---

## Quality Gates

### Gate 1: Code Quality

```yaml
# SonarQube Quality Gate Configuration

quality_gate:
  name: "Payments Engine Quality Gate"
  
  conditions:
    # Coverage
    - metric: coverage
      operator: LESS_THAN
      threshold: 80
      status: FAILED
    
    # Bugs
    - metric: new_bugs
      operator: GREATER_THAN
      threshold: 0
      status: FAILED
    
    # Vulnerabilities
    - metric: new_vulnerabilities
      operator: GREATER_THAN
      threshold: 0
      status: FAILED
    
    # Code Smells
    - metric: new_code_smells
      operator: GREATER_THAN
      threshold: 10
      status: WARNING
    
    # Duplications
    - metric: new_duplicated_lines_density
      operator: GREATER_THAN
      threshold: 3
      status: WARNING
    
    # Maintainability
    - metric: new_maintainability_rating
      operator: WORSE_THAN
      threshold: A
      status: FAILED
    
    # Security Rating
    - metric: new_security_rating
      operator: WORSE_THAN
      threshold: A
      status: FAILED
    
    # Reliability Rating
    - metric: new_reliability_rating
      operator: WORSE_THAN
      threshold: A
      status: FAILED

If Quality Gate FAILS → Block deployment ❌
```

### Gate 2: Test Success

```
Test Success Gate:

Required:
├─ Unit tests: 100% pass (0 failures allowed)
├─ Integration tests: 100% pass (0 failures allowed)
├─ E2E tests: > 95% pass (< 5% failures allowed for flaky tests)
├─ Contract tests: 100% pass (breaking changes not allowed)
└─ Smoke tests: 100% pass (deployment verification)

Optional (Can warn, not block):
├─ Performance tests: < 10% regression
├─ Security tests: No critical vulnerabilities
└─ Chaos tests: System recovers within SLA

If Required FAILS → Block deployment ❌
If Optional FAILS → Warning, manual review
```

### Gate 3: Security Quality

```
Security Quality Gate:

Required:
├─ Critical vulnerabilities: 0
├─ Secrets in code: 0
├─ Container critical CVEs: 0
├─ Infrastructure misconfigurations: 0 critical
└─ SAST critical findings: 0

Acceptable (Can deploy with warning):
├─ High vulnerabilities: < 5 (with remediation plan)
├─ Medium vulnerabilities: < 20
├─ Low vulnerabilities: Any
└─ SAST high findings: < 10

If Critical vulnerabilities → Block deployment ❌
If High vulnerabilities → Warning, require approval
```

### Gate 4: Performance Quality

```
Performance Quality Gate:

Regression Thresholds:
├─ Response time increase: < 10%
├─ Throughput decrease: < 10%
├─ Error rate increase: < 0.5%
└─ Resource usage increase: < 20%

Absolute Thresholds:
├─ API response time: p95 < 200ms
├─ Throughput: > 45,000 req/sec
├─ Error rate: < 1%
├─ Memory usage: < 85% limit
└─ CPU usage: < 80% limit

If Regression > 10% → Block deployment ❌
If Absolute threshold exceeded → Block deployment ❌
```

---

## Test Data Seeding

### Database Test Data

```sql
-- Test data seeding script (for dev/staging)

-- Seed tenants
INSERT INTO tenants (tenant_id, name, status, created_at) VALUES
('TEST-001', 'Test Bank', 'test_mode', NOW()),
('STD-001', 'Standard Bank (Test)', 'active', NOW()),
('NED-001', 'Nedbank (Test)', 'active', NOW());

-- Seed customers (1000 test customers)
INSERT INTO customers (customer_id, tenant_id, name, email, id_number, created_at)
SELECT 
    'CUST-' || LPAD(generate_series::TEXT, 6, '0'),
    'TEST-001',
    'Customer ' || generate_series,
    'customer.' || generate_series || '@example.com',
    '80' || LPAD((generate_series % 100)::TEXT, 2, '0') || 
    '01' || LPAD((generate_series % 30)::TEXT, 2, '0') || 
    LPAD(FLOOR(RANDOM() * 10000)::TEXT, 4, '0') || 
    FLOOR(RANDOM() * 10),
    NOW()
FROM generate_series(1, 1000);

-- Seed accounts (2000 test accounts, 2 per customer)
INSERT INTO accounts (account_number, customer_id, account_type, balance, currency, status, created_at)
SELECT 
    'ACC' || LPAD((generate_series * 2)::TEXT, 10, '0'),
    'CUST-' || LPAD(((generate_series + 1) / 2)::TEXT, 6, '0'),
    CASE (generate_series % 2) 
        WHEN 0 THEN 'CURRENT'
        ELSE 'SAVINGS'
    END,
    FLOOR(RANDOM() * 100000) + 10000,  -- Balance: 10K - 110K
    'ZAR',
    'ACTIVE',
    NOW()
FROM generate_series(1, 2000);

-- Seed transactions (historical data, 10K transactions)
INSERT INTO transactions (transaction_id, account_number, amount, type, status, created_at)
SELECT 
    'TXN-' || LPAD(generate_series::TEXT, 10, '0'),
    'ACC' || LPAD((FLOOR(RANDOM() * 2000) + 1)::TEXT, 10, '0'),
    FLOOR(RANDOM() * 50000) + 100,
    CASE FLOOR(RANDOM() * 3)
        WHEN 0 THEN 'DEBIT'
        WHEN 1 THEN 'CREDIT'
        ELSE 'TRANSFER'
    END,
    'COMPLETED',
    NOW() - (FLOOR(RANDOM() * 90) || ' days')::INTERVAL
FROM generate_series(1, 10000);

-- Seed payment limits
INSERT INTO payment_limits (customer_id, limit_type, limit_amount, used_amount, reset_frequency, created_at)
SELECT 
    'CUST-' || LPAD(generate_series::TEXT, 6, '0'),
    'DAILY',
    100000,  -- 100K ZAR daily limit
    0,
    'DAILY',
    NOW()
FROM generate_series(1, 1000);

-- Test data cleanup (run before tests)
DELETE FROM transactions WHERE environment = 'test';
DELETE FROM payments WHERE environment = 'test';
```

---

## Summary

### Testing Architecture Highlights

✅ **Test Pyramid**: 80% unit, 15% integration, 5% E2E  
✅ **12,500+ Automated Tests**: Comprehensive coverage  
✅ **Shift-Left Testing**: Tests run during development  
✅ **Quality Gates**: 4 gates (code, tests, security, performance)  
✅ **Contract Testing**: Prevent breaking changes (Pact)  
✅ **Performance Testing**: Load, stress, spike, endurance  
✅ **Security Testing**: SAST, DAST, dependency, container scanning  
✅ **Chaos Engineering**: Resilience testing (weekly)  
✅ **Test Automation**: 90%+ automated  
✅ **Fast Feedback**: < 10 minutes for commit feedback  

### Implementation Effort

**Phase 1: Unit & Integration Tests** (2-3 weeks)
- Set up test frameworks (JUnit, Mockito, Testcontainers)
- Write unit tests (10,000+ tests)
- Write integration tests (1,500+ tests)
- Configure code coverage (JaCoCo)

**Phase 2: E2E & Contract Tests** (2 weeks)
- Set up E2E framework (Spring Boot Test, RestAssured)
- Write E2E tests (500+ tests)
- Set up contract testing (Pact)
- Configure test environments

**Phase 3: Performance & Security Tests** (2 weeks)
- Set up performance testing (Gatling)
- Write load test scenarios
- Configure security scanning (OWASP ZAP, Snyk, Trivy)
- Set up chaos engineering (Chaos Mesh)

**Phase 4: CI/CD Integration** (1 week)
- Integrate tests into pipelines
- Configure quality gates
- Set up test reporting
- Configure automated rollback on test failure

**Total**: 7-8 weeks

### Test Coverage Targets

| Test Type | Coverage Target | Current |
|-----------|-----------------|---------|
| **Unit Tests** | 80% code coverage | - |
| **Integration Tests** | 100% API endpoints | - |
| **E2E Tests** | 100% critical journeys | - |
| **Contract Tests** | 100% service contracts | - |
| **Performance Tests** | All services | - |
| **Security Tests** | OWASP Top 10 | - |
| **Chaos Tests** | All failure scenarios | - |

### Testing Maturity

Your testing architecture achieves:
- ✅ **Level 4: Optimized** (comprehensive, automated, continuously improving)
- ✅ **Industry Best Practices**: Test pyramid, shift-left, automation
- ✅ **Quality Assurance**: Multiple quality gates
- ✅ **Fast Feedback**: < 10 minutes for commit feedback

**Verdict**: **Production-ready testing architecture** ensuring **high quality** and **reliability** for a **financial payments platform** handling **billions of rands** in transactions. 🏆 ✅

---

## Related Documents

- **[22-DEPLOYMENT-ARCHITECTURE.md](22-DEPLOYMENT-ARCHITECTURE.md)** - CI/CD integration
- **[21-SECURITY-ARCHITECTURE.md](21-SECURITY-ARCHITECTURE.md)** - Security testing
- **[16-DISTRIBUTED-TRACING.md](16-DISTRIBUTED-TRACING.md)** - Troubleshooting failed tests
- **[00-ARCHITECTURE-OVERVIEW.md](00-ARCHITECTURE-OVERVIEW.md)** - Overall architecture

---

**Last Updated**: 2025-10-11  
**Version**: 1.0  
**Classification**: Internal
