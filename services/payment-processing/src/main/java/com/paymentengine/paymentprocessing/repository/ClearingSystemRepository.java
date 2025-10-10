package com.paymentengine.paymentprocessing.repository;

import com.paymentengine.paymentprocessing.entity.ClearingSystemEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for clearing system entities
 */
@Repository
public interface ClearingSystemRepository extends JpaRepository<ClearingSystemEntity, String> {
    
    /**
     * Find clearing system by code
     */
    Optional<ClearingSystemEntity> findByCode(String code);
    
    /**
     * Find all active clearing systems
     */
    List<ClearingSystemEntity> findByIsActiveTrue();
    
    /**
     * Find clearing systems by country code
     */
    List<ClearingSystemEntity> findByCountryCodeAndIsActiveTrue(String countryCode);
    
    /**
     * Find clearing systems by currency
     */
    List<ClearingSystemEntity> findByCurrencyAndIsActiveTrue(String currency);
    
    /**
     * Find clearing systems by processing mode
     */
    List<ClearingSystemEntity> findByProcessingModeAndIsActiveTrue(String processingMode);
    
    /**
     * Check if clearing system code exists
     */
    boolean existsByCode(String code);
    
    /**
     * Find clearing systems that support a specific payment type
     */
    @Query("SELECT cs FROM ClearingSystemEntity cs WHERE cs.isActive = true " +
           "AND KEY(cs.supportedPaymentTypes) = :paymentType")
    List<ClearingSystemEntity> findBySupportedPaymentType(@Param("paymentType") String paymentType);
    
    /**
     * Find clearing systems that support a specific local instrument
     */
    @Query("SELECT cs FROM ClearingSystemEntity cs WHERE cs.isActive = true " +
           "AND KEY(cs.supportedLocalInstruments) = :localInstrumentCode")
    List<ClearingSystemEntity> findBySupportedLocalInstrument(@Param("localInstrumentCode") String localInstrumentCode);
    
    /**
     * Find clearing systems that support a specific message type
     */
    @Query("SELECT cs FROM ClearingSystemEntity cs WHERE cs.isActive = true " +
           "AND KEY(cs.supportedMessageTypes) = :messageType")
    List<ClearingSystemEntity> findBySupportedMessageType(@Param("messageType") String messageType);
}