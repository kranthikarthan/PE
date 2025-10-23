/**
 * MSW Handlers
 * 
 * Mock API handlers for all microservices.
 * Provides realistic responses for testing and development.
 */

import { rest } from 'msw';
import { API_BASE_URL } from '@config/environment';

// Mock data generators
const generateServiceHealth = (id: string, name: string, status: string) => ({
  id,
  name,
  status,
  uptime: Math.floor(Math.random() * 86400),
  responseTime: Math.floor(Math.random() * 1000) + 100,
  instances: Math.floor(Math.random() * 5) + 1,
  version: '1.0.0',
  lastCheck: new Date().toISOString(),
});

const generatePayment = (id: string) => ({
  id,
  amount: Math.floor(Math.random() * 10000) + 100,
  currency: 'USD',
  status: ['PENDING', 'COMPLETED', 'FAILED', 'CANCELLED'][Math.floor(Math.random() * 4)],
  sourceAccount: `ACC${Math.floor(Math.random() * 10000)}`,
  destinationAccount: `ACC${Math.floor(Math.random() * 10000)}`,
  reference: `REF${Math.floor(Math.random() * 100000)}`,
  createdAt: new Date(Date.now() - Math.random() * 86400000).toISOString(),
  completedAt: Math.random() > 0.5 ? new Date().toISOString() : null,
  processingTime: Math.floor(Math.random() * 5000) + 100,
});

const generateTransaction = (id: string) => ({
  id,
  amount: Math.floor(Math.random() * 10000) + 100,
  currency: 'USD',
  status: ['COMPLETED', 'PENDING', 'FAILED', 'PROCESSING'][Math.floor(Math.random() * 4)],
  type: ['PAYMENT', 'REFUND', 'REVERSAL', 'ADJUSTMENT'][Math.floor(Math.random() * 4)],
  sourceAccount: `ACC${Math.floor(Math.random() * 10000)}`,
  destinationAccount: `ACC${Math.floor(Math.random() * 10000)}`,
  reference: `REF${Math.floor(Math.random() * 100000)}`,
  createdAt: new Date(Date.now() - Math.random() * 86400000).toISOString(),
  completedAt: Math.random() > 0.5 ? new Date().toISOString() : null,
  processingTime: Math.floor(Math.random() * 5000) + 100,
});

const generateReconciliationBatch = (id: string) => ({
  id,
  batchName: `Batch_${id}`,
  status: ['COMPLETED', 'PROCESSING', 'FAILED', 'PENDING'][Math.floor(Math.random() * 4)],
  startTime: new Date(Date.now() - Math.random() * 3600000).toISOString(),
  endTime: Math.random() > 0.5 ? new Date().toISOString() : null,
  totalRecords: Math.floor(Math.random() * 10000) + 100,
  processedRecords: Math.floor(Math.random() * 10000) + 50,
  failedRecords: Math.floor(Math.random() * 100) + 1,
  successRate: Math.random() * 0.3 + 0.7,
  processingTime: Math.floor(Math.random() * 3600) + 60,
});

export const handlers = [
  // Authentication endpoints
  rest.post(`${API_BASE_URL}/auth/login`, (req, res, ctx) => {
    return res(
      ctx.status(200),
      ctx.json({
        token: 'mock-jwt-token',
        user: {
          id: '1',
          firstName: 'John',
          lastName: 'Doe',
          email: 'john.doe@example.com',
          tenantId: 'tenant-1',
          businessUnitId: 'bu-1',
          roles: ['ADMIN', 'OPERATOR'],
        },
      })
    );
  }),

  rest.post(`${API_BASE_URL}/auth/logout`, (req, res, ctx) => {
    return res(ctx.status(200), ctx.json({ message: 'Logged out successfully' }));
  }),

  rest.get(`${API_BASE_URL}/auth/me`, (req, res, ctx) => {
    return res(
      ctx.status(200),
      ctx.json({
        id: '1',
        firstName: 'John',
        lastName: 'Doe',
        email: 'john.doe@example.com',
        tenantId: 'tenant-1',
        businessUnitId: 'bu-1',
        roles: ['ADMIN', 'OPERATOR'],
      })
    );
  }),

  // Operations Management Service
  rest.get(`${API_BASE_URL}/ops/v1/services/health`, (req, res, ctx) => {
    const services = [
      generateServiceHealth('1', 'Payment Initiation Service', 'UP'),
      generateServiceHealth('2', 'Saga Orchestrator', 'UP'),
      generateServiceHealth('3', 'Analytics Service', 'DEGRADED'),
      generateServiceHealth('4', 'Reconciliation Service', 'UP'),
      generateServiceHealth('5', 'Operations Management Service', 'UP'),
    ];

    return res(ctx.status(200), ctx.json(services));
  }),

  rest.get(`${API_BASE_URL}/ops/v1/services/health/summary`, (req, res, ctx) => {
    return res(
      ctx.status(200),
      ctx.json({
        totalServices: 5,
        healthyServices: 4,
        unhealthyServices: 1,
        overallHealth: 'HEALTHY',
      })
    );
  }),

  rest.get(`${API_BASE_URL}/ops/v1/services/alerts`, (req, res, ctx) => {
    const alerts = [
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

    return res(ctx.status(200), ctx.json(alerts));
  }),

  rest.get(`${API_BASE_URL}/ops/v1/circuit-breakers`, (req, res, ctx) => {
    const circuitBreakers = [
      {
        name: 'payment-service',
        state: 'CLOSED',
        failureRate: 0.05,
        lastFailure: null,
      },
      {
        name: 'analytics-service',
        state: 'OPEN',
        failureRate: 0.8,
        lastFailure: new Date().toISOString(),
      },
    ];

    return res(ctx.status(200), ctx.json(circuitBreakers));
  }),

  rest.get(`${API_BASE_URL}/ops/v1/feature-flags`, (req, res, ctx) => {
    const featureFlags = [
      {
        name: 'ENABLE_NEW_UI',
        enabled: true,
        description: 'Enable new user interface',
      },
      {
        name: 'ENABLE_ANALYTICS',
        enabled: false,
        description: 'Enable analytics features',
      },
    ];

    return res(ctx.status(200), ctx.json(featureFlags));
  }),

  rest.get(`${API_BASE_URL}/ops/v1/pods`, (req, res, ctx) => {
    const pods = [
      {
        name: 'payment-service-pod-1',
        status: 'Running',
        restarts: 0,
        age: '2d',
        cpu: '100m',
        memory: '256Mi',
        node: 'node-1',
      },
      {
        name: 'analytics-service-pod-1',
        status: 'Running',
        restarts: 2,
        age: '1d',
        cpu: '200m',
        memory: '512Mi',
        node: 'node-2',
      },
    ];

    return res(ctx.status(200), ctx.json(pods));
  }),

  // Payment Initiation Service
  rest.get(`${API_BASE_URL}/payments/v1/failed`, (req, res, ctx) => {
    const page = parseInt(req.url.searchParams.get('page') || '0');
    const size = parseInt(req.url.searchParams.get('size') || '20');
    
    const payments = Array.from({ length: size }, (_, i) => 
      generatePayment(`payment-${page * size + i}`)
    );

    return res(
      ctx.status(200),
      ctx.json({
        content: payments,
        totalElements: 100,
        totalPages: 5,
        size,
        number: page,
      })
    );
  }),

  rest.post(`${API_BASE_URL}/payments/v1/retry`, (req, res, ctx) => {
    return res(
      ctx.status(200),
      ctx.json({ message: 'Payment retry initiated successfully' })
    );
  }),

  rest.post(`${API_BASE_URL}/payments/v1/cancel`, (req, res, ctx) => {
    return res(
      ctx.status(200),
      ctx.json({ message: 'Payment cancelled successfully' })
    );
  }),

  rest.post(`${API_BASE_URL}/payments/v1/bulk-retry`, (req, res, ctx) => {
    return res(
      ctx.status(200),
      ctx.json({ message: 'Bulk retry initiated successfully' })
    );
  }),

  rest.post(`${API_BASE_URL}/payments/v1/bulk-cancel`, (req, res, ctx) => {
    return res(
      ctx.status(200),
      ctx.json({ message: 'Bulk cancel initiated successfully' })
    );
  }),

  // Transaction Processing Service
  rest.get(`${API_BASE_URL}/transactions/v1/search`, (req, res, ctx) => {
    const page = parseInt(req.url.searchParams.get('page') || '0');
    const size = parseInt(req.url.searchParams.get('size') || '20');
    
    const transactions = Array.from({ length: size }, (_, i) => 
      generateTransaction(`transaction-${page * size + i}`)
    );

    return res(
      ctx.status(200),
      ctx.json({
        content: transactions,
        totalElements: 200,
        totalPages: 10,
        size,
        number: page,
      })
    );
  }),

  rest.get(`${API_BASE_URL}/transactions/v1/metrics`, (req, res, ctx) => {
    return res(
      ctx.status(200),
      ctx.json({
        totalTransactions: 10000,
        successRate: 0.95,
        failedTransactions: 500,
        averageProcessingTime: 1500,
      })
    );
  }),

  rest.get(`${API_BASE_URL}/transactions/v1/:id`, (req, res, ctx) => {
    const { id } = req.params;
    const transaction = generateTransaction(id as string);
    return res(ctx.status(200), ctx.json(transaction));
  }),

  // Reconciliation Service
  rest.get(`${API_BASE_URL}/reconciliation/v1/batches`, (req, res, ctx) => {
    const page = parseInt(req.url.searchParams.get('page') || '0');
    const size = parseInt(req.url.searchParams.get('size') || '20');
    
    const batches = Array.from({ length: size }, (_, i) => 
      generateReconciliationBatch(`batch-${page * size + i}`)
    );

    return res(
      ctx.status(200),
      ctx.json({
        content: batches,
        totalElements: 50,
        totalPages: 3,
        size,
        number: page,
      })
    );
  }),

  rest.get(`${API_BASE_URL}/reconciliation/v1/metrics`, (req, res, ctx) => {
    return res(
      ctx.status(200),
      ctx.json({
        totalBatches: 50,
        successRate: 0.92,
        failedBatches: 4,
        averageProcessingTime: 1800,
        detailedMetrics: [
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
        ],
      })
    );
  }),

  rest.get(`${API_BASE_URL}/reconciliation/v1/exceptions`, (req, res, ctx) => {
    const exceptions = [
      {
        id: '1',
        batchId: 'batch-1',
        type: 'VALIDATION_ERROR',
        severity: 'HIGH',
        message: 'Invalid account number format',
        createdAt: new Date(Date.now() - 3600000).toISOString(),
      },
      {
        id: '2',
        batchId: 'batch-2',
        type: 'PROCESSING_ERROR',
        severity: 'MEDIUM',
        message: 'Timeout during processing',
        createdAt: new Date(Date.now() - 7200000).toISOString(),
      },
    ];

    return res(ctx.status(200), ctx.json(exceptions));
  }),

  // Tenant Management Service
  rest.get(`${API_BASE_URL}/tenant/v1/channels`, (req, res, ctx) => {
    const channels = [
      {
        id: '1',
        name: 'Bank API Channel',
        type: 'BANK_API',
        status: 'ACTIVE',
        endpoint: 'https://api.bank.com/v1',
        createdAt: new Date(Date.now() - 86400000).toISOString(),
        authentication: {
          type: 'API_KEY',
          credentials: {},
        },
        configuration: {
          timeout: 30,
          retryAttempts: 3,
        },
      },
      {
        id: '2',
        name: 'Card Network Channel',
        type: 'CARD_NETWORK',
        status: 'INACTIVE',
        endpoint: 'https://api.cardnetwork.com/v1',
        createdAt: new Date(Date.now() - 172800000).toISOString(),
        authentication: {
          type: 'OAUTH2',
          credentials: {},
        },
        configuration: {
          timeout: 45,
          retryAttempts: 2,
        },
      },
    ];

    return res(ctx.status(200), ctx.json(channels));
  }),

  rest.get(`${API_BASE_URL}/ops/v1/clearing-systems`, (req, res, ctx) => {
    const clearingSystems = [
      {
        id: '1',
        name: 'SAMOS Clearing System',
        type: 'SAMOS',
        status: 'ACTIVE',
        endpoint: 'https://samos.clearing.com/api',
        messageFormat: {
          format: 'ISO20022',
          version: '1.0',
          encoding: 'UTF-8',
        },
        createdAt: new Date(Date.now() - 259200000).toISOString(),
      },
      {
        id: '2',
        name: 'BankservAfrica System',
        type: 'BANKSERV_AFRICA',
        status: 'ACTIVE',
        endpoint: 'https://bankserv.africa/api',
        messageFormat: {
          format: 'ISO8583',
          version: '1.1',
          encoding: 'ASCII',
        },
        createdAt: new Date(Date.now() - 345600000).toISOString(),
      },
    ];

    return res(ctx.status(200), ctx.json(clearingSystems));
  }),

  // Error handlers
  rest.get('*', (req, res, ctx) => {
    console.warn(`Unhandled GET request: ${req.url}`);
    return res(ctx.status(404), ctx.json({ message: 'Not found' }));
  }),

  rest.post('*', (req, res, ctx) => {
    console.warn(`Unhandled POST request: ${req.url}`);
    return res(ctx.status(404), ctx.json({ message: 'Not found' }));
  }),

  rest.put('*', (req, res, ctx) => {
    console.warn(`Unhandled PUT request: ${req.url}`);
    return res(ctx.status(404), ctx.json({ message: 'Not found' }));
  }),

  rest.delete('*', (req, res, ctx) => {
    console.warn(`Unhandled DELETE request: ${req.url}`);
    return res(ctx.status(404), ctx.json({ message: 'Not found' }));
  }),
];
