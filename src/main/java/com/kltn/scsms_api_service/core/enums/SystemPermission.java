package com.kltn.scsms_api_service.core.enums;

import lombok.Getter;

/**
 * Enum for specific system permissions - corresponds to actual permission records
 */
@Getter
public enum SystemPermission {
    // User Management Permissions
    USER_CREATE("USER_CREATE", "Create User", PermissionModule.USER_MANAGEMENT, "Can create new user accounts"),
    USER_READ("USER_READ", "View User", PermissionModule.USER_MANAGEMENT, "Can view user information"),
    USER_UPDATE("USER_UPDATE", "Update User", PermissionModule.USER_MANAGEMENT, "Can update user information"),
    USER_DELETE("USER_DELETE", "Delete User", PermissionModule.USER_MANAGEMENT, "Can delete user accounts"),
    USER_RESET_PASSWORD("USER_RESET_PASSWORD", "Reset User Password", PermissionModule.USER_MANAGEMENT, "Can reset user passwords"),
    USER_ACTIVATE("USER_ACTIVATE", "Activate User", PermissionModule.USER_MANAGEMENT, "Can activate user accounts"),
    USER_DEACTIVATE("USER_DEACTIVATE", "Deactivate User", PermissionModule.USER_MANAGEMENT, "Can deactivate user accounts"),
    
    // Role Management Permissions
    ROLE_CREATE("ROLE_CREATE", "Create Role", PermissionModule.ROLE_MANAGEMENT, "Can create new roles"),
    ROLE_READ("ROLE_READ", "View Role", PermissionModule.ROLE_MANAGEMENT, "Can view role information"),
    ROLE_UPDATE("ROLE_UPDATE", "Update Role", PermissionModule.ROLE_MANAGEMENT, "Can update role information"),
    ROLE_DELETE("ROLE_DELETE", "Delete Role", PermissionModule.ROLE_MANAGEMENT, "Can delete roles"),
    ROLE_ASSIGN("ROLE_ASSIGN", "Assign Role", PermissionModule.ROLE_MANAGEMENT, "Can assign roles to users"),
    ROLE_UNASSIGN("ROLE_UNASSIGN", "Unassign Role", PermissionModule.ROLE_MANAGEMENT, "Can remove role assignments"),
    
    // Permission Management
    PERMISSION_CREATE("PERMISSION_CREATE", "Create Permission", PermissionModule.PERMISSION_MANAGEMENT, "Can create new permissions"),
    PERMISSION_READ("PERMISSION_READ", "View Permission", PermissionModule.PERMISSION_MANAGEMENT, "Can view permissions"),
    PERMISSION_UPDATE("PERMISSION_UPDATE", "Update Permission", PermissionModule.PERMISSION_MANAGEMENT, "Can update permissions"),
    PERMISSION_DELETE("PERMISSION_DELETE", "Delete Permission", PermissionModule.PERMISSION_MANAGEMENT, "Can delete permissions"),
    PERMISSION_ASSIGN("PERMISSION_ASSIGN", "Assign Permission", PermissionModule.PERMISSION_MANAGEMENT, "Can assign permissions to roles"),
    
    // Product Management Permissions
    PRODUCT_CREATE("PRODUCT_CREATE", "Create Product", PermissionModule.PRODUCT_MANAGEMENT, "Can create new products"),
    PRODUCT_READ("PRODUCT_READ", "View Product", PermissionModule.PRODUCT_MANAGEMENT, "Can view product information"),
    PRODUCT_UPDATE("PRODUCT_UPDATE", "Update Product", PermissionModule.PRODUCT_MANAGEMENT, "Can update product information"),
    PRODUCT_DELETE("PRODUCT_DELETE", "Delete Product", PermissionModule.PRODUCT_MANAGEMENT, "Can delete products"),
    PRODUCT_IMPORT("PRODUCT_IMPORT", "Import Products", PermissionModule.PRODUCT_MANAGEMENT, "Can import products from files"),
    PRODUCT_EXPORT("PRODUCT_EXPORT", "Export Products", PermissionModule.PRODUCT_MANAGEMENT, "Can export product data"),
    
    // Order Management Permissions
    ORDER_CREATE("ORDER_CREATE", "Create Order", PermissionModule.ORDER_MANAGEMENT, "Can create new orders"),
    ORDER_READ("ORDER_READ", "View Order", PermissionModule.ORDER_MANAGEMENT, "Can view order information"),
    ORDER_UPDATE("ORDER_UPDATE", "Update Order", PermissionModule.ORDER_MANAGEMENT, "Can update order information"),
    ORDER_DELETE("ORDER_DELETE", "Delete Order", PermissionModule.ORDER_MANAGEMENT, "Can delete orders"),
    ORDER_APPROVE("ORDER_APPROVE", "Approve Order", PermissionModule.ORDER_MANAGEMENT, "Can approve orders"),
    ORDER_CANCEL("ORDER_CANCEL", "Cancel Order", PermissionModule.ORDER_MANAGEMENT, "Can cancel orders"),
    ORDER_FULFILL("ORDER_FULFILL", "Fulfill Order", PermissionModule.ORDER_MANAGEMENT, "Can mark orders as fulfilled"),
    
    // System Admin Permissions
    SYSTEM_CONFIG("SYSTEM_CONFIG", "System Configuration", PermissionModule.SYSTEM_ADMIN, "Can configure system settings"),
    SYSTEM_LOGS_VIEW("SYSTEM_LOGS_VIEW", "View System Logs", PermissionModule.SYSTEM_ADMIN, "Can view system logs"),
    SYSTEM_BACKUP("SYSTEM_BACKUP", "System Backup", PermissionModule.SYSTEM_ADMIN, "Can perform system backups"),
    SYSTEM_MAINTENANCE("SYSTEM_MAINTENANCE", "System Maintenance", PermissionModule.SYSTEM_ADMIN, "Can perform system maintenance"),
    
    // Report Management Permissions
    REPORT_SALES_VIEW("REPORT_SALES_VIEW", "View Sales Report", PermissionModule.REPORT_MANAGEMENT, "Can view sales reports"),
    REPORT_USER_VIEW("REPORT_USER_VIEW", "View User Report", PermissionModule.REPORT_MANAGEMENT, "Can view user reports"),
    REPORT_INVENTORY_VIEW("REPORT_INVENTORY_VIEW", "View Inventory Report", PermissionModule.REPORT_MANAGEMENT, "Can view inventory reports"),
    REPORT_EXPORT("REPORT_EXPORT", "Export Reports", PermissionModule.REPORT_MANAGEMENT, "Can export reports"),
    REPORT_SCHEDULE("REPORT_SCHEDULE", "Schedule Reports", PermissionModule.REPORT_MANAGEMENT, "Can schedule automatic reports");
    
    private final String permissionCode;
    private final String permissionName;
    private final PermissionModule module;
    private final String description;
    
    SystemPermission(String permissionCode, String permissionName, PermissionModule module, String description) {
        this.permissionCode = permissionCode;
        this.permissionName = permissionName;
        this.module = module;
        this.description = description;
    }
    
    /**
     * Get permission by code
     */
    public static SystemPermission fromCode(String code) {
        for (SystemPermission permission : values()) {
            if (permission.getPermissionCode().equals(code)) {
                return permission;
            }
        }
        throw new IllegalArgumentException("Unknown permission code: " + code);
    }
    
    /**
     * Get all permissions for a specific module
     */
    public static SystemPermission[] getPermissionsByModule(PermissionModule module) {
        return java.util.Arrays.stream(values())
            .filter(p -> p.getModule() == module)
            .toArray(SystemPermission[]::new);
    }
    
    /**
     * Check if permission code exists
     */
    public static boolean isValidCode(String code) {
        try {
            fromCode(code);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
