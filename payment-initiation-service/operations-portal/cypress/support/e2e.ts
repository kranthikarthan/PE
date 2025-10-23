/**
 * Cypress E2E Support
 * 
 * Global support file for E2E tests.
 * Sets up commands, fixtures, and test utilities.
 */

// Import commands.js using ES2015 syntax:
import './commands';

// Alternatively you can use CommonJS syntax:
// require('./commands')

// Import MSW for API mocking
import { server } from '../../src/mocks/server';

// Start MSW server before all tests
before(() => {
  server.listen();
});

// Reset MSW handlers after each test
afterEach(() => {
  server.resetHandlers();
});

// Clean up MSW server after all tests
after(() => {
  server.close();
});

// Global test configuration
Cypress.on('uncaught:exception', (err, runnable) => {
  // Prevent Cypress from failing on uncaught exceptions
  // that are not related to the application
  if (err.message.includes('ResizeObserver loop limit exceeded')) {
    return false;
  }
  return true;
});

// Configure viewport
Cypress.on('window:before:load', (win) => {
  // Mock matchMedia
  Object.defineProperty(win, 'matchMedia', {
    writable: true,
    value: cy.stub().returns({
      matches: false,
      media: '',
      onchange: null,
      addListener: cy.stub(),
      removeListener: cy.stub(),
      addEventListener: cy.stub(),
      removeEventListener: cy.stub(),
      dispatchEvent: cy.stub(),
    }),
  });

  // Mock IntersectionObserver
  Object.defineProperty(win, 'IntersectionObserver', {
    writable: true,
    value: cy.stub().returns({
      observe: cy.stub(),
      unobserve: cy.stub(),
      disconnect: cy.stub(),
    }),
  });

  // Mock ResizeObserver
  Object.defineProperty(win, 'ResizeObserver', {
    writable: true,
    value: cy.stub().returns({
      observe: cy.stub(),
      unobserve: cy.stub(),
      disconnect: cy.stub(),
    }),
  });
});
