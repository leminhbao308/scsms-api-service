package com.kltn.scsms_api_service.core.controllers;

import com.kltn.scsms_api_service.annotations.RequireRole;
import com.kltn.scsms_api_service.annotations.SwaggerOperation;
import com.kltn.scsms_api_service.constants.ApiConstant;
import com.kltn.scsms_api_service.core.dto.response.ApiResponse;
import com.kltn.scsms_api_service.core.dto.response.PaginatedResponse;
import com.kltn.scsms_api_service.core.dto.centerManagement.CenterInfoDto;
import com.kltn.scsms_api_service.core.dto.centerManagement.param.CenterFilterParam;
import com.kltn.scsms_api_service.core.dto.centerManagement.request.CreateCenterRequest;
import com.kltn.scsms_api_service.core.dto.centerManagement.request.UpdateCenterRequest;
import com.kltn.scsms_api_service.core.service.businessService.CenterManagementService;
import com.kltn.scsms_api_service.core.utils.ResponseBuilder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping(ApiConstant.CENTER_MANAGEMENT_PREFIX)
@RequiredArgsConstructor
@Tag(name = "Center Management", description = "API endpoints for managing car care centers")
public class CenterManagementController {
    
    private final CenterManagementService centerManagementService;
    
    @GetMapping("/get-all")
    @RequireRole(roles = {"ADMIN", "MANAGER", "CUSTOMER"})
    @SwaggerOperation(summary = "Get all centers with filtering and pagination")
    @Operation(summary = "Get all centers with filtering and pagination",
               description = "Retrieve a paginated list of centers with optional filtering")
    public ResponseEntity<ApiResponse<PaginatedResponse<CenterInfoDto>>> getAllCenters(
            @Parameter(description = "Filter parameters for centers")
            @Valid CenterFilterParam centerFilterParam) {
        
        log.info("Getting all centers with filters: {}", centerFilterParam);
        
        Page<CenterInfoDto> centers = centerManagementService.getAllCenters(
            centerFilterParam.standardizeFilterRequest(centerFilterParam));
        
        PaginatedResponse<CenterInfoDto> paginatedResponse = PaginatedResponse.<CenterInfoDto>builder()
            .content(centers.getContent())
            .page(centers.getNumber())
            .size(centers.getSize())
            .totalElements(centers.getTotalElements())
            .totalPages(centers.getTotalPages())
            .first(centers.isFirst())
            .last(centers.isLast())
            .build();
        
        return ResponseBuilder.success("Centers retrieved successfully", paginatedResponse);
    }
    
    @GetMapping("/{centerId}")
    @RequireRole(roles = {"ADMIN", "MANAGER"})
    @SwaggerOperation(summary = "Get center by ID")
    @Operation(summary = "Get center by ID",
               description = "Retrieve a specific center by its ID")
    public ResponseEntity<ApiResponse<CenterInfoDto>> getCenterById(
            @Parameter(description = "Center ID")
            @PathVariable UUID centerId) {
        
        log.info("Getting center by ID: {}", centerId);
        
        CenterInfoDto center = centerManagementService.getCenterById(centerId);
        
        return ResponseBuilder.success("Center retrieved successfully", center);
    }
    
    @GetMapping("/{centerId}/branches")
    @RequireRole(roles = {"ADMIN", "MANAGER"})
    @SwaggerOperation(summary = "Get center with all its branches")
    @Operation(summary = "Get center with all its branches",
               description = "Retrieve a specific center with all its associated branches")
    public ResponseEntity<ApiResponse<CenterInfoDto>> getCenterWithBranches(
            @Parameter(description = "Center ID")
            @PathVariable UUID centerId) {
        
        log.info("Getting center with branches by ID: {}", centerId);
        
        CenterInfoDto center = centerManagementService.getCenterWithBranches(centerId);
        
        return ResponseBuilder.success("Center with branches retrieved successfully", center);
    }
    
    @PostMapping("/create")
    @RequireRole(roles = {"ADMIN"})
    @SwaggerOperation(summary = "Create new center")
    @Operation(summary = "Create new center",
               description = "Create a new car care center")
    public ResponseEntity<ApiResponse<CenterInfoDto>> createCenter(
            @Parameter(description = "Center creation request")
            @Valid @RequestBody CreateCenterRequest createCenterRequest) {
        
        log.info("Creating new center: {}", createCenterRequest.getCenterName());
        
        CenterInfoDto createdCenter = centerManagementService.createCenter(createCenterRequest);
        
        return ResponseBuilder.created("Center created successfully", createdCenter);
    }
    
    @PostMapping("/{centerId}/update")
    @RequireRole(roles = {"ADMIN", "MANAGER"})
    @SwaggerOperation(summary = "Update center")
    @Operation(summary = "Update center",
               description = "Update an existing center")
    public ResponseEntity<ApiResponse<CenterInfoDto>> updateCenter(
            @Parameter(description = "Center ID")
            @PathVariable UUID centerId,
            @Parameter(description = "Center update request")
            @Valid @RequestBody UpdateCenterRequest updateCenterRequest) {
        
        log.info("Updating center with ID: {}", centerId);
        
        CenterInfoDto updatedCenter = centerManagementService.updateCenter(centerId, updateCenterRequest);
        
        return ResponseBuilder.success("Center updated successfully", updatedCenter);
    }
    
    @PostMapping("/{centerId}/delete")
    @RequireRole(roles = {"ADMIN"})
    @SwaggerOperation(summary = "Delete center")
    @Operation(summary = "Delete center",
               description = "Delete a center (soft delete)")
    public ResponseEntity<ApiResponse<Void>> deleteCenter(
            @Parameter(description = "Center ID")
            @PathVariable UUID centerId) {
        
        log.info("Deleting center with ID: {}", centerId);
        
        centerManagementService.deleteCenter(centerId);
        
        return ResponseBuilder.success("Center deleted successfully");
    }
}
