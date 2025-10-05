package com.kltn.scsms_api_service.core.controllers;

import com.kltn.scsms_api_service.annotations.RequirePermission;
import com.kltn.scsms_api_service.annotations.SwaggerOperation;
import com.kltn.scsms_api_service.constants.ApiConstant;
import com.kltn.scsms_api_service.constants.PermissionConstant;
import com.kltn.scsms_api_service.core.dto.response.ApiResponse;
import com.kltn.scsms_api_service.core.dto.response.PaginatedResponse;
import com.kltn.scsms_api_service.core.dto.servicePackageTypeManagement.ServicePackageTypeInfoDto;
import com.kltn.scsms_api_service.core.dto.servicePackageTypeManagement.param.ServicePackageTypeFilterParam;
import com.kltn.scsms_api_service.core.dto.servicePackageTypeManagement.request.CreateServicePackageTypeRequest;
import com.kltn.scsms_api_service.core.dto.servicePackageTypeManagement.request.UpdateServicePackageTypeRequest;
import com.kltn.scsms_api_service.core.service.businessService.ServicePackageTypeManagementService;
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
import java.util.Map;
import java.util.UUID;

/**
 * Controller for ServicePackageType management operations
 */
@RestController
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Service Package Type Management", description = "APIs for managing service package types")
public class ServicePackageTypeManagementController {

    private final ServicePackageTypeManagementService servicePackageTypeManagementService;

    /**
     * Get all service package types with pagination and filters
     */
    @GetMapping(ApiConstant.GET_ALL_SERVICE_PACKAGE_TYPES_API)
    @Operation(summary = "Get all service package types", description = "Retrieve a paginated list of all service package types with optional filtering")
    @SwaggerOperation(summary = "Get all service package types")
    // @RequirePermission(permissions =
    // PermissionConstant.SERVICE_PACKAGE_TYPE_READ)
    public ResponseEntity<ApiResponse<PaginatedResponse<ServicePackageTypeInfoDto>>> getAllServicePackageTypes(
            @Parameter(description = "Filter parameters") @ModelAttribute ServicePackageTypeFilterParam filterParam) {
        log.info("Getting all service package types with filter: {}", filterParam);

        Page<ServicePackageTypeInfoDto> servicePackageTypePage = servicePackageTypeManagementService
                .getAllServicePackageTypes(
                        ServicePackageTypeFilterParam.standardize(filterParam));

        return ResponseBuilder.paginated("Service package types fetched successfully", servicePackageTypePage);
    }

    /**
     * Get service package type by ID
     */
    @GetMapping(ApiConstant.GET_SERVICE_PACKAGE_TYPE_BY_ID_API)
    @Operation(summary = "Get service package type by ID", description = "Retrieve a specific service package type by its ID")
    @SwaggerOperation(summary = "Get service package type by ID")
    // @RequirePermission(permissions =
    // PermissionConstant.SERVICE_PACKAGE_TYPE_READ)
    public ResponseEntity<ApiResponse<ServicePackageTypeInfoDto>> getServicePackageTypeById(
            @Parameter(description = "Service package type ID") @PathVariable UUID servicePackageTypeId) {
        log.info("Getting service package type by ID: {}", servicePackageTypeId);

        ServicePackageTypeInfoDto servicePackageType = servicePackageTypeManagementService
                .getServicePackageTypeById(servicePackageTypeId);

        return ResponseBuilder.success("Service package type fetched successfully", servicePackageType);
    }

    /**
     * Get service package type by code
     */
    @GetMapping(ApiConstant.GET_SERVICE_PACKAGE_TYPE_BY_CODE_API)
    @Operation(summary = "Get service package type by code", description = "Retrieve a specific service package type by its code")
    @SwaggerOperation(summary = "Get service package type by code")
    // @RequirePermission(permissions =
    // PermissionConstant.SERVICE_PACKAGE_TYPE_READ)
    public ResponseEntity<ApiResponse<ServicePackageTypeInfoDto>> getServicePackageTypeByCode(
            @Parameter(description = "Service package type code") @PathVariable String code) {
        log.info("Getting service package type by code: {}", code);

        ServicePackageTypeInfoDto servicePackageType = servicePackageTypeManagementService
                .getServicePackageTypeByCode(code);

        return ResponseBuilder.success("Service package type fetched successfully", servicePackageType);
    }

    /**
     * Get active service package types
     */
    @GetMapping(ApiConstant.GET_ACTIVE_SERVICE_PACKAGE_TYPES_API)
    @Operation(summary = "Get active service package types", description = "Retrieve all active service package types")
    @SwaggerOperation(summary = "Get active service package types")
    // @RequirePermission(permissions =
    // PermissionConstant.SERVICE_PACKAGE_TYPE_READ)
    public ResponseEntity<ApiResponse<List<ServicePackageTypeInfoDto>>> getActiveServicePackageTypes() {
        log.info("Getting active service package types");

        List<ServicePackageTypeInfoDto> servicePackageTypes = servicePackageTypeManagementService
                .getActiveServicePackageTypes();

        return ResponseBuilder.success("Active service package types fetched successfully", servicePackageTypes);
    }

    /**
     * Get default service package type
     */
    @GetMapping(ApiConstant.GET_DEFAULT_SERVICE_PACKAGE_TYPE_API)
    @Operation(summary = "Get default service package type", description = "Retrieve the default service package type")
    @SwaggerOperation(summary = "Get default service package type")
    // @RequirePermission(permissions =
    // PermissionConstant.SERVICE_PACKAGE_TYPE_READ)
    public ResponseEntity<ApiResponse<ServicePackageTypeInfoDto>> getDefaultServicePackageType() {
        log.info("Getting default service package type");

        ServicePackageTypeInfoDto servicePackageType = servicePackageTypeManagementService
                .getDefaultServicePackageType();

        return ResponseBuilder.success("Default service package type fetched successfully", servicePackageType);
    }

    /**
     * Get service package types for customer type
     */
    @GetMapping(ApiConstant.GET_SERVICE_PACKAGE_TYPES_FOR_CUSTOMER_TYPE_API)
    @Operation(summary = "Get service package types for customer type", description = "Retrieve service package types applicable for a specific customer type")
    @SwaggerOperation(summary = "Get service package types for customer type")
    // @RequirePermission(permissions =
    // PermissionConstant.SERVICE_PACKAGE_TYPE_READ)
    public ResponseEntity<ApiResponse<List<ServicePackageTypeInfoDto>>> getServicePackageTypesForCustomerType(
            @Parameter(description = "Customer type") @PathVariable String customerType) {
        log.info("Getting service package types for customer type: {}", customerType);

        List<ServicePackageTypeInfoDto> servicePackageTypes = servicePackageTypeManagementService
                .getServicePackageTypesForCustomerType(customerType);

        return ResponseBuilder.success("Service package types for customer type fetched successfully",
                servicePackageTypes);
    }

    /**
     * Search service package types by keyword
     */
    @GetMapping(ApiConstant.SEARCH_SERVICE_PACKAGE_TYPES_API)
    @Operation(summary = "Search service package types", description = "Search service package types by keyword")
    @SwaggerOperation(summary = "Search service package types")
    // @RequirePermission(permissions =
    // PermissionConstant.SERVICE_PACKAGE_TYPE_READ)
    public ResponseEntity<ApiResponse<List<ServicePackageTypeInfoDto>>> searchServicePackageTypes(
            @Parameter(description = "Search keyword") @RequestParam String keyword) {
        log.info("Searching service package types by keyword: {}", keyword);

        List<ServicePackageTypeInfoDto> servicePackageTypes = servicePackageTypeManagementService
                .searchServicePackageTypes(keyword);

        return ResponseBuilder.success("Service package types search completed", servicePackageTypes);
    }

    /**
     * Create new service package type
     */
    @PostMapping(ApiConstant.CREATE_SERVICE_PACKAGE_TYPE_API)
    @Operation(summary = "Create service package type", description = "Create a new service package type")
    @SwaggerOperation(summary = "Create service package type")
    // @RequirePermission(permissions =
    // PermissionConstant.SERVICE_PACKAGE_TYPE_CREATE)
    public ResponseEntity<ApiResponse<ServicePackageTypeInfoDto>> createServicePackageType(
            @Parameter(description = "Service package type creation request") @Valid @RequestBody CreateServicePackageTypeRequest createServicePackageTypeRequest) {
        log.info("Creating service package type: {}", createServicePackageTypeRequest.getCode());

        ServicePackageTypeInfoDto createdServicePackageType = servicePackageTypeManagementService
                .createServicePackageType(createServicePackageTypeRequest);

        return ResponseBuilder.created("Service package type created successfully", createdServicePackageType);
    }

    /**
     * Update existing service package type
     */
    @PostMapping(ApiConstant.UPDATE_SERVICE_PACKAGE_TYPE_API)
    @Operation(summary = "Update service package type", description = "Update an existing service package type")
    @SwaggerOperation(summary = "Update service package type")
    // @RequirePermission(permissions =
    // PermissionConstant.SERVICE_PACKAGE_TYPE_UPDATE)
    public ResponseEntity<ApiResponse<ServicePackageTypeInfoDto>> updateServicePackageType(
            @Parameter(description = "Service package type ID") @PathVariable UUID servicePackageTypeId,
            @Parameter(description = "Service package type update request") @Valid @RequestBody UpdateServicePackageTypeRequest updateServicePackageTypeRequest) {
        log.info("Updating service package type with ID: {}", servicePackageTypeId);

        ServicePackageTypeInfoDto updatedServicePackageType = servicePackageTypeManagementService
                .updateServicePackageType(servicePackageTypeId, updateServicePackageTypeRequest);

        return ResponseBuilder.success("Service package type updated successfully", updatedServicePackageType);
    }

    /**
     * Delete service package type (soft delete)
     */
    @PostMapping(ApiConstant.DELETE_SERVICE_PACKAGE_TYPE_API)
    @Operation(summary = "Delete service package type", description = "Delete a service package type (soft delete)")
    @SwaggerOperation(summary = "Delete service package type")
    // @RequirePermission(permissions =
    // PermissionConstant.SERVICE_PACKAGE_TYPE_DELETE)
    public ResponseEntity<ApiResponse<Void>> deleteServicePackageType(
            @Parameter(description = "Service package type ID") @PathVariable UUID servicePackageTypeId) {
        log.info("Deleting service package type with ID: {}", servicePackageTypeId);

        servicePackageTypeManagementService.deleteServicePackageType(servicePackageTypeId);

        return ResponseBuilder.success("Service package type deleted successfully");
    }

    /**
     * Restore service package type (undo soft delete)
     */
    @PostMapping(ApiConstant.RESTORE_SERVICE_PACKAGE_TYPE_API)
    @Operation(summary = "Restore service package type", description = "Restore a soft-deleted service package type")
    @SwaggerOperation(summary = "Restore service package type")
    // @RequirePermission(permissions =
    // PermissionConstant.SERVICE_PACKAGE_TYPE_UPDATE)
    public ResponseEntity<ApiResponse<Void>> restoreServicePackageType(
            @Parameter(description = "Service package type ID") @PathVariable UUID servicePackageTypeId) {
        log.info("Restoring service package type with ID: {}", servicePackageTypeId);

        servicePackageTypeManagementService.restoreServicePackageType(servicePackageTypeId);

        return ResponseBuilder.success("Service package type restored successfully");
    }

    /**
     * Update service package type status (activate/deactivate)
     */
    @PostMapping(ApiConstant.UPDATE_SERVICE_PACKAGE_TYPE_STATUS_API)
    @Operation(summary = "Update service package type status", description = "Update service package type status to active or inactive")
    @SwaggerOperation(summary = "Update service package type status")
    // @RequirePermission(permissions =
    // PermissionConstant.SERVICE_PACKAGE_TYPE_ACTIVATE, permLogic =
    // com.kltn.scsms_api_service.annotations.PermissionLogic.OR)
    public ResponseEntity<ApiResponse<Void>> updateServicePackageTypeStatus(
            @Parameter(description = "Service package type ID") @PathVariable UUID servicePackageTypeId,
            @Parameter(description = "Status update request") @RequestBody Map<String, Boolean> statusRequest) {
        Boolean isActive = statusRequest.get("is_active");
        log.info("Updating service package type status for ID: {} to active: {}", servicePackageTypeId, isActive);

        servicePackageTypeManagementService.updateServicePackageTypeStatus(servicePackageTypeId, isActive);

        String statusMessage = isActive ? "activated" : "deactivated";
        return ResponseBuilder.success("Service package type " + statusMessage + " successfully");
    }

    /**
     * Activate service package type
     */
    @PostMapping(ApiConstant.ACTIVATE_SERVICE_PACKAGE_TYPE_API)
    @Operation(summary = "Activate service package type", description = "Activate a service package type")
    @SwaggerOperation(summary = "Activate service package type")
    // @RequirePermission(permissions =
    // PermissionConstant.SERVICE_PACKAGE_TYPE_ACTIVATE)
    public ResponseEntity<ApiResponse<Void>> activateServicePackageType(
            @Parameter(description = "Service package type ID") @PathVariable UUID servicePackageTypeId) {
        log.info("Activating service package type with ID: {}", servicePackageTypeId);

        servicePackageTypeManagementService.activateServicePackageType(servicePackageTypeId);

        return ResponseBuilder.success("Service package type activated successfully");
    }

    /**
     * Deactivate service package type
     */
    @PostMapping(ApiConstant.DEACTIVATE_SERVICE_PACKAGE_TYPE_API)
    @Operation(summary = "Deactivate service package type", description = "Deactivate a service package type")
    @SwaggerOperation(summary = "Deactivate service package type")
    // @RequirePermission(permissions =
    // PermissionConstant.SERVICE_PACKAGE_TYPE_DEACTIVATE)
    public ResponseEntity<ApiResponse<Void>> deactivateServicePackageType(
            @Parameter(description = "Service package type ID") @PathVariable UUID servicePackageTypeId) {
        log.info("Deactivating service package type with ID: {}", servicePackageTypeId);

        servicePackageTypeManagementService.deactivateServicePackageType(servicePackageTypeId);

        return ResponseBuilder.success("Service package type deactivated successfully");
    }

    /**
     * Set service package type as default
     */
    @PostMapping(ApiConstant.SET_SERVICE_PACKAGE_TYPE_AS_DEFAULT_API)
    @Operation(summary = "Set service package type as default", description = "Set a service package type as the default one")
    @SwaggerOperation(summary = "Set service package type as default")
    // @RequirePermission(permissions =
    // PermissionConstant.SERVICE_PACKAGE_TYPE_SET_DEFAULT)
    public ResponseEntity<ApiResponse<Void>> setServicePackageTypeAsDefault(
            @Parameter(description = "Service package type ID") @PathVariable UUID servicePackageTypeId) {
        log.info("Setting service package type as default with ID: {}", servicePackageTypeId);

        servicePackageTypeManagementService.setAsDefault(servicePackageTypeId);

        return ResponseBuilder.success("Service package type set as default successfully");
    }

    /**
     * Remove default status from service package type
     */
    @PostMapping(ApiConstant.REMOVE_SERVICE_PACKAGE_TYPE_DEFAULT_STATUS_API)
    @Operation(summary = "Remove default status", description = "Remove default status from a service package type")
    @SwaggerOperation(summary = "Remove default status")
    // @RequirePermission(permissions =
    // PermissionConstant.SERVICE_PACKAGE_TYPE_SET_DEFAULT)
    public ResponseEntity<ApiResponse<Void>> removeServicePackageTypeDefaultStatus(
            @Parameter(description = "Service package type ID") @PathVariable UUID servicePackageTypeId) {
        log.info("Removing default status from service package type with ID: {}", servicePackageTypeId);

        servicePackageTypeManagementService.removeDefaultStatus(servicePackageTypeId);

        return ResponseBuilder.success("Default status removed from service package type successfully");
    }

    /**
     * Validate service package type code
     */
    @GetMapping(ApiConstant.VALIDATE_SERVICE_PACKAGE_TYPE_CODE_API)
    @Operation(summary = "Validate service package type code", description = "Check if service package type code is available")
    @SwaggerOperation(summary = "Validate service package type code")
    // @RequirePermission(permissions =
    // PermissionConstant.SERVICE_PACKAGE_TYPE_READ)
    public ResponseEntity<ApiResponse<Boolean>> validateServicePackageTypeCode(
            @Parameter(description = "Service package type code to validate") @RequestParam String code) {
        log.info("Validating service package type code: {}", code);

        boolean isValid = servicePackageTypeManagementService.validateServicePackageTypeCode(code);

        return ResponseBuilder.success("Service package type code validation completed", isValid);
    }

    /**
     * Get service package type statistics
     */
    @GetMapping(ApiConstant.GET_SERVICE_PACKAGE_TYPE_STATISTICS_API)
    @Operation(summary = "Get service package type statistics", description = "Get statistics about service package types")
    @SwaggerOperation(summary = "Get service package type statistics")
    // @RequirePermission(permissions =
    // PermissionConstant.SERVICE_PACKAGE_TYPE_STATISTICS)
    public ResponseEntity<ApiResponse<ServicePackageTypeManagementService.ServicePackageTypeStatisticsDto>> getServicePackageTypeStatistics() {
        log.info("Getting service package type statistics");

        ServicePackageTypeManagementService.ServicePackageTypeStatisticsDto statistics = servicePackageTypeManagementService
                .getServicePackageTypeStatistics();

        return ResponseBuilder.success("Service package type statistics fetched successfully", statistics);
    }
}
