package com.paymentengine.paymentprocessing.repository;

import com.paymentengine.paymentprocessing.entity.ClearingSystemEndpointEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for clearing system endpoint entities
 */
@Repository
public interface ClearingSystemEndpointRepository extends JpaRepository<ClearingSystemEndpointEntity, String> {
    
    /**
     * Find endpoints by clearing system ID
     */
    List<ClearingSystemEndpointEntity> findByClearingSystemIdAndIsActiveTrueOrderByPriorityAsc(String clearingSystemId);
    
    /**
     * Find endpoints by clearing system ID and endpoint type
     */
    List<ClearingSystemEndpointEntity> findByClearingSystemIdAndEndpointTypeAndIsActiveTrueOrderByPriorityAsc(
            String clearingSystemId, String endpointType);
    
    /**
     * Find endpoints by clearing system ID and message type
     */
    List<ClearingSystemEndpointEntity> findByClearingSystemIdAndMessageTypeAndIsActiveTrueOrderByPriorityAsc(
            String clearingSystemId, String messageType);
    
    /**
     * Find endpoints by clearing system ID, endpoint type, and message type
     */
    List<ClearingSystemEndpointEntity> findByClearingSystemIdAndEndpointTypeAndMessageTypeAndIsActiveTrueOrderByPriorityAsc(
            String clearingSystemId, String endpointType, String messageType);
    
    /**
     * Find the primary endpoint for a clearing system, endpoint type, and message type
     */
    Optional<ClearingSystemEndpointEntity> findFirstByClearingSystemIdAndEndpointTypeAndMessageTypeAndIsActiveTrueOrderByPriorityAsc(
            String clearingSystemId, String endpointType, String messageType);
    
    /**
     * Find endpoints by clearing system code (using join)
     */
    @Query("SELECT e FROM ClearingSystemEndpointEntity e " +
           "JOIN ClearingSystemEntity cs ON e.clearingSystemId = cs.id " +
           "WHERE cs.code = :clearingSystemCode AND e.isActive = true " +
           "ORDER BY e.priority ASC")
    List<ClearingSystemEndpointEntity> findByClearingSystemCode(@Param("clearingSystemCode") String clearingSystemCode);
    
    /**
     * Find endpoints by clearing system code, endpoint type, and message type
     */
    @Query("SELECT e FROM ClearingSystemEndpointEntity e " +
           "JOIN ClearingSystemEntity cs ON e.clearingSystemId = cs.id " +
           "WHERE cs.code = :clearingSystemCode AND e.endpointType = :endpointType " +
           "AND e.messageType = :messageType AND e.isActive = true " +
           "ORDER BY e.priority ASC")
    List<ClearingSystemEndpointEntity> findByClearingSystemCodeAndEndpointTypeAndMessageType(
            @Param("clearingSystemCode") String clearingSystemCode,
            @Param("endpointType") String endpointType,
            @Param("messageType") String messageType);
    
    /**
     * Find the primary endpoint for a clearing system code, endpoint type, and message type
     */
    @Query("SELECT e FROM ClearingSystemEndpointEntity e " +
           "JOIN ClearingSystemEntity cs ON e.clearingSystemId = cs.id " +
           "WHERE cs.code = :clearingSystemCode AND e.endpointType = :endpointType " +
           "AND e.messageType = :messageType AND e.isActive = true " +
           "ORDER BY e.priority ASC")
    Optional<ClearingSystemEndpointEntity> findFirstByClearingSystemCodeAndEndpointTypeAndMessageType(
            @Param("clearingSystemCode") String clearingSystemCode,
            @Param("endpointType") String endpointType,
            @Param("messageType") String messageType);
    
    /**
     * Check if endpoint exists for clearing system, endpoint type, and message type
     */
    boolean existsByClearingSystemIdAndEndpointTypeAndMessageTypeAndIsActiveTrue(
            String clearingSystemId, String endpointType, String messageType);
}