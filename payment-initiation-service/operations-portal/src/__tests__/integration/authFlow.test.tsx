/**
 * Authentication Flow Integration Tests
 * 
 * Integration tests for the complete authentication flow.
 * Tests login, logout, token management, and route protection.
 */

import React from 'react';
import { screen, fireEvent, waitFor } from '@testing-library/react';
import { render } from '../../test-utils';
import App from '../../App';
import { fixtures } from '../../test-utils/fixtures';

// Mock the API services
jest.mock('../../services', () => ({
  getAuthService: () => ({
    login: jest.fn().mockResolvedValue(fixtures.authResponse),
    logout: jest.fn().mockResolvedValue({ message: 'Logged out successfully' }),
    getCurrentUser: jest.fn().mockResolvedValue(fixtures.user),
  }),
}));

describe('Authentication Flow Integration', () => {
  beforeEach(() => {
    // Clear any existing auth state
    localStorage.clear();
    sessionStorage.clear();
  });

  describe('Login Flow', () => {
    it('should redirect to login page when not authenticated', () => {
      render(<App />);

      expect(screen.getByText('Sign In to Payments Engine')).toBeInTheDocument();
      expect(screen.getByLabelText('Email Address')).toBeInTheDocument();
      expect(screen.getByLabelText('Password')).toBeInTheDocument();
    });

    it('should successfully login with valid credentials', async () => {
      render(<App />);

      const emailInput = screen.getByLabelText('Email Address');
      const passwordInput = screen.getByLabelText('Password');
      const loginButton = screen.getByRole('button', { name: /sign in/i });

      fireEvent.change(emailInput, { target: { value: 'john.doe@example.com' } });
      fireEvent.change(passwordInput, { target: { value: 'password123' } });
      fireEvent.click(loginButton);

      await waitFor(() => {
        expect(screen.getByText('Operations Dashboard')).toBeInTheDocument();
      });

      expect(screen.getByText('Operations Dashboard')).toBeInTheDocument();
      expect(screen.getByText('System Health Overview')).toBeInTheDocument();
    });

    it('should show error message for invalid credentials', async () => {
      // Mock failed login
      const mockAuthService = {
        login: jest.fn().mockRejectedValue(new Error('Invalid credentials')),
        logout: jest.fn(),
        getCurrentUser: jest.fn(),
      };

      jest.doMock('../../services', () => ({
        getAuthService: () => mockAuthService,
      }));

      render(<App />);

      const emailInput = screen.getByLabelText('Email Address');
      const passwordInput = screen.getByLabelText('Password');
      const loginButton = screen.getByRole('button', { name: /sign in/i });

      fireEvent.change(emailInput, { target: { value: 'invalid@example.com' } });
      fireEvent.change(passwordInput, { target: { value: 'wrongpassword' } });
      fireEvent.click(loginButton);

      await waitFor(() => {
        expect(screen.getByText(/invalid credentials/i)).toBeInTheDocument();
      });
    });

    it('should show loading state during login', async () => {
      render(<App />);

      const emailInput = screen.getByLabelText('Email Address');
      const passwordInput = screen.getByLabelText('Password');
      const loginButton = screen.getByRole('button', { name: /sign in/i });

      fireEvent.change(emailInput, { target: { value: 'john.doe@example.com' } });
      fireEvent.change(passwordInput, { target: { value: 'password123' } });
      fireEvent.click(loginButton);

      // Check for loading state
      expect(screen.getByRole('button', { name: /sign in/i })).toBeDisabled();
    });
  });

  describe('Logout Flow', () => {
    beforeEach(async () => {
      // Set up authenticated state
      localStorage.setItem('auth_token', 'mock-jwt-token');
      localStorage.setItem('user', JSON.stringify(fixtures.user));
    });

    it('should successfully logout and redirect to login', async () => {
      render(<App />);

      // Wait for dashboard to load
      await waitFor(() => {
        expect(screen.getByText('Operations Dashboard')).toBeInTheDocument();
      });

      // Click on user menu
      const userMenuButton = screen.getByRole('button', { name: /user menu/i });
      fireEvent.click(userMenuButton);

      // Click logout
      const logoutButton = screen.getByText('Logout');
      fireEvent.click(logoutButton);

      await waitFor(() => {
        expect(screen.getByText('Sign In to Payments Engine')).toBeInTheDocument();
      });
    });

    it('should clear auth state on logout', async () => {
      render(<App />);

      // Wait for dashboard to load
      await waitFor(() => {
        expect(screen.getByText('Operations Dashboard')).toBeInTheDocument();
      });

      // Click on user menu
      const userMenuButton = screen.getByRole('button', { name: /user menu/i });
      fireEvent.click(userMenuButton);

      // Click logout
      const logoutButton = screen.getByText('Logout');
      fireEvent.click(logoutButton);

      await waitFor(() => {
        expect(localStorage.getItem('auth_token')).toBeNull();
        expect(localStorage.getItem('user')).toBeNull();
      });
    });
  });

  describe('Route Protection', () => {
    it('should protect dashboard route when not authenticated', () => {
      render(<App />);

      // Try to navigate to dashboard
      window.history.pushState({}, '', '/');

      expect(screen.getByText('Sign In to Payments Engine')).toBeInTheDocument();
    });

    it('should allow access to dashboard when authenticated', async () => {
      // Set up authenticated state
      localStorage.setItem('auth_token', 'mock-jwt-token');
      localStorage.setItem('user', JSON.stringify(fixtures.user));

      render(<App />);

      await waitFor(() => {
        expect(screen.getByText('Operations Dashboard')).toBeInTheDocument();
      });
    });

    it('should protect service management route when not authenticated', () => {
      render(<App />);

      // Try to navigate to service management
      window.history.pushState({}, '', '/services');

      expect(screen.getByText('Sign In to Payments Engine')).toBeInTheDocument();
    });

    it('should allow access to service management when authenticated', async () => {
      // Set up authenticated state
      localStorage.setItem('auth_token', 'mock-jwt-token');
      localStorage.setItem('user', JSON.stringify(fixtures.user));

      render(<App />);

      // Navigate to service management
      window.history.pushState({}, '', '/services');

      await waitFor(() => {
        expect(screen.getByText('Service Management')).toBeInTheDocument();
      });
    });
  });

  describe('Token Management', () => {
    it('should store token in localStorage on successful login', async () => {
      render(<App />);

      const emailInput = screen.getByLabelText('Email Address');
      const passwordInput = screen.getByLabelText('Password');
      const loginButton = screen.getByRole('button', { name: /sign in/i });

      fireEvent.change(emailInput, { target: { value: 'john.doe@example.com' } });
      fireEvent.change(passwordInput, { target: { value: 'password123' } });
      fireEvent.click(loginButton);

      await waitFor(() => {
        expect(localStorage.getItem('auth_token')).toBe('mock-jwt-token');
      });
    });

    it('should store user data in localStorage on successful login', async () => {
      render(<App />);

      const emailInput = screen.getByLabelText('Email Address');
      const passwordInput = screen.getByLabelText('Password');
      const loginButton = screen.getByRole('button', { name: /sign in/i });

      fireEvent.change(emailInput, { target: { value: 'john.doe@example.com' } });
      fireEvent.change(passwordInput, { target: { value: 'password123' } });
      fireEvent.click(loginButton);

      await waitFor(() => {
        const userData = JSON.parse(localStorage.getItem('user') || '{}');
        expect(userData.email).toBe('john.doe@example.com');
      });
    });

    it('should handle token expiration', async () => {
      // Set up expired token
      localStorage.setItem('auth_token', 'expired-token');
      localStorage.setItem('user', JSON.stringify(fixtures.user));

      render(<App />);

      // Mock token validation failure
      const mockAuthService = {
        login: jest.fn(),
        logout: jest.fn(),
        getCurrentUser: jest.fn().mockRejectedValue(new Error('Token expired')),
      };

      jest.doMock('../../services', () => ({
        getAuthService: () => mockAuthService,
      }));

      await waitFor(() => {
        expect(screen.getByText('Sign In to Payments Engine')).toBeInTheDocument();
      });
    });
  });

  describe('User Context', () => {
    it('should display user information in navbar when authenticated', async () => {
      // Set up authenticated state
      localStorage.setItem('auth_token', 'mock-jwt-token');
      localStorage.setItem('user', JSON.stringify(fixtures.user));

      render(<App />);

      await waitFor(() => {
        expect(screen.getByText('John Doe')).toBeInTheDocument();
        expect(screen.getByText('john.doe@example.com')).toBeInTheDocument();
      });
    });

    it('should display tenant information in navbar', async () => {
      // Set up authenticated state
      localStorage.setItem('auth_token', 'mock-jwt-token');
      localStorage.setItem('user', JSON.stringify(fixtures.user));

      render(<App />);

      await waitFor(() => {
        expect(screen.getByText('tenant-1')).toBeInTheDocument();
        expect(screen.getByText('bu-1')).toBeInTheDocument();
      });
    });
  });

  describe('Session Persistence', () => {
    it('should maintain session across page refreshes', async () => {
      // Set up authenticated state
      localStorage.setItem('auth_token', 'mock-jwt-token');
      localStorage.setItem('user', JSON.stringify(fixtures.user));

      render(<App />);

      await waitFor(() => {
        expect(screen.getByText('Operations Dashboard')).toBeInTheDocument();
      });

      // Simulate page refresh
      window.location.reload();

      await waitFor(() => {
        expect(screen.getByText('Operations Dashboard')).toBeInTheDocument();
      });
    });

    it('should redirect to login when session is invalid', async () => {
      // Set up invalid session
      localStorage.setItem('auth_token', 'invalid-token');
      localStorage.setItem('user', JSON.stringify(fixtures.user));

      render(<App />);

      await waitFor(() => {
        expect(screen.getByText('Sign In to Payments Engine')).toBeInTheDocument();
      });
    });
  });

  describe('Navigation', () => {
    beforeEach(async () => {
      // Set up authenticated state
      localStorage.setItem('auth_token', 'mock-jwt-token');
      localStorage.setItem('user', JSON.stringify(fixtures.user));
    });

    it('should navigate between protected routes', async () => {
      render(<App />);

      await waitFor(() => {
        expect(screen.getByText('Operations Dashboard')).toBeInTheDocument();
      });

      // Navigate to service management
      const serviceManagementLink = screen.getByText('Service Management');
      fireEvent.click(serviceManagementLink);

      await waitFor(() => {
        expect(screen.getByText('Service Management')).toBeInTheDocument();
      });

      // Navigate to payment repair
      const paymentRepairLink = screen.getByText('Payment Repair');
      fireEvent.click(paymentRepairLink);

      await waitFor(() => {
        expect(screen.getByText('Payment Repair')).toBeInTheDocument();
      });
    });

    it('should highlight active route in navigation', async () => {
      render(<App />);

      await waitFor(() => {
        expect(screen.getByText('Operations Dashboard')).toBeInTheDocument();
      });

      // Check if dashboard link is highlighted
      const dashboardLink = screen.getByText('Dashboard');
      expect(dashboardLink.closest('a')).toHaveClass('active');
    });
  });
});
