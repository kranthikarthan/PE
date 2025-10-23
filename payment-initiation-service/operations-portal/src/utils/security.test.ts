/**
 * Security Utilities Tests
 * 
 * Comprehensive tests for security utilities including input sanitization,
 * XSS protection, CSRF protection, and secure data handling.
 */

import {
  escapeHtml,
  sanitizeHtml,
  sanitizeInput,
  generateCsrfToken,
  validateCsrfToken,
  maskSensitiveData,
  validateEmail,
  validatePassword,
  validateAmount,
  validateAccountNumber,
  validatePhoneNumber,
  generateCSPHeader,
  secureStorage,
  RateLimiter,
  logSecurityEvent,
  SECURITY_EVENTS
} from './security';

describe('Security Utilities', () => {
  describe('XSS Protection', () => {
    it('should escape HTML characters', () => {
      expect(escapeHtml('<script>alert("xss")</script>')).toBe('&lt;script&gt;alert(&quot;xss&quot;)&lt;/script&gt;');
      expect(escapeHtml('Hello & World')).toBe('Hello &amp; World');
      expect(escapeHtml('Test "quotes" and \'apostrophes\'')).toBe('Test &quot;quotes&quot; and &#039;apostrophes&#039;');
    });

    it('should sanitize HTML content', () => {
      expect(sanitizeHtml('<script>alert("xss")</script>')).toBe('');
      expect(sanitizeHtml('<iframe src="malicious.com"></iframe>')).toBe('');
      expect(sanitizeHtml('<div onclick="alert(1)">Click me</div>')).toBe('<div>Click me</div>');
      expect(sanitizeHtml('javascript:alert(1)')).toBe('');
    });

    it('should sanitize user input', () => {
      expect(sanitizeInput('<script>alert("xss")</script>')).toBe('alert("xss")');
      expect(sanitizeInput('javascript:alert(1)')).toBe('alert(1)');
      expect(sanitizeInput('onclick="alert(1)"')).toBe('alert(1)');
      expect(sanitizeInput('Hello World')).toBe('Hello World');
    });
  });

  describe('CSRF Protection', () => {
    it('should generate CSRF token', () => {
      const token1 = generateCsrfToken();
      const token2 = generateCsrfToken();
      
      expect(token1).toBeDefined();
      expect(token2).toBeDefined();
      expect(token1).not.toBe(token2);
      expect(token1.length).toBe(64);
    });

    it('should validate CSRF token', () => {
      const token = generateCsrfToken();
      const storedToken = token;
      
      expect(validateCsrfToken(token, storedToken)).toBe(true);
      expect(validateCsrfToken(token, 'invalid')).toBe(false);
      expect(validateCsrfToken('', '')).toBe(false);
    });
  });

  describe('Sensitive Data Masking', () => {
    it('should mask account numbers', () => {
      expect(maskSensitiveData('1234567890123456', 'account')).toBe('1234****3456');
      expect(maskSensitiveData('1234567890', 'account')).toBe('1234****90');
    });

    it('should mask email addresses', () => {
      expect(maskSensitiveData('john.doe@example.com', 'email')).toBe('jo***@example.com');
      expect(maskSensitiveData('a@b.com', 'email')).toBe('a***@b.com');
    });

    it('should mask phone numbers', () => {
      expect(maskSensitiveData('+1234567890', 'phone')).toBe('+123***7890');
      expect(maskSensitiveData('1234567890', 'phone')).toBe('123***7890');
    });

    it('should mask SSN', () => {
      expect(maskSensitiveData('123-45-6789', 'ssn')).toBe('123-**-6789');
    });

    it('should mask credit card numbers', () => {
      expect(maskSensitiveData('1234567890123456', 'creditcard')).toBe('1234-****-****-3456');
    });
  });

  describe('Input Validation', () => {
    it('should validate email addresses', () => {
      expect(validateEmail('test@example.com')).toBe(true);
      expect(validateEmail('user.name@domain.co.uk')).toBe(true);
      expect(validateEmail('invalid-email')).toBe(false);
      expect(validateEmail('')).toBe(false);
      expect(validateEmail('a'.repeat(300) + '@example.com')).toBe(false);
    });

    it('should validate passwords', () => {
      const weakPassword = validatePassword('123');
      expect(weakPassword.isValid).toBe(false);
      expect(weakPassword.errors).toContain('Password must be at least 8 characters long');

      const strongPassword = validatePassword('StrongPass123!');
      expect(strongPassword.isValid).toBe(true);
      expect(strongPassword.errors).toHaveLength(0);

      const repeatedChars = validatePassword('aaa123456');
      expect(repeatedChars.isValid).toBe(false);
      expect(repeatedChars.errors).toContain('Password must not contain repeated characters');
    });

    it('should validate amounts', () => {
      expect(validateAmount(100)).toBe(true);
      expect(validateAmount(0.01)).toBe(true);
      expect(validateAmount(999999999.99)).toBe(true);
      expect(validateAmount(0)).toBe(false);
      expect(validateAmount(-100)).toBe(false);
      expect(validateAmount(1000000000)).toBe(false);
      expect(validateAmount(NaN)).toBe(false);
    });

    it('should validate account numbers', () => {
      expect(validateAccountNumber('12345678')).toBe(true);
      expect(validateAccountNumber('12345678901234567890')).toBe(true);
      expect(validateAccountNumber('1234567')).toBe(false);
      expect(validateAccountNumber('123456789012345678901')).toBe(false);
      expect(validateAccountNumber('1234567a')).toBe(false);
    });

    it('should validate phone numbers', () => {
      expect(validatePhoneNumber('+1234567890')).toBe(true);
      expect(validatePhoneNumber('1234567890')).toBe(true);
      expect(validatePhoneNumber('+1-234-567-8900')).toBe(true);
      expect(validatePhoneNumber('abc123')).toBe(false);
      expect(validatePhoneNumber('')).toBe(false);
    });
  });

  describe('Content Security Policy', () => {
    it('should generate CSP header', () => {
      const csp = generateCSPHeader();
      
      expect(csp).toContain("default-src 'self'");
      expect(csp).toContain("script-src 'self'");
      expect(csp).toContain("style-src 'self'");
      expect(csp).toContain("img-src 'self'");
      expect(csp).toContain("connect-src 'self'");
    });
  });

  describe('Secure Storage', () => {
    beforeEach(() => {
      localStorage.clear();
    });

    it('should store and retrieve data securely', () => {
      const testData = { user: 'test', id: 123 };
      
      secureStorage.setItem('test', testData);
      const retrieved = secureStorage.getItem('test');
      
      expect(retrieved).toEqual(testData);
    });

    it('should handle null values', () => {
      secureStorage.setItem('test', null);
      const retrieved = secureStorage.getItem('test');
      
      expect(retrieved).toBeNull();
    });

    it('should handle errors gracefully', () => {
      // Mock localStorage to throw error
      const originalSetItem = localStorage.setItem;
      localStorage.setItem = jest.fn(() => {
        throw new Error('Storage error');
      });
      
      const consoleSpy = jest.spyOn(console, 'error').mockImplementation();
      
      secureStorage.setItem('test', 'value');
      
      expect(consoleSpy).toHaveBeenCalledWith('Failed to store data securely:', expect.any(Error));
      
      localStorage.setItem = originalSetItem;
      consoleSpy.mockRestore();
    });
  });

  describe('Rate Limiting', () => {
    let rateLimiter: RateLimiter;

    beforeEach(() => {
      rateLimiter = new RateLimiter(1000, 3); // 1 second window, 3 requests max
    });

    it('should allow requests within limit', () => {
      expect(rateLimiter.isAllowed('user1')).toBe(true);
      expect(rateLimiter.isAllowed('user1')).toBe(true);
      expect(rateLimiter.isAllowed('user1')).toBe(true);
    });

    it('should block requests exceeding limit', () => {
      rateLimiter.isAllowed('user1');
      rateLimiter.isAllowed('user1');
      rateLimiter.isAllowed('user1');
      
      expect(rateLimiter.isAllowed('user1')).toBe(false);
    });

    it('should track remaining requests', () => {
      rateLimiter.isAllowed('user1');
      rateLimiter.isAllowed('user1');
      
      expect(rateLimiter.getRemainingRequests('user1')).toBe(1);
    });

    it('should reset user requests', () => {
      rateLimiter.isAllowed('user1');
      rateLimiter.isAllowed('user1');
      rateLimiter.isAllowed('user1');
      
      expect(rateLimiter.isAllowed('user1')).toBe(false);
      
      rateLimiter.reset('user1');
      expect(rateLimiter.isAllowed('user1')).toBe(true);
    });
  });

  describe('Security Event Logging', () => {
    it('should have security event constants', () => {
      expect(SECURITY_EVENTS.LOGIN_ATTEMPT).toBe('login_attempt');
      expect(SECURITY_EVENTS.LOGIN_SUCCESS).toBe('login_success');
      expect(SECURITY_EVENTS.LOGIN_FAILURE).toBe('login_failure');
      expect(SECURITY_EVENTS.LOGOUT).toBe('logout');
      expect(SECURITY_EVENTS.PERMISSION_DENIED).toBe('permission_denied');
      expect(SECURITY_EVENTS.SUSPICIOUS_ACTIVITY).toBe('suspicious_activity');
    });

    it('should log security events', () => {
      const consoleSpy = jest.spyOn(console, 'log').mockImplementation();
      
      logSecurityEvent('test_event', { detail: 'test' });
      
      expect(consoleSpy).toHaveBeenCalledWith('Security Event:', expect.objectContaining({
        event: 'test_event',
        details: { detail: 'test' }
      }));
      
      consoleSpy.mockRestore();
    });
  });
});
