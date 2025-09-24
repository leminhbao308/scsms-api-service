package com.kltn.scsms_api_service.core.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to check role on methods or classes using role code from database
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireRole {
    /**
     * Required role codes, if user has any of these roles, permission check is bypassed
     */
    String[] roles();
    
    /**
     * Custom error message when permission denied
     */
    String message() default "Access denied - Insufficient roles or permissions";
}
