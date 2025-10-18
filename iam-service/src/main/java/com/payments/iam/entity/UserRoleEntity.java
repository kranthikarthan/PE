package com.payments.iam.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * User-Role Mapping Entity.
 *
 * <p>Links a user (from Azure AD B2C) to a role within a specific tenant.
 * Supports multi-tenancy: same user can have different roles in different tenants.
 *
 * <p>Example:
 * - user@acme.com has role=BANK_ADMIN in tenant_id=tenant-123
 * - user@acme.com has role=BANK_OPERATOR in tenant_id=tenant-456
 * - user@acme.com has role=CUSTOMER in tenant_id=tenant-789
 */
@Entity
@Table(
    name = "user_roles",
    indexes = {
      @Index(name = "idx_user_tenant", columnList = "user_id,tenant_id"),
      @Index(name = "idx_tenant_role", columnList = "tenant_id,role_id"),
    },
    uniqueConstraints = {
      @UniqueConstraint(
          name = "uk_user_tenant_role",
          columnNames = {"user_id", "tenant_id", "role_id"})
    })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class UserRoleEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "id")
  private UUID id;

  @Column(name = "user_id", nullable = false, length = 255)
  private String userId;

  @Column(name = "tenant_id", nullable = false)
  private UUID tenantId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "role_id", nullable = false, referencedColumnName = "id")
  private RoleEntity role;

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @Column(name = "created_by", length = 255)
  private String createdBy;

  @PrePersist
  protected void onCreate() {
    createdAt = LocalDateTime.now();
  }
}
