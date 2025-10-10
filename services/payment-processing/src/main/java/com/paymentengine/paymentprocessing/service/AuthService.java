package com.paymentengine.paymentprocessing.service;

import com.paymentengine.paymentprocessing.client.CoreBankingClient;
import com.paymentengine.paymentprocessing.dto.LoginResponse;
import com.paymentengine.paymentprocessing.security.JwtTokenProvider;
import com.paymentengine.shared.constants.KafkaTopics;
import com.paymentengine.shared.event.AuditEvent;
import com.paymentengine.shared.util.EventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Authentication service for user login, token management, and security operations
 */
@Service
public class AuthService {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);
    private static final String BLACKLISTED_TOKEN_PREFIX = "blacklisted_token:";
    private static final String USER_SESSION_PREFIX = "user_session:";
    private static final String LOGIN_ATTEMPTS_PREFIX = "login_attempts:";
    
    private final CoreBankingClient coreBankingClient;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final RedisTemplate<String, Object> redisTemplate;
    private final EventPublisher eventPublisher;
    
    @Autowired
    public AuthService(
            CoreBankingClient coreBankingClient,
            JwtTokenProvider jwtTokenProvider,
            PasswordEncoder passwordEncoder,
            RedisTemplate<String, Object> redisTemplate,
            EventPublisher eventPublisher) {
        this.coreBankingClient = coreBankingClient;
        this.jwtTokenProvider = jwtTokenProvider;
        this.passwordEncoder = passwordEncoder;
        this.redisTemplate = redisTemplate;
        this.eventPublisher = eventPublisher;
    }
    
    /**
     * Authenticate user and generate JWT tokens
     */
    public LoginResponse login(String username, String password, String clientIp, String userAgent) {
        logger.info("Processing login for user: {}", username);
        
        try {
            // Check login attempts rate limiting
            if (isLoginRateLimited(username, clientIp)) {
                publishSecurityEvent("LOGIN_RATE_LIMITED", username, clientIp, "Too many login attempts");
                throw new RuntimeException("Too many login attempts. Please try again later.");
            }
            
            // Validate credentials with core banking service
            Map<String, Object> user = validateCredentials(username, password);
            
            if (user == null) {
                recordFailedLoginAttempt(username, clientIp);
                publishSecurityEvent("LOGIN_FAILURE", username, clientIp, "Invalid credentials");
                throw new RuntimeException("Invalid username or password");
            }
            
            // Generate tokens
            String accessToken = jwtTokenProvider.generateAccessToken(user);
            String refreshToken = jwtTokenProvider.generateRefreshToken(user);
            
            // Store session information
            storeUserSession(user, refreshToken, clientIp, userAgent);
            
            // Clear failed login attempts
            clearFailedLoginAttempts(username, clientIp);
            
            // Publish successful login event
            publishSecurityEvent("LOGIN_SUCCESS", username, clientIp, "Successful login");
            
            // Create response
            LoginResponse response = new LoginResponse();
            response.setAccessToken(accessToken);
            response.setRefreshToken(refreshToken);
            response.setTokenType("Bearer");
            response.setExpiresIn(jwtTokenProvider.getAccessTokenExpirationSeconds());
            response.setUser(user);
            
            logger.info("Login successful for user: {}", username);
            return response;
            
        } catch (Exception e) {
            logger.error("Login failed for user {}: {}", username, e.getMessage());
            recordFailedLoginAttempt(username, clientIp);
            throw e;
        }
    }
    
    /**
     * Refresh access token using refresh token
     */
    public LoginResponse refreshToken(String refreshToken) {
        logger.debug("Processing token refresh");
        
        try {
            if (!jwtTokenProvider.isRefreshTokenValid(refreshToken)) {
                throw new RuntimeException("Invalid or expired refresh token");
            }
            
            Map<String, Object> user = jwtTokenProvider.extractUserFromRefreshToken(refreshToken);
            
            // Verify user is still active
            if (!isUserActive(user)) {
                throw new RuntimeException("User account is not active");
            }
            
            // Generate new tokens
            String newAccessToken = jwtTokenProvider.generateAccessToken(user);
            String newRefreshToken = jwtTokenProvider.generateRefreshToken(user);
            
            // Update session
            updateUserSession(user, newRefreshToken);
            
            // Invalidate old refresh token
            invalidateRefreshToken(refreshToken);
            
            LoginResponse response = new LoginResponse();
            response.setAccessToken(newAccessToken);
            response.setRefreshToken(newRefreshToken);
            response.setTokenType("Bearer");
            response.setExpiresIn(jwtTokenProvider.getAccessTokenExpirationSeconds());
            response.setUser(user);
            
            logger.debug("Token refresh successful");
            return response;
            
        } catch (Exception e) {
            logger.error("Token refresh failed: {}", e.getMessage());
            throw e;
        }
    }
    
    /**
     * Logout user and invalidate tokens
     */
    public void logout(String accessToken, String userId) {
        logger.info("Processing logout for user: {}", userId);
        
        try {
            // Blacklist the access token
            if (accessToken != null) {
                blacklistToken(accessToken);
            }
            
            // Remove user session
            removeUserSession(userId);
            
            // Publish logout event
            publishSecurityEvent("LOGOUT", userId, null, "User logout");
            
            logger.info("Logout successful for user: {}", userId);
            
        } catch (Exception e) {
            logger.error("Logout failed for user {}: {}", userId, e.getMessage());
            throw e;
        }
    }
    
    /**
     * Get current user information
     */
    public Map<String, Object> getCurrentUserInfo(String userId) {
        try {
            // Get user info from core banking service
            return coreBankingClient.getUserInfo(userId);
            
        } catch (Exception e) {
            logger.error("Failed to get user info for {}: {}", userId, e.getMessage());
            throw new RuntimeException("Failed to retrieve user information");
        }
    }
    
    /**
     * Validate JWT token
     */
    public Map<String, Object> validateToken(String token) {
        try {
            if (isTokenBlacklisted(token)) {
                throw new RuntimeException("Token has been revoked");
            }
            
            if (!jwtTokenProvider.isAccessTokenValid(token)) {
                throw new RuntimeException("Invalid or expired token");
            }
            
            Map<String, Object> user = jwtTokenProvider.extractUserFromAccessToken(token);
            
            return Map.of(
                "valid", true,
                "user", user,
                "expiresAt", jwtTokenProvider.getTokenExpiration(token)
            );
            
        } catch (Exception e) {
            return Map.of(
                "valid", false,
                "error", e.getMessage()
            );
        }
    }
    
    /**
     * Change user password
     */
    public void changePassword(String userId, String currentPassword, String newPassword) {
        logger.info("Password change request for user: {}", userId);
        
        try {
            // Validate current password
            Map<String, Object> user = coreBankingClient.getUserInfo(userId);
            String username = (String) user.get("username");
            
            if (!validateCredentials(username, currentPassword)) {
                throw new RuntimeException("Current password is incorrect");
            }
            
            // Update password in core banking service
            coreBankingClient.updateUserPassword(userId, passwordEncoder.encode(newPassword));
            
            // Invalidate all user sessions (force re-login)
            invalidateAllUserSessions(userId);
            
            // Publish security event
            publishSecurityEvent("PASSWORD_CHANGED", username, null, "Password changed successfully");
            
            logger.info("Password changed successfully for user: {}", userId);
            
        } catch (Exception e) {
            logger.error("Password change failed for user {}: {}", userId, e.getMessage());
            throw e;
        }
    }
    
    // Helper methods
    
    private Map<String, Object> validateCredentials(String username, String password) {
        try {
            // Call core banking service to validate credentials
            return coreBankingClient.validateUserCredentials(username, password);
        } catch (Exception e) {
            logger.debug("Credential validation failed for user {}: {}", username, e.getMessage());
            return null;
        }
    }
    
    private boolean validateCredentials(String username, String password, boolean returnBoolean) {
        Map<String, Object> user = validateCredentials(username, password);
        return user != null;
    }
    
    private boolean isUserActive(Map<String, Object> user) {
        return Boolean.TRUE.equals(user.get("isActive"));
    }
    
    private boolean isLoginRateLimited(String username, String clientIp) {
        String userKey = LOGIN_ATTEMPTS_PREFIX + "user:" + username;
        String ipKey = LOGIN_ATTEMPTS_PREFIX + "ip:" + clientIp;
        
        Integer userAttempts = (Integer) redisTemplate.opsForValue().get(userKey);
        Integer ipAttempts = (Integer) redisTemplate.opsForValue().get(ipKey);
        
        return (userAttempts != null && userAttempts >= 5) || 
               (ipAttempts != null && ipAttempts >= 10);
    }
    
    private void recordFailedLoginAttempt(String username, String clientIp) {
        String userKey = LOGIN_ATTEMPTS_PREFIX + "user:" + username;
        String ipKey = LOGIN_ATTEMPTS_PREFIX + "ip:" + clientIp;
        
        redisTemplate.opsForValue().increment(userKey);
        redisTemplate.opsForValue().increment(ipKey);
        
        // Set expiration (15 minutes)
        redisTemplate.expire(userKey, 15, TimeUnit.MINUTES);
        redisTemplate.expire(ipKey, 15, TimeUnit.MINUTES);
    }
    
    private void clearFailedLoginAttempts(String username, String clientIp) {
        String userKey = LOGIN_ATTEMPTS_PREFIX + "user:" + username;
        String ipKey = LOGIN_ATTEMPTS_PREFIX + "ip:" + clientIp;
        
        redisTemplate.delete(userKey);
        redisTemplate.delete(ipKey);
    }
    
    private void storeUserSession(Map<String, Object> user, String refreshToken, String clientIp, String userAgent) {
        String userId = (String) user.get("id");
        String sessionKey = USER_SESSION_PREFIX + userId;
        
        Map<String, Object> session = Map.of(
            "userId", userId,
            "username", user.get("username"),
            "refreshToken", refreshToken,
            "clientIp", clientIp,
            "userAgent", userAgent != null ? userAgent : "",
            "loginAt", Instant.now().toString(),
            "lastActivity", Instant.now().toString()
        );
        
        redisTemplate.opsForHash().putAll(sessionKey, session);
        redisTemplate.expire(sessionKey, 24, TimeUnit.HOURS); // Session expires in 24 hours
    }
    
    private void updateUserSession(Map<String, Object> user, String refreshToken) {
        String userId = (String) user.get("id");
        String sessionKey = USER_SESSION_PREFIX + userId;
        
        redisTemplate.opsForHash().put(sessionKey, "refreshToken", refreshToken);
        redisTemplate.opsForHash().put(sessionKey, "lastActivity", Instant.now().toString());
    }
    
    private void removeUserSession(String userId) {
        String sessionKey = USER_SESSION_PREFIX + userId;
        redisTemplate.delete(sessionKey);
    }
    
    private void blacklistToken(String token) {
        String tokenKey = BLACKLISTED_TOKEN_PREFIX + token;
        redisTemplate.opsForValue().set(tokenKey, "blacklisted", 
            jwtTokenProvider.getAccessTokenExpirationSeconds(), TimeUnit.SECONDS);
    }
    
    private boolean isTokenBlacklisted(String token) {
        String tokenKey = BLACKLISTED_TOKEN_PREFIX + token;
        return Boolean.TRUE.equals(redisTemplate.hasKey(tokenKey));
    }
    
    private void invalidateRefreshToken(String refreshToken) {
        // Add to blacklist for remaining validity period
        String tokenKey = BLACKLISTED_TOKEN_PREFIX + refreshToken;
        redisTemplate.opsForValue().set(tokenKey, "blacklisted", 
            jwtTokenProvider.getRefreshTokenExpirationSeconds(), TimeUnit.SECONDS);
    }
    
    private void invalidateAllUserSessions(String userId) {
        // Remove user session
        removeUserSession(userId);
        
        // In a more sophisticated implementation, you would also:
        // 1. Get all active refresh tokens for the user
        // 2. Blacklist all of them
        // 3. Notify all active sessions to re-authenticate
    }
    
    private void publishSecurityEvent(String eventType, String username, String clientIp, String message) {
        try {
            AuditEvent event = new AuditEvent();
            event.setEventType(eventType);
            event.setSource("auth-service");
            event.setUserId(username);
            
            Map<String, Object> eventData = new HashMap<>();
            eventData.put("username", username);
            eventData.put("clientIp", clientIp);
            eventData.put("message", message);
            eventData.put("timestamp", Instant.now().toString());
            
            event.setNewValues(eventData);
            
            eventPublisher.publishEvent(KafkaTopics.SECURITY_LOGIN_ATTEMPT, username, event);
            
        } catch (Exception e) {
            logger.error("Failed to publish security event: {}", e.getMessage());
        }
    }
}