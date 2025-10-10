import axios from 'axios';

const API_BASE_URL = process.env.REACT_APP_CONFIG_SERVICE_URL || 'http://localhost:8080/api/v1/config';

export interface Tenant {
  id: string;
  name: string;
  code: string;
  status: 'ACTIVE' | 'INACTIVE' | 'SUSPENDED';
  contactEmail?: string;
  contactPhone?: string;
  address?: string;
  createdAt: string;
  updatedAt: string;
  createdBy?: string;
  updatedBy?: string;
}

export interface TenantRequest {
  name: string;
  code: string;
  contactEmail?: string;
  contactPhone?: string;
  address?: string;
}

export interface TenantConfiguration {
  id: string;
  tenantId: string;
  configKey: string;
  configValue: string;
  configType: 'STRING' | 'INTEGER' | 'BOOLEAN' | 'JSON' | 'XML' | 'BINARY';
  isEncrypted: boolean;
  isActive: boolean;
  createdAt: string;
  updatedAt: string;
  createdBy?: string;
  updatedBy?: string;
}

export interface TenantConfigurationRequest {
  configKey: string;
  configValue: string;
  configType: 'STRING' | 'INTEGER' | 'BOOLEAN' | 'JSON' | 'XML' | 'BINARY';
  isEncrypted?: boolean;
}

export interface FeatureFlag {
  id: string;
  flagName: string;
  flagDescription?: string;
  flagValue: boolean;
  tenantId?: string;
  environment: 'DEVELOPMENT' | 'TESTING' | 'STAGING' | 'PRODUCTION';
  rolloutPercentage: number;
  targetUsers?: string;
  isActive: boolean;
  createdAt: string;
  updatedAt: string;
  createdBy?: string;
  updatedBy?: string;
}

export interface FeatureFlagRequest {
  flagName: string;
  flagDescription?: string;
  flagValue: boolean;
  tenantId?: string;
  environment: 'DEVELOPMENT' | 'TESTING' | 'STAGING' | 'PRODUCTION';
  rolloutPercentage?: number;
  targetUsers?: string;
}

export interface ConfigurationHistory {
  id: string;
  configType: 'TENANT' | 'TENANT_CONFIG' | 'FEATURE_FLAG' | 'SERVICE_CONFIG' | 'ENVIRONMENT_CONFIG';
  configId: string;
  configKey: string;
  oldValue?: string;
  newValue?: string;
  changeReason?: string;
  changedBy: string;
  changedAt: string;
  createdAt: string;
}

class ConfigApiService {
  private api = axios.create({
    baseURL: API_BASE_URL,
    headers: {
      'Content-Type': 'application/json',
    },
  });

  constructor() {
    // Add request interceptor to include auth token
    this.api.interceptors.request.use(
      (config) => {
        const token = localStorage.getItem('authToken');
        if (token) {
          config.headers.Authorization = `Bearer ${token}`;
        }
        return config;
      },
      (error) => {
        return Promise.reject(error);
      }
    );
  }

  // Tenant Management
  async getTenants(): Promise<Tenant[]> {
    const response = await this.api.get<Tenant[]>('/tenants');
    return response.data;
  }

  async getTenant(id: string): Promise<Tenant> {
    const response = await this.api.get<Tenant>(`/tenants/${id}`);
    return response.data;
  }

  async createTenant(tenantData: TenantRequest): Promise<Tenant> {
    const response = await this.api.post<Tenant>('/tenants', tenantData);
    return response.data;
  }

  async updateTenant(id: string, tenantData: TenantRequest): Promise<Tenant> {
    const response = await this.api.put<Tenant>(`/tenants/${id}`, tenantData);
    return response.data;
  }

  async activateTenant(id: string): Promise<void> {
    await this.api.put(`/tenants/${id}/activate`);
  }

  async deactivateTenant(id: string): Promise<void> {
    await this.api.put(`/tenants/${id}/deactivate`);
  }

  // Tenant Configuration Management
  async getTenantConfigurations(tenantId: string): Promise<TenantConfiguration[]> {
    const response = await this.api.get<TenantConfiguration[]>(`/tenants/${tenantId}/configurations`);
    return response.data;
  }

  async getTenantConfiguration(tenantId: string, configKey: string): Promise<TenantConfiguration> {
    const response = await this.api.get<TenantConfiguration>(`/tenants/${tenantId}/configurations/${configKey}`);
    return response.data;
  }

  async createTenantConfiguration(tenantId: string, configData: TenantConfigurationRequest): Promise<TenantConfiguration> {
    const response = await this.api.post<TenantConfiguration>(`/tenants/${tenantId}/configurations`, configData);
    return response.data;
  }

  async updateTenantConfiguration(tenantId: string, configKey: string, configData: TenantConfigurationRequest): Promise<TenantConfiguration> {
    const response = await this.api.put<TenantConfiguration>(`/tenants/${tenantId}/configurations/${configKey}`, configData);
    return response.data;
  }

  async deleteTenantConfiguration(tenantId: string, configKey: string): Promise<void> {
    await this.api.delete(`/tenants/${tenantId}/configurations/${configKey}`);
  }

  // Feature Flag Management
  async getFeatureFlags(): Promise<FeatureFlag[]> {
    const response = await this.api.get<FeatureFlag[]>('/feature-flags');
    return response.data;
  }

  async getFeatureFlag(flagName: string): Promise<FeatureFlag> {
    const response = await this.api.get<FeatureFlag>(`/feature-flags/${flagName}`);
    return response.data;
  }

  async createFeatureFlag(flagData: FeatureFlagRequest): Promise<FeatureFlag> {
    const response = await this.api.post<FeatureFlag>('/feature-flags', flagData);
    return response.data;
  }

  async updateFeatureFlag(flagName: string, flagData: FeatureFlagRequest): Promise<FeatureFlag> {
    const response = await this.api.put<FeatureFlag>(`/feature-flags/${flagName}`, flagData);
    return response.data;
  }

  async toggleFeatureFlag(flagName: string): Promise<void> {
    await this.api.put(`/feature-flags/${flagName}/toggle`);
  }

  async deleteFeatureFlag(flagName: string): Promise<void> {
    await this.api.delete(`/feature-flags/${flagName}`);
  }

  // Configuration History
  async getConfigurationHistory(): Promise<ConfigurationHistory[]> {
    const response = await this.api.get<ConfigurationHistory[]>('/history');
    return response.data;
  }

  async getConfigurationHistoryByType(configType: string): Promise<ConfigurationHistory[]> {
    const response = await this.api.get<ConfigurationHistory[]>(`/history/${configType}`);
    return response.data;
  }

  // Health Check
  async healthCheck(): Promise<string> {
    const response = await this.api.get<string>('/health');
    return response.data;
  }

  // Utility methods
  async getStringValue(tenantId: string, configKey: string, defaultValue: string = ''): Promise<string> {
    try {
      const config = await this.getTenantConfiguration(tenantId, configKey);
      return config.configValue || defaultValue;
    } catch (error) {
      return defaultValue;
    }
  }

  async getIntegerValue(tenantId: string, configKey: string, defaultValue: number = 0): Promise<number> {
    try {
      const config = await this.getTenantConfiguration(tenantId, configKey);
      return parseInt(config.configValue) || defaultValue;
    } catch (error) {
      return defaultValue;
    }
  }

  async getBooleanValue(tenantId: string, configKey: string, defaultValue: boolean = false): Promise<boolean> {
    try {
      const config = await this.getTenantConfiguration(tenantId, configKey);
      return config.configValue === 'true' || defaultValue;
    } catch (error) {
      return defaultValue;
    }
  }

  async isFeatureEnabled(flagName: string, tenantId?: string): Promise<boolean> {
    try {
      const flag = await this.getFeatureFlag(flagName);
      return flag.flagValue && flag.isActive;
    } catch (error) {
      return false;
    }
  }
}

export const configApi = new ConfigApiService();
export default configApi;