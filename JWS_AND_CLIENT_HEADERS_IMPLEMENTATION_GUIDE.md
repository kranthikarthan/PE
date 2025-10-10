# JWS and Client Headers Implementation Guide

## Overview

This document provides comprehensive guidance on the implementation of JWS (JSON Web Signature) as an alternative to JWT authentication and configurable client ID/secret headers for outgoing HTTP calls in the Payment Engine system.

## Table of Contents

1. [Architecture Overview](#architecture-overview)
2. [JWS Implementation](#jws-implementation)
3. [Client Headers Configuration](#client-headers-configuration)
4. [Tenant Authentication Configuration](#tenant-authentication-configuration)
5. [Frontend Management Interface](#frontend-management-interface)
6. [API Endpoints](#api-endpoints)
7. [Configuration Examples](#configuration-examples)
8. [Security Considerations](#security-considerations)
9. [Migration Guide](#migration-guide)
10. [Troubleshooting](#troubleshooting)

---

## Architecture Overview

### System Components

```
┌─────────────────────────────────────────────────────────────────┐
│                    Payment Engine Architecture                  │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  ┌─────────────────┐    ┌─────────────────┐    ┌──────────────┐ │
│  │   Auth Service  │    │ Payment Processing│    │ API Gateway  │ │
│  │                 │    │ Service          │    │ Service      │ │
│  │ ┌─────────────┐ │    │ ┌─────────────┐ │    │ ┌──────────┐ │ │
│  │ │ JWT Service │ │    │ │ Outgoing    │ │    │ │ Auth     │ │ │
│  │ └─────────────┘ │    │ │ HTTP Service│ │    │ │ Filter   │ │ │
│  │ ┌─────────────┐ │    │ └─────────────┘ │    │ └──────────┘ │ │
│  │ │ JWS Service │ │    │ ┌─────────────┐ │    │              │ │
│  │ └─────────────┘ │    │ │ Tenant Auth │ │    │              │ │
│  │ ┌─────────────┐ │    │ │ Config      │ │    │              │ │
│  │ │ Unified     │ │    │ └─────────────┘ │    │              │ │
│  │ │ Token       │ │    │                 │    │              │ │
│  │ │ Service     │ │    │                 │    │              │ │
│  │ └─────────────┘ │    │                 │    │              │ │
│  └─────────────────┘    └─────────────────┘    └──────────────┘ │
│                                                                 │
│  ┌─────────────────────────────────────────────────────────────┐ │
│  │                React Frontend                              │ │
│  │  ┌─────────────────────────────────────────────────────┐   │ │
│  │  │         Tenant Auth Configuration UI                │   │ │
│  │  └─────────────────────────────────────────────────────┘   │ │
│  └─────────────────────────────────────────────────────────────┘ │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

### Key Features

- **Dual Token Support**: Both JWT and JWS authentication methods
- **Tenant-Specific Configuration**: Per-tenant authentication settings
- **Configurable Client Headers**: Customizable headers for outgoing requests
- **Unified Token Service**: Seamless switching between JWT and JWS
- **Frontend Management**: React-based configuration interface
- **Security**: Enhanced security with JWS and configurable authentication

---

## JWS Implementation

### JWS Service Features

The JWS service provides enhanced security features compared to standard JWT:

#### Supported Algorithms

| Algorithm | Type | Description |
|-----------|------|-------------|
| HS256 | Symmetric | HMAC with SHA-256 |
| HS384 | Symmetric | HMAC with SHA-384 |
| HS512 | Symmetric | HMAC with SHA-512 |
| RS256 | Asymmetric | RSA with SHA-256 |
| RS384 | Asymmetric | RSA with SHA-384 |
| RS512 | Asymmetric | RSA with SHA-512 |

#### Key Features

- **Enhanced Security**: Stronger cryptographic signatures
- **Algorithm Flexibility**: Support for both symmetric and asymmetric algorithms
- **Public Key Support**: External verification capabilities
- **Auto-Detection**: Automatic token type detection
- **Backward Compatibility**: Seamless integration with existing JWT infrastructure

### JWS Service Implementation

```java
@Service
public class JwsTokenService {
    
    // Generate access token using JWS
    public String generateAccessToken(UUID userId, String username, String email, 
                                    Set<String> roles, Set<String> permissions) {
        // Implementation details...
    }
    
    // Validate JWS token
    public boolean validateToken(String token) {
        // Implementation details...
    }
    
    // Get public key for external verification
    public String getPublicKey() {
        // Implementation details...
    }
}
```

### Unified Token Service

The unified token service provides a single interface for both JWT and JWS:

```java
@Service
public class UnifiedTokenService {
    
    // Generate token using configured method
    public String generateAccessToken(UUID userId, String username, String email, 
                                    Set<String> roles, Set<String> permissions) {
        // Auto-detects or uses configured token type
    }
    
    // Validate token with auto-detection
    public boolean validateToken(String token) {
        // Automatically detects JWT vs JWS
    }
}
```

---

## Client Headers Configuration

### Overview

The client headers configuration allows tenants to specify custom headers for outgoing HTTP requests, including client ID and secret headers.

### Configuration Options

| Field | Description | Default |
|-------|-------------|---------|
| `includeClientHeaders` | Whether to include client headers | `false` |
| `clientId` | Client identifier | - |
| `clientSecret` | Client secret | - |
| `clientIdHeaderName` | Header name for client ID | `X-Client-ID` |
| `clientSecretHeaderName` | Header name for client secret | `X-Client-Secret` |

### Implementation

```java
@Service
public class OutgoingHttpService {
    
    // Make HTTP request with tenant-specific headers
    public <T> ResponseEntity<T> makeRequest(String tenantId, HttpMethod method, 
                                           String url, Object requestBody, 
                                           Class<T> responseType) {
        // Get tenant auth configuration
        Optional<TenantAuthConfiguration> authConfig = 
            tenantAuthConfigurationService.getConfigurationForOutgoingCalls(tenantId);
        
        // Create headers with client credentials
        HttpHeaders headers = createHeaders(authConfig);
        
        // Make request with configured headers
        return restTemplate.exchange(url, method, new HttpEntity<>(requestBody, headers), responseType);
    }
}
```

---

## Tenant Authentication Configuration

### Database Schema

```sql
CREATE TABLE tenant_auth_configuration (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id VARCHAR(50) NOT NULL,
    auth_method VARCHAR(20) NOT NULL DEFAULT 'JWT',
    client_id VARCHAR(100),
    client_secret VARCHAR(500),
    client_id_header_name VARCHAR(100) DEFAULT 'X-Client-ID',
    client_secret_header_name VARCHAR(100) DEFAULT 'X-Client-Secret',
    auth_header_name VARCHAR(100) DEFAULT 'Authorization',
    auth_header_prefix VARCHAR(20) DEFAULT 'Bearer',
    token_endpoint VARCHAR(100),
    public_key_endpoint VARCHAR(100),
    jws_public_key VARCHAR(100),
    jws_algorithm VARCHAR(50) DEFAULT 'HS256',
    jws_issuer VARCHAR(100),
    is_active BOOLEAN NOT NULL DEFAULT true,
    include_client_headers BOOLEAN NOT NULL DEFAULT false,
    description VARCHAR(500),
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### Entity Model

```java
@Entity
@Table(name = "tenant_auth_configuration")
public class TenantAuthConfiguration {
    
    public enum AuthMethod {
        JWT("JSON Web Token"),
        JWS("JSON Web Signature"),
        OAUTH2("OAuth 2.0"),
        API_KEY("API Key"),
        BASIC("Basic Authentication");
    }
    
    // Entity fields and methods...
}
```

---

## Frontend Management Interface

### React Components

#### TenantAuthConfiguration Component

The main component for managing tenant authentication configuration:

```typescript
interface TenantAuthConfigurationProps {
  tenantId: string;
  onConfigurationChange?: () => void;
}

const TenantAuthConfigurationComponent: React.FC<TenantAuthConfigurationProps> = ({
  tenantId,
  onConfigurationChange
}) => {
  // Component implementation...
};
```

#### Features

- **Configuration Management**: Create, update, activate/deactivate configurations
- **Authentication Method Selection**: Choose between JWT, JWS, OAuth2, API Key, or Basic Auth
- **Client Headers Configuration**: Configure client ID and secret headers
- **JWS Settings**: Configure JWS algorithm, issuer, and public key
- **Endpoint Configuration**: Set token and public key endpoints
- **Real-time Validation**: Form validation and error handling

### UI Features

- **Tabbed Interface**: Organized configuration sections
- **Active Configuration Display**: Clear indication of active settings
- **Configuration Table**: List all configurations with status
- **Action Buttons**: Edit, activate, deactivate, delete operations
- **Form Validation**: Real-time validation and error messages

---

## API Endpoints

### Tenant Authentication Configuration API

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/tenant-auth-config` | Create or update configuration |
| GET | `/api/v1/tenant-auth-config/tenant/{tenantId}/active` | Get active configuration |
| GET | `/api/v1/tenant-auth-config/tenant/{tenantId}` | Get all configurations |
| GET | `/api/v1/tenant-auth-config/{id}` | Get configuration by ID |
| POST | `/api/v1/tenant-auth-config/{id}/activate` | Activate configuration |
| POST | `/api/v1/tenant-auth-config/{id}/deactivate` | Deactivate configuration |
| DELETE | `/api/v1/tenant-auth-config/{id}` | Delete configuration |
| GET | `/api/v1/tenant-auth-config/active` | Get all active configurations |
| GET | `/api/v1/tenant-auth-config/method/{authMethod}` | Get by auth method |
| GET | `/api/v1/tenant-auth-config/with-client-headers` | Get with client headers |
| GET | `/api/v1/tenant-auth-config/auth-methods` | Get available auth methods |

### Request/Response Examples

#### Create Configuration Request

```json
{
  "tenantId": "tenant-001",
  "authMethod": "JWS",
  "clientId": "client-123",
  "clientSecret": "secret-456",
  "clientIdHeaderName": "X-Client-ID",
  "clientSecretHeaderName": "X-Client-Secret",
  "includeClientHeaders": true,
  "jwsAlgorithm": "RS256",
  "jwsIssuer": "payment-engine",
  "description": "Production JWS configuration",
  "createdBy": "admin"
}
```

#### Configuration Response

```json
{
  "id": "uuid-123",
  "tenantId": "tenant-001",
  "authMethod": "JWS",
  "clientId": "client-123",
  "clientIdHeaderName": "X-Client-ID",
  "clientSecretHeaderName": "X-Client-Secret",
  "authHeaderName": "Authorization",
  "authHeaderPrefix": "Bearer",
  "jwsAlgorithm": "RS256",
  "jwsIssuer": "payment-engine",
  "isActive": true,
  "includeClientHeaders": true,
  "description": "Production JWS configuration",
  "createdBy": "admin",
  "createdAt": "2024-01-01T00:00:00Z",
  "updatedAt": "2024-01-01T00:00:00Z"
}
```

---

## Configuration Examples

### JWT Configuration

```json
{
  "tenantId": "tenant-001",
  "authMethod": "JWT",
  "includeClientHeaders": false,
  "description": "Standard JWT configuration"
}
```

### JWS Configuration

```json
{
  "tenantId": "tenant-002",
  "authMethod": "JWS",
  "jwsAlgorithm": "RS256",
  "jwsIssuer": "payment-engine",
  "jwsPublicKey": "-----BEGIN PUBLIC KEY-----\n...\n-----END PUBLIC KEY-----",
  "includeClientHeaders": true,
  "clientId": "client-456",
  "clientSecret": "secret-789",
  "description": "Enhanced JWS configuration"
}
```

### OAuth2 Configuration

```json
{
  "tenantId": "tenant-003",
  "authMethod": "OAUTH2",
  "tokenEndpoint": "https://auth.example.com/oauth/token",
  "clientId": "oauth-client-123",
  "clientSecret": "oauth-secret-456",
  "includeClientHeaders": true,
  "description": "OAuth2 configuration"
}
```

### API Key Configuration

```json
{
  "tenantId": "tenant-004",
  "authMethod": "API_KEY",
  "clientId": "api-key-789",
  "includeClientHeaders": true,
  "clientIdHeaderName": "X-API-Key",
  "description": "API key configuration"
}
```

---

## Security Considerations

### JWS Security Benefits

1. **Stronger Signatures**: JWS provides more robust cryptographic signatures
2. **Algorithm Flexibility**: Support for both symmetric and asymmetric algorithms
3. **Public Key Verification**: External systems can verify tokens without shared secrets
4. **Enhanced Integrity**: Better protection against token tampering

### Client Headers Security

1. **Secure Storage**: Client secrets are encrypted in the database
2. **Header Customization**: Configurable header names for security through obscurity
3. **Access Control**: Role-based access to configuration management
4. **Audit Logging**: All configuration changes are logged

### Best Practices

1. **Use Strong Algorithms**: Prefer RS256/RS384/RS512 for JWS
2. **Rotate Keys Regularly**: Implement key rotation policies
3. **Secure Storage**: Encrypt sensitive configuration data
4. **Access Control**: Implement proper authorization for configuration access
5. **Monitoring**: Monitor authentication failures and suspicious activity

---

## Migration Guide

### From JWT to JWS

1. **Update Configuration**: Change `authMethod` from `JWT` to `JWS`
2. **Configure Algorithm**: Set appropriate `jwsAlgorithm`
3. **Set Issuer**: Configure `jwsIssuer` if needed
4. **Update Public Key**: Set `jwsPublicKey` for external verification
5. **Test Integration**: Verify all systems work with JWS tokens

### Adding Client Headers

1. **Enable Headers**: Set `includeClientHeaders` to `true`
2. **Configure Credentials**: Set `clientId` and `clientSecret`
3. **Customize Headers**: Adjust header names if needed
4. **Test Outgoing Calls**: Verify headers are included in requests

### Step-by-Step Migration

```bash
# 1. Create new JWS configuration
curl -X POST /api/v1/tenant-auth-config \
  -H "Content-Type: application/json" \
  -d '{
    "tenantId": "tenant-001",
    "authMethod": "JWS",
    "jwsAlgorithm": "RS256",
    "includeClientHeaders": true,
    "clientId": "new-client-id",
    "clientSecret": "new-client-secret"
  }'

# 2. Activate new configuration
curl -X POST /api/v1/tenant-auth-config/{config-id}/activate \
  -d "updatedBy=admin"

# 3. Verify configuration
curl -X GET /api/v1/tenant-auth-config/tenant/tenant-001/active
```

---

## Troubleshooting

### Common Issues

#### JWS Token Validation Fails

**Problem**: JWS tokens are not validating correctly

**Solutions**:
1. Check algorithm configuration matches token algorithm
2. Verify public key is correctly configured
3. Ensure issuer matches token issuer
4. Check token format and structure

#### Client Headers Not Included

**Problem**: Client headers are not being added to outgoing requests

**Solutions**:
1. Verify `includeClientHeaders` is set to `true`
2. Check `clientId` and `clientSecret` are configured
3. Verify header names are correct
4. Check tenant configuration is active

#### Configuration Not Active

**Problem**: New configuration is not being used

**Solutions**:
1. Ensure configuration is activated
2. Check only one configuration per tenant is active
3. Verify tenant ID matches
4. Check configuration is not expired

### Debugging Steps

1. **Check Logs**: Review application logs for errors
2. **Verify Configuration**: Confirm configuration is correct
3. **Test Endpoints**: Use API endpoints to verify settings
4. **Check Database**: Verify data is stored correctly
5. **Monitor Network**: Check outgoing request headers

### Error Codes

| Code | Description | Solution |
|------|-------------|----------|
| 400 | Invalid configuration | Check request format and required fields |
| 404 | Configuration not found | Verify configuration exists and is accessible |
| 409 | Multiple active configurations | Deactivate other configurations first |
| 500 | Internal server error | Check logs and system status |

---

## Conclusion

The JWS and client headers implementation provides enhanced security and flexibility for the Payment Engine system. With configurable authentication methods and client headers, tenants can customize their authentication approach while maintaining security best practices.

The implementation includes:

- **Dual token support** (JWT and JWS)
- **Tenant-specific configuration**
- **Configurable client headers**
- **Comprehensive frontend management**
- **Robust API endpoints**
- **Security best practices**

This system enables secure, flexible authentication while providing the tools needed for effective management and monitoring.