/**
 * MSW Server Configuration
 * 
 * Mock Service Worker server for API mocking in tests and development.
 * Provides realistic API responses for all microservices.
 */

import { setupServer } from 'msw/node';
import { handlers } from './handlers';

// Setup MSW server with all handlers
export const server = setupServer(...handlers);
