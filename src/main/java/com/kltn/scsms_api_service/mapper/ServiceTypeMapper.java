package com.kltn.scsms_api_service.mapper;

import com.kltn.scsms_api_service.core.dto.serviceTypeManagement.ServiceTypeInfoDto;
import com.kltn.scsms_api_service.core.dto.serviceTypeManagement.request.CreateServiceTypeRequest;
import com.kltn.scsms_api_service.core.dto.serviceTypeManagement.request.UpdateServiceTypeRequest;
import com.kltn.scsms_api_service.core.entity.ServiceType;
import org.mapstruct.*;

/**
 * Mapper for ServiceType entity and DTOs
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = {AuditMapper.class})
public interface ServiceTypeMapper {
    
    /**
     * Map ServiceType entity to ServiceTypeInfoDto
     */
    @Mapping(target = "displayName", expression = "java(entity.getDisplayName())")
    ServiceTypeInfoDto toServiceTypeInfoDto(ServiceType entity);
    
    /**
     * Map CreateServiceTypeRequest to ServiceType entity
     */
    @Mapping(target = "serviceTypeId", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "modifiedDate", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "modifiedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    ServiceType toEntity(CreateServiceTypeRequest request);
    
    /**
     * Update ServiceType entity from UpdateServiceTypeRequest
     */
    @Mapping(target = "serviceTypeId", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "modifiedDate", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "modifiedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromRequest(UpdateServiceTypeRequest request, @MappingTarget ServiceType entity);
    
    /**
     * Update ServiceType entity from UpdateServiceTypeRequest and return updated entity
     */
    default ServiceType updateEntity(ServiceType existingEntity, UpdateServiceTypeRequest request) {
        updateEntityFromRequest(request, existingEntity);
        return existingEntity;
    }
}
