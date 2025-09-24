package com.kltn.scsms_api_service.core.controllers;

import com.kltn.scsms_api_service.core.annotations.RequireRole;
import com.kltn.scsms_api_service.core.annotations.SwaggerOperation;
import com.kltn.scsms_api_service.core.constants.ApiConstant;
import com.kltn.scsms_api_service.core.dto.response.ApiResponse;
import com.kltn.scsms_api_service.core.dto.response.RoleResponse;
import com.kltn.scsms_api_service.core.service.businessService.RoleManagementService;
import com.kltn.scsms_api_service.core.utils.ResponseBuilder;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controller handling role management operations
 * Manages role creation, assignment, and permission management
 */
@RestController
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Role Management", description = "Role management endpoints")
public class RoleManagementController {
    
    private final RoleManagementService roleManagementService;
    
    @GetMapping(ApiConstant.GET_ALL_ROLES_API)
    @SwaggerOperation(
        summary = "Get all roles",
        description = "Retrieve a list of all roles that can be filtered by name, etc.")
    @RequireRole(roles = {"ADMIN", "MANAGER"})
    public ResponseEntity<ApiResponse<List<RoleResponse>>> getAllRoles() {
        log.info("Fetching all roles");
        
        List<RoleResponse> roles = roleManagementService.getAllRoles();
        
        return ResponseBuilder.success("Roles fetched successfully", roles);
    }
}
