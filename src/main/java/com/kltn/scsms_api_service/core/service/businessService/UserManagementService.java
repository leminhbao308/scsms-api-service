package com.kltn.scsms_api_service.core.service.businessService;

import com.kltn.scsms_api_service.core.dto.param.UserFilterParam;
import com.kltn.scsms_api_service.core.dto.response.UserResponse;
import com.kltn.scsms_api_service.core.service.entityService.PermissionService;
import com.kltn.scsms_api_service.core.service.entityService.RoleService;
import com.kltn.scsms_api_service.core.service.entityService.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserManagementService {
    
    private final UserService userService;
    private final RoleService roleService;
    private final PermissionService permissionService;
    
    public Page<UserResponse> getAllUsers(UserFilterParam userFilterParam) {
        
        return userService.getAllUsersWithFilters(userFilterParam);
    }
}
