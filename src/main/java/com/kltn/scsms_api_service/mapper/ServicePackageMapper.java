package com.kltn.scsms_api_service.mapper;

import com.kltn.scsms_api_service.core.dto.servicePackageManagement.ServicePackageInfoDto;
import com.kltn.scsms_api_service.core.dto.servicePackageManagement.request.CreateServicePackageRequest;
import com.kltn.scsms_api_service.core.dto.servicePackageManagement.request.UpdateServicePackageRequest;
import com.kltn.scsms_api_service.core.entity.ServicePackage;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    uses = {AuditMapper.class, CategoryMapper.class, ServicePackageServiceMapper.class}
)
public interface ServicePackageMapper {
    
    @Mapping(target = "categoryId", source = "category.categoryId")
    @Mapping(target = "categoryName", source = "category.categoryName")
    @Mapping(target = "serviceCost", expression = "java(servicePackage.calculateServiceCost())")
    @Mapping(target = "packageServices", source = "packageServices")
    @Mapping(target = "serviceProcessId", source = "serviceProcess.id")
    @Mapping(target = "serviceProcessName", source = "serviceProcess.name")
    @Mapping(target = "serviceProcessCode", source = "serviceProcess.code")
    @Mapping(target = "serviceCount", expression = "java(servicePackage.getServiceCount())")
    ServicePackageInfoDto toServicePackageInfoDto(ServicePackage servicePackage);
    
    @Mapping(target = "category", ignore = true) // Will be set in service
    @Mapping(target = "serviceProcess", ignore = true) // Will be set in service
    @Mapping(target = "packageId", ignore = true)
    @Mapping(target = "packageServices", ignore = true) // Will be handled separately
    @Mapping(target = "packagePrice", ignore = true) // Will be calculated
    @Mapping(target = "totalDuration", ignore = true) // Will be calculated
    @Mapping(target = "isActive", constant = "true")
    @Mapping(target = "isDefaultProcess", constant = "false")
    @Mapping(target = "isDeleted", constant = "false")
    ServicePackage toEntity(CreateServicePackageRequest createServicePackageRequest);
    
    default ServicePackage updateEntity(ServicePackage existingServicePackage, UpdateServicePackageRequest updateRequest) {
        if (updateRequest == null) {
            return existingServicePackage;
        }
        
        if (updateRequest.getPackageName() != null) {
            existingServicePackage.setPackageName(updateRequest.getPackageName());
        }
        if (updateRequest.getDescription() != null) {
            existingServicePackage.setDescription(updateRequest.getDescription());
        }
        if (updateRequest.getTotalDuration() != null) {
            existingServicePackage.setTotalDuration(updateRequest.getTotalDuration());
        }
        if (updateRequest.getServicePackageTypeId() != null) {
            existingServicePackage.setServicePackageTypeId(updateRequest.getServicePackageTypeId());
        }
        if (updateRequest.getIsActive() != null) {
            existingServicePackage.setIsActive(updateRequest.getIsActive());
        }
        if (updateRequest.getServiceProcessId() != null) {
            // Will be set in service layer
        }
        if (updateRequest.getIsDefaultProcess() != null) {
            existingServicePackage.setIsDefaultProcess(updateRequest.getIsDefaultProcess());
        }
        
        return existingServicePackage;
    }
    
}