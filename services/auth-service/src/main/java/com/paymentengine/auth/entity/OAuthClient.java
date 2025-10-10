package com.paymentengine.auth.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "oauth_clients")
public class OAuthClient {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @NotBlank
    @Size(max = 100)
    @Column(name = "client_id", unique = true, nullable = false)
    private String clientId;
    
    @NotBlank
    @Size(max = 255)
    @Column(name = "client_secret_hash", nullable = false)
    private String clientSecretHash;
    
    @NotBlank
    @Size(max = 100)
    @Column(name = "client_name", nullable = false)
    private String clientName;
    
    @Size(max = 500)
    private String description;
    
    @Size(max = 1000)
    @Column(name = "redirect_uris")
    private String redirectUris;
    
    @Size(max = 500)
    private String scopes;
    
    @Size(max = 500)
    @Column(name = "grant_types")
    private String grantTypes;
    
    @Column(name = "access_token_validity")
    private Integer accessTokenValidity = 3600;
    
    @Column(name = "refresh_token_validity")
    private Integer refreshTokenValidity = 86400;
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @Size(max = 100)
    @Column(name = "created_by")
    private String createdBy;
    
    @Size(max = 100)
    @Column(name = "updated_by")
    private String updatedBy;
    
    // Constructors
    public OAuthClient() {}
    
    public OAuthClient(String clientId, String clientSecretHash, String clientName) {
        this.clientId = clientId;
        this.clientSecretHash = clientSecretHash;
        this.clientName = clientName;
    }
    
    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    
    public String getClientId() { return clientId; }
    public void setClientId(String clientId) { this.clientId = clientId; }
    
    public String getClientSecretHash() { return clientSecretHash; }
    public void setClientSecretHash(String clientSecretHash) { this.clientSecretHash = clientSecretHash; }
    
    public String getClientName() { return clientName; }
    public void setClientName(String clientName) { this.clientName = clientName; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getRedirectUris() { return redirectUris; }
    public void setRedirectUris(String redirectUris) { this.redirectUris = redirectUris; }
    
    public String getScopes() { return scopes; }
    public void setScopes(String scopes) { this.scopes = scopes; }
    
    public String getGrantTypes() { return grantTypes; }
    public void setGrantTypes(String grantTypes) { this.grantTypes = grantTypes; }
    
    public Integer getAccessTokenValidity() { return accessTokenValidity; }
    public void setAccessTokenValidity(Integer accessTokenValidity) { this.accessTokenValidity = accessTokenValidity; }
    
    public Integer getRefreshTokenValidity() { return refreshTokenValidity; }
    public void setRefreshTokenValidity(Integer refreshTokenValidity) { this.refreshTokenValidity = refreshTokenValidity; }
    
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    
    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }
}