package com.kltn.scsms_api_service.core.controllers;

import com.kltn.scsms_api_service.annotations.SwaggerOperation;
import com.kltn.scsms_api_service.constants.ApiConstant;
import com.kltn.scsms_api_service.core.dto.response.ApiResponse;
import com.kltn.scsms_api_service.core.dto.response.PaginatedResponse;
import com.kltn.scsms_api_service.core.dto.branchManagement.BranchInfoDto;
import com.kltn.scsms_api_service.core.dto.branchManagement.param.BranchFilterParam;
import com.kltn.scsms_api_service.core.dto.branchManagement.request.CreateBranchRequest;
import com.kltn.scsms_api_service.core.dto.branchManagement.request.UpdateBranchRequest;
import com.kltn.scsms_api_service.core.dto.branchManagement.request.UpdateBranchStatusRequest;
import com.kltn.scsms_api_service.core.service.businessService.BranchManagementService;
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

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping
@RequiredArgsConstructor
@Tag(name = "Branch Management", description = "API endpoints for managing car care branches")
public class BranchManagementController {
    
    private final BranchManagementService branchManagementService;
    
    @GetMapping(ApiConstant.GET_ALL_BRANCHES_API)
    @SwaggerOperation(summary = "Get all branches with filtering and pagination")
    @Operation(summary = "Get all branches with filtering and pagination",
               description = "Retrieve a paginated list of branches with optional filtering")
    public ResponseEntity<ApiResponse<PaginatedResponse<BranchInfoDto>>> getAllBranches(
            @Parameter(description = "Filter parameters for branches")
            @Valid BranchFilterParam branchFilterParam) {
        
        log.info("Getting all branches with filters: {}", branchFilterParam);
        
        Page<BranchInfoDto> branches = branchManagementService.getAllBranches(
            branchFilterParam.standardizeFilterRequest(branchFilterParam));
        
        PaginatedResponse<BranchInfoDto> paginatedResponse = PaginatedResponse.<BranchInfoDto>builder()
            .content(branches.getContent())
            .page(branches.getNumber())
            .size(branches.getSize())
            .totalElements(branches.getTotalElements())
            .totalPages(branches.getTotalPages())
            .first(branches.isFirst())
            .last(branches.isLast())
            .build();
        
        return ResponseBuilder.success("Branches retrieved successfully", paginatedResponse);
    }
    
    @GetMapping(ApiConstant.GET_BRANCH_BY_ID_API)
    @SwaggerOperation(summary = "Get branch by ID")
    @Operation(summary = "Get branch by ID",
               description = "Retrieve a specific branch by its ID")
    public ResponseEntity<ApiResponse<BranchInfoDto>> getBranchById(
            @Parameter(description = "Branch ID")
            @PathVariable UUID branchId) {
        
        log.info("Getting branch by ID: {}", branchId);
        
        BranchInfoDto branch = branchManagementService.getBranchById(branchId);
        
        return ResponseBuilder.success("Branch retrieved successfully", branch);
    }
    
    @GetMapping(ApiConstant.GET_BRANCHES_BY_CENTER_API)
    @SwaggerOperation(summary = "Get branches by center ID")
    @Operation(summary = "Get branches by center ID",
               description = "Retrieve all branches belonging to a specific center")
    public ResponseEntity<ApiResponse<List<BranchInfoDto>>> getBranchesByCenterId(
            @Parameter(description = "Center ID")
            @PathVariable UUID centerId) {
        
        log.info("Getting branches by center ID: {}", centerId);
        
        List<BranchInfoDto> branches = branchManagementService.getBranchesByCenterId(centerId);
        
        return ResponseBuilder.success("Branches retrieved successfully", branches);
    }
    
    
    
    @PostMapping(ApiConstant.CREATE_BRANCH_API)
    @SwaggerOperation(summary = "Create new branch")
    @Operation(summary = "Create new branch",
               description = "Create a new branch for a car care center")
    public ResponseEntity<ApiResponse<BranchInfoDto>> createBranch(
            @Parameter(description = "Branch creation request")
            @Valid @RequestBody CreateBranchRequest createBranchRequest) {
        
        log.info("Creating new branch: {}", createBranchRequest.getBranchName());
        
        BranchInfoDto createdBranch = branchManagementService.createBranchWithWarehouse(createBranchRequest);
        
        return ResponseBuilder.created("Branch created successfully", createdBranch);
    }
    
    @PostMapping(ApiConstant.UPDATE_BRANCH_API)
    @SwaggerOperation(summary = "Update branch")
    @Operation(summary = "Update branch",
               description = "Update an existing branch")
    public ResponseEntity<ApiResponse<BranchInfoDto>> updateBranch(
            @Parameter(description = "Branch ID")
            @PathVariable UUID branchId,
            @Parameter(description = "Branch update request")
            @Valid @RequestBody UpdateBranchRequest updateBranchRequest) {
        
        log.info("Updating branch with ID: {}", branchId);
        
        BranchInfoDto updatedBranch = branchManagementService.updateBranch(branchId, updateBranchRequest);
        
        return ResponseBuilder.success("Branch updated successfully", updatedBranch);
    }
    
    @PostMapping(ApiConstant.UPDATE_BRANCH_STATUS_API)
    @SwaggerOperation(summary = "Update branch status")
    @Operation(summary = "Update branch status",
               description = "Update the active status of a branch")
    public ResponseEntity<ApiResponse<BranchInfoDto>> updateBranchStatus(
            @Parameter(description = "Branch ID")
            @PathVariable UUID branchId,
            @Parameter(description = "Branch status update request")
            @Valid @RequestBody UpdateBranchStatusRequest updateBranchStatusRequest) {
        
        log.info("Updating branch status for ID: {}", branchId);
        
        BranchInfoDto updatedBranch = branchManagementService.updateBranchActiveStatus(branchId, updateBranchStatusRequest);
        
        return ResponseBuilder.success("Branch status updated successfully", updatedBranch);
    }
    
    @PostMapping(ApiConstant.DELETE_BRANCH_API)
    @SwaggerOperation(summary = "Delete branch")
    @Operation(summary = "Delete branch",
               description = "Delete a branch (soft delete)")
    public ResponseEntity<ApiResponse<Void>> deleteBranch(
            @Parameter(description = "Branch ID")
            @PathVariable UUID branchId) {
        
        log.info("Deleting branch with ID: {}", branchId);
        
        branchManagementService.deleteBranch(branchId);
        
        return ResponseBuilder.success("Branch deleted successfully");
    }
}
