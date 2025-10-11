# Confluent Kafka for Saga Pattern - Implementation Guide

## Overview

This document provides detailed specifications for implementing the Saga pattern using **Confluent Kafka** as an alternative to Azure Service Bus. Kafka is particularly well-suited for Saga patterns due to its event sourcing capabilities, exactly-once semantics, and event replay features.

---

## Why Kafka for Saga Pattern?

### Advantages

| Feature | Benefit for Saga Pattern |
|---------|--------------------------|
| **Event Sourcing** | Native support - every saga state change is an event |
| **Event Replay** | Rebuild saga state from event log |
| **Exactly-Once** | Idempotent processing with Kafka transactions |
| **Ordering** | Guaranteed order per partition (per saga instance) |
| **Scalability** | Handle millions of concurrent sagas |
| **Durability** | Events never lost - configurable retention |
| **Time Travel** | Query saga state at any point in time |
| **Parallel Processing** | Multiple consumers per topic |

### Saga Pattern Options with Kafka

```
Option 1: Orchestration-Based Saga (Recommended)
├── Saga Orchestrator Service
├── Produces commands to topics
├── Consumes responses from services
└── Maintains saga state in Kafka topic

Option 2: Choreography-Based Saga
├── No central orchestrator
├── Services react to events
├── Eventual consistency through event chain
└── More distributed, less centralized control
```

---

## Architecture with Kafka

### Kafka Topics Structure

```
kafka-cluster:
  topics:
    # Saga Orchestration Topics
    - saga.payment.commands           (partitioned by sagaId)
    - saga.payment.events             (partitioned by sagaId)
    - saga.payment.state              (compacted, stores current state)
    
    # Domain Events (from services)
    - payment.initiated               (partitioned by paymentId)
    - payment.validated               (partitioned by paymentId)
    - account.funds.reserved          (partitioned by accountNumber)
    - routing.determined              (partitioned by paymentId)
    - transaction.created             (partitioned by transactionId)
    - clearing.submitted              (partitioned by transactionId)
    - clearing.completed              (partitioned by transactionId)
    - payment.completed               (partitioned by paymentId)
    - payment.failed                  (partitioned by paymentId)
    
    # Compensation Events
    - saga.compensate.commands        (partitioned by sagaId)
    - saga.compensated.events         (partitioned by sagaId)
    
    # Limit Management Events
    - limit.consumed                  (partitioned by customerId)
    - limit.released                  (partitioned by customerId)
```

---

## Saga Orchestrator with Kafka

### Orchestration-Based Implementation

#### Topic Structure

```yaml
# Command Topics (Saga → Services)
saga.commands.validate-payment:
  partitions: 32
  replication-factor: 3
  retention: 7 days
  
saga.commands.reserve-funds:
  partitions: 32
  replication-factor: 3
  retention: 7 days

saga.commands.create-transaction:
  partitions: 32
  replication-factor: 3
  retention: 7 days

# Response Topics (Services → Saga)
saga.responses.payment-validated:
  partitions: 32
  replication-factor: 3
  retention: 7 days

saga.responses.funds-reserved:
  partitions: 32
  replication-factor: 3
  retention: 7 days

# Saga State Topic (Compacted - stores current state)
saga.state.payment-saga:
  partitions: 32
  replication-factor: 3
  cleanup.policy: compact
  retention: infinite
```

#### Saga State Event Sourcing

```java
@Service
@Slf4j
public class KafkaSagaOrchestrator {
    
    @Autowired
    private KafkaTemplate<String, SagaEvent> kafkaTemplate;
    
    @Autowired
    private KafkaStreamsStateStore sagaStateStore;
    
    /**
     * Start a new payment saga
     */
    public String startPaymentSaga(PaymentSagaRequest request) {
        String sagaId = generateSagaId();
        
        // Create saga started event
        SagaEvent sagaStarted = SagaEvent.builder()
            .sagaId(sagaId)
            .eventType("SAGA_STARTED")
            .sagaType("PAYMENT_SAGA")
            .payload(request)
            .timestamp(Instant.now())
            .build();
        
        // Publish to saga events topic (event sourcing)
        kafkaTemplate.send("saga.payment.events", sagaId, sagaStarted);
        
        // Publish first command
        executeStep(sagaId, "VALIDATE_PAYMENT", request);
        
        return sagaId;
    }
    
    /**
     * Execute a saga step
     */
    private void executeStep(String sagaId, String stepName, Object payload) {
        SagaCommand command = SagaCommand.builder()
            .sagaId(sagaId)
            .stepName(stepName)
            .commandType(getCommandType(stepName))
            .payload(payload)
            .timestamp(Instant.now())
            .build();
        
        // Publish command to appropriate topic
        String topic = getCommandTopic(stepName);
        kafkaTemplate.send(topic, sagaId, command);
        
        // Record step execution event
        SagaEvent stepStarted = SagaEvent.builder()
            .sagaId(sagaId)
            .eventType("STEP_STARTED")
            .stepName(stepName)
            .timestamp(Instant.now())
            .build();
        
        kafkaTemplate.send("saga.payment.events", sagaId, stepStarted);
    }
    
    /**
     * Handle step completion
     */
    @KafkaListener(
        topics = "saga.responses.#",
        groupId = "saga-orchestrator"
    )
    public void handleStepCompletion(SagaResponse response) {
        
        String sagaId = response.getSagaId();
        String stepName = response.getStepName();
        
        if (response.isSuccess()) {
            // Record step completion
            SagaEvent stepCompleted = SagaEvent.builder()
                .sagaId(sagaId)
                .eventType("STEP_COMPLETED")
                .stepName(stepName)
                .result(response.getResult())
                .timestamp(Instant.now())
                .build();
            
            kafkaTemplate.send("saga.payment.events", sagaId, stepCompleted);
            
            // Get next step
            String nextStep = getNextStep(stepName);
            
            if (nextStep != null) {
                // Execute next step
                executeStep(sagaId, nextStep, response.getResult());
            } else {
                // Saga completed
                completeSaga(sagaId);
            }
        } else {
            // Step failed - start compensation
            compensateSaga(sagaId, stepName, response.getError());
        }
    }
    
    /**
     * Compensate saga (rollback)
     */
    private void compensateSaga(String sagaId, String failedStep, String error) {
        
        log.warn("Saga {} failed at step {}, starting compensation", sagaId, failedStep);
        
        // Record compensation started
        SagaEvent compensationStarted = SagaEvent.builder()
            .sagaId(sagaId)
            .eventType("COMPENSATION_STARTED")
            .failedStep(failedStep)
            .error(error)
            .timestamp(Instant.now())
            .build();
        
        kafkaTemplate.send("saga.payment.events", sagaId, compensationStarted);
        
        // Get completed steps (from state store)
        List<String> completedSteps = getCompletedSteps(sagaId);
        
        // Compensate in reverse order (LIFO)
        Collections.reverse(completedSteps);
        
        for (String step : completedSteps) {
            if (hasCompensation(step)) {
                executeCompensation(sagaId, step);
            }
        }
        
        // Mark saga as compensated
        SagaEvent sagaCompensated = SagaEvent.builder()
            .sagaId(sagaId)
            .eventType("SAGA_COMPENSATED")
            .timestamp(Instant.now())
            .build();
        
        kafkaTemplate.send("saga.payment.events", sagaId, sagaCompensated);
    }
}
```

#### Kafka Streams for Saga State Management

```java
@Configuration
public class SagaStreamsConfig {
    
    @Bean
    public StreamsBuilder sagaStateStreamsBuilder() {
        StreamsBuilder builder = new StreamsBuilder();
        
        // Consume saga events and build state
        KStream<String, SagaEvent> sagaEvents = builder.stream("saga.payment.events");
        
        // Group by sagaId and aggregate state
        KTable<String, SagaState> sagaState = sagaEvents
            .groupByKey()
            .aggregate(
                SagaState::new,
                (sagaId, event, state) -> state.apply(event),
                Materialized.<String, SagaState, KeyValueStore<Bytes, byte[]>>as("saga-state-store")
                    .withKeySerde(Serdes.String())
                    .withValueSerde(new JsonSerde<>(SagaState.class))
            );
        
        // Publish current state to compacted topic
        sagaState.toStream()
            .to("saga.state.payment-saga", Produced.with(Serdes.String(), new JsonSerde<>(SagaState.class)));
        
        return builder;
    }
}

/**
 * Saga State (rebuilt from events)
 */
@Data
public class SagaState {
    private String sagaId;
    private String sagaType;
    private String status; // RUNNING, COMPLETED, COMPENSATING, COMPENSATED, FAILED
    private String currentStep;
    private Map<String, StepResult> completedSteps = new HashMap<>();
    private Object payload;
    private Instant startedAt;
    private Instant completedAt;
    
    /**
     * Apply event to update state (Event Sourcing)
     */
    public SagaState apply(SagaEvent event) {
        switch (event.getEventType()) {
            case "SAGA_STARTED":
                this.sagaId = event.getSagaId();
                this.sagaType = event.getSagaType();
                this.status = "RUNNING";
                this.payload = event.getPayload();
                this.startedAt = event.getTimestamp();
                break;
                
            case "STEP_STARTED":
                this.currentStep = event.getStepName();
                break;
                
            case "STEP_COMPLETED":
                this.completedSteps.put(
                    event.getStepName(), 
                    new StepResult(true, event.getResult())
                );
                break;
                
            case "COMPENSATION_STARTED":
                this.status = "COMPENSATING";
                break;
                
            case "SAGA_COMPLETED":
                this.status = "COMPLETED";
                this.completedAt = event.getTimestamp();
                break;
                
            case "SAGA_COMPENSATED":
                this.status = "COMPENSATED";
                this.completedAt = event.getTimestamp();
                break;
        }
        return this;
    }
}
```

---

## Kafka Configuration

### Confluent Kafka on Azure

#### Option 1: Confluent Cloud (Managed)

```hcl
# Terraform for Confluent Cloud
resource "confluent_kafka_cluster" "payments" {
  display_name = "payments-engine-kafka"
  availability = "MULTI_ZONE"
  cloud        = "AZURE"
  region       = "southafricanorth"
  
  standard {
    # Or DEDICATED for production
  }
  
  environment {
    id = confluent_environment.payments.id
  }
}

# Topics
resource "confluent_kafka_topic" "saga_events" {
  kafka_cluster {
    id = confluent_kafka_cluster.payments.id
  }
  
  topic_name         = "saga.payment.events"
  partitions_count   = 32
  
  config = {
    "cleanup.policy"    = "delete"
    "retention.ms"      = "604800000"  # 7 days
    "compression.type"  = "lz4"
  }
}

resource "confluent_kafka_topic" "saga_state" {
  kafka_cluster {
    id = confluent_kafka_cluster.payments.id
  }
  
  topic_name         = "saga.state.payment-saga"
  partitions_count   = 32
  
  config = {
    "cleanup.policy"    = "compact"
    "retention.ms"      = "-1"  # infinite
    "compression.type"  = "lz4"
    "min.compaction.lag.ms" = "60000"
  }
}
```

#### Option 2: Self-Hosted Kafka on AKS

```yaml
# Helm values for Kafka on AKS
kafka:
  enabled: true
  replicaCount: 3
  
  resources:
    requests:
      memory: "4Gi"
      cpu: "2"
    limits:
      memory: "8Gi"
      cpu: "4"
  
  persistence:
    enabled: true
    storageClass: "managed-premium"
    size: "500Gi"
  
  configurationOverrides:
    "num.partitions": "32"
    "default.replication.factor": "3"
    "min.insync.replicas": "2"
    "log.retention.hours": "168"  # 7 days
    "compression.type": "lz4"
    "message.max.bytes": "10485760"  # 10MB
    
    # Exactly-once semantics
    "enable.idempotence": "true"
    "transactional.id.expiration.ms": "86400000"

zookeeper:
  enabled: true
  replicaCount: 3
  
  resources:
    requests:
      memory: "2Gi"
      cpu: "1"

schema-registry:
  enabled: true
  replicaCount: 2
  
  kafkaStore:
    topic: "_schemas"
```

#### Option 3: Azure Event Hubs (Kafka-Compatible)

```hcl
# Azure Event Hubs with Kafka protocol
resource "azurerm_eventhub_namespace" "payments" {
  name                = "payments-eventhub"
  location            = azurerm_resource_group.payments.location
  resource_group_name = azurerm_resource_group.payments.name
  sku                 = "Premium"
  capacity            = 4
  
  kafka_enabled       = true
  
  zone_redundant      = true
  
  network_rulesets {
    default_action = "Deny"
    
    virtual_network_rule {
      subnet_id = azurerm_subnet.integration_subnet.id
    }
  }
}

resource "azurerm_eventhub" "saga_events" {
  name                = "saga-payment-events"
  namespace_name      = azurerm_eventhub_namespace.payments.name
  resource_group_name = azurerm_resource_group.payments.name
  partition_count     = 32
  message_retention   = 7
}
```

---

## Saga Implementation Patterns

### Pattern 1: Event Sourcing Saga (Recommended)

Every saga state change is stored as an event in Kafka. State is rebuilt by replaying events.

```java
@Service
public class EventSourcedSagaOrchestrator {
    
    @Autowired
    private KafkaTemplate<String, SagaEvent> eventProducer;
    
    @Autowired
    private InteractiveQueryService sagaStateQuery;
    
    /**
     * Start saga and publish SAGA_STARTED event
     */
    public String startSaga(PaymentSagaRequest request) {
        String sagaId = UUID.randomUUID().toString();
        
        // Create SAGA_STARTED event
        SagaEvent event = SagaEvent.builder()
            .sagaId(sagaId)
            .eventType("SAGA_STARTED")
            .sagaType("PAYMENT_SAGA")
            .payload(request)
            .eventId(UUID.randomUUID().toString())
            .timestamp(Instant.now())
            .build();
        
        // Publish to Kafka (event sourced)
        ProducerRecord<String, SagaEvent> record = new ProducerRecord<>(
            "saga.payment.events",
            sagaId,  // Key = sagaId (ensures ordering)
            event
        );
        
        kafkaTemplate.send(record);
        
        return sagaId;
    }
    
    /**
     * Listen to domain events and progress saga
     */
    @KafkaListener(topics = "payment.validated", groupId = "saga-orchestrator")
    public void onPaymentValidated(PaymentValidatedEvent event) {
        
        String sagaId = event.getCorrelationId(); // sagaId in correlation
        
        // Get current saga state
        SagaState state = getSagaState(sagaId);
        
        if (state == null || !"VALIDATE_PAYMENT".equals(state.getCurrentStep())) {
            return; // Not relevant to this saga
        }
        
        // Publish STEP_COMPLETED event
        publishStepCompleted(sagaId, "VALIDATE_PAYMENT", event);
        
        // Execute next step
        publishCommand(sagaId, "RESERVE_FUNDS", event);
    }
    
    /**
     * Get saga state from Kafka Streams state store
     */
    private SagaState getSagaState(String sagaId) {
        ReadOnlyKeyValueStore<String, SagaState> store = 
            sagaStateQuery.getQueryableStore(
                "saga-state-store",
                QueryableStoreTypes.keyValueStore()
            );
        
        return store.get(sagaId);
    }
    
    /**
     * Publish saga event (event sourcing)
     */
    private void publishStepCompleted(String sagaId, String stepName, Object result) {
        SagaEvent event = SagaEvent.builder()
            .sagaId(sagaId)
            .eventType("STEP_COMPLETED")
            .stepName(stepName)
            .result(result)
            .eventId(UUID.randomUUID().toString())
            .timestamp(Instant.now())
            .build();
        
        kafkaTemplate.send("saga.payment.events", sagaId, event);
    }
    
    /**
     * Publish command to service
     */
    private void publishCommand(String sagaId, String commandName, Object payload) {
        SagaCommand command = SagaCommand.builder()
            .sagaId(sagaId)
            .commandType(commandName)
            .payload(payload)
            .eventId(UUID.randomUUID().toString())
            .timestamp(Instant.now())
            .build();
        
        String topic = "saga.commands." + commandName.toLowerCase().replace("_", "-");
        kafkaTemplate.send(topic, sagaId, command);
    }
}
```

---

### Pattern 2: Choreography-Based Saga

Services react to events and publish new events. No central orchestrator.

```
Payment Initiated Event
  ↓
Validation Service (listens to payment.initiated)
  → Validates
  → Publishes: payment.validated
  ↓
Account Service (listens to payment.validated)
  → Reserves funds
  → Publishes: funds.reserved
  ↓
Routing Service (listens to funds.reserved)
  → Determines route
  → Publishes: routing.determined
  ↓
Transaction Service (listens to routing.determined)
  → Creates transaction
  → Publishes: transaction.created
  ↓
... (continues)
```

**Advantages**:
- No single point of failure
- Highly decoupled services
- Natural event-driven flow

**Disadvantages**:
- Complex to track saga state
- Harder to implement compensation
- Difficult to debug

---

## Kafka Producer Configuration

### Exactly-Once Semantics

```yaml
spring:
  kafka:
    bootstrap-servers: ${KAFKA_BOOTSTRAP_SERVERS}
    
    producer:
      # Exactly-once configuration
      acks: all
      retries: 3
      enable-idempotence: true
      transactional-id: saga-orchestrator-${INSTANCE_ID}
      
      # Performance
      compression-type: lz4
      batch-size: 32768
      linger-ms: 10
      buffer-memory: 67108864
      
      # Serialization
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.springframework.kafka.support.serializer.JsonSerializer
      
      properties:
        max.in.flight.requests.per.connection: 5
```

### Transactional Producer (Exactly-Once)

```java
@Service
public class TransactionalSagaProducer {
    
    @Autowired
    private KafkaTemplate<String, SagaEvent> kafkaTemplate;
    
    @Transactional("kafkaTransactionManager")
    public void publishSagaEvents(List<SagaEvent> events) {
        
        // All events published in single transaction
        for (SagaEvent event : events) {
            kafkaTemplate.send("saga.payment.events", event.getSagaId(), event);
        }
        
        // If any send fails, all are rolled back
    }
    
    @Bean
    public KafkaTransactionManager kafkaTransactionManager(
        ProducerFactory<String, SagaEvent> producerFactory
    ) {
        return new KafkaTransactionManager<>(producerFactory);
    }
}
```

---

## Kafka Consumer Configuration

### Saga Orchestrator Consumer

```yaml
spring:
  kafka:
    consumer:
      group-id: saga-orchestrator
      enable-auto-commit: false
      auto-offset-reset: earliest
      
      # Exactly-once
      isolation-level: read_committed
      
      # Serialization
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: org.springframework.kafka.support.serializer.JsonDeserializer
      
      properties:
        spring.json.trusted.packages: "com.payments.*"
        max.poll.records: 500
        max.poll.interval.ms: 300000
```

### Consumer with Manual Commit

```java
@KafkaListener(
    topics = "payment.validated",
    groupId = "saga-orchestrator",
    containerFactory = "kafkaListenerContainerFactory"
)
public void handlePaymentValidated(
    @Payload PaymentValidatedEvent event,
    @Header(KafkaHeaders.RECEIVED_PARTITION_ID) int partition,
    @Header(KafkaHeaders.OFFSET) long offset,
    Acknowledgment acknowledgment
) {
    try {
        // Process event
        processSagaStep(event);
        
        // Manually commit offset (exactly-once)
        acknowledgment.acknowledge();
        
    } catch (Exception e) {
        log.error("Failed to process event", e);
        // Don't acknowledge - will be reprocessed
    }
}
```

---

## Event Replay & Recovery

### Rebuild Saga State from Events

```java
@Service
public class SagaRecoveryService {
    
    @Autowired
    private KafkaConsumer<String, SagaEvent> kafkaConsumer;
    
    /**
     * Rebuild saga state by replaying events
     */
    public SagaState rebuildSagaState(String sagaId) {
        
        // Seek to beginning of partition for this sagaId
        kafkaConsumer.subscribe(List.of("saga.payment.events"));
        
        // Find partition for sagaId
        int partition = getPartitionForKey(sagaId, 32);
        
        TopicPartition tp = new TopicPartition("saga.payment.events", partition);
        kafkaConsumer.assign(List.of(tp));
        kafkaConsumer.seekToBeginning(List.of(tp));
        
        SagaState state = new SagaState();
        
        // Replay all events for this saga
        while (true) {
            ConsumerRecords<String, SagaEvent> records = kafkaConsumer.poll(Duration.ofSeconds(1));
            
            for (ConsumerRecord<String, SagaEvent> record : records) {
                if (sagaId.equals(record.key())) {
                    // Apply event to rebuild state
                    state = state.apply(record.value());
                }
            }
            
            if (records.isEmpty()) {
                break; // Reached end
            }
        }
        
        return state;
    }
}
```

---

## Kafka vs Azure Service Bus

### Migration Path

```
Phase 1: Start with Azure Service Bus
  ├── Faster to implement
  ├── Azure-native integration
  ├── Lower initial complexity
  └── Prove architecture works

Phase 2: Evaluate at Scale
  ├── Monitor throughput
  ├── Measure event replay needs
  ├── Assess costs
  └── Decision point

Phase 3: Migrate to Kafka (if needed)
  ├── High throughput required (> 100K msg/sec)
  ├── Event sourcing becomes critical
  ├── Need event replay capability
  └── Dual-write during migration
```

### Dual-Write Pattern (Migration)

```java
@Service
public class DualEventPublisher {
    
    @Autowired
    private ServiceBusTemplate serviceBusTemplate;
    
    @Autowired
    private KafkaTemplate<String, Event> kafkaTemplate;
    
    public void publishEvent(Event event) {
        
        // Write to both Service Bus and Kafka
        CompletableFuture<Void> serviceBusFuture = CompletableFuture.runAsync(() -> 
            serviceBusTemplate.send("topic", event)
        );
        
        CompletableFuture<Void> kafkaFuture = CompletableFuture.runAsync(() -> 
            kafkaTemplate.send("topic", event.getKey(), event)
        );
        
        // Wait for both to complete
        CompletableFuture.allOf(serviceBusFuture, kafkaFuture).join();
    }
}
```

---

## Cost Comparison

### Azure Service Bus (Current Design)

| Component | Configuration | Monthly Cost |
|-----------|---------------|--------------|
| Service Bus Premium | 1 Messaging Unit | $650 |
| Topics | 20 topics | Included |
| Subscriptions | 50 subscriptions | Included |
| Messages | 50M/month | Included |
| **Total** | | **$650/month** |

### Confluent Cloud (Kafka Option)

| Component | Configuration | Monthly Cost |
|-----------|---------------|--------------|
| Kafka Cluster | Standard, Multi-AZ | $1,500 |
| Ingress | 500 GB | $200 |
| Egress | 1 TB | $300 |
| Storage | 2 TB | $200 |
| Schema Registry | Included | $0 |
| ksqlDB (optional) | 1 instance | $500 |
| **Total** | | **$2,700/month** |

### Self-Hosted Kafka on AKS

| Component | Configuration | Monthly Cost |
|-----------|---------------|--------------|
| AKS Nodes | 3 × D8s_v3 | $1,800 |
| Persistent Storage | 1.5 TB SSD | $300 |
| Zookeeper Nodes | 3 × D4s_v3 | $900 |
| Schema Registry | 2 × D4s_v3 | $600 |
| **Total** | | **$3,600/month** |

**Recommendation**: Start with **Azure Service Bus ($650/month)**, migrate to **Confluent Cloud ($2,700/month)** if throughput > 100K msg/sec.

---

## Spring Boot Configuration

### application.yml (Kafka)

```yaml
spring:
  kafka:
    bootstrap-servers: ${KAFKA_BOOTSTRAP_SERVERS}
    
    # Producer
    producer:
      acks: all
      retries: 3
      enable-idempotence: true
      transactional-id: saga-orchestrator-${INSTANCE_ID}
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.springframework.kafka.support.serializer.JsonSerializer
    
    # Consumer
    consumer:
      group-id: saga-orchestrator
      enable-auto-commit: false
      auto-offset-reset: earliest
      isolation-level: read_committed
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: org.springframework.kafka.support.serializer.JsonDeserializer
      properties:
        spring.json.trusted.packages: "com.payments.*"
    
    # Streams (for state management)
    streams:
      application-id: saga-state-processor
      state-dir: /tmp/kafka-streams
      replication-factor: 3
      properties:
        num.stream.threads: 4
        processing.guarantee: exactly_once_v2
```

---

## Monitoring Kafka Sagas

### Kafka Metrics

```java
// Key metrics to monitor
kafka.producer.record-send-rate
kafka.producer.record-error-rate
kafka.consumer.records-lag-max
kafka.consumer.records-consumed-rate
kafka.streams.state-store-size

// Saga-specific metrics
saga.active.count          // Number of running sagas
saga.completed.rate        // Sagas completed per second
saga.failed.rate           // Sagas failed per second
saga.compensation.rate     // Compensations triggered per second
saga.duration.avg          // Average saga duration
```

### Kafka Dashboard

```
Kafka Saga Dashboard
├── Kafka Health
│   ├── Broker Status: 3/3 UP ✅
│   ├── Under-Replicated Partitions: 0
│   ├── Consumer Lag: 50 messages
│   └── Throughput: 45,000 msg/sec
│
├── Saga Statistics
│   ├── Active Sagas: 1,250
│   ├── Completed (24h): 2.1M
│   ├── Failed (24h): 125 (0.006%)
│   ├── Compensated (24h): 125
│   └── Avg Duration: 8.5 seconds
│
└── Topics
    ├── saga.payment.events: 2.1M msg/day
    ├── saga.payment.state: 1.2M entries (compacted)
    └── Consumer Lag: < 100 messages ✅
```

---

## Testing Kafka Sagas

### Embedded Kafka for Tests

```java
@SpringBootTest
@EmbeddedKafka(
    partitions = 1,
    topics = {"saga.payment.events", "saga.commands.validate-payment"}
)
class KafkaSagaOrchestratorTest {
    
    @Autowired
    private KafkaTemplate<String, SagaEvent> kafkaTemplate;
    
    @Autowired
    private KafkaSagaOrchestrator sagaOrchestrator;
    
    @Test
    void shouldCompleteSagaSuccessfully() {
        // Start saga
        String sagaId = sagaOrchestrator.startSaga(request);
        
        // Simulate domain events
        publishEvent("payment.validated", new PaymentValidatedEvent(...));
        publishEvent("funds.reserved", new FundsReservedEvent(...));
        // ... more events
        
        // Wait for saga completion
        await().atMost(10, SECONDS).until(() -> 
            getSagaState(sagaId).getStatus().equals("COMPLETED")
        );
        
        // Verify
        SagaState state = getSagaState(sagaId);
        assertThat(state.getCompletedSteps()).hasSize(7);
    }
}
```

---

## Best Practices

### 1. Partitioning Strategy
- **Key by sagaId**: Ensures all events for a saga go to same partition
- **Ordering**: Maintains event order per saga
- **Parallelism**: Different sagas processed in parallel

### 2. Idempotency
- Use `eventId` for deduplication
- Store processed event IDs in database or state store
- Handle duplicate events gracefully

### 3. Error Handling
- Dead Letter Topic for failed events
- Retry with exponential backoff
- Circuit breaker for external calls

### 4. State Management
- Use Kafka Streams for state aggregation
- Compacted topic for current state
- Event log for full history

### 5. Monitoring
- Consumer lag alerts
- Saga duration tracking
- Failure rate monitoring
- Compensation rate tracking

---

## Decision Matrix

### Choose Azure Service Bus If:

✅ Throughput < 50,000 msg/sec  
✅ Azure-native solution preferred  
✅ Simple pub/sub sufficient  
✅ Lower cost priority  
✅ Orchestration-based saga only  
✅ Limited event replay needs  

### Choose Confluent Kafka If:

✅ Throughput > 100,000 msg/sec  
✅ Event sourcing is critical  
✅ Need event replay capability  
✅ Want choreography-based saga  
✅ Exactly-once semantics required  
✅ Long-term event retention  
✅ Building event-driven architecture  
✅ Need time-travel queries  

### Hybrid Approach:

✅ Use both platforms for different purposes:
- **Kafka**: Saga orchestration, event sourcing, high-throughput
- **Service Bus**: Critical low-volume messages, notifications

---

## Summary

### Kafka for Saga Pattern

**Advantages**:
- ✅ Perfect for event sourcing
- ✅ Exactly-once semantics
- ✅ Event replay capability
- ✅ High throughput
- ✅ Natural fit for sagas
- ✅ Time-travel debugging

**Challenges**:
- ❌ More complex to set up
- ❌ Higher cost
- ❌ Requires Kafka expertise
- ❌ More operational overhead

**Recommendation**: 
- **Start** with Azure Service Bus for simplicity
- **Migrate** to Kafka when throughput > 100K msg/sec or event sourcing becomes critical
- **Plan** architecture to support both (abstract event publisher interface)

---

## Related Documents

- **[01-ASSUMPTIONS.md](01-ASSUMPTIONS.md)** - Section 2.3 (Communication Patterns)
- **[00-ARCHITECTURE-OVERVIEW.md](00-ARCHITECTURE-OVERVIEW.md)** - Event-driven architecture
- **[02-MICROSERVICES-BREAKDOWN.md](02-MICROSERVICES-BREAKDOWN.md)** - Section 13 (Saga Orchestrator)
- **[03-EVENT-SCHEMAS.md](03-EVENT-SCHEMAS.md)** - Event definitions
- **[07-AZURE-INFRASTRUCTURE.md](07-AZURE-INFRASTRUCTURE.md)** - Infrastructure options

---

**Last Updated**: 2025-10-11  
**Version**: 1.0
