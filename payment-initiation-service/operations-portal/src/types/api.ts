/**
 * API Response Types
 * 
 * Common response wrappers and error types for all API calls.
 */

export interface ApiResponse<T = any> {
  data: T;
  success: boolean;
  message?: string;
  timestamp: string;
  correlationId: string;
}

export interface PagedResponse<T = any> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
  numberOfElements: number;
}

export interface ErrorResponse {
  error: string;
  message: string;
  status: number;
  timestamp: string;
  path: string;
  correlationId?: string;
  details?: Record<string, any>;
}

export interface ApiError {
  code: string;
  message: string;
  details?: Record<string, any>;
}

export interface PaginationParams {
  page?: number;
  size?: number;
  sort?: string;
  direction?: 'asc' | 'desc';
}

export interface SearchParams {
  query?: string;
  filters?: Record<string, any>;
  dateFrom?: string;
  dateTo?: string;
}

export interface TenantContext {
  tenantId: string;
  businessUnitId: string;
  userId: string;
  correlationId: string;
}

export interface RequestHeaders {
  'Authorization'?: string;
  'X-Tenant-ID'?: string;
  'X-Business-Unit-ID'?: string;
  'X-Correlation-ID'?: string;
  'X-User-ID'?: string;
  'Content-Type'?: string;
}
