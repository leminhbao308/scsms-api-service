package com.kltn.scsms_api_service.mapper;

import com.kltn.scsms_api_service.core.dto.serviceManagement.ServiceInfoDto;
import com.kltn.scsms_api_service.core.dto.serviceManagement.request.CreateServiceRequest;
import com.kltn.scsms_api_service.core.dto.serviceManagement.request.UpdateServiceRequest;
import com.kltn.scsms_api_service.core.entity.Service;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ServiceMapper {

    @Mapping(target = "categoryId", source = "category.categoryId")
    @Mapping(target = "categoryName", source = "category.categoryName")
    @Mapping(target = "serviceProcessId", source = "serviceProcess.id")
    @Mapping(target = "serviceProcessName", source = "serviceProcess.name")
    @Mapping(target = "serviceProcessCode", source = "serviceProcess.code")
    @Mapping(target = "estimatedDuration", source = "estimatedDuration")
    ServiceInfoDto toServiceInfoDto(Service service);

    @Mapping(target = "category", ignore = true) // Will be set in service
    @Mapping(target = "serviceProcess", ignore = true) // Will be set in service
    @Mapping(target = "serviceId", ignore = true)
    @Mapping(target = "isActive", constant = "true")
    @Mapping(target = "isDefaultProcess", constant = "false")
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
        if (updateRequest.getServiceTypeId() != null) {
            existingService.setServiceTypeId(updateRequest.getServiceTypeId());
        }
        if (updateRequest.getIsFeatured() != null) {
            existingService.setIsFeatured(updateRequest.getIsFeatured());
        }
        if (updateRequest.getIsActive() != null) {
            existingService.setIsActive(updateRequest.getIsActive());
        }
        if (updateRequest.getServiceProcessId() != null) {
            // Will be set in service layer
        }
        if (updateRequest.getIsDefaultProcess() != null) {
            existingService.setIsDefaultProcess(updateRequest.getIsDefaultProcess());
        }
        if (updateRequest.getBranchId() != null) {
            existingService.setBranchId(updateRequest.getBranchId());
        }

        return existingService;
    }
}