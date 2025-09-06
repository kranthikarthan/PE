package com.paymentengine.corebanking.repository;

import com.paymentengine.corebanking.entity.PaymentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for PaymentType entity operations
 */
@Repository
public interface PaymentTypeRepository extends JpaRepository<PaymentType, UUID> {
    
    /**
     * Find payment type by code
     */
    Optional<PaymentType> findByCode(String code);
    
    /**
     * Find all active payment types
     */
    List<PaymentType> findByIsActiveTrue();
    
    /**
     * Find payment types by synchronous flag
     */
    List<PaymentType> findByIsSynchronous(Boolean isSynchronous);
    
    /**
     * Find active payment types by synchronous flag
     */
    @Query("SELECT pt FROM PaymentType pt WHERE pt.isSynchronous = :isSynchronous AND pt.isActive = true")
    List<PaymentType> findActivePaymentTypesBySynchronous(@Param("isSynchronous") Boolean isSynchronous);
    
    /**
     * Find payment types by name pattern
     */
    @Query("SELECT pt FROM PaymentType pt WHERE pt.name LIKE %:namePattern% AND pt.isActive = true")
    List<PaymentType> findByNameContaining(@Param("namePattern") String namePattern);
    
    /**
     * Check if payment type code exists
     */
    boolean existsByCode(String code);
    
    /**
     * Count active payment types
     */
    long countByIsActiveTrue();
    
    /**
     * Find payment types that support amount range
     */
    @Query("SELECT pt FROM PaymentType pt WHERE " +
           "pt.isActive = true AND " +
           "(pt.minAmount IS NULL OR pt.minAmount <= :amount) AND " +
           "(pt.maxAmount IS NULL OR pt.maxAmount >= :amount)")
    List<PaymentType> findPaymentTypesSupportingAmount(@Param("amount") java.math.BigDecimal amount);
}