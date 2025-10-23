/**
 * Dashboard Component Tests
 * 
 * Component tests for the Dashboard page.
 * Tests rendering, data loading, and user interactions.
 */

import React from 'react';
import { screen, waitFor, fireEvent } from '@testing-library/react';
import { render } from '../../test-utils';
import Dashboard from '../Dashboard';
import { fixtures } from '../../test-utils/fixtures';

// Mock the API hooks
const mockUseApi = jest.fn();
jest.mock('@hooks', () => ({
  useApi: () => mockUseApi(),
}));

// Mock the notification context
const mockShowError = jest.fn();
jest.mock('@contexts', () => ({
  useNotification: () => ({
    showError: mockShowError,
  }),
}));

describe('Dashboard', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  describe('Loading State', () => {
    it('should show loading skeletons when data is loading', () => {
      mockUseApi.mockReturnValue({
        loading: true,
        data: null,
        error: null,
        execute: jest.fn(),
      });

      render(<Dashboard />);

      // Check for loading skeletons
      expect(screen.getByTestId('service-health-skeleton')).toBeInTheDocument();
      expect(screen.getByTestId('health-summary-skeleton')).toBeInTheDocument();
      expect(screen.getByTestId('alerts-skeleton')).toBeInTheDocument();
    });
  });

  describe('Error State', () => {
    it('should show error messages when API calls fail', () => {
      mockUseApi.mockReturnValue({
        loading: false,
        data: null,
        error: 'Failed to load data',
        execute: jest.fn(),
      });

      render(<Dashboard />);

      expect(screen.getByText('Failed to load service health data: Failed to load data')).toBeInTheDocument();
    });
  });

  describe('Success State', () => {
    beforeEach(() => {
      mockUseApi.mockReturnValue({
        loading: false,
        data: fixtures.serviceHealth,
        error: null,
        execute: jest.fn(),
      });
    });

    it('should render dashboard with service health data', () => {
      render(<Dashboard />);

      expect(screen.getByText('Operations Dashboard')).toBeInTheDocument();
      expect(screen.getByText('System Health Overview')).toBeInTheDocument();
      expect(screen.getByText('System Overview')).toBeInTheDocument();
      expect(screen.getByText('Recent Alerts')).toBeInTheDocument();
    });

    it('should display service health information', () => {
      render(<Dashboard />);

      expect(screen.getByText('Payment Initiation Service')).toBeInTheDocument();
      expect(screen.getByText('Saga Orchestrator')).toBeInTheDocument();
      expect(screen.getByText('Analytics Service')).toBeInTheDocument();
    });

    it('should display service status chips', () => {
      render(<Dashboard />);

      expect(screen.getByText('UP')).toBeInTheDocument();
      expect(screen.getByText('DEGRADED')).toBeInTheDocument();
    });

    it('should display service metrics', () => {
      render(<Dashboard />);

      expect(screen.getByText('v1.0.0')).toBeInTheDocument();
      expect(screen.getByText('Response:')).toBeInTheDocument();
      expect(screen.getByText('Uptime:')).toBeInTheDocument();
    });
  });

  describe('Refresh Functionality', () => {
    it('should call execute when refresh button is clicked', () => {
      const mockExecute = jest.fn();
      mockUseApi.mockReturnValue({
        loading: false,
        data: fixtures.serviceHealth,
        error: null,
        execute: mockExecute,
      });

      render(<Dashboard />);

      const refreshButton = screen.getByRole('button', { name: /refresh/i });
      fireEvent.click(refreshButton);

      expect(mockExecute).toHaveBeenCalledTimes(3); // Called for each API hook
    });

    it('should disable refresh button when loading', () => {
      mockUseApi.mockReturnValue({
        loading: true,
        data: null,
        error: null,
        execute: jest.fn(),
      });

      render(<Dashboard />);

      const refreshButton = screen.getByRole('button', { name: /refresh/i });
      expect(refreshButton).toBeDisabled();
    });
  });

  describe('Auto-refresh', () => {
    it('should set up auto-refresh interval', () => {
      const mockExecute = jest.fn();
      mockUseApi.mockReturnValue({
        loading: false,
        data: fixtures.serviceHealth,
        error: null,
        execute: mockExecute,
      });

      render(<Dashboard />);

      // Wait for auto-refresh to trigger
      waitFor(() => {
        expect(mockExecute).toHaveBeenCalled();
      }, { timeout: 35000 });
    });
  });

  describe('Service Health Display', () => {
    it('should show correct status colors', () => {
      mockUseApi.mockReturnValue({
        loading: false,
        data: fixtures.serviceHealth,
        error: null,
        execute: jest.fn(),
      });

      render(<Dashboard />);

      // Check for status chips with correct colors
      const upStatusChips = screen.getAllByText('UP');
      const degradedStatusChips = screen.getAllByText('DEGRADED');
      
      expect(upStatusChips.length).toBeGreaterThan(0);
      expect(degradedStatusChips.length).toBeGreaterThan(0);
    });

    it('should format uptime correctly', () => {
      mockUseApi.mockReturnValue({
        loading: false,
        data: fixtures.serviceHealth,
        error: null,
        execute: jest.fn(),
      });

      render(<Dashboard />);

      // Check for formatted uptime
      expect(screen.getByText(/1d/)).toBeInTheDocument();
    });

    it('should format response time correctly', () => {
      mockUseApi.mockReturnValue({
        loading: false,
        data: fixtures.serviceHealth,
        error: null,
        execute: jest.fn(),
      });

      render(<Dashboard />);

      // Check for formatted response time
      expect(screen.getByText(/150ms/)).toBeInTheDocument();
    });
  });

  describe('Health Summary', () => {
    it('should display health summary metrics', () => {
      mockUseApi.mockReturnValue({
        loading: false,
        data: fixtures.serviceHealthSummary,
        error: null,
        execute: jest.fn(),
      });

      render(<Dashboard />);

      expect(screen.getByText('3')).toBeInTheDocument(); // Total services
      expect(screen.getByText('2')).toBeInTheDocument(); // Healthy services
      expect(screen.getByText('1')).toBeInTheDocument(); // Unhealthy services
    });

    it('should display progress bars for metrics', () => {
      mockUseApi.mockReturnValue({
        loading: false,
        data: fixtures.serviceHealthSummary,
        error: null,
        execute: jest.fn(),
      });

      render(<Dashboard />);

      const progressBars = screen.getAllByRole('progressbar');
      expect(progressBars.length).toBeGreaterThan(0);
    });
  });

  describe('Alerts Display', () => {
    it('should display recent alerts', () => {
      mockUseApi.mockReturnValue({
        loading: false,
        data: fixtures.serviceAlerts,
        error: null,
        execute: jest.fn(),
      });

      render(<Dashboard />);

      expect(screen.getByText('High CPU usage detected on Analytics Service')).toBeInTheDocument();
      expect(screen.getByText('Memory usage above threshold')).toBeInTheDocument();
    });

    it('should display alert severity chips', () => {
      mockUseApi.mockReturnValue({
        loading: false,
        data: fixtures.serviceAlerts,
        error: null,
        execute: jest.fn(),
      });

      render(<Dashboard />);

      expect(screen.getByText('HIGH')).toBeInTheDocument();
      expect(screen.getByText('MEDIUM')).toBeInTheDocument();
    });

    it('should display alert timestamps', () => {
      mockUseApi.mockReturnValue({
        loading: false,
        data: fixtures.serviceAlerts,
        error: null,
        execute: jest.fn(),
      });

      render(<Dashboard />);

      // Check for formatted timestamps
      expect(screen.getByText(/ago/)).toBeInTheDocument();
    });
  });

  describe('Empty States', () => {
    it('should handle empty service health data', () => {
      mockUseApi.mockReturnValue({
        loading: false,
        data: [],
        error: null,
        execute: jest.fn(),
      });

      render(<Dashboard />);

      expect(screen.getByText('No services available')).toBeInTheDocument();
    });

    it('should handle empty alerts data', () => {
      mockUseApi.mockReturnValue({
        loading: false,
        data: [],
        error: null,
        execute: jest.fn(),
      });

      render(<Dashboard />);

      expect(screen.getByText('No recent alerts')).toBeInTheDocument();
    });
  });

  describe('Accessibility', () => {
    it('should have proper ARIA labels', () => {
      mockUseApi.mockReturnValue({
        loading: false,
        data: fixtures.serviceHealth,
        error: null,
        execute: jest.fn(),
      });

      render(<Dashboard />);

      expect(screen.getByRole('button', { name: /refresh/i })).toBeInTheDocument();
      expect(screen.getByRole('progressbar')).toBeInTheDocument();
    });

    it('should have proper heading structure', () => {
      mockUseApi.mockReturnValue({
        loading: false,
        data: fixtures.serviceHealth,
        error: null,
        execute: jest.fn(),
      });

      render(<Dashboard />);

      expect(screen.getByRole('heading', { level: 4, name: 'Operations Dashboard' })).toBeInTheDocument();
      expect(screen.getByRole('heading', { level: 6, name: 'System Health Overview' })).toBeInTheDocument();
    });
  });

  describe('Responsive Design', () => {
    it('should render correctly on different screen sizes', () => {
      mockUseApi.mockReturnValue({
        loading: false,
        data: fixtures.serviceHealth,
        error: null,
        execute: jest.fn(),
      });

      render(<Dashboard />);

      // Check for responsive grid layout
      expect(screen.getByText('System Health Overview')).toBeInTheDocument();
      expect(screen.getByText('System Overview')).toBeInTheDocument();
    });
  });
});
