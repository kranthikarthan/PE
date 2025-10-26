/**
 * Logger Utility
 * 
 * Centralized logging utility for the application.
 * Provides structured logging with different levels and output destinations.
 */

/**
 * Log levels
 */
export enum LogLevel {
  DEBUG = 0,
  INFO = 1,
  WARN = 2,
  ERROR = 3,
  FATAL = 4,
}

/**
 * Log entry interface
 */
export interface LogEntry {
  level: LogLevel;
  message: string;
  timestamp: string;
  context?: string;
  data?: any;
  error?: Error;
  userId?: string;
  sessionId?: string;
  requestId?: string;
}

/**
 * Logger configuration
 */
export interface LoggerConfig {
  level: LogLevel;
  enableConsole: boolean;
  enableStorage: boolean;
  enableRemote: boolean;
  remoteEndpoint?: string;
  maxStorageEntries: number;
  context?: string;
}

/**
 * Default logger configuration
 */
const defaultConfig: LoggerConfig = {
  level: LogLevel.INFO,
  enableConsole: true,
  enableStorage: false,
  enableRemote: false,
  maxStorageEntries: 1000,
  context: 'app',
};

/**
 * Logger class
 */
class Logger {
  private config: LoggerConfig;
  private storage: LogEntry[] = [];
  private context: string;

  constructor(config: Partial<LoggerConfig> = {}) {
    this.config = { ...defaultConfig, ...config };
    this.context = this.config.context || 'app';
  }

  /**
   * Create log entry
   */
  private createLogEntry(
    level: LogLevel,
    message: string,
    data?: any,
    error?: Error
  ): LogEntry {
    return {
      level,
      message,
      timestamp: new Date().toISOString(),
      context: this.context,
      data,
      error,
      userId: this.getUserId(),
      sessionId: this.getSessionId(),
      requestId: this.getRequestId(),
    };
  }

  /**
   * Get user ID from storage or context
   */
  private getUserId(): string | undefined {
    try {
      // Try to get from localStorage or sessionStorage
      const userStr = localStorage.getItem('user');
      if (userStr) {
        const user = JSON.parse(userStr);
        return user?.id;
      }
      return undefined;
    } catch {
      return undefined;
    }
  }

  /**
   * Get session ID
   */
  private getSessionId(): string | undefined {
    try {
      return sessionStorage.getItem('sessionId') || undefined;
    } catch {
      return undefined;
    }
  }

  /**
   * Get request ID from context
   */
  private getRequestId(): string | undefined {
    try {
      return sessionStorage.getItem('requestId') || undefined;
    } catch {
      return undefined;
    }
  }

  /**
   * Format log entry for console
   */
  private formatForConsole(entry: LogEntry): string {
    const levelName = LogLevel[entry.level];
    const timestamp = new Date(entry.timestamp).toLocaleTimeString();
    
    return `[${timestamp}] ${levelName}: ${entry.message}`;
  }

  /**
   * Format log entry for storage/remote
   */
  private formatForStorage(entry: LogEntry): string {
    return JSON.stringify(entry);
  }

  /**
   * Write to console
   */
  private writeToConsole(entry: LogEntry): void {
    if (!this.config.enableConsole) return;

    const formatted = this.formatForConsole(entry);
    
    switch (entry.level) {
      case LogLevel.DEBUG:
        console.debug(formatted, entry.data);
        break;
      case LogLevel.INFO:
        console.info(formatted, entry.data);
        break;
      case LogLevel.WARN:
        console.warn(formatted, entry.data);
        break;
      case LogLevel.ERROR:
      case LogLevel.FATAL:
        console.error(formatted, entry.data, entry.error);
        break;
    }
  }

  /**
   * Write to storage
   */
  private writeToStorage(entry: LogEntry): void {
    if (!this.config.enableStorage) return;

    this.storage.push(entry);
    
    // Limit storage size
    if (this.storage.length > this.config.maxStorageEntries) {
      this.storage = this.storage.slice(-this.config.maxStorageEntries);
    }
  }

  /**
   * Write to remote endpoint
   */
  private async writeToRemote(entry: LogEntry): Promise<void> {
    if (!this.config.enableRemote || !this.config.remoteEndpoint) return;

    try {
      await fetch(this.config.remoteEndpoint, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: this.formatForStorage(entry),
      });
    } catch (error) {
      console.error('Failed to send log to remote endpoint:', error);
    }
  }

  /**
   * Log message
   */
  private log(level: LogLevel, message: string, data?: any, error?: Error): void {
    if (level < this.config.level) return;

    const entry = this.createLogEntry(level, message, data, error);
    
    this.writeToConsole(entry);
    this.writeToStorage(entry);
    
    if (this.config.enableRemote) {
      this.writeToRemote(entry).catch(console.error);
    }
  }

  /**
   * Debug log
   */
  debug(message: string, data?: any): void {
    this.log(LogLevel.DEBUG, message, data);
  }

  /**
   * Info log
   */
  info(message: string, data?: any): void {
    this.log(LogLevel.INFO, message, data);
  }

  /**
   * Warning log
   */
  warn(message: string, data?: any): void {
    this.log(LogLevel.WARN, message, data);
  }

  /**
   * Error log
   */
  error(message: string, error?: Error, data?: any): void {
    this.log(LogLevel.ERROR, message, data, error);
  }

  /**
   * Fatal log
   */
  fatal(message: string, error?: Error, data?: any): void {
    this.log(LogLevel.FATAL, message, data, error);
  }

  /**
   * Performance log
   */
  performance(name: string, startTime: number, endTime?: number): void {
    const duration = (endTime || performance.now()) - startTime;
    this.info(`Performance: ${name}`, { duration, startTime, endTime });
  }

  /**
   * API request log
   */
  apiRequest(method: string, url: string, status?: number, duration?: number): void {
    this.info(`API Request: ${method} ${url}`, { method, url, status, duration });
  }

  /**
   * API error log
   */
  apiError(method: string, url: string, error: Error, status?: number): void {
    this.error(`API Error: ${method} ${url}`, error, { method, url, status });
  }

  /**
   * User action log
   */
  userAction(action: string, data?: any): void {
    this.info(`User Action: ${action}`, data);
  }

  /**
   * Security log
   */
  security(event: string, data?: any): void {
    this.warn(`Security: ${event}`, data);
  }

  /**
   * Get stored logs
   */
  getLogs(level?: LogLevel, limit?: number): LogEntry[] {
    let logs = this.storage;
    
    if (level !== undefined) {
      logs = logs.filter(log => log.level >= level);
    }
    
    if (limit !== undefined) {
      logs = logs.slice(-limit);
    }
    
    return logs;
  }

  /**
   * Clear stored logs
   */
  clearLogs(): void {
    this.storage = [];
  }

  /**
   * Export logs
   */
  exportLogs(): string {
    return JSON.stringify(this.storage, null, 2);
  }

  /**
   * Set log level
   */
  setLevel(level: LogLevel): void {
    this.config.level = level;
  }

  /**
   * Enable/disable console logging
   */
  setConsoleEnabled(enabled: boolean): void {
    this.config.enableConsole = enabled;
  }

  /**
   * Enable/disable storage logging
   */
  setStorageEnabled(enabled: boolean): void {
    this.config.enableStorage = enabled;
  }

  /**
   * Enable/disable remote logging
   */
  setRemoteEnabled(enabled: boolean, endpoint?: string): void {
    this.config.enableRemote = enabled;
    if (endpoint) {
      this.config.remoteEndpoint = endpoint;
    }
  }

  /**
   * Set context
   */
  setContext(context: string): void {
    this.context = context;
  }
}

/**
 * Default logger instance
 */
export const logger = new Logger();

/**
 * Create custom logger
 */
export const createLogger = (config: Partial<LoggerConfig>): Logger => {
  return new Logger(config);
};

/**
 * Logger hooks for React components
 */
export const useLogger = (context?: string) => {
  const loggerInstance = React.useMemo(() => {
    const instance = new Logger({ context });
    return instance;
  }, [context]);

  return loggerInstance;
};

/**
 * Performance measurement utility
 */
export const measurePerformance = (name: string) => {
  const startTime = performance.now();
  
  return {
    end: () => {
      const endTime = performance.now();
      logger.performance(name, startTime, endTime);
      return endTime - startTime;
    },
  };
};

/**
 * API logging wrapper
 */
export const withApiLogging = async <T>(
  apiCall: () => Promise<T>,
  method: string,
  url: string
): Promise<T> => {
  const startTime = performance.now();
  
  try {
    const result = await apiCall();
    const duration = performance.now() - startTime;
    logger.apiRequest(method, url, 200, duration);
    return result;
  } catch (error) {
    const duration = performance.now() - startTime;
    logger.apiError(method, url, error as Error, undefined);
    throw error;
  }
};

/**
 * Error boundary logger
 */
export const logErrorBoundary = (error: Error, errorInfo: any) => {
  logger.error('Error Boundary caught error', error, errorInfo);
};

/**
 * Unhandled error logger
 */
export const logUnhandledError = (error: Error, isPromiseRejection = false) => {
  logger.error(
    isPromiseRejection ? 'Unhandled Promise Rejection' : 'Unhandled Error',
    error
  );
};

// Import React for hooks
import React from 'react';
