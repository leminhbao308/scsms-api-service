package com.kltn.scsms_api_service.core.service.businessService;

import com.kltn.scsms_api_service.core.dto.vehicleManagement.VehicleProfileInfoDto;
import com.kltn.scsms_api_service.core.dto.vehicleManagement.param.VehicleProfileFilterParam;
import com.kltn.scsms_api_service.core.dto.vehicleManagement.request.CreateVehicleProfileRequest;
import com.kltn.scsms_api_service.core.dto.vehicleManagement.request.UpdateVehicleProfileRequest;
import com.kltn.scsms_api_service.core.entity.*;
import com.kltn.scsms_api_service.core.service.entityService.*;
import com.kltn.scsms_api_service.core.service.websocket.WebSocketService;
import com.kltn.scsms_api_service.exception.ClientSideException;
import com.kltn.scsms_api_service.exception.ErrorCode;
import com.kltn.scsms_api_service.mapper.VehicleProfileMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class VehicleProfileManagementService {
    
    private final VehicleProfileMapper vehicleProfileMapper;
    
    private final UserService userService;
    
    private final VehicleProfileService vehicleProfileService;
    private final VehicleBrandService vehicleBrandService;
    private final VehicleTypeService vehicleTypeService;
    private final VehicleModelService vehicleModelService;
    private final WebSocketService webSocketService;
    
    public Page<VehicleProfileInfoDto> getAllVehicleProfiles(VehicleProfileFilterParam VehicleProfileFilterParam) {
        Page<VehicleProfile> vehicleProfilePage = vehicleProfileService.getAllVehicleProfilesWithFilters(VehicleProfileFilterParam);
        
        return vehicleProfilePage.map(vehicleProfileMapper::toVehicleProfileInfoDto);
    }
    
    public VehicleProfileInfoDto getVehicleProfileById(UUID modelId) {
        VehicleProfile VehicleProfile = vehicleProfileService.getVehicleProfileById(modelId);
        
        return vehicleProfileMapper.toVehicleProfileInfoDto(VehicleProfile);
    }
    
    public VehicleProfileInfoDto createVehicleProfile(CreateVehicleProfileRequest request) {
        // Validate
        validateVehicleProfileRequest(request.getVehicleModelId(), request.getVehicleBrandId(), request.getVehicleTypeId());
        
        VehicleProfile newProfile = vehicleProfileService.saveVehicleProfile(vehicleProfileMapper.toEntity(request));
        
        // Notify WebSocket clients about vehicle profile creation
        try {
            webSocketService.notifyVehicleProfileReload();
            log.info("WebSocket: Vehicle profile creation notification sent");
        } catch (Exception ex) {
            log.error("WebSocket: Failed to send vehicle profile creation notification: {}", ex.getMessage(), ex);
            // Don't fail the operation if WebSocket notification fails
        }
        
        return vehicleProfileMapper.toVehicleProfileInfoDto(newProfile);
    }
    
    public VehicleProfileInfoDto updateVehicleProfile(UUID profileId, UpdateVehicleProfileRequest request) {
        // Get existing model
        VehicleProfile existingProfile = vehicleProfileService.getOtpVehicleProfileById(profileId)
            .orElseThrow(() -> new RuntimeException("Vehicle type with ID " + profileId + " not found."));
        
        // Validate existing model id if changed
        if (!existingProfile.getVehicleModelId().equals(request.getVehicleModelId())) {
            VehicleModel existingModel = vehicleModelService.getVehicleModelRefById(request.getVehicleModelId());
            if (existingModel == null) {
                throw new ClientSideException(ErrorCode.NOT_FOUND, "Vehicle model with id " + request.getVehicleModelId() + " does not exists.");
            }
        }
        
        // Validate existing brand id if changed
        if (!existingProfile.getVehicleBrandId().equals(request.getVehicleBrandId())) {
            VehicleBrand existingBrand = vehicleBrandService.getVehicleBrandRefById(request.getVehicleBrandId());
            if (existingBrand == null) {
                throw new ClientSideException(ErrorCode.NOT_FOUND, "Vehicle brand with id " + request.getVehicleBrandId() + " does not exists.");
            }
        }
        
        // Validate existing type id if changed
        if (!existingProfile.getVehicleTypeId().equals(request.getVehicleTypeId())) {
            VehicleType existingType = vehicleTypeService.getVehicleTypeRefById(request.getVehicleTypeId());
            if (existingType == null) {
                throw new ClientSideException(ErrorCode.NOT_FOUND, "Vehicle type with id " + request.getVehicleTypeId() + " does not exists.");
            }
        }
        
        // Validate existing owner id if changed
        if (!existingProfile.getOwnerId().equals(request.getOwnerId())) {
            User ownerRef = userService.getUserRefById(request.getOwnerId());
            if (ownerRef == null) {
                throw new ClientSideException(ErrorCode.NOT_FOUND, "Owner with id " + request.getOwnerId() + " does not exists.");
            }
        }
        
        // Update fields
        existingProfile.setVehicleModelId(request.getVehicleModelId());
        existingProfile.setVehicleBrandId(request.getVehicleBrandId());
        existingProfile.setVehicleTypeId(request.getVehicleTypeId());
        existingProfile.setOwnerId(request.getOwnerId());
        existingProfile.setLicensePlate(request.getLicensePlate());
        existingProfile.setDescription(request.getDescription());
        existingProfile.setDistanceTraveled(request.getDistanceTraveled());
        existingProfile.setIsActive(request.getIsActive());
        
        // Save updated model
        VehicleProfile updatedModel = vehicleProfileService.saveVehicleProfile(existingProfile);
        
        // Notify WebSocket clients about vehicle profile update
        try {
            webSocketService.notifyVehicleProfileReload();
            log.info("WebSocket: Vehicle profile update notification sent");
        } catch (Exception ex) {
            log.error("WebSocket: Failed to send vehicle profile update notification: {}", ex.getMessage(), ex);
            // Don't fail the operation if WebSocket notification fails
        }
        
        return vehicleProfileMapper.toVehicleProfileInfoDto(updatedModel);
    }
    
    public void deleteVehicleProfile(UUID uuid) {
        vehicleProfileService.softDeleteVehicleProfile(uuid);
        
        // Notify WebSocket clients about vehicle profile deletion
        try {
            webSocketService.notifyVehicleProfileReload();
            log.info("WebSocket: Vehicle profile deletion notification sent");
        } catch (Exception ex) {
            log.error("WebSocket: Failed to send vehicle profile deletion notification: {}", ex.getMessage(), ex);
            // Don't fail the operation if WebSocket notification fails
        }
    }
    
    private void validateVehicleProfileRequest(UUID modelId, UUID brandId, UUID typeId) {
        // Validate brandId exists
        VehicleBrand brandRef = vehicleBrandService.getVehicleBrandRefById(brandId);
        if (brandRef == null) {
            throw new ClientSideException(ErrorCode.NOT_FOUND, "Vehicle brand not found with ID: " + brandId);
        }
        
        // Validate typeId exists
        VehicleType typeRef = vehicleTypeService.getVehicleTypeRefById(typeId);
        if (typeRef == null) {
            throw new ClientSideException(ErrorCode.NOT_FOUND, "Vehicle type not found with ID: " + typeId);
        }
        
        // Validate modelId exists
        VehicleModel modelRef = vehicleModelService.getVehicleModelRefById(modelId);
        if (modelRef == null) {
            throw new ClientSideException(ErrorCode.NOT_FOUND, "Vehicle model not found with ID: " + modelId);
        }
    }
    
    public Page<VehicleProfileInfoDto> getAllVehicleProfilesByOwnerId(UUID ownerId, VehicleProfileFilterParam vehicleProfileFilterParam) {
        Page<VehicleProfile> vehicleProfilePage = vehicleProfileService.getAllVehicleProfilesByOwnerIdWithFilters(ownerId, vehicleProfileFilterParam);
        
        return vehicleProfilePage.map(vehicleProfileMapper::toVehicleProfileInfoDto);
    }
}
