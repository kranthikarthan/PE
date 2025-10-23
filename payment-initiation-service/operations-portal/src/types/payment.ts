/**
 * Payment Domain Types
 * 
 * TypeScript interfaces matching backend payment domain models.
 */

export enum PaymentStatus {
  PENDING = 'PENDING',
  PROCESSING = 'PROCESSING',
  COMPLETED = 'COMPLETED',
  FAILED = 'FAILED',
  CANCELLED = 'CANCELLED',
  TIMEOUT = 'TIMEOUT',
  RETRY_INITIATED = 'RETRY_INITIATED',
}

export enum PaymentMethod {
  CARD = 'CARD',
  BANK_TRANSFER = 'BANK_TRANSFER',
  WALLET = 'WALLET',
  CASH = 'CASH',
  CRYPTOCURRENCY = 'CRYPTOCURRENCY',
}

export enum Currency {
  USD = 'USD',
  EUR = 'EUR',
  GBP = 'GBP',
  ZAR = 'ZAR',
  BTC = 'BTC',
  ETH = 'ETH',
}

export interface Payment {
  id: string;
  amount: number;
  currency: Currency;
  status: PaymentStatus;
  method: PaymentMethod;
  sourceAccount: string;
  destinationAccount: string;
  reference: string;
  description?: string;
  metadata?: Record<string, any>;
  createdAt: string;
  updatedAt: string;
  completedAt?: string;
  failureReason?: string;
  processingTime?: number;
  tenantId: string;
  businessUnitId: string;
  correlationId: string;
}

export interface PaymentInitiationRequest {
  amount: number;
  currency: Currency;
  method: PaymentMethod;
  sourceAccount: string;
  destinationAccount: string;
  reference: string;
  description?: string;
  metadata?: Record<string, any>;
  tenantId: string;
  businessUnitId: string;
}

export interface PaymentInitiationResponse {
  paymentId: string;
  status: PaymentStatus;
  message: string;
  correlationId: string;
  estimatedCompletionTime?: string;
}

export interface PaymentRepairRequest {
  reason: string;
  forceRetry?: boolean;
  metadata?: Record<string, any>;
}

export interface PaymentRepairResponse {
  paymentId: string;
  action: 'RETRY' | 'CANCEL' | 'REFUND';
  status: PaymentStatus;
  message: string;
  correlationId: string;
}

export interface PaymentRepairLog {
  id: string;
  paymentId: string;
  action: string;
  performedBy: string;
  reason: string;
  result: 'SUCCESS' | 'FAILED';
  timestamp: string;
  metadata?: Record<string, any>;
}

export interface PaymentSearchCriteria {
  paymentId?: string;
  reference?: string;
  status?: PaymentStatus;
  method?: PaymentMethod;
  currency?: Currency;
  amountFrom?: number;
  amountTo?: number;
  dateFrom?: string;
  dateTo?: string;
  tenantId?: string;
  businessUnitId?: string;
}

export interface PaymentMetrics {
  totalPayments: number;
  successfulPayments: number;
  failedPayments: number;
  pendingPayments: number;
  successRate: number;
  averageProcessingTime: number;
  totalVolume: number;
  currencyBreakdown: Record<Currency, number>;
  methodBreakdown: Record<PaymentMethod, number>;
}
