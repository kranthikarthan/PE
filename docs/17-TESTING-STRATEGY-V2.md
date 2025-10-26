# Testing Strategy v2 - Comprehensive Testing Framework

## 🎯 **Testing Strategy Overview**

The Payments Engine v2 implements a comprehensive testing strategy with unit tests, integration tests, end-to-end tests, and performance tests for ISO 20022 message processing and UETR correlation.

## 🏗️ **Testing Pyramid**

### **Testing Layers**
```yaml
Unit Tests (70%):
  - Service layer tests
  - Repository layer tests
  - Utility class tests
  - ISO 20022 message tests
  - UETR correlation tests

Integration Tests (20%):
  - API integration tests
  - Database integration tests
  - Message queue tests
  - External service tests

End-to-End Tests (10%):
  - Complete payment flows
  - ISO 20022 message flows
  - UETR correlation flows
  - Multi-service workflows
```

### **Testing Principles**
```yaml
Test Coverage:
  - Minimum 80% code coverage
  - 100% critical path coverage
  - 100% ISO 20022 message coverage
  - 100% UETR correlation coverage

Test Quality:
  - Fast execution
  - Reliable results
  - Clear assertions
  - Comprehensive scenarios
```

## 🔧 **Unit Testing**

### **Service Layer Tests**
```java
@ExtendWith(MockitoExtension.class)
class PaymentInitiationServiceTest {
    
    @Mock
    private PaymentRepository paymentRepository;
    
    @Mock
    private Iso20022MessageService iso20022MessageService;
    
    @Mock
    private UetrService uetrService;
    
    @InjectMocks
    private PaymentInitiationService paymentInitiationService;
    
    @Test
    void shouldInitiatePaymentSuccessfully() {
        // Given
        PaymentRequest request = PaymentRequest.builder()
            .paymentId("PAY-001")
            .amount(new BigDecimal("1000.00"))
            .currency("ZAR")
            .sourceAccount("ACC-001")
            .destinationAccount("ACC-002")
            .build();
            
        String uetr = "550e8400-e29b-41d4-a716-446655440000";
        when(uetrService.generateUetr()).thenReturn(uetr);
        when(paymentRepository.save(any(Payment.class))).thenReturn(createPayment());
        
        // When
        PaymentResponse response = paymentInitiationService.initiatePayment(request);
        
        // Then
        assertThat(response.getPaymentId()).isEqualTo("PAY-001");
        assertThat(response.getUetr()).isEqualTo(uetr);
        assertThat(response.getStatus()).isEqualTo(PaymentStatus.PROCESSING);
        
        verify(uetrService).generateUetr();
        verify(paymentRepository).save(any(Payment.class));
        verify(iso20022MessageService).processPain001(any(Pain001Message.class));
    }
    
    @Test
    void shouldThrowExceptionWhenPaymentIdIsNull() {
        // Given
        PaymentRequest request = PaymentRequest.builder()
            .paymentId(null)
            .amount(new BigDecimal("1000.00"))
            .currency("ZAR")
            .build();
            
        // When & Then
        assertThatThrownBy(() -> paymentInitiationService.initiatePayment(request))
            .isInstanceOf(ValidationException.class)
            .hasMessage("Payment ID is required");
    }
    
    @Test
    void shouldThrowExceptionWhenAmountIsNegative() {
        // Given
        PaymentRequest request = PaymentRequest.builder()
            .paymentId("PAY-001")
            .amount(new BigDecimal("-100.00"))
            .currency("ZAR")
            .build();
            
        // When & Then
        assertThatThrownBy(() -> paymentInitiationService.initiatePayment(request))
            .isInstanceOf(ValidationException.class)
            .hasMessage("Amount must be greater than 0");
    }
}
```

### **ISO 20022 Message Tests**
```java
@ExtendWith(MockitoExtension.class)
class Iso20022MessageServiceTest {
    
    @Mock
    private Iso20022MessageRepository messageRepository;
    
    @Mock
    private XsdValidator xsdValidator;
    
    @Mock
    private JsonSchemaValidator jsonSchemaValidator;
    
    @InjectMocks
    private Iso20022MessageService iso20022MessageService;
    
    @Test
    void shouldProcessPain001MessageSuccessfully() {
        // Given
        Pain001Message pain001Message = createPain001Message();
        when(xsdValidator.validate(anyString())).thenReturn(ValidationResult.success());
        when(messageRepository.save(any(Iso20022Message.class))).thenReturn(createIso20022Message());
        
        // When
        Iso20022Message result = iso20022MessageService.processPain001(pain001Message);
        
        // Then
        assertThat(result.getMessageType()).isEqualTo("pain.001");
        assertThat(result.getStatus()).isEqualTo(MessageStatus.VALIDATED);
        assertThat(result.getUetr()).isNotNull();
        
        verify(xsdValidator).validate(anyString());
        verify(messageRepository).save(any(Iso20022Message.class));
    }
    
    @Test
    void shouldThrowExceptionWhenPain001MessageIsInvalid() {
        // Given
        Pain001Message pain001Message = createInvalidPain001Message();
        when(xsdValidator.validate(anyString())).thenReturn(ValidationResult.failure("Invalid XML"));
        
        // When & Then
        assertThatThrownBy(() -> iso20022MessageService.processPain001(pain001Message))
            .isInstanceOf(ValidationException.class)
            .hasMessage("Invalid XML");
    }
    
    @Test
    void shouldProcessPain002MessageSuccessfully() {
        // Given
        Pain002Message pain002Message = createPain002Message();
        when(xsdValidator.validate(anyString())).thenReturn(ValidationResult.success());
        when(messageRepository.save(any(Iso20022Message.class))).thenReturn(createIso20022Message());
        
        // When
        Iso20022Message result = iso20022MessageService.processPain002(pain002Message);
        
        // Then
        assertThat(result.getMessageType()).isEqualTo("pain.002");
        assertThat(result.getStatus()).isEqualTo(MessageStatus.VALIDATED);
        assertThat(result.getUetr()).isNotNull();
    }
}
```

### **UETR Correlation Tests**
```java
@ExtendWith(MockitoExtension.class)
class UetrCorrelationServiceTest {
    
    @Mock
    private UetrRepository uetrRepository;
    
    @Mock
    private Iso20022MessageRepository messageRepository;
    
    @InjectMocks
    private UetrCorrelationService uetrCorrelationService;
    
    @Test
    void shouldCorrelateMessagesByUetr() {
        // Given
        String uetr = "550e8400-e29b-41d4-a716-446655440000";
        List<Iso20022Message> messages = createIso20022Messages();
        when(messageRepository.findByUetr(uetr)).thenReturn(messages);
        
        // When
        UetrCorrelation correlation = uetrCorrelationService.getCorrelation(uetr);
        
        // Then
        assertThat(correlation.getUetr()).isEqualTo(uetr);
        assertThat(correlation.getMessages()).hasSize(3);
        assertThat(correlation.getPain001Message()).isNotNull();
        assertThat(correlation.getPacs008Message()).isNotNull();
        assertThat(correlation.getPacs002Message()).isNotNull();
    }
    
    @Test
    void shouldThrowExceptionWhenUetrNotFound() {
        // Given
        String uetr = "non-existent-uetr";
        when(messageRepository.findByUetr(uetr)).thenReturn(Collections.emptyList());
        
        // When & Then
        assertThatThrownBy(() -> uetrCorrelationService.getCorrelation(uetr))
            .isInstanceOf(UetrNotFoundException.class)
            .hasMessage("UETR not found: " + uetr);
    }
}
```

## 🔗 **Integration Testing**

### **API Integration Tests**
```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class PaymentInitiationIntegrationTest {
    
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:13")
            .withDatabaseName("payments_test")
            .withUsername("test")
            .withPassword("test");
    
    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis:6-alpine")
            .withExposedPorts(6379);
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    @Autowired
    private PaymentRepository paymentRepository;
    
    @Test
    void shouldInitiatePaymentViaApi() {
        // Given
        PaymentRequest request = PaymentRequest.builder()
            .paymentId("PAY-001")
            .amount(new BigDecimal("1000.00"))
            .currency("ZAR")
            .sourceAccount("ACC-001")
            .destinationAccount("ACC-002")
            .build();
            
        // When
        ResponseEntity<PaymentResponse> response = restTemplate.postForEntity(
            "/api/v1/payments",
            request,
            PaymentResponse.class
        );
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody().getPaymentId()).isEqualTo("PAY-001");
        assertThat(response.getBody().getUetr()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(PaymentStatus.PROCESSING);
        
        // Verify database
        Optional<Payment> savedPayment = paymentRepository.findByPaymentId("PAY-001");
        assertThat(savedPayment).isPresent();
        assertThat(savedPayment.get().getUetr()).isNotNull();
    }
    
    @Test
    void shouldReturnValidationErrorForInvalidRequest() {
        // Given
        PaymentRequest request = PaymentRequest.builder()
            .paymentId("") // Invalid: empty payment ID
            .amount(new BigDecimal("-100.00")) // Invalid: negative amount
            .currency("INVALID") // Invalid: not 3 characters
            .build();
            
        // When
        ResponseEntity<ErrorResponse> response = restTemplate.postForEntity(
            "/api/v1/payments",
            request,
            ErrorResponse.class
        );
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getErrors()).hasSize(3);
        assertThat(response.getBody().getErrors()).contains(
            "Payment ID is required",
            "Amount must be greater than 0",
            "Currency must be 3 characters"
        );
    }
}
```

### **Database Integration Tests**
```java
@DataJpaTest
@Testcontainers
class PaymentRepositoryIntegrationTest {
    
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:13")
            .withDatabaseName("payments_test")
            .withUsername("test")
            .withPassword("test");
    
    @Autowired
    private TestEntityManager entityManager;
    
    @Autowired
    private PaymentRepository paymentRepository;
    
    @Test
    void shouldFindPaymentByPaymentId() {
        // Given
        Payment payment = createPayment();
        entityManager.persistAndFlush(payment);
        
        // When
        Optional<Payment> found = paymentRepository.findByPaymentId("PAY-001");
        
        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getPaymentId()).isEqualTo("PAY-001");
        assertThat(found.get().getUetr()).isNotNull();
    }
    
    @Test
    void shouldFindPaymentsByTenantId() {
        // Given
        Payment payment1 = createPayment("PAY-001", "tenant-001");
        Payment payment2 = createPayment("PAY-002", "tenant-001");
        Payment payment3 = createPayment("PAY-003", "tenant-002");
        
        entityManager.persistAndFlush(payment1);
        entityManager.persistAndFlush(payment2);
        entityManager.persistAndFlush(payment3);
        
        // When
        List<Payment> tenantPayments = paymentRepository.findByTenantId("tenant-001");
        
        // Then
        assertThat(tenantPayments).hasSize(2);
        assertThat(tenantPayments).extracting(Payment::getPaymentId)
            .containsExactlyInAnyOrder("PAY-001", "PAY-002");
    }
    
    @Test
    void shouldFindPaymentsByUetr() {
        // Given
        String uetr = "550e8400-e29b-41d4-a716-446655440000";
        Payment payment = createPaymentWithUetr("PAY-001", uetr);
        entityManager.persistAndFlush(payment);
        
        // When
        Optional<Payment> found = paymentRepository.findByUetr(uetr);
        
        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getUetr()).isEqualTo(uetr);
    }
}
```

## 🎭 **End-to-End Testing**

### **Complete Payment Flow Tests**
```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class PaymentFlowE2ETest {
    
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:13")
            .withDatabaseName("payments_test")
            .withUsername("test")
            .withPassword("test");
    
    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis:6-alpine")
            .withExposedPorts(6379);
    
    @Container
    static GenericContainer<?> kafka = new GenericContainer<>("confluentinc/cp-kafka:latest")
            .withExposedPorts(9092)
            .withEnvironment("KAFKA_ZOOKEEPER_CONNECT", "zookeeper:2181")
            .withEnvironment("KAFKA_ADVERTISED_LISTENERS", "PLAINTEXT://localhost:9092");
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    @Autowired
    private PaymentRepository paymentRepository;
    
    @Autowired
    private Iso20022MessageRepository messageRepository;
    
    @Test
    void shouldProcessCompletePaymentFlow() {
        // Given
        PaymentRequest request = PaymentRequest.builder()
            .paymentId("PAY-E2E-001")
            .amount(new BigDecimal("1000.00"))
            .currency("ZAR")
            .sourceAccount("ACC-001")
            .destinationAccount("ACC-002")
            .build();
            
        // When - Step 1: Initiate Payment
        ResponseEntity<PaymentResponse> initiationResponse = restTemplate.postForEntity(
            "/api/v1/payments",
            request,
            PaymentResponse.class
        );
        
        // Then - Step 1: Payment Initiated
        assertThat(initiationResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        PaymentResponse paymentResponse = initiationResponse.getBody();
        assertThat(paymentResponse.getPaymentId()).isEqualTo("PAY-E2E-001");
        assertThat(paymentResponse.getUetr()).isNotNull();
        assertThat(paymentResponse.getStatus()).isEqualTo(PaymentStatus.PROCESSING);
        
        // When - Step 2: Check Payment Status
        ResponseEntity<PaymentStatusResponse> statusResponse = restTemplate.getForEntity(
            "/api/v1/payments/" + paymentResponse.getPaymentId() + "/status",
            PaymentStatusResponse.class
        );
        
        // Then - Step 2: Payment Status Retrieved
        assertThat(statusResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        PaymentStatusResponse statusResponseBody = statusResponse.getBody();
        assertThat(statusResponseBody.getPaymentId()).isEqualTo("PAY-E2E-001");
        assertThat(statusResponseBody.getUetr()).isEqualTo(paymentResponse.getUetr());
        
        // When - Step 3: Check ISO 20022 Messages
        ResponseEntity<Iso20022MessageListResponse> messagesResponse = restTemplate.getForEntity(
            "/api/v1/payments/" + paymentResponse.getPaymentId() + "/iso20022-messages",
            Iso20022MessageListResponse.class
        );
        
        // Then - Step 3: ISO 20022 Messages Retrieved
        assertThat(messagesResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        Iso20022MessageListResponse messagesResponseBody = messagesResponse.getBody();
        assertThat(messagesResponseBody.getMessages()).isNotEmpty();
        assertThat(messagesResponseBody.getMessages()).extracting(Iso20022Message::getMessageType)
            .contains("pain.001");
        
        // Verify Database State
        Optional<Payment> savedPayment = paymentRepository.findByPaymentId("PAY-E2E-001");
        assertThat(savedPayment).isPresent();
        assertThat(savedPayment.get().getUetr()).isEqualTo(paymentResponse.getUetr());
        
        List<Iso20022Message> savedMessages = messageRepository.findByUetr(paymentResponse.getUetr());
        assertThat(savedMessages).isNotEmpty();
    }
}
```

### **ISO 20022 Message Flow Tests**
```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class Iso20022MessageFlowE2ETest {
    
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:13")
            .withDatabaseName("payments_test")
            .withUsername("test")
            .withPassword("test");
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    @Autowired
    private Iso20022MessageRepository messageRepository;
    
    @Test
    void shouldProcessPain001ToPacs008Flow() {
        // Given
        Pain001Message pain001Message = createPain001Message();
        
        // When - Step 1: Send pain.001
        ResponseEntity<Pain001Response> pain001Response = restTemplate.postForEntity(
            "/api/v1/iso20022/pain001",
            pain001Message,
            Pain001Response.class
        );
        
        // Then - Step 1: pain.001 Processed
        assertThat(pain001Response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        Pain001Response pain001ResponseBody = pain001Response.getBody();
        assertThat(pain001ResponseBody.getMessageId()).isNotNull();
        assertThat(pain001ResponseBody.getUetr()).isNotNull();
        assertThat(pain001ResponseBody.getStatus()).isEqualTo(MessageStatus.VALIDATED);
        
        // When - Step 2: Check Generated pacs.008
        ResponseEntity<Pacs008Response> pacs008Response = restTemplate.getForEntity(
            "/api/v1/iso20022/pacs008/" + pain001ResponseBody.getUetr(),
            Pacs008Response.class
        );
        
        // Then - Step 2: pacs.008 Generated
        assertThat(pacs008Response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Pacs008Response pacs008ResponseBody = pacs008Response.getBody();
        assertThat(pacs008ResponseBody.getMessageId()).isNotNull();
        assertThat(pacs008ResponseBody.getUetr()).isEqualTo(pain001ResponseBody.getUetr());
        assertThat(pacs008ResponseBody.getStatus()).isEqualTo(MessageStatus.PROCESSED);
        
        // Verify Database State
        List<Iso20022Message> messages = messageRepository.findByUetr(pain001ResponseBody.getUetr());
        assertThat(messages).hasSize(2);
        assertThat(messages).extracting(Iso20022Message::getMessageType)
            .containsExactlyInAnyOrder("pain.001", "pacs.008");
    }
}
```

## 🚀 **Performance Testing**

### **Load Testing**
```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class PaymentPerformanceTest {
    
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:13")
            .withDatabaseName("payments_test")
            .withUsername("test")
            .withPassword("test");
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    @Test
    void shouldHandleConcurrentPaymentInitiation() throws InterruptedException {
        // Given
        int numberOfThreads = 10;
        int paymentsPerThread = 100;
        CountDownLatch latch = new CountDownLatch(numberOfThreads);
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        
        // When
        for (int i = 0; i < numberOfThreads; i++) {
            final int threadId = i;
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                try {
                    for (int j = 0; j < paymentsPerThread; j++) {
                        PaymentRequest request = PaymentRequest.builder()
                            .paymentId("PAY-" + threadId + "-" + j)
                            .amount(new BigDecimal("100.00"))
                            .currency("ZAR")
                            .sourceAccount("ACC-001")
                            .destinationAccount("ACC-002")
                            .build();
                            
                        ResponseEntity<PaymentResponse> response = restTemplate.postForEntity(
                            "/api/v1/payments",
                            request,
                            PaymentResponse.class
                        );
                        
                        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
                    }
                } finally {
                    latch.countDown();
                }
            });
            futures.add(future);
        }
        
        // Wait for all threads to complete
        latch.await(60, TimeUnit.SECONDS);
        
        // Then
        for (CompletableFuture<Void> future : futures) {
            assertThat(future.isDone()).isTrue();
            assertThat(future.isCompletedExceptionally()).isFalse();
        }
    }
    
    @Test
    void shouldMeetPerformanceRequirements() {
        // Given
        int numberOfPayments = 1000;
        long startTime = System.currentTimeMillis();
        
        // When
        for (int i = 0; i < numberOfPayments; i++) {
            PaymentRequest request = PaymentRequest.builder()
                .paymentId("PAY-PERF-" + i)
                .amount(new BigDecimal("100.00"))
                .currency("ZAR")
                .sourceAccount("ACC-001")
                .destinationAccount("ACC-002")
                .build();
                
            ResponseEntity<PaymentResponse> response = restTemplate.postForEntity(
                "/api/v1/payments",
                request,
                PaymentResponse.class
            );
            
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        }
        
        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;
        double paymentsPerSecond = (double) numberOfPayments / (totalTime / 1000.0);
        
        // Then
        assertThat(paymentsPerSecond).isGreaterThan(100.0); // 100 payments per second minimum
        assertThat(totalTime).isLessThan(10000); // 10 seconds maximum
    }
}
```

## 📊 **Test Configuration**

### **Test Profiles**
```yaml
# application-test.yml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/payments_test
    username: test
    password: test
  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: false
  redis:
    host: localhost
    port: 6379
    database: 0

logging:
  level:
    com.payments: DEBUG
    org.springframework.web: DEBUG
    org.testcontainers: INFO

test:
  containers:
    postgres:
      image: postgres:13
      database: payments_test
      username: test
      password: test
    redis:
      image: redis:6-alpine
      port: 6379
    kafka:
      image: confluentinc/cp-kafka:latest
      port: 9092
```

### **Test Data Builders**
```java
public class PaymentTestDataBuilder {
    
    public static PaymentRequest.PaymentRequestBuilder paymentRequest() {
        return PaymentRequest.builder()
            .paymentId("PAY-001")
            .amount(new BigDecimal("1000.00"))
            .currency("ZAR")
            .sourceAccount("ACC-001")
            .destinationAccount("ACC-002");
    }
    
    public static Payment.PaymentBuilder payment() {
        return Payment.builder()
            .paymentId("PAY-001")
            .uetr("550e8400-e29b-41d4-a716-446655440000")
            .amount(new BigDecimal("1000.00"))
            .currency("ZAR")
            .status(PaymentStatus.PROCESSING)
            .tenantId("tenant-001");
    }
    
    public static Iso20022Message.Iso20022MessageBuilder iso20022Message() {
        return Iso20022Message.builder()
            .messageId("MSG-001")
            .messageType("pain.001")
            .uetr("550e8400-e29b-41d4-a716-446655440000")
            .status(MessageStatus.VALIDATED)
            .tenantId("tenant-001");
    }
}
```

---

**Version**: 2.0  
**Last Updated**: 2025-01-27  
**Status**: 🚀 Ready for Implementation  
**Next Review**: Weekly during implementation
