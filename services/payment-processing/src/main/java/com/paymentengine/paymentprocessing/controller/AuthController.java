package com.paymentengine.paymentprocessing.controller;

import com.paymentengine.paymentprocessing.dto.LoginRequest;
import com.paymentengine.paymentprocessing.dto.LoginResponse;
import com.paymentengine.paymentprocessing.dto.RefreshTokenRequest;
import com.paymentengine.paymentprocessing.service.ResilientAuthService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Authentication controller delegating to the dedicated auth service.
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final ResilientAuthService resilientAuthService;

    public AuthController(ResilientAuthService resilientAuthService) {
        this.resilientAuthService = resilientAuthService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        logger.info("Proxying login request for user: {}", request.getUsername());
        try {
            LoginResponse response = resilientAuthService.authenticate(request);
            return ResponseEntity.ok(response);
        } catch (Exception ex) {
            logger.error("Login failed for user {}: {}", request.getUsername(), ex.getMessage());
            return ResponseEntity.status(401).build();
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        logger.debug("Refreshing access token");
        try {
            LoginResponse response = resilientAuthService.refreshToken(request.getRefreshToken());
            return ResponseEntity.ok(response);
        } catch (Exception ex) {
            logger.error("Token refresh failed: {}", ex.getMessage());
            return ResponseEntity.status(401).build();
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader("Authorization") String authorizationHeader) {
        logger.debug("Logging out current session");
        try {
            resilientAuthService.logout(authorizationHeader);
            return ResponseEntity.ok().build();
        } catch (Exception ex) {
            logger.error("Logout failed: {}", ex.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/validate")
    public ResponseEntity<Boolean> validateToken(@RequestHeader("Authorization") String authorizationHeader) {
        try {
            boolean valid = resilientAuthService.validateToken(authorizationHeader);
            return ResponseEntity.ok(valid);
        } catch (Exception ex) {
            logger.error("Token validation failed: {}", ex.getMessage());
            return ResponseEntity.ok(false);
        }
    }
}