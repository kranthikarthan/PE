/**
 * useApi Hook
 * 
 * Generic hook for making API calls with loading, error, and success states.
 * Provides automatic retry logic and error handling.
 */

import { useState, useEffect, useCallback, useRef } from 'react';
import { useNotification } from '../contexts/NotificationContext';
import { ApiResponse } from '../types/api';

interface UseApiState<T> {
  data: T | null;
  loading: boolean;
  error: string | null;
  success: boolean;
}

interface UseApiOptions {
  immediate?: boolean;
  retryAttempts?: number;
  retryDelay?: number;
  showNotifications?: boolean;
  onSuccess?: (data: any) => void;
  onError?: (error: string) => void;
}

interface UseApiReturn<T> extends UseApiState<T> {
  execute: (...args: any[]) => Promise<T | null>;
  reset: () => void;
  setData: (data: T | null) => void;
}

/**
 * Generic API hook for making API calls
 */
export function useApi<T = any>(
  apiFunction: (...args: any[]) => Promise<T>,
  options: UseApiOptions = {}
): UseApiReturn<T> {
  const {
    immediate = false,
    retryAttempts = 3,
    retryDelay = 1000,
    showNotifications = true,
    onSuccess,
    onError,
  } = options;

  const { showError, showSuccess } = useNotification();
  const [state, setState] = useState<UseApiState<T>>({
    data: null,
    loading: false,
    error: null,
    success: false,
  });

  const retryCountRef = useRef(0);
  const isMountedRef = useRef(true);

  // Cleanup on unmount
  useEffect(() => {
    return () => {
      isMountedRef.current = false;
    };
  }, []);

  // Execute API call with retry logic
  const execute = useCallback(async (...args: any[]): Promise<T | null> => {
    if (!isMountedRef.current) return null;

    setState(prev => ({
      ...prev,
      loading: true,
      error: null,
      success: false,
    }));

    try {
      const result = await apiFunction(...args);
      
      if (isMountedRef.current) {
        setState({
          data: result,
          loading: false,
          error: null,
          success: true,
        });

        if (showNotifications && onSuccess) {
          onSuccess(result);
        }

        retryCountRef.current = 0;
        return result;
      }
    } catch (error: any) {
      if (!isMountedRef.current) return null;

      const errorMessage = error.message || 'An error occurred';
      
      // Retry logic
      if (retryCountRef.current < retryAttempts) {
        retryCountRef.current++;
        
        if (isMountedRef.current) {
          setTimeout(() => {
            if (isMountedRef.current) {
              execute(...args);
            }
          }, retryDelay * retryCountRef.current);
        }
        return null;
      }

      setState({
        data: null,
        loading: false,
        error: errorMessage,
        success: false,
      });

      if (showNotifications) {
        showError('API Error', errorMessage);
      }

      if (onError) {
        onError(errorMessage);
      }

      retryCountRef.current = 0;
      return null;
    }

    return null;
  }, [apiFunction, retryAttempts, retryDelay, showNotifications, showError, onSuccess, onError]);

  // Reset state
  const reset = useCallback(() => {
    setState({
      data: null,
      loading: false,
      error: null,
      success: false,
    });
    retryCountRef.current = 0;
  }, []);

  // Set data manually
  const setData = useCallback((data: T | null) => {
    setState(prev => ({
      ...prev,
      data,
    }));
  }, []);

  // Execute immediately if requested
  useEffect(() => {
    if (immediate) {
      execute();
    }
  }, [immediate, execute]);

  return {
    ...state,
    execute,
    reset,
    setData,
  };
}

/**
 * Hook for API calls that return paginated data
 */
export function usePaginatedApi<T = any>(
  apiFunction: (page: number, size: number, ...args: any[]) => Promise<{ content: T[]; totalElements: number; totalPages: number }>,
  options: UseApiOptions & {
    pageSize?: number;
    initialPage?: number;
  } = {}
) {
  const { pageSize = 20, initialPage = 0, ...apiOptions } = options;
  
  const [page, setPage] = useState(initialPage);
  const [size, setSize] = useState(pageSize);
  const [totalElements, setTotalElements] = useState(0);
  const [totalPages, setTotalPages] = useState(0);

  const api = useApi(
    (pageNum: number, pageSizeNum: number, ...args: any[]) => 
      apiFunction(pageNum, pageSizeNum, ...args),
    {
      ...apiOptions,
      onSuccess: (data: any) => {
        setTotalElements(data.totalElements);
        setTotalPages(data.totalPages);
        if (apiOptions.onSuccess) {
          apiOptions.onSuccess(data);
        }
      },
    }
  );

  const execute = useCallback((pageNum: number = page, pageSizeNum: number = size, ...args: any[]) => {
    setPage(pageNum);
    setSize(pageSizeNum);
    return api.execute(pageNum, pageSizeNum, ...args);
  }, [api, page, size]);

  const nextPage = useCallback((...args: any[]) => {
    if (page < totalPages - 1) {
      return execute(page + 1, size, ...args);
    }
    return Promise.resolve(null);
  }, [execute, page, totalPages, size]);

  const prevPage = useCallback((...args: any[]) => {
    if (page > 0) {
      return execute(page - 1, size, ...args);
    }
    return Promise.resolve(null);
  }, [execute, page, size]);

  const goToPage = useCallback((pageNum: number, ...args: any[]) => {
    if (pageNum >= 0 && pageNum < totalPages) {
      return execute(pageNum, size, ...args);
    }
    return Promise.resolve(null);
  }, [execute, size, totalPages]);

  return {
    ...api,
    execute,
    nextPage,
    prevPage,
    goToPage,
    page,
    size,
    totalElements,
    totalPages,
    hasNextPage: page < totalPages - 1,
    hasPrevPage: page > 0,
  };
}

/**
 * Hook for API calls with search functionality
 */
export function useSearchApi<T = any>(
  apiFunction: (query: string, ...args: any[]) => Promise<T[]>,
  options: UseApiOptions & {
    debounceMs?: number;
    minQueryLength?: number;
  } = {}
) {
  const { debounceMs = 300, minQueryLength = 2, ...apiOptions } = options;
  
  const [query, setQuery] = useState('');
  const [debouncedQuery, setDebouncedQuery] = useState('');
  const debounceTimeoutRef = useRef<NodeJS.Timeout | null>(null);

  // Debounce search query
  useEffect(() => {
    if (debounceTimeoutRef.current) {
      clearTimeout(debounceTimeoutRef.current);
    }

    debounceTimeoutRef.current = setTimeout(() => {
      setDebouncedQuery(query);
    }, debounceMs);

    return () => {
      if (debounceTimeoutRef.current) {
        clearTimeout(debounceTimeoutRef.current);
      }
    };
  }, [query, debounceMs]);

  const api = useApi(
    (searchQuery: string, ...args: any[]) => 
      apiFunction(searchQuery, ...args),
    apiOptions
  );

  const search = useCallback((searchQuery: string, ...args: any[]) => {
    if (searchQuery.length < minQueryLength) {
      api.reset();
      return Promise.resolve(null);
    }
    return api.execute(searchQuery, ...args);
  }, [api, minQueryLength]);

  // Auto-search when debounced query changes
  useEffect(() => {
    if (debouncedQuery.length >= minQueryLength) {
      search(debouncedQuery);
    } else {
      api.reset();
    }
  }, [debouncedQuery, search, api, minQueryLength]);

  return {
    ...api,
    query,
    setQuery,
    search,
    debouncedQuery,
  };
}

export default useApi;
