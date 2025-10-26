/**
 * usePagination Hook
 * 
 * Hook for managing pagination state and operations.
 * Provides pagination controls and state management.
 */

import { useState, useCallback, useMemo } from 'react';

interface UsePaginationOptions {
  initialPage?: number;
  initialPageSize?: number;
  totalItems?: number;
  maxVisiblePages?: number;
}

interface UsePaginationReturn {
  // Current state
  currentPage: number;
  pageSize: number;
  totalItems: number;
  totalPages: number;
  
  // Computed values
  hasNextPage: boolean;
  hasPrevPage: boolean;
  isFirstPage: boolean;
  isLastPage: boolean;
  startIndex: number;
  endIndex: number;
  visiblePages: number[];
  
  // Actions
  goToPage: (page: number) => void;
  nextPage: () => void;
  prevPage: () => void;
  firstPage: () => void;
  lastPage: () => void;
  setPageSize: (size: number) => void;
  setTotalItems: (total: number) => void;
  reset: () => void;
}

/**
 * Hook for managing pagination state
 */
export function usePagination(options: UsePaginationOptions = {}): UsePaginationReturn {
  const {
    initialPage = 0,
    initialPageSize = 20,
    totalItems = 0,
    maxVisiblePages = 5,
  } = options;

  const [currentPage, setCurrentPage] = useState(initialPage);
  const [pageSize, setPageSize] = useState(initialPageSize);
  const [totalItemsState, setTotalItemsState] = useState(totalItems);

  // Calculate total pages
  const totalPages = useMemo(() => {
    return Math.max(1, Math.ceil(totalItemsState / pageSize));
  }, [totalItemsState, pageSize]);

  // Check if there's a next page
  const hasNextPage = useMemo(() => {
    return currentPage < totalPages - 1;
  }, [currentPage, totalPages]);

  // Check if there's a previous page
  const hasPrevPage = useMemo(() => {
    return currentPage > 0;
  }, [currentPage]);

  // Check if on first page
  const isFirstPage = useMemo(() => {
    return currentPage === 0;
  }, [currentPage]);

  // Check if on last page
  const isLastPage = useMemo(() => {
    return currentPage === totalPages - 1;
  }, [currentPage, totalPages]);

  // Calculate start index for current page
  const startIndex = useMemo(() => {
    return currentPage * pageSize;
  }, [currentPage, pageSize]);

  // Calculate end index for current page
  const endIndex = useMemo(() => {
    return Math.min(startIndex + pageSize - 1, totalItemsState - 1);
  }, [startIndex, pageSize, totalItemsState]);

  // Calculate visible page numbers
  const visiblePages = useMemo(() => {
    const pages: number[] = [];
    const halfVisible = Math.floor(maxVisiblePages / 2);
    
    let startPage = Math.max(0, currentPage - halfVisible);
    let endPage = Math.min(totalPages - 1, startPage + maxVisiblePages - 1);
    
    // Adjust if we're near the end
    if (endPage - startPage < maxVisiblePages - 1) {
      startPage = Math.max(0, endPage - maxVisiblePages + 1);
    }
    
    for (let i = startPage; i <= endPage; i++) {
      pages.push(i);
    }
    
    return pages;
  }, [currentPage, totalPages, maxVisiblePages]);

  // Go to specific page
  const goToPage = useCallback((page: number) => {
    const validPage = Math.max(0, Math.min(page, totalPages - 1));
    setCurrentPage(validPage);
  }, [totalPages]);

  // Go to next page
  const nextPage = useCallback(() => {
    if (hasNextPage) {
      setCurrentPage(prev => prev + 1);
    }
  }, [hasNextPage]);

  // Go to previous page
  const prevPage = useCallback(() => {
    if (hasPrevPage) {
      setCurrentPage(prev => prev - 1);
    }
  }, [hasPrevPage]);

  // Go to first page
  const firstPage = useCallback(() => {
    setCurrentPage(0);
  }, []);

  // Go to last page
  const lastPage = useCallback(() => {
    setCurrentPage(totalPages - 1);
  }, [totalPages]);

  // Set page size
  const setPageSizeHandler = useCallback((size: number) => {
    const validSize = Math.max(1, size);
    setPageSize(validSize);
    // Reset to first page when page size changes
    setCurrentPage(0);
  }, []);

  // Set total items
  const setTotalItems = useCallback((total: number) => {
    const validTotal = Math.max(0, total);
    setTotalItemsState(validTotal);
    
    // Adjust current page if it's beyond the new total
    const newTotalPages = Math.max(1, Math.ceil(validTotal / pageSize));
    if (currentPage >= newTotalPages) {
      setCurrentPage(Math.max(0, newTotalPages - 1));
    }
  }, [currentPage, pageSize]);

  // Reset pagination
  const reset = useCallback(() => {
    setCurrentPage(initialPage);
    setPageSize(initialPageSize);
    setTotalItemsState(totalItems);
  }, [initialPage, initialPageSize, totalItems]);

  return {
    // Current state
    currentPage,
    pageSize,
    totalItems: totalItemsState,
    totalPages,
    
    // Computed values
    hasNextPage,
    hasPrevPage,
    isFirstPage,
    isLastPage,
    startIndex,
    endIndex,
    visiblePages,
    
    // Actions
    goToPage,
    nextPage,
    prevPage,
    firstPage,
    lastPage,
    setPageSize: setPageSizeHandler,
    setTotalItems,
    reset,
  };
}

/**
 * Hook for pagination with API integration
 */
export function useApiPagination<T = any>(
  apiFunction: (page: number, size: number, ...args: any[]) => Promise<{ content: T[]; totalElements: number; totalPages: number }>,
  options: UsePaginationOptions = {}
) {
  const pagination = usePagination(options);
  const [data, setData] = useState<T[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  // Fetch data when pagination changes
  const fetchData = useCallback(async (...args: any[]) => {
    setLoading(true);
    setError(null);

    try {
      const result = await apiFunction(pagination.currentPage, pagination.pageSize, ...args);
      setData(result.content);
      pagination.setTotalItems(result.totalElements);
    } catch (err: any) {
      setError(err.message || 'Failed to fetch data');
    } finally {
      setLoading(false);
    }
  }, [apiFunction, pagination]);

  // Auto-fetch when pagination changes
  const handlePageChange = useCallback((page: number) => {
    pagination.goToPage(page);
  }, [pagination]);

  const handlePageSizeChange = useCallback((size: number) => {
    pagination.setPageSize(size);
  }, [pagination]);

  return {
    ...pagination,
    data,
    loading,
    error,
    fetchData,
    handlePageChange,
    handlePageSizeChange,
  };
}

export default usePagination;
