/**
 * Custom hook for retry logic with exponential backoff
 */

import { useCallback, useRef, useState } from 'react';
import { useErrorHandler } from './useErrorHandler';
import { AppError } from '../utils/errorTypes';

interface UseRetryOptions {
  maxRetries?: number;
  baseDelay?: number;
  maxDelay?: number;
  backoffMultiplier?: number;
  onRetry?: (attempt: number, error: AppError) => void;
  onMaxRetriesReached?: (error: AppError) => void;
}

interface RetryState {
  isRetrying: boolean;
  attempt: number;
  lastError: AppError | null;
  totalRetries: number;
}

export const useRetry = (options: UseRetryOptions = {}) => {
  const {
    maxRetries = 3,
    baseDelay = 1000,
    maxDelay = 30000,
    backoffMultiplier = 2,
    onRetry,
    onMaxRetriesReached
  } = options;

  const [retryState, setRetryState] = useState<RetryState>({
    isRetrying: false,
    attempt: 0,
    lastError: null,
    totalRetries: 0
  });

  const timeoutRef = useRef<NodeJS.Timeout | null>(null);
  const { shouldRetry, getRetryDelay } = useErrorHandler();

  const executeWithRetry = useCallback(async <T>(
    asyncFn: () => Promise<T>,
    customOptions?: Partial<UseRetryOptions>
  ): Promise<T | null> => {
    const mergedOptions = { ...options, ...customOptions };
    const { maxRetries: customMaxRetries = maxRetries } = mergedOptions;

    let attempt = 0;
    let lastError: AppError | null = null;

    while (attempt <= customMaxRetries) {
      try {
        setRetryState(prev => ({
          ...prev,
          isRetrying: attempt > 0,
          attempt,
          lastError: null
        }));

        const result = await asyncFn();
        
        // Success - reset retry state
        setRetryState({
          isRetrying: false,
          attempt: 0,
          lastError: null,
          totalRetries: retryState.totalRetries + attempt
        });

        return result;
      } catch (error) {
        lastError = error as AppError;
        attempt++;

        setRetryState(prev => ({
          ...prev,
          isRetrying: attempt <= customMaxRetries,
          attempt,
          lastError,
          totalRetries: prev.totalRetries + 1
        }));

        // Check if we should retry
        if (attempt > customMaxRetries || !shouldRetry(lastError, attempt - 1)) {
          if (onMaxRetriesReached) {
            onMaxRetriesReached(lastError);
          }
          break;
        }

        // Calculate delay for next retry
        const delay = Math.min(
          baseDelay * Math.pow(backoffMultiplier, attempt - 1),
          maxDelay
        );

        // Call onRetry callback
        if (onRetry) {
          onRetry(attempt, lastError);
        }

        // Wait before retrying
        await new Promise(resolve => {
          timeoutRef.current = setTimeout(resolve, delay);
        });
      }
    }

    return null;
  }, [maxRetries, baseDelay, maxDelay, backoffMultiplier, onRetry, onMaxRetriesReached, shouldRetry, retryState.totalRetries]);

  const executeWithRetryAndDelay = useCallback(async <T>(
    asyncFn: () => Promise<T>,
    customOptions?: Partial<UseRetryOptions>
  ): Promise<T | null> => {
    const mergedOptions = { ...options, ...customOptions };
    const { maxRetries: customMaxRetries = maxRetries } = mergedOptions;

    let attempt = 0;
    let lastError: AppError | null = null;

    while (attempt <= customMaxRetries) {
      try {
        setRetryState(prev => ({
          ...prev,
          isRetrying: attempt > 0,
          attempt,
          lastError: null
        }));

        const result = await asyncFn();
        
        // Success - reset retry state
        setRetryState({
          isRetrying: false,
          attempt: 0,
          lastError: null,
          totalRetries: retryState.totalRetries + attempt
        });

        return result;
      } catch (error) {
        lastError = error as AppError;
        attempt++;

        setRetryState(prev => ({
          ...prev,
          isRetrying: attempt <= customMaxRetries,
          attempt,
          lastError,
          totalRetries: prev.totalRetries + 1
        }));

        // Check if we should retry
        if (attempt > customMaxRetries || !shouldRetry(lastError, attempt - 1)) {
          if (onMaxRetriesReached) {
            onMaxRetriesReached(lastError);
          }
          break;
        }

        // Use error handler's retry delay calculation
        const delay = getRetryDelay(lastError, attempt - 1);

        // Call onRetry callback
        if (onRetry) {
          onRetry(attempt, lastError);
        }

        // Wait before retrying
        await new Promise(resolve => {
          timeoutRef.current = setTimeout(resolve, delay);
        });
      }
    }

    return null;
  }, [maxRetries, onRetry, onMaxRetriesReached, shouldRetry, getRetryDelay, retryState.totalRetries]);

  const cancelRetry = useCallback(() => {
    if (timeoutRef.current) {
      clearTimeout(timeoutRef.current);
      timeoutRef.current = null;
    }
    
    setRetryState(prev => ({
      ...prev,
      isRetrying: false
    }));
  }, []);

  const resetRetry = useCallback(() => {
    if (timeoutRef.current) {
      clearTimeout(timeoutRef.current);
      timeoutRef.current = null;
    }
    
    setRetryState({
      isRetrying: false,
      attempt: 0,
      lastError: null,
      totalRetries: 0
    });
  }, []);

  return {
    executeWithRetry,
    executeWithRetryAndDelay,
    cancelRetry,
    resetRetry,
    ...retryState
  };
};
