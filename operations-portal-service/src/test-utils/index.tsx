/**
 * Test Utilities
 * 
 * Custom render utilities and test helpers for React Testing Library.
 * Provides providers and mocks for comprehensive testing.
 */

import React, { ReactElement } from 'react';
import { render, RenderOptions } from '@testing-library/react';
import { ThemeProvider, createTheme } from '@mui/material/styles';
import { LocalizationProvider } from '@mui/x-date-pickers/LocalizationProvider';
import { AdapterDateFns } from '@mui/x-date-pickers/AdapterDateFns';
import { BrowserRouter } from 'react-router-dom';

// Context providers
import { AuthProvider } from '@contexts/AuthContext';
import { TenantProvider } from '@contexts/TenantContext';
import { NotificationProvider } from '@contexts/NotificationContext';

// Create test theme
const testTheme = createTheme({
  palette: {
    mode: 'light',
    primary: {
      main: '#1976d2',
    },
    secondary: {
      main: '#dc004e',
    },
  },
});


// Mock user data
const mockUser = {
  id: '1',
  firstName: 'John',
  lastName: 'Doe',
  email: 'john.doe@example.com',
  tenantId: 'tenant-1',
  businessUnitId: 'bu-1',
  roles: ['ADMIN', 'OPERATOR'],
};

// Mock tenant data
const mockTenant = {
  tenantId: 'tenant-1',
  businessUnitId: 'bu-1',
  name: 'Test Tenant',
  status: 'ACTIVE',
};

// Custom render function with providers
interface CustomRenderOptions extends Omit<RenderOptions, 'wrapper'> {
  initialRoute?: string;
}

const AllTheProviders: React.FC<{
  children: React.ReactNode;
}> = ({
  children,
}) => {
  return (
    <BrowserRouter>
      <ThemeProvider theme={testTheme}>
        <LocalizationProvider dateAdapter={AdapterDateFns}>
          <AuthProvider>
            <TenantProvider>
              <NotificationProvider>
                {children}
              </NotificationProvider>
            </TenantProvider>
          </AuthProvider>
        </LocalizationProvider>
      </ThemeProvider>
    </BrowserRouter>
  );
};

const customRender = (
  ui: ReactElement,
  options: CustomRenderOptions = {}
) => {
  const {
    ...renderOptions
  } = options;

  return render(ui, {
    wrapper: ({ children }) => (
      <AllTheProviders>
        {children}
      </AllTheProviders>
    ),
    ...renderOptions,
  });
};

// Mock API responses
export const mockApiResponses = {
  services: [
    {
      id: '1',
      name: 'Payment Initiation Service',
      status: 'UP',
      uptime: 86400,
      responseTime: 150,
      instances: 3,
      version: '1.0.0',
    },
    {
      id: '2',
      name: 'Saga Orchestrator',
      status: 'UP',
      uptime: 86400,
      responseTime: 200,
      instances: 2,
      version: '1.0.0',
    },
  ],
  payments: [
    {
      id: 'payment-1',
      amount: 1000,
      currency: 'USD',
      status: 'FAILED',
      sourceAccount: 'ACC123',
      destinationAccount: 'ACC456',
      reference: 'REF123',
      createdAt: new Date().toISOString(),
    },
  ],
  transactions: [
    {
      id: 'transaction-1',
      amount: 1000,
      currency: 'USD',
      status: 'COMPLETED',
      type: 'PAYMENT',
      sourceAccount: 'ACC123',
      destinationAccount: 'ACC456',
      reference: 'REF123',
      createdAt: new Date().toISOString(),
    },
  ],
};

// Mock functions
export const mockFunctions = {
  login: jest.fn(),
  logout: jest.fn(),
  showSuccess: jest.fn(),
  showError: jest.fn(),
  navigate: jest.fn(),
  execute: jest.fn(),
};

// Test data generators
export const generateTestData = {
  user: (overrides = {}) => ({ ...mockUser, ...overrides }),
  tenant: (overrides = {}) => ({ ...mockTenant, ...overrides }),
  service: (overrides = {}) => ({
    id: '1',
    name: 'Test Service',
    status: 'UP',
    uptime: 86400,
    responseTime: 150,
    instances: 1,
    version: '1.0.0',
    ...overrides,
  }),
  payment: (overrides = {}) => ({
    id: 'payment-1',
    amount: 1000,
    currency: 'USD',
    status: 'PENDING',
    sourceAccount: 'ACC123',
    destinationAccount: 'ACC456',
    reference: 'REF123',
    createdAt: new Date().toISOString(),
    ...overrides,
  }),
  transaction: (overrides = {}) => ({
    id: 'transaction-1',
    amount: 1000,
    currency: 'USD',
    status: 'COMPLETED',
    type: 'PAYMENT',
    sourceAccount: 'ACC123',
    destinationAccount: 'ACC456',
    reference: 'REF123',
    createdAt: new Date().toISOString(),
    ...overrides,
  }),
};

// Wait for async operations
export const waitForAsync = () => new Promise(resolve => setTimeout(resolve, 0));

// Mock localStorage
export const mockLocalStorage = {
  getItem: jest.fn(),
  setItem: jest.fn(),
  removeItem: jest.fn(),
  clear: jest.fn(),
};

// Mock sessionStorage
export const mockSessionStorage = {
  getItem: jest.fn(),
  setItem: jest.fn(),
  removeItem: jest.fn(),
  clear: jest.fn(),
};

// Setup mocks
beforeEach(() => {
  // Reset all mocks
  jest.clearAllMocks();
  
  // Mock localStorage
  Object.defineProperty(window, 'localStorage', {
    value: mockLocalStorage,
    writable: true,
  });
  
  // Mock sessionStorage
  Object.defineProperty(window, 'sessionStorage', {
    value: mockSessionStorage,
    writable: true,
  });
  
  // Mock fetch
  global.fetch = jest.fn();
});

// Export everything
export * from '@testing-library/react';
export { customRender as render };
