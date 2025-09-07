# Fraud API Toggle Configuration System

## Overview

This document describes the implementation of a dynamic fraud API toggle configuration system that allows enabling or disabling fraud/risk API calls at different levels (tenant, payment type, local instrumentation code, clearing system) without requiring application restarts. The system provides real-time configuration management with caching for optimal performance.

## Key Features

### 1. **Multi-Level Configuration**
- **Tenant Level**: Enable/disable fraud API for entire tenant
- **Payment Type Level**: Enable/disable for specific payment types (e.g., SEPA_CREDIT_TRANSFER, DOMESTIC_TRANSFER)
- **Local Instrumentation Code Level**: Enable/disable for specific local instruments (e.g., SEPA_CT, SEPA_CT_INSTANT)
- **Clearing System Level**: Enable/disable for specific clearing systems (e.g., TARGET2, EBA_CLEARING)

### 2. **Dynamic Configuration Management**
- **Real-time Updates**: Configuration changes take effect immediately without application restart
- **Priority-based Resolution**: More specific configurations override general ones
- **Scheduled Configuration**: Set future effective times and expiration dates
- **Active/Inactive Status**: Enable or disable configurations without deletion

### 3. **Advanced Features**
- **Caching**: Redis-based caching for optimal performance
- **Audit Trail**: Complete audit logging of configuration changes
- **Reason Tracking**: Required reasons for enabling/disabling fraud API
- **Statistics and Monitoring**: Comprehensive statistics and monitoring capabilities

## Architecture Components

### 1. **Entities**

#### FraudApiToggleConfiguration
- Stores configuration for fraud API enable/disable at different levels
- Supports tenant, payment type, local instrument, and clearing system specificity
- Includes timing controls (effective from/until) and priority ordering
- Tracks audit information (created by, updated by, timestamps)

### 2. **Services**

#### FraudApiToggleService
- Main service for fraud API toggle operations
- Configuration management (CRUD operations)
- Status checking with priority-based resolution
- Statistics and monitoring
- Cache management

#### FraudApiToggleServiceImpl
- Implementation with Redis caching
- Priority-based configuration resolution
- Dynamic configuration updates
- Error handling and fallback mechanisms

### 3. **Repositories**

#### FraudApiToggleConfigurationRepository
- JPA repository with custom queries for priority-based resolution
- Efficient queries for different configuration levels
- Time-based filtering for effective configurations
- Performance-optimized indexes

### 4. **Controllers**

#### FraudApiToggleController
- REST API endpoints for configuration management
- Status checking endpoints
- Statistics and monitoring endpoints
- Cache management endpoints

## Database Schema

### Tables

#### fraud_api_toggle_configurations
```sql
- id (UUID, Primary Key)
- tenant_id (VARCHAR(50), NOT NULL)
- payment_type (VARCHAR(50), Optional)
- local_instrumentation_code (VARCHAR(50), Optional)
- clearing_system_code (VARCHAR(50), Optional)
- is_enabled (BOOLEAN, NOT NULL)
- enabled_reason (VARCHAR(500), Optional)
- disabled_reason (VARCHAR(500), Optional)
- effective_from (TIMESTAMP WITH TIME ZONE, Optional)
- effective_until (TIMESTAMP WITH TIME ZONE, Optional)
- priority (INTEGER, NOT NULL, DEFAULT 100)
- is_active (BOOLEAN, NOT NULL, DEFAULT TRUE)
- created_by (VARCHAR(100), Optional)
- updated_by (VARCHAR(100), Optional)
- created_at (TIMESTAMP WITH TIME ZONE, NOT NULL)
- updated_at (TIMESTAMP WITH TIME ZONE, NOT NULL)
```

### Indexes
- Comprehensive indexing for performance optimization
- Composite indexes for common query patterns
- Partial indexes for active configurations and time-based queries

### Views
- `fraud_api_toggle_configurations_active` - All active configurations
- `fraud_api_toggle_configurations_currently_effective` - Currently effective configurations
- `fraud_api_toggle_configurations_future_effective` - Future effective configurations
- `fraud_api_toggle_configurations_expired` - Expired configurations

## API Endpoints

### Configuration Management
- `GET /api/v1/fraud-api-toggle/configurations` - List all active configurations
- `GET /api/v1/fraud-api-toggle/configurations/tenant/{tenantId}` - Get configurations by tenant
- `GET /api/v1/fraud-api-toggle/configurations/effective` - Get currently effective configurations
- `GET /api/v1/fraud-api-toggle/configurations/future` - Get future effective configurations
- `GET /api/v1/fraud-api-toggle/configurations/expired` - Get expired configurations
- `POST /api/v1/fraud-api-toggle/configurations` - Create new configuration
- `PUT /api/v1/fraud-api-toggle/configurations/{id}` - Update configuration
- `DELETE /api/v1/fraud-api-toggle/configurations/{id}` - Delete configuration

### Status Checking
- `GET /api/v1/fraud-api-toggle/check` - Check if fraud API is enabled for specific context
- `POST /api/v1/fraud-api-toggle/enable` - Enable fraud API for specific context
- `POST /api/v1/fraud-api-toggle/disable` - Disable fraud API for specific context
- `POST /api/v1/fraud-api-toggle/toggle` - Toggle fraud API for specific context

### Scheduling
- `POST /api/v1/fraud-api-toggle/configurations/schedule` - Schedule configuration for future effective time

### Statistics and Monitoring
- `GET /api/v1/fraud-api-toggle/statistics` - Get configuration statistics
- `POST /api/v1/fraud-api-toggle/cache/refresh` - Refresh cache

## React Frontend

### FraudApiToggleConfiguration Component
- **Tabbed interface** for configurations, status checking, and statistics
- **Configuration management** with form validation
- **Real-time status checking** with context-specific queries
- **Statistics dashboard** with key metrics
- **Scheduled configuration** support with date/time pickers
- **Priority-based configuration** display and management

### Key Features
- **Responsive design** with Material-UI components
- **Real-time updates** for configuration changes
- **Comprehensive filtering** and search capabilities
- **Audit trail** display for configuration changes
- **Role-based access control** integration

## Configuration Resolution Logic

### Priority Order
1. **Clearing System Level** (Highest Priority)
   - Most specific: tenant + payment type + local instrument + clearing system
2. **Local Instrument Level**
   - Specific: tenant + payment type + local instrument
3. **Payment Type Level**
   - General: tenant + payment type
4. **Tenant Level** (Lowest Priority)
   - Most general: tenant only

### Resolution Algorithm
```java
// Find most specific configuration
Optional<FraudApiToggleConfiguration> config = repository.findMostSpecificConfiguration(
    tenantId, paymentType, localInstrumentationCode, clearingSystemCode, now);

// Return enabled status or default to true
return config.map(FraudApiToggleConfiguration::getIsEnabled).orElse(true);
```

## Integration Points

### 1. **Fraud Risk Monitoring Service**
- Integrated into `FraudRiskMonitoringServiceImpl`
- Pre-assessment check for fraud API enabled status
- Automatic approval when fraud API is disabled
- Logging of disabled fraud API decisions

### 2. **Caching Layer**
- Redis-based caching for configuration lookups
- Cache invalidation on configuration updates
- Performance optimization for high-frequency checks

### 3. **Database Integration**
- JPA entities with comprehensive relationships
- Optimized queries with proper indexing
- Database views for common operations
- Migration scripts for schema management

## Security Considerations

### 1. **Authentication and Authorization**
- Role-based access control for all endpoints
- Tenant isolation for multi-tenant environments
- Audit logging for all configuration changes

### 2. **Data Protection**
- Encryption of sensitive configuration data
- Secure credential storage
- Data retention policies

### 3. **API Security**
- Rate limiting and throttling
- Input validation and sanitization
- SQL injection prevention

## Performance Optimization

### 1. **Database Optimization**
- Comprehensive indexing strategy
- Query optimization for priority-based resolution
- Connection pooling
- Caching mechanisms

### 2. **API Performance**
- Asynchronous processing
- Caching for configuration lookups
- Efficient query patterns
- Timeout handling

### 3. **Monitoring and Metrics**
- Performance metrics collection
- Health check endpoints
- Alerting for performance issues
- Capacity planning support

## Deployment Considerations

### 1. **Environment Configuration**
- Redis cache configuration
- Database connection settings
- Monitoring and logging settings

### 2. **Scaling Considerations**
- Horizontal scaling support
- Load balancing
- Database sharding strategies
- Caching layer implementation

### 3. **Monitoring and Maintenance**
- Health check endpoints
- Performance monitoring
- Log aggregation
- Alert management

## Usage Examples

### 1. **Enable Fraud API for Specific Payment Type**
```bash
curl -X POST /api/v1/fraud-api-toggle/enable \
  -H "Content-Type: application/json" \
  -d '{
    "tenantId": "tenant-001",
    "paymentType": "SEPA_CREDIT_TRANSFER",
    "reason": "SEPA transfers require fraud checking",
    "createdBy": "admin"
  }'
```

### 2. **Disable Fraud API for Specific Clearing System**
```bash
curl -X POST /api/v1/fraud-api-toggle/disable \
  -H "Content-Type: application/json" \
  -d '{
    "tenantId": "tenant-001",
    "paymentType": "SEPA_CREDIT_TRANSFER",
    "localInstrumentationCode": "SEPA_CT",
    "clearingSystemCode": "EBA_CLEARING",
    "reason": "EBA Clearing has its own fraud detection",
    "createdBy": "admin"
  }'
```

### 3. **Check Fraud API Status**
```bash
curl "/api/v1/fraud-api-toggle/check?tenantId=tenant-001&paymentType=SEPA_CREDIT_TRANSFER&localInstrumentationCode=SEPA_CT&clearingSystemCode=TARGET2"
```

### 4. **Schedule Future Configuration**
```bash
curl -X POST /api/v1/fraud-api-toggle/configurations/schedule \
  -H "Content-Type: application/json" \
  -d '{
    "tenantId": "tenant-001",
    "paymentType": "HIGH_VALUE_TRANSFER",
    "isEnabled": true,
    "enabledReason": "High value transfers will require fraud checking from next month",
    "effectiveFrom": "2024-02-01T00:00:00Z",
    "priority": 85,
    "createdBy": "admin"
  }'
```

## Future Enhancements

### 1. **Advanced Scheduling**
- Recurring configuration schedules
- Business hours-based configurations
- Holiday and weekend configurations

### 2. **Enhanced Monitoring**
- Real-time configuration change notifications
- Configuration drift detection
- Automated compliance reporting

### 3. **Integration Enhancements**
- Webhook notifications for configuration changes
- Integration with external configuration management systems
- API versioning and backward compatibility

## Conclusion

The fraud API toggle configuration system provides a comprehensive solution for dynamic fraud API enable/disable management at multiple levels. With its priority-based resolution, real-time updates, caching optimization, and comprehensive monitoring capabilities, it offers a robust foundation for fraud API management that adapts to changing business requirements without requiring application restarts.

The system is designed to be scalable, maintainable, and extensible, with proper security considerations and performance optimizations. The React frontend provides an intuitive interface for configuration management and monitoring, while the backend services ensure reliable and efficient fraud API toggle operations.

This implementation establishes a solid foundation for dynamic fraud API management, providing the flexibility to adapt to changing fraud detection requirements and business policies while maintaining optimal performance and security.