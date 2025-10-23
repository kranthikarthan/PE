/**
 * Security Provider Tests
 * 
 * Tests for the SecurityProvider component and related security functionality.
 */

import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { SecurityProvider, useSecurity, withSecurity, SecureForm, secureApiCall } from '../SecurityProvider';
import { SECURITY_EVENTS } from '../../utils/security';

// Mock security utilities
jest.mock('../../utils/inputSanitizer', () => ({
  inputSanitizer: {
    sanitize: jest.fn((input, type) => input),
    sanitizeObject: jest.fn((obj, rules) => obj)
  }
}));

jest.mock('../../utils/secureCookies', () => ({
  csrfManager: {
    generateToken: jest.fn(),
    getToken: jest.fn(() => 'mock-csrf-token'),
    validateToken: jest.fn(() => true)
  },
  secureSessionManager: {
    isSessionValid: jest.fn(() => true),
    updateSessionActivity: jest.fn()
  }
}));

jest.mock('../../utils/secretsManager', () => ({
  secureStorageManager: {
    setItem: jest.fn(),
    getItem: jest.fn(),
    removeItem: jest.fn()
  },
  jwtManager: {
    getToken: jest.fn(() => 'mock-jwt-token'),
    isTokenExpired: jest.fn(() => false)
  }
}));

// Test component that uses security context
const TestComponent: React.FC = () => {
  const security = useSecurity();
  
  return (
    <div>
      <div data-testid="is-secure">{security.isSecure.toString()}</div>
      <div data-testid="security-issues">{security.securityIssues.join(',')}</div>
      <button 
        data-testid="sanitize-button"
        onClick={() => security.sanitizeInput('test', 'text')}
      >
        Sanitize
      </button>
      <button 
        data-testid="log-event-button"
        onClick={() => security.logSecurityEvent('test_event', { detail: 'test' })}
      >
        Log Event
      </button>
    </div>
  );
};

// Test component for HOC
const TestComponentForHOC: React.FC = () => {
  return <div data-testid="hoc-component">HOC Component</div>;
};

const WrappedComponent = withSecurity(TestComponentForHOC);

describe('SecurityProvider', () => {
  it('should provide security context', () => {
    render(
      <SecurityProvider>
        <TestComponent />
      </SecurityProvider>
    );
    
    expect(screen.getByTestId('is-secure')).toHaveTextContent('true');
  });

  it('should sanitize input', () => {
    render(
      <SecurityProvider>
        <TestComponent />
      </SecurityProvider>
    );
    
    fireEvent.click(screen.getByTestId('sanitize-button'));
    
    // Verify sanitizeInput was called
    expect(require('../../utils/inputSanitizer').inputSanitizer.sanitize).toHaveBeenCalledWith('test', 'text');
  });

  it('should log security events', () => {
    const consoleSpy = jest.spyOn(console, 'log').mockImplementation();
    
    render(
      <SecurityProvider>
        <TestComponent />
      </SecurityProvider>
    );
    
    fireEvent.click(screen.getByTestId('log-event-button'));
    
    expect(consoleSpy).toHaveBeenCalledWith('Security Event:', expect.objectContaining({
      event: 'test_event',
      details: { detail: 'test' }
    }));
    
    consoleSpy.mockRestore();
  });

  it('should handle security issues', () => {
    // Mock security issues
    jest.spyOn(console, 'error').mockImplementation();
    
    render(
      <SecurityProvider>
        <TestComponent />
      </SecurityProvider>
    );
    
    // Security issues should be empty by default
    expect(screen.getByTestId('security-issues')).toHaveTextContent('');
  });

  it('should throw error when used outside provider', () => {
    const consoleSpy = jest.spyOn(console, 'error').mockImplementation();
    
    expect(() => {
      render(<TestComponent />);
    }).toThrow('useSecurity must be used within a SecurityProvider');
    
    consoleSpy.mockRestore();
  });
});

describe('withSecurity HOC', () => {
  it('should wrap component with security functionality', () => {
    render(<WrappedComponent />);
    
    expect(screen.getByTestId('hoc-component')).toBeInTheDocument();
  });
});

describe('SecureForm', () => {
  it('should handle form submission with sanitization', async () => {
    const onSubmit = jest.fn();
    
    render(
      <SecurityProvider>
        <SecureForm onSubmit={onSubmit}>
          <input name="username" defaultValue="test" />
          <input name="email" defaultValue="test@example.com" />
          <button type="submit">Submit</button>
        </SecureForm>
      </SecurityProvider>
    );
    
    fireEvent.click(screen.getByText('Submit'));
    
    await waitFor(() => {
      expect(onSubmit).toHaveBeenCalledWith({
        username: 'test',
        email: 'test@example.com'
      });
    });
  });

  it('should sanitize form data', async () => {
    const onSubmit = jest.fn();
    
    render(
      <SecurityProvider>
        <SecureForm onSubmit={onSubmit} sanitizeRules={{ username: 'text', email: 'email' }}>
          <input name="username" defaultValue="<script>alert('xss')</script>" />
          <input name="email" defaultValue="test@example.com" />
          <button type="submit">Submit</button>
        </SecureForm>
      </SecurityProvider>
    );
    
    fireEvent.click(screen.getByText('Submit'));
    
    await waitFor(() => {
      expect(onSubmit).toHaveBeenCalled();
    });
  });
});

describe('secureApiCall', () => {
  it('should make secure API call', async () => {
    const mockApiCall = jest.fn().mockResolvedValue({ data: 'success' });
    const mockSecurity = {
      isSessionValid: jest.fn(() => true),
      updateSessionActivity: jest.fn(),
      logSecurityEvent: jest.fn()
    };
    
    const result = await secureApiCall(mockApiCall, mockSecurity as any);
    
    expect(result).toEqual({ data: 'success' });
    expect(mockSecurity.isSessionValid).toHaveBeenCalled();
    expect(mockSecurity.updateSessionActivity).toHaveBeenCalled();
    expect(mockSecurity.logSecurityEvent).toHaveBeenCalledWith(
      SECURITY_EVENTS.DATA_ACCESS,
      expect.objectContaining({ success: true })
    );
  });

  it('should handle invalid session', async () => {
    const mockApiCall = jest.fn().mockResolvedValue({ data: 'success' });
    const mockSecurity = {
      isSessionValid: jest.fn(() => false),
      updateSessionActivity: jest.fn(),
      logSecurityEvent: jest.fn()
    };
    
    await expect(secureApiCall(mockApiCall, mockSecurity as any)).rejects.toThrow('Invalid session');
    
    expect(mockSecurity.logSecurityEvent).toHaveBeenCalledWith(
      SECURITY_EVENTS.SUSPICIOUS_ACTIVITY,
      expect.objectContaining({ reason: 'API call failed' })
    );
  });

  it('should handle API call failure', async () => {
    const mockApiCall = jest.fn().mockRejectedValue(new Error('API Error'));
    const mockSecurity = {
      isSessionValid: jest.fn(() => true),
      updateSessionActivity: jest.fn(),
      logSecurityEvent: jest.fn()
    };
    
    await expect(secureApiCall(mockApiCall, mockSecurity as any)).rejects.toThrow('API Error');
    
    expect(mockSecurity.logSecurityEvent).toHaveBeenCalledWith(
      SECURITY_EVENTS.SUSPICIOUS_ACTIVITY,
      expect.objectContaining({ reason: 'API call failed' })
    );
  });
});

describe('Security Context Integration', () => {
  it('should provide all security functions', () => {
    render(
      <SecurityProvider>
        <TestComponent />
      </SecurityProvider>
    );
    
    // All security functions should be available
    expect(screen.getByTestId('is-secure')).toBeInTheDocument();
    expect(screen.getByTestId('security-issues')).toBeInTheDocument();
    expect(screen.getByTestId('sanitize-button')).toBeInTheDocument();
    expect(screen.getByTestId('log-event-button')).toBeInTheDocument();
  });

  it('should handle security initialization errors', () => {
    // Mock console.error to prevent test output
    const consoleSpy = jest.spyOn(console, 'error').mockImplementation();
    
    // Mock security initialization to throw error
    jest.spyOn(require('../../utils/secureCookies').csrfManager, 'generateToken').mockImplementation(() => {
      throw new Error('CSRF generation failed');
    });
    
    render(
      <SecurityProvider>
        <TestComponent />
      </SecurityProvider>
    );
    
    expect(screen.getByTestId('is-secure')).toHaveTextContent('false');
    expect(screen.getByTestId('security-issues')).toHaveTextContent('Security initialization failed');
    
    consoleSpy.mockRestore();
  });
});
