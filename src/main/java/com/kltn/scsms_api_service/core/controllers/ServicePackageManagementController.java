package com.kltn.scsms_api_service.core.controllers;

import com.kltn.scsms_api_service.abstracts.BaseController;
import com.kltn.scsms_api_service.annotations.RequirePermission;
import com.kltn.scsms_api_service.annotations.SwaggerOperation;
import com.kltn.scsms_api_service.core.constants.ApiConstant;
import com.kltn.scsms_api_service.core.dto.servicePackageManagement.ServicePackageInfoDto;
import com.kltn.scsms_api_service.core.dto.servicePackageManagement.ServicePackageStepInfoDto;
import com.kltn.scsms_api_service.core.dto.servicePackageManagement.param.ServicePackageFilterParam;
import com.kltn.scsms_api_service.core.dto.servicePackageManagement.request.CreateServicePackageRequest;
import com.kltn.scsms_api_service.core.dto.servicePackageManagement.request.CreateServicePackageStepRequest;
import com.kltn.scsms_api_service.core.dto.servicePackageManagement.request.UpdateServicePackageRequest;
import com.kltn.scsms_api_service.core.dto.servicePackageManagement.request.UpdateServicePackageStepRequest;
import com.kltn.scsms_api_service.core.dto.response.ApiResponse;
import com.kltn.scsms_api_service.core.entity.ServicePackage;
import com.kltn.scsms_api_service.core.service.businessService.ServicePackageManagementService;
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
@RequestMapping(ApiConstant.SERVICE_PACKAGE_MANAGEMENT_PREFIX)
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Service Package Management", description = "APIs for managing service packages")
public class ServicePackageManagementController extends BaseController {
    
    private final ServicePackageManagementService servicePackageManagementService;
    
    @GetMapping(ApiConstant.GET_ALL_SERVICE_PACKAGES_API)
    @Operation(summary = "Get all service packages", description = "Retrieve all service packages with optional filtering and pagination")
    @SwaggerOperation(summary = "Get all service packages")
    @RequirePermission(permissions = {"SERVICE_PACKAGE_READ"})
    public ResponseEntity<ApiResponse<Object>> getAllServicePackages(
            @Parameter(description = "Filter parameters") ServicePackageFilterParam filterParam) {
        log.info("Getting all service packages with filter: {}", filterParam);
        
        if (filterParam != null && filterParam.getPage() > 0) {
            // Return paginated results
            Page<ServicePackageInfoDto> servicePackagePage = servicePackageManagementService.getAllServicePackages(filterParam);
            return ResponseBuilder.success(servicePackagePage);
        } else {
            // Return all results
            List<ServicePackageInfoDto> servicePackages = servicePackageManagementService.getAllServicePackages();
            return ResponseBuilder.success(servicePackages);
        }
    }
    
    @GetMapping(ApiConstant.GET_SERVICE_PACKAGE_BY_ID_API)
    @Operation(summary = "Get service package by ID", description = "Retrieve a specific service package by its ID")
    @SwaggerOperation(summary = "Get service package by ID")
    @RequirePermission(permissions = {"SERVICE_PACKAGE_READ"})
    public ResponseEntity<ApiResponse<ServicePackageInfoDto>> getServicePackageById(
            @Parameter(description = "Service package ID") @PathVariable UUID packageId) {
        log.info("Getting service package by ID: {}", packageId);
        ServicePackageInfoDto servicePackage = servicePackageManagementService.getServicePackageById(packageId);
        return ResponseBuilder.success(servicePackage);
    }
    
    @GetMapping(ApiConstant.GET_SERVICE_PACKAGES_BY_CATEGORY_API)
    @Operation(summary = "Get service packages by category", description = "Retrieve all service packages in a specific category")
    @SwaggerOperation(summary = "Get service packages by category")
    @RequirePermission(permissions = {"SERVICE_PACKAGE_READ"})
    public ResponseEntity<ApiResponse<List<ServicePackageInfoDto>>> getServicePackagesByCategory(
            @Parameter(description = "Category ID") @PathVariable UUID categoryId) {
        log.info("Getting service packages by category ID: {}", categoryId);
        List<ServicePackageInfoDto> servicePackages = servicePackageManagementService.getServicePackagesByCategory(categoryId);
        return ResponseBuilder.success(servicePackages);
    }
    
    @GetMapping(ApiConstant.GET_SERVICE_PACKAGES_BY_TYPE_API)
    @Operation(summary = "Get service packages by type", description = "Retrieve all service packages of a specific type")
    @SwaggerOperation(summary = "Get service packages by type")
    @RequirePermission(permissions = {"SERVICE_PACKAGE_READ"})
    public ResponseEntity<ApiResponse<List<ServicePackageInfoDto>>> getServicePackagesByType(
            @Parameter(description = "Package type") @PathVariable ServicePackage.PackageType packageType) {
        log.info("Getting service packages by type: {}", packageType);
        List<ServicePackageInfoDto> servicePackages = servicePackageManagementService.getServicePackagesByType(packageType);
        return ResponseBuilder.success(servicePackages);
    }
    
    @GetMapping(ApiConstant.GET_POPULAR_SERVICE_PACKAGES_API)
    @Operation(summary = "Get popular service packages", description = "Retrieve all popular service packages")
    @SwaggerOperation(summary = "Get popular service packages")
    @RequirePermission(permissions = {"SERVICE_PACKAGE_READ"})
    public ResponseEntity<ApiResponse<List<ServicePackageInfoDto>>> getPopularServicePackages() {
        log.info("Getting popular service packages");
        List<ServicePackageInfoDto> servicePackages = servicePackageManagementService.getPopularServicePackages();
        return ResponseBuilder.success(servicePackages);
    }
    
    @GetMapping(ApiConstant.GET_RECOMMENDED_SERVICE_PACKAGES_API)
    @Operation(summary = "Get recommended service packages", description = "Retrieve all recommended service packages")
    @SwaggerOperation(summary = "Get recommended service packages")
    @RequirePermission(permissions = {"SERVICE_PACKAGE_READ"})
    public ResponseEntity<ApiResponse<List<ServicePackageInfoDto>>> getRecommendedServicePackages() {
        log.info("Getting recommended service packages");
        List<ServicePackageInfoDto> servicePackages = servicePackageManagementService.getRecommendedServicePackages();
        return ResponseBuilder.success(servicePackages);
    }
    
    @GetMapping(ApiConstant.SEARCH_SERVICE_PACKAGES_API)
    @Operation(summary = "Search service packages", description = "Search service packages by keyword")
    @SwaggerOperation(summary = "Search service packages")
    @RequirePermission(permissions = {"SERVICE_PACKAGE_READ"})
    public ResponseEntity<ApiResponse<List<ServicePackageInfoDto>>> searchServicePackages(
            @Parameter(description = "Search keyword") @RequestParam String keyword) {
        log.info("Searching service packages by keyword: {}", keyword);
        List<ServicePackageInfoDto> servicePackages = servicePackageManagementService.searchServicePackages(keyword);
        return ResponseBuilder.success(servicePackages);
    }
    
    @GetMapping("/limited-time")
    @Operation(summary = "Get limited time service packages", description = "Retrieve all limited time service packages")
    @SwaggerOperation(summary = "Get limited time service packages")
    @RequirePermission(permissions = {"SERVICE_PACKAGE_READ"})
    public ResponseEntity<ApiResponse<List<ServicePackageInfoDto>>> getLimitedTimeServicePackages() {
        log.info("Getting limited time service packages");
        List<ServicePackageInfoDto> servicePackages = servicePackageManagementService.getLimitedTimeServicePackages();
        return ResponseBuilder.success(servicePackages);
    }
    
    @GetMapping("/currently-active")
    @Operation(summary = "Get currently active service packages", description = "Retrieve all currently active service packages")
    @SwaggerOperation(summary = "Get currently active service packages")
    @RequirePermission(permissions = {"SERVICE_PACKAGE_READ"})
    public ResponseEntity<ApiResponse<List<ServicePackageInfoDto>>> getCurrentlyActiveServicePackages() {
        log.info("Getting currently active service packages");
        List<ServicePackageInfoDto> servicePackages = servicePackageManagementService.getCurrentlyActiveServicePackages();
        return ResponseBuilder.success(servicePackages);
    }
    
    @GetMapping("/expired")
    @Operation(summary = "Get expired service packages", description = "Retrieve all expired service packages")
    @SwaggerOperation(summary = "Get expired service packages")
    @RequirePermission(permissions = {"SERVICE_PACKAGE_READ"})
    public ResponseEntity<ApiResponse<List<ServicePackageInfoDto>>> getExpiredServicePackages() {
        log.info("Getting expired service packages");
        List<ServicePackageInfoDto> servicePackages = servicePackageManagementService.getExpiredServicePackages();
        return ResponseBuilder.success(servicePackages);
    }
    
    @GetMapping("/upcoming")
    @Operation(summary = "Get upcoming service packages", description = "Retrieve all upcoming service packages")
    @SwaggerOperation(summary = "Get upcoming service packages")
    @RequirePermission(permissions = {"SERVICE_PACKAGE_READ"})
    public ResponseEntity<ApiResponse<List<ServicePackageInfoDto>>> getUpcomingServicePackages() {
        log.info("Getting upcoming service packages");
        List<ServicePackageInfoDto> servicePackages = servicePackageManagementService.getUpcomingServicePackages();
        return ResponseBuilder.success(servicePackages);
    }
    
    @GetMapping("/best-savings")
    @Operation(summary = "Get service packages with best savings", description = "Retrieve service packages with the best savings")
    @SwaggerOperation(summary = "Get service packages with best savings")
    @RequirePermission(permissions = {"SERVICE_PACKAGE_READ"})
    public ResponseEntity<ApiResponse<List<ServicePackageInfoDto>>> getServicePackagesWithBestSavings() {
        log.info("Getting service packages with best savings");
        List<ServicePackageInfoDto> servicePackages = servicePackageManagementService.getServicePackagesWithBestSavings();
        return ResponseBuilder.success(servicePackages);
    }
    
    @GetMapping("/highest-discount")
    @Operation(summary = "Get service packages with highest discount", description = "Retrieve service packages with the highest discount")
    @SwaggerOperation(summary = "Get service packages with highest discount")
    @RequirePermission(permissions = {"SERVICE_PACKAGE_READ"})
    public ResponseEntity<ApiResponse<List<ServicePackageInfoDto>>> getServicePackagesWithHighestDiscount() {
        log.info("Getting service packages with highest discount");
        List<ServicePackageInfoDto> servicePackages = servicePackageManagementService.getServicePackagesWithHighestDiscount();
        return ResponseBuilder.success(servicePackages);
    }
    
    @PostMapping(ApiConstant.CREATE_SERVICE_PACKAGE_API)
    @Operation(summary = "Create service package", description = "Create a new service package")
    @SwaggerOperation(summary = "Create service package")
    @RequirePermission(permissions = {"SERVICE_PACKAGE_CREATE"})
    public ResponseEntity<ApiResponse<ServicePackageInfoDto>> createServicePackage(
            @Parameter(description = "Service package creation request") @Valid @RequestBody CreateServicePackageRequest createServicePackageRequest) {
        log.info("Creating service package: {}", createServicePackageRequest.getPackageName());
        ServicePackageInfoDto servicePackage = servicePackageManagementService.createServicePackage(createServicePackageRequest);
        return ResponseBuilder.created(servicePackage);
    }
    
    @PutMapping(ApiConstant.UPDATE_SERVICE_PACKAGE_API)
    @Operation(summary = "Update service package", description = "Update an existing service package")
    @SwaggerOperation(summary = "Update service package")
    @RequirePermission(permissions = {"SERVICE_PACKAGE_UPDATE"})
    public ResponseEntity<ApiResponse<ServicePackageInfoDto>> updateServicePackage(
            @Parameter(description = "Service package ID") @PathVariable UUID packageId,
            @Parameter(description = "Service package update request") @Valid @RequestBody UpdateServicePackageRequest updateServicePackageRequest) {
        log.info("Updating service package with ID: {}", packageId);
        ServicePackageInfoDto servicePackage = servicePackageManagementService.updateServicePackage(packageId, updateServicePackageRequest);
        return ResponseBuilder.success(servicePackage);
    }
    
    @DeleteMapping(ApiConstant.DELETE_SERVICE_PACKAGE_API)
    @Operation(summary = "Delete service package", description = "Delete a service package (soft delete)")
    @SwaggerOperation(summary = "Delete service package")
    @RequirePermission(permissions = {"SERVICE_PACKAGE_DELETE"})
    public ResponseEntity<ApiResponse<Void>> deleteServicePackage(
            @Parameter(description = "Service package ID") @PathVariable UUID packageId) {
        log.info("Deleting service package with ID: {}", packageId);
        servicePackageManagementService.deleteServicePackage(packageId);
        return ResponseBuilder.success("Service package deleted successfully");
    }
    
    @PutMapping("/{packageId}/activate")
    @Operation(summary = "Activate service package", description = "Activate a service package")
    @SwaggerOperation(summary = "Activate service package")
    @RequirePermission(permissions = {"SERVICE_PACKAGE_UPDATE"})
    public ResponseEntity<ApiResponse<Void>> activateServicePackage(
            @Parameter(description = "Service package ID") @PathVariable UUID packageId) {
        log.info("Activating service package with ID: {}", packageId);
        servicePackageManagementService.activateServicePackage(packageId);
        return ResponseBuilder.success("Service package activated successfully");
    }
    
    @PutMapping("/{packageId}/deactivate")
    @Operation(summary = "Deactivate service package", description = "Deactivate a service package")
    @SwaggerOperation(summary = "Deactivate service package")
    @RequirePermission(permissions = {"SERVICE_PACKAGE_UPDATE"})
    public ResponseEntity<ApiResponse<Void>> deactivateServicePackage(
            @Parameter(description = "Service package ID") @PathVariable UUID packageId) {
        log.info("Deactivating service package with ID: {}", packageId);
        servicePackageManagementService.deactivateServicePackage(packageId);
        return ResponseBuilder.success("Service package deactivated successfully");
    }
    
    @GetMapping("/category/{categoryId}/count")
    @Operation(summary = "Get service package count by category", description = "Get the number of service packages in a category")
    @SwaggerOperation(summary = "Get service package count by category")
    @RequirePermission(permissions = {"SERVICE_PACKAGE_READ"})
    public ResponseEntity<ApiResponse<Long>> getServicePackageCountByCategory(
            @Parameter(description = "Category ID") @PathVariable UUID categoryId) {
        log.info("Getting service package count by category ID: {}", categoryId);
        long count = servicePackageManagementService.getServicePackageCountByCategory(categoryId);
        return ResponseBuilder.success(count);
    }
    
    @GetMapping("/type/{packageType}/count")
    @Operation(summary = "Get service package count by type", description = "Get the number of service packages of a specific type")
    @SwaggerOperation(summary = "Get service package count by type")
    @RequirePermission(permissions = {"SERVICE_PACKAGE_READ"})
    public ResponseEntity<ApiResponse<Long>> getServicePackageCountByType(
            @Parameter(description = "Package type") @PathVariable ServicePackage.PackageType packageType) {
        log.info("Getting service package count by type: {}", packageType);
        long count = servicePackageManagementService.getServicePackageCountByType(packageType);
        return ResponseBuilder.success(count);
    }
    
    // ServicePackageStep endpoints
    @GetMapping(ApiConstant.GET_SERVICE_PACKAGE_STEPS_API)
    @Operation(summary = "Get service package steps", description = "Retrieve all steps for a service package")
    @SwaggerOperation(summary = "Get service package steps")
    @RequirePermission(permissions = {"SERVICE_PACKAGE_READ"})
    public ResponseEntity<ApiResponse<List<ServicePackageStepInfoDto>>> getServicePackageSteps(
            @Parameter(description = "Service package ID") @PathVariable UUID packageId) {
        log.info("Getting service package steps for package ID: {}", packageId);
        List<ServicePackageStepInfoDto> steps = servicePackageManagementService.getServicePackageSteps(packageId);
        return ResponseBuilder.success(steps);
    }
    
    @PostMapping(ApiConstant.ADD_SERVICE_PACKAGE_STEP_API)
    @Operation(summary = "Add service package step", description = "Add a new step to a service package")
    @SwaggerOperation(summary = "Add service package step")
    @RequirePermission(permissions = {"SERVICE_PACKAGE_UPDATE"})
    public ResponseEntity<ApiResponse<ServicePackageStepInfoDto>> addServicePackageStep(
            @Parameter(description = "Service package ID") @PathVariable UUID packageId,
            @Parameter(description = "Service package step creation request") @Valid @RequestBody CreateServicePackageStepRequest createRequest) {
        log.info("Adding service package step to package ID: {}", packageId);
        ServicePackageStepInfoDto step = servicePackageManagementService.addServicePackageStep(packageId, createRequest);
        return ResponseBuilder.created(step);
    }
    
    @PutMapping(ApiConstant.UPDATE_SERVICE_PACKAGE_STEP_API)
    @Operation(summary = "Update service package step", description = "Update an existing service package step")
    @SwaggerOperation(summary = "Update service package step")
    @RequirePermission(permissions = {"SERVICE_PACKAGE_UPDATE"})
    public ResponseEntity<ApiResponse<ServicePackageStepInfoDto>> updateServicePackageStep(
            @Parameter(description = "Service package ID") @PathVariable UUID packageId,
            @Parameter(description = "Service package step ID") @PathVariable UUID stepId,
            @Parameter(description = "Service package step update request") @Valid @RequestBody UpdateServicePackageStepRequest updateRequest) {
        log.info("Updating service package step with ID: {} for package ID: {}", stepId, packageId);
        ServicePackageStepInfoDto step = servicePackageManagementService.updateServicePackageStep(packageId, stepId, updateRequest);
        return ResponseBuilder.success(step);
    }
    
    @DeleteMapping(ApiConstant.DELETE_SERVICE_PACKAGE_STEP_API)
    @Operation(summary = "Delete service package step", description = "Delete a service package step")
    @SwaggerOperation(summary = "Delete service package step")
    @RequirePermission(permissions = {"SERVICE_PACKAGE_UPDATE"})
    public ResponseEntity<ApiResponse<Void>> deleteServicePackageStep(
            @Parameter(description = "Service package ID") @PathVariable UUID packageId,
            @Parameter(description = "Service package step ID") @PathVariable UUID stepId) {
        log.info("Deleting service package step with ID: {} for package ID: {}", stepId, packageId);
        servicePackageManagementService.deleteServicePackageStep(packageId, stepId);
        return ResponseBuilder.success("Service package step deleted successfully");
    }
}