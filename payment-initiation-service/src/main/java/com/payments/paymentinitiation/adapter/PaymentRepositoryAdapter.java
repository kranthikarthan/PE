package com.payments.paymentinitiation.adapter;

import com.payments.domain.payment.Payment;
import com.payments.domain.payment.PaymentStatus;
import com.payments.domain.shared.PaymentId;
import com.payments.paymentinitiation.entity.PaymentEntity;
import com.payments.paymentinitiation.mapper.PaymentMapper;
import com.payments.paymentinitiation.port.PaymentRepositoryPort;
import com.payments.paymentinitiation.repository.PaymentJpaRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Payment Repository Adapter
 *
 * <p>Implements the PaymentRepositoryPort using JPA repository following the Ports and Adapters
 * pattern
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentRepositoryAdapter implements PaymentRepositoryPort {

  private final PaymentJpaRepository jpaRepository;
  private final PaymentMapper paymentMapper;

  @Override
  @Transactional
  public Payment save(Payment payment) {
    log.debug("Saving payment: {}", payment.getId());

    PaymentEntity entity = paymentMapper.toEntity(payment);
    PaymentEntity savedEntity = jpaRepository.saveAndFlush(entity);

    log.debug("Payment saved successfully: {}", savedEntity.getPaymentId());
    return paymentMapper.toDomain(savedEntity);
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<Payment> findById(PaymentId paymentId) {
    log.debug("Finding payment by ID: {}", paymentId);

    return jpaRepository.findById(paymentId).map(paymentMapper::toDomain);
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<Payment> findByIdAndTenantId(PaymentId paymentId, String tenantId) {
    log.debug("Finding payment by ID and tenant: {}, {}", paymentId, tenantId);

    return jpaRepository.findByIdAndTenantId(paymentId, tenantId).map(paymentMapper::toDomain);
  }

  @Override
  @Transactional(readOnly = true)
  public Page<Payment> findByTenantId(String tenantId, Pageable pageable) {
    log.debug("Finding payments by tenant: {}", tenantId);

    return jpaRepository.findByTenantId(tenantId, pageable).map(paymentMapper::toDomain);
  }

  @Override
  @Transactional(readOnly = true)
  public Page<Payment> findByTenantIdAndBusinessUnitId(
      String tenantId, String businessUnitId, Pageable pageable) {
    log.debug("Finding payments by tenant and business unit: {}, {}", tenantId, businessUnitId);

    return jpaRepository
        .findByTenantIdAndBusinessUnitId(tenantId, businessUnitId, pageable)
        .map(paymentMapper::toDomain);
  }

  @Override
  @Transactional(readOnly = true)
  public List<Payment> findByStatusAndTenantId(PaymentStatus status, String tenantId) {
    log.debug("Finding payments by status and tenant: {}, {}", status, tenantId);

    return jpaRepository.findByStatusAndTenantId(status, tenantId).stream()
        .map(paymentMapper::toDomain)
        .toList();
  }

  @Override
  @Transactional(readOnly = true)
  public Page<Payment> findByTenantIdAndDateRange(
      String tenantId, Instant startDate, Instant endDate, Pageable pageable) {
    log.debug(
        "Finding payments by tenant and date range: {}, {} to {}", tenantId, startDate, endDate);

    return jpaRepository
        .findByTenantIdAndDateRange(tenantId, startDate, endDate, pageable)
        .map(paymentMapper::toDomain);
  }

  @Override
  @Transactional(readOnly = true)
  public long countByStatusAndTenantId(PaymentStatus status, String tenantId) {
    log.debug("Counting payments by status and tenant: {}, {}", status, tenantId);

    return jpaRepository.countByStatusAndTenantId(status, tenantId);
  }

  @Override
  @Transactional(readOnly = true)
  public Page<Payment> findBySourceAccountAndTenantId(
      String sourceAccount, String tenantId, Pageable pageable) {
    log.debug("Finding payments by source account and tenant: {}, {}", sourceAccount, tenantId);

    return jpaRepository
        .findBySourceAccountAndTenantId(sourceAccount, tenantId, pageable)
        .map(paymentMapper::toDomain);
  }

  @Override
  @Transactional(readOnly = true)
  public Page<Payment> findByDestinationAccountAndTenantId(
      String destinationAccount, String tenantId, Pageable pageable) {
    log.debug(
        "Finding payments by destination account and tenant: {}, {}", destinationAccount, tenantId);

    return jpaRepository
        .findByDestinationAccountAndTenantId(destinationAccount, tenantId, pageable)
        .map(paymentMapper::toDomain);
  }

  @Override
  @Transactional(readOnly = true)
  public boolean existsByIdAndTenantId(PaymentId paymentId, String tenantId) {
    log.debug("Checking if payment exists by ID and tenant: {}, {}", paymentId, tenantId);

    return jpaRepository.existsByIdAndTenantId(paymentId, tenantId);
  }

  @Override
  @Transactional
  public void deleteById(PaymentId paymentId) {
    log.debug("Deleting payment by ID: {}", paymentId);

    jpaRepository.deleteById(paymentId);
    log.debug("Payment deleted successfully: {}", paymentId);
  }
}
