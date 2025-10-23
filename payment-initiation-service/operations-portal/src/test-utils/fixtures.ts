/**
 * Test Fixtures
 * 
 * Predefined test data and fixtures for consistent testing.
 * Provides mock data for all domain objects and API responses.
 */

import { ServiceHealth, ServiceHealthSummary, ServiceAlerts, ServiceStatus } from '../types/service';
import { Payment, PaymentStatus, Currency, PaymentMethod } from '../types/payment';
import { Transaction, TransactionStatus, TransactionType } from '../types/transaction';
import { ReconciliationBatch, PerformanceMetric, ReconciliationException, ReconciliationBatchStatus, ReconciliationType } from '../types/reconciliation';
import { Channel, ChannelConfiguration, ChannelType, OnboardingStatus, AuthenticationMethod } from '../types/onboarding';
import { User, AuthResponse, UserRole, Permission } from '../types/auth';

// Service Health Fixtures
export const serviceHealthFixtures: ServiceHealth[] = [
  {
    id: '1',
    name: 'Payment Initiation Service',
    status: ServiceStatus.UP,
    uptime: 86400,
    responseTime: 150,
    instances: 3,
    version: '1.0.0',
    lastCheck: new Date().toISOString(),
    components: { database: ServiceStatus.UP, cache: ServiceStatus.UP },
    metrics: {
      requestsPerSecond: 100,
      errorRate: 0.01,
      averageResponseTime: 150,
      p95ResponseTime: 200,
      p99ResponseTime: 500,
      memoryUsage: 512,
      cpuUsage: 0.3,
      activeConnections: 50,
    },
  },
  {
    id: '2',
    name: 'Saga Orchestrator',
    status: ServiceStatus.UP,
    uptime: 86400,
    responseTime: 200,
    instances: 2,
    version: '1.0.0',
    lastCheck: new Date().toISOString(),
    components: { database: ServiceStatus.UP, cache: ServiceStatus.UP },
    metrics: {
      requestsPerSecond: 50,
      errorRate: 0.02,
      averageResponseTime: 200,
      p95ResponseTime: 300,
      p99ResponseTime: 600,
      memoryUsage: 256,
      cpuUsage: 0.2,
      activeConnections: 25,
    },
  },
  {
    id: '3',
    name: 'Analytics Service',
    status: ServiceStatus.DEGRADED,
    uptime: 72000,
    responseTime: 500,
    instances: 1,
    version: '1.0.0',
    lastCheck: new Date().toISOString(),
    components: { database: ServiceStatus.DEGRADED, cache: ServiceStatus.UP },
    metrics: {
      requestsPerSecond: 25,
      errorRate: 0.1,
      averageResponseTime: 500,
      p95ResponseTime: 800,
      p99ResponseTime: 1200,
      memoryUsage: 1024,
      cpuUsage: 0.8,
      activeConnections: 10,
    },
  },
];

export const serviceHealthSummaryFixture: ServiceHealthSummary = {
  totalServices: 3,
  healthyServices: 2,
  unhealthyServices: 1,
  overallHealth: 'DEGRADED' as const,
};

export const serviceAlertFixtures: ServiceAlerts[] = [
  {
    id: '1',
    message: 'High CPU usage detected on Analytics Service',
    severity: 'HIGH',
    timestamp: new Date(Date.now() - 3600000).toISOString(),
    serviceId: '3',
    acknowledged: false,
  },
  {
    id: '2',
    message: 'Memory usage above threshold',
    severity: 'MEDIUM',
    timestamp: new Date(Date.now() - 7200000).toISOString(),
    serviceId: '2',
    acknowledged: true,
    acknowledgedBy: 'admin',
    acknowledgedAt: new Date(Date.now() - 1800000).toISOString(),
  },
];

// Payment Fixtures
export const paymentFixtures: Payment[] = [
  {
    id: 'payment-1',
    amount: 1000,
    currency: Currency.USD,
    status: PaymentStatus.FAILED,
    sourceAccount: 'ACC123456',
    destinationAccount: 'ACC789012',
    reference: 'REF123456',
    method: PaymentMethod.BANK_TRANSFER,
    description: 'Failed payment test',
    createdAt: new Date(Date.now() - 3600000).toISOString(),
    updatedAt: new Date(Date.now() - 3600000).toISOString(),
    completedAt: undefined,
    processingTime: 1500,
    failureReason: 'Insufficient funds',
    tenantId: 'tenant-1',
    businessUnitId: 'bu-1',
    correlationId: 'corr-123',
  },
  {
    id: 'payment-2',
    amount: 2500,
    currency: Currency.USD,
    status: PaymentStatus.TIMEOUT,
    sourceAccount: 'ACC123456',
    destinationAccount: 'ACC789012',
    reference: 'REF123457',
    method: PaymentMethod.BANK_TRANSFER,
    description: 'Timeout payment test',
    createdAt: new Date(Date.now() - 7200000).toISOString(),
    updatedAt: new Date(Date.now() - 7200000).toISOString(),
    completedAt: undefined,
    processingTime: 30000,
    failureReason: 'Request timeout',
    tenantId: 'tenant-1',
    businessUnitId: 'bu-1',
    correlationId: 'corr-124',
  },
  {
    id: 'payment-3',
    amount: 500,
    currency: Currency.USD,
    status: PaymentStatus.RETRY_INITIATED,
    sourceAccount: 'ACC123456',
    destinationAccount: 'ACC789012',
    reference: 'REF123458',
    method: PaymentMethod.CARD,
    description: 'Retry payment test',
    createdAt: new Date(Date.now() - 1800000).toISOString(),
    updatedAt: new Date(Date.now() - 1800000).toISOString(),
    completedAt: undefined,
    processingTime: 2000,
    failureReason: 'Network error',
    tenantId: 'tenant-1',
    businessUnitId: 'bu-1',
    correlationId: 'corr-125',
  },
];

// Transaction Fixtures
export const transactionFixtures: Transaction[] = [
  {
    id: 'transaction-1',
    amount: 1000,
    currency: Currency.USD,
    status: TransactionStatus.COMPLETED,
    type: TransactionType.PAYMENT,
    sourceAccount: 'ACC123456',
    destinationAccount: 'ACC789012',
    reference: 'REF123456',
    createdAt: new Date(Date.now() - 3600000).toISOString(),
    updatedAt: new Date(Date.now() - 3500000).toISOString(),
    completedAt: new Date(Date.now() - 3500000).toISOString(),
    processingTime: 1000,
    tenantId: 'tenant-1',
    businessUnitId: 'bu-1',
    correlationId: 'corr-456',
  },
  {
    id: 'transaction-2',
    amount: 2500,
    currency: Currency.USD,
    status: TransactionStatus.PENDING,
    type: TransactionType.PAYMENT,
    sourceAccount: 'ACC123456',
    destinationAccount: 'ACC789012',
    reference: 'REF123457',
    createdAt: new Date(Date.now() - 1800000).toISOString(),
    updatedAt: new Date(Date.now() - 1800000).toISOString(),
    completedAt: undefined,
    processingTime: 0,
    tenantId: 'tenant-1',
    businessUnitId: 'bu-1',
    correlationId: 'corr-457',
  },
  {
    id: 'transaction-3',
    amount: 500,
    currency: Currency.USD,
    status: TransactionStatus.FAILED,
    type: TransactionType.REFUND,
    sourceAccount: 'ACC123456',
    destinationAccount: 'ACC789012',
    reference: 'REF123458',
    createdAt: new Date(Date.now() - 900000).toISOString(),
    updatedAt: new Date(Date.now() - 900000).toISOString(),
    completedAt: undefined,
    processingTime: 2000,
    failureReason: 'Invalid account number',
    tenantId: 'tenant-1',
    businessUnitId: 'bu-1',
    correlationId: 'corr-458',
  },
];

// Reconciliation Fixtures
export const reconciliationBatchFixtures: ReconciliationBatch[] = [
  {
    id: 'batch-1',
    batchName: 'Daily Reconciliation - 2024-01-15',
    type: ReconciliationType.DAILY,
    status: ReconciliationBatchStatus.COMPLETED,
    startTime: new Date(Date.now() - 3600000).toISOString(),
    endTime: new Date(Date.now() - 3000000).toISOString(),
    totalRecords: 1000,
    processedRecords: 1000,
    failedRecords: 5,
    successRate: 0.995,
    processingTime: '10m',
    errorDetails: undefined,
    tenantId: 'tenant-1',
    businessUnitId: 'bu-1',
    correlationId: 'corr-batch-1',
  },
  {
    id: 'batch-2',
    batchName: 'Daily Reconciliation - 2024-01-14',
    type: ReconciliationType.DAILY,
    status: ReconciliationBatchStatus.PROCESSING,
    startTime: new Date(Date.now() - 1800000).toISOString(),
    endTime: undefined,
    totalRecords: 1500,
    processedRecords: 1200,
    failedRecords: 10,
    successRate: 0.992,
    processingTime: '30m',
    errorDetails: undefined,
    tenantId: 'tenant-1',
    businessUnitId: 'bu-1',
    correlationId: 'corr-batch-2',
  },
  {
    id: 'batch-3',
    batchName: 'Daily Reconciliation - 2024-01-13',
    type: ReconciliationType.DAILY,
    status: ReconciliationBatchStatus.FAILED,
    startTime: new Date(Date.now() - 7200000).toISOString(),
    endTime: new Date(Date.now() - 7000000).toISOString(),
    totalRecords: 2000,
    processedRecords: 1500,
    failedRecords: 500,
    successRate: 0.75,
    processingTime: '3m 20s',
    errorDetails: 'Database connection timeout',
    tenantId: 'tenant-1',
    businessUnitId: 'bu-1',
    correlationId: 'corr-batch-3',
  },
];

export const performanceMetricFixtures: PerformanceMetric[] = [
  {
    name: 'Processing Time',
    value: '1.8s',
    unit: 'seconds',
    trend: 'down',
    change: '-0.2s',
    description: 'Average batch processing time',
  },
  {
    name: 'Success Rate',
    value: '92%',
    unit: 'percentage',
    trend: 'up',
    change: '+2%',
    description: 'Batch processing success rate',
  },
  {
    name: 'Throughput',
    value: '150',
    unit: 'transactions/second',
    trend: 'up',
    change: '+15',
    description: 'Average transaction processing rate',
  },
];

export const reconciliationExceptionFixtures: ReconciliationException[] = [
  {
    id: 'exception-1',
    batchId: 'batch-1',
    recordId: 'record-123',
    exceptionType: 'VALIDATION_ERROR',
    description: 'Invalid account number format',
    severity: 'HIGH',
    status: 'OPEN',
    createdAt: new Date(Date.now() - 3600000).toISOString(),
    resolvedAt: undefined,
    resolvedBy: undefined,
    resolution: undefined,
    metadata: { field: 'accountNumber', expectedFormat: '^[0-9]{10}$' },
  },
  {
    id: 'exception-2',
    batchId: 'batch-2',
    recordId: 'record-456',
    exceptionType: 'PROCESSING_ERROR',
    description: 'Timeout during processing',
    severity: 'MEDIUM',
    status: 'RESOLVED',
    createdAt: new Date(Date.now() - 7200000).toISOString(),
    resolvedAt: new Date(Date.now() - 3600000).toISOString(),
    resolvedBy: 'admin',
    resolution: 'Increased timeout configuration',
    metadata: { timeout: 30000, endpoint: '/api/transactions' },
  },
];

// Channel Fixtures
export const channelFixtures: Channel[] = [
  {
    id: 'channel-1',
    name: 'Bank API Channel',
    type: ChannelType.BANK_API,
    status: OnboardingStatus.COMPLETED,
    endpoint: 'https://api.bank.com/v1',
    description: 'Primary bank API channel',
    authentication: AuthenticationMethod.API_KEY,
    rateLimit: 100,
    webhookUrl: 'https://webhook.bank.com/notifications',
    enableLogging: true,
    enableMonitoring: true,
    createdAt: new Date(Date.now() - 86400000).toISOString(),
    lastUpdated: new Date(Date.now() - 3600000).toISOString(),
    tenantId: 'tenant-1',
    businessUnitId: 'bu-1',
  },
  {
    id: 'channel-2',
    name: 'Card Network Channel',
    type: ChannelType.BANK_API,
    status: OnboardingStatus.PENDING,
    endpoint: 'https://api.cardnetwork.com/v1',
    description: 'Card network integration channel',
    authentication: AuthenticationMethod.OAUTH2,
    rateLimit: 50,
    webhookUrl: 'https://webhook.cardnetwork.com/notifications',
    enableLogging: false,
    enableMonitoring: true,
    createdAt: new Date(Date.now() - 172800000).toISOString(),
    lastUpdated: new Date(Date.now() - 7200000).toISOString(),
    tenantId: 'tenant-1',
    businessUnitId: 'bu-1',
  },
];

// User and Auth Fixtures
export const userFixture: User = {
  id: '1',
  username: 'john.doe',
  firstName: 'John',
  lastName: 'Doe',
  email: 'john.doe@example.com',
  tenantId: 'tenant-1',
  businessUnitId: 'bu-1',
  roles: [UserRole.ADMIN, UserRole.OPS_OPERATOR],
  permissions: [Permission.PAYMENT_VIEW, Permission.SERVICE_CONTROL, Permission.CHANNEL_MANAGE],
  isActive: true,
  lastLogin: new Date(Date.now() - 3600000).toISOString(),
  createdAt: new Date(Date.now() - 86400000).toISOString(),
  updatedAt: new Date(Date.now() - 3600000).toISOString(),
};

export const authResponseFixture: AuthResponse = {
  token: 'mock-jwt-token',
  user: userFixture,
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
      status: ReconciliationBatchStatus.COMPLETED,
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
