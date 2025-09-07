import { configApi, Tenant, TenantRequest, FeatureFlag, FeatureFlagRequest } from '../../services/configApi';
import axios from 'axios';

// Mock axios
jest.mock('axios');
const mockedAxios = axios as jest.Mocked<typeof axios>;

// Mock localStorage
const localStorageMock = {
  getItem: jest.fn(),
  setItem: jest.fn(),
  removeItem: jest.fn(),
  clear: jest.fn(),
};
Object.defineProperty(window, 'localStorage', {
  value: localStorageMock,
});

describe('ConfigApiService', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    localStorageMock.getItem.mockReturnValue('access-token');
  });

  describe('Tenant Management', () => {
    describe('getTenants', () => {
      it('should get all tenants successfully', async () => {
        const mockResponse = {
          data: [
            {
              id: 'tenant-id-1',
              name: 'Tenant 1',
              code: 'TENANT1',
              status: 'ACTIVE',
              contactEmail: 'tenant1@example.com',
              contactPhone: '+1234567890',
              address: '123 Main St',
              createdAt: '2023-01-01T00:00:00Z',
              updatedAt: '2023-01-01T00:00:00Z',
            },
            {
              id: 'tenant-id-2',
              name: 'Tenant 2',
              code: 'TENANT2',
              status: 'INACTIVE',
              contactEmail: 'tenant2@example.com',
              contactPhone: '+0987654321',
              address: '456 Oak Ave',
              createdAt: '2023-01-01T00:00:00Z',
              updatedAt: '2023-01-01T00:00:00Z',
            },
          ],
        };

        mockedAxios.create.mockReturnValue({
          ...mockedAxios,
          get: jest.fn().mockResolvedValue(mockResponse),
          interceptors: {
            request: { use: jest.fn() },
            response: { use: jest.fn() },
          },
        } as any);

        const result = await configApi.getTenants();

        expect(result).toEqual(mockResponse.data);
        expect(result).toHaveLength(2);
      });
    });

    describe('getTenant', () => {
      it('should get tenant by ID successfully', async () => {
        const tenantId = 'tenant-id';
        const mockResponse = {
          data: {
            id: tenantId,
            name: 'Test Tenant',
            code: 'TEST',
            status: 'ACTIVE',
            contactEmail: 'test@example.com',
            contactPhone: '+1234567890',
            address: '123 Test St',
            createdAt: '2023-01-01T00:00:00Z',
            updatedAt: '2023-01-01T00:00:00Z',
          },
        };

        mockedAxios.create.mockReturnValue({
          ...mockedAxios,
          get: jest.fn().mockResolvedValue(mockResponse),
          interceptors: {
            request: { use: jest.fn() },
            response: { use: jest.fn() },
          },
        } as any);

        const result = await configApi.getTenant(tenantId);

        expect(result).toEqual(mockResponse.data);
      });
    });

    describe('createTenant', () => {
      it('should create tenant successfully', async () => {
        const tenantRequest: TenantRequest = {
          name: 'New Tenant',
          code: 'NEW',
          contactEmail: 'new@example.com',
          contactPhone: '+1111111111',
          address: '789 New St',
        };

        const mockResponse = {
          data: {
            id: 'new-tenant-id',
            name: 'New Tenant',
            code: 'NEW',
            status: 'ACTIVE',
            contactEmail: 'new@example.com',
            contactPhone: '+1111111111',
            address: '789 New St',
            createdAt: '2023-01-01T00:00:00Z',
            updatedAt: '2023-01-01T00:00:00Z',
          },
        };

        mockedAxios.create.mockReturnValue({
          ...mockedAxios,
          post: jest.fn().mockResolvedValue(mockResponse),
          interceptors: {
            request: { use: jest.fn() },
            response: { use: jest.fn() },
          },
        } as any);

        const result = await configApi.createTenant(tenantRequest);

        expect(result).toEqual(mockResponse.data);
      });
    });

    describe('updateTenant', () => {
      it('should update tenant successfully', async () => {
        const tenantId = 'tenant-id';
        const tenantRequest: TenantRequest = {
          name: 'Updated Tenant',
          code: 'UPDATED',
          contactEmail: 'updated@example.com',
          contactPhone: '+2222222222',
          address: '999 Updated St',
        };

        const mockResponse = {
          data: {
            id: tenantId,
            name: 'Updated Tenant',
            code: 'UPDATED',
            status: 'ACTIVE',
            contactEmail: 'updated@example.com',
            contactPhone: '+2222222222',
            address: '999 Updated St',
            createdAt: '2023-01-01T00:00:00Z',
            updatedAt: '2023-01-01T00:00:00Z',
          },
        };

        mockedAxios.create.mockReturnValue({
          ...mockedAxios,
          put: jest.fn().mockResolvedValue(mockResponse),
          interceptors: {
            request: { use: jest.fn() },
            response: { use: jest.fn() },
          },
        } as any);

        const result = await configApi.updateTenant(tenantId, tenantRequest);

        expect(result).toEqual(mockResponse.data);
      });
    });

    describe('activateTenant', () => {
      it('should activate tenant successfully', async () => {
        const tenantId = 'tenant-id';

        mockedAxios.create.mockReturnValue({
          ...mockedAxios,
          put: jest.fn().mockResolvedValue({ data: {} }),
          interceptors: {
            request: { use: jest.fn() },
            response: { use: jest.fn() },
          },
        } as any);

        await configApi.activateTenant(tenantId);

        expect(mockedAxios.create().put).toHaveBeenCalledWith(`/tenants/${tenantId}/activate`);
      });
    });

    describe('deactivateTenant', () => {
      it('should deactivate tenant successfully', async () => {
        const tenantId = 'tenant-id';

        mockedAxios.create.mockReturnValue({
          ...mockedAxios,
          put: jest.fn().mockResolvedValue({ data: {} }),
          interceptors: {
            request: { use: jest.fn() },
            response: { use: jest.fn() },
          },
        } as any);

        await configApi.deactivateTenant(tenantId);

        expect(mockedAxios.create().put).toHaveBeenCalledWith(`/tenants/${tenantId}/deactivate`);
      });
    });
  });

  describe('Feature Flag Management', () => {
    describe('getFeatureFlags', () => {
      it('should get all feature flags successfully', async () => {
        const mockResponse = {
          data: [
            {
              id: 'flag-id-1',
              flagName: 'FEATURE_1',
              flagDescription: 'First feature flag',
              flagValue: true,
              tenantId: 'tenant-id-1',
              environment: 'PRODUCTION',
              rolloutPercentage: 100,
              targetUsers: 'user1,user2',
              isActive: true,
              createdAt: '2023-01-01T00:00:00Z',
              updatedAt: '2023-01-01T00:00:00Z',
            },
            {
              id: 'flag-id-2',
              flagName: 'FEATURE_2',
              flagDescription: 'Second feature flag',
              flagValue: false,
              tenantId: null,
              environment: 'DEVELOPMENT',
              rolloutPercentage: 0,
              targetUsers: null,
              isActive: true,
              createdAt: '2023-01-01T00:00:00Z',
              updatedAt: '2023-01-01T00:00:00Z',
            },
          ],
        };

        mockedAxios.create.mockReturnValue({
          ...mockedAxios,
          get: jest.fn().mockResolvedValue(mockResponse),
          interceptors: {
            request: { use: jest.fn() },
            response: { use: jest.fn() },
          },
        } as any);

        const result = await configApi.getFeatureFlags();

        expect(result).toEqual(mockResponse.data);
        expect(result).toHaveLength(2);
      });
    });

    describe('getFeatureFlag', () => {
      it('should get feature flag by name successfully', async () => {
        const flagName = 'FEATURE_1';
        const mockResponse = {
          data: {
            id: 'flag-id-1',
            flagName: 'FEATURE_1',
            flagDescription: 'First feature flag',
            flagValue: true,
            tenantId: 'tenant-id-1',
            environment: 'PRODUCTION',
            rolloutPercentage: 100,
            targetUsers: 'user1,user2',
            isActive: true,
            createdAt: '2023-01-01T00:00:00Z',
            updatedAt: '2023-01-01T00:00:00Z',
          },
        };

        mockedAxios.create.mockReturnValue({
          ...mockedAxios,
          get: jest.fn().mockResolvedValue(mockResponse),
          interceptors: {
            request: { use: jest.fn() },
            response: { use: jest.fn() },
          },
        } as any);

        const result = await configApi.getFeatureFlag(flagName);

        expect(result).toEqual(mockResponse.data);
      });
    });

    describe('createFeatureFlag', () => {
      it('should create feature flag successfully', async () => {
        const featureFlagRequest: FeatureFlagRequest = {
          flagName: 'NEW_FEATURE',
          flagDescription: 'New feature flag',
          flagValue: true,
          tenantId: 'tenant-id-1',
          environment: 'PRODUCTION',
          rolloutPercentage: 50,
          targetUsers: 'user1,user2,user3',
        };

        const mockResponse = {
          data: {
            id: 'new-flag-id',
            flagName: 'NEW_FEATURE',
            flagDescription: 'New feature flag',
            flagValue: true,
            tenantId: 'tenant-id-1',
            environment: 'PRODUCTION',
            rolloutPercentage: 50,
            targetUsers: 'user1,user2,user3',
            isActive: true,
            createdAt: '2023-01-01T00:00:00Z',
            updatedAt: '2023-01-01T00:00:00Z',
          },
        };

        mockedAxios.create.mockReturnValue({
          ...mockedAxios,
          post: jest.fn().mockResolvedValue(mockResponse),
          interceptors: {
            request: { use: jest.fn() },
            response: { use: jest.fn() },
          },
        } as any);

        const result = await configApi.createFeatureFlag(featureFlagRequest);

        expect(result).toEqual(mockResponse.data);
      });
    });

    describe('updateFeatureFlag', () => {
      it('should update feature flag successfully', async () => {
        const flagName = 'FEATURE_1';
        const featureFlagRequest: FeatureFlagRequest = {
          flagName: 'FEATURE_1',
          flagDescription: 'Updated feature flag',
          flagValue: false,
          tenantId: 'tenant-id-1',
          environment: 'PRODUCTION',
          rolloutPercentage: 75,
          targetUsers: 'user1,user2,user3,user4',
        };

        const mockResponse = {
          data: {
            id: 'flag-id-1',
            flagName: 'FEATURE_1',
            flagDescription: 'Updated feature flag',
            flagValue: false,
            tenantId: 'tenant-id-1',
            environment: 'PRODUCTION',
            rolloutPercentage: 75,
            targetUsers: 'user1,user2,user3,user4',
            isActive: true,
            createdAt: '2023-01-01T00:00:00Z',
            updatedAt: '2023-01-01T00:00:00Z',
          },
        };

        mockedAxios.create.mockReturnValue({
          ...mockedAxios,
          put: jest.fn().mockResolvedValue(mockResponse),
          interceptors: {
            request: { use: jest.fn() },
            response: { use: jest.fn() },
          },
        } as any);

        const result = await configApi.updateFeatureFlag(flagName, featureFlagRequest);

        expect(result).toEqual(mockResponse.data);
      });
    });

    describe('toggleFeatureFlag', () => {
      it('should toggle feature flag successfully', async () => {
        const flagName = 'FEATURE_1';

        mockedAxios.create.mockReturnValue({
          ...mockedAxios,
          put: jest.fn().mockResolvedValue({ data: {} }),
          interceptors: {
            request: { use: jest.fn() },
            response: { use: jest.fn() },
          },
        } as any);

        await configApi.toggleFeatureFlag(flagName);

        expect(mockedAxios.create().put).toHaveBeenCalledWith(`/feature-flags/${flagName}/toggle`);
      });
    });

    describe('deleteFeatureFlag', () => {
      it('should delete feature flag successfully', async () => {
        const flagName = 'FEATURE_1';

        mockedAxios.create.mockReturnValue({
          ...mockedAxios,
          delete: jest.fn().mockResolvedValue({ data: {} }),
          interceptors: {
            request: { use: jest.fn() },
            response: { use: jest.fn() },
          },
        } as any);

        await configApi.deleteFeatureFlag(flagName);

        expect(mockedAxios.create().delete).toHaveBeenCalledWith(`/feature-flags/${flagName}`);
      });
    });
  });

  describe('Configuration History', () => {
    describe('getConfigurationHistory', () => {
      it('should get configuration history successfully', async () => {
        const mockResponse = {
          data: [
            {
              id: 'history-id-1',
              configType: 'TENANT',
              configId: 'tenant-id-1',
              configKey: 'tenant_created',
              oldValue: null,
              newValue: '{"name":"Test Tenant","code":"TEST"}',
              changeReason: 'Tenant created',
              changedBy: 'system',
              changedAt: '2023-01-01T00:00:00Z',
              createdAt: '2023-01-01T00:00:00Z',
            },
            {
              id: 'history-id-2',
              configType: 'FEATURE_FLAG',
              configId: 'flag-id-1',
              configKey: 'FEATURE_1',
              oldValue: 'false',
              newValue: 'true',
              changeReason: 'Feature flag toggled',
              changedBy: 'admin',
              changedAt: '2023-01-01T00:00:00Z',
              createdAt: '2023-01-01T00:00:00Z',
            },
          ],
        };

        mockedAxios.create.mockReturnValue({
          ...mockedAxios,
          get: jest.fn().mockResolvedValue(mockResponse),
          interceptors: {
            request: { use: jest.fn() },
            response: { use: jest.fn() },
          },
        } as any);

        const result = await configApi.getConfigurationHistory();

        expect(result).toEqual(mockResponse.data);
        expect(result).toHaveLength(2);
      });
    });

    describe('getConfigurationHistoryByType', () => {
      it('should get configuration history by type successfully', async () => {
        const configType = 'TENANT';
        const mockResponse = {
          data: [
            {
              id: 'history-id-1',
              configType: 'TENANT',
              configId: 'tenant-id-1',
              configKey: 'tenant_created',
              oldValue: null,
              newValue: '{"name":"Test Tenant","code":"TEST"}',
              changeReason: 'Tenant created',
              changedBy: 'system',
              changedAt: '2023-01-01T00:00:00Z',
              createdAt: '2023-01-01T00:00:00Z',
            },
          ],
        };

        mockedAxios.create.mockReturnValue({
          ...mockedAxios,
          get: jest.fn().mockResolvedValue(mockResponse),
          interceptors: {
            request: { use: jest.fn() },
            response: { use: jest.fn() },
          },
        } as any);

        const result = await configApi.getConfigurationHistoryByType(configType);

        expect(result).toEqual(mockResponse.data);
        expect(result).toHaveLength(1);
      });
    });
  });

  describe('Health Check', () => {
    it('should perform health check successfully', async () => {
      const mockResponse = {
        data: 'Configuration Service is healthy',
      };

      mockedAxios.create.mockReturnValue({
        ...mockedAxios,
        get: jest.fn().mockResolvedValue(mockResponse),
        interceptors: {
          request: { use: jest.fn() },
          response: { use: jest.fn() },
        },
      } as any);

      const result = await configApi.healthCheck();

      expect(result).toBe('Configuration Service is healthy');
    });
  });

  describe('Utility Methods', () => {
    describe('getStringValue', () => {
      it('should get string value successfully', async () => {
        const tenantId = 'tenant-id';
        const configKey = 'string_config';
        const defaultValue = 'default';

        const mockResponse = {
          data: {
            id: 'config-id',
            tenantId: tenantId,
            configKey: configKey,
            configValue: 'actual_value',
            configType: 'STRING',
            isEncrypted: false,
            isActive: true,
            createdAt: '2023-01-01T00:00:00Z',
            updatedAt: '2023-01-01T00:00:00Z',
          },
        };

        mockedAxios.create.mockReturnValue({
          ...mockedAxios,
          get: jest.fn().mockResolvedValue(mockResponse),
          interceptors: {
            request: { use: jest.fn() },
            response: { use: jest.fn() },
          },
        } as any);

        const result = await configApi.getStringValue(tenantId, configKey, defaultValue);

        expect(result).toBe('actual_value');
      });

      it('should return default value on error', async () => {
        const tenantId = 'tenant-id';
        const configKey = 'string_config';
        const defaultValue = 'default';

        mockedAxios.create.mockReturnValue({
          ...mockedAxios,
          get: jest.fn().mockRejectedValue(new Error('Config not found')),
          interceptors: {
            request: { use: jest.fn() },
            response: { use: jest.fn() },
          },
        } as any);

        const result = await configApi.getStringValue(tenantId, configKey, defaultValue);

        expect(result).toBe(defaultValue);
      });
    });

    describe('getIntegerValue', () => {
      it('should get integer value successfully', async () => {
        const tenantId = 'tenant-id';
        const configKey = 'int_config';
        const defaultValue = 0;

        const mockResponse = {
          data: {
            id: 'config-id',
            tenantId: tenantId,
            configKey: configKey,
            configValue: '42',
            configType: 'INTEGER',
            isEncrypted: false,
            isActive: true,
            createdAt: '2023-01-01T00:00:00Z',
            updatedAt: '2023-01-01T00:00:00Z',
          },
        };

        mockedAxios.create.mockReturnValue({
          ...mockedAxios,
          get: jest.fn().mockResolvedValue(mockResponse),
          interceptors: {
            request: { use: jest.fn() },
            response: { use: jest.fn() },
          },
        } as any);

        const result = await configApi.getIntegerValue(tenantId, configKey, defaultValue);

        expect(result).toBe(42);
      });

      it('should return default value on error', async () => {
        const tenantId = 'tenant-id';
        const configKey = 'int_config';
        const defaultValue = 0;

        mockedAxios.create.mockReturnValue({
          ...mockedAxios,
          get: jest.fn().mockRejectedValue(new Error('Config not found')),
          interceptors: {
            request: { use: jest.fn() },
            response: { use: jest.fn() },
          },
        } as any);

        const result = await configApi.getIntegerValue(tenantId, configKey, defaultValue);

        expect(result).toBe(defaultValue);
      });
    });

    describe('getBooleanValue', () => {
      it('should get boolean value successfully', async () => {
        const tenantId = 'tenant-id';
        const configKey = 'bool_config';
        const defaultValue = false;

        const mockResponse = {
          data: {
            id: 'config-id',
            tenantId: tenantId,
            configKey: configKey,
            configValue: 'true',
            configType: 'BOOLEAN',
            isEncrypted: false,
            isActive: true,
            createdAt: '2023-01-01T00:00:00Z',
            updatedAt: '2023-01-01T00:00:00Z',
          },
        };

        mockedAxios.create.mockReturnValue({
          ...mockedAxios,
          get: jest.fn().mockResolvedValue(mockResponse),
          interceptors: {
            request: { use: jest.fn() },
            response: { use: jest.fn() },
          },
        } as any);

        const result = await configApi.getBooleanValue(tenantId, configKey, defaultValue);

        expect(result).toBe(true);
      });

      it('should return default value on error', async () => {
        const tenantId = 'tenant-id';
        const configKey = 'bool_config';
        const defaultValue = false;

        mockedAxios.create.mockReturnValue({
          ...mockedAxios,
          get: jest.fn().mockRejectedValue(new Error('Config not found')),
          interceptors: {
            request: { use: jest.fn() },
            response: { use: jest.fn() },
          },
        } as any);

        const result = await configApi.getBooleanValue(tenantId, configKey, defaultValue);

        expect(result).toBe(defaultValue);
      });
    });

    describe('isFeatureEnabled', () => {
      it('should check if feature is enabled successfully', async () => {
        const flagName = 'FEATURE_1';
        const tenantId = 'tenant-id';

        const mockResponse = {
          data: {
            id: 'flag-id-1',
            flagName: 'FEATURE_1',
            flagDescription: 'First feature flag',
            flagValue: true,
            tenantId: tenantId,
            environment: 'PRODUCTION',
            rolloutPercentage: 100,
            targetUsers: 'user1,user2',
            isActive: true,
            createdAt: '2023-01-01T00:00:00Z',
            updatedAt: '2023-01-01T00:00:00Z',
          },
        };

        mockedAxios.create.mockReturnValue({
          ...mockedAxios,
          get: jest.fn().mockResolvedValue(mockResponse),
          interceptors: {
            request: { use: jest.fn() },
            response: { use: jest.fn() },
          },
        } as any);

        const result = await configApi.isFeatureEnabled(flagName, tenantId);

        expect(result).toBe(true);
      });

      it('should return false on error', async () => {
        const flagName = 'FEATURE_1';
        const tenantId = 'tenant-id';

        mockedAxios.create.mockReturnValue({
          ...mockedAxios,
          get: jest.fn().mockRejectedValue(new Error('Feature flag not found')),
          interceptors: {
            request: { use: jest.fn() },
            response: { use: jest.fn() },
          },
        } as any);

        const result = await configApi.isFeatureEnabled(flagName, tenantId);

        expect(result).toBe(false);
      });
    });
  });
});