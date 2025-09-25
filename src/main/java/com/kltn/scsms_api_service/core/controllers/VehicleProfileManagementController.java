package com.kltn.scsms_api_service.core.controllers;

import com.kltn.scsms_api_service.core.annotations.RequireRole;
import com.kltn.scsms_api_service.core.annotations.SwaggerOperation;
import com.kltn.scsms_api_service.core.constants.ApiConstant;
import com.kltn.scsms_api_service.core.dto.response.ApiResponse;
import com.kltn.scsms_api_service.core.dto.response.PaginatedResponse;
import com.kltn.scsms_api_service.core.dto.vehicleManagement.VehicleProfileInfoDto;
import com.kltn.scsms_api_service.core.dto.vehicleManagement.param.VehicleProfileFilterParam;
import com.kltn.scsms_api_service.core.dto.vehicleManagement.request.CreateVehicleProfileRequest;
import com.kltn.scsms_api_service.core.dto.vehicleManagement.request.UpdateVehicleProfileRequest;
import com.kltn.scsms_api_service.core.service.businessService.VehicleProfileManagementService;
import com.kltn.scsms_api_service.core.utils.ResponseBuilder;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Controller handling vehicle management operations
 * Manages user's vehicle profiles and related data.
 * Provides endpoints for CRUD operations on vehicle profile related data.
 */
@RestController
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Vehicle Profile Management", description = "Vehicle profile management endpoints")
public class VehicleProfileManagementController {
    
    private final VehicleProfileManagementService vehicleProfileManagementService;
    
    @GetMapping(ApiConstant.GET_ALL_VEHICLE_PROFILES_API)
    @SwaggerOperation(
        summary = "Get all vehicle profiles",
        description = "Retrieve a paginated list of all vehicle profiles that can be filtered ")
    @RequireRole(roles = {"ADMIN", "MANAGER", "TECHNICIAN"})
    public ResponseEntity<ApiResponse<PaginatedResponse<VehicleProfileInfoDto>>> getAllVehicleProfile(@ModelAttribute VehicleProfileFilterParam vehicleProfileFilterParam) {
        log.info("Fetching all vehicle profiles with filters: {}", vehicleProfileFilterParam);
        
        Page<VehicleProfileInfoDto> vehicleProfiles = vehicleProfileManagementService.getAllVehicleProfiles(VehicleProfileFilterParam.standardize(vehicleProfileFilterParam));
        
        return ResponseBuilder.paginated("Vehicle brands fetched successfully", vehicleProfiles);
    }
    
    @GetMapping(ApiConstant.GET_VEHICLE_PROFILE_BY_ID_API)
    @SwaggerOperation(
        summary = "Get vehicle profile by ID",
        description = "Retrieve detailed information about a specific vehicle profile by its unique identifier.")
    @RequireRole(roles = {"ADMIN", "MANAGER", "TECHNICIAN"})
    public ResponseEntity<ApiResponse<VehicleProfileInfoDto>> getVehicleProfileById(@PathVariable("profileId") String profileId) {
        log.info("Fetching vehicle profile with ID: {}", profileId);
        
        VehicleProfileInfoDto vehicleProfile = vehicleProfileManagementService.getVehicleProfileById(UUID.fromString(profileId));
        
        return ResponseBuilder.success("Vehicle profile fetched successfully", vehicleProfile);
    }
    
    @PostMapping(ApiConstant.CREATE_VEHICLE_PROFILE_API)
    @SwaggerOperation(
        summary = "Create a new vehicle profile",
        description = "Add a new vehicle profile to the system with details like name and description.")
    @RequireRole(roles = {"ADMIN", "MANAGER"})
    public ResponseEntity<ApiResponse<VehicleProfileInfoDto>> createVehicleProfile(@RequestBody CreateVehicleProfileRequest request) {
        VehicleProfileInfoDto createdProfile = vehicleProfileManagementService.createVehicleProfile(request);
        
        log.info("Created new vehicle profile with id: {}", createdProfile.getVehicleId());
        return ResponseBuilder.success("Vehicle profile created successfully", createdProfile);
    }
    
    @PostMapping(ApiConstant.UPDATE_VEHICLE_PROFILE_API)
    @SwaggerOperation(
        summary = "Update an existing vehicle profile",
        description = "Modify the details of an existing vehicle profile by its ID.")
    @RequireRole(roles = {"ADMIN", "MANAGER"})
    public ResponseEntity<ApiResponse<VehicleProfileInfoDto>> updateVehicleProfile(
        @PathVariable("profileId") String profileId,
        @RequestBody UpdateVehicleProfileRequest request) {
        log.info("Updating vehicle profile with ID: {}", profileId);
        
        VehicleProfileInfoDto updatedProfile = vehicleProfileManagementService.updateVehicleProfile(UUID.fromString(profileId), request);
        
        return ResponseBuilder.success("Vehicle profile updated successfully", updatedProfile);
    }
    
    @PostMapping(ApiConstant.DELETE_VEHICLE_PROFILE_API)
    @SwaggerOperation(
        summary = "Delete a vehicle profile",
        description = "Remove a vehicle profile from the system by its ID.")
    @RequireRole(roles = {"ADMIN", "MANAGER"})
    public ResponseEntity<ApiResponse<Void>> deleteVehicleProfile(@PathVariable("profileId") String profileId) {
        log.info("Deleting vehicle profile with ID: {}", profileId);
        vehicleProfileManagementService.deleteVehicleProfile(UUID.fromString(profileId));
        return ResponseBuilder.success("Vehicle profile deleted successfully");
    }
}
