# Tenant Management Service

**Phase 3.1 - Platform Services**

Tenant management microservice for the Payments Engine. Responsible for tenant lifecycle management, business unit management, configuration management, and multi-tenancy enforcement.

## Overview

### Purpose
The Tenant Management Service provides a centralized API for:
- Creating and managing tenants (banks, financial institutions, fintechs, corporates)
- Managing business units within tenants
- Tenant configuration management
- Multi-tenancy enforcement via Row-Level Security (RLS)
- Tenant provisioning and activation

### Key Features
- ✅ **Tenant Lifecycle Management**: Create, activate, suspend, delete tenants
- ✅ **Business Unit Management**: Hierarchical business unit structures
- ✅ **Configuration Management**: Tenant-specific configurations
- ✅ **Multi-Tenancy Enforcement**: PostgreSQL RLS + application-level checks
- ✅ **Caching**: Redis caching for O(1) tenant lookups
- ✅ **Event Publishing**: Kafka topics for tenant lifecycle events
- ✅ **Audit Logging**: Complete audit trail of all operations
- ✅ **Security**: JWT authentication, X-Tenant-ID validation
- ✅ **Observability**: OpenTelemetry tracing, Micrometer metrics

## Architecture

### Technology Stack
- **Framework**: Spring Boot 3.2.0
- **Database**: PostgreSQL with Row-Level Security (RLS)
- **Caching**: Redis (tenant lookups O(1))
- **Messaging**: Azure Service Bus (tenant events)
- **Security**: JWT tokens, Azure AD B2C
- **Observability**: OpenTelemetry + Prometheus
- **Testing**: JUnit 5, Mockito, TestContainers

### Project Structure
```
tenant-management-service/
├── src/main/java/com/payments/tenant/
│   ├── TenantManagementServiceApplication.java       # Entry point
│   ├── config/
│   │   ├── SecurityConfig.java                       # JWT, CORS configuration
│   │   ├── CacheConfig.java                          # Redis caching
│   │   └── TenantContextConfig.java                  # Multi-tenancy context
│   ├── controller/
│   │   └── TenantController.java                     # REST API endpoints
│   ├── service/
│   │   ├── TenantService.java                        # Business logic
│   │   ├── BusinessUnitService.java                  # Business unit management
│   │   └── TenantEventPublisher.java                 # Event publishing
│   ├── repository/
│   │   ├── TenantRepository.java                     # JPA repository
│   │   └── BusinessUnitRepository.java               # JPA repository
│   ├── entity/
│   │   ├── TenantEntity.java                         # JPA entity
│   │   └── BusinessUnitEntity.java                   # JPA entity
│   ├── dto/
│   │   ├── CreateTenantRequest.java                  # Request DTO
│   │   ├── TenantResponse.java                       # Response DTO
│   │   └── TenantStatusUpdateRequest.java            # Update DTO
│   ├── exception/
│   │   ├── TenantNotFoundException.java              # Not found exception
│   │   ├── TenantCreationException.java              # Creation exception
│   │   └── MultiTenancyException.java                # Multi-tenancy violation
│   └── util/
│       ├── TenantContextHolder.java                  # ThreadLocal tenant context
│       └── TenantIdGenerator.java                    # ID generation
├── src/main/resources/
│   ├── application.yml                               # Application config
│   └── db/migration/
│       └── V1__Create_tenant_tables.sql              # DB schema (Flyway)
├── src/test/java/com/payments/tenant/
│   ├── controller/TenantControllerTest.java
│   ├── service/TenantServiceTest.java
│   └── repository/TenantRepositoryTest.java
└── pom.xml                                           # Maven dependencies
```

## API Endpoints

### Tenant Management

#### Create Tenant
```http
POST /api/v1/tenants
Content-Type: application/json
Authorization: Bearer <jwt_token>

{
  "tenantId": "STD-001",
  "tenantName": "Standard Bank",
  "tenantType": "BANK",
  "registrationNumber": "REG-12345",
  "contactEmail": "admin@standardbank.co.za",
  "contactPhone": "+27-11-xxx-xxxx",
  "address": {
    "line1": "123 Main Street",
    "city": "Johannesburg",
    "province": "Gauteng",
    "country": "ZAF",
    "postalCode": "2000"
  },
  "currency": "ZAR"
}

Response:
{
  "tenantId": "STD-001",
  "tenantName": "Standard Bank",
  "status": "PENDING_APPROVAL",
  "createdAt": "2025-01-01T10:00:00Z"
}
```

#### Get Tenant
```http
GET /api/v1/tenants/{tenantId}
Authorization: Bearer <jwt_token>
X-Tenant-ID: STD-001

Response:
{
  "tenantId": "STD-001",
  "tenantName": "Standard Bank",
  "tenantType": "BANK",
  "status": "ACTIVE",
  "createdAt": "2025-01-01T10:00:00Z",
  "updatedAt": "2025-01-02T15:30:00Z"
}
```

#### Activate Tenant
```http
PUT /api/v1/tenants/{tenantId}/activate
Authorization: Bearer <jwt_token>
X-Tenant-ID: {tenantId}

Response:
{
  "tenantId": "STD-001",
  "status": "ACTIVE",
  "message": "Tenant activated successfully"
}
```

#### List Tenants (Admin Only)
```http
GET /api/v1/tenants?page=0&size=20&status=ACTIVE
Authorization: Bearer <jwt_token>

Response:
{
  "content": [
    {
      "tenantId": "STD-001",
      "tenantName": "Standard Bank",
      "status": "ACTIVE"
    }
  ],
  "totalElements": 1,
  "totalPages": 1
}
```

### Business Unit Management

#### Create Business Unit
```http
POST /api/v1/tenants/{tenantId}/business-units
Authorization: Bearer <jwt_token>
X-Tenant-ID: {tenantId}

{
  "businessUnitName": "Retail Banking",
  "businessUnitType": "RETAIL",
  "parentBusinessUnitId": null
}

Response:
{
  "businessUnitId": "RET-STD-001",
  "businessUnitName": "Retail Banking",
  "status": "ACTIVE"
}
```

## Configuration

### Environment Variables

```bash
# Database
DB_USERNAME=postgres
DB_PASSWORD=password
DB_HOST=localhost
DB_PORT=5432
DB_NAME=payments_engine

# Redis
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=

# Security
JWT_SECRET=your-secret-key-change-in-production

# Azure
AZURE_KEYVAULT_ENABLED=false
AZURE_KEYVAULT_URI=https://your-keyvault.vault.azure.net/
AZURE_SERVICEBUS_CONNECTION_STRING=

# CORS
CORS_ALLOWED_ORIGINS=http://localhost:3000,http://localhost:4200
```

## Setup & Installation

### Prerequisites
- Java 17+
- PostgreSQL 12+
- Redis 6+
- Maven 3.8+

### Installation Steps

```bash
# 1. Build the service
mvn clean package

# 2. Run database migrations (automatic via Flyway)
# Ensure PostgreSQL is running and migrations folder exists

# 3. Start the service
java -jar target/tenant-management-service-0.1.0-SNAPSHOT.jar

# 4. Verify health
curl http://localhost:8081/api/v1/actuator/health
```

### Docker Setup

```bash
# Build Docker image
docker build -t payments-engine/tenant-management-service:latest .

# Run with docker-compose
docker-compose up tenant-management-service
```

## Security

### Multi-Tenancy Enforcement

#### Row-Level Security (RLS)
PostgreSQL Row-Level Security ensures data isolation at the database level:

```sql
-- All queries are scoped to current tenant
ALTER TABLE tenants ENABLE ROW LEVEL SECURITY;

CREATE POLICY tenant_isolation ON tenants
    USING (tenant_id = current_setting('app.current_tenant_id')::varchar);
```

#### Application-Level Context
```java
// TenantContextHolder stores current tenant in ThreadLocal
TenantContextHolder.setTenantId("STD-001");

// All service calls automatically check tenant context
@Service
public class TenantService {
  public Tenant getTenant(String tenantId) {
    String currentTenant = TenantContextHolder.getTenantId();
    if (!tenantId.equals(currentTenant)) {
      throw new MultiTenancyException("Access denied to tenant: " + tenantId);
    }
    return tenantRepository.findById(tenantId);
  }
}
```

### Authentication
- **JWT Tokens**: Signed with RS256 (RSA)
- **Expiration**: 1 hour (configurable)
- **Required Headers**:
  - `Authorization: Bearer <jwt_token>`
  - `X-Tenant-ID: <tenant_id>` (validated against token claims)
  - `X-Correlation-ID: <correlation_id>` (for tracing)

### Authorization
- **RBAC**: Role-Based Access Control
  - **Admin**: Full access (create/update/delete tenants)
  - **Operator**: View-only access (read tenants)
  - **Tenant Admin**: Manage own tenant configuration

## Observability

### Metrics
```bash
# Prometheus metrics
curl http://localhost:8081/api/v1/actuator/prometheus

# Key metrics:
- http_server_requests_seconds_bucket (API latency)
- jpa_hibernate_queries (DB queries)
- cache_gets_miss (Redis cache misses)
- tenant_creation_total (Total tenants created)
```

### Tracing
```bash
# OpenTelemetry traces exported to Jaeger
# Trace all tenant operations with correlation ID
- Trace ID: Unique per request
- Span ID: Per operation
- Parent Span: Request → Service → Repository
```

### Health Checks
```bash
# Liveness probe
curl http://localhost:8081/api/v1/actuator/health/liveness

# Readiness probe
curl http://localhost:8081/api/v1/actuator/health/readiness

# Dependency checks included:
- PostgreSQL connection
- Redis connection
- Service startup verification
```

## Testing

### Unit Tests
```bash
mvn test
```

### Integration Tests
```bash
# Using TestContainers for PostgreSQL/Redis
mvn verify
```

### Test Coverage
- Target: 80%+ coverage
- All critical paths tested (happy path + edge cases)
- Multi-tenant scenarios tested

## Development

### Code Quality Standards
- **SOLID Principles**: Single Responsibility, Open/Closed, etc.
- **Code Style**: Google Java Format (via Spotless)
- **Linting**: SonarQube (A rating target)
- **Methods**: < 20 lines, max 3 nesting levels

### Building Locally
```bash
# Compile
mvn compile

# Format code
mvn spotless:apply

# Run linter
mvn sonar:sonar
```

## Performance

### Caching Strategy
- **Tenant Lookups**: O(1) via Redis HashMap
- **TTL**: 10 minutes
- **Cache Key**: `tenant:{tenantId}`
- **Cache Invalidation**: On update/delete

### Database Indexes
```sql
-- Key indexes for performance
CREATE INDEX idx_tenants_status ON tenants(status);
CREATE INDEX idx_tenants_created_at ON tenants(created_at DESC);
CREATE INDEX idx_business_units_tenant ON business_units(tenant_id);
```

## Troubleshooting

### Common Issues

#### 1. JWT Token Validation Failed
```
Error: JWT signature verification failed
Solution: Ensure JWT_SECRET matches the issuer's secret
```

#### 2. Multi-Tenancy Violation
```
Error: Access denied to tenant (X-Tenant-ID mismatch)
Solution: Ensure X-Tenant-ID header matches JWT claims
```

#### 3. Database Migration Failed
```
Error: Flyway migration failed
Solution: Check PostgreSQL permissions, run migrations manually if needed
```

#### 4. Redis Connection Failed
```
Error: Cannot connect to Redis
Solution: Ensure Redis is running on localhost:6379, or update REDIS_HOST
```

## Support

- **Documentation**: See `/docs/34-FEATURE-BREAKDOWN-TREE-ENHANCED.md`
- **Architecture**: See `/docs/00-ARCHITECTURE-OVERVIEW.md`
- **Security**: See `/docs/21-SECURITY-ARCHITECTURE.md`

---

**Status**: ✅ Production Ready  
**Last Updated**: October 18, 2025  
**Maintainer**: Payments Engine Team
