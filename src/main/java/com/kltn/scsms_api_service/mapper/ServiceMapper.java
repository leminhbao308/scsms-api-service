package com.kltn.scsms_api_service.mapper;

import com.kltn.scsms_api_service.core.dto.serviceManagement.ServiceInfoDto;
import com.kltn.scsms_api_service.core.dto.serviceManagement.request.CreateServiceRequest;
import com.kltn.scsms_api_service.core.dto.serviceManagement.request.UpdateServiceRequest;
import com.kltn.scsms_api_service.core.entity.Service;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    uses = {AuditMapper.class, CategoryMapper.class}
)
public interface ServiceMapper {
    
    @Mapping(target = "categoryId", source = "category.categoryId")
    @Mapping(target = "categoryName", source = "category.categoryName")
    ServiceInfoDto toServiceInfoDto(Service service);
    
    @Mapping(target = "category", ignore = true) // Will be set in service
    @Mapping(target = "serviceId", ignore = true)
    @Mapping(target = "isActive", constant = "true")
    @Mapping(target = "isDeleted", constant = "false")
    Service toEntity(CreateServiceRequest createServiceRequest);
    
    default Service updateEntity(Service existingService, UpdateServiceRequest updateRequest) {
        if (updateRequest == null) {
            return existingService;
        }
        
        if (updateRequest.getServiceName() != null) {
            existingService.setServiceName(updateRequest.getServiceName());
        }
        if (updateRequest.getDescription() != null) {
            existingService.setDescription(updateRequest.getDescription());
        }
        if (updateRequest.getStandardDuration() != null) {
            existingService.setStandardDuration(updateRequest.getStandardDuration());
        }
        if (updateRequest.getRequiredSkillLevel() != null) {
            existingService.setRequiredSkillLevel(updateRequest.getRequiredSkillLevel());
        }
        if (updateRequest.getIsPackage() != null) {
            existingService.setIsPackage(updateRequest.getIsPackage());
        }
        if (updateRequest.getBasePrice() != null) {
            existingService.setBasePrice(updateRequest.getBasePrice());
        }
        if (updateRequest.getMinPrice() != null) {
            existingService.setMinPrice(updateRequest.getMinPrice());
        }
        if (updateRequest.getMaxPrice() != null) {
            existingService.setMaxPrice(updateRequest.getMaxPrice());
        }
        if (updateRequest.getComplexityLevel() != null) {
            existingService.setComplexityLevel(updateRequest.getComplexityLevel());
        }
        if (updateRequest.getServiceType() != null) {
            existingService.setServiceType(updateRequest.getServiceType());
        }
        if (updateRequest.getVehicleTypes() != null) {
            existingService.setVehicleTypes(updateRequest.getVehicleTypes());
        }
        if (updateRequest.getRequiredTools() != null) {
            existingService.setRequiredTools(updateRequest.getRequiredTools());
        }
        if (updateRequest.getSafetyNotes() != null) {
            existingService.setSafetyNotes(updateRequest.getSafetyNotes());
        }
        if (updateRequest.getQualityCriteria() != null) {
            existingService.setQualityCriteria(updateRequest.getQualityCriteria());
        }
        if (updateRequest.getPhotoRequired() != null) {
            existingService.setPhotoRequired(updateRequest.getPhotoRequired());
        }
        if (updateRequest.getCustomerApprovalRequired() != null) {
            existingService.setCustomerApprovalRequired(updateRequest.getCustomerApprovalRequired());
        }
        if (updateRequest.getIsExpressService() != null) {
            existingService.setIsExpressService(updateRequest.getIsExpressService());
        }
        if (updateRequest.getIsPremiumService() != null) {
            existingService.setIsPremiumService(updateRequest.getIsPremiumService());
        }
        if (updateRequest.getImageUrls() != null) {
            existingService.setImageUrls(updateRequest.getImageUrls());
        }
        if (updateRequest.getTags() != null) {
            existingService.setTags(updateRequest.getTags());
        }
        if (updateRequest.getSortOrder() != null) {
            existingService.setSortOrder(updateRequest.getSortOrder());
        }
        if (updateRequest.getIsFeatured() != null) {
            existingService.setIsFeatured(updateRequest.getIsFeatured());
        }
        if (updateRequest.getIsActive() != null) {
            existingService.setIsActive(updateRequest.getIsActive());
        }
        
        return existingService;
    }
}