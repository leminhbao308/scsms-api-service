package com.kltn.scsms_api_service.core.utils;

import com.kltn.scsms_api_service.core.dto.token.LoginUserInfo;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;


@UtilityClass
@Slf4j
public class PermissionUtils {
    
    public static LoginUserInfo getCurrentUser() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return null;
            }
            
            Object principal = authentication.getPrincipal();
            if (principal instanceof LoginUserInfo loginUserInfo) {
                return loginUserInfo;
            }
            
            return null;
        } catch (Exception e) {
            log.error("Error getting current user: {}", e.getMessage());
            return null;
        }
    }
}
