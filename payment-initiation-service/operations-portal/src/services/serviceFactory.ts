/**
 * Service Factory
 * 
 * Centralized factory for creating and managing all API service clients.
 * Provides singleton instances and dependency injection.
 */

import { HttpClient, createHttpClient } from './httpClient';
import { config } from '@config/environment';
import { PaymentInitiationService } from './paymentInitiationService';
import { OperationsManagementService } from './operationsManagementService';
import { TransactionProcessingService } from './transactionProcessingService';
import { ReconciliationService } from './reconciliationService';
import { AuthService } from './authService';

/**
 * Service Factory class for managing API clients
 */
export class ServiceFactory {
  private static instance: ServiceFactory;
  private services: Map<string, any> = new Map();
  private httpClients: Map<string, HttpClient> = new Map();

  private constructor() {
    this.initializeHttpClients();
  }

  /**
   * Get singleton instance
   */
  static getInstance(): ServiceFactory {
    if (!ServiceFactory.instance) {
      ServiceFactory.instance = new ServiceFactory();
    }
    return ServiceFactory.instance;
  }

  /**
   * Initialize HTTP clients for all services
   */
  private initializeHttpClients(): void {
    // Main API client
    this.httpClients.set('main', createHttpClient(config.apiBaseUrl));
    
    // Operations management client
    this.httpClients.set('operations', createHttpClient(config.operationsApiUrl));
    
    // Authentication client
    this.httpClients.set('auth', createHttpClient(config.authApiUrl));
    
    // Transaction processing client
    this.httpClients.set('transactions', createHttpClient(config.transactionProcessingApiUrl));
    
    // Reconciliation client
    this.httpClients.set('reconciliation', createHttpClient(config.reconciliationApiUrl));
    
    // Analytics client
    this.httpClients.set('analytics', createHttpClient(config.analyticsApiUrl));
    
    // Audit client
    this.httpClients.set('audit', createHttpClient(config.auditApiUrl));
    
    // Batch processing client
    this.httpClients.set('batch-processing', createHttpClient(config.batchProcessingApiUrl));
    
    // Metrics aggregation client
    this.httpClients.set('metrics-aggregation', createHttpClient(config.metricsAggregationApiUrl));
    
    // Notification client
    this.httpClients.set('notification', createHttpClient(config.notificationApiUrl));
    
    // Settlement client
    this.httpClients.set('settlement', createHttpClient(config.settlementApiUrl));
    
    // Tenant management client
    this.httpClients.set('tenant-management', createHttpClient(config.tenantManagementApiUrl));
    
    // Web BFF client
    this.httpClients.set('web-bff', createHttpClient(config.webBffApiUrl));
    
    // Adapter clients
    this.httpClients.set('account-adapter', createHttpClient(config.accountAdapterApiUrl));
    this.httpClients.set('bankservafrica-adapter', createHttpClient(config.bankservafricaAdapterApiUrl));
    this.httpClients.set('payshap-adapter', createHttpClient(config.payshapAdapterApiUrl));
    this.httpClients.set('rtc-adapter', createHttpClient(config.rtcAdapterApiUrl));
    this.httpClients.set('samos-adapter', createHttpClient(config.samosAdapterApiUrl));
    this.httpClients.set('swift-adapter', createHttpClient(config.swiftAdapterApiUrl));
    
    // Core service clients
    this.httpClients.set('routing', createHttpClient(config.routingApiUrl));
    this.httpClients.set('saga-orchestrator', createHttpClient(config.sagaOrchestratorApiUrl));
    this.httpClients.set('validation', createHttpClient(config.validationApiUrl));
  }

  /**
   * Get HTTP client by service name
   */
  getHttpClient(serviceName: string): HttpClient {
    const client = this.httpClients.get(serviceName);
    if (!client) {
      throw new Error(`HTTP client not found for service: ${serviceName}`);
    }
    return client;
  }

  /**
   * Get Payment Initiation Service
   */
  getPaymentInitiationService(): PaymentInitiationService {
    const key = 'payment-initiation-service';
    if (!this.services.has(key)) {
      const httpClient = this.getHttpClient('main');
      this.services.set(key, new PaymentInitiationService(httpClient));
    }
    return this.services.get(key);
  }

  /**
   * Get Operations Management Service
   */
  getOperationsManagementService(): OperationsManagementService {
    const key = 'operations-management-service';
    if (!this.services.has(key)) {
      const httpClient = this.getHttpClient('operations');
      this.services.set(key, new OperationsManagementService(httpClient));
    }
    return this.services.get(key);
  }

  /**
   * Get Transaction Processing Service
   */
  getTransactionProcessingService(): TransactionProcessingService {
    const key = 'transaction-processing-service';
    if (!this.services.has(key)) {
      const httpClient = this.getHttpClient('transactions');
      this.services.set(key, new TransactionProcessingService(httpClient));
    }
    return this.services.get(key);
  }

  /**
   * Get Reconciliation Service
   */
  getReconciliationService(): ReconciliationService {
    const key = 'reconciliation-service';
    if (!this.services.has(key)) {
      const httpClient = this.getHttpClient('reconciliation');
      this.services.set(key, new ReconciliationService(httpClient));
    }
    return this.services.get(key);
  }

  /**
   * Get Authentication Service
   */
  getAuthService(): AuthService {
    const key = 'auth-service';
    if (!this.services.has(key)) {
      const httpClient = this.getHttpClient('auth');
      this.services.set(key, new AuthService(httpClient));
    }
    return this.services.get(key);
  }

  /**
   * Get service by name (generic method)
   */
  getService<T>(serviceName: string): T {
    const service = this.services.get(serviceName);
    if (!service) {
      throw new Error(`Service not found: ${serviceName}`);
    }
    return service;
  }

  /**
   * Create a new service instance
   */
  createService<T>(
    serviceName: string,
    serviceClass: new (httpClient: HttpClient) => T,
    httpClientName: string
  ): T {
    const httpClient = this.getHttpClient(httpClientName);
    return new serviceClass(httpClient);
  }

  /**
   * Clear all services (useful for testing)
   */
  clearServices(): void {
    this.services.clear();
  }

  /**
   * Get all available service names
   */
  getAvailableServices(): string[] {
    return Array.from(this.services.keys());
  }

  /**
   * Check if service exists
   */
  hasService(serviceName: string): boolean {
    return this.services.has(serviceName);
  }

  /**
   * Remove service from cache
   */
  removeService(serviceName: string): boolean {
    return this.services.delete(serviceName);
  }
}

/**
 * Global service factory instance
 */
export const serviceFactory = ServiceFactory.getInstance();

/**
 * Convenience functions for accessing services
 */
export const getPaymentInitiationService = () => serviceFactory.getPaymentInitiationService();
export const getOperationsManagementService = () => serviceFactory.getOperationsManagementService();
export const getTransactionProcessingService = () => serviceFactory.getTransactionProcessingService();
export const getReconciliationService = () => serviceFactory.getReconciliationService();
export const getAuthService = () => serviceFactory.getAuthService();

/**
 * Service names constants
 */
export const SERVICE_NAMES = {
  PAYMENT_INITIATION: 'payment-initiation-service',
  OPERATIONS_MANAGEMENT: 'operations-management-service',
  TRANSACTION_PROCESSING: 'transaction-processing-service',
  RECONCILIATION: 'reconciliation-service',
  AUTH: 'auth-service',
  ANALYTICS: 'analytics-service',
  AUDIT: 'audit-service',
  BATCH_PROCESSING: 'batch-processing-service',
  METRICS_AGGREGATION: 'metrics-aggregation-service',
  NOTIFICATION: 'notification-service',
  SETTLEMENT: 'settlement-service',
  TENANT_MANAGEMENT: 'tenant-management-service',
  WEB_BFF: 'web-bff-service',
  ACCOUNT_ADAPTER: 'account-adapter-service',
  BANKSERVAFRICA_ADAPTER: 'bankservafrica-adapter-service',
  PAYSHAP_ADAPTER: 'payshap-adapter-service',
  RTC_ADAPTER: 'rtc-adapter-service',
  SAMOS_ADAPTER: 'samos-adapter-service',
  SWIFT_ADAPTER: 'swift-adapter-service',
  ROUTING: 'routing-service',
  SAGA_ORCHESTRATOR: 'saga-orchestrator-service',
  VALIDATION: 'validation-service',
} as const;

export default serviceFactory;
