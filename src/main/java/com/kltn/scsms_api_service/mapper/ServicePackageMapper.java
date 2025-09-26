package com.kltn.scsms_api_service.mapper;

import com.kltn.scsms_api_service.core.dto.servicePackageManagement.ServicePackageInfoDto;
import com.kltn.scsms_api_service.core.dto.servicePackageManagement.ServicePackageStepInfoDto;
import com.kltn.scsms_api_service.core.dto.servicePackageManagement.request.CreateServicePackageRequest;
import com.kltn.scsms_api_service.core.dto.servicePackageManagement.request.CreateServicePackageStepRequest;
import com.kltn.scsms_api_service.core.dto.servicePackageManagement.request.UpdateServicePackageRequest;
import com.kltn.scsms_api_service.core.dto.servicePackageManagement.request.UpdateServicePackageStepRequest;
import com.kltn.scsms_api_service.core.entity.ServicePackage;
import com.kltn.scsms_api_service.core.entity.ServicePackageStep;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    uses = {AuditMapper.class, CategoryMapper.class}
)
public interface ServicePackageMapper {
    
    @Mapping(target = "categoryId", source = "category.categoryId")
    @Mapping(target = "categoryName", source = "category.categoryName")
    ServicePackageInfoDto toServicePackageInfoDto(ServicePackage servicePackage);
    
    @Mapping(target = "category", ignore = true) // Will be set in service
    @Mapping(target = "packageId", ignore = true)
    @Mapping(target = "packageSteps", ignore = true) // Will be handled separately
    @Mapping(target = "isActive", constant = "true")
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
        if (updateRequest.getPackagePrice() != null) {
            existingServicePackage.setPackagePrice(updateRequest.getPackagePrice());
        }
        if (updateRequest.getOriginalPrice() != null) {
            existingServicePackage.setOriginalPrice(updateRequest.getOriginalPrice());
        }
        if (updateRequest.getDiscountPercentage() != null) {
            existingServicePackage.setDiscountPercentage(updateRequest.getDiscountPercentage());
        }
        if (updateRequest.getSavingsAmount() != null) {
            existingServicePackage.setSavingsAmount(updateRequest.getSavingsAmount());
        }
        if (updateRequest.getPackageType() != null) {
            existingServicePackage.setPackageType(updateRequest.getPackageType());
        }
        if (updateRequest.getTargetVehicleTypes() != null) {
            existingServicePackage.setTargetVehicleTypes(updateRequest.getTargetVehicleTypes());
        }
        if (updateRequest.getValidityPeriodDays() != null) {
            existingServicePackage.setValidityPeriodDays(updateRequest.getValidityPeriodDays());
        }
        if (updateRequest.getMaxUsageCount() != null) {
            existingServicePackage.setMaxUsageCount(updateRequest.getMaxUsageCount());
        }
        if (updateRequest.getIsLimitedTime() != null) {
            existingServicePackage.setIsLimitedTime(updateRequest.getIsLimitedTime());
        }
        if (updateRequest.getStartDate() != null) {
            existingServicePackage.setStartDate(updateRequest.getStartDate());
        }
        if (updateRequest.getEndDate() != null) {
            existingServicePackage.setEndDate(updateRequest.getEndDate());
        }
        if (updateRequest.getIsPopular() != null) {
            existingServicePackage.setIsPopular(updateRequest.getIsPopular());
        }
        if (updateRequest.getIsRecommended() != null) {
            existingServicePackage.setIsRecommended(updateRequest.getIsRecommended());
        }
        if (updateRequest.getImageUrls() != null) {
            existingServicePackage.setImageUrls(updateRequest.getImageUrls());
        }
        if (updateRequest.getTags() != null) {
            existingServicePackage.setTags(updateRequest.getTags());
        }
        if (updateRequest.getSortOrder() != null) {
            existingServicePackage.setSortOrder(updateRequest.getSortOrder());
        }
        if (updateRequest.getTermsAndConditions() != null) {
            existingServicePackage.setTermsAndConditions(updateRequest.getTermsAndConditions());
        }
        if (updateRequest.getIsActive() != null) {
            existingServicePackage.setIsActive(updateRequest.getIsActive());
        }
        
        return existingServicePackage;
    }
    
    // ServicePackageStep mappings
    @Mapping(target = "packageId", source = "servicePackage.packageId")
    @Mapping(target = "packageName", source = "servicePackage.packageName")
    @Mapping(target = "referencedServiceId", source = "referencedService.serviceId")
    @Mapping(target = "referencedServiceName", source = "referencedService.serviceName")
    ServicePackageStepInfoDto toServicePackageStepInfoDto(ServicePackageStep servicePackageStep);
    
    List<ServicePackageStepInfoDto> toServicePackageStepInfoDtoList(List<ServicePackageStep> servicePackageSteps);
    
    @Mapping(target = "packageStepId", ignore = true)
    @Mapping(target = "servicePackage", ignore = true) // Will be set in service
    @Mapping(target = "referencedService", ignore = true) // Will be set in service
    @Mapping(target = "isActive", constant = "true")
    @Mapping(target = "isDeleted", constant = "false")
    ServicePackageStep toServicePackageStep(CreateServicePackageStepRequest createRequest);
    
    default ServicePackageStep updateServicePackageStep(ServicePackageStep existingStep, UpdateServicePackageStepRequest updateRequest) {
        if (updateRequest == null) {
            return existingStep;
        }
        
        if (updateRequest.getStepName() != null) {
            existingStep.setStepName(updateRequest.getStepName());
        }
        if (updateRequest.getDescription() != null) {
            existingStep.setDescription(updateRequest.getDescription());
        }
        if (updateRequest.getEstimatedDuration() != null) {
            existingStep.setEstimatedDuration(updateRequest.getEstimatedDuration());
        }
        if (updateRequest.getIsOptional() != null) {
            existingStep.setIsOptional(updateRequest.getIsOptional());
        }
        if (updateRequest.getInstructions() != null) {
            existingStep.setInstructions(updateRequest.getInstructions());
        }
        if (updateRequest.getStepType() != null) {
            existingStep.setStepType(updateRequest.getStepType());
        }
        if (updateRequest.getStepOrder() != null) {
            existingStep.setStepOrder(updateRequest.getStepOrder());
        }
        
        return existingStep;
    }
}