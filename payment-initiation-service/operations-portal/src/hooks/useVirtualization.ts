/**
 * Custom hooks for virtualization and performance optimization
 */

import { useState, useEffect, useMemo, useCallback, useRef } from 'react';

interface VirtualizationOptions {
  itemHeight: number;
  containerHeight: number;
  overscan?: number;
  threshold?: number;
}

interface VirtualizationResult<T> {
  visibleItems: T[];
  startIndex: number;
  endIndex: number;
  totalHeight: number;
  offsetY: number;
}

/**
 * Hook for virtualizing large lists
 */
export const useVirtualization = <T>(
  items: T[],
  options: VirtualizationOptions
): VirtualizationResult<T> => {
  const { itemHeight, containerHeight, overscan = 5, threshold = 100 } = options;
  
  const [scrollTop, setScrollTop] = useState(0);
  const containerRef = useRef<HTMLDivElement>(null);

  const visibleCount = Math.ceil(containerHeight / itemHeight);
  const startIndex = Math.max(0, Math.floor(scrollTop / itemHeight) - overscan);
  const endIndex = Math.min(items.length - 1, startIndex + visibleCount + overscan * 2);
  
  const visibleItems = items.slice(startIndex, endIndex + 1);
  const totalHeight = items.length * itemHeight;
  const offsetY = startIndex * itemHeight;

  const handleScroll = useCallback((event: Event) => {
    const target = event.target as HTMLDivElement;
    setScrollTop(target.scrollTop);
  }, []);

  useEffect(() => {
    const container = containerRef.current;
    if (container) {
      container.addEventListener('scroll', handleScroll, { passive: true });
      return () => container.removeEventListener('scroll', handleScroll);
    }
  }, [handleScroll]);

  return {
    visibleItems,
    startIndex,
    endIndex,
    totalHeight,
    offsetY,
    containerRef
  };
};

/**
 * Hook for infinite scrolling
 */
export const useInfiniteScroll = (
  callback: () => void,
  hasMore: boolean,
  threshold: number = 100
) => {
  const [isLoading, setIsLoading] = useState(false);
  const observerRef = useRef<IntersectionObserver | null>(null);
  const triggerRef = useRef<HTMLDivElement>(null);

  const handleIntersection = useCallback((entries: IntersectionObserverEntry[]) => {
    const target = entries[0];
    if (target.isIntersecting && hasMore && !isLoading) {
      setIsLoading(true);
      callback();
    }
  }, [callback, hasMore, isLoading]);

  useEffect(() => {
    if (triggerRef.current) {
      observerRef.current = new IntersectionObserver(handleIntersection, {
        threshold: 0.1,
        rootMargin: `${threshold}px`
      });
      
      observerRef.current.observe(triggerRef.current);
    }

    return () => {
      if (observerRef.current) {
        observerRef.current.disconnect();
      }
    };
  }, [handleIntersection, threshold]);

  useEffect(() => {
    if (isLoading) {
      const timer = setTimeout(() => setIsLoading(false), 1000);
      return () => clearTimeout(timer);
    }
  }, [isLoading]);

  return { triggerRef, isLoading };
};

/**
 * Hook for windowing (virtual scrolling with window)
 */
export const useWindowing = <T>(
  items: T[],
  options: VirtualizationOptions & { windowSize?: number }
) => {
  const { itemHeight, containerHeight, windowSize = 10 } = options;
  
  const [scrollTop, setScrollTop] = useState(0);
  const [windowStart, setWindowStart] = useState(0);
  
  const visibleCount = Math.ceil(containerHeight / itemHeight);
  const startIndex = Math.max(0, Math.floor(scrollTop / itemHeight));
  const endIndex = Math.min(items.length - 1, startIndex + visibleCount);
  
  const windowStartIndex = Math.max(0, startIndex - windowSize);
  const windowEndIndex = Math.min(items.length - 1, endIndex + windowSize);
  
  const windowItems = items.slice(windowStartIndex, windowEndIndex + 1);
  const totalHeight = items.length * itemHeight;
  const offsetY = windowStartIndex * itemHeight;

  const handleScroll = useCallback((event: Event) => {
    const target = event.target as HTMLDivElement;
    setScrollTop(target.scrollTop);
  }, []);

  return {
    windowItems,
    startIndex: windowStartIndex,
    endIndex: windowEndIndex,
    totalHeight,
    offsetY,
    handleScroll
  };
};

/**
 * Hook for lazy loading with intersection observer
 */
export const useLazyLoading = <T>(
  items: T[],
  batchSize: number = 20,
  threshold: number = 100
) => {
  const [loadedItems, setLoadedItems] = useState<T[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const [hasMore, setHasMore] = useState(true);
  const observerRef = useRef<IntersectionObserver | null>(null);
  const triggerRef = useRef<HTMLDivElement>(null);

  const loadMore = useCallback(() => {
    if (isLoading || !hasMore) return;
    
    setIsLoading(true);
    
    setTimeout(() => {
      const currentLength = loadedItems.length;
      const nextBatch = items.slice(currentLength, currentLength + batchSize);
      
      setLoadedItems(prev => [...prev, ...nextBatch]);
      setHasMore(currentLength + batchSize < items.length);
      setIsLoading(false);
    }, 300);
  }, [items, loadedItems.length, batchSize, isLoading, hasMore]);

  const handleIntersection = useCallback((entries: IntersectionObserverEntry[]) => {
    const target = entries[0];
    if (target.isIntersecting && hasMore && !isLoading) {
      loadMore();
    }
  }, [hasMore, isLoading, loadMore]);

  useEffect(() => {
    if (triggerRef.current) {
      observerRef.current = new IntersectionObserver(handleIntersection, {
        threshold: 0.1,
        rootMargin: `${threshold}px`
      });
      
      observerRef.current.observe(triggerRef.current);
    }

    return () => {
      if (observerRef.current) {
        observerRef.current.disconnect();
      }
    };
  }, [handleIntersection, threshold]);

  useEffect(() => {
    if (items.length > 0 && loadedItems.length === 0) {
      loadMore();
    }
  }, [items, loadedItems.length, loadMore]);

  return {
    loadedItems,
    isLoading,
    hasMore,
    triggerRef,
    loadMore
  };
};

/**
 * Hook for performance monitoring
 */
export const usePerformanceMonitor = (componentName: string) => {
  const renderCount = useRef(0);
  const startTime = useRef<number>(0);
  const endTime = useRef<number>(0);

  useEffect(() => {
    renderCount.current += 1;
    startTime.current = performance.now();
    
    return () => {
      endTime.current = performance.now();
      const renderTime = endTime.current - startTime.current;
      
      if (process.env.NODE_ENV === 'development') {
        console.log(`${componentName} render #${renderCount.current}: ${renderTime.toFixed(2)}ms`);
      }
    };
  });

  return {
    renderCount: renderCount.current,
    getRenderTime: () => endTime.current - startTime.current
  };
};

/**
 * Hook for memory optimization
 */
export const useMemoryOptimization = () => {
  const cleanupFunctions = useRef<(() => void)[]>([]);
  
  const addCleanup = useCallback((cleanup: () => void) => {
    cleanupFunctions.current.push(cleanup);
  }, []);
  
  const cleanup = useCallback(() => {
    cleanupFunctions.current.forEach(fn => fn());
    cleanupFunctions.current = [];
  }, []);
  
  useEffect(() => {
    return cleanup;
  }, [cleanup]);
  
  return { addCleanup, cleanup };
};
