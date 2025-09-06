package com.paymentengine.middleware.controller;

import com.paymentengine.middleware.dto.LoginRequest;
import com.paymentengine.middleware.dto.LoginResponse;
import com.paymentengine.middleware.dto.RefreshTokenRequest;
import com.paymentengine.middleware.service.AuthService;
import io.micrometer.core.annotation.Timed;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Authentication controller for login, logout, and token management
 */
@RestController
@RequestMapping("/api/v1/auth")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthController {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    
    private final AuthService authService;
    
    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }
    
    /**
     * User login endpoint
     */
    @PostMapping("/login")
    @Timed(value = "auth.login", description = "Time taken for user login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {
        
        logger.info("Login attempt for user: {}", request.getUsername());
        
        try {
            String clientIp = getClientIpAddress(httpRequest);
            String userAgent = httpRequest.getHeader("User-Agent");
            
            LoginResponse response = authService.login(
                request.getUsername(), 
                request.getPassword(),
                clientIp,
                userAgent
            );
            
            logger.info("Login successful for user: {}", request.getUsername());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.warn("Login failed for user {}: {}", request.getUsername(), e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Refresh token endpoint
     */
    @PostMapping("/refresh")
    @Timed(value = "auth.refresh", description = "Time taken for token refresh")
    public ResponseEntity<LoginResponse> refreshToken(
            @Valid @RequestBody RefreshTokenRequest request) {
        
        logger.debug("Token refresh attempt");
        
        try {
            LoginResponse response = authService.refreshToken(request.getRefreshToken());
            logger.debug("Token refresh successful");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.warn("Token refresh failed: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * User logout endpoint
     */
    @PostMapping("/logout")
    @PreAuthorize("hasAuthority('user')")
    @Timed(value = "auth.logout", description = "Time taken for user logout")
    public ResponseEntity<Map<String, String>> logout(HttpServletRequest request) {
        
        String userId = request.getHeader("X-User-ID");
        String username = request.getHeader("X-Username");
        
        logger.info("Logout request for user: {} ({})", username, userId);
        
        try {
            String token = extractTokenFromRequest(request);
            authService.logout(token, userId);
            
            logger.info("Logout successful for user: {}", username);
            return ResponseEntity.ok(Map.of("message", "Logout successful"));
            
        } catch (Exception e) {
            logger.warn("Logout failed for user {}: {}", username, e.getMessage());
            return ResponseEntity.ok(Map.of("message", "Logout completed"));
        }
    }
    
    /**
     * Get current user information
     */
    @GetMapping("/me")
    @PreAuthorize("hasAuthority('user')")
    @Timed(value = "auth.me", description = "Time taken to get user info")
    public ResponseEntity<Map<String, Object>> getCurrentUser(HttpServletRequest request) {
        
        String userId = request.getHeader("X-User-ID");
        String username = request.getHeader("X-Username");
        
        logger.debug("Get current user info for: {} ({})", username, userId);
        
        try {
            Map<String, Object> userInfo = authService.getCurrentUserInfo(userId);
            return ResponseEntity.ok(userInfo);
            
        } catch (Exception e) {
            logger.error("Failed to get user info for {}: {}", username, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Validate token endpoint (for internal use)
     */
    @PostMapping("/validate")
    @Timed(value = "auth.validate", description = "Time taken for token validation")
    public ResponseEntity<Map<String, Object>> validateToken(
            @RequestBody Map<String, String> request) {
        
        String token = request.get("token");
        
        try {
            Map<String, Object> validation = authService.validateToken(token);
            return ResponseEntity.ok(validation);
            
        } catch (Exception e) {
            logger.warn("Token validation failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "valid", false,
                "error", e.getMessage()
            ));
        }
    }
    
    /**
     * Change password endpoint
     */
    @PostMapping("/change-password")
    @PreAuthorize("hasAuthority('user')")
    @Timed(value = "auth.change_password", description = "Time taken for password change")
    public ResponseEntity<Map<String, String>> changePassword(
            @RequestBody Map<String, String> request,
            HttpServletRequest httpRequest) {
        
        String userId = httpRequest.getHeader("X-User-ID");
        String username = httpRequest.getHeader("X-Username");
        String currentPassword = request.get("currentPassword");
        String newPassword = request.get("newPassword");
        
        logger.info("Password change request for user: {}", username);
        
        try {
            authService.changePassword(userId, currentPassword, newPassword);
            
            logger.info("Password changed successfully for user: {}", username);
            return ResponseEntity.ok(Map.of("message", "Password changed successfully"));
            
        } catch (Exception e) {
            logger.warn("Password change failed for user {}: {}", username, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Health check for auth service
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
            "status", "UP",
            "service", "auth-service",
            "timestamp", java.time.Instant.now().toString()
        ));
    }
    
    // Helper methods
    
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
    
    private String extractTokenFromRequest(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }
}