/**
 * Onboarding Types
 * 
 * TypeScript interfaces for channel and clearing system onboarding.
 */

export enum ChannelType {
  BANK_API = 'BANK_API',
  CARD_NETWORK = 'CARD_NETWORK',
  DIGITAL_WALLET = 'DIGITAL_WALLET',
  MOBILE_MONEY = 'MOBILE_MONEY',
  CRYPTOCURRENCY = 'CRYPTOCURRENCY',
}

export enum ClearingSystemType {
  SAMOS = 'SAMOS',
  BANKSERVAFRICA = 'BANKSERVAFRICA',
  RTC = 'RTC',
  PAYSHAP = 'PAYSHAP',
  SWIFT = 'SWIFT',
}

export enum OnboardingStatus {
  PENDING = 'PENDING',
  IN_PROGRESS = 'IN_PROGRESS',
  COMPLETED = 'COMPLETED',
  FAILED = 'FAILED',
  CANCELLED = 'CANCELLED',
}

export enum AuthenticationMethod {
  OAUTH2 = 'OAUTH2',
  API_KEY = 'API_KEY',
  JWT = 'JWT',
  BASIC = 'BASIC',
  CERTIFICATE = 'CERTIFICATE',
}

export enum MessageFormat {
  ISO20022 = 'ISO20022',
  ISO8583 = 'ISO8583',
  JSON = 'JSON',
  XML = 'XML',
  FIXED_WIDTH = 'FIXED_WIDTH',
}

export interface Channel {
  id: string;
  name: string;
  type: ChannelType;
  status: OnboardingStatus;
  endpoint: string;
  description?: string;
  authentication: AuthenticationMethod;
  rateLimit?: number;
  webhookUrl?: string;
  enableLogging: boolean;
  enableMonitoring: boolean;
  createdAt: string;
  lastUpdated: string;
  tenantId: string;
  businessUnitId: string;
}

export interface ChannelOnboardingRequest {
  name: string;
  type: ChannelType;
  endpoint: string;
  description?: string;
  authentication: AuthenticationMethod;
  rateLimit?: number;
  webhookUrl?: string;
  enableLogging: boolean;
  enableMonitoring: boolean;
  tenantId: string;
  businessUnitId: string;
}

export interface ClearingSystem {
  id: string;
  name: string;
  type: ClearingSystemType;
  status: OnboardingStatus;
  endpoint: string;
  description?: string;
  authentication: AuthenticationMethod;
  messageFormat: MessageFormat;
  rateLimit?: number;
  webhookUrl?: string;
  enableLogging: boolean;
  enableMonitoring: boolean;
  createdAt: string;
  updatedAt?: string;
  lastUpdated: string;
  tenantId: string;
  businessUnitId: string;
}

export interface ClearingSystemOnboardingRequest {
  name: string;
  type: ClearingSystemType;
  endpoint: string;
  description?: string;
  authentication: AuthenticationMethod;
  messageFormat: MessageFormat;
  rateLimit?: number;
  webhookUrl?: string;
  enableLogging: boolean;
  enableMonitoring: boolean;
  tenantId: string;
  businessUnitId: string;
}

export interface OnboardingTestResult {
  testType: 'CONNECTIVITY' | 'AUTHENTICATION' | 'MESSAGE_FORMAT' | 'PERFORMANCE';
  status: 'PASSED' | 'FAILED' | 'WARNING';
  message: string;
  details?: Record<string, any>;
  timestamp: string;
}

export interface OnboardingValidationResult {
  isValid: boolean;
  errors: string[];
  warnings: string[];
  testResults: OnboardingTestResult[];
}

export interface ChannelConfiguration {
  timeout?: number;
  retryAttempts?: number;
  enableLogging?: boolean;
  enableMonitoring?: boolean;
  rateLimit?: number;
  webhookUrl?: string;
}

export interface ClearingSystemConfiguration {
  timeout?: number;
  retryAttempts?: number;
  enableLogging?: boolean;
  enableMonitoring?: boolean;
  rateLimit?: number;
  webhookUrl?: string;
  messageFormat?: MessageFormat;
}

export interface ChannelTestResult {
  success: boolean;
  message: string;
  details?: Record<string, any>;
  timestamp: string;
}

export interface ClearingSystemTestResult {
  success: boolean;
  message: string;
  details?: Record<string, any>;
  timestamp: string;
}

export interface OnboardingStep {
  id: string;
  name: string;
  description: string;
  status: 'PENDING' | 'IN_PROGRESS' | 'COMPLETED' | 'FAILED';
  required: boolean;
  order: number;
  validationResult?: OnboardingValidationResult;
}
