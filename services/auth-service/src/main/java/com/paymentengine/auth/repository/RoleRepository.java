package com.paymentengine.auth.repository;

import com.paymentengine.auth.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RoleRepository extends JpaRepository<Role, UUID> {
    
    Optional<Role> findByName(String name);
    
    List<Role> findByNameIn(List<String> names);
    
    @Query("SELECT r FROM Role r JOIN r.permissions p WHERE p.name = :permissionName")
    List<Role> findByPermissionName(@Param("permissionName") String permissionName);
    
    @Query("SELECT r FROM Role r JOIN r.permissions p WHERE p.resourceType = :resourceType")
    List<Role> findByResourceType(@Param("resourceType") String resourceType);
    
    boolean existsByName(String name);
}