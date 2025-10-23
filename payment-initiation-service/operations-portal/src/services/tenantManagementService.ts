/**
 * Tenant Management Service Client
 *
 * API client for tenant management operations including channels,
 * clearing systems, and business unit configuration.
 */

import { BaseApiClient } from './baseApiClient';
import { HttpClient } from './httpClient';
import {
  Channel,
  ChannelOnboardingRequest,
  ChannelTestResult,
  ClearingSystem,
  ClearingSystemOnboardingRequest
} from '../types/onboarding';
import { ApiResponse, PaginationParams } from '../types/api';

export class TenantManagementService extends BaseApiClient {
  constructor(httpClient: HttpClient) {
    super(httpClient);
  }

  /**
   * Get all channels for the current tenant
   */
  async getAllChannels(): Promise<Channel[]> {
    const response = await this.httpClient.get<Channel[]>('/tenant/v1/channels');
    return this.handleResponse(response);
  }

  /**
   * Get channel by ID
   */
  async getChannel(channelId: string): Promise<Channel> {
    this.validateRequired({ channelId }, ['channelId']);

    const response = await this.httpClient.get<Channel>(`/tenant/v1/channels/${channelId}`);
    return this.handleResponse(response);
  }

  /**
   * Create new channel
   */
  async createChannel(request: ChannelOnboardingRequest): Promise<Channel> {
    const response = await this.httpClient.post<Channel>('/tenant/v1/channels', request);
    return this.handleResponse(response);
  }

  /**
   * Update existing channel
   */
  async updateChannel(channelId: string, updates: Partial<ChannelOnboardingRequest>): Promise<Channel> {
    this.validateRequired({ channelId }, ['channelId']);

    const response = await this.httpClient.put<Channel>(`/tenant/v1/channels/${channelId}`, updates);
    return this.handleResponse(response);
  }

  /**
   * Delete channel
   */
  async deleteChannel(channelId: string): Promise<void> {
    this.validateRequired({ channelId }, ['channelId']);

    const response = await this.httpClient.delete(`/tenant/v1/channels/${channelId}`);
    return this.handleResponse(response);
  }

  /**
   * Test channel connection
   */
  async testChannel(channelId: string): Promise<ChannelTestResult> {
    this.validateRequired({ channelId }, ['channelId']);

    const response = await this.httpClient.post<ChannelTestResult>(`/tenant/v1/channels/${channelId}/test`);
    return this.handleResponse(response);
  }

  /**
   * Test channel connection (alias for testChannel)
   */
  async testChannelConnection(channelId: string): Promise<ChannelTestResult> {
    return this.testChannel(channelId);
  }

  /**
   * Get all clearing systems for the current tenant
   */
  async getAllClearingSystems(): Promise<ClearingSystem[]> {
    const response = await this.httpClient.get<ClearingSystem[]>('/tenant/v1/clearing-systems');
    return this.handleResponse(response);
  }

  /**
   * Get clearing system by ID
   */
  async getClearingSystem(systemId: string): Promise<ClearingSystem> {
    this.validateRequired({ systemId }, ['systemId']);

    const response = await this.httpClient.get<ClearingSystem>(`/tenant/v1/clearing-systems/${systemId}`);
    return this.handleResponse(response);
  }

  /**
   * Create new clearing system
   */
  async createClearingSystem(request: ClearingSystemOnboardingRequest): Promise<ClearingSystem> {
    const response = await this.httpClient.post<ClearingSystem>('/tenant/v1/clearing-systems', request);
    return this.handleResponse(response);
  }

  /**
   * Update existing clearing system
   */
  async updateClearingSystem(systemId: string, updates: Partial<ClearingSystemOnboardingRequest>): Promise<ClearingSystem> {
    this.validateRequired({ systemId }, ['systemId']);

    const response = await this.httpClient.put<ClearingSystem>(`/tenant/v1/clearing-systems/${systemId}`, updates);
    return this.handleResponse(response);
  }

  /**
   * Delete clearing system
   */
  async deleteClearingSystem(systemId: string): Promise<void> {
    this.validateRequired({ systemId }, ['systemId']);

    const response = await this.httpClient.delete(`/tenant/v1/clearing-systems/${systemId}`);
    return this.handleResponse(response);
  }

  /**
   * Test clearing system connection
   */
  async testClearingSystem(systemId: string): Promise<ChannelTestResult> {
    this.validateRequired({ systemId }, ['systemId']);

    const response = await this.httpClient.post<ChannelTestResult>(`/tenant/v1/clearing-systems/${systemId}/test`);
    return this.handleResponse(response);
  }
}

export default TenantManagementService;
