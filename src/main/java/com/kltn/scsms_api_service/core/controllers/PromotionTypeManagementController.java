package com.kltn.scsms_api_service.core.controllers;

import com.kltn.scsms_api_service.annotations.RequirePermission;
import com.kltn.scsms_api_service.annotations.SwaggerOperation;
import com.kltn.scsms_api_service.constants.ApiConstant;
import com.kltn.scsms_api_service.constants.PermissionConstant;
import com.kltn.scsms_api_service.core.dto.promotionTypeManagement.PromotionTypeInfoDto;
import com.kltn.scsms_api_service.core.dto.promotionTypeManagement.param.PromotionTypeFilterParam;
import com.kltn.scsms_api_service.core.dto.promotionTypeManagement.request.CreatePromotionTypeRequest;
import com.kltn.scsms_api_service.core.dto.promotionTypeManagement.request.UpdatePromotionTypeRequest;
import com.kltn.scsms_api_service.core.dto.promotionTypeManagement.request.PromotionTypeStatusUpdateRequest;
import com.kltn.scsms_api_service.core.dto.response.ApiResponse;
import com.kltn.scsms_api_service.core.dto.response.PaginatedResponse;
import com.kltn.scsms_api_service.core.service.businessService.PromotionTypeManagementService;
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

@RestController
@RequestMapping
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Promotion Type Management", description = "APIs for managing promotion types")
public class PromotionTypeManagementController {

    private final PromotionTypeManagementService promotionTypeManagementService;

    @GetMapping(ApiConstant.GET_ALL_PROMOTION_TYPES_API)
    @Operation(summary = "Get all promotion types", description = "Retrieve all promotion types with optional filtering and pagination")
    @SwaggerOperation(summary = "Get all promotion types")
//    @RequirePermission(permissions = {PermissionConstant.PROMOTION_READ})
    public ResponseEntity<ApiResponse<PaginatedResponse<PromotionTypeInfoDto>>> getAllPromotionTypes(
            @Parameter(description = "Filter parameters") @ModelAttribute PromotionTypeFilterParam filterParam) {
        log.info("Getting all promotion types with filter: {}", filterParam);
        Page<PromotionTypeInfoDto> promotionTypes = promotionTypeManagementService.getAllPromotionTypes(filterParam);
        return ResponseBuilder.paginated("Promotion types fetched successfully", promotionTypes);
    }

    @GetMapping(ApiConstant.GET_PROMOTION_TYPE_BY_ID_API)
    @Operation(summary = "Get promotion type by ID", description = "Retrieve a specific promotion type by its ID")
    @SwaggerOperation(summary = "Get promotion type by ID")
//    @RequirePermission(permissions = {PermissionConstant.PROMOTION_READ})
    public ResponseEntity<ApiResponse<PromotionTypeInfoDto>> getPromotionTypeById(
            @Parameter(description = "Promotion Type ID") @PathVariable UUID promotionTypeId) {
        log.info("Getting promotion type by ID: {}", promotionTypeId);
        PromotionTypeInfoDto promotionType = promotionTypeManagementService.getPromotionTypeById(promotionTypeId);
        return ResponseBuilder.success("Promotion type fetched successfully", promotionType);
    }

    @GetMapping(ApiConstant.GET_PROMOTION_TYPE_BY_TYPE_CODE_API)
    @Operation(summary = "Get promotion type by type code", description = "Retrieve a specific promotion type by its type code")
    @SwaggerOperation(summary = "Get promotion type by type code")
//    @RequirePermission(permissions = {PermissionConstant.PROMOTION_READ})
    public ResponseEntity<ApiResponse<PromotionTypeInfoDto>> getPromotionTypeByTypeCode(
            @Parameter(description = "Promotion Type Code") @PathVariable String typeCode) {
        log.info("Getting promotion type by type code: {}", typeCode);
        PromotionTypeInfoDto promotionType = promotionTypeManagementService.getPromotionTypeByTypeCode(typeCode);
        return ResponseBuilder.success("Promotion type fetched successfully", promotionType);
    }

    @GetMapping(ApiConstant.GET_ACTIVE_PROMOTION_TYPES_API)
    @Operation(summary = "Get active promotion types", description = "Retrieve all active promotion types")
    @SwaggerOperation(summary = "Get active promotion types")
//    @RequirePermission(permissions = {PermissionConstant.PROMOTION_READ})
    public ResponseEntity<ApiResponse<List<PromotionTypeInfoDto>>> getActivePromotionTypes() {
        log.info("Getting active promotion types");
        List<PromotionTypeInfoDto> promotionTypes = promotionTypeManagementService.getActivePromotionTypes();
        return ResponseBuilder.success("Active promotion types fetched successfully", promotionTypes);
    }

    @GetMapping(ApiConstant.SEARCH_PROMOTION_TYPES_API)
    @Operation(summary = "Search promotion types", description = "Search promotion types by keyword")
    @SwaggerOperation(summary = "Search promotion types")
//    @RequirePermission(permissions = {PermissionConstant.PROMOTION_READ})
    public ResponseEntity<ApiResponse<List<PromotionTypeInfoDto>>> searchPromotionTypes(
            @Parameter(description = "Search keyword") @RequestParam String keyword) {
        log.info("Searching promotion types by keyword: {}", keyword);
        List<PromotionTypeInfoDto> promotionTypes = promotionTypeManagementService.searchPromotionTypes(keyword);
        return ResponseBuilder.success("Promotion types searched successfully", promotionTypes);
    }

    @PostMapping(ApiConstant.CREATE_PROMOTION_TYPE_API)
    @Operation(summary = "Create promotion type", description = "Create a new promotion type")
    @SwaggerOperation(summary = "Create promotion type")
//    @RequirePermission(permissions = {PermissionConstant.PROMOTION_CREATE})
    public ResponseEntity<ApiResponse<PromotionTypeInfoDto>> createPromotionType(
            @Parameter(description = "Promotion type creation request") @Valid @RequestBody CreatePromotionTypeRequest createRequest) {
        log.info("Creating promotion type: {}", createRequest.getTypeName());
        PromotionTypeInfoDto promotionType = promotionTypeManagementService.createPromotionType(createRequest);
        return ResponseBuilder.created("Promotion type created successfully", promotionType);
    }

    @PostMapping(ApiConstant.UPDATE_PROMOTION_TYPE_API)
    @Operation(summary = "Update promotion type", description = "Update an existing promotion type")
    @SwaggerOperation(summary = "Update promotion type")
//    @RequirePermission(permissions = {PermissionConstant.PROMOTION_UPDATE})
    public ResponseEntity<ApiResponse<PromotionTypeInfoDto>> updatePromotionType(
            @Parameter(description = "Promotion Type ID") @PathVariable UUID promotionTypeId,
            @Parameter(description = "Promotion type update request") @Valid @RequestBody UpdatePromotionTypeRequest updateRequest) {
        log.info("Updating promotion type with ID: {}", promotionTypeId);
        PromotionTypeInfoDto promotionType = promotionTypeManagementService.updatePromotionType(promotionTypeId, updateRequest);
        return ResponseBuilder.success("Promotion type updated successfully", promotionType);
    }

    @PostMapping(ApiConstant.DELETE_PROMOTION_TYPE_API)
    @Operation(summary = "Delete promotion type", description = "Delete a promotion type (soft delete)")
    @SwaggerOperation(summary = "Delete promotion type")
//    @RequirePermission(permissions = {PermissionConstant.PROMOTION_DELETE})
    public ResponseEntity<ApiResponse<Void>> deletePromotionType(
            @Parameter(description = "Promotion Type ID") @PathVariable UUID promotionTypeId) {
        log.info("Deleting promotion type with ID: {}", promotionTypeId);
        promotionTypeManagementService.deletePromotionType(promotionTypeId);
        return ResponseBuilder.success("Promotion type deleted successfully");
    }

    @PostMapping(ApiConstant.ACTIVATE_PROMOTION_TYPE_API)
    @Operation(summary = "Activate promotion type", description = "Activate a promotion type")
    @SwaggerOperation(summary = "Activate promotion type")
//    @RequirePermission(permissions = {PermissionConstant.PROMOTION_ACTIVATE})
    public ResponseEntity<ApiResponse<Void>> activatePromotionType(
            @Parameter(description = "Promotion Type ID") @PathVariable UUID promotionTypeId) {
        log.info("Activating promotion type with ID: {}", promotionTypeId);
        promotionTypeManagementService.activatePromotionType(promotionTypeId);
        return ResponseBuilder.success("Promotion type activated successfully");
    }

    @PostMapping(ApiConstant.DEACTIVATE_PROMOTION_TYPE_API)
    @Operation(summary = "Deactivate promotion type", description = "Deactivate a promotion type")
    @SwaggerOperation(summary = "Deactivate promotion type")
//    @RequirePermission(permissions = {PermissionConstant.PROMOTION_DEACTIVATE})
    public ResponseEntity<ApiResponse<Void>> deactivatePromotionType(
            @Parameter(description = "Promotion Type ID") @PathVariable UUID promotionTypeId) {
        log.info("Deactivating promotion type with ID: {}", promotionTypeId);
        promotionTypeManagementService.deactivatePromotionType(promotionTypeId);
        return ResponseBuilder.success("Promotion type deactivated successfully");
    }

    @PostMapping(ApiConstant.UPDATE_PROMOTION_TYPE_STATUS_API)
    @Operation(summary = "Update promotion type status", description = "Update the active status of a promotion type")
    @SwaggerOperation(summary = "Update promotion type status")
//    @RequirePermission(permissions = {PermissionConstant.PROMOTION_UPDATE})
    public ResponseEntity<ApiResponse<PromotionTypeInfoDto>> updatePromotionTypeStatus(
            @Parameter(description = "Promotion Type ID") @PathVariable UUID promotionTypeId,
            @Valid @RequestBody PromotionTypeStatusUpdateRequest statusRequest) {
        log.info("Updating promotion type status for ID: {} to {}", promotionTypeId, statusRequest.getIsActive());

        PromotionTypeInfoDto updatedPromotionType = promotionTypeManagementService.updatePromotionTypeStatus(promotionTypeId, statusRequest);

        return ResponseBuilder.success("Promotion type status updated successfully", updatedPromotionType);
    }

    @GetMapping(ApiConstant.GET_PROMOTION_TYPE_STATISTICS_API)
    @Operation(summary = "Get promotion type statistics", description = "Get statistics about promotion types")
    @SwaggerOperation(summary = "Get promotion type statistics")
//    @RequirePermission(permissions = {PermissionConstant.PROMOTION_READ})
    public ResponseEntity<ApiResponse<PromotionTypeManagementService.PromotionTypeStatsDto>> getPromotionTypeStatistics() {
        log.info("Getting promotion type statistics");
        PromotionTypeManagementService.PromotionTypeStatsDto statistics = promotionTypeManagementService.getPromotionTypeStatistics();
        return ResponseBuilder.success("Promotion type statistics fetched successfully", statistics);
    }
}
