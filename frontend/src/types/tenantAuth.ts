export interface TenantAuthConfiguration {
  id: string;
  tenantId: string;
  authMethod: AuthMethod;
  clientId?: string;
  clientIdHeaderName: string;
  clientSecretHeaderName: string;
  authHeaderName: string;
  authHeaderPrefix: string;
  tokenEndpoint?: string;
  publicKeyEndpoint?: string;
  jwsPublicKey?: string;
  jwsAlgorithm: string;
  jwsIssuer?: string;
  isActive: boolean;
  includeClientHeaders: boolean;
  description?: string;
  createdBy?: string;
  updatedBy?: string;
  createdAt: string;
  updatedAt: string;
}

export interface TenantAuthConfigurationRequest {
  tenantId: string;
  authMethod: AuthMethod;
  clientId?: string;
  clientSecret?: string;
  clientIdHeaderName?: string;
  clientSecretHeaderName?: string;
  authHeaderName?: string;
  authHeaderPrefix?: string;
  tokenEndpoint?: string;
  publicKeyEndpoint?: string;
  jwsPublicKey?: string;
  jwsAlgorithm?: string;
  jwsIssuer?: string;
  includeClientHeaders?: boolean;
  description?: string;
  createdBy?: string;
  updatedBy?: string;
}

export enum AuthMethod {
  JWT = 'JWT',
  JWS = 'JWS',
  OAUTH2 = 'OAUTH2',
  API_KEY = 'API_KEY',
  BASIC = 'BASIC'
}

export interface AuthMethodOption {
  value: AuthMethod;
  label: string;
  description: string;
}

export const AUTH_METHOD_OPTIONS: AuthMethodOption[] = [
  {
    value: AuthMethod.JWT,
    label: 'JSON Web Token (JWT)',
    description: 'Standard JWT tokens with HMAC or RSA signing'
  },
  {
    value: AuthMethod.JWS,
    label: 'JSON Web Signature (JWS)',
    description: 'JWS tokens with enhanced security features'
  },
  {
    value: AuthMethod.OAUTH2,
    label: 'OAuth 2.0',
    description: 'OAuth 2.0 authorization framework'
  },
  {
    value: AuthMethod.API_KEY,
    label: 'API Key',
    description: 'Simple API key authentication'
  },
  {
    value: AuthMethod.BASIC,
    label: 'Basic Authentication',
    description: 'HTTP Basic Authentication'
  }
];

export interface JwsAlgorithmOption {
  value: string;
  label: string;
  description: string;
}

export const JWS_ALGORITHM_OPTIONS: JwsAlgorithmOption[] = [
  {
    value: 'HS256',
    label: 'HMAC SHA-256',
    description: 'HMAC with SHA-256 (symmetric)'
  },
  {
    value: 'HS384',
    label: 'HMAC SHA-384',
    description: 'HMAC with SHA-384 (symmetric)'
  },
  {
    value: 'HS512',
    label: 'HMAC SHA-512',
    description: 'HMAC with SHA-512 (symmetric)'
  },
  {
    value: 'RS256',
    label: 'RSA SHA-256',
    description: 'RSA with SHA-256 (asymmetric)'
  },
  {
    value: 'RS384',
    label: 'RSA SHA-384',
    description: 'RSA with SHA-384 (asymmetric)'
  },
  {
    value: 'RS512',
    label: 'RSA SHA-512',
    description: 'RSA with SHA-512 (asymmetric)'
  }
];

export interface ClientHeaderConfig {
  includeClientHeaders: boolean;
  clientIdHeaderName: string;
  clientSecretHeaderName: string;
}

export interface AuthEndpointConfig {
  tokenEndpoint?: string;
  publicKeyEndpoint?: string;
}

export interface JwsConfig {
  jwsPublicKey?: string;
  jwsAlgorithm: string;
  jwsIssuer?: string;
}