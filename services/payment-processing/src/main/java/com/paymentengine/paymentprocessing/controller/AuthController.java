package com.paymentengine.paymentprocessing.controller;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Simple authentication controller for testing purposes
 * In production, this should be replaced with a proper OAuth2/OIDC implementation
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Value("${jwt.secret:your-secret-key-here-change-in-production}")
    private String jwtSecret;

    @Value("${jwt.access-token-expiration:3600}")
    private long jwtExpiration;

    /**
     * Generate a test JWT token for tenant management
     */
    @PostMapping("/test-token")
    public ResponseEntity<Map<String, String>> generateTestToken(
            @RequestParam(defaultValue = "admin") String username,
            @RequestParam(defaultValue = "tenant:manage,tenant:read,tenant:export,tenant:import") String authorities) {
        
        try {
            SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
            
            List<String> authorityList = List.of(authorities.split(","));
            
            String token = Jwts.builder()
                .setSubject(username)
                .claim("authorities", authorityList)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration * 1000))
                .signWith(key)
                .compact();
            
            Map<String, String> response = new HashMap<>();
            response.put("token", token);
            response.put("tokenType", "Bearer");
            response.put("expiresIn", String.valueOf(jwtExpiration));
            response.put("authorities", authorities);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to generate token: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    /**
     * Generate a test JWT token with admin role
     */
    @PostMapping("/admin-token")
    public ResponseEntity<Map<String, String>> generateAdminToken() {
        return generateTestToken("admin", "ROLE_ADMIN,tenant:manage,tenant:read,tenant:export,tenant:import");
    }

    /**
     * Generate a test JWT token with read-only permissions
     */
    @PostMapping("/readonly-token")
    public ResponseEntity<Map<String, String>> generateReadOnlyToken() {
        return generateTestToken("readonly-user", "tenant:read");
    }
}