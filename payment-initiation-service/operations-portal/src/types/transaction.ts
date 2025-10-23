/**
 * Transaction Types
 * 
 * TypeScript interfaces for transaction processing and enquiries.
 */

export enum TransactionStatus {
  PENDING = 'PENDING',
  PROCESSING = 'PROCESSING',
  COMPLETED = 'COMPLETED',
  FAILED = 'FAILED',
  CANCELLED = 'CANCELLED',
  TIMEOUT = 'TIMEOUT',
}

export enum TransactionType {
  PAYMENT = 'PAYMENT',
  REFUND = 'REFUND',
  REVERSAL = 'REVERSAL',
  ADJUSTMENT = 'ADJUSTMENT',
  FEE = 'FEE',
}

export interface Transaction {
  id: string;
  type: TransactionType;
  status: TransactionStatus;
  amount: number;
  currency: string;
  sourceAccount: string;
  destinationAccount: string;
  reference: string;
  description?: string;
  metadata?: Record<string, any>;
  createdAt: string;
  updatedAt: string;
  completedAt?: string;
  processingTime?: number;
  failureReason?: string;
  tenantId: string;
  businessUnitId: string;
  correlationId: string;
  parentTransactionId?: string;
  relatedTransactionIds?: string[];
}

export interface TransactionSearchCriteria {
  transactionId?: string;
  reference?: string;
  status?: TransactionStatus;
  type?: TransactionType;
  amountFrom?: number;
  amountTo?: number;
  currency?: string;
  dateFrom?: string;
  dateTo?: string;
  tenantId?: string;
  businessUnitId?: string;
}

export interface TransactionMetrics {
  totalTransactions: number;
  completedTransactions: number;
  failedTransactions: number;
  pendingTransactions: number;
  successRate: number;
  averageProcessingTime: number;
  totalVolume: number;
  currencyBreakdown: Record<string, number>;
  typeBreakdown: Record<TransactionType, number>;
}

export interface TransactionExportRequest {
  format: 'CSV' | 'EXCEL' | 'PDF';
  criteria: TransactionSearchCriteria;
  includeMetadata?: boolean;
  includeRelatedTransactions?: boolean;
}

export interface TransactionExportResponse {
  exportId: string;
  status: 'PENDING' | 'PROCESSING' | 'COMPLETED' | 'FAILED';
  downloadUrl?: string;
  expiresAt: string;
  recordCount: number;
}
