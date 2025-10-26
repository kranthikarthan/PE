/**
 * AuthService Tests
 * 
 * Unit tests for the AuthService API client.
 * Tests authentication operations with mocked responses.
 */

import { http } from 'msw';
import { server } from '../../mocks/server';
import { AuthService } from '../authService';
import { fixtures } from '../../test-utils/fixtures';

describe('AuthService', () => {
  let authService: AuthService;

  beforeEach(() => {
    authService = new AuthService();
  });

  describe('login', () => {
    it('should successfully login with valid credentials', async () => {
      const credentials = {
        email: 'john.doe@example.com',
        password: 'password123',
      };

      const result = await authService.login(credentials);

      expect(result).toEqual(fixtures.authResponse);
      expect(result.token).toBe('mock-jwt-token');
      expect(result.user.email).toBe(credentials.email);
    });

    it('should throw error for invalid credentials', async () => {
      const credentials = {
        email: 'invalid@example.com',
        password: 'wrongpassword',
      };

      // Mock server to return error
      server.use(
        http.post('*/auth/login', (req, res, ctx) => {
          return res(
            ctx.status(401),
            ctx.json({ message: 'Invalid credentials' })
          );
        })
      );

      await expect(authService.login(credentials)).rejects.toThrow();
    });

    it('should handle network errors', async () => {
      const credentials = {
        email: 'john.doe@example.com',
        password: 'password123',
      };

      // Mock server to return network error
      server.use(
        http.post('*/auth/login', (req, res, ctx) => {
          return res.networkError('Network Error');
        })
      );

      await expect(authService.login(credentials)).rejects.toThrow();
    });
  });

  describe('logout', () => {
    it('should successfully logout', async () => {
      const result = await authService.logout();

      expect(result).toEqual({ message: 'Logged out successfully' });
    });

    it('should handle logout errors', async () => {
      // Mock server to return error
      server.use(
        http.post('*/auth/logout', (req, res, ctx) => {
          return res(
            ctx.status(500),
            ctx.json({ message: 'Internal server error' })
          );
        })
      );

      await expect(authService.logout()).rejects.toThrow();
    });
  });

  describe('getCurrentUser', () => {
    it('should return current user information', async () => {
      const result = await authService.getCurrentUser();

      expect(result).toEqual(fixtures.user);
      expect(result.email).toBe('john.doe@example.com');
    });

    it('should handle unauthorized access', async () => {
      // Mock server to return unauthorized
      server.use(
        http.get('*/auth/me', (req, res, ctx) => {
          return res(
            ctx.status(401),
            ctx.json({ message: 'Unauthorized' })
          );
        })
      );

      await expect(authService.getCurrentUser()).rejects.toThrow();
    });
  });

  describe('refreshToken', () => {
    it('should successfully refresh token', async () => {
      const result = await authService.refreshToken();

      expect(result).toEqual({
        token: 'new-jwt-token',
        expiresIn: 3600,
      });
    });

    it('should handle refresh token errors', async () => {
      // Mock server to return error
      server.use(
        http.post('*/auth/refresh', (req, res, ctx) => {
          return res(
            ctx.status(401),
            ctx.json({ message: 'Invalid refresh token' })
          );
        })
      );

      await expect(authService.refreshToken()).rejects.toThrow();
    });
  });

  describe('validateToken', () => {
    it('should validate valid token', async () => {
      const result = await authService.validateToken('valid-token');

      expect(result).toBe(true);
    });

    it('should reject invalid token', async () => {
      // Mock server to return error
      server.use(
        http.post('*/auth/validate', (req, res, ctx) => {
          return res(
            ctx.status(401),
            ctx.json({ message: 'Invalid token' })
          );
        })
      );

      const result = await authService.validateToken('invalid-token');

      expect(result).toBe(false);
    });
  });
});
