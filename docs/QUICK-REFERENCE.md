# Quick Reference Guide

## 📑 Document Index

| Document | When to Use |
|----------|-------------|
| [00-ARCHITECTURE-OVERVIEW](00-ARCHITECTURE-OVERVIEW.md) | Understanding overall system design |
| [01-ASSUMPTIONS](01-ASSUMPTIONS.md) | **Start here! Review all assumptions** |
| [02-MICROSERVICES-BREAKDOWN](02-MICROSERVICES-BREAKDOWN.md) | Building individual services |
| [03-EVENT-SCHEMAS](03-EVENT-SCHEMAS.md) | Implementing event consumers/publishers |
| [04-AI-AGENT-TASK-BREAKDOWN](04-AI-AGENT-TASK-BREAKDOWN.md) | **AI agents: Your task assignments** |
| [05-DATABASE-SCHEMAS](05-DATABASE-SCHEMAS.md) | Creating database migrations |
| [06-SOUTH-AFRICA-CLEARING](06-SOUTH-AFRICA-CLEARING.md) | Integrating with clearing systems |
| [07-AZURE-INFRASTRUCTURE](07-AZURE-INFRASTRUCTURE.md) | Deploying to Azure |
| [08-CORE-BANKING-INTEGRATION](08-CORE-BANKING-INTEGRATION.md) | **Integrating with external account systems** |
| [09-LIMIT-MANAGEMENT](09-LIMIT-MANAGEMENT.md) | **Implementing customer limits** |
| [10-FRAUD-SCORING-INTEGRATION](10-FRAUD-SCORING-INTEGRATION.md) | **Integrating fraud scoring API** |
| [11-KAFKA-SAGA-IMPLEMENTATION](11-KAFKA-SAGA-IMPLEMENTATION.md) | **Using Kafka for Saga pattern** |

---

## 🎯 Service Quick Reference

| Service | Port | Database | Key Endpoints | Events Published | Events Consumed |
|---------|------|----------|---------------|------------------|-----------------|
| Payment Initiation | 8085 | PostgreSQL | POST /api/v1/payments | PaymentInitiatedEvent | - |
| Validation | 8086 | PostgreSQL + Redis | POST /api/v1/validate/payment, GET /api/v1/limits/customer/{id} | PaymentValidatedEvent, LimitConsumedEvent | PaymentInitiatedEvent |
| Account Adapter | 8081 | PostgreSQL + Redis | GET /api/v1/accounts/{id}, POST /debit, POST /credit | FundsReservedEvent | - |
| Routing | 8083 | Redis | POST /api/v1/routing/determine | RoutingDeterminedEvent | PaymentValidatedEvent |
| Transaction Processing | 8087 | PostgreSQL | POST /api/v1/transactions | TransactionCreatedEvent | RoutingDeterminedEvent |
| SAMOS Adapter | 8088 | PostgreSQL | POST /api/v1/clearing/submit | ClearingSubmittedEvent | TransactionCreatedEvent |
| Bankserv Adapter | 8089 | PostgreSQL | POST /api/v1/clearing/submit | ClearingSubmittedEvent | TransactionCreatedEvent |
| RTC Adapter | 8090 | PostgreSQL | POST /api/v1/clearing/submit | ClearingSubmittedEvent | TransactionCreatedEvent |
| Settlement | 8091 | PostgreSQL | POST /api/v1/settlement/batches | SettlementCompleteEvent | ClearingCompletedEvent |
| Reconciliation | 8092 | PostgreSQL | POST /api/v1/reconciliation/run | - | - |
| Notification | 8082 | PostgreSQL | POST /api/v1/notifications/send | - | PaymentCompletedEvent |
| Reporting | 8084 | PostgreSQL + Synapse | POST /api/v1/reports/generate | - | - |
| Saga Orchestrator | 8093 | PostgreSQL | POST /api/v1/sagas/start | SagaStartedEvent | All Events |
| API Gateway | 8080 | Redis | All routes | - | - |
| IAM | 8094 | PostgreSQL | POST /api/v1/auth/login | - | - |
| Audit | 8095 | CosmosDB | - | - | All Events |

---

## 🗄️ Database Quick Reference

### PostgreSQL Databases

| Database | Service | Key Tables | Size (Year 1) |
|----------|---------|------------|---------------|
| payment_initiation_db | Payment Initiation | payments, payment_status_history | 500 GB |
| validation_db | Validation | validation_rules, validation_results | 100 GB |
| account_db | Account | accounts, account_holds | 100 GB |
| transaction_db | Transaction Processing | transactions, transaction_events, ledger_entries | 1 TB |
| clearing_db | Clearing Adapters | clearing_submissions, clearing_batches | 800 GB |
| settlement_db | Settlement | settlement_batches, settlement_transactions | 300 GB |
| reconciliation_db | Reconciliation | reconciliation_runs, reconciliation_exceptions | 200 GB |
| saga_db | Saga Orchestrator | sagas, saga_steps | 150 GB |

### Redis Cache

| Key Pattern | TTL | Purpose | Service |
|-------------|-----|---------|---------|
| `account:balance:{accountNumber}` | 30s | Account balance | Account Service |
| `validation:rules` | 5m | Validation rules | Validation Service |
| `routing:tables` | 1h | Routing rules | Routing Service |
| `session:{userId}` | JWT expiry | User sessions | API Gateway |

---

## 🔄 Event Flow Quick Reference

### Payment Flow (Happy Path)

```
PaymentInitiatedEvent
  → PaymentValidatedEvent
    → FundsReservedEvent
      → RoutingDeterminedEvent
        → TransactionCreatedEvent
          → ClearingSubmittedEvent
            → ClearingCompletedEvent
              → SettlementCompleteEvent
                → PaymentCompletedEvent
```

### Payment Flow (Failure at Validation)

```
PaymentInitiatedEvent
  → ValidationFailedEvent
    → PaymentFailedEvent
```

### Payment Flow (Compensation)

```
[... Normal flow until ClearingFailedEvent]
  → SagaCompensatingEvent
    → [Reverse steps: Release funds, cancel transaction]
      → PaymentFailedEvent
```

---

## 🌐 Azure Services Quick Reference

### Core Infrastructure

| Service | Configuration | Monthly Cost | Purpose |
|---------|---------------|--------------|---------|
| AKS | 8 nodes (D8s_v3) | $4,800 | Container orchestration |
| PostgreSQL | GP D4s_v3 + Replica | $1,200 | Transactional data |
| Cosmos DB | 10,000 RU/s | $600 | Audit logs |
| Redis Premium | P3, 26 GB, 3 shards | $1,400 | Caching |
| Service Bus Premium | 1 MU | $650 | Messaging |
| Application Gateway | WAF v2 | $450 | Load balancing |
| Azure Monitor | 100 GB/day | $2,300 | Logging, metrics |
| API Management | Premium | $3,000 | API gateway |

**Total: ~$15,050/month**

### Network Configuration

| Resource | CIDR/Range | Purpose |
|----------|------------|---------|
| VNet | 10.0.0.0/16 | Main virtual network |
| AKS Subnet | 10.0.1.0/24 | Kubernetes pods |
| Database Subnet | 10.0.2.0/24 | PostgreSQL, Redis |
| Integration Subnet | 10.0.3.0/24 | Service Bus, Key Vault |
| Service CIDR | 10.1.0.0/16 | Kubernetes services |

---

## 🔐 Security Quick Reference

### Authentication Flow

```
1. User → POST /api/v1/auth/login (IAM Service)
2. IAM → Validate credentials (Azure AD B2C)
3. IAM → Generate JWT (15 min expiry)
4. User → API requests with Bearer token
5. API Gateway → Validate JWT
6. API Gateway → Forward to service with user context
```

### JWT Token Structure

```json
{
  "sub": "user-123",
  "email": "user@example.com",
  "roles": ["USER", "ADMIN"],
  "exp": 1697012345,
  "iat": 1697011445
}
```

### Key Vault Secrets

| Secret Name | Purpose | Rotation |
|-------------|---------|----------|
| postgresql-admin-password | Database admin | 90 days |
| servicebus-connection-string | Event bus | No rotation |
| redis-connection-string | Cache | No rotation |
| jwt-signing-key | Token signing | 180 days |
| smtp-api-key | Email notifications | 90 days |
| twilio-api-key | SMS notifications | 90 days |

---

## 📊 Monitoring Quick Reference

### Key Metrics

| Metric | Target | Alert Threshold | Severity |
|--------|--------|-----------------|----------|
| API Response Time (p95) | < 200ms | > 500ms | High |
| Error Rate | < 1% | > 5% | Critical |
| Throughput | 10,000 TPS | < 5,000 TPS | Medium |
| Service Availability | 99.95% | < 99% | Critical |
| Database CPU | < 70% | > 85% | High |
| Memory Usage | < 80% | > 90% | High |
| Queue Depth | < 1000 | > 5000 | Medium |

### Health Check Endpoints

| Service | Endpoint | Expected Response |
|---------|----------|-------------------|
| All Services | GET /actuator/health | `{"status": "UP"}` |
| All Services | GET /actuator/info | Service metadata |
| API Gateway | GET /health | 200 OK |
| PostgreSQL | Connection check | < 100ms |
| Redis | PING command | PONG |

### Log Queries (KQL)

```kusto
// Find errors in last hour
AppExceptions
| where TimeGenerated > ago(1h)
| summarize Count = count() by Type
| order by Count desc

// Payment success rate
AppRequests
| where Name contains "POST /api/v1/payments"
| where TimeGenerated > ago(1h)
| summarize SuccessRate = 100.0 * countif(Success) / count()

// Slow queries
AppDependencies
| where Type == "SQL"
| where DurationMs > 1000
| project TimeGenerated, Name, DurationMs, Data
| order by DurationMs desc
```

---

## 🚨 Incident Response Quick Reference

### Severity Levels

| Level | Description | Response Time | Escalation |
|-------|-------------|---------------|------------|
| P1 - Critical | System down, no payments processing | 15 minutes | Immediate |
| P2 - High | Partial outage, degraded performance | 1 hour | 2 hours |
| P3 - Medium | Single service issue, workaround available | 4 hours | 8 hours |
| P4 - Low | Minor issue, no impact to users | 24 hours | - |

### Common Issues & Solutions

**Clearing System Unavailable**
- ✅ Queue payments locally
- ✅ Switch to alternate clearing system
- ✅ Notify users of delay
- ✅ Monitor clearing system status

**Database Performance Degradation**
- ✅ Check slow query log
- ✅ Verify index usage
- ✅ Scale up database tier temporarily
- ✅ Enable read replica routing

**High Memory Usage in AKS**
- ✅ Check for memory leaks
- ✅ Scale up pod replicas
- ✅ Increase pod memory limits
- ✅ Restart unhealthy pods

**Service Bus Queue Depth Growing**
- ✅ Scale out consumer pods
- ✅ Check for processing errors
- ✅ Verify dead letter queue
- ✅ Increase consumer concurrency

---

## 🔧 Development Quick Reference

### Local Development Setup

```bash
# Start infrastructure
docker-compose up -d

# Build all services
mvn clean install

# Run single service
cd services/payment-initiation
mvn spring-boot:run -Dspring.profiles.active=local

# Run tests
mvn test

# Run integration tests
mvn verify -P integration-tests
```

### Environment Variables

| Variable | Description | Example |
|----------|-------------|---------|
| SPRING_PROFILES_ACTIVE | Active profile | production |
| DATABASE_URL | PostgreSQL URL | jdbc:postgresql://... |
| REDIS_HOST | Redis hostname | payments-redis.redis.cache... |
| SERVICEBUS_CONNECTION_STRING | Service Bus | Endpoint=sb://... |
| KEY_VAULT_URL | Key Vault URL | https://payments-kv.vault... |
| APPLICATION_INSIGHTS_KEY | App Insights | abc123... |

### Common Maven Commands

```bash
# Build without tests
mvn clean package -DskipTests

# Run specific test class
mvn test -Dtest=PaymentServiceTest

# Generate code coverage report
mvn jacoco:report

# Run SonarQube analysis
mvn sonar:sonar

# Build Docker image
mvn spring-boot:build-image
```

### Common kubectl Commands

```bash
# Get pod status
kubectl get pods -n payments

# View pod logs
kubectl logs -f <pod-name> -n payments

# Describe pod
kubectl describe pod <pod-name> -n payments

# Port forward to service
kubectl port-forward svc/payment-initiation 8085:8080 -n payments

# Scale deployment
kubectl scale deployment payment-initiation --replicas=5 -n payments

# Apply configuration
kubectl apply -f k8s/payment-initiation.yaml

# Restart deployment
kubectl rollout restart deployment payment-initiation -n payments
```

---

## 📋 Testing Quick Reference

### Test Coverage Requirements

| Test Type | Minimum Coverage | Purpose |
|-----------|------------------|---------|
| Unit Tests | 80% | Business logic |
| Integration Tests | 60% | API endpoints, DB |
| Contract Tests | 100% | Event schemas |
| E2E Tests | Critical paths | End-to-end flows |

### Test Data

**Test Accounts**
```
Source Account: 1234567890
Destination Account: 0987654321
Balance: R10,000.00
Status: ACTIVE
```

**Test Payment**
```json
{
  "idempotencyKey": "test-uuid-123",
  "sourceAccount": "1234567890",
  "destinationAccount": "0987654321",
  "amount": 1000.00,
  "currency": "ZAR",
  "reference": "Test payment",
  "paymentType": "RTC"
}
```

### Performance Test Targets

| Scenario | Target TPS | Duration | Success Rate |
|----------|------------|----------|--------------|
| Load Test | 10,000 | 1 hour | > 99% |
| Stress Test | 20,000 | 30 min | > 95% |
| Soak Test | 5,000 | 24 hours | > 99% |
| Spike Test | 0→20,000→0 | 15 min | > 95% |

---

## 🌍 South Africa Clearing Quick Reference

### SAMOS (RTGS)

| Attribute | Value |
|-----------|-------|
| **Threshold** | > R5 million (recommended) |
| **Operating Hours** | 08:00-15:30 CAT |
| **Settlement** | Real-time |
| **Message Format** | ISO 20022 (pacs.008) |
| **Protocol** | SWIFT |
| **Fee** | ~R30-50 per transaction |

### BankservAfrica RTC

| Attribute | Value |
|-----------|-------|
| **Threshold** | < R5 million |
| **Operating Hours** | 24/7/365 |
| **Settlement** | Real-time (T+0) |
| **Message Format** | ISO 20022 |
| **Protocol** | REST API |
| **Response Time** | < 10 seconds |
| **Fee** | ~R3-5 per transaction |

### BankservAfrica ACH/EFT

| Attribute | Value |
|-----------|-------|
| **Operating Hours** | 24/7 |
| **Batch Cutoffs** | 08:00, 10:00, 12:00, 14:00 CAT |
| **Settlement** | T+1 |
| **Message Format** | Proprietary |
| **Protocol** | SFTP |
| **Fee** | ~R1-3 per transaction |

### Response Codes

| Code | Description | Action |
|------|-------------|--------|
| ACSC | Accepted Settlement Completed | Success |
| AC01 | Incorrect account number | Retry with correct details |
| AC04 | Closed account | Contact beneficiary |
| AM05 | Duplication | Check for duplicate |
| RR01 | Missing debtor account | Provide complete info |

---

## 🎓 Learning Resources

### Architecture Patterns
- **Microservices**: https://microservices.io/patterns/
- **Saga Pattern**: https://microservices.io/patterns/data/saga.html
- **CQRS**: https://martinfowler.com/bliki/CQRS.html
- **Event Sourcing**: https://martinfowler.com/eaaDev/EventSourcing.html

### South African Payments
- **PASA**: https://www.pasa.org.za/
- **SARB**: https://www.resbank.co.za/
- **BankservAfrica**: https://www.bankservafrica.com/

### Azure Documentation
- **AKS**: https://docs.microsoft.com/azure/aks/
- **Service Bus**: https://docs.microsoft.com/azure/service-bus-messaging/
- **PostgreSQL**: https://docs.microsoft.com/azure/postgresql/

---

## 📞 Contact Information

| Team | Email | Slack Channel |
|------|-------|---------------|
| Architecture | architecture@example.com | #architecture |
| DevOps | devops@example.com | #devops |
| Security | security@example.com | #security |
| Payments | payments@example.com | #payments |
| Support | support@example.com | #support |

---

## 🔖 Bookmarks

- [Swagger UI](http://localhost:8080/swagger-ui.html) (Local)
- [Azure Portal](https://portal.azure.com)
- [Azure DevOps](https://dev.azure.com)
- [Grafana Dashboard](https://grafana.example.com)
- [Kibana Logs](https://kibana.example.com)
- [SonarQube](https://sonarqube.example.com)

---

**Last Updated**: 2025-10-11  
**Version**: 1.0
