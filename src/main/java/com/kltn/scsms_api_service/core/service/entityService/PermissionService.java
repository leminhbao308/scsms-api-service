package com.kltn.scsms_api_service.core.service.entityService;

import com.kltn.scsms_api_service.core.entity.*;
import com.kltn.scsms_api_service.core.repository.PermissionRepository;
import com.kltn.scsms_api_service.core.repository.RolePermissionRepository;
import com.kltn.scsms_api_service.core.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PermissionService {

    private final PermissionRepository permissionRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final RoleRepository roleRepository;

    /**
     * Get all permissions grouped by module
     */
    public Map<String, List<Permission>> getPermissionsGroupedByModule() {
        List<Permission> permissions = permissionRepository.findActivePermissions();
        return permissions.stream()
                .collect(Collectors.groupingBy(Permission::getModule));
    }

    /**
     * Get permissions by module code
     */
    public List<Permission> getPermissionsByModule(String moduleCode) {
        return permissionRepository.findByModuleOrderByName(moduleCode);
    }

    /**
     * Check if user has specific permission by permission code
     */
    public boolean hasPermission(User user, String permissionCode) {
        if (user == null || permissionCode == null || permissionCode.trim().isEmpty()) {
            return false;
        }

        return user.getRole().getRolePermissions().stream()
                .map(RolePermission::getPermission)
                .anyMatch(permission -> permissionCode.equals(permission.getPermissionCode()));
    }

    /**
     * Check if user has any of the specified permissions
     */
    public boolean hasAnyPermission(User user, String... permissionCodes) {
        if (user == null || permissionCodes == null || permissionCodes.length == 0) {
            return false;
        }

        Set<String> userPermissionCodes = getUserPermissionCodes(user);
        return Arrays.stream(permissionCodes)
                .anyMatch(userPermissionCodes::contains);
    }

    /**
     * Check if user has all of the specified permissions
     */
    public boolean hasAllPermissions(User user, String... permissionCodes) {
        if (user == null || permissionCodes == null || permissionCodes.length == 0) {
            return false;
        }

        Set<String> userPermissionCodes = getUserPermissionCodes(user);
        return Arrays.stream(permissionCodes)
                .allMatch(userPermissionCodes::contains);
    }

    /**
     * Get all permission codes for a user
     */
    public Set<String> getUserPermissionCodes(User user) {
        if (user == null) {
            return Collections.emptySet();
        }

        return user.getRole().getRolePermissions().stream()
                .map(rolePermission -> rolePermission.getPermission().getPermissionCode())
                .collect(Collectors.toSet());
    }

    /**
     * Get all permissions for a user
     */
    public Set<Permission> getUserPermissions(User user) {
        if (user == null) {
            return Collections.emptySet();
        }

        return user.getRole().getRolePermissions().stream()
                .map(RolePermission::getPermission)
                .collect(Collectors.toSet());
    }

    /**
     * Assign permission to role by permission code
     */
    @Transactional
    public boolean assignPermissionToRole(UUID roleId, String permissionCode, User grantedBy) {
        Optional<Role> roleOpt = roleRepository.findById(roleId);
        Optional<Permission> permissionOpt = permissionRepository.findByPermissionCode(permissionCode);

        if (roleOpt.isEmpty()) {
            log.error("Role not found with ID: {}", roleId);
            return false;
        }

        if (permissionOpt.isEmpty()) {
            log.error("Permission not found with code: {}", permissionCode);
            return false;
        }

        Role role = roleOpt.get();
        Permission permission = permissionOpt.get();

        // Check if permission is already assigned
        if (rolePermissionRepository.existsByRoleAndPermission(role, permission)) {
            log.warn("Permission '{}' already assigned to role '{}'", permissionCode, role.getRoleName());
            return false;
        }

        RolePermission rolePermission = new RolePermission();
        rolePermission.setRole(role);
        rolePermission.setPermission(permission);
        rolePermission.setGrantedBy(grantedBy);
        rolePermission.setGrantedAt(LocalDateTime.now());

        rolePermissionRepository.save(rolePermission);
        log.info("Permission '{}' assigned to role '{}' by user '{}'",
                permissionCode, role.getRoleName(), grantedBy.getFullName());

        return true;
    }

    /**
     * Remove permission from role by permission code
     */
    @Transactional
    public boolean removePermissionFromRole(UUID roleId, String permissionCode) {
        Optional<Role> roleOpt = roleRepository.findById(roleId);
        Optional<Permission> permissionOpt = permissionRepository.findByPermissionCode(permissionCode);

        if (roleOpt.isEmpty() || permissionOpt.isEmpty()) {
            return false;
        }

        Role role = roleOpt.get();
        Permission permission = permissionOpt.get();

        Optional<RolePermission> rolePermissionOpt = rolePermissionRepository.findByRoleAndPermission(role, permission);
        if (rolePermissionOpt.isPresent()) {
            rolePermissionRepository.delete(rolePermissionOpt.get());
            log.info("Permission '{}' removed from role '{}'", permissionCode, role.getRoleName());
            return true;
        }

        return false;
    }

    /**
     * Assign multiple permissions to role
     */
    @Transactional
    public int assignPermissionsToRole(UUID roleId, Set<String> permissionCodes, User grantedBy) {
        int assignedCount = 0;
        for (String permissionCode : permissionCodes) {
            if (assignPermissionToRole(roleId, permissionCode, grantedBy)) {
                assignedCount++;
            }
        }
        return assignedCount;
    }

    /**
     * Get all permission codes for a role
     */
    public Set<String> getRolePermissionCodes(UUID roleId) {
        Optional<Role> roleOpt = roleRepository.findById(roleId);
        if (roleOpt.isEmpty()) {
            return Collections.emptySet();
        }

        return roleOpt.get().getRolePermissions().stream()
                .map(rp -> rp.getPermission().getPermissionCode())
                .collect(Collectors.toSet());
    }

    /**
     * Get all permissions for a role
     */
    public Set<Permission> getRolePermissions(UUID roleId) {
        Optional<Role> roleOpt = roleRepository.findById(roleId);
        if (roleOpt.isEmpty()) {
            return Collections.emptySet();
        }

        return roleOpt.get().getRolePermissions().stream()
                .map(RolePermission::getPermission)
                .collect(Collectors.toSet());
    }

    /**
     * Get permission by code
     */
    public Optional<Permission> getPermissionByCode(String permissionCode) {
        return permissionRepository.findByPermissionCode(permissionCode);
    }

    /**
     * Get multiple permissions by codes
     */
    public List<Permission> getPermissionsByCodes(List<String> permissionCodes) {
        return permissionRepository.findByPermissionCodes(permissionCodes);
    }

    /**
     * Validate permissions exist in database
     */
    public boolean validatePermissionsExist(Set<String> permissionCodes) {
        if (permissionCodes == null || permissionCodes.isEmpty()) {
            return true;
        }

        List<String> foundCodes = permissionRepository.findByPermissionCodes(
                        new ArrayList<>(permissionCodes)
                ).stream()
                .map(Permission::getPermissionCode)
                .toList();

        return foundCodes.containsAll(permissionCodes);
    }

    /**
     * Get all available permissions
     */
    public List<Permission> getAllPermissions() {
        return permissionRepository.findActivePermissions();
    }

    /**
     * Get permissions by module with count
     */
    public Map<String, Long> getPermissionCountByModule() {
        List<String> modules = permissionRepository.findAllModules();
        return modules.stream()
                .collect(Collectors.toMap(
                        module -> module,
                        module -> permissionRepository.countByModule(module)
                ));
    }

    /**
     * Search permissions by keyword
     */
    public List<Permission> searchPermissions(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllPermissions();
        }
        return permissionRepository.searchByKeyword(keyword.trim());
    }
}
