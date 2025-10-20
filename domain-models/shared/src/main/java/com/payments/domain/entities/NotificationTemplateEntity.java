package com.payments.domain.entities;

import com.payments.domain.events.*;
import com.payments.domain.exceptions.*;
import com.payments.domain.shared.*;
import com.payments.domain.valueobjects.*;
import java.time.Instant;
import java.util.Map;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Pure domain entity for Notification Template - no infrastructure dependencies Contains only
 * business logic and domain rules
 */
@Data
@Builder
@EqualsAndHashCode(callSuper = false)
public class NotificationTemplateEntity extends AggregateRoot<TemplateId> {

  private TemplateId templateId;
  private TenantId tenantId;
  private String name;
  private NotificationType type;
  private NotificationChannel channel;
  private String subject;
  private String content;
  private Map<String, Object> variables;
  private boolean isActive;
  private Instant createdAt;
  private Instant updatedAt;
  private String createdBy;
  private String updatedBy;

  // Default constructor for builder
  public NotificationTemplateEntity() {
    // Default constructor for builder compatibility
  }

  // Constructor for creating a new Template
  public NotificationTemplateEntity(
      TemplateId templateId,
      TenantId tenantId,
      String name,
      NotificationType type,
      NotificationChannel channel,
      String subject,
      String content,
      Map<String, Object> variables,
      String createdBy) {
    if (templateId == null
        || tenantId == null
        || name == null
        || type == null
        || channel == null
        || subject == null
        || content == null
        || createdBy == null) {
      throw new IllegalArgumentException("All template fields must be provided.");
    }

    this.templateId = templateId;
    this.tenantId = tenantId;
    this.name = name;
    this.type = type;
    this.channel = channel;
    this.subject = subject;
    this.content = content;
    this.variables = variables;
    this.isActive = true;
    this.createdAt = Instant.now();
    this.updatedAt = this.createdAt;
    this.createdBy = createdBy;
    this.updatedBy = createdBy;

    addDomainEvent(
        new TemplateCreatedEvent(templateId, tenantId, name, type, channel, this.createdAt));
  }

  // Private constructor for re-hydration from persistence (used by infrastructure layer)
  private NotificationTemplateEntity(
      TemplateId templateId,
      TenantId tenantId,
      String name,
      NotificationType type,
      NotificationChannel channel,
      String subject,
      String content,
      Map<String, Object> variables,
      boolean isActive,
      Instant createdAt,
      Instant updatedAt,
      String createdBy,
      String updatedBy) {
    this.templateId = templateId;
    this.tenantId = tenantId;
    this.name = name;
    this.type = type;
    this.channel = channel;
    this.subject = subject;
    this.content = content;
    this.variables = variables;
    this.isActive = isActive;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
    this.createdBy = createdBy;
    this.updatedBy = updatedBy;
  }

  public void updateTemplate(
      String name,
      String subject,
      String content,
      Map<String, Object> variables,
      String updatedBy) {
    if (!this.isActive) {
      throw new InvalidStateTransitionException("Cannot update an inactive template.");
    }
    this.name = name;
    this.subject = subject;
    this.content = content;
    this.variables = variables;
    this.updatedAt = Instant.now();
    this.updatedBy = updatedBy;
    addDomainEvent(new TemplateUpdatedEvent(templateId, tenantId, name, this.updatedAt));
  }

  public void activate() {
    if (this.isActive) {
      throw new InvalidStateTransitionException("Template is already active.");
    }
    this.isActive = true;
    this.updatedAt = Instant.now();
    addDomainEvent(new TemplateActivatedEvent(templateId, tenantId, name, this.updatedAt));
  }

  public void deactivate() {
    if (!this.isActive) {
      throw new InvalidStateTransitionException("Template is already inactive.");
    }
    this.isActive = false;
    this.updatedAt = Instant.now();
    addDomainEvent(new TemplateDeactivatedEvent(templateId, tenantId, name, this.updatedAt));
  }

  public String renderContent(Map<String, Object> context) {
    if (!this.isActive) {
      throw new InvalidStateTransitionException("Cannot render content from inactive template.");
    }
    // Simple template rendering logic - in real implementation, use a template engine
    String renderedContent = this.content;
    if (context != null) {
      for (Map.Entry<String, Object> entry : context.entrySet()) {
        String placeholder = "{{" + entry.getKey() + "}}";
        renderedContent = renderedContent.replace(placeholder, String.valueOf(entry.getValue()));
      }
    }
    return renderedContent;
  }

  public String renderSubject(Map<String, Object> context) {
    if (!this.isActive) {
      throw new InvalidStateTransitionException("Cannot render subject from inactive template.");
    }
    // Simple template rendering logic - in real implementation, use a template engine
    String renderedSubject = this.subject;
    if (context != null) {
      for (Map.Entry<String, Object> entry : context.entrySet()) {
        String placeholder = "{{" + entry.getKey() + "}}";
        renderedSubject = renderedSubject.replace(placeholder, String.valueOf(entry.getValue()));
      }
    }
    return renderedSubject;
  }

  // Compatibility methods for backward compatibility
  public String getId() {
    return templateId != null ? templateId.getValue() : null;
  }

  public String getNotificationType() {
    return type != null ? type.name() : null;
  }

  public String getEmailSubject() {
    return subject;
  }

  public String getEmailTemplate() {
    return content;
  }

  public String getPushTitle() {
    return subject;
  }

  public String getPushBody() {
    return content;
  }

  public String getSmsTemplate() {
    return content;
  }

  public void setEmailSubject(String emailSubject) {
    this.subject = emailSubject;
  }

  public void setEmailTemplate(String emailTemplate) {
    this.content = emailTemplate;
  }

  public void setPushTitle(String pushTitle) {
    this.subject = pushTitle;
  }

  public void setPushBody(String pushBody) {
    this.content = pushBody;
  }

  public void setSmsTemplate(String smsTemplate) {
    this.content = smsTemplate;
  }

  // Builder compatibility methods
  public static NotificationTemplateEntityBuilder builder() {
    return new NotificationTemplateEntityBuilder();
  }

  public static class NotificationTemplateEntityBuilder {
    private NotificationTemplateEntity templateEntity;

    public NotificationTemplateEntityBuilder() {
      this.templateEntity = new NotificationTemplateEntity();
    }

    public NotificationTemplateEntityBuilder id(java.util.UUID id) {
      this.templateEntity.templateId = TemplateId.of(id.toString());
      return this;
    }

    public NotificationTemplateEntityBuilder templateId(TemplateId templateId) {
      this.templateEntity.templateId = templateId;
      return this;
    }

    public NotificationTemplateEntityBuilder tenantId(TenantId tenantId) {
      this.templateEntity.tenantId = tenantId;
      return this;
    }

    public NotificationTemplateEntityBuilder name(String name) {
      this.templateEntity.name = name;
      return this;
    }

    public NotificationTemplateEntityBuilder type(NotificationType type) {
      this.templateEntity.type = type;
      return this;
    }

    public NotificationTemplateEntityBuilder notificationType(NotificationType type) {
      this.templateEntity.type = type;
      return this;
    }

    public NotificationTemplateEntityBuilder channel(NotificationChannel channel) {
      this.templateEntity.channel = channel;
      return this;
    }

    public NotificationTemplateEntityBuilder subject(String subject) {
      this.templateEntity.subject = subject;
      return this;
    }

    public NotificationTemplateEntityBuilder emailSubject(String subject) {
      this.templateEntity.subject = subject;
      return this;
    }

    public NotificationTemplateEntityBuilder content(String content) {
      this.templateEntity.content = content;
      return this;
    }

    public NotificationTemplateEntityBuilder emailTemplate(String content) {
      this.templateEntity.content = content;
      return this;
    }

    public NotificationTemplateEntityBuilder pushTitle(String title) {
      this.templateEntity.subject = title;
      return this;
    }

    public NotificationTemplateEntityBuilder pushBody(String body) {
      this.templateEntity.content = body;
      return this;
    }

    public NotificationTemplateEntityBuilder smsTemplate(String template) {
      this.templateEntity.content = template;
      return this;
    }

    public NotificationTemplateEntityBuilder variables(Map<String, Object> variables) {
      this.templateEntity.variables = variables;
      return this;
    }

    public NotificationTemplateEntityBuilder isActive(boolean isActive) {
      this.templateEntity.isActive = isActive;
      return this;
    }

    public NotificationTemplateEntityBuilder active(boolean active) {
      this.templateEntity.isActive = active;
      return this;
    }

    public NotificationTemplateEntityBuilder createdAt(Instant createdAt) {
      this.templateEntity.createdAt = createdAt;
      return this;
    }

    public NotificationTemplateEntityBuilder updatedAt(Instant updatedAt) {
      this.templateEntity.updatedAt = updatedAt;
      return this;
    }

    public NotificationTemplateEntityBuilder createdBy(String createdBy) {
      this.templateEntity.createdBy = createdBy;
      return this;
    }

    public NotificationTemplateEntityBuilder updatedBy(String updatedBy) {
      this.templateEntity.updatedBy = updatedBy;
      return this;
    }

    public NotificationTemplateEntity build() {
      return this.templateEntity;
    }
  }
}
