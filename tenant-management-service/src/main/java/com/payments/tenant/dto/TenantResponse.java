package com.payments.tenant.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.payments.tenant.entity.TenantEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

/**
 * Tenant Response DTO.
 *
 * <p>Used for GET endpoints. Contains all readable tenant information including audit fields.
 *
 * <p>Fields:
 * - tenantId: Auto-generated identifier
 * - tenantName: Tenant name
 * - tenantType: Type (BANK, FINTECH, etc.)
 * - status: Current status (ACTIVE, SUSPENDED, INACTIVE)
 * - contactEmail: Contact email (never null)
 * - Audit fields: createdAt, updatedAt, createdBy, updatedBy
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TenantResponse {

  private String tenantId;
  private String tenantName;
  private TenantEntity.TenantType tenantType;
  private TenantEntity.TenantStatus status;
  private String registrationNumber;
  private String taxNumber;
  private String contactEmail;
  private String contactPhone;
  private String addressLine1;
  private String addressLine2;
  private String city;
  private String province;
  private String postalCode;
  private String country;
  private String timezone;
  private String currency;

  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ")
  private OffsetDateTime createdAt;

  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ")
  private OffsetDateTime updatedAt;

  private String createdBy;
  private String updatedBy;

  /**
   * Convert TenantEntity to TenantResponse DTO.
   *
   * @param entity TenantEntity
   * @return TenantResponse
   */
  public static TenantResponse from(TenantEntity entity) {
    return TenantResponse.builder()
        .tenantId(entity.getTenantId())
        .tenantName(entity.getTenantName())
        .tenantType(entity.getTenantType())
        .status(entity.getStatus())
        .registrationNumber(entity.getRegistrationNumber())
        .taxNumber(entity.getTaxNumber())
        .contactEmail(entity.getContactEmail())
        .contactPhone(entity.getContactPhone())
        .addressLine1(entity.getAddressLine1())
        .addressLine2(entity.getAddressLine2())
        .city(entity.getCity())
        .province(entity.getProvince())
        .postalCode(entity.getPostalCode())
        .country(entity.getCountry())
        .timezone(entity.getTimezone())
        .currency(entity.getCurrency())
        .createdAt(entity.getCreatedAt())
        .updatedAt(entity.getUpdatedAt())
        .createdBy(entity.getCreatedBy())
        .updatedBy(entity.getUpdatedBy())
        .build();
  }
}
