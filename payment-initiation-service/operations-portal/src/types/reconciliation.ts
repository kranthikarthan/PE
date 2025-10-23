/**
 * Reconciliation Types
 * 
 * TypeScript interfaces for reconciliation monitoring and batch processing.
 */

export enum ReconciliationBatchStatus {
  PENDING = 'PENDING',
  PROCESSING = 'PROCESSING',
  COMPLETED = 'COMPLETED',
  FAILED = 'FAILED',
  CANCELLED = 'CANCELLED',
}

export enum ReconciliationType {
  DAILY = 'DAILY',
  HOURLY = 'HOURLY',
  REAL_TIME = 'REAL_TIME',
  MANUAL = 'MANUAL',
}

export interface ReconciliationBatch {
  id: string;
  batchName: string;
  type: ReconciliationType;
  status: ReconciliationBatchStatus;
  startTime: string;
  endTime?: string;
  totalRecords: number;
  processedRecords: number;
  failedRecords: number;
  successRate: number;
  processingTime?: string;
  errorDetails?: string;
  tenantId: string;
  businessUnitId: string;
  correlationId: string;
}

export interface ReconciliationMetrics {
  averageProcessingTime: string;
  successRate: number;
  recordsPerHour: number;
  errorRate: number;
  trend: 'UP' | 'DOWN' | 'STABLE';
  change: string;
}

export interface ReconciliationException {
  id: string;
  batchId: string;
  recordId: string;
  exceptionType: string;
  description: string;
  severity: 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';
  status: 'OPEN' | 'IN_PROGRESS' | 'RESOLVED' | 'IGNORED';
  createdAt: string;
  resolvedAt?: string;
  resolvedBy?: string;
  resolution?: string;
  metadata?: Record<string, any>;
}

export interface ReconciliationReport {
  batchId: string;
  reportType: 'SUMMARY' | 'DETAILED' | 'EXCEPTIONS';
  format: 'CSV' | 'EXCEL' | 'PDF';
  downloadUrl: string;
  expiresAt: string;
  recordCount: number;
  generatedAt: string;
  generatedBy: string;
}

export interface ReconciliationSearchCriteria {
  batchId?: string;
  status?: ReconciliationBatchStatus;
  type?: ReconciliationType;
  dateFrom?: string;
  dateTo?: string;
  tenantId?: string;
  businessUnitId?: string;
}

export interface ReconciliationPerformanceMetric {
  name: string;
  value: string;
  trend: 'UP' | 'DOWN' | 'STABLE';
  change: string;
  unit?: string;
}
