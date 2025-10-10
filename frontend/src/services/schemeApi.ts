import apiService from './api';
import {
  SchemeInteractionConfig,
  SchemeConfigForm,
  SchemeConfigSearchCriteria,
  PagedSchemeConfigResponse,
  SchemeConfigTestRequest,
  SchemeConfigTestResponse,
  SchemeInteractionStats,
  SchemeMessageRequest,
  SchemeMessageResponse,
  InteractionMode,
  MessageFormat,
  ResponseMode
} from '../types/scheme';

/**
 * API service for scheme interaction configuration management
 */
class SchemeApiService {

  // ============================================================================
  // CONFIGURATION MANAGEMENT
  // ============================================================================

  /**
   * Get all scheme configurations with pagination and filtering
   */
  async getConfigurations(criteria?: SchemeConfigSearchCriteria): Promise<PagedSchemeConfigResponse> {
    const params = new URLSearchParams();
    
    if (criteria) {
      Object.entries(criteria).forEach(([key, value]) => {
        if (value !== undefined && value !== null && value !== '') {
          params.append(key, value.toString());
        }
      });
    }

    const response = await apiService.request({
      method: 'GET',
      url: `/api/v1/scheme/configurations?${params.toString()}`,
    });
    return response;
  }

  /**
   * Get a specific scheme configuration by ID
   */
  async getConfiguration(configId: string): Promise<SchemeInteractionConfig> {
    const response = await apiService.request({
      method: 'GET',
      url: `/api/v1/scheme/configurations/${configId}`,
    });
    return response;
  }

  /**
   * Create a new scheme configuration
   */
  async createConfiguration(config: SchemeConfigForm): Promise<SchemeInteractionConfig> {
    const response = await apiService.request({
      method: 'POST',
      url: '/api/v1/scheme/configurations',
      data: config,
    });
    return response;
  }

  /**
   * Update an existing scheme configuration
   */
  async updateConfiguration(configId: string, config: SchemeConfigForm): Promise<SchemeInteractionConfig> {
    const response = await apiService.request({
      method: 'PUT',
      url: `/api/v1/scheme/configurations/${configId}`,
      data: config,
    });
    return response;
  }

  /**
   * Delete a scheme configuration
   */
  async deleteConfiguration(configId: string): Promise<void> {
    await apiService.request({
      method: 'DELETE',
      url: `/api/v1/scheme/configurations/${configId}`,
    });
  }

  /**
   * Clone an existing configuration
   */
  async cloneConfiguration(configId: string, newName: string): Promise<SchemeInteractionConfig> {
    const response = await apiService.request({
      method: 'POST',
      url: `/api/v1/scheme/configurations/${configId}/clone`,
      data: { name: newName },
    });
    return response;
  }

  /**
   * Activate/deactivate a configuration
   */
  async toggleConfigurationStatus(configId: string, isActive: boolean): Promise<SchemeInteractionConfig> {
    const response = await apiService.request({
      method: 'PATCH',
      url: `/api/v1/scheme/configurations/${configId}/status`,
      data: { isActive },
    });
    return response;
  }

  // ============================================================================
  // CONFIGURATION TESTING
  // ============================================================================

  /**
   * Test a scheme configuration
   */
  async testConfiguration(testRequest: SchemeConfigTestRequest): Promise<SchemeConfigTestResponse> {
    const response = await apiService.request({
      method: 'POST',
      url: '/api/v1/scheme/configurations/test',
      data: testRequest,
    });
    return response;
  }

  /**
   * Validate a configuration without testing
   */
  async validateConfiguration(configId: string): Promise<{
    valid: boolean;
    errors: string[];
    warnings: string[];
  }> {
    const response = await apiService.request({
      method: 'POST',
      url: `/api/v1/scheme/configurations/${configId}/validate`,
    });
    return response;
  }

  /**
   * Get test history for a configuration
   */
  async getTestHistory(configId: string, limit: number = 10): Promise<SchemeConfigTestResponse[]> {
    const response = await apiService.request({
      method: 'GET',
      url: `/api/v1/scheme/configurations/${configId}/test-history?limit=${limit}`,
    });
    return response;
  }

  // ============================================================================
  // SCHEME MESSAGE PROCESSING
  // ============================================================================

  /**
   * Send a scheme message using a specific configuration
   */
  async sendSchemeMessage(
    configId: string,
    message: SchemeMessageRequest
  ): Promise<SchemeMessageResponse> {
    const response = await apiService.request({
      method: 'POST',
      url: `/api/v1/scheme/configurations/${configId}/send`,
      data: message,
    });
    return response;
  }

  /**
   * Send a synchronous scheme message
   */
  async sendSynchronousMessage(
    configId: string,
    messageType: string,
    payload: any,
    format: MessageFormat = MessageFormat.JSON
  ): Promise<SchemeMessageResponse> {
    const message: SchemeMessageRequest = {
      messageType,
      messageId: `MSG-${Date.now()}`,
      correlationId: `CORR-${Date.now()}`,
      format,
      interactionMode: InteractionMode.SYNCHRONOUS,
      payload,
      metadata: {
        timestamp: new Date().toISOString(),
        source: 'frontend'
      }
    };

    return this.sendSchemeMessage(configId, message);
  }

  /**
   * Send an asynchronous scheme message
   */
  async sendAsynchronousMessage(
    configId: string,
    messageType: string,
    payload: any,
    format: MessageFormat = MessageFormat.JSON,
    responseMode: ResponseMode = ResponseMode.WEBHOOK
  ): Promise<{ messageId: string; correlationId: string; status: string }> {
    const message: SchemeMessageRequest = {
      messageType,
      messageId: `MSG-${Date.now()}`,
      correlationId: `CORR-${Date.now()}`,
      format,
      interactionMode: InteractionMode.ASYNCHRONOUS,
      payload,
      metadata: {
        timestamp: new Date().toISOString(),
        source: 'frontend',
        responseMode
      }
    };

    const response = await apiService.request({
      method: 'POST',
      url: `/api/v1/scheme/configurations/${configId}/send-async`,
      data: message,
    });
    return response;
  }

  /**
   * Poll for asynchronous message response
   */
  async pollMessageResponse(
    configId: string,
    correlationId: string,
    timeoutMs: number = 30000
  ): Promise<SchemeMessageResponse | null> {
    const response = await apiService.request({
      method: 'GET',
      url: `/api/v1/scheme/configurations/${configId}/poll/${correlationId}`,
      params: { timeoutMs },
    });
    return response;
  }

  // ============================================================================
  // STATISTICS AND MONITORING
  // ============================================================================

  /**
   * Get scheme interaction statistics
   */
  async getStatistics(
    configId?: string,
    fromDate?: string,
    toDate?: string
  ): Promise<SchemeInteractionStats> {
    const params = new URLSearchParams();
    if (configId) params.append('configId', configId);
    if (fromDate) params.append('fromDate', fromDate);
    if (toDate) params.append('toDate', toDate);

    const response = await apiService.request({
      method: 'GET',
      url: `/api/v1/scheme/statistics?${params.toString()}`,
    });
    return response;
  }

  /**
   * Get configuration health status
   */
  async getConfigurationHealth(configId: string): Promise<{
    status: 'HEALTHY' | 'DEGRADED' | 'UNHEALTHY';
    lastChecked: string;
    responseTime: number;
    errorRate: number;
    details: Record<string, any>;
  }> {
    const response = await apiService.request({
      method: 'GET',
      url: `/api/v1/scheme/configurations/${configId}/health`,
    });
    return response;
  }

  /**
   * Get all configuration health statuses
   */
  async getAllConfigurationHealth(): Promise<Record<string, {
    status: 'HEALTHY' | 'DEGRADED' | 'UNHEALTHY';
    lastChecked: string;
    responseTime: number;
    errorRate: number;
  }>> {
    const response = await apiService.request({
      method: 'GET',
      url: '/api/v1/scheme/configurations/health',
    });
    return response;
  }

  // ============================================================================
  // TEMPLATE MANAGEMENT
  // ============================================================================

  /**
   * Get available message templates
   */
  async getMessageTemplates(): Promise<Array<{
    id: string;
    name: string;
    messageType: string;
    format: MessageFormat;
    template: any;
    description: string;
  }>> {
    const response = await apiService.request({
      method: 'GET',
      url: '/api/v1/scheme/templates',
    });
    return response;
  }

  /**
   * Create a message from template
   */
  async createMessageFromTemplate(
    templateId: string,
    variables: Record<string, any>
  ): Promise<SchemeMessageRequest> {
    const response = await apiService.request({
      method: 'POST',
      url: `/api/v1/scheme/templates/${templateId}/create`,
      data: { variables },
    });
    return response;
  }

  // ============================================================================
  // CONVENIENCE METHODS
  // ============================================================================

  /**
   * Get active configurations only
   */
  async getActiveConfigurations(): Promise<SchemeInteractionConfig[]> {
    const response = await this.getConfigurations({ isActive: true });
    return response.content;
  }

  /**
   * Get configurations by interaction mode
   */
  async getConfigurationsByMode(mode: InteractionMode): Promise<SchemeInteractionConfig[]> {
    const response = await this.getConfigurations({ interactionMode: mode });
    return response.content;
  }

  /**
   * Get configurations by message format
   */
  async getConfigurationsByFormat(format: MessageFormat): Promise<SchemeInteractionConfig[]> {
    const response = await this.getConfigurations({ messageFormat: format });
    return response.content;
  }

  /**
   * Quick test for a configuration with default test message
   */
  async quickTest(configId: string): Promise<SchemeConfigTestResponse> {
    const testRequest: SchemeConfigTestRequest = {
      configId,
      testMessage: {
        messageType: 'pain001',
        messageId: `TEST-${Date.now()}`,
        correlationId: `TEST-CORR-${Date.now()}`,
        format: MessageFormat.JSON,
        interactionMode: InteractionMode.SYNCHRONOUS,
        payload: {
          test: true,
          timestamp: new Date().toISOString()
        }
      },
      validateOnly: false
    };

    return this.testConfiguration(testRequest);
  }

  /**
   * Health check for scheme service
   */
  async healthCheck(): Promise<{
    status: string;
    timestamp: string;
    version: string;
    uptime: number;
  }> {
    const response = await apiService.request({
      method: 'GET',
      url: '/api/v1/scheme/health',
    });
    return response;
  }
}

// Create and export singleton instance
const schemeApiService = new SchemeApiService();
export default schemeApiService;