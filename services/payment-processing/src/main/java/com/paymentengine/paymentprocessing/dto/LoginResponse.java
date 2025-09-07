package com.paymentengine.paymentprocessing.dto;

import java.util.Map;

/**
 * Login response DTO containing authentication tokens and user information
 */
public class LoginResponse {
    
    private String accessToken;
    private String refreshToken;
    private String tokenType = "Bearer";
    private int expiresIn;
    private String scope;
    private Map<String, Object> user;
    
    // Constructors
    public LoginResponse() {}
    
    public LoginResponse(String accessToken, String refreshToken, int expiresIn, Map<String, Object> user) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.expiresIn = expiresIn;
        this.user = user;
    }
    
    // Getters and Setters
    public String getAccessToken() {
        return accessToken;
    }
    
    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }
    
    public String getRefreshToken() {
        return refreshToken;
    }
    
    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
    
    public String getTokenType() {
        return tokenType;
    }
    
    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }
    
    public int getExpiresIn() {
        return expiresIn;
    }
    
    public void setExpiresIn(int expiresIn) {
        this.expiresIn = expiresIn;
    }
    
    public String getScope() {
        return scope;
    }
    
    public void setScope(String scope) {
        this.scope = scope;
    }
    
    public Map<String, Object> getUser() {
        return user;
    }
    
    public void setUser(Map<String, Object> user) {
        this.user = user;
    }
    
    @Override
    public String toString() {
        return "LoginResponse{" +
                "accessToken='[PROTECTED]'" +
                ", refreshToken='[PROTECTED]'" +
                ", tokenType='" + tokenType + '\'' +
                ", expiresIn=" + expiresIn +
                ", scope='" + scope + '\'' +
                ", user=" + user +
                '}';
    }
}