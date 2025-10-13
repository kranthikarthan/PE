# Data Structures & Algorithms (DSA) Guidance - All Features

## Overview

This document provides **comprehensive Data Structures and Algorithms guidance** for all 40 features across 7 phases of the Payments Engine. Each feature includes recommendations for optimal data structures, algorithms, time/space complexity considerations, and Java-specific implementations.

**Purpose**: Help AI agents make informed decisions about data structures and algorithms to ensure optimal performance, scalability, and maintainability.

**Target Audience**: AI coding agents, developers, architects

**Date**: 2025-10-12  
**Status**: ✅ Complete

---

## Table of Contents

1. [Phase 0: Foundation (5 Features)](#phase-0-foundation-5-features)
2. [Phase 1: Core Services (6 Features)](#phase-1-core-services-6-features)
3. [Phase 2: Clearing Adapters (5 Features)](#phase-2-clearing-adapters-5-features)
4. [Phase 3: Platform Services (5 Features)](#phase-3-platform-services-5-features)
5. [Phase 4: Advanced Features (7 Features)](#phase-4-advanced-features-7-features)
6. [Phase 5: Infrastructure (7 Features)](#phase-5-infrastructure-7-features)
7. [Phase 6: Testing (5 Features)](#phase-6-testing-5-features)
8. [Common Patterns & Best Practices](#common-patterns--best-practices)
9. [Java Collections Cheat Sheet](#java-collections-cheat-sheet)

---

## Phase 0: Foundation (5 Features)

### Feature 0.1: Database Schemas

**Purpose**: Generate PostgreSQL migration scripts (Flyway)

**Data Structures**:
- ✅ **HashMap<String, TableDefinition>**: Store table metadata (O(1) lookup)
- ✅ **LinkedHashMap<String, Column>**: Preserve column insertion order
- ✅ **ArrayList<String>**: Store SQL DDL statements in execution order
- ✅ **Set<String>**: Track unique constraint names, index names (prevent duplicates)
- ✅ **Graph (Adjacency List)**: Model foreign key dependencies between tables

**Algorithms**:
- ✅ **Topological Sort** (DFS-based): Order tables by foreign key dependencies
  - Time: O(V + E) where V = tables, E = foreign keys
  - Ensures parent tables are created before child tables
- ✅ **Depth-First Search (DFS)**: Detect circular foreign key dependencies
  - Time: O(V + E)
  - Prevent infinite loops in schema generation

**Implementation**:
```java
// Topological sort for table creation order
public List<String> getTableCreationOrder(Map<String, List<String>> dependencies) {
    Set<String> visited = new HashSet<>();
    Stack<String> stack = new Stack<>();
    
    for (String table : dependencies.keySet()) {
        if (!visited.contains(table)) {
            topologicalSortDFS(table, dependencies, visited, stack);
        }
    }
    
    List<String> result = new ArrayList<>();
    while (!stack.isEmpty()) {
        result.add(stack.pop());
    }
    return result; // Correct creation order
}
```

**Complexity**:
- Time: O(T + F) where T = tables, F = foreign keys
- Space: O(T) for visited set and stack

---

### Feature 0.2: Event Schemas (AsyncAPI)

**Purpose**: Generate AsyncAPI specifications for 25+ events

**Data Structures**:
- ✅ **HashMap<String, EventSchema>**: Map event name to schema (O(1) lookup)
- ✅ **TreeMap<String, EventSchema>**: Store events in alphabetical order (for documentation)
- ✅ **LinkedHashMap<String, Field>**: Preserve field order in JSON Schema
- ✅ **Set<String>**: Track required fields (validation)

**Algorithms**:
- ✅ **JSON Schema Validation**: Recursive validation algorithm
  - Time: O(N) where N = number of fields
- ✅ **Tree Traversal**: Navigate nested event structures
  - Time: O(N) where N = total fields

**Implementation**:
```java
// Validate event against JSON schema
public boolean validateEvent(JsonNode event, JsonSchema schema) {
    for (String requiredField : schema.getRequiredFields()) {
        if (!event.has(requiredField)) {
            return false; // Missing required field
        }
    }
    return true;
}
```

**Complexity**:
- Time: O(N) per event validation (N = fields)
- Space: O(D) where D = max nesting depth (recursion stack)

---

### Feature 0.3: Domain Models

**Purpose**: Generate Java domain entities (JPA), Value Objects, Aggregates

**Data Structures**:
- ✅ **HashMap<String, Entity>**: Store entities by name (O(1) lookup)
- ✅ **Set<Entity>**: Track aggregates (unique entities)
- ✅ **LinkedHashSet<Field>**: Preserve field order while preventing duplicates
- ✅ **Graph (Adjacency List)**: Model entity relationships (One-to-Many, Many-to-One)

**Algorithms**:
- ✅ **Graph Traversal (BFS)**: Navigate entity relationships
  - Time: O(V + E) where V = entities, E = relationships
- ✅ **Equality & HashCode Generation**: Based on business keys
  - Time: O(K) where K = number of key fields

**Implementation**:
```java
// Generate equals() and hashCode() based on business key
@Override
public boolean equals(Object obj) {
    if (this == obj) return true;
    if (!(obj instanceof Payment)) return false;
    Payment other = (Payment) obj;
    return Objects.equals(this.paymentId, other.paymentId) &&
           Objects.equals(this.tenantId, other.tenantId);
}

@Override
public int hashCode() {
    return Objects.hash(paymentId, tenantId); // O(K) where K = keys
}
```

**Complexity**:
- Time: O(1) for entity lookup, O(E) for relationship traversal
- Space: O(N) where N = total entities

---

### Feature 0.4: Shared Libraries

**Purpose**: Build reusable utility libraries (event publishing, error handling, etc.)

**Data Structures**:
- ✅ **ConcurrentHashMap<String, T>**: Thread-safe caching (O(1) average)
- ✅ **BlockingQueue<Event>**: Event buffer for async publishing (FIFO)
- ✅ **CircularBuffer**: Fixed-size buffer for recent events (for retry)
- ✅ **LRU Cache (LinkedHashMap)**: Least Recently Used cache for idempotency

**Algorithms**:
- ✅ **LRU Cache Eviction**: Remove least recently used items
  - Time: O(1) for get/put using LinkedHashMap
- ✅ **Exponential Backoff**: Retry algorithm
  - Time: O(1) per retry calculation

**Implementation**:
```java
// LRU Cache implementation
public class LRUCache<K, V> extends LinkedHashMap<K, V> {
    private final int capacity;
    
    public LRUCache(int capacity) {
        super(capacity, 0.75f, true); // accessOrder = true
        this.capacity = capacity;
    }
    
    @Override
    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        return size() > capacity; // O(1) eviction
    }
}

// Usage: Store idempotency keys
LRUCache<String, PaymentResponse> idempotencyCache = new LRUCache<>(10000);
```

**Complexity**:
- Time: O(1) for cache operations (get/put/remove)
- Space: O(C) where C = cache capacity

---

### Feature 0.5: Infrastructure Setup (Terraform)

**Purpose**: Provision Azure infrastructure (AKS, PostgreSQL, Redis, Service Bus)

**Data Structures**:
- ✅ **HashMap<String, Resource>**: Map resource name to definition
- ✅ **Graph (Directed Acyclic Graph)**: Model resource dependencies
- ✅ **PriorityQueue<Resource>**: Order resources by deployment priority
- ✅ **Set<String>**: Track provisioned resources (idempotency)

**Algorithms**:
- ✅ **Topological Sort**: Order resource creation by dependencies
  - Time: O(R + D) where R = resources, D = dependencies
- ✅ **DFS**: Detect circular dependencies
  - Time: O(R + D)

**Implementation**:
```java
// Topological sort for Terraform resource creation
public List<Resource> getProvisioningOrder(Map<String, List<String>> dependencies) {
    // Similar to Feature 0.1 (Database Schemas)
    return topologicalSort(dependencies);
}
```

**Complexity**:
- Time: O(R + D) where R = resources, D = dependencies
- Space: O(R) for tracking visited resources

---

## Phase 1: Core Services (6 Features)

### Feature 1.1: Payment Initiation Service

**Purpose**: Accept payment requests, validate, generate payment ID, publish events

**Data Structures**:
- ✅ **ConcurrentHashMap<String, Payment>**: In-memory cache for recent payments (O(1))
- ✅ **Redis (LRU Cache)**: Idempotency cache (key = idempotency key, value = response)
- ✅ **UUID**: Generate unique payment IDs (time-based UUID v1 or random UUID v4)
- ✅ **LinkedHashMap**: Preserve insertion order for audit trail

**Algorithms**:
- ✅ **UUID Generation**: Cryptographically secure random number generation
  - Time: O(1)
- ✅ **Idempotency Check**: Hash-based lookup
  - Time: O(1) average in HashMap/Redis
- ✅ **Input Validation**: Regex matching, range checks
  - Time: O(M) where M = input size

**Implementation**:
```java
// Idempotency check using Redis
public PaymentResponse initiatePayment(PaymentRequest request, String idempotencyKey) {
    // 1. Check idempotency cache (O(1))
    PaymentResponse cached = redisTemplate.opsForValue().get(idempotencyKey);
    if (cached != null) {
        return cached; // Return cached response
    }
    
    // 2. Generate payment ID (O(1))
    String paymentId = "PAY-" + UUID.randomUUID().toString();
    
    // 3. Create payment entity
    Payment payment = Payment.builder()
        .paymentId(paymentId)
        .amount(request.getAmount())
        .build();
    
    // 4. Save to database
    paymentRepository.save(payment);
    
    // 5. Publish event
    eventPublisher.publish(new PaymentInitiatedEvent(payment));
    
    // 6. Cache response (O(1))
    PaymentResponse response = new PaymentResponse(paymentId, "INITIATED");
    redisTemplate.opsForValue().set(idempotencyKey, response, 24, TimeUnit.HOURS);
    
    return response;
}
```

**Complexity**:
- Time: O(1) for idempotency check, O(1) for UUID generation, O(M) for validation
- Space: O(N) where N = number of cached payments (bounded by Redis TTL)

---

### Feature 1.2: Validation Service

**Purpose**: Execute business rules using Drools, validate payments

**Data Structures**:
- ✅ **LinkedList<Rule>**: Store rules in execution order (sequential processing)
- ✅ **HashMap<String, RuleResult>**: Store rule execution results (O(1) lookup)
- ✅ **BitSet**: Track which rules passed/failed (space-efficient)
- ✅ **PriorityQueue<Rule>**: Order rules by priority (salience in Drools)

**Algorithms**:
- ✅ **Rule Engine Pattern Matching**: Rete algorithm (Drools internal)
  - Time: O(R * F) where R = rules, F = facts
- ✅ **Short-Circuit Evaluation**: Stop on first failure
  - Time: Best case O(1), Worst case O(R)

**Implementation**:
```java
// Drools rule execution
public ValidationResult validate(Payment payment) {
    KieSession kieSession = kieContainer.newKieSession();
    
    // Insert facts
    kieSession.insert(payment);
    
    // Fire all rules (Rete algorithm internally)
    int rulesFired = kieSession.fireAllRules(); // O(R * F)
    
    // Collect results
    ValidationResult result = kieSession.getGlobal("validationResult");
    kieSession.dispose();
    
    return result;
}
```

**Complexity**:
- Time: O(R * F) where R = rules, F = facts (Rete algorithm)
- Space: O(R + F) for rule network and working memory

---

### Feature 1.3: Account Adapter Service

**Purpose**: Integrate with 5 external core banking systems (debit/credit operations)

**Data Structures**:
- ✅ **ConcurrentHashMap<String, AccountRouting>**: Cache account → system routing (O(1))
- ✅ **Redis (LRU Cache)**: Cache account balances (60-second TTL)
- ✅ **ConcurrentHashMap<String, CircuitBreakerState>**: Track circuit breaker states per system
- ✅ **LinkedBlockingQueue<Request>**: Request queue for rate limiting

**Algorithms**:
- ✅ **Consistent Hashing**: Distribute accounts across multiple backend systems
  - Time: O(log N) where N = number of systems (using TreeMap)
- ✅ **Token Bucket Algorithm**: Rate limiting per backend system
  - Time: O(1) for token check/refill
- ✅ **Exponential Backoff**: Retry algorithm with jitter
  - Time: O(1) per retry calculation

**Implementation**:
```java
// Consistent hashing for account routing
public class ConsistentHash {
    private final TreeMap<Integer, String> ring = new TreeMap<>();
    
    public void addSystem(String systemId) {
        for (int i = 0; i < 150; i++) { // Virtual nodes
            int hash = hash(systemId + i);
            ring.put(hash, systemId);
        }
    }
    
    public String getSystem(String accountNumber) {
        int hash = hash(accountNumber);
        Map.Entry<Integer, String> entry = ring.ceilingEntry(hash);
        if (entry == null) {
            entry = ring.firstEntry(); // Wrap around
        }
        return entry.getValue(); // O(log N)
    }
}

// Token bucket rate limiting
public class TokenBucket {
    private long tokens;
    private final long capacity;
    private final long refillRate; // tokens per second
    private long lastRefillTime;
    
    public synchronized boolean tryAcquire() {
        refill();
        if (tokens > 0) {
            tokens--;
            return true;
        }
        return false; // Rate limit exceeded
    }
    
    private void refill() {
        long now = System.nanoTime();
        long elapsedTime = now - lastRefillTime;
        long tokensToAdd = (elapsedTime * refillRate) / 1_000_000_000L;
        tokens = Math.min(capacity, tokens + tokensToAdd);
        lastRefillTime = now;
    }
}
```

**Complexity**:
- Time: O(log N) for consistent hashing, O(1) for token bucket
- Space: O(N * V) where N = systems, V = virtual nodes (consistent hashing)

---

### Feature 1.4: Routing Service

**Purpose**: Determine clearing system based on payment attributes using Drools

**Data Structures**:
- ✅ **HashMap<String, ClearingSystem>**: Map currency/amount → clearing system
- ✅ **Decision Tree**: Model routing rules (if-else logic)
- ✅ **PriorityQueue<RoutingRule>**: Order rules by priority (salience)
- ✅ **Trie (Prefix Tree)**: Match account number prefixes to clearing systems

**Algorithms**:
- ✅ **Rule-Based Decision Tree**: Evaluate rules in priority order
  - Time: O(R) where R = number of rules (worst case)
- ✅ **Trie Search**: Match account prefix
  - Time: O(K) where K = prefix length (typically 2-4 digits)

**Implementation**:
```java
// Trie for account prefix routing
public class RoutingTrie {
    private static class TrieNode {
        Map<Character, TrieNode> children = new HashMap<>();
        ClearingSystem clearingSystem;
    }
    
    private final TrieNode root = new TrieNode();
    
    public void addRoute(String prefix, ClearingSystem system) {
        TrieNode node = root;
        for (char c : prefix.toCharArray()) {
            node = node.children.computeIfAbsent(c, k -> new TrieNode());
        }
        node.clearingSystem = system;
    }
    
    public ClearingSystem route(String accountNumber) {
        TrieNode node = root;
        for (char c : accountNumber.toCharArray()) {
            node = node.children.get(c);
            if (node == null) break;
            if (node.clearingSystem != null) {
                return node.clearingSystem; // Found match
            }
        }
        return ClearingSystem.DEFAULT; // No match
    }
}
```

**Complexity**:
- Time: O(K) for Trie search where K = prefix length (typically constant)
- Space: O(P * K) where P = number of prefixes, K = average prefix length

---

### Feature 1.5: Transaction Processing Service

**Purpose**: Orchestrate payment processing (5-step workflow)

**Data Structures**:
- ✅ **Queue<Step>**: Store processing steps (FIFO execution)
- ✅ **HashMap<String, StepResult>**: Store step results (O(1) lookup)
- ✅ **Stack<Step>**: Store completed steps for compensation (rollback)
- ✅ **Graph (DAG)**: Model step dependencies for parallel execution

**Algorithms**:
- ✅ **Pipeline Pattern**: Sequential step execution
  - Time: O(S) where S = number of steps
- ✅ **Compensation Algorithm**: Reverse stack traversal for rollback
  - Time: O(S) for rollback
- ✅ **Parallel Execution (DAG)**: Use topological sort to identify parallelizable steps
  - Time: O(S + D) where D = dependencies

**Implementation**:
```java
// Pipeline pattern with compensation
public class TransactionPipeline {
    private final Queue<Step> steps = new LinkedList<>();
    private final Stack<Step> executedSteps = new Stack<>();
    
    public Result execute(Payment payment) {
        try {
            for (Step step : steps) {
                StepResult result = step.execute(payment);
                executedSteps.push(step);
                
                if (!result.isSuccess()) {
                    compensate(); // Rollback
                    return Result.failure(result.getError());
                }
            }
            return Result.success();
        } catch (Exception e) {
            compensate();
            return Result.failure(e.getMessage());
        }
    }
    
    private void compensate() {
        while (!executedSteps.isEmpty()) {
            Step step = executedSteps.pop();
            step.compensate(); // Execute compensation logic
        }
    }
}
```

**Complexity**:
- Time: O(S) for execution, O(S) for compensation (worst case)
- Space: O(S) for stack storage

---

### Feature 1.6: Saga Orchestrator Service

**Purpose**: Manage distributed transactions using orchestration-based Saga pattern

**Data Structures**:
- ✅ **Finite State Machine (FSM)**: Model 9 saga states
  - Represented as: HashMap<State, Map<Event, State>> (state transition table)
- ✅ **PriorityQueue<SagaInstance>**: Order sagas by timeout (earliest timeout first)
- ✅ **HashMap<String, SagaInstance>**: Map saga ID to instance (O(1) lookup)
- ✅ **LinkedList<SagaStep>**: Store compensation actions in reverse order

**Algorithms**:
- ✅ **State Machine Transition**: O(1) state lookup and transition
- ✅ **Timeout Detection**: Min-heap (PriorityQueue) for earliest timeout
  - Time: O(log N) for insert/remove, O(1) for peek
- ✅ **Compensation Chain**: Reverse traversal of LinkedList
  - Time: O(S) where S = number of steps

**Implementation**:
```java
// Saga state machine
public enum SagaState {
    INITIATED, VALIDATED, DEBITED, ROUTED, SUBMITTED, ACKNOWLEDGED, COMPLETED, FAILED, COMPENSATED
}

public class SagaStateMachine {
    private final Map<SagaState, Map<SagaEvent, SagaState>> transitions = new HashMap<>();
    
    public SagaStateMachine() {
        // Define transitions (O(1) lookup)
        transitions.put(INITIATED, Map.of(
            SagaEvent.VALIDATION_SUCCESS, VALIDATED,
            SagaEvent.VALIDATION_FAILED, FAILED
        ));
        transitions.put(VALIDATED, Map.of(
            SagaEvent.DEBIT_SUCCESS, DEBITED,
            SagaEvent.DEBIT_FAILED, COMPENSATED
        ));
        // ... more transitions
    }
    
    public SagaState transition(SagaState current, SagaEvent event) {
        Map<SagaEvent, SagaState> eventTransitions = transitions.get(current);
        if (eventTransitions == null) {
            throw new IllegalStateException("No transitions from state: " + current);
        }
        return eventTransitions.getOrDefault(event, current); // O(1)
    }
}

// Timeout management with PriorityQueue
public class SagaTimeoutManager {
    private final PriorityQueue<SagaInstance> timeoutQueue = 
        new PriorityQueue<>(Comparator.comparing(SagaInstance::getTimeoutAt));
    
    public void schedule(SagaInstance saga) {
        timeoutQueue.offer(saga); // O(log N)
    }
    
    public List<SagaInstance> getExpiredSagas() {
        List<SagaInstance> expired = new ArrayList<>();
        long now = System.currentTimeMillis();
        
        while (!timeoutQueue.isEmpty() && timeoutQueue.peek().getTimeoutAt() <= now) {
            expired.add(timeoutQueue.poll()); // O(log N) per poll
        }
        return expired;
    }
}
```

**Complexity**:
- Time: O(1) for state transition, O(log N) for timeout operations
- Space: O(N) where N = number of active sagas

---

## Phase 2: Clearing Adapters (5 Features)

### Feature 2.1: SAMOS Adapter

**Purpose**: Integrate with South African Reserve Bank's RTGS system (ISO 20022)

**Data Structures**:
- ✅ **Queue<Payment>**: Pending payments queue (FIFO processing)
- ✅ **HashMap<String, SubmissionStatus>**: Track submission status per payment (O(1))
- ✅ **XML DOM Tree**: Parse ISO 20022 pacs.008 messages
- ✅ **CircularBuffer**: Store recent submissions for retry/audit (fixed size)

**Algorithms**:
- ✅ **XML Parsing (DOM)**: Tree traversal algorithm
  - Time: O(N) where N = number of XML nodes
- ✅ **XML Validation (XSD)**: Schema validation algorithm
  - Time: O(N) for XML traversal + validation
- ✅ **Message Queue Processing**: FIFO with batching
  - Time: O(B) where B = batch size

**Implementation**:
```java
// ISO 20022 message builder
public String buildPacs008Message(Payment payment) {
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    DocumentBuilder builder = factory.newDocumentBuilder();
    Document doc = builder.newDocument();
    
    // Build XML structure (O(N) where N = fields)
    Element root = doc.createElement("Document");
    Element cdtTrf = doc.createElement("FIToFICstmrCdtTrf");
    
    // Add payment details
    Element txId = doc.createElement("TxId");
    txId.setTextContent(payment.getPaymentId());
    cdtTrf.appendChild(txId);
    
    root.appendChild(cdtTrf);
    doc.appendChild(root);
    
    // Serialize to XML string
    return serializeXML(doc);
}
```

**Complexity**:
- Time: O(N) for XML parsing/generation where N = nodes
- Space: O(N) for DOM tree

---

### Feature 2.2: BankservAfrica Adapter

**Purpose**: Integrate with BankservAfrica ACH/EFT system (ISO 8583)

**Data Structures**:
- ✅ **ByteBuffer**: Parse binary ISO 8583 messages
- ✅ **BitSet**: Represent ISO 8583 bitmap (field presence)
- ✅ **HashMap<Integer, String>**: Map field number → field value (O(1))
- ✅ **Queue<Payment>**: Batch payment queue

**Algorithms**:
- ✅ **Binary Parsing**: Bit manipulation for ISO 8583
  - Time: O(F) where F = number of fields
- ✅ **Bitmap Decoding**: BitSet operations
  - Time: O(128) for primary bitmap, O(256) for secondary (constant)
- ✅ **Batch Processing**: Group payments into files
  - Time: O(N) where N = number of payments

**Implementation**:
```java
// ISO 8583 bitmap parsing
public Map<Integer, String> parseISO8583(byte[] message) {
    ByteBuffer buffer = ByteBuffer.wrap(message);
    
    // Read MTI (Message Type Indicator) - 4 bytes
    String mti = readBytes(buffer, 4);
    
    // Read primary bitmap - 8 bytes (64 bits)
    byte[] primaryBitmap = readBytes(buffer, 8);
    BitSet bitmap = BitSet.valueOf(primaryBitmap);
    
    // Check if secondary bitmap present (bit 1 = 1)
    if (bitmap.get(1)) {
        byte[] secondaryBitmap = readBytes(buffer, 8);
        // Merge bitmaps
    }
    
    // Parse fields based on bitmap
    Map<Integer, String> fields = new HashMap<>();
    for (int i = 2; i <= 128; i++) {
        if (bitmap.get(i)) {
            String value = parseField(buffer, i); // O(1) per field
            fields.put(i, value);
        }
    }
    
    return fields;
}
```

**Complexity**:
- Time: O(F) where F = number of populated fields (typically 10-20)
- Space: O(F) for field storage

---

### Feature 2.3: RTC Adapter

**Purpose**: Real-Time Clearing integration

**Data Structures**:
- ✅ **PriorityQueue<Payment>**: Order payments by urgency/amount
- ✅ **HashMap<String, RTCStatus>**: Track real-time status (O(1))
- ✅ **ConcurrentLinkedQueue<Payment>**: Thread-safe queue for high throughput

**Algorithms**:
- ✅ **Priority Queue Processing**: Process urgent payments first
  - Time: O(log N) for insert/remove
- ✅ **Real-Time Processing**: Stream processing pattern
  - Time: O(1) per payment (parallel processing)

**Complexity**:
- Time: O(log N) for priority operations, O(1) for status lookup
- Space: O(N) where N = pending payments

---

### Feature 2.4: PayShap Adapter

**Purpose**: Instant P2P payment integration (mobile/email lookup)

**Data Structures**:
- ✅ **Trie (Prefix Tree)**: Search mobile numbers/emails efficiently
- ✅ **HashMap<String, Account>**: Lookup table for instant resolution (O(1))
- ✅ **Bloom Filter**: Quick check if mobile/email exists (space-efficient)

**Algorithms**:
- ✅ **Trie Search**: Find account by mobile prefix
  - Time: O(K) where K = mobile number length (constant: 10 digits)
- ✅ **Bloom Filter Lookup**: Probabilistic membership test
  - Time: O(K) where K = number of hash functions (typically 3-5)
  - False positive rate: configurable (e.g., 1%)

**Implementation**:
```java
// Bloom filter for mobile number lookup
public class MobileBloomFilter {
    private final BitSet bitSet;
    private final int size;
    private final int hashCount;
    
    public void add(String mobileNumber) {
        for (int i = 0; i < hashCount; i++) {
            int hash = hash(mobileNumber, i) % size;
            bitSet.set(hash);
        }
    }
    
    public boolean mightContain(String mobileNumber) {
        for (int i = 0; i < hashCount; i++) {
            int hash = hash(mobileNumber, i) % size;
            if (!bitSet.get(hash)) {
                return false; // Definitely not present
            }
        }
        return true; // Might be present (check HashMap for confirmation)
    }
}
```

**Complexity**:
- Time: O(K) for Trie search, O(H) for Bloom filter where H = hash functions
- Space: O(M) for Bloom filter where M = bit array size (much smaller than HashMap)

---

### Feature 2.5: SWIFT Adapter

**Purpose**: International payments with sanctions screening

**Data Structures**:
- ✅ **Trie (Radix Tree)**: Store sanctions list (OFAC, UN, EU) for prefix matching
- ✅ **Set<String>**: Store sanctioned entities (exact match)
- ✅ **HashMap<String, FXRate>**: Cache foreign exchange rates (O(1) lookup)
- ✅ **Queue<Payment>**: Pending SWIFT messages

**Algorithms**:
- ✅ **String Matching (Trie)**: Search sanctions list
  - Time: O(M) where M = name length
- ✅ **Fuzzy Matching (Levenshtein Distance)**: Detect similar names
  - Time: O(M * N) where M, N = string lengths (typically < 100)
- ✅ **Dynamic Programming**: Compute edit distance

**Implementation**:
```java
// Levenshtein distance for fuzzy name matching
public int levenshteinDistance(String s1, String s2) {
    int[][] dp = new int[s1.length() + 1][s2.length() + 1];
    
    // Initialize base cases
    for (int i = 0; i <= s1.length(); i++) {
        dp[i][0] = i;
    }
    for (int j = 0; j <= s2.length(); j++) {
        dp[0][j] = j;
    }
    
    // Fill DP table
    for (int i = 1; i <= s1.length(); i++) {
        for (int j = 1; j <= s2.length(); j++) {
            if (s1.charAt(i-1) == s2.charAt(j-1)) {
                dp[i][j] = dp[i-1][j-1]; // No operation needed
            } else {
                dp[i][j] = 1 + Math.min(
                    dp[i-1][j],     // Delete
                    Math.min(
                        dp[i][j-1],  // Insert
                        dp[i-1][j-1] // Replace
                    )
                );
            }
        }
    }
    
    return dp[s1.length()][s2.length()]; // O(M * N)
}

public boolean isSanctioned(String name) {
    // 1. Exact match (O(1))
    if (sanctionedSet.contains(name.toLowerCase())) {
        return true;
    }
    
    // 2. Fuzzy match (O(M * N * S) where S = sanctions list size)
    for (String sanctioned : sanctionedSet) {
        int distance = levenshteinDistance(name, sanctioned);
        if (distance <= 2) { // Allow 2 character difference
            return true; // Potential match - require manual review
        }
    }
    
    return false;
}
```

**Complexity**:
- Time: O(M * N) for Levenshtein distance, O(M * N * S) for fuzzy matching (S = list size)
- Space: O(M * N) for DP table (can be optimized to O(min(M, N)) using rolling array)

---

## Phase 3: Platform Services (5 Features)

### Feature 3.1: Tenant Management Service

**Purpose**: Multi-tenancy support (CRUD for tenants, business units, customers)

**Data Structures**:
- ✅ **Tree (N-ary Tree)**: Model 3-level hierarchy (Tenant → Business Unit → Customer)
- ✅ **HashMap<String, Tenant>**: Tenant lookup by ID (O(1))
- ✅ **Set<String>**: Track active tenant IDs (O(1) membership test)
- ✅ **Graph (Adjacency List)**: Model cross-tenant relationships (if any)

**Algorithms**:
- ✅ **Tree Traversal (DFS/BFS)**: Navigate tenant hierarchy
  - Time: O(N) where N = total entities (tenants + BUs + customers)
- ✅ **Path Compression**: Optimize tenant lookup
  - Time: O(log N) with balanced tree

**Implementation**:
```java
// Tenant hierarchy tree
public class TenantHierarchy {
    static class Node {
        String id;
        String type; // TENANT, BUSINESS_UNIT, CUSTOMER
        List<Node> children = new ArrayList<>();
    }
    
    private final Node root = new Node("ROOT", "SYSTEM");
    
    // Get all customers under a tenant (DFS)
    public List<Customer> getAllCustomers(String tenantId) {
        Node tenantNode = findNode(root, tenantId);
        if (tenantNode == null) return Collections.emptyList();
        
        List<Customer> customers = new ArrayList<>();
        dfs(tenantNode, customers);
        return customers; // O(N) where N = customers under tenant
    }
    
    private void dfs(Node node, List<Customer> customers) {
        if (node.type.equals("CUSTOMER")) {
            customers.add((Customer) node);
        }
        for (Node child : node.children) {
            dfs(child, customers); // Recursive traversal
        }
    }
}
```

**Complexity**:
- Time: O(N) for tree traversal, O(1) for tenant lookup
- Space: O(H) for recursion stack where H = tree height (typically 3: Tenant → BU → Customer)

---

### Feature 3.2: IAM Service

**Purpose**: Identity and Access Management (authentication, authorization, RBAC)

**Data Structures**:
- ✅ **HashMap<String, User>**: User lookup by ID (O(1))
- ✅ **HashMap<String, Set<Permission>>**: Role → Permissions mapping (O(1))
- ✅ **Graph (Adjacency List)**: Model role hierarchy (Manager → Employee)
- ✅ **Bloom Filter**: Quick check if permission exists (before expensive DB lookup)
- ✅ **LRU Cache (LinkedHashMap)**: Cache JWT tokens and permissions

**Algorithms**:
- ✅ **JWT Verification**: HMAC-SHA256 or RSA signature verification
  - Time: O(K) where K = token size (typically constant)
- ✅ **Permission Check (Graph Traversal)**: BFS to find inherited permissions
  - Time: O(V + E) where V = roles, E = role relationships
- ✅ **Role Resolution**: Transitive closure of role hierarchy
  - Time: O(R) where R = number of roles

**Implementation**:
```java
// RBAC with role hierarchy
public class RBACService {
    private final Map<String, Set<String>> rolePermissions = new HashMap<>();
    private final Map<String, Set<String>> roleHierarchy = new HashMap<>(); // role → parent roles
    
    // Check if user has permission (with role inheritance)
    public boolean hasPermission(User user, String permission) {
        Set<String> allPermissions = new HashSet<>();
        
        // BFS to collect permissions from all roles (including inherited)
        Queue<String> queue = new LinkedList<>(user.getRoles());
        Set<String> visited = new HashSet<>();
        
        while (!queue.isEmpty()) {
            String role = queue.poll();
            if (visited.contains(role)) continue;
            visited.add(role);
            
            // Add permissions from this role
            Set<String> permissions = rolePermissions.get(role);
            if (permissions != null) {
                allPermissions.addAll(permissions);
            }
            
            // Add parent roles to queue (inheritance)
            Set<String> parentRoles = roleHierarchy.get(role);
            if (parentRoles != null) {
                queue.addAll(parentRoles);
            }
        }
        
        return allPermissions.contains(permission); // O(V + E)
    }
}
```

**Complexity**:
- Time: O(V + E) for permission check with hierarchy where V = roles, E = inheritance relationships
- Space: O(V) for visited set

---

### Feature 3.3: Audit Service

**Purpose**: Capture audit trail for all payment operations

**Data Structures**:
- ✅ **CircularBuffer**: Store recent audit events (fixed size, overwrite oldest)
- ✅ **LinkedList<AuditEvent>**: Maintain insertion order for chronological audit trail
- ✅ **HashMap<String, List<AuditEvent>>**: Index by payment ID for fast lookup (O(1))
- ✅ **Time-Series Database (Cassandra-like)**: Append-only log structure

**Algorithms**:
- ✅ **Append-Only Log**: Write audit events sequentially
  - Time: O(1) per write
- ✅ **Range Query**: Retrieve audit events by time range
  - Time: O(log N + K) where N = total events, K = events in range
- ✅ **Indexing**: B-Tree or LSM-Tree for time-based indexing

**Implementation**:
```java
// Audit event storage with indexing
public class AuditService {
    // Primary storage: LinkedList for chronological order
    private final LinkedList<AuditEvent> auditLog = new LinkedList<>();
    
    // Secondary index: payment ID → events
    private final Map<String, List<AuditEvent>> paymentIndex = new ConcurrentHashMap<>();
    
    // Circular buffer for recent events (fast access)
    private final CircularBuffer<AuditEvent> recentEvents = new CircularBuffer<>(1000);
    
    public void logEvent(AuditEvent event) {
        // 1. Append to log (O(1))
        auditLog.add(event);
        
        // 2. Update index (O(1))
        paymentIndex.computeIfAbsent(event.getPaymentId(), k -> new ArrayList<>())
                    .add(event);
        
        // 3. Add to circular buffer (O(1))
        recentEvents.add(event);
        
        // 4. Publish to event bus for async processing
        eventPublisher.publish(event);
    }
    
    public List<AuditEvent> getPaymentAudit(String paymentId) {
        return paymentIndex.getOrDefault(paymentId, Collections.emptyList()); // O(1)
    }
}
```

**Complexity**:
- Time: O(1) for append, O(1) for payment ID lookup, O(log N + K) for range query
- Space: O(N) where N = total audit events

---

### Feature 3.4: Notification Service

**Purpose**: Send notifications via IBM MQ (fire-and-forget)

**Data Structures**:
- ✅ **BlockingQueue<Notification>**: Thread-safe queue for async processing
- ✅ **PriorityQueue<Notification>**: Order by urgency (HIGH, MEDIUM, LOW)
- ✅ **HashMap<String, NotificationStatus>**: Track delivery status (O(1))
- ✅ **CircularBuffer**: Store recent notifications for retry

**Algorithms**:
- ✅ **Producer-Consumer Pattern**: Multi-threaded notification processing
  - Time: O(1) for enqueue/dequeue
- ✅ **Retry with Exponential Backoff**: Failed notification retry
  - Time: O(1) per retry calculation

**Implementation**:
```java
// Producer-consumer pattern for notifications
public class NotificationService {
    private final BlockingQueue<Notification> queue = new LinkedBlockingQueue<>(10000);
    private final ExecutorService workers = Executors.newFixedThreadPool(10);
    
    public void sendAsync(Notification notification) {
        queue.offer(notification); // O(1) non-blocking
    }
    
    @PostConstruct
    public void startWorkers() {
        for (int i = 0; i < 10; i++) {
            workers.submit(() -> {
                while (true) {
                    try {
                        Notification notification = queue.take(); // O(1) blocking
                        sendToIBMMQ(notification);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            });
        }
    }
}
```

**Complexity**:
- Time: O(1) for enqueue/dequeue (BlockingQueue)
- Space: O(Q) where Q = queue capacity

---

### Feature 3.5: Reporting Service

**Purpose**: Generate reports from Azure Synapse Analytics

**Data Structures**:
- ✅ **ColumnStore**: Synapse column-oriented storage
- ✅ **HashMap<String, ReportDefinition>**: Cache report templates (O(1))
- ✅ **PriorityQueue<ReportJob>**: Schedule reports by priority/deadline
- ✅ **Bitmap Index**: Fast filtering on categorical columns (payment status, type)

**Algorithms**:
- ✅ **Columnar Scanning**: Read only required columns
  - Time: O(N * C) where N = rows, C = selected columns
- ✅ **Aggregation (MapReduce)**: Parallel aggregation across partitions
  - Time: O(N / P) where P = number of partitions (parallel)
- ✅ **Bitmap Indexing**: Fast filtering using bitwise AND/OR operations
  - Time: O(N / 64) for bitmap operations (64-bit words)

**Implementation**:
```java
// Parallel aggregation with streams
public ReportResult generateReport(ReportRequest request) {
    // 1. Query Synapse Analytics (columnar storage)
    List<Payment> payments = synapseClient.query(request.getQuery());
    
    // 2. Parallel aggregation using Java streams
    Map<String, DoubleSummaryStatistics> stats = payments.parallelStream()
        .collect(Collectors.groupingBy(
            Payment::getCurrency,
            Collectors.summarizingDouble(Payment::getAmount)
        ));
    
    // 3. Build report
    ReportResult report = new ReportResult();
    stats.forEach((currency, stat) -> {
        report.addRow(currency, stat.getSum(), stat.getCount(), stat.getAverage());
    });
    
    return report;
}
```

**Complexity**:
- Time: O(N / P) for parallel aggregation where P = CPU cores
- Space: O(G) where G = number of groups (e.g., number of currencies)

---

## Phase 4: Advanced Features (7 Features)

### Feature 4.1: Batch Processing Service

**Purpose**: Process bulk payment files (CSV, Excel, XML, JSON) using Spring Batch

**Data Structures**:
- ✅ **Queue<BatchJob>**: Pending batch jobs (FIFO)
- ✅ **HashMap<String, JobExecution>**: Track job status (O(1))
- ✅ **Chunk-based Processing**: Process records in chunks (e.g., 1000 records/chunk)
- ✅ **Thread Pool**: Parallel chunk processing

**Algorithms**:
- ✅ **Chunk Processing**: Read → Process → Write pattern
  - Time: O(N / C) where N = total records, C = chunk size
- ✅ **Parallel Processing**: Multi-threaded chunk execution
  - Time: O(N / (C * T)) where T = number of threads
- ✅ **Skip/Retry Logic**: Handle record-level failures
  - Time: O(1) per skip/retry decision

**Implementation**:
```java
// Spring Batch chunk processing
@Bean
public Step processPaymentStep() {
    return stepBuilderFactory.get("processPaymentStep")
        .<PaymentRecord, Payment>chunk(1000) // Chunk size = 1000
        .reader(paymentFileReader())
        .processor(paymentProcessor())
        .writer(paymentWriter())
        .faultTolerant()
        .skip(ValidationException.class)
        .skipLimit(100) // Skip up to 100 invalid records
        .retry(TransientException.class)
        .retryLimit(3) // Retry up to 3 times
        .taskExecutor(taskExecutor()) // Parallel processing
        .throttleLimit(10) // Max 10 concurrent chunks
        .build();
}
```

**Complexity**:
- Time: O(N / (C * T)) where N = records, C = chunk size, T = threads
- Space: O(C) for chunk buffer

---

### Feature 4.2: Settlement Service

**Purpose**: Calculate net positions and generate settlement reports

**Data Structures**:
- ✅ **HashMap<String, BigDecimal>**: Net position per bank (O(1) update)
- ✅ **PriorityQueue<Settlement>**: Order settlements by amount (largest first)
- ✅ **Graph (Adjacency Matrix)**: Model inter-bank flows
- ✅ **Balanced Tree (TreeMap)**: Sort settlements by bank ID

**Algorithms**:
- ✅ **Netting Algorithm**: Calculate net positions
  - Time: O(N) where N = number of transactions
- ✅ **Multilateral Netting**: Optimize settlement positions
  - Time: O(B²) where B = number of banks (typically < 100)
- ✅ **Aggregation**: Sum debits and credits per bank
  - Time: O(N) single pass

**Implementation**:
```java
// Multilateral netting algorithm
public Map<String, BigDecimal> calculateNetPositions(List<Transaction> transactions) {
    Map<String, BigDecimal> netPositions = new HashMap<>();
    
    // Single pass aggregation (O(N))
    for (Transaction txn : transactions) {
        String debtor = txn.getDebtorBank();
        String creditor = txn.getCreditorBank();
        BigDecimal amount = txn.getAmount();
        
        // Debit from debtor
        netPositions.merge(debtor, amount.negate(), BigDecimal::add);
        
        // Credit to creditor
        netPositions.merge(creditor, amount, BigDecimal::add);
    }
    
    return netPositions; // O(N) time, O(B) space
}

// Generate settlement instructions
public List<SettlementInstruction> generateInstructions(Map<String, BigDecimal> netPositions) {
    PriorityQueue<NetPosition> creditors = new PriorityQueue<>(
        Comparator.comparing(NetPosition::getAmount).reversed()
    );
    PriorityQueue<NetPosition> debtors = new PriorityQueue<>(
        Comparator.comparing(NetPosition::getAmount)
    );
    
    // Separate creditors and debtors (O(B log B))
    netPositions.forEach((bank, amount) -> {
        if (amount.compareTo(BigDecimal.ZERO) > 0) {
            creditors.offer(new NetPosition(bank, amount));
        } else if (amount.compareTo(BigDecimal.ZERO) < 0) {
            debtors.offer(new NetPosition(bank, amount.negate()));
        }
    });
    
    // Match creditors with debtors (O(B log B))
    List<SettlementInstruction> instructions = new ArrayList<>();
    while (!creditors.isEmpty() && !debtors.isEmpty()) {
        NetPosition creditor = creditors.poll();
        NetPosition debtor = debtors.poll();
        
        BigDecimal settleAmount = creditor.getAmount().min(debtor.getAmount());
        instructions.add(new SettlementInstruction(debtor.getBank(), creditor.getBank(), settleAmount));
        
        // Re-queue if not fully settled
        BigDecimal remainingCredit = creditor.getAmount().subtract(settleAmount);
        if (remainingCredit.compareTo(BigDecimal.ZERO) > 0) {
            creditors.offer(new NetPosition(creditor.getBank(), remainingCredit));
        }
        
        BigDecimal remainingDebit = debtor.getAmount().subtract(settleAmount);
        if (remainingDebit.compareTo(BigDecimal.ZERO) > 0) {
            debtors.offer(new NetPosition(debtor.getBank(), remainingDebit));
        }
    }
    
    return instructions;
}
```

**Complexity**:
- Time: O(N) for netting, O(B log B) for settlement generation where B = banks
- Space: O(B) for net positions

---

### Feature 4.3: Reconciliation Service

**Purpose**: Match payments with clearing system responses

**Data Structures**:
- ✅ **HashMap<String, Payment>**: Index payments by reference (O(1) lookup)
- ✅ **HashMap<String, ClearingResponse>**: Index responses by reference (O(1))
- ✅ **Set<String>**: Track matched payment IDs (O(1) membership test)
- ✅ **PriorityQueue<UnmatchedItem>**: Order unmatched items by age (oldest first)

**Algorithms**:
- ✅ **Matching Algorithm**: Join payments with responses
  - Time: O(N) where N = number of payments
- ✅ **Set Intersection**: Find matched items
  - Time: O(min(N, M)) where N = payments, M = responses
- ✅ **Timeout Detection**: Identify unmatched items exceeding threshold
  - Time: O(log U) where U = unmatched items (using PriorityQueue)

**Implementation**:
```java
// Reconciliation matching algorithm
public ReconciliationResult reconcile(List<Payment> payments, List<ClearingResponse> responses) {
    // 1. Index payments by clearing reference (O(N))
    Map<String, Payment> paymentIndex = payments.stream()
        .collect(Collectors.toMap(Payment::getClearingReference, Function.identity()));
    
    // 2. Index responses by clearing reference (O(M))
    Map<String, ClearingResponse> responseIndex = responses.stream()
        .collect(Collectors.toMap(ClearingResponse::getClearingReference, Function.identity()));
    
    // 3. Find matched items (O(min(N, M)))
    Set<String> paymentRefs = paymentIndex.keySet();
    Set<String> responseRefs = responseIndex.keySet();
    Set<String> matchedRefs = new HashSet<>(paymentRefs);
    matchedRefs.retainAll(responseRefs); // Set intersection
    
    // 4. Find unmatched items
    Set<String> unmatchedPayments = new HashSet<>(paymentRefs);
    unmatchedPayments.removeAll(matchedRefs);
    
    Set<String> unmatchedResponses = new HashSet<>(responseRefs);
    unmatchedResponses.removeAll(matchedRefs);
    
    // 5. Build result
    ReconciliationResult result = new ReconciliationResult();
    result.setMatchedCount(matchedRefs.size());
    result.setUnmatchedPaymentCount(unmatchedPayments.size());
    result.setUnmatchedResponseCount(unmatchedResponses.size());
    
    return result;
}
```

**Complexity**:
- Time: O(N + M) where N = payments, M = responses
- Space: O(N + M) for indexes

---

### Feature 4.4: Internal API Gateway

**Purpose**: Route internal API requests to appropriate services

**Data Structures**:
- ✅ **Trie (Radix Tree)**: Route matching by URL path (O(K) where K = path length)
- ✅ **HashMap<String, ServiceEndpoint>**: Service registry (O(1) lookup)
- ✅ **LRU Cache**: Cache routing decisions (O(1) get/put)
- ✅ **CircularBuffer**: Store recent requests for rate limiting

**Algorithms**:
- ✅ **Trie-based Routing**: Match URL paths to services
  - Time: O(K) where K = path segments (typically 3-5)
- ✅ **Load Balancing (Round Robin)**: Distribute requests across instances
  - Time: O(1) per request
- ✅ **Weighted Round Robin**: Prefer high-performance instances
  - Time: O(N) where N = number of instances (typically < 10)

**Implementation**:
```java
// URL routing with Trie
public class APIRouter {
    static class RouteNode {
        Map<String, RouteNode> children = new HashMap<>();
        ServiceEndpoint endpoint; // null for intermediate nodes
    }
    
    private final RouteNode root = new RouteNode();
    
    public void addRoute(String path, ServiceEndpoint endpoint) {
        String[] segments = path.split("/");
        RouteNode node = root;
        
        for (String segment : segments) {
            if (segment.isEmpty()) continue;
            node = node.children.computeIfAbsent(segment, k -> new RouteNode());
        }
        node.endpoint = endpoint;
    }
    
    public ServiceEndpoint route(String path) {
        String[] segments = path.split("/");
        RouteNode node = root;
        
        for (String segment : segments) {
            if (segment.isEmpty()) continue;
            node = node.children.get(segment);
            if (node == null) return null; // No route found
        }
        
        return node.endpoint; // O(K) where K = path segments
    }
}

// Round-robin load balancing
public class LoadBalancer {
    private final List<ServiceInstance> instances;
    private final AtomicInteger counter = new AtomicInteger(0);
    
    public ServiceInstance next() {
        int index = counter.getAndIncrement() % instances.size();
        return instances.get(index); // O(1)
    }
}
```

**Complexity**:
- Time: O(K) for routing where K = path segments, O(1) for load balancing
- Space: O(R * K) where R = number of routes, K = average path segments

---

### Feature 4.5: Web BFF (GraphQL)

**Purpose**: Backend-for-Frontend for React web app using GraphQL

**Data Structures**:
- ✅ **Graph (Query Tree)**: Represent GraphQL query structure
- ✅ **HashMap<String, Resolver>**: Map field name → resolver function (O(1))
- ✅ **DataLoader (Batching)**: Batch multiple API calls to reduce N+1 queries
- ✅ **LRU Cache**: Cache GraphQL query results

**Algorithms**:
- ✅ **Query Parsing**: Build Abstract Syntax Tree (AST) from GraphQL query
  - Time: O(Q) where Q = query size
- ✅ **DataLoader Batching**: Collect requests in a tick, execute in batch
  - Time: O(B) where B = batch size (vs O(N) individual requests)
- ✅ **Query Optimization**: Merge duplicate field requests
  - Time: O(F) where F = number of fields

**Implementation**:
```java
// DataLoader for batching (N+1 problem solution)
public class PaymentDataLoader {
    private final BatchLoader<String, Payment> batchLoader = ids -> 
        CompletableFuture.supplyAsync(() -> paymentService.getPayments(ids));
    
    public DataLoader<String, Payment> create() {
        return DataLoader.newDataLoader(batchLoader);
    }
}

// GraphQL resolver with DataLoader
@Component
public class PaymentResolver implements GraphQLQueryResolver {
    @Autowired
    private DataLoader<String, Payment> paymentDataLoader;
    
    public CompletableFuture<Payment> getPayment(String id, DataFetchingEnvironment env) {
        return paymentDataLoader.load(id); // Batched execution
    }
    
    public CompletableFuture<List<Payment>> getPayments(List<String> ids, DataFetchingEnvironment env) {
        return paymentDataLoader.loadMany(ids); // Single batch API call
    }
}
```

**Complexity**:
- Time: O(Q) for query parsing, O(B) for batched execution (vs O(N) without batching)
- Space: O(B) for batch buffer

---

### Feature 4.6: Mobile BFF (REST Lightweight)

**Purpose**: Backend-for-Frontend for mobile apps (minimal payload)

**Data Structures**:
- ✅ **DTO (Data Transfer Object)**: Lightweight response objects
- ✅ **HashMap<String, Object>**: Cache responses (O(1) lookup)
- ✅ **Compression (Gzip)**: Reduce payload size (3G optimization)

**Algorithms**:
- ✅ **Field Projection**: Select only required fields
  - Time: O(F) where F = selected fields (vs O(N) for all fields)
- ✅ **Pagination**: Limit result set size
  - Time: O(P) where P = page size (vs O(N) for all results)
- ✅ **Gzip Compression**: Reduce network transfer
  - Time: O(D) where D = data size (typically 70-90% reduction)

**Implementation**:
```java
// Lightweight DTO with field projection
@Data
@Builder
public class MobilePaymentResponse {
    private String id;
    private BigDecimal amount;
    private String currency;
    private String status;
    private Instant createdAt;
    // NO nested objects, NO audit trail (keep payload < 5 KB)
}

// Field projection with JPA
@Repository
public interface PaymentRepository extends JpaRepository<Payment, String> {
    @Query("SELECT new com.payments.dto.MobilePaymentResponse(" +
           "p.id, p.amount, p.currency, p.status, p.createdAt) " +
           "FROM Payment p WHERE p.id = :id")
    MobilePaymentResponse findMobileView(@Param("id") String id); // O(1) query
}
```

**Complexity**:
- Time: O(1) for single record, O(P) for pagination
- Space: O(P) where P = page size

---

### Feature 4.7: Partner BFF (REST Comprehensive)

**Purpose**: Backend-for-Frontend for partner integrations (full payload)

**Data Structures**:
- ✅ **DTO with nested objects**: Comprehensive response objects
- ✅ **HashMap<String, RateLimiter>**: Rate limiter per partner (O(1) lookup)
- ✅ **Token Bucket**: Rate limiting algorithm per partner
- ✅ **Sliding Window Log**: Track request history for throttling

**Algorithms**:
- ✅ **Rate Limiting (Token Bucket)**: Allow 100 req/min per partner
  - Time: O(1) per request
- ✅ **Sliding Window Counter**: Track requests in time window
  - Time: O(W) where W = window size (typically remove old entries)

**Implementation**:
```java
// Sliding window rate limiter
public class SlidingWindowRateLimiter {
    private final ConcurrentHashMap<String, Deque<Long>> requestTimestamps = new ConcurrentHashMap<>();
    private final long windowMs;
    private final int maxRequests;
    
    public SlidingWindowRateLimiter(long windowMs, int maxRequests) {
        this.windowMs = windowMs;
        this.maxRequests = maxRequests;
    }
    
    public boolean allowRequest(String partnerId) {
        long now = System.currentTimeMillis();
        Deque<Long> timestamps = requestTimestamps.computeIfAbsent(partnerId, k -> new ConcurrentLinkedDeque<>());
        
        // Remove timestamps outside window (O(W) worst case)
        while (!timestamps.isEmpty() && now - timestamps.peekFirst() > windowMs) {
            timestamps.pollFirst();
        }
        
        // Check if under limit
        if (timestamps.size() < maxRequests) {
            timestamps.offerLast(now);
            return true; // Request allowed
        }
        
        return false; // Rate limit exceeded
    }
}
```

**Complexity**:
- Time: O(W) per request where W = requests in window (typically < 100)
- Space: O(P * W) where P = partners, W = requests per partner

---

## Phase 5: Infrastructure (7 Features)

### Feature 5.1: Service Mesh (Istio)

**Purpose**: Deploy Istio for traffic management, security, observability

**Data Structures**:
- ✅ **Graph (Service Topology)**: Model service dependencies
- ✅ **HashMap<String, ServiceConfig>**: Istio configurations per service (O(1))
- ✅ **Priority Queue**: Order circuit breaker activations by time

**Algorithms**:
- ✅ **Circuit Breaker State Machine**: Track open/closed/half-open states
  - Time: O(1) per state transition
- ✅ **Load Balancing (Consistent Hashing)**: Distribute traffic evenly
  - Time: O(log N) where N = service instances

**Complexity**:
- Time: O(1) for config lookup, O(log N) for load balancing
- Space: O(S) where S = number of services

---

### Feature 5.2: Prometheus Setup

**Purpose**: Metrics collection and alerting

**Data Structures**:
- ✅ **Time-Series Database**: Store metrics over time
- ✅ **HashMap<String, MetricFamily>**: Index metrics by name (O(1))
- ✅ **CircularBuffer**: Store recent metric samples (fixed size)

**Algorithms**:
- ✅ **Time-Series Aggregation**: Sum, average, percentile calculations
  - Time: O(N) where N = number of samples in time range
- ✅ **Range Query**: Retrieve metrics for time window
  - Time: O(log N + K) where K = samples in range

**Complexity**:
- Time: O(1) for metric recording, O(N) for aggregation
- Space: O(M * T) where M = metrics, T = time retention

---

### Feature 5.3: Grafana Dashboards

**Purpose**: Metrics visualization

**Data Structures**:
- ✅ **JSON Object**: Store dashboard definitions
- ✅ **HashMap<String, Dashboard>**: Cache dashboards (O(1) lookup)
- ✅ **Time-Series Array**: Data points for charts

**Algorithms**:
- ✅ **Data Downsampling**: Reduce points for visualization
  - Time: O(N / R) where R = reduction factor
- ✅ **Interpolation**: Fill gaps in time series
  - Time: O(N) linear interpolation

**Complexity**:
- Time: O(N / R) for downsampling
- Space: O(D) where D = dashboard size

---

### Feature 5.4: Jaeger Distributed Tracing

**Purpose**: End-to-end request tracing

**Data Structures**:
- ✅ **Span Tree**: Hierarchical trace structure
- ✅ **HashMap<String, Span>**: Index spans by span ID (O(1))
- ✅ **Graph (Call Graph)**: Visualize service dependencies

**Algorithms**:
- ✅ **Tree Traversal**: Build trace from spans
  - Time: O(S) where S = number of spans
- ✅ **Sampling (Probabilistic)**: Sample 10% of requests
  - Time: O(1) per decision (random number generation)

**Complexity**:
- Time: O(S) for trace assembly where S = spans
- Space: O(S) for trace storage

---

### Feature 5.5: GitOps (ArgoCD)

**Purpose**: Declarative continuous deployment

**Data Structures**:
- ✅ **Git Tree**: Repository structure
- ✅ **Diff Algorithm (Myers)**: Compare desired vs actual state
- ✅ **DAG (Directed Acyclic Graph)**: Deployment dependencies

**Algorithms**:
- ✅ **Myers Diff Algorithm**: Find minimal changes
  - Time: O(N * D) where N = lines, D = diff size
- ✅ **Three-Way Merge**: Merge Git, cluster, desired state
  - Time: O(N) where N = resources

**Complexity**:
- Time: O(N * D) for diff, O(N) for sync
- Space: O(N) for state storage

---

### Feature 5.6: Feature Flags (Unleash)

**Purpose**: Progressive delivery and A/B testing

**Data Structures**:
- ✅ **HashMap<String, FeatureFlag>**: Flag configuration (O(1) lookup)
- ✅ **BitSet**: Efficient storage for user cohorts (large-scale A/B testing)
- ✅ **Bloom Filter**: Quick check if user is in experiment group

**Algorithms**:
- ✅ **Consistent Hashing**: Assign users to experiments
  - Time: O(1) per assignment
- ✅ **Percentage Rollout**: Gradually enable features (0% → 100%)
  - Time: O(1) per check (hash user ID, compare to threshold)

**Implementation**:
```java
// Feature flag evaluation
public boolean isEnabled(String flagName, UnleashContext context) {
    FeatureFlag flag = flags.get(flagName); // O(1)
    if (flag == null) return false;
    
    // Check if user is in rollout percentage
    int hash = Math.abs(context.getUserId().hashCode());
    int bucket = hash % 100; // Map to 0-99
    
    return bucket < flag.getRolloutPercentage(); // O(1)
}
```

**Complexity**:
- Time: O(1) for flag evaluation
- Space: O(F) where F = number of flags

---

### Feature 5.7: Kubernetes Operators

**Purpose**: Automate Day 2 operations for databases, Kafka, etc.

**Data Structures**:
- ✅ **Custom Resource Definition (CRD)**: YAML schema
- ✅ **Reconciliation Loop**: Desired vs actual state comparison
- ✅ **Work Queue**: Pending reconciliation tasks

**Algorithms**:
- ✅ **Reconciliation Algorithm**: Converge to desired state
  - Time: O(R) where R = resources to reconcile
- ✅ **Level-Triggered**: Continuously check state (vs edge-triggered)
  - Time: O(1) per reconciliation cycle

**Implementation**:
```java
// Kubernetes operator reconciliation
public void reconcile(CustomResource resource) {
    // 1. Get desired state (O(1))
    DesiredState desired = resource.getSpec();
    
    // 2. Get actual state (O(1) API call)
    ActualState actual = kubernetesClient.getActualState(resource.getName());
    
    // 3. Compare and compute diff
    List<Action> actions = computeDiff(desired, actual); // O(F) where F = fields
    
    // 4. Apply actions
    for (Action action : actions) {
        action.apply(kubernetesClient); // O(A) where A = actions
    }
    
    // 5. Update status
    resource.setStatus(new Status("Reconciled", actions.size()));
}
```

**Complexity**:
- Time: O(R + A) where R = resources, A = actions
- Space: O(R) for resource cache

---

## Phase 6: Testing (5 Features)

### Feature 6.1: End-to-End Testing

**Purpose**: Validate complete payment flows

**Data Structures**:
- ✅ **Graph (Test Dependency Graph)**: Model test execution order
- ✅ **HashMap<String, TestResult>**: Store test results (O(1) lookup)
- ✅ **Queue<TestCase>**: Test execution queue

**Algorithms**:
- ✅ **Topological Sort**: Order tests by dependencies
  - Time: O(T + D) where T = tests, D = dependencies
- ✅ **Test Parallelization**: Run independent tests in parallel
  - Time: O(T / P) where P = parallel threads

**Complexity**:
- Time: O(T) for sequential, O(T / P) for parallel execution
- Space: O(T) for test results

---

### Feature 6.2: Load Testing

**Purpose**: Validate performance under load (Gatling)

**Data Structures**:
- ✅ **Histogram**: Store response time distribution
- ✅ **Circular Buffer**: Store recent request/response pairs
- ✅ **Priority Queue**: Track slowest requests (for analysis)

**Algorithms**:
- ✅ **Percentile Calculation**: Compute p50, p95, p99
  - Time: O(N log N) for sorting (or O(N) with streaming algorithm)
- ✅ **Request Generation (Poisson Process)**: Model realistic load
  - Time: O(1) per request generation

**Implementation**:
```java
// Streaming percentile calculation (t-digest algorithm)
public class StreamingPercentile {
    private final TDigest digest = new TDigest(100);
    
    public void add(double value) {
        digest.add(value); // O(log N) amortized
    }
    
    public double percentile(double p) {
        return digest.quantile(p / 100.0); // O(1)
    }
}
```

**Complexity**:
- Time: O(log N) per sample, O(1) for percentile query (streaming)
- Space: O(C) where C = compression factor (much smaller than O(N))

---

### Feature 6.3: Security Testing

**Purpose**: SAST, DAST, penetration testing

**Data Structures**:
- ✅ **Graph (Call Graph)**: Model security vulnerability propagation
- ✅ **Trie**: Store common attack patterns (SQL injection, XSS)
- ✅ **HashMap<String, Vulnerability>**: Index vulnerabilities (O(1))

**Algorithms**:
- ✅ **Pattern Matching**: Detect vulnerabilities using regex
  - Time: O(M * N) where M = patterns, N = code size
- ✅ **Taint Analysis**: Track data flow from sources to sinks
  - Time: O(V + E) where V = variables, E = data flows

**Complexity**:
- Time: O(M * N) for pattern matching
- Space: O(V) for vulnerability storage

---

### Feature 6.4: Compliance Testing

**Purpose**: Validate POPIA, FICA, PCI-DSS compliance

**Data Structures**:
- ✅ **Checklist (ArrayList)**: Compliance requirements
- ✅ **HashMap<String, ComplianceResult>**: Test results per requirement (O(1))
- ✅ **Graph**: Model compliance dependencies

**Algorithms**:
- ✅ **Checklist Validation**: Verify all requirements met
  - Time: O(R) where R = requirements
- ✅ **Evidence Collection**: Gather audit evidence
  - Time: O(E) where E = evidence items

**Complexity**:
- Time: O(R) for validation
- Space: O(R) for checklist

---

### Feature 6.5: Production Readiness

**Purpose**: Final verification before production deployment

**Data Structures**:
- ✅ **Checklist (ArrayList)**: Readiness criteria
- ✅ **HashMap<String, CheckResult>**: Results per check (O(1))

**Algorithms**:
- ✅ **Health Check**: Verify all services healthy
  - Time: O(S) where S = services
- ✅ **Smoke Testing**: Quick validation of critical paths
  - Time: O(T) where T = smoke tests

**Complexity**:
- Time: O(S + T) for health checks and smoke tests
- Space: O(S) for service status

---

## Common Patterns & Best Practices

### 1. Caching Strategies

**LRU Cache (LinkedHashMap)**:
```java
public class LRUCache<K, V> extends LinkedHashMap<K, V> {
    private final int capacity;
    
    public LRUCache(int capacity) {
        super(capacity, 0.75f, true); // accessOrder = true
        this.capacity = capacity;
    }
    
    @Override
    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        return size() > capacity;
    }
}
```
- Time: O(1) for get/put/remove
- Space: O(capacity)

**Cache-Aside Pattern**:
```java
public Payment getPayment(String id) {
    // 1. Check cache
    Payment cached = cache.get(id);
    if (cached != null) return cached;
    
    // 2. Query database
    Payment payment = paymentRepository.findById(id).orElse(null);
    
    // 3. Update cache
    if (payment != null) {
        cache.put(id, payment);
    }
    
    return payment;
}
```

---

### 2. Rate Limiting Patterns

**Token Bucket**:
- Best for: Smooth rate limiting, burst handling
- Time: O(1)
- Space: O(1)

**Sliding Window**:
- Best for: Precise rate limits, no burst tolerance
- Time: O(W) where W = window size
- Space: O(W)

**Leaky Bucket**:
- Best for: Constant output rate
- Time: O(1)
- Space: O(Q) where Q = queue size

---

### 3. Concurrency Patterns

**Thread-Safe Collections**:
```java
// Use ConcurrentHashMap for high-concurrency scenarios
ConcurrentHashMap<String, Payment> cache = new ConcurrentHashMap<>();

// Use CopyOnWriteArrayList for read-heavy, write-rare scenarios
CopyOnWriteArrayList<String> auditLog = new CopyOnWriteArrayList<>();

// Use BlockingQueue for producer-consumer
BlockingQueue<Event> eventQueue = new LinkedBlockingQueue<>(10000);
```

**Optimistic Locking (JPA)**:
```java
@Entity
public class Payment {
    @Version
    private Long version; // Optimistic lock
    
    // Other fields...
}
```
- Time: O(1) for version check
- Space: O(1) per entity

---

### 4. Batch Processing Patterns

**Chunk Processing**:
- Read → Process → Write pattern
- Optimal chunk size: 100-1000 records
- Time: O(N / C) where C = chunk size

**Parallel Chunk Processing**:
```java
@Bean
public TaskExecutor taskExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(10);
    executor.setMaxPoolSize(20);
    executor.setQueueCapacity(1000);
    return executor;
}
```
- Time: O(N / (C * T)) where T = threads

---

### 5. Search & Matching Patterns

**Trie for Prefix Matching**:
- Use case: Account number routing, mobile lookup
- Time: O(K) where K = key length
- Space: O(N * K) where N = keys

**Bloom Filter for Existence Check**:
- Use case: Sanctions screening, idempotency check
- Time: O(H) where H = hash functions
- Space: O(M) where M = bit array size
- False positive rate: Configurable (e.g., 1%)

**Consistent Hashing**:
- Use case: Distributed cache, load balancing
- Time: O(log N) using TreeMap
- Space: O(N * V) where V = virtual nodes

---

## Java Collections Cheat Sheet

### Time Complexity Reference

| Collection | Get/Contains | Add | Remove | Notes |
|------------|--------------|-----|--------|-------|
| **ArrayList** | O(1) | O(1) amortized | O(N) | Best for indexed access |
| **LinkedList** | O(N) | O(1) | O(1)* | Best for insertions/deletions, *at head/tail |
| **HashMap** | O(1) | O(1) | O(1) | Best for key-value lookup |
| **TreeMap** | O(log N) | O(log N) | O(log N) | Sorted map, range queries |
| **HashSet** | O(1) | O(1) | O(1) | Unique elements |
| **TreeSet** | O(log N) | O(log N) | O(log N) | Sorted set |
| **PriorityQueue** | O(1) peek | O(log N) | O(log N) poll | Min/max heap |
| **ConcurrentHashMap** | O(1) | O(1) | O(1) | Thread-safe HashMap |
| **CopyOnWriteArrayList** | O(1) | O(N) | O(N) | Thread-safe, read-heavy |
| **LinkedHashMap** | O(1) | O(1) | O(1) | Insertion or access order |
| **LinkedHashSet** | O(1) | O(1) | O(1) | Insertion order |

### When to Use What

**Fast Lookup (O(1))**:
- HashMap, HashSet, ConcurrentHashMap

**Sorted Order**:
- TreeMap, TreeSet (O(log N))

**Insertion Order**:
- LinkedHashMap, LinkedHashSet

**Priority/Ordering**:
- PriorityQueue (min/max heap)

**Thread-Safe**:
- ConcurrentHashMap (read/write heavy)
- CopyOnWriteArrayList (read heavy)
- Collections.synchronizedMap/List (simple cases)

**Queue/Deque**:
- LinkedList (general purpose)
- ArrayDeque (faster than LinkedList)
- PriorityQueue (priority-based)
- BlockingQueue (producer-consumer)

---

## Summary

This DSA guidance document provides **comprehensive recommendations** for all 40 features across 7 phases. Key principles:

1. ✅ **Choose appropriate data structures** based on operation requirements (O(1) lookup, O(log N) sorted, etc.)
2. ✅ **Optimize for common case** (e.g., use HashMap for frequent lookups, PriorityQueue for ordering)
3. ✅ **Consider concurrency** (ConcurrentHashMap vs HashMap, CopyOnWriteArrayList vs ArrayList)
4. ✅ **Balance time vs space** (Bloom filter for space-efficient membership test, Trie for fast prefix matching)
5. ✅ **Use caching strategically** (LRU cache, cache-aside pattern, TTL-based eviction)
6. ✅ **Batch when possible** (chunk processing, DataLoader for GraphQL, bulk database operations)

**Total DSA Patterns Covered**: 50+ patterns  
**Total Algorithms Covered**: 35+ algorithms  
**Total Data Structures**: 30+ data structures

**Status**: ✅ Complete - Ready for AI agent development

---

**Created**: 2025-10-12  
**Version**: 1.0  
**Maintained by**: Architecture Team

