import { createSlice, createAsyncThunk, PayloadAction } from '@reduxjs/toolkit';
import { apiClient } from '../../services/apiClient';

// Types
export interface Tenant {
  id: string;
  tenantId: string;
  tenantName: string;
  tenantType: 'BANK' | 'FINTECH' | 'CORPORATE' | 'GOVERNMENT' | 'CREDIT_UNION';
  status: 'ACTIVE' | 'INACTIVE' | 'SUSPENDED' | 'PENDING_ACTIVATION';
  subscriptionTier: 'BASIC' | 'STANDARD' | 'PREMIUM' | 'ENTERPRISE';
  configuration: Record<string, any>;
  createdAt: string;
  updatedAt: string;
}

export interface TenantConfiguration {
  [key: string]: any;
}

export interface FeatureFlag {
  name: string;
  enabled: boolean;
  config: Record<string, any>;
  rolloutPercentage: number;
}

export interface TenantState {
  // Current tenant
  currentTenant: string;
  currentTenantInfo: Tenant | null;
  
  // Available tenants
  availableTenants: Tenant[];
  
  // Tenant configuration
  configuration: TenantConfiguration;
  featureFlags: Record<string, FeatureFlag>;
  
  // UI state
  loading: {
    tenants: boolean;
    configuration: boolean;
    featureFlags: boolean;
    switching: boolean;
  };
  
  error: string | null;
  
  // Permissions
  permissions: string[];
  
  // Tenant-specific settings
  settings: {
    theme: string;
    locale: string;
    timezone: string;
    currency: string;
  };
}

const initialState: TenantState = {
  currentTenant: 'default',
  currentTenantInfo: null,
  availableTenants: [],
  configuration: {},
  featureFlags: {},
  loading: {
    tenants: false,
    configuration: false,
    featureFlags: false,
    switching: false,
  },
  error: null,
  permissions: [],
  settings: {
    theme: 'light',
    locale: 'en-US',
    timezone: 'UTC',
    currency: 'USD',
  },
};

// Async Thunks
export const loadAvailableTenants = createAsyncThunk(
  'tenant/loadAvailableTenants',
  async (_, { rejectWithValue }) => {
    try {
      const response = await apiClient.listTenants();
      return response.data;
    } catch (error: any) {
      return rejectWithValue(error.response?.data?.message || 'Failed to load tenants');
    }
  }
);

export const switchTenant = createAsyncThunk(
  'tenant/switchTenant',
  async (tenantId: string, { rejectWithValue, dispatch }) => {
    try {
      // Update API client
      apiClient.setTenant(tenantId);
      
      // Load tenant info
      const tenantResponse = await apiClient.getTenant(tenantId);
      const tenant = tenantResponse.data;
      
      // Load tenant configuration
      dispatch(loadTenantConfiguration(tenantId));
      dispatch(loadTenantFeatureFlags(tenantId));
      
      return { tenantId, tenant };
    } catch (error: any) {
      return rejectWithValue(error.response?.data?.message || 'Failed to switch tenant');
    }
  }
);

export const loadTenantConfiguration = createAsyncThunk(
  'tenant/loadConfiguration',
  async (tenantId: string, { rejectWithValue }) => {
    try {
      const response = await apiClient.getTenantConfig(tenantId);
      return response.data;
    } catch (error: any) {
      return rejectWithValue(error.response?.data?.message || 'Failed to load configuration');
    }
  }
);

export const updateTenantConfiguration = createAsyncThunk(
  'tenant/updateConfiguration',
  async (
    { key, value, tenantId }: { key: string; value: string; tenantId?: string },
    { rejectWithValue, getState }
  ) => {
    try {
      const state = getState() as { tenant: TenantState };
      const targetTenant = tenantId || state.tenant.currentTenant;
      
      await apiClient.setTenantConfig(key, value, targetTenant);
      
      // Reload configuration
      const response = await apiClient.getTenantConfig(targetTenant);
      return response.data;
    } catch (error: any) {
      return rejectWithValue(error.response?.data?.message || 'Failed to update configuration');
    }
  }
);

export const loadTenantFeatureFlags = createAsyncThunk(
  'tenant/loadFeatureFlags',
  async (tenantId: string, { rejectWithValue }) => {
    try {
      const response = await apiClient.getTenantFeatureFlags(tenantId);
      return response.data;
    } catch (error: any) {
      return rejectWithValue(error.response?.data?.message || 'Failed to load feature flags');
    }
  }
);

export const updateFeatureFlag = createAsyncThunk(
  'tenant/updateFeatureFlag',
  async (
    {
      featureName,
      enabled,
      config = {},
      tenantId,
    }: {
      featureName: string;
      enabled: boolean;
      config?: Record<string, any>;
      tenantId?: string;
    },
    { rejectWithValue, getState, dispatch }
  ) => {
    try {
      const state = getState() as { tenant: TenantState };
      const targetTenant = tenantId || state.tenant.currentTenant;
      
      await apiClient.setTenantFeatureFlag(featureName, enabled, config, targetTenant);
      
      // Reload feature flags
      dispatch(loadTenantFeatureFlags(targetTenant));
      
      return { featureName, enabled, config };
    } catch (error: any) {
      return rejectWithValue(error.response?.data?.message || 'Failed to update feature flag');
    }
  }
);

export const createNewTenant = createAsyncThunk(
  'tenant/createTenant',
  async (tenantData: Partial<Tenant>, { rejectWithValue, dispatch }) => {
    try {
      const response = await apiClient.createTenant(tenantData);
      const newTenant = response.data;
      
      // Reload available tenants
      dispatch(loadAvailableTenants());
      
      return newTenant;
    } catch (error: any) {
      return rejectWithValue(error.response?.data?.message || 'Failed to create tenant');
    }
  }
);

// Slice
const tenantSlice = createSlice({
  name: 'tenant',
  initialState,
  reducers: {
    // Set current tenant (for initial load)
    setCurrentTenant: (state, action: PayloadAction<string>) => {
      state.currentTenant = action.payload;
      apiClient.setTenant(action.payload);
    },
    
    // Update tenant settings
    updateTenantSettings: (state, action: PayloadAction<Partial<TenantState['settings']>>) => {
      state.settings = { ...state.settings, ...action.payload };
    },
    
    // Set permissions
    setTenantPermissions: (state, action: PayloadAction<string[]>) => {
      state.permissions = action.payload;
    },
    
    // Clear tenant data (on logout)
    clearTenantData: (state) => {
      state.currentTenant = 'default';
      state.currentTenantInfo = null;
      state.configuration = {};
      state.featureFlags = {};
      state.permissions = [];
      state.error = null;
    },
    
    // Clear errors
    clearTenantError: (state) => {
      state.error = null;
    },
    
    // Update single configuration value locally
    updateConfigurationValue: (state, action: PayloadAction<{ key: string; value: any }>) => {
      state.configuration[action.payload.key] = action.payload.value;
    },
    
    // Update single feature flag locally
    updateFeatureFlagLocal: (state, action: PayloadAction<{ name: string; enabled: boolean; config?: any }>) => {
      if (state.featureFlags[action.payload.name]) {
        state.featureFlags[action.payload.name].enabled = action.payload.enabled;
        if (action.payload.config) {
          state.featureFlags[action.payload.name].config = action.payload.config;
        }
      }
    },
  },
  extraReducers: (builder) => {
    // Load available tenants
    builder
      .addCase(loadAvailableTenants.pending, (state) => {
        state.loading.tenants = true;
        state.error = null;
      })
      .addCase(loadAvailableTenants.fulfilled, (state, action) => {
        state.loading.tenants = false;
        state.availableTenants = action.payload;
      })
      .addCase(loadAvailableTenants.rejected, (state, action) => {
        state.loading.tenants = false;
        state.error = action.payload as string;
      });

    // Switch tenant
    builder
      .addCase(switchTenant.pending, (state) => {
        state.loading.switching = true;
        state.error = null;
      })
      .addCase(switchTenant.fulfilled, (state, action) => {
        state.loading.switching = false;
        state.currentTenant = action.payload.tenantId;
        state.currentTenantInfo = action.payload.tenant;
      })
      .addCase(switchTenant.rejected, (state, action) => {
        state.loading.switching = false;
        state.error = action.payload as string;
      });

    // Load tenant configuration
    builder
      .addCase(loadTenantConfiguration.pending, (state) => {
        state.loading.configuration = true;
      })
      .addCase(loadTenantConfiguration.fulfilled, (state, action) => {
        state.loading.configuration = false;
        state.configuration = action.payload;
      })
      .addCase(loadTenantConfiguration.rejected, (state, action) => {
        state.loading.configuration = false;
        state.error = action.payload as string;
      });

    // Update tenant configuration
    builder
      .addCase(updateTenantConfiguration.fulfilled, (state, action) => {
        state.configuration = action.payload;
      })
      .addCase(updateTenantConfiguration.rejected, (state, action) => {
        state.error = action.payload as string;
      });

    // Load feature flags
    builder
      .addCase(loadTenantFeatureFlags.pending, (state) => {
        state.loading.featureFlags = true;
      })
      .addCase(loadTenantFeatureFlags.fulfilled, (state, action) => {
        state.loading.featureFlags = false;
        // Convert array to object for easier access
        state.featureFlags = action.payload.reduce((acc: Record<string, FeatureFlag>, flag: FeatureFlag) => {
          acc[flag.name] = flag;
          return acc;
        }, {});
      })
      .addCase(loadTenantFeatureFlags.rejected, (state, action) => {
        state.loading.featureFlags = false;
        state.error = action.payload as string;
      });

    // Create new tenant
    builder
      .addCase(createNewTenant.fulfilled, (state, action) => {
        state.availableTenants.push(action.payload);
      })
      .addCase(createNewTenant.rejected, (state, action) => {
        state.error = action.payload as string;
      });
  },
});

// Actions
export const {
  setCurrentTenant,
  updateTenantSettings,
  setTenantPermissions,
  clearTenantData,
  clearTenantError,
  updateConfigurationValue,
  updateFeatureFlagLocal,
} = tenantSlice.actions;

// Selectors
export const selectCurrentTenant = (state: { tenant: TenantState }) => state.tenant.currentTenant;
export const selectCurrentTenantInfo = (state: { tenant: TenantState }) => state.tenant.currentTenantInfo;
export const selectAvailableTenants = (state: { tenant: TenantState }) => state.tenant.availableTenants;
export const selectTenantConfiguration = (state: { tenant: TenantState }) => state.tenant.configuration;
export const selectTenantFeatureFlags = (state: { tenant: TenantState }) => state.tenant.featureFlags;
export const selectTenantLoading = (state: { tenant: TenantState }) => state.tenant.loading;
export const selectTenantError = (state: { tenant: TenantState }) => state.tenant.error;
export const selectTenantPermissions = (state: { tenant: TenantState }) => state.tenant.permissions;
export const selectTenantSettings = (state: { tenant: TenantState }) => state.tenant.settings;

// Feature flag selector
export const selectFeatureFlag = (featureName: string) => (state: { tenant: TenantState }) => 
  state.tenant.featureFlags[featureName]?.enabled || false;

// Configuration value selector
export const selectConfigValue = (configKey: string) => (state: { tenant: TenantState }) =>
  state.tenant.configuration[configKey];

export default tenantSlice.reducer;