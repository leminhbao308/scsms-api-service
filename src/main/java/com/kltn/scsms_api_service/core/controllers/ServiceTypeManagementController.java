package com.kltn.scsms_api_service.core.controllers;

import com.kltn.scsms_api_service.annotations.SwaggerOperation;
import com.kltn.scsms_api_service.constants.ApiConstant;
import com.kltn.scsms_api_service.core.dto.response.ApiResponse;
import com.kltn.scsms_api_service.core.dto.response.PaginatedResponse;
import com.kltn.scsms_api_service.core.dto.serviceTypeManagement.ServiceTypeInfoDto;
import com.kltn.scsms_api_service.core.dto.serviceTypeManagement.param.ServiceTypeFilterParam;
import com.kltn.scsms_api_service.core.dto.serviceTypeManagement.request.CreateServiceTypeRequest;
import com.kltn.scsms_api_service.core.dto.serviceTypeManagement.request.UpdateServiceTypeRequest;
import com.kltn.scsms_api_service.core.service.businessService.ServiceTypeManagementService;
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

@RestController
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Service Type Management", description = "APIs for managing service types")
public class ServiceTypeManagementController {

    private final ServiceTypeManagementService serviceTypeManagementService;

    /**
     * Get all service types with pagination and filters
     */
    @GetMapping(ApiConstant.GET_ALL_SERVICE_TYPES_API)
    @Operation(summary = "Get all service types", description = "Retrieve a paginated list of all service types with optional filtering")
    @SwaggerOperation(summary = "Get all service types")
    // @RequirePermission(permissions = PermissionConstant.SERVICE_TYPE_READ)
    public ResponseEntity<ApiResponse<PaginatedResponse<ServiceTypeInfoDto>>> getAllServiceTypes(
            @Parameter(description = "Filter parameters") @ModelAttribute ServiceTypeFilterParam filterParam) {
        log.info("Getting all service types with filter: {}", filterParam);

        Page<ServiceTypeInfoDto> serviceTypePage = serviceTypeManagementService.getAllServiceTypes(
                ServiceTypeFilterParam.standardize(filterParam));

        return ResponseBuilder.paginated("Service types fetched successfully", serviceTypePage);
    }

    /**
     * Get service type by ID
     */
    @GetMapping(ApiConstant.GET_SERVICE_TYPE_BY_ID_API)
    @Operation(summary = "Get service type by ID", description = "Retrieve a specific service type by its ID")
    @SwaggerOperation(summary = "Get service type by ID")
    // @RequirePermission(permissions = PermissionConstant.SERVICE_TYPE_READ)
    public ResponseEntity<ApiResponse<ServiceTypeInfoDto>> getServiceTypeById(
            @Parameter(description = "Service type ID") @PathVariable UUID serviceTypeId) {
        log.info("Getting service type by ID: {}", serviceTypeId);

        ServiceTypeInfoDto serviceType = serviceTypeManagementService.getServiceTypeById(serviceTypeId);

        return ResponseBuilder.success("Service type fetched successfully", serviceType);
    }

    /**
     * Get service type by code
     */
    @GetMapping(ApiConstant.GET_SERVICE_TYPE_BY_CODE_API)
    @Operation(summary = "Get service type by code", description = "Retrieve a specific service type by its code")
    @SwaggerOperation(summary = "Get service type by code")
    // @RequirePermission(permissions = PermissionConstant.SERVICE_TYPE_READ)
    public ResponseEntity<ApiResponse<ServiceTypeInfoDto>> getServiceTypeByCode(
            @Parameter(description = "Service type code") @PathVariable String code) {
        log.info("Getting service type by code: {}", code);

        ServiceTypeInfoDto serviceType = serviceTypeManagementService.getServiceTypeByCode(code);

        return ResponseBuilder.success("Service type fetched successfully", serviceType);
    }

    /**
     * Get active service types
     */
    @GetMapping(ApiConstant.GET_ACTIVE_SERVICE_TYPES_API)
    @Operation(summary = "Get active service types", description = "Retrieve all active service types")
    @SwaggerOperation(summary = "Get active service types")
    // @RequirePermission(permissions = PermissionConstant.SERVICE_TYPE_READ)
    public ResponseEntity<ApiResponse<List<ServiceTypeInfoDto>>> getActiveServiceTypes() {
        log.info("Getting active service types");

        List<ServiceTypeInfoDto> serviceTypes = serviceTypeManagementService.getActiveServiceTypes();

        return ResponseBuilder.success("Active service types fetched successfully", serviceTypes);
    }

    /**
     * Get service types for dropdown
     */
    @GetMapping(ApiConstant.GET_SERVICE_TYPES_DROPDOWN_API)
    @Operation(summary = "Get service types dropdown", description = "Retrieve all active service types for dropdown selection")
    @SwaggerOperation(summary = "Get service types dropdown")
    // @RequirePermission(permissions = PermissionConstant.SERVICE_TYPE_READ)
    public ResponseEntity<ApiResponse<List<ServiceTypeInfoDto>>> getServiceTypesDropdown() {
        log.info("Getting service types for dropdown");

        List<ServiceTypeInfoDto> serviceTypes = serviceTypeManagementService.getActiveServiceTypes();

        return ResponseBuilder.success("Service types for dropdown fetched successfully", serviceTypes);
    }

    /**
     * Search service types by keyword
     */
    @GetMapping(ApiConstant.SEARCH_SERVICE_TYPES_API)
    @Operation(summary = "Search service types", description = "Search service types by keyword")
    @SwaggerOperation(summary = "Search service types")
    // @RequirePermission(permissions = PermissionConstant.SERVICE_TYPE_READ)/
    public ResponseEntity<ApiResponse<List<ServiceTypeInfoDto>>> searchServiceTypes(
            @Parameter(description = "Search keyword") @RequestParam String keyword) {
        log.info("Searching service types by keyword: {}", keyword);

        List<ServiceTypeInfoDto> serviceTypes = serviceTypeManagementService.searchServiceTypes(keyword);

        return ResponseBuilder.success("Service types search completed", serviceTypes);
    }

    /**
     * Create new service type
     */
    @PostMapping(ApiConstant.CREATE_SERVICE_TYPE_API)
    @Operation(summary = "Create service type", description = "Create a new service type")
    @SwaggerOperation(summary = "Create service type")
    // @RequirePermission(permissions = PermissionConstant.SERVICE_TYPE_CREATE)
    public ResponseEntity<ApiResponse<ServiceTypeInfoDto>> createServiceType(
            @Parameter(description = "Service type creation request") @Valid @RequestBody CreateServiceTypeRequest createServiceTypeRequest) {
        log.info("Creating service type: {}", createServiceTypeRequest.getCode());

        ServiceTypeInfoDto createdServiceType = serviceTypeManagementService
                .createServiceType(createServiceTypeRequest);

        return ResponseBuilder.created("Service type created successfully", createdServiceType);
    }

    /**
     * Update existing service type
     */
    @PostMapping(ApiConstant.UPDATE_SERVICE_TYPE_API)
    @Operation(summary = "Update service type", description = "Update an existing service type")
    @SwaggerOperation(summary = "Update service type")
    // @RequirePermission(permissions = PermissionConstant.SERVICE_TYPE_UPDATE)
    public ResponseEntity<ApiResponse<ServiceTypeInfoDto>> updateServiceType(
            @Parameter(description = "Service type ID") @PathVariable UUID serviceTypeId,
            @Parameter(description = "Service type update request") @Valid @RequestBody UpdateServiceTypeRequest updateServiceTypeRequest) {
        log.info("Updating service type with ID: {}", serviceTypeId);

        ServiceTypeInfoDto updatedServiceType = serviceTypeManagementService.updateServiceType(serviceTypeId,
                updateServiceTypeRequest);

        return ResponseBuilder.success("Service type updated successfully", updatedServiceType);
    }

    /**
     * Delete service type (soft delete)
     */
    @PostMapping(ApiConstant.DELETE_SERVICE_TYPE_API)
    @Operation(summary = "Delete service type", description = "Delete a service type (soft delete)")
    @SwaggerOperation(summary = "Delete service type")
    // @RequirePermission(permissions = PermissionConstant.SERVICE_TYPE_DELETE)
    public ResponseEntity<ApiResponse<Void>> deleteServiceType(
            @Parameter(description = "Service type ID") @PathVariable UUID serviceTypeId) {
        log.info("Deleting service type with ID: {}", serviceTypeId);

        serviceTypeManagementService.deleteServiceType(serviceTypeId);

        return ResponseBuilder.success("Service type deleted successfully");
    }

    /**
     * Restore service type (undo soft delete)
     */
    @PostMapping(ApiConstant.RESTORE_SERVICE_TYPE_API)
    @Operation(summary = "Restore service type", description = "Restore a soft-deleted service type")
    @SwaggerOperation(summary = "Restore service type")
    // @RequirePermission(permissions = PermissionConstant.SERVICE_TYPE_UPDATE)
    public ResponseEntity<ApiResponse<Void>> restoreServiceType(
            @Parameter(description = "Service type ID") @PathVariable UUID serviceTypeId) {
        log.info("Restoring service type with ID: {}", serviceTypeId);

        serviceTypeManagementService.restoreServiceType(serviceTypeId);

        return ResponseBuilder.success("Service type restored successfully");
    }

    /**
     * Update service type status (activate/deactivate)
     */
    @PostMapping(ApiConstant.UPDATE_SERVICE_TYPE_STATUS_API)
    @Operation(summary = "Update service type status", description = "Update service type status to active or inactive")
    @SwaggerOperation(summary = "Update service type status")
    // @RequirePermission(permissions = PermissionConstant.SERVICE_TYPE_ACTIVATE,
    // permLogic = com.kltn.scsms_api_service.annotations.PermissionLogic.OR)
    public ResponseEntity<ApiResponse<Void>> updateServiceTypeStatus(
            @Parameter(description = "Service type ID") @PathVariable UUID serviceTypeId,
            @Parameter(description = "Status update request") @RequestBody Map<String, Boolean> statusRequest) {
        Boolean isActive = statusRequest.get("is_active");
        log.info("Updating service type status for ID: {} to active: {}", serviceTypeId, isActive);

        serviceTypeManagementService.updateServiceTypeStatus(serviceTypeId, isActive);

        String statusMessage = isActive ? "activated" : "deactivated";
        return ResponseBuilder.success("Service type " + statusMessage + " successfully");
    }

    /**
     * Activate service type
     */
    @PostMapping(ApiConstant.ACTIVATE_SERVICE_TYPE_API)
    @Operation(summary = "Activate service type", description = "Activate a service type")
    @SwaggerOperation(summary = "Activate service type")
    // @RequirePermission(permissions = PermissionConstant.SERVICE_TYPE_ACTIVATE)
    public ResponseEntity<ApiResponse<Void>> activateServiceType(
            @Parameter(description = "Service type ID") @PathVariable UUID serviceTypeId) {
        log.info("Activating service type with ID: {}", serviceTypeId);

        serviceTypeManagementService.activateServiceType(serviceTypeId);

        return ResponseBuilder.success("Service type activated successfully");
    }

    /**
     * Deactivate service type
     */
    @PostMapping(ApiConstant.DEACTIVATE_SERVICE_TYPE_API)
    @Operation(summary = "Deactivate service type", description = "Deactivate a service type")
    @SwaggerOperation(summary = "Deactivate service type")
    // @RequirePermission(permissions = PermissionConstant.SERVICE_TYPE_DEACTIVATE)
    public ResponseEntity<ApiResponse<Void>> deactivateServiceType(
            @Parameter(description = "Service type ID") @PathVariable UUID serviceTypeId) {
        log.info("Deactivating service type with ID: {}", serviceTypeId);

        serviceTypeManagementService.deactivateServiceType(serviceTypeId);

        return ResponseBuilder.success("Service type deactivated successfully");
    }

    /**
     * Validate service type code
     */
    @GetMapping(ApiConstant.VALIDATE_SERVICE_TYPE_CODE_API)
    @Operation(summary = "Validate service type code", description = "Check if service type code is available")
    @SwaggerOperation(summary = "Validate service type code")
    // @RequirePermission(permissions = PermissionConstant.SERVICE_TYPE_READ)
    public ResponseEntity<ApiResponse<Boolean>> validateServiceTypeCode(
            @Parameter(description = "Service type code to validate") @RequestParam String code) {
        log.info("Validating service type code: {}", code);

        boolean isValid = serviceTypeManagementService.validateServiceTypeCode(code);

        return ResponseBuilder.success("Service type code validation completed", isValid);
    }

    /**
     * Get service type statistics
     */
    @GetMapping(ApiConstant.GET_SERVICE_TYPE_STATISTICS_API)
    @Operation(summary = "Get service type statistics", description = "Get statistics about service types")
    @SwaggerOperation(summary = "Get service type statistics")
    // @RequirePermission(permissions = PermissionConstant.SERVICE_TYPE_STATISTICS)
    public ResponseEntity<ApiResponse<ServiceTypeManagementService.ServiceTypeStatisticsDto>> getServiceTypeStatistics() {
        log.info("Getting service type statistics");

        ServiceTypeManagementService.ServiceTypeStatisticsDto statistics = serviceTypeManagementService
                .getServiceTypeStatistics();

        return ResponseBuilder.success("Service type statistics fetched successfully", statistics);
    }
}
