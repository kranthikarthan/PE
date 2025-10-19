package com.payments.settlement.service;

import com.payments.domain.settlement.SettlementOrchestration;
import com.payments.settlement.repository.SettlementOrchestrationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for SettlementOrchestrationService.
 *
 * @since PE-410
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Settlement Orchestration Service Tests")
class SettlementOrchestrationServiceTest {

    @Mock
    private SettlementOrchestrationRepository settlementOrchestrationRepository;

    @InjectMocks
    private SettlementOrchestrationService settlementOrchestrationService;

    private SettlementOrchestration testOrchestration;

    @BeforeEach
    void setUp() {
        // Create test orchestration
        testOrchestration = SettlementOrchestration.builder()
                .id(1L)
                .orchestrationId("ORCH-001")
                .orchestrationName("Test Settlement Orchestration")
                .description("Test orchestration for settlement coordination")
                .orchestrationType(SettlementOrchestration.OrchestrationType.NETTING_SETTLEMENT)
                .status(SettlementOrchestration.OrchestrationStatus.INITIATED)
                .currentPhase("INITIATED")
                .phaseProgress(BigDecimal.ZERO)
                .totalPhases(5)
                .completedPhases(0)
                .startTime(LocalDateTime.now())
                .priority(5)
                .businessUnitId("BU1")
                .tenantId("TENANT1")
                .createdBy("TEST_USER")
                .build();
    }

    @Test
    @DisplayName("Should create orchestration successfully")
    void shouldCreateOrchestrationSuccessfully() {
        // Given
        when(settlementOrchestrationRepository.save(any(SettlementOrchestration.class))).thenReturn(testOrchestration);

        // When
        SettlementOrchestration result = settlementOrchestrationService.createOrchestration(testOrchestration);

        // Then
        assertNotNull(result);
        assertEquals("ORCH-001", result.getOrchestrationId());
        assertEquals("Test Settlement Orchestration", result.getOrchestrationName());
        assertEquals(SettlementOrchestration.OrchestrationStatus.INITIATED, result.getStatus());
        assertNotNull(result.getStartTime());
        verify(settlementOrchestrationRepository).save(any(SettlementOrchestration.class));
    }

    @Test
    @DisplayName("Should start coordination successfully")
    void shouldStartCoordinationSuccessfully() {
        // Given
        when(settlementOrchestrationRepository.findById(1L)).thenReturn(Optional.of(testOrchestration));
        when(settlementOrchestrationRepository.save(any(SettlementOrchestration.class))).thenReturn(testOrchestration);

        // When
        SettlementOrchestration result = settlementOrchestrationService.startCoordination(1L);

        // Then
        assertNotNull(result);
        assertEquals(SettlementOrchestration.OrchestrationStatus.COORDINATING, result.getStatus());
        verify(settlementOrchestrationRepository).findById(1L);
        verify(settlementOrchestrationRepository).save(any(SettlementOrchestration.class));
    }

    @Test
    @DisplayName("Should throw exception when orchestration not found for coordination")
    void shouldThrowExceptionWhenOrchestrationNotFoundForCoordination() {
        // Given
        when(settlementOrchestrationRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            settlementOrchestrationService.startCoordination(1L);
        });

        assertEquals("Settlement orchestration not found: 1", exception.getMessage());
        verify(settlementOrchestrationRepository).findById(1L);
        verify(settlementOrchestrationRepository, never()).save(any(SettlementOrchestration.class));
    }

    @Test
    @DisplayName("Should throw exception when orchestration is not active for coordination")
    void shouldThrowExceptionWhenOrchestrationIsNotActiveForCoordination() {
        // Given
        testOrchestration.setStatus(SettlementOrchestration.OrchestrationStatus.COMPLETED);
        when(settlementOrchestrationRepository.findById(1L)).thenReturn(Optional.of(testOrchestration));

        // When & Then
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            settlementOrchestrationService.startCoordination(1L);
        });

        assertEquals("Orchestration is not active: 1", exception.getMessage());
        verify(settlementOrchestrationRepository).findById(1L);
        verify(settlementOrchestrationRepository, never()).save(any(SettlementOrchestration.class));
    }

    @Test
    @DisplayName("Should start execution successfully")
    void shouldStartExecutionSuccessfully() {
        // Given
        when(settlementOrchestrationRepository.findById(1L)).thenReturn(Optional.of(testOrchestration));
        when(settlementOrchestrationRepository.save(any(SettlementOrchestration.class))).thenReturn(testOrchestration);

        // When
        SettlementOrchestration result = settlementOrchestrationService.startExecution(1L);

        // Then
        assertNotNull(result);
        assertEquals(SettlementOrchestration.OrchestrationStatus.EXECUTING, result.getStatus());
        verify(settlementOrchestrationRepository).findById(1L);
        verify(settlementOrchestrationRepository).save(any(SettlementOrchestration.class));
    }

    @Test
    @DisplayName("Should start monitoring successfully")
    void shouldStartMonitoringSuccessfully() {
        // Given
        when(settlementOrchestrationRepository.findById(1L)).thenReturn(Optional.of(testOrchestration));
        when(settlementOrchestrationRepository.save(any(SettlementOrchestration.class))).thenReturn(testOrchestration);

        // When
        SettlementOrchestration result = settlementOrchestrationService.startMonitoring(1L);

        // Then
        assertNotNull(result);
        assertEquals(SettlementOrchestration.OrchestrationStatus.MONITORING, result.getStatus());
        verify(settlementOrchestrationRepository).findById(1L);
        verify(settlementOrchestrationRepository).save(any(SettlementOrchestration.class));
    }

    @Test
    @DisplayName("Should complete orchestration successfully")
    void shouldCompleteOrchestrationSuccessfully() {
        // Given
        when(settlementOrchestrationRepository.findById(1L)).thenReturn(Optional.of(testOrchestration));
        when(settlementOrchestrationRepository.save(any(SettlementOrchestration.class))).thenReturn(testOrchestration);

        // When
        SettlementOrchestration result = settlementOrchestrationService.completeOrchestration(1L);

        // Then
        assertNotNull(result);
        assertEquals(SettlementOrchestration.OrchestrationStatus.COMPLETED, result.getStatus());
        assertNotNull(result.getEndTime());
        verify(settlementOrchestrationRepository).findById(1L);
        verify(settlementOrchestrationRepository).save(any(SettlementOrchestration.class));
    }

    @Test
    @DisplayName("Should fail orchestration successfully")
    void shouldFailOrchestrationSuccessfully() {
        // Given
        String errorMessage = "Test error message";
        when(settlementOrchestrationRepository.findById(1L)).thenReturn(Optional.of(testOrchestration));
        when(settlementOrchestrationRepository.save(any(SettlementOrchestration.class))).thenReturn(testOrchestration);

        // When
        SettlementOrchestration result = settlementOrchestrationService.failOrchestration(1L, errorMessage);

        // Then
        assertNotNull(result);
        assertEquals(SettlementOrchestration.OrchestrationStatus.FAILED, result.getStatus());
        assertEquals(errorMessage, result.getErrorMessage());
        assertNotNull(result.getEndTime());
        verify(settlementOrchestrationRepository).findById(1L);
        verify(settlementOrchestrationRepository).save(any(SettlementOrchestration.class));
    }

    @Test
    @DisplayName("Should cancel orchestration successfully")
    void shouldCancelOrchestrationSuccessfully() {
        // Given
        when(settlementOrchestrationRepository.findById(1L)).thenReturn(Optional.of(testOrchestration));
        when(settlementOrchestrationRepository.save(any(SettlementOrchestration.class))).thenReturn(testOrchestration);

        // When
        SettlementOrchestration result = settlementOrchestrationService.cancelOrchestration(1L);

        // Then
        assertNotNull(result);
        assertEquals(SettlementOrchestration.OrchestrationStatus.CANCELLED, result.getStatus());
        assertNotNull(result.getEndTime());
        verify(settlementOrchestrationRepository).findById(1L);
        verify(settlementOrchestrationRepository).save(any(SettlementOrchestration.class));
    }

    @Test
    @DisplayName("Should suspend orchestration successfully")
    void shouldSuspendOrchestrationSuccessfully() {
        // Given
        when(settlementOrchestrationRepository.findById(1L)).thenReturn(Optional.of(testOrchestration));
        when(settlementOrchestrationRepository.save(any(SettlementOrchestration.class))).thenReturn(testOrchestration);

        // When
        SettlementOrchestration result = settlementOrchestrationService.suspendOrchestration(1L);

        // Then
        assertNotNull(result);
        assertEquals(SettlementOrchestration.OrchestrationStatus.SUSPENDED, result.getStatus());
        verify(settlementOrchestrationRepository).findById(1L);
        verify(settlementOrchestrationRepository).save(any(SettlementOrchestration.class));
    }

    @Test
    @DisplayName("Should resume orchestration successfully")
    void shouldResumeOrchestrationSuccessfully() {
        // Given
        testOrchestration.setStatus(SettlementOrchestration.OrchestrationStatus.SUSPENDED);
        when(settlementOrchestrationRepository.findById(1L)).thenReturn(Optional.of(testOrchestration));
        when(settlementOrchestrationRepository.save(any(SettlementOrchestration.class))).thenReturn(testOrchestration);

        // When
        SettlementOrchestration result = settlementOrchestrationService.resumeOrchestration(1L);

        // Then
        assertNotNull(result);
        assertEquals(SettlementOrchestration.OrchestrationStatus.COORDINATING, result.getStatus());
        verify(settlementOrchestrationRepository).findById(1L);
        verify(settlementOrchestrationRepository).save(any(SettlementOrchestration.class));
    }

    @Test
    @DisplayName("Should update progress successfully")
    void shouldUpdateProgressSuccessfully() {
        // Given
        String currentPhase = "PROCESSING";
        BigDecimal phaseProgress = BigDecimal.valueOf(50.0);
        when(settlementOrchestrationRepository.findById(1L)).thenReturn(Optional.of(testOrchestration));
        when(settlementOrchestrationRepository.save(any(SettlementOrchestration.class))).thenReturn(testOrchestration);

        // When
        SettlementOrchestration result = settlementOrchestrationService.updateProgress(1L, currentPhase, phaseProgress);

        // Then
        assertNotNull(result);
        assertEquals(currentPhase, result.getCurrentPhase());
        assertEquals(phaseProgress, result.getPhaseProgress());
        verify(settlementOrchestrationRepository).findById(1L);
        verify(settlementOrchestrationRepository).save(any(SettlementOrchestration.class));
    }

    @Test
    @DisplayName("Should complete phase successfully")
    void shouldCompletePhaseSuccessfully() {
        // Given
        when(settlementOrchestrationRepository.findById(1L)).thenReturn(Optional.of(testOrchestration));
        when(settlementOrchestrationRepository.save(any(SettlementOrchestration.class))).thenReturn(testOrchestration);

        // When
        SettlementOrchestration result = settlementOrchestrationService.completePhase(1L);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getCompletedPhases());
        assertEquals(BigDecimal.ZERO, result.getPhaseProgress());
        verify(settlementOrchestrationRepository).findById(1L);
        verify(settlementOrchestrationRepository).save(any(SettlementOrchestration.class));
    }

    @Test
    @DisplayName("Should get orchestration by ID")
    void shouldGetOrchestrationById() {
        // Given
        when(settlementOrchestrationRepository.findById(1L)).thenReturn(Optional.of(testOrchestration));

        // When
        Optional<SettlementOrchestration> result = settlementOrchestrationService.getOrchestration(1L);

        // Then
        assertTrue(result.isPresent());
        assertEquals("ORCH-001", result.get().getOrchestrationId());
        verify(settlementOrchestrationRepository).findById(1L);
    }

    @Test
    @DisplayName("Should get orchestration by orchestration ID")
    void shouldGetOrchestrationByOrchestrationId() {
        // Given
        when(settlementOrchestrationRepository.findByOrchestrationId("ORCH-001")).thenReturn(Optional.of(testOrchestration));

        // When
        Optional<SettlementOrchestration> result = settlementOrchestrationService.getOrchestrationByOrchestrationId("ORCH-001");

        // Then
        assertTrue(result.isPresent());
        assertEquals("ORCH-001", result.get().getOrchestrationId());
        verify(settlementOrchestrationRepository).findByOrchestrationId("ORCH-001");
    }

    @Test
    @DisplayName("Should get orchestration statistics")
    void shouldGetOrchestrationStatistics() {
        // Given
        when(settlementOrchestrationRepository.findById(1L)).thenReturn(Optional.of(testOrchestration));

        // When
        Map<String, Object> statistics = settlementOrchestrationService.getOrchestrationStatistics(1L);

        // Then
        assertNotNull(statistics);
        assertEquals("ORCH-001", statistics.get("orchestrationId"));
        assertEquals("Test Settlement Orchestration", statistics.get("orchestrationName"));
        assertEquals(SettlementOrchestration.OrchestrationStatus.INITIATED, statistics.get("status"));
        assertEquals("INITIATED", statistics.get("currentPhase"));
        assertEquals(5, statistics.get("totalPhases"));
        assertEquals(0, statistics.get("completedPhases"));
        verify(settlementOrchestrationRepository).findById(1L);
    }

    @Test
    @DisplayName("Should get orchestration summary")
    void shouldGetOrchestrationSummary() {
        // Given
        when(settlementOrchestrationRepository.findById(1L)).thenReturn(Optional.of(testOrchestration));

        // When
        String summary = settlementOrchestrationService.getOrchestrationSummary(1L);

        // Then
        assertNotNull(summary);
        assertTrue(summary.contains("ORCH-001"));
        assertTrue(summary.contains("Test Settlement Orchestration"));
        assertTrue(summary.contains("INITIATED"));
        verify(settlementOrchestrationRepository).findById(1L);
    }

    @Test
    @DisplayName("Should throw exception when orchestration not found for statistics")
    void shouldThrowExceptionWhenOrchestrationNotFoundForStatistics() {
        // Given
        when(settlementOrchestrationRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            settlementOrchestrationService.getOrchestrationStatistics(1L);
        });

        assertEquals("Settlement orchestration not found: 1", exception.getMessage());
        verify(settlementOrchestrationRepository).findById(1L);
    }

    @Test
    @DisplayName("Should throw exception when orchestration not found for summary")
    void shouldThrowExceptionWhenOrchestrationNotFoundForSummary() {
        // Given
        when(settlementOrchestrationRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            settlementOrchestrationService.getOrchestrationSummary(1L);
        });

        assertEquals("Settlement orchestration not found: 1", exception.getMessage());
        verify(settlementOrchestrationRepository).findById(1L);
    }
}
