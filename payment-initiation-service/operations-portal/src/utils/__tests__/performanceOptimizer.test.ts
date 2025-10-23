/**
 * Tests for performance optimization utilities
 */

import { 
  PerformanceOptimizer, 
  usePerformanceMonitoring, 
  useMemoryMonitoring,
  useBundleMonitoring,
  performanceUtils,
  getPerformanceRecommendations
} from '../performanceOptimizer';

describe('PerformanceOptimizer', () => {
  let optimizer: PerformanceOptimizer;

  beforeEach(() => {
    optimizer = new PerformanceOptimizer();
  });

  describe('constructor', () => {
    it('should initialize with default config', () => {
      expect(optimizer.getMetrics()).toEqual({
        renderTime: 0,
        memoryUsage: 0,
        bundleSize: 0,
        loadTime: 0
      });
    });

    it('should initialize with custom config', () => {
      const customOptimizer = new PerformanceOptimizer({
        enableMemoization: false,
        enableVirtualization: true
      });

      expect(customOptimizer).toBeDefined();
    });
  });

  describe('optimizeComponent', () => {
    it('should return component when memoization is disabled', () => {
      const disabledOptimizer = new PerformanceOptimizer({
        enableMemoization: false
      });

      const MockComponent = () => <div>Test</div>;
      const optimized = disabledOptimizer.optimizeComponent(MockComponent);

      expect(optimized).toBe(MockComponent);
    });

    it('should optimize component when memoization is enabled', () => {
      const MockComponent = () => <div>Test</div>;
      const optimized = optimizer.optimizeComponent(MockComponent);

      expect(optimized).toBeDefined();
    });
  });

  describe('optimizeList', () => {
    it('should return items when virtualization is disabled', () => {
      const disabledOptimizer = new PerformanceOptimizer({
        enableVirtualization: false
      });

      const items = [1, 2, 3, 4, 5];
      const optimized = disabledOptimizer.optimizeList(items);

      expect(optimized).toBe(items);
    });

    it('should optimize list when virtualization is enabled', () => {
      const items = [1, 2, 3, 4, 5];
      const optimized = optimizer.optimizeList(items, {
        itemHeight: 50,
        containerHeight: 200,
        overscan: 5
      });

      expect(optimized).toBeDefined();
    });
  });

  describe('optimizeBundle', () => {
    it('should optimize bundle when enabled', () => {
      const consoleSpy = jest.spyOn(console, 'log').mockImplementation();
      
      optimizer.optimizeBundle();
      
      expect(consoleSpy).toHaveBeenCalledWith('Bundle optimization applied');
      
      consoleSpy.mockRestore();
    });

    it('should not optimize bundle when disabled', () => {
      const disabledOptimizer = new PerformanceOptimizer({
        enableBundleOptimization: false
      });

      const consoleSpy = jest.spyOn(console, 'log').mockImplementation();
      
      disabledOptimizer.optimizeBundle();
      
      expect(consoleSpy).not.toHaveBeenCalled();
      
      consoleSpy.mockRestore();
    });
  });

  describe('optimizeCodeSplitting', () => {
    it('should optimize code splitting when enabled', () => {
      const consoleSpy = jest.spyOn(console, 'log').mockImplementation();
      
      optimizer.optimizeCodeSplitting();
      
      expect(consoleSpy).toHaveBeenCalledWith('Code splitting optimization applied');
      
      consoleSpy.mockRestore();
    });

    it('should not optimize code splitting when disabled', () => {
      const disabledOptimizer = new PerformanceOptimizer({
        enableCodeSplitting: false
      });

      const consoleSpy = jest.spyOn(console, 'log').mockImplementation();
      
      disabledOptimizer.optimizeCodeSplitting();
      
      expect(consoleSpy).not.toHaveBeenCalled();
      
      consoleSpy.mockRestore();
    });
  });

  describe('getMetrics', () => {
    it('should return current metrics', () => {
      const metrics = optimizer.getMetrics();
      
      expect(metrics).toEqual({
        renderTime: 0,
        memoryUsage: 0,
        bundleSize: 0,
        loadTime: 0
      });
    });
  });

  describe('updateMetrics', () => {
    it('should update metrics', () => {
      optimizer.updateMetrics({
        renderTime: 16,
        memoryUsage: 50
      });

      const metrics = optimizer.getMetrics();
      
      expect(metrics.renderTime).toBe(16);
      expect(metrics.memoryUsage).toBe(50);
    });
  });

  describe('getRecommendations', () => {
    it('should return recommendations for slow render time', () => {
      optimizer.updateMetrics({ renderTime: 20 });
      
      const recommendations = optimizer.getRecommendations();
      
      expect(recommendations).toContain('Consider memoizing expensive components');
    });

    it('should return recommendations for high memory usage', () => {
      optimizer.updateMetrics({ memoryUsage: 150 });
      
      const recommendations = optimizer.getRecommendations();
      
      expect(recommendations).toContain('Consider implementing virtualization for large lists');
    });

    it('should return recommendations for large bundle size', () => {
      optimizer.updateMetrics({ bundleSize: 2000000 });
      
      const recommendations = optimizer.getRecommendations();
      
      expect(recommendations).toContain('Consider code splitting and lazy loading');
    });

    it('should return recommendations for slow load time', () => {
      optimizer.updateMetrics({ loadTime: 5000 });
      
      const recommendations = optimizer.getRecommendations();
      
      expect(recommendations).toContain('Consider optimizing bundle size and enabling compression');
    });
  });
});

describe('usePerformanceMonitoring', () => {
  it('should track render time', () => {
    const { result } = renderHook(() => 
      usePerformanceMonitoring('TestComponent')
    );

    expect(result.current.endMonitoring).toBeDefined();
    
    const renderTime = result.current.endMonitoring();
    expect(renderTime).toBeGreaterThan(0);
  });
});

describe('useMemoryMonitoring', () => {
  it('should get memory usage', () => {
    const { result } = renderHook(() => useMemoryMonitoring());

    const memoryUsage = result.current.getMemoryUsage();
    
    if (memoryUsage) {
      expect(memoryUsage).toHaveProperty('used');
      expect(memoryUsage).toHaveProperty('total');
      expect(memoryUsage).toHaveProperty('limit');
    }
  });
});

describe('useBundleMonitoring', () => {
  it('should get bundle size', () => {
    const { result } = renderHook(() => useBundleMonitoring());

    const bundleSize = result.current.getBundleSize();
    
    expect(bundleSize).toHaveProperty('total');
    expect(bundleSize).toHaveProperty('gzipped');
    expect(bundleSize).toHaveProperty('chunks');
  });
});

describe('performanceUtils', () => {
  describe('debounce', () => {
    beforeEach(() => {
      jest.useFakeTimers();
    });

    afterEach(() => {
      jest.useRealTimers();
    });

    it('should debounce function calls', () => {
      const mockFn = jest.fn();
      const debouncedFn = performanceUtils.debounce(mockFn, 100);

      debouncedFn();
      debouncedFn();
      debouncedFn();

      expect(mockFn).not.toHaveBeenCalled();

      jest.advanceTimersByTime(100);

      expect(mockFn).toHaveBeenCalledTimes(1);
    });
  });

  describe('throttle', () => {
    beforeEach(() => {
      jest.useFakeTimers();
    });

    afterEach(() => {
      jest.useRealTimers();
    });

    it('should throttle function calls', () => {
      const mockFn = jest.fn();
      const throttledFn = performanceUtils.throttle(mockFn, 100);

      throttledFn();
      throttledFn();
      throttledFn();

      expect(mockFn).toHaveBeenCalledTimes(1);

      jest.advanceTimersByTime(100);

      throttledFn();
      expect(mockFn).toHaveBeenCalledTimes(2);
    });
  });

  describe('rafThrottle', () => {
    it('should throttle using requestAnimationFrame', () => {
      const mockFn = jest.fn();
      const rafThrottledFn = performanceUtils.rafThrottle(mockFn);

      rafThrottledFn();
      rafThrottledFn();
      rafThrottledFn();

      expect(mockFn).toHaveBeenCalledTimes(1);
    });
  });

  describe('createIntersectionObserver', () => {
    it('should create intersection observer', () => {
      const callback = jest.fn();
      const observer = performanceUtils.createIntersectionObserver(callback);

      expect(observer).toBeInstanceOf(IntersectionObserver);
    });
  });

  describe('createResizeObserver', () => {
    it('should create resize observer', () => {
      const callback = jest.fn();
      const observer = performanceUtils.createResizeObserver(callback);

      expect(observer).toBeInstanceOf(ResizeObserver);
    });
  });
});

describe('getPerformanceRecommendations', () => {
  it('should return performance recommendations', () => {
    const recommendations = getPerformanceRecommendations();

    expect(recommendations).toContain('Use React.memo for expensive components');
    expect(recommendations).toContain('Implement useMemo and useCallback for expensive calculations');
    expect(recommendations).toContain('Use virtualization for large lists');
    expect(recommendations).toContain('Implement lazy loading for routes and components');
  });
});
