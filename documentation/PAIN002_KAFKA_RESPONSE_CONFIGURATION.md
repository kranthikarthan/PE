# 🔄 **pain.002 Kafka Response Configuration Guide**

## 📋 **Overview**

The Payment Engine now supports **three configurable response modes** for pain.001 (Customer Credit Transfer Initiation) requests:

1. **SYNCHRONOUS** - Immediate API response with pain.002 message
2. **ASYNCHRONOUS** - Async API response with callback mechanism  
3. **🆕 KAFKA_TOPIC** - pain.002 response published to payment-type-specific Kafka topics

This enhancement provides **complete flexibility** in how payment status responses are delivered, enabling:
- **Event-driven response processing**
- **Decoupled response handling** 
- **Payment-type-specific routing**
- **Scalable response processing**

---

## ⚙️ **Response Mode Configuration**

### **Configuration Options**

Each payment type can be configured with one of three response modes:

```yaml
payment_types:
  - code: "RTP"
    name: "Real-Time Payment"
    is_synchronous: true
    configuration:
      # Response configuration
      response_mode: "SYNCHRONOUS"  # SYNCHRONOUS | ASYNCHRONOUS | KAFKA_TOPIC
      kafka_response_config:
        enabled: false
        use_payment_type_specific_topic: true
        topic_pattern: "payment-engine.{tenantId}.responses.{paymentType}.pain002"
        include_original_message: true
        priority: "HIGH"
        target_systems: ["core-banking", "notification-service"]
        retry_policy:
          max_retries: 3
          backoff_ms: 1000
          
  - code: "ACH_CREDIT"
    name: "ACH Credit Transfer"
    is_synchronous: false
    configuration:
      # Use Kafka topic for ACH responses
      response_mode: "KAFKA_TOPIC"
      kafka_response_config:
        enabled: true
        use_payment_type_specific_topic: true
        topic_pattern: "payment-engine.{tenantId}.responses.{paymentType}.pain002"
        include_original_message: false
        priority: "NORMAL"
        target_systems: ["ach-processor", "notification-service"]
        retry_policy:
          max_retries: 5
          backoff_ms: 2000
```

---

## 🔧 **Runtime Configuration Management**

### **Get Payment Type Response Configuration**

```bash
GET /api/v1/config/tenants/{tenantId}/payment-types/{paymentTypeCode}/response-config
Authorization: Bearer <token>
X-Tenant-ID: {tenantId}

# Response
{
  "tenantId": "regional-bank",
  "paymentTypeCode": "ACH_CREDIT",
  "responseMode": "KAFKA_TOPIC",
  "kafkaResponseConfig": {
    "enabled": true,
    "usePaymentTypeSpecificTopic": true,
    "topicPattern": "payment-engine.{tenantId}.responses.{paymentType}.pain002",
    "priority": "NORMAL",
    "targetSystems": ["ach-processor", "notification-service"],
    "retryPolicy": {
      "maxRetries": 5,
      "backoffMs": 2000
    }
  }
}
```

### **Update Payment Type Response Configuration**

```bash
PUT /api/v1/config/tenants/{tenantId}/payment-types/{paymentTypeCode}/response-config
Authorization: Bearer <token>
X-Tenant-ID: {tenantId}
Content-Type: application/json

{
  "responseMode": "KAFKA_TOPIC",
  "kafkaResponseConfig": {
    "enabled": true,
    "usePaymentTypeSpecificTopic": true,
    "topicPattern": "payment-engine.{tenantId}.responses.{paymentType}.pain002",
    "includeOriginalMessage": false,
    "priority": "HIGH",
    "targetSystems": ["core-banking", "notification-service", "audit-service"],
    "retryPolicy": {
      "maxRetries": 3,
      "backoffMs": 1000
    }
  }
}

# Response
{
  "message": "Payment type response configuration updated successfully",
  "tenantId": "regional-bank",
  "paymentTypeCode": "ACH_CREDIT",
  "responseMode": "KAFKA_TOPIC"
}
```

---

## 📨 **Kafka Topic Architecture**

### **Topic Naming Patterns**

#### **Payment-Type-Specific Topics**
```
payment-engine.{tenantId}.responses.{paymentType}.pain002

Examples:
- payment-engine.regional-bank.responses.rtp.pain002
- payment-engine.regional-bank.responses.ach_credit.pain002
- payment-engine.regional-bank.responses.wire_transfer.pain002
- payment-engine.fintech-corp.responses.crypto_transfer.pain002
```

#### **Tenant Default Topics**
```
payment-engine.{tenantId}.responses.pain002

Examples:
- payment-engine.regional-bank.responses.pain002
- payment-engine.fintech-corp.responses.pain002
```

#### **Explicit Topic Names**
```yaml
# Custom topic configuration
kafka_response_config:
  explicit_topic_name: "custom-bank-responses"
```

### **Topic Message Structure**

```json
{
  "messageType": "pain.002.001.03",
  "pain002Message": {
    "GrpHdr": {
      "MsgId": "PAIN002-1705312200123",
      "CreDtTm": "2024-01-15T10:30:00.123Z",
      "InitgPty": {
        "Nm": "Payment Engine"
      }
    },
    "OrgnlGrpInfAndSts": {
      "OrgnlMsgId": "MSG-20240115-001",
      "OrgnlMsgNmId": "pain.001.001.03",
      "GrpSts": "ACCP"
    },
    "TxInfAndSts": [
      {
        "StsId": "STS-1705312200123",
        "OrgnlEndToEndId": "E2E-20240115-001",
        "OrgnlTxId": "TXN-20240115-001",
        "TxSts": "ACCP"
      }
    ]
  },
  "tenantId": "regional-bank",
  "paymentType": "ACH_CREDIT",
  "originalMessageId": "MSG-20240115-001",
  "responseMessageId": "PAIN002-1705312200123",
  "publishedAt": "2024-01-15T10:30:00.123Z",
  "responseMode": "KAFKA_TOPIC",
  "responseConfig": {
    "usePaymentTypeSpecificTopic": true,
    "priority": "NORMAL",
    "targetSystems": ["ach-processor", "notification-service"]
  },
  "processingInfo": {
    "processedBy": "payment-engine-core-banking",
    "processingVersion": "1.0.0",
    "processingTimestamp": "2024-01-15T10:30:00.123Z"
  },
  "routingInfo": {
    "targetSystems": ["ach-processor", "notification-service"],
    "priority": "NORMAL",
    "retryPolicy": {
      "maxRetries": 5,
      "backoffMs": 2000
    }
  }
}
```

---

## 🔄 **Response Mode Behavior**

### **1. SYNCHRONOUS Mode**

```bash
# Request
POST /api/v1/iso20022/pain001
X-Tenant-ID: regional-bank
{
  "CstmrCdtTrfInitn": {
    "GrpHdr": {"MsgId": "MSG-20240115-001", ...},
    "PmtInf": {...}
  }
}

# Immediate Response
{
  "responseMode": "SYNCHRONOUS",
  "pain002Message": {
    "GrpHdr": {"MsgId": "PAIN002-1705312200123", ...},
    "OrgnlGrpInfAndSts": {"OrgnlMsgId": "MSG-20240115-001", "GrpSts": "ACCP"},
    "TxInfAndSts": [{"OrgnlEndToEndId": "E2E-20240115-001", "TxSts": "ACCP"}]
  },
  "transactionId": "TXN-20240115-001",
  "status": "SUCCESS",
  "immediate": true
}
```

### **2. ASYNCHRONOUS Mode**

```bash
# Request
POST /api/v1/iso20022/pain001
X-Tenant-ID: regional-bank

# Immediate Response
{
  "responseMode": "ASYNCHRONOUS",
  "transactionId": "TXN-20240115-002",
  "status": "ACCEPTED_FOR_PROCESSING",
  "message": "Payment accepted for processing. Status will be provided via callback.",
  "callbackScheduled": true
}

# Later: Callback with pain.002 response
# POST https://client-webhook-url.com/payment-status
# {pain.002 message}
```

### **3. 🆕 KAFKA_TOPIC Mode**

```bash
# Request
POST /api/v1/iso20022/pain001
X-Tenant-ID: regional-bank

# Immediate Response
{
  "responseMode": "KAFKA_TOPIC",
  "transactionId": "TXN-20240115-003",
  "status": "ACCEPTED_FOR_PROCESSING",
  "message": "Payment accepted. Status response will be published to Kafka topic.",
  "kafkaTopicName": "payment-engine.regional-bank.responses.ach_credit.pain002",
  "originalMessageId": "MSG-20240115-001",
  "responseMessageId": "PAIN002-1705312200123",
  "kafkaPublished": true
}

# Later: pain.002 message published to Kafka topic
# Topic: payment-engine.regional-bank.responses.ach_credit.pain002
# Message: {complete pain.002 message with metadata}
```

---

## 🎯 **Use Cases and Examples**

### **Use Case 1: Real-Time Payments (Synchronous)**

```yaml
# Configuration for RTP
- code: "RTP"
  configuration:
    response_mode: "SYNCHRONOUS"
    # Immediate response needed for real-time payments
```

```bash
# Result: Immediate pain.002 response in API call
POST /api/v1/iso20022/pain001 → Immediate pain.002 response
```

### **Use Case 2: ACH Transfers (Kafka Topic)**

```yaml
# Configuration for ACH
- code: "ACH_CREDIT"
  configuration:
    response_mode: "KAFKA_TOPIC"
    kafka_response_config:
      enabled: true
      use_payment_type_specific_topic: true
      target_systems: ["ach-processor", "notification-service"]
```

```bash
# Result: pain.002 published to ACH-specific topic
POST /api/v1/iso20022/pain001 → pain.002 published to:
payment-engine.regional-bank.responses.ach_credit.pain002
```

### **Use Case 3: Wire Transfers (Custom Topic)**

```yaml
# Configuration for Wire with custom topic
- code: "WIRE_TRANSFER"
  configuration:
    response_mode: "KAFKA_TOPIC"
    kafka_response_config:
      enabled: true
      explicit_topic_name: "wire-responses-high-priority"
      priority: "HIGH"
      target_systems: ["wire-processor", "compliance-service"]
```

```bash
# Result: pain.002 published to custom topic
POST /api/v1/iso20022/pain001 → pain.002 published to:
wire-responses-high-priority
```

### **Use Case 4: Cryptocurrency Transfers (Multi-System)**

```yaml
# Configuration for crypto with multiple target systems
- code: "CRYPTO_TRANSFER"
  configuration:
    response_mode: "KAFKA_TOPIC"
    kafka_response_config:
      enabled: true
      use_payment_type_specific_topic: true
      target_systems: ["blockchain-processor", "compliance-service", "audit-service"]
      priority: "HIGH"
      include_original_message: true
```

```bash
# Result: Enhanced pain.002 with crypto-specific routing
POST /api/v1/iso20022/pain001 → pain.002 published to:
payment-engine.regional-bank.responses.crypto_transfer.pain002
# With routing to blockchain-processor, compliance-service, audit-service
```

---

## 🔧 **Configuration Management**

### **Runtime Configuration Changes**

```bash
# Enable Kafka responses for RTP (was synchronous)
PUT /api/v1/config/tenants/regional-bank/payment-types/RTP/response-config
{
  "responseMode": "KAFKA_TOPIC",
  "kafkaResponseConfig": {
    "enabled": true,
    "usePaymentTypeSpecificTopic": true,
    "priority": "HIGH",
    "targetSystems": ["real-time-processor", "fraud-detection"]
  }
}

# Switch ACH back to synchronous (was Kafka)
PUT /api/v1/config/tenants/regional-bank/payment-types/ACH_CREDIT/response-config
{
  "responseMode": "SYNCHRONOUS"
}

# Configure custom topic for specific payment type
PUT /api/v1/config/tenants/regional-bank/payment-types/WIRE_TRANSFER/response-config
{
  "responseMode": "KAFKA_TOPIC",
  "kafkaResponseConfig": {
    "enabled": true,
    "explicitTopicName": "high-value-wire-responses",
    "priority": "CRITICAL",
    "includeOriginalMessage": true
  }
}
```

### **Per-Tenant Configuration**

Different tenants can have different response configurations for the same payment type:

```bash
# Regional Bank: Uses Kafka for ACH
PUT /api/v1/config/tenants/regional-bank/payment-types/ACH_CREDIT/response-config
{
  "responseMode": "KAFKA_TOPIC",
  "kafkaResponseConfig": {"enabled": true, "priority": "NORMAL"}
}

# FinTech Corp: Uses synchronous for ACH
PUT /api/v1/config/tenants/fintech-corp/payment-types/ACH_CREDIT/response-config
{
  "responseMode": "SYNCHRONOUS"
}
```

---

## 📊 **Monitoring and Observability**

### **Kafka Response Metrics**

```promql
# pain.002 messages published to Kafka
sum(rate(kafka_pain002_responses_published_total{tenant_id="regional-bank"}[5m])) by (payment_type)

# Kafka publishing failures
sum(rate(kafka_pain002_responses_failed_total{tenant_id="regional-bank"}[5m])) by (payment_type, error_type)

# Response mode distribution
count by (response_mode) (payment_response_mode_usage{tenant_id="regional-bank"})
```

### **Grafana Dashboard Panels**

```json
{
  "title": "pain.002 Response Modes",
  "targets": [
    {
      "expr": "sum(rate(pain002_responses_total{tenant_id=\"$tenant\"}[5m])) by (response_mode, payment_type)",
      "legendFormat": "{{payment_type}} - {{response_mode}}"
    }
  ]
}
```

---

## 🧪 **Testing Examples**

### **Test Synchronous Response**

```bash
# Configure RTP for synchronous responses
PUT /api/v1/config/tenants/test-bank/payment-types/RTP/response-config
{
  "responseMode": "SYNCHRONOUS"
}

# Test pain.001 request
POST /api/v1/iso20022/pain001
X-Tenant-ID: test-bank
{
  "CstmrCdtTrfInitn": {
    "GrpHdr": {"MsgId": "TEST-MSG-001"},
    "PmtInf": {
      "PmtInfId": "TEST-PMT-001",
      "PmtMtd": "TRF",
      "CdtTrfTxInf": {
        "PmtId": {"EndToEndId": "TEST-E2E-001"},
        "Amt": {"InstdAmt": {"Ccy": "USD", "Value": "1000.00"}}
      }
    }
  }
}

# Expected Response: Immediate pain.002 in API response
{
  "responseMode": "SYNCHRONOUS",
  "pain002Message": {
    "GrpHdr": {"MsgId": "PAIN002-...", "CreDtTm": "..."},
    "OrgnlGrpInfAndSts": {"OrgnlMsgId": "TEST-MSG-001", "GrpSts": "ACCP"}
  },
  "immediate": true
}
```

### **Test Kafka Topic Response**

```bash
# Configure ACH for Kafka responses
PUT /api/v1/config/tenants/test-bank/payment-types/ACH_CREDIT/response-config
{
  "responseMode": "KAFKA_TOPIC",
  "kafkaResponseConfig": {
    "enabled": true,
    "usePaymentTypeSpecificTopic": true
  }
}

# Test pain.001 request
POST /api/v1/iso20022/pain001
X-Tenant-ID: test-bank
# ... pain.001 message with ACH payment type

# Expected Response: Kafka publishing confirmation
{
  "responseMode": "KAFKA_TOPIC",
  "status": "ACCEPTED_FOR_PROCESSING",
  "message": "Payment accepted. Status response will be published to Kafka topic.",
  "kafkaTopicName": "payment-engine.test-bank.responses.ach_credit.pain002",
  "kafkaPublished": true
}

# Expected Kafka Message: pain.002 published to topic
# Topic: payment-engine.test-bank.responses.ach_credit.pain002
# Message: {complete pain.002 message with metadata}
```

### **Test Custom Topic Configuration**

```bash
# Configure with explicit topic name
PUT /api/v1/config/tenants/test-bank/payment-types/WIRE_TRANSFER/response-config
{
  "responseMode": "KAFKA_TOPIC",
  "kafkaResponseConfig": {
    "enabled": true,
    "explicitTopicName": "high-value-wire-responses",
    "priority": "CRITICAL"
  }
}

# Test pain.001 wire transfer
# Expected: pain.002 published to "high-value-wire-responses" topic
```

---

## 🔄 **Consumer Implementation Examples**

### **Java Kafka Consumer**

```java
@KafkaListener(topics = "payment-engine.#{tenantId}.responses.ach_credit.pain002")
public void handleAchPain002Response(
        @Payload Map<String, Object> message,
        @Header("X-Tenant-ID") String tenantId) {
    
    logger.info("Received pain.002 response for tenant: {}", tenantId);
    
    // Extract pain.002 message
    @SuppressWarnings("unchecked")
    Map<String, Object> pain002Message = (Map<String, Object>) message.get("pain002Message");
    
    // Process response based on status
    String groupStatus = (String) ((Map<String, Object>) pain002Message.get("OrgnlGrpInfAndSts")).get("GrpSts");
    
    switch (groupStatus) {
        case "ACCP":
            handleAcceptedPayment(message);
            break;
        case "RJCT":
            handleRejectedPayment(message);
            break;
        case "PDNG":
            handlePendingPayment(message);
            break;
    }
}
```

### **Node.js Kafka Consumer**

```javascript
const kafka = require('kafkajs');

const consumer = kafka.consumer({ groupId: 'ach-response-processor' });

await consumer.subscribe({ 
  topic: 'payment-engine.regional-bank.responses.ach_credit.pain002' 
});

await consumer.run({
  eachMessage: async ({ topic, partition, message }) => {
    const responseData = JSON.parse(message.value.toString());
    
    console.log('Received pain.002 response:', {
      tenantId: responseData.tenantId,
      paymentType: responseData.paymentType,
      originalMessageId: responseData.originalMessageId,
      status: responseData.pain002Message.OrgnlGrpInfAndSts.GrpSts
    });
    
    // Process the response
    await processAchResponse(responseData);
  },
});
```

---

## ⚙️ **Advanced Configuration Options**

### **Priority-Based Routing**

```yaml
kafka_response_config:
  priority: "HIGH"  # HIGH, NORMAL, LOW
  # HIGH priority messages can be routed to dedicated high-performance topics
```

### **Target System Routing**

```yaml
kafka_response_config:
  target_systems: 
    - "ach-processor"      # ACH processing system
    - "notification-service" # Customer notifications
    - "compliance-service"   # Regulatory reporting
    - "audit-service"       # Audit trail
```

### **Message Enrichment Options**

```yaml
kafka_response_config:
  include_original_message: true  # Include original pain.001 in response
  include_processing_metadata: true
  include_routing_information: true
```

### **Retry and Error Handling**

```yaml
kafka_response_config:
  retry_policy:
    max_retries: 5
    backoff_ms: 2000
    exponential_backoff: true
  error_handling:
    dead_letter_topic: "payment-engine.{tenantId}.responses.dlq"
    fallback_to_sync: true  # Fallback to synchronous on Kafka failure
```

---

## 🏆 **Benefits of Kafka Response Mode**

### **🔄 Event-Driven Architecture**
- **Decoupled Processing**: Response handling separated from payment processing
- **Scalable Consumption**: Multiple consumers can process responses independently
- **Reliable Delivery**: Kafka guarantees message delivery and ordering

### **📊 Payment-Type-Specific Processing**
- **Specialized Routing**: Different payment types routed to different systems
- **Custom Processing**: Payment-type-specific response processing logic
- **Performance Optimization**: Dedicated topics for high-volume payment types

### **🏢 Multi-Tenant Benefits**
- **Tenant Isolation**: Each tenant's responses isolated in separate topics
- **Custom Configuration**: Per-tenant response configuration
- **Resource Management**: Per-tenant topic management and monitoring

### **⚡ Operational Excellence**
- **Zero-Downtime Configuration**: Change response modes without restart
- **A/B Testing**: Test different response modes for different payment types
- **Gradual Migration**: Migrate from synchronous to Kafka responses gradually

---

This enhancement provides **complete flexibility** in pain.001/pain.002 response handling, enabling banks to choose the optimal response mechanism for each payment type while maintaining full configurability and multi-tenant isolation.