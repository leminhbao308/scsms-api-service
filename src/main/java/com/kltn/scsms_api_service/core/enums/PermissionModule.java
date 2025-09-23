package com.kltn.scsms_api_service.core.enums;

import lombok.Getter;

/**
 * Enum for permission modules - corresponds to 'module' column in permissions table
 */
@Getter
public enum PermissionModule {
    USER_MANAGEMENT("USER_MANAGEMENT", "User Management", "Manage users, profiles, and user-related operations"),
    ROLE_MANAGEMENT("ROLE_MANAGEMENT", "Role Management", "Manage roles and role assignments"),
    PERMISSION_MANAGEMENT("PERMISSION_MANAGEMENT", "Permission Management", "Manage permissions and access control"),
    PRODUCT_MANAGEMENT("PRODUCT_MANAGEMENT", "Product Management", "Manage products, categories, and inventory"),
    ORDER_MANAGEMENT("ORDER_MANAGEMENT", "Order Management", "Manage orders, order processing, and fulfillment"),
    CUSTOMER_MANAGEMENT("CUSTOMER_MANAGEMENT", "Customer Management", "Manage customer information and relationships"),
    SUPPLIER_MANAGEMENT("SUPPLIER_MANAGEMENT", "Supplier Management", "Manage supplier information and contracts"),
    INVENTORY_MANAGEMENT("INVENTORY_MANAGEMENT", "Inventory Management", "Manage stock, warehouses, and inventory tracking"),
    FINANCE_MANAGEMENT("FINANCE_MANAGEMENT", "Finance Management", "Manage financial operations, payments, and accounting"),
    REPORT_MANAGEMENT("REPORT_MANAGEMENT", "Report Management", "Generate and manage various system reports"),
    SYSTEM_ADMIN("SYSTEM_ADMIN", "System Administration", "System configuration and administrative tasks"),
    CONTENT_MANAGEMENT("CONTENT_MANAGEMENT", "Content Management", "Manage website content, pages, and media"),
    NOTIFICATION_MANAGEMENT("NOTIFICATION_MANAGEMENT", "Notification Management", "Manage notifications and communication"),
    AUDIT_MANAGEMENT("AUDIT_MANAGEMENT", "Audit Management", "Manage audit logs and compliance tracking"),
    BACKUP_MANAGEMENT("BACKUP_MANAGEMENT", "Backup Management", "Manage system backups and data recovery");
    
    private final String code;
    private final String displayName;
    private final String description;
    
    PermissionModule(String code, String displayName, String description) {
        this.code = code;
        this.displayName = displayName;
        this.description = description;
    }
    
    /**
     * Get enum by code
     */
    public static PermissionModule fromCode(String code) {
        for (PermissionModule module : values()) {
            if (module.getCode().equals(code)) {
                return module;
            }
        }
        throw new IllegalArgumentException("Unknown permission module code: " + code);
    }
    
    /**
     * Check if code exists
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
