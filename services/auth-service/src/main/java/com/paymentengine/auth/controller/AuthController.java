package com.paymentengine.auth.controller;

import com.paymentengine.auth.dto.LoginRequest;
import com.paymentengine.auth.dto.LoginResponse;
import com.paymentengine.auth.dto.RefreshTokenRequest;
import com.paymentengine.auth.dto.UserRegistrationRequest;
import com.paymentengine.auth.entity.User;
import com.paymentengine.auth.service.AuthService;
import com.paymentengine.auth.service.UserService;
import io.micrometer.core.annotation.Timed;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth")
@CrossOrigin(origins = "*")
public class AuthController {
    
    @Autowired
    private AuthService authService;
    
    @Autowired
    private UserService userService;
    
    @PostMapping("/login")
    @Timed(value = "auth.login", description = "Time taken for user login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        try {
            LoginResponse response = authService.authenticate(request.getUsername(), request.getPassword());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PostMapping("/refresh")
    @Timed(value = "auth.refresh", description = "Time taken for token refresh")
    public ResponseEntity<LoginResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        try {
            LoginResponse response = authService.refreshToken(request.getRefreshToken());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PostMapping("/register")
    @Timed(value = "auth.register", description = "Time taken for user registration")
    public ResponseEntity<User> register(@Valid @RequestBody UserRegistrationRequest request) {
        try {
            User user = authService.registerUser(request);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PostMapping("/logout")
    @Timed(value = "auth.logout", description = "Time taken for user logout")
    public ResponseEntity<Void> logout(@RequestHeader("Authorization") String token) {
        try {
            authService.logout(token);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/validate")
    @Timed(value = "auth.validate", description = "Time taken for token validation")
    public ResponseEntity<Boolean> validateToken(@RequestHeader("Authorization") String token) {
        try {
            boolean isValid = authService.validateToken(token);
            return ResponseEntity.ok(isValid);
        } catch (Exception e) {
            return ResponseEntity.ok(false);
        }
    }
    
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN') or @authService.isCurrentUser(#userId)")
    @Timed(value = "auth.get_user", description = "Time taken to get user details")
    public ResponseEntity<User> getUser(@PathVariable UUID userId) {
        try {
            return userService.findById(userId)
                    .map(user -> ResponseEntity.ok(user))
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    @Timed(value = "auth.list_users", description = "Time taken to list users")
    public ResponseEntity<List<User>> getUsers() {
        try {
            List<User> users = userService.findAll();
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PutMapping("/user/{userId}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    @Timed(value = "auth.activate_user", description = "Time taken to activate user")
    public ResponseEntity<Void> activateUser(@PathVariable UUID userId) {
        try {
            userService.activateUser(userId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PutMapping("/user/{userId}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    @Timed(value = "auth.deactivate_user", description = "Time taken to deactivate user")
    public ResponseEntity<Void> deactivateUser(@PathVariable UUID userId) {
        try {
            userService.deactivateUser(userId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PutMapping("/user/{userId}/unlock")
    @PreAuthorize("hasRole('ADMIN')")
    @Timed(value = "auth.unlock_user", description = "Time taken to unlock user")
    public ResponseEntity<Void> unlockUser(@PathVariable UUID userId) {
        try {
            userService.unlockUser(userId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PostMapping("/user/{userId}/change-password")
    @PreAuthorize("hasRole('ADMIN') or @authService.isCurrentUser(#userId)")
    @Timed(value = "auth.change_password", description = "Time taken to change password")
    public ResponseEntity<Void> changePassword(@PathVariable UUID userId, 
                                             @RequestParam String oldPassword, 
                                             @RequestParam String newPassword) {
        try {
            boolean success = userService.changePassword(userId, oldPassword, newPassword);
            return success ? ResponseEntity.ok().build() : ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/health")
    @Timed(value = "auth.health", description = "Time taken for auth service health check")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Auth Service is healthy");
    }
}