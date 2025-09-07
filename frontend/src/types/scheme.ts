// Scheme Interaction Configuration Types

export interface SchemeInteractionConfig {
  id: string;
  name: string;
  description: string;
  isActive: boolean;
  interactionMode: InteractionMode;
  messageFormat: MessageFormat;
  responseMode: ResponseMode;
  timeoutMs: number;
  retryPolicy: RetryPolicy;
  authentication: AuthenticationConfig;
  endpoints: EndpointConfig[];
  createdAt: string;
  updatedAt: string;
  createdBy: string;
  updatedBy: string;
}

export enum InteractionMode {
  SYNCHRONOUS = 'SYNCHRONOUS',
  ASYNCHRONOUS = 'ASYNCHRONOUS',
  HYBRID = 'HYBRID' // Can handle both sync and async based on request
}

export enum MessageFormat {
  JSON = 'JSON',
  XML = 'XML',
  BOTH = 'BOTH' // Support both formats
}

export enum ResponseMode {
  IMMEDIATE = 'IMMEDIATE', // Return response immediately
  WEBHOOK = 'WEBHOOK', // Send response via webhook
  KAFKA_TOPIC = 'KAFKA_TOPIC', // Publish to Kafka topic
  POLLING = 'POLLING' // Client polls for response
}

export interface RetryPolicy {
  maxRetries: number;
  backoffMs: number;
  exponentialBackoff: boolean;
  retryableStatusCodes: number[];
}

export interface AuthenticationConfig {
  type: 'NONE' | 'API_KEY' | 'JWT' | 'OAUTH2' | 'MUTUAL_TLS';
  apiKey?: string;
  jwtSecret?: string;
  oauth2Config?: OAuth2Config;
  certificatePath?: string;
  keyPath?: string;
}

export interface OAuth2Config {
  clientId: string;
  clientSecret: string;
  tokenUrl: string;
  scope: string[];
}

export interface EndpointConfig {
  id: string;
  name: string;
  url: string;
  method: 'GET' | 'POST' | 'PUT' | 'DELETE';
  isActive: boolean;
  timeoutMs: number;
  headers: Record<string, string>;
  supportedMessageTypes: string[];
  priority: number;
}

export interface SchemeMessageRequest {
  messageType: string;
  messageId: string;
  correlationId: string;
  format: MessageFormat;
  interactionMode: InteractionMode;
  payload: any;
  metadata?: Record<string, any>;
}

export interface SchemeMessageResponse {
  messageId: string;
  correlationId: string;
  status: 'SUCCESS' | 'ERROR' | 'PENDING';
  responseCode: string;
  responseMessage: string;
  payload?: any;
  errorDetails?: ErrorDetails;
  processingTimeMs?: number;
  timestamp: string;
}

export interface ErrorDetails {
  errorCode: string;
  errorMessage: string;
  errorCategory: 'VALIDATION' | 'PROCESSING' | 'NETWORK' | 'AUTHENTICATION' | 'TIMEOUT';
  retryable: boolean;
  details?: Record<string, any>;
}

export interface SchemeInteractionStats {
  totalRequests: number;
  successfulRequests: number;
  failedRequests: number;
  averageResponseTime: number;
  successRate: number;
  formatBreakdown: Record<MessageFormat, number>;
  modeBreakdown: Record<InteractionMode, number>;
  lastUpdated: string;
}

export interface SchemeConfigForm {
  name: string;
  description: string;
  isActive: boolean;
  interactionMode: InteractionMode;
  messageFormat: MessageFormat;
  responseMode: ResponseMode;
  timeoutMs: number;
  retryPolicy: RetryPolicy;
  authentication: AuthenticationConfig;
  endpoints: Omit<EndpointConfig, 'id'>[];
}

// ISO 20022 specific scheme configurations
export interface Iso20022SchemeConfig extends SchemeInteractionConfig {
  supportedMessageTypes: Iso20022MessageType[];
  validationRules: ValidationRule[];
  transformationRules: TransformationRule[];
  complianceChecks: ComplianceCheck[];
}

export interface ValidationRule {
  id: string;
  name: string;
  messageType: string;
  field: string;
  rule: string;
  errorMessage: string;
  isActive: boolean;
}

export interface TransformationRule {
  id: string;
  name: string;
  fromFormat: MessageFormat;
  toFormat: MessageFormat;
  messageType: string;
  transformationScript: string;
  isActive: boolean;
}

export interface ComplianceCheck {
  id: string;
  name: string;
  messageType: string;
  checkType: 'AML' | 'KYC' | 'SANCTIONS' | 'REGULATORY';
  isRequired: boolean;
  timeoutMs: number;
}

// Configuration management
export interface SchemeConfigSearchCriteria {
  name?: string;
  isActive?: boolean;
  interactionMode?: InteractionMode;
  messageFormat?: MessageFormat;
  responseMode?: ResponseMode;
  page?: number;
  size?: number;
  sortBy?: string;
  sortDirection?: 'asc' | 'desc';
}

export interface PagedSchemeConfigResponse {
  content: SchemeInteractionConfig[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
}

// Test and validation
export interface SchemeConfigTestRequest {
  configId: string;
  testMessage: SchemeMessageRequest;
  validateOnly?: boolean;
}

export interface SchemeConfigTestResponse {
  success: boolean;
  responseTimeMs: number;
  response?: SchemeMessageResponse;
  error?: ErrorDetails;
  validationResults?: ValidationResult[];
}

export interface ValidationResult {
  field: string;
  valid: boolean;
  errorMessage?: string;
  severity: 'ERROR' | 'WARNING' | 'INFO';
}

// Import existing ISO 20022 types
import { Iso20022MessageType } from './iso20022';