/**
 * usePermissions Hook Tests
 * 
 * Unit tests for the usePermissions custom hook.
 * Tests permission checking and role-based access control.
 */

import { renderHook } from '@testing-library/react';
import { usePermissions } from '../usePermissions';
import { fixtures } from '../../test-utils/fixtures';

// Mock the auth context
const mockUseAuth = jest.fn();
const mockUseTenant = jest.fn();

jest.mock('@contexts/AuthContext', () => ({
  useAuth: () => mockUseAuth(),
}));

jest.mock('@contexts/TenantContext', () => ({
  useTenant: () => mockUseTenant(),
}));

describe('usePermissions', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  describe('with authenticated user', () => {
    beforeEach(() => {
      mockUseAuth.mockReturnValue({
        isAuthenticated: true,
        user: fixtures.user,
      });
      mockUseTenant.mockReturnValue({
        tenantId: 'tenant-1',
        businessUnitId: 'bu-1',
      });
    });

    it('should allow access for users with required permissions', () => {
      const { result } = renderHook(() => usePermissions());

      expect(result.current.canManageServices()).toBe(true);
      expect(result.current.canViewPayments()).toBe(true);
      expect(result.current.canManageChannels()).toBe(true);
    });

    it('should deny access for users without required permissions', () => {
      const userWithoutPermissions = {
        ...fixtures.user,
        permissions: ['VIEW_PAYMENTS'], // Only view permissions
      };

      mockUseAuth.mockReturnValue({
        isAuthenticated: true,
        user: userWithoutPermissions,
      });

      const { result } = renderHook(() => usePermissions());

      expect(result.current.canManageServices()).toBe(false);
      expect(result.current.canViewPayments()).toBe(true);
      expect(result.current.canManageChannels()).toBe(false);
    });

    it('should allow access for admin users', () => {
      const adminUser = {
        ...fixtures.user,
        roles: ['ADMIN'],
        permissions: ['*'], // All permissions
      };

      mockUseAuth.mockReturnValue({
        isAuthenticated: true,
        user: adminUser,
      });

      const { result } = renderHook(() => usePermissions());

      expect(result.current.canManageServices()).toBe(true);
      expect(result.current.canViewPayments()).toBe(true);
      expect(result.current.canManageChannels()).toBe(true);
      expect(result.current.canManageReconciliation()).toBe(true);
      expect(result.current.canManageClearingSystems()).toBe(true);
    });

    it('should allow access for operator users with specific permissions', () => {
      const operatorUser = {
        ...fixtures.user,
        roles: ['OPERATOR'],
        permissions: ['MANAGE_SERVICES', 'VIEW_PAYMENTS'],
      };

      mockUseAuth.mockReturnValue({
        isAuthenticated: true,
        user: operatorUser,
      });

      const { result } = renderHook(() => usePermissions());

      expect(result.current.canManageServices()).toBe(true);
      expect(result.current.canViewPayments()).toBe(true);
      expect(result.current.canManageChannels()).toBe(false);
    });

    it('should check specific permissions', () => {
      const { result } = renderHook(() => usePermissions());

      expect(result.current.hasPermission('MANAGE_SERVICES')).toBe(true);
      expect(result.current.hasPermission('VIEW_PAYMENTS')).toBe(true);
      expect(result.current.hasPermission('NONEXISTENT_PERMISSION')).toBe(false);
    });

    it('should check multiple permissions', () => {
      const { result } = renderHook(() => usePermissions());

      expect(result.current.hasAnyPermission(['MANAGE_SERVICES', 'VIEW_PAYMENTS'])).toBe(true);
      expect(result.current.hasAnyPermission(['NONEXISTENT_PERMISSION'])).toBe(false);
    });

    it('should check all permissions', () => {
      const { result } = renderHook(() => usePermissions());

      expect(result.current.hasAllPermissions(['MANAGE_SERVICES', 'VIEW_PAYMENTS'])).toBe(true);
      expect(result.current.hasAllPermissions(['MANAGE_SERVICES', 'NONEXISTENT_PERMISSION'])).toBe(false);
    });

    it('should check roles', () => {
      const { result } = renderHook(() => usePermissions());

      expect(result.current.hasRole('ADMIN')).toBe(true);
      expect(result.current.hasRole('OPERATOR')).toBe(true);
      expect(result.current.hasRole('VIEWER')).toBe(false);
    });

    it('should check multiple roles', () => {
      const { result } = renderHook(() => usePermissions());

      expect(result.current.hasAnyRole(['ADMIN', 'OPERATOR'])).toBe(true);
      expect(result.current.hasAnyRole(['VIEWER'])).toBe(false);
    });

    it('should check all roles', () => {
      const { result } = renderHook(() => usePermissions());

      expect(result.current.hasAllRoles(['ADMIN', 'OPERATOR'])).toBe(true);
      expect(result.current.hasAllRoles(['ADMIN', 'VIEWER'])).toBe(false);
    });
  });

  describe('with unauthenticated user', () => {
    beforeEach(() => {
      mockUseAuth.mockReturnValue({
        isAuthenticated: false,
        user: null,
      });
      mockUseTenant.mockReturnValue({
        tenantId: null,
        businessUnitId: null,
      });
    });

    it('should deny all permissions', () => {
      const { result } = renderHook(() => usePermissions());

      expect(result.current.canManageServices()).toBe(false);
      expect(result.current.canViewPayments()).toBe(false);
      expect(result.current.canManageChannels()).toBe(false);
      expect(result.current.canManageReconciliation()).toBe(false);
      expect(result.current.canManageClearingSystems()).toBe(false);
    });

    it('should deny all role checks', () => {
      const { result } = renderHook(() => usePermissions());

      expect(result.current.hasRole('ADMIN')).toBe(false);
      expect(result.current.hasRole('OPERATOR')).toBe(false);
      expect(result.current.hasAnyRole(['ADMIN', 'OPERATOR'])).toBe(false);
      expect(result.current.hasAllRoles(['ADMIN', 'OPERATOR'])).toBe(false);
    });

    it('should deny all permission checks', () => {
      const { result } = renderHook(() => usePermissions());

      expect(result.current.hasPermission('MANAGE_SERVICES')).toBe(false);
      expect(result.current.hasAnyPermission(['MANAGE_SERVICES'])).toBe(false);
      expect(result.current.hasAllPermissions(['MANAGE_SERVICES'])).toBe(false);
    });
  });

  describe('with user without permissions', () => {
    beforeEach(() => {
      mockUseAuth.mockReturnValue({
        isAuthenticated: true,
        user: {
          ...fixtures.user,
          permissions: [],
        },
      });
      mockUseTenant.mockReturnValue({
        tenantId: 'tenant-1',
        businessUnitId: 'bu-1',
      });
    });

    it('should deny all permissions', () => {
      const { result } = renderHook(() => usePermissions());

      expect(result.current.canManageServices()).toBe(false);
      expect(result.current.canViewPayments()).toBe(false);
      expect(result.current.canManageChannels()).toBe(false);
    });
  });

  describe('with user without roles', () => {
    beforeEach(() => {
      mockUseAuth.mockReturnValue({
        isAuthenticated: true,
        user: {
          ...fixtures.user,
          roles: [],
        },
      });
      mockUseTenant.mockReturnValue({
        tenantId: 'tenant-1',
        businessUnitId: 'bu-1',
      });
    });

    it('should deny all role checks', () => {
      const { result } = renderHook(() => usePermissions());

      expect(result.current.hasRole('ADMIN')).toBe(false);
      expect(result.current.hasRole('OPERATOR')).toBe(false);
    });
  });

  describe('with null user', () => {
    beforeEach(() => {
      mockUseAuth.mockReturnValue({
        isAuthenticated: false,
        user: null,
      });
      mockUseTenant.mockReturnValue({
        tenantId: null,
        businessUnitId: null,
      });
    });

    it('should handle null user gracefully', () => {
      const { result } = renderHook(() => usePermissions());

      expect(result.current.canManageServices()).toBe(false);
      expect(result.current.canViewPayments()).toBe(false);
      expect(result.current.hasRole('ADMIN')).toBe(false);
      expect(result.current.hasPermission('MANAGE_SERVICES')).toBe(false);
    });
  });

  describe('with undefined user', () => {
    beforeEach(() => {
      mockUseAuth.mockReturnValue({
        isAuthenticated: false,
        user: undefined,
      });
      mockUseTenant.mockReturnValue({
        tenantId: null,
        businessUnitId: null,
      });
    });

    it('should handle undefined user gracefully', () => {
      const { result } = renderHook(() => usePermissions());

      expect(result.current.canManageServices()).toBe(false);
      expect(result.current.canViewPayments()).toBe(false);
      expect(result.current.hasRole('ADMIN')).toBe(false);
      expect(result.current.hasPermission('MANAGE_SERVICES')).toBe(false);
    });
  });

  describe('permission combinations', () => {
    beforeEach(() => {
      mockUseAuth.mockReturnValue({
        isAuthenticated: true,
        user: fixtures.user,
      });
      mockUseTenant.mockReturnValue({
        tenantId: 'tenant-1',
        businessUnitId: 'bu-1',
      });
    });

    it('should handle complex permission scenarios', () => {
      const { result } = renderHook(() => usePermissions());

      // Test specific permission combinations
      expect(result.current.canManageServices()).toBe(true);
      expect(result.current.canViewPayments()).toBe(true);
      expect(result.current.canManageChannels()).toBe(true);
      expect(result.current.canManageReconciliation()).toBe(true);
      expect(result.current.canManageClearingSystems()).toBe(true);
    });

    it('should handle permission arrays', () => {
      const { result } = renderHook(() => usePermissions());

      const requiredPermissions = ['MANAGE_SERVICES', 'VIEW_PAYMENTS'];
      expect(result.current.hasAllPermissions(requiredPermissions)).toBe(true);
    });

    it('should handle role arrays', () => {
      const { result } = renderHook(() => usePermissions());

      const requiredRoles = ['ADMIN', 'OPERATOR'];
      expect(result.current.hasAllRoles(requiredRoles)).toBe(true);
    });
  });
});
