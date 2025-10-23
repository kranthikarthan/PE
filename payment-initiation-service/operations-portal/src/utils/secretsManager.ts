/**
 * Secrets Management
 * 
 * Secure handling of sensitive data, environment variables,
 * and secrets with proper encryption and access control.
 */

// Environment Variable Validation
export const validateEnvironmentVariables = (): {
  isValid: boolean;
  missing: string[];
  invalid: string[];
} => {
  const requiredVars = [
    'REACT_APP_API_BASE_URL',
    'REACT_APP_AUTH_SERVICE_URL',
    'REACT_APP_OPERATIONS_SERVICE_URL'
  ];
  
  const missing: string[] = [];
  const invalid: string[] = [];
  
  for (const varName of requiredVars) {
    const value = process.env[varName];
    
    if (!value) {
      missing.push(varName);
    } else if (typeof value !== 'string' || value.trim() === '') {
      invalid.push(varName);
    }
  }
  
  return {
    isValid: missing.length === 0 && invalid.length === 0,
    missing,
    invalid
  };
};

// Secure Environment Configuration
export const getSecureConfig = (): Record<string, any> => {
  const config = {
    // API Configuration
    apiBaseUrl: process.env.REACT_APP_API_BASE_URL || 'http://localhost:8080',
    authServiceUrl: process.env.REACT_APP_AUTH_SERVICE_URL || 'http://localhost:8081',
    operationsServiceUrl: process.env.REACT_APP_OPERATIONS_SERVICE_URL || 'http://localhost:8082',
    paymentServiceUrl: process.env.REACT_APP_PAYMENT_SERVICE_URL || 'http://localhost:8083',
    transactionServiceUrl: process.env.REACT_APP_TRANSACTION_SERVICE_URL || 'http://localhost:8084',
    reconciliationServiceUrl: process.env.REACT_APP_RECONCILIATION_SERVICE_URL || 'http://localhost:8085',
    
    // Security Configuration
    enableHttps: process.env.REACT_APP_ENABLE_HTTPS === 'true',
    enableDebug: process.env.REACT_APP_ENABLE_DEBUG === 'true',
    enableMockData: process.env.REACT_APP_ENABLE_MOCK_DATA === 'true',
    
    // Performance Configuration
    cacheTtl: parseInt(process.env.REACT_APP_CACHE_TTL || '300000', 10),
    requestTimeout: parseInt(process.env.REACT_APP_REQUEST_TIMEOUT || '30000', 10),
    retryAttempts: parseInt(process.env.REACT_APP_RETRY_ATTEMPTS || '3', 10),
    
    // Session Configuration
    sessionTimeout: parseInt(process.env.REACT_APP_SESSION_TIMEOUT || '3600000', 10),
    
    // Monitoring Configuration
    enableMonitoring: process.env.REACT_APP_ENABLE_MONITORING === 'true',
    metricsEndpoint: process.env.REACT_APP_METRICS_ENDPOINT || '/metrics'
  };
  
  return config;
};

// Client-Side Encryption
export class ClientEncryption {
  private static instance: ClientEncryption;
  private encryptionKey: string | null = null;
  
  static getInstance(): ClientEncryption {
    if (!ClientEncryption.instance) {
      ClientEncryption.instance = new ClientEncryption();
    }
    return ClientEncryption.instance;
  }
  
  async generateKey(): Promise<CryptoKey> {
    return crypto.subtle.generateKey(
      {
        name: 'AES-GCM',
        length: 256
      },
      true,
      ['encrypt', 'decrypt']
    );
  }
  
  async encrypt(data: string, key: CryptoKey): Promise<string> {
    const encoder = new TextEncoder();
    const dataBuffer = encoder.encode(data);
    
    const iv = crypto.getRandomValues(new Uint8Array(12));
    
    const encrypted = await crypto.subtle.encrypt(
      {
        name: 'AES-GCM',
        iv: iv
      },
      key,
      dataBuffer
    );
    
    const result = new Uint8Array(iv.length + encrypted.byteLength);
    result.set(iv);
    result.set(new Uint8Array(encrypted), iv.length);
    
    return btoa(String.fromCharCode(...result));
  }
  
  async decrypt(encryptedData: string, key: CryptoKey): Promise<string> {
    const data = Uint8Array.from(atob(encryptedData), c => c.charCodeAt(0));
    
    const iv = data.slice(0, 12);
    const encrypted = data.slice(12);
    
    const decrypted = await crypto.subtle.decrypt(
      {
        name: 'AES-GCM',
        iv: iv
      },
      key,
      encrypted
    );
    
    const decoder = new TextDecoder();
    return decoder.decode(decrypted);
  }
  
  // Simple base64 encryption for less sensitive data
  encryptSimple(data: string): string {
    return btoa(unescape(encodeURIComponent(data)));
  }
  
  decryptSimple(encryptedData: string): string {
    try {
      return decodeURIComponent(escape(atob(encryptedData)));
    } catch {
      return '';
    }
  }
}

// Secure Storage Manager
export class SecureStorageManager {
  private static instance: SecureStorageManager;
  private encryption: ClientEncryption;
  
  static getInstance(): SecureStorageManager {
    if (!SecureStorageManager.instance) {
      SecureStorageManager.instance = new SecureStorageManager();
    }
    return SecureStorageManager.instance;
  }
  
  constructor() {
    this.encryption = ClientEncryption.getInstance();
  }
  
  setItem(key: string, value: any, encrypt: boolean = true): void {
    try {
      const serialized = JSON.stringify(value);
      const processed = encrypt ? this.encryption.encryptSimple(serialized) : serialized;
      localStorage.setItem(key, processed);
    } catch (error) {
      console.error('Failed to store data securely:', error);
    }
  }
  
  getItem<T>(key: string, decrypt: boolean = true): T | null {
    try {
      const stored = localStorage.getItem(key);
      if (!stored) return null;
      
      const processed = decrypt ? this.encryption.decryptSimple(stored) : stored;
      return JSON.parse(processed);
    } catch (error) {
      console.error('Failed to retrieve data securely:', error);
      return null;
    }
  }
  
  removeItem(key: string): void {
    localStorage.removeItem(key);
  }
  
  clear(): void {
    localStorage.clear();
  }
  
  // Secure session storage
  setSessionItem(key: string, value: any, encrypt: boolean = true): void {
    try {
      const serialized = JSON.stringify(value);
      const processed = encrypt ? this.encryption.encryptSimple(serialized) : serialized;
      sessionStorage.setItem(key, processed);
    } catch (error) {
      console.error('Failed to store session data securely:', error);
    }
  }
  
  getSessionItem<T>(key: string, decrypt: boolean = true): T | null {
    try {
      const stored = sessionStorage.getItem(key);
      if (!stored) return null;
      
      const processed = decrypt ? this.encryption.decryptSimple(stored) : stored;
      return JSON.parse(processed);
    } catch (error) {
      console.error('Failed to retrieve session data securely:', error);
      return null;
    }
  }
  
  removeSessionItem(key: string): void {
    sessionStorage.removeItem(key);
  }
  
  clearSession(): void {
    sessionStorage.clear();
  }
}

// API Key Management
export class APIKeyManager {
  private static instance: APIKeyManager;
  
  static getInstance(): APIKeyManager {
    if (!APIKeyManager.instance) {
      APIKeyManager.instance = new APIKeyManager();
    }
    return APIKeyManager.instance;
  }
  
  setAPIKey(service: string, key: string): void {
    const secureStorage = SecureStorageManager.getInstance();
    secureStorage.setItem(`api_key_${service}`, key);
  }
  
  getAPIKey(service: string): string | null {
    const secureStorage = SecureStorageManager.getInstance();
    return secureStorage.getItem<string>(`api_key_${service}`);
  }
  
  removeAPIKey(service: string): void {
    const secureStorage = SecureStorageManager.getInstance();
    secureStorage.removeItem(`api_key_${service}`);
  }
  
  clearAllAPIKeys(): void {
    const secureStorage = SecureStorageManager.getInstance();
    const keys = Object.keys(localStorage);
    keys.forEach(key => {
      if (key.startsWith('api_key_')) {
        secureStorage.removeItem(key);
      }
    });
  }
}

// JWT Token Management
export class JWTManager {
  private static instance: JWTManager;
  
  static getInstance(): JWTManager {
    if (!JWTManager.instance) {
      JWTManager.instance = new JWTManager();
    }
    return JWTManager.instance;
  }
  
  setToken(token: string, refreshToken?: string): void {
    const secureStorage = SecureStorageManager.getInstance();
    secureStorage.setItem('jwt_token', token);
    
    if (refreshToken) {
      secureStorage.setItem('refresh_token', refreshToken);
    }
  }
  
  getToken(): string | null {
    const secureStorage = SecureStorageManager.getInstance();
    return secureStorage.getItem<string>('jwt_token');
  }
  
  getRefreshToken(): string | null {
    const secureStorage = SecureStorageManager.getInstance();
    return secureStorage.getItem<string>('refresh_token');
  }
  
  clearTokens(): void {
    const secureStorage = SecureStorageManager.getInstance();
    secureStorage.removeItem('jwt_token');
    secureStorage.removeItem('refresh_token');
  }
  
  isTokenExpired(token: string): boolean {
    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      return Date.now() >= payload.exp * 1000;
    } catch {
      return true;
    }
  }
  
  getTokenPayload(token: string): any {
    try {
      return JSON.parse(atob(token.split('.')[1]));
    } catch {
      return null;
    }
  }
}

// Secrets Validation
export const validateSecrets = (): {
  isValid: boolean;
  issues: string[];
} => {
  const issues: string[] = [];
  
  // Check for hardcoded secrets in code
  const code = document.documentElement.outerHTML;
  const secretPatterns = [
    /password\s*=\s*["'][^"']+["']/gi,
    /secret\s*=\s*["'][^"']+["']/gi,
    /key\s*=\s*["'][^"']+["']/gi,
    /token\s*=\s*["'][^"']+["']/gi
  ];
  
  for (const pattern of secretPatterns) {
    if (pattern.test(code)) {
      issues.push('Potential hardcoded secret detected in code');
      break;
    }
  }
  
  // Check for insecure storage
  if (localStorage.getItem('password') || localStorage.getItem('secret')) {
    issues.push('Sensitive data stored in localStorage');
  }
  
  // Check for missing HTTPS in production
  if (process.env.NODE_ENV === 'production' && !window.location.protocol.includes('https')) {
    issues.push('HTTPS not enforced in production');
  }
  
  return {
    isValid: issues.length === 0,
    issues
  };
};

// Export singleton instances
export const secureStorageManager = SecureStorageManager.getInstance();
export const apiKeyManager = APIKeyManager.getInstance();
export const jwtManager = JWTManager.getInstance();
export const clientEncryption = ClientEncryption.getInstance();
