package com.kltn.scsms_api_service.core.service.businessService;

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
import com.kltn.scsms_api_service.core.entity.VehicleBrand;
import com.kltn.scsms_api_service.core.entity.VehicleType;
import com.kltn.scsms_api_service.core.service.entityService.*;
import com.kltn.scsms_api_service.exception.ClientSideException;
import com.kltn.scsms_api_service.exception.ErrorCode;
import com.kltn.scsms_api_service.mapper.VehicleBrandMapper;
import com.kltn.scsms_api_service.mapper.VehicleTypeMapper;
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
    
    private final VehicleBrandMapper vehicleBrandMapper;
    private final VehicleTypeMapper vehicleTypeMapper;
    
    private final UserService userService;
    private final VehicleProfileService vehicleProfileService;
    private final VehicleModelService vehicleModelService;
    private final VehicleBrandService vehicleBrandService;
    private final VehicleTypeService vehicleTypeService;
    
    public Page<VehicleBrandInfoDto> getAllVehicleBrands(VehicleBrandFilterParam vehicleBrandFilterParam) {
        Page<VehicleBrand> vehicleBrandPage = vehicleBrandService.getAllVehicleBrandsWithFilters(vehicleBrandFilterParam);
        
        return vehicleBrandPage.map(vehicleBrandMapper::toVehicleBrandInfoDto);
    }
    
    public List<VehicleBrandDropdownResponse> getAllVehicleBrandsForDropdown() {
        List<VehicleBrand> vehicleBrands = vehicleBrandService.getAllActiveVehicleBrands(true, false);
        
        return vehicleBrands.stream().map(vehicleBrandMapper::toVehicleBrandDropdownResponse)
            .toList();
    }
    
    public VehicleBrandInfoDto getVehicleBrandById(UUID brandId) {
        VehicleBrand vehicleBrand = vehicleBrandService.getVehicleBrandById(brandId);
        
        return vehicleBrandMapper.toVehicleBrandInfoDto(vehicleBrand);
    }
    
    public VehicleBrandInfoDto createVehicleBrand(CreateVehicleBrandRequest request) {
        // Validate unique brand code
        Optional<VehicleBrand> existingBrand = vehicleBrandService.getOtpVehicleBrandByCode(request.getBrandCode());
        if (existingBrand.isPresent()) {
            throw new RuntimeException("Vehicle brand with code " + request.getBrandCode() + " already exists.");
        }
        
        VehicleBrand newBrand = vehicleBrandService.saveVehicleBrand(vehicleBrandMapper.toEntity(request));
        
        return vehicleBrandMapper.toVehicleBrandInfoDto(newBrand);
    }
    
    public VehicleBrandInfoDto updateVehicleBrand(UUID brandId, UpdateVehicleBrandRequest request) {
        // Get existing brand
        VehicleBrand existingBrand = vehicleBrandService.getOtpVehicleBrandById(brandId)
            .orElseThrow(() -> new RuntimeException("Vehicle brand with ID " + brandId + " not found."));
        
        // Validate unique brand code if changed
        if (!existingBrand.getBrandCode().equals(request.getBrandCode())) {
            Optional<VehicleBrand> brandWithSameCode = vehicleBrandService.getOtpVehicleBrandByCode(request.getBrandCode());
            if (brandWithSameCode.isPresent()) {
                throw new ClientSideException(ErrorCode.DUPLICATE, "Vehicle brand with code " + request.getBrandCode() + " already exists.");
            }
        }
        
        // Update fields
        existingBrand.setBrandCode(request.getBrandCode());
        existingBrand.setBrandName(request.getBrandName());
        existingBrand.setDescription(request.getDescription());
        existingBrand.setBrandLogoUrl(request.getBrandLogoUrl());
        
        VehicleBrand updatedBrand = vehicleBrandService.saveVehicleBrand(existingBrand);
        
        return vehicleBrandMapper.toVehicleBrandInfoDto(updatedBrand);
    }
    
    public void deleteVehicleBrand(UUID uuid) {
        vehicleBrandService.softDeleteVehicleBrand(uuid);
    }
    
    public Page<VehicleTypeInfoDto> getAllVehicleTypes(VehicleTypeFilterParam vehicleTypeFilterParam) {
        Page<VehicleType> vehicleTypePage = vehicleTypeService.getAllVehicleTypesWithFilters(vehicleTypeFilterParam);
        
        return vehicleTypePage.map(vehicleTypeMapper::toVehicleTypeInfoDto);
    }
    
    public List<VehicleTypeDropdownResponse> getAllVehicleTypesForDropdown() {
        List<VehicleType> vehicleTypes = vehicleTypeService.getAllActiveVehicleTypes(true, false);
        
        return vehicleTypes.stream().map(vehicleTypeMapper::toVehicleTypeDropdownResponse)
            .toList();
    }
    
    public VehicleTypeInfoDto getVehicleTypeById(UUID typeId) {
        VehicleType vehicleType = vehicleTypeService.getVehicleTypeById(typeId);
        
        return vehicleTypeMapper.toVehicleTypeInfoDto(vehicleType);
    }
    
    public VehicleTypeInfoDto createVehicleType(CreateVehicleTypeRequest request) {
        // Validate unique type code
        Optional<VehicleType> existingType = vehicleTypeService.getOtpVehicleTypeByCode(request.getTypeCode());
        if (existingType.isPresent()) {
            throw new RuntimeException("Vehicle type with code " + request.getTypeCode() + " already exists.");
        }
        
        VehicleType newType = vehicleTypeService.saveVehicleType(vehicleTypeMapper.toEntity(request));
        
        return vehicleTypeMapper.toVehicleTypeInfoDto(newType);
    }
    
    public VehicleTypeInfoDto updateVehicleType(UUID typeId, UpdateVehicleTypeRequest request) {
        // Get existing type
        VehicleType existingType = vehicleTypeService.getOtpVehicleTypeById(typeId)
            .orElseThrow(() -> new RuntimeException("Vehicle type with ID " + typeId + " not found."));
        
        // Validate unique type code if changed
        if (!existingType.getTypeCode().equals(request.getTypeCode())) {
            Optional<VehicleType> typeWithSameCode = vehicleTypeService.getOtpVehicleTypeByCode(request.getTypeCode());
            if (typeWithSameCode.isPresent()) {
                throw new ClientSideException(ErrorCode.DUPLICATE, "Vehicle type with code " + request.getTypeCode() + " already exists.");
            }
        }
        
        // Update fields
        existingType.setTypeCode(request.getTypeCode());
        existingType.setTypeName(request.getTypeName());
        existingType.setDescription(request.getDescription());
        
        VehicleType updatedType = vehicleTypeService.saveVehicleType(existingType);
        
        return vehicleTypeMapper.toVehicleTypeInfoDto(updatedType);
    }
    
    public void deleteVehicleType(UUID uuid) {
        vehicleTypeService.softDeleteVehicleType(uuid);
    }
}
