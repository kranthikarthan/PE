package com.payments.paymentinitiation.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

/** JPA Entity for pain.001 validation results */
@Entity
@Table(name = "pain001_validation_results")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Pain001ValidationResultEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "pain001_message_id", nullable = false)
  private Pain001MessageEntity pain001Message;

  @Enumerated(EnumType.STRING)
  @Column(name = "validation_type", nullable = false, length = 20)
  private ValidationType validationType;

  @Column(name = "validation_result", nullable = false)
  private Boolean validationResult;

  @Column(name = "validation_message", length = 500)
  private String validationMessage;

  @Column(name = "validation_details", columnDefinition = "JSONB")
  private String validationDetails;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;

  /** Validation Type Enumeration */
  public enum ValidationType {
    XSD_VALIDATION("XSD_VALIDATION"),
    BUSINESS_RULES("BUSINESS_RULES"),
    SCHEMA_VALIDATION("SCHEMA_VALIDATION"),
    CONTENT_VALIDATION("CONTENT_VALIDATION");

    private final String value;

    ValidationType(String value) {
      this.value = value;
    }

    public String getValue() {
      return value;
    }
  }
}
