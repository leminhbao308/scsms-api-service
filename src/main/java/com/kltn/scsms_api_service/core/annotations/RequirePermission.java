package com.kltn.scsms_api_service.core.annotations;

import com.kltn.scsms_api_service.core.enums.SystemPermission;

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
     * Permission code that corresponds to permission_code in database
     */
    String value();
    String message() default "Access denied";
}
