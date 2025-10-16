package com.payments.transactionprocessing.service;

import com.payments.domain.shared.*;
import com.payments.domain.transaction.*;
import com.payments.transactionprocessing.entity.TransactionEntity;
import com.payments.transactionprocessing.exception.TransactionNotFoundException;
import com.payments.transactionprocessing.repository.TransactionRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionService {

  private final TransactionRepository transactionRepository;
  private final LedgerService ledgerService;
  private final TransactionEventService eventService;
  private final BalanceValidationService balanceValidationService;

  @Transactional
  public Transaction createTransaction(
      TransactionId id,
      TenantContext tenantContext,
      PaymentId paymentId,
      AccountNumber debitAccount,
      AccountNumber creditAccount,
      Money amount,
      TransactionType transactionType) {

    log.info(
        "Creating transaction {} for payment {} with amount {}",
        id.getValue(),
        paymentId.getValue(),
        amount);

    // Create domain transaction
    Transaction transaction =
        Transaction.create(
            id, tenantContext, paymentId, debitAccount, creditAccount, amount, transactionType);

    // Validate double-entry invariants before saving
    if (!balanceValidationService.validateDoubleEntryInvariants(transaction)) {
      throw new IllegalArgumentException("Transaction violates double-entry bookkeeping rules");
    }

    // Validate sufficient balance for debit account
    if (!balanceValidationService.validateSufficientBalance(debitAccount, tenantContext, amount)) {
      throw new IllegalArgumentException(
          "Insufficient balance in debit account " + debitAccount.getValue());
    }

    // Save to database
    TransactionEntity entity = TransactionEntity.fromDomain(transaction);
    TransactionEntity savedEntity = transactionRepository.save(entity);

    // Save ledger entries
    ledgerService.createLedgerEntries(transaction);

    // Validate account balances after ledger entries are created
    if (!balanceValidationService.validateAccountBalances(transaction)) {
      throw new IllegalStateException(
          "Account balance validation failed after transaction creation");
    }

    // Save events
    eventService.saveTransactionEvents(transaction);

    log.info("Transaction {} created successfully with validated balances", id.getValue());
    return savedEntity.toDomain();
  }

  @Transactional
  public Transaction startProcessing(TransactionId id, TenantContext tenantContext) {
    log.info("Starting processing for transaction {}", id.getValue());

    TransactionEntity entity = findTransactionByIdAndTenant(id, tenantContext);
    Transaction transaction = entity.toDomain();

    transaction.startProcessing();

    // Update entity
    entity.setStatus(transaction.getStatus());
    TransactionEntity savedEntity = transactionRepository.save(entity);

    // Save events
    eventService.saveTransactionEvents(transaction);

    log.info("Transaction {} processing started", id.getValue());
    return savedEntity.toDomain();
  }

  @Transactional
  public Transaction markCleared(
      TransactionId id,
      TenantContext tenantContext,
      String clearingSystem,
      String clearingReference) {
    log.info(
        "Marking transaction {} as cleared with system {} and reference {}",
        id.getValue(),
        clearingSystem,
        clearingReference);

    TransactionEntity entity = findTransactionByIdAndTenant(id, tenantContext);
    Transaction transaction = entity.toDomain();

    transaction.markCleared(clearingSystem, clearingReference);

    // Update entity
    entity.setStatus(transaction.getStatus());
    entity.setClearingSystem(clearingSystem);
    entity.setClearingReference(clearingReference);
    TransactionEntity savedEntity = transactionRepository.save(entity);

    // Save events
    eventService.saveTransactionEvents(transaction);

    log.info("Transaction {} marked as cleared", id.getValue());
    return savedEntity.toDomain();
  }

  @Transactional
  public Transaction completeTransaction(TransactionId id, TenantContext tenantContext) {
    log.info("Completing transaction {}", id.getValue());

    TransactionEntity entity = findTransactionByIdAndTenant(id, tenantContext);
    Transaction transaction = entity.toDomain();

    transaction.complete();

    // Update entity
    entity.setStatus(transaction.getStatus());
    entity.setCompletedAt(transaction.getCompletedAt());
    TransactionEntity savedEntity = transactionRepository.save(entity);

    // Save events
    eventService.saveTransactionEvents(transaction);

    log.info("Transaction {} completed successfully", id.getValue());
    return savedEntity.toDomain();
  }

  @Transactional
  public Transaction failTransaction(TransactionId id, TenantContext tenantContext, String reason) {
    log.info("Failing transaction {} with reason: {}", id.getValue(), reason);

    TransactionEntity entity = findTransactionByIdAndTenant(id, tenantContext);
    Transaction transaction = entity.toDomain();

    transaction.fail(reason);

    // Update entity
    entity.setStatus(transaction.getStatus());
    entity.setFailureReason(reason);
    TransactionEntity savedEntity = transactionRepository.save(entity);

    // Save events
    eventService.saveTransactionEvents(transaction);

    log.info("Transaction {} failed with reason: {}", id.getValue(), reason);
    return savedEntity.toDomain();
  }

  @Cacheable(value = "transactions", key = "#id.value + '_' + #tenantContext.tenantId")
  public Optional<Transaction> findById(TransactionId id, TenantContext tenantContext) {
    log.debug("Finding transaction {} for tenant {}", id.getValue(), tenantContext.getTenantId());

    return transactionRepository
        .findByIdAndTenantContext(id, tenantContext)
        .map(TransactionEntity::toDomain);
  }

  public List<Transaction> findByPaymentId(PaymentId paymentId, TenantContext tenantContext) {
    log.debug(
        "Finding transactions for payment {} and tenant {}",
        paymentId.getValue(),
        tenantContext.getTenantId());

    return transactionRepository.findByPaymentIdAndTenantContext(paymentId, tenantContext).stream()
        .map(TransactionEntity::toDomain)
        .collect(Collectors.toList());
  }

  public List<Transaction> findByStatus(TransactionStatus status, TenantContext tenantContext) {
    log.debug(
        "Finding transactions with status {} for tenant {}", status, tenantContext.getTenantId());

    return transactionRepository.findByStatusAndTenantContext(status, tenantContext).stream()
        .map(TransactionEntity::toDomain)
        .collect(Collectors.toList());
  }

  public List<Transaction> findByDateRange(
      TenantContext tenantContext, Instant startDate, Instant endDate) {
    log.debug(
        "Finding transactions for tenant {} between {} and {}",
        tenantContext.getTenantId(),
        startDate,
        endDate);

    return transactionRepository
        .findByTenantContextAndDateRange(tenantContext, startDate, endDate)
        .stream()
        .map(TransactionEntity::toDomain)
        .collect(Collectors.toList());
  }

  public Optional<Transaction> findByClearingReference(
      String clearingSystem, String clearingReference) {
    log.debug(
        "Finding transaction by clearing system {} and reference {}",
        clearingSystem,
        clearingReference);

    return transactionRepository
        .findByClearingSystemAndReference(clearingSystem, clearingReference)
        .map(TransactionEntity::toDomain);
  }

  private TransactionEntity findTransactionByIdAndTenant(
      TransactionId id, TenantContext tenantContext) {
    return transactionRepository
        .findByIdAndTenantContext(id, tenantContext)
        .orElseThrow(
            () ->
                new TransactionNotFoundException(
                    "Transaction not found: "
                        + id.getValue()
                        + " for tenant: "
                        + tenantContext.getTenantId()));
  }
}
