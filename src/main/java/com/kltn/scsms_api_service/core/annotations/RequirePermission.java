package com.kltn.scsms_api_service.core.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to check permission on methods or classes using permission code from database
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequirePermission {
    /**
     * Required permission codes
     */
    String[] permissions();
    
    /**
     * Required role codes, if user has any of these roles, permission check is bypassed
     * Leave empty if not using role bypass
     */
    String[] roles() default {};
    
    /**
     * Logic operation for multiple permissions
     * AND: user must have ALL permissions
     * OR: user must have ANY permission
     */
    PermissionLogic permLogic() default PermissionLogic.AND;
    
    /**
     * Custom error message when permission denied
     */
    String message() default "Access denied - Insufficient roles or permissions";
}
