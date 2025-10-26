/**
 * Error types and interfaces for comprehensive error handling
 */

export enum ErrorType {
  NETWORK = 'NETWORK',
  API = 'API',
  VALIDATION = 'VALIDATION',
  AUTHENTICATION = 'AUTHENTICATION',
  AUTHORIZATION = 'AUTHORIZATION',
  TIMEOUT = 'TIMEOUT',
  SERVER = 'SERVER',
  CLIENT = 'CLIENT',
  UNKNOWN = 'UNKNOWN'
}

export enum ErrorSeverity {
  LOW = 'LOW',
  MEDIUM = 'MEDIUM',
  HIGH = 'HIGH',
  CRITICAL = 'CRITICAL'
}

export interface AppError {
  type: ErrorType;
  severity: ErrorSeverity;
  message: string;
  code?: string | number;
  details?: Record<string, any>;
  timestamp: Date;
  source?: string;
  userMessage?: string;
  retryable: boolean;
  retryAfter?: number; // seconds
}

export interface NetworkError extends AppError {
  type: ErrorType.NETWORK;
  status?: number;
  url?: string;
  method?: string;
}

export interface ApiError extends AppError {
  type: ErrorType.API;
  status: number;
  endpoint: string;
  method: string;
  response?: any;
}

export interface ValidationError extends AppError {
  type: ErrorType.VALIDATION;
  field: string;
  value: any;
  rule: string;
}

export interface AuthError extends AppError {
  type: ErrorType.AUTHENTICATION | ErrorType.AUTHORIZATION;
  action?: string;
  resource?: string;
}

export interface TimeoutError extends AppError {
  type: ErrorType.TIMEOUT;
  timeout: number;
  operation: string;
}

export interface ServerError extends AppError {
  type: ErrorType.SERVER;
  status: number;
  endpoint: string;
  traceId?: string;
}

export interface ClientError extends AppError {
  type: ErrorType.CLIENT;
  component: string;
  action: string;
}

export type AnyError = 
  | NetworkError 
  | ApiError 
  | ValidationError 
  | AuthError 
  | TimeoutError 
  | ServerError 
  | ClientError;

export interface ErrorContext {
  userId?: string;
  tenantId?: string;
  sessionId?: string;
  correlationId?: string;
  userAgent?: string;
  url?: string;
  timestamp: Date;
}

export interface ErrorHandlerConfig {
  maxRetries: number;
  retryDelay: number;
  timeout: number;
  enableLogging: boolean;
  enableReporting: boolean;
  enableUserNotifications: boolean;
}
