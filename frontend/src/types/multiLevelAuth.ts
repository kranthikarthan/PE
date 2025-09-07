export type AuthMethod = 'JWT' | 'JWS' | 'OAUTH2' | 'API_KEY' | 'BASIC';
export type JwsAlgorithm = 'HS256' | 'HS384' | 'HS512' | 'RS256' | 'RS384' | 'RS512';
export type Environment = 'dev' | 'staging' | 'prod';
export type PaymentType = 'SEPA' | 'SWIFT' | 'ACH' | 'CARD' | 'CUSTOM';
export type ServiceType = 'fraud' | 'clearing' | 'banking' | 'custom';

// Base authentication configuration interface
export interface BaseAuthConfiguration {
  authMethod: AuthMethod;
  jwtSecret?: string;
  jwtIssuer?: string;
  jwtAudience?: string;
  jwtExpirationSeconds?: number;
  jwsSecret?: string;
  jwsAlgorithm?: JwsAlgorithm;
  jwsIssuer?: string;
  jwsAudience?: string;
  jwsExpirationSeconds?: number;
  oauth2TokenEndpoint?: string;
  oauth2ClientId?: string;
  oauth2ClientSecret?: string;
  oauth2Scope?: string;
  apiKey?: string;
  apiKeyHeaderName?: string;
  basicAuthUsername?: string;
  basicAuthPassword?: string;
  includeClientHeaders?: boolean;
  clientId?: string;
  clientSecret?: string;
  clientIdHeaderName?: string;
  clientSecretHeaderName?: string;
  metadata?: Record<string, string>;
}

// Clearing System Level Configuration
export interface ClearingSystemAuthConfigurationRequest extends BaseAuthConfiguration {
  environment: Environment;
  description?: string;
}

export interface ClearingSystemAuthConfigurationResponse extends BaseAuthConfiguration {
  id: string;
  environment: Environment;
  isActive: boolean;
  description?: string;
  createdAt: string;
  updatedAt: string;
}

// Payment Type Level Configuration
export interface PaymentTypeAuthConfigurationRequest extends BaseAuthConfiguration {
  tenantId: string;
  paymentType: PaymentType;
  clearingSystem?: string;
  routingCode?: string;
  currency?: string;
  isHighValue?: boolean;
  description?: string;
}

export interface PaymentTypeAuthConfigurationResponse extends BaseAuthConfiguration {
  id: string;
  tenantId: string;
  paymentType: PaymentType;
  clearingSystem?: string;
  routingCode?: string;
  currency?: string;
  isHighValue: boolean;
  isActive: boolean;
  description?: string;
  createdAt: string;
  updatedAt: string;
}

// Downstream Call Level Configuration
export interface DownstreamCallAuthConfigurationRequest extends BaseAuthConfiguration {
  tenantId: string;
  serviceType: ServiceType;
  endpoint: string;
  paymentType?: PaymentType;
  targetHost?: string;
  targetPort?: number;
  targetProtocol?: 'HTTP' | 'HTTPS';
  targetPath?: string;
  timeoutSeconds?: number;
  retryAttempts?: number;
  retryDelaySeconds?: number;
  description?: string;
}

export interface DownstreamCallAuthConfigurationResponse extends BaseAuthConfiguration {
  id: string;
  tenantId: string;
  serviceType: ServiceType;
  endpoint: string;
  paymentType?: PaymentType;
  targetHost?: string;
  targetPort?: number;
  targetProtocol?: 'HTTP' | 'HTTPS';
  targetPath?: string;
  timeoutSeconds?: number;
  retryAttempts?: number;
  retryDelaySeconds?: number;
  isActive: boolean;
  description?: string;
  createdAt: string;
  updatedAt: string;
}

// Resolved Configuration (from hierarchy)
export interface ResolvedAuthConfiguration extends BaseAuthConfiguration {
  targetHost?: string;
  targetPort?: number;
  targetProtocol?: 'HTTP' | 'HTTPS';
  targetPath?: string;
  timeoutSeconds?: number;
  retryAttempts?: number;
  retryDelaySeconds?: number;
}

// Configuration Hierarchy Info
export interface ConfigurationHierarchyInfo {
  clearingSystemLevel?: ClearingSystemAuthConfigurationResponse;
  tenantLevel?: any; // TenantAuthConfigurationResponse
  paymentTypeLevel?: PaymentTypeAuthConfigurationResponse;
  downstreamCallLevel?: DownstreamCallAuthConfigurationResponse;
  resolvedConfiguration: ResolvedAuthConfiguration;
}

// API Request/Response Types
export interface DownstreamCallRequest {
  tenantId: string;
  serviceType: ServiceType;
  endpoint: string;
  paymentType?: PaymentType;
  requestBody: Record<string, any>;
  additionalHeaders?: Record<string, string>;
}

export interface DownstreamCallResponse {
  success: boolean;
  data?: any;
  error?: string;
  message?: string;
  tenantId: string;
  serviceType: ServiceType;
  endpoint: string;
  paymentType?: PaymentType;
  timestamp: string;
}

export interface DownstreamStats {
  tenantId: string;
  serviceType: ServiceType;
  endpoint: string;
  paymentType?: PaymentType;
  authMethod?: AuthMethod;
  includeClientHeaders?: boolean;
  targetHost?: string;
  targetPort?: number;
  targetProtocol?: string;
  timeoutSeconds?: number;
  retryAttempts?: number;
  timestamp: number;
  error?: string;
}

export interface TenantAccessValidation {
  tenantId: string;
  serviceType: ServiceType;
  endpoint: string;
  paymentType?: PaymentType;
  hasAccess: boolean;
  timestamp: number;
}

// Configuration Management Types
export interface ConfigurationLevel {
  level: 'clearing-system' | 'tenant' | 'payment-type' | 'downstream-call';
  name: string;
  description: string;
  priority: number;
  isActive: boolean;
}

export interface ConfigurationTemplate {
  id: string;
  name: string;
  description: string;
  level: ConfigurationLevel['level'];
  template: Partial<BaseAuthConfiguration>;
  isDefault: boolean;
}

// Form Types for UI
export interface AuthMethodFormData {
  authMethod: AuthMethod;
  jwtConfig?: {
    secret: string;
    issuer: string;
    audience: string;
    expirationSeconds: number;
  };
  jwsConfig?: {
    secret: string;
    algorithm: JwsAlgorithm;
    issuer: string;
    audience: string;
    expirationSeconds: number;
  };
  oauth2Config?: {
    tokenEndpoint: string;
    clientId: string;
    clientSecret: string;
    scope: string;
  };
  apiKeyConfig?: {
    apiKey: string;
    headerName: string;
  };
  basicAuthConfig?: {
    username: string;
    password: string;
  };
  clientHeadersConfig?: {
    includeClientHeaders: boolean;
    clientId: string;
    clientSecret: string;
    clientIdHeaderName: string;
    clientSecretHeaderName: string;
  };
}

// Configuration Validation Types
export interface ConfigurationValidationResult {
  isValid: boolean;
  errors: string[];
  warnings: string[];
  level: ConfigurationLevel['level'];
  configurationId?: string;
}

export interface ConfigurationTestResult {
  success: boolean;
  response?: any;
  error?: string;
  duration: number;
  timestamp: string;
  configuration: ResolvedAuthConfiguration;
}