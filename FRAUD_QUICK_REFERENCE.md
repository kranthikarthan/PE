# Fraud System Quick Reference Guide

## Overview

This quick reference guide provides essential information for developers and operators working with the fraud system components.

## System Components

### 1. **Core Fraud Monitoring System**
- **Purpose**: Real-time fraud detection and risk assessment
- **Integration**: Bank's own fraud/risk monitoring engine
- **Key Features**: Risk rules, decision criteria, alerting, monitoring

### 2. **Dynamic Fraud API Toggle System**
- **Purpose**: Enable/disable fraud API calls at multiple levels
- **Key Features**: Real-time configuration, priority-based resolution, caching

## Quick Commands

### Fraud API Toggle Operations

#### Check Fraud API Status
```bash
curl "/api/v1/fraud-api-toggle/check?tenantId=tenant-001&paymentType=SEPA_CREDIT_TRANSFER"
```

#### Enable Fraud API
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

#### Disable Fraud API
```bash
curl -X POST /api/v1/fraud-api-toggle/disable \
  -H "Content-Type: application/json" \
  -d '{
    "tenantId": "tenant-001",
    "paymentType": "SEPA_CREDIT_TRANSFER",
    "reason": "Maintenance window",
    "createdBy": "admin"
  }'
```

#### Toggle Fraud API
```bash
curl -X POST /api/v1/fraud-api-toggle/toggle \
  -H "Content-Type: application/json" \
  -d '{
    "tenantId": "tenant-001",
    "paymentType": "SEPA_CREDIT_TRANSFER",
    "reason": "Toggle for testing",
    "createdBy": "admin"
  }'
```

### Fraud Risk Monitoring Operations

#### List Configurations
```bash
curl "/api/v1/fraud-risk/configurations"
```

#### Test Bank's Fraud API
```bash
curl -X POST "/api/v1/fraud-risk/configurations/{id}/test-api"
```

#### Get Assessment Statistics
```bash
curl "/api/v1/fraud-risk/statistics"
```

## Configuration Levels

### Priority Order (Most Specific to Least Specific)
1. **Clearing System Level** (Highest Priority)
   - tenant + payment type + local instrument + clearing system
2. **Local Instrument Level**
   - tenant + payment type + local instrument
3. **Payment Type Level**
   - tenant + payment type
4. **Tenant Level** (Lowest Priority)
   - tenant only

### Example Configuration Hierarchy
```
tenant-001 (Default: Enabled)
├── SEPA_CREDIT_TRANSFER (Enabled)
│   ├── SEPA_CT (Enabled)
│   │   ├── TARGET2 (Enabled)
│   │   └── EBA_CLEARING (Disabled)
│   └── SEPA_CT_INSTANT (Disabled)
└── DOMESTIC_TRANSFER (Disabled)
```

## Database Tables

### Core Tables
- `fraud_risk_configurations` - Fraud monitoring configurations
- `fraud_risk_assessments` - Fraud assessment results
- `fraud_api_toggle_configurations` - Dynamic fraud API toggle settings

### Key Views
- `fraud_risk_configurations_active` - Active fraud configurations
- `fraud_api_toggle_configurations_currently_effective` - Currently effective toggle configurations

## React Frontend Components

### FraudRiskConfiguration
- **Path**: `/frontend/src/components/FraudRiskConfiguration.tsx`
- **Purpose**: Manage fraud monitoring configurations
- **Features**: Configuration management, API testing, statistics

### FraudApiToggleConfiguration
- **Path**: `/frontend/src/components/FraudApiToggleConfiguration.tsx`
- **Purpose**: Manage fraud API toggle configurations
- **Features**: Toggle management, status checking, scheduling

## Service Classes

### Core Services
- `FraudRiskMonitoringService` - Main fraud assessment orchestration
- `ExternalFraudApiService` - Bank's fraud engine integration
- `FraudApiToggleService` - Dynamic fraud API toggle management
- `RiskAssessmentEngine` - Risk rule evaluation and scoring

### Controllers
- `FraudRiskMonitoringController` - Fraud assessment API endpoints
- `FraudApiToggleController` - Fraud API toggle management endpoints

## Integration Points

### Payment Processing Flow
```
Payment Request
    ↓
Fraud API Toggle Check
    ↓
├─ If Disabled: Auto-approve
└─ If Enabled: Fraud Risk Assessment
    ↓
Bank's Fraud Engine API Call
    ↓
Decision Enforcement
    ↓
Payment Processing
```

### Key Integration Classes
- `SchemeProcessingServiceImpl` - Payment processing integration
- `FraudRiskMonitoringServiceImpl` - Fraud assessment integration

## Caching

### Redis Cache Keys
- `fraud-api-toggle:{tenantId}:{paymentType}:{localInstrument}:{clearingSystem}`
- `fraud-api-config:{tenantId}:{paymentType}:{localInstrument}:{clearingSystem}`

### Cache Operations
- **Refresh Cache**: `POST /api/v1/fraud-api-toggle/cache/refresh`
- **Auto-invalidation**: On configuration updates

## Monitoring and Alerting

### Key Metrics
- Fraud assessment success/failure rates
- API response times
- Configuration change frequency
- Toggle usage statistics

### Health Checks
- `GET /api/v1/fraud-risk/health` - Fraud monitoring health
- `GET /api/v1/fraud-api-toggle/statistics` - Toggle system statistics

## Troubleshooting

### Common Issues

#### 1. **Fraud API Always Disabled**
- Check toggle configurations for the specific context
- Verify priority order and effective dates
- Check cache status

#### 2. **Fraud Assessment Failures**
- Verify bank's fraud API configuration
- Check API connectivity and authentication
- Review error logs and retry mechanisms

#### 3. **Configuration Not Taking Effect**
- Refresh cache: `POST /api/v1/fraud-api-toggle/cache/refresh`
- Check configuration priority and effective dates
- Verify configuration is active

### Debug Commands
```bash
# Check current effective configurations
curl "/api/v1/fraud-api-toggle/configurations/effective"

# Check specific context status
curl "/api/v1/fraud-api-toggle/check?tenantId=tenant-001&paymentType=SEPA_CREDIT_TRANSFER"

# Get system statistics
curl "/api/v1/fraud-api-toggle/statistics"
```

## Security Considerations

### Authentication
- All API endpoints require proper authentication
- Role-based access control for configuration management
- API key management for bank's fraud engine

### Data Protection
- Sensitive configuration data is encrypted
- Audit logging for all configuration changes
- Secure credential storage

## Performance Optimization

### Database
- Comprehensive indexing for fast lookups
- Connection pooling for optimal performance
- Query optimization for priority-based resolution

### Caching
- Redis-based caching for configuration lookups
- Cache invalidation on updates
- Performance optimization for high-frequency checks

## Related Documentation

- **FRAUD_RISK_MONITORING_IMPLEMENTATION.md** - Core fraud monitoring system
- **FRAUD_API_TOGGLE_IMPLEMENTATION.md** - Dynamic fraud API toggle system
- **FRAUD_DOCUMENTATION_INDEX.md** - Comprehensive documentation index

## Support

For additional support or questions:
1. Check the comprehensive documentation files
2. Review the API endpoint documentation
3. Examine the React component implementations
4. Consult the database schema and migration scripts