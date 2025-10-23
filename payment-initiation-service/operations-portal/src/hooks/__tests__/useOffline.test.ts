/**
 * Tests for useOffline hook
 */

import { renderHook, act } from '@testing-library/react';
import { useOffline } from '../useOffline';

// Mock the notification hook
jest.mock('../useNotification', () => ({
  useNotification: () => ({
    showNotification: jest.fn()
  })
}));

// Mock fetch
global.fetch = jest.fn();

describe('useOffline', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    // Reset navigator.onLine
    Object.defineProperty(navigator, 'onLine', {
      writable: true,
      value: true
    });
  });

  it('should detect online status', () => {
    const { result } = renderHook(() => useOffline());
    
    expect(result.current.isOnline).toBe(true);
    expect(result.current.isOffline).toBe(false);
    expect(result.current.wasOffline).toBe(false);
  });

  it('should detect offline status', () => {
    Object.defineProperty(navigator, 'onLine', {
      writable: true,
      value: false
    });

    const { result } = renderHook(() => useOffline());
    
    expect(result.current.isOnline).toBe(false);
    expect(result.current.isOffline).toBe(true);
  });

  it('should handle online event', () => {
    const { result } = renderHook(() => useOffline());
    
    // Simulate going offline
    act(() => {
      Object.defineProperty(navigator, 'onLine', {
        writable: true,
        value: false
      });
      window.dispatchEvent(new Event('offline'));
    });
    
    expect(result.current.isOffline).toBe(true);
    expect(result.current.wasOffline).toBe(true);
    
    // Simulate going back online
    act(() => {
      Object.defineProperty(navigator, 'onLine', {
        writable: true,
        value: true
      });
      window.dispatchEvent(new Event('online'));
    });
    
    expect(result.current.isOnline).toBe(true);
    expect(result.current.wasOffline).toBe(false);
  });

  it('should call onOnline callback', () => {
    const onOnline = jest.fn();
    const { result } = renderHook(() => useOffline({ onOnline }));
    
    act(() => {
      Object.defineProperty(navigator, 'onLine', {
        writable: true,
        value: false
      });
      window.dispatchEvent(new Event('offline'));
    });
    
    act(() => {
      Object.defineProperty(navigator, 'onLine', {
        writable: true,
        value: true
      });
      window.dispatchEvent(new Event('online'));
    });
    
    expect(onOnline).toHaveBeenCalled();
  });

  it('should call onOffline callback', () => {
    const onOffline = jest.fn();
    const { result } = renderHook(() => useOffline({ onOffline }));
    
    act(() => {
      Object.defineProperty(navigator, 'onLine', {
        writable: true,
        value: false
      });
      window.dispatchEvent(new Event('offline'));
    });
    
    expect(onOffline).toHaveBeenCalled();
  });

  it('should handle connectivity check', async () => {
    (global.fetch as jest.Mock).mockResolvedValue({
      ok: true
    });

    const { result } = renderHook(() => useOffline({
      checkInterval: 100
    }));
    
    // Wait for connectivity check
    await act(async () => {
      await new Promise(resolve => setTimeout(resolve, 150));
    });
    
    expect(global.fetch).toHaveBeenCalledWith('/api/health', {
      method: 'HEAD',
      cache: 'no-cache',
      signal: expect.any(AbortSignal)
    });
  });

  it('should handle connectivity check failure', async () => {
    (global.fetch as jest.Mock).mockRejectedValue(new Error('Network error'));

    const { result } = renderHook(() => useOffline({
      checkInterval: 100
    }));
    
    // Wait for connectivity check
    await act(async () => {
      await new Promise(resolve => setTimeout(resolve, 150));
    });
    
    expect(result.current.isOffline).toBe(true);
  });

  it('should disable notifications', () => {
    const { result } = renderHook(() => useOffline({
      enableNotifications: false
    }));
    
    act(() => {
      Object.defineProperty(navigator, 'onLine', {
        writable: true,
        value: false
      });
      window.dispatchEvent(new Event('offline'));
    });
    
    expect(result.current.isOffline).toBe(true);
  });
});
