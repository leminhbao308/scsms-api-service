package com.kltn.scsms_api_service.mapper;

import com.kltn.scsms_api_service.core.dto.serviceProcessManagement.ServiceProcessStepInfoDto;
import com.kltn.scsms_api_service.core.dto.serviceProcessManagement.request.CreateServiceProcessStepRequest;
import com.kltn.scsms_api_service.core.dto.serviceProcessManagement.request.UpdateServiceProcessStepRequest;
import com.kltn.scsms_api_service.core.entity.ServiceProcessStep;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface ServiceProcessStepMapper {
    
    @Mapping(target = "processId", source = "serviceProcess.id")
    @Mapping(target = "processName", source = "serviceProcess.name")
    @Mapping(target = "isFirstStep", expression = "java(serviceProcessStep.isFirstStep())")
    @Mapping(target = "isLastStep", expression = "java(serviceProcessStep.isLastStep())")
    @Mapping(target = "totalProductCount", expression = "java(serviceProcessStep.getTotalProductCount())")
    @Mapping(target = "stepProducts", source = "stepProducts")
    ServiceProcessStepInfoDto toServiceProcessStepInfoDto(ServiceProcessStep serviceProcessStep);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "serviceProcess", ignore = true) // Will be set in service
    @Mapping(target = "stepProducts", ignore = true) // Will be handled separately
    @Mapping(target = "isDeleted", constant = "false")
    ServiceProcessStep toEntity(CreateServiceProcessStepRequest createServiceProcessStepRequest);
    
    default ServiceProcessStep updateEntity(ServiceProcessStep existingServiceProcessStep, UpdateServiceProcessStepRequest updateRequest) {
        if (updateRequest == null) {
            return existingServiceProcessStep;
        }
        
        if (updateRequest.getStepOrder() != null) {
            existingServiceProcessStep.setStepOrder(updateRequest.getStepOrder());
        }
        if (updateRequest.getName() != null) {
            existingServiceProcessStep.setName(updateRequest.getName());
        }
        if (updateRequest.getDescription() != null) {
            existingServiceProcessStep.setDescription(updateRequest.getDescription());
        }
        if (updateRequest.getEstimatedTime() != null) {
            existingServiceProcessStep.setEstimatedTime(updateRequest.getEstimatedTime());
        }
        if (updateRequest.getIsRequired() != null) {
            existingServiceProcessStep.setIsRequired(updateRequest.getIsRequired());
        }
        
        return existingServiceProcessStep;
    }
}
