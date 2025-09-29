package com.kltn.scsms_api_service.mapper;

import com.kltn.scsms_api_service.core.dto.serviceManagement.ServiceInfoDto;
import com.kltn.scsms_api_service.core.dto.serviceManagement.ServiceProductDto;
import com.kltn.scsms_api_service.core.dto.serviceManagement.request.CreateServiceRequest;
import com.kltn.scsms_api_service.core.dto.serviceManagement.request.UpdateServiceRequest;
import com.kltn.scsms_api_service.core.entity.Service;
import com.kltn.scsms_api_service.core.entity.ServiceProduct;
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
    @Mapping(target = "serviceProducts", source = "serviceProducts")
    ServiceInfoDto toServiceInfoDto(Service service);
    
    ServiceProductDto toServiceProductDto(ServiceProduct serviceProduct);
    
    @Mapping(target = "category", ignore = true) // Will be set in service
    @Mapping(target = "serviceId", ignore = true)
    @Mapping(target = "serviceProducts", ignore = true) // Will be set separately
    @Mapping(target = "productCost", ignore = true) // Will be calculated
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
        if (updateRequest.getLaborCost() != null) {
            existingService.setLaborCost(updateRequest.getLaborCost());
        }
        if (updateRequest.getServiceType() != null) {
            existingService.setServiceType(updateRequest.getServiceType());
        }
        if (updateRequest.getPhotoRequired() != null) {
            existingService.setPhotoRequired(updateRequest.getPhotoRequired());
        }
        if (updateRequest.getImageUrls() != null) {
            existingService.setImageUrls(updateRequest.getImageUrls());
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