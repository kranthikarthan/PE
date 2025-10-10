package com.paymentengine.shared.dto;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Shared DTO for enhanced token operations
 * Used across all services for consistent token data transfer
 */
public class EnhancedTokenDTO {
    
    private String token;
    private String tokenType; // JWT, JWS
    private String algorithm; // HS256, HS384, HS512, RS256, RS384, RS512
    private String issuer;
    private String audience;
    private String subject;
    private LocalDateTime issuedAt;
    private LocalDateTime expiresAt;
    private Map<String, Object> claims;
    private Map<String, Object> metadata;
    
    // Constructors
    public EnhancedTokenDTO() {}
    
    public EnhancedTokenDTO(String token, String tokenType, String algorithm) {
        this.token = token;
        this.tokenType = tokenType;
        this.algorithm = algorithm;
    }
    
    // Getters and Setters
    public String getToken() {
        return token;
    }
    
    public void setToken(String token) {
        this.token = token;
    }
    
    public String getTokenType() {
        return tokenType;
    }
    
    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }
    
    public String getAlgorithm() {
        return algorithm;
    }
    
    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }
    
    public String getIssuer() {
        return issuer;
    }
    
    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }
    
    public String getAudience() {
        return audience;
    }
    
    public void setAudience(String audience) {
        this.audience = audience;
    }
    
    public String getSubject() {
        return subject;
    }
    
    public void setSubject(String subject) {
        this.subject = subject;
    }
    
    public LocalDateTime getIssuedAt() {
        return issuedAt;
    }
    
    public void setIssuedAt(LocalDateTime issuedAt) {
        this.issuedAt = issuedAt;
    }
    
    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }
    
    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }
    
    public Map<String, Object> getClaims() {
        return claims;
    }
    
    public void setClaims(Map<String, Object> claims) {
        this.claims = claims;
    }
    
    public Map<String, Object> getMetadata() {
        return metadata;
    }
    
    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }
}