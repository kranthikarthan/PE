export type ClearingSystemProcessingMode = 'SYNCHRONOUS' | 'ASYNCHRONOUS' | 'BATCH';
export type ClearingSystemAuthenticationType = 'NONE' | 'API_KEY' | 'JWT' | 'OAUTH2' | 'MTLS';
export type EndpointType = 'SYNC' | 'ASYNC' | 'POLLING' | 'WEBHOOK';
export type HttpMethod = 'GET' | 'POST' | 'PUT' | 'DELETE' | 'PATCH';

export interface ClearingSystemEndpoint {
  id: string;
  clearingSystemId: string;
  name: string;
  endpointType: EndpointType;
  messageType: string; // pacs008, pacs002, pain001, pain002
  url: string;
  httpMethod: HttpMethod;
  timeoutMs: number;
  retryAttempts: number;
  authenticationType: ClearingSystemAuthenticationType;
  authenticationConfig: Record<string, string>;
  defaultHeaders: Record<string, string>;
  isActive: boolean;
  priority: number;
  description?: string;
  createdAt: string;
  updatedAt: string;
}

export interface ClearingSystem {
  id: string;
  code: string;
  name: string;
  description?: string;
  countryCode?: string;
  currency?: string;
  isActive: boolean;
  processingMode: ClearingSystemProcessingMode;
  timeoutSeconds: number;
  endpointUrl: string;
  authenticationType: ClearingSystemAuthenticationType;
  authenticationConfig: Record<string, string>;
  supportedMessageTypes: Record<string, string>;
  supportedPaymentTypes: Record<string, string>;
  supportedLocalInstruments: Record<string, string>;
  endpoints: ClearingSystemEndpoint[];
  createdAt: string;
  updatedAt: string;
}

export interface TenantClearingSystemMapping {
  id: string;
  tenantId: string;
  paymentType: string;
  localInstrumentCode?: string;
  clearingSystemCode: string;
  clearingSystemName?: string;
  priority: number;
  isActive: boolean;
  description?: string;
  createdAt: string;
  updatedAt: string;
}

export interface ClearingSystemForm extends Omit<ClearingSystem, 'id' | 'createdAt' | 'updatedAt' | 'endpoints'> {
  endpoints: Omit<ClearingSystemEndpoint, 'id' | 'clearingSystemId' | 'createdAt' | 'updatedAt'>[];
}

export interface TenantMappingForm extends Omit<TenantClearingSystemMapping, 'id' | 'createdAt' | 'updatedAt' | 'clearingSystemName'> {}

export interface ClearingSystemTestRequest {
  clearingSystemId: string;
  endpointId: string;
  messageType: string;
  messagePayload: any;
  expectedStatus?: number;
  expectedContent?: string;
}

export interface ClearingSystemTestResponse {
  success: boolean;
  status: number;
  responseBody: any;
  headers: Record<string, string>;
  errorMessage?: string;
  processingTimeMs: number;
  timestamp: string;
}

export interface ClearingSystemRoute {
  tenantId: string;
  paymentType: string;
  localInstrumentCode?: string;
  clearingSystemCode: string;
  clearingSystemName: string;
  schemeConfigurationId: string;
  endpointUrl: string;
  authenticationType: ClearingSystemAuthenticationType;
  isActive: boolean;
  routingPriority: number;
  timestamp: string;
}

export interface ClearingSystemSummary {
  id: string;
  code: string;
  name: string;
  description?: string;
  countryCode?: string;
  currency?: string;
  isActive: boolean;
  processingMode: ClearingSystemProcessingMode;
  timeoutSeconds: number;
  endpointUrl: string;
  authenticationType: ClearingSystemAuthenticationType;
  tenantMappingCount: number;
  endpointCount: number;
  createdAt: string;
  updatedAt: string;
}

export interface ClearingSystemStats {
  totalClearingSystems: number;
  activeClearingSystems: number;
  totalEndpoints: number;
  activeEndpoints: number;
  totalTenantMappings: number;
  activeTenantMappings: number;
  clearingSystemsByCountry: Record<string, number>;
  clearingSystemsByCurrency: Record<string, number>;
  clearingSystemsByProcessingMode: Record<string, number>;
}