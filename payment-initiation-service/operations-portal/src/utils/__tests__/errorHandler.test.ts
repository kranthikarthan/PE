/**
 * Tests for error handling utilities
 */

import { errorHandler, ErrorHandler, ErrorType, ErrorSeverity } from '../errorHandler';
import { AppError, NetworkError, ApiError, ValidationError } from '../errorTypes';

describe('ErrorHandler', () => {
  let handler: ErrorHandler;

  beforeEach(() => {
    handler = new ErrorHandler({
      maxRetries: 3,
      retryDelay: 1000,
      enableLogging: false,
      enableReporting: false,
      enableUserNotifications: false
    });
  });

  describe('handleError', () => {
    it('should handle network errors', () => {
      const networkError = {
        code: 'NETWORK_ERROR',
        message: 'Network Error',
        config: {
          url: '/api/test',
          method: 'GET'
        }
      };

      const result = handler.handleError(networkError);
      
      expect(result.type).toBe(ErrorType.NETWORK);
      expect(result.severity).toBe(ErrorSeverity.HIGH);
      expect(result.retryable).toBe(true);
      expect(result.url).toBe('/api/test');
    });

    it('should handle API errors', () => {
      const apiError = {
        response: {
          status: 404,
          data: {
            message: 'Not found',
            code: 'RESOURCE_NOT_FOUND'
          }
        },
        config: {
          url: '/api/test',
          method: 'GET'
        }
      };

      const result = handler.handleError(apiError);
      
      expect(result.type).toBe(ErrorType.API);
      expect(result.severity).toBe(ErrorSeverity.MEDIUM);
      expect(result.status).toBe(404);
      expect(result.endpoint).toBe('/api/test');
    });

    it('should handle validation errors', () => {
      const validationError = {
        name: 'ValidationError',
        message: 'Invalid input',
        field: 'email',
        value: 'invalid-email'
      };

      const result = handler.handleError(validationError);
      
      expect(result.type).toBe(ErrorType.VALIDATION);
      expect(result.severity).toBe(ErrorSeverity.MEDIUM);
      expect(result.retryable).toBe(false);
    });

    it('should handle timeout errors', () => {
      const timeoutError = {
        code: 'ECONNABORTED',
        message: 'timeout of 5000ms exceeded',
        timeout: 5000
      };

      const result = handler.handleError(timeoutError);
      
      expect(result.type).toBe(ErrorType.TIMEOUT);
      expect(result.severity).toBe(ErrorSeverity.MEDIUM);
      expect(result.retryable).toBe(true);
    });

    it('should handle authentication errors', () => {
      const authError = {
        status: 401,
        message: 'Unauthorized',
        config: {
          url: '/api/auth',
          method: 'POST'
        }
      };

      const result = handler.handleError(authError);
      
      expect(result.type).toBe(ErrorType.AUTHENTICATION);
      expect(result.severity).toBe(ErrorSeverity.HIGH);
      expect(result.retryable).toBe(false);
    });

    it('should handle server errors', () => {
      const serverError = {
        status: 500,
        message: 'Internal Server Error',
        config: {
          url: '/api/test',
          method: 'POST'
        }
      };

      const result = handler.handleError(serverError);
      
      expect(result.type).toBe(ErrorType.SERVER);
      expect(result.severity).toBe(ErrorSeverity.HIGH);
      expect(result.retryable).toBe(true);
    });
  });

  describe('retry logic', () => {
    it('should determine if error should be retried', () => {
      const retryableError: AppError = {
        type: ErrorType.NETWORK,
        severity: ErrorSeverity.HIGH,
        message: 'Network error',
        timestamp: new Date(),
        retryable: true
      };

      const nonRetryableError: AppError = {
        type: ErrorType.VALIDATION,
        severity: ErrorSeverity.MEDIUM,
        message: 'Validation error',
        timestamp: new Date(),
        retryable: false
      };

      expect(handler.shouldRetry(retryableError, 1)).toBe(true);
      expect(handler.shouldRetry(nonRetryableError, 1)).toBe(false);
      expect(handler.shouldRetry(retryableError, 5)).toBe(false); // Exceeds max retries
    });

    it('should calculate retry delay with exponential backoff', () => {
      const error: AppError = {
        type: ErrorType.NETWORK,
        severity: ErrorSeverity.HIGH,
        message: 'Network error',
        timestamp: new Date(),
        retryable: true,
        retryAfter: 5
      };

      const delay1 = handler.getRetryDelay(error, 1);
      const delay2 = handler.getRetryDelay(error, 2);
      const delay3 = handler.getRetryDelay(error, 3);

      expect(delay2).toBeGreaterThan(delay1);
      expect(delay3).toBeGreaterThan(delay2);
    });
  });

  describe('error statistics', () => {
    it('should track error statistics', () => {
      const error1: AppError = {
        type: ErrorType.NETWORK,
        severity: ErrorSeverity.HIGH,
        message: 'Network error',
        timestamp: new Date(),
        retryable: true
      };

      const error2: AppError = {
        type: ErrorType.API,
        severity: ErrorSeverity.MEDIUM,
        message: 'API error',
        timestamp: new Date(),
        retryable: false
      };

      handler.handleError(error1);
      handler.handleError(error2);

      const stats = handler.getErrorStats();
      
      expect(stats.total).toBe(2);
      expect(stats.byType[ErrorType.NETWORK]).toBe(1);
      expect(stats.byType[ErrorType.API]).toBe(1);
      expect(stats.bySeverity[ErrorSeverity.HIGH]).toBe(1);
      expect(stats.bySeverity[ErrorSeverity.MEDIUM]).toBe(1);
    });

    it('should clear error queue', () => {
      const error: AppError = {
        type: ErrorType.NETWORK,
        severity: ErrorSeverity.HIGH,
        message: 'Network error',
        timestamp: new Date(),
        retryable: true
      };

      handler.handleError(error);
      expect(handler.getErrorStats().total).toBe(1);

      handler.clearErrorQueue();
      expect(handler.getErrorStats().total).toBe(0);
    });
  });
});

describe('errorHandler singleton', () => {
  it('should be a singleton instance', () => {
    expect(errorHandler).toBeInstanceOf(ErrorHandler);
  });

  it('should handle errors consistently', () => {
    const error = {
      code: 'NETWORK_ERROR',
      message: 'Network Error'
    };

    const result = errorHandler.handleError(error);
    expect(result.type).toBe(ErrorType.NETWORK);
    expect(result.severity).toBe(ErrorSeverity.HIGH);
  });
});
