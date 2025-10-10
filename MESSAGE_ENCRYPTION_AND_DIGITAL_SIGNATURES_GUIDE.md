# Message Encryption and Digital Signatures Configuration Guide

## Overview

The Payment Engine provides comprehensive message encryption and digital signature capabilities that can be configured at multiple levels during tenant setup and modified at the endpoint level for both incoming and outgoing messages.

---

## 🔐 **Available Encryption Algorithms**

### **Symmetric Encryption (AES)**

| Algorithm | Key Size | Mode | Security Level | Use Case |
|-----------|----------|------|----------------|----------|
| **AES-128-GCM** | 128-bit | GCM | High | Standard encryption for most messages |
| **AES-256-GCM** | 256-bit | GCM | Very High | High-security messages, sensitive data |
| **AES-128-CBC** | 128-bit | CBC | Medium | Legacy system compatibility |
| **AES-256-CBC** | 256-bit | CBC | High | Legacy system compatibility |

### **Asymmetric Encryption (RSA)**

| Algorithm | Key Size | Security Level | Use Case |
|-----------|----------|----------------|----------|
| **RSA-OAEP-2048** | 2048-bit | High | Key exchange, small message encryption |
| **RSA-OAEP-4096** | 4096-bit | Very High | High-security key exchange |

### **Modern Encryption**

| Algorithm | Key Size | Security Level | Use Case |
|-----------|----------|----------------|----------|
| **ChaCha20-Poly1305** | 256-bit | Very High | High-performance encryption |
| **SM4-GCM** | 128-bit | High | Chinese standard compliance |

### **No Encryption**
- **NONE** - No encryption applied (for testing or non-sensitive data)

---

## ✍️ **Available Digital Signature Algorithms**

### **RSA Signatures**

| Algorithm | Hash Function | Security Level | Use Case |
|-----------|---------------|----------------|----------|
| **RSA-SHA256** | SHA-256 | High | Standard message signing |
| **RSA-SHA384** | SHA-384 | Very High | High-security message signing |
| **RSA-SHA512** | SHA-512 | Very High | Maximum security message signing |

### **ECDSA Signatures**

| Algorithm | Curve | Security Level | Use Case |
|-----------|-------|----------------|----------|
| **ECDSA-SHA256** | P-256 | High | Modern, efficient signing |
| **ECDSA-SHA384** | P-384 | Very High | High-security signing |
| **ECDSA-SHA512** | P-521 | Very High | Maximum security signing |

### **Ed25519 Signatures**

| Algorithm | Security Level | Use Case |
|-----------|----------------|----------|
| **Ed25519** | Very High | Modern, fast signing |

### **Chinese Standards**

| Algorithm | Security Level | Use Case |
|-----------|----------------|----------|
| **SM2-SM3** | High | Chinese standard compliance |

### **HMAC Signatures**

| Algorithm | Security Level | Use Case |
|-----------|----------------|----------|
| **HMAC-SHA256** | High | Shared secret signing |
| **HMAC-SHA384** | Very High | High-security shared secret signing |
| **HMAC-SHA512** | Very High | Maximum security shared secret signing |

### **No Signature**
- **NONE** - No signature applied (for testing or non-critical messages)

---

## 🏗️ **Configuration Levels**

### **1. Clearing System Level (Global)**
- **Scope**: Entire clearing system
- **Priority**: Lowest (fallback)
- **Use Case**: Default encryption/signature for all messages

### **2. Tenant Level**
- **Scope**: All messages for a specific tenant
- **Priority**: Medium
- **Use Case**: Tenant-specific security requirements

### **3. Payment Type Level**
- **Scope**: Messages for specific payment types (SEPA, SWIFT, ACH, CARD)
- **Priority**: High
- **Use Case**: Payment type specific security requirements

### **4. Endpoint Level (Highest Priority)**
- **Scope**: Specific service endpoints (e.g., /fraud, /clearing, /banking)
- **Priority**: Highest
- **Use Case**: Endpoint-specific security requirements

---

## 📨 **Message Directions**

### **Incoming Messages**
- Messages received by the Payment Engine
- Encryption: Decrypt incoming messages
- Signature: Verify incoming message signatures

### **Outgoing Messages**
- Messages sent by the Payment Engine
- Encryption: Encrypt outgoing messages
- Signature: Sign outgoing messages

### **Both Directions**
- Apply same configuration to both incoming and outgoing messages

---

## 🔑 **Key Management Providers**

### **Azure Key Vault**
- **Provider**: Microsoft Azure Key Vault
- **Features**: Hardware security modules, key rotation, audit logging
- **Use Case**: Enterprise Azure environments

### **AWS KMS**
- **Provider**: Amazon Web Services Key Management Service
- **Features**: Hardware security modules, key rotation, audit logging
- **Use Case**: Enterprise AWS environments

### **HashiCorp Vault**
- **Provider**: HashiCorp Vault
- **Features**: Open source, flexible key management
- **Use Case**: On-premises and hybrid environments

### **Local Storage**
- **Provider**: Local file system or database
- **Features**: Simple key storage
- **Use Case**: Development and testing environments

### **Custom Provider**
- **Provider**: Custom key management system
- **Features**: Custom integration
- **Use Case**: Specialized key management requirements

---

## 📋 **Message Formats**

### **JSON**
- **Format**: JavaScript Object Notation
- **Content Type**: application/json
- **Use Case**: REST APIs, modern integrations

### **XML**
- **Format**: Extensible Markup Language
- **Content Type**: application/xml
- **Use Case**: Legacy systems, SOAP services

### **ISO 20022**
- **Format**: ISO 20022 financial messaging standard
- **Content Type**: application/xml
- **Use Case**: Financial message standards

### **Custom**
- **Format**: Custom message format
- **Content Type**: Custom
- **Use Case**: Proprietary message formats

### **Binary**
- **Format**: Binary data
- **Content Type**: application/octet-stream
- **Use Case**: Binary protocols, file transfers

---

## 🛡️ **Security Headers Configuration**

### **Standard Security Headers**
- **X-Content-Type-Options**: nosniff
- **X-Frame-Options**: DENY
- **X-XSS-Protection**: 1; mode=block
- **Strict-Transport-Security**: max-age=31536000; includeSubDomains

### **Custom Security Headers**
- **X-Encryption-Algorithm**: Algorithm used for encryption
- **X-Signature-Algorithm**: Algorithm used for signing
- **X-Key-ID**: Key identifier used
- **X-Timestamp**: Message timestamp
- **X-Nonce**: Random nonce for replay protection

---

## 🎯 **Configuration Examples**

### **Example 1: High-Security SEPA Payments**

```json
{
  "tenantId": "bank-001",
  "configurationLevel": "PAYMENT_TYPE",
  "paymentType": "SEPA",
  "direction": "BOTH",
  "encryptionEnabled": true,
  "encryptionAlgorithm": "AES_256_GCM",
  "encryptionKeyId": "sepa-encryption-key-001",
  "encryptionProvider": "AZURE_KEY_VAULT",
  "signatureEnabled": true,
  "signatureAlgorithm": "ECDSA_SHA384",
  "signatureKeyId": "sepa-signature-key-001",
  "signatureProvider": "AZURE_KEY_VAULT",
  "messageFormat": "ISO20022",
  "contentType": "application/xml",
  "securityHeadersEnabled": true
}
```

### **Example 2: Fraud Detection Endpoint**

```json
{
  "tenantId": "bank-001",
  "configurationLevel": "ENDPOINT",
  "serviceType": "fraud",
  "endpoint": "/fraud",
  "direction": "BOTH",
  "encryptionEnabled": true,
  "encryptionAlgorithm": "AES_128_GCM",
  "encryptionKeyId": "fraud-encryption-key-001",
  "encryptionProvider": "AWS_KMS",
  "signatureEnabled": true,
  "signatureAlgorithm": "RSA_SHA256",
  "signatureKeyId": "fraud-signature-key-001",
  "signatureProvider": "AWS_KMS",
  "messageFormat": "JSON",
  "contentType": "application/json",
  "securityHeadersEnabled": true
}
```

### **Example 3: Legacy System Integration**

```json
{
  "tenantId": "bank-001",
  "configurationLevel": "TENANT",
  "direction": "OUTGOING",
  "encryptionEnabled": true,
  "encryptionAlgorithm": "AES_256_CBC",
  "encryptionKeyId": "legacy-encryption-key-001",
  "encryptionProvider": "LOCAL_STORAGE",
  "signatureEnabled": true,
  "signatureAlgorithm": "RSA_SHA256",
  "signatureKeyId": "legacy-signature-key-001",
  "signatureProvider": "LOCAL_STORAGE",
  "messageFormat": "XML",
  "contentType": "application/xml",
  "securityHeadersEnabled": false
}
```

### **Example 4: Development Environment**

```json
{
  "tenantId": "dev-tenant",
  "configurationLevel": "CLEARING_SYSTEM",
  "direction": "BOTH",
  "encryptionEnabled": false,
  "encryptionAlgorithm": "NONE",
  "signatureEnabled": false,
  "signatureAlgorithm": "NONE",
  "messageFormat": "JSON",
  "contentType": "application/json",
  "securityHeadersEnabled": false
}
```

---

## 🔧 **Configuration Hierarchy Resolution**

### **Priority Order (Highest to Lowest)**
1. **Endpoint Level** - Most specific configuration
2. **Payment Type Level** - Payment type specific
3. **Tenant Level** - Tenant specific
4. **Clearing System Level** - Global fallback

### **Resolution Logic**
```java
// Pseudo-code for configuration resolution
public ResolvedMessageSecurityConfiguration resolveConfiguration(
    String tenantId,
    String serviceType,
    String endpoint,
    String paymentType,
    MessageDirection direction) {
    
    // 1. Try to find endpoint-specific configuration
    if (endpoint != null) {
        config = findEndpointConfiguration(tenantId, serviceType, endpoint, direction);
        if (config != null) return config;
    }
    
    // 2. Try to find payment type specific configuration
    if (paymentType != null) {
        config = findPaymentTypeConfiguration(tenantId, paymentType, direction);
        if (config != null) return config;
    }
    
    // 3. Try to find tenant-specific configuration
    config = findTenantConfiguration(tenantId, direction);
    if (config != null) return config;
    
    // 4. Fall back to clearing system configuration
    config = findClearingSystemConfiguration(direction);
    return config;
}
```

---

## 🚀 **API Endpoints**

### **Configuration Management**
- `POST /api/v1/message-security/configuration` - Create configuration
- `PUT /api/v1/message-security/configuration/{id}` - Update configuration
- `GET /api/v1/message-security/configuration/{id}` - Get configuration
- `DELETE /api/v1/message-security/configuration/{id}` - Delete configuration

### **Configuration Resolution**
- `GET /api/v1/message-security/resolve` - Resolve configuration for context
- `GET /api/v1/message-security/tenant/{tenantId}` - Get all tenant configurations
- `GET /api/v1/message-security/statistics` - Get configuration statistics

### **Configuration Activation**
- `POST /api/v1/message-security/configuration/{id}/activate` - Activate configuration
- `POST /api/v1/message-security/configuration/{id}/deactivate` - Deactivate configuration

---

## 🧪 **Testing and Validation**

### **Configuration Validation**
- Algorithm compatibility validation
- Key availability validation
- Provider connectivity validation
- Message format validation

### **Security Testing**
- Encryption/decryption testing
- Signature generation/verification testing
- Key rotation testing
- Performance testing

### **Integration Testing**
- End-to-end message flow testing
- Multi-level configuration testing
- Provider integration testing
- Error handling testing

---

## 📊 **Monitoring and Observability**

### **Security Metrics**
- Encryption/decryption success rates
- Signature verification success rates
- Key usage statistics
- Configuration resolution statistics

### **Performance Metrics**
- Encryption/decryption latency
- Signature generation/verification latency
- Configuration resolution latency
- Key retrieval latency

### **Audit Logging**
- Configuration changes
- Key usage
- Security events
- Access patterns

---

## 🔒 **Security Best Practices**

### **Key Management**
- Use hardware security modules (HSMs) for production
- Implement key rotation policies
- Use separate keys for encryption and signing
- Monitor key usage and access

### **Algorithm Selection**
- Use AES-256-GCM for symmetric encryption
- Use ECDSA-SHA384 or RSA-SHA384 for signatures
- Avoid deprecated algorithms (MD5, SHA-1, DES)
- Consider post-quantum cryptography for future-proofing

### **Configuration Management**
- Use least privilege principle
- Implement configuration validation
- Monitor configuration changes
- Use version control for configurations

### **Message Security**
- Always sign before encrypting
- Use authenticated encryption modes (GCM)
- Include timestamps and nonces
- Implement replay protection

---

## 🎯 **Use Case Recommendations**

### **High-Security Financial Messages**
- **Encryption**: AES-256-GCM
- **Signature**: ECDSA-SHA384 or RSA-SHA384
- **Key Provider**: Azure Key Vault or AWS KMS
- **Format**: ISO 20022

### **API Communications**
- **Encryption**: AES-128-GCM
- **Signature**: RSA-SHA256 or ECDSA-SHA256
- **Key Provider**: HashiCorp Vault
- **Format**: JSON

### **Legacy System Integration**
- **Encryption**: AES-256-CBC
- **Signature**: RSA-SHA256
- **Key Provider**: Local Storage
- **Format**: XML

### **Development and Testing**
- **Encryption**: None or AES-128-GCM
- **Signature**: None or HMAC-SHA256
- **Key Provider**: Local Storage
- **Format**: JSON

---

## 🎉 **Conclusion**

The Payment Engine provides a comprehensive and flexible message encryption and digital signature system that can be configured at multiple levels to meet various security requirements. The system supports:

✅ **Multiple Encryption Algorithms**: From AES to ChaCha20-Poly1305  
✅ **Multiple Signature Algorithms**: From RSA to Ed25519  
✅ **Multiple Key Providers**: From local storage to cloud HSMs  
✅ **Multiple Configuration Levels**: From global to endpoint-specific  
✅ **Multiple Message Formats**: From JSON to ISO 20022  
✅ **Comprehensive Security**: Encryption, signatures, and security headers  
✅ **Flexible Configuration**: Easy setup and modification  
✅ **Production Ready**: Enterprise-grade security and monitoring  

The system is designed to be secure, flexible, and easy to use while providing enterprise-grade security for financial message processing.