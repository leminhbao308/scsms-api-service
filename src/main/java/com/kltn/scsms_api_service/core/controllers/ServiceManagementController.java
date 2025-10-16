package com.kltn.scsms_api_service.core.controllers;

import com.kltn.scsms_api_service.annotations.SwaggerOperation;
import com.kltn.scsms_api_service.constants.ApiConstant;
import com.kltn.scsms_api_service.core.dto.serviceManagement.ServiceInfoDto;
import com.kltn.scsms_api_service.core.dto.serviceManagement.param.ServiceFilterParam;
import com.kltn.scsms_api_service.core.dto.serviceManagement.request.CreateServiceRequest;
import com.kltn.scsms_api_service.core.dto.serviceManagement.request.UpdateServiceRequest;
import com.kltn.scsms_api_service.core.dto.response.ApiResponse;
import com.kltn.scsms_api_service.core.entity.Service;
import com.kltn.scsms_api_service.core.service.businessService.ServiceManagementService;
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
@Tag(name = "Service Management", description = "APIs for managing services")
public class ServiceManagementController {
    
    private final ServiceManagementService serviceManagementService;
    
    @GetMapping(ApiConstant.GET_ALL_SERVICES_API)
    @Operation(summary = "Get all services", description = "Retrieve all services with optional filtering and pagination")
    @SwaggerOperation(summary = "Get all services")
    // @RequirePermission(permissions = {"SERVICE_READ"})
    public ResponseEntity<ApiResponse<Object>> getAllServices(
            @Parameter(description = "Filter parameters") ServiceFilterParam filterParam) {
        log.info("Getting all services with filter: {}", filterParam);
        
        // Check if pagination is requested
        if (filterParam != null && filterParam.getPage() >= 0 && filterParam.getSize() > 0) {
            // Return paginated results
            Page<ServiceInfoDto> servicePage = serviceManagementService.getAllServices(filterParam);
            return ResponseBuilder.success(servicePage);
        } else {
            // Return all results
            List<ServiceInfoDto> services = serviceManagementService.getAllServices();
            return ResponseBuilder.success(services);
        }
    }
    
    @GetMapping(ApiConstant.GET_SERVICE_BY_ID_API)
    @Operation(summary = "Get service by ID", description = "Retrieve a specific service by its ID")
    @SwaggerOperation(summary = "Get service by ID")
    // @RequirePermission(permissions = {"SERVICE_READ"})
    public ResponseEntity<ApiResponse<ServiceInfoDto>> getServiceById(
            @Parameter(description = "Service ID") @PathVariable UUID serviceId) {
        log.info("Getting service by ID: {}", serviceId);
        ServiceInfoDto service = serviceManagementService.getServiceById(serviceId);
        return ResponseBuilder.success(service);
    }
    
    @GetMapping("/url/{serviceUrl}")
    @Operation(summary = "Get service by URL", description = "Retrieve a specific service by its URL")
    @SwaggerOperation(summary = "Get service by URL")
    // @RequirePermission(permissions = {"SERVICE_READ"})
    public ResponseEntity<ApiResponse<ServiceInfoDto>> getServiceByUrl(
            @Parameter(description = "Service URL") @PathVariable String serviceUrl) {
        log.info("Getting service by URL: {}", serviceUrl);
        ServiceInfoDto service = serviceManagementService.getServiceByUrl(serviceUrl);
        return ResponseBuilder.success(service);
    }
    
    @GetMapping(ApiConstant.GET_SERVICES_BY_CATEGORY_API)
    @Operation(summary = "Get services by category", description = "Retrieve all services in a specific category")
    @SwaggerOperation(summary = "Get services by category")
    // @RequirePermission(permissions = {"SERVICE_READ"})
    public ResponseEntity<ApiResponse<List<ServiceInfoDto>>> getServicesByCategory(
            @Parameter(description = "Category ID") @PathVariable UUID categoryId) {
        log.info("Getting services by category ID: {}", categoryId);
        List<ServiceInfoDto> services = serviceManagementService.getServicesByCategory(categoryId);
        return ResponseBuilder.success(services);
    }
    
    @GetMapping(ApiConstant.GET_SERVICES_BY_TYPE_API)
    @Operation(summary = "Get services by type", description = "Retrieve all services of a specific type")
    @SwaggerOperation(summary = "Get services by type")
    // @RequirePermission(permissions = {"SERVICE_READ"})
    public ResponseEntity<ApiResponse<List<ServiceInfoDto>>> getServicesByTypeId(
            @Parameter(description = "Service type ID") @PathVariable UUID serviceTypeId) {
        log.info("Getting services by type ID: {}", serviceTypeId);
        List<ServiceInfoDto> services = serviceManagementService.getServicesByTypeId(serviceTypeId);
        return ResponseBuilder.success(services);
    }
    
    @GetMapping(ApiConstant.GET_SERVICES_BY_SKILL_LEVEL_API)
    @Operation(summary = "Get services by skill level", description = "Retrieve all services requiring a specific skill level")
    @SwaggerOperation(summary = "Get services by skill level")
    // @RequirePermission(permissions = {"SERVICE_READ"})
    public ResponseEntity<ApiResponse<List<ServiceInfoDto>>> getServicesBySkillLevel(
            @Parameter(description = "Skill level") @PathVariable Service.SkillLevel skillLevel) {
        log.info("Getting services by skill level: {}", skillLevel);
        List<ServiceInfoDto> services = serviceManagementService.getServicesBySkillLevel(skillLevel);
        return ResponseBuilder.success(services);
    }
    
    @GetMapping(ApiConstant.SEARCH_SERVICES_API)
    @Operation(summary = "Search services", description = "Search services by keyword")
    @SwaggerOperation(summary = "Search services")
    // @RequirePermission(permissions = {"SERVICE_READ"})
    public ResponseEntity<ApiResponse<List<ServiceInfoDto>>> searchServices(
            @Parameter(description = "Search keyword") @RequestParam String keyword) {
        log.info("Searching services by keyword: {}", keyword);
        List<ServiceInfoDto> services = serviceManagementService.searchServices(keyword);
        return ResponseBuilder.success(services);
    }
    
    @GetMapping("/featured")
    @Operation(summary = "Get featured services", description = "Retrieve all featured services")
    @SwaggerOperation(summary = "Get featured services")
    // @RequirePermission(permissions = {"SERVICE_READ"})
    public ResponseEntity<ApiResponse<List<ServiceInfoDto>>> getFeaturedServices() {
        log.info("Getting featured services");
        List<ServiceInfoDto> services = serviceManagementService.getFeaturedServices();
        return ResponseBuilder.success(services);
    }
    
    
    
    @PostMapping(ApiConstant.CREATE_SERVICE_API)
    @Operation(summary = "Create service", description = "Create a new service")
    @SwaggerOperation(summary = "Create service")
    // @RequirePermission(permissions = {"SERVICE_CREATE"})
    public ResponseEntity<ApiResponse<ServiceInfoDto>> createService(
            @Parameter(description = "Service creation request") @Valid @RequestBody CreateServiceRequest createServiceRequest) {
        log.info("Creating service: {}", createServiceRequest.getServiceName());
        ServiceInfoDto service = serviceManagementService.createService(createServiceRequest);
        return ResponseBuilder.created(service);
    }
    
    @PostMapping(ApiConstant.UPDATE_SERVICE_API)
    @Operation(summary = "Update service", description = "Update an existing service")
    @SwaggerOperation(summary = "Update service")
    // @RequirePermission(permissions = {"SERVICE_UPDATE"})
    public ResponseEntity<ApiResponse<ServiceInfoDto>> updateService(
            @Parameter(description = "Service ID") @PathVariable UUID serviceId,
            @Parameter(description = "Service update request") @Valid @RequestBody UpdateServiceRequest updateServiceRequest) {
        log.info("Updating service with ID: {}", serviceId);
        ServiceInfoDto service = serviceManagementService.updateService(serviceId, updateServiceRequest);
        return ResponseBuilder.success(service);
    }
    
    @PostMapping(ApiConstant.DELETE_SERVICE_API)
    @Operation(summary = "Delete service", description = "Delete a service (soft delete)")
    @SwaggerOperation(summary = "Delete service")
    // @RequirePermission(permissions = {"SERVICE_DELETE"})
    public ResponseEntity<ApiResponse<Void>> deleteService(
            @Parameter(description = "Service ID") @PathVariable UUID serviceId) {
        log.info("Deleting service with ID: {}", serviceId);
        serviceManagementService.deleteService(serviceId);
        return ResponseBuilder.success("Service deleted successfully");
    }
    
    @PostMapping(ApiConstant.SERVICE_MANAGEMENT_PREFIX + "/{serviceId}/status")
    @Operation(summary = "Update service status", description = "Update the active status of a service")
    @SwaggerOperation(summary = "Update service status")
    // @RequirePermission(permissions = {"SERVICE_UPDATE"}) // Temporarily disabled for testing
    public ResponseEntity<ApiResponse<Void>> updateServiceStatus(
            @Parameter(description = "Service ID") @PathVariable UUID serviceId,
            @Parameter(description = "Status update request") @RequestBody Map<String, Boolean> statusRequest) {
        Boolean isActive = statusRequest.get("is_active");
        log.info("Updating service status for ID: {} to active: {}", serviceId, isActive);
        serviceManagementService.updateServiceStatus(serviceId, isActive);
        String message = isActive ? "Service activated successfully" : "Service deactivated successfully";
        return ResponseBuilder.success(message);
    }
    
    @GetMapping("/category/{categoryId}/count")
    @Operation(summary = "Get service count by category", description = "Get the number of services in a category")
    @SwaggerOperation(summary = "Get service count by category")
    // @RequirePermission(permissions = {"SERVICE_READ"})
    public ResponseEntity<ApiResponse<Long>> getServiceCountByCategory(
            @Parameter(description = "Category ID") @PathVariable UUID categoryId) {
        log.info("Getting service count by category ID: {}", categoryId);
        long count = serviceManagementService.getServiceCountByCategory(categoryId);
        return ResponseBuilder.success(count);
    }
    
    @GetMapping("/type/{serviceType}/count")
    @Operation(summary = "Get service count by type", description = "Get the number of services of a specific type")
    @SwaggerOperation(summary = "Get service count by type")
    // @RequirePermission(permissions = {"SERVICE_READ"})
    public ResponseEntity<ApiResponse<Long>> getServiceCountByTypeId(
            @Parameter(description = "Service type ID") @PathVariable UUID serviceTypeId) {
        log.info("Getting service count by type ID: {}", serviceTypeId);
        long count = serviceManagementService.getServiceCountByTypeId(serviceTypeId);
        return ResponseBuilder.success(count);
    }
    
    @GetMapping("/skill-level/{skillLevel}/count")
    @Operation(summary = "Get service count by skill level", description = "Get the number of services requiring a specific skill level")
    @SwaggerOperation(summary = "Get service count by skill level")
    // @RequirePermission(permissions = {"SERVICE_READ"})
    public ResponseEntity<ApiResponse<Long>> getServiceCountBySkillLevel(
            @Parameter(description = "Skill level") @PathVariable Service.SkillLevel skillLevel) {
        log.info("Getting service count by skill level: {}", skillLevel);
        long count = serviceManagementService.getServiceCountBySkillLevel(skillLevel);
        return ResponseBuilder.success(count);
    }
    
}
