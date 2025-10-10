package com.paymentengine.gateway.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.paymentengine.gateway.security.JwtAuthenticationManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Service for enhanced token operations in API Gateway
 * Integrates with Auth Service for unified token management
 */
@Service
@Transactional
public class EnhancedTokenService {

    private static final Logger logger = LoggerFactory.getLogger(EnhancedTokenService.class);

    private final RestTemplate restTemplate;
    private final JwtAuthenticationManager jwtAuthenticationManager;
    private final String authServiceBaseUrl;

    public EnhancedTokenService(RestTemplate restTemplate,
                                JwtAuthenticationManager jwtAuthenticationManager,
                                @Value("${services.auth.base-url:http://auth-service:8080}") String authServiceBaseUrl) {
        this.restTemplate = restTemplate;
        this.jwtAuthenticationManager = jwtAuthenticationManager;
        this.authServiceBaseUrl = authServiceBaseUrl + "/api/v1/auth";
    }

    /**
     * Generate token using unified token service
     */
    public Optional<String> generateToken(String username, String authMethod, Map<String, Object> authConfiguration) {
        logger.info("Generating token for user: {} with auth method: {}", username, authMethod);

        try {
            String password = authConfiguration != null
                    ? (String) authConfiguration.getOrDefault("password", authConfiguration.get("secret"))
                    : null;

            if (password == null) {
                logger.warn("No password provided for user {} using auth method {}", username, authMethod);
                return Optional.empty();
            }

            LoginRequest request = new LoginRequest(username, password);
            HttpHeaders headers = defaultHeaders();
            ResponseEntity<LoginResponse> response = restTemplate.postForEntity(
                    authServiceBaseUrl + "/login",
                    new HttpEntity<>(request, headers),
                    LoginResponse.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                logger.info("Successfully generated token for user: {}", username);
                return Optional.ofNullable(response.getBody().getAccessToken());
            }

            return Optional.empty();

        } catch (Exception e) {
            logger.error("Failed to generate token for user: {}", username, e);
            return Optional.empty();
        }
    }
    
    /**
     * Validate token using unified token service
     */
    public boolean validateToken(String token, String authMethod, Map<String, Object> authConfiguration) {
        logger.info("Validating token with auth method: {}", authMethod);

        try {
            HttpHeaders headers = defaultHeaders();
            headers.set(HttpHeaders.AUTHORIZATION, ensureBearerPrefix(token));

            ResponseEntity<Boolean> response = restTemplate.exchange(
                    authServiceBaseUrl + "/validate",
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    Boolean.class
            );

            boolean valid = response.getStatusCode().is2xxSuccessful()
                    && Boolean.TRUE.equals(response.getBody())
                    && jwtAuthenticationManager.isTokenValid(token);

            logger.info("Token validation result for auth method {}: {}", authMethod, valid);
            return valid;

        } catch (Exception e) {
            logger.error("Failed to validate token", e);
            return false;
        }
    }
    
    /**
     * Get token claims
     */
    public Optional<Map<String, Object>> getTokenClaims(String token, String authMethod) {
        logger.info("Getting token claims for auth method: {}", authMethod);

        try {
            if (!jwtAuthenticationManager.isTokenValid(token)) {
                return Optional.empty();
            }

            Map<String, Object> claims = new HashMap<>();
            claims.put("userId", jwtAuthenticationManager.extractUserId(token));
            claims.put("username", jwtAuthenticationManager.extractUsername(token));
            claims.put("valid", true);

            return Optional.of(claims);

        } catch (Exception e) {
            logger.error("Failed to get token claims", e);
            return Optional.empty();
        }
    }
    
    /**
     * Refresh token
     */
    public Optional<String> refreshToken(String refreshToken, String authMethod) {
        logger.info("Refreshing token with auth method: {}", authMethod);

        try {
            HttpHeaders headers = defaultHeaders();
            RefreshTokenRequest request = new RefreshTokenRequest(refreshToken);

            ResponseEntity<LoginResponse> response = restTemplate.postForEntity(
                    authServiceBaseUrl + "/refresh",
                    new HttpEntity<>(request, headers),
                    LoginResponse.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                logger.info("Successfully refreshed token");
                return Optional.ofNullable(response.getBody().getAccessToken());
            }

            return Optional.empty();

        } catch (Exception e) {
            logger.error("Failed to refresh token", e);
            return Optional.empty();
        }
    }
    
    /**
     * Revoke token
     */
    public boolean revokeToken(String token, String authMethod) {
        logger.info("Revoking token with auth method: {}", authMethod);

        try {
            HttpHeaders headers = defaultHeaders();
            headers.set(HttpHeaders.AUTHORIZATION, ensureBearerPrefix(token));

            ResponseEntity<Void> response = restTemplate.exchange(
                    authServiceBaseUrl + "/logout",
                    HttpMethod.POST,
                    new HttpEntity<>(headers),
                    Void.class
            );

            boolean success = response.getStatusCode().is2xxSuccessful();
            logger.info("Token revoke result: {}", success);
            return success;

        } catch (Exception e) {
            logger.error("Failed to revoke token", e);
            return false;
        }
    }

    private HttpHeaders defaultHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    private String ensureBearerPrefix(String token) {
        if (token == null || token.isBlank()) {
            return token;
        }
        return token.startsWith("Bearer ") ? token : "Bearer " + token;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class LoginRequest {
        private final String username;
        private final String password;

        public LoginRequest(String username, String password) {
            this.username = username;
            this.password = password;
        }

        public String getUsername() {
            return username;
        }

        public String getPassword() {
            return password;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class RefreshTokenRequest {
        @JsonProperty("refreshToken")
        private final String refreshToken;

        public RefreshTokenRequest(String refreshToken) {
            this.refreshToken = refreshToken;
        }

        public String getRefreshToken() {
            return refreshToken;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class LoginResponse {
        @JsonProperty("accessToken")
        private String accessToken;

        public String getAccessToken() {
            return accessToken;
        }

        public void setAccessToken(String accessToken) {
            this.accessToken = accessToken;
        }
    }
}