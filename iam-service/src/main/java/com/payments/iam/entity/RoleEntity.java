package com.payments.iam.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Role Entity - Defines RBAC roles available in the system.
 *
 * <p>Supported Roles:
 * - CUSTOMER: Individual user, can initiate payments, view own data
 * - BUSINESS_CUSTOMER: Corporate user, can do bulk payments, dual authorization
 * - BANK_OPERATOR: Bank staff, read-only access, payment reversal
 * - BANK_ADMIN: Bank administrator, can manage users, configure system
 * - SUPPORT_AGENT: Support staff, limited view, no PII access
 */
@Entity
@Table(name = "roles")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class RoleEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "id")
  private UUID id;

  @Column(name = "name", nullable = false, unique = true, length = 50)
  private String name;

  @Column(name = "description", columnDefinition = "TEXT")
  private String description;

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @PrePersist
  protected void onCreate() {
    createdAt = LocalDateTime.now();
  }

  /**
   * Enum for well-known roles.
   */
  public enum RoleName {
    CUSTOMER("customer", "Individual customer"),
    BUSINESS_CUSTOMER("business_customer", "Corporate/business customer"),
    BANK_OPERATOR("bank_operator", "Bank operator - read-only access"),
    BANK_ADMIN("bank_admin", "Bank administrator"),
    SUPPORT_AGENT("support_agent", "Support staff");

    private final String value;
    private final String description;

    RoleName(String value, String description) {
      this.value = value;
      this.description = description;
    }

    public String getValue() {
      return value;
    }

    public String getDescription() {
      return description;
    }
  }
}
