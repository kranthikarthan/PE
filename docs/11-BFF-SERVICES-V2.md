# BFF Services v2 - Enhanced Backend for Frontend Architecture

## 🎯 **BFF Architecture Overview**

The Payments Engine v2 implements a comprehensive Backend for Frontend (BFF) architecture optimized for different client types with ISO 20022 compliance and UETR correlation.

## 🏗️ **BFF Service Architecture**

### **3 BFF Services for Different Client Types**

#### **1. Web BFF - GraphQL (2 days)**
```yaml
Service Name: Web BFF - GraphQL
AI Agent: WebBFFAgent
Duration: 2 days
Technology: GraphQL, Apollo Server, Spring Boot
Optimization: Web applications, flexible queries, caching
```

#### **2. Mobile BFF - REST (1.5 days)**
```yaml
Service Name: Mobile BFF - REST
AI Agent: MobileBFFAgent
Duration: 1.5 days
Technology: REST API, Spring Boot, lightweight
Optimization: Mobile networks, reduced payload, offline support
```

#### **3. Partner BFF - REST (1.5 days)**
```yaml
Service Name: Partner BFF - REST
AI Agent: PartnerBFFAgent
Duration: 1.5 days
Technology: REST API, Spring Boot, comprehensive
Optimization: Partner integration, full feature set, rate limiting
```

## 🔧 **Web BFF - GraphQL Service**

### **Service Architecture**
```yaml
Service Name: Web BFF - GraphQL
Phase: Phase 4 (Advanced Features)
AI Agent: WebBFFAgent
Duration: 2 days
Database: Redis (caching)
Dependencies: All core services
```

### **GraphQL Schema**
```graphql
# ISO 20022 Payment Types
type Payment {
  id: ID!
  uetr: String!
  status: PaymentStatus!
  amount: Money!
  sourceAccount: String!
  destinationAccount: String!
  paymentType: PaymentType!
  priority: Priority!
  createdAt: DateTime!
  updatedAt: DateTime!
  iso20022Messages: [Iso20022Message!]!
}

type Iso20022Message {
  messageId: String!
  messageType: String!
  uetr: String!
  status: MessageStatus!
  rawXml: String
  parsedJson: String
  createdAt: DateTime!
}

type Money {
  amount: Float!
  currency: String!
}

enum PaymentStatus {
  PENDING
  PROCESSING
  COMPLETED
  FAILED
  CANCELLED
}

enum PaymentType {
  EFT
  RTC
  RTGS
  INTERNATIONAL
}

enum Priority {
  NORMAL
  HIGH
  URGENT
}

enum MessageStatus {
  PENDING
  VALIDATED
  PROCESSED
  FAILED
}

# Queries
type Query {
  # Payment queries
  payment(id: ID!): Payment
  payments(
    status: PaymentStatus
    fromDate: DateTime
    toDate: DateTime
    limit: Int
    offset: Int
  ): [Payment!]!
  
  # ISO 20022 message queries
  iso20022Messages(uetr: String!): [Iso20022Message!]!
  messageCorrelation(uetr: String!): MessageCorrelation
  
  # Tenant queries
  tenantInfo: TenantInfo!
  tenantConfig: TenantConfig!
}

# Mutations
type Mutation {
  # Payment mutations
  initiatePayment(input: PaymentInitiationInput!): PaymentInitiationResult!
  cancelPayment(id: ID!): PaymentCancellationResult!
  
  # ISO 20022 message mutations
  sendPain001(input: Pain001Input!): Pain001Result!
  requestPain002(uetr: String!): Pain002Result!
}

# Subscriptions
type Subscription {
  paymentStatusChanged(paymentId: ID!): Payment!
  iso20022MessageProcessed(uetr: String!): Iso20022Message!
}
```

### **GraphQL Resolvers**
```java
@Component
public class PaymentResolver implements GraphQLQueryResolver {
    
    @Autowired
    private PaymentService paymentService;
    
    @Autowired
    private Iso20022MessageService iso20022MessageService;
    
    public Payment payment(String id) {
        return paymentService.getPayment(id);
    }
    
    public List<Payment> payments(PaymentStatus status, DateTime fromDate, 
                                 DateTime toDate, Integer limit, Integer offset) {
        return paymentService.getPayments(status, fromDate, toDate, limit, offset);
    }
    
    public List<Iso20022Message> iso20022Messages(String uetr) {
        return iso20022MessageService.getMessagesByUetr(uetr);
    }
    
    public MessageCorrelation messageCorrelation(String uetr) {
        return iso20022MessageService.getMessageCorrelation(uetr);
    }
}

@Component
public class PaymentMutationResolver implements GraphQLMutationResolver {
    
    @Autowired
    private PaymentInitiationService paymentInitiationService;
    
    @Autowired
    private Iso20022MessageService iso20022MessageService;
    
    public PaymentInitiationResult initiatePayment(PaymentInitiationInput input) {
        return paymentInitiationService.initiatePayment(input);
    }
    
    public Pain001Result sendPain001(Pain001Input input) {
        return iso20022MessageService.sendPain001(input);
    }
    
    public Pain002Result requestPain002(String uetr) {
        return iso20022MessageService.requestPain002(uetr);
    }
}

@Component
public class PaymentSubscriptionResolver implements GraphQLSubscriptionResolver {
    
    @Autowired
    private PaymentEventService paymentEventService;
    
    public Publisher<Payment> paymentStatusChanged(String paymentId) {
        return paymentEventService.getPaymentStatusStream(paymentId);
    }
    
    public Publisher<Iso20022Message> iso20022MessageProcessed(String uetr) {
        return paymentEventService.getIso20022MessageStream(uetr);
    }
}
```

### **GraphQL Configuration**
```java
@Configuration
@EnableGraphQL
public class GraphQLConfig {
    
    @Bean
    public GraphQLSchema graphQLSchema() {
        return GraphQLSchema.newSchema()
            .query(QueryType.newQueryType()
                .name("Query")
                .field(FieldDefinition.newFieldDefinition()
                    .name("payment")
                    .type(new GraphQLObjectType("Payment", "Payment details", 
                        Arrays.asList(
                            new GraphQLFieldDefinition("id", "Payment ID", new GraphQLNonNull(new GraphQLID())),
                            new GraphQLFieldDefinition("uetr", "UETR", new GraphQLNonNull(new GraphQLString())),
                            new GraphQLFieldDefinition("status", "Payment Status", new GraphQLNonNull(PaymentStatusType.PAYMENT_STATUS)),
                            new GraphQLFieldDefinition("amount", "Payment Amount", new GraphQLNonNull(MoneyType.MONEY))
                        ), null))
                    .argument(Argument.newArgument()
                        .name("id")
                        .type(new GraphQLNonNull(new GraphQLID())))
                    .dataFetcher(environment -> {
                        String id = environment.getArgument("id");
                        return paymentService.getPayment(id);
                    })
                    .build())
                .build())
            .mutation(MutationType.newMutationType()
                .name("Mutation")
                .field(FieldDefinition.newFieldDefinition()
                    .name("initiatePayment")
                    .type(PaymentInitiationResultType.PAYMENT_INITIATION_RESULT)
                    .argument(Argument.newArgument()
                        .name("input")
                        .type(new GraphQLNonNull(PaymentInitiationInputType.PAYMENT_INITIATION_INPUT)))
                    .dataFetcher(environment -> {
                        PaymentInitiationInput input = environment.getArgument("input");
                        return paymentInitiationService.initiatePayment(input);
                    })
                    .build())
                .build())
            .subscription(SubscriptionType.newSubscriptionType()
                .name("Subscription")
                .field(FieldDefinition.newFieldDefinition()
                    .name("paymentStatusChanged")
                    .type(new GraphQLNonNull(PaymentType.PAYMENT))
                    .argument(Argument.newArgument()
                        .name("paymentId")
                        .type(new GraphQLNonNull(new GraphQLID())))
                    .dataFetcher(environment -> {
                        String paymentId = environment.getArgument("paymentId");
                        return paymentEventService.getPaymentStatusStream(paymentId);
                    })
                    .build())
                .build())
            .build();
    }
}
```

## 📱 **Mobile BFF - REST Service**

### **Service Architecture**
```yaml
Service Name: Mobile BFF - REST
Phase: Phase 4 (Advanced Features)
AI Agent: MobileBFFAgent
Duration: 1.5 days
Database: Redis (caching)
Dependencies: All core services
```

### **REST API Endpoints**
```java
@RestController
@RequestMapping("/api/v1/mobile")
public class MobileBFFController {
    
    @Autowired
    private PaymentService paymentService;
    
    @Autowired
    private Iso20022MessageService iso20022MessageService;
    
    // Lightweight payment initiation
    @PostMapping("/payments")
    public ResponseEntity<MobilePaymentResponse> initiatePayment(
        @RequestBody @Valid MobilePaymentRequest request
    ) {
        MobilePaymentResponse response = paymentService.initiateMobilePayment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    // Optimized payment status
    @GetMapping("/payments/{id}")
    public ResponseEntity<MobilePaymentStatus> getPaymentStatus(@PathVariable String id) {
        MobilePaymentStatus status = paymentService.getMobilePaymentStatus(id);
        return ResponseEntity.ok(status);
    }
    
    // Lightweight payment list
    @GetMapping("/payments")
    public ResponseEntity<MobilePaymentList> getPayments(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size,
        @RequestParam(required = false) String status
    ) {
        MobilePaymentList payments = paymentService.getMobilePayments(page, size, status);
        return ResponseEntity.ok(payments);
    }
    
    // ISO 20022 message status
    @GetMapping("/payments/{id}/messages")
    public ResponseEntity<MobileIso20022Status> getIso20022Status(@PathVariable String id) {
        MobileIso20022Status status = iso20022MessageService.getMobileIso20022Status(id);
        return ResponseEntity.ok(status);
    }
    
    // UETR lookup
    @GetMapping("/uetr/{uetr}")
    public ResponseEntity<MobileUetrInfo> getUetrInfo(@PathVariable String uetr) {
        MobileUetrInfo info = iso20022MessageService.getMobileUetrInfo(uetr);
        return ResponseEntity.ok(info);
    }
}
```

### **Mobile-Optimized DTOs**
```java
// Lightweight payment request
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MobilePaymentRequest {
    @NotNull
    private String sourceAccount;
    
    @NotNull
    private String destinationAccount;
    
    @NotNull
    @DecimalMin("0.01")
    private BigDecimal amount;
    
    @NotNull
    @Size(min = 3, max = 3)
    private String currency;
    
    @NotNull
    private PaymentType paymentType;
    
    private String reference;
    
    // Mobile-specific fields
    private String deviceId;
    private String location;
    private String biometricToken;
}

// Optimized payment response
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MobilePaymentResponse {
    private String paymentId;
    private String uetr;
    private PaymentStatus status;
    private BigDecimal amount;
    private String currency;
    private Instant createdAt;
    private String qrCode; // For mobile payments
    private String deepLink; // For mobile apps
}

// Lightweight payment status
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MobilePaymentStatus {
    private String paymentId;
    private String uetr;
    private PaymentStatus status;
    private String statusMessage;
    private BigDecimal amount;
    private String currency;
    private Instant updatedAt;
    private boolean isCompleted;
    private String nextAction; // For mobile UX
}

// Mobile-optimized ISO 20022 status
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MobileIso20022Status {
    private String uetr;
    private String pain001Status;
    private String pacs008Status;
    private String pacs002Status;
    private String pain002Status;
    private Instant lastUpdated;
    private String overallStatus;
}
```

### **Mobile Optimization Features**
```java
@Service
public class MobileOptimizationService {
    
    // Response compression
    @Bean
    public FilterRegistrationBean<GzipFilter> gzipFilter() {
        FilterRegistrationBean<GzipFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new GzipFilter());
        registrationBean.addUrlPatterns("/api/v1/mobile/*");
        return registrationBean;
    }
    
    // Caching for mobile
    @Cacheable(value = "mobile-payment-status", key = "#paymentId")
    public MobilePaymentStatus getCachedPaymentStatus(String paymentId) {
        return paymentService.getMobilePaymentStatus(paymentId);
    }
    
    // Offline support
    public MobileOfflineData getOfflineData(String userId) {
        return MobileOfflineData.builder()
            .recentPayments(paymentService.getRecentPayments(userId, 10))
            .favoriteAccounts(accountService.getFavoriteAccounts(userId))
            .paymentTemplates(paymentService.getPaymentTemplates(userId))
            .build();
    }
    
    // Push notifications
    public void sendPushNotification(String userId, String paymentId, String status) {
        PushNotification notification = PushNotification.builder()
            .userId(userId)
            .title("Payment Update")
            .message("Payment " + paymentId + " is now " + status)
            .data(Map.of("paymentId", paymentId, "status", status))
            .build();
            
        pushNotificationService.send(notification);
    }
}
```

## 🤝 **Partner BFF - REST Service**

### **Service Architecture**
```yaml
Service Name: Partner BFF - REST
Phase: Phase 4 (Advanced Features)
AI Agent: PartnerBFFAgent
Duration: 1.5 days
Database: Redis (caching)
Dependencies: All core services
```

### **Partner API Endpoints**
```java
@RestController
@RequestMapping("/api/v1/partner")
@PreAuthorize("hasRole('PARTNER')")
public class PartnerBFFController {
    
    @Autowired
    private PaymentService paymentService;
    
    @Autowired
    private Iso20022MessageService iso20022MessageService;
    
    // Comprehensive payment initiation
    @PostMapping("/payments")
    public ResponseEntity<PartnerPaymentResponse> initiatePayment(
        @RequestBody @Valid PartnerPaymentRequest request
    ) {
        PartnerPaymentResponse response = paymentService.initiatePartnerPayment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    // Detailed payment information
    @GetMapping("/payments/{id}")
    public ResponseEntity<PartnerPaymentDetails> getPaymentDetails(@PathVariable String id) {
        PartnerPaymentDetails details = paymentService.getPartnerPaymentDetails(id);
        return ResponseEntity.ok(details);
    }
    
    // Comprehensive payment list with filtering
    @GetMapping("/payments")
    public ResponseEntity<PartnerPaymentList> getPayments(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "50") int size,
        @RequestParam(required = false) String status,
        @RequestParam(required = false) String fromDate,
        @RequestParam(required = false) String toDate,
        @RequestParam(required = false) String uetr
    ) {
        PartnerPaymentList payments = paymentService.getPartnerPayments(
            page, size, status, fromDate, toDate, uetr);
        return ResponseEntity.ok(payments);
    }
    
    // ISO 20022 message management
    @GetMapping("/payments/{id}/iso20022")
    public ResponseEntity<PartnerIso20022Details> getIso20022Details(@PathVariable String id) {
        PartnerIso20022Details details = iso20022MessageService.getPartnerIso20022Details(id);
        return ResponseEntity.ok(details);
    }
    
    // UETR correlation details
    @GetMapping("/uetr/{uetr}/correlation")
    public ResponseEntity<PartnerUetrCorrelation> getUetrCorrelation(@PathVariable String uetr) {
        PartnerUetrCorrelation correlation = iso20022MessageService.getPartnerUetrCorrelation(uetr);
        return ResponseEntity.ok(correlation);
    }
    
    // Batch payment processing
    @PostMapping("/payments/batch")
    public ResponseEntity<PartnerBatchResponse> processBatchPayments(
        @RequestBody @Valid PartnerBatchRequest request
    ) {
        PartnerBatchResponse response = paymentService.processPartnerBatchPayments(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    // Payment repair
    @PostMapping("/payments/{id}/repair")
    public ResponseEntity<PartnerRepairResponse> repairPayment(
        @PathVariable String id,
        @RequestBody @Valid PartnerRepairRequest request
    ) {
        PartnerRepairResponse response = paymentService.repairPartnerPayment(id, request);
        return ResponseEntity.ok(response);
    }
}
```

### **Partner-Optimized DTOs**
```java
// Comprehensive payment request
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PartnerPaymentRequest {
    @NotNull
    private String sourceAccount;
    
    @NotNull
    private String destinationAccount;
    
    @NotNull
    @DecimalMin("0.01")
    private BigDecimal amount;
    
    @NotNull
    @Size(min = 3, max = 3)
    private String currency;
    
    @NotNull
    private PaymentType paymentType;
    
    private String reference;
    private String description;
    private String customerReference;
    private String bankReference;
    
    // Partner-specific fields
    private String partnerId;
    private String partnerReference;
    private Map<String, Object> partnerMetadata;
    private String callbackUrl;
    private String webhookUrl;
    
    // ISO 20022 specific fields
    private String uetr;
    private String instructionId;
    private String endToEndId;
    private String transactionId;
}

// Detailed payment response
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PartnerPaymentResponse {
    private String paymentId;
    private String uetr;
    private PaymentStatus status;
    private BigDecimal amount;
    private String currency;
    private Instant createdAt;
    private Instant updatedAt;
    
    // ISO 20022 message IDs
    private String pain001MessageId;
    private String pacs008MessageId;
    private String pacs002MessageId;
    private String pain002MessageId;
    
    // Partner-specific response
    private String partnerReference;
    private String callbackUrl;
    private String webhookUrl;
    private Map<String, Object> partnerMetadata;
}

// Comprehensive payment details
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PartnerPaymentDetails {
    private String paymentId;
    private String uetr;
    private PaymentStatus status;
    private BigDecimal amount;
    private String currency;
    private String sourceAccount;
    private String destinationAccount;
    private String reference;
    private String description;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant completedAt;
    
    // ISO 20022 message details
    private List<PartnerIso20022Message> iso20022Messages;
    
    // Partner-specific details
    private String partnerId;
    private String partnerReference;
    private Map<String, Object> partnerMetadata;
    private String callbackUrl;
    private String webhookUrl;
}

// ISO 20022 message details for partners
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PartnerIso20022Message {
    private String messageId;
    private String messageType;
    private String status;
    private String rawXml;
    private String parsedJson;
    private Instant createdAt;
    private String validationStatus;
    private String processingStatus;
    private String errorMessage;
}
```

### **Partner Rate Limiting**
```java
@Configuration
public class PartnerRateLimitingConfig {
    
    @Bean
    public RateLimiter partnerRateLimiter() {
        return RateLimiter.create(100.0); // 100 requests per second
    }
    
    @Bean
    public FilterRegistrationBean<RateLimitingFilter> rateLimitingFilter() {
        FilterRegistrationBean<RateLimitingFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new RateLimitingFilter(partnerRateLimiter()));
        registrationBean.addUrlPatterns("/api/v1/partner/*");
        return registrationBean;
    }
}

@Component
public class PartnerRateLimitingFilter implements Filter {
    
    private final RateLimiter rateLimiter;
    
    public PartnerRateLimitingFilter(RateLimiter rateLimiter) {
        this.rateLimiter = rateLimiter;
    }
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, 
                        FilterChain filterChain) throws IOException, ServletException {
        
        if (rateLimiter.tryAcquire()) {
            filterChain.doFilter(request, response);
        } else {
            HttpServletResponse httpResponse = (HttpServletResponse) response;
            httpResponse.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            httpResponse.getWriter().write("Rate limit exceeded");
        }
    }
}
```

## 🔄 **BFF Event Integration**

### **Event-Driven BFF Updates**
```java
@Component
public class BFFEventIntegration {
    
    @Autowired
    private WebSocketService webSocketService;
    
    @Autowired
    private PushNotificationService pushNotificationService;
    
    @Autowired
    private WebhookService webhookService;
    
    @EventListener
    public void onPaymentStatusChanged(PaymentStatusChangedEvent event) {
        // Update GraphQL subscriptions
        webSocketService.broadcastPaymentUpdate(event.getPaymentId(), event.getStatus());
        
        // Send push notifications for mobile
        pushNotificationService.sendPaymentUpdate(event.getPaymentId(), event.getStatus());
        
        // Send webhooks for partners
        webhookService.sendPaymentWebhook(event.getPaymentId(), event.getStatus());
    }
    
    @EventListener
    public void onIso20022MessageProcessed(Iso20022MessageProcessedEvent event) {
        // Update all BFF services
        webSocketService.broadcastIso20022Update(event.getUetr(), event.getMessageType());
        pushNotificationService.sendIso20022Update(event.getUetr(), event.getMessageType());
        webhookService.sendIso20022Webhook(event.getUetr(), event.getMessageType());
    }
}
```

## 📊 **BFF Performance Optimization**

### **Caching Strategy**
```java
@Service
public class BFFCachingService {
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    // Web BFF caching
    @Cacheable(value = "web-payment-details", key = "#paymentId")
    public Payment getCachedPayment(String paymentId) {
        return paymentService.getPayment(paymentId);
    }
    
    // Mobile BFF caching
    @Cacheable(value = "mobile-payment-status", key = "#paymentId")
    public MobilePaymentStatus getCachedMobileStatus(String paymentId) {
        return paymentService.getMobilePaymentStatus(paymentId);
    }
    
    // Partner BFF caching
    @Cacheable(value = "partner-payment-details", key = "#paymentId")
    public PartnerPaymentDetails getCachedPartnerDetails(String paymentId) {
        return paymentService.getPartnerPaymentDetails(paymentId);
    }
    
    // UETR correlation caching
    @Cacheable(value = "uetr-correlation", key = "#uetr")
    public UetrCorrelation getCachedUetrCorrelation(String uetr) {
        return iso20022MessageService.getUetrCorrelation(uetr);
    }
}
```

### **Response Optimization**
```java
@Configuration
public class BFFOptimizationConfig {
    
    // Response compression
    @Bean
    public FilterRegistrationBean<GzipFilter> gzipFilter() {
        FilterRegistrationBean<GzipFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new GzipFilter());
        registrationBean.addUrlPatterns("/api/v1/*");
        return registrationBean;
    }
    
    // Connection pooling
    @Bean
    public RestTemplate restTemplate() {
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        factory.setConnectTimeout(5000);
        factory.setReadTimeout(10000);
        factory.setConnectionRequestTimeout(5000);
        return new RestTemplate(factory);
    }
    
    // Async processing
    @Bean
    public TaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(20);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("BFF-");
        executor.initialize();
        return executor;
    }
}
```

---

**Version**: 2.0  
**Last Updated**: 2025-01-27  
**Status**: 🚀 Ready for Implementation  
**Next Review**: Weekly during implementation
