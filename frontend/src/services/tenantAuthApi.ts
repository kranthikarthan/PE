import { apiClient } from './apiClient';
import { 
  TenantAuthConfiguration, 
  TenantAuthConfigurationRequest, 
  AuthMethod 
} from '../types/tenantAuth';

const API_BASE = '/api/v1/tenant-auth-config';

export const tenantAuthApi = {
  // Create or update tenant authentication configuration
  createOrUpdateConfiguration: async (config: TenantAuthConfigurationRequest): Promise<TenantAuthConfiguration> => {
    const response = await apiClient.post(`${API_BASE}`, config);
    return response.data;
  },

  // Get active authentication configuration for tenant
  getActiveConfiguration: async (tenantId: string): Promise<TenantAuthConfiguration | null> => {
    try {
      const response = await apiClient.get(`${API_BASE}/tenant/${tenantId}/active`);
      return response.data;
    } catch (error: any) {
      if (error.response?.status === 404) {
        return null;
      }
      throw error;
    }
  },

  // Get all authentication configurations for tenant
  getConfigurations: async (tenantId: string): Promise<TenantAuthConfiguration[]> => {
    const response = await apiClient.get(`${API_BASE}/tenant/${tenantId}`);
    return response.data;
  },

  // Get authentication configuration by ID
  getConfigurationById: async (id: string): Promise<TenantAuthConfiguration | null> => {
    try {
      const response = await apiClient.get(`${API_BASE}/${id}`);
      return response.data;
    } catch (error: any) {
      if (error.response?.status === 404) {
        return null;
      }
      throw error;
    }
  },

  // Activate authentication configuration
  activateConfiguration: async (id: string, updatedBy: string): Promise<TenantAuthConfiguration> => {
    const response = await apiClient.post(`${API_BASE}/${id}/activate`, null, {
      params: { updatedBy }
    });
    return response.data;
  },

  // Deactivate authentication configuration
  deactivateConfiguration: async (id: string, updatedBy: string): Promise<TenantAuthConfiguration> => {
    const response = await apiClient.post(`${API_BASE}/${id}/deactivate`, null, {
      params: { updatedBy }
    });
    return response.data;
  },

  // Delete authentication configuration
  deleteConfiguration: async (id: string): Promise<void> => {
    await apiClient.delete(`${API_BASE}/${id}`);
  },

  // Get all active authentication configurations
  getAllActiveConfigurations: async (): Promise<TenantAuthConfiguration[]> => {
    const response = await apiClient.get(`${API_BASE}/active`);
    return response.data;
  },

  // Get configurations by authentication method
  getConfigurationsByAuthMethod: async (authMethod: AuthMethod): Promise<TenantAuthConfiguration[]> => {
    const response = await apiClient.get(`${API_BASE}/method/${authMethod}`);
    return response.data;
  },

  // Get active configurations by authentication method
  getActiveConfigurationsByAuthMethod: async (authMethod: AuthMethod): Promise<TenantAuthConfiguration[]> => {
    const response = await apiClient.get(`${API_BASE}/method/${authMethod}/active`);
    return response.data;
  },

  // Get configurations that include client headers
  getConfigurationsWithClientHeaders: async (): Promise<TenantAuthConfiguration[]> => {
    const response = await apiClient.get(`${API_BASE}/with-client-headers`);
    return response.data;
  },

  // Check if tenant has active authentication configuration
  hasActiveConfiguration: async (tenantId: string): Promise<boolean> => {
    const response = await apiClient.get(`${API_BASE}/tenant/${tenantId}/has-active`);
    return response.data;
  },

  // Get available authentication methods
  getAvailableAuthMethods: async (): Promise<AuthMethod[]> => {
    const response = await apiClient.get(`${API_BASE}/auth-methods`);
    return response.data;
  }
};