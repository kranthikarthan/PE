# Tenant Cloning and Migration Guide

## Overview

This guide provides comprehensive documentation for the tenant cloning and migration system in the Payment Engine. The system enables complete tenant configuration management, including cloning, versioning, export/import, and migration across different environments (dev, IAT, UAT, prod).

## Table of Contents

1. [System Architecture](#system-architecture)
2. [Core Features](#core-features)
3. [Tenant Configuration Versioning](#tenant-configuration-versioning)
4. [Cloning Operations](#cloning-operations)
5. [Export/Import Functionality](#exportimport-functionality)
6. [Environment Migration](#environment-migration)
7. [Template Management](#template-management)
8. [API Reference](#api-reference)
9. [React Frontend](#react-frontend)
10. [Best Practices](#best-practices)
11. [Troubleshooting](#troubleshooting)

## System Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                    TENANT CLONING SYSTEM                       │
├─────────────────────────────────────────────────────────────────┤
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐ │
│  │   React UI      │  │   REST API      │  │   Database      │ │
│  │                 │  │                 │  │                 │ │
│  │  ┌───────────┐  │  │  ┌───────────┐  │  │  ┌───────────┐  │ │
│  │  │ Tenant    │  │  │  │ Tenant    │  │  │  │ Tenant    │  │ │
│  │  │ Management│  │  │  │ Management│  │  │  │ Configs   │  │ │
│  │  │ Component │  │  │  │ Controller│  │  │  │ Tables    │  │ │
│  │  └───────────┘  │  │  └───────────┘  │  │  └───────────┘  │ │
│  │  ┌───────────┐  │  │  ┌───────────┐  │  │  ┌───────────┐  │ │
│  │  │ Clone     │  │  │  │ Tenant    │  │  │  │ Cloning   │  │ │
│  │  │ Dialogs   │  │  │  │ Cloning   │  │  │  │ History   │  │ │
│  │  └───────────┘  │  │  │ Service   │  │  │  └───────────┘  │ │
│  │  ┌───────────┐  │  │  └───────────┘  │  │  ┌───────────┐  │ │
│  │  │ Export/   │  │  │  ┌───────────┐  │  │  │ Templates │  │ │
│  │  │ Import    │  │  │  │ Export/   │  │  │  │ Tables    │  │ │
│  │  │ Dialogs   │  │  │  │ Import    │  │  │  └───────────┘  │ │
│  │  └───────────┘  │  │  │ Service   │  │  │                 │ │
│  └─────────────────┘  │  └───────────┘  │  └─────────────────┘ │
└─────────────────────────────────────────────────────────────────┘
```

## Core Features

### 1. Tenant Configuration Versioning
- **Semantic Versioning**: Support for major.minor.patch versioning
- **Version History**: Complete audit trail of all configuration changes
- **Rollback Capability**: Ability to rollback to any previous version
- **Active Version Management**: Single active version per tenant per environment

### 2. Tenant Cloning
- **Complete Configuration Copy**: Clone all configuration data and metadata
- **Environment-Specific Cloning**: Clone with environment-specific overrides
- **Version-Specific Cloning**: Clone specific versions of tenant configurations
- **Selective Cloning**: Choose which parts to clone (config data, metadata, etc.)

### 3. Export/Import System
- **Multiple Formats**: Support for JSON, YAML, and XML export formats
- **Compression**: Optional compression for large exports
- **Batch Operations**: Export/import multiple versions at once
- **Validation**: Pre-import validation with detailed error reporting

### 4. Environment Migration
- **Cross-Environment Cloning**: Clone configurations between environments
- **Environment Overrides**: Automatic environment-specific configuration adjustments
- **Migration Tracking**: Complete audit trail of environment migrations
- **Validation**: Environment-specific validation rules

### 5. Template Management
- **Reusable Templates**: Create templates from existing configurations
- **Template Application**: Apply templates to new tenants with overrides
- **Template Versioning**: Version control for templates
- **Usage Tracking**: Monitor template usage and effectiveness

## Tenant Configuration Versioning

### Version Structure
```
Version Format: MAJOR.MINOR.PATCH
Example: 1.2.3

MAJOR: Breaking changes or major feature additions
MINOR: New features or significant enhancements
PATCH: Bug fixes or minor improvements
```

### Version Management
```java
// Auto-generate next version
String nextVersion = generateVersion(tenantId);
// Result: "1.0.1" (increments patch version)

// Manual version specification
TenantCloneRequest request = new TenantCloneRequest();
request.setTargetVersion("2.0.0"); // Custom version
```

### Version History
```sql
-- Get all versions for a tenant
SELECT version, created_at, created_by, change_log 
FROM tenant_configurations 
WHERE tenant_id = 'tenant-001' 
ORDER BY created_at DESC;

-- Get active version
SELECT * FROM tenant_configurations 
WHERE tenant_id = 'tenant-001' AND is_active = TRUE;
```

## Cloning Operations

### 1. Basic Tenant Cloning

#### API Request
```http
POST /api/tenant-management/clone
Content-Type: application/json

{
  "sourceTenantId": "tenant-001",
  "targetTenantId": "tenant-002",
  "targetEnvironment": "DEVELOPMENT",
  "name": "Development Tenant",
  "description": "Cloned from production for development",
  "changeLog": "Initial clone from production",
  "clonedBy": "admin",
  "activateAfterClone": true,
  "copyMetadata": true,
  "copyConfigurationData": true
}
```

#### Response
```json
{
  "success": true,
  "message": "Tenant cloned successfully",
  "configurationId": "uuid-here",
  "tenantId": "tenant-002",
  "version": "1.0.0",
  "environment": "DEVELOPMENT",
  "sourceTenantId": "tenant-001",
  "sourceVersion": "1.2.0",
  "clonedAt": "2024-01-01T10:00:00",
  "clonedBy": "admin",
  "summary": {
    "configurationDataCount": 25,
    "metadataCount": 10,
    "overridesApplied": 0
  }
}
```

### 2. Environment-Specific Cloning

#### API Request
```http
POST /api/tenant-management/clone-to-environment
Content-Type: application/json

{
  "sourceTenantId": "tenant-001",
  "targetTenantId": "tenant-001-uat",
  "targetEnvironment": "USER_ACCEPTANCE",
  "configurationOverrides": {
    "database.url": "jdbc:postgresql://uat-db:5432/payment_engine",
    "redis.host": "uat-redis",
    "logging.level": "WARN"
  },
  "metadataOverrides": {
    "environment.type": "UAT",
    "backup.enabled": "false"
  }
}
```

### 3. Version-Specific Cloning

#### API Request
```http
POST /api/tenant-management/clone-version
Content-Type: application/json

{
  "sourceTenantId": "tenant-001",
  "sourceVersion": "1.1.0",
  "targetTenantId": "tenant-001-rollback",
  "targetEnvironment": "PRODUCTION",
  "changeLog": "Rollback to stable version 1.1.0"
}
```

## Export/Import Functionality

### 1. Export Tenant Configuration

#### API Request
```http
POST /api/tenant-management/export
Content-Type: application/json

{
  "tenantId": "tenant-001",
  "version": "1.2.0",
  "exportFormat": "JSON",
  "includeConfigurationData": true,
  "includeMetadata": true,
  "includeHistory": false,
  "compress": true,
  "exportedBy": "admin",
  "exportReason": "Migration to new environment"
}
```

#### Response
```json
{
  "success": true,
  "message": "Tenant exported successfully",
  "exportId": "export_1704067200000_abc12345",
  "tenantId": "tenant-001",
  "version": "1.2.0",
  "exportFormat": "JSON",
  "filePath": "/tmp/exports/tenant_export_tenant-001_export_1704067200000_abc12345.json.zip",
  "downloadUrl": "/api/tenant-management/exports/export_1704067200000_abc12345/download",
  "fileSize": 1024000,
  "exportedAt": "2024-01-01T10:00:00",
  "exportedBy": "admin",
  "exportSummary": {
    "configurationsExported": 1,
    "versionsIncluded": ["1.2.0"],
    "totalConfigurationData": 25,
    "totalMetadata": 10
  }
}
```

### 2. Import Tenant Configuration

#### API Request
```http
POST /api/tenant-management/import
Content-Type: application/json

{
  "importData": "{\"exportMetadata\":{\"exportedAt\":\"2024-01-01T10:00:00\"},\"configurations\":[...]}",
  "importFormat": "JSON",
  "targetTenantId": "tenant-002",
  "targetEnvironment": "PRODUCTION",
  "activateAfterImport": true,
  "validateBeforeImport": true,
  "overwriteExisting": false,
  "importedBy": "admin"
}
```

### 3. File-Based Import

#### API Request
```http
POST /api/tenant-management/import-file
Content-Type: multipart/form-data

file: tenant_export_tenant-001_export_1704067200000_abc12345.json.zip
targetTenantId: tenant-002
targetEnvironment: PRODUCTION
importedBy: admin
```

## Environment Migration

### Migration Workflow

1. **Export from Source Environment**
   ```bash
   curl -X POST http://source-env/api/tenant-management/export \
     -H "Content-Type: application/json" \
     -d '{
       "tenantId": "tenant-001",
       "exportFormat": "JSON",
       "compress": true,
       "exportedBy": "admin"
     }'
   ```

2. **Download Export File**
   ```bash
   curl -O http://source-env/api/tenant-management/exports/export_id/download
   ```

3. **Import to Target Environment**
   ```bash
   curl -X POST http://target-env/api/tenant-management/import-file \
     -F "file=@tenant_export_tenant-001_export_id.json.zip" \
     -F "targetTenantId=tenant-001" \
     -F "targetEnvironment=PRODUCTION" \
     -F "importedBy=admin"
   ```

### Environment-Specific Overrides

#### Development Environment
```json
{
  "logging.level": "DEBUG",
  "debug.enabled": "true",
  "cache.enabled": "false",
  "database.url": "jdbc:postgresql://dev-db:5432/payment_engine",
  "redis.host": "dev-redis"
}
```

#### Integration Testing Environment
```json
{
  "logging.level": "INFO",
  "debug.enabled": "false",
  "cache.enabled": "true",
  "database.url": "jdbc:postgresql://iat-db:5432/payment_engine",
  "redis.host": "iat-redis"
}
```

#### User Acceptance Testing Environment
```json
{
  "logging.level": "WARN",
  "debug.enabled": "false",
  "cache.enabled": "true",
  "database.url": "jdbc:postgresql://uat-db:5432/payment_engine",
  "redis.host": "uat-redis"
}
```

#### Production Environment
```json
{
  "logging.level": "ERROR",
  "debug.enabled": "false",
  "cache.enabled": "true",
  "monitoring.enabled": "true",
  "database.url": "jdbc:postgresql://prod-db:5432/payment_engine",
  "redis.host": "prod-redis"
}
```

## Template Management

### 1. Create Template

#### API Request
```http
POST /api/tenant-management/templates?tenantId=tenant-001&version=1.2.0&templateName=production-template
```

#### Response
```json
{
  "success": true,
  "message": "Template created successfully",
  "configurationId": "uuid-here",
  "tenantId": "TEMPLATE_PRODUCTION-TEMPLATE",
  "version": "1.0.0"
}
```

### 2. Apply Template

#### API Request
```http
POST /api/tenant-management/templates/production-template/apply?tenantId=tenant-003
Content-Type: application/json

{
  "tenant.id": "tenant-003",
  "database.url": "jdbc:postgresql://new-db:5432/payment_engine",
  "redis.host": "new-redis"
}
```

### 3. Template Structure
```json
{
  "template_name": "production-template",
  "template_description": "Standard production tenant template",
  "source_tenant_id": "tenant-001",
  "source_version": "1.2.0",
  "template_configuration_data": {
    "tenant.id": "{{TENANT_ID}}",
    "environment": "PRODUCTION",
    "database.url": "jdbc:postgresql://prod-db:5432/payment_engine",
    "redis.host": "prod-redis",
    "logging.level": "INFO",
    "cache.enabled": "true",
    "monitoring.enabled": "true"
  },
  "template_metadata": {
    "template.type": "production",
    "created.from": "tenant-001",
    "version": "1.2.0"
  }
}
```

## API Reference

### Tenant Management Endpoints

#### Clone Operations
- `POST /api/tenant-management/clone` - Clone tenant configuration
- `POST /api/tenant-management/clone-to-environment` - Clone to different environment
- `POST /api/tenant-management/clone-version` - Clone specific version
- `POST /api/tenant-management/rollback/{tenantId}/{version}` - Rollback to version

#### Export/Import Operations
- `POST /api/tenant-management/export` - Export tenant configuration
- `POST /api/tenant-management/import` - Import tenant configuration
- `POST /api/tenant-management/import-file` - Import from file

#### Information Endpoints
- `GET /api/tenant-management/tenants` - Get all available tenants
- `GET /api/tenant-management/tenants/{tenantId}/versions` - Get tenant versions
- `GET /api/tenant-management/tenants/{tenantId}/history` - Get tenant history
- `GET /api/tenant-management/compare` - Compare configurations
- `GET /api/tenant-management/validate/{tenantId}/{version}` - Validate configuration
- `GET /api/tenant-management/statistics` - Get cloning statistics

#### Template Management
- `POST /api/tenant-management/templates` - Create template
- `POST /api/tenant-management/templates/{templateName}/apply` - Apply template

### Request/Response DTOs

#### TenantCloneRequest
```java
public class TenantCloneRequest {
    private String sourceTenantId;
    private String sourceVersion;
    private String targetTenantId;
    private TenantConfiguration.Environment targetEnvironment;
    private String targetVersion;
    private String name;
    private String description;
    private String changeLog;
    private String clonedBy;
    private Boolean activateAfterClone = true;
    private Boolean copyMetadata = true;
    private Boolean copyConfigurationData = true;
    private Map<String, String> configurationOverrides;
    private Map<String, String> metadataOverrides;
    private Map<String, String> additionalMetadata;
}
```

#### TenantExportRequest
```java
public class TenantExportRequest {
    private String tenantId;
    private String version;
    private List<String> includeVersions;
    private TenantConfiguration.Environment environment;
    private Boolean includeConfigurationData = true;
    private Boolean includeMetadata = true;
    private Boolean includeHistory = false;
    private Boolean includeRelatedConfigurations = false;
    private String exportFormat = "JSON";
    private String exportPath;
    private Boolean compress = true;
    private String exportedBy;
    private String exportReason;
}
```

## React Frontend

### TenantManagement Component

The React frontend provides a comprehensive interface for tenant management operations:

#### Features
- **Tenant Overview**: List all available tenants with version information
- **Cloning Interface**: User-friendly forms for tenant cloning operations
- **Export/Import**: File-based and data-based export/import functionality
- **Version Management**: View and manage tenant configuration versions
- **Template Management**: Create and apply configuration templates
- **Statistics Dashboard**: View cloning and usage statistics

#### Key Components
```typescript
interface TenantConfiguration {
  id: string;
  tenantId: string;
  version: string;
  name: string;
  description: string;
  isActive: boolean;
  isDefault: boolean;
  environment: string;
  sourceTenantId?: string;
  sourceVersion?: string;
  clonedBy?: string;
  clonedAt?: string;
  configurationData: Record<string, string>;
  metadata: Record<string, string>;
  changeLog?: string;
  createdBy: string;
  updatedBy: string;
  createdAt: string;
  updatedAt: string;
}
```

#### Usage
```typescript
import TenantManagement from './components/TenantManagement';

function App() {
  return (
    <div className="App">
      <TenantManagement />
    </div>
  );
}
```

### Frontend Features

#### 1. Tenant Overview Tab
- List all available tenants
- Show active versions and environments
- Quick actions for cloning, exporting, and viewing history

#### 2. Cloning Dialog
- Source tenant selection with autocomplete
- Target tenant and environment configuration
- Configuration and metadata override options
- Change log and description fields

#### 3. Export Dialog
- Tenant and version selection
- Export format selection (JSON, YAML, XML)
- Compression and inclusion options
- Export reason tracking

#### 4. Import Dialog
- File upload for import
- Data-based import with validation
- Target tenant configuration
- Import options and overrides

#### 5. Statistics Tab
- Configuration counts by environment
- Cloning operation statistics
- Template usage analytics
- Recent activity tracking

## Best Practices

### 1. Version Management
- **Semantic Versioning**: Use semantic versioning for all configurations
- **Change Logs**: Always provide meaningful change logs
- **Testing**: Test configurations in development before promoting
- **Rollback Planning**: Keep stable versions for quick rollbacks

### 2. Environment Migration
- **Staged Migration**: Use development → IAT → UAT → production flow
- **Environment Validation**: Validate configurations for each environment
- **Backup Strategy**: Always backup before major migrations
- **Documentation**: Document all migration steps and decisions

### 3. Template Usage
- **Standard Templates**: Create standard templates for common configurations
- **Template Versioning**: Version control your templates
- **Usage Tracking**: Monitor template usage and effectiveness
- **Regular Updates**: Keep templates updated with best practices

### 4. Security Considerations
- **Access Control**: Implement proper access controls for cloning operations
- **Audit Logging**: Maintain comprehensive audit logs
- **Data Sensitivity**: Handle sensitive configuration data appropriately
- **Validation**: Validate all imported configurations

### 5. Performance Optimization
- **Batch Operations**: Use batch operations for multiple configurations
- **Compression**: Use compression for large exports
- **Caching**: Cache frequently accessed configurations
- **Cleanup**: Regular cleanup of old versions and exports

## Troubleshooting

### Common Issues

#### 1. Clone Operation Failures
```bash
# Check tenant existence
curl -X GET http://localhost:8080/api/tenant-management/tenants

# Validate source configuration
curl -X GET http://localhost:8080/api/tenant-management/validate/tenant-001/1.0.0

# Check for version conflicts
curl -X GET http://localhost:8080/api/tenant-management/tenants/tenant-001/versions
```

#### 2. Export/Import Issues
```bash
# Check export format
curl -X POST http://localhost:8080/api/tenant-management/export \
  -H "Content-Type: application/json" \
  -d '{"tenantId": "tenant-001", "exportFormat": "JSON"}'

# Validate import data
curl -X POST http://localhost:8080/api/tenant-management/import \
  -H "Content-Type: application/json" \
  -d '{"importData": "...", "validateBeforeImport": true}'
```

#### 3. Template Application Failures
```bash
# Check template existence
curl -X GET http://localhost:8080/api/tenant-management/templates

# Validate template structure
curl -X GET http://localhost:8080/api/tenant-management/validate/TEMPLATE_PRODUCTION-TEMPLATE/1.0.0
```

### Debugging Commands

#### Database Queries
```sql
-- Check tenant configurations
SELECT * FROM tenant_configurations WHERE tenant_id = 'tenant-001';

-- Check cloning history
SELECT * FROM tenant_cloning_history WHERE source_tenant_id = 'tenant-001';

-- Check export history
SELECT * FROM tenant_export_history WHERE tenant_id = 'tenant-001';

-- Check template usage
SELECT * FROM tenant_template_usage WHERE applied_to_tenant_id = 'tenant-001';
```

#### Log Analysis
```bash
# Check application logs
tail -f logs/application.log | grep "TenantCloning"

# Check database logs
tail -f logs/database.log | grep "tenant_configurations"

# Check API logs
tail -f logs/api.log | grep "/api/tenant-management"
```

### Error Codes

#### Common Error Responses
```json
{
  "success": false,
  "message": "Source tenant configuration not found",
  "errors": {
    "sourceTenantId": "Tenant 'tenant-001' does not exist",
    "sourceVersion": "Version '1.0.0' not found for tenant 'tenant-001'"
  }
}
```

#### Validation Errors
```json
{
  "success": false,
  "message": "Import validation failed",
  "errors": {
    "validation": {
      "valid": false,
      "errors": ["Missing required configuration key: tenant.id"],
      "warnings": ["No metadata found"]
    }
  }
}
```

## Conclusion

The tenant cloning and migration system provides comprehensive capabilities for managing tenant configurations across environments. With features like versioning, cloning, export/import, and template management, it enables efficient and reliable tenant configuration management for the Payment Engine.

For additional support or questions, please refer to the API documentation or contact the development team.