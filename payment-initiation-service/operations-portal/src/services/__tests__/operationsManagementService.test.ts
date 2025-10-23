/**
 * OperationsManagementService Tests
 * 
 * Unit tests for the OperationsManagementService API client.
 * Tests service health, circuit breakers, and feature flags operations.
 */

import { rest } from 'msw';
import { server } from '../../mocks/server';
import { OperationsManagementService } from '../operationsManagementService';
import { fixtures } from '../../test-utils/fixtures';

describe('OperationsManagementService', () => {
  let opsService: OperationsManagementService;

  beforeEach(() => {
    opsService = new OperationsManagementService();
  });

  describe('getAllServicesHealth', () => {
    it('should return all services health data', async () => {
      const result = await opsService.getAllServicesHealth();

      expect(result).toEqual(fixtures.serviceHealth);
      expect(result).toHaveLength(3);
      expect(result[0].name).toBe('Payment Initiation Service');
    });

    it('should handle API errors', async () => {
      // Mock server to return error
      server.use(
        rest.get('*/ops/v1/services/health', (req, res, ctx) => {
          return res(
            ctx.status(500),
            ctx.json({ message: 'Internal server error' })
          );
        })
      );

      await expect(opsService.getAllServicesHealth()).rejects.toThrow();
    });
  });

  describe('getServiceHealthSummary', () => {
    it('should return service health summary', async () => {
      const result = await opsService.getServiceHealthSummary();

      expect(result).toEqual(fixtures.serviceHealthSummary);
      expect(result.totalServices).toBe(3);
      expect(result.healthyServices).toBe(2);
    });

    it('should handle summary API errors', async () => {
      // Mock server to return error
      server.use(
        rest.get('*/ops/v1/services/health/summary', (req, res, ctx) => {
          return res(
            ctx.status(500),
            ctx.json({ message: 'Internal server error' })
          );
        })
      );

      await expect(opsService.getServiceHealthSummary()).rejects.toThrow();
    });
  });

  describe('getServiceAlerts', () => {
    it('should return service alerts', async () => {
      const result = await opsService.getServiceAlerts();

      expect(result).toEqual(fixtures.serviceAlerts);
      expect(result).toHaveLength(2);
      expect(result[0].severity).toBe('HIGH');
    });

    it('should handle alerts API errors', async () => {
      // Mock server to return error
      server.use(
        rest.get('*/ops/v1/services/alerts', (req, res, ctx) => {
          return res(
            ctx.status(500),
            ctx.json({ message: 'Internal server error' })
          );
        })
      );

      await expect(opsService.getServiceAlerts()).rejects.toThrow();
    });
  });

  describe('getAllCircuitBreakers', () => {
    it('should return all circuit breakers', async () => {
      const result = await opsService.getAllCircuitBreakers();

      expect(result).toHaveLength(2);
      expect(result[0].name).toBe('payment-service');
      expect(result[0].state).toBe('CLOSED');
    });

    it('should handle circuit breaker API errors', async () => {
      // Mock server to return error
      server.use(
        rest.get('*/ops/v1/circuit-breakers', (req, res, ctx) => {
          return res(
            ctx.status(500),
            ctx.json({ message: 'Internal server error' })
          );
        })
      );

      await expect(opsService.getAllCircuitBreakers()).rejects.toThrow();
    });
  });

  describe('openCircuitBreaker', () => {
    it('should successfully open circuit breaker', async () => {
      const result = await opsService.openCircuitBreaker('payment-service', 'Manual intervention');

      expect(result).toEqual({ message: 'Circuit breaker opened successfully' });
    });

    it('should handle circuit breaker open errors', async () => {
      // Mock server to return error
      server.use(
        rest.post('*/ops/v1/circuit-breakers/payment-service/open', (req, res, ctx) => {
          return res(
            ctx.status(500),
            ctx.json({ message: 'Failed to open circuit breaker' })
          );
        })
      );

      await expect(
        opsService.openCircuitBreaker('payment-service', 'Manual intervention')
      ).rejects.toThrow();
    });
  });

  describe('closeCircuitBreaker', () => {
    it('should successfully close circuit breaker', async () => {
      const result = await opsService.closeCircuitBreaker('payment-service', 'Issue resolved');

      expect(result).toEqual({ message: 'Circuit breaker closed successfully' });
    });

    it('should handle circuit breaker close errors', async () => {
      // Mock server to return error
      server.use(
        rest.post('*/ops/v1/circuit-breakers/payment-service/close', (req, res, ctx) => {
          return res(
            ctx.status(500),
            ctx.json({ message: 'Failed to close circuit breaker' })
          );
        })
      );

      await expect(
        opsService.closeCircuitBreaker('payment-service', 'Issue resolved')
      ).rejects.toThrow();
    });
  });

  describe('getAllFeatureFlags', () => {
    it('should return all feature flags', async () => {
      const result = await opsService.getAllFeatureFlags();

      expect(result).toHaveLength(2);
      expect(result[0].name).toBe('ENABLE_NEW_UI');
      expect(result[0].enabled).toBe(true);
    });

    it('should handle feature flags API errors', async () => {
      // Mock server to return error
      server.use(
        rest.get('*/ops/v1/feature-flags', (req, res, ctx) => {
          return res(
            ctx.status(500),
            ctx.json({ message: 'Internal server error' })
          );
        })
      );

      await expect(opsService.getAllFeatureFlags()).rejects.toThrow();
    });
  });

  describe('toggleFeatureFlag', () => {
    it('should successfully toggle feature flag', async () => {
      const result = await opsService.toggleFeatureFlag('ENABLE_NEW_UI', false);

      expect(result).toEqual({ message: 'Feature flag updated successfully' });
    });

    it('should handle feature flag toggle errors', async () => {
      // Mock server to return error
      server.use(
        rest.put('*/ops/v1/feature-flags/ENABLE_NEW_UI', (req, res, ctx) => {
          return res(
            ctx.status(500),
            ctx.json({ message: 'Failed to update feature flag' })
          );
        })
      );

      await expect(
        opsService.toggleFeatureFlag('ENABLE_NEW_UI', false)
      ).rejects.toThrow();
    });
  });

  describe('getAllPods', () => {
    it('should return all Kubernetes pods', async () => {
      const result = await opsService.getAllPods();

      expect(result).toHaveLength(2);
      expect(result[0].name).toBe('payment-service-pod-1');
      expect(result[0].status).toBe('Running');
    });

    it('should handle pods API errors', async () => {
      // Mock server to return error
      server.use(
        rest.get('*/ops/v1/pods', (req, res, ctx) => {
          return res(
            ctx.status(500),
            ctx.json({ message: 'Internal server error' })
          );
        })
      );

      await expect(opsService.getAllPods()).rejects.toThrow();
    });
  });

  describe('restartPod', () => {
    it('should successfully restart pod', async () => {
      const result = await opsService.restartPod('payment-service-pod-1', 'Scheduled maintenance');

      expect(result).toEqual({ message: 'Pod restart initiated successfully' });
    });

    it('should handle pod restart errors', async () => {
      // Mock server to return error
      server.use(
        rest.post('*/ops/v1/pods/payment-service-pod-1/restart', (req, res, ctx) => {
          return res(
            ctx.status(500),
            ctx.json({ message: 'Failed to restart pod' })
          );
        })
      );

      await expect(
        opsService.restartPod('payment-service-pod-1', 'Scheduled maintenance')
      ).rejects.toThrow();
    });
  });

  describe('controlService', () => {
    it('should successfully control service', async () => {
      const controlRequest = {
        action: 'RESTART',
        reason: 'Scheduled maintenance',
        force: false,
      };

      const result = await opsService.controlService('payment-service', controlRequest);

      expect(result).toEqual({ message: 'Service control initiated successfully' });
    });

    it('should handle service control errors', async () => {
      // Mock server to return error
      server.use(
        rest.post('*/ops/v1/services/payment-service/control', (req, res, ctx) => {
          return res(
            ctx.status(500),
            ctx.json({ message: 'Failed to control service' })
          );
        })
      );

      const controlRequest = {
        action: 'RESTART',
        reason: 'Scheduled maintenance',
        force: false,
      };

      await expect(
        opsService.controlService('payment-service', controlRequest)
      ).rejects.toThrow();
    });
  });
});
