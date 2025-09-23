package com.kltn.scsms_api_service.core.utils;

import com.kltn.scsms_api_service.core.enums.PermissionModule;
import com.kltn.scsms_api_service.core.enums.SystemPermission;
import lombok.experimental.UtilityClass;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@UtilityClass
public class PermissionUtils {
    
    /**
     * Get all permissions for a module
     */
    public static Set<SystemPermission> getModulePermissions(PermissionModule module) {
        return Arrays.stream(SystemPermission.values())
            .filter(permission -> permission.getModule() == module)
            .collect(Collectors.toSet());
    }
    
    /**
     * Get basic CRUD permissions for a module
     */
    public static Set<SystemPermission> getBasicCrudPermissions(PermissionModule module) {
        return Arrays.stream(SystemPermission.values())
            .filter(permission -> permission.getModule() == module)
            .filter(permission -> {
                String code = permission.getPermissionCode();
                return code.endsWith("_CREATE") ||
                    code.endsWith("_READ") ||
                    code.endsWith("_UPDATE") ||
                    code.endsWith("_DELETE");
            })
            .collect(Collectors.toSet());
    }
    
    /**
     * Check if permission belongs to specific module
     */
    public static boolean belongsToModule(SystemPermission permission, PermissionModule module) {
        return permission.getModule() == module;
    }
    
    /**
     * Get permission codes as strings
     */
    public static Set<String> getPermissionCodes(Set<SystemPermission> permissions) {
        return permissions.stream()
            .map(SystemPermission::getPermissionCode)
            .collect(Collectors.toSet());
    }
}
