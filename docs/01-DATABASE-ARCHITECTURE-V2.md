# Database Architecture v2 - Polyglot Persistence Strategy

## 🎯 **Database Architecture Overview**

The Payments Engine v2 employs a **polyglot persistence** strategy, leveraging multiple database technologies optimized for specific use cases while maintaining data consistency and performance.

## 🏗️ **Database Technology Stack**

### **1. PostgreSQL - Core Transactional Layer**
**Purpose**: ACID-compliant transactional data, payment state management, and financial integrity

**Key Features**:
- **ACID Compliance**: Strong consistency for financial transactions
- **JSON/XML Support**: Native support for ISO 20022 message storage
- **Full-Text Search**: Advanced search capabilities for message content
- **Partitioning**: Time-based partitioning for high-volume data
- **Multi-AZ Deployment**: High availability with automatic failover

### **2. Cassandra - High-Volume Message Storage**
**Purpose**: ISO 20022 message storage, high-throughput processing, and horizontal scaling

**Key Features**:
- **Horizontal Scaling**: Linear scaling with additional nodes
- **High Availability**: No single point of failure
- **Time-Series Optimization**: Optimized for timestamped data
- **Schema Flexibility**: Dynamic schema evolution for message formats
- **Multi-Region**: Cross-region replication for disaster recovery

### **3. Redis - Real-Time Caching**
**Purpose**: Session management, real-time lookups, and performance optimization

**Key Features**:
- **Sub-millisecond Latency**: Ultra-fast read/write operations
- **Memory Optimization**: Efficient memory usage with data structures
- **Clustering**: High availability with Redis Cluster
- **Persistence**: RDB and AOF for data durability
- **Pub/Sub**: Real-time event distribution

### **4. EventStore - Immutable Audit Trails**
**Purpose**: Event sourcing, audit trails, and message correlation history

**Key Features**:
- **Immutable Events**: Append-only event storage
- **Event Sourcing**: Complete event history for replay
- **Audit Compliance**: Regulatory compliance for financial data
- **Stream Processing**: Real-time event processing
- **Snapshot Support**: Efficient state reconstruction

### **5. TimescaleDB - Operational Intelligence**
**Purpose**: Time-series analytics, monitoring, and operational metrics

**Key Features**:
- **Time-Series Optimization**: Specialized for timestamped data
- **Compression**: Automatic data compression for storage efficiency
- **Aggregations**: Built-in time-series aggregations
- **Retention Policies**: Automatic data lifecycle management
- **PostgreSQL Compatibility**: Seamless integration with PostgreSQL

## 🗃️ **Database Schema Design**

### **PostgreSQL Schema - Core Transactional Data**

#### **Payments Table**
```sql
-- Core payment data with UETR correlation
CREATE TABLE payments (
  id UUID PRIMARY KEY,
  uetr UUID UNIQUE NOT NULL, -- ISO 20022 UETR
  tenant_id VARCHAR(50) NOT NULL,
  status VARCHAR(20) NOT NULL,
  amount DECIMAL(15,2) NOT NULL,
  currency VARCHAR(3) NOT NULL,
  source_account VARCHAR(50),
  destination_account VARCHAR(50),
  payment_type VARCHAR(20),
  priority VARCHAR(20),
  initiated_by VARCHAR(100),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  completed_at TIMESTAMP,
  failed_at TIMESTAMP
);

-- Indexes for performance
CREATE INDEX idx_payments_uetr ON payments(uetr);
CREATE INDEX idx_payments_tenant_status ON payments(tenant_id, status);
CREATE INDEX idx_payments_created_at ON payments(created_at);
CREATE INDEX idx_payments_status ON payments(status) WHERE status IN ('PROCESSING', 'PENDING');
```

#### **ISO 20022 Message Correlation**
```sql
-- Message correlation with UETR
CREATE TABLE iso20022_message_correlation (
  payment_id UUID REFERENCES payments(id),
  uetr UUID NOT NULL,
  pain001_message_id VARCHAR(50),
  pacs008_message_id VARCHAR(50),
  pacs002_message_id VARCHAR(50),
  pacs004_message_id VARCHAR(50),
  camt054_message_id VARCHAR(50),
  correlation_status VARCHAR(20),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (payment_id, uetr)
);

-- Message details
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
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

#### **Audit Trail**
```sql
-- Immutable audit trail
CREATE TABLE payment_audit_log (
  id UUID PRIMARY KEY,
  payment_id UUID REFERENCES payments(id),
  uetr UUID NOT NULL,
  action VARCHAR(50) NOT NULL,
  timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  user_id VARCHAR(50),
  iso20022_message_id VARCHAR(50),
  raw_message TEXT,
  hash VARCHAR(64) -- For integrity verification
);

-- Partitioning for high volume
CREATE TABLE payment_audit_log_2025_01 PARTITION OF payment_audit_log
FOR VALUES FROM ('2025-01-01') TO ('2025-02-01');
```

### **Cassandra Schema - High-Volume Message Storage**

#### **ISO 20022 Messages**
```sql
-- Keyspace for ISO 20022 messages
CREATE KEYSPACE iso20022_messages 
WITH REPLICATION = {
  'class': 'NetworkTopologyStrategy',
  'datacenter1': 3,
  'datacenter2': 3
};

-- pain.001 messages
CREATE TABLE pain001_messages (
  uetr UUID,
  message_id TEXT,
  tenant_id TEXT,
  payment_id UUID,
  raw_xml TEXT,
  parsed_json TEXT,
  validation_status TEXT,
  processing_status TEXT,
  created_at TIMESTAMP,
  PRIMARY KEY (uetr, message_id)
) WITH CLUSTERING ORDER BY (created_at DESC)
  AND COMPACTION = {
    'class': 'TimeWindowCompactionStrategy',
    'compaction_window_size': '1',
    'compaction_window_unit': 'DAYS'
  };

-- pacs.008 messages
CREATE TABLE pacs008_messages (
  uetr UUID,
  message_id TEXT,
  tenant_id TEXT,
  payment_id UUID,
  original_pain001_id TEXT,
  raw_xml TEXT,
  parsed_json TEXT,
  status TEXT,
  created_at TIMESTAMP,
  PRIMARY KEY (uetr, message_id)
) WITH CLUSTERING ORDER BY (created_at DESC);

-- pacs.002 messages
CREATE TABLE pacs002_messages (
  uetr UUID,
  message_id TEXT,
  tenant_id TEXT,
  payment_id UUID,
  original_message_id TEXT,
  original_transaction_id TEXT,
  status_code TEXT,
  status_reason TEXT,
  raw_xml TEXT,
  created_at TIMESTAMP,
  PRIMARY KEY (uetr, message_id)
) WITH CLUSTERING ORDER BY (created_at DESC);

-- UETR correlation
CREATE TABLE uetr_correlation (
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

#### **Message Processing Status**
```sql
-- Message processing status
CREATE TABLE message_processing_status (
  uetr UUID,
  message_id TEXT,
  processing_status TEXT,
  error_message TEXT,
  retry_count INT,
  last_processed_at TIMESTAMP,
  created_at TIMESTAMP,
  PRIMARY KEY (uetr, message_id)
) WITH DEFAULT TTL = 604800; -- 7 days
```

### **Redis Schema - Real-Time Caching**

#### **Cache Keys Structure**
```yaml
# Payment status cache
payment:status:{payment_id} -> {
  "status": "PROCESSING",
  "uetr": "12345678-1234-1234-1234-123456789012",
  "updated_at": "2025-01-27T10:00:00Z"
}

# UETR lookup cache
uetr:lookup:{uetr} -> {
  "payment_id": "payment-12345",
  "tenant_id": "tenant-001",
  "status": "PROCESSING"
}

# Message correlation cache
correlation:{uetr} -> {
  "pain001_id": "pain.001.20250127.001",
  "pacs008_id": "pacs.008.20250127.001",
  "pacs002_id": "pacs.002.20250127.001"
}

# Tenant configuration cache
tenant:config:{tenant_id} -> {
  "settings": {...},
  "limits": {...},
  "features": {...}
}
```

### **EventStore Schema - Immutable Audit**

#### **Event Streams**
```csharp
// Payment events stream
public class PaymentEvent
{
    public string PaymentId { get; set; }
    public string UETR { get; set; }
    public string EventType { get; set; }
    public DateTime Timestamp { get; set; }
    public string Data { get; set; }
    public string Hash { get; set; }
}

// ISO 20022 events stream
public class Iso20022Event
{
    public string UETR { get; set; }
    public string MessageId { get; set; }
    public string MessageType { get; set; }
    public DateTime Timestamp { get; set; }
    public string RawMessage { get; set; }
    public string Hash { get; set; }
}
```

### **TimescaleDB Schema - Operational Intelligence**

#### **Performance Metrics**
```sql
-- Payment metrics
CREATE TABLE payment_metrics (
  time TIMESTAMP,
  tenant_id TEXT,
  status TEXT,
  volume INTEGER,
  latency_ms INTEGER,
  error_rate DECIMAL(5,2)
);

-- Message processing metrics
CREATE TABLE message_metrics (
  time TIMESTAMP,
  message_type TEXT,
  tenant_id TEXT,
  volume INTEGER,
  processing_time_ms INTEGER,
  validation_time_ms INTEGER,
  error_count INTEGER
);

-- System performance metrics
CREATE TABLE system_metrics (
  time TIMESTAMP,
  service_name TEXT,
  cpu_usage DECIMAL(5,2),
  memory_usage DECIMAL(5,2),
  disk_usage DECIMAL(5,2),
  network_io INTEGER
);
```

## 🔄 **Data Flow Architecture**

### **1. Payment Initiation Flow**
```
Client Request → PostgreSQL (payment creation)
├── UETR Generation → Redis (correlation cache)
├── pain.001 Processing → Cassandra (message storage)
├── Event Publishing → EventStore (audit trail)
└── Metrics Collection → TimescaleDB (monitoring)
```

### **2. Message Processing Flow**
```
ISO 20022 Message → Cassandra (ingestion)
├── Validation → PostgreSQL (state update)
├── Processing → Redis (status cache)
├── Correlation → EventStore (audit trail)
└── Metrics → TimescaleDB (analytics)
```

### **3. Status Reporting Flow**
```
Status Change → PostgreSQL (state update)
├── pain.002 Generation → Cassandra (message storage)
├── Client Notification → Redis (delivery status)
├── Audit Trail → EventStore (immutable record)
└── Metrics → TimescaleDB (performance data)
```

## ⚡ **Performance Optimization**

### **Database Performance Targets**
```
Database             | Read Latency | Write Latency | Throughput
--------------------|--------------|---------------|------------
PostgreSQL          | <10ms        | <20ms         | 2,000 TPS
Cassandra           | <5ms         | <10ms         | 8,200 msg/sec
Redis               | <1ms         | <2ms          | 50,000 ops/sec
EventStore          | <5ms         | <10ms         | 8,200 events/sec
TimescaleDB         | <10ms        | <20ms         | 1,000 metrics/sec
```

### **Optimization Strategies**

#### **PostgreSQL Optimization**
```sql
-- Partitioning for high volume
CREATE TABLE payments_2025_01 PARTITION OF payments
FOR VALUES FROM ('2025-01-01') TO ('2025-02-01');

-- Index optimization
CREATE INDEX CONCURRENTLY idx_payments_tenant_status 
ON payments (tenant_id, status) 
WHERE status IN ('PROCESSING', 'PENDING');

-- Connection pooling
-- max_connections = 200
-- shared_buffers = 256MB
-- effective_cache_size = 1GB
```

#### **Cassandra Optimization**
```sql
-- Compaction strategy
ALTER TABLE pain001_messages WITH COMPACTION = {
  'class': 'TimeWindowCompactionStrategy',
  'compaction_window_size': '1',
  'compaction_window_unit': 'DAYS'
};

-- Materialized views
CREATE MATERIALIZED VIEW pain001_by_status AS
SELECT message_id, tenant_id, status, created_at
FROM pain001_messages
WHERE status IS NOT NULL
PRIMARY KEY (status, created_at, message_id);
```

#### **Redis Optimization**
```yaml
# Redis configuration
maxmemory: 2gb
maxmemory-policy: allkeys-lru
save: "900 1 300 10 60 10000"
appendonly: yes
appendfsync: everysec
```

## 🔒 **Data Consistency & Integrity**

### **Consistency Patterns**
- **Strong Consistency**: PostgreSQL for core payment state
- **Eventual Consistency**: Cassandra for message processing
- **Causal Consistency**: EventStore for audit trails
- **Session Consistency**: Redis for real-time operations

### **Data Integrity**
```sql
-- Foreign key constraints
ALTER TABLE iso20022_message_correlation 
ADD CONSTRAINT fk_payment_id 
FOREIGN KEY (payment_id) REFERENCES payments(id);

-- Check constraints
ALTER TABLE payments 
ADD CONSTRAINT chk_amount_positive 
CHECK (amount > 0);

-- Unique constraints
ALTER TABLE payments 
ADD CONSTRAINT uk_uetr 
UNIQUE (uetr);
```

### **Audit Trail Integrity**
```csharp
// Hash verification for audit integrity
public class AuditIntegrity
{
    public string CalculateHash(string data)
    {
        using (var sha256 = SHA256.Create())
        {
            var hash = sha256.ComputeHash(Encoding.UTF8.GetBytes(data));
            return Convert.ToBase64String(hash);
        }
    }
    
    public bool VerifyHash(string data, string hash)
    {
        return CalculateHash(data) == hash;
    }
}
```

## 🚀 **Deployment Architecture**

### **Multi-AZ Azure Deployment**
```
Primary Region (East US):
├── PostgreSQL (Multi-AZ, 3 nodes)
├── Cassandra (3 nodes per AZ, 9 total)
├── Redis (3 nodes per AZ, 6 total)
├── EventStore (3 nodes per AZ, 9 total)
└── TimescaleDB (Multi-AZ, 3 nodes)

Secondary Region (West US):
├── PostgreSQL (Read Replica)
├── Cassandra (3 nodes per AZ, 9 total)
├── Redis (3 nodes per AZ, 6 total)
├── EventStore (3 nodes per AZ, 9 total)
└── TimescaleDB (Read Replica)
```

### **Database Connection Management**
```yaml
# Connection pooling configuration
PostgreSQL:
  max_connections: 200
  connection_timeout: 30s
  idle_timeout: 10m

Cassandra:
  max_connections_per_host: 8
  connection_timeout: 30s
  read_timeout: 30s

Redis:
  max_connections: 100
  connection_timeout: 5s
  command_timeout: 5s
```

## 📊 **Monitoring & Observability**

### **Database Metrics**
```yaml
PostgreSQL Metrics:
  - connection_count
  - query_performance
  - lock_wait_time
  - cache_hit_ratio

Cassandra Metrics:
  - read_latency
  - write_latency
  - compaction_operations
  - gc_pause_time

Redis Metrics:
  - memory_usage
  - hit_ratio
  - command_latency
  - connection_count
```

### **Performance Monitoring**
```sql
-- Query performance monitoring
SELECT 
  query,
  calls,
  total_time,
  mean_time,
  stddev_time
FROM pg_stat_statements
ORDER BY total_time DESC
LIMIT 10;
```

## 🔧 **Maintenance & Operations**

### **Backup Strategy**
```yaml
PostgreSQL:
  - Daily full backups
  - Continuous WAL archiving
  - Point-in-time recovery

Cassandra:
  - Snapshot backups
  - Incremental backups
  - Cross-region replication

Redis:
  - RDB snapshots
  - AOF persistence
  - Cross-region replication
```

### **Data Lifecycle Management**
```sql
-- Automatic data archival
CREATE TABLE payment_audit_log_archive (
  LIKE payment_audit_log
) PARTITION BY RANGE (created_at);

-- Data retention policies
CREATE POLICY payment_retention ON payment_audit_log
FOR ALL TO public
USING (created_at > CURRENT_DATE - INTERVAL '7 years');
```

---

**Version**: 2.0  
**Last Updated**: 2025-01-27  
**Status**: 🚀 In Development  
**Next Review**: Weekly during implementation
