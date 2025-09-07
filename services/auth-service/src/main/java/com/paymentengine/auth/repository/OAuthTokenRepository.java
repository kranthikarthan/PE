package com.paymentengine.auth.repository;

import com.paymentengine.auth.entity.OAuthToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OAuthTokenRepository extends JpaRepository<OAuthToken, UUID> {
    
    Optional<OAuthToken> findByAccessToken(String accessToken);
    
    Optional<OAuthToken> findByRefreshToken(String refreshToken);
    
    List<OAuthToken> findByClientId(String clientId);
    
    List<OAuthToken> findByUserId(UUID userId);
    
    List<OAuthToken> findByIsRevoked(Boolean isRevoked);
    
    @Query("SELECT t FROM OAuthToken t WHERE t.clientId = :clientId AND t.userId = :userId AND t.isRevoked = false")
    List<OAuthToken> findActiveByClientIdAndUserId(@Param("clientId") String clientId, @Param("userId") UUID userId);
    
    @Query("SELECT t FROM OAuthToken t WHERE t.expiresAt < :now AND t.isRevoked = false")
    List<OAuthToken> findExpiredTokens(@Param("now") LocalDateTime now);
    
    @Query("SELECT t FROM OAuthToken t WHERE t.issuedAt < :cutoffDate")
    List<OAuthToken> findTokensOlderThan(@Param("cutoffDate") LocalDateTime cutoffDate);
    
    @Query("SELECT COUNT(t) FROM OAuthToken t WHERE t.clientId = :clientId AND t.isRevoked = false")
    Long countActiveTokensByClientId(@Param("clientId") String clientId);
    
    @Query("SELECT COUNT(t) FROM OAuthToken t WHERE t.userId = :userId AND t.isRevoked = false")
    Long countActiveTokensByUserId(@Param("userId") UUID userId);
    
    void deleteByExpiresAtBefore(LocalDateTime cutoffDate);
    
    void deleteByIssuedAtBefore(LocalDateTime cutoffDate);
}