# Phase 3.4.11: IBM MQ Adapter - Optional Integration

**Date**: October 18, 2025  
**Status**: PLANNED (After Phase 3.4.10)  
**Scope**: Optional capability to switch from internal service to IBM MQ  
**Complexity**: MEDIUM  
**Estimated Duration**: 2-3 hours  

---

## 1. Executive Summary

Add **optional IBM MQ integration** to Phase 3.4 Notification Service via feature toggle, allowing operators to choose between:

- **Mode A**: Internal Service (current implementation) - Full control, multi-tenancy, templates
- **Mode B**: IBM MQ (new adapter) - Fire-and-forget, high throughput, externalized complexity

Both modes coexist, can be run in parallel during migration.

---

## 2. Architecture

### Current State (Phase 3.4.1-3.4.10)

```
Payment Events → Kafka → NotificationEventConsumer → NotificationService
                                                      ├─ Email Adapter
                                                      ├─ SMS Adapter
                                                      └─ Push Adapter
                                                      
Database (PostgreSQL):
├─ notification_queue
├─ notification_templates
├─ notification_preferences
└─ notification_audit_log
```

### New State (Phase 3.4.11 - OPTIONAL)

```
                           ┌─── MODE A: INTERNAL (current) ───┐
                           │                                   │
Payment Events → Kafka → NotificationEventConsumer ──→ NotificationService
                           │                          ├─ Email Adapter
                           │                          ├─ SMS Adapter
                           │                          └─ Push Adapter
                           │
                           └─── MODE B: IBM MQ (new) ────────┐
                                                    │
                           IBMMQAdapter ────────→ IBM MQ Queue
                                                    │
                           Remote Notifications Engine (external)
                           ├─ Email (SES)
                           ├─ SMS (Twilio)
                           └─ Push (FCM)

Feature Toggle (application.yml):
├─ features.notifications.use-internal: true/false
├─ features.notifications.use-ibm-mq: true/false
└─ features.notifications.mode: INTERNAL | IBM_MQ | DUAL (parallel)
```

---

## 3. Implementation Components

### 3.1 Feature Toggle Infrastructure

**File**: `notification-service/src/main/java/com/payments/notification/config/NotificationModeConfig.java`

```java
@Configuration
@EnableConfigurationProperties(NotificationModeProperties.class)
public class NotificationModeConfig {
    
    // Properties class to load from application.yml
    @ConfigurationProperties(prefix = "features.notifications")
    public static class NotificationModeProperties {
        private boolean useInternal = true;           // Default: internal
        private boolean useIbmMq = false;             // Optional: IBM MQ
        private DeliveryMode mode = DeliveryMode.INTERNAL;  // INTERNAL | IBM_MQ | DUAL
        // ... getters/setters
    }
    
    public enum DeliveryMode {
        INTERNAL,    // Use internal service only
        IBM_MQ,      // Use IBM MQ only
        DUAL         // Use both (testing/migration)
    }
}
```

**Configuration** (`application.yml`):

```yaml
features:
  notifications:
    use-internal: true
    use-ibm-mq: false
    mode: INTERNAL  # Options: INTERNAL, IBM_MQ, DUAL
    
    # IBM MQ configuration (only if mode includes IBM_MQ)
    ibm-mq:
      enabled: false
      queue-manager: PAYMENTS_QM
      channel: PAYMENTS.SVRCONN
      connection-name: ibmmq.payments.local(1414)
      queue-name: PAYMENTS.NOTIFICATIONS.OUT
      message-persistence: NON_PERSISTENT
      put-timeout: 1000
      retry-policy:
        max-retries: 3
        backoff-ms: 1000
```

---

### 3.2 IBM MQ Adapter

**File**: `notification-service/src/main/java/com/payments/notification/adapter/IBMMQNotificationAdapter.java`

```java
@Service
@Slf4j
@ConditionalOnProperty(
    name = "features.notifications.use-ibm-mq",
    havingValue = "true"
)
public class IBMMQNotificationAdapter {
    
    private final JmsTemplate jmsTemplate;
    private final MeterRegistry meterRegistry;
    private final NotificationModeConfig.NotificationModeProperties properties;
    
    /**
     * Send notification to IBM MQ (fire-and-forget).
     * Does NOT block payment processing.
     * 
     * @param event Payment event (from Kafka)
     * @return true if message queued (not guaranteed delivery)
     */
    public boolean sendToIBMMQ(PaymentEvent event) {
        Timer.Sample sample = Timer.start(meterRegistry);
        
        try {
            // Convert payment event to notification request
            NotificationRequest request = buildNotificationRequest(event);
            String messageJson = objectMapper.writeValueAsString(request);
            
            // Send non-persistently (fire-and-forget)
            jmsTemplate.send(properties.getIbmMq().getQueueName(), session -> {
                TextMessage message = session.createTextMessage(messageJson);
                message.setJMSDeliveryMode(DeliveryMode.NON_PERSISTENT);
                message.setJMSPriority(4);  // Normal priority
                message.setJMSExpiration(System.currentTimeMillis() + 300000);  // 5 min
                
                // Custom headers
                message.setStringProperty("notificationType", 
                    event.getType().name());
                message.setStringProperty("tenantId", event.getTenantId());
                message.setStringProperty("correlationId", event.getCorrelationId());
                
                return message;
            });
            
            sample.stop(Timer.builder("notification.ibmmq.send")
                .tag("type", event.getType().name())
                .tag("success", "true")
                .register(meterRegistry));
            
            log.info("Notification sent to IBM MQ: eventId={}, type={}", 
                event.getId(), event.getType());
            
            return true;
            
        } catch (Exception e) {
            // Fire-and-forget: log but don't throw
            sample.stop(Timer.builder("notification.ibmmq.send")
                .tag("type", event.getType().name())
                .tag("success", "false")
                .tag("error", e.getClass().getSimpleName())
                .register(meterRegistry));
            
            log.warn("Failed to send to IBM MQ (fire-and-forget): " +
                "eventId={}, error={}", event.getId(), e.getMessage());
            
            return false;  // Non-critical failure
        }
    }
    
    private NotificationRequest buildNotificationRequest(PaymentEvent event) {
        return NotificationRequest.builder()
            .messageId(UUID.randomUUID())
            .notificationType(mapEventType(event.getType()))
            .channels(List.of(
                NotificationChannel.EMAIL,
                NotificationChannel.SMS,
                NotificationChannel.PUSH
            ))
            .recipient(NotificationRecipient.builder()
                .customerId(event.getUserId())
                .tenantId(event.getTenantId())
                .email(event.getUserEmail())
                .phone(event.getUserPhone())
                .build())
            .payload(NotificationPayload.builder()
                .paymentId(event.getId().toString())
                .amount(event.getAmount())
                .currency(event.getCurrency())
                .status(event.getStatus().name())
                .timestamp(event.getTimestamp())
                .build())
            .build();
    }
}
```

---

### 3.3 Notification Event Consumer (Updated)

**File**: `notification-service/src/main/java/com/payments/notification/listener/NotificationEventConsumer.java`

```java
@Component
@Slf4j
@RequiredArgsConstructor
public class NotificationEventConsumer {
    
    private final NotificationService notificationService;
    private final IBMMQNotificationAdapter ibmMqAdapter;
    private final NotificationModeConfig.NotificationModeProperties modeConfig;
    
    @KafkaListener(
        topics = "payment.initiated,payment.cleared,payment.failed",
        groupId = "notification-service-group"
    )
    public void handlePaymentEvent(PaymentEvent event, Acknowledgment ack) {
        try {
            log.info("Received payment event: type={}, tenantId={}", 
                event.getType(), event.getTenantId());
            
            // Route based on delivery mode
            switch (modeConfig.getMode()) {
                case INTERNAL:
                    // Use internal service (current implementation)
                    notificationService.handlePaymentEvent(event);
                    break;
                    
                case IBM_MQ:
                    // Use IBM MQ only
                    ibmMqAdapter.sendToIBMMQ(event);
                    break;
                    
                case DUAL:
                    // Use both (for testing/migration)
                    notificationService.handlePaymentEvent(event);
                    ibmMqAdapter.sendToIBMMQ(event);
                    break;
            }
            
            // Manual commit (only after successful processing)
            ack.acknowledge();
            
        } catch (Exception e) {
            log.error("Error processing payment event: eventId={}, error={}", 
                event.getId(), e.getMessage(), e);
            // Could implement dead letter queue here
        }
    }
}
```

---

### 3.4 DTOs for IBM MQ

**File**: `notification-service/src/main/java/com/payments/notification/dto/NotificationRequest.java`

```java
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationRequest {
    private UUID messageId;
    private NotificationType notificationType;
    private List<NotificationChannel> channels;
    private NotificationRecipient recipient;
    private NotificationPayload payload;
    private String templateId;
    private Map<String, String> templateVariables;
    private LocalDateTime timestamp = LocalDateTime.now();
    
    public String toJson() throws JsonProcessingException {
        return new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .writeValueAsString(this);
    }
}

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationRecipient {
    private String customerId;
    private String tenantId;
    private String email;
    private String phone;
    private List<String> deviceTokens;
    private String preferredLanguage;
    private String timezone;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationPayload {
    private String paymentId;
    private BigDecimal amount;
    private String currency;
    private String status;
    private LocalDateTime timestamp;
    private String reference;
    private String fromAccount;
    private String toAccount;
}
```

---

### 3.5 Dependencies (pom.xml additions)

```xml
<!-- IBM MQ Integration (conditional) -->
<dependency>
    <groupId>com.ibm.mq</groupId>
    <artifactId>mq-jms-spring-boot-starter</artifactId>
    <version>3.1.1</version>
    <optional>true</optional>
</dependency>

<dependency>
    <groupId>com.ibm.mq</groupId>
    <artifactId>com.ibm.mq.allclient</artifactId>
    <version>9.3.4.0</version>
    <optional>true</optional>
</dependency>

<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-jms</artifactId>
    <optional>true</optional>
</dependency>
```

---

### 3.6 Configuration Bean (conditional)

**File**: `notification-service/src/main/java/com/payments/notification/config/IBMMQConfig.java`

```java
@Configuration
@ConditionalOnProperty(
    name = "features.notifications.use-ibm-mq",
    havingValue = "true"
)
@RequiredArgsConstructor
public class IBMMQConfig {
    
    private final NotificationModeConfig.NotificationModeProperties properties;
    
    @Bean
    public ConnectionFactory ibmMqConnectionFactory() {
        MQConnectionFactory connectionFactory = new MQConnectionFactory();
        connectionFactory.setHostName(getHost(properties.getIbmMq().getConnectionName()));
        connectionFactory.setPort(getPort(properties.getIbmMq().getConnectionName()));
        connectionFactory.setChannel(properties.getIbmMq().getChannel());
        connectionFactory.setQueueManager(properties.getIbmMq().getQueueManager());
        connectionFactory.setTransportType(ClientConstants.TRANSPORT_TYPE_TCP);
        return connectionFactory;
    }
    
    @Bean
    public JmsTemplate jmsTemplate(ConnectionFactory connectionFactory) {
        JmsTemplate template = new JmsTemplate(connectionFactory);
        template.setDeliveryMode(
            DeliveryMode.NON_PERSISTENT
        );  // Fire-and-forget
        template.setPriority(4);  // Normal
        template.setTimeToLive(300000);  // 5 minutes
        template.setExplicitQosEnabled(true);
        template.setSessionTransacted(false);
        return template;
    }
}
```

---

## 4. Migration Strategy (Dual Mode)

### Step 1: Enable Dual Mode (Testing)

```yaml
features:
  notifications:
    mode: DUAL  # Send to both internal service AND IBM MQ
    use-internal: true
    use-ibm-mq: true
```

**Actions**:
- ✅ Both systems receive events
- ✅ Compare delivery rates
- ✅ Monitor error rates
- ✅ Validate message formats
- ⏱️ Run for 1-2 weeks

### Step 2: Switch to IBM MQ Only

```yaml
features:
  notifications:
    mode: IBM_MQ  # Use IBM MQ only
    use-internal: false
    use-ibm-mq: true
```

**Actions**:
- ✅ All notifications via IBM MQ
- ✅ Monitor throughput
- ✅ Check remote engine processing
- ⏱️ Validate stability (1 week)

### Step 3: Rollback (if needed)

```yaml
features:
  notifications:
    mode: INTERNAL  # Back to internal
    use-internal: true
    use-ibm-mq: false
```

---

## 5. Testing Strategy

### Unit Tests

```java
@Test
void sendToIBMMQ_Success() {
    PaymentEvent event = createTestEvent();
    boolean result = ibmMqAdapter.sendToIBMMQ(event);
    assertThat(result).isTrue();
    verify(jmsTemplate).send(anyString(), any());
}

@Test
void sendToIBMMQ_Failure_DoesNotThrow() {
    PaymentEvent event = createTestEvent();
    when(jmsTemplate.send(anyString(), any()))
        .thenThrow(new JmsException("MQ down"));
    
    boolean result = ibmMqAdapter.sendToIBMMQ(event);
    
    assertThat(result).isFalse();  // No exception thrown
}
```

### Integration Tests

```java
@Test
void eventConsumer_DualMode_SendsToBoth() {
    PaymentEvent event = createTestEvent();
    
    eventConsumer.handlePaymentEvent(event, acknowledgment);
    
    verify(notificationService).handlePaymentEvent(event);
    verify(ibmMqAdapter).sendToIBMMQ(event);
}

@Test
void eventConsumer_IBMMQOnly_SkipsInternal() {
    modeConfig.setMode(DeliveryMode.IBM_MQ);
    PaymentEvent event = createTestEvent();
    
    eventConsumer.handlePaymentEvent(event, acknowledgment);
    
    verify(ibmMqAdapter).sendToIBMMQ(event);
    verify(notificationService, never()).handlePaymentEvent(event);
}
```

---

## 6. Monitoring & Metrics

### Prometheus Metrics

```yaml
# IBM MQ adapter
notification.ibmmq.send.count:          # Counter by type, success
notification.ibmmq.send.duration:       # Timer by type
notification.ibmmq.connection.active:   # Gauge
notification.ibmmq.errors.count:        # Counter by error type

# Comparison (internal vs IBM MQ)
notification.internal.send.count:       # Internal service metrics
notification.ibmmq.send.count:          # IBM MQ metrics
```

### Alerts

```yaml
- alert: IBMMQConnectionFailed
  expr: notification_ibmmq_connection_active < 1
  annotations:
    summary: "IBM MQ connection down (notifications will fail)"

- alert: IBMMQHighErrorRate
  expr: |
    (
      sum(rate(notification_ibmmq_send_count{success="false"}[5m]))
      /
      sum(rate(notification_ibmmq_send_count[5m]))
    ) > 0.1
  annotations:
    summary: "IBM MQ error rate >10%"
```

---

## 7. Rollback Plan

If IBM MQ causes issues:

```yaml
# Immediate rollback to internal
features:
  notifications:
    mode: INTERNAL
    use-ibm-mq: false
```

**Benefits**:
- ✅ Single configuration change
- ✅ No code deployment needed
- ✅ Feature toggle handles logic
- ✅ Internal service still running in parallel (if in DUAL mode)

---

## 8. Success Criteria

| Criterion | Metric | Target |
|-----------|--------|--------|
| **Functionality** | Both modes work | ✅ 100% |
| **Performance** | IBM MQ throughput | >50K msg/sec |
| **Reliability** | Fire-and-forget | No payment blocking |
| **Backward Compat** | Internal still works | ✅ 100% |
| **Migration** | Switchover time | <5 min downtime |
| **Monitoring** | Metrics captured | ✅ Complete |
| **Documentation** | Setup guide ready | ✅ Complete |

---

## 9. Timeline

| Phase | Duration | Activities |
|-------|----------|------------|
| **Design** | 30 min | Architecture, feature toggle design |
| **Implementation** | 1 hour | Adapter, DTOs, config, integration |
| **Testing** | 45 min | Unit tests, integration tests, DUAL mode |
| **Documentation** | 15 min | Setup guide, migration playbook |
| **Total** | **~2.5 hours** | Complete IBM MQ integration |

---

## 10. File Structure

```
notification-service/
├── src/main/java/com/payments/notification/
│   ├── adapter/
│   │   └── IBMMQNotificationAdapter.java (NEW)
│   ├── config/
│   │   ├── NotificationModeConfig.java (NEW)
│   │   └── IBMMQConfig.java (NEW)
│   ├── dto/
│   │   ├── NotificationRequest.java (NEW)
│   │   ├── NotificationRecipient.java (NEW)
│   │   └── NotificationPayload.java (NEW)
│   ├── listener/
│   │   └── NotificationEventConsumer.java (UPDATED)
│   └── ...
├── src/test/java/...
│   ├── IBMMQAdapterTest.java (NEW)
│   ├── NotificationEventConsumerTest.java (UPDATED)
│   └── ...
├── pom.xml (UPDATED)
└── src/main/resources/
    └── application.yml (UPDATED)
```

---

## 11. Implementation Order

1. ✅ Create `NotificationModeConfig.java` (feature toggle)
2. ✅ Create `IBMMQConfig.java` (IBM MQ beans)
3. ✅ Create `IBMMQNotificationAdapter.java` (main adapter)
4. ✅ Create DTOs (`NotificationRequest`, `NotificationRecipient`, `NotificationPayload`)
5. ✅ Update `NotificationEventConsumer.java` (add routing logic)
6. ✅ Update `pom.xml` (add IBM MQ dependencies)
7. ✅ Update `application.yml` (add configuration)
8. ✅ Add unit tests
9. ✅ Add integration tests
10. ✅ Create migration playbook documentation

---

## 12. Future Considerations

- 🔮 Circuit breaker for IBM MQ failures
- 🔮 Dead letter queue handling
- 🔮 Async queue depth monitoring
- 🔮 Load balancing across multiple IBM MQ instances
- 🔮 Support for other messaging systems (RabbitMQ, AWS SQS)

---

## Summary

**Phase 3.4.11 adds optional IBM MQ integration as final task**, allowing:

✅ Internal service continues working (current implementation)  
✅ Feature toggle to switch to IBM MQ  
✅ Dual mode for migration/testing  
✅ Zero impact if not used  
✅ Easy rollback if needed  

**Estimated**: 2.5 hours for complete implementation + tests

---

**Status**: Ready for Phase 3.4.11 implementation (after Phase 3.4.10)
