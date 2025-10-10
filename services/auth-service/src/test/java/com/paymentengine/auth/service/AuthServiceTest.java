package com.paymentengine.auth.service;

import com.paymentengine.auth.dto.LoginRequest;
import com.paymentengine.auth.dto.LoginResponse;
import com.paymentengine.auth.dto.UserRegistrationRequest;
import com.paymentengine.auth.entity.User;
import com.paymentengine.auth.repository.OAuthTokenRepository;
import com.paymentengine.auth.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserService userService;

    @Mock
    private JwtTokenService jwtTokenService;

    @Mock
    private OAuthTokenRepository oAuthTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    private User testUser;
    private LoginRequest loginRequest;
    private UserRegistrationRequest registrationRequest;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(UUID.randomUUID());
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPasswordHash("encodedPassword");
        testUser.setStatus(User.UserStatus.ACTIVE);
        testUser.setRoles(Set.of());

        loginRequest = new LoginRequest("testuser", "password");
        registrationRequest = new UserRegistrationRequest(
                "newuser", "new@example.com", "password", "New", "User");
    }

    @Test
    void authenticate_ValidCredentials_ReturnsLoginResponse() {
        // Given
        when(userService.findByUsernameOrEmail("testuser", "testuser"))
                .thenReturn(Optional.of(testUser));
        when(userService.isUserActive(testUser.getId())).thenReturn(true);
        when(userService.isPasswordValid(testUser, "password")).thenReturn(true);
        when(jwtTokenService.generateAccessToken(any(), any(), any(), any(), any()))
                .thenReturn("accessToken");
        when(jwtTokenService.generateRefreshToken(any(), any()))
                .thenReturn("refreshToken");

        // When
        LoginResponse response = authService.authenticate("testuser", "password");

        // Then
        assertNotNull(response);
        assertEquals("accessToken", response.getAccessToken());
        assertEquals("refreshToken", response.getRefreshToken());
        assertEquals("Bearer", response.getTokenType());
        assertEquals(3600L, response.getExpiresIn());
        assertEquals(testUser.getId(), response.getUserId());
        assertEquals(testUser.getUsername(), response.getUsername());
        assertEquals(testUser.getEmail(), response.getEmail());

        verify(userService).recordSuccessfulLogin(testUser.getId());
        verify(oAuthTokenRepository).save(any());
    }

    @Test
    void authenticate_InvalidCredentials_ThrowsException() {
        // Given
        when(userService.findByUsernameOrEmail("testuser", "testuser"))
                .thenReturn(Optional.of(testUser));
        when(userService.isUserActive(testUser.getId())).thenReturn(true);
        when(userService.isPasswordValid(testUser, "password")).thenReturn(false);

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            authService.authenticate("testuser", "password");
        });

        verify(userService).recordFailedLogin("testuser");
        verify(userService, never()).recordSuccessfulLogin(any());
    }

    @Test
    void authenticate_UserNotFound_ThrowsException() {
        // Given
        when(userService.findByUsernameOrEmail("testuser", "testuser"))
                .thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            authService.authenticate("testuser", "password");
        });

        verify(userService, never()).recordSuccessfulLogin(any());
        verify(userService, never()).recordFailedLogin(any());
    }

    @Test
    void authenticate_UserLocked_ThrowsException() {
        // Given
        when(userService.findByUsernameOrEmail("testuser", "testuser"))
                .thenReturn(Optional.of(testUser));
        when(userService.isUserActive(testUser.getId())).thenReturn(false);

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            authService.authenticate("testuser", "password");
        });

        verify(userService).recordFailedLogin("testuser");
        verify(userService, never()).recordSuccessfulLogin(any());
    }

    @Test
    void refreshToken_ValidToken_ReturnsNewTokens() {
        // Given
        String refreshToken = "validRefreshToken";
        when(jwtTokenService.validateToken(refreshToken)).thenReturn(true);
        when(jwtTokenService.isRefreshToken(refreshToken)).thenReturn(true);
        when(jwtTokenService.getUserIdFromToken(refreshToken)).thenReturn(testUser.getId());
        when(jwtTokenService.getUsernameFromToken(refreshToken)).thenReturn(testUser.getUsername());
        when(userService.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(userService.isUserActive(testUser.getId())).thenReturn(true);
        when(jwtTokenService.generateAccessToken(any(), any(), any(), any(), any()))
                .thenReturn("newAccessToken");
        when(jwtTokenService.generateRefreshToken(any(), any()))
                .thenReturn("newRefreshToken");

        // When
        LoginResponse response = authService.refreshToken(refreshToken);

        // Then
        assertNotNull(response);
        assertEquals("newAccessToken", response.getAccessToken());
        assertEquals("newRefreshToken", response.getRefreshToken());

        verify(oAuthTokenRepository).findByRefreshToken(refreshToken);
        verify(oAuthTokenRepository).save(any());
    }

    @Test
    void refreshToken_InvalidToken_ThrowsException() {
        // Given
        String refreshToken = "invalidRefreshToken";
        when(jwtTokenService.validateToken(refreshToken)).thenReturn(false);

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            authService.refreshToken(refreshToken);
        });
    }

    @Test
    void registerUser_ValidData_ReturnsUser() {
        // Given
        when(userService.existsByUsername("newuser")).thenReturn(false);
        when(userService.existsByEmail("new@example.com")).thenReturn(false);
        when(userService.createUser(any(), any(), any(), any(), any())).thenReturn(testUser);

        // When
        User result = authService.registerUser(registrationRequest);

        // Then
        assertNotNull(result);
        assertEquals(testUser, result);

        verify(userService).createUser(
                "newuser", "new@example.com", "password", "New", "User");
    }

    @Test
    void registerUser_UsernameExists_ThrowsException() {
        // Given
        when(userService.existsByUsername("newuser")).thenReturn(true);

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            authService.registerUser(registrationRequest);
        });

        verify(userService, never()).createUser(any(), any(), any(), any(), any());
    }

    @Test
    void registerUser_EmailExists_ThrowsException() {
        // Given
        when(userService.existsByUsername("newuser")).thenReturn(false);
        when(userService.existsByEmail("new@example.com")).thenReturn(true);

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            authService.registerUser(registrationRequest);
        });

        verify(userService, never()).createUser(any(), any(), any(), any(), any());
    }

    @Test
    void logout_ValidToken_RevokesToken() {
        // Given
        String token = "Bearer validToken";
        when(oAuthTokenRepository.findByAccessToken("validToken"))
                .thenReturn(Optional.of(new com.paymentengine.auth.entity.OAuthToken()));

        // When
        authService.logout(token);

        // Then
        verify(oAuthTokenRepository).findByAccessToken("validToken");
    }

    @Test
    void validateToken_ValidToken_ReturnsTrue() {
        // Given
        String token = "Bearer validToken";
        when(jwtTokenService.validateToken("validToken")).thenReturn(true);
        when(jwtTokenService.isAccessToken("validToken")).thenReturn(true);
        when(oAuthTokenRepository.findByAccessToken("validToken"))
                .thenReturn(Optional.of(new com.paymentengine.auth.entity.OAuthToken()));

        // When
        boolean result = authService.validateToken(token);

        // Then
        assertTrue(result);
    }

    @Test
    void validateToken_InvalidToken_ReturnsFalse() {
        // Given
        String token = "Bearer invalidToken";
        when(jwtTokenService.validateToken("invalidToken")).thenReturn(false);

        // When
        boolean result = authService.validateToken(token);

        // Then
        assertFalse(result);
    }

    @Test
    void isCurrentUser_ValidUser_ReturnsTrue() {
        // Given
        UUID userId = testUser.getId();
        when(jwtTokenService.getUserIdFromToken(any())).thenReturn(userId);

        // When
        boolean result = authService.isCurrentUser(userId);

        // Then
        assertTrue(result);
    }

    @Test
    void isCurrentUser_InvalidUser_ReturnsFalse() {
        // Given
        UUID userId = testUser.getId();
        UUID differentUserId = UUID.randomUUID();
        when(jwtTokenService.getUserIdFromToken(any())).thenReturn(differentUserId);

        // When
        boolean result = authService.isCurrentUser(userId);

        // Then
        assertFalse(result);
    }
}