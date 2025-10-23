/**
 * Cypress Configuration
 * 
 * E2E testing configuration for the React Operations Portal.
 * Sets up Cypress for comprehensive end-to-end testing.
 */

import { defineConfig } from 'cypress';

export default defineConfig({
  e2e: {
    baseUrl: 'http://localhost:3000',
    supportFile: 'cypress/support/e2e.ts',
    specPattern: 'cypress/e2e/**/*.cy.{js,jsx,ts,tsx}',
    viewportWidth: 1280,
    viewportHeight: 720,
    video: true,
    screenshotOnRunFailure: true,
    defaultCommandTimeout: 10000,
    requestTimeout: 10000,
    responseTimeout: 10000,
    setupNodeEvents(on, config) {
      // Implement node event listeners here
    },
  },
  component: {
    devServer: {
      framework: 'create-react-app',
      bundler: 'webpack',
    },
    supportFile: 'cypress/support/component.ts',
    specPattern: 'cypress/component/**/*.cy.{js,jsx,ts,tsx}',
  },
  env: {
    // Environment variables for testing
    API_BASE_URL: 'http://localhost:8080/api',
    AUTH_API_URL: 'http://localhost:8080/auth',
    OPERATIONS_MANAGEMENT_API_URL: 'http://localhost:8021/api/ops/v1',
    PAYMENT_INITIATION_API_URL: 'http://localhost:8080/api/payments/v1',
    TRANSACTION_PROCESSING_API_URL: 'http://localhost:8080/api/transactions/v1',
    RECONCILIATION_API_URL: 'http://localhost:8080/api/reconciliation/v1',
    IAM_API_URL: 'http://localhost:8080/api/iam/v1',
  },
});
