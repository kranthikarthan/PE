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
  totalBatches: number;
  completedBatches: number;
  processingBatches: number;
  failedBatches: number;
  averageProcessingTime: string;
  successRate: number;
  recordsPerHour: number;
  errorRate: number;
  trend: 'up' | 'down' | 'stable';
  change: string;
  detailedMetrics?: PerformanceMetric[];
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
  trend: 'up' | 'down' | 'stable';
  change: string;
  unit?: string;
  description: string;
}

export interface PerformanceMetric {
  name: string;
  value: string;
  unit?: string;
  trend: 'up' | 'down' | 'stable';
  change: string;
  description: string;
}
