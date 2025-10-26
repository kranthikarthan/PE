/**
 * Environment Configuration
 * 
 * Centralized configuration for all environment variables and API endpoints.
 * Provides type-safe access to configuration values.
 */

export interface EnvironmentConfig {
  // Environment
  env: 'development' | 'production' | 'test';
  
  // API Base URLs
  apiBaseUrl: string;
  operationsApiUrl: string;
  authApiUrl: string;
  analyticsApiUrl: string;
  auditApiUrl: string;
  batchProcessingApiUrl: string;
  metricsAggregationApiUrl: string;
  notificationApiUrl: string;
  reconciliationApiUrl: string;
  settlementApiUrl: string;
  tenantManagementApiUrl: string;
  webBffApiUrl: string;
  accountAdapterApiUrl: string;
  bankservafricaAdapterApiUrl: string;
  payshapAdapterApiUrl: string;
  rtcAdapterApiUrl: string;
  samosAdapterApiUrl: string;
  swiftAdapterApiUrl: string;
  routingApiUrl: string;
  sagaOrchestratorApiUrl: string;
  transactionProcessingApiUrl: string;
  validationApiUrl: string;
  
  // Authentication
  authEnabled: boolean;
  authTokenKey: string;
  authRefreshTokenKey: string;
  
  // Feature Flags
  enableMockAuth: boolean;
  enableDebugLogging: boolean;
  enableMsw: boolean;
  
  // API Configuration
  apiTimeout: number;
  apiRetryAttempts: number;
  apiRetryDelay: number;
  
  // Application Configuration
  appName: string;
  appVersion: string;
  defaultTenantId: string;
  defaultBusinessUnitId: string;
}

/**
 * Get environment variable with fallback
 */
function getEnvVar(key: string, defaultValue: string = ''): string {
  return process.env[key] || defaultValue;
}

/**
 * Get boolean environment variable
 */
function getBooleanEnvVar(key: string, defaultValue: boolean = false): boolean {
  const value = process.env[key];
  if (value === undefined) return defaultValue;
  return value.toLowerCase() === 'true';
}

/**
 * Get number environment variable
 */
function getNumberEnvVar(key: string, defaultValue: number): number {
  const value = process.env[key];
  if (value === undefined) return defaultValue;
  const parsed = parseInt(value, 10);
  return isNaN(parsed) ? defaultValue : parsed;
}

/**
 * Environment configuration object
 */
export const config: EnvironmentConfig = {
  // Environment
  env: (process.env.REACT_APP_ENV as any) || 'development',
  
  // API Base URLs
  apiBaseUrl: getEnvVar('REACT_APP_API_BASE_URL', 'http://localhost:8080'),
  operationsApiUrl: getEnvVar('REACT_APP_OPERATIONS_API_URL', 'http://localhost:8021'),
  authApiUrl: getEnvVar('REACT_APP_AUTH_API_URL', 'http://localhost:8001'),
  analyticsApiUrl: getEnvVar('REACT_APP_ANALYTICS_API_URL', 'http://localhost:8002'),
  auditApiUrl: getEnvVar('REACT_APP_AUDIT_API_URL', 'http://localhost:8003'),
  batchProcessingApiUrl: getEnvVar('REACT_APP_BATCH_PROCESSING_API_URL', 'http://localhost:8004'),
  metricsAggregationApiUrl: getEnvVar('REACT_APP_METRICS_AGGREGATION_API_URL', 'http://localhost:8005'),
  notificationApiUrl: getEnvVar('REACT_APP_NOTIFICATION_API_URL', 'http://localhost:8006'),
  reconciliationApiUrl: getEnvVar('REACT_APP_RECONCILIATION_API_URL', 'http://localhost:8007'),
  settlementApiUrl: getEnvVar('REACT_APP_SETTLEMENT_API_URL', 'http://localhost:8008'),
  tenantManagementApiUrl: getEnvVar('REACT_APP_TENANT_MANAGEMENT_API_URL', 'http://localhost:8009'),
  webBffApiUrl: getEnvVar('REACT_APP_WEB_BFF_API_URL', 'http://localhost:8010'),
  accountAdapterApiUrl: getEnvVar('REACT_APP_ACCOUNT_ADAPTER_API_URL', 'http://localhost:8011'),
  bankservafricaAdapterApiUrl: getEnvVar('REACT_APP_BANKSERVAFRICA_ADAPTER_API_URL', 'http://localhost:8012'),
  payshapAdapterApiUrl: getEnvVar('REACT_APP_PAYSHAP_ADAPTER_API_URL', 'http://localhost:8013'),
  rtcAdapterApiUrl: getEnvVar('REACT_APP_RTC_ADAPTER_API_URL', 'http://localhost:8014'),
  samosAdapterApiUrl: getEnvVar('REACT_APP_SAMOS_ADAPTER_API_URL', 'http://localhost:8015'),
  swiftAdapterApiUrl: getEnvVar('REACT_APP_SWIFT_ADAPTER_API_URL', 'http://localhost:8016'),
  routingApiUrl: getEnvVar('REACT_APP_ROUTING_API_URL', 'http://localhost:8017'),
  sagaOrchestratorApiUrl: getEnvVar('REACT_APP_SAGA_ORCHESTRATOR_API_URL', 'http://localhost:8018'),
  transactionProcessingApiUrl: getEnvVar('REACT_APP_TRANSACTION_PROCESSING_API_URL', 'http://localhost:8019'),
  validationApiUrl: getEnvVar('REACT_APP_VALIDATION_API_URL', 'http://localhost:8020'),
  
  // Authentication
  authEnabled: getBooleanEnvVar('REACT_APP_AUTH_ENABLED', true),
  authTokenKey: getEnvVar('REACT_APP_AUTH_TOKEN_KEY', 'payment_engine_token'),
  authRefreshTokenKey: getEnvVar('REACT_APP_AUTH_REFRESH_TOKEN_KEY', 'payment_engine_refresh_token'),
  
  // Feature Flags
  enableMockAuth: getBooleanEnvVar('REACT_APP_ENABLE_MOCK_AUTH', true),
  enableDebugLogging: getBooleanEnvVar('REACT_APP_ENABLE_DEBUG_LOGGING', true),
  enableMsw: getBooleanEnvVar('REACT_APP_ENABLE_MSW', true),
  
  // API Configuration
  apiTimeout: getNumberEnvVar('REACT_APP_API_TIMEOUT', 30000),
  apiRetryAttempts: getNumberEnvVar('REACT_APP_API_RETRY_ATTEMPTS', 3),
  apiRetryDelay: getNumberEnvVar('REACT_APP_API_RETRY_DELAY', 1000),
  
  // Application Configuration
  appName: getEnvVar('REACT_APP_APP_NAME', 'Payments Engine Operations Portal'),
  appVersion: getEnvVar('REACT_APP_APP_VERSION', '1.0.0'),
  defaultTenantId: getEnvVar('REACT_APP_DEFAULT_TENANT_ID', 'TENANT-001'),
  defaultBusinessUnitId: getEnvVar('REACT_APP_DEFAULT_BUSINESS_UNIT_ID', 'BU-001'),
};

/**
 * Validate required configuration
 */
export function validateConfig(): void {
  const requiredFields: (keyof EnvironmentConfig)[] = [
    'apiBaseUrl',
    'operationsApiUrl',
    'authApiUrl',
  ];
  
  const missingFields = requiredFields.filter(field => !config[field]);
  
  if (missingFields.length > 0) {
    throw new Error(`Missing required configuration: ${missingFields.join(', ')}`);
  }
}

/**
 * Get service URL by service name
 */
export function getServiceUrl(serviceName: string): string {
  const serviceMap: Record<string, string> = {
    'payment-initiation': config.apiBaseUrl,
    'operations': config.operationsApiUrl,
    'auth': config.authApiUrl,
    'analytics': config.analyticsApiUrl,
    'audit': config.auditApiUrl,
    'batch-processing': config.batchProcessingApiUrl,
    'metrics-aggregation': config.metricsAggregationApiUrl,
    'notification': config.notificationApiUrl,
    'reconciliation': config.reconciliationApiUrl,
    'settlement': config.settlementApiUrl,
    'tenant-management': config.tenantManagementApiUrl,
    'web-bff': config.webBffApiUrl,
    'account-adapter': config.accountAdapterApiUrl,
    'bankservafrica-adapter': config.bankservafricaAdapterApiUrl,
    'payshap-adapter': config.payshapAdapterApiUrl,
    'rtc-adapter': config.rtcAdapterApiUrl,
    'samos-adapter': config.samosAdapterApiUrl,
    'swift-adapter': config.swiftAdapterApiUrl,
    'routing': config.routingApiUrl,
    'saga-orchestrator': config.sagaOrchestratorApiUrl,
    'transaction-processing': config.transactionProcessingApiUrl,
    'validation': config.validationApiUrl,
  };
  
  const url = serviceMap[serviceName];
  if (!url) {
    throw new Error(`Unknown service: ${serviceName}`);
  }
  
  return url;
}

export default config;
