# Testing Guide

## Overview

The React Operations Portal implements a comprehensive testing strategy with multiple layers of testing to ensure reliability, maintainability, and quality. Our testing pyramid includes unit tests, component tests, integration tests, and end-to-end tests.

## 🧪 Testing Strategy

### Testing Pyramid

```
┌─────────────────────────────────────────────────────────────┐
│                    E2E Tests (Cypress)                     │
│  • Critical user journeys                                 │
│  • Cross-browser testing                                  │
│  • Accessibility testing                                  │
│  • Performance testing                                    │
├─────────────────────────────────────────────────────────────┤
│                 Integration Tests                          │
│  • API integration                                        │
│  • Authentication flow                                    │
│  • State management                                       │
│  • Context providers                                      │
├─────────────────────────────────────────────────────────────┤
│                  Component Tests                           │
│  • User interactions                                      │
│  • Error states                                           │
│  • Loading states                                         │
│  • Conditional rendering                                  │
├─────────────────────────────────────────────────────────────┤
│                   Unit Tests (Jest)                        │
│  • Service clients                                        │
│  • Custom hooks                                           │
│  • Utility functions                                      │
│  • Error handling                                         │
└─────────────────────────────────────────────────────────────┘
```

## 🛠️ Testing Infrastructure

### Core Testing Tools

- **Jest**: Unit testing framework with mocking capabilities
- **React Testing Library**: Component testing with user-centric approach
- **MSW (Mock Service Worker)**: API mocking for consistent testing
- **Cypress**: End-to-end testing with real browser testing
- **axe-core**: Accessibility testing for WCAG compliance

### Test Configuration

```javascript
// jest.config.js
module.exports = {
  testEnvironment: 'jsdom',
  setupFilesAfterEnv: ['<rootDir>/src/setupTests.ts'],
  moduleNameMapping: {
    '^@/(.*)$': '<rootDir>/src/$1',
    '^@components/(.*)$': '<rootDir>/src/components/$1',
    '^@services/(.*)$': '<rootDir>/src/services/$1',
    '^@hooks/(.*)$': '<rootDir>/src/hooks/$1',
    '^@utils/(.*)$': '<rootDir>/src/utils/$1',
    '^@types/(.*)$': '<rootDir>/src/types/$1'
  },
  collectCoverageFrom: [
    'src/**/*.{ts,tsx}',
    '!src/**/*.d.ts',
    '!src/index.tsx',
    '!src/setupTests.ts'
  ],
  coverageThreshold: {
    global: {
      branches: 80,
      functions: 80,
      lines: 80,
      statements: 80
    }
  }
};
```

## 📝 Unit Tests

### Service Client Testing

```typescript
// src/services/__tests__/authService.test.ts
import { rest } from 'msw';
import { setupServer } from 'msw/node';
import { AuthService } from '../authService';

const server = setupServer(
  rest.post('/api/auth/login', (req, res, ctx) => {
    return res(
      ctx.json({
        token: 'mock-jwt-token',
        user: { id: '1', name: 'Test User' }
      })
    );
  })
);

describe('AuthService', () => {
  beforeAll(() => server.listen());
  afterEach(() => server.resetHandlers());
  afterAll(() => server.close());

  it('should login successfully', async () => {
    const authService = new AuthService();
    const result = await authService.login('user', 'password');
    
    expect(result.token).toBe('mock-jwt-token');
    expect(result.user.name).toBe('Test User');
  });
});
```

### Custom Hook Testing

```typescript
// src/hooks/__tests__/useApi.test.ts
import { renderHook, waitFor } from '@testing-library/react';
import { useApi } from '../useApi';

describe('useApi', () => {
  it('should handle successful API calls', async () => {
    const { result } = renderHook(() => useApi('/api/test'));
    
    await waitFor(() => {
      expect(result.current.data).toBeDefined();
      expect(result.current.loading).toBe(false);
      expect(result.current.error).toBeNull();
    });
  });

  it('should handle API errors', async () => {
    const { result } = renderHook(() => useApi('/api/error'));
    
    await waitFor(() => {
      expect(result.current.error).toBeDefined();
      expect(result.current.loading).toBe(false);
    });
  });
});
```

### Utility Function Testing

```typescript
// src/utils/__tests__/formatters.test.ts
import { formatCurrency, formatDate, formatNumber } from '../formatters';

describe('formatters', () => {
  describe('formatCurrency', () => {
    it('should format currency correctly', () => {
      expect(formatCurrency(1234.56, 'USD')).toBe('$1,234.56');
      expect(formatCurrency(1234.56, 'EUR')).toBe('€1,234.56');
    });
  });

  describe('formatDate', () => {
    it('should format dates correctly', () => {
      const date = new Date('2023-12-25');
      expect(formatDate(date)).toBe('Dec 25, 2023');
    });
  });
});
```

## 🧩 Component Tests

### Basic Component Testing

```typescript
// src/components/__tests__/DataTable.test.tsx
import { render, screen, fireEvent } from '@testing-library/react';
import { DataTable } from '../DataTable';

const mockData = [
  { id: 1, name: 'Item 1', status: 'active' },
  { id: 2, name: 'Item 2', status: 'inactive' }
];

describe('DataTable', () => {
  it('should render data correctly', () => {
    render(<DataTable data={mockData} />);
    
    expect(screen.getByText('Item 1')).toBeInTheDocument();
    expect(screen.getByText('Item 2')).toBeInTheDocument();
  });

  it('should handle sorting', () => {
    render(<DataTable data={mockData} sortable />);
    
    const nameHeader = screen.getByText('Name');
    fireEvent.click(nameHeader);
    
    expect(screen.getByText('Item 1')).toBeInTheDocument();
  });
});
```

### Component with Context Testing

```typescript
// src/pages/__tests__/Dashboard.test.tsx
import { render, screen, waitFor } from '@testing-library/react';
import { Dashboard } from '../Dashboard';
import { renderWithProviders } from '../../test-utils';

describe('Dashboard', () => {
  it('should display system health', async () => {
    renderWithProviders(<Dashboard />);
    
    await waitFor(() => {
      expect(screen.getByText('System Health')).toBeInTheDocument();
      expect(screen.getByText('All Systems Operational')).toBeInTheDocument();
    });
  });

  it('should handle loading state', () => {
    renderWithProviders(<Dashboard />);
    
    expect(screen.getByTestId('loading-spinner')).toBeInTheDocument();
  });
});
```

### Error State Testing

```typescript
// src/components/__tests__/ErrorBoundary.test.tsx
import { render, screen } from '@testing-library/react';
import { ErrorBoundary } from '../ErrorBoundary';

const ThrowError = () => {
  throw new Error('Test error');
};

describe('ErrorBoundary', () => {
  it('should catch and display errors', () => {
    render(
      <ErrorBoundary>
        <ThrowError />
      </ErrorBoundary>
    );
    
    expect(screen.getByText('Something went wrong')).toBeInTheDocument();
  });
});
```

## 🔗 Integration Tests

### API Integration Testing

```typescript
// src/__tests__/integration/api.test.ts
import { render, screen, waitFor } from '@testing-library/react';
import { Dashboard } from '../../pages/Dashboard';
import { renderWithProviders } from '../../test-utils';

describe('API Integration', () => {
  it('should fetch and display system health', async () => {
    renderWithProviders(<Dashboard />);
    
    await waitFor(() => {
      expect(screen.getByText('System Health')).toBeInTheDocument();
      expect(screen.getByText('All Systems Operational')).toBeInTheDocument();
    });
  });
});
```

### Authentication Flow Testing

```typescript
// src/__tests__/integration/authFlow.test.tsx
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import { AuthProvider } from '../../contexts/AuthContext';
import { Login } from '../../pages/Login';

describe('Authentication Flow', () => {
  it('should login and redirect to dashboard', async () => {
    render(
      <BrowserRouter>
        <AuthProvider>
          <Login />
        </AuthProvider>
      </BrowserRouter>
    );
    
    fireEvent.change(screen.getByLabelText('Username'), {
      target: { value: 'testuser' }
    });
    fireEvent.change(screen.getByLabelText('Password'), {
      target: { value: 'testpass' }
    });
    fireEvent.click(screen.getByText('Login'));
    
    await waitFor(() => {
      expect(screen.getByText('Dashboard')).toBeInTheDocument();
    });
  });
});
```

## 🌐 End-to-End Tests

### Cypress Configuration

```typescript
// cypress.config.ts
import { defineConfig } from 'cypress';

export default defineConfig({
  e2e: {
    baseUrl: 'http://localhost:3000',
    supportFile: 'cypress/support/e2e.ts',
    specPattern: 'cypress/e2e/**/*.cy.ts',
    viewportWidth: 1280,
    viewportHeight: 720,
    video: true,
    screenshotOnRunFailure: true
  }
});
```

### Critical User Journey Testing

```typescript
// cypress/e2e/critical-user-journeys.cy.ts
describe('Critical User Journeys', () => {
  beforeEach(() => {
    cy.login('testuser', 'testpass');
  });

  it('should complete login to dashboard flow', () => {
    cy.visit('/');
    cy.get('[data-testid="dashboard"]').should('be.visible');
    cy.get('[data-testid="system-health"]').should('contain', 'All Systems Operational');
  });

  it('should navigate to service management', () => {
    cy.visit('/services');
    cy.get('[data-testid="service-list"]').should('be.visible');
    cy.get('[data-testid="service-item"]').should('have.length.greaterThan', 0);
  });

  it('should perform payment repair operations', () => {
    cy.visit('/payment-repair');
    cy.get('[data-testid="failed-payments"]').should('be.visible');
    cy.get('[data-testid="retry-button"]').first().click();
    cy.get('[data-testid="success-message"]').should('be.visible');
  });
});
```

### Accessibility Testing

```typescript
// cypress/e2e/accessibility.cy.ts
import 'cypress-axe';

describe('Accessibility', () => {
  it('should have no accessibility violations', () => {
    cy.visit('/');
    cy.injectAxe();
    cy.checkA11y();
  });

  it('should support keyboard navigation', () => {
    cy.visit('/');
    cy.get('body').tab();
    cy.focused().should('have.attr', 'tabindex');
  });
});
```

## 📊 Test Coverage

### Coverage Configuration

```javascript
// jest.config.js
module.exports = {
  collectCoverageFrom: [
    'src/**/*.{ts,tsx}',
    '!src/**/*.d.ts',
    '!src/index.tsx',
    '!src/setupTests.ts'
  ],
  coverageThreshold: {
    global: {
      branches: 80,
      functions: 80,
      lines: 80,
      statements: 80
    }
  }
};
```

### Coverage Reports

```bash
# Generate coverage report
npm run test:coverage

# View coverage in browser
npm run test:coverage:open
```

## 🚀 Running Tests

### Test Commands

```bash
# Run all tests
npm test

# Run tests in watch mode
npm run test:watch

# Run tests with coverage
npm run test:coverage

# Run specific test types
npm run test:unit
npm run test:component
npm run test:integration
npm run test:e2e
npm run test:a11y

# Run tests in CI mode
npm run test:ci
```

### Test Scripts

```json
{
  "scripts": {
    "test": "jest",
    "test:watch": "jest --watch",
    "test:coverage": "jest --coverage",
    "test:ci": "jest --ci --coverage --watchAll=false",
    "test:unit": "jest --testPathPattern=__tests__/unit",
    "test:component": "jest --testPathPattern=__tests__/component",
    "test:integration": "jest --testPathPattern=__tests__/integration",
    "test:e2e": "cypress run",
    "test:e2e:open": "cypress open",
    "test:a11y": "cypress run --spec 'cypress/e2e/accessibility.cy.ts'"
  }
}
```

## 🛠️ Test Utilities

### Custom Render Function

```typescript
// src/test-utils/index.tsx
import { render, RenderOptions } from '@testing-library/react';
import { ReactElement } from 'react';
import { BrowserRouter } from 'react-router-dom';
import { ThemeProvider } from '@mui/material/styles';
import { createTheme } from '@mui/material/styles';
import { AuthProvider } from '../contexts/AuthContext';
import { TenantProvider } from '../contexts/TenantContext';
import { NotificationProvider } from '../contexts/NotificationProvider';

const theme = createTheme();

const AllTheProviders = ({ children }: { children: React.ReactNode }) => {
  return (
    <BrowserRouter>
      <ThemeProvider theme={theme}>
        <AuthProvider>
          <TenantProvider>
            <NotificationProvider>
              {children}
            </NotificationProvider>
          </TenantProvider>
        </AuthProvider>
      </ThemeProvider>
    </BrowserRouter>
  );
};

const customRender = (
  ui: ReactElement,
  options?: Omit<RenderOptions, 'wrapper'>
) => render(ui, { wrapper: AllTheProviders, ...options });

export * from '@testing-library/react';
export { customRender as render };
```

### Mock Data and Fixtures

```typescript
// src/test-utils/fixtures.ts
export const mockUser = {
  id: '1',
  name: 'Test User',
  email: 'test@example.com',
  role: 'admin',
  permissions: ['read', 'write', 'admin']
};

export const mockSystemHealth = {
  status: 'healthy',
  services: [
    { name: 'Payment Service', status: 'up', responseTime: 120 },
    { name: 'Auth Service', status: 'up', responseTime: 80 }
  ]
};

export const mockTransactions = [
  {
    id: '1',
    amount: 1000,
    currency: 'USD',
    status: 'completed',
    timestamp: '2023-12-25T10:00:00Z'
  }
];
```

## 🎯 Testing Best Practices

### 1. **Test Structure**
- Use descriptive test names
- Follow AAA pattern (Arrange, Act, Assert)
- Keep tests focused and simple
- Test behavior, not implementation

### 2. **Mocking Strategy**
- Mock external dependencies
- Use MSW for API mocking
- Mock at the right level of abstraction
- Keep mocks simple and maintainable

### 3. **Test Data**
- Use realistic test data
- Create reusable fixtures
- Avoid hardcoded values
- Use factories for complex data

### 4. **Error Testing**
- Test error states and edge cases
- Test error handling and recovery
- Test user feedback for errors
- Test error boundaries

### 5. **Accessibility Testing**
- Test keyboard navigation
- Test screen reader compatibility
- Test color contrast
- Test focus management

### 6. **Performance Testing**
- Test loading states
- Test large data sets
- Test memory usage
- Test rendering performance

## 🔍 Debugging Tests

### Debugging Unit Tests

```bash
# Run specific test with debugging
npm test -- --testNamePattern="should login successfully" --verbose

# Run tests in debug mode
node --inspect-brk node_modules/.bin/jest --runInBand
```

### Debugging E2E Tests

```bash
# Run Cypress in headed mode
npm run test:e2e:open

# Run specific test
npx cypress run --spec "cypress/e2e/critical-user-journeys.cy.ts"
```

## 📈 Continuous Integration

### GitHub Actions Configuration

```yaml
# .github/workflows/test.yml
name: Tests
on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-node@v4
        with:
          node-version: '18'
          cache: 'npm'
      - run: npm ci
      - run: npm run test:ci
      - run: npm run test:e2e
      - uses: codecov/codecov-action@v3
        with:
          file: ./coverage/lcov.info
```

## 🎯 Quality Gates

### Coverage Requirements
- **Unit Tests**: 80%+ coverage
- **Component Tests**: 80%+ coverage
- **Integration Tests**: 70%+ coverage
- **E2E Tests**: Critical user journeys covered

### Performance Requirements
- **Bundle Size**: < 1MB gzipped
- **Load Time**: < 3 seconds
- **Time to Interactive**: < 5 seconds
- **Lighthouse Score**: 90+ for all categories

### Accessibility Requirements
- **WCAG AA Compliance**: 100%
- **Keyboard Navigation**: All interactive elements
- **Screen Reader**: Compatible with major screen readers
- **Color Contrast**: 4.5:1 minimum ratio

This comprehensive testing strategy ensures the React Operations Portal is reliable, maintainable, and provides an excellent user experience across all scenarios.
