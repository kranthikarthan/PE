/**
 * Authentication Context
 * 
 * React Context for managing authentication state, user session,
 * and authentication operations throughout the application.
 */

import React, { createContext, useContext, useReducer, useEffect, ReactNode } from 'react';
import { 
  User, 
  LoginRequest, 
  AuthContextType, 
  UserRole, 
  Permission 
} from '@types/auth';
import { getAuthService } from '@services';
import { config } from '@config/environment';
import { STORAGE_KEYS } from '@constants';

// Auth State Interface
interface AuthState {
  user: User | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  error: string | null;
  lastActivity: number;
}

// Auth Actions
type AuthAction =
  | { type: 'LOGIN_START' }
  | { type: 'LOGIN_SUCCESS'; payload: User }
  | { type: 'LOGIN_FAILURE'; payload: string }
  | { type: 'LOGOUT' }
  | { type: 'SET_LOADING'; payload: boolean }
  | { type: 'SET_ERROR'; payload: string | null }
  | { type: 'UPDATE_USER'; payload: User }
  | { type: 'UPDATE_ACTIVITY' };

// Initial State
const initialState: AuthState = {
  user: null,
  isAuthenticated: false,
  isLoading: true,
  error: null,
  lastActivity: Date.now(),
};

// Auth Reducer
function authReducer(state: AuthState, action: AuthAction): AuthState {
  switch (action.type) {
    case 'LOGIN_START':
      return {
        ...state,
        isLoading: true,
        error: null,
      };
    case 'LOGIN_SUCCESS':
      return {
        ...state,
        user: action.payload,
        isAuthenticated: true,
        isLoading: false,
        error: null,
        lastActivity: Date.now(),
      };
    case 'LOGIN_FAILURE':
      return {
        ...state,
        user: null,
        isAuthenticated: false,
        isLoading: false,
        error: action.payload,
        lastActivity: Date.now(),
      };
    case 'LOGOUT':
      return {
        ...state,
        user: null,
        isAuthenticated: false,
        isLoading: false,
        error: null,
        lastActivity: Date.now(),
      };
    case 'SET_LOADING':
      return {
        ...state,
        isLoading: action.payload,
      };
    case 'SET_ERROR':
      return {
        ...state,
        error: action.payload,
      };
    case 'UPDATE_USER':
      return {
        ...state,
        user: action.payload,
        lastActivity: Date.now(),
      };
    case 'UPDATE_ACTIVITY':
      return {
        ...state,
        lastActivity: Date.now(),
      };
    default:
      return state;
  }
}

// Create Context
const AuthContext = createContext<AuthContextType | undefined>(undefined);

// Auth Provider Props
interface AuthProviderProps {
  children: ReactNode;
}

// Auth Provider Component
export function AuthProvider({ children }: AuthProviderProps) {
  const [state, dispatch] = useReducer(authReducer, initialState);

  // Initialize auth state on mount
  useEffect(() => {
    initializeAuth();
  }, []);

  // Check for token refresh periodically
  useEffect(() => {
    if (state.isAuthenticated) {
      const interval = setInterval(() => {
        checkTokenValidity();
      }, 5 * 60 * 1000); // Check every 5 minutes

      return () => clearInterval(interval);
    }
  }, [state.isAuthenticated]);

  // Initialize authentication state
  const initializeAuth = async () => {
    try {
      const token = localStorage.getItem(config.authTokenKey);
      if (!token) {
        dispatch({ type: 'SET_LOADING', payload: false });
        return;
      }

      // Validate token by getting user profile
      const authService = getAuthService();
      const user = await authService.getUserProfile();
      
      dispatch({ type: 'LOGIN_SUCCESS', payload: user });
    } catch (error) {
      console.error('Auth initialization failed:', error);
      // Clear invalid tokens
      localStorage.removeItem(config.authTokenKey);
      localStorage.removeItem(config.authRefreshTokenKey);
      dispatch({ type: 'SET_LOADING', payload: false });
    }
  };

  // Check token validity
  const checkTokenValidity = async () => {
    try {
      const authService = getAuthService();
      const isValid = await authService.validateSession();
      
      if (!isValid) {
        dispatch({ type: 'LOGOUT' });
        localStorage.removeItem(config.authTokenKey);
        localStorage.removeItem(config.authRefreshTokenKey);
      }
    } catch (error) {
      console.error('Token validation failed:', error);
      dispatch({ type: 'LOGOUT' });
    }
  };

  // Login function
  const login = async (credentials: LoginRequest): Promise<void> => {
    try {
      dispatch({ type: 'LOGIN_START' });

      const authService = getAuthService();
      const response = await authService.login(credentials);

      // Store tokens
      localStorage.setItem(config.authTokenKey, response.accessToken);
      localStorage.setItem(config.authRefreshTokenKey, response.refreshToken);

      dispatch({ type: 'LOGIN_SUCCESS', payload: response.user });
    } catch (error: any) {
      const errorMessage = error.message || 'Login failed';
      dispatch({ type: 'LOGIN_FAILURE', payload: errorMessage });
      throw error;
    }
  };

  // Logout function
  const logout = async (): Promise<void> => {
    try {
      const refreshToken = localStorage.getItem(config.authRefreshTokenKey);
      if (refreshToken) {
        const authService = getAuthService();
        await authService.logout({ refreshToken });
      }
    } catch (error) {
      console.error('Logout error:', error);
    } finally {
      // Clear tokens and state
      localStorage.removeItem(config.authTokenKey);
      localStorage.removeItem(config.authRefreshTokenKey);
      dispatch({ type: 'LOGOUT' });
    }
  };

  // Refresh token function
  const refreshToken = async (): Promise<void> => {
    try {
      const refreshToken = localStorage.getItem(config.authRefreshTokenKey);
      if (!refreshToken) {
        throw new Error('No refresh token available');
      }

      const authService = getAuthService();
      const response = await authService.refreshToken({ refreshToken });

      // Update tokens
      localStorage.setItem(config.authTokenKey, response.accessToken);
      localStorage.setItem(config.authRefreshTokenKey, response.refreshToken);
    } catch (error) {
      console.error('Token refresh failed:', error);
      dispatch({ type: 'LOGOUT' });
      throw error;
    }
  };

  // Check permission
  const hasPermission = (permission: Permission): boolean => {
    if (!state.user) return false;
    return state.user.permissions.includes(permission);
  };

  // Check role
  const hasRole = (role: UserRole): boolean => {
    if (!state.user) return false;
    return state.user.roles.includes(role);
  };

  // Check any role
  const hasAnyRole = (roles: UserRole[]): boolean => {
    if (!state.user) return false;
    return roles.some(role => state.user!.roles.includes(role));
  };

  // Update user profile
  const updateUser = (user: User) => {
    dispatch({ type: 'UPDATE_USER', payload: user });
  };

  // Update activity timestamp
  const updateActivity = () => {
    dispatch({ type: 'UPDATE_ACTIVITY' });
  };

  // Clear error
  const clearError = () => {
    dispatch({ type: 'SET_ERROR', payload: null });
  };

  const contextValue: AuthContextType = {
    user: state.user,
    isAuthenticated: state.isAuthenticated,
    isLoading: state.isLoading,
    login,
    logout,
    refreshToken,
    hasPermission,
    hasRole,
    hasAnyRole,
  };

  return (
    <AuthContext.Provider value={contextValue}>
      {children}
    </AuthContext.Provider>
  );
}

// Custom hook to use auth context
export function useAuth(): AuthContextType {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
}

// Higher-order component for protected routes
export function withAuth<P extends object>(
  Component: React.ComponentType<P>
): React.ComponentType<P> {
  return function AuthenticatedComponent(props: P) {
    const { isAuthenticated, isLoading } = useAuth();

    if (isLoading) {
      return <div>Loading...</div>; // Replace with proper loading component
    }

    if (!isAuthenticated) {
      // Redirect to login or show unauthorized message
      window.location.href = '/login';
      return null;
    }

    return <Component {...props} />;
  };
}

export default AuthContext;
