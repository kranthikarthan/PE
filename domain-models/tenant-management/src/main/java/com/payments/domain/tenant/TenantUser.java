package com.payments.domain.tenant;

import com.payments.domain.shared.*;
import jakarta.persistence.*;
import java.time.Instant;
import lombok.*;

/** Tenant User (Entity within Tenant Aggregate) */
@Entity
@Table(name = "tenant_users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Data
public class TenantUser {

  @EmbeddedId
  @AttributeOverride(name = "value", column = @Column(name = "tenant_user_id"))
  private TenantUserId id;

  @Embedded
  @AttributeOverride(name = "value", column = @Column(name = "tenant_id"))
  private TenantId tenantId;

  @Embedded
  @AttributeOverride(name = "value", column = @Column(name = "business_unit_id"))
  private BusinessUnitId businessUnitId;

  @Embedded
  @AttributeOverride(name = "value", column = @Column(name = "user_id"))
  private UserId userId;

  @Transient private String username;

  @Transient private String email;

  @Column(name = "role")
  private String role;

  @Transient private UserStatus status;

  @Column(name = "is_active")
  private Boolean isActive;

  @Transient // Not directly mapped in DDL
  private String permissions;

  @Column(name = "assigned_at")
  private Instant createdAt;

  @Column(name = "last_accessed_at")
  private Instant updatedAt;

  @Column(name = "assigned_by")
  private String createdBy;

  @Transient private String updatedBy;

  public static TenantUser create(
      TenantUserId id,
      TenantId tenantId,
      BusinessUnitId businessUnitId,
      UserId userId,
      String username,
      String email,
      String role,
      String createdBy) {
    TenantUser tenantUser = new TenantUser();
    tenantUser.id = id;
    tenantUser.tenantId = tenantId;
    tenantUser.businessUnitId = businessUnitId;
    tenantUser.userId = userId;
    tenantUser.username = username;
    tenantUser.email = email;
    tenantUser.role = role;
    tenantUser.status = UserStatus.ACTIVE;
    tenantUser.isActive = true;
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

/** User Status enumeration */
enum UserStatus {
  ACTIVE,
  SUSPENDED,
  INACTIVE
}
