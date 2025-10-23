/**
 * Custom hooks for React optimization with memoization
 */

import { useMemo, useCallback, useRef, useEffect } from 'react';

/**
 * Hook for memoizing expensive calculations
 */
export const useExpensiveCalculation = <T>(
  calculation: () => T,
  dependencies: React.DependencyList
): T => {
  return useMemo(calculation, dependencies);
};

/**
 * Hook for memoizing callback functions
 */
export const useStableCallback = <T extends (...args: any[]) => any>(
  callback: T,
  dependencies: React.DependencyList
): T => {
  return useCallback(callback, dependencies);
};

/**
 * Hook for memoizing objects to prevent unnecessary re-renders
 */
export const useStableObject = <T extends Record<string, any>>(
  object: T,
  dependencies: React.DependencyList
): T => {
  return useMemo(() => object, dependencies);
};

/**
 * Hook for memoizing arrays to prevent unnecessary re-renders
 */
export const useStableArray = <T>(
  array: T[],
  dependencies: React.DependencyList
): T[] => {
  return useMemo(() => array, dependencies);
};

/**
 * Hook for debouncing values
 */
export const useDebounce = <T>(value: T, delay: number): T => {
  const [debouncedValue, setDebouncedValue] = React.useState<T>(value);

  useEffect(() => {
    const handler = setTimeout(() => {
      setDebouncedValue(value);
    }, delay);

    return () => {
      clearTimeout(handler);
    };
  }, [value, delay]);

  return debouncedValue;
};

/**
 * Hook for throttling values
 */
export const useThrottle = <T>(value: T, delay: number): T => {
  const [throttledValue, setThrottledValue] = React.useState<T>(value);
  const lastExecuted = useRef<number>(Date.now());

  useEffect(() => {
    if (Date.now() >= lastExecuted.current + delay) {
      lastExecuted.current = Date.now();
      setThrottledValue(value);
    } else {
      const timer = setTimeout(() => {
        lastExecuted.current = Date.now();
        setThrottledValue(value);
      }, delay);

      return () => clearTimeout(timer);
    }
  }, [value, delay]);

  return throttledValue;
};

/**
 * Hook for memoizing API responses
 */
export const useMemoizedApiResponse = <T>(
  data: T,
  dependencies: React.DependencyList
): T => {
  return useMemo(() => data, dependencies);
};

/**
 * Hook for memoizing filtered/sorted data
 */
export const useMemoizedData = <T>(
  data: T[],
  filterFn?: (item: T) => boolean,
  sortFn?: (a: T, b: T) => number,
  dependencies: React.DependencyList = []
): T[] => {
  return useMemo(() => {
    let result = data;
    
    if (filterFn) {
      result = result.filter(filterFn);
    }
    
    if (sortFn) {
      result = result.sort(sortFn);
    }
    
    return result;
  }, [data, filterFn, sortFn, ...dependencies]);
};

/**
 * Hook for memoizing paginated data
 */
export const useMemoizedPagination = <T>(
  data: T[],
  page: number,
  pageSize: number,
  dependencies: React.DependencyList = []
): { paginatedData: T[]; totalPages: number; totalItems: number } => {
  return useMemo(() => {
    const totalItems = data.length;
    const totalPages = Math.ceil(totalItems / pageSize);
    const startIndex = (page - 1) * pageSize;
    const endIndex = startIndex + pageSize;
    const paginatedData = data.slice(startIndex, endIndex);
    
    return {
      paginatedData,
      totalPages,
      totalItems
    };
  }, [data, page, pageSize, ...dependencies]);
};

/**
 * Hook for memoizing search results
 */
export const useMemoizedSearch = <T>(
  data: T[],
  searchTerm: string,
  searchFields: (keyof T)[],
  dependencies: React.DependencyList = []
): T[] => {
  return useMemo(() => {
    if (!searchTerm.trim()) {
      return data;
    }
    
    const lowerSearchTerm = searchTerm.toLowerCase();
    
    return data.filter(item => 
      searchFields.some(field => {
        const value = item[field];
        if (typeof value === 'string') {
          return value.toLowerCase().includes(lowerSearchTerm);
        }
        if (typeof value === 'number') {
          return value.toString().includes(lowerSearchTerm);
        }
        return false;
      })
    );
  }, [data, searchTerm, searchFields, ...dependencies]);
};

/**
 * Hook for memoizing computed values
 */
export const useMemoizedComputation = <T>(
  computation: () => T,
  dependencies: React.DependencyList
): T => {
  return useMemo(computation, dependencies);
};

/**
 * Hook for memoizing event handlers
 */
export const useMemoizedEventHandler = <T extends (...args: any[]) => any>(
  handler: T,
  dependencies: React.DependencyList
): T => {
  return useCallback(handler, dependencies);
};

/**
 * Hook for memoizing refs
 */
export const useMemoizedRef = <T>(value: T): React.MutableRefObject<T> => {
  const ref = useRef<T>(value);
  ref.current = value;
  return ref;
};
