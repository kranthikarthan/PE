# Security Architecture - Design Document

## Overview

This document provides the **comprehensive security architecture** for the Payments Engine. Security is paramount in a multi-tenant, multi-bank payments platform handling sensitive financial data. This design implements **defense-in-depth** with multiple security layers, ensuring protection against threats while maintaining regulatory compliance.

---

## Security Principles

### 1. Zero-Trust Security Model

```
Traditional Security (Perimeter-Based):
┌────────────────────────────────────────┐
│         Firewall (Perimeter)           │
│  ┌─────────────────────────────────┐   │
│  │  Inside = Trusted               │   │
│  │  Services trust each other      │   │  ❌ Problem: If breached,
│  │  No internal encryption         │   │     attacker has full access
│  └─────────────────────────────────┘   │
└────────────────────────────────────────┘

Zero-Trust Security (Never Trust):
┌────────────────────────────────────────┐
│  Every request authenticated           │
│  Every service-to-service call:        │
│  ├─ Mutual TLS (mTLS)                  │
│  ├─ JWT token validation               │
│  ├─ Authorization check                │
│  └─ Encrypted communication            │
│                                        │
│  ✅ Assume breach, minimize impact    │
└────────────────────────────────────────┘

Principles:
1. Never trust, always verify
2. Least privilege access
3. Assume breach, minimize blast radius
4. Encrypt everything
```

### 2. Defense-in-Depth (Layered Security)

```
7 Security Layers:

Layer 7: Application Security
         - Input validation
         - SQL injection prevention
         - XSS prevention
         - CSRF protection

Layer 6: Authentication & Authorization
         - Multi-factor authentication (MFA)
         - OAuth 2.0 / OIDC
         - Role-Based Access Control (RBAC)
         - JWT tokens

Layer 5: API Security
         - API Gateway rate limiting
         - API key management
         - Request signing
         - IP whitelisting

Layer 4: Network Security
         - Network policies (Kubernetes)
         - Service mesh (Istio mTLS)
         - Web Application Firewall (WAF)
         - DDoS protection (Azure Front Door)

Layer 3: Data Security
         - Encryption at rest (AES-256)
         - Encryption in transit (TLS 1.3)
         - Data masking
         - Row-Level Security (RLS)

Layer 2: Infrastructure Security
         - Container security (scanning)
         - Kubernetes security (RBAC, policies)
         - Secrets management (Key Vault)
         - Patch management

Layer 1: Physical Security
         - Azure data centers (physical)
         - Geo-redundancy
         - Disaster recovery

Each layer provides protection even if others fail ✅
```

### 3. Least Privilege Access

```
Principle: Grant minimum permissions needed

Examples:
- Service accounts: Read-only access to databases
- Developers: No production access
- Operators: Limited to operations, not data
- Customers: Only their own data (RLS)

Implementation:
- RBAC (Role-Based Access Control)
- Azure AD groups
- Kubernetes RBAC
- Database permissions
```

### 4. Security by Design

```
Security integrated at every stage:

Design Phase:
- Threat modeling
- Security requirements
- Compliance mapping

Development Phase:
- Secure coding standards
- SAST (Static Analysis)
- Dependency scanning

Testing Phase:
- Security testing
- Penetration testing
- Vulnerability scanning

Deployment Phase:
- Secrets in Key Vault
- Container scanning
- Infrastructure as Code review

Operations Phase:
- Security monitoring
- Incident response
- Regular audits
```

---

## Security Architecture Layers

### Layer 1: External Perimeter Security

```
┌─────────────────────────────────────────────────────────────┐
│                    INTERNET                                  │
└────────────────────────┬────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────┐
│             Azure Front Door (Global)                        │
│  - DDoS Protection (L7)                                      │
│  - Web Application Firewall (WAF)                            │
│  - SSL/TLS Termination (TLS 1.3)                            │
│  - Geographic routing                                        │
│  - Rate limiting (global)                                    │
└────────────────────────┬────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────┐
│          Azure Application Gateway (Regional)                │
│  - SSL/TLS Re-encryption                                     │
│  - WAF (OWASP Top 10)                                        │
│  - IP whitelisting                                           │
│  - Request filtering                                         │
└────────────────────────┬────────────────────────────────────┘
                         │
                         ▼
                  AKS Cluster (Private)
```

#### DDoS Protection

```
Azure Front Door DDoS Protection:
- Automatic detection and mitigation
- L3-L7 protection
- Capacity: 10+ Tbps
- Global anycast network

Rate Limiting:
- Per IP: 1000 requests/minute
- Per tenant: 10,000 requests/minute
- Global: 1,000,000 requests/minute

Response:
- Block malicious IPs (automatic)
- Challenge-response for suspicious traffic
- Geo-blocking (if needed)
```

#### Web Application Firewall (WAF)

```yaml
# WAF Configuration

waf_rules:
  # OWASP Top 10 Protection
  - rule: SQL Injection Prevention
    action: Block
    patterns:
      - "'; DROP TABLE"
      - "UNION SELECT"
      - "1=1--"
  
  - rule: Cross-Site Scripting (XSS)
    action: Block
    patterns:
      - "<script>"
      - "javascript:"
      - "onerror="
  
  - rule: Path Traversal
    action: Block
    patterns:
      - "../"
      - "..%2F"
  
  - rule: Command Injection
    action: Block
    patterns:
      - "; ls"
      - "| cat"
  
  # Custom Rules
  - rule: Block suspicious user agents
    action: Block
    patterns:
      - "sqlmap"
      - "nikto"
      - "nmap"
  
  - rule: Rate limit per IP
    action: Throttle
    threshold: 1000/minute
  
  - rule: Geo-blocking (if needed)
    action: Block
    countries: ["CN", "RU"]  # Example
```

---

### Layer 2: Network Security

#### Kubernetes Network Policies

```yaml
# Network Policy: Payment Service

apiVersion: networking.k8s.io/v1
kind: NetworkPolicy
metadata:
  name: payment-service-policy
  namespace: payments
spec:
  podSelector:
    matchLabels:
      app: payment-service
  
  policyTypes:
    - Ingress
    - Egress
  
  # Ingress: Who can call Payment Service?
  ingress:
    # Allow from API Gateway only
    - from:
        - podSelector:
            matchLabels:
              app: api-gateway
      ports:
        - protocol: TCP
          port: 8080
    
    # Allow from Web BFF
    - from:
        - podSelector:
            matchLabels:
              app: web-bff
      ports:
        - protocol: TCP
          port: 8080
  
  # Egress: What can Payment Service call?
  egress:
    # Allow to Validation Service
    - to:
        - podSelector:
            matchLabels:
              app: validation-service
      ports:
        - protocol: TCP
          port: 8080
    
    # Allow to Account Adapter
    - to:
        - podSelector:
            matchLabels:
              app: account-adapter
      ports:
        - protocol: TCP
          port: 8080
    
    # Allow to PostgreSQL
    - to:
        - podSelector:
            matchLabels:
              app: postgresql
      ports:
        - protocol: TCP
          port: 5432
    
    # Allow to Kafka
    - to:
        - podSelector:
            matchLabels:
              app: kafka
      ports:
        - protocol: TCP
          port: 9092
    
    # Allow DNS
    - to:
        - namespaceSelector:
            matchLabels:
              name: kube-system
      ports:
        - protocol: UDP
          port: 53
    
    # Block all other egress
```

**Result**: Payment Service can ONLY communicate with explicitly allowed services

#### Service Mesh Security (Istio mTLS)

```yaml
# Strict mTLS for all services

apiVersion: security.istio.io/v1beta1
kind: PeerAuthentication
metadata:
  name: default
  namespace: payments
spec:
  mtls:
    mode: STRICT  # Require mTLS for all traffic

---

# Authorization Policy: Only Payment Service can call Validation Service

apiVersion: security.istio.io/v1beta1
kind: AuthorizationPolicy
metadata:
  name: validation-service-authz
  namespace: payments
spec:
  selector:
    matchLabels:
      app: validation-service
  
  action: ALLOW
  
  rules:
    # Allow from Payment Service
    - from:
        - source:
            principals:
              - "cluster.local/ns/payments/sa/payment-service"
      to:
        - operation:
            methods: ["POST"]
            paths: ["/api/v1/validate/*"]
    
    # Allow from Saga Orchestrator
    - from:
        - source:
            principals:
              - "cluster.local/ns/payments/sa/saga-orchestrator"
      to:
        - operation:
            methods: ["POST"]
            paths: ["/api/v1/validate/*"]
  
  # Deny all other access (implicit)
```

**Benefits**:
- ✅ All service-to-service traffic encrypted (mTLS)
- ✅ Mutual authentication (both client and server verified)
- ✅ Certificate rotation automatic (no manual intervention)
- ✅ Zero-trust network (no service trusts another by default)

---

### Layer 3: Authentication & Authorization

#### Identity & Access Management Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                  AUTHENTICATION LAYER                        │
├─────────────────────────────────────────────────────────────┤
│                                                              │
│  ┌────────────────────────────────────────────────────────┐ │
│  │  Azure AD B2C (Customer Identity)                      │ │
│  │  - Multi-factor authentication (MFA)                   │ │
│  │  - Social login (Google, Facebook, etc.)              │ │
│  │  - SMS/Email OTP                                       │ │
│  │  - Biometric (face/fingerprint)                       │ │
│  │  - Password policies (12+ chars, complexity)          │ │
│  │  - Brute-force protection                             │ │
│  └────────────────────────────────────────────────────────┘ │
│                                                              │
│  ┌────────────────────────────────────────────────────────┐ │
│  │  Azure AD (Enterprise Identity)                        │ │
│  │  - SSO (Single Sign-On)                                │ │
│  │  - SAML/OIDC                                           │ │
│  │  - Conditional Access                                  │ │
│  │  - MFA (Microsoft Authenticator)                       │ │
│  └────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────┘
                              │
                              │ Issues JWT
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                  AUTHORIZATION LAYER                         │
├─────────────────────────────────────────────────────────────┤
│                                                              │
│  ┌────────────────────────────────────────────────────────┐ │
│  │  API Gateway (Kong)                                    │ │
│  │  - JWT validation                                      │ │
│  │  - Signature verification                              │ │
│  │  - Token expiry check                                  │ │
│  │  - Claim validation                                    │ │
│  └────────────────────────────────────────────────────────┘ │
│                                                              │
│  ┌────────────────────────────────────────────────────────┐ │
│  │  Authorization Service                                 │ │
│  │  - Role-Based Access Control (RBAC)                   │ │
│  │  - Attribute-Based Access Control (ABAC)              │ │
│  │  - Policy enforcement                                  │ │
│  └────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────┘
```

#### JWT Token Structure

```json
{
  "header": {
    "alg": "RS256",
    "typ": "JWT",
    "kid": "azure-ad-key-1"
  },
  "payload": {
    "iss": "https://login.microsoftonline.com/{tenant-id}",
    "sub": "user-12345",
    "aud": "payments-engine-api",
    "exp": 1704067200,
    "iat": 1704063600,
    "nbf": 1704063600,
    
    "tenant_id": "STD-001",
    "business_unit_id": "BU-001",
    "customer_id": "CUST-12345",
    
    "roles": [
      "payment.initiate",
      "payment.view",
      "account.view"
    ],
    
    "permissions": [
      "payment:create",
      "payment:read",
      "account:read"
    ],
    
    "mfa_completed": true,
    "risk_level": "low",
    
    "jti": "unique-token-id",
    "session_id": "session-12345"
  },
  "signature": "..."
}
```

#### Multi-Factor Authentication (MFA)

```
MFA Flow:

Step 1: User enters username + password
        ↓
Step 2: Azure AD B2C validates credentials
        ↓
Step 3: Trigger MFA challenge
        ├─ Option 1: SMS OTP (6-digit code)
        ├─ Option 2: Email OTP
        ├─ Option 3: Microsoft Authenticator push
        ├─ Option 4: TOTP (Time-based One-Time Password)
        └─ Option 5: Biometric (face/fingerprint)
        ↓
Step 4: User completes MFA challenge
        ↓
Step 5: Azure AD B2C issues JWT token
        ↓
Step 6: User authenticated ✅

MFA Requirements:
- Mandatory for: Payment initiation, account changes
- Optional for: View-only operations (configurable)
- Remember device: 30 days (configurable)
- Backup codes: 10 codes provided
```

#### Role-Based Access Control (RBAC)

```yaml
# RBAC Roles

roles:
  - name: customer
    description: Standard customer
    permissions:
      - payment:create (own account)
      - payment:read (own payments)
      - account:read (own accounts)
      - notification:read (own notifications)
    
    limits:
      - max_transaction: 50000 ZAR
      - daily_limit: 200000 ZAR
  
  - name: business_customer
    description: Business customer
    permissions:
      - payment:create (business accounts)
      - payment:read (business payments)
      - payment:approve (dual authorization)
      - account:read (business accounts)
      - report:generate
    
    limits:
      - max_transaction: 1000000 ZAR
      - daily_limit: 5000000 ZAR
  
  - name: bank_operator
    description: Bank operations team
    permissions:
      - payment:read (all tenants)
      - payment:reverse (with approval)
      - account:read (all)
      - report:generate
      - alert:acknowledge
    
    restrictions:
      - cannot_initiate_payments: true
      - audit_all_actions: true
  
  - name: bank_admin
    description: Bank administrator
    permissions:
      - tenant:configure
      - user:manage
      - limit:configure
      - report:all
    
    restrictions:
      - cannot_view_customer_data: true
      - audit_all_actions: true
      - require_approval: true
  
  - name: support_agent
    description: Customer support
    permissions:
      - customer:view (limited fields)
      - payment:view (status only)
      - notification:send
    
    restrictions:
      - no_pii_access: true
      - no_financial_details: true
      - audit_all_actions: true

# Permission Syntax: <resource>:<action>
# Examples: payment:create, account:read, user:delete
```

#### Attribute-Based Access Control (ABAC)

```
ABAC Policy Examples:

Policy 1: Time-Based Access
IF current_time BETWEEN 08:00 AND 18:00
   AND day_of_week NOT IN (Saturday, Sunday)
THEN allow payment:create
ELSE deny

Policy 2: IP-Based Access
IF user_ip IN whitelisted_ips
   OR user_ip IN corporate_network_range
THEN allow
ELSE require_additional_mfa

Policy 3: Amount-Based Authorization
IF payment_amount <= user.daily_limit
   AND payment_amount <= user.transaction_limit
THEN allow payment:create
ELSE require_approval

Policy 4: Risk-Based Access
IF risk_level == "high"
   OR payment_amount > 100000
   OR destination_country IN high_risk_countries
THEN require additional_verification
ELSE allow

Policy 5: Tenant-Based Isolation
IF user.tenant_id == resource.tenant_id
THEN allow
ELSE deny
```

---

### Layer 4: API Security

#### API Gateway Security

```
┌─────────────────────────────────────────────────────────────┐
│                  API Gateway (Kong)                          │
├─────────────────────────────────────────────────────────────┤
│                                                              │
│  ┌────────────────────────────────────────────────────────┐ │
│  │  1. Authentication                                     │ │
│  │     - JWT validation (signature, expiry)              │ │
│  │     - API key validation                              │ │
│  │     - OAuth 2.0 token introspection                   │ │
│  └────────────────────────────────────────────────────────┘ │
│                                                              │
│  ┌────────────────────────────────────────────────────────┐ │
│  │  2. Rate Limiting                                      │ │
│  │     - Per IP: 1000 req/min                            │ │
│  │     - Per user: 500 req/min                           │ │
│  │     - Per API key: 10000 req/min                      │ │
│  │     - Global: 1M req/min                              │ │
│  └────────────────────────────────────────────────────────┘ │
│                                                              │
│  ┌────────────────────────────────────────────────────────┐ │
│  │  3. Request Validation                                 │ │
│  │     - Schema validation (OpenAPI)                      │ │
│  │     - Input sanitization                               │ │
│  │     - Content-Type verification                        │ │
│  │     - Request size limits (1MB max)                    │ │
│  └────────────────────────────────────────────────────────┘ │
│                                                              │
│  ┌────────────────────────────────────────────────────────┐ │
│  │  4. IP Whitelisting                                    │ │
│  │     - Corporate IPs allowed                            │ │
│  │     - Partner bank IPs allowed                         │ │
│  │     - Geo-blocking (if needed)                        │ │
│  └────────────────────────────────────────────────────────┘ │
│                                                              │
│  ┌────────────────────────────────────────────────────────┐ │
│  │  5. Request/Response Logging                           │ │
│  │     - Audit trail (who, what, when)                   │ │
│  │     - PII masking in logs                             │ │
│  │     - Correlation ID injection                         │ │
│  └────────────────────────────────────────────────────────┘ │
│                                                              │
│  ┌────────────────────────────────────────────────────────┐ │
│  │  6. Security Headers                                   │ │
│  │     - X-Content-Type-Options: nosniff                 │ │
│  │     - X-Frame-Options: DENY                           │ │
│  │     - Strict-Transport-Security (HSTS)                │ │
│  │     - Content-Security-Policy                          │ │
│  └────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────┘
```

#### API Request Signing (For Partner Banks)

```
Request Signing Flow:

1. Partner Bank prepares request payload
   {
     "from_account": "12345678",
     "to_account": "87654321",
     "amount": 10000.00,
     "currency": "ZAR"
   }

2. Partner Bank computes HMAC signature
   canonical_string = METHOD + "\n" + 
                     PATH + "\n" + 
                     TIMESTAMP + "\n" + 
                     REQUEST_BODY
   
   signature = HMAC-SHA256(api_secret, canonical_string)

3. Partner Bank sends request with signature header
   POST /api/v1/payments
   Headers:
     Authorization: Bearer {jwt_token}
     X-API-Key: partner-bank-key-12345
     X-Timestamp: 2025-01-01T10:00:00Z
     X-Signature: a3f5d8e9b2c1...
   Body: {payment details}

4. API Gateway validates signature
   - Recompute signature using partner's secret (from Key Vault)
   - Compare computed signature with received signature
   - Check timestamp (must be within 5 minutes)
   - IF signature matches AND timestamp valid
     THEN allow request
     ELSE reject (401 Unauthorized)

Benefits:
- ✅ Request integrity (cannot be tampered)
- ✅ Non-repudiation (partner cannot deny sending)
- ✅ Replay attack prevention (timestamp check)
```

#### API Rate Limiting Strategy

```yaml
# Rate Limiting Configuration

rate_limits:
  # Per IP Address
  - type: ip
    limit: 1000
    window: 1 minute
    action: throttle
    burst: 1500
  
  # Per User (authenticated)
  - type: user
    limit: 500
    window: 1 minute
    action: throttle
    burst: 750
  
  # Per API Key (partner banks)
  - type: api_key
    tiers:
      - name: bronze
        limit: 1000
        window: 1 minute
      
      - name: silver
        limit: 5000
        window: 1 minute
      
      - name: gold
        limit: 10000
        window: 1 minute
      
      - name: platinum
        limit: 50000
        window: 1 minute
  
  # Per Endpoint (critical operations)
  - type: endpoint
    endpoints:
      - path: /api/v1/payments
        limit: 10000
        window: 1 minute
      
      - path: /api/v1/accounts
        limit: 20000
        window: 1 minute
  
  # Global (entire platform)
  - type: global
    limit: 1000000
    window: 1 minute
    action: queue

# Actions:
# - throttle: Slow down requests
# - reject: Return 429 Too Many Requests
# - queue: Queue requests for later processing
```

---

### Layer 5: Data Security

#### Encryption at Rest

```
Database Encryption (PostgreSQL):

┌─────────────────────────────────────────────────────────────┐
│             PostgreSQL (Azure Database)                      │
├─────────────────────────────────────────────────────────────┤
│                                                              │
│  ┌────────────────────────────────────────────────────────┐ │
│  │  Transparent Data Encryption (TDE)                     │ │
│  │  - Encryption algorithm: AES-256                       │ │
│  │  - Encryption key: Managed by Azure Key Vault         │ │
│  │  - Automatic key rotation: Every 90 days              │ │
│  │  - All data encrypted on disk                         │ │
│  └────────────────────────────────────────────────────────┘ │
│                                                              │
│  ┌────────────────────────────────────────────────────────┐ │
│  │  Column-Level Encryption (Sensitive Fields)            │ │
│  │                                                         │ │
│  │  CREATE TABLE customers (                              │ │
│  │    customer_id UUID PRIMARY KEY,                       │ │
│  │    name VARCHAR(255),                                  │ │
│  │    email VARCHAR(255),                                 │ │
│  │    id_number VARCHAR(255)                              │ │
│  │      ENCRYPTED WITH (                                  │ │
│  │        COLUMN_ENCRYPTION_KEY = CEK_IDNumber,           │ │
│  │        ENCRYPTION_TYPE = DETERMINISTIC,                │ │
│  │        ALGORITHM = 'AEAD_AES_256_CBC_HMAC_SHA_256'     │ │
│  │      ),                                                 │ │
│  │    bank_account VARCHAR(255)                           │ │
│  │      ENCRYPTED WITH (                                  │ │
│  │        COLUMN_ENCRYPTION_KEY = CEK_BankAccount,        │ │
│  │        ENCRYPTION_TYPE = DETERMINISTIC,                │ │
│  │        ALGORITHM = 'AEAD_AES_256_CBC_HMAC_SHA_256'     │ │
│  │      )                                                  │ │
│  │  );                                                     │ │
│  └────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────┘

Sensitive Fields Encrypted:
- Customer ID numbers
- Bank account numbers
- Credit card numbers (PCI-DSS)
- Passwords (hashed with bcrypt)
- API keys
- OAuth tokens
- Session tokens

Key Management:
- Keys stored in Azure Key Vault (HSM-backed)
- Key rotation: Every 90 days (automated)
- Key access audit: All access logged
- Multi-region key replication
```

#### Encryption in Transit

```
TLS Configuration:

┌─────────────────────────────────────────────────────────────┐
│              All Communication Encrypted                     │
├─────────────────────────────────────────────────────────────┤
│                                                              │
│  External Communication (Client ↔ API Gateway):             │
│  ├─ TLS 1.3 (preferred)                                     │
│  ├─ TLS 1.2 (minimum)                                       │
│  ├─ Certificate: Let's Encrypt / DigiCert                   │
│  ├─ Perfect Forward Secrecy (PFS)                           │
│  ├─ HSTS (HTTP Strict Transport Security)                   │
│  └─ Certificate pinning (mobile apps)                       │
│                                                              │
│  Internal Communication (Service ↔ Service):                │
│  ├─ mTLS (Mutual TLS via Istio)                            │
│  ├─ Certificate: Istio-issued (auto-rotated)               │
│  ├─ Mutual authentication (both ends verify)                │
│  └─ Certificate rotation: Every 24 hours (automatic)        │
│                                                              │
│  Database Communication (Service ↔ PostgreSQL):             │
│  ├─ TLS 1.2+                                                │
│  ├─ Certificate: Azure-managed                              │
│  └─ Verify server certificate                               │
│                                                              │
│  Kafka Communication (Service ↔ Kafka):                     │
│  ├─ TLS 1.2+                                                │
│  ├─ SASL/SCRAM authentication                               │
│  └─ Encryption in transit + at rest                         │
└─────────────────────────────────────────────────────────────┘

TLS Cipher Suites (Allowed):
- TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384
- TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256
- TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384
- TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256

TLS Cipher Suites (Blocked):
- All TLS 1.0 and TLS 1.1 ciphers (deprecated)
- All cipher suites without Perfect Forward Secrecy
- All cipher suites with known vulnerabilities
```

#### Data Masking & Tokenization

```sql
-- Data Masking for Non-Production Environments

-- Production Database
SELECT 
    customer_id,
    name,
    email,
    id_number,      -- "8001015009087"
    bank_account    -- "1234567890"
FROM customers;

-- Non-Production Database (Development, QA)
SELECT 
    customer_id,
    name,
    MASK_EMAIL(email) AS email,           -- "j***@example.com"
    MASK_ID_NUMBER(id_number) AS id_number,    -- "800101****087"
    MASK_ACCOUNT(bank_account) AS bank_account -- "123456****"
FROM customers;

-- Data Masking Functions
CREATE FUNCTION MASK_EMAIL(email VARCHAR) RETURNS VARCHAR AS $$
BEGIN
    RETURN SUBSTRING(email FROM 1 FOR 1) || '***@' || 
           SUBSTRING(email FROM POSITION('@' IN email) + 1);
END;
$$ LANGUAGE plpgsql;

CREATE FUNCTION MASK_ID_NUMBER(id_number VARCHAR) RETURNS VARCHAR AS $$
BEGIN
    RETURN SUBSTRING(id_number FROM 1 FOR 6) || '****' || 
           SUBSTRING(id_number FROM 11);
END;
$$ LANGUAGE plpgsql;

CREATE FUNCTION MASK_ACCOUNT(account VARCHAR) RETURNS VARCHAR AS $$
BEGIN
    RETURN SUBSTRING(account FROM 1 FOR 6) || '****';
END;
$$ LANGUAGE plpgsql;
```

```
Tokenization for Credit Cards (PCI-DSS):

Scenario: Store credit card for recurring payments

Step 1: Customer enters credit card
        Card Number: 4532-1234-5678-9010

Step 2: Tokenization Service (External PCI-DSS Vault)
        ├─ Receive card number
        ├─ Generate token: TOK-ABC123XYZ
        ├─ Store card number in secure vault
        └─ Return token to Payments Engine

Step 3: Payments Engine stores token (not card number)
        customers table:
        customer_id | name      | card_token
        12345       | John Doe  | TOK-ABC123XYZ

Step 4: Payment time
        ├─ Payments Engine sends token to Tokenization Service
        ├─ Tokenization Service returns real card number
        ├─ Payments Engine sends to payment gateway
        └─ Response returned

Benefits:
✅ Payments Engine never stores real card numbers (PCI-DSS compliance)
✅ Token is useless if database breached
✅ Tokenization Service is PCI-DSS Level 1 certified
```

#### Data Retention & Deletion

```
Data Retention Policy:

Category: Customer Data
- Active customers: Retain indefinitely
- Inactive customers (> 2 years): Archive to cold storage
- Deleted account: Soft delete, retain 7 years (compliance)
- Permanent deletion: After 7 years (automated)

Category: Transaction Data
- Recent transactions (< 1 year): Hot storage (PostgreSQL)
- Historical transactions (1-7 years): Warm storage (CosmosDB)
- Archived transactions (> 7 years): Cold storage (Azure Blob)
- Permanent deletion: Never (regulatory requirement)

Category: Audit Logs
- Retain: 7 years (compliance)
- Storage: CosmosDB (immutable)
- Access: Read-only after 30 days

Category: Session Data
- Retain: 30 days
- Automatic deletion: After 30 days

Category: Temporary Data (caches, etc.)
- Retain: 24 hours
- Automatic deletion: After 24 hours

Right to Erasure (GDPR/POPIA):
- Customer requests deletion
- Soft delete: Mark as deleted, anonymize PII
- Retain transaction records (regulatory requirement, anonymized)
- Delete all other data within 30 days
```

---

## Secrets Management

### Azure Key Vault Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                Azure Key Vault (HSM-Backed)                  │
├─────────────────────────────────────────────────────────────┤
│                                                              │
│  Secrets Stored:                                             │
│  ├─ Database connection strings                             │
│  ├─ API keys (external services)                            │
│  ├─ OAuth client secrets                                     │
│  ├─ Encryption keys                                          │
│  ├─ JWT signing keys                                         │
│  ├─ Kafka credentials                                        │
│  ├─ SMTP credentials                                         │
│  └─ Third-party service credentials                          │
│                                                              │
│  Access Control:                                             │
│  ├─ Managed Identity (services)                             │
│  ├─ Azure AD authentication                                  │
│  ├─ Least privilege access                                   │
│  └─ Audit all access (who, what, when)                      │
│                                                              │
│  Key Rotation:                                               │
│  ├─ Automatic: Every 90 days                                │
│  ├─ Manual: On-demand (if compromised)                      │
│  ├─ Versioning: Keep 3 previous versions                    │
│  └─ Zero-downtime rotation                                   │
│                                                              │
│  Backup & Recovery:                                          │
│  ├─ Geo-replicated (multi-region)                          │
│  ├─ Backup: Daily                                           │
│  ├─ Soft delete: 90-day retention                          │
│  └─ Purge protection: Enabled                               │
└─────────────────────────────────────────────────────────────┘
```

### Accessing Secrets (From Services)

```java
// Spring Boot Service accessing Key Vault

@Configuration
public class AzureKeyVaultConfig {
    
    @Bean
    public SecretClient secretClient() {
        // Use Managed Identity (no credentials in code)
        DefaultAzureCredential credential = new DefaultAzureCredentialBuilder()
            .build();
        
        return new SecretClientBuilder()
            .vaultUrl("https://payments-kv.vault.azure.net/")
            .credential(credential)
            .buildClient();
    }
}

@Service
public class PaymentService {
    
    @Autowired
    private SecretClient secretClient;
    
    public void processPayment() {
        // Retrieve database password from Key Vault
        KeyVaultSecret secret = secretClient.getSecret("database-password");
        String dbPassword = secret.getValue();
        
        // Use password to connect to database
        // Password is NEVER hard-coded ✅
    }
}
```

**Benefits**:
- ✅ No secrets in code (zero hard-coded passwords)
- ✅ No secrets in config files
- ✅ No secrets in environment variables (Kubernetes secrets)
- ✅ Centralized secret management
- ✅ Automatic rotation
- ✅ Audit trail (who accessed what)

---

## Compliance & Regulatory

### Regulatory Requirements

```
South Africa Financial Regulations:

1. POPIA (Protection of Personal Information Act)
   - Consent for data processing
   - Data subject rights (access, erasure)
   - Data breach notification (72 hours)
   - Data Protection Officer required
   - Our compliance:
     ✅ Consent management
     ✅ Data access APIs
     ✅ Data erasure process
     ✅ Breach notification process
     ✅ DPO appointed

2. FICA (Financial Intelligence Centre Act)
   - Know Your Customer (KYC)
   - Customer Due Diligence (CDD)
   - Suspicious transaction reporting
   - Record keeping (5 years)
   - Our compliance:
     ✅ KYC verification process
     ✅ CDD checks
     ✅ Transaction monitoring
     ✅ STR reporting
     ✅ 7-year record retention

3. PCI-DSS (Payment Card Industry Data Security Standard)
   - Requirement if storing/processing card data
   - Our approach: Tokenization (no card storage)
   ✅ PCI-DSS Level 1 compliant tokenization provider
   ✅ Never store card numbers
   ✅ Annual PCI audit

4. SARB (South African Reserve Bank) Regulations
   - Transaction reporting
   - Audit trail requirements
   - System availability (99.9%+)
   - Our compliance:
     ✅ Complete audit trail
     ✅ 99.99% uptime (design goal)
     ✅ Disaster recovery plan
```

### Audit Logging

```
Audit Log Structure:

Event: Payment Initiated
{
  "event_id": "evt-12345",
  "timestamp": "2025-01-01T10:00:00Z",
  "event_type": "payment.initiated",
  "severity": "INFO",
  
  "actor": {
    "user_id": "user-12345",
    "tenant_id": "STD-001",
    "ip_address": "41.xxx.xxx.xxx",
    "user_agent": "Mozilla/5.0...",
    "session_id": "session-12345"
  },
  
  "resource": {
    "type": "payment",
    "id": "PAY-12345",
    "action": "create"
  },
  
  "details": {
    "from_account": "****5678",      // Masked
    "to_account": "****1234",         // Masked
    "amount": 10000.00,
    "currency": "ZAR"
  },
  
  "result": {
    "status": "success",
    "http_status": 201,
    "response_time_ms": 145
  },
  
  "security": {
    "authentication_method": "JWT",
    "mfa_completed": true,
    "risk_level": "low"
  },
  
  "compliance": {
    "data_classification": "sensitive",
    "retention_period": "7_years"
  }
}

Audit Log Events (All Captured):
- Authentication (login, logout, failed login)
- Authorization (access granted, access denied)
- Data access (read, create, update, delete)
- Payment operations (initiate, approve, reverse)
- Configuration changes
- User management (create, update, delete)
- System events (startup, shutdown, errors)

Audit Log Storage:
- Storage: CosmosDB (immutable, append-only)
- Retention: 7 years
- Encryption: AES-256 at rest
- Access: Read-only for compliance team
- Integrity: Blockchain-based integrity check (optional)
```

---

## Security Monitoring & Incident Response

### Security Information and Event Management (SIEM)

```
┌─────────────────────────────────────────────────────────────┐
│             Azure Sentinel (SIEM)                            │
├─────────────────────────────────────────────────────────────┤
│                                                              │
│  Data Sources:                                               │
│  ├─ Azure AD logs (authentication)                          │
│  ├─ API Gateway logs                                         │
│  ├─ WAF logs                                                 │
│  ├─ Network security group logs                             │
│  ├─ Application logs (all 17 services)                      │
│  ├─ Database audit logs                                      │
│  ├─ Key Vault access logs                                    │
│  └─ Istio access logs                                        │
│                                                              │
│  Analytics Rules (Detect Threats):                           │
│  ├─ Brute force attacks (5 failed logins in 5 minutes)     │
│  ├─ Unusual access patterns (login from new country)        │
│  ├─ Privilege escalation attempts                           │
│  ├─ Data exfiltration (large data transfers)               │
│  ├─ SQL injection attempts                                   │
│  ├─ Suspicious API calls (unusual endpoints)                │
│  └─ Anomalous transaction patterns                          │
│                                                              │
│  Automated Responses:                                        │
│  ├─ Block IP (automatic)                                    │
│  ├─ Disable user account (automatic)                        │
│  ├─ Trigger MFA challenge                                    │
│  ├─ Alert security team (PagerDuty)                        │
│  └─ Create incident ticket (ServiceNow)                     │
└─────────────────────────────────────────────────────────────┘
```

### Security Alerts

```
Alert Categories:

Category: Authentication
├─ Alert: Multiple failed login attempts
│  Threshold: 5 failed logins in 5 minutes
│  Action: Block IP for 1 hour, notify security team
│
├─ Alert: Login from unusual location
│  Threshold: Login from country different from usual
│  Action: Trigger MFA challenge, notify user
│
└─ Alert: Impossible travel
   Threshold: Login from 2 locations > 500km apart < 1 hour
   Action: Require MFA, notify security team

Category: Data Access
├─ Alert: Mass data access
│  Threshold: Access > 1000 records in 1 hour
│  Action: Notify security team, create incident
│
├─ Alert: Sensitive data access
│  Threshold: Access to PII outside normal hours
│  Action: Require approval, audit
│
└─ Alert: Data export
   Threshold: Large data export (> 10MB)
   Action: Notify security team, log

Category: Transactions
├─ Alert: High-value transaction
│  Threshold: Transaction > 1M ZAR
│  Action: Require dual authorization
│
├─ Alert: Unusual transaction pattern
│  Threshold: Transaction frequency > 10x normal
│  Action: Fraud check, hold transaction
│
└─ Alert: Cross-border transaction
   Threshold: Transaction to high-risk country
   Action: Enhanced due diligence

Category: System
├─ Alert: Service failure
│  Threshold: Service unavailable > 5 minutes
│  Action: Notify operations team, failover
│
├─ Alert: Database connection failure
│  Threshold: Connection failures > 10 in 1 minute
│  Action: Check database, notify DBA
│
└─ Alert: Certificate expiry
   Threshold: Certificate expires in < 30 days
   Action: Renew certificate, notify operations
```

### Incident Response Plan

```
Incident Response Process:

Phase 1: Detection (0-15 minutes)
├─ Alert triggered (Azure Sentinel)
├─ Security team notified (PagerDuty)
├─ Initial assessment (severity level)
└─ Incident declared (if critical)

Phase 2: Containment (15 minutes - 1 hour)
├─ Isolate affected systems
├─ Block malicious IPs
├─ Disable compromised accounts
├─ Preserve evidence (logs, snapshots)
└─ Prevent further damage

Phase 3: Investigation (1-4 hours)
├─ Analyze logs (Azure Sentinel)
├─ Identify root cause
├─ Determine scope of impact
├─ Document findings
└─ Update stakeholders

Phase 4: Eradication (4-8 hours)
├─ Remove malware/backdoors
├─ Patch vulnerabilities
├─ Rotate compromised credentials
├─ Update security policies
└─ Verify clean state

Phase 5: Recovery (8-24 hours)
├─ Restore services (if taken offline)
├─ Verify data integrity
├─ Monitor for re-infection
├─ Return to normal operations
└─ Notify customers (if required)

Phase 6: Post-Incident (1-7 days)
├─ Post-mortem meeting
├─ Root cause analysis
├─ Lessons learned
├─ Update incident response plan
├─ Security improvements
└─ Compliance reporting (if required)

Incident Severity Levels:
- Critical (P0): Data breach, system down, financial loss
  Response time: Immediate (24/7)
  
- High (P1): Security vulnerability, partial outage
  Response time: < 1 hour
  
- Medium (P2): Security concern, degraded performance
  Response time: < 4 hours
  
- Low (P3): Minor issue, no immediate impact
  Response time: < 24 hours
```

---

## Security Testing

### Vulnerability Scanning

```
Continuous Security Scanning:

1. Dependency Scanning (Daily)
   Tool: Dependabot, Snyk
   Scans: Java dependencies, npm packages
   Action: Auto-create PR for vulnerable dependency updates
   
2. Container Image Scanning (On build)
   Tool: Trivy, Azure Defender for Container Registries
   Scans: Base images, application layers
   Action: Block deployment if critical vulnerabilities found
   
3. Code Scanning (On commit)
   Tool: SonarQube, Checkmarx
   Scans: SQL injection, XSS, hardcoded secrets
   Action: Fail build if critical issues found
   
4. Infrastructure Scanning (Daily)
   Tool: Azure Security Center
   Scans: Kubernetes misconfigurations, NSG rules
   Action: Alert security team, create remediation tickets
   
5. API Security Scanning (Weekly)
   Tool: OWASP ZAP, Burp Suite
   Scans: API endpoints, authentication, authorization
   Action: Create vulnerability reports
```

### Penetration Testing

```
Penetration Testing Schedule:

External Penetration Test:
- Frequency: Quarterly
- Scope: External APIs, Web BFF, Mobile BFF
- Tester: Third-party certified (CREST, OSCP)
- Report: Executive summary + detailed findings
- Remediation: Within 30 days (critical), 90 days (high)

Internal Penetration Test:
- Frequency: Bi-annually
- Scope: Internal services, databases, network
- Tester: Third-party certified
- Report: Detailed findings + proof of concepts
- Remediation: Within 30 days (critical), 90 days (high)

Red Team Exercise:
- Frequency: Annually
- Scope: Full attack simulation (social engineering, etc.)
- Tester: Third-party red team
- Report: Attack path, recommendations
- Remediation: Strategic security improvements

Bug Bounty Program (Optional):
- Platform: HackerOne, Bugcrowd
- Scope: External APIs, web/mobile apps
- Rewards: $100 - $10,000 (based on severity)
- Review: Security team reviews all submissions
```

---

## Security Best Practices

### Secure Coding Standards

```
Secure Coding Checklist:

Input Validation:
✅ Validate all user input (type, length, format)
✅ Whitelist allowed values (not blacklist)
✅ Reject invalid input (don't attempt to fix)
✅ Sanitize input before use

SQL Injection Prevention:
✅ Use prepared statements (ALWAYS)
✅ Never concatenate SQL with user input
✅ Parameterized queries only
✅ ORM frameworks (JPA, Hibernate)

Cross-Site Scripting (XSS) Prevention:
✅ Escape output (HTML, JavaScript, JSON)
✅ Content Security Policy (CSP)
✅ Use framework auto-escaping (React, Angular)
✅ Validate input (reject scripts)

Cross-Site Request Forgery (CSRF) Prevention:
✅ Use CSRF tokens (stateless)
✅ SameSite cookie attribute
✅ Verify Origin/Referer headers
✅ Re-authenticate for sensitive operations

Authentication & Session Management:
✅ Never store passwords in plain text (bcrypt)
✅ Use strong password policies (12+ chars)
✅ Implement account lockout (5 attempts)
✅ Session timeout (15 minutes inactivity)
✅ Secure session cookies (HttpOnly, Secure, SameSite)

Authorization:
✅ Implement least privilege
✅ Check authorization on every request
✅ Never trust client-side authorization
✅ Use RBAC/ABAC consistently

Error Handling:
✅ Don't expose stack traces to users
✅ Log errors securely (no PII in logs)
✅ Generic error messages to users
✅ Detailed logs for debugging

Secrets Management:
✅ Never hard-code secrets
✅ Use Azure Key Vault
✅ Rotate secrets regularly
✅ Audit secret access

Logging & Monitoring:
✅ Log security events (authentication, authorization)
✅ Mask sensitive data in logs (PII, passwords)
✅ Implement correlation IDs
✅ Monitor for anomalies
```

### Security Development Lifecycle (SDL)

```
SDL Process:

Phase 1: Requirements
├─ Identify security requirements
├─ Define security controls
├─ Compliance requirements (POPIA, PCI-DSS)
└─ Threat modeling

Phase 2: Design
├─ Security architecture review
├─ Data flow diagrams
├─ Attack surface analysis
└─ Security design patterns

Phase 3: Development
├─ Secure coding standards
├─ Code reviews (security focus)
├─ Static analysis (SAST)
└─ Dependency scanning

Phase 4: Testing
├─ Unit tests (security scenarios)
├─ Integration tests
├─ Security testing (DAST)
├─ Penetration testing
└─ Vulnerability scanning

Phase 5: Deployment
├─ Container scanning
├─ Infrastructure security review
├─ Secrets management
└─ Security configuration

Phase 6: Operations
├─ Security monitoring (SIEM)
├─ Incident response
├─ Security patching
└─ Regular audits
```

---

## Summary

### Security Architecture Highlights

✅ **Zero-Trust Security**: Never trust, always verify  
✅ **Defense-in-Depth**: 7 layers of security  
✅ **Encryption Everywhere**: At rest, in transit, in use  
✅ **Comprehensive Authentication**: MFA, OAuth 2.0, JWT  
✅ **Fine-Grained Authorization**: RBAC, ABAC policies  
✅ **API Security**: Rate limiting, request signing, validation  
✅ **Secrets Management**: Azure Key Vault (HSM-backed)  
✅ **Compliance**: POPIA, FICA, PCI-DSS ready  
✅ **Security Monitoring**: Azure Sentinel SIEM  
✅ **Incident Response**: 24/7 SOC, documented processes  
✅ **Continuous Security**: Scanning, testing, auditing  

### Implementation Effort

**Phase 1: Core Security** (2-3 weeks)
- Azure AD B2C setup
- API Gateway security
- Secrets management
- Network policies

**Phase 2: Advanced Security** (2-3 weeks)
- Istio mTLS deployment
- Encryption at rest
- SIEM setup
- Compliance controls

**Phase 3: Continuous Security** (Ongoing)
- Vulnerability scanning
- Penetration testing
- Security monitoring
- Incident response

**Total**: 4-6 weeks initial + ongoing

### Security Maturity

Your security architecture achieves:
- ✅ **Level 4: Optimized** (managed, monitored, continuously improving)
- ✅ **Industry Best Practices**: Zero-trust, defense-in-depth
- ✅ **Regulatory Compliance**: POPIA, FICA, PCI-DSS
- ✅ **Enterprise-Grade**: Multi-layered protection

**Verdict**: **Production-ready security architecture** suitable for handling **sensitive financial data** for **100+ banks** across **Africa**. 🔒 🏆

---

## Related Documents

- **[00-ARCHITECTURE-OVERVIEW.md](00-ARCHITECTURE-OVERVIEW.md)** - Overall architecture
- **[07-AZURE-INFRASTRUCTURE.md](07-AZURE-INFRASTRUCTURE.md)** - Azure infrastructure
- **[12-TENANT-MANAGEMENT.md](12-TENANT-MANAGEMENT.md)** - Multi-tenancy security
- **[17-SERVICE-MESH-ISTIO.md](17-SERVICE-MESH-ISTIO.md)** - mTLS security

---

**Last Updated**: 2025-10-11  
**Version**: 1.0  
**Classification**: Confidential
