/**
 * Tenant Context
 * 
 * React Context for managing tenant and business unit context
 * throughout the application.
 */

import React, { createContext, useContext, useReducer, useEffect, ReactNode } from 'react';
import { TenantContextType } from '../types/auth';
import { config } from '../config/environment';
import { STORAGE_KEYS } from '../constants';

// Tenant State Interface
interface TenantState {
  tenantId: string;
  businessUnitId: string;
  availableTenants: Array<{
    id: string;
    name: string;
    businessUnits: Array<{
      id: string;
      name: string;
    }>;
  }>;
  isLoading: boolean;
  error: string | null;
}

// Tenant Actions
type TenantAction =
  | { type: 'SET_TENANT'; payload: string }
  | { type: 'SET_BUSINESS_UNIT'; payload: string }
  | { type: 'SET_AVAILABLE_TENANTS'; payload: Array<any> }
  | { type: 'SET_LOADING'; payload: boolean }
  | { type: 'SET_ERROR'; payload: string | null }
  | { type: 'RESET' };

// Initial State
const initialState: TenantState = {
  tenantId: config.defaultTenantId,
  businessUnitId: config.defaultBusinessUnitId,
  availableTenants: [],
  isLoading: false,
  error: null,
};

// Tenant Reducer
function tenantReducer(state: TenantState, action: TenantAction): TenantState {
  switch (action.type) {
    case 'SET_TENANT':
      return {
        ...state,
        tenantId: action.payload,
        error: null,
      };
    case 'SET_BUSINESS_UNIT':
      return {
        ...state,
        businessUnitId: action.payload,
        error: null,
      };
    case 'SET_AVAILABLE_TENANTS':
      return {
        ...state,
        availableTenants: action.payload,
      };
    case 'SET_LOADING':
      return {
        ...state,
        isLoading: action.payload,
      };
    case 'SET_ERROR':
      return {
        ...state,
        error: action.payload,
      };
    case 'RESET':
      return {
        ...initialState,
        tenantId: config.defaultTenantId,
        businessUnitId: config.defaultBusinessUnitId,
      };
    default:
      return state;
  }
}

// Create Context
const TenantContext = createContext<TenantContextType | undefined>(undefined);

// Tenant Provider Props
interface TenantProviderProps {
  children: ReactNode;
}

// Tenant Provider Component
export function TenantProvider({ children }: TenantProviderProps) {
  const [state, dispatch] = useReducer(tenantReducer, initialState);

  // Initialize tenant context on mount
  useEffect(() => {
    initializeTenantContext();
  }, []);

  // Initialize tenant context
  const initializeTenantContext = () => {
    try {
      // Load from localStorage or use defaults
      const savedTenantId = localStorage.getItem(STORAGE_KEYS.TENANT_ID);
      const savedBusinessUnitId = localStorage.getItem(STORAGE_KEYS.BUSINESS_UNIT_ID);

      if (savedTenantId) {
        dispatch({ type: 'SET_TENANT', payload: savedTenantId });
      }

      if (savedBusinessUnitId) {
        dispatch({ type: 'SET_BUSINESS_UNIT', payload: savedBusinessUnitId });
      }
    } catch (error) {
      console.error('Failed to initialize tenant context:', error);
    }
  };

  // Set tenant
  const setTenant = (tenantId: string) => {
    try {
      dispatch({ type: 'SET_TENANT', payload: tenantId });
      localStorage.setItem(STORAGE_KEYS.TENANT_ID, tenantId);
    } catch (error) {
      console.error('Failed to set tenant:', error);
      dispatch({ type: 'SET_ERROR', payload: 'Failed to set tenant' });
    }
  };

  // Set business unit
  const setBusinessUnit = (businessUnitId: string) => {
    try {
      dispatch({ type: 'SET_BUSINESS_UNIT', payload: businessUnitId });
      localStorage.setItem(STORAGE_KEYS.BUSINESS_UNIT_ID, businessUnitId);
    } catch (error) {
      console.error('Failed to set business unit:', error);
      dispatch({ type: 'SET_ERROR', payload: 'Failed to set business unit' });
    }
  };

  // Clear error
  const clearError = () => {
    dispatch({ type: 'SET_ERROR', payload: null });
  };

  const contextValue: TenantContextType = {
    tenantId: state.tenantId,
    businessUnitId: state.businessUnitId,
    setTenant,
    setBusinessUnit,
  };

  return (
    <TenantContext.Provider value={contextValue}>
      {children}
    </TenantContext.Provider>
  );
}

// Custom hook to use tenant context
export function useTenant(): TenantContextType {
  const context = useContext(TenantContext);
  if (context === undefined) {
    throw new Error('useTenant must be used within a TenantProvider');
  }
  return context;
}

export default TenantContext;
