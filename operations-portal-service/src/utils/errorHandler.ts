/**
 * Comprehensive error handling utility
 */

import { 
  AppError, 
  AnyError, 
  ErrorType, 
  ErrorSeverity, 
  ErrorContext,
  ErrorHandlerConfig,
  NetworkError,
  ApiError,
  ValidationError,
  AuthError,
  TimeoutError,
  ServerError,
  ClientError
} from './errorTypes';

class ErrorHandler {
  private config: ErrorHandlerConfig;
  private errorQueue: AppError[] = [];
  private retryCounts: Map<string, number> = new Map();

  constructor(config: Partial<ErrorHandlerConfig> = {}) {
    this.config = {
      maxRetries: 3,
      retryDelay: 1000,
      timeout: 30000,
      enableLogging: true,
      enableReporting: true,
      enableUserNotifications: true,
      ...config
    };
  }

  /**
   * Handle any error and convert to AppError
   */
  handleError(error: any, context?: Partial<ErrorContext>): AppError {
    const appError = this.convertToAppError(error, context);
    
    if (this.config.enableLogging) {
      this.logError(appError);
    }
    
    if (this.config.enableReporting) {
      this.reportError(appError);
    }
    
    if (this.config.enableUserNotifications) {
      this.notifyUser(appError);
    }
    
    return appError;
  }

  /**
   * Convert any error to AppError
   */
  private convertToAppError(error: any, context?: Partial<ErrorContext>): AppError {
    const timestamp = new Date();
    const errorContext: ErrorContext = {
      timestamp,
      ...context
    };

    // Network errors
    if (error.code === 'NETWORK_ERROR' || error.message?.includes('Network Error')) {
      return this.createNetworkError(error, errorContext);
    }

    // API errors (Axios errors)
    if (error.response) {
      return this.createApiError(error, errorContext);
    }

    // Timeout errors
    if (error.code === 'ECONNABORTED' || error.message?.includes('timeout')) {
      return this.createTimeoutError(error, errorContext);
    }

    // Validation errors
    if (error.name === 'ValidationError' || error.type === 'validation') {
      return this.createValidationError(error, errorContext);
    }

    // Authentication/Authorization errors
    if (error.status === 401 || error.status === 403) {
      return this.createAuthError(error, errorContext);
    }

    // Server errors
    if (error.status >= 500) {
      return this.createServerError(error, errorContext);
    }

    // Client errors
    if (error.status >= 400 && error.status < 500) {
      return this.createClientError(error, errorContext);
    }

    // Unknown error
    return this.createUnknownError(error, errorContext);
  }

  private createNetworkError(error: any, context: ErrorContext): NetworkError {
    return {
      type: ErrorType.NETWORK,
      severity: ErrorSeverity.HIGH,
      message: error.message || 'Network connection failed',
      code: error.code,
      timestamp: context.timestamp,
      source: 'NetworkHandler',
      userMessage: 'Unable to connect to the server. Please check your internet connection.',
      retryable: true,
      retryAfter: 5,
      status: error.status,
      url: error.config?.url,
      method: error.config?.method
    };
  }

  private createApiError(error: any, context: ErrorContext): ApiError {
    const status = error.response?.status || 500;
    const severity = this.getSeverityFromStatus(status);
    
    return {
      type: ErrorType.API,
      severity,
      message: error.response?.data?.message || error.message || 'API request failed',
      code: error.response?.data?.code || error.code,
      timestamp: context.timestamp,
      source: 'ApiHandler',
      userMessage: this.getUserMessageFromStatus(status),
      retryable: this.isRetryableStatus(status),
      retryAfter: this.getRetryAfterFromStatus(status),
      status,
      endpoint: error.config?.url,
      method: error.config?.method,
      response: error.response?.data
    };
  }

  private createValidationError(error: any, context: ErrorContext): ValidationError {
    return {
      type: ErrorType.VALIDATION,
      severity: ErrorSeverity.MEDIUM,
      message: error.message || 'Validation failed',
      code: error.code,
      timestamp: context.timestamp,
      source: 'ValidationHandler',
      userMessage: 'Please check your input and try again.',
      retryable: false,
      field: error.field || 'unknown',
      value: error.value,
      rule: error.rule || 'validation'
    };
  }

  private createAuthError(error: any, context: ErrorContext): AuthError {
    const isAuth = error.status === 401;
    const type = isAuth ? ErrorType.AUTHENTICATION : ErrorType.AUTHORIZATION;
    
    return {
      type,
      severity: ErrorSeverity.HIGH,
      message: error.message || (isAuth ? 'Authentication failed' : 'Access denied'),
      code: error.code,
      timestamp: context.timestamp,
      source: 'AuthHandler',
      userMessage: isAuth 
        ? 'Your session has expired. Please log in again.'
        : 'You do not have permission to perform this action.',
      retryable: isAuth ? false : true,
      action: error.action,
      resource: error.resource
    };
  }

  private createTimeoutError(error: any, context: ErrorContext): TimeoutError {
    return {
      type: ErrorType.TIMEOUT,
      severity: ErrorSeverity.MEDIUM,
      message: error.message || 'Request timeout',
      code: error.code,
      timestamp: context.timestamp,
      source: 'TimeoutHandler',
      userMessage: 'The request took too long to complete. Please try again.',
      retryable: true,
      retryAfter: 3,
      timeout: error.timeout || this.config.timeout,
      operation: error.operation || 'API request'
    };
  }

  private createServerError(error: any, context: ErrorContext): ServerError {
    return {
      type: ErrorType.SERVER,
      severity: ErrorSeverity.HIGH,
      message: error.message || 'Server error occurred',
      code: error.code,
      timestamp: context.timestamp,
      source: 'ServerHandler',
      userMessage: 'A server error occurred. Please try again later.',
      retryable: true,
      retryAfter: 10,
      status: error.status || 500,
      endpoint: error.config?.url,
      traceId: error.traceId
    };
  }

  private createClientError(error: any, context: ErrorContext): ClientError {
    return {
      type: ErrorType.CLIENT,
      severity: ErrorSeverity.MEDIUM,
      message: error.message || 'Client error occurred',
      code: error.code,
      timestamp: context.timestamp,
      source: 'ClientHandler',
      userMessage: 'There was an error with your request. Please check your input.',
      retryable: false,
      component: error.component || 'unknown',
      action: error.action || 'unknown'
    };
  }

  private createUnknownError(error: any, context: ErrorContext): AppError {
    return {
      type: ErrorType.UNKNOWN,
      severity: ErrorSeverity.MEDIUM,
      message: error.message || 'An unknown error occurred',
      code: error.code,
      timestamp: context.timestamp,
      source: 'UnknownHandler',
      userMessage: 'An unexpected error occurred. Please try again.',
      retryable: true,
      retryAfter: 5
    };
  }

  private getSeverityFromStatus(status: number): ErrorSeverity {
    if (status >= 500) return ErrorSeverity.HIGH;
    if (status >= 400) return ErrorSeverity.MEDIUM;
    return ErrorSeverity.LOW;
  }

  private getUserMessageFromStatus(status: number): string {
    switch (status) {
      case 400: return 'Invalid request. Please check your input.';
      case 401: return 'Authentication required. Please log in.';
      case 403: return 'Access denied. You do not have permission.';
      case 404: return 'Resource not found.';
      case 409: return 'Conflict. The resource already exists.';
      case 422: return 'Validation failed. Please check your input.';
      case 429: return 'Too many requests. Please try again later.';
      case 500: return 'Server error. Please try again later.';
      case 502: return 'Bad gateway. Please try again later.';
      case 503: return 'Service unavailable. Please try again later.';
      case 504: return 'Gateway timeout. Please try again later.';
      default: return 'An error occurred. Please try again.';
    }
  }

  private isRetryableStatus(status: number): boolean {
    return status >= 500 || status === 429 || status === 408;
  }

  private getRetryAfterFromStatus(status: number): number {
    if (status === 429) return 60; // 1 minute for rate limiting
    if (status >= 500) return 10; // 10 seconds for server errors
    return 5; // Default 5 seconds
  }

  private logError(error: AppError): void {
    const logLevel = this.getLogLevel(error.severity);
    console[logLevel](`[${error.type}] ${error.message}`, {
      severity: error.severity,
      source: error.source,
      timestamp: error.timestamp,
      retryable: error.retryable,
      details: error.details
    });
  }

  private getLogLevel(severity: ErrorSeverity): 'log' | 'warn' | 'error' {
    switch (severity) {
      case ErrorSeverity.LOW: return 'log';
      case ErrorSeverity.MEDIUM: return 'warn';
      case ErrorSeverity.HIGH: return 'error';
      case ErrorSeverity.CRITICAL: return 'error';
      default: return 'log';
    }
  }

  private reportError(error: AppError): void {
    // Add to error queue for batch reporting
    this.errorQueue.push(error);
    
    // Report critical errors immediately
    if (error.severity === ErrorSeverity.CRITICAL) {
      this.sendErrorReport(error);
    }
  }

  private notifyUser(error: AppError): void {
    // Only notify for high severity errors or non-retryable errors
    if (error.severity >= ErrorSeverity.HIGH || !error.retryable) {
      // This would integrate with the notification system
      console.warn('User notification:', error.userMessage);
    }
  }

  private sendErrorReport(error: AppError): void {
    // In a real application, this would send to an error reporting service
    console.error('Error report:', {
      type: error.type,
      severity: error.severity,
      message: error.message,
      timestamp: error.timestamp,
      source: error.source
    });
  }

  /**
   * Check if an error should be retried
   */
  shouldRetry(error: AppError, attempt: number): boolean {
    if (!error.retryable || attempt >= this.config.maxRetries) {
      return false;
    }

    const retryKey = `${error.type}-${error.code}`;
    const currentRetries = this.retryCounts.get(retryKey) || 0;
    
    if (currentRetries >= this.config.maxRetries) {
      return false;
    }

    this.retryCounts.set(retryKey, currentRetries + 1);
    return true;
  }

  /**
   * Get retry delay for an error
   */
  getRetryDelay(error: AppError, attempt: number): number {
    const baseDelay = error.retryAfter || this.config.retryDelay;
    return baseDelay * Math.pow(2, attempt); // Exponential backoff
  }

  /**
   * Clear retry counts (call on successful operations)
   */
  clearRetryCounts(): void {
    this.retryCounts.clear();
  }

  /**
   * Get error statistics
   */
  getErrorStats(): { total: number; byType: Record<string, number>; bySeverity: Record<string, number> } {
    const byType: Record<string, number> = {};
    const bySeverity: Record<string, number> = {};

    this.errorQueue.forEach(error => {
      byType[error.type] = (byType[error.type] || 0) + 1;
      bySeverity[error.severity] = (bySeverity[error.severity] || 0) + 1;
    });

    return {
      total: this.errorQueue.length,
      byType,
      bySeverity
    };
  }

  /**
   * Clear error queue
   */
  clearErrorQueue(): void {
    this.errorQueue = [];
  }
}

// Export singleton instance
export const errorHandler = new ErrorHandler();

// Export class for custom instances
export { ErrorHandler };