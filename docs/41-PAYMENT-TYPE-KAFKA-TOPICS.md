# Payment-Type-Specific Kafka Topics - Design Document

## Overview

This document details the **payment-type-specific Kafka topic configuration** feature for channel responses, allowing channels to configure separate Kafka topics for different payment types (EFT, RTC, RTGS, SWIFT, PayShap, etc.) during onboarding.

**Feature ID**: Enhancement to Feature 7.11 (Channel Onboarding UI)  
**Version**: 1.0  
**Date**: 2025-10-12  
**Status**: ✅ DESIGNED - Ready for Implementation

---

## Table of Contents

1. [Business Rationale](#business-rationale)
2. [Architecture Design](#architecture-design)
3. [Database Schema](#database-schema)
4. [Backend Implementation](#backend-implementation)
5. [Frontend Implementation](#frontend-implementation)
6. [Topic Naming Convention](#topic-naming-convention)
7. [Routing Logic](#routing-logic)
8. [Testing Strategy](#testing-strategy)
9. [Performance & Scalability](#performance--scalability)
10. [Migration Strategy](#migration-strategy)

---

## 1. Business Rationale

### 1.1 Why Payment-Type-Specific Topics?

**Current Limitation**:
- Single Kafka topic per channel for ALL payment types
- Mixed payment types in one consumer group
- Cannot apply different processing logic per payment type
- No isolation between payment types

**Benefits of Payment-Type-Specific Topics**:

1. **Isolation**: EFT payments don't block SWIFT payments
2. **Scalability**: Scale consumers independently per payment type
3. **Processing Logic**: Different processing for different payment types
4. **Monitoring**: Separate metrics per payment type
5. **Data Residency**: Route SWIFT payments to different regions
6. **Compliance**: Apply stricter compliance rules for SWIFT vs EFT

---

### 1.2 Use Cases

**Use Case 1: High-Volume Corporate Channel**
```
Corporate Partner X:
├─ EFT payments: 10K/day → Topic: payments.responses.eft.corp-x
├─ RTC payments: 1K/day → Topic: payments.responses.rtc.corp-x
├─ SWIFT payments: 100/day → Topic: payments.responses.swift.corp-x
└─ PayShap payments: 500/day → Topic: payments.responses.payshap.corp-x

Benefit: Scale EFT consumers independently without affecting SWIFT
```

**Use Case 2: Regional Processing**
```
International Bank:
├─ Domestic EFT → Topic: payments.responses.eft.bank-za (stays in SA)
├─ SWIFT → Topic: payments.responses.swift.bank-za (replicated to EU/US)
└─ PayShap → Topic: payments.responses.payshap.bank-za (stays in SA)

Benefit: Data residency compliance (GDPR, POPIA)
```

**Use Case 3: Different SLAs**
```
Payment Service Provider:
├─ EFT (standard): 1-hour SLA → 5 partitions, 2 consumers
├─ RTC (real-time): 5-second SLA → 10 partitions, 10 consumers
└─ SWIFT (critical): 10-minute SLA → 20 partitions, 20 consumers

Benefit: Meet different SLAs without over-provisioning
```

---

## 2. Architecture Design

### 2.1 High-Level Architecture

```
┌─────────────────────────────────────────────────────────────────────┐
│              PAYMENTS ENGINE (Notification Service)                  │
├─────────────────────────────────────────────────────────────────────┤
│                                                                      │
│  1. Payment completed (Payment ID: PAY-123, Type: EFT)              │
│  2. Get channel configuration (Channel ID: CHANNEL-001)             │
│  3. Check if payment-type topics enabled                            │
│  4. Lookup topic for payment type (EFT)                             │
│  5. Publish to topic: payments.responses.eft.tenant-001.channel-001 │
│                                                                      │
└──────────────────────────────┬──────────────────────────────────────┘
                               │ Kafka Publish
                               ▼
┌─────────────────────────────────────────────────────────────────────┐
│                      KAFKA CLUSTER (Confluent)                       │
├─────────────────────────────────────────────────────────────────────┤
│                                                                      │
│  Topic: payments.responses.eft.tenant-001.channel-001 (10 partitions)│
│  Topic: payments.responses.rtc.tenant-001.channel-001 (5 partitions) │
│  Topic: payments.responses.rtgs.tenant-001.channel-001 (3 partitions)│
│  Topic: payments.responses.swift.tenant-001.channel-001 (20 parts)  │
│  Topic: payments.responses.payshap.tenant-001.channel-001 (5 parts) │
│                                                                      │
└──────────────────────────────┬──────────────────────────────────────┘
                               │ Kafka Consume
                               ▼
┌─────────────────────────────────────────────────────────────────────┐
│                       CHANNEL (Consumer)                             │
├─────────────────────────────────────────────────────────────────────┤
│                                                                      │
│  EFT Consumer Group:      5 consumers → Topic: eft.*                │
│  RTC Consumer Group:      10 consumers → Topic: rtc.*               │
│  SWIFT Consumer Group:    20 consumers → Topic: swift.*             │
│  PayShap Consumer Group:  5 consumers → Topic: payshap.*            │
│                                                                      │
└─────────────────────────────────────────────────────────────────────┘
```

---

### 2.2 Component Interaction

**Sequence Diagram**:
```
Channel Admin → React UI → Backend API → Database → Kafka Admin API

1. Channel Admin opens onboarding wizard
2. Selects "Kafka" as response pattern
3. Toggles "Use payment-type topics" switch
4. Configures topics for EFT, RTC, SWIFT, etc.
5. Backend validates topic names
6. Backend saves to channel_kafka_topics table
7. Backend auto-creates Kafka topics (if not exist)
8. Channel Admin tests connection (sends test message)
9. Channel activated
```

**Runtime Flow**:
```
Payment Completed → Notification Service → Topic Lookup → Kafka Publish

1. Payment completed (Payment ID: PAY-123, Type: EFT)
2. Notification Service gets channel config
3. If kafkaUsePaymentTypeTopics = true:
   3a. Query channel_kafka_topics for payment_type = 'EFT'
   3b. Get topic name: payments.responses.eft.tenant-001.channel-001
4. Else:
   4a. Use default topic: payments.responses.tenant-001.channel-001
5. Publish Avro message to selected topic
6. Log: "Published to topic: {topicName}, paymentType: {type}"
```

---

## 3. Database Schema

### 3.1 Enhanced Channel Configurations Table

```sql
-- Add new column to channel_configurations
ALTER TABLE channel_configurations
ADD COLUMN kafka_use_payment_type_topics BOOLEAN DEFAULT FALSE;

COMMENT ON COLUMN channel_configurations.kafka_use_payment_type_topics IS 
'Enable payment-type-specific Kafka topics. If true, each payment type will have its own topic.';
```

---

### 3.2 New Table: Payment Type Topics

```sql
-- Payment-type-specific Kafka topic configuration
CREATE TABLE channel_kafka_topics (
    id SERIAL PRIMARY KEY,
    channel_id VARCHAR(50) NOT NULL,
    tenant_id VARCHAR(50) NOT NULL,
    payment_type VARCHAR(50) NOT NULL,  -- EFT, RTC, RTGS, SWIFT, PAYSHAP, CARD, BATCH, OTHER
    
    -- Kafka Topic Configuration
    kafka_topic VARCHAR(255) NOT NULL,
    kafka_consumer_group VARCHAR(255),
    kafka_partition_count INTEGER DEFAULT 5,
    
    -- Metadata
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100) NOT NULL,
    
    -- Constraints
    CONSTRAINT fk_kafka_topic_channel FOREIGN KEY (channel_id) REFERENCES channel_configurations(channel_id) ON DELETE CASCADE,
    CONSTRAINT fk_kafka_topic_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(tenant_id),
    CONSTRAINT uk_channel_payment_type UNIQUE (channel_id, payment_type),
    CONSTRAINT chk_payment_type CHECK (payment_type IN ('EFT', 'RTC', 'RTGS', 'SWIFT', 'PAYSHAP', 'CARD', 'BATCH', 'OTHER'))
);

-- Indexes
CREATE INDEX idx_kafka_topic_channel ON channel_kafka_topics(channel_id);
CREATE INDEX idx_kafka_topic_tenant ON channel_kafka_topics(tenant_id);
CREATE INDEX idx_kafka_topic_payment_type ON channel_kafka_topics(payment_type);

COMMENT ON TABLE channel_kafka_topics IS 
'Stores payment-type-specific Kafka topic configurations for channels that enable kafka_use_payment_type_topics';
```

---

### 3.3 Example Data

```sql
-- Channel configuration
INSERT INTO channel_configurations (
    channel_id, tenant_id, channel_name, channel_type, response_pattern,
    kafka_topic, kafka_consumer_group, kafka_partition_count,
    kafka_use_payment_type_topics, status, created_by
) VALUES (
    'CHANNEL-CORP-001',
    'TENANT-001',
    'Corporate Partner X',
    'CORPORATE',
    'KAFKA',
    'payments.responses.tenant-001.channel-corp-001',  -- Default fallback topic
    'corp-x-consumer-group',
    10,
    TRUE,  -- Enable payment-type topics
    'ACTIVE',
    'admin@corp-x.com'
);

-- Payment-type-specific topics
INSERT INTO channel_kafka_topics (channel_id, tenant_id, payment_type, kafka_topic, kafka_consumer_group, kafka_partition_count, created_by) VALUES
('CHANNEL-CORP-001', 'TENANT-001', 'EFT', 'payments.responses.eft.tenant-001.channel-corp-001', 'corp-x-eft-group', 10, 'admin@corp-x.com'),
('CHANNEL-CORP-001', 'TENANT-001', 'RTC', 'payments.responses.rtc.tenant-001.channel-corp-001', 'corp-x-rtc-group', 5, 'admin@corp-x.com'),
('CHANNEL-CORP-001', 'TENANT-001', 'RTGS', 'payments.responses.rtgs.tenant-001.channel-corp-001', 'corp-x-rtgs-group', 3, 'admin@corp-x.com'),
('CHANNEL-CORP-001', 'TENANT-001', 'SWIFT', 'payments.responses.swift.tenant-001.channel-corp-001', 'corp-x-swift-group', 20, 'admin@corp-x.com'),
('CHANNEL-CORP-001', 'TENANT-001', 'PAYSHAP', 'payments.responses.payshap.tenant-001.channel-corp-001', 'corp-x-payshap-group', 5, 'admin@corp-x.com'),
('CHANNEL-CORP-001', 'TENANT-001', 'CARD', 'payments.responses.card.tenant-001.channel-corp-001', 'corp-x-card-group', 10, 'admin@corp-x.com'),
('CHANNEL-CORP-001', 'TENANT-001', 'BATCH', 'payments.responses.batch.tenant-001.channel-corp-001', 'corp-x-batch-group', 2, 'admin@corp-x.com');
```

---

## 4. Backend Implementation

### 4.1 JPA Entity

```java
@Entity
@Table(name = "channel_kafka_topics")
public class ChannelKafkaTopic {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "channel_id", nullable = false)
    private String channelId;
    
    @Column(name = "tenant_id", nullable = false)
    private String tenantId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_type", nullable = false)
    private PaymentType paymentType;
    
    @Column(name = "kafka_topic", nullable = false)
    private String kafkaTopic;
    
    @Column(name = "kafka_consumer_group")
    private String kafkaConsumerGroup;
    
    @Column(name = "kafka_partition_count")
    private Integer kafkaPartitionCount = 5;
    
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
    
    @Column(name = "created_by", nullable = false)
    private String createdBy;
    
    // Getters, setters, equals, hashCode
}

public enum PaymentType {
    EFT,      // Electronic Funds Transfer
    RTC,      // Real-Time Clearing
    RTGS,     // Real-Time Gross Settlement (SAMOS)
    SWIFT,    // SWIFT International
    PAYSHAP,  // PayShap Instant P2P
    CARD,     // Card Payments
    BATCH,    // Batch Payments
    OTHER     // Other Payment Types
}
```

---

### 4.2 Repository

```java
@Repository
public interface ChannelKafkaTopicRepository extends JpaRepository<ChannelKafkaTopic, Long> {
    
    /**
     * Find Kafka topic configuration for a specific channel and payment type
     */
    Optional<ChannelKafkaTopic> findByChannelIdAndPaymentType(String channelId, PaymentType paymentType);
    
    /**
     * Get all payment-type topics for a channel
     */
    List<ChannelKafkaTopic> findByChannelId(String channelId);
    
    /**
     * Get all payment-type topics for a tenant
     */
    List<ChannelKafkaTopic> findByTenantId(String tenantId);
    
    /**
     * Delete all payment-type topics for a channel (cascade on channel deletion)
     */
    void deleteByChannelId(String channelId);
}
```

---

### 4.3 REST API

```java
@RestController
@RequestMapping("/api/v1/channels")
public class ChannelKafkaTopicController {
    
    @Autowired
    private ChannelKafkaTopicService kafkaTopicService;
    
    /**
     * Configure payment-type-specific Kafka topics
     * POST /api/v1/channels/{channelId}/kafka-topics
     */
    @PostMapping("/{channelId}/kafka-topics")
    public ResponseEntity<List<ChannelKafkaTopic>> configureKafkaTopics(
        @PathVariable String channelId,
        @RequestBody ConfigureKafkaTopicsRequest request,
        @RequestHeader("X-Tenant-ID") String tenantId,
        @RequestHeader("X-User-ID") String userId
    ) {
        List<ChannelKafkaTopic> topics = kafkaTopicService.configureTopics(
            channelId, tenantId, request.getTopics(), userId
        );
        return ResponseEntity.ok(topics);
    }
    
    /**
     * Get all payment-type topics for a channel
     * GET /api/v1/channels/{channelId}/kafka-topics
     */
    @GetMapping("/{channelId}/kafka-topics")
    public ResponseEntity<List<ChannelKafkaTopic>> getKafkaTopics(
        @PathVariable String channelId,
        @RequestHeader("X-Tenant-ID") String tenantId
    ) {
        List<ChannelKafkaTopic> topics = kafkaTopicService.getTopics(channelId, tenantId);
        return ResponseEntity.ok(topics);
    }
    
    /**
     * Test Kafka topic (send test message)
     * POST /api/v1/channels/{channelId}/kafka-topics/{paymentType}/test
     */
    @PostMapping("/{channelId}/kafka-topics/{paymentType}/test")
    public ResponseEntity<TestResult> testKafkaTopic(
        @PathVariable String channelId,
        @PathVariable PaymentType paymentType,
        @RequestHeader("X-Tenant-ID") String tenantId
    ) {
        TestResult result = kafkaTopicService.testTopic(channelId, paymentType, tenantId);
        return ResponseEntity.ok(result);
    }
}

@Data
public class ConfigureKafkaTopicsRequest {
    private List<PaymentTypeTopicConfig> topics;
}

@Data
public class PaymentTypeTopicConfig {
    private PaymentType paymentType;
    private String kafkaTopic;
    private String kafkaConsumerGroup;
    private Integer kafkaPartitionCount;
}
```

---

### 4.4 Service Implementation

```java
@Service
public class ChannelKafkaTopicService {
    
    @Autowired
    private ChannelKafkaTopicRepository kafkaTopicRepo;
    
    @Autowired
    private ChannelConfigurationRepository channelRepo;
    
    @Autowired
    private KafkaAdminService kafkaAdminService;
    
    @Transactional
    public List<ChannelKafkaTopic> configureTopics(
        String channelId,
        String tenantId,
        List<PaymentTypeTopicConfig> topicConfigs,
        String userId
    ) {
        // 1. Validate channel exists and belongs to tenant
        ChannelConfiguration channel = channelRepo.findByChannelIdAndTenantId(channelId, tenantId)
            .orElseThrow(() -> new ChannelNotFoundException(channelId));
        
        // 2. Enable payment-type topics
        channel.setKafkaUsePaymentTypeTopics(true);
        channelRepo.save(channel);
        
        // 3. Delete existing payment-type topics
        kafkaTopicRepo.deleteByChannelId(channelId);
        
        // 4. Create new payment-type topics
        List<ChannelKafkaTopic> topics = new ArrayList<>();
        for (PaymentTypeTopicConfig config : topicConfigs) {
            ChannelKafkaTopic topic = ChannelKafkaTopic.builder()
                .channelId(channelId)
                .tenantId(tenantId)
                .paymentType(config.getPaymentType())
                .kafkaTopic(config.getKafkaTopic())
                .kafkaConsumerGroup(config.getKafkaConsumerGroup())
                .kafkaPartitionCount(config.getKafkaPartitionCount())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .createdBy(userId)
                .build();
            
            // Validate topic name
            validateTopicName(topic.getKafkaTopic());
            
            // Save to database
            topic = kafkaTopicRepo.save(topic);
            
            // Auto-create Kafka topic (if not exists)
            kafkaAdminService.createTopicIfNotExists(
                topic.getKafkaTopic(),
                topic.getKafkaPartitionCount(),
                3  // replication factor
            );
            
            topics.add(topic);
        }
        
        log.info("Configured {} payment-type topics for channel: channelId={}", topics.size(), channelId);
        return topics;
    }
    
    private void validateTopicName(String topicName) {
        // Must match pattern: payments.responses.{payment-type}.{tenant-id}.{channel-id}
        if (!topicName.matches("^payments\\.responses\\.[a-z]+\\.[a-z0-9-]+\\.[a-z0-9-]+$")) {
            throw new InvalidTopicNameException("Topic name must match pattern: payments.responses.{payment-type}.{tenant-id}.{channel-id}");
        }
    }
}
```

---

## 5. Frontend Implementation

### 5.1 Enhanced Kafka Configuration Component

```tsx
// src/pages/ChannelOnboarding/KafkaConfiguration.tsx

import React, { useState } from 'react';
import {
  Box,
  TextField,
  Typography,
  Alert,
  Stack,
  Paper,
  Chip,
  Switch,
  FormControlLabel,
  Accordion,
  AccordionSummary,
  AccordionDetails,
} from '@mui/material';
import { ExpandMore as ExpandMoreIcon } from '@mui/icons-material';
import { useFormContext, Controller } from 'react-hook-form';

export default function KafkaConfiguration() {
  const { control, watch, setValue } = useFormContext();
  const tenantId = 'TENANT-001'; // From context
  const channelId = watch('channelId') || 'channel-001';
  const channelType = watch('channelType');
  const usePaymentTypeTopics = watch('kafkaUsePaymentTypeTopics') || false;
  
  const autoGeneratedTopic = `payments.responses.${channelType.toLowerCase()}.${tenantId}`;
  
  // Payment types available
  const paymentTypes = [
    { value: 'EFT', label: 'EFT (Electronic Funds Transfer)', icon: '💳', defaultPartitions: 10 },
    { value: 'RTC', label: 'RTC (Real-Time Clearing)', icon: '⚡', defaultPartitions: 5 },
    { value: 'RTGS', label: 'RTGS (SAMOS)', icon: '🏦', defaultPartitions: 3 },
    { value: 'SWIFT', label: 'SWIFT (International)', icon: '🌍', defaultPartitions: 20 },
    { value: 'PAYSHAP', label: 'PayShap (Instant P2P)', icon: '💸', defaultPartitions: 5 },
    { value: 'CARD', label: 'Card Payments', icon: '💳', defaultPartitions: 10 },
    { value: 'BATCH', label: 'Batch Payments', icon: '📦', defaultPartitions: 2 },
    { value: 'OTHER', label: 'Other Payment Types', icon: '📄', defaultPartitions: 5 },
  ];
  
  return (
    <Box>
      <Typography variant="h6" gutterBottom>
        Kafka Configuration
      </Typography>
      
      <Alert severity="info" sx={{ mb: 3 }}>
        <strong>High-Throughput Option</strong>: Kafka provides exactly-once semantics,
        message replay, and supports 100K+ msg/sec. Ideal for high-volume channels.
      </Alert>
      
      {/* Toggle: Single Topic vs Payment-Type-Specific Topics */}
      <Paper sx={{ p: 2, mb: 3, bgcolor: 'background.default' }}>
        <Controller
          name="kafkaUsePaymentTypeTopics"
          control={control}
          render={({ field }) => (
            <Box>
              <FormControlLabel
                control={<Switch {...field} checked={field.value} />}
                label={
                  <Box>
                    <Typography variant="subtitle1" sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                      Use separate Kafka topics per payment type
                      <Chip label="Advanced" size="small" color="primary" />
                    </Typography>
                    <Typography variant="caption" color="text.secondary">
                      {field.value
                        ? 'Each payment type (EFT, RTC, RTGS, SWIFT, etc.) will have its own dedicated Kafka topic for better isolation and scalability'
                        : 'All payment types will use a single Kafka topic (simpler setup, less control)'}
                    </Typography>
                  </Box>
                }
              />
            </Box>
          )}
        />
      </Paper>
      
      <Stack spacing={3}>
        {/* Conditional Rendering: Single Topic or Payment-Type Topics */}
        {!usePaymentTypeTopics ? (
          // Single Topic Configuration
          <>
            <Controller
              name="kafkaTopic"
              control={control}
              rules={{ required: 'Kafka topic is required' }}
              render={({ field, fieldState }) => (
                <TextField
                  {...field}
                  label="Kafka Topic Name"
                  placeholder={autoGeneratedTopic}
                  helperText={
                    fieldState.error?.message ||
                    'Auto-generated if left empty. Format: payments.responses.{channel}.{tenant}'
                  }
                  error={!!fieldState.error}
                  fullWidth
                />
              )}
            />
            
            <Controller
              name="kafkaConsumerGroup"
              control={control}
              rules={{ required: 'Consumer group is required' }}
              render={({ field, fieldState }) => (
                <TextField
                  {...field}
                  label="Consumer Group Name"
                  placeholder={`${channelType.toLowerCase()}-consumer-group`}
                  helperText={
                    fieldState.error?.message ||
                    'Unique identifier for your consumer group'
                  }
                  error={!!fieldState.error}
                  fullWidth
                />
              )}
            />
            
            <Controller
              name="kafkaPartitionCount"
              control={control}
              render={({ field }) => (
                <TextField
                  {...field}
                  label="Partition Count"
                  type="number"
                  inputProps={{ min: 1, max: 50 }}
                  helperText="Number of partitions (affects parallelism). Recommended: 5-10 for standard load"
                  fullWidth
                />
              )}
            />
          </>
        ) : (
          // Payment-Type-Specific Topics Configuration
          <Paper sx={{ p: 2, bgcolor: 'background.default' }}>
            <Typography variant="subtitle1" gutterBottom>
              Configure Kafka Topics Per Payment Type
            </Typography>
            <Typography variant="caption" color="text.secondary" sx={{ mb: 2, display: 'block' }}>
              Each payment type will publish to its own topic for better isolation, scalability, and monitoring
            </Typography>
            
            <Stack spacing={1}>
              {paymentTypes.map((paymentType) => (
                <Accordion key={paymentType.value}>
                  <AccordionSummary expandIcon={<ExpandMoreIcon />}>
                    <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                      <Typography sx={{ fontSize: '1.5rem' }}>{paymentType.icon}</Typography>
                      <Typography variant="body1">
                        <strong>{paymentType.label}</strong>
                      </Typography>
                    </Box>
                  </AccordionSummary>
                  <AccordionDetails>
                    <Stack spacing={2}>
                      <Controller
                        name={`kafkaPaymentTypeTopics.${paymentType.value}.topic`}
                        control={control}
                        defaultValue={`payments.responses.${paymentType.value.toLowerCase()}.${tenantId}.${channelId}`}
                        render={({ field }) => (
                          <TextField
                            {...field}
                            label="Kafka Topic Name"
                            placeholder={`payments.responses.${paymentType.value.toLowerCase()}.${tenantId}.${channelId}`}
                            size="small"
                            fullWidth
                            helperText={`Topic for ${paymentType.label} payment responses`}
                          />
                        )}
                      />
                      
                      <Controller
                        name={`kafkaPaymentTypeTopics.${paymentType.value}.consumerGroup`}
                        control={control}
                        defaultValue={`${channelType.toLowerCase()}-${paymentType.value.toLowerCase()}-group`}
                        render={({ field }) => (
                          <TextField
                            {...field}
                            label="Consumer Group"
                            placeholder={`${channelType.toLowerCase()}-${paymentType.value.toLowerCase()}-group`}
                            size="small"
                            fullWidth
                            helperText="Unique consumer group for this payment type"
                          />
                        )}
                      />
                      
                      <Controller
                        name={`kafkaPaymentTypeTopics.${paymentType.value}.partitions`}
                        control={control}
                        defaultValue={paymentType.defaultPartitions}
                        render={({ field }) => (
                          <TextField
                            {...field}
                            label="Partition Count"
                            type="number"
                            inputProps={{ min: 1, max: 50 }}
                            size="small"
                            fullWidth
                            helperText={`Recommended: ${paymentType.defaultPartitions} partitions for this payment type`}
                          />
                        )}
                      />
                    </Stack>
                  </AccordionDetails>
                </Accordion>
              ))}
            </Stack>
          </Paper>
        )}
      </Stack>
    </Box>
  );
}
```

---

## 6. Topic Naming Convention

### 6.1 Format

**Pattern**:
```
payments.responses.{payment-type}.{tenant-id}.{channel-id}
```

**Examples**:
```
payments.responses.eft.tenant-001.channel-web
payments.responses.rtc.tenant-001.channel-mobile
payments.responses.rtgs.tenant-002.channel-partner
payments.responses.swift.tenant-003.channel-corporate
payments.responses.payshap.tenant-001.channel-mobile
payments.responses.card.tenant-004.channel-branch
payments.responses.batch.tenant-001.channel-partner
```

---

### 6.2 Validation Rules

**Must**:
- Start with `payments.responses.`
- Use lowercase payment type (eft, rtc, rtgs, swift, payshap, card, batch, other)
- Use lowercase tenant ID (tenant-001)
- Use lowercase channel ID (channel-web)
- Use dots (.) as separators

**Must Not**:
- Use uppercase letters
- Use underscores (_)
- Exceed 255 characters
- Contain special characters (except dots and hyphens)

**Regex**:
```regex
^payments\.responses\.[a-z]+\.[a-z0-9-]+\.[a-z0-9-]+$
```

---

## 7. Routing Logic

### 7.1 Decision Tree

```
Payment Completed
    ↓
Get Channel Configuration
    ↓
Is response_pattern = KAFKA?
    ├─ NO → Route to Webhook/WebSocket/Push/Polling
    └─ YES → Continue
         ↓
    Is kafka_use_payment_type_topics = true?
         ├─ NO → Use default topic (kafka_topic)
         └─ YES → Continue
              ↓
         Query channel_kafka_topics for payment_type
              ├─ FOUND → Use payment-type-specific topic
              └─ NOT FOUND → Fallback to default topic
                   ↓
         Publish to Kafka Topic
              ↓
         Log: "Published to {topic}, paymentType: {type}"
```

---

### 7.2 Fallback Strategy

**Scenario 1: Payment-type topic not configured**
```java
if (channel.getKafkaUsePaymentTypeTopics()) {
    Optional<ChannelKafkaTopic> topicOpt = kafkaTopicRepo.findByChannelIdAndPaymentType(
        channel.getChannelId(), payment.getPaymentType()
    );
    
    if (topicOpt.isPresent()) {
        topicName = topicOpt.get().getKafkaTopic();
    } else {
        // Fallback to default topic
        topicName = channel.getKafkaTopic();
        log.warn("Payment-type topic not found, using default: paymentType={}, defaultTopic={}", 
            payment.getPaymentType(), topicName);
    }
}
```

**Scenario 2: Kafka topic creation fails**
```java
try {
    kafkaAdminService.createTopicIfNotExists(topicName, partitions, replicationFactor);
} catch (KafkaException e) {
    log.error("Failed to create Kafka topic: topic={}", topicName, e);
    // Allow channel onboarding to complete, but log error
    // Admin must manually create topic before activation
}
```

---

## 8. Testing Strategy

### 8.1 Unit Tests

```java
@SpringBootTest
class ChannelKafkaTopicServiceTest {
    
    @Autowired
    private ChannelKafkaTopicService kafkaTopicService;
    
    @Test
    void testConfigurePaymentTypeTopics() {
        // Given
        String channelId = "CHANNEL-001";
        String tenantId = "TENANT-001";
        List<PaymentTypeTopicConfig> configs = List.of(
            new PaymentTypeTopicConfig(PaymentType.EFT, "payments.responses.eft.tenant-001.channel-001", "eft-group", 10),
            new PaymentTypeTopicConfig(PaymentType.SWIFT, "payments.responses.swift.tenant-001.channel-001", "swift-group", 20)
        );
        
        // When
        List<ChannelKafkaTopic> topics = kafkaTopicService.configureTopics(channelId, tenantId, configs, "admin");
        
        // Then
        assertEquals(2, topics.size());
        assertEquals("payments.responses.eft.tenant-001.channel-001", topics.get(0).getKafkaTopic());
        assertEquals(10, topics.get(0).getKafkaPartitionCount());
    }
}
```

---

### 8.2 Integration Tests

```java
@SpringBootTest
@Testcontainers
class DynamicNotificationRouterIntegrationTest {
    
    @Container
    static KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.5.0"));
    
    @Test
    void testRouteToPaymentTypeSpecificTopic() {
        // Given: Channel with payment-type topics enabled
        ChannelConfiguration channel = createChannelWithPaymentTypeTopics();
        Payment payment = createPayment(PaymentType.EFT);
        
        // When: Route payment response
        notificationRouter.routePaymentResponse(payment, PaymentEventType.COMPLETED);
        
        // Then: Verify message published to EFT-specific topic
        ConsumerRecords<String, PaymentResponse> records = consumeFromTopic("payments.responses.eft.tenant-001.channel-001");
        assertEquals(1, records.count());
        assertEquals(payment.getPaymentId(), records.iterator().next().value().getPaymentId());
    }
}
```

---

### 8.3 E2E Tests (Cypress)

```typescript
// cypress/e2e/channel-onboarding-kafka-payment-types.cy.ts

describe('Channel Onboarding - Payment-Type-Specific Kafka Topics', () => {
  it('should configure separate topics for EFT and SWIFT', () => {
    // 1. Navigate to channel onboarding
    cy.visit('/channels/onboard');
    
    // 2. Step 1: Select channel type
    cy.get('[data-testid="channel-type-CORPORATE"]').click();
    cy.get('[data-testid="next-button"]').click();
    
    // 3. Step 2: Select Kafka response pattern
    cy.get('[data-testid="response-pattern-KAFKA"]').click();
    cy.get('[data-testid="next-button"]').click();
    
    // 4. Step 3: Enable payment-type topics
    cy.get('[data-testid="kafka-use-payment-type-topics-switch"]').click();
    
    // 5. Configure EFT topic
    cy.get('[data-testid="payment-type-EFT-accordion"]').click();
    cy.get('[name="kafkaPaymentTypeTopics.EFT.topic"]').type('payments.responses.eft.tenant-001.channel-corp-001');
    cy.get('[name="kafkaPaymentTypeTopics.EFT.consumerGroup"]').type('corp-eft-group');
    cy.get('[name="kafkaPaymentTypeTopics.EFT.partitions"]').clear().type('10');
    
    // 6. Configure SWIFT topic
    cy.get('[data-testid="payment-type-SWIFT-accordion"]').click();
    cy.get('[name="kafkaPaymentTypeTopics.SWIFT.topic"]').type('payments.responses.swift.tenant-001.channel-corp-001');
    cy.get('[name="kafkaPaymentTypeTopics.SWIFT.consumerGroup"]').type('corp-swift-group');
    cy.get('[name="kafkaPaymentTypeTopics.SWIFT.partitions"]').clear().type('20');
    
    // 7. Next step
    cy.get('[data-testid="next-button"]').click();
    
    // 8. Review and create
    cy.get('[data-testid="create-channel-button"]').click();
    
    // 9. Verify success
    cy.contains('Channel created successfully').should('be.visible');
  });
});
```

---

## 9. Performance & Scalability

### 9.1 Throughput Estimation

**Single Topic (Baseline)**:
```
Topic: payments.responses.tenant-001.channel-corp-001
├─ Partitions: 10
├─ Consumers: 10 (one per partition)
├─ Throughput: 100K msg/sec total
└─ Bottleneck: All payment types share same consumers
```

**Payment-Type Topics (Optimized)**:
```
EFT Topic: payments.responses.eft.tenant-001.channel-corp-001
├─ Partitions: 10
├─ Consumers: 10
├─ Throughput: 100K msg/sec (EFT only)

SWIFT Topic: payments.responses.swift.tenant-001.channel-corp-001
├─ Partitions: 20
├─ Consumers: 20
├─ Throughput: 200K msg/sec (SWIFT only)

Total Throughput: 300K msg/sec (3x improvement) ✅
```

---

### 9.2 Scalability

**Horizontal Scaling**:
- Each payment type scales independently
- Add more partitions for high-volume types (SWIFT: 20 partitions)
- Fewer partitions for low-volume types (BATCH: 2 partitions)

**Partition Recommendations**:
```
EFT:     10 partitions (10K msg/sec)
RTC:     5 partitions (5K msg/sec)
RTGS:    3 partitions (1K msg/sec)
SWIFT:   20 partitions (20K msg/sec - high-value transactions)
PayShap: 5 partitions (5K msg/sec)
CARD:    10 partitions (10K msg/sec)
BATCH:   2 partitions (100 msg/sec - large files)
OTHER:   5 partitions (1K msg/sec)
```

---

### 9.3 Monitoring

**Kafka Metrics Per Payment Type**:
```yaml
Metrics:
  - kafka_messages_in_per_sec{topic="payments.responses.eft.*"}
  - kafka_messages_out_per_sec{topic="payments.responses.eft.*"}
  - kafka_consumer_lag{topic="payments.responses.eft.*"}
  - kafka_producer_errors_total{topic="payments.responses.eft.*"}
  
Grafana Dashboard:
  - Panel 1: TPS per payment type (EFT, RTC, SWIFT, etc.)
  - Panel 2: Consumer lag per payment type
  - Panel 3: Error rate per payment type
  - Panel 4: Partition utilization per payment type
```

---

## 10. Migration Strategy

### 10.1 Existing Channels (Single Topic → Payment-Type Topics)

**Step 1: Enable payment-type topics (no disruption)**
```sql
UPDATE channel_configurations
SET kafka_use_payment_type_topics = FALSE
WHERE channel_id = 'CHANNEL-CORP-001';
```

**Step 2: Create payment-type topics (in parallel)**
```sql
INSERT INTO channel_kafka_topics (channel_id, tenant_id, payment_type, kafka_topic, created_by) VALUES
('CHANNEL-CORP-001', 'TENANT-001', 'EFT', 'payments.responses.eft.tenant-001.channel-corp-001', 'admin'),
('CHANNEL-CORP-001', 'TENANT-001', 'SWIFT', 'payments.responses.swift.tenant-001.channel-corp-001', 'admin');
```

**Step 3: Activate payment-type topics (switch over)**
```sql
UPDATE channel_configurations
SET kafka_use_payment_type_topics = TRUE
WHERE channel_id = 'CHANNEL-CORP-001';
```

**Step 4: Verify (monitor for 24 hours)**
```
- Check consumer lag (should be 0)
- Check error rate (should be <0.1%)
- Check TPS per payment type (should match baseline)
```

---

### 10.2 Rollback Plan

**If issues occur, revert to single topic**:
```sql
UPDATE channel_configurations
SET kafka_use_payment_type_topics = FALSE
WHERE channel_id = 'CHANNEL-CORP-001';
```

All messages will route back to default topic (kafka_topic).

---

## Conclusion

**Payment-type-specific Kafka topics** provide:
- ✅ Isolation (EFT doesn't block SWIFT)
- ✅ Scalability (scale consumers independently)
- ✅ Performance (3x throughput improvement)
- ✅ Monitoring (separate metrics per payment type)
- ✅ Compliance (data residency for SWIFT)
- ✅ Flexibility (different SLAs per payment type)

**Status**: ✅ **READY FOR IMPLEMENTATION**

---

**Document Version**: 1.0  
**Created**: 2025-10-12  
**Total Lines**: 1,100+  
**Related Documents**:
- `docs/39-CHANNEL-INTEGRATION-MECHANISMS.md` (Channel Integration)
- `docs/40-PHASE-7-DETAILED-DESIGN.md` (Phase 7 Design)
- `PHASE-7-SUMMARY.md` (Phase 7 Summary)
