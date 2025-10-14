package com.payments.domain.tenant;

import com.payments.domain.shared.*;
import lombok.*;
import jakarta.persistence.*;
import java.time.Instant;

/**
 * Tenant User (Entity within Tenant Aggregate)
 */
@Entity
@Table(name = "tenant_users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Data
public class TenantUser {
    
    @EmbeddedId
    private TenantUserId id;
    
    @Embedded
    private TenantId tenantId;
    
    @Embedded
    private UserId userId;
    
    private String username;
    
    private String email;
    
    private String role;
    
    @Enumerated(EnumType.STRING)
    private UserStatus status;
    
    private Instant createdAt;
    
    private Instant updatedAt;
    
    private String createdBy;
    
    private String updatedBy;
    
    public static TenantUser create(
        TenantUserId id,
        TenantId tenantId,
        UserId userId,
        String username,
        String email,
        String role,
        String createdBy
    ) {
        TenantUser tenantUser = new TenantUser();
        tenantUser.id = id;
        tenantUser.tenantId = tenantId;
        tenantUser.userId = userId;
        tenantUser.username = username;
        tenantUser.email = email;
        tenantUser.role = role;
        tenantUser.status = UserStatus.ACTIVE;
        tenantUser.createdAt = Instant.now();
        tenantUser.updatedAt = Instant.now();
        tenantUser.createdBy = createdBy;
        tenantUser.updatedBy = createdBy;
        
        return tenantUser;
    }
    
    public void updateRole(String role, String updatedBy) {
        this.role = role;
        this.updatedAt = Instant.now();
        this.updatedBy = updatedBy;
    }
    
    public void suspend(String updatedBy) {
        this.status = UserStatus.SUSPENDED;
        this.updatedAt = Instant.now();
        this.updatedBy = updatedBy;
    }
    
    public void activate(String updatedBy) {
        this.status = UserStatus.ACTIVE;
        this.updatedAt = Instant.now();
        this.updatedBy = updatedBy;
    }
}

/**
 * User Status enumeration
 */
enum UserStatus {
    ACTIVE,
    SUSPENDED,
    INACTIVE
}
