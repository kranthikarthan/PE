package com.paymentengine.middleware.service;

import com.paymentengine.middleware.dto.ClearingSystemRequest;
import com.paymentengine.middleware.dto.ClearingSystemResponse;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.timelimiter.TimeLimiter;
import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.ratelimiter.RateLimiter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ResilientClearingSystemServiceTest {

    @Mock
    private CircuitBreaker clearingSystemCircuitBreaker;

    @Mock
    private Retry clearingSystemRetry;

    @Mock
    private TimeLimiter clearingSystemTimeLimiter;

    @Mock
    private Bulkhead clearingSystemBulkhead;

    @Mock
    private RateLimiter clearingSystemRateLimiter;

    @Mock
    private ClearingSystemRoutingService clearingSystemRoutingService;

    @InjectMocks
    private ResilientClearingSystemService resilientClearingSystemService;

    private ClearingSystemResponse testClearingSystem;
    private ClearingSystemRequest testRequest;

    @BeforeEach
    void setUp() {
        testClearingSystem = new ClearingSystemResponse();
        testClearingSystem.setId(UUID.randomUUID());
        testClearingSystem.setName("Test Clearing System");
        testClearingSystem.setCode("TEST");
        testClearingSystem.setIsActive(true);

        testRequest = new ClearingSystemRequest();
        testRequest.setName("Test Clearing System");
        testRequest.setCode("TEST");
        testRequest.setDescription("Test Description");
        testRequest.setIsActive(true);
    }

    @Test
    void getAllClearingSystems_Success_ReturnsClearingSystems() {
        // Given
        List<ClearingSystemResponse> expectedSystems = Arrays.asList(testClearingSystem);
        when(clearingSystemRoutingService.getAllClearingSystems()).thenReturn(expectedSystems);

        // When
        List<ClearingSystemResponse> result = resilientClearingSystemService.getAllClearingSystems();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testClearingSystem, result.get(0));
        verify(clearingSystemRoutingService).getAllClearingSystems();
    }

    @Test
    void getClearingSystemById_Success_ReturnsClearingSystem() {
        // Given
        UUID id = testClearingSystem.getId();
        when(clearingSystemRoutingService.getClearingSystemById(id)).thenReturn(testClearingSystem);

        // When
        ClearingSystemResponse result = resilientClearingSystemService.getClearingSystemById(id);

        // Then
        assertNotNull(result);
        assertEquals(testClearingSystem, result);
        verify(clearingSystemRoutingService).getClearingSystemById(id);
    }

    @Test
    void createClearingSystem_Success_ReturnsCreatedClearingSystem() {
        // Given
        when(clearingSystemRoutingService.createClearingSystem(testRequest)).thenReturn(testClearingSystem);

        // When
        ClearingSystemResponse result = resilientClearingSystemService.createClearingSystem(testRequest);

        // Then
        assertNotNull(result);
        assertEquals(testClearingSystem, result);
        verify(clearingSystemRoutingService).createClearingSystem(testRequest);
    }

    @Test
    void updateClearingSystem_Success_ReturnsUpdatedClearingSystem() {
        // Given
        UUID id = testClearingSystem.getId();
        when(clearingSystemRoutingService.updateClearingSystem(id, testRequest)).thenReturn(testClearingSystem);

        // When
        ClearingSystemResponse result = resilientClearingSystemService.updateClearingSystem(id, testRequest);

        // Then
        assertNotNull(result);
        assertEquals(testClearingSystem, result);
        verify(clearingSystemRoutingService).updateClearingSystem(id, testRequest);
    }

    @Test
    void deleteClearingSystem_Success_DeletesClearingSystem() {
        // Given
        UUID id = testClearingSystem.getId();
        doNothing().when(clearingSystemRoutingService).deleteClearingSystem(id);

        // When
        resilientClearingSystemService.deleteClearingSystem(id);

        // Then
        verify(clearingSystemRoutingService).deleteClearingSystem(id);
    }

    @Test
    void getClearingSystemByIdAsync_Success_ReturnsCompletableFuture() {
        // Given
        UUID id = testClearingSystem.getId();
        when(clearingSystemRoutingService.getClearingSystemById(id)).thenReturn(testClearingSystem);

        // When
        CompletableFuture<ClearingSystemResponse> result = resilientClearingSystemService.getClearingSystemByIdAsync(id);

        // Then
        assertNotNull(result);
        assertTrue(result.isDone());
        assertEquals(testClearingSystem, result.join());
        verify(clearingSystemRoutingService).getClearingSystemById(id);
    }

    @Test
    void getAllClearingSystemsAsync_Success_ReturnsCompletableFuture() {
        // Given
        List<ClearingSystemResponse> expectedSystems = Arrays.asList(testClearingSystem);
        when(clearingSystemRoutingService.getAllClearingSystems()).thenReturn(expectedSystems);

        // When
        CompletableFuture<List<ClearingSystemResponse>> result = resilientClearingSystemService.getAllClearingSystemsAsync();

        // Then
        assertNotNull(result);
        assertTrue(result.isDone());
        assertEquals(expectedSystems, result.join());
        verify(clearingSystemRoutingService).getAllClearingSystems();
    }

    @Test
    void getAllClearingSystems_ServiceThrowsException_PropagatesException() {
        // Given
        RuntimeException expectedException = new RuntimeException("Service unavailable");
        when(clearingSystemRoutingService.getAllClearingSystems()).thenThrow(expectedException);

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            resilientClearingSystemService.getAllClearingSystems();
        });

        verify(clearingSystemRoutingService).getAllClearingSystems();
    }

    @Test
    void getClearingSystemById_ServiceThrowsException_PropagatesException() {
        // Given
        UUID id = testClearingSystem.getId();
        RuntimeException expectedException = new RuntimeException("Service unavailable");
        when(clearingSystemRoutingService.getClearingSystemById(id)).thenThrow(expectedException);

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            resilientClearingSystemService.getClearingSystemById(id);
        });

        verify(clearingSystemRoutingService).getClearingSystemById(id);
    }

    @Test
    void createClearingSystem_ServiceThrowsException_PropagatesException() {
        // Given
        RuntimeException expectedException = new RuntimeException("Service unavailable");
        when(clearingSystemRoutingService.createClearingSystem(testRequest)).thenThrow(expectedException);

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            resilientClearingSystemService.createClearingSystem(testRequest);
        });

        verify(clearingSystemRoutingService).createClearingSystem(testRequest);
    }

    @Test
    void updateClearingSystem_ServiceThrowsException_PropagatesException() {
        // Given
        UUID id = testClearingSystem.getId();
        RuntimeException expectedException = new RuntimeException("Service unavailable");
        when(clearingSystemRoutingService.updateClearingSystem(id, testRequest)).thenThrow(expectedException);

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            resilientClearingSystemService.updateClearingSystem(id, testRequest);
        });

        verify(clearingSystemRoutingService).updateClearingSystem(id, testRequest);
    }

    @Test
    void deleteClearingSystem_ServiceThrowsException_PropagatesException() {
        // Given
        UUID id = testClearingSystem.getId();
        RuntimeException expectedException = new RuntimeException("Service unavailable");
        doThrow(expectedException).when(clearingSystemRoutingService).deleteClearingSystem(id);

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            resilientClearingSystemService.deleteClearingSystem(id);
        });

        verify(clearingSystemRoutingService).deleteClearingSystem(id);
    }

    @Test
    void getClearingSystemByIdAsync_ServiceThrowsException_PropagatesException() {
        // Given
        UUID id = testClearingSystem.getId();
        RuntimeException expectedException = new RuntimeException("Service unavailable");
        when(clearingSystemRoutingService.getClearingSystemById(id)).thenThrow(expectedException);

        // When
        CompletableFuture<ClearingSystemResponse> result = resilientClearingSystemService.getClearingSystemByIdAsync(id);

        // Then
        assertNotNull(result);
        assertTrue(result.isCompletedExceptionally());
        assertThrows(RuntimeException.class, () -> result.join());
        verify(clearingSystemRoutingService).getClearingSystemById(id);
    }

    @Test
    void getAllClearingSystemsAsync_ServiceThrowsException_PropagatesException() {
        // Given
        RuntimeException expectedException = new RuntimeException("Service unavailable");
        when(clearingSystemRoutingService.getAllClearingSystems()).thenThrow(expectedException);

        // When
        CompletableFuture<List<ClearingSystemResponse>> result = resilientClearingSystemService.getAllClearingSystemsAsync();

        // Then
        assertNotNull(result);
        assertTrue(result.isCompletedExceptionally());
        assertThrows(RuntimeException.class, () -> result.join());
        verify(clearingSystemRoutingService).getAllClearingSystems();
    }
}