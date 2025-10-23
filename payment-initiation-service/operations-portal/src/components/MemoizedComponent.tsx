/**
 * Higher-order component for memoization
 */

import React, { memo, ComponentType, ComponentProps } from 'react';

/**
 * HOC for memoizing components
 */
export const withMemo = <P extends object>(
  Component: ComponentType<P>,
  areEqual?: (prevProps: P, nextProps: P) => boolean
) => {
  const MemoizedComponent = memo(Component, areEqual);
  MemoizedComponent.displayName = `Memoized(${Component.displayName || Component.name})`;
  return MemoizedComponent;
};

/**
 * HOC for memoizing components with custom comparison
 */
export const withCustomMemo = <P extends object>(
  Component: ComponentType<P>,
  compareFn: (prevProps: P, nextProps: P) => boolean
) => {
  return withMemo(Component, compareFn);
};

/**
 * HOC for memoizing components that only re-render when specific props change
 */
export const withSelectiveMemo = <P extends object>(
  Component: ComponentType<P>,
  selectProps: (props: P) => any[]
) => {
  const MemoizedComponent = memo(Component, (prevProps, nextProps) => {
    const prevSelected = selectProps(prevProps);
    const nextSelected = selectProps(nextProps);
    
    return prevSelected.every((value, index) => value === nextSelected[index]);
  });
  
  MemoizedComponent.displayName = `SelectiveMemo(${Component.displayName || Component.name})`;
  return MemoizedComponent;
};

/**
 * HOC for memoizing components with deep comparison
 */
export const withDeepMemo = <P extends object>(
  Component: ComponentType<P>
) => {
  const MemoizedComponent = memo(Component, (prevProps, nextProps) => {
    return JSON.stringify(prevProps) === JSON.stringify(nextProps);
  });
  
  MemoizedComponent.displayName = `DeepMemo(${Component.displayName || Component.name})`;
  return MemoizedComponent;
};

/**
 * HOC for memoizing components with shallow comparison
 */
export const withShallowMemo = <P extends object>(
  Component: ComponentType<P>
) => {
  const MemoizedComponent = memo(Component, (prevProps, nextProps) => {
    const prevKeys = Object.keys(prevProps);
    const nextKeys = Object.keys(nextProps);
    
    if (prevKeys.length !== nextKeys.length) {
      return false;
    }
    
    return prevKeys.every(key => prevProps[key as keyof P] === nextProps[key as keyof P]);
  });
  
  MemoizedComponent.displayName = `ShallowMemo(${Component.displayName || Component.name})`;
  return MemoizedComponent;
};

/**
 * HOC for memoizing components with performance monitoring
 */
export const withPerformanceMemo = <P extends object>(
  Component: ComponentType<P>,
  componentName?: string
) => {
  const MemoizedComponent = memo(Component, (prevProps, nextProps) => {
    const start = performance.now();
    const result = JSON.stringify(prevProps) === JSON.stringify(nextProps);
    const end = performance.now();
    
    if (process.env.NODE_ENV === 'development') {
      console.log(`${componentName || Component.name} memo comparison: ${(end - start).toFixed(2)}ms`);
    }
    
    return result;
  });
  
  MemoizedComponent.displayName = `PerformanceMemo(${Component.displayName || Component.name})`;
  return MemoizedComponent;
};

/**
 * HOC for memoizing components with error boundaries
 */
export const withErrorBoundaryMemo = <P extends object>(
  Component: ComponentType<P>,
  fallback?: React.ComponentType<{ error: Error }>
) => {
  const MemoizedComponent = memo(Component);
  
  const WrappedComponent = (props: P) => {
    try {
      return <MemoizedComponent {...props} />;
    } catch (error) {
      if (fallback) {
        return <fallback error={error as Error} />;
      }
      throw error;
    }
  };
  
  WrappedComponent.displayName = `ErrorBoundaryMemo(${Component.displayName || Component.name})`;
  return WrappedComponent;
};

/**
 * HOC for memoizing components with lazy loading
 */
export const withLazyMemo = <P extends object>(
  Component: ComponentType<P>,
  loadingComponent?: React.ComponentType
) => {
  const LazyComponent = React.lazy(() => Promise.resolve({ default: Component }));
  
  const MemoizedComponent = memo(LazyComponent);
  
  const WrappedComponent = (props: P) => {
    return (
      <React.Suspense fallback={loadingComponent ? <loadingComponent /> : <div>Loading...</div>}>
        <MemoizedComponent {...props} />
      </React.Suspense>
    );
  };
  
  WrappedComponent.displayName = `LazyMemo(${Component.displayName || Component.name})`;
  return WrappedComponent;
};

/**
 * HOC for memoizing components with virtualization
 */
export const withVirtualizationMemo = <P extends object>(
  Component: ComponentType<P>,
  virtualizationOptions?: {
    itemHeight: number;
    containerHeight: number;
    overscan?: number;
  }
) => {
  const MemoizedComponent = memo(Component);
  
  const WrappedComponent = (props: P) => {
    if (virtualizationOptions) {
      // This would integrate with virtualization logic
      return <MemoizedComponent {...props} />;
    }
    
    return <MemoizedComponent {...props} />;
  };
  
  WrappedComponent.displayName = `VirtualizationMemo(${Component.displayName || Component.name})`;
  return WrappedComponent;
};
