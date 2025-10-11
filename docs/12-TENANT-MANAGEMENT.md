# Multi-Tenancy & Tenant Hierarchy - Implementation Guide

## Overview

This document provides comprehensive specifications for implementing **multi-tenancy** with **tenant hierarchy** in the Payments Engine. The system supports multiple banks/financial institutions (tenants) on a single platform with complete data isolation and tenant-specific configurations.

---

## Tenant Hierarchy Model

### 3-Level Hierarchy

```
┌─────────────────────────────────────────────────────────────┐
│  TENANT (Bank / Financial Institution)                      │
│  - Unique tenant_id: "STD-001", "NED-001"                  │
│  - Top-level isolation boundary                            │
│  - Owns all business units and customers                   │
└─────────────────────────────────────────────────────────────┘
                         │
         ┌───────────────┼───────────────┐
         ▼               ▼               ▼
┌──────────────┐  ┌──────────────┐  ┌──────────────┐
│ BUSINESS UNIT│  │ BUSINESS UNIT│  │ BUSINESS UNIT│
│ - Retail     │  │ - Corporate  │  │ - Investment │
│ - bu_id: RET │  │ - bu_id: CORP│  │ - bu_id: INV │
│ - Division   │  │ - Division   │  │ - Division   │
└──────────────┘  └──────────────┘  └──────────────┘
       │                 │                 │
       ▼                 ▼                 ▼
   ┌────────┐       ┌────────┐       ┌────────┐
   │Customer│       │Customer│       │Customer│
   │ C-0001 │       │ C-5001 │       │ C-9001 │
   └────────┘       └────────┘       └────────┘
```

### Real-World Example

```yaml
# Tenant 1: Standard Bank SA
tenant_id: STD-001
tenant_name: Standard Bank SA
country: ZA
status: ACTIVE
business_units:
  - bu_id: STD-001-RET
    name: Retail Banking
    type: RETAIL
    customer_count: 2,500,000
    
  - bu_id: STD-001-CORP
    name: Corporate Banking
    type: CORPORATE
    customer_count: 15,000
    
  - bu_id: STD-001-INV
    name: Investment Banking
    type: INVESTMENT
    customer_count: 500

# Tenant 2: Nedbank
tenant_id: NED-001
tenant_name: Nedbank
country: ZA
status: ACTIVE
business_units:
  - bu_id: NED-001-PER
    name: Personal Banking
    type: RETAIL
    customer_count: 1,800,000
```

---

## Data Isolation Strategy

### Shared Database with Row-Level Security

```sql
-- Every table has tenant_id
CREATE TABLE payments (
    payment_id UUID PRIMARY KEY,
    tenant_id VARCHAR(20) NOT NULL,        -- Isolation key
    business_unit_id VARCHAR(30) NOT NULL,
    customer_id VARCHAR(50) NOT NULL,
    amount DECIMAL(18,2) NOT NULL,
    -- ... other columns
    
    CONSTRAINT fk_tenant FOREIGN KEY (tenant_id) 
        REFERENCES tenants(tenant_id)
);

-- Index for tenant filtering (critical for performance)
CREATE INDEX idx_payments_tenant ON payments(tenant_id);
CREATE INDEX idx_payments_tenant_bu ON payments(tenant_id, business_unit_id);

-- PostgreSQL Row-Level Security Policy
ALTER TABLE payments ENABLE ROW LEVEL SECURITY;

CREATE POLICY tenant_isolation_policy ON payments
    USING (tenant_id = current_setting('app.current_tenant_id')::VARCHAR);
```

### Why Shared Database?

| Strategy | Pros | Cons | Decision |
|----------|------|------|----------|
| **Shared DB + Row-Level Security** | ✅ Simplest operations<br>✅ Cost effective<br>✅ Easy backups<br>✅ Easy migrations | ⚠️ Requires careful query filtering | ✅ **CHOSEN** |
| Schema per Tenant | ✅ Moderate isolation | ❌ Complex migrations<br>❌ 100+ schemas hard to manage | ❌ |
| Database per Tenant | ✅ Complete isolation | ❌ Very expensive<br>❌ Complex operations<br>❌ Hard to scale | ❌ |
| Separate Deployments | ✅ Complete isolation | ❌ Not scalable<br>❌ Very expensive | ❌ |

---

## Tenant Database Schema

### Core Tenant Tables

```sql
-- =============================================================================
-- TENANT MANAGEMENT SCHEMA
-- =============================================================================

-- 1. Tenants (Top-level)
CREATE TABLE tenants (
    tenant_id VARCHAR(20) PRIMARY KEY,
    tenant_name VARCHAR(200) NOT NULL,
    tenant_code VARCHAR(10) UNIQUE NOT NULL,
    country_code CHAR(2) NOT NULL,
    
    -- Status
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    -- PENDING, ACTIVE, SUSPENDED, DEACTIVATED
    
    -- Contact
    primary_contact_name VARCHAR(200),
    primary_contact_email VARCHAR(255),
    primary_contact_phone VARCHAR(50),
    
    -- Configuration
    timezone VARCHAR(50) DEFAULT 'Africa/Johannesburg',
    currency VARCHAR(3) DEFAULT 'ZAR',
    language VARCHAR(10) DEFAULT 'en-ZA',
    
    -- Limits & Quotas
    max_transactions_per_second INT DEFAULT 1000,
    max_storage_gb INT DEFAULT 100,
    max_api_calls_per_month BIGINT DEFAULT 10000000,
    max_business_units INT DEFAULT 10,
    max_customers INT DEFAULT 100000,
    
    -- Billing
    billing_model VARCHAR(20) DEFAULT 'TRANSACTION',
    -- TRANSACTION, MONTHLY, ANNUAL
    billing_rate DECIMAL(10,4),
    billing_currency VARCHAR(3) DEFAULT 'ZAR',
    
    -- Metadata
    onboarded_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    activated_at TIMESTAMP,
    suspended_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    
    -- Compliance
    kyc_status VARCHAR(20) DEFAULT 'PENDING',
    kyc_verified_at TIMESTAMP,
    contract_signed BOOLEAN DEFAULT FALSE,
    contract_signed_at TIMESTAMP
);

CREATE INDEX idx_tenants_status ON tenants(status);
CREATE INDEX idx_tenants_country ON tenants(country_code);

-- 2. Business Units (Level 2)
CREATE TABLE business_units (
    bu_id VARCHAR(30) PRIMARY KEY,
    tenant_id VARCHAR(20) NOT NULL,
    bu_code VARCHAR(20) NOT NULL,
    bu_name VARCHAR(200) NOT NULL,
    bu_type VARCHAR(50) NOT NULL,
    -- RETAIL, CORPORATE, INVESTMENT, PRIVATE_BANKING, SME, WEALTH
    
    -- Status
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    
    -- Configuration
    timezone VARCHAR(50),
    default_currency VARCHAR(3),
    
    -- Limits (override tenant defaults)
    max_transaction_amount DECIMAL(18,2),
    daily_transaction_limit DECIMAL(18,2),
    monthly_transaction_limit DECIMAL(18,2),
    
    -- Metadata
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_bu_tenant FOREIGN KEY (tenant_id) 
        REFERENCES tenants(tenant_id) ON DELETE CASCADE,
    CONSTRAINT uk_bu_tenant_code UNIQUE (tenant_id, bu_code)
);

CREATE INDEX idx_bu_tenant ON business_units(tenant_id);
CREATE INDEX idx_bu_status ON business_units(tenant_id, status);

-- 3. Tenant Configurations
CREATE TABLE tenant_configs (
    config_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id VARCHAR(20) NOT NULL,
    business_unit_id VARCHAR(30),  -- NULL = tenant-level config
    
    config_key VARCHAR(200) NOT NULL,
    config_value TEXT NOT NULL,
    config_type VARCHAR(50) NOT NULL,
    -- LIMIT, FRAUD_RULE, NOTIFICATION, CLEARING, CORE_BANKING, BUSINESS_RULE
    
    -- Metadata
    description TEXT,
    is_sensitive BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    
    CONSTRAINT fk_config_tenant FOREIGN KEY (tenant_id) 
        REFERENCES tenants(tenant_id) ON DELETE CASCADE,
    CONSTRAINT fk_config_bu FOREIGN KEY (business_unit_id) 
        REFERENCES business_units(bu_id) ON DELETE CASCADE,
    CONSTRAINT uk_config_key UNIQUE (tenant_id, business_unit_id, config_key)
);

CREATE INDEX idx_config_tenant ON tenant_configs(tenant_id);
CREATE INDEX idx_config_type ON tenant_configs(tenant_id, config_type);

-- 4. Tenant Users (Admin users per tenant)
CREATE TABLE tenant_users (
    user_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id VARCHAR(20) NOT NULL,
    business_unit_id VARCHAR(30),  -- NULL = tenant admin
    
    username VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL,
    full_name VARCHAR(200),
    
    -- Role
    role VARCHAR(50) NOT NULL,
    -- TENANT_ADMIN, BU_ADMIN, OPERATIONS, COMPLIANCE, SUPPORT
    
    -- Status
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    -- ACTIVE, SUSPENDED, LOCKED
    
    -- Authentication
    password_hash VARCHAR(255),  -- If not using external IDP
    mfa_enabled BOOLEAN DEFAULT FALSE,
    mfa_secret VARCHAR(100),
    
    -- Session
    last_login_at TIMESTAMP,
    last_login_ip VARCHAR(50),
    failed_login_attempts INT DEFAULT 0,
    locked_until TIMESTAMP,
    
    -- Metadata
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_user_tenant FOREIGN KEY (tenant_id) 
        REFERENCES tenants(tenant_id) ON DELETE CASCADE,
    CONSTRAINT uk_user_email UNIQUE (email)
);

CREATE INDEX idx_user_tenant ON tenant_users(tenant_id);
CREATE INDEX idx_user_username ON tenant_users(username);

-- 5. Tenant API Keys (For programmatic access)
CREATE TABLE tenant_api_keys (
    api_key_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id VARCHAR(20) NOT NULL,
    business_unit_id VARCHAR(30),
    
    key_name VARCHAR(200) NOT NULL,
    api_key_hash VARCHAR(255) NOT NULL,  -- Hashed API key
    api_key_prefix VARCHAR(20) NOT NULL,  -- First 8 chars for identification
    
    -- Scope
    scopes TEXT[],  -- ['payment:create', 'payment:read', ...]
    
    -- Status
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    
    -- Limits
    rate_limit_per_second INT DEFAULT 100,
    
    -- Metadata
    expires_at TIMESTAMP,
    last_used_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    
    CONSTRAINT fk_apikey_tenant FOREIGN KEY (tenant_id) 
        REFERENCES tenants(tenant_id) ON DELETE CASCADE
);

CREATE INDEX idx_apikey_tenant ON tenant_api_keys(tenant_id);
CREATE INDEX idx_apikey_prefix ON tenant_api_keys(api_key_prefix);

-- 6. Tenant Metrics (Usage tracking)
CREATE TABLE tenant_metrics (
    metric_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id VARCHAR(20) NOT NULL,
    business_unit_id VARCHAR(30),
    
    metric_date DATE NOT NULL,
    
    -- Transaction metrics
    transaction_count BIGINT DEFAULT 0,
    transaction_volume DECIMAL(18,2) DEFAULT 0,
    transaction_fees DECIMAL(18,2) DEFAULT 0,
    
    -- API metrics
    api_calls BIGINT DEFAULT 0,
    api_errors BIGINT DEFAULT 0,
    
    -- Storage metrics
    storage_used_gb DECIMAL(10,2) DEFAULT 0,
    
    -- Performance metrics
    avg_response_time_ms INT,
    p95_response_time_ms INT,
    
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_metrics_tenant FOREIGN KEY (tenant_id) 
        REFERENCES tenants(tenant_id) ON DELETE CASCADE,
    CONSTRAINT uk_metrics_date UNIQUE (tenant_id, business_unit_id, metric_date)
);

CREATE INDEX idx_metrics_tenant_date ON tenant_metrics(tenant_id, metric_date DESC);

-- 7. Tenant Audit Log
CREATE TABLE tenant_audit_log (
    audit_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id VARCHAR(20) NOT NULL,
    
    event_type VARCHAR(100) NOT NULL,
    -- TENANT_CREATED, CONFIG_CHANGED, USER_ADDED, LIMIT_EXCEEDED, etc.
    
    entity_type VARCHAR(50),
    entity_id VARCHAR(100),
    
    action VARCHAR(50) NOT NULL,
    -- CREATE, UPDATE, DELETE, LOGIN, LOGOUT, etc.
    
    actor_type VARCHAR(50) NOT NULL,
    -- TENANT_ADMIN, PLATFORM_ADMIN, SYSTEM, API
    actor_id VARCHAR(100),
    
    details JSONB,
    ip_address VARCHAR(50),
    user_agent TEXT,
    
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_audit_tenant FOREIGN KEY (tenant_id) 
        REFERENCES tenants(tenant_id) ON DELETE CASCADE
);

CREATE INDEX idx_audit_tenant ON tenant_audit_log(tenant_id, created_at DESC);
CREATE INDEX idx_audit_event_type ON tenant_audit_log(event_type);
CREATE INDEX idx_audit_actor ON tenant_audit_log(actor_type, actor_id);

-- =============================================================================
-- SEED DATA - Sample Tenants
-- =============================================================================

INSERT INTO tenants (
    tenant_id, tenant_name, tenant_code, country_code, status,
    primary_contact_name, primary_contact_email,
    max_transactions_per_second, max_customers
) VALUES
    ('STD-001', 'Standard Bank SA', 'STDBANKSA', 'ZA', 'ACTIVE',
     'John Doe', 'john.doe@standardbank.co.za', 5000, 5000000),
    
    ('NED-001', 'Nedbank', 'NEDBANK', 'ZA', 'ACTIVE',
     'Jane Smith', 'jane.smith@nedbank.co.za', 3000, 3000000),
    
    ('ABSA-001', 'Absa Bank', 'ABSA', 'ZA', 'ACTIVE',
     'Bob Johnson', 'bob.johnson@absa.co.za', 4000, 4000000),
    
    ('FNB-001', 'First National Bank', 'FNB', 'ZA', 'ACTIVE',
     'Alice Williams', 'alice.williams@fnb.co.za', 6000, 6000000);

INSERT INTO business_units (
    bu_id, tenant_id, bu_code, bu_name, bu_type
) VALUES
    -- Standard Bank
    ('STD-001-RET', 'STD-001', 'RET', 'Retail Banking', 'RETAIL'),
    ('STD-001-CORP', 'STD-001', 'CORP', 'Corporate Banking', 'CORPORATE'),
    ('STD-001-INV', 'STD-001', 'INV', 'Investment Banking', 'INVESTMENT'),
    
    -- Nedbank
    ('NED-001-PER', 'NED-001', 'PER', 'Personal Banking', 'RETAIL'),
    ('NED-001-BUS', 'NED-001', 'BUS', 'Business Banking', 'CORPORATE'),
    
    -- Absa
    ('ABSA-001-RET', 'ABSA-001', 'RET', 'Retail', 'RETAIL'),
    ('ABSA-001-PRIV', 'ABSA-001', 'PRIV', 'Private Banking', 'PRIVATE_BANKING'),
    
    -- FNB
    ('FNB-001-RET', 'FNB-001', 'RET', 'Retail', 'RETAIL'),
    ('FNB-001-SME', 'FNB-001', 'SME', 'SME Banking', 'SME');

-- =============================================================================
-- ROW-LEVEL SECURITY POLICIES
-- =============================================================================

-- Enable RLS on all tenant tables
ALTER TABLE tenants ENABLE ROW LEVEL SECURITY;
ALTER TABLE business_units ENABLE ROW LEVEL SECURITY;
ALTER TABLE tenant_configs ENABLE ROW LEVEL SECURITY;
ALTER TABLE tenant_users ENABLE ROW LEVEL SECURITY;
ALTER TABLE tenant_api_keys ENABLE ROW LEVEL SECURITY;

-- Policy: Tenant can only see their own data
CREATE POLICY tenant_isolation_policy ON business_units
    USING (tenant_id = current_setting('app.current_tenant_id', true)::VARCHAR);

CREATE POLICY tenant_isolation_policy ON tenant_configs
    USING (tenant_id = current_setting('app.current_tenant_id', true)::VARCHAR);

CREATE POLICY tenant_isolation_policy ON tenant_users
    USING (tenant_id = current_setting('app.current_tenant_id', true)::VARCHAR);

CREATE POLICY tenant_isolation_policy ON tenant_api_keys
    USING (tenant_id = current_setting('app.current_tenant_id', true)::VARCHAR);

-- Platform admins can see all (bypass RLS)
-- Set: SET app.is_platform_admin = TRUE;

-- =============================================================================
-- FUNCTIONS
-- =============================================================================

-- Function: Get tenant hierarchy
CREATE OR REPLACE FUNCTION get_tenant_hierarchy(p_tenant_id VARCHAR)
RETURNS TABLE (
    tenant_id VARCHAR,
    tenant_name VARCHAR,
    bu_id VARCHAR,
    bu_name VARCHAR,
    customer_count BIGINT
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        t.tenant_id,
        t.tenant_name,
        bu.bu_id,
        bu.bu_name,
        COUNT(c.customer_id) as customer_count
    FROM tenants t
    LEFT JOIN business_units bu ON t.tenant_id = bu.tenant_id
    LEFT JOIN customers c ON bu.bu_id = c.business_unit_id
    WHERE t.tenant_id = p_tenant_id
    GROUP BY t.tenant_id, t.tenant_name, bu.bu_id, bu.bu_name;
END;
$$ LANGUAGE plpgsql;

-- Function: Validate tenant access
CREATE OR REPLACE FUNCTION validate_tenant_access(
    p_tenant_id VARCHAR,
    p_user_id UUID
) RETURNS BOOLEAN AS $$
DECLARE
    v_valid BOOLEAN;
BEGIN
    SELECT EXISTS (
        SELECT 1 FROM tenant_users
        WHERE tenant_id = p_tenant_id
          AND user_id = p_user_id
          AND status = 'ACTIVE'
    ) INTO v_valid;
    
    RETURN v_valid;
END;
$$ LANGUAGE plpgsql;

-- =============================================================================
-- VIEWS
-- =============================================================================

-- View: Tenant Summary
CREATE OR REPLACE VIEW v_tenant_summary AS
SELECT 
    t.tenant_id,
    t.tenant_name,
    t.status,
    t.country_code,
    COUNT(DISTINCT bu.bu_id) as business_unit_count,
    COUNT(DISTINCT c.customer_id) as total_customers,
    COALESCE(SUM(tm.transaction_count), 0) as total_transactions_today
FROM tenants t
LEFT JOIN business_units bu ON t.tenant_id = bu.tenant_id
LEFT JOIN customers c ON bu.bu_id = c.business_unit_id
LEFT JOIN tenant_metrics tm ON t.tenant_id = tm.tenant_id 
    AND tm.metric_date = CURRENT_DATE
GROUP BY t.tenant_id, t.tenant_name, t.status, t.country_code;
```

---

## Tenant Context Propagation

### Request Flow with Tenant Context

```
1. API Gateway
   ↓ Extract tenant_id from JWT
   ↓ Validate tenant status (ACTIVE)
   ↓ Add X-Tenant-ID header
   ↓
2. Microservice
   ↓ TenantContextFilter extracts X-Tenant-ID
   ↓ Set ThreadLocal TenantContext
   ↓ Set PostgreSQL session variable
   ↓
3. Database Query
   ↓ Row-Level Security filters by tenant_id
   ↓ Only tenant's data returned
```

### Java Implementation

#### TenantContext (Thread-Local)

```java
/**
 * Thread-local tenant context
 */
public class TenantContext {
    
    private static final ThreadLocal<String> TENANT_ID = new ThreadLocal<>();
    private static final ThreadLocal<String> BUSINESS_UNIT_ID = new ThreadLocal<>();
    private static final ThreadLocal<String> TENANT_NAME = new ThreadLocal<>();
    
    public static void setTenantId(String tenantId) {
        TENANT_ID.set(tenantId);
    }
    
    public static String getTenantId() {
        String tenantId = TENANT_ID.get();
        if (tenantId == null) {
            throw new TenantContextException("Tenant context not set");
        }
        return tenantId;
    }
    
    public static void setBusinessUnitId(String businessUnitId) {
        BUSINESS_UNIT_ID.set(businessUnitId);
    }
    
    public static String getBusinessUnitId() {
        return BUSINESS_UNIT_ID.get();
    }
    
    public static void setTenantName(String tenantName) {
        TENANT_NAME.set(tenantName);
    }
    
    public static String getTenantName() {
        return TENANT_NAME.get();
    }
    
    public static void clear() {
        TENANT_ID.remove();
        BUSINESS_UNIT_ID.remove();
        TENANT_NAME.remove();
    }
    
    public static boolean isSet() {
        return TENANT_ID.get() != null;
    }
}
```

#### Tenant Filter (Spring Boot)

```java
@Component
@Order(1)
public class TenantContextFilter extends OncePerRequestFilter {
    
    private static final String TENANT_HEADER = "X-Tenant-ID";
    private static final String BUSINESS_UNIT_HEADER = "X-Business-Unit-ID";
    
    @Autowired
    private TenantRepository tenantRepository;
    
    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException {
        
        try {
            // Extract tenant ID from header
            String tenantId = request.getHeader(TENANT_HEADER);
            String businessUnitId = request.getHeader(BUSINESS_UNIT_HEADER);
            
            if (tenantId == null || tenantId.isBlank()) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, 
                    "Missing X-Tenant-ID header");
                return;
            }
            
            // Validate tenant exists and is active
            Optional<Tenant> tenant = tenantRepository.findById(tenantId);
            if (tenant.isEmpty()) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, 
                    "Invalid tenant ID");
                return;
            }
            
            if (!"ACTIVE".equals(tenant.get().getStatus())) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, 
                    "Tenant is not active");
                return;
            }
            
            // Set tenant context
            TenantContext.setTenantId(tenantId);
            TenantContext.setTenantName(tenant.get().getTenantName());
            
            if (businessUnitId != null && !businessUnitId.isBlank()) {
                TenantContext.setBusinessUnitId(businessUnitId);
            }
            
            // Continue filter chain
            filterChain.doFilter(request, response);
            
        } finally {
            // Clear context after request
            TenantContext.clear();
        }
    }
    
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // Skip for health checks and public endpoints
        String path = request.getRequestURI();
        return path.startsWith("/actuator/") || 
               path.startsWith("/public/") ||
               path.equals("/health");
    }
}
```

#### JPA Interceptor (Auto-add tenant_id)

```java
@Component
public class TenantJpaInterceptor extends EmptyInterceptor {
    
    @Override
    public boolean onSave(
        Object entity,
        Serializable id,
        Object[] state,
        String[] propertyNames,
        Type[] types
    ) {
        if (entity instanceof TenantAware) {
            TenantAware tenantEntity = (TenantAware) entity;
            
            // Auto-set tenant_id if not already set
            if (tenantEntity.getTenantId() == null) {
                tenantEntity.setTenantId(TenantContext.getTenantId());
                
                // Update state array
                for (int i = 0; i < propertyNames.length; i++) {
                    if ("tenantId".equals(propertyNames[i])) {
                        state[i] = TenantContext.getTenantId();
                        return true;
                    }
                }
            }
        }
        return false;
    }
    
    @Override
    public String onPrepareStatement(String sql) {
        // Optionally inject tenant_id into WHERE clause
        // For extra safety (in addition to RLS)
        return sql;
    }
}

/**
 * Marker interface for tenant-aware entities
 */
public interface TenantAware {
    String getTenantId();
    void setTenantId(String tenantId);
}
```

#### Database Session Variable (PostgreSQL RLS)

```java
@Aspect
@Component
public class TenantDataSourceAspect {
    
    @Autowired
    private EntityManager entityManager;
    
    @Around("@annotation(org.springframework.transaction.annotation.Transactional)")
    public Object setTenantContext(ProceedingJoinPoint joinPoint) throws Throwable {
        
        if (TenantContext.isSet()) {
            // Set PostgreSQL session variable for RLS
            String sql = "SET LOCAL app.current_tenant_id = :tenantId";
            entityManager.createNativeQuery(sql)
                .setParameter("tenantId", TenantContext.getTenantId())
                .executeUpdate();
        }
        
        return joinPoint.proceed();
    }
}
```

---

## Tenant-Specific Microservice Changes

### API Gateway (Tenant Routing)

```yaml
# Kong API Gateway Configuration
routes:
  - name: payment-initiation
    paths:
      - /api/v1/payments
    plugins:
      # 1. JWT Authentication (extracts tenant_id from token)
      - name: jwt
        config:
          claims_to_verify:
            - tenant_id
            - business_unit_id
      
      # 2. Tenant Validation
      - name: request-transformer
        config:
          add:
            headers:
              - X-Tenant-ID:$(jwt.tenant_id)
              - X-Business-Unit-ID:$(jwt.business_unit_id)
      
      # 3. Rate Limiting (per tenant)
      - name: rate-limiting
        config:
          policy: redis
          limit_by: header
          header_name: X-Tenant-ID
          second: 1000  # Max 1000 req/sec per tenant
          
      # 4. Tenant-Specific Routing
      - name: upstream-url
        config:
          url: http://payment-service.$(tenant_id).svc.cluster.local
```

### JWT Token Structure

```json
{
  "sub": "customer_id:C-12345",
  "tenant_id": "STD-001",
  "tenant_name": "Standard Bank SA",
  "business_unit_id": "STD-001-RET",
  "business_unit_name": "Retail Banking",
  "customer_id": "C-12345",
  "roles": ["customer", "payment_initiator"],
  "permissions": ["payment:create", "payment:read", "account:read"],
  "iat": 1696867200,
  "exp": 1696870800
}
```

---

## Tenant Configuration Service

### New Microservice: Tenant Configuration Service

```java
@RestController
@RequestMapping("/api/v1/tenant-config")
public class TenantConfigController {
    
    @Autowired
    private TenantConfigService tenantConfigService;
    
    /**
     * Get tenant configuration
     */
    @GetMapping("/{configKey}")
    public ResponseEntity<TenantConfigResponse> getConfig(
        @PathVariable String configKey
    ) {
        String tenantId = TenantContext.getTenantId();
        String businessUnitId = TenantContext.getBusinessUnitId();
        
        // Lookup: Business Unit config → Tenant config → Default
        TenantConfig config = tenantConfigService.getConfig(
            tenantId, businessUnitId, configKey
        );
        
        return ResponseEntity.ok(TenantConfigResponse.from(config));
    }
    
    /**
     * Get all configs for tenant
     */
    @GetMapping
    public ResponseEntity<List<TenantConfigResponse>> getAllConfigs(
        @RequestParam(required = false) String type
    ) {
        String tenantId = TenantContext.getTenantId();
        
        List<TenantConfig> configs = type != null
            ? tenantConfigService.getConfigsByType(tenantId, type)
            : tenantConfigService.getAllConfigs(tenantId);
        
        return ResponseEntity.ok(
            configs.stream()
                .map(TenantConfigResponse::from)
                .collect(Collectors.toList())
        );
    }
    
    /**
     * Update configuration (tenant admin only)
     */
    @PutMapping("/{configKey}")
    @PreAuthorize("hasRole('TENANT_ADMIN')")
    public ResponseEntity<TenantConfigResponse> updateConfig(
        @PathVariable String configKey,
        @RequestBody UpdateConfigRequest request
    ) {
        String tenantId = TenantContext.getTenantId();
        String businessUnitId = request.getBusinessUnitId();
        
        TenantConfig updated = tenantConfigService.updateConfig(
            tenantId,
            businessUnitId,
            configKey,
            request.getConfigValue(),
            request.getDescription()
        );
        
        return ResponseEntity.ok(TenantConfigResponse.from(updated));
    }
}

@Service
public class TenantConfigService {
    
    @Autowired
    private TenantConfigRepository configRepository;
    
    @Cacheable(value = "tenant-config", key = "#tenantId + ':' + #businessUnitId + ':' + #configKey")
    public TenantConfig getConfig(String tenantId, String businessUnitId, String configKey) {
        
        // 1. Check business unit config
        if (businessUnitId != null) {
            Optional<TenantConfig> buConfig = configRepository.findByTenantAndBusinessUnitAndKey(
                tenantId, businessUnitId, configKey
            );
            if (buConfig.isPresent()) {
                return buConfig.get();
            }
        }
        
        // 2. Check tenant-level config
        Optional<TenantConfig> tenantConfig = configRepository.findByTenantAndKey(
            tenantId, null, configKey
        );
        if (tenantConfig.isPresent()) {
            return tenantConfig.get();
        }
        
        // 3. Return platform default
        return getDefaultConfig(configKey);
    }
}
```

---

## Updating Existing Tables

### Add tenant_id to All Tables

```sql
-- Example: payments table
ALTER TABLE payments ADD COLUMN tenant_id VARCHAR(20);
ALTER TABLE payments ADD COLUMN business_unit_id VARCHAR(30);

UPDATE payments p
SET tenant_id = c.tenant_id,
    business_unit_id = c.business_unit_id
FROM customers c
WHERE p.customer_id = c.customer_id;

ALTER TABLE payments ALTER COLUMN tenant_id SET NOT NULL;
ALTER TABLE payments ALTER COLUMN business_unit_id SET NOT NULL;

-- Add foreign keys
ALTER TABLE payments 
    ADD CONSTRAINT fk_payment_tenant 
    FOREIGN KEY (tenant_id) REFERENCES tenants(tenant_id);

ALTER TABLE payments 
    ADD CONSTRAINT fk_payment_bu 
    FOREIGN KEY (business_unit_id) REFERENCES business_units(bu_id);

-- Add indexes
CREATE INDEX idx_payments_tenant ON payments(tenant_id);
CREATE INDEX idx_payments_tenant_bu ON payments(tenant_id, business_unit_id);

-- Enable RLS
ALTER TABLE payments ENABLE ROW LEVEL SECURITY;

CREATE POLICY tenant_isolation_policy ON payments
    USING (tenant_id = current_setting('app.current_tenant_id', true)::VARCHAR);
```

### Migration Script for All Tables

```sql
DO $$
DECLARE
    table_name TEXT;
    tables TEXT[] := ARRAY[
        'payments', 'transactions', 'customers', 'accounts',
        'validation_rules', 'fraud_detection_log', 'customer_limits',
        'payment_type_limits', 'audit_log', 'notifications'
    ];
BEGIN
    FOREACH table_name IN ARRAY tables
    LOOP
        -- Add tenant_id column
        EXECUTE format('ALTER TABLE %I ADD COLUMN IF NOT EXISTS tenant_id VARCHAR(20)', table_name);
        EXECUTE format('ALTER TABLE %I ADD COLUMN IF NOT EXISTS business_unit_id VARCHAR(30)', table_name);
        
        -- Add indexes
        EXECUTE format('CREATE INDEX IF NOT EXISTS idx_%I_tenant ON %I(tenant_id)', table_name, table_name);
        
        -- Enable RLS
        EXECUTE format('ALTER TABLE %I ENABLE ROW LEVEL SECURITY', table_name);
        
        -- Create policy
        EXECUTE format('
            CREATE POLICY tenant_isolation_policy ON %I
            USING (tenant_id = current_setting(''app.current_tenant_id'', true)::VARCHAR)
        ', table_name);
        
        RAISE NOTICE 'Updated table: %', table_name;
    END LOOP;
END $$;
```

---

## Tenant Onboarding Process

### Automated Onboarding API

```java
@RestController
@RequestMapping("/api/v1/platform/tenants")
@PreAuthorize("hasRole('PLATFORM_ADMIN')")
public class TenantOnboardingController {
    
    @Autowired
    private TenantOnboardingService onboardingService;
    
    /**
     * Onboard new tenant
     */
    @PostMapping
    public ResponseEntity<TenantOnboardingResponse> onboardTenant(
        @RequestBody @Valid TenantOnboardingRequest request
    ) {
        TenantOnboardingResponse response = onboardingService.onboardTenant(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}

@Service
@Slf4j
public class TenantOnboardingService {
    
    @Autowired
    private TenantRepository tenantRepository;
    
    @Autowired
    private BusinessUnitRepository businessUnitRepository;
    
    @Autowired
    private TenantConfigService configService;
    
    @Autowired
    private KafkaTopicProvisioner kafkaProvisioner;
    
    @Autowired
    private RedisNamespaceProvisioner redisProvisioner;
    
    @Transactional
    public TenantOnboardingResponse onboardTenant(TenantOnboardingRequest request) {
        
        log.info("Starting tenant onboarding: {}", request.getTenantName());
        
        // 1. Create tenant record
        Tenant tenant = Tenant.builder()
            .tenantId(generateTenantId(request.getTenantCode()))
            .tenantName(request.getTenantName())
            .tenantCode(request.getTenantCode())
            .countryCode(request.getCountryCode())
            .status("PENDING")
            .primaryContactEmail(request.getContactEmail())
            .build();
        
        tenant = tenantRepository.save(tenant);
        log.info("Created tenant: {}", tenant.getTenantId());
        
        // 2. Create business units
        List<BusinessUnit> businessUnits = request.getBusinessUnits().stream()
            .map(buRequest -> BusinessUnit.builder()
                .buId(generateBusinessUnitId(tenant.getTenantId(), buRequest.getCode()))
                .tenantId(tenant.getTenantId())
                .buCode(buRequest.getCode())
                .buName(buRequest.getName())
                .buType(buRequest.getType())
                .status("ACTIVE")
                .build())
            .collect(Collectors.toList());
        
        businessUnits = businessUnitRepository.saveAll(businessUnits);
        log.info("Created {} business units", businessUnits.size());
        
        // 3. Provision Kafka topics (tenant-specific)
        kafkaProvisioner.provisionTopicsForTenant(tenant.getTenantId());
        log.info("Provisioned Kafka topics");
        
        // 4. Provision Redis namespace
        redisProvisioner.provisionNamespaceForTenant(tenant.getTenantId());
        log.info("Provisioned Redis namespace");
        
        // 5. Set default configurations
        configService.setDefaultConfigs(tenant.getTenantId(), request.getConfigs());
        log.info("Set default configurations");
        
        // 6. Create first admin user
        createAdminUser(tenant, request.getAdminEmail(), request.getAdminName());
        log.info("Created admin user");
        
        // 7. Activate tenant
        tenant.setStatus("ACTIVE");
        tenant.setActivatedAt(Instant.now());
        tenantRepository.save(tenant);
        log.info("Activated tenant");
        
        // 8. Send welcome email
        sendWelcomeEmail(tenant, request.getAdminEmail());
        
        log.info("Tenant onboarding completed: {}", tenant.getTenantId());
        
        return TenantOnboardingResponse.builder()
            .tenantId(tenant.getTenantId())
            .tenantName(tenant.getTenantName())
            .status("ACTIVE")
            .businessUnits(businessUnits.stream()
                .map(bu -> new BusinessUnitInfo(bu.getBuId(), bu.getBuName()))
                .collect(Collectors.toList()))
            .apiEndpoint("https://api.payments.example.com")
            .documentationUrl("https://docs.payments.example.com")
            .build();
    }
    
    private String generateTenantId(String tenantCode) {
        return tenantCode.toUpperCase() + "-001";
    }
    
    private String generateBusinessUnitId(String tenantId, String buCode) {
        return tenantId + "-" + buCode.toUpperCase();
    }
}
```

---

## Event Schema Updates

### Add Tenant Context to All Events

```yaml
# AsyncAPI Event Schema
components:
  schemas:
    TenantContext:
      type: object
      required:
        - tenant_id
        - tenant_name
      properties:
        tenant_id:
          type: string
          description: Unique tenant identifier
          example: "STD-001"
        tenant_name:
          type: string
          description: Tenant name
          example: "Standard Bank SA"
        business_unit_id:
          type: string
          description: Business unit identifier
          example: "STD-001-RET"
        business_unit_name:
          type: string
          description: Business unit name
          example: "Retail Banking"

    PaymentInitiatedPayload:
      type: object
      required:
        - tenant_context  # NEW
        - payment_id
        - customer_id
        - amount
      properties:
        tenant_context:  # NEW
          $ref: '#/components/schemas/TenantContext'
        payment_id:
          type: string
        customer_id:
          type: string
        amount:
          type: number
        # ... other fields
```

### Kafka Topic Per Tenant (Optional)

```yaml
# Option 1: Shared topics with tenant_id in message key
topic: payment.initiated
key: "STD-001:PAY-12345"  # tenant_id:payment_id
partition_strategy: hash(key)  # Ensures same tenant goes to same partition

# Option 2: Tenant-specific topics (for high-volume tenants)
topics:
  - payment.initiated.STD-001  # Standard Bank
  - payment.initiated.NED-001  # Nedbank
  - payment.initiated.common   # Low-volume tenants
```

---

## Tenant Monitoring Dashboard

### Metrics Per Tenant

```java
@Component
public class TenantMetricsCollector {
    
    @Autowired
    private MeterRegistry meterRegistry;
    
    public void recordTransaction(String tenantId, BigDecimal amount) {
        Counter.builder("tenant.transactions")
            .tag("tenant_id", tenantId)
            .register(meterRegistry)
            .increment();
        
        DistributionSummary.builder("tenant.transaction.amount")
            .tag("tenant_id", tenantId)
            .baseUnit("ZAR")
            .register(meterRegistry)
            .record(amount.doubleValue());
    }
    
    public void recordApiCall(String tenantId, String endpoint, long duration) {
        Timer.builder("tenant.api.duration")
            .tag("tenant_id", tenantId)
            .tag("endpoint", endpoint)
            .register(meterRegistry)
            .record(duration, TimeUnit.MILLISECONDS);
    }
}
```

### Grafana Dashboard per Tenant

```
Tenant Dashboard: Standard Bank SA (STD-001)
├── Transactions Today: 125,456
├── Transaction Volume: R 4.2B
├── API Calls: 2.1M
├── Average Response Time: 85ms
├── Error Rate: 0.02%
│
├── Business Units
│   ├── Retail Banking: 85,000 txn
│   ├── Corporate Banking: 35,000 txn
│   └── Investment Banking: 5,456 txn
│
└── Alerts
    ├── ⚠️ API rate approaching limit (85% of 5000 req/sec)
    └── ✅ All systems operational
```

---

## Testing Multi-Tenancy

### Integration Test

```java
@SpringBootTest
@AutoConfigureMockMvc
class TenantIsolationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    void shouldIsolateTenantData() throws Exception {
        // Tenant 1: Standard Bank
        String tenant1Token = generateJwtToken("STD-001", "C-001");
        
        // Tenant 2: Nedbank
        String tenant2Token = generateJwtToken("NED-001", "C-002");
        
        // Create payment for Tenant 1
        mockMvc.perform(post("/api/v1/payments")
                .header("Authorization", "Bearer " + tenant1Token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"amount\": 100}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.payment_id").value("PAY-001"));
        
        // Tenant 2 should NOT see Tenant 1's payment
        mockMvc.perform(get("/api/v1/payments/PAY-001")
                .header("Authorization", "Bearer " + tenant2Token))
            .andExpect(status().isNotFound());
        
        // Tenant 1 should see their own payment
        mockMvc.perform(get("/api/v1/payments/PAY-001")
                .header("Authorization", "Bearer " + tenant1Token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.payment_id").value("PAY-001"));
    }
}
```

---

## Summary

### Multi-Tenancy Features

✅ **3-Level Hierarchy**: Tenant → Business Unit → Customer  
✅ **Data Isolation**: PostgreSQL Row-Level Security + App-level filtering  
✅ **Tenant Context**: Thread-local context + HTTP headers  
✅ **Configuration**: Tenant-specific configs with business unit overrides  
✅ **Onboarding**: Automated tenant provisioning API  
✅ **Security**: JWT with tenant claims, API key per tenant  
✅ **Monitoring**: Per-tenant metrics and dashboards  
✅ **Events**: Tenant context in all events  
✅ **Performance**: Resource quotas and rate limiting per tenant  

### Database Changes

- ✅ Added 7 new tables for tenant management
- ✅ Added `tenant_id` and `business_unit_id` to all existing tables
- ✅ Enabled PostgreSQL Row-Level Security on all tables
- ✅ Created tenant isolation policies
- ✅ Added tenant hierarchy views and functions

### Microservice Changes

- ✅ TenantContextFilter for request interception
- ✅ TenantContext (ThreadLocal) for context propagation
- ✅ JPA interceptor for auto-setting tenant_id
- ✅ New Tenant Configuration Service
- ✅ Tenant onboarding API

---

## Related Documents

- **[01-ASSUMPTIONS.md](01-ASSUMPTIONS.md)** - Section 2.4 (Multi-Tenancy)
- **[05-DATABASE-SCHEMAS.md](05-DATABASE-SCHEMAS.md)** - Tenant tables
- **[02-MICROSERVICES-BREAKDOWN.md](02-MICROSERVICES-BREAKDOWN.md)** - Updated services
- **[03-EVENT-SCHEMAS.md](03-EVENT-SCHEMAS.md)** - Tenant context in events

---

**Last Updated**: 2025-10-11  
**Version**: 1.0
