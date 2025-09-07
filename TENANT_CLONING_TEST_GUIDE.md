# Tenant Cloning System - End-to-End Testing Guide

## Overview

This guide provides comprehensive testing procedures for the tenant cloning and migration system, including manual testing steps, automated test scripts, and troubleshooting procedures.

## Prerequisites

### Required Services
- PostgreSQL Database (port 5432)
- Redis (port 6379)
- Kafka (port 9092)
- Eureka Service Discovery (port 8761)
- API Gateway (port 8080)
- Middleware Service (port 8082)
- Frontend Application (port 3000)

### Required Tools
- curl (for API testing)
- jq (for JSON processing)
- Docker & Docker Compose
- Node.js & npm (for frontend)

## Quick Start Testing

### 1. Start All Services
```bash
# Start infrastructure services
docker-compose up -d postgres redis kafka eureka

# Start application services
docker-compose up -d

# Start frontend
cd frontend && npm start
```

### 2. Run Automated Tests
```bash
# Run the comprehensive test script
./test-tenant-cloning.sh
```

## Manual Testing Procedures

### 1. Authentication Testing

#### Get Test JWT Token
```bash
# Get admin token
curl -X POST http://localhost:8082/api/auth/admin-token

# Get read-only token
curl -X POST http://localhost:8082/api/auth/readonly-token

# Get custom token
curl -X POST "http://localhost:8082/api/auth/test-token?username=testuser&authorities=tenant:read,tenant:manage"
```

#### Test Token Validation
```bash
# Test with valid token
curl -H "Authorization: Bearer YOUR_TOKEN" http://localhost:8080/api/tenant-management/tenants

# Test without token (should return 401)
curl http://localhost:8080/api/tenant-management/tenants

# Test with invalid token (should return 401)
curl -H "Authorization: Bearer invalid-token" http://localhost:8080/api/tenant-management/tenants
```

### 2. Tenant Management API Testing

#### Get Available Tenants
```bash
curl -H "Authorization: Bearer YOUR_TOKEN" \
  http://localhost:8080/api/tenant-management/tenants
```

**Expected Response:**
```json
["tenant-001", "tenant-002", "tenant-003"]
```

#### Clone Tenant
```bash
curl -X POST \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "sourceTenantId": "tenant-001",
    "targetTenantId": "tenant-clone-test",
    "targetVersion": "1.0.0",
    "targetEnvironment": "DEVELOPMENT",
    "clonedBy": "test-user",
    "changeLog": "Test clone operation"
  }' \
  http://localhost:8080/api/tenant-management/clone
```

**Expected Response:**
```json
{
  "success": true,
  "message": "Tenant cloned successfully",
  "targetTenantId": "tenant-clone-test",
  "targetVersion": "1.0.0",
  "configurationDataCount": 8,
  "metadataCount": 5
}
```

#### Export Tenant Configuration
```bash
curl -X POST \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "tenantId": "tenant-001",
    "version": "1.0.0",
    "exportFormat": "JSON",
    "exportedBy": "test-user",
    "exportReason": "Test export operation"
  }' \
  http://localhost:8080/api/tenant-management/export
```

**Expected Response:**
```json
{
  "success": true,
  "message": "Tenant exported successfully",
  "exportId": "export-12345",
  "downloadUrl": "/api/tenant-management/download/export-12345",
  "fileSize": 2048
}
```

#### Import Tenant Configuration
```bash
curl -X POST \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "importData": "{\"tenantId\":\"tenant-001\",\"version\":\"1.0.0\",\"configurations\":{}}",
    "targetTenantId": "tenant-import-test",
    "targetVersion": "1.0.0",
    "targetEnvironment": "DEVELOPMENT",
    "importedBy": "test-user"
  }' \
  http://localhost:8080/api/tenant-management/import
```

#### Get Tenant Versions
```bash
curl -H "Authorization: Bearer YOUR_TOKEN" \
  http://localhost:8080/api/tenant-management/tenants/tenant-001/versions
```

#### Get Tenant History
```bash
curl -H "Authorization: Bearer YOUR_TOKEN" \
  http://localhost:8080/api/tenant-management/tenants/tenant-001/history
```

#### Get Statistics
```bash
curl -H "Authorization: Bearer YOUR_TOKEN" \
  http://localhost:8080/api/tenant-management/statistics
```

### 3. Frontend Testing

#### Access Tenant Management Page
1. Open browser to `http://localhost:3000`
2. Login with valid credentials
3. Navigate to "Tenant Management" in the sidebar
4. Verify the page loads with tenant list

#### Test Clone Functionality
1. Click "Clone Tenant" button
2. Fill in the clone form:
   - Source Tenant: tenant-001
   - Target Tenant: tenant-frontend-test
   - Target Version: 1.0.0
   - Environment: DEVELOPMENT
3. Click "Clone"
4. Verify success message appears
5. Verify new tenant appears in the list

#### Test Export Functionality
1. Click "Export" button
2. Select tenant and version
3. Choose export format (JSON/YAML/XML)
4. Click "Export"
5. Verify download starts

#### Test Import Functionality
1. Click "Import" button
2. Upload a valid configuration file
3. Set target tenant details
4. Click "Import"
5. Verify success message

### 4. Error Handling Testing

#### Test Invalid Requests
```bash
# Test clone with non-existent source tenant
curl -X POST \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "sourceTenantId": "non-existent",
    "targetTenantId": "test",
    "targetVersion": "1.0.0",
    "targetEnvironment": "DEVELOPMENT"
  }' \
  http://localhost:8080/api/tenant-management/clone

# Expected: HTTP 400 with error message
```

#### Test Permission Denied
```bash
# Test with read-only token
READONLY_TOKEN=$(curl -s -X POST http://localhost:8082/api/auth/readonly-token | jq -r '.token')

curl -X POST \
  -H "Authorization: Bearer $READONLY_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "sourceTenantId": "tenant-001",
    "targetTenantId": "test",
    "targetVersion": "1.0.0",
    "targetEnvironment": "DEVELOPMENT"
  }' \
  http://localhost:8080/api/tenant-management/clone

# Expected: HTTP 403 Forbidden
```

## Database Testing

### Verify Tables Exist
```sql
-- Connect to PostgreSQL
psql -h localhost -U postgres -d payment_engine

-- Check if tenant configuration tables exist
\dt tenant_*

-- Check sample data
SELECT * FROM tenant_configurations LIMIT 5;
SELECT * FROM tenant_configuration_data LIMIT 5;
SELECT * FROM tenant_cloning_history LIMIT 5;
```

### Verify Data Integrity
```sql
-- Check tenant configurations
SELECT tenant_id, version, is_active, environment 
FROM tenant_configurations 
ORDER BY tenant_id, version;

-- Check configuration data
SELECT tc.tenant_id, tc.version, COUNT(tcd.config_key) as config_count
FROM tenant_configurations tc
LEFT JOIN tenant_configuration_data tcd ON tc.id = tcd.tenant_configuration_id
GROUP BY tc.tenant_id, tc.version;

-- Check cloning history
SELECT source_tenant_id, target_tenant_id, operation_type, success, cloned_at
FROM tenant_cloning_history
ORDER BY cloned_at DESC;
```

## Performance Testing

### Load Testing with Apache Bench
```bash
# Test get tenants endpoint
ab -n 100 -c 10 -H "Authorization: Bearer YOUR_TOKEN" \
  http://localhost:8080/api/tenant-management/tenants

# Test clone endpoint
ab -n 50 -c 5 -p clone_data.json -T application/json \
  -H "Authorization: Bearer YOUR_TOKEN" \
  http://localhost:8080/api/tenant-management/clone
```

### Memory and CPU Monitoring
```bash
# Monitor middleware service
docker stats middleware-service

# Monitor database
docker stats postgres
```

## Troubleshooting

### Common Issues

#### 1. Service Not Starting
```bash
# Check service logs
docker-compose logs middleware-service
docker-compose logs api-gateway

# Check service health
curl http://localhost:8082/actuator/health
curl http://localhost:8080/actuator/health
```

#### 2. Database Connection Issues
```bash
# Check database connectivity
docker-compose exec postgres psql -U postgres -d payment_engine -c "SELECT 1;"

# Check database logs
docker-compose logs postgres
```

#### 3. Authentication Issues
```bash
# Verify JWT secret configuration
curl -X POST http://localhost:8082/api/auth/admin-token

# Check middleware logs for authentication errors
docker-compose logs middleware-service | grep -i auth
```

#### 4. Frontend Issues
```bash
# Check frontend logs
cd frontend && npm run build

# Check browser console for errors
# Open Developer Tools (F12) and check Console tab
```

### Debug Mode

#### Enable Debug Logging
```yaml
# Add to middleware application.yml
logging:
  level:
    com.paymentengine.middleware: DEBUG
    org.springframework.security: DEBUG
    org.springframework.web: DEBUG
```

#### Database Query Logging
```yaml
# Add to middleware application.yml
spring:
  jpa:
    show-sql: true
    properties:
      hibernate:
        format_sql: true
        type: trace
```

## Test Data Management

### Create Test Data
```sql
-- Insert test tenant configurations
INSERT INTO tenant_configurations (tenant_id, version, name, description, is_active, environment, created_by)
VALUES 
  ('test-tenant-1', '1.0.0', 'Test Tenant 1', 'Test tenant for automation', true, 'DEVELOPMENT', 'test-user'),
  ('test-tenant-2', '1.0.0', 'Test Tenant 2', 'Test tenant for automation', true, 'DEVELOPMENT', 'test-user');

-- Insert test configuration data
INSERT INTO tenant_configuration_data (tenant_configuration_id, config_key, config_value)
SELECT tc.id, 'test.config', 'test-value'
FROM tenant_configurations tc
WHERE tc.tenant_id = 'test-tenant-1';
```

### Cleanup Test Data
```sql
-- Clean up test data
DELETE FROM tenant_cloning_history WHERE source_tenant_id LIKE 'test-%' OR target_tenant_id LIKE 'test-%';
DELETE FROM tenant_configuration_data WHERE tenant_configuration_id IN (
  SELECT id FROM tenant_configurations WHERE tenant_id LIKE 'test-%'
);
DELETE FROM tenant_configurations WHERE tenant_id LIKE 'test-%';
```

## Continuous Integration

### GitHub Actions Test
```yaml
name: Tenant Cloning Tests
on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      - name: Start services
        run: docker-compose up -d
      - name: Wait for services
        run: sleep 30
      - name: Run tests
        run: ./test-tenant-cloning.sh
      - name: Cleanup
        run: docker-compose down
```

## Security Testing

### OWASP ZAP Testing
```bash
# Install OWASP ZAP
docker run -t owasp/zap2docker-stable zap-baseline.py -t http://localhost:8080/api/tenant-management/tenants
```

### Authentication Bypass Testing
```bash
# Test various authentication bypass attempts
curl -H "Authorization: Bearer " http://localhost:8080/api/tenant-management/tenants
curl -H "Authorization: Bearer null" http://localhost:8080/api/tenant-management/tenants
curl -H "Authorization: Bearer undefined" http://localhost:8080/api/tenant-management/tenants
```

## Conclusion

This testing guide provides comprehensive procedures for validating the tenant cloning system. Regular testing ensures system reliability, security, and performance. For production deployments, consider implementing automated testing in your CI/CD pipeline and regular security audits.