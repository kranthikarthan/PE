# Backend for Frontend (BFF) Pattern - Implementation Guide

## Overview

This document provides the **complete Backend for Frontend (BFF) implementation** for the Payments Engine. The BFF pattern creates separate API gateways optimized for each client type: Web Portal, Mobile App, and Partner APIs.

---

## BFF Architecture

```
┌────────────────────────────────────────────────────────────────┐
│                         CLIENTS                                 │
├───────────────┬───────────────────┬───────────────────────────┤
│  Web Portal   │    Mobile App     │     Partner Bank APIs     │
│  (Desktop)    │  (iOS, Android)   │   (B2B Integration)      │
└───────┬───────┴────────┬──────────┴────────┬──────────────────┘
        │                │                    │
        │ GraphQL        │ REST (lightweight) │ REST (comprehensive)
        │                │                    │
        ▼                ▼                    ▼
┌──────────────┐ ┌──────────────┐  ┌──────────────────────────┐
│   Web BFF    │ │  Mobile BFF  │  │     Partner API BFF      │
│              │ │              │  │                          │
│ Port: 8090   │ │ Port: 8091   │  │      Port: 8092          │
│ Protocol:    │ │ Protocol:    │  │      Protocol:           │
│ - GraphQL    │ │ - REST       │  │      - REST              │
│              │ │ - Smaller    │  │      - OAuth 2.0         │
│ Features:    │ │   payloads   │  │      - Comprehensive     │
│ - Rich       │ │ - Optimized  │  │      - Bulk operations   │
│   queries    │ │   for mobile │  │      - Webhooks          │
│ - Aggregated │ │ - Image      │  │                          │
│   data       │ │   URLs       │  │                          │
│ - Multiple   │ │ - Push       │  │                          │
│   entities   │ │   tokens     │  │                          │
└──────┬───────┘ └──────┬───────┘  └──────────┬───────────────┘
       │                │                      │
       └────────────────┴──────────────────────┘
                        │
       ┌────────────────┴────────────────┐
       │      Core Services (17)          │
       │  - Payment Initiation            │
       │  - Validation                    │
       │  - Account Adapter               │
       │  - Notification                  │
       │  - etc.                          │
       └──────────────────────────────────┘
```

---

## Why BFF Pattern?

### Without BFF (Current)

```
Mobile App → Generic API Gateway → Multiple Backend Calls
Problem:
- Mobile gets same large payloads as web
- Multiple round-trips (slow on mobile networks)
- No mobile-specific optimizations
- Authentication same for all clients
```

### With BFF

```
Mobile App → Mobile BFF → Backend Calls (aggregated)
Benefits:
- Smaller payloads tailored for mobile
- Single request returns aggregated data
- Mobile-specific features (push tokens, image optimization)
- Different auth per client type
```

---

## 1. Web BFF (GraphQL)

### Technology Stack
- **Language**: Java 17, Spring Boot 3.x
- **Protocol**: GraphQL (Spring for GraphQL)
- **Port**: 8090
- **Client**: React Web Portal

### Why GraphQL for Web?

| Feature | Benefit |
|---------|---------|
| **Flexible Queries** | Frontend requests exactly what it needs |
| **Single Request** | Reduce round-trips |
| **Type Safety** | Schema provides contract |
| **Developer Experience** | GraphQL Playground for testing |
| **Versioning** | No API versioning needed |

### Implementation

#### GraphQL Schema

```graphql
# schema.graphqls

type Query {
    # Dashboard queries
    paymentDashboard(customerId: ID!): PaymentDashboard!
    
    # Individual queries
    payment(paymentId: ID!): Payment
    payments(
        filter: PaymentFilter
        pagination: Pagination
    ): PaymentConnection!
    
    # Account queries
    accounts(customerId: ID!): [Account!]!
    accountBalance(accountNumber: String!): AccountBalance!
    
    # Tenant queries
    tenantConfig(configKey: String!): TenantConfig
}

type Mutation {
    # Payment mutations
    initiatePayment(input: InitiatePaymentInput!): PaymentResponse!
    cancelPayment(paymentId: ID!): PaymentResponse!
    
    # Notification mutations
    markNotificationAsRead(notificationId: ID!): Boolean!
}

type Subscription {
    # Real-time updates
    paymentUpdated(customerId: ID!): Payment!
    notificationReceived(customerId: ID!): Notification!
}

# ============================================================================
# Complex Types
# ============================================================================

"""
Payment Dashboard - Aggregated data for web portal
"""
type PaymentDashboard {
    summary: PaymentSummary!
    recentPayments: [Payment!]!
    accounts: [Account!]!
    notifications: [Notification!]!
    limits: LimitSummary!
}

type PaymentSummary {
    totalPaymentsToday: Int!
    totalVolumeToday: Money!
    pendingPayments: Int!
    failedPayments: Int!
}

type Payment {
    id: ID!
    tenantContext: TenantContext!
    sourceAccount: String!
    destinationAccount: String!
    amount: Money!
    reference: String!
    paymentType: PaymentType!
    status: PaymentStatus!
    initiatedAt: DateTime!
    completedAt: DateTime
    statusHistory: [StatusChange!]!
}

type Account {
    accountNumber: String!
    accountType: String!
    balance: Money!
    availableBalance: Money!
    status: AccountStatus!
}

type Notification {
    id: ID!
    type: NotificationType!
    title: String!
    message: String!
    read: Boolean!
    createdAt: DateTime!
}

type LimitSummary {
    dailyLimit: Money!
    dailyUsed: Money!
    monthlyLimit: Money!
    monthlyUsed: Money!
}

# ============================================================================
# Input Types
# ============================================================================

input InitiatePaymentInput {
    sourceAccount: String!
    destinationAccount: String!
    amount: Decimal!
    currency: String!
    reference: String!
    paymentType: PaymentType!
    priority: Priority!
}

input PaymentFilter {
    status: PaymentStatus
    paymentType: PaymentType
    fromDate: DateTime
    toDate: DateTime
}

input Pagination {
    page: Int!
    size: Int!
}

# ============================================================================
# Enums
# ============================================================================

enum PaymentStatus {
    INITIATED
    VALIDATED
    CLEARING
    CLEARED
    COMPLETED
    FAILED
}

enum PaymentType {
    EFT
    RTC
    RTGS
    DEBIT_ORDER
}

# ============================================================================
# Scalar Types
# ============================================================================

scalar DateTime
scalar Decimal

type Money {
    amount: Decimal!
    currency: String!
}
```

#### GraphQL Controllers

```java
package com.payments.bff.web;

import org.springframework.graphql.data.method.annotation.*;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Web BFF - GraphQL Controller
 * 
 * Aggregates data from multiple backend services
 * Optimized for web portal
 */
@Controller
@Slf4j
public class WebBFFController {
    
    @Autowired
    private PaymentService paymentService;
    
    @Autowired
    private AccountService accountService;
    
    @Autowired
    private NotificationService notificationService;
    
    @Autowired
    private LimitService limitService;
    
    // ────────────────────────────────────────────────────────────
    // QUERIES (Aggregated data for web portal)
    // ────────────────────────────────────────────────────────────
    
    /**
     * Dashboard Query - Aggregates data from multiple services
     * 
     * This is the key benefit of BFF: Single request returns all dashboard data
     */
    @QueryMapping
    public Mono<PaymentDashboard> paymentDashboard(@Argument String customerId) {
        log.info("Fetching payment dashboard for customer: {}", customerId);
        
        // Fetch data from multiple services IN PARALLEL
        Mono<PaymentSummary> summaryMono = paymentService.getSummary(customerId);
        Mono<List<Payment>> recentPaymentsMono = paymentService.getRecentPayments(customerId, 10);
        Mono<List<Account>> accountsMono = accountService.getAccounts(customerId);
        Mono<List<Notification>> notificationsMono = notificationService.getUnread(customerId, 5);
        Mono<LimitSummary> limitsMono = limitService.getLimitSummary(customerId);
        
        // Wait for all requests to complete, then combine
        return Mono.zip(
            summaryMono,
            recentPaymentsMono,
            accountsMono,
            notificationsMono,
            limitsMono
        ).map(tuple -> PaymentDashboard.builder()
            .summary(tuple.getT1())
            .recentPayments(tuple.getT2())
            .accounts(tuple.getT3())
            .notifications(tuple.getT4())
            .limits(tuple.getT5())
            .build()
        );
    }
    
    /**
     * Single Payment Query
     */
    @QueryMapping
    public Mono<Payment> payment(@Argument String paymentId) {
        return paymentService.getPayment(paymentId);
    }
    
    /**
     * Payments List Query (with filtering and pagination)
     */
    @QueryMapping
    public Mono<PaymentConnection> payments(
        @Argument PaymentFilter filter,
        @Argument Pagination pagination
    ) {
        return paymentService.getPayments(filter, pagination);
    }
    
    /**
     * Account Balance Query
     */
    @QueryMapping
    public Mono<AccountBalance> accountBalance(@Argument String accountNumber) {
        return accountService.getBalance(accountNumber);
    }
    
    /**
     * Tenant Config Query
     */
    @QueryMapping
    public Mono<TenantConfig> tenantConfig(@Argument String configKey) {
        String tenantId = TenantContext.getTenantId();
        return tenantConfigService.getConfig(tenantId, configKey);
    }
    
    // ────────────────────────────────────────────────────────────
    // MUTATIONS
    // ────────────────────────────────────────────────────────────
    
    @MutationMapping
    public Mono<PaymentResponse> initiatePayment(
        @Argument InitiatePaymentInput input
    ) {
        log.info("Initiating payment via Web BFF: {}", input);
        
        InitiatePaymentCommand command = InitiatePaymentCommand.builder()
            .tenantId(TenantContext.getTenantId())
            .sourceAccount(input.getSourceAccount())
            .destinationAccount(input.getDestinationAccount())
            .amount(input.getAmount())
            .currency(input.getCurrency())
            .reference(input.getReference())
            .paymentType(input.getPaymentType())
            .priority(input.getPriority())
            .initiatedBy(SecurityContext.getCurrentUser())
            .build();
        
        return paymentService.initiatePayment(command);
    }
    
    @MutationMapping
    public Mono<Boolean> markNotificationAsRead(@Argument String notificationId) {
        return notificationService.markAsRead(notificationId);
    }
    
    // ────────────────────────────────────────────────────────────
    // SUBSCRIPTIONS (Real-time updates via WebSocket)
    // ────────────────────────────────────────────────────────────
    
    @SubscriptionMapping
    public Flux<Payment> paymentUpdated(@Argument String customerId) {
        log.info("Client subscribed to payment updates for customer: {}", customerId);
        
        // Subscribe to payment events from Kafka/Service Bus
        return paymentEventStream.subscribe(customerId)
            .map(event -> paymentService.getPayment(event.getPaymentId()).block());
    }
    
    @SubscriptionMapping
    public Flux<Notification> notificationReceived(@Argument String customerId) {
        return notificationEventStream.subscribe(customerId)
            .map(event -> notificationService.getNotification(event.getNotificationId()).block());
    }
}
```

#### Configuration

```yaml
# application-web-bff.yml
server:
  port: 8090

spring:
  application:
    name: web-bff
  
  graphql:
    graphiql:
      enabled: true  # GraphQL Playground
      path: /graphiql
    
    schema:
      printer:
        enabled: true
    
    websocket:
      path: /graphql-ws  # For subscriptions
  
  # Downstream services
  services:
    payment-service:
      url: http://payment-service:8080
    account-service:
      url: http://account-adapter:8082
    notification-service:
      url: http://notification-service:8089

# Security
security:
  jwt:
    public-key: ${JWT_PUBLIC_KEY}

# Caching
spring:
  cache:
    type: redis
    redis:
      time-to-live: 300000  # 5 minutes
```

---

## 2. Mobile BFF (REST - Lightweight)

### Technology Stack
- **Language**: Java 17, Spring Boot 3.x
- **Protocol**: REST (optimized for mobile)
- **Port**: 8091
- **Client**: Mobile Apps (iOS, Android)

### Why Different from Web?

| Aspect | Web Portal | Mobile App |
|--------|------------|------------|
| **Payload Size** | Large (rich UI) | Small (limited bandwidth) |
| **Round-trips** | Acceptable | Minimize (battery/data) |
| **Image URLs** | Full size | Thumbnails, CDN links |
| **Real-time** | WebSocket | Push notifications |
| **Offline** | Not needed | Sync when online |

### Implementation

```java
package com.payments.bff.mobile;

/**
 * Mobile BFF - REST Controller
 * 
 * Optimized for mobile apps:
 * - Smaller payloads
 * - Fewer round-trips
 * - Mobile-specific features (push tokens, image optimization)
 */
@RestController
@RequestMapping("/mobile/api/v1")
@Slf4j
public class MobileBFFController {
    
    @Autowired
    private PaymentService paymentService;
    
    @Autowired
    private AccountService accountService;
    
    @Autowired
    private NotificationService notificationService;
    
    /**
     * Mobile Dashboard - Lightweight version
     * 
     * Returns minimal data optimized for mobile
     */
    @GetMapping("/dashboard")
    public Mono<MobileDashboardResponse> getDashboard() {
        String customerId = SecurityContext.getCurrentCustomerId();
        
        log.info("Fetching mobile dashboard for customer: {}", customerId);
        
        // Fetch data in parallel (but less data than web)
        return Mono.zip(
            paymentService.getRecentPayments(customerId, 5),  // Only 5 recent (vs 10 for web)
            accountService.getBalancesOnly(customerId),       // Only balances, no details
            notificationService.getUnreadCount(customerId)    // Just count, not messages
        ).map(tuple -> MobileDashboardResponse.builder()
            .recentPayments(tuple.getT1().stream()
                .map(this::toMobilePaymentSummary)  // Simplified model
                .collect(Collectors.toList()))
            .accountBalances(tuple.getT2())
            .unreadNotifications(tuple.getT3())
            .lastSyncedAt(Instant.now())
            .build()
        );
    }
    
    /**
     * Single Payment - Mobile version (lighter)
     */
    @GetMapping("/payments/{paymentId}")
    public Mono<MobilePaymentResponse> getPayment(@PathVariable String paymentId) {
        return paymentService.getPayment(paymentId)
            .map(this::toMobilePaymentResponse);
    }
    
    /**
     * Initiate Payment - Mobile optimized
     */
    @PostMapping("/payments")
    public Mono<MobilePaymentResponse> initiatePayment(
        @RequestBody @Valid InitiatePaymentRequest request
    ) {
        log.info("Initiating payment via Mobile BFF: {}", request);
        
        // Validate mobile-specific fields
        if (request.getDeviceId() == null) {
            throw new BadRequestException("Device ID required for mobile payments");
        }
        
        InitiatePaymentCommand command = InitiatePaymentCommand.builder()
            .tenantId(TenantContext.getTenantId())
            .sourceAccount(request.getSourceAccount())
            .destinationAccount(request.getDestinationAccount())
            .amount(request.getAmount())
            .currency("ZAR")
            .reference(request.getReference())
            .paymentType(request.getPaymentType())
            .priority(Priority.NORMAL)
            .initiatedBy(SecurityContext.getCurrentUser())
            .deviceId(request.getDeviceId())  // Mobile-specific
            .deviceType(request.getDeviceType())  // iOS, Android
            .build();
        
        return paymentService.initiatePayment(command)
            .map(this::toMobilePaymentResponse);
    }
    
    /**
     * Register Push Token - Mobile-specific
     */
    @PostMapping("/push-token")
    public Mono<Void> registerPushToken(@RequestBody RegisterPushTokenRequest request) {
        String customerId = SecurityContext.getCurrentCustomerId();
        
        return notificationService.registerPushToken(
            customerId,
            request.getToken(),
            request.getDeviceType(),
            request.getDeviceId()
        );
    }
    
    /**
     * Sync Data - Mobile-specific (offline support)
     */
    @PostMapping("/sync")
    public Mono<SyncResponse> syncData(@RequestBody SyncRequest request) {
        String customerId = SecurityContext.getCurrentCustomerId();
        
        // Get updates since last sync
        return Mono.zip(
            paymentService.getPaymentsSince(customerId, request.getLastSyncedAt()),
            notificationService.getNotificationsSince(customerId, request.getLastSyncedAt())
        ).map(tuple -> SyncResponse.builder()
            .payments(tuple.getT1())
            .notifications(tuple.getT2())
            .syncedAt(Instant.now())
            .build()
        );
    }
    
    // ────────────────────────────────────────────────────────────
    // TRANSFORMATION METHODS (Simplify for mobile)
    // ────────────────────────────────────────────────────────────
    
    private MobilePaymentSummary toMobilePaymentSummary(Payment payment) {
        return MobilePaymentSummary.builder()
            .id(payment.getId())
            .amount(payment.getAmount().getAmount())
            .currency(payment.getAmount().getCurrency().getCurrencyCode())
            .reference(truncate(payment.getReference(), 30))  // Truncate for mobile
            .status(payment.getStatus().toString())
            .statusIcon(getStatusIcon(payment.getStatus()))  // Mobile-specific
            .initiatedAt(payment.getInitiatedAt())
            .build();
    }
    
    private String getStatusIcon(PaymentStatus status) {
        // Return emoji or icon name for mobile UI
        switch (status) {
            case COMPLETED: return "✓";
            case FAILED: return "✗";
            case CLEARING: return "⏳";
            default: return "●";
        }
    }
}

// ============================================================================
// MOBILE-SPECIFIC DTOs (Lightweight)
// ============================================================================

@Data
@Builder
class MobileDashboardResponse {
    private List<MobilePaymentSummary> recentPayments;  // Only 5, simplified
    private List<AccountBalanceDTO> accountBalances;    // Only balances
    private int unreadNotifications;                    // Just count
    private Instant lastSyncedAt;
}

@Data
@Builder
class MobilePaymentSummary {
    private String id;
    private BigDecimal amount;
    private String currency;
    private String reference;  // Truncated to 30 chars
    private String status;
    private String statusIcon;  // Emoji for mobile UI
    private Instant initiatedAt;
    // NO status history, NO detailed info (save bandwidth)
}
```

#### Configuration

```yaml
# application-mobile-bff.yml
server:
  port: 8091

spring:
  application:
    name: mobile-bff
  
  # Mobile-specific settings
  servlet:
    multipart:
      max-file-size: 10MB  # For receipt uploads
  
  # Compression (important for mobile)
  compression:
    enabled: true
    mime-types: application/json,application/xml,text/html,text/xml,text/plain

# Mobile-specific features
mobile:
  push-notifications:
    enabled: true
    fcm-api-key: ${FCM_API_KEY}  # Firebase Cloud Messaging
  
  image-cdn:
    base-url: https://cdn.payments.example.com
  
  sync:
    max-records-per-sync: 100
```

---

## 3. Partner API BFF (REST - Comprehensive)

### Technology Stack
- **Language**: Java 17, Spring Boot 3.x
- **Protocol**: REST (comprehensive, B2B)
- **Port**: 8092
- **Client**: Partner Banks (B2B Integration)

### Why Different?

| Feature | Internal (Web/Mobile) | Partner API |
|---------|----------------------|-------------|
| **Auth** | JWT (customer) | OAuth 2.0 (client credentials) |
| **Rate Limiting** | Per user | Per partner |
| **Payloads** | Simplified | Comprehensive (all fields) |
| **Operations** | Single payments | Bulk payments |
| **Webhooks** | No | Yes |
| **SLA** | Best effort | Guaranteed |

### Implementation

```java
package com.payments.bff.partner;

/**
 * Partner API BFF - REST Controller
 * 
 * Optimized for B2B partners:
 * - Comprehensive data
 * - Bulk operations
 * - Webhooks
 * - OAuth 2.0
 */
@RestController
@RequestMapping("/partner/api/v1")
@Slf4j
public class PartnerApiBFFController {
    
    @Autowired
    private PaymentService paymentService;
    
    @Autowired
    private BulkPaymentService bulkPaymentService;
    
    @Autowired
    private WebhookService webhookService;
    
    /**
     * Single Payment (comprehensive response)
     */
    @GetMapping("/payments/{paymentId}")
    public Mono<PartnerPaymentResponse> getPayment(@PathVariable String paymentId) {
        return paymentService.getPayment(paymentId)
            .map(this::toComprehensiveResponse);  // ALL fields
    }
    
    /**
     * Initiate Payment (comprehensive)
     */
    @PostMapping("/payments")
    public Mono<PartnerPaymentResponse> initiatePayment(
        @RequestBody @Valid PartnerInitiatePaymentRequest request
    ) {
        log.info("Partner payment request: {}", request);
        
        // Validate partner-specific fields
        validatePartnerRequest(request);
        
        InitiatePaymentCommand command = InitiatePaymentCommand.builder()
            .tenantId(TenantContext.getTenantId())
            .sourceAccount(request.getSourceAccount())
            .destinationAccount(request.getDestinationAccount())
            .amount(request.getAmount())
            .currency(request.getCurrency())
            .reference(request.getReference())
            .paymentType(request.getPaymentType())
            .priority(request.getPriority())
            .initiatedBy("PARTNER:" + SecurityContext.getPartnerId())
            .partnerReference(request.getPartnerReference())  // Partner-specific
            .callbackUrl(request.getCallbackUrl())  // For webhooks
            .build();
        
        return paymentService.initiatePayment(command)
            .doOnSuccess(payment -> {
                // Register webhook if provided
                if (request.getCallbackUrl() != null) {
                    webhookService.register(
                        payment.getId(),
                        request.getCallbackUrl()
                    );
                }
            })
            .map(this::toComprehensiveResponse);
    }
    
    /**
     * Bulk Payment Initiation - Partner-specific
     */
    @PostMapping("/payments/bulk")
    public Mono<BulkPaymentResponse> initiateBulkPayments(
        @RequestBody @Valid BulkPaymentRequest request
    ) {
        log.info("Bulk payment request: {} payments", request.getPayments().size());
        
        // Validate bulk limits
        if (request.getPayments().size() > 1000) {
            throw new BadRequestException("Maximum 1000 payments per bulk request");
        }
        
        return bulkPaymentService.processBulk(request)
            .map(result -> BulkPaymentResponse.builder()
                .batchId(result.getBatchId())
                .totalPayments(result.getTotalPayments())
                .accepted(result.getAcceptedCount())
                .rejected(result.getRejectedCount())
                .results(result.getResults())
                .estimatedCompletionTime(result.getEstimatedCompletion())
                .build()
            );
    }
    
    /**
     * Payment Status (comprehensive)
     */
    @GetMapping("/payments/{paymentId}/status")
    public Mono<PaymentStatusResponse> getPaymentStatus(@PathVariable String paymentId) {
        return paymentService.getPaymentWithHistory(paymentId)
            .map(payment -> PaymentStatusResponse.builder()
                .paymentId(payment.getId())
                .status(payment.getStatus())
                .statusHistory(payment.getStatusHistory())  // Full history
                .currentStep(payment.getCurrentStep())
                .estimatedCompletion(payment.getEstimatedCompletion())
                .clearingReference(payment.getClearingReference())
                .build()
            );
    }
    
    /**
     * Webhook Management
     */
    @PostMapping("/webhooks")
    public Mono<WebhookResponse> registerWebhook(
        @RequestBody @Valid RegisterWebhookRequest request
    ) {
        String partnerId = SecurityContext.getPartnerId();
        
        return webhookService.register(
            partnerId,
            request.getUrl(),
            request.getEvents(),
            request.getSecret()
        ).map(webhook -> WebhookResponse.builder()
            .webhookId(webhook.getId())
            .url(webhook.getUrl())
            .events(webhook.getEvents())
            .status("ACTIVE")
            .build()
        );
    }
}

// ============================================================================
// PARTNER-SPECIFIC DTOs (Comprehensive)
// ============================================================================

@Data
class PartnerPaymentResponse {
    private String paymentId;
    private String partnerReference;  // Partner's reference
    private String tenantId;
    private String sourceAccount;
    private String destinationAccount;
    private BigDecimal amount;
    private String currency;
    private String reference;
    private String paymentType;
    private String status;
    private List<StatusChange> statusHistory;  // Full history
    private String clearingReference;
    private Instant initiatedAt;
    private Instant completedAt;
    private Instant estimatedCompletionTime;
    private Map<String, String> metadata;  // Additional data
}

@Data
class BulkPaymentRequest {
    private String batchReference;
    private List<PaymentRequest> payments;  // Up to 1000
    private String callbackUrl;
    private Priority priority;
}

@Data
class BulkPaymentResponse {
    private String batchId;
    private int totalPayments;
    private int accepted;
    private int rejected;
    private List<PaymentResult> results;
    private Instant estimatedCompletionTime;
}
```

#### OAuth 2.0 Configuration

```yaml
# application-partner-api-bff.yml
server:
  port: 8092

spring:
  application:
    name: partner-api-bff
  
  # OAuth 2.0 Resource Server
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: https://auth.payments.example.com
          jwk-set-uri: https://auth.payments.example.com/.well-known/jwks.json

# Partner-specific settings
partner:
  rate-limit:
    default: 1000  # 1000 requests per minute per partner
    premium: 5000  # 5000 for premium partners
  
  bulk-payment:
    max-batch-size: 1000
    max-concurrent-batches: 10
  
  webhook:
    retry-attempts: 3
    retry-delay: 5000  # 5 seconds
    timeout: 30000  # 30 seconds
```

---

## 4. BFF Comparison

| Feature | Web BFF | Mobile BFF | Partner API BFF |
|---------|---------|------------|-----------------|
| **Protocol** | GraphQL | REST | REST |
| **Port** | 8090 | 8091 | 8092 |
| **Payload Size** | Large | Small | Comprehensive |
| **Auth** | JWT | JWT | OAuth 2.0 |
| **Real-time** | WebSocket | Push | Webhooks |
| **Caching** | Aggressive | Moderate | Minimal |
| **Rate Limit** | 1000/min | 100/min | 1000-5000/min |
| **Bulk Ops** | No | No | Yes |
| **SLA** | Best effort | Best effort | 99.9% |

---

## 5. Benefits Achieved

### Performance

- **Fewer Round-trips**: Single request returns aggregated data
- **Smaller Payloads**: Mobile gets only what it needs (50% smaller)
- **Parallel Fetching**: BFF calls backends in parallel

### Developer Experience

- **Client Optimized**: Each client gets API tailored to its needs
- **Type Safety**: GraphQL provides schema, contracts
- **Versioning**: No API versioning needed (GraphQL flexible)

### Maintainability

- **Separation of Concerns**: BFF handles client-specific logic
- **Backend Services**: Stay generic, don't change for clients
- **Independent Evolution**: Clients evolve without backend changes

---

## Related Documents

- **[13-MODERN-ARCHITECTURE-PATTERNS.md](13-MODERN-ARCHITECTURE-PATTERNS.md)** - BFF pattern overview
- **[02-MICROSERVICES-BREAKDOWN.md](02-MICROSERVICES-BREAKDOWN.md)** - Backend services

---

**Last Updated**: 2025-10-11  
**Version**: 1.0
