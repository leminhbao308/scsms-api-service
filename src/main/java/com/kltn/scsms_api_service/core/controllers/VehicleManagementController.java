package com.kltn.scsms_api_service.core.controllers;

import com.kltn.scsms_api_service.core.annotations.RequireRole;
import com.kltn.scsms_api_service.core.annotations.SwaggerOperation;
import com.kltn.scsms_api_service.core.constants.ApiConstant;
import com.kltn.scsms_api_service.core.dto.response.ApiResponse;
import com.kltn.scsms_api_service.core.dto.response.PaginatedResponse;
import com.kltn.scsms_api_service.core.dto.vehicleManagement.VehicleBrandInfoDto;
import com.kltn.scsms_api_service.core.dto.vehicleManagement.VehicleTypeInfoDto;
import com.kltn.scsms_api_service.core.dto.vehicleManagement.param.VehicleBrandFilterParam;
import com.kltn.scsms_api_service.core.dto.vehicleManagement.param.VehicleTypeFilterParam;
import com.kltn.scsms_api_service.core.dto.vehicleManagement.request.CreateVehicleBrandRequest;
import com.kltn.scsms_api_service.core.dto.vehicleManagement.request.CreateVehicleTypeRequest;
import com.kltn.scsms_api_service.core.dto.vehicleManagement.request.UpdateVehicleBrandRequest;
import com.kltn.scsms_api_service.core.dto.vehicleManagement.request.UpdateVehicleTypeRequest;
import com.kltn.scsms_api_service.core.dto.vehicleManagement.response.VehicleBrandDropdownResponse;
import com.kltn.scsms_api_service.core.dto.vehicleManagement.response.VehicleTypeDropdownResponse;
import com.kltn.scsms_api_service.core.service.businessService.VehicleManagementService;
import com.kltn.scsms_api_service.core.utils.ResponseBuilder;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Controller handling vehicle management operations
 * Manages vehicle brands, models, types, and profiles of users' vehicles.
 * Provides endpoints for CRUD operations on vehicle-related data.
 */
@RestController
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Vehicle Management", description = "Vehicle management endpoints")
public class VehicleManagementController {
    
    private final VehicleManagementService vehicleManagementService;
    
    @GetMapping(ApiConstant.GET_ALL_VEHICLE_BRANDS_API)
    @SwaggerOperation(
        summary = "Get all vehicle brands",
        description = "Retrieve a paginated list of all vehicle brands that can be filtered by name, code, etc.")
    @RequireRole(roles = {"ADMIN", "MANAGER", "TECHNICIAN"})
    public ResponseEntity<ApiResponse<PaginatedResponse<VehicleBrandInfoDto>>> getAllVehicleBrand(@ModelAttribute VehicleBrandFilterParam vehicleBrandFilterParam) {
        log.info("Fetching all vehicle brands");
        
        Page<VehicleBrandInfoDto> vehicleBrands = vehicleManagementService.getAllVehicleBrands(VehicleBrandFilterParam.standardize(vehicleBrandFilterParam));
        
        return ResponseBuilder.paginated("Vehicle brands fetched successfully", vehicleBrands);
    }
    
    @GetMapping(ApiConstant.GET_ALL_VEHICLE_TYPES_API)
    @SwaggerOperation(
        summary = "Get all vehicle types",
        description = "Retrieve a paginated list of all vehicle types that can be filtered by name, code, etc.")
    @RequireRole(roles = {"ADMIN", "MANAGER", "TECHNICIAN"})
    public ResponseEntity<ApiResponse<PaginatedResponse<VehicleTypeInfoDto>>> getAllVehicleType(@ModelAttribute VehicleTypeFilterParam vehicleTypeFilterParam) {
        log.info("Fetching all vehicle types");
        
        Page<VehicleTypeInfoDto> vehicleTypes = vehicleManagementService.getAllVehicleTypes(VehicleTypeFilterParam.standardize(vehicleTypeFilterParam));
        
        return ResponseBuilder.paginated("Vehicle brands fetched successfully", vehicleTypes);
    }
    
    
    
    @GetMapping(ApiConstant.GET_ALL_VEHICLE_BRANDS_DROPDOWN_API)
    @SwaggerOperation(
        summary = "Get all vehicle brands for dropdown",
        description = "Retrieve a list of all vehicle brands for populating dropdowns, without pagination.")
    @RequireRole(roles = {"ADMIN", "MANAGER", "TECHNICIAN"})
    public ResponseEntity<ApiResponse<List<VehicleBrandDropdownResponse>>> getAllVehicleBrandForDropdown() {
        log.info("Fetching all vehicle brands for dropdown");
        
        List<VehicleBrandDropdownResponse> vehicleBrands = vehicleManagementService.getAllVehicleBrandsForDropdown();
        
        return ResponseBuilder.success("Vehicle brands for dropdown fetched successfully", vehicleBrands);
    }
    
    @GetMapping(ApiConstant.GET_ALL_VEHICLE_TYPES_DROPDOWN_API)
    @SwaggerOperation(
        summary = "Get all vehicle types for dropdown",
        description = "Retrieve a list of all vehicle types for populating dropdowns, without pagination.")
    @RequireRole(roles = {"ADMIN", "MANAGER", "TECHNICIAN"})
    public ResponseEntity<ApiResponse<List<VehicleTypeDropdownResponse>>> getAllVehicleTypeForDropdown() {
        log.info("Fetching all vehicle types for dropdown");
        
        List<VehicleTypeDropdownResponse> vehicleTypes = vehicleManagementService.getAllVehicleTypesForDropdown();
        
        return ResponseBuilder.success("Vehicle brands for dropdown fetched successfully", vehicleTypes);
    }
    
    
    
    @GetMapping(ApiConstant.GET_VEHICLE_BRAND_BY_ID_API)
    @SwaggerOperation(
        summary = "Get vehicle brand by ID",
        description = "Retrieve detailed information about a specific vehicle brand by its unique identifier.")
    @RequireRole(roles = {"ADMIN", "MANAGER", "TECHNICIAN"})
    public ResponseEntity<ApiResponse<VehicleBrandInfoDto>> getVehicleBrandById(@PathVariable("brandId") String brandId) {
        log.info("Fetching vehicle brand with ID: {}", brandId);
        
        VehicleBrandInfoDto vehicleBrand = vehicleManagementService.getVehicleBrandById(UUID.fromString(brandId));
        
        return ResponseBuilder.success("Vehicle brand fetched successfully", vehicleBrand);
        
    }
    
    @GetMapping(ApiConstant.GET_VEHICLE_TYPE_BY_ID_API)
    @SwaggerOperation(
        summary = "Get vehicle type by ID",
        description = "Retrieve detailed information about a specific vehicle type by its unique identifier.")
    @RequireRole(roles = {"ADMIN", "MANAGER", "TECHNICIAN"})
    public ResponseEntity<ApiResponse<VehicleTypeInfoDto>> getVehicleById(@PathVariable("typeId") String typeID) {
        log.info("Fetching vehicle type with ID: {}", typeID);
        
        VehicleTypeInfoDto vehicleType = vehicleManagementService.getVehicleTypeById(UUID.fromString(typeID));
        
        return ResponseBuilder.success("Vehicle type fetched successfully", vehicleType);
        
    }
    
    
    
    @PostMapping(ApiConstant.CREATE_VEHICLE_BRAND_API)
    @SwaggerOperation(
        summary = "Create a new vehicle brand",
        description = "Add a new vehicle brand to the system with details like name, code, and description.")
    @RequireRole(roles = {"ADMIN", "MANAGER"})
    public ResponseEntity<ApiResponse<VehicleBrandInfoDto>> createVehicleBrand(@RequestBody CreateVehicleBrandRequest request) {
        VehicleBrandInfoDto createdBrand = vehicleManagementService.createVehicleBrand(request);
        
        log.info("Created new vehicle brand with code: {}", createdBrand.getBrandCode());
        return ResponseBuilder.success("Vehicle brand created successfully", createdBrand);
    }
    
    @PostMapping(ApiConstant.CREATE_VEHICLE_TYPE_API)
    @SwaggerOperation(
        summary = "Create a new vehicle type",
        description = "Add a new vehicle type to the system with details like name, code, and description.")
    @RequireRole(roles = {"ADMIN", "MANAGER"})
    public ResponseEntity<ApiResponse<VehicleTypeInfoDto>> createVehicleType(@RequestBody CreateVehicleTypeRequest request) {
        VehicleTypeInfoDto createdType = vehicleManagementService.createVehicleType(request);
        
        log.info("Created new vehicle type with code: {}", createdType.getTypeCode());
        return ResponseBuilder.success("Vehicle type created successfully", createdType);
    }
    
    
    
    @PostMapping(ApiConstant.UPDATE_VEHICLE_BRAND_API)
    @SwaggerOperation(
        summary = "Update an existing vehicle brand",
        description = "Modify the details of an existing vehicle brand by its ID.")
    @RequireRole(roles = {"ADMIN", "MANAGER"})
    public ResponseEntity<ApiResponse<VehicleBrandInfoDto>> updateVehicleBrand(
        @PathVariable("brandId") String brandId,
        @RequestBody UpdateVehicleBrandRequest request) {
        log.info("Updating vehicle brand with ID: {}", brandId);
        
        VehicleBrandInfoDto updatedBrand = vehicleManagementService.updateVehicleBrand(UUID.fromString(brandId), request);
        
        return ResponseBuilder.success("Vehicle brand updated successfully", updatedBrand);
    }
    
    @PostMapping(ApiConstant.UPDATE_VEHICLE_TYPE_API)
    @SwaggerOperation(
        summary = "Update an existing vehicle type",
        description = "Modify the details of an existing vehicle type by its ID.")
    @RequireRole(roles = {"ADMIN", "MANAGER"})
    public ResponseEntity<ApiResponse<VehicleTypeInfoDto>> updateVehicleType(
        @PathVariable("typeId") String typeId,
        @RequestBody UpdateVehicleTypeRequest request) {
        log.info("Updating vehicle type with ID: {}", typeId);
        
        VehicleTypeInfoDto updatedType = vehicleManagementService.updateVehicleType(UUID.fromString(typeId), request);
        
        return ResponseBuilder.success("Vehicle brand updated successfully", updatedType);
    }
    
    
    
    @PostMapping(ApiConstant.DELETE_VEHICLE_BRAND_API)
    @SwaggerOperation(
        summary = "Delete a vehicle brand",
        description = "Remove a vehicle brand from the system by its ID.")
    @RequireRole(roles = {"ADMIN", "MANAGER"})
    public ResponseEntity<ApiResponse<Void>> deleteVehicleBrand(@PathVariable("brandId") String brandId) {
        log.info("Deleting vehicle brand with ID: {}", brandId);
        vehicleManagementService.deleteVehicleBrand(UUID.fromString(brandId));
        return ResponseBuilder.success("Vehicle brand deleted successfully");
    }
    
    @PostMapping(ApiConstant.DELETE_VEHICLE_TYPE_API)
    @SwaggerOperation(
        summary = "Delete a vehicle type",
        description = "Remove a vehicle type from the system by its ID.")
    @RequireRole(roles = {"ADMIN", "MANAGER"})
    public ResponseEntity<ApiResponse<Void>> deleteVehicleType(@PathVariable("typeId") String typeId) {
        log.info("Deleting vehicle type with ID: {}", typeId);
        vehicleManagementService.deleteVehicleType(UUID.fromString(typeId));
        return ResponseBuilder.success("Vehicle type deleted successfully");
    }
}
