package com.payments.notification.repository;

import com.payments.notification.domain.model.NotificationTemplateEntity;
import com.payments.notification.domain.model.NotificationType;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * JPA Repository for {@link NotificationTemplateEntity}.
 *
 * <p>Provides:
 * - Template lookups by type and tenant
 * - Caching for frequently accessed templates
 * - Active/inactive template filtering
 *
 * @author Payment Engine
 */
@Repository
public interface NotificationTemplateRepository extends JpaRepository<NotificationTemplateEntity, UUID> {

  /**
   * Find a template by tenant and notification type with caching.
   *
   * <p>Results are cached in Redis for 10 minutes.
   *
   * @param tenantId the tenant ID
   * @param notificationType the notification type
   * @return optional containing template if found
   */
  @Cacheable(
      value = "notification_templates",
      key = "#tenantId + ':' + #notificationType.name()",
      unless = "#result == null")
  @Query(
      "SELECT t FROM NotificationTemplateEntity t "
          + "WHERE t.tenantId = :tenantId "
          + "AND t.notificationType = :notificationType "
          + "AND t.isActive = true "
          + "ORDER BY t.updatedAt DESC")
  Optional<NotificationTemplateEntity> findActiveTemplateByTenantAndType(
      @Param("tenantId") String tenantId,
      @Param("notificationType") NotificationType notificationType);

  /**
   * Find all active templates for a tenant.
   *
   * @param tenantId the tenant ID
   * @param pageable pagination parameters
   * @return page of active templates
   */
  Page<NotificationTemplateEntity> findByTenantIdAndIsActiveOrderByUpdatedAtDesc(
      String tenantId, Boolean isActive, Pageable pageable);

  /**
   * Find template by unique name per tenant with caching.
   *
   * @param tenantId the tenant ID
   * @param name the template name
   * @return optional containing template if found
   */
  @Cacheable(
      value = "notification_templates",
      key = "#tenantId + ':name:' + #name",
      unless = "#result == null")
  Optional<NotificationTemplateEntity> findByTenantIdAndName(String tenantId, String name);

  /**
   * Check if a template name exists for a tenant.
   *
   * @param tenantId the tenant ID
   * @param name the template name
   * @return true if template exists
   */
  boolean existsByTenantIdAndName(String tenantId, String name);

  /**
   * Find all templates by notification type for a tenant.
   *
   * @param tenantId the tenant ID
   * @param notificationType the notification type
   * @return list of templates
   */
  List<NotificationTemplateEntity> findByTenantIdAndNotificationType(
      String tenantId, NotificationType notificationType);

  /**
   * Count active templates for a tenant.
   *
   * @param tenantId the tenant ID
   * @return count of active templates
   */
  long countByTenantIdAndIsActive(String tenantId, Boolean isActive);

  /**
   * Deactivate all templates for a tenant (for migration or cleanup).
   *
   * @param tenantId the tenant ID
   */
  @Modifying
  @Transactional
  @CacheEvict(value = "notification_templates", allEntries = true)
  @Query("UPDATE NotificationTemplateEntity t SET t.isActive = false WHERE t.tenantId = :tenantId")
  void deactivateAllForTenant(@Param("tenantId") String tenantId);

  /**
   * Activate templates by type for a tenant.
   *
   * @param tenantId the tenant ID
   * @param notificationType the notification type
   */
  @Modifying
  @Transactional
  @CacheEvict(value = "notification_templates", allEntries = true)
  @Query(
      "UPDATE NotificationTemplateEntity t SET t.isActive = true "
          + "WHERE t.tenantId = :tenantId AND t.notificationType = :notificationType")
  void activateTemplatesForType(
      @Param("tenantId") String tenantId,
      @Param("notificationType") NotificationType notificationType);

  /**
   * Save template with cache eviction.
   *
   * @param template the template to save
   * @return saved template
   */
  @Override
  @CacheEvict(value = "notification_templates", allEntries = true)
  <S extends NotificationTemplateEntity> S save(S template);

  /**
   * Delete template with cache eviction.
   *
   * @param template the template to delete
   */
  @Override
  @CacheEvict(value = "notification_templates", allEntries = true)
  void delete(NotificationTemplateEntity template);
}
