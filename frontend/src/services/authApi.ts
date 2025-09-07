import axios from 'axios';

const API_BASE_URL = process.env.REACT_APP_AUTH_SERVICE_URL || 'http://localhost:8080/api/v1/auth';

export interface LoginRequest {
  username: string;
  password: string;
}

export interface LoginResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresIn: number;
  userId: string;
  username: string;
  email: string;
  roles: string[];
  permissions: string[];
}

export interface UserRegistrationRequest {
  username: string;
  email: string;
  password: string;
  firstName?: string;
  lastName?: string;
}

export interface User {
  id: string;
  username: string;
  email: string;
  firstName?: string;
  lastName?: string;
  status: 'ACTIVE' | 'INACTIVE' | 'LOCKED' | 'SUSPENDED';
  lastLoginAt?: string;
  createdAt: string;
  updatedAt: string;
  roles: Role[];
}

export interface Role {
  id: string;
  name: string;
  description?: string;
  permissions: Permission[];
}

export interface Permission {
  id: string;
  name: string;
  description?: string;
  resourceType?: string;
  resourceId?: string;
}

export interface RefreshTokenRequest {
  refreshToken: string;
}

class AuthApiService {
  private api = axios.create({
    baseURL: API_BASE_URL,
    headers: {
      'Content-Type': 'application/json',
    },
  });

  constructor() {
    // Add request interceptor to include auth token
    this.api.interceptors.request.use(
      (config) => {
        const token = localStorage.getItem('accessToken');
        if (token) {
          config.headers.Authorization = `Bearer ${token}`;
        }
        return config;
      },
      (error) => {
        return Promise.reject(error);
      }
    );

    // Add response interceptor to handle token refresh
    this.api.interceptors.response.use(
      (response) => response,
      async (error) => {
        const originalRequest = error.config;
        
        if (error.response?.status === 401 && !originalRequest._retry) {
          originalRequest._retry = true;
          
          try {
            const refreshToken = localStorage.getItem('refreshToken');
            if (refreshToken) {
              const response = await this.refreshToken({ refreshToken });
              localStorage.setItem('accessToken', response.accessToken);
              localStorage.setItem('refreshToken', response.refreshToken);
              
              // Retry original request
              originalRequest.headers.Authorization = `Bearer ${response.accessToken}`;
              return this.api(originalRequest);
            }
          } catch (refreshError) {
            // Refresh failed, redirect to login
            this.logout();
            window.location.href = '/login';
          }
        }
        
        return Promise.reject(error);
      }
    );
  }

  async login(credentials: LoginRequest): Promise<LoginResponse> {
    const response = await this.api.post<LoginResponse>('/login', credentials);
    return response.data;
  }

  async register(userData: UserRegistrationRequest): Promise<User> {
    const response = await this.api.post<User>('/register', userData);
    return response.data;
  }

  async refreshToken(request: RefreshTokenRequest): Promise<LoginResponse> {
    const response = await this.api.post<LoginResponse>('/refresh', request);
    return response.data;
  }

  async logout(): Promise<void> {
    try {
      await this.api.post('/logout');
    } finally {
      localStorage.removeItem('accessToken');
      localStorage.removeItem('refreshToken');
      localStorage.removeItem('user');
    }
  }

  async validateToken(): Promise<boolean> {
    try {
      const response = await this.api.get<boolean>('/validate');
      return response.data;
    } catch (error) {
      return false;
    }
  }

  async getCurrentUser(): Promise<User> {
    const response = await this.api.get<User>('/user/me');
    return response.data;
  }

  async getUser(userId: string): Promise<User> {
    const response = await this.api.get<User>(`/user/${userId}`);
    return response.data;
  }

  async getUsers(): Promise<User[]> {
    const response = await this.api.get<User[]>('/users');
    return response.data;
  }

  async activateUser(userId: string): Promise<void> {
    await this.api.put(`/user/${userId}/activate`);
  }

  async deactivateUser(userId: string): Promise<void> {
    await this.api.put(`/user/${userId}/deactivate`);
  }

  async unlockUser(userId: string): Promise<void> {
    await this.api.put(`/user/${userId}/unlock`);
  }

  async changePassword(userId: string, oldPassword: string, newPassword: string): Promise<void> {
    await this.api.post(`/user/${userId}/change-password`, {
      oldPassword,
      newPassword,
    });
  }

  async healthCheck(): Promise<string> {
    const response = await this.api.get<string>('/health');
    return response.data;
  }

  // Utility methods
  isAuthenticated(): boolean {
    const token = localStorage.getItem('accessToken');
    return !!token;
  }

  getStoredUser(): User | null {
    const userStr = localStorage.getItem('user');
    return userStr ? JSON.parse(userStr) : null;
  }

  storeUser(user: User): void {
    localStorage.setItem('user', JSON.stringify(user));
  }

  getToken(): string | null {
    return localStorage.getItem('accessToken');
  }
}

export const authApi = new AuthApiService();
export default authApi;