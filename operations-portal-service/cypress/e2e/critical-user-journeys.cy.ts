/**
 * Critical User Journeys E2E Tests
 * 
 * End-to-end tests for critical user workflows.
 * Tests complete user journeys from login to operations.
 */

describe('Critical User Journeys', () => {
  beforeEach(() => {
    // Mock API responses
    cy.mockApiResponse('/api/ops/v1/services/health', [
      {
        id: '1',
        name: 'Payment Initiation Service',
        status: 'UP',
        uptime: 86400,
        responseTime: 150,
        instances: 3,
        version: '1.0.0',
      },
    ]);

    cy.mockApiResponse('/api/ops/v1/services/health/summary', {
      totalServices: 5,
      healthyServices: 4,
      unhealthyServices: 1,
      overallHealth: 'HEALTHY',
    });

    cy.mockApiResponse('/api/ops/v1/services/alerts', [
      {
        id: '1',
        message: 'High CPU usage detected',
        severity: 'HIGH',
        timestamp: new Date().toISOString(),
      },
    ]);
  });

  describe('Complete Operations Workflow', () => {
    it('should complete full operations workflow from login to service management', () => {
      // 1. Login
      cy.login('john.doe@example.com', 'password123');
      cy.checkNotification('Login successful!', 'success');

      // 2. Navigate to Dashboard
      cy.navigateTo('dashboard');
      cy.get('[data-testid="dashboard"]').should('be.visible');
      cy.get('[data-testid="system-health-overview"]').should('be.visible');
      cy.get('[data-testid="recent-alerts"]').should('be.visible');

      // 3. Check service health
      cy.get('[data-testid="service-Payment Initiation Service"]').should('be.visible');
      cy.checkStatusChip('UP', 'Success');

      // 4. Navigate to Service Management
      cy.clickNavItem('Service Management');
      cy.get('[data-testid="service-management"]').should('be.visible');

      // 5. Start a service
      cy.startService('Payment Initiation Service');
      cy.checkNotification('Service control initiated successfully', 'success');

      // 6. Check circuit breakers
      cy.get('[data-testid="circuit-breakers-section"]').should('be.visible');
      cy.openCircuitBreaker('payment-service');
      cy.checkNotification('Circuit breaker opened successfully', 'success');

      // 7. Toggle feature flag
      cy.toggleFeatureFlag('ENABLE_NEW_UI');
      cy.checkNotification('Feature flag updated successfully', 'success');

      // 8. Navigate to Payment Repair
      cy.clickNavItem('Payment Repair');
      cy.get('[data-testid="payment-repair"]').should('be.visible');

      // 9. Search for failed payments
      cy.searchInTable('FAILED');
      cy.waitForData();

      // 10. Retry a payment
      cy.retryPayment('payment-1');
      cy.checkNotification('Payment retry initiated successfully', 'success');

      // 11. Navigate to Transaction Enquiries
      cy.clickNavItem('Transaction Enquiries');
      cy.get('[data-testid="transaction-enquiries"]').should('be.visible');

      // 12. Search transactions
      cy.searchInTable('REF123456');
      cy.waitForData();

      // 13. Filter by status
      cy.filterTable('status', 'COMPLETED');
      cy.waitForData();

      // 14. Navigate to Reconciliation Monitoring
      cy.clickNavItem('Reconciliation Monitoring');
      cy.get('[data-testid="reconciliation-monitoring"]').should('be.visible');

      // 15. Check reconciliation batches
      cy.get('[data-testid="reconciliation-batches"]').should('be.visible');

      // 16. Navigate to Channel Onboarding
      cy.clickNavItem('Channel Onboarding');
      cy.get('[data-testid="channel-onboarding"]').should('be.visible');

      // 17. Create a new channel
      cy.createChannel({
        name: 'Test Bank API',
        type: 'BANK_API',
        endpoint: 'https://api.testbank.com/v1',
        description: 'Test bank API channel',
      });
      cy.checkNotification('Channel created successfully', 'success');

      // 18. Test channel connection
      cy.testChannelConnection('Test Bank API');
      cy.checkNotification('Connection test successful', 'success');

      // 19. Navigate to Clearing System Onboarding
      cy.clickNavItem('Clearing System Onboarding');
      cy.get('[data-testid="clearing-system-onboarding"]').should('be.visible');

      // 20. Logout
      cy.logout();
      cy.url().should('include', '/login');
    });
  });

  describe('Service Management Workflow', () => {
    it('should manage services, circuit breakers, and feature flags', () => {
      cy.login();
      cy.navigateTo('services');

      // Check service status
      cy.get('[data-testid="service-Payment Initiation Service"]').should('be.visible');
      cy.checkStatusChip('UP', 'Success');

      // Start service
      cy.startService('Payment Initiation Service');
      cy.checkNotification('Service control initiated successfully', 'success');

      // Stop service
      cy.stopService('Payment Initiation Service');
      cy.checkNotification('Service control initiated successfully', 'success');

      // Restart service
      cy.restartService('Payment Initiation Service');
      cy.checkNotification('Service control initiated successfully', 'success');

      // Open circuit breaker
      cy.openCircuitBreaker('payment-service');
      cy.checkNotification('Circuit breaker opened successfully', 'success');

      // Close circuit breaker
      cy.closeCircuitBreaker('payment-service');
      cy.checkNotification('Circuit breaker closed successfully', 'success');

      // Toggle feature flags
      cy.toggleFeatureFlag('ENABLE_NEW_UI');
      cy.checkNotification('Feature flag updated successfully', 'success');

      cy.toggleFeatureFlag('ENABLE_ANALYTICS');
      cy.checkNotification('Feature flag updated successfully', 'success');
    });
  });

  describe('Payment Repair Workflow', () => {
    it('should repair failed payments and handle bulk operations', () => {
      cy.login();
      cy.navigateTo('payment-repair');

      // Check failed payments
      cy.get('[data-testid="failed-payments-table"]').should('be.visible');

      // Search for specific payment
      cy.searchInTable('payment-1');
      cy.waitForData();

      // Retry individual payment
      cy.retryPayment('payment-1');
      cy.checkNotification('Payment retry initiated successfully', 'success');

      // Cancel payment
      cy.cancelPayment('payment-2');
      cy.checkNotification('Payment cancelled successfully', 'success');

      // Bulk retry payments
      cy.bulkRetryPayments(['payment-3', 'payment-4']);
      cy.checkNotification('Bulk retry initiated successfully', 'success');

      // Filter by status
      cy.filterTable('status', 'FAILED');
      cy.waitForData();

      // Sort by amount
      cy.sortTable('amount');
      cy.waitForData();
    });
  });

  describe('Transaction Enquiries Workflow', () => {
    it('should search and filter transactions effectively', () => {
      cy.login();
      cy.navigateTo('transactions');

      // Check transaction table
      cy.get('[data-testid="transactions-table"]').should('be.visible');

      // Search by reference
      cy.searchInTable('REF123456');
      cy.waitForData();

      // Filter by status
      cy.filterTable('status', 'COMPLETED');
      cy.waitForData();

      // Filter by type
      cy.filterTable('type', 'PAYMENT');
      cy.waitForData();

      // Select date range
      cy.selectDateRange('2024-01-01', '2024-01-31');
      cy.waitForData();

      // Sort by amount
      cy.sortTable('amount');
      cy.waitForData();

      // Sort by date
      cy.sortTable('createdAt');
      cy.waitForData();

      // View transaction details
      cy.clickTableAction(0, 'view-details');
      cy.get('[data-testid="transaction-details-dialog"]').should('be.visible');

      // Close dialog
      cy.get('[data-testid="close-dialog-button"]').click();
    });
  });

  describe('Reconciliation Monitoring Workflow', () => {
    it('should monitor reconciliation batches and handle exceptions', () => {
      cy.login();
      cy.navigateTo('reconciliation');

      // Check reconciliation batches
      cy.get('[data-testid="reconciliation-batches"]').should('be.visible');

      // Check performance metrics
      cy.get('[data-testid="performance-metrics"]').should('be.visible');

      // Check exceptions
      cy.get('[data-testid="reconciliation-exceptions"]').should('be.visible');

      // Filter by status
      cy.filterTable('status', 'COMPLETED');
      cy.waitForData();

      // Sort by success rate
      cy.sortTable('successRate');
      cy.waitForData();

      // View batch details
      cy.clickTableAction(0, 'view-details');
      cy.get('[data-testid="batch-details-dialog"]').should('be.visible');

      // Download report
      cy.get('[data-testid="download-report-button"]').click();
      cy.checkNotification('Reconciliation report download initiated', 'success');
    });
  });

  describe('Channel Onboarding Workflow', () => {
    it('should onboard new channels and test connections', () => {
      cy.login();
      cy.navigateTo('channel-onboarding');

      // Check existing channels
      cy.get('[data-testid="existing-channels"]').should('be.visible');

      // Create new channel
      cy.createChannel({
        name: 'New Bank API',
        type: 'BANK_API',
        endpoint: 'https://api.newbank.com/v1',
        description: 'New bank API integration',
      });
      cy.checkNotification('Channel created successfully', 'success');

      // Test channel connection
      cy.testChannelConnection('New Bank API');
      cy.checkNotification('Connection test successful', 'success');

      // Edit channel
      cy.get('[data-testid="channel-New Bank API"]').within(() => {
        cy.get('[data-testid="edit-channel-button"]').click();
      });

      // Update channel details
      cy.fillForm({
        name: 'Updated Bank API',
        description: 'Updated bank API integration',
      });

      cy.get('[data-testid="save-channel-button"]').click();
      cy.checkNotification('Channel updated successfully', 'success');

      // Delete channel
      cy.get('[data-testid="channel-Updated Bank API"]').within(() => {
        cy.get('[data-testid="delete-channel-button"]').click();
      });

      cy.get('[data-testid="confirm-dialog"]').within(() => {
        cy.get('[data-testid="confirm-button"]').click();
      });

      cy.checkNotification('Channel deleted successfully', 'success');
    });
  });

  describe('Clearing System Onboarding Workflow', () => {
    it('should onboard clearing systems and configure message formats', () => {
      cy.login();
      cy.navigateTo('clearing-onboarding');

      // Check existing clearing systems
      cy.get('[data-testid="existing-clearing-systems"]').should('be.visible');

      // Create new clearing system
      cy.get('[data-testid="add-clearing-system-button"]').click();

      cy.fillForm({
        name: 'New SAMOS System',
        type: 'SAMOS',
        endpoint: 'https://samos.new.com/api',
        description: 'New SAMOS clearing system',
      });

      cy.get('[data-testid="save-clearing-system-button"]').click();
      cy.checkNotification('Clearing system created successfully', 'success');

      // Test clearing system connection
      cy.get('[data-testid="clearing-system-New SAMOS System"]').within(() => {
        cy.get('[data-testid="test-connection-button"]').click();
      });

      cy.checkNotification('Connection test successful', 'success');

      // Configure message format
      cy.get('[data-testid="clearing-system-New SAMOS System"]').within(() => {
        cy.get('[data-testid="configure-message-format-button"]').click();
      });

      cy.selectDropdown('messageFormat', 'ISO20022');
      cy.get('[data-testid="save-message-format-button"]').click();
      cy.checkNotification('Message format configured successfully', 'success');
    });
  });

  describe('Error Handling and Recovery', () => {
    it('should handle API errors gracefully', () => {
      // Mock API error
      cy.mockApiError('/api/ops/v1/services/health', 500, 'Internal server error');

      cy.login();
      cy.navigateTo('dashboard');

      // Check error message
      cy.get('[data-testid="error-message"]').should('contain', 'Failed to load service health data');
      cy.checkNotification('Failed to load service health data', 'error');

      // Retry after error
      cy.get('[data-testid="retry-button"]').click();
      cy.waitForLoading();
    });

    it('should handle network errors gracefully', () => {
      // Mock network error
      cy.intercept('GET', '/api/ops/v1/services/health', { forceNetworkError: true });

      cy.login();
      cy.navigateTo('dashboard');

      // Check error message
      cy.get('[data-testid="error-message"]').should('contain', 'Network error');
      cy.checkNotification('Network error', 'error');
    });

    it('should handle authentication errors', () => {
      // Mock authentication error
      cy.mockApiError('/api/auth/me', 401, 'Unauthorized');

      cy.login();
      cy.navigateTo('dashboard');

      // Should redirect to login
      cy.url().should('include', '/login');
      cy.checkNotification('Your session has expired. Please log in again.', 'error');
    });
  });

  describe('Accessibility and Usability', () => {
    it('should be accessible and keyboard navigable', () => {
      cy.login();
      cy.navigateTo('dashboard');

      // Check accessibility
      cy.checkA11y();

      // Test keyboard navigation
      cy.checkKeyboardNavigation();

      // Test tab navigation
      cy.get('body').tab();
      cy.focused().should('be.visible');

      // Test escape key
      cy.get('body').type('{esc}');
    });

    it('should be responsive on different screen sizes', () => {
      cy.login();

      // Test mobile viewport
      cy.viewport(375, 667);
      cy.navigateTo('dashboard');
      cy.get('[data-testid="dashboard"]').should('be.visible');

      // Test tablet viewport
      cy.viewport(768, 1024);
      cy.navigateTo('dashboard');
      cy.get('[data-testid="dashboard"]').should('be.visible');

      // Test desktop viewport
      cy.viewport(1280, 720);
      cy.navigateTo('dashboard');
      cy.get('[data-testid="dashboard"]').should('be.visible');
    });
  });
});
