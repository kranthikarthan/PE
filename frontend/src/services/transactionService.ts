import axios from 'axios';
import {
  CreateTransactionRequest,
  TransactionResponse,
  TransactionSearchParams,
  TransactionSearchResponse,
  TransactionStatistics,
  Account,
  PaymentType,
  TransactionErrorResponseType
} from '../types/transaction';

const API_BASE_URL = process.env.REACT_APP_API_BASE_URL || 'http://localhost:8080';

class TransactionService {
  private baseURL: string;

  constructor() {
    this.baseURL = `${API_BASE_URL}/api/v1/transactions`;
  }

  /**
   * Create a new transaction
   */
  async createTransaction(request: CreateTransactionRequest): Promise<TransactionResponse> {
    try {
      const response = await axios.post<TransactionResponse>(this.baseURL, request);
      return response.data;
    } catch (error: any) {
      this.handleTransactionError(error);
    }
  }

  /**
   * Get transaction by ID
   */
  async getTransactionById(transactionId: string): Promise<TransactionResponse> {
    try {
      const response = await axios.get<TransactionResponse>(`${this.baseURL}/${transactionId}`);
      return response.data;
    } catch (error: any) {
      this.handleTransactionError(error);
    }
  }

  /**
   * Get transaction by reference
   */
  async getTransactionByReference(reference: string): Promise<TransactionResponse> {
    try {
      const response = await axios.get<TransactionResponse>(`${this.baseURL}/reference/${reference}`);
      return response.data;
    } catch (error: any) {
      this.handleTransactionError(error);
    }
  }

  /**
   * Get transaction status
   */
  async getTransactionStatus(transactionId: string): Promise<any> {
    try {
      const response = await axios.get(`${this.baseURL}/${transactionId}/status`);
      return response.data;
    } catch (error: any) {
      this.handleTransactionError(error);
    }
  }

  /**
   * Search transactions
   */
  async searchTransactions(params: TransactionSearchParams): Promise<TransactionSearchResponse> {
    try {
      const response = await axios.get<TransactionSearchResponse>(`${this.baseURL}/search`, {
        params
      });
      return response.data;
    } catch (error: any) {
      this.handleTransactionError(error);
    }
  }

  /**
   * Get transactions for an account
   */
  async getTransactionsByAccount(accountId: string, page = 0, size = 20): Promise<TransactionSearchResponse> {
    try {
      const response = await axios.get<TransactionSearchResponse>(`${this.baseURL}/account/${accountId}`, {
        params: { page, size }
      });
      return response.data;
    } catch (error: any) {
      this.handleTransactionError(error);
    }
  }

  /**
   * Cancel a transaction
   */
  async cancelTransaction(transactionId: string, reason: string): Promise<TransactionResponse> {
    try {
      const response = await axios.post<TransactionResponse>(`${this.baseURL}/${transactionId}/cancel`, {
        reason
      });
      return response.data;
    } catch (error: any) {
      this.handleTransactionError(error);
    }
  }

  /**
   * Get transaction statistics
   */
  async getTransactionStatistics(startDate: string, endDate: string): Promise<TransactionStatistics> {
    try {
      const response = await axios.get<TransactionStatistics>(`${this.baseURL}/statistics`, {
        params: { startDate, endDate }
      });
      return response.data;
    } catch (error: any) {
      this.handleTransactionError(error);
    }
  }

  /**
   * Get accounts
   */
  async getAccounts(): Promise<Account[]> {
    try {
      const response = await axios.get<Account[]>(`${API_BASE_URL}/api/v1/accounts`);
      return response.data;
    } catch (error: any) {
      this.handleTransactionError(error);
    }
  }

  /**
   * Get payment types
   */
  async getPaymentTypes(): Promise<PaymentType[]> {
    try {
      const response = await axios.get<PaymentType[]>(`${API_BASE_URL}/api/v1/payment-types`);
      return response.data;
    } catch (error: any) {
      this.handleTransactionError(error);
    }
  }

  /**
   * Handle transaction-specific errors
   */
  private handleTransactionError(error: any): never {
    if (error.response?.data) {
      const errorData = error.response.data as TransactionErrorResponseType;
      
      switch (errorData.error) {
        case 'VALIDATION_ERROR':
          throw new Error(`Validation Error: ${errorData.message}. Errors: ${(errorData as any).validationErrors?.join(', ')}`);
        
        case 'ACCOUNT_ERROR':
        case 'INSUFFICIENT_FUNDS':
        case 'ACCOUNT_INACTIVE':
          throw new Error(`Account Error: ${errorData.message}`);
        
        case 'TRANSACTION_CREATION_ERROR':
        case 'TRANSACTION_ERROR':
          throw new Error(`Transaction Error: ${errorData.message}`);
        
        case 'INTERNAL_ERROR':
          throw new Error(`Internal Error: ${errorData.message}`);
        
        default:
          throw new Error(`Unknown Error: ${errorData.message}`);
      }
    }
    
    if (error.response?.status === 404) {
      throw new Error('Transaction not found');
    }
    
    if (error.response?.status === 401) {
      throw new Error('Unauthorized access');
    }
    
    if (error.response?.status === 403) {
      throw new Error('Access forbidden');
    }
    
    throw new Error(error.message || 'An unexpected error occurred');
  }
}

export default new TransactionService();