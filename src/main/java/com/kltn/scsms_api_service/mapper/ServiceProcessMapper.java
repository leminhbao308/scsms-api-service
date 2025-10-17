package com.kltn.scsms_api_service.mapper;

import com.kltn.scsms_api_service.core.dto.serviceProcessManagement.ServiceProcessInfoDto;
import com.kltn.scsms_api_service.core.dto.serviceProcessManagement.request.CreateServiceProcessRequest;
import com.kltn.scsms_api_service.core.dto.serviceProcessManagement.request.UpdateServiceProcessRequest;
import com.kltn.scsms_api_service.core.entity.ServiceProcess;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    uses = {AuditMapper.class, ServiceProcessStepMapper.class}
)
public interface ServiceProcessMapper {
    
    @Mapping(target = "stepCount", expression = "java(serviceProcess.getStepCount())")
    @Mapping(target = "processSteps", source = "processSteps")
    @Mapping(target = "audit.createdDate", source = "createdDate")
    @Mapping(target = "audit.modifiedDate", source = "modifiedDate")
    @Mapping(target = "audit.createdBy", source = "createdBy")
    @Mapping(target = "audit.modifiedBy", source = "modifiedBy")
    @Mapping(target = "audit.isActive", source = "isActive")
    @Mapping(target = "audit.isDeleted", source = "isDeleted")
    ServiceProcessInfoDto toServiceProcessInfoDto(ServiceProcess serviceProcess);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "processSteps", ignore = true) // Will be handled separately
    @Mapping(target = "services", ignore = true)
    @Mapping(target = "isActive", constant = "true")
    @Mapping(target = "isDefault", constant = "false")
    @Mapping(target = "isDeleted", constant = "false")
    ServiceProcess toEntity(CreateServiceProcessRequest createServiceProcessRequest);
    
    default ServiceProcess updateEntity(ServiceProcess existingServiceProcess, UpdateServiceProcessRequest updateRequest) {
        if (updateRequest == null) {
            return existingServiceProcess;
        }
        
        if (updateRequest.getCode() != null) {
            existingServiceProcess.setCode(updateRequest.getCode());
        }
        if (updateRequest.getName() != null) {
            existingServiceProcess.setName(updateRequest.getName());
        }
        if (updateRequest.getDescription() != null) {
            existingServiceProcess.setDescription(updateRequest.getDescription());
        }
        // Loại bỏ estimated_duration - thời gian được quản lý ở Service level
        if (updateRequest.getIsDefault() != null) {
            existingServiceProcess.setIsDefault(updateRequest.getIsDefault());
        }
        if (updateRequest.getIsActive() != null) {
            existingServiceProcess.setIsActive(updateRequest.getIsActive());
        }
        
        return existingServiceProcess;
    }
}
