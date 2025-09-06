package com.paymentengine.corebanking.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User service for authentication and user management
 */
@Service
@Transactional
public class UserService {
    
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    
    private final JdbcTemplate jdbcTemplate;
    
    @Autowired
    public UserService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
    
    /**
     * Find user by username
     */
    @Transactional(readOnly = true)
    public Map<String, Object> findByUsername(String username) {
        try {
            String sql = """
                SELECT u.id, u.username, u.email, u.password_hash, u.first_name, u.last_name,
                       u.is_active, u.last_login_at, u.failed_login_attempts, u.locked_until,
                       u.created_at, u.updated_at
                FROM payment_engine.users u 
                WHERE u.username = ? AND u.is_active = true
                """;
            
            List<Map<String, Object>> results = jdbcTemplate.queryForList(sql, username);
            
            if (results.isEmpty()) {
                return null;
            }
            
            Map<String, Object> user = results.get(0);
            
            // Convert column names to camelCase
            Map<String, Object> result = new HashMap<>();
            result.put("id", user.get("id"));
            result.put("username", user.get("username"));
            result.put("email", user.get("email"));
            result.put("passwordHash", user.get("password_hash"));
            result.put("firstName", user.get("first_name"));
            result.put("lastName", user.get("last_name"));
            result.put("isActive", user.get("is_active"));
            result.put("lastLoginAt", user.get("last_login_at"));
            result.put("failedLoginAttempts", user.get("failed_login_attempts"));
            result.put("lockedUntil", user.get("locked_until"));
            result.put("createdAt", user.get("created_at"));
            result.put("updatedAt", user.get("updated_at"));
            
            return result;
            
        } catch (Exception e) {
            logger.error("Error finding user by username {}: {}", username, e.getMessage(), e);
            throw new RuntimeException("Failed to find user", e);
        }
    }
    
    /**
     * Get user permissions
     */
    @Transactional(readOnly = true)
    public List<String> getUserPermissions(String userId) {
        try {
            String sql = """
                SELECT DISTINCT jsonb_array_elements_text(r.permissions) as permission
                FROM payment_engine.users u
                JOIN payment_engine.user_roles ur ON u.id = ur.user_id
                JOIN payment_engine.roles r ON ur.role_id = r.id
                WHERE u.id = ?::uuid AND u.is_active = true AND r.is_active = true
                """;
            
            return jdbcTemplate.queryForList(sql, String.class, userId);
            
        } catch (Exception e) {
            logger.error("Error getting user permissions for {}: {}", userId, e.getMessage(), e);
            return List.of();
        }
    }
    
    /**
     * Get user roles
     */
    @Transactional(readOnly = true)
    public List<String> getUserRoles(String userId) {
        try {
            String sql = """
                SELECT r.name
                FROM payment_engine.users u
                JOIN payment_engine.user_roles ur ON u.id = ur.user_id
                JOIN payment_engine.roles r ON ur.role_id = r.id
                WHERE u.id = ?::uuid AND u.is_active = true AND r.is_active = true
                """;
            
            return jdbcTemplate.queryForList(sql, String.class, userId);
            
        } catch (Exception e) {
            logger.error("Error getting user roles for {}: {}", userId, e.getMessage(), e);
            return List.of();
        }
    }
    
    /**
     * Update user password
     */
    public void updateUserPassword(String userId, String newPasswordHash) {
        try {
            String sql = """
                UPDATE payment_engine.users 
                SET password_hash = ?, password_changed_at = CURRENT_TIMESTAMP, updated_at = CURRENT_TIMESTAMP
                WHERE id = ?::uuid
                """;
            
            int updated = jdbcTemplate.update(sql, newPasswordHash, userId);
            
            if (updated == 0) {
                throw new RuntimeException("User not found or not updated");
            }
            
            logger.info("Password updated successfully for user: {}", userId);
            
        } catch (Exception e) {
            logger.error("Error updating password for user {}: {}", userId, e.getMessage(), e);
            throw new RuntimeException("Failed to update password", e);
        }
    }
    
    /**
     * Update last login timestamp
     */
    public void updateLastLogin(String userId) {
        try {
            String sql = """
                UPDATE payment_engine.users 
                SET last_login_at = CURRENT_TIMESTAMP, 
                    failed_login_attempts = 0,
                    updated_at = CURRENT_TIMESTAMP
                WHERE id = ?::uuid
                """;
            
            jdbcTemplate.update(sql, userId);
            
        } catch (Exception e) {
            logger.error("Error updating last login for user {}: {}", userId, e.getMessage());
        }
    }
    
    /**
     * Record failed login attempt
     */
    public void recordFailedLoginAttempt(String username) {
        try {
            String sql = """
                UPDATE payment_engine.users 
                SET failed_login_attempts = failed_login_attempts + 1,
                    locked_until = CASE 
                        WHEN failed_login_attempts + 1 >= 5 THEN CURRENT_TIMESTAMP + INTERVAL '15 minutes'
                        ELSE locked_until 
                    END,
                    updated_at = CURRENT_TIMESTAMP
                WHERE username = ?
                """;
            
            jdbcTemplate.update(sql, username);
            
        } catch (Exception e) {
            logger.error("Error recording failed login attempt for user {}: {}", username, e.getMessage());
        }
    }
    
    /**
     * Check if user account is locked
     */
    @Transactional(readOnly = true)
    public boolean isUserLocked(String username) {
        try {
            String sql = """
                SELECT CASE 
                    WHEN locked_until IS NOT NULL AND locked_until > CURRENT_TIMESTAMP THEN true
                    ELSE false
                END as is_locked
                FROM payment_engine.users 
                WHERE username = ?
                """;
            
            List<Boolean> results = jdbcTemplate.queryForList(sql, Boolean.class, username);
            return !results.isEmpty() && Boolean.TRUE.equals(results.get(0));
            
        } catch (Exception e) {
            logger.error("Error checking if user is locked {}: {}", username, e.getMessage());
            return false;
        }
    }
    
    /**
     * Get user by ID
     */
    @Transactional(readOnly = true)
    public Map<String, Object> findById(String userId) {
        try {
            String sql = """
                SELECT u.id, u.username, u.email, u.first_name, u.last_name,
                       u.is_active, u.last_login_at, u.created_at, u.updated_at
                FROM payment_engine.users u 
                WHERE u.id = ?::uuid
                """;
            
            List<Map<String, Object>> results = jdbcTemplate.queryForList(sql, userId);
            
            if (results.isEmpty()) {
                return null;
            }
            
            Map<String, Object> user = results.get(0);
            
            // Add permissions and roles
            user.put("permissions", getUserPermissions(userId));
            user.put("roles", getUserRoles(userId));
            
            return user;
            
        } catch (Exception e) {
            logger.error("Error finding user by ID {}: {}", userId, e.getMessage(), e);
            throw new RuntimeException("Failed to find user", e);
        }
    }
    
    /**
     * Check if user has permission
     */
    @Transactional(readOnly = true)
    public boolean hasPermission(String userId, String permission) {
        try {
            List<String> permissions = getUserPermissions(userId);
            return permissions.contains(permission) || permissions.contains("super:admin");
            
        } catch (Exception e) {
            logger.error("Error checking permission for user {}: {}", userId, e.getMessage());
            return false;
        }
    }
}