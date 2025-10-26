/**
 * Tests for useRetry hook
 */

import { renderHook, act } from '@testing-library/react';
import { useRetry } from '../useRetry';
import { ErrorType, ErrorSeverity } from '../../utils/errorTypes';

// Mock the error handler hook
jest.mock('../useErrorHandler', () => ({
  useErrorHandler: () => ({
    shouldRetry: jest.fn().mockReturnValue(true),
    getRetryDelay: jest.fn().mockReturnValue(1000)
  })
}));

describe('useRetry', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('should execute function successfully on first try', async () => {
    const { result } = renderHook(() => useRetry());
    
    const asyncFn = jest.fn().mockResolvedValue('success');
    
    await act(async () => {
      const response = await result.current.executeWithRetry(asyncFn);
      expect(response).toBe('success');
      expect(asyncFn).toHaveBeenCalledTimes(1);
    });
  });

  it('should retry on failure', async () => {
    const { result } = renderHook(() => useRetry({
      maxRetries: 2,
      baseDelay: 100
    }));
    
    const asyncFn = jest.fn()
      .mockRejectedValueOnce(new Error('First failure'))
      .mockRejectedValueOnce(new Error('Second failure'))
      .mockResolvedValueOnce('success');
    
    await act(async () => {
      const response = await result.current.executeWithRetry(asyncFn);
      expect(response).toBe('success');
      expect(asyncFn).toHaveBeenCalledTimes(3);
    });
  });

  it('should fail after max retries', async () => {
    const { result } = renderHook(() => useRetry({
      maxRetries: 2,
      baseDelay: 100
    }));
    
    const asyncFn = jest.fn().mockRejectedValue(new Error('Persistent failure'));
    
    await act(async () => {
      const response = await result.current.executeWithRetry(asyncFn);
      expect(response).toBeNull();
      expect(asyncFn).toHaveBeenCalledTimes(3); // Initial + 2 retries
    });
  });

  it('should call onRetry callback', async () => {
    const onRetry = jest.fn();
    const { result } = renderHook(() => useRetry({
      onRetry,
      maxRetries: 1,
      baseDelay: 100
    }));
    
    const asyncFn = jest.fn()
      .mockRejectedValueOnce(new Error('First failure'))
      .mockResolvedValueOnce('success');
    
    await act(async () => {
      await result.current.executeWithRetry(asyncFn);
      expect(onRetry).toHaveBeenCalledWith(1, expect.any(Error));
    });
  });

  it('should call onMaxRetriesReached callback', async () => {
    const onMaxRetriesReached = jest.fn();
    const { result } = renderHook(() => useRetry({
      onMaxRetriesReached,
      maxRetries: 1,
      baseDelay: 100
    }));
    
    const asyncFn = jest.fn().mockRejectedValue(new Error('Persistent failure'));
    
    await act(async () => {
      await result.current.executeWithRetry(asyncFn);
      expect(onMaxRetriesReached).toHaveBeenCalledWith(expect.any(Error));
    });
  });

  it('should track retry state', async () => {
    const { result } = renderHook(() => useRetry({
      maxRetries: 2,
      baseDelay: 100
    }));
    
    const asyncFn = jest.fn()
      .mockRejectedValueOnce(new Error('First failure'))
      .mockResolvedValueOnce('success');
    
    await act(async () => {
      expect(result.current.isRetrying).toBe(false);
      expect(result.current.attempt).toBe(0);
      
      await result.current.executeWithRetry(asyncFn);
      
      expect(result.current.isRetrying).toBe(false);
      expect(result.current.attempt).toBe(0);
    });
  });

  it('should cancel retry', () => {
    const { result } = renderHook(() => useRetry());
    
    act(() => {
      result.current.cancelRetry();
      expect(result.current.isRetrying).toBe(false);
    });
  });

  it('should reset retry state', () => {
    const { result } = renderHook(() => useRetry());
    
    act(() => {
      result.current.resetRetry();
      expect(result.current.isRetrying).toBe(false);
      expect(result.current.attempt).toBe(0);
      expect(result.current.lastError).toBeNull();
      expect(result.current.totalRetries).toBe(0);
    });
  });

  it('should use custom retry options', async () => {
    const { result } = renderHook(() => useRetry({
      maxRetries: 1,
      baseDelay: 200
    }));
    
    const asyncFn = jest.fn()
      .mockRejectedValueOnce(new Error('First failure'))
      .mockResolvedValueOnce('success');
    
    await act(async () => {
      const response = await result.current.executeWithRetry(asyncFn, {
        maxRetries: 3
      });
      expect(response).toBe('success');
      expect(asyncFn).toHaveBeenCalledTimes(2);
    });
  });
});
