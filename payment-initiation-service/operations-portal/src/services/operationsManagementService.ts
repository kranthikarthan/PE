/**
 * Operations Management Service Client
 * 
 * API client for service health monitoring, circuit breaker management,
 * feature flags, and Kubernetes pod management.
 */

import { BaseApiClient } from './baseApiClient';
import { HttpClient } from './httpClient';
import { 
  ServiceHealth, 
  ServiceMetrics, 
  CircuitBreakerInfo, 
  ServiceControlRequest, 
  ServiceControlResponse,
  PodInfo,
  PodControlRequest,
  PodControlResponse,
  ServiceLogs,
  ServiceAlerts
} from '../types/service';
import { ApiResponse, PaginationParams } from '../types/api';
import { API_ENDPOINTS } from '../constants';

export class OperationsManagementService extends BaseApiClient {
  constructor(httpClient: HttpClient) {
    super(httpClient);
  }

  /**
   * Get health status for all services
   */
  async getAllServicesHealth(): Promise<ServiceHealth[]> {
    const response = await this.httpClient.get<ServiceHealth[]>(
      API_ENDPOINTS.OPERATIONS.SERVICES
    );
    
    return this.handleResponse(response);
  }

  /**
   * Get health status for a specific service
   */
  async getServiceHealth(serviceId: string): Promise<ServiceHealth> {
    this.validateRequired({ serviceId }, ['serviceId']);

    const response = await this.httpClient.get<ServiceHealth>(
      API_ENDPOINTS.OPERATIONS.SERVICE_HEALTH(serviceId)
    );
    
    return this.handleResponse(response);
  }

  /**
   * Get metrics for a specific service
   */
  async getServiceMetrics(serviceId: string): Promise<ServiceMetrics> {
    this.validateRequired({ serviceId }, ['serviceId']);

    const response = await this.httpClient.get<ServiceMetrics>(
      API_ENDPOINTS.OPERATIONS.SERVICE_METRICS(serviceId)
    );
    
    return this.handleResponse(response);
  }

  /**
   * Control a service (start, stop, restart, pause, resume)
   */
  async controlService(serviceId: string, controlRequest: ServiceControlRequest): Promise<ServiceControlResponse> {
    this.validateRequired({ serviceId }, ['serviceId']);
    this.validateRequired(controlRequest, ['action']);

    const response = await this.httpClient.post<ServiceControlResponse>(
      API_ENDPOINTS.OPERATIONS.SERVICE_CONTROL(serviceId),
      controlRequest
    );
    
    return this.handleResponse(response);
  }

  /**
   * Restart a service
   */
  async restartService(serviceId: string, reason?: string): Promise<ServiceControlResponse> {
    return this.controlService(serviceId, {
      action: 'RESTART',
      reason: reason || 'Restarted by operations team',
      force: false,
    });
  }

  /**
   * Stop a service
   */
  async stopService(serviceId: string, reason?: string): Promise<ServiceControlResponse> {
    return this.controlService(serviceId, {
      action: 'STOP',
      reason: reason || 'Stopped by operations team',
      force: false,
    });
  }

  /**
   * Start a service
   */
  async startService(serviceId: string, reason?: string): Promise<ServiceControlResponse> {
    return this.controlService(serviceId, {
      action: 'START',
      reason: reason || 'Started by operations team',
      force: false,
    });
  }

  /**
   * Get all circuit breakers
   */
  async getAllCircuitBreakers(): Promise<CircuitBreakerInfo[]> {
    const response = await this.httpClient.get<CircuitBreakerInfo[]>(
      API_ENDPOINTS.OPERATIONS.CIRCUIT_BREAKERS
    );
    
    return this.handleResponse(response);
  }

  /**
   * Get circuit breaker for a specific service
   */
  async getCircuitBreaker(serviceId: string): Promise<CircuitBreakerInfo> {
    this.validateRequired({ serviceId }, ['serviceId']);

    const response = await this.httpClient.get<CircuitBreakerInfo>(
      API_ENDPOINTS.OPERATIONS.CIRCUIT_BREAKER_CONTROL(serviceId)
    );
    
    return this.handleResponse(response);
  }

  /**
   * Open circuit breaker for a service
   */
  async openCircuitBreaker(serviceId: string, reason?: string): Promise<ServiceControlResponse> {
    this.validateRequired({ serviceId }, ['serviceId']);

    const response = await this.httpClient.post<ServiceControlResponse>(
      `${API_ENDPOINTS.OPERATIONS.CIRCUIT_BREAKER_CONTROL(serviceId)}/open`,
      { reason: reason || 'Circuit breaker opened by operations team' }
    );
    
    return this.handleResponse(response);
  }

  /**
   * Close circuit breaker for a service
   */
  async closeCircuitBreaker(serviceId: string, reason?: string): Promise<ServiceControlResponse> {
    this.validateRequired({ serviceId }, ['serviceId']);

    const response = await this.httpClient.post<ServiceControlResponse>(
      `${API_ENDPOINTS.OPERATIONS.CIRCUIT_BREAKER_CONTROL(serviceId)}/close`,
      { reason: reason || 'Circuit breaker closed by operations team' }
    );
    
    return this.handleResponse(response);
  }

  /**
   * Get all feature flags
   */
  async getAllFeatureFlags(): Promise<any[]> {
    const response = await this.httpClient.get<any[]>(
      API_ENDPOINTS.OPERATIONS.FEATURE_FLAGS
    );
    
    return this.handleResponse(response);
  }

  /**
   * Toggle a feature flag
   */
  async toggleFeatureFlag(flagName: string, enabled: boolean, tenantId?: string): Promise<any> {
    this.validateRequired({ flagName }, ['flagName']);

    const response = await this.httpClient.post<any>(
      API_ENDPOINTS.OPERATIONS.FEATURE_FLAG_TOGGLE(flagName),
      {
        enabled,
        tenantId: tenantId || undefined,
      }
    );
    
    return this.handleResponse(response);
  }

  /**
   * Get all pods
   */
  async getAllPods(): Promise<PodInfo[]> {
    const response = await this.httpClient.get<PodInfo[]>(
      API_ENDPOINTS.OPERATIONS.PODS
    );
    
    return this.handleResponse(response);
  }

  /**
   * Control a pod (restart, delete, scale)
   */
  async controlPod(podName: string, controlRequest: PodControlRequest): Promise<PodControlResponse> {
    this.validateRequired({ podName }, ['podName']);
    this.validateRequired(controlRequest, ['action']);

    const response = await this.httpClient.post<PodControlResponse>(
      API_ENDPOINTS.OPERATIONS.POD_CONTROL(podName),
      controlRequest
    );
    
    return this.handleResponse(response);
  }

  /**
   * Restart a pod
   */
  async restartPod(podName: string, reason?: string): Promise<PodControlResponse> {
    return this.controlPod(podName, {
      action: 'RESTART',
      reason: reason || 'Restarted by operations team',
    });
  }

  /**
   * Delete a pod
   */
  async deletePod(podName: string, reason?: string): Promise<PodControlResponse> {
    return this.controlPod(podName, {
      action: 'DELETE',
      reason: reason || 'Deleted by operations team',
    });
  }

  /**
   * Scale a pod
   */
  async scalePod(podName: string, replicas: number, reason?: string): Promise<PodControlResponse> {
    return this.controlPod(podName, {
      action: 'SCALE',
      replicas,
      reason: reason || 'Scaled by operations team',
    });
  }

  /**
   * Get pod logs
   */
  async getPodLogs(podName: string, lines?: number): Promise<ServiceLogs> {
    this.validateRequired({ podName }, ['podName']);

    const queryParams = lines ? { lines } : {};
    const url = this.buildUrl(API_ENDPOINTS.OPERATIONS.POD_LOGS(podName), queryParams);
    const response = await this.httpClient.get<ServiceLogs>(url);
    
    return this.handleResponse(response);
  }

  /**
   * Get service alerts
   */
  async getServiceAlerts(serviceId?: string): Promise<ServiceAlerts[]> {
    const queryParams = serviceId ? { serviceId } : {};
    const url = this.buildUrl(`${API_ENDPOINTS.OPERATIONS.BASE}/alerts`, queryParams);
    const response = await this.httpClient.get<ServiceAlerts[]>(url);
    
    return this.handleResponse(response);
  }

  /**
   * Acknowledge an alert
   */
  async acknowledgeAlert(alertId: string, userId: string): Promise<ServiceAlerts> {
    this.validateRequired({ alertId, userId }, ['alertId', 'userId']);

    const response = await this.httpClient.post<ServiceAlerts>(
      `${API_ENDPOINTS.OPERATIONS.BASE}/alerts/${alertId}/acknowledge`,
      { userId }
    );
    
    return this.handleResponse(response);
  }

  /**
   * Get service health summary
   */
  async getServiceHealthSummary(): Promise<{
    totalServices: number;
    healthyServices: number;
    unhealthyServices: number;
    degradedServices: number;
    overallHealth: 'HEALTHY' | 'DEGRADED' | 'UNHEALTHY';
  }> {
    const services = await this.getAllServicesHealth();
    
    const totalServices = services.length;
    const healthyServices = services.filter(s => s.status === 'UP').length;
    const unhealthyServices = services.filter(s => s.status === 'DOWN').length;
    const degradedServices = services.filter(s => s.status === 'DEGRADED').length;
    
    let overallHealth: 'HEALTHY' | 'DEGRADED' | 'UNHEALTHY';
    if (unhealthyServices > 0) {
      overallHealth = 'UNHEALTHY';
    } else if (degradedServices > 0) {
      overallHealth = 'DEGRADED';
    } else {
      overallHealth = 'HEALTHY';
    }

    return {
      totalServices,
      healthyServices,
      unhealthyServices,
      degradedServices,
      overallHealth,
    };
  }
}
