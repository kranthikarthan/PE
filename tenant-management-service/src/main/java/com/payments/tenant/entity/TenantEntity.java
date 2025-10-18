package com.payments.tenant.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serializable;
import java.time.OffsetDateTime;

/**
 * Tenant Entity - Represents a tenant (bank/organization) in the system.
 *
 * <p>Multi-Tenancy: This entity is subject to Row-Level Security (RLS) in PostgreSQL. All queries
 * are automatically scoped to the current tenant via the `app.current_tenant_id` setting.
 *
 * <p>Caching: Tenants are cached in Redis (10-minute TTL) for O(1) lookups.
 *
 * <p>Audit: All create/update operations are tracked with timestamps and user info.
 */
@Entity
@Table(
    name = "tenants",
    uniqueConstraints = {@UniqueConstraint(columnNames = "tenant_id")})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(exclude = {"createdAt", "updatedAt"})
@ToString(exclude = {"createdAt", "updatedAt"})
public class TenantEntity implements Serializable {

  private static final long serialVersionUID = 1L;

  /** Unique tenant identifier (e.g., STD-001). */
  @Id
  @Column(name = "tenant_id", nullable = false, length = 20)
  private String tenantId;

  /** Human-readable tenant name (e.g., Standard Bank). */
  @Column(name = "tenant_name", nullable = false, length = 200)
  private String tenantName;

  /** Type of tenant: BANK, FINANCIAL_INSTITUTION, FINTECH, CORPORATE. */
  @Column(name = "tenant_type", nullable = false, length = 50)
  @Enumerated(EnumType.STRING)
  private TenantType tenantType;

  /** Current status: ACTIVE, INACTIVE, SUSPENDED, PENDING_APPROVAL. */
  @Column(name = "status", nullable = false, length = 20, columnDefinition = "VARCHAR(20) DEFAULT 'ACTIVE'")
  @Enumerated(EnumType.STRING)
  @Builder.Default
  private TenantStatus status = TenantStatus.ACTIVE;

  /** Business registration number. */
  @Column(name = "registration_number", length = 50)
  private String registrationNumber;

  /** Tax identification number. */
  @Column(name = "tax_number", length = 50)
  private String taxNumber;

  /** Primary contact email address. */
  @Column(name = "contact_email", nullable = false, length = 200)
  private String contactEmail;

  /** Primary contact phone number. */
  @Column(name = "contact_phone", length = 20)
  private String contactPhone;

  /** Address line 1. */
  @Column(name = "address_line1", length = 200)
  private String addressLine1;

  /** Address line 2. */
  @Column(name = "address_line2", length = 200)
  private String addressLine2;

  /** City. */
  @Column(name = "city", length = 100)
  private String city;

  /** Province/State. */
  @Column(name = "province", length = 100)
  private String province;

  /** Postal/ZIP code. */
  @Column(name = "postal_code", length = 10)
  private String postalCode;

  /** Country code (ISO 3166-1 alpha-3). Default: ZAF (South Africa). */
  @Column(name = "country", length = 3, columnDefinition = "VARCHAR(3) DEFAULT 'ZAF'")
  @Builder.Default
  private String country = "ZAF";

  /** Timezone for the tenant (e.g., Africa/Johannesburg). */
  @Column(name = "timezone", length = 50, columnDefinition = "VARCHAR(50) DEFAULT 'Africa/Johannesburg'")
  @Builder.Default
  private String timezone = "Africa/Johannesburg";

  /** Primary currency (ISO 4217 code). Default: ZAR. */
  @Column(name = "currency", length = 3, columnDefinition = "VARCHAR(3) DEFAULT 'ZAR'")
  @Builder.Default
  private String currency = "ZAR";

  /** Timestamp when record was created. */
  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private OffsetDateTime createdAt;

  /** Timestamp when record was last updated. */
  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  private OffsetDateTime updatedAt;

  /** User who created the record (audit trail). */
  @Column(name = "created_by", length = 100)
  private String createdBy;

  /** User who last updated the record (audit trail). */
  @Column(name = "updated_by", length = 100)
  private String updatedBy;

  /**
   * Check if tenant is active and can process transactions.
   *
   * @return true if tenant is ACTIVE
   */
  public boolean isActive() {
    return status == TenantStatus.ACTIVE;
  }

  /**
   * Check if tenant is suspended (inactive but not deleted).
   *
   * @return true if tenant is SUSPENDED
   */
  public boolean isSuspended() {
    return status == TenantStatus.SUSPENDED;
  }

  /**
   * Activate tenant if pending approval.
   *
   * @param activatedBy User who approved activation
   */
  public void activate(String activatedBy) {
    if (status == TenantStatus.PENDING_APPROVAL) {
      this.status = TenantStatus.ACTIVE;
      this.updatedBy = activatedBy;
    }
  }

  /**
   * Suspend tenant operations temporarily.
   *
   * @param suspendedBy User who suspended
   */
  public void suspend(String suspendedBy) {
    if (status != TenantStatus.INACTIVE) {
      this.status = TenantStatus.SUSPENDED;
      this.updatedBy = suspendedBy;
    }
  }

  /**
   * Deactivate tenant (soft delete).
   *
   * @param deactivatedBy User who deactivated
   */
  public void deactivate(String deactivatedBy) {
    this.status = TenantStatus.INACTIVE;
    this.updatedBy = deactivatedBy;
  }

  /** Tenant type enumeration. */
  public enum TenantType {
    BANK,
    FINANCIAL_INSTITUTION,
    FINTECH,
    CORPORATE
  }

  /** Tenant status enumeration. */
  public enum TenantStatus {
    ACTIVE,
    INACTIVE,
    SUSPENDED,
    PENDING_APPROVAL
  }
}
