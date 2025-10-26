/**
 * MSW Browser Configuration
 * 
 * Mock Service Worker for browser environment.
 * Provides API mocking for development and testing.
 */

import { setupWorker } from 'msw/browser';
import { handlers } from './handlers';

// Setup MSW worker for browser environment
export const worker = setupWorker(...handlers);
