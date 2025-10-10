import { createSlice, createAsyncThunk, PayloadAction } from '@reduxjs/toolkit';
import { AuthState, User } from '../../types';
import apiService from '../../services/api';

type LoginResult = {
  token: string;
  refreshToken: string;
  user: User;
};

type RefreshResult = {
  token: string;
  refreshToken: string;
};

const initialState: AuthState = {
  isAuthenticated: false,
  user: null,
  token: localStorage.getItem('authToken'),
  refreshToken: localStorage.getItem('refreshToken'),
  loading: false,
  error: null,
};

// Async thunks
export const login = createAsyncThunk<LoginResult,
  { username: string; password: string },
  { rejectValue: string }>(
  'auth/login',
  async ({ username, password }, { rejectWithValue }) => {
    try {
      const response = await apiService.login(username, password);

      const token = response?.accessToken ?? '';
      const refreshTokenValue = response?.refreshToken ?? '';
      if (!token || !refreshTokenValue) {
        throw new Error('Authentication response missing tokens');
      }

      const roles = Array.isArray(response.roles)
        ? response.roles
        : Array.from(response.roles ?? []);
      const permissions = Array.isArray(response.permissions)
        ? response.permissions
        : Array.from(response.permissions ?? []);

      const user: User = {
        id: response.userId ?? '',
        username: response.username ?? username,
        email: response.email ?? '',
        firstName: (response as any).firstName ?? undefined,
        lastName: (response as any).lastName ?? undefined,
        roles,
        permissions,
        lastLoginAt: (response as any).lastLoginAt ?? undefined,
        isActive: true,
      };

      localStorage.setItem('authToken', token);
      localStorage.setItem('refreshToken', refreshTokenValue);

      return {
        token,
        refreshToken: refreshTokenValue,
        user,
      };
    } catch (error: any) {
      return rejectWithValue(error.message || 'Login failed');
    }
  }
);

export const logout = createAsyncThunk(
  'auth/logout',
  async (_, { rejectWithValue }) => {
    try {
      await apiService.logout();
      localStorage.removeItem('authToken');
      localStorage.removeItem('refreshToken');
    } catch (error: any) {
      // Even if logout fails on server, clear local storage
      localStorage.removeItem('authToken');
      localStorage.removeItem('refreshToken');
      return rejectWithValue(error.message || 'Logout failed');
    }
  }
);

export const getCurrentUser = createAsyncThunk(
  'auth/getCurrentUser',
  async (_, { rejectWithValue }) => {
    try {
      const response = await apiService.request({
        method: 'GET',
        url: '/api/v1/auth/me',
      });
      const data = response as any;
      return {
        id: data.id,
        username: data.username,
        email: data.email,
        firstName: data.firstName,
        lastName: data.lastName,
        roles: Array.isArray(data.roles) ? data.roles : Array.from(data.roles ?? []),
        permissions: Array.isArray(data.permissions) ? data.permissions : Array.from(data.permissions ?? []),
        lastLoginAt: data.lastLoginAt ?? null,
        isActive: Boolean(data.active),
      } as User;
    } catch (error: any) {
      return rejectWithValue(error.message || 'Failed to get current user');
    }
  }
);

export const refreshToken = createAsyncThunk<RefreshResult,
  void,
  { rejectValue: string; state: { auth: AuthState } }>(
  'auth/refreshToken',
  async (_, { rejectWithValue, getState }) => {
    try {
      const state = getState() as { auth: AuthState };
      const refreshToken = state.auth.refreshToken;

      if (!refreshToken) {
        throw new Error('No refresh token available');
      }
      
      const response = await apiService.request<any>({
        method: 'POST',
        url: '/api/v1/auth/refresh',
        data: { refreshToken },
      });

      const token = response?.accessToken ?? '';
      const newRefreshToken = response?.refreshToken ?? refreshToken;

      if (!token) {
        throw new Error('Token refresh response missing access token');
      }

      localStorage.setItem('authToken', token);
      localStorage.setItem('refreshToken', newRefreshToken ?? '');

      return {
        token,
        refreshToken: newRefreshToken,
      };
    } catch (error: any) {
      localStorage.removeItem('authToken');
      localStorage.removeItem('refreshToken');
      return rejectWithValue(error.message || 'Token refresh failed');
    }
  }
);

const authSlice = createSlice({
  name: 'auth',
  initialState,
  reducers: {
    clearError: (state) => {
      state.error = null;
    },
    setUser: (state, action: PayloadAction<User>) => {
      state.user = action.payload;
      state.isAuthenticated = true;
    },
    clearAuth: (state) => {
      state.isAuthenticated = false;
      state.user = null;
      state.token = null;
      state.refreshToken = null;
      state.error = null;
      localStorage.removeItem('authToken');
      localStorage.removeItem('refreshToken');
    },
    updateUserProfile: (state, action: PayloadAction<Partial<User>>) => {
      if (state.user) {
        state.user = { ...state.user, ...action.payload };
      }
    },
  },
  extraReducers: (builder) => {
    // Login
    builder
      .addCase(login.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(login.fulfilled, (state, action) => {
        state.loading = false;
        state.isAuthenticated = true;
        state.user = action.payload.user;
        state.token = action.payload.token;
        state.refreshToken = action.payload.refreshToken;
        state.error = null;
      })
      .addCase(login.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload as string;
        state.isAuthenticated = false;
        state.user = null;
        state.token = null;
        state.refreshToken = null;
      });

    // Logout
    builder
      .addCase(logout.pending, (state) => {
        state.loading = true;
      })
      .addCase(logout.fulfilled, (state) => {
        state.loading = false;
        state.isAuthenticated = false;
        state.user = null;
        state.token = null;
        state.refreshToken = null;
        state.error = null;
      })
      .addCase(logout.rejected, (state, action) => {
        state.loading = false;
        // Clear auth state even if server logout fails
        state.isAuthenticated = false;
        state.user = null;
        state.token = null;
        state.refreshToken = null;
        state.error = action.payload as string;
      });

    // Get current user
    builder
      .addCase(getCurrentUser.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(getCurrentUser.fulfilled, (state, action) => {
        state.loading = false;
        state.user = action.payload;
        state.isAuthenticated = true;
        state.error = null;
      })
      .addCase(getCurrentUser.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload as string;
        // Don't clear auth state here as token might still be valid
      });

    // Refresh token
    builder
      .addCase(refreshToken.pending, (state) => {
        state.loading = true;
      })
      .addCase(refreshToken.fulfilled, (state, action) => {
        state.loading = false;
        state.token = action.payload.token;
        state.refreshToken = action.payload.refreshToken;
        state.error = null;
      })
      .addCase(refreshToken.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload as string;
        state.isAuthenticated = false;
        state.user = null;
        state.token = null;
        state.refreshToken = null;
      });
  },
});

export const { clearError, setUser, clearAuth, updateUserProfile } = authSlice.actions;

// Selectors
export const selectAuth = (state: { auth: AuthState }) => state.auth;
export const selectIsAuthenticated = (state: { auth: AuthState }) => state.auth.isAuthenticated;
export const selectUser = (state: { auth: AuthState }) => state.auth.user;
export const selectAuthLoading = (state: { auth: AuthState }) => state.auth.loading;
export const selectAuthError = (state: { auth: AuthState }) => state.auth.error;

export default authSlice.reducer;