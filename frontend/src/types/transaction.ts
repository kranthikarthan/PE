// Transaction Types for Payment Engine Integration

export interface CreateTransactionRequest {
  fromAccountId?: string;
  toAccountId?: string;
  paymentTypeId: string;
  amount: number;
  currencyCode: string;
  description?: string;
  externalReference?: string;
  metadata?: Record<string, any>;
  channel?: string;
  ipAddress?: string;
  deviceId?: string;
}

export interface TransactionResponse {
  id: string;
  transactionReference: string;
  externalReference?: string;
  fromAccountId?: string;
  toAccountId?: string;
  paymentTypeId: string;
  amount: number;
  currencyCode: string;
  feeAmount: number;
  status: TransactionStatus;
  transactionType: TransactionType;
  description?: string;
  metadata?: Record<string, any>;
  initiatedAt: string;
  processedAt?: string;
  completedAt?: string;
  createdAt: string;
  updatedAt: string;
}

export enum TransactionStatus {
  PENDING = 'PENDING',
  PROCESSING = 'PROCESSING',
  COMPLETED = 'COMPLETED',
  FAILED = 'FAILED',
  CANCELLED = 'CANCELLED',
  REVERSED = 'REVERSED'
}

export enum TransactionType {
  DEBIT = 'DEBIT',
  CREDIT = 'CREDIT',
  TRANSFER = 'TRANSFER',
  PAYMENT = 'PAYMENT',
  REFUND = 'REFUND'
}

// Error Response Types
export interface ValidationErrorResponse {
  error: 'VALIDATION_ERROR';
  message: string;
  validationErrors: string[];
}

export interface AccountErrorResponse {
  error: string;
  message: string;
  accountId: string;
}

export interface TransactionErrorResponse {
  error: string;
  message: string;
  transactionReference?: string;
}

export interface InternalErrorResponse {
  error: 'INTERNAL_ERROR';
  message: string;
}

export type TransactionErrorResponseType = 
  | ValidationErrorResponse 
  | AccountErrorResponse 
  | TransactionErrorResponse 
  | InternalErrorResponse;

// Search and Filter Types
export interface TransactionSearchParams {
  transactionReference?: string;
  accountId?: string;
  status?: TransactionStatus;
  paymentTypeId?: string;
  minAmount?: number;
  maxAmount?: number;
  startDate?: string;
  endDate?: string;
  page?: number;
  size?: number;
  sortBy?: string;
  sortDirection?: 'asc' | 'desc';
}

export interface TransactionSearchResponse {
  content: TransactionResponse[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
}

// Statistics Types
export interface TransactionStatistics {
  totalCount: number;
  totalAmount: number;
  averageAmount: number;
  maxAmount: number;
  minAmount: number;
}

// Account Types
export interface Account {
  id: string;
  accountNumber: string;
  customerId: string;
  accountTypeId: string;
  currencyCode: string;
  balance: number;
  availableBalance: number;
  status: AccountStatus;
  openedDate: string;
  closedDate?: string;
  createdAt: string;
  updatedAt: string;
}

export enum AccountStatus {
  ACTIVE = 'ACTIVE',
  INACTIVE = 'INACTIVE',
  SUSPENDED = 'SUSPENDED',
  CLOSED = 'CLOSED'
}

// Payment Type Types
export interface PaymentType {
  id: string;
  name: string;
  code: string;
  description: string;
  isActive: boolean;
  isSynchronous: boolean;
  minAmount: number;
  maxAmount: number;
  feeType: string;
  feeAmount: number;
  createdAt: string;
  updatedAt: string;
}