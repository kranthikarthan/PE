package com.paymentengine.auth.repository;

import com.paymentengine.auth.entity.OAuthClient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OAuthClientRepository extends JpaRepository<OAuthClient, UUID> {
    
    Optional<OAuthClient> findByClientId(String clientId);
    
    List<OAuthClient> findByIsActive(Boolean isActive);
    
    List<OAuthClient> findByClientNameContaining(String clientName);
    
    @Query("SELECT c FROM OAuthClient c WHERE c.clientId = :clientId AND c.isActive = true")
    Optional<OAuthClient> findActiveByClientId(@Param("clientId") String clientId);
    
    @Query("SELECT COUNT(c) FROM OAuthClient c WHERE c.isActive = :isActive")
    Long countByIsActive(@Param("isActive") Boolean isActive);
    
    boolean existsByClientId(String clientId);
}