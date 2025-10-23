/**
 * Custom hook for error handling in components
 */

import { useCallback, useRef } from 'react';
import { AppError, ErrorContext } from '../utils/errorTypes';
import { errorHandler } from '../utils/errorHandler';
import { useNotification } from '../contexts';

interface UseErrorHandlerOptions {
  enableNotifications?: boolean;
  enableLogging?: boolean;
  enableReporting?: boolean;
}

export const useErrorHandler = (options: UseErrorHandlerOptions = {}) => {
  const { showError } = useNotification();
  const errorCountRef = useRef(0);
  const lastErrorRef = useRef<AppError | null>(null);

  const {
    enableNotifications = true,
    enableLogging = true,
    enableReporting = true
  } = options;

  const handleError = useCallback((
    error: any, 
    context?: Partial<ErrorContext>,
    customOptions?: Partial<UseErrorHandlerOptions>
  ) => {
    const mergedOptions = { ...options, ...customOptions };
    
    // Create error context with component information
    const errorContext: ErrorContext = {
      timestamp: new Date(),
      url: window.location.href,
      userAgent: navigator.userAgent,
      ...context
    };

    // Handle the error
    const appError = errorHandler.handleError(error, errorContext);
    
    // Update error tracking
    errorCountRef.current += 1;
    lastErrorRef.current = appError;

    // Show user notification if enabled
    if (mergedOptions.enableNotifications && appError.userMessage) {
      const severity = getNotificationSeverity(appError.severity);
      showError('Error', appError.userMessage);
    }

    return appError;
  }, [showError, options]);

  const handleAsyncError = useCallback(async <T>(
    asyncFn: () => Promise<T>,
    context?: Partial<ErrorContext>,
    customOptions?: Partial<UseErrorHandlerOptions>
  ): Promise<T | null> => {
    try {
      return await asyncFn();
    } catch (error) {
      handleError(error, context, customOptions);
      return null;
    }
  }, [handleError]);

  const handleApiError = useCallback((
    error: any,
    endpoint: string,
    method: string,
    context?: Partial<ErrorContext>
  ) => {
    return handleError(error, {
      ...context,
      url: endpoint,
      timestamp: new Date()
    });
  }, [handleError]);

  const handleValidationError = useCallback((
    error: any,
    field: string,
    value: any,
    context?: Partial<ErrorContext>
  ) => {
    return handleError(error, {
      ...context,
      url: window.location.href,
      timestamp: new Date()
    });
  }, [handleError]);

  const handleNetworkError = useCallback((
    error: any,
    url: string,
    context?: Partial<ErrorContext>
  ) => {
    return handleError(error, {
      ...context,
      url: url,
      timestamp: new Date()
    });
  }, [handleError]);

  const clearErrors = useCallback(() => {
    errorCountRef.current = 0;
    lastErrorRef.current = null;
    errorHandler.clearRetryCounts();
  }, []);

  const getErrorStats = useCallback(() => {
    return {
      errorCount: errorCountRef.current,
      lastError: lastErrorRef.current,
      handlerStats: errorHandler.getErrorStats()
    };
  }, []);

  const shouldRetry = useCallback((error: AppError, attempt: number) => {
    return errorHandler.shouldRetry(error, attempt);
  }, []);

  const getRetryDelay = useCallback((error: AppError, attempt: number) => {
    return errorHandler.getRetryDelay(error, attempt);
  }, []);

  return {
    handleError,
    handleAsyncError,
    handleApiError,
    handleValidationError,
    handleNetworkError,
    clearErrors,
    getErrorStats,
    shouldRetry,
    getRetryDelay
  };
};

function getNotificationSeverity(severity: string): 'success' | 'info' | 'warning' | 'error' {
  switch (severity) {
    case 'LOW': return 'info';
    case 'MEDIUM': return 'warning';
    case 'HIGH': return 'error';
    case 'CRITICAL': return 'error';
    default: return 'error';
  }
}
