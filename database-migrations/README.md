# Database Architecture for Microservices

This document describes the independent database schemas for each microservice in the payments engine.

## Overview

Each microservice has its own independent database schema with no cross-service dependencies. This ensures:

- **Service Independence**: Each service can evolve its database schema independently
- **Data Isolation**: Services cannot directly access each other's data
- **Scalability**: Each service can scale its database independently
- **Fault Tolerance**: Database issues in one service don't affect others

## Database Structure

### 1. Payment Initiation Service (`payment_initiation` database)

**Purpose**: Manages payment initiation, validation, and ISO 20022 message processing.

**Key Tables**:
- `payments` - Main payment records
- `payment_status_history` - Audit trail of status changes
- `payment_validation_results` - Validation results
- `payment_fees` - Fee calculations
- `payment_notifications` - Notification tracking
- `pain001_messages` - ISO 20022 pain.001 messages
- `pain002_messages` - ISO 20022 pain.002 messages
- `pain001_audit_log` - Audit log for pain.001
- `pain002_audit_log` - Audit log for pain.002

**Key Features**:
- Multi-tenancy with Row Level Security (RLS)
- Comprehensive audit trails
- ISO 20022 message support
- Payment validation and fee calculation

### 2. Routing Service (`routing` database)

**Purpose**: Determines which clearing system to use for payments.

**Key Tables**:
- `routing_rules` - Routing decision rules
- `clearing_systems` - Clearing system configurations
- `routing_decisions` - Audit trail of routing decisions
- `clearing_system_metrics` - Performance metrics
- `routing_rule_evaluations` - Detailed rule evaluation log

**Key Features**:
- Rule-based routing engine
- Clearing system health monitoring
- Performance metrics and analytics
- Circuit breaker support

### 3. Transaction Processing Service (`transaction_processing` database)

**Purpose**: Manages transaction ledger, double-entry bookkeeping, and account balances.

**Key Tables**:
- `transactions` - Main transaction ledger
- `transaction_events` - Event sourcing log
- `ledger_entries` - Double-entry bookkeeping entries
- `account_balances` - Current account balances
- `transaction_fees` - Transaction fees
- `transaction_reversals` - Reversal tracking
- `transaction_audit_log` - Audit trail

**Key Features**:
- Event sourcing for transaction state changes
- Double-entry bookkeeping validation
- Real-time account balance updates
- Transaction reversal support

### 4. Account Adapter Service (`account_adapter` database)

**Purpose**: Routes account requests to appropriate backend systems.

**Key Tables**:
- `account_routing` - Account to backend system mapping
- `backend_systems` - Backend system configurations
- `account_cache` - Temporary account data cache
- `api_call_log` - Audit trail of API calls
- `backend_system_metrics` - Performance metrics
- `idempotency_records` - Idempotency tracking
- `circuit_breaker_state` - Circuit breaker state

**Key Features**:
- Account routing to backend systems
- Circuit breaker pattern implementation
- API call auditing and metrics
- Idempotency support

### 5. Saga Orchestrator Service (`saga_orchestrator` database)

**Purpose**: Manages distributed transaction orchestration using the Saga pattern.

**Key Tables**:
- `saga_instances` - Main saga orchestration instances
- `saga_steps` - Individual steps within sagas
- `saga_events` - Event sourcing log
- `saga_compensation_log` - Compensation actions
- `saga_timeouts` - Timeout tracking
- `saga_audit_log` - Audit trail

**Key Features**:
- Saga pattern implementation
- Compensation handling
- Timeout management
- Event sourcing for saga state

## Multi-Tenancy

All databases implement multi-tenancy using:

1. **Tenant ID**: Every table includes `tenant_id` and `business_unit_id` columns
2. **Row Level Security (RLS)**: PostgreSQL RLS policies ensure data isolation
3. **Application Context**: Services set tenant context using `SET LOCAL app.current_tenant_id = 'TENANT-ID'`

## Data Consistency

Since services have independent databases, data consistency is maintained through:

1. **Event-Driven Communication**: Services communicate via events (Kafka)
2. **Saga Pattern**: Distributed transactions are managed by the Saga Orchestrator
3. **Eventual Consistency**: Services eventually reach consistent state through event processing

## Migration Strategy

Each service has its own migration files in the `database-migrations/{service-name}/` directory:

- `database-migrations/payment-initiation/V1__Create_payment_initiation_tables.sql`
- `database-migrations/routing/V1__Create_routing_service_tables.sql`
- `database-migrations/transaction-processing/V1__Create_transaction_processing_tables.sql`
- `database-migrations/account-adapter/V1__Create_account_adapter_tables.sql`
- `database-migrations/saga-orchestrator/V1__Create_saga_orchestrator_tables.sql`

## Deployment

Each service connects to its own database:

```yaml
# Payment Initiation Service
SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/payment_initiation

# Routing Service
SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/routing

# Transaction Processing Service
SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/transaction_processing

# Account Adapter Service
SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/account_adapter

# Saga Orchestrator Service
SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/saga_orchestrator
```

## Benefits

1. **Service Independence**: Each service can evolve independently
2. **Data Isolation**: Services cannot accidentally access each other's data
3. **Scalability**: Each service can scale its database independently
4. **Fault Tolerance**: Database issues in one service don't affect others
5. **Technology Diversity**: Each service can use different database technologies if needed
6. **Team Autonomy**: Different teams can manage different services independently

## Considerations

1. **Data Duplication**: Some data may be duplicated across services (e.g., payment IDs)
2. **Eventual Consistency**: Services may have temporary inconsistencies
3. **Complex Queries**: Cross-service queries require event-driven approaches
4. **Transaction Management**: Distributed transactions require careful orchestration

This architecture follows microservices best practices and ensures each service maintains its own data sovereignty while enabling loose coupling and high scalability.