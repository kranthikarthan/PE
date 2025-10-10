import apiClient from './apiClient';
import {
  ClearingSystemAuthConfigurationRequest,
  ClearingSystemAuthConfigurationResponse,
  PaymentTypeAuthConfigurationRequest,
  PaymentTypeAuthConfigurationResponse,
  DownstreamCallAuthConfigurationRequest,
  DownstreamCallAuthConfigurationResponse,
  ResolvedAuthConfiguration,
  ConfigurationHierarchyInfo,
  DownstreamCallRequest,
  DownstreamCallResponse,
  DownstreamStats,
  TenantAccessValidation,
  ConfigurationTemplate,
  ConfigurationTestResult
} from '../types/multiLevelAuth';

// Clearing System Level API
export const clearingSystemAuthApi = {
  createConfiguration: (data: ClearingSystemAuthConfigurationRequest) =>
    apiClient.post<ClearingSystemAuthConfigurationResponse>('/api/v1/clearing-system-auth-configurations', data),

  getConfigurationById: (id: string) =>
    apiClient.get<ClearingSystemAuthConfigurationResponse>(`/api/v1/clearing-system-auth-configurations/${id}`),

  getConfigurationsByEnvironment: (environment: string) =>
    apiClient.get<ClearingSystemAuthConfigurationResponse[]>(`/api/v1/clearing-system-auth-configurations/environment/${environment}`),

  getActiveConfigurationByEnvironment: (environment: string) =>
    apiClient.get<ClearingSystemAuthConfigurationResponse>(`/api/v1/clearing-system-auth-configurations/environment/${environment}/active`),

  updateConfiguration: (id: string, data: ClearingSystemAuthConfigurationRequest) =>
    apiClient.put<ClearingSystemAuthConfigurationResponse>(`/api/v1/clearing-system-auth-configurations/${id}`, data),

  deleteConfiguration: (id: string) =>
    apiClient.delete(`/api/v1/clearing-system-auth-configurations/${id}`),

  activateConfiguration: (id: string) =>
    apiClient.post<ClearingSystemAuthConfigurationResponse>(`/api/v1/clearing-system-auth-configurations/${id}/activate`),

  deactivateConfiguration: (id: string) =>
    apiClient.post<ClearingSystemAuthConfigurationResponse>(`/api/v1/clearing-system-auth-configurations/${id}/deactivate`),
};

// Payment Type Level API
export const paymentTypeAuthApi = {
  createConfiguration: (data: PaymentTypeAuthConfigurationRequest) =>
    apiClient.post<PaymentTypeAuthConfigurationResponse>('/api/v1/payment-type-auth-configurations', data),

  getConfigurationById: (id: string) =>
    apiClient.get<PaymentTypeAuthConfigurationResponse>(`/api/v1/payment-type-auth-configurations/${id}`),

  getConfigurationsByTenantId: (tenantId: string) =>
    apiClient.get<PaymentTypeAuthConfigurationResponse[]>(`/api/v1/payment-type-auth-configurations/tenant/${tenantId}`),

  getConfigurationsByPaymentType: (paymentType: string) =>
    apiClient.get<PaymentTypeAuthConfigurationResponse[]>(`/api/v1/payment-type-auth-configurations/payment-type/${paymentType}`),

  getConfigurationByTenantAndPaymentType: (tenantId: string, paymentType: string) =>
    apiClient.get<PaymentTypeAuthConfigurationResponse>(`/api/v1/payment-type-auth-configurations/tenant/${tenantId}/payment-type/${paymentType}`),

  updateConfiguration: (id: string, data: PaymentTypeAuthConfigurationRequest) =>
    apiClient.put<PaymentTypeAuthConfigurationResponse>(`/api/v1/payment-type-auth-configurations/${id}`, data),

  deleteConfiguration: (id: string) =>
    apiClient.delete(`/api/v1/payment-type-auth-configurations/${id}`),

  activateConfiguration: (id: string) =>
    apiClient.post<PaymentTypeAuthConfigurationResponse>(`/api/v1/payment-type-auth-configurations/${id}/activate`),

  deactivateConfiguration: (id: string) =>
    apiClient.post<PaymentTypeAuthConfigurationResponse>(`/api/v1/payment-type-auth-configurations/${id}/deactivate`),
};

// Downstream Call Level API
export const downstreamCallAuthApi = {
  createConfiguration: (data: DownstreamCallAuthConfigurationRequest) =>
    apiClient.post<DownstreamCallAuthConfigurationResponse>('/api/v1/downstream-call-auth-configurations', data),

  getConfigurationById: (id: string) =>
    apiClient.get<DownstreamCallAuthConfigurationResponse>(`/api/v1/downstream-call-auth-configurations/${id}`),

  getConfigurationsByTenantId: (tenantId: string) =>
    apiClient.get<DownstreamCallAuthConfigurationResponse[]>(`/api/v1/downstream-call-auth-configurations/tenant/${tenantId}`),

  getConfigurationsByServiceType: (serviceType: string) =>
    apiClient.get<DownstreamCallAuthConfigurationResponse[]>(`/api/v1/downstream-call-auth-configurations/service-type/${serviceType}`),

  getConfigurationByTenantServiceAndEndpoint: (tenantId: string, serviceType: string, endpoint: string) =>
    apiClient.get<DownstreamCallAuthConfigurationResponse>(`/api/v1/downstream-call-auth-configurations/tenant/${tenantId}/service/${serviceType}/endpoint/${endpoint}`),

  updateConfiguration: (id: string, data: DownstreamCallAuthConfigurationRequest) =>
    apiClient.put<DownstreamCallAuthConfigurationResponse>(`/api/v1/downstream-call-auth-configurations/${id}`, data),

  deleteConfiguration: (id: string) =>
    apiClient.delete(`/api/v1/downstream-call-auth-configurations/${id}`),

  activateConfiguration: (id: string) =>
    apiClient.post<DownstreamCallAuthConfigurationResponse>(`/api/v1/downstream-call-auth-configurations/${id}/activate`),

  deactivateConfiguration: (id: string) =>
    apiClient.post<DownstreamCallAuthConfigurationResponse>(`/api/v1/downstream-call-auth-configurations/${id}/deactivate`),
};

// Enhanced Downstream Routing API
export const enhancedDownstreamRoutingApi = {
  callExternalService: (data: DownstreamCallRequest) =>
    apiClient.post<DownstreamCallResponse>('/api/v1/enhanced-downstream/call', data),

  callFraudSystem: (tenantId: string, paymentType: string | undefined, requestBody: Record<string, any>) =>
    apiClient.post<DownstreamCallResponse>(`/api/v1/enhanced-downstream/fraud/${tenantId}`, requestBody, {
      params: paymentType ? { paymentType } : {}
    }),

  callClearingSystem: (tenantId: string, paymentType: string | undefined, requestBody: Record<string, any>) =>
    apiClient.post<DownstreamCallResponse>(`/api/v1/enhanced-downstream/clearing/${tenantId}`, requestBody, {
      params: paymentType ? { paymentType } : {}
    }),

  callBankingSystem: (tenantId: string, paymentType: string | undefined, requestBody: Record<string, any>) =>
    apiClient.post<DownstreamCallResponse>(`/api/v1/enhanced-downstream/banking/${tenantId}`, requestBody, {
      params: paymentType ? { paymentType } : {}
    }),

  callExternalServiceAuto: (tenantId: string, paymentType: string | undefined, requestBody: Record<string, any>) =>
    apiClient.post<DownstreamCallResponse>(`/api/v1/enhanced-downstream/auto/${tenantId}`, requestBody, {
      params: paymentType ? { paymentType } : {}
    }),

  getResolvedConfiguration: (tenantId: string, serviceType: string, endpoint: string, paymentType?: string) =>
    apiClient.get<ResolvedAuthConfiguration>(`/api/v1/enhanced-downstream/config/${tenantId}/${serviceType}/${endpoint}`, {
      params: paymentType ? { paymentType } : {}
    }),

  getDownstreamStats: (tenantId: string, serviceType: string, endpoint: string, paymentType?: string) =>
    apiClient.get<DownstreamStats>(`/api/v1/enhanced-downstream/stats/${tenantId}/${serviceType}/${endpoint}`, {
      params: paymentType ? { paymentType } : {}
    }),

  validateTenantAccess: (tenantId: string, serviceType: string, endpoint: string, paymentType?: string) =>
    apiClient.get<TenantAccessValidation>(`/api/v1/enhanced-downstream/validate/${tenantId}/${serviceType}/${endpoint}`, {
      params: paymentType ? { paymentType } : {}
    }),

  healthCheck: () =>
    apiClient.get<{ status: string; service: string; timestamp: number; features: Record<string, boolean> }>('/api/v1/enhanced-downstream/health'),
};

// Configuration Hierarchy API
export const configurationHierarchyApi = {
  getConfigurationHierarchy: (tenantId: string, serviceType: string, endpoint: string, paymentType?: string) =>
    apiClient.get<ConfigurationHierarchyInfo>(`/api/v1/configuration-hierarchy/${tenantId}/${serviceType}/${endpoint}`, {
      params: paymentType ? { paymentType } : {}
    }),

  getConfigurationLevels: () =>
    apiClient.get<Array<{ level: string; name: string; description: string; priority: number; isActive: boolean }>>('/api/v1/configuration-hierarchy/levels'),

  getConfigurationTemplates: () =>
    apiClient.get<ConfigurationTemplate[]>('/api/v1/configuration-hierarchy/templates'),

  testConfiguration: (tenantId: string, serviceType: string, endpoint: string, paymentType?: string) =>
    apiClient.post<ConfigurationTestResult>(`/api/v1/configuration-hierarchy/test/${tenantId}/${serviceType}/${endpoint}`, {}, {
      params: paymentType ? { paymentType } : {}
    }),
};

// Configuration Management API
export const configurationManagementApi = {
  // Bulk operations
  bulkCreateConfigurations: (configurations: Array<{
    level: 'clearing-system' | 'payment-type' | 'downstream-call';
    data: any;
  }>) =>
    apiClient.post<Array<{ success: boolean; id?: string; error?: string }>>('/api/v1/configuration-management/bulk-create', configurations),

  bulkUpdateConfigurations: (configurations: Array<{
    level: 'clearing-system' | 'payment-type' | 'downstream-call';
    id: string;
    data: any;
  }>) =>
    apiClient.put<Array<{ success: boolean; error?: string }>>('/api/v1/configuration-management/bulk-update', configurations),

  bulkDeleteConfigurations: (configurations: Array<{
    level: 'clearing-system' | 'payment-type' | 'downstream-call';
    id: string;
  }>) =>
    apiClient.delete<Array<{ success: boolean; error?: string }>>('/api/v1/configuration-management/bulk-delete', { data: configurations }),

  // Configuration validation
  validateConfiguration: (level: string, data: any) =>
    apiClient.post<{ isValid: boolean; errors: string[]; warnings: string[] }>(`/api/v1/configuration-management/validate/${level}`, data),

  // Configuration export/import
  exportConfigurations: (tenantId?: string, level?: string) =>
    apiClient.get<Blob>(`/api/v1/configuration-management/export`, {
      params: { tenantId, level },
      responseType: 'blob'
    }),

  importConfigurations: (file: File) => {
    const formData = new FormData();
    formData.append('file', file);
    return apiClient.post<{ success: boolean; imported: number; errors: string[] }>('/api/v1/configuration-management/import', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    });
  },

  // Configuration backup/restore
  backupConfigurations: (tenantId?: string) =>
    apiClient.post<{ backupId: string; timestamp: string }>(`/api/v1/configuration-management/backup`, {}, {
      params: tenantId ? { tenantId } : {}
    }),

  restoreConfigurations: (backupId: string, tenantId?: string) =>
    apiClient.post<{ success: boolean; restored: number }>(`/api/v1/configuration-management/restore/${backupId}`, {}, {
      params: tenantId ? { tenantId } : {}
    }),

  listBackups: (tenantId?: string) =>
    apiClient.get<Array<{ backupId: string; timestamp: string; tenantId?: string; size: number }>>('/api/v1/configuration-management/backups', {
      params: tenantId ? { tenantId } : {}
    }),
};