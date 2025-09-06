import axios, { AxiosInstance, AxiosRequestConfig, AxiosResponse } from 'axios';
import { store } from '../store';

// API Client with tenant support
class ApiClient {
  private client: AxiosInstance;
  private currentTenant: string = 'default';

  constructor() {
    this.client = axios.create({
      baseURL: process.env.REACT_APP_API_URL || 'http://localhost:8080',
      timeout: 30000,
      headers: {
        'Content-Type': 'application/json',
      },
    });

    this.setupInterceptors();
  }

  private setupInterceptors() {
    // Request interceptor - Add auth and tenant headers
    this.client.interceptors.request.use(
      (config) => {
        // Add authorization header
        const state = store.getState();
        const token = state.auth.token;
        
        if (token) {
          config.headers.Authorization = `Bearer ${token}`;
        }

        // Add tenant header
        const tenantId = this.getCurrentTenant();
        if (tenantId) {
          config.headers['X-Tenant-ID'] = tenantId;
        }

        // Add user ID header if available
        const userId = state.auth.user?.id;
        if (userId) {
          config.headers['X-User-ID'] = userId;
        }

        // Add request ID for tracing
        config.headers['X-Request-ID'] = this.generateRequestId();

        console.log(`[API] ${config.method?.toUpperCase()} ${config.url} [Tenant: ${tenantId}]`);
        return config;
      },
      (error) => {
        console.error('[API] Request error:', error);
        return Promise.reject(error);
      }
    );

    // Response interceptor - Handle tenant-specific responses
    this.client.interceptors.response.use(
      (response) => {
        // Log tenant-aware responses
        const tenantId = response.headers['x-tenant-id'] || this.currentTenant;
        console.log(`[API] Response from ${response.config.url} [Tenant: ${tenantId}]`, response.status);
        return response;
      },
      (error) => {
        console.error('[API] Response error:', error);
        
        // Handle tenant-specific errors
        if (error.response?.status === 403 && error.response?.data?.error === 'TENANT_ACCESS_DENIED') {
          console.error('[API] Tenant access denied:', this.currentTenant);
          // Could dispatch tenant error action here
        }

        // Handle token expiration
        if (error.response?.status === 401) {
          console.warn('[API] Authentication error - token may be expired');
          // Could dispatch logout action here
        }

        return Promise.reject(error);
      }
    );
  }

  // Tenant management
  setTenant(tenantId: string) {
    this.currentTenant = tenantId;
    console.log(`[API] Switched to tenant: ${tenantId}`);
  }

  getCurrentTenant(): string {
    // Try to get tenant from Redux store first
    const state = store.getState();
    const storeTenant = (state as any).tenant?.currentTenant;
    
    if (storeTenant) {
      return storeTenant;
    }

    // Fall back to instance variable
    return this.currentTenant;
  }

  // Utility methods
  private generateRequestId(): string {
    return `req_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`;
  }

  // HTTP Methods with tenant support
  async get<T = any>(url: string, config?: AxiosRequestConfig): Promise<AxiosResponse<T>> {
    return this.client.get<T>(url, config);
  }

  async post<T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<AxiosResponse<T>> {
    return this.client.post<T>(url, data, config);
  }

  async put<T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<AxiosResponse<T>> {
    return this.client.put<T>(url, data, config);
  }

  async delete<T = any>(url: string, config?: AxiosRequestConfig): Promise<AxiosResponse<T>> {
    return this.client.delete<T>(url, config);
  }

  async patch<T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<AxiosResponse<T>> {
    return this.client.patch<T>(url, data, config);
  }

  // Tenant-specific API methods
  async getTenantConfig(tenantId?: string): Promise<any> {
    const tenant = tenantId || this.getCurrentTenant();
    return this.get(`/api/v1/config/tenants/${tenant}/config`);
  }

  async setTenantConfig(key: string, value: string, tenantId?: string): Promise<any> {
    const tenant = tenantId || this.getCurrentTenant();
    return this.post(`/api/v1/config/tenants/${tenant}/config`, {
      configKey: key,
      configValue: value,
      environment: 'production'
    });
  }

  async getTenantFeatureFlags(tenantId?: string): Promise<any> {
    const tenant = tenantId || this.getCurrentTenant();
    return this.get(`/api/v1/config/tenants/${tenant}/features`);
  }

  async setTenantFeatureFlag(featureName: string, enabled: boolean, config: any = {}, tenantId?: string): Promise<any> {
    const tenant = tenantId || this.getCurrentTenant();
    return this.post(`/api/v1/config/tenants/${tenant}/features/${featureName}`, {
      enabled,
      config
    });
  }

  async checkFeatureFlag(featureName: string, tenantId?: string): Promise<boolean> {
    try {
      const tenant = tenantId || this.getCurrentTenant();
      const response = await this.get(`/api/v1/config/tenants/${tenant}/features/${featureName}`);
      return response.data.enabled;
    } catch (error) {
      console.warn(`[API] Feature flag check failed for ${featureName}:`, error);
      return false;
    }
  }

  // Multi-tenant transaction methods
  async createTransaction(transactionData: any, tenantId?: string): Promise<any> {
    const tenant = tenantId || this.getCurrentTenant();
    return this.post('/api/v1/transactions', {
      ...transactionData,
      tenantId: tenant
    });
  }

  async getTransactions(filters: any = {}, tenantId?: string): Promise<any> {
    const tenant = tenantId || this.getCurrentTenant();
    return this.get('/api/v1/transactions', {
      params: {
        ...filters,
        tenantId: tenant
      }
    });
  }

  // Multi-tenant ISO 20022 methods
  async processIso20022Message(messageType: string, messageData: any, tenantId?: string): Promise<any> {
    const tenant = tenantId || this.getCurrentTenant();
    return this.post(`/api/v1/iso20022/${messageType}`, {
      ...messageData,
      tenantId: tenant
    });
  }

  // Tenant management methods
  async createTenant(tenantData: any): Promise<any> {
    return this.post('/api/v1/config/tenants', tenantData);
  }

  async getTenant(tenantId: string): Promise<any> {
    return this.get(`/api/v1/config/tenants/${tenantId}`);
  }

  async listTenants(): Promise<any> {
    return this.get('/api/v1/config/tenants');
  }

  // Payment type management
  async addPaymentType(paymentTypeData: any, tenantId?: string): Promise<any> {
    const tenant = tenantId || this.getCurrentTenant();
    return this.post(`/api/v1/config/tenants/${tenant}/payment-types`, paymentTypeData);
  }

  async updatePaymentType(paymentTypeCode: string, updates: any, tenantId?: string): Promise<any> {
    const tenant = tenantId || this.getCurrentTenant();
    return this.put(`/api/v1/config/tenants/${tenant}/payment-types/${paymentTypeCode}`, updates);
  }

  async getPaymentTypes(tenantId?: string): Promise<any> {
    const tenant = tenantId || this.getCurrentTenant();
    return this.get('/api/v1/payment-types', {
      params: { tenantId: tenant }
    });
  }

  // Rate limit management
  async updateRateLimit(endpoint: string, rateLimitConfig: any, tenantId?: string): Promise<any> {
    const tenant = tenantId || this.getCurrentTenant();
    return this.put(`/api/v1/config/tenants/${tenant}/rate-limits`, rateLimitConfig, {
      params: { endpoint }
    });
  }

  async getRateLimit(endpoint: string, tenantId?: string): Promise<any> {
    const tenant = tenantId || this.getCurrentTenant();
    return this.get(`/api/v1/config/tenants/${tenant}/rate-limits`, {
      params: { endpoint }
    });
  }
}

// Export singleton instance
export const apiClient = new ApiClient();
export default apiClient;