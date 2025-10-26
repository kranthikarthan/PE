/**
 * Tests for useTimeout hook
 */

import { renderHook, act } from '@testing-library/react';
import { useTimeout } from '../useTimeout';
import { ErrorType, ErrorSeverity } from '../../utils/errorTypes';

// Mock the error handler hook
jest.mock('../useErrorHandler', () => ({
  useErrorHandler: () => ({
    handleError: jest.fn()
  })
}));

describe('useTimeout', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    jest.useFakeTimers();
  });

  afterEach(() => {
    jest.useRealTimers();
  });

  it('should create timeout', () => {
    const { result } = renderHook(() => useTimeout());
    
    act(() => {
      const timeoutPromise = result.current.createTimeout('test-operation', 1000);
      expect(result.current.activeTimeouts).toBe(1);
    });
  });

  it('should execute function with timeout', async () => {
    const { result } = renderHook(() => useTimeout());
    
    const asyncFn = jest.fn().mockResolvedValue('success');
    
    await act(async () => {
      const response = await result.current.withTimeout(asyncFn, 'test-operation', 1000);
      expect(response).toBe('success');
    });
  });

  it('should timeout slow operations', async () => {
    const { result } = renderHook(() => useTimeout({
      defaultTimeout: 100
    }));
    
    const asyncFn = jest.fn().mockImplementation(() => 
      new Promise(resolve => setTimeout(resolve, 200))
    );
    
    await act(async () => {
      try {
        await result.current.withTimeout(asyncFn, 'slow-operation', 100);
      } catch (error) {
        expect(error).toBeDefined();
      }
    });
  });

  it('should clear specific timeout', () => {
    const { result } = renderHook(() => useTimeout());
    
    act(() => {
      result.current.createTimeout('test-operation', 1000);
      expect(result.current.activeTimeouts).toBe(1);
      
      result.current.clearTimeout('test-operation');
      expect(result.current.activeTimeouts).toBe(0);
    });
  });

  it('should clear all timeouts', () => {
    const { result } = renderHook(() => useTimeout());
    
    act(() => {
      result.current.createTimeout('operation1', 1000);
      result.current.createTimeout('operation2', 1000);
      expect(result.current.activeTimeouts).toBe(2);
      
      result.current.clearAllTimeouts();
      expect(result.current.activeTimeouts).toBe(0);
    });
  });

  it('should get active timeouts', () => {
    const { result } = renderHook(() => useTimeout());
    
    act(() => {
      result.current.createTimeout('operation1', 1000);
      result.current.createTimeout('operation2', 1000);
      
      const activeTimeouts = result.current.getActiveTimeouts();
      expect(activeTimeouts).toHaveLength(2);
    });
  });

  it('should call onTimeout callback', () => {
    const onTimeout = jest.fn();
    const { result } = renderHook(() => useTimeout({
      onTimeout,
      defaultTimeout: 100
    }));
    
    act(() => {
      result.current.createTimeout('test-operation', 100);
    });
    
    act(() => {
      jest.advanceTimersByTime(100);
    });
    
    expect(onTimeout).toHaveBeenCalledWith(100, 'test-operation');
  });

  it('should handle timeout with custom options', async () => {
    const { result } = renderHook(() => useTimeout({
      defaultTimeout: 1000
    }));
    
    const asyncFn = jest.fn().mockResolvedValue('success');
    
    await act(async () => {
      const response = await result.current.withTimeout(asyncFn, 'test-operation', 500);
      expect(response).toBe('success');
    });
  });

  it('should track multiple timeouts', () => {
    const { result } = renderHook(() => useTimeout());
    
    act(() => {
      result.current.createTimeout('operation1', 1000);
      result.current.createTimeout('operation2', 2000);
      result.current.createTimeout('operation3', 3000);
      
      expect(result.current.activeTimeouts).toBe(3);
    });
  });
});
