/**
 * Transaction Processing Service Client
 * 
 * API client for transaction queries, search, and export operations.
 */

import { BaseApiClient } from './baseApiClient';
import { HttpClient } from './httpClient';
import { 
  Transaction, 
  TransactionSearchCriteria, 
  TransactionMetrics,
  TransactionExportRequest,
  TransactionExportResponse,
  TransactionStatus,
  TransactionType
} from '../types/transaction';
import { ApiResponse, PagedResponse, PaginationParams } from '../types/api';
import { API_ENDPOINTS } from '../constants';

export class TransactionProcessingService extends BaseApiClient {
  constructor(httpClient: HttpClient) {
    super(httpClient);
  }

  /**
   * Get transaction by ID
   */
  async getTransactionById(transactionId: string): Promise<Transaction> {
    if (!this.validateUUID(transactionId)) {
      throw new Error('Invalid transaction ID format');
    }

    const response = await this.httpClient.get<Transaction>(
      API_ENDPOINTS.TRANSACTIONS.GET_BY_ID(transactionId)
    );
    
    return this.handleResponse(response);
  }

  /**
   * Search transactions with criteria
   */
  async searchTransactions(
    criteria: TransactionSearchCriteria,
    pagination?: PaginationParams
  ): Promise<PagedResponse<Transaction>> {
    const queryParams = {
      ...this.buildSearchParams(criteria),
      ...this.buildPaginationParams(pagination || {}),
    };

    const url = this.buildUrl(API_ENDPOINTS.TRANSACTIONS.SEARCH, queryParams);
    const response = await this.httpClient.get<PagedResponse<Transaction>>(url);
    
    return this.handlePagedResponse(response);
  }

  /**
   * Get transactions by status
   */
  async getTransactionsByStatus(
    status: TransactionStatus,
    pagination?: PaginationParams
  ): Promise<PagedResponse<Transaction>> {
    const queryParams = {
      status,
      ...this.buildPaginationParams(pagination || {}),
    };

    const url = this.buildUrl(API_ENDPOINTS.TRANSACTIONS.SEARCH, queryParams);
    const response = await this.httpClient.get<PagedResponse<Transaction>>(url);
    
    return this.handlePagedResponse(response);
  }

  /**
   * Get transactions by type
   */
  async getTransactionsByType(
    type: TransactionType,
    pagination?: PaginationParams
  ): Promise<PagedResponse<Transaction>> {
    const queryParams = {
      type,
      ...this.buildPaginationParams(pagination || {}),
    };

    const url = this.buildUrl(API_ENDPOINTS.TRANSACTIONS.SEARCH, queryParams);
    const response = await this.httpClient.get<PagedResponse<Transaction>>(url);
    
    return this.handlePagedResponse(response);
  }

  /**
   * Get transactions by date range
   */
  async getTransactionsByDateRange(
    dateFrom: string,
    dateTo: string,
    pagination?: PaginationParams
  ): Promise<PagedResponse<Transaction>> {
    const queryParams = {
      dateFrom: this.formatDate(new Date(dateFrom)),
      dateTo: this.formatDate(new Date(dateTo)),
      ...this.buildPaginationParams(pagination || {}),
    };

    const url = this.buildUrl(API_ENDPOINTS.TRANSACTIONS.SEARCH, queryParams);
    const response = await this.httpClient.get<PagedResponse<Transaction>>(url);
    
    return this.handlePagedResponse(response);
  }

  /**
   * Get transactions by amount range
   */
  async getTransactionsByAmountRange(
    amountFrom: number,
    amountTo: number,
    pagination?: PaginationParams
  ): Promise<PagedResponse<Transaction>> {
    const queryParams = {
      amountFrom,
      amountTo,
      ...this.buildPaginationParams(pagination || {}),
    };

    const url = this.buildUrl(API_ENDPOINTS.TRANSACTIONS.SEARCH, queryParams);
    const response = await this.httpClient.get<PagedResponse<Transaction>>(url);
    
    return this.handlePagedResponse(response);
  }

  /**
   * Get transactions by reference
   */
  async getTransactionsByReference(
    reference: string,
    pagination?: PaginationParams
  ): Promise<PagedResponse<Transaction>> {
    this.validateRequired({ reference }, ['reference']);

    const queryParams = {
      reference: this.sanitizeString(reference),
      ...this.buildPaginationParams(pagination || {}),
    };

    const url = this.buildUrl(API_ENDPOINTS.TRANSACTIONS.SEARCH, queryParams);
    const response = await this.httpClient.get<PagedResponse<Transaction>>(url);
    
    return this.handlePagedResponse(response);
  }

  /**
   * Get transaction metrics
   */
  async getTransactionMetrics(
    tenantId?: string,
    businessUnitId?: string,
    dateFrom?: string,
    dateTo?: string
  ): Promise<TransactionMetrics> {
    const queryParams: Record<string, any> = {};
    
    if (tenantId) queryParams.tenantId = tenantId;
    if (businessUnitId) queryParams.businessUnitId = businessUnitId;
    if (dateFrom) queryParams.dateFrom = this.formatDate(new Date(dateFrom));
    if (dateTo) queryParams.dateTo = this.formatDate(new Date(dateTo));

    const url = this.buildUrl(API_ENDPOINTS.TRANSACTIONS.METRICS, queryParams);
    const response = await this.httpClient.get<TransactionMetrics>(url);
    
    return this.handleResponse(response);
  }

  /**
   * Export transactions
   */
  async exportTransactions(exportRequest: TransactionExportRequest): Promise<TransactionExportResponse> {
    this.validateRequired(exportRequest, ['format', 'criteria']);

    const response = await this.httpClient.post<TransactionExportResponse>(
      API_ENDPOINTS.TRANSACTIONS.EXPORT,
      exportRequest
    );
    
    return this.handleResponse(response);
  }

  /**
   * Get export status
   */
  async getExportStatus(exportId: string): Promise<TransactionExportResponse> {
    this.validateRequired({ exportId }, ['exportId']);

    const response = await this.httpClient.get<TransactionExportResponse>(
      `${API_ENDPOINTS.TRANSACTIONS.EXPORT}/${exportId}/status`
    );
    
    return this.handleResponse(response);
  }

  /**
   * Download export file
   */
  async downloadExport(exportId: string): Promise<Blob> {
    this.validateRequired({ exportId }, ['exportId']);

    const response = await this.httpClient.getInstance().get(
      `${API_ENDPOINTS.TRANSACTIONS.EXPORT}/${exportId}/download`,
      { responseType: 'blob' }
    );
    
    return response.data;
  }

  /**
   * Get recent transactions
   */
  async getRecentTransactions(
    limit: number = 10,
    tenantId?: string,
    businessUnitId?: string
  ): Promise<Transaction[]> {
    const queryParams: Record<string, any> = {
      limit: Math.min(limit, 100), // Cap at 100
    };
    
    if (tenantId) queryParams.tenantId = tenantId;
    if (businessUnitId) queryParams.businessUnitId = businessUnitId;

    const url = this.buildUrl(`${API_ENDPOINTS.TRANSACTIONS.BASE}/recent`, queryParams);
    const response = await this.httpClient.get<Transaction[]>(url);
    
    return this.handleResponse(response);
  }

  /**
   * Get transaction statistics
   */
  async getTransactionStatistics(
    tenantId?: string,
    businessUnitId?: string,
    dateFrom?: string,
    dateTo?: string
  ): Promise<{
    totalTransactions: number;
    totalVolume: number;
    averageAmount: number;
    successRate: number;
    topCurrencies: Array<{ currency: string; count: number; volume: number }>;
    topTypes: Array<{ type: TransactionType; count: number; volume: number }>;
  }> {
    const queryParams: Record<string, any> = {};
    
    if (tenantId) queryParams.tenantId = tenantId;
    if (businessUnitId) queryParams.businessUnitId = businessUnitId;
    if (dateFrom) queryParams.dateFrom = this.formatDate(new Date(dateFrom));
    if (dateTo) queryParams.dateTo = this.formatDate(new Date(dateTo));

    const url = this.buildUrl(`${API_ENDPOINTS.TRANSACTIONS.BASE}/statistics`, queryParams);
    const response = await this.httpClient.get<any>(url);
    
    return this.handleResponse(response);
  }

  /**
   * Get failed transactions
   */
  async getFailedTransactions(pagination?: PaginationParams): Promise<PagedResponse<Transaction>> {
    return this.getTransactionsByStatus(TransactionStatus.FAILED, pagination);
  }

  /**
   * Get pending transactions
   */
  async getPendingTransactions(pagination?: PaginationParams): Promise<PagedResponse<Transaction>> {
    return this.getTransactionsByStatus(TransactionStatus.PENDING, pagination);
  }

  /**
   * Get completed transactions
   */
  async getCompletedTransactions(pagination?: PaginationParams): Promise<PagedResponse<Transaction>> {
    return this.getTransactionsByStatus(TransactionStatus.COMPLETED, pagination);
  }
}
