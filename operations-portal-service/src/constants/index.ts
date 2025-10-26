/**
 * Application Constants
 * 
 * Centralized constants for the application including API endpoints,
 * configuration values, and application settings.
 */

// API Endpoints
export const API_ENDPOINTS = {
  // Payment Initiation Service
  PAYMENTS: {
    BASE: '/api/payments/v1',
    INITIATE: '/api/payments/v1/initiate',
    GET_BY_ID: (id: string) => `/api/payments/v1/${id}`,
    SEARCH: '/api/payments/v1/search',
    REPAIR: (id: string) => `/api/payments/v1/${id}/repair`,
    CANCEL: (id: string) => `/api/payments/v1/${id}/cancel`,
    RETRY: (id: string) => `/api/payments/v1/${id}/retry`,
    METRICS: '/api/payments/v1/metrics',
  },
  
  // Operations Management Service
  OPERATIONS: {
    BASE: '/api/ops/v1',
    SERVICES: '/api/ops/v1/services',
    SERVICE_HEALTH: (service: string) => `/api/ops/v1/services/${service}`,
    SERVICE_METRICS: (service: string) => `/api/ops/v1/services/${service}/metrics`,
    SERVICE_CONTROL: (service: string) => `/api/ops/v1/services/${service}/control`,
    CIRCUIT_BREAKERS: '/api/ops/v1/circuit-breakers',
    CIRCUIT_BREAKER_CONTROL: (service: string) => `/api/ops/v1/circuit-breakers/${service}`,
    FEATURE_FLAGS: '/api/ops/v1/feature-flags',
    FEATURE_FLAG_TOGGLE: (flag: string) => `/api/ops/v1/feature-flags/${flag}/toggle`,
    PODS: '/api/ops/v1/pods',
    POD_CONTROL: (pod: string) => `/api/ops/v1/pods/${pod}/control`,
    POD_LOGS: (pod: string) => `/api/ops/v1/pods/${pod}/logs`,
  },
  
  // Transaction Processing Service
  TRANSACTIONS: {
    BASE: '/api/transactions/v1',
    SEARCH: '/api/transactions/v1/search',
    GET_BY_ID: (id: string) => `/api/transactions/v1/${id}`,
    EXPORT: '/api/transactions/v1/export',
    METRICS: '/api/transactions/v1/metrics',
  },
  
  // Reconciliation Service
  RECONCILIATION: {
    BASE: '/api/reconciliation/v1',
    BATCHES: '/api/reconciliation/v1/batches',
    BATCH_DETAILS: (id: string) => `/api/reconciliation/v1/batches/${id}`,
    METRICS: '/api/reconciliation/v1/metrics',
    REPORTS: '/api/reconciliation/v1/reports',
  },
  
  // Authentication Service
  AUTH: {
    BASE: '/api/auth/v1',
    LOGIN: '/api/auth/v1/login',
    LOGOUT: '/api/auth/v1/logout',
    REFRESH: '/api/auth/v1/refresh',
    PROFILE: '/api/auth/v1/profile',
    PERMISSIONS: '/api/auth/v1/permissions',
  },
  
  // Channel Management
  CHANNELS: {
    BASE: '/api/channels/v1',
    LIST: '/api/channels/v1',
    CREATE: '/api/channels/v1',
    GET_BY_ID: (id: string) => `/api/channels/v1/${id}`,
    UPDATE: (id: string) => `/api/channels/v1/${id}`,
    DELETE: (id: string) => `/api/channels/v1/${id}`,
    TEST: (id: string) => `/api/channels/v1/${id}/test`,
  },
  
  // Clearing Systems
  CLEARING_SYSTEMS: {
    BASE: '/api/clearing-systems/v1',
    LIST: '/api/clearing-systems/v1',
    CREATE: '/api/clearing-systems/v1',
    GET_BY_ID: (id: string) => `/api/clearing-systems/v1/${id}`,
    UPDATE: (id: string) => `/api/clearing-systems/v1/${id}`,
    DELETE: (id: string) => `/api/clearing-systems/v1/${id}`,
    TEST: (id: string) => `/api/clearing-systems/v1/${id}/test`,
  },
} as const;

// Application Configuration
export const APP_CONFIG = {
  NAME: 'Payments Engine Operations Portal',
  VERSION: '1.0.0',
  DESCRIPTION: 'Operations management portal for the Payments Engine platform',
  
  // Pagination
  DEFAULT_PAGE_SIZE: 20,
  MAX_PAGE_SIZE: 100,
  
  // Timeouts
  API_TIMEOUT: 30000,
  REQUEST_TIMEOUT: 10000,
  
  // Retry Configuration
  MAX_RETRY_ATTEMPTS: 3,
  RETRY_DELAY: 1000,
  
  // Refresh Intervals
  DASHBOARD_REFRESH_INTERVAL: 30000, // 30 seconds
  SERVICE_HEALTH_REFRESH_INTERVAL: 60000, // 1 minute
  METRICS_REFRESH_INTERVAL: 300000, // 5 minutes
} as const;

// UI Configuration
export const UI_CONFIG = {
  // Theme
  THEME: {
    PRIMARY_COLOR: '#1976d2',
    SECONDARY_COLOR: '#dc004e',
    SUCCESS_COLOR: '#4caf50',
    WARNING_COLOR: '#ff9800',
    ERROR_COLOR: '#f44336',
    INFO_COLOR: '#2196f3',
  },
  
  // Layout
  LAYOUT: {
    DRAWER_WIDTH: 240,
    HEADER_HEIGHT: 64,
    FOOTER_HEIGHT: 48,
  },
  
  // Animation
  ANIMATION: {
    DURATION_SHORT: 150,
    DURATION_MEDIUM: 300,
    DURATION_LONG: 500,
  },
  
  // Breakpoints
  BREAKPOINTS: {
    XS: 0,
    SM: 600,
    MD: 900,
    LG: 1200,
    XL: 1536,
  },
} as const;

// Status Colors
export const STATUS_COLORS = {
  SUCCESS: '#4caf50',
  WARNING: '#ff9800',
  ERROR: '#f44336',
  INFO: '#2196f3',
  PENDING: '#9e9e9e',
  PROCESSING: '#2196f3',
  COMPLETED: '#4caf50',
  FAILED: '#f44336',
  CANCELLED: '#9e9e9e',
  UP: '#4caf50',
  DOWN: '#f44336',
  DEGRADED: '#ff9800',
  UNKNOWN: '#9e9e9e',
} as const;

// Error Messages
export const ERROR_MESSAGES = {
  NETWORK_ERROR: 'Network connection failed. Please check your internet connection.',
  TIMEOUT_ERROR: 'Request timed out. Please try again.',
  UNAUTHORIZED: 'You are not authorized to perform this action.',
  FORBIDDEN: 'Access denied. Insufficient permissions.',
  NOT_FOUND: 'The requested resource was not found.',
  SERVER_ERROR: 'An internal server error occurred. Please try again later.',
  VALIDATION_ERROR: 'Please check your input and try again.',
  UNKNOWN_ERROR: 'An unexpected error occurred. Please try again.',
} as const;

// Success Messages
export const SUCCESS_MESSAGES = {
  PAYMENT_CREATED: 'Payment initiated successfully.',
  PAYMENT_UPDATED: 'Payment updated successfully.',
  PAYMENT_DELETED: 'Payment deleted successfully.',
  SERVICE_RESTARTED: 'Service restarted successfully.',
  FEATURE_FLAG_TOGGLED: 'Feature flag updated successfully.',
  EXPORT_COMPLETED: 'Export completed successfully.',
  SETTINGS_SAVED: 'Settings saved successfully.',
} as const;

// Validation Rules
export const VALIDATION_RULES = {
  // Payment
  PAYMENT_AMOUNT_MIN: 0.01,
  PAYMENT_AMOUNT_MAX: 1000000,
  REFERENCE_MAX_LENGTH: 50,
  DESCRIPTION_MAX_LENGTH: 255,
  
  // User
  USERNAME_MIN_LENGTH: 3,
  USERNAME_MAX_LENGTH: 50,
  PASSWORD_MIN_LENGTH: 8,
  EMAIL_MAX_LENGTH: 255,
  
  // Channel
  CHANNEL_NAME_MAX_LENGTH: 100,
  ENDPOINT_MAX_LENGTH: 500,
  
  // Search
  SEARCH_QUERY_MIN_LENGTH: 2,
  SEARCH_QUERY_MAX_LENGTH: 100,
} as const;

// Date Formats
export const DATE_FORMATS = {
  DISPLAY: 'MMM dd, yyyy HH:mm:ss',
  API: 'yyyy-MM-dd\'T\'HH:mm:ss.SSS\'Z\'',
  DATE_ONLY: 'yyyy-MM-dd',
  TIME_ONLY: 'HH:mm:ss',
  RELATIVE: 'relative', // For relative time display
} as const;

// Currency Configuration
export const CURRENCY_CONFIG = {
  DEFAULT: 'USD',
  SUPPORTED: ['USD', 'EUR', 'GBP', 'ZAR', 'BTC', 'ETH'],
  PRECISION: 2,
  SYMBOLS: {
    USD: '$',
    EUR: '€',
    GBP: '£',
    ZAR: 'R',
    BTC: '₿',
    ETH: 'Ξ',
  },
} as const;

// Feature Flags
export const FEATURE_FLAGS = {
  ENABLE_MOCK_AUTH: 'enable-mock-auth',
  ENABLE_DEBUG_LOGGING: 'enable-debug-logging',
  ENABLE_MSW: 'enable-msw',
  ENABLE_ANALYTICS: 'enable-analytics',
  ENABLE_NOTIFICATIONS: 'enable-notifications',
  ENABLE_EXPORT: 'enable-export',
  ENABLE_BULK_OPERATIONS: 'enable-bulk-operations',
} as const;

// Local Storage Keys
export const STORAGE_KEYS = {
  AUTH_TOKEN: 'payment_engine_token',
  REFRESH_TOKEN: 'payment_engine_refresh_token',
  USER_PROFILE: 'user_profile',
  TENANT_ID: 'tenant_id',
  BUSINESS_UNIT_ID: 'business_unit_id',
  THEME: 'theme_preference',
  LANGUAGE: 'language_preference',
  DASHBOARD_SETTINGS: 'dashboard_settings',
} as const;

// HTTP Status Codes
export const HTTP_STATUS = {
  OK: 200,
  CREATED: 201,
  NO_CONTENT: 204,
  BAD_REQUEST: 400,
  UNAUTHORIZED: 401,
  FORBIDDEN: 403,
  NOT_FOUND: 404,
  CONFLICT: 409,
  UNPROCESSABLE_ENTITY: 422,
  TOO_MANY_REQUESTS: 429,
  INTERNAL_SERVER_ERROR: 500,
  BAD_GATEWAY: 502,
  SERVICE_UNAVAILABLE: 503,
  GATEWAY_TIMEOUT: 504,
} as const;
