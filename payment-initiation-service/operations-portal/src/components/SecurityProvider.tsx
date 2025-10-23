/**
 * Security Provider
 * 
 * React context provider for security-related functionality including
 * input sanitization, CSRF protection, and security monitoring.
 */

import React, { createContext, useContext, useEffect, useState, ReactNode } from 'react';
import { inputSanitizer } from '../utils/inputSanitizer';
import { csrfManager, secureSessionManager } from '../utils/secureCookies';
import { secureStorageManager, jwtManager } from '../utils/secretsManager';
import { logSecurityEvent, SECURITY_EVENTS } from '../utils/security';

interface SecurityContextType {
  // Input Sanitization
  sanitizeInput: (input: any, type: string) => any;
  sanitizeFormData: (data: Record<string, any>) => Record<string, any>;
  
  // CSRF Protection
  getCsrfToken: () => string | null;
  validateCsrfToken: (token: string) => boolean;
  
  // Session Management
  isSessionValid: () => boolean;
  updateSessionActivity: () => void;
  
  // Security Monitoring
  logSecurityEvent: (event: string, details: Record<string, any>) => void;
  
  // Storage Security
  setSecureItem: (key: string, value: any) => void;
  getSecureItem: <T>(key: string) => T | null;
  removeSecureItem: (key: string) => void;
  
  // JWT Management
  getJWTToken: () => string | null;
  isJWTExpired: () => boolean;
  
  // Security Status
  isSecure: boolean;
  securityIssues: string[];
}

const SecurityContext = createContext<SecurityContextType | undefined>(undefined);

interface SecurityProviderProps {
  children: ReactNode;
}

export const SecurityProvider: React.FC<SecurityProviderProps> = ({ children }) => {
  const [isSecure, setIsSecure] = useState(true);
  const [securityIssues, setSecurityIssues] = useState<string[]>([]);
  
  // Initialize security measures
  useEffect(() => {
    initializeSecurity();
  }, []);
  
  const initializeSecurity = () => {
    try {
      // Generate CSRF token
      csrfManager.generateToken();
      
      // Check session validity
      const sessionValid = secureSessionManager.isSessionValid();
      if (!sessionValid) {
        logSecurityEvent(SECURITY_EVENTS.SUSPICIOUS_ACTIVITY, {
          reason: 'Invalid session detected'
        });
      }
      
      // Validate JWT token
      const token = jwtManager.getToken();
      if (token && jwtManager.isTokenExpired(token)) {
        logSecurityEvent(SECURITY_EVENTS.LOGIN_FAILURE, {
          reason: 'Expired token detected'
        });
        jwtManager.clearTokens();
      }
      
      // Check for security issues
      checkSecurityIssues();
      
    } catch (error) {
      console.error('Security initialization failed:', error);
      setIsSecure(false);
      setSecurityIssues(['Security initialization failed']);
    }
  };
  
  const checkSecurityIssues = () => {
    const issues: string[] = [];
    
    // Check HTTPS
    if (process.env.NODE_ENV === 'production' && !window.location.protocol.includes('https')) {
      issues.push('HTTPS not enforced in production');
    }
    
    // Check for insecure storage
    if (localStorage.getItem('password') || localStorage.getItem('secret')) {
      issues.push('Sensitive data stored in localStorage');
    }
    
    // Check for missing security headers
    if (!document.querySelector('meta[name="csrf-token"]')) {
      issues.push('CSRF token meta tag missing');
    }
    
    setSecurityIssues(issues);
    setIsSecure(issues.length === 0);
  };
  
  const sanitizeInput = (input: any, type: string) => {
    try {
      return inputSanitizer.sanitize(input, type as any);
    } catch (error) {
      logSecurityEvent(SECURITY_EVENTS.SUSPICIOUS_ACTIVITY, {
        reason: 'Input sanitization failed',
        error: error.message
      });
      return input;
    }
  };
  
  const sanitizeFormData = (data: Record<string, any>) => {
    try {
      return inputSanitizer.sanitizeObject(data, {
        // Default sanitization rules
        username: 'text',
        email: 'email',
        password: 'text',
        phone: 'phone',
        url: 'url',
        description: 'html',
        search: 'search'
      });
    } catch (error) {
      logSecurityEvent(SECURITY_EVENTS.SUSPICIOUS_ACTIVITY, {
        reason: 'Form data sanitization failed',
        error: error.message
      });
      return data;
    }
  };
  
  const getCsrfToken = () => {
    return csrfManager.getToken();
  };
  
  const validateCsrfToken = (token: string) => {
    return csrfManager.validateToken(token);
  };
  
  const isSessionValid = () => {
    return secureSessionManager.isSessionValid();
  };
  
  const updateSessionActivity = () => {
    secureSessionManager.updateSessionActivity();
  };
  
  const setSecureItem = (key: string, value: any) => {
    secureStorageManager.setItem(key, value);
  };
  
  const getSecureItem = <T>(key: string): T | null => {
    return secureStorageManager.getItem<T>(key);
  };
  
  const removeSecureItem = (key: string) => {
    secureStorageManager.removeItem(key);
  };
  
  const getJWTToken = () => {
    return jwtManager.getToken();
  };
  
  const isJWTExpired = () => {
    const token = jwtManager.getToken();
    return token ? jwtManager.isTokenExpired(token) : true;
  };
  
  const contextValue: SecurityContextType = {
    sanitizeInput,
    sanitizeFormData,
    getCsrfToken,
    validateCsrfToken,
    isSessionValid,
    updateSessionActivity,
    logSecurityEvent,
    setSecureItem,
    getSecureItem,
    removeSecureItem,
    getJWTToken,
    isJWTExpired,
    isSecure,
    securityIssues
  };
  
  return (
    <SecurityContext.Provider value={contextValue}>
      {children}
    </SecurityContext.Provider>
  );
};

// Custom hook to use security context
export const useSecurity = (): SecurityContextType => {
  const context = useContext(SecurityContext);
  if (!context) {
    throw new Error('useSecurity must be used within a SecurityProvider');
  }
  return context;
};

// Security HOC for components
export const withSecurity = <P extends object>(Component: React.ComponentType<P>) => {
  return (props: P) => {
    const security = useSecurity();
    
    // Log component access
    useEffect(() => {
      security.logSecurityEvent(SECURITY_EVENTS.DATA_ACCESS, {
        component: Component.name,
        timestamp: new Date().toISOString()
      });
    }, [security]);
    
    return <Component {...props} />;
  };
};

// Security wrapper for forms
export const SecureForm: React.FC<{
  children: ReactNode;
  onSubmit: (data: any) => void;
  sanitizeRules?: Record<string, string>;
}> = ({ children, onSubmit, sanitizeRules = {} }) => {
  const security = useSecurity();
  
  const handleSubmit = (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    
    const formData = new FormData(event.currentTarget);
    const data: Record<string, any> = {};
    
    for (const [key, value] of formData.entries()) {
      data[key] = value;
    }
    
    // Sanitize form data
    const sanitizedData = security.sanitizeFormData(data);
    
    // Log form submission
    security.logSecurityEvent(SECURITY_EVENTS.DATA_ACCESS, {
      form: event.currentTarget.name || 'unknown',
      fields: Object.keys(sanitizedData)
    });
    
    onSubmit(sanitizedData);
  };
  
  return (
    <form onSubmit={handleSubmit}>
      {children}
    </form>
  );
};

// Security wrapper for API calls
export const secureApiCall = async (
  apiCall: () => Promise<any>,
  security: SecurityContextType
): Promise<any> => {
  try {
    // Validate session
    if (!security.isSessionValid()) {
      throw new Error('Invalid session');
    }
    
    // Update session activity
    security.updateSessionActivity();
    
    // Make API call
    const result = await apiCall();
    
    // Log successful API call
    security.logSecurityEvent(SECURITY_EVENTS.DATA_ACCESS, {
      success: true,
      timestamp: new Date().toISOString()
    });
    
    return result;
  } catch (error) {
    // Log failed API call
    security.logSecurityEvent(SECURITY_EVENTS.SUSPICIOUS_ACTIVITY, {
      reason: 'API call failed',
      error: error.message
    });
    
    throw error;
  }
};
