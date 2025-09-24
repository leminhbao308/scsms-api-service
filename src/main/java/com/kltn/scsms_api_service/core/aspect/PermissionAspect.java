package com.kltn.scsms_api_service.core.aspect;

import com.kltn.scsms_api_service.core.annotations.PermissionLogic;
import com.kltn.scsms_api_service.core.annotations.RequirePermission;
import com.kltn.scsms_api_service.core.dto.token.LoginUserInfo;
import com.kltn.scsms_api_service.core.exception.ClientSideException;
import com.kltn.scsms_api_service.core.exception.ErrorCode;
import com.kltn.scsms_api_service.core.service.entityService.PermissionService;
import com.kltn.scsms_api_service.core.utils.PermissionUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
        
        String[] bypassRoles = requirePermission.roles();
        
        boolean hasPermission = false;
        
        // Check for role bypass first
        if (bypassRoles != null && bypassRoles.length > 0) {
            boolean hasBypassRole = Arrays.stream(bypassRoles)
                .anyMatch(role -> role.equalsIgnoreCase(currentUser.getRole()));
            
            if (hasBypassRole) {
                log.debug("Permission check bypassed for user {} - Has bypass role: {}",
                    currentUser.getEmail(),
                    String.join(", ", bypassRoles));
                return joinPoint.proceed();
            }
        }
        
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
}
