# Payment Engine - Enterprise Multi-Tenant Banking Platform

## 🏦 **Overview**

The Payment Engine is a **complete, enterprise-grade, multi-tenant banking platform** designed for Banking-as-a-Service operations. It provides unlimited bank clients with complete data isolation, runtime configuration management, and comprehensive ISO 20022 compliance.

## 🎯 **Key Capabilities**

### **🏢 Multi-Tenancy**
- **Unlimited Bank Clients**: Serve multiple banks with complete isolation
- **Data Isolation**: Row-level security ensures tenant data separation
- **Tenant-Specific Configuration**: Custom settings per bank client
- **Resource Quotas**: Per-tenant limits and monitoring
- **Subscription Tiers**: Different feature sets per subscription level

### **⚙️ Runtime Configurability**
- **Zero-Downtime Configuration**: Change settings without restarts
- **Dynamic Payment Types**: Add new payment methods via API
- **Feature Flag Management**: Gradual rollouts with A/B testing
- **Rate Limiting**: Dynamic per-tenant rate limit adjustments
- **Business Rules**: Tenant-specific validation and compliance rules

### **📋 ISO 20022 Compliance**
- **Complete Message Support**: pain, pacs, camt message types
- **Standards Compliance**: Full ISO 20022 pain.001.001.03 support
- **Message Validation**: Comprehensive validation and error handling
- **Bulk Processing**: High-volume message processing capabilities
- **Cancellation Support**: camt.055 payment cancellation requests

### **🔒 Enterprise Security**
- **Multi-Layer Security**: Network, API, application, and data security
- **OAuth2/JWT Authentication**: Enterprise-grade authentication
- **Role-Based Access Control**: Granular permission management
- **Audit Trails**: Comprehensive logging and compliance tracking
- **Azure Key Vault**: Secure secrets management
- **Real-Time Fraud Detection**: Integration with bank's fraud/risk monitoring engine
- **Dynamic Fraud API Control**: Enable/disable fraud checks at multiple levels

### **📊 Comprehensive Monitoring**
- **Multi-Tenant Dashboards**: Per-tenant monitoring and alerting
- **SLA Tracking**: Tenant-specific SLA compliance monitoring
- **Performance Metrics**: Real-time performance and resource usage
- **Security Monitoring**: Fraud detection and security event correlation
- **Cost Tracking**: Per-tenant resource usage for billing
- **Fraud Risk Monitoring**: Real-time fraud assessment and decision tracking
- **Fraud API Toggle Monitoring**: Dynamic fraud API control monitoring

## 🏗️ **Architecture**

```
┌─────────────────────────────────────────────────────────────────────────┐
│                    MULTI-TENANT BANKING PLATFORM                       │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐        │
│  │   Tenant A      │  │   Tenant B      │  │   Tenant C      │        │
│  │  (Bank ABC)     │  │  (FinTech XYZ)  │  │ (Credit Union)  │        │
│  └─────────────────┘  └─────────────────┘  └─────────────────┘        │
│           │                       │                       │            │
│  ┌─────────────────────────────────────────────────────────────────────┤
│  │                    TENANT ISOLATION LAYER                          │
│  │  • Row-Level Security (RLS)                                        │
│  │  • Tenant Context Propagation                                      │
│  │  • Configuration Isolation                                         │
│  └─────────────────────────────────────────────────────────────────────┤
│  ┌─────────────────────────────────────────────────────────────────────┤
│  │                     APPLICATION LAYER                              │
│  │  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐  ┌────────────┐ │
│  │  │   React     │  │ API Gateway │  │ Payment Processing  │  │    Core    │ │
│  │  │  Frontend   │  │             │  │   Service   │  │  Banking   │ │
│  │  │             │  │ • Routing   │  │             │  │            │ │
│  │  │ • Multi-    │  │ • Auth      │  │ • Auth      │  │ • Payments │ │
│  │  │   Tenant UI │  │ • Rate Lmt  │  │ • Webhooks  │  │ • Accounts │ │
│  │  │ • Config    │  │ • Tenant    │  │ • Notifs    │  │ • ISO20022 │ │
│  │  │   Mgmt      │  │   Headers   │  │             │  │ • Config   │ │
│  │  └─────────────┘  └─────────────┘  └─────────────┘  └────────────┘ │
│  └─────────────────────────────────────────────────────────────────────┤
│  ┌─────────────────────────────────────────────────────────────────────┤
│  │                       DATA LAYER                                   │
│  │  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐  ┌────────────┐ │
│  │  │ PostgreSQL  │  │    Redis    │  │    Kafka    │  │   Azure    │ │
│  │  │             │  │             │  │             │  │    Key     │ │
│  │  │ • Banking   │  │ • Config    │  │ • Events    │  │   Vault    │ │
│  │  │   Schema    │  │   Cache     │  │ • Multi-    │  │            │ │
│  │  │ • Config    │  │ • Sessions  │  │   Tenant    │  │ • Secrets  │ │
│  │  │   Schema    │  │ • Rate Lmt  │  │   Topics    │  │ • Certs    │ │
│  │  │ • RLS       │  │             │  │             │  │            │ │
│  │  └─────────────┘  └─────────────┘  └─────────────┘  └────────────┘ │
│  └─────────────────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────────────────┘
```

## 🚀 **Quick Start**

### **Prerequisites**
- Docker 24.0+
- Docker Compose 2.0+
- Node.js 18+
- Java 17+
- Maven 3.9+

### **Local Development Setup**

```bash
# 1. Clone repository
git clone https://github.com/your-org/payment-engine.git
cd payment-engine

# 2. Start infrastructure services
docker-compose up -d postgres kafka redis elasticsearch

# 3. Wait for services to be ready
./scripts/wait-for-services.sh

# 4. Run database migrations
docker-compose exec postgres psql -U payment_user -d payment_engine -f /docker-entrypoint-initdb.d/03-run-migrations.sql

# 5. Build and start all services
./build-all.sh --deploy

# 6. Start frontend
cd frontend
npm install
npm start
```

### **Access the Application**

- **Frontend**: http://localhost:3000
- **API Gateway**: http://localhost:8080
- **Configuration UI**: http://localhost:3000/configuration
- **Grafana**: http://localhost:3001 (admin/admin)
- **Kibana**: http://localhost:5601

### **Default Login**
- **Username**: admin@payment-engine.com
- **Password**: admin123
- **Tenant**: default

## 🔧 **Multi-Tenant Operations**

### **Create New Tenant (Bank Client)**

```bash
# Create new bank tenant
curl -X POST http://localhost:8080/api/v1/config/tenants \
  -H "Authorization: Bearer <admin-token>" \
  -H "Content-Type: application/json" \
  -d '{
    "tenantId": "regional-bank",
    "tenantName": "Regional Bank Corp",
    "tenantType": "BANK",
    "subscriptionTier": "PREMIUM",
    "configuration": {
      "features": {
        "iso20022": true,
        "bulkProcessing": true,
        "advancedFraudDetection": false
      },
      "limits": {
        "transactionsPerDay": 100000,
        "apiCallsPerHour": 10000
      }
    }
  }'
```

### **Add Payment Type at Runtime**

```bash
# Add new payment type for specific tenant
curl -X POST http://localhost:8080/api/v1/config/tenants/regional-bank/payment-types \
  -H "Authorization: Bearer <token>" \
  -H "X-Tenant-ID: regional-bank" \
  -H "Content-Type: application/json" \
  -d '{
    "code": "CRYPTO_TRANSFER",
    "name": "Cryptocurrency Transfer",
    "description": "Blockchain-based transfers",
    "isSynchronous": true,
    "maxAmount": 1000000.00,
    "processingFee": 5.00,
    "configuration": {
      "blockchainNetwork": "ethereum",
      "confirmationsRequired": 6
    }
  }'
```

### **Configure Feature Flags**

```bash
# Enable feature for specific tenant with gradual rollout
curl -X POST http://localhost:8080/api/v1/config/tenants/regional-bank/features/advanced-fraud-detection \
  -H "Authorization: Bearer <token>" \
  -H "X-Tenant-ID: regional-bank" \
  -H "Content-Type: application/json" \
  -d '{
    "enabled": true,
    "config": {
      "rolloutPercentage": 25,
      "mlModel": "fraud-detector-v2.1",
      "confidenceThreshold": 0.85
    }
  }'
```

### **Process ISO 20022 Messages**

```bash
# Process pain.001 payment initiation
curl -X POST http://localhost:8080/api/v1/iso20022/pain001 \
  -H "Authorization: Bearer <token>" \
  -H "X-Tenant-ID: regional-bank" \
  -H "Content-Type: application/json" \
  -d '{
    "CstmrCdtTrfInitn": {
      "GrpHdr": {
        "MsgId": "MSG-20240115-001",
        "CreDtTm": "2024-01-15T10:30:00.000Z",
        "NbOfTxs": "1",
        "CtrlSum": "1000.00"
      },
      "PmtInf": {
        "PmtInfId": "PMT-20240115-001",
        "PmtMtd": "TRF",
        "CdtTrfTxInf": {
          "PmtId": {"EndToEndId": "E2E-20240115-001"},
          "Amt": {"InstdAmt": {"Ccy": "USD", "Value": "1000.00"}},
          "Cdtr": {"Nm": "John Doe"},
          "CdtrAcct": {"Id": {"IBAN": "US33XXXX1234567890"}}
        }
      }
    }
  }'
```

## 📊 **Monitoring & Observability**

### **Multi-Tenant Dashboards**

The system includes comprehensive monitoring with tenant-specific dashboards:

- **Tenant Overview**: Transaction volumes, error rates, resource usage per tenant
- **SLA Monitoring**: Per-tenant SLA compliance tracking
- **Feature Flag Status**: Rollout status and performance impact
- **Security Dashboard**: Fraud detection, authentication failures, security events
- **Resource Usage**: Per-tenant resource consumption for billing

### **Key Metrics**

```promql
# Transaction volume per tenant
sum(rate(payment_transactions_total{tenant_id="regional-bank"}[5m]))

# Error rate per tenant
(sum(rate(payment_transactions_total{tenant_id="regional-bank",status="ERROR"}[5m])) / 
 sum(rate(payment_transactions_total{tenant_id="regional-bank"}[5m]))) * 100

# Resource usage per tenant
tenant_resource_usage_percentage{tenant_id="regional-bank",resource_type="TRANSACTIONS_PER_DAY"}

# Feature flag rollout status
tenant_feature_flag_rollout_percentage{tenant_id="regional-bank",feature_name="advanced-fraud-detection"}
```

### **Alerting**

Tenant-specific alerts with context:

```yaml
- alert: TenantHighErrorRate
  expr: tenant_error_rate{tenant_id="regional-bank"} > 5
  annotations:
    summary: "High error rate for tenant regional-bank"
    tenant_id: "regional-bank"
    runbook_url: "https://docs.paymentengine.com/runbooks/tenant-high-errors"
```

## 🔒 **Security Features**

### **Multi-Layer Security**

1. **Network Security**: TLS 1.3, WAF, VNet isolation
2. **API Security**: OAuth2/JWT, RBAC, rate limiting
3. **Application Security**: Input validation, XSS/CSRF protection
4. **Data Security**: Row-level security, encryption at rest/transit
5. **Audit & Compliance**: Comprehensive logging, regulatory reporting

### **Tenant Isolation**

- **Data Isolation**: Row-level security ensures tenant data separation
- **Configuration Isolation**: Tenant-specific configurations with inheritance
- **Resource Isolation**: Per-tenant quotas and limits
- **Security Isolation**: Tenant-aware authentication and authorization

## 📋 **API Documentation**

### **Core APIs**

| Category | Endpoint Pattern | Description |
|----------|-----------------|-------------|
| **Tenant Management** | `/api/v1/config/tenants/**` | Create, manage, configure tenants |
| **Configuration** | `/api/v1/config/tenants/{id}/config/**` | Runtime configuration management |
| **Payment Types** | `/api/v1/config/tenants/{id}/payment-types/**` | Dynamic payment type management |
| **Feature Flags** | `/api/v1/config/tenants/{id}/features/**` | Feature flag and A/B testing |
| **Rate Limits** | `/api/v1/config/tenants/{id}/rate-limits/**` | Dynamic rate limit management |
| **Transactions** | `/api/v1/transactions/**` | Transaction processing (tenant-aware) |
| **ISO 20022** | `/api/v1/iso20022/**` | ISO 20022 message processing |
| **Accounts** | `/api/v1/accounts/**` | Account management (tenant-aware) |

### **Authentication**

All APIs require authentication via JWT tokens with tenant context:

```bash
curl -X GET /api/v1/transactions \
  -H "Authorization: Bearer <jwt-token>" \
  -H "X-Tenant-ID: regional-bank"
```

## 🗄️ **Database Schema**

### **Multi-Tenant Schema Design**

```sql
-- Tenant management
📊 config.tenants                    -- Tenant information and settings
📊 config.tenant_configurations      -- Tenant-specific configurations
📊 config.tenant_limits              -- Resource quotas and limits

-- Configuration management
📊 config.feature_flags              -- A/B testing and feature rollouts
📊 config.rate_limits                -- Dynamic rate limiting
📊 config.dynamic_payment_types      -- Runtime payment type management
📊 config.business_rules             -- Tenant-specific business logic

-- Banking data (with tenant isolation)
📊 payment_engine.transactions       -- Multi-tenant transactions
📊 payment_engine.accounts           -- Multi-tenant accounts
📊 payment_engine.customers          -- Multi-tenant customers
📊 payment_engine.payment_types      -- Payment type definitions

-- ISO 20022 support
📊 iso20022_messages                 -- ISO 20022 message storage
📊 iso20022_cancellations           -- Payment cancellation tracking
📊 iso20022_scheme_messages         -- Scheme message processing

-- Audit and compliance
📊 audit.transaction_logs           -- Transaction audit trail
📊 audit.configuration_changes      -- Configuration change history
📊 audit.security_events           -- Security event logging
```

### **Row-Level Security (RLS)**

All tenant-aware tables use RLS for automatic data isolation:

```sql
-- Automatic tenant filtering
ALTER TABLE payment_engine.transactions ENABLE ROW LEVEL SECURITY;

CREATE POLICY tenant_isolation_transactions ON payment_engine.transactions
    FOR ALL TO payment_engine_role
    USING (tenant_id = current_setting('app.current_tenant', true));
```

## 🚀 **Deployment**

### **Local Development**
```bash
./build-all.sh --deploy
```

### **Production Deployment**
```bash
# Deploy to Azure AKS
az aks get-credentials --resource-group payment-engine-prod --name payment-engine-aks
kubectl apply -f deployment/kubernetes/
```

### **CI/CD Pipeline**
- **Azure DevOps**: Complete CI/CD with multi-environment support
- **GitOps**: Optional ArgoCD integration for GitOps workflows
- **Docker**: Multi-stage builds with security scanning
- **Kubernetes**: Helm charts and manifests for all environments

## 📚 **Documentation**

| Document | Description |
|----------|-------------|
| [Complete API Documentation](documentation/COMPLETE_API_DOCUMENTATION.md) | Comprehensive API reference with examples |
| [Architecture Overview](documentation/COMPLETE_ARCHITECTURE_OVERVIEW.md) | Detailed system architecture and design |
| [Deployment Guide](documentation/COMPLETE_DEPLOYMENT_GUIDE.md) | Production deployment instructions |
| [ISO 20022 Documentation](documentation/ISO20022_API_DOCUMENTATION.md) | ISO 20022 message processing guide |
| [Configuration Guide](documentation/CONFIGURATION_GUIDE.md) | Runtime configuration management |
| [Multi-Tenancy Guide](documentation/MULTI_TENANCY_GUIDE.md) | Multi-tenant setup and management |

## 🔧 **Configuration**

### **Environment Variables**

| Variable | Description | Default |
|----------|-------------|---------|
| `SPRING_PROFILES_ACTIVE` | Active Spring profiles | `production,multi-tenant` |
| `TENANCY_ENABLED` | Enable multi-tenancy | `true` |
| `CONFIG_SERVICE_ENABLED` | Enable configuration service | `true` |
| `FEATURE_FLAGS_ENABLED` | Enable feature flags | `true` |
| `DATABASE_URL` | PostgreSQL connection URL | Required |
| `REDIS_URL` | Redis connection URL | Required |
| `KAFKA_BOOTSTRAP_SERVERS` | Kafka brokers | Required |

### **Configuration Files**

| File | Purpose |
|------|---------|
| `configuration/payment-types.yml` | Default payment type definitions |
| `configuration/kafka-topics.yml` | Kafka topic configurations |
| `deployment/kubernetes/*.yaml` | Kubernetes deployment manifests |
| `monitoring/prometheus/*.yml` | Monitoring and alerting rules |

## 🧪 **Testing**

### **Test Suites**

- **Unit Tests**: Comprehensive unit test coverage for all services
- **Integration Tests**: End-to-end API and database testing
- **Multi-Tenant Tests**: Tenant isolation and context testing
- **ISO 20022 Tests**: Message validation and processing tests
- **Performance Tests**: Load testing with multi-tenant scenarios
- **Security Tests**: Authentication, authorization, and data isolation tests

### **Running Tests**

```bash
# Run all tests
./scripts/run-tests.sh

# Run specific test suites
mvn test -Dtest=TenantIsolationTest
mvn test -Dtest=Iso20022ProcessingTest
mvn test -Dtest=ConfigurationServiceTest
```

## 🤝 **Contributing**

### **Development Guidelines**

1. **Multi-Tenancy**: All new features must support multi-tenancy
2. **Configuration**: Make features configurable at runtime
3. **Testing**: Include tenant isolation tests for all changes
4. **Documentation**: Update API documentation for all changes
5. **Security**: Follow security best practices and audit requirements

### **Code Standards**

- **Java**: Follow Google Java Style Guide
- **TypeScript**: Use ESLint and Prettier
- **SQL**: Use consistent naming conventions
- **API**: Follow RESTful API design principles

## 📞 **Support**

### **Getting Help**

- **Documentation**: Comprehensive guides and API references
- **Issues**: GitHub Issues for bug reports and feature requests
- **Discussions**: GitHub Discussions for questions and community support
- **Enterprise Support**: Contact for enterprise support options

### **Monitoring & Alerts**

- **Health Checks**: `/api/v1/health` endpoint for service health
- **Metrics**: Prometheus metrics for monitoring and alerting
- **Logs**: Structured logging with tenant context
- **Dashboards**: Grafana dashboards for operational visibility

## 📄 **License**

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🏆 **Features Summary**

✅ **Multi-Tenant Architecture** - Unlimited bank clients with complete isolation  
✅ **Runtime Configuration** - Zero-downtime configuration management  
✅ **ISO 20022 Compliance** - Complete pain, pacs, camt message support  
✅ **Feature Flag Management** - A/B testing and gradual rollouts  
✅ **Dynamic Payment Types** - Add payment methods via API  
✅ **Enterprise Security** - Multi-layer security with audit trails  
✅ **Comprehensive Monitoring** - Tenant-specific dashboards and alerting  
✅ **Self-Healing** - Automated recovery and rollback capabilities  
✅ **Scalable Architecture** - Kubernetes-native with auto-scaling  
✅ **Banking-as-a-Service Ready** - Complete platform for serving multiple banks  

---

**The Payment Engine is a complete, production-ready, multi-tenant banking platform designed for Banking-as-a-Service operations with enterprise-grade security, monitoring, and configurability.**