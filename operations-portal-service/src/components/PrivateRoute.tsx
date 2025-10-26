/**
 * Private Route Component
 * 
 * Higher-order component for protecting routes that require authentication.
 * Redirects to login page if user is not authenticated.
 */

import React, { ReactNode } from 'react';
import { Navigate, useLocation } from 'react-router-dom';
import { useAuth } from '@contexts/AuthContext';
import { Box, CircularProgress, Typography } from '@mui/material';

interface PrivateRouteProps {
  children: ReactNode;
  requiredPermissions?: string[];
  requiredRoles?: string[];
  fallback?: ReactNode;
}

export function PrivateRoute({ 
  children, 
  requiredPermissions = [], 
  requiredRoles = [],
  fallback 
}: PrivateRouteProps): React.ReactElement {
  const { isAuthenticated, isLoading, user, hasPermission, hasAnyRole } = useAuth();
  const location = useLocation();

  // Show loading spinner while checking authentication
  if (isLoading) {
    return (
      <Box 
        display="flex" 
        justifyContent="center" 
        alignItems="center" 
        minHeight="100vh"
        flexDirection="column"
        gap={2}
      >
        <CircularProgress size={40} />
        <Typography variant="body2" color="text.secondary">
          Authenticating...
        </Typography>
      </Box>
    );
  }

  // Redirect to login if not authenticated
  if (!isAuthenticated) {
    return <Navigate to="/login" state={{ from: location }} replace />;
  }

  // Check required permissions
  if (requiredPermissions.length > 0) {
    const hasRequiredPermissions = requiredPermissions.every(permission => 
      hasPermission(permission as any)
    );
    
    if (!hasRequiredPermissions) {
      if (fallback) {
        return <React.Fragment>{fallback}</React.Fragment>;
      }
      return (
        <Box 
          display="flex" 
          justifyContent="center" 
          alignItems="center" 
          minHeight="100vh"
          flexDirection="column"
          gap={2}
        >
          <Typography variant="h5" color="error">
            Access Denied
          </Typography>
          <Typography variant="body1" color="text.secondary">
            You don't have the required permissions to access this page.
          </Typography>
        </Box>
      );
    }
  }

  // Check required roles
  if (requiredRoles.length > 0) {
    const hasRequiredRoles = hasAnyRole(requiredRoles as any);
    
    if (!hasRequiredRoles) {
      if (fallback) {
        return <React.Fragment>{fallback}</React.Fragment>;
      }
      return (
        <Box 
          display="flex" 
          justifyContent="center" 
          alignItems="center" 
          minHeight="100vh"
          flexDirection="column"
          gap={2}
        >
          <Typography variant="h5" color="error">
            Access Denied
          </Typography>
          <Typography variant="body1" color="text.secondary">
            You don't have the required role to access this page.
          </Typography>
        </Box>
      );
    }
  }

  return <React.Fragment>{children}</React.Fragment>;
}

export default PrivateRoute;
