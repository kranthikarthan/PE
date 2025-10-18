package com.payments.notification.repository;

import com.payments.notification.domain.model.NotificationPreferenceEntity;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

/**
 * JPA Repository for {@link NotificationPreferenceEntity}.
 *
 * <p>Provides:
 * - User preference lookups with caching
 * - GDPR compliance operations
 * - Multi-tenant preference management
 *
 * @author Payment Engine
 */
@Repository
public interface NotificationPreferenceRepository extends JpaRepository<NotificationPreferenceEntity, UUID> {

  /**
   * Find user preferences by tenant and user ID with caching.
   *
   * <p>Results are cached in Redis for 30 minutes since preferences change infrequently.
   *
   * @param tenantId the tenant ID
   * @param userId the user ID
   * @return optional containing preferences if found
   */
  @Cacheable(
      value = "notification_preferences",
      key = "#tenantId + ':' + #userId",
      unless = "#result == null")
  Optional<NotificationPreferenceEntity> findByTenantIdAndUserId(
      String tenantId, String userId);

  /**
   * Check if preferences exist for a user.
   *
   * @param tenantId the tenant ID
   * @param userId the user ID
   * @return true if preferences exist
   */
  boolean existsByTenantIdAndUserId(String tenantId, String userId);

  /**
   * Save or update user preferences with cache eviction.
   *
   * @param preference the preference to save
   * @return saved preference
   */
  @Override
  @CacheEvict(
      value = "notification_preferences",
      key = "#preference.tenantId + ':' + #preference.userId")
  <S extends NotificationPreferenceEntity> S save(S preference);

  /**
   * Delete user preferences with cache eviction (GDPR compliance).
   *
   * @param preference the preference to delete
   */
  @Override
  @CacheEvict(
      value = "notification_preferences",
      key = "#preference.tenantId + ':' + #preference.userId")
  void delete(NotificationPreferenceEntity preference);

  /**
   * Delete preferences by tenant and user ID (GDPR right to be forgotten).
   *
   * @param tenantId the tenant ID
   * @param userId the user ID (can be wildcard for tenant cleanup)
   * @return number of rows deleted
   */
  @Modifying
  @Transactional
  @CacheEvict(value = "notification_preferences", allEntries = true)
  @Query("DELETE FROM NotificationPreferenceEntity p WHERE p.tenantId = :tenantId AND p.userId = :userId")
  int deleteByTenantIdAndUserId(@Param("tenantId") String tenantId, @Param("userId") String userId);

  /**
   * Delete all preferences for a tenant (account closure).
   *
   * @param tenantId the tenant ID
   * @return number of rows deleted
   */
  @Modifying
  @Transactional
  @CacheEvict(value = "notification_preferences", allEntries = true)
  @Query("DELETE FROM NotificationPreferenceEntity p WHERE p.tenantId = :tenantId")
  int deleteByTenantId(@Param("tenantId") String tenantId);

  /**
   * Count preferences for a tenant (usage metrics).
   *
   * @param tenantId the tenant ID
   * @return count of preferences
   */
  long countByTenantId(String tenantId);

  /**
   * Find users with transaction alerts enabled (for mass notifications).
   *
   * @param tenantId the tenant ID
   * @return list of user IDs with alerts enabled
   */
  @Query(
      "SELECT p.userId FROM NotificationPreferenceEntity p "
          + "WHERE p.tenantId = :tenantId AND p.transactionAlertsOptIn = true")
  java.util.List<String> findUsersWithTransactionAlertsEnabled(@Param("tenantId") String tenantId);

  /**
   * Find users with marketing emails enabled (for campaigns).
   *
   * @param tenantId the tenant ID
   * @return list of user IDs with marketing opt-in
   */
  @Query(
      "SELECT p.userId FROM NotificationPreferenceEntity p "
          + "WHERE p.tenantId = :tenantId AND p.marketingOptIn = true")
  java.util.List<String> findUsersWithMarketingEnabled(@Param("tenantId") String tenantId);
}
