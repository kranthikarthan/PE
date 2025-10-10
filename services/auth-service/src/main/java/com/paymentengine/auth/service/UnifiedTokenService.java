package com.paymentengine.auth.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.UUID;

/**
 * Unified Token Service
 * 
 * Provides a unified interface for token generation and validation,
 * supporting both JWT and JWS based on configuration.
 */
@Service
public class UnifiedTokenService {
    
    private static final Logger logger = LoggerFactory.getLogger(UnifiedTokenService.class);
    
    @Autowired
    private JwtTokenService jwtTokenService;
    
    @Autowired
    private JwsTokenService jwsTokenService;
    
    @Value("${app.auth.token-type:JWT}")
    private String defaultTokenType;
    
    /**
     * Generate access token using configured token type
     */
    public String generateAccessToken(UUID userId, String username, String email, Set<String> roles, Set<String> permissions) {
        return generateAccessToken(userId, username, email, roles, permissions, defaultTokenType);
    }
    
    /**
     * Generate access token using specified token type
     */
    public String generateAccessToken(UUID userId, String username, String email, Set<String> roles, Set<String> permissions, String tokenType) {
        logger.debug("Generating access token using type: {}", tokenType);
        
        switch (tokenType.toUpperCase()) {
            case "JWS":
                return jwsTokenService.generateAccessToken(userId, username, email, roles, permissions);
            case "JWT":
            default:
                return jwtTokenService.generateAccessToken(userId, username, email, roles, permissions);
        }
    }
    
    /**
     * Generate refresh token using configured token type
     */
    public String generateRefreshToken(UUID userId, String username) {
        return generateRefreshToken(userId, username, defaultTokenType);
    }
    
    /**
     * Generate refresh token using specified token type
     */
    public String generateRefreshToken(UUID userId, String username, String tokenType) {
        logger.debug("Generating refresh token using type: {}", tokenType);
        
        switch (tokenType.toUpperCase()) {
            case "JWS":
                return jwsTokenService.generateRefreshToken(userId, username);
            case "JWT":
            default:
                return jwtTokenService.generateRefreshToken(userId, username);
        }
    }
    
    /**
     * Validate token using auto-detection of token type
     */
    public boolean validateToken(String token) {
        // Try to detect token type and validate accordingly
        if (isJwsToken(token)) {
            return jwsTokenService.validateToken(token);
        } else {
            return jwtTokenService.validateToken(token);
        }
    }
    
    /**
     * Validate token using specified token type
     */
    public boolean validateToken(String token, String tokenType) {
        logger.debug("Validating token using type: {}", tokenType);
        
        switch (tokenType.toUpperCase()) {
            case "JWS":
                return jwsTokenService.validateToken(token);
            case "JWT":
            default:
                return jwtTokenService.validateToken(token);
        }
    }
    
    /**
     * Get username from token using auto-detection
     */
    public String getUsernameFromToken(String token) {
        if (isJwsToken(token)) {
            return jwsTokenService.getUsernameFromToken(token);
        } else {
            return jwtTokenService.getUsernameFromToken(token);
        }
    }
    
    /**
     * Get user ID from token using auto-detection
     */
    public UUID getUserIdFromToken(String token) {
        if (isJwsToken(token)) {
            return jwsTokenService.getUserIdFromToken(token);
        } else {
            return jwtTokenService.getUserIdFromToken(token);
        }
    }
    
    /**
     * Get email from token using auto-detection
     */
    public String getEmailFromToken(String token) {
        if (isJwsToken(token)) {
            return jwsTokenService.getEmailFromToken(token);
        } else {
            return jwtTokenService.getEmailFromToken(token);
        }
    }
    
    /**
     * Get roles from token using auto-detection
     */
    public Set<String> getRolesFromToken(String token) {
        if (isJwsToken(token)) {
            return jwsTokenService.getRolesFromToken(token);
        } else {
            return jwtTokenService.getRolesFromToken(token);
        }
    }
    
    /**
     * Get permissions from token using auto-detection
     */
    public Set<String> getPermissionsFromToken(String token) {
        if (isJwsToken(token)) {
            return jwsTokenService.getPermissionsFromToken(token);
        } else {
            return jwtTokenService.getPermissionsFromToken(token);
        }
    }
    
    /**
     * Get token type from token using auto-detection
     */
    public String getTokenType(String token) {
        if (isJwsToken(token)) {
            return jwsTokenService.getTokenType(token);
        } else {
            return jwtTokenService.getTokenType(token);
        }
    }
    
    /**
     * Check if token is expired using auto-detection
     */
    public boolean isTokenExpired(String token) {
        if (isJwsToken(token)) {
            return jwsTokenService.isTokenExpired(token);
        } else {
            return jwtTokenService.isTokenExpired(token);
        }
    }
    
    /**
     * Check if token is access token using auto-detection
     */
    public boolean isAccessToken(String token) {
        if (isJwsToken(token)) {
            return jwsTokenService.isAccessToken(token);
        } else {
            return jwtTokenService.isAccessToken(token);
        }
    }
    
    /**
     * Check if token is refresh token using auto-detection
     */
    public boolean isRefreshToken(String token) {
        if (isJwsToken(token)) {
            return jwsTokenService.isRefreshToken(token);
        } else {
            return jwtTokenService.isRefreshToken(token);
        }
    }
    
    /**
     * Get expiration time from token using auto-detection
     */
    public long getExpirationTimeFromToken(String token) {
        if (isJwsToken(token)) {
            return jwsTokenService.getExpirationTimeFromToken(token);
        } else {
            return jwtTokenService.getExpirationTimeFromToken(token);
        }
    }
    
    /**
     * Get time until expiration using auto-detection
     */
    public long getTimeUntilExpiration(String token) {
        if (isJwsToken(token)) {
            return jwsTokenService.getTimeUntilExpiration(token);
        } else {
            return jwtTokenService.getTimeUntilExpiration(token);
        }
    }
    
    /**
     * Get public key for external verification (JWS only)
     */
    public String getPublicKey() {
        return jwsTokenService.getPublicKey();
    }
    
    /**
     * Get default token type
     */
    public String getDefaultTokenType() {
        return defaultTokenType;
    }
    
    /**
     * Auto-detect if token is JWS based on structure
     */
    private boolean isJwsToken(String token) {
        try {
            // JWS tokens typically have 3 parts separated by dots
            String[] parts = token.split("\\.");
            if (parts.length == 3) {
                // Try to parse as JWS first
                jwsTokenService.getClaimsFromToken(token);
                return true;
            }
        } catch (Exception e) {
            // Not a JWS token, likely JWT
        }
        return false;
    }
}