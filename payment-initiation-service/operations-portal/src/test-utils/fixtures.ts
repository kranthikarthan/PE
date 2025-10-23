/**
 * Test Fixtures
 * 
 * Predefined test data and fixtures for consistent testing.
 * Provides mock data for all domain objects and API responses.
 */

import { ServiceHealth, ServiceHealthSummary, ServiceAlert } from '@types/service';
import { Payment, PaymentStatus } from '@types/payment';
import { Transaction, TransactionStatus, TransactionType } from '@types/transaction';
import { ReconciliationBatch, PerformanceMetric, ReconciliationException } from '@types/reconciliation';
import { Channel, ChannelConfiguration } from '@types/onboarding';
import { User, AuthResponse } from '@types/auth';

// Service Health Fixtures
export const serviceHealthFixtures: ServiceHealth[] = [
  {
    id: '1',
    name: 'Payment Initiation Service',
    status: 'UP',
    uptime: 86400,
    responseTime: 150,
    instances: 3,
    version: '1.0.0',
    lastCheck: new Date().toISOString(),
  },
  {
    id: '2',
    name: 'Saga Orchestrator',
    status: 'UP',
    uptime: 86400,
    responseTime: 200,
    instances: 2,
    version: '1.0.0',
    lastCheck: new Date().toISOString(),
  },
  {
    id: '3',
    name: 'Analytics Service',
    status: 'DEGRADED',
    uptime: 72000,
    responseTime: 500,
    instances: 1,
    version: '1.0.0',
    lastCheck: new Date().toISOString(),
  },
];

export const serviceHealthSummaryFixture: ServiceHealthSummary = {
  totalServices: 3,
  healthyServices: 2,
  unhealthyServices: 1,
  overallHealth: 'DEGRADED',
};

export const serviceAlertFixtures: ServiceAlert[] = [
  {
    id: '1',
    message: 'High CPU usage detected on Analytics Service',
    severity: 'HIGH',
    timestamp: new Date(Date.now() - 3600000).toISOString(),
    serviceId: '3',
  },
  {
    id: '2',
    message: 'Memory usage above threshold',
    severity: 'MEDIUM',
    timestamp: new Date(Date.now() - 7200000).toISOString(),
    serviceId: '2',
  },
];

// Payment Fixtures
export const paymentFixtures: Payment[] = [
  {
    id: 'payment-1',
    amount: 1000,
    currency: 'USD',
    status: PaymentStatus.FAILED,
    sourceAccount: 'ACC123456',
    destinationAccount: 'ACC789012',
    reference: 'REF123456',
    createdAt: new Date(Date.now() - 3600000).toISOString(),
    completedAt: null,
    processingTime: 1500,
    failureReason: 'Insufficient funds',
  },
  {
    id: 'payment-2',
    amount: 2500,
    currency: 'USD',
    status: PaymentStatus.TIMEOUT,
    sourceAccount: 'ACC123456',
    destinationAccount: 'ACC789012',
    reference: 'REF123457',
    createdAt: new Date(Date.now() - 7200000).toISOString(),
    completedAt: null,
    processingTime: 30000,
    failureReason: 'Request timeout',
  },
  {
    id: 'payment-3',
    amount: 500,
    currency: 'USD',
    status: PaymentStatus.RETRY_INITIATED,
    sourceAccount: 'ACC123456',
    destinationAccount: 'ACC789012',
    reference: 'REF123458',
    createdAt: new Date(Date.now() - 1800000).toISOString(),
    completedAt: null,
    processingTime: 2000,
    failureReason: 'Network error',
  },
];

// Transaction Fixtures
export const transactionFixtures: Transaction[] = [
  {
    id: 'transaction-1',
    amount: 1000,
    currency: 'USD',
    status: TransactionStatus.COMPLETED,
    type: TransactionType.PAYMENT,
    sourceAccount: 'ACC123456',
    destinationAccount: 'ACC789012',
    reference: 'REF123456',
    createdAt: new Date(Date.now() - 3600000).toISOString(),
    completedAt: new Date(Date.now() - 3500000).toISOString(),
    processingTime: 1000,
  },
  {
    id: 'transaction-2',
    amount: 2500,
    currency: 'USD',
    status: TransactionStatus.PENDING,
    type: TransactionType.PAYMENT,
    sourceAccount: 'ACC123456',
    destinationAccount: 'ACC789012',
    reference: 'REF123457',
    createdAt: new Date(Date.now() - 1800000).toISOString(),
    completedAt: null,
    processingTime: null,
  },
  {
    id: 'transaction-3',
    amount: 500,
    currency: 'USD',
    status: TransactionStatus.FAILED,
    type: TransactionType.REFUND,
    sourceAccount: 'ACC123456',
    destinationAccount: 'ACC789012',
    reference: 'REF123458',
    createdAt: new Date(Date.now() - 900000).toISOString(),
    completedAt: null,
    processingTime: 2000,
    failureReason: 'Invalid account number',
  },
];

// Reconciliation Fixtures
export const reconciliationBatchFixtures: ReconciliationBatch[] = [
  {
    id: 'batch-1',
    batchName: 'Daily Reconciliation - 2024-01-15',
    status: 'COMPLETED',
    startTime: new Date(Date.now() - 3600000).toISOString(),
    endTime: new Date(Date.now() - 3000000).toISOString(),
    totalRecords: 1000,
    processedRecords: 1000,
    failedRecords: 5,
    successRate: 0.995,
    processingTime: 600,
  },
  {
    id: 'batch-2',
    batchName: 'Daily Reconciliation - 2024-01-14',
    status: 'PROCESSING',
    startTime: new Date(Date.now() - 1800000).toISOString(),
    endTime: null,
    totalRecords: 1500,
    processedRecords: 1200,
    failedRecords: 10,
    successRate: 0.992,
    processingTime: 1800,
  },
  {
    id: 'batch-3',
    batchName: 'Daily Reconciliation - 2024-01-13',
    status: 'FAILED',
    startTime: new Date(Date.now() - 7200000).toISOString(),
    endTime: new Date(Date.now() - 7000000).toISOString(),
    totalRecords: 2000,
    processedRecords: 1500,
    failedRecords: 500,
    successRate: 0.75,
    processingTime: 200,
  },
];

export const performanceMetricFixtures: PerformanceMetric[] = [
  {
    name: 'Processing Time',
    value: '1.8s',
    unit: 'seconds',
    trend: 'down',
    description: 'Average batch processing time',
  },
  {
    name: 'Success Rate',
    value: '92%',
    unit: 'percentage',
    trend: 'up',
    description: 'Batch processing success rate',
  },
  {
    name: 'Throughput',
    value: '150',
    unit: 'transactions/second',
    trend: 'up',
    description: 'Average transaction processing rate',
  },
];

export const reconciliationExceptionFixtures: ReconciliationException[] = [
  {
    id: 'exception-1',
    batchId: 'batch-1',
    type: 'VALIDATION_ERROR',
    severity: 'HIGH',
    message: 'Invalid account number format',
    createdAt: new Date(Date.now() - 3600000).toISOString(),
  },
  {
    id: 'exception-2',
    batchId: 'batch-2',
    type: 'PROCESSING_ERROR',
    severity: 'MEDIUM',
    message: 'Timeout during processing',
    createdAt: new Date(Date.now() - 7200000).toISOString(),
  },
];

// Channel Fixtures
export const channelFixtures: Channel[] = [
  {
    id: 'channel-1',
    name: 'Bank API Channel',
    type: 'BANK_API',
    status: 'ACTIVE',
    endpoint: 'https://api.bank.com/v1',
    description: 'Primary bank API channel',
    createdAt: new Date(Date.now() - 86400000).toISOString(),
    updatedAt: new Date(Date.now() - 3600000).toISOString(),
    authentication: {
      type: 'API_KEY',
      credentials: {
        apiKey: 'encrypted-key',
      },
    },
    configuration: {
      timeout: 30,
      retryAttempts: 3,
      enableLogging: true,
      enableMonitoring: true,
    },
  },
  {
    id: 'channel-2',
    name: 'Card Network Channel',
    type: 'CARD_NETWORK',
    status: 'INACTIVE',
    endpoint: 'https://api.cardnetwork.com/v1',
    description: 'Card network integration channel',
    createdAt: new Date(Date.now() - 172800000).toISOString(),
    updatedAt: new Date(Date.now() - 7200000).toISOString(),
    authentication: {
      type: 'OAUTH2',
      credentials: {
        clientId: 'encrypted-client-id',
        clientSecret: 'encrypted-client-secret',
      },
    },
    configuration: {
      timeout: 45,
      retryAttempts: 2,
      enableLogging: false,
      enableMonitoring: true,
    },
  },
];

// User and Auth Fixtures
export const userFixture: User = {
  id: '1',
  firstName: 'John',
  lastName: 'Doe',
  email: 'john.doe@example.com',
  tenantId: 'tenant-1',
  businessUnitId: 'bu-1',
  roles: ['ADMIN', 'OPERATOR'],
  permissions: ['MANAGE_SERVICES', 'VIEW_PAYMENTS', 'MANAGE_CHANNELS'],
  lastLogin: new Date(Date.now() - 3600000).toISOString(),
  createdAt: new Date(Date.now() - 86400000).toISOString(),
};

export const authResponseFixture: AuthResponse = {
  token: 'mock-jwt-token',
  user: userFixture,
  expiresIn: 3600,
  refreshToken: 'mock-refresh-token',
};

// API Response Fixtures
export const apiResponseFixtures = {
  success: {
    message: 'Operation completed successfully',
    data: null,
    timestamp: new Date().toISOString(),
  },
  error: {
    message: 'An error occurred',
    error: 'VALIDATION_ERROR',
    details: 'Invalid input provided',
    timestamp: new Date().toISOString(),
  },
  pagedResponse: {
    content: paymentFixtures,
    totalElements: 100,
    totalPages: 10,
    size: 10,
    number: 0,
    first: true,
    last: false,
  },
};

// Test Scenarios
export const testScenarios = {
  successfulLogin: {
    email: 'john.doe@example.com',
    password: 'password123',
    expectedUser: userFixture,
  },
  failedLogin: {
    email: 'invalid@example.com',
    password: 'wrongpassword',
    expectedError: 'Invalid credentials',
  },
  serviceControl: {
    serviceId: '1',
    action: 'RESTART',
    reason: 'Scheduled maintenance',
    expectedResult: 'Service restart initiated successfully',
  },
  paymentRepair: {
    paymentIds: ['payment-1', 'payment-2'],
    action: 'RETRY',
    reason: 'Bulk retry operation',
    expectedResult: 'Bulk retry initiated successfully',
  },
  transactionSearch: {
    query: 'REF123456',
    filters: {
      status: 'COMPLETED',
      dateFrom: '2024-01-01',
      dateTo: '2024-01-31',
    },
    expectedResults: 1,
  },
};

// Mock Functions
export const mockApiFunctions = {
  login: jest.fn().mockResolvedValue(authResponseFixture),
  logout: jest.fn().mockResolvedValue({ message: 'Logged out successfully' }),
  getServicesHealth: jest.fn().mockResolvedValue(serviceHealthFixtures),
  getServiceHealthSummary: jest.fn().mockResolvedValue(serviceHealthSummaryFixture),
  getServiceAlerts: jest.fn().mockResolvedValue(serviceAlertFixtures),
  getFailedPayments: jest.fn().mockResolvedValue(apiResponseFixtures.pagedResponse),
  retryPayment: jest.fn().mockResolvedValue({ message: 'Payment retry initiated successfully' }),
  cancelPayment: jest.fn().mockResolvedValue({ message: 'Payment cancelled successfully' }),
  searchTransactions: jest.fn().mockResolvedValue(apiResponseFixtures.pagedResponse),
  getReconciliationBatches: jest.fn().mockResolvedValue(apiResponseFixtures.pagedResponse),
  getPerformanceMetrics: jest.fn().mockResolvedValue(performanceMetricFixtures),
  getReconciliationExceptions: jest.fn().mockResolvedValue(reconciliationExceptionFixtures),
  getAllChannels: jest.fn().mockResolvedValue(channelFixtures),
  createChannel: jest.fn().mockResolvedValue({ message: 'Channel created successfully' }),
  updateChannel: jest.fn().mockResolvedValue({ message: 'Channel updated successfully' }),
  deleteChannel: jest.fn().mockResolvedValue({ message: 'Channel deleted successfully' }),
  testChannelConnection: jest.fn().mockResolvedValue({
    success: true,
    message: 'Connection test successful',
    details: { responseTime: 150, status: 'OK' },
  }),
};

// Error Fixtures
export const errorFixtures = {
  networkError: new Error('Network Error'),
  validationError: new Error('Validation Error'),
  authenticationError: new Error('Authentication Error'),
  authorizationError: new Error('Authorization Error'),
  serverError: new Error('Server Error'),
  timeoutError: new Error('Request Timeout'),
};

// Loading States
export const loadingStates = {
  initial: { loading: false, error: null, data: null },
  loading: { loading: true, error: null, data: null },
  success: { loading: false, error: null, data: serviceHealthFixtures },
  error: { loading: false, error: 'An error occurred', data: null },
};

// Export all fixtures
export const fixtures = {
  serviceHealth: serviceHealthFixtures,
  serviceHealthSummary: serviceHealthSummaryFixture,
  serviceAlerts: serviceAlertFixtures,
  payments: paymentFixtures,
  transactions: transactionFixtures,
  reconciliationBatches: reconciliationBatchFixtures,
  performanceMetrics: performanceMetricFixtures,
  reconciliationExceptions: reconciliationExceptionFixtures,
  channels: channelFixtures,
  user: userFixture,
  authResponse: authResponseFixture,
  apiResponses: apiResponseFixtures,
  testScenarios: testScenarios,
  mockFunctions: mockApiFunctions,
  errors: errorFixtures,
  loadingStates: loadingStates,
};
