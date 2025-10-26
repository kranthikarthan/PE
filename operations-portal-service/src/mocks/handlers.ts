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
  http.post('*/api/auth/v1/login', async ({ request }) => {
    return HttpResponse.json({
      success: true,
      data: {
        token: 'mock-jwt-token',
        user: {
          id: '1',
          firstName: 'John',
          lastName: 'Doe',
          email: 'admin@paymentengine.com',
          tenantId: 'tenant-1',
          businessUnitId: 'bu-1',
          roles: ['ADMIN', 'OPERATOR'],
        },
      },
      message: 'Login successful',
      timestamp: new Date().toISOString(),
      correlationId: 'mock-correlation-id',
    });
  }),

  http.post('*/api/auth/v1/logout', async ({ request }) => {
    return HttpResponse.json({
      success: true,
      data: null,
      message: 'Logged out successfully',
      timestamp: new Date().toISOString(),
      correlationId: 'mock-correlation-id',
    });
  }),

  http.get('*/api/auth/v1/profile', async ({ request }) => {
    return HttpResponse.json({
      success: true,
      data: {
        id: '1',
        firstName: 'John',
        lastName: 'Doe',
        email: 'admin@paymentengine.com',
        tenantId: 'tenant-1',
        businessUnitId: 'bu-1',
        roles: ['ADMIN', 'OPERATOR'],
        permissions: ['READ', 'WRITE', 'DELETE'],
        lastLogin: new Date(Date.now() - 3600000).toISOString(),
        createdAt: new Date(Date.now() - 86400000).toISOString(),
      },
      message: 'Profile retrieved successfully',
      timestamp: new Date().toISOString(),
      correlationId: 'mock-correlation-id',
    });
  }),

  // Operations Management Service
  http.get('*/api/ops/v1/services', async ({ request }) => {
    const services = [
      generateServiceHealth('1', 'Payment Initiation Service', 'UP'),
      generateServiceHealth('2', 'Saga Orchestrator', 'UP'),
      generateServiceHealth('3', 'Analytics Service', 'DEGRADED'),
      generateServiceHealth('4', 'Reconciliation Service', 'UP'),
      generateServiceHealth('5', 'Settlement Service', 'UP'),
    ];
    
    return HttpResponse.json({
      success: true,
      data: services,
      message: 'Services retrieved successfully',
      timestamp: new Date().toISOString(),
      correlationId: 'mock-correlation-id',
    });
  }),

  http.get('*/api/ops/v1/alerts', async ({ request }) => {
    const alerts = [
      {
        id: '1',
        message: 'High error rate detected in Analytics Service',
        severity: 'HIGH',
        timestamp: new Date(Date.now() - 300000).toISOString(),
        serviceId: '3',
        acknowledged: false,
        acknowledgedBy: null,
        acknowledgedAt: null,
      },
      {
        id: '2',
        message: 'Memory usage above 80% in Payment Service',
        severity: 'MEDIUM',
        timestamp: new Date(Date.now() - 600000).toISOString(),
        serviceId: '1',
        acknowledged: true,
        acknowledgedBy: 'admin@paymentengine.com',
        acknowledgedAt: new Date(Date.now() - 300000).toISOString(),
      },
    ];
    
    return HttpResponse.json({
      success: true,
      data: alerts,
      message: 'Alerts retrieved successfully',
      timestamp: new Date().toISOString(),
      correlationId: 'mock-correlation-id',
    });
  }),

  http.get('*/api/ops/v1/services/health', async ({ request }) => {
    const services = [
      generateServiceHealth('1', 'Payment Initiation Service', 'UP'),
      generateServiceHealth('2', 'Saga Orchestrator', 'UP'),
      generateServiceHealth('3', 'Analytics Service', 'DEGRADED'),
      generateServiceHealth('4', 'Reconciliation Service', 'UP'),
      generateServiceHealth('5', 'Operations Management Service', 'UP'),
    ];

    return HttpResponse.json(services);
  }),

  http.get('*/api/ops/v1/services/health/summary', async ({ request }) => {
    return HttpResponse.json({
      success: true,
      data: {
        totalServices: 5,
        healthyServices: 4,
        unhealthyServices: 1,
        overallHealth: 'HEALTHY',
      },
      message: 'Health summary retrieved successfully',
      timestamp: new Date().toISOString(),
      correlationId: 'mock-correlation-id',
    });
  }),

  // Circuit Breakers API
  http.get('*/api/ops/v1/circuit-breakers', async ({ request }) => {
    const circuitBreakers = [
      {
        id: '1',
        serviceName: 'Payment Initiation Service',
        status: 'CLOSED',
        failureCount: 0,
        failureThreshold: 5,
        timeoutDuration: 60000,
        lastFailure: null,
        createdAt: new Date(Date.now() - 86400000).toISOString(),
      },
      {
        id: '2',
        serviceName: 'Analytics Service',
        status: 'OPEN',
        failureCount: 8,
        failureThreshold: 5,
        timeoutDuration: 30000,
        lastFailure: new Date(Date.now() - 300000).toISOString(),
        createdAt: new Date(Date.now() - 172800000).toISOString(),
      },
    ];
    
    return HttpResponse.json({
      success: true,
      data: circuitBreakers,
      message: 'Circuit breakers retrieved successfully',
      timestamp: new Date().toISOString(),
      correlationId: 'mock-correlation-id',
    });
  }),

  // Feature Flags API
  http.get('*/api/ops/v1/feature-flags', async ({ request }) => {
    const featureFlags = [
      {
        id: '1',
        name: 'enable-mock-auth',
        description: 'Enable mock authentication for development',
        enabled: true,
        environment: 'development',
        createdAt: new Date(Date.now() - 86400000).toISOString(),
        updatedAt: new Date(Date.now() - 3600000).toISOString(),
      },
      {
        id: '2',
        name: 'enable-debug-logging',
        description: 'Enable debug logging for troubleshooting',
        enabled: false,
        environment: 'production',
        createdAt: new Date(Date.now() - 172800000).toISOString(),
        updatedAt: new Date(Date.now() - 7200000).toISOString(),
      },
      {
        id: '3',
        name: 'enable-analytics',
        description: 'Enable analytics data collection',
        enabled: true,
        environment: 'production',
        createdAt: new Date(Date.now() - 259200000).toISOString(),
        updatedAt: new Date(Date.now() - 10800000).toISOString(),
      },
    ];
    
    return HttpResponse.json({
      success: true,
      data: featureFlags,
      message: 'Feature flags retrieved successfully',
      timestamp: new Date().toISOString(),
      correlationId: 'mock-correlation-id',
    });
  }),

  // Pods API
  http.get('*/api/ops/v1/pods', async ({ request }) => {
    const pods = [
      {
        id: '1',
        name: 'payment-service-pod-1',
        serviceName: 'Payment Initiation Service',
        status: 'RUNNING',
        cpuUsage: 45.2,
        memoryUsage: 67.8,
        restartCount: 0,
        createdAt: new Date(Date.now() - 86400000).toISOString(),
        lastRestart: null,
      },
      {
        id: '2',
        name: 'analytics-service-pod-1',
        serviceName: 'Analytics Service',
        status: 'PENDING',
        cpuUsage: 0,
        memoryUsage: 0,
        restartCount: 3,
        createdAt: new Date(Date.now() - 172800000).toISOString(),
        lastRestart: new Date(Date.now() - 1800000).toISOString(),
      },
      {
        id: '3',
        name: 'reconciliation-service-pod-1',
        serviceName: 'Reconciliation Service',
        status: 'RUNNING',
        cpuUsage: 23.1,
        memoryUsage: 45.6,
        restartCount: 1,
        createdAt: new Date(Date.now() - 259200000).toISOString(),
        lastRestart: new Date(Date.now() - 3600000).toISOString(),
      },
    ];
    
    return HttpResponse.json({
      success: true,
      data: pods,
      message: 'Pods retrieved successfully',
      timestamp: new Date().toISOString(),
      correlationId: 'mock-correlation-id',
    });
  }),

  // Payment Initiation Service APIs
  http.get('*/api/payments/v1/search', async ({ request }) => {
    const url = new URL(request.url);
    const status = url.searchParams.get('status') || 'ALL';
    const page = parseInt(url.searchParams.get('page') || '0');
    const size = parseInt(url.searchParams.get('size') || '20');
    
    const payments = [
      {
        id: 'PAY-001',
        amount: 1500.00,
        currency: 'USD',
        status: 'FAILED',
        sourceAccount: 'ACC-12345',
        destinationAccount: 'ACC-67890',
        reference: 'REF-001',
        createdAt: new Date(Date.now() - 3600000).toISOString(),
        completedAt: null,
        processingTime: 2500,
        failureReason: 'Insufficient funds',
        method: 'BANK_TRANSFER',
        description: 'Payment to supplier',
        updatedAt: new Date(Date.now() - 3600000).toISOString(),
        tenantId: 'TENANT-001',
        businessUnitId: 'BU-001',
        correlationId: 'CORR-001',
      },
      {
        id: 'PAY-002',
        amount: 2500.00,
        currency: 'USD',
        status: 'FAILED',
        sourceAccount: 'ACC-23456',
        destinationAccount: 'ACC-78901',
        reference: 'REF-002',
        createdAt: new Date(Date.now() - 7200000).toISOString(),
        completedAt: null,
        processingTime: 1800,
        failureReason: 'Invalid account number',
        method: 'BANK_TRANSFER',
        description: 'Payment to vendor',
        updatedAt: new Date(Date.now() - 7200000).toISOString(),
        tenantId: 'TENANT-001',
        businessUnitId: 'BU-001',
        correlationId: 'CORR-002',
      },
      {
        id: 'PAY-003',
        amount: 500.00,
        currency: 'USD',
        status: 'COMPLETED',
        sourceAccount: 'ACC-34567',
        destinationAccount: 'ACC-89012',
        reference: 'REF-003',
        createdAt: new Date(Date.now() - 10800000).toISOString(),
        completedAt: new Date(Date.now() - 10750000).toISOString(),
        processingTime: 1200,
        failureReason: null,
        method: 'BANK_TRANSFER',
        description: 'Payment to contractor',
        updatedAt: new Date(Date.now() - 10750000).toISOString(),
        tenantId: 'TENANT-001',
        businessUnitId: 'BU-001',
        correlationId: 'CORR-003',
      },
    ].filter(payment => status === 'ALL' || payment.status === status);
    
    const startIndex = page * size;
    const endIndex = startIndex + size;
    const paginatedPayments = payments.slice(startIndex, endIndex);
    
    return HttpResponse.json({
      success: true,
      data: {
        content: paginatedPayments,
        page: page,
        size: size,
        totalElements: payments.length,
        totalPages: Math.ceil(payments.length / size),
        first: page === 0,
        last: endIndex >= payments.length,
        numberOfElements: paginatedPayments.length,
      },
      message: 'Payments retrieved successfully',
      timestamp: new Date().toISOString(),
      correlationId: 'mock-correlation-id',
    });
  }),

  http.get('*/api/payments/v1/metrics', async ({ request }) => {
    const metrics = {
      totalPayments: 1250,
      successfulPayments: 1100,
      failedPayments: 150,
      successRate: 88.0,
      averageProcessingTime: 1500,
      totalVolume: 2500000.00,
      currency: 'USD',
      last24Hours: {
        totalPayments: 45,
        successfulPayments: 42,
        failedPayments: 3,
        successRate: 93.3,
        averageProcessingTime: 1200,
        totalVolume: 125000.00,
      },
    };
    
    return HttpResponse.json({
      success: true,
      data: metrics,
      message: 'Payment metrics retrieved successfully',
      timestamp: new Date().toISOString(),
      correlationId: 'mock-correlation-id',
    });
  }),

  // Static assets handler (to prevent 404 errors in console)
  http.get('*/manifest.json', async ({ request }) => {
    return HttpResponse.json({
      name: "Payment Engine Operations Portal",
      short_name: "PE Ops Portal",
      description: "Operations Portal for Payment Engine",
      start_url: "/",
      display: "standalone",
      background_color: "#ffffff",
      theme_color: "#000000",
      icons: [
        {
          src: "favicon.ico",
          sizes: "64x64 32x32 24x24 16x16",
          type: "image/x-icon"
        }
      ]
    });
  }),

  // Logo handler (to prevent 404 errors)
  http.get('*/logo192.png', async ({ request }) => {
    return HttpResponse.text('', {
      headers: {
        'Content-Type': 'image/png',
      },
    });
  }),

  // Route handler for clearing-onboarding (React Router fallback)
  http.get('*/clearing-onboarding', async ({ request }) => {
    return HttpResponse.text('', {
      headers: {
        'Content-Type': 'text/html',
      },
    });
  }),

  // Reconciliation Service APIs
  http.get('*/api/reconciliation/v1/batches', async ({ request }) => {
    const url = new URL(request.url);
    const page = parseInt(url.searchParams.get('page') || '0');
    const size = parseInt(url.searchParams.get('size') || '20');
    
    const batches = [
      {
        id: '1',
        batchId: 'BATCH-001',
        status: 'COMPLETED',
        type: 'DAILY',
        startTime: new Date(Date.now() - 86400000).toISOString(),
        endTime: new Date(Date.now() - 82800000).toISOString(),
        totalTransactions: 1250,
        matchedTransactions: 1200,
        unmatchedTransactions: 50,
        processingTime: 1800000,
        createdAt: new Date(Date.now() - 86400000).toISOString(),
        updatedAt: new Date(Date.now() - 82800000).toISOString(),
        tenantId: 'TENANT-001',
        businessUnitId: 'BU-001',
      },
      {
        id: '2',
        batchId: 'BATCH-002',
        status: 'IN_PROGRESS',
        type: 'HOURLY',
        startTime: new Date(Date.now() - 3600000).toISOString(),
        endTime: null,
        totalTransactions: 450,
        matchedTransactions: 300,
        unmatchedTransactions: 150,
        processingTime: 900000,
        createdAt: new Date(Date.now() - 3600000).toISOString(),
        updatedAt: new Date(Date.now() - 1800000).toISOString(),
        tenantId: 'TENANT-001',
        businessUnitId: 'BU-001',
      },
      {
        id: '3',
        batchId: 'BATCH-003',
        status: 'FAILED',
        type: 'DAILY',
        startTime: new Date(Date.now() - 172800000).toISOString(),
        endTime: new Date(Date.now() - 169200000).toISOString(),
        totalTransactions: 800,
        matchedTransactions: 0,
        unmatchedTransactions: 800,
        processingTime: 0,
        createdAt: new Date(Date.now() - 172800000).toISOString(),
        updatedAt: new Date(Date.now() - 169200000).toISOString(),
        tenantId: 'TENANT-001',
        businessUnitId: 'BU-001',
      },
    ];
    
    const startIndex = page * size;
    const endIndex = startIndex + size;
    const paginatedBatches = batches.slice(startIndex, endIndex);
    
    return HttpResponse.json({
      success: true,
      data: {
        content: paginatedBatches,
        page: page,
        size: size,
        totalElements: batches.length,
        totalPages: Math.ceil(batches.length / size),
        first: page === 0,
        last: endIndex >= batches.length,
        numberOfElements: paginatedBatches.length,
      },
      message: 'Reconciliation batches retrieved successfully',
      timestamp: new Date().toISOString(),
      correlationId: 'mock-correlation-id',
    });
  }),

  http.get('*/api/reconciliation/v1/exceptions', async ({ request }) => {
    const exceptions = [
      {
        id: '1',
        exceptionId: 'EXC-001',
        type: 'MISSING_TRANSACTION',
        severity: 'HIGH',
        description: 'Transaction not found in clearing system',
        status: 'OPEN',
        transactionId: 'TXN-001',
        amount: 1500.00,
        currency: 'USD',
        createdAt: new Date(Date.now() - 3600000).toISOString(),
        resolvedAt: null,
        resolvedBy: null,
        tenantId: 'TENANT-001',
        businessUnitId: 'BU-001',
      },
      {
        id: '2',
        exceptionId: 'EXC-002',
        type: 'AMOUNT_MISMATCH',
        severity: 'MEDIUM',
        description: 'Amount discrepancy between systems',
        status: 'RESOLVED',
        transactionId: 'TXN-002',
        amount: 2500.00,
        currency: 'USD',
        createdAt: new Date(Date.now() - 7200000).toISOString(),
        resolvedAt: new Date(Date.now() - 1800000).toISOString(),
        resolvedBy: 'admin@paymentengine.com',
        tenantId: 'TENANT-001',
        businessUnitId: 'BU-001',
      },
      {
        id: '3',
        exceptionId: 'EXC-003',
        type: 'DUPLICATE_TRANSACTION',
        severity: 'LOW',
        description: 'Duplicate transaction detected',
        status: 'OPEN',
        transactionId: 'TXN-003',
        amount: 500.00,
        currency: 'USD',
        createdAt: new Date(Date.now() - 10800000).toISOString(),
        resolvedAt: null,
        resolvedBy: null,
        tenantId: 'TENANT-001',
        businessUnitId: 'BU-001',
      },
    ];
    
    return HttpResponse.json({
      success: true,
      data: {
        content: exceptions,
        page: 0,
        size: 20,
        totalElements: exceptions.length,
        totalPages: 1,
        first: true,
        last: true,
        numberOfElements: exceptions.length,
      },
      message: 'Reconciliation exceptions retrieved successfully',
      timestamp: new Date().toISOString(),
      correlationId: 'mock-correlation-id',
    });
  }),

  // Tenant Management Service APIs
  http.get('*/tenant/v1/channels', async ({ request }) => {
    const channels = [
      {
        id: '1',
        channelId: 'CH-001',
        name: 'Bank API Channel',
        type: 'BANK_API',
        status: 'ACTIVE',
        endpoint: 'https://api.bank.com/v1',
        authenticationMethod: 'API_KEY',
        rateLimit: 1000,
        timeout: 30000,
        retryCount: 3,
        isActive: true,
        createdAt: new Date(Date.now() - 86400000).toISOString(),
        updatedAt: new Date(Date.now() - 3600000).toISOString(),
        tenantId: 'TENANT-001',
        businessUnitId: 'BU-001',
      },
      {
        id: '2',
        channelId: 'CH-002',
        name: 'Card Network Channel',
        type: 'CARD_NETWORK',
        status: 'INACTIVE',
        endpoint: 'https://api.cardnetwork.com/v2',
        authenticationMethod: 'OAUTH2',
        rateLimit: 500,
        timeout: 45000,
        retryCount: 2,
        isActive: false,
        createdAt: new Date(Date.now() - 172800000).toISOString(),
        updatedAt: new Date(Date.now() - 7200000).toISOString(),
        tenantId: 'TENANT-001',
        businessUnitId: 'BU-001',
      },
      {
        id: '3',
        channelId: 'CH-003',
        name: 'Digital Wallet Channel',
        type: 'DIGITAL_WALLET',
        status: 'ACTIVE',
        endpoint: 'https://api.wallet.com/v1',
        authenticationMethod: 'JWT',
        rateLimit: 2000,
        timeout: 20000,
        retryCount: 5,
        isActive: true,
        createdAt: new Date(Date.now() - 259200000).toISOString(),
        updatedAt: new Date(Date.now() - 10800000).toISOString(),
        tenantId: 'TENANT-001',
        businessUnitId: 'BU-001',
      },
    ];
    
    return HttpResponse.json({
      success: true,
      data: channels,
      message: 'Channels retrieved successfully',
      timestamp: new Date().toISOString(),
      correlationId: 'mock-correlation-id',
    });
  }),

        // Create Channel API
        http.post('*/tenant/v1/channels', async ({ request }) => {
          const channelData = await request.json() as any;
          
          // Generate a new channel ID
          const newChannelId = `CH-${String(Date.now()).slice(-6)}`;
          
          const newChannel = {
            id: String(Date.now()),
            channelId: newChannelId,
            name: channelData?.name || '',
            type: channelData?.type || 'BANK_API',
            status: 'PENDING',
            endpoint: channelData?.endpoint || '',
            description: channelData?.description || '',
            authentication: channelData?.authentication || 'API_KEY',
            rateLimit: channelData?.rateLimit || 1000,
            webhookUrl: channelData?.webhookUrl || '',
            enableLogging: channelData?.enableLogging || true,
            enableMonitoring: channelData?.enableMonitoring || true,
            timeout: 30000,
            retryCount: 3,
            isActive: false,
            createdAt: new Date().toISOString(),
            updatedAt: new Date().toISOString(),
            tenantId: channelData?.tenantId || 'TENANT-001',
            businessUnitId: channelData?.businessUnitId || 'BU-001',
          };
    
    return HttpResponse.json({
      success: true,
      data: newChannel,
      message: 'Channel created successfully',
      timestamp: new Date().toISOString(),
      correlationId: 'mock-correlation-id',
    });
  }),

  // Clearing Systems API
  http.get('*/tenant/v1/clearing-systems', async ({ request }) => {
    const clearingSystems = [
      {
        id: '1',
        systemId: 'CS-001',
        name: 'SAMOS Clearing System',
        type: 'DOMESTIC',
        status: 'ACTIVE',
        endpoint: 'https://api.samos.com/v1',
        authenticationMethod: 'API_KEY',
        supportedCurrencies: ['USD', 'EUR'],
        processingTime: 30000,
        retryCount: 3,
        isActive: true,
        createdAt: new Date(Date.now() - 86400000).toISOString(),
        updatedAt: new Date(Date.now() - 3600000).toISOString(),
        tenantId: 'TENANT-001',
        businessUnitId: 'BU-001',
      },
      {
        id: '2',
        systemId: 'CS-002',
        name: 'BankservAfrica Clearing',
        type: 'DOMESTIC',
        status: 'ACTIVE',
        endpoint: 'https://api.bankservafrica.com/v2',
        authenticationMethod: 'OAUTH2',
        supportedCurrencies: ['USD', 'ZAR'],
        processingTime: 45000,
        retryCount: 2,
        isActive: true,
        createdAt: new Date(Date.now() - 172800000).toISOString(),
        updatedAt: new Date(Date.now() - 7200000).toISOString(),
        tenantId: 'TENANT-001',
        businessUnitId: 'BU-001',
      },
      {
        id: '3',
        systemId: 'CS-003',
        name: 'RTC Clearing System',
        type: 'REAL_TIME',
        status: 'INACTIVE',
        endpoint: 'https://api.rtc.com/v1',
        authenticationMethod: 'JWT',
        supportedCurrencies: ['USD'],
        processingTime: 15000,
        retryCount: 5,
        isActive: false,
        createdAt: new Date(Date.now() - 259200000).toISOString(),
        updatedAt: new Date(Date.now() - 10800000).toISOString(),
        tenantId: 'TENANT-001',
        businessUnitId: 'BU-001',
      },
      {
        id: '4',
        systemId: 'CS-004',
        name: 'PayShap Clearing',
        type: 'INSTANT',
        status: 'ACTIVE',
        endpoint: 'https://api.payshap.com/v1',
        authenticationMethod: 'API_KEY',
        supportedCurrencies: ['USD', 'EUR', 'GBP'],
        processingTime: 5000,
        retryCount: 3,
        isActive: true,
        createdAt: new Date(Date.now() - 345600000).toISOString(),
        updatedAt: new Date(Date.now() - 14400000).toISOString(),
        tenantId: 'TENANT-001',
        businessUnitId: 'BU-001',
      },
      {
        id: '5',
        systemId: 'CS-005',
        name: 'SWIFT Network',
        type: 'INTERNATIONAL',
        status: 'ACTIVE',
        endpoint: 'https://api.swift.com/v1',
        authenticationMethod: 'CERTIFICATE',
        supportedCurrencies: ['USD', 'EUR', 'GBP', 'JPY', 'CHF'],
        processingTime: 120000,
        retryCount: 2,
        isActive: true,
        createdAt: new Date(Date.now() - 432000000).toISOString(),
        updatedAt: new Date(Date.now() - 18000000).toISOString(),
        tenantId: 'TENANT-001',
        businessUnitId: 'BU-001',
      },
    ];
    
    return HttpResponse.json({
      success: true,
      data: clearingSystems,
      message: 'Clearing systems retrieved successfully',
      timestamp: new Date().toISOString(),
      correlationId: 'mock-correlation-id',
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