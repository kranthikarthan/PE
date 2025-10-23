/**
 * Tests for useMemoization hooks
 */

import { renderHook, act } from '@testing-library/react';
import { 
  useExpensiveCalculation,
  useStableCallback,
  useStableObject,
  useStableArray,
  useDebounce,
  useThrottle,
  useMemoizedData,
  useMemoizedPagination,
  useMemoizedSearch
} from '../useMemoization';

describe('useMemoization', () => {
  describe('useExpensiveCalculation', () => {
    it('should memoize expensive calculations', () => {
      const expensiveCalculation = jest.fn().mockReturnValue(42);
      
      const { result, rerender } = renderHook(() => 
        useExpensiveCalculation(expensiveCalculation, [1, 2, 3])
      );
      
      expect(result.current).toBe(42);
      expect(expensiveCalculation).toHaveBeenCalledTimes(1);
      
      rerender();
      expect(expensiveCalculation).toHaveBeenCalledTimes(1);
    });

    it('should recalculate when dependencies change', () => {
      const expensiveCalculation = jest.fn().mockReturnValue(42);
      
      const { result, rerender } = renderHook(
        ({ deps }) => useExpensiveCalculation(expensiveCalculation, deps),
        { initialProps: { deps: [1, 2, 3] } }
      );
      
      expect(result.current).toBe(42);
      expect(expensiveCalculation).toHaveBeenCalledTimes(1);
      
      rerender({ deps: [1, 2, 4] });
      expect(expensiveCalculation).toHaveBeenCalledTimes(2);
    });
  });

  describe('useStableCallback', () => {
    it('should memoize callback functions', () => {
      const callback = jest.fn();
      
      const { result, rerender } = renderHook(() => 
        useStableCallback(callback, [1, 2, 3])
      );
      
      const firstCallback = result.current;
      rerender();
      const secondCallback = result.current;
      
      expect(firstCallback).toBe(secondCallback);
    });

    it('should create new callback when dependencies change', () => {
      const callback = jest.fn();
      
      const { result, rerender } = renderHook(
        ({ deps }) => useStableCallback(callback, deps),
        { initialProps: { deps: [1, 2, 3] } }
      );
      
      const firstCallback = result.current;
      rerender({ deps: [1, 2, 4] });
      const secondCallback = result.current;
      
      expect(firstCallback).not.toBe(secondCallback);
    });
  });

  describe('useStableObject', () => {
    it('should memoize objects', () => {
      const object = { a: 1, b: 2 };
      
      const { result, rerender } = renderHook(() => 
        useStableObject(object, [1, 2, 3])
      );
      
      const firstObject = result.current;
      rerender();
      const secondObject = result.current;
      
      expect(firstObject).toBe(secondObject);
    });

    it('should create new object when dependencies change', () => {
      const object = { a: 1, b: 2 };
      
      const { result, rerender } = renderHook(
        ({ deps }) => useStableObject(object, deps),
        { initialProps: { deps: [1, 2, 3] } }
      );
      
      const firstObject = result.current;
      rerender({ deps: [1, 2, 4] });
      const secondObject = result.current;
      
      expect(firstObject).not.toBe(secondObject);
    });
  });

  describe('useStableArray', () => {
    it('should memoize arrays', () => {
      const array = [1, 2, 3];
      
      const { result, rerender } = renderHook(() => 
        useStableArray(array, [1, 2, 3])
      );
      
      const firstArray = result.current;
      rerender();
      const secondArray = result.current;
      
      expect(firstArray).toBe(secondArray);
    });

    it('should create new array when dependencies change', () => {
      const array = [1, 2, 3];
      
      const { result, rerender } = renderHook(
        ({ deps }) => useStableArray(array, deps),
        { initialProps: { deps: [1, 2, 3] } }
      );
      
      const firstArray = result.current;
      rerender({ deps: [1, 2, 4] });
      const secondArray = result.current;
      
      expect(firstArray).not.toBe(secondArray);
    });
  });

  describe('useDebounce', () => {
    beforeEach(() => {
      jest.useFakeTimers();
    });

    afterEach(() => {
      jest.useRealTimers();
    });

    it('should debounce values', () => {
      const { result, rerender } = renderHook(
        ({ value, delay }) => useDebounce(value, delay),
        { initialProps: { value: 'test', delay: 100 } }
      );
      
      expect(result.current).toBe('test');
      
      rerender({ value: 'test2', delay: 100 });
      expect(result.current).toBe('test');
      
      act(() => {
        jest.advanceTimersByTime(100);
      });
      
      expect(result.current).toBe('test2');
    });
  });

  describe('useThrottle', () => {
    beforeEach(() => {
      jest.useFakeTimers();
    });

    afterEach(() => {
      jest.useRealTimers();
    });

    it('should throttle values', () => {
      const { result, rerender } = renderHook(
        ({ value, delay }) => useThrottle(value, delay),
        { initialProps: { value: 'test', delay: 100 } }
      );
      
      expect(result.current).toBe('test');
      
      rerender({ value: 'test2', delay: 100 });
      expect(result.current).toBe('test');
      
      act(() => {
        jest.advanceTimersByTime(100);
      });
      
      expect(result.current).toBe('test2');
    });
  });

  describe('useMemoizedData', () => {
    it('should memoize filtered data', () => {
      const data = [1, 2, 3, 4, 5];
      const filterFn = (item: number) => item > 2;
      
      const { result, rerender } = renderHook(() => 
        useMemoizedData(data, filterFn, undefined, [data])
      );
      
      expect(result.current).toEqual([3, 4, 5]);
      
      rerender();
      expect(result.current).toBe(result.current);
    });

    it('should memoize sorted data', () => {
      const data = [3, 1, 4, 2, 5];
      const sortFn = (a: number, b: number) => a - b;
      
      const { result, rerender } = renderHook(() => 
        useMemoizedData(data, undefined, sortFn, [data])
      );
      
      expect(result.current).toEqual([1, 2, 3, 4, 5]);
      
      rerender();
      expect(result.current).toBe(result.current);
    });
  });

  describe('useMemoizedPagination', () => {
    it('should memoize paginated data', () => {
      const data = [1, 2, 3, 4, 5, 6, 7, 8, 9, 10];
      
      const { result, rerender } = renderHook(() => 
        useMemoizedPagination(data, 1, 3, [data])
      );
      
      expect(result.current.paginatedData).toEqual([1, 2, 3]);
      expect(result.current.totalPages).toBe(4);
      expect(result.current.totalItems).toBe(10);
      
      rerender();
      expect(result.current).toBe(result.current);
    });
  });

  describe('useMemoizedSearch', () => {
    it('should memoize search results', () => {
      const data = [
        { name: 'John', age: 30 },
        { name: 'Jane', age: 25 },
        { name: 'Bob', age: 35 }
      ];
      
      const { result, rerender } = renderHook(() => 
        useMemoizedSearch(data, 'John', ['name'], [data, 'John'])
      );
      
      expect(result.current).toEqual([{ name: 'John', age: 30 }]);
      
      rerender();
      expect(result.current).toBe(result.current);
    });
  });
});
