package com.payments.settlement.service;

import com.payments.settlement.domain.SettlementWorkflow;
import com.payments.settlement.domain.SettlementState;
import com.payments.settlement.domain.SettlementPosition;
import com.payments.settlement.repository.SettlementWorkflowRepository;
import com.payments.settlement.repository.SettlementStateRepository;
import com.payments.settlement.repository.SettlementPositionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for SettlementStateMachine.
 *
 * @since PE-409
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Settlement State Machine Tests")
class SettlementStateMachineTest {

    @Mock
    private SettlementWorkflowRepository settlementWorkflowRepository;

    @Mock
    private SettlementStateRepository settlementStateRepository;

    @Mock
    private SettlementPositionRepository settlementPositionRepository;

    @InjectMocks
    private SettlementStateMachine settlementStateMachine;

    private SettlementWorkflow testWorkflow;
    private SettlementPosition testPosition;

    @BeforeEach
    void setUp() {
        // Create test workflow
        testWorkflow = SettlementWorkflow.builder()
                .id(1L)
                .workflowId("WF-001")
                .workflowName("Test Settlement Workflow")
                .description("Test workflow for settlement processing")
                .nettingCycleId(1L)
                .workflowType(SettlementWorkflow.WorkflowType.NETTING_SETTLEMENT)
                .status(SettlementWorkflow.WorkflowStatus.INITIATED)
                .currentStep("INITIATED")
                .stepProgress(BigDecimal.ZERO)
                .totalSteps(5)
                .completedSteps(0)
                .startTime(LocalDateTime.now())
                .priority(5)
                .businessUnitId("BU1")
                .tenantId("TENANT1")
                .createdBy("TEST_USER")
                .build();

        // Create test position
        testPosition = SettlementPosition.builder()
                .id(1L)
                .workflowId(1L)
                .nettingPositionId(1L)
                .participantId("PARTICIPANT-A")
                .participantName("Participant A")
                .currency("ZAR")
                .netAmount(BigDecimal.valueOf(1000.00))
                .settlementAmount(BigDecimal.valueOf(1000.00))
                .settledAmount(BigDecimal.ZERO)
                .remainingAmount(BigDecimal.valueOf(1000.00))
                .positionType(SettlementPosition.PositionType.DEBIT)
                .status(SettlementPosition.SettlementStatus.PENDING)
                .priority(5)
                .businessUnitId("BU1")
                .tenantId("TENANT1")
                .createdBy("TEST_USER")
                .build();
    }

    @Test
    @DisplayName("Should transition workflow successfully")
    void shouldTransitionWorkflowSuccessfully() {
        // Given
        when(settlementWorkflowRepository.findById(1L)).thenReturn(Optional.of(testWorkflow));
        when(settlementStateRepository.save(any(SettlementState.class))).thenReturn(new SettlementState());
        when(settlementWorkflowRepository.save(any(SettlementWorkflow.class))).thenReturn(testWorkflow);

        // When
        SettlementWorkflow result = settlementStateMachine.transitionWorkflow(1L, "PROCESSING");

        // Then
        assertNotNull(result);
        assertEquals("PROCESSING", result.getCurrentStep());
        verify(settlementWorkflowRepository).findById(1L);
        verify(settlementStateRepository).save(any(SettlementState.class));
        verify(settlementWorkflowRepository).save(any(SettlementWorkflow.class));
    }

    @Test
    @DisplayName("Should throw exception when workflow not found")
    void shouldThrowExceptionWhenWorkflowNotFound() {
        // Given
        when(settlementWorkflowRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            settlementStateMachine.transitionWorkflow(1L, "PROCESSING");
        });

        assertEquals("Settlement workflow not found: 1", exception.getMessage());
        verify(settlementWorkflowRepository).findById(1L);
        verify(settlementStateRepository, never()).save(any(SettlementState.class));
    }

    @Test
    @DisplayName("Should throw exception when workflow is not active")
    void shouldThrowExceptionWhenWorkflowIsNotActive() {
        // Given
        testWorkflow.setStatus(SettlementWorkflow.WorkflowStatus.COMPLETED);
        when(settlementWorkflowRepository.findById(1L)).thenReturn(Optional.of(testWorkflow));

        // When & Then
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            settlementStateMachine.transitionWorkflow(1L, "PROCESSING");
        });

        assertEquals("Workflow is not active: 1", exception.getMessage());
        verify(settlementWorkflowRepository).findById(1L);
        verify(settlementStateRepository, never()).save(any(SettlementState.class));
    }

    @Test
    @DisplayName("Should transition position successfully")
    void shouldTransitionPositionSuccessfully() {
        // Given
        when(settlementPositionRepository.findById(1L)).thenReturn(Optional.of(testPosition));
        when(settlementStateRepository.save(any(SettlementState.class))).thenReturn(new SettlementState());
        when(settlementPositionRepository.save(any(SettlementPosition.class))).thenReturn(testPosition);

        // When
        SettlementPosition result = settlementStateMachine.transitionPosition(1L, "PROCESSING");

        // Then
        assertNotNull(result);
        verify(settlementPositionRepository).findById(1L);
        verify(settlementStateRepository).save(any(SettlementState.class));
        verify(settlementPositionRepository).save(any(SettlementPosition.class));
    }

    @Test
    @DisplayName("Should throw exception when position not found")
    void shouldThrowExceptionWhenPositionNotFound() {
        // Given
        when(settlementPositionRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            settlementStateMachine.transitionPosition(1L, "PROCESSING");
        });

        assertEquals("Settlement position not found: 1", exception.getMessage());
        verify(settlementPositionRepository).findById(1L);
        verify(settlementStateRepository, never()).save(any(SettlementState.class));
    }

    @Test
    @DisplayName("Should throw exception when position is not active")
    void shouldThrowExceptionWhenPositionIsNotActive() {
        // Given
        testPosition.setStatus(SettlementPosition.SettlementStatus.SETTLED);
        when(settlementPositionRepository.findById(1L)).thenReturn(Optional.of(testPosition));

        // When & Then
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            settlementStateMachine.transitionPosition(1L, "PROCESSING");
        });

        assertEquals("Position is not active: 1", exception.getMessage());
        verify(settlementPositionRepository).findById(1L);
        verify(settlementStateRepository, never()).save(any(SettlementState.class));
    }

    @Test
    @DisplayName("Should validate valid state transition")
    void shouldValidateValidStateTransition() {
        // When
        boolean isValid = settlementStateMachine.validateTransition("INITIATED", "PROCESSING");

        // Then
        assertTrue(isValid);
    }

    @Test
    @DisplayName("Should validate invalid state transition")
    void shouldValidateInvalidStateTransition() {
        // When
        boolean isValid = settlementStateMachine.validateTransition("COMPLETED", "PROCESSING");

        // Then
        assertFalse(isValid);
    }

    @Test
    @DisplayName("Should validate unknown current state")
    void shouldValidateUnknownCurrentState() {
        // When
        boolean isValid = settlementStateMachine.validateTransition("UNKNOWN", "PROCESSING");

        // Then
        assertFalse(isValid);
    }

    @Test
    @DisplayName("Should get current workflow state")
    void shouldGetCurrentWorkflowState() {
        // Given
        SettlementState currentState = SettlementState.builder()
                .id(1L)
                .workflowId(1L)
                .stateName("PROCESSING")
                .stateType(SettlementState.StateType.WORKFLOW)
                .status(SettlementState.StateStatus.ACTIVE)
                .stateTimestamp(LocalDateTime.now())
                .build();
        
        when(settlementStateRepository.findCurrentWorkflowState(1L)).thenReturn(Optional.of(currentState));

        // When
        Optional<SettlementState> result = settlementStateMachine.getCurrentWorkflowState(1L);

        // Then
        assertTrue(result.isPresent());
        assertEquals("PROCESSING", result.get().getStateName());
        verify(settlementStateRepository).findCurrentWorkflowState(1L);
    }

    @Test
    @DisplayName("Should get current position state")
    void shouldGetCurrentPositionState() {
        // Given
        SettlementState currentState = SettlementState.builder()
                .id(1L)
                .workflowId(1L)
                .stateName("PROCESSING")
                .stateType(SettlementState.StateType.POSITION)
                .status(SettlementState.StateStatus.ACTIVE)
                .positionId(1L)
                .stateTimestamp(LocalDateTime.now())
                .build();
        
        when(settlementStateRepository.findCurrentPositionState(1L)).thenReturn(Optional.of(currentState));

        // When
        Optional<SettlementState> result = settlementStateMachine.getCurrentPositionState(1L);

        // Then
        assertTrue(result.isPresent());
        assertEquals("PROCESSING", result.get().getStateName());
        verify(settlementStateRepository).findCurrentPositionState(1L);
    }

    @Test
    @DisplayName("Should get workflow state history")
    void shouldGetWorkflowStateHistory() {
        // Given
        List<SettlementState> states = Arrays.asList(
                SettlementState.builder().id(1L).workflowId(1L).stateName("INITIATED").build(),
                SettlementState.builder().id(2L).workflowId(1L).stateName("PROCESSING").build()
        );
        when(settlementStateRepository.findByWorkflowIdOrderByStateTimestampDesc(1L)).thenReturn(states);

        // When
        List<SettlementState> result = settlementStateMachine.getWorkflowStateHistory(1L);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(settlementStateRepository).findByWorkflowIdOrderByStateTimestampDesc(1L);
    }

    @Test
    @DisplayName("Should get position state history")
    void shouldGetPositionStateHistory() {
        // Given
        List<SettlementState> states = Arrays.asList(
                SettlementState.builder().id(1L).positionId(1L).stateName("PENDING").build(),
                SettlementState.builder().id(2L).positionId(1L).stateName("PROCESSING").build()
        );
        when(settlementStateRepository.findByPositionIdOrderByStateTimestampDesc(1L)).thenReturn(states);

        // When
        List<SettlementState> result = settlementStateMachine.getPositionStateHistory(1L);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(settlementStateRepository).findByPositionIdOrderByStateTimestampDesc(1L);
    }

    @Test
    @DisplayName("Should complete state successfully")
    void shouldCompleteStateSuccessfully() {
        // Given
        SettlementState state = SettlementState.builder()
                .id(1L)
                .workflowId(1L)
                .stateName("PROCESSING")
                .stateType(SettlementState.StateType.WORKFLOW)
                .status(SettlementState.StateStatus.ACTIVE)
                .stateTimestamp(LocalDateTime.now())
                .build();
        
        when(settlementStateRepository.findById(1L)).thenReturn(Optional.of(state));
        when(settlementStateRepository.save(any(SettlementState.class))).thenReturn(state);

        // When
        settlementStateMachine.completeState(1L);

        // Then
        verify(settlementStateRepository).findById(1L);
        verify(settlementStateRepository).save(any(SettlementState.class));
    }

    @Test
    @DisplayName("Should fail state successfully")
    void shouldFailStateSuccessfully() {
        // Given
        SettlementState state = SettlementState.builder()
                .id(1L)
                .workflowId(1L)
                .stateName("PROCESSING")
                .stateType(SettlementState.StateType.WORKFLOW)
                .status(SettlementState.StateStatus.ACTIVE)
                .stateTimestamp(LocalDateTime.now())
                .build();
        
        when(settlementStateRepository.findById(1L)).thenReturn(Optional.of(state));
        when(settlementStateRepository.save(any(SettlementState.class))).thenReturn(state);

        // When
        settlementStateMachine.failState(1L, "Test error message");

        // Then
        verify(settlementStateRepository).findById(1L);
        verify(settlementStateRepository).save(any(SettlementState.class));
    }

    @Test
    @DisplayName("Should retry state successfully")
    void shouldRetryStateSuccessfully() {
        // Given
        SettlementState state = SettlementState.builder()
                .id(1L)
                .workflowId(1L)
                .stateName("PROCESSING")
                .stateType(SettlementState.StateType.WORKFLOW)
                .status(SettlementState.StateStatus.FAILED)
                .retryCount(0)
                .maxRetries(3)
                .stateTimestamp(LocalDateTime.now())
                .build();
        
        when(settlementStateRepository.findById(1L)).thenReturn(Optional.of(state));
        when(settlementStateRepository.save(any(SettlementState.class))).thenReturn(state);

        // When
        settlementStateMachine.retryState(1L);

        // Then
        verify(settlementStateRepository).findById(1L);
        verify(settlementStateRepository).save(any(SettlementState.class));
    }

    @Test
    @DisplayName("Should throw exception when state cannot be retried")
    void shouldThrowExceptionWhenStateCannotBeRetried() {
        // Given
        SettlementState state = SettlementState.builder()
                .id(1L)
                .workflowId(1L)
                .stateName("PROCESSING")
                .stateType(SettlementState.StateType.WORKFLOW)
                .status(SettlementState.StateStatus.FAILED)
                .retryCount(3)
                .maxRetries(3)
                .stateTimestamp(LocalDateTime.now())
                .build();
        
        when(settlementStateRepository.findById(1L)).thenReturn(Optional.of(state));

        // When & Then
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            settlementStateMachine.retryState(1L);
        });

        assertEquals("State cannot be retried: 1", exception.getMessage());
        verify(settlementStateRepository).findById(1L);
        verify(settlementStateRepository, never()).save(any(SettlementState.class));
    }

    @Test
    @DisplayName("Should suspend state successfully")
    void shouldSuspendStateSuccessfully() {
        // Given
        SettlementState state = SettlementState.builder()
                .id(1L)
                .workflowId(1L)
                .stateName("PROCESSING")
                .stateType(SettlementState.StateType.WORKFLOW)
                .status(SettlementState.StateStatus.ACTIVE)
                .stateTimestamp(LocalDateTime.now())
                .build();
        
        when(settlementStateRepository.findById(1L)).thenReturn(Optional.of(state));
        when(settlementStateRepository.save(any(SettlementState.class))).thenReturn(state);

        // When
        settlementStateMachine.suspendState(1L);

        // Then
        verify(settlementStateRepository).findById(1L);
        verify(settlementStateRepository).save(any(SettlementState.class));
    }

    @Test
    @DisplayName("Should resume state successfully")
    void shouldResumeStateSuccessfully() {
        // Given
        SettlementState state = SettlementState.builder()
                .id(1L)
                .workflowId(1L)
                .stateName("PROCESSING")
                .stateType(SettlementState.StateType.WORKFLOW)
                .status(SettlementState.StateStatus.SUSPENDED)
                .stateTimestamp(LocalDateTime.now())
                .build();
        
        when(settlementStateRepository.findById(1L)).thenReturn(Optional.of(state));
        when(settlementStateRepository.save(any(SettlementState.class))).thenReturn(state);

        // When
        settlementStateMachine.resumeState(1L);

        // Then
        verify(settlementStateRepository).findById(1L);
        verify(settlementStateRepository).save(any(SettlementState.class));
    }

    @Test
    @DisplayName("Should get state machine statistics")
    void shouldGetStateMachineStatistics() {
        // Given
        List<SettlementState> states = Arrays.asList(
                SettlementState.builder().id(1L).workflowId(1L).status(SettlementState.StateStatus.ACTIVE).durationSeconds(100L).build(),
                SettlementState.builder().id(2L).workflowId(1L).status(SettlementState.StateStatus.COMPLETED).durationSeconds(200L).build(),
                SettlementState.builder().id(3L).workflowId(1L).status(SettlementState.StateStatus.FAILED).durationSeconds(150L).build()
        );
        when(settlementStateRepository.findByWorkflowIdOrderByStateTimestampDesc(1L)).thenReturn(states);

        // When
        Map<String, Object> statistics = settlementStateMachine.getStateMachineStatistics(1L);

        // Then
        assertNotNull(statistics);
        assertEquals(3, statistics.get("totalStates"));
        assertEquals(1, statistics.get("activeStates"));
        assertEquals(1, statistics.get("completedStates"));
        assertEquals(1, statistics.get("failedStates"));
        verify(settlementStateRepository).findByWorkflowIdOrderByStateTimestampDesc(1L);
    }
}
