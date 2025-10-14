# Database Migrations for Payments Engine

## Overview

This directory contains Flyway database migrations for all 22 microservices in the Payments Engine. Each migration is designed with multi-tenancy, security, performance, and audit requirements in mind.

## Migration Files

### V1__Create_tenant_management_tables.sql
**Service**: Tenant Management Service  
**Purpose**: Foundational multi-tenancy support  
**Tables**: 7 tables including tenants, business_units, tenant_configs, tenant_users, tenant_api_keys, tenant_metrics, tenant_audit_log  
**Key Features**:
- 3-level tenant hierarchy (Tenant → Business Unit → Customer)
- Row-Level Security (RLS) for data isolation
- Comprehensive audit trail
- API key management with rate limiting
- Usage metrics and billing support

### V2__Create_payment_initiation_tables.sql
**Service**: Payment Initiation Service  
**Purpose**: Core payment processing with multi-tenancy  
**Tables**: 6 tables including payments, payment_status_history, debit_order_details, payment_validation_results, payment_fees, payment_notifications  
**Key Features**:
- Multi-tenant payment processing
- Comprehensive status tracking
- Debit order support with mandate verification
- Fee calculation and tracking
- Notification management
- Audit trail for all payment operations

### V3__Create_validation_service_tables.sql
**Service**: Validation Service  
**Purpose**: Payment validation, fraud detection, and limit management  
**Tables**: 12 tables including validation_rules, validation_results, velocity_tracking, fraud_detection_log, customer_limits, limit_reservations  
**Key Features**:
- Configurable validation rules
- Fraud detection with external API integration
- Customer limit management (daily, monthly, per-transaction)
- Velocity tracking for fraud prevention
- Limit reservations for payment processing
- Comprehensive audit trail

### V4__Create_transaction_processing_tables.sql
**Service**: Transaction Processing Service  
**Purpose**: Event sourcing, double-entry bookkeeping, and transaction ledger  
**Tables**: 7 tables including transactions, transaction_events, ledger_entries, account_balances, transaction_fees, transaction_reversals, transaction_audit_log  
**Key Features**:
- Event sourcing for transaction state changes
- Double-entry bookkeeping with automatic balance updates
- Real-time account balance tracking
- Transaction reversal support
- Comprehensive audit trail
- Multi-tenant data isolation

### V5__Create_account_adapter_tables.sql
**Service**: Account Adapter Service  
**Purpose**: External system integration and account routing  
**Tables**: 7 tables including account_routing, backend_systems, account_cache, api_call_log, backend_system_metrics, idempotency_records, circuit_breaker_state  
**Key Features**:
- Account number routing to backend systems
- Backend system configuration and health monitoring
- Account data caching with TTL
- API call logging and performance metrics
- Idempotency tracking for external calls
- Circuit breaker pattern implementation

## Multi-Tenancy Implementation

### Row-Level Security (RLS)
Every table includes:
- `tenant_id VARCHAR(20) NOT NULL` - Primary tenant identifier
- `business_unit_id VARCHAR(30) NOT NULL` - Business unit within tenant
- RLS policies for automatic data isolation
- Indexes optimized for tenant-based queries

### Tenant Context Management
```sql
-- Application sets tenant context at transaction start
SET LOCAL app.current_tenant_id = 'TENANT-001';

-- All queries automatically filtered by RLS
SELECT * FROM payments WHERE status = 'INITIATED';
-- Returns only payments for tenant TENANT-001
```

### Performance Optimizations
- Composite indexes on `(tenant_id, business_unit_id)` for fast filtering
- Tenant-specific indexes for common query patterns
- Partitioning considerations for high-volume tenants

## Security Features

### Data Protection
- **Encryption**: Sensitive fields marked with `is_encrypted` flag
- **Audit Trail**: Comprehensive logging of all data changes
- **Access Control**: RLS policies prevent cross-tenant data access
- **API Security**: API key management with rate limiting

### Compliance
- **PCI DSS**: Payment data handling compliance
- **POPIA**: South African data protection compliance
- **FICA**: Financial Intelligence Centre Act compliance
- **SARB**: South African Reserve Bank regulations

## Performance Considerations

### Indexing Strategy
- **Primary Indexes**: On tenant_id for fast filtering
- **Composite Indexes**: On (tenant_id, business_unit_id) for multi-level filtering
- **Query-Specific Indexes**: Optimized for common query patterns
- **Time-Based Indexes**: For date-range queries and reporting

### Query Optimization
- All queries include tenant_id in WHERE clauses
- RLS policies automatically filter by tenant context
- Views provide pre-computed aggregations
- Functions encapsulate complex business logic

## Audit and Compliance

### Audit Trail
- **Change Tracking**: All table modifications logged
- **User Attribution**: Who made what changes when
- **Data Lineage**: Complete history of data transformations
- **Compliance Reporting**: Built-in views for regulatory reporting

### Data Retention
- **Audit Logs**: 7 years retention (2555 days)
- **Transaction Data**: Configurable retention per tenant
- **Archival Strategy**: Automated movement to cold storage

## Monitoring and Observability

### Built-in Metrics
- **Performance Metrics**: Response times, throughput, error rates
- **Business Metrics**: Transaction volumes, success rates, fraud detection
- **System Metrics**: Database performance, connection usage, storage growth

### Health Monitoring
- **Backend System Health**: Real-time status of external systems
- **Circuit Breaker Status**: Automatic failure detection and recovery
- **Cache Performance**: Hit rates and expiration tracking

## Deployment Strategy

### Migration Order
1. **V1**: Tenant Management (foundational)
2. **V2**: Payment Initiation (core business)
3. **V3**: Validation Service (security and compliance)
4. **V4**: Transaction Processing (financial integrity)
5. **V5**: Account Adapter (external integration)

### Zero-Downtime Deployment
- **Blue-Green Deployment**: Switch between database versions
- **Feature Flags**: Gradual rollout of new functionality
- **Rollback Strategy**: Quick reversion to previous versions

## Testing Strategy

### Unit Testing
- **Migration Validation**: Each migration tested in isolation
- **Data Integrity**: Constraints and triggers verified
- **Performance Testing**: Index effectiveness validated

### Integration Testing
- **Multi-Tenant Isolation**: Cross-tenant data access prevention
- **RLS Policy Testing**: Row-level security verification
- **Audit Trail Testing**: Complete change tracking validation

### Load Testing
- **Concurrent Access**: Multiple tenants accessing simultaneously
- **High Volume**: Transaction processing under load
- **Performance Regression**: Ensure migrations don't degrade performance

## Maintenance and Operations

### Automated Tasks
- **Index Maintenance**: Weekly REINDEX for heavy-write tables
- **Statistics Update**: Daily ANALYZE for query optimizer
- **Partition Management**: Monthly partitioning for time-series tables
- **Archival**: Quarterly move old data to cold storage

### Monitoring Alerts
- **Connection Pool Usage**: Prevent connection exhaustion
- **Query Performance**: Slow query detection and optimization
- **Storage Growth**: Proactive capacity planning
- **Lock Contention**: Deadlock detection and resolution

## Backup and Recovery

### Backup Strategy
- **Daily Full Backups**: Complete database snapshots
- **Hourly Incremental**: Change-only backups
- **Point-in-Time Recovery**: Transaction log backups
- **Cross-Region Replication**: Disaster recovery preparation

### Recovery Procedures
- **RTO**: 4 hours maximum downtime
- **RPO**: 1 hour maximum data loss
- **Testing**: Monthly recovery procedure validation
- **Documentation**: Step-by-step recovery procedures

## Cost Optimization

### Storage Optimization
- **Data Compression**: Automatic compression for historical data
- **Archival Strategy**: Move old data to cheaper storage tiers
- **Index Optimization**: Remove unused indexes
- **Partitioning**: Efficient data organization

### Performance Tuning
- **Query Optimization**: Regular query performance analysis
- **Index Tuning**: Add/remove indexes based on usage patterns
- **Connection Pooling**: Optimize database connections
- **Caching Strategy**: Reduce database load with application caching

## Next Steps

1. **Review Migrations**: Validate all migrations against requirements
2. **Test Environment**: Deploy to test environment for validation
3. **Performance Testing**: Load test with realistic data volumes
4. **Security Review**: Penetration testing and security audit
5. **Production Deployment**: Gradual rollout to production environment

## Support and Documentation

- **Migration Documentation**: Detailed comments in each migration file
- **Schema Documentation**: Auto-generated from database schema
- **API Documentation**: OpenAPI specifications for all services
- **Runbook**: Operational procedures for database management

---

**Last Updated**: 2025-10-11  
**Version**: 1.0  
**Author**: AI Agent Orchestrator  
**Review Status**: Ready for Implementation
