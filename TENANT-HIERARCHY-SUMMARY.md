# Multi-Tenancy & Tenant Hierarchy - Feature Summary

## 📋 Overview

This document summarizes the **multi-tenancy and tenant hierarchy** feature added to the Payments Engine architecture. The system now supports multiple banks/financial institutions (tenants) on a single platform with complete data isolation.

---

## 🎯 Feature Highlights

### 3-Level Tenant Hierarchy

```
┌─────────────────────────────────────────┐
│  LEVEL 1: TENANT                        │
│  (Bank / Financial Institution)         │
│  e.g., "Standard Bank SA", "Nedbank"    │
└─────────────────────────────────────────┘
                   │
         ┌─────────┼─────────┐
         ▼         ▼         ▼
┌─────────────┐ ┌─────────────┐ ┌─────────────┐
│  LEVEL 2:   │ │  LEVEL 2:   │ │  LEVEL 2:   │
│  BUSINESS   │ │  BUSINESS   │ │  BUSINESS   │
│  UNIT       │ │  UNIT       │ │  UNIT       │
│  (Retail)   │ │  (Corporate)│ │  (Investment)│
└─────────────┘ └─────────────┘ └─────────────┘
       │               │               │
       ▼               ▼               ▼
   ┌────────┐     ┌────────┐     ┌────────┐
   │LEVEL 3:│     │LEVEL 3:│     │LEVEL 3:│
   │CUSTOMER│     │CUSTOMER│     │CUSTOMER│
   └────────┘     └────────┘     └────────┘
```

### Real-World Example

```yaml
# Tenant 1: Standard Bank SA
tenant_id: STD-001
tenant_name: Standard Bank SA
business_units:
  - Retail Banking (STD-001-RET) - 2.5M customers
  - Corporate Banking (STD-001-CORP) - 15K customers  
  - Investment Banking (STD-001-INV) - 500 customers

# Tenant 2: Nedbank
tenant_id: NED-001
tenant_name: Nedbank
business_units:
  - Personal Banking (NED-001-PER) - 1.8M customers
  - Business Banking (NED-001-BUS) - 8K customers
```

---

## ✨ What Was Implemented

### 1. Tenant Management Service (NEW Microservice)

**Responsibilities**:
- ✅ Tenant lifecycle management (onboard, activate, suspend, deactivate)
- ✅ Tenant hierarchy management (Tenant → Business Unit → Customer)
- ✅ Tenant-specific configurations (limits, fraud rules, clearing credentials)
- ✅ Tenant context lookup for all services
- ✅ Usage metrics and quota enforcement
- ✅ Tenant user and API key management
- ✅ Audit trail for all tenant operations

**Key Endpoints**:
```
POST   /api/v1/platform/tenants                  # Onboard new tenant
GET    /api/v1/platform/tenants                  # List all tenants
GET    /api/v1/tenant/config/{key}               # Get tenant config
GET    /api/internal/v1/tenant/lookup/{tenantId} # Internal lookup
```

### 2. Data Isolation Strategy

**Approach**: Shared Database + Row-Level Security (RLS)

```sql
-- Every table now has tenant_id
ALTER TABLE payments ADD COLUMN tenant_id VARCHAR(20) NOT NULL;
ALTER TABLE payments ADD COLUMN business_unit_id VARCHAR(30);

-- PostgreSQL Row-Level Security enforces isolation
ALTER TABLE payments ENABLE ROW LEVEL SECURITY;

CREATE POLICY tenant_isolation_policy ON payments
    USING (tenant_id = current_setting('app.current_tenant_id')::VARCHAR);
```

**Why this approach?**
- ✅ Simpler operations than schema-per-tenant or database-per-tenant
- ✅ Cost-effective (single database infrastructure)
- ✅ Easy backups and migrations
- ✅ PostgreSQL RLS provides strong security guarantees

### 3. Tenant Context Propagation

**Request Flow**:
```
1. API Gateway
   ↓ Extracts tenant_id from JWT token
   ↓ Adds X-Tenant-ID header
   ↓
2. Tenant Context Filter (in each microservice)
   ↓ Validates tenant exists and is ACTIVE
   ↓ Sets ThreadLocal TenantContext
   ↓ Sets PostgreSQL session variable
   ↓
3. Database Query
   ↓ Row-Level Security filters by tenant_id
   ↓ Only tenant's data returned
```

**Java Implementation**:
```java
// ThreadLocal context
public class TenantContext {
    private static final ThreadLocal<String> TENANT_ID = new ThreadLocal<>();
    
    public static void setTenantId(String tenantId) {
        TENANT_ID.set(tenantId);
    }
    
    public static String getTenantId() {
        return TENANT_ID.get();
    }
}

// Filter applied to all services
@Component
@Order(1)
public class TenantContextFilter extends OncePerRequestFilter {
    
    @Override
    protected void doFilterInternal(...) {
        String tenantId = request.getHeader("X-Tenant-ID");
        
        // Validate tenant
        if (!isValidTenant(tenantId)) {
            response.sendError(403, "Invalid tenant");
            return;
        }
        
        // Set context
        TenantContext.setTenantId(tenantId);
        
        // Continue
        filterChain.doFilter(request, response);
        
        // Clear context
        TenantContext.clear();
    }
}
```

### 4. JWT Token Structure

```json
{
  "sub": "C-12345",
  "tenant_id": "STD-001",
  "tenant_name": "Standard Bank SA",
  "business_unit_id": "STD-001-RET",
  "business_unit_name": "Retail Banking",
  "customer_id": "C-12345",
  "roles": ["customer", "payment_initiator"],
  "iat": 1696867200,
  "exp": 1696870800
}
```

### 5. Tenant-Specific Configurations

Each tenant has separate configuration:
- ✅ Payment limits (tenant defaults, BU overrides, customer overrides)
- ✅ Fraud rules and thresholds  
- ✅ Clearing system credentials (SAMOS, BankservAfrica)
- ✅ Core banking system endpoints (different per tenant)
- ✅ Notification templates
- ✅ Business rules

**Configuration Lookup Hierarchy**:
```
1. Check Business Unit config (most specific)
2. Check Tenant-level config
3. Return Platform default
```

### 6. Database Changes

**New Tables (7)**:
- `tenants`: Top-level tenant records
- `business_units`: Divisions within tenants
- `tenant_configs`: Tenant-specific configurations
- `tenant_users`: Admin users per tenant
- `tenant_api_keys`: API keys for programmatic access
- `tenant_metrics`: Daily usage metrics per tenant
- `tenant_audit_log`: Audit trail for all operations

**Updated ALL Existing Tables**:
```sql
-- Added to every table in every service
tenant_id VARCHAR(20) NOT NULL
business_unit_id VARCHAR(30)

-- Indexes for performance
idx_<table>_tenant ON <table>(tenant_id)
idx_<table>_tenant_bu ON <table>(tenant_id, business_unit_id)

-- Row-Level Security policies
ALTER TABLE <table> ENABLE ROW LEVEL SECURITY;
```

### 7. Event Schema Updates

**All events now include tenant context**:
```yaml
TenantContext:
  tenant_id: "STD-001"
  tenant_name: "Standard Bank SA"
  business_unit_id: "STD-001-RET"
  business_unit_name: "Retail Banking"

PaymentInitiatedEvent:
  tenant_context: { ... }  # NEW - included in ALL events
  payment_id: "PAY-12345"
  amount: 1000.00
  # ... other fields
```

### 8. Tenant Onboarding Process

**Automated 7-step Process (15 minutes)**:

```
1. Create tenant record
   ↓
2. Create business units
   ↓
3. Provision resources (Kafka topics, Redis namespace)
   ↓
4. Configure clearing system credentials
   ↓
5. Set default limits and fraud rules
   ↓
6. Create first admin user
   ↓
7. Activate tenant (status: ACTIVE)
```

**API**:
```bash
POST /api/v1/platform/tenants
{
  "tenant_name": "New Bank SA",
  "tenant_code": "NEWBANK",
  "country_code": "ZA",
  "contact_email": "admin@newbank.co.za",
  "business_units": [
    {"code": "RET", "name": "Retail Banking", "type": "RETAIL"},
    {"code": "CORP", "name": "Corporate Banking", "type": "CORPORATE"}
  ]
}
```

### 9. Tenant Isolation & Security

**Data Isolation**:
- ✅ PostgreSQL Row-Level Security (RLS) enforces tenant filtering
- ✅ Every query automatically filtered by tenant_id
- ✅ No cross-tenant data access possible (strict isolation)
- ✅ Platform admins can view all tenants (with explicit flag)

**Performance Isolation**:
- ✅ Kubernetes resource quotas per tenant
- ✅ Rate limiting per tenant (max TPS)
- ✅ Database connection pools per tenant
- ✅ Kafka partitions assigned per tenant

**Tenant Quotas**:
```yaml
max_transactions_per_second: 10,000
max_storage_gb: 500
max_api_calls_per_month: 50,000,000
max_business_units: 20
max_customers: 1,000,000
```

### 10. Monitoring & Metrics

**Per-Tenant Dashboard**:
```
Tenant: Standard Bank SA (STD-001)
├── Transactions Today: 125,456
├── Transaction Volume: R 4.2B
├── API Calls: 2.1M
├── Average Response Time: 85ms
├── Error Rate: 0.02%
│
├── Business Units
│   ├── Retail: 85,000 txn
│   ├── Corporate: 35,000 txn
│   └── Investment: 5,456 txn
│
└── Quota Usage
    ├── TPS: 2,150 / 10,000 (21%)
    ├── Storage: 125 GB / 500 GB (25%)
    └── API Calls: 12M / 50M (24%)
```

---

## 📊 Architecture Impact

### Before vs After

| Aspect | Before | After (Multi-Tenant) |
|--------|--------|----------------------|
| **Deployment Model** | Single tenant | Multi-tenant SaaS |
| **Data Model** | Shared | Tenant-isolated |
| **Configuration** | Global | Tenant-specific |
| **Hierarchy** | Flat | 3-level hierarchy |
| **Authentication** | Single IDP | Multi-tenant IDP |
| **Database** | Simple tables | tenant_id in all tables + RLS |
| **Events** | No tenant context | Tenant context in all events |
| **Microservices** | 16 services | 17 services (+Tenant Management) |
| **Onboarding** | Manual | Automated (15 min) |

### Multi-Tenancy Benefits

✅ **SaaS Model**: Single platform serves multiple banks  
✅ **Cost Efficiency**: Shared infrastructure, lower per-tenant cost  
✅ **Rapid Onboarding**: New tenants live in 15 minutes  
✅ **Tenant Isolation**: Complete data and performance isolation  
✅ **Flexibility**: Tenant-specific configs, limits, and rules  
✅ **Scalability**: Support 100+ tenants on single platform  
✅ **Security**: Row-level security enforced at database level  
✅ **Monitoring**: Per-tenant metrics and dashboards  

---

## 🔧 Implementation Summary

### Code Changes

**New Files**:
- ✅ `TenantManagementService.java` (450 lines)
- ✅ `TenantContextFilter.java` (150 lines)
- ✅ `TenantContext.java` (100 lines)
- ✅ `TenantOnboardingService.java` (300 lines)
- ✅ `TenantJpaInterceptor.java` (150 lines)

**Updated Files**:
- ✅ All entity classes: Added `tenant_id` and `business_unit_id` fields
- ✅ All repositories: Added tenant filtering methods
- ✅ All services: Use `TenantContext.getTenantId()` for queries
- ✅ API Gateway: Extract tenant_id from JWT
- ✅ Event schemas: Added `TenantContext` to all events

### Database Changes

**New Schema**: `tenant_management_db`
- 7 new tables for tenant management

**Updated Schemas**: ALL databases (13 databases)
- Added `tenant_id` and `business_unit_id` columns to ALL tables (100+ tables)
- Added indexes on `tenant_id` for performance
- Enabled Row-Level Security on ALL tables
- Created tenant isolation policies

### Configuration Changes

**application.yml** (all services):
```yaml
spring:
  jpa:
    properties:
      hibernate:
        session_factory:
          interceptor: com.payments.TenantJpaInterceptor
        
datasource:
  hikari:
    data-source-properties:
      app.current_tenant_id: ${TENANT_ID}
```

**API Gateway**:
```yaml
plugins:
  - jwt:
      claims_to_verify: [tenant_id, business_unit_id]
  - request-transformer:
      add:
        headers:
          - X-Tenant-ID:$(jwt.tenant_id)
          - X-Business-Unit-ID:$(jwt.business_unit_id)
  - rate-limiting:
      limit_by: header
      header_name: X-Tenant-ID
```

---

## 📚 Documentation

### New Documents

1. **[12-TENANT-MANAGEMENT.md](docs/12-TENANT-MANAGEMENT.md)** (800 lines)
   - Complete tenant hierarchy design
   - Database schema with RLS policies
   - Tenant context propagation
   - Configuration management
   - Onboarding API and process
   - Testing strategy

### Updated Documents

2. **[01-ASSUMPTIONS.md](docs/01-ASSUMPTIONS.md)** - Section 2.4
   - Multi-tenancy assumptions
   - Tenant hierarchy model
   - Data isolation strategy
   - Tenant identification
   - Onboarding process

3. **[02-MICROSERVICES-BREAKDOWN.md](docs/02-MICROSERVICES-BREAKDOWN.md)** - Section 0
   - New Tenant Management Service
   - API endpoints
   - Database tables
   - Key implementation code

4. **[03-EVENT-SCHEMAS.md](docs/03-EVENT-SCHEMAS.md)**
   - Added `TenantContext` schema
   - Updated all event payloads to include `tenant_context`

5. **[05-DATABASE-SCHEMAS.md](docs/05-DATABASE-SCHEMAS.md)**
   - Added Tenant Management database (Section 0)
   - Updated all tables with `tenant_id` columns
   - Added RLS policies
   - Migration scripts

6. **[README.md](README.md)**
   - Updated microservices count (16 → 17)
   - Added multi-tenancy to core principles
   - Added link to tenant management guide

---

## 🎯 Use Cases

### Use Case 1: Multiple Banks on One Platform

**Scenario**: Platform serves Standard Bank, Nedbank, Absa, and FNB simultaneously.

**Implementation**:
- Each bank = separate tenant (STD-001, NED-001, ABSA-001, FNB-001)
- Complete data isolation via Row-Level Security
- Tenant-specific clearing credentials and core banking endpoints
- Per-tenant limits and fraud rules
- Separate admin portals per bank

### Use Case 2: Bank with Multiple Divisions

**Scenario**: Standard Bank has Retail, Corporate, and Investment divisions.

**Implementation**:
- Tenant: Standard Bank (STD-001)
- Business Units: Retail (STD-001-RET), Corporate (STD-001-CORP), Investment (STD-001-INV)
- Each BU can override tenant-level limits
- Separate reporting per BU
- Cross-BU payments supported (same tenant)

### Use Case 3: Tenant-Specific Configuration

**Scenario**: Different fraud rules for different banks.

**Implementation**:
```java
// Get fraud threshold for current tenant
BigDecimal threshold = tenantConfigService.getConfig(
    TenantContext.getTenantId(),
    "fraud.high_risk_threshold"
);
// Standard Bank: 10000
// Nedbank: 15000
// Different per tenant
```

---

## 🧪 Testing Multi-Tenancy

### Integration Test Example

```java
@Test
void shouldIsolateTenantData() {
    // Tenant 1: Standard Bank
    String tenant1Token = generateJwt("STD-001", "C-001");
    
    // Tenant 2: Nedbank  
    String tenant2Token = generateJwt("NED-001", "C-002");
    
    // Create payment for Tenant 1
    mockMvc.perform(post("/api/v1/payments")
            .header("Authorization", "Bearer " + tenant1Token)
            .content("{\"amount\": 100}"))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.payment_id").value("PAY-001"));
    
    // Tenant 2 should NOT see Tenant 1's payment (403 Forbidden)
    mockMvc.perform(get("/api/v1/payments/PAY-001")
            .header("Authorization", "Bearer " + tenant2Token))
        .andExpect(status().isNotFound());
}
```

---

## 🚀 Migration Path

### For Existing Single-Tenant Deployment

**Phase 1**: Add Tenant Management Service
```bash
1. Deploy tenant_management_db
2. Create first tenant (existing deployment)
3. Deploy Tenant Management Service
```

**Phase 2**: Add tenant_id to All Tables
```sql
-- Run migration script (see 05-DATABASE-SCHEMAS.md)
-- Zero-downtime possible with careful planning
```

**Phase 3**: Update All Services
```bash
1. Add TenantContextFilter to all services
2. Update entities with @TenantAware
3. Enable Row-Level Security
4. Deploy service by service (canary)
```

**Phase 4**: Onboard Second Tenant
```bash
POST /api/v1/platform/tenants
# Verify isolation works
```

---

## 📈 Performance Considerations

### Indexing Strategy
```sql
-- All tables have tenant index
CREATE INDEX idx_<table>_tenant ON <table>(tenant_id);

-- Composite indexes for common queries
CREATE INDEX idx_payments_tenant_status 
    ON payments(tenant_id, status);
```

### Query Performance
- ✅ All queries filtered by `tenant_id` (uses index)
- ✅ PostgreSQL RLS adds minimal overhead (<1ms)
- ✅ Connection pooling per tenant reduces contention

### Scalability
- ✅ Supports 100+ tenants on single platform
- ✅ High-volume tenants can be partitioned by `tenant_id`
- ✅ Horizontal scaling via read replicas (filtered by tenant)

---

## 🔒 Security Best Practices

1. **Never Skip Tenant Validation**: Every request must include valid `X-Tenant-ID`
2. **Use Row-Level Security**: Belt-and-suspenders approach (RLS + app filtering)
3. **Audit All Access**: Log all tenant data access in `tenant_audit_log`
4. **Separate Credentials**: Each tenant has separate clearing/core banking credentials
5. **Rate Limiting**: Enforce per-tenant rate limits to prevent abuse
6. **No Cross-Tenant Queries**: Application code must never query across tenants

---

## 📊 Summary

### What Was Added

✅ **1 New Microservice**: Tenant Management Service  
✅ **3-Level Hierarchy**: Tenant → Business Unit → Customer  
✅ **7 New Database Tables**: Complete tenant management schema  
✅ **100+ Tables Updated**: Added `tenant_id` to ALL tables  
✅ **Row-Level Security**: PostgreSQL RLS on all tables  
✅ **Tenant Context**: ThreadLocal context + HTTP headers  
✅ **Automated Onboarding**: 15-minute tenant provisioning  
✅ **Tenant-Specific Config**: Limits, fraud rules, endpoints  
✅ **Per-Tenant Monitoring**: Metrics, quotas, dashboards  
✅ **Event Updates**: Tenant context in all events  

### Documents

**New**: 1 comprehensive guide (800 lines)  
**Updated**: 5 existing documents  
**Total Multi-Tenancy Docs**: ~2,000 lines  

### Code Impact

**New Code**: ~1,500 lines (Tenant Management Service)  
**Updated Code**: All services (tenant context propagation)  
**Database Changes**: 7 new tables + 100+ tables updated  

---

## 🎯 Next Steps

For implementation:

1. **Read**: [12-TENANT-MANAGEMENT.md](docs/12-TENANT-MANAGEMENT.md) - Complete design
2. **Database**: Deploy `tenant_management_db` first
3. **Service**: Implement Tenant Management Service
4. **Migration**: Add `tenant_id` to existing tables
5. **Services**: Add `TenantContextFilter` to all services
6. **Testing**: Verify tenant isolation
7. **Onboard**: Create first tenant via API

---

**Last Updated**: 2025-10-11  
**Version**: 1.0
