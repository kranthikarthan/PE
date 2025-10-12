# Enterprise Integration Patterns - Implementation Guide

## Overview

This document applies **Enterprise Integration Patterns (EIP)** by Gregor Hohpe to the Payments Engine architecture, formalizing our integration approach using industry-standard, battle-tested patterns.

**Reference**: *Enterprise Integration Patterns: Designing, Building, and Deploying Messaging Solutions* by Gregor Hohpe and Bobby Woolf

---

## Table of Contents

1. [Integration Architecture Overview](#integration-architecture-overview)
2. [Pattern Categories](#pattern-categories)
3. [Message Construction Patterns](#message-construction-patterns)
4. [Message Routing Patterns](#message-routing-patterns)
5. [Message Transformation Patterns](#message-transformation-patterns)
6. [Message Endpoint Patterns](#message-endpoint-patterns)
7. [System Management Patterns](#system-management-patterns)
8. [Pattern Implementation Matrix](#pattern-implementation-matrix)
9. [Azure Service Bus / Kafka Mapping](#azure-service-bus--kafka-mapping)
10. [Code Examples](#code-examples)

---

## Integration Architecture Overview

### Current Integration Challenges

The Payments Engine integrates with:
- 20 microservices (internal)
- 6+ external core banking systems (REST APIs)
- 5 clearing systems (SAMOS, Bankserv, RTC, PayShap, SWIFT)
- 1 fraud scoring API
- 1 remote notifications engine (IBM MQ)
- Multiple frontend channels (Web, Mobile, Partner APIs)

**Challenge**: How do we manage this complexity reliably, scalably, and maintainably?

**Answer**: **Enterprise Integration Patterns** provide proven solutions.

---

## Pattern Categories

EIP defines 65+ patterns across 6 categories:

| Category | Description | Patterns Used |
|----------|-------------|---------------|
| **Integration Styles** | How systems communicate | Messaging, Shared Database, File Transfer, RPC |
| **Messaging Channels** | How messages travel | Point-to-Point, Publish-Subscribe, Dead Letter Channel |
| **Message Construction** | How messages are structured | Command Message, Event Message, Document Message |
| **Message Routing** | How messages are directed | Content-Based Router, Message Filter, Splitter, Aggregator |
| **Message Transformation** | How messages are converted | Envelope Wrapper, Content Enricher, Claim Check, Normalizer |
| **Message Endpoints** | How systems connect | Event-Driven Consumer, Polling Consumer, Idempotent Receiver |
| **System Management** | How integration is managed | Control Bus, Message Store, Wire Tap, Channel Purger |

---

## Message Construction Patterns

### 1. Command Message

**Intent**: Send a command to another system to perform an action.

**Used In**: Payment initiation, account debit/credit commands

**Implementation**:

```java
// Payment Initiation Service
@Service
public class PaymentCommandPublisher {
    
    @Autowired
    private ServiceBusSenderClient commandChannel;
    
    public void sendDebitAccountCommand(DebitAccountCommand command) {
        ServiceBusMessage message = new ServiceBusMessage(
            toJson(command)
        );
        message.setContentType("application/json");
        message.setMessageId(command.getCommandId());
        message.setSubject("DebitAccount"); // Command type
        message.getApplicationProperties().put("commandType", "DEBIT_ACCOUNT");
        message.getApplicationProperties().put("idempotencyKey", command.getIdempotencyKey());
        
        commandChannel.sendMessage(message);
        log.info("Sent DebitAccountCommand: {}", command.getCommandId());
    }
}
```

**Example Commands**:
- `DebitAccountCommand`
- `CreditAccountCommand`
- `ValidatePaymentCommand`
- `SubmitToClearingCommand`
- `ReserveCustomerLimitCommand`

---

### 2. Event Message

**Intent**: Notify other systems that something happened (past tense).

**Used In**: Payment lifecycle events, audit trail

**Implementation**:

```java
// Transaction Processing Service
@Service
public class PaymentEventPublisher {
    
    @Autowired
    private ServiceBusSenderClient eventChannel;
    
    public void publishPaymentCompletedEvent(PaymentCompletedEvent event) {
        ServiceBusMessage message = new ServiceBusMessage(
            toJson(event)
        );
        message.setContentType("application/json");
        message.setMessageId(event.getEventId());
        message.setSubject("PaymentCompleted"); // Event type (past tense)
        message.getApplicationProperties().put("eventType", "PAYMENT_COMPLETED");
        message.getApplicationProperties().put("aggregateId", event.getPaymentId());
        message.getApplicationProperties().put("timestamp", event.getTimestamp());
        message.getApplicationProperties().put("tenantId", event.getTenantId());
        
        eventChannel.sendMessage(message);
        log.info("Published PaymentCompletedEvent: {}", event.getPaymentId());
    }
}
```

**Example Events**:
- `PaymentInitiatedEvent`
- `PaymentValidatedEvent`
- `PaymentCompletedEvent`
- `PaymentFailedEvent`
- `LimitExceededEvent`

---

### 3. Document Message

**Intent**: Transfer a data structure between systems.

**Used In**: Batch file processing, clearing system messages

**Implementation**:

```java
// Batch Processing Service
public void processBatchFile(BatchPaymentDocument document) {
    // Document contains complete payment batch
    ServiceBusMessage message = new ServiceBusMessage(
        toJson(document)
    );
    message.setContentType("application/json");
    message.setSubject("BatchPaymentDocument");
    message.getApplicationProperties().put("batchId", document.getBatchId());
    message.getApplicationProperties().put("paymentCount", document.getPayments().size());
    message.getApplicationProperties().put("totalAmount", document.getTotalAmount());
    
    documentChannel.sendMessage(message);
}
```

---

### 4. Correlation Identifier

**Intent**: Track related messages across multiple steps.

**Used In**: Saga orchestration, distributed tracing

**Implementation**:

```java
@Service
public class CorrelationService {
    
    private static final String CORRELATION_ID_HEADER = "X-Correlation-ID";
    private static final String CAUSATION_ID_HEADER = "X-Causation-ID";
    
    public void sendCorrelatedMessage(String correlationId, String causationId, Object payload) {
        ServiceBusMessage message = new ServiceBusMessage(toJson(payload));
        
        // Correlation ID: Tracks entire workflow
        message.setCorrelationId(correlationId);
        message.getApplicationProperties().put(CORRELATION_ID_HEADER, correlationId);
        
        // Causation ID: Tracks immediate cause-effect
        message.getApplicationProperties().put(CAUSATION_ID_HEADER, causationId);
        
        // OpenTelemetry Trace ID (distributed tracing)
        message.getApplicationProperties().put("traceId", getTraceId());
        message.getApplicationProperties().put("spanId", getSpanId());
        
        sender.sendMessage(message);
    }
}
```

**Correlation Strategy**:
```
Payment Flow:
├─ Correlation ID: PAY-2025-123456 (entire payment lifecycle)
├─ Causation ID: Changes at each step
│   ├─ Step 1: PAY-2025-123456 (initiation)
│   ├─ Step 2: VAL-2025-789012 (validation result causes next step)
│   ├─ Step 3: ACC-2025-345678 (account debit result)
│   └─ Step 4: CLR-2025-901234 (clearing submission)
└─ Trace ID: OpenTelemetry trace ID (technical tracing)
```

---

### 5. Return Address

**Intent**: Specify where to send the reply.

**Used In**: Request-reply patterns, async callbacks

**Implementation**:

```java
public void sendRequestWithReplyAddress(FraudCheckRequest request) {
    ServiceBusMessage message = new ServiceBusMessage(toJson(request));
    
    // Return Address: Queue for reply
    message.setReplyTo("payment.fraud.replies");
    message.setReplyToSessionId(request.getSessionId());
    
    // Include callback URL for HTTP-based replies
    message.getApplicationProperties().put("callbackUrl", 
        "https://payment-service/api/fraud-callback");
    
    sender.sendMessage(message);
}

// Reply Handler
@ServiceBusListener(destination = "payment.fraud.replies")
public void handleFraudCheckReply(ServiceBusReceivedMessage message) {
    String correlationId = message.getCorrelationId();
    FraudCheckReply reply = fromJson(message.getBody(), FraudCheckReply.class);
    
    // Process reply using correlation ID
    paymentService.processFraudCheckResult(correlationId, reply);
}
```

---

### 6. Message Expiration

**Intent**: Prevent processing of stale messages.

**Used In**: Time-sensitive payment instructions, fraud checks

**Implementation**:

```java
public void sendTimeoutSensitiveMessage(PaymentInstruction instruction) {
    ServiceBusMessage message = new ServiceBusMessage(toJson(instruction));
    
    // Set Time-To-Live (TTL)
    Duration ttl = Duration.ofMinutes(5); // Expire after 5 minutes
    message.setTimeToLive(ttl);
    
    // Set scheduled enqueue time (delay delivery)
    message.setScheduledEnqueueTime(OffsetDateTime.now().plusSeconds(30));
    
    message.getApplicationProperties().put("expiresAt", 
        OffsetDateTime.now().plus(ttl).toString());
    
    sender.sendMessage(message);
}

// Consumer checks expiration
@ServiceBusListener(destination = "payment.instructions")
public void processInstruction(ServiceBusReceivedMessage message) {
    OffsetDateTime expiresAt = OffsetDateTime.parse(
        message.getApplicationProperties().get("expiresAt").toString()
    );
    
    if (OffsetDateTime.now().isAfter(expiresAt)) {
        log.warn("Message expired, discarding: {}", message.getMessageId());
        return; // Discard expired message
    }
    
    // Process message
}
```

---

## Message Routing Patterns

### 7. Content-Based Router

**Intent**: Route messages to different destinations based on content.

**Used In**: Routing Service (route by payment type, amount, currency)

**Implementation**:

```java
@Service
public class ContentBasedPaymentRouter {
    
    @ServiceBusListener(destination = "payment.routing")
    public void routePayment(ServiceBusReceivedMessage message) {
        PaymentInstruction payment = fromJson(message.getBody(), PaymentInstruction.class);
        
        // Route based on content
        String destination = determineRoute(payment);
        
        // Forward to appropriate queue
        forwardToQueue(destination, payment);
        
        log.info("Routed payment {} to {}", payment.getPaymentId(), destination);
    }
    
    private String determineRoute(PaymentInstruction payment) {
        // High-value → SAMOS (RTGS)
        if (payment.getAmount().compareTo(new BigDecimal("5000000")) > 0) {
            return "clearing.samos";
        }
        
        // International → SWIFT
        if (!payment.getCurrency().equals("ZAR")) {
            return "clearing.swift";
        }
        
        // Instant P2P (< R3000) → PayShap
        if (payment.getPaymentType() == PaymentType.P2P 
            && payment.getAmount().compareTo(new BigDecimal("3000")) <= 0) {
            return "clearing.payshap";
        }
        
        // Real-time domestic → RTC
        if (payment.isRealTime()) {
            return "clearing.rtc";
        }
        
        // Default → BankservAfrica (EFT batch)
        return "clearing.bankserv";
    }
}
```

**Routing Rules**:
```
Amount > R5M               → SAMOS (RTGS)
Currency != ZAR            → SWIFT
P2P + Amount <= R3K        → PayShap
Real-time + Domestic       → RTC
Default (Batch)            → BankservAfrica EFT
```

---

### 8. Message Filter

**Intent**: Discard messages that don't meet criteria.

**Used In**: Tenant-based filtering, duplicate detection

**Implementation**:

```java
@Service
public class TenantMessageFilter {
    
    @ServiceBusListener(destination = "payment.notifications")
    public void filterAndProcess(ServiceBusReceivedMessage message) {
        String tenantId = message.getApplicationProperties().get("tenantId").toString();
        String messageType = message.getApplicationProperties().get("messageType").toString();
        
        // Filter 1: Tenant whitelist
        if (!isAllowedTenant(tenantId)) {
            log.warn("Filtering out message for disallowed tenant: {}", tenantId);
            return; // Discard
        }
        
        // Filter 2: Message type blacklist
        if (isBlockedMessageType(messageType)) {
            log.warn("Filtering out blocked message type: {}", messageType);
            return; // Discard
        }
        
        // Filter 3: Duplicate detection (idempotency)
        String idempotencyKey = message.getApplicationProperties().get("idempotencyKey").toString();
        if (isDuplicate(idempotencyKey)) {
            log.warn("Filtering out duplicate message: {}", idempotencyKey);
            return; // Discard
        }
        
        // Message passed all filters, process it
        processMessage(message);
    }
}
```

---

### 9. Splitter

**Intent**: Break a composite message into individual messages.

**Used In**: Batch file processing (split file into individual payments)

**Implementation**:

```java
@Service
public class BatchPaymentSplitter {
    
    @ServiceBusListener(destination = "batch.upload")
    public void splitBatchFile(ServiceBusReceivedMessage message) {
        BatchPaymentFile batchFile = fromJson(message.getBody(), BatchPaymentFile.class);
        
        log.info("Splitting batch file {} with {} payments", 
            batchFile.getBatchId(), batchFile.getPayments().size());
        
        // Split into individual payment messages
        for (PaymentInstruction payment : batchFile.getPayments()) {
            ServiceBusMessage individualMessage = new ServiceBusMessage(toJson(payment));
            
            // Preserve correlation (link back to batch)
            individualMessage.setCorrelationId(batchFile.getBatchId());
            individualMessage.getApplicationProperties().put("batchId", batchFile.getBatchId());
            individualMessage.getApplicationProperties().put("sequenceNumber", payment.getSequenceNumber());
            individualMessage.getApplicationProperties().put("totalCount", batchFile.getPayments().size());
            
            // Send individual payment for processing
            paymentSender.sendMessage(individualMessage);
        }
        
        log.info("Split batch {} into {} individual payment messages", 
            batchFile.getBatchId(), batchFile.getPayments().size());
    }
}
```

---

### 10. Aggregator

**Intent**: Combine multiple related messages into a single message.

**Used In**: Batch response aggregation, multi-step saga results

**Implementation**:

```java
@Service
public class BatchResponseAggregator {
    
    // In-memory store for aggregation (use Redis in production)
    private final Map<String, AggregationState> aggregationStore = new ConcurrentHashMap<>();
    
    @ServiceBusListener(destination = "payment.responses")
    public void aggregateResponse(ServiceBusReceivedMessage message) {
        PaymentResponse response = fromJson(message.getBody(), PaymentResponse.class);
        String batchId = message.getApplicationProperties().get("batchId").toString();
        int totalCount = (int) message.getApplicationProperties().get("totalCount");
        
        // Get or create aggregation state
        AggregationState state = aggregationStore.computeIfAbsent(batchId, 
            k -> new AggregationState(batchId, totalCount));
        
        // Add response to aggregation
        state.addResponse(response);
        
        // Check if aggregation is complete
        if (state.isComplete()) {
            // All responses received, create aggregate
            BatchResponseAggregate aggregate = state.createAggregate();
            
            // Publish aggregated result
            publishBatchResult(aggregate);
            
            // Clean up
            aggregationStore.remove(batchId);
            
            log.info("Batch {} aggregation complete: {} succeeded, {} failed", 
                batchId, aggregate.getSuccessCount(), aggregate.getFailureCount());
        } else {
            log.debug("Batch {} aggregation: {}/{} responses received", 
                batchId, state.getReceivedCount(), state.getTotalCount());
        }
    }
    
    @Data
    static class AggregationState {
        private final String batchId;
        private final int totalCount;
        private final List<PaymentResponse> responses = new ArrayList<>();
        private final AtomicInteger receivedCount = new AtomicInteger(0);
        private final Instant startTime = Instant.now();
        
        public void addResponse(PaymentResponse response) {
            responses.add(response);
            receivedCount.incrementAndGet();
        }
        
        public boolean isComplete() {
            return receivedCount.get() >= totalCount;
        }
        
        public BatchResponseAggregate createAggregate() {
            long successCount = responses.stream()
                .filter(r -> r.getStatus() == PaymentStatus.COMPLETED)
                .count();
            long failureCount = responses.size() - successCount;
            
            return new BatchResponseAggregate(
                batchId,
                responses,
                successCount,
                failureCount,
                Duration.between(startTime, Instant.now())
            );
        }
    }
}
```

**Aggregation Timeout**:
```java
@Scheduled(fixedRate = 60000) // Every 60 seconds
public void checkAggregationTimeouts() {
    Instant timeout = Instant.now().minus(Duration.ofMinutes(5));
    
    aggregationStore.entrySet().stream()
        .filter(e -> e.getValue().getStartTime().isBefore(timeout))
        .forEach(e -> {
            log.warn("Batch {} aggregation timed out: {}/{} responses received",
                e.getKey(), 
                e.getValue().getReceivedCount(), 
                e.getValue().getTotalCount());
            
            // Publish partial aggregate
            publishPartialBatchResult(e.getValue().createAggregate());
            
            // Clean up
            aggregationStore.remove(e.getKey());
        });
}
```

---

### 11. Resequencer

**Intent**: Process messages in the correct order.

**Used In**: Batch payment processing, ordered event replay

**Implementation**:

```java
@Service
public class PaymentResequencer {
    
    private final Map<String, ResequencerBuffer> buffers = new ConcurrentHashMap<>();
    
    @ServiceBusListener(destination = "payment.ordered")
    public void resequence(ServiceBusReceivedMessage message) {
        String batchId = message.getApplicationProperties().get("batchId").toString();
        int sequenceNumber = (int) message.getApplicationProperties().get("sequenceNumber");
        
        PaymentInstruction payment = fromJson(message.getBody(), PaymentInstruction.class);
        
        // Get or create resequencer buffer for this batch
        ResequencerBuffer buffer = buffers.computeIfAbsent(batchId, 
            k -> new ResequencerBuffer(batchId));
        
        // Add message to buffer
        buffer.add(sequenceNumber, payment);
        
        // Process messages in sequence
        while (buffer.hasNext()) {
            PaymentInstruction orderedPayment = buffer.getNext();
            processInOrder(orderedPayment);
        }
    }
    
    @Data
    static class ResequencerBuffer {
        private final String batchId;
        private final TreeMap<Integer, PaymentInstruction> buffer = new TreeMap<>();
        private int nextExpectedSequence = 1;
        
        public void add(int sequenceNumber, PaymentInstruction payment) {
            buffer.put(sequenceNumber, payment);
        }
        
        public boolean hasNext() {
            return buffer.containsKey(nextExpectedSequence);
        }
        
        public PaymentInstruction getNext() {
            PaymentInstruction payment = buffer.remove(nextExpectedSequence);
            nextExpectedSequence++;
            return payment;
        }
    }
}
```

---

### 12. Scatter-Gather

**Intent**: Broadcast a message to multiple recipients and aggregate replies.

**Used In**: Multi-account balance check, parallel fraud checks

**Implementation**:

```java
@Service
public class MultiAccountBalanceChecker {
    
    public CompletableFuture<BalanceCheckAggregate> checkAllAccounts(
        List<String> accountIds, String customerId) {
        
        String correlationId = UUID.randomUUID().toString();
        
        // SCATTER: Send balance check requests to all accounts
        List<CompletableFuture<AccountBalance>> futures = accountIds.stream()
            .map(accountId -> checkAccountBalance(accountId, correlationId))
            .collect(Collectors.toList());
        
        // GATHER: Wait for all responses and aggregate
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
            .thenApply(v -> {
                List<AccountBalance> balances = futures.stream()
                    .map(CompletableFuture::join)
                    .collect(Collectors.toList());
                
                return new BalanceCheckAggregate(
                    customerId,
                    balances,
                    balances.stream().map(AccountBalance::getBalance).reduce(BigDecimal.ZERO, BigDecimal::add)
                );
            })
            .orTimeout(5, TimeUnit.SECONDS)
            .exceptionally(ex -> {
                log.error("Balance check scatter-gather failed for customer: {}", customerId, ex);
                return BalanceCheckAggregate.failed(customerId, ex.getMessage());
            });
    }
    
    private CompletableFuture<AccountBalance> checkAccountBalance(
        String accountId, String correlationId) {
        
        return CompletableFuture.supplyAsync(() -> {
            // Call external core banking system
            return accountAdapterClient.checkBalance(accountId, correlationId);
        }, executorService);
    }
}
```

---

## Message Transformation Patterns

### 13. Envelope Wrapper

**Intent**: Wrap messages with routing/technical metadata.

**Used In**: All message publishing

**Implementation**:

```java
@Service
public class MessageEnvelopeWrapper {
    
    public ServiceBusMessage wrapWithEnvelope(Object payload, MessageMetadata metadata) {
        // Create envelope
        MessageEnvelope envelope = MessageEnvelope.builder()
            .messageId(UUID.randomUUID().toString())
            .correlationId(metadata.getCorrelationId())
            .timestamp(Instant.now())
            .source("payment-service")
            .version("1.0")
            .tenantId(metadata.getTenantId())
            .payload(payload)
            .build();
        
        ServiceBusMessage message = new ServiceBusMessage(toJson(envelope));
        
        // Add technical metadata to message properties
        message.setMessageId(envelope.getMessageId());
        message.setCorrelationId(envelope.getCorrelationId());
        message.setContentType("application/json");
        message.setSubject(payload.getClass().getSimpleName());
        
        // Application properties (indexed for filtering)
        message.getApplicationProperties().put("messageType", payload.getClass().getSimpleName());
        message.getApplicationProperties().put("version", envelope.getVersion());
        message.getApplicationProperties().put("tenantId", envelope.getTenantId());
        message.getApplicationProperties().put("source", envelope.getSource());
        message.getApplicationProperties().put("timestamp", envelope.getTimestamp().toString());
        
        return message;
    }
    
    public <T> T unwrapEnvelope(ServiceBusReceivedMessage message, Class<T> payloadType) {
        MessageEnvelope envelope = fromJson(message.getBody(), MessageEnvelope.class);
        
        // Validate envelope
        validateEnvelope(envelope);
        
        // Extract payload
        return objectMapper.convertValue(envelope.getPayload(), payloadType);
    }
}

@Data
@Builder
class MessageEnvelope {
    private String messageId;
    private String correlationId;
    private Instant timestamp;
    private String source;
    private String version;
    private String tenantId;
    private Object payload; // Actual business data
}
```

---

### 14. Content Enricher

**Intent**: Add missing data to a message.

**Used In**: Enrich payment with customer details, account info

**Implementation**:

```java
@Service
public class PaymentEnricher {
    
    @Autowired
    private CustomerService customerService;
    
    @Autowired
    private AccountService accountService;
    
    @ServiceBusListener(destination = "payment.enrichment")
    public void enrichPayment(ServiceBusReceivedMessage message) {
        PaymentInstruction payment = fromJson(message.getBody(), PaymentInstruction.class);
        
        // Enrich with customer details
        CustomerDetails customer = customerService.getCustomer(payment.getCustomerId());
        payment.setCustomerName(customer.getFullName());
        payment.setCustomerEmail(customer.getEmail());
        payment.setCustomerPhone(customer.getPhone());
        payment.setKycStatus(customer.getKycStatus());
        
        // Enrich with account details
        AccountDetails account = accountService.getAccount(payment.getDebitAccountId());
        payment.setAccountType(account.getAccountType());
        payment.setAccountBranch(account.getBranchCode());
        payment.setCurrency(account.getCurrency());
        
        // Forward enriched payment
        forwardEnrichedPayment(payment);
        
        log.info("Enriched payment {} with customer and account details", 
            payment.getPaymentId());
    }
}
```

---

### 15. Claim Check

**Intent**: Store large payload separately, pass reference.

**Used In**: Large batch files, clearing system responses

**Implementation**:

```java
@Service
public class ClaimCheckPattern {
    
    @Autowired
    private BlobStorageClient blobStorage;
    
    private static final int MAX_MESSAGE_SIZE = 256 * 1024; // 256 KB
    
    public ServiceBusMessage createMessageWithClaimCheck(Object payload) {
        String payloadJson = toJson(payload);
        
        // Check if payload is too large
        if (payloadJson.getBytes().length > MAX_MESSAGE_SIZE) {
            // Store payload in blob storage
            String claimCheckId = UUID.randomUUID().toString();
            String blobUrl = blobStorage.upload(claimCheckId, payloadJson);
            
            // Create claim check reference
            ClaimCheck claimCheck = ClaimCheck.builder()
                .claimCheckId(claimCheckId)
                .blobUrl(blobUrl)
                .size(payloadJson.getBytes().length)
                .contentType("application/json")
                .expiresAt(Instant.now().plus(Duration.ofHours(24)))
                .build();
            
            // Send small claim check instead of large payload
            ServiceBusMessage message = new ServiceBusMessage(toJson(claimCheck));
            message.getApplicationProperties().put("isClaimCheck", true);
            message.getApplicationProperties().put("claimCheckId", claimCheckId);
            
            log.info("Created claim check {} for large payload ({} bytes)", 
                claimCheckId, payloadJson.getBytes().length);
            
            return message;
        } else {
            // Payload is small enough, send directly
            ServiceBusMessage message = new ServiceBusMessage(payloadJson);
            message.getApplicationProperties().put("isClaimCheck", false);
            return message;
        }
    }
    
    public <T> T retrievePayload(ServiceBusReceivedMessage message, Class<T> type) {
        boolean isClaimCheck = (boolean) message.getApplicationProperties()
            .getOrDefault("isClaimCheck", false);
        
        if (isClaimCheck) {
            // Retrieve actual payload from blob storage
            ClaimCheck claimCheck = fromJson(message.getBody(), ClaimCheck.class);
            String payloadJson = blobStorage.download(claimCheck.getBlobUrl());
            
            log.info("Retrieved payload from claim check: {}", claimCheck.getClaimCheckId());
            
            return fromJson(payloadJson, type);
        } else {
            // Payload is in message body
            return fromJson(message.getBody(), type);
        }
    }
}

@Data
@Builder
class ClaimCheck {
    private String claimCheckId;
    private String blobUrl;
    private long size;
    private String contentType;
    private Instant expiresAt;
}
```

---

### 16. Normalizer

**Intent**: Convert messages from different formats to canonical format.

**Used In**: Multiple clearing systems with different message formats

**Implementation**:

```java
@Service
public class ClearingMessageNormalizer {
    
    public CanonicalPaymentMessage normalize(Object externalMessage, ClearingSystem source) {
        return switch (source) {
            case SAMOS -> normalizeSAMOS((SAMOSMessage) externalMessage);
            case BANKSERV -> normalizeBankserv((BankservMessage) externalMessage);
            case RTC -> normalizeRTC((RTCMessage) externalMessage);
            case PAYSHAP -> normalizePayShap((PayShapMessage) externalMessage);
            case SWIFT -> normalizeSWIFT((SWIFTMessage) externalMessage);
        };
    }
    
    private CanonicalPaymentMessage normalizeSAMOS(SAMOSMessage samosmsg) {
        // ISO 20022 pacs.008 format
        return CanonicalPaymentMessage.builder()
            .paymentId(samosmsg.getMsgId())
            .amount(samosmsg.getIntrBkSttlmAmt().getValue())
            .currency(samosmsg.getIntrBkSttlmAmt().getCurrency())
            .debtorAccount(samosmsg.getDbtr().getAcct().getId())
            .creditorAccount(samosmsg.getCdtr().getAcct().getId())
            .debtorName(samosmsg.getDbtr().getNm())
            .creditorName(samosmsg.getCdtr().getNm())
            .reference(samosmsg.getEndToEndId())
            .clearingSystem(ClearingSystem.SAMOS)
            .timestamp(samosmsg.getCreDtTm())
            .build();
    }
    
    private CanonicalPaymentMessage normalizeBankserv(BankservMessage bsmsg) {
        // ISO 8583 / Proprietary format
        return CanonicalPaymentMessage.builder()
            .paymentId(bsmsg.getField37()) // Retrieval Reference Number
            .amount(new BigDecimal(bsmsg.getField4())) // Transaction Amount
            .currency("ZAR")
            .debtorAccount(bsmsg.getField102()) // From Account
            .creditorAccount(bsmsg.getField103()) // To Account
            .reference(bsmsg.getField48()) // Additional Data
            .clearingSystem(ClearingSystem.BANKSERV)
            .timestamp(parseISO8583DateTime(bsmsg.getField7()))
            .build();
    }
    
    private CanonicalPaymentMessage normalizePayShap(PayShapMessage psmsg) {
        // ISO 20022 pacs.008 format
        return CanonicalPaymentMessage.builder()
            .paymentId(psmsg.getPmtId().getTxId())
            .amount(psmsg.getInstdAmt().getValue())
            .currency(psmsg.getInstdAmt().getCcy())
            .debtorAccount(resolveProxyToAccount(psmsg.getDbtrPrxy())) // Mobile/Email
            .creditorAccount(resolveProxyToAccount(psmsg.getCdtrPrxy()))
            .debtorName(psmsg.getDbtr().getNm())
            .creditorName(psmsg.getCdtr().getNm())
            .reference(psmsg.getRmtInf().getUstrd())
            .clearingSystem(ClearingSystem.PAYSHAP)
            .timestamp(psmsg.getCreDtTm())
            .build();
    }
    
    private CanonicalPaymentMessage normalizeSWIFT(SWIFTMessage swiftmsg) {
        // MT103 or ISO 20022 pacs.008
        if (swiftmsg.getFormat() == SWIFTFormat.MT103) {
            return normalizeMT103((MT103Message) swiftmsg.getPayload());
        } else {
            return normalizeISO20022((ISO20022Message) swiftmsg.getPayload());
        }
    }
}

@Data
@Builder
class CanonicalPaymentMessage {
    // Unified format for internal processing
    private String paymentId;
    private BigDecimal amount;
    private String currency;
    private String debtorAccount;
    private String creditorAccount;
    private String debtorName;
    private String creditorName;
    private String reference;
    private ClearingSystem clearingSystem;
    private Instant timestamp;
}
```

---

## Message Endpoint Patterns

### 17. Event-Driven Consumer

**Intent**: Automatically consume messages as they arrive.

**Used In**: All event listeners in microservices

**Implementation**:

```java
@Service
@Slf4j
public class PaymentEventConsumer {
    
    @Autowired
    private PaymentProcessor paymentProcessor;
    
    // Event-driven consumer (push-based)
    @ServiceBusListener(
        destination = "payment.events",
        concurrency = "5-10" // 5-10 concurrent consumers
    )
    public void onPaymentEvent(
        ServiceBusReceivedMessage message,
        ServiceBusReceiverClient receiverClient
    ) {
        try {
            PaymentEvent event = fromJson(message.getBody(), PaymentEvent.class);
            
            // Process event
            paymentProcessor.process(event);
            
            // Complete message (acknowledge)
            receiverClient.complete(message);
            
            log.info("Processed payment event: {}", event.getPaymentId());
            
        } catch (BusinessException ex) {
            // Business error: Dead letter (won't retry)
            receiverClient.deadLetter(message, 
                "BusinessValidationFailed", 
                ex.getMessage());
            
            log.error("Business validation failed, dead lettered: {}", 
                message.getMessageId(), ex);
            
        } catch (TransientException ex) {
            // Transient error: Abandon (will retry)
            receiverClient.abandon(message);
            
            log.warn("Transient error, abandoned for retry: {}", 
                message.getMessageId(), ex);
            
        } catch (Exception ex) {
            // Unknown error: Abandon with delay
            receiverClient.abandon(message);
            
            log.error("Unexpected error processing message: {}", 
                message.getMessageId(), ex);
        }
    }
}
```

---

### 18. Polling Consumer

**Intent**: Explicitly poll for messages (pull-based).

**Used In**: Batch file processing, scheduled tasks

**Implementation**:

```java
@Service
public class BatchFilePollingConsumer {
    
    @Autowired
    private ServiceBusReceiverClient batchQueueReceiver;
    
    @Scheduled(fixedDelay = 10000) // Poll every 10 seconds
    public void pollForBatchFiles() {
        try {
            // Poll for messages (max 10 messages, 5 second timeout)
            IterableStream<ServiceBusReceivedMessage> messages = 
                batchQueueReceiver.receiveMessages(10, Duration.ofSeconds(5));
            
            for (ServiceBusReceivedMessage message : messages) {
                try {
                    BatchPaymentFile batchFile = fromJson(message.getBody(), BatchPaymentFile.class);
                    
                    // Process batch file
                    processBatchFile(batchFile);
                    
                    // Complete message
                    batchQueueReceiver.complete(message);
                    
                    log.info("Processed batch file: {}", batchFile.getBatchId());
                    
                } catch (Exception ex) {
                    batchQueueReceiver.abandon(message);
                    log.error("Error processing batch file: {}", message.getMessageId(), ex);
                }
            }
            
        } catch (Exception ex) {
            log.error("Error polling for batch files", ex);
        }
    }
}
```

---

### 19. Idempotent Receiver

**Intent**: Handle duplicate messages safely.

**Used In**: All payment processing services (critical!)

**Implementation**:

```java
@Service
public class IdempotentPaymentProcessor {
    
    @Autowired
    private IdempotencyStore idempotencyStore; // Redis or Database
    
    @ServiceBusListener(destination = "payment.processing")
    public void processPayment(ServiceBusReceivedMessage message) {
        String idempotencyKey = message.getApplicationProperties()
            .get("idempotencyKey").toString();
        
        // Check if already processed
        IdempotencyRecord existing = idempotencyStore.get(idempotencyKey);
        
        if (existing != null) {
            if (existing.getStatus() == ProcessingStatus.COMPLETED) {
                // Already processed successfully, return cached result
                log.info("Duplicate message detected (already completed): {}", idempotencyKey);
                replyWithCachedResult(message, existing.getResult());
                return;
                
            } else if (existing.getStatus() == ProcessingStatus.IN_PROGRESS) {
                // Currently processing, reject duplicate
                log.warn("Duplicate message detected (in progress): {}", idempotencyKey);
                throw new DuplicateProcessingException("Payment is currently being processed");
                
            } else if (existing.getStatus() == ProcessingStatus.FAILED) {
                // Previous attempt failed, allow retry
                log.info("Retrying previously failed payment: {}", idempotencyKey);
            }
        }
        
        // Record that we're processing this message
        idempotencyStore.set(idempotencyKey, IdempotencyRecord.inProgress());
        
        try {
            PaymentInstruction payment = fromJson(message.getBody(), PaymentInstruction.class);
            
            // Process payment (actual business logic)
            PaymentResult result = paymentService.processPayment(payment);
            
            // Record successful completion with result
            idempotencyStore.set(idempotencyKey, 
                IdempotencyRecord.completed(result));
            
            log.info("Payment processed successfully: {}", payment.getPaymentId());
            
        } catch (Exception ex) {
            // Record failure
            idempotencyStore.set(idempotencyKey, 
                IdempotencyRecord.failed(ex.getMessage()));
            
            throw ex;
        }
    }
}

@Data
class IdempotencyRecord {
    private String idempotencyKey;
    private ProcessingStatus status;
    private Object result;
    private String errorMessage;
    private Instant createdAt;
    private Instant expiresAt;
    
    public static IdempotencyRecord inProgress() {
        IdempotencyRecord record = new IdempotencyRecord();
        record.status = ProcessingStatus.IN_PROGRESS;
        record.createdAt = Instant.now();
        record.expiresAt = Instant.now().plus(Duration.ofHours(24));
        return record;
    }
    
    public static IdempotencyRecord completed(Object result) {
        IdempotencyRecord record = new IdempotencyRecord();
        record.status = ProcessingStatus.COMPLETED;
        record.result = result;
        record.createdAt = Instant.now();
        record.expiresAt = Instant.now().plus(Duration.ofDays(7));
        return record;
    }
    
    public static IdempotencyRecord failed(String errorMessage) {
        IdempotencyRecord record = new IdempotencyRecord();
        record.status = ProcessingStatus.FAILED;
        record.errorMessage = errorMessage;
        record.createdAt = Instant.now();
        record.expiresAt = Instant.now().plus(Duration.ofHours(1));
        return record;
    }
}

enum ProcessingStatus {
    IN_PROGRESS,
    COMPLETED,
    FAILED
}
```

**Idempotency Store (Redis)**:
```java
@Service
public class RedisIdempotencyStore implements IdempotencyStore {
    
    @Autowired
    private RedisTemplate<String, IdempotencyRecord> redisTemplate;
    
    private static final String KEY_PREFIX = "idempotency:";
    
    @Override
    public IdempotencyRecord get(String idempotencyKey) {
        return redisTemplate.opsForValue().get(KEY_PREFIX + idempotencyKey);
    }
    
    @Override
    public void set(String idempotencyKey, IdempotencyRecord record) {
        Duration ttl = Duration.between(Instant.now(), record.getExpiresAt());
        redisTemplate.opsForValue().set(
            KEY_PREFIX + idempotencyKey, 
            record, 
            ttl
        );
    }
}
```

---

### 20. Competing Consumers

**Intent**: Multiple consumers compete for messages (load balancing).

**Used In**: All high-throughput services

**Implementation**:

```java
@Configuration
public class CompetingConsumerConfig {
    
    @Bean
    public ServiceBusProcessorClient paymentProcessor(
        ServiceBusClientBuilder clientBuilder) {
        
        return clientBuilder
            .processor()
            .queueName("payment.processing")
            .prefetchCount(10) // Pre-fetch 10 messages per consumer
            .maxConcurrentCalls(5) // 5 concurrent message handlers per consumer
            .processMessage(this::processPayment)
            .processError(this::handleError)
            .buildProcessorClient();
    }
}

// Deploy multiple instances of this service
// Each instance = competing consumer
// Azure Service Bus automatically distributes messages across instances
```

**Scaling Strategy**:
```yaml
# Kubernetes HPA (Horizontal Pod Autoscaler)
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: payment-processor-hpa
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: payment-processor
  minReplicas: 3
  maxReplicas: 20
  metrics:
  - type: External
    external:
      metric:
        name: azure_service_bus_queue_length
        selector:
          matchLabels:
            queue: "payment.processing"
      target:
        type: AverageValue
        averageValue: "100" # Scale when queue length > 100 per pod
```

---

### 21. Durable Subscriber

**Intent**: Receive messages even when disconnected.

**Used In**: Audit service, reporting service

**Implementation**:

```java
@Service
public class AuditDurableSubscriber {
    
    @Bean
    public ServiceBusProcessorClient auditSubscriptionProcessor(
        ServiceBusClientBuilder clientBuilder) {
        
        return clientBuilder
            .processor()
            .topicName("payment.events")
            .subscriptionName("audit-service-subscription") // Durable subscription
            .receiveMode(ServiceBusReceiveMode.PEEK_LOCK)
            .maxAutoLockRenewDuration(Duration.ofMinutes(5))
            .processMessage(this::auditMessage)
            .processError(this::handleError)
            .buildProcessorClient();
    }
    
    @ServiceBusListener(
        destination = "payment.events",
        subscription = "audit-service-subscription",
        isSubscription = true
    )
    public void auditMessage(ServiceBusReceivedMessage message) {
        // Even if audit service was down, messages are retained
        // Durable subscription ensures no messages are lost
        
        PaymentEvent event = fromJson(message.getBody(), PaymentEvent.class);
        auditStore.save(event);
        
        log.info("Audited event: {}", event.getEventId());
    }
}
```

**Subscription Configuration**:
```java
@Bean
public SubscriptionClient createDurableSubscription(
    ServiceBusAdministrationClient adminClient) {
    
    CreateSubscriptionOptions options = new CreateSubscriptionOptions()
        .setMaxDeliveryCount(10) // Retry up to 10 times
        .setLockDuration(Duration.ofMinutes(5))
        .setDefaultMessageTimeToLive(Duration.ofDays(7))
        .setDeadLetteringOnMessageExpiration(true)
        .setEnableDeadLetteringOnFilterEvaluationExceptions(true);
    
    // Create subscription with filter
    CreateRuleOptions filterRule = new CreateRuleOptions()
        .setFilter(new SqlRuleFilter("eventType IN ('PAYMENT_COMPLETED', 'PAYMENT_FAILED')"));
    
    adminClient.createSubscription("payment.events", "audit-service-subscription", options);
    adminClient.createRule("payment.events", "audit-service-subscription", "auditFilter", filterRule);
    
    return new SubscriptionClient(
        new ConnectionStringBuilder(connectionString, "payment.events/subscriptions/audit-service-subscription"),
        ReceiveMode.PEEKLOCK
    );
}
```

---

### 22. Transactional Client

**Intent**: Send/receive messages within a transaction.

**Used In**: Saga orchestrator, critical payment steps

**Implementation**:

```java
@Service
public class TransactionalPaymentProcessor {
    
    @Autowired
    private PaymentRepository paymentRepository;
    
    @Autowired
    private ServiceBusSenderClient messageSender;
    
    @Transactional
    public void processPaymentWithTransaction(PaymentInstruction payment) {
        try {
            // Step 1: Update database
            Payment entity = paymentRepository.findById(payment.getPaymentId())
                .orElseThrow();
            entity.setStatus(PaymentStatus.PROCESSING);
            paymentRepository.save(entity);
            
            // Step 2: Send message to next step
            ServiceBusMessage message = new ServiceBusMessage(toJson(payment));
            messageSender.sendMessage(message);
            
            // Both database and message operations succeed together
            log.info("Payment processed transactionally: {}", payment.getPaymentId());
            
        } catch (Exception ex) {
            // If either fails, both rollback
            log.error("Transaction failed, rolling back: {}", payment.getPaymentId(), ex);
            throw ex;
        }
    }
}
```

**Note**: Azure Service Bus doesn't support distributed transactions (XA). Use **Outbox Pattern** instead:

```java
@Service
public class OutboxPatternProcessor {
    
    @Transactional
    public void processPaymentWithOutbox(PaymentInstruction payment) {
        // Step 1: Update payment in database
        Payment entity = paymentRepository.findById(payment.getPaymentId())
            .orElseThrow();
        entity.setStatus(PaymentStatus.PROCESSING);
        paymentRepository.save(entity);
        
        // Step 2: Write message to outbox table (same transaction)
        OutboxMessage outboxMessage = OutboxMessage.builder()
            .id(UUID.randomUUID().toString())
            .aggregateId(payment.getPaymentId())
            .messageType("PaymentProcessed")
            .payload(toJson(payment))
            .destination("payment.next-step")
            .createdAt(Instant.now())
            .status(OutboxStatus.PENDING)
            .build();
        outboxRepository.save(outboxMessage);
        
        // Both updates happen in same database transaction (ACID)
    }
    
    // Separate process reads outbox and publishes messages
    @Scheduled(fixedDelay = 1000) // Every 1 second
    public void processOutbox() {
        List<OutboxMessage> pending = outboxRepository.findByStatus(OutboxStatus.PENDING);
        
        for (OutboxMessage outboxMessage : pending) {
            try {
                // Publish to Service Bus
                ServiceBusMessage message = new ServiceBusMessage(outboxMessage.getPayload());
                message.setMessageId(outboxMessage.getId());
                messageSender.sendMessage(message);
                
                // Mark as published
                outboxMessage.setStatus(OutboxStatus.PUBLISHED);
                outboxMessage.setPublishedAt(Instant.now());
                outboxRepository.save(outboxMessage);
                
                log.info("Published outbox message: {}", outboxMessage.getId());
                
            } catch (Exception ex) {
                // Retry on next poll
                log.error("Failed to publish outbox message: {}", outboxMessage.getId(), ex);
            }
        }
    }
}
```

---

## System Management Patterns

### 23. Control Bus

**Intent**: Manage and monitor integration infrastructure.

**Used In**: Health checks, graceful shutdown, configuration updates

**Implementation**:

```java
@Service
public class ControlBusService {
    
    @Autowired
    private List<ServiceBusProcessorClient> allProcessors;
    
    @ServiceBusListener(destination = "control.bus")
    public void handleControlMessage(ServiceBusReceivedMessage message) {
        String command = message.getApplicationProperties().get("command").toString();
        
        switch (command) {
            case "STOP_PROCESSING":
                stopAllProcessors();
                break;
            case "START_PROCESSING":
                startAllProcessors();
                break;
            case "HEALTH_CHECK":
                performHealthCheck();
                break;
            case "UPDATE_CONFIG":
                updateConfiguration(message.getBody());
                break;
            case "DRAIN_QUEUE":
                drainQueue(message.getApplicationProperties().get("queueName").toString());
                break;
            default:
                log.warn("Unknown control command: {}", command);
        }
    }
    
    private void stopAllProcessors() {
        allProcessors.forEach(processor -> {
            processor.stop();
            log.info("Stopped processor: {}", processor.getIdentifier());
        });
    }
    
    private void startAllProcessors() {
        allProcessors.forEach(processor -> {
            processor.start();
            log.info("Started processor: {}", processor.getIdentifier());
        });
    }
}
```

---

### 24. Wire Tap

**Intent**: Inspect messages without affecting flow.

**Used In**: Monitoring, debugging, audit

**Implementation**:

```java
@Service
public class MessageWireTap {
    
    @Autowired
    private ServiceBusSenderClient monitoringChannel;
    
    @Around("@annotation(WireTap)")
    public Object tapMessage(ProceedingJoinPoint joinPoint) throws Throwable {
        Object[] args = joinPoint.getArgs();
        
        // Find the message argument
        for (Object arg : args) {
            if (arg instanceof ServiceBusReceivedMessage) {
                ServiceBusReceivedMessage message = (ServiceBusReceivedMessage) arg;
                
                // Tap: Send copy to monitoring channel
                tapToMonitoring(message);
            }
        }
        
        // Continue processing original message
        return joinPoint.proceed();
    }
    
    private void tapToMonitoring(ServiceBusReceivedMessage originalMessage) {
        try {
            // Create monitoring message
            WireTapMessage tapMessage = WireTapMessage.builder()
                .originalMessageId(originalMessage.getMessageId())
                .correlationId(originalMessage.getCorrelationId())
                .subject(originalMessage.getSubject())
                .size(originalMessage.getBody().toBytes().length)
                .enqueuedTime(originalMessage.getEnqueuedTime())
                .deliveryCount(originalMessage.getDeliveryCount())
                .properties(originalMessage.getApplicationProperties())
                .tappedAt(Instant.now())
                .build();
            
            // Send to monitoring channel (async, don't block)
            ServiceBusMessage message = new ServiceBusMessage(toJson(tapMessage));
            monitoringChannel.sendMessage(message);
            
        } catch (Exception ex) {
            // Wire tap failure should not affect main flow
            log.error("Wire tap failed (ignoring): {}", originalMessage.getMessageId(), ex);
        }
    }
}

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface WireTap {
}

// Usage
@Service
public class PaymentProcessor {
    
    @WireTap // Automatically taps all messages
    @ServiceBusListener(destination = "payment.processing")
    public void processPayment(ServiceBusReceivedMessage message) {
        // Process payment
    }
}
```

---

### 25. Message Store

**Intent**: Store messages for auditing, replay, debugging.

**Used In**: Event sourcing, audit trail, message replay

**Implementation**:

```java
@Service
public class MessageStore {
    
    @Autowired
    private CosmosClient cosmosClient;
    
    private static final String DATABASE_ID = "payment-engine";
    private static final String CONTAINER_ID = "message-store";
    
    public void store(ServiceBusReceivedMessage message) {
        StoredMessage storedMessage = StoredMessage.builder()
            .id(message.getMessageId())
            .messageId(message.getMessageId())
            .correlationId(message.getCorrelationId())
            .subject(message.getSubject())
            .contentType(message.getContentType())
            .body(new String(message.getBody().toBytes()))
            .applicationProperties(message.getApplicationProperties())
            .enqueuedTime(message.getEnqueuedTime())
            .deliveryCount(message.getDeliveryCount())
            .sequenceNumber(message.getSequenceNumber())
            .partitionKey(message.getApplicationProperties().get("tenantId").toString())
            .storedAt(Instant.now())
            .ttl(7 * 24 * 60 * 60) // 7 days TTL
            .build();
        
        cosmosClient.getDatabase(DATABASE_ID)
            .getContainer(CONTAINER_ID)
            .createItem(storedMessage);
        
        log.info("Stored message in message store: {}", message.getMessageId());
    }
    
    public List<StoredMessage> replay(String correlationId) {
        String query = "SELECT * FROM c WHERE c.correlationId = '" + correlationId + "' ORDER BY c.enqueuedTime";
        
        return cosmosClient.getDatabase(DATABASE_ID)
            .getContainer(CONTAINER_ID)
            .queryItems(query, new CosmosQueryRequestOptions(), StoredMessage.class)
            .stream()
            .collect(Collectors.toList());
    }
}

@Data
@Builder
class StoredMessage {
    private String id; // Cosmos DB document ID
    private String messageId;
    private String correlationId;
    private String subject;
    private String contentType;
    private String body;
    private Map<String, Object> applicationProperties;
    private OffsetDateTime enqueuedTime;
    private long deliveryCount;
    private long sequenceNumber;
    private String partitionKey;
    private Instant storedAt;
    private int ttl; // Time-to-live in seconds
}
```

---

### 26. Dead Letter Channel

**Intent**: Store messages that couldn't be processed.

**Used In**: Error handling, message recovery

**Implementation**:

```java
@Service
public class DeadLetterHandler {
    
    @Autowired
    private ServiceBusReceiverClient deadLetterReceiver;
    
    @Autowired
    private AlertingService alertingService;
    
    // Monitor dead letter queue
    @Scheduled(fixedDelay = 60000) // Every 60 seconds
    public void monitorDeadLetterQueue() {
        IterableStream<ServiceBusReceivedMessage> deadLetters = 
            deadLetterReceiver.receiveMessages(100, Duration.ofSeconds(10));
        
        List<DeadLetterMessage> messages = new ArrayList<>();
        
        for (ServiceBusReceivedMessage message : deadLetters) {
            DeadLetterMessage dlm = DeadLetterMessage.builder()
                .messageId(message.getMessageId())
                .correlationId(message.getCorrelationId())
                .subject(message.getSubject())
                .enqueuedTime(message.getEnqueuedTime())
                .deliveryCount(message.getDeliveryCount())
                .deadLetterReason(message.getDeadLetterReason())
                .deadLetterErrorDescription(message.getDeadLetterErrorDescription())
                .body(new String(message.getBody().toBytes()))
                .build();
            
            messages.add(dlm);
            
            // Log dead letter
            log.error("Dead letter message: {} - Reason: {} - Description: {}",
                message.getMessageId(),
                message.getDeadLetterReason(),
                message.getDeadLetterErrorDescription());
        }
        
        // Alert if dead letters found
        if (!messages.isEmpty()) {
            alertingService.sendAlert(
                AlertSeverity.HIGH,
                "Dead Letter Queue Alert",
                messages.size() + " messages in dead letter queue"
            );
        }
    }
    
    // Reprocess dead letter message
    public void reprocessDeadLetter(String messageId) {
        ServiceBusReceivedMessage message = deadLetterReceiver.receiveMessages(1)
            .stream()
            .filter(m -> m.getMessageId().equals(messageId))
            .findFirst()
            .orElseThrow(() -> new MessageNotFoundException(messageId));
        
        try {
            // Clone message
            ServiceBusMessage reprocessed = new ServiceBusMessage(message.getBody());
            reprocessed.setMessageId(UUID.randomUUID().toString());
            reprocessed.setCorrelationId(message.getCorrelationId());
            reprocessed.setContentType(message.getContentType());
            reprocessed.setSubject(message.getSubject());
            message.getApplicationProperties().forEach((k, v) -> 
                reprocessed.getApplicationProperties().put(k, v));
            reprocessed.getApplicationProperties().put("reprocessed", true);
            reprocessed.getApplicationProperties().put("originalMessageId", message.getMessageId());
            
            // Send to original queue for reprocessing
            String originalQueue = message.getApplicationProperties().get("originalQueue").toString();
            sendToQueue(originalQueue, reprocessed);
            
            // Complete dead letter message
            deadLetterReceiver.complete(message);
            
            log.info("Reprocessed dead letter message: {}", messageId);
            
        } catch (Exception ex) {
            log.error("Failed to reprocess dead letter message: {}", messageId, ex);
            throw ex;
        }
    }
}
```

---

### 27. Invalid Message Channel

**Intent**: Route invalid messages separately.

**Used In**: Message validation failures

**Implementation**:

```java
@Service
public class InvalidMessageHandler {
    
    @Autowired
    private ServiceBusSenderClient invalidMessageChannel;
    
    @ServiceBusListener(destination = "payment.validation")
    public void validateMessage(ServiceBusReceivedMessage message, 
                                ServiceBusReceiverClient receiverClient) {
        try {
            // Validate message structure
            ValidationResult validation = validateMessageStructure(message);
            
            if (!validation.isValid()) {
                // Move to invalid message channel
                ServiceBusMessage invalidMsg = new ServiceBusMessage(message.getBody());
                invalidMsg.getApplicationProperties().putAll(message.getApplicationProperties());
                invalidMsg.getApplicationProperties().put("validationErrors", validation.getErrors());
                invalidMsg.getApplicationProperties().put("invalidatedAt", Instant.now().toString());
                
                invalidMessageChannel.sendMessage(invalidMsg);
                
                // Complete original message (remove from queue)
                receiverClient.complete(message);
                
                log.warn("Invalid message moved to invalid channel: {} - Errors: {}",
                    message.getMessageId(), validation.getErrors());
                
                return;
            }
            
            // Valid message, process normally
            processValidMessage(message);
            receiverClient.complete(message);
            
        } catch (Exception ex) {
            receiverClient.abandon(message);
            throw ex;
        }
    }
    
    private ValidationResult validateMessageStructure(ServiceBusReceivedMessage message) {
        List<String> errors = new ArrayList<>();
        
        // Required properties
        if (!message.getApplicationProperties().containsKey("messageType")) {
            errors.add("Missing required property: messageType");
        }
        if (!message.getApplicationProperties().containsKey("tenantId")) {
            errors.add("Missing required property: tenantId");
        }
        if (message.getCorrelationId() == null) {
            errors.add("Missing correlation ID");
        }
        
        // Validate JSON structure
        try {
            String json = new String(message.getBody().toBytes());
            objectMapper.readTree(json); // Parse to verify valid JSON
        } catch (Exception ex) {
            errors.add("Invalid JSON structure: " + ex.getMessage());
        }
        
        return errors.isEmpty() 
            ? ValidationResult.valid() 
            : ValidationResult.invalid(errors);
    }
}
```

---

## Pattern Implementation Matrix

| Pattern | Implementation | Service(s) | Azure Service Bus / Kafka |
|---------|----------------|------------|---------------------------|
| **Command Message** | ✅ Implemented | All services | Queue / Topic |
| **Event Message** | ✅ Implemented | All services | Topic / Event Hub |
| **Document Message** | ✅ Implemented | Batch Processing | Queue |
| **Correlation Identifier** | ✅ Implemented | All services | Message Properties |
| **Return Address** | ✅ Implemented | Fraud Scoring | ReplyTo |
| **Message Expiration** | ✅ Implemented | Payment Initiation | TTL |
| **Content-Based Router** | ✅ Implemented | Routing Service | Custom Logic |
| **Message Filter** | ✅ Implemented | All consumers | Subscription Filters |
| **Splitter** | ✅ Implemented | Batch Processing | Custom Logic |
| **Aggregator** | ✅ Implemented | Batch Processing | Custom Logic + Redis |
| **Resequencer** | ✅ Implemented | Batch Processing | Custom Logic |
| **Scatter-Gather** | ✅ Implemented | Account Adapter | CompletableFuture |
| **Envelope Wrapper** | ✅ Implemented | All publishers | Custom Logic |
| **Content Enricher** | ✅ Implemented | Validation Service | Custom Logic |
| **Claim Check** | ✅ Implemented | Batch Processing | Blob Storage |
| **Normalizer** | ✅ Implemented | Clearing Adapters | Custom Logic |
| **Event-Driven Consumer** | ✅ Implemented | All services | @ServiceBusListener |
| **Polling Consumer** | ✅ Implemented | Batch Processing | @Scheduled |
| **Idempotent Receiver** | ✅ Implemented | All services | Redis Idempotency Store |
| **Competing Consumers** | ✅ Implemented | All services | Multiple Instances |
| **Durable Subscriber** | ✅ Implemented | Audit, Reporting | Subscriptions |
| **Transactional Client** | ✅ Implemented (Outbox) | Payment Processing | Outbox Pattern |
| **Control Bus** | ✅ Implemented | Platform Services | Control Queue |
| **Wire Tap** | ✅ Implemented | Monitoring | Monitoring Channel |
| **Message Store** | ✅ Implemented | Audit Service | CosmosDB |
| **Dead Letter Channel** | ✅ Implemented | All services | Built-in DLQ |
| **Invalid Message Channel** | ✅ Implemented | Validation Service | Invalid Queue |

---

## Azure Service Bus / Kafka Mapping

### Pattern Mapping

| EIP Pattern | Azure Service Bus | Kafka |
|-------------|-------------------|-------|
| **Point-to-Point Channel** | Queue | Topic (single consumer group) |
| **Publish-Subscribe Channel** | Topic + Subscriptions | Topic (multiple consumer groups) |
| **Dead Letter Channel** | Dead Letter Queue (built-in) | Custom topic |
| **Message Expiration** | TTL (Time-To-Live) | Retention policy |
| **Message Priority** | Not supported | Not supported (use separate topics) |
| **Message Groups** | Sessions | Partitions |
| **Guaranteed Delivery** | At-Least-Once | At-Least-Once (Kafka default) |
| **Exactly-Once Delivery** | Idempotency + Deduplication | Exactly-Once Semantics (EOS) |
| **Message Ordering** | Sessions / Partitioning | Partitions (within partition) |
| **Durable Subscription** | Subscription (always durable) | Consumer group offset |
| **Message Filter** | SQL Filters on Subscription | Custom consumer logic |
| **Correlation Identifier** | CorrelationId property | Message header |
| **Return Address** | ReplyTo property | Custom header |

---

## Code Examples

### Complete Payment Flow with EIP Patterns

```java
/**
 * End-to-end payment flow demonstrating multiple EIP patterns
 */
@Service
@Slf4j
public class PaymentFlowOrchestrator {
    
    @Autowired
    private ServiceBusSenderClient commandBus;
    
    @Autowired
    private ServiceBusSenderClient eventBus;
    
    @Autowired
    private IdempotencyStore idempotencyStore;
    
    @Autowired
    private MessageStore messageStore;
    
    /**
     * Step 1: Initiate Payment (Command Message + Correlation Identifier)
     */
    public void initiatePayment(PaymentRequest request) {
        String correlationId = UUID.randomUUID().toString();
        String idempotencyKey = request.getIdempotencyKey();
        
        // Idempotent Receiver Pattern
        if (idempotencyStore.exists(idempotencyKey)) {
            log.warn("Duplicate payment request: {}", idempotencyKey);
            return;
        }
        
        // Create command message
        InitiatePaymentCommand command = InitiatePaymentCommand.builder()
            .paymentId(correlationId)
            .customerId(request.getCustomerId())
            .amount(request.getAmount())
            .currency(request.getCurrency())
            .debitAccount(request.getDebitAccount())
            .creditAccount(request.getCreditAccount())
            .reference(request.getReference())
            .build();
        
        // Envelope Wrapper Pattern
        ServiceBusMessage message = wrapWithEnvelope(command);
        message.setCorrelationId(correlationId);
        message.getApplicationProperties().put("idempotencyKey", idempotencyKey);
        message.getApplicationProperties().put("commandType", "INITIATE_PAYMENT");
        
        // Message Store Pattern
        messageStore.store(message);
        
        // Send command
        commandBus.sendMessage(message);
        
        // Record idempotency
        idempotencyStore.set(idempotencyKey, IdempotencyRecord.inProgress());
        
        log.info("Payment initiated: {} (correlation: {})", request.getPaymentId(), correlationId);
    }
    
    /**
     * Step 2: Validate Payment (Content Enricher + Message Filter)
     */
    @ServiceBusListener(destination = "payment.commands")
    public void validatePayment(ServiceBusReceivedMessage message) {
        InitiatePaymentCommand command = unwrapEnvelope(message, InitiatePaymentCommand.class);
        String correlationId = message.getCorrelationId();
        
        // Content Enricher Pattern - enrich with customer details
        CustomerDetails customer = customerService.getCustomer(command.getCustomerId());
        command.setCustomerName(customer.getFullName());
        command.setCustomerEmail(customer.getEmail());
        command.setKycStatus(customer.getKycStatus());
        
        // Message Filter Pattern - filter out invalid customers
        if (customer.getKycStatus() != KycStatus.VERIFIED) {
            log.warn("Payment rejected - Customer not KYC verified: {}", command.getCustomerId());
            publishPaymentRejectedEvent(correlationId, "KYC_NOT_VERIFIED");
            return;
        }
        
        // Validate payment
        ValidationResult validation = paymentValidator.validate(command);
        
        if (validation.isValid()) {
            // Publish event (Event Message Pattern)
            publishPaymentValidatedEvent(correlationId, command);
        } else {
            // Invalid Message Channel Pattern
            sendToInvalidChannel(message, validation.getErrors());
        }
    }
    
    /**
     * Step 3: Route Payment (Content-Based Router)
     */
    @ServiceBusListener(destination = "payment.validated")
    public void routePayment(ServiceBusReceivedMessage message) {
        PaymentValidatedEvent event = fromJson(message.getBody(), PaymentValidatedEvent.class);
        String correlationId = message.getCorrelationId();
        
        // Content-Based Router Pattern
        ClearingSystem clearingSystem = determineRoute(event);
        
        // Create routing command
        RoutePaymentCommand command = RoutePaymentCommand.builder()
            .paymentId(event.getPaymentId())
            .clearingSystem(clearingSystem)
            .amount(event.getAmount())
            .currency(event.getCurrency())
            .build();
        
        // Send to appropriate clearing adapter
        String destination = "clearing." + clearingSystem.name().toLowerCase();
        sendCommandTo(destination, command, correlationId);
        
        log.info("Payment routed to {}: {}", clearingSystem, event.getPaymentId());
    }
    
    /**
     * Step 4: Process via Clearing (Normalizer)
     */
    @ServiceBusListener(destination = "clearing.samos")
    public void processSAMOS(ServiceBusReceivedMessage message) {
        RoutePaymentCommand command = unwrapEnvelope(message, RoutePaymentCommand.class);
        String correlationId = message.getCorrelationId();
        
        // Convert to SAMOS format
        SAMOSMessage samosMessage = convertToSAMOS(command);
        
        // Submit to SAMOS
        SAMOSResponse response = samosClient.submit(samosMessage);
        
        // Normalizer Pattern - convert response to canonical format
        CanonicalPaymentResponse canonical = normalizeSAMOSResponse(response);
        
        // Publish completion event
        publishPaymentCompletedEvent(correlationId, canonical);
    }
    
    /**
     * Step 5: Notify Customer (Return Address + Wire Tap)
     */
    @ServiceBusListener(destination = "payment.completed")
    @WireTap // Wire Tap Pattern - tap message for monitoring
    public void notifyCustomer(ServiceBusReceivedMessage message) {
        PaymentCompletedEvent event = fromJson(message.getBody(), PaymentCompletedEvent.class);
        
        // Get reply address from message
        String replyTo = message.getReplyTo(); // Return Address Pattern
        
        // Send notification
        NotificationRequest notification = NotificationRequest.builder()
            .customerId(event.getCustomerId())
            .channel(NotificationChannel.SMS)
            .templateId("PAYMENT_COMPLETED")
            .parameters(Map.of(
                "paymentId", event.getPaymentId(),
                "amount", event.getAmount(),
                "currency", event.getCurrency()
            ))
            .build();
        
        notificationService.sendNotification(notification);
        
        log.info("Customer notified: {}", event.getCustomerId());
    }
}
```

---

## Summary & Best Practices

### Pattern Selection Guide

| Scenario | Recommended Pattern(s) |
|----------|------------------------|
| **Sending commands** | Command Message + Correlation Identifier + Idempotent Receiver |
| **Broadcasting events** | Event Message + Publish-Subscribe Channel |
| **Large payloads** | Claim Check |
| **Multiple formats** | Normalizer + Canonical Data Model |
| **Ordered processing** | Resequencer + Message Groups (Sessions) |
| **Batch processing** | Splitter + Aggregator |
| **Routing by content** | Content-Based Router |
| **Filtering messages** | Message Filter |
| **Reliability** | Idempotent Receiver + Dead Letter Channel + Message Store |
| **Monitoring** | Wire Tap + Message Store |
| **Error handling** | Dead Letter Channel + Invalid Message Channel |
| **Transactions** | Outbox Pattern (not XA) |
| **Scalability** | Competing Consumers + Partitioning |

---

### Best Practices

#### 1. Always Use Correlation Identifier
```java
// GOOD ✅
message.setCorrelationId(requestId);
message.getApplicationProperties().put("X-Correlation-ID", requestId);

// BAD ❌
// No way to track related messages
```

#### 2. Make All Consumers Idempotent
```java
// GOOD ✅
if (idempotencyStore.exists(idempotencyKey)) {
    return cachedResult;
}

// BAD ❌
// Duplicate messages cause duplicate processing
```

#### 3. Use Envelope Wrapper for Metadata
```java
// GOOD ✅
ServiceBusMessage message = envelopeWrapper.wrap(payload, metadata);

// BAD ❌
// Mixing business data with technical metadata
```

#### 4. Implement Dead Letter Monitoring
```java
// GOOD ✅
@Scheduled(fixedDelay = 60000)
public void monitorDeadLetters() { ... }

// BAD ❌
// Dead letters accumulate unnoticed
```

#### 5. Use Claim Check for Large Payloads
```java
// GOOD ✅
if (size > MAX_SIZE) {
    return claimCheckPattern.store(payload);
}

// BAD ❌
// Large messages block queues
```

---

## Conclusion

The Payments Engine leverages **27 Enterprise Integration Patterns** to create a robust, scalable, and maintainable integration architecture:

✅ **Reliability**: Idempotent Receiver, Dead Letter Channel, Message Store  
✅ **Scalability**: Competing Consumers, Partitioning, Claim Check  
✅ **Flexibility**: Content-Based Router, Normalizer, Message Transformation  
✅ **Observability**: Wire Tap, Message Store, Control Bus  
✅ **Maintainability**: Canonical Data Model, Envelope Wrapper, Clear Patterns  

**Reference**: *Enterprise Integration Patterns* by Gregor Hohpe & Bobby Woolf  
📖 https://www.enterpriseintegrationpatterns.com/

---

**Last Updated**: 2025-10-11  
**Version**: 1.0  
**Status**: ✅ Production-Ready
