package com.payments.analytics.domain;

import com.payments.domain.shared.TenantContext;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Report Template entity for defining report structures.
 *
 * <p>This entity represents a template for generating reports,
 * including the structure, parameters, and configuration.
 *
 * @since PE-415
 */
@Entity
@Table(name = "report_templates", indexes = {
    @Index(name = "idx_report_templates_template_name", columnList = "templateName"),
    @Index(name = "idx_report_templates_tenant_id", columnList = "tenantId"),
    @Index(name = "idx_report_templates_business_unit_id", columnList = "businessUnitId"),
    @Index(name = "idx_report_templates_category", columnList = "category"),
    @Index(name = "idx_report_templates_status", columnList = "status")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "template_name", nullable = false)
    private String templateName;

    @Column(name = "description")
    private String description;

    @Column(name = "category", nullable = false)
    @Enumerated(EnumType.STRING)
    private ReportCategory category;

    @Column(name = "report_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private ReportType reportType;

    @Column(name = "output_format", nullable = false)
    @Enumerated(EnumType.STRING)
    private OutputFormat outputFormat;

    @Column(name = "template_content", columnDefinition = "TEXT")
    private String templateContent;

    @Column(name = "parameters", columnDefinition = "TEXT")
    private String parameters; // JSON string for report parameters

    @Column(name = "data_source", nullable = false)
    private String dataSource;

    @Column(name = "query", columnDefinition = "TEXT")
    private String query;

    @Column(name = "schedule")
    private String schedule; // Cron expression for scheduled reports

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private TemplateStatus status;

    @Column(name = "is_public", nullable = false)
    private Boolean isPublic;

    @Column(name = "access_level", nullable = false)
    @Enumerated(EnumType.STRING)
    private AccessLevel accessLevel;

    @Column(name = "version")
    private String version;

    @Column(name = "created_by", nullable = false)
    private String createdBy;

    @Column(name = "last_modified_by")
    private String lastModifiedBy;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "business_unit_id")
    private UUID businessUnitId;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @PrePersist
    @PreUpdate
    public void setTenantAndBusinessUnit() {
        if (TenantContext.getCurrentTenantId() != null) {
            this.tenantId = TenantContext.getCurrentTenantId();
        }
        if (TenantContext.getCurrentBusinessUnitId() != null) {
            this.businessUnitId = TenantContext.getCurrentBusinessUnitId();
        }
    }

    /**
     * Report categories for organizing templates.
     */
    public enum ReportCategory {
        PAYMENT_ANALYTICS,
        SETTLEMENT_ANALYTICS,
        RECONCILIATION_ANALYTICS,
        BATCH_PROCESSING_ANALYTICS,
        PERFORMANCE_ANALYTICS,
        SECURITY_ANALYTICS,
        COMPLIANCE_ANALYTICS,
        BUSINESS_INTELLIGENCE,
        OPERATIONAL_ANALYTICS,
        FINANCIAL_ANALYTICS
    }

    /**
     * Report types for different report structures.
     */
    public enum ReportType {
        SUMMARY,
        DETAILED,
        TREND,
        COMPARATIVE,
        DRILL_DOWN,
        DASHBOARD,
        ALERT,
        EXCEPTION,
        AUDIT,
        COMPLIANCE
    }

    /**
     * Output formats for reports.
     */
    public enum OutputFormat {
        PDF,
        EXCEL,
        CSV,
        JSON,
        XML,
        HTML,
        PNG,
        JPEG
    }

    /**
     * Template status for lifecycle management.
     */
    public enum TemplateStatus {
        DRAFT,
        ACTIVE,
        INACTIVE,
        DEPRECATED,
        ARCHIVED
    }

    /**
     * Access levels for report templates.
     */
    public enum AccessLevel {
        PUBLIC,
        TENANT,
        BUSINESS_UNIT,
        PRIVATE
    }
}
