import apiService from './api';
import { ApiResponse, PagedResponse } from '../types';
import { 
  ClearingSystem, 
  ClearingSystemForm, 
  ClearingSystemSummary,
  ClearingSystemStats,
  ClearingSystemEndpoint,
  TenantClearingSystemMapping,
  TenantMappingForm,
  ClearingSystemTestRequest,
  ClearingSystemTestResponse,
  ClearingSystemRoute
} from '../types/clearingSystem';

class ClearingSystemApiService {
  private readonly BASE_URL = '/api/v1/clearing-systems';
  private readonly MAPPINGS_URL = '/api/v1/clearing-systems/mappings';
  private readonly ENDPOINTS_URL = '/api/v1/clearing-systems/endpoints';
  private readonly ROUTING_URL = '/api/v1/clearing-systems/routing';

  // ============================================================================
  // CLEARING SYSTEM MANAGEMENT
  // ============================================================================

  /**
   * Get all clearing systems
   */
  async getClearingSystems(): Promise<ApiResponse<ClearingSystemSummary[]>> {
    const response = await apiService.request({
      method: 'GET',
      url: this.BASE_URL,
    });
    return response;
  }

  /**
   * Get clearing system by ID
   */
  async getClearingSystemById(id: string): Promise<ApiResponse<ClearingSystem>> {
    const response = await apiService.request({
      method: 'GET',
      url: `${this.BASE_URL}/${id}`,
    });
    return response;
  }

  /**
   * Get clearing system by code
   */
  async getClearingSystemByCode(code: string): Promise<ApiResponse<ClearingSystem>> {
    const response = await apiService.request({
      method: 'GET',
      url: `${this.BASE_URL}/code/${code}`,
    });
    return response;
  }

  /**
   * Create new clearing system
   */
  async createClearingSystem(clearingSystem: ClearingSystemForm): Promise<ApiResponse<ClearingSystem>> {
    const response = await apiService.request({
      method: 'POST',
      url: this.BASE_URL,
      data: clearingSystem,
    });
    return response;
  }

  /**
   * Update clearing system
   */
  async updateClearingSystem(id: string, clearingSystem: ClearingSystemForm): Promise<ApiResponse<ClearingSystem>> {
    const response = await apiService.request({
      method: 'PUT',
      url: `${this.BASE_URL}/${id}`,
      data: clearingSystem,
    });
    return response;
  }

  /**
   * Delete clearing system
   */
  async deleteClearingSystem(id: string): Promise<ApiResponse<void>> {
    const response = await apiService.request({
      method: 'DELETE',
      url: `${this.BASE_URL}/${id}`,
    });
    return response;
  }

  /**
   * Toggle clearing system status
   */
  async toggleClearingSystemStatus(id: string, isActive: boolean): Promise<ApiResponse<ClearingSystem>> {
    const response = await apiService.request({
      method: 'PATCH',
      url: `${this.BASE_URL}/${id}/status`,
      data: { isActive },
    });
    return response;
  }

  /**
   * Get clearing system statistics
   */
  async getClearingSystemStats(): Promise<ApiResponse<ClearingSystemStats>> {
    const response = await apiService.request({
      method: 'GET',
      url: `${this.BASE_URL}/stats`,
    });
    return response;
  }

  // ============================================================================
  // ENDPOINT MANAGEMENT
  // ============================================================================

  /**
   * Get endpoints for a clearing system
   */
  async getClearingSystemEndpoints(clearingSystemId: string): Promise<ApiResponse<ClearingSystemEndpoint[]>> {
    const response = await apiService.request({
      method: 'GET',
      url: `${this.ENDPOINTS_URL}/clearing-system/${clearingSystemId}`,
    });
    return response;
  }

  /**
   * Get endpoint by ID
   */
  async getEndpointById(id: string): Promise<ApiResponse<ClearingSystemEndpoint>> {
    const response = await apiService.request({
      method: 'GET',
      url: `${this.ENDPOINTS_URL}/${id}`,
    });
    return response;
  }

  /**
   * Create new endpoint
   */
  async createEndpoint(endpoint: Omit<ClearingSystemEndpoint, 'id' | 'createdAt' | 'updatedAt'>): Promise<ApiResponse<ClearingSystemEndpoint>> {
    const response = await apiService.request({
      method: 'POST',
      url: this.ENDPOINTS_URL,
      data: endpoint,
    });
    return response;
  }

  /**
   * Update endpoint
   */
  async updateEndpoint(id: string, endpoint: Omit<ClearingSystemEndpoint, 'id' | 'clearingSystemId' | 'createdAt' | 'updatedAt'>): Promise<ApiResponse<ClearingSystemEndpoint>> {
    const response = await apiService.request({
      method: 'PUT',
      url: `${this.ENDPOINTS_URL}/${id}`,
      data: endpoint,
    });
    return response;
  }

  /**
   * Delete endpoint
   */
  async deleteEndpoint(id: string): Promise<ApiResponse<void>> {
    const response = await apiService.request({
      method: 'DELETE',
      url: `${this.ENDPOINTS_URL}/${id}`,
    });
    return response;
  }

  /**
   * Toggle endpoint status
   */
  async toggleEndpointStatus(id: string, isActive: boolean): Promise<ApiResponse<ClearingSystemEndpoint>> {
    const response = await apiService.request({
      method: 'PATCH',
      url: `${this.ENDPOINTS_URL}/${id}/status`,
      data: { isActive },
    });
    return response;
  }

  // ============================================================================
  // TENANT MAPPING MANAGEMENT
  // ============================================================================

  /**
   * Get tenant clearing system mappings
   */
  async getTenantMappings(tenantId?: string): Promise<ApiResponse<TenantClearingSystemMapping[]>> {
    const params = tenantId ? { tenantId } : {};
    const response = await apiService.request({
      method: 'GET',
      url: this.MAPPINGS_URL,
      params,
    });
    return response;
  }

  /**
   * Get tenant mapping by ID
   */
  async getTenantMappingById(id: string): Promise<ApiResponse<TenantClearingSystemMapping>> {
    const response = await apiService.request({
      method: 'GET',
      url: `${this.MAPPINGS_URL}/${id}`,
    });
    return response;
  }

  /**
   * Create new tenant mapping
   */
  async createTenantMapping(mapping: TenantMappingForm): Promise<ApiResponse<TenantClearingSystemMapping>> {
    const response = await apiService.request({
      method: 'POST',
      url: this.MAPPINGS_URL,
      data: mapping,
    });
    return response;
  }

  /**
   * Update tenant mapping
   */
  async updateTenantMapping(id: string, mapping: TenantMappingForm): Promise<ApiResponse<TenantClearingSystemMapping>> {
    const response = await apiService.request({
      method: 'PUT',
      url: `${this.MAPPINGS_URL}/${id}`,
      data: mapping,
    });
    return response;
  }

  /**
   * Delete tenant mapping
   */
  async deleteTenantMapping(id: string): Promise<ApiResponse<void>> {
    const response = await apiService.request({
      method: 'DELETE',
      url: `${this.MAPPINGS_URL}/${id}`,
    });
    return response;
  }

  /**
   * Toggle tenant mapping status
   */
  async toggleTenantMappingStatus(id: string, isActive: boolean): Promise<ApiResponse<TenantClearingSystemMapping>> {
    const response = await apiService.request({
      method: 'PATCH',
      url: `${this.MAPPINGS_URL}/${id}/status`,
      data: { isActive },
    });
    return response;
  }

  // ============================================================================
  // ROUTING AND TESTING
  // ============================================================================

  /**
   * Get clearing system route for tenant and payment type
   */
  async getClearingSystemRoute(
    tenantId: string, 
    paymentType: string, 
    localInstrumentCode?: string
  ): Promise<ApiResponse<ClearingSystemRoute>> {
    const params: any = { tenantId, paymentType };
    if (localInstrumentCode) {
      params.localInstrumentCode = localInstrumentCode;
    }
    
    const response = await apiService.request({
      method: 'GET',
      url: `${this.ROUTING_URL}/route`,
      params,
    });
    return response;
  }

  /**
   * Get available clearing systems for tenant
   */
  async getAvailableClearingSystems(tenantId: string): Promise<ApiResponse<ClearingSystemSummary[]>> {
    const response = await apiService.request({
      method: 'GET',
      url: `${this.ROUTING_URL}/available`,
      params: { tenantId },
    });
    return response;
  }

  /**
   * Test clearing system endpoint
   */
  async testClearingSystemEndpoint(testRequest: ClearingSystemTestRequest): Promise<ApiResponse<ClearingSystemTestResponse>> {
    const response = await apiService.request({
      method: 'POST',
      url: `${this.BASE_URL}/test`,
      data: testRequest,
    });
    return response;
  }

  /**
   * Test clearing system routing
   */
  async testClearingSystemRouting(
    tenantId: string,
    paymentType: string,
    localInstrumentCode?: string
  ): Promise<ApiResponse<ClearingSystemRoute>> {
    const params: any = { tenantId, paymentType };
    if (localInstrumentCode) {
      params.localInstrumentCode = localInstrumentCode;
    }
    
    const response = await apiService.request({
      method: 'POST',
      url: `${this.ROUTING_URL}/test`,
      params,
    });
    return response;
  }

  // ============================================================================
  // BULK OPERATIONS
  // ============================================================================

  /**
   * Bulk create tenant mappings
   */
  async bulkCreateTenantMappings(mappings: TenantMappingForm[]): Promise<ApiResponse<TenantClearingSystemMapping[]>> {
    const response = await apiService.request({
      method: 'POST',
      url: `${this.MAPPINGS_URL}/bulk`,
      data: { mappings },
    });
    return response;
  }

  /**
   * Bulk update tenant mappings
   */
  async bulkUpdateTenantMappings(mappings: { id: string; mapping: TenantMappingForm }[]): Promise<ApiResponse<TenantClearingSystemMapping[]>> {
    const response = await apiService.request({
      method: 'PUT',
      url: `${this.MAPPINGS_URL}/bulk`,
      data: { mappings },
    });
    return response;
  }

  /**
   * Bulk delete tenant mappings
   */
  async bulkDeleteTenantMappings(ids: string[]): Promise<ApiResponse<void>> {
    const response = await apiService.request({
      method: 'DELETE',
      url: `${this.MAPPINGS_URL}/bulk`,
      data: { ids },
    });
    return response;
  }

  // ============================================================================
  // HEALTH AND STATUS
  // ============================================================================

  /**
   * Health check for clearing system service
   */
  async healthCheck(): Promise<ApiResponse<{ status: string; timestamp: string }>> {
    const response = await apiService.request({
      method: 'GET',
      url: `${this.BASE_URL}/health`,
    });
    return response;
  }

  /**
   * Get clearing system service status
   */
  async getServiceStatus(): Promise<ApiResponse<{ 
    status: string; 
    version: string; 
    features: Record<string, boolean>;
    timestamp: string;
  }>> {
    const response = await apiService.request({
      method: 'GET',
      url: `${this.BASE_URL}/status`,
    });
    return response;
  }
}

const clearingSystemApiService = new ClearingSystemApiService();
export default clearingSystemApiService;