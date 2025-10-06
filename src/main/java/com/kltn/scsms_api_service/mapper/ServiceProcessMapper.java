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
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface ServiceProcessMapper {
    
    @Mapping(target = "stepCount", expression = "java(serviceProcess.getStepCount())")
    @Mapping(target = "processSteps", source = "processSteps")
    ServiceProcessInfoDto toServiceProcessInfoDto(ServiceProcess serviceProcess);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "processSteps", ignore = true) // Will be handled separately
    @Mapping(target = "services", ignore = true)
    @Mapping(target = "servicePackages", ignore = true)
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
        if (updateRequest.getEstimatedDuration() != null) {
            existingServiceProcess.setEstimatedDuration(updateRequest.getEstimatedDuration());
        }
        if (updateRequest.getIsDefault() != null) {
            existingServiceProcess.setIsDefault(updateRequest.getIsDefault());
        }
        if (updateRequest.getIsActive() != null) {
            existingServiceProcess.setIsActive(updateRequest.getIsActive());
        }
        
        return existingServiceProcess;
    }
}
