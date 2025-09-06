package com.paymentengine.corebanking.controller;

import com.paymentengine.corebanking.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Authentication controller for core banking service
 * Handles credential validation and user management
 */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    
    @Autowired
    public AuthController(UserService userService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }
    
    /**
     * Validate user credentials (internal endpoint for middleware)
     */
    @PostMapping("/validate-credentials")
    public ResponseEntity<Map<String, Object>> validateCredentials(
            @RequestParam String username,
            @RequestParam String password) {
        
        logger.debug("Validating credentials for user: {}", username);
        
        try {
            Map<String, Object> user = userService.findByUsername(username);
            
            if (user == null) {
                logger.debug("User not found: {}", username);
                return ResponseEntity.notFound().build();
            }
            
            String storedPasswordHash = (String) user.get("passwordHash");
            
            if (!passwordEncoder.matches(password, storedPasswordHash)) {
                logger.debug("Password mismatch for user: {}", username);
                return ResponseEntity.badRequest().build();
            }
            
            // Remove sensitive data before returning
            user.remove("passwordHash");
            
            // Add permissions and roles
            user.put("permissions", userService.getUserPermissions((String) user.get("id")));
            user.put("roles", userService.getUserRoles((String) user.get("id")));
            
            logger.debug("Credentials validated successfully for user: {}", username);
            return ResponseEntity.ok(user);
            
        } catch (Exception e) {
            logger.error("Error validating credentials for user {}: {}", username, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
}