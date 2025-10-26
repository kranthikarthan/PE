/**
 * Accessibility E2E Tests
 * 
 * End-to-end tests for accessibility compliance.
 * Tests WCAG AA compliance and keyboard navigation.
 */

describe('Accessibility Tests', () => {
  beforeEach(() => {
    cy.login();
  });

  describe('WCAG AA Compliance', () => {
    it('should pass accessibility audit on dashboard', () => {
      cy.navigateTo('dashboard');
      cy.checkA11y();
    });

    it('should pass accessibility audit on service management', () => {
      cy.navigateTo('services');
      cy.checkA11y();
    });

    it('should pass accessibility audit on payment repair', () => {
      cy.navigateTo('payment-repair');
      cy.checkA11y();
    });

    it('should pass accessibility audit on transaction enquiries', () => {
      cy.navigateTo('transactions');
      cy.checkA11y();
    });

    it('should pass accessibility audit on reconciliation monitoring', () => {
      cy.navigateTo('reconciliation');
      cy.checkA11y();
    });

    it('should pass accessibility audit on channel onboarding', () => {
      cy.navigateTo('channel-onboarding');
      cy.checkA11y();
    });

    it('should pass accessibility audit on clearing system onboarding', () => {
      cy.navigateTo('clearing-onboarding');
      cy.checkA11y();
    });
  });

  describe('Keyboard Navigation', () => {
    it('should be fully navigable with keyboard', () => {
      cy.navigateTo('dashboard');

      // Test tab navigation
      cy.get('body').tab();
      cy.focused().should('be.visible');

      // Test shift+tab navigation
      cy.get('body').tab({ shift: true });
      cy.focused().should('be.visible');

      // Test arrow key navigation
      cy.get('body').type('{rightarrow}');
      cy.focused().should('be.visible');

      // Test enter key activation
      cy.get('body').type('{enter}');
    });

    it('should navigate between pages with keyboard', () => {
      cy.navigateTo('dashboard');

      // Navigate to service management
      cy.get('[data-testid="nav-service-management"]').focus();
      cy.get('[data-testid="nav-service-management"]').type('{enter}');
      cy.url().should('include', '/services');

      // Navigate to payment repair
      cy.get('[data-testid="nav-payment-repair"]').focus();
      cy.get('[data-testid="nav-payment-repair"]').type('{enter}');
      cy.url().should('include', '/payment-repair');
    });

    it('should navigate forms with keyboard', () => {
      cy.navigateTo('channel-onboarding');

      // Test form navigation
      cy.get('[data-testid="form-field-name"]').focus();
      cy.get('[data-testid="form-field-name"]').type('Test Channel');

      cy.get('[data-testid="form-field-type"]').focus();
      cy.get('[data-testid="form-field-type"]').type('{downarrow}');
      cy.get('[data-testid="form-field-type"]').type('{enter}');

      cy.get('[data-testid="form-field-endpoint"]').focus();
      cy.get('[data-testid="form-field-endpoint"]').type('https://api.test.com');
    });

    it('should navigate tables with keyboard', () => {
      cy.navigateTo('transactions');

      // Test table navigation
      cy.get('[data-testid="transactions-table"]').focus();
      cy.get('[data-testid="transactions-table"]').type('{rightarrow}');
      cy.get('[data-testid="transactions-table"]').type('{leftarrow}');
      cy.get('[data-testid="transactions-table"]').type('{downarrow}');
      cy.get('[data-testid="transactions-table"]').type('{uparrow}');
    });
  });

  describe('Screen Reader Support', () => {
    it('should have proper ARIA labels', () => {
      cy.navigateTo('dashboard');

      // Check for ARIA labels
      cy.get('[aria-label="Operations Dashboard"]').should('exist');
      cy.get('[aria-label="System Health Overview"]').should('exist');
      cy.get('[aria-label="Recent Alerts"]').should('exist');
    });

    it('should have proper heading structure', () => {
      cy.navigateTo('dashboard');

      // Check heading hierarchy
      cy.get('h1').should('exist');
      cy.get('h2').should('exist');
      cy.get('h3').should('exist');
    });

    it('should have proper form labels', () => {
      cy.navigateTo('channel-onboarding');

      // Check form labels
      cy.get('[data-testid="form-field-name"]').should('have.attr', 'aria-label');
      cy.get('[data-testid="form-field-type"]').should('have.attr', 'aria-label');
      cy.get('[data-testid="form-field-endpoint"]').should('have.attr', 'aria-label');
    });

    it('should have proper table headers', () => {
      cy.navigateTo('transactions');

      // Check table headers
      cy.get('[data-testid="transactions-table"]').should('have.attr', 'role', 'table');
      cy.get('[data-testid="transactions-table"]').should('have.attr', 'aria-label');
    });
  });

  describe('Color Contrast', () => {
    it('should have sufficient color contrast', () => {
      cy.navigateTo('dashboard');

      // Check color contrast for text elements
      cy.get('[data-testid="dashboard"]').should('have.css', 'color');
      cy.get('[data-testid="dashboard"]').should('have.css', 'background-color');
    });

    it('should have sufficient color contrast for status indicators', () => {
      cy.navigateTo('services');

      // Check status chip colors
      cy.get('[data-testid="status-chip-UP"]').should('have.css', 'color');
      cy.get('[data-testid="status-chip-DOWN"]').should('have.css', 'color');
    });
  });

  describe('Focus Management', () => {
    it('should manage focus properly on page navigation', () => {
      cy.navigateTo('dashboard');

      // Navigate to service management
      cy.clickNavItem('Service Management');
      cy.focused().should('be.visible');

      // Navigate back to dashboard
      cy.clickNavItem('Dashboard');
      cy.focused().should('be.visible');
    });

    it('should manage focus properly on modal opening', () => {
      cy.navigateTo('services');

      // Open service control modal
      cy.get('[data-testid="service-Payment Initiation Service"]').within(() => {
        cy.get('[data-testid="control-service-button"]').click();
      });

      cy.get('[data-testid="service-control-modal"]').should('be.visible');
      cy.focused().should('be.visible');

      // Close modal
      cy.get('[data-testid="close-modal-button"]').click();
      cy.focused().should('be.visible');
    });

    it('should manage focus properly on dialog opening', () => {
      cy.navigateTo('payment-repair');

      // Open payment details dialog
      cy.clickTableAction(0, 'view-details');
      cy.get('[data-testid="payment-details-dialog"]').should('be.visible');
      cy.focused().should('be.visible');

      // Close dialog
      cy.get('[data-testid="close-dialog-button"]').click();
      cy.focused().should('be.visible');
    });
  });

  describe('Error Handling', () => {
    it('should announce errors to screen readers', () => {
      // Mock API error
      cy.mockApiError('/api/ops/v1/services/health', 500, 'Internal server error');

      cy.navigateTo('dashboard');

      // Check for error announcement
      cy.get('[data-testid="error-message"]').should('have.attr', 'aria-live');
      cy.get('[data-testid="error-message"]').should('have.attr', 'aria-atomic', 'true');
    });

    it('should announce success messages to screen readers', () => {
      cy.navigateTo('services');

      // Start a service
      cy.startService('Payment Initiation Service');

      // Check for success announcement
      cy.get('[data-testid="success-message"]').should('have.attr', 'aria-live');
      cy.get('[data-testid="success-message"]').should('have.attr', 'aria-atomic', 'true');
    });
  });

  describe('Loading States', () => {
    it('should announce loading states to screen readers', () => {
      cy.navigateTo('dashboard');

      // Check for loading announcement
      cy.get('[data-testid="loading-spinner"]').should('have.attr', 'aria-live');
      cy.get('[data-testid="loading-spinner"]').should('have.attr', 'aria-label', 'Loading');
    });
  });

  describe('Form Validation', () => {
    it('should announce validation errors to screen readers', () => {
      cy.navigateTo('channel-onboarding');

      // Submit form without required fields
      cy.get('[data-testid="save-channel-button"]').click();

      // Check for validation error announcement
      cy.get('[data-testid="validation-error"]').should('have.attr', 'aria-live');
      cy.get('[data-testid="validation-error"]').should('have.attr', 'aria-atomic', 'true');
    });
  });

  describe('Data Tables', () => {
    it('should have proper table structure for screen readers', () => {
      cy.navigateTo('transactions');

      // Check table structure
      cy.get('[data-testid="transactions-table"]').should('have.attr', 'role', 'table');
      cy.get('[data-testid="transactions-table"]').should('have.attr', 'aria-label');

      // Check column headers
      cy.get('[data-testid="column-header-amount"]').should('have.attr', 'role', 'columnheader');
      cy.get('[data-testid="column-header-status"]').should('have.attr', 'role', 'columnheader');

      // Check row headers
      cy.get('[data-testid="table-row-0"]').should('have.attr', 'role', 'row');
    });
  });

  describe('Interactive Elements', () => {
    it('should have proper button labels', () => {
      cy.navigateTo('services');

      // Check button labels
      cy.get('[data-testid="start-service-button"]').should('have.attr', 'aria-label');
      cy.get('[data-testid="stop-service-button"]').should('have.attr', 'aria-label');
      cy.get('[data-testid="restart-service-button"]').should('have.attr', 'aria-label');
    });

    it('should have proper link labels', () => {
      cy.navigateTo('dashboard');

      // Check link labels
      cy.get('[data-testid="nav-service-management"]').should('have.attr', 'aria-label');
      cy.get('[data-testid="nav-payment-repair"]').should('have.attr', 'aria-label');
    });

    it('should have proper input labels', () => {
      cy.navigateTo('channel-onboarding');

      // Check input labels
      cy.get('[data-testid="form-field-name"]').should('have.attr', 'aria-label');
      cy.get('[data-testid="form-field-endpoint"]').should('have.attr', 'aria-label');
    });
  });

  describe('Navigation', () => {
    it('should have proper navigation structure', () => {
      cy.navigateTo('dashboard');

      // Check navigation structure
      cy.get('[data-testid="main-navigation"]').should('have.attr', 'role', 'navigation');
      cy.get('[data-testid="main-navigation"]').should('have.attr', 'aria-label', 'Main navigation');
    });

    it('should have proper breadcrumb navigation', () => {
      cy.navigateTo('services');

      // Check breadcrumb navigation
      cy.get('[data-testid="breadcrumb-navigation"]').should('have.attr', 'role', 'navigation');
      cy.get('[data-testid="breadcrumb-navigation"]').should('have.attr', 'aria-label', 'Breadcrumb navigation');
    });
  });

  describe('Search and Filter', () => {
    it('should have proper search functionality', () => {
      cy.navigateTo('transactions');

      // Check search input
      cy.get('[data-testid="search-input"]').should('have.attr', 'aria-label');
      cy.get('[data-testid="search-input"]').should('have.attr', 'aria-describedby');

      // Check search button
      cy.get('[data-testid="search-button"]').should('have.attr', 'aria-label');
    });

    it('should have proper filter functionality', () => {
      cy.navigateTo('transactions');

      // Check filter controls
      cy.get('[data-testid="filter-status"]').should('have.attr', 'aria-label');
      cy.get('[data-testid="filter-type"]').should('have.attr', 'aria-label');
    });
  });
});
