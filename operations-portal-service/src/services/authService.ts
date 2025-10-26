/**
 * Authentication Service Client
 * 
 * API client for authentication, authorization, and user management.
 */

import { BaseApiClient } from './baseApiClient';
import { HttpClient } from './httpClient';
import { 
  User, 
  LoginRequest, 
  LoginResponse, 
  RefreshTokenRequest, 
  RefreshTokenResponse,
  LogoutRequest,
  UserRole,
  Permission
} from '../types/auth';
import { ApiResponse } from '../types/api';
import { API_ENDPOINTS } from '../constants';

export class AuthService extends BaseApiClient {
  constructor(httpClient: HttpClient) {
    super(httpClient);
  }

  /**
   * Login user
   */
  async login(credentials: LoginRequest): Promise<LoginResponse> {
    this.validateRequired(credentials, ['username', 'password']);

    const response = await this.httpClient.post<LoginResponse>(
      API_ENDPOINTS.AUTH.LOGIN,
      {
        username: this.sanitizeString(credentials.username),
        password: credentials.password, // Don't sanitize password
        tenantId: credentials.tenantId,
        businessUnitId: credentials.businessUnitId,
      }
    );
    
    return this.handleResponse(response);
  }

  /**
   * Logout user
   */
  async logout(logoutRequest: LogoutRequest): Promise<void> {
    this.validateRequired(logoutRequest, ['refreshToken']);

    await this.httpClient.post<void>(
      API_ENDPOINTS.AUTH.LOGOUT,
      logoutRequest
    );
  }

  /**
   * Refresh authentication token
   */
  async refreshToken(refreshRequest: RefreshTokenRequest): Promise<RefreshTokenResponse> {
    this.validateRequired(refreshRequest, ['refreshToken']);

    const response = await this.httpClient.post<RefreshTokenResponse>(
      API_ENDPOINTS.AUTH.REFRESH,
      refreshRequest
    );
    
    return this.handleResponse(response);
  }

  /**
   * Get user profile
   */
  async getUserProfile(): Promise<User> {
    const response = await this.httpClient.get<User>(
      API_ENDPOINTS.AUTH.PROFILE
    );
    
    return this.handleResponse(response);
  }

  /**
   * Get user permissions
   */
  async getUserPermissions(): Promise<Permission[]> {
    const response = await this.httpClient.get<Permission[]>(
      API_ENDPOINTS.AUTH.PERMISSIONS
    );
    
    return this.handleResponse(response);
  }

  /**
   * Check if user has specific permission
   */
  async hasPermission(permission: Permission): Promise<boolean> {
    try {
      const permissions = await this.getUserPermissions();
      return permissions.includes(permission);
    } catch {
      return false;
    }
  }

  /**
   * Check if user has any of the specified permissions
   */
  async hasAnyPermission(permissions: Permission[]): Promise<boolean> {
    try {
      const userPermissions = await this.getUserPermissions();
      return permissions.some(permission => userPermissions.includes(permission));
    } catch {
      return false;
    }
  }

  /**
   * Check if user has specific role
   */
  async hasRole(role: UserRole): Promise<boolean> {
    try {
      const user = await this.getUserProfile();
      return user.roles.includes(role);
    } catch {
      return false;
    }
  }

  /**
   * Check if user has any of the specified roles
   */
  async hasAnyRole(roles: UserRole[]): Promise<boolean> {
    try {
      const user = await this.getUserProfile();
      return roles.some(role => user.roles.includes(role));
    } catch {
      return false;
    }
  }

  /**
   * Update user profile
   */
  async updateUserProfile(updates: Partial<User>): Promise<User> {
    const response = await this.httpClient.put<User>(
      API_ENDPOINTS.AUTH.PROFILE,
      updates
    );
    
    return this.handleResponse(response);
  }

  /**
   * Change password
   */
  async changePassword(
    currentPassword: string,
    newPassword: string
  ): Promise<void> {
    this.validateRequired({ currentPassword, newPassword }, ['currentPassword', 'newPassword']);

    await this.httpClient.post<void>(
      `${API_ENDPOINTS.AUTH.PROFILE}/change-password`,
      {
        currentPassword,
        newPassword,
      }
    );
  }

  /**
   * Get user session info
   */
  async getSessionInfo(): Promise<{
    userId: string;
    tenantId: string;
    businessUnitId: string;
    roles: UserRole[];
    permissions: Permission[];
    expiresAt: string;
    issuedAt: string;
  }> {
    const response = await this.httpClient.get<any>(
      `${API_ENDPOINTS.AUTH.BASE}/session`
    );
    
    return this.handleResponse(response);
  }

  /**
   * Validate current session
   */
  async validateSession(): Promise<boolean> {
    try {
      await this.getUserProfile();
      return true;
    } catch {
      return false;
    }
  }

  /**
   * Get user activity log
   */
  async getUserActivityLog(
    limit: number = 50,
    offset: number = 0
  ): Promise<Array<{
    id: string;
    action: string;
    resource: string;
    timestamp: string;
    ipAddress: string;
    userAgent: string;
  }>> {
    const queryParams = {
      limit: Math.min(limit, 100), // Cap at 100
      offset: Math.max(offset, 0),
    };

    const url = this.buildUrl(`${API_ENDPOINTS.AUTH.BASE}/activity`, queryParams);
    const response = await this.httpClient.get<any[]>(url);
    
    return this.handleResponse(response);
  }

  /**
   * Get user preferences
   */
  async getUserPreferences(): Promise<Record<string, any>> {
    const response = await this.httpClient.get<Record<string, any>>(
      `${API_ENDPOINTS.AUTH.PROFILE}/preferences`
    );
    
    return this.handleResponse(response);
  }

  /**
   * Update user preferences
   */
  async updateUserPreferences(preferences: Record<string, any>): Promise<Record<string, any>> {
    const response = await this.httpClient.put<Record<string, any>>(
      `${API_ENDPOINTS.AUTH.PROFILE}/preferences`,
      preferences
    );
    
    return this.handleResponse(response);
  }

  /**
   * Get available tenants for user
   */
  async getAvailableTenants(): Promise<Array<{
    id: string;
    name: string;
    businessUnits: Array<{
      id: string;
      name: string;
    }>;
  }>> {
    const response = await this.httpClient.get<any[]>(
      `${API_ENDPOINTS.AUTH.BASE}/tenants`
    );
    
    return this.handleResponse(response);
  }

  /**
   * Switch tenant context
   */
  async switchTenant(tenantId: string, businessUnitId?: string): Promise<void> {
    this.validateRequired({ tenantId }, ['tenantId']);

    await this.httpClient.post<void>(
      `${API_ENDPOINTS.AUTH.BASE}/switch-tenant`,
      {
        tenantId,
        businessUnitId,
      }
    );
  }
}
