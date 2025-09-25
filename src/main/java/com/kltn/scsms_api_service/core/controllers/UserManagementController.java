package com.kltn.scsms_api_service.core.controllers;

import com.kltn.scsms_api_service.core.annotations.RequirePermission;
import com.kltn.scsms_api_service.core.annotations.SwaggerOperation;
import com.kltn.scsms_api_service.core.constants.ApiConstant;
import com.kltn.scsms_api_service.core.constants.PermissionConstant;
import com.kltn.scsms_api_service.core.dto.userManagement.param.UserFilterParam;
import com.kltn.scsms_api_service.core.dto.userManagement.request.CreateUserRequest;
import com.kltn.scsms_api_service.core.dto.response.ApiResponse;
import com.kltn.scsms_api_service.core.dto.response.PaginatedResponse;
import com.kltn.scsms_api_service.core.dto.userManagement.UserInfoDto;
import com.kltn.scsms_api_service.core.dto.userManagement.request.UpdateUserRequest;
import com.kltn.scsms_api_service.core.service.businessService.UserManagementService;
import com.kltn.scsms_api_service.core.utils.ResponseBuilder;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

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
    @RequirePermission(permissions = PermissionConstant.USER_READ)
    public ResponseEntity<ApiResponse<PaginatedResponse<UserInfoDto>>> getAllUsers(@ModelAttribute UserFilterParam userFilterParam) {
        log.info("Fetching all users");
        
        Page<UserInfoDto> users = userManagementService.getAllUsers(UserFilterParam.standardize(userFilterParam));
        
        return ResponseBuilder.paginated("Users fetched successfully", users);
    }
    
    @PostMapping(ApiConstant.CREATE_USER_API)
    @SwaggerOperation(
        summary = "Create a new user",
        description = "Create a new user with the provided details.")
    @RequirePermission(permissions = PermissionConstant.USER_CREATE)
    public ResponseEntity<ApiResponse<UserInfoDto>> createUser(@RequestBody CreateUserRequest createUserRequest) {
        log.info("Creating new user with email: {}", createUserRequest.getEmail());
        UserInfoDto createdUser = userManagementService.createUser(createUserRequest);
        return ResponseBuilder.success("User created successfully", createdUser);
    }
    
    @PostMapping(ApiConstant.UPDATE_USER_API)
    @SwaggerOperation(
        summary = "Update an existing user",
        description = "Update the details of an existing user. Inclue enabling/disabling the user.")
    @RequirePermission(permissions = PermissionConstant.USER_UPDATE)
    public ResponseEntity<ApiResponse<UserInfoDto>> updateUser(
        @PathVariable(value = "userId") String userId,
        @RequestBody UpdateUserRequest updateUserRequest) {
        log.info("Updating user with ID: {}", userId);
        UserInfoDto updatedUser = userManagementService.updateUser(UUID.fromString(userId), updateUserRequest);
        return ResponseBuilder.success("User updated successfully", updatedUser);
    }
    
    @PostMapping(ApiConstant.DELETE_USER_API)
    @SwaggerOperation(
        summary = "Delete a user",
        description = "Delete a user by their ID.")
    @RequirePermission(permissions = PermissionConstant.USER_DELETE)
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable(value = "userId") String userId) {
        log.info("Deleting user with ID: {}", userId);
        userManagementService.deleteUser(UUID.fromString(userId));
        return ResponseBuilder.success("User deleted successfully");
    }
}
