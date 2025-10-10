import { authApi, LoginRequest, UserRegistrationRequest } from '../../services/authApi';
import axios from 'axios';

// Mock axios
jest.mock('axios');
const mockedAxios = axios as jest.Mocked<typeof axios>;

// Mock localStorage
const localStorageMock = {
  getItem: jest.fn(),
  setItem: jest.fn(),
  removeItem: jest.fn(),
  clear: jest.fn(),
};
Object.defineProperty(window, 'localStorage', {
  value: localStorageMock,
});

describe('AuthApiService', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    localStorageMock.getItem.mockReturnValue(null);
  });

  describe('login', () => {
    it('should login successfully and store tokens', async () => {
      const loginRequest: LoginRequest = {
        username: 'testuser',
        password: 'password123',
      };

      const mockResponse = {
        data: {
          accessToken: 'access-token',
          refreshToken: 'refresh-token',
          tokenType: 'Bearer',
          expiresIn: 3600,
          userId: 'user-id',
          username: 'testuser',
          email: 'test@example.com',
          roles: ['USER'],
          permissions: ['read'],
        },
      };

      mockedAxios.create.mockReturnValue({
        ...mockedAxios,
        post: jest.fn().mockResolvedValue(mockResponse),
        interceptors: {
          request: { use: jest.fn() },
          response: { use: jest.fn() },
        },
      } as any);

      const result = await authApi.login(loginRequest);

      expect(result).toEqual(mockResponse.data);
      expect(localStorageMock.setItem).toHaveBeenCalledWith('authToken', 'access-token');
      expect(localStorageMock.setItem).toHaveBeenCalledWith('refreshToken', 'refresh-token');
    });

    it('should handle login failure', async () => {
      const loginRequest: LoginRequest = {
        username: 'testuser',
        password: 'wrongpassword',
      };

      const mockError = {
        response: {
          data: {
            message: 'Invalid credentials',
          },
        },
      };

      mockedAxios.create.mockReturnValue({
        ...mockedAxios,
        post: jest.fn().mockRejectedValue(mockError),
        interceptors: {
          request: { use: jest.fn() },
          response: { use: jest.fn() },
        },
      } as any);

      await expect(authApi.login(loginRequest)).rejects.toEqual(mockError);
    });
  });

  describe('register', () => {
    it('should register user successfully', async () => {
      const registrationRequest: UserRegistrationRequest = {
        username: 'newuser',
        email: 'new@example.com',
        password: 'password123',
        firstName: 'New',
        lastName: 'User',
      };

      const mockResponse = {
        data: {
          id: 'user-id',
          username: 'newuser',
          email: 'new@example.com',
          firstName: 'New',
          lastName: 'User',
          status: 'ACTIVE',
          createdAt: '2023-01-01T00:00:00Z',
          updatedAt: '2023-01-01T00:00:00Z',
          roles: [],
        },
      };

      mockedAxios.create.mockReturnValue({
        ...mockedAxios,
        post: jest.fn().mockResolvedValue(mockResponse),
        interceptors: {
          request: { use: jest.fn() },
          response: { use: jest.fn() },
        },
      } as any);

      const result = await authApi.register(registrationRequest);

      expect(result).toEqual(mockResponse.data);
    });
  });

  describe('refreshToken', () => {
    it('should refresh token successfully', async () => {
      const refreshTokenRequest = {
        refreshToken: 'refresh-token',
      };

      const mockResponse = {
        data: {
          accessToken: 'new-access-token',
          refreshToken: 'new-refresh-token',
          tokenType: 'Bearer',
          expiresIn: 3600,
          userId: 'user-id',
          username: 'testuser',
          email: 'test@example.com',
          roles: ['USER'],
          permissions: ['read'],
        },
      };

      mockedAxios.create.mockReturnValue({
        ...mockedAxios,
        post: jest.fn().mockResolvedValue(mockResponse),
        interceptors: {
          request: { use: jest.fn() },
          response: { use: jest.fn() },
        },
      } as any);

      const result = await authApi.refreshToken(refreshTokenRequest);

      expect(result).toEqual(mockResponse.data);
    });
  });

  describe('logout', () => {
    it('should logout successfully and clear tokens', async () => {
      mockedAxios.create.mockReturnValue({
        ...mockedAxios,
        post: jest.fn().mockResolvedValue({ data: {} }),
        interceptors: {
          request: { use: jest.fn() },
          response: { use: jest.fn() },
        },
      } as any);

      await authApi.logout();

      expect(localStorageMock.removeItem).toHaveBeenCalledWith('authToken');
      expect(localStorageMock.removeItem).toHaveBeenCalledWith('refreshToken');
      expect(localStorageMock.removeItem).toHaveBeenCalledWith('user');
    });

    it('should clear tokens even if logout request fails', async () => {
      mockedAxios.create.mockReturnValue({
        ...mockedAxios,
        post: jest.fn().mockRejectedValue(new Error('Network error')),
        interceptors: {
          request: { use: jest.fn() },
          response: { use: jest.fn() },
        },
      } as any);

      await authApi.logout();

      expect(localStorageMock.removeItem).toHaveBeenCalledWith('authToken');
      expect(localStorageMock.removeItem).toHaveBeenCalledWith('refreshToken');
      expect(localStorageMock.removeItem).toHaveBeenCalledWith('user');
    });
  });

  describe('validateToken', () => {
    it('should validate token successfully', async () => {
      const mockResponse = {
        data: true,
      };

      mockedAxios.create.mockReturnValue({
        ...mockedAxios,
        get: jest.fn().mockResolvedValue(mockResponse),
        interceptors: {
          request: { use: jest.fn() },
          response: { use: jest.fn() },
        },
      } as any);

      const result = await authApi.validateToken();

      expect(result).toBe(true);
    });

    it('should return false on validation failure', async () => {
      mockedAxios.create.mockReturnValue({
        ...mockedAxios,
        get: jest.fn().mockRejectedValue(new Error('Invalid token')),
        interceptors: {
          request: { use: jest.fn() },
          response: { use: jest.fn() },
        },
      } as any);

      const result = await authApi.validateToken();

      expect(result).toBe(false);
    });
  });

  describe('getCurrentUser', () => {
    it('should get current user successfully', async () => {
      const mockResponse = {
        data: {
          id: 'user-id',
          username: 'testuser',
          email: 'test@example.com',
          firstName: 'Test',
          lastName: 'User',
          status: 'ACTIVE',
          createdAt: '2023-01-01T00:00:00Z',
          updatedAt: '2023-01-01T00:00:00Z',
          roles: [],
        },
      };

      mockedAxios.create.mockReturnValue({
        ...mockedAxios,
        get: jest.fn().mockResolvedValue(mockResponse),
        interceptors: {
          request: { use: jest.fn() },
          response: { use: jest.fn() },
        },
      } as any);

      const result = await authApi.getCurrentUser();

      expect(result).toEqual(mockResponse.data);
    });
  });

  describe('getUser', () => {
    it('should get user by ID successfully', async () => {
      const userId = 'user-id';
      const mockResponse = {
        data: {
          id: userId,
          username: 'testuser',
          email: 'test@example.com',
          firstName: 'Test',
          lastName: 'User',
          status: 'ACTIVE',
          createdAt: '2023-01-01T00:00:00Z',
          updatedAt: '2023-01-01T00:00:00Z',
          roles: [],
        },
      };

      mockedAxios.create.mockReturnValue({
        ...mockedAxios,
        get: jest.fn().mockResolvedValue(mockResponse),
        interceptors: {
          request: { use: jest.fn() },
          response: { use: jest.fn() },
        },
      } as any);

      const result = await authApi.getUser(userId);

      expect(result).toEqual(mockResponse.data);
    });
  });

  describe('getUsers', () => {
    it('should get all users successfully', async () => {
      const mockResponse = {
        data: [
          {
            id: 'user-id-1',
            username: 'user1',
            email: 'user1@example.com',
            firstName: 'User',
            lastName: 'One',
            status: 'ACTIVE',
            createdAt: '2023-01-01T00:00:00Z',
            updatedAt: '2023-01-01T00:00:00Z',
            roles: [],
          },
          {
            id: 'user-id-2',
            username: 'user2',
            email: 'user2@example.com',
            firstName: 'User',
            lastName: 'Two',
            status: 'ACTIVE',
            createdAt: '2023-01-01T00:00:00Z',
            updatedAt: '2023-01-01T00:00:00Z',
            roles: [],
          },
        ],
      };

      mockedAxios.create.mockReturnValue({
        ...mockedAxios,
        get: jest.fn().mockResolvedValue(mockResponse),
        interceptors: {
          request: { use: jest.fn() },
          response: { use: jest.fn() },
        },
      } as any);

      const result = await authApi.getUsers();

      expect(result).toEqual(mockResponse.data);
      expect(result).toHaveLength(2);
    });
  });

  describe('activateUser', () => {
    it('should activate user successfully', async () => {
      const userId = 'user-id';

      mockedAxios.create.mockReturnValue({
        ...mockedAxios,
        put: jest.fn().mockResolvedValue({ data: {} }),
        interceptors: {
          request: { use: jest.fn() },
          response: { use: jest.fn() },
        },
      } as any);

      await authApi.activateUser(userId);

      expect(mockedAxios.create().put).toHaveBeenCalledWith(`/user/${userId}/activate`);
    });
  });

  describe('deactivateUser', () => {
    it('should deactivate user successfully', async () => {
      const userId = 'user-id';

      mockedAxios.create.mockReturnValue({
        ...mockedAxios,
        put: jest.fn().mockResolvedValue({ data: {} }),
        interceptors: {
          request: { use: jest.fn() },
          response: { use: jest.fn() },
        },
      } as any);

      await authApi.deactivateUser(userId);

      expect(mockedAxios.create().put).toHaveBeenCalledWith(`/user/${userId}/deactivate`);
    });
  });

  describe('unlockUser', () => {
    it('should unlock user successfully', async () => {
      const userId = 'user-id';

      mockedAxios.create.mockReturnValue({
        ...mockedAxios,
        put: jest.fn().mockResolvedValue({ data: {} }),
        interceptors: {
          request: { use: jest.fn() },
          response: { use: jest.fn() },
        },
      } as any);

      await authApi.unlockUser(userId);

      expect(mockedAxios.create().put).toHaveBeenCalledWith(`/user/${userId}/unlock`);
    });
  });

  describe('changePassword', () => {
    it('should change password successfully', async () => {
      const userId = 'user-id';
      const oldPassword = 'oldpassword';
      const newPassword = 'newpassword';

      mockedAxios.create.mockReturnValue({
        ...mockedAxios,
        post: jest.fn().mockResolvedValue({ data: {} }),
        interceptors: {
          request: { use: jest.fn() },
          response: { use: jest.fn() },
        },
      } as any);

      await authApi.changePassword(userId, oldPassword, newPassword);

      expect(mockedAxios.create().post).toHaveBeenCalledWith(`/user/${userId}/change-password`, {
        oldPassword,
        newPassword,
      });
    });
  });

  describe('healthCheck', () => {
    it('should perform health check successfully', async () => {
      const mockResponse = {
        data: 'Auth Service is healthy',
      };

      mockedAxios.create.mockReturnValue({
        ...mockedAxios,
        get: jest.fn().mockResolvedValue(mockResponse),
        interceptors: {
          request: { use: jest.fn() },
          response: { use: jest.fn() },
        },
      } as any);

      const result = await authApi.healthCheck();

      expect(result).toBe('Auth Service is healthy');
    });
  });

  describe('utility methods', () => {
    it('should check if user is authenticated', () => {
      localStorageMock.getItem.mockReturnValue('access-token');

      const result = authApi.isAuthenticated();

      expect(result).toBe(true);
      expect(localStorageMock.getItem).toHaveBeenCalledWith('authToken');
    });

    it('should return false when not authenticated', () => {
      localStorageMock.getItem.mockReturnValue(null);

      const result = authApi.isAuthenticated();

      expect(result).toBe(false);
    });

    it('should get stored user', () => {
      const user = { id: 'user-id', username: 'testuser' };
      localStorageMock.getItem.mockReturnValue(JSON.stringify(user));

      const result = authApi.getStoredUser();

      expect(result).toEqual(user);
    });

    it('should return null when no user stored', () => {
      localStorageMock.getItem.mockReturnValue(null);

      const result = authApi.getStoredUser();

      expect(result).toBeNull();
    });

    it('should store user', () => {
      const user = { id: 'user-id', username: 'testuser' };

      authApi.storeUser(user);

      expect(localStorageMock.setItem).toHaveBeenCalledWith('user', JSON.stringify(user));
    });

    it('should get token', () => {
      localStorageMock.getItem.mockReturnValue('access-token');

      const result = authApi.getToken();

      expect(result).toBe('access-token');
      expect(localStorageMock.getItem).toHaveBeenCalledWith('authToken');
    });
  });
});