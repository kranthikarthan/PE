# IBM MQ Integration for Remote Notifications Engine

## Overview

This document describes the architectural option to integrate with a **remote notifications engine** using **IBM MQ (non-persistent)** for notification delivery. Since notifications are **not a core function** of the payments engine, this design treats them as **fire-and-forget** operations that don't impact payment processing reliability.

---

## Design Philosophy

### Core Principle: Notifications are Non-Critical

```
Payment Processing (Core):
├─ MUST succeed (critical path)
├─ ACID transactions
├─ Persistent storage
├─ Synchronous validation
├─ Saga orchestration
└─ Zero data loss

Notifications (Non-Core):
├─ CAN fail (not critical path)
├─ Fire-and-forget
├─ Non-persistent messaging
├─ Asynchronous delivery
├─ No saga participation
└─ Best-effort delivery
```

**Rationale**: A payment must succeed even if notifications fail. Notifications are for **user convenience**, not **business logic**.

---

## Architecture Options

### Option 1: Internal Notification Service (Current)

```
┌─────────────────────────────────────────────────────────────┐
│                    Payments Engine                           │
├─────────────────────────────────────────────────────────────┤
│                                                              │
│  Payment Service                                             │
│         │                                                    │
│         ├──> Kafka Event (payment.completed)                │
│         │                                                    │
│         └──> Notification Service                           │
│                     │                                        │
│                     ├──> SMS Provider (Twilio)              │
│                     ├──> Email Provider (SendGrid)          │
│                     ├──> Push Provider (Firebase)           │
│                     └──> In-App (WebSocket)                 │
│                                                              │
└─────────────────────────────────────────────────────────────┘

Pros:
✅ Full control over notification logic
✅ Easy to customize per tenant
✅ No external dependencies

Cons:
❌ Must maintain notification code
❌ Must manage provider integrations
❌ Non-core functionality in core system
```

### Option 2: Remote Notifications Engine via IBM MQ ⭐ (New)

```
┌──────────────────────────────┐      IBM MQ      ┌────────────────────────┐
│     Payments Engine          │   (Non-Persist)  │  Remote Notifications  │
├──────────────────────────────┤  ═══════════════> │       Engine           │
│                              │                   ├────────────────────────┤
│  Payment Service             │                   │                        │
│         │                    │                   │  • SMS (Twilio)        │
│         └──> IBM MQ Adapter  │   PUT Message     │  • Email (SendGrid)    │
│                 │            │ ─────────────────>│  • Push (Firebase)     │
│                 │            │   (fire-forget)   │  • In-App              │
│                 │            │                   │  • WhatsApp            │
│                 │            │                   │  • Webhooks            │
│                 X            │                   │                        │
│            (no ACK wait)     │                   │  • Template Mgmt       │
│                              │                   │  • Retry Logic         │
│                              │                   │  • Analytics           │
│                              │                   │  • Compliance          │
└──────────────────────────────┘                   └────────────────────────┘

Pros:
✅ Externalize non-core functionality
✅ IBM MQ is enterprise-grade
✅ Non-persistent = high throughput
✅ Fire-and-forget = no blocking
✅ Remote engine handles all complexity
✅ Payments engine stays focused

Cons:
⚠️ Dependency on external system
⚠️ Less control over notification logic
⚠️ IBM MQ infrastructure required
```

**Recommendation**: **Option 2** (Remote Engine) for production systems where notifications are not core business logic.

---

## IBM MQ Configuration

### Non-Persistent Messaging

**Why Non-Persistent?**

```
Persistent Messaging:
├─ Messages written to disk
├─ Survives queue manager restart
├─ Slower throughput (~5K msg/sec)
├─ Higher latency (~10-50ms)
└─ Overkill for notifications

Non-Persistent Messaging: ⭐
├─ Messages in memory only
├─ Lost on queue manager restart
├─ Higher throughput (~50K+ msg/sec)
├─ Lower latency (~1-5ms)
└─ Perfect for notifications (fire-and-forget)
```

**Trade-off**: If IBM MQ crashes, in-flight notifications are lost. **This is acceptable** because:
1. Notifications are not critical to payment processing
2. Payments are already recorded (persistent)
3. Users can check payment status via API/UI
4. Can be re-sent from payment history if needed

### MQ Configuration

```yaml
# IBM MQ Configuration (Non-Persistent)
ibm:
  mq:
    queue-manager: PAYMENTS_QM
    channel: PAYMENTS.SVRCONN
    connection-name: ibmmq.payments.local(1414)
    user: payments_app
    
    # Queue for notification requests
    notification-queue: PAYMENTS.NOTIFICATIONS.OUT
    
    # Non-persistent messaging
    message-persistence: NON_PERSISTENT
    message-priority: 4  # Normal priority
    
    # Timeouts (fire-and-forget)
    put-timeout: 1000  # 1 second max
    connection-timeout: 5000
    
    # Connection pooling
    pool:
      enabled: true
      max-connections: 10
      idle-timeout: 300000  # 5 minutes
    
    # SSL/TLS (optional)
    ssl:
      enabled: false  # For internal network
      cipher-suite: TLS_RSA_WITH_AES_256_CBC_SHA256
      
    # Error handling
    error-handling:
      mode: IGNORE  # Fire-and-forget
      log-failures: true
      metrics-enabled: true
```

---

## Message Format

### Notification Request Message

```json
{
  "messageType": "NOTIFICATION_REQUEST",
  "version": "1.0",
  "timestamp": "2025-01-15T10:30:00Z",
  "messageId": "550e8400-e29b-41d4-a716-446655440000",
  
  "notificationDetails": {
    "notificationType": "PAYMENT_COMPLETED",
    "priority": "NORMAL",
    "channels": ["SMS", "EMAIL", "PUSH"],
    
    "recipient": {
      "customerId": "CUST-12345",
      "tenantId": "TENANT-001",
      "phone": "+27821234567",
      "email": "customer@example.com",
      "deviceTokens": ["firebase-token-123"],
      "preferredLanguage": "en",
      "timezone": "Africa/Johannesburg"
    },
    
    "payload": {
      "paymentId": "PAY-67890",
      "amount": 10000.00,
      "currency": "ZAR",
      "fromAccount": "***1234",
      "toAccount": "***5678",
      "status": "COMPLETED",
      "timestamp": "2025-01-15T10:29:55Z",
      "reference": "Salary payment"
    },
    
    "templateId": "payment_completed_v1",
    "templateVariables": {
      "customerName": "John Doe",
      "amount": "R 100.00",
      "beneficiary": "Jane Smith",
      "timestamp": "15 Jan 2025, 10:29"
    }
  },
  
  "metadata": {
    "source": "payments-engine",
    "environment": "production",
    "region": "za-north",
    "correlationId": "550e8400-e29b-41d4-a716-446655440000"
  }
}
```

### MQ Message Headers

```
MQMD (Message Descriptor):
├─ Format: MQSTR (String)
├─ Persistence: MQPER_NOT_PERSISTENT ⭐
├─ Priority: 4 (Normal)
├─ Expiry: 300 (5 minutes)
├─ MsgType: MQMT_DATAGRAM (fire-and-forget)
├─ Encoding: JSON (UTF-8)
└─ CodedCharSetId: 1208 (UTF-8)

RFH2 (Rules and Formatting Header):
├─ usr.notificationType: PAYMENT_COMPLETED
├─ usr.tenantId: TENANT-001
├─ usr.priority: NORMAL
└─ usr.correlationId: 550e8400...
```

---

## Implementation

### IBM MQ Adapter Service

```java
/**
 * Adapter for sending notifications to remote engine via IBM MQ.
 * Uses non-persistent messaging for fire-and-forget delivery.
 */
@Service
@Slf4j
public class IbmMqNotificationAdapter {
    
    private final JmsTemplate jmsTemplate;
    private final MeterRegistry meterRegistry;
    
    @Autowired
    public IbmMqNotificationAdapter(
        ConnectionFactory connectionFactory,
        MeterRegistry meterRegistry
    ) {
        this.jmsTemplate = createJmsTemplate(connectionFactory);
        this.meterRegistry = meterRegistry;
    }
    
    /**
     * Send notification request to remote engine.
     * Non-blocking, fire-and-forget.
     * 
     * @param notification Notification request
     * @return true if message sent (does NOT guarantee delivery)
     */
    public boolean sendNotification(NotificationRequest notification) {
        Timer.Sample sample = Timer.start(meterRegistry);
        
        try {
            // Convert to JSON
            String messageJson = toJson(notification);
            
            // Send to MQ (non-persistent, fire-and-forget)
            jmsTemplate.send(session -> {
                TextMessage message = session.createTextMessage(messageJson);
                
                // Non-persistent delivery
                message.setJMSDeliveryMode(DeliveryMode.NON_PERSISTENT);
                message.setJMSPriority(4);  // Normal
                message.setJMSExpiration(System.currentTimeMillis() + 300000);  // 5 min
                
                // Custom headers
                message.setStringProperty("notificationType", 
                    notification.getNotificationType().name());
                message.setStringProperty("tenantId", 
                    notification.getRecipient().getTenantId());
                message.setStringProperty("correlationId", 
                    notification.getMessageId());
                
                return message;
            });
            
            // Metrics
            sample.stop(Timer.builder("notification.ibmmq.send")
                .tag("type", notification.getNotificationType().name())
                .tag("success", "true")
                .register(meterRegistry));
            
            log.info("Notification sent to IBM MQ: messageId={}, type={}", 
                notification.getMessageId(), 
                notification.getNotificationType());
            
            return true;
            
        } catch (JmsException e) {
            // Fire-and-forget: log error but don't throw exception
            sample.stop(Timer.builder("notification.ibmmq.send")
                .tag("type", notification.getNotificationType().name())
                .tag("success", "false")
                .tag("error", e.getClass().getSimpleName())
                .register(meterRegistry));
            
            log.warn("Failed to send notification to IBM MQ (fire-and-forget, ignoring): " +
                "messageId={}, error={}", 
                notification.getMessageId(), 
                e.getMessage());
            
            return false;
        }
    }
    
    /**
     * Create JmsTemplate with non-persistent configuration.
     */
    private JmsTemplate createJmsTemplate(ConnectionFactory connectionFactory) {
        JmsTemplate template = new JmsTemplate(connectionFactory);
        
        // Non-persistent delivery
        template.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
        template.setPriority(4);  // Normal priority
        template.setTimeToLive(300000);  // 5 minutes
        
        // Fire-and-forget settings
        template.setExplicitQosEnabled(true);
        template.setSessionTransacted(false);
        
        // Timeouts
        template.setReceiveTimeout(1000);  // 1 second (for PUT)
        
        return template;
    }
    
    private String toJson(NotificationRequest notification) {
        // Use Jackson or similar
        return objectMapper.writeValueAsString(notification);
    }
}
```

### Integration with Payment Service

```java
@Service
@Slf4j
public class PaymentService {
    
    private final PaymentRepository paymentRepository;
    private final IbmMqNotificationAdapter notificationAdapter;
    
    @Transactional
    public PaymentResponse completePayment(UUID paymentId) {
        // 1. Complete payment (CRITICAL PATH)
        Payment payment = paymentRepository.findById(paymentId)
            .orElseThrow(() -> new PaymentNotFoundException(paymentId));
        
        payment.complete();
        payment = paymentRepository.save(payment);
        
        // 2. Send notification (NON-CRITICAL, FIRE-AND-FORGET)
        try {
            NotificationRequest notification = buildNotification(payment);
            notificationAdapter.sendNotification(notification);
        } catch (Exception e) {
            // IMPORTANT: Don't fail payment if notification fails
            log.warn("Failed to send notification for payment {}, continuing anyway", 
                paymentId, e);
        }
        
        return PaymentResponse.from(payment);
    }
    
    private NotificationRequest buildNotification(Payment payment) {
        return NotificationRequest.builder()
            .messageId(UUID.randomUUID())
            .notificationType(NotificationType.PAYMENT_COMPLETED)
            .channels(List.of(
                NotificationChannel.SMS, 
                NotificationChannel.EMAIL,
                NotificationChannel.PUSH
            ))
            .recipient(NotificationRecipient.builder()
                .customerId(payment.getCustomerId().toString())
                .tenantId(payment.getTenantId().toString())
                .phone(payment.getCustomerPhone())
                .email(payment.getCustomerEmail())
                .build())
            .payload(NotificationPayload.builder()
                .paymentId(payment.getPaymentId().toString())
                .amount(payment.getAmount().getAmount())
                .currency(payment.getAmount().getCurrency().getCurrencyCode())
                .status(payment.getStatus().name())
                .build())
            .templateId("payment_completed_v1")
            .build();
    }
}
```

---

## Infrastructure Setup

### IBM MQ Deployment (Azure)

#### Option A: Self-Hosted on AKS

```yaml
# IBM MQ StatefulSet on Kubernetes
apiVersion: apps/v1
kind: StatefulSet
metadata:
  name: ibm-mq
  namespace: messaging
spec:
  serviceName: ibm-mq
  replicas: 2  # Active-standby HA
  selector:
    matchLabels:
      app: ibm-mq
  template:
    metadata:
      labels:
        app: ibm-mq
    spec:
      containers:
      - name: ibm-mq
        image: icr.io/ibm-messaging/mq:latest
        ports:
        - containerPort: 1414
          name: mq
        - containerPort: 9443
          name: console
        env:
        - name: LICENSE
          value: accept
        - name: MQ_QMGR_NAME
          value: PAYMENTS_QM
        - name: MQ_APP_PASSWORD
          valueFrom:
            secretKeyRef:
              name: ibm-mq-credentials
              key: app-password
        volumeMounts:
        - name: mq-data
          mountPath: /mnt/mqm
        resources:
          requests:
            memory: "2Gi"
            cpu: "1000m"
          limits:
            memory: "4Gi"
            cpu: "2000m"
  volumeClaimTemplates:
  - metadata:
      name: mq-data
    spec:
      accessModes: [ "ReadWriteOnce" ]
      storageClassName: managed-premium
      resources:
        requests:
          storage: 20Gi
```

#### Option B: IBM MQ on Cloud (Managed)

```hcl
# Terraform for IBM MQ on Cloud
resource "ibm_mq_queue_manager" "payments" {
  name              = "payments-qm"
  location          = "eu-de"  # Or za-south when available
  size              = "small"
  version           = "9.3.0_LTS"
  
  # High availability
  ha_config {
    mode = "active-standby"
  }
  
  # Queue configuration
  queue {
    name = "PAYMENTS.NOTIFICATIONS.OUT"
    max_depth = 50000
    default_persistence = "non-persistent"
  }
  
  # Channel
  channel {
    name = "PAYMENTS.SVRCONN"
    type = "SVRCONN"
    max_instances = 100
  }
  
  # Authentication
  auth_info {
    type = "idpwos"
    user_name = "payments_app"
  }
}
```

### Spring Boot Configuration

```yaml
# application.yml
spring:
  application:
    name: payment-service
  
# IBM MQ Configuration
ibm:
  mq:
    queue-manager: PAYMENTS_QM
    channel: PAYMENTS.SVRCONN
    connection-name: ibmmq.payments.local(1414)
    user: ${IBM_MQ_USER}
    password: ${IBM_MQ_PASSWORD}
    
    # Queue names
    queues:
      notifications: PAYMENTS.NOTIFICATIONS.OUT
    
    # Non-persistent messaging
    delivery-mode: NON_PERSISTENT
    priority: 4
    time-to-live: 300000  # 5 minutes
    
    # Connection pool
    pool:
      enabled: true
      max-connections: 10
      idle-timeout: 300000

# Feature toggle
features:
  notifications:
    # Use IBM MQ adapter instead of internal service
    use-ibm-mq: true
    use-internal-service: false
```

### Dependencies (Maven)

```xml
<!-- IBM MQ Dependencies -->
<dependency>
    <groupId>com.ibm.mq</groupId>
    <artifactId>mq-jms-spring-boot-starter</artifactId>
    <version>3.1.1</version>
</dependency>

<dependency>
    <groupId>com.ibm.mq</groupId>
    <artifactId>com.ibm.mq.allclient</artifactId>
    <version>9.3.4.0</version>
</dependency>

<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-jms</artifactId>
</dependency>
```

---

## Remote Notifications Engine (Receiving Side)

### Consumer Application

```java
/**
 * Remote notifications engine - receives requests from payments engine
 * via IBM MQ and processes them.
 */
@Service
@Slf4j
public class NotificationRequestConsumer {
    
    private final NotificationProcessor notificationProcessor;
    
    @JmsListener(
        destination = "PAYMENTS.NOTIFICATIONS.OUT",
        containerFactory = "jmsListenerContainerFactory"
    )
    public void receiveNotificationRequest(
        @Payload String messageJson,
        @Headers Map<String, Object> headers,
        Message message
    ) {
        try {
            // Parse message
            NotificationRequest request = parseJson(messageJson);
            
            log.info("Received notification request: messageId={}, type={}", 
                request.getMessageId(), 
                request.getNotificationType());
            
            // Process notification (async)
            notificationProcessor.processAsync(request);
            
        } catch (Exception e) {
            log.error("Failed to process notification request", e);
            // Note: Non-persistent, so message is lost if processing fails
            // This is acceptable for notifications (fire-and-forget)
        }
    }
}

@Service
@Slf4j
public class NotificationProcessor {
    
    private final SmsProvider smsProvider;
    private final EmailProvider emailProvider;
    private final PushProvider pushProvider;
    
    @Async
    public void processAsync(NotificationRequest request) {
        List<NotificationChannel> channels = request.getChannels();
        
        for (NotificationChannel channel : channels) {
            try {
                switch (channel) {
                    case SMS:
                        smsProvider.send(request);
                        break;
                    case EMAIL:
                        emailProvider.send(request);
                        break;
                    case PUSH:
                        pushProvider.send(request);
                        break;
                }
            } catch (Exception e) {
                log.warn("Failed to send notification via {}: {}", 
                    channel, e.getMessage());
                // Continue with other channels
            }
        }
    }
}
```

---

## Monitoring & Observability

### Metrics to Track

```yaml
# Prometheus Metrics
notification.ibmmq.send.count:
  type: counter
  tags: [type, success, error]
  description: Number of notifications sent to IBM MQ

notification.ibmmq.send.duration:
  type: timer
  tags: [type, success]
  description: Time to send notification to IBM MQ

notification.ibmmq.errors.count:
  type: counter
  tags: [error_type]
  description: Number of errors sending to IBM MQ

notification.ibmmq.connection.active:
  type: gauge
  description: Number of active IBM MQ connections
```

### Alerts

```yaml
# Prometheus Alert Rules
groups:
- name: ibm_mq_notifications
  interval: 30s
  rules:
  
  # High error rate
  - alert: IbmMqNotificationErrorRateHigh
    expr: |
      (
        sum(rate(notification_ibmmq_send_count{success="false"}[5m]))
        /
        sum(rate(notification_ibmmq_send_count[5m]))
      ) > 0.1
    for: 5m
    labels:
      severity: warning
    annotations:
      summary: High IBM MQ notification error rate
      description: |
        More than 10% of notifications failing to send to IBM MQ.
        This is non-critical but should be investigated.
        
  # MQ connectivity issues
  - alert: IbmMqConnectionFailed
    expr: ibmmq_connection_active < 1
    for: 2m
    labels:
      severity: warning
    annotations:
      summary: No active IBM MQ connections
      description: |
        Payments engine has no active connections to IBM MQ.
        Notifications will be lost until connection restored.
        Payments are NOT affected.
```

### Logging

```java
// Structured logging for IBM MQ notifications
log.info("IBM MQ notification sent", 
    kv("messageId", notification.getMessageId()),
    kv("notificationType", notification.getNotificationType()),
    kv("tenantId", notification.getRecipient().getTenantId()),
    kv("channels", notification.getChannels()),
    kv("duration_ms", duration),
    kv("success", true)
);

log.warn("IBM MQ notification failed (fire-and-forget, ignoring)",
    kv("messageId", notification.getMessageId()),
    kv("error", e.getMessage()),
    kv("errorType", e.getClass().getSimpleName()),
    kv("duration_ms", duration),
    kv("success", false)
);
```

---

## Error Handling & Resilience

### Fire-and-Forget Strategy

```
Payment Flow:
1. Validate payment ✅
2. Debit account ✅
3. Credit account ✅
4. Update payment status ✅
5. Send notification ⚠️  <-- Fire-and-forget (can fail)

If Step 5 fails:
├─ Payment still succeeds ✅
├─ Error logged ✅
├─ Metrics recorded ✅
└─ Continue processing ✅

User can:
├─ Check payment status via API
├─ View transaction history
├─ Get notification re-sent on request
└─ Use in-app notification center
```

### Failure Scenarios

| Scenario | Impact | Mitigation |
|----------|--------|------------|
| IBM MQ down | Notifications lost | Payment succeeds, user checks app |
| Network issue | Notification delayed/lost | Retry in remote engine |
| Invalid message | Single notification lost | Validate before send |
| Queue full | Back-pressure | Drop message (non-persistent) |
| Remote engine down | Notifications pile up | Queue depth monitoring |

### Circuit Breaker (Optional)

```java
@CircuitBreaker(
    name = "ibm-mq-notifications",
    fallbackMethod = "fallbackSendNotification"
)
public boolean sendNotification(NotificationRequest notification) {
    // IBM MQ send logic
}

// Fallback: Log locally for manual retry
public boolean fallbackSendNotification(
    NotificationRequest notification, 
    Exception e
) {
    log.error("Circuit breaker open for IBM MQ, storing notification locally", e);
    
    // Store in local database for manual retry
    notificationRepository.save(
        FailedNotification.builder()
            .notificationRequest(notification)
            .failureReason(e.getMessage())
            .timestamp(Instant.now())
            .build()
    );
    
    return false;
}
```

---

## Testing Strategy

### Unit Tests

```java
@Test
void sendNotification_Success() {
    // Given
    NotificationRequest request = createTestNotification();
    
    // When
    boolean result = ibmMqAdapter.sendNotification(request);
    
    // Then
    assertThat(result).isTrue();
    verify(jmsTemplate).send(any(MessageCreator.class));
}

@Test
void sendNotification_Failure_DoesNotThrowException() {
    // Given
    NotificationRequest request = createTestNotification();
    when(jmsTemplate.send(any())).thenThrow(new JmsException("MQ down"));
    
    // When
    boolean result = ibmMqAdapter.sendNotification(request);
    
    // Then
    assertThat(result).isFalse();
    // No exception thrown (fire-and-forget)
}
```

### Integration Tests

```java
@SpringBootTest
@TestPropertySource(properties = {
    "ibm.mq.queue-manager=TEST_QM",
    "ibm.mq.connection-name=localhost(1414)"
})
class IbmMqNotificationIntegrationTest {
    
    @Autowired
    private IbmMqNotificationAdapter adapter;
    
    @Test
    void sendNotification_EndToEnd() {
        // Given
        NotificationRequest request = createTestNotification();
        
        // When
        boolean result = adapter.sendNotification(request);
        
        // Then
        assertThat(result).isTrue();
        
        // Verify message in queue (if testing with real MQ)
        // Or use embedded MQ for testing
    }
}
```

---

## Comparison: Internal vs IBM MQ

| Aspect | Internal Notification Service | IBM MQ + Remote Engine |
|--------|------------------------------|------------------------|
| **Complexity** | Higher (maintain in-house) | Lower (externalize) |
| **Control** | Full control | Less control |
| **Throughput** | ~10K notifications/sec | ~50K+ notifications/sec |
| **Latency** | 50-100ms | 1-5ms (non-persistent) |
| **Reliability** | Persistent (99.99%) | Non-persistent (99.9%) |
| **Cost** | Development + maintenance | IBM MQ licensing |
| **Scalability** | Horizontal scaling | IBM MQ handles it |
| **Vendor Lock-in** | None | IBM MQ dependency |
| **Best For** | Notifications are core | Notifications are non-core ⭐ |

---

## Migration Path

### Step 1: Add IBM MQ Adapter (Parallel Mode)

```yaml
features:
  notifications:
    use-ibm-mq: true
    use-internal-service: true  # Both enabled
    mode: DUAL  # Send to both
```

### Step 2: Monitor & Validate (2 weeks)

- Compare notification delivery rates
- Validate message formats
- Monitor error rates
- Gather feedback

### Step 3: Disable Internal Service

```yaml
features:
  notifications:
    use-ibm-mq: true
    use-internal-service: false  # Disabled
```

### Step 4: Decommission Internal Service (Optional)

- Archive notification service code
- Remove notification database
- Update documentation

---

## Cost Analysis

### IBM MQ Costs

**Option A: Self-Hosted on AKS**
```
IBM MQ License: $10K-50K/year (depending on cores)
AKS Resources: $500/month (2 nodes, 4 vCPU each)
Storage: $50/month (20GB premium)
Total: ~$16K-56K/year
```

**Option B: IBM MQ on Cloud (Managed)**
```
IBM MQ on Cloud: $500-1500/month (small-medium)
Data Transfer: $100/month
Total: ~$7K-19K/year
```

### Internal Notification Service Costs

```
Development: $50K (initial)
Maintenance: $20K/year
Infrastructure: $200/month (Kafka, databases)
Provider APIs: $500/month (Twilio, SendGrid, Firebase)
Total: $50K (initial) + $32K/year (ongoing)
```

### ROI Analysis

```
Year 1:
├─ IBM MQ Option: $7K-56K
├─ Internal Service: $50K + $32K = $82K
└─ Savings: $26K-75K (32-91%) ✅

Year 2+:
├─ IBM MQ Option: $7K-56K/year
├─ Internal Service: $32K/year + maintenance
└─ Savings: $0-25K/year (0-78%)

Break-even: Year 1 (if using managed IBM MQ)
```

**Recommendation**: Use **IBM MQ on Cloud (Managed)** for lower total cost and zero maintenance.

---

## Summary

### Key Design Decisions

1. **Non-Persistent Messaging**: Notifications are fire-and-forget (can be lost)
2. **No Transaction Participation**: Notifications don't block payment processing
3. **No ACK Waiting**: Payment service doesn't wait for notification confirmation
4. **Best-Effort Delivery**: Remote engine handles retries (not payments engine)
5. **Fire-and-Forget**: Payment succeeds even if notification fails

### Benefits

✅ **Separation of Concerns**: Core payment logic separate from notifications  
✅ **High Throughput**: 50K+ notifications/sec (non-persistent MQ)  
✅ **Low Latency**: 1-5ms to send message (fire-and-forget)  
✅ **Externalize Complexity**: Remote engine handles templates, retries, analytics  
✅ **Enterprise-Grade**: IBM MQ is battle-tested  
✅ **Cost-Effective**: Lower TCO than building in-house  

### Trade-offs

⚠️ **Notification Loss**: If MQ crashes, in-flight messages lost (acceptable)  
⚠️ **Less Control**: Remote engine manages notification logic  
⚠️ **IBM MQ Dependency**: Additional infrastructure component  
⚠️ **Licensing Cost**: IBM MQ requires license (or use managed service)  

### When to Use This Option

✅ Notifications are **not core** to business logic  
✅ Payment must succeed **even if notifications fail**  
✅ High throughput required (50K+ notifications/sec)  
✅ Want to **externalize** notification complexity  
✅ Already have IBM MQ infrastructure  
✅ Enterprise environment (IBM MQ is standard)  

---

**Last Updated**: 2025-10-11  
**Version**: 1.0  
**Classification**: Internal  
**Related Documents**: 
- `02-MICROSERVICES-BREAKDOWN.md` (Notification Service)
- `03-EVENT-SCHEMAS.md` (Events)
- `07-AZURE-INFRASTRUCTURE.md` (Infrastructure)
