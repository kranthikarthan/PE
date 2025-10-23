# Security Guide

## Overview

This document outlines the comprehensive security measures implemented in the React Operations Portal to ensure the protection of sensitive data, secure communication, and robust access control.

## 🔒 Security Architecture

### Defense in Depth Strategy

```
┌─────────────────────────────────────────────────────────────┐
│                    Security Layers                         │
├─────────────────────────────────────────────────────────────┤
│  Application Security                                       │
│  ├── Input Validation & Sanitization                       │
│  ├── XSS Protection (CSP)                                  │
│  ├── CSRF Protection                                       │
│  └── Secure Authentication                                 │
├─────────────────────────────────────────────────────────────┤
│  Network Security                                           │
│  ├── HTTPS Enforcement                                     │
│  ├── CORS Configuration                                    │
│  ├── Rate Limiting                                         │
│  └── Network Policies                                      │
├─────────────────────────────────────────────────────────────┤
│  Infrastructure Security                                    │
│  ├── Container Security                                    │
│  ├── Kubernetes Security                                   │
│  ├── Secret Management                                     │
│  └── Monitoring & Alerting                                 │
└─────────────────────────────────────────────────────────────┘
```

## 🛡️ Frontend Security

### Input Validation and Sanitization

```typescript
// src/utils/validators.ts
export const sanitizeInput = (input: string): string => {
  return input
    .replace(/[<>]/g, '') // Remove HTML tags
    .replace(/javascript:/gi, '') // Remove javascript: protocol
    .replace(/on\w+=/gi, '') // Remove event handlers
    .trim();
};

export const validateEmail = (email: string): boolean => {
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  return emailRegex.test(email);
};

export const validateAmount = (amount: number): boolean => {
  return amount > 0 && amount <= 999999999.99;
};

export const validateAccountNumber = (accountNumber: string): boolean => {
  const accountRegex = /^[0-9]{8,20}$/;
  return accountRegex.test(accountNumber);
};
```

### XSS Protection

```typescript
// src/utils/security.ts
export const escapeHtml = (text: string): string => {
  const map: Record<string, string> = {
    '&': '&amp;',
    '<': '&lt;',
    '>': '&gt;',
    '"': '&quot;',
    "'": '&#039;'
  };
  return text.replace(/[&<>"']/g, (m) => map[m]);
};

export const sanitizeHtml = (html: string): string => {
  // Remove script tags and event handlers
  return html
    .replace(/<script\b[^<]*(?:(?!<\/script>)<[^<]*)*<\/script>/gi, '')
    .replace(/on\w+\s*=\s*["'][^"']*["']/gi, '')
    .replace(/javascript\s*:/gi, '');
};
```

### Content Security Policy (CSP)

```typescript
// src/utils/csp.ts
export const cspConfig = {
  'default-src': ["'self'"],
  'script-src': ["'self'", "'unsafe-inline'", "'unsafe-eval'"],
  'style-src': ["'self'", "'unsafe-inline'", "https://fonts.googleapis.com"],
  'img-src': ["'self'", "data:", "https:"],
  'font-src': ["'self'", "data:", "https://fonts.gstatic.com"],
  'connect-src': ["'self'", "https:"],
  'frame-ancestors': ["'self'"],
  'base-uri': ["'self'"],
  'form-action': ["'self'"]
};

export const generateCSPHeader = (): string => {
  return Object.entries(cspConfig)
    .map(([key, values]) => `${key} ${values.join(' ')}`)
    .join('; ');
};
```

## 🔐 Authentication & Authorization

### JWT Token Management

```typescript
// src/utils/auth.ts
export const tokenManager = {
  setToken: (token: string): void => {
    // Store in httpOnly cookie (handled by backend)
    document.cookie = `auth_token=${token}; HttpOnly; Secure; SameSite=Strict`;
  },

  getToken: (): string | null => {
    const cookies = document.cookie.split(';');
    const tokenCookie = cookies.find(cookie => 
      cookie.trim().startsWith('auth_token=')
    );
    return tokenCookie ? tokenCookie.split('=')[1] : null;
  },

  removeToken: (): void => {
    document.cookie = 'auth_token=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;';
  },

  isTokenExpired: (token: string): boolean => {
    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      return Date.now() >= payload.exp * 1000;
    } catch {
      return true;
    }
  }
};
```

### Role-Based Access Control (RBAC)

```typescript
// src/utils/rbac.ts
export const permissions = {
  // Dashboard permissions
  DASHBOARD_VIEW: 'dashboard:view',
  DASHBOARD_METRICS: 'dashboard:metrics',
  
  // Service management permissions
  SERVICES_VIEW: 'services:view',
  SERVICES_CONTROL: 'services:control',
  SERVICES_MONITOR: 'services:monitor',
  
  // Payment permissions
  PAYMENTS_VIEW: 'payments:view',
  PAYMENTS_CREATE: 'payments:create',
  PAYMENTS_UPDATE: 'payments:update',
  PAYMENTS_DELETE: 'payments:delete',
  PAYMENTS_REPAIR: 'payments:repair',
  
  // Transaction permissions
  TRANSACTIONS_VIEW: 'transactions:view',
  TRANSACTIONS_SEARCH: 'transactions:search',
  TRANSACTIONS_EXPORT: 'transactions:export',
  
  // Reconciliation permissions
  RECONCILIATION_VIEW: 'reconciliation:view',
  RECONCILIATION_PROCESS: 'reconciliation:process',
  RECONCILIATION_EXCEPTIONS: 'reconciliation:exceptions',
  
  // Onboarding permissions
  CHANNELS_VIEW: 'channels:view',
  CHANNELS_CREATE: 'channels:create',
  CHANNELS_UPDATE: 'channels:update',
  CHANNELS_DELETE: 'channels:delete',
  
  // Admin permissions
  USERS_MANAGE: 'users:manage',
  ROLES_MANAGE: 'roles:manage',
  SYSTEM_CONFIG: 'system:config'
};

export const rolePermissions: Record<string, string[]> = {
  'admin': Object.values(permissions),
  'operator': [
    permissions.DASHBOARD_VIEW,
    permissions.SERVICES_VIEW,
    permissions.SERVICES_MONITOR,
    permissions.PAYMENTS_VIEW,
    permissions.PAYMENTS_REPAIR,
    permissions.TRANSACTIONS_VIEW,
    permissions.TRANSACTIONS_SEARCH,
    permissions.RECONCILIATION_VIEW,
    permissions.CHANNELS_VIEW
  ],
  'viewer': [
    permissions.DASHBOARD_VIEW,
    permissions.SERVICES_VIEW,
    permissions.PAYMENTS_VIEW,
    permissions.TRANSACTIONS_VIEW,
    permissions.RECONCILIATION_VIEW,
    permissions.CHANNELS_VIEW
  ]
};

export const hasPermission = (userRole: string, permission: string): boolean => {
  const rolePerms = rolePermissions[userRole] || [];
  return rolePerms.includes(permission);
};

export const hasAnyPermission = (userRole: string, permissions: string[]): boolean => {
  return permissions.some(permission => hasPermission(userRole, permission));
};
```

### Secure API Communication

```typescript
// src/services/httpClient.ts
export const createSecureHttpClient = (baseURL: string): AxiosInstance => {
  const client = axios.create({
    baseURL,
    timeout: 30000,
    headers: {
      'Content-Type': 'application/json',
      'X-Requested-With': 'XMLHttpRequest'
    }
  });

  // Request interceptor for security
  client.interceptors.request.use(
    (config) => {
      // Add CSRF token
      const csrfToken = getCsrfToken();
      if (csrfToken) {
        config.headers['X-CSRF-Token'] = csrfToken;
      }

      // Add correlation ID for tracking
      config.headers['X-Correlation-ID'] = generateCorrelationId();

      // Add tenant context
      const tenantId = getTenantId();
      if (tenantId) {
        config.headers['X-Tenant-ID'] = tenantId;
      }

      // Add user context
      const userId = getUserId();
      if (userId) {
        config.headers['X-User-ID'] = userId;
      }

      return config;
    },
    (error) => Promise.reject(error)
  );

  // Response interceptor for security
  client.interceptors.response.use(
    (response) => {
      // Log security events
      logSecurityEvent('API_SUCCESS', {
        url: response.config.url,
        method: response.config.method,
        status: response.status
      });
      return response;
    },
    async (error) => {
      // Log security events
      logSecurityEvent('API_ERROR', {
        url: error.config?.url,
        method: error.config?.method,
        status: error.response?.status,
        error: error.message
      });

      // Handle security-related errors
      if (error.response?.status === 401) {
        // Unauthorized - redirect to login
        redirectToLogin();
      } else if (error.response?.status === 403) {
        // Forbidden - show access denied
        showAccessDenied();
      } else if (error.response?.status === 429) {
        // Rate limited - show rate limit message
        showRateLimitMessage();
      }

      return Promise.reject(error);
    }
  );

  return client;
};
```

## 🔒 Data Protection

### Sensitive Data Handling

```typescript
// src/utils/dataProtection.ts
export const maskSensitiveData = (data: string, type: 'account' | 'email' | 'phone'): string => {
  switch (type) {
    case 'account':
      return data.replace(/(\d{4})\d{4}(\d{4})/, '$1****$2');
    case 'email':
      const [local, domain] = data.split('@');
      return `${local.substring(0, 2)}***@${domain}`;
    case 'phone':
      return data.replace(/(\d{3})\d{3}(\d{4})/, '$1***$2');
    default:
      return data;
  }
};

export const encryptSensitiveData = (data: string): string => {
  // Use Web Crypto API for client-side encryption
  const encoder = new TextEncoder();
  const dataBuffer = encoder.encode(data);
  
  // In production, use proper encryption with a secure key
  return btoa(String.fromCharCode(...dataBuffer));
};

export const decryptSensitiveData = (encryptedData: string): string => {
  // Decrypt sensitive data
  const binaryString = atob(encryptedData);
  const bytes = new Uint8Array(binaryString.length);
  for (let i = 0; i < binaryString.length; i++) {
    bytes[i] = binaryString.charCodeAt(i);
  }
  return new TextDecoder().decode(bytes);
};
```

### Secure Storage

```typescript
// src/utils/secureStorage.ts
export const secureStorage = {
  setItem: (key: string, value: any): void => {
    try {
      const encryptedValue = encryptSensitiveData(JSON.stringify(value));
      localStorage.setItem(key, encryptedValue);
    } catch (error) {
      console.error('Failed to store data securely:', error);
    }
  },

  getItem: <T>(key: string): T | null => {
    try {
      const encryptedValue = localStorage.getItem(key);
      if (!encryptedValue) return null;
      
      const decryptedValue = decryptSensitiveData(encryptedValue);
      return JSON.parse(decryptedValue);
    } catch (error) {
      console.error('Failed to retrieve data securely:', error);
      return null;
    }
  },

  removeItem: (key: string): void => {
    localStorage.removeItem(key);
  },

  clear: (): void => {
    localStorage.clear();
  }
};
```

## 🚨 Security Monitoring

### Security Event Logging

```typescript
// src/utils/securityLogger.ts
export const logSecurityEvent = (event: string, details: Record<string, any>): void => {
  const securityEvent = {
    timestamp: new Date().toISOString(),
    event,
    details,
    userAgent: navigator.userAgent,
    url: window.location.href,
    sessionId: getSessionId()
  };

  // Send to security monitoring service
  sendToSecurityService(securityEvent);
  
  // Log to console in development
  if (process.env.NODE_ENV === 'development') {
    console.log('Security Event:', securityEvent);
  }
};

export const securityEvents = {
  LOGIN_ATTEMPT: 'login_attempt',
  LOGIN_SUCCESS: 'login_success',
  LOGIN_FAILURE: 'login_failure',
  LOGOUT: 'logout',
  PERMISSION_DENIED: 'permission_denied',
  SUSPICIOUS_ACTIVITY: 'suspicious_activity',
  DATA_ACCESS: 'data_access',
  DATA_EXPORT: 'data_export',
  CONFIGURATION_CHANGE: 'configuration_change'
};
```

### Anomaly Detection

```typescript
// src/utils/anomalyDetection.ts
export const detectAnomalies = (userBehavior: UserBehavior): SecurityAlert[] => {
  const alerts: SecurityAlert[] = [];

  // Detect unusual login patterns
  if (userBehavior.loginAttempts > 5) {
    alerts.push({
      type: 'BRUTE_FORCE_ATTEMPT',
      severity: 'HIGH',
      message: 'Multiple failed login attempts detected'
    });
  }

  // Detect unusual data access patterns
  if (userBehavior.dataAccessRate > 100) {
    alerts.push({
      type: 'UNUSUAL_DATA_ACCESS',
      severity: 'MEDIUM',
      message: 'Unusual data access pattern detected'
    });
  }

  // Detect unusual time patterns
  if (userBehavior.unusualHours) {
    alerts.push({
      type: 'UNUSUAL_HOURS',
      severity: 'LOW',
      message: 'Access during unusual hours'
    });
  }

  return alerts;
};
```

## 🔐 Container Security

### Docker Security Configuration

```dockerfile
# Dockerfile
FROM node:18-alpine AS builder

# Security: Use non-root user
RUN addgroup -g 1001 -S nodejs && \
    adduser -S nextjs -u 1001

# Security: Set proper permissions
RUN chown -R nextjs:nodejs /app
USER nextjs

# Security: Use distroless image for production
FROM gcr.io/distroless/nodejs18-debian11
COPY --from=builder /app /app
USER 1001
EXPOSE 8080
CMD ["node", "server.js"]
```

### Kubernetes Security Configuration

```yaml
# k8s/deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: operations-portal
spec:
  template:
    spec:
      securityContext:
        runAsNonRoot: true
        runAsUser: 1001
        runAsGroup: 1001
        fsGroup: 1001
        seccompProfile:
          type: RuntimeDefault
      containers:
      - name: operations-portal
        securityContext:
          allowPrivilegeEscalation: false
          readOnlyRootFilesystem: true
          runAsNonRoot: true
          runAsUser: 1001
          runAsGroup: 1001
          capabilities:
            drop:
            - ALL
        resources:
          requests:
            memory: "128Mi"
            cpu: "100m"
          limits:
            memory: "256Mi"
            cpu: "200m"
```

## 🛡️ Network Security

### Network Policies

```yaml
# k8s/networkpolicy.yaml
apiVersion: networking.k8s.io/v1
kind: NetworkPolicy
metadata:
  name: operations-portal-network-policy
spec:
  podSelector:
    matchLabels:
      app: operations-portal
  policyTypes:
  - Ingress
  - Egress
  ingress:
  - from:
    - namespaceSelector:
        matchLabels:
          name: ingress-nginx
    ports:
    - protocol: TCP
      port: 8080
  egress:
  - to:
    - namespaceSelector:
        matchLabels:
          name: operations-portal
    ports:
    - protocol: TCP
        port: 8080
  - to: []
    ports:
    - protocol: TCP
        port: 53
    - protocol: UDP
        port: 53
```

### CORS Configuration

```typescript
// src/utils/cors.ts
export const corsConfig = {
  origin: process.env.REACT_APP_ALLOWED_ORIGINS?.split(',') || ['http://localhost:3000'],
  credentials: true,
  methods: ['GET', 'POST', 'PUT', 'DELETE', 'OPTIONS'],
  allowedHeaders: [
    'Content-Type',
    'Authorization',
    'X-Requested-With',
    'X-CSRF-Token',
    'X-Correlation-ID',
    'X-Tenant-ID',
    'X-User-ID'
  ],
  exposedHeaders: ['X-Correlation-ID'],
  maxAge: 86400 // 24 hours
};
```

## 🔍 Security Testing

### Security Test Suite

```typescript
// src/__tests__/security.test.ts
describe('Security Tests', () => {
  it('should sanitize user input', () => {
    const maliciousInput = '<script>alert("xss")</script>';
    const sanitized = sanitizeInput(maliciousInput);
    expect(sanitized).not.toContain('<script>');
  });

  it('should validate email format', () => {
    expect(validateEmail('test@example.com')).toBe(true);
    expect(validateEmail('invalid-email')).toBe(false);
  });

  it('should mask sensitive data', () => {
    const accountNumber = '1234567890123456';
    const masked = maskSensitiveData(accountNumber, 'account');
    expect(masked).toBe('1234****3456');
  });

  it('should enforce CSP headers', () => {
    const cspHeader = generateCSPHeader();
    expect(cspHeader).toContain("default-src 'self'");
    expect(cspHeader).toContain("script-src 'self'");
  });
});
```

### Penetration Testing Checklist

- [ ] **Input Validation**: Test all input fields for XSS, SQL injection
- [ ] **Authentication**: Test login bypass, session management
- [ ] **Authorization**: Test privilege escalation, access control
- [ ] **Data Protection**: Test data encryption, secure storage
- [ ] **Network Security**: Test HTTPS enforcement, CORS configuration
- [ ] **Session Management**: Test session fixation, CSRF protection
- [ ] **Error Handling**: Test information disclosure in error messages
- [ ] **File Upload**: Test malicious file upload prevention
- [ ] **API Security**: Test rate limiting, input validation
- [ ] **Client-Side Security**: Test DOM-based XSS, client-side storage

## 📋 Security Checklist

### Development Security
- [ ] Input validation and sanitization implemented
- [ ] XSS protection with CSP headers
- [ ] CSRF protection with tokens
- [ ] Secure authentication and session management
- [ ] Role-based access control implemented
- [ ] Sensitive data encryption and masking
- [ ] Secure API communication
- [ ] Security event logging
- [ ] Error handling without information disclosure

### Deployment Security
- [ ] HTTPS enforcement
- [ ] Secure container configuration
- [ ] Kubernetes security policies
- [ ] Network segmentation
- [ ] Secret management
- [ ] Security monitoring and alerting
- [ ] Regular security updates
- [ ] Vulnerability scanning
- [ ] Penetration testing

### Operational Security
- [ ] Security monitoring dashboard
- [ ] Incident response procedures
- [ ] Security training for developers
- [ ] Regular security audits
- [ ] Threat modeling
- [ ] Security documentation
- [ ] Compliance with security standards
- [ ] Security metrics and reporting

This comprehensive security guide ensures the React Operations Portal is protected against common security threats and follows industry best practices for secure application development and deployment.
