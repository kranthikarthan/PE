package com.paymentengine.auth.service;

import com.paymentengine.auth.dto.LoginResponse;
import com.paymentengine.auth.dto.UserRegistrationRequest;
import com.paymentengine.auth.entity.OAuthToken;
import com.paymentengine.auth.entity.User;
import com.paymentengine.auth.repository.OAuthTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class AuthService {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private JwtTokenService jwtTokenService;
    
    @Autowired
    private UnifiedTokenService unifiedTokenService;
    
    @Autowired
    private OAuthTokenRepository oAuthTokenRepository;
    
    public LoginResponse authenticate(String username, String password) {
        // Find user by username or email
        User user = userService.findByUsernameOrEmail(username, username)
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));
        
        // Check if user is active and not locked
        if (!userService.isUserActive(user.getId())) {
            userService.recordFailedLogin(username);
            throw new RuntimeException("Account is locked or inactive");
        }
        
        // Verify password
        if (!userService.isPasswordValid(user, password)) {
            userService.recordFailedLogin(username);
            throw new RuntimeException("Invalid credentials");
        }
        
        // Record successful login
        userService.recordSuccessfulLogin(user.getId());
        
        // Generate tokens
        Set<String> roles = user.getRoles().stream()
                .map(role -> role.getName())
                .collect(Collectors.toSet());
        
        Set<String> permissions = user.getRoles().stream()
                .flatMap(role -> role.getPermissions().stream())
                .map(permission -> permission.getName())
                .collect(Collectors.toSet());
        
        String accessToken = unifiedTokenService.generateAccessToken(
                user.getId(), user.getUsername(), user.getEmail(), roles, permissions);
        
        String refreshToken = unifiedTokenService.generateRefreshToken(user.getId(), user.getUsername());
        
        // Store tokens in database
        OAuthToken oAuthToken = new OAuthToken(
                "payment-engine-client", user.getId(), "access", accessToken, 
                refreshToken, String.join(",", permissions), 
                LocalDateTime.now().plusSeconds(3600));
        oAuthTokenRepository.save(oAuthToken);
        
        return new LoginResponse(
                accessToken, refreshToken, 3600L, user.getId(), 
                user.getUsername(), user.getEmail(), roles, permissions);
    }
    
    public LoginResponse refreshToken(String refreshToken) {
        // Validate refresh token
        if (!jwtTokenService.validateToken(refreshToken) || !jwtTokenService.isRefreshToken(refreshToken)) {
            throw new RuntimeException("Invalid refresh token");
        }
        
        // Get user from token
        UUID userId = jwtTokenService.getUserIdFromToken(refreshToken);
        String username = jwtTokenService.getUsernameFromToken(refreshToken);
        
        User user = userService.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Check if user is still active
        if (!userService.isUserActive(user.getId())) {
            throw new RuntimeException("Account is locked or inactive");
        }
        
        // Generate new tokens
        Set<String> roles = user.getRoles().stream()
                .map(role -> role.getName())
                .collect(Collectors.toSet());
        
        Set<String> permissions = user.getRoles().stream()
                .flatMap(role -> role.getPermissions().stream())
                .map(permission -> permission.getName())
                .collect(Collectors.toSet());
        
        String newAccessToken = jwtTokenService.generateAccessToken(
                user.getId(), user.getUsername(), user.getEmail(), roles, permissions);
        
        String newRefreshToken = jwtTokenService.generateRefreshToken(user.getId(), user.getUsername());
        
        // Revoke old tokens
        oAuthTokenRepository.findByRefreshToken(refreshToken)
                .ifPresent(token -> token.revoke("Token refreshed"));
        
        // Store new tokens
        OAuthToken oAuthToken = new OAuthToken(
                "payment-engine-client", user.getId(), "access", newAccessToken, 
                newRefreshToken, String.join(",", permissions), 
                LocalDateTime.now().plusSeconds(3600));
        oAuthTokenRepository.save(oAuthToken);
        
        return new LoginResponse(
                newAccessToken, newRefreshToken, 3600L, user.getId(), 
                user.getUsername(), user.getEmail(), roles, permissions);
    }
    
    public User registerUser(UserRegistrationRequest request) {
        // Check if username or email already exists
        if (userService.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists");
        }
        
        if (userService.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }
        
        // Create user
        User user = userService.createUser(
                request.getUsername(), request.getEmail(), request.getPassword(),
                request.getFirstName(), request.getLastName());
        
        return user;
    }
    
    public void logout(String token) {
        // Extract token from Authorization header
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        
        // Revoke token
        oAuthTokenRepository.findByAccessToken(token)
                .ifPresent(oAuthToken -> oAuthToken.revoke("User logout"));
    }
    
    public boolean validateToken(String token) {
        // Extract token from Authorization header
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        
        // Validate JWT token
        if (!jwtTokenService.validateToken(token) || !jwtTokenService.isAccessToken(token)) {
            return false;
        }
        
        // Check if token is revoked in database
        return oAuthTokenRepository.findByAccessToken(token)
                .map(tokenEntity -> !tokenEntity.getIsRevoked() && !tokenEntity.isExpired())
                .orElse(false);
    }
    
    public boolean isCurrentUser(UUID userId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            try {
                UUID currentUserId = jwtTokenService.getUserIdFromToken(authentication.getName());
                return currentUserId.equals(userId);
            } catch (Exception e) {
                return false;
            }
        }
        return false;
    }
}