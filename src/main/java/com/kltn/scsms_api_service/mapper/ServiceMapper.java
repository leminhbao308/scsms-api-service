package com.kltn.scsms_api_service.mapper;

import com.kltn.scsms_api_service.core.dto.serviceManagement.ServiceInfoDto;
import com.kltn.scsms_api_service.core.dto.serviceManagement.request.CreateServiceRequest;
import com.kltn.scsms_api_service.core.dto.serviceManagement.request.UpdateServiceRequest;
import com.kltn.scsms_api_service.core.entity.Service;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = {AuditMapper.class})
public interface ServiceMapper {

    @Mapping(target = "categoryId", source = "category.categoryId")
    @Mapping(target = "categoryName", source = "category.categoryName")
    @Mapping(target = "serviceProcessId", source = "serviceProcess.id")
    @Mapping(target = "serviceProcessName", source = "serviceProcess.name")
    @Mapping(target = "serviceProcessCode", source = "serviceProcess.code")
    @Mapping(target = "serviceTypeName", ignore = true) // Will be set in service layer
    @Mapping(target = "branchName", ignore = true) // Will be set in service layer
    @Mapping(target = "estimatedDuration", source = "estimatedDuration")
    @Mapping(target = "audit.createdDate", source = "createdDate")
    @Mapping(target = "audit.modifiedDate", source = "modifiedDate")
    @Mapping(target = "audit.createdBy", source = "createdBy")
    @Mapping(target = "audit.modifiedBy", source = "modifiedBy")
    @Mapping(target = "audit.isActive", source = "isActive")
    @Mapping(target = "audit.isDeleted", source = "isDeleted")
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
        if (updateRequest.getRequiredSkillLevel() != null) {
            existingService.setRequiredSkillLevel(updateRequest.getRequiredSkillLevel());
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