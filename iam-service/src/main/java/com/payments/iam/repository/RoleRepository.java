package com.payments.iam.repository;

import com.payments.iam.entity.RoleEntity;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * RoleRepository - Data access for Role entities.
 *
 * <p>Provides efficient role lookups with caching for O(1) performance.
 */
@Repository
public interface RoleRepository extends JpaRepository<RoleEntity, UUID> {

  /**
   * Find role by name with caching.
   *
   * <p>Cache: "roles" with key = role name
   *
   * @param name the role name
   * @return Optional containing the role if found
   */
  @Cacheable(value = "roles", key = "#name")
  @Query("SELECT r FROM RoleEntity r WHERE r.name = :name")
  Optional<RoleEntity> findByName(@Param("name") String name);

  /**
   * Check if a role exists by name.
   *
   * @param name the role name
   * @return true if role exists
   */
  boolean existsByName(String name);

  /**
   * Find role by name (uncached, for updates).
   *
   * @param name the role name
   * @return Optional containing the role if found
   */
  @Query("SELECT r FROM RoleEntity r WHERE r.name = :name")
  Optional<RoleEntity> findByNameNoCache(@Param("name") String name);
}
