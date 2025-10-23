/**
 * Payment Initiation Service Client
 * 
 * API client for payment initiation operations including CRUD operations,
 * payment repair, and metrics.
 */

import { BaseApiClient } from './baseApiClient';
import { HttpClient } from './httpClient';
import { 
  Payment, 
  PaymentInitiationRequest, 
  PaymentInitiationResponse, 
  PaymentRepairRequest, 
  PaymentRepairResponse,
  PaymentRepairLog,
  PaymentSearchCriteria,
  PaymentMetrics,
  PaymentStatus,
  Currency
} from '../types/payment';
import { ApiResponse, PagedResponse, PaginationParams } from '../types/api';
import { API_ENDPOINTS } from '../constants';

export class PaymentInitiationService extends BaseApiClient {
  constructor(httpClient: HttpClient) {
    super(httpClient);
  }

  /**
   * Initiate a new payment
   */
  async initiatePayment(request: PaymentInitiationRequest): Promise<PaymentInitiationResponse> {
    this.validateRequired(request, ['amount', 'currency', 'method', 'sourceAccount', 'destinationAccount', 'reference']);
    
    const response = await this.httpClient.post<PaymentInitiationResponse>(
      API_ENDPOINTS.PAYMENTS.INITIATE,
      request
    );
    
    return this.handleResponse(response);
  }

  /**
   * Get payment by ID
   */
  async getPaymentById(paymentId: string): Promise<Payment> {
    if (!this.validateUUID(paymentId)) {
      throw new Error('Invalid payment ID format');
    }

    const response = await this.httpClient.get<Payment>(
      API_ENDPOINTS.PAYMENTS.GET_BY_ID(paymentId)
    );
    
    return this.handleResponse(response);
  }

  /**
   * Search payments with criteria
   */
  async searchPayments(
    criteria: PaymentSearchCriteria,
    pagination?: PaginationParams
  ): Promise<PagedResponse<Payment>> {
    const queryParams = {
      ...this.buildSearchParams(criteria),
      ...this.buildPaginationParams(pagination || {}),
    };

    const url = this.buildUrl(API_ENDPOINTS.PAYMENTS.SEARCH, queryParams);
    const response = await this.httpClient.get<PagedResponse<Payment>>(url);
    
    return this.handlePagedResponse(response);
  }

  /**
   * Repair a failed payment
   */
  async repairPayment(paymentId: string, repairRequest: PaymentRepairRequest): Promise<PaymentRepairResponse> {
    if (!this.validateUUID(paymentId)) {
      throw new Error('Invalid payment ID format');
    }

    this.validateRequired(repairRequest, ['reason']);

    const response = await this.httpClient.post<PaymentRepairResponse>(
      API_ENDPOINTS.PAYMENTS.REPAIR(paymentId),
      repairRequest
    );
    
    return this.handleResponse(response);
  }

  /**
   * Cancel a payment
   */
  async cancelPayment(paymentId: string, reason?: string): Promise<PaymentRepairResponse> {
    if (!this.validateUUID(paymentId)) {
      throw new Error('Invalid payment ID format');
    }

    const response = await this.httpClient.post<PaymentRepairResponse>(
      API_ENDPOINTS.PAYMENTS.CANCEL(paymentId),
      { reason: reason || 'Cancelled by operations team' }
    );
    
    return this.handleResponse(response);
  }

  /**
   * Retry a failed payment
   */
  async retryPayment(paymentId: string, reason?: string): Promise<PaymentRepairResponse> {
    if (!this.validateUUID(paymentId)) {
      throw new Error('Invalid payment ID format');
    }

    const response = await this.httpClient.post<PaymentRepairResponse>(
      API_ENDPOINTS.PAYMENTS.RETRY(paymentId),
      { reason: reason || 'Retry initiated by operations team' }
    );
    
    return this.handleResponse(response);
  }

  /**
   * Get payment repair history
   */
  async getPaymentRepairHistory(paymentId: string): Promise<PaymentRepairLog[]> {
    if (!this.validateUUID(paymentId)) {
      throw new Error('Invalid payment ID format');
    }

    const response = await this.httpClient.get<PaymentRepairLog[]>(
      `${API_ENDPOINTS.PAYMENTS.GET_BY_ID(paymentId)}/repair-history`
    );
    
    return this.handleResponse(response);
  }

  /**
   * Get payment metrics
   */
  async getPaymentMetrics(
    tenantId?: string,
    businessUnitId?: string,
    dateFrom?: string,
    dateTo?: string
  ): Promise<PaymentMetrics> {
    const queryParams: Record<string, any> = {};
    
    if (tenantId) queryParams.tenantId = tenantId;
    if (businessUnitId) queryParams.businessUnitId = businessUnitId;
    if (dateFrom) queryParams.dateFrom = this.formatDate(new Date(dateFrom));
    if (dateTo) queryParams.dateTo = this.formatDate(new Date(dateTo));

    const url = this.buildUrl(API_ENDPOINTS.PAYMENTS.METRICS, queryParams);
    const response = await this.httpClient.get<PaymentMetrics>(url);
    
    return this.handleResponse(response);
  }

  /**
   * Get payments by status
   */
  async getPaymentsByStatus(
    status: PaymentStatus,
    pagination?: PaginationParams
  ): Promise<PagedResponse<Payment>> {
    const queryParams = {
      status,
      ...this.buildPaginationParams(pagination || {}),
    };

    const url = this.buildUrl(API_ENDPOINTS.PAYMENTS.SEARCH, queryParams);
    const response = await this.httpClient.get<PagedResponse<Payment>>(url);
    
    return this.handlePagedResponse(response);
  }

  /**
   * Get failed payments for repair
   */
  async getFailedPayments(pagination?: PaginationParams): Promise<PagedResponse<Payment>> {
    return this.getPaymentsByStatus(PaymentStatus.FAILED, pagination);
  }

  /**
   * Get payments by currency
   */
  async getPaymentsByCurrency(
    currency: Currency,
    pagination?: PaginationParams
  ): Promise<PagedResponse<Payment>> {
    const queryParams = {
      currency,
      ...this.buildPaginationParams(pagination || {}),
    };

    const url = this.buildUrl(API_ENDPOINTS.PAYMENTS.SEARCH, queryParams);
    const response = await this.httpClient.get<PagedResponse<Payment>>(url);
    
    return this.handlePagedResponse(response);
  }

  /**
   * Bulk retry payments
   */
  async bulkRetryPayments(
    paymentIds: string[],
    reason: string
  ): Promise<PaymentRepairResponse[]> {
    if (paymentIds.length === 0) {
      throw new Error('No payment IDs provided');
    }

    // Validate all payment IDs
    paymentIds.forEach(id => {
      if (!this.validateUUID(id)) {
        throw new Error(`Invalid payment ID format: ${id}`);
      }
    });

    const response = await this.httpClient.post<PaymentRepairResponse[]>(
      `${API_ENDPOINTS.PAYMENTS.BASE}/bulk-retry`,
      {
        paymentIds,
        reason: this.sanitizeString(reason),
      }
    );
    
    return this.handleResponse(response);
  }

  /**
   * Bulk cancel payments
   */
  async bulkCancelPayments(
    paymentIds: string[],
    reason: string
  ): Promise<PaymentRepairResponse[]> {
    if (paymentIds.length === 0) {
      throw new Error('No payment IDs provided');
    }

    // Validate all payment IDs
    paymentIds.forEach(id => {
      if (!this.validateUUID(id)) {
        throw new Error(`Invalid payment ID format: ${id}`);
      }
    });

    const response = await this.httpClient.post<PaymentRepairResponse[]>(
      `${API_ENDPOINTS.PAYMENTS.BASE}/bulk-cancel`,
      {
        paymentIds,
        reason: this.sanitizeString(reason),
      }
    );
    
    return this.handleResponse(response);
  }
}
