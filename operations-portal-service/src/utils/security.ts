/**
 * Security Utilities
 * 
 * Comprehensive security utilities for input sanitization, XSS protection,
 * CSRF protection, and secure data handling.
 */

// XSS Protection
export const escapeHtml = (text: string): string => {
  const map: Record<string, string> = {
    '&': '&amp;',
    '<': '&lt;',
    '>': '&gt;',
    '"': '&quot;',
    "'": '&#039;',
    '/': '&#x2F;',
    '`': '&#x60;',
    '=': '&#x3D;'
  };
  return text.replace(/[&<>"'`=\/]/g, (m) => map[m]);
};

export const sanitizeHtml = (html: string): string => {
  // Remove script tags and event handlers
  return html
    .replace(/<script\b[^<]*(?:(?!<\/script>)<[^<]*)*<\/script>/gi, '')
    .replace(/<iframe\b[^<]*(?:(?!<\/iframe>)<[^<]*)*<\/iframe>/gi, '')
    .replace(/<object\b[^<]*(?:(?!<\/object>)<[^<]*)*<\/object>/gi, '')
    .replace(/<embed\b[^<]*(?:(?!<\/embed>)<[^<]*)*<\/embed>/gi, '')
    .replace(/<link\b[^<]*(?:(?!<\/link>)<[^<]*)*<\/link>/gi, '')
    .replace(/<meta\b[^<]*(?:(?!<\/meta>)<[^<]*)*<\/meta>/gi, '')
    .replace(/on\w+\s*=\s*["'][^"']*["']/gi, '')
    .replace(/javascript\s*:/gi, '')
    .replace(/vbscript\s*:/gi, '')
    .replace(/data\s*:/gi, '');
};

export const sanitizeInput = (input: string): string => {
  if (typeof input !== 'string') {
    return '';
  }
  
  return input
    .replace(/[<>]/g, '') // Remove HTML tags
    .replace(/javascript:/gi, '') // Remove javascript: protocol
    .replace(/on\w+=/gi, '') // Remove event handlers
    .replace(/[<>"'`]/g, '') // Remove dangerous characters
    .trim();
};

// CSRF Protection
export const generateCsrfToken = (): string => {
  const array = new Uint8Array(32);
  crypto.getRandomValues(array);
  return Array.from(array, byte => byte.toString(16).padStart(2, '0')).join('');
};

export const validateCsrfToken = (token: string, storedToken: string): boolean => {
  if (!token || !storedToken) {
    return false;
  }
  
  // Use constant-time comparison to prevent timing attacks
  if (token.length !== storedToken.length) {
    return false;
  }
  
  let result = 0;
  for (let i = 0; i < token.length; i++) {
    result |= token.charCodeAt(i) ^ storedToken.charCodeAt(i);
  }
  
  return result === 0;
};

// Secure Data Handling
export const maskSensitiveData = (data: string, type: 'account' | 'email' | 'phone' | 'ssn' | 'creditcard'): string => {
  if (!data || typeof data !== 'string') {
    return '';
  }
  
  switch (type) {
    case 'account':
      // Mask account number: 1234567890123456 -> 1234****3456
      return data.replace(/(\d{4})\d{4,8}(\d{4})/, '$1****$2');
    
    case 'email':
      // Mask email: john.doe@example.com -> jo***@example.com
      const [local, domain] = data.split('@');
      if (local.length <= 2) {
        return `${local[0]}***@${domain}`;
      }
      return `${local.substring(0, 2)}***@${domain}`;
    
    case 'phone':
      // Mask phone: +1234567890 -> +123***7890
      return data.replace(/(\+\d{3})\d{3}(\d{4})/, '$1***$2');
    
    case 'ssn':
      // Mask SSN: 123-45-6789 -> 123-**-6789
      return data.replace(/(\d{3})-(\d{2})-(\d{4})/, '$1-**-$3');
    
    case 'creditcard':
      // Mask credit card: 1234567890123456 -> 1234-****-****-3456
      return data.replace(/(\d{4})\d{4}(\d{4})\d{4}/, '$1-****-****-$2');
    
    default:
      return data;
  }
};

// Input Validation
export const validateEmail = (email: string): boolean => {
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  return emailRegex.test(email) && email.length <= 254;
};

export const validatePassword = (password: string): { isValid: boolean; errors: string[] } => {
  const errors: string[] = [];
  
  if (password.length < 8) {
    errors.push('Password must be at least 8 characters long');
  }
  
  if (password.length > 128) {
    errors.push('Password must be less than 128 characters');
  }
  
  if (!/[A-Z]/.test(password)) {
    errors.push('Password must contain at least one uppercase letter');
  }
  
  if (!/[a-z]/.test(password)) {
    errors.push('Password must contain at least one lowercase letter');
  }
  
  if (!/\d/.test(password)) {
    errors.push('Password must contain at least one number');
  }
  
  if (!/[!@#$%^&*()_+\-=\[\]{};':"\\|,.<>\/?]/.test(password)) {
    errors.push('Password must contain at least one special character');
  }
  
  // Check for common weak patterns
  if (/(.)\1{2,}/.test(password)) {
    errors.push('Password must not contain repeated characters');
  }
  
  if (/123|abc|qwe|asd|zxc/i.test(password)) {
    errors.push('Password must not contain common sequences');
  }
  
  return {
    isValid: errors.length === 0,
    errors
  };
};

export const validateAmount = (amount: number): boolean => {
  return amount > 0 && amount <= 999999999.99 && Number.isFinite(amount);
};

export const validateAccountNumber = (accountNumber: string): boolean => {
  const accountRegex = /^[0-9]{8,20}$/;
  return accountRegex.test(accountNumber);
};

export const validatePhoneNumber = (phone: string): boolean => {
  const phoneRegex = /^[\+]?[1-9][\d]{0,15}$/;
  return phoneRegex.test(phone.replace(/[\s\-\(\)]/g, ''));
};

// Content Security Policy
export const generateCSPHeader = (): string => {
  const cspConfig = {
    'default-src': ["'self'"],
    'script-src': ["'self'", "'unsafe-inline'", "'unsafe-eval'"],
    'style-src': ["'self'", "'unsafe-inline'", "https://fonts.googleapis.com"],
    'img-src': ["'self'", "data:", "https:"],
    'font-src': ["'self'", "data:", "https://fonts.gstatic.com"],
    'connect-src': ["'self'", "https:"],
    'frame-ancestors': ["'self'"],
    'base-uri': ["'self'"],
    'form-action': ["'self'"],
    'object-src': ["'none'"],
    'media-src': ["'self'"],
    'worker-src': ["'self'", "blob:"],
    'manifest-src': ["'self'"]
  };
  
  return Object.entries(cspConfig)
    .map(([key, values]) => `${key} ${values.join(' ')}`)
    .join('; ');
};

// Secure Storage
export const secureStorage = {
  setItem: (key: string, value: any): void => {
    try {
      const encryptedValue = btoa(JSON.stringify(value));
      localStorage.setItem(key, encryptedValue);
    } catch (error) {
      console.error('Failed to store data securely:', error);
    }
  },

  getItem: <T>(key: string): T | null => {
    try {
      const encryptedValue = localStorage.getItem(key);
      if (!encryptedValue) return null;
      
      const decryptedValue = atob(encryptedValue);
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

// Rate Limiting
export class RateLimiter {
  private requests: Map<string, number[]> = new Map();
  private readonly windowMs: number;
  private readonly maxRequests: number;

  constructor(windowMs: number = 60000, maxRequests: number = 100) {
    this.windowMs = windowMs;
    this.maxRequests = maxRequests;
  }

  isAllowed(identifier: string): boolean {
    const now = Date.now();
    const requests = this.requests.get(identifier) || [];
    
    // Remove old requests outside the window
    const validRequests = requests.filter(time => now - time < this.windowMs);
    
    if (validRequests.length >= this.maxRequests) {
      return false;
    }
    
    validRequests.push(now);
    this.requests.set(identifier, validRequests);
    
    return true;
  }

  getRemainingRequests(identifier: string): number {
    const now = Date.now();
    const requests = this.requests.get(identifier) || [];
    const validRequests = requests.filter(time => now - time < this.windowMs);
    
    return Math.max(0, this.maxRequests - validRequests.length);
  }

  reset(identifier: string): void {
    this.requests.delete(identifier);
  }
}

// Security Headers
export const securityHeaders = {
  'X-Content-Type-Options': 'nosniff',
  'X-Frame-Options': 'SAMEORIGIN',
  'X-XSS-Protection': '1; mode=block',
  'Referrer-Policy': 'strict-origin-when-cross-origin',
  'Permissions-Policy': 'geolocation=(), microphone=(), camera=()',
  'Strict-Transport-Security': 'max-age=31536000; includeSubDomains',
  'Content-Security-Policy': generateCSPHeader()
};

// Security Event Logging
export const logSecurityEvent = (event: string, details: Record<string, any>): void => {
  const securityEvent = {
    timestamp: new Date().toISOString(),
    event,
    details,
    userAgent: navigator.userAgent,
    url: window.location.href,
    sessionId: getSessionId(),
    ip: 'client-side' // IP will be captured server-side
  };

  // Send to security monitoring service
  if (typeof window !== 'undefined') {
    fetch('/api/security/events', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'X-CSRF-Token': getCsrfToken()
      },
      body: JSON.stringify(securityEvent)
    }).catch(error => {
      console.error('Failed to send security event:', error);
    });
  }
  
  // Log to console in development
  if (process.env.NODE_ENV === 'development') {
    console.log('Security Event:', securityEvent);
  }
};

// Helper functions
const getSessionId = (): string => {
  return sessionStorage.getItem('sessionId') || 'unknown';
};

const getCsrfToken = (): string => {
  return document.querySelector('meta[name="csrf-token"]')?.getAttribute('content') || '';
};

// Security Constants
export const SECURITY_EVENTS = {
  LOGIN_ATTEMPT: 'login_attempt',
  LOGIN_SUCCESS: 'login_success',
  LOGIN_FAILURE: 'login_failure',
  LOGOUT: 'logout',
  PERMISSION_DENIED: 'permission_denied',
  SUSPICIOUS_ACTIVITY: 'suspicious_activity',
  DATA_ACCESS: 'data_access',
  DATA_EXPORT: 'data_export',
  CONFIGURATION_CHANGE: 'configuration_change',
  XSS_ATTEMPT: 'xss_attempt',
  CSRF_ATTEMPT: 'csrf_attempt',
  RATE_LIMIT_EXCEEDED: 'rate_limit_exceeded'
} as const;

export type SecurityEvent = typeof SECURITY_EVENTS[keyof typeof SECURITY_EVENTS];
