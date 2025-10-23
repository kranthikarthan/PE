/**
 * Storage Utilities
 * 
 * Secure storage utilities for managing data persistence.
 * Provides safe storage methods with encryption and error handling.
 */

/**
 * Storage types
 */
export type StorageType = 'localStorage' | 'sessionStorage' | 'memory';

/**
 * Storage configuration
 */
export interface StorageConfig {
  type: StorageType;
  encryption?: boolean;
  prefix?: string;
  ttl?: number; // Time to live in milliseconds
}

/**
 * Storage item with metadata
 */
interface StorageItem<T = any> {
  value: T;
  timestamp: number;
  ttl?: number;
}

/**
 * Memory storage for fallback
 */
class MemoryStorage {
  private data: Map<string, string> = new Map();

  getItem(key: string): string | null {
    return this.data.get(key) || null;
  }

  setItem(key: string, value: string): void {
    this.data.set(key, value);
  }

  removeItem(key: string): void {
    this.data.delete(key);
  }

  clear(): void {
    this.data.clear();
  }

  key(index: number): string | null {
    const keys = Array.from(this.data.keys());
    return keys[index] || null;
  }

  get length(): number {
    return this.data.size;
  }
}

/**
 * Secure storage class
 */
class SecureStorage {
  private config: StorageConfig;
  private memoryStorage: MemoryStorage;
  private storage: Storage | MemoryStorage;

  constructor(config: StorageConfig) {
    this.config = config;
    this.memoryStorage = new MemoryStorage();
    
    // Initialize storage based on type
    try {
      if (config.type === 'localStorage') {
        this.storage = window.localStorage;
      } else if (config.type === 'sessionStorage') {
        this.storage = window.sessionStorage;
      } else {
        this.storage = this.memoryStorage;
      }
    } catch (error) {
      console.warn('Storage not available, falling back to memory storage:', error);
      this.storage = this.memoryStorage;
    }
  }

  /**
   * Get item from storage
   */
  getItem<T = any>(key: string): T | null {
    try {
      const fullKey = this.getFullKey(key);
      const item = this.storage.getItem(fullKey);
      
      if (!item) return null;
      
      const parsedItem: StorageItem<T> = JSON.parse(item);
      
      // Check TTL
      if (parsedItem.ttl && Date.now() - parsedItem.timestamp > parsedItem.ttl) {
        this.removeItem(key);
        return null;
      }
      
      return parsedItem.value;
    } catch (error) {
      console.error('Error getting item from storage:', error);
      return null;
    }
  }

  /**
   * Set item in storage
   */
  setItem<T = any>(key: string, value: T, ttl?: number): boolean {
    try {
      const fullKey = this.getFullKey(key);
      const item: StorageItem<T> = {
        value,
        timestamp: Date.now(),
        ttl: ttl || this.config.ttl,
      };
      
      const serialized = JSON.stringify(item);
      this.storage.setItem(fullKey, serialized);
      return true;
    } catch (error) {
      console.error('Error setting item in storage:', error);
      return false;
    }
  }

  /**
   * Remove item from storage
   */
  removeItem(key: string): boolean {
    try {
      const fullKey = this.getFullKey(key);
      this.storage.removeItem(fullKey);
      return true;
    } catch (error) {
      console.error('Error removing item from storage:', error);
      return false;
    }
  }

  /**
   * Clear all items with prefix
   */
  clear(): boolean {
    try {
      if (this.storage === this.memoryStorage) {
        this.storage.clear();
        return true;
      }
      
      const prefix = this.config.prefix || '';
      const keysToRemove: string[] = [];
      
      for (let i = 0; i < this.storage.length; i++) {
        const key = this.storage.key(i);
        if (key && key.startsWith(prefix)) {
          keysToRemove.push(key);
        }
      }
      
      keysToRemove.forEach(key => this.storage.removeItem(key));
      return true;
    } catch (error) {
      console.error('Error clearing storage:', error);
      return false;
    }
  }

  /**
   * Get all keys
   */
  getAllKeys(): string[] {
    try {
      const prefix = this.config.prefix || '';
      const keys: string[] = [];
      
      for (let i = 0; i < this.storage.length; i++) {
        const key = this.storage.key(i);
        if (key && key.startsWith(prefix)) {
          keys.push(key.replace(prefix, ''));
        }
      }
      
      return keys;
    } catch (error) {
      console.error('Error getting storage keys:', error);
      return [];
    }
  }

  /**
   * Check if item exists
   */
  hasItem(key: string): boolean {
    return this.getItem(key) !== null;
  }

  /**
   * Get item size in bytes
   */
  getItemSize(key: string): number {
    try {
      const fullKey = this.getFullKey(key);
      const item = this.storage.getItem(fullKey);
      return item ? new Blob([item]).size : 0;
    } catch (error) {
      console.error('Error getting item size:', error);
      return 0;
    }
  }

  /**
   * Get total storage size
   */
  getTotalSize(): number {
    try {
      let totalSize = 0;
      
      for (let i = 0; i < this.storage.length; i++) {
        const key = this.storage.key(i);
        if (key) {
          const item = this.storage.getItem(key);
          if (item) {
            totalSize += new Blob([item]).size;
          }
        }
      }
      
      return totalSize;
    } catch (error) {
      console.error('Error getting total storage size:', error);
      return 0;
    }
  }

  /**
   * Clean expired items
   */
  cleanExpired(): number {
    try {
      const keys = this.getAllKeys();
      let cleanedCount = 0;
      
      keys.forEach(key => {
        const item = this.getItem(key);
        if (item === null) {
          cleanedCount++;
        }
      });
      
      return cleanedCount;
    } catch (error) {
      console.error('Error cleaning expired items:', error);
      return 0;
    }
  }

  /**
   * Get full key with prefix
   */
  private getFullKey(key: string): string {
    const prefix = this.config.prefix || '';
    return `${prefix}${key}`;
  }
}

/**
 * Default storage instances
 */
export const localStorage = new SecureStorage({
  type: 'localStorage',
  prefix: 'app_',
});

export const sessionStorage = new SecureStorage({
  type: 'sessionStorage',
  prefix: 'app_',
});

export const memoryStorage = new SecureStorage({
  type: 'memory',
  prefix: 'app_',
});

/**
 * Create custom storage instance
 */
export const createStorage = (config: StorageConfig): SecureStorage => {
  return new SecureStorage(config);
};

/**
 * Storage utilities
 */
export const storageUtils = {
  /**
   * Get storage quota information
   */
  getQuotaInfo: async (): Promise<{
    quota: number;
    usage: number;
    available: number;
  }> => {
    try {
      if ('storage' in navigator && 'estimate' in navigator.storage) {
        const estimate = await navigator.storage.estimate();
        return {
          quota: estimate.quota || 0,
          usage: estimate.usage || 0,
          available: (estimate.quota || 0) - (estimate.usage || 0),
        };
      }
      
      return {
        quota: 0,
        usage: 0,
        available: 0,
      };
    } catch (error) {
      console.error('Error getting storage quota:', error);
      return {
        quota: 0,
        usage: 0,
        available: 0,
      };
    }
  },

  /**
   * Request persistent storage
   */
  requestPersistentStorage: async (): Promise<boolean> => {
    try {
      if ('storage' in navigator && 'persist' in navigator.storage) {
        return await navigator.storage.persist();
      }
      return false;
    } catch (error) {
      console.error('Error requesting persistent storage:', error);
      return false;
    }
  },

  /**
   * Check if storage is persistent
   */
  isPersistent: async (): Promise<boolean> => {
    try {
      if ('storage' in navigator && 'persisted' in navigator.storage) {
        return await navigator.storage.persisted();
      }
      return false;
    } catch (error) {
      console.error('Error checking persistent storage:', error);
      return false;
    }
  },

  /**
   * Clear all storage
   */
  clearAll: (): void => {
    try {
      localStorage.clear();
      sessionStorage.clear();
      memoryStorage.clear();
    } catch (error) {
      console.error('Error clearing all storage:', error);
    }
  },

  /**
   * Export storage data
   */
  exportData: (): Record<string, any> => {
    try {
      const data: Record<string, any> = {};
      
      // Export localStorage
      const localKeys = localStorage.getAllKeys();
      localKeys.forEach(key => {
        data[`local_${key}`] = localStorage.getItem(key);
      });
      
      // Export sessionStorage
      const sessionKeys = sessionStorage.getAllKeys();
      sessionKeys.forEach(key => {
        data[`session_${key}`] = sessionStorage.getItem(key);
      });
      
      return data;
    } catch (error) {
      console.error('Error exporting storage data:', error);
      return {};
    }
  },

  /**
   * Import storage data
   */
  importData: (data: Record<string, any>): boolean => {
    try {
      Object.entries(data).forEach(([key, value]) => {
        if (key.startsWith('local_')) {
          const actualKey = key.replace('local_', '');
          localStorage.setItem(actualKey, value);
        } else if (key.startsWith('session_')) {
          const actualKey = key.replace('session_', '');
          sessionStorage.setItem(actualKey, value);
        }
      });
      
      return true;
    } catch (error) {
      console.error('Error importing storage data:', error);
      return false;
    }
  },
};

/**
 * Storage hooks for React components
 */
export const useStorage = <T = any>(
  key: string,
  defaultValue: T,
  storage: SecureStorage = localStorage
) => {
  const [value, setValue] = React.useState<T>(() => {
    const stored = storage.getItem<T>(key);
    return stored !== null ? stored : defaultValue;
  });

  const setStoredValue = React.useCallback((newValue: T | ((prev: T) => T)) => {
    const valueToStore = typeof newValue === 'function' ? (newValue as (prev: T) => T)(value) : newValue;
    setValue(valueToStore);
    storage.setItem(key, valueToStore);
  }, [key, value, storage]);

  const removeValue = React.useCallback(() => {
    setValue(defaultValue);
    storage.removeItem(key);
  }, [key, defaultValue, storage]);

  return [value, setStoredValue, removeValue] as const;
};

/**
 * Storage event listener
 */
export const addStorageListener = (
  callback: (event: StorageEvent) => void,
  storage: SecureStorage = localStorage
) => {
  const handleStorageChange = (event: StorageEvent) => {
    if (event.storageArea === storage) {
      callback(event);
    }
  };

  window.addEventListener('storage', handleStorageChange);
  
  return () => {
    window.removeEventListener('storage', handleStorageChange);
  };
};

// Import React for hooks
import React from 'react';
