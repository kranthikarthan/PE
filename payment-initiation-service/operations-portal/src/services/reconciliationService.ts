/**
 * Reconciliation Service Client
 * 
 * API client for reconciliation monitoring, batch processing, and exception handling.
 */

import { BaseApiClient } from './baseApiClient';
import { HttpClient } from './httpClient';
import { 
  ReconciliationBatch, 
  ReconciliationMetrics, 
  ReconciliationException,
  ReconciliationReport,
  ReconciliationSearchCriteria,
  ReconciliationPerformanceMetric,
  ReconciliationBatchStatus,
  ReconciliationType
} from '@types/reconciliation';
import { ApiResponse, PagedResponse, PaginationParams } from '@types/api';
import { API_ENDPOINTS } from '@constants';

export class ReconciliationService extends BaseApiClient {
  constructor(httpClient: HttpClient) {
    super(httpClient);
  }

  /**
   * Get all reconciliation batches
   */
  async getAllBatches(
    pagination?: PaginationParams,
    searchCriteria?: ReconciliationSearchCriteria
  ): Promise<PagedResponse<ReconciliationBatch>> {
    const queryParams = {
      ...this.buildPaginationParams(pagination || {}),
      ...this.buildSearchParams(searchCriteria || {}),
    };

    const url = this.buildUrl(API_ENDPOINTS.RECONCILIATION.BATCHES, queryParams);
    const response = await this.httpClient.get<PagedResponse<ReconciliationBatch>>(url);
    
    return this.handlePagedResponse(response);
  }

  /**
   * Get batch by ID
   */
  async getBatchById(batchId: string): Promise<ReconciliationBatch> {
    this.validateRequired({ batchId }, ['batchId']);

    const response = await this.httpClient.get<ReconciliationBatch>(
      API_ENDPOINTS.RECONCILIATION.BATCH_DETAILS(batchId)
    );
    
    return this.handleResponse(response);
  }

  /**
   * Get batches by status
   */
  async getBatchesByStatus(
    status: ReconciliationBatchStatus,
    pagination?: PaginationParams
  ): Promise<PagedResponse<ReconciliationBatch>> {
    const queryParams = {
      status,
      ...this.buildPaginationParams(pagination || {}),
    };

    const url = this.buildUrl(API_ENDPOINTS.RECONCILIATION.BATCHES, queryParams);
    const response = await this.httpClient.get<PagedResponse<ReconciliationBatch>>(url);
    
    return this.handlePagedResponse(response);
  }

  /**
   * Get batches by type
   */
  async getBatchesByType(
    type: ReconciliationType,
    pagination?: PaginationParams
  ): Promise<PagedResponse<ReconciliationBatch>> {
    const queryParams = {
      type,
      ...this.buildPaginationParams(pagination || {}),
    };

    const url = this.buildUrl(API_ENDPOINTS.RECONCILIATION.BATCHES, queryParams);
    const response = await this.httpClient.get<PagedResponse<ReconciliationBatch>>(url);
    
    return this.handlePagedResponse(response);
  }

  /**
   * Get batches by date range
   */
  async getBatchesByDateRange(
    dateFrom: string,
    dateTo: string,
    pagination?: PaginationParams
  ): Promise<PagedResponse<ReconciliationBatch>> {
    const queryParams = {
      dateFrom: this.formatDate(new Date(dateFrom)),
      dateTo: this.formatDate(new Date(dateTo)),
      ...this.buildPaginationParams(pagination || {}),
    };

    const url = this.buildUrl(API_ENDPOINTS.RECONCILIATION.BATCHES, queryParams);
    const response = await this.httpClient.get<PagedResponse<ReconciliationBatch>>(url);
    
    return this.handlePagedResponse(response);
  }

  /**
   * Get reconciliation metrics
   */
  async getReconciliationMetrics(
    tenantId?: string,
    businessUnitId?: string,
    dateFrom?: string,
    dateTo?: string
  ): Promise<ReconciliationMetrics[]> {
    const queryParams: Record<string, any> = {};
    
    if (tenantId) queryParams.tenantId = tenantId;
    if (businessUnitId) queryParams.businessUnitId = businessUnitId;
    if (dateFrom) queryParams.dateFrom = this.formatDate(new Date(dateFrom));
    if (dateTo) queryParams.dateTo = this.formatDate(new Date(dateTo));

    const url = this.buildUrl(API_ENDPOINTS.RECONCILIATION.METRICS, queryParams);
    const response = await this.httpClient.get<ReconciliationMetrics[]>(url);
    
    return this.handleResponse(response);
  }

  /**
   * Get performance metrics
   */
  async getPerformanceMetrics(
    tenantId?: string,
    businessUnitId?: string
  ): Promise<ReconciliationPerformanceMetric[]> {
    const queryParams: Record<string, any> = {};
    
    if (tenantId) queryParams.tenantId = tenantId;
    if (businessUnitId) queryParams.businessUnitId = businessUnitId;

    const url = this.buildUrl(`${API_ENDPOINTS.RECONCILIATION.BASE}/performance-metrics`, queryParams);
    const response = await this.httpClient.get<ReconciliationPerformanceMetric[]>(url);
    
    return this.handleResponse(response);
  }

  /**
   * Get reconciliation exceptions
   */
  async getReconciliationExceptions(
    batchId?: string,
    severity?: 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL',
    status?: 'OPEN' | 'IN_PROGRESS' | 'RESOLVED' | 'IGNORED',
    pagination?: PaginationParams
  ): Promise<PagedResponse<ReconciliationException>> {
    const queryParams: Record<string, any> = {
      ...this.buildPaginationParams(pagination || {}),
    };
    
    if (batchId) queryParams.batchId = batchId;
    if (severity) queryParams.severity = severity;
    if (status) queryParams.status = status;

    const url = this.buildUrl(`${API_ENDPOINTS.RECONCILIATION.BASE}/exceptions`, queryParams);
    const response = await this.httpClient.get<PagedResponse<ReconciliationException>>(url);
    
    return this.handlePagedResponse(response);
  }

  /**
   * Get exception by ID
   */
  async getExceptionById(exceptionId: string): Promise<ReconciliationException> {
    this.validateRequired({ exceptionId }, ['exceptionId']);

    const response = await this.httpClient.get<ReconciliationException>(
      `${API_ENDPOINTS.RECONCILIATION.BASE}/exceptions/${exceptionId}`
    );
    
    return this.handleResponse(response);
  }

  /**
   * Resolve an exception
   */
  async resolveException(
    exceptionId: string, 
    resolution: string, 
    resolvedBy: string
  ): Promise<ReconciliationException> {
    this.validateRequired({ exceptionId, resolution, resolvedBy }, ['exceptionId', 'resolution', 'resolvedBy']);

    const response = await this.httpClient.post<ReconciliationException>(
      `${API_ENDPOINTS.RECONCILIATION.BASE}/exceptions/${exceptionId}/resolve`,
      {
        resolution: this.sanitizeString(resolution),
        resolvedBy: this.sanitizeString(resolvedBy),
      }
    );
    
    return this.handleResponse(response);
  }

  /**
   * Ignore an exception
   */
  async ignoreException(
    exceptionId: string, 
    reason: string, 
    ignoredBy: string
  ): Promise<ReconciliationException> {
    this.validateRequired({ exceptionId, reason, ignoredBy }, ['exceptionId', 'reason', 'ignoredBy']);

    const response = await this.httpClient.post<ReconciliationException>(
      `${API_ENDPOINTS.RECONCILIATION.BASE}/exceptions/${exceptionId}/ignore`,
      {
        reason: this.sanitizeString(reason),
        ignoredBy: this.sanitizeString(ignoredBy),
      }
    );
    
    return this.handleResponse(response);
  }

  /**
   * Get reconciliation reports
   */
  async getReconciliationReports(
    batchId?: string,
    reportType?: 'SUMMARY' | 'DETAILED' | 'EXCEPTIONS',
    format?: 'CSV' | 'EXCEL' | 'PDF',
    pagination?: PaginationParams
  ): Promise<PagedResponse<ReconciliationReport>> {
    const queryParams: Record<string, any> = {
      ...this.buildPaginationParams(pagination || {}),
    };
    
    if (batchId) queryParams.batchId = batchId;
    if (reportType) queryParams.reportType = reportType;
    if (format) queryParams.format = format;

    const url = this.buildUrl(API_ENDPOINTS.RECONCILIATION.REPORTS, queryParams);
    const response = await this.httpClient.get<PagedResponse<ReconciliationReport>>(url);
    
    return this.handlePagedResponse(response);
  }

  /**
   * Generate reconciliation report
   */
  async generateReport(
    batchId: string,
    reportType: 'SUMMARY' | 'DETAILED' | 'EXCEPTIONS',
    format: 'CSV' | 'EXCEL' | 'PDF'
  ): Promise<ReconciliationReport> {
    this.validateRequired({ batchId, reportType, format }, ['batchId', 'reportType', 'format']);

    const response = await this.httpClient.post<ReconciliationReport>(
      API_ENDPOINTS.RECONCILIATION.REPORTS,
      {
        batchId,
        reportType,
        format,
      }
    );
    
    return this.handleResponse(response);
  }

  /**
   * Download reconciliation report
   */
  async downloadReport(reportId: string): Promise<Blob> {
    this.validateRequired({ reportId }, ['reportId']);

    const response = await this.httpClient.getInstance().get(
      `${API_ENDPOINTS.RECONCILIATION.REPORTS}/${reportId}/download`,
      { responseType: 'blob' }
    );
    
    return response.data;
  }

  /**
   * Get active batches (processing or pending)
   */
  async getActiveBatches(): Promise<ReconciliationBatch[]> {
    const response = await this.httpClient.get<ReconciliationBatch[]>(
      `${API_ENDPOINTS.RECONCILIATION.BASE}/active-batches`
    );
    
    return this.handleResponse(response);
  }

  /**
   * Get batch summary statistics
   */
  async getBatchSummary(
    tenantId?: string,
    businessUnitId?: string,
    dateFrom?: string,
    dateTo?: string
  ): Promise<{
    totalBatches: number;
    completedBatches: number;
    processingBatches: number;
    failedBatches: number;
    totalRecords: number;
    processedRecords: number;
    failedRecords: number;
    averageSuccessRate: number;
    averageProcessingTime: string;
  }> {
    const queryParams: Record<string, any> = {};
    
    if (tenantId) queryParams.tenantId = tenantId;
    if (businessUnitId) queryParams.businessUnitId = businessUnitId;
    if (dateFrom) queryParams.dateFrom = this.formatDate(new Date(dateFrom));
    if (dateTo) queryParams.dateTo = this.formatDate(new Date(dateTo));

    const url = this.buildUrl(`${API_ENDPOINTS.RECONCILIATION.BASE}/summary`, queryParams);
    const response = await this.httpClient.get<any>(url);
    
    return this.handleResponse(response);
  }

  /**
   * Retry failed batch
   */
  async retryBatch(batchId: string, reason?: string): Promise<ReconciliationBatch> {
    this.validateRequired({ batchId }, ['batchId']);

    const response = await this.httpClient.post<ReconciliationBatch>(
      `${API_ENDPOINTS.RECONCILIATION.BATCH_DETAILS(batchId)}/retry`,
      { reason: reason || 'Retry initiated by operations team' }
    );
    
    return this.handleResponse(response);
  }

  /**
   * Cancel batch processing
   */
  async cancelBatch(batchId: string, reason?: string): Promise<ReconciliationBatch> {
    this.validateRequired({ batchId }, ['batchId']);

    const response = await this.httpClient.post<ReconciliationBatch>(
      `${API_ENDPOINTS.RECONCILIATION.BATCH_DETAILS(batchId)}/cancel`,
      { reason: reason || 'Cancelled by operations team' }
    );
    
    return this.handleResponse(response);
  }
}
