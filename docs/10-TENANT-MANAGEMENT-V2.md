# Tenant Management v2 - Enhanced Multi-Tenant Architecture

## 🎯 **Multi-Tenant Architecture Overview**

The Payments Engine v2 implements a comprehensive multi-tenant architecture with 3-level hierarchy, Row-Level Security (RLS), and ISO 20022 compliance.

## 🏗️ **Tenant Hierarchy**

### **3-Level Hierarchy**
```
Tenant (Top Level)
├── Business Unit 1
│   ├── Customer 1
│   ├── Customer 2
│   └── Customer N
├── Business Unit 2
│   ├── Customer 1
│   └── Customer N
└── Business Unit N
    └── Customer N
```

### **Tenant Types**
```yaml
Enterprise Tenants:
  - Large financial institutions
  - Multiple business units
  - Complex hierarchies
  - Custom ISO 20022 configurations

SME Tenants:
  - Small to medium enterprises
  - Single business unit
  - Simple hierarchies
  - Standard ISO 20022 configurations

Partner Tenants:
  - Third-party integrators
  - API-only access
  - Limited functionality
  - Partner-specific ISO 20022 formats
```

## 🗃️ **Database Schema Design**

### **PostgreSQL Schema with RLS**

#### **Tenant Tables**
```sql
-- Core tenant table
CREATE TABLE tenants (
  tenant_id VARCHAR(50) PRIMARY KEY,
  tenant_name VARCHAR(100) NOT NULL,
  tenant_type VARCHAR(20) NOT NULL, -- ENTERPRISE, SME, PARTNER
  status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
  iso20022_config JSONB,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Business units within tenants
CREATE TABLE business_units (
  bu_id VARCHAR(50) PRIMARY KEY,
  tenant_id VARCHAR(50) NOT NULL REFERENCES tenants(tenant_id),
  bu_name VARCHAR(100) NOT NULL,
  bu_type VARCHAR(20) NOT NULL, -- RETAIL, CORPORATE, INVESTMENT
  iso20022_config JSONB,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Customers within business units
CREATE TABLE customers (
  customer_id VARCHAR(50) PRIMARY KEY,
  bu_id VARCHAR(50) NOT NULL REFERENCES business_units(bu_id),
  customer_name VARCHAR(100) NOT NULL,
  customer_type VARCHAR(20) NOT NULL, -- INDIVIDUAL, CORPORATE
  iso20022_config JSONB,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tenant-specific configurations
CREATE TABLE tenant_configurations (
  config_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  tenant_id VARCHAR(50) NOT NULL REFERENCES tenants(tenant_id),
  config_key VARCHAR(100) NOT NULL,
  config_value JSONB NOT NULL,
  config_type VARCHAR(20) NOT NULL, -- ISO20022, LIMITS, FRAUD, ROUTING
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  UNIQUE(tenant_id, config_key)
);

-- Tenant users and API keys
CREATE TABLE tenant_users (
  user_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  tenant_id VARCHAR(50) NOT NULL REFERENCES tenants(tenant_id),
  username VARCHAR(100) NOT NULL,
  email VARCHAR(100) NOT NULL,
  role VARCHAR(20) NOT NULL, -- ADMIN, USER, VIEWER
  status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  UNIQUE(tenant_id, username)
);

CREATE TABLE tenant_api_keys (
  key_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  tenant_id VARCHAR(50) NOT NULL REFERENCES tenants(tenant_id),
  key_name VARCHAR(100) NOT NULL,
  api_key VARCHAR(100) NOT NULL,
  permissions JSONB NOT NULL,
  expires_at TIMESTAMP,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  UNIQUE(api_key)
);
```

#### **Row-Level Security (RLS)**
```sql
-- Enable RLS on all tenant-related tables
ALTER TABLE tenants ENABLE ROW LEVEL SECURITY;
ALTER TABLE business_units ENABLE ROW LEVEL SECURITY;
ALTER TABLE customers ENABLE ROW LEVEL SECURITY;
ALTER TABLE tenant_configurations ENABLE ROW LEVEL SECURITY;
ALTER TABLE tenant_users ENABLE ROW LEVEL SECURITY;
ALTER TABLE tenant_api_keys ENABLE ROW LEVEL SECURITY;

-- RLS policies for tenant isolation
CREATE POLICY tenant_isolation_tenants ON tenants
  FOR ALL TO application_role
  USING (tenant_id = current_setting('app.current_tenant_id'));

CREATE POLICY tenant_isolation_business_units ON business_units
  FOR ALL TO application_role
  USING (tenant_id = current_setting('app.current_tenant_id'));

CREATE POLICY tenant_isolation_customers ON customers
  FOR ALL TO application_role
  USING (bu_id IN (
    SELECT bu_id FROM business_units 
    WHERE tenant_id = current_setting('app.current_tenant_id')
  ));

CREATE POLICY tenant_isolation_configurations ON tenant_configurations
  FOR ALL TO application_role
  USING (tenant_id = current_setting('app.current_tenant_id'));

CREATE POLICY tenant_isolation_users ON tenant_users
  FOR ALL TO application_role
  USING (tenant_id = current_setting('app.current_tenant_id'));

CREATE POLICY tenant_isolation_api_keys ON tenant_api_keys
  FOR ALL TO application_role
  USING (tenant_id = current_setting('app.current_tenant_id'));
```

### **Cassandra Schema for Multi-Tenant ISO 20022 Messages**

#### **Tenant-Specific Message Storage**
```sql
-- Tenant-specific ISO 20022 message storage
CREATE TABLE tenant_iso20022_messages (
  tenant_id TEXT,
  uetr UUID,
  message_id TEXT,
  message_type TEXT,
  raw_xml TEXT,
  parsed_json TEXT,
  validation_status TEXT,
  processing_status TEXT,
  created_at TIMESTAMP,
  PRIMARY KEY (tenant_id, uetr, message_id)
) WITH CLUSTERING ORDER BY (created_at DESC);

-- Tenant-specific message correlation
CREATE TABLE tenant_uetr_correlation (
  tenant_id TEXT,
  uetr UUID,
  payment_id UUID,
  pain001_id TEXT,
  pacs008_id TEXT,
  pacs002_id TEXT,
  pacs004_id TEXT,
  camt054_id TEXT,
  correlation_status TEXT,
  created_at TIMESTAMP,
  PRIMARY KEY (tenant_id, uetr)
);
```

## 🔧 **Tenant Management Service**

### **Service Architecture**
```yaml
Service Name: Tenant Management Service
Phase: Phase 3 (Platform Services)
AI Agent: TenantManagementAgent
Duration: 3-4 days
Database: PostgreSQL with RLS
Cache: Redis for tenant configs
```

### **API Endpoints**

#### **Tenant Management (Platform Admin)**
```java
@RestController
@RequestMapping("/api/v1/platform/tenants")
@PreAuthorize("hasRole('PLATFORM_ADMIN')")
public class TenantManagementController {
    
    @PostMapping
    public ResponseEntity<TenantResponse> onboardTenant(
        @RequestBody @Valid TenantOnboardingRequest request
    ) {
        // Onboard new tenant with ISO 20022 configuration
        TenantResponse response = tenantOnboardingService.onboardTenant(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @GetMapping
    public ResponseEntity<Page<TenantResponse>> listTenants(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        Page<TenantResponse> tenants = tenantService.listTenants(page, size);
        return ResponseEntity.ok(tenants);
    }
    
    @GetMapping("/{tenantId}")
    public ResponseEntity<TenantResponse> getTenant(@PathVariable String tenantId) {
        TenantResponse tenant = tenantService.getTenant(tenantId);
        return ResponseEntity.ok(tenant);
    }
    
    @PutMapping("/{tenantId}")
    public ResponseEntity<TenantResponse> updateTenant(
        @PathVariable String tenantId,
        @RequestBody @Valid TenantUpdateRequest request
    ) {
        TenantResponse tenant = tenantService.updateTenant(tenantId, request);
        return ResponseEntity.ok(tenant);
    }
    
    @DeleteMapping("/{tenantId}")
    public ResponseEntity<Void> deactivateTenant(@PathVariable String tenantId) {
        tenantService.deactivateTenant(tenantId);
        return ResponseEntity.noContent().build();
    }
}
```

#### **Business Unit Management (Tenant Admin)**
```java
@RestController
@RequestMapping("/api/v1/tenant/business-units")
@PreAuthorize("hasRole('TENANT_ADMIN')")
public class BusinessUnitController {
    
    @PostMapping
    public ResponseEntity<BusinessUnitResponse> createBusinessUnit(
        @RequestBody @Valid BusinessUnitRequest request
    ) {
        BusinessUnitResponse bu = businessUnitService.createBusinessUnit(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(bu);
    }
    
    @GetMapping
    public ResponseEntity<List<BusinessUnitResponse>> listBusinessUnits() {
        List<BusinessUnitResponse> businessUnits = businessUnitService.listBusinessUnits();
        return ResponseEntity.ok(businessUnits);
    }
    
    @GetMapping("/{buId}")
    public ResponseEntity<BusinessUnitResponse> getBusinessUnit(@PathVariable String buId) {
        BusinessUnitResponse bu = businessUnitService.getBusinessUnit(buId);
        return ResponseEntity.ok(bu);
    }
}
```

#### **Tenant Configuration Management**
```java
@RestController
@RequestMapping("/api/v1/tenant/config")
@PreAuthorize("hasRole('TENANT_ADMIN')")
public class TenantConfigurationController {
    
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllConfigurations() {
        Map<String, Object> configs = tenantConfigService.getAllConfigurations();
        return ResponseEntity.ok(configs);
    }
    
    @GetMapping("/{key}")
    public ResponseEntity<Object> getConfiguration(@PathVariable String key) {
        Object config = tenantConfigService.getConfiguration(key);
        return ResponseEntity.ok(config);
    }
    
    @PutMapping("/{key}")
    public ResponseEntity<Object> updateConfiguration(
        @PathVariable String key,
        @RequestBody Object value
    ) {
        Object config = tenantConfigService.updateConfiguration(key, value);
        return ResponseEntity.ok(config);
    }
    
    @DeleteMapping("/{key}")
    public ResponseEntity<Void> deleteConfiguration(@PathVariable String key) {
        tenantConfigService.deleteConfiguration(key);
        return ResponseEntity.noContent().build();
    }
}
```

#### **Internal Tenant Lookup (Used by All Services)**
```java
@RestController
@RequestMapping("/api/internal/v1/tenant")
public class InternalTenantController {
    
    @GetMapping("/lookup/{tenantId}")
    public ResponseEntity<TenantInfo> getTenantInfo(@PathVariable String tenantId) {
        TenantInfo tenantInfo = tenantService.getTenantInfo(tenantId);
        return ResponseEntity.ok(tenantInfo);
    }
    
    @GetMapping("/validate/{tenantId}")
    public ResponseEntity<Boolean> validateTenant(@PathVariable String tenantId) {
        boolean isValid = tenantService.validateTenant(tenantId);
        return ResponseEntity.ok(isValid);
    }
    
    @GetMapping("/config/{tenantId}/{key}")
    public ResponseEntity<Object> getTenantConfig(
        @PathVariable String tenantId,
        @PathVariable String key
    ) {
        Object config = tenantService.getTenantConfig(tenantId, key);
        return ResponseEntity.ok(config);
    }
}
```

## 🔒 **Tenant Context Propagation**

### **Tenant Context Filter**
```java
@Component
public class TenantContextFilter implements Filter {
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, 
                        FilterChain filterChain) throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        try {
            // Extract tenant ID from header
            String tenantId = httpRequest.getHeader("X-Tenant-ID");
            if (tenantId == null) {
                httpResponse.sendError(HttpServletResponse.SC_BAD_REQUEST, 
                    "Missing X-Tenant-ID header");
                return;
            }
            
            // Validate tenant
            Tenant tenant = tenantService.validateTenant(tenantId);
            if (tenant == null) {
                httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, 
                    "Invalid tenant ID");
                return;
            }
            
            // Set ThreadLocal context
            TenantContext.setTenantId(tenantId);
            TenantContext.setTenantName(tenant.getTenantName());
            TenantContext.setTenantType(tenant.getTenantType());
            
            // Set PostgreSQL session variable for RLS
            Connection connection = dataSource.getConnection();
            try (PreparedStatement stmt = connection.prepareStatement(
                "SET app.current_tenant_id = ?")) {
                stmt.setString(1, tenantId);
                stmt.execute();
            }
            
            // Continue
            filterChain.doFilter(request, response);
            
        } finally {
            TenantContext.clear();
        }
    }
}
```

### **Tenant Context Class**
```java
public class TenantContext {
    private static final ThreadLocal<String> TENANT_ID = new ThreadLocal<>();
    private static final ThreadLocal<String> TENANT_NAME = new ThreadLocal<>();
    private static final ThreadLocal<String> TENANT_TYPE = new ThreadLocal<>();
    
    public static void setTenantId(String tenantId) {
        TENANT_ID.set(tenantId);
    }
    
    public static String getTenantId() {
        return TENANT_ID.get();
    }
    
    public static void setTenantName(String tenantName) {
        TENANT_NAME.set(tenantName);
    }
    
    public static String getTenantName() {
        return TENANT_NAME.get();
    }
    
    public static void setTenantType(String tenantType) {
        TENANT_TYPE.set(tenantType);
    }
    
    public static String getTenantType() {
        return TENANT_TYPE.get();
    }
    
    public static void clear() {
        TENANT_ID.remove();
        TENANT_NAME.remove();
        TENANT_TYPE.remove();
    }
}
```

## 🎯 **ISO 20022 Tenant Configuration**

### **Tenant-Specific ISO 20022 Settings**
```yaml
# Enterprise Tenant ISO 20022 Configuration
enterprise_tenant:
  iso20022:
    message_formats:
      - pain.001.001.10 (XML)
      - pain.001.001.10 (JSON)
      - pain.002.001.10 (XML)
      - pain.002.001.10 (JSON)
    validation:
      xsd_validation: true
      json_schema_validation: true
      business_rule_validation: true
    correlation:
      uetr_generation: true
      message_correlation: true
      audit_trail: true
    limits:
      max_message_size: 10MB
      max_messages_per_second: 1000
      max_concurrent_requests: 100

# SME Tenant ISO 20022 Configuration
sme_tenant:
  iso20022:
    message_formats:
      - pain.001.001.10 (XML)
      - pain.002.001.10 (XML)
    validation:
      xsd_validation: true
      business_rule_validation: true
    correlation:
      uetr_generation: true
      message_correlation: true
    limits:
      max_message_size: 5MB
      max_messages_per_second: 100
      max_concurrent_requests: 10

# Partner Tenant ISO 20022 Configuration
partner_tenant:
  iso20022:
    message_formats:
      - pain.001.001.10 (JSON)
      - pain.002.001.10 (JSON)
    validation:
      json_schema_validation: true
    correlation:
      uetr_generation: true
    limits:
      max_message_size: 1MB
      max_messages_per_second: 10
      max_concurrent_requests: 5
```

## 🔄 **Tenant Events**

### **Tenant Events Published**
```java
// Tenant lifecycle events
public class TenantCreatedEvent {
    private String tenantId;
    private String tenantName;
    private String tenantType;
    private Map<String, Object> iso20022Config;
    private Instant timestamp;
}

public class TenantActivatedEvent {
    private String tenantId;
    private String tenantName;
    private Instant timestamp;
}

public class TenantSuspendedEvent {
    private String tenantId;
    private String reason;
    private Instant timestamp;
}

public class TenantConfigChangedEvent {
    private String tenantId;
    private String configKey;
    private Object oldValue;
    private Object newValue;
    private Instant timestamp;
}

// Business unit events
public class BusinessUnitCreatedEvent {
    private String tenantId;
    private String buId;
    private String buName;
    private String buType;
    private Instant timestamp;
}

// Customer events
public class CustomerCreatedEvent {
    private String tenantId;
    private String buId;
    private String customerId;
    private String customerName;
    private Instant timestamp;
}
```

### **Tenant Events Subscribed**
```java
// Payment events for tenant metrics
@EventListener
public void onPaymentCompleted(PaymentCompletedEvent event) {
    tenantMetricsService.updateTenantMetrics(
        event.getTenantId(),
        "payment_completed",
        event.getAmount(),
        event.getTimestamp()
    );
}

@EventListener
public void onPaymentFailed(PaymentFailedEvent event) {
    tenantMetricsService.updateTenantMetrics(
        event.getTenantId(),
        "payment_failed",
        event.getAmount(),
        event.getTimestamp()
    );
}

// ISO 20022 message events
@EventListener
public void onIso20022MessageProcessed(Iso20022MessageProcessedEvent event) {
    tenantMetricsService.updateTenantMetrics(
        event.getTenantId(),
        "iso20022_message_processed",
        event.getMessageType(),
        event.getTimestamp()
    );
}
```

## 📊 **Tenant Metrics and Monitoring**

### **Tenant Metrics Collection**
```java
@Service
public class TenantMetricsService {
    
    public void updateTenantMetrics(String tenantId, String metricType, 
                                   Object value, Instant timestamp) {
        TenantMetrics metrics = TenantMetrics.builder()
            .tenantId(tenantId)
            .metricType(metricType)
            .metricValue(value)
            .timestamp(timestamp)
            .build();
            
        tenantMetricsRepository.save(metrics);
        
        // Update real-time metrics in Redis
        updateRealtimeMetrics(tenantId, metricType, value);
    }
    
    private void updateRealtimeMetrics(String tenantId, String metricType, Object value) {
        String key = "tenant:metrics:" + tenantId;
        redisTemplate.opsForHash().increment(key, metricType, 1);
        redisTemplate.expire(key, Duration.ofHours(1));
    }
}
```

### **Tenant Metrics API**
```java
@RestController
@RequestMapping("/api/v1/platform/metrics/tenants")
@PreAuthorize("hasRole('PLATFORM_ADMIN')")
public class TenantMetricsController {
    
    @GetMapping
    public ResponseEntity<List<TenantMetricsResponse>> getAllTenantMetrics() {
        List<TenantMetricsResponse> metrics = tenantMetricsService.getAllTenantMetrics();
        return ResponseEntity.ok(metrics);
    }
    
    @GetMapping("/{tenantId}")
    public ResponseEntity<TenantMetricsResponse> getTenantMetrics(@PathVariable String tenantId) {
        TenantMetricsResponse metrics = tenantMetricsService.getTenantMetrics(tenantId);
        return ResponseEntity.ok(metrics);
    }
    
    @GetMapping("/{tenantId}/realtime")
    public ResponseEntity<Map<String, Object>> getRealtimeMetrics(@PathVariable String tenantId) {
        Map<String, Object> metrics = tenantMetricsService.getRealtimeMetrics(tenantId);
        return ResponseEntity.ok(metrics);
    }
}
```

## 🚀 **Tenant Onboarding Process**

### **Automated Tenant Onboarding**
```java
@Service
public class TenantOnboardingService {
    
    public TenantResponse onboardTenant(TenantOnboardingRequest request) {
        // 1. Validate tenant information
        validateTenantRequest(request);
        
        // 2. Create tenant record
        Tenant tenant = createTenant(request);
        
        // 3. Set up default configurations
        setupDefaultConfigurations(tenant.getTenantId());
        
        // 4. Create default business unit
        BusinessUnit defaultBU = createDefaultBusinessUnit(tenant.getTenantId());
        
        // 5. Set up ISO 20022 configurations
        setupIso20022Configurations(tenant.getTenantId(), request.getTenantType());
        
        // 6. Create admin user
        TenantUser adminUser = createAdminUser(tenant.getTenantId());
        
        // 7. Generate API keys
        TenantApiKey apiKey = generateApiKey(tenant.getTenantId());
        
        // 8. Publish tenant created event
        eventPublisher.publishEvent(new TenantCreatedEvent(tenant));
        
        return TenantResponse.builder()
            .tenantId(tenant.getTenantId())
            .tenantName(tenant.getTenantName())
            .tenantType(tenant.getTenantType())
            .status(tenant.getStatus())
            .apiKey(apiKey.getApiKey())
            .adminUser(adminUser.getUsername())
            .build();
    }
}
```

## 🔒 **Security and Compliance**

### **Tenant Isolation**
- **Row-Level Security (RLS)** in PostgreSQL
- **Tenant Context Propagation** across all services
- **API Key Management** per tenant
- **Role-Based Access Control (RBAC)** per tenant

### **Compliance Features**
- **Audit Trails** for all tenant operations
- **Data Retention** policies per tenant
- **GDPR Compliance** for EU tenants
- **ISO 20022 Compliance** per tenant configuration

---

**Version**: 2.0  
**Last Updated**: 2025-01-27  
**Status**: 🚀 Ready for Implementation  
**Next Review**: Weekly during implementation
