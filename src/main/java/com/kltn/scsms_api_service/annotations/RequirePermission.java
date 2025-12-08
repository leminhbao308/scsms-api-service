package com.kltn.scsms_api_service.annotations;

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
