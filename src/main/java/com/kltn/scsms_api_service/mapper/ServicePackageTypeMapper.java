package com.kltn.scsms_api_service.mapper;

import com.kltn.scsms_api_service.core.dto.servicePackageTypeManagement.ServicePackageTypeInfoDto;
import com.kltn.scsms_api_service.core.dto.servicePackageTypeManagement.request.CreateServicePackageTypeRequest;
import com.kltn.scsms_api_service.core.dto.servicePackageTypeManagement.request.UpdateServicePackageTypeRequest;
import com.kltn.scsms_api_service.core.entity.ServicePackageType;
import org.mapstruct.*;

/**
 * Mapper for ServicePackageType entity and DTOs
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = {AuditMapper.class})
public interface ServicePackageTypeMapper {
    
    /**
     * Map ServicePackageType entity to ServicePackageTypeInfoDto
     */
    @Mapping(target = "displayName", source = "displayName")
    ServicePackageTypeInfoDto toServicePackageTypeInfoDto(ServicePackageType entity);
    
    /**
     * Map CreateServicePackageTypeRequest to ServicePackageType entity
     */
    @Mapping(target = "servicePackageTypeId", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "modifiedDate", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "modifiedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    ServicePackageType toEntity(CreateServicePackageTypeRequest request);
    
    /**
     * Update ServicePackageType entity from UpdateServicePackageTypeRequest
     */
    @Mapping(target = "servicePackageTypeId", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "modifiedDate", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "modifiedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromRequest(UpdateServicePackageTypeRequest request, @MappingTarget ServicePackageType entity);
    
    /**
     * Update ServicePackageType entity from UpdateServicePackageTypeRequest and return updated entity
     */
    default ServicePackageType updateEntity(ServicePackageType existingEntity, UpdateServicePackageTypeRequest request) {
        updateEntityFromRequest(request, existingEntity);
        return existingEntity;
    }
}
