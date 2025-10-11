package com.paymentengine.auth.service;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.Set;

@Service
public class JwtTokenService {
    
    @Value("${app.jwt.secret:mySecretKey}")
    private String jwtSecret;
    
    @Value("${app.jwt.expiration:3600}")
    private int jwtExpiration;
    
    @Value("${app.jwt.refresh-expiration:86400}")
    private int refreshExpiration;
    
    @Value("${app.jwt.issuer:payment-engine-auth}")
    private String jwtIssuer;
    
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }
    
    public String generateAccessToken(UUID userId, String username, String email, Set<String> roles, Set<String> permissions) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId.toString());
        claims.put("username", username);
        claims.put("email", email);
        claims.put("roles", roles);
        claims.put("permissions", permissions);
        claims.put("type", "access");
        
        return createToken(claims, username, jwtExpiration);
    }
    
    public String generateRefreshToken(UUID userId, String username) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId.toString());
        claims.put("username", username);
        claims.put("type", "refresh");
        
        return createToken(claims, username, refreshExpiration);
    }
    
    private String createToken(Map<String, Object> claims, String subject, int expiration) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expirationTime = now.plusSeconds(expiration);
        
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuer(jwtIssuer)
                .setIssuedAt(Date.from(now.atZone(ZoneId.systemDefault()).toInstant()))
                .setExpiration(Date.from(expirationTime.atZone(ZoneId.systemDefault()).toInstant()))
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }
    
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
    
    public Claims getClaimsFromToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
    
    public String getUsernameFromToken(String token) {
        return getClaimsFromToken(token).getSubject();
    }
    
    public UUID getUserIdFromToken(String token) {
        String userIdStr = getClaimsFromToken(token).get("userId", String.class);
        return UUID.fromString(userIdStr);
    }
    
    public String getEmailFromToken(String token) {
        return getClaimsFromToken(token).get("email", String.class);
    }
    
    @SuppressWarnings("unchecked")
    public Set<String> getRolesFromToken(String token) {
        return (Set<String>) getClaimsFromToken(token).get("roles");
    }
    
    @SuppressWarnings("unchecked")
    public Set<String> getPermissionsFromToken(String token) {
        return (Set<String>) getClaimsFromToken(token).get("permissions");
    }
    
    public String getTokenType(String token) {
        return getClaimsFromToken(token).get("type", String.class);
    }
    
    public boolean isTokenExpired(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            return claims.getExpiration().before(new Date());
        } catch (JwtException | IllegalArgumentException e) {
            return true;
        }
    }
    
    public boolean isAccessToken(String token) {
        try {
            return "access".equals(getTokenType(token));
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
    
    public boolean isRefreshToken(String token) {
        try {
            return "refresh".equals(getTokenType(token));
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
    
    public long getExpirationTimeFromToken(String token) {
        return getClaimsFromToken(token).getExpiration().getTime();
    }
    
    public long getTimeUntilExpiration(String token) {
        long expirationTime = getExpirationTimeFromToken(token);
        long currentTime = System.currentTimeMillis();
        return expirationTime - currentTime;
    }
}