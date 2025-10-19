package com.payments.settlement.service;

import com.payments.settlement.domain.NettingCycle;
import com.payments.settlement.domain.NettingPosition;
import com.payments.settlement.domain.NettingTransaction;
import com.payments.settlement.repository.NettingCycleRepository;
import com.payments.settlement.repository.NettingPositionRepository;
import com.payments.settlement.repository.NettingTransactionRepository;
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
 * Unit tests for NettingCalculationService.
 *
 * @since PE-408
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Netting Calculation Service Tests")
class NettingCalculationServiceTest {

    @Mock
    private NettingCycleRepository nettingCycleRepository;

    @Mock
    private NettingPositionRepository nettingPositionRepository;

    @Mock
    private NettingTransactionRepository nettingTransactionRepository;

    @InjectMocks
    private NettingCalculationService nettingCalculationService;

    private NettingCycle testCycle;
    private List<NettingTransaction> testTransactions;

    @BeforeEach
    void setUp() {
        // Create test cycle
        testCycle = NettingCycle.builder()
                .id(1L)
                .cycleId("CYCLE-001")
                .cycleName("Test Netting Cycle")
                .description("Test cycle for netting calculation")
                .cycleType(NettingCycle.CycleType.DAILY)
                .status(NettingCycle.CycleStatus.ACTIVE)
                .startTime(LocalDateTime.now())
                .priority(5)
                .businessUnitId("BU1")
                .tenantId("TENANT1")
                .createdBy("TEST_USER")
                .build();

        // Create test transactions
        testTransactions = Arrays.asList(
                NettingTransaction.builder()
                        .id(1L)
                        .transactionId("TXN-001")
                        .nettingCycleId(1L)
                        .debtorParticipantId("PARTICIPANT-A")
                        .debtorParticipantName("Participant A")
                        .creditorParticipantId("PARTICIPANT-B")
                        .creditorParticipantName("Participant B")
                        .amount(BigDecimal.valueOf(1000.00))
                        .currency("ZAR")
                        .reference("REF-001")
                        .description("Payment from A to B")
                        .transactionType(NettingTransaction.TransactionType.PAYMENT)
                        .status(NettingTransaction.TransactionStatus.PENDING)
                        .transactionDate(LocalDateTime.now())
                        .priority(5)
                        .businessUnitId("BU1")
                        .tenantId("TENANT1")
                        .createdBy("TEST_USER")
                        .build(),
                NettingTransaction.builder()
                        .id(2L)
                        .transactionId("TXN-002")
                        .nettingCycleId(1L)
                        .debtorParticipantId("PARTICIPANT-B")
                        .debtorParticipantName("Participant B")
                        .creditorParticipantId("PARTICIPANT-A")
                        .creditorParticipantName("Participant A")
                        .amount(BigDecimal.valueOf(500.00))
                        .currency("ZAR")
                        .reference("REF-002")
                        .description("Payment from B to A")
                        .transactionType(NettingTransaction.TransactionType.PAYMENT)
                        .status(NettingTransaction.TransactionStatus.PENDING)
                        .transactionDate(LocalDateTime.now())
                        .priority(5)
                        .businessUnitId("BU1")
                        .tenantId("TENANT1")
                        .createdBy("TEST_USER")
                        .build(),
                NettingTransaction.builder()
                        .id(3L)
                        .transactionId("TXN-003")
                        .nettingCycleId(1L)
                        .debtorParticipantId("PARTICIPANT-A")
                        .debtorParticipantName("Participant A")
                        .creditorParticipantId("PARTICIPANT-C")
                        .creditorParticipantName("Participant C")
                        .amount(BigDecimal.valueOf(300.00))
                        .currency("ZAR")
                        .reference("REF-003")
                        .description("Payment from A to C")
                        .transactionType(NettingTransaction.TransactionType.PAYMENT)
                        .status(NettingTransaction.TransactionStatus.PENDING)
                        .transactionDate(LocalDateTime.now())
                        .priority(5)
                        .businessUnitId("BU1")
                        .tenantId("TENANT1")
                        .createdBy("TEST_USER")
                        .build()
        );
    }

    @Test
    @DisplayName("Should calculate netting positions successfully")
    void shouldCalculateNettingPositionsSuccessfully() {
        // Given
        when(nettingCycleRepository.findById(1L)).thenReturn(Optional.of(testCycle));
        when(nettingTransactionRepository.findByNettingCycleId(1L)).thenReturn(testTransactions);
        when(nettingPositionRepository.saveAll(anyList())).thenReturn(Collections.emptyList());
        when(nettingCycleRepository.save(any(NettingCycle.class))).thenReturn(testCycle);

        // When
        List<NettingPosition> positions = nettingCalculationService.calculateNettingPositions(1L);

        // Then
        assertNotNull(positions);
        verify(nettingCycleRepository).findById(1L);
        verify(nettingTransactionRepository).findByNettingCycleId(1L);
        verify(nettingPositionRepository).saveAll(anyList());
        verify(nettingCycleRepository, times(2)).save(any(NettingCycle.class));
    }

    @Test
    @DisplayName("Should throw exception when cycle not found")
    void shouldThrowExceptionWhenCycleNotFound() {
        // Given
        when(nettingCycleRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            nettingCalculationService.calculateNettingPositions(1L);
        });

        assertEquals("Netting cycle not found: 1", exception.getMessage());
        verify(nettingCycleRepository).findById(1L);
        verify(nettingTransactionRepository, never()).findByNettingCycleId(anyLong());
    }

    @Test
    @DisplayName("Should throw exception when cycle is not active")
    void shouldThrowExceptionWhenCycleIsNotActive() {
        // Given
        testCycle.setStatus(NettingCycle.CycleStatus.COMPLETED);
        when(nettingCycleRepository.findById(1L)).thenReturn(Optional.of(testCycle));

        // When & Then
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            nettingCalculationService.calculateNettingPositions(1L);
        });

        assertEquals("Netting cycle is not active: 1", exception.getMessage());
        verify(nettingCycleRepository).findById(1L);
        verify(nettingTransactionRepository, never()).findByNettingCycleId(anyLong());
    }

    @Test
    @DisplayName("Should handle empty transactions list")
    void shouldHandleEmptyTransactionsList() {
        // Given
        when(nettingCycleRepository.findById(1L)).thenReturn(Optional.of(testCycle));
        when(nettingTransactionRepository.findByNettingCycleId(1L)).thenReturn(Collections.emptyList());
        when(nettingCycleRepository.save(any(NettingCycle.class))).thenReturn(testCycle);

        // When
        List<NettingPosition> positions = nettingCalculationService.calculateNettingPositions(1L);

        // Then
        assertNotNull(positions);
        assertTrue(positions.isEmpty());
        verify(nettingCycleRepository).findById(1L);
        verify(nettingTransactionRepository).findByNettingCycleId(1L);
        verify(nettingPositionRepository, never()).saveAll(anyList());
        verify(nettingCycleRepository, times(2)).save(any(NettingCycle.class));
    }

    @Test
    @DisplayName("Should calculate multi-currency netting positions")
    void shouldCalculateMultiCurrencyNettingPositions() {
        // Given
        List<String> currencies = Arrays.asList("ZAR", "USD", "EUR");
        when(nettingCycleRepository.findById(1L)).thenReturn(Optional.of(testCycle));
        when(nettingTransactionRepository.findByNettingCycleIdAndCurrency(1L, "ZAR")).thenReturn(testTransactions);
        when(nettingTransactionRepository.findByNettingCycleIdAndCurrency(1L, "USD")).thenReturn(Collections.emptyList());
        when(nettingTransactionRepository.findByNettingCycleIdAndCurrency(1L, "EUR")).thenReturn(Collections.emptyList());
        when(nettingPositionRepository.saveAll(anyList())).thenReturn(Collections.emptyList());

        // When
        Map<String, List<NettingPosition>> currencyPositions = 
            nettingCalculationService.calculateMultiCurrencyNetting(1L, currencies);

        // Then
        assertNotNull(currencyPositions);
        assertEquals(3, currencyPositions.size());
        assertTrue(currencyPositions.containsKey("ZAR"));
        assertTrue(currencyPositions.containsKey("USD"));
        assertTrue(currencyPositions.containsKey("EUR"));
        verify(nettingCycleRepository).findById(1L);
        verify(nettingTransactionRepository).findByNettingCycleIdAndCurrency(1L, "ZAR");
        verify(nettingTransactionRepository).findByNettingCycleIdAndCurrency(1L, "USD");
        verify(nettingTransactionRepository).findByNettingCycleIdAndCurrency(1L, "EUR");
    }

    @Test
    @DisplayName("Should validate netting balance successfully")
    void shouldValidateNettingBalanceSuccessfully() {
        // Given
        NettingPosition position1 = NettingPosition.builder()
                .id(1L)
                .nettingCycleId(1L)
                .participantId("PARTICIPANT-A")
                .currency("ZAR")
                .netAmount(BigDecimal.valueOf(500.00))
                .debitAmount(BigDecimal.valueOf(1000.00))
                .creditAmount(BigDecimal.valueOf(500.00))
                .tenantId("TENANT1")
                .build();

        NettingPosition position2 = NettingPosition.builder()
                .id(2L)
                .nettingCycleId(1L)
                .participantId("PARTICIPANT-B")
                .currency("ZAR")
                .netAmount(BigDecimal.valueOf(-500.00))
                .debitAmount(BigDecimal.valueOf(500.00))
                .creditAmount(BigDecimal.valueOf(1000.00))
                .tenantId("TENANT1")
                .build();

        List<NettingPosition> positions = Arrays.asList(position1, position2);
        when(nettingPositionRepository.findByNettingCycleId(1L)).thenReturn(positions);

        // When
        boolean isBalanced = nettingCalculationService.validateNettingBalance(1L);

        // Then
        assertTrue(isBalanced);
        verify(nettingPositionRepository).findByNettingCycleId(1L);
    }

    @Test
    @DisplayName("Should detect unbalanced netting")
    void shouldDetectUnbalancedNetting() {
        // Given
        NettingPosition position1 = NettingPosition.builder()
                .id(1L)
                .nettingCycleId(1L)
                .participantId("PARTICIPANT-A")
                .currency("ZAR")
                .netAmount(BigDecimal.valueOf(1000.00))
                .debitAmount(BigDecimal.valueOf(1000.00))
                .creditAmount(BigDecimal.valueOf(2000.00))
                .tenantId("TENANT1")
                .build();

        NettingPosition position2 = NettingPosition.builder()
                .id(2L)
                .nettingCycleId(1L)
                .participantId("PARTICIPANT-B")
                .currency("ZAR")
                .netAmount(BigDecimal.valueOf(-500.00))
                .debitAmount(BigDecimal.valueOf(500.00))
                .creditAmount(BigDecimal.valueOf(1000.00))
                .tenantId("TENANT1")
                .build();

        List<NettingPosition> positions = Arrays.asList(position1, position2);
        when(nettingPositionRepository.findByNettingCycleId(1L)).thenReturn(positions);

        // When
        boolean isBalanced = nettingCalculationService.validateNettingBalance(1L);

        // Then
        assertFalse(isBalanced);
        verify(nettingPositionRepository).findByNettingCycleId(1L);
    }

    @Test
    @DisplayName("Should handle empty positions list for balance validation")
    void shouldHandleEmptyPositionsListForBalanceValidation() {
        // Given
        when(nettingPositionRepository.findByNettingCycleId(1L)).thenReturn(Collections.emptyList());

        // When
        boolean isBalanced = nettingCalculationService.validateNettingBalance(1L);

        // Then
        assertTrue(isBalanced);
        verify(nettingPositionRepository).findByNettingCycleId(1L);
    }

    @Test
    @DisplayName("Should get netting positions for cycle")
    void shouldGetNettingPositionsForCycle() {
        // Given
        List<NettingPosition> positions = Arrays.asList(
                NettingPosition.builder().id(1L).nettingCycleId(1L).participantId("PARTICIPANT-A").build(),
                NettingPosition.builder().id(2L).nettingCycleId(1L).participantId("PARTICIPANT-B").build()
        );
        when(nettingPositionRepository.findByNettingCycleId(1L)).thenReturn(positions);

        // When
        List<NettingPosition> result = nettingCalculationService.getNettingPositions(1L);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(nettingPositionRepository).findByNettingCycleId(1L);
    }

    @Test
    @DisplayName("Should get netting positions by participant")
    void shouldGetNettingPositionsByParticipant() {
        // Given
        List<NettingPosition> positions = Arrays.asList(
                NettingPosition.builder().id(1L).participantId("PARTICIPANT-A").tenantId("TENANT1").build()
        );
        when(nettingPositionRepository.findByParticipantIdAndTenantId("PARTICIPANT-A", "TENANT1"))
                .thenReturn(positions);

        // When
        List<NettingPosition> result = nettingCalculationService.getNettingPositionsByParticipant("PARTICIPANT-A", "TENANT1");

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(nettingPositionRepository).findByParticipantIdAndTenantId("PARTICIPANT-A", "TENANT1");
    }

    @Test
    @DisplayName("Should get netting positions by currency")
    void shouldGetNettingPositionsByCurrency() {
        // Given
        List<NettingPosition> positions = Arrays.asList(
                NettingPosition.builder().id(1L).currency("ZAR").tenantId("TENANT1").build()
        );
        when(nettingPositionRepository.findByCurrencyAndTenantId("ZAR", "TENANT1"))
                .thenReturn(positions);

        // When
        List<NettingPosition> result = nettingCalculationService.getNettingPositionsByCurrency("ZAR", "TENANT1");

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(nettingPositionRepository).findByCurrencyAndTenantId("ZAR", "TENANT1");
    }

    @Test
    @DisplayName("Should get netting positions summary")
    void shouldGetNettingPositionsSummary() {
        // Given
        NettingPosition position1 = NettingPosition.builder()
                .id(1L)
                .nettingCycleId(1L)
                .participantId("PARTICIPANT-A")
                .currency("ZAR")
                .netAmount(BigDecimal.valueOf(500.00))
                .debitAmount(BigDecimal.valueOf(1000.00))
                .creditAmount(BigDecimal.valueOf(1500.00))
                .transactionCount(5)
                .tenantId("TENANT1")
                .build();

        NettingPosition position2 = NettingPosition.builder()
                .id(2L)
                .nettingCycleId(1L)
                .participantId("PARTICIPANT-B")
                .currency("ZAR")
                .netAmount(BigDecimal.valueOf(-500.00))
                .debitAmount(BigDecimal.valueOf(1500.00))
                .creditAmount(BigDecimal.valueOf(1000.00))
                .transactionCount(3)
                .tenantId("TENANT1")
                .build();

        List<NettingPosition> positions = Arrays.asList(position1, position2);
        when(nettingPositionRepository.findByNettingCycleId(1L)).thenReturn(positions);

        // When
        Map<String, Object> summary = nettingCalculationService.getNettingPositionsSummary(1L);

        // Then
        assertNotNull(summary);
        assertEquals(2, summary.get("totalPositions"));
        assertEquals(2, summary.get("totalParticipants"));
        assertEquals(1, summary.get("totalCurrencies"));
        assertEquals(BigDecimal.valueOf(2500.00), summary.get("totalDebitAmount"));
        assertEquals(BigDecimal.valueOf(2500.00), summary.get("totalCreditAmount"));
        assertEquals(BigDecimal.ZERO, summary.get("totalNetAmount"));
        verify(nettingPositionRepository).findByNettingCycleId(1L);
    }

    @Test
    @DisplayName("Should handle exception during calculation")
    void shouldHandleExceptionDuringCalculation() {
        // Given
        when(nettingCycleRepository.findById(1L)).thenReturn(Optional.of(testCycle));
        when(nettingTransactionRepository.findByNettingCycleId(1L)).thenThrow(new RuntimeException("Database error"));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            nettingCalculationService.calculateNettingPositions(1L);
        });

        assertEquals("Failed to calculate netting positions", exception.getMessage());
        verify(nettingCycleRepository).findById(1L);
        verify(nettingTransactionRepository).findByNettingCycleId(1L);
        verify(nettingCycleRepository).save(any(NettingCycle.class));
    }
}
