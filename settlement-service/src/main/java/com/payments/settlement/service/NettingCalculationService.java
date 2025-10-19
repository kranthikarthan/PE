package com.payments.settlement.service;

import com.payments.settlement.domain.NettingCycle;
import com.payments.settlement.domain.NettingPosition;
import com.payments.settlement.domain.NettingTransaction;
import com.payments.settlement.repository.NettingCycleRepository;
import com.payments.settlement.repository.NettingPositionRepository;
import com.payments.settlement.repository.NettingTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for netting calculation operations.
 *
 * <p>This service provides comprehensive netting calculation capabilities including
 * position calculation, multi-currency netting, validation, and settlement coordination.
 * It implements advanced netting algorithms for efficient settlement processing.
 *
 * @since PE-408
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NettingCalculationService {
  
  private final NettingCycleRepository nettingCycleRepository;
  private final NettingPositionRepository nettingPositionRepository;
  private final NettingTransactionRepository nettingTransactionRepository;
  
  /**
   * Calculates netting positions for a given cycle.
   *
   * @param cycleId the netting cycle ID
   * @return list of calculated netting positions
   */
  @Transactional
  public List<NettingPosition> calculateNettingPositions(Long cycleId) {
    log.info("Starting netting calculation for cycle ID: {}", cycleId);
    
    NettingCycle cycle = nettingCycleRepository.findById(cycleId)
        .orElseThrow(() -> new IllegalArgumentException("Netting cycle not found: " + cycleId));
    
    if (!cycle.isActive()) {
      throw new IllegalStateException("Netting cycle is not active: " + cycleId);
    }
    
    // Start processing
    cycle.startProcessing();
    nettingCycleRepository.save(cycle);
    
    try {
      // Get all transactions for the cycle
      List<NettingTransaction> transactions = nettingTransactionRepository.findByNettingCycleId(cycleId);
      
      if (transactions.isEmpty()) {
        log.warn("No transactions found for cycle ID: {}", cycleId);
        return Collections.emptyList();
      }
      
      // Group transactions by participant and currency
      Map<String, Map<String, List<NettingTransaction>>> groupedTransactions = 
          groupTransactionsByParticipantAndCurrency(transactions);
      
      // Calculate positions for each participant and currency
      List<NettingPosition> positions = new ArrayList<>();
      
      for (Map.Entry<String, Map<String, List<NettingTransaction>>> participantEntry : groupedTransactions.entrySet()) {
        String participantId = participantEntry.getKey();
        Map<String, List<NettingTransaction>> currencyTransactions = participantEntry.getValue();
        
        for (Map.Entry<String, List<NettingTransaction>> currencyEntry : currencyTransactions.entrySet()) {
          String currency = currencyEntry.getKey();
          List<NettingTransaction> participantTransactions = currencyEntry.getValue();
          
          NettingPosition position = calculatePositionForParticipantAndCurrency(
              cycle, participantId, currency, participantTransactions);
          
          positions.add(position);
        }
      }
      
      // Save positions
      List<NettingPosition> savedPositions = nettingPositionRepository.saveAll(positions);
      
      // Update cycle with calculated amounts
      updateCycleWithCalculatedAmounts(cycle, savedPositions);
      
      // Complete the cycle
      cycle.complete();
      nettingCycleRepository.save(cycle);
      
      log.info("Netting calculation completed for cycle ID: {} with {} positions", 
          cycleId, savedPositions.size());
      
      return savedPositions;
      
    } catch (Exception e) {
      log.error("Error calculating netting positions for cycle ID: {}", cycleId, e);
      cycle.fail();
      nettingCycleRepository.save(cycle);
      throw new RuntimeException("Failed to calculate netting positions", e);
    }
  }
  
  /**
   * Calculates netting positions for multiple currencies.
   *
   * @param cycleId the netting cycle ID
   * @param currencies list of currencies to process
   * @return map of currency to list of positions
   */
  @Transactional
  public Map<String, List<NettingPosition>> calculateMultiCurrencyNetting(Long cycleId, List<String> currencies) {
    log.info("Starting multi-currency netting calculation for cycle ID: {} with currencies: {}", 
        cycleId, currencies);
    
    Map<String, List<NettingPosition>> currencyPositions = new HashMap<>();
    
    for (String currency : currencies) {
      List<NettingPosition> positions = calculateNettingPositionsForCurrency(cycleId, currency);
      currencyPositions.put(currency, positions);
    }
    
    log.info("Multi-currency netting calculation completed for cycle ID: {} with {} currencies", 
        cycleId, currencyPositions.size());
    
    return currencyPositions;
  }
  
  /**
   * Validates netting positions for balance.
   *
   * @param cycleId the netting cycle ID
   * @return true if balanced, false otherwise
   */
  @Transactional(readOnly = true)
  public boolean validateNettingBalance(Long cycleId) {
    log.info("Validating netting balance for cycle ID: {}", cycleId);
    
    List<NettingPosition> positions = nettingPositionRepository.findByNettingCycleId(cycleId);
    
    if (positions.isEmpty()) {
      log.warn("No positions found for cycle ID: {}", cycleId);
      return true;
    }
    
    // Group positions by currency
    Map<String, List<NettingPosition>> positionsByCurrency = positions.stream()
        .collect(Collectors.groupingBy(NettingPosition::getCurrency));
    
    boolean isBalanced = true;
    
    for (Map.Entry<String, List<NettingPosition>> entry : positionsByCurrency.entrySet()) {
      String currency = entry.getKey();
      List<NettingPosition> currencyPositions = entry.getValue();
      
      BigDecimal totalNetAmount = currencyPositions.stream()
          .map(NettingPosition::getNetAmount)
          .reduce(BigDecimal.ZERO, BigDecimal::add);
      
      if (totalNetAmount.compareTo(BigDecimal.ZERO) != 0) {
        log.error("Netting is not balanced for currency {}: total net amount = {}", 
            currency, totalNetAmount);
        isBalanced = false;
      } else {
        log.info("Netting is balanced for currency {}: total net amount = {}", 
            currency, totalNetAmount);
      }
    }
    
    log.info("Netting balance validation completed for cycle ID: {} - {}", 
        cycleId, isBalanced ? "BALANCED" : "NOT BALANCED");
    
    return isBalanced;
  }
  
  /**
   * Calculates netting positions for a specific currency.
   *
   * @param cycleId the netting cycle ID
   * @param currency the currency
   * @return list of positions for the currency
   */
  @Transactional
  public List<NettingPosition> calculateNettingPositionsForCurrency(Long cycleId, String currency) {
    log.info("Calculating netting positions for cycle ID: {} and currency: {}", cycleId, currency);
    
    NettingCycle cycle = nettingCycleRepository.findById(cycleId)
        .orElseThrow(() -> new IllegalArgumentException("Netting cycle not found: " + cycleId));
    
    // Get transactions for the specific currency
    List<NettingTransaction> transactions = nettingTransactionRepository
        .findByNettingCycleIdAndCurrency(cycleId, currency);
    
    if (transactions.isEmpty()) {
      log.warn("No transactions found for cycle ID: {} and currency: {}", cycleId, currency);
      return Collections.emptyList();
    }
    
    // Group transactions by participant
    Map<String, List<NettingTransaction>> participantTransactions = transactions.stream()
        .collect(Collectors.groupingBy(NettingTransaction::getDebtorParticipantId));
    
    // Calculate positions for each participant
    List<NettingPosition> positions = new ArrayList<>();
    
    for (Map.Entry<String, List<NettingTransaction>> entry : participantTransactions.entrySet()) {
      String participantId = entry.getKey();
      List<NettingTransaction> participantTransactionList = entry.getValue();
      
      NettingPosition position = calculatePositionForParticipantAndCurrency(
          cycle, participantId, currency, participantTransactionList);
      
      positions.add(position);
    }
    
    // Save positions
    List<NettingPosition> savedPositions = nettingPositionRepository.saveAll(positions);
    
    log.info("Netting positions calculated for cycle ID: {} and currency: {} with {} positions", 
        cycleId, currency, savedPositions.size());
    
    return savedPositions;
  }
  
  /**
   * Groups transactions by participant and currency.
   *
   * @param transactions the transactions to group
   * @return grouped transactions
   */
  private Map<String, Map<String, List<NettingTransaction>>> groupTransactionsByParticipantAndCurrency(
      List<NettingTransaction> transactions) {
    
    return transactions.stream()
        .collect(Collectors.groupingBy(
            NettingTransaction::getDebtorParticipantId,
            Collectors.groupingBy(NettingTransaction::getCurrency)
        ));
  }
  
  /**
   * Calculates position for a specific participant and currency.
   *
   * @param cycle the netting cycle
   * @param participantId the participant ID
   * @param currency the currency
   * @param transactions the transactions for the participant and currency
   * @return the calculated position
   */
  private NettingPosition calculatePositionForParticipantAndCurrency(
      NettingCycle cycle, String participantId, String currency, 
      List<NettingTransaction> transactions) {
    
    BigDecimal totalDebitAmount = BigDecimal.ZERO;
    BigDecimal totalCreditAmount = BigDecimal.ZERO;
    int debitCount = 0;
    int creditCount = 0;
    
    // Calculate totals from transactions
    for (NettingTransaction transaction : transactions) {
      if (transaction.isActive()) {
        BigDecimal amount = transaction.getAmount();
        
        if (transaction.isDebitForDebtor()) {
          totalDebitAmount = totalDebitAmount.add(amount);
          debitCount++;
        } else {
          totalCreditAmount = totalCreditAmount.add(amount);
          creditCount++;
        }
      }
    }
    
    // Create position
    NettingPosition position = NettingPosition.builder()
        .nettingCycleId(cycle.getId())
        .participantId(participantId)
        .currency(currency)
        .debitAmount(totalDebitAmount)
        .creditAmount(totalCreditAmount)
        .transactionCount(transactions.size())
        .debitCount(debitCount)
        .creditCount(creditCount)
        .priority(cycle.getPriority())
        .businessUnitId(cycle.getBusinessUnitId())
        .tenantId(cycle.getTenantId())
        .createdBy(cycle.getCreatedBy())
        .build();
    
    // Calculate net amount and position type
    position.updateAmounts(totalDebitAmount, totalCreditAmount);
    
    return position;
  }
  
  /**
   * Updates cycle with calculated amounts.
   *
   * @param cycle the netting cycle
   * @param positions the calculated positions
   */
  private void updateCycleWithCalculatedAmounts(NettingCycle cycle, List<NettingPosition> positions) {
    BigDecimal totalDebitAmount = positions.stream()
        .map(NettingPosition::getDebitAmount)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
    
    BigDecimal totalCreditAmount = positions.stream()
        .map(NettingPosition::getCreditAmount)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
    
    int totalTransactionCount = positions.stream()
        .mapToInt(NettingPosition::getTransactionCount)
        .sum();
    
    int participantCount = (int) positions.stream()
        .map(NettingPosition::getParticipantId)
        .distinct()
        .count();
    
    cycle.updateAmounts(totalDebitAmount, totalCreditAmount);
    cycle.setTransactionCount(totalTransactionCount);
    cycle.setParticipantCount(participantCount);
    
    // Set currency if not already set
    if (cycle.getCurrency() == null && !positions.isEmpty()) {
      cycle.setCurrency(positions.get(0).getCurrency());
    }
  }
  
  /**
   * Gets netting positions for a cycle.
   *
   * @param cycleId the netting cycle ID
   * @return list of positions
   */
  @Transactional(readOnly = true)
  public List<NettingPosition> getNettingPositions(Long cycleId) {
    return nettingPositionRepository.findByNettingCycleId(cycleId);
  }
  
  /**
   * Gets netting positions for a participant.
   *
   * @param participantId the participant ID
   * @param tenantId the tenant ID
   * @return list of positions
   */
  @Transactional(readOnly = true)
  public List<NettingPosition> getNettingPositionsByParticipant(String participantId, String tenantId) {
    return nettingPositionRepository.findByParticipantIdAndTenantId(participantId, tenantId);
  }
  
  /**
   * Gets netting positions for a currency.
   *
   * @param currency the currency
   * @param tenantId the tenant ID
   * @return list of positions
   */
  @Transactional(readOnly = true)
  public List<NettingPosition> getNettingPositionsByCurrency(String currency, String tenantId) {
    return nettingPositionRepository.findByCurrencyAndTenantId(currency, tenantId);
  }
  
  /**
   * Gets netting positions summary for a cycle.
   *
   * @param cycleId the netting cycle ID
   * @return summary map
   */
  @Transactional(readOnly = true)
  public Map<String, Object> getNettingPositionsSummary(Long cycleId) {
    List<NettingPosition> positions = getNettingPositions(cycleId);
    
    Map<String, Object> summary = new HashMap<>();
    summary.put("totalPositions", positions.size());
    summary.put("totalParticipants", positions.stream()
        .map(NettingPosition::getParticipantId)
        .distinct()
        .count());
    summary.put("totalCurrencies", positions.stream()
        .map(NettingPosition::getCurrency)
        .distinct()
        .count());
    summary.put("totalDebitAmount", positions.stream()
        .map(NettingPosition::getDebitAmount)
        .reduce(BigDecimal.ZERO, BigDecimal::add));
    summary.put("totalCreditAmount", positions.stream()
        .map(NettingPosition::getCreditAmount)
        .reduce(BigDecimal.ZERO, BigDecimal::add));
    summary.put("totalNetAmount", positions.stream()
        .map(NettingPosition::getNetAmount)
        .reduce(BigDecimal.ZERO, BigDecimal::add));
    
    return summary;
  }
}
