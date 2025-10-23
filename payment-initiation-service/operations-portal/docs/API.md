# API Documentation

## Overview

The React Operations Portal integrates with 22 microservices to provide comprehensive operations management capabilities. This document outlines the API integration patterns, service clients, and data models used throughout the application.

## 🔌 API Architecture

### Service Integration Pattern

```
┌─────────────────────────────────────────────────────────────┐
│                    React Operations Portal                 │
├─────────────────────────────────────────────────────────────┤
│  HTTP Client (Axios)                                       │
│  ├── Request Interceptors                                  │
│  │   ├── Auth Token Injection                             │
│  │   ├── Correlation ID                                   │
│  │   ├── Tenant Context                                   │
│  │   └── Request Logging                                  │
│  └── Response Interceptors                                 │
│      ├── Error Handling                                   │
│      ├── Token Refresh                                     │
│      └── Response Logging                                  │
├─────────────────────────────────────────────────────────────┤
│  Service Clients (22 Services)                            │
│  ├── PaymentInitiationService                             │
│  ├── OperationsManagementService                          │
│  ├── TransactionProcessingService                          │
│  ├── ReconciliationService                                │
│  ├── AuthService                                          │
│  └── ... (17 more services)                               │
└─────────────────────────────────────────────────────────────┘
```

## 🏗️ Service Clients

### Base API Client

```typescript
// src/services/baseApiClient.ts
export abstract class BaseApiClient {
  protected baseURL: string;
  protected httpClient: AxiosInstance;

  constructor(baseURL: string) {
    this.baseURL = baseURL;
    this.httpClient = createHttpClient(baseURL);
  }

  protected async get<T>(endpoint: string, params?: any): Promise<T> {
    const response = await this.httpClient.get(endpoint, { params });
    return response.data;
  }

  protected async post<T>(endpoint: string, data?: any): Promise<T> {
    const response = await this.httpClient.post(endpoint, data);
    return response.data;
  }

  protected async put<T>(endpoint: string, data?: any): Promise<T> {
    const response = await this.httpClient.put(endpoint, data);
    return response.data;
  }

  protected async delete<T>(endpoint: string): Promise<T> {
    const response = await this.httpClient.delete(endpoint);
    return response.data;
  }
}
```

### HTTP Client Configuration

```typescript
// src/services/httpClient.ts
export const createHttpClient = (baseURL: string): AxiosInstance => {
  const client = axios.create({
    baseURL,
    timeout: 30000,
    headers: {
      'Content-Type': 'application/json',
    },
  });

  // Request interceptor
  client.interceptors.request.use(
    (config) => {
      // Add auth token
      const token = getAuthToken();
      if (token) {
        config.headers.Authorization = `Bearer ${token}`;
      }

      // Add correlation ID
      config.headers['X-Correlation-ID'] = generateCorrelationId();

      // Add tenant context
      const tenantId = getTenantId();
      if (tenantId) {
        config.headers['X-Tenant-ID'] = tenantId;
      }

      return config;
    },
    (error) => Promise.reject(error)
  );

  // Response interceptor
  client.interceptors.response.use(
    (response) => response,
    async (error) => {
      if (error.response?.status === 401) {
        // Handle token refresh
        await handleTokenRefresh();
        return client.request(error.config);
      }
      return Promise.reject(error);
    }
  );

  return client;
};
```

## 🔐 Authentication Service

### AuthService

```typescript
// src/services/authService.ts
export class AuthService extends BaseApiClient {
  constructor() {
    super(process.env.REACT_APP_AUTH_SERVICE_URL || 'http://localhost:8081');
  }

  async login(credentials: LoginCredentials): Promise<AuthResponse> {
    return this.post<AuthResponse>('/api/auth/login', credentials);
  }

  async logout(): Promise<void> {
    return this.post<void>('/api/auth/logout');
  }

  async refreshToken(): Promise<AuthResponse> {
    return this.post<AuthResponse>('/api/auth/refresh');
  }

  async getCurrentUser(): Promise<User> {
    return this.get<User>('/api/auth/me');
  }

  async updateProfile(profile: UserProfile): Promise<User> {
    return this.put<User>('/api/auth/profile', profile);
  }
}
```

### Authentication Types

```typescript
// src/types/auth.ts
export interface LoginCredentials {
  username: string;
  password: string;
  rememberMe?: boolean;
}

export interface AuthResponse {
  token: string;
  refreshToken: string;
  user: User;
  expiresIn: number;
}

export interface User {
  id: string;
  username: string;
  email: string;
  firstName: string;
  lastName: string;
  role: string;
  permissions: string[];
  tenantId: string;
  businessUnitId: string;
  lastLoginAt: string;
  isActive: boolean;
}

export interface UserProfile {
  firstName: string;
  lastName: string;
  email: string;
  phone?: string;
  preferences: UserPreferences;
}
```

## 🏥 Operations Management Service

### OperationsManagementService

```typescript
// src/services/operationsManagementService.ts
export class OperationsManagementService extends BaseApiClient {
  constructor() {
    super(process.env.REACT_APP_OPERATIONS_SERVICE_URL || 'http://localhost:8082');
  }

  // Service Health
  async getAllServicesHealth(): Promise<ServiceHealth[]> {
    return this.get<ServiceHealth[]>('/api/operations/services/health');
  }

  async getServiceHealthSummary(): Promise<ServiceHealthSummary> {
    return this.get<ServiceHealthSummary>('/api/operations/services/health/summary');
  }

  async getServiceAlerts(): Promise<ServiceAlert[]> {
    return this.get<ServiceAlert[]>('/api/operations/services/alerts');
  }

  // Circuit Breakers
  async getAllCircuitBreakers(): Promise<CircuitBreakerInfo[]> {
    return this.get<CircuitBreakerInfo[]>('/api/operations/circuit-breakers');
  }

  async openCircuitBreaker(serviceName: string): Promise<void> {
    return this.post<void>(`/api/operations/circuit-breakers/${serviceName}/open`);
  }

  async closeCircuitBreaker(serviceName: string): Promise<void> {
    return this.post<void>(`/api/operations/circuit-breakers/${serviceName}/close`);
  }

  // Feature Flags
  async getAllFeatureFlags(): Promise<FeatureFlag[]> {
    return this.get<FeatureFlag[]>('/api/operations/feature-flags');
  }

  async toggleFeatureFlag(flagName: string, enabled: boolean): Promise<void> {
    return this.put<void>(`/api/operations/feature-flags/${flagName}`, { enabled });
  }

  // Kubernetes Pods
  async getAllPods(): Promise<PodInfo[]> {
    return this.get<PodInfo[]>('/api/operations/pods');
  }

  async restartPod(podName: string): Promise<void> {
    return this.post<void>(`/api/operations/pods/${podName}/restart`);
  }

  // Service Control
  async controlService(serviceName: string, action: ServiceAction): Promise<void> {
    return this.post<void>(`/api/operations/services/${serviceName}/${action}`);
  }
}
```

### Operations Types

```typescript
// src/types/service.ts
export interface ServiceHealth {
  serviceName: string;
  status: 'UP' | 'DOWN' | 'DEGRADED';
  responseTime: number;
  lastChecked: string;
  dependencies: ServiceDependency[];
  metrics: ServiceMetrics;
}

export interface ServiceHealthSummary {
  totalServices: number;
  healthyServices: number;
  unhealthyServices: number;
  degradedServices: number;
  overallStatus: 'HEALTHY' | 'DEGRADED' | 'UNHEALTHY';
}

export interface ServiceAlert {
  id: string;
  serviceName: string;
  severity: 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';
  message: string;
  timestamp: string;
  acknowledged: boolean;
}

export interface CircuitBreakerInfo {
  serviceName: string;
  state: 'CLOSED' | 'OPEN' | 'HALF_OPEN';
  failureCount: number;
  failureThreshold: number;
  lastFailure: string;
}

export interface FeatureFlag {
  name: string;
  description: string;
  enabled: boolean;
  lastModified: string;
  modifiedBy: string;
}

export interface PodInfo {
  name: string;
  namespace: string;
  status: 'Running' | 'Pending' | 'Failed' | 'Succeeded';
  ready: boolean;
  restarts: number;
  age: string;
  node: string;
}

export type ServiceAction = 'start' | 'stop' | 'restart' | 'pause' | 'resume';
```

## 💳 Payment Initiation Service

### PaymentInitiationService

```typescript
// src/services/paymentInitiationService.ts
export class PaymentInitiationService extends BaseApiClient {
  constructor() {
    super(process.env.REACT_APP_PAYMENT_SERVICE_URL || 'http://localhost:8083');
  }

  // Payment Operations
  async createPayment(payment: CreatePaymentRequest): Promise<Payment> {
    return this.post<Payment>('/api/payments', payment);
  }

  async getPayment(paymentId: string): Promise<Payment> {
    return this.get<Payment>(`/api/payments/${paymentId}`);
  }

  async updatePayment(paymentId: string, payment: UpdatePaymentRequest): Promise<Payment> {
    return this.put<Payment>(`/api/payments/${paymentId}`, payment);
  }

  async deletePayment(paymentId: string): Promise<void> {
    return this.delete<void>(`/api/payments/${paymentId}`);
  }

  // Payment Repair
  async getFailedPayments(filters?: PaymentFilters): Promise<Payment[]> {
    return this.get<Payment[]>('/api/payments/failed', filters);
  }

  async retryPayment(paymentId: string): Promise<Payment> {
    return this.post<Payment>(`/api/payments/${paymentId}/retry`);
  }

  async cancelPayment(paymentId: string, reason: string): Promise<Payment> {
    return this.post<Payment>(`/api/payments/${paymentId}/cancel`, { reason });
  }

  async bulkRetryPayments(paymentIds: string[]): Promise<BulkOperationResult> {
    return this.post<BulkOperationResult>('/api/payments/bulk/retry', { paymentIds });
  }
}
```

### Payment Types

```typescript
// src/types/payment.ts
export interface Payment {
  id: string;
  amount: number;
  currency: string;
  status: PaymentStatus;
  method: PaymentMethod;
  recipient: PaymentRecipient;
  sender: PaymentSender;
  createdAt: string;
  updatedAt: string;
  processedAt?: string;
  failedAt?: string;
  failureReason?: string;
  retryCount: number;
  maxRetries: number;
}

export interface CreatePaymentRequest {
  amount: number;
  currency: string;
  method: PaymentMethod;
  recipient: PaymentRecipient;
  sender: PaymentSender;
  description?: string;
  metadata?: Record<string, any>;
}

export interface UpdatePaymentRequest {
  amount?: number;
  currency?: string;
  recipient?: PaymentRecipient;
  sender?: PaymentSender;
  description?: string;
  metadata?: Record<string, any>;
}

export interface PaymentFilters {
  status?: PaymentStatus[];
  method?: PaymentMethod[];
  currency?: string[];
  dateRange?: {
    start: string;
    end: string;
  };
  amountRange?: {
    min: number;
    max: number;
  };
}

export type PaymentStatus = 
  | 'PENDING' 
  | 'PROCESSING' 
  | 'COMPLETED' 
  | 'FAILED' 
  | 'CANCELLED' 
  | 'RETRYING';

export type PaymentMethod = 
  | 'BANK_TRANSFER' 
  | 'CARD' 
  | 'WALLET' 
  | 'CASH' 
  | 'CRYPTOCURRENCY';

export interface PaymentRecipient {
  id: string;
  name: string;
  accountNumber: string;
  bankCode: string;
  email?: string;
  phone?: string;
}

export interface PaymentSender {
  id: string;
  name: string;
  accountNumber: string;
  bankCode: string;
  email?: string;
  phone?: string;
}

export interface BulkOperationResult {
  totalProcessed: number;
  successful: number;
  failed: number;
  results: Array<{
    id: string;
    success: boolean;
    error?: string;
  }>;
}
```

## 📊 Transaction Processing Service

### TransactionProcessingService

```typescript
// src/services/transactionProcessingService.ts
export class TransactionProcessingService extends BaseApiClient {
  constructor() {
    super(process.env.REACT_APP_TRANSACTION_SERVICE_URL || 'http://localhost:8084');
  }

  // Transaction Queries
  async getTransactions(filters?: TransactionFilters): Promise<PagedResponse<Transaction>> {
    return this.get<PagedResponse<Transaction>>('/api/transactions', filters);
  }

  async getTransactionDetails(transactionId: string): Promise<TransactionDetails> {
    return this.get<TransactionDetails>(`/api/transactions/${transactionId}`);
  }

  async searchTransactions(query: TransactionSearchQuery): Promise<Transaction[]> {
    return this.post<Transaction[]>('/api/transactions/search', query);
  }

  // Transaction Export
  async exportTransactions(filters: TransactionFilters, format: ExportFormat): Promise<Blob> {
    const response = await this.httpClient.post('/api/transactions/export', {
      filters,
      format
    }, {
      responseType: 'blob'
    });
    return response.data;
  }

  // Transaction Analytics
  async getTransactionMetrics(dateRange: DateRange): Promise<TransactionMetrics> {
    return this.get<TransactionMetrics>('/api/transactions/metrics', { dateRange });
  }

  async getTransactionTrends(period: string): Promise<TransactionTrend[]> {
    return this.get<TransactionTrend[]>(`/api/transactions/trends/${period}`);
  }
}
```

### Transaction Types

```typescript
// src/types/transaction.ts
export interface Transaction {
  id: string;
  paymentId: string;
  amount: number;
  currency: string;
  status: TransactionStatus;
  type: TransactionType;
  direction: TransactionDirection;
  timestamp: string;
  description: string;
  reference: string;
  metadata: Record<string, any>;
}

export interface TransactionDetails extends Transaction {
  payment: Payment;
  processingSteps: ProcessingStep[];
  auditTrail: AuditEntry[];
  relatedTransactions: Transaction[];
}

export interface TransactionFilters {
  status?: TransactionStatus[];
  type?: TransactionType[];
  direction?: TransactionDirection[];
  dateRange?: DateRange;
  amountRange?: AmountRange;
  currency?: string[];
  reference?: string;
}

export interface TransactionSearchQuery {
  query: string;
  filters?: TransactionFilters;
  sortBy?: string;
  sortOrder?: 'asc' | 'desc';
  limit?: number;
  offset?: number;
}

export interface TransactionMetrics {
  totalTransactions: number;
  totalAmount: number;
  averageAmount: number;
  successRate: number;
  failureRate: number;
  processingTime: {
    average: number;
    median: number;
    p95: number;
    p99: number;
  };
}

export type TransactionStatus = 
  | 'PENDING' 
  | 'PROCESSING' 
  | 'COMPLETED' 
  | 'FAILED' 
  | 'CANCELLED';

export type TransactionType = 
  | 'PAYMENT' 
  | 'REFUND' 
  | 'CHARGEBACK' 
  | 'ADJUSTMENT';

export type TransactionDirection = 'INBOUND' | 'OUTBOUND';

export type ExportFormat = 'CSV' | 'EXCEL' | 'PDF';
```

## 🔄 Reconciliation Service

### ReconciliationService

```typescript
// src/services/reconciliationService.ts
export class ReconciliationService extends BaseApiClient {
  constructor() {
    super(process.env.REACT_APP_RECONCILIATION_SERVICE_URL || 'http://localhost:8085');
  }

  // Reconciliation Batches
  async getReconciliationBatches(filters?: ReconciliationFilters): Promise<ReconciliationBatch[]> {
    return this.get<ReconciliationBatch[]>('/api/reconciliation/batches', filters);
  }

  async getReconciliationBatch(batchId: string): Promise<ReconciliationBatch> {
    return this.get<ReconciliationBatch>(`/api/reconciliation/batches/${batchId}`);
  }

  async createReconciliationBatch(batch: CreateReconciliationBatchRequest): Promise<ReconciliationBatch> {
    return this.post<ReconciliationBatch>('/api/reconciliation/batches', batch);
  }

  async processReconciliationBatch(batchId: string): Promise<ReconciliationBatch> {
    return this.post<ReconciliationBatch>(`/api/reconciliation/batches/${batchId}/process`);
  }

  // Performance Metrics
  async getReconciliationMetrics(dateRange: DateRange): Promise<ReconciliationMetrics> {
    return this.get<ReconciliationMetrics>('/api/reconciliation/metrics', { dateRange });
  }

  async getReconciliationPerformance(): Promise<ReconciliationPerformance> {
    return this.get<ReconciliationPerformance>('/api/reconciliation/performance');
  }

  // Exception Handling
  async getReconciliationExceptions(filters?: ExceptionFilters): Promise<ReconciliationException[]> {
    return this.get<ReconciliationException[]>('/api/reconciliation/exceptions', filters);
  }

  async resolveReconciliationException(exceptionId: string, resolution: ExceptionResolution): Promise<void> {
    return this.post<void>(`/api/reconciliation/exceptions/${exceptionId}/resolve`, resolution);
  }
}
```

### Reconciliation Types

```typescript
// src/types/reconciliation.ts
export interface ReconciliationBatch {
  id: string;
  name: string;
  status: ReconciliationStatus;
  type: ReconciliationType;
  startDate: string;
  endDate: string;
  totalTransactions: number;
  matchedTransactions: number;
  unmatchedTransactions: number;
  exceptions: number;
  processingTime: number;
  createdAt: string;
  completedAt?: string;
}

export interface ReconciliationMetrics {
  totalBatches: number;
  successfulBatches: number;
  failedBatches: number;
  averageProcessingTime: number;
  totalTransactions: number;
  matchedTransactions: number;
  unmatchedTransactions: number;
  exceptions: number;
}

export interface ReconciliationPerformance {
  dailyPerformance: DailyPerformance[];
  weeklyPerformance: WeeklyPerformance[];
  monthlyPerformance: MonthlyPerformance[];
  trends: PerformanceTrend[];
}

export interface ReconciliationException {
  id: string;
  batchId: string;
  transactionId: string;
  type: ExceptionType;
  severity: ExceptionSeverity;
  description: string;
  createdAt: string;
  resolvedAt?: string;
  resolution?: string;
}

export type ReconciliationStatus = 
  | 'PENDING' 
  | 'PROCESSING' 
  | 'COMPLETED' 
  | 'FAILED' 
  | 'CANCELLED';

export type ReconciliationType = 
  | 'DAILY' 
  | 'WEEKLY' 
  | 'MONTHLY' 
  | 'AD_HOC';

export type ExceptionType = 
  | 'MISSING_TRANSACTION' 
  | 'AMOUNT_MISMATCH' 
  | 'DUPLICATE_TRANSACTION' 
  | 'INVALID_REFERENCE';

export type ExceptionSeverity = 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';
```

## 🔧 Custom Hooks

### useApi Hook

```typescript
// src/hooks/useApi.ts
export const useApi = <T>(
  apiCall: () => Promise<T>,
  dependencies: any[] = []
) => {
  const [data, setData] = useState<T | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<Error | null>(null);

  const execute = useCallback(async () => {
    setLoading(true);
    setError(null);
    
    try {
      const result = await apiCall();
      setData(result);
    } catch (err) {
      setError(err as Error);
    } finally {
      setLoading(false);
    }
  }, dependencies);

  useEffect(() => {
    execute();
  }, [execute]);

  return { data, loading, error, refetch: execute };
};
```

### usePaginatedApi Hook

```typescript
// src/hooks/useApi.ts
export const usePaginatedApi = <T>(
  apiCall: (page: number, size: number) => Promise<PagedResponse<T>>,
  initialPage = 1,
  initialSize = 10
) => {
  const [data, setData] = useState<T[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<Error | null>(null);
  const [pagination, setPagination] = useState({
    page: initialPage,
    size: initialSize,
    total: 0,
    totalPages: 0
  });

  const fetchData = useCallback(async (page: number, size: number) => {
    setLoading(true);
    setError(null);
    
    try {
      const result = await apiCall(page, size);
      setData(result.data);
      setPagination({
        page: result.page,
        size: result.size,
        total: result.total,
        totalPages: result.totalPages
      });
    } catch (err) {
      setError(err as Error);
    } finally {
      setLoading(false);
    }
  }, [apiCall]);

  const goToPage = useCallback((page: number) => {
    fetchData(page, pagination.size);
  }, [fetchData, pagination.size]);

  const changePageSize = useCallback((size: number) => {
    fetchData(1, size);
  }, [fetchData]);

  useEffect(() => {
    fetchData(initialPage, initialSize);
  }, [fetchData, initialPage, initialSize]);

  return {
    data,
    loading,
    error,
    pagination,
    goToPage,
    changePageSize,
    refetch: () => fetchData(pagination.page, pagination.size)
  };
};
```

## 📝 API Response Types

### Generic Response Types

```typescript
// src/types/api.ts
export interface ApiResponse<T> {
  data: T;
  message?: string;
  timestamp: string;
}

export interface PagedResponse<T> {
  data: T[];
  page: number;
  size: number;
  total: number;
  totalPages: number;
  hasNext: boolean;
  hasPrevious: boolean;
}

export interface ErrorResponse {
  error: string;
  message: string;
  timestamp: string;
  path: string;
  status: number;
  details?: Record<string, any>;
}

export interface ValidationError {
  field: string;
  message: string;
  rejectedValue: any;
}

export interface ApiError extends Error {
  status: number;
  response?: ErrorResponse;
  validationErrors?: ValidationError[];
}
```

## 🔒 Security Considerations

### Authentication
- JWT tokens with automatic refresh
- Role-based access control (RBAC)
- Permission-based authorization
- Secure token storage

### API Security
- HTTPS enforcement
- CORS configuration
- Rate limiting
- Input validation and sanitization
- SQL injection prevention
- XSS protection

### Data Protection
- Encryption in transit
- Encryption at rest
- PII data handling
- Audit logging
- Data retention policies

This comprehensive API documentation provides developers with all the information needed to integrate with the React Operations Portal's backend services effectively and securely.
