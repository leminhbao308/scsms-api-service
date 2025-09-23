package com.kltn.scsms_api_service.core.enums;

import lombok.Getter;

/**
 * Enum for common permission actions - can be combined with modules
 */
@Getter
public enum PermissionAction {
    CREATE("CREATE", "Create", "Permission to create new records"),
    READ("READ", "Read/View", "Permission to view/read records"),
    UPDATE("UPDATE", "Update/Edit", "Permission to update/edit existing records"),
    DELETE("DELETE", "Delete", "Permission to delete records"),
    EXPORT("EXPORT", "Export", "Permission to export data"),
    IMPORT("IMPORT", "Import", "Permission to import data"),
    APPROVE("APPROVE", "Approve", "Permission to approve requests/operations"),
    REJECT("REJECT", "Reject", "Permission to reject requests/operations"),
    ASSIGN("ASSIGN", "Assign", "Permission to assign items/roles to users"),
    UNASSIGN("UNASSIGN", "Unassign", "Permission to remove assignments"),
    EXECUTE("EXECUTE", "Execute", "Permission to execute operations/functions"),
    CONFIGURE("CONFIGURE", "Configure", "Permission to configure system settings");
    
    private final String code;
    private final String displayName;
    private final String description;
    
    PermissionAction(String code, String displayName, String description) {
        this.code = code;
        this.displayName = displayName;
        this.description = description;
    }
    
    public static PermissionAction fromCode(String code) {
        for (PermissionAction action : values()) {
            if (action.getCode().equals(code)) {
                return action;
            }
        }
        throw new IllegalArgumentException("Unknown permission action code: " + code);
    }
}
