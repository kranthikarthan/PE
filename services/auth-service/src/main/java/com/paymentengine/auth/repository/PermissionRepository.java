package com.paymentengine.auth.repository;

import com.paymentengine.auth.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, UUID> {
    
    Optional<Permission> findByName(String name);
    
    List<Permission> findByResourceType(String resourceType);
    
    List<Permission> findByResourceTypeAndResourceId(String resourceType, String resourceId);
    
    @Query("SELECT p FROM Permission p WHERE p.name LIKE %:name%")
    List<Permission> findByNameContaining(@Param("name") String name);
    
    @Query("SELECT p FROM Permission p WHERE p.resourceType = :resourceType AND p.name = :name")
    Optional<Permission> findByResourceTypeAndName(@Param("resourceType") String resourceType, 
                                                  @Param("name") String name);
    
    boolean existsByName(String name);
}