/**
 * Custom hook for offline detection and handling
 */

import { useState, useEffect, useCallback } from 'react';
import { useNotification } from './useNotification';

interface UseOfflineOptions {
  enableNotifications?: boolean;
  checkInterval?: number;
  onOnline?: () => void;
  onOffline?: () => void;
}

export const useOffline = (options: UseOfflineOptions = {}) => {
  const {
    enableNotifications = true,
    checkInterval = 5000,
    onOnline,
    onOffline
  } = options;

  const [isOnline, setIsOnline] = useState(navigator.onLine);
  const [wasOffline, setWasOffline] = useState(false);
  const { showNotification } = useNotification();

  const handleOnline = useCallback(() => {
    setIsOnline(true);
    
    if (wasOffline && enableNotifications) {
      showNotification('Connection restored', 'success');
    }
    
    setWasOffline(false);
    onOnline?.();
  }, [wasOffline, enableNotifications, showNotification, onOnline]);

  const handleOffline = useCallback(() => {
    setIsOnline(false);
    setWasOffline(true);
    
    if (enableNotifications) {
      showNotification('You are offline. Some features may be limited.', 'warning');
    }
    
    onOffline?.();
  }, [enableNotifications, showNotification, onOffline]);

  useEffect(() => {
    // Listen for online/offline events
    window.addEventListener('online', handleOnline);
    window.addEventListener('offline', handleOffline);

    // Periodic connectivity check
    const checkConnectivity = async () => {
      try {
        const response = await fetch('/api/health', {
          method: 'HEAD',
          cache: 'no-cache',
          signal: AbortSignal.timeout(5000)
        });
        
        if (response.ok && !isOnline) {
          handleOnline();
        }
      } catch (error) {
        if (isOnline) {
          handleOffline();
        }
      }
    };

    const interval = setInterval(checkConnectivity, checkInterval);

    return () => {
      window.removeEventListener('online', handleOnline);
      window.removeEventListener('offline', handleOffline);
      clearInterval(interval);
    };
  }, [handleOnline, handleOffline, checkInterval, isOnline]);

  return {
    isOnline,
    wasOffline,
    isOffline: !isOnline
  };
};
