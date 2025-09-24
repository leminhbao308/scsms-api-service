package com.kltn.scsms_api_service.core.aspect;

import com.kltn.scsms_api_service.core.annotations.PermissionLogic;
import com.kltn.scsms_api_service.core.annotations.RequirePermission;
import com.kltn.scsms_api_service.core.annotations.RequireRole;
import com.kltn.scsms_api_service.core.dto.token.LoginUserInfo;
import com.kltn.scsms_api_service.core.service.entityService.PermissionService;
import com.kltn.scsms_api_service.core.utils.PermissionUtils;
import com.kltn.scsms_api_service.exception.ClientSideException;
import com.kltn.scsms_api_service.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class PermissionAspect {
    
    private final PermissionService permissionService;
    
    @Around("@annotation(requirePermission)")
    public Object checkPermission(ProceedingJoinPoint joinPoint, RequirePermission requirePermission) throws Throwable {
        // Get current user from security context
        LoginUserInfo currentUser = PermissionUtils.getCurrentUser();
        
        if (currentUser == null) {
            log.error("Permission check failed - No authenticated user found");
            throw new ClientSideException(ErrorCode.UNAUTHORIZED, "Authentication required");
        }
        
        String[] requiredPermissions = requirePermission.permissions();
        PermissionLogic permLogic = requirePermission.permLogic();
        
        boolean hasPermission = false;
        
        if (permLogic == PermissionLogic.AND) {
            hasPermission = permissionService.hasAllPermissions(currentUser, requiredPermissions);
        } else {
            hasPermission = permissionService.hasAnyPermission(currentUser, requiredPermissions);
        }
        
        if (!hasPermission) {
            log.error("Permission denied for user {} - Required: {} ({})",
                currentUser.getEmail(),
                String.join(", ", requiredPermissions),
                permLogic.name());
            throw new ClientSideException(ErrorCode.FORBIDDEN, requirePermission.message());
        }
        
        log.debug("Permission check passed for user {} - Permissions: {}",
            currentUser.getEmail(), String.join(", ", requiredPermissions));
        
        return joinPoint.proceed();
    }
    
    @Around("@within(requirePermission)")
    public Object checkClassPermission(ProceedingJoinPoint joinPoint, RequirePermission requirePermission) throws Throwable {
        return checkPermission(joinPoint, requirePermission);
    }
    
    @Around("@annotation(requireRole)")
    public Object checkRole(ProceedingJoinPoint joinPoint, RequireRole requireRole) throws Throwable {
        // Get current user from security context
        LoginUserInfo currentUser = PermissionUtils.getCurrentUser();
        
        if (currentUser == null) {
            log.error("Permission check failed - No authenticated user found");
            throw new ClientSideException(ErrorCode.UNAUTHORIZED, "Authentication required");
        }
        
        String[] requiredRoles = requireRole.roles();
        
        if (requiredRoles != null && requiredRoles.length > 0) {
            boolean hasRole = Arrays.stream(requiredRoles)
                .anyMatch(role -> role.equalsIgnoreCase(currentUser.getRole()));
            
            if (!hasRole) {
                log.error("Role check failed for user {} - Required roles: {}",
                    currentUser.getEmail(),
                    String.join(", ", requiredRoles));
                throw new ClientSideException(ErrorCode.FORBIDDEN, requireRole.message());
            } else {
                log.debug("Role check passed for user {} - Role: {}",
                    currentUser.getEmail(), currentUser.getRole());
            }
        }
        
        return joinPoint.proceed();
    }
    
    @Around("@within(requireRole)")
    public Object checkClassPermission(ProceedingJoinPoint joinPoint, RequireRole requireRole) throws Throwable {
        return checkRole(joinPoint, requireRole);
    }
}
