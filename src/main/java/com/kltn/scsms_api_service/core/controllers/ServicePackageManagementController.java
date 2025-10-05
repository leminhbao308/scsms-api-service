package com.kltn.scsms_api_service.core.controllers;

import com.kltn.scsms_api_service.annotations.SwaggerOperation;
import com.kltn.scsms_api_service.constants.ApiConstant;
import com.kltn.scsms_api_service.core.dto.servicePackageManagement.ServicePackageInfoDto;
import com.kltn.scsms_api_service.core.dto.servicePackageManagement.ServicePackageProductDto;
import com.kltn.scsms_api_service.core.dto.servicePackageManagement.ServicePackageServiceDto;
import com.kltn.scsms_api_service.core.dto.servicePackageManagement.param.ServicePackageFilterParam;
import com.kltn.scsms_api_service.core.dto.servicePackageManagement.request.CreateServicePackageRequest;
import com.kltn.scsms_api_service.core.dto.servicePackageManagement.request.CreateServicePackageProductRequest;
import com.kltn.scsms_api_service.core.dto.servicePackageManagement.request.CreateServicePackageServiceRequest;
import com.kltn.scsms_api_service.core.dto.servicePackageManagement.request.UpdateServicePackageRequest;
import com.kltn.scsms_api_service.core.dto.servicePackageManagement.request.UpdateServicePackageProductRequest;
import com.kltn.scsms_api_service.core.dto.servicePackageManagement.request.UpdateServicePackageServiceRequest;
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
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Service Package Management", description = "APIs for managing service packages")
public class ServicePackageManagementController {
    
    private final ServicePackageManagementService servicePackageManagementService;
    
    @GetMapping(ApiConstant.GET_ALL_SERVICE_PACKAGES_API)
    @Operation(summary = "Get all service packages", description = "Retrieve all service packages with optional filtering and pagination")
    @SwaggerOperation(summary = "Get all service packages")
    // @RequirePermission(permissions = {"SERVICE_PACKAGE_READ"})
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
    // @RequirePermission(permissions = {"SERVICE_PACKAGE_READ"})
    public ResponseEntity<ApiResponse<ServicePackageInfoDto>> getServicePackageById(
            @Parameter(description = "Service package ID") @PathVariable UUID packageId) {
        log.info("Getting service package by ID: {}", packageId);
        ServicePackageInfoDto servicePackage = servicePackageManagementService.getServicePackageById(packageId);
        return ResponseBuilder.success(servicePackage);
    }
    
    @GetMapping(ApiConstant.GET_SERVICE_PACKAGES_BY_CATEGORY_API)
    @Operation(summary = "Get service packages by category", description = "Retrieve all service packages in a specific category")
    @SwaggerOperation(summary = "Get service packages by category")
    // @RequirePermission(permissions = {"SERVICE_PACKAGE_READ"})
    public ResponseEntity<ApiResponse<List<ServicePackageInfoDto>>> getServicePackagesByCategory(
            @Parameter(description = "Category ID") @PathVariable UUID categoryId) {
        log.info("Getting service packages by category ID: {}", categoryId);
        List<ServicePackageInfoDto> servicePackages = servicePackageManagementService.getServicePackagesByCategory(categoryId);
        return ResponseBuilder.success(servicePackages);
    }
    
    @GetMapping(ApiConstant.GET_SERVICE_PACKAGES_BY_TYPE_API)
    @Operation(summary = "Get service packages by type", description = "Retrieve all service packages of a specific type")
    @SwaggerOperation(summary = "Get service packages by type")
    // @RequirePermission(permissions = {"SERVICE_PACKAGE_READ"})
    public ResponseEntity<ApiResponse<List<ServicePackageInfoDto>>> getServicePackagesByType(
            @Parameter(description = "Package type") @PathVariable ServicePackage.PackageType packageType) {
        log.info("Getting service packages by type: {}", packageType);
        List<ServicePackageInfoDto> servicePackages = servicePackageManagementService.getServicePackagesByType(packageType);
        return ResponseBuilder.success(servicePackages);
    }
    
    
    @GetMapping(ApiConstant.SEARCH_SERVICE_PACKAGES_API)
    @Operation(summary = "Search service packages", description = "Search service packages by keyword")
    @SwaggerOperation(summary = "Search service packages")
    // @RequirePermission(permissions = {"SERVICE_PACKAGE_READ"})
    public ResponseEntity<ApiResponse<List<ServicePackageInfoDto>>> searchServicePackages(
            @Parameter(description = "Search keyword") @RequestParam String keyword) {
        log.info("Searching service packages by keyword: {}", keyword);
        List<ServicePackageInfoDto> servicePackages = servicePackageManagementService.searchServicePackages(keyword);
        return ResponseBuilder.success(servicePackages);
    }
    
    
    @PostMapping(ApiConstant.CREATE_SERVICE_PACKAGE_API)
    @Operation(summary = "Create service package", description = "Create a new service package")
    @SwaggerOperation(summary = "Create service package")
    // @RequirePermission(permissions = {"SERVICE_PACKAGE_CREATE"})
    public ResponseEntity<ApiResponse<ServicePackageInfoDto>> createServicePackage(
            @Parameter(description = "Service package creation request") @Valid @RequestBody CreateServicePackageRequest createServicePackageRequest) {
        log.info("Creating service package: {}", createServicePackageRequest.getPackageName());
        ServicePackageInfoDto servicePackage = servicePackageManagementService.createServicePackage(createServicePackageRequest);
        return ResponseBuilder.created(servicePackage);
    }
    
    @PostMapping(ApiConstant.UPDATE_SERVICE_PACKAGE_API)
    @Operation(summary = "Update service package", description = "Update an existing service package")
    @SwaggerOperation(summary = "Update service package")
    // @RequirePermission(permissions = {"SERVICE_PACKAGE_UPDATE"})
    public ResponseEntity<ApiResponse<ServicePackageInfoDto>> updateServicePackage(
            @Parameter(description = "Service package ID") @PathVariable UUID packageId,
            @Parameter(description = "Service package update request") @Valid @RequestBody UpdateServicePackageRequest updateServicePackageRequest) {
        log.info("Updating service package with ID: {}", packageId);
        ServicePackageInfoDto servicePackage = servicePackageManagementService.updateServicePackage(packageId, updateServicePackageRequest);
        return ResponseBuilder.success(servicePackage);
    }
    
    @PostMapping(ApiConstant.DELETE_SERVICE_PACKAGE_API)
    @Operation(summary = "Delete service package", description = "Delete a service package (soft delete)")
    @SwaggerOperation(summary = "Delete service package")
    // @RequirePermission(permissions = {"SERVICE_PACKAGE_DELETE"})
    public ResponseEntity<ApiResponse<Void>> deleteServicePackage(
            @Parameter(description = "Service package ID") @PathVariable UUID packageId) {
        log.info("Deleting service package with ID: {}", packageId);
        servicePackageManagementService.deleteServicePackage(packageId);
        return ResponseBuilder.success("Service package deleted successfully");
    }
    
    @PostMapping(ApiConstant.SERVICE_PACKAGE_MANAGEMENT_PREFIX +"/{packageId}/status")
    @Operation(summary = "Update service package status", description = "Update the active status of a service package")
    @SwaggerOperation(summary = "Update service package status")
    // @RequirePermission(permissions = {"SERVICE_PACKAGE_UPDATE"})
    public ResponseEntity<ApiResponse<Void>> updateServicePackageStatus(
            @Parameter(description = "Service package ID") @PathVariable UUID packageId,
            @Parameter(description = "Status update request") @RequestBody Map<String, Boolean> statusRequest) {
        Boolean isActive = statusRequest.get("is_active");
        log.info("Updating service package status for ID: {} to active: {}", packageId, isActive);
        servicePackageManagementService.updateServicePackageStatus(packageId, isActive);
        String message = isActive ? "Service package activated successfully" : "Service package deactivated successfully";
        return ResponseBuilder.success(message);
    }
    
    @GetMapping("/packages/category/{categoryId}/count")
    @Operation(summary = "Get service package count by category", description = "Get the number of service packages in a category")
    @SwaggerOperation(summary = "Get service package count by category")
    // @RequirePermission(permissions = {"SERVICE_PACKAGE_READ"})
    public ResponseEntity<ApiResponse<Long>> getServicePackageCountByCategory(
            @Parameter(description = "Category ID") @PathVariable UUID categoryId) {
        log.info("Getting service package count by category ID: {}", categoryId);
        long count = servicePackageManagementService.getServicePackageCountByCategory(categoryId);
        return ResponseBuilder.success(count);
    }
    
    @GetMapping("/packages/type/{packageType}/count")
    @Operation(summary = "Get service package count by type", description = "Get the number of service packages of a specific type")
    @SwaggerOperation(summary = "Get service package count by type")
    // @RequirePermission(permissions = {"SERVICE_PACKAGE_READ"})
    public ResponseEntity<ApiResponse<Long>> getServicePackageCountByType(
            @Parameter(description = "Package type") @PathVariable ServicePackage.PackageType packageType) {
        log.info("Getting service package count by type: {}", packageType);
        long count = servicePackageManagementService.getServicePackageCountByType(packageType);
        return ResponseBuilder.success(count);
    }
    
    
    // ServicePackageProduct endpoints
    @GetMapping(ApiConstant.SERVICE_PACKAGE_MANAGEMENT_PREFIX +"/{packageId}/products")
    @Operation(summary = "Get service package products", description = "Retrieve all products for a service package")
    @SwaggerOperation(summary = "Get service package products")
    // @RequirePermission(permissions = {"SERVICE_PACKAGE_READ"})
    public ResponseEntity<ApiResponse<List<ServicePackageProductDto>>> getServicePackageProducts(
            @Parameter(description = "Service package ID") @PathVariable UUID packageId) {
        log.info("Getting service package products for package ID: {}", packageId);
        List<ServicePackageProductDto> products = servicePackageManagementService.getServicePackageProducts(packageId);
        return ResponseBuilder.success(products);
    }
    
    @PostMapping("/{packageId}/products/add")
    @Operation(summary = "Add product to service package", description = "Add a new product to a service package")
    @SwaggerOperation(summary = "Add product to service package")
    // @RequirePermission(permissions = {"SERVICE_PACKAGE_UPDATE"})
    public ResponseEntity<ApiResponse<ServicePackageProductDto>> addProductToServicePackage(
            @Parameter(description = "Service package ID") @PathVariable UUID packageId,
            @Parameter(description = "Service package product creation request") @Valid @RequestBody CreateServicePackageProductRequest createRequest) {
        log.info("Adding product to service package with ID: {}", packageId);
        ServicePackageProductDto product = servicePackageManagementService.addProductToServicePackage(packageId, createRequest);
        return ResponseBuilder.created(product);
    }
    
    @PostMapping("/{packageId}/products/{productId}/update")
    @Operation(summary = "Update service package product", description = "Update an existing product in a service package")
    @SwaggerOperation(summary = "Update service package product")
    // @RequirePermission(permissions = {"SERVICE_PACKAGE_UPDATE"})
    public ResponseEntity<ApiResponse<ServicePackageProductDto>> updateServicePackageProduct(
            @Parameter(description = "Service package ID") @PathVariable UUID packageId,
            @Parameter(description = "Product ID") @PathVariable UUID productId,
            @Parameter(description = "Service package product update request") @Valid @RequestBody UpdateServicePackageProductRequest updateRequest) {
        log.info("Updating service package product for package ID: {} and product ID: {}", packageId, productId);
        ServicePackageProductDto product = servicePackageManagementService.updateServicePackageProduct(packageId, productId, updateRequest);
        return ResponseBuilder.success(product);
    }
    
    @PostMapping("/{packageId}/products/{productId}/remove")
    @Operation(summary = "Remove product from service package", description = "Remove a product from a service package")
    @SwaggerOperation(summary = "Remove product from service package")
    // @RequirePermission(permissions = {"SERVICE_PACKAGE_UPDATE"})
    public ResponseEntity<ApiResponse<Void>> removeProductFromServicePackage(
            @Parameter(description = "Service package ID") @PathVariable UUID packageId,
            @Parameter(description = "Product ID") @PathVariable UUID productId) {
        log.info("Removing product from service package with package ID: {} and product ID: {}", packageId, productId);
        servicePackageManagementService.removeProductFromServicePackage(packageId, productId);
        return ResponseBuilder.success("Product removed from service package successfully");
    }
    
    // Service Package Services endpoints
    @GetMapping("/{packageId}/services")
    @Operation(summary = "Get service package services", description = "Get all services in a service package")
    @SwaggerOperation(summary = "Get service package services")
    // @RequirePermission(permissions = {"SERVICE_PACKAGE_READ"})
    public ResponseEntity<ApiResponse<List<ServicePackageServiceDto>>> getServicePackageServices(
            @Parameter(description = "Service package ID") @PathVariable UUID packageId) {
        log.info("Getting service package services for package ID: {}", packageId);
        List<ServicePackageServiceDto> services = servicePackageManagementService.getServicePackageServices(packageId);
        return ResponseBuilder.success(services);
    }
    
    @PostMapping("/{packageId}/services/add")
    @Operation(summary = "Add service to service package", description = "Add a service to a service package")
    @SwaggerOperation(summary = "Add service to service package")
    // @RequirePermission(permissions = {"SERVICE_PACKAGE_UPDATE"})
    public ResponseEntity<ApiResponse<ServicePackageServiceDto>> addServiceToServicePackage(
            @Parameter(description = "Service package ID") @PathVariable UUID packageId,
            @Parameter(description = "Service package service create request") @Valid @RequestBody CreateServicePackageServiceRequest createRequest) {
        log.info("Adding service to service package with ID: {}", packageId);
        ServicePackageServiceDto service = servicePackageManagementService.addServiceToServicePackage(packageId, createRequest);
        return ResponseBuilder.created(service);
    }
    
    @PostMapping("/{packageId}/services/{serviceId}/update")
    @Operation(summary = "Update service package service", description = "Update an existing service in a service package")
    @SwaggerOperation(summary = "Update service package service")
    // @RequirePermission(permissions = {"SERVICE_PACKAGE_UPDATE"})
    public ResponseEntity<ApiResponse<ServicePackageServiceDto>> updateServicePackageService(
            @Parameter(description = "Service package ID") @PathVariable UUID packageId,
            @Parameter(description = "Service ID") @PathVariable UUID serviceId,
            @Parameter(description = "Service package service update request") @Valid @RequestBody UpdateServicePackageServiceRequest updateRequest) {
        log.info("Updating service package service for package ID: {} and service ID: {}", packageId, serviceId);
        ServicePackageServiceDto service = servicePackageManagementService.updateServicePackageService(packageId, serviceId, updateRequest);
        return ResponseBuilder.success(service);
    }
    
    @PostMapping("/{packageId}/services/{serviceId}/remove")
    @Operation(summary = "Remove service from service package", description = "Remove a service from a service package")
    @SwaggerOperation(summary = "Remove service from service package")
    // @RequirePermission(permissions = {"SERVICE_PACKAGE_UPDATE"})
    public ResponseEntity<ApiResponse<Void>> removeServiceFromServicePackage(
            @Parameter(description = "Service package ID") @PathVariable UUID packageId,
            @Parameter(description = "Service ID") @PathVariable UUID serviceId) {
        log.info("Removing service from service package with package ID: {} and service ID: {}", packageId, serviceId);
        servicePackageManagementService.removeServiceFromServicePackage(packageId, serviceId);
        return ResponseBuilder.success("Service removed from service package successfully");
    }
}
