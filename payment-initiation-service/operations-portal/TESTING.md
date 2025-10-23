# Testing Guide

## Overview

This document provides comprehensive testing guidelines for the React Operations Portal. The testing strategy includes unit tests, integration tests, E2E tests, and accessibility tests with a target of 80%+ coverage.

## Testing Stack

- **Jest**: Test runner and assertion library
- **React Testing Library**: Component testing utilities
- **MSW (Mock Service Worker)**: API mocking
- **Cypress**: E2E testing
- **axe-core**: Accessibility testing

## Test Structure

```
src/
├── __tests__/                 # Integration tests
├── components/__tests__/      # Component tests
├── hooks/__tests__/           # Hook tests
├── services/__tests__/        # Service tests
├── test-utils/                # Test utilities
├── mocks/                     # MSW handlers
└── setupTests.ts              # Test setup

cypress/
├── e2e/                       # E2E tests
├── support/                  # Cypress support
└── fixtures/                  # Test fixtures
```

## Running Tests

### Unit and Integration Tests
```bash
# Run all tests
npm test

# Run tests in watch mode
npm test -- --watch

# Run tests with coverage
npm test -- --coverage

# Run specific test file
npm test -- --testPathPattern=Dashboard.test.tsx
```

### E2E Tests
```bash
# Run Cypress tests
npm run cypress:open

# Run Cypress tests headlessly
npm run cypress:run

# Run specific E2E test
npm run cypress:run -- --spec "cypress/e2e/critical-user-journeys.cy.ts"
```

## Test Categories

### 1. Unit Tests

**Purpose**: Test individual functions, components, and utilities in isolation.

**Coverage**: Services, hooks, utilities, formatters, validators

**Examples**:
- API service methods
- Custom hooks (useApi, usePermissions)
- Utility functions (formatters, validators)
- Component rendering

### 2. Integration Tests

**Purpose**: Test interactions between multiple components and services.

**Coverage**: Authentication flow, API integration, state management

**Examples**:
- Login/logout flow
- API error handling
- Context provider integration
- Navigation between pages

### 3. Component Tests

**Purpose**: Test React components with user interactions.

**Coverage**: All page components, shared components, user interactions

**Examples**:
- Dashboard component rendering
- DataTable sorting and filtering
- Form submissions
- Button clicks and navigation

### 4. E2E Tests

**Purpose**: Test complete user workflows from start to finish.

**Coverage**: Critical user journeys, cross-browser compatibility

**Examples**:
- Complete operations workflow
- Service management operations
- Payment repair workflow
- Channel onboarding process

### 5. Accessibility Tests

**Purpose**: Ensure WCAG AA compliance and keyboard navigation.

**Coverage**: All pages and components

**Examples**:
- Screen reader compatibility
- Keyboard navigation
- Color contrast
- ARIA labels and roles

## Test Utilities

### Custom Render Function
```typescript
import { render } from '@test-utils';

// Renders component with all providers
render(<Component />, {
  initialAuthState: { isAuthenticated: true },
  initialTenantState: { tenantId: 'tenant-1' },
});
```

### Mock Data
```typescript
import { fixtures } from '@test-utils/fixtures';

// Use predefined test data
const mockServices = fixtures.serviceHealth;
const mockPayments = fixtures.payments;
```

### MSW Handlers
```typescript
import { server } from '@mocks/server';

// Mock API responses
server.use(
  rest.get('/api/services/health', (req, res, ctx) => {
    return res(ctx.json(mockServices));
  })
);
```

## Writing Tests

### Component Tests
```typescript
describe('Dashboard', () => {
  it('should render dashboard with service health data', () => {
    render(<Dashboard />);
    
    expect(screen.getByText('Operations Dashboard')).toBeInTheDocument();
    expect(screen.getByText('System Health Overview')).toBeInTheDocument();
  });
});
```

### Hook Tests
```typescript
describe('useApi', () => {
  it('should execute API call and return data', async () => {
    const { result } = renderHook(() => useApi(mockApiCall));
    
    await waitFor(() => {
      expect(result.current.data).toEqual(mockData);
    });
  });
});
```

### Service Tests
```typescript
describe('AuthService', () => {
  it('should login with valid credentials', async () => {
    const result = await authService.login(credentials);
    
    expect(result.token).toBe('mock-jwt-token');
    expect(result.user.email).toBe(credentials.email);
  });
});
```

### E2E Tests
```typescript
describe('Critical User Journeys', () => {
  it('should complete full operations workflow', () => {
    cy.login();
    cy.navigateTo('dashboard');
    cy.get('[data-testid="dashboard"]').should('be.visible');
    
    cy.clickNavItem('Service Management');
    cy.startService('Payment Initiation Service');
    cy.checkNotification('Service control initiated successfully', 'success');
  });
});
```

## Test Data Management

### Fixtures
- **Service Health**: Mock service health data
- **Payments**: Mock payment data
- **Transactions**: Mock transaction data
- **Users**: Mock user and auth data
- **API Responses**: Mock API response data

### Mock Functions
- **API Calls**: Mocked service methods
- **Navigation**: Mocked router functions
- **Notifications**: Mocked toast functions
- **Storage**: Mocked localStorage/sessionStorage

## Coverage Requirements

### Minimum Coverage Thresholds
- **Branches**: 80%
- **Functions**: 80%
- **Lines**: 80%
- **Statements**: 80%

### Coverage Reports
- **Text**: Console output
- **HTML**: Detailed HTML report
- **LCOV**: CI/CD integration

## Best Practices

### 1. Test Organization
- Group related tests in describe blocks
- Use descriptive test names
- Follow AAA pattern (Arrange, Act, Assert)

### 2. Mock Management
- Mock external dependencies
- Use MSW for API mocking
- Reset mocks between tests

### 3. Data Management
- Use fixtures for consistent test data
- Create test data generators
- Avoid hardcoded values

### 4. Assertions
- Test behavior, not implementation
- Use semantic queries
- Test accessibility attributes

### 5. Error Handling
- Test error states
- Test loading states
- Test edge cases

## Debugging Tests

### Jest Debugging
```bash
# Run tests in debug mode
npm test -- --detectOpenHandles

# Run specific test with verbose output
npm test -- --verbose --testNamePattern="Dashboard"
```

### Cypress Debugging
```bash
# Open Cypress in debug mode
npm run cypress:open

# Run with debug output
npm run cypress:run -- --headed
```

## CI/CD Integration

### GitHub Actions
```yaml
- name: Run Tests
  run: npm test -- --coverage --watchAll=false

- name: Run E2E Tests
  run: npm run cypress:run

- name: Upload Coverage
  uses: codecov/codecov-action@v3
```

### Coverage Reporting
- **Codecov**: Coverage reporting
- **SonarQube**: Code quality analysis
- **GitHub**: PR coverage comments

## Troubleshooting

### Common Issues
1. **MSW not intercepting requests**: Check handler registration
2. **Tests timing out**: Increase timeout or wait for async operations
3. **Coverage not updating**: Clear cache and rebuild
4. **Cypress tests failing**: Check test data and selectors

### Solutions
1. **Clear cache**: `npm test -- --clearCache`
2. **Reset mocks**: Use `jest.clearAllMocks()`
3. **Debug selectors**: Use Cypress debug tools
4. **Check console**: Look for error messages

## Performance Testing

### Load Testing
- **K6**: Load testing scenarios
- **Artillery**: Performance testing
- **Lighthouse**: Performance audits

### Monitoring
- **Bundle size**: Webpack bundle analyzer
- **Runtime performance**: React DevTools
- **Memory usage**: Chrome DevTools

## Security Testing

### Security Scenarios
- **XSS prevention**: Input sanitization
- **CSRF protection**: Token validation
- **Authentication**: Session management
- **Authorization**: Permission checks

### Tools
- **OWASP ZAP**: Security scanning
- **Snyk**: Vulnerability scanning
- **ESLint Security**: Code analysis

## Conclusion

This testing strategy ensures comprehensive coverage of the React Operations Portal with a focus on quality, reliability, and maintainability. The combination of unit, integration, E2E, and accessibility tests provides confidence in the application's functionality and user experience.
