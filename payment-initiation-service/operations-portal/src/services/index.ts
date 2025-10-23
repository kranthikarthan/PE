/**
 * Services Index
 * 
 * Centralized exports for all API service clients and utilities.
 */

// HTTP Client
export { HttpClient, createHttpClient, httpClient, operationsClient, authClient } from './httpClient';

// Base API Client
export { BaseApiClient } from './baseApiClient';

// Service Factory
export { 
  ServiceFactory, 
  serviceFactory,
  getPaymentInitiationService,
  getOperationsManagementService,
  getTransactionProcessingService,
  getReconciliationService,
  getAuthService,
  SERVICE_NAMES
} from './serviceFactory';

// Individual Services
export { PaymentInitiationService } from './paymentInitiationService';
export { OperationsManagementService } from './operationsManagementService';
export { TransactionProcessingService } from './transactionProcessingService';
export { ReconciliationService } from './reconciliationService';
export { AuthService } from './authService';

// Re-export service factory as default
export { default } from './serviceFactory';
