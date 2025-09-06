package com.paymentengine.middleware.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * JWT token provider for generating and validating access and refresh tokens
 */
@Component
public class JwtTokenProvider {
    
    private static final Logger logger = LoggerFactory.getLogger(JwtTokenProvider.class);
    
    private final SecretKey jwtSecret;
    private final int accessTokenExpirationSeconds;
    private final int refreshTokenExpirationSeconds;
    
    public JwtTokenProvider(
            @Value("${jwt.secret:default-secret-key}") String secret,
            @Value("${jwt.access-token-expiration:3600}") int accessTokenExpirationSeconds,
            @Value("${jwt.refresh-token-expiration:86400}") int refreshTokenExpirationSeconds) {
        this.jwtSecret = Keys.hmacShaKeyFor(secret.getBytes());
        this.accessTokenExpirationSeconds = accessTokenExpirationSeconds;
        this.refreshTokenExpirationSeconds = refreshTokenExpirationSeconds;
    }
    
    /**
     * Generate access token for authenticated user
     */
    public String generateAccessToken(Map<String, Object> user) {
        Instant now = Instant.now();
        Instant expiration = now.plus(accessTokenExpirationSeconds, ChronoUnit.SECONDS);
        
        @SuppressWarnings("unchecked")
        List<String> permissions = (List<String>) user.getOrDefault("permissions", List.of());
        
        return Jwts.builder()
            .setSubject((String) user.get("username"))
            .claim("userId", user.get("id"))
            .claim("email", user.get("email"))
            .claim("firstName", user.get("firstName"))
            .claim("lastName", user.get("lastName"))
            .claim("permissions", permissions)
            .claim("roles", user.get("roles"))
            .claim("tokenType", "access")
            .setIssuedAt(Date.from(now))
            .setExpiration(Date.from(expiration))
            .setIssuer("payment-engine-auth")
            .setAudience("payment-engine-api")
            .signWith(jwtSecret, SignatureAlgorithm.HS256)
            .compact();
    }
    
    /**
     * Generate refresh token for authenticated user
     */
    public String generateRefreshToken(Map<String, Object> user) {
        Instant now = Instant.now();
        Instant expiration = now.plus(refreshTokenExpirationSeconds, ChronoUnit.SECONDS);
        
        return Jwts.builder()
            .setSubject((String) user.get("username"))
            .claim("userId", user.get("id"))
            .claim("tokenType", "refresh")
            .setIssuedAt(Date.from(now))
            .setExpiration(Date.from(expiration))
            .setIssuer("payment-engine-auth")
            .setAudience("payment-engine-auth")
            .signWith(jwtSecret, SignatureAlgorithm.HS256)
            .compact();
    }
    
    /**
     * Validate access token
     */
    public boolean isAccessTokenValid(String token) {
        try {
            Claims claims = parseToken(token);
            return "access".equals(claims.get("tokenType")) && !isTokenExpired(claims);
        } catch (Exception e) {
            logger.debug("Access token validation failed: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Validate refresh token
     */
    public boolean isRefreshTokenValid(String token) {
        try {
            Claims claims = parseToken(token);
            return "refresh".equals(claims.get("tokenType")) && !isTokenExpired(claims);
        } catch (Exception e) {
            logger.debug("Refresh token validation failed: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Extract user information from access token
     */
    public Map<String, Object> extractUserFromAccessToken(String token) {
        Claims claims = parseToken(token);
        
        if (!"access".equals(claims.get("tokenType"))) {
            throw new RuntimeException("Not an access token");
        }
        
        Map<String, Object> user = new HashMap<>();
        user.put("id", claims.get("userId"));
        user.put("username", claims.getSubject());
        user.put("email", claims.get("email"));
        user.put("firstName", claims.get("firstName"));
        user.put("lastName", claims.get("lastName"));
        user.put("permissions", claims.get("permissions"));
        user.put("roles", claims.get("roles"));
        
        return user;
    }
    
    /**
     * Extract user information from refresh token
     */
    public Map<String, Object> extractUserFromRefreshToken(String token) {
        Claims claims = parseToken(token);
        
        if (!"refresh".equals(claims.get("tokenType"))) {
            throw new RuntimeException("Not a refresh token");
        }
        
        // For refresh tokens, we need to get full user info from the database
        String userId = (String) claims.get("userId");
        
        // This would typically call the user service to get current user information
        // For now, return basic info from token
        Map<String, Object> user = new HashMap<>();
        user.put("id", userId);
        user.put("username", claims.getSubject());
        
        return user;
    }
    
    /**
     * Get token expiration date
     */
    public Instant getTokenExpiration(String token) {
        Claims claims = parseToken(token);
        return claims.getExpiration().toInstant();
    }
    
    /**
     * Extract user ID from token
     */
    public String extractUserId(String token) {
        try {
            Claims claims = parseToken(token);
            return (String) claims.get("userId");
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * Extract username from token
     */
    public String extractUsername(String token) {
        try {
            Claims claims = parseToken(token);
            return claims.getSubject();
        } catch (Exception e) {
            return null;
        }
    }
    
    public int getAccessTokenExpirationSeconds() {
        return accessTokenExpirationSeconds;
    }
    
    public int getRefreshTokenExpirationSeconds() {
        return refreshTokenExpirationSeconds;
    }
    
    // Private helper methods
    
    private Claims parseToken(String token) {
        try {
            return Jwts.parserBuilder()
                .setSigningKey(jwtSecret)
                .build()
                .parseClaimsJws(token)
                .getBody();
        } catch (ExpiredJwtException e) {
            logger.debug("Token expired: {}", e.getMessage());
            throw new RuntimeException("Token expired", e);
        } catch (UnsupportedJwtException e) {
            logger.debug("Unsupported token: {}", e.getMessage());
            throw new RuntimeException("Unsupported token", e);
        } catch (MalformedJwtException e) {
            logger.debug("Malformed token: {}", e.getMessage());
            throw new RuntimeException("Malformed token", e);
        } catch (SignatureException e) {
            logger.debug("Invalid token signature: {}", e.getMessage());
            throw new RuntimeException("Invalid token signature", e);
        } catch (IllegalArgumentException e) {
            logger.debug("Empty token: {}", e.getMessage());
            throw new RuntimeException("Empty token", e);
        }
    }
    
    private boolean isTokenExpired(Claims claims) {
        return claims.getExpiration().before(new Date());
    }
}