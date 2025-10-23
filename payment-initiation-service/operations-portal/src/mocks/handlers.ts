/**
 * MSW Handlers
 * 
 * Mock API handlers for all microservices.
 * Provides realistic responses for testing and development.
 */

import { http, HttpResponse } from 'msw';
import config from '@config/environment';

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
  http.post(`${config.apiBaseUrl}/auth/login`, async ({ request }) => {
    return HttpResponse.json({
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
    });
  }),

  http.post(`${config.apiBaseUrl}/auth/logout`, async ({ request }) => {
    return HttpResponse.json({ message: 'Logged out successfully' });
  }),

  http.get(`${config.apiBaseUrl}/auth/me`, async ({ request }) => {
    return HttpResponse.json({
      id: '1',
      firstName: 'John',
      lastName: 'Doe',
      email: 'john.doe@example.com',
      tenantId: 'tenant-1',
      businessUnitId: 'bu-1',
      roles: ['ADMIN', 'OPERATOR'],
    });
  }),

  // Operations Management Service
  http.get(`${config.apiBaseUrl}/ops/v1/services/health`, async ({ request }) => {
    const services = [
      generateServiceHealth('1', 'Payment Initiation Service', 'UP'),
      generateServiceHealth('2', 'Saga Orchestrator', 'UP'),
      generateServiceHealth('3', 'Analytics Service', 'DEGRADED'),
      generateServiceHealth('4', 'Reconciliation Service', 'UP'),
      generateServiceHealth('5', 'Operations Management Service', 'UP'),
    ];

    return HttpResponse.json(services);
  }),

  http.get(`${config.apiBaseUrl}/ops/v1/services/health/summary`, async ({ request }) => {
    return HttpResponse.json({
      totalServices: 5,
      healthyServices: 4,
      unhealthyServices: 1,
      overallHealth: 'HEALTHY',
    });
  }),

  http.get(`${config.apiBaseUrl}/ops/v1/services/alerts`, async ({ request }) => {
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

    return HttpResponse.json(alerts);
  }),

  http.get(`${config.apiBaseUrl}/ops/v1/circuit-breakers`, async ({ request }) => {
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

    return HttpResponse.json(circuitBreakers);
  }),

  http.get(`${config.apiBaseUrl}/ops/v1/feature-flags`, async ({ request }) => {
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

    return HttpResponse.json(featureFlags);
  }),

  http.get(`${config.apiBaseUrl}/ops/v1/pods`, async ({ request }) => {
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

    return HttpResponse.json(pods);
  }),

  // Payment Initiation Service
  http.get(`${config.apiBaseUrl}/payments/v1/failed`, async ({ request }) => {
    const url = new URL(request.url);
    const page = parseInt(url.searchParams.get('page') || '0');
    const size = parseInt(url.searchParams.get('size') || '20');
    
    const payments = Array.from({ length: size }, (_, i) => 
      generatePayment(`payment-${page * size + i}`)
    );

    return HttpResponse.json({
      content: payments,
      totalElements: 100,
      totalPages: 5,
      size,
      number: page,
    });
  }),

  http.post(`${config.apiBaseUrl}/payments/v1/retry`, async ({ request }) => {
    return HttpResponse.json({ message: 'Payment retry initiated successfully' });
  }),

  http.post(`${config.apiBaseUrl}/payments/v1/cancel`, async ({ request }) => {
    return HttpResponse.json({ message: 'Payment cancelled successfully' });
  }),

  http.post(`${config.apiBaseUrl}/payments/v1/bulk-retry`, async ({ request }) => {
    return HttpResponse.json({ message: 'Bulk retry initiated successfully' });
  }),

  http.post(`${config.apiBaseUrl}/payments/v1/bulk-cancel`, async ({ request }) => {
    return HttpResponse.json({ message: 'Bulk cancel initiated successfully' });
  }),

  // Transaction Processing Service
  http.get(`${config.apiBaseUrl}/transactions/v1/search`, async ({ request }) => {
    const url = new URL(request.url);
    const page = parseInt(url.searchParams.get('page') || '0');
    const size = parseInt(url.searchParams.get('size') || '20');
    
    const transactions = Array.from({ length: size }, (_, i) => 
      generateTransaction(`transaction-${page * size + i}`)
    );

    return HttpResponse.json({
      content: transactions,
      totalElements: 200,
      totalPages: 10,
      size,
      number: page,
    });
  }),

  http.get(`${config.apiBaseUrl}/transactions/v1/metrics`, async ({ request }) => {
    return HttpResponse.json({
      totalTransactions: 10000,
      successRate: 0.95,
      failedTransactions: 500,
      averageProcessingTime: 1500,
    });
  }),

  http.get(`${config.apiBaseUrl}/transactions/v1/:id`, async ({ params }) => {
    const { id } = params;
    const transaction = generateTransaction(id as string);
    return HttpResponse.json(transaction);
  }),

  // Reconciliation Service
  http.get(`${config.apiBaseUrl}/reconciliation/v1/batches`, async ({ request }) => {
    const url = new URL(request.url);
    const page = parseInt(url.searchParams.get('page') || '0');
    const size = parseInt(url.searchParams.get('size') || '20');
    
    const batches = Array.from({ length: size }, (_, i) => 
      generateReconciliationBatch(`batch-${page * size + i}`)
    );

    return HttpResponse.json({
      content: batches,
      totalElements: 50,
      totalPages: 3,
      size,
      number: page,
    });
  }),

  http.get(`${config.apiBaseUrl}/reconciliation/v1/metrics`, async ({ request }) => {
    return HttpResponse.json({
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
    });
  }),

  http.get(`${config.apiBaseUrl}/reconciliation/v1/exceptions`, async ({ request }) => {
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

    return HttpResponse.json(exceptions);
  }),

  // Tenant Management Service
  http.get(`${config.apiBaseUrl}/tenant/v1/channels`, async ({ request }) => {
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

    return HttpResponse.json(channels);
  }),

  http.get(`${config.apiBaseUrl}/ops/v1/clearing-systems`, async ({ request }) => {
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

    return HttpResponse.json(clearingSystems);
  }),

  // Error handlers
  http.get('*', async ({ request }) => {
    console.warn(`Unhandled GET request: ${request.url}`);
    return HttpResponse.json({ message: 'Not found' }, { status: 404 });
  }),

  http.post('*', async ({ request }) => {
    console.warn(`Unhandled POST request: ${request.url}`);
    return HttpResponse.json({ message: 'Not found' }, { status: 404 });
  }),

  http.put('*', async ({ request }) => {
    console.warn(`Unhandled PUT request: ${request.url}`);
    return HttpResponse.json({ message: 'Not found' }, { status: 404 });
  }),

  http.delete('*', async ({ request }) => {
    console.warn(`Unhandled DELETE request: ${request.url}`);
    return HttpResponse.json({ message: 'Not found' }, { status: 404 });
  }),
];