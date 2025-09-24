package com.kltn.scsms_api_service.core.controllers;

import com.kltn.scsms_api_service.core.annotations.SwaggerOperation;
import com.kltn.scsms_api_service.core.constants.ApiConstant;
import com.kltn.scsms_api_service.core.dto.param.UserFilterParam;
import com.kltn.scsms_api_service.core.dto.response.ApiResponse;
import com.kltn.scsms_api_service.core.dto.response.PaginatedResponse;
import com.kltn.scsms_api_service.core.dto.response.UserResponse;
import com.kltn.scsms_api_service.core.service.businessService.UserManagementService;
import com.kltn.scsms_api_service.core.utils.ResponseBuilder;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller handling user management operations
 * Manages user creation, role assignment, and user details retrieval
 */
@RestController
@RequiredArgsConstructor
@Slf4j
@Tag(name = "User Management", description = "User management endpoints")
public class UserManagementController {
    
    private final UserManagementService userManagementService;
    
    @GetMapping(ApiConstant.GET_ALL_USERS_API)
    @SwaggerOperation(
        summary = "Get all users",
        description = "Retrieve a paginated list of all users that can be filtered by role, active, etc.")
    public ResponseEntity<ApiResponse<PaginatedResponse<UserResponse>>> getAllUsers(@ModelAttribute UserFilterParam userFilterParam) {
        log.info("Fetching all users");
        
        Page<UserResponse> users = userManagementService.getAllUsers(UserFilterParam.standardizeFilterRequest(userFilterParam));
        
        return ResponseBuilder.paginated("Users fetched successfully", users);
    }
}
