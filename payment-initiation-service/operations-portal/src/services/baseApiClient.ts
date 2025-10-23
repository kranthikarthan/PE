/**
 * Base API Client
 * 
 * Abstract base class for all API service clients.
 * Provides common functionality for API calls, error handling, and response transformation.
 */

import { HttpClient } from './httpClient';
import { ApiResponse, PagedResponse, PaginationParams, SearchParams } from '../types/api';

export abstract class BaseApiClient {
  protected httpClient: HttpClient;

  constructor(httpClient: HttpClient) {
    this.httpClient = httpClient;
  }

  /**
   * Handle API response and extract data
   */
  protected handleResponse<T>(response: ApiResponse<T>): T {
    if (!response.success) {
      throw new Error(response.message || 'API request failed');
    }
    return response.data;
  }

  /**
   * Handle paginated response
   */
  protected handlePagedResponse<T>(response: ApiResponse<PagedResponse<T>>): PagedResponse<T> {
    if (!response.success) {
      throw new Error(response.message || 'API request failed');
    }
    return response.data;
  }

  /**
   * Build query parameters for pagination
   */
  protected buildPaginationParams(params: PaginationParams): Record<string, any> {
    const queryParams: Record<string, any> = {};
    
    if (params.page !== undefined) {
      queryParams.page = params.page;
    }
    if (params.size !== undefined) {
      queryParams.size = params.size;
    }
    if (params.sort) {
      queryParams.sort = params.sort;
    }
    if (params.direction) {
      queryParams.direction = params.direction;
    }
    
    return queryParams;
  }

  /**
   * Build query parameters for search
   */
  protected buildSearchParams(params: SearchParams): Record<string, any> {
    const queryParams: Record<string, any> = {};
    
    if (params.query) {
      queryParams.q = params.query;
    }
    if (params.dateFrom) {
      queryParams.dateFrom = params.dateFrom;
    }
    if (params.dateTo) {
      queryParams.dateTo = params.dateTo;
    }
    if (params.filters) {
      Object.entries(params.filters).forEach(([key, value]) => {
        if (value !== undefined && value !== null && value !== '') {
          queryParams[key] = value;
        }
      });
    }
    
    return queryParams;
  }

  /**
   * Build URL with query parameters
   */
  protected buildUrl(endpoint: string, params?: Record<string, any>): string {
    if (!params || Object.keys(params).length === 0) {
      return endpoint;
    }

    const searchParams = new URLSearchParams();
    Object.entries(params).forEach(([key, value]) => {
      if (value !== undefined && value !== null && value !== '') {
        searchParams.append(key, String(value));
      }
    });

    const queryString = searchParams.toString();
    return queryString ? `${endpoint}?${queryString}` : endpoint;
  }

  /**
   * Handle API errors with proper error messages
   */
  protected handleError(error: any): never {
    if (error.response?.data) {
      const errorData = error.response.data;
      throw new Error(errorData.message || errorData.error || 'API request failed');
    }
    
    if (error.message) {
      throw new Error(error.message);
    }
    
    throw new Error('An unexpected error occurred');
  }

  /**
   * Create a standardized API response
   */
  protected createResponse<T>(data: T, success: boolean = true, message?: string): ApiResponse<T> {
    return {
      data,
      success,
      message,
      timestamp: new Date().toISOString(),
      correlationId: this.generateCorrelationId(),
    };
  }

  /**
   * Generate correlation ID for request tracking
   */
  private generateCorrelationId(): string {
    return `req_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`;
  }

  /**
   * Validate required parameters
   */
  protected validateRequired(params: Record<string, any>, requiredFields: string[]): void {
    const missingFields = requiredFields.filter(field => 
      params[field] === undefined || params[field] === null || params[field] === ''
    );
    
    if (missingFields.length > 0) {
      throw new Error(`Missing required fields: ${missingFields.join(', ')}`);
    }
  }

  /**
   * Format date for API calls
   */
  protected formatDate(date: Date | string): string {
    if (typeof date === 'string') {
      return date;
    }
    return date.toISOString();
  }

  /**
   * Parse date from API response
   */
  protected parseDate(dateString: string): Date {
    return new Date(dateString);
  }

  /**
   * Sanitize string input
   */
  protected sanitizeString(input: string): string {
    return input.trim().replace(/[<>]/g, '');
  }

  /**
   * Validate UUID format
   */
  protected validateUUID(uuid: string): boolean {
    const uuidRegex = /^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$/i;
    return uuidRegex.test(uuid);
  }

  /**
   * Validate email format
   */
  protected validateEmail(email: string): boolean {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return emailRegex.test(email);
  }

  /**
   * Validate URL format
   */
  protected validateURL(url: string): boolean {
    try {
      new URL(url);
      return true;
    } catch {
      return false;
    }
  }
}
