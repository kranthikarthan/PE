# Phase 3.4: Notification Service - Kickoff Document

**Date**: October 18, 2025  
**Status**: IN PROGRESS 🚀  
**Estimated Duration**: 2-3 days  
**Complexity**: HIGH (Multi-channel, Async, Event-driven)

---

## 1. Executive Summary

The **Notification Service** is a multi-channel event-driven microservice responsible for:
- **Event Processing**: Consuming payment events from Kafka
- **Template Management**: Storing and rendering notification templates
- **Multi-Channel Delivery**: Email, SMS, Push notifications
- **Preference Enforcement**: Respecting user notification preferences
- **Competing Consumers Pattern**: Distributing notifications across service instances
- **Compliance**: Audit logging for all notifications sent

### Key Features
✅ Multi-tenancy enforcement  
✅ Template-based notifications  
✅ Async event processing  
✅ Channel prioritization  
✅ Retry logic with exponential backoff  
✅ Delivery status tracking  
✅ GDPR-compliant preference management  

---

## 2. Architecture Overview

```
┌─────────────────────────────────────────────────────────────────┐
│ Kafka Topics                                                    │
│  ├─ payment.initiated (events)                                 │
│  ├─ payment.cleared (events)                                   │
│  ├─ payment.failed (events)                                    │
│  └─ notification.events (dead letter queue)                    │
└─────────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────────┐
│ Notification Service (Competing Consumers)                      │
│  ├─ Instance 1: Consumes & processes notifications             │
│  ├─ Instance 2: Processes in parallel (load balancing)         │
│  └─ Instance N: Scale horizontally                             │
└─────────────────────────────────────────────────────────────────┘
       ↓              ↓              ↓
    Email          SMS            Push
   Channel       Channel        Channel
     │              │              │
     ├─→ AWS SES    ├─→ Twilio   ├─→ FCM
     ├─→ Retry      ├─→ Retry    ├─→ Retry
     └─→ Log        └─→ Log      └─→ Log
     
┌─────────────────────────────────────────────────────────────────┐
│ PostgreSQL                                                      │
│  ├─ notification_queue (pending notifications)                 │
│  ├─ notification_history (sent/failed tracking)                │
│  ├─ notification_templates (templates)                         │
│  ├─ notification_preferences (user preferences)                │
│  └─ notification_channels (channel configs)                    │
└─────────────────────────────────────────────────────────────────┘
```

---

## 3. Data Model

### 3.1 NotificationEntity
```java
// Represents a pending/sent notification
- id: UUID (PK)
- tenantId: String (FK, RLS)
- userId: String
- templateId: UUID (FK)
- channelType: NotificationChannel (EMAIL, SMS, PUSH)
- status: NotificationStatus (PENDING, SENT, FAILED, RETRY)
- recipientAddress: String (email/phone/token)
- templateData: JSON (Mustache/Thymeleaf variables)
- attempts: Integer (0-3)
- lastAttemptAt: LocalDateTime
- sentAt: LocalDateTime (nullable)
- failureReason: String (nullable)
- createdAt: LocalDateTime
- updatedAt: LocalDateTime

Indexes:
- (tenantId, userId, createdAt DESC)
- (tenantId, status, updatedAt)
- (status, lastAttemptAt) for retry queries
```

### 3.2 NotificationTemplateEntity
```java
// Stores reusable notification templates
- id: UUID (PK)
- tenantId: String (FK, RLS)
- name: String (UNIQUE per tenant)
- notificationType: NotificationType (PAYMENT_INITIATED, PAYMENT_FAILED, etc.)
- channels: Set<NotificationChannel> (EMAIL, SMS, PUSH)
- emailTemplate: String (HTML with Mustache placeholders)
- smsTemplate: String (140 chars max, Mustache placeholders)
- pushTemplate: String (JSON structure)
- subject: String (for email)
- isActive: Boolean
- createdAt: LocalDateTime
- updatedAt: LocalDateTime

Indexes:
- (tenantId, notificationType)
- (tenantId, isActive)
```

### 3.3 NotificationPreferenceEntity
```java
// User preferences for notification delivery
- id: UUID (PK)
- tenantId: String (FK, RLS)
- userId: String
- preferredChannels: Set<NotificationChannel>
- unsubscribedChannels: Set<NotificationChannel>
- quietHours: TimeRange (start, end)
- marketingOptIn: Boolean
- transactionAlertsOptIn: Boolean
- createdAt: LocalDateTime
- updatedAt: LocalDateTime

Unique Index: (tenantId, userId)
```

### 3.4 NotificationChannelEntity
```java
// Configuration for external channel integrations
- id: UUID (PK)
- tenantId: String (FK, RLS)
- channelType: NotificationChannel (EMAIL, SMS, PUSH)
- provider: String (SES, Twilio, FCM)
- apiKey: String (encrypted)
- apiSecret: String (encrypted, optional)
- isActive: Boolean
- retryPolicy: JSON {maxAttempts: 3, backoffMs: 1000}
- createdAt: LocalDateTime
- updatedAt: LocalDateTime

Unique Index: (tenantId, channelType, provider)
```

---

## 4. Event Processing Flow

### 4.1 Kafka Events Consumed
```json
{
  "eventType": "payment.initiated",
  "tenantId": "acme-corp",
  "transactionId": "txn-123",
  "userId": "user-456",
  "amount": 1000.00,
  "currency": "ZAR",
  "timestamp": "2025-10-18T10:30:00Z"
}
```

### 4.2 Processing Steps
1. **Receive**: Kafka listener in competing consumers group receives event
2. **Validate**: Deserialize and validate event structure
3. **Lookup Template**: Query `NotificationTemplateEntity` by tenant + type
4. **Check Preferences**: Verify user hasn't unsubscribed from channels
5. **Render**: Use Mustache/Thymeleaf to populate template with event data
6. **Queue**: Insert into `notification_queue` with PENDING status
7. **Send**: Dispatch to appropriate channel adapter
8. **Track**: Update status (SENT/FAILED) and log to history
9. **Retry**: If failed, schedule retry with exponential backoff
10. **Audit**: Log to audit service

---

## 5. Implementation Roadmap

### Phase 3.4.1: Domain Models ✅ TODO
- [x] NotificationEntity
- [x] NotificationTemplateEntity
- [x] NotificationPreferenceEntity
- [x] NotificationChannelEntity
- [x] Enums: NotificationChannel, NotificationStatus, NotificationType

### Phase 3.4.2: Database Migration 📅
- [ ] Create `V8__Create_notification_tables.sql`
- [ ] Define all 4 tables with RLS policies
- [ ] Create indexes for query optimization
- [ ] Insert default templates

### Phase 3.4.3: Service Infrastructure 🔧
- [ ] Create `notification-service` Maven module
- [ ] Configure `pom.xml` with dependencies
- [ ] Create `application.yml` with:
  - Database (PostgreSQL)
  - Kafka (competing consumers)
  - Redis (caching templates)
  - OAuth2 (Azure AD B2C)
  - External channel configs (SES, Twilio, FCM)
- [ ] Main application class with:
  - `@EnableJpaRepositories`
  - `@EnableKafka`
  - `@EnableCaching`
  - `@EnableScheduling` (for retry scheduler)

### Phase 3.4.4: Repositories 📦
- [ ] NotificationRepository (queries with retry logic)
- [ ] NotificationTemplateRepository (caching)
- [ ] NotificationPreferenceRepository (tenant-scoped)
- [ ] NotificationChannelRepository (provider configs)

### Phase 3.4.5: Event Processing 📨
- [ ] NotificationEventConsumer (Kafka listener, competing consumers)
- [ ] NotificationEventProcessor (deserialize + validate)
- [ ] NotificationRetryScheduler (scheduled task for retries)
- [ ] DeadLetterQueueHandler (failed events)

### Phase 3.4.6: Business Logic 💼
- [ ] NotificationService:
  - `queueNotification(event)` → save to DB
  - `sendNotification(notification)` → dispatch to channel
  - `retryFailedNotifications()` → scheduled task
  - `getUserPreferences(userId, tenantId)` → fetch + cache
  - `updatePreferences(userId, preferences)` → atomic update
  - `queryNotificationHistory(filters)` → paginated search

### Phase 3.4.7: Channel Adapters 🔌
- [ ] EmailAdapter (AWS SES)
- [ ] SMSAdapter (Twilio)
- [ ] PushNotificationAdapter (Firebase Cloud Messaging)
- [ ] Common interface: `NotificationChannelAdapter`

### Phase 3.4.8: REST API 🌐
- [ ] `GET /api/notifications/history` → paginated history
- [ ] `GET /api/notifications/{id}` → detail
- [ ] `GET /api/preferences` → user preferences
- [ ] `PUT /api/preferences` → update preferences
- [ ] `POST /api/templates` → create template (admin)
- [ ] `GET /api/templates/{type}` → fetch template
- [ ] `POST /api/test-notification` → send test (admin)

### Phase 3.4.9: Testing 🧪
- [ ] Unit tests: Service, Repository, Adapters
- [ ] Integration tests: Controller, Kafka listener, E2E
- [ ] 80%+ code coverage

### Phase 3.4.10: Commit & Push 📤
- [ ] Commit all changes to `feature/main-next`
- [ ] Push to remote

---

## 6. Design Patterns

### 6.1 Competing Consumers (Kafka)
- Multiple instances of `NotificationEventConsumer` in same consumer group
- Kafka distributes partitions across instances
- Automatic load balancing and failover
- Each instance processes independently

### 6.2 Template Method Pattern (Channel Adapters)
```java
interface NotificationChannelAdapter {
    CompletableFuture<SendResult> sendAsync(Notification);
}

class EmailAdapter implements NotificationChannelAdapter { ... }
class SMSAdapter implements NotificationChannelAdapter { ... }
class PushAdapter implements NotificationChannelAdapter { ... }
```

### 6.3 Retry with Exponential Backoff
- Max 3 attempts per notification
- Backoff: 1s, 2s, 4s (exponential)
- Scheduled task runs every 30 seconds
- Failed notifications moved to DLQ after 3 attempts

### 6.4 Multi-Tenancy (RLS)
- All queries filtered by `tenantId`
- Database RLS policy enforces isolation
- X-Tenant-ID header in REST API

---

## 7. External Dependencies

| Service | Purpose | Status |
|---------|---------|--------|
| AWS SES | Email delivery | To configure |
| Twilio | SMS delivery | To configure |
| Firebase Cloud Messaging | Push notifications | To configure |
| PostgreSQL | Notification storage | ✅ Ready |
| Kafka | Event streaming | ✅ Ready |
| Redis | Template caching | ✅ Ready |
| Azure AD B2C | Authentication | ✅ Ready |

---

## 8. Success Criteria

✅ All domain models created and compiled  
✅ Database migration V8 applied successfully  
✅ Kafka listener receives and processes events  
✅ Notifications queued and tracked in DB  
✅ At least 1 channel adapter functional (Email)  
✅ REST API endpoints operational  
✅ 80%+ code coverage in tests  
✅ All tests passing  
✅ Service starts and runs on port 8084  
✅ Integrated with existing Phase 3.1-3.3 services  

---

## 9. Timeline Estimate

| Phase | Duration | Status |
|-------|----------|--------|
| Domain Models | 30 min | 📅 TODO |
| DB Migration | 20 min | 📅 TODO |
| Service Setup | 20 min | 📅 TODO |
| Repositories | 20 min | 📅 TODO |
| Event Listener | 45 min | 📅 TODO |
| Business Logic | 1 hour | 📅 TODO |
| Channel Adapters | 1.5 hours | 📅 TODO |
| REST API | 45 min | 📅 TODO |
| Testing | 2 hours | 📅 TODO |
| **Total** | **~7 hours** | 📅 TODO |

---

## 10. Risks & Mitigations

| Risk | Impact | Mitigation |
|------|--------|-----------|
| External API failures (SES, Twilio) | Notifications not sent | Retry logic + fallback channels |
| High event volume | Queue backlog | Competing consumers + partitioning |
| Template rendering errors | Malformed messages | Validation + templates |
| User preference race conditions | Stale data | Optimistic locking |
| Kafka consumer lag | Delayed notifications | Monitor & scale instances |

---

## 11. Next Steps

1. ✅ **Kickoff** (This document)
2. 📝 Create domain models in `domain-models/notification`
3. 🗄️ Create V8 database migration
4. 🔧 Set up `notification-service` Maven module
5. 📦 Implement repositories with caching
6. 📨 Build Kafka event consumer
7. 💼 Implement NotificationService logic
8. 🔌 Create channel adapters (Email first)
9. 🌐 Build REST API controller
10. 🧪 Write comprehensive tests
11. ✅ Commit & push to `feature/main-next`

---

**Created by**: Payment Engine Agent  
**Version**: 1.0 (Oct 18, 2025)  
**Next Phase**: Phase 3.5 - Reporting Service
