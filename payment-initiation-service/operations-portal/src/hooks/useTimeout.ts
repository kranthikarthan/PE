/**
 * Custom hook for timeout handling
 */

import { useCallback, useRef, useState } from 'react';
import { useErrorHandler } from './useErrorHandler';
import { TimeoutError, ErrorType, ErrorSeverity } from '../utils/errorTypes';

interface UseTimeoutOptions {
  defaultTimeout?: number;
  enableNotifications?: boolean;
  onTimeout?: (timeout: number, operation: string) => void;
}

export const useTimeout = (options: UseTimeoutOptions = {}) => {
  const {
    defaultTimeout = 30000,
    enableNotifications = true,
    onTimeout
  } = options;

  const [activeTimeouts, setActiveTimeouts] = useState<Set<string>>(new Set());
  const timeoutRefs = useRef<Map<string, number>>(new Map());
  const { handleError } = useErrorHandler();

  const createTimeout = useCallback((
    operation: string,
    timeout: number = defaultTimeout,
    onTimeoutCallback?: () => void
  ): Promise<void> => {
    return new Promise((resolve, reject) => {
      const timeoutId = `${operation}-${Date.now()}`;
      
      const timeoutHandle = window.setTimeout(() => {
        // Remove from active timeouts
        setActiveTimeouts(prev => {
          const newSet = new Set(prev);
          newSet.delete(timeoutId);
          return newSet;
        });
        
        // Clear timeout reference
        timeoutRefs.current.delete(timeoutId);
        
        // Create timeout error
        const timeoutError: TimeoutError = {
          type: ErrorType.TIMEOUT,
          severity: ErrorSeverity.MEDIUM,
          message: `Operation '${operation}' timed out after ${timeout}ms`,
          timestamp: new Date(),
          source: 'TimeoutHandler',
          userMessage: `The operation '${operation}' took too long to complete. Please try again.`,
          retryable: true,
          retryAfter: 5,
          timeout,
          operation
        };
        
        // Handle timeout error
        handleError(timeoutError, {
          url: window.location.href,
          timestamp: new Date()
        });
        
        // Call timeout callback
        if (onTimeoutCallback) {
          onTimeoutCallback();
        }
        
        // Call onTimeout callback
        if (onTimeout) {
          onTimeout(timeout, operation);
        }
        
        reject(timeoutError);
      }, timeout);
      
      // Store timeout reference
      timeoutRefs.current.set(timeoutId, timeoutHandle);
      
      // Add to active timeouts
      setActiveTimeouts(prev => new Set(prev).add(timeoutId));
      
      // Return cleanup function
      return () => {
        window.clearTimeout(timeoutHandle);
        setActiveTimeouts(prev => {
          const newSet = new Set(prev);
          newSet.delete(timeoutId);
          return newSet;
        });
        timeoutRefs.current.delete(timeoutId);
      };
    });
  }, [defaultTimeout, handleError, onTimeout]);

  const withTimeout = useCallback(async <T>(
    asyncFn: () => Promise<T>,
    operation: string,
    timeout: number = defaultTimeout
  ): Promise<T> => {
    const timeoutPromise = createTimeout(operation, timeout);
    
    try {
      const result = await Promise.race([
        asyncFn(),
        timeoutPromise.then(() => {
          throw new Error(`Operation '${operation}' timed out`);
        })
      ]);
      
      return result;
    } catch (error) {
      throw error;
    }
  }, [createTimeout, defaultTimeout]);

  const clearTimeoutByOperation = useCallback((operation: string) => {
    const timeoutId = Array.from(timeoutRefs.current.keys())
      .find(id => id.startsWith(operation));
    
    if (timeoutId) {
      const timeoutHandle = timeoutRefs.current.get(timeoutId);
      if (timeoutHandle) {
        window.clearTimeout(timeoutHandle);
        timeoutRefs.current.delete(timeoutId);
      }
      
      setActiveTimeouts(prev => {
        const newSet = new Set(prev);
        newSet.delete(timeoutId);
        return newSet;
      });
    }
  }, []);

  const clearAllTimeouts = useCallback(() => {
    timeoutRefs.current.forEach((timeoutHandle) => {
      window.clearTimeout(timeoutHandle);
    });
    
    timeoutRefs.current.clear();
    setActiveTimeouts(new Set());
  }, []);

  const getActiveTimeouts = useCallback(() => {
    return Array.from(activeTimeouts);
  }, [activeTimeouts]);

  return {
    createTimeout,
    withTimeout,
    clearTimeoutByOperation,
    clearAllTimeouts,
    getActiveTimeouts,
    activeTimeouts: activeTimeouts.size
  };
};
