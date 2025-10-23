/**
 * Tests for useVirtualization hooks
 */

import { renderHook, act } from '@testing-library/react';
import { 
  useVirtualization,
  useInfiniteScroll,
  useWindowing,
  useLazyLoading,
  usePerformanceMonitor,
  useMemoryOptimization
} from '../useVirtualization';

describe('useVirtualization', () => {
  describe('useVirtualization', () => {
    it('should calculate visible items', () => {
      const items = Array.from({ length: 100 }, (_, i) => i);
      
      const { result } = renderHook(() => 
        useVirtualization(items, {
          itemHeight: 50,
          containerHeight: 200,
          overscan: 5
        })
      );
      
      expect(result.current.visibleItems).toHaveLength(9); // 4 visible + 5 overscan
      expect(result.current.startIndex).toBe(0);
      expect(result.current.endIndex).toBe(8);
      expect(result.current.totalHeight).toBe(5000);
      expect(result.current.offsetY).toBe(0);
    });

    it('should handle scroll position', () => {
      const items = Array.from({ length: 100 }, (_, i) => i);
      
      const { result } = renderHook(() => 
        useVirtualization(items, {
          itemHeight: 50,
          containerHeight: 200,
          overscan: 5
        })
      );
      
      // Simulate scroll
      act(() => {
        result.current.containerRef.current = {
          scrollTop: 100
        } as any;
      });
      
      expect(result.current.startIndex).toBe(2);
    });
  });

  describe('useInfiniteScroll', () => {
    it('should handle intersection', () => {
      const callback = jest.fn();
      
      const { result } = renderHook(() => 
        useInfiniteScroll(callback, true, 100)
      );
      
      expect(result.current.isLoading).toBe(false);
      expect(result.current.triggerRef.current).toBeNull();
    });

    it('should call callback on intersection', () => {
      const callback = jest.fn();
      
      const { result } = renderHook(() => 
        useInfiniteScroll(callback, true, 100)
      );
      
      // Simulate intersection
      act(() => {
        const mockEntry = {
          isIntersecting: true
        } as IntersectionObserverEntry;
        
        result.current.handleIntersection([mockEntry]);
      });
      
      expect(callback).toHaveBeenCalled();
    });
  });

  describe('useWindowing', () => {
    it('should calculate window items', () => {
      const items = Array.from({ length: 100 }, (_, i) => i);
      
      const { result } = renderHook(() => 
        useWindowing(items, {
          itemHeight: 50,
          containerHeight: 200,
          windowSize: 10
        })
      );
      
      expect(result.current.windowItems).toHaveLength(20); // 10 before + 10 after
      expect(result.current.startIndex).toBe(0);
      expect(result.current.endIndex).toBe(19);
      expect(result.current.totalHeight).toBe(5000);
    });
  });

  describe('useLazyLoading', () => {
    it('should load initial batch', () => {
      const items = Array.from({ length: 100 }, (_, i) => i);
      
      const { result } = renderHook(() => 
        useLazyLoading(items, 20, 100)
      );
      
      expect(result.current.loadedItems).toHaveLength(20);
      expect(result.current.isLoading).toBe(false);
      expect(result.current.hasMore).toBe(true);
    });

    it('should load more items on intersection', () => {
      const items = Array.from({ length: 100 }, (_, i) => i);
      
      const { result } = renderHook(() => 
        useLazyLoading(items, 20, 100)
      );
      
      // Simulate intersection
      act(() => {
        const mockEntry = {
          isIntersecting: true
        } as IntersectionObserverEntry;
        
        result.current.handleIntersection([mockEntry]);
      });
      
      expect(result.current.isLoading).toBe(true);
    });
  });

  describe('usePerformanceMonitor', () => {
    it('should track render count', () => {
      const { result } = renderHook(() => 
        usePerformanceMonitor('TestComponent')
      );
      
      expect(result.current.renderCount).toBe(1);
      
      act(() => {
        // Trigger re-render
        renderHook(() => usePerformanceMonitor('TestComponent'));
      });
      
      expect(result.current.renderCount).toBe(2);
    });
  });

  describe('useMemoryOptimization', () => {
    it('should manage cleanup functions', () => {
      const { result } = renderHook(() => useMemoryOptimization());
      
      const cleanup1 = jest.fn();
      const cleanup2 = jest.fn();
      
      act(() => {
        result.current.addCleanup(cleanup1);
        result.current.addCleanup(cleanup2);
      });
      
      act(() => {
        result.current.cleanup();
      });
      
      expect(cleanup1).toHaveBeenCalled();
      expect(cleanup2).toHaveBeenCalled();
    });
  });
});
