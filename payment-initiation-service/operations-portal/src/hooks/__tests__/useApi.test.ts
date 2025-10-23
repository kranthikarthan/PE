/**
 * useApi Hook Tests
 * 
 * Unit tests for the useApi custom hook.
 * Tests API call management, loading states, and error handling.
 */

import { renderHook, waitFor } from '@testing-library/react';
import { rest } from 'msw';
import { server } from '../../mocks/server';
import { useApi } from '../useApi';
import { fixtures } from '../../test-utils/fixtures';

// Mock the service
const mockApiCall = jest.fn();

describe('useApi', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    mockApiCall.mockResolvedValue(fixtures.serviceHealth);
  });

  it('should initialize with loading state', () => {
    const { result } = renderHook(() => useApi(mockApiCall));

    expect(result.current.loading).toBe(true);
    expect(result.current.data).toBeNull();
    expect(result.current.error).toBeNull();
  });

  it('should execute API call and return data', async () => {
    const { result } = renderHook(() => useApi(mockApiCall, { immediate: true }));

    await waitFor(() => {
      expect(result.current.loading).toBe(false);
    });

    expect(result.current.data).toEqual(fixtures.serviceHealth);
    expect(result.current.error).toBeNull();
    expect(mockApiCall).toHaveBeenCalledTimes(1);
  });

  it('should handle API errors', async () => {
    const errorMessage = 'API Error';
    mockApiCall.mockRejectedValue(new Error(errorMessage));

    const { result } = renderHook(() => useApi(mockApiCall, { immediate: true }));

    await waitFor(() => {
      expect(result.current.loading).toBe(false);
    });

    expect(result.current.data).toBeNull();
    expect(result.current.error).toBe(errorMessage);
  });

  it('should not execute immediately when immediate is false', () => {
    renderHook(() => useApi(mockApiCall, { immediate: false }));

    expect(mockApiCall).not.toHaveBeenCalled();
  });

  it('should execute when execute is called manually', async () => {
    const { result } = renderHook(() => useApi(mockApiCall, { immediate: false }));

    expect(mockApiCall).not.toHaveBeenCalled();

    result.current.execute();

    await waitFor(() => {
      expect(result.current.loading).toBe(false);
    });

    expect(mockApiCall).toHaveBeenCalledTimes(1);
  });

  it('should execute with parameters', async () => {
    const parameters = { id: '1', status: 'active' };
    
    const { result } = renderHook(() => useApi(mockApiCall, { immediate: false }));

    result.current.execute(parameters);

    await waitFor(() => {
      expect(result.current.loading).toBe(false);
    });

    expect(mockApiCall).toHaveBeenCalledWith(parameters);
  });

  it('should reset state when reset is called', async () => {
    const { result } = renderHook(() => useApi(mockApiCall, { immediate: true }));

    await waitFor(() => {
      expect(result.current.loading).toBe(false);
    });

    expect(result.current.data).toEqual(fixtures.serviceHealth);

    result.current.reset();

    expect(result.current.data).toBeNull();
    expect(result.current.error).toBeNull();
    expect(result.current.loading).toBe(false);
  });

  it('should handle multiple executions', async () => {
    const { result } = renderHook(() => useApi(mockApiCall, { immediate: false }));

    // First execution
    result.current.execute();
    await waitFor(() => {
      expect(result.current.loading).toBe(false);
    });

    expect(mockApiCall).toHaveBeenCalledTimes(1);

    // Second execution
    result.current.execute();
    await waitFor(() => {
      expect(result.current.loading).toBe(false);
    });

    expect(mockApiCall).toHaveBeenCalledTimes(2);
  });

  it('should handle concurrent executions', async () => {
    const { result } = renderHook(() => useApi(mockApiCall, { immediate: false }));

    // Start multiple executions
    result.current.execute();
    result.current.execute();
    result.current.execute();

    await waitFor(() => {
      expect(result.current.loading).toBe(false);
    });

    // Should only execute once due to loading state
    expect(mockApiCall).toHaveBeenCalledTimes(1);
  });

  it('should handle API call with different response types', async () => {
    const stringResponse = 'Success';
    mockApiCall.mockResolvedValue(stringResponse);

    const { result } = renderHook(() => useApi(mockApiCall, { immediate: true }));

    await waitFor(() => {
      expect(result.current.loading).toBe(false);
    });

    expect(result.current.data).toBe(stringResponse);
  });

  it('should handle API call with null response', async () => {
    mockApiCall.mockResolvedValue(null);

    const { result } = renderHook(() => useApi(mockApiCall, { immediate: true }));

    await waitFor(() => {
      expect(result.current.loading).toBe(false);
    });

    expect(result.current.data).toBeNull();
  });

  it('should handle API call with undefined response', async () => {
    mockApiCall.mockResolvedValue(undefined);

    const { result } = renderHook(() => useApi(mockApiCall, { immediate: true }));

    await waitFor(() => {
      expect(result.current.loading).toBe(false);
    });

    expect(result.current.data).toBeUndefined();
  });

  it('should handle network errors', async () => {
    const networkError = new Error('Network Error');
    mockApiCall.mockRejectedValue(networkError);

    const { result } = renderHook(() => useApi(mockApiCall, { immediate: true }));

    await waitFor(() => {
      expect(result.current.loading).toBe(false);
    });

    expect(result.current.error).toBe('Network Error');
    expect(result.current.data).toBeNull();
  });

  it('should handle timeout errors', async () => {
    const timeoutError = new Error('Request timeout');
    mockApiCall.mockRejectedValue(timeoutError);

    const { result } = renderHook(() => useApi(mockApiCall, { immediate: true }));

    await waitFor(() => {
      expect(result.current.loading).toBe(false);
    });

    expect(result.current.error).toBe('Request timeout');
  });

  it('should handle server errors', async () => {
    const serverError = new Error('Internal Server Error');
    mockApiCall.mockRejectedValue(serverError);

    const { result } = renderHook(() => useApi(mockApiCall, { immediate: true }));

    await waitFor(() => {
      expect(result.current.loading).toBe(false);
    });

    expect(result.current.error).toBe('Internal Server Error');
  });

  it('should handle validation errors', async () => {
    const validationError = new Error('Validation Error');
    mockApiCall.mockRejectedValue(validationError);

    const { result } = renderHook(() => useApi(mockApiCall, { immediate: true }));

    await waitFor(() => {
      expect(result.current.loading).toBe(false);
    });

    expect(result.current.error).toBe('Validation Error');
  });

  it('should handle authentication errors', async () => {
    const authError = new Error('Authentication Error');
    mockApiCall.mockRejectedValue(authError);

    const { result } = renderHook(() => useApi(mockApiCall, { immediate: true }));

    await waitFor(() => {
      expect(result.current.loading).toBe(false);
    });

    expect(result.current.error).toBe('Authentication Error');
  });

  it('should handle authorization errors', async () => {
    const authError = new Error('Authorization Error');
    mockApiCall.mockRejectedValue(authError);

    const { result } = renderHook(() => useApi(mockApiCall, { immediate: true }));

    await waitFor(() => {
      expect(result.current.loading).toBe(false);
    });

    expect(result.current.error).toBe('Authorization Error');
  });

  it('should handle unknown errors', async () => {
    const unknownError = new Error('Unknown Error');
    mockApiCall.mockRejectedValue(unknownError);

    const { result } = renderHook(() => useApi(mockApiCall, { immediate: true }));

    await waitFor(() => {
      expect(result.current.loading).toBe(false);
    });

    expect(result.current.error).toBe('Unknown Error');
  });

  it('should handle errors without message', async () => {
    const errorWithoutMessage = new Error();
    mockApiCall.mockRejectedValue(errorWithoutMessage);

    const { result } = renderHook(() => useApi(mockApiCall, { immediate: true }));

    await waitFor(() => {
      expect(result.current.loading).toBe(false);
    });

    expect(result.current.error).toBe('An unknown error occurred');
  });

  it('should handle non-Error objects', async () => {
    const nonErrorObject = 'String error';
    mockApiCall.mockRejectedValue(nonErrorObject);

    const { result } = renderHook(() => useApi(mockApiCall, { immediate: true }));

    await waitFor(() => {
      expect(result.current.loading).toBe(false);
    });

    expect(result.current.error).toBe('An unknown error occurred');
  });
});
