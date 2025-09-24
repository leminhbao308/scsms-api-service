package com.kltn.scsms_api_service.core.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to check multiple permissions on methods or classes using permission codes from database
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequirePermissions {
    /**
     * Array of permission codes that correspond to permission_code in database
     */
    String[] value();
    LogicalOperator operator() default LogicalOperator.AND;
    String message() default "Access denied";

    enum LogicalOperator {
        AND, OR
    }
}
