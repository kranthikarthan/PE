import axios, { AxiosInstance, AxiosRequestConfig, AxiosResponse } from 'axios';
import {
  Transaction,
  Account,
  PaymentType,
  CreateTransactionRequest,
  TransactionSearchCriteria,
  AccountSearchCriteria,
  PagedResponse,
  ApiResponse,
  DashboardStats,
  TransactionStats,
  Customer
} from '../types';

// API Configuration
const API_BASE_URL = process.env.REACT_APP_API_BASE_URL || 'http://localhost:8080';
const API_TIMEOUT = 30000; // 30 seconds

class ApiService {
  private api: AxiosInstance;

  constructor() {
    this.api = axios.create({
      baseURL: API_BASE_URL,
      timeout: API_TIMEOUT,
      headers: {
        'Content-Type': 'application/json',
      },
    });

    this.setupInterceptors();
  }

  private setupInterceptors(): void {
    // Request interceptor for auth token
    this.api.interceptors.request.use(
      (config) => {
        const token = localStorage.getItem('authToken');
        if (token) {
          config.headers.Authorization = `Bearer ${token}`;
        }
        
        // Add correlation ID for tracking
        config.headers['X-Correlation-ID'] = this.generateCorrelationId();
        
        return config;
      },
      (error) => {
        return Promise.reject(error);
      }
    );

    // Response interceptor for error handling
    this.api.interceptors.response.use(
      (response) => response,
      async (error) => {
        const originalRequest = error.config;

        // Handle 401 Unauthorized
        if (error.response?.status === 401 && !originalRequest._retry) {
          originalRequest._retry = true;
          
          try {
            const refreshToken = localStorage.getItem('refreshToken');
            if (refreshToken) {
              const response = await this.refreshAuthToken(refreshToken);
              localStorage.setItem('authToken', response.data.token);
              
              // Retry original request
              originalRequest.headers.Authorization = `Bearer ${response.data.token}`;
              return this.api(originalRequest);
            }
          } catch (refreshError) {
            // Refresh failed, redirect to login
            localStorage.removeItem('authToken');
            localStorage.removeItem('refreshToken');
            window.location.href = '/login';
            return Promise.reject(refreshError);
          }
        }

        return Promise.reject(this.handleApiError(error));
      }
    );
  }

  private generateCorrelationId(): string {
    return `${Date.now()}-${Math.random().toString(36).substr(2, 9)}`;
  }

  private handleApiError(error: any): any {
    if (error.response) {
      // Server responded with error status
      return {
        message: error.response.data?.message || 'An error occurred',
        status: error.response.status,
        code: error.response.data?.code,
        details: error.response.data?.details,
      };
    } else if (error.request) {
      // Request made but no response received
      return {
        message: 'Network error - please check your connection',
        status: 0,
        code: 'NETWORK_ERROR',
      };
    } else {
      // Something else happened
      return {
        message: error.message || 'An unexpected error occurred',
        status: 0,
        code: 'UNKNOWN_ERROR',
      };
    }
  }

  private async refreshAuthToken(refreshToken: string): Promise<AxiosResponse> {
    return this.api.post('/api/v1/auth/refresh', { refreshToken });
  }

  // Authentication APIs
  async login(username: string, password: string): Promise<ApiResponse<any>> {
    const response = await this.api.post('/api/v1/auth/login', {
      username,
      password,
    });
    return response.data;
  }

  async logout(): Promise<void> {
    await this.api.post('/api/v1/auth/logout');
    localStorage.removeItem('authToken');
    localStorage.removeItem('refreshToken');
  }

  // Transaction APIs
  async createTransaction(request: CreateTransactionRequest): Promise<ApiResponse<Transaction>> {
    const response = await this.api.post('/api/v1/transactions', request);
    return response.data;
  }

  async getTransaction(transactionId: string): Promise<ApiResponse<Transaction>> {
    const response = await this.api.get(`/api/v1/transactions/${transactionId}`);
    return response.data;
  }

  async getTransactionByReference(reference: string): Promise<ApiResponse<Transaction>> {
    const response = await this.api.get(`/api/v1/transactions/reference/${reference}`);
    return response.data;
  }

  async getTransactionStatus(transactionId: string): Promise<ApiResponse<any>> {
    const response = await this.api.get(`/api/v1/transactions/${transactionId}/status`);
    return response.data;
  }

  async searchTransactions(criteria: TransactionSearchCriteria): Promise<PagedResponse<Transaction>> {
    const params = new URLSearchParams();
    
    Object.entries(criteria).forEach(([key, value]) => {
      if (value !== undefined && value !== null && value !== '') {
        params.append(key, value.toString());
      }
    });

    const response = await this.api.get(`/api/v1/transactions/search?${params.toString()}`);
    return response.data;
  }

  async getTransactionsByAccount(
    accountId: string,
    page: number = 0,
    size: number = 20
  ): Promise<PagedResponse<Transaction>> {
    const response = await this.api.get(
      `/api/v1/transactions/account/${accountId}?page=${page}&size=${size}`
    );
    return response.data;
  }

  async cancelTransaction(transactionId: string, reason: string): Promise<ApiResponse<Transaction>> {
    const response = await this.api.post(`/api/v1/transactions/${transactionId}/cancel`, {
      reason,
    });
    return response.data;
  }

  async getTransactionStatistics(
    startDate: string,
    endDate: string
  ): Promise<ApiResponse<TransactionStats>> {
    const response = await this.api.get(
      `/api/v1/transactions/statistics?startDate=${startDate}&endDate=${endDate}`
    );
    return response.data;
  }

  // Account APIs
  async getAccount(accountId: string): Promise<ApiResponse<Account>> {
    const response = await this.api.get(`/api/v1/accounts/${accountId}`);
    return response.data;
  }

  async getAccountByNumber(accountNumber: string): Promise<ApiResponse<Account>> {
    const response = await this.api.get(`/api/v1/accounts/number/${accountNumber}`);
    return response.data;
  }

  async getAccountBalance(accountId: string): Promise<ApiResponse<any>> {
    const response = await this.api.get(`/api/v1/accounts/${accountId}/balance`);
    return response.data;
  }

  async searchAccounts(criteria: AccountSearchCriteria): Promise<PagedResponse<Account>> {
    const params = new URLSearchParams();
    
    Object.entries(criteria).forEach(([key, value]) => {
      if (value !== undefined && value !== null && value !== '') {
        params.append(key, value.toString());
      }
    });

    const response = await this.api.get(`/api/v1/accounts/search?${params.toString()}`);
    return response.data;
  }

  async getAccountsByCustomer(customerId: string): Promise<ApiResponse<Account[]>> {
    const response = await this.api.get(`/api/v1/accounts/customer/${customerId}`);
    return response.data;
  }

  // Payment Type APIs
  async getPaymentTypes(): Promise<ApiResponse<PaymentType[]>> {
    const response = await this.api.get('/api/v1/payment-types');
    return response.data;
  }

  async getActivePaymentTypes(): Promise<ApiResponse<PaymentType[]>> {
    const response = await this.api.get('/api/v1/payment-types?active=true');
    return response.data;
  }

  async getPaymentType(paymentTypeId: string): Promise<ApiResponse<PaymentType>> {
    const response = await this.api.get(`/api/v1/payment-types/${paymentTypeId}`);
    return response.data;
  }

  // Customer APIs
  async getCustomer(customerId: string): Promise<ApiResponse<Customer>> {
    const response = await this.api.get(`/api/v1/customers/${customerId}`);
    return response.data;
  }

  async searchCustomers(criteria: any): Promise<PagedResponse<Customer>> {
    const params = new URLSearchParams();
    
    Object.entries(criteria).forEach(([key, value]) => {
      if (value !== undefined && value !== null && value !== '') {
        params.append(key, value.toString());
      }
    });

    const response = await this.api.get(`/api/v1/customers/search?${params.toString()}`);
    return response.data;
  }

  // Dashboard APIs
  async getDashboardStats(): Promise<ApiResponse<DashboardStats>> {
    const response = await this.api.get('/api/v1/dashboard/stats');
    return response.data;
  }

  async getTransactionVolumeChart(
    startDate: string,
    endDate: string,
    granularity: 'hour' | 'day' | 'month' = 'day'
  ): Promise<ApiResponse<any[]>> {
    const response = await this.api.get(
      `/api/v1/dashboard/transaction-volume?startDate=${startDate}&endDate=${endDate}&granularity=${granularity}`
    );
    return response.data;
  }

  async getTransactionStatusChart(): Promise<ApiResponse<any[]>> {
    const response = await this.api.get('/api/v1/dashboard/transaction-status');
    return response.data;
  }

  async getPaymentTypeChart(): Promise<ApiResponse<any[]>> {
    const response = await this.api.get('/api/v1/dashboard/payment-types');
    return response.data;
  }

  // Health and Monitoring APIs
  async getHealthCheck(): Promise<ApiResponse<any>> {
    const response = await this.api.get('/api/v1/health');
    return response.data;
  }

  async getMetrics(): Promise<ApiResponse<any>> {
    const response = await this.api.get('/api/v1/metrics');
    return response.data;
  }

  // Utility methods
  async uploadFile(file: File, endpoint: string): Promise<ApiResponse<any>> {
    const formData = new FormData();
    formData.append('file', file);

    const response = await this.api.post(endpoint, formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });
    return response.data;
  }

  async downloadFile(endpoint: string, filename: string): Promise<void> {
    const response = await this.api.get(endpoint, {
      responseType: 'blob',
    });

    const url = window.URL.createObjectURL(new Blob([response.data]));
    const link = document.createElement('a');
    link.href = url;
    link.setAttribute('download', filename);
    document.body.appendChild(link);
    link.click();
    link.remove();
    window.URL.revokeObjectURL(url);
  }

  // Generic request method for custom endpoints
  async request<T>(config: AxiosRequestConfig): Promise<T> {
    const response = await this.api.request(config);
    return response.data;
  }
}

// Create and export singleton instance
const apiService = new ApiService();
export default apiService;