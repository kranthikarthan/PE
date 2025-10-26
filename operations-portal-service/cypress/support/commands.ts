/**
 * Cypress Commands
 * 
 * Custom Cypress commands for the React Operations Portal.
 * Provides reusable commands for common test operations.
 */

/// <reference types="cypress" />

// Authentication commands
Cypress.Commands.add('login', (email = 'john.doe@example.com', password = 'password123') => {
  cy.visit('/login');
  cy.get('[data-testid="email-input"]').type(email);
  cy.get('[data-testid="password-input"]').type(password);
  cy.get('[data-testid="login-button"]').click();
  cy.url().should('not.include', '/login');
  cy.get('[data-testid="dashboard"]').should('be.visible');
});

Cypress.Commands.add('logout', () => {
  cy.get('[data-testid="user-menu-button"]').click();
  cy.get('[data-testid="logout-button"]').click();
  cy.url().should('include', '/login');
});

// Navigation commands
Cypress.Commands.add('navigateTo', (page: string) => {
  const pageMap: Record<string, string> = {
    dashboard: '/',
    services: '/services',
    'payment-repair': '/payment-repair',
    transactions: '/transactions',
    reconciliation: '/reconciliation',
    'channel-onboarding': '/channel-onboarding',
    'clearing-onboarding': '/clearing-onboarding',
  };

  const path = pageMap[page] || `/${page}`;
  cy.visit(path);
  cy.url().should('include', path);
});

Cypress.Commands.add('clickNavItem', (itemName: string) => {
  cy.get(`[data-testid="nav-${itemName.toLowerCase().replace(/\s+/g, '-')}"]`).click();
});

// Data interaction commands
Cypress.Commands.add('searchInTable', (searchTerm: string) => {
  cy.get('[data-testid="search-input"]').type(searchTerm);
  cy.get('[data-testid="search-button"]').click();
});

Cypress.Commands.add('filterTable', (filterName: string, filterValue: string) => {
  cy.get(`[data-testid="filter-${filterName}"]`).click();
  cy.get(`[data-testid="filter-${filterName}-option-${filterValue}"]`).click();
});

Cypress.Commands.add('sortTable', (columnName: string) => {
  cy.get(`[data-testid="sort-${columnName}"]`).click();
});

Cypress.Commands.add('selectTableRow', (rowIndex: number) => {
  cy.get(`[data-testid="table-row-${rowIndex}"] input[type="checkbox"]`).check();
});

Cypress.Commands.add('selectAllTableRows', () => {
  cy.get('[data-testid="select-all-checkbox"]').check();
});

Cypress.Commands.add('clickTableAction', (rowIndex: number, actionName: string) => {
  cy.get(`[data-testid="table-row-${rowIndex}"]`).within(() => {
    cy.get(`[data-testid="action-${actionName}"]`).click();
  });
});

// Form interaction commands
Cypress.Commands.add('fillForm', (formData: Record<string, string>) => {
  Object.entries(formData).forEach(([field, value]) => {
    cy.get(`[data-testid="form-field-${field}"]`).type(value);
  });
});

Cypress.Commands.add('selectDropdown', (fieldName: string, optionValue: string) => {
  cy.get(`[data-testid="form-field-${fieldName}"]`).click();
  cy.get(`[data-testid="dropdown-option-${optionValue}"]`).click();
});

Cypress.Commands.add('selectDateRange', (startDate: string, endDate: string) => {
  cy.get('[data-testid="date-range-picker"]').click();
  cy.get('[data-testid="start-date-input"]').type(startDate);
  cy.get('[data-testid="end-date-input"]').type(endDate);
  cy.get('[data-testid="apply-date-range"]').click();
});

// Service management commands
Cypress.Commands.add('startService', (serviceName: string) => {
  cy.get(`[data-testid="service-${serviceName}"]`).within(() => {
    cy.get('[data-testid="start-service-button"]').click();
  });
  cy.get('[data-testid="confirm-dialog"]').within(() => {
    cy.get('[data-testid="confirm-button"]').click();
  });
});

Cypress.Commands.add('stopService', (serviceName: string) => {
  cy.get(`[data-testid="service-${serviceName}"]`).within(() => {
    cy.get('[data-testid="stop-service-button"]').click();
  });
  cy.get('[data-testid="confirm-dialog"]').within(() => {
    cy.get('[data-testid="confirm-button"]').click();
  });
});

Cypress.Commands.add('restartService', (serviceName: string) => {
  cy.get(`[data-testid="service-${serviceName}"]`).within(() => {
    cy.get('[data-testid="restart-service-button"]').click();
  });
  cy.get('[data-testid="confirm-dialog"]').within(() => {
    cy.get('[data-testid="confirm-button"]').click();
  });
});

// Payment repair commands
Cypress.Commands.add('retryPayment', (paymentId: string) => {
  cy.get(`[data-testid="payment-${paymentId}"]`).within(() => {
    cy.get('[data-testid="retry-payment-button"]').click();
  });
  cy.get('[data-testid="confirm-dialog"]').within(() => {
    cy.get('[data-testid="confirm-button"]').click();
  });
});

Cypress.Commands.add('cancelPayment', (paymentId: string) => {
  cy.get(`[data-testid="payment-${paymentId}"]`).within(() => {
    cy.get('[data-testid="cancel-payment-button"]').click();
  });
  cy.get('[data-testid="confirm-dialog"]').within(() => {
    cy.get('[data-testid="confirm-button"]').click();
  });
});

Cypress.Commands.add('bulkRetryPayments', (paymentIds: string[]) => {
  paymentIds.forEach(id => {
    cy.selectTableRow(id);
  });
  cy.get('[data-testid="bulk-retry-button"]').click();
  cy.get('[data-testid="confirm-dialog"]').within(() => {
    cy.get('[data-testid="confirm-button"]').click();
  });
});

// Circuit breaker commands
Cypress.Commands.add('openCircuitBreaker', (breakerName: string) => {
  cy.get(`[data-testid="circuit-breaker-${breakerName}"]`).within(() => {
    cy.get('[data-testid="open-breaker-button"]').click();
  });
  cy.get('[data-testid="confirm-dialog"]').within(() => {
    cy.get('[data-testid="confirm-button"]').click();
  });
});

Cypress.Commands.add('closeCircuitBreaker', (breakerName: string) => {
  cy.get(`[data-testid="circuit-breaker-${breakerName}"]`).within(() => {
    cy.get('[data-testid="close-breaker-button"]').click();
  });
  cy.get('[data-testid="confirm-dialog"]').within(() => {
    cy.get('[data-testid="confirm-button"]').click();
  });
});

// Feature flag commands
Cypress.Commands.add('toggleFeatureFlag', (flagName: string) => {
  cy.get(`[data-testid="feature-flag-${flagName}"]`).within(() => {
    cy.get('[data-testid="toggle-switch"]').click();
  });
});

// Channel onboarding commands
Cypress.Commands.add('createChannel', (channelData: Record<string, string>) => {
  cy.get('[data-testid="add-channel-button"]').click();
  cy.fillForm(channelData);
  cy.get('[data-testid="save-channel-button"]').click();
});

Cypress.Commands.add('testChannelConnection', (channelName: string) => {
  cy.get(`[data-testid="channel-${channelName}"]`).within(() => {
    cy.get('[data-testid="test-connection-button"]').click();
  });
});

// Utility commands
Cypress.Commands.add('waitForLoading', () => {
  cy.get('[data-testid="loading-spinner"]', { timeout: 10000 }).should('not.exist');
});

Cypress.Commands.add('waitForData', () => {
  cy.get('[data-testid="data-table"]', { timeout: 10000 }).should('be.visible');
});

Cypress.Commands.add('checkNotification', (message: string, type: 'success' | 'error' | 'warning' | 'info') => {
  cy.get(`[data-testid="notification-${type}"]`).should('contain', message);
});

Cypress.Commands.add('checkStatusChip', (status: string, color: string) => {
  cy.get(`[data-testid="status-chip-${status}"]`).should('have.class', `MuiChip-color${color}`);
});

Cypress.Commands.add('checkMetricCard', (metricName: string, expectedValue: string) => {
  cy.get(`[data-testid="metric-card-${metricName}"]`).should('contain', expectedValue);
});

// Accessibility commands
Cypress.Commands.add('checkA11y', () => {
  cy.injectAxe();
  cy.checkA11y();
});

Cypress.Commands.add('checkKeyboardNavigation', () => {
  cy.get('body').tab();
  cy.focused().should('be.visible');
});

// API mocking commands
Cypress.Commands.add('mockApiResponse', (endpoint: string, response: any) => {
  cy.intercept('GET', endpoint, response).as('mockApi');
});

Cypress.Commands.add('mockApiError', (endpoint: string, statusCode: number, errorMessage: string) => {
  cy.intercept('GET', endpoint, {
    statusCode,
    body: { message: errorMessage },
  }).as('mockApiError');
});

// Declare custom commands for TypeScript
declare global {
  namespace Cypress {
    interface Chainable {
      login(email?: string, password?: string): Chainable<void>;
      logout(): Chainable<void>;
      navigateTo(page: string): Chainable<void>;
      clickNavItem(itemName: string): Chainable<void>;
      searchInTable(searchTerm: string): Chainable<void>;
      filterTable(filterName: string, filterValue: string): Chainable<void>;
      sortTable(columnName: string): Chainable<void>;
      selectTableRow(rowIndex: number): Chainable<void>;
      selectAllTableRows(): Chainable<void>;
      clickTableAction(rowIndex: number, actionName: string): Chainable<void>;
      fillForm(formData: Record<string, string>): Chainable<void>;
      selectDropdown(fieldName: string, optionValue: string): Chainable<void>;
      selectDateRange(startDate: string, endDate: string): Chainable<void>;
      startService(serviceName: string): Chainable<void>;
      stopService(serviceName: string): Chainable<void>;
      restartService(serviceName: string): Chainable<void>;
      retryPayment(paymentId: string): Chainable<void>;
      cancelPayment(paymentId: string): Chainable<void>;
      bulkRetryPayments(paymentIds: string[]): Chainable<void>;
      openCircuitBreaker(breakerName: string): Chainable<void>;
      closeCircuitBreaker(breakerName: string): Chainable<void>;
      toggleFeatureFlag(flagName: string): Chainable<void>;
      createChannel(channelData: Record<string, string>): Chainable<void>;
      testChannelConnection(channelName: string): Chainable<void>;
      waitForLoading(): Chainable<void>;
      waitForData(): Chainable<void>;
      checkNotification(message: string, type: 'success' | 'error' | 'warning' | 'info'): Chainable<void>;
      checkStatusChip(status: string, color: string): Chainable<void>;
      checkMetricCard(metricName: string, expectedValue: string): Chainable<void>;
      checkA11y(): Chainable<void>;
      checkKeyboardNavigation(): Chainable<void>;
      mockApiResponse(endpoint: string, response: any): Chainable<void>;
      mockApiError(endpoint: string, statusCode: number, errorMessage: string): Chainable<void>;
    }
  }
}
