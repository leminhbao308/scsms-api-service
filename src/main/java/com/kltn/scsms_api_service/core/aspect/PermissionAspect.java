package com.kltn.scsms_api_service.core.aspect;

import com.kltn.scsms_api_service.core.annotations.RequirePermission;
import com.kltn.scsms_api_service.core.annotations.RequirePermissions;
import com.kltn.scsms_api_service.core.entity.User;
import com.kltn.scsms_api_service.core.service.PermissionService;
import com.kltn.scsms_api_service.core.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class PermissionAspect {

    private final PermissionService permissionService;
    private final UserService userService;

    private static final String NO_AUTHENTICATED_MESSAGE = "No authenticated user found for permission check";
    private static final String USER_ATTEMPTED_METHOD_MESSAGE = "User '{}' attempted to access method '{}' without required permission(s) '{}'";
    private static final String USER_ATTEMPTED_CLASS_MESSAGE = "User '{}' attempted to access class '{}' without required permission(s) '{}'";
    private static final String PERMISSION_CHECK_PASSED_MESSAGE = "Permission check passed for user '{}' with permission(s) '{}'";
    private static final String CLASS_LEVEL_PERMISSION_CHECK_PASSED_MESSAGE = "Class-level permission check passed for user '{}' with permission(s) '{}'";

    @Before("@annotation(com.kltn.scsms_api_service.core.annotations.RequirePermission)")
    public void checkSinglePermission(JoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        RequirePermission requirePermission = method.getAnnotation(RequirePermission.class);

        validateSinglePermission(
                getCurrentUserOrThrow(),
                new String[]{requirePermission.value()},
                requirePermission.message(),
                method.getName(),
                true
        );
    }

    @Before("@annotation(com.kltn.scsms_api_service.core.annotations.RequirePermissions)")
    public void checkMultiplePermissions(JoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        RequirePermissions requirePermissions = method.getAnnotation(RequirePermissions.class);

        validateMultiplePermissions(
                getCurrentUserOrThrow(),
                requirePermissions.value(),
                requirePermissions.operator(),
                requirePermissions.message(),
                method.getName(),
                true
        );
    }

    @Before("@within(com.kltn.scsms_api_service.core.annotations.RequirePermission)")
    public void checkClassLevelSinglePermission(JoinPoint joinPoint) {
        // Skip if method has its own permission annotation
        if (hasMethodLevelPermissionAnnotation(joinPoint)) {
            return;
        }

        Class<?> targetClass = joinPoint.getTarget().getClass();
        RequirePermission requirePermission = targetClass.getAnnotation(RequirePermission.class);

        validateSinglePermission(
                getCurrentUserOrThrow(),
                new String[]{requirePermission.value()},
                requirePermission.message(),
                targetClass.getSimpleName(),
                false
        );
    }

    @Before("@within(com.kltn.scsms_api_service.core.annotations.RequirePermissions)")
    public void checkClassLevelMultiplePermissions(JoinPoint joinPoint) {
        // Skip if method has its own permission annotation
        if (hasMethodLevelPermissionAnnotation(joinPoint)) {
            return;
        }

        Class<?> targetClass = joinPoint.getTarget().getClass();
        RequirePermissions requirePermissions = targetClass.getAnnotation(RequirePermissions.class);

        validateMultiplePermissions(
                getCurrentUserOrThrow(),
                requirePermissions.value(),
                requirePermissions.operator(),
                requirePermissions.message(),
                targetClass.getSimpleName(),
                false
        );
    }

    /**
     * Validates single permission for the current user
     */
    private void validateSinglePermission(User currentUser, String[] permissionCodes,
                                          String errorMessage, String target, boolean isMethod) {
        String permissionCode = permissionCodes[0];

        if (!permissionService.hasPermission(currentUser, permissionCode)) {
            logPermissionDenied(currentUser, target, permissionCode, isMethod);
            throw new AccessDeniedException(errorMessage);
        }

        logPermissionGranted(currentUser, permissionCode, isMethod);
    }

    /**
     * Validates multiple permissions for the current user
     */
    private void validateMultiplePermissions(User currentUser, String[] permissionCodes,
                                             RequirePermissions.LogicalOperator operator,
                                             String errorMessage, String target, boolean isMethod) {
        boolean hasRequiredPermissions = (operator == RequirePermissions.LogicalOperator.AND)
                ? permissionService.hasAllPermissions(currentUser, permissionCodes)
                : permissionService.hasAnyPermission(currentUser, permissionCodes);

        if (!hasRequiredPermissions) {
            String permissions = String.join(", ", permissionCodes);
            logPermissionDenied(currentUser, target, permissions, isMethod);
            throw new AccessDeniedException(errorMessage);
        }

        logPermissionGranted(currentUser, String.join(", ", permissionCodes), isMethod);
    }

    /**
     * Checks if the method has its own permission annotation
     */
    private boolean hasMethodLevelPermissionAnnotation(JoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        return method.isAnnotationPresent(RequirePermission.class) ||
                method.isAnnotationPresent(RequirePermissions.class);
    }

    /**
     * Gets current authenticated user or throws AccessDeniedException
     */
    private User getCurrentUserOrThrow() {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            log.warn(NO_AUTHENTICATED_MESSAGE);
            throw new AccessDeniedException("Authentication required");
        }
        return currentUser;
    }

    /**
     * Logs permission denied access attempts
     */
    private void logPermissionDenied(User user, String target, String permissions, boolean isMethod) {
        if (isMethod) {
            log.warn(USER_ATTEMPTED_METHOD_MESSAGE, user.getUserId(), target, permissions);
        } else {
            log.warn(USER_ATTEMPTED_CLASS_MESSAGE, user.getUserId(), target, permissions);
        }
    }

    /**
     * Logs successful permission checks
     */
    private void logPermissionGranted(User user, String permissions, boolean isMethod) {
        if (isMethod) {
            log.debug(PERMISSION_CHECK_PASSED_MESSAGE, user.getUserId(), permissions);
        } else {
            log.debug(CLASS_LEVEL_PERMISSION_CHECK_PASSED_MESSAGE, user.getUserId(), permissions);
        }
    }

    /**
     * Gets the current authenticated user from SecurityContext
     */
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() ||
                "anonymousUser".equals(authentication.getPrincipal())) {
            return null;
        }

        String username = authentication.getName();
        return userService.findByEmail(username).orElse(null);
    }
}