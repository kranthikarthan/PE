package com.paymentengine.paymentprocessing.service;

import com.paymentengine.paymentprocessing.dto.LoginRequest;
import com.paymentengine.paymentprocessing.dto.LoginResponse;
import com.paymentengine.paymentprocessing.service.ResiliencyConfigurationService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.function.Supplier;

@Service
public class ResilientAuthService {

    private static final String SERVICE_NAME = "auth-service";

    private final ResiliencyConfigurationService resiliencyConfigurationService;
    private final RestTemplate restTemplate;
    private final String authServiceBaseUrl;

    public ResilientAuthService(ResiliencyConfigurationService resiliencyConfigurationService,
                                RestTemplate restTemplate,
                                @Value("${services.auth.base-url:http://auth-service:8080}") String authServiceBaseUrl) {
        this.resiliencyConfigurationService = resiliencyConfigurationService;
        this.restTemplate = restTemplate;
        this.authServiceBaseUrl = authServiceBaseUrl;
    }

    private String getAuthServiceUrl() {
        return authServiceBaseUrl + "/api/v1/auth";
    }

    public LoginResponse authenticate(LoginRequest request) {
        Supplier<LoginResponse> supplier = () -> restTemplate.postForObject(
                getAuthServiceUrl() + "/login",
                request,
                LoginResponse.class
        );
        return resiliencyConfigurationService.executeResilientCall(
                SERVICE_NAME,
                request.getUsername(),
                supplier,
                throwable -> {
                    throw new RuntimeException("Authentication failed", throwable);
                }
        );
    }

    public boolean validateToken(String token) {
        Supplier<Boolean> supplier = () -> {
            HttpHeaders headers = new HttpHeaders();
            headers.set(HttpHeaders.AUTHORIZATION, ensureBearerPrefix(token));
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<Boolean> response = restTemplate.exchange(
                    getAuthServiceUrl() + "/validate",
                    HttpMethod.GET,
                    entity,
                    Boolean.class
            );

            return response.getBody() != null && response.getBody();
        };
        return resiliencyConfigurationService.executeResilientCall(
                SERVICE_NAME,
                "system",
                supplier,
                throwable -> false
        );
    }

    public LoginResponse refreshToken(String refreshToken) {
        Supplier<LoginResponse> supplier = () -> restTemplate.postForObject(
                getAuthServiceUrl() + "/refresh",
                new com.paymentengine.paymentprocessing.dto.RefreshTokenRequest(refreshToken),
                LoginResponse.class
        );
        return resiliencyConfigurationService.executeResilientCall(
                SERVICE_NAME,
                "system",
                supplier,
                throwable -> {
                    throw new RuntimeException("Token refresh failed", throwable);
                }
        );
    }

    public void logout(String token) {
        Supplier<Void> supplier = () -> {
            HttpHeaders headers = new HttpHeaders();
            headers.set(HttpHeaders.AUTHORIZATION, ensureBearerPrefix(token));
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            restTemplate.exchange(
                    getAuthServiceUrl() + "/logout",
                    HttpMethod.POST,
                    entity,
                    Void.class
            );
            return null;
        };
        resiliencyConfigurationService.executeResilientCall(
                SERVICE_NAME,
                "system",
                supplier,
                throwable -> {
                    throw new RuntimeException("Logout failed", throwable);
                }
        );
    }

    private String ensureBearerPrefix(String token) {
        if (token == null || token.isBlank()) {
            return token;
        }
        return token.startsWith("Bearer ") ? token : "Bearer " + token;
    }
}