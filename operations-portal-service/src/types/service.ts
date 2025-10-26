/**
 * Service Health and Management Types
 * 
 * TypeScript interfaces for service monitoring, health checks, and management.
 */

export enum ServiceStatus {
  UP = 'UP',
  DOWN = 'DOWN',
  DEGRADED = 'DEGRADED',
  UNKNOWN = 'UNKNOWN',
}

export enum CircuitBreakerState {
  CLOSED = 'CLOSED',
  OPEN = 'OPEN',
  HALF_OPEN = 'HALF_OPEN',
}

export interface ServiceHealth {
  id: string;
  name: string;
  status: ServiceStatus;
  uptime: number;
  responseTime: number;
  lastCheck: string;
  version: string;
  instances: number;
  components: Record<string, ServiceStatus>;
  metrics: ServiceMetrics;
}

export interface ServiceHealthSummary {
  totalServices: number;
  healthyServices: number;
  unhealthyServices: number;
  overallHealth: 'HEALTHY' | 'DEGRADED' | 'UNHEALTHY';
}

export interface ServiceMetrics {
  requestsPerSecond: number;
  errorRate: number;
  averageResponseTime: number;
  p95ResponseTime: number;
  p99ResponseTime: number;
  memoryUsage: number;
  cpuUsage: number;
  activeConnections: number;
}

export interface CircuitBreakerInfo {
  name: string;
  state: CircuitBreakerState;
  failureRate: number;
  requestCount: number;
  failureCount: number;
  lastFailureTime?: string;
  timeout: number;
  retryAfter?: string;
}

export interface ServiceControlRequest {
  action: 'START' | 'STOP' | 'RESTART' | 'PAUSE' | 'RESUME';
  reason?: string;
  force?: boolean;
}

export interface ServiceControlResponse {
  serviceId: string;
  action: string;
  status: 'SUCCESS' | 'FAILED';
  message: string;
  timestamp: string;
}

export interface PodInfo {
  name: string;
  namespace: string;
  status: 'Running' | 'Pending' | 'Failed' | 'Succeeded' | 'Unknown';
  restarts: number;
  age: string;
  cpu: string;
  memory: string;
  node: string;
  labels: Record<string, string>;
}

export interface PodControlRequest {
  action: 'RESTART' | 'DELETE' | 'SCALE';
  replicas?: number;
  reason?: string;
}

export interface PodControlResponse {
  podName: string;
  action: string;
  status: 'SUCCESS' | 'FAILED';
  message: string;
  timestamp: string;
}

export interface ServiceLogs {
  podName: string;
  logs: string[];
  lastLines: number;
  timestamp: string;
}

export interface ServiceAlerts {
  id: string;
  serviceId: string;
  severity: 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';
  message: string;
  timestamp: string;
  acknowledged: boolean;
  acknowledgedBy?: string;
  acknowledgedAt?: string;
}
