/**
 * Tests for useErrorHandler hook
 */

import { renderHook, act } from '@testing-library/react';
import { useErrorHandler } from '../useErrorHandler';
import { ErrorType, ErrorSeverity } from '../../utils/errorTypes';

// Mock the notification hook
jest.mock('../useNotification', () => ({
  useNotification: () => ({
    showNotification: jest.fn()
  })
}));

describe('useErrorHandler', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('should handle errors with default options', () => {
    const { result } = renderHook(() => useErrorHandler());
    
    const error = {
      code: 'NETWORK_ERROR',
      message: 'Network Error'
    };

    act(() => {
      const appError = result.current.handleError(error);
      expect(appError.type).toBe(ErrorType.NETWORK);
      expect(appError.severity).toBe(ErrorSeverity.HIGH);
    });
  });

  it('should handle async errors', async () => {
    const { result } = renderHook(() => useErrorHandler());
    
    const asyncFn = jest.fn().mockRejectedValue(new Error('Async error'));
    
    await act(async () => {
      const response = await result.current.handleAsyncError(asyncFn);
      expect(response).toBeNull();
    });
  });

  it('should handle API errors with context', () => {
    const { result } = renderHook(() => useErrorHandler());
    
    const error = {
      response: {
        status: 404,
        data: { message: 'Not found' }
      },
      config: {
        url: '/api/test',
        method: 'GET'
      }
    };

    act(() => {
      const appError = result.current.handleApiError(error, '/api/test', 'GET');
      expect(appError.type).toBe(ErrorType.API);
      expect(appError.status).toBe(404);
    });
  });

  it('should handle validation errors', () => {
    const { result } = renderHook(() => useErrorHandler());
    
    const error = {
      name: 'ValidationError',
      message: 'Invalid input'
    };

    act(() => {
      const appError = result.current.handleValidationError(error, 'email', 'invalid');
      expect(appError.type).toBe(ErrorType.VALIDATION);
      expect(appError.retryable).toBe(false);
    });
  });

  it('should handle network errors', () => {
    const { result } = renderHook(() => useErrorHandler());
    
    const error = {
      code: 'NETWORK_ERROR',
      message: 'Network Error'
    };

    act(() => {
      const appError = result.current.handleNetworkError(error, '/api/test');
      expect(appError.type).toBe(ErrorType.NETWORK);
      expect(appError.retryable).toBe(true);
    });
  });

  it('should clear errors', () => {
    const { result } = renderHook(() => useErrorHandler());
    
    act(() => {
      result.current.clearErrors();
      const stats = result.current.getErrorStats();
      expect(stats.errorCount).toBe(0);
      expect(stats.lastError).toBeNull();
    });
  });

  it('should provide retry logic', () => {
    const { result } = renderHook(() => useErrorHandler());
    
    const error = {
      type: ErrorType.NETWORK,
      severity: ErrorSeverity.HIGH,
      message: 'Network error',
      timestamp: new Date(),
      retryable: true
    };

    act(() => {
      const shouldRetry = result.current.shouldRetry(error, 1);
      expect(shouldRetry).toBe(true);
      
      const delay = result.current.getRetryDelay(error, 1);
      expect(delay).toBeGreaterThan(0);
    });
  });
});
