/**
 * Secure Cookie Management
 * 
 * Utilities for secure cookie handling with proper security attributes
 * and protection against common cookie-based attacks.
 */

// Cookie Security Configuration
export const COOKIE_CONFIG = {
  // Security attributes
  httpOnly: true,
  secure: process.env.NODE_ENV === 'production',
  sameSite: 'strict' as const,
  
  // Expiration settings
  sessionExpiry: 24 * 60 * 60 * 1000, // 24 hours
  rememberMeExpiry: 30 * 24 * 60 * 60 * 1000, // 30 days
  
  // Path and domain
  path: '/',
  domain: process.env.REACT_APP_COOKIE_DOMAIN || undefined
};

// Secure Cookie Setter
export const setSecureCookie = (
  name: string,
  value: string,
  options: {
    expires?: Date;
    maxAge?: number;
    httpOnly?: boolean;
    secure?: boolean;
    sameSite?: 'strict' | 'lax' | 'none';
    path?: string;
    domain?: string;
  } = {}
): void => {
  const {
    expires,
    maxAge,
    httpOnly = COOKIE_CONFIG.httpOnly,
    secure = COOKIE_CONFIG.secure,
    sameSite = COOKIE_CONFIG.sameSite,
    path = COOKIE_CONFIG.path,
    domain = COOKIE_CONFIG.domain
  } = options;
  
  let cookieString = `${encodeURIComponent(name)}=${encodeURIComponent(value)}`;
  
  if (expires) {
    cookieString += `; expires=${expires.toUTCString()}`;
  }
  
  if (maxAge) {
    cookieString += `; max-age=${maxAge}`;
  }
  
  if (httpOnly) {
    cookieString += '; HttpOnly';
  }
  
  if (secure) {
    cookieString += '; Secure';
  }
  
  if (sameSite) {
    cookieString += `; SameSite=${sameSite}`;
  }
  
  if (path) {
    cookieString += `; path=${path}`;
  }
  
  if (domain) {
    cookieString += `; domain=${domain}`;
  }
  
  document.cookie = cookieString;
};

// Secure Cookie Getter
export const getSecureCookie = (name: string): string | null => {
  const cookies = document.cookie.split(';');
  
  for (const cookie of cookies) {
    const [cookieName, cookieValue] = cookie.trim().split('=');
    if (cookieName === name) {
      return decodeURIComponent(cookieValue);
    }
  }
  
  return null;
};

// Remove Cookie
export const removeSecureCookie = (name: string, path: string = COOKIE_CONFIG.path, domain?: string): void => {
  const expires = new Date(0).toUTCString();
  setSecureCookie(name, '', {
    expires: new Date(0),
    path,
    domain
  });
};

// Clear All Cookies
export const clearAllCookies = (): void => {
  const cookies = document.cookie.split(';');
  
  for (const cookie of cookies) {
    const [name] = cookie.trim().split('=');
    if (name) {
      removeSecureCookie(name);
    }
  }
};

// Cookie Security Validator
export const validateCookieSecurity = (cookieString: string): {
  isValid: boolean;
  issues: string[];
} => {
  const issues: string[] = [];
  
  if (!cookieString.includes('HttpOnly')) {
    issues.push('Missing HttpOnly flag');
  }
  
  if (!cookieString.includes('Secure')) {
    issues.push('Missing Secure flag');
  }
  
  if (!cookieString.includes('SameSite')) {
    issues.push('Missing SameSite flag');
  }
  
  if (cookieString.includes('SameSite=None') && !cookieString.includes('Secure')) {
    issues.push('SameSite=None requires Secure flag');
  }
  
  return {
    isValid: issues.length === 0,
    issues
  };
};

// Session Management
export class SecureSessionManager {
  private static instance: SecureSessionManager;
  private sessionId: string | null = null;
  private sessionData: Record<string, any> = {};
  
  static getInstance(): SecureSessionManager {
    if (!SecureSessionManager.instance) {
      SecureSessionManager.instance = new SecureSessionManager();
    }
    return SecureSessionManager.instance;
  }
  
  createSession(userId: string, rememberMe: boolean = false): string {
    const sessionId = this.generateSessionId();
    const expires = rememberMe ? COOKIE_CONFIG.rememberMeExpiry : COOKIE_CONFIG.sessionExpiry;
    
    this.sessionId = sessionId;
    this.sessionData = {
      userId,
      createdAt: Date.now(),
      lastActivity: Date.now(),
      rememberMe
    };
    
    // Store session data securely
    setSecureCookie('sessionId', sessionId, {
      maxAge: expires,
      httpOnly: true,
      secure: COOKIE_CONFIG.secure,
      sameSite: 'strict'
    });
    
    // Store session data in memory (in production, this would be server-side)
    sessionStorage.setItem('sessionData', JSON.stringify(this.sessionData));
    
    return sessionId;
  }
  
  getSession(): { sessionId: string | null; sessionData: Record<string, any> } {
    const sessionId = getSecureCookie('sessionId');
    
    if (!sessionId) {
      return { sessionId: null, sessionData: {} };
    }
    
    try {
      const sessionData = JSON.parse(sessionStorage.getItem('sessionData') || '{}');
      return { sessionId, sessionData };
    } catch {
      return { sessionId: null, sessionData: {} };
    }
  }
  
  updateSessionActivity(): void {
    const { sessionId, sessionData } = this.getSession();
    
    if (sessionId && sessionData) {
      sessionData.lastActivity = Date.now();
      sessionStorage.setItem('sessionData', JSON.stringify(sessionData));
    }
  }
  
  destroySession(): void {
    const { sessionId } = this.getSession();
    
    if (sessionId) {
      removeSecureCookie('sessionId');
      sessionStorage.removeItem('sessionData');
      this.sessionId = null;
      this.sessionData = {};
    }
  }
  
  isSessionValid(): boolean {
    const { sessionId, sessionData } = this.getSession();
    
    if (!sessionId || !sessionData) {
      return false;
    }
    
    const now = Date.now();
    const sessionExpiry = sessionData.rememberMe ? COOKIE_CONFIG.rememberMeExpiry : COOKIE_CONFIG.sessionExpiry;
    
    return (now - sessionData.lastActivity) < sessionExpiry;
  }
  
  private generateSessionId(): string {
    const array = new Uint8Array(32);
    crypto.getRandomValues(array);
    return Array.from(array, byte => byte.toString(16).padStart(2, '0')).join('');
  }
}

// CSRF Token Management
export class CSRFManager {
  private static instance: CSRFManager;
  private csrfToken: string | null = null;
  
  static getInstance(): CSRFManager {
    if (!CSRFManager.instance) {
      CSRFManager.instance = new CSRFManager();
    }
    return CSRFManager.instance;
  }
  
  generateToken(): string {
    const array = new Uint8Array(32);
    crypto.getRandomValues(array);
    const token = Array.from(array, byte => byte.toString(16).padStart(2, '0')).join('');
    
    this.csrfToken = token;
    
    // Store token in secure cookie
    setSecureCookie('csrfToken', token, {
      maxAge: COOKIE_CONFIG.sessionExpiry,
      httpOnly: true,
      secure: COOKIE_CONFIG.secure,
      sameSite: 'strict'
    });
    
    return token;
  }
  
  getToken(): string | null {
    if (!this.csrfToken) {
      this.csrfToken = getSecureCookie('csrfToken');
    }
    return this.csrfToken;
  }
  
  validateToken(token: string): boolean {
    const storedToken = this.getToken();
    
    if (!storedToken || !token) {
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
  }
  
  clearToken(): void {
    this.csrfToken = null;
    removeSecureCookie('csrfToken');
  }
}

// Authentication Token Management
export class AuthTokenManager {
  private static instance: AuthTokenManager;
  
  static getInstance(): AuthTokenManager {
    if (!AuthTokenManager.instance) {
      AuthTokenManager.instance = new AuthTokenManager();
    }
    return AuthTokenManager.instance;
  }
  
  setAuthToken(token: string, rememberMe: boolean = false): void {
    const expires = rememberMe ? COOKIE_CONFIG.rememberMeExpiry : COOKIE_CONFIG.sessionExpiry;
    
    setSecureCookie('authToken', token, {
      maxAge: expires,
      httpOnly: true,
      secure: COOKIE_CONFIG.secure,
      sameSite: 'strict'
    });
  }
  
  getAuthToken(): string | null {
    return getSecureCookie('authToken');
  }
  
  clearAuthToken(): void {
    removeSecureCookie('authToken');
  }
  
  isTokenExpired(token: string): boolean {
    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      return Date.now() >= payload.exp * 1000;
    } catch {
      return true;
    }
  }
}

// Export singleton instances
export const secureSessionManager = SecureSessionManager.getInstance();
export const csrfManager = CSRFManager.getInstance();
export const authTokenManager = AuthTokenManager.getInstance();
