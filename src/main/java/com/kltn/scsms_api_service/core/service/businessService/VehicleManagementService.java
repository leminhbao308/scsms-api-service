package com.kltn.scsms_api_service.core.service.businessService;

import com.kltn.scsms_api_service.core.dto.vehicleManagement.VehicleBrandInfoDto;
import com.kltn.scsms_api_service.core.dto.vehicleManagement.param.VehicleBrandFilterParam;
import com.kltn.scsms_api_service.core.dto.vehicleManagement.request.CreateVehicleBrandRequest;
import com.kltn.scsms_api_service.core.dto.vehicleManagement.request.UpdateVehicleBrandRequest;
import com.kltn.scsms_api_service.core.dto.vehicleManagement.response.VehicleBrandDropdownResponse;
import com.kltn.scsms_api_service.core.entity.VehicleBrand;
import com.kltn.scsms_api_service.core.service.entityService.*;
import com.kltn.scsms_api_service.exception.ClientSideException;
import com.kltn.scsms_api_service.exception.ErrorCode;
import com.kltn.scsms_api_service.mapper.VehicleMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class VehicleManagementService {
    
    private final VehicleMapper vehicleMapper;
    
    private final UserService userService;
    private final VehicleProfileService vehicleProfileService;
    private final VehicleModelService vehicleModelService;
    private final VehicleBrandService vehicleBrandService;
    private final VehicleTypeService vehicleTypeService;
    
    public Page<VehicleBrandInfoDto> getAllVehicleBrands(VehicleBrandFilterParam vehicleBrandFilterParam) {
        Page<VehicleBrand> vehicleBrandPage = vehicleBrandService.getAllVehicleBrandsWithFilters(vehicleBrandFilterParam);
        
        return vehicleBrandPage.map(vehicleMapper::toVehicleBrandInfoDto);
    }
    
    public List<VehicleBrandDropdownResponse> getAllVehicleBrandsForDropdown() {
        List<VehicleBrand> vehicleBrands = vehicleBrandService.getAllActiveVehicleBrands(true, false);
        
        return vehicleBrands.stream().map(vehicleMapper::toVehicleBrandDropdownResponse)
            .toList();
    }
    
    public VehicleBrandInfoDto getVehicleBrandById(UUID brandId) {
        VehicleBrand vehicleBrand = vehicleBrandService.getVehicleBrandById(brandId);
        
        return vehicleMapper.toVehicleBrandInfoDto(vehicleBrand);
    }
    
    public VehicleBrandInfoDto createVehicleBrand(CreateVehicleBrandRequest request) {
        // Validate unique brand code
        Optional<VehicleBrand> existingBrand = vehicleBrandService.getOtpVehicleBrandByCode(request.getBrandCode());
        if (existingBrand.isPresent()) {
            throw new RuntimeException("Vehicle brand with code " + request.getBrandCode() + " already exists.");
        }
        
        VehicleBrand newBrand = vehicleBrandService.saveVehicleBrand(vehicleMapper.toEntity(request));
        
        return vehicleMapper.toVehicleBrandInfoDto(newBrand);
    }
    
    public VehicleBrandInfoDto updateVehicleBrand(UUID brandId, UpdateVehicleBrandRequest updateUserRequest) {
        // Get existing brand
        VehicleBrand existingBrand = vehicleBrandService.getOtpVehicleBrandById(brandId)
            .orElseThrow(() -> new RuntimeException("Vehicle brand with ID " + brandId + " not found."));
        
        // Validate unique brand code if changed
        if (!existingBrand.getBrandCode().equals(updateUserRequest.getBrandCode())) {
            Optional<VehicleBrand> brandWithSameCode = vehicleBrandService.getOtpVehicleBrandByCode(updateUserRequest.getBrandCode());
            if (brandWithSameCode.isPresent()) {
                throw new ClientSideException(ErrorCode.DUPLICATE, "Vehicle brand with code " + updateUserRequest.getBrandCode() + " already exists.");
            }
        }
        
        // Update fields
        existingBrand.setBrandCode(updateUserRequest.getBrandCode());
        existingBrand.setBrandName(updateUserRequest.getBrandName());
        existingBrand.setDescription(updateUserRequest.getDescription());
        existingBrand.setBrandLogoUrl(updateUserRequest.getBrandLogoUrl());
        
        VehicleBrand updatedBrand = vehicleBrandService.saveVehicleBrand(existingBrand);
        
        return vehicleMapper.toVehicleBrandInfoDto(updatedBrand);
    }
    
    public void deleteVehicleBrand(UUID uuid) {
        vehicleBrandService.softDeleteVehicleBrand(uuid);
    }
}
