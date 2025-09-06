package com.paymentengine.gateway.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * JWT Authentication Manager for validating JWT tokens
 */
@Component
public class JwtAuthenticationManager implements ReactiveAuthenticationManager {
    
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationManager.class);
    
    private final SecretKey jwtSecret;
    
    public JwtAuthenticationManager(@Value("${jwt.secret:default-secret-key}") String secret) {
        this.jwtSecret = Keys.hmacShaKeyFor(secret.getBytes());
    }
    
    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {
        String token = authentication.getCredentials().toString();
        
        return Mono.fromCallable(() -> validateToken(token))
            .cast(Authentication.class)
            .onErrorResume(e -> {
                logger.warn("JWT validation failed: {}", e.getMessage());
                return Mono.empty();
            });
    }
    
    private Authentication validateToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                .setSigningKey(jwtSecret)
                .build()
                .parseClaimsJws(token)
                .getBody();
            
            // Check if token is expired
            if (claims.getExpiration().before(new Date())) {
                throw new RuntimeException("Token expired");
            }
            
            String username = claims.getSubject();
            String userId = claims.get("userId", String.class);
            
            @SuppressWarnings("unchecked")
            List<String> permissions = claims.get("permissions", List.class);
            
            List<SimpleGrantedAuthority> authorities = permissions != null 
                ? permissions.stream()
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList())
                : List.of();
            
            // Create custom user principal with additional claims
            UserPrincipal userPrincipal = new UserPrincipal(
                userId,
                username,
                claims.get("email", String.class),
                claims.get("firstName", String.class),
                claims.get("lastName", String.class),
                authorities
            );
            
            return new UsernamePasswordAuthenticationToken(userPrincipal, null, authorities);
            
        } catch (Exception e) {
            logger.error("Error validating JWT token: {}", e.getMessage());
            throw new RuntimeException("Invalid token", e);
        }
    }
    
    /**
     * Validate token without authentication (for utility purposes)
     */
    public boolean isTokenValid(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                .setSigningKey(jwtSecret)
                .build()
                .parseClaimsJws(token)
                .getBody();
            
            return !claims.getExpiration().before(new Date());
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Extract user ID from token
     */
    public String extractUserId(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                .setSigningKey(jwtSecret)
                .build()
                .parseClaimsJws(token)
                .getBody();
            
            return claims.get("userId", String.class);
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * Extract username from token
     */
    public String extractUsername(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                .setSigningKey(jwtSecret)
                .build()
                .parseClaimsJws(token)
                .getBody();
            
            return claims.getSubject();
        } catch (Exception e) {
            return null;
        }
    }
}