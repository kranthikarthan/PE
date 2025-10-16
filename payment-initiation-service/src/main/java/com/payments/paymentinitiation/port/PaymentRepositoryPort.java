package com.payments.paymentinitiation.port;

import com.payments.domain.payment.Payment;
import com.payments.domain.shared.PaymentId;
import com.payments.domain.payment.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Repository Port for Payment Aggregate
 * 
 * Defines the contract for payment persistence operations
 * following the Ports and Adapters pattern
 */
public interface PaymentRepositoryPort {

    /**
     * Save payment aggregate
     * 
     * @param payment Payment aggregate
     * @return Saved payment aggregate
     */
    Payment save(Payment payment);

    /**
     * Find payment by ID
     * 
     * @param paymentId Payment ID
     * @return Optional payment aggregate
     */
    Optional<Payment> findById(PaymentId paymentId);

    /**
     * Find payment by ID and tenant context
     * 
     * @param paymentId Payment ID
     * @param tenantId Tenant ID
     * @return Optional payment aggregate
     */
    Optional<Payment> findByIdAndTenantId(PaymentId paymentId, String tenantId);

    /**
     * Find payments by tenant ID
     * 
     * @param tenantId Tenant ID
     * @param pageable Pagination parameters
     * @return Page of payment aggregates
     */
    Page<Payment> findByTenantId(String tenantId, Pageable pageable);

    /**
     * Find payments by tenant and business unit
     * 
     * @param tenantId Tenant ID
     * @param businessUnitId Business unit ID
     * @param pageable Pagination parameters
     * @return Page of payment aggregates
     */
    Page<Payment> findByTenantIdAndBusinessUnitId(String tenantId, String businessUnitId, Pageable pageable);

    /**
     * Find payments by status and tenant
     * 
     * @param status Payment status
     * @param tenantId Tenant ID
     * @return List of payment aggregates
     */
    List<Payment> findByStatusAndTenantId(PaymentStatus status, String tenantId);

    /**
     * Find payments by date range and tenant
     * 
     * @param tenantId Tenant ID
     * @param startDate Start date
     * @param endDate End date
     * @param pageable Pagination parameters
     * @return Page of payment aggregates
     */
    Page<Payment> findByTenantIdAndDateRange(String tenantId, Instant startDate, Instant endDate, Pageable pageable);

    /**
     * Count payments by status and tenant
     * 
     * @param status Payment status
     * @param tenantId Tenant ID
     * @return Count of payments
     */
    long countByStatusAndTenantId(PaymentStatus status, String tenantId);

    /**
     * Find payments by source account and tenant
     * 
     * @param sourceAccount Source account number
     * @param tenantId Tenant ID
     * @param pageable Pagination parameters
     * @return Page of payment aggregates
     */
    Page<Payment> findBySourceAccountAndTenantId(String sourceAccount, String tenantId, Pageable pageable);

    /**
     * Find payments by destination account and tenant
     * 
     * @param destinationAccount Destination account number
     * @param tenantId Tenant ID
     * @param pageable Pagination parameters
     * @return Page of payment aggregates
     */
    Page<Payment> findByDestinationAccountAndTenantId(String destinationAccount, String tenantId, Pageable pageable);

    /**
     * Check if payment exists by ID and tenant
     * 
     * @param paymentId Payment ID
     * @param tenantId Tenant ID
     * @return true if exists, false otherwise
     */
    boolean existsByIdAndTenantId(PaymentId paymentId, String tenantId);

    /**
     * Delete payment by ID
     * 
     * @param paymentId Payment ID
     */
    void deleteById(PaymentId paymentId);
}
