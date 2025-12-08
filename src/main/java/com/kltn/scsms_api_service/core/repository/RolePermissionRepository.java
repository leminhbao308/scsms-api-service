package com.kltn.scsms_api_service.core.repository;

import com.kltn.scsms_api_service.core.entity.Permission;
import com.kltn.scsms_api_service.core.entity.Role;
import com.kltn.scsms_api_service.core.entity.RolePermission;
import com.kltn.scsms_api_service.core.entity.compositId.RolePermissionId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RolePermissionRepository extends JpaRepository<RolePermission, RolePermissionId> {

    boolean existsByRoleAndPermission(Role role, Permission permission);

    Optional<RolePermission> findByRoleAndPermission(Role role, Permission permission);

    List<RolePermission> findByRole(Role role);

    List<RolePermission> findByPermission(Permission permission);

    @Query("SELECT rp FROM RolePermission rp WHERE rp.role.roleId = :roleId")
    List<RolePermission> findByRoleId(@Param("roleId") UUID roleId);

    @Query("SELECT rp FROM RolePermission rp WHERE rp.permission.permissionId = :permissionId")
    List<RolePermission> findByPermissionId(@Param("permissionId") UUID permissionId);

    void deleteByRole(Role role);

    void deleteByPermission(Permission permission);

    @Query("SELECT rp FROM RolePermission rp WHERE rp.role.roleCode = :roleCode")
    List<RolePermission> findByRoleCode(@Param("roleCode") String roleCode);

    @Query("SELECT rp FROM RolePermission rp WHERE rp.permission.permissionCode = :permissionCode")
    List<RolePermission> findByPermissionCode(@Param("permissionCode") String permissionCode);

    @Query("SELECT COUNT(rp) FROM RolePermission rp WHERE rp.role.roleCode = :roleCode")
    long countByRoleCode(@Param("roleCode") String roleCode);

    @Query("SELECT rp FROM RolePermission rp " +
            "JOIN FETCH rp.role r " +
            "JOIN FETCH rp.permission p " +
            "WHERE r.roleCode = :roleCode AND p.module = :module")
    List<RolePermission> findByRoleCodeAndModule(@Param("roleCode") String roleCode, @Param("module") String module);
}
