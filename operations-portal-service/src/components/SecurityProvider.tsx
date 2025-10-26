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
  sanitizeInput: (input: any, type: 'search' | 'text' | 'html' | 'url' | 'email' | 'phone' | 'numeric' | 'date' | 'json' | 'filename' | 'sql' | 'xss') => any;
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
  secureStore: (key: string, value: any) => void;
  secureRetrieve: (key: string) => any;
  secureRemove: (key: string) => void;
  
  // JWT Management
  getJwtToken: () => string | null;
  validateJwtToken: (token: string) => boolean;
  refreshJwtToken: () => Promise<string | null>;
}

const SecurityContext = createContext<SecurityContextType | null>(null);

interface SecurityProviderProps {
  children: ReactNode;
}

export const SecurityProvider: React.FC<SecurityProviderProps> = ({ children }) => {
  const [csrfToken, setCsrfToken] = useState<string | null>(null);

  useEffect(() => {
    // Initialize CSRF token
    const token = csrfManager.getToken();
    setCsrfToken(token);
  }, []);

  const sanitizeInput = (input: any, type: 'search' | 'text' | 'html' | 'url' | 'email' | 'phone' | 'numeric' | 'date' | 'json' | 'filename' | 'sql' | 'xss'): any => {
    return inputSanitizer.sanitize(input, type);
  };

  const sanitizeFormData = (data: Record<string, any>): Record<string, any> => {
    const sanitized: Record<string, any> = {};
    for (const [key, value] of Object.entries(data)) {
      sanitized[key] = inputSanitizer.sanitize(value, 'text');
    }
    return sanitized;
  };

  const getCsrfToken = (): string | null => {
    return csrfManager.getToken();
  };

  const validateCsrfToken = (token: string): boolean => {
    return csrfManager.validateToken(token);
  };

  const isSessionValid = (): boolean => {
    return secureSessionManager.isSessionValid();
  };

  const updateSessionActivity = (): void => {
    secureSessionManager.updateSessionActivity();
  };

  const logSecurityEvent = (event: string, details: Record<string, any>): void => {
    console.log('Security Event:', { event, details, timestamp: new Date().toISOString() });
  };

  const secureStore = (key: string, value: any): void => {
    secureStorageManager.setItem(key, value);
  };

  const secureRetrieve = (key: string): any => {
    return secureStorageManager.getItem(key);
  };

  const secureRemove = (key: string): void => {
    secureStorageManager.removeItem(key);
  };

  const getJwtToken = (): string | null => {
    return jwtManager.getToken();
  };

  const validateJwtToken = (token: string): boolean => {
    return !jwtManager.isTokenExpired(token);
  };

  const refreshJwtToken = async (): Promise<string | null> => {
    const refreshToken = jwtManager.getRefreshToken();
    if (!refreshToken) return null;
    
    // In a real implementation, this would make an API call to refresh the token
    // For now, we'll just return the current token
    return jwtManager.getToken();
  };

  const contextValue: SecurityContextType = {
    sanitizeInput,
    sanitizeFormData,
    getCsrfToken,
    validateCsrfToken,
    isSessionValid,
    updateSessionActivity,
    logSecurityEvent,
    secureStore,
    secureRetrieve,
    secureRemove,
    getJwtToken,
    validateJwtToken,
    refreshJwtToken,
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
export const withSecurity = <P extends object,>(Component: React.ComponentType<P>) => {
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
    
    formData.forEach((value, key) => {
      data[key] = value;
    });
    
    // Sanitize form data
    const sanitizedData = security.sanitizeFormData(data);
    
    // Log form submission
    security.logSecurityEvent(SECURITY_EVENTS.DATA_ACCESS, {
      form: event.currentTarget.id || 'unknown',
      timestamp: new Date().toISOString()
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
export const secureApiCall = async <T,>(
  apiCall: () => Promise<T>,
  security: SecurityContextType
): Promise<T> => {
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
      error: (error as Error).message
    });
    
    throw error;
  }
};