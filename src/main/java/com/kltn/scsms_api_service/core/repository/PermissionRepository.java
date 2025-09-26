package com.kltn.scsms_api_service.core.repository;

import com.kltn.scsms_api_service.core.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, UUID> {
    
    Optional<Permission> findByPermissionName(String permissionName);
    
    boolean existsByPermissionName(String permissionName);
    
    List<Permission> findByModule(String module);
    
    List<Permission> findByPermissionNameIn(Set<String> permissionNames);
    
    @Query("SELECT p FROM Permission p WHERE p.module = :module ORDER BY p.permissionName")
    List<Permission> findByModuleOrderByPermissionName(@Param("module") String module);
    
    @Query("SELECT DISTINCT p.module FROM Permission p ORDER BY p.module")
    List<String> findDistinctModules();
    
    @Query("SELECT p FROM Permission p WHERE p.permissionName LIKE %:keyword% OR p.description LIKE %:keyword%")
    List<Permission> searchByKeyword(@Param("keyword") String keyword);

    Optional<Permission> findByPermissionCode(String permissionCode);

    @Query("SELECT p FROM Permission p WHERE p.module = :module ORDER BY p.permissionName")
    List<Permission> findByModuleOrderByName(@Param("module") String module);

    @Query("SELECT DISTINCT p.module FROM Permission p ORDER BY p.module")
    List<String> findAllModules();

    @Query("SELECT p FROM Permission p WHERE p.permissionCode IN :codes")
    List<Permission> findByPermissionCodes(@Param("codes") List<String> codes);

    @Query("SELECT COUNT(p) FROM Permission p WHERE p.module = :module")
    long countByModule(@Param("module") String module);

    @Query("SELECT p FROM Permission p WHERE p.isActive = true AND p.isDeleted = false")
    List<Permission> findActivePermissions();
    
    @Query("SELECT p.permissionCode FROM Permission p " +
        "JOIN p.rolePermissions rp " +
        "JOIN rp.role r " +
        "JOIN r.users u " +
        "WHERE u.userId = :userId")
    Set<String> findPermissionCodesByUserId(@Param("userId") UUID userId);
}
