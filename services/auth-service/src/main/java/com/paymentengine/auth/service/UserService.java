package com.paymentengine.auth.service;

import com.paymentengine.auth.entity.User;
import com.paymentengine.auth.entity.Role;
import com.paymentengine.auth.repository.UserRepository;
import com.paymentengine.auth.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
@Transactional
public class UserService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private RoleRepository roleRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    public User createUser(String username, String email, String password, String firstName, String lastName) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setStatus(User.UserStatus.ACTIVE);
        user.setPasswordChangedAt(LocalDateTime.now());
        user.setCreatedAt(LocalDateTime.now());
        
        return userRepository.save(user);
    }
    
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }
    
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
    
    public Optional<User> findByUsernameOrEmail(String username, String email) {
        return userRepository.findByUsernameOrEmail(username, email);
    }
    
    public Optional<User> findById(UUID id) {
        return userRepository.findById(id);
    }
    
    public List<User> findAll() {
        return userRepository.findAll();
    }
    
    public List<User> findByStatus(User.UserStatus status) {
        return userRepository.findByStatus(status);
    }
    
    public User updateUser(User user) {
        user.setUpdatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }
    
    public void deleteUser(UUID id) {
        userRepository.deleteById(id);
    }
    
    public boolean changePassword(UUID userId, String oldPassword, String newPassword) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (passwordEncoder.matches(oldPassword, user.getPasswordHash())) {
                user.setPasswordHash(passwordEncoder.encode(newPassword));
                user.setPasswordChangedAt(LocalDateTime.now());
                userRepository.save(user);
                return true;
            }
        }
        return false;
    }

    public boolean isPasswordValid(User user, String rawPassword) {
        return passwordEncoder.matches(rawPassword, user.getPasswordHash());
    }
    
    public void assignRoles(UUID userId, Set<String> roleNames) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            Set<Role> roles = Set.copyOf(roleRepository.findByNameIn(roleNames.stream().toList()));
            user.setRoles(roles);
            userRepository.save(user);
        }
    }
    
    public void removeRoles(UUID userId, Set<String> roleNames) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            Set<Role> currentRoles = user.getRoles();
            currentRoles.removeIf(role -> roleNames.contains(role.getName()));
            user.setRoles(currentRoles);
            userRepository.save(user);
        }
    }
    
    public void recordSuccessfulLogin(UUID userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setLastLoginAt(LocalDateTime.now());
            user.setFailedLoginAttempts(0);
            user.setLockedUntil(null);
            userRepository.save(user);
        }
    }
    
    public void recordFailedLogin(String username) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            int failedAttempts = user.getFailedLoginAttempts() + 1;
            user.setFailedLoginAttempts(failedAttempts);
            
            // Lock account after 5 failed attempts for 30 minutes
            if (failedAttempts >= 5) {
                user.setLockedUntil(LocalDateTime.now().plusMinutes(30));
                user.setStatus(User.UserStatus.LOCKED);
            }
            
            userRepository.save(user);
        }
    }
    
    public void unlockUser(UUID userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setLockedUntil(null);
            user.setFailedLoginAttempts(0);
            if (user.getStatus() == User.UserStatus.LOCKED) {
                user.setStatus(User.UserStatus.ACTIVE);
            }
            userRepository.save(user);
        }
    }
    
    public void activateUser(UUID userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setStatus(User.UserStatus.ACTIVE);
            userRepository.save(user);
        }
    }
    
    public void deactivateUser(UUID userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setStatus(User.UserStatus.INACTIVE);
            userRepository.save(user);
        }
    }
    
    public void suspendUser(UUID userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setStatus(User.UserStatus.SUSPENDED);
            userRepository.save(user);
        }
    }
    
    public boolean isUserLocked(UUID userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            return user.getLockedUntil() != null && user.getLockedUntil().isAfter(LocalDateTime.now());
        }
        return false;
    }
    
    public boolean isUserActive(UUID userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            return user.getStatus() == User.UserStatus.ACTIVE && !isUserLocked(userId);
        }
        return false;
    }
    
    public List<User> findLockedUsers() {
        return userRepository.findLockedUsers(LocalDateTime.now());
    }
    
    public List<User> findUsersWithFailedAttempts(int maxAttempts) {
        return userRepository.findUsersWithFailedAttempts(maxAttempts);
    }
    
    public Long countByStatus(User.UserStatus status) {
        return userRepository.countByStatus(status);
    }
    
    public List<User> findByRoleName(String roleName) {
        return userRepository.findByRoleName(roleName);
    }
    
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }
    
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
}