package com.payments.domain.tenant;

import com.payments.domain.shared.*;
import jakarta.persistence.*;
import java.time.Instant;
import lombok.*;

/** Tenant Configuration (Entity within Tenant Aggregate) */
@Entity
@Table(name = "tenant_configs")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Data
public class TenantConfiguration {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "config_id")
  private Long id;

  @Embedded
  @AttributeOverride(name = "value", column = @Column(name = "tenant_id"))
  private TenantId tenantId;

  @Column(name = "config_key")
  private String configKey;

  @Column(name = "config_value")
  private String configValue;

  @Enumerated(EnumType.STRING)
  @Column(name = "config_type")
  private ConfigurationType configType;

  @Transient private String description;

  @Column(name = "is_encrypted")
  private Boolean isEncrypted;

  @Column(name = "is_sensitive")
  private Boolean isSensitive;

  @Column(name = "created_at")
  private Instant createdAt;

  @Column(name = "updated_at")
  private Instant updatedAt;

  @Column(name = "created_by")
  private String createdBy;

  @Column(name = "updated_by")
  private String updatedBy;

  public static TenantConfiguration create(
      TenantId tenantId,
      String configKey,
      String configValue,
      ConfigurationType configType,
      String createdBy) {
    TenantConfiguration config = new TenantConfiguration();
    config.tenantId = tenantId;
    config.configKey = configKey;
    config.configValue = configValue;
    config.configType = configType;
    config.isEncrypted = false;
    config.createdAt = Instant.now();
    config.updatedAt = Instant.now();
    config.createdBy = createdBy;
    config.updatedBy = createdBy;

    return config;
  }

  public void updateValue(String value, String updatedBy) {
    this.configValue = value;
    this.updatedAt = Instant.now();
    this.updatedBy = updatedBy;
  }
}
