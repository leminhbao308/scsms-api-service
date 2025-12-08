package com.kltn.scsms_api_service.mapper;

import com.kltn.scsms_api_service.core.dto.centerManagement.CenterInfoDto;
import com.kltn.scsms_api_service.core.dto.centerManagement.request.CreateCenterRequest;
import com.kltn.scsms_api_service.core.dto.centerManagement.request.UpdateCenterRequest;
import com.kltn.scsms_api_service.core.entity.Center;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    uses = {AuditMapper.class}
)
public interface CenterMapper {
    
    @Mapping(target = "managerId", source = "manager.userId")
    CenterInfoDto toCenterInfoDto(Center center);
    
    @Mapping(target = "branches", ignore = true)
    @Mapping(target = "businessHours", ignore = true)
    @Mapping(target = "socialMedia", ignore = true)
    @Mapping(target = "manager", ignore = true)
    @Mapping(target = "managerAssignedAt", ignore = true)
    @Mapping(target = "managerAssignedBy", ignore = true)
    @Mapping(target = "operatingStatus", ignore = true)
    Center toEntity(CreateCenterRequest createCenterRequest);
    
    default Center updateEntity(Center existingCenter, UpdateCenterRequest updateRequest) {
        if (updateRequest == null) {
            return existingCenter;
        }
        
        if (updateRequest.getCenterName() != null) {
            existingCenter.setCenterName(updateRequest.getCenterName());
        }
        if (updateRequest.getCenterCode() != null) {
            existingCenter.setCenterCode(updateRequest.getCenterCode());
        }
        if (updateRequest.getDescription() != null) {
            existingCenter.setDescription(updateRequest.getDescription());
        }
        if (updateRequest.getHeadquartersAddress() != null) {
            existingCenter.setHeadquartersAddress(updateRequest.getHeadquartersAddress());
        }
        if (updateRequest.getHeadquartersPhone() != null) {
            existingCenter.setHeadquartersPhone(updateRequest.getHeadquartersPhone());
        }
        if (updateRequest.getHeadquartersEmail() != null) {
            existingCenter.setHeadquartersEmail(updateRequest.getHeadquartersEmail());
        }
        if (updateRequest.getWebsite() != null) {
            existingCenter.setWebsite(updateRequest.getWebsite());
        }
        if (updateRequest.getTaxCode() != null) {
            existingCenter.setTaxCode(updateRequest.getTaxCode());
        }
        if (updateRequest.getBusinessLicense() != null) {
            existingCenter.setBusinessLicense(updateRequest.getBusinessLicense());
        }
        if (updateRequest.getLogoUrl() != null) {
            existingCenter.setLogoUrl(updateRequest.getLogoUrl());
        }
        if (updateRequest.getEstablishedDate() != null) {
            existingCenter.setEstablishedDate(updateRequest.getEstablishedDate());
        }
        if (updateRequest.getOperatingStatus() != null) {
            existingCenter.setOperatingStatus(updateRequest.getOperatingStatus());
        }
        if (updateRequest.getIsActive() != null) {
            existingCenter.setIsActive(updateRequest.getIsActive());
        }
        
        return existingCenter;
    }
    
    default CenterInfoDto toCenterInfoDtoWithManager(Center center) {
        CenterInfoDto dto = toCenterInfoDto(center);
        
        if (center.getManager() != null) {
            dto.setManagerId(center.getManager().getUserId());
            dto.setManagerAssignedAt(center.getManagerAssignedAt());
            dto.setManagerAssignedBy(center.getManagerAssignedBy());
        }
        
        return dto;
    }
}
