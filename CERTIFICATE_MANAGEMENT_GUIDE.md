# Certificate Management System
## Multi-Service Automated Certificate and Key Management for Payment Engine

### Overview

The Certificate Management System provides comprehensive automated certificate and key management capabilities across all Payment Engine services, including:

- **Multi-Service Implementation**: Certificate management implemented across API Gateway, Core Banking, and Payment Processing services
- **Automated Certificate Generation**: Generate X.509 certificates with custom key usage and extended key usage
- **PFX Certificate Import**: Import and consume bank-generated trusted CA certificates in .pfx format
- **Certificate Validation**: Validate certificates against trusted CA certificates
- **Certificate Renewal**: Automated certificate renewal and lifecycle management
- **Certificate Rollback**: Rollback certificates to previous versions for disaster recovery
- **Certificate Rotation**: Automated certificate rotation and lifecycle management
- **Secure Storage**: Encrypted storage of certificates and private keys
- **Web-based Management**: React frontend for certificate management operations

---

## 🏗️ **Architecture Overview**

### **Multi-Service System Components**

```
┌─────────────────────────────────────────────────────────────┐
│                    React Frontend                           │
│  ┌─────────────────┐  ┌─────────────────┐  ┌──────────────┐ │
│  │   Certificate   │  │   Certificate   │  │ Certificate  │ │
│  │   Management    │  │   Generation    │  │   Import     │ │
│  │     Page        │  │     Dialog      │  │    Dialog    │ │
│  └─────────────────┘  └─────────────────┘  └──────────────┘ │
└─────────────────────────────────────────────────────────────┘
                                │
                                ▼
┌─────────────────────────────────────────────────────────────┐
│                 API Gateway Service                         │
│  ┌─────────────────┐  ┌─────────────────┐  ┌──────────────┐ │
│  │   Certificate   │  │   Certificate   │  │   Trusted    │ │
│  │   Management    │  │      Info       │  │ Certificate  │ │
│  │   Controller    │  │   Repository    │  │ Repository   │ │
│  └─────────────────┘  └─────────────────┘  └──────────────┘ │
└─────────────────────────────────────────────────────────────┘
                                │
                                ▼
┌─────────────────────────────────────────────────────────────┐
│                 Core Banking Service                        │
│  ┌─────────────────┐  ┌─────────────────┐  ┌──────────────┐ │
│  │   Certificate   │  │   Certificate   │  │   Trusted    │ │
│  │   Management    │  │      Info       │  │ Certificate  │ │
│  │   Controller    │  │   Repository    │  │ Repository   │ │
│  └─────────────────┘  └─────────────────┘  └──────────────┘ │
└─────────────────────────────────────────────────────────────┘
                                │
                                ▼
┌─────────────────────────────────────────────────────────────┐
│              Payment Processing Service                     │
│  ┌─────────────────┐  ┌─────────────────┐  ┌──────────────┐ │
│  │   Certificate   │  │   Certificate   │  │   Trusted    │ │
│  │   Management    │  │      Info       │  │ Certificate  │ │
│  │   Controller    │  │   Repository    │  │ Repository   │ │
│  └─────────────────┘  └─────────────────┘  └──────────────┘ │
└─────────────────────────────────────────────────────────────┘
                                │
                                ▼
┌─────────────────────────────────────────────────────────────┐
│                    Database Layer                           │
│  ┌─────────────────┐  ┌─────────────────┐  ┌──────────────┐ │
│  │   Certificate   │  │   Trusted       │  │   File       │ │
│  │      Info       │  │  Certificate    │  │  Storage     │ │
│  │     Table       │  │     Table       │  │  System      │ │
│  └─────────────────┘  └─────────────────┘  └──────────────┘ │
└─────────────────────────────────────────────────────────────┘
```

### **Key Features**

1. **Multi-Service Certificate Management**
   - **API Gateway Service**: SSL/TLS certificates for secure communication and client certificate validation
   - **Core Banking Service**: Banking-specific certificates for external system communication
   - **Payment Processing Service**: Centralized certificate management and coordination

2. **Certificate Generation**
   - Self-signed X.509 certificates
   - Custom key usage and extended key usage
   - Configurable validity periods
   - RSA key generation (2048-bit default)
   - Service-specific certificate types (GATEWAY_SSL, BANKING_SSL, etc.)

3. **PFX Import**
   - Import .pfx and .p12 certificate files
   - Extract certificate chains
   - Password-protected import
   - Automatic validation
   - Service-specific import handling

4. **Certificate Validation**
   - Trust chain verification
   - Certificate expiration checking
   - Signature validation
   - Trusted CA validation
   - Cross-service validation

5. **Certificate Renewal**
   - Automated renewal process
   - Zero-downtime renewal
   - Service-specific renewal policies
   - Audit trail

6. **Certificate Rollback**
   - Rollback to previous certificate versions
   - Disaster recovery capabilities
   - Service-specific rollback policies
   - Audit trail

7. **Certificate Rotation**
   - Automated rotation process
   - Zero-downtime rotation
   - Rollback capabilities
   - Audit trail

8. **Secure Storage**
   - Encrypted file storage per service
   - Database metadata storage
   - Access control
   - Audit logging

---

## 🔧 **Backend Implementation**

### **Core Service Classes**

#### **CertificateManagementService**
```java
@Service
@Transactional
public class CertificateManagementService {
    
    // Generate new certificate and private key pair
    public CertificateGenerationResult generateCertificate(CertificateGenerationRequest request);
    
    // Import and consume .pfx certificate file
    public PfxImportResult importPfxCertificate(MultipartFile pfxFile, PfxImportRequest request);
    
    // Validate certificate against trusted CA certificates
    public CertificateValidationResult validateCertificate(String certificateId);
    
    // Rotate certificate (generate new one and mark old as expired)
    public CertificateGenerationResult rotateCertificate(String certificateId, CertificateGenerationRequest request);
    
    // Get certificates expiring soon
    public List<CertificateInfo> getExpiringCertificates(int daysAhead);
}
```

#### **Certificate Entities**

**CertificateInfo Entity**
```java
@Entity
@Table(name = "certificate_info", schema = "payment_engine")
public class CertificateInfo {
    @Id
    private UUID id;
    private String subjectDN;
    private String issuerDN;
    private String serialNumber;
    private LocalDateTime validFrom;
    private LocalDateTime validTo;
    private String publicKeyAlgorithm;
    private Integer keySize;
    private String signatureAlgorithm;
    private String certificateType;
    private String tenantId;
    private CertificateStatus status;
    private ValidationStatus validationStatus;
    // ... additional fields
}
```

**TrustedCertificate Entity**
```java
@Entity
@Table(name = "trusted_certificates", schema = "payment_engine")
public class TrustedCertificate {
    @Id
    private UUID id;
    private String alias;
    private String subjectDN;
    private String issuerDN;
    private String serialNumber;
    private LocalDateTime validFrom;
    private LocalDateTime validTo;
    private String certificateType;
    private String tenantId;
    private TrustedCertificateStatus status;
    // ... additional fields
}
```

### **Multi-Service REST API Endpoints**

#### **API Gateway Service Endpoints**

**Certificate Generation**
```http
POST /api/v1/gateway/certificates/generate
Content-Type: application/json

{
  "subjectDN": "CN=gateway.example.com, O=Organization, C=US",
  "tenantId": "tenant-123",
  "certificateType": "GATEWAY_SSL",
  "validityDays": 365,
  "keyUsage": ["digitalSignature", "keyEncipherment"],
  "extendedKeyUsage": ["serverAuth", "clientAuth"],
  "description": "API Gateway SSL Certificate"
}
```

**Client Certificate Import**
```http
POST /api/v1/gateway/certificates/import/pfx
Content-Type: multipart/form-data

file: [PFX_FILE]
password: "pfx_password"
tenantId: "tenant-123"
certificateType: "CLIENT_CERT"
description: "Client Certificate"
```

**Certificate Renewal**
```http
POST /api/v1/gateway/certificates/{certificateId}/renew
Content-Type: application/json

{
  "subjectDN": "CN=gateway.example.com, O=Organization, C=US",
  "tenantId": "tenant-123",
  "certificateType": "GATEWAY_SSL",
  "validityDays": 365
}
```

**Certificate Rollback**
```http
POST /api/v1/gateway/certificates/{certificateId}/rollback
```

#### **Core Banking Service Endpoints**

**Certificate Generation**
```http
POST /api/v1/core-banking/certificates/generate
Content-Type: application/json

{
  "subjectDN": "CN=banking.example.com, O=Organization, C=US",
  "tenantId": "tenant-123",
  "certificateType": "BANKING_SSL",
  "validityDays": 365,
  "keyUsage": ["digitalSignature", "keyEncipherment", "nonRepudiation"],
  "extendedKeyUsage": ["serverAuth", "clientAuth"],
  "description": "Core Banking SSL Certificate"
}
```

**Banking Certificate Import**
```http
POST /api/v1/core-banking/certificates/import/pfx
Content-Type: multipart/form-data

file: [PFX_FILE]
password: "pfx_password"
tenantId: "tenant-123"
certificateType: "BANKING_CERT"
description: "Bank Certificate"
```

**Certificate Renewal**
```http
POST /api/v1/core-banking/certificates/{certificateId}/renew
Content-Type: application/json

{
  "subjectDN": "CN=banking.example.com, O=Organization, C=US",
  "tenantId": "tenant-123",
  "certificateType": "BANKING_SSL",
  "validityDays": 365
}
```

**Certificate Rollback**
```http
POST /api/v1/core-banking/certificates/{certificateId}/rollback
```

#### **Payment Processing Service Endpoints**

**Certificate Generation**
```http
POST /api/v1/certificates/generate
Content-Type: application/json

{
  "subjectDN": "CN=payment-processing.example.com, O=Organization, C=US",
  "tenantId": "tenant-123",
  "certificateType": "GENERATED",
  "validityDays": 365,
  "keyUsage": ["digitalSignature", "keyEncipherment"],
  "extendedKeyUsage": ["serverAuth", "clientAuth"],
  "description": "Payment Processing Certificate"
}
```

**PFX Import**
```http
POST /api/v1/certificates/import/pfx
Content-Type: multipart/form-data

file: [PFX_FILE]
password: "pfx_password"
tenantId: "tenant-123"
certificateType: "PFX_IMPORTED"
description: "Bank Certificate"
```

**Certificate Validation**
```http
POST /api/v1/certificates/{certificateId}/validate
```

**Certificate Renewal**
```http
POST /api/v1/certificates/{certificateId}/renew
Content-Type: application/json

{
  "subjectDN": "CN=payment-processing.example.com, O=Organization, C=US",
  "tenantId": "tenant-123",
  "certificateType": "GENERATED",
  "validityDays": 365
}
```

**Certificate Rollback**
```http
POST /api/v1/certificates/{certificateId}/rollback
```

**Certificate Rotation**
```http
POST /api/v1/certificates/{certificateId}/rotate
Content-Type: application/json

{
  "subjectDN": "CN=payment-processing.example.com, O=Organization, C=US",
  "tenantId": "tenant-123",
  "certificateType": "GENERATED",
  "validityDays": 365
}
```

---

## 🏢 **Multi-Service Implementation Summary**

### **Service-Specific Certificate Management**

#### **API Gateway Service**
- **Purpose**: SSL/TLS certificates for secure communication and client certificate validation
- **Certificate Types**: `GATEWAY_SSL`, `CLIENT_CERT`
- **Storage Path**: `/app/certificates/gateway`
- **Key Features**: 
  - Gateway-specific SSL certificate generation
  - Client certificate import and validation
  - Certificate renewal and rollback
  - Statistics and monitoring

#### **Core Banking Service**
- **Purpose**: Banking-specific certificates for external system communication
- **Certificate Types**: `BANKING_SSL`, `BANKING_CERT`
- **Storage Path**: `/app/certificates/core-banking`
- **Key Features**:
  - Banking-specific SSL certificate generation
  - External banking system certificate import
  - Certificate renewal and rollback
  - Banking compliance features

#### **Payment Processing Service**
- **Purpose**: Centralized certificate management and coordination for payment processing
- **Certificate Types**: `GENERATED`, `PFX_IMPORTED`, `CLIENT`, `SERVER`, `CA`
- **Storage Path**: `/app/certificates`
- **Key Features**:
  - Centralized certificate management
  - PFX certificate import and validation
  - Certificate rotation, renewal, and rollback
  - Trusted certificate management
  - Comprehensive audit and monitoring

### **Cross-Service Features**

#### **Certificate Lifecycle Management**
- **Generation**: All services support certificate generation with service-specific configurations
- **Import**: All services support PFX certificate import with validation
- **Validation**: All services support certificate validation against trusted CAs
- **Renewal**: All services support certificate renewal with zero downtime
- **Rollback**: All services support certificate rollback for disaster recovery
- **Rotation**: Payment Processing service provides centralized rotation capabilities

#### **Security Features**
- **Encrypted Storage**: All services use encrypted file storage for certificates
- **Access Control**: Role-based access control across all services
- **Audit Logging**: Comprehensive audit logging for all certificate operations
- **Validation**: Cross-service certificate validation capabilities

#### **Monitoring and Alerting**
- **Expiration Monitoring**: All services monitor certificate expiration
- **Statistics**: Service-specific certificate statistics and dashboards
- **Alerting**: Automated alerts for certificate expiration and validation failures

---

## 🎨 **Frontend Implementation**

### **React Components**

#### **CertificateManagement**
Main certificate management page with:
- Certificate listing and filtering
- Statistics dashboard
- Action buttons for generation and import
- Tabbed interface for different views

#### **CertificateGenerationDialog**
Dialog for generating new certificates:
- Form for certificate parameters
- Key usage and extended key usage selection
- Download options for generated certificates
- PEM format display and copy functionality

#### **CertificateImportDialog**
Dialog for importing PFX certificates:
- Drag-and-drop file upload
- Password input for PFX files
- Import options configuration
- Certificate details display after import

#### **CertificateDetailsDialog**
Dialog for viewing certificate details:
- Comprehensive certificate information
- Validation status and results
- Technical details and history
- Action buttons for validation and rotation

#### **CertificateList**
Table component for displaying certificates:
- Sortable and filterable columns
- Status indicators and icons
- Action menus for each certificate
- Expiration warnings

### **TypeScript Types**

```typescript
export interface CertificateInfo {
  id: string;
  subjectDN: string;
  issuerDN: string;
  serialNumber: string;
  validFrom: string;
  validTo: string;
  publicKeyAlgorithm: string;
  keySize?: number;
  signatureAlgorithm: string;
  certificateType: string;
  tenantId: string;
  status: 'ACTIVE' | 'INACTIVE' | 'EXPIRED' | 'ROTATED' | 'REVOKED';
  validationStatus?: 'VALID' | 'INVALID' | 'PENDING' | 'EXPIRED' | 'REVOKED';
  // ... additional fields
}

export interface CertificateGenerationRequest {
  subjectDN: string;
  tenantId?: string;
  certificateType?: string;
  validityDays?: number;
  keyUsage?: string[];
  extendedKeyUsage?: string[];
  description?: string;
  includePrivateKey?: boolean;
  includePublicKey?: boolean;
}

export interface PfxImportRequest {
  password: string;
  tenantId?: string;
  certificateType?: string;
  description?: string;
  validateCertificate?: boolean;
  extractPrivateKey?: boolean;
  extractCertificateChain?: boolean;
}
```

### **Service Layer**

```typescript
export const certificateService = {
  // Generate a new certificate and private key pair
  generateCertificate: async (request: CertificateGenerationRequest): Promise<CertificateGenerationResult>;
  
  // Import a .pfx certificate file
  importPfxCertificate: async (file: File, request: PfxImportRequest): Promise<PfxImportResult>;
  
  // Validate a certificate
  validateCertificate: async (certificateId: string): Promise<CertificateValidationResult>;
  
  // Get all certificates with optional filtering
  getAllCertificates: async (filter?: CertificateFilter): Promise<CertificateInfo[]>;
  
  // Delete certificate
  deleteCertificate: async (certificateId: string): Promise<void>;
  
  // Rotate certificate
  rotateCertificate: async (certificateId: string, request: CertificateGenerationRequest): Promise<CertificateGenerationResult>;
  
  // Get certificates expiring soon
  getExpiringCertificates: async (daysAhead: number): Promise<CertificateInfo[]>;
  
  // Download certificate as PEM file
  downloadCertificatePem: (certificate: CertificateInfo, certificatePem: string): void;
  
  // Download private key as PEM file
  downloadPrivateKeyPem: (certificate: CertificateInfo, privateKeyPem: string): void;
  
  // Copy text to clipboard
  copyToClipboard: async (text: string): Promise<void>;
};
```

---

## 🔐 **Security Features**

### **Certificate Storage**
- **File System**: Certificates stored in encrypted format
- **Database**: Metadata stored with proper indexing
- **Access Control**: Role-based access to certificate operations
- **Audit Logging**: All operations logged for compliance

### **Key Management**
- **Private Key Protection**: Private keys encrypted at rest
- **Key Rotation**: Automated key rotation with zero downtime
- **Key Backup**: Secure backup and recovery procedures
- **Key Destruction**: Secure key destruction when certificates expire

### **Validation and Trust**
- **Trust Chain**: Certificate validation against trusted CA certificates
- **Revocation Checking**: OCSP and CRL support
- **Expiration Monitoring**: Automated expiration alerts
- **Trust Store Management**: Centralized trusted certificate management

---

## 📊 **Database Schema**

### **Certificate Info Table**
```sql
CREATE TABLE payment_engine.certificate_info (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    subject_dn VARCHAR(500) NOT NULL,
    issuer_dn VARCHAR(500) NOT NULL,
    serial_number VARCHAR(100) NOT NULL,
    valid_from TIMESTAMP NOT NULL,
    valid_to TIMESTAMP NOT NULL,
    public_key_algorithm VARCHAR(50),
    key_size INTEGER,
    signature_algorithm VARCHAR(50),
    certificate_type VARCHAR(50),
    tenant_id VARCHAR(100),
    status VARCHAR(20) DEFAULT 'ACTIVE',
    alias VARCHAR(100),
    validation_status VARCHAR(20),
    validation_message VARCHAR(500),
    last_validated TIMESTAMP,
    rotated_to UUID,
    rotated_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### **Trusted Certificate Table**
```sql
CREATE TABLE payment_engine.trusted_certificates (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    alias VARCHAR(100) NOT NULL,
    subject_dn VARCHAR(500) NOT NULL,
    issuer_dn VARCHAR(500) NOT NULL,
    serial_number VARCHAR(100) NOT NULL,
    valid_from TIMESTAMP NOT NULL,
    valid_to TIMESTAMP NOT NULL,
    public_key_algorithm VARCHAR(50),
    key_size INTEGER,
    signature_algorithm VARCHAR(50),
    certificate_type VARCHAR(50) DEFAULT 'TRUSTED_CA',
    tenant_id VARCHAR(100),
    status VARCHAR(20) DEFAULT 'ACTIVE',
    description VARCHAR(500),
    source VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### **Indexes**
```sql
-- Certificate Info Indexes
CREATE INDEX idx_certificate_info_tenant_id ON payment_engine.certificate_info(tenant_id);
CREATE INDEX idx_certificate_info_status ON payment_engine.certificate_info(status);
CREATE INDEX idx_certificate_info_type ON payment_engine.certificate_info(certificate_type);
CREATE INDEX idx_certificate_info_valid_to ON payment_engine.certificate_info(valid_to);
CREATE INDEX idx_certificate_info_subject_dn ON payment_engine.certificate_info(subject_dn);
CREATE INDEX idx_certificate_info_serial_number ON payment_engine.certificate_info(serial_number);

-- Trusted Certificate Indexes
CREATE INDEX idx_trusted_certificates_alias ON payment_engine.trusted_certificates(alias);
CREATE INDEX idx_trusted_certificates_tenant_id ON payment_engine.trusted_certificates(tenant_id);
CREATE INDEX idx_trusted_certificates_status ON payment_engine.trusted_certificates(status);
CREATE INDEX idx_trusted_certificates_valid_to ON payment_engine.trusted_certificates(valid_to);
CREATE INDEX idx_trusted_certificates_subject_dn ON payment_engine.trusted_certificates(subject_dn);
```

---

## 🚀 **Usage Examples**

### **Generate a New Certificate**

#### **Backend (Java)**
```java
CertificateGenerationRequest request = new CertificateGenerationRequest();
request.setSubjectDN("CN=payment-engine.com, O=Payment Engine, C=US");
request.setTenantId("tenant-123");
request.setCertificateType("GENERATED");
request.setValidityDays(365);
request.setKeyUsage(Arrays.asList("digitalSignature", "keyEncipherment"));
request.setExtendedKeyUsage(Arrays.asList("serverAuth", "clientAuth"));
request.setDescription("Payment Engine SSL Certificate");

CertificateGenerationResult result = certificateManagementService.generateCertificate(request);
```

#### **Frontend (React)**
```typescript
const handleGenerateCertificate = async () => {
  const request: CertificateGenerationRequest = {
    subjectDN: "CN=payment-engine.com, O=Payment Engine, C=US",
    tenantId: "tenant-123",
    certificateType: "GENERATED",
    validityDays: 365,
    keyUsage: ["digitalSignature", "keyEncipherment"],
    extendedKeyUsage: ["serverAuth", "clientAuth"],
    description: "Payment Engine SSL Certificate"
  };

  try {
    const result = await certificateService.generateCertificate(request);
    console.log('Certificate generated:', result);
  } catch (error) {
    console.error('Failed to generate certificate:', error);
  }
};
```

### **Import a PFX Certificate**

#### **Backend (Java)**
```java
MultipartFile pfxFile = // ... get uploaded file
PfxImportRequest request = new PfxImportRequest();
request.setPassword("pfx_password");
request.setTenantId("tenant-123");
request.setCertificateType("PFX_IMPORTED");
request.setDescription("Bank Certificate");
request.setValidateCertificate(true);
request.setExtractPrivateKey(true);
request.setExtractCertificateChain(true);

PfxImportResult result = certificateManagementService.importPfxCertificate(pfxFile, request);
```

#### **Frontend (React)**
```typescript
const handleImportPfx = async (file: File) => {
  const request: PfxImportRequest = {
    password: "pfx_password",
    tenantId: "tenant-123",
    certificateType: "PFX_IMPORTED",
    description: "Bank Certificate",
    validateCertificate: true,
    extractPrivateKey: true,
    extractCertificateChain: true
  };

  try {
    const result = await certificateService.importPfxCertificate(file, request);
    console.log('PFX imported:', result);
  } catch (error) {
    console.error('Failed to import PFX:', error);
  }
};
```

### **Validate a Certificate**

#### **Backend (Java)**
```java
String certificateId = "certificate-uuid";
CertificateValidationResult result = certificateManagementService.validateCertificate(certificateId);

if (result.isValid()) {
    System.out.println("Certificate is valid: " + result.getMessage());
} else {
    System.err.println("Certificate validation failed: " + result.getMessage());
}
```

#### **Frontend (React)**
```typescript
const handleValidateCertificate = async (certificateId: string) => {
  try {
    const result = await certificateService.validateCertificate(certificateId);
    if (result.isValid) {
      console.log('Certificate is valid:', result.message);
    } else {
      console.error('Certificate validation failed:', result.message);
    }
  } catch (error) {
    console.error('Failed to validate certificate:', error);
  }
};
```

---

## 🔄 **Certificate Lifecycle Management**

### **Certificate States**

1. **ACTIVE**: Certificate is valid and in use
2. **INACTIVE**: Certificate is valid but not in use
3. **EXPIRED**: Certificate has passed its validity period
4. **ROTATED**: Certificate has been replaced by a new one
5. **REVOKED**: Certificate has been revoked

### **Validation States**

1. **VALID**: Certificate passes all validation checks
2. **INVALID**: Certificate fails validation
3. **PENDING**: Certificate validation is in progress
4. **EXPIRED**: Certificate has expired
5. **REVOKED**: Certificate has been revoked

### **Rotation Process**

1. **Generate New Certificate**: Create new certificate with same parameters
2. **Update Application**: Deploy new certificate to application
3. **Mark Old as Rotated**: Update old certificate status
4. **Cleanup**: Remove old certificate after grace period

---

## 📈 **Monitoring and Alerting**

### **Key Metrics**

- **Certificate Count**: Total number of certificates by type and status
- **Expiration Alerts**: Certificates expiring within 30 days
- **Validation Status**: Success/failure rates for certificate validation
- **Rotation Events**: Certificate rotation frequency and success rates

### **Alerting Rules**

```yaml
# Certificate Expiration Alert
- alert: CertificateExpiringSoon
  expr: certificate_expiry_days < 30
  for: 1h
  labels:
    severity: warning
  annotations:
    summary: "Certificate expiring soon"
    description: "Certificate {{ $labels.certificate_id }} expires in {{ $value }} days"

# Certificate Validation Failure
- alert: CertificateValidationFailed
  expr: certificate_validation_failures > 0
  for: 5m
  labels:
    severity: critical
  annotations:
    summary: "Certificate validation failed"
    description: "Certificate {{ $labels.certificate_id }} validation failed"
```

---

## 🛠️ **Configuration**

### **Application Properties**

```yaml
# Certificate Management Configuration
certificate:
  storage:
    path: /app/certificates
  default:
    validity:
      days: 365
  key:
    size: 2048
  signature:
    algorithm: SHA256withRSA

# Security Configuration
security:
  certificate:
    encryption:
      enabled: true
      algorithm: AES-256-GCM
    access:
      control:
        enabled: true
        roles:
          - certificate:admin
          - certificate:user
          - certificate:readonly
```

### **Environment Variables**

```bash
# Certificate Storage
CERTIFICATE_STORAGE_PATH=/app/certificates
CERTIFICATE_DEFAULT_VALIDITY_DAYS=365
CERTIFICATE_KEY_SIZE=2048
CERTIFICATE_SIGNATURE_ALGORITHM=SHA256withRSA

# Security
CERTIFICATE_ENCRYPTION_ENABLED=true
CERTIFICATE_ACCESS_CONTROL_ENABLED=true
```

---

## 🧪 **Testing**

### **Unit Tests**

```java
@ExtendWith(MockitoExtension.class)
class CertificateManagementServiceTest {
    
    @Mock
    private CertificateInfoRepository certificateInfoRepository;
    
    @Mock
    private TrustedCertificateRepository trustedCertificateRepository;
    
    @InjectMocks
    private CertificateManagementService certificateManagementService;
    
    @Test
    void testGenerateCertificate() {
        // Test certificate generation
        CertificateGenerationRequest request = new CertificateGenerationRequest();
        request.setSubjectDN("CN=test.com, O=Test, C=US");
        request.setValidityDays(365);
        
        CertificateGenerationResult result = certificateManagementService.generateCertificate(request);
        
        assertNotNull(result);
        assertEquals("CN=test.com, O=Test, C=US", result.getSubjectDN());
        assertNotNull(result.getCertificatePem());
        assertNotNull(result.getPrivateKeyPem());
    }
    
    @Test
    void testImportPfxCertificate() {
        // Test PFX import
        MultipartFile pfxFile = createMockPfxFile();
        PfxImportRequest request = new PfxImportRequest();
        request.setPassword("test_password");
        
        PfxImportResult result = certificateManagementService.importPfxCertificate(pfxFile, request);
        
        assertNotNull(result);
        assertNotNull(result.getCertificatePem());
        assertNotNull(result.getPrivateKeyPem());
    }
    
    @Test
    void testValidateCertificate() {
        // Test certificate validation
        String certificateId = "test-certificate-id";
        
        CertificateValidationResult result = certificateManagementService.validateCertificate(certificateId);
        
        assertNotNull(result);
        assertTrue(result.isValid());
    }
}
```

### **Integration Tests**

```java
@SpringBootTest
@TestPropertySource(properties = {
    "certificate.storage.path=/tmp/test-certificates"
})
class CertificateManagementIntegrationTest {
    
    @Autowired
    private CertificateManagementService certificateManagementService;
    
    @Test
    void testCertificateLifecycle() {
        // Generate certificate
        CertificateGenerationRequest request = new CertificateGenerationRequest();
        request.setSubjectDN("CN=integration-test.com, O=Test, C=US");
        request.setValidityDays(365);
        
        CertificateGenerationResult result = certificateManagementService.generateCertificate(request);
        assertNotNull(result);
        
        // Validate certificate
        CertificateValidationResult validation = certificateManagementService.validateCertificate(result.getCertificateId());
        assertTrue(validation.isValid());
        
        // Rotate certificate
        CertificateGenerationResult rotated = certificateManagementService.rotateCertificate(result.getCertificateId(), request);
        assertNotNull(rotated);
        assertNotEquals(result.getCertificateId(), rotated.getCertificateId());
    }
}
```

---

## 📚 **API Documentation**

### **OpenAPI Specification**

```yaml
openapi: 3.0.0
info:
  title: Certificate Management API
  version: 1.0.0
  description: API for managing certificates and keys in the Payment Engine

paths:
  /api/v1/certificates/generate:
    post:
      summary: Generate a new certificate
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/CertificateGenerationRequest'
      responses:
        '201':
          description: Certificate generated successfully
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/CertificateGenerationResult'
        '400':
          description: Bad request
        '500':
          description: Internal server error

  /api/v1/certificates/import/pfx:
    post:
      summary: Import a PFX certificate
      requestBody:
        required: true
        content:
          multipart/form-data:
            schema:
              type: object
              properties:
                file:
                  type: string
                  format: binary
                password:
                  type: string
                tenantId:
                  type: string
                certificateType:
                  type: string
                description:
                  type: string
      responses:
        '201':
          description: PFX certificate imported successfully
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/PfxImportResult'
        '400':
          description: Bad request
        '500':
          description: Internal server error

components:
  schemas:
    CertificateGenerationRequest:
      type: object
      required:
        - subjectDN
      properties:
        subjectDN:
          type: string
          description: Subject Distinguished Name
        tenantId:
          type: string
          description: Tenant ID
        certificateType:
          type: string
          enum: [GENERATED, CLIENT, SERVER, CA]
        validityDays:
          type: integer
          minimum: 1
          maximum: 3650
        keyUsage:
          type: array
          items:
            type: string
        extendedKeyUsage:
          type: array
          items:
            type: string
        description:
          type: string
        includePrivateKey:
          type: boolean
        includePublicKey:
          type: boolean

    CertificateGenerationResult:
      type: object
      properties:
        certificateId:
          type: string
        subjectDN:
          type: string
        issuerDN:
          type: string
        validFrom:
          type: string
          format: date-time
        validTo:
          type: string
          format: date-time
        serialNumber:
          type: string
        publicKeyAlgorithm:
          type: string
        keySize:
          type: integer
        signatureAlgorithm:
          type: string
        certificatePem:
          type: string
        privateKeyPem:
          type: string
        publicKeyPem:
          type: string
```

---

## 🔧 **Troubleshooting**

### **Common Issues**

#### **Certificate Generation Fails**
- **Cause**: Invalid subject DN format
- **Solution**: Ensure subject DN follows X.500 format (CN=name, O=org, C=country)

#### **PFX Import Fails**
- **Cause**: Incorrect password or corrupted file
- **Solution**: Verify password and file integrity

#### **Certificate Validation Fails**
- **Cause**: Missing trusted CA certificates
- **Solution**: Import trusted CA certificates to trust store

#### **File Storage Issues**
- **Cause**: Insufficient disk space or permissions
- **Solution**: Check disk space and file system permissions

### **Logging and Debugging**

```yaml
# Logging Configuration
logging:
  level:
    com.paymentengine.payment-processing.service.CertificateManagementService: DEBUG
    com.paymentengine.payment-processing.controller.CertificateManagementController: DEBUG
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
    file: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
```

---

## 🚀 **Deployment**

### **Docker Configuration**

```dockerfile
FROM openjdk:17-jre-slim

# Install required packages
RUN apt-get update && apt-get install -y \
    openssl \
    && rm -rf /var/lib/apt/lists/*

# Create certificate storage directory
RUN mkdir -p /app/certificates

# Copy application
COPY target/payment-processing-service.jar /app/app.jar

# Set environment variables
ENV CERTIFICATE_STORAGE_PATH=/app/certificates
ENV CERTIFICATE_DEFAULT_VALIDITY_DAYS=365
ENV CERTIFICATE_KEY_SIZE=2048

# Expose port
EXPOSE 8080

# Run application
CMD ["java", "-jar", "/app/app.jar"]
```

### **Kubernetes Deployment**

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: certificate-management
spec:
  replicas: 2
  selector:
    matchLabels:
      app: certificate-management
  template:
    metadata:
      labels:
        app: certificate-management
    spec:
      containers:
      - name: certificate-management
        image: certificate-management:latest
        ports:
        - containerPort: 8080
        env:
        - name: CERTIFICATE_STORAGE_PATH
          value: "/app/certificates"
        - name: CERTIFICATE_DEFAULT_VALIDITY_DAYS
          value: "365"
        - name: CERTIFICATE_KEY_SIZE
          value: "2048"
        volumeMounts:
        - name: certificate-storage
          mountPath: /app/certificates
        resources:
          requests:
            memory: "512Mi"
            cpu: "250m"
          limits:
            memory: "1Gi"
            cpu: "500m"
      volumes:
      - name: certificate-storage
        persistentVolumeClaim:
          claimName: certificate-storage-pvc
---
apiVersion: v1
kind: PersistentVolumeClaim
metadata:
  name: certificate-storage-pvc
spec:
  accessModes:
    - ReadWriteOnce
  resources:
    requests:
      storage: 10Gi
```

---

## 📋 **Best Practices**

### **Certificate Management**

1. **Use Strong Key Sizes**: Minimum 2048-bit RSA keys
2. **Regular Rotation**: Rotate certificates before expiration
3. **Secure Storage**: Encrypt certificates and private keys
4. **Access Control**: Implement role-based access control
5. **Audit Logging**: Log all certificate operations

### **Security Considerations**

1. **Private Key Protection**: Never expose private keys
2. **Certificate Validation**: Always validate certificates
3. **Trust Store Management**: Maintain updated trust stores
4. **Revocation Checking**: Implement OCSP and CRL checking
5. **Secure Communication**: Use HTTPS for all API calls

### **Performance Optimization**

1. **Caching**: Cache frequently accessed certificates
2. **Batch Operations**: Use batch operations for bulk imports
3. **Async Processing**: Use async processing for long operations
4. **Database Indexing**: Properly index certificate tables
5. **File System Optimization**: Use SSD storage for certificate files

---

## 🎯 **Future Enhancements**

### **Planned Features**

1. **Certificate Authority (CA)**: Built-in CA for certificate issuance
2. **Automated Renewal**: Automatic certificate renewal before expiration
3. **Certificate Templates**: Predefined certificate templates
4. **Bulk Operations**: Bulk certificate generation and import
5. **Integration APIs**: REST APIs for external system integration

### **Advanced Features**

1. **Hardware Security Module (HSM)**: HSM integration for key storage
2. **Certificate Transparency**: CT log integration
3. **Advanced Validation**: Extended validation and policy checking
4. **Multi-Cloud Support**: Support for multiple cloud providers
5. **Compliance Reporting**: Automated compliance reporting

---

## 📞 **Support and Maintenance**

### **Support Channels**

- **Documentation**: Comprehensive documentation and guides
- **Issue Tracking**: GitHub issues for bug reports and feature requests
- **Community Forum**: Community support and discussions
- **Professional Support**: Enterprise support for production deployments

### **Maintenance Tasks**

1. **Regular Updates**: Keep dependencies and libraries updated
2. **Security Patches**: Apply security patches promptly
3. **Backup Procedures**: Regular backup of certificates and configuration
4. **Monitoring**: Continuous monitoring of certificate health
5. **Performance Tuning**: Regular performance optimization

---

**Document Version**: 1.0  
**Last Updated**: December 2024  
**Maintainer**: Payment Engine Team  
**Status**: Production Ready