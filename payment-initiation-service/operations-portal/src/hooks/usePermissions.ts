/**
 * usePermissions Hook
 * 
 * Hook for checking user permissions and roles.
 * Provides convenient methods for permission-based UI rendering.
 */

import { useAuth } from '@contexts/AuthContext';
import { Permission, UserRole } from '@types/auth';

/**
 * Hook for checking user permissions
 */
export function usePermissions() {
  const { user, hasPermission, hasRole, hasAnyRole } = useAuth();

  /**
   * Check if user has specific permission
   */
  const can = (permission: Permission): boolean => {
    return hasPermission(permission);
  };

  /**
   * Check if user has any of the specified permissions
   */
  const canAny = (permissions: Permission[]): boolean => {
    return permissions.some(permission => hasPermission(permission));
  };

  /**
   * Check if user has all of the specified permissions
   */
  const canAll = (permissions: Permission[]): boolean => {
    return permissions.every(permission => hasPermission(permission));
  };

  /**
   * Check if user has specific role
   */
  const isRole = (role: UserRole): boolean => {
    return hasRole(role);
  };

  /**
   * Check if user has any of the specified roles
   */
  const isAnyRole = (roles: UserRole[]): boolean => {
    return hasAnyRole(roles);
  };

  /**
   * Check if user is admin (any admin role)
   */
  const isAdmin = (): boolean => {
    return hasAnyRole([
      UserRole.ADMIN,
      UserRole.OPS_ADMIN,
      UserRole.PLATFORM_ADMIN,
      UserRole.TENANT_ADMIN,
    ]);
  };

  /**
   * Check if user is operations user (any ops role)
   */
  const isOperationsUser = (): boolean => {
    return hasAnyRole([
      UserRole.OPS_ADMIN,
      UserRole.OPS_OPERATOR,
      UserRole.OPS_VIEWER,
    ]);
  };

  /**
   * Check if user can view payments
   */
  const canViewPayments = (): boolean => {
    return can(Permission.PAYMENT_VIEW);
  };

  /**
   * Check if user can manage payments
   */
  const canManagePayments = (): boolean => {
    return canAny([
      Permission.PAYMENT_CREATE,
      Permission.PAYMENT_UPDATE,
      Permission.PAYMENT_DELETE,
      Permission.PAYMENT_REPAIR,
    ]);
  };

  /**
   * Check if user can manage services
   */
  const canManageServices = (): boolean => {
    return canAny([
      Permission.SERVICE_CONTROL,
      Permission.SERVICE_RESTART,
    ]);
  };

  /**
   * Check if user can view transactions
   */
  const canViewTransactions = (): boolean => {
    return can(Permission.TRANSACTION_VIEW);
  };

  /**
   * Check if user can search transactions
   */
  const canSearchTransactions = (): boolean => {
    return can(Permission.TRANSACTION_SEARCH);
  };

  /**
   * Check if user can export data
   */
  const canExport = (): boolean => {
    return can(Permission.TRANSACTION_EXPORT);
  };

  /**
   * Check if user can manage reconciliation
   */
  const canManageReconciliation = (): boolean => {
    return can(Permission.RECONCILIATION_MANAGE);
  };

  /**
   * Check if user can manage channels
   */
  const canManageChannels = (): boolean => {
    return can(Permission.CHANNEL_MANAGE);
  };

  /**
   * Check if user can manage clearing systems
   */
  const canManageClearingSystems = (): boolean => {
    return can(Permission.CLEARING_SYSTEM_MANAGE);
  };

  /**
   * Check if user has system admin privileges
   */
  const isSystemAdmin = (): boolean => {
    return can(Permission.SYSTEM_ADMIN);
  };

  /**
   * Check if user can manage users
   */
  const canManageUsers = (): boolean => {
    return can(Permission.USER_MANAGE);
  };

  /**
   * Check if user can manage tenants
   */
  const canManageTenants = (): boolean => {
    return can(Permission.TENANT_MANAGE);
  };

  /**
   * Get user's role display name
   */
  const getRoleDisplayName = (): string => {
    if (!user) return 'Unknown';
    
    const roleNames: Record<UserRole, string> = {
      [UserRole.ADMIN]: 'Administrator',
      [UserRole.OPS_ADMIN]: 'Operations Administrator',
      [UserRole.OPS_OPERATOR]: 'Operations Operator',
      [UserRole.OPS_VIEWER]: 'Operations Viewer',
      [UserRole.PLATFORM_ADMIN]: 'Platform Administrator',
      [UserRole.TENANT_ADMIN]: 'Tenant Administrator',
      [UserRole.BUSINESS_UNIT_ADMIN]: 'Business Unit Administrator',
    };

    const primaryRole = user.roles[0];
    return roleNames[primaryRole] || primaryRole;
  };

  /**
   * Get user's permission count
   */
  const getPermissionCount = (): number => {
    return user?.permissions.length || 0;
  };

  /**
   * Get user's role count
   */
  const getRoleCount = (): number => {
    return user?.roles.length || 0;
  };

  return {
    // Basic permission checks
    can,
    canAny,
    canAll,
    isRole,
    isAnyRole,
    
    // Role-based checks
    isAdmin,
    isOperationsUser,
    isSystemAdmin,
    
    // Feature-specific permission checks
    canViewPayments,
    canManagePayments,
    canManageServices,
    canViewTransactions,
    canSearchTransactions,
    canExport,
    canManageReconciliation,
    canManageChannels,
    canManageClearingSystems,
    canManageUsers,
    canManageTenants,
    
    // Utility functions
    getRoleDisplayName,
    getPermissionCount,
    getRoleCount,
  };
}

export default usePermissions;
