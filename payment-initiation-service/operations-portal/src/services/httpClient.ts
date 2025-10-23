/**
 * HTTP Client Configuration
 * 
 * Centralized Axios instance with interceptors for authentication,
 * error handling, retry logic, and request/response transformation.
 */

import axios, { AxiosInstance, AxiosRequestConfig, AxiosResponse, AxiosError } from 'axios';
import { config } from '@config/environment';
import { ApiResponse, ErrorResponse, TenantContext } from '@types/api';

/**
 * HTTP Client class with retry logic and interceptors
 */
export class HttpClient {
  private instance: AxiosInstance;
  private retryAttempts: number;
  private retryDelay: number;

  constructor(baseURL: string, timeout: number = config.apiTimeout) {
    this.retryAttempts = config.apiRetryAttempts;
    this.retryDelay = config.apiRetryDelay;

    this.instance = axios.create({
      baseURL,
      timeout,
      headers: {
        'Content-Type': 'application/json',
      },
    });

    this.setupInterceptors();
  }

  /**
   * Setup request and response interceptors
   */
  private setupInterceptors(): void {
    // Request interceptor
    this.instance.interceptors.request.use(
      (config) => {
        // Add authentication token
        const token = this.getAuthToken();
        if (token) {
          config.headers.Authorization = `Bearer ${token}`;
        }

        // Add tenant context
        const tenantContext = this.getTenantContext();
        if (tenantContext) {
          config.headers['X-Tenant-ID'] = tenantContext.tenantId;
          config.headers['X-Business-Unit-ID'] = tenantContext.businessUnitId;
          config.headers['X-User-ID'] = tenantContext.userId;
          config.headers['X-Correlation-ID'] = tenantContext.correlationId;
        }

        // Add request timestamp
        config.metadata = { startTime: Date.now() };

        if (config.enableDebugLogging) {
          console.log(`[HTTP Request] ${config.method?.toUpperCase()} ${config.url}`, {
            headers: config.headers,
            data: config.data,
          });
        }

        return config;
      },
      (error) => {
        console.error('[HTTP Request Error]', error);
        return Promise.reject(error);
      }
    );

    // Response interceptor
    this.instance.interceptors.response.use(
      (response: AxiosResponse) => {
        const duration = Date.now() - (response.config.metadata?.startTime || 0);
        
        if (response.config.enableDebugLogging) {
          console.log(`[HTTP Response] ${response.config.method?.toUpperCase()} ${response.config.url}`, {
            status: response.status,
            duration: `${duration}ms`,
            data: response.data,
          });
        }

        return response;
      },
      async (error: AxiosError) => {
        const originalRequest = error.config as AxiosRequestConfig & { _retry?: boolean };

        // Handle 401 Unauthorized - try to refresh token
        if (error.response?.status === 401 && !originalRequest._retry) {
          originalRequest._retry = true;
          
          try {
            await this.refreshAuthToken();
            return this.instance(originalRequest);
          } catch (refreshError) {
            this.handleAuthError();
            return Promise.reject(refreshError);
          }
        }

        // Handle retry logic for network errors
        if (this.shouldRetry(error) && !originalRequest._retry) {
          originalRequest._retry = true;
          return this.retryRequest(originalRequest);
        }

        // Transform error response
        const transformedError = this.transformError(error);
        console.error('[HTTP Response Error]', transformedError);
        
        return Promise.reject(transformedError);
      }
    );
  }

  /**
   * Get authentication token from storage
   */
  private getAuthToken(): string | null {
    try {
      return localStorage.getItem(config.authTokenKey);
    } catch {
      return null;
    }
  }

  /**
   * Get tenant context from storage or state
   */
  private getTenantContext(): TenantContext | null {
    try {
      const tenantId = localStorage.getItem('tenantId') || config.defaultTenantId;
      const businessUnitId = localStorage.getItem('businessUnitId') || config.defaultBusinessUnitId;
      const userId = localStorage.getItem('userId') || 'system';
      const correlationId = this.generateCorrelationId();

      return {
        tenantId,
        businessUnitId,
        userId,
        correlationId,
      };
    } catch {
      return null;
    }
  }

  /**
   * Generate correlation ID for request tracking
   */
  private generateCorrelationId(): string {
    return `req_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`;
  }

  /**
   * Refresh authentication token
   */
  private async refreshAuthToken(): Promise<void> {
    try {
      const refreshToken = localStorage.getItem(config.authRefreshTokenKey);
      if (!refreshToken) {
        throw new Error('No refresh token available');
      }

      const response = await axios.post(`${config.authApiUrl}/auth/refresh`, {
        refreshToken,
      });

      const { accessToken, refreshToken: newRefreshToken } = response.data;
      localStorage.setItem(config.authTokenKey, accessToken);
      localStorage.setItem(config.authRefreshTokenKey, newRefreshToken);
    } catch (error) {
      console.error('[Token Refresh Error]', error);
      throw error;
    }
  }

  /**
   * Handle authentication error (logout user)
   */
  private handleAuthError(): void {
    localStorage.removeItem(config.authTokenKey);
    localStorage.removeItem(config.authRefreshTokenKey);
    window.location.href = '/login';
  }

  /**
   * Determine if request should be retried
   */
  private shouldRetry(error: AxiosError): boolean {
    if (!error.config) return false;
    
    const retryableStatuses = [408, 429, 500, 502, 503, 504];
    const isNetworkError = !error.response;
    const isRetryableStatus = error.response?.status && retryableStatuses.includes(error.response.status);
    
    return isNetworkError || isRetryableStatus;
  }

  /**
   * Retry request with exponential backoff
   */
  private async retryRequest(config: AxiosRequestConfig): Promise<AxiosResponse> {
    const delay = this.retryDelay * Math.pow(2, (config._retryCount || 0));
    config._retryCount = (config._retryCount || 0) + 1;

    await new Promise(resolve => setTimeout(resolve, delay));
    return this.instance(config);
  }

  /**
   * Transform error response to standardized format
   */
  private transformError(error: AxiosError): ErrorResponse {
    const response = error.response;
    
    if (response?.data) {
      return {
        error: response.data.error || 'Request failed',
        message: response.data.message || error.message,
        status: response.status,
        timestamp: new Date().toISOString(),
        path: error.config?.url || '',
        correlationId: response.headers['x-correlation-id'],
        details: response.data.details,
      };
    }

    return {
      error: 'Network Error',
      message: error.message || 'Network request failed',
      status: 0,
      timestamp: new Date().toISOString(),
      path: error.config?.url || '',
    };
  }

  /**
   * GET request
   */
  async get<T = any>(url: string, config?: AxiosRequestConfig): Promise<ApiResponse<T>> {
    const response = await this.instance.get<ApiResponse<T>>(url, config);
    return response.data;
  }

  /**
   * POST request
   */
  async post<T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<ApiResponse<T>> {
    const response = await this.instance.post<ApiResponse<T>>(url, data, config);
    return response.data;
  }

  /**
   * PUT request
   */
  async put<T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<ApiResponse<T>> {
    const response = await this.instance.put<ApiResponse<T>>(url, data, config);
    return response.data;
  }

  /**
   * PATCH request
   */
  async patch<T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<ApiResponse<T>> {
    const response = await this.instance.patch<ApiResponse<T>>(url, data, config);
    return response.data;
  }

  /**
   * DELETE request
   */
  async delete<T = any>(url: string, config?: AxiosRequestConfig): Promise<ApiResponse<T>> {
    const response = await this.instance.delete<ApiResponse<T>>(url, config);
    return response.data;
  }

  /**
   * Get the underlying Axios instance for advanced usage
   */
  getInstance(): AxiosInstance {
    return this.instance;
  }
}

/**
 * Create HTTP client instances for different services
 */
export const createHttpClient = (baseURL: string, timeout?: number): HttpClient => {
  return new HttpClient(baseURL, timeout);
};

/**
 * Default HTTP client for the main API
 */
export const httpClient = createHttpClient(config.apiBaseUrl);

/**
 * Operations management HTTP client
 */
export const operationsClient = createHttpClient(config.operationsApiUrl);

/**
 * Authentication HTTP client
 */
export const authClient = createHttpClient(config.authApiUrl);

export default httpClient;
