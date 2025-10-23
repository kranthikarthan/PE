/**
 * Authentication Types
 * 
 * TypeScript interfaces for authentication, authorization, and user management.
 */

export enum UserRole {
  ADMIN = 'ADMIN',
  OPS_ADMIN = 'OPS_ADMIN',
  OPS_OPERATOR = 'OPS_OPERATOR',
  OPS_VIEWER = 'OPS_VIEWER',
  PLATFORM_ADMIN = 'PLATFORM_ADMIN',
  TENANT_ADMIN = 'TENANT_ADMIN',
  BUSINESS_UNIT_ADMIN = 'BUSINESS_UNIT_ADMIN',
}

export enum Permission {
  // Payment Operations
  PAYMENT_VIEW = 'PAYMENT_VIEW',
  PAYMENT_CREATE = 'PAYMENT_CREATE',
  PAYMENT_UPDATE = 'PAYMENT_UPDATE',
  PAYMENT_DELETE = 'PAYMENT_DELETE',
  PAYMENT_REPAIR = 'PAYMENT_REPAIR',
  
  // Service Management
  SERVICE_VIEW = 'SERVICE_VIEW',
  SERVICE_CONTROL = 'SERVICE_CONTROL',
  SERVICE_RESTART = 'SERVICE_RESTART',
  
  // Transaction Operations
  TRANSACTION_VIEW = 'TRANSACTION_VIEW',
  TRANSACTION_SEARCH = 'TRANSACTION_SEARCH',
  TRANSACTION_EXPORT = 'TRANSACTION_EXPORT',
  
  // Reconciliation
  RECONCILIATION_VIEW = 'RECONCILIATION_VIEW',
  RECONCILIATION_MANAGE = 'RECONCILIATION_MANAGE',
  
  // Onboarding
  CHANNEL_VIEW = 'CHANNEL_VIEW',
  CHANNEL_MANAGE = 'CHANNEL_MANAGE',
  CLEARING_SYSTEM_VIEW = 'CLEARING_SYSTEM_VIEW',
  CLEARING_SYSTEM_MANAGE = 'CLEARING_SYSTEM_MANAGE',
  
  // System Administration
  SYSTEM_ADMIN = 'SYSTEM_ADMIN',
  USER_MANAGE = 'USER_MANAGE',
  TENANT_MANAGE = 'TENANT_MANAGE',
}

export interface User {
  id: string;
  username: string;
  email: string;
  firstName: string;
  lastName: string;
  roles: UserRole[];
  permissions: Permission[];
  tenantId: string;
  businessUnitId: string;
  isActive: boolean;
  lastLoginAt?: string;
  createdAt: string;
  updatedAt: string;
  lastLogin?: string; // For backward compatibility
}

export interface LoginRequest {
  username: string;
  password: string;
  tenantId?: string;
  businessUnitId?: string;
}

export interface LoginResponse {
  user: User;
  accessToken: string;
  refreshToken: string;
  expiresIn: number;
  tokenType: string;
}

export interface AuthResponse {
  token: string;
  user: User;
}

export interface RefreshTokenRequest {
  refreshToken: string;
}

export interface RefreshTokenResponse {
  accessToken: string;
  refreshToken: string;
  expiresIn: number;
  tokenType: string;
}

export interface LogoutRequest {
  refreshToken: string;
}

export interface AuthContextType {
  user: User | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  error: string | null;
  login: (credentials: LoginRequest) => Promise<void>;
  logout: () => Promise<void>;
  refreshToken: () => Promise<void>;
  hasPermission: (permission: Permission) => boolean;
  hasRole: (role: UserRole) => boolean;
  hasAnyRole: (roles: UserRole[]) => boolean;
}

export interface TenantContextType {
  tenantId: string;
  businessUnitId: string;
  setTenant: (tenantId: string) => void;
  setBusinessUnit: (businessUnitId: string) => void;
}

export interface SessionInfo {
  userId: string;
  tenantId: string;
  businessUnitId: string;
  roles: UserRole[];
  permissions: Permission[];
  expiresAt: string;
  issuedAt: string;
}
