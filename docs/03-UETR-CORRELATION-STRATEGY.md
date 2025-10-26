# UETR Correlation Strategy - ISO 20022 Message Tracking

## 🎯 **UETR Overview**

The **UETR (Unique End-to-End Transaction Reference)** is the cornerstone of ISO 20022 message correlation, providing global uniqueness and end-to-end tracking across all payment systems.

## 🔗 **UETR Characteristics**

### **Format & Structure**
```
UETR Format: 36-character UUID
Example: "12345678-1234-1234-1234-123456789012"

Components:
├── 8 characters: Timestamp (hex)
├── 4 characters: Random (hex)
├── 4 characters: Random (hex)
├── 4 characters: Random (hex)
└── 12 characters: Random (hex)
```

### **Global Uniqueness**
- **Scope**: Global across all payment systems
- **Uniqueness**: Guaranteed by UUID v4 algorithm
- **Persistence**: 7-year retention for audit compliance
- **Correlation**: Links all related ISO 20022 messages

## 🏗️ **UETR Implementation Architecture**

### **UETR Generation Service**
```java
@Service
public class UETRGenerator {
    
    /**
     * Generate a new UETR for payment initiation
     * @return 36-character UUID string
     */
    public String generateUETR() {
        return UUID.randomUUID().toString();
    }
    
    /**
     * Generate UETR with timestamp prefix for better traceability
     * @return UETR with timestamp prefix
     */
    public String generateTimestampedUETR() {
        long timestamp = System.currentTimeMillis();
        String timestampHex = Long.toHexString(timestamp);
        String randomPart = UUID.randomUUID().toString().substring(8);
        return timestampHex + "-" + randomPart;
    }
    
    /**
     * Validate UETR format
     * @param uetr UETR to validate
     * @return true if valid format
     */
    public boolean isValidUETR(String uetr) {
        if (uetr == null || uetr.length() != 36) {
            return false;
        }
        return uetr.matches("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$");
    }
}
```

### **UETR Correlation Service**
```java
@Service
public class UETRCorrelationService {
    
    @Autowired
    private UETRCorrelationRepository uetrCorrelationRepository;
    
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    
    /**
     * Create UETR correlation for a payment
     */
    public void createUETRCorrelation(String paymentId, String uetr, String tenantId) {
        UETRCorrelation correlation = UETRCorrelation.builder()
            .paymentId(paymentId)
            .uetr(uetr)
            .tenantId(tenantId)
            .correlationStatus("ACTIVE")
            .createdAt(Instant.now())
            .build();
            
        uetrCorrelationRepository.save(correlation);
        
        // Cache for fast lookups
        cacheUETRCorrelation(uetr, paymentId, tenantId);
    }
    
    /**
     * Add message to UETR correlation
     */
    public void addMessageToCorrelation(String uetr, String messageType, String messageId) {
        UETRCorrelation correlation = uetrCorrelationRepository.findByUetr(uetr);
        if (correlation != null) {
            switch (messageType) {
                case "pain.001":
                    correlation.setPain001MessageId(messageId);
                    break;
                case "pacs.008":
                    correlation.setPacs008MessageId(messageId);
                    break;
                case "pacs.002":
                    correlation.setPacs002MessageId(messageId);
                    break;
                case "pacs.004":
                    correlation.setPacs004MessageId(messageId);
                    break;
                case "camt.054":
                    correlation.setCamt054MessageId(messageId);
                    break;
            }
            uetrCorrelationRepository.save(correlation);
        }
    }
    
    /**
     * Get all messages for a UETR
     */
    public List<String> getCorrelatedMessages(String uetr) {
        UETRCorrelation correlation = uetrCorrelationRepository.findByUetr(uetr);
        if (correlation == null) {
            return Collections.emptyList();
        }
        
        List<String> messages = new ArrayList<>();
        if (correlation.getPain001MessageId() != null) {
            messages.add(correlation.getPain001MessageId());
        }
        if (correlation.getPacs008MessageId() != null) {
            messages.add(correlation.getPacs008MessageId());
        }
        if (correlation.getPacs002MessageId() != null) {
            messages.add(correlation.getPacs002MessageId());
        }
        if (correlation.getPacs004MessageId() != null) {
            messages.add(correlation.getPacs004MessageId());
        }
        if (correlation.getCamt054MessageId() != null) {
            messages.add(correlation.getCamt054MessageId());
        }
        
        return messages;
    }
    
    /**
     * Cache UETR correlation for fast lookups
     */
    private void cacheUETRCorrelation(String uetr, String paymentId, String tenantId) {
        String cacheKey = "uetr:correlation:" + uetr;
        Map<String, String> correlationData = new HashMap<>();
        correlationData.put("paymentId", paymentId);
        correlationData.put("tenantId", tenantId);
        correlationData.put("timestamp", String.valueOf(System.currentTimeMillis()));
        
        redisTemplate.opsForHash().putAll(cacheKey, correlationData);
        redisTemplate.expire(cacheKey, Duration.ofHours(24));
    }
}
```

## 🔄 **Message Flow with UETR Correlation**

### **1. Payment Initiation Flow**
```
Client Request → Payment Initiation Service
├── Generate UETR → UETRGenerator.generateUETR()
├── Create Payment → PostgreSQL (with UETR)
├── Build pain.001 → Include UETR in EndToEndId
├── Store Message → Cassandra (with UETR correlation)
├── Publish Event → Kafka (with UETR)
└── Cache Correlation → Redis (for fast lookups)
```

### **2. Clearing System Flow**
```
pacs.008 Generation → Clearing Adapter
├── Extract UETR → From pain.001 EndToEndId
├── Build pacs.008 → Include UETR in EndToEndId
├── Store Message → Cassandra (with UETR correlation)
├── Send to Clearing → External System
└── Update Correlation → Add pacs.008 to UETR chain
```

### **3. Status Reporting Flow**
```
pacs.002 Received → Status Processing Service
├── Extract UETR → From pacs.002 EndToEndId
├── Find Original → Lookup pain.001 by UETR
├── Build pain.002 → Include UETR in EndToEndId
├── Store Message → Cassandra (with UETR correlation)
├── Send to Client → Client Notification
└── Update Correlation → Add pacs.002 to UETR chain
```

## 🗃️ **Database Schema for UETR Correlation**

### **PostgreSQL - Core Correlation**
```sql
-- UETR correlation table
CREATE TABLE uetr_correlation (
  payment_id UUID PRIMARY KEY,
  uetr UUID UNIQUE NOT NULL,
  tenant_id VARCHAR(50) NOT NULL,
  pain001_message_id VARCHAR(50),
  pacs008_message_id VARCHAR(50),
  pacs002_message_id VARCHAR(50),
  pacs004_message_id VARCHAR(50),
  camt054_message_id VARCHAR(50),
  correlation_status VARCHAR(20) DEFAULT 'ACTIVE',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for performance
CREATE INDEX idx_uetr_correlation_uetr ON uetr_correlation(uetr);
CREATE INDEX idx_uetr_correlation_tenant ON uetr_correlation(tenant_id);
CREATE INDEX idx_uetr_correlation_status ON uetr_correlation(correlation_status);

-- Message details with UETR
CREATE TABLE iso20022_message_details (
  message_id VARCHAR(50) PRIMARY KEY,
  uetr UUID NOT NULL,
  message_type VARCHAR(20) NOT NULL,
  original_message_id VARCHAR(50),
  original_transaction_id VARCHAR(50),
  instruction_id VARCHAR(50),
  transaction_id VARCHAR(50),
  status_code VARCHAR(20),
  status_reason VARCHAR(100),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (uetr) REFERENCES uetr_correlation(uetr)
);
```

### **Cassandra - Message Storage**
```sql
-- UETR-based message storage
CREATE TABLE iso20022_messages_by_uetr (
  uetr UUID,
  message_id TEXT,
  message_type TEXT,
  tenant_id TEXT,
  payment_id UUID,
  raw_xml TEXT,
  parsed_json TEXT,
  validation_status TEXT,
  processing_status TEXT,
  created_at TIMESTAMP,
  PRIMARY KEY (uetr, message_id)
) WITH CLUSTERING ORDER BY (created_at DESC);

-- Message correlation by UETR
CREATE TABLE uetr_message_correlation (
  uetr UUID PRIMARY KEY,
  payment_id UUID,
  tenant_id TEXT,
  pain001_id TEXT,
  pacs008_id TEXT,
  pacs002_id TEXT,
  pacs004_id TEXT,
  camt054_id TEXT,
  correlation_status TEXT,
  created_at TIMESTAMP
) WITH DEFAULT TTL = 2592000; -- 30 days
```

### **Redis - Fast Lookups**
```yaml
# UETR correlation cache
uetr:correlation:{uetr} -> {
  "paymentId": "payment-12345",
  "tenantId": "tenant-001",
  "status": "ACTIVE",
  "timestamp": "1640995200000"
}

# Message chain cache
uetr:chain:{uetr} -> {
  "pain001": "pain.001.20250127.001",
  "pacs008": "pacs.008.20250127.001",
  "pacs002": "pacs.002.20250127.001"
}

# UETR lookup cache
uetr:lookup:{uetr} -> {
  "paymentId": "payment-12345",
  "tenantId": "tenant-001",
  "status": "PROCESSING"
}
```

## 🔍 **UETR Query Patterns**

### **1. Find Payment by UETR**
```sql
-- PostgreSQL query
SELECT p.*, c.*
FROM payments p
JOIN uetr_correlation c ON p.id = c.payment_id
WHERE c.uetr = '12345678-1234-1234-1234-123456789012';
```

### **2. Find All Messages for UETR**
```sql
-- Cassandra query
SELECT *
FROM iso20022_messages_by_uetr
WHERE uetr = 12345678-1234-1234-1234-123456789012;
```

### **3. Find Message Chain**
```sql
-- PostgreSQL query
SELECT 
  uetr,
  pain001_message_id,
  pacs008_message_id,
  pacs002_message_id,
  pacs004_message_id,
  camt054_message_id
FROM uetr_correlation
WHERE uetr = '12345678-1234-1234-1234-123456789012';
```

### **4. Find Messages by Type and UETR**
```sql
-- Cassandra query
SELECT *
FROM iso20022_messages_by_uetr
WHERE uetr = 12345678-1234-1234-1234-123456789012
  AND message_type = 'pain.001';
```

## 🎯 **UETR in ISO 20022 Messages**

### **pain.001 (Payment Initiation)**
```xml
<Document xmlns="urn:iso:std:iso:20022:tech:xsd:pain.001.001.10">
  <CstmrCdtTrfInitn>
    <GrpHdr>
      <MsgId>pain.001.20250127.001</MsgId>
      <CreDtTm>2025-01-27T10:00:00Z</CreDtTm>
      <NbOfTxs>1</NbOfTxs>
    </GrpHdr>
    <PmtInf>
      <PmtInfId>PMT-001</PmtInfId>
      <PmtMtd>TRF</PmtMtd>
      <CdtTrfTxInf>
        <PmtId>
          <EndToEndId>12345678-1234-1234-1234-123456789012</EndToEndId>
        </PmtId>
        <Amt>
          <InstdAmt Ccy="USD">1000.00</InstdAmt>
        </Amt>
      </CdtTrfTxInf>
    </PmtInf>
  </CstmrCdtTrfInitn>
</Document>
```

### **pacs.008 (FI to FI Credit Transfer)**
```xml
<Document xmlns="urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08">
  <FIToFICstmrCdtTrf>
    <GrpHdr>
      <MsgId>pacs.008.20250127.001</MsgId>
      <CreDtTm>2025-01-27T10:00:00Z</CreDtTm>
      <NbOfTxs>1</NbOfTxs>
    </GrpHdr>
    <CdtTrfTxInf>
      <PmtId>
        <EndToEndId>12345678-1234-1234-1234-123456789012</EndToEndId>
      </PmtId>
      <Amt>
        <InstdAmt Ccy="USD">1000.00</InstdAmt>
      </Amt>
    </CdtTrfTxInf>
  </FIToFICstmrCdtTrf>
</Document>
```

### **pacs.002 (Payment Status Report)**
```xml
<Document xmlns="urn:iso:std:iso:20022:tech:xsd:pacs.002.001.10">
  <FIToFIPmtStsRpt>
    <GrpHdr>
      <MsgId>pacs.002.20250127.001</MsgId>
      <CreDtTm>2025-01-27T10:00:00Z</CreDtTm>
      <OrgnlGrpInf>
        <OrgnlMsgId>pain.001.20250127.001</OrgnlMsgId>
        <OrgnlMsgNmId>pain.001.001.10</OrgnlMsgNmId>
      </OrgnlGrpInf>
    </GrpHdr>
    <TxInfAndSts>
      <StsId>STS-001</StsId>
      <OrgnlEndToEndId>12345678-1234-1234-1234-123456789012</OrgnlEndToEndId>
      <TxSts>ACSC</TxSts>
      <StsRsnInf>
        <Rsn>
          <Cd>NARR</Cd>
        </Rsn>
      </StsRsnInf>
    </TxInfAndSts>
  </FIToFIPmtStsRpt>
</Document>
```

## 🚀 **Performance Optimization**

### **UETR Lookup Performance**
```java
@Service
public class UETRLookupService {
    
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    
    @Autowired
    private UETRCorrelationRepository uetrCorrelationRepository;
    
    /**
     * Fast UETR lookup with caching
     */
    public UETRCorrelation findUETRCorrelation(String uetr) {
        // Try cache first
        String cacheKey = "uetr:correlation:" + uetr;
        Map<Object, Object> cached = redisTemplate.opsForHash().entries(cacheKey);
        
        if (!cached.isEmpty()) {
            return UETRCorrelation.builder()
                .uetr(uetr)
                .paymentId((String) cached.get("paymentId"))
                .tenantId((String) cached.get("tenantId"))
                .build();
        }
        
        // Fallback to database
        UETRCorrelation correlation = uetrCorrelationRepository.findByUetr(uetr);
        if (correlation != null) {
            // Cache for future lookups
            cacheUETRCorrelation(uetr, correlation);
        }
        
        return correlation;
    }
    
    /**
     * Batch UETR lookup for performance
     */
    public Map<String, UETRCorrelation> findUETRCorrelations(List<String> uetrs) {
        Map<String, UETRCorrelation> results = new HashMap<>();
        
        // Batch cache lookup
        List<String> cacheKeys = uetrs.stream()
            .map(uetr -> "uetr:correlation:" + uetr)
            .collect(Collectors.toList());
        
        List<Object> cached = redisTemplate.opsForValue().multiGet(cacheKeys);
        
        // Process cached results
        for (int i = 0; i < uetrs.size(); i++) {
            if (cached.get(i) != null) {
                // Process cached result
                // ... implementation details
            }
        }
        
        // Batch database lookup for missing UETRs
        List<String> missingUetrs = uetrs.stream()
            .filter(uetr -> !results.containsKey(uetr))
            .collect(Collectors.toList());
        
        if (!missingUetrs.isEmpty()) {
            List<UETRCorrelation> dbResults = uetrCorrelationRepository.findByUetrIn(missingUetrs);
            for (UETRCorrelation correlation : dbResults) {
                results.put(correlation.getUetr().toString(), correlation);
                cacheUETRCorrelation(correlation.getUetr().toString(), correlation);
            }
        }
        
        return results;
    }
}
```

### **Database Indexing Strategy**
```sql
-- PostgreSQL indexes for UETR performance
CREATE INDEX CONCURRENTLY idx_uetr_correlation_uetr 
ON uetr_correlation(uetr);

CREATE INDEX CONCURRENTLY idx_uetr_correlation_tenant_status 
ON uetr_correlation(tenant_id, correlation_status);

CREATE INDEX CONCURRENTLY idx_iso20022_message_details_uetr 
ON iso20022_message_details(uetr);

-- Cassandra materialized views for UETR queries
CREATE MATERIALIZED VIEW uetr_by_tenant AS
SELECT uetr, tenant_id, payment_id, correlation_status, created_at
FROM uetr_correlation
WHERE tenant_id IS NOT NULL
PRIMARY KEY (tenant_id, created_at, uetr);
```

## 🔒 **Security & Compliance**

### **UETR Security**
- **Encryption**: UETR values encrypted at rest
- **Access Control**: Role-based access to UETR data
- **Audit Trail**: Complete UETR access logging
- **Data Retention**: 7-year UETR retention for compliance

### **Compliance Features**
- **Global Uniqueness**: UETR ensures global uniqueness
- **End-to-End Tracking**: Complete payment journey tracking
- **Audit Trail**: Immutable UETR correlation history
- **Regulatory Compliance**: Meets ISO 20022 standards

## 📊 **Monitoring & Observability**

### **UETR Metrics**
```yaml
UETR Metrics:
  - uetr_generation_rate
  - uetr_lookup_latency
  - uetr_correlation_success_rate
  - uetr_cache_hit_ratio
  - uetr_message_chain_completeness
```

### **Performance Monitoring**
```java
@Component
public class UETRPerformanceMonitor {
    
    @EventListener
    public void onUETRGenerated(UETRGeneratedEvent event) {
        // Track UETR generation metrics
        meterRegistry.counter("uetr.generated").increment();
    }
    
    @EventListener
    public void onUETRLookup(UETRLookupEvent event) {
        // Track UETR lookup performance
        Timer.Sample sample = Timer.start(meterRegistry);
        sample.stop(Timer.builder("uetr.lookup.duration").register(meterRegistry));
    }
}
```

---

**Version**: 2.0  
**Last Updated**: 2025-01-27  
**Status**: 🚀 In Development  
**Next Review**: Weekly during implementation
